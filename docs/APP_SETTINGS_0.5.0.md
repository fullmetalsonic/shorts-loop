# ShortsLoop 0.5.0 · 앱별 설정 / App settings

이 문서는 0.5.0의 앱별 설정 화면과 동작 기준을 설명합니다. 새 버전의 실제 휴대폰 자동 넘김 시험은 **NOT RUN**입니다. PC 시험이나 이전 버전의 화면 관측을 새 버전의 실제 자동 넘김 성공으로 간주하지 않습니다. 최종 빌드·배포·시험 결과는 [검증 원장](VERIFICATION.md)을 확인하세요.

This guide describes the per-app settings and behavior of 0.5.0. Physical-phone automatic-advance testing of this version is **NOT RUN**. PC checks and observations of earlier versions do not establish successful automatic navigation in this version. See the [verification ledger](VERIFICATION.md) for final build, delivery and test results.

## 한국어 사용법

### 1. 홈에서 앱 선택하기

1. 홈의 **앱별 설정**에서 `YouTube 설정`, `Instagram 설정`, `TikTok 설정` 중 하나를 누릅니다.
2. 상세 화면의 **이 앱 자동 넘김 사용**을 켭니다. 앱이 설치되어 있어야 선택할 수 있습니다.
3. 해당 앱의 반복 횟수와 필요한 옵션을 설정합니다. 다른 앱의 설정은 바뀌지 않습니다.
4. 접근성 연결 등 필요한 설정을 완료하고, 화면 하단의 **전체 자동 넘김 실행**을 켭니다. 이 스위치는 선택한 모든 앱의 실행을 함께 제어합니다.
5. `‹ 홈` 또는 Android 뒤로 가기를 누르면 홈으로 돌아옵니다. 화면 이동 자체는 자동 넘김을 시작하거나 숫자 입력을 저장하지 않습니다.

홈에는 앱별 상태와 반복 횟수가 표시됩니다. **선택됨 · 전체 실행 꺼짐**은 앱 선택은 저장되어 있지만 아직 실행하지 않는 상태입니다. 설정 화면을 여는 것과 그 앱을 실행 대상으로 켜는 것은 별개입니다.

숫자를 직접 입력했으면 키보드의 **완료** 또는 해당 **적용** 버튼을 누르세요. 화살표와 `+/−`는 변경값을 바로 저장합니다. 스위치·모드 선택은 즉시 반영됩니다. 기능이나 전체 실행을 켤 때 필요한 미완료 숫자도 검사하며, 잘못된 입력이 있으면 해당 화면에서 정정해야 합니다. 앱을 바꿔 보거나 홈으로 돌아온 것만으로 입력 중인 값이 적용되지는 않습니다.

### 2. 앱별로 사용할 수 있는 규칙

| 규칙 | YouTube | Instagram | TikTok |
| --- | --- | --- | --- |
| 일반 영상 · 횟수로 넘김 | 읽을 수 있는 재생 정보 | 읽을 수 있는 재생 정보 | 추천 피드의 읽을 수 있는 비율 진행값 또는 실제 재생 시간 |
| 긴 영상 건너뛰기 | 실제 총길이가 확인될 때 | 실제 총길이가 확인될 때 | 실제 총길이가 확인될 때만. 비율 진행값으로 길이를 추정하지 않음 |
| 광고 · 자동 넘김 | 별도 광고 규칙 없음 | 인식된 광고 | 지원하는 구조로 인식된 광고 |
| 진행 정보 없는 영상 · 시간제로 넘김 | 별도 시간제 없음 | 안전한 단일 영상으로 확인될 때 | 안전한 일반 영상 구조로 확인될 때 |
| 사진 콘텐츠 | 별도 사진 규칙 없음 | 인식된 사진 릴스 | 인식된 사진 게시물 |
| 라이브 미리보기 넘기기 | 전용 미리보기 요소가 확인될 때 | 미지원 | 미지원 |
| 화면 분석 실험기능 | 미지원 | Android 14 이상에서 별도 선택 | 미지원 |

기능이 있다는 뜻이 모든 콘텐츠·언어·앱 버전에서 인식된다는 뜻은 아닙니다. 댓글, 메뉴, 웹 화면, 혼합 콘텐츠, 불명확한 구조는 대기할 수 있습니다. TikTok 지원 대상은 현재 확인한 `com.ss.android.ugc.trill` 앱의 추천 피드이며, 다른 TikTok 패키지나 모든 광고 형식까지 보장하지 않습니다.

### 3. 일반 영상 · 횟수로 넘김

