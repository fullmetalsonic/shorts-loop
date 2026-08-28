# Android 버전별 기능 / Android compatibility

## 0.2.9/code31 · 언어와 OS 지원 / Language and OS support

설치 하한Android8/API26, API29 타일 상태줄·API33 타일 추가 요청·API34 화면 분석 제한은 그대로다.0.2.9는 앱·플로팅·빠른 설정 실행 상태·도움말·업데이트/오류를 한국어·영어로 표시한다. 새 권한·호스트 앱 개조·언어별 감지 우회를 추가하지 않으며 기존 설정·서명·패키지를 유지한다. 최신 APK의 실폰 두 모드·0/3/10초·댓글창 보호는 확인했다. 번호 없는 사진·사진 직후 광고·혼합 사례는 미확보이며 공개 파일 검증은 완료했다. [조건별 검증](VERIFICATION.md),[언어 안내](LOCALIZATION.md). 아래 과거 결과는 새 시험으로 합산하지 않는다.

The minimum remains Android8/API26. Tile subtitles require API29, tile-add requests API33, and visual assistance API34. Version0.2.9 localizes app-owned UI, floating controls, runtime tile state, help and update/errors into Korean and English. It adds no permission, host modification or language-based detection bypass; settings, signer and package remain. The latest APK has physical evidence for both photo modes,0/3/10s and comments protection. Unreadable-index,photo-to-ad and mixed cases remain unobserved;public-file verification is complete. Earlier results below are historical.

앱 표시 기준은 **시스템 첫 언어가한국어이면한국어,그밖에는영어**다. 영어→한국어 또는 일본어→한국어 목록에서도 영어를 사용한다. Android 자체가 표시하는 런처·접근성 서비스 이름/설명 및 권한·설치 창은OS fallback을 따를 수 있으므로 일부 표기가 다를 수 있다. 시스템 언어 변경 중에도 화면 이탈·진행 소실·서비스 재연결의 안전 동작을 유지하며 필요하면 전체 실행을OFF→ON한다. 언어 변경만으로 기존 옵션을 초기화하지 않는다.

The **first system language selects Korean if Korean, otherwise English**, including English→Korean and Japanese→Korean. Android-owned launcher/accessibility labels and permission/installer screens may use OS fallback. Language changes retain screen-change, lost-progress and service-reconnection safeguards; toggle main execution OFF→ON if needed. Saved options are not reset solely by a language change.

**표시 번역과 호스트 인식의 호환성은 별개다.** 기존 판독은 한국어·영어 텍스트·지원되는 시계 표기·접근성 구조 중심이며, 모든 언어·국가·YouTube/Instagram 버전의 카운트·광고 인식을 보장하지 않는다. OS 지원·앱 설치·영어 UI 표시만으로 자동 넘김이 검증되지는 않는다. 읽기 불가능한 화면은 기존 대기·정지 계약을 따른다.

**UI translation is separate from host-detection compatibility.** Readers retain existing Korean/English text, supported clocks and accessibility structures; counts and ads are not guaranteed in every language, region or host version. OS support, installation and English UI alone do not prove auto-advance. Unreadable screens retain existing waits/stops.

## 과거 0.2.7/code29 · 호환 범위 유지 / Historical unchanged compatibility

정식0.2.7의 설치 하한/API별 기능은0.2.6과 같다. 플로팅 표시 수정의 동일APK API26/33/34 계측5568/5568/5567항목과 실제Android17 표시/조작 검증을 완료했다. 이 수치는 호스트별 새로운 연속시험이 아니다. [0.2.7 배포·검증](releases/v0.2.7.md).

EN: Stable0.2.7 retains the0.2.6 minimum OS and per-API capabilities. Exact-APK native checks passed5568/5568/5567 assertions on API26/33/34,with physical Android17 display/interaction checks. These are not new host endurance runs.

## 이전 0.2.6/code28 · Public 시험판 검증 완료 / Previous published pre-release

**0.2.6/code28 공개 시험판(pre-release)을 게시하고 공개 파일 검증까지 완료했다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,로컬lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 is published as a public pre-release,and public artifact verification is complete.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;local lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증 후 기존 Public 저장소에v0.2.6/code28 pre-release를 게시했으며 CI·공개 다운로드 동일성도 확인했다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. After the YouTube retest and impact-scope checks passed,v0.2.6/code28 was published as a pre-release in the existing Public repository. CI and public-download parity were verified.

