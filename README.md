# 쇼츠 자동 넘김 · ShortsLoop 0.2.5 (시험판 / Prerelease)

**0.2.5(code21) 공개 시험판입니다.** [Android 버전별 기능 안내](docs/COMPATIBILITY.md), GitHub 인앱 업데이트, 사용 빈도에 따른 메뉴 배치와 선택형 YouTube 라이브 미리보기 넘김을 통합했습니다. 구형 OS에서는 지원하지 않는 화면 분석만 비활성화하고 기본 기능은 유지합니다. 빌드·356제품시험·일반 YouTube10연속 및 공개 파일 동일성을 확인했습니다. [검증과 한계](docs/VERIFICATION.md).<br>
**0.2.5(code21) is a public prerelease.** It combines [per-Android feature guidance](docs/COMPATIBILITY.md), in-app GitHub updates, reordered settings and optional YouTube live-preview skipping. Unsupported visual analysis does not disable basic features. Build,356 product tests,ten consecutive ordinary YouTube transitions and public-artifact parity passed. See the linked verification record for limits.

일반 YouTube 쇼츠와 Instagram 릴스를 정한 횟수만큼 보고 다음 영상으로 넘기는 Android 시험판입니다. **20연속 전환 시험은 미완료**입니다. 모든 영상·기기에서의 안정성을 보장하지 않으며 이전 버전의 결과와 이번 검증을 구분합니다.

An experimental Android app that advances supported YouTube Shorts and Instagram Reels after a chosen number of plays. **Twenty-consecutive-transition testing remains incomplete.** Reliability across every video/device is not guaranteed; historical results remain separate from this release's checks.

**진행 정보 없는 Instagram 영상만 시간제로 넘김.** 기본 OFF·10초, 숫자 입력과 ＋/－로 5~60초를 1초씩 조절합니다. 정상 진행 정보가 있는 영상과 YouTube는 기존 재생 횟수대로 넘깁니다. 반복 2회/시간 10초이면 정상 영상은 2회, 정보 없는 대상 영상은 **총 10초** 뒤 이동합니다. 완주 횟수나 영상 길이를 알아내는 기능은 아닙니다. [설정·안전 동작·검증](docs/TIMED_FALLBACK.md).

**Timed fallback for clockless Instagram videos only.** OFF by default, initially 10 seconds, adjustable from 5 to 60 with typing and one-second −/+ controls. Usable playback clocks and YouTube retain repeat counting. With 2 plays/10 seconds, recognized videos use 2 plays; eligible clockless videos wait **10 seconds total**, not 20. This does not infer completion or duration.

**반복 0회와 실행 OFF는 다릅니다.** 0회는 일반 영상 반복·시간제 넘김을 중지합니다. Instagram 선택 + 광고 옵션 ON + 메인 실행 ON이면 **0회여도 인식된 광고는 넘깁니다**. YouTube 선택 + 라이브 옵션 ON + 메인 실행 ON이면 인식된 라이브 미리보기도 반복 0회와 독립적으로 넘깁니다. 메인 실행 OFF는 광고·라이브까지 모두 중지합니다.

**Zero plays differs from execution OFF.** Zero stops normal repeat-based and timed advancing. Recognized Instagram ads can still advance at zero when Instagram is selected and both the ad option and main execution are ON. Recognized YouTube live previews are also independent of zero plays when YouTube, the live option and main execution are enabled. Main execution OFF stops everything, including ads and live previews.

**기존 화면 분석 시험 기능은 선택형으로 보존됩니다.** 기본 OFF이며 Android 14 이상에서 별도 동의 후 화면을 RAM에서 비교합니다. 시간제와 함께 켜면 시간제가 우선하고 화면 분석 선택값은 보존합니다. 학습 때문에 추가 재생될 수 있으며 정확한 총 N회를 보장하지 않습니다. [화면 분석 안내](docs/VISUAL_ASSIST_TRIAL.md).

**The existing visual-assist trial remains optional.** It is OFF by default, separately consented, Android 14+, and RAM-only. Timed fallback takes priority when both are enabled, preserving the visual preference. Learning can add plays; exact total N is not guaranteed.

