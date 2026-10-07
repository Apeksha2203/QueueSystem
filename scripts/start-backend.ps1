param([string]$JavaHome, [string]$MavenCommand, [string]$TomcatHome)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$workspaceRoot = Split-Path $projectRoot -Parent
if (!$JavaHome) { $JavaHome = Join-Path $workspaceRoot 'OpenJDK17U-jdk_x64_windows_hotspot_17.0.15_6/jdk-17.0.15+6' }
if (!$MavenCommand) { $MavenCommand = Join-Path $workspaceRoot 'apache-maven-3.10.0-bin/apache-maven-3.10.0/bin/mvn.cmd' }
if (!$TomcatHome) { $TomcatHome = Join-Path $workspaceRoot 'apache-tomcat-10.1.60-windows-x64/apache-tomcat-10.1.60' }
$env:JAVA_HOME = $JavaHome
$env:CATALINA_HOME = $TomcatHome
$env:CATALINA_BASE = Join-Path $projectRoot '.runtime/tomcat'
$env:QUEUE_DB_CONFIG = Join-Path $projectRoot 'config/db.local.properties'
if (!(Test-Path -LiteralPath $env:QUEUE_DB_CONFIG)) { throw 'Run scripts/configure-db.ps1 first.' }
& $MavenCommand --no-transfer-progress -f (Join-Path $projectRoot 'pom.xml') package
if ($LASTEXITCODE -ne 0) { throw 'Backend build failed.' }
foreach ($directory in 'conf', 'logs', 'temp', 'webapps', 'work') {
    New-Item -ItemType Directory -Path (Join-Path $env:CATALINA_BASE $directory) -Force | Out-Null
}
$serverPath = Join-Path $env:CATALINA_BASE 'conf/server.xml'
if (!(Test-Path -LiteralPath $serverPath)) {
    Copy-Item -Path (Join-Path $TomcatHome 'conf/*') -Destination (Join-Path $env:CATALINA_BASE 'conf') -Recurse
    $serverXml = [IO.File]::ReadAllText($serverPath)
    $serverXml = $serverXml.Replace('port="8080"', 'port="8081"').Replace('port="8005"', 'port="8006"')
    [IO.File]::WriteAllText($serverPath, $serverXml, [Text.UTF8Encoding]::new($false))
}
Copy-Item -LiteralPath (Join-Path $projectRoot 'target/queue-system.war') -Destination (Join-Path $env:CATALINA_BASE 'webapps/ROOT.war') -Force
Write-Host 'Starting the team Java backend at http://127.0.0.1:8081. React runs independently through Vite.'
& (Join-Path $TomcatHome 'bin/catalina.bat') run
