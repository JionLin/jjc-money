# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository shape

This repository is a Markdown content archive, not an application codebase. The primary working directory is `22-25year/`, which contains monthly aggregate files such as `2024-10(共19篇).md` and `2025-11(共9篇).md`, plus `25-12月.md` for December 2025 content.

Each file generally represents one month of posts and is structured as:
- a top-level monthly heading like `# 2025-11 文章汇总 (共9篇)`
- `[TOC]`
- repeated article sections separated by `---`
- article headings in the form `## YYYY-MM-DD_金渐成_标题`
- article body text followed by reader comments

The newer `25-12月.md` file uses a different, more presentation-oriented format with metadata blocks such as `**📅 发布日期**`, `**🏷️ 标签**`, and `### 💬 评论区`.

Several monthly files contain non-text bytes or mixed encodings. Tools may report them as `data`, and regex searches can stop early with `"found \"\\0\" byte"`. When editing, verify the target file carefully before assuming it is plain UTF-8 text.

## Working conventions

- Prefer updating existing monthly archive files instead of creating new organizational documents unless explicitly asked.
- Preserve the existing per-file formatting style. Older monthly files are compact raw exports; `25-12月.md` is manually formatted and should keep its richer Markdown structure.
- When extracting or editing an article, identify it by its `## YYYY-MM-DD_金渐成_标题` section heading rather than by approximate position in the file.
- Be careful with bulk search/replace across the archive because article bodies and comment sections can contain repeated phrases.

## Useful commands

There is no build, lint, or test workflow in this repository.

Useful inspection commands are simple filesystem checks, for example:
- `ls -la /Users/johnny/Documents/jjc-money/22-25year`
- `file /Users/johnny/Documents/jjc-money/22-25year/*`

Useful content inspection patterns:
- search headings in monthly archives with a Markdown heading regex
- search for a target article date/title before editing a specific section

## Architecture notes

The important structure here is content-oriented rather than code-oriented:
- `22-25year/` is the archive root.
- One file usually maps to one month of exported posts.
- Each monthly file contains multiple articles plus appended comment threads.
- File naming encodes chronology and article counts, which is important context when adding, splitting, or validating content.
- Formatting is not fully uniform across time, so future edits should inspect nearby content first and match the local pattern instead of assuming a single global template.
