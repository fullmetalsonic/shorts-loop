# 사용 설명서 / User guide · ShortsLoop 0.5.1

0.5.1은 YouTube 광고 준비 패널을 추가한 설치용 업데이트이며 광고 자동 넘김은 아직 비활성이다. [0.5.1 설정·설치·남은 시험](APP_SETTINGS_0.5.1.md)을 먼저 확인한다. 아래0.5.0의 기존 조작은 그대로 적용된다. / Start with the0.5.1 guide; YouTube ads remain unsupported. Existing controls below are unchanged.

## 0.5.0 현재 사용법 / Current guide

홈에서 YouTube·Instagram·TikTok 버튼→해당 앱 사용ON→반복/특수 옵션을 설정하고, 홈 공통 권한을 준비한 뒤 하단 전체 실행을 켠다. 이전의 편집 탭과 별도 사용 앱 선택 영역은 앱별 상세 화면으로 통합했다. 뒤로가기는 실행을 켜지 않으며 미완성 숫자는 자동 적용하지 않는다.

Open a host from the home buttons,enable it and configure its rules;prepare shared permissions and start master execution. Separate editor tabs/host selection are replaced by per-app pages. Back navigation never starts execution or silently applies drafts.

TikTok도 광고 대기0.0–9.9초/0.1단위,사진 전체·한 장 각각0–10초/기본3초,적격 무진행 영상2–60초/기본3초,알려진 총길이의 긴 영상 필터를 지원한다. 광고OFF는 광고 전용 넘김만 끄며 다른 적격 일반/사진 규칙은 적용 가능하다. 실제 길이가 없으면 긴 영상 필터는 적용되지 않는다. TikTok LIVE/화면 분석은 미지원이다.

TikTok gains independent ad/photo/clockless/known-duration settings. AdsOFF disables the ad-specific rule,not other eligible rules. Unknown duration cannot qualify for long filtering;TikTok LIVE/visual remain unsupported.

화면별 버튼·입력 예·오류·복구는 [앱별 상세 설명서](APP_SETTINGS_0.5.0.md),[새 실사용 점검표](FIELD_TEST_0.5.0.md),[릴리스](releases/v0.5.0.md)를 따른다. 새 실폰 자동 넘김은NOT RUN이며 아래0.4.0 안내는 과거 UI 기록이다.

See the linked per-app guide for controls,examples,validation and recovery. New physical automation is unrun;the0.4.0 section below describes the historical UI.

## 이전0.4.0 빠른 사용법 / Historical quick guide

게시·최종 APK 상태는 [0.4.0 릴리스](releases/v0.4.0.md),검사 결과는 [검증 원장](VERIFICATION.md)을 확인하세요. 이번 버전은 폰 연결 종료 후 PC 검증으로 준비하며 신규 TikTok·세 앱 실폰 자동 넘김은 아직 미검증입니다.

See the release record for publication/artifact status and verification ledger for results. New TikTok and triple-host physical automation remain unverified.

1. 기존 앱을 삭제하지 않고 같은 서명의 업데이트를 설치합니다. 인앱 메뉴에서 설치 버전0.4.0을 확인합니다. 설치 승인은 직접 하며 실행은OFF 상태로 준비합니다. / Install over the existing same-signed app,confirm0.4.0 and keep executionOFF while configuring.
2. **사용할 앱 / Apps to use**에서 YouTube·Instagram·TikTok 중 설치된 지원 대상을 선택합니다. TikTok은 처음에는 선택OFF이며 `com.ss.android.ugc.trill`만 지원합니다. / Select supported installed hosts;TikTok is deselected initially and only this package is supported.
3. **앱별 설정 / Settings for each app**의 앱을 골라 횟수를 숫자로 입력하고 완료/적용합니다. 화살표도 사용 가능합니다. 예:YouTube2,Instagram1,TikTok2. 탭 선택은 자동화를 켜는 동작이 아닙니다. / Select an editor,apply a typed count or use arrows;tabs do not enable automation.
4. 필요한 독립 옵션을 설정하고 **사용 준비 / Setup**에서 접근성을 연결합니다. 플로팅은 선택 사항이며 사용할 때만 다른 앱 위 표시 권한이 필요합니다. 배터리 제한 없음 안내도 확인합니다. / Configure desired rules,accessibility,optional overlay permission and battery guidance.
5. 활성 창만 처리하려면 여러 앱 모드OFF,분할의 보이는 대상들을 처리하려면 **여러 앱 동시 적용 / Process multiple apps**를ON으로 합니다. 한 개/두 개/세 개 배치마다 다시 켤 필요는 없지만 안전한 창·실제 재생이 필요합니다. / ModeON follows one,two or three eligible visible hosts without forcing playback.
6. 하단 **전체 자동 넘김 실행 / Auto-advance**을 켜고 재생합니다. X는 해당 앱만 일시정지하고 인앱 해당 탭의 재개 버튼 또는 전체OFF→ON으로 재개합니다. 빠른 설정 짧은 터치는 전체ON/OFF입니다. / Enable master execution;X pauses one host,and the tile toggles all.

### 시간 설정 예 / Timing examples

| 설정 / Setting | 입력 예 / Example | 결과 / Result |
| --- | --- | --- |
| 일반 반복 / Ordinary count |2 | 처음 포함 총2회,중간 시작이면 다음 처음 대기 / Two complete plays including the first;may wait for a fresh beginning |
| Instagram 시간제 / Clockless timer |3초 | 판별2초 포함 총3초,진행정보 없는 지원 일반 영상만 / Three seconds total including2s qualification,eligible clockless videos only |
| 광고 대기 / Ad delay |1.3초 | 안전하게 확인된 같은 Instagram 광고를 추가1.3초 기다린 뒤 넘김 / Wait1.3s before skipping the identified ad |
| 광고 대기 / Ad delay |0.0초 | 추가대기 없음,안전 확인은 유지 / No added delay;guards remain |
| 라이브·사진 / Live and photos |0초 | 지원 콘텐츠에서 안전 확인 후 즉시 / Immediate after guards on supported content |

Instagram 시간제는 **2–60초·미설정3초**이며,기존에10초 등을 저장했다면 그 값이 유지됩니다. 토글은 기본OFF입니다.1초·0초·소수는 저장하지 않습니다. 광고 대기는 **0.0–9.9초·기본0.0초**,화살표는0.1초씩 움직입니다. `1.3`은13개의0.1초 단위이며 `9.99`는 거부합니다. 입력 초안은 완료/적용 전까지 저장값을 바꾸지 않습니다.

Clockless timing accepts whole2–60s,default3 only when unset;existing10s or other valid values stay unchanged. Ad delay uses0.0–9.9s in0.1s steps;1.3 is accepted,9.99 rejected. Drafts do not replace saved values.

**반복0회는 일반 반복·시간제·화면 분석을 끕니다.** Instagram 광고·사진,YouTube 라이브,YouTube/Instagram 긴 영상은 지원된 별도 옵션이ON이면 독립적으로 동작합니다. 광고만 넘기려면 반복0,광고ON,다른 독립옵션OFF,전체ON으로 설정합니다. 모든 것을 끄려면 전체OFF입니다.

**Count0 stops ordinary repeat,timer and visual counting.** Supported ads,photos,live and long-video filters remain independent. For ads only,use count0,adsON,other filtersOFF,masterON.

### TikTok와 멈춤 안내 / TikTok and waiting

TikTok은 추천 피드에서 진행값을 읽을 수 있는 일반 영상만 대상으로 하며 기본2회입니다. 진행값0–10000은 비율이지 영상 길이가 아닙니다. 진행정보 없음·광고·LIVE·사진·긴 영상 필터·시간제는 지원하지 않습니다. 지원하지 않는 옵션은 TikTok 탭에 표시하지 않습니다.

TikTok supports readable ordinary recommendation-video progress,default2 plays. Its0–10000 range is a proportion,not duration. Clockless timing and special-content filtering are unsupported and hidden.

카운트0은 설정값0과 다릅니다. 설정이2인데 현재0이면 시작점·진행정보를 기다릴 수 있습니다. 일반 초단위 영상의 시작 인정은 최대1.3초이면서 길이10% 이내이며 모든 초기화 원인을 해결한 것은 아닙니다. 광고 대기의 양수 설정은 같은 광고 식별 정보가 없으면 기다립니다. 광고 지연을 키우면 모든 다음 영상 시작 문제가 해결된다고 보장하지 않습니다.

Current0 differs from configured0:with target2,it may mean waiting for a beginning/progress. The1.3s/10% start tolerance is not a universal reset fix. Positive ad delays wait when identity is unavailable,and cannot promise to fix every next-video start.

정지 안내가 계속되면 댓글·메뉴·키보드·가림·PiP·앱 자체 일시정지를 확인합니다. 일반 영상에서 다시 **전체OFF→ON**으로 시작하세요. 특히 다음 TikTok 목적지 확인 실패 뒤에는 수동으로 정상 영상에 이동한 다음 다시 시작해야 할 수 있습니다. 접근성 연결 자체가 끊겼다면 Android 설정에서 이 앱 서비스를OFF→ON하고 돌아와 전체 실행을 켭니다. USB/디버깅은 일반 사용에 필요하지 않습니다.

Check menus,keyboard,obstruction,PiP and host pauses,then restart **masterOFF→ON** on a supported ordinary video. An unconfirmed TikTok destination may require manually choosing a normal video before restart. If accessibility disconnected,reconnect it in Android Settings. Normal use needs no debugging connection.

[짧은 현장 점검 순서](FIELD_TEST_0.4.0.md) · [상세 계약](PRODUCT_SPEC.md) · [업데이트](UPDATE_DELIVERY_PLAN.md).

## 이전0.3.0·0.2.x 사용법과 당시 시험 원문 / Historical guides and evidence

아래 당시 버전·시험·이전UI명·기본10초 등의 내용은 역사적 기록입니다. 충돌하는 설정값·메뉴는 위0.4.0 안내가 우선하며 이전 PASS는 이번 결과가 아닙니다.

Historical versions,tests,UI names and old defaults below are retained as evidence,not the current contract or fresh test results.

## 현재 안내 · 공개 완료 / Current guide · Published

