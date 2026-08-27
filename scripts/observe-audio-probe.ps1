param(
    [Parameter(Mandatory=$true)][string]$Device,
    [int]$Samples = 35,
    [int]$IntervalMilliseconds = 2000,
    [Parameter(Mandatory=$true)][string]$Name
)
$ErrorActionPreference = 'Stop'
if ($Samples -lt 1 -or $Samples -gt 90 -or $IntervalMilliseconds -lt 500 -or $IntervalMilliseconds -gt 5000) { throw 'Invalid bounded observation.' }
if ($Name -notmatch '^[a-zA-Z0-9_-]+$') { throw 'Invalid evidence filename.' }
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskDirectory = Join-Path $taskRoot 'private/device-tests'
if (-not (Test-Path -LiteralPath $taskDirectory)) { New-Item -ItemType Directory -Path $taskDirectory | Out-Null }
$taskOutput = Join-Path $taskDirectory ($Name + '.jsonl')
if (Test-Path -LiteralPath $taskOutput) { throw 'Preserve previous evidence; choose a new name.' }
$taskAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
for ($taskIndex = 1; $taskIndex -le $Samples; $taskIndex++) {
    $taskRaw = & $taskAdb -s $Device shell dumpsys activity service com.fullmetalsonic.shortsloop.audioprobe/.capture.ProbeService
    $taskState = @($taskRaw | Where-Object { $_ -match '^\s*audio(Probe|Pattern|PatternDiag) ' } | ForEach-Object { $_.Trim() })
    if ($taskState.Count -eq 0) {
        $taskRaw = & $taskAdb -s $Device shell dumpsys activity com.fullmetalsonic.shortsloop.audioprobe/.ui.ProbeActivity
        $taskState = @($taskRaw | Where-Object { $_ -match '^\s*audio(Probe|Pattern|PatternDiag) ' } | ForEach-Object { $_.Trim() })
    }
    if ($taskState.Count -eq 0) { throw 'Audio probe has no process-local result; open the diagnostic app first.' }
    $taskLine = [pscustomobject]@{Sample=$taskIndex; At=(Get-Date).ToString('HH:mm:ss.fff'); State=$taskState} | ConvertTo-Json -Compress
    [IO.File]::AppendAllText($taskOutput, $taskLine + [Environment]::NewLine, [Text.UTF8Encoding]::new($false))
    $taskLine
    if ($taskState -match 'running=false') { break }
    if ($taskIndex -lt $Samples) { Start-Sleep -Milliseconds $IntervalMilliseconds }
}
