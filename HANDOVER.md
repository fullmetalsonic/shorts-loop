# ShortsLoop · 기술 인수인계 / Technical handover

## 최신 0.2.6/code28 · 검증 완료·게시 준비 / Verified,publication pending

**0.2.6/code28은 로컬 PC·기기 검증을 마친 공개 시험판(pre-release) 게시 준비 상태이며 아직 미게시다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 has completed local PC/device verification and is ready for public pre-release delivery,but is not yet published.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증은 통과했으며,기존 Public 저장소에v0.2.6/code28 pre-release를 게시한 뒤 CI·공개 다운로드 동일성을 별도로 확인한다. 현재는 게시 전이다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. The YouTube retest and impact-scope checks passed. Planned delivery is v0.2.6/code28 as a pre-release in the existing Public repository,followed by separate CI/public-download parity checks;publication is still pending.

검증 중 실패가 발생하면 **실패 확인→재현→직접 원인·영향 범위 분석→필요한 범위 수정→재시험** 순서를 반복한다. 기능·상태 전환·검증의 의존관계를 논리적으로 연결하되 별도 그래프 프레임워크를 설치하거나 새 제품 기능을 추가하지 않는다. 수정된 산출물의 근거와 변경하지 않은 경로의 기존 근거를 구분해 관리한다.

code28 고정 APK는 **746246bytes**,SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`다. 위 PC·계측·설치·YouTube20회 결과는 이 동일 산출물에 해당한다. 시험 후 플로팅X로 실행을 중지하고 blocked=false를 확인했다.13:12 인앱 숫자 입력으로 긴 영상30→60초를 복원하고 런타임을 확인했다. 최종 상태는 **전체OFF,반복1,긴 영상ON/60초,광고ON,라이브ON/0초,시간제ON/10초,화면 분석OFF**다.

The frozen code28 APK is **746246bytes**,SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`. PC,emulator,installation and YouTube20 evidence refer to this same artifact. Execution was stopped with floatingX and blocked=false verified. At13:12 the long-video threshold was restored in-app30→60 seconds and checked in runtime. Final state:overallOFF,target1,long-videoON/60,adsON,liveON/0,timedON/10,visual assistanceOFF.

**YouTube20 실기기 기록:**13:08:46~13:11:14.291,148.6초,기준0→20·요청20/확인20. 일반4·긴 영상15·라이브1,광고/시간제0,수동0·실패0·복구0이다. 전후 화면0~20을 육안 대조했다.10번은 들어온 라이브가 다음 동작으로 이동 중인 화면이고11번에서 다른 일반 영상이 확인됐다. 현재 행의 정확한+1을 관측했다.13번 관측 중 알림 배너가 나타났으나 이후 전환은 계속됐으며 **단일 사례이지 모든 알림에 대한 보증이나 과거 실패 원인 규명은 아니다**. 목표 이후 추가3회는20회 결과에 합산하지 않는다.

**Physical YouTube20:**13:08:46–13:11:14.291,148.6 seconds,baseline0→20 and20 requests/20 confirmations. The mix was4 ordinary,15 long-video,1 live,0 ads/timed,with0 manual swipes,failures or recoveries. Screenshots0–20 were visually reviewed;10 shows the incoming live preview already moving out,and11 confirms a distinct ordinary video. Exact current-row+1 was observed. One heads-up notification at observation13 did not prevent later transitions;this single case neither guarantees all notification behavior nor identifies the original failure's cause. Three post-goal transitions are excluded.

**남은 한계:** 같은 길이의 연속 영상 쌍은 최종 실기기20회에서 나오지 않았다. 동일 길이 분기의 JUnit 검증과 실기기 일반 행+1 관측을 그 사례의 실기기 PASS로 합치지 않는다. 드문 일반 timeout의 실제 새 시작점 복구는 이번 연속 시험에서 발생하지 않았다. 좁은 플로팅의 ‘긴영상’ 첫 글자 일부 잘림은 동작에 영향 없는 경미한 알려진 문제로 남겼으며,1/1·10초 숫자는 정상이다. 전체 시각감사 PASS나 모든 기기·호스트 버전의 보증을 하지 않는다.