## 과거 code26 지원·검증 범위 / Historical support and checks

**과거0.2.6/code26은 실폰 후속 실패로 게시 보류된 미배포 후보였다.** 빌드·454 JUnit(실패0)·정적 가드 PASS, lint0오류/기존3경고. 12:33 동일 APK의 Android API26/33/34 계측209/209/208개 PASS,12:36 휴대폰 설치·전체 기존 설정 직접 비교 보존·접근성 연결·설치 APK 해시 일치 PASS. 12:38:20 YouTube 공식 시험은 요청10/확인10(긴 영상9+라이브1)과 전후 화면의 서로 다른 영상 확인으로 PASS했다. 12:39:22 별도 일반1/1 전환1회도 화면 쌍으로 확인했다. 그러나 후속 연속 실행 중 요청20/확인19에서 같은59초 길이·pager index 부재로 안전정지했다. 해당 실패 요청에는 전후 화면 쌍이 없어 실제 다음 영상 이동 여부는 미확정이다. Instagram은12:43:56~12:45:31.831(96.0초) 별도 시험에서 요청10/확인10(일반3·긴 영상4·시간제10초2·광고1),수동0·실패/복구0으로 PASS했다. **두 앱의 지정10회 PASS가 유튜브 후속 실패를 덮지 않으며 제품 완료·배포 준비 완료가 아니다.**

**Historical0.2.6/code26 remained unpublished after a subsequent device failure blocked its release.** Build,454 JUnit tests with zero failures,static guards and209/209/208 exact-APK API26/33/34 checks passed; lint has0 errors and3 existing warnings. Installation preserved all compared preferences and accessibility binding,and matched the APK hash. The12:38:20 YouTube run passed10 requests/10 confirmed distinct transitions:9 long-video and1 live. A separate12:39:22 ordinary1/1 transition also passed screenshot-pair review. Further continuation then safety-stopped at20 requests/19 confirmations when both durations were59 seconds and pager indices were unavailable. No pre/post screenshot pair exists for that failed request,so actual movement is unproven. A separate96.0-second Instagram run at12:43:56–12:45:31.831 passed10 requests/10 confirmations:3 ordinary,4 long-video,2 ten-second clockless and1 ad,with no manual swipes,failures or recoveries. The two designated ten-transition PASS results do not override the later YouTube failure or establish release readiness.

Android8(API26) 설치 하한과 버전별 권한·옵션 계약은 유지한다. 긴 영상 옵션은 기본OFF·기준60초·1~3600초이며,선택·설치된 YouTube/Instagram 중 전면 일반 영상의 유효한 총길이≥기준일 때만 적용한다. 앱 자체의 OS 지원과 ShortsLoop의 API 가용성은 별개다. 같은 길이·pager index 부재 등으로 전환을 확실히 구분하지 못하면 지원 OS에서도 안전정지할 수 있다. **계측 PASS는 모든 실제 소셜 앱 화면·기기에서의 자동 동작 보증이 아니다.** [최신 검증](VERIFICATION.md), [전환 확인 한계](DEBUG_LOG.md).

**이전 후보는 별도 기록이다.** code23은12:12 실제62→93초 영상 이동 후 요청1/확인0으로 실패했고, code24는12:17 같은 창·영역·인식·안전 조건에서도 공통 텍스트 identity가 같음을 재현했다. code25도12:21~12:22 실제93→57초 이동 후 요청/현재 index가 모두−1이고 공통 identity가 같아 실패했다. code23/24/25는 실폰FAIL·미배포이며 PC·계측PASS가 이를 덮지 않는다. code22의 YouTube2회는 기능 추가로 중단한 과거 관측이며 수동180초 영상 이동1회는 제외했다. 어느 후보의 관측도 code26의10회에 합산하지 않는다.

## 과거 code23 호환성 체크포인트 · 실폰FAIL/미배포 / Historical compatibility checkpoint

