# spring-ai-lab

与 `d:\code1` **同级**的独立 Maven 工程，用于 **JDK 17 + Spring Boot 3 + Spring AI**，通过 **OpenAI 兼容** 客户端连接 **DeepSeek**（`https://api.deepseek.com`），演示最小 **Function Calling**（`@Tool` + `ChatClient`）。

## 环境

- **JDK 17**（本机示例：`D:\jdklist\jdk17`）
- **Maven 3.9+**
- 可访问公网的 **DeepSeek API**；模型须为 **`deepseek-chat`**（工具调用；不要用 `deepseek-reasoner`）

## 配置

在系统环境或 shell 中设置：

```text
DEEPSEEK_API_KEY=<你的 DeepSeek API Key>
```

`src/main/resources/application.yml` 使用 `${DEEPSEEK_API_KEY}`，**勿**将真实密钥提交到仓库。

## 运行

```powershell
$env:JAVA_HOME = "D:\jdklist\jdk17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:DEEPSEEK_API_KEY = "<your-key>"
cd D:\spring-ai-lab
mvn spring-boot:run
```

## 调用示例

```powershell
curl -s -X POST http://localhost:8080/chat -H "Content-Type: application/json" -d "{\"message\":\"杭州今天天气怎么样？\",\"conversationId\":\"demo-1\"}"
```

流式（SSE）：

```powershell
curl -N -X POST http://localhost:8080/chat/stream -H "Content-Type: application/json" -d "{\"message\":\"继续说详细点\",\"conversationId\":\"demo-1\"}"
```

若模型选择调用工具，控制台会出现 `Tool getWeather invoked` 的 **INFO** 日志。

## 构建

```powershell
$env:JAVA_HOME = "D:\jdklist\jdk17"
cd D:\spring-ai-lab
mvn -q verify
```

测试使用 `test` profile，使用占位 API Key，**不**请求真实 DeepSeek。

## 已知限制

- 需网络与有效 **DeepSeek** 密钥才能完成真实对话与工具调用链路。
- 天气工具为 **演示数据**，非真实气象服务。
