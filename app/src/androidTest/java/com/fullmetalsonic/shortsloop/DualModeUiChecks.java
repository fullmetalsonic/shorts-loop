package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.ui.SettingsScreen;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/** Actual activity toggle and detached default/localization checks; no automation is started. */
final class DualModeUiChecks {
    static int run(Activity activity, SettingsStore store) {
        int checks = 0;
        boolean originalMode = store.dualMode();
        Switch toggle = activity.findViewById(R.id.mw_dual_mode);
        RadioGroup tabs = activity.findViewById(R.id.mw_host_tabs);
        int originalTab = tabs.getCheckedRadioButtonId();
        EditText youtube = activity.findViewById(R.id.count_input);
        EditText instagram = activity.findViewById(R.id.mw_instagram_count_input);
        String originalYoutube = youtube.getText().toString(), originalInstagram = instagram.getText().toString();
        Map<String, Object> before = unrelated(store);
        try {
            require(!store.enabled(), "Dual mode fixture starts with execution OFF"); checks++;
            require(toggle != null && toggle.isEnabled(), "Dual mode is a distinct available common switch"); checks++;
            require(toggle.isChecked() == originalMode, "Stored dual mode is rendered"); checks++;
            store.dualMode(false); render(activity);
            require(!toggle.isChecked(), "Off mode is visible"); checks++;
            youtube.setText("12"); instagram.setText("4");
            toggle.performClick();
            require(toggle.isChecked() && store.dualMode(), "Tap enables and saves only dual mode"); checks++;
            require(!store.enabled(), "Mode ON does not start execution"); checks++;
            require(before.equals(unrelated(store)), "Mode ON preserves every other preference"); checks++;
            require("12".contentEquals(youtube.getText()) && "4".contentEquals(instagram.getText()), "Mode ON preserves both unsaved count drafts"); checks++;
            tabs.check(R.id.mw_tab_instagram);
            require(store.dualMode() && !store.enabled(), "Selecting an editor tab changes neither mode nor execution"); checks++;
            toggle.performClick();
            require(!toggle.isChecked() && !store.dualMode(), "Tap disables dual mode"); checks++;
            require(before.equals(unrelated(store)) && !store.enabled(), "Mode OFF preserves settings and execution"); checks++;
            require("12".contentEquals(youtube.getText()) && "4".contentEquals(instagram.getText()), "Mode OFF preserves both drafts"); checks++;
            for (String language : new String[]{"ko", "en"}) {
                android.content.Context localized = AppLocale.forLanguage(activity, language);
                SettingsScreen fresh = new SettingsScreen(localized, 2, n -> {}, 10, n -> {}, 0, n -> {}, 60, n -> {});
                require(!fresh.dualMode.isChecked(), "Fresh detached control defaults OFF"); checks++;
                require(fresh.dualMode.getText().toString().equals(localized.getString(R.string.mw_dual_toggle)), "Dual label follows locale"); checks++;
            }
            return checks;
        } catch (ReflectiveOperationException error) { throw new AssertionError("Dual mode binding", error); }
        finally {
            youtube.setText(originalYoutube); instagram.setText(originalInstagram);
            store.dualMode(originalMode); tabs.check(originalTab);
            try { render(activity); } catch (ReflectiveOperationException error) { throw new AssertionError("Dual fixture restore", error); }
        }
    }
    private static Map<String, Object> unrelated(SettingsStore store) {
        Map<String, Object> values = new HashMap<>(store.preferences.getAll()); values.remove("dual_mode"); return values;
    }
    private static void render(Activity activity) throws ReflectiveOperationException {
        Method method = activity.getClass().getDeclaredMethod("render"); method.setAccessible(true); method.invoke(activity);
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
