## 1. Runtime Foundation

- [x] 1.1 Add a `SkillLoader` component in `spring-ai-lab` that loads `.agent/skills/jinjian-perspective/SKILL.md`, caches the current content, checks `lastModified()` before each request, and logs the active Skill version.
- [x] 1.2 Add minimum-compatible Skill version validation and a clear startup/runtime failure path when the Skill cannot be loaded safely.
- [x] 1.3 Define runtime request metadata and trace fields for mode, skill version, cited sources, fresh-data status, cost, latency, and degrade status.

## 2. Mode Routing and Scope Control

- [x] 2.1 Implement a `ModeRouter` that first applies rule-based routing for clear `Ticker` and `Historical View` questions.
- [x] 2.2 Add a lightweight structured fallback classification path for inputs that do not match rule-based routing.
- [x] 2.3 Implement explicit downgrade/rejection behavior for `Portfolio`, `Personal Portfolio`, and non-investing requests in line with the v1 scope.

## 3. Archive Evidence Retrieval

- [x] 3.1 Implement `searchLocalArchive` to search raw files under `22-25year/` and `26year/` with ticker and anchor keywords.
- [x] 3.2 Implement `readArchiveSection` so the runtime can open and verify the full matched section before citing a historical claim.
- [x] 3.3 Add citation metadata extraction for file path, anchor/locator, excerpt, and context type when the runtime can distinguish正文、评论区、or 作者回复.
- [x] 3.4 Add optional archive-overview narrowing using `docs/indexes/archive-index.md` without requiring monthly article index files.
- [x] 3.5 Implement `searchDerivedDocs` for topology and deep-analysis documents as secondary evidence aids only.

## 4. Fresh Facts Gating

- [x] 4.1 Implement a `FreshDataService` abstraction that fetches ticker facts through a prioritized provider chain.
- [x] 4.2 Add provider fallback behavior so the runtime can try the next source when the preferred source fails.
- [x] 4.3 Add TTL-based caching for prices, valuation metrics, filings, and macro context using existing Redis support where appropriate.
- [x] 4.4 Implement explicit degradation to "historical framework exercise" when required fresh ticker facts are unavailable.
- [x] 4.5 Ensure every successful ticker fact payload carries source and as-of metadata into traces and final answers.

## 5. Chat Orchestration and Streaming

- [x] 5.1 Extend `ChatOrchestrator` to execute the v1 flow as a Skill runtime rather than a generic chat pass-through.
- [x] 5.2 Register the new archive and fresh-data tools using the existing Spring AI tool-calling pattern established by `WeatherTools`.
- [x] 5.3 Implement the `Ticker` answer path so it combines verified historical evidence, fresh facts, and the required structured answer sections.
- [x] 5.4 Implement the `Historical View` answer path so it cites verified raw archive evidence and avoids current-action framing.
- [x] 5.5 Add logical stream events for trace start, mode detected, tool call, source cited, fresh data, answer chunks, disclaimer, and trace end while preserving the current POST streaming interface.

## 6. Frontend and UX

- [x] 6.1 Replace the current demo page with a v1 chat UI that can submit streaming POST requests and render incremental output.
- [x] 6.2 Add visible mode labels, citation cards, fresh-data timestamps, and disclaimer rendering in the chat UI.
- [x] 6.3 Add a collapsible debug panel for tool calls, degrade status, trace metadata, and cost/latency when debug mode is enabled.

## 7. Observability, Validation, and Rollout

- [x] 7.1 Extend request logging and trace capture so each answer records route choice, tool calls, cited sources, fresh-data provenance, latency, and degradation.
- [x] 7.2 Add dev-only debug endpoints or equivalent inspection hooks for current Skill state and trace lookup.
- [x] 7.3 Build the v1 Golden Set for `Ticker`, `Historical View`, downgrade, and rejection cases.
- [x] 7.4 Validate the runtime against the Golden Set and confirm that unsupported requests degrade or reject as specified.
