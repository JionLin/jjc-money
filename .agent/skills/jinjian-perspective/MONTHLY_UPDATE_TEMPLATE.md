# JinJian Perspective Monthly Update Template

> 用途：每月拿到新的金渐成文章、评论区补充、年度展望修订、个人阶段变化后，按本模板完成一次结构化更新。
>
> 目标：不是“把新内容抄进 SKILL”，而是先判断它属于哪一层，再决定要不要改 [SKILL.md](/Users/johnny/Documents/jjc-money/.agent/skills/jinjian-perspective/SKILL.md)。

---

## 1. Monthly Update Meta

```md
# JinJian Perspective Monthly Update — YYYY-MM

## Update Meta

- Update month: YYYY-MM
- Operator: [name]
- Target skill version: vX.Y.Z
- Source files:
  - [ ] `26year/YYYY-MM.md`
  - [ ] `26year/Global Market Outlook for 2026.md`
  - [ ] other:
- Source verification status:
  - [ ] raw month file checked
  - [ ] comments reviewed
  - [ ] annual outlook checked
  - [ ] old anchors compared
```

---

## 2. New Material Intake

先把新增内容拆成“事实”，不要一上来写结论。

```md
## New Material Intake

### A. 新增战术动作
- [例] 继续加仓/减仓了什么
- [例] 哪个点位挂单、哪个点位没成交、有没有上调挂单
- [例] 哪些仓位从进攻流向防守

### B. 新增年度判断
- [例] 对全年美股/大宗/日本/港股/加密的看法是否有变化
- [例] 新增了哪些支撑位/目标位/情景判断

### C. 新增人生阶段信息
- [例] 是否更低频
- [例] 是否更重税务
- [例] 是否有新的资产隔离/家办/信托/一级市场动作

### D. 新增表达风格
- [例] 是否出现新的标志性表达
- [例] 是否出现更鲜明的比喻/警句

### E. 新增边界信息
- [例] 明确说自己不懂什么
- [例] 明确说哪些市场只是观察、不重仓
```

---

## 3. Layer Classification

这是最重要的一步。每条新信息都要先分层。

```md
## Layer Classification

### Layer 1 — Freshness / Current Facts
- 定义：实时价格、财报、估值、VIX、利率、宏观数据
- 去处：不写死进 skill 结论，只写进规则层
- 是否需要改 SKILL:
  - [ ] Yes
  - [ ] No
- 如果改，只能改：
  - Freshness Gate
  - Tools Integration
  - Output contract / SOP

### Layer 2 — Monthly Tactical Layer
- 定义：本月接货点、减仓点、挂单微调、仓位漂移、评论区对临盘动作的补充
- 主要来源：`26year/YYYY-MM.md`
- 是否需要改 SKILL:
  - [ ] Yes
  - [ ] No
- 常见落点：
  - §2.2 / §2.2b
  - §2.4
  - §2.6
  - §7.1 Step 4 / Step 6

### Layer 3 — Annual Outlook Layer
- 定义：全年主线、年度支撑位、目标位、跨资产排序
- 主要来源：`Global Market Outlook for 2026.md`
- 是否需要改 SKILL:
  - [ ] Yes
  - [ ] No
- 常见落点：
  - §0 Anchor Priority Rule
  - §2.5
  - §2.6
  - §6 Tools Integration
  - §7.3 Macro Outlook SOP

### Layer 4 — Historical Archive Layer
- 定义：旧文中的历史锚点、旧周期的操作逻辑
- 是否需要改 SKILL:
  - [ ] Yes
  - [ ] No
- 注意：
  - 不要让旧 deep-analysis 压过新的年度锚点
  - 只作为历史对照层

### Layer 5 — Author Context Layer
- 定义：作者当前所处的人生/资金/风险阶段
- 是否需要改 SKILL:
  - [ ] Yes
  - [ ] No
- 常见落点：
  - §2.4 Author Context
  - §4.1 能力圈
  - §7.2 Personal Portfolio SOP
```

---

## 4. Conflict Check

每月更新前，先查冲突，再决定是“覆盖”还是“并存”。

```md
## Conflict Check

### 4.1 Anchor Conflict
- Old anchor:
- New anchor:
- Conflict type:
  - [ ] tactical vs annual
  - [ ] annual vs old deep-analysis
  - [ ] fresh data vs local archive
- Resolution:
  - [ ] fresh data wins
  - [ ] annual outlook wins
  - [ ] monthly tactical note wins
  - [ ] keep both with timeframe label

### 4.2 Style Conflict
- 是否出现作者语气明显变化？
- 新表达是否值得写进 Signature Phrases？
- 是否只是一次性评论区情绪，不值得沉淀？

### 4.3 Paradigm Conflict
- 作者当前阶段是否已经变化？
- 这是长期转向，还是短期事件？
- 应写成 permanent rule 还是 snapshot context？

### 4.4 Scope Conflict
- 新内容是否逼近税务/法律/合规建议？
- 如果是，只能保留方向性约束，不能写成操作手册
```

