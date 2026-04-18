# README Design for jjc-money

## Goal
Create a bilingual README for the repository that works for both public GitHub presentation and lightweight self-maintenance, with stronger emphasis on presenting the repository as a personal content collection.

## Audience
- Chinese-speaking visitors who arrive at the GitHub repository page
- English-speaking visitors who need a concise orientation
- The repository owner when returning later to understand structure and maintenance expectations

## Recommended approach
Use a balanced README with Chinese-first bilingual structure:
1. Chinese primary section for clear presentation and context
2. A concise structural overview of the repository
3. Lightweight maintenance guidance
4. A shorter English section at the end

This keeps the homepage readable while still making the archive maintainable.

## README structure

### 1. Header and positioning
- Repository name: `jjc-money`
- One-sentence description identifying it as a Markdown archive of "金渐成" related content
- Short note explaining that the repository preserves a long-term body of writing and related material

### 2. Repository overview
The README should frame the repository primarily as a personal content collection rather than a technical dataset.

Key points:
- This is a long-term archive of personal articles and related content
- Content is mainly organized by month
- The primary archive lives in `22-25year/`
- The tone should feel personal and clear, not overly technical

### 3. Repository structure
Include a short directory overview covering:
- `22-25year/` for monthly Markdown archives
- `docs/indexes/` for indexes and supporting documentation
- `CLAUDE.md` for repository collaboration guidance

Keep this section concise and scannable.

### 4. Content format
Explain that monthly archive files generally contain:
- A monthly heading
- Multiple article sections
- Comment or interaction content

Also note that formatting is not fully uniform across different periods.

### 5. Maintenance notes
Keep this section lightweight.

It should say that:
- New content should follow the existing archive style
- Edits should preserve the surrounding formatting pattern
- Articles are best located by date or title

### 6. English section
Add a short English summary at the end covering:
- What the repository is
- How the content is organized
- Where a visitor should start browsing

The English section should be shorter than the Chinese section.

## Tone and style
- Chinese-first, English-second
- Clear and personal rather than formal or technical
- Concise enough for a repository homepage
- Avoid unnecessary implementation details or long operational instructions

## Scope boundaries
The README should not try to become full documentation.
It should not duplicate the detailed archive-handling guidance already present in `CLAUDE.md`.
It should orient readers, explain structure, and provide minimal maintenance context only.
