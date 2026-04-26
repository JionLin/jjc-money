---
name: wechat-monthly-archive
description: Extract WeChat Official Account articles and visible comment sections from the user's screen, then write them into a monthly Markdown archive file that matches an existing month template such as 2026-03.md. Use when the user wants to continue a monthly archive, backfill a date range, or convert公众号文章+评论区 into a local month file.
---

# WeChat Monthly Archive

Use this skill when the user wants to archive a date range of WeChat Official Account posts, including the visible comment section, into a monthly Markdown file that follows an existing local month file.

This repository is a Markdown archive, so the output file is the deliverable.

## Inputs To Confirm

Collect these from the user request or infer them from nearby files:

- Source month template file, usually the previous month such as `26year/2026-03.md`
- Target output file, such as `26year/2026-04.md`
- Date range, for example `2026-04-07` through today
- Source surface, usually the WeChat window visible on the user's screen
- Whether the user wants a dry run, description only, or full execution

If a key input is missing and the risk is material, ask one focused question. Otherwise make the smallest reasonable assumption and state it after the work.

## Default Workflow

1. Read the repository instructions and inspect the template month file first.
2. Identify the exact output pattern used by the template month file:
   - article title line
   - publication date line
   - tag line
   - body formatting
   - `### 留言区`
   - reader comments, author replies, pinned notes, and visible moderation markers
   - article separators such as `---`
3. Open the target month file and inspect whether it is empty, a placeholder, or already partially filled.
4. Use the visible WeChat interface to locate all articles in the requested date range.
5. Process articles in chronological order unless the user explicitly wants another order.
6. For each article, capture:
   - title
   - publication date
   - visible tags if shown
   - full visible article body
   - visible comment section
   - author replies
   - pinned comments
   - markers such as deleted,违规,不可见, or only-visible-to-author style notices if they are visible on screen
7. Write the article into the target month file using the template month structure.
8. After finishing all articles, verify coverage, order, and formatting against the date range and the template file.

## Extraction Rules

- Trust the visible source on screen over memory.
- Preserve the local formatting style of the template month file.
- Keep article content and comment content separate.
- Do not summarize when the user asked for archival output.
- Do not invent tags, comments, or missing text.
- If a comment thread is only partially visible and cannot be loaded further, note that it is partial instead of pretending it is complete.
- If an article has no visible comments, still include the `### 留言区` section and note that no visible comments were available.
- Keep the original language and tone of the content.
- Preserve meaningful markers like `*(作者置顶)*`, `**✍️ 作者回复**：`, or visible moderation notices when present in the source.

## Output Shape

Match the local month template. In this repository, a common target shape is:

```md
# 文章标题

**📅 发布日期**：2026年04月07日
**🏷️ 标签**：#标签1 #标签2

正文段落...

### 留言区

**读者 (昵称)**：
> 评论内容
> **✍️ 作者回复**：
> 回复内容

---
```

If the template month uses a different structure, follow the template instead of this example.

## Comment Handling

- Keep reader names exactly as shown.
- Keep author identity labels exactly as shown if the template uses a standard label.
- Keep quoting structure consistent inside one file.
- If a reply is visible, nest it in the same comment block using the template's pattern.
- If the platform shows a moderation placeholder, copy the placeholder text instead of guessing the hidden content.

## Validation Checklist

Before finishing, confirm:

- The article date range starts and ends at the requested boundaries.
- No requested article in the visible list was skipped.
- Articles are in the intended order.
- Every article has title, date, body, and `### 留言区`.
- Formatting matches the template month file closely enough to sit beside it naturally.
- The target file does not accidentally overwrite unrelated existing content.

## Safe Working Pattern

- If the user says "先不急着操作", stop after describing the requested output and proposed checklist.
- If the target file already contains content, inspect it before deciding whether to append, merge, or replace.
- Prefer incremental saves when collecting many articles.
- When using screen automation, move article by article and verify after each paste-heavy step.

## Suggested Execution Checklist

Use this compact checklist during live runs:

1. Read the template month file.
2. Read the target month file.
3. Confirm date range.
4. Count candidate articles in WeChat.
5. Extract each article body.
6. Extract each article comment section.
7. Normalize into the month template format.
8. Write or append to the target file.
9. Spot-check at least one early article and one late article.
10. Report what was captured, any gaps, and any assumptions.