**Remaining limits:** no equal-duration pair occurred in the final physical20,so JUnit coverage plus ordinary row+1 observations do not constitute a physical PASS for that exact pair. Rare-timeout fresh-start recovery was not exercised by this run. Minor clipping of the first character in the floating long-video label remains a nonblocking known issue;1/1 and10-second numerals are normal. This is not an all-visual-audit PASS or a guarantee across all devices/host versions.

[현재 동작 계약](docs/PRODUCT_SPEC.md) · [D-035 원인·예방](docs/DEBUG_LOG.md) · [최신 검증](docs/VERIFICATION.md)

## 과거 code26 · 지정시험 성공 후 후속실패 / Historical candidate

**과거0.2.6/code26은 실폰 후속 실패로 게시 보류된 미배포 후보였다.** 빌드·454 JUnit(실패0)·정적 가드 PASS, lint0오류/기존3경고. 12:33 동일 APK의 Android API26/33/34 계측209/209/208개 PASS,12:36 휴대폰 설치·전체 기존 설정 직접 비교 보존·접근성 연결·설치 APK 해시 일치 PASS. 12:38:20 YouTube 공식 시험은 요청10/확인10(긴 영상9+라이브1)과 전후 화면의 서로 다른 영상 확인으로 PASS했다. 12:39:22 별도 일반1/1 전환1회도 화면 쌍으로 확인했다. 그러나 후속 연속 실행 중 요청20/확인19에서 같은59초 길이·pager index 부재로 안전정지했다. 해당 실패 요청에는 전후 화면 쌍이 없어 실제 다음 영상 이동 여부는 미확정이다. Instagram은12:43:56~12:45:31.831(96.0초) 별도 시험에서 요청10/확인10(일반3·긴 영상4·시간제10초2·광고1),수동0·실패/복구0으로 PASS했다. **두 앱의 지정10회 PASS가 유튜브 후속 실패를 덮지 않으며 제품 완료·배포 준비 완료가 아니다.**

**Historical0.2.6/code26 remained unpublished after a subsequent device failure blocked its release.** Build,454 JUnit tests with zero failures,static guards and209/209/208 exact-APK API26/33/34 checks passed; lint has0 errors and3 existing warnings. Installation preserved all compared preferences and accessibility binding,and matched the APK hash. The12:38:20 YouTube run passed10 requests/10 confirmed distinct transitions:9 long-video and1 live. A separate12:39:22 ordinary1/1 transition also passed screenshot-pair review. Further continuation then safety-stopped at20 requests/19 confirmations when both durations were59 seconds and pager indices were unavailable. No pre/post screenshot pair exists for that failed request,so actual movement is unproven. A separate96.0-second Instagram run at12:43:56–12:45:31.831 passed10 requests/10 confirmations:3 ordinary,4 long-video,2 ten-second clockless and1 ad,with no manual swipes,failures or recoveries. The two designated ten-transition PASS results do not override the later YouTube failure or establish release readiness.

YouTube의 별도 RAM 메타데이터 키 경로는 **다른 키 AND (요청 후 같은 창·pager의 최신 실제 index 변화 OR 다른 유효 총길이) AND 300ms 이상 안정 AND 최신 실제 전진 재생**을 모두 요구한다. 요청 시 키 출처를 고정해 메타데이터의 등장·소실을 다른 출처의 키와 비교하지 않는다. 부분 메타데이터 소실로 키만 달라져도 이동으로 인정하지 않는다. 일반 반복 identity는 바꾸지 않는다. 메타데이터 키를 쓰지 않는 기존 확인 경로는 안정된 다른 identity 또는 최신 동일 pager 이동+다른 안정된 총길이+전진 근거를 유지한다. 길이 단독은 확인 근거가 아니며, 메타데이터 경로에서 같은 길이이고 pager index도 없으면 실제 이동했더라도 안전정지할 수 있다. 긴 영상 확인4.5초 실패는 일반 복구나 추가 스와이프로 우회하지 않는다.

