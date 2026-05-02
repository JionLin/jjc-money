# jjc-money 智能数字分身 v1 架构设计

**文档状态**: Draft  
**文档目的**: 说明如何将 `jinjian-perspective` Skill 产品化为一个可验证、可追溯、可分层回答的问答系统。  
**核心目标**: 不是做一个“像金渐成说话”的聊天机器人，而是做一个能够结合本地历史观点与当前事实、按既定判断框架稳定输出的 AI 问答系统。

---

## 1. 问题定义

本项目的核心问题不是“能不能模仿一种语气”，而是：

> 当用户问“某个标的现在怎么看”时，系统能否同时做到：
> 1. 找到作者过往的真实历史观点  
> 2. 获取当前最新事实  
> 3. 按 Skill 规定的判断框架进行融合  
> 4. 严格区分“历史观点”和“当前事实”

这决定了本系统本质上不是普通聊天应用，而是一个 **Skill 执行系统**。

---

## 2. 当前现状

当前仓库的真实结构如下：

- `22-25year/` 与 `26year/`：本地原始语料，属于最终真相层
- `docs/`：派生分析、拓扑文档、个人上下文、辅助说明
- `.agent/skills/jinjian-perspective/SKILL.md`：核心 Skill，定义判断框架、证据层级、输出合同与执行 SOP
- `spring-ai-lab/`：现有 Java 聊天与工具实验底座，已具备 SSE、会话记忆、工具调用等基础能力
- `spring-ai-lab/src/main/resources/static/index.html`：当前仅为 demo 级前端页面

因此，本项目当前并不是“从零搭建一个 AI 产品”，而是：

> 在一个以内容归档为核心的仓库中，为一个已经成熟的 Skill 提供运行时外壳。

---

## 3. V1 目标

V1 只聚焦最小闭环，不追求覆盖 Skill 的全部能力。

### 3.1 支持的问题类型

V1 仅支持两类问题：

1. **Historical View**
   - 用户询问作者过去如何判断、如何操作
   - 例：`2025 年 4 月关税战时他是怎么操作的？`

2. **Ticker**
   - 用户询问某个具体标的当前怎么看
   - 例：`NVDA 现在怎么看？能买吗？`

### 3.2 V1 成功标准

V1 成功，不是指“界面好看”或“对话像真人”，而是指：

- 能正确执行 Skill 的判断流程
- 能回到本地原始语料核对历史观点
- 能接入最新事实数据
- 能明确区分历史观点、当前事实、以及基于框架的联动判断

---

## 4. 非目标

以下内容不属于 V1 范围：

- 通用人格聊天机器人
- 多人格 / 多 Skill 系统
- 自动重建全文索引体系
- 向量检索优先方案
- 复杂个人组合管理
- 多租户产品化
- 平台级 agent 编排系统

这些都可以作为后续演进方向，但不应提前进入 V1。

---

## 5. 核心设计原则

V1 必须服从 `jinjian-perspective` Skill 中已经定义好的运行规则。

### 5.1 Freshness Gate

- 操作类 / 估值类问题必须先获取实时数据
- 若无法获取实时数据，只能标记为“历史框架演练”
- 不允许在缺少实时事实时给出当前操作建议

### 5.2 Evidence Hierarchy

证据优先级如下：

1. 实时市场 / 财报 / filing 数据
2. 原始月度档案文件 (`22-25year/`, `26year/`)
3. 派生文档（topology、deep-analysis 等）
4. 索引或导航层

### 5.3 Source Verification

任何最终结论都必须回到原始月度文件核验。  
派生文档只能帮助理解，不可替代原文。

### 5.4 Advice Framing

输出的是框架、区间、条件和风险，不是伪装成确定性的个性化指令。

### 5.5 Conflict Handling

如果当前事实与历史锚点冲突，必须解释冲突来源，而不是强行把旧观点包装成当前结论。

---

## 6. 系统分层

V1 建议采用如下四层结构：

```text
User Question
   │
   ▼
Interaction Layer
   │  Web chat / stream UI / query form
   ▼
Skill Execution Layer
   │  Mode Router + SOP runner + output contract
   ▼
Evidence Layer
   │  raw archive / derived docs / personal context
   ▼
Fresh Facts Layer
   │  IR / filings / market metrics
   ▼
Structured Answer
```

### 6.1 Interaction Layer

负责用户交互体验：

- 输入问题
- 展示流式输出
- 展示来源与时间标记
- 展示“历史观点 / 当前事实 / 联动判断”的分层结果

### 6.2 Skill Execution Layer

这是系统核心。

它的职责不是“随便调用大模型回答”，而是：

- 识别用户问题属于哪个模式
- 按 Skill 的 SOP 执行
- 确保输出结构符合合同
- 确保边界与免责声明正确落地

### 6.3 Evidence Layer