- 범위는 **0~99회**입니다. `2`를 입력하고 **입력한 횟수 적용**을 누르면, 확인 가능한 온전한 재생을 총 두 번 관측한 뒤 넘깁니다. 처음 한 번에 두 번을 추가하는 의미가 아닙니다.
- **0회는 일반 반복과 진행 정보 없는 영상의 시간제를 중지**합니다. 화면 분석도 중지합니다. 전체 실행 OFF와는 다릅니다.
- 켜 둔 광고·사진·긴 영상 규칙과 YouTube 라이브 규칙은 0회에서도 별도로 동작합니다.
- 영상 중간에 시작하거나 추적을 잃으면 시작점을 다시 기다릴 수 있습니다. 숫자 `0`이 보인다는 이유만으로 진행 정보 없는 영상으로 판정하지 않습니다.

### 4. 광고 · 자동 넘김

Instagram 또는 TikTok 상세 화면에서 **광고 자동 넘김**을 켜고 **넘기기 전 대기 시간 (초)**을 설정합니다.

- 범위 **0.0~9.9초**, `+/−`는 **0.1초**씩, 최초값은 **0.0초**입니다. 예: `1.5` 입력 → **광고 대기 시간 적용**.
- **0.0초는 끄기가 아니라 안전 확인 후 즉시 요청**입니다. 화면 인식과 입력 대기 때문에 정확히 0밀리초에 이동한다는 뜻은 아닙니다.
- 양수는 같은 광고가 안전하게 유지되는 동안 넘기기 전에 기다리는 시간입니다. 기기 부하·화면 관측·다른 앱 입력 대기 때문에 실제 이동은 설정 시간보다 늦을 수 있습니다.
- 광고 옵션이 켜져 있으면 인식된 광고에서 일반 반복·시간제·사진·긴 영상보다 먼저 처리합니다.
- TikTok에서 광고 옵션을 끄더라도, 해당 콘텐츠가 지원하는 일반 영상 또는 사진으로 별도 확인되면 그에 맞는 켜 둔 규칙은 적용됩니다. 광고 표시만 있는 불명확한 캐러셀이나 웹 화면을 일반 영상으로 간주하지 않습니다. Instagram의 기존 광고 옵션 OFF 동작은 유지됩니다.

**광고만 넘기기 예:** 해당 앱 선택 ON, 반복 `0`, 광고 ON, 사진 OFF, 긴 영상 OFF, 전체 실행 ON. YouTube까지 함께 선택했다면 라이브 옵션도 OFF로 두세요. 시간제는 반복 0에서 중지됩니다.

### 5. 진행 정보 없는 영상 · 시간제로 넘김

**시간제 넘김**은 기본 OFF이며 최초 시간은 **3초**, 범위는 **2~60초의 정수**입니다. 숫자를 적용하고 옵션을 켭니다.

예: `3초`는 최초 판별 **2초를 포함한 총 3초**입니다. `2초 + 3초`가 아닙니다. 반복 횟수와 곱하지 않으며, 영상의 실제 길이나 완주 횟수를 알아낸 것도 아닙니다.

실제 진행 정보가 있으면 일반 반복 규칙을 사용합니다. 진행 정보가 없더라도 안전한 영상으로 확인되지 않으면 시간제로 넘기지 않습니다. 사진·라이브·웹 광고는 이 시간제의 대체 대상이 아닙니다. 반복 0, 전체 실행 OFF, 앱 선택 OFF에서는 중지합니다. 감지된 정지·댓글·다른 화면·영상 원천 변경 이후에는 대기시간을 다시 계산합니다.

### 6. 사진 콘텐츠

**사진 자동 넘김**은 기본 OFF이며 반복 0에서도 사용할 수 있습니다. 다음 두 모드 중 하나를 선택합니다.

- **게시물 통째로 넘기기:** 같은 사진 게시물을 **통째 넘김 시간(초)**만큼 관측한 뒤 다음 게시물로 이동합니다. 각 사진을 모두 볼 필요는 없습니다.
- **한 장씩 보고 다음 게시물로:** **사진 한 장당 보기 시간 (초)**만큼 기다린 뒤 다음 사진으로 이동합니다. 마지막 사진도 같은 시간만큼 본 뒤 다음 게시물로 이동합니다.

두 시간은 각각 **0~10초**, 최초값은 **3초 / 3초**이며 따로 저장합니다. 0초는 안전한 화면 확인 후 즉시 요청한다는 뜻입니다.

