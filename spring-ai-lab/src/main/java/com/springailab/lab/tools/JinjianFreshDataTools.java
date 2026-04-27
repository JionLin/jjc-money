package com.springailab.lab.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springailab.lab.domain.runtime.fresh.FreshDataResult;
import com.springailab.lab.domain.runtime.fresh.FreshDataService;
import com.springailab.lab.domain.runtime.trace.RuntimeTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Fresh facts tool wrapper for Spring AI tool-calling.
 */
@Component
public class JinjianFreshDataTools {

    private static final Logger log = LoggerFactory.getLogger(JinjianFreshDataTools.class);

    private final FreshDataService freshDataService;

    private final ObjectMapper objectMapper;

    public JinjianFreshDataTools(FreshDataService freshDataService, ObjectMapper objectMapper) {
        this.freshDataService = freshDataService;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Fetch fresh ticker facts with fallback providers and provenance")
    public String fetchFreshTickerFacts(@ToolParam(description = "Ticker symbol, e.g. NVDA") String ticker) {
        log.info("Tool fetchFreshTickerFacts invoked, ticker={}", ticker);
        RuntimeTrace tmp = new RuntimeTrace("tool-" + Instant.now().toEpochMilli(), "tool-call", Instant.now());
        FreshDataResult result = this.freshDataService.fetchFreshFacts(ticker, tmp);
        if (!result.success()) {
            return toJson(Map.of(
                    "success", false,
                    "degradeReason", result.degradeReason(),
                    "providerAttempts", result.providerAttempts()));
        }
        return toJson(Map.of(
                "success", true,
                "ticker", result.factRecord().ticker(),
                "source", result.factRecord().source(),
                "asOf", result.factRecord().asOf(),
                "facts", result.factRecord().facts(),
                "providerAttempts", result.providerAttempts()));
    }

    private String toJson(Object value) {
        try {
            return this.objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