**현재 공개 버전은0.3.0/code32입니다.** 53FD 흐름 검증 후보에서 실행·듀얼ON을 유지한 전체화면→두 앱 분할→전체화면 복귀와 실제 자동 전환을 확인했습니다. 숨겨진 앱에는 새 넘김 요청이 없었습니다. 현재 설치본은 실행 내용이 같고 내장 소스 기록만 다른 최종9AA1입니다. 빌드·단위·3개 OS 검사·설정 보존 업그레이드·설치 해시 확인을 마쳤으며,게시 전 전체 실행은OFF로 두었습니다. 최종 공개 파일·CI·익명 다운로드 동일성 확인도 완료했습니다. 이전 후보의 회전·일반 앱 병행 시험은 별도 이력입니다. [최신 검증 원장](VERIFICATION.md),[이전 후보별 이력](SPLIT_SCREEN_PLAN.md).

**The current public version is0.3.0/code32.** Physical-flow candidate53FD completed fullscreen→dual-host split→fullscreen transitions with execution/dualON throughout;the hidden host issued no new advance. The phone now runs final9AA1 with identical runtime payload and different embedded revision metadata. Build,unit,three-OS,settings-preserving upgrade and installation-hash checks passed. Execution remainsOFF;final public assets,CI and anonymous-download parity are verified. Earlier rotation and ordinary-app coexistence evidence remains separate in the [verification record](VERIFICATION.md).

듀얼 모드와 전체 실행을ON으로 유지하면 전체화면의 대상 앱 하나,분할 화면의 두 대상 앱,일반 앱 옆의 대상 앱 하나를 현재 보이는 창에 맞춰 감지합니다. 배치마다 모드를 다시 켜는 방식은 아닙니다. 창·권한·재생 상태가 안전 조건을 만족해야 하며,중지하거나 서비스가 다시 연결된 뒤에는 별도의 재개가 필요할 수 있습니다.

With dual mode and executionON,the app detects one fullscreen host,two split-view hosts,or one host beside an ordinary app according to the visible windows;layout changes do not require toggling the mode again. Window,permission and playback guards still apply,and stopping or service reconnection may require explicit resumption.

키보드·팝업 등 가림 창의 안전 보호와 앱 자체의 일시정지는 그대로 적용됩니다. 강제로 재생하지 않으며,모든 앱·배치의 동작을 보장하지 않습니다. 듀얼 사진 릴스의 새 실물 표본과 회전 후 원래 배치로의 복귀 시험은 아직 남아 있습니다.

Keyboard/popup guards and host-imposed pauses still apply;playback is never forced. This is not a guarantee for every app or layout. Fresh dual-window photo-Reel samples and the return-rotation test remain outstanding.

### 먼저 구분할 세 가지 / Three different controls

| 메뉴 / Control | 의미 / Meaning |
|---|---|
| 사용할 앱 / Apps to use | 자동화를 허용할 앱 선택. 하나 또는 둘 다 선택 / Select one or both automation targets |
| 앱별 설정의 YouTube·Instagram 탭 / App settings tabs | 편집할 앱 선택. 탭만 바꿔서는 실행이나 듀얼 모드가 켜지지 않음 / Choose what to edit, not what to run |
| 듀얼 화면 적용 / Use dual-window mode | 기본OFF: 현재 활성 창 하나. ON: 함께 보이는 선택 앱을 각각 감지 / DefaultOFF: active window only. ON: detect each visible selected host |

### 0.3.0 사용 순서 / Steps for0.3.0

1. 전체 실행을OFF로 두고 ‘사용할 앱’에서 사용할 대상을 선택합니다. 접근성·플로팅 권한은 필요한 경우 직접 연결합니다. / Leave executionOFF, choose target apps, and manually connect required permissions.
2. YouTube 탭에서 반복 횟수·긴 영상 기준·플로팅 터치 방식을 정하고 완료/적용합니다. Instagram 탭에서 같은 항목을 따로 정합니다. 예: YouTube2회·긴 영상60초, Instagram1회·긴 영상120초. / Set and apply each tab separately, for example YouTube2 plays/60s length threshold and Instagram1 play/120s threshold.
3. Instagram의 시간제·사진·광고는 Instagram 탭에서, YouTube 라이브는 YouTube 탭에서 설정합니다. / Configure Instagram timer,photo and ad rules in its tab,and YouTube live previews in the YouTube tab.
4. **듀얼OFF는 현재 활성 창만 처리합니다.** 반대쪽 창에서 일반 앱을 조작하는 동안에도 보이는 쇼츠 창을 처리하려면 듀얼을ON으로 합니다. 선택한 대상 앱이 한 개만 보여도 동작하며, 두 쇼츠 앱을 반드시 함께 열 필요는 없습니다. YouTube와 Instagram을 모두 처리하려면 두 앱을 선택하고 듀얼을ON으로 합니다. / **DualOFF processes only the active window.** Turn dualON to process a visible Shorts/Reels window while using an ordinary app in the other pane. One visible selected host is enough;two video apps are not required. Select both hosts and turn dualON to process both YouTube and Instagram.
5. 필요하면 공통 플로팅 표시를 켭니다. 마지막에 하단 전체 실행을 직접 켭니다. 모드·설정 변경만으로 실행이 켜지지 않습니다. / Optionally show floating controls, then explicitly turn on overall execution.
6. 각 앱의 상태와 실제 재생을 확인합니다. 앱이 자체적으로 일시정지한 영상은 강제로 재생하지 않습니다. / Check each app’s status and actual playback; the app does not force a host-paused video to play.

탭을 바꾸거나 듀얼을 켜고 꺼도 저장한 앱별 설정은 유지됩니다. 탭 사이의 입력 초안도 서로 섞이지 않지만, 앱을 종료하기 전에는 완료/적용하세요. 0회는 **해당 앱의 일반 반복과 시간제·화면 분석**을 중지하며, 별도 긴 영상·광고·라이브·사진 옵션의 의미는 유지합니다.

Changing tabs or dual mode preserves saved per-app values and keeps the two editor drafts separate. Apply drafts before exiting. Zero plays stops that app’s ordinary repeat, timer and visual counting; its supported independent filters remain separate.

### 두 플로팅과 중지 범위 / Floating controls and stop scope

- 각 대상 창에 앱 표시가 있는 반투명72×56dp 플로팅을 사용합니다. 듀얼에서 두 대상 창이 유효하면 두 개를 표시하며, 창이 없거나 안전한 공간이 부족하면 해당 플로팅은 숨깁니다. / Each eligible host has a labelled translucent72×56dp control; unavailable or too-small windows remain hidden.
- 숫자 터치는 해당 앱의 횟수만 바꾸고, 끌기는 해당 앱의 위치만 저장합니다. 위치는 현재 창 안으로 보정됩니다. / Taps and drags affect only that app’s count and saved position.
- **X는 해당 앱만 일시정지**합니다. 그 앱의 설정 탭에서 ‘이 앱 다시 시작’을 누르면 재개합니다. 전체 실행이OFF이면 이 버튼만으로 전체 실행을 켜지 않습니다. / X pauses only its host; Resume this app resumes it without enabling an off master switch.
- **하단 전체OFF와 실행 타일OFF는 양쪽 모두 중지**합니다. 전체OFF→ON은 선택한 앱들을 각 기준 횟수로 다시 시작합니다. 표시OFF는 플로팅만 숨깁니다. / OverallOFF or tileOFF stops both; overallOFF→ON resumes selected hosts at their configured counts. Hiding controls alone does not stop execution.
- X는24×24dp의 작은 터치 영역입니다. 누르기 어렵다면 인앱 전체 실행 스위치를 사용하세요. / The24×24dp X remains a small target; use the in-app master switch when needed.

화면 분석 보조는 계속 실험 기능·Android14 이상·별도 동의 대상입니다. **두 대상 창이 함께 보이는 동안 분석은 동작하지 않으며 저장한 선택은 보존**합니다. 듀얼 모드의 정확한 반복 횟수를 화면 분석으로 대신 보증하지 않습니다. 권한·설치·서명 검사를 우회하지 마세요.

Visual assistance remains experimental,Android14+ and separately consented. **It is inactive while both target windows are visible,without erasing the saved choice.** It does not certify accurate dual-host playback counts.

## 공개0.2.9 사용법·이전 검증 원문 / Published0.2.9 guide and historical evidence

아래는0.2.9와 이전 버전의 사용법·검증 기록입니다. 해당 버전의 단일 플로팅X·공통 반복 설정·화면 순서는 위0.3.0 앱별 계약보다 우선하지 않으며, 당시 PASS를0.3.0의 PASS로 재사용하지 않습니다.

The retained sections below describe0.2.9 and older versions. Their single-control X,shared-repeat settings and layout are historical where superseded above;their test results are not0.3.0 evidence.

**ShortsLoop 0.2.9/code31**의 한국어·영어 사용 안내입니다. 기존 영상 규칙·설정·패키지·서명을 보존하며 선택형 사진 두 모드를 추가합니다. 최신 APK의 두 모드·0/3/10초·댓글창 보호를 실폰에서 확인했습니다. 번호 없는 사진·사진 직후 광고·혼합 사례는 미확보이고 0.2.9를 공개했습니다. [사진 설정·예시·한계](PHOTO_REELS.md),[검증 상태](VERIFICATION.md),[언어·메뉴](LOCALIZATION.md).

This guide covers **ShortsLoop0.2.9/code31** in Korean and English,including optional photo modes while preserving existing settings,package and signer. The latest APK has physical evidence for both modes,0/3/10s and comments protection. Unreadable-index,photo-to-ad and mixed cases remain unobserved;publication is complete. See the photo guide,verification and [README](../README.md).

인앱 하단은 `ShortsLoop 0.2.9`,업데이트 카드는 `설치 버전 0.2.9` 또는 `Installed version 0.2.9`입니다. 앱 버전에 ‘시험판/정식/stable/trial’ 같은 등급을 붙이지 않습니다. 이는 배포물의 `debuggable=false` 보장이나 기능의 검증 범위를 대신하지 않습니다. 선택형 **화면 분석 보조의 실험 기능 안내·별도 동의·Android14 이상 제한**은 그대로 유지합니다.

The footer uses `ShortsLoop 0.2.9`,and the update card uses `Installed version 0.2.9` or its Korean equivalent,without release-grade suffixes. Neutral wording is not a substitute for verifying `debuggable=false` or feature coverage. Optional **visual-assistance experimental warnings,separate consent and the Android14+ restriction** remain.

## 표시 언어와 메뉴 찾기 / Language and finding menus

