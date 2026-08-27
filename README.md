# 쇼츠 자동 넘김 · ShortsLoop 0.2.2

일반 YouTube 쇼츠와 Instagram 릴스를 정한 횟수만큼 보고 다음 영상으로 넘기는 Android 앱입니다.<br>
An Android app that advances supported YouTube Shorts and Instagram Reels after a chosen number of plays.

**시험판 / Experimental prerelease:** YouTube 간헐 정지와 일부 반복 경계 인식 문제가 남아 있습니다. 0.2.2는 배터리 설정 안내를 추가한 버전이며, 이 문제들을 해결한 안정판이 아닙니다.<br>
Intermittent YouTube stops and missed loop boundaries remain. Version 0.2.2 adds battery setup guidance; it is not a stability fix.

[APK 다운로드 / Download APK](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.2) · [변경 내용 / Release notes](docs/releases/v0.2.2.md) · [검증 / Verification](docs/VERIFICATION.md)

## 주요 기능 / Features

- 총 재생 횟수 0~99: 숫자 입력과 ▲/▼. **0은 자동 넘김 안 함**.<br>
  Total plays from 0 to 99, with typing and step buttons. **0 disables auto-advance**.
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

## 설치와 첫 실행 / Install and get started

Android 8.0(API26) 이상이 설치 대상입니다. 실제 시험은 Galaxy Z Fold8 / Android17에서 했으며 모든 기기의 호환성을 보장하지 않습니다. 인앱 언어는 현재 한국어입니다.<br>
The minimum OS is Android 8.0/API26. Device testing was on Galaxy Z Fold8 / Android17, not all supported devices. The in-app interface is currently Korean.

1. 위 Release에서 `shorts-loop-v0.2.2-debug.apk`를 내려받습니다. 개발 서명·디버그 APK이며 Play Store 배포본이 아닙니다. 첨부한 SHA256과 파일 해시를 비교할 수 있습니다.<br>
   Download `shorts-loop-v0.2.2-debug.apk` from the release. This is a development-signed, debuggable APK, not a Play Store release. A SHA256 file is included.
2. 신뢰할 수 있는 출처인지 확인한 뒤 Android 설치 안내에 따릅니다. 출처를 알 수 없는 앱 설치 허용이 필요할 수 있습니다. 기존 YouTube·Instagram은 수정하지 않습니다.<br>
   Verify the source and follow Android's installation prompts, including permission to install from that source if needed. YouTube and Instagram are not modified.
3. 앱을 열고 **반복 횟수**에 `2` 입력 → **입력한 횟수 적용** 또는 키보드 완료. 사용할 앱을 하나 이상 선택합니다.<br>
   Open the app, enter `2` under “반복 횟수” (plays), and press Apply or keyboard Done. Select at least one target app.
4. **사용 준비 → 접근성 연결하기**에서 안내를 읽고 Android 설정의 이 앱 접근성 서비스를 직접 켭니다. 제한된 설정으로 차단되면 OS 안내를 확인하고, 출처·권한을 신뢰할 수 없으면 중단하세요.<br>
   Under “사용 준비” (setup), open “접근성 연결하기” and manually enable this app's accessibility service in Android Settings. If restricted settings block access, follow the OS guidance; stop if you do not trust the source or permission.
5. **화면 위에 숫자 표시**를 켠 경우에만 다른 앱 위 표시 권한도 직접 허용합니다. 플로팅을 끄면 이 권한 없이 사용할 수 있습니다.<br>
   Grant overlay permission only if “화면 위에 숫자 표시” (floating numbers) is enabled. It is not required when floating controls are disabled.
6. 삼성 기기에서는 아래 배터리 안내를 확인합니다. 화면 하단 **자동 넘김 실행**을 켜고 선택한 앱의 쇼츠/릴스를 전체 화면으로 재생합니다.<br>
   On Samsung devices, review the battery setup below. Turn on “자동 넘김 실행” at the bottom, then play full-screen Shorts/Reels in a selected app.

접근성은 화면을 읽고 조작할 수 있는 강한 권한입니다. ADB·무선 디버깅·루팅은 일반 사용에 필요하지 않습니다.<br>
Accessibility is a powerful permission that can read and interact with the screen. ADB, wireless debugging, and root are not required for normal use.

## 횟수와 플로팅 사용법 / Counts and floating controls

횟수는 추가 반복이 아닌 **처음 재생을 포함한 총 횟수**입니다. `2`이면 정상 추적 중 `1/2 → 2/2 → 다음 영상`입니다. 중간부터 켜면 다음 처음 재생부터 셉니다.<br>
The count means **total plays, including the first play**, not additional repeats. With `2`, normal tracking shows `1/2 → 2/2 → next video`. Starting mid-video waits for a fresh playback cycle.

