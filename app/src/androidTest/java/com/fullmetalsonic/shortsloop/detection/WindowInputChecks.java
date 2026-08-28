package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.os.Parcel;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Synthetic framework window metadata only. No service binding, live roots, UI operations,
 * permission changes or gestures. Public Parcelable APIs construct detached fixtures; every
 * check uses new windows because the real guard takes ownership and recycles them.
 */
public final class WindowInputChecks {
    private static final int TARGET = 7;
    private int checks;

    public static int run() {
        return new WindowInputChecks().verify();
    }

    private int verify() {
        require(Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains("emulator"), "Disposable emulator only");
        Rect expected = new Rect(0, 0, 1000, 1800), corridor = new Rect(400, 450, 430, 1350);
        try {
            require(input(TARGET, expected, corridor, app(TARGET, expected)), "Unchanged window and contained clear corridor pass");
            require(expected.equals(new Rect(0, 0, 1000, 1800)) && corridor.equals(new Rect(400, 450, 430, 1350)),
                    "Guard does not mutate caller bounds or corridor");

            Rect moved = new Rect(10, 20, 1010, 1820);
            require(moved.contains(corridor), "Moved-window fixture still contains the entire old corridor");
            require(!input(TARGET, expected, corridor, app(TARGET, moved)), "Movement invalidates expected geometry even when old corridor is inside");
            require(input(TARGET, moved, corridor, app(TARGET, moved)), "The moved window is otherwise valid with newly matched bounds");
            Rect enlarged = new Rect(0, 0, 1200, 2000);
            require(enlarged.contains(corridor), "Expanded-window fixture still contains the old corridor");
            require(!input(TARGET, expected, corridor, app(TARGET, enlarged)), "Expansion invalidates expected geometry");
            require(input(TARGET, enlarged, corridor, app(TARGET, enlarged)), "Expansion is not rejected when expected bounds are fresh");
            Rect reduced = new Rect(0, 0, 900, 1700);
            require(reduced.contains(corridor), "Reduced-window fixture still contains the old corridor");
            require(!input(TARGET, expected, corridor, app(TARGET, reduced)), "Reduction also invalidates expected geometry");
            require(!input(TARGET, expected, null, app(TARGET, expected)), "Missing corridor never authorizes input");
            require(!input(TARGET, expected, new Rect(), app(TARGET, expected)), "Empty corridor never authorizes input");
            require(!input(TARGET, null, corridor, app(TARGET, expected)), "Missing expected window bounds never authorize input");
            require(!input(TARGET, new Rect(), corridor, app(TARGET, expected)), "Empty expected bounds cannot match a visible window");
            require(!input(TARGET, expected, new Rect(-1, 400, 430, 1300), app(TARGET, expected)), "Partly out-of-window corridor is rejected");
            require(!input(TARGET, expected, corridor), "Missing target window is rejected");
            require(!input(TARGET, expected, corridor, app(TARGET + 1, expected)), "Same geometry under a different window ID is rejected");
            require(!input(TARGET, expected, corridor, app(TARGET, expected), app(TARGET, expected)), "Duplicate target IDs remain ambiguous");

            // TYPE_APPLICATION exits overlayOwner before root/package lookup. The settings
            // title deliberately matches the old mistaken overlay title; no live root is needed.
            Spec settings = new Spec(9, 1, 3, true, expected, "ShortsLoop");
            require(!input(TARGET, expected, corridor, app(TARGET, expected), settings), "Our titled TYPE_APPLICATION settings cover is not exempted as a floating control");
            Spec outsideSettings = new Spec(9, 1, 3, true, new Rect(1020, 0, 1800, 1800), "ShortsLoop");
            require(input(TARGET, expected, corridor, app(TARGET, expected), outsideSettings), "Nonoverlapping settings in the other pane do not block this pane");

            Spec divider = new Spec(8, 5, 4, false, new Rect(980, 600, 1030, 900), null);
            require(observation(TARGET, app(TARGET, expected), divider), "Narrow boundary divider is an allowed observation exemption");
            require(input(TARGET, expected, corridor, app(TARGET, expected), divider), "Clear inner corridor remains usable beside the divider");
            require(!input(TARGET, expected, new Rect(970, 650, 990, 800), app(TARGET, expected), divider),
                    "An observation-exempt divider still blocks a touching input corridor");
            Spec caption = new Spec(8, 7, 4, false, new Rect(420, 30, 580, 70), null);
            require(observation(TARGET, app(TARGET, expected), caption), "Small top caption is allowed only for observation");
            require(!input(TARGET, expected, new Rect(450, 10, 480, 100), app(TARGET, expected), caption),
                    "Observation-exempt caption cannot be touched by an automatic gesture");
            require(semantic(TARGET, expected, expected, app(TARGET, expected), caption),
                    "Semantic pager action has no touch corridor through an exempt caption");
            require(!semantic(TARGET, expected, expected, app(TARGET, moved)), "Semantic scroll rechecks exact window geometry");
            require(!semantic(TARGET, expected, null, app(TARGET, expected)), "Semantic scroll requires a known page");
            require(!semantic(TARGET, expected, new Rect(-1, 0, 1000, 1800), app(TARGET, expected)), "Semantic page stays in its window");
            require(!semantic(TARGET, expected, expected, app(TARGET, expected), settings), "Semantic scroll cannot bypass a real covering settings window");
            return checks;
        } catch (RuntimeException error) {
            throw new AssertionError("Detached AccessibilityWindowInfo parcel fixture failed on API " + Build.VERSION.SDK_INT, error);
        }
    }

