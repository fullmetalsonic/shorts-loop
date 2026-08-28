package com.fullmetalsonic.shortsloop.data;

import com.fullmetalsonic.shortsloop.core.AdSkipPolicy;
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutPolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class LiveSettingsStoreTest {
    @Test public void missingLiveKeysDefaultOffAndImmediateWithoutMigrationWrites() {
        MemoryPreferences prefs = new MemoryPreferences(Map.of("settings_version", 1, "target", 2));
        SettingsStore store = new SettingsStore(prefs);
        assertFalse(store.skipLive()); assertEquals(0, store.liveDelaySeconds());
        assertEquals(2, store.target()); assertEquals(0, prefs.writes.size());
    }
    @Test public void liveSettingsSurviveRestoreAndDoNotChangeOtherOptions() {
        MemoryPreferences prefs = new MemoryPreferences(); SettingsStore store = new SettingsStore(prefs);
        store.ceiling(7); store.target(0); store.skipAds(true); store.fallbackSeconds(25); store.timedFallback(true);
        store.skipLive(true); store.liveDelaySeconds(5);
        SettingsStore restored = new SettingsStore(new MemoryPreferences(prefs.getAll()));
        assertTrue(restored.skipLive()); assertEquals(5, restored.liveDelaySeconds());
        assertEquals(7, restored.ceiling()); assertEquals(0, restored.target());
        assertTrue(restored.skipAds()); assertTrue(restored.timedFallback()); assertEquals(25, restored.fallbackSeconds());
    }
    @Test public void youtubeDeselectionPreservesPreferenceButDisablesLive() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.skipLive(true); store.liveDelaySeconds(60); store.start();
        store.selectedApp(SettingsStore.YOUTUBE_PACKAGE, false);
        assertTrue(store.skipLive()); assertEquals(60, store.liveDelaySeconds());
        assertFalse(LiveSkipPolicy.enabled(store.enabled(), store.skipLive(), store.youtubeEnabled()));
        store.selectedApp(SettingsStore.YOUTUBE_PACKAGE, true);
        assertTrue(LiveSkipPolicy.enabled(store.enabled(), store.skipLive(), store.youtubeEnabled()));
    }
    @Test public void zeroRepeatAllowsLiveAndAdsButNotInstagramTimer() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(0); store.skipLive(true); store.skipAds(true); store.timedFallback(true);
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true); store.start();
        assertEquals(0, store.target());
        assertTrue(LiveSkipPolicy.enabled(store.enabled(), store.skipLive(), store.youtubeEnabled()));
        assertTrue(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.instagramEnabled()));
        assertFalse(ClocklessTimeoutPolicy.enabled(store.enabled(), store.target(), store.instagramEnabled(), store.timedFallback()));
        store.enabled(false);
        assertFalse(LiveSkipPolicy.enabled(store.enabled(), store.skipLive(), store.youtubeEnabled()));
        assertFalse(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.instagramEnabled()));
    }
    @Test public void wrongPreferenceTypesRecoverOffAndZeroWithoutOverwritingOriginals() {
        for (Object value : new Object[]{"5", 5L, true, 5.5f}) {
            MemoryPreferences prefs = new MemoryPreferences(Map.of("settings_version", 1, "skip_live", "true", "live_delay_seconds", value));
            SettingsStore store = new SettingsStore(prefs);
            assertFalse(store.skipLive()); assertEquals(0, store.liveDelaySeconds());
            assertEquals(value, prefs.getAll().get("live_delay_seconds")); assertEquals(0, prefs.writes.size());
        }
    }
    @Test public void corruptNumericDelayUsesDefaultWhileSetterClampsBounds() {
        for (int value : new int[]{Integer.MIN_VALUE, -1, 61, Integer.MAX_VALUE}) {
            SettingsStore store = new SettingsStore(new MemoryPreferences(Map.of("settings_version", 1, "live_delay_seconds", value)));
            assertEquals(0, store.liveDelaySeconds());
        }
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.liveDelaySeconds(-1); assertEquals(0, store.liveDelaySeconds());
        store.liveDelaySeconds(61); assertEquals(60, store.liveDelaySeconds());
    }
    @Test public void legacyMigrationPreservesNewLiveKeysAndOldCountMeaning() {
        MemoryPreferences prefs = new MemoryPreferences(Map.of("target", 0, "last_nonzero", 2, "skip_live", true, "live_delay_seconds", 9));
        SettingsStore store = new SettingsStore(prefs);
        assertEquals(0, store.target()); assertEquals(2, store.ceiling());
        assertTrue(store.skipLive()); assertEquals(9, store.liveDelaySeconds());
        assertFalse(store.enabled()); assertEquals(1, prefs.writes.size());
    }
    @Test public void turningLiveOffDoesNotChangeDelayOrRepeatCount() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(3); store.skipLive(true); store.liveDelaySeconds(12); store.skipLive(false);
        assertFalse(store.skipLive()); assertEquals(12, store.liveDelaySeconds());
        assertEquals(3, store.ceiling()); assertEquals(3, store.target());
        assertFalse(store.skipAds()); assertEquals(10, store.fallbackSeconds());
    }
}
