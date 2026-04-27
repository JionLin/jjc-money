package com.springailab.lab.domain.runtime.trace;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RuntimeTraceStore {

    private static final int MAX_TRACES = 300;

    private final Map<String, RuntimeTrace> traces = new ConcurrentHashMap<>();

    public void put(RuntimeTrace trace) {
        this.traces.put(trace.getTraceId(), trace);
        trimIfNeeded();
    }

    public Optional<RuntimeTrace> findById(String traceId) {
        return Optional.ofNullable(this.traces.get(traceId));
    }

    public List<RuntimeTrace> recent(int limit) {
        return this.traces.values().stream()
                .sorted(Comparator.comparing(RuntimeTrace::getStartedAt).reversed())
                .limit(Math.max(1, limit))
                .toList();
    }

    private void trimIfNeeded() {
        if (this.traces.size() <= MAX_TRACES) {
            return;
        }
        List<RuntimeTrace> ordered = new ArrayList<>(this.traces.values());
        ordered.sort(Comparator.comparing(RuntimeTrace::getStartedAt).reversed());
        ordered.stream().skip(MAX_TRACES).forEach(trace -> this.traces.remove(trace.getTraceId()));
    }
}
