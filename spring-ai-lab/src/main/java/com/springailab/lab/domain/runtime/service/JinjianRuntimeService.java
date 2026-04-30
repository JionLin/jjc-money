package com.springailab.lab.domain.runtime.service;

import com.springailab.lab.domain.runtime.archive.ArchiveEvidence;
import com.springailab.lab.domain.runtime.archive.ArchiveEvidenceService;
import com.springailab.lab.domain.runtime.fresh.FreshDataResult;
import com.springailab.lab.domain.runtime.fresh.FreshDataService;
import com.springailab.lab.domain.runtime.model.CitationRecord;
import com.springailab.lab.domain.runtime.model.DegradeStatus;
import com.springailab.lab.domain.runtime.model.FreshFactRecord;
import com.springailab.lab.domain.runtime.model.RuntimeAnswer;
import com.springailab.lab.domain.runtime.model.RuntimeMode;
import com.springailab.lab.domain.runtime.model.RuntimeStreamEvent;
import com.springailab.lab.domain.runtime.routing.ModeDecision;
import com.springailab.lab.domain.runtime.routing.ModeRouter;
import com.springailab.lab.domain.runtime.skill.SkillLoader;
import com.springailab.lab.domain.runtime.skill.SkillSnapshot;
import com.springailab.lab.domain.runtime.trace.RuntimeTrace;
import com.springailab.lab.domain.runtime.trace.RuntimeTraceStore;
import com.springailab.lab.domain.runtime.trace.ToolCallRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这是 AI 运行时核心服务。
 *
 * 如果把 `ChatController` 看成“HTTP 入口”，
 * 把 `ChatOrchestrator` 看成“流程调度层”，
 * 那这个类就是“真正干活的人”。
 *
 * 它负责的事情包括：
 * 1. 判断用户问题属于哪种模式（比如 Ticker / Historical View）
 * 2. 去本地归档里找历史证据
 * 3. 在需要时去拿 fresh data（实时数据 / 新事实）
 * 4. 把这些材料拼成最终答复
 * 5. 再把答复转换成适合前端流式显示的事件列表
 *
 * 从“Spring AI 初学者”的角度说，
 * 这里更像一个“应用层 AI runtime”，
 * 而不是直接调用某个模型 SDK 的最底层代码。
 * 它的重点是：把业务规则、检索规则、输出结构组织起来。
 */
@Service
public class JinjianRuntimeService {

    private static final Pattern TICKER_PATTERN = Pattern.compile("\\b[A-Z]{1,5}\\b");

    private final SkillLoader skillLoader;

    private final ModeRouter modeRouter;

    private final ArchiveEvidenceService archiveEvidenceService;

    private final FreshDataService freshDataService;

    private final RuntimeTraceStore traceStore;

    private final ChatClient chatClient;

