package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class FeatureSupportPolicyTest {
    @Test public void subtitleBoundary() { assertFalse(FeatureSupportPolicy.tileSubtitle(28)); assertTrue(FeatureSupportPolicy.tileSubtitle(29)); }
    @Test public void tileAddBoundary() { assertFalse(FeatureSupportPolicy.tileAddRequest(32)); assertTrue(FeatureSupportPolicy.tileAddRequest(33)); }
    @Test public void visualBoundary() { assertFalse(FeatureSupportPolicy.visualCapture(33)); assertTrue(FeatureSupportPolicy.visualCapture(34)); }
    @Test public void allSupportedOsRetainInstagramBasics() {
        for (int sdk : new int[]{26,27,28,29,30,31,32,33,34,35,36,37}) {
            assertTrue(FeatureSupportPolicy.instagramFeature(true, true));
            assertEquals(sdk >= 34, FeatureSupportPolicy.visualCapture(sdk));
        }
    }
    @Test public void missingOrDeselectedAppDisablesOnlyItsFeatures() {
        assertFalse(FeatureSupportPolicy.instagramFeature(false, true));
        assertFalse(FeatureSupportPolicy.instagramFeature(true, false));
        assertFalse(FeatureSupportPolicy.instagramFeature(false, false));
    }
    @Test public void oldOsExplainsOsFirst() { assertEquals(FeatureSupportPolicy.Availability.ANDROID_TOO_OLD, FeatureSupportPolicy.visualAvailability(28, false, false)); }
    @Test public void modernMissingAppExplainsInstallation() { assertEquals(FeatureSupportPolicy.Availability.APP_MISSING, FeatureSupportPolicy.visualAvailability(34, false, true)); }
    @Test public void modernDeselectedExplainsSelection() { assertEquals(FeatureSupportPolicy.Availability.APP_NOT_SELECTED, FeatureSupportPolicy.visualAvailability(34, true, false)); }
    @Test public void modernSelectedAvailable() { assertEquals(FeatureSupportPolicy.Availability.AVAILABLE, FeatureSupportPolicy.visualAvailability(34, true, true)); }
    @Test public void storedVisualOnCannotLookEnabledOnOldOs() { assertFalse(FeatureSupportPolicy.visualChecked(33, true, true, true)); }
    @Test public void visualDoesNotOptInAutomatically() { assertFalse(FeatureSupportPolicy.visualChecked(34, true, true, false)); assertTrue(FeatureSupportPolicy.visualChecked(34, true, true, true)); }
    @Test public void savedVisualDoesNotOverrideMissingApp() { assertFalse(FeatureSupportPolicy.visualChecked(34, false, true, true)); assertFalse(FeatureSupportPolicy.visualChecked(34, true, false, true)); }
    @Test public void legacyTileHasVisibleState() {
        assertEquals("0회 · 광고만 넘김", FeatureSupportPolicy.tileLabel(28, "쇼츠 넘김", "0회 · 광고만 넘김"));
        assertEquals("권한 필요", FeatureSupportPolicy.tileLabel(26, "쇼츠 넘김", "권한 필요"));
    }
    @Test public void modernTileKeepsNameWithSeparateSubtitle() { assertEquals("쇼츠 넘김", FeatureSupportPolicy.tileLabel(29, "쇼츠 넘김", "광고만")); }
}
