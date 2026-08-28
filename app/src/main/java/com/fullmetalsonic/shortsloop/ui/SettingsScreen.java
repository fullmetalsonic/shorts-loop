package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Home and retained per-app details. Navigation never applies drafts or changes execution. */
public final class SettingsScreen {
    public final LinearLayout root, homeContent, floatingDetails;
    public final CountEditor count;
    public final SecondsEditor seconds;
    public final AdDelayEditor adDelay;
    public final LiveSkipPanel live;
    public final LongVideoPanel longVideo;
    public final PhotoReelPanel photos;
    public final TextView applied, status, permissionStatus, timedSupport, adSupport, visualSupport, detailTitle, executionTitle;
    public final boolean compactFooter;
    public final Switch youtube, instagram, tiktok;
    public final Switch floating, execution, skipAds, visualAssist, timedFallback, dualMode;
    public final RadioGroup tapModes;
    public final RadioButton rotary, quick;
    public final Button accessButton, overlayButton, tileButton, setupJump, updateBanner, back;
    public final BatterySetupPanel battery;
    public final UpdatePanel updates;
    public final HostSettingsPanel youtubeSettings, instagramSettings, tiktokSettings;
    public final SharedVideoRulesPanel instagramRules, tiktokRules;
    public final AppEntryCard youtubeEntry, instagramEntry, tiktokEntry;
    private final ScrollView scroll;
    private final LinearLayout appsCard, setupCard, updateCard, liveCard, experimental, navigation;
    private final Map<String,Integer> scrollPositions = new HashMap<>();
    private String currentHost;
    private boolean routeInitialized;

