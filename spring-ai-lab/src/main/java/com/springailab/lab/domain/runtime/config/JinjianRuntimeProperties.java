package com.springailab.lab.domain.runtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Jinjian v1 runtime configuration.
 */
@Component
@ConfigurationProperties(prefix = "lab.runtime")
public class JinjianRuntimeProperties {

    private String skillPath = ".agent/skills/jinjian-perspective/SKILL.md";

    private String minimumSkillVersion = "2.0";

    private boolean failFastOnSkillError = true;

    private boolean overviewNarrowingEnabled = true;

    private boolean debugEnabled = true;

    private List<String> archiveRoots = new ArrayList<>(List.of("22-25year", "26year"));

    private String archiveOverviewPath = "docs/indexes/archive-index.md";

    private List<String> derivedDocRoots = new ArrayList<>(List.of("docs/topology-details", "docs/deep-analysis"));

    private long freshPriceTtlSeconds = 180;

    private long freshValuationTtlSeconds = 600;

    private long freshFilingsTtlSeconds = 1800;

    private long freshMacroTtlSeconds = 1800;

    public String getSkillPath() {
        return skillPath;
    }

    public void setSkillPath(String skillPath) {
        this.skillPath = skillPath;
    }

    public String getMinimumSkillVersion() {
        return minimumSkillVersion;
    }

    public void setMinimumSkillVersion(String minimumSkillVersion) {
        this.minimumSkillVersion = minimumSkillVersion;
    }

    public boolean isFailFastOnSkillError() {
        return failFastOnSkillError;
    }

    public void setFailFastOnSkillError(boolean failFastOnSkillError) {
        this.failFastOnSkillError = failFastOnSkillError;
    }

    public boolean isOverviewNarrowingEnabled() {
        return overviewNarrowingEnabled;
    }

    public void setOverviewNarrowingEnabled(boolean overviewNarrowingEnabled) {
        this.overviewNarrowingEnabled = overviewNarrowingEnabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public List<String> getArchiveRoots() {
        return archiveRoots;
    }

    public void setArchiveRoots(List<String> archiveRoots) {
        this.archiveRoots = archiveRoots;
    }

    public String getArchiveOverviewPath() {
        return archiveOverviewPath;
    }

    public void setArchiveOverviewPath(String archiveOverviewPath) {
        this.archiveOverviewPath = archiveOverviewPath;
    }

    public List<String> getDerivedDocRoots() {
        return derivedDocRoots;
    }

    public void setDerivedDocRoots(List<String> derivedDocRoots) {
        this.derivedDocRoots = derivedDocRoots;
    }

    public long getFreshPriceTtlSeconds() {
        return freshPriceTtlSeconds;
    }

    public void setFreshPriceTtlSeconds(long freshPriceTtlSeconds) {
        this.freshPriceTtlSeconds = freshPriceTtlSeconds;
    }

    public long getFreshValuationTtlSeconds() {
        return freshValuationTtlSeconds;
    }

    public void setFreshValuationTtlSeconds(long freshValuationTtlSeconds) {
        this.freshValuationTtlSeconds = freshValuationTtlSeconds;
    }

    public long getFreshFilingsTtlSeconds() {
        return freshFilingsTtlSeconds;
    }

    public void setFreshFilingsTtlSeconds(long freshFilingsTtlSeconds) {
        this.freshFilingsTtlSeconds = freshFilingsTtlSeconds;
    }

    public long getFreshMacroTtlSeconds() {
        return freshMacroTtlSeconds;
    }

    public void setFreshMacroTtlSeconds(long freshMacroTtlSeconds) {
        this.freshMacroTtlSeconds = freshMacroTtlSeconds;
    }
}