과거대상: **0.2.6(code23),실폰FAIL·미배포**. API26이상동작과화면분석API34제한의분리를유지했고빌드418시험·163/163/162계측·12:10설치/설정/연결/해시는PASS했습니다. 그러나12:12실제긴영상전환확인은FAIL이었으며0.2.6전체PASS나최신code28검증으로사용하지않습니다. 최종 [검증·게시](VERIFICATION.md)를 확인하세요.

Historical **code23** retained API26/no-new-permission compatibility and passed build,418 tests,163/163/162 emulator checks and installation parity. It then failed actual long-video confirmation at12:12 and remained unpublished. These passes do not verify code25.

과거code22의383제품시험·109/109/108계측은 긴 영상 기능 추가 전의 별도 증거입니다. code22의YouTube2회 자동전환과180초영상1개 수동제외 후 세션중단을code23의검증이나10연속PASS로합산하지않습니다.<br>
Historical code22's383 tests and109/109/108 checks preceded long-video support. Its two automatic YouTube transitions and one excluded manual180-second skip ended with feature integration; they are not code23 evidence or a ten-transition PASS.

## 지원 원칙 / Policy

설치 하한은 기존 **Android 8.0(API26)**을 유지합니다. Android 7까지 확장하지 않습니다. 각 OS에서 가능한 기능만 제공하며, 특정 실험 기능이 없어도 기본 기능을 차단하지 않습니다. **YouTube 또는 Instagram 공식 앱이 해당 기기에서 설치·실행되고, 지원되는 화면 정보를 제공해야 합니다.** 앱 설치 성공만으로 자동 넘김을 보장하지 않습니다.

The existing minimum remains **Android 8.0 (API26)**, without an Android 7 backport. Unavailable experimental features do not block basic features. **The official host app must install, run, and expose a supported screen on the device.** Installation alone does not guarantee auto-advance.

| 기기 Android / OS | 기본 횟수·플로팅 / Counting, floating | Instagram 시간제·광고 / Timer, ads | 빠른 설정 / Quick Settings | 화면 분석 실험 / Visual trial |
|---|---|---|---|---|
| 8~9 (API26~28) | 조건 충족 시 / Conditional | 조건 충족 시 / Conditional | 직접 추가, 라벨에 상태 / Manual; state in label | 사용 불가 / Unavailable |
| 10~12L (API29~32) | 동일 / Same | 동일 / Same | 직접 추가, 별도 상태줄 / Manual; subtitle | 사용 불가 / Unavailable |
| 13 (API33) | 동일 / Same | 동일 / Same | 앱에서 추가 요청 / In-app add request | 사용 불가 / Unavailable |
| 14 이상 (API34+) | 동일 / Same | 동일 / Same | 앱에서 추가 요청 / In-app add request | 별도 동의 후 / Separate opt-in |

‘조건 충족’은 대상 앱 설치·선택, 접근성 연결, 지원되는 쇼츠/릴스 화면을 의미합니다. 플로팅을 켜면 다른 앱 위 표시 권한도 필요합니다. 광고는 명시적인 광고 표시를 인식할 때만, 시간제는 진행 정보 없는 안전한 Instagram 영상에서만 동작합니다. 삼성 팝업·PiP와 모든 앱 버전의 인식을 보장하지 않습니다.

Conditions include host installation/selection, accessibility permission, and a supported Shorts/Reels screen. Floating controls additionally require overlay permission. Ads require an explicit recognized ad label; timed fallback applies only to eligible clockless Instagram videos. Pop-up/PiP and all host-app versions are not guaranteed.

긴 영상은 두 선택 앱의 확인 가능한 일반 영상에 적용하며 기본OFF·총길이기준60초·1~3600초입니다. 총길이≥기준과같은안전페이지의실제진행을확인해야하며길이불명·정지·특수콘텐츠를추정하지않습니다. 반복0과독립,전체OFF는중지합니다. 어떤OS에서도이조건을생략하지않으며4.5초전환확인실패는안전정지입니다.<br>
Long-video skipping supports known ordinary durations in either selected host(defaultOFF,threshold60 seconds,range1–3600). Duration≥threshold plus stable-page/real-progress evidence is required on every OS. Unknown,paused or special content is not inferred. Zero plays is independent; overallOFF stops it,and an unconfirmed transition hard-stops after4.5 seconds.

