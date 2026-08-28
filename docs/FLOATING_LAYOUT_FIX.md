# 플로팅 글자 잘림 수정 · 0.2.7/code29 / Compact floating-label fix

로컬 구현·검증·휴대폰 설치 완료. 정식 게시 상태·CI·공개 파일 동일성은 [0.2.7 릴리스 기록](releases/v0.2.7.md)을 따른다.

Implemented, locally verified and installed on the handset. See the0.2.7 release record for stable publication,CI and public-file parity.

## 원인과 변경 / Cause and change

외곽 72×56dp 안에서 글자 영역은 48dp였고, `setSingleLine(true)`가 가로 스크롤을 켰다. Android의 자동 크기 조절이 실제 좁은 폭 대신 큰 가상 폭을 사용해 ‘긴영상’을 줄이지 못했다. 기존 문자열 검사는 실제 레이아웃을 생성하지 않아 이 잘림을 잡지 못했다. [Android TextView 구현](https://android.googlesource.com/platform/frameworks/base/+/main/core/java/android/widget/TextView.java), [자동 크기 조절 문서](https://developer.android.com/develop/ui/views/text-and-emoji/autosizing-textview).

The old 48dp text area used a horizontally scrolling single line, so Android autosizing did not constrain text to the real width. String-only tests never measured the real layout and missed the clipping.

- 외곽 **72×56dp**, 반투명 배경, 위치 저장, X **24×24dp**는 유지한다.
- 글자 뷰는 전체 폭을 쓰고 좌우 6dp·위 24dp·아래 4dp 여백을 둔다. X 아래 공간을 활용해 실제 글자 영역은 60×28dp다.
- 가로 스크롤을 끈 한 줄에서 8–21sp 중 실제 공간에 맞는 가장 큰 크기를 고른다. X도 8–16sp 범위로 맞춘다. 항상 최소 글꼴로 표시하는 것은 아니다.
- ‘긴영상’, ‘라이브’, 횟수·시간 문구를 축약하지 않는다. 상태 설명, 탭·끌기·종료 의미도 유지한다.
- 표시만 `FloatingContent`로 분리한다. 카운트, 감지, 넘김 및 안전정지 로직은 변경하지 않는다.
- 화면/글꼴 설정 변경 콜백에서 dp·sp 크기를 다시 적용하며 리스너·표시·저장 위치를 유지한다.

EN: Keep the 72×56dp outer window, transparency, saved position and 24×24dp close target. Use the full width below the close button, with 6/24/6/4dp padding. One non-scrolling line chooses the largest fitting 8–21sp size; the close symbol uses 8–16sp. Preserve meaningful labels and all interactions. A small presentation class isolates the layout; counting, detection, swipes and safety policies are unchanged. Configuration callbacks refresh dp/sp without replacing listeners or state.

## 실행한 검증 / Executed checks

2026-08-28, 동일 APK: **726467 bytes**, SHA-256 `809CD1EF1287209E23A31896B00FEFF9585511319939FC8113CBC2B1876DAF1A`.

| 항목 / Check | 결과 / Result |
| --- | --- |
| 빌드·기존 제품 JUnit·정적 안전 가드 / Build, product JUnit, static guards | PASS, 468 JUnit |
| Lint | 오류 0, 기존 경고 3 / 0 errors, 3 existing warnings |
| API26 / API33 / API34 실제 Android 계측 / Native instrumentation | PASS: 5568 / 5568 / 5567 검사항목(assertions) |
| 실제 레이아웃 / Real layout | 16개 라벨 순차 변경 × 글꼴 배율 0.85/1/1.3/1.5/2 × LTR/RTL × 기본/굵은 글꼴 |
| 잘림 방지 / Clipping prevention | 한 줄·문자 전체·ellipsis 없음·가로 스크롤 없음·line/ink 경계·X 비겹침·외곽/X 크기 검사 PASS |
| 기존 결함 재현 / Legacy reproduction | 기존 48dp 단일행에서 ‘긴영상’이 폭을 초과함을 실제 Android 측정으로 확인 |
| 터치 영역 / Hit routing | X와 글자 영역 분리, 같은 뷰 상태 변경·metrics 재적용 후 리스너 유지 PASS. 분리된 시험 뷰의 동기식 터치 전달이며 실제 서비스 제스처 시험과 구분 |
| 설치 / Installation | 기존 서명 유지, 설치본 SHA-256 동일, 설치 전후 저장 설정 비교 PASS, 접근성 enabled/bound·런타임 connected·blocked=false |
| 실제 휴대폰 / Physical Android17 | ‘긴영상’ 세 글자와 숫자의 잘림 없음 육안 확인. 탭 1→0→1, 드래그 시 횟수 유지, 위치 저장·원위치 복원, X 종료·플로팅 숨김 확인 |
| 독립 소스 리뷰 / Independent source review | 변경 범위 내 P1/P2 발견 0건 / No P1/P2 found in scope |

기기 시험 중 실제 69초 영상에서 ‘긴영상’ 표시를 캡처했고 이후 긴 영상 요청1/확인1을 관측했다. 이는 플로팅 변경의 짧은 회귀 관측이며 새로운 YouTube10/20회 또는 Instagram10회 연속시험이 아니다. 기존 버전의 연속시험 결과는 해당 버전 기록에 보존한다. 원시 기기 로그와 시청 화면은 비공개 로컬 진단 경로에만 둔다.

During the handset check the complete long-video label was captured on a 69-second video; a subsequent long-video request/confirmation of 1/1 was observed. This is a short overlay regression observation, not a new YouTube10/20 or Instagram10 endurance run. Prior results remain version-specific. Raw logs and viewing screenshots remain private/local.

시험 후 전체 실행 OFF, 반복1, 긴 영상 ON/60초, 광고 ON, 라이브 ON/0초, 시간제 ON/10초, 화면 분석 OFF, 기존 플로팅 위치를 유지했다. 설치 직전 저장된 target0은 시험 중 실행·탭 조작으로1이 됐으므로 시험 후 모든 설정이 설치 직전과 동일하다고 주장하지 않는다.

After testing: execution OFF, repeat1, long-video ON/60s, ads ON, live ON/0s, timer ON/10s, visual OFF and original floating position. The pre-install target0 became1 through the test's enable/tap actions; post-test preferences are not claimed to be byte-for-byte identical to the installation baseline.

## 한계 / Limits

- 실행 중 시스템 글꼴/화면 배율 자체를 변경하는 시험은 미실행이다. 각 배율로 생성한 뷰와 동일 설정에서의 metrics 재적용은 검사했다.
- RTL 영역 경계는 검사했지만 `~1/12` 같은 비대칭 혼합 문자열의 육안 표시 순서는 미검증이다.
- X의 24dp 터치 영역은 작은 플로팅을 유지하기 위한 기존 제약이며, 모든 사용자의 접근성 요구를 충족한다고 보증하지 않는다.
- 모든 제조사 글꼴·임의 배율·내비게이션 오버레이 시나리오를 검증한 것은 아니다.
- GitHub 게시·CI·공개 파일 다운로드 검증은 별도의 릴리스 기록에 누적한다. 메일은 발송하지 않았다.

EN: Live system font/display-setting changes and visual ordering of asymmetric mixed RTL strings were not tested. The existing compact24dp close target remains an accessibility trade-off. This is not coverage of every OEM font,arbitrary scale or navigation overlay. Publication,CI and public-file checks are tracked separately in the release record. No email was sent.
