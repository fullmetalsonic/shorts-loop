package com.fullmetalsonic.shortsloop.data;

import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** In-memory preference contracts, not Android disk/update-install verification. */
public class LongVideoSettingsStoreTest {
    @Test public void absentKeysDefaultOffAndSixtyWithoutWritingPreferences() {
        MemoryPreferences prefs = new MemoryPreferences(Map.of("settings_version", 1, "target", 2));
        Map<String, ?> original = prefs.getAll();
        SettingsStore store = new SettingsStore(prefs);
        assertFalse(store.skipLong());
        assertEquals(LongVideoPolicy.DEFAULT_SECONDS, store.longVideoSeconds());
        assertEquals(2, store.target());
        assertEquals(original, prefs.getAll());
        assertTrue(prefs.writes.isEmpty());
    }

    @Test public void wrongToggleTypesReadOffWithoutChangingOriginalValues() {
        for (Object invalid : new Object[]{"true", 1, 1L, 1.0f}) {
            MemoryPreferences prefs = new MemoryPreferences(Map.of("settings_version", 1, "skip_long", invalid,
                    "long_video_seconds", 75));
            Map<String, ?> original = prefs.getAll();
            SettingsStore store = new SettingsStore(prefs);
            assertFalse(store.skipLong());
            assertEquals(75, store.longVideoSeconds());
            assertEquals(original, prefs.getAll());
            assertTrue(prefs.writes.isEmpty());
        }
    }

    @Test public void wrongThresholdTypesReadSixtyWithoutChangingOriginalValues() {
        for (Object invalid : new Object[]{"60", 60L, true, 60.0f}) {
            MemoryPreferences prefs = new MemoryPreferences(Map.of("settings_version", 1, "skip_long", true,
                    "long_video_seconds", invalid));
            Map<String, ?> original = prefs.getAll();
            SettingsStore store = new SettingsStore(prefs);
            assertTrue(store.skipLong());
            assertEquals(LongVideoPolicy.DEFAULT_SECONDS, store.longVideoSeconds());
            assertEquals(original, prefs.getAll());
            assertTrue(prefs.writes.isEmpty());
        }
    }

    @Test public void invalidNumericThresholdReadsSixtyWithoutRepairingStoredValue() {
        for (int invalid : new int[]{Integer.MIN_VALUE, -1, 0, 3601, Integer.MAX_VALUE}) {
            MemoryPreferences prefs = new MemoryPreferences(Map.of("settings_version", 1,
                    "long_video_seconds", invalid));
            Map<String, ?> original = prefs.getAll();
            SettingsStore store = new SettingsStore(prefs);
            assertEquals(LongVideoPolicy.DEFAULT_SECONDS, store.longVideoSeconds());
            assertEquals(original, prefs.getAll());
            assertTrue(prefs.writes.isEmpty());
        }
    }

    @Test public void validStoredBoundaryValuesSurviveReadWithoutWrites() {
        for (int valid : new int[]{1, 60, 75, 3600}) {
            MemoryPreferences prefs = new MemoryPreferences(Map.of("settings_version", 1, "skip_long", true,
                    "long_video_seconds", valid));
            SettingsStore store = new SettingsStore(prefs);
            assertTrue(store.skipLong());
            assertEquals(valid, store.longVideoSeconds());
            assertTrue(prefs.writes.isEmpty());
        }
    }

    @Test public void savedChoicesSurviveRecreationWithoutChangingOtherPreferences() {
        MemoryPreferences prefs = new MemoryPreferences();
        SettingsStore store = new SettingsStore(prefs);
        store.ceiling(7); store.target(0); store.enabled(false); store.skipAds(true);
        store.skipLive(true); store.liveDelaySeconds(5); store.timedFallback(true); store.fallbackSeconds(25);
        store.visualAssist(true); store.floatingEnabled(false); store.position(0.25f, 0.75f);
        store.selectedApp(SettingsStore.YOUTUBE_PACKAGE, false);
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
        Map<String, Object> originalOthers = new HashMap<>(prefs.getAll());
        store.skipLong(true); store.longVideoSeconds(321);
        Map<String, Object> changedOthers = new HashMap<>(prefs.getAll());
        changedOthers.remove("skip_long"); changedOthers.remove("long_video_seconds");
        assertEquals(originalOthers, changedOthers);

        MemoryPreferences recreatedPrefs = new MemoryPreferences(prefs.getAll());
        SettingsStore recreated = new SettingsStore(recreatedPrefs);
        assertTrue(recreated.skipLong()); assertEquals(321, recreated.longVideoSeconds());
        assertEquals(prefs.getAll(), recreatedPrefs.getAll());
        assertTrue(recreatedPrefs.writes.isEmpty());
        assertEquals(0, recreated.target()); assertEquals(7, recreated.ceiling());
        assertFalse(recreated.enabled());
    }

    @Test public void turningOptionOffPreservesThresholdAndNeverStartsExecution() {
        MemoryPreferences prefs = new MemoryPreferences(Map.of("settings_version", 1, "target", 0,
                "ceiling", 3, "enabled", false));
        SettingsStore store = new SettingsStore(prefs);
        store.skipLong(true); store.longVideoSeconds(3600); store.skipLong(false);
        SettingsStore recreated = new SettingsStore(new MemoryPreferences(prefs.getAll()));
        assertFalse(recreated.skipLong()); assertEquals(3600, recreated.longVideoSeconds());
        assertEquals(0, recreated.target()); assertEquals(3, recreated.ceiling());
        assertFalse(recreated.enabled());
    }

    @Test public void legacyMigrationRetainsLongVideoChoicesAndZeroCountMeaning() {
        MemoryPreferences prefs = new MemoryPreferences(Map.of("target", 0, "last_nonzero", 2,
                "skip_long", true, "long_video_seconds", 90));
        SettingsStore store = new SettingsStore(prefs);
        assertTrue(store.skipLong()); assertEquals(90, store.longVideoSeconds());
        assertEquals(0, store.target()); assertEquals(2, store.ceiling());
        assertFalse(store.enabled()); assertEquals(1, prefs.writes.size());
    }
}
