# UI·인간공학 기준 · ShortsLoop 0.2.7

## 0.2.7/code29 · 플로팅 크기 유지·글자 잘림 수정 / Compact-label correction

외곽72×56dp·X24×24dp·반투명·기존 조작은 유지한다. 숫자/문구를 X 아래 전체폭으로 배치하고 실제 공간 안에 들어가는 가장 큰 글꼴을 사용한다. ‘긴영상’ 축약 없이 실폰에서 전체 표시 확인. 큰 글꼴 배율0.85–2와 RTL/굵은 글꼴의 실제 경계 검사 PASS. 기존 X의 작은 터치영역 한계는 남는다. **아래0.2.6의 잘림 기록은 과거 버전 기록**이며 이번 로컬후보에서 수정했다. [검증·미실행 범위](FLOATING_LAYOUT_FIX.md).

EN: The local0.2.7 candidate preserves the72×56dp window and24×24dp X,using the full width below X and the largest fitting text size. Physical long-label display and native font-scale/RTL/bold bounds checks passed. Existing small-target accessibility limitations remain. Clipping described in0.2.6 records below is historical.

## 최신 0.2.6/code28 · Public 시험판 검증 완료 / Published and verified

**0.2.6/code28 공개 시험판(pre-release)을 게시하고 공개 파일 검증까지 완료했다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,로컬lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 is published as a public pre-release,and public artifact verification is complete.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;local lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

독립 검토는 이번 변경과 검증 근거 범위에서PASS·확인된P1/P2 0건이다. 내비게이션 오버레이 실기기 시나리오는NOT RUN이며,동일 길이 영상 쌍의 최종 실기기 미관측·드문timeout 복구·플로팅 글자 일부 잘림 등 명시된 한계를 지우거나 모든 화면의 시각감사PASS로 확대하지 않는다.

Independent review passed for the changed scope and verification evidence,with0 identified P1/P2 issues. The navigation-overlay device scenario is NOT RUN. This does not remove the documented equal-duration-pair/rare-timeout limits or minor floating-label clipping,or imply that every visual scenario passed.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증 후 기존 Public 저장소에v0.2.6/code28 pre-release를 게시했으며 CI·공개 다운로드 동일성도 확인했다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. After the YouTube retest and impact-scope checks passed,v0.2.6/code28 was published as a pre-release in the existing Public repository. CI and public-download parity were verified.

설정 구조·입력 계약은 유지한다. 플로팅 ‘긴영상’ 첫 글자 일부 잘림은 수정하지 않은 비차단 경미 이슈로 유지한다.1/1·10초 숫자와 기능은 정상이며 최종 시각감사 전체PASS로 표시하지 않는다. 이 문서의 과거 시각 한계와 후보별 검증을 구분한다.

## 과거 code26 · 지정시험 성공 후 후속실패 / Historical candidate

**과거0.2.6/code26은 실폰 후속 실패로 게시 보류된 미배포 후보였다.** 빌드·454 JUnit(실패0)·정적 가드 PASS, lint0오류/기존3경고. 12:33 동일 APK의 Android API26/33/34 계측209/209/208개 PASS,12:36 휴대폰 설치·전체 기존 설정 직접 비교 보존·접근성 연결·설치 APK 해시 일치 PASS. 12:38:20 YouTube 공식 시험은 요청10/확인10(긴 영상9+라이브1)과 전후 화면의 서로 다른 영상 확인으로 PASS했다. 12:39:22 별도 일반1/1 전환1회도 화면 쌍으로 확인했다. 그러나 후속 연속 실행 중 요청20/확인19에서 같은59초 길이·pager index 부재로 안전정지했다. 해당 실패 요청에는 전후 화면 쌍이 없어 실제 다음 영상 이동 여부는 미확정이다. Instagram은12:43:56~12:45:31.831(96.0초) 별도 시험에서 요청10/확인10(일반3·긴 영상4·시간제10초2·광고1),수동0·실패/복구0으로 PASS했다. **두 앱의 지정10회 PASS가 유튜브 후속 실패를 덮지 않으며 제품 완료·배포 준비 완료가 아니다.**

**Historical0.2.6/code26 remained unpublished after a subsequent device failure blocked its release.** Build,454 JUnit tests with zero failures,static guards and209/209/208 exact-APK API26/33/34 checks passed; lint has0 errors and3 existing warnings. Installation preserved all compared preferences and accessibility binding,and matched the APK hash. The12:38:20 YouTube run passed10 requests/10 confirmed distinct transitions:9 long-video and1 live. A separate12:39:22 ordinary1/1 transition also passed screenshot-pair review. Further continuation then safety-stopped at20 requests/19 confirmations when both durations were59 seconds and pager indices were unavailable. No pre/post screenshot pair exists for that failed request,so actual movement is unproven. A separate96.0-second Instagram run at12:43:56–12:45:31.831 passed10 requests/10 confirmations:3 ordinary,4 long-video,2 ten-second clockless and1 ad,with no manual swipes,failures or recoveries. The two designated ten-transition PASS results do not override the later YouTube failure or establish release readiness.

