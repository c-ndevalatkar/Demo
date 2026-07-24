@echo off
echo Checking status of Tomcat11 service...

sc query Tomcat11 | find "STATE" | find "RUNNING" >nul

if %errorlevel% equ 0 (
    echo Tomcat11 is already running. No need to start.
    exit /b 0
)

echo Tomcat11 is stopped. Starting the service...
net start Tomcat11

echo Waiting for Tomcat11 to transition to RUNNING state...
set /a attempt=1

:loop

echo Checking start status (Attempt %attempt%)...
sc query Tomcat11 | find "STATE" | find "RUNNING" >nul

if %errorlevel% equ 0 (
    echo [SUCCESS] Tomcat11 started successfully!
    exit /b 0
)

if %attempt% geq 12 (
    echo [ERROR] Tomcat11 did not start within 60 seconds. Check logs.
    exit /b 1
)

timeout /t 5 >nul
set /a attempt=%attempt%+1
goto loop