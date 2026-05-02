# jjc-money

> **金渐成** — 财富逐步积累，乃至功成。  
> 不贪快、不贪多，顺势而为，认知变现，知行合一，方能渐成。

一个以「金渐成」公众号 300+ 篇文章为原始语料的 **个人投资知识归档 + AI 辅助决策系统**。

从 2022 年 11 月的第一篇房地产分析，到 2026 年 4 月的全球多元配置路线图，4 年文字跨度覆盖了美股七巨头实战、仓位管理方法论、宏观经济解读、人生哲学与育儿等七大版块。仓库不仅仅是存档——它还包含一套可运行的 AI 技能（`SKILL.md`）和 Spring AI 后端，用于将作者的投资框架转化为可交互的决策辅助工具。

---

## 项目全景

```
jjc-money/
├── 22-25year/          # 📦 核心归档：2022.11 → 2025.12，30 个月度 Markdown 文件
├── 26year/             # 📦 2026 年归档：01-04 月 + 全球市场展望
├── docs/               # 📊 衍生文档层
│   ├── {TICKER}/       #     按标的分类的深度研判报告 (AAPL/AMZN/BRK/GOOGL/META/MSFT/NVDA/TSM)
│   ├── PORTFOLIO/      #     个人组合卡片、调仓计划
│   ├── SYSTEM/         #     知识图谱、技能指南、数据源参考、个人快照
│   ├── topology-details/ #   知识拓扑细节（美股实战、仓位管理）
│   └── indexes/        #     索引目录（预留）
├── .agent/skills/      # 🧠 AI 技能
│   └── jinjian-perspective/  SKILL.md (v2.5.2) — 金渐成投资视角核心提示词
├── .agent/workflows/   # 🔄 OpenSpec 工作流（propose/apply/explore/archive）
├── spring-ai-lab/      # ☕ Spring AI 后端（Spring Boot 3.4 + DeepSeek）
├── openspec/           # 📋 OpenSpec 变更管理
├── version/            # 📐 架构设计文档（v1 草案/终稿）
├── AGENTS.md           # 🤖 AI Agent 工作指引（Codex/Claude/Cursor 等）
└── README.md           # 📖 本文件 — 统一入口
```

---

## 数据规模速览

| 指标 | 数值 |
|------|------|
| 总文章数 | **340+** 篇（含评论区互动） |
| 月度归档文件 | **35** 个（`22-25year/` 30 + `26year/` 5） |
| 原始文字量 | **~89,000 行 / 6.9 MB** |
| 时间跨度 | 2022-11 → 2026-04（持续更新） |
| 深度研判报告 | **8 个标的** 共 14+ 份 |
| AI 技能版本 | `SKILL.md` v2.5.2 |

---

## 核心内容七大版块

基于全量文本的聚类分析，4 年文章可归纳为以下版块（详见 [知识图谱拓扑](docs/SYSTEM/jinjiancheng-knowledge-topology.md)）：

| 版块 | 权重 | 说明 |
|------|:----:|------|
| 🏛️ **A — 美股投资实战** | ~35% | 七巨头 + 台积电/博通/AMD；消费/医药/伯克希尔；ETF/期权 |
| 🏠 **B — 中国房地产** | ~20% | 房企暴雷预测、卖房实操、房价走势（2022-2023 核心，后期收缩） |
| 💰 **C — 仓位管理方法论** | ~15% | 2-3-3-2 法、负成本/印钞机、金字塔、做 T、三账户体系 |
| 📊 **D — 宏观经济** | ~10% | 美联储利率周期、关税/贸易战、2026 全球路线图、日历风险 |
| 🧠 **E — 人生哲学** | ~10% | 认知变现、知行合一、婚姻观、阶层流动、健康管理 |
| 👨‍👧‍👦 **F — 育儿教育** | ~5% | 亲子投资教学、子女自驱力、留学规划 |
| 🐱 **G — 个人生活** | ~5% | 养猫、山居生活、酱酒收藏、公众号运营 |

**内容重心迁移轨迹**：  
房地产从业者视角（2022-2023）→ 美股实战投资者（2024）→ 全球多元化配置者（2025-2026）

---

## 模块详解

### 📦 核心归档（`22-25year/` + `26year/`）

这是整个项目的 **唯一权威数据源**。所有衍生文档、知识图谱、AI 技能的事实依据，最终都回溯到这里。