YouTube의 별도 RAM 메타데이터 키 경로는 **다른 키 AND (요청 후 같은 창·pager의 최신 실제 index 변화 OR 다른 유효 총길이) AND 300ms 이상 안정 AND 최신 실제 전진 재생**을 모두 요구한다. 요청 시 키 출처를 고정해 메타데이터의 등장·소실을 다른 출처의 키와 비교하지 않는다. 부분 메타데이터 소실로 키만 달라져도 이동으로 인정하지 않는다. 일반 반복 identity는 바꾸지 않는다. 메타데이터 키를 쓰지 않는 기존 확인 경로는 안정된 다른 identity 또는 최신 동일 pager 이동+다른 안정된 총길이+전진 근거를 유지한다. 길이 단독은 확인 근거가 아니며, 메타데이터 경로에서 같은 길이이고 pager index도 없으면 실제 이동했더라도 안전정지할 수 있다. 긴 영상 확인4.5초 실패는 일반 복구나 추가 스와이프로 우회하지 않는다.

The supplemental YouTube RAM-metadata path requires a different key AND either request-fresh same-window/pager index movement or a different valid duration,then at least300ms of stability and current forward playback. The identity source is fixed at request time; appearing,missing or partially missing metadata alone cannot confirm movement. Ordinary repeat identity is unchanged. Non-metadata confirmation retains stable changed identity or corroborated fresh pager movement with changed stable duration and forward progress. Duration alone is insufficient. Same-duration metadata pages without pager indices may still safety-stop after real movement. Long-video4.5-second timeouts never use ordinary recovery or retry swipes.

고정 APK: **757038bytes**, SHA256 `82CE7C221C1BF3E6DA8F86F9D487F9685D89DFB22A38D24F60B77F447519E926`. [검증 원장](VERIFICATION.md), [원인·재발방지](DEBUG_LOG.md).

연속 시험은 반복1·긴 영상ON/기준30초·광고/라이브ON·Instagram 시간제10초로 수행했다. 종료 후 플로팅X로 실행을 중지했고 blocked=false를 확인했다.12:46 인앱 숫자 입력으로 긴 영상 기준30→60초를 복원하고 UI·런타임에서 확인했다. 최종 상태는 **전체 실행OFF,반복1,긴 영상ON/60초,광고ON,라이브ON/0초,시간제ON/10초,화면 분석OFF**다. 제품의 신규 기본값OFF/60초를 바꾼 것이 아니라 기존 옵션은 보존했다. 드문 일반 timeout의 실제 발생·새 시작점 복구,최종 전체 화면 시각/사용성 감사,공개 CI·Release·익명 다운로드 동일성은 완료로 표시하지 않는다. 유튜브 후속 확인 실패가 남아 게시 보류를 유지한다.

**이전 후보는 별도 기록이다.** code23은12:12 실제62→93초 영상 이동 후 요청1/확인0으로 실패했고, code24는12:17 같은 창·영역·인식·안전 조건에서도 공통 텍스트 identity가 같음을 재현했다. code25도12:21~12:22 실제93→57초 이동 후 요청/현재 index가 모두−1이고 공통 identity가 같아 실패했다. code23/24/25는 실폰FAIL·미배포이며 PC·계측PASS가 이를 덮지 않는다. code22의 YouTube2회는 기능 추가로 중단한 과거 관측이며 수동180초 영상 이동1회는 제외했다. 어느 후보의 관측도 code26의10회에 합산하지 않는다.

Earlier code23/24/25 candidates failed physical confirmation and were not published,despite PC/emulator passes. Code23 actually moved62→93 seconds but confirmed0 of1 requests; code24 reproduced identical shared-text identities; code25 moved93→57 seconds but both pager indices were−1. Code22 stopped after two automatic transitions for feature integration,excluding one manual180-second skip. No historical transitions count toward code26.

최신 계측에는 네이티브 설정·서비스 검사가 포함된다. 일반 횟수→긴 영상→시간제→광고→라이브 순서와0회 독립 동작, 입력 초안·설정 보존을 유지한다. 전체 실행은 하단 고정 토글이며 대기/정지 표시가 옵션 표시보다 우선한다. 계측 PASS를 최신 휴대폰 화면의 육안·사용성 감사 완료로 표현하지 않는다. 실제 좁은 플로팅에서 ‘긴영상’ 첫 글자가 약간 잘리는 화면이 관측됐다. 숫자1/1·10초는 정상이고 동작 영향은 없으나 최종 시각감사 전체PASS로 표시하지 않는다. 이번 상태 정리에서는 UI 구현을 변경하지 않는다.

## 과거 code23 · PC/설치PASS 이후 실폰FAIL·미배포 / Historical candidate,device FAIL

