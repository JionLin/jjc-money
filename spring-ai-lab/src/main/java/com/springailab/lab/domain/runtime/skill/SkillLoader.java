package com.springailab.lab.domain.runtime.skill;

import com.springailab.lab.domain.runtime.config.JinjianRuntimeProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads jinjian skill content and refreshes by file mtime.
 */
@Component
public class SkillLoader {

    private static final Logger log = LoggerFactory.getLogger(SkillLoader.class);

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?m)^\\s*version\\s*:\\s*\\\"?([0-9A-Za-z._-]+)\\\"?");

    private final JinjianRuntimeProperties runtimeProperties;

    private final Object lock = new Object();

    private volatile SkillSnapshot currentSnapshot;

    public SkillLoader(JinjianRuntimeProperties runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }

    @PostConstruct
    public void initialize() {
        try {
            this.currentSnapshot = loadSnapshot();
            log.info("Active jinjian skill loaded, version={}, path={}",
                    this.currentSnapshot.version(), this.currentSnapshot.sourcePath());
        } catch (SkillLoadException ex) {
            if (this.runtimeProperties.isFailFastOnSkillError()) {
                throw ex;
            }
            log.error("Skill load failed at startup, runtime will retry per request: {}", ex.getMessage(), ex);
        }
    }

    public SkillSnapshot getActiveSkill() {
        SkillSnapshot snapshot = this.currentSnapshot;
        if (snapshot == null) {
            synchronized (this.lock) {
                if (this.currentSnapshot == null) {
                    this.currentSnapshot = loadSnapshot();
                }
                return this.currentSnapshot;
            }
        }
        try {
            long currentMtime = Files.getLastModifiedTime(snapshot.sourcePath()).toMillis();
            if (currentMtime > snapshot.lastModifiedEpochMillis()) {
                synchronized (this.lock) {
                    SkillSnapshot latest = this.currentSnapshot;
                    if (latest == null || Files.getLastModifiedTime(latest.sourcePath()).toMillis() > latest.lastModifiedEpochMillis()) {
                        this.currentSnapshot = loadSnapshot();
                        log.info("Jinjian skill reloaded, version={}, path={}",
                                this.currentSnapshot.version(), this.currentSnapshot.sourcePath());
                    }
                }
            }
        } catch (IOException ex) {
            throw new SkillLoadException("Unable to check active skill mtime", ex);
        }
        return this.currentSnapshot;
    }

    private SkillSnapshot loadSnapshot() {
        Path path = resolveSkillPath();
        if (!Files.exists(path)) {
            throw new SkillLoadException("Skill file not found: " + path);
        }
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            String version = parseSkillVersion(content);
            ensureVersionCompatible(version, this.runtimeProperties.getMinimumSkillVersion());
            long mtime = Files.getLastModifiedTime(path).toMillis();
            return new SkillSnapshot(version, content, path, mtime);
        } catch (IOException ex) {
            throw new SkillLoadException("Failed to load skill content from " + path, ex);
        }
    }

    private Path resolveSkillPath() {
        String configured = this.runtimeProperties.getSkillPath();
        if (!StringUtils.hasText(configured)) {
            throw new SkillLoadException("lab.runtime.skill-path must not be empty");
        }
        Path configuredPath = Paths.get(configured).normalize();
        List<Path> candidates = new ArrayList<>();
        if (configuredPath.isAbsolute()) {
            candidates.add(configuredPath);
        } else {
            Path cwd = Paths.get("").toAbsolutePath().normalize();
            candidates.add(cwd.resolve(configuredPath).normalize());
            candidates.add(cwd.getParent() == null ? cwd.resolve(configuredPath).normalize()
                    : cwd.getParent().resolve(configuredPath).normalize());
        }
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    private static String parseSkillVersion(String content) {
        Matcher matcher = VERSION_PATTERN.matcher(content == null ? "" : content);
        if (!matcher.find()) {
            throw new SkillLoadException("Skill metadata version is missing");
        }
        return matcher.group(1).trim();
    }

    private static void ensureVersionCompatible(String actual, String minimum) {
        if (!StringUtils.hasText(minimum)) {
            return;
        }
        if (compareVersion(actual, minimum) < 0) {
            throw new SkillLoadException("Skill version " + actual + " is below minimum compatible version " + minimum);
        }
    }

    private static int compareVersion(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int max = Math.max(leftParts.length, rightParts.length);
        for (int i = 0; i < max; i++) {
            String lp = i < leftParts.length ? leftParts[i] : "0";
            String rp = i < rightParts.length ? rightParts[i] : "0";
            Integer ln = parseIntOrNull(lp);
            Integer rn = parseIntOrNull(rp);
            int result;
            if (ln != null && rn != null) {
                result = ln.compareTo(rn);
            } else {
                result = lp.compareTo(rp);
            }
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
