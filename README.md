# 쇼츠 자동 넘김 · ShortsLoop 0.2.9

로컬0.2.9에는 **Instagram 사진 릴스 통째 넘김 / 한 장씩 보기**와 각각0–10초(기본3초), 장 번호 확인 불가 시 선택형 통째 넘김을 추가했습니다. 기본OFF이며 반복0회와 독립입니다. [설정·예시·한계](docs/PHOTO_REELS.md). 최신 APK의 두 모드·0/3/10초·댓글창 보호를 실폰에서 확인했습니다. 번호 없는 사진·사진 직후 광고·혼합 사례는 미확보이며 0.2.9를 공개했습니다. [조건별 검증](docs/VERIFICATION.md).

Local0.2.9 adds **whole-Reel / each-photo modes for Instagram photos**,separate0–10s delays(default3) and optional unreadable-index fallback. DefaultOFF,independent of repeat0. Both modes,0/3/10s and comments protection are physically tested on the latest APK;unreadable-index,photo-to-ad and mixed cases remain unobserved. Published as0.2.9. See the linked guide and verification.

ShortsLoop는 **YouTube Shorts와 Instagram Reels를 위한 Android 자동 스크롤(auto scroll) 앱**입니다. 접근성 서비스(AccessibilityService)로 지원되는 재생 정보를 읽고, 재생 횟수 카운터(repeat counter)가 설정한 총 횟수를 확인하면 다음 영상으로 넘깁니다. 한국어·영어(Korean/English) UI, 선택형 플로팅과 빠른 설정 토글을 제공합니다. 모든 영상·언어·기기에서의 감지를 보장하지 않습니다.

ShortsLoop is an **Android auto scroll app for YouTube Shorts and Instagram Reels**. Its AccessibilityService reads supported playback information, and a repeat counter advances after your chosen total plays. It includes a Korean/English interface, an optional floating control and a Quick Settings toggle. Detection is not guaranteed for every video, language or device.

**0.2.9/code31**은 앱·플로팅·빠른 설정의 안내, 도움말과 업데이트 오류를 한국어·영어로 제공합니다. 시스템의 **첫 번째 언어가 한국어이면 한국어, 그 밖에는 영어**입니다. 예를 들어 일본어가 첫 번째이고 한국어가 두 번째이면 영어로 표시합니다. 기존 설정·패키지·서명·권한은 유지하며 사진 릴스 감지를 별도로 추가합니다. [언어 선택·지원 범위](docs/LOCALIZATION.md).

**0.2.9/code31** localizes the app, floating control, Quick Settings, help and update errors. The **first system language selects Korean when it is Korean, otherwise English**: Japanese first and Korean second still selects English. Existing settings, package, signing identity and permissions remain; photo-Reel detection is added separately. See the localization guide for scope and exceptions.

업데이트 조회 정책은 바꾸지 않습니다. 고정 저장소의 공개 릴리스 목록에서 draft를 제외하고 **공개된 prerelease도 포함**해,유효한 메타데이터·더 높은 versionCode·기기 OS 호환성을 만족하는 항목을 확인합니다. 설치 전 크기·SHA256·패키지·버전·현재 설치본과 같은 서명 집합을 검사하고 Android의 최종 설치 확인을 거칩니다. 앱의 중립적인 버전 표시가 prerelease를 제외한다는 뜻은 아닙니다.

Update selection is unchanged:exclude drafts from the fixed repository's public release feed,but **include published prereleases** with valid metadata,a higher versionCode and compatible OS. Size,SHA256,package,version and the installed signer set are checked before Android's final installation confirmation. Neutral version wording does not imply excluding prereleases.

[0.2.9 공개 릴리스](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.9) · [검증 상태](docs/VERIFICATION.md) · [배포 점검](docs/RELEASE_PRESENTATION_AUDIT.md). 공개 APK·SHA256·업데이트JSON을 익명으로 내려받아 원본과 일치함을 확인했습니다. 이전 버전은 [0.2.8](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.8)입니다.<br>
**0.2.9 is published.** Anonymous downloads of the APK,checksum and update JSON match the frozen originals. Version0.2.8 is the previous release.

Android 8.0 이상. 최종 검증된 같은 서명의 APK를 기존 앱 위에 설치하면 설정을 유지할 수 있습니다. 최신0.2.9 후보에서 사진 두 모드·0/3/10초·댓글 보호를 실폰 확인했습니다. 번호 없는 사진·사진 직후 광고·혼합 사례는 미확보입니다. 후보별 근거는 검증 기록에서 구분합니다. 인앱 하단은 `ShortsLoop 0.2.9`, 업데이트 카드는 `설치 버전 0.2.9` / `Installed version 0.2.9`입니다. 중립적인 버전 표시는 배포 검증이나 기능 보증을 대신하지 않습니다. 화면 분석 보조의 **실험 기능 안내·별도 동의·Android 14 이상 제한**은 유지합니다.<br>
Android 8.0+. Install the finally verified same-signed APK over the existing app to retain settings. The latest0.2.9 candidate has physical evidence for both photo modes,0/3/10s and comment protection;unreadable-index,photo-to-ad and mixed cases remain unobserved. Evidence is candidate-specific. The footer shows `ShortsLoop 0.2.9` and the update card shows the localized installed version. Neutral version labels do not replace artifact or feature verification. Visual assistance retains its **experimental warning, separate consent and Android 14+ limit**.

## 주요 기능 / Features

- 총 재생 횟수0~99: 숫자 입력과▲/▼. **0은 일반 반복·시간제 중지, 긴 영상·광고·라이브는 별도 옵션**.<br>
  Total plays from0 to99 with typing and step buttons. **Zero stops repeat-based and timed advancing; long videos, ads and live previews use separate options**.
- 긴 영상 건너뛰기: 기본OFF·기준60초·1~3600초, 확인된 총길이≥기준일 때 두 앱에 적용.<br>
  Long-video skipping: OFF by default, threshold60 seconds, range1–3600; both selected apps qualify when known duration≥threshold.
- 일반 영상 넘김 확인 시간 초과 후, 검증된 새 재생 시작부터 다시 세기. 복구를 위한 추가 스와이프·시간만으로 완주 처리하지 않음.<br>
  Recount from a verified fresh start after an ordinary transition-confirmation timeout; no recovery swipe and no completion inferred from elapsed time alone.
- 선택 앱: YouTube, Instagram 또는 둘 다. 앱별로 별도 감지.<br>
  Select YouTube, Instagram, or both; each has a separate reader.
- 선택형 반투명 플로팅: 현재/적용 횟수, 이동·위치 저장, 모서리 × 종료.<br>
  Optional translucent floating control with current/target count, drag-to-save position, and × to stop.
- 플로팅 탭: 0부터 기준까지 순환하거나 0↔기준으로 전환.<br>
  Tap to cycle through 0…N or toggle between 0 and N.
- 인앱 및 빠른 설정의 실행 토글. 배터리 절전 예외 상태와 설정 바로가기.<br>
  In-app and Quick Settings toggles, plus battery-exemption status and setup shortcut.
- Instagram의 명시적으로 인식한 광고만 바로 넘기는 별도 옵션, 기본 OFF.<br>
  Optional immediate skipping of explicitly recognized Instagram ads, OFF by default.
- YouTube 쇼츠 안의 라이브 미리보기 넘김: 기본 OFF, 대기 0~60초·기본 0초(바로), 반복 0회와 독립.<br>
  Optional YouTube Shorts live-preview skipping: OFF by default, wait 0–60 seconds, initially 0 (immediate after recognition), independent of zero plays.
- 진행 정보 없는 Instagram 영상의 선택형 시간제: 기본 OFF·10초, 5~60초.<br>
  Optional timed fallback for clockless Instagram videos: OFF by default, initially 10 seconds, range 5–60 seconds.
- 앱을 열 때 최대 24시간에 한 번 새 버전 확인, 수동 확인·다운로드·설치를 분리한 GitHub 업데이트.<br>
  GitHub updates: automatic checks on app opening, at most once per 24 hours, with separate manual check, download and install actions.

## 설치와 첫 실행 / Install and get started

