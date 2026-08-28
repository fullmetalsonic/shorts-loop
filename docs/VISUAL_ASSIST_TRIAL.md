# 화면 분석 보조 시험 / Visual-assist trial

대상:0.2.3-visual-test/code10,2026-08-27. **개발 중 시험판이며 해결/20개 연속 성공/공개 배포 판정 전이다.**

## 목적과 영향 범위

화면 캡처 capability를 사용하는 선택적 보조 방식을 시험 APK에 구현한다. 학습 중 추가 재생이 발생할 수 있다. 새 인터넷/마이크/알림 접근 권한, 다른 앱 변경, 동영상 다운로드는 이 시험 범위 밖이다. 당시 배포 검증 목표는20개 연속 자동 이동이었다.

기존 재생 시간 기반 YouTube/Instagram 카운터가 우선이다. 화면 분석은 설정에서 별도로 동의하고 켜야 하며, Android14+의 단일 Instagram 동영상 릴스에서 유효한 시간값이 없을 때만 시도한다. 단순 시간 제한으로 넘기는 방식이 아니다.

## 사용자 흐름과 의미

1. 사용할 앱에서 Instagram을 선택한다. ‘화면 분석 보조 · 시험’은 기본OFF다.
2. 토글ON→안내창→‘확인하고 켜기’. 취소/뒤로가기는OFF를 유지한다. 화면 복원이나 주기적 상태 갱신으로 동의창이 자동 실행되지 않는다.
3. 자동 넘김 실행을 켜고 Instagram 릴스로 돌아간다. 정상 시간값이 있으면 기존 숫자1/1처럼 표시한다.
4. 시간값이 없는 영상에서는 움직이는 짧은 장면 순서를 기준으로 반복 복귀와 간격을 확인한다. `…/1`은 화면 분석 준비/대기이며 완료 회차가 아니다.
5. 학습이 완료된 뒤 새 온전한 화면 주기를 센다. `~1/1`의 물결은 화면 추정이라는 뜻이다. 처음 준비 동안 영상이 추가 반복될 수 있고 영상 안의 반복 장면과 실제 끝을 항상 구분할 수 없으므로 정확한 총N회를 보장하지 않는다.
6. 후보가 불충분하거나 정지·탐색·창 변화·댓글/메뉴·보호된 화면·캡처 실패이면 넘기지 않고 대기/학습을 취소한다. 필요하면 수동으로 다음 영상을 넘기거나 보조 토글을 끈다.

## 데이터·안전 구조

- 창 캡처 API `takeScreenshotOfWindow`만 사용. 전체 디스플레이/다른 앱/녹음으로 우회하지 않는다. 기존 접근성 서비스의 `canTakeScreenshot` 선언을 추가하며 인터넷 권한은 계속 없다.
- 활성창·Instagram·단일영상·광고/댓글 등 구조 검사를 먼저 수행하고, 비동기 캡처 후 같은 창ID/창사각형/영상해시/영상사각형/설정/실행 상태를 재검증한다. 화면 식별 해시는 기기 RAM에서만 사용한다.
- 동시 캡처1개, 세대 토큰·촬영시각(업타임)·지연·중복/역순 검사. 원본은 잠시RAM에 받고 중앙영역을16×24 RGB로 축소한다. 원본Bitmap/HardwareBuffer는 성공·폐기·오류 경로에서 반환하고 특징값도 파일/네트워크에 기록하지 않는다.
- 기존 시간 카운터에 가짜 duration/position을 주입하지 않는다. 화면 추정 전환은 Instagram의 페이지 앞으로 이동 동작을 요청하며 CTA를 누르지 않는다. 별도 요청/확인 숫자를 기록한다.
- 화면 추정 요청 뒤 같은 영상에 재생 막대가 새로 나타나도 성공으로 처리하지 않는다. 비어 있지 않은 다른 영상 식별값으로 실제 페이지 전환을 확인하고, 실패/중단은 기존 안전정지로 처리한다.
- OFF/0회/앱선택해제/설정변경/서비스 종료/사용자 조작/다른 창은 미완료 캡처와 학습을 취소한다. 추정 시간만 흘렀다는 이유로 회차나 요청을 만들지 않는다.

## 검증 계획과 판정

합성 움직임·정지·잡음·부분 정지·간격/설정 변경·1요청 제한, 비동기 stale-frame 정책, 설정기본값/저장손상, 기존148회귀 및 새 연결 정적 검사 → Build/lint → 독립 리뷰 → 최신APK/설치본 해시 대조 → 기기 UI동의/취소/꺼짐/기존 정상시간 우선/문제영상 학습/실제이동 → 손대지 않는20개 연속 시험 순서다.

수동 다음 이동·프로세스 재시작·실패 영상 제외로20개를 채우지 않는다. 광고 이동은 일반 영상N회 성공과 분리한다. 화면 보조 성공은 정확한 시간값 기반 성공과 따로 기록한다.20개 연속이 성공해도 모든 콘텐츠의 정확한N회 보증으로 확대하지 않는다. 실제 결과는 [검증](VERIFICATION.md)·[디버그 대장](DEBUG_LOG.md)에 누적한다.

## English

This opt-in experimental fallback analyzes only clockless Instagram video Reels on Android14+. Existing playback clocks remain preferred. Captures/features stay in RAM and are neither saved nor transmitted. The initial learning phase may add playback cycles; visual repetition is not guaranteed to equal the true video boundary. `…/N` means learning/waiting and `~n/N` means visual estimation. No timer substitutes for observed returns. The release-verification target at this stage was20 consecutive hands-off advances, with ordinary/advertisement/visual results reported separately.

## 관련 근거

- [앞선 실기기·대체 시간원 조사](INSTAGRAM_TIMING_RESEARCH_2026-08-27.md)
- [Android 창 캡처 API](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService)
- [촬영시각·HardwareBuffer](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.ScreenshotResult)
