# ShortsLoop 0.5.1 · 설정과 남은 시험 / Settings and remaining tests

## 설치·변경 / Install and changes

기존 앱을 삭제하지 않고 같은 서명의0.5.1/code35 APK를 덮어 설치한다. 인앱 업데이트는 Android 설치 확인을 직접 거친다. 접근성 연결이 끊겨 있으면 Android 설정에서 접근성을 껐다 켠 뒤 돌아온다. 기존 앱별 설정과 저장값을 유지하며 일반 사용에 USB 디버깅은 필요 없다.

Install the same-signed0.5.1/code35 over the existing app. Android confirmation is manual. If accessibility disconnects, reconnect it in Android settings. Existing preferences are preserved; normal use requires no USB debugging.

YouTube 설정의 광고 패널은 **준비 상태**다. 스위치는 비활성이며 광고 자동 넘김은 동작하지 않는다. 대기시간0.0–9.9초를0.1초씩 또는 직접 입력하여 저장할 수 있지만, 현재 재생·넘김에는 영향을 주지 않는다. 예:1.3 입력→적용은 준비값 저장만 수행한다. 잘못된 숫자는 저장하지 않는다. 협찬 고지나 일반 영상 광고를 쇼츠 피드 광고로 추측하지 않는다.

The YouTube ad panel is **preparation only**. Its disabled switch cannot skip ads. A0.0–9.9-second delay can be typed/applied or changed in0.1-second steps, but has no playback effect yet. Invalid numbers are not saved. Sponsorship disclosures are not assumed to be feed ads.

기존 반복·사진·시간제·광고·라이브·긴 영상·공통 설정의 상세 조작은 [0.5.0에서 유지된 사용법](APP_SETTINGS_0.5.0.md)을 따른다. 그 문서의 YouTube 광고 '규칙 없음'은0.5.1에서도 **자동 동작 미지원**이라는 의미로 유지되며, 준비 패널만 추가되었다.

Existing controls follow the linked0.5.0 guide. Its lack of a YouTube ad rule still means no automated ad support; only the preparation panel is new.

## 남은 실폰 시험 / Remaining physical tests

아래는 아직 실행하지 않은 시험이며 PC·에뮬레이터 결과로 대체하지 않는다.

- 실제 폰0.5.0→0.5.1 업데이트, 접근성 재연결, 앱별 선택·반복·플로팅 위치 보존.
- YouTube·Instagram·TikTok 각각 자동10개 전환. 수동으로 넘긴 것은 제외하고 일반/광고/시간제/사진/긴 영상 종류를 구분한다.
- TikTok 광고0.0/0.3/1.3/9.9초, 반복0+광고ON, 광고OFF의 적격 일반·사진 동작, 사진 마지막 장과 번호 없는 화면.
- 두 앱/세 앱 분할, 순서·회전·한쪽만 사용, PiP 복귀·잠금·알림·가림·실패 후 전체OFF→ON 복구.
- 실제 총길이를 읽는 긴 영상 필터와 모르는 길이 제외, 장시간 사용·배터리 안정성.
- YouTube 실제 쇼츠 피드 광고와 앞뒤 일반 영상의 구조 조사. **광고 감지 구현 전이므로 YouTube 광고 넘김 성공 시험은 아직 수행할 수 없다.**

Physical update/accessibility/preferences, ten automatic transitions per host, TikTok special-content combinations, multi-window/rotation/interruption recovery and endurance remain unrun. YouTube feed-ad evidence is still required before implementing and testing ad automation. See the [existing detailed field checklist](FIELD_TEST_0.5.0.md) with0.5.1 as the installed version.

[검증·APK](releases/v0.5.1.md) · [광고 준비 근거](YOUTUBE_ADS_PREPARATION.md)