Android 8.0(API26) 이상이 설치 대상입니다. 공식 YouTube 설치 안내는 Android 9 이상이며 Instagram의 최소 OS는 확인되지 않아 기기 Play 스토어의 호환 여부를 확인해야 합니다. API29부터 타일 상태줄, API33부터 인앱 타일 추가 요청, API34부터 화면 분석 시험을 지원합니다. [지원표와 확인 시점의 근거](docs/COMPATIBILITY.md). 과거 실기기 시험은 Galaxy Z Fold8 / Android17에서 했으며 모든 기기를 보장하지 않습니다. 인앱 언어는 시스템 첫 언어에 따른 한국어 또는 영어입니다.<br>
The app minimum is Android 8.0/API26. YouTube's documented requirement is Android 9+; Instagram's minimum is unverified, so check its device Play Store listing. Tile subtitles require API29, in-app tile-add requests API33, and visual assist API34. See the linked support table and dated sources. Earlier device tests used Galaxy Z Fold8 / Android17, not every device. The interface selects Korean or English from the first system language.

1. 공개가 완료된 Release에서 배포 APK를 내려받고 첨부 SHA256을 확인해 설치하세요. Play Store 배포본이 아닙니다. **0.2.4에는 인앱 업데이트가 없으므로 브라우저 등에서 새 APK를 직접 받아 덮어쓰기 설치해야 합니다.** 0.2.5 이상은 인앱에서 호환되는 상위 공개 버전을 확인할 수 있습니다.<br>
   Download the published APK from a completed public release and verify its SHA256. This is not a Play Store release. **0.2.4 has no updater: manually download and install the newer APK over the existing app.** Version0.2.5 and later can check for compatible newer public releases in-app.
2. 신뢰할 수 있는 출처인지 확인한 뒤 Android 설치 안내에 따릅니다. 출처를 알 수 없는 앱 설치 허용이 필요할 수 있습니다. 기존 YouTube·Instagram은 수정하지 않습니다.<br>
   Verify the source and follow Android's installation prompts, including permission to install from that source if needed. YouTube and Instagram are not modified.
3. 앱을 열고 **사용할 앱**을 선택한 뒤 **일반 영상 · 횟수로 넘김**에서 반복 횟수 `2` 입력 → **입력한 횟수 적용** 또는 키보드 완료. 시간제·광고·라이브는 각각 별도 항목에서 켭니다.<br>
   Select a target app, then enter `2` under the normal-video repeat section and press Apply or keyboard Done. Timed fallback, ads and live previews have separate sections and switches.
4. **사용 준비 → 접근성 설정·다시 연결**에서 안내를 읽고 Android 설정의 이 앱 접근성 서비스를 직접 켭니다. 제한된 설정으로 차단되면 OS 안내를 확인하고, 출처·권한을 신뢰할 수 없으면 중단하세요.<br>
   Under “사용 준비” (setup), open “접근성 설정·다시 연결” and manually enable this app's accessibility service in Android Settings. If restricted settings block access, follow the OS guidance; stop if you do not trust the source or permission.
5. **화면 위에 숫자 표시**를 켠 경우에만 다른 앱 위 표시 권한도 직접 허용합니다. 플로팅을 끄면 이 권한 없이 사용할 수 있습니다.<br>
   Grant overlay permission only if “화면 위에 숫자 표시” (floating numbers) is enabled. It is not required when floating controls are disabled.
6. 삼성 기기에서는 아래 배터리 안내를 확인합니다. 화면 하단 **전체 자동 넘김 실행**을 켜고 선택한 앱의 쇼츠/릴스를 전체 화면으로 재생합니다.<br>
   On Samsung devices, review the battery setup below. Turn on “전체 자동 넘김 실행” (main execution) at the bottom, then play full-screen Shorts/Reels in a selected app.

접근성은 화면을 읽고 조작할 수 있는 강한 권한입니다. ADB·무선 디버깅·루팅은 일반 사용에 필요하지 않습니다.<br>
Accessibility is a powerful permission that can read and interact with the screen. ADB, wireless debugging, and root are not required for normal use.

화면 분석을 꺼도 기본 진행 정보·광고·라이브 인식·다음 넘김에는 접근성이 필요합니다. code17부터 미연결 시 ‘접근성 연결 확인 · 해결 방법 보기’와 ‘접근성 설정·다시 연결’을 표시합니다. 설정이 ON인데 실제 연결이 끊겼다면 Android에서 이 앱의 접근성을 OFF→ON하고 연결 후 앱의 전체 실행도 다시 켜세요. 안내 버튼은 권한·실행을 자동으로 켜지 않습니다. 플로팅 권한은 표시 ON일 때만 필요합니다.<br>
Accessibility is required for clock reading, ad/live recognition and advancing even with visual assist OFF. Since code17, the app shows accessibility help and a settings/reconnect button when disconnected. If the Android setting is ON but the service is disconnected, toggle this app's accessibility OFF→ON, then re-enable main execution after reconnection. Help buttons never grant permission or start execution automatically. Overlay permission is only needed when floating display is ON.

## 횟수와 플로팅 사용법 / Counts and floating controls

메뉴는 같은 위치에서 번역됩니다. 예: **일반 영상 · 횟수로 넘김 / Ordinary videos · Play count**, **전체 자동 넘김 실행 / Auto-advance**, **업데이트 · 앱 정보 / Updates · App information**. 영어 플로팅의 `Filters`, `Ads`, `Live`, `A+L`은 한국어의 `조건`, `광고`, `라이브`, `광·라`에 해당하며 현재 영상 종류가 아닌 독립 옵션 상태입니다. [메뉴 대응표·언어 변경 방법](docs/LOCALIZATION.md).<br>
Menus keep the same positions in both languages. Floating labels describe independent options, not the current content type. See the linked menu map and language-change instructions. Android itself may choose a different fallback language for launcher/accessibility labels; this does not change playback rules.

횟수는 추가 반복이 아닌 **처음 재생을 포함한 총 횟수**입니다. `2`이면 정상 추적 중 `1/2 → 2/2 → 다음 영상`입니다. 중간부터 켜면 다음 처음 재생부터 셉니다.<br>
The count means **total plays, including the first play**, not additional repeats. With `2`, normal tracking shows `1/2 → 2/2 → next video`. Starting mid-video waits for a fresh playback cycle.

- 입력 범위는 0~99. ▲/▼는 1씩 즉시 적용. 빈칸·음수·소수·100 이상은 적용하지 않으며 기존 확정값을 유지합니다. 오류를 고친 뒤 다시 적용하세요.<br>
  Use integers 0–99; ▲/▼ apply one-step changes immediately. Empty, negative, fractional, or out-of-range input is rejected without replacing the saved value. Correct it and apply again.
- 인앱에서 기준 N을 정하면 현재 적용값도 N이 됩니다. 기준3에서 **횟수 순환**은 `0→1→2→3→0`, **반복 켜기·끄기**는 `0↔3`입니다. 광고·라이브는 별도입니다.<br>
  Setting the in-app limit N also sets the current target to N. With N=3, rotary mode cycles `0→1→2→3→0`; quick-toggle mode alternates `0↔3`.
- 기준5, 현재2일 때 플로팅은 `1/2`처럼 표시합니다. 실행을 껐다 켜면 현재값은 기준5로 복원되고 다시 셉니다. 기준0이면 플로팅을 눌러도0입니다. 이 횟수 변경은 광고·라이브 옵션을 끄지 않습니다.<br>
  With limit5 and current target2, the display is `1/2`. Restarting execution restores the target to5 and restarts counting. With limit0, floating taps cannot enable normal/timed advancing. Changing the count does not turn ad or live-preview options off.
- 숫자를 잡고 끌어 놓으면 위치 저장. 숫자 탭은 횟수 변경. **×는 전체 실행 종료**, 인앱 표시 토글 OFF는 **플로팅만 숨김**입니다.<br>
  Drag the numbers to save a position; tap to change the target. **× stops execution**, while disabling the in-app floating option **only hides the control**.
