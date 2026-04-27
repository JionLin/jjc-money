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
 * Chat orchestration backed by Jinjian v1 runtime contract.
 */
@Service
public class ChatOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrator.class);

    private static final Duration STREAM_TIMEOUT = Duration.ofMinutes(5);

    private final JinjianRuntimeService runtimeService;

    private final ConversationMessageStore conversationMessageStore;

    private final MeterRegistry meterRegistry;

    public ChatOrchestrator(JinjianRuntimeService runtimeService,
                            ConversationMessageStore conversationMessageStore,
                            MeterRegistry meterRegistry) {
        this.runtimeService = runtimeService;
        this.conversationMessageStore = conversationMessageStore;
        this.meterRegistry = meterRegistry;
    }

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

    public SseEmitter streamChat(String message, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        String normalizedMessage = normalizeMessage(message);

        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT.toMillis());
        Timer.Sample sample = Timer.start(this.meterRegistry);
        Counter.builder("chat_stream_requests_total").register(this.meterRegistry).increment();

        CompletableFuture.runAsync(() -> {
            try {
                RuntimeAnswer answer = this.runtimeService.execute(normalizedConversationId, normalizedMessage);
                for (RuntimeStreamEvent event : answer.streamEvents()) {
                    sendEvent(emitter, event.name(),
                            new ChatEventPayload(normalizedConversationId, event.content(), event.name(), event.metadata()));
                }
                sendEvent(emitter, "done", new ChatEventPayload(normalizedConversationId, "completed", "done",
                        Map.of("traceId", answer.trace().getTraceId())));

                appendConversation(normalizedConversationId, normalizedMessage, answer.reply());
                collectDegradeMetric(answer.degradeStatus().name());
                sample.stop(Timer.builder("chat_stream_latency").register(this.meterRegistry));
                emitter.complete();
            } catch (Exception ex) {
                log.error("Stream runtime failed", ex);
                Counter.builder("chat_stream_errors_total").register(this.meterRegistry).increment();
                sendEvent(emitter, "error", new ChatEventPayload(normalizedConversationId,
                        "runtime_failed", "error", Map.of("message", ex.getMessage())));
                sample.stop(Timer.builder("chat_stream_latency").register(this.meterRegistry));
                emitter.completeWithError(ex);
            }
        });

        return emitter;
    }

    private void collectDegradeMetric(String degradeStatus) {
        Counter.builder("chat_runtime_degrade_total")
                .tag("status", degradeStatus)
                .register(this.meterRegistry)
                .increment();
    }

    private void appendConversation(String conversationId, String userMessage, String assistantReply) {
        this.conversationMessageStore.appendMessage(conversationId, "U:" + userMessage);
        this.conversationMessageStore.appendMessage(conversationId, "A:" + assistantReply);
    }

    private static String normalizeConversationId(String conversationId) {
        if (StringUtils.hasText(conversationId)) {
            return conversationId.trim();
        }
        return "conversation-" + UUID.randomUUID();
    }

    private static String normalizeMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "你好";
        }
        return message.trim();
    }

    private static void sendEvent(SseEmitter emitter, String eventName, ChatEventPayload payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
        } catch (IOException ex) {
            throw new IllegalStateException("SSE send failed", ex);
        }
    }
}
