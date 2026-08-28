# 한국어·영어 안내 / Korean and English · ShortsLoop 0.4.0

## 현재0.4.0/code33 표시 / Current display

첫 시스템 언어가한국어이면한국어,그 밖에는영어인 규칙을 유지한다. TikTok 앱 선택/설정/미지원 안내·TT 라벨·진행정보 없음/정지/지원불가 상태·광고 대기0.0–9.9초 편집과 오류·Instagram 시간제2–60/기본3 안내를 두 언어로 추가했다. 여러 앱 모드는 저장키를 바꾸지 않고 표시명을 갱신했다.

The first-language policy is unchanged. Added bilingual TikTok selection/capability/status labels,TT badge,exact ad-delay editing/errors and updated Instagram timer guidance. Multi-app wording changes do not replace the saved preference key.

좁은 폭 또는 큰 글꼴에서는 세 앱 이름을 세로로 배치한다. 앱별 입력 초안·오류·저장 단위는 언어와 무관하게 분리된다. 표시 번역은 모든 TikTok지역패키지·호스트언어·영상 종류 인식 지원을 의미하지 않는다. 최종 번역/네이티브/시각 시험은 [검증](VERIFICATION.md),게시 상태는 [0.4.0 기록](releases/v0.4.0.md)을 확인한다.

Narrow/large-font app selectors stack vertically;typed drafts and validation remain host-specific. Translation does not prove host-language/variant/content recognition. See current verification and release records.

## 이전 언어·검증 원문 / Historical localization records

아래0.3.0·0.2.x 버전·수치·공개 상태는 당시 기록이며 이번 검증에 재사용하지 않는다. / Earlier version results are not new0.4.0 evidence.

대상은 **0.3.0/code32**입니다. 시스템 첫 언어에 따른 한영 선택을 유지하고 앱별 설정·듀얼·호스트별X 안내를 추가합니다. 최종 제품 후보의 빌드·단위·3개 OS 검사와 범위별 실기기 동작을 확인했으며,공개 릴리스·CI·익명 공개 파일 동일성 검증도 완료했습니다. 모든 실시간 언어 변경·모든 기기 동작을 검증했다는 뜻은 아닙니다. 현재 공개판은0.3.0/code32입니다. [검증 원장](VERIFICATION.md),[0.3.0 기록](releases/v0.3.0.md).

This guide targets **0.3.0/code32**,retaining first-system-language selection and adding per-host,dual-mode and scopedX guidance. Product build/unit/three-OS checks and scoped physical flows are verified;public release,CI and anonymous-file parity checks are complete. This is not universal real-time language-change or device coverage. Public0.3.0/code32 is current;see the linked records.

## 1. 언어를 정하는 기준 / Language selection

앱이 직접 표시하는 메뉴·도움말·플로팅·빠른 설정 상태는 **시스템 언어 목록의 첫 번째 언어**를 따릅니다. 첫 언어가 한국어(`ko`)이면 한국어, 그 밖에는 영어입니다. 별도 인앱 언어 선택이나 번역 다운로드가 필요하지 않습니다. 아래 표의 순서는 Android 시스템 언어의 우선순위입니다.

App-owned menus, help, floating controls and Quick Settings state follow the **first language in the system language list**. Korean (`ko`) selects Korean; every other first language selects English. There is no separate in-app language selector or translation download. List order below means Android system-language priority.

| 시스템 언어 순서 / System order | 앱·플로팅·실행 타일 / App, floating, runtime tile |
|---|---|
| 한국어 / Korean (`ko-KR`) | 한국어 / Korean |
| 영어 / English (`en-US`, `en-GB`) | 영어 / English |
| 일본어 / Japanese (`ja-JP`) | 영어 / English |
| 한국어 → 영어 / Korean → English | 한국어 / Korean |
| 영어 → 한국어 / English → Korean | 영어 / English |
| 일본어 → 한국어 / Japanese → Korean | 영어 / English |

