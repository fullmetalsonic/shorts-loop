param(
    [Parameter(Mandatory=$true)][ValidatePattern('^emulator-\d+$')][string]$Device,
    [Parameter(Mandatory=$true)][string]$PreviousApk,
    [Parameter(Mandatory=$true)][string]$ReleaseApk
)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$taskProduct = (& $taskAdb -s $Device shell getprop ro.product.name | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $taskProduct -notmatch 'sdk|emulator') { throw 'Disposable emulator only. Never run on a phone.' }
$taskDirectory = Join-Path $taskRoot ('private/upgrade-checks/' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + $Device)
New-Item -ItemType Directory -Path $taskDirectory | Out-Null
function Install-Apk([string]$Path) {
    $taskResult = (& $taskAdb -s $Device install -r (Resolve-Path -LiteralPath $Path).Path 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0 -or $taskResult -notmatch '(?m)^Success\s*$') { throw $taskResult }
}
function Run-Phase([string]$Phase) {
    $taskResult = (& $taskAdb -s $Device shell am instrument -w -e upgradePhase $Phase com.fullmetalsonic.shortsloop.test/com.fullmetalsonic.shortsloop.CompatibilityInstrumentation 2>&1 | Out-String)
    $taskExit = $LASTEXITCODE
    $taskResult | Tee-Object -FilePath (Join-Path $taskDirectory "$Phase.txt")
    if ($taskExit -ne 0 -or $taskResult -notmatch '(?m)^INSTRUMENTATION_RESULT: result=PASS\s*$') { throw 'Upgrade regression failed; do not uninstall to hide it.' }
}
Install-Apk $PreviousApk
Install-Apk (Join-Path $taskRoot 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')
Run-Phase 'seed'
Install-Apk $ReleaseApk
Run-Phase 'verify'
"RELEASE_APK_SHA256=$((Get-FileHash -LiteralPath $ReleaseApk -Algorithm SHA256).Hash)"
"EVIDENCE=$taskDirectory"
