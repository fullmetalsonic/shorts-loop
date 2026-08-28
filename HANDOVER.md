# ShortsLoop · 기술 인수인계 / Technical handover

## 최신 검증 체크포인트 / Latest verification checkpoint

**현재 후보는0.2.5/code21, PC 검증·실기기 설치 PASS, 미게시**다. 고정 APK709703bytes, SHA256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`.356JUnit·빌드·lint0오류/기존3경고·정적가드(LIVE_TREE_LIFECYCLE 포함),10:39 동일 APK의 API26/33/34 계측74/74/73 PASS.10:40 휴대폰code21 설치·접근성 bound·재생 설정 보존·설치본 해시 PASS. 최종 조회 원복 독립리뷰에서 추가P1/P2 없음.

**Current0.2.5/code21 is PC-verified and phone-installed, but unpublished.** Its frozen709703-byte APK matches the SHA-256 above. Build,356 tests, lint(0 errors/3 existing warnings), static guards and74/74/73 exact-APK emulator checks passed; phone version/accessibility binding/preferences/hash checks passed at10:40. Final cleanup review found no additionalP1/P2 findings.

**code21 YouTube10연속 자동 전환 PASS**:10:40:41→10:47:45.702,424.5초,확인기준0→10,세대6 유지. 수동 입력·앱 전환 없이 일반 영상10개를 자동으로 넘겼고 라이브 이동은0회다. 전환1~10 화면을 모두 육안 대조해 서로 다른 정상 전체화면 영상·플로팅과 올바른 이동 방향을 확인했다. 목표 이후 관측분은 공식10회에 더하지 않는다. 약10:50 플로팅X로 전체실행OFF 확인. 10:51 업데이트 자동조회 선택을ON으로 복원하고 UI와 저장값을 확인했다.

code21 라이브0초/5초/OFF 개별 재시험은 NOT RUN으로 아래code20 근거와 구분한다. D-021은 미수정,20연속 검증도 미완료다. 현재미게시이며 공개·CI·익명 다운로드/해시 확인은 후속 수행한다.

**Code21 passed10 consecutive YouTube auto-transitions** from10:40:41 to10:47:45.702(424.5 seconds),confirmation0→10,generation6,without manual input or app changes. All10 transition screens were visually checked: distinct normal full-screen videos,visible floating control and correct movement. No live preview was skipped. Observations after the target are excluded from the official count. FloatingX stopped overall execution around10:50; the automatic-update preference was restored to ON and checked in UI/storage at10:51. Code21 individual live retests,20 consecutive transitions and publication checks remain incomplete; D-021 is not fixed.

이전 개별 라이브 검증 후보는 **0.2.5/code20, 빌드·핵심 라이브 실기기 시험 PASS, 미게시**다. cleanup을 포함한356제품시험·빌드·lint0오류/기존3경고, 동일 후보 API26/33/34 에뮬레이터74/74/73검사를 통과했다. 휴대폰 설치 준비·설정 보존·설치본 해시도 일치했다. 실제 라이브5초 지연·0초/반복0회 독립 동작·라이브OFF 대기를 각각 확인했다. 새10연속 시험은10:35:28에 시작해2회 확인 후10:37:17 외부 앱 전환으로 중단됐다. 제품 감지FAIL이나10PASS가 아니다. [검증 기록](docs/VERIFICATION.md), [D-033](docs/DEBUG_LOG.md).

The device-tested **0.2.5/code20** passed its final build,356 product tests, lint(0 errors/3 existing warnings),74/74/73 exact-candidate emulator checks and phone installation/preferences/hash checks. Actual5-second delay, immediate skipping at zero normal plays, and live-OFF waiting passed separately. A new10-transition run began at10:35:28 and ended externally at10:37:17 after two confirmations. The candidate is unpublished.

## code20 검증 산출물과 동작 / Verified code20 artifact and behavior

고정 APK: **725487bytes**, SHA256 `EF59D4E40E192A89D5B207741B03CCE08FA11AC1079DC61C7776C19A1D3D60EB`. 이 code20 산출물과 code18 실패 후보를 혼합하지 않는다. 최종 게시·CI·공개 재다운로드는 아직 수행하지 않았으며 [릴리스](docs/releases/v0.2.5.md)에 후속 기록한다.

Frozen code20 APK: **725487bytes**, with the SHA-256 above matching the installed artifact. It is not interchangeable with code18 or a subsequent rebuild; release/CI/public-download verification is pending.

code20은 전체실행ON·YouTube선택·전면YouTube에서 라이브 옵션과 무관하게 확장 조회한다. 라이브OFF도 일반→라이브 진입을 인식하고 넘김만 차단한다. Instagram/다른 앱/전체OFF는 기존 조회, 모드 변경 시 이전 root 폐기, failClosed/onDestroy에서 명시 원복이다. 새 권한은 없다.

Code20 expands retrieval during overall execution in selected foreground YouTube, independently of the skip option, preserving live recognition when skipping is OFF. Other hosts/overallOFF use default retrieval; mode changes discard the old root and failClosed/onDestroy restore defaults. No new permission.

code19는 라이브ON만 확장 조회하던 중간안이다. 후속 리뷰에서 라이브OFF의 전환 확인과 pending 중 조회형태 변경에 따른 거짓 identity 위험을 찾아 code20으로 보강했다. 초기356시험은 cleanup 전 체크포인트였고 **최종 cleanup 포함 재빌드와356시험도 PASS**했다.

Code19 remains an intermediate live-ON-only proposal. Code20 resolved the live-OFF recognition and pending tree-shape risks; the final cleanup-inclusive build and356-test rerun passed.

## 과거 체크포인트 / Historical checkpoints

- code16: Android 설치창의15→16 업데이트·재생 설정 보존·설치 해시 확인. 당시 고정 APK703134bytes, SHA256 `6FA61EA51C04AF5A8246E21183C7F4D9FDF0564FEEF5794553BEBEF7C1F4EFE1`. 이후 같은 소스 재빌드는 ZIP 바이트가 달랐지만 내부22항목은 각각 동일했다. 이는 code18 산출물의 검증값이 아니다.
- code16의 YouTube10연속 시험은2개 확인 뒤 라이브 미리보기에서FAIL. 후속 일반→동일 라이브2회 재진입에서 전용 노드를 문구보다 먼저 관측했다. 당시에는 라이브 기능 미구현이었으며 이 실패를 새 시험과 합산하지 않는다.
- code18: 라이브 구현 후보738945bytes, SHA256 `941532517058CB8553EFE5DB34ED1762426C468B2D66F88A567CE788E306C54D`.352시험/에뮬레이터/설치는 PASS지만 조회 플래그 불일치로 실제 라이브 인식FAIL. 배포본이 아니다.
- code17: 접근성 재연결 안내 UI 후보. 당시 기기 설치 전·게시 전 체크포인트이며 현재 후보가 아니다. 과거 시험 결과는 [검증 원장](docs/VERIFICATION.md)에 보존한다.

Code16 updater checks and its failed2-of10 YouTube run, plus the code17 UI-only checkpoint, remain historical. None verifies the later code20 artifact; code18's real recognition failure is preserved separately.

## 동작 계약 / Product contract

- 일반 진행정보: 설정한 총 재생 횟수0–99. 중간 진입은 다음 처음 재생부터 카운트.
- 진행정보 없는 적격 Instagram 단일영상: 시간제 선택 시10초 기본/5–60초. 정확한 완주 횟수가 아님.
- 0회: 일반 반복·시간제·화면분석 중지. 선택 앱의 Instagram 광고·YouTube 라이브 옵션은 전체 실행ON일 때 각각 독립 적용.
- 라이브: 기본OFF·0~60초/기본0초, 전용 노드·안정된 단일 페이지·전면창 검증. 제목/CTA/시청자 수는 식별 근거가 아니며 같은 노드 재사용 시 안전 정지할 수 있음.
- 전체 실행OFF: 광고·라이브 포함 전부 중지. 플로팅은 선택 사항, 두 가지 탭 모드와 위치 저장.
- 설정 순서: 횟수→시간제→광고→YouTube 라이브→플로팅→사용 앱→사용 준비→업데이트·앱 정보→실험→도움말.
- 준비가 부족하거나 새 버전이 있을 때만 상단 안내. 전체 실행은 하단 고정.
- API26 기본 기능, API29 타일 상태줄, API33 타일 추가 요청, API34 창 화면 분석. 공식 호스트 앱의 OS 지원은 별개.
- 업데이트: 고정 GitHub 공개 릴리스/시험판, 앱 진입 시 최대 하루 한 번 선택 조회, 수동 조회·다운로드·설치. OS 확인 수동, 설치 전 실행OFF, 설정 유지.
- 인터넷은 업데이트 조회/다운로드에만 사용. 영상·계정·시청이력 수집/업로드 없음.

## 검증과 제한 / Verification and limits

code18 제품352JUnit·빌드 PASS, lint0오류/기존3경고. 동일 고정 APK로 API26/33/34 에뮬레이터74/74/73검사 PASS. 실제 휴대폰 ADB 업데이트 설치·기존 접근성의 설치 후 재연결·재생 설정 보존·설치본 해시 일치 PASS. 이는 새 권한 자동 부여나 전체 실행 자동 시작을 뜻하지 않는다. code18 라이브 인식은 FAIL이며 정식0/5초 실제 넘김·10연속은 NOT RUN이다. code20의 새 검증은 위 체크포인트와 검증 원장에 별도 기록하며 에뮬레이터 계측은 소셜앱 자동 넘김 E2E가 아니다.

독립 리뷰의 최종 root 재검증, 요청 전 오래된 pager 인덱스 배제, 일반 쇼츠의 기존 null-child/600노드 수집 유지, 동일 uptime 안정 상태 보존을 수정했다. 해당 회귀는352제품시험에 포함된다. code16의274시험·47/47/46계측·36개 설치 사전검사·OS15→16 설치·기존 GitHub 자산 다운로드 결과는 과거 이력으로 별도 보존한다.

게시 전 신규 버전 응답은 테스트 APK 전용 fixture이며 실제 GitHub에서 미공개 파일을 받은 것으로 보고하지 않는다. 제품에는 테스트 후크/fixture 자산이 없다. 공개 후 익명 다운로드와 메타데이터 확인을 추가한다.

기존 D-019/D-021 간헐 중지·특수 창·호스트 UI 변경·장시간 및20회 연속 시험 미완료는 유지한다. code18은 라이브 감지를 추가하지만 일반 영상 반복 경계 문제를 고친 버전이 아니다. code16 당시17→0 관측을 code18 새 증거로 바꾸지 않으며 code18 D-021 재시험도 미완료다. VisualSequence 별도 실험20개 중18통과2실패는 제품에서 제외된다. 오디오 실험은 별도 앱이며 통합하지 않았다.

## 개발·재현 / Development

- JDK17 이상, SDK35/BuildTools35.0.0, Gradle8.9/AGP8.7.3.
- Windows 한글 경로의 Gradle test worker 제약은 `scripts/verify.ps1`의 직접 JUnit으로 검증. CI는 Linux 표준 Gradle 시험.
- 호환성 계측은 `scripts/verify-compat-emulator.ps1`로 에뮬레이터에서만 실행.
- `-PupdaterBootstrap`는 설치 시험용code15/전용 계측 runner를 선택한다. 최신 수정 대상은code21이며 code18은 보존된 이전 후보이다.
- 테스트 APK의 `final-update.apk` 자산은 로컬 시험용이며 Git/제품 APK에서 제외.
- `scripts/prepare-release.ps1 -Apk <tested.apk>`는 버전·기존서명 검증 후 APK/업데이트JSON/SHA 산출. 기존 출력 덮어쓰기 금지.
- 실제 자동넘김 관측 중 UIAutomator는 접근성 연결에 간섭하므로 사용하지 않는다. 개인 화면·로그는 비공개로 보관한다.

## 문서 색인 / Documentation index

- [한영 소개·사용법](README.md)
- [상세 사용 설명서](docs/USER_GUIDE.md)
- [제품 기준](docs/PRODUCT_SPEC.md)
- [UI·인간공학 기준](docs/UI_DESIGN.md)
- [Android 호환성](docs/COMPATIBILITY.md)
- [업데이트 전달 계약](docs/UPDATE_DELIVERY_PLAN.md)
- [검증 기록](docs/VERIFICATION.md)
- [디버그·재발방지 대장](docs/DEBUG_LOG.md)
- [누적이력](docs/CHANGELOG.md)
- [0.2.5 릴리스](docs/releases/v0.2.5.md)
- [0.2.4 릴리스](docs/releases/v0.2.4.md)
- [시간제 보조](docs/TIMED_FALLBACK.md)
- [라이브 미리보기 넘김](docs/LIVE_SKIP.md)
- [화면 분석 실험](docs/VISUAL_ASSIST_TRIAL.md)
- [오디오 실험](docs/AUDIO_PROBE_TRIAL.md)
- [Instagram 진행정보 조사](docs/INSTAGRAM_TIMING_RESEARCH_2026-08-27.md)
- [아이콘 자산](assets/icon-concepts/ADAPTIVE_ASSET.md)

## 다음 확인 / Follow-up

D-033의 code20 인식·5초/0초·OFF 시험은 통과했으나10연속은2회 확인 후 외부 앱 전환으로 중단됐다. code21의 PC·설치·일반 YouTube10연속은 PASS이며 개별 라이브 재시험·20연속은 미완료다. 업데이트 자동조회ON 복원은10:51 확인했으며 공개 검증이 다음 단계다. D-021 등 일반 영상 한계는 유지한다. 게시 단계에서 릴리스·CI·메타데이터·익명 APK·설치본 일치를 확인해야 하며 현재는 미게시다. 제조사별 절전/타일·폴더블·장시간은 남은 검증이다. 메일 발송은 하지 않았다.
