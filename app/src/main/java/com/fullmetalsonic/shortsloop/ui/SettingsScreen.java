package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;

/** Layout only: settings and service state are bound by MainActivity. */
public final class SettingsScreen {
    public final LinearLayout root;
    public final CountEditor count;
    public final SecondsEditor seconds;
    public final LiveSkipPanel live;
    public final LongVideoPanel longVideo;
    public final PhotoReelPanel photos;
    public final TextView applied, status, permissionStatus, timedSupport, adSupport, visualSupport;
    public final CheckBox youtube, instagram;
    public final Switch floating, execution, skipAds, visualAssist, timedFallback;
    public final RadioGroup tapModes;
    public final RadioButton rotary, quick;
    public final LinearLayout floatingDetails;
    public final Button accessButton, overlayButton, tileButton;
    public final BatterySetupPanel battery;
    public final UpdatePanel updates;
    public final Button setupJump, updateBanner;
    private final ScrollView scroll;
    private final LinearLayout appsCard, setupCard, updateCard;
    public SettingsScreen(Context c, int initial, CountEditor.Listener countListener, int initialSeconds,
            SecondsEditor.Listener secondsListener, int initialLiveDelay, LiveSkipPanel.Listener liveListener,
            int initialLongSeconds, LongVideoPanel.Listener longListener) {
        root = UiTheme.column(c); root.setBackgroundColor(UiTheme.BACKGROUND);
        scroll = new ScrollView(c); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        FrameLayout holder = new ContentFrame(c); LinearLayout content = UiTheme.column(c);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        content.setPadding(0, UiTheme.dp(c, 20), 0, UiTheme.dp(c, 12)); content.setFocusableInTouchMode(true);
        // This container is a focus sink after Done, not an actionable highlighted control.
        content.setDefaultFocusHighlightEnabled(false);
        holder.addView(content, cp); scroll.addView(holder); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout header = new LinearLayout(c); header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(c); icon.setImageResource(R.mipmap.ic_launcher); icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        header.addView(icon, new LinearLayout.LayoutParams(UiTheme.dp(c, 52), UiTheme.dp(c, 52)));
        LinearLayout names = UiTheme.column(c); names.setPadding(UiTheme.dp(c, 12), 0, 0, 0);
        names.addView(UiTheme.text(c, c.getString(R.string.app_name), 24, UiTheme.TEXT, true));
        names.addView(UiTheme.text(c, c.getString(R.string.ui_header_summary), 13, UiTheme.MUTED, false));
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1)); content.addView(header); UiTheme.space(c, content, 20);
        setupJump = UiTheme.button(c, c.getString(R.string.setup_needed_banner)); setupJump.setId(R.id.setup_jump); content.addView(setupJump);
        updateBanner = UiTheme.button(c, c.getString(R.string.ui_update_view)); updateBanner.setId(R.id.update_banner); updateBanner.setVisibility(View.GONE); content.addView(updateBanner);
        LinearLayout repeat = UiTheme.card(c, content, c.getString(R.string.ui_repeat_title));
        repeat.addView(UiTheme.text(c, c.getString(R.string.ui_repeat_intro), 14, UiTheme.MUTED, false)); UiTheme.space(c, repeat, 12);
        count = new CountEditor(c, initial, countListener); repeat.addView(count);
        applied = UiTheme.text(c, "", 14, UiTheme.CYAN, false); applied.setPadding(0, UiTheme.dp(c, 8), 0, 0); repeat.addView(applied);
        LinearLayout longCard = UiTheme.card(c, content, c.getString(R.string.long_video_card_title));
        longVideo = new LongVideoPanel(c, initialLongSeconds, longListener); longCard.addView(longVideo);
        LinearLayout timedCard = UiTheme.card(c, content, c.getString(R.string.ui_timer_title));
        timedFallback = toggle(c, c.getString(R.string.ui_timer_toggle), R.id.timed_fallback_toggle); timedCard.addView(timedFallback);
        timedSupport = UiTheme.text(c, "", 13, UiTheme.CYAN, false); timedSupport.setId(R.id.timed_support); timedCard.addView(timedSupport);
        timedCard.addView(UiTheme.text(c, c.getString(R.string.ui_timer_helper), 13, UiTheme.MUTED, false));
        UiTheme.space(c, timedCard, 8);
        seconds = new SecondsEditor(c, initialSeconds, secondsListener); timedCard.addView(seconds);
        timedCard.addView(UiTheme.text(c, c.getString(R.string.ui_timer_zero_help), 13, UiTheme.MUTED, false));
        LinearLayout photoCard = UiTheme.card(c, content, c.getString(R.string.photo_title));
        photos = new PhotoReelPanel(c); photoCard.addView(photos);
        LinearLayout adsCard = UiTheme.card(c, content, c.getString(R.string.ui_ads_title));
        adsCard.addView(UiTheme.text(c, c.getString(R.string.long_video_independent), 14, UiTheme.CYAN, true));
        skipAds = toggle(c, c.getString(R.string.ui_ads_toggle), R.id.skip_ads_toggle); adsCard.addView(skipAds);
        adSupport = UiTheme.text(c, "", 13, UiTheme.CYAN, false); adSupport.setId(R.id.ad_support); adsCard.addView(adSupport);
        adsCard.addView(UiTheme.text(c, c.getString(R.string.ui_ads_helper), 13, UiTheme.MUTED, false));
        UiTheme.space(c, adsCard, 8);
        adsCard.addView(UiTheme.text(c, c.getString(R.string.ui_ads_only_help), 13, UiTheme.TEXT, false));
        LinearLayout liveCard = UiTheme.card(c, content, c.getString(R.string.live_card_title));
        live = new LiveSkipPanel(c, initialLiveDelay, liveListener); liveCard.addView(live);
        LinearLayout floatCard = UiTheme.card(c, content, c.getString(R.string.ui_floating_title));
        floating = toggle(c, c.getString(R.string.ui_floating_toggle), R.id.floating_toggle); floatCard.addView(floating);
        floatCard.addView(UiTheme.text(c, c.getString(R.string.ui_floating_optional), 14, UiTheme.MUTED, false));
        floatingDetails = UiTheme.column(c); UiTheme.space(c, floatingDetails, 14);
        floatingDetails.addView(UiTheme.text(c, c.getString(R.string.ui_floating_tap_title), 15, UiTheme.TEXT, true));
        tapModes = new RadioGroup(c); tapModes.setOrientation(RadioGroup.VERTICAL);
        rotary = radio(c, c.getString(R.string.ui_tap_rotary), R.id.tap_rotary);
        quick = radio(c, c.getString(R.string.ui_tap_quick), R.id.tap_quick);
        tapModes.addView(rotary); tapModes.addView(quick); floatingDetails.addView(tapModes);
        floatingDetails.addView(UiTheme.text(c, c.getString(R.string.ui_floating_help), 13, UiTheme.MUTED, false)); floatCard.addView(floatingDetails);
        LinearLayout apps = UiTheme.card(c, content, c.getString(R.string.ui_apps_title));
        appsCard = apps;
        apps.addView(UiTheme.text(c, c.getString(R.string.ui_apps_help), 14, UiTheme.MUTED, false));
        youtube = appChoice(c, c.getString(R.string.ui_youtube), R.id.app_youtube); apps.addView(youtube);
        instagram = appChoice(c, c.getString(R.string.ui_instagram), R.id.app_instagram); apps.addView(instagram);
        LinearLayout setup = UiTheme.card(c, content, c.getString(R.string.ui_setup_title));
        setupCard = setup;
        permissionStatus = UiTheme.text(c, "", 14, UiTheme.MUTED, false); setup.addView(permissionStatus);
        accessButton = UiTheme.button(c, c.getString(R.string.ui_accessibility_connect)); accessButton.setId(R.id.permission_accessibility); setup.addView(accessButton);
        overlayButton = UiTheme.button(c, c.getString(R.string.ui_overlay_permission)); overlayButton.setId(R.id.permission_overlay); setup.addView(overlayButton);
        CompatibilityPanel compatibility = new CompatibilityPanel(c); compatibility.setId(R.id.compatibility_panel); setup.addView(compatibility);
        battery = new BatterySetupPanel(c); setup.addView(battery);
        tileButton = UiTheme.button(c, c.getString(R.string.compat_tile_add_button)); tileButton.setId(R.id.tile_add); setup.addView(tileButton);
        updateCard = UiTheme.card(c, content, c.getString(R.string.ui_update_title));
        updates = new UpdatePanel(c); updateCard.addView(updates);
        LinearLayout experimental = UiTheme.card(c, content, c.getString(R.string.ui_experimental_title));
        visualAssist = toggle(c, c.getString(R.string.ui_visual_toggle), R.id.visual_assist_toggle); experimental.addView(visualAssist);
        visualSupport = UiTheme.text(c, "", 13, UiTheme.CYAN, false); visualSupport.setId(R.id.visual_support); experimental.addView(visualSupport);
        experimental.addView(UiTheme.text(c, c.getString(R.string.ui_visual_helper), 13, UiTheme.MUTED, false));
        Button help = UiTheme.button(c, c.getString(R.string.ui_help_show)); help.setId(R.id.help_toggle); content.addView(help);
        TextView details = UiTheme.text(c, c.getString(R.string.ui_help_body),
                14, UiTheme.MUTED, false);
        details.setPadding(UiTheme.dp(c, 8), UiTheme.dp(c, 12), UiTheme.dp(c, 8), UiTheme.dp(c, 12)); details.setVisibility(View.GONE); content.addView(details);
        help.setOnClickListener(v -> { boolean show = details.getVisibility() != View.VISIBLE; details.setVisibility(show ? View.VISIBLE : View.GONE); help.setText(show ? c.getString(R.string.ui_help_hide) : c.getString(R.string.ui_help_show)); });
        TextView version = UiTheme.text(c, c.getString(R.string.app_version, com.fullmetalsonic.shortsloop.BuildConfig.VERSION_NAME), 12, UiTheme.MUTED, false);
        version.setId(R.id.app_version); version.setGravity(Gravity.CENTER); version.setPadding(0, UiTheme.dp(c, 16), 0, UiTheme.dp(c, 8)); content.addView(version);
        LinearLayout footer = new LinearLayout(c); footer.setGravity(Gravity.CENTER_VERTICAL); footer.setPadding(UiTheme.dp(c, 20), UiTheme.dp(c, 12), UiTheme.dp(c, 20), UiTheme.dp(c, 12));
        footer.setBackground(UiTheme.surface(c, UiTheme.SURFACE, 0, true));
        LinearLayout state = UiTheme.column(c); state.addView(UiTheme.text(c, c.getString(R.string.ui_execution_title), 18, UiTheme.TEXT, true));
        status = UiTheme.text(c, c.getString(R.string.off), 13, UiTheme.MUTED, false); status.setPadding(0, UiTheme.dp(c, 4), UiTheme.dp(c, 8), 0); state.addView(status);
        footer.addView(state, new LinearLayout.LayoutParams(0, -2, 1));
        execution = toggle(c, "", R.id.execution_toggle); execution.setContentDescription(c.getString(R.string.ui_execution_description));
        execution.setTextOn(c.getString(R.string.ui_on)); execution.setTextOff(c.getString(R.string.off)); execution.setShowText(true); execution.setTextSize(13);
        footer.addView(execution, new LinearLayout.LayoutParams(-2, UiTheme.dp(c, 56))); root.addView(footer);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else legacyInsets(v, insets);
            return insets;
        });
    }
    public void showSetup(boolean appsMissing) { scroll.smoothScrollTo(0, (appsMissing ? appsCard : setupCard).getTop()); }
    public void showUpdates() { scroll.smoothScrollTo(0, updateCard.getTop()); }
    private static CheckBox appChoice(Context c, String label, int id) {
        CheckBox v = new CheckBox(c); v.setId(id); v.setText(label); v.setTextSize(17); v.setTextColor(UiTheme.TEXT);
        v.setSingleLine(false); v.setButtonTintList(UiTheme.checkedColors()); v.setMinHeight(UiTheme.dp(c, 56)); v.setPadding(UiTheme.dp(c, 4), 0, UiTheme.dp(c, 8), 0); return v;
    }
    private static RadioButton radio(Context c, String label, int id) {
        RadioButton v = new RadioButton(c); v.setId(id); v.setText(label); v.setTextSize(15); v.setTextColor(UiTheme.TEXT);
        v.setSingleLine(false); v.setButtonTintList(UiTheme.checkedColors()); v.setMinHeight(UiTheme.dp(c, 64));
        // The entire row is a target, including blank space beyond short labels.
        v.setLayoutParams(new RadioGroup.LayoutParams(-1, -2));
        v.setPadding(0, UiTheme.dp(c, 6), 0, UiTheme.dp(c, 6)); return v;
    }
    private static Switch toggle(Context c, String label, int id) {
        Switch v = new Switch(c); v.setId(id); v.setText(label); v.setTextSize(16); v.setTextColor(UiTheme.TEXT); v.setMinHeight(UiTheme.dp(c, 52));
        v.setSingleLine(false); v.setThumbTintList(UiTheme.checkedColors()); v.setTrackTintList(android.content.res.ColorStateList.valueOf(UiTheme.BORDER)); v.setSwitchPadding(UiTheme.dp(c, 16)); return v;
    }
    @SuppressWarnings("deprecation") private static void legacyInsets(View v, WindowInsets i) {
        v.setPadding(i.getSystemWindowInsetLeft(), i.getSystemWindowInsetTop(), i.getSystemWindowInsetRight(), i.getSystemWindowInsetBottom());
    }
}
