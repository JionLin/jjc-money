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
 * Chat HTTP endpoints backed by Jinjian v1 runtime.
 */
@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatOrchestrator chatOrchestrator;

    public ChatController(ChatOrchestrator chatOrchestrator) {
        this.chatOrchestrator = chatOrchestrator;
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        log.info("Chat request received, messageLength={}, request={}",
                request.message() == null ? 0 : request.message().length(), request);
        return this.chatOrchestrator.chat(request.message(), request.conversationId());
    }

    @PostMapping(value = "/chat/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        log.info("Stream chat request received, messageLength={}, conversationId={}",
                request.message() == null ? 0 : request.message().length(), request.conversationId());
        return this.chatOrchestrator.streamChat(request.message(), request.conversationId());
    }

    public record ChatRequest(String message, String conversationId) {
    }
}
