package com.fullmetalsonic.shortsloop.detection;

import android.view.accessibility.AccessibilityWindowInfo;
import java.util.List;

/** Window metadata only: never reads the content of another app or System UI. */
public final class YouTubeWindowGuard {
    public boolean allows(List<AccessibilityWindowInfo> windows, int videoWindowId) {
        try {
            for (AccessibilityWindowInfo window : windows) {
                if (window.isFocused()) {
                    return window.getId() == videoWindowId
                            && window.getType() == AccessibilityWindowInfo.TYPE_APPLICATION
                            && !window.isInPictureInPictureMode();
                }
            }
            return false;
        } finally {
            for (AccessibilityWindowInfo window : windows) recycle(window);
        }
    }
    @SuppressWarnings("deprecation")
    private static void recycle(AccessibilityWindowInfo window) { window.recycle(); }
}