**Android가 직접 표시하는 항목은 예외입니다.** 런처 앱 이름, Android 접근성 설정의 서비스 이름·설명 및 시스템 권한·설치 창은 OS의 리소스 선택·번역 규칙을 따를 수 있습니다. 예를 들어 일본어→한국어 목록에서 앱 내부는 영어여도 일부 OS 표시가 한국어일 수 있습니다. 이는 두 앱이 설치됐다는 뜻이나 감지 설정이 바뀌었다는 뜻이 아닙니다. OS별 메뉴 경로·문구도 다를 수 있습니다.

**Android-owned text is an exception.** Launcher names, accessibility-service names/descriptions and system permission/installer screens can follow Android's own resource matching and translations. With Japanese→Korean, some OS labels may be Korean while the app is English. This does not mean a duplicate app is installed or detection settings changed. OS menu paths and wording can vary.

## 2. 언어를 바꾸고 다시 사용하기 / Change language and resume

1. 편집 중인 횟수·시간이 있으면 **완료/적용**으로 확정합니다. 확정 전 임시 입력의 화면 재생성 후 보존은 보장하지 않습니다.
2. 안전하게 **전체 자동 넘김 실행 / Auto-advance**를 끕니다.
3. Android의 **설정 → 언어**에서 원하는 언어를 첫 번째로 옮깁니다. 제조사에 따라 ‘일반 관리’나 ‘시스템’ 아래에 있습니다. ShortsLoop가 시스템 언어를 대신 변경하지 않습니다.
4. ShortsLoop로 돌아와 메뉴·도움말·설치 버전 표시를 확인합니다. 예: `설치 버전 0.3.0` 또는 `Installed version 0.3.0`.
5. 선택 앱을 지원되는 전체/분할 화면으로 열고 필요한 경우 전체 실행을 다시 켭니다. 언어 변경 중 화면 전환·진행 정보 소실·접근성 재연결에 대한 기존 보호를 해제하지 않습니다. ‘재시작 필요’ 또는 안전정지 안내가 있으면 현재 화면을 확인한 뒤 전체 실행을 **OFF→ON**합니다.

1. Apply any edited count or time before leaving; uncommitted input is not guaranteed to survive screen recreation.
2. Turn **Auto-advance** off for safety.
3. In Android **Settings → Languages**, move your preferred language first. The path may be under General management or System. ShortsLoop does not change system language for you.
4. Return to ShortsLoop and check menus, help and `Installed version 0.3.0` or its Korean equivalent.
5. Open a selected host in a supported full/split layout and re-enable execution when ready. Language changes do not bypass screen-change, missing-progress or accessibility-reconnection safeguards. If restart or safety-stop guidance appears, inspect the screen and toggle main execution **OFF→ON**.

기존에 저장한 횟수·앱 선택·플로팅 위치·광고·라이브·긴 영상·시간제·화면 분석 선택은 언어 변경 때문에 초기화하지 않습니다. 단, 서비스 재연결 후 전체 실행을OFF로 두는 기존 안전 규칙은 그대로입니다. 기기의 앱별 언어 메뉴가 보이더라도 제품 화면의 기준은 위 시스템 첫 언어 규칙입니다.

Saved counts, selected hosts, floating position and feature options are not reset by language changes. The existing safety rule that leaves execution off after service reconnection still applies. Even if a device exposes per-app language controls, the product UI uses the first-system-language rule above.

## 3. 자주 쓰는 메뉴 / Common menu names

순서·기능·숫자의 의미는 두 언어에서 같습니다. / Position, behavior and numeric meanings are the same in both languages.

