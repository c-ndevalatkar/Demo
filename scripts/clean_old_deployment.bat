@echo off
echo Cleaning up old deployment...

set WEBAPPS_DIR="C:\Program Files\Apache Software Foundation\Tomcat 11.0\webapps"
SET APP_NAME="applaunch"

if exist %WEBAPPS_DIR%\%APP_NAME%.war (
    echo Deleting old WAR file...
    del /q /f %WEBAPPS_DIR%\%APP_NAME%.war
)

if exist %WEBAPPS_DIR%\%APP_NAME% (
    echo Deleting old extracted application folder...
    rmdir /s /q %WEBAPPS_DIR%\%APP_NAME%
)

echo Cleanup completed successfully.
exit /b 0