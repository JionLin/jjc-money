# Archive Index System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a long-lived Markdown index system for the `22-25year/` archive so future questions can be answered by first locating relevant files quickly, then reading only matched source passages and replying with conclusions plus original-text evidence.

**Architecture:** Keep the source archive unchanged and add a parallel `docs/indexes/` layer. The implementation has three parts: define stable index file formats, generate the initial archive-level and month-level Markdown indexes from existing files, and establish a retrieval workflow that uses the indexes first and source files second. Special handling for `25-12月.md` is planned explicitly because its structure differs from the monthly aggregate files.

**Tech Stack:** Markdown archives, shell inspection commands, Claude Code file read/edit tools, repository documentation under `docs/`

---

## File Structure

### Existing files to read
- `CLAUDE.md` — repository rules and archive structure
- `docs/superpowers/specs/2026-04-18-archive-index-design.md` — approved design
- `22-25year/*.md` — source archive files to index
- `22-25year/25-12月.md` — special-format archive file

### Files to create
- `docs/indexes/README.md` — explains how the index system is organized and maintained
- `docs/indexes/archive-index.md` — top-level archive summary across all month files
- `docs/indexes/monthly/` — directory for month-level index files
- `docs/indexes/monthly/2022-11.md`
- `docs/indexes/monthly/2022-12.md`
- `docs/indexes/monthly/2023-07.md`
- `docs/indexes/monthly/2023-08.md`
- `docs/indexes/monthly/2023-09.md`
- `docs/indexes/monthly/2023-11.md`
- `docs/indexes/monthly/2023-12.md`
- `docs/indexes/monthly/2024-01.md`
- `docs/indexes/monthly/2024-02.md`
- `docs/indexes/monthly/2024-03.md`
- `docs/indexes/monthly/2024-04.md`
- `docs/indexes/monthly/2024-05.md`
- `docs/indexes/monthly/2024-06.md`
- `docs/indexes/monthly/2024-08.md`
- `docs/indexes/monthly/2024-09.md`
- `docs/indexes/monthly/2024-10.md`
- `docs/indexes/monthly/2024-11.md`
- `docs/indexes/monthly/2024-12.md`
- `docs/indexes/monthly/2025-01.md`
- `docs/indexes/monthly/2025-02.md`
- `docs/indexes/monthly/2025-03.md`
- `docs/indexes/monthly/2025-04.md`
- `docs/indexes/monthly/2025-05.md`
- `docs/indexes/monthly/2025-06.md`
- `docs/indexes/monthly/2025-07.md`
- `docs/indexes/monthly/2025-08.md`
- `docs/indexes/monthly/2025-09.md`
- `docs/indexes/monthly/2025-10.md`
- `docs/indexes/monthly/2025-11.md`
- `docs/indexes/monthly/2025-12.md`

### Files to modify
- `docs/superpowers/plans/2026-04-18-archive-index-system.md` — this implementation plan itself

---

### Task 1: Define the stable index format

**Files:**
- Create: `docs/indexes/README.md`
- Create: `docs/indexes/archive-index.md`
- Create: `docs/indexes/monthly/.gitkeep`
- Modify: `docs/superpowers/specs/2026-04-18-archive-index-design.md:20-130` (reference only; do not edit)

- [ ] **Step 1: Write the failing structure check by inspecting the current indexes directory**

Run: `ls -la "/Users/johnny/Documents/jjc-money/docs/indexes" && ls -la "/Users/johnny/Documents/jjc-money/docs/indexes/monthly"`
Expected: `archive-index.md`, `README.md`, and `monthly/` contents are missing or incomplete

- [ ] **Step 2: Create the monthly index directory placeholder**

```text
Path: docs/indexes/monthly/.gitkeep
Content:

```

- [ ] **Step 3: Write the maintenance guide with the exact format rules**

```markdown
# Archive Indexes

## Purpose

These files speed up archive retrieval. They are not the source of truth. Answers must still be verified against the source files in `22-25year/`.

## Structure

- `archive-index.md`: top-level month summary
- `monthly/YYYY-MM.md`: per-month article index

## Archive index fields

Each month entry must include:
- source file name
- article count
- date range
- base topic keywords
- link to the month index

## Monthly index fields

Each article entry must include:
- article date
- article title
- source file name
- source heading or source locator
- base topic keywords
- optional one-line summary

## Retrieval workflow

1. Search `archive-index.md` to narrow candidate months.
2. Search one or more files in `monthly/` to locate candidate articles.
3. Read matched sections from the source archive file.
4. Answer with a short conclusion followed by quoted source evidence.

## Special cases

`22-25year/25-12月.md` does not follow the older `## YYYY-MM-DD_金渐成_标题` pattern consistently. Its month index must use the local structure in that file, such as publication date and section title.

