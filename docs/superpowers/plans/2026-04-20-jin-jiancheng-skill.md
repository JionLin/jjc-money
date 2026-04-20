# 金渐成人物 Skill Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 仅基于 `22-25year/` 与 `26year/` 的本地语料，先产出“金渐成”人物内核小样，再在用户确认后生成可运行的 `.claude/skills/jin-jiancheng-perspective/SKILL.md`。

**Architecture:** 实施分两段。第一段从月度归档中按“正文 + 作者评论回复”提炼心智模型、决策启发式、表达 DNA、反模式与诚实边界，并在对话中提交小样确认。第二段把已确认的小样组装成单文件 Skill，并把“引用历史材料默认从近到远排序”固化进工作流与输出规则。

**Tech Stack:** Markdown 文件、仓库本地语料、Claude Code 读写工具、`rg`/`git` 只读命令

---

## File Structure

### Existing files to read
- `docs/superpowers/specs/2026-04-20-jin-jiancheng-skill-design.md` — 已确认的设计约束
- `22-25year/*.md` — 2022-2025 月度归档主语料
- `26year/*.md` — 2026 月度归档补充语料
- `CLAUDE.md` — 归档结构与检索约束

### Files to create
- `.claude/skills/jin-jiancheng-perspective/SKILL.md` — 最终人物 Skill

### Files to modify
- 无。实施以新增 Skill 文件为主。

### Execution notes
- 不创建额外研究目录，不下载外部资料。
- 小样摘要在对话里展示，不单独落盘。
- 如实现过程中发现 `.claude/skills/` 不存在，直接创建所需目录即可。

---

### Task 1: Re-read the spec and lock extraction rules

**Files:**
- Read: `docs/superpowers/specs/2026-04-20-jin-jiancheng-skill-design.md`
- Read: `CLAUDE.md`

- [ ] **Step 1: Read the confirmed spec and restate the mandatory rules**

Read these files and extract the non-negotiables into working memory:

```text
- corpus is local-only: 22-25year/ + 26year/
- author replies in comment sections are first-class evidence
- reader comments are context only
- default historical ordering is newest → oldest
- output sample first, write SKILL.md only after approval
```

- [ ] **Step 2: Verify the repository shape before reading content**

Run:

```bash
ls "22-25year" && ls "26year"
```

Expected: both directories list monthly Markdown files, including `25-12月.md` and the 2026 files.

- [ ] **Step 3: Confirm the structural parsing rules for old and new formats**

Use these rules during extraction:

```text
old format anchors:
- ## YYYY-MM-DD_金渐成_标题
- article body
- trailing comments / interaction block

new format anchors:
- # 标题
- **📅 发布日期**
- **🏷️ 标签**
- ### 💬 评论区
```

- [ ] **Step 4: Commit the planning checkpoint**

```bash
git add docs/superpowers/plans/2026-04-20-jin-jiancheng-skill.md
git commit -m "docs: add jin jiancheng skill implementation plan"
```

---

### Task 2: Build a reading set from newest to oldest

**Files:**
- Read: `26year/2026-03.md`
- Read: `26year/26-02月.md`
- Read: `26year/26-01.md`
- Read: `22-25year/25-12月.md`
- Read: `22-25year/2025-11(共9篇).md`
- Read: `22-25year/2025-10(共22篇).md`
- Read: `22-25year/2025-09(共12篇).md`
- Read: `22-25year/2025-08(共9篇).md`
- Read: `22-25year/2025-07(共7篇).md`
- Read: `22-25year/2025-06(共18篇).md`
- Read: `22-25year/2025-05(共17篇).md`
- Read: `22-25year/2025-04(共15篇).md`
- Read: `22-25year/2025-03(共19篇).md`
- Read: `22-25year/2025-02(共14篇).md`
- Read: `22-25year/2025-01(共16篇).md`
- Read: `22-25year/2024-12(共18篇).md`
- Read: `22-25year/2024-11(共21篇).md`
- Read: `22-25year/2024-10(共19篇).md`
- Read: `22-25year/2024-09(共16篇).md`
- Read: `22-25year/2024-08(共15篇).md`
- Read: `22-25year/2024-06(共11篇).md`
- Read: `22-25year/2024-05(共6篇).md`
- Read: `22-25year/2024-04(共9篇).md`
- Read: `22-25year/2024-03(共3篇).md`
- Read: `22-25year/2024-02(共2篇).md`
- Read: `22-25year/2024-01(共4篇).md`
- Read: `22-25year/2023-12(共5篇).md`
- Read: `22-25year/2023-11(共2篇).md`
- Read: `22-25year/2023-09(共9篇).md`
- Read: `22-25year/2023-08(共19篇).md`
- Read: `22-25year/2023-07(共10篇).md`
- Read: `22-25year/2022-12(共1篇).md`
- Read: `22-25year/2022-11(共8篇).md`

- [ ] **Step 1: Start with the most recent files to capture the latest stable voice**

Read in this order first:

```text
26year/2026-03.md
26year/26-02月.md
26year/26-01.md
22-25year/25-12月.md
22-25year/2025-11(共9篇).md
22-25year/2025-10(共22篇).md
```

Record recurring themes, repeated argument shapes, and notable comment-reply patterns.

- [ ] **Step 2: Expand backward through 2025 to test whether patterns are stable**

Read the remaining 2025 files from newest to oldest and classify every candidate finding into one of these buckets:

```text
A. candidate mental model
B. candidate decision heuristic
C. candidate expression-DNA pattern
D. candidate anti-pattern
E. single-use observation (do not elevate yet)
```

- [ ] **Step 3: Read 2024-2022 files to validate cross-period recurrence**

Only promote a pattern if it survives older material checks. Use this rule:

```text
appears in recent files only -> keep provisional
appears in recent + older files -> eligible for stable model
appears once only -> keep as example or discard
```

- [ ] **Step 4: Use repo search to find likely comment sections and author replies quickly**

Run:

```bash
rg "评论区|💬 评论区|回复|作者|金渐成" "22-25year" "26year"
```

Expected: hits across multiple month files that help locate interactive sections without scanning every line manually.

- [ ] **Step 5: Commit the reading-set checkpoint**

```bash
git add docs/superpowers/plans/2026-04-20-jin-jiancheng-skill.md
git commit -m "docs: document corpus-reading workflow for jin jiancheng skill"
```

---

### Task 3: Extract the small sample before writing any skill file

**Files:**
- Read: `22-25year/*.md`
- Read: `26year/*.md`
- Create later: `.claude/skills/jin-jiancheng-perspective/SKILL.md`

- [ ] **Step 1: Draft 3-5 mental models from repeated evidence only**

Use this output shape in the conversation:

```markdown
## 心智模型
1. 名称
   - 核心判断：...
   - 重复证据：2026-03 正文；2025-12 评论回复；2024-10 正文
   - 为什么不是偶发观点：...
```

Reject any model that cannot point to multiple months.

- [ ] **Step 2: Draft 5-8 decision heuristics with evidence**

Use this output shape:

```markdown
## 决策启发式
1. 当遇到 X 时，优先看 Y
   - 证据：2025-11 评论回复；2025-04 正文
```

Prefer patterns that appear in both essays and replies.

- [ ] **Step 3: Draft the expression DNA using comment replies as primary evidence**

Use this output shape:

```markdown
## 表达 DNA
- 常见句法：...
- 常见转折：...
- 常用动作：定义 / 拆概念 / 反问 / 举例 / 立边界
- 证据：2026-01 评论回复；2025-08 正文
```

- [ ] **Step 4: Draft anti-patterns and honest boundaries**

Use this output shape:

```markdown
## 反模式
- 反对的表达 / 思维：...
- 证据：...

## 诚实边界
- 只基于仓库语料
- 评论回复受问题语境约束
- 单次表达不代表长期立场
- 资料截止到当前 26year/
```

- [ ] **Step 5: Present the sample in the conversation and stop for approval**

Do **not** create `.claude/skills/jin-jiancheng-perspective/SKILL.md` yet. The conversation message should include all five sections below in this order:

```text
1. 心智模型
2. 决策启发式
3. 表达 DNA
4. 反模式
5. 诚实边界
```

- [ ] **Step 6: Commit the sample checkpoint plan note**

```bash
git add docs/superpowers/plans/2026-04-20-jin-jiancheng-skill.md
git commit -m "docs: record sample-first workflow for jin jiancheng skill"
```

---

### Task 4: Write the final SKILL.md after sample approval

**Files:**
- Create: `.claude/skills/jin-jiancheng-perspective/SKILL.md`
- Read: `docs/superpowers/specs/2026-04-20-jin-jiancheng-skill-design.md`

- [ ] **Step 1: Create the skill directory and start the skill file**

Create this exact skeleton:

```markdown
---
name: jin-jiancheng-perspective
description: Use when the user wants answers in the style of 金渐成, or wants to interpret ideas, choices, and archived articles through 金渐成’s recurring judgment patterns based on the local repository corpus.
---

# 金渐成 Perspective
```

- [ ] **Step 2: Write the role and workflow rules**

Insert these sections near the top of the file:

```markdown
## 角色规则

- 只基于当前仓库 `22-25year/` 与 `26year/` 的可见语料行事
- 不伪造“金渐成一定会怎么想”
- 需要引用历史材料时，默认按时间从近到远组织
- 读者评论只作上下文，作者回复才可作为观点证据

## 回答工作流

1. 先判断问题是抽象框架题，还是需要回读本地材料的问题。
2. 如果需要引用本地材料，先找最新月份，再逐步回溯更早月份。
3. 回答时先给最新材料，再补更早材料。
4. 如果新旧观点存在变化，显式说明演变，不抹平差异。
5. 如果语料不足，直接说明边界。
```

- [ ] **Step 3: Write the approved mental models and heuristics verbatim from the sample**

Use this section layout:

```markdown
## 心智模型
### 1. [模型名]
- 核心判断：...
- 证据类型：正文 / 评论回复
- 适用方式：...
- 局限：...

## 决策启发式
- 当遇到 ... 时，优先看 ...
- 当被质疑 ... 时，会先 ...
```

Use only the user-approved sample content. Do not add new speculative models at this stage.

- [ ] **Step 4: Write the expression DNA, anti-patterns, and boundaries**

Use this exact structure:

```markdown
## 表达 DNA
- 常见句法：...
- 常见节奏：...
- 常见动作：...
- 面对异议时：...

## 反模式
- 不接受：...
- 常批评：...
- 常划清界限：...

## 诚实边界
- 本 Skill 不是金渐成本人，只是基于本地归档提炼出的认知代理。
- 语料范围仅限 `22-25year/` 与 `26year/`。
- 评论区作者回复是重要材料，但受具体提问语境限制。
- 历史材料默认按从近到远引用；用户另有要求时除外。
```

- [ ] **Step 5: Add the usage rule for historical ordering explicitly**

Add a dedicated section with this content:

```markdown
## 时间排序规则

当回答涉及历史文章、评论区作者回复、同主题观点演变或多篇材料比较时，默认按时间从近到远组织：先给最新表达，再回溯更早材料。只有用户明确要求追溯源头、按正序梳理、或优先看最早观点时，才改为从远到近。
```

- [ ] **Step 6: Verify the file reads as one coherent skill**

Run:

```bash
python - <<'PY'
from pathlib import Path
p = Path('.claude/skills/jin-jiancheng-perspective/SKILL.md')
text = p.read_text(encoding='utf-8')
for token in ['TODO', 'TBD', '类似', '待补', '之后补充']:
    assert token not in text, token
print('PASS: skill file has no placeholders')
PY
```

Expected: `PASS: skill file has no placeholders`

- [ ] **Step 7: Commit the skill**

```bash
git add .claude/skills/jin-jiancheng-perspective/SKILL.md
git commit -m "feat: add jin jiancheng perspective skill"
```

---

### Task 5: Verify the skill behavior against the approved constraints

**Files:**
- Read: `.claude/skills/jin-jiancheng-perspective/SKILL.md`
- Read: `docs/superpowers/specs/2026-04-20-jin-jiancheng-skill-design.md`

- [ ] **Step 1: Check spec coverage manually**

Verify each item below is implemented in `SKILL.md`:

```text
local-only corpus
author replies treated as important material
reader comments treated as context only
newest-to-oldest historical ordering
sample-approved content only
honest boundaries present
```

- [ ] **Step 2: Run a text-level verification for the key rules**

Run:

```bash
python - <<'PY'
from pathlib import Path
text = Path('.claude/skills/jin-jiancheng-perspective/SKILL.md').read_text(encoding='utf-8')
checks = {
    '22-25year': '22-25year/' in text or '22-25year' in text,
    '26year': '26year/' in text or '26year' in text,
    '排序规则': '从近到远' in text,
    '作者回复': '作者回复' in text,
    '读者评论': '读者评论' in text,
    '诚实边界': '诚实边界' in text,
}
for k, v in checks.items():
    print(f'{k}: {"PASS" if v else "FAIL"}')
assert all(checks.values())
PY
```

Expected: every line prints `PASS` and the command exits cleanly.

- [ ] **Step 3: Run a dry review prompt against the skill manually**

Use these two prompts as the review standard:

```text
A. “按金渐成的方式看待一个抽象问题：为什么普通人总在信息很多时判断更差？”
B. “回顾仓库里关于同一主题的历史表达，按时间从近到远梳理，并区分正文与评论区作者回复。”
```

Expected behavior:

```text
A should answer using distilled models without pretending to quote missing sources.
B should explicitly prefer newest → oldest and keep comments/replies separated correctly.
```

- [ ] **Step 4: Commit the verification checkpoint**

```bash
git add .claude/skills/jin-jiancheng-perspective/SKILL.md docs/superpowers/plans/2026-04-20-jin-jiancheng-skill.md
git commit -m "docs: verify jin jiancheng perspective skill constraints"
```

---

## Self-review

### Spec coverage
- Local-only corpus: covered by Tasks 1, 2, 4, 5
- Author replies as important material: covered by Tasks 2, 3, 4, 5
- Reader comments as context only: covered by Tasks 1, 4, 5
- Sample before final skill: covered by Task 3
- Final skill path: covered by Task 4
- Newest-to-oldest ordering in later answers: covered by Tasks 3, 4, 5
- Honest boundaries: covered by Tasks 3, 4, 5

### Placeholder scan
- No `TODO`, `TBD`, “implement later”, or “similar to Task N” placeholders were left in the plan.

### Type consistency
- Final skill path is consistently `.claude/skills/jin-jiancheng-perspective/SKILL.md`
- The ordering rule is consistently described as “从近到远”
- Comment handling is consistently split into “作者回复” vs “读者评论”