- **旧格式**（`22-25year/` 大部分文件）：`## YYYY-MM-DD_金渐成_标题` 标题 → 正文 → 评论区，以 `---` 分隔
- **新格式**（`25-12月.md` 及 `26year/`）：`# 标题` → `**📅 发布日期**` → `**🏷️ 标签**` → `### 💬 评论区`

### 📊 衍生文档层（`docs/`）

| 子目录 | 用途 |
|--------|------|
| `{TICKER}/` | 按标的分类的深度研判报告（如 `NVDA/nvda-deep-analysis-20260424.md`） |
| `PORTFOLIO/` | 个人组合卡片、调仓计划、组合分析 |
| `SYSTEM/` | 知识图谱拓扑、技能提示词指南、数据源参考、个人快照 |
| `topology-details/` | A_美股投资实战、C_仓位管理与配置 — 知识图谱的垂直细节 |

> ⚠️ `docs/` 中的内容是 **衍生地图**，不是权威源。当衍生文档与 `22-25year/`、`26year/` 原始归档冲突时，以原始归档为准。

### 🧠 AI 技能（`.agent/skills/jinjian-perspective/`）

[SKILL.md](/.agent/skills/jinjian-perspective/SKILL.md)（v2.5.2）是从 300+ 篇文章中蒸馏出的 **投资决策框架提示词**，包含：

- **5 大心智模型**：顺势、激流缓退、负成本印钞机、创富→守富→传富、粪坑检测
- **决策启发式**：PE 估值快扫、2-3-3-2 建仓/减仓协议、赛道选择、情景应对表
- **表达 DNA**：深入浅出、嬉笑怒骂、犀利但不刻薄
- **执行 SOP**：6 种模式路由（Ticker / Portfolio / Personal / Macro / Historical / Life）
- **护栏系统**：Freshness Gate、证据层级、锚点优先级、诚实边界

配套文档：[skill-prompt-guide.md](docs/SYSTEM/skill-prompt-guide.md)

### ☕ Spring AI 后端（`spring-ai-lab/`）

基于 Spring Boot 3.4 + Spring AI 1.0 + DeepSeek（OpenAI 兼容接口）的决策辅助后端：

- Prompt 热加载（运行时可刷新 SKILL.md）
- Function Calling（本地归档检索）
- Resilience4j 熔断
- 单 JAR 部署

### 🤖 Agent 基础设施

| 文件 | 用途 |
|------|------|
| `AGENTS.md` | AI Agent 的工作指引（仓库结构、内容格式、检索范式、操作约定） |
| `.agent/workflows/` | OpenSpec 工作流（propose → apply → archive → explore） |
| `openspec/` | OpenSpec 变更配置与规格 |

---

## 快速检索指南

```bash
# 列出所有月度归档
ls -la 22-25year/ 26year/

# 按标题搜索文章
rg "^## " 22-25year/*.md

# 按关键词搜索特定标的
rg "英伟达|NVDA" 22-25year/*.md 26year/*.md

# 搜索 2026 年内容（新格式）
rg "^## |^# |^\*\*📅 发布日期\*\*" 22-25year/25-12月.md 26year/*.md

# 搜索特定文件
rg "估值|锚点|加仓|减仓" 26year/2026-04.md
```

---

## AI Agent 入口指引

如果你是 AI Agent 首次接触本仓库，按以下顺序建立上下文：

1. **本文件**（`README.md`）— 你正在读的全局地图
2. **[AGENTS.md](AGENTS.md)** — 操作约定、内容格式、检索命令
3. **[SKILL.md](.agent/skills/jinjian-perspective/SKILL.md)** — 核心投资决策框架（按需加载）
4. **[知识图谱拓扑](docs/SYSTEM/jinjiancheng-knowledge-topology.md)** — 七大版块分类与词频统计
5. **原始归档**（`22-25year/` + `26year/`）— 任何事实验证的最终权威

**检索范式**：source-first（原始归档优先）→ topology/deep-analysis 仅做导航地图 → 最终结论必须回溯到原始月度文件验证。

---

## 维护约定

- 新增内容时，延续现有月度归档方式，写入 `22-25year/` 或 `26year/`
- 编辑已有内容时，保持周边格式一致
- 修改 `.agent/skills/` 下的 SKILL.md 时，**必须同步更新** `docs/SYSTEM/skill-prompt-guide.md`
- 衍生文档（`docs/`）不得覆盖原始归档的事实
- 本仓库无 build/lint/test 流程