The supplemental YouTube RAM-metadata path requires a different key AND either request-fresh same-window/pager index movement or a different valid duration,then at least300ms of stability and current forward playback. The identity source is fixed at request time; appearing,missing or partially missing metadata alone cannot confirm movement. Ordinary repeat identity is unchanged. Non-metadata confirmation retains stable changed identity or corroborated fresh pager movement with changed stable duration and forward progress. Duration alone is insufficient. Same-duration metadata pages without pager indices may still safety-stop after real movement. Long-video4.5-second timeouts never use ordinary recovery or retry swipes.

고정 APK: **757038bytes**, SHA256 `82CE7C221C1BF3E6DA8F86F9D487F9685D89DFB22A38D24F60B77F447519E926`. [검증 원장](docs/VERIFICATION.md), [원인·재발방지](docs/DEBUG_LOG.md).

연속 시험은 반복1·긴 영상ON/기준30초·광고/라이브ON·Instagram 시간제10초로 수행했다. 종료 후 플로팅X로 실행을 중지했고 blocked=false를 확인했다.12:46 인앱 숫자 입력으로 긴 영상 기준30→60초를 복원하고 UI·런타임에서 확인했다. 최종 상태는 **전체 실행OFF,반복1,긴 영상ON/60초,광고ON,라이브ON/0초,시간제ON/10초,화면 분석OFF**다. 제품의 신규 기본값OFF/60초를 바꾼 것이 아니라 기존 옵션은 보존했다. 드문 일반 timeout의 실제 발생·새 시작점 복구,최종 전체 화면 시각/사용성 감사,공개 CI·Release·익명 다운로드 동일성은 완료로 표시하지 않는다. 유튜브 후속 확인 실패가 남아 게시 보류를 유지한다.

Instagram 공식 시험은12:43:56~12:45:31.831,총96.0초이며 기준 요청/확인19→29에서 **요청10/확인10 PASS**다. 구성은 일반3·긴 영상4·진행정보 없는10초 시간제2·광고1,수동 이동0·실패0·복구0이다. 전후0~10 화면을 육안 대조했으며8번 캡처는 광고→일반 전환 중이고9번은 안정된 페이지였다. 목표 뒤 추가6회는 이10회 결과에 합산하지 않는다.

The formal Instagram run lasted96.0 seconds at12:43:56–12:45:31.831,advancing the request/confirmation baseline19→29:10/10 PASS. It comprised3 ordinary,4 long-video,2 ten-second clockless and1 ad transition,with0 manual swipes,failures or recoveries. Screenshots0–10 were visually reviewed;capture8 shows the outgoing-ad/incoming-ordinary gesture and9 a settled page. Six later transitions are excluded from this ten-transition result.

다음 안전한 조사 방향은 `CollectionItemInfo` 또는 pager 스크롤 위치가 독립적인 페이지 이동 근거를 제공하는지 **읽기 전용으로 관측**하는 것이다. 이번 상태 정리에서는 추가 구현·공개를 하지 않는다. 기존 확인 조건을 제거하거나 서로 겹치지 않는 제목 전용→음원 전용 메타데이터를 곧바로 다른 영상으로 인정하지 않는다.

The next safe investigation is read-only observation of CollectionItemInfo or pager scroll position for independent transition evidence. No further implementation or publication occurs in this status update. Do not drop confirmation guards or treat disjoint title-only→audio-only metadata as proof of another video.

**이전 후보는 별도 기록이다.** code23은12:12 실제62→93초 영상 이동 후 요청1/확인0으로 실패했고, code24는12:17 같은 창·영역·인식·안전 조건에서도 공통 텍스트 identity가 같음을 재현했다. code25도12:21~12:22 실제93→57초 이동 후 요청/현재 index가 모두−1이고 공통 identity가 같아 실패했다. code23/24/25는 실폰FAIL·미배포이며 PC·계측PASS가 이를 덮지 않는다. code22의 YouTube2회는 기능 추가로 중단한 과거 관측이며 수동180초 영상 이동1회는 제외했다. 어느 후보의 관측도 code26의10회에 합산하지 않는다.

