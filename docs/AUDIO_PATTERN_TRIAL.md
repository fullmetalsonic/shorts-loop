# 음향 반복 후보 시험 / Audio pattern trial

## 목적과 범위 / Purpose and scope

2026-08-27 내부 재생음 수신을 확인한 뒤 반복 주기 분석을 별도 **쇼츠 오디오 시험 0.2-audio-pattern**으로 확장했다. 소리의 반복 주기 후보를 계산하며 기존 ShortsLoop 제품은 교체하지 않는다. 기존60초 제한과 Instagram UID + MEDIA 범위, OS 수동 승인 절차를 유지한다.

This diagnostic extension estimates repeating audio periods. It does not identify a video ending, count complete plays, or advance Reels. The main ShortsLoop app remains unchanged.

## 성공 판정과 한계 / What counts as success

- 입력 성공: Instagram 내부 PCM에 유효한 소리가 들어오는 것. [0.1 실기기 결과](AUDIO_PROBE_TRIAL.md#기기-수신-시험--device-capture-test)에서 확인했다.
- 이번 단계: 합성 오디오의 알려진 주기를 찾고, 실제 같은 릴스의 반복과 후보가 일치하는지 대조한다.
- 영상 종료 성공은 별개다. 같은 후렴이 영상 안에서 여러 번 나오면 음향 주기가 영상 길이보다 짧다. 음향 정보만으로 두 경우를 항상 구분할 수 없다.
- 정확한 총 N회와20개 연속 자동 넘김은 아직 이 시험의 결과가 아니다. 실제 자동 넘김을 연결하기 전에 영상 변경·정지·광고·창 상태와의 결합을 별도 검토해야 한다.

Repeated audio can be an internal chorus, not a video loop. A candidate period or high similarity is not a calibrated probability and must not trigger a swipe by itself.

## 처리 범위 / Processing boundaries

1. 사용자 동의 후 같은 프로필의 Instagram 내부 재생음만 수신한다. 마이크 입력, 화면 캡처 프레임, 네트워크 권한을 추가하지 않는다.
2. PCM16/mono/16kHz를 고정 시간 간격의 주파수 특징으로 바꾼다. 수신 함수가 반환하는 배열 길이가 달라도 샘플 수를 기준으로 시간 간격을 맞춘다.
3. 기기 RAM에서 과거 특징 순서와 현재 특징 순서를 비교한다. 약3~25초의 후보를 검사하며, 최소 두 주기와 추가 검증 시간이 필요하다. 따라서60초 시험에서도 시작 지연이나 긴 영상 때문에 후보를 얻지 못할 수 있다.
4. 무음·변화 없는 단일음·3초 미만 반복·전체 순서가 맞지 않는 부분 일치·수신 공백을 검사한다. 모든 음악에 대한 오판 방지가 검증된 것은 아니다.
5. 최대60초 후 원PCM 작업 버퍼와 음향 특징 배열을 비운다. 결과에는 주기·유사도·특징 개수·초기화 이유 등 집계 수치만 남긴다. 원소리나 특징 배열을 파일·로그·외부로 보내지 않는다.

표본 미수신이0.5초 지속되면 이전 후보를 초기화한다. 음소거 때문에0값의 표본이 들어오는 경우는 코어가 별도로 검사한다. 한 번의 분석 호출이150ms를 넘으면 후보를 무효화하고 시험을 중단한다. 이는 처리 완료 후 검사이며 실시간 운영체제 수준의 강제 시간 보장은 아니다.

RAM-only analysis; bounded session and history. Samples are not transcribed or identified against a song database. Aggregate CPU processing time is diagnostic only, not a battery benchmark.

## 실제 시험 순서 / Device procedure

1. 현재 ShortsLoop 실행·목표·화면 분석 설정과 휴대폰 미디어 음량을 확인한다.
2. 같은 릴스가 유지되도록 기존 자동 넘김 실행만 잠시 끈다. 다른 설정은 변경하지 않는다.
3. Instagram에서 일시정지 화면의 자체 음소거 버튼을 확인하고 소리를 켠다. 휴대폰 미디어 음량도 낮은 수준으로 준비한다. **휴대폰 음량과 Instagram 자체 음소거는 별개로 확인한다.**
4. 시험 앱을 열어 **60초 시험 시작**을 누르고 사용자가 OS 캡처를 승인한다. 승인 후 즉시 Instagram으로 돌아가 같은 릴스를 재생한다.
5. 영상·광고·재생 속도를 바꾸거나 탐색하지 않고 관찰한다. 시험 앱은 영상 신원을 읽지 않으므로 다른 릴스로 이동하면 새 시험으로 다시 시작한다.
6. 종료 후 후보가 실제 주기와 일치했는지, 무음이나 공백으로 학습이 초기화됐는지, 정상 종료됐는지 구분한다. 소리가 없었던 시험을 캡처 불가로 단정하지 않는다.
7. 시험을 위해 바꾼 음량·Instagram 음소거·기존 자동 넘김 실행을 원래대로 돌린다.

Keep one audible Reel throughout each session and temporarily disable the existing auto-advancer. Manually approve capture every time. Restore the user's original settings afterwards.

## 화면과 진단 / UI and diagnostics

- **음향 반복 후보 약 X초**: 소리 특징이 반복된 것으로 추정하는 간격이다. 영상 길이나 완주 횟수가 아니다.
- **후보 없음**: 학습 부족, 범위 밖, 무음, 반복 없음 또는 보수적인 조건에서 거부된 상태다. 원인은 아래 진단 상태와 함께 확인한다.
- **패턴 유사도**: 알고리즘 내부 비교 점수다. 성공 확률로 해석하지 않는다.
- **특징 개수**: 현재 RAM 분석에 사용한 시간 구간 개수다. 프레임 개수와 재생 횟수를 혼동하지 않는다.
- **분석 CPU/최장 처리**: 해당 분석 호출들의 CPU 시간 누계와 가장 긴 벽시계 처리 시간이다. OS·전체 앱·오디오 장치 전력 소모는 포함하지 않는다.

개발용 수치 관측:

```powershell
.\scripts\observe-audio-probe.ps1 -Device <USB_SERIAL> -Samples 35 -IntervalMilliseconds 2000 -Name audio-pattern-run-01
```

기존 파일은 덮어쓰지 않는다. 결과는 `private/device-tests`에만 남으며 서비스 종료 후에는 시험 앱의 RAM 결과를 조회한다. 화면·계정·제목·PCM·특징 배열은 이 스크립트의 출력 대상이 아니다.

## 검증 상태 / Verification status

2026-08-27 후보A `0.2-audio-pattern`/code2 구현 및 USB설치 완료. APK65389bytes, SHA256 `67350933EAA04EA4A178CA3B5494086A93089CDCF0FB1236ED7A2A5B09D1C6C1`; 기기에서 다시 받은 설치본과 일치한다.

| 검사 | 결과 | 한계 |
|---|---|---|
| 한정모듈 빌드·시험 컴파일·lint | PASS | lint0오류5경고:target35/backup규칙/진단아이콘/한국어문구2 |
| 직접JUnit | 38 PASS | 새주기25+기존신호13, 합성PCM이며 실제음악 정확도 아님 |
| 정적 범위·연결 검사와 독립 코드리뷰 | PASS | 무수신0.5초초기화/분석지연종료/배열삭제/권한범위 확인, 미해결P1/P2지적0 |
| 설치·버전·설치본해시·시작UI | PASS | 기존제품 교체없음 |
| 첫 실제60초 입력·종료 | 수신 및 종료 확인 | 재생조작이 겹쳐 중간일시정지, 주기정확도 판정제외 |
| 실제 반복주기·영상끝·정확N·20연속 | 미검증/미달 | 제품자동넘김 연결없음 |

새코어 시험은3/6/10/13.73/20/25초 반복,긴주기 범위밖,무음/DC/단일음/고정화음/짧은비트,무관한순서/노이즈/부분후렴,볼륨차이,읽기길이변동,수신공백·시간역행·초기화·60초한도·배열삭제를 포함한다. 실제 노래/말소리에서 보수적인 특징조건 때문에 후보를 놓칠 수 있다.

### 첫 기기 세션 / First device session

20:30~31 새 OS캡처 동의로 시작한 세션. 시작 준비 조작과 화면 전환이 겹쳤고 이후 일시정지 화면 및 오디오 플레이어 paused를 직접 확인했다. 전체60초를 같은소리의 연속재생으로 볼 수 없어 **주기 정확도 대조에서 제외**한다. 이 관측만으로 모든 무음 원인을 단정하지 않는다.

- 최종60.045초/957600표본/2839읽기구간/521신호구간/비영0표본18.36%,최대-11.49dBFS/peak17220.
- 후보0초/특징383/초기화3,`SILENT_OR_NARROW_GAP`. 분석CPU221ms/최장호출7ms는 이 불완전세션의 값이며 배터리수명 측정이 아니다.
- running=false·60초자동종료, 서비스 소멸 및MediaProjection=null 확인. 기존자동넘김은OFF였으며 자동스와이프없음.
- 증거는 `private/device-tests/audio-pattern-v02a-run1.jsonl` 및 비공개화면. `before-consent`/`consent` 이름의 자료는 실제로 OS캡처 동의 **후** 생성되어 파일명으로 동의시점을 판단하면 안 된다.

The first run acquired audio but included pauses, so it cannot validate period accuracy. No repeat candidate was accepted. Capture stopped automatically and the projection was released.

### 두 번째 세션·화면 대조 / Second session and visual control

두 번째 세션도 새 OS캡처 동의 후 시작했으며 관측 중 재생 화면 탭은 없었다. 관측은48.366초 시점과59.987초~종료 구간에 한정되어 초기 전구간 비조작 재생을 직접 증명하지는 않는다. 최종60.162초/958272표본/2804블록/2356신호블록/비영0표본84.29%,최대-10.75dBFS/peak17226. 특징578/초기화1/후보0,`NO_DISTINCT_REPEAT`,분석CPU919ms/최장16ms. 종료 후 서비스없음·Projection=null·IG플레이어started/실제재생화면 확인. 신호 수신은 되지만 이 세션의 최종 주기 후보는 확보하지 못했다.

이후 기존 RAM 화면프로브를 별도60초 실행했다.240프레임/평균움직임24.800,최저오차 후보10.400초(오차10.988/198쌍),다음20.950초(오차16.665/156쌍). 화면 반복의 후보이며 정확한 길이나 오디오 주기가 아니다. 육안 추정은10초 미만이었지만 실제 길이의 계측값은 아니다. **길어서 검출하지 못했다고 단정하지 않는다.** 기존제품은OFF를 확인한 뒤 프로브를 실행했고, 접근성 연결 재생성으로 제품누계가0으로 초기화되었다. 이전6/6과 합산하지 않는다.

증거: `private/device-tests/audio-pattern-v02a-run2-tail.jsonl`, `private/instagram-probe/audio-pattern-v02a-visual-control.txt`, 비공개 실제화면. 화면프로브는 기기/로컬JAR SHA2561B84C87A46364449D2D48F2BDDDD4EE0814B81067798082720B1B715CA27E416 일치. 프로브의 원프레임/PCM/특징 배열은 미저장하고 집계만 남겼다. 별도UI확인용 스크린샷은 private에 보관한다.

### 원인 진단 보강 / Rejection diagnostics

0.2.1-audio-diagnostics는 판정 임계값·3~25초 범위·60초 제한을 그대로 두고 거부 단계 집계만 추가하는 후속 시험판이다. 소리수신 블록과 분석용100ms유효프레임은 다른 값이다. 현재 FFT는 작은소리/좁은대역을 거부하고 최근2초 중19/20유효 및 전체비교95%유효를 요구한다. 정상 말소리의 휴지도 이 조건을 통과하지 못할 가능성이 있으나 위세션에는 해당집계가 없어 직접원인으로 확정할 수 없다.

`audioPatternDiag`는 누적프레임/유효/작은소리/좁은대역 수,최근20프레임 유효수,검색·품질거부·후보없음·후보평가거부·경쟁·짧은반복·수신공백 횟수와 고정 거부코드만 기록한다. PCM/주파수 배열/대화내용/영상식별은 기록하지 않는다. 상태 초기화 이후에도 시험누계를 유지하고 명시clear/시험종료시 비운다. 종료집계만 결과에 복사한다. 이 보강만으로 인식률 개선을 주장하지 않는다.

- `frames/valid/lowRms/narrow`는 중간history초기화 전을 포함한 시험누계다. `recent20Good`는 현재history의 최근최대20개 중 유효수다.
- `searchAttempts`는 검색호출, `assessmentRejected`는 개별후보 평가거부여서 한검색에서 여러번 증가할 수 있다. 서로 다른 단위의 카운터를 합쳐 백분율로 표시하지 않는다.
- `lastReject`는 마지막 개별탈락 기록이다. 같은검색에서 다른후보가 성공해 CANDIDATE가 되어도 과거탈락기록이 남을 수 있어 **현재 유일 실패원인으로 해석하지 않는다.**
- `gapResets`는 표본수신공백 초기화만 센다. 무음/좁은대역2초초기화는 기존 `audioPattern resets`에 포함된다.

### 진단판 설치·실제 관측 / Diagnostic build and actual observation

0.2.1-audio-diagnostics/code3 후보A,APK88989bytes,SHA256 `093881C3C8539762930A5389A764A1776A502E6A324D6D7D96864D8F912AC24F`. USB설치·버전조회·기기설치본pull해시일치. 빌드/시험컴파일/lint PASS(0오류6경고:기존5+진단UI문자열결합), 기존38+진단10=48시험PASS, 범위/연결 정적검사 및독립리뷰PASS. 통합빌드와48시험은 실제 실행한 결과다.

기기에서 새 OS캡처에 직접 동의한 후20:42:04.779~20:43:00.857에1.072~57.169초 관측36표본,20:43:20.637에최종집계조회. 관측 중 탭/스와이프없음. 같은릴스 실제화면/IG플레이어started 확인. 최종60.167초/958272표본/2795블록/2724신호블록/비영0표본97.48%/최대-10.79dBFS/peak17225. 서비스소멸·Projection=null로 종료확인.

| 진단 수치 | 실제 값 | 해석 |
|---|---:|---|
| 전체/유효100ms프레임 | 598 / 572 | 유효프레임95.65%, 신호블록97.46%와다른단위 |
| 작은소리 / 좁은대역 | 14 / 12 | 작은소리14는초기에생긴뒤고정,종료까지반복음소거된것아님 |
| 최근20개유효 | 20 | 최종시점입력품질대기아님 |
| 검색/최근품질거부/거친후보없음 | 119 / 6 / 2 | 대부분후보평가까지진입 |
| 개별후보평가거부 | 776 | 한검색여러후보,776개영상아님 |
| 경쟁/짧은주기/수신공백초기화 | 0 / 0 / 0 | 해당거부단계또는공백리셋으로기록되지않음 |
| 기존history초기화 / 후보 | 0 / 0초 | 무음2초학습초기화없이미검출 |
| 분석CPU / 최장호출 | 1119 / 15ms | 처리지연150ms가드로중단되지않음 |

관측된개별거부기록은 `WHOLE_QUALITY`와 `FLAT_MINIMUM` 등이었다. **이번실패는권한/소리미수신/계속되는초기화가아니라후보선택·검증경로에서발생**했다. 진짜전체주기후보가그단계까지올라왔는지,상위8개거친후보에밀려제외됐는지,실제음향주기가얼마인지는이집계만으로확정할수없다. 임계값을임의로낮추지않았으며인식해결로보고하지않는다.

Evidence stays private: `audio-diagnostics-v021a-run1.jsonl`, `audio-diagnostics-v021a-final.jsonl`. The diagnostic run received usable audio without reset, but no repeating period was accepted. This localizes the failure to candidate selection/validation, not permissions. It does not establish the true video duration or prove which true-period test failed. Main-app integration and the 20-video release gate remain unmet.

시험 후 / After testing:20:46:34~36 **기존실행ON·목표/기준1·광고/플로팅ON·visualOFF·blocked=false**로복원했다. 휴대폰음량0·IG자체음소거아이콘확인후재생복원. 종료후이미다른릴스로이동되어원래시험영상으로되돌리지않았다. 현재도정보없는릴스0표시이며근본수정이아니다. `audio-diagnostics-v021a-restored.jsonl`과실제화면으로확인. 종료UI는후보없음/598특징,스크롤하단572/598유효·마지막탈락과현재판정의구분을육안확인했다. 원PCM/특징은종료시비우고진단집계만RAM에남는다.

기존 제품의 미연결 VisualSequenceTracker2FAIL은 이 시험으로 해결되지 않는다. 이 진단 시험의 새 Public 게시는 없음.

## 조사 근거 / Research references

[Dan Ellis의 음향 지문 설명](https://www.ee.columbia.edu/~dpwe/resources/matlab/fingerprint/)은 주파수 특징과 상대 시간 일치로 소리를 비교하는 원리를 설명한다. 이번 구현은 작은 기기 내 주기 비교 실험이며 해당 코드·곡 데이터베이스나 Shazam을 가져오거나 같은 정확도를 주장하지 않는다.

[Android AudioRecord](https://developer.android.com/reference/android/media/AudioRecord)의 반환 표본 수·읽기 모드·오류를 기준으로 입력을 처리한다. 기기 입력 성공과 반복 알고리즘 정확도는 따로 검증한다.
