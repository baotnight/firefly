$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FlowSync - Start All" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Project Root: $root"
Write-Host "  Backend :  Spring Boot 3.3.5  (:8080)"
Write-Host "  Frontend:  Vue 3 + Element Plus (:8081)"
Write-Host "  Database:  MySQL 8.x  (:3306)"
Write-Host ""
Write-Host "  Make sure you have:" -ForegroundColor Yellow
Write-Host "    [1] MySQL running and init.sql executed"
Write-Host "    [2] application.yml password configured"
Write-Host "    [3] npm install completed in frontend\"
Write-Host ""

# ---- 加载 .env ----
$envFile = Join-Path $root "backend\.env"
$env:DEEPSEEK_API_KEY = ""
if (Test-Path $envFile) {
    Write-Host "  [OK] Loading backend\.env ..." -ForegroundColor Green
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^DEEPSEEK_API_KEY=(.+)$') {
            $env:DEEPSEEK_API_KEY = $matches[1]
            Write-Host "       DEEPSEEK_API_KEY loaded"
        }
    }
} else {
    Write-Host "  [INFO] backend\.env not found - AI fallback template" -ForegroundColor DarkGray
    Write-Host "         Copy backend\.env.example to backend\.env" -ForegroundColor DarkGray
}
Write-Host ""

# ---- 后端 ----
Write-Host "[1/3] Starting backend..." -ForegroundColor Green
$apiKey = $env:DEEPSEEK_API_KEY
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$root\backend'; Write-Host 'Backend starting (Spring Boot :8080)...' -ForegroundColor Yellow; java -Xmx1024m -classpath '.mvn\wrapper\maven-wrapper.jar' '-Dmaven.home=$root\backend' '-Dmaven.multiModuleProjectDirectory=$root\backend' '-Ddeepseek.api-key=$apiKey' org.apache.maven.wrapper.MavenWrapperMain spring-boot:run"
)

# ---- 前端 ----
Write-Host "[2/3] Starting frontend..." -ForegroundColor Green
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$root\frontend'; Write-Host 'Frontend starting (Vue :8081)...' -ForegroundColor Yellow; npm run serve"
)

# ---- ngrok（如果已安装） ----
$ngrokPath = Join-Path $root "ngrok.exe"
if (Test-Path $ngrokPath) {
    Write-Host "[3/3] Starting ngrok tunnel..." -ForegroundColor Green
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "cd '$root'; Write-Host 'ngrok tunnel starting...' -ForegroundColor Yellow; .\ngrok http 8081"
    )
} else {
    Write-Host "[3/3] ngrok not found - skip." -ForegroundColor DarkGray
    Write-Host "  Download from https://ngrok.com and put ngrok.exe in project root." -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "  Backend  - wait for 'Started FlowsyncApiApplication'"
Write-Host "  Frontend - wait for compile, then open http://localhost:8081"
if (Test-Path $ngrokPath) {
    Write-Host "  ngrok    - share the ngrok URL with others"
}
Write-Host ""
Write-Host "  Default accounts (password: 123456):" -ForegroundColor White
Write-Host "    leader   - admin role, all features"
Write-Host "    member1  - member role, limited access"
Write-Host "    member2  - member role, limited access"
Write-Host "  Or click 'Register' on the login page to create a new account."
Write-Host ""
