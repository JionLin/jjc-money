package com.springailab.lab.domain.runtime.archive;

import com.springailab.lab.domain.runtime.config.JinjianRuntimeProperties;
import com.springailab.lab.domain.runtime.model.CitationContextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Local archive evidence retrieval using raw monthly files as the final authority.
 */
@Service
public class ArchiveEvidenceService {

    private static final Logger log = LoggerFactory.getLogger(ArchiveEvidenceService.class);

    private static final Pattern MONTH_PATTERN = Pattern.compile("(20\\d{2}-\\d{2})");

    private final JinjianRuntimeProperties runtimeProperties;

    public ArchiveEvidenceService(JinjianRuntimeProperties runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }

    public List<ArchiveEvidence> searchLocalArchive(String ticker, String keywords, int limit) {
        List<String> terms = collectTerms(ticker, keywords);
        if (terms.isEmpty()) {
            return List.of();
        }

        Set<String> monthHints = this.runtimeProperties.isOverviewNarrowingEnabled()
                ? readMonthHintsFromOverview(terms)
                : Set.of();

        List<Path> files = collectRawArchiveFiles(monthHints);
        List<ArchiveEvidence> matches = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Path file : files) {
            try {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (!containsAny(line, terms)) {
                        continue;
                    }
                    Section section = locateSection(lines, i);
                    String locator = StringUtils.hasText(section.heading())
                            ? section.heading()
                            : "line:" + (i + 1);
                    String key = file + "#" + locator;
                    if (!seen.add(key)) {
                        continue;
                    }
                    String excerpt = buildExcerpt(line);
                    CitationContextType contextType = detectContextType(lines, section.start(), i, section.end());
                    matches.add(new ArchiveEvidence(file.toString(), locator, excerpt, contextType, section.text(), false));
                    if (matches.size() >= limit) {
                        return matches;
                    }
                }
            } catch (IOException ex) {
                log.warn("Skip archive file due to read failure: {}", file, ex);
            }
        }
        return matches;
    }

    public String readArchiveSection(String sourceFilePath, String locator) {
        Path sourceFile = Paths.get(sourceFilePath).normalize();
        try {
            List<String> lines = Files.readAllLines(sourceFile, StandardCharsets.UTF_8);
            if (locator != null && locator.startsWith("line:")) {
                int line = Integer.parseInt(locator.substring("line:".length()).trim()) - 1;
                if (line >= 0 && line < lines.size()) {
                    return locateSection(lines, line).text();
                }
            }
            if (StringUtils.hasText(locator)) {
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).trim().equals(locator.trim())) {
                        return locateSection(lines, i).text();
                    }
                }
            }
            return String.join("\n", lines);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read archive section from " + sourceFilePath, ex);
        }
    }

    public List<ArchiveEvidence> searchDerivedDocs(String keywords, int limit) {
        List<String> terms = collectTerms(null, keywords);
        if (terms.isEmpty()) {
            return List.of();
        }

        List<ArchiveEvidence> matches = new ArrayList<>();
        for (String root : this.runtimeProperties.getDerivedDocRoots()) {
            Path rootPath = resolvePath(root);
            if (!Files.exists(rootPath)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(rootPath)) {
                List<Path> docs = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".md"))
                        .toList();
                for (Path doc : docs) {
                    List<String> lines;
                    try {
                        lines = Files.readAllLines(doc, StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        log.warn("Skip derived doc due to read failure: {}", doc, ex);
                        continue;
                    }
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        if (!containsAny(line, terms)) {
                            continue;
                        }
                        Section section = locateSection(lines, i);
                        matches.add(new ArchiveEvidence(
                                doc.toString(),
                                StringUtils.hasText(section.heading()) ? section.heading() : "line:" + (i + 1),
                                buildExcerpt(line),
                                CitationContextType.DERIVED_DOC,
                                section.text(),
                                true));
                        if (matches.size() >= limit) {
                            return matches;
                        }
                        break;
                    }
                }
            } catch (IOException ex) {
                log.warn("Skip derived doc root due to walk failure: {}", rootPath, ex);
            }
        }
        return matches;
    }

    private List<Path> collectRawArchiveFiles(Set<String> monthHints) {
        List<Path> files = new ArrayList<>();
        for (String root : this.runtimeProperties.getArchiveRoots()) {
            Path rootPath = resolvePath(root);
            if (!Files.exists(rootPath)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(rootPath)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".md"))
                        .filter(path -> matchesMonthHint(path, monthHints))
                        .forEach(files::add);
            } catch (IOException ex) {
                log.warn("Skip archive root due to walk failure: {}", rootPath, ex);
            }
        }
        return files;
    }

    private Set<String> readMonthHintsFromOverview(List<String> terms) {
        Path overview = resolvePath(this.runtimeProperties.getArchiveOverviewPath());
        if (!Files.exists(overview)) {
            return Set.of();
        }
        Set<String> hints = new LinkedHashSet<>();
        try {
            List<String> lines = Files.readAllLines(overview, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (!containsAny(line, terms)) {
                    continue;
                }
                Matcher matcher = MONTH_PATTERN.matcher(line);
                while (matcher.find()) {
                    hints.add(matcher.group(1));
                }
            }
        } catch (IOException ex) {
            log.warn("Cannot read archive overview file: {}", overview, ex);
        }
        return hints;
    }

    private static boolean matchesMonthHint(Path file, Set<String> hints) {
        if (hints == null || hints.isEmpty()) {
            return true;
        }
        String lower = file.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String hint : hints) {
            if (lower.contains(hint.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static Section locateSection(List<String> lines, int hitIndex) {
        int start = hitIndex;
        for (int i = hitIndex; i >= 0; i--) {
            if (isHeading(lines.get(i))) {
                start = i;
                break;
            }
        }

        int end = lines.size();
        for (int i = start + 1; i < lines.size(); i++) {
            if (isHeading(lines.get(i))) {
                end = i;
                break;
            }
        }

        String heading = isHeading(lines.get(start)) ? lines.get(start).trim() : "";
        String text = String.join("\n", lines.subList(start, end));
        return new Section(start, end, heading, text);
    }

    private static CitationContextType detectContextType(List<String> lines, int sectionStart, int hitIndex, int sectionEnd) {
        int from = Math.max(sectionStart, hitIndex - 6);
        int to = Math.min(sectionEnd, hitIndex + 6);
        for (int i = from; i < to; i++) {
            String line = lines.get(i);
            if (line.contains("评论区") || line.contains("评论")) {
                return CitationContextType.COMMENT;
            }
            if (line.contains("作者回复") || line.contains("回复")) {
                return CitationContextType.AUTHOR_REPLY;
            }
        }
        return CitationContextType.BODY;
    }

    private static String buildExcerpt(String line) {
        if (!StringUtils.hasText(line)) {
            return "";
        }
        String compact = line.trim().replaceAll("\\s+", " ");
        return compact.length() > 180 ? compact.substring(0, 180) + "..." : compact;
    }

    private static boolean isHeading(String line) {
        if (!StringUtils.hasText(line)) {
            return false;
        }
        String trimmed = line.trim();
        return trimmed.startsWith("## ") || trimmed.startsWith("# ");
    }

    private static boolean containsAny(String line, List<String> terms) {
        if (!StringUtils.hasText(line)) {
            return false;
        }
        String lowered = line.toLowerCase(Locale.ROOT);
        for (String term : terms) {
            if (lowered.contains(term.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static List<String> collectTerms(String ticker, String keywords) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (StringUtils.hasText(ticker)) {
            values.add(ticker.trim());
        }
        if (StringUtils.hasText(keywords)) {
            String[] parts = keywords.split("[,，\\s]+");
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    values.add(part.trim());
                }
            }
        }
        return new ArrayList<>(values);
    }

    private static Path resolvePath(String configuredPath) {
        Path path = Paths.get(configuredPath).normalize();
        if (path.isAbsolute()) {
            return path;
        }
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path direct = cwd.resolve(path).normalize();
        if (Files.exists(direct)) {
            return direct;
        }
        Path parent = cwd.getParent();
        if (parent != null) {
            Path parentResolved = parent.resolve(path).normalize();
            if (Files.exists(parentResolved)) {
                return parentResolved;
            }
        }
        return direct;
    }

    private record Section(int start, int end, String heading, String text) {
    }
}
