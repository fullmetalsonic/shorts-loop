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
            SecondsEditor.Listener secondsListener, int initialLiveDelay, LiveSkipPanel.Listener liveListener) {
        root = UiTheme.column(c); root.setBackgroundColor(UiTheme.BACKGROUND);
        scroll = new ScrollView(c); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        FrameLayout holder = new FrameLayout(c); LinearLayout content = UiTheme.column(c);
        int widthDp = Math.round(c.getResources().getDisplayMetrics().widthPixels / c.getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(UiTheme.dp(c, Math.min(600, Math.max(200, widthDp - 32))), -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        content.setPadding(0, UiTheme.dp(c, 20), 0, UiTheme.dp(c, 12)); content.setFocusableInTouchMode(true);
        // This container is a focus sink after Done, not an actionable highlighted control.
        content.setDefaultFocusHighlightEnabled(false);
        holder.addView(content, cp); scroll.addView(holder); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout header = new LinearLayout(c); header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(c); icon.setImageResource(R.mipmap.ic_launcher); icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        header.addView(icon, new LinearLayout.LayoutParams(UiTheme.dp(c, 52), UiTheme.dp(c, 52)));
        LinearLayout names = UiTheme.column(c); names.setPadding(UiTheme.dp(c, 12), 0, 0, 0);
        names.addView(UiTheme.text(c, "쇼츠 자동 넘김", 24, UiTheme.TEXT, true));
        names.addView(UiTheme.text(c, "반복 · 시간제 · 광고 · 라이브를 한곳에서", 13, UiTheme.MUTED, false));
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1)); content.addView(header); UiTheme.space(c, content, 20);
        setupJump = UiTheme.button(c, "사용 준비가 필요합니다 · 바로가기"); setupJump.setId(R.id.setup_jump); content.addView(setupJump);
        updateBanner = UiTheme.button(c, "새 업데이트 보기"); updateBanner.setId(R.id.update_banner); updateBanner.setVisibility(View.GONE); content.addView(updateBanner);
        LinearLayout repeat = UiTheme.card(c, content, "일반 영상 · 횟수로 넘김");
        repeat.addView(UiTheme.text(c, "YouTube · Instagram / 재생 정보가 있는 영상을 총 몇 번 볼까요?", 14, UiTheme.MUTED, false)); UiTheme.space(c, repeat, 12);
        count = new CountEditor(c, initial, countListener); repeat.addView(count);
        applied = UiTheme.text(c, "", 14, UiTheme.CYAN, false); applied.setPadding(0, UiTheme.dp(c, 8), 0, 0); repeat.addView(applied);
        LinearLayout timedCard = UiTheme.card(c, content, "진행 정보 없는 영상 · 시간제로 넘김");
        timedFallback = toggle(c, "시간제 넘김", R.id.timed_fallback_toggle); timedCard.addView(timedFallback);
        timedSupport = UiTheme.text(c, "", 13, UiTheme.CYAN, false); timedSupport.setId(R.id.timed_support); timedCard.addView(timedSupport);
        timedCard.addView(UiTheme.text(c, "Instagram 전용 · 진행 정보를 읽을 수 없을 때만 설정 시간 후 넘깁니다. 완주 횟수를 계산하는 기능은 아닙니다.", 13, UiTheme.MUTED, false));
        UiTheme.space(c, timedCard, 8);
        seconds = new SecondsEditor(c, initialSeconds, secondsListener); timedCard.addView(seconds);
        timedCard.addView(UiTheme.text(c, "반복 횟수가 0이면 시간제도 중지합니다. 정지·댓글·다른 화면에서 돌아오면 시간을 처음부터 셉니다. 기본은 꺼짐입니다.", 13, UiTheme.MUTED, false));
        LinearLayout adsCard = UiTheme.card(c, content, "광고 · 바로 넘김");
        adsCard.addView(UiTheme.text(c, "반복 횟수와 별개 · 0회에서도 사용 가능", 14, UiTheme.CYAN, true));
        skipAds = toggle(c, "광고 바로 넘기기", R.id.skip_ads_toggle); adsCard.addView(skipAds);
        adSupport = UiTheme.text(c, "", 13, UiTheme.CYAN, false); adSupport.setId(R.id.ad_support); adsCard.addView(adSupport);
        adsCard.addView(UiTheme.text(c, "Instagram 전용 · 이 옵션과 아래 전체 실행을 켜면, 광고를 인식하는 즉시 넘깁니다. 끄면 광고는 직접 넘겨 주세요.", 13, UiTheme.MUTED, false));
        UiTheme.space(c, adsCard, 8);
        adsCard.addView(UiTheme.text(c, "광고만 넘기려면: 반복 0회 + 광고 켜기 + 라이브 끄기 + 전체 실행 켜기", 13, UiTheme.TEXT, false));
        LinearLayout liveCard = UiTheme.card(c, content, c.getString(R.string.live_card_title));
        live = new LiveSkipPanel(c, initialLiveDelay, liveListener); liveCard.addView(live);
        LinearLayout floatCard = UiTheme.card(c, content, "플로팅 리모컨");
        floating = toggle(c, "화면 위에 숫자 표시", R.id.floating_toggle); floatCard.addView(floating);
        floatCard.addView(UiTheme.text(c, "꺼도 자동 넘김은 계속 사용할 수 있어요.", 14, UiTheme.MUTED, false));
        floatingDetails = UiTheme.column(c); UiTheme.space(c, floatingDetails, 14);
        floatingDetails.addView(UiTheme.text(c, "숫자를 터치할 때", 15, UiTheme.TEXT, true));
        tapModes = new RadioGroup(c); tapModes.setOrientation(RadioGroup.VERTICAL);
        rotary = radio(c, "횟수 순환\n0 → 1 → … → 기준 횟수 → 0", R.id.tap_rotary);
        quick = radio(c, "반복 켜기·끄기\n0 ↔ 기준 횟수 · 광고·라이브는 별도", R.id.tap_quick);
        tapModes.addView(rotary); tapModes.addView(quick); floatingDetails.addView(tapModes);
        floatingDetails.addView(UiTheme.text(c, "0회는 반복·시간제만 중지합니다. 별도 옵션 상태는 ‘광고’·‘라이브’로 표시하며 둘 다 켜면 ‘광·라’입니다.\n숫자를 끌면 이동 · 모서리 ×는 광고·라이브까지 전체 실행 종료", 13, UiTheme.MUTED, false)); floatCard.addView(floatingDetails);
        LinearLayout apps = UiTheme.card(c, content, "사용할 앱");
        appsCard = apps;
        apps.addView(UiTheme.text(c, "선택한 앱에서만 동작합니다. 둘 다 선택해도 됩니다.", 14, UiTheme.MUTED, false));
        youtube = appChoice(c, "YouTube 쇼츠", R.id.app_youtube); apps.addView(youtube);
        instagram = appChoice(c, "Instagram 릴스", R.id.app_instagram); apps.addView(instagram);
        LinearLayout setup = UiTheme.card(c, content, "사용 준비");
        setupCard = setup;
        permissionStatus = UiTheme.text(c, "", 14, UiTheme.MUTED, false); setup.addView(permissionStatus);
        accessButton = UiTheme.button(c, "접근성 연결하기"); accessButton.setId(R.id.permission_accessibility); setup.addView(accessButton);
        overlayButton = UiTheme.button(c, "다른 앱 위 표시 허용"); overlayButton.setId(R.id.permission_overlay); setup.addView(overlayButton);
        CompatibilityPanel compatibility = new CompatibilityPanel(c); compatibility.setId(R.id.compatibility_panel); setup.addView(compatibility);
        battery = new BatterySetupPanel(c); setup.addView(battery);
        tileButton = UiTheme.button(c, "빠른 설정에 실행 토글 추가"); tileButton.setId(R.id.tile_add); setup.addView(tileButton);
        updateCard = UiTheme.card(c, content, "업데이트 · 앱 정보");
        updates = new UpdatePanel(c); updateCard.addView(updates);
        LinearLayout experimental = UiTheme.card(c, content, "실험 기능");
        visualAssist = toggle(c, "화면 분석 보조 · 시험", R.id.visual_assist_toggle); experimental.addView(visualAssist);
        visualSupport = UiTheme.text(c, "", 13, UiTheme.CYAN, false); visualSupport.setId(R.id.visual_support); experimental.addView(visualSupport);
        experimental.addView(UiTheme.text(c, "재생 정보 없는 Instagram 영상만 분석. 화면 저장·전송 없음. 처음 반복을 학습해 추가 재생될 수 있고 정확한 횟수를 보장하지 않습니다. Android 14 이상. 시간제와 함께 켜면 시간제가 우선입니다.", 13, UiTheme.MUTED, false));
        Button help = UiTheme.button(c, "사용법과 감지 한계 보기"); help.setId(R.id.help_toggle); content.addView(help);
        TextView details = UiTheme.text(c,
                "1/2는 첫 번째 재생, 2/2는 두 번째 재생입니다.\n중간에 켜면 다음 처음 재생부터 셉니다."
                + "\n\n0회는 일반 영상의 반복·시간제 넘김을 멈춥니다. 광고·라이브는 반복 횟수와 별도입니다. 아래 전체 실행을 끄면 광고·라이브까지 모두 멈추고 플로팅도 닫힙니다."
                + "\n\n광고 바로 넘기기는 선택한 Instagram의 광고를 인식했을 때만 동작합니다. 광고 옵션과 전체 실행이 켜져 있으면 0회에서도 광고를 넘깁니다."
                + "\n\n라이브 미리보기 넘기기는 선택한 YouTube의 쇼츠 안에서 인식한 라이브에만 적용합니다. 기본은 꺼짐이고, 지연 0초는 바로 넘기기이며 1~60초로 조절할 수 있습니다. 라이브 옵션과 전체 실행이 켜져 있으면 일반 반복 0회에서도 동작합니다. 일반 영상의 반복 횟수나 Instagram 시간제 설정은 바꾸지 않습니다."
                + "\n\n시간제 넘김은 진행 정보를 읽을 수 없는 Instagram 영상에만 적용합니다. 기본은 꺼짐이며 초기 10초를 5~60초로 조절합니다. 정확한 완주 횟수가 아닌 대기 시간이고 정지·댓글·다른 화면에서 돌아오면 처음부터 셉니다. 정상 진행 정보가 있으면 기존 반복 횟수를 따릅니다."
                + "\n\n댓글·메뉴·앱 전환 시에는 대기합니다. 기본 모드에서는 진행 정보를 읽을 수 없는 일반 영상과 3초 미만 일반 영상을 넘기지 않습니다. 화면 분석 보조는 재생 정보 없는 Instagram 영상의 시험용 대안이며 정확한 횟수를 보장하지 않습니다. 둘 다 켜면 시간제가 우선이고 화면 분석 선택은 보존됩니다. 작은 수동 탐색은 완벽하게 구분하지 못할 수 있습니다. 앱 업데이트에 따라 감지가 달라질 수 있습니다."
                + "\n\n업데이트 확인·다운로드에만 인터넷 사용 · 영상·계정·시청 이력 저장·전송 없음. 서비스가 재연결되면 안전을 위해 꺼진 상태로 시작합니다.",
                14, UiTheme.MUTED, false);
        details.setPadding(UiTheme.dp(c, 8), UiTheme.dp(c, 12), UiTheme.dp(c, 8), UiTheme.dp(c, 12)); details.setVisibility(View.GONE); content.addView(details);
        help.setOnClickListener(v -> { boolean show = details.getVisibility() != View.VISIBLE; details.setVisibility(show ? View.VISIBLE : View.GONE); help.setText(show ? "사용법 접기" : "사용법과 감지 한계 보기"); });
        TextView version = UiTheme.text(c, "ShortsLoop " + com.fullmetalsonic.shortsloop.BuildConfig.VERSION_NAME + " · 시험판", 12, UiTheme.MUTED, false); version.setGravity(Gravity.CENTER); version.setPadding(0, UiTheme.dp(c, 16), 0, UiTheme.dp(c, 8)); content.addView(version);
        LinearLayout footer = new LinearLayout(c); footer.setGravity(Gravity.CENTER_VERTICAL); footer.setPadding(UiTheme.dp(c, 20), UiTheme.dp(c, 12), UiTheme.dp(c, 20), UiTheme.dp(c, 12));
        footer.setBackground(UiTheme.surface(c, UiTheme.SURFACE, 0, true));
        LinearLayout state = UiTheme.column(c); state.addView(UiTheme.text(c, "전체 자동 넘김 실행", 18, UiTheme.TEXT, true));
        status = UiTheme.text(c, "꺼짐", 13, UiTheme.MUTED, false); status.setPadding(0, UiTheme.dp(c, 4), UiTheme.dp(c, 8), 0); state.addView(status);
        footer.addView(state, new LinearLayout.LayoutParams(0, -2, 1));
        execution = toggle(c, "", R.id.execution_toggle); execution.setContentDescription("반복·시간제·광고·라이브 전체 실행 켜기 또는 끄기");
        execution.setTextOn("켜짐"); execution.setTextOff("꺼짐"); execution.setShowText(true); execution.setTextSize(13);
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
        v.setButtonTintList(UiTheme.checkedColors()); v.setMinHeight(UiTheme.dp(c, 56)); v.setPadding(UiTheme.dp(c, 4), 0, UiTheme.dp(c, 8), 0); return v;
    }
    private static RadioButton radio(Context c, String label, int id) {
        RadioButton v = new RadioButton(c); v.setId(id); v.setText(label); v.setTextSize(15); v.setTextColor(UiTheme.TEXT);
        v.setButtonTintList(UiTheme.checkedColors()); v.setMinHeight(UiTheme.dp(c, 64));
        // The entire row is a target, including blank space beyond short labels.
        v.setLayoutParams(new RadioGroup.LayoutParams(-1, -2));
        v.setPadding(0, UiTheme.dp(c, 6), 0, UiTheme.dp(c, 6)); return v;
    }
    private static Switch toggle(Context c, String label, int id) {
        Switch v = new Switch(c); v.setId(id); v.setText(label); v.setTextSize(16); v.setTextColor(UiTheme.TEXT); v.setMinHeight(UiTheme.dp(c, 52));
        v.setThumbTintList(UiTheme.checkedColors()); v.setTrackTintList(android.content.res.ColorStateList.valueOf(UiTheme.BORDER)); v.setSwitchPadding(UiTheme.dp(c, 16)); return v;
    }
    @SuppressWarnings("deprecation") private static void legacyInsets(View v, WindowInsets i) {
        v.setPadding(i.getSystemWindowInsetLeft(), i.getSystemWindowInsetTop(), i.getSystemWindowInsetRight(), i.getSystemWindowInsetBottom());
    }
}
