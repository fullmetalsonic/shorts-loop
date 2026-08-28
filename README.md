# 쇼츠 자동 넘김 · ShortsLoop 0.3.0

YouTube Shorts와 Instagram Reels를 설정한 횟수만큼 보고 자동으로 넘기는 Android 앱입니다. 앱별 설정·반투명 플로팅·빠른 설정 토글과 선택형 듀얼 화면 처리를 제공합니다. YouTube나 Instagram 앱을 수정하지 않습니다.

An Android auto-scroll app for YouTube Shorts and Instagram Reels,with per-app play counts,optional translucent controls,Quick Settings and opt-in multi-window processing. It does not modify either host app.

**0.3.0/code32 공개 완료.** 2026-08-28 공개 릴리스·CI·익명 다운로드 동일성을 확인했습니다. [0.3.0 릴리스](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.3.0) · [APK 다운로드](https://github.com/fullmetalsonic/shorts-loop/releases/download/v0.3.0/shorts-loop-v0.3.0.apk) · [검증 원장](docs/VERIFICATION.md).

**0.3.0/code32 is publicly released.** Public release,CI and anonymous-download parity were verified on2026-08-28. Use the release,APK and verification links above.

최종 공개 APK는742,854bytes이며,SHA256은 `9AA1E88425206CF1B9CEFBCD55B722DF83822D2F00830C81DDF925981AF394AA`입니다. 실기기 흐름 검증 후보53FD와 내장 소스 revision 기록만 다르고 나머지 ZIP 항목은 같습니다. 최종 APK도3개 OS 검사·실폰 덮어 설치/해시·설정 보존을 확인했습니다. CI는 debug/release 각각564시험 통과,lint0오류/12경고이며 로컬0오류/13경고와 구분합니다.

The final742,854-byte APK has the SHA256 above. Only the embedded source-revision record differs from the53FD physical-flow candidate;all other ZIP entries match. The final APK passed three-OS and physical upgrade/hash/preferences checks. CI passed564 tests each for debug/release,with lint0errors/12warnings versus local0errors/13warnings.

## 0.3.0의 핵심 / What's new

- **앱별 독립 설정:** YouTube와 Instagram의 반복 횟수·긴 영상 기준·플로팅 탭 방식·위치·일시정지를 따로 저장합니다. 이전 공통값은 양쪽 초기값으로 한 번 복사하고 원래 설정을 보존합니다.<br>
  **Separate settings:** play counts,long-video thresholds,floating tap modes,positions and pause state are per host. Existing shared values initialize both hosts once without deleting the original preferences.
- **듀얼 화면 적용은 기본OFF:** OFF는 현재 활성 대상 창만,ON은 화면에 보이는 선택 대상을 각각 처리합니다. 전체 실행과 듀얼을ON으로 유지하면 전체화면 한 앱·분할 두 앱·일반 앱 옆 한 쇼츠 앱을 현재 창에 맞춰 감지합니다. 배치마다 모드를 다시 켤 필요는 없습니다.<br>
  **Dual mode defaultsOFF:** OFF processes the active target window;ON follows visible selected hosts—one fullscreen host,two split hosts,or one host beside an ordinary app. Layout changes alone do not require re-enabling the mode.
- **앱별 플로팅과 중지:** 각 대상에 앱 표시가 있는72×56dp 플로팅을 사용합니다. 탭·끌기·X는 해당 앱만 조작합니다. **X는 해당 앱만 일시정지**,인앱/타일의 **전체OFF는 모두 중지**합니다.<br>
  **Per-host controls:** each eligible host has a labelled72×56dp control. Tapping,dragging andX affect only that host. **X pauses its host;masterOFF or tileOFF stops all automation.**
- **창 안전 검사:** 독립 카운터와 전환 확인을 유지하고 입력은 하나씩 처리합니다. 좁은 창·회전·전체화면 전환의 시스템 손잡이를 제한적으로 구분하되,실제 입력 경로의 가림과 변경된 창 경계는 계속 차단합니다.<br>
  **Window safety:** counters and confirmations stay independent;inputs are serialized. Narrow,rotated and fullscreen system controls are qualified conservatively without allowing obstructed touch paths or stale window geometry.

듀얼ON이 다른 앱의 강제 재생을 뜻하지 않습니다. 숨겨진 대상은 처리하지 않으며,키보드·팝업·잠금·앱 자체 일시정지와 권한/서비스 중단의 보호는 유지합니다. 모든 앱·영상·배치·기기를 보장하지 않습니다.

DualON does not force playback. Hidden hosts are not processed;keyboard,popup,lock,host-pause and permission/service safeguards remain. Support is not guaranteed for every app,video,layout or device.

## 설치와 첫 실행 / Install and start

Android8.0(API26) 이상. 호스트 앱 자체의 기기/OS 지원은 별도이며 [호환성 표](docs/COMPATIBILITY.md)를 확인하세요. [배포 APK](https://github.com/fullmetalsonic/shorts-loop/releases/download/v0.3.0/shorts-loop-v0.3.0.apk)와 Release의 SHA256을 확인해 설치하세요. 같은 패키지·서명의 덮어 설치는 저장된 설정을 유지하며,앱을 삭제하면 설정이 사라집니다.

Android8.0/API26+. Host-app compatibility is separate;see the support table. Verify the published `shorts-loop-v0.3.0.apk` and checksum,then install over the same-signed app to retain settings. Uninstalling removes preferences.

1. 앱을 열고 전체 실행OFF 상태에서 **사용할 앱 / Apps to use**를 선택합니다.<br>
   Open the app with executionOFF and select the hosts to use.
2. **앱별 설정 / Settings for each app**의 YouTube·Instagram 탭에서 반복 횟수와 긴 영상·플로팅 옵션을 각각 정하고 적용합니다. 예: YouTube2회,Instagram1회. **편집 탭은 실행할 앱 선택과 다릅니다.**<br>
   Apply settings separately in each host tab—for example,YouTube2 plays and Instagram1. **Editor tabs do not select which apps run.**
3. Instagram의 광고·시간제·사진,YouTube의 라이브 옵션은 해당 탭에서 필요한 것만 켭니다.<br>
   Enable desired Instagram ad/timer/photo rules and YouTube live-preview rules in their own tabs.
4. **사용 준비 / Setup**에서 이 앱의 접근성을 직접 켭니다. 플로팅을 원할 때만 다른 앱 위 표시 권한을 켭니다. 배터리 제한 없음 안내도 확인하세요.<br>
   Manually enable accessibility. Grant overlay permission only if using floating controls,and review the battery guidance.
5. 활성 창만 처리하려면 듀얼OFF. 일반 앱을 다른 창에서 조작하면서 보이는 쇼츠를 처리하거나 두 쇼츠 앱을 함께 처리하려면 **듀얼 화면 적용 / Use dual-window mode**를ON으로 합니다.<br>
   Leave dualOFF for the active window only;turn itON for a visible Shorts host beside another app or for two visible hosts.
6. 하단 **전체 자동 넘김 실행 / Auto-advance**을 직접 켭니다. 모드·앱 선택·권한 안내 버튼만으로 실행이 켜지지 않습니다.<br>
   Explicitly enable the master switch. Mode changes,host selection and permission-help buttons do not start execution.
7. X로 한 앱을 멈췄다면 해당 탭의 **이 앱 다시 시작 / Resume this app**을 사용합니다. 전체OFF 상태에서는 이 버튼만으로 전체 실행이 켜지지 않습니다. 전체OFF→ON은 선택한 앱을 각각 기준 횟수로 재개합니다.<br>
   Resume a host paused byX from its tab. Host resume cannot enable an off master;masterOFF→ON resumes selected hosts at their own configured counts.

접근성은 화면을 읽고 조작할 수 있는 강한 권한입니다. 화면 분석을 꺼도 기본 감지와 넘김에 필요합니다. **ADB·무선 디버깅·루팅은 일반 사용에 필요하지 않습니다.** 접근성 설정은ON인데 연결이 안 되면 해당 서비스를OFF→ON한 뒤 앱의 전체 실행을 다시 켜세요. 앱은 권한을 몰래 승인하거나 서비스 재연결 후 자동으로 실행하지 않습니다.

Accessibility can read and interact with the screen and is required even with visual analysisOFF. **Normal use does not requireADB,wireless debugging or root.** If enabled but disconnected,reconnect the service in Android Settings,then re-enable execution. Permissions and execution after reconnection are never silently granted.

## 횟수와 독립 옵션 / Counts and independent rules

횟수는 추가 반복이 아닌 **첫 재생을 포함한 총 횟수**입니다.2회는 `1/2 → 2/2 → 다음 영상`이며,중간에 켜면 정상 시작점을 기다릴 수 있습니다. 입력0–99와▲/▼,플로팅의0…기준 순환 또는0↔기준 전환을 지원합니다. 빈칸·음수·소수·범위 밖 입력은 저장값을 덮어쓰지 않습니다.

Counts mean **total plays including the first**,not extra repeats. At2 plays,the display is `1/2 → 2/2 → next`;starting mid-video may wait for a fresh cycle. Use0–99,step buttons,and rotary0…N or0↔N floating taps. Invalid input does not replace committed settings.

| 기능 / Rule | 기본·범위 / Default and range | 반복0회 / At zero plays |
|---|---|---|
| 일반 반복 / Ordinary repeat | 총0–99회 / Total0–99 | 중지 / Stops |
| 긴 영상 / Long videos | OFF,총길이60초,1–3600초 / Known total duration | 독립 / Independent |
| Instagram 시간제 / Clockless timer | OFF,10초,5–60초 | 중지 / Stops |
| Instagram 광고 / Ads | OFF,명시적 광고 인식 / Explicit recognition | 독립 / Independent |
| Instagram 사진 / Photos | OFF,통째·한 장 각각3초,0–10초 / Whole/each | 독립 / Independent |
| YouTube 라이브 미리보기 / Live previews | OFF,0초,0–60초 | 독립 / Independent |

**전체 실행OFF는 모든 규칙을 멈춥니다.** 긴 영상은 확인된 총길이가 기준 이상일 때 적용하며 그 시간만큼 기다리는 기능이 아닙니다. 시간제는 길이·완주 횟수 추정이 아닌 제한적 대체 대기입니다. 라이브·사진의0초는 꺼짐이 아니라 안전 확인 후 바로 동작입니다.

**MasterOFF stops every rule.** Long-video filtering uses known total duration≥threshold,not a waiting period. The clockless timer is a limited timeout,not a duration or completion estimate. Zero-second live/photo delays mean immediate after safety checks,not disabled.

플로팅은 선택 사항입니다. 표시OFF는 숫자창만 숨기며 자동화를 끄지 않습니다. X는24×24dp의 작은 터치 영역이므로 필요하면 인앱 또는 빠른 설정을 사용하세요. 빠른 설정 타일은 짧게 누르면 전체 실행 토글,길게 누르면 설정입니다.

Floating controls are optional;hiding them does not stop automation. X remains a small24×24dp target;use in-app or Quick Settings controls when needed. Tap the tile to toggle all execution,long-press for settings.

자세한 예시: [사용 설명서](docs/USER_GUIDE.md) · [시간제](docs/TIMED_FALLBACK.md) · [사진](docs/PHOTO_REELS.md) · [라이브](docs/LIVE_SKIP.md) · [제한적 재생 복구](docs/PLAYBACK_RECOVERY.md).

## 검증과 한계 / Verification and limitations

최신 제품 후보는 BUILD·564JUnit·정적 가드,API26/33/34의28,043/28,053/27,794 native 검사와 설치 해시 확인을 통과했습니다. 로컬lint는 오류0·경고13,독립 검토 범위의 미해결P1/P2는0건입니다. 이는 모든 시나리오를 보장한다는 뜻은 아닙니다. 정확한 APK·시험 조건·게시 상태는 [검증 원장](docs/VERIFICATION.md)과 [0.3.0 기록](docs/releases/v0.3.0.md)을 따릅니다.

The product candidate passed build,564 unit tests,static guards,28,043/28,053/27,794 native checks onAPI26/33/34,and installed-APK hash verification. Local lint has0 errors/13 warnings;independent review found0 unresolvedP1/P2 issues in scope. This is not universal scenario coverage;see the artifact-specific records.

같은 최종 시험 후보에서 실행·듀얼ON을 유지한 채 다음 구간을 확인했습니다.

| 구간 / Segment | 확인 결과 / Confirmed result |
|---|---|
| Instagram 전체화면 / Fullscreen | Instagram3회:일반1·광고1·긴 영상1 |
| 분할 복귀 / Return to split view | YouTube추가4회:일반3·긴 영상1;Instagram추가1회:일반 |
| Instagram 전체화면 재복귀 / Back to fullscreen | Instagram추가9회:일반4·긴 영상4·광고1;숨겨진YouTube는4회에서 더 요청하지 않음 |

These segments used the same tested candidate while execution and dual mode stayedON. They are not a ten-consecutive-per-app endurance claim. Earlier Calculator coexistence and top/bottom tests used earlier candidates and are kept separate.

남은 한계는 원래 회전 방향으로 복귀하는 시험,전체 예외 조합,새 듀얼 사진 실물 표본,장시간 및 모든 앱/기기입니다. 다른 창의 키보드·팝업,PiP·잠금·가려짐·앱 자체 정지는 안전 대기/정지로 이어질 수 있습니다. 인식 실패를 추측 스와이프나 강제 재생으로 우회하지 않습니다. 반복 경계 누락과 식별이 불명확한 연속 콘텐츠의 제한도 [디버그 대장](docs/DEBUG_LOG.md)에 남깁니다.

Remaining limits include returning to the original rotation,all exception combinations,fresh dual-photo samples,long sessions and every app/device. Keyboards,popups,PiP,locking,obstruction or host pauses may cause guarded waits/stops. No guessed swipe or forced playback bypasses recognition failure. Loop-boundary and ambiguous-content limitations remain documented.

화면 분석 보조는 **선택형 실험 기능·Android14 이상·별도 동의**를 유지합니다. 두 대상 창이 보이면 분석을 비활성화하되 저장한 선택은 보존합니다. 별도 미연결 오디오·영상 주기 실험은 제품 기능이 아닙니다.

Visual assistance remains **experimental,opt-in,Android14+ and separately consented**. It is inactive while both target windows are visible without deleting the saved choice. Unwired audio/visual-period experiments are not product features.

## 언어·개인정보·업데이트 / Language,privacy and updates

- 시스템 첫 언어가한국어이면한국어,그 밖에는영어입니다. 표시 번역이 모든 호스트 감지 언어를 지원한다는 뜻은 아닙니다. [언어 안내](docs/LOCALIZATION.md).<br>
  The first system language selects Korean when Korean,otherwise English. UI translation does not guarantee detection in every host language.
- 영상·계정·시청이력을 업로드하지 않습니다. 설정은 로컬 저장이며,접근성 정보는 감지에 일시적으로 사용합니다. 개인 화면·원시 로그·서명 개인키는 공개하지 않습니다.<br>
  Videos,accounts and viewing history are not uploaded. Preferences stay local;accessibility information is used transiently. Personal captures,raw logs and private signing keys are excluded.
- 인앱 업데이트는 고정 Public GitHub 저장소에서 확인합니다. 앱을 열 때 최대24시간마다 선택형 조회하며 수동 조회·다운로드·Android 설치 확인을 분리합니다. 백그라운드 감시·자동 설치는 없습니다.<br>
  Updates use this fixed Public GitHub repository,with optional checks on opening no more than once per24 hours,separate manual download and Android installation confirmation. No background monitor or silent installation.
- 공개 prerelease도 후보에 포함하되 draft는 제외합니다. 더 높은 versionCode·지원OS·메타데이터·크기·SHA256·패키지·같은 서명 집합을 검사합니다. 설치 단계는 전체 실행을OFF로 두고 다른 설정을 보존합니다.<br>
  Published prereleases are eligible;drafts are excluded. Metadata,higher versionCode,OS,size,SHA256,package and signer-set checks apply. Installation stops execution while preserving other settings.

[업데이트 절차](docs/UPDATE_DELIVERY_PLAN.md) · [배포 안전 검사](docs/RELEASE_PRESENTATION_AUDIT.md).

## 빌드 / Build

Java/native Android,minimumAPI26,compile/target35. 개발용 debug와 사용자 배포용 non-debuggable release를 구분합니다. 공개 업데이트에는 기존 서명이 필요하며,개인/CI 빌드는 서명이 달라 기존 앱 위에 설치되지 않을 수 있습니다.

Java/native Android,minimumAPI26,compile/target35. Development debug and non-debuggable distribution release builds are separate. Updating the original app requires its signing identity;personal/CI builds may not install over it.

```powershell
.\scripts\verify.ps1 -BuildType release
.\scripts\verify-release-safety.ps1 -Apk app/build/outputs/apk/release/app-release.apk -DebugApk app/build/outputs/apk/debug/app-debug.apk
.\scripts\prepare-release.ps1 -Apk app/build/outputs/apk/release/app-release.apk
```

JDK17+,Android SDK35,Build Tools35.0.0,Gradle8.9/AGP8.7.3. 비교용 debug 빌드와 로컬 서명 준비는 [빌드 안내](docs/RELEASE_BUILD.md)를 따르며,키·암호를 저장소에 넣지 마세요. 위 명령은 재현 절차이지 공개 완료 선언이 아닙니다.

See the build guide for the comparison debug build and local signing setup. Never commit keys/passwords. These commands are a reproducible procedure,not a declaration that publication has completed.

## 문서·이력 / Documentation and history

- [사용 설명서 / User guide](docs/USER_GUIDE.md)
- [분할 화면 계약·후보별 이력 / Multi-window scope and candidate history](docs/SPLIT_SCREEN_PLAN.md)
- [제품 기준 / Product contract](docs/PRODUCT_SPEC.md) · [UI 기준 / UI criteria](docs/UI_DESIGN.md)
- [호환성 / Compatibility](docs/COMPATIBILITY.md) · [언어 / Localization](docs/LOCALIZATION.md)
- [검증 / Verification](docs/VERIFICATION.md) · [디버그 / Debug log](docs/DEBUG_LOG.md)
- [누적이력 / Changelog](docs/CHANGELOG.md) · [인수인계·색인 / Handover and index](HANDOVER.md)
- 이전 공개 기록 / Previous releases: [0.2.9](docs/releases/v0.2.9.md),[0.2.8](docs/releases/v0.2.8.md),[0.2.7](docs/releases/v0.2.7.md),[0.2.6](docs/releases/v0.2.6.md),[0.2.5](docs/releases/v0.2.5.md),[0.2.4](docs/releases/v0.2.4.md),[0.2.2](docs/releases/v0.2.2.md)
- [0.2.7 플로팅 수정 이력 / Historical floating-layout fix](docs/FLOATING_LAYOUT_FIX.md)

이전 후보의 실패·개별 시험·공개 파일 검증은 해당 원장과 릴리스에 보존합니다. 과거 YouTube20회나 사진0/3/10초 시험을0.3.0의 새 시험으로 재사용하지 않습니다.

Earlier failures,individual tests and public-artifact checks remain in their version-specific records. Historical YouTube20 or photo0/3/10-second trials are not new0.3.0 tests.

## 원본 출처 표시 요청 / Attribution request

YouTube 및 Instagram과 무관한 개인 프로젝트입니다. **별도의 오픈소스 라이선스는 아직 지정하지 않았습니다.**

An independent project,not affiliated with YouTube or Instagram. **No separate open-source license has been designated yet.**

수정·파생 버전을 공유하거나 배포할 때 README 또는 앱 소개에 **ShortsLoop**와 [원본 저장소](https://github.com/fullmetalsonic/shorts-loop)를 표시해 주세요. 자체 변경을 구분하고 원작자가 제작·보증한 것처럼 표시하지 말아 주세요.

When sharing a modified or derived version,please credit **ShortsLoop** and the [original repository](https://github.com/fullmetalsonic/shorts-loop). Distinguish your changes and do not imply original-author endorsement.

예 / Example: `Based on ShortsLoop — https://github.com/fullmetalsonic/shorts-loop (modified by [your name])`.

이 문구는 출처 표시 요청이며 별도의 라이선스나 추가 이용 권한 부여를 대신하지 않습니다.<br>
This is an attribution request,not a substitute for a separate license or additional grant of usage rights.
