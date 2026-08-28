# YouTube 쇼츠 광고 · 구현 준비 / Implementation preparation

## 상태 / Status

**구현 준비 중, 자동 넘김 미지원.** 실제 YouTube Shorts 광고의 접근성 구조가 아직 확보되지 않았다. 이번 공개 범위는 준비 소스·시험·문서뿐이다. 배포된0.5.0 APK·태그·인앱 업데이트는 변경하지 않고 새 설치용 APK를 게시하지 않는다. `main`의 준비 소스와 배포 APK를 동일 산출물로 취급하지 않는다.

**Preparation only, not functional ad skipping.** Publication covers preparation source, tests and documentation only. The distributed0.5.0 APK, tag and in-app update stay unchanged; no new installable APK is published. Development `main` and the distributed APK are different source/artifact checkpoints.

## 목표 기능 / Intended behavior

- YouTube 개별 설정에 광고 넘김 ON/OFF, 기본OFF.
- 대기0.0–9.9초, 0.1초 단위·최초0.0. 반복0과 독립, 전체OFF는 모두 중지.
- Instagram/TikTok과 별도 저장. 기존 설정·서명·권한 보존.
- 쇼츠 피드 광고만 대상. 일반 영상의 광고나 영상에 삽입된 협찬 메시지는 대상이 아니다.

Per-YouTube opt-in ad skipping with independent integer-tenths delay0–99, defaultOFF/0. The rule is independent of ordinary count0 but always stops with masterOFF. Preserve existing settings and permissions; exclude ordinary-player ads and creator sponsorships.

## 확인한 근거와 부족한 점 / Evidence and gap

