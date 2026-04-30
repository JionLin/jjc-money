package com.springailab.lab.domain.runtime.home;

import com.springailab.lab.domain.runtime.skill.SkillLoader;
import com.springailab.lab.domain.runtime.skill.SkillSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Builds homepage recommendation cards from the active runtime skill.
 */
@Service
public class HomeRecommendationsService {

    private static final Logger log = LoggerFactory.getLogger(HomeRecommendationsService.class);

    private static final int MAX_TITLE_LENGTH = 12;
    private static final int MAX_PROMPT_LENGTH = 34;
    private static final int MAX_DESCRIPTION_LENGTH = 24;
    private static final int CARD_LIMIT = 4;

    private static final String HOME_TITLE = "\u4eca\u5929\u60f3\u804a\u70b9\u4ec0\u4e48\uff1f";
    private static final String HOME_SUBTITLE =
            "\u53ef\u4ee5\u95ee\u4f30\u503c\u3001\u5386\u53f2\u89c2\u70b9\u3001\u4ed3\u4f4d\u914d\u7f6e\uff0c\u6216\u76f4\u63a5\u8f93\u5165\u80a1\u7968\u4ee3\u7801\u3002";
    private static final String FALLBACK_SUBTITLE =
            "\u4ece\u4e0b\u9762\u7684\u63a8\u8350\u95ee\u9898\u5f00\u59cb\uff0c\u4e5f\u53ef\u4ee5\u76f4\u63a5\u8f93\u5165\u4f60\u7684\u95ee\u9898\u3002";

    private final SkillLoader skillLoader;

    public HomeRecommendationsService(SkillLoader skillLoader) {
        this.skillLoader = skillLoader;
    }

    public HomeRecommendationsResponse getHomepageRecommendations() {
        try {
            SkillSnapshot snapshot = this.skillLoader.getActiveSkill();
            List<HomeRecommendationCard> cards = buildDynamicCards(snapshot.content());
            if (cards.isEmpty()) {
                return fallbackResponse();
            }
            return new HomeRecommendationsResponse(HOME_TITLE, HOME_SUBTITLE, cards);
        } catch (RuntimeException ex) {
            log.warn("Failed to build homepage recommendations from active skill: {}", ex.getMessage());
            return fallbackResponse();
        }
    }

    private List<HomeRecommendationCard> buildDynamicCards(String skillContent) {
        String normalized = skillContent == null ? "" : skillContent.toLowerCase(Locale.ROOT);
        Set<String> selectedIds = new LinkedHashSet<>();
        List<HomeRecommendationCard> cards = new ArrayList<>();

        for (CardTemplate template : CARD_TEMPLATES) {
            if (template.matches(normalized) && selectedIds.add(template.id())) {
                cards.add(template.toCard());
            }
            if (cards.size() >= CARD_LIMIT) {
                return cards;
            }
        }

        for (CardTemplate template : CARD_TEMPLATES) {
            if (selectedIds.add(template.id())) {
                cards.add(template.toCard());
            }
            if (cards.size() >= CARD_LIMIT) {
                break;
            }
        }
        return cards;
    }

    private HomeRecommendationsResponse fallbackResponse() {
        List<HomeRecommendationCard> cards = CARD_TEMPLATES.stream()
                .limit(CARD_LIMIT)
                .map(CardTemplate::toCard)
                .toList();
        return new HomeRecommendationsResponse(HOME_TITLE, FALLBACK_SUBTITLE, cards);
    }

    private static String clamp(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }

    private static final class CardTemplate {

        private final String id;
        private final String title;
        private final String prompt;
        private final String description;
        private final List<String> keywords;

        private CardTemplate(String id, String title, String prompt, String description, List<String> keywords) {
            this.id = id;
            this.title = title;
            this.prompt = prompt;
            this.description = description;
            this.keywords = keywords;
        }

        String id() {
            return this.id;
        }

