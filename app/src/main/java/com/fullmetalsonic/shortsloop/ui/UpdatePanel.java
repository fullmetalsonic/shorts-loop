package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.BuildConfig;
import com.fullmetalsonic.shortsloop.R;

/** An unobtrusive banner is bound separately; this card contains the infrequent update actions. */
public final class UpdatePanel extends LinearLayout {
    public final TextView status;
    public final Button check, action, cancel;
    public final ProgressBar progress;
    public final Switch automatic;
    public UpdatePanel(Context context) {
        super(context); setOrientation(VERTICAL);
        TextView installedVersion = UiTheme.text(context, context.getString(R.string.installed_version, BuildConfig.VERSION_NAME), 14, UiTheme.MUTED, false);
        installedVersion.setId(R.id.installed_version); addView(installedVersion);
        status = UiTheme.text(context, getContext().getString(R.string.ui_update_idle), 14, UiTheme.CYAN, false); status.setId(R.id.update_status); addView(status);
        progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal); progress.setMax(100); progress.setVisibility(GONE); addView(progress);
        check = UiTheme.button(context, getContext().getString(R.string.ui_update_check)); check.setId(R.id.update_check); addView(check);
        action = UiTheme.button(context, getContext().getString(R.string.ui_update_download)); action.setId(R.id.update_action); action.setVisibility(GONE); addView(action);
        cancel = UiTheme.button(context, getContext().getString(R.string.ui_update_cancel)); cancel.setId(R.id.update_cancel); cancel.setVisibility(GONE); addView(cancel);
        automatic = new Switch(context); automatic.setText(R.string.update_auto_title); automatic.setTextColor(UiTheme.TEXT); automatic.setTextSize(15);
        automatic.setSingleLine(false); automatic.setSwitchPadding(UiTheme.dp(context, 16));
        automatic.setId(R.id.update_automatic); automatic.setMinHeight(UiTheme.dp(context, 52)); addView(automatic);
        addView(UiTheme.text(context, getContext().getString(R.string.ui_update_helper), 13, UiTheme.MUTED, false));
    }
}
