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
    private final SettingsStore store;
    private final Listener listener;
    private final WindowManager manager;
    private FrameLayout root;
    private TextView number;
    private WindowManager.LayoutParams params;
    private int availableX, availableY, insetX, insetY;
    private int originX, originY;
    private float downX, downY;
    private boolean dragging;

    public FloatingController(Context context, SettingsStore store, Listener listener) {
        this.context = context; this.store = store; this.listener = listener;
        manager = context.getSystemService(WindowManager.class);
        localized = AppLocale.wrap(context);
    }
    // Coordinates are absolute screen pixels, deliberately independent of text direction.
    @SuppressLint({"ClickableViewAccessibility", "RtlHardcoded"})
    public void show() {
        if (root != null) return;
        root = new FloatingContent(localized);
        number = root.findViewById(R.id.floating_count);
        Button close = root.findViewById(R.id.floating_close);
        close.setOnClickListener(view -> listener.close());
        number.setOnClickListener(view -> listener.cycle());
        number.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX(); downY = event.getRawY(); originX = params.x; originY = params.y;
                    dragging = false; listener.interaction(true); return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - downX, dy = event.getRawY() - downY;
                    if (Math.hypot(dx, dy) > ViewConfiguration.get(context).getScaledTouchSlop()) dragging = true;
                    if (dragging) {
                        params.x = insetX + PositionPolicy.clamp(originX + Math.round(dx) - insetX, availableX);
                        params.y = insetY + PositionPolicy.clamp(originY + Math.round(dy) - insetY, availableY);
                        manager.updateViewLayout(root, params);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (dragging) savePosition(); else view.performClick();
                    listener.interaction(false); return true;
                case MotionEvent.ACTION_CANCEL:
                    if (dragging) savePosition();
                    listener.interaction(false); return true;
                default: return true;
            }
        });
        params = new WindowManager.LayoutParams(dp(WIDTH_DP), dp(HEIGHT_DP), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.alpha = 0.8f;
        params.setTitle("ShortsLoop");
        measureArea(); restorePosition();
        try { manager.addView(root, params); }
        catch (RuntimeException failure) { root = null; number = null; throw failure; }
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
        if (!description.contentEquals(number.getContentDescription() == null ? "" : number.getContentDescription()))
            number.setContentDescription(description);
    }
    public void configurationChanged() {
        localized = AppLocale.wrap(context);
        if (root == null) return;
        // Rebuild only presentation: the saved position and execution settings stay unchanged.
        listener.interaction(false); dragging = false;
        hide(); show();
        update(lastCurrent, lastTarget, lastStatus, lastRemaining);
    }
    private void restorePosition() {
        params.x = insetX + PositionPolicy.restore(store.x(), availableX);
        params.y = insetY + PositionPolicy.restore(store.y(), availableY);
    }
    private void savePosition() {
        store.position(PositionPolicy.save(params.x - insetX, availableX), PositionPolicy.save(params.y - insetY, availableY));
    }
    @SuppressWarnings("deprecation")
    private void measureArea() {
        int width, height, right = 0, bottom = dp(24);
        insetX = 0; insetY = dp(28);
        if (Build.VERSION.SDK_INT >= 30) {
            android.view.WindowMetrics metrics = manager.getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds(); width = bounds.width(); height = bounds.height();
            android.graphics.Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            insetX = insets.left; insetY = insets.top; right = insets.right; bottom = insets.bottom;
        } else {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            manager.getDefaultDisplay().getRealMetrics(metrics); width = metrics.widthPixels; height = metrics.heightPixels;
        }
        availableX = Math.max(0, width - insetX - right - dp(WIDTH_DP));
        availableY = Math.max(0, height - insetY - bottom - dp(HEIGHT_DP));
    }
    public Rect bounds() {
        return root == null ? new Rect() : new Rect(params.x, params.y, params.x + dp(WIDTH_DP), params.y + dp(HEIGHT_DP));
    }
    public void hide() {
        if (root != null) { manager.removeView(root); root = null; number = null; }
    }
    private int dp(float value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
}
