package com.springailab.lab.web;

import com.springailab.lab.domain.runtime.config.JinjianRuntimeProperties;
import com.springailab.lab.domain.runtime.skill.SkillLoader;
import com.springailab.lab.domain.runtime.skill.SkillSnapshot;
import com.springailab.lab.domain.runtime.trace.RuntimeTrace;
import com.springailab.lab.domain.runtime.trace.RuntimeTraceStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Dev-only inspection hooks for runtime status and traces.
 */
@RestController
@RequestMapping("/dev/runtime")
public class RuntimeDebugController {

    private final JinjianRuntimeProperties runtimeProperties;

    private final SkillLoader skillLoader;

    private final RuntimeTraceStore traceStore;

    public RuntimeDebugController(JinjianRuntimeProperties runtimeProperties,
                                  SkillLoader skillLoader,
                                  RuntimeTraceStore traceStore) {
        this.runtimeProperties = runtimeProperties;
        this.skillLoader = skillLoader;
        this.traceStore = traceStore;
    }

    @GetMapping("/skill-state")
    public Map<String, Object> skillState() {
        ensureDebugEnabled();
        SkillSnapshot snapshot = this.skillLoader.getActiveSkill();
        return Map.of(
                "version", snapshot.version(),
                "path", snapshot.sourcePath().toString(),
                "lastModifiedEpochMillis", snapshot.lastModifiedEpochMillis());
    }

    @GetMapping("/traces")
    public List<RuntimeTrace> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        ensureDebugEnabled();
        return this.traceStore.recent(Math.max(1, Math.min(limit, 100)));
    }

    @GetMapping("/traces/{traceId}")
    public RuntimeTrace byId(@PathVariable String traceId) {
        ensureDebugEnabled();
        return this.traceStore.findById(traceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "trace not found"));
    }

    private void ensureDebugEnabled() {
        if (!this.runtimeProperties.isDebugEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "debug endpoint disabled");
        }
    }
}
