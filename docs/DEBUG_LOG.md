# 디버그·재발방지 대장

## D-043 · 분할 화면 상태/대기 입력 분리 / Split-screen isolation and deferred input

현재0.3.0/code32는2026-08-28 공개 완료했다. 최종 APK는742,854bytes/SHA256 `9AA1E88425206CF1B9CEFBCD55B722DF83822D2F00830C81DDF925981AF394AA`이며,아래 실기기 흐름 후보53FD와 ZIP 내부의 소스 revision 기록만 다르고 나머지 항목은 동일하다. 최종 APK의 같은3개 OS native 검사·실폰 덮어 설치/해시·설정 보존·접근성 연결·전체OFF,CI와 익명 공개3파일 동일성 검증을 완료했다. 아래 과정별 후보·실패·미실행 범위를 보존한다. [최종 검증 원장](VERIFICATION.md),[공개 기록](releases/v0.3.0.md).

EN: Public0.3.0/code32 was released on2026-08-28. The final742,854-byte APK above differs from the53FD physical-flow candidate only in its embedded source-revision record;all other ZIP entries match. Same-APK three-OS checks,physical upgrade/hash/preferences/accessibility/masterOFF,CI and anonymous three-asset parity are verified. Candidate history and unrun limits below remain distinct;see the linked final records.

- 증상·재현: 기존 서비스는active root·카운터·타이머·플로팅이하나여서초점없는창을추적하지못했다. 새구조검토에서설정창을자기플로팅으로오인하는조건,0회플로팅위치갱신누락,대기중콘텐츠키변경무시,실행직전창변경정착대기우회를발견했다. 뒤4개는정적/합성재현이며실폰오발관측이라고보고하지않는다.
- 직접원인: 단일전역세션,초점창전용필터,windowtitle만으로overlay구분,0회earlyreturn,일반identity만비교,재조회후generation/hold누락.
- 영향: 두앱count혼합·플로팅오표시·가려진영역입력또는다른영상에이전대기요청적용위험.
- 잘못된접근: 단순히isFocused검사만제거하거나좌표를대기큐에넣거나비동기콜백중전역세션을교체하는것.
- 수정: HostPlaybackSession2개/SettingsStore.forHost/독립RuntimeState,순서독립windowguard,자기overlay는TYPE_SYSTEM/ACCESSIBILITY_OVERLAY와자기package확인,0회도읽기전용창갱신,3초대기상한+contentIdentity/photoPageKey/window/page검증,일반넘김전후generation/hold/경계확인,lease별직렬화. 실패는해당host만정지한다.
- 자동예방: ActionArbiterTest4,WindowPolicyTest8,HostSessionIsolationChecks,DeferredSessionChecks309,HostOverlayChecks,기존사진/복구시험이관및정적가드갱신. 도움말native검사는확장안내전체문자열을찾도록수정했으며줄바꿈/잘림검사는유지한다.
- 재시험: 1차546JUnit/3OSnativePASS,추가대기309검사포함후보API34 native27754PASS. 실제비초점YouTube진행값갱신확인. 두앱자동넘김과배치변경은시험중이며[현재인수인계](../HANDOVER.md)가최신상태다.

EN: Single active-window state was replaced by independent host sessions and serialized input. Review caught four conditional hazards:own settings mistaken for an overlay,stale zero-count placement,changed content accepted by a deferred request,and geometry changes bypassing settlement. Regression tests retain fail-closed behavior and verify lease,window,page and sibling-state isolation. Physical concurrent auto-advance remains under validation.

### 실기기 후속 발견 · 창 경계·자기 플로팅·창 제어 / Physical follow-up: divider, own overlay and window controls

1. **분할 경계 손잡이의 정상 창 차단**
   - 증상·재현·증거: Samsung 분할 화면의 비초점 `TYPE_SPLIT_SCREEN_DIVIDER(5)`가 두 앱 경계에 각각 약32px 겹쳐, 두 세션 모두 `screen.other_window`로 대기했다. 이는 비초점 영상의 진행값을 읽을 수 있다는 기존 관측과 별개인 창 안전 판정 문제다.
   - 직접 원인·잘못된 접근: 위 레이어의 겹치는 창을 일괄 차단하여 OS 경계 손잡이와 실제 가림 창을 구분하지 못했다. 분할 화면이라는 이유로 모든 겹침을 허용하는 수정은 적용하지 않는다.
   - 수정·영향 범위: `WindowPolicy`는 비초점 TYPE5가 대상 창의 경계에 닿고, 겹침 폭이 대상 폭의4% 이하이거나 겹침 높이가 대상 높이의4% 이하인 경우만 예외로 둔다. 면적4% 기준이 아니다. 초점 있는 손잡이, 넓은 겹침, 창 내부의 가림, 유사한 다른 유형의 창은 계속 차단한다.
   - 자동 예방: 세로 분할의 양쪽 경계, 가로 분할, 초점·넓은 가림·내부 가림·다른 유형의 부정 조건을 검사하는 JUnit 메서드3개를 추가했다.

2. **자기 플로팅의 표시·숨김 반복과 카운트 초기화**
   - 증상·재현·증거: 실제 자기 플로팅은 TYPE3이고 root가 존재하며 프레임워크가 제공한 노드 package도 자사와 정확히 일치했지만, 창 제목은 `ShortsLoop`로 시작하지 않았다. 플로팅이 표시·숨김을 반복하면서 카운트가 초기화되었고, 해당 관측 동안 자동 넘김 요청은0건이었다.
   - 직접 원인·잘못된 접근: 표시용 창 제목까지 소유 판정에 요구하여 정상 자기 플로팅을 다른 가림 창으로 처리했다. 제목은 기기 구현에 따라 달라질 수 있으므로 소유 증거로 사용하지 않는다.
   - 수정·영향 범위: `YouTubeWindowGuard`의 제목 검사를 제거했다. `WindowPolicy.ownOverlay`는 비초점 TYPE3 또는 TYPE4이면서 root 노드의 package가 `com.fullmetalsonic.shortsloop`와 정확히 같은 경우만 자기 플로팅으로 판정한다. TYPE1 자사 설정창, 초점 있는 창, root/package 누락, 비슷한 접두어의 다른 package는 예외에 포함하지 않는다. 실제 가림 창·설정창 보호는 유지한다.
   - 자동 예방: TYPE3/4 허용과 TYPE1·초점·null·빈 값·유사 package·다른 package 거부를 확인하는 JUnit 메서드1개, assertion8개를 추가했다.

**두 수정의 후속 재시험:** JUnit552개와3개 OS의 native 검사를 통과한 후보를 설치했다. 양쪽 앱의 진행값이 동시에 증가했지만 각 첫 일반영상 제스처 직후 창을 유효하게 읽지 못하고 안전 정지했다. 따라서 PC/native 통과를 실제 듀얼 자동 넘김 성공으로 해석하지 않는다. 추가 원인은 아래3번이며, 최신 결과는 [인수인계](../HANDOVER.md)와 [검증 기록](VERIFICATION.md)에 구분해 기록한다.

EN: Two physical-device blockers were isolated. First, an unfocused Samsung TYPE5 split divider overlapped each pane edge by about32px, causing both sessions to wait with `screen.other_window`. The exception now requires an unfocused TYPE5 touching the pane boundary and overlap no greater than4% of the corresponding pane width or height, not area. Focused, broad, interior and other-type covers remain blocked;three JUnit methods cover these conditions. Second, the app's real TYPE3 floating window had a non-null root and the exact framework-provided app package, but its title did not start with `ShortsLoop`. Repeated hiding/showing reset counting;no automatic request occurred during that observation. Title matching was removed. Ownership requires an unfocused TYPE3/4 and the exact root-node package;own TYPE1 settings windows and missing/lookalike packages remain blocked. One JUnit method adds eight ownership assertions. The subsequent552-test candidate passed unit tests and native checks on three OS versions. After installation, both playback clocks advanced, but each first ordinary gesture was followed by invalid-window safety stopping. This is not a physical dual-auto-advance pass;the third issue follows.