아래는12:12실폰FAIL 이전 체크포인트다. UI·설치검증은전체자동전환PASS가아니며최신code28검증으로재사용하지않는다.

일반 횟수 바로 아래 **긴 영상 건너뛰기** 카드를 추가한다. 기본OFF·총길이기준60초·1~3600초,숫자입력/완료·적용/−/+1초다. ‘기다리는 시간’이 아닌 ‘최소 총길이’임을 밝히고,확인된길이≥기준·실제진행·안전한화면일때만넘기며길이불명은추정하지않음을설명한다. 반복0과별개이고전체OFF는모두중지한다.

선택한설치앱이하나이상일때활성화하고불가이유를표시하며저장옵션/기준/초안을보존한다. 광고전용도움말은긴영상·라이브OFF를포함한다.0회에서긴영상옵션이활성이면플로팅은‘조건’,복구는‘대기’,hardstop은‘정지’이며 **대기/정지 표시가 독립옵션 숫자/라벨보다 우선**한다. 이를감지상태나재생횟수로오인하지않도록도움말을유지한다.

최종code23의418제품시험과12:10 동일APK API26/33/34 계측163/163/162에네이티브UI입력·메뉴순서·옵션보존·서비스안전표시검사가포함된다. 폰설치/설정/접근성/해시는확인했지만,새폰화면전체의시각·사용성감사와YouTube10/Instagram10은아직미완료다. 에뮬레이터입력검사를모든기기화면PASS로확대하지않는다. [검증](VERIFICATION.md), [사용법](USER_GUIDE.md).

EN: Code23 places a default-OFF long-video card immediately below repeat settings,with a60-second total-duration threshold and1–3600 integer input/one-second steps. Saved drafts/options survive unavailable hosts. Zero plays is independent; overallOFF stops everything. Waiting/stopped labels override option labels. Native UI/service checks are included in163/163/162 exact-APK emulator checks;full physical visual/usability and10+10 social-app verification remain pending.

## 과거 0.2.6/code22 · 복구 상태 구분 / Historical recovery visibility

기존72×56dp 플로팅 크기와 탭/드래그/X 동작은 유지한다. 일반 횟수는1/N, 일반timeout복구는‘대기’,수동재시작필요오류는‘정지’로 구분한다. 자세한 사유는인앱상태와접근성설명에표시한다. 시작재인식후에는1/N로돌아오고완주전에넘기지않는다. 횟수카드도움말에다음시작점계산과안전정지재시작을안내한다. [복구계약](PLAYBACK_RECOVERY.md).

The existing72×56dp floating size and tap/drag/X behavior remain. Counts show1/N, ordinary-timeout recovery shows waiting, and hard stops show stopped. Detailed status remains in-app and in accessibility descriptions. The count card explains fresh-start recounting and manual restart for hard stops.

위code22의383제품시험·109/109/108계측PASS는과거복구후보증거다. 실제YouTube2회후180초영상1개수동제외·기능추가중단은10연속PASS가아니며code23검증과합산하지않는다.

## 0.2.5(code21) · 호환성·업데이트·빈도순 메뉴·라이브 미리보기

[호환성 표](COMPATIBILITY.md)에 따라 ‘사용 준비’에 현재 Android 버전·기본 지원·타일 추가 방식·화면 분석 지원 여부를 짧게 표시한다. 미지원 옵션은 숨기지 않고 꺼진 비활성 상태와 이유를 함께 표시한다. Instagram 관련 옵션에는 설치/선택 여부를 설명하고 저장 선택은 유지한다. 구형 타일은 공간이 좁으므로 별도 상태줄 대신 현재 상태를 라벨에 우선하며 접근성 설명에는 앱 이름과 전체 상태를 유지한다.

0.2.5는 기존 카드를 사용 빈도순으로 재배치하고 ‘업데이트 · 앱 정보’와 ‘YouTube 라이브 · 미리보기 넘김’을 추가한다. 하단 전체 실행 고정·기존 입력/리스너/저장값·아이콘 자산은 유지한다. 준비가 부족하거나 새 버전이 있을 때만 상단에 해당 바로가기를 보여 준다. 현재 통합판은2026-08-28 Public 시험판으로 공개했다. 최종 검증·게시 결과는 [VERIFICATION](VERIFICATION.md) 참조.

0.2.4는 기능별 카드로 **일반 영상 횟수 / 정보 없는 Instagram 시간제 / 광고**를 분리한다. 광고는 반복 0회에서도 독립 동작하며 시간제는 0회에서 중지한다. 하단 ‘전체 자동 넘김 실행’은 광고를 포함한 모든 동작의 메인 토글이다. 시간제 기본 OFF·10초·5~60초·±1초, 기존 화면 분석은 별도 ‘실험 기능’ 카드로 보존한다.

