# VIVA GUIDE: Earlier workspace-specific Maven/Tomcat launcher. Inspect its hard-coded tool paths; scripts/start-backend.ps1 is the documented isolated launcher.
# Build and Run Script for QueueSystem
$ErrorActionPreference = "Stop"

$rootDir = "c:\Users\ADMIN\Downloads\java-mini-project"
$jdkDir = Join-Path $rootDir "OpenJDK17U-jdk_x64_windows_hotspot_17.0.15_6\jdk-17.0.15+6"
$mvnDir = Join-Path $rootDir "apache-maven-3.10.0-bin\apache-maven-3.10.0"
$tomcatDir = Join-Path $rootDir "apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60"

if (!(Test-Path $jdkDir) -or !(Test-Path $mvnDir) -or !(Test-Path $tomcatDir)) {
    Write-Host "Error: One or more tools could not be found!" -ForegroundColor Red
    Write-Host "JDK: $jdkDir" -ForegroundColor Red
    Write-Host "Maven: $mvnDir" -ForegroundColor Red
    Write-Host "Tomcat: $tomcatDir" -ForegroundColor Red
    exit 1
}

# Set Environment Variables
$env:JAVA_HOME = $jdkDir
$env:PATH = "$jdkDir\bin;$mvnDir\bin;" + $env:PATH
$env:CATALINA_HOME = $tomcatDir

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Building the Maven Project..." -ForegroundColor Yellow
mvn clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Deploying to Tomcat..." -ForegroundColor Yellow

$warFile = Join-Path $PWD "target\queue-system-1.0-SNAPSHOT.war"
if (!(Test-Path $warFile)) {
    $warFile = Join-Path $PWD "target\queue-system.war"
}

$tomcatWebapps = Join-Path $tomcatDir "webapps"

# Remove default ROOT application to deploy our app at the root URL (localhost:8080/)
$rootApp = Join-Path $tomcatWebapps "ROOT"
if (Test-Path $rootApp) {
    Remove-Item -Recurse -Force $rootApp
}
$rootWar = Join-Path $tomcatWebapps "ROOT.war"
if (Test-Path $rootWar) {
    Remove-Item -Force $rootWar
}

# Copy our WAR to ROOT.war
Copy-Item -Path $warFile -Destination $rootWar -Force
Write-Host "Deployed successfully." -ForegroundColor Green

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Starting Tomcat Server..." -ForegroundColor Yellow
Write-Host "Your application will be available at: http://localhost:8080/" -ForegroundColor Green

$startupScript = Join-Path $tomcatDir "bin\catalina.bat"
& $startupScript run
