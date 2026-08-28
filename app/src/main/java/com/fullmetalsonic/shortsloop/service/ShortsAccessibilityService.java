package com.fullmetalsonic.shortsloop.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.AdvanceGate;
import com.fullmetalsonic.shortsloop.core.PhotoReelTracker;
import com.fullmetalsonic.shortsloop.core.PhotoTransition;
import com.fullmetalsonic.shortsloop.core.AdSkipPolicy;
import com.fullmetalsonic.shortsloop.core.LoopCounter;
import com.fullmetalsonic.shortsloop.core.PlaybackRestart;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.core.LongVideoTracker;
import com.fullmetalsonic.shortsloop.core.ModePolicy;
import com.fullmetalsonic.shortsloop.core.SessionPolicy;
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutPolicy;
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutTracker;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipTracker;
import com.fullmetalsonic.shortsloop.core.LiveTransitionPolicy;
import com.fullmetalsonic.shortsloop.core.LiveTreePolicy;
import com.fullmetalsonic.shortsloop.core.YouTubePageStepPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.YouTubeReader;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.detection.YouTubeWindowGuard;
import com.fullmetalsonic.shortsloop.detection.YouTubePagePosition;
import com.fullmetalsonic.shortsloop.detection.ShortsReader;
import com.fullmetalsonic.shortsloop.detection.InstagramReader;
import com.fullmetalsonic.shortsloop.overlay.FloatingController;
import com.fullmetalsonic.shortsloop.tile.ShortsTileService;
import com.fullmetalsonic.shortsloop.visual.VisualAssistController;
import com.fullmetalsonic.shortsloop.core.VisualLoopTracker;
import java.util.Objects;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class ShortsAccessibilityService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final LoopCounter counter = new LoopCounter();
    private final AdvanceGate gate = new AdvanceGate();
    private final PhotoReelTracker photos = new PhotoReelTracker();
    private final PhotoTransition photoTransition = new PhotoTransition();
    private boolean unresolvedPhotoAttempt;
    private String photoRequestPageKey = "";
    private int photoSlideRequests, photoSlideConfirmed, photoReelRequests, photoReelConfirmed;
    private final PlaybackRestart restart = new PlaybackRestart();
    private int ordinaryRequestWindow = -1;
    private int recoveryEntries, recoveryStarts;
    private long recoveryEnteredAt = -1;
    private final LongVideoTracker longVideo = new LongVideoTracker();
    private boolean pendingLong, unresolvedLongAttempt;
    private int longRequests, confirmedLong, longRequestWindow = -1;
    private Rect longRequestWindowBounds;
    private String longConfirmationDiagnostic = "none";
    private int longRequestRow = YouTubePageStepPolicy.UNKNOWN, longCurrentRow = YouTubePageStepPolicy.UNKNOWN;
    private Rect longRequestPageBounds;
    private AccessibilityNodeInfo longRequestPager;
    private long longRequestedAt = -1;
    private int longRequestIndex = -1;
    private boolean longPagerChanged;
    private boolean longRequestContent;
    private final ClocklessTimeoutTracker timed = new ClocklessTimeoutTracker();
    private final LiveSkipTracker live = new LiveSkipTracker();
    private final ShortsReader reader = new ShortsReader();
    private final YouTubeWindowGuard windowGuard = new YouTubeWindowGuard();
    private SettingsStore store;
    private FloatingController floating;
    private VisualAssistController visual;
    private boolean interacting;
    private long holdUntil;
    private int generation;
    private int lastPageIndex = -1;
    private double lastPosition = -1;
    private double lastDuration = -1;
    private int advanceRequests;
    private int confirmedAdvances;
    private int adRequests, confirmedAds;
    private boolean pendingAd;
    private boolean pendingVisual;
    private boolean pendingTimed;
    private boolean pendingLive, unresolvedLiveAttempt;
    private int liveRequests, confirmedLive;
    private int liveRequestWindow = -1, liveRequestIndex = -1;
    private long liveRequestedAt = -1, lastPollAt;
    private Rect liveRequestWindowBounds;
    private AccessibilityNodeInfo liveRequestPager;
    private int timedRequests, confirmedTimed;
    private int visualRequests, confirmedVisual;
    private String activePackage = "";
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (store == null) return;
            lastPollAt = SystemClock.uptimeMillis();
            try { tick(); }
            catch (RuntimeException ignored) { failClosed("error.query"); }
            if (store != null && store.enabled()) handler.postDelayed(this, store.target() == 0 && !adSkippingEnabled() && !liveSkippingEnabled() && !longSkippingEnabled() && !photoSkippingEnabled() ? 900
                    : visual != null && visual.active() ? 450 : 300);
        }
    };
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        store = new SettingsStore(this);
        // Never resume unattended gestures after service/process reconnection.
        store.enabled(false);
        RuntimeState.connected = true; RuntimeState.blocked = false; RuntimeState.current = 0;
        RuntimeState.status = "off";
        visual = VisualAssistController.create(this, new VisualAssistController.Host() {
            @Override public boolean stillEligible(YouTubeSnapshot expected) { return visualEligible(expected); }
            @Override public void result(YouTubeSnapshot expected, VisualLoopTracker.Result result) {
                if (!visualEligible(expected)) { visual.reset(); return; }
                publish(result.current, visual.status());
                if (result.advance) advanceVisual(expected);
            }
        });
        floating = new FloatingController(this, store, new FloatingController.Listener() {
            @Override public void cycle() { store.target(ModePolicy.next(store.target(), store.ceiling(), store.tapMode())); }
            @Override public void close() { store.enabled(false); }
            @Override public void interaction(boolean active) {
                interacting = active; interruptSession(); holdUntil = SystemClock.uptimeMillis() + 700;
            }
        });
        store.preferences.registerOnSharedPreferenceChangeListener(this);
        applySettings();
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if ("floating_enabled".equals(key)) { updateFloating(); return; }
        // Only explicit execution OFF/ON may re-arm an unconfirmed live request.
        // A floating count tap can first interrupt the gesture and then change target.
        if ("enabled".equals(key)) { unresolvedLiveAttempt = false; unresolvedLongAttempt = false; unresolvedPhotoAttempt = false; }
        else if (unresolvedPhotoAttempt && store.enabled()) {
            failClosed("photo.failed"); return;
        }
        else if (unresolvedLiveAttempt && store.enabled()) {
            failClosed("error.live_settings"); return;
        }
        else if (unresolvedLongAttempt && store.enabled()) {
            failClosed("error.long_settings"); return;
        }
        if ("target".equals(key) || "enabled".equals(key) || "ceiling".equals(key)
                || "tap_mode".equals(key) || "youtube_enabled".equals(key) || "instagram_enabled".equals(key)
                || "skip_ads".equals(key) || "visual_assist".equals(key)
                || "timed_fallback".equals(key) || "fallback_seconds".equals(key)
                || "skip_live".equals(key) || "live_delay_seconds".equals(key)
                || "skip_long".equals(key) || "long_video_seconds".equals(key)
                || (key != null && key.startsWith("photo_"))) applySettings();
    }
    private void updateFloating() {
        if (!store.enabled() || !store.floatingEnabled()) floating.hide();
        else if (!Settings.canDrawOverlays(this)) store.enabled(false);
        else {
            try { floating.show(); floating.update(RuntimeState.current, store.target(), RuntimeState.status, RuntimeState.timedRemainingSeconds); }
            catch (RuntimeException ignored) { store.enabled(false); }
        }
        ShortsTileService.refresh(this);
    }
    private void applySettings() {
        configureLiveTree(activePackage);
        handler.removeCallbacks(poll); invalidate();
        RuntimeState.blocked = false; counter.setTarget(store.target());
        if (!store.enabled()) {
            floating.hide(); publish(0, "off");
        } else if (!store.hasSelectedApps()) {
            store.enabled(false); publish(0, "app.select");
        } else if (store.floatingEnabled() && !Settings.canDrawOverlays(this)) {
            store.enabled(false); publish(0, "permission.overlay");
        } else {
            try { if (store.floatingEnabled()) floating.show(); else floating.hide();
                publish(0, store.target() == 0 ? zeroCountStatus() : "playback.start_wait"); handler.post(poll); }
            catch (RuntimeException ignored) { store.enabled(false); publish(0, "error.overlay"); }
        }
        ShortsTileService.refresh(this);
    }
    private void tick() {
        if (!store.enabled() || RuntimeState.blocked) return;
        if (store.floatingEnabled() && !Settings.canDrawOverlays(this)) { store.enabled(false); return; }
        // Zero ordinary plays still permits independently opted-in ads and live previews.
        if (store.target() == 0 && !adSkippingEnabled() && !liveSkippingEnabled() && !longSkippingEnabled() && !photoSkippingEnabled()) {
            clearLayoutQuery(); invalidate(); publish(0, zeroCountStatus()); return;
        }
        if (!screenAvailable() || interacting || SystemClock.uptimeMillis() < holdUntil) {
            if (gate.pending() || photoTransition.pending()) { failClosed("error.transition_restart"); return; }
            if (restart.active()) { restart.suspend(); publish(0, PlaybackRestart.WAITING); return; }
            invalidate(); publish(0, "screen.waiting"); return;
        }
        YouTubeSnapshot snapshot = snapshot();
        if (RuntimeState.blocked) return;
        long now = SystemClock.uptimeMillis();
        if (photoTransition.pending()) {
            PhotoReelTracker.Action requested = photoTransition.action();
            PhotoTransition.State state = photoTransition.inspect(photoScope(snapshot),
                    snapshot.recognized() ? snapshot.identity : "", snapshot.photo == null ? null : snapshot.photo.position, now,
                    !photoRequestPageKey.isEmpty() && !snapshot.photoPageKey.isEmpty() && !photoRequestPageKey.equals(snapshot.photoPageKey));
            if (state == PhotoTransition.State.FAILED) { failClosed("photo.failed"); return; }
            if (state == PhotoTransition.State.WAITING) { publish(0, "photo.confirming"); return; }
            if (state == PhotoTransition.State.CONFIRMED) {
                if (requested == PhotoReelTracker.Action.SLIDE) photoSlideConfirmed++;
                else { photoReelConfirmed++; confirmedAdvances++; }
                unresolvedPhotoAttempt = false; photos.reset();
            }
        }
        if (gate.pending()) {
            // Ads have no clock. Only a structurally identified, different page
            // can confirm them; a stale page event alone never confirms another ad.
            // A newly appearing clock on the SAME visual page is not a page transition.
            AdvanceGate.State state = pendingLive ? inspectLiveTransition(snapshot, now)
                    : pendingLong ? inspectLongTransition(snapshot, now)
                    : (pendingVisual || pendingTimed)
                    ? gate.inspectRecognizedPage(snapshot.recognized() ? snapshot.identity : "", now)
                    : snapshot.usable()
                    ? gate.inspect(snapshot.identity, snapshot.progress.duration, now)
                    : snapshot.recognized() ? gate.inspectRecognizedPage(snapshot.identity, now) : gate.unavailable(now);
            if (state == AdvanceGate.State.WAITING) {
                publish(pendingAd || pendingTimed || pendingLive || pendingLong ? 0 : store.target(), pendingLong ? LongVideoPolicy.CONFIRMING : pendingLive ? LiveSkipPolicy.STATUS_CONFIRMING : pendingAd ? "ads.confirming"
                        : pendingTimed ? "timed.confirming" : "advance.confirming"); return;
            }
            if (state == AdvanceGate.State.FAILED) { advanceTimedOut(); return; }
            if (state == AdvanceGate.State.CONFIRMED) {
                confirmedAdvances++; if (pendingAd) confirmedAds++;
                if (pendingVisual) confirmedVisual++;
                if (pendingTimed) confirmedTimed++;
                if (pendingLive) { confirmedLive++; unresolvedLiveAttempt = false; releaseLiveRequest(); }
                if (pendingLong) { confirmedLong++; unresolvedLongAttempt = false; }
                pendingLong = false; longVideo.reset(); releaseLongRequest();
                pendingAd = false; pendingVisual = false; pendingTimed = false; pendingLive = false;
                counter.reset(); visual.reset(); timed.reset();
            }
        }
        // Recovery must not fall through into ads/live/timers or issue a retry gesture.
        if (restart.active()) { observeRestart(snapshot, now); return; }
        if (snapshot.photo == null) photos.reset();
        if (!longCandidate(snapshot)) longVideo.reset();
        if (!timedCandidate(snapshot)) timed.reset();
        if (!snapshot.live) live.reset();
        if (snapshot.live) {
            counter.reset(); timed.reset(); visual.reset(); lastPosition = -1; lastDuration = -1;
            if (!liveCandidate(snapshot)) { live.reset(); publish(0, "live.disabled"); return; }
            String key = snapshot.identity + "|" + snapshot.windowId + "|" + snapshot.page.toShortString()
                    + "|" + snapshot.windowBounds.toShortString();
            LiveSkipTracker.Result result = live.observe(key, store.liveDelaySeconds(), now);
            publishState(0, store.liveDelaySeconds() == 0 ? LiveSkipPolicy.STATUS_IMMEDIATE : LiveSkipPolicy.STATUS_DELAYED,
                    result.remainingSeconds());
            if (result.due()) advanceLive(snapshot);
            return;
        }
        if (snapshot.ad) {
            visual.reset();
            counter.reset(); lastPosition = -1; lastDuration = -1;
            if (adSkippingEnabled()) advanceAd(snapshot);
            else publish(0, "ads.disabled");
            return;
        }
        if (snapshot.photo != null) { observePhoto(snapshot, now); return; }
        // Known total duration is an independent filter, not a watch-time timer.
        if (longCandidate(snapshot)) {
            counter.reset(); timed.reset(); visual.reset();
            lastPosition = snapshot.progress.position; lastDuration = snapshot.progress.duration;
            boolean due = longVideo.observe(longKey(snapshot), snapshot.progress, store.longVideoSeconds(), now);
            publish(0, LongVideoPolicy.CHECKING);
            if (due) advanceLong(snapshot);
            return;
        }
        // Ads are independent. Zero still prevents every ordinary/count/timer/visual advance.
        if (store.target() == 0) {
            counter.reset(); timed.reset(); visual.reset(); lastPosition = -1; lastDuration = -1;
            publish(0, snapshot.recognized() ? zeroCountStatus() : snapshot.reason); return;
        }
        // A real clock always wins, including current=0 while awaiting the next start.
        // This path needs a structurally identified, unpaused Instagram video, not merely current=0.
        if (timedCandidate(snapshot)) {
            if (visual.active()) visual.reset();
            counter.reset(); lastPosition = -1; lastDuration = -1;
            String key = snapshot.identity + "|" + snapshot.windowId + "|" + snapshot.page.toShortString()
                    + "|" + snapshot.windowBounds.toShortString();
            ClocklessTimeoutTracker.Result result = timed.observe(key, store.fallbackSeconds(), now);
            publishState(0, result.qualifying() ? "timed.checking" : "timed.waiting",
                    result.remainingSeconds());
            if (result.due()) advanceClockless(snapshot, true);
            return;
        }
        if (snapshot.visualCandidate && store.visualAssist() && store.instagramEnabled()
                && InstagramReader.PACKAGE.equals(activePackage)) {
            counter.reset(); lastPosition = -1; lastDuration = -1;
            visual.observe(snapshot, store.target(), now);
            publish(visual.current(), visual.status()); return;
        }
        if (!snapshot.usable()) { invalidate(); publish(0, snapshot.reason); return; }
        if (visual.active()) visual.reset();
        lastPosition = snapshot.progress.position; lastDuration = snapshot.progress.duration;
        LoopCounter.Result result = counter.observe(snapshot.progress, snapshot.identity, now);
        publish(result.current, result.waitingForStart ? "playback.next_start" : "playback.counting");
        if (result.advance) advance(snapshot);
    }
    private void advanceTimedOut() {
        if (!PlaybackRestart.ordinaryRequest(pendingAd, pendingLive, pendingTimed, pendingVisual, pendingLong)
                || ordinaryRequestWindow < 0 || store.target() <= 0) {
            failClosed("error.advance"); return;
        }
        int window = ordinaryRequestWindow;
        invalidate(); // Discard emitted counts and invalidate callbacks from the old request.
        restart.begin(activePackage, window);
        recoveryEntries++; recoveryEnteredAt = SystemClock.uptimeMillis();
        lastPosition = lastDuration = -1;
        publish(0, PlaybackRestart.WAITING);
    }
    private void observeRestart(YouTubeSnapshot snapshot, long now) {
        if (!store.enabled() || store.target() <= 0 || !store.isSelected(activePackage)
                || !snapshot.usable() || !snapshot.recognized()
                || !restart.accepts(activePackage, snapshot.windowId)
                || snapshot.windowBounds == null || !snapshot.windowBounds.contains(snapshot.page)) {
            restart.suspend(); publish(0, PlaybackRestart.WAITING); return;
        }
        lastPosition = snapshot.progress.position; lastDuration = snapshot.progress.duration;
        String key = snapshot.identity + "|" + snapshot.page.toShortString() + "|" + snapshot.windowBounds.toShortString();
        PlaybackRestart.Start start = restart.observe(snapshot.progress, key, now);
        if (start == null) { publish(0, PlaybackRestart.WAITING); return; }
        counter.reset();
        counter.observe(start.progress, snapshot.identity, start.at);
        LoopCounter.Result result = counter.observe(snapshot.progress, snapshot.identity, now);
        recoveryStarts++;
        // Finding a fresh start is neither a completed play nor a confirmed page transition.
        publish(result.current, PlaybackRestart.COUNTING);
    }
    private YouTubeSnapshot snapshot() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        try {
            String pkg = root == null || root.getPackageName() == null ? "" : root.getPackageName().toString();
            if (SessionPolicy.packageChanged(activePackage, pkg)) {
                interruptSession(); lastPageIndex = -1; activePackage = pkg;
            }
            // YouTube live containers and Instagram photo indices can be non-important nodes.
            // Reacquire after a query-mode change; do not mix two tree shapes.
            if (configureLiveTree(pkg)) return YouTubeSnapshot.unavailable("live.query_ready");
            Rect window = root == null ? null : windowGuard.allowedBounds(getWindows(), root.getWindowId());
            if (window == null)
                return YouTubeSnapshot.unavailable("screen.other_window");
            return reader.read(root, store).inWindow(root.getWindowId(), window);
        } finally { YouTubeReader.recycle(root); }
    }
    private boolean configureLiveTree(String packageName) {
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null || store == null) return false;
        boolean include = LiveTreePolicy.includeLayoutNodes(store.enabled() && !RuntimeState.blocked,
                store.youtubeEnabled(), packageName)
                || com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.includeLayoutNodes(
                        store.enabled() && !RuntimeState.blocked, store.instagramEnabled(), store.photoEnabled(), packageName);
        int flag = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        int flags = include ? info.flags | flag : info.flags & ~flag;
        if (flags == info.flags) return false;
        info.flags = flags;
        setServiceInfo(info);
        return true;
    }
    private boolean screenAvailable() {
        return getSystemService(PowerManager.class).isInteractive() && !getSystemService(KeyguardManager.class).isKeyguardLocked();
    }
    private boolean adSkippingEnabled() {
        return store != null && AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.instagramEnabled());
    }
    private boolean liveSkippingEnabled() {
        return store != null && LiveSkipPolicy.enabled(store.enabled(), store.skipLive(), store.youtubeEnabled());
    }
    private boolean longSkippingEnabled() {
        return store != null && store.enabled() && store.skipLong() && store.hasSelectedApps();
    }
    private boolean photoSkippingEnabled() {
        return store != null && store.enabled() && store.instagramEnabled() && store.photoEnabled();
    }
    private String photoScope(YouTubeSnapshot value) {
        return value.windowId < 0 || value.windowBounds == null ? ""
                : activePackage + "|" + value.windowId + "|" + value.windowBounds.toShortString();
    }
    private void observePhoto(YouTubeSnapshot value, long now) {
        counter.reset(); timed.reset(); visual.reset(); longVideo.reset(); lastPosition = lastDuration = -1;
        if (!photoSkippingEnabled()) { photos.reset(); publish(0, "photo.disabled"); return; }
        if (!InstagramReader.PACKAGE.equals(activePackage) || !value.recognized() || value.windowId < 0
                || value.windowBounds == null || !value.windowBounds.contains(value.page)) {
            photos.reset(); publish(0, "photo.waiting"); return;
        }
        PhotoReelTracker.Result result = photos.observe(photoScope(value) + "|" + value.identity + "|" + value.page.toShortString(),
                value.photo.position, store.photoMode(), store.photoWholeSeconds(), store.photoSlideSeconds(), store.photoFallback(), now);
        publishState(0, result.status(), result.remaining());
        if (result.action() != PhotoReelTracker.Action.NONE) advancePhoto(value, result.action());
    }
    private void advancePhoto(YouTubeSnapshot expected, PhotoReelTracker.Action action) {
        if (!photoSkippingEnabled() || RuntimeState.blocked || gate.pending() || photoTransition.pending()
                || restart.active() || interacting || !screenAvailable() || SystemClock.uptimeMillis() < holdUntil) return;
        YouTubeSnapshot fresh = snapshot();
        if (RuntimeState.blocked || fresh.photoPageKey.isEmpty() || !PhotoGestureDispatcher.samePhoto(expected, fresh)) { photos.reset(); return; }
        photoRequestPageKey = fresh.photoPageKey;
        photoTransition.begin(action, photoScope(fresh), fresh.identity, fresh.photo.position, SystemClock.uptimeMillis());
        unresolvedPhotoAttempt = true;
        int requestGeneration = generation;
        if (action == PhotoReelTracker.Action.SLIDE) photoSlideRequests++;
        else { photoReelRequests++; advanceRequests++; }
        boolean accepted = PhotoGestureDispatcher.dispatch(this, reader, windowGuard, store, fresh, action, floating.bounds(),
                new GestureResultCallback() {
                    @Override public void onCancelled(GestureDescription description) {
                        if (generation == requestGeneration) failClosed("photo.failed");
                    }
                }, handler);
        if (!accepted) { failClosed("photo.failed"); return; }
        publish(0, "photo.confirming");
    }
    private boolean longCandidate(YouTubeSnapshot value) {
        return longSkippingEnabled() && store.isSelected(activePackage) && value != null && value.usable()
                && value.recognized() && value.windowId >= 0 && value.windowBounds != null
                && value.windowBounds.contains(value.page)
                && LongVideoPolicy.qualifies(true, value.progress, store.longVideoSeconds());
    }
    private String longKey(YouTubeSnapshot value) {
        return activePackage + "|" + value.windowId + "|" + longIdentity(value) + "|" + value.page.toShortString()
                + "|" + value.windowBounds.toShortString();
    }
    private String longIdentity(YouTubeSnapshot value) {
        return value.contentIdentity.isEmpty() ? value.identity : "content:" + value.contentIdentity;
    }
    private String longConfirmationIdentity(YouTubeSnapshot value) {
        if (!longRequestContent || value.live || value.ad) return value.identity;
        return value.contentIdentity.isEmpty() ? "" : "content:" + value.contentIdentity;
    }
    private AdvanceGate.State inspectLongTransition(YouTubeSnapshot value, long now) {
        boolean safe = value.recognized() && value.windowId == longRequestWindow && value.windowBounds != null
                && value.windowBounds.equals(longRequestWindowBounds) && value.windowBounds.contains(value.page);
        boolean youtubeContent = longRequestContent && YouTubeReader.PACKAGE.equals(activePackage) && !value.live && !value.ad;
        if (youtubeContent) {
            longCurrentRow = safe ? readYouTubeRow(value) : YouTubePageStepPolicy.UNSAFE;
            safe = safe && YouTubePageStepPolicy.permits(longRequestRow, longCurrentRow);
        }
        longConfirmationDiagnostic = "recognized=" + value.recognized() + " sameWindow=" + (value.windowId == longRequestWindow)
                + " sameBounds=" + Objects.equals(value.windowBounds, longRequestWindowBounds) + " safe=" + safe
                + " duration=" + (value.progress == null ? -1 : value.progress.duration)
                + " requestIndex=" + longRequestIndex + " currentIndex=" + lastPageIndex + " pagerChanged=" + longPagerChanged
                + " requestRow=" + longRequestRow + " currentRow=" + longCurrentRow
                + " contentKey=" + !value.contentIdentity.isEmpty() + " " + gate.transitionDiagnostic(longConfirmationIdentity(value), now);
        String identity = safe ? longConfirmationIdentity(value) : "";
        if (longRequestContent && !value.live && !value.ad)
            return gate.inspectContentPage(identity, safe ? value.progress : null,
                    safe && (longPagerChanged || (youtubeContent && YouTubePageStepPolicy.next(longRequestRow, longCurrentRow))), now);
        return gate.inspectLongPage(identity, safe ? value.progress : null, safe && longPagerChanged, now);
    }
    private int readYouTubeRow(YouTubeSnapshot expected) {
        if (longRequestPageBounds == null || !longRequestPageBounds.equals(expected.page)) return YouTubePageStepPolicy.UNSAFE;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        java.util.List<AccessibilityNodeInfo> pagers = java.util.Collections.emptyList();
        try {
            if (root == null || !root.refresh() || root.getWindowId() != longRequestWindow
                    || !YouTubeReader.PACKAGE.contentEquals(root.getPackageName() == null ? "" : root.getPackageName()))
                return YouTubePageStepPolicy.UNSAFE;
            Rect allowed = windowGuard.allowedBounds(getWindows(), root.getWindowId());
            if (!longRequestWindowBounds.equals(allowed)) return YouTubePageStepPolicy.UNSAFE;
            pagers = root.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler");
            AccessibilityNodeInfo chosen = null;
            for (AccessibilityNodeInfo pager : pagers) if (pager.isVisibleToUser()) {
                if (chosen != null) return YouTubePageStepPolicy.UNSAFE;
                chosen = pager;
            }
            if (chosen == null || longRequestPager == null || !longRequestPager.equals(chosen)) return YouTubePageStepPolicy.UNSAFE;
            int row = YouTubePagePosition.read(chosen, expected.page, expected.windowId);
            // Re-read metadata on this same root after the ordinal: do not combine
            // a previous page's content snapshot with a new page's row number.
            YouTubeSnapshot after = reader.read(root, store);
            if (!after.usable() || !Objects.equals(after.contentIdentity, expected.contentIdentity)
                    || !Objects.equals(after.page, expected.page)) return YouTubePageStepPolicy.UNSAFE;
            if (YouTubePagePosition.read(chosen, expected.page, expected.windowId) != row) return YouTubePageStepPolicy.UNSAFE;
            return row;
        } finally {
            for (AccessibilityNodeInfo pager : pagers) YouTubeReader.recycle(pager);
            YouTubeReader.recycle(root);
        }
    }
    private void advanceLong(YouTubeSnapshot original) {
        if (!longCandidate(original) || RuntimeState.blocked || gate.pending() || restart.active()
                || interacting || !screenAvailable() || SystemClock.uptimeMillis() < holdUntil) return;
        YouTubeSnapshot fresh = snapshot();
        if (RuntimeState.blocked || !longCandidate(fresh) || !longKey(original).equals(longKey(fresh))
                || Math.abs(original.progress.duration - fresh.progress.duration) > 0.5
                || fresh.progress.position < original.progress.position) { longVideo.reset(); return; }
        AccessibilityNodeInfo root = getRootInActiveWindow();
        java.util.List<AccessibilityNodeInfo> pagers = java.util.Collections.emptyList();
        try {
            if (root == null || !root.refresh() || root.getWindowId() != fresh.windowId
                    || !activePackage.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) { longVideo.reset(); return; }
            String pagerId = YouTubeReader.PACKAGE.equals(activePackage)
                    ? "com.google.android.youtube:id/reel_recycler" : InstagramReader.PAGER_ID;
            pagers = root.findAccessibilityNodeInfosByViewId(pagerId);
            AccessibilityNodeInfo chosen = null;
            for (AccessibilityNodeInfo node : pagers) if (node.isVisibleToUser()) {
                if (chosen != null) { longVideo.reset(); return; }
                chosen = node;
            }
            if (chosen == null || !chosen.refresh() || !chosen.isVisibleToUser() || chosen.getWindowId() != fresh.windowId
                    || !pagerId.equals(chosen.getViewIdResourceName())
                    || !activePackage.contentEquals(chosen.getPackageName() == null ? "" : chosen.getPackageName())) { longVideo.reset(); return; }
            Rect pagerBounds = new Rect(); chosen.getBoundsInScreen(pagerBounds);
            if (!pagerBounds.contains(fresh.page)) { longVideo.reset(); return; }
            releaseLongRequest();
            longRequestPager = AccessibilityNodeInfo.obtain(chosen);
            longRequestWindow = fresh.windowId; longRequestWindowBounds = new Rect(fresh.windowBounds);
            longRequestIndex = lastPageIndex;
            longRequestContent = !fresh.contentIdentity.isEmpty();
            longRequestPageBounds = new Rect(fresh.page);
            if (longRequestContent && YouTubeReader.PACKAGE.equals(activePackage)) {
                longRequestRow = readYouTubeRow(fresh);
                if (longRequestRow == YouTubePageStepPolicy.UNSAFE) { releaseLongRequest(); longVideo.reset(); return; }
            }
            longVideo.consume(); dispatchPageSwipe(fresh, false, true);
        } finally {
            for (AccessibilityNodeInfo pager : pagers) YouTubeReader.recycle(pager);
            YouTubeReader.recycle(root);
        }
    }
    private void releaseLongRequest() {
        YouTubeReader.recycle(longRequestPager); longRequestPager = null;
        longRequestedAt = -1; longRequestIndex = longRequestWindow = -1; longRequestWindowBounds = null; longPagerChanged = false; longRequestContent = false;
        longRequestRow = longCurrentRow = YouTubePageStepPolicy.UNKNOWN; longRequestPageBounds = null;
    }
    private boolean liveCandidate(YouTubeSnapshot value) {
        return liveSkippingEnabled() && YouTubeReader.PACKAGE.equals(activePackage) && value != null && value.live
                && value.recognized() && value.windowId >= 0 && value.windowBounds != null && value.windowBounds.contains(value.page);
    }
    private String zeroCountStatus() {
        if (photoSkippingEnabled()) return "photo.rules";
        return LongVideoPolicy.zeroCountStatus(adSkippingEnabled(), liveSkippingEnabled(), longSkippingEnabled());
    }
    private AdvanceGate.State inspectLiveTransition(YouTubeSnapshot value, long now) {
        if (value.windowId != liveRequestWindow || value.windowBounds == null
                || !value.windowBounds.equals(liveRequestWindowBounds)) return gate.inspectLivePage("", now);
        if (value.live) return gate.inspectLivePage(value.recognized() ? value.identity : "", now);
        return value.usable() ? gate.inspectRecognizedPage(value.identity, now) : gate.inspectLivePage("", now);
    }
    private void releaseLiveRequest() {
        YouTubeReader.recycle(liveRequestPager); liveRequestPager = null;
        liveRequestedAt = -1; liveRequestWindow = liveRequestIndex = -1; liveRequestWindowBounds = null;
    }
    private void advanceLive(YouTubeSnapshot original) {
        if (!liveCandidate(original) || RuntimeState.blocked || gate.pending() || interacting || !screenAvailable()
                || SystemClock.uptimeMillis() < holdUntil) return;
        YouTubeSnapshot fresh = snapshot();
        if (RuntimeState.blocked || !liveCandidate(fresh) || !Objects.equals(original.identity, fresh.identity)
                || original.windowId != fresh.windowId || !original.page.equals(fresh.page)
                || !original.windowBounds.equals(fresh.windowBounds)) { live.reset(); return; }
        AccessibilityNodeInfo root = getRootInActiveWindow();
        java.util.List<AccessibilityNodeInfo> pagers = java.util.Collections.emptyList();
        try {
            if (root == null || root.getWindowId() != fresh.windowId || !root.refresh()
                    || !YouTubeReader.PACKAGE.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) { live.reset(); return; }
            Rect currentWindow = windowGuard.allowedBounds(getWindows(), root.getWindowId());
            if (currentWindow == null || !currentWindow.equals(fresh.windowBounds)) { live.reset(); return; }
            YouTubeSnapshot verified = reader.read(root, store).inWindow(root.getWindowId(), currentWindow);
            if (!liveCandidate(verified) || !Objects.equals(fresh.identity, verified.identity)
                    || !fresh.page.equals(verified.page) || !screenAvailable() || RuntimeState.blocked) { live.reset(); return; }
            pagers = root.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler");
            AccessibilityNodeInfo chosen = null;
            for (AccessibilityNodeInfo node : pagers) if (node.isVisibleToUser()) {
                if (chosen != null) { live.reset(); return; }
                chosen = node;
            }
            if (chosen == null || !chosen.refresh() || !chosen.isVisibleToUser() || chosen.getWindowId() != verified.windowId
                    || !"com.google.android.youtube:id/reel_recycler".equals(chosen.getViewIdResourceName())
                    || !YouTubeReader.PACKAGE.contentEquals(chosen.getPackageName() == null ? "" : chosen.getPackageName())) { live.reset(); return; }
            Rect pagerBounds = new Rect(); chosen.getBoundsInScreen(pagerBounds);
            if (!pagerBounds.contains(verified.page)) { live.reset(); return; }
            releaseLiveRequest();
            liveRequestPager = AccessibilityNodeInfo.obtain(chosen);
            liveRequestWindow = fresh.windowId; liveRequestIndex = lastPageIndex;
            liveRequestWindowBounds = new Rect(fresh.windowBounds);
            dispatchPageSwipe(verified, true, false);
        } finally {
            for (AccessibilityNodeInfo pager : pagers) YouTubeReader.recycle(pager);
            YouTubeReader.recycle(root);
        }
    }
    private boolean visualEligible(YouTubeSnapshot expected) {
        if (store == null || !store.enabled() || !store.visualAssist() || !store.instagramEnabled()
                || store.timedFallback()
                || store.target() <= 0 || RuntimeState.blocked || gate.pending() || interacting
                || (store.floatingEnabled() && !Settings.canDrawOverlays(this))
                || SystemClock.uptimeMillis() < holdUntil || !screenAvailable()) return false;
        return VisualAssistController.samePage(expected, snapshot()) && !RuntimeState.blocked;
    }
    private boolean timedCandidate(YouTubeSnapshot value) {
        return store != null && ClocklessTimeoutPolicy.enabled(store.enabled(), store.target(),
                store.instagramEnabled(), store.timedFallback())
                && InstagramReader.PACKAGE.equals(activePackage) && value != null && !value.usable()
                && value.visualCandidate && !value.ad && value.recognized() && value.windowId >= 0
                && value.windowBounds != null && value.windowBounds.contains(value.page);
    }
    private boolean timedEligible(YouTubeSnapshot expected) {
        if (!timedCandidate(expected) || RuntimeState.blocked || gate.pending() || interacting
                || (store.floatingEnabled() && !Settings.canDrawOverlays(this))
                || SystemClock.uptimeMillis() < holdUntil || !screenAvailable()) return false;
        YouTubeSnapshot fresh = snapshot();
        return !RuntimeState.blocked && timedCandidate(fresh) && VisualAssistController.samePage(expected, fresh);
    }
    /** Clockless requests use the identified pager and STRICT different-identity confirmation. */
    private void advanceVisual(YouTubeSnapshot expected) {
        advanceClockless(expected, false);
    }
    private void advanceClockless(YouTubeSnapshot expected, boolean timeout) {
        if (!(timeout ? timedEligible(expected) : visualEligible(expected))) { visual.reset(); timed.reset(); return; }
        String mode = timeout ? "timed" : "estimate";
        AccessibilityNodeInfo root = getRootInActiveWindow();
        java.util.List<AccessibilityNodeInfo> pagers = java.util.Collections.emptyList();
        try {
            if (root == null || !root.refresh() || root.getWindowId() != expected.windowId
                    || !InstagramReader.PACKAGE.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) {
                visual.reset(); timed.reset(); return;
            }
            Rect window = windowGuard.allowedBounds(getWindows(), root.getWindowId());
            if (window == null) { visual.reset(); timed.reset(); return; }
            YouTubeSnapshot fresh = reader.read(root, store).inWindow(root.getWindowId(), window);
            if (!VisualAssistController.samePage(expected, fresh)) { visual.reset(); timed.reset(); return; }
            pagers = root.findAccessibilityNodeInfosByViewId(InstagramReader.PAGER_ID);
            AccessibilityNodeInfo pager = null;
            for (AccessibilityNodeInfo candidate : pagers) if (candidate.isVisibleToUser()) {
                if (pager != null) { failClosed("error.multiple_reels"); return; }
                pager = candidate;
            }
            if (pager == null || !pager.refresh() || !pager.isVisibleToUser() || !pager.isScrollable()
                    || !InstagramReader.PAGER_ID.equals(pager.getViewIdResourceName())
                    || pager.getWindowId() != expected.windowId
                    || !InstagramReader.PACKAGE.contentEquals(pager.getPackageName() == null ? "" : pager.getPackageName())) {
                failClosed(mode + ".missing_action"); return;
            }
            Rect bounds = new Rect(); pager.getBoundsInScreen(bounds);
            if (!bounds.contains(fresh.page)) { failClosed(mode + ".invalid_bounds"); return; }
            gate.begin(fresh.identity, -1, SystemClock.uptimeMillis());
            pendingVisual = !timeout; pendingTimed = timeout; pendingAd = false;
            advanceRequests++; if (timeout) timedRequests++; else visualRequests++;
            visual.reset(); timed.reset();
            if (!pager.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                failClosed(mode + ".request_rejected"); return;
            }
            publish(timeout ? 0 : store.target(), mode + ".confirming");
        } finally {
            for (AccessibilityNodeInfo pager : pagers) YouTubeReader.recycle(pager);
            YouTubeReader.recycle(root);
        }
    }
    /** Semantic pager action avoids clicking an advertisement's CTA/embedded browser. */
    private void advanceAd(YouTubeSnapshot original) {
        if (!adSkippingEnabled()
                || RuntimeState.blocked || gate.pending() || interacting || !screenAvailable()) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        java.util.List<AccessibilityNodeInfo> pagers = java.util.Collections.emptyList();
        try {
            if (root == null || !InstagramReader.PACKAGE.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())
                    || !windowGuard.allows(getWindows(), root.getWindowId()) || !root.refresh()) return;
            YouTubeSnapshot fresh = reader.read(root, store);
            if (!fresh.ad || !fresh.recognized() || !Objects.equals(original.identity, fresh.identity)) return;
            pagers = root.findAccessibilityNodeInfosByViewId(InstagramReader.PAGER_ID);
            AccessibilityNodeInfo pager = null;
            for (AccessibilityNodeInfo candidate : pagers) if (candidate.isVisibleToUser()) {
                if (pager != null) { failClosed("error.multiple_reels"); return; }
                pager = candidate;
            }
            if (pager == null || !pager.refresh() || !pager.isVisibleToUser() || !pager.isScrollable()
                    || !InstagramReader.PAGER_ID.equals(pager.getViewIdResourceName())
                    || !InstagramReader.PACKAGE.contentEquals(pager.getPackageName() == null ? "" : pager.getPackageName())) {
                failClosed("error.ads_action"); return;
            }
            Rect bounds = new Rect(); pager.getBoundsInScreen(bounds);
            if (!bounds.contains(fresh.page)) { failClosed("error.ads_screen"); return; }
            gate.begin(fresh.identity, -1, SystemClock.uptimeMillis()); pendingAd = true;
            advanceRequests++; adRequests++;
            if (!pager.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                failClosed("error.ads_rejected"); return;
            }
            publish(0, "ads.confirming");
        } finally {
            for (AccessibilityNodeInfo pager : pagers) YouTubeReader.recycle(pager);
            YouTubeReader.recycle(root);
        }
    }
    private void advance(YouTubeSnapshot original) {
        if (!store.enabled() || store.target() == 0 || interacting || !screenAvailable()) return;
        YouTubeSnapshot fresh = snapshot();
        if (!fresh.usable() || !Objects.equals(original.identity, fresh.identity)
                || original.progress.duration != fresh.progress.duration
                || fresh.windowId != original.windowId || !Objects.equals(fresh.page, original.page)
                || fresh.windowBounds == null || !fresh.windowBounds.contains(fresh.page)) { invalidate(); return; }
        if (RuntimeState.blocked || gate.pending()) return;
        dispatchPageSwipe(fresh, false, false);
    }
    private void dispatchPageSwipe(YouTubeSnapshot fresh, boolean forLive, boolean forLong) {
        Rect page = fresh.page;
        Rect overlay = floating.bounds();
        float startY = page.top + page.height() * 0.75f;
        float endY = page.top + page.height() * 0.25f;
        float x = -1;
        for (float fraction : new float[]{0.4f, 0.65f, 0.18f, 0.8f}) {
            int candidate = page.left + Math.round(page.width() * fraction);
            Rect corridor = new Rect(candidate - 12, (int) endY, candidate + 12, (int) startY);
            if (!Rect.intersects(overlay, corridor)) { x = candidate; break; }
        }
        if (x < 0) { failClosed("error.floating_obstructs"); return; }
        Path path = new Path(); path.moveTo(x, startY); path.lineTo(x, endY);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 280)).build();
        long requestAt = SystemClock.uptimeMillis();
        gate.begin(forLong ? longIdentity(fresh) : fresh.identity, forLive ? -1 : fresh.progress.duration, requestAt);
        ordinaryRequestWindow = forLive || forLong ? -1 : fresh.windowId;
        pendingLive = forLive;
        pendingLong = forLong;
        if (forLong) { longRequestedAt = requestAt; longRequests++; unresolvedLongAttempt = true; }
        if (forLive) { liveRequestedAt = requestAt; liveRequests++; unresolvedLiveAttempt = true; }
        int requestGeneration = generation;
        advanceRequests++;
        boolean accepted = dispatchGesture(gesture, new GestureResultCallback() {
            @Override public void onCompleted(GestureDescription description) {
                // Completion means the gesture ran, not that YouTube changed videos.
                if (generation == requestGeneration) publish(forLive || forLong ? 0 : store.target(), forLong ? LongVideoPolicy.CONFIRMING : forLive ? LiveSkipPolicy.STATUS_CONFIRMING : "shorts.confirming");
            }
            @Override public void onCancelled(GestureDescription description) {
                if (generation == requestGeneration) failClosed("error.cancelled");
            }
        }, handler);
        if (!accepted) failClosed("error.rejected");
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (store == null || !store.enabled() || event == null) return;
        String eventPackage = event.getPackageName() == null ? "" : event.getPackageName().toString();
        if (!RuntimeState.blocked && (liveSkippingEnabled() || pendingLive)
                && store.isSelected(eventPackage) && YouTubeReader.PACKAGE.equals(eventPackage)) {
            int type = event.getEventType();
            if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    || (type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (live.active() || pendingLive))
                    || type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                handler.removeCallbacks(poll);
                handler.postDelayed(poll, Math.max(0, 100 - (SystemClock.uptimeMillis() - lastPollAt)));
            }
        }
        if (!store.isSelected(eventPackage) || !eventPackage.equals(activePackage)) return;
        if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_SCROLLED
                && event.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED) return;
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;
        try {
            String id = source.getViewIdResourceName();
            if (InstagramReader.PACKAGE.equals(eventPackage) && (photos.active() || photoTransition.pending())) {
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
                        || (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED && !photoTransition.pending())) {
                    interruptSession(); holdUntil = SystemClock.uptimeMillis() + 900;
                }
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED && (longVideo.active() || pendingLong)) {
                interruptSession(); holdUntil = SystemClock.uptimeMillis() + 900;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
                    && YouTubeReader.PACKAGE.equals(eventPackage) && (live.active() || pendingLive)) {
                interruptSession(); holdUntil = SystemClock.uptimeMillis() + 900;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
                    && InstagramReader.PACKAGE.equals(eventPackage)
                    && ((visual != null && visual.active()) || timed.active())) {
                interruptSession(); holdUntil = SystemClock.uptimeMillis() + 900;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
                    && "android.widget.SeekBar".contentEquals(source.getClassName() == null ? "" : source.getClassName())) {
                interruptSession(); holdUntil = SystemClock.uptimeMillis() + 900;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED
                    && ("com.google.android.youtube:id/reel_recycler".equals(id) || InstagramReader.PAGER_ID.equals(id))) {
                // Photo requests use exact index / stable different identity, never this generic event flag.
                if (photoTransition.pending()) return;
                int index = event.getFromIndex();
                if (pendingLive && gate.pending()) {
                    // A rejected late event must not poison the baseline used by a subsequent fresh event.
                    if (source.refresh() && LiveTransitionPolicy.accepts(liveRequestedAt, event.getEventTime(),
                            SystemClock.uptimeMillis(), liveRequestWindow, source.getWindowId(), liveRequestIndex, index,
                            liveRequestPager != null && liveRequestPager.equals(source))) {
                        gate.pageChanged(); lastPageIndex = index;
                    }
                } else if (pendingLong && gate.pending()) {
                    if (source.refresh() && LiveTransitionPolicy.accepts(longRequestedAt, event.getEventTime(),
                            SystemClock.uptimeMillis(), longRequestWindow, source.getWindowId(), longRequestIndex, index,
                            longRequestPager != null && longRequestPager.equals(source))) {
                        longPagerChanged = true; lastPageIndex = index;
                    }
                } else {
                    if (index >= 0 && lastPageIndex >= 0 && index != lastPageIndex) {
                        if (gate.pending()) gate.pageChanged();
                        else if (restart.active()) restart.suspend();
                        else invalidate();
                    }
                    if (index >= 0) lastPageIndex = index;
                }
            }
        } finally { YouTubeReader.recycle(source); }
    }
    private void invalidate() {
        photos.reset(); photoTransition.reset();
        photoRequestPageKey = "";
        restart.cancel(); ordinaryRequestWindow = -1;
        longVideo.reset(); pendingLong = false; releaseLongRequest();
        generation++; counter.reset(); gate.cancel(); pendingAd = false; pendingVisual = false; pendingTimed = false; pendingLive = false;
        live.reset(); releaseLiveRequest();
        timed.reset(); RuntimeState.timedRemainingSeconds = -1;
        if (visual != null) visual.reset(); RuntimeState.current = 0;
    }
    private void interruptSession() {
        if (photoTransition.pending()) { failClosed("photo.failed"); return; }
        if (restart.active()) { restart.suspend(); counter.reset(); return; }
        if (gate.interrupt() == AdvanceGate.State.FAILED) failClosed("error.transition");
        else invalidate();
    }
    private void failClosed(String message) {
        invalidate(); RuntimeState.blocked = true; clearLayoutQuery(); publish(0, "blocked:" + message); ShortsTileService.refresh(this);
    }
    private void clearLayoutQuery() {
        // The framework may already be disconnecting. There must be no gesture or
        // recovery attempt here, and failure to contact it must not restart polling.
        try { configureLiveTree(""); } catch (RuntimeException ignored) { }
    }
    private void publish(int current, String message) {
        publishState(current, message, -1);
    }
    private void publishState(int current, String message, int remainingSeconds) {
        RuntimeState.current = current; RuntimeState.status = message;
        RuntimeState.timedRemainingSeconds = remainingSeconds;
        if (floating != null && store != null) floating.update(current, store.target(), message, remainingSeconds);
    }
    @Override public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config); interruptSession(); holdUntil = SystemClock.uptimeMillis() + 1000;
        if (floating != null) floating.configurationChanged();
    }
    @Override public void onInterrupt() { if (store != null) store.enabled(false); }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null); invalidate();
        clearLayoutQuery();
        RuntimeState.connected = false; RuntimeState.current = 0; RuntimeState.status = "service.disconnected";
        if (store != null) { store.preferences.unregisterOnSharedPreferenceChangeListener(this); store.enabled(false); }
        if (floating != null) floating.hide();
        reader.close();
        store = null; ShortsTileService.refresh(this); super.onDestroy();
    }
    /** Shell diagnostics: numeric playback state only, no title, URL, account or UI tree. */
    @Override protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("ShortsLoop " + com.fullmetalsonic.shortsloop.BuildConfig.VERSION_NAME);
        writer.println("connected=" + RuntimeState.connected + " enabled=" + (store != null && store.enabled())
                + " target=" + (store == null ? -1 : store.target()) + " current=" + RuntimeState.current
                + " blocked=" + RuntimeState.blocked);
        writer.println("position=" + lastPosition + " duration=" + lastDuration + " pending=" + gate.pending()
                + " requests=" + advanceRequests + " confirmed=" + confirmedAdvances);
        writer.println("status=" + RuntimeState.status);
        writer.println("photoEnabled=" + (store != null && store.photoEnabled()) + " photoMode=" + (store == null ? -1 : store.photoMode())
                + " photoWholeSeconds=" + (store == null ? -1 : store.photoWholeSeconds())
                + " photoSlideSeconds=" + (store == null ? -1 : store.photoSlideSeconds())
                + " photoFallback=" + (store != null && store.photoFallback()) + " photoPending=" + photoTransition.pending()
                + " photoSlideRequests=" + photoSlideRequests + " photoSlideConfirmed=" + photoSlideConfirmed
                + " photoReelRequests=" + photoReelRequests + " photoReelConfirmed=" + photoReelConfirmed);
        writer.println("counter=" + counter.diagnostic() + " generation=" + generation);
        writer.println("recovering=" + restart.active() + " recoveryEntries=" + recoveryEntries
                + " recoveryStarts=" + recoveryStarts + " recoveryEnteredAt=" + recoveryEnteredAt);
        writer.println("skipLong=" + (store != null && store.skipLong()) + " longSeconds=" + (store == null ? -1 : store.longVideoSeconds())
                + " longRequests=" + longRequests + " longConfirmed=" + confirmedLong + " pendingLong=" + pendingLong);
        writer.println("longConfirmation=" + longConfirmationDiagnostic);
        writer.println("ceiling=" + (store == null ? -1 : store.ceiling()) + " tapMode=" + (store == null ? -1 : store.tapMode())
                + " floating=" + (store != null && store.floatingEnabled()) + " app=" + activePackage);
        writer.println("ads=" + (store != null && store.skipAds()) + " adRequests=" + adRequests + " adConfirmed=" + confirmedAds);
        writer.println("skipLive=" + (store != null && store.skipLive()) + " liveDelaySeconds=" + (store == null ? 0 : store.liveDelaySeconds())
                + " liveRequests=" + liveRequests + " liveConfirmed=" + confirmedLive + " pendingLive=" + pendingLive + " " + live.diagnostic());
        writer.println("timedEnabled=" + (store != null && store.timedFallback())
                + " timedSeconds=" + (store == null ? -1 : store.fallbackSeconds())
                + " timedRemaining=" + RuntimeState.timedRemainingSeconds
                + " timedRequests=" + timedRequests + " timedConfirmed=" + confirmedTimed
                + " pendingTimed=" + pendingTimed + " " + timed.diagnostic());
        writer.println((visual == null ? "visual=false" : visual.diagnostic())
                + " visualEnabled=" + (store != null && store.visualAssist())
                + " visualRequests=" + visualRequests + " visualConfirmed=" + confirmedVisual);
    }
}