0.2.2의 BatterySetupPanel도 유지한다. 현재 Android 절전 예외 상태를 문구+색상으로 표시하고 적용 후에도 ‘확인하기’ 버튼을 둔다. 수동 경로·취소·배터리 비용·지원 한계를 안내한 뒤 자기 앱 설정으로만 이동한다. onResume 상태 갱신은 실행 토글을 조작하지 않는다. 당시 정적 리뷰와 최신 기기 결과는 구분하며 최종 결과는 검증 기록을 따른다.

## 목적과 상태

아이콘과 일체감 있는 색상과 직관적인 설정 흐름을 목표로 한다. 자주 쓰는 설정과 종료 조작을 쉽게 찾고, 플로팅을 사용하지 않는 사람에게 불필요한 설정·권한을 요구하지 않는 것을 우선한다.

아래는 현재 네이티브 구현 기준이며 모든 화면 크기·큰 글꼴·최신 빌드 전체 UX의 PASS 선언이 아니다. 과거0.2.0 입력·플로팅과v021b 광고 동작은 이력으로 유지한다. 특히 당시 ‘0회 광고 중지’ 시험은 새 광고 독립 동작을 검증한 것이 아니다. 최신 시험·기기 결과는 [검증 기록](VERIFICATION.md), 과거 공개판은 [0.2.4 릴리스](releases/v0.2.4.md), 동작 계약은 [PRODUCT_SPEC](PRODUCT_SPEC.md), 조작 순서는 [USER_GUIDE](USER_GUIDE.md)를 따른다.

## 1. 화면 구조와 사용 순서

```text
앱 아이콘  쇼츠 자동 넘김
           반복 · 길이 · 시간제 · 광고 · 라이브

[접근성 연결 확인 · 해결 방법 보기]  ← 접근성 미연결 시
[사용 준비가 필요합니다 · 바로가기]  ← 접근성 연결 후 다른 준비가 부족할 때
[새 버전 … · 업데이트 보기]          ← 호환되는 상위 버전을 확인했을 때만

일반 영상 · 횟수로 넘김
  재생 정보가 있는 영상을 총 몇 번 볼까요?
  [ 큰 숫자 입력 ] [ ▲ ]
                  [ ▼ ]
  0은 일반반복·시간제 중지, 긴 영상·광고·라이브는 별도 / 적용 버튼
  현재 적용 횟수 또는 기준/현재 값 차이

긴 영상 건너뛰기
  긴 영상 건너뛰기 [토글]  ← 기본 OFF, 선택한 두 앱 공통
  [ − ] [ 최소 총길이 (초) ] [ + ]
  기본 60초 · 1~3600초 · 기준 이상(≥)이면 대상 / 적용 버튼
  길이와 실제 진행을 안전 확인 · 대기 시간이 아님
  반복 0회와 독립 · 길이 모름은 추정하지 않음

진행 정보 없는 영상 · 시간제로 넘김
  시간제 넘김 [토글]  ← 기본 OFF, Instagram 전용
  [ − ] [ 대기 시간 (초) ] [ + ]
  기본 10초 · 5~60초 · 1초씩 / 필요할 때 적용 버튼
  반복 0회면 중지 · 완주 보장 아님 · 중단 복귀 시 다시 시작

광고 · 바로 넘김
  반복 횟수와 별개 · 0회에서도 사용 가능
  광고 바로 넘기기 [토글]  ← 기본 OFF, Instagram 전용
  전체 실행도 켜야 동작 · 광고 OFF면 직접 넘김
  광고만 사용: 반복 0회 + 광고 ON + 긴 영상·라이브 OFF + 전체 실행 ON

YouTube 라이브 · 미리보기 넘김
  라이브 미리보기 넘기기 [토글]  ← 기본 OFF, YouTube 쇼츠 전용
  [ − ] [ 라이브 대기 (초) ] [ + ]
  기본 0초 = 바로 넘기기 · 0~60초 · ±1초 / 적용 버튼
  반복 0회에서도 동작 · 전체 실행 OFF는 모두 중지

플로팅 리모컨
  화면 위에 숫자 표시 [토글]
  표시 ON일 때만 두 터치 방식과 이동/종료 설명

사용할 앱
  [체크] YouTube 쇼츠
  [체크] Instagram 릴스

사용 준비
  현재 연결 상태
  부족한 권한 버튼만 표시
  접근성 설정·다시 연결 / 이미 ON이면 OFF→ON 복구 안내
  백그라운드 실행 · 배터리
    절전 예외 상태 / 수동 설정 안내 버튼
  빠른 설정에 실행 토글 추가

업데이트 · 앱 정보
  설치 버전 / 조회·다운로드·설치 상태
  업데이트 확인 [버튼]
  새 버전일 때 다운로드 → 검사 후 설치 [별도 버튼 단계]
  다운로드 중 진행률 / 다운로드 취소
  앱 열 때 새 버전 확인 [토글]  ← 기본 ON, 최대 24시간마다
  GitHub 업데이트 전용 통신 · 설치 진입 시 전체 실행 OFF

실험 기능
  화면 분석 보조 · 시험 [토글]  ← 기본 OFF, 별도 동의
  Android 14 이상 · 정확 N 보장 아님 · 시간제가 우선

사용법과 감지 한계 보기 [펼치기]
버전
──────────────────────────
전체 자동 넘김 실행 [켜짐/꺼짐]  ← 하단 고정
현재 상태 / 다음 행동 설명
```

