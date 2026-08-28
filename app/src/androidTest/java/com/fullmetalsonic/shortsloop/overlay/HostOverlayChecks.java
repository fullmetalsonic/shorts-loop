package com.fullmetalsonic.shortsloop.overlay;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Detached native views/fake window writes, not permission grants or social-app E2E. */
public final class HostOverlayChecks {
    private static final String YT = SettingsStore.YOUTUBE_PACKAGE, IG = SettingsStore.INSTAGRAM_PACKAGE;
    private int checks;

    public static int run(Context context, SettingsStore store) {
        HostOverlayChecks test = new HostOverlayChecks();
        test.require(Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains("emulator"), "Disposable emulator only");
        test.require(Looper.myLooper() == Looper.getMainLooper(), "Native overlay checks run on the main thread");
        test.geometry(); test.layout(context); test.controllers(context, store);
        return test.checks;
    }

    private void geometry() {
        Rect display = new Rect(0, 24, 1000, 1976), upper = new Rect(0, 0, 1000, 990);
        Rect lower = new Rect(0, 1010, 1000, 2000);
        Rect a = OverlayPlacement.area(display, upper, 4, 72, 56);
        Rect b = OverlayPlacement.area(display, lower, 4, 72, 56);
        require(new Rect(4, 28, 996, 986).equals(a), "Upper pane intersects safe system bars before host inset");
        require(new Rect(4, 1014, 996, 1972).equals(b), "Lower pane never borrows divider or navigation bar");
        for (float x : new float[]{0, .5f, 1, -1, 2, Float.NaN, Float.POSITIVE_INFINITY}) {
            Rect first = OverlayPlacement.restore(a, 72, 56, x, x);
            Rect swapped = OverlayPlacement.restore(b, 72, 56, x, x);
            require(a.contains(first) && b.contains(swapped), "Saved position is confined before and after pane swap");
        }
        Rect small = OverlayPlacement.area(display, new Rect(400, 500, 600, 650), 4, 72, 56);
        require(small.contains(OverlayPlacement.restore(small, 72, 56, 1, 1)), "Resize restores within smaller host");
        require(new Rect(4, 28, 76, 84).equals(OverlayPlacement.clamp(a, 72, 56, -5000, -5000)), "Drag clamps at upper-left safe edge");
        require(new Rect(924, 930, 996, 986).equals(OverlayPlacement.clamp(a, 72, 56, 5000, 5000)), "Drag cannot enter lower pane");
        Rect exact = OverlayPlacement.area(display, new Rect(100, 100, 180, 164), 4, 72, 56);
        require(exact != null && exact.equals(OverlayPlacement.restore(exact, 72, 56, .9f, .35f)), "Exact fit is allowed without travel");
        require(OverlayPlacement.area(display, new Rect(100, 100, 179, 164), 4, 72, 56) == null, "Too-narrow pane stays hidden");
        require(OverlayPlacement.area(display, new Rect(100, 100, 180, 163), 4, 72, 56) == null, "Too-short pane stays hidden");
        require(OverlayPlacement.area(display, new Rect(2000, 0, 3000, 1000), 4, 72, 56) == null, "Off-display host stays hidden");
        require(OverlayPlacement.area(display, null, 4, 72, 56) == null, "Missing host stays hidden");
        require(OverlayPlacement.area(display, new Rect(), 4, 72, 56) == null, "Empty host stays hidden");
        require(OverlayPlacement.area(null, upper, 4, 72, 56) == null, "Missing display stays hidden");
        require(display.equals(new Rect(0, 24, 1000, 1976)) && upper.equals(new Rect(0, 0, 1000, 990)), "Geometry does not mutate caller rectangles");
    }

