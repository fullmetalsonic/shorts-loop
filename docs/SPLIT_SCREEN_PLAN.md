# 분할 화면 설계·시험 범위 / Split-screen design and verification · 0.3.0

상태: **현재 Public 공개판은0.3.0/code32다.** 듀얼 모드는0.2.9 배포에 포함되지 않는다. 전체화면·분할 복귀,회전·상하 두 배치와 일반 앱 병행의 확인 범위를 아래처럼 후보별로 구분하며,최종 판정과 정확한 수치·해시는 [검증 원장](VERIFICATION.md)을 따른다. 공개 파일·CI·익명 다운로드 동일성 확인을 완료했으며,모든 앱·배치·전체 UI 검증 완료를 주장하지 않는다.

Status: **The current public version is0.3.0/code32.** Dual mode is not part of0.2.9. Fullscreen/split returns,rotation,top/bottom layouts and ordinary-app coexistence have artifact-specific evidence below;the [verification record](VERIFICATION.md) owns exact counts,hashes and final outcomes. Public assets,CI and anonymous-download parity are verified;all-app,all-layout or full-UI completion is not claimed.

최종 공개 APK9AA1E884…는 아래53FD 후보와 내장 소스 revision 기록만 다르고 나머지 ZIP 항목은 동일하다. 최종 공개 APK의3개 OS 검사·실폰 덮어 설치/해시·설정 보존·전체OFF도 확인했다. / The final published9AA1E884… APK differs from53FD only in its embedded source-revision record;other ZIP entries match. Final-APK three-OS,physical upgrade/hash/preferences and masterOFF checks also passed.

### 공개판과 검증 후보의 확인 범위 / Release and tested-candidate evidence

- 실기기 흐름 검증 후보 `53FDA565…`: 전체화면 전환 때 TYPE3 시스템 손잡이를 잘못 차단하던 제품 창 보호 규칙을 수정했다. BUILD·JUnit564·3개 OS 검사·설정 보존 업그레이드·설치 해시 확인을 통과했다. 실행·듀얼ON을 유지하며 Instagram 전체화면3회(일반1·광고1·긴 영상1)→Instagram 왼쪽·YouTube 오른쪽 분할에서 추가YouTube4회(일반3·긴 영상1)와Instagram1회(일반)→Instagram 전체화면 복귀 후 추가9회(일반4·긴 영상4·광고1)를 확인했다. 마지막 구간에서 숨겨진 YouTube의 요청·확인은4에 유지됐다. 이 구간을 앱별10연속 또는 모든 배치 성공으로 보고하지 않는다. 이전F582/DE7과 동일한 APK 또는 실행 코드가 아니다.
- 동작 계약: 듀얼ON·전체 실행ON을 유지하면 전체화면 한 대상·분할 두 대상·일반 앱 옆 한 대상을 보이는 창에 따라 감지한다. 배치마다 모드를 다시 켜도록 설계한 것이 아니다. 중지·서비스 재연결·권한·창/재생 안전 조건은 별도로 적용한다.
- 한계·남은 시험: 키보드·팝업 보호와 앱 자체의 일시정지를 유지하며 강제 재생하지 않는다. 하나의 계산기 사례를 모든 일반 앱에 확대하지 않는다. 듀얼 사진 릴스의 새 실물 표본과 회전 후 원래 배치로의 복귀 시험은 아직 남아 있다. 최종 물리 시험 결과는 [검증 원장](VERIFICATION.md)에서 갱신한다.

EN: Physical-flow verification candidate53FDA565… changes the product guard for a TYPE3 system handle after fullscreen entry;it is not the same APK or executable payload asF582/DE7. Build,564 unit tests,three-OS,settings-preserving upgrade and installation-hash checks passed. With execution/dualON throughout,the confirmed sequence was3 fullscreen Instagram advances,then4 YouTube and1 Instagram advances in split view,then9 more Instagram advances after returning to fullscreen. Hidden YouTube remained at4 requests/confirmations. This is not a ten-consecutive-per-host or all-layout claim. The mode follows visible hosts without re-enabling dual mode for each layout;stop,reconnection,permission,keyboard/popup and playback guards still apply. Fresh dual-photo samples and return rotation remain outstanding;one Calculator case does not prove every app.

