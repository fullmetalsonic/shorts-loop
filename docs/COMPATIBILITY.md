# Android 버전별 기능 / Android compatibility

대상: **0.2.5(code21), 호환성·인앱 업데이트·메뉴 배치·YouTube 라이브 미리보기를 통합한 시험판**. 현재 검증·공개 준비 중입니다. code14 호환성 후보 및 이전 공개판0.2.4 결과는 별도 이력입니다. 최종 검증·게시 결과는 [VERIFICATION](VERIFICATION.md) 참조.

Scope: **0.2.5 (code21), integrating compatibility, in-app updates, reordered settings and YouTube live-preview skipping**. Verification and publication are in progress. The code14 compatibility candidate and public 0.2.4 results are historical, not verification of this build. See VERIFICATION for final test and publication results.

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

YouTube 라이브 미리보기도 기본 접근성 경로를 사용하며 화면 분석의 API34 제한과 별개입니다. **기본 OFF·대기 0~60초·기본 0초(인식 후 바로)**이며 YouTube 선택·옵션 ON·전체 실행 ON이면 일반 반복 0회에서도 동작합니다. 공식 YouTube가 해당 OS에서 실행되고 쇼츠 내 전용 라이브 구조를 제공해야 합니다. 일반 라이브 시청 화면·텍스트만으로 판정하지 않으며 새 권한·OCR·화면/오디오 분석을 추가하지 않습니다. 부분 표시·화면 차단·구조 미지원·페이지 구분 실패에서는 대기 또는 안전 정지합니다. [라이브 계약](LIVE_SKIP.md).

YouTube live previews use basic accessibility independently of API34 visual assist. **OFF by default, wait 0–60 seconds, initially 0 (immediate after recognition)**; YouTube selection, the live option and main execution enable it even at zero plays. The official host must run on that OS and expose the supported Shorts live structure. Ordinary live-watch screens or text alone are insufficient. No new permission, OCR, image or audio analysis is added. Partial, blocked, unsupported or indistinguishable pages wait or safety-stop.

## 화면에서 확인하기 / On-device instructions

1. **사용할 앱**에서 설치된 앱을 선택합니다. 미설치 앱은 ‘미설치’로 표시됩니다.<br>Select an installed host app. Missing apps are labeled and disabled.
2. **사용 준비 → 이 기기의 기능**에서 현재 Android 버전과 가능한 기능을 확인합니다.<br>See the current OS and supported features under device capabilities in setup.
3. Android 8~12L에서는 **빠른 설정 직접 추가 방법**을 눌러 안내를 확인합니다. 상단 빠른 설정을 펼치고 편집에서 ‘쇼츠 넘김’을 활성 영역으로 옮깁니다. 제조사마다 편집 버튼 모양이 다릅니다. 13 이상에서는 앱 버튼으로 추가 요청 후 시스템 확인을 직접 선택합니다.<br>On Android 8–12L, open manual tile help, expand Quick Settings, edit tiles and add ShortsLoop. OEM controls vary. On 13+, use the in-app request and confirm in the system dialog.
4. Android 14 미만에서는 **화면 분석 보조**가 꺼진 비활성 상태이며 이유가 보입니다. 이전 기기에서 켰던 선택은 지우지 않지만 여기서는 실행하지 않습니다. Instagram의 시간제·광고는 별도로 사용할 수 있습니다.<br>Below Android 14, visual assist is disabled with a reason. A previous saved choice is retained but cannot run here. Instagram timer and ads remain separate options.
5. Instagram 미설치·미선택이면 관련 옵션이 비활성화됩니다. 저장값은 보존되며 앱 설치·재선택 후 다시 적용됩니다. 전체 실행을 임의로 켜거나 권한을 자동 허용하지 않습니다.<br>Missing/deselected Instagram disables its options without erasing preferences. They become applicable again after installation/selection. This never auto-starts execution or grants permission.

**0회는 일반·시간제·화면 분석을 중지**합니다. 광고는 Instagram 선택+광고 ON+전체 실행 ON, 라이브는 YouTube 선택+라이브 ON+전체 실행 ON이면 독립적으로 동작합니다. 전체 실행 OFF는 모두 중지합니다. 시간제 기본 OFF/10초, 범위5~60초이며 정상 진행 정보를 10초로 자르지 않습니다.

