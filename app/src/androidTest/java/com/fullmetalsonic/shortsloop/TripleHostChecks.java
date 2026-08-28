package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Looper;
import com.fullmetalsonic.shortsloop.core.AdvanceGate;
import com.fullmetalsonic.shortsloop.core.HostRegistry;
import com.fullmetalsonic.shortsloop.core.LoopCounter;
import com.fullmetalsonic.shortsloop.core.NormalizedLoopCounter;
import com.fullmetalsonic.shortsloop.core.NormalizedProgress;
import com.fullmetalsonic.shortsloop.core.Progress;
import com.fullmetalsonic.shortsloop.core.WindowPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.overlay.FloatingController;
import com.fullmetalsonic.shortsloop.service.HostPlaybackSession;
import com.fullmetalsonic.shortsloop.service.RuntimeState;
import com.fullmetalsonic.shortsloop.service.ShortsAccessibilityService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Synthetic metadata and unbound sessions only. No polling, window lookup or OS input. */
final class TripleHostChecks {
    private static final String[] HOSTS = {HostRegistry.YOUTUBE, HostRegistry.INSTAGRAM, HostRegistry.TIKTOK};
    private static final int[][] ORDERS = {{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
    private int checks;

    static int run(Context context, SettingsStore root) { return new TripleHostChecks().verify(context, root); }

    @SuppressWarnings("unchecked")
    private int verify(Context context, SettingsStore root) {
        require(Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains("emulator"), "Disposable emulator only");
        require(Looper.myLooper() == Looper.getMainLooper(), "Three-host fixtures run on the main thread");
        Map<String, ?> saved = new HashMap<>(root.preferences.getAll());
        HostPlaybackSession[] sessions = new HostPlaybackSession[3];
        RuntimeState.HostState[] states = new RuntimeState.HostState[3], savedStates = new RuntimeState.HostState[3];
        SettingsStore[] stores = new SettingsStore[3];
        ShortsAccessibilityService coordinator = new ShortsAccessibilityService();
        List<HostPlaybackSession> registered = null;
        try {
            require(Arrays.equals(HOSTS, HostRegistry.packages()), "Explicit three-host order");
            String[] independent = HostRegistry.packages(); independent[0] = "changed";
            require(Arrays.equals(HOSTS, HostRegistry.packages()), "Registry callers cannot mutate the allowlist");
            for (String unknown : new String[]{null, "", "other.app", "com.zhiliaoapp.musically", HostRegistry.TIKTOK + ".lite"}) {
                require(!HostRegistry.supports(unknown) && !root.isSelected(unknown), "Unknown package is not a host");
                boolean rejected = false;
                try { RuntimeState.forHost(unknown); } catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "Unknown runtime host cannot alias Instagram");
            }
            root.enabled(false); root.floatingEnabled(false);
            for (int i = 0; i < 3; i++) {
                stores[i] = root.forHost(HOSTS[i]);
                states[i] = RuntimeState.forHost(HOSTS[i]); savedStates[i] = copy(states[i]);
                root.selectedApp(HOSTS[i], true); stores[i].enabled(true); stores[i].ceiling(i + 2);
                sessions[i] = new HostPlaybackSession(context, HOSTS[i], null);
                set(sessions[i], "floating", new FloatingController(context, stores[i], null));
                require(HOSTS[i].equals(((SettingsStore)get(sessions[i], "store")).hostPackage()), "Session keeps explicit host settings");
                for (int j = 0; j < i; j++) {
                    require(states[i] != states[j], "Three runtime objects are distinct");
                    for (String field : new String[]{"counter", "normalizedCounter", "gate", "photos", "timed", "live"})
                        require(get(sessions[i], field) != get(sessions[j], field), "Every host owns its " + field);
                }
            }
            registered = (List<HostPlaybackSession>)get(coordinator, "sessions");
            registered.addAll(Arrays.asList(sessions)); set(coordinator, "store", root);
            root.enabled(true);
            windowsAndSelection(root, stores, sessions, coordinator);
            for (String host : HOSTS) root.selectedApp(host, true);
            unsupportedSpecials(root, stores[2], sessions[2]);
            failuresAndDeferred(root, stores, states, sessions);
            root.enabled(false);
            for (int i = 0; i < 3; i++) {
                sessions[i].onSharedPreferenceChanged(root.preferences, "enabled");
                require(!stores[i].enabled() && states[i].current == 0 && "off".equals(states[i].status), "Master OFF stops each host");
                require(!((AdvanceGate)get(sessions[i], "gate")).pending() && get(sessions[i], "deferredAction") == null,
                        "Master OFF clears pending and deferred work for all three");
                noRequests(sessions[i]);
            }
            return checks;
        } catch (ReflectiveOperationException error) {
            throw new AssertionError("Three-host isolation wiring", error);
        } finally {
            for (HostPlaybackSession session : sessions) if (session != null) session.destroySession();
            if (registered != null) registered.clear();
            restore(root.preferences, saved);
            for (int i = 0; i < 3; i++) if (savedStates[i] != null) copyInto(savedStates[i], states[i]);
        }
    }

    private void windowsAndSelection(SettingsStore root, SettingsStore[] stores, HostPlaybackSession[] sessions,
            ShortsAccessibilityService coordinator) throws ReflectiveOperationException {
        for (int mask = 0; mask < 8; mask++) {
            for (int host = 0; host < 3; host++) root.selectedApp(HOSTS[host], (mask & (1 << host)) != 0);
            require(root.hasSelectedApps() == (mask != 0), "All eight selection combinations remain explicit");
            for (int[] order : ORDERS) {
                List<WindowPolicy.Window> windows = new ArrayList<>();
                WindowPolicy.Box[] boxes = new WindowPolicy.Box[3];
                for (int slot = 0; slot < 3; slot++) {
                    int host = order[slot];
                    boxes[host] = new WindowPolicy.Box(slot * 620, 0, slot * 620 + 600, 1200);
                    windows.add(0, new WindowPolicy.Window(100 + host, 1, 10 + slot, slot == 0, false, false, boxes[host]));
                    set(sessions[host], "observedWindow", 100 + host);
                    set(sessions[host], "observedBounds", rect(boxes[host]));
                }
                int eligible = 0;
                for (int host = 0; host < 3; host++) {
                    boolean selected = (mask & (1 << host)) != 0;
                    require(stores[host].enabled() == selected, "Selection follows host, not window position");
                    require(WindowPolicy.allowed(windows, 100 + host), "Non-overlapping host window is independently observable");
                    WindowPolicy.Box box = boxes[host];
                    require(WindowPolicy.inputClear(windows, 100 + host,
                            new WindowPolicy.Box(box.left() + 200, 200, box.left() + 400, 1000)), "Input stays inside its own pane");
                    require(!WindowPolicy.inputClear(windows, 100 + host,
                            new WindowPolicy.Box(box.left() + 200, 200, box.right() + 1, 1000)), "Cross-pane input is rejected");
                    if (stores[host].enabled() && WindowPolicy.allowed(windows, 100 + host)) eligible++;
                }
                require(eligible == Integer.bitCount(mask), "Eight selections by six positions produce the expected eligible hosts");
                List<WindowPolicy.Window> covered = new ArrayList<>(windows);
                covered.add(new WindowPolicy.Window(900, 2, 100, true, false, false, new WindowPolicy.Box(0, 0, 1840, 1200)));
                for (int host = 0; host < 3; host++) require(!WindowPolicy.allowed(covered, 100 + host), "Focused modal stops all three panes");
            }
        }
        for (String host : HOSTS) root.selectedApp(host, true);
        for (int visible = 3; visible >= 0; visible--) {
            for (int host = 0; host < 3; host++) set(sessions[host], "observedBounds", host < visible ? new Rect(host * 620, 0, host * 620 + 600, 1200) : null);
            require((Boolean)call(coordinator, "multipleHostsVisible", new Class<?>[0]) == (visible >= 2), "Known visibility follows 3/2/1/0, not host order");
        }
        List<WindowPolicy.Window> missing = new ArrayList<>();
        missing.add(new WindowPolicy.Window(100, 1, 10, true, false, false, new WindowPolicy.Box(0, 0, 600, 1200)));
        missing.add(new WindowPolicy.Window(101, 1, 11, false, true, false, new WindowPolicy.Box(620, 0, 1220, 1200)));
        require(WindowPolicy.allowed(missing, 100), "One remaining visible host can continue");
        require(!WindowPolicy.allowed(missing, 101) && !WindowPolicy.allowed(missing, 102), "PiP and missing hosts cannot receive input");
    }

    private void unsupportedSpecials(SettingsStore root, SettingsStore tt, HostPlaybackSession session) throws ReflectiveOperationException {
        root.skipAds(true); root.photoEnabled(true); root.skipLive(true); root.timedFallback(true); root.visualAssist(true);
        tt.skipLong(true);
        require(!tt.skipAds() && !tt.photoEnabled() && !tt.skipLive() && !tt.timedFallback() && !tt.visualAssist() && !tt.skipLong(),
                "TikTok never inherits Instagram/YouTube special capabilities");
        for (String method : new String[]{"adSkippingEnabled", "photoSkippingEnabled", "liveSkippingEnabled", "longSkippingEnabled"})
            require(!(Boolean)call(session, method, new Class<?>[0]), "Unsupported TikTok policy remains inactive: " + method);
        for (String key : new String[]{"skip_ads", "ad_delay_tenths", "timed_fallback", "fallback_seconds", "photo_enabled", "skip_live"})
            require(!SettingsStore.affectsHost(key, HostRegistry.TIKTOK), "Other-host special edits do not reset TikTok");
    }

    private void failuresAndDeferred(SettingsStore root, SettingsStore[] stores, RuntimeState.HostState[] states,
            HostPlaybackSession[] sessions) throws ReflectiveOperationException {
        int[] ran = new int[3];
        for (int failed = 0; failed < 3; failed++) {
            Object[] previous = new Object[3], deferred = new Object[3];
            int[] generations = new int[3];
            for (int host = 0; host < 3; host++) {
                stores[host].enabled(true); states[host].blocked = false; states[host].current = host + 1;
                states[host].status = "playback.counting"; states[host].timedRemainingSeconds = host + 5;
                LoopCounter counter = (LoopCounter)get(sessions[host], "counter"); counter.setTarget(host + 2);
                counter.observe(new Progress(0, 10), "page-" + host, 1000);
                counter.observe(new Progress(.3, 10), "page-" + host, 1300);
                NormalizedLoopCounter normalized = (NormalizedLoopCounter)get(sessions[host], "normalizedCounter");
                normalized.setTarget(host + 2); normalized.observe(new NormalizedProgress(.01), "page-" + host, 1000);
                normalized.observe(new NormalizedProgress(.02), "page-" + host, 1300);
                ((AdvanceGate)get(sessions[host], "gate")).begin("old-" + host, 10, 1000);
                final int slot = host; Runnable action = () -> ran[slot]++;
                set(sessions[host], "deferredAction", action); set(sessions[host], "deferredPage", page(host));
                set(sessions[host], "deferredAt", 1000L);
                previous[host] = get(counter, "previous"); deferred[host] = action;
                generations[host] = (Integer)get(sessions[host], "generation");
            }
            for (int host = 0; host < 3; host++) if (host != failed) {
                sessions[host].onSharedPreferenceChanged(root.preferences, stores[failed].scopedKey("target"));
                require((Integer)get(sessions[host], "generation") == generations[host], "Other-host edits preserve callback generation");
            }
            call(sessions[failed], "failClosed", new Class<?>[]{String.class}, "error.advance");
            require(states[failed].blocked && states[failed].current == 0, "Only the failed host stops");
            require(get(sessions[failed], "deferredAction") == null, "Failed host loses its deferred input");
            for (int host = 0; host < 3; host++) if (host != failed) {
                require(!states[host].blocked && states[host].current == host + 1 && states[host].timedRemainingSeconds == host + 5,
                        "Sibling display and countdown remain intact");
                require((Integer)get(sessions[host], "generation") == generations[host]
                        && get(get(sessions[host], "counter"), "previous") == previous[host], "Sibling ordinary counter evidence survives");
                require((Double)get(get(sessions[host], "normalizedCounter"), "previous") == .02, "Sibling normalized counter evidence survives");
                require(get(sessions[host], "deferredAction") == deferred[host] && ((AdvanceGate)get(sessions[host], "gate")).pending(),
                        "Sibling deferred work and pending transition stay separate");
            }
            require(root.enabled(), "Individual failure does not change master execution");
        }
        for (int expired = 0; expired < 3; expired++) {
            for (int host = 0; host < 3; host++) {
                final int slot = host;
                set(sessions[host], "deferredAction", (Runnable)() -> ran[slot]++);
                set(sessions[host], "deferredPage", page(host)); set(sessions[host], "deferredAt", 1000L);
            }
            require(!(Boolean)call(sessions[expired], "processDeferred", new Class<?>[]{YouTubeSnapshot.class, long.class}, page(expired), 4001L),
                    "Expired three-host intention is discarded before coordinator access");
            require(get(sessions[expired], "deferredAction") == null, "Expired host drops saved action");
            for (int host = 0; host < 3; host++) if (host != expired)
                require(get(sessions[host], "deferredAction") != null, "One expired intention does not erase sibling intentions");
        }
        require(Arrays.equals(ran, new int[3]), "No synthetic deferred action was executed");
        for (HostPlaybackSession session : sessions) noRequests(session);
    }

    private void noRequests(HostPlaybackSession session) throws ReflectiveOperationException {
        for (String name : new String[]{"advanceRequests", "adRequests", "liveRequests", "longRequests", "timedRequests",
                "visualRequests", "photoSlideRequests", "photoReelRequests"}) require((Integer)get(session, name) == 0, "No OS request: " + name);
    }
    private static YouTubeSnapshot page(int host) {
        return new YouTubeSnapshot(new Progress(1, 10), "page-" + host, new Rect(0, 100, 600, 1000), "")
                .withContentIdentity("content-" + host).withPhotoPageKey("node-" + host).inWindow(100 + host, new Rect(0, 0, 600, 1200));
    }
    private static Rect rect(WindowPolicy.Box box) { return new Rect(box.left(), box.top(), box.right(), box.bottom()); }
    private static Object call(Object target, String name, Class<?>[] types, Object... args) throws ReflectiveOperationException {
        Method method = target.getClass().getDeclaredMethod(name, types); method.setAccessible(true); return method.invoke(target, args);
    }
    private static Object get(Object target, String name) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(target);
    }
    private static void set(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
    private static RuntimeState.HostState copy(RuntimeState.HostState source) { RuntimeState.HostState copy = new RuntimeState.HostState(); copyInto(source, copy); return copy; }
    private static void copyInto(RuntimeState.HostState source, RuntimeState.HostState target) {
        target.blocked = source.blocked; target.current = source.current; target.status = source.status; target.timedRemainingSeconds = source.timedRemainingSeconds;
    }
    @SuppressWarnings("unchecked")
    private static void restore(SharedPreferences preferences, Map<String, ?> saved) {
        SharedPreferences.Editor edit = preferences.edit().clear();
        for (Map.Entry<String, ?> entry : saved.entrySet()) {
            String key = entry.getKey(); Object value = entry.getValue();
            if (value instanceof Boolean) edit.putBoolean(key, (Boolean)value);
            else if (value instanceof Integer) edit.putInt(key, (Integer)value);
            else if (value instanceof Long) edit.putLong(key, (Long)value);
            else if (value instanceof Float) edit.putFloat(key, (Float)value);
            else if (value instanceof String) edit.putString(key, (String)value);
            else if (value instanceof Set) edit.putStringSet(key, new HashSet<>((Set<String>)value));
            else throw new AssertionError("Unexpected preference type");
        }
        if (!edit.commit()) throw new AssertionError("Three-host fixture preferences were not restored");
    }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
}