    private void layout(Context base) {
        for (String language : new String[]{"ko", "en"}) for (String host : com.fullmetalsonic.shortsloop.core.HostRegistry.packages()) {
            for (float scale : new float[]{1f, 2f}) for (boolean rtl : new boolean[]{false, true}) {
                Context localized = AppLocale.forLanguage(base, language);
                Configuration config = new Configuration(localized.getResources().getConfiguration());
                config.fontScale = scale; config.setLayoutDirection(rtl ? new Locale("ar") : Locale.ENGLISH);
                Context context = localized.createConfigurationContext(config);
                FloatingContent content = new FloatingContent(context, host);
                content.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
                TextView badge = content.findViewById(R.id.floating_host), count = content.findViewById(R.id.floating_count);
                TextView close = content.findViewById(R.id.floating_close);
                for (String label : new String[]{"99/99", "~99/99", context.getString(R.string.flo_ads_live), context.getString(R.string.flo_stop)}) {
                    count.setText(label); measure(content);
                    fits(badge); fits(count); fits(close);
                    require(content.getWidth() == dp(context, 72) && content.getHeight() == dp(context, 56), "Hosted control keeps compact dimensions");
                    require(badge.getBottom() <= count.getPaddingTop(), "Badge cannot cover count text");
                    require(rtl ? badge.getLeft() >= close.getRight() : badge.getRight() <= close.getLeft(), "Badge and close do not overlap in either direction");
                }
                require((YT.equals(host) ? "YT" : IG.equals(host) ? "IG" : "TT").contentEquals(badge.getText()), "App badge is unambiguous without color");
                require(close.getContentDescription().toString().contains(YT.equals(host) ? "YouTube" : IG.equals(host) ? "Instagram" : "TikTok"), "Close action announces the full app name");
                require(!badge.isClickable() && !badge.isFocusable() && badge.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_NO,
                        "Decorative badge does not add a duplicate focus target");
                int[] clicks = {0, 0};
                count.setOnClickListener(v -> clicks[0]++); close.setOnClickListener(v -> clicks[1]++);
                View.OnTouchListener syncClick = (v, event) -> { if (event.getActionMasked() == MotionEvent.ACTION_UP) v.performClick(); return true; };
                count.setOnTouchListener(syncClick); close.setOnTouchListener(syncClick);
                tap(content, badge.getLeft() + badge.getWidth() / 2f, badge.getHeight() / 2f);
                require(clicks[0] == 1 && clicks[1] == 0, "Badge region retains count/drag hit surface");
                tap(content, close.getLeft() + close.getWidth() / 2f, close.getHeight() / 2f);
                require(clicks[0] == 1 && clicks[1] == 1, "Host close hit target remains separate");
            }
        }
    }

