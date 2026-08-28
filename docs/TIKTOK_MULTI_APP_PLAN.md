# TikTok·세 앱·시간 설정 기획과 구현 상태 / Plan and implementation status · 0.4.0

## 2026-08-29 구현 체크포인트 / Implementation checkpoint

기준0.4.0/code33.0.3.0이후계획중근거가확보된일반비율진행경로·다중호스트기반·시간설정·1.3초시작범위를구현했다. 공개완료여부와최종시험은[릴리스](releases/v0.4.0.md),[검증](VERIFICATION.md)을따른다. 아래사전기획의‘오늘은문서만/대기/미구현’은그날의범위이며현재작업중단을뜻하지않는다.

Version0.4.0 implements the evidence-backed normalized-progress route,multi-host foundation,timing settings and start tolerance. Historical planning-only statements below do not describe the current delivery phase.

| 계획 / Plan | 구현 상태 / Implementation | 남은 확인 / Remaining validation |
| --- | --- | --- |
| TikTok일반추천영상 / Ordinary recommendation video | `com.ss.android.ugc.trill`의비율진행·페이지/창확인,선택OFF/반복2 / Normalized progress with scoped guards | 실제반복0/1/2·정지·10연속 / Physical0/1/2,pauses,ten consecutive |
| 세호스트기반 / Three hosts | 명시적registry,독립세션/설정/플로팅,공정직렬입력 / Registry,isolation,fair serialized input | 실제세앱각10/총30,순서/회전/포커스 / Physical30,order,rotation,focus |
| Instagram시간제 / Clockless timer | 정수2–60,미설정3,판별2초포함,기존값보존 / Whole2–60,default3,qualification included | 광고직후·가림·수동복귀실폰 / Physical ad return,obstruction |
| Instagram광고대기 / Ad delay |0.0–9.9/0.1단위,기본0.0,넘기기전,양수는동일광고source필요 / Pre-skip integer tenths | 지연0/0.3/1.3비교와다음영상첫관측 / Physical delay/next-start comparisons |
| 시작인식 / Start recognition | 초단위일반상한1.3초+길이10%,resetReason진단 / Cap1.3s/10%,reason diagnostics | D-044별도초기화호출원인·YouTube동일성 / Other reset callers and YouTube cause |
| TikTok시간제·광고·LIVE·사진·긴영상 / Other TikTok rules | **미지원·보류**,활성옵션없음 / Unsupported,not exposed | 종류별신뢰근거와독립시험필요 / Positive evidence and separate tests |
| 오디오·MediaSession·화면보조·초점왕복 / Alternative approaches | 제품연결하지않음 / Not connected | 새권한/범위는별도결정 / Separate authority and evidence |

광고대기기본값은기존즉시동작을보존하는0.0초로결정했다. 양수대기중독립source정보가없거나화면조건이바뀌면즉시추정스와이프하지않는다. 지연이전환확인보호를대체하거나다음릴스시작누락을모두해결한다는주장은하지않는다.

Ad delay defaults to0.0 to preserve existing behavior. Positive waits require ad identity continuity and never replace transition guards or prove all missed starts fixed.

읽기관측6개(진행4·숨김1·홍보1)와새제품자동화시험은구분한다. 폰연결종료후신규실폰시험은NOT RUN이고[현장점검표](FIELD_TEST_0.4.0.md)를사용한다.미확정TikTok전환은안전정지후전체OFF→ON이필요할수있다.

The six-page survey is not an automation pass. New phone tests are unrun;an unconfirmed TikTok transition can require masterOFF→ON.

## 사전기획 원문·조건부 대안 / Historical proposal and conditional alternatives

아래기본미정·미구현표현은위현재결정으로대체된경우과거기록이다. 조건부실험을모두출시기능으로읽지않으며과거관측/계획리뷰를제품시험PASS로계산하지않는다.

The following proposal is retained for rationale;superseded defaults/statuses are historical and conditional alternatives are not shipped capabilities.

작성·조사: 2026-08-28. 상태: **기획 전용·미구현 / PLANNING ONLY — NOT IMPLEMENTED**.

2026-08-29 후속: [현재 TikTok 추천 영상 관측](TIKTOK_FEASIBILITY_2026-08-29.md)에서 설치본 `com.ss.android.ugc.trill`46.7.3의 선택 탭/보이는 영상 구조를 읽었다. 기존 예상 패키지와 다르며 범용 구분·시계·자동 넘김은 미검증이다. 조사만 마치고 대기한다. 별도 일반 시작 상한1.3초는 로컬 소스/570시험에만 반영됐고폰에는 미설치다. 아래‘기기 접근 없음/문서만’은8월28일의 사전기획 경계를 기록한 것이다. / Later read-only device observation found structural evidence in a different installed TikTok package;general detection/clock/automation are unverified. Further work is on hold. The separate1.3s ordinary cap is local-only,not installed. No-device statements below describe the earlier planning checkpoint.

