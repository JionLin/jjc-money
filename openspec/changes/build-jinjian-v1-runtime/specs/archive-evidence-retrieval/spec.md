## ADDED Requirements

### Requirement: Archive retrieval SHALL use raw source files as the final authority
The system SHALL treat `22-25year/` and `26year/` files as the final authority for historical evidence. Any claim presented as the author's historical view MUST be verified against a matched raw source section before it is returned to the user.

#### Scenario: Historical claim is verified against raw source
- **WHEN** the system finds a candidate historical claim during archive retrieval
- **THEN** it opens the matched raw source section
- **AND** it verifies the claim against that raw section before using it in the final answer

### Requirement: Archive overview SHALL be optional navigation only
The system MAY use `docs/indexes/archive-index.md` to narrow likely months, but it MUST NOT require monthly article index files as a mandatory step in the retrieval path.

#### Scenario: Runtime uses overview and raw search
- **WHEN** archive overview data is available
- **THEN** the system may use it to narrow candidate months
- **AND** it still searches and verifies against raw source files

#### Scenario: Runtime skips monthly indexes
- **WHEN** monthly article index files are missing, stale, or unused
- **THEN** the system still retrieves evidence successfully by searching raw source files directly

### Requirement: Archive retrieval SHALL return citation-ready evidence
The system SHALL return retrieval results with enough metadata for citation and traceability, including file path, anchor or locator, excerpt, and context type when known.

#### Scenario: Retrieved evidence includes citation metadata
- **WHEN** archive retrieval returns a matched section
- **THEN** the result includes the source file path
- **AND** includes a section anchor or locator
- **AND** includes a short excerpt suitable for trace or UI display

#### Scenario: Context type is distinguished when available
- **WHEN** the runtime can distinguish between正文、评论区、or 作者回复
- **THEN** the citation metadata includes that context type

### Requirement: Derived docs SHALL be secondary evidence aids
The system MAY consult topology or deep-analysis documents to enrich understanding, but it MUST NOT present them as the final authority for historical-view claims.

#### Scenario: Derived docs enrich but do not replace source verification
- **WHEN** the system uses a topology or deep-analysis document during retrieval
- **THEN** the final answer still cites verified raw archive sources for historical claims