자주 바꾸는 횟수·시간제·광고·라이브·플로팅을 위에, 사용 앱 선택·최초 권한 연결·앱 정보는 아래에 둔다. 첫 사용자는 상단 준비 바로가기로 앱 선택 또는 권한 카드에 접근한다. 준비를 마치면 바로가기를 숨겨 반복 사용을 방해하지 않는다. 종료는 화면 아래 고정 위치에서 찾는다. 플로팅 관련 기능은 한 카드에 모으고 표시를 끄면 세부 방식을 접어 시각적 부담을 줄인다. 권한이 이미 연결된 사용자는 매번 설정 버튼을 다시 볼 필요가 없다.

업데이트는 새 버전이 확인된 경우에만 상단 배너로 알리고, 누르면 ‘업데이트 · 앱 정보’ 카드로 이동한다. 배너 터치와 다운로드·설치를 혼합하지 않는다. 수동 ‘업데이트 확인’, ‘업데이트 다운로드’, 검사 후 ‘업데이트 설치’를 단계별 문구로 구분한다. 다운로드 중 진행률·취소를 제공하고 실패에는 재시도 안내를 표시한다. 설치 창을 열었다는 이유만으로 성공 표시하지 않는다.

‘앱 열 때 새 버전 확인’은 기본 ON이며 마지막 시도 후 24시간 간격이다. OFF와 수동 확인을 함께 제공한다. 시스템 알림·백그라운드 감시는 없고 이미 확인한 후보는 다시 표시할 수 있다. 설치 버튼 진입 시 전체 실행은 OFF가 되지만 다른 설정은 유지한다고 먼저 안내한다. 사용자가 Android의 ‘이 출처 허용’을 직접 선택하고 돌아온 후 설치를 다시 눌러야 하며 최종 설치 확인도 직접 한다. 취소 후 실행을 자동 복원하지 않는다. 기존 0.2.4에는 이 UI가 없어 최초 0.2.5 APK는 수동 설치해야 한다.

하단 실행 영역의 고정 이유는 설정 화면 어디서든 현재 상태를 보고 끌 수 있게 하는 것이다. 0.2.4부터 명칭을 ‘전체 자동 넘김 실행’으로 통일했다. 이 명칭 변경을 높이 축소나 모든 화면의 사용성 검증 완료로 확대하지 않는다.

라이브는 광고 다음 별도 카드로 배치한다. ‘반복 0회’와 ‘라이브 0초’가 반대 의미로 오해되지 않도록 **0회=일반 반복 중지**, **0초=라이브 인식 후 바로 넘기기**를 각 단위 옆에 설명한다. 기본 OFF·0~60초·±1초이며 숫자 입력은 완료/적용 전까지 초안이다. YouTube 미설치·미선택에는 이유와 비활성 상태를 표시하되 설정은 보존한다. 라이브 옵션만 켜도 전체 실행은 자동 시작하지 않는다. [라이브 동작·한계](LIVE_SKIP.md).

광고를 별도 카드로 분리하여 반복 숫자의 하위 옵션처럼 보이지 않게 한다. ‘반복 횟수와 별개 · 0회에서도 사용 가능’과 광고 전용 예시를 같은 카드에 표시한다. Instagram을 해제하면 광고/시간제/화면 분석은 조작 비활성화하되 값은 보존한다. 광고는 제거가 아닌 페이지 이동이고 재생바 없음만으로 광고라고 판단하지 않는다고 안내한다.

옵션 아래에 ‘끄면 광고는 직접 넘겨 주세요’를 명시해 OFF의 결과를 바로 알게 한다. 릴스 내부의 ‘더 알아보기’ 카드가 팝업처럼 보여도 카드 클릭이나 닫기를 하는 기능은 아니다. 해당 광고 페이지 전체를 넘긴다. 별도 창 광고 팝업 자동 닫기는 미구현/범위 미확정으로 구분한다.

시간제는 횟수 입력과 별도 단위 ‘초’를 사용한다. 예를 들어 반복 2회·10초이면 정상 영상은 2회, 정보 없는 지원 영상은 총 10초이며 20초가 아니다. 정상 영상의 시작 대기 `0/N`과 진행 정보 없음도 구분한다. 타이머는 정확한 완주가 아니며 감지된 정지·댓글·메뉴·다른 화면 복귀 후 처음부터 다시 센다는 설명을 카드 옆에 둔다.