    /**
     * 构造器注入运行时依赖。
     *
     * 各依赖的职责可以先这样记：
     * - `SkillLoader`：读取当前激活的 skill 配置
     * - `ModeRouter`：判断用户问题应该走哪种模式
     * - `ArchiveEvidenceService`：去本地归档找历史文章证据
     * - `FreshDataService`：获取当前新的事实数据
     * - `RuntimeTraceStore`：保存本次运行的 trace，方便调试和回放
     */
    public JinjianRuntimeService(SkillLoader skillLoader,
                                 ModeRouter modeRouter,
                                 ArchiveEvidenceService archiveEvidenceService,
                                 FreshDataService freshDataService,
                                 RuntimeTraceStore traceStore,
                                 ChatClient.Builder chatClientBuilder) {
        this.skillLoader = skillLoader;
        this.modeRouter = modeRouter;
        this.archiveEvidenceService = archiveEvidenceService;
        this.freshDataService = freshDataService;
        this.traceStore = traceStore;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 运行时总入口。
     *
     * 不管前端问什么，最后都会先来到这里。
     * 你可以把这个方法理解成一个“总调度方法”。
     *
     * 核心步骤：
     * 1. 创建 `trace`
     *    trace 就像本次请求的“执行记录单”，后面会记录模式、耗时、工具调用等信息
     * 2. 读取当前激活 skill 的版本信息
     * 3. 调用 `modeRouter.route(message)` 判断这条问题该走哪条分支
     * 4. 根据模式进入不同执行方法
     * 5. 执行完成后，补充耗时、结束时间、成本等 trace 信息
     * 6. 把最终答案再转成 stream events，供 `/chat/stream` 使用
     *
     * 也就是说：
     * - `/chat` 最终会用到这里返回的 `reply`
     * - `/chat/stream` 最终会用到这里返回的 `streamEvents`
     */
    public RuntimeAnswer execute(String conversationId, String message) {
        Instant started = Instant.now();
        RuntimeTrace trace = new RuntimeTrace("trace-" + UUID.randomUUID(), conversationId, started);

        SkillSnapshot skill = this.skillLoader.getActiveSkill();
        trace.setSkillVersion(skill.version());

        ModeDecision decision = this.modeRouter.route(message);
        trace.setMode(decision.mode());
        trace.setModeSource(decision.source());
        trace.setModeConfidence(decision.confidence());

        RuntimeAnswer answer = switch (decision.mode()) {
            case TICKER -> executeTickerMode(trace, message, decision);
            case HISTORICAL_VIEW -> executeHistoricalMode(trace, message, decision);
            case PORTFOLIO, PERSONAL_PORTFOLIO -> executePortfolioDowngrade(trace, message, decision);
            case REJECT_NON_INVESTING -> executeNonInvestingReject(trace, message, decision);
            case UNKNOWN -> executeHistoricalMode(trace, message, decision);
        };

        Instant finished = Instant.now();
        trace.setLatencyMillis(Duration.between(started, finished).toMillis());
        trace.finish(finished);
        trace.setEstimatedCost(0D);
        this.traceStore.put(trace);

        List<RuntimeStreamEvent> streamEvents = buildStreamEvents(answer, trace);
        return new RuntimeAnswer(
                answer.reply(),
                answer.mode(),
                answer.citations(),
                answer.freshFact(),
                answer.degradeStatus(),
                answer.disclaimer(),
                trace,
                streamEvents);
    }

    /**
     * 真流式总入口（引入大模型）。
     *
     * 1. 复用原有的 execute 获取数据。
     * 2. 保留元数据事件。
     * 3. 接入 Spring AI ChatClient，将数据转化为上下文，交由大模型生成。
     */
    public Flux<RuntimeStreamEvent> executeStream(String conversationId, String message) {
        // 1. 获取证据和上下文 (非常快，纯本地操作)
        RuntimeAnswer answer = this.execute(conversationId, message);
        RuntimeTrace trace = answer.trace();

        // 2. 准备元数据流 (立即推送给前端)
        List<RuntimeStreamEvent> metaEvents = new ArrayList<>(answer.streamEvents());
        // 过滤掉原有的伪流式正文和结尾事件
        metaEvents.removeIf(e -> e.name().equals("answer_chunk") || e.name().equals("disclaimer") || e.name().equals("trace_end"));
        Flux<RuntimeStreamEvent> metaFlux = Flux.fromIterable(metaEvents);

        // 3. 构建大模型 Prompt
        String systemPrompt = loadSkillPrompt();
        String contextStr = buildLlmContext(answer);

        // 4. 调用大模型，实时生成并转为事件流
        Flux<RuntimeStreamEvent> llmFlux = this.chatClient.prompt()
                .system(sys -> sys.text(systemPrompt + "\n\n" + contextStr))
                .user(message)
                .stream()
                .content()
                .filter(StringUtils::hasText)
                .map(chunk -> new RuntimeStreamEvent("answer_chunk", chunk, Map.of()));

        // 5. 准备收尾流
        Flux<RuntimeStreamEvent> endFlux = Flux.defer(() -> {
            trace.setLatencyMillis(Duration.between(trace.getStartedAt(), Instant.now()).toMillis());
            this.traceStore.put(trace);
            
            Map<String, Object> tail = new HashMap<>();
            tail.put("traceId", trace.getTraceId());
            tail.put("latencyMs", trace.getLatencyMillis());
            tail.put("degradeStatus", answer.degradeStatus().name());
            tail.put("estimatedCost", trace.getEstimatedCost());
            
            return Flux.just(
                    new RuntimeStreamEvent("disclaimer", answer.disclaimer(), Map.of("degradeStatus", answer.degradeStatus().name())),
                    new RuntimeStreamEvent("trace_end", "done", tail)
            );
        });

        // 6. 像水管一样拼起来
        return Flux.concat(metaFlux, llmFlux, endFlux);
    }

    private String loadSkillPrompt() {
        try {
            return Files.readString(Path.of("d:/jjc-money/.agent/skills/jinjian-perspective/SKILL.md"));
        } catch (Exception e) {
            return "You are an investment assistant.";
        }
    }

    private String buildLlmContext(RuntimeAnswer answer) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RETRIEVED CONTEXT ===\n");
        sb.append("Current Mode: ").append(answer.mode().name()).append("\n");
        if (answer.freshFact() != null) {
            sb.append("Fresh Facts:\n- Ticker: ").append(answer.freshFact().ticker()).append("\n");
            sb.append("- Data: ").append(answer.freshFact().facts()).append("\n");
        }
        sb.append("Archive Evidence:\n");
        for (CitationRecord c : answer.citations()) {
            sb.append("- ").append(c.excerpt()).append("\n");
        }
        sb.append("=========================\n");
        sb.append("请严格遵循 SKILL 中的约束。如果你认为 Context 证据不足以支撑当前结论，请明确指出。\n");
        return sb.toString();
    }

