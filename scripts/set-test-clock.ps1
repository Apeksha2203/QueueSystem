param([switch]$Disable,[string]$Time='10:15')
$ErrorActionPreference='Stop'
$clockPath=Join-Path (Split-Path $PSScriptRoot -Parent) 'config/test-clock.local.properties'
if($Disable){ if(Test-Path -LiteralPath $clockPath){Remove-Item -LiteralPath $clockPath}; Write-Host 'Real campus time restored.'; exit }
[void][DateTime]::ParseExact($Time,'HH:mm',[Globalization.CultureInfo]::InvariantCulture)
[IO.File]::WriteAllText($clockPath,"enabled=true`ntime=$Time`n",[Text.UTF8Encoding]::new($false))
Write-Host "LOCAL TEST CLOCK: today's campus time is frozen at $Time. Database changes are real."
Write-Host 'Restore real time before deployment: ./scripts/set-test-clock.ps1 -Disable'