시스템 첫 언어가 한국어이면 한국어, 그 밖에는 영어입니다. **일본어→한국어 순서도 영어**입니다. 앱 내부·플로팅·실행 타일은 이 규칙을 따르지만 Android가 직접 표시하는 런처·접근성 이름 및 시스템 권한·설치 창은 OS의 기본 언어 선택을 따를 수 있습니다. 언어를 바꿀 때는 입력을 완료/적용하고 전체 실행을 끈 뒤 Android 설정에서 원하는 언어를 첫 번째로 옮기세요. 앱으로 돌아와 표시를 확인한 다음 전체 화면에서 필요한 때 실행을 다시 켭니다. 안전정지 안내이면 화면을 확인하고OFF→ON하세요. 저장한 옵션·위치는 언어 변경만으로 지우지 않습니다.

Korean as the first system language selects Korean; every other first language selects English, **including Japanese→Korean**. App-owned display, floating controls and runtime tiles follow this rule; Android-owned launcher/accessibility labels and system dialogs may follow OS fallback instead. Apply edited values and stop main execution before reordering system languages. Return, check the display, then enable execution on a full-screen supported video. If safety-stopped, inspect the screen and toggle OFF→ON. Language changes do not erase saved options or position.

자주 쓰는 메뉴는 **일반 영상 · 횟수로 넘김 / Ordinary videos · Play count**, **사용할 앱 / Apps to use**, **사용 준비 / Setup**, **전체 자동 넘김 실행 / Auto-advance**, **업데이트 · 앱 정보 / Updates · App information**입니다. 메뉴의 위치와0회·0초의 의미는 언어별로 같습니다. 전체 메뉴·오류·플로팅 설명은 [언어 안내](LOCALIZATION.md)를 참고하세요.

The bilingual menu names above identify the same controls in the same positions. Zero plays and zero seconds retain their distinct meanings. UI translation does **not** guarantee host detection in every language: existing readers focus on Korean/English labels, supported clocks and accessibility structures. See the localization guide for the full menu map, floating labels and errors.

## 업데이트할 때 알아둘 점

0.2.9도 디버깅 비활성 release APK와 기존 설치본의 서명 일치를 검증한 뒤 배포합니다. 공개 검증이 완료된 파일을 기존 앱 위에 설치하며,설정 보존을 위해 먼저 삭제하지 마세요. 설치 후 자동 넘김 실행은 직접 켭니다. 이전0.2.8의 검증은 [과거 기록](releases/v0.2.8.md)이며0.2.9의 결과로 재사용하지 않습니다. [현재 검증 상태](VERIFICATION.md),[배포 점검 기록](RELEASE_PRESENTATION_AUDIT.md).

Version0.2.9 requires verification of its non-debuggable release artifact and existing signer before distribution. Install the verified same-signed APK over the existing app; do not uninstall first if you want to retain settings. Enable execution yourself after installation. Historical0.2.8 verification does not establish0.2.9 readiness.

업데이트 조회 정책은 바꾸지 않습니다. 고정 저장소의 공개 릴리스 목록에서 draft를 제외하고 **공개된 prerelease도 포함**해,유효한 메타데이터·더 높은 versionCode·기기 OS 호환성을 만족하는 항목을 확인합니다. 설치 전 크기·SHA256·패키지·버전·현재 설치본과 같은 서명 집합을 검사하고 Android의 최종 설치 확인을 거칩니다. 앱의 중립적인 버전 표시가 prerelease를 제외한다는 뜻은 아닙니다.

Update selection is unchanged:exclude drafts from the fixed repository's public release feed,but **include published prereleases** with valid metadata,a higher versionCode and compatible OS. Size,SHA256,package,version and the installed signer set are checked before Android's final installation confirmation. Neutral version wording does not imply excluding prereleases.

현재 기능은 일반 반복·긴 영상·Instagram 시간제·Instagram 광고·YouTube 라이브로 구분합니다. 반복0은 일반 반복·시간제·화면 분석을 멈추지만 긴 영상·광고·라이브의 독립 옵션을 끄는 것은 아닙니다. 모든 자동 동작을 중지하려면 전체 실행을OFF로 합니다. 화면 분석 보조는 기본OFF·별도 동의·Android14 이상 실험 기능이며 정확한 총N회를 보장하지 않습니다.

## 1. 처음 사용할 때의 순서

1. 공개 완료된 릴리스의 배포 APK·해시를 확인하고 설치합니다. **0.2.4에는 업데이트 메뉴가 없으므로 브라우저 등에서 새 APK를 직접 받아 기존 앱 위에 설치해야 합니다.** 0.2.5 이상은 인앱에서 호환되는 상위 공개 버전을 확인할 수 있습니다. 삭제 후 재설치하면 설정이 사라지므로 먼저 삭제하지 마세요. ‘쇼츠 자동 넘김’을 엽니다. 기존 YouTube·Instagram은 수정하지 않습니다.
2. ‘사용할 앱’에서 YouTube 쇼츠, Instagram 릴스를 선택합니다. 둘 다 선택할 수 있습니다.
3. ‘일반 영상 · 횟수로 넘김’에서 총 재생 횟수를 정합니다. 처음에는 `2` 입력 후 완료/적용하면 됩니다.
4. 긴 일반 영상을 건너뛰려면 ‘긴 영상 건너뛰기’를 켜고 총길이 기준을 정합니다. 기본OFF·기준60초·입력1~3600초이며,반복0회와 별도로 동작합니다.
5. 필요하면 ‘진행 정보 없는 영상 · 시간제로 넘김’을 켭니다. 기본 OFF·10초이며 Instagram의 지원되는 정보 없는 영상만 대상으로 합니다.
6. 광고도 넘기려면 별도 ‘광고 · 바로 넘김’에서 광고 옵션을 켭니다. 기본 OFF이며 반복 0회에서도 동작하는 별도 기능입니다.
7. YouTube 라이브 미리보기도 넘기려면 별도 라이브 옵션을 켭니다. 기본 OFF이며 0~60초 중 0초는 인식 후 바로 넘기기입니다.
8. ‘플로팅 리모컨’의 ‘화면 위에 숫자 표시’를 정합니다. 숫자가 떠 있는 것이 싫으면 꺼도 됩니다. ‘사용 준비’에서 필요한 권한만 직접 연결합니다.
9. 아래 고정된 ‘전체 자동 넘김 실행’을 켜고 선택한 앱의 쇼츠/릴스를 전체 화면으로 재생합니다. 정상 영상 중간부터 켰다면 다음 처음 재생부터 셉니다.

화면 위쪽은 설정, 아래쪽 고정 영역은 실행과 상태 확인용입니다. 길게 스크롤하더라도 실행을 끄기 위해 맨 위로 돌아갈 필요가 없습니다.

본문 순서는 **일반 영상 횟수 → 긴 영상 건너뛰기 → 정보 없는 영상 시간제 → 광고 → YouTube 라이브 → 플로팅 → 사용할 앱 → 사용 준비 → 업데이트 · 앱 정보 → 실험 기능 → 도움말**입니다. 자주 바꾸는 설정을 먼저 두고, 처음 한 번 연결하는 준비 항목은 아래에 둡니다. 접근성이 연결되지 않았으면 상단 ‘접근성 연결 확인 · 해결 방법 보기’, 그 밖의 앱 선택·표시 권한이 부족하면 ‘사용 준비가 필요합니다 · 바로가기’를 표시합니다. 새 버전 배너는 적용 가능한 업데이트가 확인된 경우에만 표시합니다. 하단 ‘전체 자동 넘김 실행’은 자동 넘김 기능을 한꺼번에 켜고 끄는 메인 스위치이며, 업데이트 확인 스위치와는 별개입니다.

## 2. 필요한 권한만 연결하기

### 접근성: 자동 넘김을 사용할 때 필요

1. ‘사용 준비’에서 ‘접근성 설정·다시 연결’을 누릅니다.
2. 화면 읽기·조작 권한 안내를 읽고 설정을 엽니다.
3. Android 설정 → 설치된 앱 또는 설치된 서비스 → 쇼츠 자동 넘김 → 사용을 직접 켭니다. 제조사에 따라 이름과 경로는 다를 수 있습니다.
4. 이 앱으로 돌아와 연결 상태를 확인합니다. 연결되면 접근성 설정 버튼은 숨겨집니다.

접근성은 화면 정보를 읽고 스와이프를 실행할 수 있는 강한 권한입니다. 영상 내용·계정·시청이력은 저장·전송하지 않습니다. 0.2.5의 인터넷 사용은 GitHub 업데이트 확인·다운로드에 한정합니다. 다른 앱의 접근성 권한은 변경하지 않습니다.

**화면 분석 보조를 꺼도 접근성 연결은 필요합니다.** 기본 진행 정보·광고·라이브 표시를 읽고 다음 영상으로 넘기는 데 사용하기 때문입니다. 다른 앱 위 표시 권한은 플로팅 표시를 켠 경우에만 별도로 필요합니다.

Android 설정에서 접근성이 ON이어도 OS가 프로세스를 종료한 뒤 실제 서비스 연결이 끊길 수 있습니다. code17부터 준비 안내에도 이 복구 순서를 표시합니다. 앱이 연결되지 않았다고 표시하면 ‘접근성 설정·다시 연결’에서 Android 설정을 열어 **이 앱의 접근성을 OFF→ON**하고 돌아와 연결 상태를 확인하세요. 재연결 후 전체 실행은 안전을 위해 OFF이므로 하단 실행 토글도 직접 다시 켜야 합니다. 안내 버튼은 권한·실행 상태를 자동 변경하지 않습니다. 화면 분석 토글과 접근성 서비스 토글을 혼동하지 마세요.

### 다른 앱 위 표시: 플로팅을 사용할 때만 필요

- 플로팅 표시를 켜면 ‘다른 앱 위 표시 허용’ 버튼에서 이 앱의 권한을 직접 켭니다.
- 플로팅 표시를 끄면 이 권한이 없어도 접근성만으로 자동 넘김을 사용할 수 있습니다.
- 실행 중 플로팅 표시를 새로 켰는데 표시 권한이 없으면 실행이 중지될 수 있습니다. 권한을 허용하거나 플로팅 표시를 끈 뒤 실행 토글을 다시 켜세요.
- 연결이 완료된 권한 버튼은 숨기고 현재 준비 상태만 보여 줍니다. 권한을 취소하려면 Android의 해당 앱 설정을 사용합니다.

### 인터넷과 앱 설치: 업데이트용

0.2.5는 `INTERNET`으로 GitHub의 공개 업데이트 정보·APK만 받습니다. GitHub 계정 로그인이나 영상·계정·시청 기록 업로드는 없습니다. ‘앱 열 때 새 버전 확인’을 끄면 자동 조회를 중지할 수 있으며, 자동 넘김 자체는 업데이트 조회에 성공해야 하는 기능이 아닙니다.