---

---

# jjc-money (English)

> **JinJianCheng (金渐成)** — Wealth accumulates gradually until it is complete.  
> No rushing, no greed. Ride the trend, monetize cognition, unite knowledge and action.

A **personal investment knowledge archive + AI-assisted decision system** built on 300+ articles from the WeChat public account "金渐成" by hedge-fund manager "金不换" (pen-name 天玑).

Spanning from November 2022 to April 2026, the archive covers US mega-cap stock operations, position management methodology, macroeconomic analysis, life philosophy, parenting, and more — organized into 7 major knowledge domains. Beyond archiving, the repo includes a runnable AI skill (`SKILL.md`) and a Spring AI backend that transforms the author's investment framework into an interactive decision-support tool.

---

## Project Overview

```
jjc-money/
├── 22-25year/          # 📦 Core archive: 2022.11 → 2025.12, 30 monthly Markdown files
├── 26year/             # 📦 2026 archive: Jan–Apr + Global Market Outlook
├── docs/               # 📊 Derived documents layer
│   ├── {TICKER}/       #     Deep analysis reports by ticker (AAPL/AMZN/BRK/GOOGL/META/MSFT/NVDA/TSM)
│   ├── PORTFOLIO/      #     Personal portfolio cards and rebalancing plans
│   ├── SYSTEM/         #     Knowledge topology, skill guide, data source reference, personal snapshot
│   ├── topology-details/ #   Detailed knowledge topology (US equities, position management)
│   └── indexes/        #     Index directory (reserved)
├── .agent/skills/      # 🧠 AI Skills
│   └── jinjian-perspective/  SKILL.md (v2.5.2) — core investment perspective prompt
├── .agent/workflows/   # 🔄 OpenSpec workflows (propose/apply/explore/archive)
├── spring-ai-lab/      # ☕ Spring AI backend (Spring Boot 3.4 + DeepSeek)
├── openspec/           # 📋 OpenSpec change management
├── version/            # 📐 Architecture design docs (v1 drafts/finals)
├── AGENTS.md           # 🤖 AI Agent working guide (for Codex/Claude/Cursor etc.)
└── README.md           # 📖 This file — unified entry point
```

---

## Data Scale at a Glance

| Metric | Value |
|--------|-------|
| Total articles | **340+** (including comment threads) |
| Monthly archive files | **35** (`22-25year/` 30 + `26year/` 5) |
| Raw text volume | **~89,000 lines / 6.9 MB** |
| Time span | 2022-11 → 2026-04 (continuously updated) |
| Deep analysis reports | **8 tickers**, 14+ reports |
| AI skill version | `SKILL.md` v2.5.2 |

---

## Seven Knowledge Domains

Based on full-text clustering analysis, 4 years of content fall into 7 domains (see [Knowledge Topology](docs/SYSTEM/jinjiancheng-knowledge-topology.md)):

| Domain | Weight | Description |
|--------|:------:|-------------|
| 🏛️ **A — US Stock Operations** | ~35% | Magnificent 7 + TSM/AVGO/AMD; consumer/pharma/Berkshire; ETFs/options |
| 🏠 **B — China Real Estate** | ~20% | Developer defaults, home-selling tactics, price trends (2022-2023 peak) |
| 💰 **C — Position Management** | ~15% | 2-3-3-2 protocol, negative-cost/printing press, pyramid, day-trading, three-account system |
| 📊 **D — Macro Economics** | ~10% | Fed rate cycles, tariffs/trade war, 2026 global roadmap, calendar risk |
| 🧠 **E — Life Philosophy** | ~10% | Cognition monetization, unity of knowledge and action, marriage, class mobility |
| 👨‍👧‍👦 **F — Parenting & Education** | ~5% | Parent-child investment lessons, self-driven learning, study abroad |
| 🐱 **G — Personal Life** | ~5% | Cat raising, mountain living, baijiu collection, content creator operations |

**Content gravity shift:**  
Real estate practitioner (2022-2023) → US stock operator (2024) → Global multi-asset allocator (2025-2026)

---

## Module Details

### 📦 Core Archive (`22-25year/` + `26year/`)

The **single source of truth** for the entire project. All derived docs, topology maps, and AI skills trace back here for factual verification.

