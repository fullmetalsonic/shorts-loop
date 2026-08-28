# 통합 업데이트 계획 · 2026-08-28

## 최신 0.2.7/code29 · 정식 배포 / Stable distribution

기존 패키지·서명·설치하한API26·업데이트 메타데이터 형식은 유지한다. 플로팅 표시만 수정한 동일APK726467bytes,SHA256 `809CD1EF1287209E23A31896B00FEFF9585511319939FC8113CBC2B1876DAF1A`를 배포한다. GitHub 시험판 표시는 해제하되 선택형 화면 분석은 실험 기능으로 유지한다. 게시·CI·익명 다운로드 동일성 상태는 [0.2.7 릴리스 기록](releases/v0.2.7.md)에 누적한다.

EN: Stable0.2.7/code29 retains the package,signer,minSdk26 and update-metadata contract. Publish the verified726467-byte APK with the SHA256 above,not a CI-resigned binary. GitHub pre-release is false;optional visual assistance remains experimental. Publication,CI and anonymous-download parity are tracked in the release record.

## 이전 0.2.6/code28 · Public 시험판 검증 완료 / Previous published pre-release

**0.2.6/code28 공개 시험판(pre-release)을 게시하고 공개 파일 검증까지 완료했다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,로컬lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 is published as a public pre-release,and public artifact verification is complete.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;local lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

**공개 검증:** 제품 커밋·태그 `8dbcce3a5cd0cfa2931461773e58e12330de14b4` / `v0.2.6`. [GitHub CI33141470669](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33141470669)는SUCCESS이며,내려받은 보고서32suites·468tests·실패0·오류0·건너뜀0을 확인했다. CI lint는0오류/2경고로 로컬0오류/3경고와 구분한다. Actions의 Node20/setup-java4 사용중단 예고 경고는 비차단 유지보수 항목이다.

