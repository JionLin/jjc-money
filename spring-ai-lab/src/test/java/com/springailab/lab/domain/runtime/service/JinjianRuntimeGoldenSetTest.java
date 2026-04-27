package com.springailab.lab.domain.runtime.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springailab.lab.domain.runtime.archive.ArchiveEvidenceService;
import com.springailab.lab.domain.runtime.config.JinjianRuntimeProperties;
import com.springailab.lab.domain.runtime.fresh.FallbackNullFreshDataProvider;
import com.springailab.lab.domain.runtime.fresh.FreshDataProvider;
import com.springailab.lab.domain.runtime.fresh.FreshDataService;
import com.springailab.lab.domain.runtime.fresh.StaticFreshDataProvider;
import com.springailab.lab.domain.runtime.model.RuntimeAnswer;
import com.springailab.lab.domain.runtime.model.RuntimeMode;
import com.springailab.lab.domain.runtime.routing.ModeRouter;
import com.springailab.lab.domain.runtime.skill.SkillLoader;
import com.springailab.lab.domain.runtime.trace.RuntimeTraceStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JinjianRuntimeGoldenSetTest {

    private JinjianRuntimeService runtimeService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();

        JinjianRuntimeProperties properties = new JinjianRuntimeProperties();
        Path skillPath = resolveFromCwdOrParent(Path.of(".agent", "skills", "jinjian-perspective", "SKILL.md"));
        properties.setSkillPath(skillPath.toString());
        properties.setMinimumSkillVersion("2.0");
        properties.setArchiveRoots(List.of(
                resolveFromCwdOrParent(Path.of("22-25year")).toString(),
                resolveFromCwdOrParent(Path.of("26year")).toString()));
        properties.setArchiveOverviewPath(resolveFromCwdOrParent(Path.of("docs", "indexes", "archive-index.md"))
                .toString());
        properties.setDerivedDocRoots(List.of(resolveFromCwdOrParent(Path.of("docs")).toString()));

        SkillLoader skillLoader = new SkillLoader(properties);
        skillLoader.initialize();

        ArchiveEvidenceService archiveEvidenceService = new ArchiveEvidenceService(properties);

        List<FreshDataProvider> providers = List.of(new StaticFreshDataProvider(), new FallbackNullFreshDataProvider());
        FreshDataService freshDataService = new FreshDataService(providers, properties, this.objectMapper, Optional.empty());

        this.runtimeService = new JinjianRuntimeService(
                skillLoader,
                new ModeRouter(),
                archiveEvidenceService,
                freshDataService,
                new RuntimeTraceStore());
    }

    @Test
    void goldenSetShouldMatchExpectedModeAndPolicy() throws Exception {
        List<GoldenCase> cases = this.objectMapper.readValue(
                getClass().getResourceAsStream("/golden-set-v1.json"),
                new TypeReference<List<GoldenCase>>() {
                });

        assertThat(cases).isNotEmpty();

        for (GoldenCase c : cases) {
            RuntimeAnswer answer = this.runtimeService.execute("golden-session", c.message());
            assertThat(answer.mode()).as("mode for case " + c.id())
                    .isEqualTo(RuntimeMode.valueOf(c.expectedMode()));
            assertThat(answer.degradeStatus().name()).as("degrade status for case " + c.id())
                    .isEqualTo(c.expectedDegradeStatus());
            assertThat(answer.trace().getMode().name()).isEqualTo(c.expectedMode());
        }
    }

    private record GoldenCase(String id, String message, String expectedMode, String expectedDegradeStatus) {
    }

    private static Path resolveFromCwdOrParent(Path relativePath) {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        Path direct = cwd.resolve(relativePath).normalize();
        if (Files.exists(direct)) {
            return direct;
        }
        Path parent = cwd.getParent();
        if (parent != null) {
            Path fromParent = parent.resolve(relativePath).normalize();
            if (Files.exists(fromParent)) {
                return fromParent;
            }
        }
        return direct;
    }
}
