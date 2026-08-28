# Instagram 시간원 후속 조사 / Timing-source research

후속 [0.2.3-visual-test 화면 분석 보조](VISUAL_ASSIST_TRIAL.md)의 구현·시험으로 이어졌다. 아래0.2.2/C차 결과는 이전 단계의 증거이며, 새 버전의 성공 결과가 아니다.

## 후속 조사 · 음악·배경음 반복 비교 (18:51 이후)

20:15 후속확인: 기기에서 OS캡처에 직접 동의한 뒤 별도시험앱에서 **Instagram 내부PCM양성수신성공**. IG자체음소거상태0→해제·재생후최대-11.07dBFS/peak17222.60초종료2회확인. [세션수치·원상복원·남은한계](AUDIO_PROBE_TRIAL.md#기기-수신-시험--device-capture-test). 아래미실행/동의대기는과거이력. 이 수신시험 단계에는 반복주기나자동넘김판정이 미구현이었다.

수신 시험은 기존 앱과 별개인 [audio-probe](AUDIO_PROBE_TRIAL.md)로 분리해 구현·검증했다. 초기 시험 준비 시점에는 실제 OS 동의·PCM 수신이 대기 상태였으며, 이후 결과는 위20:15 기록과 구분한다.

소리 반복 비교를 화면 분석과 별개인 보조 후보로 조사했다. **초기 조사 시점에는 오디오 캡처/권한 추가/녹음 시험을 하지 않았다.**

- 실제 설치된 Instagram(targetSdk36)의 패키지 정보에서 `ALLOW_AUDIO_PLAYBACK_CAPTURE` 확인. 최근 해당앱 재생기 기록은 `USAGE_MEDIA`. 이는 앱 설정/최근 재생기 메타데이터의 긍정 근거이지, 실제 PCM 수신이나 모든 영상의 캡처허용 증명은 아니다. 다른 프로필 접근은 거부되었으며 조사/우회하지 않았다.
- 공식 [AudioPlaybackCapture 안내](https://developer.android.com/media/platform/av-capture): RECORD_AUDIO와 사용자 MediaProjection 승인 필요. 재생앱·개별플레이어·시스템 정책 중 가장 제한적인 조건이 적용된다. 특정앱 UID로 범위를 제한할 수 있다.
- [MediaProjection 실행 조건](https://developer.android.com/media/grow/media-projection): 포그라운드서비스 선언/실행 상태 알림과 세션 동의·종료 처리가 필요하다. 접근성 창캡처와 별개인 권한·수동 동의 절차가 필요하다.
- 제안: 마이크 대신 Instagram 내부 재생음만 받아 RAM에서 짧은 음향 특징을 비교. 저장/업로드 없이 먼저 실제 신호 대 무음부터 구분한다. 이후 음악·말·효과음의 긴 순서와 주기를 비교하고 화면/영상 식별 상태와 결합한다.
- 정확도 한계: 같은 비트/후렴이 영상 안에서 여러 번 반복할 수 있고, 무음·소리연속재생·내부짧은루프는 영상경계를 보장하지 않는다. 음성인식으로 말을 문자로 만들거나 노래 제목을 알아내는 것은 필요하지 않다. 화면이 거의고정된 영상에는 영상분석보다 유리할 가능성이 있으나 처리량/배터리/정확도는 미측정이다.
- 초기 조사 범위는 내부재생음 수신 가능성과 별도 시험앱의 권한·동의 절차 검토였다. 제품 마이크/인터넷 권한은 추가하지 않았다.

2026-08-27, ShortsLoop0.2.2/code9의 대체 시간원·반복 감지 조사. 당시 배포 검증 목표는 **20개 이상 실제 연속 자동 진행**이었다. 이 조사 단계에서는 제품 수정·새 권한·앱 설치·외부 게시를 하지 않았다. 화면 반복 후보는 찾았지만 **정확한 N회 반복용 대체 감지기는 미구현이며20개 연속 조건도 미달**이었다.

## 최신 C차 · 이벤트·화면 주기 실험

제품 실행을 OFF하고 기기 조사용 JAR을 USB로 실행했다. APK를 설치하거나 제품 권한을 변경하지 않았다. 화면 분석 프로브는 원본 화면을 일시적으로 RAM에 받아 영상 중앙의16×24 RGB 특징만 RAM에서 비교하고 원본 Bitmap을 해제한다. 화면/특징 배열을 파일에 기록하거나 네트워크로 전송하지 않는다. 별도의 육안 확인 캡처는 기존 진단 절차에 따라 private에만 보관한다. 실제 영상·계정 식별정보는 이 문서에 싣지 않는다.

### 1. 숫자 접근성 이벤트

- [AOSP ProgressBar](https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/refs/heads/main/core/java/android/widget/ProgressBar.java)는 접근성 이벤트에 범위·진행 숫자를 채우는 구현을 갖고 있다. Instagram이 같은 정보를 내는지는 별도 시험이 필요하다.
- [AOSP EventsCommand](https://android.googlesource.com/platform/frameworks/uiautomator/+/34f0bc324a145846e938b7737be0166dbf6b92fd/cmds/uiautomator/src/com/android/commands/uiautomator/EventsCommand.java)의 플랫폼 wrapper를 이용한 [InstagramEventProbe](../scripts/probes/InstagramEventProbe.java)를 작성. 전체 이벤트 종류를 수신하고 IG의 종류·안전한 리소스ID·숫자 필드만 출력한다. 최대45초/출력1500건, 텍스트·설명 출력 없음.
- 문제A가 재생되는18초 동안 이벤트0건. 별도22초 시험에서 수동 일시정지 직후 content-changed9건이 수집되어 수집 경로 자체는 동작함을 확인. itemCount/currentItemIndex/from/to는-1, scroll0으로 재생 시간은 확보하지 못했다. 일시정지 이벤트를 영상 종료 증거로 쓰지 않는다. 앞선 B차의 빈 이벤트 결과보다 수집 경로 검증은 강화됐지만 모든 이벤트 시간원의 부재를 보장하지 않는다.
- 당시 media.player 숫자 필터 조사에서도 쓸 수 있는 플레이어 시계 미확보. 모든 내부 플레이어 정보를 읽었다는 뜻은 아니다.

### 2. 화면 순서의 반복 후보

[PC 초기 도구](../scripts/probes/measure-visual-cycle.ps1)는 A60표본/약45초에서 여러 비슷한 후보가 나왔고 USB 캡처 지연으로 표본 간격도 불규칙했다. 전체 정지 화면12표본에서도 오차0인 여러 후보를 반환했다. 따라서 이 도구의 `CandidateOnly`는 영상 반복 판정이 아니다.

[기기 VisualProbe](../scripts/probes/InstagramVisualProbe.java)와 [VisualCycleMath](../scripts/probes/VisualCycleMath.java)는 실제 표본 시각을 사용하고 전체 평균 움직임이0.8 미만이면 후보를 내지 않는다. 최대90초/360표본, 약250ms 간격,2초 이상/관측 길이÷2.1 이하의 주기를50ms 간격으로 검색한다. 활성 패키지·단일 영상 컨테이너·범위를 확인하지만 **동일 사각형이 동일 콘텐츠를 보장하지는 않는다.** 별도 광고/팝업/부분 정지/탐색/전환 보호나 자동 제스처는 없다.

| 장면/시험 | 실제 표본 | 최선 후보/평균 RGB 오차 | 해석 |
|---|---|---|---|
| 무재생바 A |36초144표본 |10.400초/31.626 |전체 길이/실제 종료점은 미확정 |
| 같은 A 재시험 |32초128표본 |10.400초/29.542 |독립 실행에서도 같은 후보, 정확N회 증거 아님 |
| 다른 무재생바 B |32초128표본 |14.750초/2.005 |Timing2표본에서117노드/Range0, 전체 길이는 미확정 |
| 정상 D |38초152표본 |16.200초/3.918 |별도 Timing에서 실제 max16.205초/current11.556→12.272초와 근접 |
| D 전체 일시정지 |8초32표본 |후보 없음/motion0 |Java 전체 정지 거부 확인. 부분 정지·자막 움직임까지 통과한 것은 아님 |

표본250ms/검색50ms이므로 D의 숫자 차이0.005초를5ms 정확도로 해석하면 안 된다. 기기 샘플 처리/캡처는 대체로 약30~55ms였으나 배터리/발열 벤치마크가 아니다. 중간 C는 화면에 광고가 명시돼 있었고 Range37.781초를 제공했다. 정상 대조/성공 개수에서 제외했으며 재생바 유무로 광고를 분류하면 안 된다는 반례다. A→B→C→D 이동은 수동 진단 이동으로 제품 E2E가 아니다.

### 3. 제품 적용 전 필요한 조건·다른 후보

- Android 공식 [AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService) / [AccessibilityServiceInfo](https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo): 앱의 화면 캡처는 `canTakeScreenshot` capability 선언이 필요하며 일반 캡처는API30+, 창 캡처는API34+다.0.2.2 제품에는 이 capability가 없다. 창별 캡처는 오버레이 혼입을 줄이는 후속 후보이며 shell 프로브와 API 경로가 다르다. **제품 통합에는 별도 capability 선언·선택적 동의 UI·시험 APK 검증이 필요하다.**
- 현재 검색기는 최소약2.1주기를 보고 후보를 내므로 처음 진입한 영상에서N1/N2를 그대로 보장하지 못한다. 주기를 알아도 실제0초 위치를 모를 수 있고 영상 내부 반복 장면은 전체 길이의 절반 같은 가짜 후보를 만든다. 학습 완료 전의 재생을 소급하여 즉시 넘기는 방식도 기존N회 의미와 다르다. 보조 모드의 준비 구간/추가 재생/불확실 상태를 명시한 동작 계약이 필요하다.
- 구현 후보: 기존 유효 시간값 우선 → 무시간원 전용 선택적 화면 학습 → 동일영상/창/충분한 움직임/반복 순서·경계 재확인 → 정지·탐색·팝업·선택해제·OFF·창 전환 시 버퍼 취소 →1요청 후 실제 전환 확인. 정지/반복동작/정지영상+움직이는자막/부분 일시정지/짧고긴영상/수동전환의 오인식 회귀가 필요하다. 아직 구현·배터리 시험하지 않았다.
- 오디오 반복도 조사: 공식 [재생 오디오 캡처](https://developer.android.com/media/platform/av-capture)는 RECORD_AUDIO·MediaProjection 사용자 동의·동일 사용자 프로필·대상 앱의 허용 정책 등을 요구한다. 음악 반복은 영상 끝과 다를 수 있다. 새 권한·오디오 수집·녹음 시험은 하지 않았다. 정보가 없는 콘텐츠를 무조건 초 타이머로 넘기는 방식은 정확N회 요구와 다르므로 도입하지 않았다.

### 4. 기존 앱의20개 연속 기준 실제 시도

프로브 종료 후 일시정지를 풀고 제품 실행ON으로 복원했다. 이후에는 기기를 탭/스와이프하거나 제품을 재시작하지 않고 자체 서비스 진단만 읽었다.

- 18:00:33.791 정상D12.205/16.205초, 중간 시작 대기0.18:00:37.968에서0.037초·현재1로 시작.
- 18:00:53.927에서16.196초,18:00:54.650에서 요청1/확인0,18:00:56.036에서 요청1/확인1.
- 바로 다음 영상은0/1·`재생 정보 없음 · 이 릴스는 수동 넘김 필요`.18:01:04.323까지와18:01:20.276~25.823 후속8표본에서도 같은 상태. 두 관측 창 사이 공백을 연속 표본으로 치환하지 않는다.
- 광고 요청/확인0, 일반 자동 이동1회 성공 후 다음 영상 정체. **20개 연속 기준 실패.** 실패 영상을 수동으로 넘겨 성공 목록에서 제외하지 않았다. 마지막 position0.288/duration16.205는 이전 정상값이므로 새 영상의 재생 상태로 해석하지 않는다.
- 최종 실행ON·기준/현재목표1·두앱선택·광고/플로팅ON·blocked=false 유지, 멈춘 장면 그대로 두었다. 이 조사 단계에서 새로운 제품 빌드/권한/설치/게시는 없다.

### 5. C차 검증과 비공개 증거

- 진단 Java8 컴파일/d8 성공, 구형 API/Java8 안내 경고 남음. [VisualCycleMathTest](../scripts/probes/VisualCycleMathTest.java) 합성3개 PASS(8초주기, 전체정지, 빈특징 거부). 독립 코드/숫자 증거 리뷰 완료. 제품 전체 회귀/20성공/정확N회로 확대하지 않는다.
- README의 통합 재빌드 명령을 실제 실행해 컴파일·합성3시험·dex 재확인. 통합 JAR SHA256 `8F063F61A29E001160EB26DB5C62228356C028FBEF3BB8BE893D0C77B3735F0A`는 로컬 검증용이며 기기에 올리거나 실행하지 않았다. 기기 실측본 해시는 아래 두 개다. PowerShell 구문 검사·문서 로컬 링크59개·git diff 공백 검사 PASS, app 소스 변경 없음 및 private 자료 Git 제외 확인.
- 기기 사용 이벤트v1 SHA256 `540F94862D9DBD31690E4329A4936C90D7F21F88E4D1E2441239F387AA2322A1`, 화면v2 `1B84C87A46364449D2D48F2BDDDD4EE0814B81067798082720B1B715CA27E416`, 각각 로컬/기기 일치. 화면v1은 reportViewIds 누락을 수정하면서 대체했고 기기 시험하지 않았다.
- `private/instagram-probe/d022c-event-a.txt`, `d022c-event-control.txt`: 이벤트 원문 숫자. `d022c-phone-visual-a.txt`, `a2`, `b`, `d` 동일 접두사 파일: 기기 화면 숫자. `d022c-timing-b.txt`, `d022c-timing-c-ad.txt`, `d022c-timing-d.txt`: Range 대조.
- `d022c-phone-visual-paused-summary.txt`는 완전한32행 로그가 아니라 도구 출력에서 전사한 요약이며 그 사실을 명시. `d022c-visual-a-tail.txt`는 PC초기 결과14~60행만 보존했으므로 전체 증거로 사용하지 않음. `d022c-visual-paused.txt`는 PC정지 반례.
- `private/device-tests/d022c-sequence-01.jsonl`45표본 및 `d022c-sequence-stalled.jsonl`8표본: 제품ON 복원/연속 시도. `private/device-captures/d022c-*.png` 육안 대조. 계정/실제 콘텐츠는 공개하지 않음.

## C-phase English summary

Numeric accessibility events provided no usable playback clock in the sampled Reel; a manual pause produced nine events and validated collection. RAM-only visual research found the same10.4-second candidate in two runs, another14.75-second candidate, and a16.2-second candidate close to a normal Reel's16.205-second RangeInfo duration. These are period candidates, not verified playback boundaries or an N-play detector. The Java experiment rejected32 fully static samples; the initial PC experiment did not.

The current search needs about2.1 periods of observation and cannot always distinguish internal repeated motion from a full video loop. Product integration needs screenshot capability, explicit opt-in semantics, safety gates and installation testing. Audio capture was researched but not attempted at this stage. After restoring the unchanged app, one normal video advanced automatically and the next stalled at0/1 without playback information. **The20-consecutive requirement failed. No new product build, installation or public release was made.**

## 이전 B차 · 조사와 실기기 결과

| 후보 | 자료/직접 시험 | 결론 |
|---|---|---|
| 다른 재생 막대/RangeInfo | 문제 릴스의 접근성 트리를 ID에 한정하지 않고 조회 | 보강 프로브4표본, 각114노드에서 RangeInfo0. 단순 ID 변경으로 해결되는 증거 없음 |
| 텍스트·설명·상태 속 시간 | 시:분:초, 분:초, 한국어/영어 분·초 표현 후보를 문자열 내부에서도 탐색 | 같은4표본에서 선택 형식 매치0/state 필드0. 모든 언어·모든 내부 시간원 부재의 증명은 아님 |
| 조작 버튼 표시 | 문제 릴스 중앙을 한 번 탭해 일시정지, 이후 재생 버튼으로 복원 | 초기 프로브3표본, 각119노드에서 RangeInfo0/지정 형식 매치0. 조작 UI를 띄우는 것만으로 시간원 확보 실패 |
| Android MediaSession | 문제 릴스 재생 중 ADB 읽기 전용 서비스 조회 | 당시 세션 목록에 Instagram 없음. 오디오 포커스 목록의 패키지 표시는 미디어 세션/시간값과 다름 |
| 접근성 이벤트 | 7초 제한 기존 UIAutomator events 조회·IG 숫자 필드 필터 | 유효 출력 없음, 종료코드1. 수집 경로/타임아웃 영향 미분리이므로 이벤트 자체 부재로 판정하지 않음 |
| 정상 대조 | 동일 보강 프로브로 다음 일반 릴스 조회 | 4표본에서 RangeInfo1/state1, 현재10.707→13.132초/전체26.1초. 도구가 모든 영상에서 실패한 것은 아님 |

보강 프로브는 각 노드를 refresh하고600노드/깊이48/최대20표본으로 제한한다. 문제/정상 대조 모두 stale0/truncated0이었다. 문제 장면의 알려진 렌더링·문자위치 키 외 추가 데이터 항목3개는 팔로우·상단 탭 TextView에서 발견됐으나 내용 요청은 하지 않았다. extras 값·다른 언어 표현·내부 플레이어를 전수 검사했다고 하지 않는다.

시간 형식 검출은 후보 확인용이며 정확한 재생 시간 판정기가 아니다. 캡션에 포함된 시각도 매치할 수 있으므로 제품 시간값으로 즉시 사용하면 안 된다. 실제 문구·계정·설명·extra 값은 출력하지 않는다. 진단 스크립트가 시스템 원문을 일시적으로 읽는 것과 제품에 저장/전송하는 것을 구분한다.

## 인터넷 자료와 적용 한계

- Android 공식 [AccessibilityNodeInfo](https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo)는 RangeInfo·stateDescription·추가 데이터 조회를 제공하지만, 대상 앱이 제공하지 않은 영상 길이를 자동으로 생성해 주지는 않는다. 이번 프로브는 일반 속성을 조회했으며 추가 데이터 요청은 하지 않았다.
- Android 공식 [MediaSessionManager.getActiveSessions](https://developer.android.com/reference/android/media/session/MediaSessionManager#getActiveSessions(android.content.ComponentName))는 세션 조회에 미디어 제어 권한 또는 활성 알림 리스너를 요구한다. 제품에는 새 알림 접근 권한을 추가하지 않았다. ADB에서 세션을 찾지 못한 현재 표본에 권한만 추가하면 해결된다고 주장할 근거도 없다.
- 공개 Android 앱 [Reeler 설명서](https://github.com/uniaakash/Reeler)는 사용자가 초 단위 이동 간격을 설정하는 방식이다. 이 설명만으로 정확한 재생 종료/반복 횟수 감지 구현이라고 볼 수 없다. 설치·실행·코드 차용하지 않았다.
- 공개 Chrome 확장 [Instagram-Auto-Scroller 소스](https://github.com/sagerkudrick/Instagram-Auto-Scroller/blob/main/content.js)는 웹 video의 loop 속성을 제거하고 ended 이벤트에 다음 이동을 연결한다. 웹 영상 객체에 접근하는 구현이므로 일반 Android Instagram 앱의 접근성 reader에 그대로 적용할 수 없다. 브라우저 시청은 이 제품의 지원 범위가 아니다.
- 제작자가 올린 [Automate Instagram Reels Limiter](https://llamalab.com/automate/community/flows/46089)에도 재생바 없는 릴스에서 동작하지 않을 수 있다는 제한이 명시돼 있다. 제3자 설명은 이번 기기의 직접 시험을 대체하지 않으며, 해당 flow를 설치하거나 실행하지 않았다.

## 남은 선택지 (미구현)

1. **기존 정확도 유지:** 현재처럼 유효한 시간값을 읽는 영상만 N회 집계. 인식 불가 상태를0과 구분해 보여주는 UX 개선은 가능하지만 자동 이동 자체를 해결하지는 않는다.
2. **선택적 시간 제한 보조 모드:** 정상 영상은 기존 N회, 인식 불가 영상만 사용자가 지정한 초 후 이동. 정확한 N회 반복이 아니므로 별도 OFF 기본 토글·초 단위 설정·명확한 상태 표시가 필요하다. 사용자 선택 전 임의로 적용하지 않는다. 광고/댓글/메뉴/앱 미선택/일시정지/창 변화 중에는 타이머를 진행하지 않는 검증도 필요하다.
3. **화면 유사도/음성 반복 추정:** B차에서는 미시험이었으며 이후 C차 화면 실험 결과는 위에 누적했다. 같은 장면의 반복 동작·정지 화면·음악 반복과 실제 영상 끝을 혼동할 수 있으므로 정확한 회수의 대체 수단으로 보장할 수 없다. 제품 화면 분석 도입은 범위·성능·프라이버시 검토가 필요한 별도 변경이다.

## 증거·검증·복원

- 새 개발용 [InstagramTimingProbe.java](../scripts/probes/InstagramTimingProbe.java), [실행 안내](../scripts/probes/README.md). Java8 대상으로 javac 및 SDK35 d8 변환 성공. Java8/구형API 안내 경고 남음; 최종 d8는 필요한 framework lib 지정 후 경고 없음.
- 최종 조사 JAR SHA256: `EE31863EBCC92CBC958FA35889D1C8B6ED76AECDDDB937CDD7E390FD1C23A5B7`, 로컬/기기 일치. APK와 별개이며 제품에 포함하지 않음.
- 비공개 원시 로그: `private/instagram-probe/d022b-tree-a-focused.txt`(초기4표본), `d022b-tree-a-paused.txt`(초기3표본), `d022b-v2-a.txt`(보강 문제 대조4표본), `d022b-v2-b.txt`(보강 정상 대조4표본). 화면은 `private/device-captures/d022b-*`. 실제 영상/계정 화면은 공개하지 않음.
- 제외 자료: `d022b-tree-a.txt`는 최초 도구 출력 절단; `d022b-v2-transition-excluded.txt`는 초기 탐색 중 수동 이동이 겹친 구간. 안정된 동일 콘텐츠 비교에서 제외했다. 이벤트 빈 출력은 판정 불가로 남긴다.
- 프로브 실행은 제품OFF로 분리. 실행기 종료 뒤 제품 서비스가 재생성되어 누계 초기화됨. ON 복원 후 `private/device-tests/d022b-restored.jsonl` 17:41:41.629~44.601의6표본에서10.014→12.848/26.1초 진행. 목표/기준1, 두 앱 선택, 광고/플로팅ON 보존. 현재0의 사유는 중간 재생 시작 대기이며 정보 없음과 다름. 복원 화면 육안 확인.
- 독립 검토에서 초기 조회의 전체문자열 매치·자식 갱신 한계를 지적받아 진단 도구를 보강하고 재시험했다. 제품의 수정 후 회귀/자동 넘김 E2E PASS로 확대하지 않는다.
- 제품 코드/권한/설치 변경 없음. 제품 전체 빌드·JUnit·lint는 재실행하지 않음. 조사 도구 컴파일/실기기 숫자 출력/문서 링크·diff 검사를 적용. 이 조사 단계의 게시는 없음.

## English summary

No reliable replacement playback clock was found for the sampled no-scrubber Reel. A refreshed accessibility-tree survey found no RangeInfo or matching time text in that scene; displaying paused controls did not expose one. The same probe tracked a normal26.1-second Reel. No Instagram MediaSession appeared in the sampled system session list. These observations do not prove that all possible timing sources are absent.

Public examples either use configurable time intervals or browser video-ended events. A separate, opt-in timeout for unrecognized Reels is a possible workaround, not accurate N-play counting. It has not been implemented. Automation and existing settings were restored. No product APK, permission, or release was changed.
