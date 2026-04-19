# Source Reference and 26year Index Workflow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a repo-local markdown reference for official and aggregate market-data sources, record whether each source is actually reachable in the current environment, verify whether `26year/` NVDA content is indexed today, and document exactly which index files must be updated when new `26year/` markdown files are added.

**Architecture:** This work stays inside the existing archive workflow: inspect the current `26year/` source files, compare them against `docs/indexes/`, then write one focused reference document that future archive maintenance can follow. No MCP server, vector store, or index automation is added in this plan.

**Tech Stack:** Markdown content files, existing archive indexes, ripgrep-based verification, Claude Code file editing tools

---

## File map

- Create: `docs/source-and-index-reference.md` — canonical note for public official sources, aggregate fallback sources, source reachability status in the current environment, current `26year/` NVDA index status, and future `26year/` index update targets
- Modify: `docs/indexes/archive-index.md` — only if verification shows `26year/` month entries are missing or stale
- Modify: `docs/indexes/monthly/2026-01.md` — create or update when `26year/26-01.md` needs month-level indexing
- Modify: `docs/indexes/monthly/2026-02.md` — create or update when `26year/26-02月.md` needs month-level indexing
- Modify: `docs/indexes/monthly/2026-03.md` — create or update when `26year/2026-03.md` needs month-level indexing

### Task 1: Verify current 26year NVDA coverage

**Files:**
- Modify: `docs/source-and-index-reference.md`
- Check: `26year/26-01.md`
- Check: `26year/26-02月.md`
- Check: `26year/2026-03.md`
- Check: `docs/indexes/archive-index.md`
- Check: `docs/indexes/monthly/*.md`

- [ ] **Step 1: Search `26year/` for NVDA-related mentions**

Run:
```bash
rg -n "英伟达|NVDA|Nvidia|英伟達" /Users/johnny/Documents/jjc-money/26year/*.md
```
Expected: matches in one or more `26year/` files, proving source content exists.

- [ ] **Step 2: Search existing indexes for the same topic**

Run:
```bash
rg -n "英伟达|NVDA|Nvidia|英偉達" /Users/johnny/Documents/jjc-money/docs/indexes
```
Expected: either no `2026-*` month index hits or only older `2025-*` hits, showing whether 26year content is already indexed.

- [ ] **Step 3: Record the verification result in the reference doc**

Add a section like this to `docs/source-and-index-reference.md`:

```md
## Current 26year NVDA index coverage

Verification date: 2026-04-19

- Source files checked: `26year/26-01.md`, `26year/26-02月.md`, `26year/2026-03.md`
- Query used: `英伟达|NVDA|Nvidia|英偉達`
- Result in source files: matches found
- Result in `docs/indexes/`: no 2026 month index entries found yet for these matches
- Conclusion: `26year/` NVDA-related content exists in source files but is not yet represented in the current index set.
```

- [ ] **Step 4: Re-read the doc section to verify wording matches command output**

Run:
```bash
rg -n "Current 26year NVDA index coverage|Conclusion" /Users/johnny/Documents/jjc-money/docs/source-and-index-reference.md
```
Expected: the section exists and states the observed status without guessing.

- [ ] **Step 5: Commit the verification note**

```bash
git add /Users/johnny/Documents/jjc-money/docs/source-and-index-reference.md
git commit -m "docs: record 26year NVDA index coverage"
```

### Task 2: Write public source reference guidance

**Files:**
- Modify: `docs/source-and-index-reference.md`

- [ ] **Step 1: Write the official-source section**

Add this content to `docs/source-and-index-reference.md`:

```md
## Official source reference

Use public, primary-source pages for factual earnings information.

### US mega-cap equities (Magnificent 7)

- Apple Investor Relations
- Microsoft Investor Relations
- Alphabet Investor Relations
- Amazon Investor Relations
- Meta Investor Relations
- NVIDIA Investor Relations
- Tesla Investor Relations
- SEC EDGAR company filings

Use these for:
- earnings releases
- shareholder letters
- Form 10-K
- Form 10-Q
- Form 8-K
- investor presentations

### TSMC

- TSMC Investor Relations
- TSMC annual / quarterly reports
- TWSE / company filing pages when needed

Use these for:
- quarterly revenue
- gross margin
- capex guidance
- management commentary
```

- [ ] **Step 2: Check and record official-source reachability in the current environment**

Run representative fetch checks against the chosen official sources and record the result in `docs/source-and-index-reference.md`.

Use a section like:

```md
## Source reachability status

Verification date: 2026-04-19

### Official sources
- NVIDIA IR: reachable / not checked / blocked
- SEC EDGAR: reachable / not checked / blocked
- TSMC IR: reachable / not checked / blocked
- Apple IR: reachable / not checked / blocked
- Microsoft IR: reachable / not checked / blocked
- Alphabet IR: reachable / not checked / blocked
- Amazon IR: reachable / not checked / blocked
- Meta IR: reachable / not checked / blocked
- Tesla IR: reachable / not checked / blocked

Notes:
- Record whether the page loaded successfully in the current environment.
- If a source is blocked, timed out, redirected to an unsupported page, or needs a different access method, say so explicitly.
```