3. **첫 입력 뒤 나타나는 작은 OS 창 제어 손잡이**
   - 증상·재현·증거: 위552검사 후보에서 양쪽 진행값은 함께 증가했으나 각 첫 일반영상 제스처 직후 유효 창 번호가 없어져 안전 정지했다. 구조 조사에서 초점이 옮겨진 앱 영역 상단에 작은 비초점 TYPE7 창이 새로 나타나는 것을 확인했다. 제스처 요청·실행과 다음 콘텐츠 확인 성공은 구분한다.
   - 직접 원인·공식 근거: 기존 창 정책은 TYPE7을 알 수 없는 가림 창으로 처리했다. Android는 API36부터 값7을 연결된 창을 조작하는 시스템 창 유형인 `TYPE_WINDOW_CONTROL`로 정의한다. 이 정의 자체가 모든 TYPE7을 안전한 작은 손잡이로 보장하지는 않는다. [Android API reference](https://developer.android.com/reference/android/view/accessibility/AccessibilityWindowInfo#TYPE_WINDOW_CONTROL)
   - 당시 첫 수정·영향 범위: 초기 `WindowPolicy.captionHandle`은 비초점 TYPE7이 대상 창 안에 완전히 들어오고, 전체가 상단12% 안에 있으며, 너비가 창 너비의25% 이하·높이가 창 높이의5% 이하이고, 수평 중심 차이가 창 너비의10% 이하인 경우만 허용했다. 이 치수·초점 조건을 벗어난 제어 메뉴와 같은 모양의 다른 창 유형은 제외했다. 제목이나 콘텐츠 문구는 사용하지 않았다. 이 초기 치수 규칙은 아래4번의 좁은 창 재현으로 보완했다.
   - 자동 예방: 양쪽 앱 영역에서 작은 손잡이 허용과 초점·다른 유형 거부, 확대·너비·높이·상단 위치·중앙 정렬 위반을 검사하는 JUnit 메서드2개를 추가했다. 이 TYPE7 단독 보완은554검사 예상 단계였으며, 아래 입력 경로 보완을 포함한 최신 후보와 구분한다.
   - 독립 검토에서 발견한 반례: 공식 유형·비율 수식·부정 조건을 읽기 전용으로 대조했다. 예외는 **앱 창** 기준이고 실제 제스처는 **영상 페이지** 기준이어서, 창 상단의 작은 영상 페이지에서는 허용된 손잡이와 일반영상 스와이프 경로가 겹칠 수 있었다. 이는 실기기 오입력 관측이 아니라 코드상 허용되는 합성 반례다. 손잡이 크기 제한만으로 ‘항상 입력 경로 밖’이라고 판단하지 않는다.
   - P2 후속 수정·코드 재리뷰: `WindowPolicy.inputClear`는 기존 창 안전 판정에 더해 비어 있지 않은 입력 경로가 현재 대상 창 안에 들어오는지 확인하고, 자기 플로팅·분할 경계·TYPE7 손잡이를 포함한 **모든 상위 레이어 창**과의 경로 겹침을 차단한다. `YouTubeWindowGuard.allowsInput`을 일반영상의 각 세로 경로 후보와 사진의 각 가로 경로 후보에 연결하고, 후보마다 현재 창 목록을 다시 읽는다. 좌표를 사용하지 않는 접근성 scroll action은 기존 창·페이지 보호를 유지한다. 소스 재리뷰에서 앞선 손잡이-경로 반례가 차단되는 연결을 확인했다. 합성 반례·사진 가로 경로·자기 플로팅·분할 경계·창 이탈·빠른 설정 보호를 다루는 JUnit 메서드3개를 추가했다.
   - 재리뷰의 추가 보완·해결: 창 이동·확대 뒤에도 이전 경로가 새 창 안에 남아 있으면 내부 포함만으로 오래된 페이지 위치를 걸러낼 수 없는 조건을 지적했다. 후속 `allowsInput`은 현재 검사 결과와 전달된 `expectedBounds`가 정확히 같은지도 요구한다. 일반영상·사진 가로 입력 모두 `fresh.windowBounds`를 전달하는 연결을 소스에서 확인했다. 따라서 손잡이-경로와 창 경계 동일성의 두 코드상 P2를 해결한 것으로 검토하며,실기기 오입력 재현을 주장하지 않는다.
   - 이전557검사 후보·제한된 실기기 재시험: 설치 APK742,174bytes/SHA256 `5178270548FE2F18DED435C275D3735119475F90E63DD3C2EAF197709EB72199` 일치,BUILD·JUnit557·STATIC PASS. 2026-08-28 20:23:55–20:26:12의 Instagram 왼쪽·YouTube 오른쪽 구간에서 YouTube5/5(일반4·긴 영상1),Instagram5/5(일반3·긴 영상2) 자동 전환을 확인했다. 수동 이동0,종료 시 대기 요청0·차단0·복구0이었다. 전체OFF 후 시스템 위치 교환·ON에서 양쪽 감지·플로팅 위치 반영과 Instagram 추가3/3(긴 영상2·광고1)을 확인했다. 별도 구간을 합쳐 각 앱10연속으로 보고하지 않으며,모든 배치·전체 UI와 후속 시험은 아직 완료하지 않았다. 앞선552검사 후보의3개 OS 수치를 이 산출물의 결과로 재사용하지 않는다.
   - 같은 후보의 추가 확정 관측: 좌우 두 배치 구간의 합계는 YouTube10회·Instagram12회였다. 중간에 전체OFF→시스템 위치 교환→ON이 있으므로 각각10/12회 무중단 연속시험은 아니다. 이어 Instagram 플로팅X로 해당 앱만 정지한 뒤 YouTube 카운트·세대가 유지되고 추가1회 실제 전환하는 것도 확인했다. 이는 해당 관측 구간의 앱별 중지 분리 근거이며,아래 좁은 창 실패를 상쇄하지 않는다.

EN: After the552-test candidate's first ordinary gestures, a small unfocused TYPE7 control appeared near the newly focused pane's top and invalidated window qualification. Android defines TYPE_WINDOW_CONTROL=7 from API36 as a system control for an associated window;that definition alone does not establish a harmless caption handle. [Android API reference](https://developer.android.com/reference/android/view/accessibility/AccessibilityWindowInfo#TYPE_WINDOW_CONTROL) The initial exception required full pane containment,location within the top12%,width≤25%,height≤5%,horizontal center offset≤10%,and no focus;its size rules are superseded by item4 below. Two JUnit methods covered accepted and rejected shapes. Independent source review found a synthetic gap:window-relative clearance did not guarantee separation from a gesture based on a smaller page near the top. The subsequent inputClear policy checks current window qualification and corridor containment,then rejects overlap with every higher-layer window,including observation-exempt overlays and handles. Each ordinary vertical and photo horizontal candidate re-reads the windows through allowsInput;semantic scrolling retains its existing non-touch guards. Three more JUnit methods cover these paths. The final allowsInput also requires current bounds to equal expectedBounds,and both touch callers supply fresh.windowBounds. Source re-review closes both identified P2 conditions;neither was an observed device misgesture. The742,174-byte candidate identified above passed installation-hash matching,BUILD,557 unit tests and static checks. Its first physical segment completed5/5 transitions per host(YouTube4 ordinary+1 long,Instagram3 ordinary+2 long),with no manual moves and no pending request,block or recovery at the end. After executionOFF,system pane swap andON,both hosts/floating positions followed the layout and Instagram initially added3/3(2 long+1 ad). The two left/right segments eventually totalled YouTube10 and Instagram12 transitions;the interveningOFF/swap/ON prevents claiming uninterrupted10/12 runs. After Instagram'sX stopped that host,YouTube retained its counter/generation and completed one additional transition. This scoped isolation evidence does not cancel the narrow-pane failure below or complete all-layout/full-UI verification.

4. **좁은 창에서 고정 크기 손잡이를 다시 가림 창으로 오인**
   - 증상·재현·증거: 위557검사 후보에서 Instagram 영역을 폭900px로 좁히자,같은 형태의 비초점 TYPE7 손잡이가 실제249×66px로 유지되었다. 첫 일반영상 제스처 뒤 다시 안전 정지했으며,창 메타데이터 실측으로 크기 조건 실패를 확인했다. 원시 좌표·콘텐츠·기기 식별자는 공개 기록에서 제외한다.
   - 직접 원인·잘못된 접근: 앱 창은 좁아져도 OS 손잡이는 같은 픽셀 크기로 남아,너비25% 한도225px를 초과했다. 정상 배치 한 가지의 상대 너비만으로 고정 크기 시스템 손잡이를 제한한 것이 원인이며,모든 TYPE7을 허용하는 방식으로 우회하지 않는다.
   - 현재 수정: 긴 변 `L=max(창 너비,창 높이)`을 함께 사용한다. 손잡이 너비는 `min(창 너비80%,L18%)`,높이는 `min(창 높이10%,L5%)` 이하이며,전체가 창 상단으로부터 `min(창 높이25%,L12%)` 이내에 들어와야 한다. 창 내부·비초점 TYPE7·수평 중심 차이10% 이하를 유지하고 너비/높이 비율2 이상을 추가한다. 치수·위치·초점·유형 조건을 벗어난 제어창은 계속 차단한다. 이 기하 조건을 모든 시스템 메뉴의 의미 분류라고 주장하지 않는다.
   - 안전 보호·독립 검토: `inputClear`의 모든 상위 레이어 창과 실제 입력 경로 교차 차단,`expectedBounds`와 현재 창 경계의 정확한 일치 검사는 그대로 유지된다. 일반영상·사진 가로 입력의 연결도 재확인했다. 규칙 보완의 읽기 전용 독립 리뷰에서 새 P1/P2는 확인하지 않았다.
   - 자동 예방: 좁은 창의 실측 형태와 회전 후 위/아래 창의 형태를 재현하는 JUnit 메서드2개를 추가했다. 나머지 조건을 만족하되 가로세로비만2 미만인 합성 손잡이 거부 assertion도 추가하여 새 비율 분기를 직접 검사한다. 합성 회전 시험을 실제 폰90도 회전 시험으로 간주하지 않는다.
   - 이전559검사 후보·확정 범위: APK742,334bytes/SHA256 `818AC00DECEE2C9C42AD4C668F4622561E030E2B2D44BAD221AB54707789E1A4`,BUILD·JUnit559·STATIC PASS. 같은 APK의 API26/33/34 native28,033/28,043/27,788검사도 PASS했다. 좁은 창 실기기 구간에서 YouTube1/1·Instagram8/8(일반1·긴 영상6·광고1)을 확인했다. 이후 최초90도 회전·상하 분할 구간에서는 YouTube 신규 전환0,Instagram 추가2/2(긴 영상1·일반1)였다. YouTube는 아래5번의 상태표시줄 오인으로 막혔으므로 회전 시험 전체를 PASS로 보고하지 않는다. 서로 다른 배치 구간과 앞선 후보의 성공 수치를 합쳐 새 후보의 무중단 시험으로 만들지 않는다.

EN: On the preceding557-test candidate,a900px-wide Instagram pane retained the same249×66px unfocused TYPE7 handle. Its width exceeded the old25% cap(225px),and the first ordinary gesture was again followed by safety stopping. Measured window metadata confirmed the size mismatch. The revised rule uses long sideL=max(pane width,pane height):width≤min(80% pane width,18%L),height≤min(10% pane height,5%L),and the entire handle inside the top min(25% pane height,12%L). Full containment,unfocused TYPE7 and center offset≤10% remain;aspect ratio≥2 is added. All-higher-layer touch-corridor rejection and exact expected/current bounds equality remain mandatory. Independent code review found no new P1/P2 in this revision. Two JUnit methods cover narrow and rotated upper/lower panes,and an aspect-only negative assertion covers the new branch. The742,334-byte candidate passedBUILD,559 unit tests,static checks and same-APK API26/33/34 native28,033/28,043/27,788 checks. Its narrow-pane segment confirmed YouTube1/1 and Instagram8/8(1 ordinary,6 long,1 ad). The initial physical90-degree/top-bottom segment added Instagram2/2(1 long,1 ordinary) but no YouTube transition because of the status-bar issue below. This is not an overall rotation PASS;do not merge different layouts or candidate results into an uninterrupted run.

5. **회전 후 짧아진 창에서 TYPE3 상태표시줄을 가림 창으로 오인**
   - 증상·재현·증거: 위559검사 후보의 최초90도 회전·상하 분할에서 YouTube의 새 자동 전환이 진행되지 않았다. 비초점 TYPE3 시스템 상태표시줄의 실측 두께는102px였지만,기존 대상 창 높이8% 한도는 약92px여서 정상 표시줄을 가림 창으로 판단했다. Instagram의 별도2회 전환 성공과 이 실패를 구분한다.
   - 직접 원인·잘못된 접근: 분할·회전으로 앱 창은 짧아져도 OS 표시줄 두께는 그대로인데,창 높이 하나에만 비례한 상한을 적용했다. 표시줄 전체를 무조건 허용하거나 입력 경로 보호를 제거하지 않는다.
   - 수정·영향 범위: `WindowPolicy.edgeBar`는 비초점 TYPE3 중 해당 경계를 완전히 가로지르는 표시줄만 검사한다. 가로 표시줄은 창의 좌우 양끝을 모두 덮고 위/아래 경계에 닿아야 하며,높이는 `min(창 높이12%,긴 변8%)` 이하여야 한다. 세로 표시줄은 창의 상하 양끝을 모두 덮고 좌/우 경계에 닿아야 하며,너비는 `min(창 너비12%,긴 변4%)` 이하여야 한다. 길이만 같고 위치가 어긋난 창,내부 가림,두꺼운 창,초점 있는 창과 다른 유형은 이 예외에서 제외한다.
   - 입력·재발 방지: 예외로 관측 가능한 표시줄도 실제 입력 경로와 겹치면 `inputClear`가 거부하며,최종 창 경계의 `expectedBounds` 일치 조건을 유지한다. JUnit 메서드2개로 회전된 짧은 창의 실측 두께·통과/거부 경계값·긴 변/창 비율 양쪽 상한·정렬·내부 위치·초점·다른 유형·가로/세로 표시줄과 경로 교차를 검사한다. 소스와 시험을 직접 대조했으며 독립 리뷰의 미해결 P1/P2는0건이다.
   - 이전DE7 후보·당시 검증 기록: APK742,318bytes/SHA256 `DE7F2803CD99D428D6AB15C63276C23B394DC3C4F8AF95281F3DD72131E3914C`,설치 해시 일치·BUILD·JUnit561·정적 가드 PASS. 같은 APK의 API26/33/34 native28,033/28,043/27,788검사 PASS. 이 체크포인트 당시 실폰 Instagram 위·YouTube 아래의 양쪽 카운트와 자동 전환을 재확인 중이었으며,후속 확정 구간은 [검증 원장](VERIFICATION.md)에 별도로 기록한다. 이전 후보의 좁은 창 또는 부분 회전 성공을 새 후보의 실폰 전체 PASS로 재사용하지 않는다.

EN: In the preceding559-test candidate's initial rotated top/bottom layout,YouTube made no new transition because an unfocused TYPE3 status bar was102px thick while the old8%-of-pane-height limit was about92px. OS bar thickness did not shrink with the pane. The revised edgeBar rule requires true full-span alignment at an outer edge. Horizontal bars must cover both left/right edges and have height≤min(12% pane height,8% long side);vertical bars must cover both top/bottom edges and have width≤min(12% pane width,4% long side). Shifted,interior,thick,focused and other-type covers remain outside the exception. Observation permission never permits touching the bar:inputClear still rejects every higher-layer corridor overlap,and expected/current bounds must match. Two JUnit methods cover measured geometry,both caps and boundary values,alignment,focus,type and touch rejection. Source/test review has no unresolved P1/P2 findings. The742,318-byte candidate identified above passed installation-hash matching,BUILD,561 unit tests,static guards and same-APK API26/33/34 native28,033/28,043/27,788 checks. Its physical retest with Instagram above YouTube is ongoing;final rotated-segment completion is not yet confirmed.

6. **분할에서 전체화면으로 돌아갈 때 상태표시줄 내부 TYPE3 손잡이 오인**
   - 이전 후보의 확인 범위: 문구 전용F582 후보에서 계산기/Instagram 구간6/6(일반3·긴 영상2·시간제1)과 숨겨진 YouTube 요청0을 확인했다. 이 성공 이후 실행·듀얼ON을 유지하고 Instagram을 분할에서 전체화면으로 바꾸자 `screen.other_window`로 대기했다. 병행 사용 성공과 배치 전환 실패를 별개로 기록한다.
   - 직접 원인·증거: 작은 시스템 손잡이가 정상 상태표시줄 안에 있었지만 TYPE7이 아니라 별도 TYPE3 창이었다. 손잡이 자체는 화면 가장자리를 완전히 가로지르는 표시줄이 아니므로 기존 `edgeBar`에도 해당하지 않았다. 프레임워크 root의 정확한 SystemUI package를 확인했다. 임의 제목·콘텐츠 추정으로 예외를 넓히지 않는다.
   - 수정·영향 범위: `WindowPolicy.systemHandleInsideBar`는 손잡이와 포함하는 표시줄 모두 TYPE3·비초점·정확한 `com.android.systemui` 소유일 때만 적용한다. 두 창은 서로 다른 ID이며 대상 앱보다 위 레이어에 있어야 한다. 손잡이는 기존 `captionHandle`의 작고 납작한 상단 중앙 기하 조건을 만족하고,별도의 기존 `edgeBar` 조건을 통과한 표시줄 안에 완전히 포함되어야 한다. 표시줄 부재·부분 포함·비슷한 package·초점·너무 큰 제어창은 제외한다. `YouTubeWindowGuard`는 root package를 복사해 메타데이터로 전달하고 노드는 해제하며 창 제목을 소유 근거로 사용하지 않는다.
   - 안전·자동 예방: 관측 예외가 생겨도 `inputClear`의 모든 상위 레이어 창과 입력 경로 교차 차단 및 `expectedBounds` 일치 검사는 유지된다. JUnit 메서드3개로 정상 포함/경로 차단,표시줄 없음·소유 불일치·낮은 레이어·초점·다른 유형,부분 포함·확대,정확한 SystemUI package와 이전 메타데이터의 비신뢰 기본값을 검사한다. 소스·시험을 직접 대조했고 독립 리뷰에서 새 미해결 P1/P2는0건이다.
   - 최신 후보·검증 범위: APK742,854bytes/SHA256 `53FDA56552ECC4B2D309BF38AD2E0072301866E813C0E445B7B328F6DA029C2A`,BUILD·JUnit564·정적 가드·같은 APK의 API26/33/34 native28,043/28,053/27,794검사·설치 해시 일치 PASS. lint0오류/13경고,범위별 독립 리뷰 미해결P1/P2는0건이다. 실폰에서 실행·듀얼ON을 유지하며 Instagram 전체화면3/3(광고1·일반1·긴 영상1)→Instagram 왼쪽·YouTube 오른쪽 분할에서 추가YouTube4/4(일반3·긴 영상1)와Instagram1/1(일반)→Instagram 전체화면 복귀 후 추가9/9(일반4·긴 영상4·광고1)를 확인했다. 마지막 구간에서 숨겨진 YouTube의 요청·확인은4로 유지됐다. 게시 준비 전 전체 실행을OFF로 두었다. 같은 구간을 앱별10연속 성공으로 확대하지 않는다. 이번 후보는 제품 가드 변경이므로 이전F582/DE7과 동일 실행 코드나 동일 APK라고 보고하지 않는다.
   - 계약·한계: 듀얼ON·실행ON을 유지하면 전체화면 한 대상·분할 두 대상·일반 앱 옆 한 대상을 보이는 창에 따라 감지하는 방향이다. 명시적 중지·서비스 재연결·권한·키보드/팝업·앱 자체 일시정지 보호는 유지한다. 현재 구간의 성공을 모든 앱·배치·듀얼 사진의 보장으로 확대하지 않는다. 정확한 최종 물리 결과는 [검증 원장](VERIFICATION.md)을 따른다.

EN: The earlier wording-onlyF582 candidate completed6/6 Calculator/Instagram transitions(3 ordinary,2 long,1 timer),with no hidden-YouTube request. A subsequent split-to-fullscreen change with execution/dualON exposed a separate small TYPE3 SystemUI handle wholly inside the normal status bar. It was neither the known TYPE7 control nor a full-span edge bar,so window qualification rejected it. The new systemHandleInsideBar exception requires exact SystemUI provenance for both distinct,unfocused TYPE3 windows above the target. The small handle must meet caption geometry and be wholly contained in a separately qualified edgeBar;missing/partial/untrusted/focused/oversized alternatives are rejected. YouTubeWindowGuard copies the framework root package into metadata and releases the node,without trusting titles. All higher-layer touch-corridor rejection and exact expected/current bounds remain mandatory. Three JUnit methods cover provenance,containment,negative cases and touch protection;source/test review found no new unresolved P1/P2. The742,854-byte candidate identified above passedBUILD,564 unit tests,static guards,same-APK API26/33/34 native28,043/28,053/27,794 checks and installed-hash verification,with lint0errors/13warnings. With execution/dualON throughout,physical confirmation comprised3 fullscreen Instagram advances(1 ad,1 ordinary,1 long),then4 YouTube(3 ordinary,1 long) and1 Instagram(ordinary) advances in split view,then9 more Instagram advances(4 ordinary,4 long,1 ad) after returning to fullscreen. Hidden YouTube remained at4 requests/confirmations. Execution was then turnedOFF for publication preparation. This is neither ten consecutive transitions per host nor payload equivalence withF582/DE7,and does not guarantee every layout or dual-photo case.

## D-042 · 사진 장 번호 조회·정상 화면 오인 / Photo index tree and false interaction guard

- 증상: 사진은 감지하지만 한 장 모드에서 `photo.index_missing`으로 대기했다. 확장 조회를 적용한 첫 후보에서는 정상 사진 화면을 `screen.interaction`으로 차단했다.
- 재현·증거: 동일 화면의 compressed tree61개 노드에는 장 번호가 없고 full tree104개 노드에는 전용 TextView의1/2가 있었다. 확장 트리에 추가되는 `bottom_sheet_camera_container`는 자식이 없는 비상호작용 FrameLayout이었다. 원본은 비공개 보존한다.
- 직접 원인: 조사 도구의 확장 조회와 제품의 중요 노드 전용 조회가 달랐고, 기존 `bottom_sheet` 부분 문자열 차단이 빈 외곽 컨테이너까지 메뉴로 판단했다.
- 잘못된 접근: 조사 XML에서 읽힌 장 번호가 제품에서도 그대로 제공된다고 가정했다. 빌드·정책시험만으로 실제 감지 성공을 판정하지 않는다.
- 수정: 실행ON·선택Instagram·사진옵션ON에서만 확장 조회하고 새 트리를 다시 읽는다. 정확한 카메라 외곽ID/FrameLayout/자식0/비중요·비클릭·비포커스·비편집/빈text·description만 예외 처리한다. 실제 댓글·메뉴와 자식 감시는 유지한다.
- 추가 검토: 사진의 같은 media component 아래에 남는 빈 영상 자리표시자는 허용하되 여러 개·다른 부모·자식 있는 영상·상호작용 요소는 제외한다. 정상 자리표시자의 작성자/재생 description은 영상 재생 증거로 쓰지 않는다.
- 재무장 보호: 사진 세로 전환은 다른 안정된 게시물 식별값과 별개로 다른 media source-node 식별값을 요구한다. 캡션의 지연 표시·부분 소실만으로 성공 처리하지 않는다. 최초 직전 노드 serial 방식의 A→B→A 키 변경 반례를 검토에서 발견하여 창번호+Android노드해시의 무상태 키로 교체했다. 동일 노드는 동일 키이며 충돌·재사용·메타데이터 부재는 보수적으로 대기/정지한다. 노드 객체나 콘텐츠는 보관하지 않는다. 확인된 사진/영상 광고도 사용 가능한 페이지 키를 보존한다.
- 자동 예방: 사진조회 opt-in 조합, 빈 외곽의 부정 조건, 캡션만 변경/페이지 근거 롤백, snapshot 복사 보존, 사진 전용 적용 버튼과 기존 안전 연결 시험을 추가했다. 최신 실제 결과와 산출물은 [검증 기록](VERIFICATION.md)을 따른다.
- 최신 실폰 재시험: 설치 APK 해시 일치 후 통째0/3/10초·한 장0/3/10초·반복0 독립·댓글 보호를 확인했다. 추가8장 한 장0초에서 가로7/7·세로1/1,한 장10초에서 가로1/1·세로1/1,0초·대체ON 댓글 보호에서 새 요청0. 신속20표본 탐색은 수동 이동을 포함하며 번호 없는 사진·사진 직후 광고·혼합 실제 사례는 미확보다. 새 제품 수정 없이 검사했고 두 시간3초·대체OFF·통째·반복1·실행ON으로 복원했다. 미관측 분기를 PASS로 기록하지 않는다.

EN: The debug tool included non-important nodes while the app did not. The observed slide index existed only in the expanded tree. Expanded queries also exposed an empty camera shell that the broad bottom-sheet guard rejected. Opt-in Instagram photo queries now reacquire the tree;only the exact empty,non-interactive shell is exempted,without bypassing descendant/menu checks. A same-media empty video placeholder is distinguished from a populated/mismatched video. Photo vertical confirmation also requires independent media source-node movement,so caption-only changes cannot rearm automation. Node reuse remains a conservative limitation. Regression and artifact-specific device evidence are linked above.

## D-041 · Instagram 사진 릴스 제외 / Photo Reel diagnosis, not implemented

**후속 구현:**0.2.9 로컬 후보에사진감지·통째/한장모드·선택형대체·전환확인·실패고정정지를추가했다.사진관련52개추가시험을포함한522JUnit과native입력·안전연결검사를수행했다. 최신 APK 실폰 두 모드·0/3/10초·댓글 보호를 확인했고 일부 예외 실제 사례는 미확보다. 아래는최초조사/설계기록이며현재상태는[사진설명](PHOTO_REELS.md)과[검증기록](VERIFICATION.md)을따른다. 게시보류다.

- 상태·범위: 2026-08-28 USB 구조 조사. 설치 ShortsLoop0.2.8/code30, Instagram444.0.0.46.85. 사진 전용 시간제는 아직 구현·설치·게시하지 않았다. 미게시0.2.9의 번역 변경과 별개이며 사진 제외 정책은 기존HEAD에도 있다.
- 증상·원인: 사진 게시물에서0/1로 대기. 구조 조회 전 서비스는 connected/enabled=true, blocked=false, `사진·혼합 릴스 · 대기`였다. `InstagramPolicy.unsupportedMedia`가 `clips_carousel*` 등을 제외하고 `InstagramReader`가 진행정보·광고·시간제 후보 판정 전에 반환한다. 기존 시간제5초 옵션이 켜져 있어도 적용 대상이 아니다. 진행정보를 못 읽어서 생긴 새 회귀가 아니라 기존 미지원 유형이다.
- 재현·증거: 같은 사진 게시물의 최초1/2, 이전 일반 영상 방문 후 복귀1/2, 가로 이동한2/2에서 각각 `clips_carousel_viewpager`와 `clips_carousel_image_media_content`가1개, 단일영상 요소·SeekBar가0개였다. 비교 일반 영상은 사진/carousel 요소0개, `clips_single_media_component`와SeekBar가각1개였다. 사진 첫 장으로 복원했다. 스크린샷·원본 트리는 비공개 보존하며 내용·계정·기기식별자는 공개 기록에서 제외한다.
- 해석·한계: 현재 설치 호스트의 해당 사진 carousel은 별도 분류 근거가 있다. 사진으로 편집한 동영상 파일, 다른 호스트 버전, 모든 사진/혼합 게시물까지 검증한 것은 아니다. 사진 화면 트리에도 별도 `clips_video_container`가 존재하므로 영상 노드가 하나라도 있다는 이유만으로 현재 콘텐츠를 영상으로 판정하면 안 된다. 현재 페이지·이미지 요소의 부모 관계와 가시성/안정 상태를 함께 검사해야 한다. 이전 서비스의 position/duration 캐시는 현재 사진의 재생시간 증거가 아니다.
- 검토안A: 사진으로 확인된 Instagram 릴스에 별도 켜기/끄기와0–10초 대기 설정.0은 꺼짐이 아니라 안전한 현재 페이지 확인 후 즉시 다음 **릴스**로 이동; 여러 장을 자동으로 가로 넘기는 기능과 구분한다. 일반영상의 반복 및 기존 진행정보 없음 시간제 계약은 유지한다. 기본값·반복0과의 관계 등 세부 계약은 구현 전 확정한다.
- 후속 요구·가능성: 통째로 다음 릴스 이동 / 사진 한 장씩 이동 중 하나를 선택하고 각 모드의0–10초 값을 별도 보존하는 구성을 검토한다. 현재 실기기 원본 트리의 `carousel_index_indicator_text_view`에서1/2와2/2를 읽을 수 있어 이 사례에서는 현재 장/전체 장 및 마지막 장을 구분할 근거가 있다. 가로 이동용 carousel은scrollable=false여서 접근성 scroll action 지원을 가정하지 않으며, 수동ADB 가로 스와이프 성공을 제품의 자동 가로 제스처 성공으로 보고하지 않는다.
- 마지막 장 보호안: 한 장 모드는 안정된 같은 게시물의유효한i/n을 요구하고,i<n이면가로이동→같은게시물의(i+1)/n확인→새장타이머,i=n이면마지막장대기후다음릴스이동→다른게시물확인 순서다. 최초안은번호누락시항상대기였으나아래선택형대체동작요구로보완한다. 번호/전체장수 모순·이미지유형불명확·전환성공미확인 때는 반복 가로 스와이프나 임의 세로 넘김 대신 기존 보호를 유지한다.0초도전환확인과화면안정검사를생략하지않는다. 손조작/댓글/잠금 보호와혼합콘텐츠 정책은 구현 전 구체화하며 마지막장→다음릴스 제품E2E는NOT RUN이다.
- 번호 확인 실패 시 선택형 대체 동작: 한 장씩 보기의 하위 옵션으로 ‘장 번호 확인 불가 시 통째로 넘기기’를 제공하는 요구를 추가한다. OFF이면대기,ON이면별도로저장한통째넘김0–10초설정을재사용한다. 사진 릴스로 확인된 안정된 같은 게시물에서 번호를 읽지 못하는 경우만 대상이며,댓글/메뉴/잠금/앱변경/사진유형불명확/미확인전환을 우회하지 않는다. 전환 중 잠깐 사라진 번호는 재확인하고, 대체 동작 진입 시 통째넘김 시간을 새로 계산하는 방향으로 검토한다.0초는안전확인후즉시이며새로운반복제스처허용이아니다. 설정UI·저장·동작·회귀시험은아직미구현이다.

EN: The per-photo mode gains a proposed optional fallback for an unreadable slide index:OFF waits;ON reuses the separately saved whole-Reel delay. It requires a confirmed stable photo Reel and cannot bypass comments,menus,unknown content or an unconfirmed transition. Brief index loss during movement is rechecked;starting a fresh whole-Reel timer on fallback entry is the proposed timing rule. This updates the initial always-wait design;implementation and automatic tests remain pending.
- 검토안B: 별도 판별이 불가능한 유형은 안전한 릴스 페이지임이 확인된 경우에 한해 진행정보 없음 시간제와의 통합을 검토한다. 댓글/메뉴/불명확한 화면까지 ‘진행정보 없음’으로 묶지 않는다. 현재 사례에는A안 근거가 있어B안으로 일괄 통합할 필요는 없다.
- 예방·미실행: 구현 시 사진→영상·영상→사진·사진→사진,0/1/10초,사진 가로 조작 중 중지,댓글/잠금/앱 전환,전환 확인 실패·중복 요청 차단을 시험해야 한다. 현재는 구조 비교만 완료했으며 신규 자동 넘김·회귀/연속 시험은NOT RUN이다.
- 진단 도구 영향: 첫 UIAutomator 구조 조회 뒤 서비스 인스턴스 재연결과 실행OFF를 확인했다(D-007과 같은 시험 간섭 가능). 이후 구조 비교는OFF 상태로 분리했다. 재생 중 일반 영상의 첫XML조회는idle 실패; 잠깐 일시정지하여 조회 후 재생을 복원했다. 임의 권한 변경·자동 재시작은 하지 않았으며 최종 서비스connected=true/enabled=false다. 구조 수집을 실제 자동 넘김 성공으로 보고하지 않는다.

EN: On the installed host, the two-image carousel exposes dedicated image/carousel elements on both slides and after re-entry; the adjacent ordinary video exposes single-video/SeekBar elements instead. Existing policy rejects the photo/mixed type before timer eligibility, so this is an unsupported-type boundary, not a localization regression. A separate opt-in0–10-second photo timeout is feasible to investigate;zero means immediate after safe page qualification, not disabled. This would advance to the next Reel, not cycle photos. Mixed media, encoded slideshow videos and other host versions remain unverified. No product change, installation, new automatic-transition pass or publication occurred. UIAutomator inspection was separated from automation after service reconnection; final execution is OFF.

## D-039 · 언어 표시와 내부 상태 결합 / Localization boundary

- 증상·재현: 기존 코드의한국어 상태를번역하면플로팅의정확한문자열비교가맞지않아상태표시가달라질수있음. 영어리소스가없는기존앱은시스템영어에서도한국어표시.
- 원인·증거: LiveSkipPolicy/LongVideoPolicy/RuntimeState의한국어출력과FloatingController의동일문자열분기. 직접번역만하는접근은표시/판단계약을분리하지못함.
- 수정·영향:93개고정중립코드→StatusText→KO/EN리소스. 호스트검출한국어/영어어휘는유지. 알려진업데이트오류만코드로번역,알수없는예외본문차단.
- 자동예방:verify-localization 리소스/서식/하드코딩가드,언어별오류검사,기존카운트/광고/라이브/안전정지회귀시험.
- 실제언어전환후속발견:Android가같은EditText값을복원해도TextWatcher가무조건dirty로처리하여적용버튼이표시됨.4입력에서저장값과문자열이같으면미편집,다르면임시입력유지로수정.언어별viewstate복원34검사로재발방지하며설정저장은발생하지않음.
- 재시험·최종결과:[0.2.9](releases/v0.2.9.md). 독립검토에서OS설정이한국어이름을선택할수있는데영문이름만찾으라는안내를발견해this app기준으로보완.

EN: Language-neutral status/error codes separate display from decisions. Bilingual host detection terms remain untouched. Resource/format guards and existing behavior tests prevent translation from changing automation;unknown exception text is suppressed. OS-owned labels may differ from the app language,so setup instructions refer to this app.

## D-040 · 큰 글꼴·좁은 뷰포트 잘림 / Large text and narrow viewport

- 증상·재현:320dp·글꼴2배native시험에서▲의텍스트높이가고정52dp버튼의내부높이를초과. 합성좁은창스크린샷에서본문컨테이너가창보다넓어양쪽잘림.
- 원인·증거:입력/화살표고정높이,SettingsScreen의전체DisplayMetrics폭기반고정컬럼. 기존텍스트자체레이아웃검사만으로는부모컨테이너가화면밖으로나가는것을검출못함.
- 잘못된접근:텍스트줄폭만검사하거나단순시험PASS를전체시각PASS로간주. getLineWidth의줄끝공백은보이는글자가아니므로실제잘림검사는getLineMax사용.
- 수정:입력/화살표는기존최소터치높이를유지하며내용에맞게증가,ContentFrame은실제측정된창폭에서여백을빼고600dp상한적용.
- 자동예방:KO/EN·320dp·글꼴1/1.5/2의실제native레이아웃·모든컨테이너좌우경계·본문/도움말/입력검사와스크린샷육안대조. 플로팅72×56dp는변경하지않음.
- 재시험·최종결과:[0.2.9](releases/v0.2.9.md). 일반실제411dp화면정상표시와좁은합성창문제는구분해서기록.

EN: Fixed-height numeric controls clipped at large fonts;display-based column width exceeded narrow viewports. Growing controls preserve minimum touch targets,and ContentFrame follows measured viewport width. Native text and all-container bounds checks plus visual review cover both languages. Trailing wrap spaces are excluded from visible-width checks. Final retest evidence is linked above.

## D-037 · 배포 상태·앱 라벨·문서 불일치 / Fixed in0.2.8

- 증상/재현: 공개0.2.7의업데이트메뉴와사용법아래하단을열면‘공개시험판’/‘시험판’표시. README일부는최신code29가아닌code28을현재로설명.
- 직접원인: 두UI문자열에시험판문구하드코딩. GitHub메타데이터/문서머리말변경은APK문자열을바꾸지않음. 기존서술과과거기록이여러문서에중복됨.
- 증거/영향: UpdatePanel20,SettingsScreen124,README24/25/38/40/228,UI_DESIGN5/7/9,USER_GUIDE21/31. 소스·실폰두화면·독립검토확인. 설치본교체시점이나업데이트캐시문제가아님.
- 잘못된기존접근: 공개flag와문서일부변경및APK해시동일성만으로표기정합성까지완료로판정. 업데이트패널존재검사에라벨검사가없음.
- 수정안: 앱이름/설치버전만표시하고정식판접미사도추가하지않음. 실제화면분석실험안내·과거시험판이력은유지. 현재안내와이력분리.
- 자동예방안: 정확한두버전라벨,예외가아닌전체앱시험판표시부재,문서현재버전및최종설치APK화면검사.
- 수정/재시험:0.2.8 두 라벨을 resources와BuildConfig 버전으로 구성하고 안정적인 view ID·실제설치버전 exact assertion을 추가했다. 현재/과거 문서를 분리했다. API26/33/34 및 휴대폰 두위치·펼친사용법 확인 PASS. 실제 실험 기능 안내 유지. [전수검토](RELEASE_PRESENTATION_AUDIT.md),[최종 기록](releases/v0.2.8.md).

EN: Fixed in0.2.8 with neutral resource-backed labels,exact installed-version assertions and current/history separation. Native and physical checks passed while preserving experimental disclosures and historical evidence.

## D-038 · 배포APK 디버깅 허용·배포 차단검사 부재 / Fixed in0.2.8

- 증상/재현: 공개0.2.7APK의aapt badging에application-debuggable,설치패키지flags에DEBUGGABLE. GitHub는prerelease=false.
- 직접원인: assembleDebug산출물을기존방식대로배포. prepare-release는서명/패키지/버전만검증하며debuggable을거절하지않음. CI도debug빌드검사만수행.
- 증거/영향: app/build.gradle,.github/workflows/android.yml,scripts/prepare-release.ps1과실제배포/설치APK. Android배포준비설정누락이며원격침해나새재생오류가관측됐다는뜻은아님.
- 잘못된기존접근: debug파일명/서명호환성과GitHub정식표시만구분하고배포APK내부debuggable검사를누락.
- 수정안: 기존패키지·서명신원·설정/업데이트호환을보존하는배포용debuggable=false빌드를구성하고해당산출물로검증. 임의키교체/앱삭제금지.
- 자동예방안: 배포스크립트에서debuggableAPK거절,CI배포빌드및검증,실제배포APK속성/서명/설치업데이트/해시확인.
- 수정/자동예방: release debuggable=false·동일 개인 서명은 환경변수로만 연결, CI unsigned release+debug 검사. 디버깅APK 출고 거절, 정확히1개 기존 signer 집합, 미커밋제품변경/내장revision 불일치 차단. 코드커밋 후 final폴더를 별도로 생성하여 초기후보와과거파일을 보존한다.
- 재시험:468JUnit·정적가드, debug APK 실제 거절, 세OS19설정·UID·서명 보존·디버깅OFF 덮어설치, 휴대폰 nondebuggable·접근성·해시 확인 PASS. 초기테스트baseline저장 UID오류는 테스트전용파일 경로로수정하여 재시험했다. 제품 QA우회는 추가하지 않음. [검토·공식근거](RELEASE_PRESENTATION_AUDIT.md),[최종 기록](releases/v0.2.8.md).

EN: Fixed with a non-debuggable release variant,preserved signing identity,exact signer/committed-revision guards and debug-APK rejection. Three-OS settings-preserving upgrades and physical release checks passed. A fixture-only storage-UID error was corrected without adding product QA bypasses. Final artifact/publication results are linked separately.

## D-036 · 작은 플로팅 ‘긴영상’ 잘림 / Compact long-video label clipping

- 증상·재현: 공개0.2.6의72×56dp 플로팅에서 ‘긴영상’ 첫 글자가 일부 잘림. 기존48dp 글자뷰와 singleLine 설정을 Android 계측에서 재현해 실제 글자 폭 초과를 확인.
- 직접 원인: singleLine이 가로 스크롤을 켜 autosize가 실제 폭 대신 큰 가상 폭을 사용. X 아래 빈 공간도 글자에 활용하지 않음.
- 확인 증거·영향 범위: 실제 TextView 측정과 실폰 화면. 카운트/넘김 실패가 아니라 플로팅 표현 영역 문제이며 긴 숫자·상태 라벨에도 같은 위험.
- 잘못된 기존 접근: 자동 크기 설정이 있다는 사실과 getText 문자열 일치만 확인. 실제 배치·glyph 경계를 검사하지 않아 시각적 결함을 놓침.
- 수정: 외곽72×56/X24×24 유지. 글자는 전체폭에 좌우6/위24/아래4dp, maxLines1·horizontalScrollingfalse·autosize8–21sp. X8–16sp. 표시 모듈 분리 및 설정변경 시 metrics 재적용, 상태/리스너/위치 유지.
- 자동 예방: 실제 FloatingContent의16개 상태 전환×5배율×LTR/RTL×기본/굵은 글꼴. 전체문자·line/ink 경계·X 비겹침·고정 크기 검사. 이전 결함 재현 assertion도 유지.
- 재시험: code29 빌드·468JUnit·정적 가드 PASS,lint0오류/기존3경고. API26/33/34 계측5568/5568/5567항목 PASS. 초기 분리된 시험뷰의 비동기 클릭 검사는 큐가 없어 실패했고, 제품 변경 없이 동기식 hit-routing fixture로 교정했다. 이후 실폰의 실제 탭/드래그/X를 따로 확인. 실폰 ‘긴영상’ 전부 보임·숫자 표시·설치 해시/설정/접근성 유지 PASS. 독립 리뷰 P1/P2 0건.
- 한계·상태: 로컬 설치 완료. 실시간 시스템 배율 변경과 비대칭 혼합 RTL 문자열 육안 순서는 미검증. [상세 검증](FLOATING_LAYOUT_FIX.md),[정식 배포 기록](releases/v0.2.7.md).

EN: Horizontally scrolling single-line text defeated real-width autosizing;string-only tests missed the visual defect. Code29 preserves outer dimensions and interactions while using the full width below X. Actual glyph/layout assertions,legacy reproduction and physical display/touch checks passed. The first detached-fixture asynchronous-click check failed and was corrected as a test-harness issue;real controller interactions were separately checked on the handset. See the linked reports for evidence,limits and stable publication.

## D-035 후속 · code27 행 번호 진단·code28 YouTube 한정 후보 / Native-row probe and targeted candidate

- 상태: code28 Public pre-release 공개·공개 파일 검증 완료. 최종468JUnit·빌드·정적 가드·API26/33/34 계측233/233/232 PASS,lint0오류/기존3경고.설치 후 전체 기존설정 비교보존·접근성 연결·런타임·해시일치 PASS다. YouTube20회는13:08:46~13:11:14.291/148.6초,요청20/확인20(일반4·긴15·라이브1),수동/실패/복구0으로 PASS했다.전후0~20화면을육안대조하고정확한행+1을관측했다. code26 Instagram10회 PASS는 유지하되 code28 재실행으로 쓰지 않는다.
- 고정 산출물:746246bytes,SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`.
- 공개 검증:제품commit/tag8dbcce3a5cd0cfa2931461773e58e12330de14b4/v0.2.6,CI33141470669 SUCCESS(32suites468tests·실패/오류/건너뜀0,lint0/2).13:21:42KST공개·13:22:04.764익명검증/Public·draftfalse·prereleasetrue·HTTP200,APK/SHA텍스트/JSON크기·해시일치.로컬lint0/3과구분한다.
- 독립 검토·남은 범위:이번변경·검증근거PASS,P1/P2 0.내비게이션오버레이실기기NOTRUN,경미한플로팅잘림유지.추가제품변경이나기기E2E없이게시동일성만확인했다.
- 실기기 재시험: **YouTube20 실기기 기록:**13:08:46~13:11:14.291,148.6초,기준0→20·요청20/확인20. 일반4·긴 영상15·라이브1,광고/시간제0,수동0·실패0·복구0이다. 전후 화면0~20을 육안 대조했다.10번은 들어온 라이브가 다음 동작으로 이동 중인 화면이고11번에서 다른 일반 영상이 확인됐다. 현재 행의 정확한+1을 관측했다.13번 관측 중 알림 배너가 나타났으나 이후 전환은 계속됐으며 **단일 사례이지 모든 알림에 대한 보증이나 과거 실패 원인 규명은 아니다**. 목표 이후 추가3회는20회 결과에 합산하지 않는다.
- 종료·복원:13:12 인앱 긴 영상30→60복원·런타임PASS,전체OFF·반복1·긴ON60·광고ON·라이브ON0·시간제ON10·화면분석OFF,blocked=false.
- 남은 조건·시각 한계: **남은 한계:** 같은 길이의 연속 영상 쌍은 최종 실기기20회에서 나오지 않았다. 동일 길이 분기의 JUnit 검증과 실기기 일반 행+1 관측을 그 사례의 실기기 PASS로 합치지 않는다. 드문 일반 timeout의 실제 새 시작점 복구는 이번 연속 시험에서 발생하지 않았다. 좁은 플로팅의 ‘긴영상’ 첫 글자 일부 잘림은 동작에 영향 없는 경미한 알려진 문제로 남겼으며,1/1·10초 숫자는 정상이다. 전체 시각감사 PASS나 모든 기기·호스트 버전의 보증을 하지 않는다.
- 실패 처리: 검증 중 실패가 발생하면 **실패 확인→재현→직접 원인·영향 범위 분석→필요한 범위 수정→재시험** 순서를 반복한다. 기능·상태 전환·검증의 의존관계를 논리적으로 연결하되 별도 그래프 프레임워크를 설치하거나 새 제품 기능을 추가하지 않는다. 수정된 산출물의 근거와 변경하지 않은 경로의 기존 근거를 구분해 관리한다.
- 증상·기존 증거: code26 후속 실행에서 요청/현재 길이59초,다른 콘텐츠 키,스크롤 이벤트 index−1로 전환을 확인하지 못했다. 그 실패 요청의 전후 화면 쌍은 없어 실제 이동 여부는 미확정이다. 길이만 다름이나 메타데이터만 다름으로 안전조건을 해제하지 않는다.
- code27 읽기 전용 재현: 임시 진단에서 동일한 실제 YouTube 페이지의 직접 자식이 `CollectionItemInfo(row0,column0,rowSpan1,columnSpan1)`를 제공했다. 수동 정방향 이동 후 같은 영역에서row1,역방향 이동 후row0을 확인했다. 반면 스크롤 이벤트의from/to/itemCount는−1,source는하위 컨테이너,y/maxY는0이어서 페이지 번호 근거로 사용할 수 없었다. 이 수동 조사 이동은 자동 넘김 시험 성공 수에 포함하지 않는다.
- 직접 원인·영향: 기존 보조 콘텐츠 키 경로가 스크롤 이벤트 인덱스와 길이 차이에 의존했지만 해당 YouTube 구조는 실제 페이지 행 번호를 별도로 제공했다. 같은 길이에서는 필요한 독립 이동근거를 얻지 못했다. 이번 code26→code28 수정은 YouTube 긴 영상의 보조 콘텐츠 키 확인에 한정하며 Instagram·일반 반복·공용`AdvanceGate` 정책은 code26 그대로 유지한다.
- 수정: code28의 YouTube 보조 콘텐츠 키 경로는 요청 시 직접 자식 단일 전체 페이지의 `CollectionItemInfo` 행 번호를 저장하고 확인할 때 다시 읽는다. 같은 창·동일 pager 객체·동일 전체 페이지 영역과 전후 콘텐츠 표본 일치를 확인한다. 요청/현재 행을 모두 알 때는 **현재 행=요청 행+1**만 허용하며,같음·역방향·+2 이상은 총길이가 달라도 거절한다. 정확한+1은 기존 pager 이동·유효한 총길이 차이 외에 추가로 인정하는 독립 근거다. **다른 콘텐츠 키·300ms 이상 안정·최신 실제 전진 재생**은 계속 필요하고 길이·행 번호 하나만으로 이동을 확정하지 않는다. 과거+1을 저장해 재사용하지 않으므로 행이 되돌아가면 이전 이동 근거도 유지되지 않는다.
- 안전한 실패 분리: `CollectionItemInfo` 자체가 없는 것은UNKNOWN이다. UNKNOWN은+1 근거가 아니며 기존 안전한 확인 경로만 평가한다. refresh 실패·잘못된 행/열/span·다른 창·pager·영역·복수 페이지·전후 콘텐츠 불일치는UNSAFE로 구분해 기존 길이 근거로도 우회하지 않는다. 콘텐츠 키의 등장·소실은 요청 시 고정한 출처를 바꾸지 않는다. 읽기 전용이며 새 권한·오디오·화면 캡처·영상 내용 저장/전송을 추가하지 않는다. 일반 반복 복구·광고·라이브·Instagram 확인·4.5초 긴 영상 안전정지는 유지한다.
- 잘못된 대안·예방: 동일/역방향/+2 행을 다음 페이지로 인정하거나,한 번의+1을고정한 채 되돌아온 행을 계속 승인하거나,UNKNOWN과UNSAFE를같이 취급하거나,서로 다른 시점의 콘텐츠와 행을 조합하지 않는다. 제목 전용→음원 전용처럼 겹치지 않는 메타데이터만으로 다른 영상이라고 확정하지 않는다.
- 자동 재발방지 범위: 정확한+1·동일행·역행·+2·경계값/overflow·UNKNOWN/UNSAFE 분리,refresh/shape/창/pager/영역/복수페이지 거절,전후 콘텐츠 일치와 현재행 롤백,정지영상·길이 단독·일반복구 우회 방지를 검증한다. 최종 실행 결과는 [VERIFICATION](VERIFICATION.md)에 기록하며 계획을 PASS로 쓰지 않는다.
- 진단 제거·개인정보: code27의 임시probe는 code28 제품에서 제거한다. 행 번호와 구조 조건만 사용하고 영상 문구·계정·원시 화면/로그를 공개하지 않는다. 새 권한·오디오 수집·영상정보 전송을 추가하지 않는다.
- 시험·배포 경계: YouTube 재시험과 영향 범위 검증 후 성공하면 기존 공개 저장소에 게시하는 범위다. Instagram 전체10회는 반복하지 않으며 code26 결과를 그대로 보존한다. 현재 PC·계측·설치·YouTube20·공개CI/파일검증PASS와 조건별미실행한계를분리한다.

EN: A temporary code27 probe found that the actual YouTube page's direct child exposes CollectionItemInfo row0,column0,spans1;manual forward/reverse moves changed it0→1→0 while event indices remained−1 and scroll y/maxY remained0. These are diagnostic manual moves,not automatic successes. Code28 removes the probe and adds request-scoped exact+1 current-row evidence for YouTube's supplemental-content long-video path only. Changed content,stable forward playback,same window/pager/full bounds,and matching sample generations remain required. Known unchanged/backward/skipped rows reject even a changed duration;rollback is not sticky. Missing item metadata is UNKNOWN,whereas malformed/refreshed-unsafe shapes are UNSAFE. Instagram and shared AdvanceGate remain unchanged from code26 for this code28 correction;code26's Instagram10 PASS is retained without a new full run. Code28 passed468 JUnit tests,build,static guards,233/233/232 exact-APK emulator checks and installation/settings/accessibility/runtime/hash checks;lint has0 errors/3 existing warnings. Its746246-byte APK hash is recorded above. YouTube20 passed in148.6 seconds at13:08:46–13:11:14.291 with20/20,4 ordinary/15 long-video/1 live and0 manual/failure/recovery;all0–20 screenshots were reviewed and exact row+1 observed. Publication and anonymous artifact parity passed;CI33141470669 shows32 suites/468 tests with0 failures/errors/skips and lint0 errors/2 warnings. Local lint remains0/3. Independent review found0 P1/P2 issues in scope;navigation-overlay testing was not run.

## D-035 과거 · code26 메타데이터 보강·동일 길이 후속 실패 / Metadata guards and same-duration failure

- 상태: **code26 실폰 후속FAIL·게시 보류(BLOCKED)**. 아래 지정10회 PASS와 이후 실패를 함께 기록하며 제품 완료로 표현하지 않는다.
- 기존 원인: code23/24에서 영상 공통 텍스트 identity가 같아 실제 이동을 확인하지 못했다. code25는 요청/현재 pager index가 모두−1인 화면에서 보조 이동근거도 얻지 못했다. 당시 영상별 메타데이터는 수집 대상 TextView가 아닌 ID 없는 ViewGroup에 있었다.
- 수정: 단일 페이지의 하단40%·좌측 넓은 메타데이터 영역을 별도 RAM 키로 읽고 일반 반복 identity는 유지한다. 요청 시 identity 출처를 고정해 전후에 다른 출처의 값을 비교하지 않는다. 임시 구조 진단은 제거하고 영상 문구·원시 트리의 저장·전송을 추가하지 않는다.
- 예방 P2·직접 원인: 메타데이터 일부가 일시적으로 사라지는 것만으로도 키가 바뀔 수 있다. **키 변화만으로 이동 확인하면 동일 영상의 일시적 UI 변화가 다음 영상으로 오인될 위험**이 있어 독립적인 이동/길이 근거를 추가했다.
- 확인 계약: YouTube의 별도 RAM 메타데이터 키 경로는 **다른 키 AND (요청 후 같은 창·pager의 최신 실제 index 변화 OR 다른 유효 총길이) AND 300ms 이상 안정 AND 최신 실제 전진 재생**을 모두 요구한다. 요청 시 키 출처를 고정해 메타데이터의 등장·소실을 다른 출처의 키와 비교하지 않는다. 부분 메타데이터 소실로 키만 달라져도 이동으로 인정하지 않는다. 일반 반복 identity는 바꾸지 않는다. 메타데이터 키를 쓰지 않는 기존 확인 경로는 안정된 다른 identity 또는 최신 동일 pager 이동+다른 안정된 총길이+전진 근거를 유지한다. 길이 단독은 확인 근거가 아니며, 메타데이터 경로에서 같은 길이이고 pager index도 없으면 실제 이동했더라도 안전정지할 수 있다. 긴 영상 확인4.5초 실패는 일반 복구나 추가 스와이프로 우회하지 않는다.
- 자동 재발방지: ID 없는 ViewGroup 수집/제한 영역·복수 페이지 거절,일반 identity 불변,요청 시 출처 고정,메타데이터 등장·전체/부분 소실,키만 변화·길이만 변화·오래된/다른 pager 이벤트·정지/버퍼링·안정화 부족 거절을 제품/네이티브 회귀에 포함한다. 긴 영상 정지값 오인·일반 복구 우회·상태표시 우선순위 방어는 유지한다.
- PC·계측·설치 재시험: 454JUnit/실패0,정적 가드 전체 PASS,lint0오류/기존3경고.12:33 동일APK API26/33/34 계측209/209/208 PASS.12:36 설치·전체 기존 설정 직접 비교 보존·접근성 연결·설치본 해시 일치 PASS.
- 고정 산출물: APK757038bytes,SHA256 `82CE7C221C1BF3E6DA8F86F9D487F9685D89DFB22A38D24F60B77F447519E926`.
- 지정 실폰 시험:12:38:20 YouTube 요청10/확인10,긴 영상9+라이브1. 전후0~10 화면을 육안 대조해 서로 다른 영상임을 확인했다. 일반10회 PASS로 표시하지 않는다.12:39:22 별도 일반1/1 자동 이동도 기준13→14 화면 쌍으로 확인했으며 공식10회와 합산하지 않는다.
- 후속 실패 재현: 계속 실행한 뒤 요청20/확인19에서 안전정지했다. 해당 실패 요청의 전후 화면 쌍이 없어 실제 다음 영상으로 이동했는지는 미확정이다. 요청 길이59초/현재 길이59초,다른 metadata key,contentKey 있음,pagerChanged=false,요청/현재 index−1이었다. 독립적인 페이지 이동 또는 길이 차이가 없어 엄격한 보조 키 확인 조건을 충족하지 못했다.
- 영향·남은 한계: 같은 길이이고 pager index를 제공하지 않는 페이지는 실제로 달라졌더라도 보수적으로 멈출 수 있다. 부분 메타데이터 소실 오인을 막는 안전조건과 구분 성능의 충돌이다. 키 단독 승인·무조건 재시도·일반 복구 우회로 완화하지 않는다. 후속 조치와 재시험이 확정되기 전에는 배포하지 않는다.
- Instagram 별도 재시험: Instagram 공식 시험은12:43:56~12:45:31.831,총96.0초이며 기준 요청/확인19→29에서 **요청10/확인10 PASS**다. 구성은 일반3·긴 영상4·진행정보 없는10초 시간제2·광고1,수동 이동0·실패0·복구0이다. 전후0~10 화면을 육안 대조했으며8번 캡처는 광고→일반 전환 중이고9번은 안정된 페이지였다. 목표 뒤 추가6회는 이10회 결과에 합산하지 않는다.
- 종료·복원: 플로팅X로 종료 후 blocked=false.12:46 인앱 입력으로 긴 영상30→60초 복원·UI/런타임 확인,전체OFF·반복1·긴 영상ON60·광고ON·라이브ON0·시간제ON10·화면 분석OFF를 유지했다.
- 다음 조사: 다음 안전한 조사 방향은 `CollectionItemInfo` 또는 pager 스크롤 위치가 독립적인 페이지 이동 근거를 제공하는지 **읽기 전용으로 관측**하는 것이다. 이번 상태 정리에서는 추가 구현·공개를 하지 않는다. 기존 확인 조건을 제거하거나 서로 겹치지 않는 제목 전용→음원 전용 메타데이터를 곧바로 다른 영상으로 인정하지 않는다.
- 시각 관측: 좁은 플로팅에서 ‘긴영상’ 첫 글자가 약간 잘리는 화면이 있었다. 숫자1/1·10초는 정상이고 동작 영향은 없으나 전체 시각감사PASS는 아니다. 이번 상태 정리에서는 UI를 수정하지 않았다.
- 미완료: 드문 일반 timeout의 실제 새 시작점 복구,최종 전체 화면 시각/사용성 감사,공개 CI·Release·다운로드 동일성. 아래 code25/23/22 수치는 각각 과거 후보 증거다.
- 개인정보: 공개기록은 원인 분류·조건·수치만 포함하며 영상내용·계정·알림·원시로그·개인경로를 포함하지 않는다.

EN: Code26's454 tests,209/209/208 exact-APK emulator checks and installation/settings/hash parity passed. Its supplemental RAM-only YouTube key is request-source-pinned and requires independent pager or duration corroboration plus stable forward playback,preventing partial metadata loss from falsely confirming a transition. The designated YouTube run passed10 distinct transitions(9 long-video,1 live),and one separate ordinary1/1 transition passed. Later continuation failed at20 requests/19 confirmations:both durations were59 seconds,the metadata key differed,but both pager indices were−1 and no fresh pager change existed. This known conservative limitation leaves release BLOCKED;the earlier ten-transition PASS does not establish overall readiness. The failed YouTube request lacks a pre/post screenshot pair,so its actual movement is unproven. The formal Instagram run lasted96.0 seconds at12:43:56–12:45:31.831,advancing the request/confirmation baseline19→29:10/10 PASS. It comprised3 ordinary,4 long-video,2 ten-second clockless and1 ad transition,with0 manual swipes,failures or recoveries. Screenshots0–10 were visually reviewed;capture8 shows the outgoing-ad/incoming-ordinary gesture and9 a settled page. Six later transitions are excluded from this ten-transition result. At12:46 the threshold was restored in-app30→60 seconds and verified in UI/runtime;overall execution isOFF. Physical rare-timeout recovery and delivery checks remain incomplete. The next safe investigation is read-only observation of CollectionItemInfo or pager scroll position for independent transition evidence. No further implementation or publication occurs in this status update. Do not drop confirmation guards or treat disjoint title-only→audio-only metadata as proof of another video.

## D-035 과거 후속 · code25 페이지 인덱스 부재·code26 설계 / Historical missing-index diagnosis

- 당시 상태:code25 실폰FAIL·미배포,code26 보강 착수 단계. 아래는12:21~12:22의 과거 원인 기록이며 그 이후 후보 결과는 위 항목을 따른다.
- 증상/재현:12:21~12:22 code25에서긴영상이실제93초→57초의다른영상으로이동했으나,`requestIndex=-1`·`currentIndex=-1`·`pagerChanged=false`였고공통identity도동일하여확인시간초과로정지했다.
- 직접 원인/확인 증거:이실제YouTube트리는요청전후사용가능한pager인덱스를제공하지않았다. 또한제목·음원메타데이터는ID없는`ViewGroup`의텍스트/설명형태로노출되고,기존`TextView`수집에는공통하단탭만포함되었다. 따라서code25의엄격한fresh인덱스보조근거와기존identity가이사례에서모두구분력을갖지못했다. 구체적인영상문구·계정·원시트리는공개기록하지않는다.
- 잘못된 기존 접근:실제앱이항상페이지인덱스를주거나영상별메타데이터를`TextView`로제공한다고가정했다. 인덱스검사를무조건생략하거나길이차이만으로확인하는대신실측구조에서별도안전근거를수집한다.
- code26수정방향:단일페이지내하단40%·좌측의넓은메타데이터영역에서RAM전용키를별도수집한다. 일반반복의기존identity는유지하고긴영상전환의확인키로분리한다. 요청시identity출처를고정하여메타데이터가새로나타나거나사라진것만으로다른영상이라고판정하지않는다. 일시적인구조진단출력은제품소스에서제거하는방향이며내용저장·전송·공개는추가하지않는다.
- 자동 재발방지/영향범위:ID없는`ViewGroup`메타데이터·영역밖/복수페이지거절·요청전후키출처유지·메타데이터생김/사라짐·기존일반identity불변을검사한다. 긴영상전환확인만보강하며광고/라이브/일반복구의안전정지를우회하지않는다. code26최종결과는아직미정이다.
- code25과거PC/계측증거:442JUnit·정적가드PASS,동일APK API26/33/34 계측166/166/165 PASS. APK722207bytes,SHA256 `0EE88E622E9EA7F85DC0FFFDBFFE0D2104EDB616E82E2D1AAF896B5EE569ECE6`. 이산출물은이후실폰FAIL한미배포후보이며code26또는소셜10+10완료의근거가아니다.
- 당시 재시험:code25 실패 재현과 구조 차이는 확인했다. 이 체크포인트에서는 code26 검증 전이었으며 최신 빌드/계측/실폰 결과는 위 항목과 [VERIFICATION](VERIFICATION.md)에 별도 기록한다.

EN: At12:21–12:22,code25 physically moved93→57 seconds but timed out with requestIndex=-1,currentIndex=-1,pagerChanged=false and the same generic identity. Actual metadata appeared in unnamed ViewGroups rather than the collected TextViews,which contained shared bottom tabs. At that checkpoint,code26 was adding a separate RAM-only key from a constrained lower-left metadata region within a single page,while preserving ordinary identity and fixing the identity source at request time. Metadata appearing/disappearing alone must not confirm movement. Temporary structural diagnostics were scheduled for removal;no video text or raw tree is published. Code25's442 tests,166/166/165 emulator checks and722207-byte hash remain evidence for a failed unpublished candidate;code26 had not yet been verified at that checkpoint. Current results are recorded above.

## D-035 과거 후속 · code23/24 동일identity 확인 실패 / Historical same-identity failure

- 상태:code23·code24 실폰FAIL·미배포,code25수정중. 기존PC/계측PASS를제품전체PASS로확대하지않는다.
- 증상/재현:12:12code23에서 긴영상요청1/확인0,실제62초영상에서93초의다른영상으로이동했으나4.5초뒤안전정지했다.12:17code24진단설치로재현했다.
- 직접 원인/확인 증거:4.5초동안sameWindow/sameBounds/recognized/safe는true지만identityDifferent는계속false였다. YouTube화면의공통nonclickabletext해시가서로다른영상에도같아,긴영상의전환확인이실제이동을구분하지못했다. 영상내용·계정·원시노드를공개기록하지않는다.
- 영향 범위:긴영상의다음페이지확인. 같은해시가나오는다른영상쌍에서도발생가능하다. 이번증거를과거D-034최초실패나알림원인으로소급확정하지않는다.
- 잘못된 기존 접근:비클릭텍스트해시를영상마다달라지는식별자로가정하고다른identity만으로긴영상전환을확인했다. 무조건재스와이프·시간초과확대·길이변화만으로성공처리하는대안은채택하지않는다.
- code25수정방향:안정된다른identity를확인하거나,요청후fresh동일pager/window의실제index변화+다른총길이300ms안정+최신실제전진을모두확인한다. 길이단독·다른창/pager·오래된이벤트·같은identity/같은길이·불명화면은확인하지않고기존hardstop을유지한다.
- 자동 재발방지:동일identity/다른길이/fresh이벤트의성공사례와길이단독·지연이벤트·다른pager/window·같은길이·정지·근거재사용·안정화부족의거절사례를회귀시험에추가하는중이다. 최종실행결과는 [VERIFICATION](VERIFICATION.md)에별도기록한다.
- 재시험:code23PC418/163/163/162와설치해시는이전단계PASS로보존. code24는진단재현용으로설치했고실패원인을확인했다. **code25의최종PC/계측/실폰10+10/공개검증은아직확정전**이다.

EN: At12:12,code23 actually moved62→93 seconds but safety-stopped with1 long-video request and0 confirmations. Code24 reproduced the failure at12:17:window,bounds,recognition and safety stayed valid while identity remained equal throughout4.5 seconds. Common nonclickable YouTube text produced an identical hash across videos. Code25 adds corroborated fresh same-pager/window index movement plus stable changed duration and current forward playback,or a stable changed identity. Duration alone,stale/wrong-window events and ambiguous same-identity/same-duration pages remain rejected. Code25 verification is pending;code23/24 were not published.

## D-035 과거 예방 · code23 / Historical preventive safeguards

- 상태:0.2.6/code23 후보의 독립 검토와 예방 구현. 아래는 확인한 위험 경로와 재발방지 계약이며, 실제 휴대폰에서 모든 위험이 발생했다는 기록이 아니다.
- 증상/재현 목표: 총길이만 보고 일시정지된 긴 영상을 넘김, 긴 영상 전환 실패를 일반 반복복구로 바꿔 재요청, 복구대기/안전정지 라벨이 긴 영상/0회 라벨로 가려지는 경우를 합성시험 대상으로 한다.
- 직접 원인/영향: 총길이값은 재생 진행이나 안전한 화면을 증명하지 않는다. 요청 종류가 분리되지 않으면 일반4.5초 timeout복구가 긴 영상에도 적용될 수 있고, 표시 분기 순서가 잘못되면 실제 안전상태를 숨길 수 있다. 두 호스트의 길이필터·복구·0회 UI가 영향 범위다.
- 잘못된 접근: 길이≥기준 한 표본만으로 즉시 스와이프, 모든timeout의자동복구,0회옵션라벨을 오류보다 먼저 표시, 진행정보없는 영상을 긴 영상으로 추측하는 방식을 채택하지 않는다.
- 수정: 별도의 긴 영상 정책/추적기와 요청종류로 분리한다. 같은 안전페이지·길이의 안정과 실제 전진을 확인한 후1회 요청하며 정지·불연속·화면이탈 때 근거를 초기화한다. 긴 영상 확인4.5초 실패는hardstop,복구대기에서는 긴 영상 우회금지다. 복구대기/안전정지 표시를 우선한다.
- 자동 재발방지: `LongVideoPolicyTest`/`LongVideoTrackerTest`의 길이경계·정지·전진·공백·중복,`LongVideoSettingsStoreTest`의8개 저장회귀,`LongVideoUiChecks`의 입력·가용성·0독립·설정보존·메뉴순서,네이티브서비스의 요청분리·표시우선순위 검사를 연결했다.
- 당시재시험: code23빌드·418제품시험/실패0·정적가드PASS.12:10 같은APK Android26/33/34 계측163/163/162와폰설치·설정·bound·해시PASS. 그러나12:12실제긴영상전환확인FAIL이발생하여미배포로전환했다. 위후속원인과code25수정을따르며PC/계측PASS로실폰실패를덮지않는다.
- 개인정보: 공개기록은 원인분류·수치·시험범위만 포함한다. 영상/계정/알림내용·개인화면·원시로그는 제외한다.

EN: Code23's preventive checks passed build,418 tests,static guards,163/163/162 emulator checks and installation parity before the12:12 physical confirmation failure. These reviewed/synthetic risks were not complete real-device coverage. The candidate remained unpublished,and the follow-up cause and code25 correction are recorded above.

## D-034 과거 통합 · 0.2.6/code23 / Historical recovery integration

code23은 아래code22의 제한적 시작점복구를 유지하면서 긴 영상필터를 특수 요청으로 추가했다. 긴 영상의확인실패는일반복구대상이 아니며복구중긴영상우회도 금지한다. 현재검증은 [VERIFICATION](VERIFICATION.md)을 따르고code22의383/109/109/108수치를재사용하지 않는다. 최초 전환실패와 알림 인과관계는 여전히미확정이다.

## D-034 과거 수정 · 0.2.6/code22 후보 / Historical recovery implementation

- 증상/직접 원인: 일반 넘김 확인 시간초과가 영구 blocked 상태가 되어 새 재생 시작도 읽지 못하고 플로팅0/N만 표시했다. 최초 전환 자체의 실패 원인과 알림 인과관계는 여전히 미확정이다.
- 범위/잘못된 접근: 모든 실패를 무조건 재스와이프하거나4.5초를 늘리지 않는다. 광고·라이브·진행정보 없는 화면에는 일반 복구를 적용하지 않는다.
- 수정: 일반 진행기반 요청의 시간초과만 읽기 전용 복구로 전환한다. 과거 요청세대를 무효화하고 이전 누적을 폐기한다. 같은 요청host/window의 현재 정상 영상에서 시작 부근과300ms~3초 이내 정상전진을 확인하고 기존 LoopCounter로N회 완주를 새로 관측한다. 시작 확인 자체는요청/전환확인 수를 증가시키지 않는다. 플로팅은복구 대기/안전정지를 구분한다.
- 예방: 중복스와이프·다른앱/창·OFF/0회·멈춘0초·불연속/영상변경·특수모드 우회를 차단한다. 권한·제스처 거부/취소·전환중화면변경·예외의 기존 hardstop은 유지한다. 복구중원래창을잃거나진행정보없으면대기지속/명시OFF→ON 필요.
- 자동시험: 제품383개(새27개), 정적서비스연결가드 PASS. 실제 Android26/33/34 에뮬레이터109/109/108개(각35개 합성 복구서비스/표시시험 포함) PASS. 새 시작이후온전한1/2/99회·이미이동한영상·0회·host/window/영역/특수타임아웃을 확인했다.
- 독립리뷰: 확인된P1/P2 없음. 실제 tick·물리 알림·드문timeout E2E와 합성서비스 직접호출을 구분한다.
- 과거 실기기: code22에서YouTube2회자동전환을확인하고180초영상1개수동이동은제외했다. 긴영상기능추가로중단하여10PASS가아니며Instagram10은미실행이다. 정상관측을실제timeout복구PASS로확대하지않는다. code22미게시.

EN: Historical code22 replaced ordinary-timeout latching with observation-only fresh-start recounting and passed383 tests plus109/109/108 emulator checks,including35 synthetic service/label checks per OS. Its phone run confirmed two automatic YouTube transitions,excluded one manual180-second skip,and stopped for feature integration before ten consecutive transitions. Instagram10/publication were not run. The original triggering cause is still unknown;these results do not verify code23.

## D-034 · 넘김 확인 실패 뒤 카운트 정지와 플로팅 상태 누락 / Latched stop hidden by count label

- 알림 관련 가설 검토: 알림 도착 자체를 카운트 초기화하는 코드는 없고 카톡 알림 이벤트도 직접 구독하지 않는다. 다만 주기 조회에서 알림/답장창이 활성 root를 바꾸거나, 입력 포커스가 YouTube 창과 달라지거나, 재생정보를 읽지 못하면 일반 관측은 `invalidate()`로 현재 카운트를 초기화한다(설정 횟수는 유지). 넘김 확인 중이면 패키지 변화는 즉시 중단 경로, root/창/정보 불가가 지속되면4500ms 확인 실패 경로로 차단될 수 있다. 배너가 나타나도 기존 활성창과 유효한 읽기를 유지하면 이 조건은 발생하지 않는다. [Android active-window 정의](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#getRootInActiveWindow())와 제품 소스에 따른 조건부 가능성이며, 실제 카톡 알림 재현이나 이번 최초 실패와의 연관은 아직 미확인이다. 실제 알림 발송·권한 변경·폰 조작 없음.
- EN notification hypothesis: No direct notification-arrival reset exists. Polling can reset the current count if a popup changes the active root/input focus or makes playback unavailable; configured count remains unchanged. During a pending advance, host changes interrupt immediately, while prolonged unavailable observations can exhaust the4500ms confirmation deadline. A banner alone does not prove these conditions. Kakao-specific reproduction and causation of the original failure are unverified; no messages were sent or device settings changed.

- 상태:2026-08-28 11:20경 code21 실기기에서 확인, 미수정. 초기 단계는 읽기 전용이었고 아래 후속에서 기존 설정으로 재개 시험했다. 설치·권한·제품 코드 변경 없음.
- 후속 재현 시험: 동일 영상에서 플로팅 간편모드1→0→1로 차단을 해제했다. 시간58초가 정상 진행하고 다음 시작에서1/1로 전환했다. 11:24:44.724 요청22→46.304 확인21/48초 영상,11:25:34.121 요청23→35.804 확인22/19초 영상,11:25:54.535 요청24→56.106 확인23/69초 영상. 수동 영상 이동 없이3번 실제 자동 전환·전환중/후 스크린샷·blocked=false/세대169 유지 확인. 원래 실패는 재현되지 않았고 수정 또는 근본 원인 확정으로 보고하지 않는다. 시험 후 원래의실행ON·목표1·간편모드1 유지.
- 독립 소스 검토: 일반 좌표·280ms 및 일반 전환 확인 조건은 이전 공개판과 동일하다. 공유 확장트리의 복수 visible page 선택/라이브 노드와의 충돌은 추가 관측 후보일 뿐 이번 원인으로 입증되지 않았다. 현재 진단에 요청 수락/콜백 이력·선택 페이지 좌표·요청 전후 판독 실패 이유가 없어 최초 요청을 사후 복원할 수 없다.
- 증상/재현: 일반 YouTube 쇼츠와 진행바가 보이지만 플로팅0/1 유지. 이미 발생한 정지 상태를 두 번 조회해 같은 값을 확인했다. 정지에 이르는 최초 전환 자체는 재현하지 않았다.
- 직접 원인/증거: connected=true,enabled=true,target=1,current=0,blocked=true,requests=21,confirmed=20,pending=false, status=넘김 확인 실패. 라이브 요청/확인은0. `tick()`은 blocked이면 화면 조회 전에 반환하므로 마지막 position0/duration58은 현재 영상의 실시간 진행정보가 아니다.
- 코드 근거: `AdvanceGate`가 요청 후4500ms까지 전환 확인을 받지 못하면FAILED,서비스는 `failClosed()`에서 카운트를0으로 초기화하고 차단을 유지한다. `FloatingController.update()`에는 이 오류의 가시적 라벨 분기가 없어0/1로 표시한다. 오류 설명은 contentDescription에만 남는다.
- 미확정: 최초 전환에서 실제 제스처가 실패했는지, 이동은 됐지만 화면/진행정보를 확인하지 못했는지 당시 표본이 없어 구분하지 못한다. 현재 일반 영상의 미지원·라이브 오인·D-021 반복경계 문제로 단정하지 않는다.
- 영향/잘못된 접근: 안전정지 후 정상 영상에서도 카운트가 멈추며 숫자만 보고 인식 불량이나 권한 문제로 오해할 수 있다. 무조건 권한 재승인·타임아웃 확대·추가 스와이프를 수행하지 않는다.
- 수정/자동 재발방지: 아직 없음. 다음 검토 대상은 실패 직전의 비식별 수치 기록, 명확한 정지 표시, 중복 제스처 방지를 유지하는 안전한 재개 조건이다. 별도 구현·시험이 필요하다.
- 시험: 현재 정지/0표시 및 소스 경로 대조 확인. 후속 수동 재개와3회 자동 이동은 위 근거로 확인했으나 최초 실패 재현은 불가했다. 수정 회귀시험은 NOT RUN. 기존10연속 PASS는 과거 표본이며 이 실패를 부정하지 않는다. 코드/APK/GitHub 변경 없음.

EN: On code21, a normal YouTube Short displayed0/1 while the service remained connected and enabled but blocked after an unconfirmed advance(21 requests/20 confirmations). Polling exits before reading the screen, so the stored0/58 timing is stale. A4500ms confirmation timeout latches the stop and resets the count; the floating label lacks a visible error branch. The original transition was not observed, so failed movement cannot yet be distinguished from failed recognition. Read-only diagnosis only; recovery, implementation and reproduction of the triggering transition are not performed. Prior ten-transition success does not invalidate this subsequent failure.

EN follow-up: Cycling the floating target1→0→1 rearmed the same video. Three subsequent actual automatic transitions were confirmed(58→48→19→69-second videos), with screenshots and stable generation169. The original failure did not reproduce. ExecutionON/target1/tap mode1 were preserved; no product change or publication. Initial read-only status above is historical. Gesture callback history, selected page bounds and pending-reader failure reasons are absent, preventing a definitive retrospective root-cause finding.

## 2026-08-28 · code21 공개 검증 / Publication verification

2026-08-28 10:57 KST에 **0.2.5/code21을 기존 Public 저장소의 시험판으로 공개**했다. [v0.2.5 Release](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.5), main·태그 기준 코드 커밋 `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5`, draft=false/prerelease=true. [CI33134278633](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33134278633) 성공: 내려받은 보고서356시험·실패0·오류0·건너뜀0, CI lint0오류/2경고(로컬 기존3경고와 구분). 익명 릴리스HTTP200 및 배포파일3개(APK709703bytes/SHA96bytes/JSON287bytes)의 원본 대비 크기·SHA-256 일치를 확인했다. 이후 문서 정리는 제품 소스·APK를 바꾸지 않는다.

**Version0.2.5/code21 was published as a Public prerelease at10:57 KST on2026-08-28**, from code commit `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5` and tagv0.2.5. CI33134278633 succeeded with356 tests and zero failures/errors/skips; CI lint reported0 errors/2 warnings, separately from3 local warnings. Anonymous release access returnedHTTP200 and all three uploaded assets matched their originals byte-for-byte and by SHA-256. Subsequent documentation changes do not alter product source or the APK.

공개 후10:58~10:59, 설치된code21 앱의 업데이트 확인을 실제 실행해 새 업데이트 없음 안내·조회 시각 갱신을 확인했다. 실제 Public HTTPS 조회와 현재 버전 안내는PASS이며 새 버전 다운로드/설치 재시험은 아니다. 업데이트 자동조회ON·전체실행OFF를 유지했다.

At10:58–10:59, the installed code21 app successfully checked the public release over HTTPS, displayed no applicable newer update and updated its attempt timestamp. This verifies current-version checking, not a new-version download or installation. Automatic checking remained ON and overall execution OFF.

배포 APK SHA-256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`. v2서명·기존 인증서 유지,메타데이터21/0.2.5/min26와10:51 설치본 동일성PASS. 공개 후보169개 민감정보 제외 독립감사와224개 로컬 링크 검사(누락0)를 확인했다. 세부 수치는 [검증 원장](VERIFICATION.md)을 따른다. 이 게시 확인은 D-021 수정·code21 라이브 개별 재시험·20연속 완료를 뜻하지 않는다. 메일 발송 없음.

EN: APK signature/certificate continuity,21/0.2.5/min26 metadata and installed parity passed. The169-file publication audit and224 local links passed. Publication does not resolve D-021 or establish individual code21 live retests or twenty consecutive transitions. No email was sent.

## 2026-08-28 · code21 조회 원복 보강·PC/설치 검증 / Cleanup and installation verification

독립 리뷰로 차단 상태에서 확장 플래그가 다시 켜지는 경로와 반복0/별도 동작 없는 idle 원복을 보강했다. code21 고정709703bytes/SHA256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`,356시험·빌드·lint0오류/기존3경고·LIVE_TREE_LIFECYCLE 정적가드 PASS.10:39 같은 APK의 API26/33/34 계측74/74/73,10:40 휴대폰 설치/버전21/접근성bound/설정보존/설치해시 PASS. 최종 조회 원복 독립리뷰 추가P1/P2 없음.

code21 일반 YouTube10연속은10:40:41→10:47:45.702/424.5초/확인기준0→10/세대6으로 PASS했다. 수동 입력·앱 전환 없이 진행했고 라이브 이동0회다. 전환1~10 화면을 모두 육안 대조해 정상 전체화면의 서로 다른 영상·플로팅·올바른 이동을 확인했다. 약10:50 플로팅X 전체OFF 확인,10:51 업데이트 자동조회ON 복원·UI/저장값 확인. code20 외부 중단·개별 라이브 또는 목표 이후 추가 관측을 공식10회에 합산하지 않는다. code21 라이브 개별 재시험은 아직 없으며 D-033의 해당 실제 근거는 아래code20 시험과 구분한다. D-021은 미수정·20연속은 미완료이고 Public 검증은 위 공개 기록과 구분해 확인했다.

EN: Code21 passed the final cleanup build,356 tests,lint/static guards,74/74/73 emulator checks,phone installation/binding/preferences/hash and final review. Its separate10-transition normal-YouTube run passed in424.5 seconds with confirmation0→10,generation6,no manual input/app changes and zero live skips. All10 transition screens passed visual review. FloatingX stopped execution around10:50;the automatic-update preference was restored ON at10:51. Code20 evidence and post-target observations are not added. Code21 individual live retests and20-transition completion remain unverified;D-021 is unchanged.

## D-033 · 조사·제품 접근성 조회 플래그 불일치 · code18 실기기 FAIL, code20 원인 수정·실기기 PASS

- 증상/재현: code18 실제 YouTube 라이브 미리보기에서 라이브 인식 대기가 지속돼 자동 이동하지 못했다. 빌드·합성 계측·설치 성공과 별개의 실기기 기능 FAIL이다.
- 직접 원인: 조사 프로브는 `FLAG_INCLUDE_NOT_IMPORTANT_VIEWS`를 사용했으나 제품 기본 조회는 사용하지 않았다. 따라서 프로브에서 보인 전용 라이브 요소가 제품 트리에 없었다.
- 확인 증거: 동일 라이브 화면에서3초씩 읽기 전용 대조. 기본 조회49노드·전용 `immersive_live_preview_player` 없음, 확장 조회101노드·전용 노드 있음. 제목/계정/원시 화면은 공개하지 않는다.
- 영향 범위: 해당 YouTube 라이브 구조의 제품 감지. 기존 D-032 전환 확인 설계와 D-021 일반 반복 경계는 별도로 추적한다. 전체 앱·모든 YouTube 버전에서도 같은 수가 나온다는 의미가 아니다.
- 잘못된 접근: 프로브와 제품의 조회 조건이 같은지 확인하지 않고 노드 존재 증거를 제품에서 그대로 얻을 수 있다고 가정한 것. 합성 노드 계측을 실제 호스트의 노드 노출 검증으로 대신할 수 없다.
- 중간안(code19): 라이브ON일 때만 확장 조회했다. 이 안은 라이브OFF의 일반→라이브 확인을 막고 pending 중 조회형태 변경에 따른 거짓 identity 위험이 있어 최종안으로 사용하지 않는다.
- 수정(code20): 전체실행ON·YouTube선택·전면YouTube일 때 라이브 옵션과 무관하게 확장 조회한다. 옵션OFF는 넘김만 차단한다. Instagram·기타 앱·전체OFF에서는 기존 조회를 유지하고 조회 모드가 바뀌면 이전 root를 폐기하여 새 조건으로 다시 읽는다. failClosed/onDestroy에서 기본 플래그를 명시적으로 복원한다. 새 권한은 추가하지 않는다.
- 자동시험: 기존352에 조회정책4개를 더한356제품시험이 cleanup 전 및 cleanup 포함 최종 재빌드에서 각각 PASS했다. 최종 lint0오류/기존3경고, 동일 후보 API26/33/34 계측74/74/73 PASS. 제품 플래그로 실행한 실제 라이브 인식·5초/0초·OFF 대기 검증은 아래처럼 별도 기록한다.
- 실기기 재시험:5초 지연은10:30:29 대기→34 요청→35 확인누계1·25초 일반 영상 진입 PASS. 라이브OFF는10:33:33~39 같은 라이브 인식·blocked=false·요청2/확인2 유지 PASS.0초/반복0회는10:34:31 조회 준비→33 요청→34 확인2→3으로 독립 동작 PASS. 실제 메인 UI에 권한 경고가 없음을 육안 확인했다.
- 산출물/설치: code20 고정725487bytes, SHA256 `EF59D4E40E192A89D5B207741B03CCE08FA11AC1079DC61C7776C19A1D3D60EB`. 휴대폰 설치 준비·재생 설정 보존·설치본 해시 PASS. code18의738945bytes/실제 인식FAIL은 보존한다.
- 상태/한계: D-033의 확인된 원인은 위 실기기 범위에서 수정·재시험PASS. 새10연속은10:35:28/확인기준3/세대30에서 시작해2회 확인 후10:37:17 외부 앱 전환으로 중단됐으며, D-021 일반 경계·모든 UI/장시간 문제의 해결을 뜻하지 않는다. code20은 당시 미게시 후보이며 현재 공개판code21과 구분한다.

EN: Code18 failed real live recognition because the read-only probe included non-important accessibility views while the product used default flags. The same live screen exposed49 default nodes without the marker and101 expanded nodes with it. Code20 limits expanded retrieval to overall execution in selected foreground YouTube, independently of the live-skip option, keeps default retrieval for Instagram/other/OFF states, and discards old roots after mode changes. No new permission. The final code20 build/356 tests,74/74/73 emulator checks and phone installation/hash passed. Device5-second delay, immediate skipping at zero normal plays, and live-OFF waiting passed, resolving the confirmed flag mismatch in these cases. The10-transition run ended externally after two confirmations; code18's failure and D-021 are preserved.

## D-032 · YouTube 라이브 미리보기 진입 후 전환 확인 실패 · 개별 라이브 이동 PASS, 연속 검증 미완료

현재 라이브 검증 상태: code21의 일반 영상10연속은PASS지만 라이브 이동0회이므로 라이브 재시험 근거로 쓰지 않는다. code20에서5초·0초/반복0회의 개별 라이브→일반 전환 확인과 라이브OFF 대기가 실제 통과했다. 새10연속은2회 확인 후 외부 중단으로 미완료이므로 D-032 전체를 연속 안정성 완료로 닫지 않는다. 다음은 과거 code18 체크포인트다: code18에서 라이브 감지·독립 옵션·0~60초 지연·전환 보호를 구현했다.352제품시험/빌드/lint·동일 후보 에뮬레이터74/74/73·휴대폰 설치/재연결/설정보존/해시는 통과했다. 다만 최초 실기기 라이브 인식은 D-033의 조회 플래그 불일치로 FAIL했고 정식0초/5초와10연속 시험은 NOT RUN이다. 이 문제를 실기기 해결 완료로 닫지 않는다. 보존 후보738945bytes, SHA256 `941532517058CB8553EFE5DB34ED1762426C468B2D66F88A567CE788E306C54D`. [검증 원장](VERIFICATION.md).

Current live-validation status: code21 passed10 normal transitions with zero live skips, which does not retest live handling. Code20 passed individual5-second/immediate live-to-normal transitions and live-OFF waiting. Its new consecutive run ended externally after two confirmations and remains incomplete. Historical code18 implemented recognition, an independent option, a0–60-second delay and transition guards. Build/product/emulator/install checks passed, but real recognition failed because of the flag mismatch in D-033. At that code18 checkpoint, formal0/5-second and10-transition runs were NOT RUN. D-032 is not closed as endurance-resolved.

### code16 재현·읽기 전용 조사 / Historical reproduction

- 증상/재현: Android17의0.2.5/code16에서 목표1회, 수동 조작 없이 YouTube10연속 자동 전환을 관측했다.2개 전환 확인 뒤3번째 요청으로 라이브 미리보기 화면에 도달했지만 `넘김 확인 실패`로 안전정지했다.
- 확인 증거: 전환 확인 누계7→9, 요청10/확인9,101.4초에서 시험FAIL. `connected=true`, `blocked=true`였으며 화면은 실제로 다른 라이브 미리보기로 바뀌었다. 원시 로그·개인 화면은 private에만 보관한다. 수동 재진입 조사는 이 자동 연속 시험 성공 횟수에 포함하지 않는다.
- 직접 원인: YouTubeReader가 정확히1개의 유효 SeekBar를 요구한다. 해당 라이브 화면에는 SeekBar가0개라 식별 가능한 새 페이지를 반환하지 못하며 AdvanceGate의4.5초 확인 제한에 걸린다. 제스처 자체 실패나 접근성 권한 해제로 분류하면 안 된다. 보이지 않는 `reel_time_bar` 래퍼의 존재만으로 실제 재생 시계가 있다고 판단해서도 안 된다.
- 영향 범위: 일반 쇼츠에서 라이브 미리보기로 전환한 뒤 자동 실행이 차단될 수 있다. 라이브에는 일반 반복 완주 카운트를 적용할 수 없다. 기존 D-021의 끝부분 시간 누락과는 별도 원인이다.
- 후속 조사: 기존 실행을OFF한 뒤 이전 일반 쇼츠→동일 라이브 미리보기 재진입을2회 읽기 전용 조사했다. 일반 화면은 SeekBar1개/라이브 식별자 없음, 라이브는 SeekBar0개와 `com.google.android.youtube:id/immersive_live_preview_player`가 확인됐다. 이 노드는 `reel_player_page_container` 아래에 있으며 ‘탭하여 라이브 콘텐츠 시청하기’ 문구가 없는 표본에서도 존재했다. 전용 노드는 빨간 라이브 배지보다 먼저 관측되기도 했다.
- 시간/기하 해석: 두 번째 조사에서 시작 기준1955ms에 화면 하단79px만 보이는 라이브 노드가 이미 존재했다.2511ms에는 전체 페이지와 라이브 배지가 있고 안내 문구는 없었으며5071ms에 문구가 다시 나타났다.2348ms에는 전환 중 이전 문구가 잠시 보인 표본도 있어 이를 최초 노출 지연의 정밀 측정으로 해석하지 않는다. 조사 폴링은120ms이며 제품의 감지/넘김 지연 보장값이 아니다.
- 잘못된 접근 방지: 제목의 LIVE 문자열, 빨간 색상, 재생바 부재만으로 라이브를 확정하지 않는다. 전용 노드가 화면 일부에 나타났다는 이유만으로 진행 중 스와이프에 두 번째 스와이프를 겹치지 않는다. 라이브 시청 버튼을 눌러 본 방송으로 진입하지 않는다.
- 당시 수정 제안(조사 시점 미구현): 전면 YouTube 쇼츠의 활성 단일 페이지 아래 전용 라이브 노드를 우선 판별하고, 창/페이지 범위·전환 안정화·현재 터치·기존 pending 보호를 함께 확인한다. 화면 변경 이벤트에 즉시 재조회하고 기존 폴링을 보조로 유지한다. 라이브 페이지 진입 확인과 다음 라이브 넘김 여부를 분리하고 끄기/즉시/지연(초) 설정을 제공한다. 후속 구현은 아래 code18 항목과 구분한다.
- 당시 자동 재발방지 계획: 일반→라이브 전환 확인, 문구 전 라이브 판별, 가려진/미리 로드된/부분 페이지 제외, 제목만LIVE인 일반영상 제외, 연속 라이브의 서로 다른 페이지 확인, 중복 제스처 방지, 라이브OFF에서 일반 쇼츠 복귀, 팝업/시스템창/잠금 보호를 시험한다. 순수·계측 시험과 실제 기기 시험은 별도 판정한다.
- 당시 조사 결과: 라이브의 조기 식별 가능성은 해당 기기·YouTube·동일 라이브2회 재진입에서 확인. 당시 감지 기능/자동 넘김 수정은 없었고10연속 시험은FAIL이었다. 조사 전후 서비스 연결을 유지했으며 종료 상태는 `connected=true`, `enabled=false`, `blocked=false`였다.
- 조사 도구: 제품 앱을 계측하거나 설치하지 않는 별도 셸 프로브. 고정 UI 문구 일치 여부·리소스ID·숫자만 출력하며 제목·계정은 기록하지 않는다. [Android UiAutomation](https://developer.android.com/reference/android/app/UiAutomation#FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)의 기존 접근성 유지 플래그를 사용했다. 이 도구의 결과는 제품 구현 PASS가 아니다.

EN: The historical code16 unattended10-transition run failed after2 confirmations at a clockless live preview. Read-only replay found the dedicated node before the watch-live prompt, including during a partial transition. At that investigation checkpoint, configurable live skipping was not yet implemented. That failure is preserved separately from code18; no10-transition PASS or new release is claimed.

### code18 수정·재발방지 / Implementation and regression protection

- 구현: 전용 리소스ID와 조상 관계·안정된 단일 페이지·전면창을 검사한다. 라이브는 recognized로 반환하되 일반 progress/화면 분석 대상으로 취급하지 않는다. 기본OFF·지연0~60초/기본0초, 일반0회와 독립, 전체OFF는 모두 중지다. 제목/CTA/시청자 수 대신 RAM의 페이지 노드 동일성을 사용한다.
- P1 최종 재검증: 마지막 root 재조회에서도 대상 창/페이지/라이브 상태를 다시 확인한다. 과거 snapshot의 안전성만으로 현재 제스처를 허용하지 않는다.
- P2 오래된 인덱스: 라이브→라이브 확인은 요청 이후 같은 창/pager 인덱스 변화와 다른 안정 페이지를 함께 요구한다. 요청 전 이벤트·다른 창/pager·변화 없는/불명 인덱스·이전 요청의 증거 재사용을 거절한다. 거절된 늦은 이벤트가 요청 기준 인덱스를 덮어쓰지 않게 한다.
- 일반 쇼츠 회귀 방지: null child와600노드 상한에서 일반 경로 전체를 새로 실패시키지 않는다. 수집은 나머지 노드를 계속 처리하며 완전성은 라이브에만 요구한다. 보이는 라이브 표시가 있는 불완전 트리는 거절하고, 없거나 숨겨져 있으면 기존 일반 판독으로 넘긴다.
- 동일 uptime: 같은 노드/기하/시각의 최종 재조회는 이미 안정화한 상태를 유지하되 시간을 더하지 않는다. 시간 역행·노드/기하 변경은 초기화한다.
- 자동 시험: YouTubeLivePolicyTest의 부분/숨김/복수/차단/불완전/동일시각 경계, LiveTransitionPolicyTest의 이벤트 신선도, LiveAdvanceGateTest의 요청별 기준과 안정 확인, 라이브 설정/시간/저장 및 Android 계측으로 재발 경로를 검사한다.352제품시험과74/74/73계측 통과는 실제 YouTube 자동 넘김 성공과 별개다.
- code18 당시 남은 재시험: 최초 라이브 인식 대기 원인 진단, 정식0초/5초 이동, 일반↔라이브/연속 라이브 및10연속 실제 전환. 후속code20에서 인식·0초/5초·OFF 대기는 위 기록처럼 통과했지만 연속 검증은 미완료다. 동일 노드를 다른 방송에 재사용하면 구분하지 못하고 안전 정지할 수 있다. 새 권한·OCR·영상/오디오 수집은 추가하지 않았다.

EN: Code18 adds structural recognition and independent live settings. Review fixes revalidate the final root, reject stale pager evidence, preserve legacy normal collection, and retain settled evidence on equal timestamps without adding time. Automated regression and installation checks passed at code18; later code20 device results are recorded above, separately from the unfinished consecutive run. Reused source nodes may conservatively stop the feature.

## 2026-08-28 · 과거 code16 통합 검증 체크포인트

제품 274시험·빌드·lint 0오류/3경고 PASS. API26/33/34 호환성 계측검사 47/47/46개와 설치 사전검사 각36개 PASS. 실제 Android 17 기기의 설치 사전검사36개, Android 설치창을 통한 code15→16 업데이트와 설정값 보존 PASS. 최종 APK703134bytes 및 설치본 SHA-256 `6FA61EA51C04AF5A8246E21183C7F4D9FDF0564FEEF5794553BEBEF7C1F4EFE1` 일치. 기존 GitHub 공개 자산 다운로드는 API26/API34/실기기 PASS. 이는 게시 전 결과이며 최종 Release·CI·공개 파일 검증은 [검증 기록](VERIFICATION.md)을 따른다. 후속 계측으로 끊긴 실기기 접근성 연결은 D-031의09:31 재시험에서 복구를 확인했다.

## D-031 · 실기기 계측 종료 후 접근성 서비스 미연결 · 수동 재연결 확인

- 증상/재현: 앱 상단의 사용 준비 안내가 지속된다. 접근성 enabled 설정과 오버레이 허용은 남아 있으나 서비스가 실제로 연결되어 있지 않다.
- 직접 확인/증거: 접근성 진단에서 Bound 목록은 비어 있고 Crashed 목록에 자체 서비스가 있다. exit-info에는09:26:47 `PACKAGE UPDATED`,09:27:22/23 계측 시작·종료 관련 `FORCE STOP` 기록이 있다. Java fatal crash 로그는 확인되지 않았다.
- 원인 범위: 실제 OS 업데이트 뒤 실행한 네트워크 계측의 시작·종료가 대상 앱과 서비스를 끊었고 Android가 재연결하지 않은 상태다. Crashed 목록만으로 제품 코드의 Java 크래시라고 단정하지 않는다.
- 영향 범위: 해당 실기기의 접근성 서비스 연결 및 자동 넘김 실행 준비. 업데이트 설치 성공·APK 해시·설정값 보존 결과와 별도로 관리한다.
- 잘못된 접근: 권한 설정이 켜져 있다는 이유로 서비스 연결도 정상이라고 판단하거나, 원인을 확인하지 않고 권한을 반복 설정하도록 안내하는 것. 실제 설치 뒤 대상 앱을 종료시키는 계측을 다시 실행한 시험 순서도 부적절했다.
- 대응: 원인을 확인한 뒤 접근성 서비스를 시스템 설정에서 수동 OFF→ON하여 재연결했다. 제품 코드·APK·저장 설정은 변경하지 않았다.
- 자동 재발방지: 자동 재연결 기능을 추가한 것은 아니다. 시험 절차에서 파괴적 계측 후 enabled 설정·bound 서비스·제품 runtime 상태·실제 사용 흐름을 함께 확인한다. 실기기 최종 설치 이후에는 계측을 재실행하지 않고 네트워크 시험은 설치 전이나 에뮬레이터에서 수행한다. 상태 복구 후 다시 검증한다.
- 재시험:09:31 수동 OFF→ON 후 `Bound{자체서비스}`, `Crashed{}`, `runtime connected=true`, `enabled=false`, `blocked=false` 확인. 실제 화면에서 상단 사용 준비 바로가기(setupJump)가 사라진 것을 육안 확인했다. 제품 코드 수정 없이 계측 후 연결 상태를 복구한 결과이며, 이 재시험에서 소셜앱 자동 넘김 E2E를 새로 수행한 것은 아니다. [검증 기록](VERIFICATION.md) 참조.

## D-030 · 새 업데이트 경로 캐시·취소·손상 사본 복구 · 0.2.5

- 증상/재현: 캐시에 저장된 후보가 사라짐; 검사가 끝나는 순간 취소하면 설치 준비로 표시될 수 있음; 같은 해시 이름의 0바이트 설치 사본이 남으면 재다운로드 후에도 설치 실패.
- 직접 원인: 캐시 유효성에 설치 버전 0을 넣어 신규 버전 비교 함수를 오용; 해시 검사 이후 취소 재확인 누락; 기존 설치 사본을 무조건 재사용.
- 증거/영향: 독립 코드 리뷰에서 재현 경로 확인. 신규 업데이트 경로만 해당하며 기존 소셜앱 감지에는 영향 없음.
- 잘못된 접근: 캐시 유효성과 실제 설치 버전 비교를 동일시하고, 다운로드 완료를 취소 불가능 시점으로 취급; 사본 파일 존재를 정상 완료로 간주.
- 수정: 양수 코드 범위로 캐시 검사, 파일 검사 후 및 UI 콜백에서 취소 재확인. 해시별 읽기 전용 설치 사본을 사용하고 정상 사본만 재사용. 손상 사본은 임시 복사본 검증 후 원자 교체하여 복구.
- 자동 재발방지: UpdateClientChecks 캐시·손상 다운로드·취소 회귀; InstallerArtifactChecks 손상 사본·부분파일·메타데이터·provider 제한 시험. 제품 APK에 시험 자산/계측 경로가 섞이지 않는 정적 검사.
- 재시험: API26/33/34 설치 사전검사 각각36개, 실제 Android 17 기기36개 PASS. 손상 사본·부분파일 복구, 메타데이터 불일치, provider 쓰기·경로 거부를 확인했다. 실제 OS 설치 code15→16 및 설정값 동일성 PASS. 최종 실행 결과와 설치 증거는 [검증 기록](VERIFICATION.md)을 따른다.

## D-029 · 구형 기기 런처 아이콘 패키징 누락 · 0.2.5

- 증상/재현: API26 실제 에뮬레이터의 앱 상단 아이콘이 비어 있었음.
- 직접 원인: 소스에는 기본 mipmap이 있으나 기존 증분 빌드의 병합 자원/APK에는 anydpi-v33만 포함됨. 기존 공개0.2.4 APK에서도 같은 누락 확인.
- 증거: AAPT2 resource dump와 API26 캡처 비교. 소스 존재만 검사해서는 발견할 수 없었음.
- 영향: API26~32의 런처/앱 헤더 아이콘 표시. 감지 알고리즘 변경 아님.
- 수정: app:clean 이후 새 빌드로 기본 anydpi와 v33 자원 동시 포함.
- 자동 재발방지: verify.ps1 PACKAGED_LEGACY_ICON_AUDIT가 완성 APK의 실제 기본 아이콘 구성 검사. 계측시험은 실행 OS에서 drawable 로딩 검증.
- 재시험: 호환성 최종 후보660843bytes/C52883CD…의 API26/33/34 18/18/17검사 및 API26 실제 아이콘 표시 PASS. 이전660364bytes/05E61E8F… 후보는 보존하되 배포 제외. 이후 통합0.2.5도 패키징 검사 및 API26/33/34 호환성 계측47/47/46개 통과.

## D-028 · 구형 OS 기능 설명·타일 상태 표시 · 호환성 개선

- 증상/재현: API26~28은 Tile.setSubtitle이 없어 기존 고정 라벨만 보였다. 구형 OS는 캡처를 못 쓰는데 공통 권한 안내에는 버전 조건이 없었다. 소스/API 리뷰 확인이며 실기기 크래시 관측이 아니다.
- 직접 원인/증거: 기존 타일의 상태가 API29+ subtitle에만 전달됨. 창 캡처는 API34 전용인데 공통 설명과 캡처 capability 선언이 모든 기기에 적용됐다.
- 영향 범위: 구형 타일 상태 전달 및 사용 가능 기능/권한 설명. 기존 스와이프·반복 감지 오류를 해결했다는 뜻이 아니다.
- 잘못된 접근: API 호출 가드만으로 사용자도 지원 여부를 알 수 있다고 가정. 타일 이름과 긴 상태를 이어 쓰면 실제 상태가 밀릴 위험도 있다.
- 수정: 구형은 상태를 라벨에 우선, 앱 이름+전체 상태는 접근성 설명 유지. API33 미만은 수동 추가 안내창. API34 전용 클래스와 XML 분리, 구형은 no-op. UI/권한 안내에14+ 조건·미설치·미선택 이유 명시, 저장 선택은 보존.
- 자동 재발방지: FeatureSupportPolicy 14시험(API28/29·32/33·33/34 경계/저장/미설치/실제광고상태), verify.ps1의capability/신규API격리/팩토리/minSdk/XML동일성 검사. 에뮬레이터 전용 UI·XML·저장·factory 계측시험 추가.
- 리뷰 수정: 계측시험이API34+IG설치정상문구를오판하는 조건을 정확 리소스별 비교로 수정. 제품 C/H 신규 회귀는 발견하지 못했다.
- 재시험: 제품241시험·빌드·lint0오류3경고 PASS. 계측 실행 결과/미검증은 [검증 기록](VERIFICATION.md)을 따른다. 실제 서비스 연결·타일 조작·소셜앱 자동넘김과 UI smoke를 혼동하지 않는다.

### 테스트 환경 메모

최신 SDK 명령행 도구의 sdkmanager 배치 래퍼는 세미콜론 패키지를 잘못 분리했다. 공식 android.exe sdk install을 직접 호출했고 avdmanager에는 실제 SDK toolsdir를 지정했다. Windows 한글 AVD 경로에서 qemu-version 쓰기 오류가 관측되어 원래 프로젝트를 옮기지 않고 별도의 ASCII 임시 AVD를 생성했다. 이는 제품 코드 버그가 아니며 원래 자료는 삭제하지 않았다.

## D-027 · 시간제 실패를 플로팅에서 진행 중으로 표시 · 리뷰 수정

- 증상/재현: 시간제 넘김 요청 거부/범위 실패에서 서비스는안전정지하지만 `status.startsWith("시간제")` 라벨분기가 `다음`을계속표시했다. 실제기기실패재현이아닌소스리뷰에서확인.
- 직접원인: 실패와진행문구의접두가동일한데접두일치만으로전환중을판정했다.
- 영향범위: 새시간제플로팅표시만. 추가스와이프를발생시키는버그는아니다.
- 잘못된접근: 문자열접두를상태열거형처럼사용해오류까지묶었다.
- 수정: `시간제 · 다음 영상 확인 중` 정확일치일때만 `다음`, 그외기존표시/상세상태복귀. pager package/window/id검사도재확인.
- 재발방지/재시험: 독립리뷰에서원인과수정재확인,빌드/lint/연결기능224시험PASS. 전체244시험중미연결VisualSequence기존2실패는별개다. 기기요청거부강제재현은미실행.

## D-022 후속 · 정확한 길이 감지 대신 제한된 시간제 대안

- 기존정확N회감지문제의원인해결은아니다. 별도OFF토글/초입력/±1초로정보없는영상의무한대기를보완한다.
- 기본15초/5~120초/초기2초확인포함,실제시간이있으면기존N회우선,중간재생대기0은대체하지않음. 정지/메뉴/창변경시취소,단일pager요청후다른식별정보확인,실패안전정지.
- 자동방지: 타이머24시험/저장7추가시험,실시간우선·IG한정·0차단·strict확인·입력원문보존정적연결검사. 별도오디오·화면주기실험통합없음.
- [시간제계약과실제결과](TIMED_FALLBACK.md). 개인정보화면/숫자원시로그는private에보관한다.

## D-026 · 실제 음향 입력 정상인데 반복 후보 없음 · 진단 보강/미해결

- 증상/재현: 같은짧은IG영상의0.2.1 시험앱60초세션에서유효소리가들어와도최종후보0. 영상이10초미만이라는초기추정은미검증이며별도화면10.4초후보도정확길이아님.
- 직접확인:598프레임중572유효,초기작은소리14이후증가없음/좁은대역12,history/gap리셋0. 검색119/최근품질거부6/거친후보없음2/개별후보거부776. WHOLE_QUALITY·FLAT_MINIMUM을관측.후보선택·검증단계까지진입후미검출.
- 확인증거:[음향시험상세](AUDIO_PATTERN_TRIAL.md),private의audio-diagnostics-v021a-run1/final집계. 실제마이크/PCM파일저장없음.
- 영향:별도음향시험코어의실제콘텐츠미검출. 기존제품자동넘김에연결하지않음.
- 잘못된접근:소리수신블록을유효특징으로간주,영상길이를원인으로단정,수신공백없이도초기화탓으로설명,개별평가수를영상/검색수로표현,마지막탈락기록을현재유일원인으로해석.
- 수정/재시험:검사기준은변경하지않고원인집계만추가.기존38+진단10시험/빌드/설치/실제수신·60초종료확인. 후보검출은미해결이다.
- 자동재발방지:진단합계·reset보존/clear초기화·짧은휴지거부·평평한최솟값·분할입력불변 시험. 실제음악정확도보장은없음.
- 다음:진짜주기후보가상위8개선택에서배제됐는지와평가조건에서탈락했는지,주기구간별집계로분리. 현재정보만으로전체품질95%하향등의추측수정은하지않음.

## D-025 · 음향 주기의 홀수 배수 경쟁으로 정상 후보 소실 · 합성 회귀 수정 확인

- 증상: 합성3초/6초 반복을59초 입력하면 초기에 나타난 정상 후보가 최종 LISTENING/후보없음으로 바뀐다.
- 재현/증거: 독립 리뷰에서3초/6초 양성 시험을 추가하도록 지적했고 코어 작성자가 재현했다. 실제 릴스가 아닌 알려진 합성 PCM 시험이다.
- 직접 원인: 기본 주기와 홀수 배수(예6초와18초)를 서로 경쟁하는 다른 후보로 취급해 보수적 모호성 검사에서 모두 거부했다.
- 영향: 별도0.2 오디오 시험 코어의 짧은 주기 양성 누락. 자동 입력 경로는 없고 기존 ShortsLoop 제품에는 영향 없다.
- 잘못된 접근: 반/4분의1 주기의 비교만으로 모든 배수 후보가 제거된다고 간주했다.
- 조치: 같은 정수배 계열을 묶고 비슷한 근거를 가진 최단 기본주기를 우선한다. 서로 관계없는 경쟁 후보 거부는 유지한다.
- 재발방지/재시험:3초/6초59초 입력 회귀시험 포함 새코어25시험 PASS, 기존SignalMeter13개와 함께38시험 PASS. 독립리뷰 미해결P1/P2지적0. 합성 입력의 수정 확인이며 실제릴스 주기/영상끝/자동넘김 성공으로 확대하지 않는다.

## D-024 · 오디오 시험 전부0 · 음소거 조건과 캡처 차단 구분

- 증상/재현: audio-probe0.1후보A,OS승인후IG재생중에도PCM모두0. 세션1 955248표본/신호0.
- 직접 확인: 시험앱RECORD_AUDIO허용/Projection존재/REMOTE_SUBMIX not silenced. 미디어음량0, 이후일시정지UI에IG자체음소거도확인. 실제플레이어캡처금지로판정할증거는없었다.
- 영향/잘못된접근: 캡처승인이나표본수만으로소리수신성공이라보거나,음소거상태의0만으로캡처불가라고보면잘못된진단. 이둘을구분한다.
- 조치: 두번째시험세션에서음량1및IG자체음소거해제후재생으로양성대조. 코드/권한우회없음.
- 증거/재시험: 45.635초까지신호0→56.711초492신호구간/최대-11.07dBFS/peak17222.최종648신호구간/비영0표본22.99%,60.172초자동종료. [상세](AUDIO_PROBE_TRIAL.md#기기-수신-시험--device-capture-test).
- 재발방지: 기존SignalMeter의무음/미세신호시험과UI의“무음만으로차단단정불가”안내유지. 문서에폰음량과IG자체음소거별도점검추가. 제품의음소거자동탐지/반복계산은미구현이며자동재발방지완료로표현하지않는다.
- 한계: 미디어음량0/IG소리ON조합은미검증. 이결과는단일영상입력확보이지모든영상/음악주기/정확N/20연속성공이아님. 음량0·IG음소거·재생복원완료.

## D-023 · 시험판 화면 분석 후보 정보 손실 · B 수정/기기 캡처 진입 확인

- 증상/재현:0.2.3-visual-test A 설치·화면 보조 동의ON·IG무시간 영상에서 기존 정보 없음 상태,frames0/errors0/requests0 지속.
- 직접 원인: ShortsReader가 앱별 identity를 접두 처리한 뒤 기존5인자 YouTubeSnapshot 생성자로 재구성하여 visualCandidate=false로 초기화. InstagramReader의 후보 판정은 서비스까지 전달되지 않았다.
- 증거: `v023a-first-visual.jsonl`30표본 및 라우터 코드. B 설치 후 `v023b-clockless-first.jsonl`에서 visual=true/frames증가/errors0 확인. 데이터는 private.
- 영향: 새 화면 보조 경로 전체. 기존 시간값 기반 및 광고 경로의 기본 메타데이터는 유지돼 있었음.
- 잘못된 접근: Snapshot필드 추가 시 앱 라우터의 재구성 경로 누락. 초기 정적 리뷰도 놓쳤고 합성 코어 시험은 해당 통합 경로를 덮지 못했다.
- 수정: 메타데이터를 보존하는 withIdentity 복사 메서드로 통일. 구조/기능의 큰 변경 없음.
- 자동 재발방지: 후보/일시정지/광고/빈identity/창ID 보존3개 JVM시험, verify.ps1 라우터 연결 검사. 실제 Rect/Android 캡처를 JVM PASS로 주장하지 않는다.
- 재시험:186시험·빌드/lint·독립 정적 리뷰 PASS. 설치 APK 해시 일치, 실제 캡처 진입 확인. 대상 정지에 가까운 영상의 학습/이동은 별개 미해결이며 D-022를 해결로 닫지 않는다.

## D-022 · 일부 Instagram 카운트 0 대기 · 실패 경로 확인/미수정

- 최신 C차 시험: 숫자 이벤트18초0건, 수동 일시정지 양성 대조9건은 시간값 없음. 화면 주기A10.4초 재현/B14.75초/정상D16.205초→약16.2초는 새로운 후보이나 정확한N회나 종료점 확인이 아님. 정지 화면 기기32표본 Java 후보0, 수학 합성3시험 PASS. 세부 로그/한계는 [조사 문서](INSTAGRAM_TIMING_RESEARCH_2026-08-27.md).
- 최신 연속 기준 재시험: 18:00:33부터 수동 탭/스와이프·재시작 없이16.205초 영상 정상 집계→18:00:54.650 요청1→18:00:56.036 확인1. 다음 영상은0/1·정보 없음,18:01:25.823까지 후속 창에서도 유지. 광고 이동0.20개 연속 기준 실패, 수동 이동으로 정상만 골라 성공으로 계산하지 않음. 비공개 `d022c-sequence-01.jsonl`, `d022c-sequence-stalled.jsonl`과 화면 대조.16.205초 값은 마지막 정상 영상이며 실패 영상 길이가 아님.
- 새 접근의 위험/재발방지: 화면 후보는 최소약2.1주기 관측과 실제 시작점 판정이 필요. 영상 내부 반복·부분 일시정지·자막 움직임·수동 전환을 영상 종료로 간주하지 않는다. 기존시간 우선/동일영상·창·연속성/정지·탐색 취소/1요청1확인 보호를 갖춘 별도 보조 기능 검토가 필요. 제품 미구현이므로 자동 재발방지 완료가 아님. 새 캡처 capability 추가/시험 설치는 이 단계에서 미실행, 실행ON·기존 설정 복원.
- 후속 조사: [대체 시간원 조사](INSTAGRAM_TIMING_RESEARCH_2026-08-27.md) 실시. 문제 트리의 다른 RangeInfo·선택 시간문구·일시정지 controls·MediaSession에서 쓸 수 있는 대체 시간 미확보. 정상 대조는26.1초 진행값 확인. 새 TimingProbe만 추가, 제품 미수정 유지. 초기 프로브의 전체문자열 매치/자식 갱신 제한은 보강 후 재조회. 출력 절단/조회 중 수동 이동 겹침은 증거 제외, 이벤트 빈 출력은 판정 불가. 전체 내부 시간 부재로 확대하지 않음.
- 증상: 일부 릴스에 진입하면 현재 회차0/목표1에서 집계·자동 넘김이 멈춤. 처음에는 기기 미연결로 원인 미확정이었으나, 2026-08-27 USB 연결 후0.2.2/code9에서 같은 증상 관측. 최초 조사 대상 링크는 웹 조회 제한·기기 링크 열기 차단으로 직접 재현하지 못했으며 다른 콘텐츠의 증거와 구분함.
- 재현 방법: 전체 화면 Instagram에서 실행ON·목표1을 유지하고 콘텐츠 내부 상향 수동 스와이프로 다음 릴스 이동, 짧은 서비스 표본과 화면을 비교. 감지 시작 여부 비교를 위해 정상/실패가 확인되면 완주를 기다리지 않고 이동. 01~12 라벨 구간 사이 진단 수동 이동11회, 일부 구간 사이에는 제품 자동 이동도 발생했으므로 전체 피드 항목을 빠짐없이 센12회 시험은 아님.
- 직접 원인: 현재 `InstagramReader`는 알려진 단일 동영상 구조와 `com.instagram.android:id/scrubber` SeekBar의 RangeInfo를 필요로 함. 시간 정보가 없으면 `usable=false`, 서비스의 `invalidate()`가 카운터를 초기화하고0과 대기 사유를 발행. 반복 집계/자동 넘김 판정에 도달하지 않음. 별도 프로브 두 장면에서는 정확한 scrubber 노드 자체 미검출로 좁힘. 앞선02/05의 같은 사유만으로는 노드 부재와 RangeInfo 거부를 구분할 수 없음.
- 확인 증거: 02/05에서 `재생 정보 없음 · 이 릴스는 수동 넘김 필요`, current=0, connected/enabled=true, blocked/pending=false. generation은 증가하지만 자동 요청/확인 누계는 그대로여서 폴링 중 감지 대기임. 이후 정상 릴스로 수동 이동하면 토글/재시작 없이 current=1과 시간 진행 복구. D-019 전환 실패 안전정지나 앱 전체 동결로 해석하지 않음.

| 비교 구간 | 실제 관측 | 해석/제한 |
|---|---|---|
| 01,03,04,06~12 | 현재1과 시간 진행 | 10개 라벨 구간에서 감지·집계 시작 확인. 완주 자동 넘김 전체 E2E는 아님;04는 광고 자동 전환 후 일반 릴스 |
| 02 | 17:17:13.557~16.476의7표본, 17:17:26.973~29.472의6표본에서 재생 정보 없음/0 | 두 관측 창 사이 약10.5초 공백. 약16초를 연속 표본으로 관측했다고 주장하지 않음 |
| 05 | 17:18:31.041~33.044의5표본에서 재생 정보 없음/0 | 약2초 관측. 첫 표본은 직전 영상, 남아 있는2.82/20.433초는 이전 유효값이므로 새 영상의 시간으로 사용하지 않음 |
| 추가 문제 장면 | 제품ON에서 재생 정보 없음/0 포착 후 OFF 분리 프로브14표본에서 scrubber 미검출 | 약0.55~5.84초. 해당 관측 창의 미검출이며 영구 미노출의 증명은 아님 |
| 분리 프로브 다음 장면 | 제품OFF에서14표본 scrubber 미검출 | 약0.51~5.76초. 제품이 꺼져 있으므로 이 장면의 제품 카운트 실패까지 확인했다고 하지 않음 |
| 분리 프로브 정상 대조 | fresh/visible=true, min=0/max=26100/current=10203/type=0 | 같은 도구로26.1초 유효 RangeInfo 확인. 초기 탐색10초로1표본만 확보 |

- 원시 증거(비공개): `비공개 증거 자료`, `d022-usb-02.jsonl`, `d022-usb-02-confirm.jsonl`, `d022-usb-03~12.jsonl` 및 대응 화면. 추가 프로브는 `비공개 증거 자료`, `d022-next-also-missing.txt`, `d022-normal-scrubber.txt`. 화면에는 계정·콘텐츠가 포함되므로 공개 제외. 프로브의 next-control 파일명은 정상 여부를 뜻하지 않음.
- 시험 분리: 자체 실행OFF 확인 후 기존 읽기 전용 프로브 실행. 로컬/기기 JAR SHA256 일치(`69717DFE9624FFB8E76754B91E825AA515E22AFF43858F5860C884535FFC6B6D`). 프로브는 해당 ID만 조회하며 다른 시간원을 전수 조사하지 않음. 실행기 `OK (1 test)`는 앱 동작 PASS가 아님. 분리 시험 뒤 서비스 재생성·누계 초기화가 있어 시험 전후 누계를 합산하지 않음.
- 복원: 실행ON, 두 앱 선택·목표/기준1·광고ON·플로팅ON 유지. `d022-restored.jsonl` 17:27:19.474~23.660의8표본에서11.207→15.640/26.1초 정상 진행. 현재0의 사유는 `다음 처음 재생부터 계산`으로, 중간부터 실행한 정상 시작 대기이며 앞선 정보 없음과 다름. 대응 화면 육안 확인.
- 영향 범위: 현재 reader가 시간 정보를 얻지 못하는 일부 Instagram 릴스. 모든 릴스/모든0표시/원본 링크까지 동일 원인으로 확대하지 않음. 왜 일부 콘텐츠가 이 노드를 제공하지 않는지, 다른 시간원이 있는지는 미확정.
- 잘못된 기존 접근: URL·이미지 외형만으로 콘텐츠 형식 단정, 눈에 재생바가 안 보인다는 사실만으로 접근성 노드/모든 시간 정보 부재 단정, 짧은 실패를 장시간 정지로 표현, 수동 시험 이동을 제품 자동 성공으로 계산, 근거 없이 권한/배터리/광고 문제로 단정하거나 무조건 시간제 스와이프 추가.
- 수정 내용: 제품 코드·설치·권한 변경 없음. 진단용 실행OFF→ON만 수행하고 설정 복원. 문서만 로컬 갱신, 새 빌드·게시 없음. 플로팅의0만으로 감지 불가/시작 대기가 구분되지 않는 UX 문제도 확인했으나 미수정.
- 자동 재발방지: 미구현. 다음 변경에서 노드 없음/RangeInfo 없음·거부/정상→미검출→정상 복구를 구별한 상태·회귀시험을 설계하고, 대체 시간원이 실제 관측되는지 먼저 확인할 것. 추정 시간으로 정확한 반복 횟수를 보장한다고 하지 않음.
- 재시험/리뷰: 짧은 정상 대조·수동 다음 이동 복구·실행 복원 확인. 독립 읽기 전용 리뷰로 표본 수/공백/서비스 재생성/원인 범위 해석 대조 완료. 수정 후 시험·전체 재생 회귀는 미실행, D-022 미해결 유지.

## D-021 · 18초 영상16→0 반복을 탐색으로 오인 · 미해결/디버깅 보류

- 후속 code16 관측:19초 일반 영상의17→0(약320ms 관측 간격)에서도 반복 경계 추적 문제가 남았다. 이는 code18 신규 관측이 아니다. 원래18초/16→0 재현을 아래에 보존하며 code18에서 D-021 수정·실기기 재시험을 완료한 것으로 표시하지 않는다.
- 증상: 재생은 반복되는데 현재 회차/자동 넘김 요청이 늘지 않음.
- 재현: 전체 화면18초 YouTube 영상, 약300ms 관측에서 마지막 표시16초 후0초로 복귀. 16:10:36.948,16:10:55.578,16:11:13.786에 반복 관측, 간격18.630/18.208초.
- 직접 원인: LoopCounter의18초영상 끝 경계는17초 이상인데 이전값16이므로 wrap 대신 음수 시간점프 경로에서 집계를 초기화. 왜 앱 표시가16에서 끝나는지(표시 갱신/길이 오차)는 별도 미확정.
- 증거: 비공개 증거 자료65표본. 동일 세대1582/blocked=false/요청16·확인15 유지, jump elapsed307~310ms. 독립 검토자의 기존 컴파일 클래스 재현에서16종료는2회 반복해도현재1/advance0,17종료는정상 누적 확인.
- 영향: 해당 경계 조건의 일반 영상 집계. 전체 영상·Instagram에 동일하게 재현된다고 단정하지 않음.
- 잘못된 접근: 모든 정지를 배터리나 반복 누적 초과로 설명하거나, 끝 경계 허용폭을 무조건 크게 늘려 수동 탐색까지 완주 처리하는 것.
- 수정: 없음. 추가 디버깅/자동수정은 보류 상태.
- 자동 재발방지 계획:18초/16→0/N1·N2, 수동 되감기, 조회공백, 짧은3초영상600ms 조건을 함께 시험할 것. 아직 새 자동시험 추가 없음.
- 재시험: 기존 클래스 독립 재현만 수행. 수정 후 실기기 재시험은 미실행.

## D-020 후속 · 자체 앱 제한 없음 적용, 제한적 개선 확인

- 자체 앱 배터리 '제한 없음' 적용, 예외 목록/커널 frozen=0 확인. 다른 앱/전체 절전 정책/권한 변경 없음.
- 동일 프로세스에서 실행 재시작 없이 응답 복구. 약56초 PiP+홈 대기50표본 응답 유지와 전체 화면 시간 감지 재개 확인.
-0.2.2에 배터리 상태와 수동 안내 메뉴 추가. 자체 앱만 열고 상태조회만 하도록 정적 회귀 검사/독립 리뷰 PASS. 앱이 OS 정책을 자동 변경하지 않음.
- D-019 재발:16:12 요청18/확인16/blocked=true인데 kernel frozen=0. 동결 해소가 전환실패 해소와 같지 않음. 최초 실패 트리거는 아직 미확정.
- 장시간/재부팅/새 메뉴 실기기 검증은 미실행. 이하 '조치 미실행'은 이전 진단 당시 기록.

## D-020 · 전체 화면 복귀 후에도 서비스 무응답 · Samsung Freecess 동결 확인, 조치 미실행

- 증상/재현: PiP 전환 후15:56부터 서비스 진단 timeout. YouTube 전체 화면 복귀를16:01 실제 캡처와 topResumedActivity로 확인했으나 진단 응답은 회복되지 않음. 실행 토글/앱 설정은 건드리지 않음.
- 직접 원인/증거: 동일 앱 PID의 cgroup.freeze=1, cgroup.events populated=1/frozen=1을 읽어 실제 프로세스 동결 완료 확인. Samsung `dumpsys activity freecess`의 mFreezedPackages에도 자체 앱이 있음. 이전 ProcessRecord.isFrozen=false는 커널 실제 동결을 반증하지 않으며, 단독으로 동결 배제에 사용하면 안 됨.
- 시간 증거: 자체 앱 MARs 기록은15:56:30.172 FRZ(Bg),16:01:08.233 UFZ(Binder accessibility),16:01:14.240 FRZ(Bg). 전체 화면 복귀 시 잠깐 깨더라도 약6초 후 다시 동결된 기록임.
- 추가 진단: 자체 앱 SIGQUIT을16:03:59.739 요청. 동일 시각 UFZ(Signal), ART Signal Catcher/밀린 dump 출력 확인 후16:04:05.749 FRZ(Bg). 이 구간은 진단 신호로 깨운 것으로 자연 회복 PASS가 아님. EPIPE는 이미 제한시간이 끝난 dump 출력에 대한 후속 오류와 부합하며 최초 정지 원인으로 취급하지 않음.
- 진단 제한: JDWP 연결 실패, 임시 포워딩 제거. debuggerd는 root 필요 응답으로 중단, root/보안 우회 없음. SIGQUIT은 진단 신호이며 앱 종료/데이터 삭제가 아님. 배터리·절전·권한·설치·앱 소스는 미변경.
- 영향: 앱 프로세스 전체가 동결되어 YouTube/Instagram 공통 서비스 폴링과 상태 응답이 실행될 수 없음. 단순 재생 횟수 누적 초과의 증거는 없음. D-019의15:46 최초 전환확인 실패까지 동일 원인으로 확대하지 않음.
- 잘못된 기존 접근: 살아있는PID/접근성등록/ActivityManager isFrozen=false만으로 실행 중이라고 판단. 피해야 할 대응은 카운터만 초기화하거나 확인되지 않은 반복 스와이프를 추가하는 것.
- 대응 계획: 자체 앱만 백그라운드 절전 예외를 적용하는 비교 시험. 이 설정으로 Freecess 문제가 해결되는지는 미검증이며 기기별 안내·서비스 실행 유지 보완은 별도 구현 범위로 검토. 시스템 전체 절전 기능 비활성화는 하지 않음.
- 자동 재발방지/재시험: 새로운 제품 수정/시험은 없음. 실기기 회귀 항목에 PiP·다른 앱 전면·전체 화면 복귀·수분 경과 후 서비스 응답/커널 동결/새 자동 요청 비교 추가. 현재 진단은 원인 확인, 수정 및 안정성 검증은 미완료.
- 독립 리뷰: 제공된 기기 증거와 공식 동결 의미를 대조하여 현재 무응답 해석 타당. 최초 실패·예외설정 효과는 미확정 유지. 검토자는 추가 기기 조작하지 않음.
- 근거: [Linux cgroup.freeze/events](https://docs.kernel.org/admin-guide/cgroup-v2.html), [Samsung 앱 관리와 예외 설정](https://developer.samsung.com/mobile/app-management.html). 후자는 절전 예외 안내이며 본 기기의 Freecess 해제를 보장하는 자료가 아님.

## D-019 · YouTube 넘김 확인 실패 후 정지 유지 · 0.2.1 원인 일부 확인, 미해결

### 추가 작은 창 시험 · 15:56 이후

- 작은 창 전환 후 최초 캡처는 홈 화면+YouTube 영상 창이며 Instagram 메인 화면이 아님. OS task의 mode=pinned, PictureInPictureMode=true로 PiP임을 확인.
- 첫 진단값은 connected/enabled=true,blocked=false,요청16/확인15,app=launcher,선택한 앱 대기. 숫자 position/duration은 이전 마지막 값이며 PiP 재생시간 추적으로 해석하지 않음.
- 이후 전면 앱은 Samsung Internet으로 관측되었고 제품 서비스 dumpsys가 반복 IOException: Timeout. ADB 연결·PID·접근성 등록은 유지, 자체 프로세스 isFrozen=false,TracerPid=0,crash 버퍼 출력 없음. 이는 진단 응답 실패의 증거이며 곧바로 앱 크래시나 PiP 원인 확정을 뜻하지 않음.
- observe-device의 이번 연속 관측은 No diagnostic service response로 중단되어 성공 표본으로 세지 않음. 이번 시험에서 입력/설정/권한/설치 변경 없음. 전체 화면 복귀 후 토글을 건드리지 않은 상태의 복구 비교 필요.

- 증상: YouTube 자동 넘김이 간헐적으로 동작하지 않음. Instagram 메인 + YouTube 작은 창의 동시 표시 정황이 있었으나 최초 실패 순간의 직접 관측과는 구분함.
- 관측: 15:46 요청11/확인10/pending 뒤 15:47~15:51 connected=true,enabled=true,blocked=true,요청11/확인10,상태 `넘김 확인 실패 · 껐다 켜 주세요` 유지. 해당 정지 구간의 화면 이동을 제품의 새 자동 요청 성공으로 계산하지 않음.
- 직접 원인: 마지막 요청의 다른 페이지 확인이 제한시간4.5초 안에 성립하지 않아 failClosed로 진입, 이후 tick이 즉시 반환. 권한 연결은 정상. 최초 확인 실패의 구체적 원인(실제 스와이프 실패/조회 불가/창 상태)은 당시 진단값만으로 구분 불가.
- 영향: 같은 실행 세션의 자동 넘김이 정지 상태로 남음. 두 앱 reader는 패키지별로 분리되지만 공통 실행의 blocked 상태는 공유함. 플로팅OFF에서 실패 상태가 시청 화면에 드러나지 않음.
- 잘못된 접근: 한 번 화면이 바뀌거나 과거 회귀시험이 통과했다는 이유로 현재 정상이라고 판단, PiP가 있었던 정황만으로 최초 실패 원인 확정. 이를 철회하고 요청/확인 카운터와 실제 화면을 함께 대조함.
- 진단/복구: APK·코드·권한은 변경하지 않음. 진단자가 빠른 설정을 열어 자체 타일OFF→ON으로 정지 해제 후 패널을 닫음. 이는 제품 스와이프가 아닌 별도 시험 조작. 제품 제스처는 page의 y75%→25%,280ms 상향이며 화면 최상단에서 시작하지 않음.
- 재시험: 15:52:03~15:54:24 전체 화면 비조작 관측에서59→58→15→11초 영상, 추가요청3/추가확인3(누계11/10→14/13). 전환 대기·확인 및 다른 영상 화면 확인. 기존 실패1건이 사라진 것처럼 누계를14/14로 기록하지 않음.
- 증거: 비공개 증거 자료, v021d-fullscreen-observation.jsonl, v021d-fullscreen-transition.jsonl, v021d-second-transition.jsonl 및 동일 접두 실제 화면. 시청내용/계정은 공개 문서에 옮기지 않음.
- 자동 재발방지: 기존 AdvanceGate 타임아웃·중단 보호 시험 존재. 이번에는 새 시험/제품 수정 없음. 다음 개선 검토는 실패 순간의 비개인 진단값 보존, 정지 상태 전달, 명시적 재시작 UX이며 미확인 제스처 무조건 재시도는 금지.
- 판정: 복구 후 해당3회 성공, 간헐 실패의 근본 원인 및 작은 창→전체 화면 회귀 미해결. 전체 안정성 PASS/수정 완료로 승격하지 않음.

## D-015 · 숫자 입력 완료 후 화면 전체 회색 강조 · 0.2.0 수정

- 증상/재현: 하드웨어 Enter로 입력 완료 후 비조작 컨테이너가 포커스를 받아 넓은 회색 강조가 생김.
- 직접 원인/증거: 포커스 회수용 content 컨테이너의 기본 focus highlight. 실제 화면과 SettingsScreen 구성 대조.
- 영향/잘못된 접근: 입력 완료와 포커스 해제 자체는 정상이나 화면 전체가 선택된 것처럼 보임. 비조작 컨테이너도 기본 강조가 적절하다고 가정.
- 수정: 해당 컨테이너만 setDefaultFocusHighlightEnabled(false). 버튼/입력 포커스는 유지.
- 재발방지/재시험: candidate-e 실제 입력 완료 화면 정상 확인. 120 JUnit 및 독립 정적 리뷰 PASS. 실제 UI 검사 항목으로 유지.

## D-016 · 정상 릴스에서 무재생바 광고로 넘어간 뒤 확인 실패 · 0.2.1 수정

- 증상/재현: 21.708초 일반 릴스 2회 후 광고로 물리적으로 이동했지만 requests=1/confirmed=0, 4.5초 뒤 안전 정지.
- 직접 원인/증거: 전환 확인이 다음 페이지의 재생시간을 필수로 요구. 광고에는 시간정보 없음. v020e-ig-two-plays-supported 숫자 로그와 실제 화면 대조.
- 영향/잘못된 접근: 카운트와 스와이프는 실행됐지만 다음 영상 재개 실패. 다음 페이지는 항상 재생시간이 있을 것이라는 가정.
- 수정: recognized와 usable 구분. 구조와 서로 다른 식별값이 확인되면 무시간 페이지도 전환 확인에 사용하되 반복 계산에서는 제외.
- 재발방지: 광고↔일반, 같은/연속 광고, 지연 이벤트, 타임아웃 단위시험. 고정 광고 식별값에서 pageChanged만으로 성공 처리 금지.
- 재시험: 0.2.1 candidate-b 광고→26.799초 일반 릴스 실제 요청1/확인1, 이후 일반 N1→37.313초 이동 확인. 일반→무시간 광고 및 연속 광고의 최신 빌드 실제 시험은 별도 필요.

## D-017 · 오른쪽 아래 광고 표시와 CTA 카드가 있는 영상 광고 미감지 · 0.2.1 수정

- 증상/재현: 우하단에 명확한 광고 글자가 있고 더 알아보기 카드가 떠 있는데 일반 영상으로 계산됨. 해당 광고의 실제 화면에서 재현.
- 직접 원인: 최초 종료형 광고에 맞춘 상단·부모와 동일한 bounds 조건만 구현. 영상형 광고는 clips_ufi_component 안의 우하단, 부모 터치영역보다 작은 글자 영역.
- 증거: 자체 실행 OFF 상태의 UI 구조 조회. id 없는 ViewGroup의 text/description 모두 정확히 광고, 비클릭 자식/클릭 부모, 우하단 동작열 조상. 영상/상품/계정 원문은 문서에 저장하지 않음.
- 영향/잘못된 접근: 서로 다른 광고 UI를 한 종류로 일반화. 재생바가 있는 광고도 존재하므로 재생바 존재 여부는 광고 판정 근거가 될 수 없음.
- 수정: 동작열 조상·우하단 작은 부모 영역·정확한 글자/설명·클릭 구조를 추가 검사. 캡션/작성자/일반 본문은 제외.
- 자동 재발방지: 우하단 배지 양성/조상 없음/큰 영역/캡션/부분문구/다른 클래스 음성 시험 4개 추가.
- 재시험: candidate-b 같은 광고 OFF 6표본 광고 대기/요청0, ON 후 광고 요청1/확인1 실제 PASS. CTA 미클릭, 다음 일반 릴스 실제 확인. 캡션 #광고 영상은 즉시 넘기지 않고 N1 완주 PASS.

## D-018 · 광고 넘김 확인 중 회전·드래그로 확인 보호 취소 · 0.2.1 독립 리뷰 수정

- 증상/재현: 광고 요청→pending 중 구성변경 또는 플로팅 드래그→invalidate→pending 취소→같은 광고에 즉시 재요청 가능. 정적 재현, 실제 동시 조작 재현은 미실행.
- 직접 원인/영향: 일반 카운트 초기화와 미확인 제스처 취소를 동일 처리. 즉시 넘기는 광고에서 중복 요청 위험.
- 잘못된 접근: 비종료 UI 변경은 언제나 안전하게 새 세션으로 시작할 수 있다고 가정.
- 수정: AdvanceGate.interrupt가 미확인 요청을 FAILED로 반환. 드래그/구성변경/탐색/패키지전환은 interruptSession→failClosed. snapshot 중 차단되면 같은 tick도 즉시 종료.
- 자동 재발방지: 광고/일반 pending interruption 및 idle interruption 3개 JUnit, 서비스 4개 경로·failClosed·blocked return 정적 연결 검사.
- 재시험: build/lint, 직접 JUnit148, 연결 검사 PASS. 독립 재리뷰 PASS, 미해결 P1/P2 0. 기기에서 pending 순간 회전/드래그는 현장 검증 필요. 명시적 설정 변경은 기존 새 설정 적용 정책 유지.

## D-014 · 짧은 라디오 행의 오른쪽 빈 공간 터치 무반응 · 0.2.0 기기시험 수정

- 증상/재현: 커버1248×1972의 간편 모드 행에서 x400,y800을 눌러도 모드0 유지. 같은 행의 원형 선택부 x130,y800은 모드1로 변경.
- 직접 원인: RadioGroup 기본 자식 WRAP_CONTENT 폭이 짧은 라벨 끝까지만 적용됨. 설명/원형 외 오른쪽 여백은 대상이 아니었음.
- 확인 증거: 실제 선택/tapMode 진단 대조, SettingsScreen.radio의 명시 LayoutParams 부재. 첫 시험의 QUICK assertion 실패는 이 UI 실패이고 모드 정책 자체 실패가 아님.
- 영향: 플로팅 터치 방식 두 선택행의 인간공학. 잘못된 값 저장/자동 제스처 발생 없음.
- 잘못된 접근: 그룹 전체폭이면 자식 라디오도 전체폭일 것으로 간주.
- 수정: RadioGroup.LayoutParams(MATCH_PARENT,WRAP_CONTENT) 명시,기존64dp 최소높이·단일선택·저장리스너 유지.
- 재발방지: 실기기 오른쪽 빈 영역 탭→선택표시/저장값 변경을 UI 회귀 체크에 추가. JVM시험을 터치성공으로 대신하지 않음.
- 재시험: 빌드/lint/JUnit120 PASS, 독립 정적재리뷰 새P1/P2없음. 최신설치판 빈영역 터치 결과는 VERIFICATION에 기록.

## D-001 · API 34 이전 타일 설정 실행 경로 lint 오탐 · 재시험 PASS

- 증상/재현: 첫 assembleDebug 성공 후 lintDebug가 StartActivityAndCollapseDeprecated 1건으로 실패.
- 직접 원인: AGP lint가 API 34 미만 분기에도 target SDK만 기준으로 이전 Intent API를 경고했다. PendingIntent 오버로드는 API 34 이후에만 존재.
- 확인 증거: app/build/reports/lint-results-debug.html 및 lint 텍스트 보고서.
- 영향: Android 8~13 호환 경로의 정적검사. 현재 폰에서는 API 34+ PendingIntent 분기 사용.
- 잘못된 기존 접근: 런타임 SDK 분기만으로 lint가 호환 경로를 자동 인정할 것이라고 가정.
- 수정: 이전 OS 전용 메서드로 격리, 내부 SDK 재확인과 해당 규칙만 좁게 SuppressLint. 최신 OS 경로는 PendingIntent 유지.
- 자동 재발방지: lintDebug 재실행. 독립 코드 리뷰에서 SDK 분기 검토.
- 재시험: lintDebug PASS, 오류 0. Android 13 이하의 실기기 실행은 미실행.

## D-002 · 전환 중 정보 공백이 확인 보호를 취소함 · 수정/재리뷰 PASS

- 증상: 자동 스와이프 후 시간바가 잠깐 사라지면 전환 확인 타임아웃이 취소됨.
- 재현: gate.begin → unusable snapshot → invalidate → gate.cancel 흐름. 독립 리뷰에서 확인.
- 직접 원인: 일반적인 조회 불가와 ‘스와이프 결과 확인 중’ 상태를 동일하게 초기화.
- 영향: 이전 영상에 남아 있어도 시간이 지나면 재스와이프할 가능성.
- 잘못된 기존 접근: 조회 불가는 언제나 카운트와 전환 보호를 함께 초기화한다는 가정.
- 수정: pending 상태의 공백은 보호 유지. 4.5초 타임아웃을 먼저 적용하고 불명확하면 차단.
- 자동 재발방지: 공백 후 같은 영상 timeout, 다른 영상 확인 복구, 늦은 확인 차단 시험.
- 재시험: 직접 JUnit 54개 PASS, 독립 재리뷰 PASS. 실제 YouTube 전환 공백 시험은 남음.

## D-003 · 짧은 영상 급격한 시간 이동을 완주로 오인 · 수정/재리뷰 PASS

- 증상: 3초 영상에서 600ms 만에 완주 판정.
- 재현: (재생초, 관측ms) = (0,1000), (2,1300), (0,1600). 목표1에서 advance=true.
- 직접 원인: 고정 2초 경계와 duration-4 허용폭이 짧은 영상에 지나치게 큼.
- 확인 증거: 독립 리뷰 JShell 재현. 해당 입력을 자동 회귀시험으로 추가.
- 영향: 짧은 쇼츠의 수동 탐색을 완주로 세어 조기 넘김 가능.
- 잘못된 기존 접근: 긴 영상용 초 단위 허용폭을 모든 길이에 동일하게 적용.
- 수정: 길이 비례 경계·관측 커버리지·최소 경과시간·급격한 전진 제한.
- 자동 재발방지: 600ms 탐색 차단, 정상 3초 완주 허용, 짧은 부분재생 대기 시험.
- 재시험: 직접 JUnit 54개 PASS, 독립 재리뷰 PASS. 실제 수동 드래그는 현장 검증 필요.

## D-004 · Gradle 시험 실행기 클래스 로딩 실패 · 대체 실행 검증

- 증상/재현: testDebugUnitTest에서 5개 시험 클래스 초기화가 ClassNotFoundException으로 실패.
- 확인 증거: app/build/test-results/testDebugUnitTest XML. javac 산출 디렉터리에 같은 class가 존재.
- 직접 원인 범위: 이 PC의 Gradle 시험 실행기/한글 classpath 경로 문제. 정확한 JVM 내부 원인은 미확정.
- 영향: Gradle 기본 runner에 한정. 동일 클래스·JUnit·Hamcrest를 직접 java -cp 실행하면 54개 모두 통과.
- 잘못된 접근 방지: runner 실패를 로직시험 성공으로 숨기거나 프로젝트를 임의 이동하지 않음.
- 대응: scripts/verify.ps1에서 시험 컴파일 후 직접 JUnit runner 실행. runner의 비정상 종료코드 차단.
- 재시험: Gradle task는 FAIL(환경)로 유지. 직접 JUnit 54개 PASS. 독립 검토자도 직접 실행 확인.

## D-005 · aapt 절대 한글 경로 인코딩 오류 · 상대 경로 검사 PASS

- 증상: APK에 절대 한글 경로를 전달하면 Illegal byte sequence/manifest 없음 오류.
- 원인: native aapt 인수의 경로 인코딩. APK 서명 검증과 Java 기반 빌드는 정상.
- 영향: APK 메타데이터 확인 명령에 한정.
- 잘못된 접근 방지: 이 메시지만으로 정상 APK가 손상됐다고 판단하지 않음.
- 대응/재현방지: 프로젝트를 작업 디렉터리로 설정하고 ASCII 상대 APK 경로를 전달.
- 재시험: 결과는 검증 기록에 반영. 원본/기존 산출물 이동·삭제 없음.

## 예방적으로 반영한 항목

- 다음 영상 전환 확인 전 연속 스와이프 방지, 확인 타임아웃 시 정지.
- 부분 재생/수동 탐색/긴 관측 공백/모드 변경 시 카운트 초기화 시험.
- 시스템 재연결 시 자동 실행 금지, 위치 저장값 손상 시 범위 보정.

## D-006 · 재생 시간 캐시 고정으로 카운트 증가 안 함 · 수정·실기기 재시험 PASS

- 증상/재현: 일반 YouTube 쇼츠는 재생/반복되는데 플로팅은 0/2. 0.1.1 서비스 진단 35개 표본에서 52초 영상의 진행시간이 41초로 고정.
- 직접 원인: SeekBar의 접근성 노드를 재조회해도 캐시가 반환되며, 이 환경에서는 재생 진행마다 캐시가 갱신되지 않았다. 기존 코드는 최신 상태 요청 없이 description을 파싱했다.
- 확인 증거: 비공개 증거 자료. 같은 화면에서 0.1.2 적용 후 17→19→21초, 51→0초 및 0/2→1/2 확인. 영상 제목/계정은 기록하지 않음.
- 영향: 시간 기반 반복 감지 전체. 시간이 정체되어 조기 넘김 대신 무기한 대기가 발생.
- 잘못된 기존 접근: getRootInActiveWindow/getChild를 다시 호출하면 자식의 시간 설명도 최신일 것이라는 가정. 구현 전 UIAutomator 관측을 실제 서비스 검증으로 대체할 수 없음.
- 수정: SeekBar에 refresh()를 요청한 후 성공/가시성을 확인하고 파싱. 실패 시 조회 불가로 처리하여 기존 카운트 초기화·전환 확인 보호 유지.
- 근거: [Android AccessibilityNodeInfo.refresh](https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo#refresh()).
- 재발방지: 직접 JUnit 반복/공백 회귀 54개와 실제 서비스 연속 관측을 함께 수행. Android 노드 캐시 자체의 자동 모의시험은 미구현이며 릴리스 전 실기기 진행→반복→넘김 검증 필수.
- 재시험: 0.1.2 시간 갱신 확인, D-008 추가 수정 후0.1.4에서 목표2의 두 번 재생과 목표1의3개 연속 자동 전환 PASS. 독립 리뷰 PASS.

## D-007 · 시험 도구 간섭과 폴드 스크린샷 오염 · 시험 절차 교정

- 증상/재현: UIAutomator dump와 접근성 서비스 동시 사용 시 서비스 재연결/비활성화 관측. 또한 다중 화면에서 display 미지정 screencap의 stdout 경고가 PNG 앞에 붙음.
- 원인 범위: 접근성 자동화 연결의 시험 간섭 및 screencap 출력 스트림 혼합. 앱의 카운트 오류와 분리해야 함.
- 영향: 잘못된 시험 상태·손상된 캡처. 앱 자체 스와이프 성공/실패의 증거로 사용할 수 없음.
- 잘못된 기존 접근: UI dump를 무간섭 조회로 간주하고 현재 활성 패널을 명시하지 않음.
- 대응: 실제 앱 시험 중 UIAutomator를 사용하지 않고, 숫자 상태만 출력하는 서비스 dump와 명시한 활성 디스플레이 캡처 사용. 사전 관측과 앱 검증을 분리.
- 재발방지: 관측 스크립트는 서비스 dump만 사용. 캡처 스크립트는 display를 필수 숫자 인수로 받고 PNG 시그니처를 자동 검사, 생성 후 육안 확인. private 파일은 덮어쓰거나 공개하지 않음.
- 재시험: 0.1.2 연속 서비스 관측 중 연결 유지 확인. 명시 디스플레이 PNG 열기 PASS.
- 창 축소 관측: 다른 앱으로 이동할 때 YouTube PiP(작은 영상 창)가 표시됨. 당시 자동 제스처 요청 0회. 자동 넘김 경로는 영상 내부 75%→25%의 아래→위 방향이며 상단 모서리에서 시작하지 않음.

## D-008 · 초 단위 지연 갱신을 수동 탐색으로 오인 · 0.1.4 실기기·독립 재시험 PASS

- 증상: 0.1.2 시간 갱신 수정 후에도 정상 재생 도중 1/2→0/2 초기화.
- 재현: 같은 쇼츠에서 반복 관측. 0.1.3 진단 전용 빌드로 `jump from=14.0 to=16.0 elapsedMs=309 generation=3` 확인. 외부 화면 초기화가 아니라 반복기 내부 점프 판정임을 분리.
- 증거: 비공개 증거 자료, 19번 표본 이후. 독립 리뷰에서도 14초 중복 표본 후 16초로 바뀌는 입력으로 초기화 재현.
- 직접 원인: 매 300ms 폴링의 시간 차이에 1초 오차만 허용하여 YouTube 시간 설명의 2초 단위 지연 갱신을 비정상 전진으로 취급.
- 영향: 정상 완주를 누적하지 못하여 자동 넘김 불가. 조기 넘김은 관측되지 않았음.
- 잘못된 기존 접근: 접근성 시간 설명이 재생 시계와 매 표본 정밀하게 동기화된다고 가정.
- 수정: 최근 3초 표본 전체를 비교하는 ProgressMotion 모듈에서 2.1배 재생속도와 2초 갱신 오차를 허용. 2초씩 빠르게 반복 점프하면 누적 검사에서 차단. 후진/화면 공백/최소 관측량/최소 완주시간/전환 보호 유지.
- 자동 재발방지: 309ms에 2초 갱신, 반복 빠른 점프 거부, 큰 탐색 거부, 긴 일시정지 후 탐색 거부, 2배속, 상태 초기화, 52초 두 번 양자화 재생 등 7개 추가. 기존 3초 영상 600ms 오판 방지 포함 총61 PASS.
- 한계: 작은 수동 탐색과 정상 시간 갱신 지연은 접근성 시간만으로 완전 구분할 수 없음. 모든 탐색을 감지한다고 보장하지 않음.
- 재시험: 0.1.4 빌드/lint/직접 JUnit61 PASS. 독립 리뷰와2배속 지연갱신 추가 재현 PASS. 실기기 목표2의52초 두 번 완주→다음, 목표1의21/45/30초3개 연속 전환에서 불필요한 초기화 및 중복 제스처 없음.

## D-009 · 빠른 설정 뒤의 YouTube 시간을 읽음 · 0.1.5 보호·재시험 PASS

- 증상/재현: YouTube 전체화면에서 자동 실행 후 빠른 설정을 펼침. 0.1.4는 여전히 뒤의38→40초 및 반복 시작을 읽음.
- 직접 원인: getRootInActiveWindow 결과만으로 실제 입력 초점이 있는 전면 창임을 보장하지 못함. 기존 패키지/쇼츠 트리 검사만으로 시스템 창 가림을 판별하지 않음.
- 확인 증거: 비공개 증거 자료와 서비스 숫자 진단. 자동 넘김은 실행하기 전 X로 정지, 요청4/확인4 유지.
- 영향: 충분히 기다리면 시스템 빠른 설정창에 제스처가 전달될 위험. 관측된 것은 뒤 화면의 추적 지속이며 실제 설정 오조작은 발생하지 않음.
- 잘못된 기존 접근: active-root 조회 결과를 현재 입력 대상 창과 동일시.
- 수정: YouTubeWindowGuard에서 창 메타데이터의 입력 초점/ID/앱 창 유형/PiP를 검사. 입력초점이 일치하는 일반 앱 창만 허용. 정보 없거나 빠른 설정/작은 화면은 대기. 제스처 직전 재조회에도 동일 검사.
- 한계: 입력초점을 가져가지 않는 다른 앱 플로팅·팝업 전체 가림까지 검출하는 것은 아님. 독립 리뷰 지적에 따라 설명서에 제한 명시.
- 접근 범위: 이미 허용된 접근성 권한 안에서 interactive-window 조회 활성화. 다른 앱·SystemUI의 제목/내용/노드 트리는 읽지 않고 창ID·초점·유형·PiP만 판정. 새 manifest uses-permission 없음.
- 근거: [Android getWindows](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#getWindows()), [AccessibilityWindowInfo](https://developer.android.com/reference/android/view/accessibility/AccessibilityWindowInfo).
- 재발방지: 일반 재생→빠른 설정 펼치기→즉시대기→접기 후 정상 감지 복구→실제 자동 넘김을 기기 회귀 절차에 추가. JVM 반복61개와 독립 변경 리뷰 병행. OS창 메타데이터 자동 모의시험은 미구현.
- 재시험: 0.1.5 빌드/lint/직접 JUnit61 및 독립 코드 리뷰 PASS. 빠른 설정6개 표본에서즉시대기/요청0 유지, 다른앱+PiP 대기, 복귀후42초 영상1/2→2/2→다음36초 영상1/2 및 요청1/확인1. 최신 APK와 설치APK 해시 일치.

## D-010 · Instagram 진단 도구의 idle/runner 지연 (제품 버그와 별개)

- 증상/재현: 재생 중 dump는 could not get idle state. 구형runner는 Android17에서 android.test.RepetitiveTest 누락. 매번 findAccessibilityNodeInfo 호출은 약10초 간격.
- 원인: 계속 변하는UI의 idle조건, 구형실행기의 framework classpath 누락, 탐색 내부 idle 대기. Instagram 진행값 미제공을 의미하지 않음.
- 증거: 비공개 증거 자료(누락예외),sample-1-progress.txt(지연),sample-2-cached-progress.txt(약400ms진행).
- 영향: 지원 가능성 조사 속도/정확성. 설치앱의 동작 경로는 미변경.
- 잘못된 접근: 일반XML덤프에 RangeInfo가 포함되거나 반복 탐색이 무간섭 고속이라고 가정.
- 대응: 일시정지로 UI구조 관측. 기기에 있는 android.test.runner/base/uiautomator jar를 classpath에 명시해 app_process 실행. 전용 scrubber 노드 획득 후 refresh로 숫자만 읽고 실패 시 재조회.
- 재발방지: 읽기전용·노드 해제·조회 설정 복원. 분석은 fresh=true 및 visible=true만 채택. 제품 E2E와 UIAutomator 동시 실행 금지. assertion 없는 OK를 기능 PASS로 오인하지 않도록 문서화.
- 재시험: 첫 영상136개 정상 표본/경계3회, 다음 영상의 다른 길이/반복 경계 확인. 제품 서비스의 조회 지연·동일영상·창 보호는 통합 후 시험 필요.

## D-011 · 패키지 변경 검사에서 null root를 앱 전환으로 오인 · 0.2.0 수정

- 증상/재현: 자동 제스처 pending 중 root가 잠시 null이면 패키지를 빈 문자열로 해석하여 invalidate/gate.cancel에 진입할 수 있었다. 코드/조건 재현이며 실제 오스와이프는 관측하지 않음.
- 직접 원인: 앱 정보 없음과 알려진 다른 앱으로의 이동을 구분하지 않음.
- 확인 증거: 코드·독립 리뷰 양쪽에서 확인. SessionPolicyTest.missingRootRetainsPendingConfirmation로 null/빈문자열과 pending→WAITING→FAILED 경로 재현.
- 영향: D-002 조회공백 보호의 회귀 위험. 미확인 제스처 재시도 차단이 풀릴 수 있음.
- 잘못된 접근: 값이 다르다는 이유만으로 패키지 전환이라고 판단.
- 수정: SessionPolicy.packageChanged는 비어 있지 않은 알려진 패키지 변경만 true. 서비스에서 사용.
- 자동 재발방지: null/empty·동일앱·다른앱·실제gate pending 유지 2개 테스트 추가.
- 재시험:119개JVM PASS, 재빌드/lint PASS, 독립 코드재리뷰 지적 해소. 서비스실기기전환 회귀는 잠금해제후 수행 필요.

## D-012 · 화면 상태 복원이 실행 스위치를 다시 켤 위험 · 0.2.0 수정

- 증상/재현: 이전 Activity에 ON이 저장된 뒤 서비스 재연결/타일/X로 OFF가 되고 화면이 복원되면 CompoundButton.setChecked가 listener를 호출해 store.start로 이어질 수 있음. 정적 경로 재현, 실기기 발생 관측 아님.
- 원인: SharedPreferences와 Android 위젯 상태 복원이 둘 다 설정의 원본처럼 작동함.
- 증거: 독립 리뷰와 MainActivity 실행리스너/Android CompoundButton 복원 동작 대조.
- 영향: 사용자 새 조작 없이 실행이 재개될 조건부 위험.
- 잘못된 접근: render()에서만 콜백을 차단하고 super.onRestoreInstanceState의콜백을 고려하지 않음.
- 수정: onRestoreInstanceState 전체를 rendering=true로 감싸 부수효과를 막고, 이후 저장설정으로 다시 render. 숫자 입력 draft는 그대로 복원.
- 자동 재발방지: 코드 복원경로에 명시 guard, 독립 재리뷰. Android실제상태복원 자동화는 미구현이며 JVM119개를 해당경로 동적PASS로 대체하지 않음.
- 재시험: 최종빌드/lint/119개시험 PASS, 독립 정적재리뷰 미해결P1/P2 0. 폰복원/재연결실험은잠금해제후필요.

## D-013 · 숫자 키 필터/길이 제한에 의한 붙여넣기 변형 위험 · 0.2.0 수정

- 증상/재현: 숫자 전용 KeyListener는 -1/1.5의 기호를 제거할 수 있고 LengthFilter(4)는 000012를0000으로 절단한다. 결과가 원래 의도와 다른 유효값으로 저장될 위험. 코드/API 경로 확인이며 실제 기기 붙여넣기 발생을 관측한 것은 아님.
- 직접 원인: 원문 유효성 검사 이전의 손실성 입력 필터와 길이 제한.
- 확인 증거: CountEditor 구성, 기존 parseCount 테스트, [TextView setRawInputType](https://developer.android.com/reference/android/widget/TextView#setRawInputType(int)), [AOSP DigitsKeyListener](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/java/android/text/method/DigitsKeyListener.java).
- 영향: 타이핑/붙여넣기로 반복 횟수 설정. 화살표·완료 모두 변형된 draft를 읽게 됨.
- 잘못된 접근: 숫자 키보드 지정과 숫자만 남기는 필터가 같은 의미라고 처리.
- 수정: TextKeyListener를 명시하고 setRawInputType(NUMBER)로 키보드만 요청. LengthFilter 제거. 전체 입력을 기존 parseCount로 검사하며 무효값은 저장하지 않음.
- 자동 재발방지: 000012→12,0000100→거부 테스트 추가. verify.ps1의 COUNT_INPUT_CONFIG_AUDIT가 손실성 필터 재도입을 차단. 이는 정적 구성 검사이며 실제 IME 대체 아님.
- 재시험: 빌드/lint·JUnit120·구성검사 PASS, 독립120 재실행/변경재리뷰 PASS. 실제 키보드/클립보드 시험은 잠금 해제 후 필요.
