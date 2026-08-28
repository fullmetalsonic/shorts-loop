package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.graphics.Rect;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.core.AdvanceGate;
import com.fullmetalsonic.shortsloop.core.LoopCounter;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;
import com.fullmetalsonic.shortsloop.core.PlaybackRestart;
import com.fullmetalsonic.shortsloop.core.Progress;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.overlay.FloatingController;
import com.fullmetalsonic.shortsloop.service.RuntimeState;
import com.fullmetalsonic.shortsloop.service.HostPlaybackSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Synthetic service wiring on disposable emulators; never binds a service or dispatches gestures. */
final class RecoveryServiceChecks {
    private static final String HOST = SettingsStore.YOUTUBE_PACKAGE;
    private static int checks;
    static int run(Context context, SettingsStore rootStore) {
        SettingsStore store = rootStore.forHost(HOST);
        RuntimeState.HostState state = RuntimeState.forHost(HOST);
        checks = 0;
        try {
            HostPlaybackSession service = new HostPlaybackSession(context, HOST, null);
            rootStore.enabled(true);
            set(service, "store", store); set(service, "activePackage", HOST);
            store.selectedApp(HOST, true); store.target(1); store.enabled(true);
            state.blocked = false;
            AdvanceGate gate = (AdvanceGate)get(service, "gate");
            LoopCounter counter = (LoopCounter)get(service, "counter"); counter.setTarget(1);
            gate.begin("old", 10, 0); require(gate.unavailable(4500) == AdvanceGate.State.FAILED, "Real gate timeout");
            set(service, "ordinaryRequestWindow", 7);
            int generation = (Integer)get(service, "generation");
            call(service, "advanceTimedOut", new Class<?>[0]);
            PlaybackRestart recovery = (PlaybackRestart)get(service, "restart");
            require(recovery.active() && !state.blocked, "Ordinary timeout remains observable");
            require((Integer)get(service, "generation") > generation, "Old request callback invalidated");
            require(state.current == 0 && state.status.equals(PlaybackRestart.WAITING), "Waiting visible");
            Rect page = new Rect(0, 0, 400, 700);
            observe(service, YouTubeSnapshot.advertisement(page).inWindow(7, page), 5000);
            observe(service, YouTubeSnapshot.livePreview("live", page).inWindow(7, page), 6000);
            observe(service, YouTubeSnapshot.withoutClock("clockless", page, false).inWindow(7, page), 7000);
            require(recovery.active() && (Integer)get(service, "advanceRequests") == 0, "No alternate skip while recovering");
            observe(service, snapshot(0, 8), 8000); observe(service, snapshot(1, 8), 9000);
            require(recovery.active(), "Wrong window cannot recover");
            observe(service, snapshot(0, 7), 10000);
            call(service, "interruptSession", new Class<?>[0]);
            observe(service, snapshot(2, 7), 11000);
            require(recovery.active(), "Interruption discards candidate, not guard");
            observe(service, snapshot(0, 7), 12000); observe(service, snapshot(1, 7), 13000);
            require(!recovery.active() && state.current == 1, "Fresh start rearms counter");
            require((Integer)get(service, "confirmedAdvances") == 0 && (Integer)get(service, "advanceRequests") == 0,
                    "Recovery is neither a request nor a confirmed advance");
            long now = 13000;
            for (int p = 2; p <= 10; p++) require(!counter.observe(new Progress(p, 10), "current", now += 1000).advance,
                    "No advance before full recovered play");
            require(counter.observe(new Progress(0, 10), "current", now + 1000).advance, "Full recovered play emits once");
            require(!counter.observe(new Progress(1, 10), "current", now + 2000).advance, "No duplicate emission");
            // All non-ordinary timeout modes retain the actual hard-stop wiring.
            for (String flag : new String[]{"pendingAd", "pendingLive", "pendingTimed", "pendingVisual", "pendingLong"}) {
                state.blocked = false; set(service, "ordinaryRequestWindow", 7); set(service, flag, true);
                call(service, "advanceTimedOut", new Class<?>[0]);
                require(state.blocked && !recovery.active(), "Hard stop preserved for " + flag);
            }
            state.blocked = false; recovery.begin(HOST, 7); store.target(0);
            observe(service, snapshot(0, 7), 40000); observe(service, snapshot(1, 7), 41000);
            require(recovery.active() && state.current == 0, "Zero prevents restart");
            store.target(1); store.enabled(false);
            observe(service, snapshot(0, 7), 42000); observe(service, snapshot(1, 7), 43000);
            require(recovery.active() && !store.enabled(), "OFF never re-enabled");
            store.enabled(true); set(service, "activePackage", SettingsStore.INSTAGRAM_PACKAGE);
            observe(service, snapshot(0, 7), 44000); observe(service, snapshot(1, 7), 45000);
            require(recovery.active(), "Host switch cannot recover in another selected app");
            set(service, "activePackage", HOST);
            Rect outside = new Rect(-1, 0, 400, 700);
            observe(service, new YouTubeSnapshot(new Progress(0, 10), "current", outside, "")
                    .inWindow(7, new Rect(0, 0, 400, 700)), 46000);
            observe(service, snapshot(2, 7), 47000);
            require(recovery.active(), "Out-of-window page drops evidence");
            observe(service, snapshot(0, 7), 48000);
            observe(service, new YouTubeSnapshot(new Progress(1, 10), "current", new Rect(0, 0, 400, 650), "")
                    .inWindow(7, new Rect(0, 0, 400, 700)), 49000);
            require(recovery.active(), "Changed page bounds cannot reuse start evidence");
            call(service, "invalidate", new Class<?>[0]); require(!recovery.active(), "Settings/session reset cancels recovery");
            set(service, "ordinaryRequestWindow", 7); store.target(0);
            call(service, "advanceTimedOut", new Class<?>[0]);
            require(state.blocked && !recovery.active(), "Zero cannot enter timeout recovery");
            state.blocked = false; store.target(1); set(service, "ordinaryRequestWindow", -1);
            call(service, "advanceTimedOut", new Class<?>[0]);
            require(state.blocked && !recovery.active(), "Unknown request window cannot enter recovery");
            state.blocked = false; store.enabled(true); store.skipLong(true); store.longVideoSeconds(10); store.target(0);
            require((Boolean)call(service, "longCandidate", new Class<?>[]{YouTubeSnapshot.class}, snapshot(0, 7)), "Length filter independent of zero plays");
            store.enabled(false);
            require(!(Boolean)call(service, "longCandidate", new Class<?>[]{YouTubeSnapshot.class}, snapshot(0, 7)), "Execution OFF blocks length filter");
            store.enabled(true); store.skipLong(false);
            require(!(Boolean)call(service, "longCandidate", new Class<?>[]{YouTubeSnapshot.class}, snapshot(0, 7)), "Length option OFF");
            store.skipLong(true); store.selectedApp(HOST, false);
            require(!(Boolean)call(service, "longCandidate", new Class<?>[]{YouTubeSnapshot.class}, snapshot(0, 7)), "Deselected host blocks length filter");
            store.selectedApp(HOST, true); store.longVideoSeconds(11);
            require(!(Boolean)call(service, "longCandidate", new Class<?>[]{YouTubeSnapshot.class}, snapshot(0, 7)), "Below threshold blocks length filter");
            store.longVideoSeconds(10);
            for (YouTubeSnapshot excluded : new YouTubeSnapshot[]{YouTubeSnapshot.advertisement(page),
                    YouTubeSnapshot.livePreview("live", page), YouTubeSnapshot.withoutClock("unknown", page, false)})
                require(!(Boolean)call(service, "longCandidate", new Class<?>[]{YouTubeSnapshot.class}, excluded.inWindow(7, page)), "Special page not a known long video");
            set(service, "longRequestWindow", 7); set(service, "longRequestWindowBounds", page); gate.begin("old-long", -1, 1000);
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(0, 8), 2200L)
                    == AdvanceGate.State.WAITING, "Wrong window cannot confirm long skip");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(0, 7), 2500L)
                    == AdvanceGate.State.WAITING, "Long skip waits for stable new page");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(1, 7), 2800L)
                    == AdvanceGate.State.CONFIRMED, "Stable different page confirms long skip");
            gate.begin("current", 62, 1000); set(service, "longPagerChanged", false);
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(0, 7), 2200L)
                    == AdvanceGate.State.WAITING, "Duration alone cannot confirm generic identity");
            set(service, "longPagerChanged", true);
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(0, 7), 2500L)
                    == AdvanceGate.State.WAITING, "Verified pager change needs stable progress");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(1, 7), 2800L)
                    == AdvanceGate.State.CONFIRMED, "Verified pager and fresh changed duration confirm generic identity");
            gate.begin("content:alpha", 62, 1000); set(service, "longPagerChanged", false); set(service, "longRequestContent", true);
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(0, 7).withContentIdentity("beta"), 2200L)
                    == AdvanceGate.State.WAITING, "Supplemental video identity must settle");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(1, 7).withContentIdentity("beta"), 2500L)
                    == AdvanceGate.State.WAITING, "Supplemental key cannot bypass missing actual YouTube pager geometry");
            gate.begin("content:alpha", 10, 1000);
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(0, 7).withContentIdentity("subset"), 2200L)
                    == AdvanceGate.State.WAITING, "Metadata subset is not independently a page change");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(1, 7).withContentIdentity("subset"), 2500L)
                    == AdvanceGate.State.WAITING, "Same-duration metadata mutation never confirms without pager evidence");
            gate.begin("content:alpha", 62, 1000);
            require("".equals(call(service, "longConfirmationIdentity", new Class<?>[]{YouTubeSnapshot.class}, snapshot(0, 7))),
                    "Content identity source stays empty when metadata disappears");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(0, 7), 2200L)
                    == AdvanceGate.State.WAITING, "Losing metadata cannot confirm through generic text");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(1, 7), 2500L)
                    == AdvanceGate.State.WAITING, "Stable missing metadata is not a different video");
            set(service, "longRequestContent", false); gate.begin("current", 10, 1000);
            require("current".equals(call(service, "longConfirmationIdentity", new Class<?>[]{YouTubeSnapshot.class}, snapshot(0, 7).withContentIdentity("beta"))),
                    "Generic identity source does not switch when metadata appears");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(0, 7).withContentIdentity("beta"), 2200L)
                    == AdvanceGate.State.WAITING, "Newly appearing metadata cannot change identity source");
            require(call(service, "inspectLongTransition", new Class<?>[]{YouTubeSnapshot.class, long.class}, snapshot(1, 7).withContentIdentity("beta"), 2500L)
                    == AdvanceGate.State.WAITING, "Identity source stays pinned for request lifetime");
            for (String key : new String[]{"target", "long_video_seconds", "skip_long"}) {
                state.blocked = false; set(service, "unresolvedLongAttempt", true);
                service.onSharedPreferenceChanged(store.preferences, store.scopedKey(key));
                require(state.blocked && !recovery.active(), "Unconfirmed long request cannot rearm through " + key);
            }
            set(service, "unresolvedLongAttempt", false);
            store.skipLong(false); store.target(1);
            Context displayContext = AppLocale.wrap(context);
            FloatingController floating = new FloatingController(context, store, null);
            TextView number = new TextView(context); set(floating, "number", number);
            floating.update(0, 1, PlaybackRestart.WAITING); require(number.getText().toString().equals(displayContext.getString(R.string.flo_wait)), "Waiting label");
            floating.update(0, 1, "blocked:error.advance"); require(number.getText().toString().equals(displayContext.getString(R.string.flo_stop)), "Stop label");
            floating.update(1, 1, PlaybackRestart.COUNTING); require(number.getText().toString().equals("1/1"), "Recovered count label");
            store.skipLong(true);
            floating.update(0, 0, LongVideoPolicy.zeroCountStatus(true, true, true)); require(number.getText().toString().equals(displayContext.getString(R.string.flo_rules)), "Zero length-filter label");
            floating.update(0, 0, LongVideoPolicy.CHECKING); require(number.getText().toString().equals(displayContext.getString(R.string.flo_long)), "Active length-filter label");
            floating.update(0, 0, LiveSkipPolicy.STATUS_CONFIRMING); require(number.getText().toString().equals(displayContext.getString(R.string.flo_next)), "Long setting does not mask live label");
            floating.update(0, 0, "ads.confirming"); require(number.getText().toString().equals(displayContext.getString(R.string.flo_ads)), "Long setting does not mask ad label");
            return checks;
        } catch (ReflectiveOperationException e) { throw new AssertionError("Service wiring check failed", e); }
        finally { rootStore.enabled(false); store.enabled(false); store.skipLong(false); state.blocked = false; state.current = 0; state.status = "off"; }
    }
    private static YouTubeSnapshot snapshot(double position, int window) {
        Rect bounds = new Rect(0, 0, 400, 700);
        return new YouTubeSnapshot(new Progress(position, 10), "current", bounds, "").inWindow(window, bounds);
    }
    private static void observe(Object service, YouTubeSnapshot value, long at) throws ReflectiveOperationException {
        call(service, "observeRestart", new Class<?>[]{YouTubeSnapshot.class, long.class}, value, at);
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
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
}
