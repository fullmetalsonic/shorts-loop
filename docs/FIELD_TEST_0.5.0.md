# 0.5.0 실사용 점검 / Physical field checks

새 APK의 소셜 앱 자동 넘김은 **NOT RUN**이다. 0.4.0에서26개 TikTok 피드를 관측한 것은 구조 조사이며, 새 버전의 자동 성공으로 세지 않는다. 에뮬레이터·합성 시험은 [검증](VERIFICATION.md)에 별도로 기록한다.

New-build social-app automation is **NOT RUN**. The26-page0.4.0 survey establishes structural evidence, not automatic success for this version. Emulator/synthetic results are separate.

## 설정과 확인 / Setup and observations

1. 동일 서명 업데이트를 덮어 설치하고0.5.0을 확인한다. 접근성이 연결되지 않으면 Android 설정에서OFF→ON 후 돌아온다. / Install the same-signed update and verify0.5.0; reconnect accessibility manually if needed.
2. 홈→TikTok에서 해당 앱 사용ON, 반복1, 필요한 광고·사진·시간제 옵션만ON. 공통 실행ON 후 추천 피드를 재생한다. / Enable TikTok, one play and desired rules; start master execution and the recommendation feed.
3. 진행값 영상은 완주 후 자동 전환, 시간제는 적격 무진행 영상에서 설정 시간 후 전환인지 구분한다. 수동 넘김은 성공으로 세지 않는다. / Distinguish loop from timeout transitions; manual moves are not automatic passes.
4. 광고 지연0.0/0.3/1.3/9.9초, 광고OFF 시 일반 재생 또는 사진 규칙, 반복0에서 광고ON을 각각 확인한다. / Check ad delays, adsOFF ordinary/photo behavior and adsON with count0.
5. 사진 전체/한 장 각각3초. 장 번호가 보일 때 정확히 한 장씩, 마지막에서 다음 게시물인지 확인한다. 번호가 안 보이면 선택한 fallback만 동작해야 한다. / Check whole/each3s, exact next slide, final-post advance and explicit unknown-index fallback.
6. 점 표시 광고를 사진 번호로 오인해 가로 입력하지 않는지 확인한다. 광고 웹페이지·프로필·댓글이 열리면 자동 입력이 멈춰야 한다. / Dot ads must not trigger photo gestures; external pages, profiles and comments stop input.
7. 긴 영상은 실제 전체 시계가 있는 표본만 확인한다. 길이를 못 읽는 영상은 이 옵션만으로 넘기지 않아야 한다. / Verify long filtering only with genuine total clocks; unknown duration does not qualify.
8. 전체OFF, 앱 선택OFF, 잠금, PiP, 가림, 수동 탐색, 사진 번호 사라짐, A→B→A 복귀 및 실패 후OFF→ON 복구를 확인한다. / Check stop/selection/lock/PiP/obstruction/manual navigation/index loss/rollback/recovery.
9. YouTube10개·Instagram10개·TikTok10개를 앱별로 기록한 뒤 두 앱/세 앱 분할을 별도로 검사한다. / Record ten transitions per host before separate pair/triple-window tests.

기록 항목: 앱/빌드, 옵션, 유형, 실제 자동 요청/확정 횟수, 실패 상태. 계정·영상 제목·화면·원시 로그·기기 식별자는 공개하지 않는다. 수동으로 넘긴 항목은 제외한다.

Record build/host/options/type and actual requested/confirmed moves. Do not publish accounts, titles, personal screenshots, raw logs or device identifiers. Exclude manually skipped samples.
