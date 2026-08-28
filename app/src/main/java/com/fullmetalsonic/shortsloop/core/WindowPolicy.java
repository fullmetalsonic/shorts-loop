package com.fullmetalsonic.shortsloop.core;

import java.util.List;

/** Metadata-only policy, independent of Android caches and screen order. */
public final class WindowPolicy {
    public record Box(int left, int top, int right, int bottom) {
        public int width() { return right-left; }
        public int height() { return bottom-top; }
        public boolean empty() { return width() <= 0 || height() <= 0; }
        public boolean overlaps(Box b) { return !empty() && !b.empty() && left < b.right && b.left < right && top < b.bottom && b.top < bottom; }
    }
    public record Window(int id, int type, int layer, boolean focused, boolean pip, boolean ownOverlay, Box bounds, boolean systemUi) {
        public Window(int id, int type, int layer, boolean focused, boolean pip, boolean ownOverlay, Box bounds) {
            this(id,type,layer,focused,pip,ownOverlay,bounds,false);
        }
    }
    private WindowPolicy() { }
    public static boolean ownOverlay(int type, boolean focused, CharSequence nodePackage) {
        return (type == 3 || type == 4) && !focused && nodePackage != null
                && "com.fullmetalsonic.shortsloop".contentEquals(nodePackage);
    }
    public static boolean systemUi(int type, CharSequence nodePackage) {
        return type == 3 && nodePackage != null && "com.android.systemui".contentEquals(nodePackage);
    }
    /** Observation exemptions never exempt a window from the actual touch corridor. */
    public static boolean inputClear(List<Window> windows, int id, Box corridor) {
        if (corridor == null || corridor.empty() || !allowed(windows, id)) return false;
        Window target = null;
        for (Window window : windows) if (window.id == id) target = window;
        if (target == null || corridor.left < target.bounds.left || corridor.top < target.bounds.top
                || corridor.right > target.bounds.right || corridor.bottom > target.bounds.bottom) return false;
        for (Window other : windows) if (other.id != id && other.layer > target.layer
                && other.bounds.overlaps(corridor)) return false;
        return true;
    }
    public static boolean allowed(List<Window> windows, int id) {
        Window target = null;
        for (Window window : windows) if (window.id == id) { if (target != null) return false; target = window; }
        if (target == null || target.type != 1 || target.pip || target.bounds.empty()) return false;
        for (Window other : windows) {
            if (other.id == id || other.ownOverlay && (other.type == 3 || other.type == 4)) continue;
            if (other.focused && other.type != 1) return false;
            if (other.layer <= target.layer || !target.bounds.overlaps(other.bounds)) continue;
            if (other.type == 5 && !other.focused && boundaryHandle(target.bounds, other.bounds)) continue;
            // API36 TYPE_WINDOW_CONTROL: only the small, unfocused top caption handle.
            // Expanded controls/menus still block; inputClear separately checks every touch path.
            if (other.type == 7 && !other.focused && captionHandle(target.bounds, other.bounds)) continue;
            if (other.type == 3 && !other.focused && edgeBar(target.bounds, other.bounds)) continue;
            if (systemHandleInsideBar(windows, target, other)) continue;
            return false;
        }
        return true;
    }
    private static boolean systemHandleInsideBar(List<Window> windows, Window target, Window handle) {
        if (handle.type != 3 || handle.focused || !handle.systemUi || !captionHandle(target.bounds,handle.bounds)) return false;
        // OEM fullscreen handles may be TYPE_SYSTEM instead of TYPE_WINDOW_CONTROL.
        // Only accept one entirely inside an already-qualified SystemUI edge bar.
        for (Window bar : windows) if (bar.id != handle.id && bar.type == 3 && !bar.focused && bar.systemUi
                && bar.layer > target.layer && edgeBar(target.bounds,bar.bounds)
                && handle.bounds.left >= bar.bounds.left && handle.bounds.right <= bar.bounds.right
                && handle.bounds.top >= bar.bounds.top && handle.bounds.bottom <= bar.bounds.bottom) return true;
        return false;
    }
    private static boolean captionHandle(Box pane, Box handle) {
        int longSide = Math.max(pane.width(), pane.height());
        return !handle.empty() && handle.left >= pane.left && handle.right <= pane.right
                && handle.top >= pane.top && handle.bottom <= pane.top + Math.min(pane.height() * .25f, longSide * .12f)
                && handle.width() <= Math.min(pane.width() * .8f, longSide * .18f)
                && handle.height() <= Math.min(pane.height() * .1f, longSide * .05f)
                && handle.width() >= handle.height() * 2
                && Math.abs((handle.left + handle.right) - (pane.left + pane.right)) <= pane.width() * .2f;
    }
    private static boolean boundaryHandle(Box pane, Box handle) {
        int overlapWidth = Math.min(pane.right, handle.right) - Math.max(pane.left, handle.left);
        int overlapHeight = Math.min(pane.bottom, handle.bottom) - Math.max(pane.top, handle.top);
        return overlapWidth <= pane.width() * .04f && (handle.left <= pane.left || handle.right >= pane.right)
                || overlapHeight <= pane.height() * .04f && (handle.top <= pane.top || handle.bottom >= pane.bottom);
    }
    private static boolean edgeBar(Box pane, Box cover) {
        // OS bars retain their thickness when split panes become shorter/narrower.
        // Bound both the display-like long axis and the covered pane fraction.
        int longSide = Math.max(pane.width(), pane.height());
        return cover.left <= pane.left && cover.right >= pane.right
                    && cover.height() <= Math.min(pane.height() * .12f, longSide * .08f)
                    && (cover.top <= pane.top || cover.bottom >= pane.bottom)
                || cover.top <= pane.top && cover.bottom >= pane.bottom
                    && cover.width() <= Math.min(pane.width() * .12f, longSide * .04f)
                    && (cover.left <= pane.left || cover.right >= pane.right);
    }
}
