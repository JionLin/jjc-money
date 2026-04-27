package com.springailab.lab.domain.runtime.routing;

import com.springailab.lab.domain.runtime.model.RuntimeMode;

public record ModeDecision(RuntimeMode mode,
                           String source,
                           double confidence,
                           String reason,
                           String extractedTicker) {
}
