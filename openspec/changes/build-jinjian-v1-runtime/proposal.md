## Why

The repository already contains the core assets for a "Jinjian" investing assistant: raw archive corpus, a mature `jinjian-perspective` Skill, and a working `spring-ai-lab` chat backend. What is missing is a reliable runtime that can execute the Skill contract end to end: retrieve verified historical views, fetch fresh facts for current ticker questions, and clearly separate history from present-day evidence.

This change is needed now because the project has reached the point where architecture exploration is no longer the bottleneck. The next useful step is to turn the current demos and documents into a narrow, implementation-ready v1 runtime that can answer real `Historical View` and `Ticker` questions with traceable evidence and controlled degradation.

## What Changes

- Build a v1 runtime on top of `spring-ai-lab` that executes `jinjian-perspective` as a runtime contract rather than a loose prompt.
- Add structured mode routing so the system explicitly supports only `Historical View` and `Ticker` in v1, while degrading or rejecting out-of-scope requests.
- Add archive evidence retrieval tools that search raw source files under `22-25year/` and `26year/`, verify matched sections, and treat `docs/indexes/` as optional navigation rather than mandatory evidence.
- Add fresh-facts gating for ticker analysis so current-action answers require recent market or filing data and degrade to "historical framework exercise" when fresh facts are unavailable.
- Extend the streaming response contract so the UI can expose mode, citations, fresh-data status, and final answer sections in a debuggable way.
- Upgrade the current demo chat page into a v1-oriented UI that exposes layered answers, source references, and debug information when enabled.

## Capabilities

### New Capabilities
- `jinjian-chat-runtime`: Execute the v1 chat workflow for supported investing question types, including scope enforcement, mode routing, SOP execution, and structured streaming output.
- `archive-evidence-retrieval`: Retrieve and verify historical evidence from raw monthly archive files, with optional use of archive-level overview files only for navigation.
- `fresh-facts-gating`: Fetch and validate fresh ticker facts for current-analysis questions and degrade safely when required real-time data is unavailable.

### Modified Capabilities

None.

## Impact

- Affected code: `spring-ai-lab` chat orchestration, tool layer, external data access, streaming output, and static frontend.
- Affected systems: local archive retrieval workflow, Skill loading and runtime execution, Redis-backed session/caching/trace support.
- Affected docs: v1 architecture references and possibly Skill / index guidance to align retrieval expectations with the new `overview + raw source` default.