1. [YouTube 공식 Shorts 광고 안내](https://support.google.com/youtube/answer/12929256?co=YOUTUBE._YTVideoType%3Dshorts&hl=ko)는 쇼츠 사이의 건너뛸 수 있는 영상·이미지 광고를 설명한다. Android 접근성 노드 구조까지 제공하지는 않는다.
2. [GKD Android 관측 규칙](https://github.com/AIsouler/GKD_subscription/blob/main/src/apps/com.google.android.youtube.ts)의 일반 광고 skip 버튼은 Shorts 전용 감지 근거가 아니다.
3. [Morphe Android 리소스 참조](https://github.com/MorpheApp/morphe-patches/blob/38c13fa35e05e4df87d28445428e1fadf1bc5ea9/patches/src/main/kotlin/app/morphe/patches/youtube/ad/HideAdsPatch.kt)에 `ad_attribution`이 있으나, 실제 Shorts 현재 페이지 안에 접근성 노드로 노출되는지는 확인되지 않았다.
4. [광고/고지 분류](https://github.com/MorpheApp/morphe-patches/blob/38c13fa35e05e4df87d28445428e1fadf1bc5ea9/extensions/youtube/src/main/java/app/morphe/extension/youtube/patches/components/AdsFilter.java)의 `reel_player_disclosure.e` 등은 유료 프로모션 고지이다. 이를 광고 넘김 신호로 사용하지 않는다.

Official information confirms Shorts ads exist, not their accessibility structure. General skip buttons and native resource references do not establish current Shorts-page exposure. Paid-promotion disclosures must not be confused with feed ads. No third-party code is copied into the product.

## 준비한 변경 / Prepared changes

- YouTube 전용 `host.youtube.skip_ads` / `host.youtube.ad_delay_tenths` 저장 구조, 광고 패널 재사용과 숫자 초안/복원 시험을 준비한다. 공개로 검증된 기존 IG/TT 키·값을 보존한다.
- YouTube 패널은 현재 미지원임을 표시하고 실행 토글을 비활성화한다. 숫자 저장은 준비용일 뿐 자동 넘김을 시작하지 않는다.
- 별도 `YouTubeAdTransition`은 같은 창·범위·pager, 정확히 다음 장 번호, 다른 페이지 원천과 안정 구간을 요구하는 **미연결 준비 모듈**이다. 일반 영상 목적지는 실제 전진 재생도 필요하다. 이 모듈의 합성 시험은 실제 광고 넘김 성공이 아니다.
- YouTubeReader와 HostPlaybackSession의 실제 감지/요청 경로에는 연결하지 않았다. 임의 광고 ID나 제목·댓글의 단어로 감지를 대신하지 않는다.

Prepared settings/UI retain independent values; YouTube's activation remains disabled with an unsupported notice. The unconnected transition helper is synthetic preparation, not a product automation pass. Reader/service action paths are unchanged.

## 초기 로컬 검증 / Initial local verification

- 변경 소스·단위시험·Android 화면 시험 소스 컴파일 **PASS** (`compileDebugJavaWithJavac`, `compileDebugUnitTestJavaWithJavac`, `compileDebugAndroidTestJavaWithJavac`). 마지막 수정 이후 재실행하여 최신 입력과 일치를 확인했다.
- 직접 JUnit **705개 PASS**, 실패0: 기존685개와 독립 설정10개·미연결 전환 모듈10개. 전환 모듈 시험은 합성 입력 시험이며 제품의 실제 광고 인식·넘김 성공이 아니다. 별도 비제품 VisualSequence 시험20개는 기존 범위대로 제외한다.
- 한국어/영어 **399키·108상태**, 사진 안전 연결 정적 검사, diff 형식 검사 **PASS**.
- 독립 소스 리뷰: 이 준비 범위의 미해결 P1/P2 **0건**. 미지원 토글 차단, 저장값과 실제 동작의 분리, 기존 두 앱 키·연결 유지, 전환 모듈의 제품 참조 없음 확인.
- **NOT RUN:** 새 APK 생성·서명·설치, lint, Android 화면 시험 실행·육안 감사, 실폰 자동 넘김·영상 회귀. 화면 시험은 컴파일만 했으며 실행 성공으로 세지 않는다. 감지 미구현이므로 전체 기능 요구 충족은 **보류**이다.
- 새 권한·네트워크 대상·런타임 감지/제스처 경로 변경 없음. GitHub·메일 발송 없음.

Changed-source compilation and705 direct JUnit tests pass, including20 new preparation tests; these do not establish functional ad skipping. Localization399/108, photo safety checks and scoped independent review pass. No new APK, lint, native UI execution/visual audit, physical automation, installation or publication. Functional acceptance is blocked on actual ad evidence.

## 준비 소스 게시 검증 / Source-publication checks

기존 Public 저장소의 `main`에 준비 소스·시험·문서를 게시하는 범위다. v0.5.0 태그·Release 첨부파일·업데이트 피드는 수정하지 않는다. CI가 생성하는 검사용 APK는 설치용 릴리스가 아니며 별도 배포하지 않는다.

게시 전 최신 입력으로 소스·단위·Android 시험 소스를 재컴파일하고 직접 JUnit705개를 다시 통과했다. Debug lint는 오류0·경고21개(OldTargetApi1, ObsoleteSdkInt1, UnusedResources19)로 통과했다. 다국어399키/108상태·사진 안전·diff 검사 PASS. 공개 대상의 비밀키·토큰·내부 경로 및 비공개 파일 경로 검사 PASS. 실제 광고 자동 전환과 Android UI 시험 실행은 여전히 NOT RUN이다.

Publication is source-only on the existing Public repository. Do not modify v0.5.0 tags, assets or update metadata; CI verification builds are not distributed releases. Recompiled source/tests and705 JUnit tests pass. Debug lint has0 errors and21 disclosed warnings. Localization, photo-safety, diff and scoped sensitive-content checks pass. Physical ad automation and native UI execution remain unrun.

### 게시 완료 / Published source checkpoint

- Public `main` 준비 소스: [`91791f2eebed1de624497b1d0f0c107a83fd0094`](https://github.com/fullmetalsonic/shorts-loop/commit/91791f2eebed1de624497b1d0f0c107a83fd0094).
- [GitHub Actions33218554151](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33218554151) **SUCCESS**: debug/release 검사용 빌드·단위시험·lint, 배포 안전·다국어 검사. Actions 런타임/setup-java 지원 종료 안내 경고는 남아 있으며 제품 오류가 아니다. CI의 검사용 산출물을 새 설치용 Release로 올리지 않았다.
- 비로그인 Public 저장소·정확한 소스 커밋·미지원 안내 문서 접근 **PASS**. 문서 상대 링크248개 **PASS**.
- 최신 정식 Release는 계속 **v0.5.0**, 태그 원천은 `cdd94aeb9e50dcbd03deb76bc06c159ed7556806`이다. APK·업데이트JSON·SHA256파일을 비로그인으로 다시 내려받아 로컬 기존 파일의 바이트·SHA256·GitHub digest 일치 **PASS**.
- 유지된 APK: **778294 bytes**, SHA256 `08B0BE6A34F666E6EA3BC21950D152B7D14B239ED10F47299E382E7B281EEDEA`. 업데이트 메타데이터는 계속0.5.0/code34. 새 태그·Release·설치·메일 발송 없음.

Source91791f2 is public and CI33218554151 succeeds. Anonymous source/document access and248 relative documentation links pass. The stable0.5.0 tag, three release files and update metadata are unchanged; anonymous byte/hash/digest parity passes. No new installable release, installation or email. Source publication does not complete YouTube ad support.

## 다음에 필요한 시험 / Required next evidence

프리미엄이 적용되지 않는 YouTube의 실제 Shorts 광고1–2개와 앞뒤 일반 쇼츠를 같은 연결에서 비교한다. 광고 전용 표시·page/pager 소속·페이지 번호·창 범위·정지/댓글/외부 링크 상태를 확인한다. 광고 문구/계정/원시 트리는 공개하지 않는다.

근거가 확보되면 감지 → 지연 → 최신 원천 재검증 → 안전한 세로 이동 → 다음 페이지 확인을 연결한다. 광고0초/0.1초/9.9초, OFF·반복0·이동 취소, 광고→일반·연속 광고, 협찬 고지/일반 영상 광고의 오검출 차단과 기존 두 앱 회귀를 검증한다. 모든 실제 자동 전환은 현재 **NOT RUN**이다.

Compare1–2 physical non-Premium Shorts ads with adjacent ordinary Shorts, then connect evidence-backed detection, delay, fresh validation, scoped navigation and strict confirmation. Test opt-out/count0, cancellation, consecutive ads and sponsorship false positives. Physical automation remains **NOT RUN**.

[인수인계](../HANDOVER.md) · [검증](VERIFICATION.md) · [공개0.5.0 사용법](APP_SETTINGS_0.5.0.md)