| 한국어 | English | 목적 / Purpose |
|---|---|---|
| 일반 영상 · 횟수로 넘김 | Ordinary videos · Play count | 총 재생 횟수 0~99 / Total plays 0–99 |
| 긴 영상 건너뛰기 | Skip long videos | 알려진 총길이 기준 / Known total-duration threshold |
| 진행 정보 없는 영상 · 시간제로 넘김 | No progress information · Timer | 지원 Instagram의 대기 시간 / Wait for eligible clockless Instagram videos |
| 광고 · 바로 넘김 | Ads · Skip immediately | 인식한 Instagram 광고 / Recognized Instagram ads |
| YouTube 라이브 · 미리보기 넘김 | YouTube live · Skip previews | 인식한 라이브 미리보기 / Recognized live previews |
| 플로팅 리모컨 | Floating control | 표시·탭 방식 / Display and tap behavior |
| 사용할 앱 | Apps to use | YouTube·Instagram 선택 / Host selection |
| 듀얼 화면 적용 | Use dual-window mode | OFF활성 창,ON보이는 선택 대상 / Active-only vs visible selected hosts |
| 앱별 설정 | Settings for each app | 편집 대상 선택 / Choose the host to edit |
| 이 앱 다시 시작 | Resume this app | 해당 호스트만 재개 / Resume one host |
| 사용 준비 | Setup | 권한·배터리·타일 / Permissions, battery and tile |
| 업데이트 · 앱 정보 | Updates · App information | 조회·다운로드·설치 / Check, download and install |
| 실험 기능 | Experimental features | 선택형 화면 분석 / Optional visual assistance |
| 전체 자동 넘김 실행 | Auto-advance | 모든 자동 동작 ON/OFF / All automation on/off |

플로팅의 `1/2`, `2/2`는 언어가 바뀌어도 해당 앱의 총 재생 횟수 표시입니다. `조건 / Filters` 등 독립 옵션 표시는 **현재 영상 종류의 판정이나 완주 횟수 자체가 아닙니다.** 복구 대기·정지 표시는 정상 재생 숫자와 구분합니다.0.3.0의 X는 표시된 호스트만 일시정지하며 전체OFF는 모두 중지합니다. 이전 단일 플로팅의 전체 종료 계약과 혼동하지 않습니다.

Floating `1/2` and `2/2` retain their host-specific play-count meaning. Independent-option labels such as `Filters` **do not classify the current content or count completed plays**. Recovery waiting/stopped states are distinct. In0.3.0,X pauses the labelled host;masterOFF stops all hosts. Do not apply the older single-control stop scope.

0회는 해당 앱의 일반 반복·시간제·화면 분석 중지입니다. 긴 영상·광고·라이브·사진은 지원 호스트의 별도 옵션이며,전체 실행OFF가 모두 중지합니다. 라이브·사진 **0초**는 인식·안전 확인 후 바로 동작이지OFF가 아닙니다. 이 의미는 번역 후에도 같습니다.

Zero plays stops that host's ordinary repetition,timer and visual assistance. Supported long-video,ad,live and photo rules are independent;masterOFF stops everything. A live/photo delay of **zero seconds** means immediate after safety checks,notOFF. Translation does not change these meanings.

## 4. 표시 언어와 영상 인식은 별개 / UI language is not detection support

표시 번역은 영상 인식 언어의 확대와 별개입니다. YouTube·Instagram 판독은 기존의 한국어·영어 텍스트,지원되는 시계 표기와 접근성 구조 중심이며 **모든 언어·지역·앱 버전의 영상·광고 인식을 보장하지 않습니다**. 일부 구조 기반 판독이 다른 언어에서도 동작할 수 있지만 전 언어 지원으로 간주하지 않습니다. 시스템 언어가 바뀌어 호스트 앱의 표기도 달라지면 기존에 읽던 진행 정보를 더 이상 읽지 못할 수 있습니다.

UI translation is separate from host-language detection. YouTube/Instagram readers focus on Korean/English text,supported clock formats and accessibility structures; **detection is not guaranteed for every language,region or host version**. Structure-based reading may work in another language without establishing full support. If system language changes host labels,previously readable progress may become unavailable.

