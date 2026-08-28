package com.fullmetalsonic.shortsloop.data;

import com.fullmetalsonic.shortsloop.core.AdSkipPolicy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class YouTubeAdSettingsStoreTest {
    private static final String YT = SettingsStore.YOUTUBE_PACKAGE, IG = SettingsStore.INSTAGRAM_PACKAGE, TT = SettingsStore.TIKTOK_PACKAGE;
    private static final String P = "host.youtube.";
    private static Map<String,Object> schema() {
        Map<String,Object> values = new HashMap<>();
        values.put("settings_version", 1); values.put("host_settings_version", 2);
        values.put("skip_ads", true); values.put("ad_delay_tenths", 37);
        values.put("host.tiktok.skip_ads", true); values.put("host.tiktok.ad_delay_tenths", 58);
        return values;
    }
    @Test public void existingInstallGetsLazyOffAndZeroWithoutChangingAnyValueOrSchema() {
        Map<String,Object> values = schema(); MemoryPreferences prefs = new MemoryPreferences(values);
        SettingsStore yt = new SettingsStore(prefs).forHost(YT);
        assertFalse(yt.skipAds()); assertEquals(0, yt.adDelayTenths());
        assertEquals(values, prefs.getAll()); assertTrue(prefs.writes.isEmpty());
        assertEquals(2, prefs.getInt("host_settings_version", 0));
    }
    @Test public void allDelaysPersistExactlyWithoutChangingInstagramOrTikTok() {
        MemoryPreferences prefs = new MemoryPreferences(schema()); SettingsStore root = new SettingsStore(prefs), yt = root.forHost(YT);
        for (int value = 0; value <= 99; value++) {
            yt.adDelayTenths(value);
            SettingsStore restored = new SettingsStore(new MemoryPreferences(prefs.getAll()));
            assertEquals(value, restored.forHost(YT).adDelayTenths());
            assertEquals(37, restored.adDelayTenths()); assertEquals(37, restored.forHost(IG).adDelayTenths());
            assertEquals(58, restored.forHost(TT).adDelayTenths());
            assertEquals(Integer.valueOf(value), prefs.getAll().get(P + "ad_delay_tenths"));
        }
    }
    @Test public void olderHostSchemaNeverCopiesLegacyAdsIntoYouTube() {
        for (int version : new int[]{0, 1}) {
            Map<String,Object> values = schema(); values.put("host_settings_version", version);
            MemoryPreferences prefs = new MemoryPreferences(values); SettingsStore root = new SettingsStore(prefs), yt = root.forHost(YT);
            assertFalse(yt.skipAds()); assertEquals(0, yt.adDelayTenths());
            assertTrue(root.skipAds()); assertEquals(37, root.adDelayTenths());
            assertTrue(root.forHost(TT).skipAds()); assertEquals(58, root.forHost(TT).adDelayTenths());
            assertFalse(prefs.contains(P + "skip_ads")); assertFalse(prefs.contains(P + "ad_delay_tenths"));
        }
    }
    @Test public void previouslyStoredYouTubeChoicesRestoreWithoutMigrationWrites() {
        Map<String,Object> values = schema(); values.put(P + "skip_ads", true); values.put(P + "ad_delay_tenths", 99);
        MemoryPreferences prefs = new MemoryPreferences(values); SettingsStore yt = new SettingsStore(prefs).forHost(YT);
        assertTrue(yt.skipAds()); assertEquals(99, yt.adDelayTenths());
        assertEquals(values, prefs.getAll()); assertTrue(prefs.writes.isEmpty());
    }
    @Test public void toggleAndWritesUseOnlyYouTubeKeys() {
        MemoryPreferences prefs = new MemoryPreferences(schema()); SettingsStore root = new SettingsStore(prefs), yt = root.forHost(YT);
        yt.skipAds(true); yt.adDelayTenths(13); yt.skipAds(false);
        assertFalse(yt.skipAds()); assertEquals(13, yt.adDelayTenths());
        assertTrue(root.skipAds()); assertTrue(root.forHost(IG).skipAds()); assertTrue(root.forHost(TT).skipAds());
        assertEquals(37, root.adDelayTenths()); assertEquals(58, root.forHost(TT).adDelayTenths());
        for (Map<String,?> write : prefs.writes) {
            Map<String,Object> unrelated = new HashMap<>(write);
            unrelated.remove(P + "skip_ads"); unrelated.remove(P + "ad_delay_tenths");
            assertEquals(schema(), unrelated);
        }
        assertFalse(prefs.contains("host.instagram.skip_ads"));
    }
    @Test public void wrongTypesAndOutOfRangeValuesFailSafeWithoutRepairingStorage() {
        for (Object invalid : new Object[]{"13", 13L, 1.3f, true, -1, 100, Integer.MAX_VALUE}) {
            Map<String,Object> values = schema(); values.put(P + "ad_delay_tenths", invalid); values.put(P + "skip_ads", "true");
            MemoryPreferences prefs = new MemoryPreferences(values); SettingsStore yt = new SettingsStore(prefs).forHost(YT);
            assertFalse(yt.skipAds()); assertEquals(0, yt.adDelayTenths());
            assertEquals(values, prefs.getAll()); assertTrue(prefs.writes.isEmpty());
        }
    }
    @Test public void invalidSetterUsesSafeDefaultAndOffPreservesDelay() {
        SettingsStore yt = new SettingsStore(new MemoryPreferences(schema())).forHost(YT);
        yt.adDelayTenths(99); yt.skipAds(true); yt.skipAds(false); assertEquals(99, yt.adDelayTenths());
        yt.adDelayTenths(100); assertEquals(0, yt.adDelayTenths());
        yt.adDelayTenths(-1); assertEquals(0, yt.adDelayTenths());
    }
    @Test public void adsRemainIndependentOfCountZeroButRespectExecutionSelectionAndPause() {
        SettingsStore root = new SettingsStore(new MemoryPreferences(schema())), yt = root.forHost(YT);
        yt.ceiling(0); yt.skipAds(true); root.selectedApp(YT, true); root.start();
        assertEquals(0, yt.target()); assertTrue(AdSkipPolicy.enabled(yt.enabled(), yt.skipAds(), yt.isSelected(YT)));
        yt.hostPaused(true); assertFalse(AdSkipPolicy.enabled(yt.enabled(), yt.skipAds(), yt.isSelected(YT)));
        yt.hostPaused(false); root.selectedApp(YT, false); assertFalse(AdSkipPolicy.enabled(yt.enabled(), yt.skipAds(), yt.isSelected(YT)));
        root.selectedApp(YT, true); root.enabled(false); assertFalse(AdSkipPolicy.enabled(yt.enabled(), yt.skipAds(), yt.isSelected(YT)));
        assertTrue(yt.skipAds()); assertEquals(0, yt.target());
    }
    @Test public void adNotificationsInvalidateOnlyTheirOwningHost() {
        for (String suffix : new String[]{"skip_ads", "ad_delay_tenths"}) {
            assertTrue(SettingsStore.affectsHost(P + suffix, YT));
            assertFalse(SettingsStore.affectsHost(P + suffix, IG)); assertFalse(SettingsStore.affectsHost(P + suffix, TT));
            assertFalse(SettingsStore.affectsHost(suffix, YT)); assertTrue(SettingsStore.affectsHost(suffix, IG));
            assertFalse(SettingsStore.affectsHost("host.tiktok." + suffix, YT));
        }
    }
    @Test public void youtubeAdsDoNotEnableUnsupportedPhotoTimerOrVisualFeatures() {
        SettingsStore root = new SettingsStore(new MemoryPreferences(schema())), yt = root.forHost(YT);
        root.photoEnabled(true); root.timedFallback(true); root.visualAssist(true); yt.skipAds(true);
        assertTrue(yt.skipAds()); assertFalse(yt.photoEnabled()); assertFalse(yt.timedFallback()); assertFalse(yt.visualAssist());
        assertFalse(yt.skipLive()); assertFalse(yt.skipLong());
    }
}