- 플로팅은72×56dp, 배경 약40% 불투명도입니다. 작은 ×가 누르기 어렵다면 하단 또는 빠른 설정 토글로 끄세요.<br>
  The control is72×56dp with roughly40% background opacity. If the small × is difficult to tap, use the in-app or Quick Settings toggle.

## 긴 영상 건너뛰기 / Skip long videos

1. 일반 횟수 카드 바로 아래 **긴 영상 건너뛰기**를 켭니다. 설치된 YouTube 또는 Instagram을 ‘사용할 앱’에서 선택해야 조작할 수 있으며, 선택을 해제해도 저장값은 보존합니다.<br>
   Enable **긴 영상 건너뛰기** directly below repeat settings. At least one installed target app must be selected; deselection preserves preferences.
2. **건너뛸 영상의 최소 총길이(초)**에1~3600 정수를 입력하고 완료/적용하거나−/+로1초씩 바꿉니다. 기본60초는60초 이상 영상을 의미하며,60초를 기다린다는 뜻이 아닙니다. 빈칸·0·음수·소수·3601 이상은 적용하지 않습니다.<br>
   Enter an integer1–3600 for the minimum total duration and press Done/Apply, or step by one second. The default60 means videos at least60 seconds long, not a60-second wait. Empty,zero,negative,fractional or over3600 input is rejected.
3. 전체 실행을 켭니다. 총길이와 같은 안전한 페이지의 실제 전진 재생을 확인한 뒤 넘깁니다. 처음 재생은 기다리지 않지만,0초 정지·일시정지·화면 전환 중에는 안전 확인을 생략하지 않습니다.<br>
   Turn overall execution ON. Advancing requires a known duration and actual forward playback on the same safe page. No fresh beginning is required, but frozen playback, pauses and screen changes do not bypass safety checks.

반복0에서도 사용 가능하며 전체OFF는 중지합니다. 길이 불명·광고·라이브를 긴 영상으로 추정하지 않습니다. 긴 영상 이동을4.5초 안에 확인하지 못하면 안전정지하며, 일반 반복의 새 시작점 복구로 바꿔 연속 재시도하지 않습니다. 플로팅은0회에서 긴 영상 옵션이 활성일 때 ‘조건’으로 표시합니다.

code26은 같은 길이·스크롤 이벤트 인덱스 부재에서 안전정지한 이력이 있습니다. code28은 실제 YouTube 페이지의 행 번호가 정확히1 증가하는지도 추가 확인합니다. 같은 페이지 영역·창·pager,달라진 콘텐츠 키·안정된 실제 진행 검사는 유지합니다. 알려진 행이 같거나 뒤로 가거나2칸 이상 뛰면 길이가 달라도 확인하지 않습니다. 페이지 정보를 안전하게 읽을 수 없으면 여전히 멈출 수 있으며 재시험 전 해결 완료로 표시하지 않습니다.<br>
Code26 safety-stopped with equal durations and missing scroll-event indices. Code28 additionally checks an exact+1 change in the native YouTube page row while preserving same-window/pager/bounds,changed-content and stable-progress guards. Known unchanged,backward or skipped rows reject even a different duration. Unsafe page information can still stop automation;the correction is not verified until retesting.<br>
It works at zero repeat count; overall OFF stops it. Unknown duration,ads and live previews are not guessed to be long videos. A long-video transition unconfirmed after4.5 seconds hard-stops rather than entering ordinary recount recovery. At zero plays, the floating control shows “조건” when long-video skipping is active.

## 진행 정보 없는 릴스 시간제 / Timed fallback for clockless Reels

긴 영상의 ‘총길이 기준’과 아래 시간제의 ‘기다리는 시간’은 다른 설정입니다. 길이 정보가 없으면 긴 영상 옵션은 적용하지 않으며, 지원되는 Instagram 영상만 아래 시간제 계약을 따릅니다.<br>
The long-video **total-duration threshold** differs from this timer's **waiting time**. Unknown-duration videos never qualify for the long-video option; eligible Instagram videos instead follow the timer rules below.

1. Instagram을 선택하고 **진행 정보 없는 영상 · 시간제로 넘김 → 시간제 넘김**을 켭니다. 기본은 OFF이며 10초로 시작합니다.<br>
   Select Instagram and enable the timed-fallback option. It is OFF by default, initially 10 seconds.
2. 5~60 사이 정수를 입력한 뒤 완료/적용하거나 −/+로 1초씩 바꿉니다. 빈칸·부호·소수·범위 밖은 저장하지 않습니다. 20은 총 20초이지 10+20초가 아닙니다.<br>
   Type an integer from 5 to 60 and press Done/Apply, or adjust by one second with −/+. Empty, signed, fractional and out-of-range input is rejected. A value of 20 means 20 seconds total, not 10+20.
3. 메인 실행을 켜고 현재 반복 횟수를 1 이상으로 둡니다. 정상 영상은 기존 횟수, 정보 없는 지원 영상만 `10초 → 9초`처럼 표시하며 기다립니다. 시간은 횟수와 독립 저장되지만 0회에서는 작동하지 않습니다.<br>
   Turn main execution ON with a positive current repeat target. Normal videos retain counting; eligible clockless videos show remaining seconds. Seconds are saved independently of the repeat count, but timed advancing is disabled at zero plays.

정상 영상에서 중간부터 켜서 `0/N`으로 다음 시작을 기다리는 상태는 시간제로 바꾸지 않습니다. 일시정지·댓글·메뉴·앱/창 전환 등 감지된 중단은 시간을 초기화하며 복귀 후 처음부터 셉니다. 사진·혼합 콘텐츠·광고를 시간제로 우회하지 않습니다. 완주 전에 넘길 수 있고 모든 앱 UI 변경·정지를 감지한다고 보장하지 않습니다.<br>
A normal video waiting for its next beginning at `0/N` does not switch to the timer. Detected pauses, comments, menus and app/window changes reset the wait. Photos, mixed content and ads do not use this fallback. It may advance before completion, and detection is not guaranteed for every UI or interruption.

## YouTube 라이브 미리보기 / YouTube live previews

**YouTube 라이브 · 미리보기 넘김**은 YouTube 쇼츠 안에서 명시적으로 인식한 라이브 미리보기만 대상으로 하며 기본 OFF입니다. YouTube를 선택한 뒤 **라이브 미리보기 넘기기**와 **전체 자동 넘김 실행**을 모두 켜세요. **일반 반복이 0회여도 동작**하며 전체 실행 OFF는 라이브까지 멈춥니다.<br>
This option applies only to explicitly recognized live previews inside YouTube Shorts and defaults OFF. Select YouTube, then enable both the live-preview option and main execution. **It works at zero normal plays**; main OFF stops live skipping too.

대기 시간은 **0~60초, 기본 0초**입니다. 0초는 인식·안전 확인 후 바로 넘기기, 예를 들어 5초는 인식 후 5초 기다리기입니다. 숫자 입력 후 완료/‘입력한 라이브 시간 적용’을 누르거나 −/+로 1초씩 바꿉니다. 빈칸·부호·소수·61 이상은 적용하지 않습니다. **반복 0회(일반 반복 중지)와 라이브 0초(바로 넘기기)는 다른 설정**입니다.<br>
The wait is **0–60 seconds, initially 0**. Zero means advance after recognition and safety checks; five waits five seconds after recognition. Type a value and press Done/Apply, or use one-second −/+ controls. Empty, signed, fractional or over-60 input is rejected. **Zero plays stops normal repetition; zero live seconds means immediate skipping.**

일반 영상 N회·Instagram 시간제는 바꾸지 않습니다. 일반 라이브 시청 화면이나 ‘라이브’라는 글자만으로 판단하지 않으며, 부분 표시·댓글·메뉴·불명확한 화면에서는 기다립니다. 방송 참여 버튼은 누르지 않습니다. 다음 페이지를 확실히 구분하지 못하면 중복 이동 대신 안전 정지할 수 있습니다. 새 권한·OCR·화면/소리 분석은 사용하지 않습니다. code20에서 라이브0초·5초·OFF를 실제 확인했고, 조회 설정 원복을 보강한 code21에서는 일반 YouTube10연속을 통과했습니다. code21의 개별 라이브3조건은 재시험하지 않았습니다. [동작과 한계](docs/LIVE_SKIP.md).<br>
Normal repeat counting and Instagram timers are unchanged. Ordinary live watch screens or the word “live” alone are insufficient; partial, blocked or ambiguous pages wait. No join button is clicked. Uncertain page changes can safety-stop instead of issuing duplicate moves. No new permission, OCR, image or audio analysis is used. Code20 passed actual0-second/5-second/OFF live trials. Code21 hardens query cleanup and passed ten consecutive ordinary YouTube transitions; those three live trials were not repeated on code21.

