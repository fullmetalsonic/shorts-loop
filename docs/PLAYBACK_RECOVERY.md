# 새 시작점 재인식·카운트 복구 / Fresh-start count recovery

## 현재 0.2.6/code28 · Public 시험판 공개 / Published pre-release

**0.2.6/code28 공개 시험판(pre-release)을 게시하고 공개 파일 검증까지 완료했다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,로컬lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 is published as a public pre-release,and public artifact verification is complete.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;local lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증 후 기존 Public 저장소에v0.2.6/code28 pre-release를 게시했으며 CI·공개 다운로드 동일성도 확인했다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. After the YouTube retest and impact-scope checks passed,v0.2.6/code28 was published as a pre-release in the existing Public repository. CI and public-download parity were verified.

## 이전 code26 상태 / Previous candidate status

이전0.2.6/code26은 후속 실폰 실패로 배포하지 않았던 후보다. YouTube 지정10회(긴 영상9+라이브1)와 별도 일반1회는 확인했으나,이후 같은 길이·pager index 부재의 긴 영상 전환 확인에 실패했다. Instagram10개는 별도96.0초 시험에서 일반3·긴 영상4·시간제2·광고1,수동0으로 PASS했다. 유튜브 실패 요청은 전후 화면 쌍이 없어 실제 이동 여부가 미확정이다. 긴 영상 실패는 별도의[D-035](DEBUG_LOG.md)이며 이 문서의 일반 카운트 복구로 우회하지 않는다. 기존454JUnit·209/209/208계측·설치PASS를 제품 전체PASS로 간주하지 않는다.<br>
Historical code26 remained unpublished after a later long-video confirmation failure. The designated YouTube10(9 long-video,1 live) and one separate ordinary transition passed,but same-duration pages without pager indices later safety-stopped. Instagram10 separately passed in96.0 seconds(3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes). Actual movement for the failed YouTube request is unproven without a pre/post screenshot pair. This separate D-035 issue is not bypassed by ordinary recount recovery;454 tests,209/209/208 emulator checks and installation passes do not establish overall readiness.

복구 대기 중 다른 앱·새 창·진행정보 없는 영상으로 이동하면 대기가 계속될 수 있다. 원래 앱의 같은 정상 재생 창으로 돌아가거나 전체 실행을 OFF→ON하여 새 세션을 시작한다. 복구가 임의로 다른 창의 광고·라이브 넘김으로 이어지지는 않는다.

During recovery, moving to another app, a new window or a clockless page may leave the app waiting. Return to ordinary playback in the original app/window, or explicitly turn execution OFF→ON to start a new session. Recovery never turns into an automatic ad/live skip in another window.

## 현재 복구 범위 / Current recovery scope

대상: **0.2.6(code28) 검증 완료·Public 공개 시험판**. 이 문서는 동작 계약과 시험 항목을 정리한다. 최신 공개·실제 기기 근거와 미실행 조건은 검증 원장에 구분한다. [최신 검증](VERIFICATION.md), [디버그 이력](DEBUG_LOG.md), [사용 설명서](USER_GUIDE.md), [제품 기준](PRODUCT_SPEC.md).

Scope: **0.2.6(code28), a verified Public pre-release**. This document specifies behavior and checks;the verification record distinguishes completed publication/device evidence from unrun scenarios.

code28에는 별도의 **긴 영상 건너뛰기**가 통합된다. 기본OFF·기준60초·1~3600초이며, 선택 앱의 확인된 일반 영상 총길이가 기준 이상일 때 화면 안정과 실제 진행을 확인한 뒤 넘긴다. 반복0과 독립이고 길이 불명은 적용하지 않는다. 긴 영상 요청의4.5초 전환 실패는 안전정지이며 여기의 일반 반복 복구 대상이 아니다. 상세 입력·동작은 [사용 설명서](USER_GUIDE.md)와 [제품 기준](PRODUCT_SPEC.md)에 있다.

Code28 retains a separate long-video option: OFF by default,60-second threshold,range1–3600. Known ordinary durations at least the threshold qualify only after stable-page and forward-playback checks, independently of zero plays. Unknown durations never qualify. A4.5-second long-video transition failure hard-stops; it is not eligible for ordinary recount recovery.

이전code22의 YouTube 시험은2회 전환 확인 후 새 기능 통합으로 중단했으며 code26의10개 시험에 합산하지 않는다. / The earlier code22 YouTube run was stopped after two confirmed transitions for feature integration; it is not added to code26's ten-transition run.

