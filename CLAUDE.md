# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository shape

This is a Markdown content archive, not an application codebase.

- Source of truth: `22-25year/` monthly archive files.
- Retrieval helpers: `docs/indexes/` (`archive-index.md` + `monthly/YYYY-MM.md`).
- Index files accelerate lookup but are not authoritative; always verify against `22-25year/` before final answers/edits.

Most monthly files follow:
- month heading
- `[TOC]`
- repeated article blocks separated by `---`
- article headings like `## YYYY-MM-DD_金渐成_标题`
- article body plus comments

`22-25year/25-12月.md` is a newer format with local metadata/section conventions (`📅 发布日期`, `🏷️ 标签`, `💬 评论区`) and should be handled by nearby local structure, not old heading assumptions.

Some monthly files may include mixed encodings or non-text bytes (`\0`). Verify target files before assuming plain UTF-8 edits.

## Working conventions

- Prefer editing existing monthly archive files; do not add new organizational docs unless asked.
- Preserve local formatting style in the target file.
- Locate/edit by exact article heading (or local section structure in `25-12月.md`), not approximate position.
- Be cautious with bulk replacements because repeated phrases appear in both article and comment content.

## Useful commands

No build/lint/test workflow exists in this repository.

Inspection:
- `ls -la /Users/johnny/Documents/jjc-money/22-25year`
- `ls -la /Users/johnny/Documents/jjc-money/docs/indexes`
- `file /Users/johnny/Documents/jjc-money/22-25year/*`

Retrieval:
- `rg "^## " /Users/johnny/Documents/jjc-money/22-25year/*.md`
- `rg "2025-11|关键词" /Users/johnny/Documents/jjc-money/docs/indexes/archive-index.md /Users/johnny/Documents/jjc-money/docs/indexes/monthly/*.md`
