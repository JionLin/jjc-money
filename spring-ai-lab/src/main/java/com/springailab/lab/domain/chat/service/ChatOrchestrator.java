package com.springailab.lab.domain.chat.service;

import com.springailab.lab.domain.runtime.model.RuntimeAnswer;
import com.springailab.lab.domain.runtime.model.RuntimeStreamEvent;
import com.springailab.lab.domain.runtime.service.JinjianRuntimeService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 这是聊天“编排层”。
 *
 * 如果把整条链路想成一个流水线：
 * - Controller 负责接单
 * - RuntimeService 负责真正做 AI / 检索 / 拼答案
 * - 那么 Orchestrator 就是中间的“流程调度员”
 *
 * 它本身不定义投资分析规则，也不直接写 HTTP 协议细节，
 * 它的主要职责是：
 * 1. 规范化输入参数
 * 2. 调用运行时服务拿到答案
 * 3. 记录指标、保存会话
 * 4. 如果是流式接口，就把答案拆好的事件逐个推送给前端
 *
 * 对不熟 Spring AI 的人来说，这一层特别重要，
 * 因为它把“Web 层”和“AI 运行时层”隔开了。
 */
@Service
public class ChatOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrator.class);

    private static final Duration STREAM_TIMEOUT = Duration.ofMinutes(5);

    private final JinjianRuntimeService runtimeService;

    private final ConversationMessageStore conversationMessageStore;

    private final MeterRegistry meterRegistry;

    /**
     * 构造器注入依赖。
     *
     * - `runtimeService`：真正执行“识别模式、查证据、拼回答”的核心服务
     * - `conversationMessageStore`：保存对话上下文
     * - `meterRegistry`：记录监控指标，比如耗时、错误数、降级数
     *
     * 所以这个类更像一个“总控台”，把几类能力拼在一起使用。
     */
    public ChatOrchestrator(JinjianRuntimeService runtimeService,
                            ConversationMessageStore conversationMessageStore,
                            MeterRegistry meterRegistry) {
        this.runtimeService = runtimeService;
        this.conversationMessageStore = conversationMessageStore;
        this.meterRegistry = meterRegistry;
    }

    /**
     * 非流式聊天入口。
     *
     * 适用于前端只想“一次性拿完整答案”的场景。
     *
     * 处理步骤：
     * 1. 先把 conversationId / message 规范化，避免出现 null、空字符串等问题
     * 2. 开始计时，后面用于统计接口耗时
     * 3. 调 `runtimeService.execute(...)` 真正执行聊天逻辑
     * 4. 把本轮问答写入会话存储
     * 5. 记录降级状态指标
     * 6. 把结果整理成一个简单 JSON 返回给前端
     *
     * 注意：这里返回的是 `ResponseEntity<Map<String, String>>`，
     * 本质上就是一个 HTTP 响应，里面放了一个字符串字典。
     */
    public ResponseEntity<Map<String, String>> chat(String message, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        String normalizedMessage = normalizeMessage(message);

        Timer.Sample sample = Timer.start(this.meterRegistry);
        RuntimeAnswer answer = this.runtimeService.execute(normalizedConversationId, normalizedMessage);
        sample.stop(Timer.builder("chat_runtime_latency").register(this.meterRegistry));

        appendConversation(normalizedConversationId, normalizedMessage, answer.reply());
        collectDegradeMetric(answer.degradeStatus().name());

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("reply", answer.reply());
        payload.put("mode", answer.mode().name());
        payload.put("traceId", answer.trace().getTraceId());
        payload.put("degradeStatus", answer.degradeStatus().name());
        return ResponseEntity.ok(payload);
    }

    /**
     * 流式聊天入口。
     *
     * 这是 `/chat/stream` 的核心实现。
     * 如果你以前没接触过 SSE，可以先把它理解成：
     * - 普通接口：后端攒完全部结果，一次性返回
     * - SSE 接口：后端拿到一点结果就先推一点，直到全部推完
     *
     * 处理步骤：
     * 1. 规范化输入
     * 2. 创建 `SseEmitter`，它代表一条和前端保持打开的事件流
     * 3. 开始计时并记录“流式请求数”
     * 4. 订阅 runtime 异步流，逐条发给前端
     * 5. 最后发送 `done` 事件，通知前端“本次流结束”
     * 6. 记录会话、指标，并正常关闭 emitter
     * 7. 如果中途异常，则发送 `error` 事件并结束流
     */
    public SseEmitter streamChat(String message, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        String normalizedMessage = normalizeMessage(message);

        // SseEmitter 可以理解成“持续向前端写事件的输出管道”。
        // 这里设置了超时时间：5 分钟。
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT.toMillis());
        Timer.Sample sample = Timer.start(this.meterRegistry);
        Counter.builder("chat_stream_requests_total").register(this.meterRegistry).increment();

        StringBuilder fullReply = new StringBuilder();
        this.runtimeService.executeStream(normalizedConversationId, normalizedMessage)
                .subscribe(
                        event -> {
                            if ("answer_chunk".equals(event.name())) {
                                fullReply.append(event.content());
                            }
                            if ("trace_end".equals(event.name())) {
                                String degradeStatus = (String) event.metadata().get("degradeStatus");
                                if (degradeStatus != null) {
                                    collectDegradeMetric(degradeStatus);
                                }
                            }
                            sendEvent(emitter, event.name(),
                                    new ChatEventPayload(normalizedConversationId, event.content(), event.name(), event.metadata()));
                        },
                        ex -> {
                            log.error("Stream runtime failed", ex);
                            Counter.builder("chat_stream_errors_total").register(this.meterRegistry).increment();
                            sendEvent(emitter, "error", new ChatEventPayload(normalizedConversationId,
                                    "runtime_failed", "error", Map.of("message", ex.getMessage())));
                            sample.stop(Timer.builder("chat_stream_latency").register(this.meterRegistry));
                            emitter.completeWithError(ex);
                        },
                        () -> {
                            sendEvent(emitter, "done", new ChatEventPayload(normalizedConversationId, "completed", "done", Map.of()));
                            appendConversation(normalizedConversationId, normalizedMessage, fullReply.toString());
                            sample.stop(Timer.builder("chat_stream_latency").register(this.meterRegistry));
                            emitter.complete();
                        }
                );

        return emitter;
    }

    /**
     * 记录降级状态指标。
     *
     * 所谓“降级”，你可以理解成：
     * 系统因为条件不满足，没有走到最理想的完整能力路径。
     * 例如：
     * - 想做 Ticker 分析，但 fresh data 没拿到
     * - 请求超出当前版本支持范围
     *
     * 这里把状态打到监控系统里，方便后续统计：
     * - 正常请求有多少
     * - degraded 有多少
     * - rejected 有多少
     */
    private void collectDegradeMetric(String degradeStatus) {
        Counter.builder("chat_runtime_degrade_total")
                .tag("status", degradeStatus)
                .register(this.meterRegistry)
                .increment();
    }

    /**
     * 保存本轮对话到会话存储。
     *
     * 这里保存的是最基础的问答文本：
     * - `U:` 开头表示 User
     * - `A:` 开头表示 Assistant
     *
     * 这样做的意义是：后续如果需要查看会话历史，或者做多轮上下文恢复，
     * 至少能拿到一份最基本的消息记录。
     */
    private void appendConversation(String conversationId, String userMessage, String assistantReply) {
        this.conversationMessageStore.appendMessage(conversationId, "U:" + userMessage);
        this.conversationMessageStore.appendMessage(conversationId, "A:" + assistantReply);
    }

    /**
     * 规范化会话 ID。
     *
     * 如果前端已经传了 conversationId，就去掉首尾空格后直接使用。
     * 如果前端没传，则自动生成一个新的 ID。
     *
     * 这样做的目的很实际：
     * 整条链路里很多地方都希望“每个请求至少有一个会话标识”，
     * 否则后面存消息、查 trace、做日志排查都会很麻烦。
     */
    private static String normalizeConversationId(String conversationId) {
        if (StringUtils.hasText(conversationId)) {
            return conversationId.trim();
        }
        return "conversation-" + UUID.randomUUID();
    }

    /**
     * 规范化用户消息。
     *
     * 对初学者来说，这一步虽然不起眼，但非常常见：
     * 任何面向用户输入的系统，真正执行业务前都要先做“输入清洗”。
     *
     * 这里的规则很简单：
     * - 如果 message 为空，就给一个默认值
     * - 否则去掉前后空格
     */
    private static String normalizeMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "你好";
        }
        return message.trim();
    }

    /**
     * 发送单个 SSE 事件。
     *
     * 可以把它理解成一个很薄的工具方法：
     * - 指定事件名 `eventName`
     * - 指定事件内容 `payload`
     * - 写入 `emitter`
     *
     * 如果底层网络连接断开、输出流关闭等原因导致写失败，
     * 这里会抛出异常，交给上面的 `try/catch` 统一处理。
     */
    private static void sendEvent(SseEmitter emitter, String eventName, ChatEventPayload payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
        } catch (IOException ex) {
            throw new IllegalStateException("SSE send failed", ex);
        }
    }
}