[0.2.5 다운로드 / Download](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.5) · [이전 0.2.4 / Previous release](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.4) · [검증 / Verification](docs/VERIFICATION.md)

## 주요 기능 / Features

- 총 재생 횟수 0~99: 숫자 입력과 ▲/▼. **0은 일반 영상·시간제 중지, 광고·라이브는 별도 옵션**.<br>
  Total plays from 0 to 99, with typing and step buttons. **Zero stops normal and timed advancing; ads and live previews use separate options**.
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

Android 8.0(API26) 이상이 설치 대상입니다. 공식 YouTube 설치 안내는 Android 9 이상이며 Instagram의 최소 OS는 확인되지 않아 기기 Play 스토어의 호환 여부를 확인해야 합니다. API29부터 타일 상태줄, API33부터 인앱 타일 추가 요청, API34부터 화면 분석 시험을 지원합니다. [지원표와 근거](docs/COMPATIBILITY.md). 과거 실기기 시험은 Galaxy Z Fold8 / Android17에서 했으며 모든 기기를 보장하지 않습니다. 인앱 언어는 현재 한국어입니다.<br>
The app minimum is Android 8.0/API26. YouTube's official requirement is Android 9+; Instagram's minimum is unverified, so check its device Play Store listing. Tile subtitles require API29, in-app tile-add requests API33, and visual assist API34. See the linked support table and sources. Earlier device tests used Galaxy Z Fold8 / Android17, not every device. The interface is currently Korean.

1. 위0.2.5 Release에서 개발 서명 APK를 내려받고 첨부 SHA256을 확인해 설치하세요. Play Store 배포본이 아닙니다. **0.2.4에는 인앱 업데이트가 없으므로 0.2.5 최초 설치는 브라우저 등에서 APK를 직접 받아 덮어쓰기 설치해야 합니다.**<br>
   Download the development-signed APK from the0.2.5 release above and verify its SHA256. This is not a Play Store release. **0.2.4 has no updater: manually download and install0.2.5 over the existing app first.**
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

## 진행 정보 없는 릴스 시간제 / Timed fallback for clockless Reels

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

업데이트 전 광고 옵션이 ON이었다면 0.2.4부터는 반복 0회에서도 광고가 넘어갈 수 있습니다. 광고를 원치 않으면 해당 옵션을 끄세요. **광고만 사용: 반복 0회 + Instagram 선택 + 광고 ON + 라이브 OFF + 전체 실행 ON.**<br>
If ads were already ON before updating, versions from 0.2.4 onward can skip them even at zero plays. Turn the option OFF if unwanted. **Ads only: zero plays + Instagram selected + ads ON + live OFF + main execution ON.**

**사용 준비 → 빠른 설정에 추가**로 “쇼츠 넘김” 타일을 추가합니다. 안 되면 휴대폰 빠른 설정 편집에서 직접 추가하세요. 짧게 누르면 실행 토글, 길게 누르면 앱 설정입니다. 패널을 열어 둔 동안은 영상 자동 넘김을 대기합니다.<br>
Add the “쇼츠 넘김” tile from setup, or manually through the phone's Quick Settings editor. Tap to toggle execution; long-press for app settings. Advancing pauses while the panel is open.

## 알려진 문제와 복구 / Known issues and recovery

- **YouTube 간헐 정지:** “넘김 확인 실패” 상태에서는 중복 스와이프를 막기 위해 멈춥니다. 현재 화면을 확인한 뒤 실행을 OFF→ON하세요. 배터리 예외만으로 이 상태가 해제되지 않습니다. 플로팅이 없으면 앱에서 상태를 확인해야 합니다.<br>
  **Intermittent YouTube stops:** an unconfirmed transition triggers a safety stop. Check the current screen, then toggle execution OFF→ON. Battery exemption alone does not clear it. Without floating controls, inspect status in the app.
