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
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.YouTubeReader;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.detection.YouTubeWindowGuard;
import com.fullmetalsonic.shortsloop.detection.ShortsReader;
import com.fullmetalsonic.shortsloop.detection.InstagramReader;
import com.fullmetalsonic.shortsloop.overlay.FloatingController;
import com.fullmetalsonic.shortsloop.tile.ShortsTileService;
import java.util.Objects;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class ShortsAccessibilityService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final LoopCounter counter = new LoopCounter();
    private final AdvanceGate gate = new AdvanceGate();
    private final ShortsReader reader = new ShortsReader();
    private final YouTubeWindowGuard windowGuard = new YouTubeWindowGuard();
    private SettingsStore store;
    private FloatingController floating;
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
    private String activePackage = "";
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (store == null) return;
            try { tick(); }
            catch (RuntimeException ignored) { failClosed("화면 조회 오류 · 껐다 켜 주세요"); }
            if (store != null && store.enabled()) handler.postDelayed(this, store.target() > 0 ? 300 : 900);
        }
    };
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        store = new SettingsStore(this);
        // Never resume unattended gestures after service/process reconnection.
        store.enabled(false);
        RuntimeState.connected = true; RuntimeState.blocked = false; RuntimeState.current = 0;
        RuntimeState.status = "꺼짐";
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
                || "skip_ads".equals(key)) applySettings();
    }
    private void updateFloating() {
        if (!store.enabled() || !store.floatingEnabled()) floating.hide();
        else if (!Settings.canDrawOverlays(this)) store.enabled(false);
        else {
            try { floating.show(); floating.update(RuntimeState.current, store.target(), RuntimeState.status); }
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
                publish(0, store.target() == 0 ? "자동 넘김 사용 안 함" : "쇼츠·릴스 시작 대기"); handler.post(poll); }
            catch (RuntimeException ignored) { store.enabled(false); publish(0, "플로팅 표시 실패 · 권한 확인"); }
        }
        ShortsTileService.refresh(this);
    }
    private void tick() {
        if (!store.enabled() || RuntimeState.blocked) return;
        if (store.floatingEnabled() && !Settings.canDrawOverlays(this)) { store.enabled(false); return; }
        if (store.target() == 0) { invalidate(); publish(0, "자동 넘김 사용 안 함"); return; }
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
            AdvanceGate.State state = snapshot.usable()
                    ? gate.inspect(snapshot.identity, snapshot.progress.duration, now)
                    : snapshot.recognized() ? gate.inspectRecognizedPage(snapshot.identity, now) : gate.unavailable(now);
            if (state == AdvanceGate.State.WAITING) {
                publish(pendingAd ? 0 : store.target(), pendingAd ? "광고 넘김 확인 중" : "다음 영상 확인 중"); return;
            }
            if (state == AdvanceGate.State.FAILED) { failClosed("넘김 확인 실패 · 껐다 켜 주세요"); return; }
            if (state == AdvanceGate.State.CONFIRMED) {
                confirmedAdvances++; if (pendingAd) confirmedAds++;
                pendingAd = false; counter.reset();
            }
        }
        if (snapshot.ad) {
            counter.reset(); lastPosition = -1; lastDuration = -1;
            if (AdSkipPolicy.enabled(store.enabled(), store.target(), store.skipAds(), store.instagramEnabled())) advanceAd(snapshot);
            else publish(0, "광고 · 바로 넘기기 꺼짐");
            return;
        }
        if (!snapshot.usable()) { invalidate(); publish(0, snapshot.reason); return; }
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
            if (root == null || !windowGuard.allows(getWindows(), root.getWindowId()))
                return YouTubeSnapshot.unavailable("다른 창·빠른 설정·작은 화면 · 대기");
            return reader.read(root, store);
        } finally { YouTubeReader.recycle(root); }
    }
    private boolean screenAvailable() {
        return getSystemService(PowerManager.class).isInteractive() && !getSystemService(KeyguardManager.class).isKeyguardLocked();
    }
    /** Semantic pager action avoids clicking an advertisement's CTA/embedded browser. */
    private void advanceAd(YouTubeSnapshot original) {
        if (!AdSkipPolicy.enabled(store.enabled(), store.target(), store.skipAds(), store.instagramEnabled())
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
    private void invalidate() { generation++; counter.reset(); gate.cancel(); pendingAd = false; RuntimeState.current = 0; }
    private void interruptSession() {
        if (gate.interrupt() == AdvanceGate.State.FAILED) failClosed("전환 중 화면 변경 · 껐다 켜 주세요");
        else invalidate();
    }
    private void failClosed(String message) {
        invalidate(); RuntimeState.blocked = true; publish(0, message); ShortsTileService.refresh(this);
    }
    private void publish(int current, String message) {
        RuntimeState.current = current; RuntimeState.status = message;
        if (floating != null && store != null) floating.update(current, store.target(), message);
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
    }
}
