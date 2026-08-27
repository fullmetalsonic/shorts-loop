# 디버그·재발방지 대장

## D-021 · 18초 영상16→0 반복을 탐색으로 오인 · 미해결/디버깅 보류

- 증상: 재생은 반복되는데 현재 회차/자동 넘김 요청이 늘지 않음.
- 재현: 전체 화면18초 YouTube 영상, 약300ms 관측에서 마지막 표시16초 후0초로 복귀. 16:10:36.948,16:10:55.578,16:11:13.786에 반복 관측, 간격18.630/18.208초.
- 직접 원인: LoopCounter의18초영상 끝 경계는17초 이상인데 이전값16이므로 wrap 대신 음수 시간점프 경로에서 집계를 초기화. 왜 앱 표시가16에서 끝나는지(표시 갱신/길이 오차)는 별도 미확정.
- 증거: private/device-tests/v021f-fullscreen-resume.jsonl65표본. 동일 세대1582/blocked=false/요청16·확인15 유지, jump elapsed307~310ms. 독립 검토자의 기존 컴파일 클래스 재현에서16종료는2회 반복해도현재1/advance0,17종료는정상 누적 확인.
- 영향: 해당 경계 조건의 일반 영상 집계. 전체 영상·Instagram에 동일하게 재현된다고 단정하지 않음.
- 잘못된 접근: 모든 정지를 배터리나 반복 누적 초과로 설명하거나, 끝 경계 허용폭을 무조건 크게 늘려 수동 탐색까지 완주 처리하는 것.
- 수정: 없음. 사용자 요청으로 추가 디버깅/자동수정 보류.
- 자동 재발방지 계획:18초/16→0/N1·N2, 수동 되감기, 조회공백, 짧은3초영상600ms 조건을 함께 시험할 것. 아직 새 자동시험 추가 없음.
- 재시험: 기존 클래스 독립 재현만 수행. 수정 후 실기기 재시험은 미실행.

## D-020 후속 · 자체 앱 제한 없음 적용, 제한적 개선 확인

- 사용자 승인 후 자체 앱 배터리 '제한 없음' 적용, 예외 목록/커널 frozen=0 확인. 다른 앱/전체 절전 정책/권한 변경 없음.
- 동일 프로세스에서 실행 재시작 없이 응답 복구. 약56초 PiP+홈 대기50표본 응답 유지와 전체 화면 시간 감지 재개 확인.
-0.2.2에 배터리 상태와 수동 안내 메뉴 추가. 자체 앱만 열고 상태조회만 하도록 정적 회귀 검사/독립 리뷰 PASS. 앱이 OS 정책을 자동 변경하지 않음.
- D-019 재발:16:12 요청18/확인16/blocked=true인데 kernel frozen=0. 동결 해소가 전환실패 해소와 같지 않음. 최초 실패 트리거는 아직 미확정.
- 장시간/재부팅/새 메뉴 실기기 검증은 미실행. 이하 '조치 미실행'은 이전 진단 당시 기록.

## D-020 · 전체 화면 복귀 후에도 서비스 무응답 · Samsung Freecess 동결 확인, 조치 미실행

