package com.fullmetalsonic.shortsloop.data;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PhotoSettingsStoreTest {
    @Test public void absentKeysAreOffAndDoNotWrite() {var p=new MemoryPreferences(Map.of("settings_version",1,"target",0));var before=p.getAll();var s=new SettingsStore(p);assertFalse(s.photoEnabled());assertFalse(s.photoFallback());assertEquals(0,s.photoMode());assertEquals(3,s.photoWholeSeconds());assertEquals(3,s.photoSlideSeconds());assertEquals(before,p.getAll());assertTrue(p.writes.isEmpty());}
    @Test public void modesPreserveBothTimesAndOtherFeatures() {
        var p=new MemoryPreferences(Map.of("settings_version",1,"target",0,"ceiling",2,"enabled",false,"fallback_seconds",25,"skip_ads",true));var before=p.getAll();var s=new SettingsStore(p);
        s.photoEnabled(true);s.photoWholeSeconds(0);s.photoSlideSeconds(10);s.photoMode(1);s.photoFallback(true);s.photoMode(0);s.photoMode(1);
        var restored=new SettingsStore(p);assertEquals(0,restored.photoWholeSeconds());assertEquals(10,restored.photoSlideSeconds());assertTrue(restored.photoFallback());assertEquals(0,restored.target());assertFalse(restored.enabled());
        var others=new HashMap<String,Object>(p.getAll());others.keySet().removeIf(k->k.startsWith("photo_"));assertEquals(before,others);
    }
    @Test public void disablingDoesNotDeletePreferences() {var s=new SettingsStore(new MemoryPreferences());s.photoWholeSeconds(10);s.photoSlideSeconds(0);s.photoFallback(true);s.photoEnabled(true);s.photoEnabled(false);assertEquals(10,s.photoWholeSeconds());assertEquals(0,s.photoSlideSeconds());assertTrue(s.photoFallback());}
    @Test public void wrongTypesFailSafelyWithoutRepairWrites() {var p=new MemoryPreferences(Map.of("settings_version",1,"photo_enabled","true","photo_mode",true,"photo_whole_seconds","0","photo_slide_seconds",-1,"photo_fallback",1));var before=p.getAll();var s=new SettingsStore(p);assertFalse(s.photoEnabled());assertFalse(s.photoFallback());assertEquals(0,s.photoMode());assertEquals(3,s.photoWholeSeconds());assertEquals(3,s.photoSlideSeconds());assertEquals(before,p.getAll());}
}
