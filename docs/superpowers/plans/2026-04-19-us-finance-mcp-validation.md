# US Finance MCP Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Verify whether the four MCP candidates named in `获取美股财报信息mcp.md` are real, installable, and capable of returning NVDA-related financial data or at least reaching a clearly documented credential gate.

**Architecture:** The work is split into two phases. First, perform evidence-based static verification for all four candidates and record a normalized comparison table. Then choose the 1-2 strongest candidates for lightweight local validation, stopping cleanly at OAuth or API-key boundaries and recording exact handoff instructions instead of guessing.

**Tech Stack:** Markdown specs and reports, Claude Code CLI, `claude mcp` commands, npm package metadata checks, web documentation checks, local shell verification

---

## File map

- Check: `获取美股财报信息mcp.md` — source list of the four candidate MCPs and claimed capabilities
- Create: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md` — working report with raw findings, evidence links, command outputs summary, and final recommendation
- Modify: `docs/superpowers/specs/2026-04-19-us-finance-mcp-validation-design.md` — only if execution reveals the spec is materially wrong
- Create: `docs/superpowers/plans/2026-04-19-us-finance-mcp-validation.md` — this implementation plan file
- Check: local Claude MCP configuration via `claude mcp list` and candidate-specific `claude mcp add ...` attempts during lightweight validation

## Candidate normalization

Use the following exact candidate labels throughout the implementation so the report stays internally consistent:

- `financial-datasets` — claimed package: `@financialdatasets/mcp-server`
- `trading-mcp` — claimed package: `@netanelavr/trading-mcp`
- `alpha-vantage-mcp` — exact package or repository name must be verified during static validation before any install attempt
- `defeatbeta-api` — exact MCP package or integration path must be verified during static validation before any install attempt

If a later command shows that a candidate is not actually an MCP server package, preserve the original candidate label in the table and set its conclusion to `not verified as MCP`.

### Task 1: Create the validation report skeleton

**Files:**
- Create: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`
- Check: `获取美股财报信息mcp.md`

- [ ] **Step 1: Read the source note and extract the exact claims**

Run:
```bash
python - <<'PY'
from pathlib import Path
p = Path('/Users/johnny/Documents/jjc-money/获取美股财报信息mcp.md')
print(p.read_text())
PY
```
Expected: the file prints the four candidate names, claimed install commands, and claimed capabilities such as Forward PE and earnings data.

- [ ] **Step 2: Create the report skeleton with a normalized table**

Create `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md` with this content:

```md
# US Finance MCP Validation Report

Verification date: 2026-04-19
Source note: `获取美股财报信息mcp.md`
Target symbol for lightweight checks: `NVDA`

## Scope

This report verifies four claimed MCP candidates:

- financial-datasets
- trading-mcp
- alpha-vantage-mcp
- defeatbeta-api

The checks are split into:

1. static verification for all four candidates
2. lightweight local validation for the strongest 1-2 candidates

## Result table

| Candidate | Reliable source found | Install command verified | Credential requirement | Tool listing verified | NVDA query verified | Forward PE / statements / earnings / overview evidence | Current conclusion |
| --- | --- | --- | --- | --- | --- | --- | --- |
| financial-datasets | pending | pending | pending | pending | pending | pending | pending |
| trading-mcp | pending | pending | pending | pending | pending | pending | pending |
| alpha-vantage-mcp | pending | pending | pending | pending | pending | pending | pending |
| defeatbeta-api | pending | pending | pending | pending | pending | pending | pending |

## Static verification notes

## Lightweight local validation notes

## Recommendation
```

- [ ] **Step 3: Verify the report file exists and includes the table header**

Run:
```bash
python - <<'PY'
from pathlib import Path
p = Path('/Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md')
text = p.read_text()
print('Result table' in text)
print('| Candidate | Reliable source found |' in text)
PY
```
Expected: prints `True` twice.

