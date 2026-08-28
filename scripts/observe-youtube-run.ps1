param(
    [Parameter(Mandatory=$true)][ValidatePattern('^[A-Za-z0-9._:-]+$')][string]$Device,
    [Parameter(Mandatory=$true)][ValidatePattern('^\d+$')][string]$DisplayId,
    [ValidateRange(1,30)][int]$Advances=10,
    [ValidateRange(30,3600)][int]$MaxSeconds=1200,
    [ValidateRange(0,60)][int]$StartTimeoutSeconds=20
)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$taskName = 'youtube-run-' + (Get-Date -Format 'yyyyMMdd-HHmmss')
$taskDirectory = Join-Path $taskRoot ('private/device-tests/' + $taskName)
New-Item -ItemType Directory -Path $taskDirectory | Out-Null
$taskStarted = [Diagnostics.Stopwatch]::StartNew()
$taskBaseline = -1; $taskLastConfirmed = -1; $taskGeneration = -1; $taskLastCounter = ''
$taskLiveBaseline=0; $taskLiveConfirmed=0
$taskResult = 'TIMEOUT'; $taskReason = 'Required advances not reached within observation limit.'
while ($taskStarted.Elapsed.TotalSeconds -lt $MaxSeconds) {
    # Read-only: no instrumentation, input, activity launch, force-stop or permission change.
    $taskRaw = (& $taskAdb -s $Device shell dumpsys activity service com.fullmetalsonic.shortsloop/.service.ShortsAccessibilityService | Out-String)
    if ($LASTEXITCODE -ne 0) { $taskResult='FAIL'; $taskReason='Diagnostic read failed'; break }
    $taskPosition = [regex]::Match($taskRaw,'position=([-\d.]+) duration=([-\d.]+) pending=(true|false) requests=(\d+) confirmed=(\d+)')
    $taskCounter = [regex]::Match($taskRaw,'counter=(.*?) generation=(\d+)')
    if (-not $taskPosition.Success -or -not $taskCounter.Success -or $taskRaw -notmatch 'connected=true enabled=true target=1 current=[01] blocked=false' -or $taskRaw -notmatch 'app=com\.google\.android\.youtube\s') {
        if ($taskBaseline -lt 0 -and $taskStarted.Elapsed.TotalSeconds -lt $StartTimeoutSeconds) {
            Start-Sleep -Milliseconds 300; continue
        }
        $taskRaw | Set-Content -LiteralPath (Join-Path $taskDirectory 'failure-runtime.txt') -Encoding utf8NoBOM
        $taskResult='FAIL'; $taskReason='Disconnected, inactive, blocked, different host, or target not one'; break
    }
    $taskConfirmed = [int]$taskPosition.Groups[5].Value
    $taskNowGeneration = [int]$taskCounter.Groups[2].Value
    $taskDiagnostic = $taskCounter.Groups[1].Value
    $taskLiveMatch=[regex]::Match($taskRaw,'liveConfirmed=(\d+)')
    if ($taskLiveMatch.Success) {$taskLiveConfirmed=[int]$taskLiveMatch.Groups[1].Value}
    if ($taskBaseline -lt 0) {$taskLiveBaseline=$taskLiveConfirmed}
    if ($taskBaseline -lt 0) { $taskBaseline=$taskConfirmed; $taskLastConfirmed=$taskConfirmed; $taskGeneration=$taskNowGeneration; $taskLastCounter=$taskDiagnostic; "START baseline=$taskBaseline generation=$taskGeneration goal=$Advances" }
    $taskSample = [ordered]@{ at=(Get-Date).ToString('HH:mm:ss.fff'); elapsed=[Math]::Round($taskStarted.Elapsed.TotalSeconds,1); position=[double]$taskPosition.Groups[1].Value; duration=[double]$taskPosition.Groups[2].Value; pending=$taskPosition.Groups[3].Value; requests=[int]$taskPosition.Groups[4].Value; confirmed=$taskConfirmed; liveConfirmed=$taskLiveConfirmed; generation=$taskNowGeneration; counter=$taskDiagnostic; status=[regex]::Match($taskRaw,'(?m)^\s*status=(.*)$').Groups[1].Value.Trim() }
    $taskSample | ConvertTo-Json -Compress | Add-Content -LiteralPath (Join-Path $taskDirectory 'samples.jsonl') -Encoding utf8NoBOM
    if ($taskNowGeneration -ne $taskGeneration -or $taskConfirmed -lt $taskLastConfirmed -or $taskConfirmed -gt $taskLastConfirmed+1) { $taskResult='FAIL'; $taskReason='Session changed or transition evidence was skipped'; break }
    if ($taskDiagnostic.StartsWith('jump ') -and $taskDiagnostic -ne $taskLastCounter) { $taskResult='FAIL'; $taskReason='Playback counter reset on a detected time jump'; break }
    if ($taskConfirmed -gt $taskLastConfirmed) {
        $taskDone=$taskConfirmed-$taskBaseline
        "ADVANCE $taskDone/$Advances at=$($taskSample.at) nextDuration=$($taskSample.duration) liveSkipped=$($taskLiveConfirmed-$taskLiveBaseline)"
        $null = & (Join-Path $PSScriptRoot 'capture-device.ps1') -Device $Device -DisplayId $DisplayId -Name "$taskName-$taskDone.png"
        $taskLastConfirmed=$taskConfirmed
        if ($taskDone -ge $Advances) { $taskResult='PASS'; $taskReason='Ten-style automatic transition observation completed; screenshots require visual review'; break }
    }
    $taskLastCounter=$taskDiagnostic
    Start-Sleep -Milliseconds 900
}
$taskSummary=[ordered]@{ result=$taskResult; reason=$taskReason; baseline=$taskBaseline; lastConfirmed=$taskLastConfirmed; completed=($taskLastConfirmed-$taskBaseline); liveSkipped=($taskLiveConfirmed-$taskLiveBaseline); goal=$Advances; seconds=[Math]::Round($taskStarted.Elapsed.TotalSeconds,1); generation=$taskGeneration }
$taskSummary | ConvertTo-Json | Tee-Object -FilePath (Join-Path $taskDirectory 'result.json')
"EVIDENCE=$taskDirectory"
if ($taskResult -ne 'PASS') { throw 'YouTube continuous run did not pass; do not combine sessions or count manual interventions.' }
