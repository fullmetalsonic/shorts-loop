package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Looper;
import com.fullmetalsonic.shortsloop.core.AdvanceGate;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;
import com.fullmetalsonic.shortsloop.core.Progress;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.PhotoFrame;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.service.HostPlaybackSession;
import com.fullmetalsonic.shortsloop.service.RuntimeState;
import com.fullmetalsonic.shortsloop.visual.VisualAssistController;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unbound real-session regression fixtures. No polling, coordinator, OS input or permission
 * changes. Valid-page acceptance is tested only through the pure comparison; processDeferred
 * is invoked only for invalid/expired requests, before its coordinator boundary.
 */
final class DeferredSessionChecks {
    private static final String YT = SettingsStore.YOUTUBE_PACKAGE, IG = SettingsStore.INSTAGRAM_PACKAGE;
    private int checks;

    static int run(Context context, SettingsStore store) {
        return new DeferredSessionChecks().verify(context, store);
    }

    private int verify(Context context, SettingsStore root) {
        require(Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains("emulator"), "Disposable emulator only");
        require(Looper.myLooper() == Looper.getMainLooper(), "Deferred fixtures run synchronously on the main thread");
        Map<String, ?> original = new HashMap<>(root.preferences.getAll());
        RuntimeState.HostState ytState = RuntimeState.forHost(YT), igState = RuntimeState.forHost(IG);
        RuntimeState.HostState ytSaved = copy(ytState), igSaved = copy(igState);
        HostPlaybackSession yt = null, ig = null;
        try {
            yt = new HostPlaybackSession(context, YT, null);
            ig = new HostPlaybackSession(context, IG, null);
            YouTubeSnapshot originalPage = video("common-bottom-tabs", "content-A", "node-A", 2, 59);
            YouTubeSnapshot forward = video("common-bottom-tabs", "content-A", "node-A", 3, 59);
            require(same(yt, originalPage, forward), "Same content/node key and geometry permit later forward progress");
            require(same(yt, video("same", "", "", 2, 59), video("same", "", "", 3, 59)),
                    "This change does not invent missing content metadata or alter the legacy comparison contract");

            discard(yt, originalPage, forward.withContentIdentity("content-B"), 1300, "Changed content with identical ordinary identity and duration");
            discard(yt, originalPage, forward.withContentIdentity(""), 1300, "Previously known content key disappears");
            discard(yt, originalPage.withContentIdentity(""), forward, 1300, "New content metadata is not the saved key");
            discard(yt, originalPage, forward.withPhotoPageKey("node-B"), 1300, "Independent source-node key changes");
            discard(yt, originalPage, forward.withPhotoPageKey(""), 1300, "Previously known source-node key disappears");
            discard(yt, originalPage, forward.inWindow(8, window()), 1300, "Window ID changes");
            discard(yt, originalPage, forward.inWindow(7, new Rect(0, 0, 1100, 1800)), 1300, "Window bounds change without page movement");
            discard(yt, originalPage, video("common-bottom-tabs", "content-A", "node-A", 3, 59,
                    new Rect(10, 100, 990, 1600)), 1300, "Page geometry changes inside the same window");
            discard(yt, originalPage, forward.withIdentity("different-post"), 1300, "Ordinary identity changes");
            discard(yt, originalPage, video("common-bottom-tabs", "content-A", "node-A", 3, 60), 1300, "Duration changes");
            discard(yt, originalPage, YouTubeSnapshot.withoutClock("common-bottom-tabs", page(), false)
                    .withContentIdentity("content-A").withPhotoPageKey("node-A").inWindow(7, window()), 1300, "Progress disappears");
            discard(yt, originalPage, YouTubeSnapshot.unavailable("screen.interaction"), 1300, "Unsafe or unrecognized page");
            discard(yt, originalPage, null, 1300, "Missing fresh snapshot");
            discard(yt, null, forward, 1300, "Missing saved snapshot");
            discard(yt, originalPage, forward, 4001, "Same page after the 3000ms deferred TTL");

            YouTubeSnapshot photo = photo("node-A", new PhotoReelPolicy.Position(1, 2), page());
            require(same(ig, photo, photo("node-A", new PhotoReelPolicy.Position(1, 2), page())), "Identical photo frame remains comparable");
            discard(ig, photo, photo.withPhotoPageKey("node-B"), 1300, "Photo source changes while caption/index remain equal");
            discard(ig, photo, photo.withPhotoPageKey(""), 1300, "Photo source metadata disappears");
            discard(ig, photo, photo("node-A", new PhotoReelPolicy.Position(2, 2), page()), 1300, "User moves to another photo");
            discard(ig, photo, photo("node-A", new PhotoReelPolicy.Position(1, 2), new Rect(20, 150, 900, 1500)),
                    1300, "Photo image geometry changes");
            discard(ig, photo, photo, 4001, "Photo intent expires without a blind retry");

            YouTubeSnapshot clockless = YouTubeSnapshot.withoutClock("instagram-post", page(), false)
                    .withPhotoPageKey("video-node").inWindow(7, window());
            YouTubeSnapshot paused = YouTubeSnapshot.withoutClock("instagram-post", page(), true)
                    .withPhotoPageKey("video-node").inWindow(7, window());
            root.selectedApp(IG, true); root.enabled(true); root.floatingEnabled(false);
            SettingsStore scoped = root.forHost(IG); scoped.enabled(true); scoped.ceiling(1); scoped.timedFallback(true);
            igState.blocked = false;
            set(ig, "visual", VisualAssistController.create(null, null));
            require(clockless.recognized() && paused.recognized() && clockless.visualCandidate && !paused.visualCandidate,
                    "Pause fixture retains recognized identity/window while losing playback eligibility");
            require(same(ig, clockless, paused), "Page sameness alone is intentionally not permission to advance");
            require(VisualAssistController.samePage(clockless, clockless), "Final clockless comparison accepts the same eligible page");
            require(!VisualAssistController.samePage(clockless, paused), "Final pre-scroll comparison also rejects a fresh paused page");
            require((Boolean)call(ig, "timedCandidate", new Class<?>[]{YouTubeSnapshot.class}, clockless), "Unpaused clockless page can qualify for the enabled timer");
            require(!(Boolean)call(ig, "timedCandidate", new Class<?>[]{YouTubeSnapshot.class}, paused), "Recognized paused page cannot qualify for the timer");
            require(!(Boolean)call(ig, "timedEligible", new Class<?>[]{YouTubeSnapshot.class}, paused), "Paused expected snapshot rejects before querying a live coordinator");
            call(ig, "advanceClockless", new Class<?>[]{YouTubeSnapshot.class, boolean.class}, paused, true);
            noRequests(ig, "Paused timed advance");
            require(!((AdvanceGate)get(ig, "gate")).pending(), "Paused timer does not open a transition gate");
            noRequests(yt, "All YouTube deferred fixtures");
            return checks;
        } catch (ReflectiveOperationException error) {
            throw new AssertionError("Deferred-session safety wiring", error);
        } finally {
            if (yt != null) yt.destroySession();
            if (ig != null) ig.destroySession();
            restore(root.preferences, original); copyInto(ytSaved, ytState); copyInto(igSaved, igState);
        }
    }

