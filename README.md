# 쇼츠 자동 넘김 · ShortsLoop 0.5.0

YouTube Shorts·Instagram Reels·TikTok 추천 영상을 설정에 따라 자동으로 넘기는 Android 앱입니다. 앱별 반복·광고·사진·시간 규칙, 반투명 플로팅, 빠른 설정 토글과 선택형 여러 앱 처리를 제공합니다. 소셜 앱을 개조하지 않습니다.

An Android auto-scroll app for YouTube Shorts, Instagram Reels and supported TikTok recommendation content, with per-app rules, translucent controls, Quick Settings and optional multi-window processing. Host apps are not modified.

[최신 APK / Latest release](https://github.com/fullmetalsonic/shorts-loop/releases/latest) · [앱별 설정 설명서 / Per-app guide](docs/APP_SETTINGS_0.5.0.md) · [0.5.0 검증·릴리스 / Release evidence](docs/releases/v0.5.0.md)

## 0.5.0 변경 / Changes

- **앱별 설정 화면:** 홈에서 YouTube·Instagram·TikTok 버튼을 누르면 해당 앱 설정만 표시합니다. 앱 사용 선택도 그 화면에 있으며 실행·플로팅·권한·업데이트는 공통입니다. / **Per-app pages:** choose a host button; host selection and its rules stay together, with shared controls outside.
- **TikTok 인식 보완:** SurfaceView/TextureView 재생 구조를 구분해 읽습니다. 0.4.0에서 확인한 SurfaceView 누락을 수정했으며, 진행정보가 없는 영상은 명확히 식별된 일반 영상만 선택형 시간제를 사용할 수 있습니다. / **TikTok detection:** fixes the observed renderer omission and adds an opt-in timer only for positively identified clockless videos.
- **TikTok 특수 규칙:** 광고 대기0.0–9.9초(0.1초씩), 사진 전체/한 장0–10초(각 기본3초), 시간제2–60초(기본3초), 실제 총길이 기준 긴 영상 필터를 추가했습니다. 모두 기본OFF입니다. / **Optional rules:** ad delay, whole/each photo, clockless timeout and known-duration filtering; all defaultOFF.
- **안전 확인:** 광고 전용 넘김OFF일 때도 적격 일반·사진 규칙은 사용할 수 있습니다. 이전 게시물의 시간/카운트가 새 항목에 이어지지 않도록 원천을 재검증합니다. 미확정 이동은 반복 스와이프로 우회하지 않습니다. / **Safety:** independent normal/photo rules can remain active with forced-ad skippingOFF; stale source evidence is discarded and unconfirmed moves do not trigger blind retries.

새0.5.0 실폰 자동 넘김은 **미실행 / NOT RUN**입니다. PC 빌드·단위·OS별 UI 검사와 실제 소셜 앱 연속시험은 다릅니다. 0.4.0의26피드 관측·수동 사진 이동은 새 자동 성공으로 세지 않습니다. [검증 원장](docs/VERIFICATION.md), [실사용 점검표](docs/FIELD_TEST_0.5.0.md).

New-build physical automation is **NOT RUN**. Build/unit/emulator UI checks are not social-app endurance passes. Prior observations/manual navigation are not new-build automatic successes.

## 설치·시작 / Install and start

Android8.0/API26+. 기존 앱을 삭제하지 않고 같은 서명의 새 APK를 덮어 설치하면 설정을 유지합니다. 인앱 업데이트도 Android의 설치 확인을 직접 거칩니다. **일반 사용에 USB·ADB·무선 디버깅·루팅은 필요하지 않습니다.**

Android8.0/API26+. Install the same-signed update over the existing app to retain settings. Installation confirmation is manual. **Normal use requires no USB,ADB,wireless debugging or root.**

1. 전체 실행OFF 상태에서 홈의 앱 버튼을 누릅니다. / With masterOFF,open a host's settings.
2. **이 앱 자동 넘김 사용**을 켜고 반복 횟수를 입력·완료/적용합니다. 예:YouTube2,Instagram1,TikTok2. / Enable that host and apply its repeat count.
3. 원하는 광고·사진·시간제·긴 영상 옵션만 켭니다. 각 앱의 설정은 서로 독립입니다. / Enable only desired rules; each host keeps separate settings.
4. 홈 **사용 준비**에서 접근성을 직접 연결합니다. 플로팅을 쓸 때만 다른 앱 위 표시 권한이 필요합니다. 배터리 제한 없음 안내도 확인합니다. / Connect accessibility manually,optional overlay permission and battery guidance.
5. 분할된 대상 창을 처리하려면 **여러 앱 동시 적용**을 켭니다. 기본OFF는 활성 창 하나만 처리합니다. 강제 재생/초점 왕복은 하지 않습니다. / Multiple-app mode follows eligible visible hosts without forced playback/focus.
6. 하단 **전체 자동 넘김 실행**을 켭니다. 플로팅X는 해당 앱만 일시정지, 전체/빠른 설정OFF는 모두 중지합니다. / Start master execution; X pauses one host,master/tileOFF stops all.

## 숫자의 의미 / Rule meanings

횟수는 첫 재생을 포함합니다.2회는 `1/2 → 2/2 → 다음`. 중간에 켜면 다음 시작점을 기다릴 수 있습니다. 숫자 입력·화살표와 플로팅0…N순환 또는0↔N전환을 지원합니다. 미완성·잘못된 숫자는 저장하지 않습니다.

Counts include the first play; starting midway may wait for a new beginning. Use typed values/arrows and rotary or0↔N floating taps. Invalid drafts never replace settings.

| 기능 / Rule | 대상·기본·범위 / Hosts,default,range | 반복0 / Count0 |
| --- | --- | --- |
| 일반 반복 / Repeat | 세 앱,0–99회 / All three,0–99 plays | 중지 / Stops |
| 긴 영상 / Long filter | 세 앱,OFF,총길이60초,1–3600초 / All three,known total duration | 독립 / Independent |
| 시간제 / Clockless timeout | Instagram·TikTok,OFF,3초,2–60초 | 중지 / Stops |
| 광고 / Ads | Instagram·TikTok,OFF,0.0초,0.0–9.9초·0.1단위 | 독립 / Independent |
| 사진 / Photos | Instagram·TikTok,OFF,전체/한 장 각3초,0–10초 | 독립 / Independent |
| 라이브 미리보기 / Live preview | YouTube만 / only,OFF,0초,0–60초 | 독립 / Independent |

**전체OFF는 모든 자동 동작을 중지합니다.** 광고·사진·라이브의0초는 안전 확인 후 즉시이지 기능OFF가 아닙니다. 시간제의 판별2초는 설정 총시간에 포함됩니다. 긴 영상 기준은 기다리는 시간이 아니라 확인된 전체 길이입니다. TikTok 비율0–10000으로 길이를 추정하지 않습니다.

**MasterOFF stops everything.** Zero ad/photo/live delay means immediate after safety checks,not disabled. Timer qualification is included in the selected total. Long filtering uses a real total clock,never a duration guessed from TikTok's normalized range.

## 한계·복구 / Limits and recovery

TikTok은 `com.ss.android.ugc.trill`의 관측된 추천 피드 구조만 지원합니다. TikTok LIVE·화면 분석·다른 지역 패키지·Lite·복제 앱은 미지원입니다. 실제 총길이를 제공하지 않으면 긴 영상 필터는 적용되지 않습니다. 사진 번호를 읽지 못할 때 통째 넘김은 사용자가 해당 fallback을 켠 경우에만 적용됩니다. 점 표시 광고에는 사진 가로 입력을 하지 않습니다.

TikTok support is limited to the observed recommendation structures of that package. LIVE,visual assistance,other variants/Lite/clones are unsupported. Unknown duration cannot qualify for long filtering; unreadable photo indices use whole-post fallback only when selected. Dot ads do not receive photo gestures.

댓글·메뉴·키보드·잠금·PiP·가림·앱 정지·불명확한 전환에서는 대기/안전정지합니다. 화면과 설정을 확인한 뒤 **전체OFF→ON**으로 새로 시작하세요. 접근성 연결이 끊기면 Android 설정에서 접근성을OFF→ON한 후 직접 실행을 켭니다. 세 앱의 동시 재생·3분할 제공 여부는 기기/OS/호스트에 달려 있습니다.

Comments,menus,keyboards,lock,PiP,obstruction,pauses and ambiguous transitions can stop automation. Inspect the screen before masterOFF→ON. Reconnect accessibility manually if disconnected. Three-window layouts and simultaneous host playback depend on the device/OS/apps.

시스템 첫 언어가 한국어이면한국어,아니면영어입니다. 설정은 로컬 저장하며 영상·계정·시청이력·원시 화면을 업로드하지 않습니다. 인터넷은 고정 Public GitHub 업데이트 조회/다운로드에만 사용합니다. 크기·SHA256·패키지·전체 서명·OS·높은versionCode를 검증하고 몰래 설치하지 않습니다.

The first system language selects Korean or otherwise English. No viewing/account/screen data is uploaded; internet access is limited to fixed Public GitHub updates with artifact validation and manual installation.

## 개발·문서 / Development and documentation

Java/native Android,minSdk26,compile/target35,JDK17+,Gradle8.9/AGP8.7.3. 배포는 기존 서명의 non-debuggable release이며 개인키·암호는 공개하지 않습니다. / Distribution uses the existing signer and a non-debuggable release; never publish signing secrets.

- [사용법 / User guide](docs/USER_GUIDE.md) · [앱별 상세 / Per-app details](docs/APP_SETTINGS_0.5.0.md)
- [화면·사용성 검토 / UI and usability audit](docs/UI_AUDIT_0.5.0.md)
- [제품 기준 / Contract](docs/PRODUCT_SPEC.md) · [호환성 / Compatibility](docs/COMPATIBILITY.md)
- [빌드 / Build](docs/RELEASE_BUILD.md) · [업데이트 / Updates](docs/UPDATE_DELIVERY_PLAN.md)
- [0.5.0 릴리스 / Release](docs/releases/v0.5.0.md) · [검증 / Verification](docs/VERIFICATION.md) · [현장 점검 / Field checks](docs/FIELD_TEST_0.5.0.md)
- [누적이력 / Changelog](docs/CHANGELOG.md) · [디버그 / Debug ledger](docs/DEBUG_LOG.md) · [인수인계·색인 / Handover](HANDOVER.md)
- [TikTok 계획 상태 / Plan](docs/TIKTOK_MULTI_APP_PLAN.md) · [0.4.0 실패 진단 / Diagnosis](docs/TIKTOK_DEVICE_DIAGNOSIS_0.4.0.md)
- 이전 공개 기록 / Historical: [0.4.0](docs/releases/v0.4.0.md),[0.3.0](docs/releases/v0.3.0.md),[0.2.9](docs/releases/v0.2.9.md)

## 원본 출처 표시 요청 / Attribution request

YouTube·Instagram·TikTok과 무관한 개인 프로젝트입니다. **별도의 오픈소스 라이선스는 아직 지정하지 않았습니다.**

An independent project,not affiliated with YouTube, Instagram or TikTok. **No separate open-source license has been designated yet.**

수정·파생 버전을 공유하거나 배포할 때 README 또는 앱 소개에 **ShortsLoop**와 [원본 저장소](https://github.com/fullmetalsonic/shorts-loop)를 표시해 주세요. 자체 변경을 구분하고 원작자가 제작·보증한 것처럼 표시하지 말아 주세요.

When sharing a modified or derived version,please credit **ShortsLoop** and the [original repository](https://github.com/fullmetalsonic/shorts-loop). Distinguish your changes and do not imply original-author endorsement.

예 / Example: `Based on ShortsLoop — https://github.com/fullmetalsonic/shorts-loop (modified by [your name])`.

이 문구는 출처 표시 요청이며 별도의 라이선스나 추가 이용 권한 부여를 대신하지 않습니다.<br>
This is an attribution request,not a substitute for a separate license or additional grant of usage rights.
