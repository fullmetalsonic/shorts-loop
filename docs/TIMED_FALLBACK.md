# 진행 정보 없는 릴스 시간제 넘김 / Clockless Reels timeout

최종 제품은 **0.2.4/code13**이다. 제품227시험·빌드·lint(0오류/4경고)·설치본동일성검사를통과했고,0회광고1회자동전환과일반·시간제중지를실기기확인했다. 시간제기본10초/5~60초는유지한다. CI·공개다운로드최신상태는 [릴리스 기록](releases/v0.2.4.md),상세시험은 [검증 원장](VERIFICATION.md)을따른다. 아래timed-test A/B는최종광고독립변경전의과거후보다.

Final product: **0.2.4/code13**, 227 product tests and build passed; lint has 0 errors/4 warnings. The installed APK matches, and one ad advanced at zero while normal/timed advancing stayed stopped. See the release record for CI and public-download verification. A/B below are historical candidates.

## 목적과 적용 범위 / Purpose and scope

2026-08-27 초기0.2.4-timed-test부터 기존ShortsLoop에 선택적 타이머를 추가했다. 초기값은15초였고 ＋/－로 조절했다. 진행정보가 없는 Instagram 영상에만 적용하며, 정상 시간값 기반 N회 집계와 YouTube 동작은 유지한다. 별도 오디오시험앱의 기능이 아니며 원인을 해결하거나 영상길이를 알아내는 기능도 아니다.

후속 설정 변경: **기본10초·최대60초**. 후보B(code12)부터 아래계약을적용한다. 최초후보A(code11)의15초/최대120초실측은과거증거로보존하고새10초시험과합치지않는다.

When Instagram does not expose a usable playback clock, an optional timer can advance a recognized single-video Reel after a configured wait. It does not estimate duration or guarantee a full play. YouTube and recognized playback retain their existing repeat-count behavior.

## 동작 계약 / Behavior

- 기본OFF 토글 **시간제 넘김**(진행 정보 없는 영상 카드). Instagram을 선택했을 때만 사용한다. 기존사용자 설정은 유지하고 자동으로켜지지않는다.
- 기본10초,5~60초 정수. 숫자입력 후 완료/적용하거나 ＋/－를 한 번 누를 때1초변경. 20으로설정하면총20초이며10+20초가아니다. 시간은반복횟수와별개다.
- 정상진행시간이있으면기존N회가우선이다. 중간부터실행해서다음시작을기다리는0표시는시간제로대체하지않는다.
- 인식된단일동영상·식별정보·안전한창·일시정지아님·진행정보없음이동시에필요하다. 단순히숫자가0이거나화면아래막대가안보인다는이유만으로켜지지않는다.
- 첫안전관측부터설정초를센다. 같은화면이2초간유지되는확인구간은설정시간에포함된다. 짧은조회공백/시간역행/설정값변경/새영상·창크기변경은다시시작한다. 1.5초초과조회공백은이전누적시간으로즉시넘기지않는다.
- 일시정지·댓글·메뉴·잠금·앱/창변경·플로팅조작·실행OFF·0회·IG선택해제는타이머를취소한다. 안전한시청상태로복귀하면처음부터다시센다. 모든Instagram UI변형을항상감지한다는보장은없다.
- 사진/혼합릴스/정체불명화면은기존대로대기한다. 광고는기존명시적광고판정과광고토글로별도처리하고시간제로우회하지않는다.
- 시간제ON이면기존화면분석보조보다우선한다. 화면분석선택값자체는보존하지만시간제중새캡처는하지않는다. 별도오디오앱/MediaProjection/마이크권한과무관하다.
- 시간이되면인스타릴스pager에다음페이지요청을1번보내고다른영상식별정보가확인되어야성공이다. 같은영상에시간정보만생기는것은넘김성공이아니다. 실패시안전정지하며무한재시도하지않는다.

Timer safety is based on the available accessibility structure, not a hidden playback clock. Pauses, menus and interruptions reset the wait. A missing clock is not proof that the video is short, nor proof of an advertisement.

## 화면·예시 / UI and examples

