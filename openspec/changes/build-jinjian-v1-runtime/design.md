## Context

The repository already has three critical assets that make a v1 implementation feasible without a greenfield rewrite:

- A large local archive corpus under `22-25year/` and `26year/` that acts as the ground-truth source for the author's historical view
- A mature `jinjian-perspective` Skill that already defines evidence hierarchy, freshness gating, output contracts, and execution SOPs
- A working `spring-ai-lab` backend that already supports chat orchestration, streaming responses, Redis-backed conversation storage, external HTTP access, and Spring AI tool calling

The gap is not conceptual anymore. The system currently lacks a narrow, reliable runtime that can execute the Skill contract for real investing questions. The v1 scope intentionally stays small: only `Historical View` and `Ticker` questions are supported. Portfolio planning, life-decision questions, vector retrieval, and multi-skill orchestration stay out of scope.

Constraints:

- The raw archive files remain the final authority for historical evidence
- Current-action ticker answers require fresh facts; otherwise the system must degrade explicitly
- The existing `POST /chat/stream` interface should be preserved to avoid unnecessary protocol churn
- The design should reuse `spring-ai-lab` components rather than introduce a parallel backend

Stakeholders:

- The user, who needs trustworthy historical-view and current-ticker answers
- Future implementers, who need a design that maps cleanly onto existing code

## Goals / Non-Goals

**Goals:**

- Turn `jinjian-perspective` into an explicit runtime contract in the backend
- Support a stable v1 flow for `Historical View` and `Ticker`
- Retrieve historical evidence from raw archive sources, with optional use of archive overview files only for navigation
- Require fresh ticker facts for current-analysis answers and degrade safely when missing
- Make mode routing, source citations, degradation, cost, and latency traceable
- Preserve the existing backend deployment shape and minimize unnecessary rewrites

**Non-Goals:**

- Supporting all Skill modes in v1
- Adding vector retrieval, MCP-only dependencies, or automatic index rebuilding as required infrastructure
- Providing personalized portfolio prescriptions
- Solving non-investing question categories
- Replacing `spring-ai-lab` with a new service

## Decisions

### Decision 1: Reuse `spring-ai-lab` as the v1 execution base

V1 will extend `ChatOrchestrator`, `ChatController`, and the existing tool / external client patterns rather than create a separate service.

Why:

- The current backend already provides streaming, Redis session support, and tool-calling infrastructure
- Reuse keeps implementation cost low and aligns with the existing repository reality
- A second backend would add migration and operational complexity before the runtime value is proven

Alternatives considered:

- Build a separate dedicated service for Jinjian runtime
  - Rejected because it adds premature service boundaries and duplicates existing chat infrastructure

### Decision 2: Treat the Skill as a runtime contract, not a prompt blob

The backend will load the Skill content explicitly and enforce its execution boundaries through mode routing, freshness gating, evidence verification, and answer structure.

Why:

- The Skill already contains operational rules, not just tone guidance
- Treating it as a plain prompt would make routing, degradation, and evidence handling too implicit
- A contract view keeps architecture aligned with the current asset that already works for manual use

Alternatives considered:

- Embed the Skill as a static system prompt and let the model decide everything ad hoc
  - Rejected because it makes behavior less traceable and weakens enforcement of scope and degradation rules

### Decision 3: Use `overview + raw source` as the default archive retrieval flow

`archive-index.md` may be used as an optional month-level narrowing step, but monthly article indexes are not part of the required runtime path.

Why:

- The raw archive files are the actual authority
- Monthly indexes add maintenance cost and can drift stale
- The current corpus size still allows direct source search with targeted keywords
- This aligns with the Skill's evidence hierarchy while reducing operational dependency on article-level indexes

Alternatives considered:

- Require `archive-index.md -> monthly/YYYY-MM.md -> raw source` for every retrieval
  - Rejected because it adds a brittle intermediate layer without being the source of truth

### Decision 4: Restrict v1 to two supported modes with explicit downgrade paths

The system will only fully support `Historical View` and `Ticker`. `Portfolio` and `Personal Portfolio` requests will degrade to general ticker-oriented guidance; non-investing requests will be rejected.

Why:

- This is the smallest useful closed loop
- It keeps implementation consistent with the user's current architecture goals
- It reduces risk of overpromising before the core runtime is stable

Alternatives considered:

- Support all Skill modes immediately
  - Rejected because it would enlarge the implementation surface too early

### Decision 5: Use two-stage mode routing

The backend will first apply rule-based routing for clear cases, then use a lightweight structured classification step when needed. Route determination will complete before the answer stream begins.

Why:

- This is more robust than parsing a `mode:` tag out of the answer stream
- It avoids coupling transport protocol with natural-language output
- It allows explicit trace logging of routing source and confidence

Alternatives considered:

- Have the main answer stream emit a routing label first
  - Rejected because it creates a fragile protocol embedded in generated text

### Decision 6: Keep the current POST streaming contract and layer logical events on top

The runtime will continue using the current `POST /chat/stream` style and consume the response with `fetch`/stream readers in the frontend. Logical event types will still be defined for UI rendering and debug traces.

Why:

- Native `EventSource` only supports GET and would force avoidable API redesign
- The current backend already supports a compatible streaming shape
- Logical events are still useful even if transport stays as streamed POST response

Alternatives considered:

- Redesign the endpoint to GET + `EventSource`
  - Rejected because it adds protocol churn without helping core v1 goals

### Decision 7: Implement fresh-facts gating as a service with provider fallbacks

Ticker answers will fetch current data through a `FreshDataService` abstraction. Structured providers, IR/filings, web search, and user-provided data form a descending reliability chain. If required fresh facts are unavailable, the answer must degrade to a historical-framework exercise.

Why:

- Freshness gating is a first-class requirement in the Skill
- Provider abstraction avoids hard-coding the runtime to a single data source
- It preserves flexibility as the environment changes

Alternatives considered:

- Hard-code a single provider or MCP as the only source
  - Rejected because it creates unnecessary operational coupling

### Decision 8: Use mtime-based Skill reload rather than file watchers

The service will load the Skill at startup and re-check file modification time before each request, optionally forcing no-cache behavior in development.

Why:

- It is simple, observable, and sufficient for the current single-node use case
- It avoids the complexity and edge cases of watcher-based reload

Alternatives considered:

- Add filesystem watcher support
  - Rejected for v1 as unnecessary complexity

## Risks / Trade-offs

- **[Risk] Route classification still mislabels ambiguous questions** → Mitigation: keep the rule-based path narrow, trace route source/confidence, and use a small Golden Set to tune the fallback classifier.
- **[Risk] Fresh data providers fail or drift** → Mitigation: provider fallback chain, explicit degradation to historical-framework mode, and trace logging of missing facts.
- **[Risk] Raw archive search returns noisy evidence** → Mitigation: require verification against opened sections and include citation metadata such as file path and context type.
- **[Risk] Skill behavior changes faster than backend assumptions** → Mitigation: parse and log Skill version, enforce minimum compatible version, and keep runtime scope narrow.
- **[Risk] Monthly index de-emphasis creates inconsistency with existing docs/Skill wording** → Mitigation: treat indexes as optional navigation in runtime design and align related docs during implementation.
- **[Risk] The v1 scope rejects or downgrades some user questions that the Skill can conceptually answer** → Mitigation: document the limitation clearly and defer broader mode support to later phases.

## Migration Plan

1. Add runtime components incrementally inside `spring-ai-lab` without replacing the current chat entrypoints.
2. Introduce Skill loading, route classification, archive retrieval tools, and fresh-data services behind the existing orchestration layer.
3. Extend streaming output with logical event markers while preserving the current POST streaming contract.
4. Upgrade the static frontend to render layered answers, source citations, and debug metadata.
5. Validate behavior against a Golden Set before treating the runtime as the default path.

Rollback strategy:

- Keep the current chat path available behind the existing orchestration layer until the new runtime is stable
- If the Skill execution path fails, fall back to the previous generic chat behavior or disable the new path by configuration

## Open Questions

- Which structured fresh-data provider will be the preferred Priority 1 source in the target environment?
- Should archive overview usage remain enabled in production by default, or only as a debug / admin convenience?
- How much of the logical streaming event structure should be surfaced to normal users versus hidden behind a debug switch?
- Should the first v1 release keep the old demo page available as a fallback route during rollout?
