package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.data.SettingsStore;

/** One retained ad-only editor tree per host; no photo or clockless capability is implied. */
public final class AdRulesPanel {
    public final LinearLayout root, card;
    public final AdDelayEditor delay;
    public final Switch toggle;
    public final TextView support;
    public final boolean automationSupported;
    public AdDelayEditor.Listener onDelay = value -> {};

    public AdRulesPanel(Context context, String host, int initialDelay) {
        if (!SettingsStore.supportedHost(host)) throw new IllegalArgumentException("Unsupported host");
        boolean youtube = SettingsStore.YOUTUBE_PACKAGE.equals(host), tiktok = SettingsStore.TIKTOK_PACKAGE.equals(host);
        // YouTube needs a verified Shorts-ad structure before its prepared option can run.
        automationSupported = !youtube;
        root = UiTheme.column(context);
        card = UiTheme.card(context, root, context.getString(R.string.ui_ads_title));
        if (automationSupported) card.addView(UiTheme.text(context, context.getString(R.string.long_video_independent), 14, UiTheme.CYAN, true));
        toggle = SettingsScreen.toggle(context, context.getString(R.string.ui_ads_toggle),
                youtube ? R.id.yt_skip_ads : tiktok ? R.id.tt_skip_ads : R.id.skip_ads_toggle); card.addView(toggle);
        toggle.setEnabled(automationSupported);
        support = UiTheme.text(context, "", 13, UiTheme.CYAN, false);
        support.setId(youtube ? R.id.yt_ad_support : tiktok ? R.id.tt_ad_support : R.id.ad_support); card.addView(support);
        if (youtube) support.setText(R.string.youtube_ads_unavailable);
        card.addView(UiTheme.text(context, context.getString(youtube ? R.string.youtube_ads_helper : R.string.ui_ads_helper), 13, UiTheme.MUTED, false));
        delay = new AdDelayEditor(context, initialDelay, value -> onDelay.changed(value)); card.addView(delay);
        card.addView(UiTheme.text(context, context.getString(youtube ? R.string.youtube_ads_only_help : R.string.ui_ads_only_help), 13, UiTheme.TEXT, false));
        if (youtube || tiktok) {
            int[] before = {R.id.ad_delay_input, R.id.ad_delay_minus, R.id.ad_delay_plus, R.id.ad_delay_apply};
            int[] after = youtube ? new int[]{R.id.yt_ad_delay_input, R.id.yt_ad_delay_minus, R.id.yt_ad_delay_plus, R.id.yt_ad_delay_apply}
                    : new int[]{R.id.tt_ad_delay_input, R.id.tt_ad_delay_minus, R.id.tt_ad_delay_plus, R.id.tt_ad_delay_apply};
            for (int i = 0; i < before.length; i++) delay.findViewById(before[i]).setId(after[i]);
        }
    }
}