Earlier code23/24/25 candidates failed physical confirmation and were not published,despite PC/emulator passes. Code23 actually moved62→93 seconds but confirmed0 of1 requests; code24 reproduced identical shared-text identities; code25 moved93→57 seconds but both pager indices were−1. Code22 stopped after two automatic transitions for feature integration,excluding one manual180-second skip. No historical transitions count toward code26.

## 과거 code23 · PC/설치PASS 이후 실폰FAIL·미배포 / Historical candidate,device FAIL

아래는12:12실폰FAIL 이전 체크포인트다. UI·설치검증은전체자동전환PASS가아니며최신code28검증으로재사용하지않는다.

code23은 일반 횟수 기반 전환 시간초과의 시작점 복구와 선택형 긴 영상 건너뛰기를 통합한 **미게시·검증 중 후보**다. 긴 영상은 기본OFF·총길이 기준60초·정수1~3600초이며, 선택한 YouTube/Instagram 일반 영상의 확인된 총길이≥기준일 때 같은 안전한 페이지와 실제 전진 재생을 확인한 뒤 넘긴다. 재생 처음이나 완주를 기다리는 기능이 아니며 길이를 모르면 추정하지 않는다. 반복0과 독립이고 전체OFF는 모두 중지한다. 긴 영상 실패는4.5초 확인 후 안전정지이며 일반 카운트 복구로 우회하지 않는다.

최종code23 빌드·418제품시험/실패0·정적가드PASS.12:10 동일APK Android26/33/34 계측163/163/162 PASS,폰설치·기존prefs보존·접근성bound·설치해시일치PASS. 고정APK **757601bytes**,SHA256 `FC866F0459CD3536114758DB277F0FCD0EF84CFA443E9C8817B448D6ED704B7F`. lint0오류/기존3경고의최종보고서는재대조중이다. 실제YouTube10/Instagram10·긴영상조건별실폰·공개CI/다운로드는아직미실행이며아래code22/21수치를재사용하지않는다. 최종 판정은 [검증 원장](docs/VERIFICATION.md), [릴리스 기록](docs/releases/v0.2.6.md)을 따른다.

메뉴는 일반 횟수→긴 영상→시간제→광고→라이브→플로팅→사용 앱→준비→업데이트→실험→도움말이다.0회 독립 옵션과 광고 전용 설정(긴 영상·라이브OFF)을 설명하고, 복구대기·안전정지 표시가 긴 영상/0회 상태에 가려지지 않도록 보호한다. 출처 표시는 README의 한영 요청을 유지하며 새 라이선스를 임의로 지정하지 않는다. [제품 계약](docs/PRODUCT_SPEC.md), [사용법](docs/USER_GUIDE.md), [복구 설계](docs/PLAYBACK_RECOVERY.md), [예방 대장](docs/DEBUG_LOG.md).

EN: Unpublished code23 combines fresh-start recovery and optional long-video skipping(defaultOFF,known duration≥60 seconds,range1–3600). Final build,418 tests,static guards,163/163/162 exact-APK emulator checks and12:10 phone installation/preferences/binding/hash passed. The frozen APK is757601 bytes with the SHA-256 above. Lint reported0 errors/3 existing warnings pending final report comparison. Social-app10+10,physical long-video cases and public delivery remain unperformed; code22/21 evidence is not reused.

## 과거 0.2.6/code22 후보 · 시작점 복구 / Historical fresh-start candidate

일반 영상 전환 확인4.5초 초과 후 화면 조회를 유지하고 새 시작+실제 전진을 확인해 누적을 폐기하고 N회를 새로 센다. 다른 요청host/window·광고/라이브/시간제/화면추정 우회·즉시 재스와이프는 금지한다. 거부/취소/전환 중 화면변경 등은 기존 안전정지다. 플로팅 대기/정지를 숫자와 구분하고 횟수 도움말에 복구 동작을 안내했다. [복구 계약·위험·시험](docs/PLAYBACK_RECOVERY.md).

