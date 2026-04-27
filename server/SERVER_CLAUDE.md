# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建、测试与运行

这是一个基于 Maven 的 Spring Boot 项目，目标 Java 版本为 17。

- 运行应用：`mvn spring-boot:run`
- 完整验证：`mvn -q verify`
- 运行测试：`mvn -q test`
- 仅编译：`mvn -q clean compile`
- 运行单个测试类：`mvn -q -Dtest=ChatControllerTest test`
- 运行单个测试方法：`mvn -q -Dtest=ChatControllerTest#chatShouldRemainCompatible test`

仓库中还提供了一个本地辅助脚本，用于在 Windows/JDK17 环境下完成启动前配置与 profile 参数注入：

- `powershell -ExecutionPolicy Bypass -File .\scripts\local-run-jdk17.ps1`

## 运行时 profiles 与配置

应用通过 Spring profiles 切换不同的大模型提供方行为。

- `src/main/resources/application.yml` 中默认激活的 profile 是 `qwen`。
- 提供方相关配置位于：
  - `src/main/resources/application-qwen.yml`
  - `src/main/resources/application-deepseek.yml`
  - `src/main/resources/application-gpt.yml`
- 公共基础配置位于 `src/main/resources/application.yml`，其中包括 datasource、Redis、MyBatis、resilience4j、tool endpoint 注册，以及 chat session/cost 配置。

应用使用的关键环境变量包括：

- `SPRING_PROFILES_ACTIVE`
- `SPRING_AI_OPENAI_API_KEY`：用于 DeepSeek / OpenAI-compatible profiles
- `DASHSCOPE_API_KEY`：用于 qwen profile
- `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`
- `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`
- `REDIS_PASSWORD`

需要特别注意的启动行为：`src/main/java/com/springailab/lab/SpringAiLabApplication.java` 中显式处理了 API key 冲突问题。如果 `SPRING_AI_OPENAI_API_KEY` 和 `DASHSCOPE_API_KEY` 同时存在，应用会根据当前激活的 provider profile，强制把正确的值写入 `spring.ai.openai.api-key`。

## 高层架构

这个项目本质上是一个 Spring AI 实验仓库，核心围绕一条聊天编排主链路展开，并附带若干通过内部/外部 HTTP 契约驱动的工具能力。

### 1. 聊天入口与编排层

- `src/main/java/com/springailab/lab/web/ChatController.java` 暴露：
  - `POST /chat`：同步回复
  - `POST /chat/stream`：SSE 流式回复
- `src/main/java/com/springailab/lab/domain/chat/service/ChatOrchestrator.java` 是核心协调层。

`ChatOrchestrator` 是整个聊天流程汇聚的地方，负责：

- 从 conversation store 构建历史消息上下文
- 调用 Spring AI `ChatClient`
- 注册工具 Bean（`WeatherTools`），供模型侧进行 tool calling
- 在请求完成后持久化 user/assistant 对话消息
- 在流式模式下发送 SSE token 事件
- 通过 Micrometer 记录 usage / cost / latency 指标

如果要改聊天行为、工具暴露方式、会话记忆逻辑或流式语义，优先从 `ChatOrchestrator` 入手。

### 2. 会话记忆

对话历史通过 `ConversationMessageStore` 抽象出来。

- 接口：`src/main/java/com/springailab/lab/domain/chat/service/ConversationMessageStore.java`
- Redis 实现：`src/main/java/com/springailab/lab/domain/chat/service/RedisConversationMessageStore.java`

消息以简单的前缀字符串形式（`U:` / `A:`）存储 in Redis List 中，同时会按配置裁剪最大消息数，并设置 TTL 过期时间。这意味着聊天连续性依赖 Redis，而不是进程内内存。

### 3. Tool calling 模型

工具方法集中在 `src/main/java/com/springailab/lab/tools/WeatherTools.java`。

虽然类名叫 `WeatherTools`，但它实际上是当前暴露给模型的通用工具集合，包含：

- 天气查询
- demo echo 查询
- 用户名查询
- 向量相似度检索

这些工具方法并不直接访问下游服务，而是统一委托给一个共享的 outbound JSON POST 抽象层。

