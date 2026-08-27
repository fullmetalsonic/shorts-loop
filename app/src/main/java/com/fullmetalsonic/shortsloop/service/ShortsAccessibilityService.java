package com.fullmetalsonic.shortsloop.service;

import android.accessibilityservice.AccessibilityService;
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
import com.fullmetalsonic.shortsloop.core.AdSkipPolicy;
import com.fullmetalsonic.shortsloop.core.LoopCounter;
import com.fullmetalsonic.shortsloop.core.ModePolicy;
import com.fullmetalsonic.shortsloop.core.SessionPolicy;
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutPolicy;
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutTracker;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.YouTubeReader;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.detection.YouTubeWindowGuard;
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
    private final ClocklessTimeoutTracker timed = new ClocklessTimeoutTracker();
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
    private int timedRequests, confirmedTimed;
    private int visualRequests, confirmedVisual;
    private String activePackage = "";
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (store == null) return;
            try { tick(); }
            catch (RuntimeException ignored) { failClosed("화면 조회 오류 · 껐다 켜 주세요"); }
            if (store != null && store.enabled()) handler.postDelayed(this, store.target() == 0 && !adSkippingEnabled() ? 900
                    : visual != null && visual.active() ? 450 : 300);
        }
    };
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        store = new SettingsStore(this);
        // Never resume unattended gestures after service/process reconnection.
        store.enabled(false);
        RuntimeState.connected = true; RuntimeState.blocked = false; RuntimeState.current = 0;
        RuntimeState.status = "꺼짐";
        visual = new VisualAssistController(this, new VisualAssistController.Host() {
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
        if ("target".equals(key) || "enabled".equals(key) || "ceiling".equals(key)
                || "tap_mode".equals(key) || "youtube_enabled".equals(key) || "instagram_enabled".equals(key)
                || "skip_ads".equals(key) || "visual_assist".equals(key)
                || "timed_fallback".equals(key) || "fallback_seconds".equals(key)) applySettings();
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
        handler.removeCallbacks(poll); invalidate();
        RuntimeState.blocked = false; counter.setTarget(store.target());
        if (!store.enabled()) {
            floating.hide(); publish(0, "꺼짐");
        } else if (!store.hasSelectedApps()) {
            store.enabled(false); publish(0, "사용할 앱을 선택해 주세요");
        } else if (store.floatingEnabled() && !Settings.canDrawOverlays(this)) {
            store.enabled(false); publish(0, "다른 앱 위 표시 권한 필요");
        } else {
            try { if (store.floatingEnabled()) floating.show(); else floating.hide();
                publish(0, store.target() == 0 ? zeroCountStatus() : "쇼츠·릴스 시작 대기"); handler.post(poll); }
            catch (RuntimeException ignored) { store.enabled(false); publish(0, "플로팅 표시 실패 · 권한 확인"); }
        }
        ShortsTileService.refresh(this);
    }
    private void tick() {
        if (!store.enabled() || RuntimeState.blocked) return;
        if (store.floatingEnabled() && !Settings.canDrawOverlays(this)) { store.enabled(false); return; }
        // With zero plays, keep reading ONLY when the separately opted-in ad feature needs it.
        if (store.target() == 0 && !adSkippingEnabled()) { invalidate(); publish(0, zeroCountStatus()); return; }
        if (!screenAvailable() || interacting || SystemClock.uptimeMillis() < holdUntil) {
            if (gate.pending()) { failClosed("전환 중 화면 변경 · 다시 켜 주세요"); return; }
            invalidate(); publish(0, "화면·조작 대기"); return;
        }
        YouTubeSnapshot snapshot = snapshot();
        if (RuntimeState.blocked) return;
        long now = SystemClock.uptimeMillis();
        if (gate.pending()) {
            // Ads have no clock. Only a structurally identified, different page
            // can confirm them; a stale page event alone never confirms another ad.
            // A newly appearing clock on the SAME visual page is not a page transition.
            AdvanceGate.State state = (pendingVisual || pendingTimed)
                    ? gate.inspectRecognizedPage(snapshot.recognized() ? snapshot.identity : "", now)
                    : snapshot.usable()
                    ? gate.inspect(snapshot.identity, snapshot.progress.duration, now)
                    : snapshot.recognized() ? gate.inspectRecognizedPage(snapshot.identity, now) : gate.unavailable(now);
            if (state == AdvanceGate.State.WAITING) {
                publish(pendingAd || pendingTimed ? 0 : store.target(), pendingAd ? "광고 넘김 확인 중"
                        : pendingTimed ? "시간제 · 다음 영상 확인 중" : "다음 영상 확인 중"); return;
            }
            if (state == AdvanceGate.State.FAILED) { failClosed("넘김 확인 실패 · 껐다 켜 주세요"); return; }
            if (state == AdvanceGate.State.CONFIRMED) {
                confirmedAdvances++; if (pendingAd) confirmedAds++;
                if (pendingVisual) confirmedVisual++;
                if (pendingTimed) confirmedTimed++;
                pendingAd = false; pendingVisual = false; pendingTimed = false;
                counter.reset(); visual.reset(); timed.reset();
            }
        }
        if (!timedCandidate(snapshot)) timed.reset();
        if (snapshot.ad) {
            visual.reset();
            counter.reset(); lastPosition = -1; lastDuration = -1;
            if (adSkippingEnabled()) advanceAd(snapshot);
            else publish(0, "광고 · 바로 넘기기 꺼짐");
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
            publishState(0, result.qualifying() ? "시간제 · 진행 정보 확인 중" : "시간제 · 설정 시간 후 넘김",
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
        publish(result.current, result.waitingForStart ? "다음 처음 재생부터 계산" : "재생 횟수 확인 중");
        if (result.advance) advance(snapshot);
    }
    private YouTubeSnapshot snapshot() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        try {
            String pkg = root == null || root.getPackageName() == null ? "" : root.getPackageName().toString();
            if (SessionPolicy.packageChanged(activePackage, pkg)) {
                interruptSession(); lastPageIndex = -1; activePackage = pkg;
            }
            Rect window = root == null ? null : windowGuard.allowedBounds(getWindows(), root.getWindowId());
            if (window == null)
                return YouTubeSnapshot.unavailable("다른 창·빠른 설정·작은 화면 · 대기");
            return reader.read(root, store).inWindow(root.getWindowId(), window);
        } finally { YouTubeReader.recycle(root); }
    }
    private boolean screenAvailable() {
        return getSystemService(PowerManager.class).isInteractive() && !getSystemService(KeyguardManager.class).isKeyguardLocked();
    }
    private boolean adSkippingEnabled() {
        return store != null && AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.instagramEnabled());
    }
    private String zeroCountStatus() {
        return adSkippingEnabled() ? "광고만 자동 넘김 · 일반·시간제 중지" : "0회 · 반복·시간제 중지, 광고 꺼짐";
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
        String mode = timeout ? "시간제" : "화면 추정";
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
                if (pager != null) { failClosed("릴스 화면이 여러 개 · 다시 켜 주세요"); return; }
                pager = candidate;
            }
            if (pager == null || !pager.refresh() || !pager.isVisibleToUser() || !pager.isScrollable()
                    || !InstagramReader.PAGER_ID.equals(pager.getViewIdResourceName())
                    || pager.getWindowId() != expected.windowId
                    || !InstagramReader.PACKAGE.contentEquals(pager.getPackageName() == null ? "" : pager.getPackageName())) {
                failClosed(mode + " 넘김 동작 없음"); return;
            }
            Rect bounds = new Rect(); pager.getBoundsInScreen(bounds);
            if (!bounds.contains(fresh.page)) { failClosed(mode + " 넘김 범위 확인 실패"); return; }
            gate.begin(fresh.identity, -1, SystemClock.uptimeMillis());
            pendingVisual = !timeout; pendingTimed = timeout; pendingAd = false;
            advanceRequests++; if (timeout) timedRequests++; else visualRequests++;
            visual.reset(); timed.reset();
            if (!pager.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                failClosed(mode + " 넘김 요청 거부 · 다시 켜 주세요"); return;
            }
            publish(timeout ? 0 : store.target(), mode + " · 다음 영상 확인 중");
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
                if (pager != null) { failClosed("릴스 화면이 여러 개 · 다시 켜 주세요"); return; }
                pager = candidate;
            }
            if (pager == null || !pager.refresh() || !pager.isVisibleToUser() || !pager.isScrollable()
                    || !InstagramReader.PAGER_ID.equals(pager.getViewIdResourceName())
                    || !InstagramReader.PACKAGE.contentEquals(pager.getPackageName() == null ? "" : pager.getPackageName())) {
                failClosed("광고 넘김 동작을 찾지 못함 · 다시 켜 주세요"); return;
            }
            Rect bounds = new Rect(); pager.getBoundsInScreen(bounds);
            if (!bounds.contains(fresh.page)) { failClosed("광고 화면 확인 실패 · 다시 켜 주세요"); return; }
            gate.begin(fresh.identity, -1, SystemClock.uptimeMillis()); pendingAd = true;
            advanceRequests++; adRequests++;
            if (!pager.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                failClosed("광고 넘김 요청 거부 · 다시 켜 주세요"); return;
            }
            publish(0, "광고 넘김 확인 중");
        } finally {
            for (AccessibilityNodeInfo pager : pagers) YouTubeReader.recycle(pager);
            YouTubeReader.recycle(root);
        }
    }
    private void advance(YouTubeSnapshot original) {
        if (!store.enabled() || store.target() == 0 || interacting || !screenAvailable()) return;
        YouTubeSnapshot fresh = snapshot();
        if (!fresh.usable() || !Objects.equals(original.identity, fresh.identity)
                || original.progress.duration != fresh.progress.duration) { invalidate(); return; }
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
        if (x < 0) { failClosed("플로팅을 옆으로 옮긴 뒤 다시 켜 주세요"); return; }
        Path path = new Path(); path.moveTo(x, startY); path.lineTo(x, endY);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 280)).build();
        gate.begin(fresh.identity, fresh.progress.duration, SystemClock.uptimeMillis());
        int requestGeneration = generation;
        advanceRequests++;
        boolean accepted = dispatchGesture(gesture, new GestureResultCallback() {
            @Override public void onCompleted(GestureDescription description) {
                // Completion means the gesture ran, not that YouTube changed videos.
                if (generation == requestGeneration) publish(store.target(), "다음 쇼츠 확인 중");
            }
            @Override public void onCancelled(GestureDescription description) {
                if (generation == requestGeneration) failClosed("넘김 취소됨 · 껐다 켜 주세요");
            }
        }, handler);
        if (!accepted) failClosed("넘김 요청 거부 · 껐다 켜 주세요");
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (store == null || !store.enabled() || event == null) return;
        String eventPackage = event.getPackageName() == null ? "" : event.getPackageName().toString();
        if (!store.isSelected(eventPackage) || !eventPackage.equals(activePackage)) return;
        if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_SCROLLED
                && event.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED) return;
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;
        try {
            String id = source.getViewIdResourceName();
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
                int index = event.getFromIndex();
                if (index >= 0 && lastPageIndex >= 0 && index != lastPageIndex) {
                    if (gate.pending()) gate.pageChanged(); else invalidate();
                }
                if (index >= 0) lastPageIndex = index;
            }
        } finally { YouTubeReader.recycle(source); }
    }
    private void invalidate() {
        generation++; counter.reset(); gate.cancel(); pendingAd = false; pendingVisual = false; pendingTimed = false;
        timed.reset(); RuntimeState.timedRemainingSeconds = -1;
        if (visual != null) visual.reset(); RuntimeState.current = 0;
    }
    private void interruptSession() {
        if (gate.interrupt() == AdvanceGate.State.FAILED) failClosed("전환 중 화면 변경 · 껐다 켜 주세요");
        else invalidate();
    }
    private void failClosed(String message) {
        invalidate(); RuntimeState.blocked = true; publish(0, message); ShortsTileService.refresh(this);
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
        RuntimeState.connected = false; RuntimeState.current = 0; RuntimeState.status = "서비스 연결 안 됨";
        if (store != null) { store.preferences.unregisterOnSharedPreferenceChangeListener(this); store.enabled(false); }
        if (floating != null) floating.hide();
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
        writer.println("counter=" + counter.diagnostic() + " generation=" + generation);
        writer.println("ceiling=" + (store == null ? -1 : store.ceiling()) + " tapMode=" + (store == null ? -1 : store.tapMode())
                + " floating=" + (store != null && store.floatingEnabled()) + " app=" + activePackage);
        writer.println("ads=" + (store != null && store.skipAds()) + " adRequests=" + adRequests + " adConfirmed=" + confirmedAds);
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
