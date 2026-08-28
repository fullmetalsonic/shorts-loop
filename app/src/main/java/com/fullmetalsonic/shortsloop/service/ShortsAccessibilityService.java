package com.fullmetalsonic.shortsloop.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.ActionArbiter;
import com.fullmetalsonic.shortsloop.core.HostRegistry;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.HostWindowAccess;
import com.fullmetalsonic.shortsloop.tile.ShortsTileService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/** Shared OS resources only. Each host owns its complete playback and failure state. */
public final class ShortsAccessibilityService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingsStore store;
    private final java.util.List<HostPlaybackSession> sessions = new java.util.ArrayList<>();
    private HostWindowAccess windowAccess;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ActionArbiter inputs = new ActionArbiter();
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        if (store != null) {
            store.preferences.unregisterOnSharedPreferenceChangeListener(this);
            store.enabled(false);
        }
        for (HostPlaybackSession session : sessions) session.destroySession();
        sessions.clear();
        store = new SettingsStore(this);
        // Never resume unattended inputs after process/service reconnection.
        store.enabled(false);
        RuntimeState.connected = true;
        windowAccess = new HostWindowAccess(this);
        for (String host : HostRegistry.packages()) sessions.add(new HostPlaybackSession(this, host, this));
        for (HostPlaybackSession session : sessions) session.startSession();
        store.preferences.registerOnSharedPreferenceChangeListener(this);
        publishSummary();
    }
    AccessibilityNodeInfo hostRoot(String host) {
        if (windowAccess == null || store == null) return null;
        if (store.dualMode()) return windowAccess.root(host);
        AccessibilityNodeInfo active = getRootInActiveWindow();
        if (active == null) return null;
        if (host.contentEquals(active.getPackageName() == null ? "" : active.getPackageName())) return active;
        com.fullmetalsonic.shortsloop.detection.YouTubeReader.recycle(active); return null;
    }
    boolean updateQueryMode() {
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) return false;
        boolean include = false;
        for (HostPlaybackSession session : sessions) include |= session.needsLayoutNodes();
        int flag = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        int flags = include ? info.flags | flag : info.flags & ~flag;
        flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        if (info.flags == flags) return false;
        info.flags = flags; setServiceInfo(info); return true;
    }
    boolean multipleHostsVisible() {
        int count = 0;
        for (HostPlaybackSession session : sessions) if (session.visible() && ++count >= 2) return true;
        return false;
    }
    boolean actionAvailable(String host) { return store != null && store.forHost(host).enabled() && inputs.available(host, SystemClock.uptimeMillis()); }
    void cancelWaiting(String host) { inputs.cancelWaiting(host); }
    boolean dispatchFor(String host, GestureDescription gesture, GestureResultCallback callback, Handler callbackHandler) {
        if (!actionAvailable(host)) return false;
        long token = inputs.acquire(host, SystemClock.uptimeMillis());
        if (token < 0) return false;
        boolean accepted;
        try {
            accepted = dispatchGesture(gesture, new GestureResultCallback() {
                @Override public void onCompleted(GestureDescription description) {
                    inputs.release(token, SystemClock.uptimeMillis());
                    if (callback != null) callback.onCompleted(description);
                }
                @Override public void onCancelled(GestureDescription description) {
                    inputs.release(token, SystemClock.uptimeMillis());
                    if (callback != null) callback.onCancelled(description);
                }
            }, callbackHandler);
        } catch (RuntimeException error) { inputs.release(token, SystemClock.uptimeMillis()); throw error; }
        if (!accepted) inputs.release(token, SystemClock.uptimeMillis());
        // A lost callback must not permanently starve the other host. No retry is emitted.
        handler.postDelayed(() -> inputs.release(token, SystemClock.uptimeMillis()), 1800);
        return accepted;
    }
    boolean scrollFor(String host, AccessibilityNodeInfo pager) {
        if (!actionAvailable(host)) return false;
        long token = inputs.acquire(host, SystemClock.uptimeMillis());
        if (token < 0) return false;
        boolean accepted;
        try { accepted = pager.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD); }
        catch (RuntimeException error) { inputs.release(token, SystemClock.uptimeMillis()); throw error; }
        handler.postDelayed(() -> inputs.release(token, SystemClock.uptimeMillis()), accepted ? 400 : 0);
        return accepted;
    }
    void publishSummary() {
        boolean anyOn = false, allBlocked = true;
        for (String host : HostRegistry.packages()) {
            if (store != null && store.forHost(host).enabled()) {
                anyOn = true; allBlocked &= RuntimeState.forHost(host).blocked;
            }
        }
        boolean blocked = anyOn && allBlocked;
        String status = store == null || !store.enabled() ? "off" : blocked ? "blocked:error.transition" : "playback.counting";
        boolean changed = RuntimeState.blocked != blocked || !status.equals(RuntimeState.status);
        RuntimeState.blocked = blocked;
        RuntimeState.current = 0; RuntimeState.timedRemainingSeconds = -1;
        RuntimeState.status = status;
        if (changed) ShortsTileService.refresh(this);
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        for (HostPlaybackSession session : sessions) session.onSharedPreferenceChanged(preferences, key);
        updateQueryMode(); publishSummary();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        for (HostPlaybackSession session : sessions) session.onAccessibilityEvent(event);
    }
    @Override public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        for (HostPlaybackSession session : sessions) session.onConfigurationChanged(configuration);
    }
    @Override public void onInterrupt() { if (store != null) store.enabled(false); }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (store != null) { store.preferences.unregisterOnSharedPreferenceChangeListener(this); store.enabled(false); }
        for (HostPlaybackSession session : sessions) session.destroySession();
        sessions.clear();
        updateQueryMode();
        RuntimeState.connected = false; RuntimeState.blocked = false;
        RuntimeState.current = 0; RuntimeState.status = "service.disconnected";
        ShortsTileService.refresh(this); store = null; super.onDestroy();
    }
    @Override protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("ShortsLoop " + com.fullmetalsonic.shortsloop.BuildConfig.VERSION_NAME
                + " connected=" + RuntimeState.connected + " enabled=" + (store != null && store.enabled())
                + " dualVisible=" + multipleHostsVisible() + " inputBusy=" + inputs.busy());
        for (HostPlaybackSession session : sessions) session.dump(writer);
    }
}