YouTube 라이브 미리보기도 기본 접근성 경로를 사용하며 화면 분석의 API34 제한과 별개입니다. **기본 OFF·대기 0~60초·기본 0초(인식 후 바로)**이며 YouTube 선택·옵션 ON·전체 실행 ON이면 일반 반복 0회에서도 동작합니다. 공식 YouTube가 해당 OS에서 실행되고 쇼츠 내 전용 라이브 구조를 제공해야 합니다. 일반 라이브 시청 화면·텍스트만으로 판정하지 않으며 새 권한·OCR·화면/오디오 분석을 추가하지 않습니다. 부분 표시·화면 차단·구조 미지원·페이지 구분 실패에서는 대기 또는 안전 정지합니다. [라이브 계약](LIVE_SKIP.md).

YouTube live previews use basic accessibility independently of API34 visual assist. **OFF by default, wait 0–60 seconds, initially 0 (immediate after recognition)**; YouTube selection, the live option and main execution enable it even at zero plays. The official host must run on that OS and expose the supported Shorts live structure. Ordinary live-watch screens or text alone are insufficient. No new permission, OCR, image or audio analysis is added. Partial, blocked, unsupported or indistinguishable pages wait or safety-stop.

## 화면에서 확인하기 / On-device instructions

1. **사용할 앱 / Apps to use**에서 설치된 앱을 선택합니다. 미설치 앱은 ‘미설치’로 표시됩니다.<br>Select an installed host under **Apps to use**. Missing apps are labeled and disabled.
2. **사용 준비 / Setup → 이 기기의 기능**에서 현재 Android 버전과 가능한 기능을 확인합니다.<br>See the current OS and supported features under device capabilities in **Setup**.
3. Android 8~12L에서는 **빠른 설정 직접 추가 방법**을 눌러 안내를 확인합니다. 상단 빠른 설정을 펼치고 편집에서 ‘쇼츠 넘김’을 활성 영역으로 옮깁니다. 제조사마다 편집 버튼 모양이 다릅니다. 13 이상에서는 앱 버튼으로 추가 요청 후 시스템 확인을 직접 선택합니다.<br>On Android 8–12L, open manual tile help, expand Quick Settings, edit tiles and add ShortsLoop. OEM controls vary. On 13+, use the in-app request and confirm in the system dialog.
4. Android 14 미만에서는 **화면 분석 보조**가 꺼진 비활성 상태이며 이유가 보입니다. 이전 기기에서 켰던 선택은 지우지 않지만 여기서는 실행하지 않습니다. Instagram의 시간제·광고는 별도로 사용할 수 있습니다.<br>Below Android 14, visual assist is disabled with a reason. A previous saved choice is retained but cannot run here. Instagram timer and ads remain separate options.
5. Instagram 미설치·미선택이면 관련 옵션이 비활성화됩니다. 저장값은 보존되며 앱 설치·재선택 후 다시 적용됩니다. 전체 실행을 임의로 켜거나 권한을 자동 허용하지 않습니다.<br>Missing/deselected Instagram disables its options without erasing preferences. They become applicable again after installation/selection. This never auto-starts execution or grants permission.

**0회는 일반반복·시간제·화면분석을중지**합니다. 긴영상은선택앱+긴영상ON+전체ON,광고는Instagram선택+광고ON+전체ON,라이브는YouTube선택+라이브ON+전체ON이면독립적으로동작합니다. 전체OFF는모두중지합니다. 시간제기본OFF/10초·5~60초는영상총길이기준과별개이며정상진행영상을10초로자르지않습니다.

**Zero plays stops repeat-based,timed and visual advancing**. Long-video,ad and live options remain independently available with the corresponding selected host,option and overall execution enabled. OverallOFF stops all. The timer defaults to OFF/10 seconds,range5–60;it is distinct from the total-duration threshold and does not truncate ordinary clock-based playback at ten seconds.

code17부터 접근성 미연결 시 **접근성 연결 확인 · 해결 방법 보기**와 **접근성 설정·다시 연결**을 표시합니다. Android 설정이 ON이어도 실제 연결이 끊길 수 있으므로 해당 서비스를 OFF→ON하고 연결 후 전체 실행을 직접 켭니다. 화면 분석 OFF에서도 기본 감지·다음 이동에 접근성이 필요합니다. 이 안내는 권한이나 실행 상태를 자동 변경하지 않습니다.

