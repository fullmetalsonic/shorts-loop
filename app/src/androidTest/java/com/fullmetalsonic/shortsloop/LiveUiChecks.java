package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.ui.LiveSkipPanel;

/** Synthetic native UI input checks. Does not grant permissions or start automation. */
public final class LiveUiChecks {
    private LiveUiChecks() {}
    public static int run(Activity activity, SettingsStore store) {
        int checks = 0;
        boolean installed;
        try { activity.getPackageManager().getApplicationInfo(SettingsStore.YOUTUBE_PACKAGE, 0); installed = true; }
        catch (android.content.pm.PackageManager.NameNotFoundException ignored) { installed = false; }
        Switch actual = activity.findViewById(R.id.skip_live_toggle);
        require(actual != null && actual.isEnabled() == (installed && store.youtubeEnabled()), "Live host availability"); checks++;
        require(!store.enabled(), "Live UI does not start execution"); checks++;
        LiveSkipPanel panel = new LiveSkipPanel(activity, 0, store::liveDelaySeconds);
        EditText input = panel.findViewById(R.id.live_delay_input);
        Button plus = panel.findViewById(R.id.live_delay_plus), minus = panel.findViewById(R.id.live_delay_minus);
        require(input.getText().toString().equals("0") && !minus.isEnabled(), "Zero initial immediate mode with lower bound"); checks++;
        input.setText("5"); plus.performClick();
        require(store.liveDelaySeconds() == 6 && input.getText().toString().equals("6"), "Typed value plus arrow"); checks++;
        minus.performClick(); require(store.liveDelaySeconds() == 5, "Minus arrow"); checks++;
        for (String invalid : new String[]{"61", "-1", "1.5", ""}) {
            input.setText(invalid);
            require(input.getText().toString().equals(invalid), "Invalid raw draft is not silently transformed"); checks++;
            require(!panel.commit() && store.liveDelaySeconds() == 5, "Invalid draft preserves stored delay"); checks++;
        }
        input.setText("0"); require(panel.commit() && store.liveDelaySeconds() == 0, "Zero saves as immediate, not disabled"); checks++;
        require(!minus.isEnabled(), "Zero has no negative step"); checks++;
        input.setText("60"); require(panel.commit() && !plus.isEnabled(), "Upper bound sixty"); checks++;
        input.setText("7"); panel.render(0);
        require(input.getText().toString().equals("7"), "Refresh preserves active input draft"); checks++;
        panel.setAvailable(false);
        require(!input.isEnabled() && !panel.commit() && store.liveDelaySeconds() == 60, "Unavailable host preserves settings"); checks++;
        require(!store.enabled(), "No execution side effect during input tests"); checks++;
        return checks;
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