    /**
     * 执行 Ticker 模式。
     *
     * 什么时候会走到这里？
     * 一般是系统判断用户在问某个股票 / 标的的当前分析问题时。
     *
     * 这个分支和 Historical View 最大的区别是：
     * 它不仅查“过去怎么说”，还尽量查“现在的数据是什么”，
     * 所以它更接近“当前分析模式”。
     *
     * 处理流程：
     * 1. 先解析 ticker
     * 2. 如果 ticker 都解析不出来，就只能降级
     * 3. 查本地原始归档证据
     * 4. 查派生分析文档补充上下文
     * 5. 查 fresh data
     * 6. 如果 fresh data 拿不到，生成降级版回复
     * 7. 如果 fresh data 拿到了，生成完整版回复
     */
    private RuntimeAnswer executeTickerMode(RuntimeTrace trace, String message, ModeDecision decision) {
        String ticker = resolveTicker(decision, message);
        if (!StringUtils.hasText(ticker)) {
            trace.setDegradeStatus(DegradeStatus.DEGRADED);
            trace.setDegradeReason("ticker missing");
            String reply = "我需要明确的 ticker 才能执行 v1 Ticker 流程。当前先降级为历史框架演练。";
            return new RuntimeAnswer(reply, RuntimeMode.TICKER, List.of(), null, DegradeStatus.DEGRADED,
                    buildDisclaimer(DegradeStatus.DEGRADED), trace, List.of());
        }

        // 第一步：查原始归档。
        // 这些归档是“作者过去真实写过的内容”，优先级最高。
        List<ArchiveEvidence> primaryEvidence = this.archiveEvidenceService.searchLocalArchive(ticker, message, 4);
        trace.addToolCall(new ToolCallRecord("searchLocalArchive", "ok", "matches=" + primaryEvidence.size()));

        List<CitationRecord> citations = new ArrayList<>();
        for (ArchiveEvidence evidence : primaryEvidence) {
            citations.add(new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType()));
            trace.addCitation(new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType()));
        }

        // 第二步：查派生分析文档。
        // 这些通常是对原始材料做过整理的辅助资料，能帮回答更完整，但不是最原始证据。
        List<ArchiveEvidence> derived = this.archiveEvidenceService.searchDerivedDocs(ticker + " " + message, 2);
        trace.addToolCall(new ToolCallRecord("searchDerivedDocs", "ok", "matches=" + derived.size()));
        for (ArchiveEvidence evidence : derived) {
            citations.add(new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType()));
            trace.addCitation(new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType()));
        }

        // 第三步：查 fresh data。
        // Ticker 模式想回答“现在怎么看”，就必须尽量有当前事实数据。
        // 如果拿不到 fresh data，就说明不能给太像“当前操作建议”的回答，只能降级。
        FreshDataResult fresh = this.freshDataService.fetchFreshFacts(ticker, trace);
        if (!fresh.success()) {
            trace.setDegradeStatus(DegradeStatus.DEGRADED);
            trace.setDegradeReason(fresh.degradeReason());
            String reply = buildDegradedTickerReply(ticker, citations, fresh.degradeReason());
            return new RuntimeAnswer(reply, RuntimeMode.TICKER, citations, null, DegradeStatus.DEGRADED,
                    buildDisclaimer(DegradeStatus.DEGRADED), trace, List.of());
        }

        FreshFactRecord fact = fresh.factRecord();
        String reply = buildTickerReply(ticker, fact, citations);
        return new RuntimeAnswer(reply, RuntimeMode.TICKER, citations, fact, DegradeStatus.NONE,
                buildDisclaimer(DegradeStatus.NONE), trace, List.of());
    }

    /**
     * 执行 Historical View 模式。
     *
     * 这个分支的定位是：
     * “帮用户回看作者过去是怎么想、怎么写、怎么判断的。”
     *
     * 它和 Ticker 模式不同的地方在于：
     * - 不强依赖 fresh data
     * - 更像资料检索 + 观点复盘
     * - 输出时也会明确提醒：这不是当前操作建议
     */
    private RuntimeAnswer executeHistoricalMode(RuntimeTrace trace, String message, ModeDecision decision) {
        String ticker = resolveTicker(decision, message);
        List<ArchiveEvidence> primaryEvidence = this.archiveEvidenceService.searchLocalArchive(ticker, message, 5);
        trace.addToolCall(new ToolCallRecord("searchLocalArchive", "ok", "matches=" + primaryEvidence.size()));

        List<CitationRecord> citations = new ArrayList<>();
        for (ArchiveEvidence evidence : primaryEvidence) {
            CitationRecord citation = new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType());
            citations.add(citation);
            trace.addCitation(citation);
        }

        // 如果连原始归档都没搜到，就退一步查派生文档。
        // 这样至少还能给用户一条可继续理解问题的线索，而不是完全空结果。
        if (citations.isEmpty()) {
            List<ArchiveEvidence> derived = this.archiveEvidenceService.searchDerivedDocs(message, 3);
            trace.addToolCall(new ToolCallRecord("searchDerivedDocs", "ok", "matches=" + derived.size()));
            for (ArchiveEvidence evidence : derived) {
                CitationRecord citation = new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType());
                citations.add(citation);
                trace.addCitation(citation);
            }
        }

        String reply = buildHistoricalReply(ticker, citations);
        return new RuntimeAnswer(reply, RuntimeMode.HISTORICAL_VIEW, citations, null, DegradeStatus.NONE,
                buildDisclaimer(DegradeStatus.NONE), trace, List.of());
    }

    /**
     * 处理“投资组合 / 仓位配置”类问题，但当前只做降级提示。
     *
     * 原因不是代码报错，而是产品能力边界如此：
     * 当前 v1 版本不打算直接给个性化仓位建议，
     * 所以这里会返回一个“目前不支持该能力，但你可以换成其他模式提问”的结果。
     */
    private RuntimeAnswer executePortfolioDowngrade(RuntimeTrace trace, String message, ModeDecision decision) {
        trace.setDegradeStatus(DegradeStatus.DEGRADED);
        trace.setDegradeReason("portfolio mode is out of v1 scope");
        String label = decision.mode() == RuntimeMode.PERSONAL_PORTFOLIO ? "Personal Portfolio" : "Portfolio";
        String reply = "v1 当前不支持 " + label + " 个性化配置建议。\n"
                + "我可以继续按 `Ticker` 或 `Historical View` 模式，提供非个性化的框架分析。";
        return new RuntimeAnswer(reply, decision.mode(), List.of(), null, DegradeStatus.DEGRADED,
                buildDisclaimer(DegradeStatus.DEGRADED), trace, List.of());
    }

    /**
     * 处理非投资类请求。
     *
     * 这个分支的作用很简单：
     * 让系统明确表达“当前版本只服务投资场景”，
     * 而不是对任何问题都硬答一遍。
     */
    private RuntimeAnswer executeNonInvestingReject(RuntimeTrace trace, String message, ModeDecision decision) {
        trace.setDegradeStatus(DegradeStatus.REJECTED);
        trace.setDegradeReason("non-investing request out of v1 scope");
        String reply = "v1 只支持投资相关问题（Ticker / Historical View）。\n"
                + "当前请求不在支持范围内，因此已拒绝。";
        return new RuntimeAnswer(reply, decision.mode(), List.of(), null, DegradeStatus.REJECTED,
                buildDisclaimer(DegradeStatus.REJECTED), trace, List.of());
    }

    /**
     * 从用户问题里尽量提取 ticker。
     *
     * 优先级：
     * 1. 先相信 `ModeRouter` 已经提取好的结果
     * 2. 如果路由器没提取到，就用一个简单正则兜底匹配
     *
     * 这里的正则 `\\b[A-Z]{1,5}\\b` 可以粗略理解成：
     * 匹配 1 到 5 位连续大写英文字母，
     * 例如 `NVDA`、`TSM`、`META`
     */
    private static String resolveTicker(ModeDecision decision, String message) {
        if (StringUtils.hasText(decision.extractedTicker())) {
            return decision.extractedTicker().toUpperCase(Locale.ROOT);
        }
        Matcher matcher = TICKER_PATTERN.matcher(message == null ? "" : message);
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() > 1) {
                return token;
            }
        }
        return "";
    }

    /**
     * 组装 Ticker 模式下的完整回复文本。
     *
     * 这里不是再去查数据，而是把前面已经拿到的材料“排版成最终回复”。
     * 目前结构分三段：
     * 1. Fresh Facts：当前事实数据
     * 2. Historical Evidence：历史归档证据
     * 3. Framework：固定解释框架
     *
     * 也就是说，这个方法更像“视图拼装器”。
     */
    private static String buildTickerReply(String ticker, FreshFactRecord fact, List<CitationRecord> citations) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Mode: Ticker\n\n");
        sb.append("### Fresh Facts\n");
        sb.append("- Ticker: ").append(ticker).append("\n");
        sb.append("- Source: ").append(fact.source()).append("\n");
        sb.append("- As-Of: ").append(fact.asOf()).append("\n");
        sb.append("- Price: ").append(fact.facts().getOrDefault("price", "N/A")).append("\n");
        sb.append("- Valuation: ").append(fact.facts().getOrDefault("valuation", "N/A")).append("\n");

        sb.append("\n### Historical Evidence (Verified Raw Archive)\n");
        appendCitations(sb, citations);

        sb.append("\n### Framework\n");
        sb.append("先看趋势，再看估值，再看仓位节奏；避免把历史观点直接当作当前操作结论。\n");
        return sb.toString();
    }

    /**
     * 组装 Ticker 模式降级时的回复文本。
     *
     * 典型场景：
     * - 用户问的是一个当前 ticker 分析问题
     * - 但系统没拿到 fresh data
     *
     * 这时不能假装自己知道“当前情况”，
     * 所以这里只给历史证据 + 框架提示，并明确说明已经降级。
     */
    private static String buildDegradedTickerReply(String ticker, List<CitationRecord> citations, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Mode: Ticker (Degraded)\n\n");
        sb.append("无法获取当前必需的 fresh facts（").append(reason).append("）。\n");
        sb.append("以下仅为历史框架演练，不构成当前操作建议。\n\n");
        sb.append("### Historical Evidence\n");
        appendCitations(sb, citations);
        sb.append("\n### Framework\n");
        sb.append("围绕 ").append(ticker).append(" 的历史观点可用于复盘思路，但当前执行需等待最新事实数据。\n");
        return sb.toString();
    }

    /**
     * 组装 Historical View 模式的回复文本。
     *
     * 这类回复的设计目标不是“现在该买还是卖”，
     * 而是“作者过去是怎么判断这个问题的”。
     * 所以文案上会显式区分历史观点和当前建议。
     */
    private static String buildHistoricalReply(String ticker, List<CitationRecord> citations) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Mode: Historical View\n\n");
        sb.append("以下为作者历史观点复盘，不是当前操作建议。\n\n");
        if (StringUtils.hasText(ticker)) {
            sb.append("- Focus ticker: ").append(ticker).append("\n\n");
        }
        sb.append("### Verified Raw Evidence\n");
        appendCitations(sb, citations);
        sb.append("\n### Historical Interpretation\n");
        sb.append("重点看当时的趋势判断、仓位节奏和风控边界，而不是机械复刻结论。\n");
        return sb.toString();
    }

    /**
     * 把证据列表追加到最终回复里。
     *
     * 如果找到了证据，就逐条列出来。
     * 如果一条都没找到，也会明确告诉用户“没找到”，
     * 这样前端或调用方就不会误以为系统漏渲染了内容。
     */
    private static void appendCitations(StringBuilder sb, List<CitationRecord> citations) {
        if (citations == null || citations.isEmpty()) {
            sb.append("- 暂未检索到可验证证据，请换更具体的时间/标的关键词。\n");
            return;
        }
        for (CitationRecord citation : citations) {
            sb.append("- ")
                    .append(citation.filePath())
                    .append(" @ ")
                    .append(citation.locator())
                    .append(" [")
                    .append(citation.contextType())
                    .append("] ")
                    .append(citation.excerpt())
                    .append("\n");
        }
    }

    /**
     * 根据当前状态生成统一免责声明。
     *
     * 这样做的好处是：
     * - 文案统一
     * - 普通接口和流式接口都能复用
     * - 后续如果要改提示语，只改这一个地方
     */
    private static String buildDisclaimer(DegradeStatus status) {
        if (status == DegradeStatus.REJECTED) {
            return "Scope notice: v1 supports investing questions only.";
        }
        if (status == DegradeStatus.DEGRADED) {
            return "Degrade notice: historical framework exercise only, not actionable advice.";
        }
        return "Risk disclaimer: this output is for research discussion, not investment advice.";
    }

    /**
     * 把最终答案转换成 SSE 事件列表。
     *
     * 这是理解流式接口的关键方法之一。
     *
     * 为什么要做这一步？
     * 因为前端的流式 UI 往往不只是想拿一段最终文本，
     * 它还可能想实时显示：
     * - 当前走的模式是什么
     * - 查了哪些资料
     * - 有没有 fresh data
     * - 正文现在输出到哪一段了
     *
     * 所以这里不是只返回一个字符串，
     * 而是返回一个“事件数组”，每个事件各自承担不同职责。
     */
    private static List<RuntimeStreamEvent> buildStreamEvents(RuntimeAnswer answer, RuntimeTrace trace) {
        List<RuntimeStreamEvent> events = new ArrayList<>();

        // 事件 1：告诉前端“这次执行开始了”，同时带上 traceId 和 skillVersion。
        events.add(new RuntimeStreamEvent("trace_start", "start",
                Map.of("traceId", trace.getTraceId(), "skillVersion", trace.getSkillVersion())));

        // 事件 2：告诉前端“系统把这条问题识别成了什么模式”。
        events.add(new RuntimeStreamEvent("mode_detected", trace.getMode().name(),
                Map.of("source", trace.getModeSource(), "confidence", trace.getModeConfidence())));

        // 事件 3：把运行中调用过的工具步骤抛给前端，方便做调试或过程展示。
        for (ToolCallRecord call : trace.getToolCalls()) {
            events.add(new RuntimeStreamEvent("tool_call", call.name(),
                    Map.of("status", call.status(), "detail", call.detail())));
        }

        // 事件 4：把引用来源逐条发出去，前端可以做“依据来源”展示。
        for (CitationRecord citation : answer.citations()) {
            events.add(new RuntimeStreamEvent("source_cited", citation.excerpt(), Map.of(
                    "filePath", citation.filePath(),
                    "locator", citation.locator(),
                    "contextType", citation.contextType().name())));
        }

        // 事件 5：如果有 fresh data，再额外告诉前端当前事实数据来自哪里、时间点是什么。
        if (answer.freshFact() != null) {
            events.add(new RuntimeStreamEvent("fresh_data", answer.freshFact().ticker(), Map.of(
                    "source", answer.freshFact().source(),
                    "asOf", answer.freshFact().asOf().toString(),
                    "fromCache", answer.freshFact().fromCache())));
        }

        // 事件 6：把正文切成多块，每块作为一个 answer_chunk 推送。
        // 这样前端就可以逐段渲染，看起来像模型正在边想边输出。
        for (String chunk : chunkText(answer.reply(), 320)) {
            events.add(new RuntimeStreamEvent("answer_chunk", chunk, Map.of()));
        }

        // 事件 7：正文完了以后，再补一个免责声明事件。
        events.add(new RuntimeStreamEvent("disclaimer", answer.disclaimer(),
                Map.of("degradeStatus", answer.degradeStatus().name())));

        // 事件 8：收尾事件，用来告诉前端这次流程真的结束了，并附带一些元信息。
        Map<String, Object> tail = new HashMap<>();
        tail.put("traceId", trace.getTraceId());
        tail.put("latencyMs", trace.getLatencyMillis());
        tail.put("degradeStatus", trace.getDegradeStatus().name());
        tail.put("estimatedCost", trace.getEstimatedCost());
        events.add(new RuntimeStreamEvent("trace_end", "done", tail));

        return events;
    }

    /**
     * 按固定最大长度切分文本。
     *
     * 这不是自然语言层面的智能分段，
     * 只是一个简单、稳定的“按字符数切块”策略。
     * 它的目的不是让文本更优美，而是方便流式输出。
     */
    private static List<String> chunkText(String text, int maxChars) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }
}
