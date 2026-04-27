## ADDED Requirements

### Requirement: V1 runtime SHALL support only the approved question modes
The system SHALL fully support only `Ticker` and `Historical View` in v1. The system MUST downgrade `Portfolio` and `Personal Portfolio` requests to general non-personalized guidance, and MUST reject non-investing requests outside the v1 scope.

#### Scenario: Ticker request is accepted
- **WHEN** a user asks for current analysis of a supported ticker
- **THEN** the system classifies the request as `Ticker`
- **AND** executes the v1 ticker workflow

#### Scenario: Historical request is accepted
- **WHEN** a user asks how the author previously viewed or handled a market event or ticker
- **THEN** the system classifies the request as `Historical View`
- **AND** executes the historical-view workflow

#### Scenario: Portfolio request is downgraded
- **WHEN** a user asks for personal portfolio allocation or personal position sizing
- **THEN** the system does not provide personalized portfolio prescriptions
- **AND** the answer explicitly states that v1 does not support personal portfolio analysis

#### Scenario: Non-investing request is rejected
- **WHEN** a user asks a non-investing question such as life, career, or relationship advice
- **THEN** the system refuses the request
- **AND** the answer explicitly states that v1 only supports investing-related questions

### Requirement: V1 runtime SHALL execute the Skill as a runtime contract
The system SHALL load the active `jinjian-perspective` Skill content and MUST make the loaded Skill version traceable for each request. The runtime MUST enforce route selection, answer structure, and degradation rules in alignment with the active Skill contract.

#### Scenario: Active Skill version is recorded
- **WHEN** the service starts or reloads the Skill
- **THEN** the runtime records the active Skill version in logs

#### Scenario: Request trace includes Skill version
- **WHEN** the system answers a user request
- **THEN** the request trace includes the Skill version used for that answer

### Requirement: V1 runtime SHALL return structured streamed answers
The system SHALL stream responses in a way that allows the frontend to render route information, source citations, fresh-data status, answer content, and final completion metadata.

#### Scenario: Stream includes route and answer content
- **WHEN** the system begins responding to a supported request
- **THEN** the stream includes a mode-detected signal before answer content is rendered
- **AND** the stream includes the answer content in incremental chunks

#### Scenario: Stream includes completion metadata
- **WHEN** the system finishes responding
- **THEN** the stream includes completion metadata such as trace completion and final status

### Requirement: V1 runtime SHALL make execution traceable
The system SHALL emit traceable execution data for each request, including route outcome, tool calls, cited sources, fresh-data state, latency, and degradation status.

#### Scenario: Successful request trace is complete
- **WHEN** a supported request completes successfully
- **THEN** the trace includes mode, tool calls, cited sources, and latency metadata

#### Scenario: Degraded request trace records fallback
- **WHEN** the runtime degrades due to missing required facts or failed tool calls
- **THEN** the trace records that a degradation path was used