한 장씩 보기에서 **장 번호를 읽을 수 없으면 통째로 넘기기**를 켜면, 확인된 사진 페이지에만 통째 넘김 시간을 적용합니다. 이 옵션은 기본 OFF이며, 끄면 번호를 읽을 수 없는 사진에서는 대기합니다. `0/5`, 현재 번호가 총수보다 큰 값 등 모순된 번호는 단순 번호 누락으로 취급하지 않습니다.

예: 3장 게시물, 한 장당 3초 → 첫째·둘째·마지막 사진에 각각 대기 후 다음 게시물. 이동 확인에 걸리는 시간은 별도이므로 정확히 9초 만에 끝난다는 보장은 없습니다. 사진에서 동영상이 섞이거나 댓글·메뉴·웹 화면이 열리면 안전하게 대기하거나 정지합니다.

### 7. 긴 영상과 YouTube 라이브

**긴 영상 건너뛰기**는 기본 OFF, 기준 최초값 **60초**, 범위 **1~3600초**입니다. **건너뛸 영상의 최소 총길이 (초)**는 시청 대기시간이 아닙니다. 예를 들어 기준 60초이면 총길이 60초 이상인 일반 영상을 안정적으로 식별하고 실제 진행이 확인된 뒤 넘깁니다.

길이를 모르면 이 규칙으로 넘기지 않습니다. 특히 TikTok의 `0~10000` 비율 진행값은 초가 아니므로 총길이로 바꾸지 않습니다. 진행 정보가 전혀 없는 적격 영상에는 별도로 켠 시간제만 적용할 수 있습니다. 긴 영상 규칙은 반복 0에서도 동작합니다.

YouTube의 **라이브 미리보기 넘기기**는 별도 기본 OFF이며 대기시간 **0~60초**, 최초값 **0초**입니다. 0초는 안전한 전용 미리보기 인식 후 즉시 넘기기입니다. TikTok LIVE 자동 넘김과 TikTok 화면 분석은 제공하지 않습니다.

### 8. 공통 설정, 복구와 저장

- **여러 앱 동시 적용:** 기본 OFF는 활성 창만 처리합니다. ON은 선택된 안전한 가시 창을 최대 세 앱까지 각각 처리합니다. OS 입력은 한 번에 하나씩 실행하며 숨긴 창·PiP·멈춘 영상의 재생이나 초점을 강제로 바꾸지 않습니다.
- **화면 위에 숫자 표시:** 플로팅은 선택 사항입니다. 숨겨도 실행은 계속됩니다. `YT`, `IG`, `TT` 표시로 앱을 구분하며 드래그 위치는 해당 창 안에 보존됩니다.
- **이 앱의 플로팅 조작:** 횟수 순환은 `0→1→…→기준 횟수→0`, 반복 켜기·끄기는 `0↔기준 횟수`입니다. 광고 등 별도 규칙의 전체 OFF 버튼이 아닙니다.
- 플로팅의 `X`는 **해당 앱만 일시정지**합니다. 상세 화면의 **이 앱 다시 시작** 또는 전체 실행 OFF→ON으로 재개합니다.
- 미확인 이동으로 안전정지한 경우 먼저 화면을 확인하세요. 숫자나 다른 옵션만 바꾸어 재시도를 반복하지 않습니다. **이 앱 다시 시작** 또는 전체 실행 OFF→ON으로 명시적으로 재개하세요.
- 이전에 저장한 유효한 설정은 유지합니다. TikTok 광고·사진·시간제 설정은 Instagram과 별도 저장하며, 아직 저장하지 않은 TikTok 추가 옵션은 OFF로 시작합니다. 이 변경으로 새 권한을 자동 승인하거나 시청 정보를 외부로 전송하지 않습니다.

## English guide

### 1. Open and enable an app

1. Under **App settings** on the home screen, open **YouTube settings**, **Instagram settings** or **TikTok settings**.
2. Turn on **Enable automation for this app**. The app must be installed.
3. Set that app's play count and optional rules. Other apps retain their own settings.
4. Complete the required accessibility setup, then turn on the bottom **All auto-advance** switch (**Run all** with large text). It starts or stops all selected apps.
5. Use **‹ Home** or Android Back to return home. Navigation itself does not start execution or apply a numeric draft.

Type numbers and press keyboard **Done** or the matching **Apply** button. Arrows and `+/−` save their changes immediately; switches and mode choices also apply immediately. Enabling a rule or starting execution validates required pending numbers. Correct invalid input on the indicated app screen. **Selected · Execution off** means selection is saved but the master switch is off.

### 2. Counts and independent rules

**Ordinary videos · Play count** accepts **0–99**. `2` means two observed complete plays in total, not an initial play plus two additional repeats. Starting partway through a video may wait for its next beginning.

