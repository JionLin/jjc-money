package com.springailab.lab.domain.runtime.trace;

import com.springailab.lab.domain.runtime.model.CitationRecord;
import com.springailab.lab.domain.runtime.model.DegradeStatus;
import com.springailab.lab.domain.runtime.model.FreshFactRecord;
import com.springailab.lab.domain.runtime.model.RuntimeMode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuntimeTrace {

    private final String traceId;

    private final String conversationId;

    private final Instant startedAt;

    private RuntimeMode mode = RuntimeMode.UNKNOWN;

    private String modeSource = "unknown";

    private double modeConfidence;

    private String skillVersion;

    private final List<CitationRecord> citations = new ArrayList<>();

    private final List<FreshFactRecord> freshFacts = new ArrayList<>();

    private final List<ToolCallRecord> toolCalls = new ArrayList<>();

    private DegradeStatus degradeStatus = DegradeStatus.NONE;

    private String degradeReason = "";

    private double estimatedCost;

    private long latencyMillis;

    private Instant finishedAt;

    public RuntimeTrace(String traceId, String conversationId, Instant startedAt) {
        this.traceId = traceId;
        this.conversationId = conversationId;
        this.startedAt = startedAt;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public RuntimeMode getMode() {
        return mode;
    }

    public void setMode(RuntimeMode mode) {
        this.mode = mode;
    }

    public String getModeSource() {
        return modeSource;
    }

    public void setModeSource(String modeSource) {
        this.modeSource = modeSource;
    }

    public double getModeConfidence() {
        return modeConfidence;
    }

    public void setModeConfidence(double modeConfidence) {
        this.modeConfidence = modeConfidence;
    }

    public String getSkillVersion() {
        return skillVersion;
    }

    public void setSkillVersion(String skillVersion) {
        this.skillVersion = skillVersion;
    }

    public List<CitationRecord> getCitations() {
        return Collections.unmodifiableList(citations);
    }

    public void addCitation(CitationRecord citation) {
        this.citations.add(citation);
    }

    public List<FreshFactRecord> getFreshFacts() {
        return Collections.unmodifiableList(freshFacts);
    }

    public void addFreshFact(FreshFactRecord record) {
        this.freshFacts.add(record);
    }

    public List<ToolCallRecord> getToolCalls() {
        return Collections.unmodifiableList(toolCalls);
    }

    public void addToolCall(ToolCallRecord record) {
        this.toolCalls.add(record);
    }

    public DegradeStatus getDegradeStatus() {
        return degradeStatus;
    }

    public void setDegradeStatus(DegradeStatus degradeStatus) {
        this.degradeStatus = degradeStatus;
    }

    public String getDegradeReason() {
        return degradeReason;
    }

    public void setDegradeReason(String degradeReason) {
        this.degradeReason = degradeReason;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public long getLatencyMillis() {
        return latencyMillis;
    }

    public void setLatencyMillis(long latencyMillis) {
        this.latencyMillis = latencyMillis;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void finish(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}
