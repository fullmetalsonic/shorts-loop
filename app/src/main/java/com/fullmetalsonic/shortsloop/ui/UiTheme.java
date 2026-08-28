package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared native UI tokens: launcher palette with restrained surfaces. */
public final class UiTheme {
    public static final int BACKGROUND = Color.rgb(8, 15, 27);
    public static final int SURFACE = Color.rgb(17, 29, 46);
    public static final int BORDER = Color.rgb(42, 61, 83);
    public static final int TEXT = Color.rgb(241, 247, 255);
    public static final int MUTED = Color.rgb(174, 193, 215);
    public static final int CYAN = Color.rgb(116, 220, 255);
    public static final int PURPLE = Color.rgb(190, 164, 250);
    public static final int WARNING = Color.rgb(255, 206, 129);
    private UiTheme() {}
    public static int dp(Context c, float n) { return Math.round(n * c.getResources().getDisplayMetrics().density); }
    public static GradientDrawable surface(Context c, int color, int radius, boolean border) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c, radius));
        if (border) d.setStroke(dp(c, 1), BORDER); return d;
    }
    public static ColorStateList checkedColors() {
        return new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_checked}, new int[]{}}, new int[]{BORDER, CYAN, MUTED});
    }
    public static TextView text(Context c, String value, int sp, int color, boolean bold) {
        TextView v = new TextView(c); v.setText(value); v.setTextSize(sp); v.setTextColor(color);
        v.setLineSpacing(dp(c, 2), 1); if (bold) v.setTypeface(null, Typeface.BOLD); return v;
    }
    public static LinearLayout column(Context c) {
        LinearLayout layout = new LinearLayout(c); layout.setOrientation(LinearLayout.VERTICAL); return layout;
    }
    public static LinearLayout card(Context c, LinearLayout parent, String heading) {
        LinearLayout card = column(c); card.setPadding(dp(c, 18), dp(c, 16), dp(c, 18), dp(c, 16));
        card.setBackground(surface(c, SURFACE, 20, true));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(c, 14);
        parent.addView(card, p); TextView title = text(c, heading, 19, TEXT, true);
        LinearLayout.LayoutParams h = new LinearLayout.LayoutParams(-1, -2); h.bottomMargin = dp(c, 8); card.addView(title, h);
        return card;
    }
    public static Button button(Context c, String label) {
        Button v = new Button(c); v.setText(label); v.setAllCaps(false); v.setTextSize(15);
        v.setSingleLine(false); v.setMaxLines(Integer.MAX_VALUE);
        v.setTextColor(CYAN); v.setMinHeight(dp(c, 48)); v.setMinimumHeight(dp(c, 48));
        v.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(27, 46, 65))); return v;
    }
    public static void space(Context c, LinearLayout parent, int height) {
        parent.addView(new View(c), new LinearLayout.LayoutParams(1, dp(c, height)));
    }
}
