# 阶段AB实现说明

本文档说明本次已在项目内完成的内容（对应实施计划阶段 A+B），方便你快速查看和联调。

## 1. 本次已完成

## 1.1 依赖接入（阶段 A）

`pom.xml` 已新增：

- `com.baomidou:mybatis-plus-spring-boot3-starter:3.5.7`
- `com.mysql:mysql-connector-j`
- `org.springframework.boot:spring-boot-starter-data-redis`

说明：

- MyBatis-Plus 用于 MySQL 5.7 用户表访问
- Redis 依赖先接入，为后续向量检索（阶段 C）做准备

## 1.2 配置接入（阶段 A）

新增配置文件：

- `src/main/resources/application-mysql.yml`
- `src/main/resources/application-redis-vector.yml`

根配置 `application.yml` 已补充 profile 与环境变量说明。

安全规则：

- MySQL/Redis 敏感信息都支持环境变量占位
- 未把明文密码硬编码到 Java 代码中

## 1.3 用户查询 API（阶段 B）

新增分层代码：

- 实体：`domain/user/entity/SysUserEntity`
- Mapper：`domain/user/mapper/SysUserMapper`
- Service：`domain/user/service/UserQueryService`
- Controller：`web/UserQueryController`
- DTO：`web/dto/UserNameResponse`

启动类增加：

- `@MapperScan("com.springailab.lab.domain.user.mapper")`

## 1.4 对外接口

- `GET /api/users/{userId}/username`

返回示例：

```json
{
  "userId": 1,
  "username": "admin"
}
```

若不存在，返回 `404` + `用户不存在`。

---

## 2. 本地联调方式

## 2.1 环境变量（PowerShell 示例）

```powershell
$env:SPRING_PROFILES_ACTIVE="qwen,mysql,redis-vector"
$env:DASHSCOPE_API_KEY="你的QwenKey"
$env:MYSQL_URL="jdbc:mysql://ys-dev-mysql.lai-ai.com:3306/db_mirror_medical?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
$env:MYSQL_USERNAME="la_ys"
$env:MYSQL_PASSWORD="你的密码"
```

## 2.2 启动与调用

启动：

```powershell
mvn spring-boot:run
```

调用：

```bash
curl "http://localhost:8080/api/users/1/username"
```

---

## 3. 已知限制

1. 当前运行机默认 JDK 为 8，项目需要 JDK 17 才能完整编译/测试  
2. Redis 向量检索尚未实现（在阶段 C）  
3. Tool 对 `UserQueryController` 的 HTTP 调用尚未接入（在阶段 D）
