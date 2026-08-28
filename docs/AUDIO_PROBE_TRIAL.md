# 내부 오디오 수신 시험 / Internal audio capture probe

후속 [음향 반복 후보·0.2.1 진단 시험](AUDIO_PATTERN_TRIAL.md)을 구현·설치했다.48합성시험은통과했지만실제릴스후보검출은미해결이다. 아래0.1 수신 결과는 입력 가능성의 근거이며 반복 인식 성공을 뜻하지 않는다.

## 범위 / Scope

2026-08-27 `audio-probe`를 별도 진단 앱으로 분리했다. 앱 이름은 **쇼츠 오디오 시험**, 버전 `0.1-audio-probe`, 패키지는 `com.fullmetalsonic.shortsloop.audioprobe`다. 기존 ShortsLoop를 교체하지 않고 기존 설정·권한도 변경하지 않는다.

This separate diagnostic app checks whether Instagram playback audio actually reaches Android AudioPlaybackCapture. It does not replace ShortsLoop or implement audio-based auto-advance.

- 같은 Android 프로필의 Instagram UID + `USAGE_MEDIA`만 허용. 다른 프로필 조회·우회 없음.
- `AudioRecord.setAudioPlaybackCaptureConfig`만 사용. 마이크 입력 선택, 화면 프레임 생성, 소리 저장·전송, 음성 문자화, 영상 이동 없음.
- 최대60초, 명시적 시작마다 OS 캡처 승인. 거부/취소 시 수집하지 않음. 수동 중지·OS 승인 종료·화면 잠금·읽기 오류에서도 종료. 자동 재시작 없음.
- PCM16/16kHz/mono 수신 → RMS/최대 진폭/0이 아닌 표본 비율만 계산 → 표본 버퍼 즉시 비움. 수치도 프로세스 RAM에만 유지. 개발자 조회에는 이 집계 수치만 출력.
- Instagram UID는 앱 단위이지 릴스 단위가 아니다. 시험 중 다른 Instagram 영상이나 광고를 재생하면 그 내부 소리도 포함될 수 있다. 시험에는 소리 있는 릴스 한 개를 사용한다.

Only same-profile Instagram media audio is eligible. No microphone source, screen frames, files, network, transcription, or gestures. Samples are cleared after scalar measurements; results exist only in process memory. The UID filter is app-wide, not Reel-specific.

## 권한과 조작 / Permissions and steps

1. `쇼츠 오디오 시험`을 열고 **60초 시험 시작**을 누른다.
2. Android의 오디오 녹음 권한과 캡처 승인창을 사용자가 직접 허용한다. 오디오 전용 API에도 `RECORD_AUDIO`가 필요하므로 OS가 “마이크”로 표시할 수 있으나 실제 마이크를 입력원으로 사용하지 않는다. OS 동의를 ADB로 대신 부여하지 않는다.
3. **Instagram 열기**를 눌러 소리가 있는 릴스를 재생한다. 시험 시간은 서비스 시작부터 흐른다. 권한 승인 자체는 수신 성공 증명이 아니다.
4. **시험 중지**, OS의 캡처 중지, 또는60초 자동 종료 후 이 시험 앱으로 복귀한다. 알림이 OS 설정으로 숨겨지면 최근 앱에서 시험 앱으로 돌아와 중지할 수 있다.
5. 새 시험은 다시 **60초 시험 시작**. 이전 결과가 초기화된다. 프로세스가 종료되면 이전 수치는 복원되지 않는다.

Open the probe, start, personally approve both system dialogs, then play an audible Reel. Return to inspect the scalar result after 60 seconds or stop manually. Every run needs fresh capture consent. If Android hides foreground notifications, return through Recents to stop.

## 수치 해석 / Interpretation

| 항목 | 의미 / Meaning |
|---|---|
| 수신 표본 | AudioRecord가 반환한 PCM 값 개수. 0이면 실제 데이터 미확보 / Number of returned PCM samples |
| 신호 구간 | RMS ≥ -70dBFS인 읽기 구간 수. 3개 이상이면 “신호 감지” 표시 / Blocks above the diagnostic threshold |
| 현재/최대 dBFS | 디지털 최대치 대비 크기. 0에 가까울수록 큼. -120은 표시 하한 / Level relative to digital full scale |
| 0이 아닌 표본 | 정확히0이 아닌 값의 비율 / Nonzero sample percentage |
| 최대 진폭 | PCM16 절댓값 최대0~32768 / Maximum absolute PCM value |

‘신호 감지’는 소리 수신의 증거이지 **음악 반복·영상 종료·정확한N회 성공이 아니다**. 짧은 비트·후렴·무음·정지화면·영상과 독립적인 음악 루프를 분리하려면 이후 별도 알고리즘과 기기 시험이 필요하다. 무음 결과 역시 사용자 음소거/재생 중단/콘텐츠 무음/캡처 정책/선택범위 중 원인을 추가 대조해야 한다.

Signal presence is not a detected loop or video boundary. Silence alone cannot distinguish muted content, paused playback, capture policy, and other restrictions. No audio-repeat accuracy is claimed.