실험 기능은 주 사용 흐름 뒤에 배치한다. 화면 분석은 Android 14 이상·Instagram 선택 때 별도 확인 대화상자로 동의를 받은 뒤 켜며 취소하면 OFF다. 타이머와 둘 다 ON이면 시간제가 우선하고 분석 선택값은 보존한다. 미연결 `VisualSequenceTracker` 실험과 별도 오디오 진단을 이 선택형 기능 또는 제품의 검증된 자동 넘김처럼 소개하지 않는다.

code17부터 제공하는 재연결 안내는 OS 설정의 ON 표시와 실제 서비스 연결을 구분한다. 미연결 때만 해결 방법 배너·설정 버튼과 OFF→ON 복구 순서를 표시한다. 연결 후 전체 실행도 직접 ON해야 하며 화면 분석을 꺼도 접근성은 필요하다고 설명한다. 설명·바로가기 추가이지 권한이나 실행을 자동 변경하는 기능은 아니다.

## 2. 아이콘과 화면의 일체감

아이콘의 짙은 남색·청색/보라색 빛을 화면 색상에 연결한다. 아이콘에는 입체감과 발광감을 남기고, 설정 화면은 글자와 컨트롤의 가독성을 위해 평평하고 절제된 카드로 구성한다. 모든 버튼에 광택·그림자·애니메이션을 넣는 방식은 사용하지 않는다. 0.2.5는 기존 아이콘 자산을 유지하며 새 아이콘 제작 작업은 포함하지 않는다.

| 토큰 | 현재 값 | 역할 |
|---|---|---|
| 배경 | `#080F1B` | 전체 남색 바탕 |
| 카드 | `#111D2E` | 설정 묶음 구분 |
| 테두리 | `#2A3D53` | 카드·조작 경계 |
| 본문 | `#F1F7FF` | 주요 제목·숫자 |
| 보조 글자 | `#AEC1D7` | 설명·다음 행동 |
| 청색 강조 | `#74DCFF` | 선택/실행/현재값 |
| 보라색 | `#BEA4FA` | 아이콘 계열의 보조 팔레트; 모든 컨트롤에 강제 적용하지 않음 |
| 주의 | `#FFCE81` | 차단·오류 상태 |

색상만으로 켜짐/꺼짐을 구별하지 않는다. 체크/라디오/스위치의 모양과 텍스트 상태를 함께 사용한다. 0회 하단은 ‘0회 · 광고 ON/OFF · 라이브 ON/OFF’를 표시한다. 플로팅·빠른 설정은 광고만/라이브만/둘 다/모두 꺼짐을 구분하고 이동 중에는 진행 상태를 우선한다. 전체 OFF만 모든 동작 중지임을 명확히 한다.

## 3. 입력과 터치 영역

- 큰 숫자는40sp, 입력창104dp 높이로 한 번에 찾도록 한다. 화살표는 각각52dp 높이이며 ‘한 회 늘리기/줄이기’ 접근성 설명을 갖는다.
- 타이핑은 임시값으로 유지한다. 완료/적용 전에는 기존 값을 지우는 순간을0으로 저장하지 않는다. 숫자 오류에는 입력창에서 구체적인 범위를 안내한다.
- 입력이 바뀐 동안에만 적용 버튼을 보여 ‘입력한 값이 아직 확정되지 않았다’는 단서를 준다. 키보드의 완료로도 같은 동작을 할 수 있다.
- 화살표는 유효한 입력에서 한 단계씩 즉시 적용한다. 범위 끝에서는 더 진행하는 버튼을 비활성화한다.
- 초 입력은 횟수와 별도 `SecondsEditor`이다. 숫자30sp/높이64dp, −/+는 각각52×64dp이며 ‘대기 시간 1초 줄이기/늘리기’ 설명을 붙인다. 초기10·범위5~60을 명시하고 부호·소수·빈칸·범위 밖은 오류로 거절한다.
- 초 초안은 상태 갱신으로 덮어쓰지 않는다. 완료/‘입력한 시간 적용’/Enter를 지원한다. 시간제 ON은 유효한 초 입력이 필요하며, 전체 실행 ON은 N>0·Instagram 선택·시간제 ON인 경우에만 시간제 초안을 검증한다. 라이브 ON 또는 전체 실행 ON의 활성 라이브 옵션은 반복 횟수와 무관하게 라이브 초안을 검증한다. 광고 전용 0회 사용을 무관한 시간제 초 오류로 막지 않는다.
- 앱 체크 항목은 최소56dp 높이, 라디오 항목은 최소64dp, 기본 버튼은 최소48dp이다. 작은 체크 그림만 정확히 겨냥하지 않아도 행을 눌러 조작할 수 있다.
- 라디오 행은 전체 가로폭을 터치 대상으로 한다.0.2.0에서 짧은 문구 오른쪽 빈 영역이 반응하지 않는 문제를 수정하고 재시험했다.
- 인앱 실행 스위치는56dp 높이 영역에 놓고 상태 설명 옆에 고정한다. 긴 설명은 스크롤하고 실행 제어는 같은 위치에 남는다.
- 앱 전환·권한 부족·0회·실행 OFF를 서로 다른 문구로 설명한다. ‘ON’ 하나로 실제 자동 스와이프가 진행 중이라고 오해하지 않게 한다.
- 완료/Enter 후 비조작 컨테이너 전체가 회색 강조되는 문제는 기본 포커스 강조를 해제해 수정·재시험했다. 편집 포커스와 실제 선택 상태가 혼동되지 않게 한다.
- 광고·시간제·화면 분석 옵션 복원/주기 갱신에는 rendering guard를 적용한다. 실행·횟수가 바뀌거나 화면 분석 동의창이 자동으로 열리지 않게 한다. Instagram 미선택 또는 Android 버전 미달에서 비활성화된 옵션의 저장값은 유지한다.

