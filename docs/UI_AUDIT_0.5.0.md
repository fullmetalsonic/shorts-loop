# 0.5.0 화면·사용성 검토 / UI and usability audit

## 1. 기존 화면 관측 / Observed baseline

0.4.0의 실제 앱 홈, 앱 선택, Instagram 설정 화면을 같은 Android 에뮬레이터에서 확인했다. 개인 소셜 앱 화면은 사용하거나 공개하지 않았다. 기존 아이콘·짙은 배경·청색 강조·기본 Android 입력 요소를 유지한다.

The baseline audit inspected the actual 0.4.0 home, host selector and Instagram settings on Android. It retains the existing icon, dark palette and native controls without publishing personal social-app captures.

## 2. 문제와 수정 / Findings and changes

| 우선순위 / Priority | 관측 문제 / Finding | 0.5.0 수정 / Change |
| --- | --- | --- |
| 높음 / High | 공통 설명 아래에서 앱 설정 진입점을 찾아야 함 / Host entry was buried under shared explanation | 홈에 세 앱의 설정 버튼과 저장된 상태·횟수를 배치 / Three direct app entries with state and count |
| 높음 / High | 실행 앱 선택과 편집 대상 선택이 분리됨 / Enabled host and edited host were separate choices | 해당 앱 상세에서 사용 토글과 규칙을 함께 제공 / Activation and rules on one host page |
| 보통 / Medium | 긴 스크롤에서 현재 편집 앱을 기억해야 함 / Long scrolling required remembering the host | 고정 앱 이름·홈 복귀, 앱별 초안·스크롤 보존 / Fixed host heading/back and independent retained state |
| 보통 / Medium | 큰 글씨에서 하단 실행 영역이 과도하게 커짐 / Large text made the persistent footer too tall | 1.5배 이상에서 전체 실행/Run all 및 짧은 상태, 전체 접근성 설명은 유지 / Compact visual labels at 1.5×+, full spoken description |

공통 실행·플로팅 표시·여러 앱·권한·업데이트는 홈에 남긴다. 앱별 반복·광고·사진·시간제·긴 영상은 해당 상세에만 표시하고 지원하지 않는 규칙은 노출하지 않는다. 별도 설정 화면 진입 자체는 저장이나 실행 명령이 아니다.

Shared execution, floating visibility, multi-app processing, permissions and updates stay on home. Host rules stay in the corresponding detail. Opening a page never starts execution or silently applies an unfinished number.

## 3. 종합 상태·검증 한계 / Overall health and limits

한국어/영어, 좁은 320dp 및 글씨 1/1.5/2배의 줄 수·배치·입력 보존 회귀검사를 추가했다. 실제 Activity의 홈과 TikTok 상세, 큰 글씨 하단을 육안 확인했다. 큰 글씨에서는 내용 스크롤이 필요하며 고정 제목과 전체 실행은 유지된다. 앱 미설치 에뮬레이터의 사용 토글 비활성은 정상 상태이다.

Automated checks cover Korean/English, 320dp layouts, 1/1.5/2× fonts, numeric drafts and navigation restoration. Actual Activity captures were visually inspected, including the compact large-text footer. Large text still requires scrolling. Disabled host selection is expected when the host is not installed on the emulator.

최종 빌드별 실행 결과는 [검증 원장](VERIFICATION.md)에 기록한다. 이는 Android 앱 화면·로직 검사이며, 접근성 전체 인증이나 실제 TikTok 자동 넘김·사진 가로 이동 성공을 뜻하지 않는다. 실폰 자동 전환은 [현장 점검](FIELD_TEST_0.5.0.md)의 NOT RUN 항목이다. 독립 검토의 미해결 P1/P2는 0건이다.

See the [verification ledger](VERIFICATION.md) for final artifact results. These are app UI/logic checks, not accessibility certification or physical social-app navigation success. Physical checks remain separately listed as NOT RUN. Independent scoped review has no open P1/P2 finding.

[앱별 사용법 / Guide](APP_SETTINGS_0.5.0.md) · [릴리스 / Release](releases/v0.5.0.md)