负责历史证据收集与核验，包括：

- 原始月度档案
- 个人上下文快照
- 派生分析文档
- 可选导航索引

### 6.4 Fresh Facts Layer

负责当前事实获取，包括：

- 最新价格
- 财报事实
- 估值指标
- 宏观与市场上下文

---

## 7. Skill 执行架构

V1 不重新发明判断逻辑，而是执行现有 Skill。

### 7.1 Skill 的角色

`jinjian-perspective` 不是普通 prompt，而是一份运行时合同，定义了：

- 模式路由
- 判断框架
- 输出结构
- 证据边界
- 风险表达方式
- 历史与现实的区分方式

### 7.2 系统职责

应用层需要做的不是“写一套新的分析逻辑”，而是：

- 正确加载 Skill
- 为 Skill 提供可靠证据
- 为 Skill 提供实时事实
- 按 Skill 的输出合同组织回答

---

## 8. 证据模型

V1 中，证据分为五类：

### 8.1 Raw Archive

- `22-25year/`
- `26year/`

这是作者历史观点的唯一最终真相层。

### 8.2 Derived Docs

- `docs/topology-details/`
- `docs/{TICKER}/*deep-analysis*.md`
- 其他分析与总结文档

这些文档用于帮助理解，但不直接作为最终历史立场依据。

### 8.3 Personal Context

- `docs/SYSTEM/personal-current-context.md`
- 相关 portfolio card / plan

这些属于动态上下文，不是永久真相。

### 8.4 Archive Overview

如果保留索引，则它只承担导航作用。  
它帮助缩小范围，但不能替代原文核验。

### 8.5 Fresh Facts

用于当前判断的实时事实：

- IR / earnings / filings
- valuation / market metrics
- macro context

---

## 9. V1 检索策略

V1 推荐采用 **source-first with light overview** 的检索路线。

### 9.1 推荐路线

```text
archive-level overview
        ↓
raw source search
        ↓
open matched sections
        ↓
verify historical view
```

### 9.2 原则

- 原始月度文件始终是最终依据
- overview 只用于缩小月份范围
- 不要求 article-level monthly indexes 作为必经层
- 若 overview 不完整，可直接搜索原始月度文件

### 9.3 关键词策略

针对标的类问题，优先搜索：

- 股票代码
- 中文名 / 英文名
- 估值
- 锚点
- 加仓
- 减仓
- 负成本
- 仓位
- VIX
- 现金流

---

## 10. 关于 docs/indexes 的定位

`docs/indexes` 不是核心资产，而是可替换的导航层。

### 10.1 建议定位

- `archive-index.md`：保留为月份级时间地图
- `monthly/YYYY-MM.md`：不再作为长期必要层

### 10.2 原因

- 真相本来就在原始月度文件中
- 当前文件规模仍允许直接搜索原文
- monthly indexes 维护成本较高，且容易过期
- Skill 真正需要的是“缩小范围 + 回原文核验”，而不是中间必须有 article-level 索引

### 10.3 结论

V1 推荐：

- 保留轻量 overview
- 弱化或逐步废弃 monthly indexes
- 将检索主链路收敛为 `overview + raw source`

---

## 11. 实时数据策略

### 11.1 原则

- 当前事实必须尽量新鲜
- 必须标注来源与时间
- 缺少 fresh data 时，只能输出历史框架演练

### 11.2 数据分层

- 官方事实：财报、filings、IR
- 市场与估值：价格、PE、增长、VIX 等

### 11.3 输出边界

必须明确区分：

- 公司官方披露事实
- 聚合数据源指标
- 基于 Skill 框架的解释性判断

---

## 12. 回答结构合同

V1 的回答建议统一分成四段：

1. **问题归类与前提**
   - 当前问题属于 Historical View 还是 Ticker
   - 是否具备 fresh data
   - 是否存在关键前提缺失

2. **历史观点证据**
   - 时间
   - 来源文件
   - 正文 / 评论区 / 作者回复
   - 原文上下文

3. **当前事实**
   - 数据时点
   - 主要指标
   - 来源标签

4. **联动判断**
   - 如果沿用这套历史框架，今天怎么看
   - 最大风险是什么
   - 哪些结论只是推导，不是历史原话

---

## 13. 演进路线

### Phase A

先跑通 Historical View  
目标：稳定从原始档案中提炼作者过往判断

### Phase B

跑通 Ticker  
目标：在历史观点基础上叠加实时事实

### Phase C

评估是否需要：

- 更强的检索层
- MCP 封装
- 向量检索
- 独立应用拆分

---

## 14. 一句话总结

V1 不是一个“会说金渐成口吻的话术机器人”，而是一个：

> 基于现有 Skill、以原始历史语料为依据、以实时事实为校准、能分层输出判断过程的问答系统。
