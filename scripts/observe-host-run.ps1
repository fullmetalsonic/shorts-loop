param(
    [Parameter(Mandatory=$true)][ValidatePattern('^[A-Za-z0-9._:-]+$')][string]$Device,
    [Parameter(Mandatory=$true)][ValidatePattern('^\d+$')][string]$DisplayId,
    [Parameter(Mandatory=$true)][ValidateSet('youtube','instagram')][string]$HostApp,
    [ValidateRange(1,30)][int]$Advances=10,
    [ValidateRange(30,3600)][int]$MaxSeconds=1800
)
$ErrorActionPreference='Stop'
$taskRoot=Split-Path -Parent $PSScriptRoot
$taskAdb=Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$taskPackage=if($HostApp -eq 'youtube'){'com.google.android.youtube'}else{'com.instagram.android'}
$taskName=$HostApp+'-verified-run-'+(Get-Date -Format 'yyyyMMdd-HHmmss')
$taskDir=Join-Path $taskRoot ('private/device-tests/'+$taskName)
New-Item -ItemType Directory -Path $taskDir | Out-Null
$taskClock=[Diagnostics.Stopwatch]::StartNew()
$taskBase=-1; $taskLast=-1; $taskBaseRequests=0; $taskRecoveryBase=0; $taskRecoveryStartsBase=0
$taskBaseAds=0; $taskBaseLive=0; $taskBaseTimed=0; $taskBaseVisual=0; $taskBaseLong=0
$taskResult='TIMEOUT'; $taskReason='Required confirmed transitions not reached'; $taskDone=0
function Number([string]$Raw,[string]$Key){return [int][regex]::Match($Raw,'\b'+$Key+'=(\d+)').Groups[1].Value}
while($taskClock.Elapsed.TotalSeconds -lt $MaxSeconds){
    $taskRaw=(& $taskAdb -s $Device shell dumpsys activity service com.fullmetalsonic.shortsloop/.service.ShortsAccessibilityService | Out-String)
    if($LASTEXITCODE -ne 0 -or $taskRaw -notmatch 'connected=true enabled=true target=1 current=[01] blocked=false' -or
        $taskRaw -notmatch ('app='+[regex]::Escape($taskPackage)+'\s')){
        $taskResult='FAIL'; $taskReason='Disconnected, stopped, blocked, wrong host or changed target';
        $taskRaw | Set-Content -LiteralPath (Join-Path $taskDir 'failure.txt') -Encoding utf8NoBOM; break
    }
    $taskCount=Number $taskRaw 'confirmed'; $taskRequests=Number $taskRaw 'requests'
    $taskRecoveries=Number $taskRaw 'recoveryEntries'; $taskStarts=Number $taskRaw 'recoveryStarts'
    $taskAds=Number $taskRaw 'adConfirmed'; $taskLive=Number $taskRaw 'liveConfirmed'
    $taskTimed=Number $taskRaw 'timedConfirmed'; $taskVisual=Number $taskRaw 'visualConfirmed'
    $taskLong=Number $taskRaw 'longConfirmed'
    if($taskBase -lt 0){
        $taskBase=$taskCount; $taskLast=$taskCount; $taskBaseRequests=$taskRequests
        $taskRecoveryBase=$taskRecoveries; $taskRecoveryStartsBase=$taskStarts
        $taskBaseAds=$taskAds; $taskBaseLive=$taskLive; $taskBaseTimed=$taskTimed; $taskBaseVisual=$taskVisual
        $taskBaseLong=$taskLong
        $null=& (Join-Path $PSScriptRoot 'capture-device.ps1') -Device $Device -DisplayId $DisplayId -Name "$taskName-0.png"
        "START $HostApp baseline=$taskBase goal=$Advances evidence=$taskDir"
    }
    $taskSample=[ordered]@{at=(Get-Date).ToString('o');seconds=[math]::Round($taskClock.Elapsed.TotalSeconds,1);runtime=$taskRaw.Trim()}
    $taskSample | ConvertTo-Json -Compress | Add-Content -LiteralPath (Join-Path $taskDir 'samples.jsonl') -Encoding utf8NoBOM
    if($taskCount -lt $taskLast -or $taskCount -gt $taskLast+1){$taskResult='FAIL';$taskReason='Process reset or missing transition evidence';break}
    if($taskCount -gt $taskLast){
        $taskDone=$taskCount-$taskBase
        $null=& (Join-Path $PSScriptRoot 'capture-device.ps1') -Device $Device -DisplayId $DisplayId -Name "$taskName-$taskDone.png"
        "ADVANCE $taskDone/$Advances at=$($taskSample.at) ads=$($taskAds-$taskBaseAds) live=$($taskLive-$taskBaseLive) long=$($taskLong-$taskBaseLong) timed=$($taskTimed-$taskBaseTimed) recovered=$($taskStarts-$taskRecoveryStartsBase)"
        $taskLast=$taskCount
        if($taskDone -ge $Advances){$taskResult='OBSERVED';$taskReason='All confirmations captured; actual distinct-page screenshots still require review';break}
    }
    Start-Sleep -Milliseconds 600
}
$taskSummary=[ordered]@{result=$taskResult;reason=$taskReason;host=$HostApp;goal=$Advances;completed=$taskDone;
    seconds=[math]::Round($taskClock.Elapsed.TotalSeconds,1);baseline=$taskBase;lastConfirmed=$taskLast;
    newRequests=($taskRequests-$taskBaseRequests);ads=($taskAds-$taskBaseAds);live=($taskLive-$taskBaseLive);
    timed=($taskTimed-$taskBaseTimed);visual=($taskVisual-$taskBaseVisual);long=($taskLong-$taskBaseLong);
    recoveryEntries=($taskRecoveries-$taskRecoveryBase);recoveryStarts=($taskStarts-$taskRecoveryStartsBase);
    scope='Read-only observation; manual input or app switches must be separately recorded; not PASS until screenshots reviewed'}
$taskSummary | ConvertTo-Json | Tee-Object -FilePath (Join-Path $taskDir 'result.json')
"EVIDENCE=$taskDir"
if($taskResult -ne 'OBSERVED'){throw 'Required host run incomplete; never combine failed and successful sessions.'}