---

## 5. Decision: What To Change In SKILL

这一步要克制。不是每个月都大改。

```md
## Decision: What To Change In SKILL

### 必改
- [ ]
- [ ]

### 可改
- [ ]
- [ ]

### 不改
- [ ]
- [ ]

### 本月不应改动的底层规则
- [ ] Freshness Gate
- [ ] Guardrails
- [ ] Output Contracts
- [ ] Anchor Priority Rule
- [ ] 其他：
```

---

## 6. Patch Plan

按 section 写 patch，避免全文件乱改。

```md
## Patch Plan

### Section-level patch list

1. `metadata.version`
   - from:
   - to:
   - reason:

2. `metadata.changelog`
   - add:
   - reason:

3. `§2.x`
   - target subsection:
   - patch summary:
   - source citation:

4. `§4.x`
   - target subsection:
   - patch summary:
   - source citation:

5. `§6`
   - patch summary:
   - source citation:

6. `§7`
   - patch summary:
   - source citation:
```

---

## 7. Suggested Versioning Rule

建议后续按这个规则升版本，避免混乱。

```md
## Versioning Rule

- vX.Y.Z

### Patch (`Z`)
- 适用场景：
  - 锚点覆盖顺序更清晰
  - 新增一个触发条件
  - 增补一个风险提醒
  - 修正一个冲突或遗漏

### Minor (`Y`)
- 适用场景：
  - 新增一个能力层
  - 新增一个输出合同
  - 新增一个模式路由
  - 新增大块方法论

### Major (`X`)
- 适用场景：
  - skill 的总体框架重写
  - 核心世界观/结构发生重大变化
```

---

## 8. Monthly Changelog Template

```md
## Changelog Entry

- "vX.Y.Z (YYYY-MM-DD): [本月更新一句话总结] — [1] [2] [3]"
```

例子：

```md
- "v2.5.2 (2026-05-31): 吸收 2026-05 档案 — 更新月度战术挂单、补充财报季风险、修正某标的年度锚点覆盖"
```

---

## 9. Recommended Output Format For Monthly Review

后续你每月给我新内容时，我建议我按下面这个格式回给你，便于你快速判断要不要落盘。

```md
# JinJian Perspective Monthly Review — YYYY-MM

## Executive Summary
- 本月最值得进 skill 的 3 个点
- 本月最不该写死的 3 个点

## Proposed Changes
1. [section]
   - reason:
   - source:
   - patch type:

2. [section]
   - reason:
   - source:
   - patch type:

## Conflicts
- [conflict]
- resolution:

## Recommended Version
- from:
- to:
- rationale:

## Go / No-Go
- [ ] Go
- [ ] No-Go
```

---

## 10. Monthly Update Checklist

每次更新前后都过一遍。

```md
## Checklist

Before editing:
- [ ] 我已经读过本月原始 source file
- [ ] 我已经区分“月度战术”与“年度展望”
- [ ] 我已经检查是否和旧 deep-analysis 冲突
- [ ] 我已经判断哪些只是短期噪音

During editing:
- [ ] 没有把实时价格写死进 skill
- [ ] 没有把税务/法律写成操作建议
- [ ] 没有把作者当月状态写成永久人设
- [ ] 没有混淆 tactical / annual / historical

After editing:
- [ ] version 已更新
- [ ] changelog 已更新
- [ ] 新增规则与 Guardrails 不冲突
- [ ] Output Contracts 仍然适用
- [ ] SOP 顺序仍然自洽
```

---

## 11. Fast Copy Template

下面这段可以直接复制，作为你每个月给我的输入模板。

```md
# JinJian Skill Monthly Update Request — YYYY-MM

## New source files
- `26year/YYYY-MM.md`
- `26year/Global Market Outlook for 2026.md` / [if updated]
- other:

## What changed this month
- 战术动作：
- 年度观点：
- 作者阶段：
- 风格表达：
- 风险边界：

## My update goal
- [ ] 只做 patch
- [ ] 做 minor update
- [ ] 先 review 再决定

## Special concerns
- [例] 新旧锚点冲突
- [例] 作者阶段明显变化
- [例] 某个赛道边界要不要调整
```

---

## 12. Maintenance Principle

最后记住一句话：

```md
月更 skill，不是把新内容堆进去。
而是把“新信息”放到正确的时间层、风险层、语境层。
```