2026-08-28 13:21:42KST에 게시했으며,13:22:04.764KST 익명 검증 완료 시 [공개 릴리스](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.6)의 Public·draft=false·pre-release=true와 페이지HTTP200을 확인했다. APK746246bytes·SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`,SHA256텍스트96bytes,업데이트JSON287bytes 모두 고정 산출물과 크기·SHA256이 일치했고 GitHub assets의 세 digest도 일치했다. 설치 APK도 같은 해시이며 제품 바이너리는 변경하지 않았다. 후속 문서는 별도 커밋이며 제품 태그는 변경하지 않는다.

게시·CI·익명파일동일성 확인은 완료했다. 추가 기기E2E는 수행하지 않았으며 과거 후보 수치와 최신 공개 code28 증거를 구분한다.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증 후 기존 Public 저장소에v0.2.6/code28 pre-release를 게시했으며 CI·공개 다운로드 동일성도 확인했다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. After the YouTube retest and impact-scope checks passed,v0.2.6/code28 was published as a pre-release in the existing Public repository. CI and public-download parity were verified.

검증 중 실패가 발생하면 **실패 확인→재현→직접 원인·영향 범위 분석→필요한 범위 수정→재시험** 순서를 반복한다. 기능·상태 전환·검증의 의존관계를 논리적으로 연결하되 별도 그래프 프레임워크를 설치하거나 새 제품 기능을 추가하지 않는다. 수정된 산출물의 근거와 변경하지 않은 경로의 기존 근거를 구분해 관리한다.

[현재 동작 계약](PRODUCT_SPEC.md) · [D-035 원인·예방](DEBUG_LOG.md) · [최신 검증](VERIFICATION.md)

## 과거 code26 · 지정시험 성공 후 후속실패 / Historical candidate

**과거0.2.6/code26은 실폰 후속 실패로 게시 보류된 미배포 후보였다.** 빌드·454 JUnit(실패0)·정적 가드 PASS, lint0오류/기존3경고. 12:33 동일 APK의 Android API26/33/34 계측209/209/208개 PASS,12:36 휴대폰 설치·전체 기존 설정 직접 비교 보존·접근성 연결·설치 APK 해시 일치 PASS. 12:38:20 YouTube 공식 시험은 요청10/확인10(긴 영상9+라이브1)과 전후 화면의 서로 다른 영상 확인으로 PASS했다. 12:39:22 별도 일반1/1 전환1회도 화면 쌍으로 확인했다. 그러나 후속 연속 실행 중 요청20/확인19에서 같은59초 길이·pager index 부재로 안전정지했다. 해당 실패 요청에는 전후 화면 쌍이 없어 실제 다음 영상 이동 여부는 미확정이다. Instagram은12:43:56~12:45:31.831(96.0초) 별도 시험에서 요청10/확인10(일반3·긴 영상4·시간제10초2·광고1),수동0·실패/복구0으로 PASS했다. **두 앱의 지정10회 PASS가 유튜브 후속 실패를 덮지 않으며 제품 완료·배포 준비 완료가 아니다.**

**Historical0.2.6/code26 remained unpublished after a subsequent device failure blocked its release.** Build,454 JUnit tests with zero failures,static guards and209/209/208 exact-APK API26/33/34 checks passed; lint has0 errors and3 existing warnings. Installation preserved all compared preferences and accessibility binding,and matched the APK hash. The12:38:20 YouTube run passed10 requests/10 confirmed distinct transitions:9 long-video and1 live. A separate12:39:22 ordinary1/1 transition also passed screenshot-pair review. Further continuation then safety-stopped at20 requests/19 confirmations when both durations were59 seconds and pager indices were unavailable. No pre/post screenshot pair exists for that failed request,so actual movement is unproven. A separate96.0-second Instagram run at12:43:56–12:45:31.831 passed10 requests/10 confirmations:3 ordinary,4 long-video,2 ten-second clockless and1 ad,with no manual swipes,failures or recoveries. The two designated ten-transition PASS results do not override the later YouTube failure or establish release readiness.

YouTube의 별도 RAM 메타데이터 키 경로는 **다른 키 AND (요청 후 같은 창·pager의 최신 실제 index 변화 OR 다른 유효 총길이) AND 300ms 이상 안정 AND 최신 실제 전진 재생**을 모두 요구한다. 요청 시 키 출처를 고정해 메타데이터의 등장·소실을 다른 출처의 키와 비교하지 않는다. 부분 메타데이터 소실로 키만 달라져도 이동으로 인정하지 않는다. 일반 반복 identity는 바꾸지 않는다. 메타데이터 키를 쓰지 않는 기존 확인 경로는 안정된 다른 identity 또는 최신 동일 pager 이동+다른 안정된 총길이+전진 근거를 유지한다. 길이 단독은 확인 근거가 아니며, 메타데이터 경로에서 같은 길이이고 pager index도 없으면 실제 이동했더라도 안전정지할 수 있다. 긴 영상 확인4.5초 실패는 일반 복구나 추가 스와이프로 우회하지 않는다.

The supplemental YouTube RAM-metadata path requires a different key AND either request-fresh same-window/pager index movement or a different valid duration,then at least300ms of stability and current forward playback. The identity source is fixed at request time; appearing,missing or partially missing metadata alone cannot confirm movement. Ordinary repeat identity is unchanged. Non-metadata confirmation retains stable changed identity or corroborated fresh pager movement with changed stable duration and forward progress. Duration alone is insufficient. Same-duration metadata pages without pager indices may still safety-stop after real movement. Long-video4.5-second timeouts never use ordinary recovery or retry swipes.

고정 APK: **757038bytes**, SHA256 `82CE7C221C1BF3E6DA8F86F9D487F9685D89DFB22A38D24F60B77F447519E926`. [검증 원장](VERIFICATION.md), [원인·재발방지](DEBUG_LOG.md).

연속 시험은 반복1·긴 영상ON/기준30초·광고/라이브ON·Instagram 시간제10초로 수행했다. 종료 후 플로팅X로 실행을 중지했고 blocked=false를 확인했다.12:46 인앱 숫자 입력으로 긴 영상 기준30→60초를 복원하고 UI·런타임에서 확인했다. 최종 상태는 **전체 실행OFF,반복1,긴 영상ON/60초,광고ON,라이브ON/0초,시간제ON/10초,화면 분석OFF**다. 제품의 신규 기본값OFF/60초를 바꾼 것이 아니라 기존 옵션은 보존했다. 드문 일반 timeout의 실제 발생·새 시작점 복구,최종 전체 화면 시각/사용성 감사,공개 CI·Release·익명 다운로드 동일성은 완료로 표시하지 않는다. 유튜브 후속 확인 실패가 남아 게시 보류를 유지한다.

Instagram 공식 시험은12:43:56~12:45:31.831,총96.0초이며 기준 요청/확인19→29에서 **요청10/확인10 PASS**다. 구성은 일반3·긴 영상4·진행정보 없는10초 시간제2·광고1,수동 이동0·실패0·복구0이다. 전후0~10 화면을 육안 대조했으며8번 캡처는 광고→일반 전환 중이고9번은 안정된 페이지였다. 목표 뒤 추가6회는 이10회 결과에 합산하지 않는다.

The formal Instagram run lasted96.0 seconds at12:43:56–12:45:31.831,advancing the request/confirmation baseline19→29:10/10 PASS. It comprised3 ordinary,4 long-video,2 ten-second clockless and1 ad transition,with0 manual swipes,failures or recoveries. Screenshots0–10 were visually reviewed;capture8 shows the outgoing-ad/incoming-ordinary gesture and9 a settled page. Six later transitions are excluded from this ten-transition result.

다음 안전한 조사 방향은 `CollectionItemInfo` 또는 pager 스크롤 위치가 독립적인 페이지 이동 근거를 제공하는지 **읽기 전용으로 관측**하는 것이다. 이번 상태 정리에서는 추가 구현·공개를 하지 않는다. 기존 확인 조건을 제거하거나 서로 겹치지 않는 제목 전용→음원 전용 메타데이터를 곧바로 다른 영상으로 인정하지 않는다.

The next safe investigation is read-only observation of CollectionItemInfo or pager scroll position for independent transition evidence. No further implementation or publication occurs in this status update. Do not drop confirmation guards or treat disjoint title-only→audio-only metadata as proof of another video.

**이전 후보는 별도 기록이다.** code23은12:12 실제62→93초 영상 이동 후 요청1/확인0으로 실패했고, code24는12:17 같은 창·영역·인식·안전 조건에서도 공통 텍스트 identity가 같음을 재현했다. code25도12:21~12:22 실제93→57초 이동 후 요청/현재 index가 모두−1이고 공통 identity가 같아 실패했다. code23/24/25는 실폰FAIL·미배포이며 PC·계측PASS가 이를 덮지 않는다. code22의 YouTube2회는 기능 추가로 중단한 과거 관측이며 수동180초 영상 이동1회는 제외했다. 어느 후보의 관측도 code26의10회에 합산하지 않는다.

Earlier code23/24/25 candidates failed physical confirmation and were not published,despite PC/emulator passes. Code23 actually moved62→93 seconds but confirmed0 of1 requests; code24 reproduced identical shared-text identities; code25 moved93→57 seconds but both pager indices were−1. Code22 stopped after two automatic transitions for feature integration,excluding one manual180-second skip. No historical transitions count toward code26.

Instagram10개 별도 시험과 시험값 복원을 완료했다. 다음 단계는 유튜브 후속 동일 길이 전환 확인 실패의 읽기 전용 원인 관측과 추가 조치 판단이다. 이후 독립 리뷰·문서 정합성·민감정보 점검을 통과해야 Public 게시·CI·공개 내려받기 해시 비교로 진행한다. 현재 공개판은0.2.5/code21이며0.2.6 링크와 메타데이터를 게시 완료로 간주하지 않는다.

## 과거 code23 · PC/설치PASS 이후 실폰FAIL·미배포 / Historical candidate,device FAIL

아래는12:12실폰FAIL 이전 체크포인트다. UI·설치검증은전체자동전환PASS가아니며최신code28검증으로재사용하지않는다.

code23은 새 시작점 복구와 긴 영상 건너뛰기(기본OFF·총길이기준60초·1~3600초)를 통합한 미게시 후보다. 기존패키지/minSdk26/서명 유지와 업데이트검증계약을 변경하지 않는다. 설치 시 전체실행만OFF로 하고 긴 영상옵션·기준을 포함한 다른설정을 보존한다. 긴 영상은 반복0과독립이며 전체OFF는중지하고 길이불명·정지·불안전한창을우회하지않는다. [제품 계약](PRODUCT_SPEC.md), [사용법](USER_GUIDE.md).

최종빌드·418제품시험/실패0·정적가드PASS,12:10 같은APK API26/33/34 계측163/163/162와 폰설치·prefs·접근성bound·해시일치PASS. 고정APK **757601bytes**,SHA256 `FC866F0459CD3536114758DB277F0FCD0EF84CFA443E9C8817B448D6ED704B7F`. lint0오류/기존3경고는최종보고서대조중이다. 실제YouTube10/Instagram10·조건별실폰·GitHub CI/Release/익명다운로드 비교는아직미실행이며공개완료로표현하지않는다. [검증](VERIFICATION.md), [0.2.6 릴리스](releases/v0.2.6.md).

EN: Unpublished code23 preserves signer,package,minSdk26 and updater validation while integrating recovery and optional long-video filtering. Build,418 tests,static guards,163/163/162 exact-APK emulator checks and installation parity passed;the757601-byte artifact has the SHA-256 above. Lint0/3 awaits report comparison. Actual10+10 and publicCI/release/download checks remain pending; no publication success is claimed.

## 과거 0.2.6/code22 후보 / Historical candidate

과거시작점복구후보0.2.6/code22의APK711847bytes/SHA256 `a0916cd7935336d0527e0cb19edeb3574a0f1406c508d75395cf063a7b7f3fce`,383시험·API26/33/34 계측109/109/108·설치/설정/해시PASS를보존한다. YouTube자동2회확인후180초영상1개수동이동은제외했고기능추가로중단했다.10연속PASS가아니며Instagram10·공개검증은미실행이다. code23수치로재사용하지않는다.

Historical code22 passed383 tests,109/109/108 emulator checks and phone installation parity. Its two automatic YouTube transitions and one excluded manual180-second skip ended for feature integration,not a ten-transition PASS. Instagram10 and publication were not run; these results do not verify code23.

## 0.2.5 공개 이력 / Historical published delivery

2026-08-28 10:57 KST에 **0.2.5/code21을 기존 Public 저장소의 시험판으로 공개**했다. [v0.2.5 Release](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.5), main·태그 기준 코드 커밋 `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5`, draft=false/prerelease=true. [CI33134278633](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33134278633) 성공: 내려받은 보고서356시험·실패0·오류0·건너뜀0, CI lint0오류/2경고(로컬 기존3경고와 구분). 익명 릴리스HTTP200 및 배포파일3개(APK709703bytes/SHA96bytes/JSON287bytes)의 원본 대비 크기·SHA-256 일치를 확인했다. 이후 문서 정리는 제품 소스·APK를 바꾸지 않는다.

**Version0.2.5/code21 was published as a Public prerelease at10:57 KST on2026-08-28**, from code commit `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5` and tagv0.2.5. CI33134278633 succeeded with356 tests and zero failures/errors/skips; CI lint reported0 errors/2 warnings, separately from3 local warnings. Anonymous release access returnedHTTP200 and all three uploaded assets matched their originals byte-for-byte and by SHA-256. Subsequent documentation changes do not alter product source or the APK.

공개 후10:58~10:59, 설치된code21 앱의 업데이트 확인을 실제 실행해 새 업데이트 없음 안내·조회 시각 갱신을 확인했다. 실제 Public HTTPS 조회와 현재 버전 안내는PASS이며 새 버전 다운로드/설치 재시험은 아니다. 업데이트 자동조회ON·전체실행OFF를 유지했다.

At10:58–10:59, the installed code21 app successfully checked the public release over HTTPS, displayed no applicable newer update and updated its attempt timestamp. This verifies current-version checking, not a new-version download or installation. Automatic checking remained ON and overall execution OFF.

v2서명·기존 인증서·메타데이터21/0.2.5/min26·10:51 설치본 동일성PASS. 공개 후보169개 독립 민감정보 감사·224개 로컬 링크(누락0)도PASS. 서명 인증서와 파일별 검증 범위는 [검증 원장](VERIFICATION.md)을 따른다. 메일 발송 없음.

EN: APK signature/certificate continuity, metadata and installed parity passed, as did the169-file publication audit and224 local links. See the verification ledger for exact values and scope. No email was sent.

## 0.2.5 체크포인트 이력 / Historical checkpoint

**최신0.2.5/code21은 PC 검증·휴대폰 설치·Public 공개 완료**다.709703bytes, SHA256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`로 고정했다.356시험·빌드·lint0오류/기존3경고·LIVE_TREE_LIFECYCLE 포함 정적가드 PASS.10:39 동일 APK API26/33/34 계측74/74/73,10:40 code21 설치·접근성bound·prefs·설치해시 PASS. 최종 조회 원복 독립리뷰 추가P1/P2 없음.

