param([Parameter(Mandatory=$true)][ValidatePattern('^emulator-\d+$')][string]$Device)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskSdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
$taskAdb = Join-Path $taskSdk 'platform-tools/adb.exe'
$taskProduct = (& $taskAdb -s $Device shell getprop ro.product.name | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $taskProduct -notmatch 'sdk|emulator') { throw 'A disposable emulator is required; no phone installation is allowed.' }
$taskBoot = (& $taskAdb -s $Device shell getprop sys.boot_completed | Out-String).Trim()
if ($taskBoot -ne '1') { throw 'Emulator is not fully booted. Start it and retry.' }
$taskMain = Join-Path $taskRoot 'app/build/outputs/apk/debug/app-debug.apk'
$taskTest = Join-Path $taskRoot 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
foreach ($taskApk in @($taskMain, $taskTest)) {
    if (-not (Test-Path -LiteralPath $taskApk)) { throw "Build the APK first: $taskApk" }
    $taskInstall = (& $taskAdb -s $Device install -r $taskApk 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0 -or $taskInstall -notmatch '(?m)^Success\s*$') { throw $taskInstall }
}
$taskDirectory = Join-Path $taskRoot 'private/compat-tests'
New-Item -ItemType Directory -Force -Path $taskDirectory | Out-Null
$taskLog = Join-Path $taskDirectory ((Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + $Device + '.txt')
$taskResult = (& $taskAdb -s $Device shell am instrument -w com.fullmetalsonic.shortsloop.test/com.fullmetalsonic.shortsloop.CompatibilityInstrumentation 2>&1 | Out-String)
$taskExit = $LASTEXITCODE
$taskResult | Tee-Object -FilePath $taskLog
if ($taskExit -ne 0 -or $taskResult -notmatch '(?m)^INSTRUMENTATION_RESULT: result=PASS\s*$') {
    throw "Compatibility smoke test failed. Evidence: $taskLog"
}
"APK_SHA256=$((Get-FileHash -LiteralPath $taskMain -Algorithm SHA256).Hash)"
"EVIDENCE=$taskLog"
Write-Warning 'This verifies app UI/configuration only, not accessibility permissions or social-app auto-advance E2E.'
