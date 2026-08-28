# 쇼츠 자동 넘김 · ShortsLoop 0.4.0

YouTube Shorts·Instagram Reels·TikTok 추천 영상을 설정한 횟수에 따라 자동으로 넘기는 Android 앱입니다. 앱별 설정, 반투명 플로팅, 빠른 설정 토글과 선택형 여러 앱 처리를 제공합니다. 원본 소셜 앱은 수정하지 않습니다.

An Android auto-scroll app for YouTube Shorts, Instagram Reels and readable TikTok recommendation videos, with separate play counts, translucent controls, Quick Settings and optional multi-window processing. Host apps are not modified.

**0.4.0/code33 공개 완료.** PC 단위시험638개씩(debug/release),Android8·13·14 검사,GitHub Actions와 익명 다운로드 파일 일치를 확인했습니다. 설치 파일과 인앱 업데이트 정보를 함께 제공합니다. 상세 근거는 [0.4.0 릴리스 기록](docs/releases/v0.4.0.md)과 [검증 원장](docs/VERIFICATION.md)을 확인하세요. 새 버전 실폰 자동 넘김은 아직 미검증이며 과거0.3.0 시험을 새 버전의 성공으로 계산하지 않습니다.

**0.4.0/code33 is public.**638 unit tests per variant,API26/33/34 native checks,GitHub Actions and anonymous download parity pass. The APK and in-app update metadata are available. New-version physical playback remains untested;earlier0.3.0 physical results are not new-version passes.