**Current0.2.5/code21 is PC-verified, phone-installed and publicly released.** Its709703-byte frozen APK matches the SHA-256 above. Final build/356 tests/lint/static guards,74/74/73 exact-APK emulator checks and phone binding/preferences/hash passed. Final review reported no additionalP1/P2 findings.

**code21 공식 YouTube10연속 PASS**:10:40:41→10:47:45.702,424.5초,확인0→10/세대6,수동 입력·앱 전환 없음,라이브0. 전환1~10 화면을 모두 육안 확인해 서로 다른 정상 전체화면 영상·플로팅과 올바른 이동을 확인했다. code20 외부 중단/개별 라이브/목표 이후 추가 관측을 합산하지 않는다. 약10:50 플로팅X 전체OFF 확인,10:51 업데이트 자동조회 선택ON 복원·UI/저장값 확인. code21 라이브 개별 재시험·20연속은 미완료이고 D-021은 미수정이다. 공개·CI·익명 배포파일·해시 일치 확인은 완료했으며 상세 결과는 위 공개 기록을 따른다.

**Code21 passed the official10-transition YouTube run** in424.5 seconds(10:40:41→10:47:45.702),confirmation0→10,generation6,no manual input/app changes,zero live skips. All10 transition screens were visually reviewed. Code20 trials and post-target observations are not added. FloatingX stopped execution around10:50;the automatic-update preference was restored to ON and checked in UI/storage at10:51. Individual code21 live retests,20-transition completion and D-021 remain separate limitations. Publication, CI and anonymous asset/hash checks passed as recorded above.