- 입력 범위는 0~99. ▲/▼는 1씩 즉시 적용. 빈칸·음수·소수·100 이상은 적용하지 않으며 기존 확정값을 유지합니다. 오류를 고친 뒤 다시 적용하세요.<br>
  Use integers 0–99; ▲/▼ apply one-step changes immediately. Empty, negative, fractional, or out-of-range input is rejected without replacing the saved value. Correct it and apply again.
- 인앱에서 기준 N을 정하면 현재 적용값도 N이 됩니다. 기준3에서 **횟수 순환**은 `0→1→2→3→0`, **간편 켜기·끄기**는 `0↔3`입니다.<br>
  Setting the in-app limit N also sets the current target to N. With N=3, rotary mode cycles `0→1→2→3→0`; quick-toggle mode alternates `0↔3`.
- 기준5, 현재2일 때 플로팅은 `1/2`처럼 표시합니다. 실행을 껐다 켜면 현재값은 기준5로 복원되고 다시 셉니다. 기준0이면 플로팅을 눌러도0입니다.<br>
  With limit5 and current target2, the display is `1/2`. Restarting execution restores the target to5 and restarts counting. If the limit is0, floating taps cannot enable advancing.
- 숫자를 잡고 끌어 놓으면 위치 저장. 숫자 탭은 횟수 변경. **×는 전체 실행 종료**, 인앱 표시 토글 OFF는 **플로팅만 숨김**입니다.<br>
  Drag the numbers to save a position; tap to change the target. **× stops execution**, while disabling the in-app floating option **only hides the control**.
- 플로팅은72×56dp, 배경 약40% 불투명도입니다. 작은 ×가 누르기 어렵다면 하단 또는 빠른 설정 토글로 끄세요.<br>
  The control is72×56dp with roughly40% background opacity. If the small × is difficult to tap, use the in-app or Quick Settings toggle.

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

**광고 바로 넘기기**는 Instagram의 명시적 광고 배지를 인식할 때만 작동하며 기본 OFF입니다. 실행 ON·현재1회 이상·Instagram 선택이 필요합니다. OFF이면 광고는 수동으로 넘깁니다. 재생바가 없거나 캡션에 #광고가 있다는 이유만으로 넘기지 않습니다. 영상 안 CTA 카드를 클릭하지 않으며 별도 팝업을 자동으로 닫지 않습니다. YouTube 광고 처리는 지원하지 않습니다.<br>
**“광고 바로 넘기기” (skip ads immediately)** is OFF by default and only acts on explicit, recognized Instagram ad badges. It requires execution ON, a positive target, and Instagram selected. With it OFF, advance ads manually. Missing progress bars or #ad captions alone are not sufficient. It never clicks ad CTAs or closes separate popups. YouTube ad handling is not supported.

**사용 준비 → 빠른 설정에 추가**로 “쇼츠 넘김” 타일을 추가합니다. 안 되면 휴대폰 빠른 설정 편집에서 직접 추가하세요. 짧게 누르면 실행 토글, 길게 누르면 앱 설정입니다. 패널을 열어 둔 동안은 영상 자동 넘김을 대기합니다.<br>
Add the “쇼츠 넘김” tile from setup, or manually through the phone's Quick Settings editor. Tap to toggle execution; long-press for app settings. Advancing pauses while the panel is open.

## 알려진 문제와 복구 / Known issues and recovery

- **YouTube 간헐 정지:** “넘김 확인 실패” 상태에서는 중복 스와이프를 막기 위해 멈춥니다. 현재 화면을 확인한 뒤 실행을 OFF→ON하세요. 배터리 예외만으로 이 상태가 해제되지 않습니다. 플로팅이 없으면 앱에서 상태를 확인해야 합니다.<br>
  **Intermittent YouTube stops:** an unconfirmed transition triggers a safety stop. Check the current screen, then toggle execution OFF→ON. Battery exemption alone does not clear it. Without floating controls, inspect status in the app.
- **반복 경계 누락:**18초 영상에서 마지막 관측값이16초 후0초로 바뀔 때 완주 대신 탐색으로 판단하여 카운트가 오르지 않는 사례가 있습니다. 미해결입니다.<br>
  **Missed loop boundary:** an 18-second video observed jumping from 16 to 0 can be treated as seeking rather than completion, leaving the count unchanged. This remains unresolved.
- **작은 창/PiP, 잠금 화면, 다른 앱 전면은 지원 대상이 아닙니다.** 전체 화면으로 복귀하세요. 프로세스/접근성 서비스 재연결 후에는 안전을 위해 실행 OFF이므로 직접 다시 켭니다.<br>
  **PiP, locked screens, and background target apps are not supported.** Return to full screen. Process/service reconnection intentionally turns execution OFF; re-enable it manually.
- 시간 정보가 없는 일반 릴스, 식별 불가능한 연속 광고, 특수 팝업 및 앱 UI 변경은 미지원 또는 추가 검증 대상입니다. 동작이 불확실하면 실행을 끄고 직접 넘기세요.<br>
  Reels without usable time data, indistinguishable consecutive ads, special popups, and app UI changes may be unsupported. If behavior is uncertain, stop execution and advance manually.

