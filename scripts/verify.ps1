param([switch]$SkipBuild, [switch]$ExcludeUnwiredSequenceExperiment, [ValidateSet('debug','release')][string]$BuildType = 'debug')
# The legacy exclusion switch is retained for old commands; product scope is now explicit in Gradle.
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $taskRoot
try {
    & (Join-Path $PSScriptRoot 'verify-localization.ps1')
    & (Join-Path $PSScriptRoot 'verify-photo-safety.ps1')
    $taskVariant = (Get-Culture).TextInfo.ToTitleCase($BuildType)
    if (-not $SkipBuild) {
        & (Join-Path $taskRoot 'gradlew.bat') --no-daemon ":app:assemble$taskVariant" ":app:compile${taskVariant}UnitTestJavaWithJavac" ":app:lint$taskVariant"
        if ($LASTEXITCODE -ne 0) { throw 'Android build, test compilation, or lint failed.' }
    }
    $taskCache = if ($env:GRADLE_USER_HOME) { Join-Path $env:GRADLE_USER_HOME 'caches/modules-2/files-2.1' } else { Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1' }
    $taskJunit = Get-ChildItem -LiteralPath (Join-Path $taskCache 'junit/junit/4.13.2') -Recurse -Filter 'junit-4.13.2.jar' | Select-Object -First 1 -ExpandProperty FullName
    $taskHamcrest = Get-ChildItem -LiteralPath (Join-Path $taskCache 'org.hamcrest/hamcrest-core/1.3') -Recurse -Filter 'hamcrest-core-1.3.jar' | Select-Object -First 1 -ExpandProperty FullName
    $taskVariant = (Get-Culture).TextInfo.ToTitleCase($BuildType)
    $taskMainClasses = Join-Path $taskRoot "app/build/intermediates/javac/$BuildType/compile${taskVariant}JavaWithJavac/classes"
    $taskTestClasses = Join-Path $taskRoot "app/build/intermediates/javac/${BuildType}UnitTest/compile${taskVariant}UnitTestJavaWithJavac/classes"
    $taskSdkRoot = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    $taskAndroidApi = Join-Path $taskSdkRoot 'platforms\android-35\android.jar'
    $taskClassPath = @($taskMainClasses, $taskTestClasses, $taskJunit, $taskHamcrest, $taskAndroidApi) -join [IO.Path]::PathSeparator
    $taskTests = @(Get-ChildItem -LiteralPath (Join-Path $taskRoot 'app/src/test/java') -Recurse -Filter '*Test.java' | ForEach-Object {
        $taskSource = Get-Content -LiteralPath $_.FullName -Raw
        $taskPackage = [regex]::Match($taskSource, 'package\s+([\w.]+);').Groups[1].Value
        "$taskPackage.$($_.BaseName)"
    })
    $taskProductSources = Get-ChildItem -LiteralPath (Join-Path $taskRoot 'app/src/main/java') -Recurse -Filter '*.java' |
        Where-Object { $_.Name -ne 'VisualSequenceTracker.java' }
    if ($taskProductSources | Select-String -SimpleMatch 'VisualSequenceTracker') {
        throw 'Sequence experiment is referenced by the product; scope separation must be reviewed.'
    }
    $taskGradle = Get-Content -LiteralPath (Join-Path $taskRoot 'app/build.gradle') -Raw
    if (-not $taskGradle.Contains("main.java.exclude 'com/fullmetalsonic/shortsloop/core/VisualSequenceTracker.java'") -or
        -not $taskGradle.Contains("test.java.exclude 'com/fullmetalsonic/shortsloop/core/VisualSequenceTrackerTest.java'") -or
        (Get-ChildItem -LiteralPath $taskMainClasses -Recurse -Filter 'VisualSequenceTracker*.class')) {
        throw 'Unconnected experiment must not be compiled into the product.'
    }
    Write-Warning 'PRODUCT SUITE: the unconnected sequence implementation and its 20 experimental tests are excluded from the APK. Use verify-sequence-experiment.ps1 for the separate known-failing experiment (18 pass, 2 fail).'
    $taskTests = @($taskTests | Where-Object { $_ -ne 'com.fullmetalsonic.shortsloop.core.VisualSequenceTrackerTest' })
    # Direct JUnit avoids this PC's Gradle test-worker Unicode-classpath failure.
    & java '-Dfile.encoding=UTF-8' -cp $taskClassPath org.junit.runner.JUnitCore @taskTests
    $taskJUnitFailed = $LASTEXITCODE -ne 0
    [xml]$taskManifest = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/AndroidManifest.xml') -Raw
    $taskAndroidNs = 'http://schemas.android.com/apk/res/android'
    $taskPermissions = @($taskManifest.manifest.'uses-permission' | ForEach-Object { $_.GetAttribute('name', $taskAndroidNs) })
    $taskAllowedPermissions = @('android.permission.SYSTEM_ALERT_WINDOW', 'android.permission.INTERNET', 'android.permission.REQUEST_INSTALL_PACKAGES')
    if ($taskPermissions.Count -ne 3 -or (Compare-Object $taskPermissions $taskAllowedPermissions)) { throw 'Unexpected permission change.' }
    $taskProvider = $taskManifest.manifest.application.provider
    if ($taskProvider.GetAttribute('exported', $taskAndroidNs) -ne 'false' -or
        $taskProvider.GetAttribute('name', $taskAndroidNs) -ne '.updates.UpdateFileProvider' -or
        $taskManifest.manifest.application.GetAttribute('usesCleartextTraffic', $taskAndroidNs) -ne 'false') { throw 'Update provider/network isolation changed.' }
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
    $taskService = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/service/HostPlaybackSession.java') -Raw
    foreach ($taskGuard in @(
        'interacting = active; interruptSession();',
        'interruptSession(); holdUntil = SystemClock.uptimeMillis() + 1000;',
        'interruptSession(); lastPageIndex = -1;',
        'interruptSession(); holdUntil = SystemClock.uptimeMillis() + 900;',
        'if (gate.interrupt() == AdvanceGate.State.FAILED) failClosed(',
        'if (state.blocked) return;'
    )) {
        if (-not $taskService.Contains($taskGuard)) { throw 'In-flight request interruption protection changed; review service wiring.' }
    }
    'ADVANCE_INTERRUPTION_WIRING_AUDIT=PASS'
    foreach ($taskGuard in @('if (state == AdvanceGate.State.FAILED) { advanceTimedOut(); return; }',
        'PlaybackRestart.ordinaryRequest(pendingAd, pendingLive, pendingTimed, pendingVisual, pendingLong)',
        'if (restart.active()) { observeRestart(snapshot, now); return; }',
        'restart.cancel(); ordinaryRequestWindow = -1;', 'restart.accepts(activePackage, snapshot.windowId)',
        'counter.observe(start.progress, snapshot.identity, start.at);',
        'if (generation == requestGeneration) failClosed(')) {
        if (-not $taskService.Contains($taskGuard)) { throw "Fresh-start recovery wiring changed: $taskGuard" }
    }
    if ($taskService.IndexOf('if (restart.active()) { observeRestart(snapshot, now); return; }') -gt $taskService.IndexOf('if (snapshot.live) {')) {
        throw 'Recovery must be handled before every special-content skip path.'
    }
    $taskRecoveryMethod = [regex]::Match($taskService, '(?s)private void observeRestart\(.*?(?=private YouTubeSnapshot snapshot\()').Value
    if ($taskRecoveryMethod -match 'dispatchGesture|advanceRequests\+\+|confirmedAdvances\+\+|advance\(snapshot\)') {
        throw 'Fresh-start recovery must only observe, never swipe or confirm the old request.'
    }
    'PLAYBACK_RESTART_WIRING_AUDIT=PASS'
    foreach ($taskGuard in @('pendingLong ? inspectLongTransition(snapshot, now)',
        'gate.inspectLongPage(identity, safe ? value.progress : null, safe && longPagerChanged, now)',
        'gate.inspectContentPage(identity, safe ? value.progress : null,',
        'YouTubePageStepPolicy.permits(longRequestRow, longCurrentRow)',
        'youtubeContent && YouTubePageStepPolicy.next(longRequestRow, longCurrentRow)',
        'longRequestRow = readYouTubeRow(fresh);',
        'if (longCandidate(snapshot)) {', 'if (due) { longDueAt = now; advanceLong(snapshot); }',
        'longVideo.consume();', 'dispatchPageSwipe(fresh, false, true);',
        'LiveTransitionPolicy.accepts(longRequestedAt, event.getEventTime()',
        'longRequestPager != null && longRequestPager.equals(source)',
        'else if (unresolvedLongAttempt && store.enabled())',
        '"skip_long".equals(key)', '"long_video_seconds".equals(key)')) {
        if (-not $taskService.Contains($taskGuard)) { throw "Long video filter wiring changed: $taskGuard" }
    }
    if ($taskService.IndexOf('if (longCandidate(snapshot)) {') -gt $taskService.IndexOf('if (store.target() == 0) {') -or
        $taskService.IndexOf('if (longCandidate(snapshot)) {') -lt $taskService.IndexOf('if (snapshot.ad) {')) {
        throw 'Long filtering must follow ads/live but precede ordinary zero-count handling.'
    }
    'LONG_VIDEO_WIRING_AUDIT=PASS'
    foreach ($taskGuard in @('longRequestContent && YouTubeReader.PACKAGE.equals(activePackage) && !value.live && !value.ad',
        '!longRequestPager.equals(chosen)', '!longRequestPageBounds.equals(expected.page)',
        '!longRequestWindowBounds.equals(allowed)',
        'YouTubeSnapshot after = reader.read(root, store);',
        '!Objects.equals(after.contentIdentity, expected.contentIdentity)',
        'longRequestRow = longCurrentRow = YouTubePageStepPolicy.UNKNOWN;')) {
        if (-not $taskService.Contains($taskGuard)) { throw "YouTube page ordinal guard changed: $taskGuard" }
    }
    if ($taskService -match 'youtube-structure|structureScroll|YouTubeStructureProbe') { throw 'Temporary structural probe must not ship.' }
    'YOUTUBE_PAGE_POSITION_WIRING_AUDIT=PASS'
    $taskFacade = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/visual/VisualAssistController.java') -Raw
    $taskVisual = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/visual/Api34VisualAssistController.java') -Raw
    [xml]$taskAccessibility = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/res/xml-v34/accessibility_service.xml') -Raw
    [xml]$taskLegacyAccessibility = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/res/xml/accessibility_service.xml') -Raw
    if ($taskLegacyAccessibility.'accessibility-service'.HasAttribute('canTakeScreenshot', $taskAndroidNs) -or
        $taskFacade -match 'TakeScreenshotCallback|ScreenshotResult|HardwareBuffer|wrapHardwareBuffer|takeScreenshotOfWindow' -or
        -not $taskFacade.Contains('if (Build.VERSION.SDK_INT >= 34) return new Api34VisualAssistController(service, host);') -or
        -not $taskService.Contains('visual = VisualAssistController.create(coordinator,') -or
        $taskGradle -notmatch 'minSdk 26') { throw 'Compatibility isolation or install floor changed unexpectedly.' }
    foreach ($taskAttribute in $taskLegacyAccessibility.'accessibility-service'.Attributes) {
        if ($taskAccessibility.'accessibility-service'.GetAttribute($taskAttribute.LocalName, $taskAttribute.NamespaceURI) -ne $taskAttribute.Value) {
            throw 'Legacy/modern accessibility metadata drift beyond the screenshot capability.'
        }
    }
    'ANDROID_COMPATIBILITY_ISOLATION_AUDIT=PASS'
    if ($taskAccessibility.'accessibility-service'.GetAttribute('canTakeScreenshot', $taskAndroidNs) -ne 'true' -or
        -not $taskVisual.Contains('Build.VERSION.SDK_INT < 34') -or
        -not $taskVisual.Contains('service.takeScreenshotOfWindow(') -or
        -not $taskVisual.Contains('VisualCapturePolicy.accepts(') -or
        -not $taskVisual.Contains('buffer.close();') -or
        $taskVisual -match 'java\.io\.|java\.net\.|takeScreenshot\(' -or
        $taskService -notmatch '(?s)\(pendingVisual \|\| pendingTimed\)\s*\? gate\.inspectRecognizedPage' -or
        -not $taskService.Contains('"visual_assist".equals(key)')) {
        throw 'Visual opt-in/privacy/freshness/strict confirmation wiring changed; review required.'
    }
    'VISUAL_ASSIST_WIRING_AUDIT=PASS'
    $taskSecondsEditor = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/ui/SecondsEditor.java') -Raw
    if (-not $taskSecondsEditor.Contains('setRawInputType(InputType.TYPE_CLASS_NUMBER)') -or
        -not $taskSecondsEditor.Contains('setKeyListener(TextKeyListener.getInstance())') -or
        $taskSecondsEditor -match 'LengthFilter|DigitsKeyListener|\.setInputType\(' -or
        -not $taskSecondsEditor.Contains('ClocklessTimeoutPolicy.parseSeconds(input.getText())')) {
        throw 'Seconds editor raw-input/validation guard changed.'
    }
    foreach ($taskGuard in @(
        '&& specialHost() && value != null && value.progress == null && value.normalizedProgress == null',
        '&& value.photo == null && !value.live && value.visualCandidate && !ordinarySnapshot(value).ad && value.recognized()',
        'if (!timedCandidate(snapshot)) timed.reset();',
        '|| store.timedFallback()',
        'ClocklessTimeoutTracker.Result result = timed.observe(key, store.fallbackSeconds(), now);',
        'if (result.due()) advanceClockless(snapshot, true);',
        '"timed_fallback".equals(key)', '"fallback_seconds".equals(key)',
        'timed.reset(); state.timedRemainingSeconds = -1;',
        'if (pendingTimed) confirmedTimed++;'
    )) {
        if (-not $taskService.Contains($taskGuard)) { throw "Timed fallback safety wiring changed: $taskGuard" }
    }
    if ($taskService.IndexOf('if (timedCandidate(snapshot)) {') -gt $taskService.IndexOf('if (snapshot.visualCandidate && store.visualAssist()')) {
        throw 'Timed fallback must be prioritized before visual analysis.'
    }
    'TIMED_FALLBACK_WIRING_AUDIT=PASS'
    $taskFloating = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/overlay/FloatingController.java') -Raw
    if (-not $taskFloating.Contains('status.equals("timed.confirming") ? localized.getString(R.string.flo_next)') -or
        $taskFloating.Contains('status.startsWith("timed")')) {
        throw 'Timed errors must not be shown as an in-flight next-page request.'
    }
    'TIMED_PENDING_LABEL_AUDIT=PASS'
    if (-not $taskService.Contains('if (store.target() == 0 && !adSkippingEnabled() && !liveSkippingEnabled() && !longSkippingEnabled() && !photoSkippingEnabled())') -or
        -not $taskService.Contains('AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.isSelected(activePackage))') -or
        $taskService -notmatch '(?s)if \(store.target\(\) == 0\) \{\s*counter.reset\(\); timed.reset\(\); visual.reset\(\);[^}]+return;' -or
        $taskService.IndexOf('if (snapshot.ad && (adSkippingEnabled() || !TikTokReader.PACKAGE.equals(activePackage))) {') -gt $taskService.IndexOf('if (store.target() == 0) {') -or
        $taskService.IndexOf('if (store.target() == 0) {') -gt $taskService.IndexOf('if (timedCandidate(snapshot)) {')) {
        throw 'Independent ads must run before the zero-play guard; timers must stay behind it.'
    }
    'ZERO_PLAY_ADS_WIRING_AUDIT=PASS'
    foreach ($taskLiveGuard in @('LiveSkipPolicy.enabled(store.enabled(), store.skipLive(), store.youtubeEnabled())',
        'LiveSkipTracker.Result result = live.observe(key, store.liveDelaySeconds(), now);',
        'if (result.due()) advanceLive(snapshot);', 'pendingLive ? inspectLiveTransition(snapshot, now)',
        'gate.inspectLivePage(', 'LiveTransitionPolicy.accepts(liveRequestedAt, event.getEventTime(),',
        'liveRequestPager.equals(source)', 'else if (unresolvedLiveAttempt && store.enabled())',
        '"skip_live".equals(key)', '"live_delay_seconds".equals(key)', 'reader.close();')) {
        if (-not $taskService.Contains($taskLiveGuard)) { throw "Live preview safety wiring changed: $taskLiveGuard" }
    }
    if ($taskService.IndexOf('if (snapshot.live) {') -gt $taskService.IndexOf('if (store.target() == 0) {')) {
        throw 'Live previews must remain independent of ordinary repeat count.'
    }
    'LIVE_SKIP_WIRING_AUDIT=PASS'
    # Guard the service-level lifecycle paths as well as the pure host policy.
    $taskCoordinator = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/service/ShortsAccessibilityService.java') -Raw
    if (-not $taskService.Contains('store != null && store.enabled() && !state.blocked') -or
        $taskService -notmatch 'if \(configureLiveTree\(activePackage\)\) return YouTubeSnapshot.unavailable' -or
        $taskService -notmatch 'state.blocked = true; clearLayoutQuery\(\);' -or
        -not $taskCoordinator.Contains('for (HostPlaybackSession session : sessions) include |= session.needsLayoutNodes();') -or
        -not $taskCoordinator.Contains('for (HostPlaybackSession session : sessions) session.destroySession();') -or
        $taskCoordinator -notmatch '(?s)sessions.clear\(\);\s*updateQueryMode\(\);') {
        throw 'Shared tree mode must use the union of enabled unblocked hosts and clear on shutdown.'
    }
    'LIVE_TREE_LIFECYCLE_AUDIT=PASS'
    foreach ($taskGuard in @('if (pendingNormalized) { failClosed("error.advance"); return; }',
        'tiktokTransition.inspect(tiktokFrame(snapshot), now)',
        'normalizedCounter.permitsAdvance(verified.normalizedProgress, normalizedCounterKey(verified), SystemClock.uptimeMillis())',
        'TikTokReader.findPager(root, fresh, fresh.windowId)',
        'else if (unresolvedNormalizedAttempt && store.enabled())',
        'tiktokTransition.cancel(); pendingNormalized = false;',
        'if (!snapshot.ad || !adSkippingEnabled()) adDelay.reset();', 'if (store.adDelayTenths() == 0) advanceAd(snapshot);',
        'AdDelayTracker.Result delay = adDelay.observe(key, store.adDelayTenths(), now);')) {
        if (-not $taskService.Contains($taskGuard)) { throw "Multi-host/timing protection missing: $taskGuard" }
    }
    if ($taskService.IndexOf('if (TikTokReader.PACKAGE.equals(activePackage) && ordinarySnapshot(snapshot).normalizedUsable()) {') -lt $taskService.IndexOf('if (store.target() == 0) {')) {
        throw 'Normalized repeat must remain disabled by count zero.'
    }
    $taskTikTokReader = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/detection/TikTokReader.java') -Raw
    if ($taskTikTokReader -match 'new Progress\(|Thread.sleep|ACTION_CLICK|\.performAction\(') {
        throw 'TikTok detection cannot guess seconds or perform input.'
    }
    'MULTI_HOST_TIMING_WIRING_AUDIT=PASS'
    foreach ($taskGuard in @('case TIMED: return timedCandidate(value);',
        'value.progress == null && value.normalizedProgress == null',
        'value.photo == null && !value.live', 'ordinarySnapshot(value).usable()',
        'tiktokTransition.begin(tiktokFrame(verified), now);',
        'unresolvedNormalizedAttempt = true;',
        'SystemClock.uptimeMillis() - longDueAt <= 900',
        'windowGuard.allowsSemantic(getWindows(), verified.windowId, verified.windowBounds, verified.page)')) {
        if (-not $taskService.Contains($taskGuard)) { throw "TikTok special-content safety changed: $taskGuard" }
    }
    'TIKTOK_SPECIAL_POLICY_WIRING_AUDIT=PASS'
    if ($taskService.Contains('allowsInput(getWindows(), fresh.windowId, fresh.windowBounds, fresh.page)') -or
        ([regex]::Matches($taskService, 'allowsSemantic\(getWindows\(\), fresh.windowId, fresh.windowBounds, fresh.page\)')).Count -ne 2) {
        throw 'Semantic ad/TikTok actions must not treat the full page as a physical touch corridor.'
    }
    'SEMANTIC_WINDOW_INPUT_SEPARATION_AUDIT=PASS'
    $taskReader = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/detection/ShortsReader.java') -Raw
    if (-not $taskReader.Contains('return snapshot.withIdentity(identity);')) {
        throw 'App routing must preserve snapshot visual eligibility metadata.'
    }
    'SNAPSHOT_METADATA_WIRING_AUDIT=PASS'
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
    $taskApk = Join-Path $taskRoot "app/build/outputs/apk/$BuildType/app-$BuildType.apk"
    if ($BuildType -eq 'release') {
        $taskOutput = Get-Content -LiteralPath (Join-Path $taskRoot 'app/build/outputs/apk/release/output-metadata.json') -Raw | ConvertFrom-Json
        if ($taskOutput.elements.Count -ne 1 -or $taskOutput.elements[0].outputFile -notmatch '^app-release(?:-unsigned)?\.apk$') { throw 'Unexpected release output metadata.' }
        $taskApk = Join-Path $taskRoot ('app/build/outputs/apk/release/' + $taskOutput.elements[0].outputFile)
        & (Join-Path $PSScriptRoot 'verify-release-safety.ps1') -Apk $taskApk
    }
    $taskAapt = Join-Path $taskSdkRoot 'build-tools/35.0.0/aapt2.exe'
    $taskResources = (& $taskAapt dump resources $taskApk | Out-String)
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect packaged APK resources.' }
    $taskIcon = [regex]::Match($taskResources, '(?ms)^\s+resource [^\r\n]+ mipmap/ic_launcher\r?\n(?<body>.*?)(?=^\s+(?:resource|type) |\z)').Groups['body'].Value
    if ($taskIcon -notmatch '\(anydpi(?:-v26)?\)') {
        throw 'APK is missing its API26-compatible launcher icon. Rebuild app:clean and recheck; source presence alone is insufficient.'
    }
    'PACKAGED_LEGACY_ICON_AUDIT=PASS'
    $taskProductJava = Get-ChildItem -LiteralPath (Join-Path $taskRoot 'app/src/main/java') -Filter *.java -Recurse | Get-Content -Raw
    if ($taskProductJava -match 'UpdateInstallInstrumentation|UpdateClientChecks|InstallerArtifactChecks|SettingsUpgradeChecks|upgrade_test_baseline|upgrade_test_identity|final-update\.apk|FIXTURE_READY') {
        throw 'Test-only updater fixture leaked into product source.'
    }
    $taskArchive = [IO.Compression.ZipFile]::OpenRead($taskApk)
    try {
        if ($taskArchive.Entries.FullName -contains 'assets/final-update.apk') { throw 'Fixture APK must never ship inside product APK.' }
    } finally { $taskArchive.Dispose() }
    'UPDATE_FIXTURE_ISOLATION_AUDIT=PASS'
    Get-Item -LiteralPath $taskApk | Select-Object Name, Length
    'APK_SHA256=' + (Get-FileHash -LiteralPath $taskApk -Algorithm SHA256).Hash
    if ($taskJUnitFailed) { throw 'Direct JUnit regression tests failed. Static audits above do not turn this into a full-suite PASS.' }
} finally { Pop-Location }
