# 배포·버전 표시 전수 검토 / Release presentation audit · 0.4.0

## 현재0.4.0/code33 기준 / Current presentation contract

인앱 하단은 `ShortsLoop 0.4.0`,업데이트 카드는 설치 버전0.4.0으로 표시한다. 앱 전체에 시험판·정식판 등급을 붙이지 않는다. 선택형 화면 분석의 실험적 안내와 TikTok 미지원/실폰 검증 필요 안내는 정확한 기능 한계이므로 유지한다.

Use neutral app/version labels. Keep experimental visual-assistance and limited/unverified TikTok disclosures;neutral wording is not universal feature verification.

README·제품/사용법·호환성·릴리스는0.4.0/code33으로 정렬했다. 배포 APK의debuggable=false·패키지·서명·revision·업데이트 메타데이터 일치와 공개3파일/CI 확인PASS는 [릴리스 원장](releases/v0.4.0.md),[검증](VERIFICATION.md)에 기록했다. 공개 버전의 실폰 자동 전환은NOT RUN으로 분리한다.

Current-version documents must align with actual non-debuggable artifact identity,metadata,CI and public assets. Final checks belong in the linked records,not assumed here.

## 이전 전수 검토 원문 / Historical presentation audits

아래 버전·PASS·공개 상태는 각 당시 기록이다. / Results below belong to their historical versions.

## 현재0.3.0/code32 · 공개 완료 감사 / Current publication audit

앱·README·설명서는0.3.0/code32를 현재 공개판으로 설명하며,인앱 버전 라벨은 중립적인 이름/설치버전 형태를 유지한다. 듀얼은 기본OFF이고,편집 탭·사용할 앱 선택·전체 실행을 구분한다. X는 해당 앱만 정지하며 전체OFF는 모두 중지한다. 과거 후보와 이전 공개0.2.9의 수치를 최신 결과로 합산하지 않는다.

최종 제품 후보의 BUILD·564JUnit·정적 가드·같은APK API26/33/34 native28,043/28,053/27,794·설치 해시를 확인했다. 로컬lint0오류/13경고,독립 검토 범위 미해결P1/P2는0건이다. 실행·듀얼ON을 유지한 전체화면→분할→전체화면 흐름은 실기기 근거가 있으나,모든 앱·예외 조합·원래 회전 방향 복귀·새 듀얼 사진은 검증 완료가 아니다. 현재 Public0.3.0/code32의 게시·CI·익명 공개 파일 동일성 검증 완료. CI lint는0오류/12경고로 로컬0오류/13경고와 구분한다. [검증 원장](VERIFICATION.md),[배포 기록](releases/v0.3.0.md).

EN: Current presentation targets0.3.0/code32 with neutral version labels and clear editor/host-selection/dual/master scopes. HostX and masterOFF are distinguished;old evidence remains historical. The final product candidate passed build,564 unit tests,static guards,28,043/28,053/27,794 same-APK native checks onAPI26/33/34 and installed-hash checks. Local lint reports0 errors/13 warnings;no unresolvedP1/P2 remains in independent review scope. Physical fullscreen→split→fullscreen evidence does not establish all-app/all-exception,return-rotation or dual-photo coverage. Public0.3.0/code32 publication,CI and anonymous-asset parity are complete. CI lint reports0errors/12warnings,distinct from local0errors/13warnings.

## 이전0.2.8 수정 결과 / Previous remediation

두 버전 라벨을 중립적인 앱명·설치버전으로 수정하고 resource·실제라벨 검사를 추가했다. non-debuggable release 구성과 기존 단일 서명·소스revision·출고거절 가드를 추가했으며 현재 사용 문서와 과거 기록을 분리했다. 실제 화면분석 실험 안내와 prerelease포함 기존 업데이트 정책은 유지했다. 빌드468JUnit·세OS 계측/19설정 덮어설치·휴대폰 문구/연결/해시 검사를 수행했고 독립 리뷰에서 P1/P2발견0건이다. [검증 수치·제약](VERIFICATION.md),[최종 배포 상태](releases/v0.2.8.md),[재현 가능한 빌드](RELEASE_BUILD.md).

EN:0.2.8 addresses label/documentation drift and release packaging with exact UI,non-debuggable,single-signer and committed-source guards. Real experimental warnings and update selection are unchanged. Build,unit,native,upgrade and scoped phone checks were run;publication details and limits are linked above.

## 원래0.2.7 검토 기록 / Original0.2.7 audit

2026-08-28 · 대상0.2.7/code29 · 제품태그5125b33·문서HEADfed435a.

**판정: 수정 필요.** 앱과 공개파일을 변경하지 않은 검토다. 앱 UI/사용법/권한안내/타일,버전표시,공개APK 빌드설정,GitHub 릴리스,README·사용설명서·제품기준·검증문서,관련 검사·배포 스크립트를 확인했다. 모든 재생 알고리즘의 재검증이나 새로운 호스트 연속시험은 아니다.

**Result: changes required.** Review only;no product or public artifact changes. Scope covers user-facing release/version labels,help/permissions/tile text,APK build settings,GitHub metadata,documentation and relevant verification/release checks. This is not a complete playback-algorithm audit or a new host endurance run.

## 확인된 문제 / Findings