code28의 YouTube 보조 콘텐츠 키 경로는 요청 시 직접 자식 단일 전체 페이지의 `CollectionItemInfo` 행 번호를 저장하고 확인할 때 다시 읽는다. 같은 창·동일 pager 객체·동일 전체 페이지 영역과 전후 콘텐츠 표본 일치를 확인한다. 요청/현재 행을 모두 알 때는 **현재 행=요청 행+1**만 허용하며,같음·역방향·+2 이상은 총길이가 달라도 거절한다. 정확한+1은 기존 pager 이동·유효한 총길이 차이 외에 추가로 인정하는 독립 근거다. **다른 콘텐츠 키·300ms 이상 안정·최신 실제 전진 재생**은 계속 필요하고 길이·행 번호 하나만으로 이동을 확정하지 않는다. 과거+1을 저장해 재사용하지 않으므로 행이 되돌아가면 이전 이동 근거도 유지되지 않는다.

`CollectionItemInfo` 자체가 없는 것은UNKNOWN이다. UNKNOWN은+1 근거가 아니며 기존 안전한 확인 경로만 평가한다. refresh 실패·잘못된 행/열/span·다른 창·pager·영역·복수 페이지·전후 콘텐츠 불일치는UNSAFE로 구분해 기존 길이 근거로도 우회하지 않는다. 콘텐츠 키의 등장·소실은 요청 시 고정한 출처를 바꾸지 않는다. 읽기 전용이며 새 권한·오디오·화면 캡처·영상 내용 저장/전송을 추가하지 않는다. 일반 반복 복구·광고·라이브·Instagram 확인·4.5초 긴 영상 안전정지는 유지한다.

For YouTube's supplemental-content path,code28 snapshots the direct child's CollectionItemInfo row and re-reads it in the same window,pager object and full-page bounds,with matching before/after content samples. If both rows are known,only current=request+1 is permitted;unchanged,backward and skipped rows reject even a different duration. Exact+1 is an additional independent corroboration,not a replacement for a changed content key,at least300ms stability and current forward playback. Evidence is recomputed,not latched after a previous+1. Missing CollectionItemInfo is UNKNOWN,not movement evidence;malformed shape,refresh failure,wrong window/pager/bounds,multiple pages or mismatched samples are UNSAFE and cannot fall back through duration. The generic Instagram path and ordinary/special-content safety policies remain unchanged from code26 in this code28 correction.

## 1. 목적과 변경 범위 / Purpose and scope

이전에는 넘김 요청 후 다음 영상임을 확인하지 못하면 안전정지 상태에서 일반 진행 정보 조회도 중단되어, 영상이 다시 처음으로 돌아와도 카운트가 재개되지 않을 수 있었다. 확인된 것은 이 정지 지속 경로이며, 최초 넘김 실패의 원인이나 알림과의 인과관계까지 확정한 것은 아니다.

0.2.6은 **일반 진행 정보로 판단한 넘김 요청의 4.5초 전환 확인 시간 초과**만 제한적으로 복구한다. 실패한 스와이프를 다시 보내는 것이 아니라 현재 안전한 영상의 재생 위치를 계속 관측한다. 확실한 새 시작과 실제 재생 진행이 확인되면 이전 누적값을 버리고 기존 정상 카운터로 설정 횟수를 다시 센다.

Previously, an unconfirmed advance could stop further progress observation, so the next playback beginning would not restart counting. This persistent-stop path is established; the original failed advance and any notification-related cause are not. Version0.2.6 adds limited observation-based recovery only for a4.5-second timeout after an ordinary progress-based advance. It does not retry the swipe.

## 2. 사용 예 / Example

반복 **1회** 설정에서 같은 영상이 계속 재생되는 경우:

1. 첫 재생을 추적하다 카운트 또는 전환 확인을 잃는다.
2. 이미 본 부분을 완주로 인정하지 않고 새 시작점을 기다린다. 기다리는 동안 복구용 추가 스와이프는 없다.
3. 영상이 처음 부근으로 돌아오고 실제로 앞으로 재생되는지 확인한다.
4. 그 재생부터 `1/1`로 새로 추적한다. **처음을 인식했다는 이유만으로 넘기지 않는다.**
5. 새로 추적한 한 회의 완주를 정상 카운터로 확인한 뒤 다음 영상 이동을 요청한다.