- [ ] **Step 4: Commit the report skeleton**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: add us finance mcp validation report skeleton"
```

### Task 2: Staticaly verify `financial-datasets`

**Files:**
- Modify: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: Verify the npm package exists**

Run:
```bash
npm view @financialdatasets/mcp-server name version description repository.url --json
```
Expected: JSON output with a package name matching `@financialdatasets/mcp-server`.

- [ ] **Step 2: Verify the claimed install command resolves to a package**

Run:
```bash
npx -y @financialdatasets/mcp-server --help
```
Expected: help text, startup text, or a credential-related startup failure that proves the package is executable.

- [ ] **Step 3: Record credential expectations from the package or official docs**

Append a note like this to the `Static verification notes` section:

```md
### financial-datasets
- npm package lookup: success / failed
- executable via `npx`: success / failed
- credential gate: OAuth / API key / none / unclear
- evidence for fields: note any explicit mention of income statement, financial metrics, Forward PE, or similar fields
- provisional rank: strong / medium / weak
```

- [ ] **Step 4: Update the result table row**

Change the `financial-datasets` row to match the observed evidence, for example:

```md
| financial-datasets | yes | yes | OAuth required before data calls | pending | pending | financial metrics and statements documented; Forward PE to be confirmed in runtime tools | strong candidate for runtime check |
```

- [ ] **Step 5: Commit the `financial-datasets` findings**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: verify financial-datasets mcp candidate"
```

### Task 3: Statically verify `trading-mcp`

**Files:**
- Modify: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: Verify the npm package exists**

Run:
```bash
npm view @netanelavr/trading-mcp name version description repository.url --json
```
Expected: JSON output with a package name matching `@netanelavr/trading-mcp`.

- [ ] **Step 2: Verify the package is executable**

Run:
```bash
npx -y @netanelavr/trading-mcp --help
```
Expected: help text, startup text, or a startup failure that still proves the package exists and runs.

- [ ] **Step 3: Record field evidence and credential expectations**

Append a note like this to the `Static verification notes` section:

```md
### trading-mcp
- npm package lookup: success / failed
- executable via `npx`: success / failed
- credential gate: API key / OAuth / none / unclear
- evidence for fields: note any explicit mention of valuation comparison, stock metrics, or fundamentals
- provisional rank: strong / medium / weak
```

- [ ] **Step 4: Update the result table row**

Change the `trading-mcp` row to match the observed evidence, for example:

```md
| trading-mcp | yes | yes | API key unclear before runtime | pending | pending | valuation comparison documented; exact Forward PE and statements coverage still to confirm | medium candidate for runtime check |
```

- [ ] **Step 5: Commit the `trading-mcp` findings**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: verify trading-mcp candidate"
```

### Task 4: Statically verify `alpha-vantage-mcp`

**Files:**
- Modify: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: Search for a real MCP package or repository name before installing anything**

Run:
```bash
npm search alpha vantage mcp --json
```
Expected: zero or more candidate packages. At least one package must be inspected before concluding whether a real MCP server exists.

- [ ] **Step 2: Inspect the strongest package candidate, if any**

Run the exact command that matches the strongest result from Step 1, for example:
```bash
npm view <exact-package-from-search> name version description repository.url --json
```
Expected: package metadata for the selected candidate, or no credible candidate found.

- [ ] **Step 3: Record whether this is a verified MCP or just an API/library mention**

Append a note like this to `Static verification notes`:

```md
### alpha-vantage-mcp
- npm or repository evidence: verified MCP / only API wrappers found / no reliable MCP found
- install command from source note: not provided or not verified
- credential gate: Alpha Vantage API key likely required if a real integration exists
- evidence for fields: Earnings and Company Overview may exist at API level; MCP packaging status must be stated separately
- provisional rank: strong / medium / weak / not verified as MCP
```

- [ ] **Step 4: Update the result table row**

Use a row like one of these, depending on evidence:

```md
| alpha-vantage-mcp | no verified MCP found | no | API key would be required even if integration exists | no | no | Alpha Vantage API supports earnings-style endpoints, but MCP packaging not verified | not verified as MCP |
```

or

```md
| alpha-vantage-mcp | yes | partial | API key required | pending | pending | Earnings and Overview documented; runtime check needed | possible runtime candidate |
```

- [ ] **Step 5: Commit the Alpha Vantage findings**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: verify alpha vantage mcp candidate"
```

### Task 5: Statically verify `defeatbeta-api`

**Files:**
- Modify: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: Search for a real MCP package or repository name**

Run:
```bash
npm search defeatbeta mcp --json
```
Expected: zero or more candidate packages. If none appear, the candidate starts from a weak position.

- [ ] **Step 2: Inspect the strongest package or repository candidate, if any**

Run the exact command that matches the strongest result from Step 1, for example:
```bash
npm view <exact-package-from-search> name version description repository.url --json
```
Expected: package metadata for the selected candidate, or no credible candidate found.