과거code22는 PC 빌드·383제품시험·정적가드PASS, 독립 소스리뷰 확인된P1/P2 없음이었다. 같은 APK Android26/33/34 계측109/109/108 PASS(복구 합성 서비스 연결검사 각35개 포함). YouTube는 실제2회 자동 전환을 확인하고180초 영상1개는 수동 이동하여 성공 수에서 제외했다. 긴 영상 기능 추가에 따라 세션을 중단했으므로10연속PASS가 아니며 Instagram10도 미실행이다. 최초 실패 원인·알림 관련 실제 재현은 미확정이다. code22는 미게시 과거 후보이며 이 수치를code23에 합산하지 않는다. 개인 원시 캡처·로그는 공개하지 않는다.

EN: Historical unpublished code22 passed build,383 product tests,static guards and109/109/108 exact-APK emulator checks. Its phone run confirmed two automatic YouTube transitions; one manually skipped180-second video is excluded. Feature integration stopped the session before ten consecutive transitions, and Instagram10 was not run. None of these results verifies code23. The original failure and notification causation remain unproven.

## 후속 진단 · D-034 / Subsequent diagnosis

알림 관련 코드 검토: 단순 알림 수신으로 초기화하지는 않지만 활성창/입력 포커스/재생정보 가용성이 바뀌면 현재 카운트 초기화 또는 진행 중 넘김 확인 실패가 가능하다. 실제 카톡 알림과 최초 실패의 연관은 미검증이며 [D-034](docs/DEBUG_LOG.md)에 조건과 근거를 기록했다. 제품·기기 변경 없음.

후속 상태: 플로팅 간편모드1→0→1로 같은 영상에서 재개 후3회 실제 자동 이동(58→48→19→69초)을 확인했다. 원래 실패는 재현되지 않았다. 최초 실패의 근본 원인은 아직 미확정이며, 실패 후 재조회 자체를 중단하는 구조는 확인했다. 실행ON/목표1/간편모드1 유지. 제품 소스·설치본·공개본은 변경하지 않았으며 아래 읽기 전용 설명은 초기 단계다.

EN follow-up: Rearming the same video through target1→0→1 restored counting and three actual automatic transitions. The original failure did not reproduce; its root cause remains unknown. ExecutionON/target1/tap mode1 remain, with no product/install/release change. The read-only description below records the initial phase.

2026-08-28 11:20경 공개code21에서 일반 YouTube 화면의0/1 정지를 확인했다. 접근성 연결/전체실행은ON이나 넘김 확인 실패로blocked=true(요청21/확인20)여서 이후 조회 자체가 중단된 상태다. 플로팅이 오류 대신0/1을 표시하는 문제가 함께 확인됐다. 최초 전환 실패의 원인은 당시 표본 부족으로 미확정이며 현재 영상 미지원으로 단정하지 않는다. 화면·설정·실행을 그대로 둔 읽기 전용 조사, 코드·APK·공개본 변경 없음. 복구/재현/수정 전이며 [D-034](docs/DEBUG_LOG.md)를 따른다.

EN: Code21 subsequently remained at0/1 on a normal YouTube page because an unconfirmed advance latched blocked=true(21 requests/20 confirmations), despite connected/enabled status. The floating label hides the error. The triggering transition has not been reproduced; no restart, settings change, implementation or publication occurred during diagnosis.

## 공개 완료 / Published release

2026-08-28 10:57 KST에 **0.2.5/code21을 기존 Public 저장소의 시험판으로 공개**했다. [v0.2.5 Release](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.5), main·태그 기준 코드 커밋 `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5`, draft=false/prerelease=true. [CI33134278633](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33134278633) 성공: 내려받은 보고서356시험·실패0·오류0·건너뜀0, CI lint0오류/2경고(로컬 기존3경고와 구분). 익명 릴리스HTTP200 및 배포파일3개(APK709703bytes/SHA96bytes/JSON287bytes)의 원본 대비 크기·SHA-256 일치를 확인했다. 이후 문서 정리는 제품 소스·APK를 바꾸지 않는다.

