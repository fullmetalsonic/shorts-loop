package com.fullmetalsonic.shortsloop.detection;

import android.view.accessibility.AccessibilityWindowInfo;
import android.graphics.Rect;
import java.util.List;

/** Window metadata only: never reads the content of another app or System UI. */
public final class YouTubeWindowGuard {
    public boolean allows(List<AccessibilityWindowInfo> windows, int videoWindowId) {
        return allowedBounds(windows, videoWindowId) != null;
    }
    public Rect allowedBounds(List<AccessibilityWindowInfo> windows, int videoWindowId) {
        try {
            for (AccessibilityWindowInfo window : windows) {
                if (window.isFocused()) {
                    boolean allowed = window.getId() == videoWindowId
                            && window.getType() == AccessibilityWindowInfo.TYPE_APPLICATION
                            && !window.isInPictureInPictureMode();
                    if (!allowed) return null;
                    Rect bounds = new Rect(); window.getBoundsInScreen(bounds);
                    return bounds.isEmpty() ? null : bounds;
                }
            }
            return null;
        } finally {
            for (AccessibilityWindowInfo window : windows) recycle(window);
        }
    }
    @SuppressWarnings("deprecation")
    private static void recycle(AccessibilityWindowInfo window) { window.recycle(); }
}