## 과거 code20·18 후보 / Historical candidates

**0.2.5/code20은 당시 확정된 미게시 후보**다. 고정 APK725487bytes, SHA256 `EF59D4E40E192A89D5B207741B03CCE08FA11AC1079DC61C7776C19A1D3D60EB`. cleanup 포함 최종356제품시험·빌드·lint0오류/기존3경고,10:30 동일 후보 API26/33/34 계측74/74/73 PASS. 휴대폰 설치 준비·설정 보존·설치본 해시도 PASS했다. 이전 cleanup 전 시험을 최종 결과로 대신한 것이 아니다.

**Unpublished0.2.5/code20** was frozen at725487bytes with the SHA-256 above. Its final cleanup-inclusive356-test run, build, lint(0 errors/3 existing warnings),74/74/73 exact-candidate emulator checks and phone installation/preferences/hash passed.

실기기5초 지연은10:30:29 대기→34 요청→35 확인1·일반25초 영상 진입,0초/반복0회는10:34:31 준비→33 요청→34 확인2→3,라이브OFF는10:33:33~39 요청2/확인2 유지로 각각PASS했다. 메인 화면에 권한 경고가 없음을 육안 확인했다. 새10연속은10:35:28 확인기준3/세대30에서 시작해2회 확인 후10:37:17 외부 앱 전환으로 중단됐으며, code20의 Public·CI·익명 재다운로드는 당시 미수행으로 보존한다.

