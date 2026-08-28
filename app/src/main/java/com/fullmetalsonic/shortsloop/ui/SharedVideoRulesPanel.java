package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.data.SettingsStore;

/** Separate retained IG/TT rule trees; each binds to its own host-scoped store. */
public final class SharedVideoRulesPanel {
    public final LinearLayout root;
    public final SecondsEditor seconds;
    public final AdDelayEditor adDelay;
    public final AdRulesPanel ads;
    public final PhotoReelPanel photos;
    public final Switch timedFallback, skipAds;
    public final TextView timedSupport, adSupport;
    public final LinearLayout timedCard, adsCard, photoCard;
    public SecondsEditor.Listener onSeconds = value -> {};
    public AdDelayEditor.Listener onAdDelay = value -> {};

    public SharedVideoRulesPanel(Context context, boolean tiktok, int initialSeconds, int initialAdDelay) {
        root = UiTheme.column(context);
        ads = new AdRulesPanel(context, tiktok ? SettingsStore.TIKTOK_PACKAGE : SettingsStore.INSTAGRAM_PACKAGE, initialAdDelay);
        root.addView(ads.root); adsCard = ads.card; skipAds = ads.toggle; adSupport = ads.support; adDelay = ads.delay;
        ads.onDelay = value -> onAdDelay.changed(value);
        timedCard = UiTheme.card(context, root, context.getString(R.string.ui_timer_title));
        timedFallback = SettingsScreen.toggle(context, context.getString(R.string.ui_timer_toggle), tiktok ? R.id.tt_timed_fallback : R.id.timed_fallback_toggle); timedCard.addView(timedFallback);
        timedSupport = UiTheme.text(context, "", 13, UiTheme.CYAN, false); timedSupport.setId(tiktok ? R.id.tt_timed_support : R.id.timed_support); timedCard.addView(timedSupport);
        timedCard.addView(UiTheme.text(context, context.getString(R.string.ui_timer_helper), 13, UiTheme.MUTED, false));
        seconds = new SecondsEditor(context, initialSeconds, value -> onSeconds.changed(value)); timedCard.addView(seconds);
        timedCard.addView(UiTheme.text(context, context.getString(R.string.ui_timer_zero_help), 13, UiTheme.MUTED, false));
        photoCard = UiTheme.card(context, root, context.getString(R.string.photo_title));
        photos = new PhotoReelPanel(context, tiktok); photoCard.addView(photos);
        if (tiktok) {
            int[] before = {R.id.fallback_seconds_input,R.id.fallback_seconds_minus,R.id.fallback_seconds_plus,R.id.fallback_seconds_apply};
            int[] after = {R.id.tt_fallback_seconds_input,R.id.tt_fallback_seconds_minus,R.id.tt_fallback_seconds_plus,R.id.tt_fallback_seconds_apply};
            for (int i=0;i<before.length;i++) root.findViewById(before[i]).setId(after[i]);
        }
    }
}
