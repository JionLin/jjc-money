package com.springailab.lab.domain.runtime.model;

import java.time.Instant;
import java.util.Map;

public record FreshFactRecord(String ticker,
                              String source,
                              Instant asOf,
                              Map<String, Object> facts,
                              boolean fromCache) {
}
