# 阶段CD实现说明

本文档说明本次在阶段 A+B 基础上新增的能力：

- 阶段 C：Redis 向量检索 API（项目内）
- 阶段 D：Tool 调用用户查询与向量检索 API

## 1. 阶段 C：向量检索 API

新增文件：

- `domain/vector/config/VectorRedisProperties`
- `domain/vector/service/VectorSearchService`
- `web/VectorSearchController`
- `web/dto/VectorSearchRequest`
- `web/dto/VectorSearchResponse`
- `web/dto/VectorSearchItem`

新增配置：

- `application-redis-vector.yml`
  - `lab.vector.redis.index-name`
  - `lab.vector.redis.vector-field`
  - `lab.vector.redis.content-field`
  - `lab.vector.redis.top-k`

接口：

- `POST /api/vector/search`

请求示例：

```json
{
  "queryVector": [0.1, 0.2, 0.3],
  "topK": 3
}
```

响应示例：

```json
{
  "items": [
    { "id": "doc:1", "content": "xxx", "score": 0.01 }
  ]
}
```

实现说明：

- 使用 `StringRedisTemplate` 执行 `FT.SEARCH ... KNN ...`
- 将向量转换为 `FLOAT32` little-endian 字节数组
- 解析 RediSearch 返回结果，映射为 `VectorSearchItem`

## 2. 阶段 D：Tool 接入

更新：

- `external/ExternalPostProfileKeys`：新增
  - `USER_QUERY`
  - `VECTOR_SEARCH`
- `application.yml` 新增 `lab.external.post.endpoints`：
  - `user-query -> /api/users/username-query`
  - `vector-search -> /api/vector/search`
- `UserQueryController` 新增 `POST /api/users/username-query`（供 Tool 按 POST 方式调用）
- `WeatherTools` 新增两个工具方法：
  - `getUsernameByUserId(Long userId)`
  - `vectorSearchByCsv(String vectorCsv, Integer topK)`

Tool 行为：

- 用户查询：提取 `username` 做摘要
- 向量检索：提取 top1 的 `content/score` 做摘要
- 参数异常：返回短文案（如“向量参数格式错误”）

## 3. 联调建议

建议 profile：

- `SPRING_PROFILES_ACTIVE=qwen,mysql,redis-vector`

链路验证顺序：

1. `GET /api/users/{id}/username`
2. `POST /api/users/username-query`
3. `POST /api/vector/search`
4. `POST /chat` 触发 Tool

## 4. 验证结果

- `mvn -DskipTests compile`：通过
- `mvn test`：当前机器默认 JDK 8，测试编译失败（Spring Boot 3.4 需要 JDK 17）
