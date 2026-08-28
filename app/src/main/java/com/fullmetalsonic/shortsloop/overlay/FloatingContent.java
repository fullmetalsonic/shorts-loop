package com.fullmetalsonic.shortsloop.overlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;

/** Compact presentation only: the controller owns state, gestures and persistence. */
final class FloatingContent extends FrameLayout {
    static final int WIDTH_DP = 72, HEIGHT_DP = 56, CLOSE_DP = 24;

    FloatingContent(Context context) {
        super(context);
        TextView number = new TextView(context);
        number.setId(R.id.floating_count);
        number.setTextColor(Color.WHITE); number.setGravity(Gravity.CENTER);
        // A horizontally scrolling single line makes Android autosize against VERY_WIDE,
        // not our actual width. Keep one line but explicitly disable horizontal scrolling.
        number.setMaxLines(1); number.setHorizontallyScrolling(false);
        // Use the full width BELOW the close button, preserving the 72x56dp outer window.
        number.setFocusable(true); number.setClickable(true);
        addView(number, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        Button close = new Button(context);
        close.setId(R.id.floating_close); close.setText(R.string.close_symbol);
        close.setTextColor(Color.WHITE); close.setPadding(0, 0, 0, 0);
        close.setMaxLines(1); close.setHorizontallyScrolling(false);
        close.setMinWidth(0); close.setMinimumWidth(0); close.setMinHeight(0); close.setMinimumHeight(0);
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setContentDescription(context.getString(R.string.close_description));
        // Existing compact 24dp close target stays on top; the rest is count/drag space.
        addView(close, new LayoutParams(dp(CLOSE_DP), dp(CLOSE_DP), Gravity.END | Gravity.TOP));
        refreshMetrics();
    }

    /** Recalculate dp and sp after font/display settings change, without losing listeners/state. */
    void refreshMetrics() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.argb(128, 9, 20, 35)); background.setCornerRadius(dp(18));
        background.setStroke(dp(1), Color.argb(90, 116, 220, 255));
        setBackground(background);
        TextView number = findViewById(R.id.floating_count);
        number.setPadding(dp(6), dp(CLOSE_DP), dp(6), dp(4));
        number.setAutoSizeTextTypeUniformWithConfiguration(8, 21, 1, TypedValue.COMPLEX_UNIT_SP);
        TextView close = findViewById(R.id.floating_close);
        close.setLayoutParams(new LayoutParams(dp(CLOSE_DP), dp(CLOSE_DP), Gravity.END | Gravity.TOP));
        close.setAutoSizeTextTypeUniformWithConfiguration(8, 16, 1, TypedValue.COMPLEX_UNIT_SP);
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
