package com.springailab.lab.web;

import com.springailab.lab.domain.chat.service.ChatOrchestrator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * {@link ChatController} HTTP 契约测试。
 *
 * @author jiaolin
 */
class ChatControllerTest {

    @Test
    void chatShouldRemainCompatible() {
        ChatOrchestrator orchestrator = Mockito.mock(ChatOrchestrator.class);
        Mockito.when(orchestrator.chat(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(Map.of("reply", "ok")));
        ChatController controller = new ChatController(orchestrator);

        ResponseEntity<Map<String, String>> result = controller.chat(new ChatController.ChatRequest("hello", "c1"));

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).containsEntry("reply", "ok");
    }

    @Test
    void streamEndpointShouldDelegateToOrchestrator() {
        ChatOrchestrator orchestrator = Mockito.mock(ChatOrchestrator.class);
        SseEmitter emitter = new SseEmitter();
        Mockito.when(orchestrator.streamChat(anyString(), anyString()))
                .thenReturn(emitter);
        ChatController controller = new ChatController(orchestrator);

        SseEmitter result = controller.streamChat(new ChatController.ChatRequest("hello", "c1"));

        assertThat(result).isSameAs(emitter);
    }
}