    private void discard(HostPlaybackSession session, YouTubeSnapshot saved, YouTubeSnapshot fresh, long now, String where)
            throws ReflectiveOperationException {
        int[] actions = {0};
        set(session, "deferredAction", (Runnable)() -> actions[0]++);
        set(session, "deferredPage", saved); set(session, "deferredAt", 1000L);
        int beforeGeneration = (Integer)get(session, "generation");
        require(!(Boolean)call(session, "processDeferred", new Class<?>[]{YouTubeSnapshot.class, long.class}, fresh, now), where + ": discarded rather than executed");
        require(actions[0] == 0, where + ": stored action was never called");
        require(get(session, "deferredAction") == null && get(session, "deferredPage") == null, where + ": saved intent and snapshot cleared");
        require((Integer)get(session, "generation") == beforeGeneration + 1, where + ": stale callbacks invalidated");
        require(!((AdvanceGate)get(session, "gate")).pending(), where + ": no pending transition opened");
        noRequests(session, where);
        require(!(Boolean)call(session, "processDeferred", new Class<?>[]{YouTubeSnapshot.class, long.class}, fresh, now + 1)
                && actions[0] == 0, where + ": subsequent poll cannot revive the discarded request");
    }

    private void noRequests(HostPlaybackSession session, String where) throws ReflectiveOperationException {
        for (String field : new String[]{"advanceRequests", "adRequests", "liveRequests", "longRequests", "timedRequests",
                "visualRequests", "photoSlideRequests", "photoReelRequests"})
            require((Integer)get(session, field) == 0, where + ": " + field + " remains zero");
    }
    private boolean same(HostPlaybackSession session, YouTubeSnapshot a, YouTubeSnapshot b) throws ReflectiveOperationException {
        return (Boolean)call(session, "sameDeferredPage", new Class<?>[]{YouTubeSnapshot.class, YouTubeSnapshot.class}, a, b);
    }
    private static Rect page() { return new Rect(0, 100, 1000, 1600); }
    private static Rect window() { return new Rect(0, 0, 1000, 1800); }
    private static YouTubeSnapshot video(String identity, String content, String node, double position, double duration) {
        return video(identity, content, node, position, duration, page());
    }
    private static YouTubeSnapshot video(String identity, String content, String node, double position, double duration, Rect page) {
        return new YouTubeSnapshot(new Progress(position, duration), identity, page, "")
                .withContentIdentity(content).withPhotoPageKey(node).inWindow(7, window());
    }
    private static YouTubeSnapshot photo(String node, PhotoReelPolicy.Position position, Rect image) {
        return YouTubeSnapshot.photograph("photo-post", page(), new PhotoFrame(image, position))
                .withPhotoPageKey(node).inWindow(7, window());
    }
    private static Object call(Object target, String name, Class<?>[] types, Object... args) throws ReflectiveOperationException {
        Method method = target.getClass().getDeclaredMethod(name, types); method.setAccessible(true); return method.invoke(target, args);
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
        target.timedRemainingSeconds = source.timedRemainingSeconds; target.status = source.status;
    }
    @SuppressWarnings("unchecked")
    private static void restore(SharedPreferences preferences, Map<String, ?> values) {
        SharedPreferences.Editor edit = preferences.edit().clear();
        for (Map.Entry<String, ?> item : values.entrySet()) {
            String key = item.getKey(); Object value = item.getValue();
            if (value instanceof Boolean) edit.putBoolean(key, (Boolean)value);
            else if (value instanceof Integer) edit.putInt(key, (Integer)value);
            else if (value instanceof Long) edit.putLong(key, (Long)value);
            else if (value instanceof Float) edit.putFloat(key, (Float)value);
            else if (value instanceof String) edit.putString(key, (String)value);
            else if (value instanceof Set) edit.putStringSet(key, new HashSet<>((Set<String>)value));
            else throw new AssertionError("Unexpected preference type");
        }
        if (!edit.commit()) throw new AssertionError("Deferred fixture preferences were not restored");
    }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
}
