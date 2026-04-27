# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Repository shape

This repository is a Markdown content archive, not an application codebase.

- Source of truth: `22-25year/` monthly archive files.
- Retrieval helpers: `docs/indexes/` (`archive-index.md` plus `monthly/YYYY-MM.md`).
- Project overview: `README.md` explains the repo as a long-lived personal archive organized primarily by month.
- Index files accelerate lookup but are not authoritative; always verify final answers and edits against the source files in `22-25year/`.

The archive is organized around a two-step retrieval model:
1. use `docs/indexes/archive-index.md` to narrow to relevant months
2. use `docs/indexes/monthly/YYYY-MM.md` to find candidate articles
3. open the matching section in the source month file under `22-25year/`

## Content structure

Most source month files in `22-25year/` follow an older export-style structure:
- month heading near the top
- `[TOC]`
- repeated article blocks separated by `---`
- article headings like `## YYYY-MM-DD_金渐成_标题`
- article body followed by reader comments or interaction content

`22-25year/25-12月.md` uses a newer local structure instead of the older heading pattern. When working in that file, rely on nearby section markers such as:
- `# 标题`
- `**📅 发布日期**`
- `**🏷️ 标签**`
- `### 💬 评论区`

Do not assume one parsing pattern works for every month.

Some archive files may include mixed encodings or non-text bytes (`\0`). Verify the target file before making broad edits or assuming plain UTF-8-safe transformations.

## Working conventions

- Prefer editing existing monthly archive files rather than creating new organizational documents.
- Preserve the local formatting style of the target file.
- Locate content by exact article heading, publication date, or local section markers rather than approximate position.
- Be careful with bulk replacements: repeated phrases can appear in both article text and comment sections.
- If index content and source content disagree, trust `22-25year/` and then update indexes only if the task calls for it.

## Key files

- `22-25year/` — authoritative monthly archive files
- `docs/indexes/archive-index.md` — month-level summary index
- `docs/indexes/monthly/` — per-month article indexes used for faster lookup
- `docs/indexes/README.md` — retrieval workflow and index maintenance rules
- `README.md` — high-level repository description

## Commands

There is no build, lint, or test workflow in this repository.

Common inspection commands:
- `ls -la /Users/johnny/Documents/jjc-money/22-25year`
- `ls -la /Users/johnny/Documents/jjc-money/docs/indexes`
- `file /Users/johnny/Documents/jjc-money/22-25year/*`

Find source articles by heading/date:
- `rg "^## " /Users/johnny/Documents/jjc-money/22-25year/*.md`
- `rg "2025-11|关键词" /Users/johnny/Documents/jjc-money/22-25year/*.md`

Use indexes first, then verify in source:
- `rg "2025-11|关键词" /Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md /Users/johnny/Documents/jjc-money/docs/indexes/monthly/*.md`
- `rg "^## |^# |^\*\*📅 发布日期\*\*" /Users/johnny/Documents/jjc-money/22-25year/25-12月.md`

Single-file inspection examples:
- `rg "英伟达|NVDA" /Users/johnny/Documents/jjc-money/22-25year/2025-11\(共9篇\).md`
- `rg "英伟达|NVDA" /Users/johnny/Documents/jjc-money/docs/indexes/monthly/2025-11.md`

## Index maintenance model

When work involves adding or refreshing archive indexes:
1. update the relevant file in `docs/indexes/monthly/`
2. update `docs/indexes/archive-index.md`
3. keep article counts, links, date ranges, and source locators aligned with the source month file

For `2025-12`, index entries should use the local structure from `22-25year/25-12月.md` rather than assuming older `## YYYY-MM-DD_金渐成_标题` headings.