1. **P2 · 배포APK가 디버깅 허용 상태.** 공개0.2.7과 설치본 모두`application-debuggable`/`DEBUGGABLE`을 확인했다. 파일명만의 문제가 아니다. Gradle에 배포용 구성/서명 연결이 없고,CI는assembleDebug·testDebugUnitTest·lintDebug,배포 스크립트는디버깅허용APK를거절하지않는다. GitHub의prerelease=false는APK설정을바꾸지않는다. 배포용debuggable=false 구성과실제업데이트호환검사가필요하다. 현재기능실패나원격침해를확인했다는뜻은아니다. [Android 공식 배포 준비](https://developer.android.com/studio/publish/preparing).
2. **P2 · 앱 전체를 시험판이라고 고정 표시.** `ui/UpdatePanel.java:20`의‘설치 버전0.2.7·공개 시험판’과`ui/SettingsScreen.java:124`의하단‘ShortsLoop0.2.7·시험판’을소스와실폰에서확인했다. GitHub는prerelease=false이며설치시점/캐시문제가아니다. 별도‘정식판’으로대체할필요없이앱이름·버전만표시하면된다.
3. **P2 · 사용자 문서의 현재/과거 혼합.** README의소개38/40는전체앱을시험판으로부르고,24/25·228은code28을현재처럼표현한다. UI_DESIGN5/7은code29를로컬후보,9는code28을최신이라고한다. USER_GUIDE21·31에도code28의‘이번버전’과이전10+10시험목표가현재처럼남아있다. 과거증거를삭제하거나code29검증으로바꾸지말고,현재사용법과버전별이력을분리해야한다.
4. **P3 · 표시/배포 회귀검사 누락.** CompatibilityInstrumentation67은업데이트패널존재만검사한다. 두버전라벨의정확한내용이나현재앱에잘못된시험판표시가없는지는검사하지않는다. prepare-release는서명·패키지·버전·크기를확인하지만디버깅허용을차단하지않는다. 기존빌드/468시험PASS는이요건을보증하지않는다.
5. **P3 · 업데이트 정책 설명 노후화.** UPDATE_DELIVERY_PLAN132의‘현재공개판이시험판이므로’는현재상태와다르다. 다만시험판도업데이트후보로포함하는것은GitHubUpdateClient44이하와UpdateClientChecks48의명시적인기존정책이다. 정식배포표시문구를수정하는일과채널정책변경을혼동하면안된다. 현재더높은시험판을받은사례는확인하지않았다.

EN: Findings are a debuggable distributed APK(P2),two hard-coded whole-app trial labels(P2),mixed current/historical documentation(P2),missing label/debuggable release guards(P3),and a stale explanation of the existing prerelease-inclusive updater policy(P3). A stable GitHub flag does not change Android build settings. No remote compromise or playback failure is inferred.

## 표시 기준 / Proposed presentation

- 앱 하단: `ShortsLoop <버전>`.
- 업데이트 메뉴: `설치 버전 <버전>`.
- 앱 전체에‘시험판/공개시험판/정식판’을붙이지않는다. 버전은BuildConfig의단일값을사용한다.
- 화면분석의‘실험기능’,추가재생가능성,정확도·지원OS·개인정보안내는유지한다.
- 과거시험판릴리스·버전별검증이력은보존하되현재설명과구분한다.

EN: Show only the app name/version or installed version. Preserve visual-assistance experimental warnings and historical release evidence. Do not globally erase every occurrence of trial/experimental wording.

## 권고 수정 범위 / Recommended next change

앱버전표시공통화·라벨검사,배포용debuggable=false빌드와차단검사,현재문서/과거이력분리,최종설치APK실제화면점검을하나의업데이트로검증한다. 기존패키지·서명신원·설정과인앱업데이트호환을보존하는방법을먼저확인한다. 키교체·기존앱삭제·새권한은추정해서진행하지않는다. 시험판업데이트채널변경은별도판단사항이다. 기존공개0.2.7파일을덮어쓰지않고더높은versionCode의새업데이트가필요하다.

EN: A follow-up should unify/test labels,add and validate a non-debuggable distribution build with release guards,separate current docs from historical records,and inspect the installed artifact. Preserve package/signing identity/settings and update compatibility;do not infer authority to rotate keys,uninstall or add permissions. Updater-channel changes are separate. Do not overwrite the existing0.2.7 release asset.

## 검토 증거와 한계 / Evidence and limits

실폰버전0.2.7/code29,패키지DEBUGGABLE,공개APK의aapt정보,GitHub최신v0.2.7·draft=false·prerelease=false,두위치실제화면을확인했다. 화면은비공개로컬진단경로에만보관한다. 독립읽기전용검토도두라벨·문서혼합·기존업데이트정책·검사누락을확인했다.

빌드/단위시험/기기연속시험은이번검토에서새로실행하지않았다. 앱설정·권한·실행상태·GitHub공개상태를변경하지않았다. 문서에기록한것은발견사항이며수정완료/재시험PASS가아니다.

EN: The installed package,public APK metadata,GitHub flags and both actual UI labels were inspected;an independent read-only review corroborated label/documentation/test gaps. No new build,unit or endurance run was performed. Product settings,permissions,execution and public release state were unchanged. These findings are not completed fixes.