## 4. 플로팅 크기 절충

기존124×56dp에서72×56dp로 약42% 폭을 줄였다. 숫자 영역48×56dp를 유지하면서 오른쪽 위 X는24×24dp로 축소했다. X 아래 빈 부분까지 숨은 외곽 클릭 영역으로 확장하지 않는다.

일반적인48dp 터치 목표를 X에는 충족하지 못한다. 폭 축소와 터치 편의 사이의 절충이며 ‘접근성 목표 완전 충족’으로 보고하지 않는다. 종료를 작은 X에만 의존하지 않도록 인앱 실행 토글과 빠른 설정을 함께 제공한다.

배경 알파128/255와 창 알파0.8의 곱으로 배경은 약40% 불투명도를 의도한다. 숫자·X는 창 알파에 따른 약80%로 더 선명하게 유지한다. 실제 영상 밝기와 움직임에 따른 읽기 쉬움은 별도 확인한다. 99/99는 자동 글자 크기 조절을 사용하므로 작은 글자로 줄어드는 조건을 특히 검사한다.

정상 영상은 C/M, 시간제·라이브 지연은 남은 ‘초’, 0회 별도 옵션은 ‘광고’·‘라이브’·‘광·라’를 표시한다. 터치 방식 이름은 ‘횟수 순환’과 ‘반복 켜기·끄기 · 광고·라이브는 별도’다. 0↔N은 일반 반복·시간제만 조절하며 광고·라이브까지 멈추려면 ×/전체 실행/타일 OFF를 사용한다.

## 5. 사용한 디자인·구현 방법

- Figma 편집·보드·프로토타입은 사용하지 않았다. Figma로 제작했다거나 전문 도구의 자동 감사를 통과한 것처럼 표시하지 않는다.
- 아이콘 제작에는 이미지 생성 도구를 사용했다. 생성 시안과 실제 런처 자산 적용·마스크/소형 가독성 검증은 구분한다. 0.2.5에서는 기존 자산을 유지한다.
- 화면은 Java/native Android로 직접 구현한다. `UiTheme`는 색상·카드·여백, `SettingsScreen`은 배치, `CountEditor`는 횟수, `SecondsEditor`는 초, `MainActivity`는 설정/상태·동의 연결을 담당한다. `CompatibilityPanel`은 OS 안내, `UpdatePanel`/`UpdateController`는 업데이트 표시와 사용자 동작을 분리한다.
- 별도 웹 목업의 모양만으로 품질을 판단하지 않고, 빌드한 APK의 실제 화면과 터치·키보드 동작으로 확인한다. 전문성은 도구 사용 명칭이 아니라 근거 있는 배치·상태 설명·실제 검증으로 평가한다.

## 6. 영향 범위별 감사 항목