- **반복 경계 누락:**18초 영상에서 마지막 관측값이16초 후0초로 바뀔 때 완주 대신 탐색으로 판단하여 카운트가 오르지 않는 사례가 있습니다. 미해결입니다.<br>
  **Missed loop boundary:** an 18-second video observed jumping from 16 to 0 can be treated as seeking rather than completion, leaving the count unchanged. This remains unresolved.
- **작은 창/PiP, 잠금 화면, 다른 앱 전면은 지원 대상이 아닙니다.** 전체 화면으로 복귀하세요. 프로세스/접근성 서비스 재연결 후에는 안전을 위해 실행 OFF이므로 직접 다시 켭니다.<br>
  **PiP, locked screens, and background target apps are not supported.** Return to full screen. Process/service reconnection intentionally turns execution OFF; re-enable it manually.
- 시간 정보가 없는 릴스는 지원되는 단일 영상에 한해 선택형 시간제를 쓰지만 완주는 보장하지 않습니다. 식별 불가능한 연속 광고, 특수 팝업 및 앱 UI 변경은 미지원 또는 추가 검증 대상입니다. 동작이 불확실하면 실행을 끄고 직접 넘기세요.<br>
  Eligible clockless single-video Reels can use the optional timer, without a completion guarantee. Indistinguishable consecutive ads, special popups and changed app UIs may remain unsupported. If uncertain, stop execution and advance manually.

20연속 전환 검증은 미완료입니다. 상세 증상·증거·재시험 계획은 [디버그 대장](docs/DEBUG_LOG.md)에 보존합니다.<br>
The 20-consecutive-transition test is incomplete. Evidence and follow-up plans are in the [debug log](docs/DEBUG_LOG.md).

## 데이터와 업데이트 / Data and updates

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
./gradlew --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

0.2.5(code21): **빌드·제품356시험·lint0오류/기존3경고, Android8/13/14 계측74/74/73개 PASS**. 실제 Android17에서 설치·접근성 연결·설정 보존·동일 APK 해시 및 일반 YouTube10연속 자동 전환(424.5초)을 확인했습니다. 10개에 라이브는 없었으며20연속은 미완료입니다. 고정 APK709703bytes, SHA-256은 [0.2.5 릴리스 기록](docs/releases/v0.2.5.md), 최종 게시 확인은 [VERIFICATION](docs/VERIFICATION.md) 참조. 아래는 **이전 0.2.4의 이력**입니다.<br>
0.2.5(code21): **build,356 product tests,lint0 errors/3 existing warnings and74/74/73 Android8/13/14 emulator checks passed**. Android17 installation,accessibility binding,preferences,exact-APK hash and ten consecutive ordinary YouTube transitions(424.5 seconds) passed. No live preview occurred;20 transitions remain incomplete. The frozen APK is709703 bytes; see the linked release record for SHA-256 and VERIFICATION for publication checks. The following results are **historical0.2.4 evidence**.

최종 0.2.4(code13): **제품 범위 227개 JUnit·빌드 PASS, lint 오류 0/경고 4**. 광고 독립·UI 변경을 포함한 APK의 서명과 휴대폰 설치본 SHA256 일치를 확인했습니다. [릴리스 기록](docs/releases/v0.2.4.md). Windows 한글 경로의 Gradle 시험 실행기 문제는 동일 컴파일 결과의 직접 JUnit 실행으로 우회했습니다.<br>
Final 0.2.4 (code13): **227 product tests and build PASS; lint 0 errors/4 warnings**. The APK includes independent ads and grouped UI; signature verification and installed-APK SHA256 matching passed. See release notes. Direct JUnit works around the Windows Unicode-path Gradle test-worker issue.

[GitHub CI](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33074271656)도 제품227개·빌드 PASS, lint0오류/3경고입니다. 익명 공개 다운로드 파일은 로컬/설치본과 같은681624bytes·SHA256이며 v2서명도 확인했습니다.<br>
GitHub CI also passed 227 product tests and build, with lint 0 errors/3 warnings. The anonymously downloaded APK matches the local/installed 681624-byte file and SHA256; its v2 signature verifies.