    private static Spec app(int id, Rect bounds) { return new Spec(id, 1, 1, false, bounds, null); }
    private static boolean input(int id, Rect expected, Rect corridor, Spec... specs) {
        return new YouTubeWindowGuard().allowsInput(windows(specs), id, expected, corridor);
    }
    private static boolean observation(int id, Spec... specs) {
        return new YouTubeWindowGuard().allows(windows(specs), id);
    }
    private static boolean semantic(int id, Rect expected, Rect page, Spec... specs) {
        return new YouTubeWindowGuard().allowsSemantic(windows(specs), id, expected, page);
    }
    @SuppressWarnings("deprecation")
    private static List<AccessibilityWindowInfo> windows(Spec... specs) {
        List<AccessibilityWindowInfo> result = new ArrayList<>();
        boolean ready = false;
        try {
            for (Spec spec : specs) {
                result.add(parcelWindow(spec));
            }
            ready = true; return result;
        } finally {
            // The real guard recycles successful fixtures in its own finally block.
            if (!ready) for (AccessibilityWindowInfo window : result) window.recycle();
        }
    }
    /**
     * AOSP writeToParcel prefixes: API26-29 five ints + Rect; API30-31 adds display
     * and uses Region; API32+ also adds task. Replace only this verified prefix and
     * title, copying the native blank object's entire remaining tail (including its
     * disconnected connection ID). Never assume later anchor/locale/child formats.
     * Sources: aosp-mirror/platform_frameworks_base tags android-8.0.0_r1,
     * android-11.0.0_r1, android-12.1.0_r1 and android-14.0.0_r1,
     * core/java/android/view/accessibility/AccessibilityWindowInfo.java.
     * This is test-only serialization, not a product API or a hidden-API exemption.
     */
    @SuppressWarnings("deprecation")
    private static AccessibilityWindowInfo parcelWindow(Spec spec) {
        AccessibilityWindowInfo blank = AccessibilityWindowInfo.obtain(), result = null;
        Parcel template = Parcel.obtain(), encoded = Parcel.obtain();
        boolean returned = false;
        try {
            blank.writeToParcel(template, 0); template.setDataPosition(0);
            if (Build.VERSION.SDK_INT >= 30) encoded.writeInt(template.readInt()); // Preserve native blank display ID.
            if (template.readInt() != blank.getType() || template.readInt() != blank.getLayer()
                    || template.readInt() != 0 || template.readInt() != blank.getId())
                throw new AssertionError("Unexpected native window parcel prefix");
            encoded.writeInt(spec.type); encoded.writeInt(spec.layer);
            encoded.writeInt(spec.focused ? 2 : 0); encoded.writeInt(spec.id);
            encoded.writeInt(template.readInt()); // Preserve absent parent.
            if (Build.VERSION.SDK_INT >= 32) encoded.writeInt(template.readInt()); // Preserve absent task.
            if (Build.VERSION.SDK_INT >= 30) {
                Region oldBounds = Region.CREATOR.createFromParcel(template), newBounds = new Region(spec.bounds);
                if (!oldBounds.isEmpty()) throw new AssertionError("Unexpected native blank window region");
                newBounds.writeToParcel(encoded, 0);
            } else {
                if (!Rect.CREATOR.createFromParcel(template).isEmpty()) throw new AssertionError("Unexpected native blank window rectangle");
                spec.bounds.writeToParcel(encoded, 0);
            }
            if (TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(template) != null)
                throw new AssertionError("Unexpected native blank window title");
            TextUtils.writeToParcel(spec.title, encoded, 0);
            encoded.appendFrom(template, template.dataPosition(), template.dataSize() - template.dataPosition());
            encoded.setDataPosition(0);
            result = AccessibilityWindowInfo.CREATOR.createFromParcel(encoded);
            Rect actual = new Rect(); result.getBoundsInScreen(actual);
            if (encoded.dataPosition() != encoded.dataSize() || result.getId() != spec.id || result.getType() != spec.type
                    || result.getLayer() != spec.layer || result.isFocused() != spec.focused || !spec.bounds.equals(actual)
                    || !TextUtils.equals(spec.title, result.getTitle()) || result.isInPictureInPictureMode()
                    || result.getChildCount() != 0 || result.getRoot() != null)
                throw new AssertionError("Synthetic framework window parcel did not round-trip as a detached window");
            returned = true; return result;
        } finally {
            if (!returned && result != null) result.recycle();
            blank.recycle(); template.recycle(); encoded.recycle();
        }
    }
    private static final class Spec {
        final int id, type, layer; final boolean focused; final Rect bounds; final String title;
        Spec(int id, int type, int layer, boolean focused, Rect bounds, String title) {
            this.id = id; this.type = type; this.layer = layer; this.focused = focused;
            this.bounds = new Rect(bounds); this.title = title;
        }
    }
    private void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); checks++; }
}