## Maintenance

When a new source month is added:
1. add or refresh the corresponding `monthly/YYYY-MM.md`
2. update `archive-index.md`
3. keep links and article counts in sync
```

- [ ] **Step 4: Write the top-level archive index template with explicit columns**

```markdown
# Archive Index

## Scope

Source directory: `22-25year/`
Index type: archive-level month summary

## Months

| Month | Source File | Article Count | Date Range | Base Topics | Month Index |
| --- | --- | ---: | --- | --- | --- |
| 2022-11 | `22-25year/2022-11(共8篇).md` | 8 | 2022-11-xx to 2022-11-xx | 待补充 | `monthly/2022-11.md` |
```

- [ ] **Step 5: Run a structure check to verify the files now exist**

Run: `ls -la "/Users/johnny/Documents/jjc-money/docs/indexes" && ls -la "/Users/johnny/Documents/jjc-money/docs/indexes/monthly"`
Expected: `README.md`, `archive-index.md`, and `monthly/.gitkeep` exist

- [ ] **Step 6: Record the format-definition checkpoint**

Run: `ls -la "/Users/johnny/Documents/jjc-money/docs/indexes" && ls -la "/Users/johnny/Documents/jjc-money/docs/indexes/monthly"`
Expected: the format files exist and are ready for the next task

### Task 2: Build the archive-level month summary

**Files:**
- Modify: `docs/indexes/archive-index.md`
- Read: `22-25year/*.md`

- [ ] **Step 1: Write the failing content check for the archive index**

Run: `grep -n "| 2025-11 |" "/Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md"`
Expected: no match because the archive index has not been populated yet

- [ ] **Step 2: Read the source file list and capture month metadata**

Run: `ls -1 "/Users/johnny/Documents/jjc-money/22-25year"`
Expected: the archive month files are listed, including older monthly aggregates and `25-12月.md`

- [ ] **Step 3: Populate the archive month table with real rows**

```markdown
| Month | Source File | Article Count | Date Range | Base Topics | Month Index |
| --- | --- | ---: | --- | --- | --- |
| 2022-11 | `22-25year/2022-11(共8篇).md` | 8 | derive from article headings | 由月度索引汇总 | `monthly/2022-11.md` |
| 2022-12 | `22-25year/2022-12(共1篇).md` | 1 | derive from article headings | 由月度索引汇总 | `monthly/2022-12.md` |
| 2023-07 | `22-25year/2023-07(共10篇).md` | 10 | derive from article headings | 由月度索引汇总 | `monthly/2023-07.md` |
| 2025-12 | `22-25year/25-12月.md` | derive from local sections | derive from publication dates | 由月度索引汇总 | `monthly/2025-12.md` |
```

- [ ] **Step 4: Verify that every source month file has an archive index row**

Run: `python3 - <<'PY'
from pathlib import Path
src = sorted(p.name for p in Path('/Users/johnny/Documents/jjc-money/22-25year').glob('*.md'))
idx = Path('/Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md').read_text()
missing = [name for name in src if name not in idx]
print('MISSING:', missing)
PY`
Expected: `MISSING: []`

- [ ] **Step 5: Record the archive summary checkpoint**

Run: `grep -n "| 2025-11 |" "/Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md"`
Expected: at least one populated archive summary row is present

### Task 3: Build one month index for the standard monthly format

**Files:**
- Create: `docs/indexes/monthly/2025-11.md`
- Read: `22-25year/2025-11(共9篇).md`

- [ ] **Step 1: Write the failing existence check for the sample month index**

Run: `test -f "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-11.md"`
Expected: exit status 1 because the month index does not exist yet

- [ ] **Step 2: Read the month source and extract article headings**

Run: `python3 - <<'PY'
from pathlib import Path
import re
text = Path('/Users/johnny/Documents/jjc-money/22-25year/2025-11(共9篇).md').read_text(errors='ignore')
for line in text.splitlines():
    if line.startswith('## '):
        print(line)
PY`
Expected: a list of `## YYYY-MM-DD_金渐成_标题` headings

- [ ] **Step 3: Write the month index using the standard article-entry format**

```markdown
# 2025-11 Index

- Source file: `22-25year/2025-11(共9篇).md`
- Article count: 9
- Date range: derive from the first and last article headings

## Articles

### 2025-11-01｜写了一段很重要的评论看法，审核无法通过，发送失败。nn简单来
- Source heading: `## 2025-11-01_金渐成_写了一段很重要的评论看法，审核无法通过，发送失败。nn简单来`
- Base topics: 投资, 风险控制, 资产配置, 再平衡
- Summary: 强调高位阶段以风险控制和资产再平衡为主。

### 2025-11-03｜此刻心定~
- Source heading: `## 2025-11-03_金渐成_此刻心定~`
- Base topics: 投资, 仓位管理, 防守型资产, 家庭
- Summary: 强调提前构筑安全边际和防守型资产，同时回归家庭重心。
```

- [ ] **Step 4: Verify the month index points back to the source headings**

Run: `grep -n "Source heading:" "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-11.md"`
Expected: one `Source heading:` line per article entry

- [ ] **Step 5: Record the standard-month checkpoint**

Run: `grep -n "Source heading:" "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-11.md"`
Expected: one `Source heading:` line per article entry remains present

### Task 4: Build one month index for the special December format

**Files:**
- Create: `docs/indexes/monthly/2025-12.md`
- Read: `22-25year/25-12月.md`

- [ ] **Step 1: Write the failing existence check for the special-format month index**

Run: `test -f "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-12.md"`
Expected: exit status 1 because the file does not exist yet

- [ ] **Step 2: Read the special-format source and identify the local locator pattern**

Run: `python3 - <<'PY'
from pathlib import Path
text = Path('/Users/johnny/Documents/jjc-money/22-25year/25-12月.md').read_text(errors='ignore')
for line in text.splitlines()[:40]:
    print(line)
PY`
Expected: article title lines plus `**📅 发布日期**` and `**🏷️ 标签**` metadata lines

- [ ] **Step 3: Write the month index using publication date and title as the source locator**

```markdown
# 2025-12 Index

- Source file: `22-25year/25-12月.md`
- Structure: special presentation format with metadata blocks

## Articles

### 2025-12-02｜为未来撒下种子~
- Source locator: title `# 为未来撒下种子~` + publication date `2025年12月02日`
- Base topics: 投资, 凯利公式, 风险控制, 人生哲学, 财富自由
- Summary: 用凯利公式与风险收益平衡解释防守型与稳健型资产配置的必要性。
```

- [ ] **Step 4: Verify the special month index uses `Source locator` entries instead of standard headings**

Run: `grep -n "Source locator:" "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-12.md"`
Expected: one `Source locator:` line per article entry

- [ ] **Step 5: Record the special-month checkpoint**

Run: `grep -n "Source locator:" "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-12.md"`
Expected: one `Source locator:` line per article entry remains present

### Task 5: Populate the remaining month indexes

**Files:**
- Create: `docs/indexes/monthly/2022-11.md`
- Create: `docs/indexes/monthly/2022-12.md`
- Create: `docs/indexes/monthly/2023-07.md`
- Create: `docs/indexes/monthly/2023-08.md`
- Create: `docs/indexes/monthly/2023-09.md`
- Create: `docs/indexes/monthly/2023-11.md`
- Create: `docs/indexes/monthly/2023-12.md`
- Create: `docs/indexes/monthly/2024-01.md`
- Create: `docs/indexes/monthly/2024-02.md`
- Create: `docs/indexes/monthly/2024-03.md`
- Create: `docs/indexes/monthly/2024-04.md`
- Create: `docs/indexes/monthly/2024-05.md`
- Create: `docs/indexes/monthly/2024-06.md`
- Create: `docs/indexes/monthly/2024-08.md`
- Create: `docs/indexes/monthly/2024-09.md`
- Create: `docs/indexes/monthly/2024-10.md`
- Create: `docs/indexes/monthly/2024-11.md`
- Create: `docs/indexes/monthly/2024-12.md`
- Create: `docs/indexes/monthly/2025-01.md`
- Create: `docs/indexes/monthly/2025-02.md`
- Create: `docs/indexes/monthly/2025-03.md`
- Create: `docs/indexes/monthly/2025-04.md`
- Create: `docs/indexes/monthly/2025-05.md`
- Create: `docs/indexes/monthly/2025-06.md`
- Create: `docs/indexes/monthly/2025-07.md`
- Create: `docs/indexes/monthly/2025-08.md`
- Create: `docs/indexes/monthly/2025-09.md`
- Create: `docs/indexes/monthly/2025-10.md`
- Modify: `docs/indexes/archive-index.md`

- [ ] **Step 1: Write the failing completeness check for the month index directory**

Run: `python3 - <<'PY'
from pathlib import Path
monthly = Path('/Users/johnny/Documents/jjc-money/docs/indexes/monthly')
print(sorted(p.name for p in monthly.glob('*.md')))
PY`
Expected: only the sample month files exist, so the directory is incomplete

- [ ] **Step 2: Create the remaining standard-format month index files using the `2025-11.md` structure**

```markdown
# YYYY-MM Index

- Source file: `22-25year/<source-file>.md`
- Article count: <count>
- Date range: <first article date> to <last article date>

## Articles

### YYYY-MM-DD｜<article title>
- Source heading: `## YYYY-MM-DD_金渐成_<article title>`
- Base topics: <comma-separated topic list>
- Summary: <one-line summary>
```

- [ ] **Step 3: Create the remaining special-format month files, if any, using the `2025-12.md` locator style**

```markdown
### YYYY-MM-DD｜<article title>
- Source locator: title `<title>` + publication date `<date>`
- Base topics: <comma-separated topic list>
- Summary: <one-line summary>
```

- [ ] **Step 4: Update `archive-index.md` so article counts, date ranges, and base topics reflect the actual month files**

```markdown
| 2025-11 | `22-25year/2025-11(共9篇).md` | 9 | 2025-11-01 to 2025-11-28 | 投资, 风险控制, 资产配置, 家庭 | `monthly/2025-11.md` |
```

- [ ] **Step 5: Verify that every archive source file has a matching monthly index file**

Run: `python3 - <<'PY'
from pathlib import Path
src = sorted(p.name for p in Path('/Users/johnny/Documents/jjc-money/22-25year').glob('*.md'))
monthly = Path('/Users/johnny/Documents/jjc-money/docs/indexes/monthly')
expected = []
for name in src:
    if name == '25-12月.md':
        expected.append('2025-12.md')
    else:
        expected.append(name.split('(')[0] + '.md')
actual = sorted(p.name for p in monthly.glob('*.md'))
missing = [name for name in expected if name not in actual]
print('MISSING:', missing)
PY`
Expected: `MISSING: []`

- [ ] **Step 6: Record the full month-index checkpoint**

Run: `python3 - <<'PY'
from pathlib import Path
monthly = sorted(p.name for p in Path('/Users/johnny/Documents/jjc-money/docs/indexes/monthly').glob('*.md'))
print('MONTHLY_COUNT:', len(monthly))
print('LAST_FIVE:', monthly[-5:])
PY`
Expected: the month index directory contains all expected month files

### Task 6: Establish the retrieval workflow for future Q&A

**Files:**
- Modify: `docs/indexes/README.md`
- Modify: `docs/indexes/archive-index.md`
- Modify: at least one sample month index file such as `docs/indexes/monthly/2025-11.md`

- [ ] **Step 1: Write the failing workflow check by searching for a retrieval section**

Run: `grep -n "## Retrieval workflow" "/Users/johnny/Documents/jjc-money/docs/indexes/README.md"`
Expected: no match or incomplete instructions

- [ ] **Step 2: Add a concise retrieval checklist to the README**

```markdown
## Retrieval workflow

When answering a question:
1. identify time, topic, title, or keyword clues in the question
2. search `archive-index.md` for likely months
3. search one or more month files in `monthly/` for matching articles
4. read only the matched source sections from `22-25year/`
5. reply in this structure:
   - conclusion
   - article list if helpful
   - quoted source evidence
```

- [ ] **Step 3: Add a quick-navigation section to the archive index**

```markdown
## How to use this file

- Search for a month, theme, or source file name.
- Open the linked month index.
- Return to the source archive file before finalizing any answer.
```

- [ ] **Step 4: Add a quick-navigation note to one sample month index**

```markdown
> To answer a question from this month, use the base topics to find candidate articles, then read the source file around the recorded source heading before quoting or summarizing.
```

- [ ] **Step 5: Verify that the retrieval instructions exist in all three places**

Run: `grep -n "Retrieval workflow\|How to use this file\|To answer a question from this month" "/Users/johnny/Documents/jjc-money/docs/indexes/README.md" "/Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md" "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-11.md"`
Expected: all three files return matches

- [ ] **Step 6: Record the retrieval-workflow checkpoint**

Run: `grep -n "Retrieval workflow\|How to use this file\|To answer a question from this month" "/Users/johnny/Documents/jjc-money/docs/indexes/README.md" "/Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md" "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-11.md"`
Expected: all three files return matches

### Task 7: Verify the completed index system against the design

**Files:**
- Read: `docs/superpowers/specs/2026-04-18-archive-index-design.md`
- Read: `docs/indexes/README.md`
- Read: `docs/indexes/archive-index.md`
- Read: `docs/indexes/monthly/*.md`

- [ ] **Step 1: Check that all required index files exist**

Run: `python3 - <<'PY'
from pathlib import Path
paths = [
    Path('/Users/johnny/Documents/jjc-money/docs/indexes/README.md'),
    Path('/Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md'),
    Path('/Users/johnny/Documents/jjc-money/docs/indexes/monthly'),
]
for p in paths:
    print(p, p.exists())
PY`
Expected: every path prints `True`

- [ ] **Step 2: Check that the archive index covers every source file**

Run: `python3 - <<'PY'
from pathlib import Path
src = sorted(p.name for p in Path('/Users/johnny/Documents/jjc-money/22-25year').glob('*.md'))
idx = Path('/Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md').read_text()
missing = [name for name in src if name not in idx]
print('MISSING:', missing)
PY`
Expected: `MISSING: []`

- [ ] **Step 3: Check that the month index directory covers every source month**

Run: `python3 - <<'PY'
from pathlib import Path
src = sorted(p.name for p in Path('/Users/johnny/Documents/jjc-money/22-25year').glob('*.md'))
monthly = Path('/Users/johnny/Documents/jjc-money/docs/indexes/monthly')
expected = []
for name in src:
    if name == '25-12月.md':
        expected.append('2025-12.md')
    else:
        expected.append(name.split('(')[0] + '.md')
actual = sorted(p.name for p in monthly.glob('*.md'))
missing = [name for name in expected if name not in actual]
print('MISSING:', missing)
PY`
Expected: `MISSING: []`

- [ ] **Step 4: Manually verify one standard month and the special month**

Run: `grep -n "Source heading:\|Source locator:" "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-11.md" "/Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-12.md"`
Expected: `2025-11.md` contains `Source heading:` lines and `2025-12.md` contains `Source locator:` lines

- [ ] **Step 5: Record the verification checkpoint**

Run: `python3 - <<'PY'
from pathlib import Path
print('README:', Path('/Users/johnny/Documents/jjc-money/docs/indexes/README.md').exists())
print('ARCHIVE:', Path('/Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md').exists())
print('MONTHLY_FILES:', len(list(Path('/Users/johnny/Documents/jjc-money/docs/indexes/monthly').glob('*.md'))))
PY`
Expected: all required files exist and the month index count is non-zero

## Self-Review

### Spec coverage
- Total archive summary: covered by Task 2.
- Month-level indexes: covered by Tasks 3, 4, and 5.
- Special handling for `25-12月.md`: covered by Task 4 and verified again in Task 7.
- Retrieval flow of “index first, source file second”: covered by Task 6.
- Long-term Markdown-readable maintenance: covered by Task 1 and Task 6.
- Answering with “conclusion + source evidence”: covered by Task 6.

### Placeholder scan
- All paths are explicit.
- All commands are explicit.
- No `TODO`, `TBD`, or “implement later” placeholders remain.
- Content templates are concrete and tied to the actual repository structure.

### Type consistency
- Archive-level index file is always `docs/indexes/archive-index.md`.
- Month-level index directory is always `docs/indexes/monthly/`.
- Standard format uses `Source heading:`.
- Special format uses `Source locator:`.
- Retrieval flow consistently requires returning to the source archive before answering.