`REQUEST_INSTALL_PACKAGES`는 내려받은 업데이트를 Android 설치 창으로 연결하기 위한 선언입니다. 실제 설치가 필요할 때 사용자가 Android의 ‘이 출처 허용’을 직접 켜고 최종 설치를 확인합니다. 허용하지 않아도 기존 자동 넘김 기능은 사용할 수 있습니다. 시스템 알림 권한·저장소 전체 접근·자동 무인 설치는 사용하지 않습니다. 자세한 순서는 10절을 참고하세요.

## 3. 반복 횟수 입력과 수정

‘반복 횟수’는 추가 반복 횟수가 아니라 **처음 재생을 포함한 총 재생 횟수**입니다. 범위는 0~99회입니다.

| 입력 | 의미 | 정상 추적 중 표시 예 |
|---|---|---|
| 0 | 일반 반복·시간제 중지. 긴 영상·광고·라이브는 별도 옵션에 따름 | 독립 옵션 상태 안내 |
| 1 | 처음부터 끝까지 총 한 번 본 뒤 넘김 | `1/1` |
| 2 | 첫 재생과 한 번의 재재생 후 넘김 | `1/2` → `2/2` |
| 5 | 처음부터 총 다섯 번 본 뒤 넘김 | `1/5` → … → `5/5` |

### 숫자를 직접 입력

1. 큰 숫자 칸을 누릅니다. 기존 숫자를 선택해 바꿀 수 있습니다.
2. 예를 들어 `5`를 입력합니다.
3. 키보드의 ‘완료’ 또는 ‘입력한 횟수 적용’을 누릅니다.
4. ‘현재 적용: 5회’로 바뀌었는지 확인합니다.

타이핑 중 숫자는 아직 임시 입력입니다. 기존 값을 지워 빈칸이 되어도 0회로 저장되지 않습니다. 실행 토글을 켤 때에도 임시 입력이 있으면 먼저 확인·적용합니다. 앱이 종료되거나 화면이 다시 만들어지기 전에는 ‘완료/적용’으로 확정하세요. 확정하지 않은 입력의 재시작 복원은 보장하지 않습니다.

### ▲▼로 한 회씩 조절

- ▲는 한 회 늘리고 ▼는 한 회 줄입니다. 화살표를 누르면 즉시 적용됩니다.
- `5`에서 ▲를 누르면 `6`, ▼를 누르면 `4`가 됩니다.
- 0에서 ▼, 99에서 ▲는 더 진행하지 않습니다.
- 입력 중인 값이 유효하면 그 값에서 한 회 조절합니다. 빈칸이나 `100` 같은 값이면 먼저 오류를 고쳐야 합니다.

### 잘못 입력했을 때

빈칸, 음수, 소수, 문자 또는 100 이상은 적용하지 않습니다. ‘0~99 사이의 정수를 입력해 주세요’ 안내가 나타나면 올바른 숫자로 고친 뒤 적용하세요. 입력 오류가 기존의 저장값을 0으로 바꾸지는 않습니다.

기준 횟수를 확정하면 현재 적용 횟수도 같은 값으로 바뀌고 완주 횟수를 새로 셉니다. 실행 중에 바꾸면 이전 횟수를 이어 세지 않습니다.

## 4. 기준 횟수와 현재 적용 횟수

기준 횟수는 인앱에서 정한 최대값이고, 현재 적용 횟수는 플로팅에서 선택한 실제 값입니다. 두 값을 따로 저장합니다.

예를 들어 기준을 5회로 정하고 플로팅에서 현재 값을 2회로 바꾸면 다음과 같습니다.

- 인앱: ‘기준 5회 · 플로팅 현재 2회’.
- 플로팅: 첫 재생 `1/2`, 두 번째 재생 `2/2`.
- 두 번 완주하면 다음 영상으로 이동합니다. 플로팅의 분모는 기준 5가 아니라 현재 값 2입니다.
- 기준 5는 그대로이므로 다시 플로팅을 눌러 3·4·5회를 선택할 수 있습니다.
- 실행을 껐다 켜면 현재 값은 기준 5회로 돌아갑니다. 인앱과 빠른 설정 모두 같습니다.

## 5. 플로팅 리모컨

### 두 가지 터치 방식

`광·라`(영어 `A+L`)는 **광고·라이브 옵션이 모두 켜짐**을 뜻합니다. 일반 반복이0회여도 두 옵션은 별도로 동작합니다. 재생 횟수나 현재 영상 종류를 뜻하는 표시는 아닙니다. / `A+L` (`광·라` in Korean) means both ad and live skipping are enabled independently of zero ordinary plays; it is not a play count or content classification.

0회에서 긴 영상 옵션도 활성이라면 ‘조건’(영어 `Filters`)으로 표시합니다. 이는 길이 조건 등의 독립 옵션이 켜졌다는 뜻이며 실제 현재 영상이 길다는 판정 자체는 아닙니다. / At zero plays, “Filters” (`조건` in Korean) indicates an active long-video option; it does not classify the current video as long.

플로팅 표시를 켜면 터치 방식이 나타납니다. 표시를 끄면 관련 세부 설정을 숨기지만 선택한 방식은 보존합니다.

| 방식 | 기준 3회일 때 숫자를 누르면 | 적합한 사용 |
|---|---|---|
| 횟수 순환 | `0 → 1 → 2 → 3 → 0` | 영상마다 횟수를 자주 바꿀 때 |
| 반복 켜기·끄기 | `0 ↔ 3` | 일반 영상 반복·시간제를 잠시 멈출 때. 긴 영상·광고·라이브는 별도 |

터치 방식을 바꾸면 현재 적용 횟수는 기준값으로 맞추고 횟수를 새로 셉니다. 기준이0이면 어느 방식을 눌러도0입니다. 일반 반복·시간제를 다시 사용하려면 인앱 기준 횟수를 양수로 바꾸세요. 긴 영상·광고·라이브 옵션은 이 횟수 전환과 별개이므로 모든 자동 넘김을 멈추려면× 또는 전체 실행 토글을 끕니다.

### 이동·닫기·표시 끄기의 차이

- 숫자를 짧게 누르면 위에서 선택한 방식으로 값이 바뀝니다.
- 숫자를 잡고 끌면 위치가 이동합니다. 놓은 상대 위치를 저장하며 드래그를 짧은 탭으로 처리하지 않습니다.
- 오른쪽 위 `×`는 전체 실행을 끄고 플로팅을 닫습니다. 표시 사용 설정·기준값·위치는 보존합니다.
- 인앱 ‘화면 위에 숫자 표시’를 끄면 플로팅만 숨깁니다. 실행 요청과 현재 적용값은 유지합니다. 표시 설정 자체는 완주 카운트를 초기화하지 않습니다.
- 다만 설정 앱을 여는 등 대상 영상 앱을 벗어나는 행동은 기존 앱 전환 보호에 따라 대기·재집계됩니다.

플로팅은 가로 72dp×세로 56dp로 이전 124dp 폭보다 약 42% 작습니다. 배경은 약 40% 불투명도이고 숫자·X는 배경보다 선명합니다. 투명도 조절 슬라이더는 없습니다.

X는 폭 축소를 위해 24dp×24dp로 작게 만들었습니다. 일반적인 48dp 터치 목표보다 작으므로 누르기 어렵다면 인앱 하단 토글이나 빠른 설정으로 종료하세요. 숫자 터치·드래그 영역은 48dp×56dp입니다. 접기·펼치기·회전 시 상대 위치를 화면 안으로 보정하지만 실제 전환 검증 결과는 검증 문서를 확인하세요.

## 6. 사용할 앱 선택

| 선택 상태 | 동작 대상 |
|---|---|
| YouTube만 선택 | 일반 YouTube 앱의 쇼츠만 |
| Instagram만 선택 | Instagram 앱의 지원되는 동영상 릴스만 |
| 둘 다 선택 | 현재 앞에 보이는 선택 앱의 쇼츠 또는 릴스 |
| 모두 해제 | 실행할 수 없음. 실행 중 마지막 선택을 해제하면 중지 |

앱을 바꾸면 이전 영상의 카운트를 새 앱으로 넘기지 않습니다. 선택하지 않은 앱, 일반 YouTube 동영상, Instagram 홈 게시물·DM 등을 자동 조작하도록 만든 기능이 아닙니다. Instagram이 설치되지 않으면 선택 항목에 ‘미설치’를 표시하고 사용할 수 없게 합니다.

일반 Instagram 릴스는 재생 진행값을 읽을 수 있는 단일 동영상을 대상으로 반복 횟수를 셉니다. 정상 정보가 있으면 시간제보다 횟수 계산이 우선입니다. 재생 정보가 없는 지원 단일 영상만 아래 선택형 시간제를 사용할 수 있습니다. 확인된 사진은 별도 사진 설정을 따르며 미지원 혼합 콘텐츠·영상 구분 정보를 읽지 못하는 화면은 대기합니다. 재생바가 없다고 광고인 것은 아닙니다.

### 긴 영상 건너뛰기

일반 횟수 카드 바로 아래에 있으며 기본OFF입니다. YouTube·Instagram 중 설치되어 있고 ‘사용할 앱’에서 선택한 앱이 하나 이상이어야 조작할 수 있습니다. 선택 해제로 비활성화해도 옵션과 기준값은 지우지 않습니다.

1. **긴 영상 건너뛰기**를 켭니다. 이 토글만 켜면 전체 실행이 시작되지는 않습니다.
2. **건너뛸 영상의 최소 총길이(초)**에1~3600 정수를 입력하고 키보드 완료 또는 **입력한 긴 영상 기준 적용**을 누릅니다. −/+는1초씩 적용합니다. 기본60초는1분 이상 영상을 뜻합니다.
3. 빈칸·0·음수·소수·숫자가 아닌 값·3601 이상이면 저장하지 않으며 이전 확정값을 유지합니다. 오류를 고친 후 다시 적용하세요. 화면 상태를 새로 읽어도 편집 중 초안을 덮어쓰지 않습니다.
4. 전체 실행을 켜고 선택 앱의 일반 쇼츠/릴스를 재생합니다. **총길이≥기준**이면 같은 페이지와 실제 전진 재생을 안정 확인한 뒤 넘깁니다. 기준60초라면59초는 기존 횟수,60초와120초는 긴 영상 대상입니다.

