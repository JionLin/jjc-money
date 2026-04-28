# 本地运行指南_JDK17

本指南用于在 Windows 环境下快速完成：

1. 切换到 JDK 17  
2. 启动项目  
3. 验证 MySQL 用户查询、Redis 向量检索、Chat Tool 链路

---

## 1. JDK 17 切换（PowerShell）

先确认你机器上 JDK 17 安装路径，例如：

- `D:\jdk-17`
- `C:\Program Files\Java\jdk-17`

在当前 PowerShell 会话执行：

```powershell
$env:JAVA_HOME="D:\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version
```

通过标准：

- `java -version` 显示 `17.x`
- `mvn -version` 中 Java 版本也是 `17.x`

---

## 2. 必要环境变量

```powershell
# 模型 profile（可换 deepseek）
$env:SPRING_PROFILES_ACTIVE="qwen,mysql,redis-vector"

# Qwen Key（若用 deepseek，则改 SPRING_AI_OPENAI_API_KEY）
$env:DASHSCOPE_API_KEY="YOUR_QWEN_KEY"

# MySQL
$env:MYSQL_URL="jdbc:mysql://ys-dev-mysql.lai-ai.com:3306/db_mirror_medical?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
$env:MYSQL_USERNAME="la_ys"
$env:MYSQL_PASSWORD="YOUR_MYSQL_PASSWORD"

# Redis 向量
$env:REDIS_HOST="127.0.0.1"
$env:REDIS_PORT="6379"
$env:REDIS_PASSWORD=""
$env:REDIS_VECTOR_INDEX="my_vector_index"
$env:REDIS_VECTOR_FIELD="vec"
$env:REDIS_CONTENT_FIELD="content"
$env:REDIS_VECTOR_TOP_K="5"
```

---

## 3. 编译与测试

```powershell
mvn clean compile
mvn test
```

若 `mvn test` 失败，优先检查：

1. 是否真的切到了 JDK 17  
2. MySQL/Redis 环境变量是否生效  
3. profile 是否包含 `mysql,redis-vector`

---

## 4. 启动与接口验证

启动：

```powershell
mvn spring-boot:run
```

### 4.1 用户名查询

```powershell
curl.exe "http://localhost:8080/api/users/1/username"
curl.exe -X POST "http://localhost:8080/api/users/username-query" ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":1}"
```

### 4.2 向量检索

```powershell
curl.exe -X POST "http://localhost:8080/api/vector/search" ^
  -H "Content-Type: application/json" ^
  -d "{\"queryVector\":[0.1,0.2,0.3],\"topK\":3}"
```

### 4.3 Chat Tool 链路

```powershell
curl.exe -X POST "http://localhost:8080/chat" ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"请根据 userId=1 查询用户名\"}"
```

---

## 5. TraceId 检查

请求返回头应包含：

- `X-Trace-Id`

日志中应看到：

- `[traceId:xxxx]`，并贯穿 Controller -> Tool -> External 调用。
