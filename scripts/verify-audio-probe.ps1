param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $taskRoot
try {
    if (-not $SkipBuild) {
        & .\gradlew.bat --no-daemon :audio-probe:assembleDebug :audio-probe:compileDebugUnitTestJavaWithJavac :audio-probe:lintDebug
        if ($LASTEXITCODE -ne 0) { throw 'Audio probe build/lint/test compilation failed.' }
    }
    $taskCache = if ($env:GRADLE_USER_HOME) { Join-Path $env:GRADLE_USER_HOME 'caches/modules-2/files-2.1' } else { Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1' }
    $taskJunit = Get-ChildItem -LiteralPath (Join-Path $taskCache 'junit/junit/4.13.2') -Recurse -Filter 'junit-4.13.2.jar' | Select-Object -First 1 -ExpandProperty FullName
    $taskHamcrest = Get-ChildItem -LiteralPath (Join-Path $taskCache 'org.hamcrest/hamcrest-core/1.3') -Recurse -Filter 'hamcrest-core-1.3.jar' | Select-Object -First 1 -ExpandProperty FullName
    $taskMain = Join-Path $taskRoot 'audio-probe/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes'
    $taskTests = Join-Path $taskRoot 'audio-probe/build/intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes'
    $taskTestNames = @(Get-ChildItem audio-probe/src/test/java -Recurse -Filter '*Test.java' | ForEach-Object {
        $taskTestSource = Get-Content -LiteralPath $_.FullName -Raw
        $taskPackage = [regex]::Match($taskTestSource, 'package\s+([\w.]+);').Groups[1].Value
        "$taskPackage.$($_.BaseName)"
    })
    & java '-Dfile.encoding=UTF-8' -cp (@($taskMain, $taskTests, $taskJunit, $taskHamcrest) -join [IO.Path]::PathSeparator) org.junit.runner.JUnitCore @taskTestNames
    if ($LASTEXITCODE -ne 0) { throw 'Audio signal unit tests failed.' }
    [xml]$taskManifest = Get-Content audio-probe/src/main/AndroidManifest.xml -Raw
    $taskNs = 'http://schemas.android.com/apk/res/android'
    $taskExpected = @('android.permission.RECORD_AUDIO', 'android.permission.FOREGROUND_SERVICE', 'android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION')
    $taskActual = @($taskManifest.manifest.'uses-permission' | ForEach-Object { $_.GetAttribute('name', $taskNs) })
    if (Compare-Object $taskExpected $taskActual) { throw 'Unexpected probe permission.' }
    if ($taskManifest.manifest.application.service.GetAttribute('exported', $taskNs) -ne 'false') { throw 'Capture service must remain private.' }
    $taskSource = (Get-ChildItem audio-probe/src/main/java -Recurse -Filter '*.java' | ForEach-Object { Get-Content $_.FullName -Raw }) -join "`n"
    if ($taskSource -match 'setAudioSource|MediaRecorder|createVirtualDisplay|java\.net\.|FileOutputStream|FileWriter|SharedPreferences|dispatchGesture') { throw 'Unexpected microphone/screen/storage/network/gesture path.' }
    foreach ($taskGuard in @('.addMatchingUid(uid)', '.addMatchingUsage(AudioAttributes.USAGE_MEDIA)', '.setAudioPlaybackCaptureConfig(config)', 'AudioRecord.READ_NON_BLOCKING', 'LIMIT_MS = 60_000', 'Arrays.fill(pcm, (short) 0)', 'isDeviceLocked()', 'START_NOT_STICKY', 'projection.registerCallback(callback, main)')) {
        if (-not $taskSource.Contains($taskGuard)) { throw "Missing capture safety guard: $taskGuard" }
    }
    'AUDIO_PROBE_SCOPE_AND_PRIVACY_AUDIT=PASS'
    $taskCapture = Get-Content audio-probe/src/main/java/com/fullmetalsonic/shortsloop/audioprobe/capture/CaptureSession.java -Raw
    if (-not $taskCapture.Contains('analyzer.accept(pcm, count, elapsed);') -or
        -not $taskCapture.Contains('analyzer.clear();') -or
        -not $taskCapture.Contains('AudioPatternAnalyzer.Diagnostics finalDiagnostics = analyzer.diagnostics();') -or
        $taskCapture.IndexOf('finalDiagnostics = analyzer.diagnostics();') -gt $taskCapture.IndexOf('analyzer.clear();') -or
        -not $taskCapture.Contains('analyzer.reset("NO_AUDIO_DATA");') -or
        -not $taskCapture.Contains('elapsed - lastPositiveReadMs >= 500') -or
        -not $taskCapture.Contains('analyzer.reset("ANALYSIS_TOO_SLOW");') -or
        $taskCapture.IndexOf('analyzer.accept(pcm, count, elapsed);') -gt $taskCapture.IndexOf('Arrays.fill(pcm, (short) 0);')) {
        throw 'Pattern analysis must consume PCM before clearing, and clear history at session end.'
    }
    $taskCoreSources = @(Get-ChildItem audio-probe/src/main/java/com/fullmetalsonic/shortsloop/audioprobe/core -Filter '*.java')
    if ($taskCoreSources | Select-String 'import android\.|java\.io\.|java\.net\.') { throw 'Audio core must remain pure RAM-only Java.' }
    'AUDIO_PATTERN_WIRING_AUDIT=PASS'
    Get-Item audio-probe/build/outputs/apk/debug/audio-probe-debug.apk | Select-Object Name, Length
    'APK_SHA256=' + (Get-FileHash audio-probe/build/outputs/apk/debug/audio-probe-debug.apk -Algorithm SHA256).Hash
} finally { Pop-Location }
