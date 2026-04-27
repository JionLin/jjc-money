package com.springailab.lab.domain.runtime.model;

import com.springailab.lab.domain.runtime.trace.RuntimeTrace;

import java.util.List;

public record RuntimeAnswer(String reply,
                            RuntimeMode mode,
                            List<CitationRecord> citations,
                            FreshFactRecord freshFact,
                            DegradeStatus degradeStatus,
                            String disclaimer,
                            RuntimeTrace trace,
                            List<RuntimeStreamEvent> streamEvents) {
}
