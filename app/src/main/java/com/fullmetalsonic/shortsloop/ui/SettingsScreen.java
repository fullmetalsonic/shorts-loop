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
    public final TextView applied, status, permissionStatus;
    public final CheckBox youtube, instagram;
    public final Switch floating, execution, skipAds;
    public final RadioGroup tapModes;
    public final RadioButton rotary, quick;
    public final LinearLayout floatingDetails;
    public final Button accessButton, overlayButton, tileButton;
    public final BatterySetupPanel battery;
    public SettingsScreen(Context c, int initial, CountEditor.Listener countListener) {
        root = UiTheme.column(c); root.setBackgroundColor(UiTheme.BACKGROUND);
        ScrollView scroll = new ScrollView(c); scroll.setFillViewport(true); scroll.setClipToPadding(false);
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
        names.addView(UiTheme.text(c, "보고 싶은 횟수만큼, 다음 영상으로", 13, UiTheme.MUTED, false));
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1)); content.addView(header); UiTheme.space(c, content, 20);
        LinearLayout repeat = UiTheme.card(c, content, "1  반복 횟수");
        repeat.addView(UiTheme.text(c, "한 영상을 총 몇 번 볼까요?", 14, UiTheme.MUTED, false)); UiTheme.space(c, repeat, 12);
        count = new CountEditor(c, initial, countListener); repeat.addView(count);
        applied = UiTheme.text(c, "", 14, UiTheme.CYAN, false); applied.setPadding(0, UiTheme.dp(c, 8), 0, 0); repeat.addView(applied);
        LinearLayout apps = UiTheme.card(c, content, "2  사용할 앱");
        apps.addView(UiTheme.text(c, "둘 다 선택해도 됩니다. 선택한 앱에서만 동작해요.", 14, UiTheme.MUTED, false));
        youtube = appChoice(c, "YouTube 쇼츠", R.id.app_youtube); apps.addView(youtube);
        instagram = appChoice(c, "Instagram 릴스", R.id.app_instagram); apps.addView(instagram);
        UiTheme.space(c, apps, 8);
        skipAds = toggle(c, "광고 바로 넘기기", R.id.skip_ads_toggle); apps.addView(skipAds);
        apps.addView(UiTheme.text(c, "Instagram 광고만 · 실행 켜짐/1회 이상일 때 바로 넘김. 끄면 광고는 직접 넘겨 주세요. 재생 정보 없는 일반 릴스도 수동 넘김이 필요합니다.", 13, UiTheme.MUTED, false));
        LinearLayout floatCard = UiTheme.card(c, content, "플로팅 리모컨");
        floating = toggle(c, "화면 위에 숫자 표시", R.id.floating_toggle); floatCard.addView(floating);
        floatCard.addView(UiTheme.text(c, "꺼도 자동 넘김은 계속 사용할 수 있어요.", 14, UiTheme.MUTED, false));
        floatingDetails = UiTheme.column(c); UiTheme.space(c, floatingDetails, 14);
        floatingDetails.addView(UiTheme.text(c, "숫자를 터치할 때", 15, UiTheme.TEXT, true));
        tapModes = new RadioGroup(c); tapModes.setOrientation(RadioGroup.VERTICAL);
        rotary = radio(c, "횟수 순환\n0 → 1 → … → 기준 횟수 → 0", R.id.tap_rotary);
        quick = radio(c, "간편 켜기·끄기\n0 ↔ 기준 횟수", R.id.tap_quick);
        tapModes.addView(rotary); tapModes.addView(quick); floatingDetails.addView(tapModes);
        floatingDetails.addView(UiTheme.text(c, "숫자를 끌면 이동 · 모서리 ×는 실행 종료", 13, UiTheme.MUTED, false)); floatCard.addView(floatingDetails);
        LinearLayout setup = UiTheme.card(c, content, "사용 준비");
        permissionStatus = UiTheme.text(c, "", 14, UiTheme.MUTED, false); setup.addView(permissionStatus);
        accessButton = UiTheme.button(c, "접근성 연결하기"); accessButton.setId(R.id.permission_accessibility); setup.addView(accessButton);
        overlayButton = UiTheme.button(c, "다른 앱 위 표시 허용"); overlayButton.setId(R.id.permission_overlay); setup.addView(overlayButton);
        battery = new BatterySetupPanel(c); setup.addView(battery);
        tileButton = UiTheme.button(c, "빠른 설정에 실행 토글 추가"); tileButton.setId(R.id.tile_add); setup.addView(tileButton);
        Button help = UiTheme.button(c, "사용법과 감지 한계 보기"); help.setId(R.id.help_toggle); content.addView(help);
        TextView details = UiTheme.text(c, "1/2는 첫 번째 재생, 2/2는 두 번째 재생입니다.\n중간에 켜면 다음 처음 재생부터 셉니다.\n\n0회는 광고를 포함한 모든 자동 넘김을 멈춥니다. 실행을 끄면 플로팅도 닫힙니다.\n\n광고 바로 넘기기는 선택한 Instagram의 광고를 인식했을 때만 동작합니다. 실행이 켜져 있고 현재 횟수가 1회 이상이어야 합니다.\n\n댓글·메뉴·앱 전환 시에는 대기합니다. 진행 정보를 읽을 수 없는 일반 영상과 3초 미만 일반 영상은 넘기지 않습니다. 작은 수동 탐색은 완벽하게 구분하지 못할 수 있습니다. 앱 업데이트에 따라 감지가 달라질 수 있습니다.\n\n인터넷 권한 없음 · 영상·계정·시청 이력 저장 없음. 서비스가 재연결되면 안전을 위해 꺼진 상태로 시작합니다.", 14, UiTheme.MUTED, false);
        details.setPadding(UiTheme.dp(c, 8), UiTheme.dp(c, 12), UiTheme.dp(c, 8), UiTheme.dp(c, 12)); details.setVisibility(View.GONE); content.addView(details);
        help.setOnClickListener(v -> { boolean show = details.getVisibility() != View.VISIBLE; details.setVisibility(show ? View.VISIBLE : View.GONE); help.setText(show ? "사용법 접기" : "사용법과 감지 한계 보기"); });
        TextView version = UiTheme.text(c, "ShortsLoop " + com.fullmetalsonic.shortsloop.BuildConfig.VERSION_NAME + " · 기기 검증용", 12, UiTheme.MUTED, false); version.setGravity(Gravity.CENTER); version.setPadding(0, UiTheme.dp(c, 16), 0, UiTheme.dp(c, 8)); content.addView(version);
        LinearLayout footer = new LinearLayout(c); footer.setGravity(Gravity.CENTER_VERTICAL); footer.setPadding(UiTheme.dp(c, 20), UiTheme.dp(c, 12), UiTheme.dp(c, 20), UiTheme.dp(c, 12));
        footer.setBackground(UiTheme.surface(c, UiTheme.SURFACE, 0, true));
        LinearLayout state = UiTheme.column(c); state.addView(UiTheme.text(c, "자동 넘김 실행", 18, UiTheme.TEXT, true));
        status = UiTheme.text(c, "꺼짐", 13, UiTheme.MUTED, false); status.setPadding(0, UiTheme.dp(c, 4), UiTheme.dp(c, 8), 0); state.addView(status);
        footer.addView(state, new LinearLayout.LayoutParams(0, -2, 1));
        execution = toggle(c, "", R.id.execution_toggle); execution.setContentDescription("자동 넘김 실행 켜기 또는 끄기");
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
