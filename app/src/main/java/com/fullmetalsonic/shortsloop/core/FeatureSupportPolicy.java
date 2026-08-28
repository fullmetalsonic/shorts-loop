package com.fullmetalsonic.shortsloop.core;

/** OS capability only; never guesses another app's minimum version or detection success. */
public final class FeatureSupportPolicy {
    public static final int TILE_SUBTITLE_SDK = 29;
    public static final int TILE_ADD_REQUEST_SDK = 33;
    public static final int VISUAL_WINDOW_CAPTURE_SDK = 34;
    public enum Availability { AVAILABLE, ANDROID_TOO_OLD, APP_MISSING, APP_NOT_SELECTED }
    private FeatureSupportPolicy() { }

    public static boolean tileSubtitle(int sdk) { return sdk >= TILE_SUBTITLE_SDK; }
    public static boolean tileAddRequest(int sdk) { return sdk >= TILE_ADD_REQUEST_SDK; }
    public static boolean visualCapture(int sdk) { return sdk >= VISUAL_WINDOW_CAPTURE_SDK; }
    public static boolean instagramFeature(boolean installed, boolean selected) { return installed && selected; }
    public static Availability visualAvailability(int sdk, boolean installed, boolean selected) {
        if (!visualCapture(sdk)) return Availability.ANDROID_TOO_OLD;
        if (!installed) return Availability.APP_MISSING;
        return selected ? Availability.AVAILABLE : Availability.APP_NOT_SELECTED;
    }
    public static boolean visualChecked(int sdk, boolean installed, boolean selected, boolean saved) {
        return saved && visualAvailability(sdk, installed, selected) == Availability.AVAILABLE;
    }
    public static String tileLabel(int sdk, String name, String state) {
        // Old tiles have only one text area: prioritize state; keep the name in contentDescription.
        return tileSubtitle(sdk) ? name : state;
    }
}
