$ErrorActionPreference = 'Stop'
$photoRoot = Split-Path -Parent $PSScriptRoot
$photoService = Get-Content -LiteralPath (Join-Path $photoRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/service/ShortsAccessibilityService.java') -Raw
$photoDispatch = Get-Content -LiteralPath (Join-Path $photoRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/service/PhotoGestureDispatcher.java') -Raw
$photoReader = Get-Content -LiteralPath (Join-Path $photoRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/detection/InstagramReader.java') -Raw
if (-not $photoReader.Contains('return withPageIdentity(YouTubeSnapshot.advertisement(photo.page),') -or
    -not $photoReader.Contains('return withPageIdentity(YouTubeSnapshot.advertisement(singleVideo ? page : pager),')) {
    throw 'Recognized ads must preserve available photo transition page evidence.'
}
if ($photoService -notmatch '(?s)PhotoReelPolicy\.includeLayoutNodes\(\s*store\.enabled\(\) && !RuntimeState\.blocked, store\.instagramEnabled\(\), store\.photoEnabled\(\), packageName\)' -or
    $photoService -notmatch 'if \(configureLiveTree\(pkg\)\) return YouTubeSnapshot.unavailable') {
    throw 'Photo index tree mode must be opt-in and reacquired before reading.'
}
foreach ($photoGuard in @('if (photoTransition.pending()) {', 'else if (unresolvedPhotoAttempt && store.enabled())',
    'photos.reset(); photoTransition.reset();', 'if (photoTransition.pending()) { failClosed("photo.failed"); return; }',
    'if (!accepted) { failClosed("photo.failed"); return; }', 'snapshot.photo == null ? null : snapshot.photo.position',
    'store.enabled() && store.instagramEnabled() && store.photoEnabled()', 'if (snapshot.photo != null) { observePhoto(snapshot, now); return; }')) {
    if (-not $photoService.Contains($photoGuard)) { throw "Photo safety wiring missing: $photoGuard" }
}
if (-not $photoService.Contains('!photoRequestPageKey.isEmpty() && !snapshot.photoPageKey.isEmpty() && !photoRequestPageKey.equals(snapshot.photoPageKey)')) {
    throw 'Photo vertical confirmation needs independent nonempty page-node change.'
}
if ($photoService.IndexOf('if (snapshot.photo != null) { observePhoto(snapshot, now); return; }') -lt $photoService.IndexOf('if (restart.active()) { observeRestart(snapshot, now); return; }') -or
    $photoService.IndexOf('if (snapshot.photo != null) { observePhoto(snapshot, now); return; }') -gt $photoService.IndexOf('if (store.target() == 0) {')) {
    throw 'Photo rules must not bypass ordinary recovery, and must remain independent of repeat zero.'
}
foreach ($photoGuard in @('a.photo.position.equals(b.photo.position)', '!samePhoto(expected, fresh)',
    'guard.allowedBounds(service.getWindows(), root.getWindowId())', '!store.photoEnabled()',
    'Rect.intersects(overlay, corridor)', 'chosen.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)',
    'fresh.photo.position.current() >= fresh.photo.position.total()')) {
    if (-not $photoDispatch.Contains($photoGuard)) { throw "Photo dispatch guard missing: $photoGuard" }
}
if ($photoDispatch -match 'ACTION_CLICK|dispatchPageSwipe|Thread.sleep|while\s*\(') { throw 'Unexpected photo click/retry path.' }
'PHOTO_SAFETY_WIRING_AUDIT=PASS'