이 값은 기다릴 시간이 아니라 **영상 전체 길이 기준**입니다. 재생 처음이나 완주를 기다리지 않지만, 정지0초·일시정지·전환 중 화면에서는 안전 확인을 생략하지 않습니다. 길이를 읽을 수 없으면 추측해서 넘기지 않으며, 진행 정보 없는 지원 Instagram 영상은 기존 시간제 옵션을 따릅니다. 반복0에서는 시간제는 멈추지만 긴 영상 옵션은 독립적으로 동작합니다. 전체OFF는 긴 영상까지 모두 멈춥니다.

긴 영상 이동 확인이4.5초 안에 되지 않으면 안전정지합니다. 일반 N회 경로의 재인식 복구로 실패를 우회하거나 같은 영상을 연속 요청하지 않습니다. 광고·라이브도 긴 영상으로 추정하지 않습니다.

code28은 YouTube 페이지의 행 번호가 요청 때보다 정확히1 증가하는지도 확인합니다. 같은 창·pager·페이지 영역,달라진 콘텐츠 키와300ms 이상 안정·실제 전진 재생 검사는 그대로입니다. 행을 모두 읽었는데 같거나 뒤로 가거나2칸 이상 뛰면 총길이가 달라도 확인하지 않습니다. 행 정보 없음은 이동 근거가 아니며,깨진 구조·갱신 실패는 안전정지 조건입니다. 이는 code26의 동일 길이 안전정지를 보완한 기존 동작입니다. 과거code28의 YouTube20회는 통과했지만 같은 길이 쌍은 그20회에서 나오지 않았으며,이 결과를0.2.9 신규 시험으로 표시하지 않습니다. 조건별 근거는 검증 문서를 확인하세요.

This independent option skips ordinary videos whose known total duration is at least the configured1–3600-second threshold(default60). It is not a waiting timer. Stable-page and actual-forward-playback checks still apply; unknown duration,ads and live previews do not qualify. At zero plays, long-video skipping can run but the Instagram timer remains stopped. An unconfirmed long-video transition hard-stops after4.5 seconds and does not enter ordinary recount recovery.

### 진행 정보 없는 영상 · 시간제로 넘김

1. ‘사용할 앱’에서 Instagram을 선택합니다. 선택을 해제하면 시간제 설정은 비활성화되지만 저장된 옵션·초 값은 보존합니다.
2. ‘시간제 넘김’ 토글을 켭니다. 기본은 OFF이고 초 값의 초기값은 10입니다.
3. 숫자 칸에 `10`을 입력한 뒤 완료/적용합니다. −/+는 한 번에 1초씩 즉시 적용하며 범위는 5~60초입니다.
4. 현재 반복 횟수를 1 이상으로 두고 ‘전체 자동 넘김 실행’을 켭니다.
5. 정상 영상은 `1/2` 같은 횟수를 표시하고, 정보 없는 지원 영상만 `10초 → 9초`처럼 남은 시간을 표시합니다. 이동 요청 중에는 ‘다음’을 표시합니다.

예: 반복 2회·시간 10초이면 정상 영상은 총 2회, 정보 없는 영상은 **총 10초** 후 넘깁니다. 시간에 반복 횟수를 곱하지 않습니다. 초 값을 20으로 바꾸면 총 20초이지 기존 10초에 20초를 더한 값이 아닙니다. **초 설정은 횟수와 독립 저장되지만, 반복 0회에서는 시간제도 중지**합니다.

빈칸·부호·소수·문자·5 미만·60 초과는 저장하지 않습니다. `61` 오류가 뜨면 `60` 이하로 고친 뒤 적용하세요. 기존 확정값은 보존합니다. 타이핑한 초안은 주기적인 상태 갱신으로 덮어쓰지 않으며, 시간제 ON 또는 실행 ON 때 필요한 유효성 검사를 통과해야 적용됩니다. 오류를 고치거나 해당 옵션을 끄고 사용하세요.

정상 영상에서 중간 재생으로 시작해 `0/N`으로 다음 처음을 기다리는 것은 ‘정보 없음’이 아니므로 시간제로 바꾸지 않습니다. 감지된 일시정지·댓글·메뉴·잠금·앱/창 변경·플로팅 조작은 타이머를 초기화하고, 안전한 영상으로 돌아오면 처음부터 다시 셉니다. 조회가 끊긴 시간을 누적해 복귀 즉시 넘기지 않습니다. 사진·혼합 콘텐츠·광고는 이 타이머로 우회하지 않습니다.

이 기능은 영상의 길이나 끝을 알아내지 않습니다. 영상이 길면 완주 전에 넘길 수 있고, 짧으면 여러 번 재생될 수 있습니다. 모든 일시정지·앱 UI 변형을 감지하는 것은 아니므로 정확한 완주가 중요하면 시간제를 끄세요. 새 화면 캡처·마이크·오디오 권한은 필요하지 않습니다. [세부 동작·과거 후보 A/B 검증](TIMED_FALLBACK.md).

### 실험 기능 · 화면 분석 보조

Android 14 이상에서 Instagram을 선택했을 때 별도 동의로 켤 수 있으며 기본 OFF입니다. 기기 RAM에서 화면 반복을 비교하며 저장·전송하지 않습니다. 취소하면 켜지지 않습니다. 처음 학습 때문에 추가 재생될 수 있고 `…/N`은 학습/대기, `~n/N`은 추정이므로 정확한 N회를 보장하지 않습니다.

시간제와 화면 분석을 둘 다 켜면 **시간제가 우선하고 화면 분석은 하지 않습니다**. 화면 분석 선택 자체는 보존하므로 시간제를 끄면 선택했던 보조 경로가 사용될 수 있습니다. 별도 미연결 전체 구간 비교 실험이나 오디오 진단과 같은 기능이 아닙니다. [시험 사용법·한계](VISUAL_ASSIST_TRIAL.md).

### Instagram 광고 바로 넘기기

1. ‘사용할 앱’에서 Instagram을 선택합니다.
2. 별도 ‘광고 · 바로 넘김’에서 ‘광고 바로 넘기기’를 켭니다. Instagram이 선택되지 않으면 이 토글은 조작할 수 없습니다.
3. 하단 ‘전체 자동 넘김 실행’을 켭니다. **광고는 반복 0회에서도 동작합니다.**
4. Instagram 릴스 화면에서 광고임을 확인하면 완주 횟수를 기다리지 않고 다음 페이지로 한 번 이동을 요청합니다.

광고 옵션만 켜서는 실행이 시작되지 않습니다. **전체 실행ON + Instagram 선택 + 광고 옵션ON**을 모두 만족해야 합니다. 반복 횟수는 광고의 실행 조건이 아니며, 광고만 쓰려면 **반복0회 + 광고ON + 긴 영상·라이브·사진OFF + 전체 실행ON**으로 설정합니다. 실행OFF, 광고 옵션OFF 또는 Instagram 선택 해제에서는 광고를 넘기지 않습니다. Instagram 선택을 해제해도 광고 옵션 값은 보존되므로 다시 선택했을 때 기존 설정을 확인하세요.

광고 옵션을 끄면 광고는 직접 넘겨야 합니다. 일반 릴스의 반복 횟수 설정을 끄는 것은 아닙니다. 앱의 토글 바로 아래에도 이 차이를 안내합니다.

광고 옵션 자체는 일반 릴스의 반복 횟수를 바꾸지 않습니다. 긴 영상 옵션이 켜져 있고 조건을 만족하면 일반 릴스도 길이 기준에 따라 완주 전에 넘길 수 있습니다. 광고 옵션은 YouTube 광고에 적용되지 않으며 광고를 제거하거나 차단하는 기능도 아닙니다. 광고 링크·‘자세히 보기’·구매 버튼은 누르지 않고 릴스 화면의 다음 페이지 동작만 요청합니다.

영상 위에 팝업처럼 ‘더 알아보기’ 카드가 붙은 릴스 광고도, 광고 구조를 확인하면 카드가 아니라 광고 페이지 전체를 넘깁니다. 별도 창으로 열린 광고 팝업의 X를 찾아 자동으로 닫는 기능은 구현하지 않았으며 지원 범위도 확정하지 않았습니다. 둘을 같은 기능으로 보지 마세요.

광고마다 표시 구조가 달라 모든 광고를 인식한다고 보장하지 않습니다. 이동 요청 뒤 최소1.2초 이후 새 페이지인지 확인하고,4.5초 안에 확인하지 못하면 안전 정지합니다. 다음 광고가 똑같은 식별 정보로 보이는 연속 광고는 실제 이동 여부를 구분할 수 없어 멈출 수 있습니다. 자동으로 무한 재시도하지 않으므로 이때는 직접 넘긴 뒤 상태를 확인하고 실행을 껐다 켜세요.

이미 이동을 요청하고 확인하는 도중에 플로팅 드래그·화면 회전·앱 전환 등으로 관측이 끊기면 재요청을 막기 위해 안전 정지할 수 있습니다. ‘전환 중 화면 변경’ 안내가 나오면 이동된 화면을 확인한 뒤 다시 켜세요.

과거 v021b에서는 광고 OFF/ON, `#광고` 캡션만 있는 일반 릴스의 정상 완주, 당시 계약인 ‘0회 광고 중지’를 확인했습니다. **0.2.4부터 광고가 반복 횟수와 독립되었으므로 과거 0회 시험을 새 동작의 PASS로 쓰지 않습니다.** 최신 0회 광고 실기기 결과는 [검증 상태](VERIFICATION.md)를 확인하세요.

### YouTube 라이브 · 미리보기 넘김

1. ‘사용할 앱’에서 설치된 YouTube를 선택합니다.
2. ‘YouTube 라이브 · 미리보기 넘김’에서 **라이브 미리보기 넘기기**를 켭니다. 기본은 OFF입니다.
3. 대기 시간을 **0~60초**로 정합니다. 기본 **0초는 인식·안전 확인 후 바로 넘기기**입니다. 예를 들어 5는 라이브 인식 후 5초 기다린 뒤 넘기기입니다.
4. 숫자를 입력했다면 키보드 완료 또는 **입력한 라이브 시간 적용**을 누릅니다. −/+는 1초씩 즉시 적용하며 0과 60에서 더 내려가거나 올라가지 않습니다. 빈칸·음수·소수·문자·61 이상은 거절하므로 오류를 고쳐 다시 적용하세요.
5. 하단 **전체 자동 넘김 실행**을 켭니다. YouTube 쇼츠 안의 인식 가능한 라이브 미리보기에서만 동작합니다.

