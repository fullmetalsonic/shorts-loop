param(
    [Parameter(Mandatory=$true)][ValidateSet('emulator-5556')][string]$Device,
    [Parameter(Mandatory=$true)][ValidatePattern('^[a-z0-9-]+$')][string]$Checkpoint,
    [Parameter(Mandatory=$true)][ValidateSet('ko','en')][string]$ExpectedLanguage,
    [ValidatePattern('^[a-zA-Z0-9-]+$')][string]$RunName = (Get-Date -Format 'yyyyMMdd-HHmmss'),
    [ValidatePattern('^[a-z0-9-]+$')][string]$CompareWith
)
# Captures a real Settings-UI locale transition. It never installs, changes locales,
# grants accessibility, starts automation or contacts any non-emulator device.
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
function Invoke-Adb([string[]]$Arguments) {
    $taskResult = (& $taskAdb -s $Device @Arguments 2>&1 | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) { throw $taskResult }
    return $taskResult
}
$taskProduct = Invoke-Adb @('shell','getprop','ro.product.name')
$taskApi = Invoke-Adb @('shell','getprop','ro.build.version.sdk')
if ($taskProduct -notmatch 'sdk|emulator' -or $taskApi -ne '33') { throw 'Only the assigned disposable API33 emulator is supported.' }
$taskDirectory = Join-Path $taskRoot ('private/locale-checks/' + $RunName + '-' + $Device)
New-Item -ItemType Directory -Path $taskDirectory -Force | Out-Null
$taskPrefix = Join-Path $taskDirectory $Checkpoint
if (Test-Path -LiteralPath ($taskPrefix + '.json')) { throw 'Preserve existing checkpoint evidence; choose a new checkpoint name.' }
Invoke-Adb @('shell','uiautomator','dump','/sdcard/shortsloop-locale-check.xml') | Out-Null
Invoke-Adb @('pull','/sdcard/shortsloop-locale-check.xml',($taskPrefix + '.xml')) | Out-Null
Invoke-Adb @('shell','screencap','-p','/sdcard/shortsloop-locale-check.png') | Out-Null
Invoke-Adb @('pull','/sdcard/shortsloop-locale-check.png',($taskPrefix + '.png')) | Out-Null
$taskConfig = Invoke-Adb @('shell','cmd','activity','get-config')
$taskLocaleSetting = Invoke-Adb @('shell','settings','get','system','system_locales')
$taskActivity = Invoke-Adb @('shell','dumpsys','activity','activities')
$taskEvents = Invoke-Adb @('shell','logcat','-b','events','-d','-v','brief')
$taskPid = Invoke-Adb @('shell','pidof','com.fullmetalsonic.shortsloop')
$taskPreferences = Invoke-Adb @('shell','su','0','cat','/data/user/0/com.fullmetalsonic.shortsloop/shared_prefs/shorts_loop.xml')
$taskUpdates = Invoke-Adb @('shell','su','0','cat','/data/user/0/com.fullmetalsonic.shortsloop/shared_prefs/updates.xml')
$taskPackagePath = (Invoke-Adb @('shell','pm','path','com.fullmetalsonic.shortsloop')) -replace '^package:',''
$taskHash = (Invoke-Adb @('shell','sha256sum',$taskPackagePath)).Split(' ')[0]
[System.IO.File]::WriteAllText($taskPrefix + '.config.txt', $taskConfig)
[System.IO.File]::WriteAllText($taskPrefix + '.activity.txt', $taskActivity)
[System.IO.File]::WriteAllText($taskPrefix + '.events.txt', (($taskEvents -split "\r?\n" | Where-Object { $_ -match 'com\.fullmetalsonic\.shortsloop' }) -join [Environment]::NewLine))
[System.IO.File]::WriteAllText($taskPrefix + '.prefs.xml', $taskPreferences)
[System.IO.File]::WriteAllText($taskPrefix + '.updates.xml', $taskUpdates)
[xml]$taskXml = Get-Content -LiteralPath ($taskPrefix + '.xml') -Raw
$taskNodes = @($taskXml.SelectNodes('//node[@package="com.fullmetalsonic.shortsloop"]'))
$taskText = @($taskNodes | ForEach-Object { $_.GetAttribute('text') } | Where-Object { $_ -ne '' })
$taskHeading = if ($ExpectedLanguage -eq 'ko') { '쇼츠 자동 넘김' } else { 'ShortsLoop' }
if ($taskText -notcontains $taskHeading) { throw 'The actual product screen does not have the expected localized heading.' }
$taskVisibleText = ($taskNodes | ForEach-Object { $_.GetAttribute('text') + ' ' + $_.GetAttribute('content-desc') }) -join [Environment]::NewLine
if ($ExpectedLanguage -eq 'en' -and $taskVisibleText -match '[가-힣]') { throw 'Korean text leaked into the actual English product screen.' }
$taskExecution = @($taskNodes | Where-Object { $_.GetAttribute('resource-id') -eq 'com.fullmetalsonic.shortsloop:id/execution_toggle' })
if ($taskExecution.Count -ne 1 -or $taskExecution[0].GetAttribute('checked') -ne 'false') { throw 'Execution must remain OFF throughout the locale test.' }
$taskPreserved = $null
if ($CompareWith) {
    $taskBaseline = Join-Path $taskDirectory $CompareWith
    $taskPreviousPreferences = [System.IO.File]::ReadAllText($taskBaseline + '.prefs.xml')
    $taskPreviousUpdates = [System.IO.File]::ReadAllText($taskBaseline + '.updates.xml')
    $taskPreserved = $taskPreviousPreferences -ceq $taskPreferences -and $taskPreviousUpdates -ceq $taskUpdates
    if (-not $taskPreserved) { throw 'Locale switching changed playback or update preferences.' }
}
$taskResult = [ordered]@{
    checkpoint=$Checkpoint; time=(Get-Date -Format o); device=$Device; api=33
    expectedUiLanguage=$ExpectedLanguage; actualHeading=$taskHeading; systemLocalesSetting=$taskLocaleSetting
    runtimeConfiguration=$taskConfig; processId=$taskPid; installedSha256=$taskHash
    productText=$taskText; executionOff=$true; preferencesPreserved=$taskPreserved
    note='Actual foreground UI and saved-state capture; does not itself change system language or prove social-app auto-advance.'
}
$taskResult | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath ($taskPrefix + '.json') -Encoding utf8NoBOM
"CHECKPOINT=PASS"
"EVIDENCE=$taskPrefix"
"LANGUAGE=$ExpectedLanguage"
"SYSTEM_LOCALES=$taskLocaleSetting"
"PID=$taskPid"
"APK_SHA256=$taskHash"
"SETTINGS_PRESERVED=$taskPreserved"
