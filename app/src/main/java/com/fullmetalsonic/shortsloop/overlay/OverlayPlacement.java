package com.fullmetalsonic.shortsloop.overlay;

import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.PositionPolicy;

/** Screen-coordinate placement only. An undersized/hidden host never borrows another pane. */
final class OverlayPlacement {
    private OverlayPlacement() { }
    static Rect area(Rect displaySafe, Rect host, int inset, int width, int height) {
        if (displaySafe == null || displaySafe.isEmpty() || host == null || host.isEmpty()
                || inset < 0 || width <= 0 || height <= 0) return null;
        Rect area = new Rect(displaySafe);
        if (!area.intersect(host)) return null;
        if ((long) area.width() < width + 2L * inset || (long) area.height() < height + 2L * inset) return null;
        area.inset(inset, inset);
        return area;
    }
    static Rect restore(Rect area, int width, int height, float x, float y) {
        int left = area.left + PositionPolicy.restore(x, area.width() - width);
        int top = area.top + PositionPolicy.restore(y, area.height() - height);
        return new Rect(left, top, left + width, top + height);
    }
    static Rect clamp(Rect area, int width, int height, int x, int y) {
        int left = area.left + PositionPolicy.clamp(x - area.left, area.width() - width);
        int top = area.top + PositionPolicy.clamp(y - area.top, area.height() - height);
        return new Rect(left, top, left + width, top + height);
    }
}
