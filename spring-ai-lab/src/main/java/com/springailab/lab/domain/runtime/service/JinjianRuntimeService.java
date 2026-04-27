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
import org.springframework.stereotype.Service;
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
 * Executes Jinjian v1 runtime contract for Ticker and Historical View modes.
 */
@Service
public class JinjianRuntimeService {

    private static final Pattern TICKER_PATTERN = Pattern.compile("\\b[A-Z]{1,5}\\b");

    private final SkillLoader skillLoader;

    private final ModeRouter modeRouter;

    private final ArchiveEvidenceService archiveEvidenceService;

    private final FreshDataService freshDataService;

    private final RuntimeTraceStore traceStore;

    public JinjianRuntimeService(SkillLoader skillLoader,
                                 ModeRouter modeRouter,
                                 ArchiveEvidenceService archiveEvidenceService,
                                 FreshDataService freshDataService,
                                 RuntimeTraceStore traceStore) {
        this.skillLoader = skillLoader;
        this.modeRouter = modeRouter;
        this.archiveEvidenceService = archiveEvidenceService;
        this.freshDataService = freshDataService;
        this.traceStore = traceStore;
    }

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

    private RuntimeAnswer executeTickerMode(RuntimeTrace trace, String message, ModeDecision decision) {
        String ticker = resolveTicker(decision, message);
        if (!StringUtils.hasText(ticker)) {
            trace.setDegradeStatus(DegradeStatus.DEGRADED);
            trace.setDegradeReason("ticker missing");
            String reply = "我需要明确的 ticker 才能执行 v1 Ticker 流程。当前先降级为历史框架演练。";
            return new RuntimeAnswer(reply, RuntimeMode.TICKER, List.of(), null, DegradeStatus.DEGRADED,
                    buildDisclaimer(DegradeStatus.DEGRADED), trace, List.of());
        }

        List<ArchiveEvidence> primaryEvidence = this.archiveEvidenceService.searchLocalArchive(ticker, message, 4);
        trace.addToolCall(new ToolCallRecord("searchLocalArchive", "ok", "matches=" + primaryEvidence.size()));

        List<CitationRecord> citations = new ArrayList<>();
        for (ArchiveEvidence evidence : primaryEvidence) {
            citations.add(new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType()));
            trace.addCitation(new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType()));
        }

        List<ArchiveEvidence> derived = this.archiveEvidenceService.searchDerivedDocs(ticker + " " + message, 2);
        trace.addToolCall(new ToolCallRecord("searchDerivedDocs", "ok", "matches=" + derived.size()));
        for (ArchiveEvidence evidence : derived) {
            citations.add(new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType()));
            trace.addCitation(new CitationRecord(evidence.filePath(), evidence.locator(), evidence.excerpt(), evidence.contextType()));
        }

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

    private RuntimeAnswer executePortfolioDowngrade(RuntimeTrace trace, String message, ModeDecision decision) {
        trace.setDegradeStatus(DegradeStatus.DEGRADED);
        trace.setDegradeReason("portfolio mode is out of v1 scope");
        String label = decision.mode() == RuntimeMode.PERSONAL_PORTFOLIO ? "Personal Portfolio" : "Portfolio";
        String reply = "v1 当前不支持 " + label + " 个性化配置建议。\n"
                + "我可以继续按 `Ticker` 或 `Historical View` 模式，提供非个性化的框架分析。";
        return new RuntimeAnswer(reply, decision.mode(), List.of(), null, DegradeStatus.DEGRADED,
                buildDisclaimer(DegradeStatus.DEGRADED), trace, List.of());
    }

    private RuntimeAnswer executeNonInvestingReject(RuntimeTrace trace, String message, ModeDecision decision) {
        trace.setDegradeStatus(DegradeStatus.REJECTED);
        trace.setDegradeReason("non-investing request out of v1 scope");
        String reply = "v1 只支持投资相关问题（Ticker / Historical View）。\n"
                + "当前请求不在支持范围内，因此已拒绝。";
        return new RuntimeAnswer(reply, decision.mode(), List.of(), null, DegradeStatus.REJECTED,
                buildDisclaimer(DegradeStatus.REJECTED), trace, List.of());
    }

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

    private static String buildDisclaimer(DegradeStatus status) {
        if (status == DegradeStatus.REJECTED) {
            return "Scope notice: v1 supports investing questions only.";
        }
        if (status == DegradeStatus.DEGRADED) {
            return "Degrade notice: historical framework exercise only, not actionable advice.";
        }
        return "Risk disclaimer: this output is for research discussion, not investment advice.";
    }

    private static List<RuntimeStreamEvent> buildStreamEvents(RuntimeAnswer answer, RuntimeTrace trace) {
        List<RuntimeStreamEvent> events = new ArrayList<>();

        events.add(new RuntimeStreamEvent("trace_start", "start",
                Map.of("traceId", trace.getTraceId(), "skillVersion", trace.getSkillVersion())));

        events.add(new RuntimeStreamEvent("mode_detected", trace.getMode().name(),
                Map.of("source", trace.getModeSource(), "confidence", trace.getModeConfidence())));

        for (ToolCallRecord call : trace.getToolCalls()) {
            events.add(new RuntimeStreamEvent("tool_call", call.name(),
                    Map.of("status", call.status(), "detail", call.detail())));
        }

        for (CitationRecord citation : answer.citations()) {
            events.add(new RuntimeStreamEvent("source_cited", citation.excerpt(), Map.of(
                    "filePath", citation.filePath(),
                    "locator", citation.locator(),
                    "contextType", citation.contextType().name())));
        }

        if (answer.freshFact() != null) {
            events.add(new RuntimeStreamEvent("fresh_data", answer.freshFact().ticker(), Map.of(
                    "source", answer.freshFact().source(),
                    "asOf", answer.freshFact().asOf().toString(),
                    "fromCache", answer.freshFact().fromCache())));
        }

        for (String chunk : chunkText(answer.reply(), 320)) {
            events.add(new RuntimeStreamEvent("answer_chunk", chunk, Map.of()));
        }

        events.add(new RuntimeStreamEvent("disclaimer", answer.disclaimer(),
                Map.of("degradeStatus", answer.degradeStatus().name())));

        Map<String, Object> tail = new HashMap<>();
        tail.put("traceId", trace.getTraceId());
        tail.put("latencyMs", trace.getLatencyMillis());
        tail.put("degradeStatus", trace.getDegradeStatus().name());
        tail.put("estimatedCost", trace.getEstimatedCost());
        events.add(new RuntimeStreamEvent("trace_end", "done", tail));

        return events;
    }

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
