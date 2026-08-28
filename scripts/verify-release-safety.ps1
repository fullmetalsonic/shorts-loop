param([Parameter(Mandatory=$true)][string]$Apk, [string]$DebugApk)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskSdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
$taskTool = if ($IsWindows) { 'aapt2.exe' } else { 'aapt2' }
$taskAapt = Join-Path $taskSdk "build-tools/35.0.0/$taskTool"
function Read-Badging([string]$Path) {
    $taskText = (& $taskAapt dump badging (Resolve-Path -LiteralPath $Path).Path | Out-String)
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect APK metadata.' }
    return $taskText
}
$taskBadging = Read-Badging $Apk
if ($taskBadging -notmatch "package: name='com.fullmetalsonic.shortsloop' versionCode='\d+' versionName='\d+\.\d+\.\d+'" -or
    $taskBadging -match '(?m)^application-debuggable(?:\s|$)') { throw 'Expected a non-debuggable product release APK.' }
[xml]$taskStrings = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/res/values/strings.xml') -Raw
foreach ($taskExpected in @(@('installed_version','설치 버전 %1$s'), @('app_version','ShortsLoop %1$s'))) {
    $taskValue = $taskStrings.resources.string | Where-Object { $_.name -eq $taskExpected[0] }
    if ($taskValue.InnerText -cne $taskExpected[1]) { throw 'Release label contains a stage badge or stale version.' }
}
$taskSources = Get-ChildItem -LiteralPath (Join-Path $taskRoot 'app/src/main/java') -Recurse -Filter '*.java' | Get-Content -Raw
if ($taskSources -match '공개 시험판|정식판|· 시험판') { throw 'Stale product release labels must not return.' }
if ($DebugApk) {
    if ((Read-Badging $DebugApk) -notmatch '(?m)^application-debuggable(?:\s|$)') { throw 'Negative fixture must be a real debuggable APK.' }
    $taskRejected = $false
    try { & (Join-Path $PSScriptRoot 'prepare-release.ps1') -Apk $DebugApk }
    catch {
        if ($_.Exception.Message -notlike 'Debuggable APKs cannot be published.*') { throw }
        $taskRejected = $true
    }
    if (-not $taskRejected) { throw 'Publication preparation accepted a debuggable APK.' }
    'DEBUG_APK_PUBLICATION_REJECTION=PASS'
}
'RELEASE_LABEL_AND_DEBUGGABLE_AUDIT=PASS'
'Signature continuity is checked separately by prepare-release.ps1; CI release APKs are unsigned.'
