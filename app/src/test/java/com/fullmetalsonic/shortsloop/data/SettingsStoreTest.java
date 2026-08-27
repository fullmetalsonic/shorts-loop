package com.fullmetalsonic.shortsloop.data;

import com.fullmetalsonic.shortsloop.core.ModePolicy;
import com.fullmetalsonic.shortsloop.core.AdSkipPolicy;
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutPolicy;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises the production store against an in-memory SharedPreferences interface. */
public class SettingsStoreTest {
    @Test public void zeroPlayStartCanEnableOnlyAdsWhileTimerStaysOff() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(0); store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
        store.skipAds(true); store.timedFallback(true); store.start();
        assertEquals(0, store.target()); assertTrue(store.enabled());
        assertTrue(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.instagramEnabled()));
        assertFalse(ClocklessTimeoutPolicy.enabled(store.enabled(), store.target(), store.instagramEnabled(), store.timedFallback()));
        store.enabled(false);
        assertFalse(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.instagramEnabled()));
    }
    @Test public void adOptionAndInstagramSelectionStillGateZeroPlayMode() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(0); store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true); store.start();
        assertFalse(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.instagramEnabled()));
        store.skipAds(true); store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, false);
        assertFalse(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.instagramEnabled()));
        assertEquals(0, store.target()); assertTrue(store.enabled());
    }
    @Test public void freshInstallHasExplicitCompatibleDefaults() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        assertEquals(2, store.ceiling()); assertEquals(2, store.target());
        assertEquals(ModePolicy.ROTARY, store.tapMode());
        assertTrue(store.floatingEnabled()); assertTrue(store.youtubeEnabled()); assertFalse(store.instagramEnabled());
        assertFalse(store.enabled()); assertEquals(1, preferences.getInt("settings_version", 0));
        assertEquals(1, preferences.writes.size());
    }

    @Test public void legacyPositiveTargetMigratesToCeilingAndTargetTogether() {
        for (int target : new int[] {1, 2}) {
            MemoryPreferences preferences = new MemoryPreferences(Map.of("target", target, "last_nonzero", 2));
            SettingsStore store = new SettingsStore(preferences);
            assertEquals(target, store.target()); assertEquals(target, store.ceiling());
            assertEquals(1, preferences.writes.size());
        }
    }

    @Test public void legacyZeroStaysZeroAndPreservesLastNonzeroCeiling() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("target", 0, "last_nonzero", 1));
        SettingsStore store = new SettingsStore(preferences);
        assertEquals(0, store.target()); assertEquals(1, store.ceiling());
        assertEquals(1, store.lastNonZero()); assertFalse(store.enabled());
    }

    @Test public void legacyZeroWithMissingLastCountUsesTwoAsCeiling() {
        SettingsStore store = new SettingsStore(new MemoryPreferences(Map.of("target", 0)));
        assertEquals(0, store.target()); assertEquals(2, store.ceiling());
    }

    @Test public void migrationPreservesPositionRunFlagAndUnrelatedData() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("target", 1, "enabled", true, "x", 0.21f, "y", 0.72f, "other_setting", "keep"));
        SettingsStore store = new SettingsStore(preferences);
        assertTrue(store.enabled()); assertEquals(0.21f, store.x(), 0); assertEquals(0.72f, store.y(), 0);
        assertEquals("keep", preferences.getString("other_setting", ""));
    }

    @Test public void migrationPublishesOneCompleteSnapshotToObservers() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("target", 0, "last_nonzero", 1));
        int[] notifications = {0};
        preferences.registerOnSharedPreferenceChangeListener((changed, key) -> {
            notifications[0]++;
            assertEquals(1, changed.getInt("ceiling", -1)); assertEquals(0, changed.getInt("target", -1));
            assertEquals(1, changed.getInt("settings_version", -1));
            assertTrue(changed.getBoolean("youtube_enabled", false));
        });
        new SettingsStore(preferences);
        assertTrue(notifications[0] > 0); assertEquals(1, preferences.writes.size());
    }

    @Test public void migrationIsIdempotentAndDoesNotReenablePausedTarget() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore first = new SettingsStore(preferences);
        first.ceiling(7); first.target(0); first.floatingEnabled(false);
        int writesBefore = preferences.writes.size();
        SettingsStore reopened = new SettingsStore(preferences);
        assertEquals(7, reopened.ceiling()); assertEquals(0, reopened.target()); assertFalse(reopened.floatingEnabled());
        assertEquals(writesBefore, preferences.writes.size());
    }

    @Test public void restoredPreferenceSnapshotKeepsEveryNewSetting() {
        MemoryPreferences firstPreferences = new MemoryPreferences();
        SettingsStore first = new SettingsStore(firstPreferences);
        first.ceiling(99); first.tapMode(ModePolicy.TOGGLE); first.target(0); first.floatingEnabled(false);
        first.selectedApp(SettingsStore.YOUTUBE_PACKAGE, false); first.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
        first.position(0.13f, 0.89f);
        MemoryPreferences restoredPreferences = new MemoryPreferences(firstPreferences.getAll());
        SettingsStore restored = new SettingsStore(restoredPreferences);
        assertEquals(99, restored.ceiling()); assertEquals(0, restored.target()); assertEquals(ModePolicy.TOGGLE, restored.tapMode());
        assertFalse(restored.floatingEnabled()); assertFalse(restored.youtubeEnabled()); assertTrue(restored.instagramEnabled());
        assertEquals(0.13f, restored.x(), 0); assertEquals(0.89f, restored.y(), 0);
        assertEquals(0, restoredPreferences.writes.size());
    }

    @Test public void ceilingUpdatesBothCountsInOneEditorTransaction() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        int writesBefore = preferences.writes.size();
        preferences.registerOnSharedPreferenceChangeListener((changed, key) -> {
            assertEquals(99, store.ceiling()); assertEquals(99, store.target());
        });
        store.ceiling(99);
        assertEquals(writesBefore + 1, preferences.writes.size()); assertEquals(99, store.lastNonZero());
    }

    @Test public void floatingTargetChangeDoesNotShrinkConfiguredCeiling() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(5); store.target(2);
        assertEquals(5, store.ceiling()); assertEquals(2, store.target());
        store.target(99); assertEquals(5, store.target()); assertEquals(5, store.ceiling());
        store.target(0); assertEquals(0, store.target()); assertEquals(5, store.ceiling());
    }

    @Test public void switchingTapModeResetsActiveTargetToConfiguredCountAtomically() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        store.ceiling(5); store.target(2);
        int writesBefore = preferences.writes.size();
        preferences.registerOnSharedPreferenceChangeListener((changed, key) -> {
            assertEquals(ModePolicy.TOGGLE, store.tapMode()); assertEquals(5, store.target());
        });
        store.tapMode(ModePolicy.TOGGLE);
        assertEquals(5, store.ceiling()); assertEquals(writesBefore + 1, preferences.writes.size());
    }

    @Test public void startUsesCeilingRatherThanLastFloatingValue() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(5); store.target(1); store.enabled(false); store.start();
        assertEquals(5, store.target()); assertTrue(store.enabled());
    }

    @Test public void startWithZeroCeilingDoesNotSilentlyRestoreOldPositiveValue() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(99); store.ceiling(0); store.start();
        assertEquals(0, store.ceiling()); assertEquals(0, store.target()); assertTrue(store.enabled());
        assertEquals(99, store.lastNonZero());
    }

    @Test public void startPublishesTargetAndEnabledInOneUpdate() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        store.ceiling(4); store.target(0);
        int writesBefore = preferences.writes.size();
        preferences.registerOnSharedPreferenceChangeListener((changed, key) -> {
            assertTrue(store.enabled()); assertEquals(4, store.target());
        });
        store.start();
        assertEquals(writesBefore + 1, preferences.writes.size());
    }

    @Test public void hidingFloatingControlDoesNotStopAutomationOrChangeCount() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(5); store.start(); store.target(3); store.position(0.3f, 0.6f); store.floatingEnabled(false);
        assertTrue(store.enabled()); assertEquals(3, store.target()); assertEquals(5, store.ceiling());
        assertEquals(0.3f, store.x(), 0); assertEquals(0.6f, store.y(), 0); assertFalse(store.floatingEnabled());
    }

    @Test public void stoppingPreservesFloatingPreferenceAndCounts() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(7); store.start(); store.target(3); store.enabled(false);
        assertFalse(store.enabled()); assertTrue(store.floatingEnabled()); assertEquals(7, store.ceiling()); assertEquals(3, store.target());
    }

    @Test public void supportedApplicationsAreSelectedIndependently() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        assertTrue(store.isSelected(SettingsStore.YOUTUBE_PACKAGE)); assertFalse(store.isSelected(SettingsStore.INSTAGRAM_PACKAGE));
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
        assertTrue(store.youtubeEnabled()); assertTrue(store.instagramEnabled()); assertTrue(store.hasSelectedApps());
        store.selectedApp(SettingsStore.YOUTUBE_PACKAGE, false);
        assertFalse(store.youtubeEnabled()); assertTrue(store.instagramEnabled()); assertTrue(store.hasSelectedApps());
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, false);
        assertFalse(store.hasSelectedApps());
    }

    @Test public void unsupportedApplicationCannotBeStoredOrSelected() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        int writesBefore = preferences.writes.size();
        store.selectedApp("other.app", true); store.selectedApp(null, true);
        assertFalse(store.isSelected("other.app")); assertFalse(store.isSelected(null));
        assertEquals(writesBefore, preferences.writes.size());
    }

    @Test public void malformedLegacyTypesUseDefaultsWithoutCrashing() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("target", "oops", "last_nonzero", false, "floating_enabled", "oops", "youtube_enabled", "oops", "instagram_enabled", 3));
        SettingsStore store = new SettingsStore(preferences);
        assertEquals(2, store.target()); assertEquals(2, store.ceiling());
        assertTrue(store.floatingEnabled()); assertTrue(store.youtubeEnabled()); assertFalse(store.instagramEnabled());
    }

    @Test public void invalidNewValuesAreBoundedOnReadAndWrite() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("settings_version", 1, "ceiling", 5, "target", 99, "tap_mode", 300));
        SettingsStore store = new SettingsStore(preferences);
        assertEquals(5, store.target()); assertEquals(ModePolicy.ROTARY, store.tapMode());
        store.ceiling(-1); assertEquals(2, store.ceiling()); assertEquals(2, store.target());
        store.ceiling(0); store.target(99); assertEquals(0, store.target());
    }

    @Test public void laterSettingsVersionIsNotOverwrittenDuringConstruction() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("settings_version", 2, "ceiling", 7, "target", 3, "floating_enabled", false));
        SettingsStore store = new SettingsStore(preferences);
        assertEquals(7, store.ceiling()); assertEquals(3, store.target()); assertFalse(store.floatingEnabled());
        assertEquals(2, preferences.getInt("settings_version", 0)); assertEquals(0, preferences.writes.size());
    }

    @Test public void adSkippingIsOptInForFreshLegacyAndCurrentSettings() {
        SettingsStore fresh = new SettingsStore(new MemoryPreferences());
        assertFalse(fresh.skipAds());
        SettingsStore legacy = new SettingsStore(new MemoryPreferences(Map.of("target", 1)));
        assertFalse(legacy.skipAds());
        MemoryPreferences existing = new MemoryPreferences(Map.of("settings_version", 1, "ceiling", 5, "target", 2));
        SettingsStore upgraded = new SettingsStore(existing);
        assertFalse(upgraded.skipAds()); assertEquals(0, existing.writes.size());
        assertEquals(1, existing.getInt("settings_version", 0));
    }

    @Test public void adSkippingSelectionSurvivesSnapshotRestoreAndAppDeselection() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true); store.skipAds(true);
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, false);
        assertTrue(store.skipAds()); assertFalse(store.instagramEnabled());
        SettingsStore restored = new SettingsStore(new MemoryPreferences(preferences.getAll()));
        assertTrue(restored.skipAds()); assertFalse(restored.instagramEnabled());
        restored.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
        assertTrue(restored.skipAds()); restored.skipAds(false); assertFalse(restored.skipAds());
    }

    @Test public void adSkippingChangeDoesNotEnableExecutionOrChangeZeroTarget() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        store.ceiling(5); store.target(0);
        int writesBefore = preferences.writes.size();
        store.skipAds(true);
        assertTrue(store.skipAds()); assertFalse(store.enabled());
        assertEquals(5, store.ceiling()); assertEquals(0, store.target());
        assertEquals(writesBefore + 1, preferences.writes.size());
    }

    @Test public void malformedAdSkippingPreferenceFailsClosed() {
        SettingsStore store = new SettingsStore(new MemoryPreferences(Map.of("settings_version", 1, "skip_ads", "true")));
        assertFalse(store.skipAds());
    }

    @Test public void migrationPreservesExplicitAdSkippingChoiceWithoutSchemaBump() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("target", 0, "last_nonzero", 1, "skip_ads", true));
        SettingsStore store = new SettingsStore(preferences);
        assertTrue(store.skipAds()); assertEquals(0, store.target()); assertEquals(1, store.ceiling());
        assertEquals(1, preferences.getInt("settings_version", 0));
    }

    @Test public void visualAssistanceDefaultsOffForNewLegacyAndExistingSettings() {
        assertFalse(new SettingsStore(new MemoryPreferences()).visualAssist());
        assertFalse(new SettingsStore(new MemoryPreferences(Map.of("target", 1))).visualAssist());
        MemoryPreferences existing = new MemoryPreferences(Map.of("settings_version", 1, "ceiling", 5, "target", 2));
        SettingsStore store = new SettingsStore(existing);
        assertFalse(store.visualAssist()); assertEquals(0, existing.writes.size());
        assertEquals(1, existing.getInt("settings_version", 0));
    }

    @Test public void visualAssistanceRestoresWithoutChangingOtherSettings() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        store.ceiling(5); store.target(2); store.skipAds(true); store.floatingEnabled(false);
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true); store.visualAssist(true);
        SettingsStore restored = new SettingsStore(new MemoryPreferences(preferences.getAll()));
        assertTrue(restored.visualAssist()); assertTrue(restored.skipAds()); assertFalse(restored.floatingEnabled());
        assertTrue(restored.instagramEnabled()); assertFalse(restored.enabled());
        assertEquals(5, restored.ceiling()); assertEquals(2, restored.target());
        restored.visualAssist(false); assertFalse(restored.visualAssist()); assertTrue(restored.skipAds());
    }

    @Test public void malformedVisualAssistancePreferenceDefaultsOff() {
        for (Object invalid : new Object[] {"true", 1, 1L, 1.0f}) {
            SettingsStore store = new SettingsStore(new MemoryPreferences(Map.of("settings_version", 1, "visual_assist", invalid)));
            assertFalse(store.visualAssist());
        }
    }

    @Test public void migrationPreservesExplicitVisualAssistanceWithoutSchemaChange() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("target", 0, "last_nonzero", 1, "visual_assist", true));
        SettingsStore store = new SettingsStore(preferences);
        assertTrue(store.visualAssist()); assertEquals(0, store.target()); assertEquals(1, store.ceiling());
        assertEquals(1, preferences.getInt("settings_version", 0));
    }

    @Test public void visualAssistanceDoesNotStartExecutionAndSurvivesInstagramDeselection() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.ceiling(0); store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true); store.visualAssist(true);
        assertFalse(store.enabled()); assertEquals(0, store.target());
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, false);
        assertTrue(store.visualAssist()); assertFalse(store.instagramEnabled());
        assertFalse(store.enabled()); assertEquals(0, store.ceiling()); assertEquals(0, store.target());
    }

    @Test public void timeoutDefaultsOffAndTenSecondsForEveryUpgradePath() {
        SettingsStore fresh = new SettingsStore(new MemoryPreferences());
        assertFalse(fresh.timedFallback()); assertEquals(10, fresh.fallbackSeconds());
        SettingsStore legacy = new SettingsStore(new MemoryPreferences(Map.of("target", 1)));
        assertFalse(legacy.timedFallback()); assertEquals(10, legacy.fallbackSeconds());
        MemoryPreferences current = new MemoryPreferences(Map.of("settings_version", 1, "ceiling", 5, "target", 2));
        SettingsStore existing = new SettingsStore(current);
        assertFalse(existing.timedFallback()); assertEquals(10, existing.fallbackSeconds());
        assertEquals(0, current.writes.size()); assertEquals(1, current.getInt("settings_version", 0));
    }

    @Test public void timeoutRestoresChoiceAndSecondsWithoutErasingVisualAssistance() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
        store.visualAssist(true); store.timedFallback(true); store.fallbackSeconds(60);
        SettingsStore restored = new SettingsStore(new MemoryPreferences(preferences.getAll()));
        assertTrue(restored.timedFallback()); assertEquals(60, restored.fallbackSeconds());
        assertTrue(restored.visualAssist()); assertTrue(restored.instagramEnabled());
        restored.timedFallback(false);
        assertFalse(restored.timedFallback()); assertEquals(60, restored.fallbackSeconds()); assertTrue(restored.visualAssist());
    }

    @Test public void malformedTimeoutPreferencesUseSafeDefaults() {
        for (Object invalid : new Object[] {"15", true, 15L, 15.0f}) {
            SettingsStore store = new SettingsStore(new MemoryPreferences(Map.of("settings_version", 1, "timed_fallback", "true", "fallback_seconds", invalid)));
            assertFalse(store.timedFallback()); assertEquals(10, store.fallbackSeconds());
        }
    }

    @Test public void timeoutSecondsPreserveEveryValidSecondAndRejectOutOfRangeValues() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        for (int seconds = 5; seconds <= 60; seconds++) {
            store.fallbackSeconds(seconds); assertEquals(seconds, store.fallbackSeconds());
        }
        for (int invalid : new int[] {Integer.MIN_VALUE, -1, 0, 4, 61, 120, Integer.MAX_VALUE}) {
            store.fallbackSeconds(invalid); assertEquals(10, store.fallbackSeconds());
            SettingsStore restored = new SettingsStore(new MemoryPreferences(Map.of("settings_version", 1, "fallback_seconds", invalid)));
            assertEquals(10, restored.fallbackSeconds());
        }
    }

    @Test public void timeoutChangesDoNotStartExecutionOrChangeRepeatAndAdSettings() {
        MemoryPreferences preferences = new MemoryPreferences();
        SettingsStore store = new SettingsStore(preferences);
        store.ceiling(5); store.target(0); store.skipAds(true); store.visualAssist(true); store.floatingEnabled(false);
        int writesBefore = preferences.writes.size();
        store.fallbackSeconds(16); store.timedFallback(true);
        assertEquals(writesBefore + 2, preferences.writes.size()); assertTrue(store.timedFallback());
        assertEquals(16, store.fallbackSeconds()); assertFalse(store.enabled());
        assertEquals(5, store.ceiling()); assertEquals(0, store.target());
        assertTrue(store.skipAds()); assertTrue(store.visualAssist()); assertFalse(store.floatingEnabled());
    }

    @Test public void timeoutChoiceAndSecondsSurviveInstagramDeselection() {
        SettingsStore store = new SettingsStore(new MemoryPreferences());
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true); store.timedFallback(true); store.fallbackSeconds(5);
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, false);
        assertTrue(store.timedFallback()); assertEquals(5, store.fallbackSeconds()); assertFalse(store.instagramEnabled());
        store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
        assertTrue(store.timedFallback()); assertEquals(5, store.fallbackSeconds());
    }

    @Test public void migrationPreservesExistingTimeoutWithoutChangingSchemaOrZeroTarget() {
        MemoryPreferences preferences = new MemoryPreferences(Map.of("target", 0, "last_nonzero", 1, "timed_fallback", true, "fallback_seconds", 25));
        SettingsStore store = new SettingsStore(preferences);
        assertTrue(store.timedFallback()); assertEquals(25, store.fallbackSeconds());
        assertEquals(1, store.ceiling()); assertEquals(0, store.target());
        assertEquals(1, preferences.getInt("settings_version", 0));
    }
}
