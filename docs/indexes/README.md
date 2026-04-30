# Archive Indexes

## Purpose

This directory is an optional overview layer. It is not the source of truth, and it is no longer part of the default monthly maintenance workflow. Answers must still be verified against the source files in `22-25year/` and `26year/`.

## Structure

- `archive-index.md`: optional top-level month overview

## Archive overview fields

Each month entry must include:
- source file name
- date range
- base topic keywords

## Retrieval workflow

1. Search raw source files under `22-25year/` and `26year/`.
2. Optionally search `archive-index.md` to narrow candidate months if it is available and useful.
3. Read matched sections from the source archive file.
4. Answer with a short conclusion followed by source-backed evidence.

## Special cases

`22-25year/25-12月.md` does not follow the older `## YYYY-MM-DD_金渐成_标题` pattern consistently. Use the local structure in that file, such as publication date and section title.

## Maintenance

When a new source month is added:
1. update the source file only as part of normal maintenance
2. update `archive-index.md` only if a task explicitly asks for overview refresh