## 배터리 제한 없음 / Unrestricted battery setup

**사용 준비 → 백그라운드 실행 · 배터리 → 배터리 제한 없음 설정하기 → 설정으로 이동 → 배터리 → 제한 없음**<br>
**Setup → Background execution / battery → Set unrestricted battery → Open Settings → Battery → Unrestricted**

- 이 앱의 설정 화면만 열고 사용자가 직접 변경합니다. 적용된 경우 “절전 예외 적용됨”과 “배터리 설정 확인하기”를 표시합니다. 돌아오면 다시 읽으며 실행을 자동 재시작하지 않습니다.<br>
  The app opens its own settings; you make the change. When exempt, it shows “절전 예외 적용됨” and a review button. Status refreshes on return, without restarting execution.
- 표시는 Android 절전 예외 여부입니다. 모든 제조사 절전 정책의 면제를 뜻하지 않습니다. 확인 불가이면 설정에서 직접 확인하세요. 메뉴 이름은 기기마다 다를 수 있습니다.<br>
  The status reflects Android's battery-exemption API, not every manufacturer policy. If unavailable, check manually. Menu names vary by device.
- 삼성 시험 기기에서 앱 동결을 확인했고, 제한 없음 적용 후 약56초의 작은 창 대기와 전체 화면 복귀에서 동결되지 않는 것을 확인했습니다. **장시간 보장이나 모든 정지 문제의 해결은 아닙니다.**<br>
  Freezing was observed on the tested Samsung device. After exemption, an approximately 56-second PiP-wait observation and full-screen return remained unfrozen. **This is not a long-term guarantee or a fix for every stop.**
- 배터리 소모가 늘 수 있습니다. 되돌리려면 같은 메뉴에서 **최적화**를 선택하세요. 다른 앱이나 시스템 전체 절전을 변경할 필요는 없습니다.<br>
  Battery usage may increase. Select **Optimized** in the same menu to undo. No changes to other apps or global power settings are needed.

## 광고와 빠른 설정 / Ads and Quick Settings

**광고 바로 넘기기**는 Instagram의 명시적 광고 배지를 인식할 때만 작동하며 기본 OFF입니다. **메인 실행 ON + Instagram 선택 + 광고 옵션 ON**이면 반복 0회에서도 작동합니다. 광고 옵션 OFF 또는 메인 실행 OFF이면 광고를 넘기지 않습니다. 재생바가 없거나 캡션에 #광고가 있다는 이유만으로 넘기지 않습니다. 영상 안 CTA 카드를 클릭하지 않으며 별도 팝업을 자동으로 닫지 않습니다. YouTube 광고 처리는 지원하지 않습니다.<br>
**“광고 바로 넘기기” (skip ads immediately)** is OFF by default and only acts on explicit, recognized Instagram ad badges. **Main execution ON + Instagram selected + ad option ON** allows ad skipping even at zero plays. Turning the ad option or main execution OFF stops ad skipping. Missing progress bars or #ad captions alone are not sufficient. It never clicks ad CTAs or closes separate popups. YouTube ad handling is not supported.

업데이트 전 광고 옵션이ON이었다면0.2.4부터는 반복0회에서도 광고가 넘어갈 수 있습니다. 광고를 원치 않으면 해당 옵션을 끄세요. **광고만 사용: 반복0회 + Instagram선택 + 광고ON + 긴 영상·라이브OFF + 전체 실행ON.**<br>
If ads were already ON before updating, versions from0.2.4 onward can skip them even at zero plays. Turn the option OFF if unwanted. **Ads only: zero plays + Instagram selected + ads ON + long-video and live OFF + main execution ON.**

**사용 준비 → 빠른 설정에 추가**로 “쇼츠 넘김” 타일을 추가합니다. 안 되면 휴대폰 빠른 설정 편집에서 직접 추가하세요. 짧게 누르면 실행 토글, 길게 누르면 앱 설정입니다. 패널을 열어 둔 동안은 영상 자동 넘김을 대기합니다.<br>
Add the “쇼츠 넘김” tile from setup, or manually through the phone's Quick Settings editor. Tap to toggle execution; long-press for app settings. Advancing pauses while the panel is open.

## 알려진 문제와 복구 / Known issues and recovery

- **일반 영상의 제한적 복구(0.2.6):** 정상 진행 정보를 쓰는 넘김 요청의 4.5초 확인 시간 초과에만 적용합니다. 같은 앱·창의 안전한 일반 영상에서 처음 부근과 그 뒤 실제 재생 진행을 확인한 뒤 이전 카운트를 버리고 새로 셉니다. 1회 설정이면 복구된 재생 한 회를 끝까지 확인한 후 넘기며, 시작점을 찾자마자 넘기지 않습니다. 확인 중에는 추가 스와이프·광고/라이브/시간제 우회를 하지 않습니다. [예시와 제한](docs/PLAYBACK_RECOVERY.md).<br>
  **Limited ordinary-video recovery(0.2.6):** only a4.5-second confirmation timeout after a progress-based advance is eligible. In the same safe app/window, a near-start sample followed by actual forward playback permits a fresh count. At target1, a newly tracked complete play is required before advancing; finding the start does not swipe. Recovery issues no extra swipe and cannot bypass the wait through ads, live previews or timers.
- **안전정지 유지:** 긴 영상·광고·라이브·시간제·화면 분석 실패, 제스처 거부/취소, 전환 중 앱·창 변경과 권한 문제는 위 복구 대상이 아닙니다. ‘정지’ 또는 재시작 안내가 나오면 화면과 원인을 확인하고 전체 실행을OFF→ON하세요. 배터리 예외만으로 정지가 해제되지는 않습니다. 플로팅이 없으면 앱에서 상태를 확인합니다.<br>
  **Hard stops remain:** long-video/ad/live/timer/visual failures, rejected or cancelled gestures, app/window changes during a transition and permission problems are not covered. If stopped or prompted to restart, inspect the screen and reason, then toggle main execution OFF→ON. Battery exemption alone does not clear a hard stop; check in-app status if floating controls are hidden.
- **반복 경계 누락:**18초 영상에서 마지막 관측값이16초 후0초로 바뀔 때 완주 대신 탐색으로 판단하여 카운트가 오르지 않는 사례가 있습니다. 미해결입니다.<br>
  **Missed loop boundary:** an 18-second video observed jumping from 16 to 0 can be treated as seeking rather than completion, leaving the count unchanged. This remains unresolved.
- **작은 창/PiP, 잠금 화면, 다른 앱 전면은 지원 대상이 아닙니다.** 전체 화면으로 복귀하세요. 프로세스/접근성 서비스 재연결 후에는 안전을 위해 실행 OFF이므로 직접 다시 켭니다.<br>
  **PiP, locked screens, and background target apps are not supported.** Return to full screen. Process/service reconnection intentionally turns execution OFF; re-enable it manually.
- 시간 정보가 없는 릴스는 지원되는 단일 영상에 한해 선택형 시간제를 쓰지만 완주는 보장하지 않습니다. 식별 불가능한 연속 광고, 특수 팝업 및 앱 UI 변경은 미지원 또는 추가 검증 대상입니다. 동작이 불확실하면 실행을 끄고 직접 넘기세요.<br>
  Eligible clockless single-video Reels can use the optional timer, without a completion guarantee. Indistinguishable consecutive ads, special popups and changed app UIs may remain unsupported. If uncertain, stop execution and advance manually.