별도 미연결 `VisualSequenceTracker` 실험은 **20개 중 18 PASS/2 FAIL**입니다. 영상 주기 오추정·학습 실패가 남아 있으며 시간제 제품 기능의 실패와 구분합니다. 실험 원본을 보존하되 배포 앱과 제품 시험에서 제외합니다. 따라서 제품 227 PASS를 이 실험까지 포함한 전체 PASS로 표현하지 않습니다.<br>
The separate, unwired `VisualSequenceTracker` experiment has **18 PASS/2 FAIL out of 20 tests**, involving period estimation and learning. These are not timed-fallback failures. Its source is preserved but excluded from the release app and product tests. Product tests passing does not mean this experiment passes.

**실기기 20연속 전환 검증은 미완료입니다.** 정상 N회·시간제·광고의 관측 종류를 구분하며 후보별 관측을 합산하거나 수동 이동을 자동 성공으로 세지 않습니다. 0.2.4 최종 APK에서 **0회 광고 1회 자동 이동, 이후 일반·시간제 중지**를 확인했습니다. 여러 기기·장시간·모든 중단 경로 검증은 남아 있습니다.<br>
**The 20-consecutive-transition device test remains incomplete.** Normal, timed and ad observations are distinguished; candidate sessions are not combined and manual swipes do not count as automatic successes. The final 0.2.4 APK **automatically advanced one ad at zero plays, then kept normal/timed advancing stopped**. Other devices, long sessions and all interruption paths remain unverified.

## 별도 실험과 이전 공개 이력 / Separate experiments and release history

- [내부 오디오 수신](docs/AUDIO_PROBE_TRIAL.md)과 [음향 반복 후보](docs/AUDIO_PATTERN_TRIAL.md)는 기존 제품과 분리된 진단 앱입니다. 제품 자동 넘김에 연결하지 않았으며 오디오 시험 APK는 이번 릴리스에 첨부하지 않습니다.<br>
  The audio probe and pattern trials are separate diagnostic apps, not connected to product auto-advance. Their APK is not included in this release.
- [이전 공개 0.2.2](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.2)는 배터리 안내 추가 버전이며 시간제·화면 분석을 포함하지 않습니다. 당시 [GitHub CI](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33049522094)는 빌드·148시험·lint 0오류/1경고 PASS, 공개 APK와 로컬 원본의 크기·SHA256 일치를 확인했습니다. 이 과거 결과는 0.2.4 검증이 아닙니다. [이력](docs/releases/v0.2.2.md).<br>
  The previous public 0.2.2 release added battery guidance and lacks timed fallback and visual assist. Its CI passed build, 148 tests and lint (0 errors/1 warning), and its downloaded APK matched the local original. These historical results do not verify 0.2.4.

## 문서 / Documentation

이 README는 한영 사용 안내입니다. 아래 상세 개발·시험 문서는 주로 한국어입니다.<br>
This README is the bilingual user guide. Detailed development and test records below are primarily Korean.

- [상세 사용 설명서 / Detailed guide](docs/USER_GUIDE.md)
- [Android 버전별 지원 / Per-Android support](docs/COMPATIBILITY.md)
- [시간제 보조 설정·한계 / Timed fallback](docs/TIMED_FALLBACK.md)
- [YouTube 라이브 설정·한계 / Live-preview skipping](docs/LIVE_SKIP.md)
- [제품 기준 / Behavior contract](docs/PRODUCT_SPEC.md)
- [UI·인간공학 기준 / UI and usability](docs/UI_DESIGN.md)
- [검증 / Verification](docs/VERIFICATION.md)
- [디버그 대장 / Debug log](docs/DEBUG_LOG.md)
- [누적이력 / Changelog](docs/CHANGELOG.md)
- [인수인계·문서 색인 / Handover and index](HANDOVER.md)
- [0.2.4 공개 기록 / Release record](docs/releases/v0.2.4.md)
- [이전 0.2.2 릴리스 / Previous release record](docs/releases/v0.2.2.md)

YouTube 및 Instagram과 무관한 개인 프로젝트입니다. 별도의 오픈소스 라이선스는 아직 지정하지 않았습니다.<br>
An independent project, not affiliated with YouTube or Instagram. No separate open-source license has been designated yet.
