# MyBatisPlus与Redis向量接入实施计划

本文档用于指导当前项目接入：

1. MySQL 5.7 用户查询（使用 MyBatis-Plus）
2. Redis 8.x 向量检索（RediSearch）
3. Tool 通过 HTTP 调用项目内 API

---

## 1. 背景与目标

当前项目已具备：

- Spring Boot + Spring AI 对话链路
- Tool 机制（`@Tool`）
- 统一外部 HTTP POST 客户端
- 多模型 profile（qwen/deepseek）

本次目标是在不破坏现有链路的前提下，新增业务能力：

- 按 `user_id` 查询 `sys_user.username`
- 使用 Redis 向量索引做语义检索
- 将两者暴露为项目内 API，并可被 Tool 调用

---

## 2. 约束与输入（已确认）

### 2.1 MySQL（业务库）

- URL: `jdbc:mysql://ys-dev-mysql.lai-ai.com:3306/db_mirror_medical`
- 用户名: `la_ys`
- 密码: 已通过环境变量注入
- 表: `sys_user`
- 本次只返回字段：`username`

### 2.2 向量库

- 当前是 Redis 8.x + RediSearch 向量索引（非 MySQL 向量函数）
- 关键信息：`VECTOR HNSW`、`FLOAT32`、`DIM=3`、`COSINE`

---

## 3. 总体架构设计

```text
Chat / Tool
   |
   ├─ 用户查询 API（项目内）
   |    └─ MyBatis-Plus -> MySQL 5.7 (sys_user)
   |
   └─ 向量检索 API（项目内）
        └─ Redis Client -> FT.SEARCH KNN (RediSearch)
```

---

## 4. 分阶段实施计划

### 阶段 A：依赖与配置接入

- 引入 MyBatis-Plus / MySQL / Redis 依赖
- 新增 `application-mysql.yml`、`application-redis-vector.yml`
- 使用环境变量管理敏感信息

**验收**

- `qwen,mysql,redis-vector` profile 可启动
- 无明文密码入库

### 阶段 B：用户查询能力

- 新增 `SysUserEntity`、`SysUserMapper`、`UserQueryService`、`UserQueryController`
- 提供：
  - `GET /api/users/{userId}/username`
  - `POST /api/users/username-query`（供 Tool 调用）
- 仅返回 `username`

**验收**

- 给定 `user_id` 返回正确用户名
- 不返回 `password`

### 阶段 C：向量检索能力

- 新增 `VectorSearchController`、`VectorSearchService`、相关 DTO 与配置
- 提供 `POST /api/vector/search`
- 通过 Redis `FT.SEARCH ... KNN` 返回 topK

**验收**

- 固定向量输入可返回命中结果
- 无结果返回空数组，不抛 500

### 阶段 D：Tool 接入

- 新增 Tool 方法：
  - `getUsernameByUserId`
  - `vectorSearchByCsv`
- 新增外呼 profile key：
  - `user-query`
  - `vector-search`
- 摘要输出 + 失败短文案

**验收**

- 对话中可触发用户查询 Tool
- 对话中可触发向量检索 Tool

---

## 5. 风险与应对

1. Redis 向量命令兼容差异  
   - 先手工验证 `FT.SEARCH` 再固化代码

2. 向量维度不一致  
   - 在接口层做入参维度校验

3. MySQL 5.7 参数不匹配  
   - 明确 JDBC 参数（时区/字符集/SSL）

4. 敏感信息泄露  
   - 强制环境变量注入 + 日志脱敏

---

## 6. 联调建议

推荐 profile：

- `SPRING_PROFILES_ACTIVE=qwen,mysql,redis-vector`

顺序：

1. 先测 `GET /api/users/{id}/username`
2. 再测 `POST /api/vector/search`
3. 最后测 `/chat` Tool 链路