Device5-second delay, immediate skipping at zero normal plays, and live-OFF waiting each passed; the main UI showed no permission warning. The10-transition run began separately at10:35:28 from confirmation baseline3/generation30 and ended externally at10:37:17 after two confirmations. Code20 was not published; those publication checks were unperformed at that checkpoint.

code19의 라이브ON 조건부 조회는 중간안이다. code20은 라이브OFF에서도 일반→라이브 전환을 확인하고 pending 중 조회형태를 유지하도록 옵션과 조회 조건을 분리했다. 전체실행ON·YouTube선택·전면YouTube에서 확장하고 다른 앱/전체OFF는 기존 조회,모드 변경 후 이전 root 폐기,failClosed/onDestroy 기본 조회 원복이다. 새 권한은 없다.

Code19 remains an intermediate live-ON-only proposal. Code20 separates retrieval from skipping, preserving recognition and tree shape with live OFF, discarding old roots on mode changes and restoring defaults on stop/destruction. No new permission.

code18은738945bytes/SHA256 `941532517058CB8553EFE5DB34ED1762426C468B2D66F88A567CE788E306C54D`의 보존 실패 후보다.352시험·설치는 통과했지만 조회 플래그 차이로 라이브 인식FAIL했다. code16/17의 업데이터·UI 결과와 code18 실패를 code20에 혼합하지 않는다. D-033의 확인된 원인은 실기기 재시험PASS지만 D-021 일반 반복 경계와 연속 안정성은 별도다. [검증 기록](VERIFICATION.md).