    private void controllers(Context base, SettingsStore store) {
        // Migrate before snapshotting: the fixture only changes/restores host position values.
        SettingsStore youtube = store.forHost(YT), instagram = store.forHost(IG);
        float yx = youtube.x(), yy = youtube.y(), ix = instagram.x(), iy = instagram.y();
        Map<String, ?> original = new HashMap<>(store.preferences.getAll());
        Sink yw = new Sink(base.getSystemService(WindowManager.class)), iw = new Sink(base.getSystemService(WindowManager.class));
        Events ye = new Events(), ie = new Events();
        FloatingController y = new FloatingController(new FixtureContext(base, yw.manager), store, ye);
        FloatingController i = new FloatingController(new FixtureContext(base, iw.manager), store, ie);
        try {
            youtube.position(0, 0); instagram.position(1, 1);
            Rect full = display(base), upper = new Rect(full.left, full.top, full.right, full.centerY() - dp(base, 4));
            Rect lower = new Rect(full.left, full.centerY() + dp(base, 4), full.right, full.bottom);
            y.setHost(YT); i.setHost(IG); y.show(); i.show();
            require(y.bounds().isEmpty() && i.bounds().isEmpty(), "Hosted controllers require explicit visible window bounds");
            Rect passed = new Rect(upper); y.setAllowedBounds(passed); passed.setEmpty(); i.setAllowedBounds(lower);
            require(yw.attached == null && iw.attached == null, "Bounds alone do not enable a hidden control");
            y.show(); i.show(); y.update(1, 2, "playback.counting"); i.update(3, 7, "playback.counting");
            require(upper.contains(y.bounds()) && lower.contains(i.bounds()), "Two controllers stay in their own app panes");
            require("1/2".contentEquals(number(yw).getText()) && "3/7".contentEquals(number(iw).getText()), "App counts do not overwrite one another");
            require(number(yw).getContentDescription().toString().startsWith("YouTube") && number(iw).getContentDescription().toString().startsWith("Instagram"), "Count accessibility identifies the full host");
            require(yw.params.alpha == .8f && iw.params.alpha == .8f, "Existing translucency is retained");
            int adds = yw.adds; y.setHost(YT); y.setAllowedBounds(upper); y.show();
            require(yw.adds == adds, "Repeated host/bounds/show calls do not recreate the overlay");
            Rect igBefore = i.bounds();
            drag(number(yw), 0, 0, 100000, 100000);
            require(upper.contains(y.bounds()) && i.bounds().equals(igBefore), "YouTube drag cannot move Instagram or cross its pane");
            require(youtube.x() == 1f && youtube.y() == 1f && instagram.x() == 1f && instagram.y() == 1f, "Drag saves only scoped normalized position");
            instagram.position(.25f, .75f);
            y.setAllowedBounds(lower); i.setAllowedBounds(upper);
            require(lower.contains(y.bounds()) && upper.contains(i.bounds()), "Swapping app window order relocates each controller safely");
            require(youtube.x() == 1f && youtube.y() == 1f && instagram.x() == .25f && instagram.y() == .75f, "Window swap does not rewrite saved relative positions");
            Rect right = new Rect(full.centerX(), full.top, full.right, full.bottom);
            y.setAllowedBounds(right); require(right.contains(y.bounds()), "Resizing to a right-side pane clamps within its window");
            y.setAllowedBounds(full); require(full.contains(y.bounds()), "Returning to fullscreen retains a safe relative position");
            Rect restored = y.bounds(); y.hide(); y.show();
            require(restored.equals(y.bounds()), "Hide/show restores the host position");
            require("1/2".contentEquals(number(yw).getText()), "Show restores the cached count without another update callback");
            require(number(yw).getContentDescription() != null && number(yw).getContentDescription().toString().startsWith("YouTube"),
                    "Show restores the host-specific accessibility description");
            int restoredCycles = ye.cycles, restoredCloses = ye.closes;
            y.update(0, 2, "blocked:error.transition");
            String stopped = number(yw).getText().toString(), stopDescription = number(yw).getContentDescription().toString();
            y.setAllowedBounds(null); y.setAllowedBounds(full); y.show();
            require(!stopped.isEmpty() && stopped.equals(number(yw).getText().toString()), "Blocked visibility refresh restores the stop label without publishing again");
            require(stopDescription.equals(number(yw).getContentDescription().toString()), "Blocked visibility refresh restores the stop explanation");
            require("3/7".contentEquals(number(iw).getText()), "Restoring one host never overwrites its sibling count");
            y.hide(); y.update(0, 2, "timed.waiting", 7); y.show();
            require(number(yw).getContext().getString(R.string.flo_seconds, 7).contentEquals(number(yw).getText()),
                    "An update received while hidden restores its remaining seconds on show");
            require(number(yw).getContentDescription().toString().startsWith("YouTube")
                    && number(yw).getContentDescription().toString().contains("7"), "Restored countdown accessibility includes host and seconds");
            require(ye.cycles == restoredCycles && ye.closes == restoredCloses, "Restoring presentation causes no cycle or close action");
            y.update(8, 9, "playback.counting"); y.configurationChanged();
            require(restored.equals(y.bounds()) && "8/9".contentEquals(number(yw).getText()), "Configuration rebuild preserves host geometry and own count");
            require("YT".contentEquals(((TextView)yw.attached.findViewById(R.id.floating_host)).getText()), "Configuration rebuild retains host badge");
            View old = number(yw); int ends = ye.ends, cycles = ye.cycles;
            event(old, MotionEvent.ACTION_DOWN, 0, 0); y.setAllowedBounds(upper); event(old, MotionEvent.ACTION_UP, 0, 0);
            require(!ye.active && ye.ends == ends + 1 && ye.cycles == cycles, "Bounds change cancels an in-flight tap without cycling");
            old = number(yw); event(old, MotionEvent.ACTION_DOWN, 0, 0); y.setAllowedBounds(null); event(old, MotionEvent.ACTION_UP, 0, 0); y.show();
            require(y.bounds().isEmpty() && yw.attached == null && !ye.active && ye.cycles == cycles, "Null bounds hide and cancel late touch even if show is called");
            y.setAllowedBounds(new Rect(full.left, full.top, full.left + 1, full.top + 1)); y.show();
            require(y.bounds().isEmpty(), "Undersized live window cannot create an out-of-pane overlay");
            y.setAllowedBounds(upper); y.show();
            number(yw).performClick(); require(ye.cycles == cycles + 1 && ie.cycles == 0, "YouTube cycle callback does not reach Instagram");
            yw.attached.findViewById(R.id.floating_close).performClick();
            require(ye.closes == 1 && ie.closes == 0 && !i.bounds().isEmpty(), "YouTube close delegates only to its listener");
            iw.attached.findViewById(R.id.floating_close).performClick();
            require(ye.closes == 1 && ie.closes == 1, "Instagram close has its own listener");
            boolean rejected = false; try { y.setHost("other.app"); } catch (IllegalArgumentException expected) { rejected = true; }
            require(rejected && !y.bounds().isEmpty(), "Invalid host is rejected without changing visible state");
            y.setHost(IG); require(y.bounds().isEmpty(), "Changing host invalidates old window/count binding");
            y.show(); require(y.bounds().isEmpty(), "Changed host cannot reuse the prior allowed bounds");
        } finally {
            y.hide(); i.hide(); youtube.position(yx, yy); instagram.position(ix, iy);
        }
        require(yw.attached == null && iw.attached == null && yw.adds == yw.removes && iw.adds == iw.removes, "All fake windows are released");
        require(original.equals(store.preferences.getAll()), "Controller checks restore positions and leave all other settings unchanged");
    }

