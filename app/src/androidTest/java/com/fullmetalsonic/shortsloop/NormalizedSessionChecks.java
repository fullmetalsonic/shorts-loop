package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.AdvanceGate;
import com.fullmetalsonic.shortsloop.core.NormalizedLoopCounter;
import com.fullmetalsonic.shortsloop.core.NormalizedProgress;
import com.fullmetalsonic.shortsloop.core.NormalizedTransition;
import com.fullmetalsonic.shortsloop.core.PlaybackRestart;
import com.fullmetalsonic.shortsloop.core.Progress;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.TikTokReader;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.overlay.FloatingController;
import com.fullmetalsonic.shortsloop.service.HostPlaybackSession;
import com.fullmetalsonic.shortsloop.service.RuntimeState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Real unbound session lifecycle and synthetic frames. No service binding, permissions or OS input. */
public final class NormalizedSessionChecks {
    private int checks;
    private NormalizedSessionChecks() { }
    public static int run(Context context, SettingsStore root) { return new NormalizedSessionChecks().verify(context, root); }
    private int verify(Context context, SettingsStore root) {
        Map<String, ?> original = root.preferences.getAll();
        RuntimeState.HostState ttState = RuntimeState.forHost(TikTokReader.PACKAGE);
        RuntimeState.HostState igState = RuntimeState.forHost(SettingsStore.INSTAGRAM_PACKAGE);
        RuntimeState.HostState savedTt = copy(ttState), savedIg = copy(igState);
        HostPlaybackSession tt = new HostPlaybackSession(context, TikTokReader.PACKAGE, null);
        HostPlaybackSession ig = new HostPlaybackSession(context, SettingsStore.INSTAGRAM_PACKAGE, null);
        try {
            root.selectedApp(TikTokReader.PACKAGE, true); root.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
            root.enabled(true); root.floatingEnabled(false);
            SettingsStore scoped = root.forHost(TikTokReader.PACKAGE); scoped.enabled(true); scoped.ceiling(1); scoped.target(1);
            set(tt, "floating", new FloatingController(context, scoped, null));
            ttState.blocked = false; igState.blocked = false; igState.current = 1;
            AdvanceGate siblingGate = (AdvanceGate)get(ig, "gate"); siblingGate.begin("ig-page", 10, 1000);
            int siblingGeneration = (Integer)get(ig, "generation");

            YouTubeSnapshot initial = frame("A", "media-A", 7, .02);
            NormalizedTransition.Frame mapped = mapped(tt, initial);
            require(mapped != null && mapped.page.equals("A") && mapped.media.equals("media-A")
                    && mapped.pager.equals("pager") && mapped.index == 7 && mapped.fraction == .02,
                    "Session carries normalized units and independent identity keys");
            require(mapped.scope.contains(TikTokReader.PACKAGE) && mapped.scope.contains("7"), "Scope contains host and window");
            require(initial.progress == null, "Normalized snapshot has no inferred seconds");
            require(mapped(tt, new YouTubeSnapshot(new Progress(1, 10), "ordinary", page(), "").inWindow(7, window())) == null,
                    "Seconds-based snapshot cannot enter normalized transition");
            require(mapped(tt, YouTubeSnapshot.advertisement(page()).inWindow(7, window())) == null, "Ad cannot become normalized frame");
            require(mapped(tt, YouTubeSnapshot.livePreview("live", page()).inWindow(7, window())) == null, "Live cannot become normalized frame");
            require(mapped(tt, initial.inWindow(-1, window())) == null, "Missing window cannot become frame");
            require(mapped(tt, initial.inWindow(7, new Rect(0, 0, 100, 100))) == null, "Out-of-window page cannot become frame");
            require(mapped(tt, YouTubeSnapshot.unavailable("screen.interaction")) == null, "Unsafe snapshot cannot become frame");

            String countKey = countKey(tt, initial);
            require(countKey.equals(countKey(tt, frame("A", "media-A", 7, .03))), "Counter key is stable while only progress moves");
            require(!countKey.equals(countKey(tt, frame("A", "media-B", 7, .03))), "Reused page with new media resets count identity");
            require(!countKey.equals(countKey(tt, frame("A", "media-A", 8, .03))), "Reused page/media with changed index resets count identity");
            require(!countKey.equals(countKey(tt, frame("A", "media-A", -1, .03))), "Lost known index cannot retain previous count identity");
            require(!countKey.equals(countKey(tt, initial.withNormalizedIdentity("new-pager", "media-A", 7))),
                    "Reused page/media under another pager resets count identity");
            require(countKey.equals(countKey(tt, initial)), "A-B-A counter key returns to original without invented sequence tokens");

            require(same(tt, initial, frame("A", "media-A", 7, .03)), "Small same-page forward sample stays comparable");
            discard(tt, initial, frame("A", "media-B", 7, .03), "Independent media changed");
            discard(tt, initial, frame("B", "media-A", 7, .03), "Page changed");
            discard(tt, initial, frame("A", "media-A", 8, .03), "Known index changed");
            discard(tt, initial, frame("A", "media-A", -1, .03), "Known index disappears");
            discard(tt, initial, frame("A", "media-A", 7, .01), "Same-page backward seek");
            discard(tt, initial, frame("A", "media-A", 7, .8), "Same-page forward jump");
            discard(tt, initial, initial.withNormalizedIdentity("other-pager", "media-A", 7), "Pager changed");
            discard(tt, initial, initial.inWindow(8, window()), "Window changed");
            discard(tt, initial, YouTubeSnapshot.unavailable("tiktok.no_progress"), "Range unavailable");

            arm(tt, initial);
            int generation = (Integer)get(tt, "generation");
            call(tt, "interruptSession", new Class<?>[0]);
            require(ttState.blocked, "Interruption of requested normalized input fails closed");
            require((Integer)get(tt, "generation") > generation, "Interruption invalidates stale callbacks");
            assertCleared(tt, "Interrupted request");
            require(!igState.blocked && igState.current == 1 && siblingGate.pending()
                    && (Integer)get(ig, "generation") == siblingGeneration, "TikTok interruption does not reset Instagram");

            ttState.blocked = false; arm(tt, initial);
            call(tt, "advanceTimedOut", new Class<?>[0]);
            require(ttState.blocked, "Normalized confirmation timeout is a hard stop");
            assertCleared(tt, "Timed-out request");
            require(!((PlaybackRestart)get(tt, "restart")).active() && (Integer)get(tt, "recoveryEntries") == 0,
                    "Normalized timeout cannot use seconds-based recovery");
            require((Boolean)get(tt, "unresolvedNormalizedAttempt"), "Timeout retains unresolved normalized-request guard");
            tt.onSharedPreferenceChanged(root.preferences, scoped.scopedKey("target"));
            require(ttState.blocked && (Boolean)get(tt, "unresolvedNormalizedAttempt"),
                    "Count changes cannot clear an unconfirmed normalized request");

            arm(tt, initial); root.enabled(false);
            tt.onSharedPreferenceChanged(root.preferences, "enabled");
            require(!ttState.blocked && ttState.status.equals("off"), "Explicit master OFF clears failed state without starting playback");
            require(!(Boolean)get(tt, "unresolvedNormalizedAttempt"), "Explicit OFF clears unresolved normalized guard");
            assertCleared(tt, "Master OFF");
            call(tt, "advanceNormalized", new Class<?>[]{YouTubeSnapshot.class}, initial);
            noInput(tt, "Disabled request");
            require(siblingGate.pending() && (Integer)get(ig, "generation") == siblingGeneration,
                    "Direct TikTok settings dispatch leaves sibling object alone");
            noInput(ig, "Sibling fixtures");
            return checks;
        } catch (ReflectiveOperationException error) { throw new AssertionError("Normalized session safety wiring", error); }
        finally {
            tt.destroySession(); ig.destroySession(); restore(root.preferences, original);
            copyInto(savedTt, ttState); copyInto(savedIg, igState);
        }
    }
    private void arm(HostPlaybackSession session, YouTubeSnapshot value) throws ReflectiveOperationException {
        ((NormalizedTransition)get(session, "normalizedTransition")).begin(mapped(session, value), 1000);
        ((AdvanceGate)get(session, "gate")).begin(value.identity, -1, 1000);
        set(session, "pendingNormalized", true);
        set(session, "unresolvedNormalizedAttempt", true);
    }
    private void assertCleared(HostPlaybackSession session, String label) throws ReflectiveOperationException {
        require(!((NormalizedTransition)get(session, "normalizedTransition")).pending(), label + ": strict tracker cancelled");
        require(!((AdvanceGate)get(session, "gate")).pending(), label + ": shared gate cancelled");
        require(!(Boolean)get(session, "pendingNormalized"), label + ": normalized pending flag cleared");
        require(!((NormalizedLoopCounter)get(session, "normalizedCounter")).pendingAdvance(), label + ": emitted completion cleared");
        noInput(session, label);
    }
    private void discard(HostPlaybackSession session, YouTubeSnapshot saved, YouTubeSnapshot fresh, String label)
            throws ReflectiveOperationException {
        int[] calls = {0};
        set(session, "deferredAction", (Runnable)() -> calls[0]++);
        set(session, "deferredPage", saved); set(session, "deferredAt", 1000L);
        require(!(Boolean)call(session, "processDeferred", new Class<?>[]{YouTubeSnapshot.class, long.class}, fresh, 1300L), label + ": discarded");
        require(calls[0] == 0 && get(session, "deferredAction") == null, label + ": no deferred execution");
        noInput(session, label);
    }
    private void noInput(HostPlaybackSession session, String label) throws ReflectiveOperationException {
        require((Integer)get(session, "advanceRequests") == 0 && (Integer)get(session, "confirmedAdvances") == 0,
                label + ": no OS request or fabricated transition confirmation");
    }
    private static NormalizedTransition.Frame mapped(HostPlaybackSession session, YouTubeSnapshot value) throws ReflectiveOperationException {
        return (NormalizedTransition.Frame)call(session, "normalizedFrame", new Class<?>[]{YouTubeSnapshot.class}, value);
    }
    private static String countKey(HostPlaybackSession session, YouTubeSnapshot value) throws ReflectiveOperationException {
        return (String)call(session, "normalizedCounterKey", new Class<?>[]{YouTubeSnapshot.class}, value);
    }
    private static boolean same(HostPlaybackSession session, YouTubeSnapshot a, YouTubeSnapshot b) throws ReflectiveOperationException {
        return (Boolean)call(session, "sameDeferredPage", new Class<?>[]{YouTubeSnapshot.class, YouTubeSnapshot.class}, a, b);
    }
    private static Rect page() { return new Rect(0, 100, 1000, 1600); }
    private static Rect window() { return new Rect(0, 0, 1000, 1800); }
    private static YouTubeSnapshot frame(String page, String media, int index, double fraction) {
        return YouTubeSnapshot.normalizedVideo(page, page(), new NormalizedProgress(fraction))
                .withNormalizedIdentity("pager", media, index).inWindow(7, window());
    }
    private static Object call(Object target, String method, Class<?>[] types, Object... args) throws ReflectiveOperationException {
        Method found = target.getClass().getDeclaredMethod(method, types); found.setAccessible(true); return found.invoke(target, args);
    }
    private static Object get(Object target, String name) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(target);
    }
    private static void set(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
    private static RuntimeState.HostState copy(RuntimeState.HostState source) {
        RuntimeState.HostState result = new RuntimeState.HostState(); copyInto(source, result); return result;
    }
    private static void copyInto(RuntimeState.HostState source, RuntimeState.HostState target) {
        target.blocked = source.blocked; target.current = source.current;
        target.status = source.status; target.timedRemainingSeconds = source.timedRemainingSeconds;
    }
    @SuppressWarnings("unchecked")
    private static void restore(SharedPreferences prefs, Map<String, ?> values) {
        SharedPreferences.Editor edit = prefs.edit().clear();
        for (Map.Entry<String, ?> item : values.entrySet()) {
            Object value = item.getValue(); String key = item.getKey();
            if (value instanceof Boolean) edit.putBoolean(key, (Boolean)value);
            else if (value instanceof Integer) edit.putInt(key, (Integer)value);
            else if (value instanceof Long) edit.putLong(key, (Long)value);
            else if (value instanceof Float) edit.putFloat(key, (Float)value);
            else if (value instanceof String) edit.putString(key, (String)value);
            else if (value instanceof Set) edit.putStringSet(key, new HashSet<>((Set<String>)value));
            else throw new AssertionError("Unexpected preference type");
        }
        if (!edit.commit()) throw new AssertionError("Normalized fixture preferences not restored");
    }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
}
