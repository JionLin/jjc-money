package com.springailab.lab.domain.runtime.model;

public record CitationRecord(String filePath,
                             String locator,
                             String excerpt,
                             CitationContextType contextType) {
}