        boolean matches(String normalizedSkill) {
            for (String keyword : this.keywords) {
                if (normalizedSkill.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
            return false;
        }

        HomeRecommendationCard toCard() {
            return new HomeRecommendationCard(
                    this.id,
                    clamp(this.title, MAX_TITLE_LENGTH),
                    clamp(this.prompt, MAX_PROMPT_LENGTH),
                    clamp(this.description, MAX_DESCRIPTION_LENGTH));
        }
    }

    public static final class HomeRecommendationsResponse {

        private final String title;
        private final String subtitle;
        private final List<HomeRecommendationCard> cards;

        public HomeRecommendationsResponse(String title, String subtitle, List<HomeRecommendationCard> cards) {
            this.title = title;
            this.subtitle = subtitle;
            this.cards = cards;
        }

        public String title() {
            return this.title;
        }

        public String subtitle() {
            return this.subtitle;
        }

        public List<HomeRecommendationCard> cards() {
            return this.cards;
        }
    }

    public static final class HomeRecommendationCard {

        private final String id;
        private final String title;
        private final String prompt;
        private final String description;

        public HomeRecommendationCard(String id, String title, String prompt, String description) {
            this.id = id;
            this.title = title;
            this.prompt = prompt;
            this.description = description;
        }

        public String id() {
            return this.id;
        }

        public String title() {
            return this.title;
        }

        public String prompt() {
            return this.prompt;
        }

        public String description() {
            return this.description;
        }
    }

    private static final List<CardTemplate> CARD_TEMPLATES = List.of(
            new CardTemplate(
                    "valuation",
                    "\u4f30\u503c\u5224\u65ad",
                    "\u73b0\u5728 NVDA \u662f\u9ad8\u4f30\u8fd8\u662f\u4f4e\u4f30\uff1f",
                    "\u6309\u5b9e\u65f6\u6570\u636e\u548c\u4f30\u503c\u6846\u67b6\u62c6\u89e3\u3002",
                    List.of("valuation", "forward pe", "peg", "ticker analysis")),
            new CardTemplate(
                    "historical-view",
                    "\u5386\u53f2\u89c2\u70b9",
                    "\u4f5c\u8005\u5728 2025 \u5e74\u662f\u600e\u4e48\u5224\u65ad\u5fae\u8f6f\u7684\uff1f",
                    "\u56de\u5230\u5386\u53f2\u6587\u7ae0\u91cc\u627e\u539f\u59cb\u89c2\u70b9\u3002",
                    List.of("historical view", "archive retrieval", "raw source")),
            new CardTemplate(
                    "position-sizing",
                    "\u4ed3\u4f4d\u8282\u594f",
                    "\u73b0\u5728\u9002\u5408\u5206\u6279\u52a0\u4ed3\uff0c\u8fd8\u662f\u5148\u63a7\u5236\u56de\u64a4\uff1f",
                    "\u7528 2-3-3-2 \u7684\u8282\u594f\u6765\u5224\u65ad\u3002",
                    List.of("2-3-3-2", "position sizing", "buy-point", "sell-point")),
            new CardTemplate(
                    "asset-allocation",
                    "\u8d44\u4ea7\u914d\u7f6e",
                    "\u6211\u7684\u4ed3\u4f4d\u8be5\u600e\u4e48\u4ece\u8fdb\u653b\u8f6c\u5411\u9632\u5b88\uff1f",
                    "\u56f4\u7ed5\u8fdb\u653b\u3001\u7a33\u5065\u3001\u9632\u5b88\u6765\u91cd\u6392\u3002",
                    List.of("portfolio", "asset-allocation", "wealth cascade")),
            new CardTemplate(
                    "life-decision",
                    "\u4eba\u751f\u51b3\u7b56",
                    "\u7528\u91d1\u6e10\u6210\u89c6\u89d2\u770b\u4e00\u4e2a\u804c\u4e1a\u9009\u62e9\u95ee\u9898\u3002",
                    "\u9002\u5408\u804a\u8ba4\u77e5\u3001\u804c\u4e1a\u548c\u957f\u671f\u9009\u62e9\u3002",
                    List.of("life decision", "career", "cognition"))
    );
}