**Version0.2.5/code21 was published as a Public prerelease at10:57 KST on2026-08-28**, from code commit `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5` and tagv0.2.5. CI33134278633 succeeded with356 tests and zero failures/errors/skips; CI lint reported0 errors/2 warnings, separately from3 local warnings. Anonymous release access returnedHTTP200 and all three uploaded assets matched their originals byte-for-byte and by SHA-256. Subsequent documentation changes do not alter product source or the APK.

공개 후10:58~10:59, 설치된code21 앱의 업데이트 확인을 실제 실행해 새 업데이트 없음 안내·조회 시각 갱신을 확인했다. 실제 Public HTTPS 조회와 현재 버전 안내는PASS이며 새 버전 다운로드/설치 재시험은 아니다. 업데이트 자동조회ON·전체실행OFF를 유지했다.

At10:58–10:59, the installed code21 app successfully checked the public release over HTTPS, displayed no applicable newer update and updated its attempt timestamp. This verifies current-version checking, not a new-version download or installation. Automatic checking remained ON and overall execution OFF.

## 최신 검증 체크포인트 / Latest verification checkpoint

**현재 공개판은0.2.5/code21이며 PC 검증·실기기 설치 PASS**다. 고정 APK709703bytes, SHA256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`.356JUnit·빌드·lint0오류/기존3경고·정적가드(LIVE_TREE_LIFECYCLE 포함),10:39 동일 APK의 API26/33/34 계측74/74/73 PASS.10:40 휴대폰code21 설치·접근성 bound·재생 설정 보존·설치본 해시 PASS. 최종 조회 원복 독립리뷰에서 추가P1/P2 없음.

**Published0.2.5/code21 is PC-verified and phone-installed.** Its frozen709703-byte APK matches the SHA-256 above. Build,356 tests, lint(0 errors/3 existing warnings), static guards and74/74/73 exact-APK emulator checks passed; phone version/accessibility binding/preferences/hash checks passed at10:40. Final cleanup review found no additionalP1/P2 findings.

**code21 YouTube10연속 자동 전환 PASS**:10:40:41→10:47:45.702,424.5초,확인기준0→10,세대6 유지. 수동 입력·앱 전환 없이 일반 영상10개를 자동으로 넘겼고 라이브 이동은0회다. 전환1~10 화면을 모두 육안 대조해 서로 다른 정상 전체화면 영상·플로팅과 올바른 이동 방향을 확인했다. 목표 이후 관측분은 공식10회에 더하지 않는다. 약10:50 플로팅X로 전체실행OFF 확인. 10:51 업데이트 자동조회 선택을ON으로 복원하고 UI와 저장값을 확인했다.

code21 라이브0초/5초/OFF 개별 재시험은 NOT RUN으로 아래code20 근거와 구분한다. D-021은 미수정,20연속 검증도 미완료다. 공개·CI·익명 다운로드/해시 확인은 위 공개 기록과 [검증 원장](docs/VERIFICATION.md)을 따른다.

**Code21 passed10 consecutive YouTube auto-transitions** from10:40:41 to10:47:45.702(424.5 seconds),confirmation0→10,generation6,without manual input or app changes. All10 transition screens were visually checked: distinct normal full-screen videos,visible floating control and correct movement. No live preview was skipped. Observations after the target are excluded from the official count. FloatingX stopped overall execution around10:50; the automatic-update preference was restored to ON and checked in UI/storage at10:51. Code21 individual live retests and20 consecutive transitions remain incomplete; D-021 is not fixed. Publication checks are recorded above.

이전 개별 라이브 검증 후보는 **0.2.5/code20, 빌드·핵심 라이브 실기기 시험 PASS, 미게시**다. cleanup을 포함한356제품시험·빌드·lint0오류/기존3경고, 동일 후보 API26/33/34 에뮬레이터74/74/73검사를 통과했다. 휴대폰 설치 준비·설정 보존·설치본 해시도 일치했다. 실제 라이브5초 지연·0초/반복0회 독립 동작·라이브OFF 대기를 각각 확인했다. 새10연속 시험은10:35:28에 시작해2회 확인 후10:37:17 외부 앱 전환으로 중단됐다. 제품 감지FAIL이나10PASS가 아니다. [검증 기록](docs/VERIFICATION.md), [D-033](docs/DEBUG_LOG.md).

The device-tested **0.2.5/code20** passed its final build,356 product tests, lint(0 errors/3 existing warnings),74/74/73 exact-candidate emulator checks and phone installation/preferences/hash checks. Actual5-second delay, immediate skipping at zero normal plays, and live-OFF waiting passed separately. A new10-transition run began at10:35:28 and ended externally at10:37:17 after two confirmations. This historical code20 candidate was not published.

## 과거 code20 검증 산출물과 동작 / Historical code20 artifact and behavior

고정 APK: **725487bytes**, SHA256 `EF59D4E40E192A89D5B207741B03CCE08FA11AC1079DC61C7776C19A1D3D60EB`. 이 code20 산출물과 code18 실패 후보를 혼합하지 않는다. code20은 당시 미게시 후보로 보존한다. 공개 대상은 위code21이며 [릴리스 기록](docs/releases/v0.2.5.md)과 구분한다.

Frozen code20 APK: **725487bytes**, with the SHA-256 above matching the installed artifact. It is not interchangeable with code18 or a subsequent rebuild; it remained unpublished. Publication verification above concerns code21 only.

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
- 긴 영상: 기본OFF·총길이 기준60초/1–3600초, 확인된일반영상길이≥기준·안전한실제진행 확인 후 넘김. 길이불명 추정 금지, 반복0과 독립.
- 진행정보 없는 적격 Instagram 단일영상: 시간제 선택 시10초 기본/5–60초. 정확한 완주 횟수가 아님.
- 0회: 일반 반복·시간제·화면분석 중지. 선택 앱의 긴 영상·Instagram 광고·YouTube 라이브 옵션은 전체 실행ON일 때 각각 독립 적용.
- 라이브: 기본OFF·0~60초/기본0초, 전용 노드·안정된 단일 페이지·전면창 검증. 제목/CTA/시청자 수는 식별 근거가 아니며 같은 노드 재사용 시 안전 정지할 수 있음.
- 전체 실행OFF: 광고·라이브 포함 전부 중지. 플로팅은 선택 사항, 두 가지 탭 모드와 위치 저장.
- 설정 순서: 횟수→긴 영상→시간제→광고→YouTube 라이브→플로팅→사용 앱→사용 준비→업데이트·앱 정보→실험→도움말.
- 준비가 부족하거나 새 버전이 있을 때만 상단 안내. 전체 실행은 하단 고정.
- API26 기본 기능, API29 타일 상태줄, API33 타일 추가 요청, API34 창 화면 분석. 공식 호스트 앱의 OS 지원은 별개.
- 업데이트: 고정 GitHub 공개 릴리스/시험판, 앱 진입 시 최대 하루 한 번 선택 조회, 수동 조회·다운로드·설치. OS 확인 수동, 설치 전 실행OFF, 설정 유지.
- 인터넷은 업데이트 조회/다운로드에만 사용. 영상·계정·시청이력 수집/업로드 없음.

## 검증과 제한 / Verification and limits

code18 제품352JUnit·빌드 PASS, lint0오류/기존3경고. 동일 고정 APK로 API26/33/34 에뮬레이터74/74/73검사 PASS. 실제 휴대폰 ADB 업데이트 설치·기존 접근성의 설치 후 재연결·재생 설정 보존·설치본 해시 일치 PASS. 이는 새 권한 자동 부여나 전체 실행 자동 시작을 뜻하지 않는다. code18 라이브 인식은 FAIL이며 정식0/5초 실제 넘김·10연속은 NOT RUN이다. code20의 새 검증은 위 체크포인트와 검증 원장에 별도 기록하며 에뮬레이터 계측은 소셜앱 자동 넘김 E2E가 아니다.

독립 리뷰의 최종 root 재검증, 요청 전 오래된 pager 인덱스 배제, 일반 쇼츠의 기존 null-child/600노드 수집 유지, 동일 uptime 안정 상태 보존을 수정했다. 해당 회귀는352제품시험에 포함된다. code16의274시험·47/47/46계측·36개 설치 사전검사·OS15→16 설치·기존 GitHub 자산 다운로드 결과는 과거 이력으로 별도 보존한다.

게시 전 신규 버전 응답은 테스트 APK 전용 fixture이며 실제 GitHub에서 미공개 파일을 받은 것으로 보고하지 않는다. 제품에는 테스트 후크/fixture 자산이 없다. 공개 후 code21의 익명 다운로드·메타데이터·실제 앱의 현재 버전 조회를 확인했다. v2서명 검증과 기존 인증서 유지도PASS이며 상세값은 [검증 원장](docs/VERIFICATION.md)에 기록한다.

기존 D-019/D-021 간헐 중지·특수 창·호스트 UI 변경·장시간 및20회 연속 시험 미완료는 유지한다. code18은 라이브 감지를 추가하지만 일반 영상 반복 경계 문제를 고친 버전이 아니다. code16 당시17→0 관측을 code18 새 증거로 바꾸지 않으며 code18 D-021 재시험도 미완료다. VisualSequence 별도 실험20개 중18통과2실패는 제품에서 제외된다. 오디오 실험은 별도 앱이며 통합하지 않았다.

## 개발·재현 / Development

- JDK17 이상, SDK35/BuildTools35.0.0, Gradle8.9/AGP8.7.3.
- Windows 한글 경로의 Gradle test worker 제약은 `scripts/verify.ps1`의 직접 JUnit으로 검증. CI는 Linux 표준 Gradle 시험.
- 호환성 계측은 `scripts/verify-compat-emulator.ps1`로 에뮬레이터에서만 실행.
- `-PupdaterBootstrap`는 설치 시험용code15/전용 계측 runner를 선택한다. 최신 검증 대상은0.2.6/code28이며code21은 이전 공개판,code23/24/25는실폰FAIL·미배포,code22/18은보존된이전후보이다.
- 테스트 APK의 `final-update.apk` 자산은 로컬 시험용이며 Git/제품 APK에서 제외.
- `scripts/prepare-release.ps1 -Apk <tested.apk>`는 버전·기존서명 검증 후 APK/업데이트JSON/SHA 산출. 기존 출력 덮어쓰기 금지.
- 실제 자동넘김 관측 중 UIAutomator는 접근성 연결에 간섭하므로 사용하지 않는다. 개인 화면·로그는 비공개로 보관한다.

## 문서 색인 / Documentation index

- [한영 소개·사용법](README.md)
- [시작점 복구·예방 시험](docs/PLAYBACK_RECOVERY.md)
- [0.2.6 릴리스 기록](docs/releases/v0.2.6.md)
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

D-033의 code20 인식·5초/0초·OFF 시험은 통과했으나 당시10연속은2회 확인 후 외부 앱 전환으로 중단됐다. 공개code21의 PC·설치·일반 YouTube10연속·CI·익명 배포파일 무결성·실제 업데이트 조회는PASS다. code21 개별 라이브 재시험·20연속은 미완료이고 D-021 등 일반 영상 한계는 유지한다. 제조사별 절전/타일·폴더블·장시간은 남은 검증이다. 메일 발송은 하지 않았다.

EN: Published code21 passed automated/install checks, ten normal YouTube transitions, CI, anonymous asset-integrity checks and an actual current-version update query. Individual code21 live retests, twenty-transition completion, D-021 and wider device/endurance coverage remain unresolved. No email was sent.
