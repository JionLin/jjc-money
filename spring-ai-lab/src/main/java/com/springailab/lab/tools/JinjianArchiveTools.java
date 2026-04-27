package com.springailab.lab.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springailab.lab.domain.runtime.archive.ArchiveEvidence;
import com.springailab.lab.domain.runtime.archive.ArchiveEvidenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Archive retrieval tools for Spring AI tool-calling.
 */
@Component
public class JinjianArchiveTools {

    private static final Logger log = LoggerFactory.getLogger(JinjianArchiveTools.class);

    private final ArchiveEvidenceService archiveEvidenceService;

    private final ObjectMapper objectMapper;

    public JinjianArchiveTools(ArchiveEvidenceService archiveEvidenceService, ObjectMapper objectMapper) {
        this.archiveEvidenceService = archiveEvidenceService;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Search local raw archive files for ticker or anchor keywords and return citation-ready evidence")
    public String searchLocalArchive(
            @ToolParam(description = "Ticker symbol, e.g. NVDA") String ticker,
            @ToolParam(description = "Anchor keywords, comma-separated") String keywords,
            @ToolParam(description = "Max result count, default 5") Integer limit) {
        int size = limit == null || limit <= 0 ? 5 : Math.min(limit, 10);
        log.info("Tool searchLocalArchive invoked, ticker={}, keywords={}, limit={}", ticker, keywords, size);
        List<ArchiveEvidence> matches = this.archiveEvidenceService.searchLocalArchive(ticker, keywords, size);
        return toJson(matches);
    }

    @Tool(description = "Read and verify the full archive section by file path and locator")
    public String readArchiveSection(
            @ToolParam(description = "Source markdown file path") String filePath,
            @ToolParam(description = "Locator heading or line:number") String locator) {
        log.info("Tool readArchiveSection invoked, filePath={}, locator={}", filePath, locator);
        String text = this.archiveEvidenceService.readArchiveSection(filePath, locator);
        return text.length() > 1800 ? text.substring(0, 1800) + "\n...(truncated)" : text;
    }

    @Tool(description = "Search topology/deep-analysis docs as secondary evidence aids only")
    public String searchDerivedDocs(
            @ToolParam(description = "Keywords, comma-separated") String keywords,
            @ToolParam(description = "Max result count, default 3") Integer limit) {
        int size = limit == null || limit <= 0 ? 3 : Math.min(limit, 10);
        log.info("Tool searchDerivedDocs invoked, keywords={}, limit={}", keywords, size);
        List<ArchiveEvidence> matches = this.archiveEvidenceService.searchDerivedDocs(keywords, size);
        return toJson(matches);
    }

    private String toJson(Object value) {
        try {
            return this.objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }
}
