package com.fullmetalsonic.shortsloop.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
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
import com.fullmetalsonic.shortsloop.core.PositionPolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;
import com.fullmetalsonic.shortsloop.core.PlaybackRestart;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;

public final class FloatingController {
    private static final int WIDTH_DP = 72, HEIGHT_DP = 56;
    public interface Listener { void cycle(); void close(); void interaction(boolean dragging); }
    private final Context context;
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
    }
    // Coordinates are absolute screen pixels, deliberately independent of text direction.
    @SuppressLint({"ClickableViewAccessibility", "RtlHardcoded"})
    public void show() {
        if (root != null) return;
        root = new FrameLayout(context);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.argb(128, 9, 20, 35)); background.setCornerRadius(dp(18));
        background.setStroke(dp(1), Color.argb(90, 116, 220, 255));
        root.setBackground(background);
        number = new TextView(context);
        number.setTextColor(Color.WHITE); number.setTextSize(21); number.setGravity(Gravity.CENTER);
        number.setSingleLine(true); number.setPadding(dp(2), 0, dp(2), 0);
        number.setAutoSizeTextTypeUniformWithConfiguration(12, 21, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        number.setId(R.id.floating_count); number.setFocusable(true); number.setClickable(true);
        root.addView(number, new FrameLayout.LayoutParams(dp(48), dp(HEIGHT_DP), Gravity.START | Gravity.TOP));
        Button close = new Button(context);
        close.setId(R.id.floating_close); close.setText(R.string.close_symbol);
        close.setTextColor(Color.WHITE); close.setTextSize(16); close.setPadding(0, 0, 0, 0);
        close.setMinWidth(0); close.setMinimumWidth(0); close.setMinHeight(0); close.setMinimumHeight(0);
        close.setBackgroundColor(Color.TRANSPARENT); close.setContentDescription(context.getString(R.string.close_description));
        // Compact exception: X is 24dp; count/drag remains at least 48dp.
        root.addView(close, new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.END | Gravity.TOP));
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
        if (number == null) return;
        String liveLabel = LiveSkipPolicy.floatingLabel(status, remainingSeconds);
        boolean timedStatus = "시간제 · 진행 정보 확인 중".equals(status) || "시간제 · 설정 시간 후 넘김".equals(status);
        String label = status.startsWith("안전정지 · ") ? "정지"
                : PlaybackRestart.WAITING.equals(status) ? "대기"
                : LongVideoPolicy.CHECKING.equals(status) || LongVideoPolicy.CONFIRMING.equals(status) ? "긴영상"
                : target == 0 && store.skipLong() && status.startsWith("반복 꺼짐 · 긴 영상") ? "조건"
                : liveLabel != null ? liveLabel
                : remainingSeconds >= 0 && timedStatus ? remainingSeconds + "초"
                : target == 0 && LiveSkipPolicy.zeroCountStatus(true, true).equals(status) ? "광·라"
                : target == 0 && LiveSkipPolicy.zeroCountStatus(false, true).equals(status) ? "라이브"
                : target == 0 && (LiveSkipPolicy.zeroCountStatus(true, false).equals(status) || "광고 넘김 확인 중".equals(status)) ? "광고"
                : status.equals("시간제 · 다음 영상 확인 중") ? "다음"
                : status.startsWith("화면 분석") ? (status.contains("수동 넘김") ? "?/" : "…/") + target
                : (status.startsWith("화면 추정") ? "~" : "") + current + "/" + target;
        if (!label.contentEquals(number.getText())) number.setText(label);
        String description = liveLabel != null ? status + (remainingSeconds > 0 && !LiveSkipPolicy.STATUS_CONFIRMING.equals(status)
                        ? " · " + remainingSeconds + "초 남음" : "") + ". 탭하면 반복 횟수 변경. 끌어서 이동. 라이브 설정은 인앱에서 변경."
                : remainingSeconds >= 0 && timedStatus ? "시간제 넘김 " + remainingSeconds + "초 남음 · " + status
                : context.getString(R.string.floating_description, current, target, status);
        if (!description.contentEquals(number.getContentDescription() == null ? "" : number.getContentDescription()))
            number.setContentDescription(description);
    }
    public void configurationChanged() {
        if (root == null) return;
        measureArea(); restorePosition(); manager.updateViewLayout(root, params);
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
