@echo off
echo Checking status of Tomcat11 service...

sc query Tomcat11 | find "STATE" | find "RUNNING" >nul

if %errorlevel% neq 0 (
    echo Tomcat11 is already stopped. No action needed.
    exit /b 0
)

echo Tomcat11 is running. Attempting to stop the service...
net stop Tomcat11

echo Waiting for Tomcat11 to transition to STOPPED state...

set /a attempt=1
:loop
echo Checking stop status (Attempt %attempt%)...

sc query Tomcat11 | find "STATE" | find "STOPPED" >nul
if %errorlevel% equ 0 (
    echo [SUCCESS] Tomcat11 stopped successfully!
    exit /b 0
)

if %attempt% geq 12 (
    echo [WARNING] Tomcat11 did not stop gracefully. Forcing shutdown...
    taskkill /f /im tomcat11.exe >nul 2>&1
    exit /b 0
)

timeout /t 5 >nul
set /a attempt=%attempt%+1

goto loop