과거code28에서 YouTube20연속 전환을 확인했지만0.2.9의 새 시험 결과는 아닙니다. 같은 길이 영상 쌍의 실기기 관측·드문 timeout 복구·모든 알림 상황은 별도 한계입니다. 표시 언어 번역이 호스트 감지 언어를 확장하지는 않습니다. 기존 판독은 한국어·영어 및 지원되는 시계 표기 중심이며 다른 언어의 모든 영상·광고를 보장하지 않습니다. 시스템 언어 변경 중에도 안전 대기·정지 조건을 유지하며, 복귀 후 재시작 안내가 있으면 화면을 확인하고 전체 실행을OFF→ON하세요. 상세 기록은 [디버그 대장](docs/DEBUG_LOG.md)에 보존합니다.<br>
Historical code28 passed the YouTube20 run; this is not a new0.2.9 test result. The exact equal-duration pair,rare-timeout recovery and universal notification behavior remain unverified. UI translation does not expand host detection: readers focus on Korean/English and supported clock formats, not all videos or ads in every language. Language changes preserve safety waits/stops; if restart is requested after returning, inspect the screen and toggle main execution OFF→ON. See the debug log for evidence and limits.

## 데이터와 업데이트 / Data and updates

업데이트 조회 정책은 바꾸지 않습니다. 고정 저장소의 공개 릴리스 목록에서 draft를 제외하고 **공개된 prerelease도 포함**해,유효한 메타데이터·더 높은 versionCode·기기 OS 호환성을 만족하는 항목을 확인합니다. 설치 전 크기·SHA256·패키지·버전·현재 설치본과 같은 서명 집합을 검사하고 Android의 최종 설치 확인을 거칩니다. 앱의 중립적인 버전 표시가 prerelease를 제외한다는 뜻은 아닙니다.

Update selection is unchanged:exclude drafts from the fixed repository's public release feed,but **include published prereleases** with valid metadata,a higher versionCode and compatible OS. Size,SHA256,package,version and the installed signer set are checked before Android's final installation confirmation. Neutral version wording does not imply excluding prereleases.

**업데이트 · 앱 정보**에서 설치 버전과 업데이트 상태를 확인합니다. 기본 ON인 **앱 열 때 새 버전 확인**은 앱을 열거나 돌아올 때 마지막 시도 후 24시간이 지났으면 GitHub를 조회합니다. OFF로 바꿔도 수동 확인은 가능합니다. 앱을 닫은 동안의 주기 감시·시스템 알림·자동 다운로드는 없습니다. 적용 가능한 더 높은 버전이 확인된 경우에만 상단 배너가 나타나며, 누르면 업데이트 카드로 이동합니다.<br>
Open **업데이트 · 앱 정보** (updates/app information). **앱 열 때 새 버전 확인** (check on opening), ON by default, checks GitHub when opening/returning to the app after 24 hours since the last attempt. It can be turned OFF; manual checking remains available. There is no background polling, system notification or automatic download. A top banner appears only for a known compatible newer version and opens the update card.

1. **업데이트 확인**: 지금 조회합니다. 새 버전이 없거나 통신에 실패해도 기존 앱을 계속 사용할 수 있습니다.<br>
   **Check**: request a fresh check. No update or a network failure does not prevent existing use.
2. **업데이트 다운로드**: 직접 눌러 받습니다. 진행률을 확인하거나 **다운로드 취소**를 누를 수 있습니다. 크기·SHA256·앱 ID·버전·서명을 검사하며 검사 실패 시 설치하지 않습니다.<br>
   **Download**: start explicitly, view progress or cancel. Size, SHA256, package, version and signing identity are checked; failure prevents installation.
3. **업데이트 설치**: 이 버튼을 누르면 안전을 위해 **전체 실행이 OFF**가 됩니다. 횟수·앱 선택·광고·라이브·시간·플로팅 위치 등 다른 설정은 유지합니다. 설치 허용이 필요하면 안내에서 Android 설정을 열고 **이 출처 허용**을 직접 선택합니다. 앱으로 돌아와 **업데이트 설치**를 다시 누르고 시스템 설치 창에서 최종 확인합니다.<br>
   **Install**: pressing it turns **main execution OFF**, preserving other settings. If required, open Android Settings and manually allow installation from this source. Return, press Install again, then confirm in the system installer.
4. 취소하면 기존 앱은 그대로입니다. 설치를 시작한 뒤 취소했더라도 실행은 OFF로 유지됩니다. 설치 후 앱을 열어 버전을 확인하고 필요한 때 직접 실행을 켜세요.<br>
   Cancelling leaves the existing app intact; execution remains OFF after entering installation, even if cancelled. After installation, reopen, verify the version and re-enable execution yourself.

0.2.5는 **INTERNET**(GitHub 업데이트 조회·다운로드)과 **REQUEST_INSTALL_PACKAGES**(사용자가 고른 APK를 Android 설치 창으로 연결)를 사용합니다. 설치 허용과 최종 설치는 자동 승인하지 않습니다. 설정·플로팅 위치·업데이트 확인 상태는 기기에 저장하고 클라우드 백업은 제외합니다. 영상·계정·시청이력은 저장·전송하지 않습니다. 접근성 화면 정보는 감지에 일시적으로 사용합니다. 개인 화면 캡처·디버그 자료·서명 개인키는 이 저장소에 포함하지 않습니다.<br>
0.2.5 uses **INTERNET** for GitHub update checks/downloads and **REQUEST_INSTALL_PACKAGES** to hand a user-selected update to Android's installer. Permission and installation are never approved automatically. Settings, position and update-check state stay local, with cloud backup disabled. Videos, accounts and viewing history are not persisted or uploaded. Accessibility data is transient; personal captures, logs and private signing keys are excluded.

기존 앱과 서명이 같으면 APK 덮어쓰기 설치로 설정을 유지할 수 있습니다. 다른 서명의 빌드는 업데이트되지 않을 수 있으며, 삭제 후 재설치하면 저장값이 사라집니다. 이 Release의 APK와 개인/CI 재빌드의 서명은 같지 않을 수 있습니다.<br>
An APK signed with the same key can update the installed app while preserving settings. Different signatures may prevent an update; uninstalling loses saved settings. Personal or CI rebuilds may use a different key from the release APK.

## 빌드·시험 / Build and test

0.2.9의 배포 대상은 **release 빌드·debuggable=false·기존 서명 유지**를 확인한 APK입니다. 빌드 유형만 바꾸고 서명이 달라지면 기존 앱 위 업데이트가 되지 않으므로 서명 집합·패키지·버전·설정 보존도 확인합니다. 파일명은 `shorts-loop-v0.2.9.apk`이며 최종 산출물과 시험·공개 결과는 [검증 기록](docs/VERIFICATION.md)에 누적합니다. 이 명령 안내 자체는 검사 완료 선언이 아닙니다.<br>
The0.2.9 distribution target is a **release build with debuggable=false and the existing signer**. Changing build type alone is insufficient:the signer set,package,version and settings preservation must also be checked. The filename is `shorts-loop-v0.2.9.apk`; final artifact, test and publication evidence belongs in the verification record. These instructions do not declare that checks have completed.

### 배포용 release 검증 / Distribution release checks

[release 빌드·서명·검사 안내](docs/RELEASE_BUILD.md)에서 개발용 빌드와 배포물의 차이를 확인하세요. / See the release-build guide for signing and artifact checks.

아래는 확정된 검사 절차이며 실행 결과는 아직 미확정입니다. 동일 서명을 유지할 로컬 환경의 `SHORTSLOOP_KEYSTORE`, `SHORTSLOOP_STORE_PASSWORD`, `SHORTSLOOP_KEY_ALIAS`, `SHORTSLOOP_KEY_PASSWORD`를 사용합니다. 키 파일·경로·암호 값은 저장소에 기록하지 않습니다.

```powershell
.\scripts\verify.ps1 -BuildType release
.\scripts\verify-release-safety.ps1 -Apk app/build/outputs/apk/release/app-release.apk -DebugApk app/build/outputs/apk/debug/app-debug.apk
.\scripts\prepare-release.ps1 -Apk app/build/outputs/apk/release/app-release.apk
```

