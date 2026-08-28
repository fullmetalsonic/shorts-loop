package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.widget.EditText;
import android.widget.Switch;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.ui.SettingsScreen;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/** Actual activity toggle and detached default/localization checks; no automation is started. */
final class DualModeUiChecks {
    static int run(Activity activity, SettingsStore store) {
        int checks = 0;
        boolean originalMode = store.dualMode();
        Switch toggle = activity.findViewById(R.id.mw_dual_mode);
        SettingsScreen screen;
        try { Field field = activity.getClass().getDeclaredField("screen"); field.setAccessible(true); screen = (SettingsScreen)field.get(activity); }
        catch (ReflectiveOperationException error) { throw new AssertionError("Activity screen", error); }
        String originalRoute = screen.currentHost();
        EditText youtube = activity.findViewById(R.id.count_input);
        EditText instagram = activity.findViewById(R.id.mw_instagram_count_input);
        String originalYoutube = youtube.getText().toString(), originalInstagram = instagram.getText().toString();
        Map<String, Object> before = unrelated(store);
        try {
            require(!store.enabled(), "Dual mode fixture starts with execution OFF"); checks++;
            require(toggle != null && toggle.isEnabled(), "Dual mode is a distinct available common switch"); checks++;
            require(toggle.isChecked() == originalMode, "Stored dual mode is rendered"); checks++;
            screen.back.performClick();
            store.dualMode(false); render(activity);
            require(!toggle.isChecked(), "Off mode is visible"); checks++;
            require(screen.status.getContentDescription().toString().equals(activity.getString(R.string.ui_execution_off)),
                    "Footer accessibility keeps full execution status"); checks++;
            require(screen.status.getText().toString().equals(activity.getString(screen.compactFooter ? R.string.off : R.string.ui_execution_off)),
                    "Actual activity footer uses compact status only for large text"); checks++;
            youtube.setText("12"); instagram.setText("4");
            toggle.performClick();
            require(toggle.isChecked() && store.dualMode(), "Tap enables and saves only dual mode"); checks++;
            require(!store.enabled(), "Mode ON does not start execution"); checks++;
            require(before.equals(unrelated(store)), "Mode ON preserves every other preference"); checks++;
            require("12".contentEquals(youtube.getText()) && "4".contentEquals(instagram.getText()), "Mode ON preserves both unsaved count drafts"); checks++;
            screen.instagramEntry.open.performClick();
            require(SettingsStore.INSTAGRAM_PACKAGE.equals(screen.currentHost()) && store.dualMode() && !store.enabled(), "Opening detail changes neither mode nor execution"); checks++;
            render(activity);
            require(SettingsStore.INSTAGRAM_PACKAGE.equals(screen.currentHost()) && "4".contentEquals(instagram.getText()), "Periodic render keeps route and draft"); checks++;
            activity.onBackPressed();
            require(screen.currentHost() == null && before.equals(unrelated(store)), "System Back returns home without saving drafts"); checks++;
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
            store.dualMode(originalMode);
            try {
                Field route = activity.getClass().getDeclaredField("settingsHost"); route.setAccessible(true); route.set(activity, originalRoute);
                render(activity);
            } catch (ReflectiveOperationException error) { throw new AssertionError("Dual fixture restore", error); }
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