읽을 수 없으면 기존 안전 대기·정지 계약을 따릅니다. 언어를 이유로 추측 스와이프·권한 우회·추가 화면/소리 분석을 하지 않습니다. 감지가 되지 않을 때는 전체 실행을 끄고 수동으로 넘기세요. 지원 언어로 호스트 설정을 바꾸는 것도 성공을 보장하지는 않습니다.

Unreadable screens retain existing safety waits/stops. Language never authorizes guessed swipes, permission bypasses or extra image/audio analysis. Turn execution off and advance manually when detection fails. Selecting a supported host language does not guarantee success on every screen.

## 5. 업데이트 오류와 개인정보 / Update errors and privacy

조회·다운로드·검사·설치 안내도 같은 언어를 사용합니다. 예: **업데이트 다운로드 / Download update**, **업데이트 설치 / Install update**. 시간 초과·취소·파일 무결성·서명 불일치 등 알려진 오류는 번역된 원인과 다음 행동을 표시합니다. 알 수 없는 예외는 일반 실패 안내만 보여 주고 원본 예외 메시지·내부 경로·서버 응답을 사용자 문구에 그대로 넣지 않습니다.

Checking, downloading, verification and installation guidance use the same language. Known timeout, cancellation, integrity and signer failures have translated explanations and next actions. Unknown exceptions use a generic failure message rather than exposing raw exception text, internal paths or server responses.

고정 Public GitHub 조회·다운로드, 메타데이터·크기·SHA256·패키지·버전·OS·서명 검사, 수동 설치 허용·최종 확인은 변경하지 않습니다. 번역용 새 권한·클라우드 번역·영상/계정 전송을 추가하지 않습니다.

Fixed Public GitHub lookup/download, metadata/size/SHA256/package/version/OS/signer checks and manual installation permission/confirmation are unchanged. Localization adds no permission, cloud translation or video/account upload.

## 6. 개발·검증 범위 / Implementation and verification scope

`i18n/LanguagePolicy`는 첫 언어 선택, `AppLocale`는 표시용 Context, `StatusText`는 내부 상태의 번역을 담당합니다. 업데이트 모듈은 구조화한 오류 코드를 `UpdateMessages`에서 표시 문구로 변환하며 네트워크 판독에 Android 표시 의존성을 섞지 않습니다. 기본 `res/values`는 영어, `res/values-ko`는 한국어입니다. 내부 감지 신호·노드 식별자·업데이트 주소·설정키는 번역하지 않습니다.

`LanguagePolicy` selects the first language; `AppLocale` supplies display contexts; `StatusText` renders internal status. `UpdateMessages` maps structured update failures to localized text without adding Android display dependencies to network parsing. Default resources are English and `values-ko` resources are Korean. Detection signals, node IDs, update addresses and preference keys are not translated.

확인 항목은 첫 언어 조합·리소스 키/서식·숫자 입력·오류 개인정보 차단·작은 화면과 큰 글자·플로팅/타일 갱신·언어 변경 중 안전 대기·기존 설정 보존입니다. PC/에뮬레이터 검사와 실제 소셜 앱 전환은 구분하며,후보별 최신 결과는 검증 원장을 따릅니다. 최초0.2.9 문서의 휴대폰NOT RUN/공개 대기 표기는 당시 체크포인트이며,[0.2.9 최종 기록](releases/v0.2.9.md)과 현재0.3.0 결과를 대신하지 않습니다.

Checks cover first-language combinations,resource keys/formats,input,private-error suppression,layout,floating/tile refresh,safety and preferences. PC/emulator checks remain distinct from physical transitions;use the artifact-specific verification record. The original0.2.9 guide's phoneNOT RUN/publication-pending statement was an early checkpoint,not the final0.2.9 outcome or the current0.3.0 status.

관련 문서 / Related: [README](../README.md) · [사용 설명서 / User guide](USER_GUIDE.md) · [제품 계약 / Product contract](PRODUCT_SPEC.md) · [OS 호환성 / Compatibility](COMPATIBILITY.md) · [검증 / Verification](VERIFICATION.md).