Code18's retained artifact passed automated/install checks but failed actual recognition. It is not the code20 release candidate. Code16/17/18 histories are preserved; the corrected D-033 cause does not resolve D-021 or prove endurance.

## 제품 범위

OS별 기능 호환성, GitHub 새 버전 인앱 안내·설치 버튼, 중요도·빈도에 따른 메뉴 순서를 함께 개선한다. 기기 시험과 문서·보안 검사를 거쳐 기존 Public 저장소에 동일 산출물을 게시한다. INTERNET는 업데이트 조회·다운로드에만, REQUEST_INSTALL_PACKAGES는 Android 설치 확인창을 여는 데 사용한다. OS 설치 허용과 최종 설치 확인은 사용자가 직접 조작하며 ADB로 대신 허용하지 않는다. 메일 발송은 포함하지 않는다.

YouTube 쇼츠의 라이브 미리보기는 별도 기본OFF 옵션이며 대기0~60초/기본0초(바로)를 제공한다. 일반 반복0회와 독립, 전체 실행OFF는 광고·라이브까지 모두 정지한다. 전용 구조·전면창·안정 페이지와 요청 후 전환 확인을 요구하며 방송 참여 버튼·제목/CTA/시청자 수·OCR을 사용하지 않는다. [라이브 계약](LIVE_SKIP.md).

Live previews inside YouTube Shorts are separately opt-in(defaultOFF), with a0–60-second delay(default0, immediate after safety checks). The option is independent of zero normal plays; main OFF stops ads and live too. Dedicated structure, foreground-window/page checks and confirmed transitions are required; no join-button click, text-based identity or OCR.

## 화면

- 일상 순서: 반복 횟수 → 긴 영상 건너뛰기 → 시간제 → 광고 → YouTube 라이브 → 플로팅 → 적용 앱 → 사용 준비 → 업데이트·앱 정보 → 실험 기능 → 도움말.
- 전체 실행은 기존 하단 고정으로 언제든 끄고 켤 수 있다.
- 필수권한이 부족할 때만 상단 준비 바로가기. 새 버전이 있을 때만 상단 작은 배지/버튼. 업데이트가 없으면 주요 설정을 밀어내지 않는다.
- 앱 진입 시 최대 하루 한 번 조용히 조회, 수동 확인·다운로드·취소·진행률·실패·재시도 제공. 백그라운드 서비스나 알림 권한은 추가하지 않는다.

## 네트워크·업데이트 계약

고정 Public 저장소 fullmetalsonic/shorts-loop의 릴리스 목록만 무인증 조회한다. 현재 공개판이 시험판이므로 draft는 제외하되 prerelease를 포함한다. 릴리스의 shorts-loop-update.json을 사용하며 schema/packageName/versionCode/versionName/minSdk/apkName/apkSize/sha256를 실제 APK와 비교한다. 버전코드가 높고 기기 OS에 맞는 것만 제공한다. Github 태그만 있는 커밋이나 임의 APK를 업데이트로 취급하지 않는다.

HTTPS·정확한호스트/저장소 경로 허용목록·매리다이렉트 검사·응답/파일 크기제한·연결/읽기/전체시간제한을 적용한다. 토큰·계정·시청내용 업로드 없음. GitHub는 접속 IP 등 일반 네트워크 요청 정보를 볼 수 있다. APK는 내부 임시폴더만 사용하고 취소/실패 시 부분파일을 정리한다.

다운로드 후 크기/SHA256/패키지/버전/최소OS/현재설치앱과동일서명집합 확인. OS 설치기로 보내기 직전 재확인한다. 해시별 읽기 전용 설치 사본을 만들어 후속 다운로드가 설치창의 파일을 바꾸지 못하게 한다. 손상 사본은 검증된 임시 사본으로 교체한다. SHA/인증서대조를 APK 전체 암호학적서명검증으로 과장하지 않으며 Android 설치검증을 우회하지 않는다. 설치직전 전체실행/플로팅은멈추고 다른설정은보존, 취소/복귀에도자동재시작하지 않는다.