안전 검사의 비교용 debug APK는 아래 개발용 절차로 별도 준비합니다. 로컬 서명 출력은 `app/build/outputs/apk/release/app-release.apk`이며, 포장 스크립트는 안전 검사 후 `artifacts/release-v0.2.9-code31/shorts-loop-v0.2.9.apk`와 SHA256·업데이트 JSON을 만듭니다. 기존 출력 폴더는 덮어쓰지 않습니다. CI는 서명 비밀 없이 release 빌드·단위시험·lint와 debug 경로를 검사하며 CI APK를 공개 업데이트로 대체하지 않습니다.<br>
These are the release-check commands, not completed results. Keep signing secrets local, prepare the comparison debug APK separately, and publish only the verified locally signed artifact. CI checks both build variants without the signing secrets.

### 개발용 debug 검증 / Development-only debug checks

아래 기존 명령은 개발용 debug APK 확인 절차이며0.2.9 배포 APK를 만드는 최종 검증을 대신하지 않습니다. `app-debug.apk`를 새 공개 파일로 올리지 않습니다.<br>
The existing commands below check a development debug APK;they do not replace0.2.9 release-artifact verification. Do not publish `app-debug.apk` as the new distribution artifact.

JDK17 이상, Android SDK35, Build Tools35.0.0, Gradle wrapper8.9 / Android Gradle Plugin8.7.3. SDK는 `ANDROID_HOME` 또는 Git에서 제외된 `local.properties`로 지정합니다. 빌드 의존성 다운로드에는 인터넷이 필요합니다.<br>
Use JDK17+, Android SDK35, Build Tools35.0.0, Gradle wrapper8.9 / AGP8.7.3. Set `ANDROID_HOME` or an ignored `local.properties`. Building requires dependency downloads.

Windows의 `verify.ps1`로 사용자 지정 SDK 경로를 쓰려면 `ANDROID_HOME`도 설정해야 합니다. 이 스크립트의 직접JUnit 단계는 `local.properties`를 읽지 않고 `ANDROID_HOME` 또는 기본 SDK 경로를 사용합니다.<br>
For a custom SDK location with Windows `verify.ps1`, also set `ANDROID_HOME`: its direct JUnit step uses that variable or the default SDK location, not `local.properties`.

Windows:
```powershell
.\scripts\verify.ps1
```