- [ ] **Step 3: Record whether this is a verified MCP or only a general API claim**

Append a note like this to `Static verification notes`:

```md
### defeatbeta-api
- npm or repository evidence: verified MCP / only API docs found / no reliable MCP found
- install command from source note: not provided or not verified
- credential gate: API key / account / unclear
- evidence for fields: three statements, valuation metrics, or other claims must be backed by docs
- provisional rank: strong / medium / weak / not verified as MCP
```

- [ ] **Step 4: Update the result table row**

Use a row like one of these, depending on evidence:

```md
| defeatbeta-api | no verified MCP found | no | unclear | no | no | API-level finance claims found, but MCP packaging not verified | not verified as MCP |
```

or

```md
| defeatbeta-api | yes | partial | API key required | pending | pending | statement and valuation coverage documented; runtime check needed | possible runtime candidate |
```

- [ ] **Step 5: Commit the Defeatbeta findings**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: verify defeatbeta mcp candidate"
```

### Task 6: Rank candidates and choose 1-2 runtime targets

**Files:**
- Modify: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: List current rows and notes side by side**

Run:
```bash
python - <<'PY'
from pathlib import Path
text = Path('/Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md').read_text()
for marker in ['| financial-datasets |','| trading-mcp |','| alpha-vantage-mcp |','| defeatbeta-api |','## Static verification notes']:
    print(marker, marker in text)
PY
```
Expected: prints `True` for all markers.

- [ ] **Step 2: Add an explicit runtime target section**

Append this section, adjusted to the actual evidence:

```md
## Runtime targets

Chosen for lightweight local validation:

1. financial-datasets
   - reason: strongest verified MCP packaging and strongest evidence of financial statement coverage
2. trading-mcp
   - reason: verified MCP packaging and plausible valuation tooling

Not chosen for runtime validation:

- alpha-vantage-mcp
  - reason: MCP packaging not verified strongly enough for local install time
- defeatbeta-api
  - reason: MCP packaging not verified strongly enough for local install time
```

- [ ] **Step 3: Verify no unchosen candidate is mislabeled as runtime-ready**

Run:
```bash
python - <<'PY'
from pathlib import Path
text = Path('/Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md').read_text()
assert '## Runtime targets' in text
print('runtime section ok')
PY
```
Expected: prints `runtime section ok`.

- [ ] **Step 4: Commit the runtime target decision**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: choose runtime targets for mcp validation"
```

### Task 7: Run lightweight local validation for runtime target 1

**Files:**
- Modify: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: Capture current MCP list before adding anything**

Run:
```bash
claude mcp list
```
Expected: list of existing MCP servers, used as a baseline before any add attempt.

- [ ] **Step 2: Add the first runtime target using its verified install command**

Run the exact command for the chosen target. If `financial-datasets` is chosen, use:
```bash
claude mcp add financial-datasets npx -y @financialdatasets/mcp-server
```
Expected: successful add, or an error that still proves the command path and package are being resolved.

- [ ] **Step 3: Re-list MCP servers to confirm registration state**

Run:
```bash
claude mcp list
```
Expected: the new MCP appears, or the add failure is clearly visible from the prior command and must be recorded.

- [ ] **Step 4: Attempt the shallowest runtime interaction available**

Use the smallest non-destructive follow-up that the installed server supports. For example, if the MCP exposes auth status or tool discovery through Claude after registration, record that. If the server immediately requires login, record the gate exactly.

Add a note like this under `Lightweight local validation notes`:

```md
### financial-datasets runtime check
- add command result: success / failed
- listed in `claude mcp list`: yes / no
- next observable step: tool discovery / auth prompt / startup failure
- NVDA query status: not attempted due to auth gate / attempted and succeeded / attempted and failed
- stop reason: OAuth required before further verification / none
```

- [ ] **Step 5: Update the result table row for runtime evidence**

Change the row to reflect runtime evidence, for example:

```md
| financial-datasets | yes | yes | OAuth required before data calls | yes | no, blocked at auth | statements and financial metrics documented; runtime blocked before NVDA query | installable and likely viable, blocked by OAuth |
```