추가6페이지비교: 일반4개에서보이는0–10000진행값증가,일반1개에서숨겨진0값,리워드홍보1개에서시계없음을확인했다. 홍보에도보이는player가있으므로컨테이너만으로일반영상판정하지않는다. 별도홍보구조후보/예외와한계는[표본기록](TIKTOK_FEASIBILITY_2026-08-29.md#후속--6개-페이지-신속-비교--six-page-follow-up)에보존한다. 반복/길이/사진/LIVE/자동넘김은대기. / Six-page follow-up found moving ranges in four videos and a promotion counterexample to player-only detection;duration/loops/special-content generality/automation remain unverified.

기준: 공개 제품 0.3.0/code32, 제품 소스 `6bfe330`, 문서 기준 `81de796`. 현재 제품은 YouTube·Instagram 대상이며, 이 문서는 TikTok 지원을 추가했다고 선언하지 않는다. 이번 작업은 코드·공식 자료·공개 저장소 조사와 로컬 문서 작성뿐이다. 기기 접근, 앱 실행·설치, 권한 변경, 빌드, 제품 시험, 버전 변경, GitHub 게시를 하지 않는다.

추가 범위: §4.1의 Instagram 진행정보 없는 영상 시간 변경과 광고 넘김 지연을 다음 TikTok 업데이트에 함께 구현·시험·기존 Public GitHub 게시할 작업 묶음에 포함한다. 오늘은 기획 문서만 수정하며, 다음 작업 재개 전 구현·기기 조작·게시를 시작하지 않는다. 확정 요구와 아직 미정인 값은 §4.1에서 구분한다.

EN: This is a planning document, not a shipped release contract. The published product is unchanged. The next batch includes the two timing requirements in §4.1 alongside TikTok, testing and publication to the existing Public repository. Implementation awaits the next work session; this task changes documentation only. Explicit requirements and undecided defaults are distinguished below.

## 1. 권장 방향 / Recommended direction

**현재 앱별 독립 구조를 세 앱으로 확장하되, TikTok 단독 감지부터 증명한다.** 접근성에 실제 진행 정보가 있으면 횟수대로 넘기고, 없으면 명확히 분리된 선택형 시간제를 검토한다. 여러 앱 모드를 켜둔 채 보이는 대상 수가 1→2→3→1로 달라져도 자동 대응하는 것이 목표다. 위치 순서가 아니라 앱과 창의 식별자로 상태를 분리한다.

세 앱을 읽는 작업은 번갈아 수행해도 되지만, 세 앱의 카운터·시간제·일시정지는 각각 독립적이어야 한다. 스와이프는 서로 취소하지 않도록 한 번에 하나만 짧게 실행한다. 재생 유지를 위해 별도의 초점 왕복을 하지 않되, 실제 넘김 입력에 따라 시스템 초점이 바뀔 수는 있다. 비초점 앱이 재생 자체를 멈추면 시간이 지났다는 이유로 재생 횟수를 만들어내지 않는다.

EN: Extend isolated host sessions to three, prove TikTok alone first, then validate pairs and triple layouts. Independent observation and counters do not require simultaneous touch input. Do not add focus-cycling or forced playback, although a real navigation gesture may change system focus. Never invent playback completion for a paused host.

## 2. 확인한 사실과 아직 모르는 것 / Evidence boundary

| 항목 | 이번 확인 | 기획에 미치는 영향 |
| --- | --- | --- |
| 기존 구조 | 코드 확인: 세션·설정·플로팅 분리가 있으나 두 앱을 직접 열거하는 부분이 남음 | TikTok 이름만 추가해서는 부족함 |
| TikTok 패키지 | 최초 공식 Play 후보 `com.zhiliaoapp.musically` [S1];8월29일 설치본은 `com.ss.android.ugc.trill`46.7.3 | 실제 설치 변형을 추가 후보로 기록. 구조 동일성/전체 변형 지원은 미검증 |
| 세 창 배치 | Samsung은 Fold 계열의 3분할 사례를 안내 [S2] | 기종·OS·앱 조합별 가능성이지 현재 폰에서 세 앱 검증은 아님 |
| 여러 창 재생 | Android 10+의 multi-resume과 오디오 포커스는 별도 동작 [S3][S4] | 보임·접근성 읽힘·실제 재생·자동 넘김 성공을 따로 기록 |
| TikTok 재생/페이지 정보 | 현재 추천 탭·보이는 영상 구조는 관측,유효 진행 시계 미확보 | 반복 복귀,게시물 경계,정지,광고/LIVE/사진은 추가 검증 전 대기 |
| 세 앱 동시 자동 넘김 | **실기기 NOT RUN** | 기존 두 앱 시험을 세 앱 성공으로 재사용하지 않음 |

Android의 기본 분할 지원을 모든 폰의 3분할 보장으로 설명하지 않는다. Android 8–9에서도 기존 단일 앱 지원은 유지하는 방향이지만, 여러 창의 실제 재생 가능성은 앱·제조사 구현에 달려 있다. Android 10+의 여러 `RESUMED` 활동도 세 영상이 계속 재생된다는 증거는 아니다. 오디오는 혼합될 수 있지만 포커스 소유자는 하나이고, 앱이 포커스를 잃어 일시정지할 수 있다. [S3][S4]

## 3. TikTok 인식·넘김 방법 비교 / Detection and action options

| 경로 | 용도·장점 | 전제·위험·선택 기준 |
| --- | --- | --- |
| A. 접근성 재생 정보 — 우선 | 시간 텍스트·범위 정보·현재/전체 진행에서 실제 반복 판정 | 값의 단위·갱신·일시정지·되감기·반복 시 초기화 확인. 백분율만 보고 영상 길이를 추정하지 않음 |
| B. 접근성 다음 버튼/스크롤 — 우선 | 해당 창의 검증된 다음 게시물 버튼이나 pager 동작 사용 | 노드가 실제 다음 게시물인지 검증. 스크롤 이벤트만으로 재생 완료나 전환 성공을 판정하지 않음 |
| C. 창 안쪽 스와이프 — 대안 | 버튼/스크롤 동작이 없을 때 기존 제스처 경로 확장 | 최신 창 경계·가림 확인 후 대상 창 안쪽 경로를 화면 좌표로 계산. 전체화면 크기 기준 고정 경로 금지 |
| D. TikTok 피드 탐색 버튼 — 조사 | 공식 도움말에 재생/다음·이전 게시물 버튼 안내 [S5] | 안내상 TalkBack/VoiceOver 활성화가 전제. ShortsLoop 접근성만 켜면 나온다고 가정하지 않으며 TalkBack을 임의로 켜지 않음 |
| E. MediaSession — 조건부 보조 | 앱이 신뢰할 수 있는 재생 위치를 제공하면 보조 시계 후보 [S6] | 일반 알림 표시 허용이 아닌, 사용자가 활성화한 NotificationListenerService의 알림 접근 경로 등이 필요. 데이터 존재를 조사하고 새 권한은 별도 승인 |
| F. 진행 막대 화면 분석 — 후순위 | 접근성에서 누락된 시각적 진행 정보 관측 | 현재 API34+ 실험 기능과 별개로 검토. 가림·좁은 창·사진·앱 UI 변화에 취약. 현재 두 앱 이상일 때의 화면 분석 제한을 그대로 풀지 않음 |
| G. 앱별 오디오 비교 — 후순위 | 허용된 앱 소리를 UID별로 분리해 반복 보조 후보를 만들 수 있음 [S7] | Android 10+, RECORD_AUDIO·MediaProjection 동의·동일 사용자 프로필·원본의 허용 용도/캡처 정책 필요. 음악 반복≠영상 반복, 무음/사진 배경음/정지 오인 위험. 단독 넘김 근거로 쓰지 않음 |
| H. 선택형 시간제 — 현실적 대체 | 일반 영상으로 확인됐지만 시계가 없는 경우 일정 시간 후 넘김 | 정확한 반복 횟수 기능이 아님. 모르는 화면·정지·댓글·가림을 시간제로 통과시키지 않음 |
| I. TikTok Display/Embed API — 별도 제품 | 메타데이터 또는 자체 임베드 플레이어의 시간·이미지 이벤트 활용 [S8] | 조사한 API는 일반 TikTok 앱에서 보고 있는 추천 피드 제어 API가 아님. 별도 뷰어로 바꾸는 것은 이번 목표 밖 |

입력 요청 접수나 제스처 완료는 다음 영상이 됐다는 뜻이 아니다. 동일 창·pager의 신선한 페이지 전환 근거를 확인해야 한다. Android `dispatchGesture`는 진행 중인 다른 제스처를 취소할 수 있어 전역 입력 직렬화를 유지한다. [S9] 오디오 경로의 MediaProjection 동의는 캡처 승인 절차를 뜻하며 화면 영상을 녹화·저장해야 한다는 뜻은 아니다. [S7]

우회안 중 수정 TikTok APK, 루팅, 통신 가로채기, 인증정보 추출, 영상 다운로드·외부 업로드는 제외한다. 자체 자동 넘김 기능이 설치본에 있다면 중복 넘김 충돌을 조사하되, 모든 TikTok 버전에 그 기능이 있다고 가정하지 않는다.

EN: Prefer actual accessibility progress plus verified per-window navigation. A timeout is an explicit fallback, not an exact loop counter. Media sessions, visual analysis and audio are conditional experiments, not promised features. The reviewed web APIs do not control the currently viewed native recommendation feed. No modified host APK, interception or content upload is proposed.

## 4. TikTok 기능 범위와 제안 기본값 / Proposed capability scope

- **첫 단계:** 일반 TikTok 추천 피드의 일반 단일 영상. 팔로잉 피드도 동일 구조인지 별도 확인한다. 앱 선택은 최초 OFF, 반복 초기값은 2회 제안이며 기존 앱 설정을 덮어쓰지 않는다. 반복 0–99·숫자 입력/화살표·플로팅 두 탭 모드는 기존과 일치시킨다.
- **시간제:** 일반 영상임이 확인되고 진행 정보만 없는 경우에 한해 OFF 기본, 초기 10초·5–60초 범위를 제안한다. 앱별 저장하고 반복 0에서는 작동하지 않는 기존 의미를 유지한다. 시간은 횟수와 곱하지 않는다. 시간제만 가능한 설치본은 ‘횟수 인식 미지원’을 명시하고 시간 상태를 표시한다. 틱톡의 정지/페이지 경계를 충분히 구분하지 못하면 시간제도 지원 보류한다.
- **긴 영상:** 전체 길이와 정상 재생이 확인되는 경우에만 기존 기준을 적용한다. 기본 OFF, 길이 불명은 긴 영상으로 추정하지 않는다.
- **광고·LIVE·사진:** TikTok 전용 구분 근거 확보 후 별도 옵션으로 검토한다. Instagram 광고/사진 또는 YouTube LIVE 규칙을 통째로 재사용하지 않는다. 지원이 증명되지 않은 옵션은 활성화 가능한 메뉴로 내놓지 않는다.
- **사진 추가 시:** 전체 게시물 넘김과 각 장 넘김을 구분하고, 장 번호·마지막 장·혼합 게시물·번호 없는 경우의 정책을 먼저 정한다. 기존 Instagram의 각 3초·0–10초를 일관성 후보로 삼되 실제 TikTok 구조 확인 전 확정하지 않는다.
- **0의 의미:** 일반 횟수 0은 일반 반복/시간제 OFF. 별도 지원·활성화된 광고/라이브/사진/긴 영상은 독립 규칙. 전체 실행 OFF는 모두 OFF. 특수 콘텐츠 시간의 0초는 지원된 화면의 안전 확인 후 즉시이며 기능 OFF와 다르다.
- **처음 제외:** TikTok Lite, Douyin, 복제 앱/업무 프로필, 같은 앱 여러 인스턴스, 로그인·검색·쇼핑·메시지·일반 프로필 화면. 패키지를 넓게 허용하거나 미확인 화면을 일반 영상으로 처리하지 않는다.

EN: Start with the ordinary native recommendation feed, with TikTok deselected by default. Proposed ordinary controls match existing semantics. Clockless timing and special-content rules require positive eligibility evidence; unknown screens remain unsupported. Preserve existing saved preferences and permissions; the deliberate Instagram timing changes are specified next.

### 4.1 다음 묶음에 추가할 시간 설정 두 항목 / Two additional timing requirements

이 절의 변경값은 **다음 구현의 확정 요구사항**이며, 현재 제품에는 미반영이다. 기존 `AGENTS.md`/제품 기준의 Instagram 시간제 5–60초·초기10초를 다음 버전에서 아래 값으로 변경한다. 현재 버전 설명을 미리 바꾸지 않고 실제 구현 시 관련 계약·도움말·한영 리소스·시험을 함께 갱신한다. TikTok 시간제의 §4 제안값은 이번 Instagram 요청으로 자동 변경하지 않는다.

| 항목 | 다음 버전 요구사항 | 0의 의미·적용 범위 |
| --- | --- | --- |
| Instagram 진행정보 없는 영상 | **2–60초, 기본 3초**, 정수 초 입력과 ±1초 조절 | 2초 안전 확인을 유지. 0/1초는 허용하지 않으며 기능 OFF는 기존 토글/횟수0으로 구분 |
| 광고 넘김 지연 | **0.0–9.9초, 0.1초 단위**, 숫자 입력과 ±0.1초 조절 | 0.0초=안전하게 광고로 판별한 뒤 추가 지연 없이 넘김. 광고 옵션 OFF와 다름 |

**A. 진행정보 없는 Instagram 영상**

- 현재의 선택형 시간제 토글과 적용 자격을 유지한다. 전체 실행 ON·Instagram 선택·시간제 ON·일반 반복 횟수 1 이상에서만 동작한다. **반복 횟수 0이면 시간제가 꺼지는 기존 의미는 바꾸지 않는다.** 시간 입력의 최솟값2초, 기능 토글OFF, 반복 횟수0을 한영 도움말에서 구분한다.
- 진행정보를 정상적으로 읽는 영상은 여전히 횟수대로 처리한다. 진행정보 없는 일반 영상에만 시간제를 적용하고, 판정 도중 정상 진행정보가 생기면 시간제 의도를 취소하고 기존 반복 관측 경로로 전환한다. 광고·사진·LIVE·댓글·혼합/미확인 화면을 이 옵션으로 처리하지 않는다.
- 기본 3초는 새 설치 또는 유효한 저장값이 없는 경우의 초기값이다. 기존에 저장한 유효한 5–60초 값은 유지한다. 10초가 저장돼 있으면 예전 기본값인지 사용자가 선택한 값인지 추측해서 3초로 덮어쓰지 않는다. 기능 토글도 자동으로 켜지 않는다.
- 단일 영상의 연속된 안전 관측 시작부터 시간을 계산하며 횟수와 곱하지 않는다. 페이지/창 변경·수동 조작·정지·가림·관측 단절·설정 변경에서는 오래된 타이머를 폐기하고 최신 페이지로 다시 자격을 확인한다. 이미 사용한 페이지 타이머가 설정 변경만으로 재발사되지 않게 한다.
- **현재 코드 근거·최종 결정:** `ClocklessTimeoutPolicy`는 최소5/기본10이고 `ClocklessTimeoutTracker`는 `QUALIFY_MS=2000`을 둔다. 같은 안전 페이지 키를 연속 관측하면서 2초가 지나고 설정 시간도 충족해야 요청을 만들며, 1.5초를 넘는 관측 공백 등에서는 다시 시작한다. 계산 자체가 2초 걸리거나 Android가 강제한 값이라는 뜻은 아니다. 기존의 보수적인 안정 확인 기준을 유지하는 방향에 따라 최초0초 제안을 **최소2초**로 변경한다. 확인 시간을 줄이거나 별도의0초 우회 경로를 만들지 않는다.
- **시간 합산:** 안정 확인2초는 설정 시간에 포함된다. 설정2초면 확인을 포함해 최소2초, 설정3초면 최소3초이며 2+3=5초가 아니다. 관측 주기·최종 안전 검사·입력 큐 때문에 실제 동작은 더 늦을 수 있다. 광고 지연에는 이 시간제 전용2초 하한을 새로 적용하지 않는다.

**B. 광고 넘김 전 지연**

- 이번 확정 대상은 기존에 지원하는 **Instagram 광고 넘김 경로**다. TikTok 광고는 전용 판별이 검증된 경우에만 같은 형식의 앱별 설정을 검토한다. 이 요청으로 미지원 앱의 광고를 인식한다고 선언하지 않는다.
- 광고 토글은 유지하고 제목을 ‘광고 자동 넘김 / Skip ads’로 정리한다. 바로 아래 ‘넘기기 전 대기 시간 / Delay before skipping’를 배치한다. 0.0초는 ‘바로 넘김’으로 설명하며 양수 설정인데 ‘바로’라고만 표시하지 않는다. 반복 횟수 0이어도 광고 옵션은 독립적으로 동작하고, 전체 OFF·호스트 정지·광고 OFF는 대기 중인 요청도 취소한다.
- **광고 지연 기본값은 아직 미지정이다.** 0.0초(추가 지연 없음)와 0.3/0.5/1.0초 등을 비교해 다음 구현·시험 때 정한다. 증거 없이 임의 값을 ‘인식 누락 해결값’으로 확정하지 않는다. 기존 광고 토글 상태를 보존하며 새 지연 키는 다른 시간 설정을 변경하지 않는다.
- 내부 값은 정수 0–99의 0.1초 단위로 저장하고 밀리초는 값×100으로 계산한다. 부동소수 누적 오차를 피하고 0.0·0.1·9.9를 정확히 왕복한다. 빈 입력·음수·9.9 초과·소수 둘째 자리 입력은 저장하지 않고 안내하며, 한영 숫자 표시와 키보드 입력을 검증한다.
- 지연 기준은 해당 광고를 안전하게 판별한 시점이다. 같은 광고의 반복 이벤트가 대기 시간을 계속 처음으로 되돌리지 않게 한다. 반대로 수동 넘김·광고 판별 소실·창/페이지 변경·설정 변경·가림/관측 단절에는 취소하고 새 근거로 다시 판단한다. 광고 공통 문자열만으로 서로 다른 광고를 같은 것으로 묶지 않는다. 경계가 불명확하면 이전 광고의 경과 시간을 이어 쓰지 않는다.
- **기다리는 동안 입력 큐/소유권을 점유하지 않는다.** 광고별 지연 완료 후에만 최신 광고/창 근거를 다시 확인해 공정 큐에 넣는다. 9.9초 지연을 기존 3초짜리 입력 대기 의도에 먼저 넣거나, 이를 위해 전체 큐 TTL을 늘리지 않는다. 다른 두 앱은 자신의 조건에 따라 계속 처리한다.
- 0.1초는 설정 해상도이지 실제 입력 시각의 100ms 오차 보장이 아니다. 기존 약300ms 관측 주기에만 의존하면 단계 차이가 뭉개질 수 있으므로 단조 시계와 취소 가능한 마감 시점 예약을 검토한다. 실행 시에는 반드시 신선한 페이지·설정 세대를 검사하고, 안전한 준비 상태보다 일찍 넘기지 않는다.
- 지연은 **광고를 넘기기 전**에 적용한다. 다음 릴스 진입 후 임의 대기를 붙여 시작 관측을 막는 기능으로 바꾸지 않는다. 광고 전환 확인과 다음 일반 영상의 초기 상태 수집을 각각 검증하며 이전 광고의 지연·카운터를 다음 릴스에 넘기지 않는다.

**관측 가설과 시험**

‘광고가 너무 빨리 넘어가 다음 릴스 처음을 놓친다’는 **원인 미확정 가설**이다. 실제 원인은 전환 확인, 이벤트 순서, 초기 노드 누락, 재사용된 광고 식별자 등일 수도 있다. 다음 디버그에서 광고 판별→지연 완료→입력→다음 페이지 확인→첫 진행값→반복 카운트 시작을 같은 빌드로 비교한다. 인식 실패를 억지 재시도·임의 카운트·확인 생략으로 숨기지 않는다.

- 시간제: 2/3/60초, 0/1/61초·잘못된 입력 거부, 정확한2초 경계/관측 공백 재시작, 정상 진행정보 복귀, 반복0/시간제OFF, 저장·재시작·업데이트 보존, 사진/광고 제외, 광고 직후 진행정보 없는 일반 영상의2초를 검사한다. 설정3초에2초가 추가되어5초가 되는 회귀를 차단한다.
- 광고 지연: 0.0/0.1/0.3/0.5/1.0/9.9초의 마감 전/동일/후 판정과 정수100단계 입력·저장을 단위 시험한다. 대기 도중 수동 이동·OFF·설정 변경·가림·콜백 지연·세 앱 동시 요청에서 중복/오래된 입력이 없어야 한다.
- 실폰 비교: 광고→진행정보 있는 영상, 광고→진행정보 없는 영상, 광고→사진, 연속 광고를 구분한다. 지연값별 실제 광고 전환과 다음 일반 영상의 첫 진행 관측·카운트 시작·설정 횟수 후 전환을 기록한다. 광고를 넘긴 사실만으로 다음 영상 인식 성공을 판정하지 않는다. 못 찾은 표본은 미검증으로 남긴다.
- 같은 업데이트의 인수 조건에 두 항목을 필수로 포함한다. TikTok 가능성이 막혔다고 이 둘을 누락하거나 조용히 따로 게시하지 않고, 범위 변경이 필요하면 진행 상태와 대안을 먼저 설명한다.

EN: Instagram's eligible clockless timer changes to **2–60 whole seconds, default3** for new/unset values; preserve valid saved values and opt-in state. The initial zero-second proposal is superseded to retain the existing2s qualification, included in the selected total:3s means3s, not5s. Ordinary count0 still disables this timer. Ads independently get a pre-skip delay of0.0–9.9s in0.1s steps; its default is undecided, and the clockless2s minimum is not applied to ads. Use integer tenths, cancel stale intents, and never hold the shared input lease during the delay. Missing next-Reel starts are an unconfirmed hypothesis: compare ad departure and destination progress/counter startup separately. These requirements join the next implementation/test/publication batch; nothing is implemented today.

### 4.2 가벼운 접촉·짧은 관측 누락 후 카운트 보존 / Light-contact continuity review

상태: **2026-08-29 Instagram 실폰2왕복 재현, 일부 초기화 호출 원인 미확정·미구현**. 최초에는 두 앱의 접촉 후 카운트0 보고로 시작했지만 추가 미세 접촉 없이도 Instagram에서 재현됐다. 시작 정보 지연/재초기화 뒤1초 시작 인정 범위를 놓치는 경로를 확인했다. YouTube는 사용자 보고 단계이고 같은 원인인지는 미확정이다. [D-044 관측·한계](DEBUG_LOG.md#d-044--가벼운-접촉-후-카운트-초기화--light-contact-reset-investigation)를 기준으로 하며, 앞선 사전기획의 기기 미접근 기록과 이번 별도 승인된 진단을 구분한다. 제품·설정·권한은 변경하지 않았다.

- **현재 코드 사실:** 일반 YouTube 영상의 모든 클릭을 무조건 초기화하는 것은 아니다. `HostPlaybackSession`은 SeekBar 클릭, 활성 긴영상/LIVE 등의 일부 클릭, 플로팅 접촉 등에서 초기화할 수 있다. 일반 경로도 `snapshot.usable()` 실패 시 즉시 `invalidate()`하며, `LoopCounter`는 영상 identity/길이 변화·3초 초과 관측 공백·비정상 진행 이동에서 초기화한다. YouTube의 진행 노드 갱신 실패/개수 변화, 비클릭 텍스트 기반 identity 변화가 접촉 직후 생기는지는 아직 확인하지 않았다.
- **목표:** 터치의 세기나 의도를 추측하지 않고 실제 페이지/재생 연속성으로 판단한다. 일반 영상의 제한된 일시적 읽기 실패는 즉시 전체 카운트를 버리는 대신, **입력 보류와 상태 초기화를 분리**하는 방안을 검토한다. 같은 호스트·창·페이지 및 정상 진행 연속성이 확인될 때만 보관한 카운트를 이어 쓴다. 짧게 멈췄다는 이유만으로 자동 복원하지 않는다.
- **새 시작 관측:**38.590초 실폰 표본의 첫 복귀는 refresh 후1.047초에서0,두 번째는0.540초에서1이었다가 세션 초기화 후2.161초에서0이었다.1초 시작 인정 기준과 전환 확인의 최소1.2초 대기를 별도로 검토한다. 대기 중 목적지 초기 표본을 읽기 전용으로 보관하되 확정된 목적지의 독립 페이지 근거·창·진행 연속성과 일치할 때만 사용한다. 이전 페이지/롤백/특수 콘텐츠 표본은 폐기한다. 요청 시점과 재생 시작은 같지 않으며, 이 보관안만으로 두 번째 초기화까지 해결됐다고 보지 않는다. 초기화 사유를 먼저 구분하며 단순한2초 무시나 시작 범위 확대를 해결책으로 확정하지 않는다.
- **광고 복귀 재현:**46.066초 릴스의 이전 광고로 이동→자동 복귀2회에서 확인 완료 후 첫 카운터 표본1.103초·1.015초가 모두1초 기준을 넘어0으로 대기했다. 확인 대기부터 복귀 이후 세션 세대는 유지됐다. 대기 중 실제 유효 초기 snapshot이 있었는지는 기존 덤프에 없어 아직 미확정이다. 표본 보관안을 구현하기 전 이를 확인하고,광고 지연만으로 해결됐다고 판단하지 않는다. [연속 실폰 근거](DEBUG_LOG.md#광고-복귀-연속-재현2회--two-controlled-ad-return-reproductions). / Two controlled ad returns reproduced late-seed0 at1.103s/1.015s without subsequent session invalidation. Verify actual early-snapshot availability before relying on buffering;ad delay alone is not a proven fix.
- **보류 범위:** 처음에는 전환 요청/복구/특수 콘텐츠 처리 중이 아닌, 진행정보를 읽고 있던 일반 영상만 대상으로 한다. 보류 중 자동 입력과 완료 횟수 증가는 금지한다. 보류 최대시간·재확인 표본 수는 실제 원인 관측 후 정하며, 반복 접촉이 보류를 무한 연장하지 못하게 한다. 보류 중 한 바퀴가 끝났거나 탐색/페이지 이동 여부를 확정할 수 없다면 그 구간을 완료 횟수로 인정하지 않는다.
- **identity 주의:** 현재 YouTube 일반 identity는 표시 텍스트에도 영향을 받는다. 이것이 바뀌었다는 이유를 전부 무시하거나, 길이만 같다고 같은 영상으로 인정하지 않는다. 텍스트 변화와 실제 페이지 변경을 나누려면 검증된 독립 페이지 근거가 필요하다. 근거가 없으면 카운트 연결을 보류/폐기하고 기존 시작점 관측 원칙을 따른다.
- **보호 유지:** 실제 영상 변경·진행바 탐색·일시정지·메뉴/댓글/키보드·잠금·창 경계 변경·가림·전체 OFF·설정 변경에는 기존 중단/초기화 보호를 유지한다. 거부/취소/미확인 입력과 진행 중 전환의 차단을 ‘가벼운 터치’ 명목으로 해제하지 않는다. 시간제2초 판별, 사진/광고/LIVE/긴영상 정책도 일괄 완화하지 않는다.
- **플로팅 구분:** 플로팅 탭의 횟수 변경, X의 해당 앱 정지, 드래그 기능은 유지한다. `1/2→0/2` 같은 현재 카운트 초기화와 설정값 자체의0 전환을 따로 기록한다. 플로팅을 실제로 누른 경우를 영상 가장자리의 무해한 접촉으로 처리하지 않는다.
- **검증:** YouTube 우선으로 무접촉/가장자리 접촉/영상 중앙 탭/진행바/플로팅/길게 누르기를 구분하고, Instagram에서도 같은 분류를 확인한다. 단독·두 앱·세 앱에서 한 앱의 접촉이 다른 앱 카운트를 건드리지 않아야 한다. 합성 시험은 일시적 읽기 누락,텍스트만 변화,같은 길이의 다른 영상,A→B→A,보류 중 끝→처음,복구 직전 메뉴/창 변경,반복 접촉,대기 요청/늦은 콜백을 포함한다.
- **표시:** 같은 영상임을 아직 재확인 중이라면 카운트 보존 표시와 ‘확인 중’을 구분한다. 이전 숫자를 그대로 표시하는 것만으로 기능을 고쳤다고 판정하지 않고, 실제 이어 세기·필요 횟수 후 정확한1회 넘김·위험 상황 입력0을 검증한다.

EN: The2026-08-29 authorized Instagram diagnosis reproduced start loss on two next/back round trips without additional light contact. A refresh/reseed at1.047s missed the target's1s start threshold; another return first counted at0.540s before session invalidation and reseeding at2.161s. The latter reset caller and YouTube causation remain unknown. Investigate destination-start buffering during the minimum1.2s confirmation guard separately from later invalidation; accept samples only after independent destination/window/continuity verification, discarding rollback and special content. Bounded ordinary-playback suspension remains a proposal, not a blanket2s ignore rule. Preserve seek/pause/menu/window guards and deliberate floating controls. This later device diagnosis supersedes the earlier no-device status for this issue only; no product/settings/permission/build/install/publication change occurred.

## 5. 세 앱의 창·재생 운영안 / Multi-window operation

1. **권장 배치:** 기기가 제공하는 겹치지 않는 3분할. 한 큰 창+두 작은 창, 가로/세로, 앱 위치 순서를 모두 검사한다. 창 크기 차이로 TikTok 진행 표시가 사라질 수 있다.
2. **조건부 대안:** 두 분할+일반 팝업. PiP와 일반 팝업을 구분한다. 팝업이 아래 창을 덮으면 기존 보호에 따라 가려진 앱이 멈출 수 있으므로 ‘세 앱 지원’으로 자동 포함하지 않는다.
3. **1/2/3 자동 대응:** 여러 앱 모드를 유지한 채 선택·표시된 안전한 대상만 처리한다. 숨겨진 앱에 입력하지 않고 다른 일반 앱을 만지지 않는다. 모드 OFF는 기존처럼 활성 대상 창 하나만 처리한다.
4. **한 앱이 일시정지:** 해당 앱에 이유를 표시하고 카운트를 만들지 않는다. 다른 앱도 각각 창·재생 조건을 충족할 때만 계속한다. 전역 잠금·모달창처럼 전체가 불안전하면 전체 입력을 막는다.
5. **초점이 필요한 경우:** 우선 의미 기반 다음 동작과 앱이 허용한 비초점 재생을 확인한다. 안 되면 현재 재생 가능한 1–2개만 처리하는 제한 모드가 우선이다. 강제 재생·무작위 탭·빠른 초점 왕복은 제품 기본안에서 제외한다. 명시적 초점 전환 실험은 별도 동의와 안전한 대상 제어가 확보된 후에만 검토하며, ‘동시 재생’으로 표시하지 않는다.
6. **소리 충돌:** 음소거 상태와 다음 영상 진입 때의 오디오 포커스 재요청을 시험한다. 제조사 다중 소리 기능은 별도 사용자 설정 후보일 뿐 필수 설치·강제 변경하지 않는다.
7. **범위:** 첫 목표는 한 물리 화면. DeX/외부 화면은 displayId와 입력 대상·플로팅 좌표 계약을 별도 검증해야 하므로 후속으로 둔다. PiP 자동 넘김은 계속 제외한다.

EN: Prefer non-overlapping three-way layouts. Two splits plus an overlapping popup is a separate conditional case. Automatically follow one, two or three eligible selected hosts; do not force playback when focus/audio rules pause another app. PiP and external-display operation are outside the first delivery.

## 6. 코드 영향 범위 / Architecture changes to plan

아래는 현재 코드를 읽어 확인한 확장 지점이다. 아직 수정하지 않았다. 파일명은 `app/src/main/java/com/fullmetalsonic/shortsloop/` 아래를 기준으로 한다.

| 영역 | 현재 확인 | 다음 구현 설계 |
| --- | --- | --- |
| `service/ShortsAccessibilityService.java` | YT/IG 세션을 직접 생성·호출, 두 앱 가시성 판단 | 명시적 지원 앱 registry와 세션 집합, 보이는 수 ≥2 판단 |
| `service/RuntimeState.java` | YT/IG 상태를 직접 구분 | HostId별 상태, 모르는 앱은 거부. 위치 변경으로 상태가 바뀌지 않음 |
| `data/SettingsStore.java` 및 UI | 두 앱 선택·접두사·설정 패널 분기 | 명시적 host→설정/표시/기능 지원표. `else=Instagram` 같은 우회 금지 |
| 설정 마이그레이션 | `host_settings_version` 완료 후 재실행을 막음 | 증가형 v2에서 신규 TT/광고 지연 키를 추가. 기존 legacy/YT/IG 값·자료형 보존, IG 시간제 미설정은3초. 재실행도 같은 결과 |
| `detection/ShortsReader.java`·`service/HostPlaybackSession.java` | 두 reader 및 비YT 경로의 IG 전용 처리 | `TikTokReader`와 앱별 탐색/페이지 확인 어댑터. 기존 snapshot 이름 정리를 이유로 무관한 전면 개편 금지 |
| `core/ActionArbiter.java` | 소유자 1개, 대기자 1개 | 최대 세 호스트의 중복 없는 FIFO/round-robin 대기. 한 호스트가 다른 호스트를 무기한 밀어내지 못하게 함 |
| 접근성 XML·Manifest | 선택 패키지/조회 대상 제한 | 검증된 TT 패키지만 추가. 전체 패키지 조회 권한 추가 금지 |
| 플로팅·타일·설정 화면 | 두 항목/두 이름을 전제로 한 곳 존재 | YT/IG/TT 안전한 라벨·한영 문구·세 상태, 좁은 화면 접근성 |
| `core/ClocklessTimeoutPolicy.java`·`ClocklessTimeoutTracker.java`·`ui/SecondsEditor.java` | IG 최소5/기본10,고정2초 판별 대기 | 2–60/기본3으로 변경하고2초 판별은 유지. 저장·문구·경계/총시간 시험 갱신 |
| `core/AdSkipPolicy.java`·`service/HostPlaybackSession.java`·광고 UI | IG 광고 토글,설정 가능한 광고 전 지연 없음 | 앱별 광고 지연 정책/추적기·0.1초 편집기·취소 가능한 예약·다음 릴스 시작 관측 검증 |

### 6.1 입력 공정성과 오래된 요청 / Fairness and stale intentions

현재 한 자리 대기자는 세 앱에서는 A/B만 번갈아 자리를 차지해 C가 계속 밀릴 수 있다. 이는 **세 앱 확장 시의 코드상 위험 분석**이며, 현재 두 앱에서 재현한 새 버그라는 뜻은 아니다.

- 앱당 대기 의도 하나만 유지하고 실행 조건을 동시에 충족한 세 앱의 유효 요청 순서를 공정하게 돌린다. TTL이 만료된 의도는 폐기한다. 광고 요청도 다른 앱의 요청을 무기한 추월하지 못한다.
- 대기에는 앱/창/페이지/요청 종류/설정 세대/생성 시점을 묶는다. 숨김, 창 이동·교체, 페이지 변경, 일시정지, 선택 해제, 설정 변경, 전체 OFF에서 부적합 의도를 폐기한다.
- 실행 직전 최신 창 경계·가림·페이지를 다시 읽는다. 만료된 요청을 늦게 실행하지 않고 새 관측으로 다시 판단한다. 연속 실패는 기존 차단/복구 계약을 따른다.
- 소유권 토큰을 유지해 늦게 온 콜백이 다음 앱의 입력 소유권을 해제하지 못하게 한다. 사용자 터치 충돌의 완전 감지를 주장하지 않으며 실제 혼합 조작으로 취소·차단 경로를 시험한다.
- 기존 대기자 유효 2초, 세션 대기 의도 3초, 콜백 유실 보호 약 1.8초, 입력 간격 180ms를 우선 기준으로 삼는다. 세 앱이라고 무작정 늘리지 말고 큐 대기/만료를 측정한 뒤 결정한다.

### 6.2 관측 비용·보호 유지 / Observation cost and guards

현재 각 세션이 약 300ms 간격으로 창/노드를 조회한다. 단순히 두 개에서 세 개로 늘리면 정기 tick 수는 약 6.7→10회/초가 되지만 실제 CPU 증가율은 측정 전 알 수 없다. 이벤트 합치기·짧은 주기의 창 메타데이터 공유를 검토하되, 입력 직전 신선도 검사는 공유 캐시로 대체하지 않는다. 노드 탐색 상한과 앱별 소요시간을 유지·측정한다.

`HostWindowAccess`/`WindowPolicy`의 PiP 제외, 가림, 동일 창 경계, 입력 경로 교차 보호는 유지한다. 모달창 때문에 `getWindows()`에서 뒤쪽 창이 빠질 수도 있으므로 ‘어제 있던 창’을 계속 조작하지 않는다. [S9] 서비스 전역 접근성 플래그를 TikTok 때문에 바꿀 경우 YT/IG 노드 변화도 회귀시험한다. 현재 화면 분석은 여러 호스트 가시 상태에서 제한되므로 세 앱 추가만으로 해제하지 않는다.

EN: Replace two-host enumeration with an explicit registry, incremental settings migration and a bounded fair action queue. Preserve immutable leases, fresh geometry checks, timeouts and existing safety contracts. Optimize observation only with measured evidence; input validation must remain fresh.

## 7. 설정·플로팅 UX / Settings and floating UX

기존 아이콘·색상·숫자 편집 스타일을 유지하고 새 디자인 도구나 아이콘 제작은 이번 범위에서 시작하지 않는다.

- **공통 영역:** 전체 실행, 적용 앱 YT/IG/TT, ‘여러 앱 동시 적용 / Process multiple apps’. 현재 `dual_mode` 키와 ON/OFF 값을 보존하고 표시명만 다중 앱에 맞게 바꾸는 안이다. 앱 하나/둘/셋마다 별도 모드는 만들지 않는다.
- **앱별 영역:** 적용 앱 선택과 ‘설정을 편집 중인 앱’을 구분한다. 충분한 폭에는 세 앱 선택 탭, 좁은 폭에는 이름이 읽히는 선택 메뉴를 검토한다. 세 개의 좁은 설정 칼럼을 강제하지 않는다.
- **항목 순서:** 일반 반복 → 지원되는 콘텐츠별 예외 → 플로팅. 기존 권한/배터리/도움말/업데이트 접근을 유지한다. 이번 확장을 핑계로 전체 화면 배치를 다시 설계하지 않는다.
- **추가 시간 UI:** IG 진행정보 없는 영상2–60초/기본3초와 광고 전 지연0.0–9.9초를 각 기능 카드 아래에 둔다. IG 시간제는2초 미만 입력 불가, 광고 지연0=즉시, 횟수0=일반/시간제OFF, 광고토글OFF=광고처리OFF를 구분한다. 사진·LIVE·긴영상 입력 범위와 기본값은 바꾸지 않는다.
- **우선 플로팅안:** 앱당 작은 YT/IG/TT 플로팅 최대 세 개. 기존 72×56dp를 출발점으로 실제 3분할에서 겹침·잘림·터치 크기 확인. 위치는 앱 창 상대좌표로 저장, X는 해당 앱만 일시정지, 표시 자체는 선택형이다.
- **대체 플로팅안:** 좁은 창에서 세 개가 방해되면 한 패널에 YT/IG/TT 세 줄로 모으는 안을 비교한다. 선택/닫기 범위를 명확히 해야 하므로 첫 구현에서 두 방식을 모두 약속하지 않는다.
- **상태:** 정상은 `1/2`처럼 간단히 유지. 정보 없음/일시정지/가림/입력 대기를 모두 카운트 `0`으로 표현하지 않는다. 플로팅은 짧은 표식과 접근성 설명, 자세한 이유는 앱 안에서 확인한다. ‘3개 감지’는 세 앱 재생 성공과 다르게 표시한다.
- **시각 검증:** 한국어/영어, 글꼴 2배, 320dp 및 실제 분할 최소 폭, 3분할의 작은 두 창, 회전·앱 순서 교환을 검사한다. 작은 X가 옆으로 길게 나가거나 글자가 잘리는 과거 문제를 다시 만들지 않는다.

EN: Keep current visual language, retain the existing multi-host preference, and separate host selection from host editing. Prefer one optional compact float per host; consider a combined panel only if real narrow-pane testing warrants it. Count, unavailable data, pause and queue waiting must not be confused.

## 8. 다음 작업 순서와 중단 기준 / Phases and stop gates

| 단계 | 다음 실행 때 할 일 | 통과 조건·대안 |
| --- | --- | --- |
| P0 관측 | 설치본 패키지/버전/언어와 일반·짧은·긴·시계 없는·광고·LIVE·사진 구조 비교 | 실제 시계/페이지/정지 근거표 작성. 구조가 확인되면 빨리 다음 표본으로, 반복 검증만 실제 처음→끝→처음을 기다림 |
| P1 공통 기반 | registry·v2 마이그레이션·세 앱 공정 큐를 독립 단위로 구현 | 기존 두 앱 회귀+합성 세 앱 상태/큐/설정 시험. 실물 TT를 본 것처럼 보고하지 않음 |
| P1-T 시간 설정 개선 | §4.1 IG 시간제2–60/기본3과 광고0.0–9.9 지연을 별도 정책/UI로 구현 | 저장 보존·2초 판별 포함 총시간·광고0의 의미·지연 취소·다음 릴스 시작 인식 비교. 기존 두 앱 회귀,추가 광고 기본값 결정 근거 기록 |
| P1-C 접촉 민감도 조사 | §4.2/D-044 두 앱의 사용자 재현 보고를 각각 로그로 대조하고 공통/앱별 경로 점검,보류/초기화 분리 설계 | 실제 초기화 사유를 확인한 뒤 필요한 범위만 완화. 원인 미확정/단순 표시 보존을 수정 완료로 처리하지 않음 |
| P2a TikTok 반복 경로 | 시계가 검증되면 일반 반복과 전환 확인부터 구현 | 단독 10개 자동 전환 확인, 반복 0/1/2와 중단/복구 별도 확인 |
| P2b TikTok 시간제 경로 | 시계는 없으나 일반 영상·정지·페이지를 구분할 수 있으면 승인된 시간제부터 구현 | 시간제로 단독 10개 전환, OFF/0·설정 시간·정지/중단 검사. 반복 인식 성공으로 보고하지 않음 |
| P3 두 앱·세 앱 | 선택된 P2 단독 경로 통과 후 YT+IG, YT+TT, IG+TT → YT+IG+TT | 앱별 분리·순서·회전·1/2/3 변화·포커스/음성 확인. 불가하면 실제 가능 범위를 명시 |
| P4 추가 콘텐츠 | 근거가 확보된 TT 긴 영상/광고/LIVE/사진과 보완 시간제만 추가 | 종류별 독립 시험과 미지원 설명. 근거 없는 필터는 보류 |
| P5 전달 | TikTok·여러 앱 및 §4.1 두 항목을 한 업데이트로 구현·시험 후 최종 소스/설치 APK/문서/독립 리뷰 정렬 | 다음 작업 재개 후 검증 통과 시 기존 Public GitHub에 게시. CI·공개파일/설치본 해시·업데이트 경로 확인. 오늘은 배포하지 않으며 새 권한/범위 변경은 별도 확인 |

P0에서 A: 시계·페이지 충분→P2a, B: 일반 영상·정지·페이지 충분하나 시계 없음→P2b, C: 화면/정지/페이지 근거도 불충분→TikTok 확장 보류로 분기한다. P1은 어느 TikTok 지원 경로에도 필요한 공통 기반이며, 근거가 불충분한 C 상태에서 TikTok용 제품 변경부터 시작하지 않는다. 독립적인 P1-T 두 항목은 계획에 유지하고, 묶음 전달이 불가능하면 범위를 먼저 논의한다. 시간제를 먼저 선택해도 이후 정확한 시계 경로를 별도 검증할 수 있다.

같은 원인 불명의 실패에 대한 추측성 수정은 약 5회 이내로 제한하고, 사실·시도·실패 이유·대안을 정리해 다음 변경 전에 논의한다. 세 창이 보여도 지속 재생/신뢰할 정보가 없으면 ‘세 앱 동시 지원 완료’라고 하지 않는다. 기존 0.3.0 산출물은 보존하고, 되돌리기를 이유로 데이터 삭제·무단 다운그레이드하지 않는다.

EN: Observe first, then test the generic core, both timing improvements, TikTok alone, pairs and triple operation. Special content requires evidence. The requested next batch proceeds through implementation, testing and existing-Public publication when work resumes; no execution occurs in this planning task. New permissions or a changed delivery scope still require confirmation.

## 9. 예방·시험 설계 / Risk-driven validation plan

| 위험 | 자동 시험·실기기 확인 계획 |
| --- | --- |
| 세 번째 앱이 계속 밀림 | 합성 세 앱 지속 요청 1,000회 이상에서 중복/기아 없음, 호스트별 획득 수·최대 대기·만료 기록. 타이머 만료를 자동 성공으로 계산하지 않음 |
| 이전 설정 초기화/잘못된 앱 선택 | 구버전 legacy→신규, 0.3.0→신규, 재실행, 자료형/전체 키 보존. TT 신규 선택OFF,IG 시간제 기존 저장값 보존/미설정3,광고 지연 키 분리 |
| 시간 범위/0의 의미 혼동·광고 지연이 다음 영상에 남음 | §4.1의 시간제2/3/60 허용·0/1 거부 및 광고0.0/0.1/9.9 경계·취소·정수100단계·광고 이후 영상/사진/연속광고 검사. 큐 대기와 광고 전 지연을 분리 |
| 창 순서에 따라 카운트 섞임 | 선택 8조합(없음/단일3/쌍3/셋1), 세 앱 위치 6순열, 가로/세로·창 크기 변경·창 ID 재생성 |
| 사라진 창에 늦은 입력 | 모드 유지 3→2→1→3, 숨김/선택 해제/전체 OFF/재연결 중 대기·늦은 콜백. 폐기한 의도 재실행 0 |
| 두 입력 충돌/수동 조작 방해 | 동시에 만료·스크롤 실패·콜백 유실·취소·사용자 드래그·플로팅 이동·분할 손잡이. 소유권 누수/이중 입력/미확인 재시도 차단 |
| 영상 반복·수동 되감기 혼동 | 진짜 끝→처음, 사용자 탐색, 버퍼링, 동일 길이의 다른 게시물, A→B→A, 진행값 누락/복귀 |
| 가벼운 접촉과 실제 조작 혼동 | §4.2의 접촉 분류/일시 누락/보류 중 반복 경계/플로팅 설정 변경을 구분. 한 앱의 접촉이 다른 앱에 전파되지 않고 위험한 입력은 계속 차단 |
| 사진 음악을 영상 시계로 오인 | 사진/혼합/배경음 반복, 장 번호 없음, 마지막 장, 사진→일반/광고를 별도 표본으로 기록 |
| 광고/LIVE 오탐 | 제목·댓글의 단어가 아닌 화면별 근거. 불확실 콘텐츠에는 특수 넘김/시간제 적용 금지 |
| 가림·알림·키보드 | 메뉴/댓글/공유/키보드/탐색 플로팅/전체 모달·PiP. 안전하지 않으면 입력 0, 회복 때 기존 확인 절차 유지 |
| 일부만 재생/음성 포커스 변경 | 세 앱 조용한/소리 있는 상태, 한 앱 정지·재개, 다음 게시물 진입 직후. 실제 진행값을 앱별 관측 |
| 전역 플래그/부하 회귀 | 기존 YT/IG 단독·두 앱·화면 분석 제한 유지, 같은 조건 두 앱/세 앱의 CPU·메모리·tick 지연·발열 비교 |
| 개인정보·배포 오염 | 합성 노드 fixture만 공개, 계정/영상/댓글/원시 XML/화면/기기 식별자는 제외. 콘텐츠·오디오 RAM 처리, 외부 전송 추가 없음 |

실폰 최종 연속 시험 제안: TikTok 단독 10회와 별개로 **세 앱을 함께 켠 채 앱별 10회, 총 30회 이상 확인된 자동 전환**. 중간 수동 넘김·앱 재시작·코드 변경은 연속 성공에 포함하지 않는다. 일반/광고/긴 영상/사진/시간제는 종류별 집계하며 광고 성공도 해당 종류의 성공으로 인정한다. 정확한 반복 기능은 별도의 일반 영상 완료 표본이 필요하다. 짧은 영상을 골라 불필요한 대기를 줄이고, 긴 영상을 수동으로 건너뛴 구간은 탐색/별도 구간으로 적는다.

추가로 API26/33/34 호환성·설정 복원과 실제 대상 폰의 3분할을 구분한다. 성능 비교는 동일 밝기·음량·유사 콘텐츠 조건의 5–10분 측정과 20–30분 유지 시험을 후보로 두며, 검사 전 ‘배터리 영향 없음’이라고 하지 않는다. 실제 기기가 세 앱 재생을 허용하지 않으면 최대 1–2개 검증 결과로 범위를 줄이고 세 앱 목표는 미달로 남긴다.

EN: Keep synthetic queue/migration tests separate from physical playback evidence. Proposed acceptance includes ten confirmed automatic transitions per host while all three are present, with transition types reported separately. Manual skips are not automatic passes. Preserve exact build identity and all remaining device/OS limitations.

## 10. GitHub 조사 결과 / Inspected open-source examples

조사 범위는 아래 세 저장소의 README·실제 관련 소스·라이선스 표시다. 완성된 TikTok 정확한 반복+세 창 지원 대체품을 이 표본에서 확인하지 못했다는 뜻이지, 인터넷 전체에 없다는 결론은 아니다. 코드를 가져오거나 실행하지 않았다.

| 저장소·고정 소스 | 확인 내용 | 재사용 판단 |
| --- | --- | --- |
| [Auto-reel-Scroller / AutoScrollService](https://github.com/OmShrikhande/Auto-reel-Scroller/blob/5de5884c5093e159535f09ee77c2bc0a2e7f7b1b/app/src/main/java/com/example/autoscrollapp/AutoScrollService.kt) | 타이머와 전체 화면 기준 스와이프. 영상 끝/반복·세 창 전환 확인 근거 없음 | README의 MIT 표기와 달리 조사한 tree에 LICENSE 파일을 확인하지 못함. 복사하지 않음 |
| [AutoSlide / AutoSlideService](https://github.com/tianxing-ovo/AutoSlide/blob/ea8b373f878f4a8f071155326fd46f1f3b3f283c/app/src/main/java/com/ltx/service/AutoSlideService.kt) | 간격·궤적 기반 자동 제스처, 부동 제어와 중단 경로 | Apache-2.0 표시. lifecycle 참고 후보이나 취소 후 다음 동작 등 기존 안전 계약과 차이. 영상 반복 판정 대체 아님 |
| [AwayDoomscrollin / AntiScrollService](https://github.com/ResolveCommunity/AwayDoomscrollin/blob/2f31799384718f74e2e14c7058d315b77609e50a/app/src/main/java/com/awaydoomscrollin/app/AntiScrollService.kt) | TikTok 패키지·스크롤 이벤트에 반응하는 사용 제한 앱 | 감지 사건 참고일 뿐 시계/반복 자동 넘김 구현 아님. LICENSE 상세 검토 전 복사 금지 |

EN: The sampled projects provide timer/gesture or scroll-event examples, not a verified drop-in three-host loop-counting engine. No third-party code was copied or executed; license uncertainties remain explicit.

## 11. 공식 근거 / Primary sources

- **S1:** [TikTok — Google Play](https://play.google.com/store/apps/details?hl=en_US&id=com.zhiliaoapp.musically). 일반 글로벌 앱 패키지 후보 확인.
- **S2:** [Samsung 3-way split screen](https://www.samsung.com/ie/support/mobile-devices/multi-tasking-with-apps-using-3-way-split-screen/) 및 [Samsung multi-window 안내](https://www.samsung.com/us/support/answer/ANS10002022/). 첫 문서는 2021년 사례이며 기종 전체 보장으로 사용하지 않는다.
- **S3:** [Android multi-window lifecycle](https://developer.android.com/develop/ui/views/layout/support-multi-window-mode). OS/제조사별 창·활동 생명주기 조건.
- **S4:** [Android audio focus](https://developer.android.com/media/optimize/audio-focus). 다중 소리와 포커스 소유/손실 구분.
- **S5:** [TikTok accessibility for watching videos](https://support.tiktok.com/ja/using-tiktok/exploring-videos/accessibility-for-watching-videos). 공식 일본어 안내에서 피드 버튼·스크린리더 전제 확인; 설치본 제공 여부는 미확인.
- **S6:** [MediaSessionManager.getActiveSessions](https://developer.android.com/reference/android/media/session/MediaSessionManager#getActiveSessions(android.content.ComponentName)). 세션 조회 권한 조건; TikTok 데이터 제공 보장은 아님.
- **S7:** [Android audio playback capture](https://developer.android.com/media/platform/av-capture). 승인·캡처 정책·UID 필터 제약.
- **S8:** [TikTok Display API](https://developers.tiktok.com/docs/en/display-api-overview), [Video Object](https://developers.tiktok.com/docs/en/tiktok-api-v2-video-object?enter_method=left_navigation), [Embed Player](https://developers.tiktok.com/docs/en/embed-player). 메타데이터/임베드와 일반 앱 제어를 구분.
- **S9:** [Android AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService). `getWindows`의 관측 범위와 `dispatchGesture`의 입력 취소 특성.

공식 플랫폼 문서는 가능성·제약 근거이고, TikTok 실제 노드나 현재 폰의 세 앱 성공 근거는 아니다. 앱 UI와 제조사 동작은 버전에 따라 바뀔 수 있으므로 다음 구현 시 설치 버전을 기준으로 다시 확인한다.

## 12. 이번 작업 확인 / Planning-only review record

- 코드 확장 지점·공식 자료·공개 소스 비교: 조사 완료. 실기기 자료는 새로 수집하지 않음.
- 제품 BUILD/TEST/실폰/시각 시험: **N/A — 기획만 수행, 제품 변경 없음**. 기획 검토를 기능 PASS로 표시하지 않음.
- 최초 기획 독립 검토: 코드/검증 경로와 공식 플랫폼/권한을 두 관점에서 검토·재리뷰했다. 시간제 선행 검증 분기, 만료 요청 폐기 문구, 창 좌표/초점 및 권한 설명을 보완했다. 해당 초안의 미해결 P1/P2·기획 차단사항0건이며 제품 동작 PASS가 아니다. §4.1 추가분은 아래 후속 검토에서 별도 확인한다.
- 후속 두 항목 검토: 시간제2–60초/기본3초·확인2초 포함,광고0.0–9.9초/기본미정,저장 보존·입력 큐·원인 가설·다음 릴스 시험·묶음 게시를 독립 검토했다. 미해결 P1/P2·기획 차단사항0건. 문서 정합성9항목·로컬 링크·공백·민감정보 패턴·Markdown 변경 범위 점검 통과. 제품 파일·버전은 그대로이며 제품 시험은 수행하지 않았다.
- 접촉 민감도 추가: §4.2/D-044의 초기화 경로를 코드로 확인하고 원인 확인 후 제한적 보류 방안을 제안했다. 해당 설계의 독립 문서 검토 P1/P2·차단사항0건,문서 요구조건6항목·로컬 링크·공백·민감정보·변경 범위 점검 통과. 후속으로 Instagram의 사용자 재현 보고를 반영해 두 앱 모두 보고된 증상으로 갱신했다. 개발 측 제품 시험·기기/로그 재현은 NOT RUN이며 같은 원인으로 단정하지 않는다.
- 다음 시작점: 다음 작업 재개 시 P0의 TikTok 읽기 전용 관측과 P1-T 설계 확인부터. 지금 사용자에게 설치/권한/테스트를 요청하지 않음.

관련 문서: [현재 분할 화면 계약](SPLIT_SCREEN_PLAN.md), [제품 기준](PRODUCT_SPEC.md), [검증 원장](VERIFICATION.md), [누적이력](CHANGELOG.md), [인수인계](../HANDOVER.md).
