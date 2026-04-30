package com.springailab.lab.domain.runtime.home;

import com.springailab.lab.domain.runtime.skill.SkillLoader;
import com.springailab.lab.domain.runtime.skill.SkillSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HomeRecommendationsServiceTest {

    @Test
    void shouldBuildCardsFromRecognizedSkillThemes() {
        SkillLoader skillLoader = Mockito.mock(SkillLoader.class);
        Mockito.when(skillLoader.getActiveSkill()).thenReturn(new SkillSnapshot(
                "2.4",
                """
                ## Historical View
                ## Portfolio
                ## Life Decision
                PEG and Forward PE matter.
                2-3-3-2 position sizing matters.
                """,
                Path.of("skill.md"),
                1L));
        HomeRecommendationsService service = new HomeRecommendationsService(skillLoader);

        HomeRecommendationsService.HomeRecommendationsResponse response = service.getHomepageRecommendations();

        assertThat(response.title()).isEqualTo("\u4eca\u5929\u60f3\u804a\u70b9\u4ec0\u4e48\uff1f");
        assertThat(response.cards()).hasSize(4);
        assertThat(response.cards()).extracting(HomeRecommendationsService.HomeRecommendationCard::id)
                .contains("valuation", "historical-view", "position-sizing", "asset-allocation");
    }

    @Test
    void shouldFillMissingThemesWithFallbackCards() {
        SkillLoader skillLoader = Mockito.mock(SkillLoader.class);
        Mockito.when(skillLoader.getActiveSkill()).thenReturn(new SkillSnapshot(
                "2.4",
                "Only valuation and PEG are described here.",
                Path.of("skill.md"),
                1L));
        HomeRecommendationsService service = new HomeRecommendationsService(skillLoader);

        HomeRecommendationsService.HomeRecommendationsResponse response = service.getHomepageRecommendations();

        assertThat(response.cards()).hasSize(4);
        assertThat(response.cards().get(0).id()).isEqualTo("valuation");
        assertThat(response.cards()).extracting(HomeRecommendationsService.HomeRecommendationCard::id)
                .contains("historical-view");
    }

    @Test
    void shouldReturnFallbackPayloadWhenSkillLoadingFails() {
        SkillLoader skillLoader = Mockito.mock(SkillLoader.class);
        Mockito.when(skillLoader.getActiveSkill()).thenThrow(new IllegalStateException("boom"));
        HomeRecommendationsService service = new HomeRecommendationsService(skillLoader);

        HomeRecommendationsService.HomeRecommendationsResponse response = service.getHomepageRecommendations();

        assertThat(response.subtitle()).contains("\u63a8\u8350\u95ee\u9898");
        assertThat(response.cards()).hasSize(4);
        assertThat(response.cards().get(0).title()).isEqualTo("\u4f30\u503c\u5224\u65ad");
    }
}
