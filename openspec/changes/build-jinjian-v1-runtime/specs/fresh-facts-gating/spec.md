## ADDED Requirements

### Requirement: Ticker analysis SHALL require fresh facts
The system SHALL require recent factual market or filing data before presenting a current-action ticker analysis. If required fresh facts are unavailable, the system MUST not present the answer as a current actionable ticker judgment.

#### Scenario: Ticker analysis uses fresh facts
- **WHEN** a user asks for current analysis of a supported ticker
- **THEN** the runtime fetches fresh factual data before completing the answer

#### Scenario: Missing fresh facts blocks current-action framing
- **WHEN** required fresh ticker facts cannot be obtained
- **THEN** the runtime does not present the answer as a current-action recommendation

### Requirement: Missing fresh facts SHALL trigger explicit degradation
If required fresh facts are unavailable for a ticker question, the system SHALL degrade the answer to a "historical framework exercise" and MUST explicitly say that fresh facts were unavailable.

#### Scenario: Degraded ticker answer is labeled
- **WHEN** all required fresh-data attempts fail for a ticker question
- **THEN** the answer explicitly states that it is a historical-framework exercise
- **AND** the answer does not use current-action wording

### Requirement: Fresh facts SHALL carry provenance
The system SHALL record and expose the as-of time and source of fresh data used in ticker analysis.

#### Scenario: Fresh-data provenance is shown
- **WHEN** ticker facts are successfully retrieved
- **THEN** the answer includes the as-of time for those facts
- **AND** includes the source label for those facts

#### Scenario: Trace records provider chain outcome
- **WHEN** the runtime attempts one or more fresh-data providers
- **THEN** the trace records which provider succeeded or that all providers failed

### Requirement: Fresh-data retrieval SHALL support provider fallback
The system SHALL attempt fresh-data retrieval through a prioritized provider chain rather than relying on a single hard-coded provider.

#### Scenario: Secondary provider is used
- **WHEN** the preferred fresh-data source is unavailable
- **THEN** the runtime attempts the next provider in the fallback chain
- **AND** uses the first successful provider result
