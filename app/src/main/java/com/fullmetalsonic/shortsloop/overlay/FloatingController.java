package com.fullmetalsonic.shortsloop.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.i18n.StatusText;
import com.fullmetalsonic.shortsloop.core.PositionPolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;
import com.fullmetalsonic.shortsloop.core.PlaybackRestart;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;

public final class FloatingController {
    private static final int WIDTH_DP = FloatingContent.WIDTH_DP, HEIGHT_DP = FloatingContent.HEIGHT_DP;
    public interface Listener { void cycle(); void close(); void interaction(boolean dragging); }
    private final Context context;
    private Context localized;
    private int lastCurrent, lastTarget, lastRemaining = -1;
    private String lastStatus = "off";
    private final SettingsStore baseStore;
    private SettingsStore store;
    private String host = "";
    private Rect allowedBounds, placementArea;
    private boolean boundsSpecified;
    private final Listener listener;
    private final WindowManager manager;
    private FrameLayout root;
    private TextView number;
    private WindowManager.LayoutParams params;
    private int availableX, availableY, insetX, insetY;
    private int originX, originY;
    private float downX, downY;
    private boolean dragging, touchActive;

    public FloatingController(Context context, SettingsStore store, Listener listener) {
        this.context = context; this.baseStore = store; this.store = store; this.listener = listener;
        manager = context.getSystemService(WindowManager.class);
        localized = AppLocale.wrap(context);
    }

    /** One controller belongs to one host. Changing hosts never carries another host's count or position. */
    public void setHost(String packageName) {
        if (!com.fullmetalsonic.shortsloop.core.HostRegistry.supports(packageName))
            throw new IllegalArgumentException("Unsupported floating host");
        if (host.equals(packageName)) return;
        hide(); host = packageName; store = baseStore.forHost(packageName);
        allowedBounds = null; boundsSpecified = true; placementArea = null;
        lastCurrent = lastTarget = 0; lastRemaining = -1; lastStatus = "off";
    }