    private void fits(TextView text) {
        android.text.Layout layout = text.getLayout();
        require(layout != null && layout.getLineCount() == 1 && layout.getLineEnd(0) == text.length() && layout.getEllipsisCount(0) == 0, "Complete one-line host/count/close text");
        require(layout.getLineWidth(0) <= text.getWidth() - text.getCompoundPaddingLeft() - text.getCompoundPaddingRight() + .5f
                && layout.getHeight() <= text.getHeight() - text.getCompoundPaddingTop() - text.getCompoundPaddingBottom(), "Hosted text fits available native layout");
    }
    private static void measure(View view) {
        int width = dp(view.getContext(), 72), height = dp(view.getContext(), 56);
        for (int n = 0; n < 3; n++) { view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)); view.layout(0, 0, width, height); }
    }
    private static TextView number(Sink sink) { return sink.attached.findViewById(R.id.floating_count); }
    private static void event(View view, int action, float x, float y) {
        long now = SystemClock.uptimeMillis(); MotionEvent event = MotionEvent.obtain(now, now, action, x, y, 0);
        try { view.dispatchTouchEvent(event); } finally { event.recycle(); }
    }
    private static void tap(View view, float x, float y) { event(view, MotionEvent.ACTION_DOWN, x, y); event(view, MotionEvent.ACTION_UP, x, y); }
    private static void drag(View view, float x, float y, float toX, float toY) {
        event(view, MotionEvent.ACTION_DOWN, x, y); event(view, MotionEvent.ACTION_MOVE, toX, toY); event(view, MotionEvent.ACTION_UP, toX, toY);
    }
    @SuppressWarnings("deprecation") private static Rect display(Context base) {
        WindowManager manager = base.getSystemService(WindowManager.class);
        if (Build.VERSION.SDK_INT >= 30) return new Rect(manager.getCurrentWindowMetrics().getBounds());
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics(); manager.getDefaultDisplay().getRealMetrics(metrics);
        return new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);
    }
    private static int dp(Context context, int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
    private static final class Events implements FloatingController.Listener {
        int cycles, closes, ends; boolean active;
        public void cycle() { cycles++; } public void close() { closes++; }
        public void interaction(boolean value) { active = value; if (!value) ends++; }
    }
    private static final class FixtureContext extends ContextWrapper {
        final WindowManager manager;
        FixtureContext(Context base, WindowManager manager) { super(base); this.manager = manager; }
        @Override public Object getSystemService(String name) { return WINDOW_SERVICE.equals(name) ? manager : super.getSystemService(name); }
    }
    private static final class Sink {
        final WindowManager manager; View attached; WindowManager.LayoutParams params; int adds, removes;
        Sink(WindowManager geometry) {
            if (geometry == null) throw new AssertionError("Read-only display geometry is required");
            manager = (WindowManager) Proxy.newProxyInstance(WindowManager.class.getClassLoader(), new Class<?>[]{WindowManager.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "addView": if (attached != null) throw new AssertionError("Duplicate fake overlay"); attached = (View)args[0]; params = (WindowManager.LayoutParams)args[1]; adds++; return null;
                    case "removeView": case "removeViewImmediate": if (attached != args[0]) throw new AssertionError("Unknown fake overlay"); attached = null; params = null; removes++; return null;
                    case "updateViewLayout": if (attached != args[0]) throw new AssertionError("Unknown update"); params = (WindowManager.LayoutParams)args[1]; return null;
                    case "getCurrentWindowMetrics": case "getMaximumWindowMetrics": case "getDefaultDisplay":
                        try { return method.invoke(geometry, args); } catch (InvocationTargetException error) { throw error.getCause(); }
                    case "toString": return "HostOverlaySink";
                    case "hashCode": return System.identityHashCode(proxy);
                    case "equals": return proxy == args[0];
                    default: throw new AssertionError("Unexpected window write: " + method.getName());
                }
            });
        }
    }
}
