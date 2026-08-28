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
import com.fullmetalsonic.shortsloop.data.SettingsStore;

/** Compact presentation only: the controller owns state, gestures and persistence. */
final class FloatingContent extends FrameLayout {
    static final int WIDTH_DP = 72, HEIGHT_DP = 56, CLOSE_DP = 24;

    FloatingContent(Context context) {
        this(context, "");
    }

    FloatingContent(Context context, String host) {
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

        if (!host.isEmpty()) {
            if (!SettingsStore.supportedHost(host)) throw new IllegalArgumentException("Unsupported host");
            TextView badge = new TextView(context);
            badge.setId(R.id.floating_host);
            boolean youtube = com.fullmetalsonic.shortsloop.data.SettingsStore.YOUTUBE_PACKAGE.equals(host);
            boolean tiktok = SettingsStore.TIKTOK_PACKAGE.equals(host);
            badge.setText(tiktok ? R.string.host_overlay_tiktok_short : youtube ? R.string.host_overlay_youtube_short : R.string.host_overlay_instagram_short);
            badge.setTextColor(tiktok ? Color.rgb(148, 242, 216) : youtube ? Color.rgb(116, 220, 255) : Color.rgb(224, 190, 255));
            badge.setGravity(Gravity.CENTER); badge.setMaxLines(1); badge.setHorizontallyScrolling(false);
            badge.setPadding(dp(4), 0, dp(2), 0);
            badge.setAutoSizeTextTypeUniformWithConfiguration(6, 12, 1, TypedValue.COMPLEX_UNIT_SP);
            // Decorative label: touches pass to the count/drag surface underneath.
            badge.setClickable(false); badge.setFocusable(false);
            badge.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(badge, new LayoutParams(dp(WIDTH_DP - CLOSE_DP), dp(CLOSE_DP), Gravity.START | Gravity.TOP));
        }

        Button close = new Button(context);
        close.setId(R.id.floating_close); close.setText(R.string.close_symbol);
        close.setTextColor(Color.WHITE); close.setPadding(0, 0, 0, 0);
        close.setMaxLines(1); close.setHorizontallyScrolling(false);
        close.setMinWidth(0); close.setMinimumWidth(0); close.setMinHeight(0); close.setMinimumHeight(0);
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setContentDescription(host.isEmpty() ? context.getString(R.string.close_description)
                : context.getString(R.string.host_overlay_close, hostName(context, host)));
        // Existing compact 24dp close target stays on top; the rest is count/drag space.
        addView(close, new LayoutParams(dp(CLOSE_DP), dp(CLOSE_DP), Gravity.END | Gravity.TOP));
        refreshMetrics();
    }

    static String hostName(Context context, String host) {
        if (SettingsStore.YOUTUBE_PACKAGE.equals(host)) return context.getString(R.string.host_overlay_youtube_name);
        if (SettingsStore.INSTAGRAM_PACKAGE.equals(host)) return context.getString(R.string.host_overlay_instagram_name);
        if (SettingsStore.TIKTOK_PACKAGE.equals(host)) return context.getString(R.string.host_overlay_tiktok_name);
        throw new IllegalArgumentException("Unsupported host");
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
