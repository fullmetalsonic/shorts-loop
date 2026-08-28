package com.fullmetalsonic.shortsloop.overlay;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

/** Actual Android text measurement/drawing, not a string-only or HTML substitute. Emulator only. */
public final class FloatingLayoutChecks {
    private int checks;
    private static final String[] LABELS = {"긴영상", "99/99", "~99/99", "라이브", "60초", "광·라",
            "조건", "대기", "정지", "다음", "…/99", "?/99", "1/1", "0/0", "광고", "긴영상"};

    public static int run(Context base, SettingsStore store) {
        return new FloatingLayoutChecks().verify(base, store);
    }

    private int verify(Context base, SettingsStore store) {
        for (float scale : new float[]{0.85f, 1f, 1.3f, 1.5f, 2f}) {
            for (boolean rtl : new boolean[]{false, true}) {
                Configuration config = new Configuration(base.getResources().getConfiguration());
                config.fontScale = scale; config.setLayoutDirection(rtl ? new Locale("ar") : Locale.KOREAN);
                Context context = base.createConfigurationContext(config);
                for (boolean bold : new boolean[]{false, true}) {
                    FloatingContent content = new FloatingContent(context);
                    content.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
                    TextView number = content.findViewById(R.id.floating_count);
                    TextView close = content.findViewById(R.id.floating_close);
                    if (bold) { number.setTypeface(Typeface.DEFAULT, Typeface.BOLD); close.setTypeface(Typeface.DEFAULT, Typeface.BOLD); }
                    for (String label : LABELS) {
                        number.setText(label); layout(content);
                        String where = label + " scale=" + scale + " rtl=" + rtl + " bold=" + bold;
                        fits(number, where); fits(close, "close " + where);
                        require(content.getWidth() == dp(context, 72) && content.getHeight() == dp(context, 56), "Outer size " + where);
                        require(close.getWidth() == dp(context, 24) && close.getHeight() == dp(context, 24), "Close size " + where);
                        require(number.getPaddingTop() >= close.getBottom(), "Text region below close " + where);
                        require(rtl ? close.getLeft() == 0 : close.getRight() == content.getWidth(), "Close at end " + where);
                    }
                    int[] clicks = {0, 0};
                    number.setOnClickListener(v -> clicks[0]++); close.setOnClickListener(v -> clicks[1]++);
                    // This detached fixture has no UI queue/attach info. Dispatch synchronously to
                    // test child hit routing; the actual controller gestures are checked on-device.
                    View.OnTouchListener synchronousClick = (view, event) -> {
                        if (event.getActionMasked() == MotionEvent.ACTION_UP) view.performClick();
                        return true;
                    };
                    number.setOnTouchListener(synchronousClick); close.setOnTouchListener(synchronousClick);
                    tap(content, content.getWidth() / 2f, content.getHeight() * .75f);
                    require(clicks[0] == 1 && clicks[1] == 0, "Count click isolated");
                    tap(content, close.getLeft() + close.getWidth() / 2f, close.getHeight() / 2f);
                    require(clicks[0] == 1 && clicks[1] == 1, "Close click isolated");
                    content.refreshMetrics(); layout(content); fits(number, "refresh " + scale);
                    require("긴영상".contentEquals(number.getText()), "Metric refresh preserves text");
                    tap(content, content.getWidth() / 2f, content.getHeight() * .75f);
                    require(clicks[0] == 2, "Metric refresh preserves listener");
                }
                if (!rtl && (scale == 1f || scale == 2f)) renderPreview(base, context, scale);
            }
        }
        // Reproduce the released defect using the old real layout and prove the regression is meaningful.
        Configuration config = new Configuration(base.getResources().getConfiguration()); config.fontScale = 1f;
        Context context = base.createConfigurationContext(config);
        TextView legacy = new TextView(context); legacy.setSingleLine(true); legacy.setPadding(dp(context, 2), 0, dp(context, 2), 0);
        legacy.setAutoSizeTextTypeUniformWithConfiguration(12, 21, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        legacy.setText("긴영상"); measure(legacy, dp(context, 48), dp(context, 56));
        require(legacy.getPaint().measureText("긴영상") > legacy.getWidth() - legacy.getPaddingLeft() - legacy.getPaddingRight(), "Old layout must reproduce clipping");
        // The real controller supplies the unchanged, meaningful label into the real content view.
        FloatingContent content = new FloatingContent(context);
        FloatingController controller = new FloatingController(context, store, null);
        try {
            java.lang.reflect.Field field = FloatingController.class.getDeclaredField("number"); field.setAccessible(true);
            TextView number = content.findViewById(R.id.floating_count); field.set(controller, number);
            controller.update(0, 1, LongVideoPolicy.CHECKING); layout(content);
            require("긴영상".contentEquals(number.getText()), "Controller label retained"); fits(number, "Controller long label");
            controller.update(99, 99, "재생 중"); layout(content);
            require("99/99".contentEquals(number.getText()), "Controller count retained"); fits(number, "Controller max count");
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
        return checks;
    }

    private void fits(TextView text, String where) {
        android.text.Layout layout = text.getLayout();
        require(layout != null && layout.getLineCount() == 1, "One line " + where);
        require(layout.getLineEnd(0) == text.length() && layout.getEllipsisCount(0) == 0, "Full string " + where);
        require(text.getScrollX() == 0, "No horizontal scrolling " + where);
        int width = text.getWidth() - text.getCompoundPaddingLeft() - text.getCompoundPaddingRight();
        int height = text.getHeight() - text.getCompoundPaddingTop() - text.getCompoundPaddingBottom();
        require(layout.getLineWidth(0) <= width + .5f && layout.getHeight() <= height, "Layout fits " + where + " width=" + width + " line=" + layout.getLineWidth(0) + " height=" + height + " layout=" + layout.getHeight());
        Rect ink = new Rect(); text.getPaint().getTextBounds(text.getText().toString(), 0, text.length(), ink);
        float x = text.getCompoundPaddingLeft() + layout.getLineLeft(0);
        int baseline = text.getBaseline();
        require(x + ink.left >= text.getCompoundPaddingLeft() - 1 && x + ink.right <= text.getWidth() - text.getCompoundPaddingRight() + 1,
                "Horizontal ink fits " + where);
        require(baseline + ink.top >= text.getCompoundPaddingTop() && baseline + ink.bottom <= text.getHeight() - text.getCompoundPaddingBottom(),
                "Vertical ink fits " + where);
    }

    private void renderPreview(Context output, Context context, float scale) {
        int width = dp(context, 240), height = dp(context, 290);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); canvas.drawColor(Color.rgb(55, 66, 78));
        Paint caption = new Paint(Paint.ANTI_ALIAS_FLAG); caption.setColor(Color.WHITE); caption.setTextSize(dp(context, 12));
        canvas.drawText("72 x 56 dp / font scale " + scale, dp(context, 12), dp(context, 20), caption);
        String[] labels = {"긴영상", "99/99", "~99/99", "라이브", "60초", "광·라"};
        for (int i = 0; i < labels.length; i++) {
            FloatingContent content = new FloatingContent(context);
            ((TextView)content.findViewById(R.id.floating_count)).setText(labels[i]); layout(content);
            canvas.save(); canvas.translate(dp(context, 24 + (i % 2) * 120), dp(context, 38 + (i / 2) * 80));
            content.draw(canvas); canvas.restore();
        }
        try (FileOutputStream stream = new FileOutputStream(new File(output.getCacheDir(), "floating-layout-" + scale + ".png"))) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) throw new AssertionError("Preview save failed");
        } catch (java.io.IOException error) { throw new AssertionError(error); }
        finally { bitmap.recycle(); }
    }

    private static void tap(View root, float x, float y) {
        long now = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent up = MotionEvent.obtain(now, now + 40, MotionEvent.ACTION_UP, x, y, 0);
        root.dispatchTouchEvent(down); root.dispatchTouchEvent(up); down.recycle(); up.recycle();
    }
    private static void layout(FloatingContent content) { measure(content, dp(content.getContext(), 72), dp(content.getContext(), 56)); }
    private static void measure(View view, int width, int height) {
        for (int i = 0; i < 3; i++) { view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)); view.layout(0, 0, width, height); }
    }
    private static int dp(Context context, int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
}
