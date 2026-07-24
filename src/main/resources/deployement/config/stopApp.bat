@echo off
echo Stopping SHS App Launch Service on port 8081...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
    echo Killing process ID %%a
    taskkill /F /PID %%a
)

echo Service stopped if it was running.
