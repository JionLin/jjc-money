param(
    [string]$JdkHome = "D:\jdk-17",
    [string]$Profile = "qwen,mysql,redis-vector",
    [string]$QwenKey = "",
    [string]$MysqlPassword = "",
    [string]$RedisHost = "127.0.0.1",
    [string]$RedisPort = "6379",
    [string]$RedisPassword = "",
    [string]$RedisVectorIndex = "my_vector_index"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Test-Path "$JdkHome\bin\java.exe")) {
    throw "JDK 17 path invalid: $JdkHome"
}

$env:JAVA_HOME = $JdkHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$env:SPRING_PROFILES_ACTIVE = $Profile
$env:DASHSCOPE_API_KEY = $QwenKey
$env:MYSQL_URL = "jdbc:mysql://ys-dev-mysql.lai-ai.com:3306/db_mirror_medical?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
$env:MYSQL_USERNAME = "la_ys"
$env:MYSQL_PASSWORD = $MysqlPassword
$env:REDIS_HOST = $RedisHost
$env:REDIS_PORT = $RedisPort
$env:REDIS_PASSWORD = $RedisPassword
$env:REDIS_VECTOR_INDEX = $RedisVectorIndex
$env:REDIS_VECTOR_FIELD = "vec"
$env:REDIS_CONTENT_FIELD = "content"
$env:REDIS_VECTOR_TOP_K = "5"

Write-Host "== Java & Maven version ==" -ForegroundColor Cyan
java -version
mvn -version

Write-Host "== Compile ==" -ForegroundColor Cyan
mvn -q clean compile

Write-Host "== Test ==" -ForegroundColor Cyan
mvn -q test

Write-Host "== Run app ==" -ForegroundColor Cyan
mvn spring-boot:run
