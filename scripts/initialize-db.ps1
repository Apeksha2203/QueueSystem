# VIVA GUIDE: Local database provisioning launcher: uses the Java database setup runner and project SQL scripts.
param([string]$JavaHome, [string]$MavenCommand)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$workspaceRoot = Split-Path $projectRoot -Parent
$configPath = Join-Path $projectRoot 'config/db.local.properties'
if (!(Test-Path -LiteralPath $configPath)) { throw 'Run scripts/configure-db.ps1 first.' }
if (!$JavaHome) { $JavaHome = Join-Path $workspaceRoot 'OpenJDK17U-jdk_x64_windows_hotspot_17.0.15_6/jdk-17.0.15+6' }
if (!$MavenCommand) { $MavenCommand = Join-Path $workspaceRoot 'apache-maven-3.10.0-bin/apache-maven-3.10.0/bin/mvn.cmd' }
$env:JAVA_HOME = $JavaHome
$previousConfig = $env:QUEUE_DB_CONFIG
$previousUrl = $env:QUEUE_DB_URL
try {
    $env:QUEUE_DB_CONFIG = $configPath
    & $MavenCommand -f (Join-Path $projectRoot 'pom.xml') compile dependency:copy-dependencies '-DincludeScope=runtime'
    if ($LASTEXITCODE -ne 0) { throw 'Java build failed.' }
    # Read the non-secret URL only; preserve the locally selected host and port.
    $urlLine = Get-Content -LiteralPath $configPath | Where-Object { $_ -like 'url=*' } | Select-Object -First 1
    if (!$urlLine) { throw 'Database URL is missing from local configuration.' }
    $databaseUrl = $urlLine.Substring(4)
    $separator = if ($databaseUrl.Contains('?')) { '&' } else { '?' }
    $env:QUEUE_DB_URL = $databaseUrl + $separator + 'createDatabaseIfNotExist=true'
    $classPath = (Join-Path $projectRoot 'target/classes') + ';' + (Join-Path $projectRoot 'target/dependency/*')
    & (Join-Path $JavaHome 'bin/java.exe') -cp $classPath com.queue.util.DatabaseSetup (Join-Path $projectRoot 'database/schema.sql')
    if ($LASTEXITCODE -ne 0) { throw 'Database initialization failed.' }
    foreach ($migration in Get-ChildItem -LiteralPath (Join-Path $projectRoot 'database/migrations') -Filter '*.sql' | Sort-Object Name) {
        & (Join-Path $JavaHome 'bin/java.exe') -cp $classPath com.queue.util.DatabaseSetup $migration.FullName
        if ($LASTEXITCODE -ne 0) { throw "Database migration failed: $($migration.Name)" }
    }
} finally {
    $env:QUEUE_DB_CONFIG = $previousConfig
    $env:QUEUE_DB_URL = $previousUrl
}