**반복 0회는 일반 반복 중지, 라이브 0초는 바로 넘기기**입니다. 둘을 혼동하지 마세요. 라이브 옵션과 전체 실행을 켜면 반복 0회에서도 라이브는 넘깁니다. 라이브만 사용하려면 반복 0회·광고 OFF·YouTube 선택·라이브 ON·전체 실행 ON으로 설정합니다. 모두 멈추려면 전체 실행/타일 OFF 또는 플로팅 ×를 사용합니다.

라이브 시간은 반복 횟수·Instagram 시간제와 별도 저장됩니다. 옵션 OFF에서도 시간을 미리 정할 수 있습니다. YouTube 미설치·미선택이면 조작을 비활성화하지만 저장한 옵션·시간은 보존합니다. 다시 선택하면 기존 옵션 상태를 확인하세요.

‘라이브’라는 제목이나 시청자 수만으로는 넘기지 않습니다. 쇼츠 내 전용 화면 구조를 확인하며 부분 표시·댓글·메뉴·앱/창 전환 등에서는 대기하고 지연 시간을 다시 셉니다. 방송 참여 버튼은 누르지 않습니다. 같은 페이지가 재사용되거나 다음 페이지를 확실히 구분하지 못하면 안전 정지할 수 있습니다. 일반 라이브 시청 화면·모든 YouTube 버전을 지원한다는 뜻은 아닙니다. 새 권한·OCR·화면/오디오 분석은 없습니다. [상세 동작·한계](LIVE_SKIP.md).

## 7. 실행 토글과 0회의 차이

| 상태 | 자동 넘김 | 플로팅 |
|---|---|---|
| 실행 OFF | 광고·라이브 포함 모두 하지 않음 | 닫힘 |
| 실행 ON, 현재 0회, 긴 영상·광고·라이브 OFF | 모든 자동 넘김 안 함 | 표시 옵션에 따라 0회 상태 표시 |
| 실행 ON, 현재 0회, 선택 앱의 긴 영상 ON | 확인된 총길이≥기준인 일반 영상 넘김. 광고·라이브는 각각 옵션에 따름 | ‘조건’ 표시 |
| 실행 ON, 현재 0회, Instagram 선택·광고 ON, 긴 영상·라이브 OFF | 인식된 Instagram 광고만 넘김 | ‘광고’ 표시 |
| 실행 ON, 현재 0회, YouTube 선택·라이브 ON, 긴 영상·광고 OFF | 인식된 YouTube 라이브 미리보기만 넘김 | ‘라이브’ 또는 남은 초 표시 |
| 실행 ON, 현재 0회, 두 앱 선택·광고·라이브 ON, 긴 영상 OFF | 앞에 보이는 대상 앱의 광고 또는 라이브 | ‘광·라’ 또는 진행 상태 표시 |
| 실행 ON, 현재 양수 | 선택한 앱에서 조건을 만족할 때 수행 | 표시 옵션에 따라 표시 또는 숨김 |

0회에서는 일반 반복·시간제가 멈추며 긴 영상·광고·라이브는 독립 옵션을 따릅니다. 하단은 활성인 독립 옵션과 반복 중지를 구분합니다. 다시 양수를 선택할 수 있도록 실행 토글은 켜짐을 유지합니다. 모든 자동 넘김을 확실히 멈추려면 **전체 실행OFF 또는 플로팅×**를 사용하세요. 기준 자체가0이면 껐다 켜도 과거 양수로 몰래 복원하지 않습니다.

실행 조건이 부족하면 켜짐을 유지하지 않고 앱 선택·권한 안내를 확인하도록 합니다. 한 번의 스와이프 후 다음 영상으로 넘어갔는지 확인하지 못해도 연속 스와이프를 반복하지 않습니다. 0.2.6의 일반 진행 기반 전환 확인 시간 초과만 새 시작점에서 다시 세는 복구 대상이며, 그 밖의 실패는 재시작 필요 상태로 멈춥니다. ‘정지’ 또는 재시작 안내이면 오류 설명과 화면을 확인하고 전체 실행을 껐다 다시 켜세요.

## 8. 빠른 설정창

1. ‘사용 준비’의 ‘빠른 설정에 실행 토글 추가’를 누릅니다.
2. 시스템 확인 창에서 추가합니다. 지원되지 않거나 추가되지 않으면 빠른 설정 편집 목록에서 ‘쇼츠 넘김’을 직접 추가합니다.
3. 타일을 짧게 누르면 실행 ON/OFF가 바뀝니다. ON은 기준 횟수로 시작하며 플로팅 표시 옵션을 따릅니다.
4. 현재0회여도 타일은 전체 실행이 켜진 상태를 유지합니다. 일반 반복·시간제는 중지하지만 해당 앱의 긴 영상·광고·라이브 옵션이 켜져 있으면 조건에 맞는 콘텐츠는 넘길 수 있습니다. 모두 멈추려면 타일을OFF로 바꾸세요.
5. 타일을 길게 누르면 설정 화면을 엽니다.
6. ‘권한 설정 필요’, ‘앱 선택 필요’, ‘재시작 필요’가 나타나면 앱 설정과 상태 문구를 확인합니다.

빠른 설정창이 열려 있는 동안에는 영상 앱이 입력 대상이 아니므로 대기합니다. 창을 닫고 영상으로 돌아가면 다시 처음 재생부터 추적할 수 있습니다.

## 9. 재생 횟수를 세는 방법과 한계

35초 영상의 20초 시점에 켜면 남은 15초를 완주로 세지 않습니다. 현재 2회 설정이라면 `0/2`로 기다렸다가 다음 시작부터 `1/2`로 추적합니다. 영상의 끝→처음 경계를 확인하고 실제로 관측한 진행량을 검사합니다. 단순히 영상 길이의 두 배만 기다리는 타이머가 아닙니다.

조회는 약 0.3초 간격이며 앱이 제공하는 시간 정보의 정밀도·갱신주기에 영향을 받습니다. 종료와 스와이프 사이에 짧은 지연이 있을 수 있습니다. 0.1.4에서 도입한 최신 시간 갱신과 표시 지연 허용, 0.1.5에서 도입한 입력초점·빠른 설정·PiP 보호를 유지하고 Instagram 감지에 공통 적용합니다.

### 0.2.6: 카운트가 끊겼을 때 새로 세기

반복1회로 같은 영상을 보고 있다가 카운트가 끊겼다면 이미 본 부분은 계산에서 제외합니다. 영상이 처음으로 돌아오고 실제로 앞으로 재생되는 것을 확인하면 그 재생부터 `1/1`로 새로 추적하며, **새로 추적한 한 회를 끝까지 확인한 뒤** 넘깁니다. 이 예에서는 두 번째 재생까지 보지만, 다음 영상으로 이미 넘어간 경우에는 그 영상의 새 시작이 기준이므로 항상 총 두 번만 본다고 보장하지 않습니다. 반복2회는 새 기준부터 두 회를 다시 셉니다.

새 복구가 적용되는 것은 **일반 진행 정보 기반 넘김 요청 후4.5초 동안 전환을 확인하지 못한 경우**입니다. 해당 요청과 같은 앱·창의 안전한 일반 영상에서 시작 부근과 후속 재생 진행을 확인합니다. 0초 한 번·0초에 멈춘 버퍼링·단순 시간 경과만으로는 재개하지 않습니다. 복구 확인 중 추가 스와이프를 하지 않으며 광고·라이브·시간제로 우회하지 않습니다.

플로팅의 복구 대기는 ‘새 시작을 기다리는 중’, 안전정지는 ‘화면 확인과 수동 재개가 필요한 상태’로 구분합니다. 대기는 목표 횟수를0으로 바꿨다는 뜻이 아닙니다. 전체 실행 OFF나 목표0은 이 일반 카운트 복구보다 우선하며, 접근성 서비스가 끊긴 뒤 자동으로 실행을 다시 켜지 않습니다. 플로팅을 숨겼으면 앱 하단 상태를 확인하세요. 상세한 조건과 남은 한계는 [카운트 복구 안내](PLAYBACK_RECOVERY.md)에 있습니다.

For target1, discard the uncertain earlier play and wait for a verified new beginning plus forward playback. Observe one complete new play before advancing; the start itself never triggers a swipe. This new recovery applies only to the4.5-second timeout after an ordinary progress-based advance. A frozen zero or elapsed time is insufficient. Waiting does not change the saved target; overall OFF, target0 and existing hard stops retain priority.

| 상황 | 처리와 복구 |
|---|---|
| 중간 재생에서 시작 | 남은 부분은 완주 제외. 다음 시작을 기다림 |
| 정상 진행 영상 일시정지·버퍼링 | 시간이 흐른다는 이유만으로 넘기지 않음. 정보 없는 시간제 영상은 감지된 중단에 한해 초기화 |
| 댓글·메뉴·키보드·다른 앱을 감지 | 추적을 초기화하고 자동 스와이프 보류. 영상으로 복귀 |
| 조회가 3초 넘게 끊김·수동 탐색으로 판단한 시간 점프 | 횟수 초기화 후 다시 관측 |
| `0/N`이 지속 | 처음 재생 또는 유효한 진행 정보 대기. 앱의 상태 설명 확인 |
| 일반 진행 기반 넘김 확인 시간 초과 | 추가 스와이프 없이 안전한 새 시작과 후속 재생을 확인한 뒤 새로 카운트 |
| ‘정지’ 또는 복구 대상 밖의 넘김 실패 | 연속 스와이프 차단. 현재 화면·오류를 확인하고 실행 OFF→ON |
| 광고 옵션 OFF | 광고에서는 대기. 직접 넘기거나 Instagram 광고 옵션을 켜기 |
| 재생 정보 없는 일반 릴스 | 광고로 간주하지 않음. 지원 단일 영상은 선택한 시간제/실험 경로, 그 외 직접 이동 |
| 연속 광고의 전환 구분 실패 |4.5초 후 안전 정지. 자동 재시도 없음 |
| 이동 확인 도중 드래그·화면/앱 변경 | 중복 요청 방지를 위해 안전 정지할 수 있음. 화면 확인 후 직접 재시작 |
| 접근성 재연결·프로세스 재시작 | 안전을 위해 실행 OFF. 사용자가 직접 다시 ON |
| 잠금·다른 창·PiP | 자동 넘김 대상 아님 |

