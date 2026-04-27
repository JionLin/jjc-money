package com.springailab.lab.domain.runtime.skill;

import java.nio.file.Path;

public record SkillSnapshot(String version, String content, Path sourcePath, long lastModifiedEpochMillis) {
}
