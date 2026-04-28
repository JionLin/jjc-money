package com.springailab.lab.domain.runtime.fresh;

import com.springailab.lab.domain.runtime.config.JinjianRuntimeProperties;
import com.springailab.lab.domain.runtime.model.FreshFactRecord;
import com.springailab.lab.domain.runtime.trace.RuntimeTrace;
import com.springailab.lab.domain.runtime.trace.ToolCallRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fresh ticker facts service with provider fallback and TTL-aware caching.
 */
@Service
public class FreshDataService {

    private static final Logger log = LoggerFactory.getLogger(FreshDataService.class);

    private static final String CACHE_KEY_PREFIX = "lab:runtime:fresh:";

    private final List<FreshDataProvider> providers;

    private final JinjianRuntimeProperties runtimeProperties;

    private final Map<String, CachedEnvelope> memoryCache = new ConcurrentHashMap<>();

    public FreshDataService(List<FreshDataProvider> providers,
                            JinjianRuntimeProperties runtimeProperties) {
        this.providers = providers;
        this.runtimeProperties = runtimeProperties;
    }

    public FreshDataResult fetchFreshFacts(String tickerRaw, RuntimeTrace trace) {
        String ticker = normalizeTicker(tickerRaw);
        if (!StringUtils.hasText(ticker)) {
            return new FreshDataResult(false, null, "ticker not provided", List.of());
        }

        FreshFactRecord fromCache = readCache(ticker);
        if (fromCache != null) {
            trace.addToolCall(new ToolCallRecord("FreshDataService", "cache_hit", "ticker=" + ticker));
            trace.addFreshFact(fromCache);
            return new FreshDataResult(true, fromCache, "", List.of("cache:" + fromCache.source()));
        }

        List<String> attempts = new ArrayList<>();
        for (FreshDataProvider provider : this.providers) {
            String providerName = provider.name();
            attempts.add(providerName + ":attempt");
            try {
                Optional<Map<String, Object>> payload = provider.fetch(ticker);
                if (payload.isEmpty()) {
                    attempts.add(providerName + ":miss");
                    continue;
                }
                FreshFactRecord record = buildRecordFromPayload(ticker, providerName, payload.get(), false);
                writeCache(record);
                trace.addToolCall(new ToolCallRecord("FreshDataProvider", "success", providerName));
                trace.addFreshFact(record);
                attempts.add(providerName + ":success");
                return new FreshDataResult(true, record, "", attempts);
            } catch (Exception ex) {
                log.warn("Fresh data provider failed: {}", providerName, ex);
                attempts.add(providerName + ":error");
                trace.addToolCall(new ToolCallRecord("FreshDataProvider", "error", providerName));
            }
        }

        trace.addToolCall(new ToolCallRecord("FreshDataService", "all_failed", "ticker=" + ticker));
        return new FreshDataResult(false, null,
                "required fresh facts unavailable after provider fallback", attempts);
    }

    private FreshFactRecord readCache(String ticker) {
        String key = CACHE_KEY_PREFIX + ticker;
        CachedEnvelope memory = this.memoryCache.get(key);
        if (memory == null || !memory.allRequiredFactsFresh(Instant.now())) {
            return null;
        }
        return buildRecordFromEnvelope(ticker, memory, true);
    }

    private void writeCache(FreshFactRecord record) {
        String key = CACHE_KEY_PREFIX + record.ticker();
        Instant now = Instant.now();
        CachedEnvelope envelope = CachedEnvelope.fromRecord(record, now,
                this.runtimeProperties.getFreshPriceTtlSeconds(),
                this.runtimeProperties.getFreshValuationTtlSeconds(),
                this.runtimeProperties.getFreshFilingsTtlSeconds(),
                this.runtimeProperties.getFreshMacroTtlSeconds());
        this.memoryCache.put(key, envelope);
    }

    private static String normalizeTicker(String tickerRaw) {
        if (!StringUtils.hasText(tickerRaw)) {
            return "";
        }
        return tickerRaw.trim().toUpperCase();
    }

    private static FreshFactRecord buildRecordFromPayload(String ticker,
                                                          String source,
                                                          Map<String, Object> payload,
                                                          boolean fromCache) {
        Map<String, Object> facts = new HashMap<>();
        facts.put("price", payload.getOrDefault("price", "N/A"));
        facts.put("valuation", payload.getOrDefault("valuation_pe", "N/A"));
        facts.put("filings", payload.getOrDefault("filings", "N/A"));
        facts.put("macro", payload.getOrDefault("macro", "N/A"));
        return new FreshFactRecord(ticker, source, Instant.now(), facts, fromCache);
    }

    private static FreshFactRecord buildRecordFromEnvelope(String ticker, CachedEnvelope envelope, boolean fromCache) {
        return new FreshFactRecord(ticker, envelope.source(), envelope.asOf(), envelope.facts(), fromCache);
    }

    private record CachedEnvelope(Instant asOf,
                                  String source,
                                  Map<String, Object> facts,
                                  Map<String, Instant> expiresAt) {

        static CachedEnvelope fromRecord(FreshFactRecord record,
                                         Instant now,
                                         long priceTtl,
                                         long valuationTtl,
                                         long filingsTtl,
                                         long macroTtl) {
            Map<String, Instant> expires = new HashMap<>();
            expires.put("price", now.plusSeconds(Math.max(priceTtl, 1)));
            expires.put("valuation", now.plusSeconds(Math.max(valuationTtl, 1)));
            expires.put("filings", now.plusSeconds(Math.max(filingsTtl, 1)));
            expires.put("macro", now.plusSeconds(Math.max(macroTtl, 1)));
            return new CachedEnvelope(record.asOf(), record.source(), record.facts(), expires);
        }

        boolean allRequiredFactsFresh(Instant now) {
            if (facts == null || expiresAt == null) {
                return false;
            }
            return isFresh(now, "price")
                    && isFresh(now, "valuation")
                    && isFresh(now, "filings")
                    && isFresh(now, "macro");
        }

        private boolean isFresh(Instant now, String key) {
            Instant expiry = expiresAt.get(key);
            Object value = facts.get(key);
            return expiry != null && expiry.isAfter(now) && value != null;
        }
    }
}
