@echo off
set APP_HOME=C:\shs-portal-app-launch-service
set JAVA_HOME=C:\Program Files\Java\jdk-21
set APP_JAR=applaunch.war
set SPRING_PROFILE=aws-dev

cd /d %APP_HOME%

echo Starting SHS App Launch Service...

start "" "%JAVA_HOME%\bin\java.exe" -jar "%APP_JAR%" --spring.profiles.active=%SPRING_PROFILE%

echo Application started in background.
exit /b 0