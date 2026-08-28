# 분할 화면 후속 계획 / Split-screen follow-up

상태: 요구사항과 읽기 전용 구조 확인 단계. **0.2.9 배포에는 포함하지 않는다.** 구현·동시 동작·위치 교환 시험은 아직 수행하지 않았다.

Status: requirements and read-only inspection only. **Not included in0.2.9.** Implementation,concurrent operation and position-swap tests have not run.

## 목표 / Requirements

- YouTube Shorts와 Instagram Reels를 분할 화면에서 동시에 열었을 때 각 앱의 감지·카운트·시간제·전환 확인을 독립 관리한다.
- 좌우/상하 배치와 앱 순서에 의존하지 않는다. 위치 교환·비율 변경·회전 시 새 창 경계를 확인하고 잘못된 영역에 입력하지 않는다.
- 앱별 플로팅2개를 우선 검토한다. 각 플로팅에는 앱 구분과 해당 카운트를 표시한다. 안정적인2개 구현이 어려우면 하나의 플로팅을 두 영역으로 나눈다.
- 앱 이름을 기준으로 조작 대상을 표시한다. 탭·닫기·일시정지 범위와 전체 실행OFF를 구분한다. 전체OFF는 양쪽 모두 중지한다.
- 재생이 정지된 앱의 횟수를 시간만으로 늘리지 않는다. 댓글·메뉴·잠금·가려짐·미확인 전환 보호는 창별로 유지한다.

EN: Maintain independent detection,counters,timers and transition confirmation for the two visible hosts,regardless of left/right or top/bottom order. Revalidate geometry after swaps,resizing and rotation. Prefer one clearly host-labelled floating control per app;fallback to a two-section shared control if needed. Distinguish per-host actions from globalOFF. Do not infer plays while a host is paused;retain per-window safety guards.

## 확인한 구조 / Observed architecture

2026-08-28 기기 창 메타데이터와 화면에서 YouTube와 Instagram이 별도 multi-window 창으로 동시에 보이는 것을 확인했다. 앱 조작·위치 교환은 수행하지 않았다. 실제 콘텐츠·계정·기기 식별자는 공개 문서에 포함하지 않는다.

현재 `ShortsAccessibilityService`에는 하나의 `activePackage`, `LoopCounter`, `AdvanceGate`, 사진/시간제 추적기가 있고, `YouTubeWindowGuard`는 초점 창만 허용한다. 따라서 한 창 인식을 두 창 동시 지원으로 표현하지 않는다. 현재 플로팅도 단일 런타임 상태를 표시한다.

EN: Read-only device inspection showed two distinct visible multi-window hosts;no app rearrangement was performed. The current service has one active host,counter,advance gate and photo/timer state;the window guard accepts only the focused window. A single working pane does not prove dual-host support. Raw device content remains private.

## 변경 영역과 예방 / Change areas and safeguards

1. 창별 세션: package/window 식별과 현재 창/페이지 근거를 묶고 전환·실패·타이머를 분리한다. 창ID가 새로 생성되면 이전 상태를 무조건 연결하지 않는다.
2. 창별 감지: 초점 없는 다른 창의 실제 노드·재생 정보가 제공되는지 먼저 검증한다. 정보 부재를 재생 완료로 해석하지 않는다.
3. 동작 조정: 양쪽의 넘김 요청이 겹쳐도 입력이 충돌하지 않도록 직렬 처리하고, 실행 직전에 해당 창의 경계·대상을 다시 확인한다. 대기 중 위치가 바뀐 요청은 재검증 없이 보내지 않는다.
4. 플로팅: 창 안의 안전한 영역, 드래그 위치 보존, 창 밖 이탈 방지, 앱 구분, 각 카운트 및 터치 범위를 검토한다. 하나의 실패가 다른 앱의 카운트에 섞이지 않게 한다.
5. 설정/복원: 기존 설정을 보존한다. 반복 횟수까지 앱별로 따로 저장할지, 공통 설정에 독립 카운트만 적용할지는 UI 설계 전에 확정한다. X가 해당 앱을 일시정지할지 전체를 끌지도 표시와 함께 확정한다.
6. 화면 분석: 현재 창 기반 화면 분석 보조를 두 창에 그대로 적용하지 않는다. 캡처 대상·성능·지원OS를 별도 확인하고 검증되지 않은 동시 분석은 활성화하지 않는다.

EN: Separate window sessions;verify non-focused node/progress availability;serialize and revalidate actions;scope floating controls and failures per host. Preserve existing settings. Confirm whether repeat settings are per-app or shared,and the per-hostX behavior,before UI implementation. Experimental visual assistance needs its own capture/performance/OS review.

## 시험 순서 / Test matrix

- 두 배치 순서 × 좌우/상하, 서로 다른 분할 비율, 회전, 전체 화면 왕복.
- 양쪽 일반 영상, 한쪽 광고/라이브/긴 영상/사진, 양쪽 동시 넘김 요청.
- 한쪽 댓글/일시정지/수동 이동/가려짐, 한쪽 오류와 다른쪽 계속 동작.
- 플로팅2개 또는 분할1개, 드래그·탭·개별중지·전체OFF, 접힘/펼침과 재실행 복원.
- 기존 단일 화면 YouTube/Instagram 회귀. 실제 앱이 동시 재생하지 않는 조건도 별도로 기록한다.

EN: Test both host orders and split orientations,ratios,rotation and fullscreen transitions;normal and special content;simultaneous requests;one-pane interruption/failure;floating controls and restore;single-window regression. Record host-imposed playback pauses separately.

상세 영향 범위와 UX를 확정한 뒤 구현한다. 미확보 사진 예외(번호 없음·사진 직후 광고·혼합)도 후속 검증 목록으로 유지한다. [현재 검증](VERIFICATION.md),[사진 계약](PHOTO_REELS.md),[인수인계](../HANDOVER.md).

EN: Finalize scope and UX before implementation. Carry forward unobserved photo exceptions:unreadable index,photo-to-ad and mixed media.
