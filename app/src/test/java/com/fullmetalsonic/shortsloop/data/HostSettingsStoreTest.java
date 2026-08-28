package com.fullmetalsonic.shortsloop.data;

import com.fullmetalsonic.shortsloop.core.ModePolicy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class HostSettingsStoreTest {
    private static final String YT = SettingsStore.YOUTUBE_PACKAGE, IG = SettingsStore.INSTAGRAM_PACKAGE;

    @Test public void migrationCopiesBothHostsAtomicallyAndPreservesLegacyKeys() {
        MemoryPreferences p = new MemoryPreferences(Map.of("settings_version", 1, "ceiling", 7, "target", 0,
                "last_nonzero", 4, "tap_mode", ModePolicy.TOGGLE, "skip_long", true,
                "long_video_seconds", 321, "x", 0.23f, "y", 0.67f, "enabled", false));
        Map<String, ?> before = p.getAll(); SettingsStore root = new SettingsStore(p);
        p.registerOnSharedPreferenceChangeListener((changed, key) -> {
            assertEquals(1, changed.getInt("host_settings_version", 0));
            assertEquals(7, changed.getInt("host.youtube.ceiling", 0));
            assertEquals(7, changed.getInt("host.instagram.ceiling", 0));
        });
        SettingsStore yt = root.forHost(YT), ig = root.forHost(IG);
        assertEquals(1, p.writes.size());
        for (Map.Entry<String, ?> entry : before.entrySet()) assertEquals(entry.getValue(), p.getAll().get(entry.getKey()));
        for (SettingsStore host : new SettingsStore[]{yt, ig}) {
            assertEquals(7, host.ceiling()); assertEquals(0, host.target()); assertEquals(4, host.lastNonZero());
            assertEquals(ModePolicy.TOGGLE, host.tapMode()); assertTrue(host.skipLong());
            assertEquals(321, host.longVideoSeconds()); assertEquals(0.23f, host.x(), 0); assertEquals(0.67f, host.y(), 0);
            assertFalse(host.hostPaused()); assertFalse(host.enabled()); assertSame(p, host.preferences);
        }
    }
    @Test public void hostEditsNeverRewriteLegacyOrSiblingSettings() {
        SettingsStore root = new SettingsStore(new MemoryPreferences());
        SettingsStore yt = root.forHost(YT), ig = root.forHost(IG);
        Map<String, ?> before = root.preferences.getAll();
        yt.ceiling(9); yt.target(3); yt.tapMode(ModePolicy.TOGGLE); yt.target(0);
        yt.skipLong(true); yt.longVideoSeconds(3600); yt.position(0.1f, 0.8f); yt.enabled(false);
        for (Map.Entry<String, ?> entry : before.entrySet())
            if (!entry.getKey().startsWith("host.youtube.")) assertEquals(entry.getValue(), root.preferences.getAll().get(entry.getKey()));
        assertEquals(2, ig.ceiling()); assertEquals(2, ig.target()); assertEquals(60, ig.longVideoSeconds());
        assertEquals(2, root.ceiling()); assertFalse(root.enabled());
    }
    @Test public void migrationIsOnceOnlyAndNeverReplacesExistingScopedValues() {
        MemoryPreferences p = new MemoryPreferences(Map.of("settings_version", 1, "ceiling", 7, "target", 3,
                "host.youtube.ceiling", 9, "host.youtube.target", 0, "host.youtube.paused", true));
        SettingsStore yt = new SettingsStore(p).forHost(YT);
        assertEquals(9, yt.ceiling()); assertEquals(0, yt.target()); assertTrue(yt.hostPaused());
        int writes = p.writes.size();
        SettingsStore reopened = new SettingsStore(p).forHost(YT); new SettingsStore(p).forHost(IG);
        assertEquals(writes, p.writes.size()); assertEquals(0, reopened.target()); assertTrue(reopened.hostPaused());
    }
    @Test public void masterStartResumesOnlySelectedHostsAndUsesEachCeiling() {
        SettingsStore root = new SettingsStore(new MemoryPreferences());
        SettingsStore yt = root.forHost(YT), ig = root.forHost(IG);
        yt.ceiling(7); ig.ceiling(4); yt.target(0); ig.target(1); yt.enabled(false); ig.enabled(false);
        root.start(); assertTrue(yt.enabled()); assertEquals(7, yt.target());
        assertFalse(ig.enabled()); assertTrue(ig.hostPaused()); assertEquals(1, ig.target());
        root.selectedApp(IG, true); root.start();
        assertTrue(ig.enabled()); assertEquals(4, ig.target()); assertFalse(ig.hostPaused());
    }
    @Test public void hostStopAndResumeNeverChangeSiblingOrMaster() {
        SettingsStore root = new SettingsStore(new MemoryPreferences()); root.selectedApp(IG, true);
        SettingsStore yt = root.forHost(YT), ig = root.forHost(IG);
        yt.ceiling(7); ig.ceiling(4); root.start(); ig.target(2);
        yt.enabled(false); assertTrue(root.enabled()); assertFalse(yt.enabled()); assertTrue(ig.enabled());
        yt.start(); assertTrue(yt.enabled()); assertEquals(7, yt.target()); assertEquals(2, ig.target());
        root.enabled(false); yt.enabled(false); yt.start();
        assertFalse(root.enabled()); assertFalse(yt.enabled()); assertFalse(ig.enabled());
    }
    @Test public void reopeningNeverStartsMasterOrResumesPausedHost() {
        MemoryPreferences p = new MemoryPreferences(); SettingsStore root = new SettingsStore(p);
        SettingsStore yt = root.forHost(YT); root.start(); yt.enabled(false); root.enabled(false);
        SettingsStore reopened = new SettingsStore(new MemoryPreferences(p.getAll()));
        assertFalse(reopened.enabled()); assertTrue(reopened.forHost(YT).hostPaused());
        assertFalse(reopened.forHost(YT).enabled());
    }
    @Test public void zeroCountAndGlobalFloatingRemainIndependent() {
        SettingsStore root = new SettingsStore(new MemoryPreferences()); root.selectedApp(IG, true);
        SettingsStore yt = root.forHost(YT), ig = root.forHost(IG);
        yt.ceiling(0); ig.ceiling(7); root.skipAds(true); root.start(); root.floatingEnabled(false);
        assertTrue(yt.enabled()); assertTrue(ig.enabled()); assertEquals(0, yt.target()); assertEquals(7, ig.target());
        assertTrue(ig.skipAds()); assertFalse(yt.floatingEnabled()); assertFalse(ig.floatingEnabled());
    }
    @Test public void exclusiveFeaturesCannotBecomeActiveOnWrongHost() {
        SettingsStore root = new SettingsStore(new MemoryPreferences());
        root.skipAds(true); root.photoEnabled(true); root.visualAssist(true); root.timedFallback(true); root.skipLive(true);
        SettingsStore yt = root.forHost(YT), ig = root.forHost(IG);
        assertFalse(yt.skipAds()); assertFalse(yt.photoEnabled()); assertFalse(yt.visualAssist()); assertFalse(yt.timedFallback());
        assertTrue(yt.skipLive()); assertFalse(ig.skipLive()); assertTrue(ig.skipAds()); assertTrue(ig.photoEnabled());
        assertTrue(ig.visualAssist()); assertTrue(ig.timedFallback()); assertTrue(root.skipLive());
    }
    @Test public void changeRoutingIgnoresSiblingAndPresentationKeys() {
        assertTrue(SettingsStore.affectsHost("host.youtube.target", YT));
        assertFalse(SettingsStore.affectsHost("host.youtube.target", IG));
        assertTrue(SettingsStore.affectsHost("host.instagram.paused", IG));
        for (String key : new String[]{"host.youtube.x", "host.youtube.y", "floating_enabled", "host_settings_version", "target"})
            assertFalse(SettingsStore.affectsHost(key, YT));
        for (String key : new String[]{"skip_ads", "photo_enabled", "photo_slide_seconds", "timed_fallback", "visual_assist"}) {
            assertTrue(SettingsStore.affectsHost(key, IG)); assertFalse(SettingsStore.affectsHost(key, YT));
        }
        assertTrue(SettingsStore.affectsHost("skip_live", YT)); assertFalse(SettingsStore.affectsHost("skip_live", IG));
        assertTrue(SettingsStore.affectsHost("enabled", YT)); assertTrue(SettingsStore.affectsHost("enabled", IG));
        assertFalse(SettingsStore.affectsHost(null, YT)); assertFalse(SettingsStore.affectsHost("enabled", null));
    }
    @Test public void invalidTypesFallBackWithoutChangingLegacyEvidence() {
        Map<String, Object> values = new HashMap<>(); values.put("settings_version", 1);
        values.put("ceiling", "bad"); values.put("target", true); values.put("x", "bad"); values.put("y", 12);
        values.put("long_video_seconds", -1); values.put("tap_mode", 100);
        MemoryPreferences p = new MemoryPreferences(values); SettingsStore yt = new SettingsStore(p).forHost(YT);
        assertEquals(2, yt.ceiling()); assertEquals(2, yt.target()); assertEquals(60, yt.longVideoSeconds());
        assertEquals(0.9f, yt.x(), 0); assertEquals(0.35f, yt.y(), 0);
        for (Map.Entry<String, Object> entry : values.entrySet()) assertEquals(entry.getValue(), p.getAll().get(entry.getKey()));
    }
    @Test public void futureHostSchemaIsReadOnlyAndUnsupportedHostIsRejected() {
        MemoryPreferences p = new MemoryPreferences(Map.of("settings_version", 1, "host_settings_version", 9,
                "host.youtube.ceiling", 8, "host.youtube.target", 6));
        SettingsStore root = new SettingsStore(p); assertEquals(6, root.forHost(YT).target()); assertEquals(0, p.writes.size());
        for (String invalid : new String[]{null, "other.app", ""}) {
            try { root.forHost(invalid); fail("Unsupported host"); } catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void malformedPauseFlagFailsClosedUntilExplicitResume() {
        MemoryPreferences p = new MemoryPreferences(Map.of("settings_version", 1, "enabled", true,
                "host_settings_version", 1, "host.youtube.paused", "false"));
        SettingsStore yt = new SettingsStore(p).forHost(YT);
        assertTrue(yt.hostPaused()); assertFalse(yt.enabled()); assertEquals(0, p.writes.size());
        yt.start(); assertFalse(yt.hostPaused()); assertTrue(yt.enabled());
    }
    @Test public void dualModeIsOptInSharedAndPreservesAllOtherSettings() {
        SettingsStore root = new SettingsStore(new MemoryPreferences());
        SettingsStore yt = root.forHost(YT), ig = root.forHost(IG);
        yt.ceiling(8); ig.ceiling(3); yt.enabled(false); root.enabled(false);
        Map<String, ?> before = root.preferences.getAll();
        assertFalse(root.dualMode()); root.dualMode(true);
        assertTrue(yt.dualMode()); assertTrue(ig.dualMode()); assertFalse(root.enabled());
        for (var entry : before.entrySet()) assertEquals(entry.getValue(), root.preferences.getAll().get(entry.getKey()));
        root.dualMode(false); assertEquals(8, yt.ceiling()); assertEquals(3, ig.ceiling()); assertTrue(yt.hostPaused());
        assertTrue(SettingsStore.affectsHost("dual_mode",YT)); assertTrue(SettingsStore.affectsHost("dual_mode",IG));
    }
    @Test public void malformedDualModeNeverEnablesTwoWindowAutomation() {
        SettingsStore root = new SettingsStore(new MemoryPreferences(Map.of("dual_mode", "true")));
        assertFalse(root.dualMode()); assertFalse(root.forHost(YT).dualMode());
    }
}
