param(
    [Parameter(Mandatory=$true)][string]$Device,
    [int]$Samples = 30,
    [int]$IntervalMilliseconds = 1500,
    [string]$Name = 'observation'
)
$ErrorActionPreference = 'Stop'
if ($Samples -lt 1 -or $Samples -gt 100 -or $IntervalMilliseconds -lt 300 -or $IntervalMilliseconds -gt 5000) { throw 'Invalid bounded observation parameters.' }
if ($Name -notmatch '^[a-zA-Z0-9_-]+$') { throw 'Invalid result filename.' }
$taskDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) 'private/device-tests'
if (-not (Test-Path -LiteralPath $taskDirectory)) { New-Item -ItemType Directory -Path $taskDirectory | Out-Null }
$taskPath = Join-Path $taskDirectory ($Name + '.jsonl')
if (Test-Path -LiteralPath $taskPath) { throw 'Preserve previous evidence; choose another result name.' }
$taskAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
for ($taskIndex = 1; $taskIndex -le $Samples; $taskIndex++) {
    $taskRaw = & $taskAdb -s $Device shell dumpsys activity service com.fullmetalsonic.shortsloop/.service.ShortsAccessibilityService
    $taskState = @($taskRaw | Where-Object { $_ -match '^\s+(ShortsLoop |connected=|position=|status=|counter=|ceiling=|ads=|timedEnabled=|visual=)' } | ForEach-Object { $_.Trim() })
    if ($taskState.Count -eq 0) { throw 'No diagnostic service response.' }
    $taskLine = [pscustomobject]@{ Sample=$taskIndex; At=(Get-Date).ToString('HH:mm:ss.fff'); State=$taskState } | ConvertTo-Json -Compress
    [IO.File]::AppendAllText($taskPath, $taskLine + [Environment]::NewLine, [Text.UTF8Encoding]::new($false))
    $taskLine
    if ($taskIndex -lt $Samples) { Start-Sleep -Milliseconds $IntervalMilliseconds }
}
