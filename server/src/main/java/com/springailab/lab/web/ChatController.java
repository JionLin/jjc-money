package com.springailab.lab.web;

import com.springailab.lab.domain.chat.service.ChatOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 最小对话 HTTP 入口，触发 DeepSeek + 工具调用链路。
 *
 * @author jiaolin
 */
@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatOrchestrator chatOrchestrator;

    /**
     * @param chatOrchestrator 对话编排服务
     */
    public ChatController(ChatOrchestrator chatOrchestrator) {
        this.chatOrchestrator = chatOrchestrator;
    }

    /**
     * 发送用户消息，由模型决定是否调用 {@link WeatherTools} 中的工具。
     *
     * @param request 请求体
     * @return 模型最终文本回复
     */
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        log.info("Chat request received, messageLength={},request={}",
                request.message() == null ? 0 : request.message().length(), request);
        return this.chatOrchestrator.chat(request.message(), request.conversationId());
    }

    /**
     * 流式对话入口。
     *
     * @param request 请求体
     * @return SSE 发射器
     */
    @PostMapping(value = "/chat/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        log.info("Stream chat request received, messageLength={},conversationId={}",
                request.message() == null ? 0 : request.message().length(), request.conversationId());
        return this.chatOrchestrator.streamChat(request.message(), request.conversationId());
    }

    /**
     * 聊天请求。
     *
     * @param message 用户自然语言
     * @param conversationId 会话ID（可空）
     */
    public record ChatRequest(String message, String conversationId) {
    }
}