### 이전DE7·F582 후보 기록 / PreviousDE7 andF582 evidence

- `DE7F2803…`: Instagram 위·YouTube 아래와,전체OFF→교환→ON 후 반대 순서에서 실제 자동 전환을 확인했다. 계산기 위·YouTube 아래에서 계산기를 조작하는 동안 YouTube 추가 전환을 확인했고,숨겨진 Instagram의 요청은 없었다. 나뉜 두 배치 구간을 무중단 연속시험으로 합산하지 않는다.
- `F582EA28…`: 당시 듀얼 모드 안내문구만 개선하여 두 DEX를 포함한 APK ZIP 항목은DE7과 같고 `resources.arsc`만 달랐다. 빌드·단위·3개 OS 검사·설치 해시 확인 후 계산기/Instagram 구간6/6(일반3·긴 영상2·시간제1)과 숨겨진 YouTube 요청0을 확인했다. 이후 분할→전체화면 전환에서 TYPE3 손잡이 오인이 발견되어 새 제품 코드 보완으로 이어졌다. 이 동일 코드 설명은F582와DE7 사이에만 적용된다.

EN: EarlierDE7 confirmed both top/bottom host orders separated byOFF/swap/ON,and visible YouTube operation while Calculator was used,with no hidden-Instagram requests. The wording-onlyF582 candidate matchedDE7's two DEX files and other ZIP entries except resources.arsc. After build/unit/three-OS/install checks,its Calculator/Instagram segment completed6/6(3 ordinary,2 long,1 timer) with no hidden-YouTube request. A later fullscreen-return failure required the new product guard. Payload equivalence applies only toDE7 versusF582,not the current candidate.

### 이전517827 후보의 좌우 중간 기록 / Previous517827 left/right checkpoint

아래는 이후 좁은 창·회전 보완 전의 중간 기록으로 보존한다. 현재 후보의 최종 실폰 결과가 아니며,후속 실패와 수정은 [D-043](DEBUG_LOG.md)을 따른다.

The following checkpoint predates the narrow-pane and rotation fixes. It is retained as history,not the latest candidate's final physical result;see D-043 for subsequent failures and fixes.

- 첫 구간: YouTube5/5(일반4·긴 영상1),Instagram5/5(일반3·긴 영상2). 수동 이동0,종료 시 대기 요청0·차단0·복구0.
- 배치 교환 구간: 전체 실행OFF 상태에서 시스템 기능으로 좌우를 바꾸고ON했다. YouTube 왼쪽·Instagram 오른쪽 감지와 플로팅 위치 반영,Instagram 추가3/3(긴 영상2·광고1)을 확인했다. 교환 전후 횟수를 합쳐 앱별10연속으로 보고하지 않는다.
- 설치 APK 해시 일치와 BUILD·JUnit557·STATIC PASS를 확인했다. 정확한 산출물·후속 시험은 [검증 기록](VERIFICATION.md)과 [디버그 대장 D-043](DEBUG_LOG.md)을 따른다.

EN: The first segment comprised YouTube4 ordinary+1 long and Instagram3 ordinary+2 long transitions,with no manual moves and no pending request,block or recovery at its end. The swap was performed through the system while execution wasOFF,then execution was re-enabled;Instagram added2 long+1 ad transitions. Do not sum separate segments into a ten-consecutive-per-host claim. Installed APK identity,BUILD,557 unit tests and static checks passed;artifact details and further verification remain in the linked records.

## 목표 / Requirements

