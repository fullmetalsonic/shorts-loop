package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.LocaleList;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.i18n.LanguagePolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.core.PlaybackRestart;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.i18n.StatusText;
import com.fullmetalsonic.shortsloop.ui.SettingsScreen;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/** Detached native fixtures only: no system-locale changes, network, permissions or gestures. */
final class LocalizationChecks {
    private int checks;
    static int run(Activity activity, SettingsStore store) {
        return new LocalizationChecks().verify(activity, store);
    }

    private int verify(Activity activity, SettingsStore store) {
        Map<String, ?> original = new HashMap<>(store.preferences.getAll());
        Map<String, ?> updates = new HashMap<>(activity.getSharedPreferences("updates", 0).getAll());
        String selected = AppLocale.language(activity);
        require(selected.equals("ko") || selected.equals("en"), "System language resolves to a supported language");
        require(selected.equals(activity.getResources().getConfiguration().getLocales().get(0).getLanguage()),
                "MainActivity uses the system-first localized context");
        require(selected.equals(AppLocale.wrap(activity).getResources().getConfiguration().getLocales().get(0).getLanguage()),
                "Wrapped context matches the selected system language");

        // Do not change Locale.setDefault or device settings. These lists represent system order.
        for (String tags : new String[]{"ko-KR", "en-US", "ja-JP", "ja-JP,ko-KR", "en-US,ko-KR", "ko-KR,en-US"}) {
            LocaleList languages = LocaleList.forLanguageTags(tags);
            String expected = languages.get(0).getLanguage().equals("ko") ? "ko" : "en";
            String policy = LanguagePolicy.select(languages.get(0).getLanguage());
            require(expected.equals(policy), "Only the first language selects the UI: " + tags);
            Context localized = AppLocale.forLanguage(activity, languages.get(0).getLanguage());
            require(expected.equals(localized.getResources().getConfiguration().getLocales().get(0).getLanguage()),
                    "Explicit first-language fixture resolves correctly: " + tags);
            require(localized.getString(R.string.app_name).equals(expected.equals("ko") ? "쇼츠 자동 넘김" : "ShortsLoop"),
                    "Localized app name: " + tags);
        }

        Context english = AppLocale.forLanguage(activity, "en");
        Context korean = AppLocale.forLanguage(activity, "ko");
        require(!hangul(english.getString(R.string.ui_help_body)), "English full help contains no Korean");
        require(hangul(korean.getString(R.string.ui_help_body)), "Korean full help remains Korean");
        require(english.getString(R.string.ui_visual_toggle).contains("Experimental"),
                "English experimental disclosure is explicit");
        require(korean.getString(R.string.ui_visual_toggle).contains("시험"),
                "Korean experimental disclosure is preserved");
        require(english.getString(R.string.app_version, "0.2.9").equals("ShortsLoop 0.2.9"),
                "English version is neutral");
        require(korean.getString(R.string.app_version, "0.2.9").equals("ShortsLoop 0.2.9"),
                "Korean version is neutral");

        // Every bundled English string must avoid Korean leakage, including non-UI status resources.
        try {
            for (Field field : R.string.class.getFields()) {
                if (field.getType() != int.class) continue;
                require(!hangul(english.getString(field.getInt(null))), "English resource leakage: " + field.getName());
            }
        } catch (IllegalAccessException error) { throw new AssertionError(error); }
        for (String status : new String[]{"off", "blocked:error.advance", "ads.confirming", "playback.counting",
                PlaybackRestart.WAITING, PlaybackRestart.COUNTING, LiveSkipPolicy.STATUS_IMMEDIATE,
                LiveSkipPolicy.STATUS_DELAYED, LiveSkipPolicy.STATUS_CONFIRMING,
                LongVideoPolicy.CHECKING, LongVideoPolicy.CONFIRMING}) {
            String en = StatusText.text(english, status), ko = StatusText.text(korean, status);
            require(!en.isEmpty() && !en.equals(status) && !hangul(en), "English status is translated: " + status);
            require(!ko.isEmpty() && !ko.equals(status) && hangul(ko), "Korean status is translated: " + status);
        }
        for (int flags = 0; flags < 8; flags++) {
            String code = LongVideoPolicy.zeroCountStatus((flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0);
            require(!hangul(StatusText.text(english, code)) && !StatusText.text(english, code).equals(code),
                    "Independent-option status is localized: " + flags);
        }

        for (String language : new String[]{"ko", "en"}) {
            for (float scale : new float[]{1f, 1.5f, 2f}) {
                Context context = fixtureContext(activity, language, scale, 320, 800);
                int[] commits = {0};
                SettingsScreen screen = new SettingsScreen(context, 2, value -> commits[0]++,
                        10, value -> commits[0]++, 0, value -> commits[0]++,
                        3600, value -> commits[0]++);
                measure(screen.root, dp(context, 320), dp(context, 800));
                String where = language + " font=" + scale;
                require(commits[0] == 0, "Constructing localized UI never saves settings: " + where);
                require(!screen.execution.isChecked(), "Localized UI never starts execution: " + where);
                require(screen.youtube.getText().toString().equals(context.getString(R.string.ui_youtube)),
                        "Localized host label: " + where);
                require(screen.execution.getContentDescription().toString().equals(context.getString(R.string.ui_execution_description)),
                        "Localized execution accessibility label: " + where);
                require(screen.count.findViewById(R.id.count_input).getContentDescription().toString()
                        .equals(context.getString(R.string.ui_count_description)), "Localized input accessibility label: " + where);
                inspect(screen.root, language.equals("en"), where);
                render(activity, screen.root, "localization-" + language + "-" + scale + "-main.png");

                Button help = screen.root.findViewById(R.id.help_toggle);
                require(help.performClick(), "Help expands: " + where);
                require(help.getText().toString().equals(context.getString(R.string.ui_help_hide)), "Localized help toggle: " + where);
                measure(screen.root, dp(context, 320), dp(context, 800));
                // The UI prepends the shared-window and per-host X behavior to the
                // unchanged full guide. Match all three sections, not the old suffix alone.
                String expectedHelp = context.getString(R.string.mw_mode_help) + "\n\n"
                        + context.getString(R.string.mw_host_floating_help) + "\n\n"
                        + context.getString(R.string.ui_help_body);
                TextView body = findText(screen.root, expectedHelp);
                require(body != null && body.getVisibility() == View.VISIBLE && body.getLineCount() > 1,
                        "Full translated help is visible and wraps: " + where);
                inspect(screen.root, language.equals("en"), where + " help");
                ScrollView scroll = findScroll(screen.root);
                if (scroll == null || body == null) throw new AssertionError("Help scroll fixture is missing");
                Rect area = new Rect(); body.getDrawingRect(area); scroll.offsetDescendantRectToMyCoords(body, area);
                scroll.scrollTo(0, area.top);
                render(activity, screen.root, "localization-" + language + "-" + scale + "-help.png");
                require(help.performClick() && body.getVisibility() == View.GONE, "Help collapses without losing state: " + where);

                EditText count = screen.root.findViewById(R.id.count_input); count.setText("-1");
                require(!screen.count.commit() && count.getError().toString().equals(context.getString(R.string.ui_count_error)),
                        "Localized count validation preserves invalid draft: " + where);
                EditText timer = screen.root.findViewById(R.id.fallback_seconds_input); timer.setText("0");
                require(!screen.seconds.commit() && timer.getError().toString().equals(context.getString(R.string.ui_timer_error)),
                        "Localized timer validation: " + where);
                EditText live = screen.root.findViewById(R.id.live_delay_input); live.setText("61");
                require(!screen.live.commit() && live.getError().toString().equals(context.getString(R.string.live_delay_error)),
                        "Localized live validation: " + where);
                EditText length = screen.root.findViewById(R.id.long_video_seconds_input); length.setText("0");
                require(!screen.longVideo.commit() && length.getError().toString().equals(context.getString(R.string.long_video_seconds_error)),
                        "Localized length validation: " + where);
                require(commits[0] == 0, "Invalid localized input never saves: " + where);
                count.setText("3");
                require(screen.count.commit() && commits[0] == 1 && count.getText().toString().equals("3"),
                        "Localized count still commits valid numeric input: " + where);
            }
        }
        require(original.equals(store.preferences.getAll()), "Every playback preference survives localization fixtures");
        require(updates.equals(activity.getSharedPreferences("updates", 0).getAll()), "Every update preference survives localization fixtures");
        require(!store.enabled(), "No automatic execution after localization checks");
        return checks;
    }

    /** Fix viewport width without changing device display/font settings. */
    @SuppressWarnings("deprecation")
    private static Context fixtureContext(Context base, String language, float scale, int width, int height) {
        Context localized = AppLocale.forLanguage(base, language);
        Configuration configuration = new Configuration(localized.getResources().getConfiguration());
        configuration.fontScale = scale; configuration.screenWidthDp = width; configuration.screenHeightDp = height;
        Context configured = localized.createConfigurationContext(configuration);
        DisplayMetrics metrics = new DisplayMetrics(); metrics.setTo(configured.getResources().getDisplayMetrics());
        metrics.widthPixels = Math.round(width * metrics.density); metrics.heightPixels = Math.round(height * metrics.density);
        Resources resources = new Resources(configured.getAssets(), metrics, configuration);
        Context fixed = new ContextWrapper(configured) {
            @Override public Resources getResources() { return resources; }
        };
        return new ContextThemeWrapper(fixed, R.style.AppTheme);
    }

    private void inspect(View view, boolean english, String where) {
        if (view.getVisibility() != View.VISIBLE) return;
        if (view.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup)view.getParent();
            require(view.getLeft() >= 0 && view.getRight() <= parent.getWidth() + 1,
                    "Every container stays within viewport: " + where + " " + view.getClass().getSimpleName());
        }
        if (view.getContentDescription() != null && english)
            require(!hangul(view.getContentDescription().toString()), "English accessibility leakage: " + where);
        if (view instanceof TextView && ((TextView)view).length() > 0) {
            TextView text = (TextView)view;
            if (english) require(!hangul(text.getText().toString()), "English rendered text leakage: " + where);
            Layout layout = text.getLayout();
            require(layout != null && layout.getLineCount() > 0, "Text has native layout: " + where);
            int availableWidth = text.getWidth() - text.getCompoundPaddingLeft() - text.getCompoundPaddingRight();
            int availableHeight = text.getHeight() - text.getCompoundPaddingTop() - text.getCompoundPaddingBottom();
            require(layout.getLineEnd(layout.getLineCount() - 1) == text.length(), "Complete text remains laid out: " + where + " " + text.getText());
            for (int line = 0; line < layout.getLineCount(); line++) {
                require(layout.getEllipsisCount(line) == 0, "No ellipsis: " + where);
                // getLineWidth includes the trailing wrap-space, which has no visible glyph.
                require(layout.getLineMax(line) <= availableWidth + 1f, "No clipped line: " + where
                        + " visible=" + layout.getLineMax(line) + " available=" + availableWidth + " " + text.getText());
            }
            require(layout.getHeight() <= availableHeight + 1, "Text height fits: " + where + " " + text.getText());
            if (view.getParent() instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup)view.getParent();
                require(view.getLeft() >= 0 && view.getRight() <= parent.getWidth() + 1, "Text stays inside its parent: " + where);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) inspect(group.getChildAt(i), english, where);
        }
    }
    private static boolean hangul(String value) { return value.matches("(?s).*[\\uAC00-\\uD7A3].*"); }
    private static TextView findText(View view, String value) {
        if (view instanceof TextView && value.contentEquals(((TextView)view).getText())) return (TextView)view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView result = findText(group.getChildAt(i), value); if (result != null) return result;
            }
        }
        return null;
    }
    private static ScrollView findScroll(View view) {
        if (view instanceof ScrollView) return (ScrollView)view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) {
                ScrollView result = findScroll(group.getChildAt(i)); if (result != null) return result;
            }
        }
        return null;
    }
    private static void measure(View view, int width, int height) {
        for (int i = 0; i < 3; i++) {
            view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            view.layout(0, 0, width, height);
        }
    }
    private static void render(Context output, View view, String filename) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        try (FileOutputStream stream = new FileOutputStream(new File(output.getCacheDir(), filename))) {
            view.draw(new Canvas(bitmap));
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) throw new AssertionError("Localization preview save failed");
        } catch (java.io.IOException error) { throw new AssertionError(error); }
        finally { bitmap.recycle(); }
    }
    private static int dp(Context context, int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
}