추가 휴대폰 디버깅은 사용자 요청으로 보류했습니다. 상세 증상·증거·재시험 계획은 [디버그 대장](docs/DEBUG_LOG.md)에 보존합니다.<br>
Further device debugging was deferred at the user's request. Evidence and follow-up plans are in the [debug log](docs/DEBUG_LOG.md).

## 데이터와 업데이트 / Data and updates

설정·플로팅 위치는 기기에 저장하고 클라우드 백업은 제외합니다. 영상·계정·시청이력을 저장하지 않으며 인터넷 권한도 없습니다. 접근성 화면 정보는 감지에 일시적으로 사용합니다. 개인 화면 캡처·디버그 자료·서명 개인키는 이 저장소에 포함하지 않습니다.<br>
Settings and floating position are stored locally, with cloud backup disabled. The app has no Internet permission and does not persist videos, accounts, or viewing history. Accessibility screen data is used transiently for detection. Personal captures, device logs, and private signing keys are excluded.

기존 앱과 서명이 같으면 APK 덮어쓰기 설치로 설정을 유지할 수 있습니다. 다른 서명의 빌드는 업데이트되지 않을 수 있으며, 삭제 후 재설치하면 저장값이 사라집니다. 이 Release의 APK와 개인/CI 재빌드의 서명은 같지 않을 수 있습니다.<br>
An APK signed with the same key can update the installed app while preserving settings. Different signatures may prevent an update; uninstalling loses saved settings. Personal or CI rebuilds may use a different key from the release APK.

## 빌드·시험 / Build and test

JDK17 이상, Android SDK35, Build Tools35.0.0, Gradle wrapper8.9 / Android Gradle Plugin8.7.3. SDK는 `ANDROID_HOME` 또는 Git에서 제외된 `local.properties`로 지정합니다. 빌드 의존성 다운로드에는 인터넷이 필요합니다.<br>
Use JDK17+, Android SDK35, Build Tools35.0.0, Gradle wrapper8.9 / AGP8.7.3. Set `ANDROID_HOME` or an ignored `local.properties`. Building requires dependency downloads.

Windows의 `verify.ps1`로 사용자 지정 SDK 경로를 쓰려면 `ANDROID_HOME`도 설정해야 합니다. 이 스크립트의 직접JUnit 단계는 `local.properties`를 읽지 않고 `ANDROID_HOME` 또는 기본 SDK 경로를 사용합니다.<br>
For a custom SDK location with Windows `verify.ps1`, also set `ANDROID_HOME`: its direct JUnit step uses that variable or the default SDK location, not `local.properties`.

Windows:
```powershell
.\scripts\verify.ps1
```

Linux / GitHub Actions:
```sh
./gradlew --no-daemon assembleDebug testDebugUnitTest lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

로컬0.2.2: 빌드·직접JUnit148개·정적 안전 연결 검사 PASS, lint 오류0/경고2. Windows 한글 경로에서 Gradle 시험 실행기 오류가 있어 스크립트가 같은 컴파일 결과를 직접JUnit으로 실행합니다. Linux CI 결과는 Actions와 [검증 기록](docs/VERIFICATION.md)을 확인하세요.<br>
Local0.2.2: build,148 direct JUnit tests, and static wiring checks pass; lint reports0 errors/2 warnings. The Windows script uses direct JUnit because the Gradle test worker failed on the Unicode path. See Actions and the [verification record](docs/VERIFICATION.md) for Linux CI.

**0.2.2의 새 배터리 메뉴 실제 터치·화면 복귀 시험 및 설치 후 자동 넘김 재시험은 미실행**입니다. 이전0.2.1 실기기 성공 사례를 새 버전 전체 검증으로 보고하지 않습니다.<br>
**The new0.2.2 battery menu's actual touch/return flow and post-install auto-advance have not been device-tested.** Earlier0.2.1 successes do not certify this release.

## 문서 / Documentation

이 README는 한영 사용 안내입니다. 아래 상세 개발·시험 문서는 주로 한국어입니다.<br>
This README is the bilingual user guide. Detailed development and test records below are primarily Korean.

- [상세 사용 설명서 / Detailed guide](docs/USER_GUIDE.md)
- [제품 기준 / Behavior contract](docs/PRODUCT_SPEC.md)
- [UI·인간공학 기준 / UI and usability](docs/UI_DESIGN.md)
- [검증 / Verification](docs/VERIFICATION.md)
- [디버그 대장 / Debug log](docs/DEBUG_LOG.md)
- [누적이력 / Changelog](docs/CHANGELOG.md)
- [인수인계·문서 색인 / Handover and index](HANDOVER.md)
- [0.2.2 릴리스 / Release record](docs/releases/v0.2.2.md)

YouTube 및 Instagram과 무관한 개인 프로젝트입니다. 별도의 오픈소스 라이선스는 아직 지정하지 않았습니다.<br>
An independent project, not affiliated with YouTube or Instagram. No separate open-source license has been designated yet.
