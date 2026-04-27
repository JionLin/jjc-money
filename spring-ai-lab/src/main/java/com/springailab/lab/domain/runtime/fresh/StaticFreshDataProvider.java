package com.springailab.lab.domain.runtime.fresh;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Demo provider with curated ticker facts.
 */
@Component
@Order(1)
public class StaticFreshDataProvider implements FreshDataProvider {

    private static final Map<String, Map<String, Object>> DATA = Map.of(
            "NVDA", Map.of(
                    "price", 121.4,
                    "valuation_pe", 62.1,
                    "filings", "Latest 10-Q margin expansion and datacenter growth",
                    "macro", "AI capex cycle remains elevated"),
            "TSM", Map.of(
                    "price", 153.2,
                    "valuation_pe", 27.6,
                    "filings", "Capacity utilization improved in advanced node mix",
                    "macro", "Foundry demand supported by AI and HPC"),
            "AAPL", Map.of(
                    "price", 188.7,
                    "valuation_pe", 30.3,
                    "filings", "Services margin steady with resilient buyback pace",
                    "macro", "Consumer hardware replacement cycle mixed"));

    @Override
    public String name() {
        return "static-provider";
    }

    @Override
    public Optional<Map<String, Object>> fetch(String ticker) {
        return Optional.ofNullable(DATA.get(ticker));
    }
}
