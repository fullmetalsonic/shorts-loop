package com.fullmetalsonic.shortsloop.data;

import com.fullmetalsonic.shortsloop.core.AdDelayPolicy;
import com.fullmetalsonic.shortsloop.core.AdSkipPolicy;
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutPolicy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class TikTokSettingsStoreTest {
    private static final String TT = SettingsStore.TIKTOK_PACKAGE;
    private static final String IG = SettingsStore.INSTAGRAM_PACKAGE;
    private static final String YT = SettingsStore.YOUTUBE_PACKAGE;
    private static final String P = "host.tiktok.";

    @Test public void defaultsDoNotInheritInstagramAndDoNotWrite() {
        Map<String, Object> values = schema();
        values.put("skip_ads", true); values.put("photo_enabled", true);
        values.put("timed_fallback", true); values.put("skip_long", true);
        values.put("ad_delay_tenths", 99); values.put("fallback_seconds", 60);
        MemoryPreferences preferences = new MemoryPreferences(values);
        SettingsStore tt = new SettingsStore(preferences).forHost(TT);
        assertFalse(tt.skipAds()); assertFalse(tt.photoEnabled());
        assertFalse(tt.timedFallback()); assertFalse(tt.skipLong()); assertFalse(tt.photoFallback());
        assertEquals(0, tt.adDelayTenths()); assertEquals(0, tt.photoMode());
        assertEquals(3, tt.photoWholeSeconds()); assertEquals(3, tt.photoSlideSeconds());
        assertEquals(3, tt.fallbackSeconds()); assertEquals(60, tt.longVideoSeconds());
        assertEquals(values, preferences.getAll()); assertTrue(preferences.writes.isEmpty());
    }

    @Test public void allOneHundredAdDelayValuesStaySeparateIntegers() {
        MemoryPreferences preferences = new MemoryPreferences(schema());
        SettingsStore root = new SettingsStore(preferences);
        SettingsStore tt = root.forHost(TT), ig = root.forHost(IG);
        for (int value = 0; value <= 99; value++) {
            tt.adDelayTenths(value); ig.adDelayTenths(99 - value);
            SettingsStore restored = new SettingsStore(new MemoryPreferences(preferences.getAll()));
            assertEquals(value, restored.forHost(TT).adDelayTenths());
            assertEquals(99 - value, restored.forHost(IG).adDelayTenths());
            assertEquals(Integer.valueOf(value), preferences.getAll().get(P + "ad_delay_tenths"));
            assertEquals(Integer.valueOf(value), AdDelayPolicy.parseTenths(AdDelayPolicy.format(value)));
        }
    }

    @Test public void photoAndTimerChoicesStayIndependent() {
        SettingsStore root = new SettingsStore(new MemoryPreferences(schema()));
        SettingsStore ig = root.forHost(IG), tt = root.forHost(TT);
        ig.photoEnabled(true); ig.photoMode(0); ig.photoWholeSeconds(7); ig.photoSlideSeconds(8);
        ig.photoFallback(false); ig.timedFallback(true); ig.fallbackSeconds(17);
        tt.photoEnabled(true); tt.photoMode(1); tt.photoFallback(true); tt.timedFallback(true);
        for (int seconds = 0; seconds <= 10; seconds++) {
            tt.photoWholeSeconds(seconds); tt.photoSlideSeconds(10 - seconds);
            assertEquals(seconds, tt.photoWholeSeconds()); assertEquals(10 - seconds, tt.photoSlideSeconds());
            assertEquals(7, ig.photoWholeSeconds()); assertEquals(8, ig.photoSlideSeconds());
        }
        for (int seconds = 2; seconds <= 60; seconds++) {
            tt.fallbackSeconds(seconds);
            assertEquals(seconds, root.forHost(TT).fallbackSeconds()); assertEquals(17, ig.fallbackSeconds());
        }
        assertEquals(1, tt.photoMode()); assertEquals(0, ig.photoMode());
        assertTrue(tt.photoFallback()); assertFalse(ig.photoFallback());
        tt.photoEnabled(false); tt.timedFallback(false);
        assertTrue(ig.photoEnabled()); assertTrue(ig.timedFallback());
        assertEquals(1, tt.photoMode()); assertTrue(tt.photoFallback()); assertEquals(60, tt.fallbackSeconds());
    }

    @Test public void instagramAndRootKeepTheirExistingKeys() {
        MemoryPreferences preferences = new MemoryPreferences(schema());
        SettingsStore root = new SettingsStore(preferences), ig = root.forHost(IG), tt = root.forHost(TT);
        tt.skipAds(true); tt.adDelayTenths(19); tt.photoWholeSeconds(0); tt.fallbackSeconds(2);
        ig.skipAds(false); ig.adDelayTenths(7); ig.photoWholeSeconds(8); ig.fallbackSeconds(17);
        assertFalse(root.skipAds()); assertEquals(7, root.adDelayTenths());
        assertEquals(8, root.photoWholeSeconds()); assertEquals(17, root.fallbackSeconds());
        assertTrue(tt.skipAds()); assertEquals(19, tt.adDelayTenths());
        assertEquals(0, tt.photoWholeSeconds()); assertEquals(2, tt.fallbackSeconds());
        assertFalse(preferences.contains("host.instagram.skip_ads"));
        assertFalse(preferences.contains("host.instagram.fallback_seconds"));
        root.adDelayTenths(8); assertEquals(8, ig.adDelayTenths()); assertEquals(19, tt.adDelayTenths());
    }

    @Test public void wrongTypesUseSafeDefaultsWithoutRepairWrites() {
        Map<String, Object> values = schema();
        values.put(P + "skip_ads", "true"); values.put(P + "photo_enabled", 1);
        values.put(P + "photo_fallback", 1L); values.put(P + "timed_fallback", "true");
        values.put(P + "skip_long", .5f); values.put(P + "ad_delay_tenths", "4");
        values.put(P + "photo_mode", false); values.put(P + "photo_whole_seconds", 0L);
        values.put(P + "photo_slide_seconds", "10"); values.put(P + "fallback_seconds", 2f);
        values.put(P + "long_video_seconds", "60");
        MemoryPreferences preferences = new MemoryPreferences(values);
        SettingsStore tt = new SettingsStore(preferences).forHost(TT);
        assertFalse(tt.skipAds()); assertFalse(tt.photoEnabled()); assertFalse(tt.photoFallback());
        assertFalse(tt.timedFallback()); assertFalse(tt.skipLong());
        assertEquals(0, tt.adDelayTenths()); assertEquals(0, tt.photoMode());
        assertEquals(3, tt.photoWholeSeconds()); assertEquals(3, tt.photoSlideSeconds());
        assertEquals(3, tt.fallbackSeconds()); assertEquals(60, tt.longVideoSeconds());
        assertEquals(values, preferences.getAll()); assertTrue(preferences.writes.isEmpty());
    }

    @Test public void featureChangesPreserveExistingTypedSettings() {
        Map<String, Object> values = schema();
        values.put("skip_ads", true); values.put("photo_whole_seconds", 8);
        values.put("fallback_seconds", 17); values.put("ad_delay_tenths", 12);
        values.put(P + "ceiling", 9); values.put(P + "target", 0);
        values.put(P + "paused", true); values.put(P + "x", .17f); values.put(P + "y", .83f);
        values.put("host.youtube.ceiling", 7); values.put("host.instagram.target", 1);
        values.put("tiktok_enabled", true); values.put("synthetic_string", "keep");
        values.put("synthetic_long", 123456789L);
        MemoryPreferences preferences = new MemoryPreferences(values);
        SettingsStore tt = new SettingsStore(preferences).forHost(TT);
        tt.skipAds(true); tt.adDelayTenths(99); tt.photoEnabled(true); tt.photoMode(1);
        tt.photoWholeSeconds(0); tt.photoSlideSeconds(10); tt.photoFallback(true);
        tt.timedFallback(true); tt.fallbackSeconds(2); tt.skipLong(true); tt.longVideoSeconds(3600);
        for (Map.Entry<String, Object> entry : values.entrySet())
            assertEquals(entry.getKey(), entry.getValue(), preferences.getAll().get(entry.getKey()));
        SettingsStore restored = new SettingsStore(new MemoryPreferences(preferences.getAll())).forHost(TT);
        assertTrue(restored.skipAds()); assertTrue(restored.photoEnabled()); assertTrue(restored.timedFallback());
        assertTrue(restored.skipLong()); assertEquals(3600, restored.longVideoSeconds());
        assertEquals(0, restored.target()); assertEquals(9, restored.ceiling()); assertTrue(restored.hostPaused());
        assertEquals(.17f, restored.x(), 0f); assertEquals(.83f, restored.y(), 0f);
    }

    @Test public void invalidNumbersReadDefaultsWithoutRewritingStoredValues() {
        for (int invalid : new int[]{Integer.MIN_VALUE, -1, 3601, Integer.MAX_VALUE}) {
            Map<String, Object> values = schema();
            for (String field : new String[]{"ad_delay_tenths", "photo_mode", "photo_whole_seconds",
                    "photo_slide_seconds", "fallback_seconds", "long_video_seconds"})
                values.put(P + field, invalid);
            MemoryPreferences preferences = new MemoryPreferences(values);
            SettingsStore tt = new SettingsStore(preferences).forHost(TT);
            assertEquals(0, tt.adDelayTenths()); assertEquals(0, tt.photoMode());
            assertEquals(3, tt.photoWholeSeconds()); assertEquals(3, tt.photoSlideSeconds());
            assertEquals(3, tt.fallbackSeconds()); assertEquals(60, tt.longVideoSeconds());
            assertEquals(values, preferences.getAll()); assertTrue(preferences.writes.isEmpty());
        }
        SettingsStore tt = new SettingsStore(new MemoryPreferences(schema())).forHost(TT);
        tt.adDelayTenths(100); tt.photoMode(2); tt.photoWholeSeconds(11); tt.photoSlideSeconds(-1);
        tt.fallbackSeconds(1); tt.longVideoSeconds(0);
        assertEquals(0, tt.adDelayTenths()); assertEquals(0, tt.photoMode());
        assertEquals(3, tt.photoWholeSeconds()); assertEquals(3, tt.photoSlideSeconds());
        assertEquals(3, tt.fallbackSeconds()); assertEquals(60, tt.longVideoSeconds());
        tt.longVideoSeconds(1); assertEquals(1, tt.longVideoSeconds());
        tt.longVideoSeconds(3600); assertEquals(3600, tt.longVideoSeconds());
    }

    @Test public void changeNotificationsOnlyAffectTheirHost() {
        String[] fields = {"skip_ads", "ad_delay_tenths", "photo_enabled", "photo_mode",
                "photo_whole_seconds", "photo_slide_seconds", "photo_fallback", "timed_fallback", "fallback_seconds"};
        for (String field : fields) {
            assertTrue(SettingsStore.affectsHost(P + field, TT));
            assertFalse(SettingsStore.affectsHost(P + field, IG));
            assertFalse(SettingsStore.affectsHost(P + field, YT));
            assertTrue(SettingsStore.affectsHost(field, IG));
            assertFalse(SettingsStore.affectsHost(field, TT));
            assertFalse(SettingsStore.affectsHost(field, YT));
        }
        String[] hosts = {YT, IG, TT};
        String[] prefixes = {"host.youtube.", "host.instagram.", P};
        for (int i = 0; i < hosts.length; i++) {
            for (int j = 0; j < hosts.length; j++) {
                assertEquals(i == j, SettingsStore.affectsHost(prefixes[i] + "skip_long", hosts[j]));
                assertEquals(i == j, SettingsStore.affectsHost(prefixes[i] + "long_video_seconds", hosts[j]));
                assertFalse(SettingsStore.affectsHost(prefixes[i] + "x", hosts[j]));
                assertFalse(SettingsStore.affectsHost(prefixes[i] + "y", hosts[j]));
            }
            assertTrue(SettingsStore.affectsHost("enabled", hosts[i]));
            assertTrue(SettingsStore.affectsHost("dual_mode", hosts[i]));
            assertFalse(SettingsStore.affectsHost("floating_enabled", hosts[i]));
        }
        assertFalse(SettingsStore.affectsHost(null, TT));
        assertFalse(SettingsStore.affectsHost(P + "skip_ads", "unsupported"));
    }

    @Test public void adsRemainIndependentOfZeroButTimerRequiresPositiveCount() {
        for (String host : new String[]{IG, TT}) {
            SettingsStore root = new SettingsStore(new MemoryPreferences(schema()));
            root.selectedApp(host, true); root.enabled(true);
            SettingsStore store = root.forHost(host);
            store.skipAds(true); store.timedFallback(true); store.target(0);
            assertTrue(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.isSelected(host)));
            assertFalse(ClocklessTimeoutPolicy.enabled(store.enabled(), store.target(), store.isSelected(host), store.timedFallback()));
            store.target(1);
            assertTrue(ClocklessTimeoutPolicy.enabled(store.enabled(), store.target(), store.isSelected(host), store.timedFallback()));
            root.selectedApp(host, false);
            assertFalse(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.isSelected(host)));
            assertFalse(ClocklessTimeoutPolicy.enabled(store.enabled(), store.target(), store.isSelected(host), store.timedFallback()));
            root.selectedApp(host, true); root.enabled(false);
            assertFalse(AdSkipPolicy.enabled(store.enabled(), store.skipAds(), store.isSelected(host)));
            assertFalse(ClocklessTimeoutPolicy.enabled(store.enabled(), store.target(), store.isSelected(host), store.timedFallback()));
            assertTrue(store.skipAds()); assertTrue(store.timedFallback());
        }
    }

    @Test public void liveAndVisualCapabilitiesRemainExclusive() {
        SettingsStore root = new SettingsStore(new MemoryPreferences(schema()));
        root.skipLive(true); root.visualAssist(true);
        SettingsStore yt = root.forHost(YT), ig = root.forHost(IG), tt = root.forHost(TT);
        tt.skipAds(true); tt.photoEnabled(true); tt.timedFallback(true); tt.skipLong(true);
        assertTrue(yt.skipLive()); assertFalse(yt.visualAssist());
        assertFalse(ig.skipLive()); assertTrue(ig.visualAssist());
        assertFalse(tt.skipLive()); assertFalse(tt.visualAssist());
        assertFalse(yt.skipAds()); assertFalse(yt.photoEnabled()); assertFalse(yt.timedFallback());
        assertFalse(ig.skipAds()); assertFalse(ig.photoEnabled()); assertFalse(ig.timedFallback());
        assertFalse(yt.skipLong()); assertFalse(ig.skipLong()); assertTrue(tt.skipLong());
    }

    private static Map<String, Object> schema() {
        Map<String, Object> values = new HashMap<>();
        values.put("settings_version", 1); values.put("host_settings_version", 2);
        return values;
    }
}