### 4. Tools 使用的统一出站 HTTP 抽象层

模型侧工具最终通过下面这组组件调用下游契约：

- 接口：`src/main/java/com/springailab/lab/external/ExternalJsonPostClient.java`
- 实现：`src/main/java/com/springailab/lab/external/RestClientExternalJsonPostClient.java`
- 装配与配置：`src/main/java/com/springailab/lab/external/ExternalJsonPostConfiguration.java`
- endpoint 注册属性：`src/main/java/com/springailab/lab/external/LabExternalPostProperties.java`

这一层非常关键，因为它集中处理了：

- 通过 profile key 查找 endpoint
- 请求超时
- trace header 透传
- 基于 resilience4j 的重试与熔断
- tool 响应的统一异常映射

如果后续要新增一个外部或内部契约驱动的工具，优先沿用这个模式，而不是在 tool 方法里直接写 HTTP 调用。

### 5. 既可直接访问、又可作为 tool 后端的内部 API

仓库中有一部分应用内 HTTP endpoint，同时也在 `lab.external.post.endpoints` 中注册成了工具调用目标：

- `src/main/java/com/springailab/lab/web/UserQueryController.java` → 用户查询
- `src/main/java/com/springailab/lab/web/VectorSearchController.java` → 向量检索
- `src/main/java/com/springailab/lab/web/MockExternalApiController.java` → 本地 mock endpoint，用于 weather / demo echo 这类契约

这意味着部分工具调用实际上会通过 HTTP 再回调到应用自身，而不是直接调用 service。改这部分逻辑时，不要轻易把这种间接层“简化掉”，因为它本身就是当前设计的一部分。

### 6. 数据访问层

#### 用户查询

用户查询走的是标准的 Spring service + MyBatis-Plus mapper 模式：

- service：`src/main/java/com/springailab/lab/domain/user/service/UserQueryService.java`
- mapper scan 在 `SpringAiLabApplication` 中配置

#### 向量检索

向量检索与 ORM 风格的数据访问分开实现：

- service：`src/main/java/com/springailab/lab/domain/vector/service/VectorSearchService.java`

这个 service 直接执行底层 Redis 命令（`FT.SEARCH ... KNN`）访问 RediSearch 索引，索引名、向量字段、内容字段都由 `VectorRedisProperties` 驱动。如果调整向量检索行为，要同时确认请求编码方式和 Redis 索引配置是否匹配。

### 7. 横切关注点：Trace 与指标

- `src/main/java/com/springailab/lab/web/TraceIdFilter.java` 负责注入/透传 `X-Trace-Id`，并写入 MDC 日志上下文。
- 工具发出的出站 HTTP 请求会复用这个 trace ID。
- 聊天相关 metrics 和估算 token 成本都在 `ChatOrchestrator` 内记录。

如果新增 HTTP 流程或外部调用，尽量沿用现有 filter/client 方案保持 trace 透传一致。

## 测试结构

测试整体以 unit / slice 风格为主，并避免真实调用模型提供方。

- 测试配置文件：`src/test/resources/application-test.yml`
- 其中会设置 dummy API key，并把 external POST endpoints 指向无效的 localhost 地址。

有代表性的测试包括：

- `src/test/java/com/springailab/lab/web/ChatControllerTest.java`：验证 controller 契约与委托行为
- `src/test/java/com/springailab/lab/domain/chat/service/RedisConversationMessageStoreTest.java`：覆盖会话持久化行为
- `src/test/java/com/springailab/lab/domain/vector/service/VectorSearchServiceTest.java`：覆盖 RediSearch 返回结果解析
- `src/test/java/com/springailab/lab/external/RestClientExternalJsonPostClientTest.java`：覆盖出站 HTTP 客户端行为
- `src/test/java/com/springailab/lab/tools/WeatherToolsTest.java`：覆盖 tool 摘要与适配逻辑

## 前端 / 演示界面

`src/main/resources/static/index.html` 中提供了一个最小静态演示页，会手动向 `/chat/stream` 发起请求并渲染原始流式响应。它适合做快速 SSE 联调验证，但并不是一个完整的前端应用。