- YouTube Shorts와 Instagram Reels를 분할 화면에서 동시에 열었을 때 각 앱의 감지·카운트·시간제·전환 확인을 독립 관리한다.
- 좌우/상하 배치와 앱 순서에 의존하지 않는다. 위치 교환·비율 변경·회전 시 새 창 경계를 확인하고 잘못된 영역에 입력하지 않는다.
- 앱별 반투명72×56dp 플로팅2개를 사용한다. 각 플로팅에는 앱 구분과 해당 카운트를 표시하며 해당 창 안의 안전한 영역에 배치한다. 창이나 공간이 없으면 해당 플로팅을 숨긴다.
- ‘듀얼 화면 적용’은 공통 토글이며 기본OFF다. OFF는 활성 대상 창 하나만 처리하고ON은 함께 보이는 선택 대상 각각을 감지한다. 토글 변경은 전체 실행을 켜거나 앱별 설정을 덮어쓰지 않는다.
- 앱 이름을 기준으로 조작 대상을 표시한다. 탭·닫기·일시정지 범위와 전체 실행OFF를 구분한다. 전체OFF는 양쪽 모두 중지한다.
- 재생이 정지된 앱의 횟수를 시간만으로 늘리지 않는다. 댓글·메뉴·잠금·가려짐·미확인 전환 보호는 창별로 유지한다.

EN: Maintain independent detection,counters,timers and transition confirmation for two visible selected hosts. Dual mode defaultsOFF(active target window only);ON detects both separately. Use two labelled translucent72×56dp controls,confined to each valid host window. Mode changes preserve settings and never enable execution. Revalidate geometry after swaps,resizing and rotation;retain per-window guards and do not infer plays during a host pause.

## 0.2.9 당시 구조와 후속 관측 / Historical architecture and later observation

2026-08-28 기기 창 메타데이터와 화면에서 YouTube와 Instagram이 별도 multi-window 창으로 동시에 보이는 것을 확인했다. 앱 조작·위치 교환은 수행하지 않았다. 실제 콘텐츠·계정·기기 식별자는 공개 문서에 포함하지 않는다.

0.2.9 당시 `ShortsAccessibilityService`에는 하나의 `activePackage`, `LoopCounter`, `AdvanceGate`, 사진/시간제 추적기가 있었고, `YouTubeWindowGuard`는 초점 창만 허용했다. 당시 플로팅도 단일 런타임 상태를 표시했다.0.3.0은 아래처럼 두 세션과 공통 입력 조정으로 분리하며,코드 구조 변경 자체를 실제 두 앱 자동 넘김의 증거로 보지 않는다.

EN: Initial read-only inspection showed two visible multi-window hosts without rearrangement. The0.2.9 service used one active host,counter,gate and photo/timer state with a focus-only guard. Version0.3.0 separates two sessions and shared input coordination;neither that refactor nor later non-focused clock progress proves physical dual-auto-advance. Raw device content remains private.

## 변경 영역과 예방 / Change areas and safeguards

1. 창별 세션: package/window 식별과 현재 창/페이지 근거를 묶고 전환·실패·타이머를 분리한다. 창ID가 새로 생성되면 이전 상태를 무조건 연결하지 않는다.
2. 창별 감지: 초점 없는 다른 창의 실제 노드·재생 정보가 제공되는지 먼저 검증한다. 정보 부재를 재생 완료로 해석하지 않는다.
3. 동작 조정: 양쪽의 넘김 요청이 겹쳐도 입력이 충돌하지 않도록 직렬 처리하고, 실행 직전에 해당 창의 경계·대상을 다시 확인한다. 대기 중 위치가 바뀐 요청은 재검증 없이 보내지 않는다.
4. 플로팅: 창 안의 안전한 영역, 드래그 위치 보존, 창 밖 이탈 방지, 앱 구분, 각 카운트 및 터치 범위를 검토한다. 하나의 실패가 다른 앱의 카운트에 섞이지 않게 한다.
5. 설정/복원: 기존 설정을 두 앱의 초기값으로 한 번만 복사하고 원래 키를 보존한다. 반복 횟수·긴 영상·탭 방식·위치는 앱별 저장한다. 상단 앱 탭으로 설정 대상을 표시하며 선택하지 않은 탭의 입력 초안도 보존한다. X는 표시된 해당 앱만 일시정지하고 인앱 해당 앱 재개로 복구한다. 하단 전체OFF는 양쪽 모두 중지한다. 전체ON은 선택된 앱을 재개한다.
6. 화면 분석: 두 대상 창이 함께 보이면 실험적 화면 분석을 비활성화하고 저장한 선택은 보존한다. API34 이상·별도 동의 제한을 유지하며,기존 단일 창 분석을 검증되지 않은 동시 분석으로 확대하지 않는다.