## 시험·게시 순서

1. 제품 정책/파서/네트워크경계/손상·다운그레이드 차단 시험, 빌드/lint/구형OS 재시험. 서명 불일치 거부는 소스 리뷰와 실제 다른 키 APK 시험을 구분해 기록한다.
2. 기존 폰의 버전·서명·설정을 기록하고 낮은 code의 업데이트 시험 시작 APK를 설치한다. 계측시험의 전송 대체는 시험 APK에만 존재하며 제품에는 외부 시험 진입점이나 검증 우회를 두지 않는다.
3. 실제 GitHub HTTPS/리다이렉트는 기존 공개 자산으로 확인한다. 대상 앱을 중단시키는 계측은 최종 설치 전에 끝내거나 에뮬레이터에서 수행한다. 아직 없는 새 공개 릴리스를 게시 전에 다운로드했다고 기록하지 않는다.
4. Android 설치 허용·확인과 실제 버전 상승·설정 보존을 확인한다. 핵심 업데이터의 code15→16 OS설치, 후속code17 UI 체크포인트, code18 ADB설치/실제 라이브FAIL, 새code20 검증은 구분한다. ADB설치 PASS를 새 인앱 설치 경로 PASS로 바꾸지 않는다. 권한 복귀·취소·재시도 중 실행하지 못한 항목은 미실행으로 남긴다.
5. 최종 설치 후에는 비파괴 확인만 수행한다. 설치 APK 해시·버전·설정 및 접근성 enabled/bound/runtime 상태를 대조하고 실제 화면을 확인한다. 이 단계에서 대상 앱을 종료시키는 계측을 다시 실행하지 않는다.
6. 실제 제품 조회 조건으로 라이브 인식·0초/5초 이동·연속 전환을 시험한다. 일반→라이브/라이브→일반/연속 라이브, OFF·앱 전환·잠금·중복 제스처 차단을 구분한다. code16의2/10 실패를 새 성공에 합산하지 않으며 D-021 일반 반복 경계는 별도로 기록한다.
7. 독립리뷰·한영문서·민감정보검사 후 동일 최종 APK와 메타데이터/SHA를 Public 게시한다. CI·익명 재다운로드·폰 설치본 해시·버전 일치를 확인한다. 실제 미완료 항목을 성공으로 바꾸지 않는다.

계측 후 접근성 enabled가 남아 있어도 서비스가 실제로 bound 상태인 것은 아니다. D-031처럼 계측에 의한 강제 종료가 확인되면 원인을 구분한 뒤 수동 OFF→ON 재연결과 상태 복구를 확인한다. 이유 없이 권한을 반복 설정하도록 안내하지 않으며, 계측 후유증을 제품 코드 크래시로 단정하지 않는다.

검증 한계: 동일 서명 APK의 정상 설치와 서명 비교 소스 리뷰는 수행했다. **다른 키로 서명한 실물 APK의 설치 전 거부 시험은 미실행**이며 자동시험 PASS로 포함하지 않는다. code20의 개별 라이브 시험과 공개code21의10연속·배포 검증은 각각 통과했으며 남은 개별 라이브 재시험·20연속 상태는 [검증 기록](VERIFICATION.md)을 따른다. code18 합성 계측은 실제 YouTube 노드 노출을 검증하지 못했으므로 기기에서 같은 조회 조건을 확인하는 단계를 생략하지 않는다.

## 근거

- [GitHub Releases API](https://docs.github.com/en/rest/releases/releases): 공개 무인증조회·릴리스목록/시험판·자산 URL.
- [Android 설치 요청 허용](https://developer.android.com/reference/android/content/pm/PackageManager#canRequestPackageInstalls()): API26부터 앱별 설치허용을 확인.
- [Android SigningInfo](https://developer.android.com/reference/android/content/pm/SigningInfo): API28 이상서명조회,구형은GET_SIGNATURES호환분기.

진행·증거는 [검증 기록](VERIFICATION.md), 오류는 [디버그 대장](DEBUG_LOG.md), 결정은 [인수인계](../HANDOVER.md)에 누적한다. 이 문서는 완료 선언이 아니다.