- 증상/재현: 사용자 PiP 전환 후15:56부터 서비스 진단 timeout. YouTube 전체 화면 복귀를16:01 실제 캡처와 topResumedActivity로 확인했으나 진단 응답은 회복되지 않음. 실행 토글/앱 설정은 건드리지 않음.
- 직접 원인/증거: 동일 앱 PID의 cgroup.freeze=1, cgroup.events populated=1/frozen=1을 읽어 실제 프로세스 동결 완료 확인. Samsung `dumpsys activity freecess`의 mFreezedPackages에도 자체 앱이 있음. 이전 ProcessRecord.isFrozen=false는 커널 실제 동결을 반증하지 않으며, 단독으로 동결 배제에 사용하면 안 됨.
- 시간 증거: 자체 앱 MARs 기록은15:56:30.172 FRZ(Bg),16:01:08.233 UFZ(Binder accessibility),16:01:14.240 FRZ(Bg). 사용자 전체 화면 복귀 시 잠깐 깨더라도 약6초 후 다시 동결된 기록임.
- 추가 진단: 자체 앱 SIGQUIT을16:03:59.739 요청. 동일 시각 UFZ(Signal), ART Signal Catcher/밀린 dump 출력 확인 후16:04:05.749 FRZ(Bg). 이 구간은 진단 신호로 깨운 것으로 자연 회복 PASS가 아님. EPIPE는 이미 제한시간이 끝난 dump 출력에 대한 후속 오류와 부합하며 최초 정지 원인으로 취급하지 않음.
- 진단 제한: JDWP 연결 실패, 임시 포워딩 제거. debuggerd는 root 필요 응답으로 중단, root/보안 우회 없음. SIGQUIT은 진단 신호이며 앱 종료/데이터 삭제가 아님. 배터리·절전·권한·설치·앱 소스는 미변경.
- 영향: 앱 프로세스 전체가 동결되어 YouTube/Instagram 공통 서비스 폴링과 상태 응답이 실행될 수 없음. 단순 재생 횟수 누적 초과의 증거는 없음. D-019의15:46 최초 전환확인 실패까지 동일 원인으로 확대하지 않음.
- 잘못된 기존 접근: 살아있는PID/접근성등록/ActivityManager isFrozen=false만으로 실행 중이라고 판단. 피해야 할 대응은 카운터만 초기화하거나 확인되지 않은 반복 스와이프를 추가하는 것.
- 대응 계획: 사용자 승인 후 자체 앱만 백그라운드 절전 예외를 적용하는 비교 시험. 이 설정으로 Freecess 문제가 해결되는지는 미검증이며 기기별 안내·서비스 실행 유지 보완은 별도 구현 범위로 검토. 시스템 전체 절전 기능 비활성화는 하지 않음.
- 자동 재발방지/재시험: 새로운 제품 수정/시험은 없음. 실기기 회귀 항목에 PiP·다른 앱 전면·전체 화면 복귀·수분 경과 후 서비스 응답/커널 동결/새 자동 요청 비교 추가. 현재 진단은 원인 확인, 수정 및 안정성 검증은 미완료.
- 독립 리뷰: 제공된 기기 증거와 공식 동결 의미를 대조하여 현재 무응답 해석 타당. 최초 실패·예외설정 효과는 미확정 유지. 검토자는 추가 기기 조작하지 않음.
- 근거: [Linux cgroup.freeze/events](https://docs.kernel.org/admin-guide/cgroup-v2.html), [Samsung 앱 관리와 예외 설정](https://developer.samsung.com/mobile/app-management.html). 후자는 절전 예외 안내이며 본 기기의 Freecess 해제를 보장하는 자료가 아님.

## D-019 · YouTube 넘김 확인 실패 후 정지 유지 · 0.2.1 원인 일부 확인, 미해결

### 추가 작은 창 시험 · 15:56 이후

- 사용자가 작은 창으로 변경. 최초 캡처는 홈 화면+YouTube 영상 창이며 Instagram 메인 화면이 아님. OS task의 mode=pinned, PictureInPictureMode=true로 PiP임을 확인.
- 첫 진단값은 connected/enabled=true,blocked=false,요청16/확인15,app=launcher,선택한 앱 대기. 숫자 position/duration은 이전 마지막 값이며 PiP 재생시간 추적으로 해석하지 않음.
- 이후 전면 앱은 Samsung Internet으로 관측되었고 제품 서비스 dumpsys가 반복 IOException: Timeout. ADB 연결·PID·접근성 등록은 유지, 자체 프로세스 isFrozen=false,TracerPid=0,crash 버퍼 출력 없음. 이는 진단 응답 실패의 증거이며 곧바로 앱 크래시나 PiP 원인 확정을 뜻하지 않음.
- observe-device의 이번 연속 관측은 No diagnostic service response로 중단되어 성공 표본으로 세지 않음. 이번 시험에서 입력/설정/권한/설치 변경 없음. 전체 화면 복귀 후 토글을 건드리지 않은 상태의 복구 비교 필요.

- 증상: 사용자에게 YouTube가 됐다가 안 되는 것으로 보임. Instagram 메인 + YouTube 작은 창이 함께 있었음을 사용자가 설명함.
- 관측: 15:46 요청11/확인10/pending 뒤 15:47~15:51 connected=true,enabled=true,blocked=true,요청11/확인10,상태 `넘김 확인 실패 · 껐다 켜 주세요` 유지. 해당 정지 구간의 화면 이동을 제품의 새 자동 요청 성공으로 계산하지 않음.
- 직접 원인: 마지막 요청의 다른 페이지 확인이 제한시간4.5초 안에 성립하지 않아 failClosed로 진입, 이후 tick이 즉시 반환. 권한 연결은 정상. 최초 확인 실패의 구체적 원인(실제 스와이프 실패/조회 불가/창 상태)은 당시 진단값만으로 구분 불가.
- 영향: 같은 실행 세션의 자동 넘김이 정지 상태로 남음. 두 앱 reader는 패키지별로 분리되지만 공통 실행의 blocked 상태는 공유함. 플로팅OFF에서 실패 상태가 시청 화면에 드러나지 않음.
- 잘못된 접근: 한 번 화면이 바뀌거나 과거 회귀시험이 통과했다는 이유로 현재 정상이라고 판단, PiP가 있었던 정황만으로 최초 실패 원인 확정. 이를 철회하고 요청/확인 카운터와 실제 화면을 함께 대조함.
- 진단/복구: APK·코드·권한은 변경하지 않음. 진단자가 빠른 설정을 열어 자체 타일OFF→ON으로 정지 해제 후 패널을 닫음. 이는 제품 스와이프가 아닌 별도 시험 조작. 제품 제스처는 page의 y75%→25%,280ms 상향이며 화면 최상단에서 시작하지 않음.
- 재시험: 15:52:03~15:54:24 전체 화면 비조작 관측에서59→58→15→11초 영상, 추가요청3/추가확인3(누계11/10→14/13). 전환 대기·확인 및 다른 영상 화면 확인. 기존 실패1건이 사라진 것처럼 누계를14/14로 기록하지 않음.
- 증거: private/device-tests/v021c-youtube-regression-report.jsonl, v021d-fullscreen-observation.jsonl, v021d-fullscreen-transition.jsonl, v021d-second-transition.jsonl 및 동일 접두 실제 화면. 시청내용/계정은 공개 문서에 옮기지 않음.
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

- 증상/재현: 우하단에 명확한 광고 글자가 있고 더 알아보기 카드가 떠 있는데 일반 영상으로 계산됨. 사용자 제보 화면에서 재현.
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
- 확인 증거: private/device-tests/v011-target2-first.jsonl. 같은 화면에서 0.1.2 적용 후 17→19→21초, 51→0초 및 0/2→1/2 확인. 영상 제목/계정은 기록하지 않음.
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
- 증거: private/device-tests/v013-reset-reason.jsonl, 19번 표본 이후. 독립 리뷰에서도 14초 중복 표본 후 16초로 바뀌는 입력으로 초기화 재현.
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
- 확인 증거: private/device-captures/v014-shade-safety.png와 서비스 숫자 진단. 자동 넘김은 실행하기 전 X로 정지, 요청4/확인4 유지.
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
- 증거: private/instagram-probe/sample-1.txt(누락예외),sample-1-progress.txt(지연),sample-2-cached-progress.txt(약400ms진행).
- 영향: 지원 가능성 조사 속도/정확성. 설치앱의 동작 경로는 미변경.
- 잘못된 접근: 일반XML덤프에 RangeInfo가 포함되거나 반복 탐색이 무간섭 고속이라고 가정.
- 대응: 일시정지로 UI구조 관측. 기기에 있는 android.test.runner/base/uiautomator jar를 classpath에 명시해 app_process 실행. 전용 scrubber 노드 획득 후 refresh로 숫자만 읽고 실패 시 재조회.
- 재발방지: 읽기전용·노드 해제·조회 설정 복원. 분석은 fresh=true 및 visible=true만 채택. 제품 E2E와 UIAutomator 동시 실행 금지. assertion 없는 OK를 기능 PASS로 오인하지 않도록 문서화.
- 재시험: 첫 영상136개 정상 표본/경계3회, 다음 영상의 다른 길이/반복 경계 확인. 제품 서비스의 조회 지연·동일영상·창 보호는 통합 후 시험 필요.

## D-011 · 패키지 변경 검사에서 null root를 앱 전환으로 오인 · 0.2.0 수정

- 증상/재현: 자동 제스처 pending 중 root가 잠시 null이면 패키지를 빈 문자열로 해석하여 invalidate/gate.cancel에 진입할 수 있었다. 코드/조건 재현이며 실제 오스와이프는 관측하지 않음.
- 직접 원인: 앱 정보 없음과 알려진 다른 앱으로의 이동을 구분하지 않음.
- 확인 증거: 부모·독립 리뷰 양쪽에서 확인. SessionPolicyTest.missingRootRetainsPendingConfirmation로 null/빈문자열과 pending→WAITING→FAILED 경로 재현.
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
