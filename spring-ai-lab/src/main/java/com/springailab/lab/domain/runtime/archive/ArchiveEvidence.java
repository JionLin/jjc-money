package com.springailab.lab.domain.runtime.archive;

import com.springailab.lab.domain.runtime.model.CitationContextType;

public record ArchiveEvidence(String filePath,
                              String locator,
                              String excerpt,
                              CitationContextType contextType,
                              String sectionText,
                              boolean derivedDoc) {
}