Since code17, the app shows accessibility help and a settings/reconnect button when disconnected. The Android setting can remain ON while the service is disconnected: toggle that service OFF→ON, then manually enable main execution after reconnection. Accessibility remains necessary for basic detection and advancing with visual assist OFF. These instructions do not automatically change permissions or execution.

## GitHub 업데이트와 설치 / GitHub updates and installation

인앱 업데이트는 앱의 기존 설치 하한인 API26부터 제공하며 화면 분석의 API34 제한과 별개입니다. ‘사용 준비’ 다음의 **업데이트 · 앱 정보**에서 설치 버전과 수동 **업데이트 확인**을 찾습니다. 기본 ON인 **앱 열 때 새 버전 확인**은 앱을 열거나 돌아올 때 마지막 시도 후 24시간 간격으로 조회하며 OFF로 바꿀 수 있습니다. 더 높고 기기에 맞는 버전이 확인된 경우만 상단 배너로 알립니다. 앱을 닫아 둔 동안의 주기 감시·시스템 알림은 없습니다.

The updater is available from the existing API26 app minimum, independently of the API34 visual trial. Find **Installed version 0.2.9** and the manual check under **Updates · App information**, after Setup. Opening/resuming the app can check automatically, at most once per 24 hours since the last attempt; this defaults ON and can be disabled. Only a known compatible newer version produces a top banner. There is no background polling or system notification.

**조회 → 직접 다운로드 → 검사 완료 후 직접 설치**를 구분합니다. `INTERNET`은 GitHub 업데이트 정보·APK 수신용이며 영상·계정·시청이력을 업로드하지 않습니다. `REQUEST_INSTALL_PACKAGES`는 설치 화면 연결용 선언입니다. 실제 ‘이 출처 허용’과 시스템 최종 설치 확인은 사용자가 직접 합니다. 기기 정책으로 설치가 제한될 수 있으며 우회하지 않습니다. 저장소 전체 접근·알림 권한·무인 자동 설치는 없습니다.

**Checking, downloading and installing are separate actions.** `INTERNET` is restricted to GitHub update information/APKs, not uploading videos, accounts or viewing history. `REQUEST_INSTALL_PACKAGES` enables the installer hand-off; the user must grant source permission and confirm installation in Android. Device policy may block installation and is not bypassed. There is no broad storage permission, notification permission or unattended installation.

설치 버튼을 누르면 전체 실행만 OFF로 바꾸고 다른 설정은 보존합니다. 설치 허용 화면에서 돌아온 뒤 설치를 다시 눌러야 하며 취소 후 실행도 자동 재시작하지 않습니다. **0.2.4에는 업데이트 기능이 없어 0.2.5 최초 설치는 같은 서명 APK를 직접 내려받아 덮어쓰기 설치해야 합니다.** [단계별 사용법](USER_GUIDE.md).

Pressing Install turns main execution OFF while preserving other settings. After granting source permission, return and press Install again. Cancelling never auto-restarts execution. **0.2.4 has no updater: initially download and install the same-signed 0.2.5 APK manually over it.** See the detailed guide for steps.

## 공식 근거와 한계 / Sources and limits