    /** Absolute screen bounds. Null hides this host; only an explicit show() can make it visible again. */
    public void setAllowedBounds(Rect hostBounds) {
        Rect next = hostBounds == null ? null : new Rect(hostBounds);
        if (boundsSpecified && java.util.Objects.equals(allowedBounds, next)) return;
        endInteraction(); boundsSpecified = true; allowedBounds = next;
        if (!measureArea()) { hide(); return; }
        if (root != null) { restorePosition(); manager.updateViewLayout(root, params); }
    }
    // Coordinates are absolute screen pixels, deliberately independent of text direction.
    @SuppressLint({"ClickableViewAccessibility", "RtlHardcoded"})
    public void show() {
        if (root != null) return;
        if (!measureArea()) return;
        root = new FloatingContent(localized, host);
        number = root.findViewById(R.id.floating_count);
        Button close = root.findViewById(R.id.floating_close);
        close.setOnClickListener(view -> listener.close());
        number.setOnClickListener(view -> listener.cycle());
        number.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX(); downY = event.getRawY(); originX = params.x; originY = params.y;
                    dragging = false; touchActive = true; listener.interaction(true); return true;
                case MotionEvent.ACTION_MOVE:
                    if (!touchActive || placementArea == null) return true;
                    float dx = event.getRawX() - downX, dy = event.getRawY() - downY;
                    if (Math.hypot(dx, dy) > ViewConfiguration.get(context).getScaledTouchSlop()) dragging = true;
                    if (dragging) {
                        Rect moved = OverlayPlacement.clamp(placementArea, dp(WIDTH_DP), dp(HEIGHT_DP),
                                originX + Math.round(dx), originY + Math.round(dy));
                        params.x = moved.left; params.y = moved.top;
                        manager.updateViewLayout(root, params);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!touchActive) return true;
                    if (dragging) savePosition(); else view.performClick();
                    endInteraction(); return true;
                case MotionEvent.ACTION_CANCEL:
                    if (!touchActive) return true;
                    if (dragging) savePosition();
                    endInteraction(); return true;
                default: return true;
            }
        });
        params = new WindowManager.LayoutParams(dp(WIDTH_DP), dp(HEIGHT_DP), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.alpha = 0.8f;
        params.setTitle(host.isEmpty() ? "ShortsLoop" : "ShortsLoop · " + FloatingContent.hostName(localized, host));
        restorePosition();
        try { manager.addView(root, params); }
        catch (RuntimeException failure) { root = null; number = null; throw failure; }
        update(lastCurrent, lastTarget, lastStatus, lastRemaining);
    }
    public void update(int current, int target, String status) {
        update(current, target, status, -1);
    }
    public void update(int current, int target, String status, int remainingSeconds) {
        lastCurrent = current; lastTarget = target; lastStatus = status; lastRemaining = remainingSeconds;
        if (number == null) return;
        String liveLabel = LiveSkipPolicy.floatingLabel(status, remainingSeconds);
        boolean timedStatus = "timed.checking".equals(status) || "timed.waiting".equals(status);
        String label = status.startsWith("blocked:") ? localized.getString(R.string.flo_stop)
                : "photo.rules".equals(status) ? localized.getString(R.string.flo_rules)
                : status.startsWith("photo.") ? "photo.confirming".equals(status) ? localized.getString(R.string.flo_next)
                    : remainingSeconds >= 0 ? localized.getString(R.string.flo_seconds, remainingSeconds) : localized.getString(R.string.flo_photo)
                : PlaybackRestart.WAITING.equals(status) ? localized.getString(R.string.flo_wait)
                : LongVideoPolicy.CHECKING.equals(status) || LongVideoPolicy.CONFIRMING.equals(status) ? localized.getString(R.string.flo_long)
                : target == 0 && store.skipLong() && status.startsWith("zero.long") ? localized.getString(R.string.flo_rules)
                : liveLabel != null ? LiveSkipPolicy.STATUS_CONFIRMING.equals(status) ? localized.getString(R.string.flo_next)
                    : remainingSeconds > 0 ? localized.getString(R.string.flo_seconds, remainingSeconds) : localized.getString(R.string.flo_live)
                : remainingSeconds >= 0 && timedStatus ? localized.getString(R.string.flo_seconds, remainingSeconds)
                : target == 0 && LiveSkipPolicy.zeroCountStatus(true, true).equals(status) ? localized.getString(R.string.flo_ads_live)
                : target == 0 && LiveSkipPolicy.zeroCountStatus(false, true).equals(status) ? localized.getString(R.string.flo_live)
                : target == 0 && (LiveSkipPolicy.zeroCountStatus(true, false).equals(status) || "ads.confirming".equals(status)) ? localized.getString(R.string.flo_ads)
                : status.equals("timed.confirming") ? localized.getString(R.string.flo_next)
                : status.startsWith("visual.") ? (status.startsWith("visual.error.") ? "?/" : "…/") + target
                : (status.startsWith("estimate.") ? "~" : "") + current + "/" + target;
        if (!label.contentEquals(number.getText())) number.setText(label);
        String readable = StatusText.text(localized, status);
        String description = status.startsWith("photo.") ? remainingSeconds >= 0 ? localized.getString(R.string.photo_status_remaining, readable, remainingSeconds) : readable
                : liveLabel != null ? localized.getString(R.string.flo_live_description, readable,
                    remainingSeconds > 0 && !LiveSkipPolicy.STATUS_CONFIRMING.equals(status)
                        ? localized.getString(R.string.flo_remaining, remainingSeconds) : "")
                : remainingSeconds >= 0 && timedStatus ? localized.getString(R.string.flo_timed_description, remainingSeconds, readable)
                : localized.getString(R.string.floating_description, current, target, readable);
        if (!host.isEmpty()) description = localized.getString(R.string.host_overlay_description,
                FloatingContent.hostName(localized, host), description);
        if (!description.contentEquals(number.getContentDescription() == null ? "" : number.getContentDescription()))
            number.setContentDescription(description);
    }
    public void configurationChanged() {
        localized = AppLocale.wrap(context);
        if (root == null) return;
        // Rebuild only presentation: the saved position and execution settings stay unchanged.
        if (touchActive) endInteraction(); else if (listener != null) listener.interaction(false);
        hide(); show();
        update(lastCurrent, lastTarget, lastStatus, lastRemaining);
    }
    private void restorePosition() {
        Rect restored = OverlayPlacement.restore(placementArea, dp(WIDTH_DP), dp(HEIGHT_DP), store.x(), store.y());
        params.x = restored.left; params.y = restored.top;
    }
    private void savePosition() {
        store.position(PositionPolicy.save(params.x - insetX, availableX), PositionPolicy.save(params.y - insetY, availableY));
    }
    @SuppressWarnings("deprecation")
    private boolean measureArea() {
        if (boundsSpecified && allowedBounds == null) { placementArea = null; return false; }
        int width, height, right = 0, bottom = dp(24), left = 0, top = 0;
        insetX = 0; insetY = dp(28);
        if (Build.VERSION.SDK_INT >= 30) {
            android.view.WindowMetrics metrics = manager.getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds(); width = bounds.width(); height = bounds.height(); left = bounds.left; top = bounds.top;
            android.graphics.Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            insetX = insets.left; insetY = insets.top; right = insets.right; bottom = insets.bottom;
        } else {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            manager.getDefaultDisplay().getRealMetrics(metrics); width = metrics.widthPixels; height = metrics.heightPixels;
        }
        Rect displaySafe = new Rect(left + insetX, top + insetY, left + width - right, top + height - bottom);
        placementArea = OverlayPlacement.area(displaySafe, boundsSpecified ? allowedBounds : displaySafe,
                host.isEmpty() ? 0 : dp(4), dp(WIDTH_DP), dp(HEIGHT_DP));
        if (placementArea == null) return false;
        insetX = placementArea.left; insetY = placementArea.top;
        availableX = placementArea.width() - dp(WIDTH_DP); availableY = placementArea.height() - dp(HEIGHT_DP);
        return true;
    }
    public Rect bounds() {
        return root == null ? new Rect() : new Rect(params.x, params.y, params.x + dp(WIDTH_DP), params.y + dp(HEIGHT_DP));
    }
    public void hide() {
        endInteraction();
        if (root != null) { manager.removeView(root); root = null; number = null; }
    }
    private void endInteraction() {
        if (!touchActive) return;
        touchActive = dragging = false;
        if (listener != null) listener.interaction(false);
    }
    private int dp(float value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
}
