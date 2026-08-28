param(
    [Parameter(Mandatory=$true)][ValidatePattern('^[A-Za-z0-9._:-]+$')][string]$Device,
    [Parameter(Mandatory=$true)][string]$ExpectedApk,
    [string]$ExpectedPlaybackPreferences,
    [switch]$AllowStoppedExecution
)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$taskAapt = Join-Path $env:LOCALAPPDATA 'Android/Sdk/build-tools/35.0.0/aapt2.exe'
function Read-Device([string[]]$Arguments) {
    $taskValue = (& $taskAdb -s $Device @Arguments 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0) { throw 'Read-only device inspection failed.' }
    return $taskValue
}
# No install, instrumentation, force-stop, permission writes, input, or UIAutomator here.
$taskMetadata = (& $taskAapt dump badging (Resolve-Path -LiteralPath $ExpectedApk).Path | Out-String)
$taskVersion = [regex]::Match($taskMetadata, "package: name='com.fullmetalsonic.shortsloop' versionCode='(\d+)' versionName='([^']+)'")
if ($LASTEXITCODE -ne 0 -or -not $taskVersion.Success) { throw 'Expected APK is not a valid product artifact.' }
$taskPackage = Read-Device @('shell','dumpsys','package','com.fullmetalsonic.shortsloop')
if ($taskPackage -notmatch ("versionCode=" + $taskVersion.Groups[1].Value + "\s") -or
    $taskPackage -notmatch ("versionName=" + [regex]::Escape($taskVersion.Groups[2].Value) + "\s")) { throw 'Installed version differs from expected APK.' }
$taskEnabled = Read-Device @('shell','settings','get','secure','enabled_accessibility_services')
if ($taskEnabled -notmatch 'com\.fullmetalsonic\.shortsloop/(?:com\.fullmetalsonic\.shortsloop)?\.service\.ShortsAccessibilityService') { throw 'Accessibility is not enabled; manual setup required.' }
$taskAccess = Read-Device @('shell','dumpsys','accessibility')
$taskBound = [regex]::Match($taskAccess, '(?s)Bound services:(.*?)Enabled services:').Groups[1].Value
if ($taskBound -notmatch 'com\.fullmetalsonic\.shortsloop') { throw 'Accessibility setting is enabled but the service is not bound; reconnect manually before delivery.' }
$taskRuntime = Read-Device @('shell','dumpsys','activity','service','com.fullmetalsonic.shortsloop/.service.ShortsAccessibilityService')
if ($taskRuntime -notmatch 'connected=true enabled=(?:true|false) target=\d+ current=\d+ blocked=false') { throw 'Runtime is disconnected or blocked; do not mark device verification complete.' }
$taskPrefs = Read-Device @('shell','run-as','com.fullmetalsonic.shortsloop','cat','shared_prefs/shorts_loop.xml')
[xml]$taskAfter = $taskPrefs
if (($taskAfter.map.ChildNodes | Where-Object { $_.GetAttribute('name') -eq 'floating_enabled' }).GetAttribute('value') -eq 'true') {
    $taskOverlay = Read-Device @('shell','appops','get','com.fullmetalsonic.shortsloop','SYSTEM_ALERT_WINDOW')
    if ($taskOverlay -notmatch 'SYSTEM_ALERT_WINDOW: allow') { throw 'Floating display is selected but overlay permission is not allowed.' }
}
if ($ExpectedPlaybackPreferences) {
    [xml]$taskBefore = Get-Content -LiteralPath $ExpectedPlaybackPreferences -Raw
    if ($AllowStoppedExecution -and ($taskAfter.map.ChildNodes | Where-Object { $_.GetAttribute('name') -eq 'enabled' }).GetAttribute('value') -ne 'false') { throw 'Execution must be OFF after installation.' }
    $taskBeforeValues = @($taskBefore.map.ChildNodes | Where-Object { -not $AllowStoppedExecution -or $_.GetAttribute('name') -ne 'enabled' } | ForEach-Object { "$($_.Name)|$($_.GetAttribute('name'))|$($_.GetAttribute('value'))|$($_.InnerText)" } | Sort-Object)
    $taskAfterValues = @($taskAfter.map.ChildNodes | Where-Object { -not $AllowStoppedExecution -or $_.GetAttribute('name') -ne 'enabled' } | ForEach-Object { "$($_.Name)|$($_.GetAttribute('name'))|$($_.GetAttribute('value'))|$($_.InnerText)" } | Sort-Object)
    if (Compare-Object $taskBeforeValues $taskAfterValues) { throw 'Playback preferences differ from the saved baseline.' }
}
$taskPackagePath = (Read-Device @('shell','pm','path','com.fullmetalsonic.shortsloop')).Trim()
if ($taskPackagePath -notmatch '^package:(/data/app/[^\r\n]+/base\.apk)$') { throw 'Unexpected installed package path.' }
$taskRemote = $Matches[1]
$taskDirectory = Join-Path $taskRoot ('private/delivery-checks/' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0,8))
New-Item -ItemType Directory -Path $taskDirectory | Out-Null
$taskPulled = Join-Path $taskDirectory 'installed.apk'
$null = Read-Device @('pull',$taskRemote,$taskPulled)
$taskHash = (Get-FileHash -LiteralPath $ExpectedApk -Algorithm SHA256).Hash
if ((Get-FileHash -LiteralPath $taskPulled -Algorithm SHA256).Hash -ne $taskHash) { throw 'Installed APK hash differs from delivery artifact.' }
$taskReport = [ordered]@{ result='PASS'; scope='Read-only delivery readiness, not social-app advance E2E'; version=$taskVersion.Groups[2].Value; code=[int]$taskVersion.Groups[1].Value; accessibilityEnabled=$true; accessibilityBound=$true; runtimeConnected=$true; blocked=$false; playbackPreferencesCompared=[bool]$ExpectedPlaybackPreferences; apkSha256=$taskHash }
$taskReport | ConvertTo-Json | Tee-Object -FilePath (Join-Path $taskDirectory 'result.json')
'UI visual check remains required: the setup warning must disappear when all setup requirements are satisfied.'
