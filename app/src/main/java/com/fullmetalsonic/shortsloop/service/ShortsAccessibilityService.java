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
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.HostWindowAccess;
import com.fullmetalsonic.shortsloop.tile.ShortsTileService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/** Shared OS resources only. Each host owns its complete playback and failure state. */
public final class ShortsAccessibilityService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingsStore store;
    private HostPlaybackSession youtube, instagram;
    private HostWindowAccess windowAccess;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ActionArbiter inputs = new ActionArbiter();
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        store = new SettingsStore(this);
        // Never resume unattended inputs after process/service reconnection.
        store.enabled(false);
        RuntimeState.connected = true;
        windowAccess = new HostWindowAccess(this);
        youtube = new HostPlaybackSession(this, SettingsStore.YOUTUBE_PACKAGE, this);
        instagram = new HostPlaybackSession(this, SettingsStore.INSTAGRAM_PACKAGE, this);
        youtube.startSession(); instagram.startSession();
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
        boolean include = youtube != null && youtube.needsLayoutNodes() || instagram != null && instagram.needsLayoutNodes();
        int flag = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        int flags = include ? info.flags | flag : info.flags & ~flag;
        flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        if (info.flags == flags) return false;
        info.flags = flags; setServiceInfo(info); return true;
    }
    boolean multipleHostsVisible() { return youtube != null && instagram != null && youtube.visible() && instagram.visible(); }
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
        RuntimeState.HostState yt = RuntimeState.forHost(SettingsStore.YOUTUBE_PACKAGE);
        RuntimeState.HostState ig = RuntimeState.forHost(SettingsStore.INSTAGRAM_PACKAGE);
        boolean ytOn = store != null && store.forHost(SettingsStore.YOUTUBE_PACKAGE).enabled();
        boolean igOn = store != null && store.forHost(SettingsStore.INSTAGRAM_PACKAGE).enabled();
        boolean blocked = (ytOn || igOn) && (!ytOn || yt.blocked) && (!igOn || ig.blocked);
        String status = store == null || !store.enabled() ? "off" : blocked ? "blocked:error.transition" : "playback.counting";
        boolean changed = RuntimeState.blocked != blocked || !status.equals(RuntimeState.status);
        RuntimeState.blocked = blocked;
        RuntimeState.current = 0; RuntimeState.timedRemainingSeconds = -1;
        RuntimeState.status = status;
        if (changed) ShortsTileService.refresh(this);
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (youtube != null) youtube.onSharedPreferenceChanged(preferences, key);
        if (instagram != null) instagram.onSharedPreferenceChanged(preferences, key);
        updateQueryMode(); publishSummary();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (youtube != null) youtube.onAccessibilityEvent(event);
        if (instagram != null) instagram.onAccessibilityEvent(event);
    }
    @Override public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (youtube != null) youtube.onConfigurationChanged(configuration);
        if (instagram != null) instagram.onConfigurationChanged(configuration);
    }
    @Override public void onInterrupt() { if (store != null) store.enabled(false); }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (store != null) { store.preferences.unregisterOnSharedPreferenceChangeListener(this); store.enabled(false); }
        if (youtube != null) youtube.destroySession();
        if (instagram != null) instagram.destroySession();
        youtube = instagram = null;
        updateQueryMode();
        RuntimeState.connected = false; RuntimeState.blocked = false;
        RuntimeState.current = 0; RuntimeState.status = "service.disconnected";
        ShortsTileService.refresh(this); store = null; super.onDestroy();
    }
    @Override protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("ShortsLoop " + com.fullmetalsonic.shortsloop.BuildConfig.VERSION_NAME
                + " connected=" + RuntimeState.connected + " enabled=" + (store != null && store.enabled())
                + " dualVisible=" + multipleHostsVisible() + " inputBusy=" + inputs.busy());
        if (youtube != null) youtube.dump(writer);
        if (instagram != null) instagram.dump(writer);
    }
}
