# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Repository shape

This repository is a Markdown content archive, not an application codebase.

- Source of truth: `22-25year/` monthly archive files.
- Project overview: `README.md` explains the repo as a long-lived personal archive organized primarily by month.
- Always verify final answers and edits against the source files in `22-25year/` or `26year/`.

The archive is organized around a source-first retrieval model:
1. search raw source files under `22-25year/` and `26year/`
2. locate candidate articles by heading, publication date, or local section markers
3. verify final claims in the matched source section

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
- Trust the source files if any derived notes or older docs disagree.

## Key files

- `22-25year/` — authoritative monthly archive files
- `README.md` — high-level repository description

## Commands

There is no build, lint, or test workflow in this repository.

Common inspection commands:
- `ls -la /Users/johnny/Documents/jjc-money/22-25year`
- `file /Users/johnny/Documents/jjc-money/22-25year/*`
- `file /Users/johnny/Documents/jjc-money/26year/*`

Find source articles by heading/date:
- `rg "^## " /Users/johnny/Documents/jjc-money/22-25year/*.md`
- `rg "2025-11|关键词" /Users/johnny/Documents/jjc-money/22-25year/*.md`

- `rg "^## |^# |^\*\*📅 发布日期\*\*" /Users/johnny/Documents/jjc-money/22-25year/25-12月.md /Users/johnny/Documents/jjc-money/26year/*.md`

Single-file inspection examples:
- `rg "英伟达|NVDA" /Users/johnny/Documents/jjc-money/22-25year/2025-11\(共9篇\).md`
- `rg "英伟达|NVDA" /Users/johnny/Documents/jjc-money/26year/2026-04.md`

For `2025-12` and `26year/` files, rely on local section markers and publication dates rather than assuming older `## YYYY-MM-DD_金渐成_标题` headings.