1. 사용할앱에서Instagram선택.
2. 시간제토글ON,초입력10. ＋를5번누르면15초로저장된다.
3. 전체자동넘김실행ON,현재반복횟수1이상.
4. 정상영상에서는플로팅`1/2`등기존횟수표시. 진행정보없는대상에서는`10초`→`9초`처럼남은시간표시. 이동요청중에는`다음`표시.
5. 예:반복2회/시간제10초이면정상영상은2회,정보없는영상은총10초후이동한다. 시간제10초를2배하지않는다.
6. 토글OFF하면기존수동대기또는사용자가선택한화면분석시험경로로돌아간다. 전체0회/OFF는시간제까지중단한다.

입력오류:빈칸/부호/소수/범위밖은저장하지않고오류표시한다. 원문을잘라다른값으로바꾸지않는다. 입력중인초안은주기적인화면갱신으로덮어쓰지않는다.

## 검증과 배포 기준 / Verification and release gate

후보B 당시 검증: 구현·빌드·설치와 제한된 실기기 시험을 완료했다. B는 일반6개 연속(정상5/시간제1)까지 확인했으며20개 기준은 미완료다. 타이머25시험/설정38시험을 포함한 연결 범위225시험 PASS. 당시전체245개 중 기존 미연결 VisualSequenceTracker의2개 실패는 숨기거나 임의 삭제하지 않았다. 최종제품227시험및명시적실험분리와구분한다.

연속성 검증 목표는 **정상 N회·시간제·광고를 합친20회 연속 자동 넘김**이다. 광고도 의도한 기능의 성공이므로 합계에 포함한다. 수동 이동/중간 실행 재시작은 연속 성공으로 세지 않는다. 종류별 요청·확인 횟수는 남기며, 시간제 성공을 정확한 N회 완주로 바꾸어 설명하지 않는다. 초기 일반영상만20개 기준과 구분한다. 개인정보 화면/원시 로그는 private에만 보관한다.

0.2.4는20회 연속 검증이 미완료인 상태로 공개한 시험판이며 안정성을 보증하지 않는다. 최종0.2.4에는 광고 독립 설정도 추가했다: **메인ON+광고ON이면0회에서도광고넘김**, 시간제는 여전히0회에서중지다. 이 변화는 앞선 A/B의0회전체정지 시험과 다르므로 최종 기기시험을 별도로 기록한다.

### 후보별 증거 / Candidate evidence

- A(code11)는 기본15초/최대120초였던 과거 후보다. 659677bytes, SHA256 `20ED64C8B688AFE033EB72ED17C7BC730B0D1F6ABB50106D9A33FF76C18682F7`. 전체244중242PASS/2FAIL(미연결 실험), 연결224PASS, lint0오류/4경고. 0입력 거부/15→16→15/키보드 완료/토글ON을 검사했다. 21:10~21:15 비조작 일반12개 전환(시간제8/정상4), 광고1 별도, 요청13/확인13. 정상36.988·55.819초 영상에서는 타이머가 비활성화됐다. 기본값 변경에 따라20회 도달 전에 B로 교체했으므로 두 후보의 연속 횟수는 합산하지 않는다.
- B(code12)는 최신10초/최대60초다. 659665bytes, SHA256 `29CEEE11A5F1101692BA3ED7122446C02349F5A94B7D0D75A6481CD4627D07D6`. 빌드/lint0오류4경고/연결225PASS, USB 설치본 해시 일치. 실제60 저장/상한+차단/61입력 거부/10초 복구, 업데이트 후 설정 보존 확인. 정상43.604초 영상에서10초 경과 후에도 기존N계산·timerinactive를 확인했다. 실제 시간제 이동과 연속 횟수의 최종 결과는 [검증 기록](VERIFICATION.md)을 기준으로 한다.
- 독립 코드 리뷰: 신규 미해결 CRITICAL/HIGH/MEDIUM0. 실패 시 `다음` 오표시는 정확한 진행 문구 일치로 고쳤다(D-027). 정적 리뷰와 ADB/Gradle 실행 검증은 별도로 구분한다.
- 원시 기기 숫자 기록은 `private/device-tests/timed-v024-*`, 실제 UI는 `private/device-captures/timed-v024-*`. 공개 문서에 계정/영상 내용을 옮기지 않는다.
