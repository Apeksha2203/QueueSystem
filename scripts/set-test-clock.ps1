# VIVA GUIDE: Local-only clock override writer/remover. Disabling removes the ignored override and restores real India time without changing opening-hour rules.
param([switch]$Disable,[string]$Time='10:15')
$ErrorActionPreference='Stop'
$clockPath=Join-Path (Split-Path $PSScriptRoot -Parent) 'config/test-clock.local.properties'
# Disable removes only the ignored clock override; the fixed opening/lunch/closing rules remain unchanged.
if($Disable){ if(Test-Path -LiteralPath $clockPath){Remove-Item -LiteralPath $clockPath}; Write-Host 'Real campus time restored.'; exit }
[void][DateTime]::ParseExact($Time,'HH:mm',[Globalization.CultureInfo]::InvariantCulture)
# Freeze campus time locally for rehearsal; reservation writes still affect the configured database.
[IO.File]::WriteAllText($clockPath,"enabled=true`ntime=$Time`n",[Text.UTF8Encoding]::new($false))
Write-Host "LOCAL TEST CLOCK: today's campus time is frozen at $Time. Database changes are real."
Write-Host 'Restore real time before deployment: ./scripts/set-test-clock.ps1 -Disable'
