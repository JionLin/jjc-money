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