3초 미만 일반 영상, 지원 형식 밖 시간 표시, 앱 UI 변경, 특수 콘텐츠는 완주를 감지하지 못할 수 있습니다. 광고 바로 넘기기는 재생 완료를 기다리지 않는 별도 기능입니다. 작은 수동 탐색과 늦게 갱신되는 시간 표시는 완벽하게 구분할 수 없습니다. 모든 댓글·메뉴·다른 앱의 비포커스 팝업을 차단한다고 보장하지 않습니다. 다른 창으로 복잡한 조작을 할 때는 실행을 꺼 두는 편이 안전합니다.

## 10. 저장과 업데이트

### 0.2.5 이상에서 새 버전 확인하기

‘사용 준비’ 다음의 **업데이트 · 앱 정보**에서 현재 설치 버전과 상태를 확인합니다.

- **앱 열 때 새 버전 확인**은 기본 ON입니다. 앱을 열거나 돌아왔을 때 처음 한 번, 이후 마지막 조회 시도에서 24시간이 지난 경우에만 자동 조회합니다. 실패한 시도도 간격에 포함합니다. OFF로 바꾸면 이후 자동 조회를 하지 않으며 수동 확인은 남습니다. 이미 확인한 새 버전 안내가 즉시 사라지는 설정은 아닙니다.
- **업데이트 확인**을 누르면 지금 직접 조회합니다. 자동 조회를 껐거나 실패 후 다시 확인할 때 사용합니다. 연속 누름은 잠시 제한합니다.
- 현재 설치본보다 높고 이 기기에 맞는 버전이 확인된 경우에만 상단 **새 버전 … · 업데이트 보기** 배너가 나타납니다. 배너를 누르면 해당 카드로 이동하며 다운로드·설치를 시작하지 않습니다. 이전 조회에서 확인한 새 버전도 다시 표시할 수 있습니다.
- 앱을 닫아 둔 동안 주기적으로 확인하는 서비스나 시스템 알림은 없습니다. 새 버전 정보 확인만으로 APK를 자동 다운로드하지 않습니다.

### 다운로드 → 검사 → 설치

1. 새 버전 안내를 읽고 **업데이트 다운로드**를 누릅니다. 앱 내부 저장공간에 받고 진행률을 표시합니다. 공유 다운로드 폴더나 저장소 전체 접근 권한은 필요하지 않습니다.
2. 원치 않으면 **다운로드 취소**를 누릅니다. 통신 실패·취소·검사 실패는 설치로 넘어가지 않으며 기존 앱·설정을 바꾸지 않습니다. 연결과 저장공간을 확인하고 다시 시도하세요.
3. 파일 크기·SHA256·앱 ID·버전·기기 최소 OS·현재 앱과 같은 서명인지 검사한 뒤 **다운로드·검사 완료**를 표시합니다. **업데이트 설치**를 별도로 눌러야 다음 단계로 갑니다. 다른 서명·손상된 APK를 설치하도록 우회하지 않습니다.
4. **업데이트 설치를 누르는 시점에 전체 자동 넘김 실행을 OFF**로 바꿉니다. 기준/현재 횟수, 앱 선택, 시간제·광고·라이브 옵션과 초 값, 플로팅 위치 등 다른 설정은 보존합니다.
5. 설치 허용이 없으면 안내의 **설정 열기**를 눌러 Android 설정에서 이 앱의 **이 출처 허용**을 직접 켭니다. 취소하거나 허용하지 않아도 기존 앱을 계속 쓸 수 있지만 실행은 OFF로 유지됩니다. 제조사에 따라 문구가 다르거나 설치가 제한될 수 있습니다.
6. 이 앱으로 돌아와 **업데이트 설치**를 다시 누릅니다. Android 설치 창에서 앱 이름을 확인하고 최종 **설치**를 직접 선택합니다. 허용 화면에서 돌아온 것만으로 설치하지 않으며, 설치 창을 연 것을 완료로 표시하지 않습니다.
7. 설치 후 앱을 다시 열어 **설치 버전**을 확인합니다. 업데이트·서비스 재연결 후 실행은 자동 재시작하지 않습니다. 설정을 확인한 뒤 필요할 때 하단 실행 토글을 직접 켜세요.

앱 화면을 완전히 닫거나 다시 만들면 진행 중 다운로드가 취소되거나 다시 다운로드가 필요할 수 있습니다. 백그라운드 다운로드·중단 지점 이어받기를 보장하지 않습니다. 설치 허용을 되돌리려면 Android의 이 앱 ‘알 수 없는 앱 설치’에서 끕니다. 기기 정책으로 설치가 막히면 우회하지 말고 현재 앱을 사용하거나 관리자의 허용 여부를 확인하세요.

### 설정 보존과 이전 버전

저장하는 값은 기준/현재 횟수, 마지막 양수 횟수, 실행 요청, 터치 방식, 적용 앱 선택, 플로팅 표시 옵션, 긴 영상 옵션·총길이 기준, 광고 옵션, 라이브 옵션·초 값, 시간제 옵션·초 값, 화면 분석 옵션 및 상대X/Y 위치입니다. 옵션은 서로 독립 저장하며 대상 앱 선택을 해제해도 관련 설정은 보존합니다. 서비스가 다시 연결되면 실행 요청을OFF로 바꿉니다. 클라우드·기기이전 백업은 제외합니다.

업데이트 자동 조회 선택·마지막 시도 시각·새 버전 정보는 재생 설정과 별도 저장합니다. APK는 앱 내부의 업데이트용 파일이며 영상 데이터가 아닙니다. **0.2.4에서 업데이트 메뉴가 있는 버전으로 옮길 때는 최초 수동 APK 설치가 필요**하고, 0.2.5부터 이후 호환되는 공개 버전을 인앱으로 확인할 수 있습니다. 공개된 prerelease도 기존 정책에 따라 업데이트 후보가 될 수 있습니다. 앱 버전의 중립적 표시와 조회 대상은 별개입니다.0.2.9의 최종 검증·게시 결과는 [VERIFICATION](VERIFICATION.md) 참조.

0.2.9에서는 조회·다운로드·설치와 오류 안내도 표시 언어를 따릅니다. **업데이트 다운로드 / Download update → 업데이트 설치 / Install update** 순서이며 Android에서 최종 확인하기 전에는 설치 완료가 아닙니다. 통신·검증 오류가 발생하면 번역된 안내에 따라 다시 시도하고, 서명·파일 검사를 우회하지 마세요. 알 수 없는 오류의 원문·내부 경로는 표시하지 않습니다.

Version0.2.9 also translates update status and errors. Use **Download update → Install update**, then confirm in Android; opening the installer alone is not completion. Follow localized retry guidance after connection/verification errors without bypassing file or signer checks. Unknown raw error text and internal paths are not displayed.

0.1.5에서 업데이트하면 기존 양수 설정은 기준/현재 값으로 옮깁니다. 기존 현재 값이 0이면 현재 0을 보존하고 마지막 양수를 기준값으로 옮깁니다. 기본 선택은 YouTube만, 플로팅 표시 ON, 횟수 순환입니다. 위치도 유지합니다.

0.1.5 설명서와 달라진 점은 0~99 입력, 두 터치 방식, 플로팅 없이 사용, Instagram 선택 및 인앱·타일 실행 기준 통일입니다. 0.2.1에는 Instagram 광고 바로 넘기기가 생겼으며 기존 사용자도 기본 OFF로 시작합니다. 기존 설정값과 저장 스키마는 그대로 유지합니다. 과거 YouTube 및0.2.0 UI 시험을 광고 기능의 검증 완료로 취급하지 않습니다. 변경 내역은 [누적이력](CHANGELOG.md)을 참고하세요.

0.2.2는 배터리 설정 안내를 추가한 이전 공개 버전입니다. 0.2.4는 선택형 시간제(기본 OFF·10초·5~60초), 기존 선택형 화면 분석 시험 보존, 기능별 설정 분류와 광고 독립 동작을 포함합니다. **업데이트 후 광고 옵션이 이미 ON이었다면 반복 0회에서도 전체 실행을 켤 때 광고가 넘어갈 수 있으므로 광고 설정을 확인하세요.** 광고를 넘기고 싶지 않으면 광고 옵션을 끕니다. 같은 서명의 업데이트 설치는 기존 설정을 보존하며 삭제 후 재설치는 저장값을 잃습니다.

설정 초기화는 Android의 이 앱 데이터 삭제로 가능하지만 저장한 설정과 위치가 사라집니다. 앱이 업데이트를 위해 설정을 자동 삭제하지 않습니다.

## 11. 삼성 기기에서 백그라운드 실행이 멈출 때 / Samsung background pauses

0.2.2에서는 **사용 준비 → 백그라운드 실행 · 배터리 → 배터리 제한 없음 설정하기**를 누르면 수동 설정 안내가 열립니다. '설정으로 이동'을 누른 뒤 이 앱의 **배터리 → 제한 없음**을 직접 선택하세요. 이미 예외이면 '배터리 설정 확인하기'를 표시하며, 취소하면 이동하지 않습니다.

Version0.2.2 adds a battery setup panel. Open its setup button, read the instructions, and choose Open Settings, then Battery → Unrestricted for this app. When already exempt, the button offers a review. Cancel leaves system settings untouched.

앱 복귀 시 '절전 예외 적용됨/아님' 상태를 다시 읽습니다. 이는 Android의 예외 목록 조회이며 모든 제조사 정책을 검사한 결과가 아닙니다. 확인 불가·설정 열기 실패이면 시스템 설정에서 직접 확인하세요. 새 권한 요청, 설정 자동 변경, 실행 자동 재시작은 없습니다. 배터리 예외 미적용이 실행 토글 자체를 막지는 않습니다.

Status refreshes on return. It reflects Android's exemption API, not every manufacturer policy. Check manually if status or navigation is unavailable. No new permission, automatic policy change, or automatic restart is performed; exemption is not an execution prerequisite.

실행이 켜져 있는데 다른 앱을 열거나 YouTube를 작은 창으로 바꾼 후 자동 넘김이 돌아오지 않는다면, 삼성의 백그라운드 앱 동결도 확인해야 합니다. 접근성 등록과 앱 프로세스가 남아 있어도 실행이 멈출 수 있습니다. 0.2.1 실기기에서 이 상태를 확인했으며, 단순한 반복 횟수 누적 문제와 구분합니다.

If auto-advance stops after switching apps or using YouTube picture-in-picture, Samsung background freezing is one possible cause. An accessibility service may remain registered even while its process is frozen. This was observed on the tested device with version 0.2.1; it is separate from the playback counter.

