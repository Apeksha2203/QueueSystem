REM VIVA GUIDE: Windows Tomcat launch helper. Environment/tool paths must exist on the presenting computer.
@echo off
echo ==========================================
echo Starting Temporary Local Tomcat Server...
echo ==========================================

:: Set paths using your downloaded folders
set "JAVA_HOME=c:\Users\ADMIN\Downloads\java-mini-project\OpenJDK17U-jdk_x64_windows_hotspot_17.0.15_6\jdk-17.0.15+6"
set "CATALINA_HOME=c:\Users\ADMIN\Downloads\java-mini-project\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60"

:: Start Tomcat in a new window
cd /d "%CATALINA_HOME%\bin"
call startup.bat

echo.
echo Tomcat should now be opening in a new Command Prompt window!
echo (Keep that new window open to keep the server running)
pause