[최신 공개 릴리스 / Latest published release](https://github.com/fullmetalsonic/shorts-loop/releases/latest) · [사용 설명서 / User guide](docs/USER_GUIDE.md) · [실사용 점검표 / Field checks](docs/FIELD_TEST_0.4.0.md)

## 0.4.0 변경 / Changes

- **TikTok 제한 지원:** 설치 패키지 `com.ss.android.ugc.trill`의 추천 피드에서 읽을 수 있는 비율 진행값으로 일반 영상 반복을 판단합니다. TikTok은 처음에는 선택OFF, 반복2회입니다. 진행정보 없는 영상의 시간제·광고·LIVE·사진·긴 영상 필터는 지원하지 않습니다. / **Limited TikTok support:** normalized progress in the recommendation feed, deselected by default with2 plays. No clockless timer, ad, LIVE, photo or long-video filtering for TikTok.
- **여러 앱 동시 적용:** 기존 `dual_mode` 선택을 보존합니다. 기본OFF는 활성 창 하나,ON은 선택되어 안전하게 보이는1·2·3개 앱을 각각 처리합니다. 앱별 설정·카운트·실패·플로팅을 분리하고 입력은 하나씩 실행합니다. 강제 초점 전환이나 강제 재생은 하지 않습니다. / **Multiple apps:** preserve the existing preference, defaultOFF; ON follows one, two or three eligible visible hosts with separate state and serialized input, without forcing focus or playback.
- **Instagram 시간제:** 진행정보 없는 일반 영상만2–60초, 미설정 기본3초. 판별2초가 총시간에 포함됩니다. 기존에 저장한 유효 시간·ON/OFF는 유지합니다. / **Instagram timer:**2–60s, default3 when unset; the2s qualification is included, and valid saved settings remain.
- **광고 대기 시간:** Instagram 광고 옵션과 별도로 넘기기 전0.0–9.9초를0.1초씩 설정합니다. 기본0.0초는 안전 확인 후 즉시이며 기능OFF가 아닙니다. 양수 대기는 같은 광고의 식별 근거가 있을 때만 작동합니다. / **Ad delay:** separate pre-skip setting0.0–9.9s in0.1s steps, default0.0 after safety checks. Positive delay requires identifiable ad-page continuity.
- **일반 시작 인식 보완:** 초단위 카운터의 시작 허용 상한을1.3초로 조정하고 영상 길이10% 제한을 유지합니다. 전환·탐색·가림 보호는 유지하며 모든 카운트0 초기화 원인을 해결한 것은 아닙니다. / **Start tolerance:** ordinary second-based counting allows up to1.3s, still capped at10% of duration; guards remain and unrelated resets are not declared fixed.

TikTok46.7.3의6페이지 관측은 진행값4개·숨겨진 진행값1개·리워드 홍보1개를 구분한 **읽기 관측**입니다. 새 제품의 TikTok 자동 넘김·일반10연속·세 앱 동시30회 성공은 아직 실폰에서 검증하지 않았습니다. [근거와 한계](docs/TIKTOK_FEASIBILITY_2026-08-29.md).

The six-page TikTok46.7.3 survey found four moving ranges, one hidden range and one reward promotion. This is **read-only evidence**, not new-product automatic advancement, ten consecutive TikTok successes or thirty three-host transitions.

## 설치와 시작 / Install and start

Android8.0(API26)+. 소셜 앱 자체의 설치·OS 지원과 다중 창 재생 가능성은 별도입니다. 기존 앱을 삭제하지 않고 같은 서명의 새 APK를 덮어 설치하면 설정을 유지합니다. 인앱 업데이트는 Android의 설치 확인을 직접 거칩니다. [호환성](docs/COMPATIBILITY.md).

Android8.0/API26+. Host installation, OS support and multi-window playback are separate. Install the same-signed update over the existing app to retain settings; Android installation confirmation is manual.

1. 전체 실행OFF 상태에서 **사용할 앱 / Apps to use**를 선택합니다. TikTok의 지원 패키지가 설치되어 있어야 TikTok을 선택할 수 있습니다. / Select installed supported hosts with executionOFF.
2. **앱별 설정 / Settings for each app**에서 각 앱의 반복 횟수와 플로팅 방식을 정하고 적용합니다. 예:YouTube2,Instagram1,TikTok2. 편집 탭은 실행 앱 선택과 다릅니다. / Apply each host's count and floating tap mode; editor tabs do not choose runnable hosts.
3. Instagram 광고·시간제·사진과 YouTube 라이브·두 앱 긴 영상 옵션은 필요한 것만 켭니다. TikTok에는 지원하지 않는 옵션을 표시하지 않습니다. / Enable only desired supported filters; unsupported TikTok controls are hidden.
4. **사용 준비 / Setup**에서 접근성을 직접 켭니다. 플로팅이 필요할 때만 다른 앱 위 표시를 허용하고 배터리 제한 없음 안내를 확인합니다. / Manually connect accessibility, optional overlay permission and battery guidance.
5. 활성 창만 처리하려면 여러 앱 모드OFF. 다른 앱 옆의 쇼츠 또는 여러 소셜 창을 처리하려면 **여러 앱 동시 적용 / Process multiple apps**를ON으로 합니다. / Use multiple-app mode for eligible visible hosts beside other apps.
6. 하단 전체 실행을ON으로 하고 실제 재생과 상태를 확인합니다. X는 해당 앱만 일시정지, 하단/빠른 설정OFF는 모두 중지합니다. / Turn on execution and check playback; X pauses one host, master/tileOFF stops all.

**일반 사용에는 USB·ADB·무선 디버깅·루팅이 필요하지 않습니다.** 화면 분석을 꺼도 접근성은 기본 감지·넘김에 필요합니다. 서비스가 끊긴 경우 Android 설정에서 이 앱의 접근성을OFF→ON하고 돌아와 전체 실행을 직접 켜세요. 앱은 권한을 몰래 승인하지 않습니다.

**Normal use requires no USB, ADB, wireless debugging or root.** Accessibility is necessary even with visual analysisOFF. If disconnected, reconnect this service in Android Settings and explicitly restart execution.

## 숫자의 의미 / Counts and independent rules

횟수는 첫 재생을 포함한 총 횟수입니다.2회는 `1/2 → 2/2 → 다음`입니다. 중간에 켜면 다음 시작점을 기다릴 수 있습니다. 입력0–99,화살표 조절,플로팅0…기준 순환 또는0↔기준 전환을 지원합니다. 미완성·잘못된 입력은 저장하지 않습니다.

Counts include the first play:2 means `1/2 → 2/2 → next`. Starting mid-video may wait for a new beginning. Use typed0–99 values, arrows and rotary0…N or0↔N floating taps; invalid drafts do not replace settings.

| 기능 / Rule | 기본·범위 / Default and range | 반복0회 / At zero plays |
| --- | --- | --- |
| 일반 반복 / Ordinary repeat |0–99회 / plays | 중지 / Stops |
| YouTube·Instagram 긴 영상 / Long videos |OFF,확인된 총길이60초,1–3600초 / Known total duration | 독립 / Independent |
| Instagram 시간제 / Clockless timer |OFF,미설정3초,2–60초 / Default3s when unset | 중지 / Stops |
| Instagram 광고 / Ads |OFF,추가대기0.0초,0.0–9.9초 / Delay0.0–9.9s | 독립 / Independent |
| Instagram 사진 / Photos |OFF,통째·한 장 각각3초,0–10초 / Whole/each | 독립 / Independent |
| YouTube 라이브 미리보기 / Live previews |OFF,0초,0–60초 | 독립 / Independent |

**전체 실행OFF는 모든 자동 동작을 멈춥니다.** 긴 영상 기준은 대기 시간이 아니라 확인된 총길이입니다. 시간제는 완주 횟수 추정이 아닙니다. 광고·라이브·사진의0초는 안전 확인 후 즉시이며,시간제의 최소2초와 구분합니다.

**MasterOFF stops every automatic action.** Long-video filtering uses known total duration, not a wait. Clockless timing is not a completion count. Zero-second ad/live/photo delays mean immediate after safety checks, unlike the clockless2s minimum.

## 한계와 복구 / Limits and recovery

댓글·메뉴·키보드·잠금·PiP·가려짐·앱 자체 정지·불명확한 전환에서는 대기 또는 안전정지합니다. 숨겨진 앱에 입력하지 않고 재생을 강제로 켜지 않습니다. TikTok의 진행값이 없거나 지원하지 않는 콘텐츠면 기다립니다. 넘긴 목적지를 확정할 수 없으면 차단될 수 있으며 화면을 확인한 뒤 **전체OFF→ON**으로 새로 시작하세요. 자동 재시도나 반복 스와이프로 확인을 우회하지 않습니다.

Comments, menus, keyboards, lock, PiP, obstruction, host pauses or ambiguous transitions can cause waits/stops. Hidden hosts receive no input. Unsupported TikTok content waits; an unconfirmed destination can require inspecting the screen and toggling **masterOFF→ON**. No blind retry bypasses confirmation.

화면 분석은 계속 선택형 실험 기능·API34+·별도 동의이며,여러 대상 창이 보일 때 비활성입니다. 별도 오디오·영상 주기 실험은 제품 기능이 아닙니다. [디버그 대장](docs/DEBUG_LOG.md),[현장 점검표](docs/FIELD_TEST_0.4.0.md),[검증 원장](docs/VERIFICATION.md)에 미확인 범위를 구분합니다.

Visual assistance remains experimental,opt-in,API34+ and separately consented, inactive with multiple visible hosts. Unwired audio/visual experiments are not product features. Device-only and unrun cases remain explicit in the linked records.

## 언어·개인정보·업데이트 / Language, privacy and updates

시스템 첫 언어가한국어이면한국어,그 밖에는영어입니다. 표시 번역은 모든 호스트 감지 언어 지원을 뜻하지 않습니다. 설정은 로컬 저장하며 영상·계정·시청이력·원시 화면을 업로드하지 않습니다. 인터넷은 고정 Public GitHub 업데이트 조회·다운로드에만 사용합니다. 백그라운드 감시·몰래 설치는 없습니다.

The first system language selects Korean if Korean,otherwise English. UI translation does not guarantee host detection in every language. Preferences stay local; videos,accounts,viewing history and raw screens are not uploaded. Internet access is limited to this fixed Public GitHub update source,without background monitoring or silent installation.

공개 prerelease도 확인 대상에 포함하되 draft는 제외합니다. 더 높은versionCode,OS,메타데이터,크기,SHA256,패키지와 기존 전체 서명 집합을 검사합니다. 설치 시 전체 실행을OFF로 두고 다른 설정을 보존합니다. [업데이트 계약](docs/UPDATE_DELIVERY_PLAN.md).

Public prereleases are eligible;drafts are excluded. Higher versionCode,OS,metadata,size,SHA256,package and signer-set checks are required. Installation stops execution without deleting other settings.

## 개발·문서 / Development and documentation

Java/native Android,minSdk26,compile/target35,JDK17+,Gradle8.9/AGP8.7.3. [빌드 안내](docs/RELEASE_BUILD.md)에 따라 개발 debug와 배포용 non-debuggable release를 구분합니다. 서명 키·암호를 저장소에 넣지 마세요.

Java/native Android with the versions above. Keep development debug and non-debuggable release builds separate;never commit signing secrets.

```powershell
.\scripts\verify.ps1 -BuildType release
.\scripts\verify-release-safety.ps1 -Apk app/build/outputs/apk/release/app-release.apk -DebugApk app/build/outputs/apk/debug/app-debug.apk
.\scripts\prepare-release.ps1 -Apk app/build/outputs/apk/release/app-release.apk
```

- [제품 기준 / Product contract](docs/PRODUCT_SPEC.md) · [사용 설명서 / User guide](docs/USER_GUIDE.md)
- [0.4.0 릴리스 / Release record](docs/releases/v0.4.0.md) · [실사용 점검 / Field checks](docs/FIELD_TEST_0.4.0.md)
- [TikTok 계획·구현 상태 / Plan and implementation status](docs/TIKTOK_MULTI_APP_PLAN.md) · [관측 근거 / Observation](docs/TIKTOK_FEASIBILITY_2026-08-29.md)
- [여러 창 계약 / Multi-window scope](docs/SPLIT_SCREEN_PLAN.md) · [시간제 / Timer](docs/TIMED_FALLBACK.md)
- [호환성 / Compatibility](docs/COMPATIBILITY.md) · [언어 / Localization](docs/LOCALIZATION.md)
- [검증 / Verification](docs/VERIFICATION.md) · [누적이력 / Changelog](docs/CHANGELOG.md) · [인수인계·전체 색인 / Handover and index](HANDOVER.md)
- 이전 공개 기록 / Historical releases: [0.3.0](docs/releases/v0.3.0.md),[0.2.9](docs/releases/v0.2.9.md),[0.2.8](docs/releases/v0.2.8.md)

## 원본 출처 표시 요청 / Attribution request

YouTube·Instagram·TikTok과 무관한 개인 프로젝트입니다. **별도의 오픈소스 라이선스는 아직 지정하지 않았습니다.**

An independent project,not affiliated with YouTube, Instagram or TikTok. **No separate open-source license has been designated yet.**

수정·파생 버전을 공유하거나 배포할 때 README 또는 앱 소개에 **ShortsLoop**와 [원본 저장소](https://github.com/fullmetalsonic/shorts-loop)를 표시해 주세요. 자체 변경을 구분하고 원작자가 제작·보증한 것처럼 표시하지 말아 주세요.

When sharing a modified or derived version,please credit **ShortsLoop** and the [original repository](https://github.com/fullmetalsonic/shorts-loop). Distinguish your changes and do not imply original-author endorsement.

예 / Example: `Based on ShortsLoop — https://github.com/fullmetalsonic/shorts-loop (modified by [your name])`.

이 문구는 출처 표시 요청이며 별도의 라이선스나 추가 이용 권한 부여를 대신하지 않습니다.<br>
This is an attribution request,not a substitute for a separate license or additional grant of usage rights.