이 예에서는 두 번째 재생까지 보게 된다. 다만 이미 다음 영상으로 이동했으나 확인만 놓쳤다면 현재 영상의 새 시작부터 세므로, 총 시청 횟수를 항상 정확히 두 번으로 보장하는 기능은 아니다. 복구를 기다리거나 정보를 놓친 만큼 추가 재생이 생길 수 있다. 반복2회라면 새 시작 후 두 회를 다시 관측한다.

With target1, wait for a fresh beginning, verify actual forward playback, then observe one complete play before advancing. If the original video remained on screen, this can mean watching its second play. If the swipe already reached another video but confirmation was missed, the current video's next verified start becomes the new baseline. Extra plays are possible; recovery does not guarantee exactly two total views. Target2 requires two newly tracked plays.

## 3. 복구 시작 확인 / Fresh-start evidence

| 조건 | 의미 |
|---|---|
| 같은 요청 앱과 창 | 다른 앱·창으로 실패한 요청을 이어 쓰지 않음 |
| 안전한 일반 영상 | 단일 대상·유효한 진행 위치와 길이·일시정지 아님을 확인 |
| 시작 부근 | 위치가 `min(1초, 영상 길이의 10%)` 이내인 표본 |
| 후속 진행 | 시작 표본 후 300ms~3초 이내, 같은 영상에서 정상적인 전진 재생 확인 |
| 기존 카운터 재사용 | 이전 누적을 버리고 새 기준으로 설정 횟수의 완주 관측 |

0초 한 표본, 0초에 멈춘 버퍼링·일시정지, 시간이 흘렀다는 사실만으로는 복구하지 않는다. 영상 식별·길이가 바뀌거나 관측 공백·비정상 시간 이동이 생기면 불확실한 시작 증거를 이어 붙이지 않는다. 재생 중간에서 화면이 돌아왔다고 바로 `1/N`으로 간주하지 않는다.

Recovery needs a near-start sample at or below `min(1 second,10% of duration)`, followed by plausible forward playback within300ms–3s on the same recognized video. A single zero, a frozen zero, elapsed time alone, an observation gap, changed identity/duration or an implausible jump is insufficient. The existing loop counter must still establish completed plays.

## 4. 기존 보호와 중단 / Preserved guards

- 복구 대기는 추가 스와이프를 발행하지 않는다. 이미 넘어간 영상을 곧바로 한 번 더 넘기는 재시도 방식이 아니다.
- 복구 상태에서 긴 영상·광고·라이브·시간제·화면 분석 경로로 전환하여 우회 이동하지 않는다.
- 긴 영상·광고·라이브·시간제·화면 분석에서 발생한 전환 실패는 이 복구 대상이 아니다.
- 제스처 거부·취소, 전환 중 앱·창 변경, 잠금·권한·대상 불명확 등의 기존 보호를 없애지 않는다.
- 전체 실행OFF·×는 모든 자동 동작보다 우선한다. 목표0은 일반 카운트 복구를 중지하며 긴 영상·광고·라이브의 독립 설정 계약 자체는 바꾸지 않는다.
- 설정·앱 선택 변경은 기존 초기화 정책을 따른다. 접근성 재연결이나 프로세스 재시작으로 전체 실행이 OFF가 된 뒤 자동으로 다시 켜지 않는다.
- 정상적인 일시정지나 버퍼링을 재생 완료로 간주하지 않는다. 정보가 끝내 돌아오지 않거나 안전한 시작을 확인하지 못하면 계속 대기하거나 해당 안전정지를 유지한다.

No recovery swipe, fallback bypass, permission change or automatic execution restart is introduced. Hard stops outside the ordinary timeout remain. Overall OFF and target0 take precedence; independent ad/live settings are unchanged. A permanently unreadable or unsafe screen does not recover merely because enough time has passed.

## 5. 화면에서 확인하고 수동으로 복구하기 / Status and manual recovery

정상 회차의 `1/2`와 복구 대기·안전정지 표시를 구분한다. 대기 상태는 새 시작을 관측 중이라는 뜻이지 기능이 꺼졌거나 목표값이0으로 저장됐다는 뜻이 아니다. 플로팅 표시를 사용하지 않으면 앱 하단 상태에서 확인한다.

