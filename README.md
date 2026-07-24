# Run application as a background service on windows using Task Scheduler
This document describes how to configure and run the SHS App Launch Service (Spring Boot WAR application) as a background service on a Windows server without using external tools like NSSM or other.
We use Windows Task Scheduler, batch scripts, and Apache httpd to run the application reliably after deployments.
## Folder Structure
Create an application directory: C:\shs-portal-app-launch-service\
Inside it, the following items will exist:
1.	runApp.bat
2.  stopApp.bat
3. 	shs-app-launch-service.war
4. 	RunAppLaunchService.xml
5. 	logs\AppLaunch (auto-created)
________________________________________
## Generate WAR File and Deploy
   To generate WAR from the App Launch Service project:
   **mvn clean install**
   After build completes:

    a) Copy target/shs-app-launch-service.war
    b) Paste into: C:\shs-portal-app-launch-service\

   During every deployment:

1.	End Task: C:\shs-portal-app-launch-service>schtasks /end /tn "RunAppLaunchService" >nul 2>&1
2.	Replace WAR file
3.	Start Task: C:\shs-portal-app-launch-service>schtasks /run /tn "RunAppLaunchService"
4. Apache HTTPD Reverse Proxy Configuration
                                                                                                                                      Update Apache httpd.conf:
### Forward /applaunch/* to Spring Boot running on port 8081
    ProxyPass        /applaunch/  http://localhost:8081/applaunch/  timeout=6000
    ProxyPassReverse /applaunch/  http://localhost:8081/applaunch/  timeout=6000
Why localhost?
1. Ensures the service works even if Windows hostname binding varies.
2. Eliminates DNS issues.
3. Works for Task Scheduler–launched background services.

Reload Apache after editing:
Restart apacheHttpServer from the service (first time only after adding above config)
Now your service is available at:
https://shsdev.symphonyhealth.com/applaunch/health/status
________________________________________
## runApp.bat (Start Application in Background)
   This script launches Spring Boot in background mode so Task Scheduler does not get stuck in Running state.
   Refer a script from the below path:
   
    File: resources/deployement/config/runApp.bat


## Highlights:
1. start "" launches Java asynchronously
2. Task Scheduler finishes immediately (status goes READY)
3. Logs go to: C:\shs-portal-app-launch-service\logs\applaunch-system.log

________________________________________
## stopApp.bat (Stop Running Java Service)
   Kills only the App Launch Service process. Refer code from the below file path.

    File: resources/deployement/config/stopApp.bat

   Notes:
   1. Identifies process by WAR name → no accidental termination.
   2. Works reliably even when multiple Java apps exist.
________________________________________
## Windows Task Scheduler Configuration
   We use Task Scheduler to ensure:
   1. The app starts on boot
   2. The app can be manually started after deployment
   3. Proper permissions are applied.

   Refer the below file path to know the schedular configuration

    File: resources/deployement/config/RunAppLaunchService.xml
________________________________________
## Importing Task into Task Scheduler
   Steps:
1.	Open Task Scheduler
2.	Right-click Task Scheduler Library → Import Task
3.	Select RunAppLaunchService.xml
4.	Set Start in (VERY IMPORTANT) under Actions → Edit:
      C:\shs-portal-app-launch-service
5.	Save task.
________________________________________
## Running the Service
       Start: schtasks /run /tn "RunAppLaunchService"
       Stop: schtasks /end /tn "RunAppLaunchService" >nul 2>&1

       Stop processes running on port 8081 including java.exe
       cd C:\shs-portal-app-launch-service>stopapp.bat

       Validate running: tasklist /fi "imagename eq java.exe"
       Check logs:
       C:\logs\AppLaunch\applaunch.log
________________________________________
## Deployment Procedure (Simple & Reliable)
**Stop running app:**

Stop running processes on port 8081 including java.exe.
Before executing below script **End** task schedular and **Disable** it and then execute below command.
    
    cd C:\shs-portal-app-launch-service>stopapp.bat

**Replace WAR file:**

    Copy new WAR to:
    C:\shs-portal-app-launch-service\

**Start application:**

    schtasks /run /tn "RunAppLaunchService" or Run manually from Task Schedular

**Verify:**

    • API health:
    http://localhost:8081/applaunch/health/status
    • Reverse proxy:
    https://shsdev.symphonyhealth.com/applaunch/health/status

