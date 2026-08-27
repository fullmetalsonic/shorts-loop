param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $taskRoot
try {
    if (-not $SkipBuild) {
        & (Join-Path $taskRoot 'gradlew.bat') --no-daemon assembleDebug compileDebugUnitTestJavaWithJavac lintDebug
        if ($LASTEXITCODE -ne 0) { throw 'Android build, test compilation, or lint failed.' }
    }
    $taskCache = if ($env:GRADLE_USER_HOME) { Join-Path $env:GRADLE_USER_HOME 'caches/modules-2/files-2.1' } else { Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1' }
    $taskJunit = Get-ChildItem -LiteralPath (Join-Path $taskCache 'junit/junit/4.13.2') -Recurse -Filter 'junit-4.13.2.jar' | Select-Object -First 1 -ExpandProperty FullName
    $taskHamcrest = Get-ChildItem -LiteralPath (Join-Path $taskCache 'org.hamcrest/hamcrest-core/1.3') -Recurse -Filter 'hamcrest-core-1.3.jar' | Select-Object -First 1 -ExpandProperty FullName
    $taskMainClasses = Join-Path $taskRoot 'app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes'
    $taskTestClasses = Join-Path $taskRoot 'app/build/intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes'
    $taskSdkRoot = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    $taskAndroidApi = Join-Path $taskSdkRoot 'platforms\android-35\android.jar'
    $taskClassPath = @($taskMainClasses, $taskTestClasses, $taskJunit, $taskHamcrest, $taskAndroidApi) -join [IO.Path]::PathSeparator
    $taskTests = @(Get-ChildItem -LiteralPath (Join-Path $taskRoot 'app/src/test/java') -Recurse -Filter '*Test.java' | ForEach-Object {
        $taskSource = Get-Content -LiteralPath $_.FullName -Raw
        $taskPackage = [regex]::Match($taskSource, 'package\s+([\w.]+);').Groups[1].Value
        "$taskPackage.$($_.BaseName)"
    })
    # Direct JUnit avoids this PC's Gradle test-worker Unicode-classpath failure.
    & java '-Dfile.encoding=UTF-8' -cp $taskClassPath org.junit.runner.JUnitCore @taskTests
    if ($LASTEXITCODE -ne 0) { throw 'Direct JUnit regression tests failed.' }
    [xml]$taskManifest = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/AndroidManifest.xml') -Raw
    $taskAndroidNs = 'http://schemas.android.com/apk/res/android'
    $taskPermissions = @($taskManifest.manifest.'uses-permission' | ForEach-Object { $_.GetAttribute('name', $taskAndroidNs) })
    if ($taskPermissions.Count -ne 1 -or $taskPermissions[0] -ne 'android.permission.SYSTEM_ALERT_WINDOW') { throw 'Unexpected permission change.' }
    $taskCore = Get-ChildItem -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/core') -Filter '*.java'
    if ($taskCore | Select-String -Pattern 'import android\.') { throw 'Core acquired Android framework dependencies.' }
    'PERMISSION_AND_MODULE_AUDIT=PASS'
    # Static guard complements parser tests; real IME/clipboard interaction still needs a device.
    $taskCountEditor = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/ui/CountEditor.java') -Raw
    if ($taskCountEditor -notmatch 'setRawInputType\(InputType\.TYPE_CLASS_NUMBER\)' -or
        $taskCountEditor -notmatch 'setKeyListener\(TextKeyListener\.getInstance\(\)\)' -or
        $taskCountEditor -match 'LengthFilter|DigitsKeyListener|\.setInputType\(') {
        throw 'Count editor may silently transform pasted input.'
    }
    'COUNT_INPUT_CONFIG_AUDIT=PASS'
    $taskService = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/service/ShortsAccessibilityService.java') -Raw
    foreach ($taskGuard in @(
        'interacting = active; interruptSession();',
        'super.onConfigurationChanged(config); interruptSession();',
        'interruptSession(); lastPageIndex = -1;',
        'interruptSession(); holdUntil = SystemClock.uptimeMillis() + 900;',
        'if (gate.interrupt() == AdvanceGate.State.FAILED) failClosed(',
        'if (RuntimeState.blocked) return;'
    )) {
        if (-not $taskService.Contains($taskGuard)) { throw 'In-flight request interruption protection changed; review service wiring.' }
    }
    'ADVANCE_INTERRUPTION_WIRING_AUDIT=PASS'
    $taskBattery = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/ui/BatterySetupPanel.java') -Raw
    $taskActivity = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/ui/MainActivity.java') -Raw
    if (-not $taskBattery.Contains('power.isIgnoringBatteryOptimizations(context.getPackageName())') -or
        -not $taskBattery.Contains('Settings.ACTION_APPLICATION_DETAILS_SETTINGS') -or
        -not $taskBattery.Contains('Uri.parse("package:" + context.getPackageName())') -or
        -not $taskActivity.Contains('screen.battery.refresh();') -or
        $taskBattery -match 'ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS|Settings\.(Global|Secure|System)\.put|\.enabled\(|\.start\(') {
        throw 'Battery setup must remain read-only, app-scoped and refreshed on return.'
    }
    'BATTERY_SETUP_WIRING_AUDIT=PASS'
    $taskApk = Join-Path $taskRoot 'app/build/outputs/apk/debug/app-debug.apk'
    Get-Item -LiteralPath $taskApk | Select-Object Name, Length
    'APK_SHA256=' + (Get-FileHash -LiteralPath $taskApk -Algorithm SHA256).Hash
} finally { Pop-Location }