대기가 길어지면 먼저 전체 화면의 일반 쇼츠/릴스인지, 영상이 일시정지되어 있지 않은지 확인한다. 안전정지 안내가 있거나 상태가 불확실하면 전체 실행을 OFF로 바꾸고 화면을 확인한 뒤 다시 ON한다. 권한 연결이 끊겼다면 [접근성 재연결 안내](USER_GUIDE.md)를 따른다. 앱 삭제나 설정 초기화를 먼저 할 필요는 없다.

Waiting is distinct from ordinary counts and hard stops; it does not mean the saved target became zero. Check in-app status when floating controls are hidden. If uncertain, stop execution, inspect the screen and restart manually. Follow accessibility-reconnection guidance if disconnected; uninstalling or clearing data is not the first recovery step.

## 6. 예방 시험과 완료 기준 / Preventive checks and acceptance

아래에는 동작 확인 항목과 결과가 명시된 실기기 시험을 함께 구분한다. 합성 확인을 실제 장애 복구 성공으로 확대하지 않는다. 실행 결과는 [VERIFICATION](VERIFICATION.md)에 빌드별로 기록하며 이전 버전의 PASS를 재사용하지 않는다.

| 항목 | 확인할 결과 |
|---|---|
| 중간 위치·0초 한 번·0초 정지 | 복구/이동 없음 |
| 시작 부근→정상 전진 | 새 기준으로 복구, 그 순간 추가 스와이프 없음 |
| 반복1/2 | 새 시작 뒤 각각1/2회 전체 관측 전에는 이동 없음 |
| 앱·창·영상 식별 변경·관측 공백·수동 탐색 | 불확실한 표본을 결합하지 않음 |
| 기존 요청의 늦은 완료·취소 콜백 | 새 세션을 오염하거나 중복 요청하지 않음 |
| 전체OFF·목표0·설정/권한 변경 | 이전 복구 예약이 동작을 되살리지 않음 |
| 긴 영상·광고·라이브·시간제·화면 분석 실패 | 복구 정책을 통한 우회 없음 |
| 긴 영상 총길이 기준·정지 상태 | 60초 기준에서59초 제외/60초 포함, 유효한 전진 없이 넘김 없음 |
| 긴 영상 옵션·반복0 | 전체ON에서 독립 적용, 총길이 불명·0회 시간제 추정 없음 |
| 긴 영상 입력·저장 | 1~3600초·기본60·+/−/초안·다른 설정 보존·자동 실행 없음 |
| 일반·복구대기·안전정지 표시 | 상태와 필요한 행동을 구분 가능 |
| 새 APK 실제 YouTube20개 | code28 수동0·요청20/확인20 PASS,일반4·긴 영상15·라이브1 |
| Instagram10개 유지 근거 | 변경하지 않은 경로의 code26 PASS 보존,code28에서 전체 재실행하지 않음 |

code28 YouTube20회와 code26 Instagram10회는 서로 다른 버전·앱의 별개 세션으로 기록한다. 요청 수 증가나 화면 캡처만으로 성공 처리하지 않고 실제 전환 증거를 함께 확인한다. YouTube10+Instagram10을 한 앱의20연속 내구 시험으로 표현하지 않는다. 별도의 중단·복구 시험을 일반10개 연속 성공에 섞어 합산하지 않는다.

Behavioral checks and explicitly reported device results are distinguished. Code28's YouTube20 PASS and code26's retained Instagram10 PASS are separate runs;requests alone are not successes and manual swipes do not count. Recovery fault scenarios remain separate from consecutive-run evidence.

## 7. 개인정보·남은 한계 / Privacy and limits

새 권한·녹음·알림 내용 읽기·화면 캡처·네트워크 전송은 추가하지 않는다. 진단에는 원인 분류·수치 상태 등 필요한 최소 정보만 사용하고 영상 제목·계정·알림 내용·개인 화면·원시 기기 로그를 공개하지 않는다. 재생 정보가 없는 영상이나 앱 UI 변경을 새로 인식하게 만드는 기능은 아니다. 기존 끝 경계 누락 등 감지 한계와 최초 실패 원인 미확정 상태는 별도로 유지한다.

No new permission, recording, notification-content reading, screen capture or network transfer is added. Diagnostic evidence must exclude titles, accounts, notification contents, personal screens and raw device logs from public documents. Recovery does not create missing playback information or prove the original failure cause; existing detection limits remain separate.