## 검증 / Verification

후보A 구현/빌드/13단위시험/정적 권한·수집범위 검사/독립 코드 리뷰 완료. lint0오류5경고(target35,백업규칙안내,진단아이콘미지정,한국어문구2건). 실제 USB설치/시작화면/권한창 표시 확인. APK56943bytes, SHA256 `0B3760F91D1B74AA60A2793159D281C103D710F0F6F754288E7FE5E7E4001174`; 설치본을 다시 내려받아 일치 확인.

19:04~05 체크포인트: `RECORD_AUDIO` 미허용, `running=false/samples=0`, OS권한창 대기. 실제 PCM 수신·60초 종료·OS중지·잠금·재시작 기기시험은 **미실행**. 전체 자동 넘김20개 연속 조건과 별개이며 이 단계의 새 Public 게시는 없음. 시험 앱의 긴 수치는 세로 스크롤로 확인하며 주요 조작 버튼은 최초 화면에서 보인다. 기존 플로팅이 제목 일부를 가리키지만 버튼은 가리지 않으며 기존 앱 설정을 변경하지 않았다.

Scoped command: `scripts/verify-audio-probe.ps1` (Android SDK35/JDK17+). The existing unconnected main-app VisualSequenceTracker prototype still has2 failing tests; this diagnostic module does not fix or hide them. See [검증 기록](VERIFICATION.md) and [기존 조사](INSTAGRAM_TIMING_RESEARCH_2026-08-27.md).

## 기기 수신 시험 / Device capture test

2026-08-27 20:10~20:15, OS오디오권한·캡처 동의를 기기에서 직접 허용한 뒤 후보A 그대로 시험했다. 코드수정·새APK설치없음. 아래는 해당기기의 해당영상에서 얻은 결과이며 모든 릴스나 YouTube 지원 결과가 아니다.

| 세션/조건 | 실제 관측 | 판정 |
|---|---|---|
| 1 · 최초 승인, 미디어음량0 | 60.080초,955248표본,2824구간,신호0,peak0 | 무음 조건. 캡처 불가능이라고 단정하지 않음 |
| 2 · 새 승인, 초반IG자체음소거 | 45.635초까지728112표본,신호0 | 음량을1로 올리는 것만으로 그 시점까지 신호 미확보 |
| 2 · 일시정지 화면의 음소거 버튼 해제 후 재생 | 56.711초에신호492구간,최대-11.07dBFS,비영0비율18.53%,peak17222 | Instagram 내부 PCM 양성 신호 확인 |
| 2 · 최종 자동 종료 | 60.172초,957600표본,2831구간 중648신호구간,비영0비율22.99%,peak17222 | 수신 성공 및60초 종료 확인 |

Both sessions ended automatically. The service was absent and `MediaProjection=null` after each run. The second run produced nonzero Instagram PCM after unmuting Instagram and resuming playback. This establishes input feasibility, not an audio loop or a video boundary.

- Android 진단에서 시험앱 입력은 `REMOTE_SUBMIX`, `not silenced`; RECORD_AUDIO=true. 첫시도미디어음량0/양성대조미디어음량1, IG자체음소거아이콘은 별도 확인했다. 볼륨0과 IG음소거를 독립적으로 모두 조합한시험은 아니므로 “폰볼륨0이면반드시수신불가”로 확대하지 않는다.
- 2번째세션은 기기에서 직접 새로 시작하고 캡처에 동의한 세션이다. OS동의의 자동 부여나 토큰 재사용은 없었다. 첫시험음량상승은60초종료후라 양성대조로세지않았다.
- 기존앱은계속실행중이었으며일반이동누계5/5→6/6을관측했다. 오디오시험의20개연속또는정확N결과로계산하지않는다. 양성신호구간의영상은새릴스이며,그영상에서마지막기존앱상태는 `재생 정보 없음`/현재0으로확인했다. 이전duration29.632초는새영상의측정값이아니다.
- 시험 후 미디어음량0·IG자체음소거·재생복원. 기존ShortsLoop실행ON/목표1/화면분석OFF 유지. 캡처세션은종료되었으며 오디오권한만사용자허용상태로남는다.
- 남은검증: 재생→일시정지→재생의온전한양성/음성/양성대조,OS수동중지/잠금종료,긴음악의반복주기,후렴오판방지,무음대체경로,다른영상/YouTube,배터리영향. **자동넘김통합이나 반복판별은아직없음**.
- 사생활화면은 `private/device-captures/audio-probe-*`에만보관. 원PCM/음향특징파일없음. [검증](VERIFICATION.md),[D-024진단주의](DEBUG_LOG.md).

## 공식 근거 / Official references

- [AudioPlaybackCapture permissions and policies](https://developer.android.com/media/platform/av-capture)
- [MediaProjection foreground service requirements](https://developer.android.com/develop/background-work/services/fgs/service-types#media-projection)
- [MediaProjection lifecycle and consent](https://developer.android.com/media/grow/media-projection)
