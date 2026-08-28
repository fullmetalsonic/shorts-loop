package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;

/** Home navigation only. Opening settings never selects or starts a host. */
@android.annotation.SuppressLint("ViewConstructor")
public final class AppEntryCard extends LinearLayout {
    public final Button open;
    public final TextView summary;
    private final String name;
    public AppEntryCard(Context context, int label, int id) {
        super(context); setOrientation(VERTICAL); name = context.getString(label);
        setPadding(UiTheme.dp(context, 14), UiTheme.dp(context, 10), UiTheme.dp(context, 14), UiTheme.dp(context, 12));
        setBackground(UiTheme.surface(context, UiTheme.SURFACE, 18, true));
        open = UiTheme.button(context, context.getString(R.string.host_open_settings, name)); open.setId(id);
        open.setTextSize(18); open.setMinHeight(UiTheme.dp(context, 56)); addView(open, new LayoutParams(-1, -2));
        summary = UiTheme.text(context, "", 13, UiTheme.MUTED, false); summary.setPadding(UiTheme.dp(context, 6), UiTheme.dp(context, 6), UiTheme.dp(context, 6), 0);
        addView(summary, new LayoutParams(-1, -2));
    }
    public void render(boolean installed, boolean selected, int count, boolean paused, boolean master) {
        String state = getContext().getString(!installed ? R.string.host_home_missing : !selected ? R.string.host_home_disabled
                : paused ? R.string.mw_host_paused : master ? R.string.host_home_enabled : R.string.host_home_master_off);
        summary.setText(getContext().getString(R.string.host_home_summary, state, count));
        open.setContentDescription(getContext().getString(R.string.host_open_description, name, summary.getText()));
    }
}