- [ ] **Step 3: Write the aggregate-source section with fallback order**

Add this content to `docs/source-and-index-reference.md`:

```md
## Aggregate market-data sources

Use these for valuation or market-style fields that company IR pages usually do not publish directly.

Priority order:
1. Financial Modeling Prep (FMP)
2. Alpha Vantage
3. Finnhub
4. Yahoo Finance / yfinance

Typical fields:
- Forward PE
- DCF or DCF-style values
- market cap
- valuation ratios
- analyst-estimate style fields
- quote snapshot

Fallback rule:
- Prefer official IR / filing pages for earnings facts.
- Prefer FMP for valuation metrics.
- If FMP fails, fall back to Alpha Vantage.
- If Alpha Vantage fails, fall back to Finnhub.
- If Finnhub fails, fall back to Yahoo Finance / yfinance.
- If all fail, return local-archive-only analysis and state that live data is unavailable.
```

- [ ] **Step 4: Check and record aggregate-source reachability in the current environment**

Run representative checks for the aggregate sources and append a section like this to `docs/source-and-index-reference.md`:

```md
### Aggregate sources
- FMP: reachable / not checked / blocked
- Alpha Vantage: reachable / not checked / blocked
- Finnhub: reachable / not checked / blocked
- Yahoo Finance / yfinance: reachable / not checked / blocked

Notes:
- Distinguish between "site reachable" and "usable without API key".
- If a source requires a key, record that it is reachable but gated.
- If a source is only accessible through a library flow rather than a plain page fetch, record that too.
```

- [ ] **Step 5: Add a provenance rule section**

Add this content to `docs/source-and-index-reference.md`:

```md
## Output provenance rules

- Label official facts as coming from IR / filings.
- Label market metrics with the actual aggregate provider used.
- If fallback occurred, say so explicitly.
- Do not describe aggregate-provider values as if they were reported directly by the company.
```

- [ ] **Step 6: Read the full doc and verify the sections appear in the intended order**

Run:
```bash
sed -n '1,260p' /Users/johnny/Documents/jjc-money/docs/source-and-index-reference.md
```
Expected: official-source guidance appears before reachability status, aggregate fallbacks, and provenance rules.

- [ ] **Step 7: Commit the source-reference document update**

```bash
git add /Users/johnny/Documents/jjc-money/docs/source-and-index-reference.md
git commit -m "docs: add market data source reference"
```

### Task 3: Record future 26year index maintenance targets

**Files:**
- Modify: `docs/source-and-index-reference.md`
- Modify: `docs/indexes/archive-index.md`
- Modify: `docs/indexes/monthly/2026-01.md`
- Modify: `docs/indexes/monthly/2026-02.md`
- Modify: `docs/indexes/monthly/2026-03.md`

- [ ] **Step 1: Write the month-to-index mapping**

Add this content to `docs/source-and-index-reference.md`:

```md
## 26year index maintenance targets

When `26year/` content changes, update these index files:

- `26year/26-01.md` -> `docs/indexes/monthly/2026-01.md`
- `26year/26-02月.md` -> `docs/indexes/monthly/2026-02.md`
- `26year/2026-03.md` -> `docs/indexes/monthly/2026-03.md`

Also update:
- `docs/indexes/archive-index.md`

Reason:
- month-level index files store article-level retrieval data
- `archive-index.md` stores month-level coverage, counts, date ranges, topics, and links
```

- [ ] **Step 2: Add the update checklist for new 26year files**

Add this content to `docs/source-and-index-reference.md`:

```md
## Checklist when a new `26year/` month file is added

1. Create or refresh the corresponding `docs/indexes/monthly/YYYY-MM.md`
2. Add or refresh the month row in `docs/indexes/archive-index.md`
3. Verify article count, date range, source file name, and locator style against the source month file
4. If the month file uses a newer local structure, index by its actual local headings and metadata instead of assuming the older archive pattern
```

- [ ] **Step 3: Verify the mapping section is easy to grep later**

Run:
```bash
rg -n "26year index maintenance targets|26-01.md|26-02月.md|2026-03.md|archive-index.md" /Users/johnny/Documents/jjc-money/docs/source-and-index-reference.md
```
Expected: one grep command surfaces all future update targets.

- [ ] **Step 4: If any 2026 month index files do not exist yet, create them before claiming `26year/` is indexed**

Run:
```bash
ls /Users/johnny/Documents/jjc-money/docs/indexes/monthly/2026-*.md
```
Expected: either existing files are listed or the command shows none, confirming follow-up indexing work is still needed.

- [ ] **Step 5: Commit the maintenance-target notes**

```bash
git add /Users/johnny/Documents/jjc-money/docs/source-and-index-reference.md
git commit -m "docs: record 26year index maintenance targets"
```

## Self-review checklist

- Spec coverage: the plan covers public source documentation, current `26year/` NVDA index verification, and the exact future index files to update.
- Placeholder scan: no TODO/TBD placeholders remain.
- Consistency: every referenced file path uses the existing repo layout and the same `26year/` -> `docs/indexes/monthly/YYYY-MM.md` mapping.