    public SettingsScreen(Context c, int initial, CountEditor.Listener countListener, int initialSeconds,
            SecondsEditor.Listener secondsListener, int initialLiveDelay, LiveSkipPanel.Listener liveListener,
            int initialLongSeconds, LongVideoPanel.Listener longListener) {
        this(c, initial, countListener, initialSeconds, secondsListener, initialLiveDelay, liveListener,
                initialLongSeconds, longListener, initial, value -> {}, initialLongSeconds, value -> {});
    }
    public SettingsScreen(Context c, int initial, CountEditor.Listener countListener, int initialSeconds,
            SecondsEditor.Listener secondsListener, int initialLiveDelay, LiveSkipPanel.Listener liveListener,
            int initialLongSeconds, LongVideoPanel.Listener longListener, int instagramCount,
            CountEditor.Listener instagramCountListener, int instagramLongSeconds, LongVideoPanel.Listener instagramLongListener) {
        this(c, initial, countListener, initialSeconds, secondsListener, initialLiveDelay, liveListener, initialLongSeconds,
                longListener, instagramCount, instagramCountListener, instagramLongSeconds, instagramLongListener, 2, value -> {}, 0, value -> {});
    }
    public SettingsScreen(Context c, int initial, CountEditor.Listener countListener, int initialSeconds,
            SecondsEditor.Listener secondsListener, int initialLiveDelay, LiveSkipPanel.Listener liveListener,
            int initialLongSeconds, LongVideoPanel.Listener longListener, int instagramCount,
            CountEditor.Listener instagramCountListener, int instagramLongSeconds, LongVideoPanel.Listener instagramLongListener,
            int tiktokCount, CountEditor.Listener tiktokCountListener, int initialAdDelay, AdDelayEditor.Listener adDelayListener) {
        this(c, initial, countListener, initialSeconds, secondsListener, initialLiveDelay, liveListener, initialLongSeconds,
                longListener, instagramCount, instagramCountListener, instagramLongSeconds, instagramLongListener,
                tiktokCount, tiktokCountListener, initialAdDelay, adDelayListener, 60, value -> {});
    }
    public SettingsScreen(Context c, int initial, CountEditor.Listener countListener, int initialSeconds,
            SecondsEditor.Listener secondsListener, int initialLiveDelay, LiveSkipPanel.Listener liveListener,
            int initialLongSeconds, LongVideoPanel.Listener longListener, int instagramCount,
            CountEditor.Listener instagramCountListener, int instagramLongSeconds, LongVideoPanel.Listener instagramLongListener,
            int tiktokCount, CountEditor.Listener tiktokCountListener, int initialAdDelay, AdDelayEditor.Listener adDelayListener,
            int tiktokLongSeconds, LongVideoPanel.Listener tiktokLongListener) {
        root = UiTheme.column(c); root.setBackgroundColor(UiTheme.BACKGROUND);
        navigation = new LinearLayout(c); navigation.setGravity(Gravity.CENTER_VERTICAL);
        boolean stackedNavigation = c.getResources().getConfiguration().fontScale > 1.3f;
        navigation.setOrientation(stackedNavigation ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        navigation.setPadding(UiTheme.dp(c,12),UiTheme.dp(c,6),UiTheme.dp(c,16),UiTheme.dp(c,6));
        back = UiTheme.button(c,c.getString(R.string.host_back_home)); back.setId(R.id.host_back_home);
        navigation.addView(back,new LinearLayout.LayoutParams(-2,-2));
        detailTitle = UiTheme.text(c,"",22,UiTheme.TEXT,true); detailTitle.setId(R.id.host_detail_title);
        detailTitle.setPadding(stackedNavigation ? 0 : UiTheme.dp(c,12),0,0,0);
        navigation.addView(detailTitle, stackedNavigation ? new LinearLayout.LayoutParams(-1,-2)
                : new LinearLayout.LayoutParams(0,-2,1)); root.addView(navigation);
        scroll = new ScrollView(c); scroll.setId(R.id.settings_scroll); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        // The route owns scroll restoration; a single ScrollView saved offset cannot represent four pages.
        scroll.setSaveEnabled(false);
        FrameLayout holder = new ContentFrame(c); LinearLayout content = UiTheme.column(c);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        content.setPadding(0,UiTheme.dp(c,16),0,UiTheme.dp(c,12)); content.setFocusableInTouchMode(true);
        content.setDefaultFocusHighlightEnabled(false);
        holder.addView(content,cp); scroll.addView(holder); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        homeContent = UiTheme.column(c); homeContent.setId(R.id.settings_home); content.addView(homeContent);
        LinearLayout header = new LinearLayout(c); header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(c); icon.setImageResource(R.mipmap.ic_launcher); icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        header.addView(icon,new LinearLayout.LayoutParams(UiTheme.dp(c,52),UiTheme.dp(c,52)));
        LinearLayout names = UiTheme.column(c); names.setPadding(UiTheme.dp(c,12),0,0,0);
        names.addView(UiTheme.text(c,c.getString(R.string.app_name),24,UiTheme.TEXT,true));
        names.addView(UiTheme.text(c,c.getString(R.string.home_summary),13,UiTheme.MUTED,false));
        header.addView(names,new LinearLayout.LayoutParams(0,-2,1)); homeContent.addView(header); UiTheme.space(c,homeContent,16);
        setupJump = UiTheme.button(c,c.getString(R.string.setup_needed_banner)); setupJump.setId(R.id.setup_jump); homeContent.addView(setupJump);
        updateBanner = UiTheme.button(c,c.getString(R.string.ui_update_view)); updateBanner.setId(R.id.update_banner); updateBanner.setVisibility(View.GONE); homeContent.addView(updateBanner);
        appsCard = UiTheme.column(c); homeContent.addView(appsCard);
        appsCard.addView(UiTheme.text(c,c.getString(R.string.home_apps_title),19,UiTheme.TEXT,true));
        TextView appHelp = UiTheme.text(c,c.getString(R.string.home_apps_help),13,UiTheme.MUTED,false); appsCard.addView(appHelp); UiTheme.space(c,appsCard,10);
        youtubeEntry = new AppEntryCard(c,R.string.ui_youtube,R.id.open_youtube_settings);
        instagramEntry = new AppEntryCard(c,R.string.ui_instagram,R.id.open_instagram_settings);
        tiktokEntry = new AppEntryCard(c,R.string.ui_tiktok,R.id.open_tiktok_settings);
        for (AppEntryCard entry : new AppEntryCard[]{youtubeEntry,instagramEntry,tiktokEntry}) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=UiTheme.dp(c,10); appsCard.addView(entry,p);
        }
        youtubeSettings = new HostSettingsPanel(c,initial,countListener,initialLongSeconds,longListener,false);
        instagramSettings = new HostSettingsPanel(c,instagramCount,instagramCountListener,instagramLongSeconds,instagramLongListener,true);
        tiktokSettings = new HostSettingsPanel(c,tiktokCount,tiktokCountListener,tiktokLongSeconds,tiktokLongListener,SettingsStore.TIKTOK_PACKAGE);
        content.addView(youtubeSettings.root); content.addView(instagramSettings.root); content.addView(tiktokSettings.root);
        youtube = youtubeSettings.activation; instagram = instagramSettings.activation; tiktok = tiktokSettings.activation;
        count = youtubeSettings.count; longVideo = youtubeSettings.longVideo; applied = youtubeSettings.applied;
        tapModes = youtubeSettings.tapModes; rotary = youtubeSettings.rotary; quick = youtubeSettings.quick;
        instagramRules = new SharedVideoRulesPanel(c,false,initialSeconds,initialAdDelay);
        instagramRules.onSeconds = secondsListener; instagramRules.onAdDelay = adDelayListener;
        tiktokRules = new SharedVideoRulesPanel(c,true,3,0);
        instagramSettings.special.addView(instagramRules.root); tiktokSettings.special.addView(tiktokRules.root);
        seconds=instagramRules.seconds; adDelay=instagramRules.adDelay; photos=instagramRules.photos;
        timedFallback=instagramRules.timedFallback; skipAds=instagramRules.skipAds;
        timedSupport=instagramRules.timedSupport; adSupport=instagramRules.adSupport;
        liveCard = UiTheme.card(c,youtubeSettings.special,c.getString(R.string.live_card_title));
        live = new LiveSkipPanel(c,initialLiveDelay,liveListener); liveCard.addView(live);
        experimental = UiTheme.card(c,instagramSettings.special,c.getString(R.string.ui_experimental_title));
        visualAssist = toggle(c,c.getString(R.string.ui_visual_toggle),R.id.visual_assist_toggle); experimental.addView(visualAssist);
        visualSupport = UiTheme.text(c,"",13,UiTheme.CYAN,false); visualSupport.setId(R.id.visual_support); experimental.addView(visualSupport);
        experimental.addView(UiTheme.text(c,c.getString(R.string.ui_visual_helper),13,UiTheme.MUTED,false));
        UiTheme.space(c,homeContent,10); homeContent.addView(UiTheme.text(c,c.getString(R.string.mw_common_settings),19,UiTheme.TEXT,true)); UiTheme.space(c,homeContent,10);
        LinearLayout dualCard = UiTheme.card(c,homeContent,c.getString(R.string.mw_mode_title));
        dualMode = toggle(c,c.getString(R.string.mw_dual_toggle),R.id.mw_dual_mode); dualCard.addView(dualMode);
        dualCard.addView(UiTheme.text(c,c.getString(R.string.mw_mode_help),13,UiTheme.MUTED,false));
        LinearLayout floatCard = UiTheme.card(c,homeContent,c.getString(R.string.ui_floating_title));
        floating = toggle(c,c.getString(R.string.ui_floating_toggle),R.id.floating_toggle); floatCard.addView(floating);
        floatCard.addView(UiTheme.text(c,c.getString(R.string.ui_floating_optional),14,UiTheme.MUTED,false));
        floatingDetails = UiTheme.column(c); UiTheme.space(c,floatingDetails,10);
        floatingDetails.addView(UiTheme.text(c,c.getString(R.string.mw_common_floating_help),13,UiTheme.MUTED,false)); floatCard.addView(floatingDetails);
        setupCard = UiTheme.card(c,homeContent,c.getString(R.string.ui_setup_title));
        permissionStatus = UiTheme.text(c,"",14,UiTheme.MUTED,false); setupCard.addView(permissionStatus);
        accessButton = UiTheme.button(c,c.getString(R.string.ui_accessibility_connect)); accessButton.setId(R.id.permission_accessibility); setupCard.addView(accessButton);
        overlayButton = UiTheme.button(c,c.getString(R.string.ui_overlay_permission)); overlayButton.setId(R.id.permission_overlay); setupCard.addView(overlayButton);
        CompatibilityPanel compatibility = new CompatibilityPanel(c); compatibility.setId(R.id.compatibility_panel); setupCard.addView(compatibility);
        battery = new BatterySetupPanel(c); setupCard.addView(battery);
        tileButton = UiTheme.button(c,c.getString(R.string.compat_tile_add_button)); tileButton.setId(R.id.tile_add); setupCard.addView(tileButton);
        updateCard = UiTheme.card(c,homeContent,c.getString(R.string.ui_update_title));
        updates = new UpdatePanel(c); updateCard.addView(updates);
        Button help = UiTheme.button(c,c.getString(R.string.ui_help_show)); help.setId(R.id.help_toggle); homeContent.addView(help);
        TextView details = UiTheme.text(c,c.getString(R.string.mw_mode_help)+"\n\n"+c.getString(R.string.mw_host_floating_help)+"\n\n"+c.getString(R.string.ui_help_body),14,UiTheme.MUTED,false);
        details.setPadding(UiTheme.dp(c,8),UiTheme.dp(c,12),UiTheme.dp(c,8),UiTheme.dp(c,12)); details.setVisibility(View.GONE); homeContent.addView(details);
        help.setOnClickListener(v->{boolean show=details.getVisibility()!=View.VISIBLE;details.setVisibility(show?View.VISIBLE:View.GONE);help.setText(show?R.string.ui_help_hide:R.string.ui_help_show);});
        TextView version=UiTheme.text(c,c.getString(R.string.app_version,com.fullmetalsonic.shortsloop.BuildConfig.VERSION_NAME),12,UiTheme.MUTED,false);
        version.setId(R.id.app_version);version.setGravity(Gravity.CENTER);version.setPadding(0,UiTheme.dp(c,16),0,UiTheme.dp(c,8));homeContent.addView(version);
        LinearLayout footer=new LinearLayout(c);footer.setGravity(Gravity.CENTER_VERTICAL);footer.setPadding(UiTheme.dp(c,20),UiTheme.dp(c,12),UiTheme.dp(c,20),UiTheme.dp(c,12));
        footer.setBackground(UiTheme.surface(c,UiTheme.SURFACE,0,true));
        compactFooter=c.getResources().getConfiguration().fontScale>=1.5f;
        LinearLayout state=UiTheme.column(c);
        executionTitle=UiTheme.text(c,c.getString(compactFooter?R.string.ui_execution_title_compact:R.string.ui_execution_title),18,UiTheme.TEXT,true);
        executionTitle.setContentDescription(c.getString(R.string.ui_execution_title));state.addView(executionTitle);
        status=UiTheme.text(c,c.getString(R.string.off),13,UiTheme.MUTED,false);status.setPadding(0,UiTheme.dp(c,4),UiTheme.dp(c,8),0);state.addView(status);
        footer.addView(state,new LinearLayout.LayoutParams(0,-2,1));
        execution=toggle(c,"",R.id.execution_toggle);execution.setContentDescription(c.getString(R.string.ui_execution_description));
        execution.setTextOn(c.getString(R.string.ui_on));execution.setTextOff(c.getString(R.string.off));execution.setShowText(true);execution.setTextSize(13);
        footer.addView(execution,new LinearLayout.LayoutParams(-2,UiTheme.dp(c,56)));root.addView(footer);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}
            else legacyInsets(v,insets);return insets;
        });
        youtubeEntry.open.setOnClickListener(v->showHost(SettingsStore.YOUTUBE_PACKAGE));
        instagramEntry.open.setOnClickListener(v->showHost(SettingsStore.INSTAGRAM_PACKAGE));
        tiktokEntry.open.setOnClickListener(v->showHost(SettingsStore.TIKTOK_PACKAGE));
        back.setOnClickListener(v->showHome()); showHome();
    }
    public SharedVideoRulesPanel rules(String host) {
        if(SettingsStore.INSTAGRAM_PACKAGE.equals(host)) return instagramRules;
        if(SettingsStore.TIKTOK_PACKAGE.equals(host)) return tiktokRules;
        return null;
    }
    public String currentHost() { return currentHost; }
    public void showHome() { navigate(null); }
    public void showHost(boolean instagramHost) { showHost(instagramHost?SettingsStore.INSTAGRAM_PACKAGE:SettingsStore.YOUTUBE_PACKAGE); }
    public void showHost(String host) {
        if(!SettingsStore.supportedHost(host))throw new IllegalArgumentException("Unsupported host");
        navigate(host);
    }
    private void navigate(String host) {
        if(routeInitialized && Objects.equals(currentHost,host)) return;
        if(routeInitialized)scrollPositions.put(routeKey(currentHost),scroll.getScrollY());
        currentHost=host;routeInitialized=true; boolean home=host==null;
        homeContent.setVisibility(home?View.VISIBLE:View.GONE); navigation.setVisibility(home?View.GONE:View.VISIBLE);
        boolean yt=SettingsStore.YOUTUBE_PACKAGE.equals(host),ig=SettingsStore.INSTAGRAM_PACKAGE.equals(host),tt=SettingsStore.TIKTOK_PACKAGE.equals(host);
        youtubeSettings.root.setVisibility(yt?View.VISIBLE:View.GONE);instagramSettings.root.setVisibility(ig?View.VISIBLE:View.GONE);tiktokSettings.root.setVisibility(tt?View.VISIBLE:View.GONE);
        liveCard.setVisibility(yt?View.VISIBLE:View.GONE);experimental.setVisibility(ig?View.VISIBLE:View.GONE);
        for(LinearLayout card:new LinearLayout[]{instagramRules.timedCard,instagramRules.adsCard,instagramRules.photoCard})card.setVisibility(ig?View.VISIBLE:View.GONE);
        for(LinearLayout card:new LinearLayout[]{tiktokRules.timedCard,tiktokRules.adsCard,tiktokRules.photoCard})card.setVisibility(tt?View.VISIBLE:View.GONE);
        if(!home)detailTitle.setText(yt?R.string.ui_youtube:ig?R.string.ui_instagram:R.string.ui_tiktok);
        int y=scrollPositions.getOrDefault(routeKey(host),0);
        scroll.post(()->{if(Objects.equals(currentHost,host))scroll.scrollTo(0,y);});
    }
    public void saveNavigation(Bundle bundle) {
        scrollPositions.put(routeKey(currentHost),scroll.getScrollY());
        for(Map.Entry<String,Integer> e:scrollPositions.entrySet())bundle.putInt("route_scroll_"+e.getKey(),e.getValue());
    }
    public void restoreNavigation(Bundle bundle) {
        if(bundle==null)return;
        for(String key:new String[]{"home",SettingsStore.YOUTUBE_PACKAGE,SettingsStore.INSTAGRAM_PACKAGE,SettingsStore.TIKTOK_PACKAGE})
            if(bundle.containsKey("route_scroll_"+key))scrollPositions.put(key,bundle.getInt("route_scroll_"+key));
        // Do not let the constructor's temporary home route overwrite a restored offset.
        routeInitialized=false;
    }
    private String routeKey(String host) { return host==null?"home":host; }
    public void showSetup(boolean appsMissing) { showHome();scroll.post(()->scroll.smoothScrollTo(0,(appsMissing?appsCard:setupCard).getTop())); }
    public void showUpdates() { showHome();scroll.post(()->scroll.smoothScrollTo(0,updateCard.getTop())); }
    public void focusError(String host) {
        showHost(host);root.post(()->{if(!Objects.equals(currentHost,host))return;View target=findError(root);if(target!=null){target.requestFocus();target.requestRectangleOnScreen(new android.graphics.Rect(0,0,target.getWidth(),target.getHeight()),false);}});
    }
    private View findError(View view) {
        if(view instanceof EditText && ((EditText)view).getError()!=null && view.isShown())return view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View match=findError(((ViewGroup)view).getChildAt(i));if(match!=null)return match;}
        return null;
    }
    static Switch toggle(Context c,String label,int id) {
        Switch v=new Switch(c);v.setId(id);v.setText(label);v.setTextSize(16);v.setTextColor(UiTheme.TEXT);v.setMinHeight(UiTheme.dp(c,52));
        v.setSingleLine(false);v.setThumbTintList(UiTheme.checkedColors());v.setTrackTintList(android.content.res.ColorStateList.valueOf(UiTheme.BORDER));v.setSwitchPadding(UiTheme.dp(c,16));return v;
    }
    @SuppressWarnings("deprecation") private static void legacyInsets(View v,WindowInsets i){v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());}
}