Linux 개발용 / Linux development:
```sh
./gradlew --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

개발용 출력 / Development output: `app/build/outputs/apk/debug/app-debug.apk`.

### 과거 빌드·검증 이력 / Historical build evidence

이전 미배포code26은 PC·설치·지정시험 일부를 통과한 뒤 YouTube 후속 확인 실패로 중단한 후보입니다. 해당454시험·209/209/208계측·APK해시·Instagram10회 근거는 [버전별 검증](docs/VERIFICATION.md)에 보존하며 code28 결과와 혼동하지 않습니다.<br>
Historical code26 passed PC/installation and designated checks before a later YouTube confirmation failure prevented release. Its454 tests,209/209/208 emulator checks,artifact hash and Instagram10 evidence are preserved in version-specific verification,not presented as code28 results.

0.2.5(code21): **빌드·제품356시험·lint0오류/기존3경고, Android8/13/14 계측74/74/73개 PASS**. 실제 Android17에서 설치·접근성 연결·설정 보존·동일 APK 해시 및 일반 YouTube10연속 자동 전환(424.5초)을 확인했습니다. 10개에 라이브는 없었으며20연속은 미완료입니다. 고정 APK709703bytes, SHA-256은 [0.2.5 릴리스 기록](docs/releases/v0.2.5.md), 최종 게시 확인은 [VERIFICATION](docs/VERIFICATION.md) 참조. 아래는 **이전 0.2.4의 이력**입니다.<br>
0.2.5(code21): **build,356 product tests,lint0 errors/3 existing warnings and74/74/73 Android8/13/14 emulator checks passed**. Android17 installation,accessibility binding,preferences,exact-APK hash and ten consecutive ordinary YouTube transitions(424.5 seconds) passed. No live preview occurred;20 transitions remain incomplete. The frozen APK is709703 bytes; see the linked release record for SHA-256 and VERIFICATION for publication checks. The following results are **historical0.2.4 evidence**.

최종 0.2.4(code13): **제품 범위 227개 JUnit·빌드 PASS, lint 오류 0/경고 4**. 광고 독립·UI 변경을 포함한 APK의 서명과 휴대폰 설치본 SHA256 일치를 확인했습니다. [릴리스 기록](docs/releases/v0.2.4.md). Windows 한글 경로의 Gradle 시험 실행기 문제는 동일 컴파일 결과의 직접 JUnit 실행으로 우회했습니다.<br>
Final 0.2.4 (code13): **227 product tests and build PASS; lint 0 errors/4 warnings**. The APK includes independent ads and grouped UI; signature verification and installed-APK SHA256 matching passed. See release notes. Direct JUnit works around the Windows Unicode-path Gradle test-worker issue.

[GitHub CI](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33074271656)도 제품227개·빌드 PASS, lint0오류/3경고입니다. 익명 공개 다운로드 파일은 로컬/설치본과 같은681624bytes·SHA256이며 v2서명도 확인했습니다.<br>
GitHub CI also passed 227 product tests and build, with lint 0 errors/3 warnings. The anonymously downloaded APK matches the local/installed 681624-byte file and SHA256; its v2 signature verifies.

별도 미연결 `VisualSequenceTracker` 실험은 **20개 중 18 PASS/2 FAIL**입니다. 영상 주기 오추정·학습 실패가 남아 있으며 시간제 제품 기능의 실패와 구분합니다. 실험 원본을 보존하되 배포 앱과 제품 시험에서 제외합니다. 따라서 제품 227 PASS를 이 실험까지 포함한 전체 PASS로 표현하지 않습니다.<br>
The separate, unwired `VisualSequenceTracker` experiment has **18 PASS/2 FAIL out of 20 tests**, involving period estimation and learning. These are not timed-fallback failures. Its source is preserved but excluded from the release app and product tests. Product tests passing does not mean this experiment passes.

**과거0.2.4의 실기기20연속 검증은 미완료였습니다.** 정상 N회·시간제·광고의 관측 종류를 구분하며 후보별 관측을 합산하거나 수동 이동을 자동 성공으로 세지 않습니다. 0.2.4 최종 APK에서 **0회 광고 1회 자동 이동, 이후 일반·시간제 중지**를 확인했습니다. 여러 기기·장시간·모든 중단 경로 검증은 남아 있습니다.<br>
**The historical0.2.4 twenty-transition device test remained incomplete.** Normal, timed and ad observations are distinguished; candidate sessions are not combined and manual swipes do not count as automatic successes. The final 0.2.4 APK **automatically advanced one ad at zero plays, then kept normal/timed advancing stopped**. Other devices, long sessions and all interruption paths remain unverified.

## 과거 버전 기록 / Version history

아래의0.2.8 이하 빌드·기기·게시 결과는 버전별 과거 기록입니다. 당시 표시·배포 상태와 검증 수치는 보존하지만0.2.9의 PASS나 공개 완료 근거로 재사용하지 않습니다.<br>
Evidence for0.2.8 and earlier remains historical; it is not a0.2.9 test or publication result.

### 이전 0.2.8/code30 / Previous0.2.8

앱명·버전만 표시하는 non-debuggable release APK로 기존 패키지·서명을 유지했습니다. [0.2.8 APK](https://github.com/fullmetalsonic/shorts-loop/releases/download/v0.2.8/shorts-loop-v0.2.8.apk) · [릴리스 기록](docs/releases/v0.2.8.md). APK **647418 bytes**, SHA256 `FAA554B16AD5A374A07057FDF2F2195931F77AE77ACC67E2E154A366108F012C`. GitHub Public·draft=false·prerelease=false, 2026-08-28 공개·제품CI·익명 APK/체크섬/메타데이터 동일성 검증 완료. 새 호스트 연속시험을 수행한 버전은 아닙니다.<br>
Version0.2.8 used neutral app/version labels and a non-debuggable release APK with the existing package and signer. Its647418-byte APK and SHA256 above were verified with Public visibility,draft/prerelease false,product CI and anonymous APK/checksum/metadata parity on2026-08-28. It did not add a new host endurance run.

<details>
<summary>0.2.7·0.2.6의 기존 설명과 검증 기록 / Earlier descriptions and evidence</summary>

**과거0.2.7 공개 기록.** [0.2.7 다운로드](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.7) · GitHub 빌드/468시험 PASS · 공개APK 원본해시 일치. 인앱 **업데이트 · 앱 정보 → 업데이트 확인**에서도 확인할 수 있습니다.<br>
**Historical0.2.7 publication record.** GitHub build/468 tests passed;the public APK matches the verified original. Check for updates from the in-app update section or use the0.2.7 release link.

**0.2.7/code29**는 플로팅72×56dp를 유지하면서 ‘긴영상’ 잘림을 수정한 정식 배포 버전입니다. 빌드·468JUnit·Android8/13/14 계측·휴대폰 표시/조작 검증을 마쳤습니다. 게시 상태와 CI·공개 파일 검증은 [0.2.7 릴리스 기록](docs/releases/v0.2.7.md),구체적인 검증 범위와 한계는 [플로팅 수정 기록](docs/FLOATING_LAYOUT_FIX.md)을 확인하세요. ‘화면 분석 보조’는 여전히 선택형 실험 기능입니다.<br>
**0.2.7/code29** is the stable-distribution version fixing label clipping while preserving the72×56dp overlay. Build,468JUnit,Android8/13/14 native checks and physical display/interaction checks passed. See the release record for publication/CI/public-file verification and the floating report for scope and limits. Optional visual assistance remains experimental.

## 이전 0.2.6/code28 · Public 시험판 기록 / Previous pre-release

**0.2.6/code28 공개 시험판(pre-release)을 게시하고 공개 파일 검증까지 완료했다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,로컬lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 is published as a public pre-release,and public artifact verification is complete.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;local lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증 후 기존 Public 저장소에v0.2.6/code28 pre-release를 게시했으며 CI·공개 다운로드 동일성도 확인했다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. After the YouTube retest and impact-scope checks passed,v0.2.6/code28 was published as a pre-release in the existing Public repository. CI and public-download parity were verified.

[GitHub CI33141470669](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33141470669)는468시험(32suites,실패/오류/건너뜀0) PASS,CI lint0오류/2경고입니다. 로컬lint의3경고와 구분합니다. 공개APK·SHA256텍스트·업데이트JSON은 고정본과 크기·해시가 일치하며 독립 검토 범위에서P1/P2 0건입니다. 내비게이션 오버레이 실기기 시나리오는 미실행입니다.<br>
GitHub CI passed468 tests across32 suites with0 failures/errors/skips and lint0 errors/2 warnings,distinct from3 local warnings. Public APK,SHA256 text and update JSON match the frozen files. Independent review found0 P1/P2 issues in scope;the navigation-overlay device scenario remains unrun.

## 이전 후보 기록 / Earlier candidates

code23~26은 실제 전환 확인 실패를 발견해 배포하지 않았습니다. code26의 Instagram10회 성공은 유지 근거로 보존하며,과거 실패 원인·버전별 PC/설치/기기 수치는 [디버그 대장](docs/DEBUG_LOG.md)과 [검증 원장](docs/VERIFICATION.md)에 남겼습니다. 당시 게시 대상은 **code28**이었습니다.<br>
Code23–26 were not released after physical transition-confirmation failures. Code26's Instagram10 PASS is retained;earlier causes and version-specific PC/installation/device evidence remain in the debug and verification records. **Code28** was that delivery candidate.

## 과거0.2.6 기능·검증 기록 / Historical0.2.6 checkpoint

**0.2.6(code28)은 검증 후 Public 시험판으로 공개했습니다.** 일반 영상의 넘김 확인 시간이 초과되면 추가 스와이프 없이 다음 재생 시작을 확인하고, 이전 카운트를 버린 뒤 설정 횟수를 새로 세는 제한적 복구를 추가합니다. 광고·라이브·시간제 및 권한·화면 변경의 안전정지는 유지합니다. 이번 YouTube 한정 보완은 YouTube를 재시험하며,변경하지 않은 Instagram 경로는 code26의 별도10회 성공 기록을 보존합니다. code28의 YouTube20회 PASS와 이전 Instagram 근거를 별개로 기록합니다. [복구 동작](docs/PLAYBACK_RECOVERY.md), [검증과 한계](docs/VERIFICATION.md).<br>
**0.2.6(code28) is published as a verified Public pre-release.** After an ordinary progress-based transition times out, limited recovery observes a fresh playback start without another swipe, discards the old count and counts the configured plays anew. Ad, live, timer, permission and screen-change safety stops remain. This YouTube-only correction is retested on YouTube;the unchanged Instagram path retains its separate code26 result. Code28's YouTube20 PASS remains separate from the retained Instagram evidence.

**code28은 선택형 ‘긴 영상 건너뛰기’도 통합합니다.** 기본 OFF·기준60초, 입력/−/+로1~3600초를 설정합니다. 선택한 YouTube·Instagram 일반 영상의 확인된 총길이가 **기준 이상(≥)**이면 처음 재생이나 완주를 기다리지 않고, 화면 안정과 실제 진행을 확인한 뒤 넘깁니다. 반복0과 독립이며 전체 실행OFF는 중지합니다. 총길이를 모르면 추측하지 않습니다. 이전 후보의 전환 횟수는 code28의20회 결과에 합산하지 않습니다.<br>
**Code28 also adds optional long-video skipping.** OFF by default, initially60 seconds, adjustable from1 to3600 with typing and−/+. An ordinary video in a selected YouTube/Instagram app qualifies when its known total duration is **at least(≥)** the threshold. It advances after stable-screen and actual-progress checks, without waiting for the beginning or completion. It is independent of zero plays; overall OFF stops it. Unknown duration is never guessed. Earlier candidates' transitions do not count toward code28's twenty.

이전 공개판 **0.2.5(code21)**는 [Android 버전별 기능 안내](docs/COMPATIBILITY.md), GitHub 인앱 업데이트, 메뉴 배치와 선택형 YouTube 라이브 넘김을 통합했습니다. 해당 버전의 빌드·356제품시험·일반 YouTube10연속·공개 파일 동일성 결과를 0.2.6 검증으로 재사용하지 않습니다.<br>
The previous public **0.2.5(code21)** combined per-Android guidance, in-app GitHub updates, reordered settings and optional YouTube live skipping. Its build,356 product tests,ten ordinary YouTube transitions and artifact-parity results do not verify0.2.6.

일반 YouTube 쇼츠와 Instagram 릴스를 정한 횟수만큼 보고 다음 영상으로 넘기는 Android 시험판입니다. **code28 YouTube20연속 전환 시험은 PASS**이며,Instagram은 변경하지 않은 경로의 code26 별도10회 PASS를 유지합니다. 모든 영상·기기에서의 안정성을 보장하지 않으며 이전 버전의 결과와 이번 검증을 구분합니다.

The historical description called this an experimental Android app that advances supported YouTube Shorts and Instagram Reels after a chosen number of plays. **Code28 passed twenty consecutive YouTube transitions.** The unchanged Instagram path retains code26's separate ten-transition PASS. Reliability across every video/device is not guaranteed; historical results remain separate from this release's checks.

**진행 정보 없는 Instagram 영상만 시간제로 넘김.** 기본 OFF·10초, 숫자 입력과 ＋/－로 5~60초를 1초씩 조절합니다. 정상 진행 정보가 있고 긴 영상 건너뛰기 조건에 해당하지 않는 영상은 기존 재생 횟수대로 넘깁니다. 반복2회/시간10초이면 해당 정상 영상은2회, 정보 없는 대상 영상은 **총10초** 뒤 이동합니다. 완주 횟수나 영상 길이를 알아내는 기능은 아닙니다. [설정·안전 동작·검증](docs/TIMED_FALLBACK.md).

**Timed fallback for clockless Instagram videos only.** OFF by default, initially10 seconds, adjustable from5 to60 with typing and one-second−/+ controls. Videos with usable clocks that do not qualify for long-video skipping retain repeat counting. With2 plays/10 seconds, those ordinary videos use2 plays; eligible clockless videos wait **10 seconds total**, not20. This does not infer completion or duration.

**반복 0회와 실행 OFF는 다릅니다.** 0회는 일반 영상 반복·시간제 넘김을 중지합니다. Instagram 선택 + 광고 옵션 ON + 메인 실행 ON이면 **0회여도 인식된 광고는 넘깁니다**. YouTube 선택 + 라이브 옵션 ON + 메인 실행 ON이면 인식된 라이브 미리보기도 반복 0회와 독립적으로 넘깁니다. 메인 실행 OFF는 광고·라이브까지 모두 중지합니다.

**Zero plays differs from execution OFF.** Zero stops normal repeat-based and timed advancing. Recognized Instagram ads can still advance at zero when Instagram is selected and both the ad option and main execution are ON. Recognized YouTube live previews are also independent of zero plays when YouTube, the live option and main execution are enabled. Main execution OFF stops everything, including ads and live previews.

**긴 영상 옵션도0회와 독립**입니다. 예를 들어 반복0·긴 영상ON·기준60초·전체 실행ON이면 총길이를 확인한60초 이상 일반 영상은 넘기고, 짧은 영상이나 길이를 모르는 일반 영상은 직접 넘깁니다. 광고만 사용하려면 긴 영상·라이브 옵션을 함께 끄세요.<br>
**Long-video skipping is also independent of zero plays.** With target0,long-video ON,threshold60 and overall ON, known ordinary videos of60 seconds or more advance; shorter or unknown-duration ordinary videos remain manual. For ads only, turn both long-video and live options OFF.

**기존 화면 분석 시험 기능은 선택형으로 보존됩니다.** 기본 OFF이며 Android 14 이상에서 별도 동의 후 화면을 RAM에서 비교합니다. 시간제와 함께 켜면 시간제가 우선하고 화면 분석 선택값은 보존합니다. 학습 때문에 추가 재생될 수 있으며 정확한 총 N회를 보장하지 않습니다. [화면 분석 안내](docs/VISUAL_ASSIST_TRIAL.md).

**The existing visual-assist trial remains optional.** It is OFF by default, separately consented, Android 14+, and RAM-only. Timed fallback takes priority when both are enabled, preserving the visual preference. Learning can add plays; exact total N is not guaranteed.

[0.2.7 정식 릴리스 / Stable0.2.7](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.7) · [이전0.2.6 / Previous0.2.6](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.6) · [검증 / Verification](docs/VERIFICATION.md)


## 과거code28 빌드·기기·공개 검증 / Historical build verification

과거 code28은468JUnit·빌드·정적 가드·API26/33/34 계측233/233/232·설치 설정/접근성/런타임/해시 검증 PASS입니다. lint는0오류/기존3경고이며 고정APK는746246bytes입니다. 실제 YouTube20회는148.6초·요청20/확인20으로 통과했고,공개 CI·APK/SHA텍스트/업데이트JSON 동일성까지 확인했습니다. [code28 SHA256·검증 원장](docs/VERIFICATION.md). 아래 code26/21/13은 과거 결과입니다.<br>
Code28 passed468 tests,build,static guards,233/233/232 API26/33/34 checks and installation/settings/accessibility/runtime/hash verification. Lint has0 errors/3 existing warnings;the frozen APK is746246 bytes. YouTube20 passed in148.6 seconds with20 requests/20 confirmations;public CI and APK/SHA-text/update-JSON parity passed;code26/21/13 results below are historical.

</details>

## 별도 실험과 이전 공개 이력 / Separate experiments and release history

- [내부 오디오 수신](docs/AUDIO_PROBE_TRIAL.md)과 [음향 반복 후보](docs/AUDIO_PATTERN_TRIAL.md)는 기존 제품과 분리된 진단 앱입니다. 제품 자동 넘김에 연결하지 않았으며 오디오 시험 APK는 이번 릴리스에 첨부하지 않습니다.<br>
  The audio probe and pattern trials are separate diagnostic apps, not connected to product auto-advance. Their APK is not included in this release.
- [이전 공개 0.2.2](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.2)는 배터리 안내 추가 버전이며 시간제·화면 분석을 포함하지 않습니다. 당시 [GitHub CI](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33049522094)는 빌드·148시험·lint 0오류/1경고 PASS, 공개 APK와 로컬 원본의 크기·SHA256 일치를 확인했습니다. 이 과거 결과는 0.2.4 검증이 아닙니다. [이력](docs/releases/v0.2.2.md).<br>
  The previous public 0.2.2 release added battery guidance and lacks timed fallback and visual assist. Its CI passed build, 148 tests and lint (0 errors/1 warning), and its downloaded APK matched the local original. These historical results do not verify 0.2.4.

## 문서 / Documentation

- [다음 업데이트: 분할 화면·앱별 플로팅 계획(미구현) / Split-screen and per-host floating plan(not implemented)](docs/SPLIT_SCREEN_PLAN.md)

이 README는 한영 사용 안내입니다. 아래 상세 개발·시험 문서는 주로 한국어입니다.<br>
This README is the bilingual user guide. Detailed development and test records below are primarily Korean.

- [상세 사용 설명서 / Detailed guide](docs/USER_GUIDE.md)
- [한국어·영어 선택과 감지 언어 한계 / Localization and detection-language limits](docs/LOCALIZATION.md)
- [Android 버전별 지원 / Per-Android support](docs/COMPATIBILITY.md)
- [시간제 보조 설정·한계 / Timed fallback](docs/TIMED_FALLBACK.md)
- [YouTube 라이브 설정·한계 / Live-preview skipping](docs/LIVE_SKIP.md)
- [새 시작점 재인식·카운트 복구 / Fresh-start count recovery](docs/PLAYBACK_RECOVERY.md)
- [제품 기준 / Behavior contract](docs/PRODUCT_SPEC.md)
- [UI·인간공학 기준 / UI and usability](docs/UI_DESIGN.md)
- [0.2.7 플로팅 잘림 수정·검증 / Compact-label fix and verification](docs/FLOATING_LAYOUT_FIX.md)
- [과거0.2.7 릴리스 기록 / Historical0.2.7 release](docs/releases/v0.2.7.md)
- [검증 / Verification](docs/VERIFICATION.md)
- [디버그 대장 / Debug log](docs/DEBUG_LOG.md)
- [누적이력 / Changelog](docs/CHANGELOG.md)
- [인수인계·문서 색인 / Handover and index](HANDOVER.md)
- [0.2.4 공개 기록 / Release record](docs/releases/v0.2.4.md)
- [이전 0.2.2 릴리스 / Previous release record](docs/releases/v0.2.2.md)

YouTube 및 Instagram과 무관한 개인 프로젝트입니다. 별도의 오픈소스 라이선스는 아직 지정하지 않았습니다.<br>
An independent project, not affiliated with YouTube or Instagram. No separate open-source license has been designated yet.

## 원본 출처 표시 요청 / Attribution request

이 프로젝트를 기반으로 수정하거나 파생 버전을 공유·배포할 때는 README 또는 앱 소개에 **ShortsLoop 원본 프로젝트**와 [원본 저장소](https://github.com/fullmetalsonic/shorts-loop)를 표시해 주세요. 원본과 자체 수정 사항을 구분하고, 수정 버전을 원작자가 제작·보증한 것처럼 표시하지 말아 주세요.<br>
When sharing or distributing a modified or derived version, please credit **ShortsLoop** and link to the [original repository](https://github.com/fullmetalsonic/shorts-loop) in your README or app information. Distinguish your changes from the original, and do not imply that the original author created or endorsed the modified version.

표시 예: `Based on ShortsLoop — https://github.com/fullmetalsonic/shorts-loop (modified by [your name])`.<br>
Example: `Based on ShortsLoop — https://github.com/fullmetalsonic/shorts-loop (modified by [your name])`.

이 문구는 출처 표시 요청이며, 별도의 오픈소스 라이선스 지정이나 추가적인 이용 권한 부여를 대신하지 않습니다.<br>
This is an attribution request, not a substitute for a separately designated open-source license or an additional grant of usage rights.
