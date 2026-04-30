package com.springailab.lab.web;

import com.springailab.lab.domain.chat.service.ChatOrchestrator;
import com.springailab.lab.domain.runtime.home.HomeRecommendationsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

class ChatControllerTest {

    @Test
    void chatShouldRemainCompatible() {
        ChatOrchestrator orchestrator = Mockito.mock(ChatOrchestrator.class);
        HomeRecommendationsService recommendationsService = Mockito.mock(HomeRecommendationsService.class);
        Mockito.when(orchestrator.chat(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(Map.of("reply", "ok")));
        ChatController controller = new ChatController(orchestrator, recommendationsService);

        ResponseEntity<Map<String, String>> result = controller.chat(new ChatController.ChatRequest("hello", "c1"));

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).containsEntry("reply", "ok");
    }

    @Test
    void streamEndpointShouldDelegateToOrchestrator() {
        ChatOrchestrator orchestrator = Mockito.mock(ChatOrchestrator.class);
        HomeRecommendationsService recommendationsService = Mockito.mock(HomeRecommendationsService.class);
        SseEmitter emitter = new SseEmitter();
        Mockito.when(orchestrator.streamChat(anyString(), anyString()))
                .thenReturn(emitter);
        ChatController controller = new ChatController(orchestrator, recommendationsService);

        SseEmitter result = controller.streamChat(new ChatController.ChatRequest("hello", "c1"));

        assertThat(result).isSameAs(emitter);
    }

    @Test
    void recommendationsShouldComeFromHomepageService() {
        ChatOrchestrator orchestrator = Mockito.mock(ChatOrchestrator.class);
        HomeRecommendationsService recommendationsService = Mockito.mock(HomeRecommendationsService.class);
        HomeRecommendationsService.HomeRecommendationsResponse payload =
                new HomeRecommendationsService.HomeRecommendationsResponse(
                        "\u4eca\u5929\u60f3\u804a\u70b9\u4ec0\u4e48\uff1f",
                        "\u6b22\u8fce\u56de\u6765",
                        List.of(new HomeRecommendationsService.HomeRecommendationCard(
                                "valuation",
                                "\u4f30\u503c\u5224\u65ad",
                                "\u73b0\u5728 NVDA \u662f\u9ad8\u4f30\u8fd8\u662f\u4f4e\u4f30\uff1f",
                                "\u6309\u5b9e\u65f6\u6570\u636e\u62c6\u89e3")));
        Mockito.when(recommendationsService.getHomepageRecommendations()).thenReturn(payload);
        ChatController controller = new ChatController(orchestrator, recommendationsService);

        HomeRecommendationsService.HomeRecommendationsResponse result = controller.homepageRecommendations();

        assertThat(result.title()).isEqualTo(payload.title());
        assertThat(result.subtitle()).isEqualTo(payload.subtitle());
        assertThat(result.cards()).hasSize(1);
        assertThat(result.cards().get(0).prompt()).isEqualTo(payload.cards().get(0).prompt());
    }
}
