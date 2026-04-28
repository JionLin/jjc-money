param(
    [string]$JdkHome = "D:\jdk-17",
    [string]$Profile = "qwen",
    [string]$QwenKey = ""
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

Write-Host "== Java & Maven version ==" -ForegroundColor Cyan
java -version
mvn -version

Write-Host "== Compile ==" -ForegroundColor Cyan
mvn -q clean compile

Write-Host "== Test ==" -ForegroundColor Cyan
mvn -q test

Write-Host "== Run app ==" -ForegroundColor Cyan
mvn spring-boot:run
