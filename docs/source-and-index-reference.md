# Source and Index Reference

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

## V1 connection decision table

| Source type | Source | V1 connection method | Need code? | Need key? | Notes |
| --- | --- | --- | --- | --- | --- |
| Official | Apple / Microsoft / Alphabet / Amazon / Meta / NVIDIA / Tesla IR | HTTP read of public pages | No | No | Best for earnings releases, shareholder letters, and investor presentations |
| Official | SEC EDGAR | HTTP read of public pages | No | No | Best for 10-K / 10-Q / 8-K |
| Official | TSMC IR / TWSE filing pages | HTTP read of public pages | No | No | Best for quarterly revenue, guidance, and presentation materials |
| Aggregate | FMP | HTTP/API | Not initially | Yes | Preferred valuation source in v1 |
| Aggregate | Alpha Vantage | HTTP/API | Not initially | Yes | Fallback source |
| Aggregate | Finnhub | HTTP/API | Not initially | Usually yes | Fallback source |
| Aggregate | Yahoo Finance | HTTP page or existing tool | Not necessarily | No | Last-resort fallback; public site stability may vary |
| Aggregate | yfinance | Local code call | Yes | No | Only use if plain HTTP flow becomes unreliable |

V1 rules:
- Official facts use public HTTP reads first.
- Valuation and market-style metrics use HTTP/API providers.
- Do not introduce custom MCP in v1.
- Do not introduce a heavy code layer in v1.
- Add a thin code wrapper only if fallback logic, field normalization, or repeatability becomes painful.

## Source reachability status

Verification date: 2026-04-19

### Official sources
- NVIDIA IR: blocked via current WebFetch flow (HTTP 403)
- SEC EDGAR: blocked via current WebFetch flow (HTTP 403)
- TSMC IR: blocked via current WebFetch flow (HTTP 403)
- Apple IR: blocked via current WebFetch flow (HTTP 403)
- Microsoft IR: reachable; investor-relations homepage with earnings/financials, annual reports, SEC filings, dividends, events, and governance content
- Alphabet IR: reachable; investor-relations page with earnings, SEC filings, news/events, governance, FAQs, and alerts
- Amazon IR: blocked via current WebFetch flow (HTTP 403)
- Meta IR: blocked via current WebFetch flow (HTTP 403)
- Tesla IR: blocked via current WebFetch flow (HTTP 403)

Notes:
- "Blocked" here means inaccessible through the current Claude WebFetch path, not necessarily inaccessible in a normal browser.
- Reachability should be rechecked if the execution environment or access method changes.

### Aggregate sources
- FMP: reachable at `site.financialmodelingprep.com`; API-oriented and key-gated
- Alpha Vantage: reachable; API/data provider and key-gated
- Finnhub: reachable; API/data provider, site reachable, API usage may still require token setup
- Yahoo Finance: blocked via current WebFetch flow (HTTP 403)
- yfinance: reachable at PyPI package page; library-based path, no direct API key mentioned

Notes:
- Distinguish site reachability from actual query usability.
- FMP and Alpha Vantage clearly present API-key onboarding.
- Yahoo Finance page access through WebFetch was blocked, but yfinance remains a possible code-based fallback.

## Output provenance rules

- Label official facts as coming from IR / filings.
- Label market metrics with the actual aggregate provider used.
- If fallback occurred, say so explicitly.
- Do not describe aggregate-provider values as if they were reported directly by the company.

## Current 26year NVDA index coverage

Verification date: 2026-04-19

- Source files checked: `26year/26-01.md`, `26year/26-02月.md`, `26year/2026-03.md`
- Query used: `英伟达|NVDA|Nvidia|英偉達`
- Result in source files: matches found in all three files
- Result in `docs/indexes/`: current matches found only in older `2025-*` monthly indexes, not in `2026-*` month indexes
- Conclusion: `26year/` NVDA-related content exists in source files but is not yet represented in the current 2026 index set.

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

## Checklist when a new `26year/` month file is added

1. Create or refresh the corresponding `docs/indexes/monthly/YYYY-MM.md`
2. Add or refresh the month row in `docs/indexes/archive-index.md`
3. Verify article count, date range, source file name, and locator style against the source month file
4. If the month file uses a newer local structure, index by its actual local headings and metadata instead of assuming the older archive pattern