EN: Separate sessions,verify non-focused data,serialize/revalidate actions and scope failures per host. Repeat,threshold,tap mode and position are per-app,initialized once from preserved legacy keys. Retained editor tabs keep drafts separate. X pauses its host;host resume does not enable an off master. MasterOFF stops both and masterON resumes selected hosts. Experimental analysis is inactive while both targets are visible,retaining its saved choice and API34+/consent limits.

## 시험 순서 / Test matrix

- 두 배치 순서 × 좌우/상하, 서로 다른 분할 비율, 회전, 전체 화면 왕복.
- 양쪽 일반 영상, 한쪽 광고/라이브/긴 영상/사진, 양쪽 동시 넘김 요청.
- 한쪽 댓글/일시정지/수동 이동/가려짐, 한쪽 오류와 다른쪽 계속 동작.
- 플로팅2개, 드래그·탭·개별중지·개별재개·전체OFF→ON,듀얼OFF/ON 전환의 설정·초안 보존,접힘/펼침과 재실행 복원.
- 기존 단일 화면 YouTube/Instagram 회귀. 실제 앱이 동시 재생하지 않는 조건도 별도로 기록한다.

EN: Test both host orders and split orientations,ratios,rotation and fullscreen transitions;normal and special content;simultaneous requests;one-pane interruption/failure;floating controls and restore;single-window regression. Record host-imposed playback pauses separately.

## 초점과 입력 설계 / Focus and input

- 두 창의 접근성 정보를 읽는 것과 입력 초점은 별개다. 창 목록에서 각 대상 루트를 조회하고 약300ms 주기로 각 앱을 확인한다. 정보가 없다고 재생·완료를 추정하지 않는다.
- Android 입력 제스처는 동시 호출하면 기존 제스처를 취소할 수 있으므로 하나씩 직렬 처리한다. 대기에는 좌표를 저장하지 않고 콘텐츠/창 근거를 저장하여 실행 직전에 다시 검사한다. 만료되거나 위치·콘텐츠가 바뀌면 폐기한다.
- 초점 없는 창이 정보를 제공하지 않으면 제한적인 초점 전환을 별도 시험한다. 무조건적인 화면 탭/재생 강제/댓글·광고 클릭은 허용하지 않는다. 초점 전환으로 재생 정지·재시작이 발생하면 자동 적용하지 않고 한계를 표시한다.
- 앱이 자체적으로 비활성 창 재생을 멈추는 조건은 앱의 실제 동작 한계다. 동시에 보인다는 사실을 동시에 재생됨으로 보고하지 않는다.
- 구조: AccessibilityService(전체 권한/창/입력 조정) → HostPlaybackSession×2(각각 기존 정책/전환/진단/플로팅). 기존 추적기를 공유하거나 비동기 콜백 중 전역 상태를 다른 앱으로 교체하지 않는다.

EN: Read both window roots without changing input focus first. Serialize gestures,retain only bounded pending intent,and revalidate identity/window/geometry before dispatch. A focus-switch fallback must be verified separately and must not blindly click content or force playback. Host-imposed pauses are reported honestly. Two independent sessions retain the existing fail-closed policies;the accessibility service coordinates shared window and input resources.

Platform references: [AccessibilityService/window and gesture APIs](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService),[multi-window lifecycle](https://developer.android.com/develop/ui/views/layout/support-multi-window-mode),[accessibility focus](https://developer.android.com/guide/topics/ui/accessibility/service),[audio focus](https://developer.android.com/media/optimize/audio-focus).

미확보 사진 예외(번호 없음·사진 직후 광고·혼합)도 후속 검증 목록으로 유지한다. [현재 검증](VERIFICATION.md),[사진 계약](PHOTO_REELS.md),[인수인계](../HANDOVER.md).

EN: The per-app settings,two-control layout,per-hostX and default-OFF dual toggle are decided. Earlier discussion of a single two-section control was a design alternative,not a second implemented mode. Carry forward unobserved photo exceptions:unreadable index,photo-to-ad and mixed media. The limited physical results above do not complete the entire test matrix;keep artifact-specific transition evidence separate from unrun scenarios.
