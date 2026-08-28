package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.AdvanceGate;
import com.fullmetalsonic.shortsloop.core.NormalizedLoopCounter;
import com.fullmetalsonic.shortsloop.core.NormalizedProgress;
import com.fullmetalsonic.shortsloop.core.TikTokPageTransition;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;
import com.fullmetalsonic.shortsloop.core.PhotoReelTracker;
import com.fullmetalsonic.shortsloop.detection.PhotoFrame;
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
            TikTokPageTransition.Frame mapped = mapped(tt, initial);
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
            require(!countKey.equals(countKey(tt, initial.withContentIdentity("tiktok-render:SurfaceView"))),
                    "Renderer changes reset count evidence without inventing a new source-node identity");

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
            specialPolicies(tt, scoped, root);
            return checks;
        } catch (ReflectiveOperationException error) { throw new AssertionError("Normalized session safety wiring", error); }
        finally {
            tt.destroySession(); ig.destroySession(); restore(root.preferences, original);
            copyInto(savedTt, ttState); copyInto(savedIg, igState);
        }
    }
    private void arm(HostPlaybackSession session, YouTubeSnapshot value) throws ReflectiveOperationException {
        ((TikTokPageTransition)get(session, "tiktokTransition")).begin(mapped(session, value), 1000);
        ((AdvanceGate)get(session, "gate")).begin(value.identity, -1, 1000);
        set(session, "pendingNormalized", true);
        set(session, "unresolvedNormalizedAttempt", true);
    }
    private void assertCleared(HostPlaybackSession session, String label) throws ReflectiveOperationException {
        require(!((TikTokPageTransition)get(session, "tiktokTransition")).pending(), label + ": strict tracker cancelled");
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
    private static TikTokPageTransition.Frame mapped(HostPlaybackSession session, YouTubeSnapshot value) throws ReflectiveOperationException {
        return (TikTokPageTransition.Frame)call(session, "normalizedFrame", new Class<?>[]{YouTubeSnapshot.class}, value);
    }
    private void specialPolicies(HostPlaybackSession session, SettingsStore tt, SettingsStore root) throws ReflectiveOperationException {
        root.enabled(true); tt.enabled(true); tt.target(1); tt.skipAds(false); tt.timedFallback(true); tt.photoEnabled(true);
        tt.skipLong(true); tt.longVideoSeconds(60);
        YouTubeSnapshot clockless = special(false, true, null, null);
        YouTubeSnapshot dotAd = special(true, false, null, null);
        YouTubeSnapshot timedAd = special(true, true, null, null);
        YouTubeSnapshot clock = special(false, false, new Progress(2, 120), null);
        YouTubeSnapshot adClock = special(true, false, new Progress(2, 120), null);
        YouTubeSnapshot adNormalized = frame("A", "media-A", 7, .02).withAd(true);
        PhotoFrame picture = new PhotoFrame(new Rect(0, 200, 1000, 1400), new PhotoReelPolicy.Position(4, 5));
        YouTubeSnapshot photoAd = special(true, false, null, picture);
        require(candidate(session, "timedCandidate", clockless), "Known clockless TikTok has an opt-in timer");
        require(candidate(session, "timedCandidate", timedAd), "Ad opt-out permits a proven ordinary clockless ad video");
        require(!candidate(session, "timedCandidate", dotAd), "Dot advertisement never becomes ordinary fallback");
        require(!candidate(session, "timedCandidate", photoAd), "Photo cannot become video fallback");
        require(!candidate(session, "timedCandidate", frame("A", "m", 7, 0)), "Valid zero range is progress, not missing progress");
        require(!candidate(session, "timedCandidate", clock), "Real seconds exclude fallback");
        require(mapped(session, adNormalized) != null, "Ad opt-out keeps valid ordinary normalized playback");
        require(candidate(session, "longCandidate", clock) && candidate(session, "longCandidate", adClock),
                "Actual known duration enables the long filter even on opted-out ads");
        require(!candidate(session, "longCandidate", adNormalized), "Normalized units never become seconds");
        require(!same(session, adNormalized, adNormalized.withAd(false)), "Raw ad type change cancels deferred work");
        require(!same(session, photoAd, photoAd.withAd(false)), "Raw photo ad type change cancels deferred work");
        require(same(session, photoAd, photoAd), "Same indexed photo ad remains comparable");
        String photoTimer = (String)call(session, "photoTimingIdentity", new Class<?>[]{YouTubeSnapshot.class}, photoAd);
        require(photoTimer.equals(call(session, "photoTimingIdentity", new Class<?>[]{YouTubeSnapshot.class},
                photoAd.withNormalizedIdentity("pager", "media-B", 7))), "Whole-post timer survives normal media slide changes");
        for (YouTubeSnapshot changed : new YouTubeSnapshot[]{photoAd.withNormalizedIdentity("new", "media-A", 7),
                photoAd.withNormalizedIdentity("pager", "media-A", 8)})
            require(!photoTimer.equals(call(session, "photoTimingIdentity", new Class<?>[]{YouTubeSnapshot.class}, changed)),
                    "Photo timer resets for pager/feed-index changes");
        require(!same(session, photoAd, photoAd.withNormalizedIdentity("other-pager", "media-A", 7)),
                "Photo deferred action cannot move to another pager");
        require(!same(session, photoAd, photoAd.withNormalizedIdentity("pager", "media-A", 8)),
                "Photo deferred action cannot use another feed index");
        set(session, "photoRequestPagerKey", "pager"); set(session, "photoRequestBounds", page()); set(session, "photoRequestIndex", 7);
        require(photoScope(session, photoAd, PhotoReelTracker.Action.SLIDE), "Horizontal movement keeps feed index");
        require(!photoScope(session, photoAd, PhotoReelTracker.Action.REEL), "Same feed index cannot prove vertical move");
        require(photoScope(session, photoAd.withNormalizedIdentity("pager", "media-B", 8), PhotoReelTracker.Action.REEL),
                "Vertical photo movement permits exact next feed index");
        for (int index : new int[]{-1, 6, 7, 9}) require(!photoScope(session, photoAd.withNormalizedIdentity("pager", "media-B", index),
                PhotoReelTracker.Action.REEL), "Missing/backward/skipped index cannot prove photo move");
        require(!photoScope(session, photoAd.withNormalizedIdentity("other", "media-B", 8), PhotoReelTracker.Action.REEL),
                "Different pager cannot confirm a photo move");
        String preparation = (String)call(session, "preparationIdentity", new Class<?>[]{YouTubeSnapshot.class}, clockless);
        String secondsKey = (String)call(session, "ordinaryCounterKey", new Class<?>[]{YouTubeSnapshot.class}, clockless);
        for (YouTubeSnapshot changed : new YouTubeSnapshot[]{clockless.withIdentity("B"),
                clockless.withNormalizedIdentity("other", "media-A", 7), clockless.withNormalizedIdentity("pager", "media-B", 7),
                clockless.withNormalizedIdentity("pager", "media-A", 8), clockless.withContentIdentity("renderer-changed")}) {
            require(!preparation.equals(call(session, "preparationIdentity", new Class<?>[]{YouTubeSnapshot.class}, changed)),
                    "Changed page/pager/media/index/renderer resets prepared time evidence");
            require(!secondsKey.equals(call(session, "ordinaryCounterKey", new Class<?>[]{YouTubeSnapshot.class}, changed)),
                    "Changed source resets actual-seconds count evidence too");
        }
        require(transitionFrame(session, photoAd).fraction == -1 && transitionFrame(session, dotAd).fraction == -1,
                "Known photo/ad destinations are represented without fabricated clocks");
        require(transitionFrame(session, clock).fraction == 2.0 / 120, "Actual seconds can confirm forward playback");
        require(transitionFrame(session, new YouTubeSnapshot(null, "unknown", page(), "").inWindow(7, window())) == null,
                "Unknown snapshot has no TikTok transition authority");
        require(transitionFrame(session, clockless.withNormalizedIdentity("", "media", -1)) == null,
                "Missing independent pager key cannot confirm movement");
        tt.skipAds(true);
        require(mapped(session, adNormalized) == null && !candidate(session, "timedCandidate", timedAd)
                && !candidate(session, "longCandidate", adClock), "Enabled ad policy takes precedence over normal rules");
        tt.target(0);
        require(!candidate(session, "timedCandidate", clockless), "Count zero stops TikTok fallback");
        require(candidate(session, "longCandidate", clock), "Count zero preserves explicit long-video filter");
        require((Boolean)call(session, "adSkippingEnabled", new Class<?>[0])
                && (Boolean)call(session, "photoSkippingEnabled", new Class<?>[0]), "Ads and photos are independent of count zero");
        root.enabled(false);
        require(!candidate(session, "timedCandidate", clockless) && !candidate(session, "longCandidate", clock)
                && !(Boolean)call(session, "adSkippingEnabled", new Class<?>[0])
                && !(Boolean)call(session, "photoSkippingEnabled", new Class<?>[0]), "Execution OFF stops all TikTok policies");
        session.onSharedPreferenceChanged(root.preferences, "enabled");
        noInput(session, "All special policy fixtures are read-only");
    }
    private static boolean candidate(HostPlaybackSession session, String name, YouTubeSnapshot value) throws ReflectiveOperationException {
        return (Boolean)call(session, name, new Class<?>[]{YouTubeSnapshot.class}, value);
    }
    private static boolean photoScope(HostPlaybackSession session, YouTubeSnapshot value, PhotoReelTracker.Action action) throws ReflectiveOperationException {
        return (Boolean)call(session, "samePhotoRequestScope", new Class<?>[]{YouTubeSnapshot.class, PhotoReelTracker.Action.class}, value, action);
    }
    private static TikTokPageTransition.Frame transitionFrame(HostPlaybackSession session, YouTubeSnapshot value) throws ReflectiveOperationException {
        return (TikTokPageTransition.Frame)call(session, "tiktokFrame", new Class<?>[]{YouTubeSnapshot.class}, value);
    }
    private static YouTubeSnapshot special(boolean ad, boolean clockless, Progress clock, PhotoFrame photo) {
        return YouTubeSnapshot.tiktokPage("A", page(), clock, null, ad, clockless, photo)
                .withNormalizedIdentity("pager", "media-A", 7).withPhotoPageKey("media-A").inWindow(7, window());
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
