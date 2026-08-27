package com.fullmetalsonic.shortsloop.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.fullmetalsonic.shortsloop.R;

/** Read-only battery exemption status and user-directed setup; never changes system policy. */
public final class BatterySetupPanel extends LinearLayout {
    private final TextView status;
    private final Button settingsButton;

    public BatterySetupPanel(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(0, UiTheme.dp(context, 16), 0, UiTheme.dp(context, 8));
        addView(UiTheme.text(context, context.getString(R.string.battery_heading), 16, UiTheme.TEXT, true));
        status = UiTheme.text(context, "", 14, UiTheme.WARNING, false);
        status.setId(R.id.battery_status);
        status.setPadding(0, UiTheme.dp(context, 6), 0, UiTheme.dp(context, 6));
        addView(status);
        addView(UiTheme.text(context, context.getString(R.string.battery_helper), 13, UiTheme.MUTED, false));
        settingsButton = UiTheme.button(context, context.getString(R.string.battery_open));
        settingsButton.setId(R.id.battery_settings);
        addView(settingsButton, new LinearLayout.LayoutParams(-1, -2));
        settingsButton.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle(R.string.battery_dialog_title)
                .setMessage(R.string.battery_dialog_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.open_settings, (dialog, which) -> openAppSettings())
                .show());
    }

    /** Refresh on Activity resume, including return from Settings. No polling or preference writes. */
    public void refresh() {
        Context context = getContext();
        try {
            PowerManager power = context.getSystemService(PowerManager.class);
            if (power == null) { unavailable(); return; }
            boolean exempt = power.isIgnoringBatteryOptimizations(context.getPackageName());
            status.setText(exempt ? R.string.battery_exempt : R.string.battery_not_exempt);
            status.setTextColor(exempt ? UiTheme.CYAN : UiTheme.WARNING);
            settingsButton.setText(exempt ? R.string.battery_review : R.string.battery_open);
        } catch (RuntimeException ignored) { unavailable(); }
    }

    private void unavailable() {
        status.setText(R.string.battery_unknown);
        status.setTextColor(UiTheme.WARNING);
        settingsButton.setText(R.string.battery_review);
    }

    private void openAppSettings() {
        Context context = getContext();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        try { context.startActivity(intent); }
        catch (RuntimeException ignored) {
            Toast.makeText(context, R.string.settings_unavailable, Toast.LENGTH_LONG).show();
        }
    }
}
