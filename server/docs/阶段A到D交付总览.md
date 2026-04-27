# 阶段A到D交付总览

本文档汇总当前已完成交付，便于你评审和面试讲解。

## 1. 交付范围

- 阶段 A：依赖与配置（MyBatis-Plus / MySQL / Redis profile）
- 阶段 B：`sys_user` 用户名查询 API
- 阶段 C：Redis 向量检索 API
- 阶段 D：Tool 对接用户查询与向量检索

---

## 2. 核心接口

### 2.1 用户查询

- `GET /api/users/{userId}/username`
- `POST /api/users/username-query`（供 Tool POST 调用）

返回：

```json
{ "userId": 1, "username": "admin" }
```

### 2.2 向量检索

- `POST /api/vector/search`

请求：

```json
{
  "queryVector": [0.1, 0.2, 0.3],
  "topK": 3
}
```

返回：

```json
{
  "items": [
    { "id": "doc:1", "content": "...", "score": 0.01 }
  ]
}
```

---

## 3. Tool 能力

`WeatherTools` 新增：

- `getUsernameByUserId(Long userId)`
- `vectorSearchByCsv(String vectorCsv, Integer topK)`

现有保持：

- `getWeather(String city)`
- `lookupDemoLine(String query)`

---

## 4. 关键代码位置

### 4.1 MyBatis-Plus 用户域

- `domain/user/entity/SysUserEntity`
- `domain/user/mapper/SysUserMapper`
- `domain/user/service/UserQueryService`
- `web/UserQueryController`
- `web/dto/UserNameResponse`
- `web/dto/UserNameQueryRequest`

### 4.2 Redis 向量域

- `domain/vector/config/VectorRedisProperties`
- `domain/vector/service/VectorSearchService`
- `web/VectorSearchController`
- `web/dto/VectorSearchRequest`
- `web/dto/VectorSearchResponse`
- `web/dto/VectorSearchItem`

### 4.3 Tool 与外呼

- `tools/WeatherTools`
- `external/ExternalPostProfileKeys`（新增 `USER_QUERY`、`VECTOR_SEARCH`）
- `application.yml` 新增 `lab.external.post.endpoints.user-query/vector-search`

---

## 5. 配置与启动

推荐 profile 组合：

- `SPRING_PROFILES_ACTIVE=qwen,mysql,redis-vector`

关键环境变量：

- `DASHSCOPE_API_KEY`
- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`

---

## 6. 测试与验证状态

### 已完成

- `mvn -DskipTests compile` 通过
- 新增向量服务单测：
  - `src/test/java/com/springailab/lab/domain/vector/service/VectorSearchServiceTest.java`

### 受环境影响未完成

- `mvn test` 在当前机器失败（默认 JDK 8）；项目需 JDK 17

---

## 7. 配套文档索引

- 基础入门：`docs/AI调用链入门指南.md`
- A+B 实现说明：`docs/阶段AB实现说明.md`
- C+D 实现说明：`docs/阶段CD实现说明.md`
- Tool 触发提示：`docs/Tool触发提示清单.md`
- 总体实施计划：`docs/MyBatisPlus与Redis向量接入实施计划.md`

---

## 8. 下一步建议

1. 切换 JDK 17 后跑全量 `mvn test`
2. 增加 `VectorSearchController` 集成测试（mock redis 响应异常/空结果）
3. 将 `vectorSearchByCsv` 升级为“文本 -> embedding -> 向量检索”链路