**0 stops ordinary counting, the clockless timer and visual assistance.** It does not disable opted-in ads, photo posts, known long-video filtering or YouTube live-preview skipping. Master execution OFF stops every rule.

### 3. Ads · Auto-skip

Instagram and TikTok have separate **Skip ads** settings. **Wait before skipping (seconds)** accepts **0.0–9.9**, with **0.1-second** buttons and initial **0.0**. Enter `1.5` and press **Apply ad wait** to save a 1.5-second wait.

Zero means immediate after safety checks, not disabled. A positive delay requires the same safe ad to remain observable. Scheduling, observation and serialized input can make actual navigation later; this is not a precise real-time guarantee. Enabled ad skipping takes priority over ordinary, timer, photo and long-video rules on a recognized ad.

With TikTok ad skipping OFF, independently recognized ordinary-video or photo content can still follow its applicable enabled rule. Unknown ad carousels and web views do not become ordinary timer candidates. Instagram retains its existing ad-option-OFF behavior.

For **ads only**, select the app, set count `0`, enable ads, disable photos and long-video filtering, then enable master execution. Disable YouTube live skipping too if YouTube is selected. Count zero already disables the clockless timer.

### 4. No progress information · Timer

**Use timer** is initially OFF, with a default of **3 seconds** and an integer range of **2–60 seconds**. The initial **2-second qualification is included**: 3 seconds means 3 seconds total, not 5. The time is not multiplied by the play count and is not an estimate of video length or completed plays.

Readable progress uses the normal counter. A timer requires a positively recognized safe ordinary video without usable progress, not simply a displayed count of zero. Photos, live content, unknown structures and web ads do not inherit this timer. Count zero, master OFF or app deselection stops it. Detected pauses, comments, other screens and source changes restart qualification.

### 5. Photo posts

Enable **Auto-advance photos** independently of play count. Choose one mode:

- **Skip the whole post:** wait for **Whole-post delay (s)**, then move to the next post without requiring every slide to be viewed.
- **View each photo, then the next post:** wait for **Time per photo (s)** on each slide, including the last, before advancing.

The two times are saved separately, each **0–10 seconds**, initially **3 / 3 seconds**. Zero requests advancement after safe settlement; it is not OFF.

In each-photo mode, **Skip the whole post if the slide number is unreadable** optionally uses the whole-post delay on a recognized photo page. It is initially OFF, so unreadable numbering waits. Contradictory numbers cannot use this fallback. Three photos at 3 seconds each still need transition-confirmation time; the total is not guaranteed to be exactly 9 seconds. Mixed or unsafe content waits or stops safely.

### 6. Skip long videos and host limits

**Skip long videos** is initially OFF, with an initial threshold of **60 seconds** and a range of **1–3600 seconds**. **Minimum total duration to skip (seconds)** describes video length, not viewing time. A 60-second threshold includes videos exactly 60 seconds long, after stable identification and real forward playback are observed.

Unknown duration is not guessed. TikTok's normalized `0–10000` progress is not seconds. A separate enabled timer may handle an eligible video with no usable progress, but the long-video filter cannot infer its duration. The long filter remains independent of count zero.

YouTube alone offers **Skip live previews**, initially OFF, delay **0–60 seconds**, initially **0**. Zero means immediate after dedicated-preview safety checks. Instagram alone has optional visual assistance on Android 14+. **TikTok LIVE skipping and TikTok visual assistance are unsupported.** TikTok support targets the observed recommendation-feed structures in `com.ss.android.ugc.trill`, not every package, language, ad format or app version.

### 7. Shared controls and recovery

**Process multiple apps** is initially OFF for active-window-only processing. When ON, up to three selected, safely visible hosts have separate counters and settings. Input is serialized; hidden windows, PiP and paused playback are not forced into focus or playback.

**Show floating count** is optional. Hiding it does not stop automation. `YT`, `IG` and `TT` identify each floating control. Dragging saves a position within that host window. **This app's floating control** either cycles `0→1→…→configured count→0` or toggles `0↔configured plays`. It does not turn off independent special-content rules.

`X` pauses only that app. Use **Resume this app** or master OFF→ON to resume. After an unconfirmed transition, inspect the screen and explicitly resume; changing ordinary settings does not repeatedly retry the failed request.

Existing valid settings are preserved. TikTok's ad, photo and timer values are separate from Instagram; unset TikTok special options start OFF. No new permission is silently granted and viewing information is not uploaded. See the [user guide](USER_GUIDE.md) and [compatibility guide](COMPATIBILITY.md) for setup and platform limits.
