param(
    [Parameter(Mandatory=$true)][ValidatePattern('^emulator-\d+$')][string]$Device,
    [ValidateSet('preflight','network')][string]$Mode='preflight'
)
$ErrorActionPreference = 'Stop'
$taskAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$taskProduct = (& $taskAdb -s $Device shell getprop ro.product.name | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $taskProduct -notmatch 'sdk|emulator') { throw 'Instrumentation is restricted to disposable emulators; it force-stops the target app and may disconnect accessibility.' }
$taskResult = (& $taskAdb -s $Device shell am instrument -w -e mode $Mode com.fullmetalsonic.shortsloop.test/com.fullmetalsonic.shortsloop.updates.UpdateInstallInstrumentation | Out-String)
$taskExit = $LASTEXITCODE
$taskResult
if ($taskExit -ne 0 -or $taskResult -notmatch '(?m)^INSTRUMENTATION_RESULT: result=PASS\s*$') { throw 'Updater instrumentation did not pass.' }
