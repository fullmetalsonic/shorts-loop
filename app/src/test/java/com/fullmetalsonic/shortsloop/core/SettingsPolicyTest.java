package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class SettingsPolicyTest {
    @Test public void numericInputAcceptsEverySupportedCount() {
        for (int count = 0; count <= 99; count++) assertEquals(Integer.valueOf(count), SettingsPolicy.parseCount(Integer.toString(count)));
    }
    @Test public void blankInputDoesNotMeanZero() {
        assertNull(SettingsPolicy.parseCount(null)); assertNull(SettingsPolicy.parseCount("")); assertNull(SettingsPolicy.parseCount("  "));
    }
    @Test public void invalidInputIsRejectedRatherThanSilentlyClamped() {
        for (String value : new String[] {"-1", "+1", "1.5", "1e1", "100", "999", "2 0", "abc", "２"}) assertNull(value, SettingsPolicy.parseCount(value));
    }
    @Test public void hugePastedNumberCannotOverflowToValidValue() {
        assertNull(SettingsPolicy.parseCount("4294967296"));
        assertNull(SettingsPolicy.parseCount("99999999999999999999999999999999999999999999999999999999999999999"));
    }
    @Test public void whitespaceAndLeadingZeroesAreUnambiguous() {
        assertEquals(Integer.valueOf(2), SettingsPolicy.parseCount(" 02 "));
        assertEquals(Integer.valueOf(0), SettingsPolicy.parseCount("000"));
    }
    @Test public void pastedLeadingZeroesMustNotTruncateTheIntendedCount() {
        assertEquals(Integer.valueOf(12), SettingsPolicy.parseCount("000012"));
        assertNull(SettingsPolicy.parseCount("0000100"));
    }
    @Test public void legacyPositiveTargetBecomesTheConfiguredCeiling() {
        assertEquals(1, SettingsPolicy.legacyCeiling(1, 2)); assertEquals(2, SettingsPolicy.legacyCeiling(2, 1));
    }
    @Test public void legacyZeroKeepsLastPositiveCeilingForFloatingResume() {
        assertEquals(1, SettingsPolicy.legacyCeiling(0, 1)); assertEquals(2, SettingsPolicy.legacyCeiling(0, 2));
        assertEquals(2, SettingsPolicy.legacyCeiling(0, 0));
    }
    @Test public void invalidLegacyValuesHaveDefinedDefaults() {
        assertEquals(2, SettingsPolicy.legacyCeiling(-1, 1)); assertEquals(2, SettingsPolicy.legacyCeiling(100, 1));
        assertEquals(2, SettingsPolicy.legacyCeiling(0, -1));
    }
}
