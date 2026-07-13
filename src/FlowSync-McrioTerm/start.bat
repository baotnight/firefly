@echo off
set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"

echo ========================================
echo   FlowSync - Start All
echo ========================================
echo.
echo   Project: %ROOT%
echo   Backend : 8080  ^|  Frontend: 8081  ^|  MySQL: 3306
echo.

:: Load .env and set env var (inherited by child processes)
if exist "%ROOT%\backend\.env" (
    for /f "tokens=2 delims==" %%a in ('findstr /B "DEEPSEEK_API_KEY=" "%ROOT%\backend\.env" 2^>nul') do set "DEEPSEEK_API_KEY=%%a"
)
if defined DEEPSEEK_API_KEY (
    echo   [OK] AI Key loaded
) else (
    echo   [INFO] backend\.env not found or key empty
)
echo.

echo   Prerequisites:
echo     [1] MySQL running + init.sql executed
echo     [2] application.yml password set
echo     [3] npm install done in frontend\
echo.

start "FlowSync-Backend" cmd /k cd /d %ROOT%\backend ^&^& java -Xmx1024m -classpath .mvn\wrapper\maven-wrapper.jar -Dmaven.home=%ROOT%\backend -Dmaven.multiModuleProjectDirectory=%ROOT%\backend org.apache.maven.wrapper.MavenWrapperMain spring-boot:run

start "FlowSync-Frontend" cmd /k cd /d %ROOT%\frontend ^&^& npm run serve

if exist "%ROOT%\ngrok.exe" (
    echo   ngrok.exe found - starting tunnel...
    start "FlowSync-ngrok" cmd /k cd /d %ROOT% ^&^& ngrok http 8081
) else (
    echo   [Skip] ngrok.exe not found. Download: https://ngrok.com
)

echo.
echo   Windows opened:
echo     [FlowSync-Backend]  wait for "Started FlowsyncApiApplication"
echo     [FlowSync-Frontend] wait for compile, then http://localhost:8081
if exist "%ROOT%\ngrok.exe" echo     [FlowSync-ngrok]   share the public URL
echo.
echo   Login: leader / 123456  (admin)
echo          member1 / 123456 (member)
echo          member2 / 123456 (member)
echo   Or click Register to create new account.
echo.
pause
