package com.springailab.lab.domain.runtime.routing;

import com.springailab.lab.domain.runtime.model.RuntimeMode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Two-stage mode router: rules first, lightweight structured fallback second.
 */
@Component
public class ModeRouter {

    private static final Pattern TICKER_SYMBOL = Pattern.compile("\\b[A-Z]{1,5}\\b");

    public ModeDecision route(String rawInput) {
        String input = StringUtils.hasText(rawInput) ? rawInput.trim() : "";
        ModeDecision byRule = routeByRule(input);
        if (byRule != null) {
            return byRule;
        }
        return routeByStructuredFallback(input);
    }

    private ModeDecision routeByRule(String input) {
        String lowered = input.toLowerCase(Locale.ROOT);
        String ticker = extractTicker(input);

        if (containsAny(lowered, "portfolio", "asset allocation", "position sizing", "仓位", "资产配置", "组合")) {
            if (containsAny(lowered, "我的", "my", "我", "personal")) {
                return new ModeDecision(RuntimeMode.PERSONAL_PORTFOLIO, "rule", 0.98,
                        "matched personal portfolio keyword", ticker);
            }
            return new ModeDecision(RuntimeMode.PORTFOLIO, "rule", 0.95,
                    "matched portfolio keyword", ticker);
        }

        if (containsAny(lowered, "relationship", "career", "emotion", "恋爱", "婚姻", "职业规划", "人生建议")) {
            return new ModeDecision(RuntimeMode.REJECT_NON_INVESTING, "rule", 0.99,
                    "matched non-investing keyword", ticker);
        }

        if (ticker != null && containsAny(lowered, "ticker", "stock", "buy", "sell", "valuation", "估值", "买", "卖", "股票")) {
            return new ModeDecision(RuntimeMode.TICKER, "rule", 0.97,
                    "matched ticker keyword and symbol", ticker);
        }

        if (containsAny(lowered, "历史", "当年", "之前怎么看", "曾经", "historical", "in 202", "how did", "过去")) {
            return new ModeDecision(RuntimeMode.HISTORICAL_VIEW, "rule", 0.95,
                    "matched historical keyword", ticker);
        }

        return null;
    }

    private ModeDecision routeByStructuredFallback(String input) {
        String lowered = input.toLowerCase(Locale.ROOT);
        String ticker = extractTicker(input);

        Map<RuntimeMode, Integer> score = new HashMap<>();
        score.put(RuntimeMode.TICKER, weightedScore(lowered,
                "price", "valuation", "earnings", "forecast", "股价", "财报", "估值", "行情"));
        score.put(RuntimeMode.HISTORICAL_VIEW, weightedScore(lowered,
                "history", "historical", "timeline", "过去", "复盘", "当时", "怎么看"));
        score.put(RuntimeMode.PORTFOLIO, weightedScore(lowered,
                "allocation", "portfolio", "position", "仓位", "组合", "配比"));
        score.put(RuntimeMode.REJECT_NON_INVESTING, weightedScore(lowered,
                "career", "job", "relationship", "emotion", "学习", "感情", "婚姻"));

        RuntimeMode bestMode = RuntimeMode.UNKNOWN;
        int best = -1;
        for (Map.Entry<RuntimeMode, Integer> entry : score.entrySet()) {
            if (entry.getValue() > best) {
                best = entry.getValue();
                bestMode = entry.getKey();
            }
        }

        if (best <= 0) {
            bestMode = ticker == null ? RuntimeMode.HISTORICAL_VIEW : RuntimeMode.TICKER;
            best = 1;
        }

        double confidence = Math.min(0.9, 0.35 + (best * 0.12));
        String reason = "structured fallback score=" + best;

        if (bestMode == RuntimeMode.PORTFOLIO && containsAny(lowered, "my", "我的", "我")) {
            return new ModeDecision(RuntimeMode.PERSONAL_PORTFOLIO, "fallback", confidence, reason, ticker);
        }

        return new ModeDecision(bestMode, "fallback", confidence, reason, ticker);
    }

    private static int weightedScore(String lowered, String... keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (lowered.contains(keyword)) {
                score += 1;
            }
        }
        return score;
    }

    private static String extractTicker(String input) {
        Matcher matcher = TICKER_SYMBOL.matcher(input == null ? "" : input);
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() <= 1) {
                continue;
            }
            return token;
        }
        return null;
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