**Zero plays stops normal, timed and visual advancing**; ads remain independent when Instagram, ads and main execution are enabled, as do live previews when YouTube, the live option and main execution are enabled. Main OFF stops all. Timer defaults to OFF/10 seconds, adjustable 5–60; usable playback clocks are not cut off at ten seconds.

code17부터 접근성 미연결 시 **접근성 연결 확인 · 해결 방법 보기**와 **접근성 설정·다시 연결**을 표시합니다. Android 설정이 ON이어도 실제 연결이 끊길 수 있으므로 해당 서비스를 OFF→ON하고 연결 후 전체 실행을 직접 켭니다. 화면 분석 OFF에서도 기본 감지·다음 이동에 접근성이 필요합니다. 이 안내는 권한이나 실행 상태를 자동 변경하지 않습니다.

Since code17, the app shows accessibility help and a settings/reconnect button when disconnected. The Android setting can remain ON while the service is disconnected: toggle that service OFF→ON, then manually enable main execution after reconnection. Accessibility remains necessary for basic detection and advancing with visual assist OFF. These instructions do not automatically change permissions or execution.

## GitHub 업데이트와 설치 / GitHub updates and installation

인앱 업데이트는 앱의 기존 설치 하한인 API26부터 제공하며 화면 분석의 API34 제한과 별개입니다. ‘사용 준비’ 다음의 **업데이트 · 앱 정보**에서 설치 버전과 수동 **업데이트 확인**을 찾습니다. 기본 ON인 **앱 열 때 새 버전 확인**은 앱을 열거나 돌아올 때 마지막 시도 후 24시간 간격으로 조회하며 OFF로 바꿀 수 있습니다. 더 높고 기기에 맞는 버전이 확인된 경우만 상단 배너로 알립니다. 앱을 닫아 둔 동안의 주기 감시·시스템 알림은 없습니다.

The updater is available from the existing API26 app minimum, independently of the API34 visual trial. Find the installed version and manual check under **업데이트 · 앱 정보**, after setup. Opening/resuming the app can check automatically, at most once per 24 hours since the last attempt; this defaults ON and can be disabled. Only a known compatible newer version produces a top banner. There is no background polling or system notification.

**조회 → 직접 다운로드 → 검사 완료 후 직접 설치**를 구분합니다. `INTERNET`은 GitHub 업데이트 정보·APK 수신용이며 영상·계정·시청이력을 업로드하지 않습니다. `REQUEST_INSTALL_PACKAGES`는 설치 화면 연결용 선언입니다. 실제 ‘이 출처 허용’과 시스템 최종 설치 확인은 사용자가 직접 합니다. 기기 정책으로 설치가 제한될 수 있으며 우회하지 않습니다. 저장소 전체 접근·알림 권한·무인 자동 설치는 없습니다.

**Checking, downloading and installing are separate actions.** `INTERNET` is restricted to GitHub update information/APKs, not uploading videos, accounts or viewing history. `REQUEST_INSTALL_PACKAGES` enables the installer hand-off; the user must grant source permission and confirm installation in Android. Device policy may block installation and is not bypassed. There is no broad storage permission, notification permission or unattended installation.

설치 버튼을 누르면 전체 실행만 OFF로 바꾸고 다른 설정은 보존합니다. 설치 허용 화면에서 돌아온 뒤 설치를 다시 눌러야 하며 취소 후 실행도 자동 재시작하지 않습니다. **0.2.4에는 업데이트 기능이 없어 0.2.5 최초 설치는 같은 서명 APK를 직접 내려받아 덮어쓰기 설치해야 합니다.** 공개 전에는 예정 릴리스가 없을 수 있습니다. [단계별 사용법](USER_GUIDE.md).

Pressing Install turns main execution OFF while preserving other settings. After granting source permission, return and press Install again. Cancelling never auto-restarts execution. **0.2.4 has no updater: initially download and install the same-signed 0.2.5 APK manually over it.** The planned release may not yet be available. See the detailed guide for steps.

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
