package com.springailab.lab.domain.runtime.fresh;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Placeholder fallback provider that intentionally fails to preserve explicit degradation paths.
 */
@Component
@Order(2)
public class FallbackNullFreshDataProvider implements FreshDataProvider {

    @Override
    public String name() {
        return "fallback-null-provider";
    }

    @Override
    public Optional<Map<String, Object>> fetch(String ticker) {
        return Optional.empty();
    }
}
