package com.fullmetalsonic.shortsloop.data;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class V2SettingsStoreTest {
    private static final String TT=SettingsStore.TIKTOK_PACKAGE,YT=SettingsStore.YOUTUBE_PACKAGE,IG=SettingsStore.INSTAGRAM_PACKAGE;
    @Test public void upgradePreservesEveryExistingValueExceptSchemaCounterAndDoesNotInheritSpecialFlags() {
        Map<String,Object> values=new HashMap<>();
        values.put("settings_version",1);values.put("host_settings_version",1);values.put("dual_mode",true);
        values.put("host.youtube.ceiling",9);values.put("host.youtube.target",0);values.put("host.youtube.paused",true);
        values.put("host.instagram.ceiling",4);values.put("host.instagram.target",2);values.put("fallback_seconds",17);
        values.put("skip_ads",true);values.put("skip_live",true);values.put("skip_long",true);values.put("photo_enabled",true);
        values.put("timed_fallback",true);values.put("visual_assist",true);values.put("host.instagram.x",.17f);
        MemoryPreferences p=new MemoryPreferences(values);SettingsStore root=new SettingsStore(p),tt=root.forHost(TT);
        assertEquals(2,p.getInt("host_settings_version",0));
        for(var e:values.entrySet())if(!e.getKey().equals("host_settings_version"))assertEquals(e.getKey(),e.getValue(),p.getAll().get(e.getKey()));
        assertEquals(2,tt.target());assertEquals(2,tt.ceiling());assertFalse(root.tiktokEnabled());assertFalse(tt.hostPaused());
        assertFalse(tt.skipAds());assertFalse(tt.skipLive());assertFalse(tt.skipLong());assertFalse(tt.photoEnabled());assertFalse(tt.timedFallback());assertFalse(tt.visualAssist());
        assertEquals(17,root.fallbackSeconds());assertEquals(0,root.adDelayTenths());
        int writes=p.writes.size();new SettingsStore(p).forHost(TT);new SettingsStore(p).forHost(YT);assertEquals(writes,p.writes.size());
    }
    @Test public void previousValidTimerValuesRemainUnchangedAndUnsetBecomesThree() {
        for(int seconds=5;seconds<=60;seconds++){
            MemoryPreferences p=new MemoryPreferences(Map.of("settings_version",1,"host_settings_version",1,"fallback_seconds",seconds,"timed_fallback",true));
            SettingsStore root=new SettingsStore(p);root.forHost(TT);assertEquals(seconds,root.fallbackSeconds());assertTrue(root.timedFallback());
        }
        SettingsStore fresh=new SettingsStore(new MemoryPreferences());fresh.forHost(TT);assertEquals(3,fresh.fallbackSeconds());assertFalse(fresh.timedFallback());
    }
    @Test public void tiktokSelectionAndCountsAreIndependentAndMasterStartUsesEverySelectedHost() {
        SettingsStore root=new SettingsStore(new MemoryPreferences());SettingsStore yt=root.forHost(YT),ig=root.forHost(IG),tt=root.forHost(TT);
        yt.ceiling(5);ig.ceiling(7);tt.ceiling(9);root.selectedApp(TT,true);root.selectedApp(IG,true);root.start();
        assertEquals(9,tt.target());assertTrue(tt.enabled());tt.target(0);assertEquals(5,yt.target());assertEquals(7,ig.target());
        tt.enabled(false);assertTrue(ig.enabled());root.start();assertEquals(9,tt.target());assertTrue(tt.enabled());
        root.enabled(false);assertFalse(tt.enabled());assertFalse(yt.enabled());assertFalse(ig.enabled());
    }
    @Test public void adDelayRestoresAllStepsAndOnlyInvalidatesInstagram() {
        SettingsStore root=new SettingsStore(new MemoryPreferences());root.forHost(IG);
        for(int i=0;i<=99;i++){root.adDelayTenths(i);assertEquals(i,new SettingsStore(new MemoryPreferences(root.preferences.getAll())).adDelayTenths());}
        assertTrue(SettingsStore.affectsHost("ad_delay_tenths",IG));assertFalse(SettingsStore.affectsHost("ad_delay_tenths",YT));assertFalse(SettingsStore.affectsHost("ad_delay_tenths",TT));
        assertTrue(SettingsStore.affectsHost("tiktok_enabled",TT));assertFalse(SettingsStore.affectsHost("tiktok_enabled",IG));
        assertTrue(SettingsStore.affectsHost("host.tiktok.target",TT));assertFalse(SettingsStore.affectsHost("host.tiktok.target",YT));
    }
}