1. 휴대폰 **설정 → 애플리케이션 → 쇼츠 자동 넘김 → 배터리**를 엽니다.
2. 해당 앱만 **제한 없음**으로 선택합니다. 다른 앱이나 휴대폰 전체 절전 설정은 변경할 필요가 없습니다.
3. YouTube 쇼츠를 전체 화면으로 열고 재생 시간이 다시 감지되는지 확인합니다. 중간 재생에서 복귀했다면 다음 처음부터 완주를 세므로 즉시 넘기지 않을 수 있습니다.
4. 앱에 복구 대기가 표시되면 새 재생 시작을 관측하는 상태입니다. ‘정지’ 또는 ‘전환 중 화면 변경’ 등 재시작 안내이면 별도의 안전정지이므로 현재 영상을 확인한 뒤 자체 실행 토글을 OFF→ON합니다. 배터리 설정이 이 안전정지까지 해제한다고 가정하지 마세요.
5. 원래 설정으로 되돌리려면 같은 배터리 화면에서 **최적화**를 선택합니다. 백그라운드 정지가 다시 발생할 수 있습니다.

Open **Settings → Apps → ShortsLoop → Battery → Unrestricted** for this app only. Return to full-screen Shorts and allow a full playback cycle to be observed. Recovery waiting observes a new start; a separate hard stop still requires checking the current video and toggling execution OFF, then ON. To undo the battery exception, select **Optimized** on the same screen.

제한 없음은 배터리 사용량을 늘릴 수 있습니다. 이 설정은 작은 창의 YouTube를 자동으로 넘기도록 지원 범위를 바꾸는 것이 아닙니다. 장시간·재부팅·다른 기기의 효과는 별도 확인이 필요합니다. 앱은 이 설정을 몰래 바꾸지 않습니다. 실제 수행한 시험 범위는 [검증 기록](VERIFICATION.md)의 D-020 후속 시험을 참고하세요.

Unrestricted battery use may increase battery consumption. It does not add auto-advance support inside PiP. Long-running sessions, reboot behavior, and other devices require separate testing. The app does not silently change this setting. See the [verification record](VERIFICATION.md) for the tested scope.

## 과거 버전 설명·검증 기록 / Historical descriptions and evidence

아래의0.2.7 이하 및 별도 [0.2.8 기록](releases/v0.2.8.md)의 빌드·기기·게시 결과는 버전별 과거 기록입니다. 당시 표시·배포 상태와 검증 수치는 보존하지만0.2.9의 PASS나 공개 완료 근거로 재사용하지 않습니다.

<details>
<summary>0.2.7 이하의 이전 문서 기록 / Earlier documentation</summary>

0.2.7은 플로팅 크기72×56dp와 조작법을 유지하면서 ‘긴영상’ 등의 글자 잘림을 수정합니다. 사용법과 자동 넘김 정책은0.2.6과 같습니다. 정식 릴리스라도 선택형 화면 분석은 실험 기능이며,기기·호스트별 인식 제약은 유지됩니다. [0.2.7 배포·검증 기록](releases/v0.2.7.md).<br>
Stable0.2.7 fixes floating-label clipping without changing the72×56dp size,controls or0.2.6 playback policies. Optional visual assistance remains experimental,and device/host recognition limitations still apply. See the0.2.7 release record.

## 이전 0.2.6/code28 · Public 시험판 기록 / Previous pre-release

**0.2.6/code28 공개 시험판(pre-release)을 게시하고 공개 파일 검증까지 완료했다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,로컬lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 is published as a public pre-release,and public artifact verification is complete.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;local lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증 후 기존 Public 저장소에v0.2.6/code28 pre-release를 게시했으며 CI·공개 다운로드 동일성도 확인했다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. After the YouTube retest and impact-scope checks passed,v0.2.6/code28 was published as a pre-release in the existing Public repository. CI and public-download parity were verified.

## 이전 code26 검증 / Previous candidate evidence

**이전0.2.6/code26은 후속 실폰 실패로 배포하지 않았던 후보입니다.** 454제품시험·209/209/208 계측·설치/설정/해시 검증은 PASS했습니다. YouTube 지정10회(긴 영상9+라이브1)와 별도 일반1회를 확인했으나 이후 같은 길이 전환 확인에 실패했습니다. Instagram10개는96.0초 동안 일반3·긴 영상4·시간제2·광고1,수동0으로 통과했습니다. 유튜브 실패 요청의 실제 이동은 전후 화면 쌍이 없어 미확정입니다. 시험 후 긴 영상 기준60초를 복원하고 전체 실행은OFF로 종료했습니다. 이전code23/24/25의 실폰FAIL과 최신 결과는 [VERIFICATION](VERIFICATION.md)에서 구분합니다.<br>
**Historical code26 remained unpublished after a follow-up phone failure.**454 tests,209/209/208 emulator checks and installation/settings/hash checks passed. The designated YouTube10(9 long-video,1 live) and one separate ordinary transition passed,but later same-duration confirmation failed. Instagram10 passed in96.0 seconds(3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes). Actual movement for the failed YouTube request is unproven without a pre/post screenshot pair. The threshold was restored to60 seconds and execution stopped;earlier code23/24/25 failures remain separate.

## 과거0.2.6 도입 기록 · 새 시작점 복구·긴 영상

0.2.6(code28)은 로컬 검증·공개 CI·다운로드 동일성 확인을 마친 Public 시험판입니다. 정상 진행 정보를 사용하는 일반 영상에서 넘김 확인 시간이 초과되면, 추가 스와이프 없이 새 재생 시작을 관측하고 이전 카운트를 버린 뒤 설정 횟수를 다시 셉니다. 시작점을 확인하자마자 넘기는 기능은 아닙니다. 광고·라이브·시간제·화면 분석 및 권한·화면 변경의 안전정지는 유지합니다. [복구 예시·조건](PLAYBACK_RECOVERY.md), [최종 검증·게시 결과](VERIFICATION.md).

0.2.6(code28) is a Public pre-release with local,CI and public-download parity verified. After an ordinary progress-based transition times out, recovery waits for a verified fresh playback start without another swipe, then counts the configured plays anew. Finding the start does not advance immediately; other safety stops remain.

code28에는 **긴 영상 건너뛰기**가 추가됩니다. 기본OFF·총길이 기준60초·1~3600초이며 반복0과 별개입니다. 확인한 총길이가 기준 이상이면 재생 처음을 기다리지 않고 안전 확인 후 넘깁니다. 길이를 모르면 추측하지 않습니다. code22의 YouTube 시험은 실제2회 전환 후 기능 추가로 중단했으며 최신 후보의10회 시험에 합산하지 않습니다.

Code28 retains optional long-video skipping: OFF by default, total-duration threshold60 seconds, range1–3600, independent of zero plays. Known durations at least the threshold qualify after safety checks without waiting for a new beginning. Unknown duration is never guessed. Code22's two confirmed YouTube transitions preceded the feature addition and do not count toward the latest ten-transition run.

이전 공개판0.2.5(code21)는 ‘사용 준비 → 이 기기의 기능’, ‘업데이트 · 앱 정보’와 사용 빈도순 메뉴·선택형 YouTube 라이브 미리보기 넘김을 통합했습니다. 2026-08-28 Public 시험판으로 공개한 이력이며 해당 버전의 시험 결과를0.2.6 검증으로 재사용하지 않습니다. 새 APK에서 YouTube10개와 Instagram10개의 실제 자동 전환을 각각 확인하는 것이 이번 시험 목표이며 완료 전 PASS로 표기하지 않습니다.

Android 8(API26)부터 설치 가능하며 8~12L은 빠른 설정 수동 추가, 13(API33) 이상은 추가 요청, 14(API34) 이상만 화면 분석 실험을 제공합니다. Android 10(API29)부터 타일에 별도 상태줄을 표시합니다. Instagram 미설치·미선택 또는 OS 미지원 옵션은 이유와 함께 비활성화하고 저장값은 보존합니다. 기본 반복·시간제·광고·플로팅의 의미는 그대로입니다. 공식 YouTube는 Android 9 이상이며 Instagram 최소 OS는 미확정이므로 기기 Play 스토어의 호환 여부를 확인합니다. [한영 지원표·근거](COMPATIBILITY.md).

## 당시 버전과 검증 범위

이전 공개판 0.2.4는 **정상 N회·시간제·광고를 합친 20연속 자동 이동 검증이 미완료인 시험판**입니다. 당시 code13은 제품227개시험·빌드 PASS, lint0오류/4경고였으며 설치본 해시 일치와 0회 광고1회 자동 이동/일반·시간제 중지를 확인했습니다. 이 결과를 0.2.5 업데이트 경로의 검증으로 재사용하지 않습니다. [0.2.4 이력](releases/v0.2.4.md), [버전별 검증 상태](VERIFICATION.md).

현재 동작은 **일반 영상 N회 / 총길이 기준 긴 영상 건너뛰기 / 정보 없는 Instagram 시간제 / Instagram 광고 / YouTube 라이브 미리보기**로 나뉩니다. 0회이면 일반 반복·시간제·화면 분석은 멈추지만 긴 영상·광고·라이브는 각각 해당 앱 선택·별도 옵션ON·전체 실행ON이면 동작합니다. 전체 실행OFF는 모두 멈춥니다. 과거0.2.1의 ‘0회는 광고 포함 모두 중지’와 달라진 동작입니다.

화면 분석 보조는 기본 OFF·별도 동의·Android 14 이상인 시험 기능으로 보존합니다. 시간제와 둘 다 켜면 시간제가 우선합니다. 정확한 총 N회는 보장하지 않습니다. 별도 미연결 `VisualSequenceTracker` 실험의 20시험 중 2실패는 제품 시간제의 실패와 다릅니다. 실험 원본을 보존하고 배포 제품과 제품 시험에서 제외하며 전체 실험까지 PASS로 보고하지 않습니다. 오디오 수신·패턴 시험은 별도 진단 앱이고 제품 자동 넘김에 연결하지 않았으며 이번 APK 첨부 대상도 아닙니다.

한영 요약은 [README](../README.md), 화면 구성의 이유는 [UI·인간공학 기준](UI_DESIGN.md), 동작 계약은 [제품 기준](PRODUCT_SPEC.md), 시간제 상세는 [시간제 안내](TIMED_FALLBACK.md)를 참고하세요. 과거 0.2.0 UI·0.2.1 광고·0.2.2 공개 기록은 이력이며 최신 APK의 전체 검증을 대신하지 않습니다.

</details>