- [YouTube 공식 설치 안내](https://support.google.com/youtube/answer/3227660): 2026-08-28 확인 기준 Android 9 이상. 앱 자체가 Android 8에 설치되어도 최신 YouTube 지원을 의미하지 않습니다.<br>YouTube's official download requirement checked on 2026-08-28 is Android 9+. Our API26 floor is not a promise that current YouTube runs on Android 8.
- [Instagram 공식 배포 페이지](https://play.google.com/store/apps/details?id=com.instagram.android): 웹 공개 내용에서 최소 OS 숫자를 확인하지 못했습니다. 임의의 최소 버전을 코드에 고정하지 않으며 해당 기기 Play 스토어의 호환 여부가 우선합니다.<br>The public web listing did not expose a verified minimum OS. We do not invent or hardcode one; check availability on the device's Play Store.
- [Android 접근성 API](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService): 제스처는 API24부터, 창 단위 화면 캡처는 API34부터입니다. 구형 OS용 전체 화면 캡처 우회는 추가하지 않습니다. 위 인터넷·설치 연결 권한은 업데이트용이지 화면 분석 우회용이 아닙니다.<br>Gesture dispatch starts at API24; window screenshots require API34. No whole-screen capture workaround is added. Internet/install permissions serve updates, not capture workarounds.
- [빠른 설정 추가 요청](https://developer.android.com/develop/ui/views/quicksettings-tiles), [타일 상태줄](https://developer.android.com/reference/android/service/quicksettings/Tile): 앱 내 추가 요청은 API33, 별도 상태줄은 API29부터입니다.<br>In-app tile requests start at API33 and tile subtitles at API29.

## 구현·검증 / Implementation and verification

버전 정책은 `FeatureSupportPolicy`, 설명은 `CompatibilityPanel`, 화면 분석은 구형 OS에서 안전한 공통 인터페이스와 `Api34VisualAssistController`로 분리합니다. API34 전용 캡처 타입은 구형 실행 경로에서 사용하지 않습니다. 접근성 XML도 기본/`xml-v34`로 분리하여 API34 이상에서만 캡처 capability를 선언합니다. 호환성 분리는 기존 횟수·시간제·광고 알고리즘을 바꾸지 않으며 code21의 라이브 감지는 별도 모듈로 추가합니다.

OS policy, UI explanations and API34 capture implementation are separated. Old OS paths do not use screenshot-specific types. Versioned accessibility XML declares capture capability only on API34+. Compatibility isolation leaves existing counting, timer and ad algorithms unchanged; code21 adds a separate live-preview detector.

빌드·단위시험은 `scripts/verify.ps1`. 에뮬레이터 전용 테스트 APK는 `:app:assembleDebugAndroidTest`로 만듭니다. 실행은 아래처럼 **명시적으로 선택한 폐기 가능한 에뮬레이터**에만 수행합니다. 이 시험은 설정값을 임시 변경 후 복원하며 접근성·플로팅 권한을 허용하지 않습니다.

Build/unit checks use `scripts/verify.ps1`; compile the emulator-only test APK with `:app:assembleDebugAndroidTest`. Run only on an explicitly selected disposable emulator. Tests temporarily change and restore preferences without granting accessibility/overlay permissions.

```powershell
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -w com.fullmetalsonic.shortsloop.test/com.fullmetalsonic.shortsloop.CompatibilityInstrumentation
```

`INSTRUMENTATION_RESULT: result=PASS`를 확인해야 합니다. 프로세스 종료 코드만으로 성공을 판정하지 않습니다. 실제 실행 결과는 [검증 기록](VERIFICATION.md), 변경·남은 위험은 [인수인계](../HANDOVER.md)와 [디버그 대장](DEBUG_LOG.md)을 따릅니다. 에뮬레이터 UI 시험은 YouTube/Instagram 실제 자동 넘김 E2E가 아닙니다.

**이전 code14 호환성 후보의 2026-08-28 결과:** 빌드·제품241시험·lint0오류3경고·독립리뷰 PASS. Android8/13/14 에뮬레이터에서 각각17/17/16검사 PASS, 설치APK해시동일. Windows에서는 `scripts/verify-compat-emulator.ps1 -Device emulator-5554`로 설치·계측·명시적PASS검사·private증거저장을 재현할 수 있습니다. 이 단계에서는 실제 휴대폰 미연결로 시험하지 않았습니다. 이 과거 결과는 code16 업데이트/설치 경로의 PASS가 아니며 통합판의 최종 결과는 검증 문서에 별도 기록합니다.

**Historical code14 compatibility-candidate results, 2026-08-28:** build, 241 product tests, lint (0 errors/3 warnings) and independent review passed. Android8/13/14 emulator checks passed (17/17/16), with matching installed APK hashes. The Windows helper installs and verifies the explicit result on a chosen emulator. No physical phone was connected at that stage. These results do not verify the code16 updater/installer; integrated-build results are recorded separately in VERIFICATION.

Require an explicit `result=PASS`, not merely a successful process exit. See verification, handover and debug records for actual outcomes. Emulator UI checks are not social-app auto-advance E2E.
