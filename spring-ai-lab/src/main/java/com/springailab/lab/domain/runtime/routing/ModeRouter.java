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
 * 模式路由器。
 *
 * 这个类可以理解成整条 AI 链路的“分诊台”。
 * 用户问题一进来，系统先在这里判断它属于哪一类问题，
 * 再决定后面应该走 Ticker、Historical View、Portfolio 还是拒绝分支。
 *
 * 当前采用两段式策略：
 * 1. 先走明确规则
 * 2. 规则不够时，再走轻量关键词打分兜底
 */
@Component
public class ModeRouter {

    private static final Pattern TICKER_SYMBOL = Pattern.compile("\\b[A-Z]{1,5}\\b");

    /**
     * 路由总入口。
     * 先做基础清洗，再优先走规则路由，最后用兜底路由补足。
     */
    public ModeDecision route(String rawInput) {
        String input = StringUtils.hasText(rawInput) ? rawInput.trim() : "";
        ModeDecision byRule = routeByRule(input);
        if (byRule != null) {
            return byRule;
        }
        return routeByStructuredFallback(input);
    }

    /**
     * 第一阶段：规则路由。
     * 只要命中某些高确定性的关键词，就直接给出模式判断。
     */
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

    /**
     * 第二阶段：结构化兜底路由。
     * 如果规则没有明显命中，就通过关键词计分选出最可能的模式。
     */
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

    /**
     * 计算某个模式的关键词得分。
     * 当前规则很简单：命中一个关键词加 1 分。
     */
    private static int weightedScore(String lowered, String... keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (lowered.contains(keyword)) {
                score += 1;
            }
        }
        return score;
    }

    /**
     * 从输入文本里粗略提取 ticker。
     * 这里只是轻量匹配像 NVDA、TSM 这样的连续大写字母。
     */
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

    /**
     * 判断字符串里是否包含任意一个关键词。
     */
    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