- **Legacy format** (most files in `22-25year/`): `## YYYY-MM-DD_金渐成_Title` heading → body → comments, separated by `---`
- **New format** (`25-12月.md` and `26year/`): `# Title` → `**📅 Published**` → `**🏷️ Tags**` → `### 💬 Comments`

### 📊 Derived Documents (`docs/`)

| Subdirectory | Purpose |
|-------------|---------|
| `{TICKER}/` | Per-ticker deep analysis reports (e.g. `NVDA/nvda-deep-analysis-20260424.md`) |
| `PORTFOLIO/` | Personal portfolio cards, rebalancing plans, allocation analysis |
| `SYSTEM/` | Knowledge topology, skill prompt guide, data source reference, personal snapshot |
| `topology-details/` | Vertical deep-dives into US equities and position management |

> ⚠️ Documents in `docs/` are **derived maps**, not authoritative sources. When they conflict with raw archives in `22-25year/` or `26year/`, the raw archives take precedence.

### 🧠 AI Skill (`.agent/skills/jinjian-perspective/`)

[SKILL.md](/.agent/skills/jinjian-perspective/SKILL.md) (v2.5.2) is the **investment decision framework prompt** distilled from 300+ articles:

- **5 Mental Models**: Ride the Trend, Gradual Exit, Cost-Negative Printing Press, Wealth Cascade, Cesspit Detector
- **Decision Heuristics**: PE valuation scan, 2-3-3-2 sizing protocol, sector selection, scenario playbook
- **Expression DNA**: Accessible, witty, incisive but not harsh
- **Execution SOP**: 6 mode routers (Ticker / Portfolio / Personal / Macro / Historical / Life)
- **Guardrail System**: Freshness Gate, evidence hierarchy, anchor priority, honesty boundaries

Companion doc: [skill-prompt-guide.md](docs/SYSTEM/skill-prompt-guide.md)

### ☕ Spring AI Backend (`spring-ai-lab/`)

Decision-support backend built on Spring Boot 3.4 + Spring AI 1.0 + DeepSeek (OpenAI-compatible):

- Runtime prompt hot-reload (SKILL.md refresh without restart)
- Function calling (local archive retrieval)
- Resilience4j circuit breaker
- Single-JAR deployment

### 🤖 Agent Infrastructure

| File | Purpose |
|------|---------|
| `AGENTS.md` | AI Agent working guide (repo structure, content formats, retrieval patterns, conventions) |
| `.agent/workflows/` | OpenSpec workflows (propose → apply → archive → explore) |
| `openspec/` | OpenSpec change config and specifications |

---

## Quick Retrieval Guide

```bash
# List all monthly archives
ls -la 22-25year/ 26year/

# Search articles by heading
rg "^## " 22-25year/*.md

# Search for specific tickers
rg "英伟达|NVDA" 22-25year/*.md 26year/*.md

# Search 2026 content (new format)
rg "^## |^# |^\*\*📅 发布日期\*\*" 22-25year/25-12月.md 26year/*.md

# Search within a specific file
rg "估值|锚点|加仓|减仓" 26year/2026-04.md
```

---

## AI Agent Onboarding Guide

If you are an AI agent encountering this repo for the first time, build context in this order:

1. **This file** (`README.md`) — the global map you are reading now
2. **[AGENTS.md](AGENTS.md)** — working conventions, content formats, retrieval commands
3. **[SKILL.md](.agent/skills/jinjian-perspective/SKILL.md)** — core investment decision framework (load on demand)
4. **[Knowledge Topology](docs/SYSTEM/jinjiancheng-knowledge-topology.md)** — 7-domain classification and word frequency stats
5. **Raw archives** (`22-25year/` + `26year/`) — the ultimate authority for any factual verification

**Retrieval paradigm**: source-first (raw archives take priority) → topology/deep-analysis docs serve only as navigation maps → final conclusions must be verified against the raw monthly files.

---

## Maintenance Conventions

- When adding content, follow the existing monthly archiving pattern into `22-25year/` or `26year/`
- When editing existing content, preserve surrounding formatting
- When modifying `SKILL.md` under `.agent/skills/`, you **MUST simultaneously update** `docs/SYSTEM/skill-prompt-guide.md`
- Derived documents (`docs/`) must never override raw archive facts
- This repo has no build, lint, or test workflow
