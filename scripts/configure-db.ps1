param(
    [string]$DatabaseHost = '127.0.0.1',
    [int]$Port = 3306,
    [string]$DatabaseUser = 'root'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$configDirectory = Join-Path $projectRoot 'config'
$configPath = Join-Path $configDirectory 'db.local.properties'

function ConvertTo-PropertyValue([string]$Value) {
    return $Value.Replace('\', '\\').Replace("`r", '\r').Replace("`n", '\n').Replace("`t", '\t')
}

$securePassword = Read-Host 'MySQL password (hidden; saved only in ignored local configuration)' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
    $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    $databaseUrl = "jdbc:mysql://${DatabaseHost}:${Port}/campus_queue?connectionTimeZone=Asia/Kolkata"
    $contents = "url=$(ConvertTo-PropertyValue $databaseUrl)`nuser=$(ConvertTo-PropertyValue $DatabaseUser)`npassword=$(ConvertTo-PropertyValue $plainPassword)`n"
    New-Item -ItemType Directory -Path $configDirectory -Force | Out-Null
    # Java Properties.load reads ISO-8859-1; escape non-ASCII characters to preserve passwords.
    $escapedContents = [regex]::Replace($contents, '[^\x00-\x7F]', {
        param($match)
        '\u{0:x4}' -f [int][char]$match.Value
    })
    [IO.File]::WriteAllText($configPath, $escapedContents, [Text.UTF8Encoding]::new($false))
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    $plainPassword = $null
    $contents = $null
    $escapedContents = $null
    $securePassword.Dispose()
}

Write-Host 'Local database configuration saved. Password was not printed.'
Write-Host 'The backend must be started with QUEUE_DB_CONFIG pointing to config/db.local.properties.'