- [ ] **Step 6: Commit the first runtime validation result**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: record first runtime mcp validation"
```

### Task 8: Run lightweight local validation for runtime target 2

**Files:**
- Modify: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: Add the second runtime target using its verified install command**

If `trading-mcp` is chosen, run:
```bash
claude mcp add trading-mcp npx -y @netanelavr/trading-mcp
```
Expected: successful add, or an error that still proves the package is reachable or reveals an immediate credential/configuration gate.

- [ ] **Step 2: Re-list MCP servers after the add attempt**

Run:
```bash
claude mcp list
```
Expected: confirms whether the second target was registered.

- [ ] **Step 3: Attempt the shallowest runtime interaction available**

Add a note like this under `Lightweight local validation notes`:

```md
### trading-mcp runtime check
- add command result: success / failed
- listed in `claude mcp list`: yes / no
- next observable step: tool discovery / auth prompt / startup failure
- NVDA query status: not attempted due to auth gate / attempted and succeeded / attempted and failed
- stop reason: API key required before further verification / none
```

- [ ] **Step 4: Update the result table row for runtime evidence**

Change the row to reflect runtime evidence, for example:

```md
| trading-mcp | yes | yes | API key or config required before live calls | yes | no, blocked at runtime | valuation tooling documented; exact NVDA field proof blocked by credential gate | installable but gated |
```

- [ ] **Step 5: Commit the second runtime validation result**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: record second runtime mcp validation"
```

### Task 9: Write the stop-point and user handoff section

**Files:**
- Modify: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: Add a credential gate section that makes the pause explicit**

Append this section, adjusted to the actual findings:

```md
## Credential-gated next steps

The following follow-up actions require user-provided credentials or interactive login:

- financial-datasets: OAuth login required to continue beyond installation
- trading-mcp: API key or service-specific configuration required before live market-data queries

If the user wants full NVDA field verification next, ask for exactly one of these:

1. permission to run the interactive login flow for the selected MCP
2. the required API key entered locally by the user
3. a decision to stop with installation-level verification only
```

- [ ] **Step 2: Add the final recommendation section**

Append a section like this, adjusted to evidence:

```md
## Recommendation

Recommended next MCP to continue with: financial-datasets

Why:
- strongest evidence of being a real MCP package
- strongest documented finance-specific surface
- most likely path to statements and valuation metrics once authenticated

Second choice: trading-mcp

Why:
- plausible valuation tooling
- weaker direct evidence for the exact NVDA fields than financial-datasets
```

- [ ] **Step 3: Verify the report now satisfies the spec stop conditions**

Run:
```bash
python - <<'PY'
from pathlib import Path
text = Path('/Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md').read_text()
checks = [
    'financial-datasets',
    'trading-mcp',
    'alpha-vantage-mcp',
    'defeatbeta-api',
    '## Runtime targets',
    '## Credential-gated next steps',
    '## Recommendation',
]
for item in checks:
    print(item, item in text)
PY
```
Expected: prints `True` for every item.

- [ ] **Step 4: Commit the handoff guidance**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: add mcp validation handoff guidance"
```

### Task 10: Final self-check and delivery

**Files:**
- Check: `docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md`

- [ ] **Step 1: Read the full report and confirm there are no `pending` placeholders left in completed rows**

Run:
```bash
python - <<'PY'
from pathlib import Path
text = Path('/Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md').read_text()
print('pending' in text)
PY
```
Expected: `False` if all completed work has been filled in. If runtime work is intentionally not done for an unchosen candidate, replace `pending` with an explicit reason.

- [ ] **Step 2: Read the final report for internal consistency**

Run:
```bash
python - <<'PY'
from pathlib import Path
p = Path('/Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md')
print(p.read_text())
PY
```
Expected: the table, runtime notes, credential-gated next steps, and recommendation all agree with each other.

- [ ] **Step 3: Check git status before reporting completion**

Run:
```bash
git status --short
```
Expected: only the report file and any intentionally created supporting files are modified.

- [ ] **Step 4: Commit the finalized report**

```bash
git add /Users/johnny/Documents/jjc-money/docs/mcp-validation/us-finance-mcp-validation-2026-04-19.md
git commit -m "docs: finalize us finance mcp validation report"
```

## Self-review

- **Spec coverage:** The plan covers static verification for all four candidates, lightweight local validation for 1-2 runtime targets, a strict evidence table, explicit credential stop points, and a final recommendation. No spec requirement is left without a task.
- **Placeholder scan:** The implementation tasks avoid `TODO` and `TBD`. Where outcomes can vary, the plan provides exact replacement text patterns instead of vague instructions.
- **Type consistency:** Candidate names, report filename, and section names are reused consistently across all tasks so later steps can verify exact markers without renaming drift.