| 관점 | 확인할 항목 | 문서 작성 시 상태 |
|---|---|---|
| 기능 | 입력/완료/적용/▲▼,0·99 경계,100 오류, 두 터치 방식 |0.2.0 실기기 PASS |
| 일관성 | 플로팅 OFF 시 실행 유지, N/M·0회 의미 |0.2.0 표시OFF·실행 유지 PASS는 과거 근거. 새0회 광고 독립 결과는 최신 검증 기록 참조 |
| 시각 | 커버 화면 기본 구성·포커스 강조, 펼친 화면·긴 문구·큰 글꼴 | 커버의 회색 포커스 강조 수정/재시험 PASS; 펼친 화면·큰 글꼴 미실행 |
| 인간공학 | 라디오 빈 행, X/드래그·위치 저장 |0.2.0 수정 후 라디오 빈 행 및 드래그·X·위치 시험 PASS |
| 글꼴/키보드 | 기본 숫자 입력/완료, 큰 글꼴,99/99 | 기본 입력·완료 시험 PASS; 큰 시스템 글꼴 미실행 |
| 권한 | 자동 넘김의 접근성/선택형 플로팅, 업데이트의 인터넷/설치 연결 권한 분리, 부족한 준비 안내 |0.2.5 실제 화면·권한 거절/복귀 결과는 검증 기록 참조 |
| 회귀 | 기존 YouTube 완주·상향 넘김·위치·타일, Instagram 앱간 분리 |v021c YouTube43→5→43초 N1, 타일OFF/ON색상·서비스상태, IG미선택 대기 PASS. 전체 조합은 검증 기록 참조 |
| 과거 광고 UX | 당시 OFF 대기→ON 이동, CTA 미클릭, 일반 릴스 재개,0회 중지 |v021b 같은 광고 OFF6표본/ON1회 이동·일반 릴스N1·당시0회 광고 중지 PASS; v021c 추가 광고요청1/확인1→20.454초 일반릴스 재개 PASS. 새0회 광고 동작의 PASS 아님 |
| 광고 오인식 | 캡션의 `#광고`만으로 즉시 넘기지 않기 |v021b26.799초 일반 릴스 완주 후37.313초 다음으로 이동, 광고요청 추가 없음: PASS |
| 0.2.4 시간제 | 10초 기본·5~60·±1·오류 입력·정상N 우선·0회 중지 |후보 B의60 저장/상한+차단/61 거절/10 복구·정상N 유지·시간제 이동은 확인. 모든 중단/복귀·최종 빌드 결과는 검증 기록 참조 |
| 0.2.4 광고 독립 | 0회 광고만, 전체OFF 전부 중지, 플로팅/타일 상태 |최종0.2.4 기기 결과는 검증 기록과 릴리스에서 확인. 과거0회 광고 중지 시험 재사용 금지 |
| 0.2.4 분류 | 별도 카드·단위·광고전용 예시·실험 분리·하단 메인 |코드 라벨/문서 정합 확인. 최신 실제 화면·터치·큰 글꼴 결과와 구분 |
| 0.2.5 라이브 | 기본OFF,0초 즉시/60초 상한/61 거절,±1,반복0 독립,전체OFF·중단 취소,상태 표시 | code21 구현 계약. 실제 이동·최종 검증 결과는 VERIFICATION 참조 |
| 0.2.5 메뉴 | 횟수·시간제·광고·라이브 우선, 준비 바로가기, 고정 실행·키보드 가림·포커스 |구현 기준을 문서화. 최종 기기 검증 결과 기록 전 |
| 0.2.5 업데이트 | 새 버전만 배너, 조회 OFF/수동 확인, 진행률/취소, 검사 실패, 설치 허용 거절/복귀·최종 확인 |구현 기준을 문서화. 최종 기기 검증 결과 기록 전 |
| 0.2.5 설치 안전 | 설치 진입 실행 OFF, 다른 설정 보존, 취소·복귀 시 자동 설치/실행 금지 |순수 시험과 실제 설치 검증을 분리하여 VERIFICATION에 기록 |

순수 정책·저장소 자동시험은 터치 위치나 글자 잘림을 확인하지 않는다. 데스크톱1920×1080 브라우저 UI는 이 네이티브 앱의 적용 대상이 아니며, 대신 폴드 커버/펼친 화면과 관련 좁은 화면·큰 글꼴을 우선한다. 실제 수행 여부와 결과는 이 표를 PASS로 일괄 변경하지 않고 검증 문서에 증거와 함께 기록한다.

v021b 단계 자동시험145개 PASS와 위 실기기 증거를 구분한다. 이후 독립 검토에서 이동 확인 중 드래그·화면 구성 변경으로 gate가 취소되어 중복 요청될 수 있는 위험을 찾아 안전 정지 경로로 보완했다. 최종v021c 빌드·직접JUnit148·정적연결·독립 재리뷰 PASS, 설치·재추출 해시 일치. YouTube N1과 타일 OFF/ON 실제 회귀 PASS. 새 광고 안내 문구는 실제 화면에서 읽을 수 있음을 확인했고 별도 PiP가 겹치는 것은 외부 창으로 구분했다. 실제 pending 순간 회전/드래그는 미실행이다.

광고는1회 이동 요청 후 최소1.2초 이후 전환 확인,4.5초 미확인 시 안전 정지하는 구조이다. 같은 식별값의 연속 광고나 확인 도중 조작/화면 변경에서는 안전 정지가 가능하므로 ‘모든 광고 자동 처리’라는 UI 약속을 하지 않는다. 폴드 펼침·큰 시스템 글꼴은 여전히 미실행이다.

과거0.2.4의 정상·시간제·광고 합계20연속 검증은 미완료였으며 그 과거 결과를 PASS로 바꾸지 않는다. 미연결 `VisualSequenceTracker` 20시험 중2실패는 원본을 보존하고 제품 앱·제품 시험에서 제외한다. 기존 선택형 화면 분석을 제거했다는 뜻도, 제품 시간제가 실패했다는 뜻도 아니다. 별도 오디오 시험 APK는 제품 배포에 첨부하지 않는다. 과거 증거는 [0.2.4 릴리스 기록](releases/v0.2.4.md), 최신 최종 검증·게시 결과는 [VERIFICATION](VERIFICATION.md) 참조.
