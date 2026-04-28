# Tool触发提示清单

用于提高模型触发以下 Tool 的稳定性：

- `getUsernameByUserId`
- `vectorSearchByCsv`
- `getWeather`
- `lookupDemoLine`

---

## 1. 用户查询 Tool：`getUsernameByUserId`

## 推荐问法

1. `请根据 userId=1 查询用户名。`
2. `帮我查一下用户ID 5 对应的用户名。`
3. `调用用户查询工具，参数 userId=2。`

## 不推荐问法

- `查一下这个用户信息`（没有给 userId，触发不稳定）

## 建议

- 尽量给明确数字 `userId`
- 单轮只问一个 userId，避免歧义

---

## 2. 向量检索 Tool：`vectorSearchByCsv`

该 Tool 当前参数是 CSV 向量字符串，需显式给出。

## 推荐问法

1. `请用向量检索，向量为 0.1,0.2,0.3，topK=3。`
2. `调用向量检索工具，query vector=0.12,0.07,0.88，返回前2条。`

## 不推荐问法

- `帮我做语义检索`（没有提供向量，模型难以填参）

## 建议

- 先给短向量做链路验证
- 线上可再升级为“文本->embedding->向量检索”两段式

---

## 3. 天气 Tool：`getWeather`

## 推荐问法

1. `查询杭州当前天气。`
2. `请调用天气工具，城市=Shanghai。`

---

## 4. 回显 Tool：`lookupDemoLine`

## 推荐问法

1. `查询关键词 order-123 的演示信息。`
2. `调用 demo 回显工具，query=patient-001。`

---

## 5. 联调顺序（建议）

1. 先触发 `getWeather` / `lookupDemoLine`（已有链路）
2. 再触发 `getUsernameByUserId`
3. 最后触发 `vectorSearchByCsv`

如果某个 Tool 连续不触发，优先把提问改成“显式工具意图 + 明确参数”的句式。
