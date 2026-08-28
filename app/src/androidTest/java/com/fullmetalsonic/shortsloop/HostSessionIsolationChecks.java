package com.fullmetalsonic.shortsloop;

import android.content.Context;
import com.fullmetalsonic.shortsloop.core.AdvanceGate;
import com.fullmetalsonic.shortsloop.core.LoopCounter;
import com.fullmetalsonic.shortsloop.core.Progress;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.overlay.FloatingController;
import com.fullmetalsonic.shortsloop.service.HostPlaybackSession;
import com.fullmetalsonic.shortsloop.service.RuntimeState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Two unbound real session objects. No polling, window inspection or gestures are started. */
final class HostSessionIsolationChecks {
    private static final String YT = SettingsStore.YOUTUBE_PACKAGE, IG = SettingsStore.INSTAGRAM_PACKAGE;
    static int run(Context context, SettingsStore root) {
        int checks = 0;
        SettingsStore ytStore = root.forHost(YT), igStore = root.forHost(IG);
        HostPlaybackSession yt = new HostPlaybackSession(context, YT, null);
        HostPlaybackSession ig = new HostPlaybackSession(context, IG, null);
        RuntimeState.HostState ytState = RuntimeState.forHost(YT), igState = RuntimeState.forHost(IG);
        try {
            root.selectedApp(YT, true); root.selectedApp(IG, true); root.enabled(true);
            ytStore.enabled(true); igStore.enabled(true); root.floatingEnabled(false);
            ytStore.ceiling(2); igStore.ceiling(3);
            set(yt, "floating", new FloatingController(context, ytStore, null));
            set(ig, "floating", new FloatingController(context, igStore, null));
            LoopCounter ytCounter = (LoopCounter)get(yt, "counter");
            LoopCounter igCounter = (LoopCounter)get(ig, "counter");
            require(ytCounter != igCounter, "Counters are separate objects"); checks++;
            ytCounter.setTarget(2); igCounter.setTarget(3);
            ytCounter.observe(new Progress(0, 10), "youtube-page", 1000);
            igCounter.observe(new Progress(0, 10), "instagram-page", 1000);
            igCounter.observe(new Progress(1, 10), "instagram-page", 2000);
            igState.blocked = false; igState.current = 1; igState.status = "playback.counting"; igState.timedRemainingSeconds = 7;
            AdvanceGate igGate = (AdvanceGate)get(ig, "gate"); igGate.begin("instagram-old", 10, 2000);
            int igGeneration = (Integer)get(ig, "generation");
            String igDiagnostic = igCounter.diagnostic();
            Object igPrevious = get(igCounter, "previous");
            call(yt, "failClosed", new Class<?>[]{String.class}, "error.advance");
            require(ytState.blocked && ytState.current == 0, "YouTube fails closed"); checks++;
            require(!igState.blocked && igState.current == 1 && "playback.counting".equals(igState.status)
                    && igState.timedRemainingSeconds == 7, "YouTube failure preserves every Instagram display field"); checks++;
            require(igGate.pending() && (Integer)get(ig, "generation") == igGeneration, "Sibling pending request and callback generation survive"); checks++;
            require(igDiagnostic.equals(igCounter.diagnostic()) && get(igCounter, "previous") == igPrevious,
                    "Sibling accumulated counter evidence survives"); checks++;
            require(root.enabled() && igStore.enabled(), "One safety stop does not disable the other app or master"); checks++;
            for (String key : new String[]{ytStore.scopedKey("target"), ytStore.scopedKey("long_video_seconds"), ytStore.scopedKey("paused"), "skip_live"}) {
                ig.onSharedPreferenceChanged(root.preferences, key);
                require((Integer)get(ig, "generation") == igGeneration && igGate.pending() && get(igCounter, "previous") == igPrevious,
                        "Instagram ignores YouTube setting " + key); checks++;
            }
            int ytGeneration = (Integer)get(yt, "generation");
            for (String key : new String[]{igStore.scopedKey("target"), "skip_ads", "photo_whole_seconds", "fallback_seconds", "target"}) {
                yt.onSharedPreferenceChanged(root.preferences, key);
                require((Integer)get(yt, "generation") == ytGeneration && ytState.blocked,
                        "YouTube ignores unrelated or legacy setting " + key); checks++;
            }
            set(yt, "unresolvedLongAttempt", true);
            yt.onSharedPreferenceChanged(root.preferences, ytStore.scopedKey("target"));
            require(ytState.blocked && (Boolean)get(yt, "unresolvedLongAttempt"), "Own edits cannot clear an unresolved request"); checks++;
            require((Integer)get(ig, "generation") == igGeneration && get(igCounter, "previous") == igPrevious,
                    "Own unresolved guard cannot reset sibling evidence"); checks++;
            ytStore.enabled(false);
            yt.onSharedPreferenceChanged(root.preferences, ytStore.scopedKey("paused"));
            require(!ytStore.enabled() && !ytState.blocked && "off".equals(ytState.status), "Explicit host pause clears only its failed session"); checks++;
            require(igStore.enabled() && igGate.pending() && igState.current == 1, "Other app keeps its pending request after host pause"); checks++;
            LoopCounter.Result next = igCounter.observe(new Progress(2, 10), "instagram-page", 3000);
            require(next.current == 1 && !next.advance && !next.waitingForStart, "Sibling counter continues rather than reseeding"); checks++;
            root.enabled(false);
            yt.onSharedPreferenceChanged(root.preferences, "enabled");
            ig.onSharedPreferenceChanged(root.preferences, "enabled");
            require(!ytStore.enabled() && !igStore.enabled() && !igGate.pending(), "Master OFF stops both sessions"); checks++;
            require(ytState.current == 0 && igState.current == 0 && "off".equals(igState.status), "Master OFF clears both counters"); checks++;
            require((Integer)get(yt, "advanceRequests") == 0 && (Integer)get(ig, "advanceRequests") == 0,
                    "Isolation fixtures dispatch no transitions"); checks++;
            return checks;
        } catch (ReflectiveOperationException error) { throw new AssertionError("Host isolation wiring", error); }
        finally {
            root.enabled(false); yt.destroySession(); ig.destroySession();
            ytState.blocked = false; igState.blocked = false; ytState.current = 0; igState.current = 0;
            ytState.status = "off"; igState.status = "off"; ytState.timedRemainingSeconds = -1; igState.timedRemainingSeconds = -1;
        }
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
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
