# 검증 기록 · ShortsLoop

## 현재0.4.0/code33 · PC 검증 / Current PC verification

제품 소스·서명 APK를 고정하여 기존 Public 저장소에 게시하는 단계다. 최종 소스/태그/해시/CI/익명 다운로드는 [0.4.0 릴리스 원장](releases/v0.4.0.md)에 확정한다. 아래 과거 버전의 실제 폰 성공을0.4.0 시험으로 재사용하지 않는다.

- BUILD PASS: release/debug APK, release/debug 단위시험 클래스, Android instrumentation APK, release lint를 컴파일했다.
- Unit PASS: 직접 JUnit4.13.2로 debug/release 각각638개, 실패0. Windows 한글 경로의 Gradle worker 대신 동일 컴파일 클래스를 실행했다. 비제품 VisualSequence 실험20개는 제품과 분리하며 PASS에 포함하지 않는다.
- 정적 안전/권한/모듈/전환/시간제/사진/자기 플로팅 의미 스크롤 분리 검사 PASS. 새 권한·추가 네트워크 대상 없음. 한국어/영어 리소스379쌍·상태108개 PASS(후속 TT 라디오 안내1쌍 추가 후 최종 재검사 예정).
- 후보 `5B5AB6DA39B9F372DC723274DF2685921BE0DBF1C971989823D4AF9954C50EEC`,763158bytes의 API26/33/34 native 검사 **38145/38153/37764 PASS**. 분리된 합성 창/노드·설정·UI·상태·전환 검사 수이며 실제 소셜 앱 전환 수가 아니다. 마지막 TT 라디오 안내 분리 이후 최종 APK로 다시 실행한다.
- 업그레이드 PASS: 공개0.3.0/code32→이전0.4.0 후보 `5771129C1633E14D0CD6E8FAF085429706AE95E7797347DA689453F565431008`를 일회용 API26/33/34에 덮어 설치. 기존44개 자료형/값을 보존(schema1→2 제외),전체53키,서로 다른 앱별 반복·길이·좌표/dual/업데이트 선호/UID/서명 유지,TT 선택OFF·반복2·특수OFF 추가,실행OFF·재실행 비파괴 확인. 이후 변경은 의미 입력 검증·TT 도움말·시험 코드이며 설정 이전 로직은 동일하다.
- Lint: 오류0/경고13(OldTargetApi1,ObsoleteSdkInt1,UnusedResources11). min26/target35를 유지하며 미해결 오류로 숨기지 않는다.
- UI/UX: KO/EN,320dp,글꼴1/1.5/2배의 보존 뷰 검사와 광고/TT 화면 렌더 수행. 앱별 탭·서로 다른 입력초안·저장·무효값·재생성·0.1초 증감 검사 PASS. TT 횟수 입력을 지원 설명보다 먼저 배치하고 지원하지 않는 특수정책·일반 복구 안내를 분리했다. API33 실제 Activity에서 숫자7 표시도 육안 확인했다. 분리된 뷰의 정적 렌더는 EditText 숫자가 비어 보여 숫자 표시 증거로 쓰지 않는다. 큰 글꼴의 영어 하단 제목은 여러 줄로 접히며 화면 스크롤이 필요하다.
- 독립 리뷰: 비율 카운트/새 페이지 확인/3호스트 공정 직렬 입력/설정·UI 범위의 새 미해결P1/P2=0. 리뷰에서 발견한 미디어 키 재사용,누락된 페이지 번호,대기 중 탐색,재연결 중복세션,실패 후 임의 재무장,자기 플로팅 의미 스크롤 오차를 수정하고 자동 회귀검사를 추가했다.
- 개인정보 점검: 공개 대상에서 원시 로그·개인 캡처·계정·기기 식별자·키·토큰·불필요한 내부 경로 제외. 기존 출처 요청/라이선스 상태 유지.
- **NOT RUN:** 새 버전 실폰 설치·YT/IG 영상 회귀·틱톡 자연반복/10연속·세 앱 각10/총30·광고 대기 비교·독립 장시간 배터리. 휴대폰 연결 종료로 [현장 점검표](FIELD_TEST_0.4.0.md)에 구분한다.1.3초는 모든 카운트 초기화 원인을 해결했다는 의미가 아니다.

EN: Candidate build,638 unit tests per variant,static guards,native API26/33/34 checks and typed-preference upgrade pass. Final signed-artifact reruns and public delivery evidence will be recorded in the release ledger. Native synthetic checks are not social-app playback success. Independent scoped review has no open P1/P2. Physical playback/triple-host/endurance and ad-delay comparison remain NOT RUN;earlier0.3.0 results are historical only.

## 이전 체크포인트 / Historical checkpoints

이하 현재·최신·대기·미게시 문구와 수치는 당시 상태이며 위0.4.0 결과 및 릴리스 원장이 우선한다. / Relative status wording below refers to its historical checkpoint.

## 2026-08-29 · 시작 상한1.3초 로컬 시험 / Local1.3-second entry tolerance

- 제품 변경: 일반 LoopCounter의seed 상한1→1.3초,길이10% 제한 유지. 전환 완료 조건/관측 공백/탐색/특수 정책/복구1초 기준은 그대로다.
- 컴파일 PASS: `:app:compileReleaseUnitTestJavaWithJavac`로 변경 제품·시험 소스를 컴파일했다. 새APK assemble/서명/설치는 수행하지 않았다.
- 제품 단위시험 PASS: `verify.ps1 -SkipBuild -BuildType release`의 최신 컴파일 클래스570JUnit(기존564+신규6),실패0.1.015/1.103/1.3 인정,1.3 바로 초과 거부,짧은 영상10%,실제 반복 후1회 입력,반복0,엄격한 복구 유지 등을 검사했다. 소스 정적 가드·다국어 감사PASS.
- 산출물 한계: 위SkipBuild의 끝에 출력된742854-byte/9AA…APK 및 기존 아이콘/릴리스 검사는 과거 공개0.3.0 산출물이다. 이를 변경 소스의BUILD/APK PASS로 재사용하지 않는다. 새APK/설치/실폰 광고 왕복/YouTube 회귀/lint/native 검사는이번 변경에 대해 NOT RUN이며 추가 지시까지 보류한다.
- 독립 소스 리뷰: 범위 내 새P1/P2 차단사항0. 시작 보관/일시 실패 유예 구조는 미구현이고1.3초 밖의 지연·별도 초기화는 여전히 발생할 수 있다.
- 별도 TikTok 화면 관측은[조사 기록](TIKTOK_FEASIBILITY_2026-08-29.md)에 구분한다. 이는 반복/자동 넘김 시험이 아니다. GitHub/메일 없음.

EN: Changed-source compilation and570 product tests/static guards pass for the1.3s ordinary seed cap,retaining10% and existing recovery/safety rules. No new APK,installation,device regression,lint or native run. Skip-build output referencing the old9AA… release APK is not evidence for this changed source. Independent scoped review found no new P1/P2 blocker. TikTok screen observation is separate,not automation validation. Further work is on hold.

## 이전0.3.0/code32 · 최종 기능 검증 / Historical functional verification

**공개 검증 완료:** 제품/태그6bfe330/v0.3.0,CI33170840370SUCCESS(debug/release각564tests·0실패/오류/건너뜀,lint0/12). 최종9AA1E884…AF394AA APK742854bytes는API26/33/34 native28043/28053/27794PASS,휴대폰설치본·익명공개다운로드·GitHubdigest일치. 공개3파일HTTP200·latest/인앱feed·메타데이터PASS. 아래53FD후보와내장revision외모든ZIP항목동일. / Public artifact,installed phone APK,product CI and anonymous assets/update feeds are verified;see release record for full hashes and timing.

공개 고정본의 소스·해시·업그레이드·CI·다운로드 확인은 [0.3.0 릴리스 원장](releases/v0.3.0.md)을 따른다. 아래 후보 해시를 공개 APK 해시로 사용하지 않는다.

업그레이드 시험: 공개0.2.9→53FD 후보 덮어 설치를 일회용 API26/33/34에서 각각 수행해PASS. 기존24개 설정의 값·타입을 보존하고 앱별 초기화19개 추가(합계43),각호스트상속·이후독립수정·재초기화방지·사진IG전용·dual기본OFF·실행OFF·동일UID/서명·업데이트선호보존을 확인했다. 휴대폰 데이터는 초기화하지 않았다. / Upgrade checks pass on26/33/34:24 typed legacy preferences preserved,19 migration keys added,independent host inheritance/reopening,Instagram-only photo settings,default-OFF dual mode and unchanged UID/signer/update preferences.

최신 실폰 후보 **742854bytes**,SHA256`53FDA56552ECC4B2D309BF38AD2E0072301866E813C0E445B7B328F6DA029C2A`의 설치 해시 일치 확인. BUILD·**564JUnit**·KO/EN360쌍/103상태·정적 안전 검사PASS. 동일 APK의 API26/33/34 native **28043/28053/27794 PASS**. lint0오류13경고(OldTargetApi1,ObsoleteSdkInt1,UnusedResources11). 독립 소스 리뷰 P1/P2=0.

전체화면 복귀 시 TYPE3 SystemUI 손잡이를 가림으로 오인한 직접 원인을 확인했다. 비초점·정확한 SystemUI 소유·제한된 기하를 가진 손잡이가 별도 상위 SystemUI 가장자리 바에 완전히 포함될 때만 관측을 허용한다. 실제 입력 경로는 여전히 모든 상위 창과 교차하지 않아야 한다. 미포함·잘못된 소유/유형/초점/레이어·확대된 바·경로 충돌 음성 사례를 시험했다. [D-043](DEBUG_LOG.md).

| 실폰 구간 / Physical segment | 후보 / Candidate | 실제 확인된 추가 전환 / Confirmed delta |
|---|---|---|
| 전체화면 Instagram / Full Instagram | 53FD | IG3/3:일반1·긴1·광고1;YT숨김0 |
| 전체 실행·듀얼ON 유지→IG좌/YT우 / Keep execution and dual ON,split | 53FD | YT4/4:일반3·긴1;IG추가1/1일반 |
| ON 유지→IG 전체화면 복귀 / Keep ON,return to full IG | 53FD | IG추가9/9:일반4·긴4·광고1;YT누적4에서변화0 |
| 회전IG위/YT아래 / Rotated IG above YT | 이전DE7F | YT3/3일반;IG8/8:일반4·긴4 |
| 전체OFF→상하교환→ON / OFF,swap,ON | 이전DE7F | YT추가2/2일반;IG추가3/3일반 |
| 계산기위/YT아래 / Calculator above YT | 이전DE7F | YT1/1일반;숨긴IG추가0,계산기9→12조작 |
| 계산기위/IG아래 / Calculator above IG | 이전F582 | IG6/6:일반3·긴2·시간제1;YT숨김0,계산기19→17조작 |

53FD 무중단 모드전환 구간은21:05:45–21:11:58KST이며 최종 요청/확인 YT4/4·IG13/13,종료pending/blocked/recovery0. 창 전환은 시스템 메뉴·분할 경계로 수행했고 영상 수동 스와이프를 자동 전환으로 세지 않았다. 화면 경계가 바뀌면 이전 카운트를 버리고 새 시작부터 세는 것은 의도된 보호다. 종료 후 전체실행OFF로 설정을 보존했다.

F582→53FD는 제품 코드 변경이므로 이전 후보의 물리시험을 새 APK 전체검증으로 합산하지 않는다. DE7F→F582끼리만 리소스표 외 ZIP항목(두DEX 포함)이 같았다. 원래 회전방향 복귀,최종 후보의 모든 배치/사진 두 모드/동시 손가락 제스처,듀얼OFF 실폰 흐름,모든 일반 앱·키보드·팝업 조합은 **NOT RUN**. 합성/native의 보호 검사와 실제 호스트 성공을 구분한다. 모든 영상·기기의 성공을 보장하지 않는다.

EN: Installed phone candidate53FD…9C2A passes build,564 unit tests,static/localization checks and exact-APK API26/33/34 native28043/28053/27794 checks;lint0 errors/13 warnings and no open P1/P2 in source review. Full→dual→full works without toggling execution or dual mode:IG3,then YT4/IG+1,then IG+9 with hidden YT frozen. Final totals4/4 and13/13 have no pending requests,blocks or recovery. Earlier rotated/calculator segments are retained under their own artifact identities,not added to this candidate's totals. Unrun combinations and original-rotation return remain explicit. Final publication evidence is in the release record.

## 이전0.3.0 후보별 기록 / Historical0.3.0 candidates

이하 ‘최신/현재/진행 중/미게시’는 당시 상태다. 위 현재 검증과 릴리스 원장이 우선한다. / Relative status wording below belongs to its historical checkpoint.

### 최신 후보 · 회전 및 일반 앱 병행 / Latest candidate,rotation and ordinary-app coexistence

최종 설명 보완 후보는 **742366bytes**,SHA256`F582EA28C3A77D78A60BAD6D79C0E5A91010D8C9600773EFFC38599D05BFE017`이며 실폰 설치 APK 해시 일치를 확인했다. BUILD·**561JUnit**·KO/EN360쌍/103상태·정적 안전 검사PASS,lint0오류13경고. 같은 APK의 API26/33/34 native 검사는 각각 **28043/28053/27794 PASS**. 직전 실폰 후보`DE7F2803CD99D428D6AB15C63276C23B394DC3C4F8AF95281F3DD72131E3914C`(742318bytes)와 ZIP 항목을 비교하여 `resources.arsc`만 다르고 두 DEX를 포함한 나머지 항목이 같음을 확인했다. 두 APK를 동일 해시로 보고하지 않는다. 바뀐 내용은 한 대상 앱과 일반 앱을 병행할 때도 듀얼ON이 필요하다는 한·영 안내다.

회전 실측에서 정상 TYPE3 상태표시줄의 고정 높이가 짧아진 상단 창 높이8%를 넘어 창을 차단했다. `edgeBar`의 긴변/창별 두께 상한과 실제 전체 범위 포함 조건을 보완했다. 비초점·유형·가장자리 조건,모든 상위 창과 입력 경로 교차 차단,입력 직전 경계 일치는 유지한다. 경계값·초점·유형·밀린 바·중앙 가림·입력 충돌을 재발방지 시험에 추가했다. 수정 소스 독립 리뷰의 새 P1/P2는0이다. [D-043](DEBUG_LOG.md).

| 실폰 구간 / Device segment | APK | 실제 추가 전환 / Confirmed delta | 범위 / Scope |
|---|---|---|---|
| 회전,IG위/YT아래 / Rotated,IG above YT | DE7F | YT3/3 일반;IG8/8(일반4·긴4) | 20:47:34–20:50,반복1·긴60초,관측 종료pending/차단/복구0 |
| 전체OFF→상하교환→ON / OFF,swap,ON | DE7F | YT2/2 일반;IG3/3 일반 | 20:50:28–20:52,플로팅이 호스트를 따라 이동,종료pending/차단/복구0 |
| 계산기위/YT아래 / Calculator above YT | DE7F | YT1/1 일반;숨긴IG 추가0 | 계산기7+2=9,이어+3=12 조작,비초점 카운트 유지·실제 다음 페이지 확인 |
| 계산기위/IG아래 / Calculator above IG | F582 | 진행 중 / In progress | 긴 영상·시간제 각1회 확인,일반 반복 전환 추가 확인 중;숨긴YT 요청0 |

상하 두 구간의 합계5/11은 중간 실행OFF와 배치 교환이 있는 결과이며 무중단 연속 시험이 아니다. 카운트 증가만으로 PASS를 판정하지 않았고 요청 후 새 페이지 확인 수치를 사용했다. 일반 앱 병행은 계산기에서 확인한 제한된 표본이며 키보드·팝업·복잡한 멀티터치나 모든 다른 앱의 성공을 보장하지 않는다. 듀얼OFF는 활성 창만 처리하고,ON은 한 개만 보이는 대상도 처리한다. 외부 앱이 재생을 멈추면 강제로 재생하지 않으며 기존 키보드/가림 보호도 유지한다.

원래 회전 방향 복귀·최종 후보의 모든 배치/사진 두 모드/동시 손가락 제스처·듀얼OFF 실폰 흐름은 **NOT RUN**이다. 이전 후보의 좌우·좁은 창·개별X 결과는 아래에서 별도 보존한다. GitHub게시·새커밋/태그·메일·OS회전설정 변경은 없다.

EN: The final742366-byte F582 candidate is installed with matching SHA256 and passes build,561 unit tests,static guards and exact-APK native28043/28053/27794 checks on API26/33/34;lint has0 errors/13 warnings. Only the resource table differs from the phone-tested DE7F candidate;both DEX files and all other ZIP entries match. A fixed-height status bar was wrongly rejected after top/bottom rotation;bounded edge-bar geometry and negative regression tests fix that case while all touch-corridor/current-bounds guards remain. Independent source review found no new P1/P2. The table reports separate confirmed segments,not uninterrupted totals. Calculator coexistence does not establish support for every other app or keyboard/overlay interaction. Instagram ordinary-repeat coexistence and return rotation remain pending;no publication or email.

### 이전818AC 후보 · 좁은 창 재시험 / Previous narrow-pane candidate

최신742334-byte 후보SHA256`818AC00DECEE2C9C42AD4C668F4622561E030E2B2D44BAD221AB54707789E1A4`를실폰에설치하고설치APK해시일치를확인했다. BUILD/559JUnit/정적가드PASS,동일APK API26/33/34 native**28033/28043/27788PASS**. 추가native26검사는hidden setter대신공개Parcelable로분리된시험창을구성하고값왕복을검증하며,제품·기기권한을변경하지않는다. lint0오류13경고는유지한다.

좁은Instagram창에서손잡이의고정폭이창너비25%를넘어확인중단되는현상을재현했다. 창의긴변과각변의상한·중심·가로형태조건으로관측규칙을보완했고,실제입력경로교차및요청직전창경계동일성검사는그대로유지했다. 최신후보의20:37:09–20:38후반무조작시험은YouTube**1/1**,Instagram**8/8**(일반1·긴영상6·광고1),종료pending0·차단0·ordinary recovery0. 이전후보와합산하지않는다.

직전517827후보는첫배치5/5씩,좌우교체후추가YouTube5/5·Instagram7/7로누적10/12를확인했다. 이후Instagram플로팅X로해당앱만OFF,YouTube카운트세대유지·추가자동전환1회,Instagram추가요청0을확인했다. 이결과는두구간의검사이며한번도중지하지않은10연속시험이아니다. 그후좁은창실패를수정했으므로좌우교체/X검사는이전후보근거로구분한다.

현재전체실행OFF·접근성연결유지·설정보존상태로사용자의90도회전/상하분할준비를기다린다. 회전·상하순서교체·원래방향복귀실폰검사는**NOT RUN**이다. OS회전설정변경·GitHub게시·메일없음. 아래내용은직전후보검증기록이다.

EN: Latest818AC00D…89E1A4,742334bytes,matches the installed phone APK. Build,559 unit tests/static guards and exact-APK native28033/28043/27788 on26/33/34 pass. Narrow-pane physical retest confirmed1/1 YouTube and8/8 Instagram transitions(1 ordinary,6 long,1 ad),with no blocks or recovery. The preceding candidate's two-order totals10/12 and isolated-X test remain separate evidence. Overall execution isOFF while awaiting user-prepared90-degree/top-bottom testing;rotation/order reversal/return remainNOT RUN. No publication,email or OS rotation-setting change.

### 직전517827 후보 기록 / Previous candidate evidence

공개 최신은0.2.9이며 아래0.3.0은 미커밋 소스의 로컬 검증 후보다. 이전 후보의 오류와 결과를 최종 후보 PASS로 합산하지 않는다. [설계·계약](SPLIT_SCREEN_PLAN.md),[원인·재발방지D-043](DEBUG_LOG.md).

- 최신 후보:742174bytes,SHA256`5178270548FE2F18DED435C275D3735119475F90E63DD3C2EAF197709EB72199`. 실폰에서 설치 APK를 다시 받아 같은 SHA256 확인. non-debuggable release,기존 단일 서명 유지,접근성 재연결과 기존 설정 보존 확인.
- BUILD PASS,제품557JUnit PASS,KO/EN360리소스쌍·103상태 및 정적 안전 배선 검사 PASS. lint0오류13경고(기존 OS 관련2개,UI 재배치 후 미사용 리소스11개). 미사용 문구는 이번 변경에서 임의 삭제하지 않았다.
- 동일 APK API34 native27762 PASS. 이어 추가한 최종 창 경계 합성 시험은 테스트 fixture의 hidden setter 접근 제한으로 중단됐으며 제품 크래시가 아니다. 이 추가 시험과 API26/33 최종 재실행은 진행 중이다. 이전969E 후보의27999/28009/27754 결과를 최신 후보에 대입하지 않는다.
- 실폰 1차 무조작 구간:20:23:55–20:26:12,Instagram왼쪽/YouTube오른쪽,듀얼ON·양쪽반복1·긴영상ON60초. YouTube요청5/확인5(일반4·긴영상1),Instagram5/5(일반3·긴영상2),각pending0·blocked=false·ordinary recovery0. 수동 스와이프와 강제 초점 전환 없이 양쪽 진행값/카운트/페이지 변경을 함께 확인했다.
- 위치 교체:전체실행OFF→시스템좌우교체→ON으로YouTube왼쪽/Instagram오른쪽. 각 창의 감지와 플로팅이 앱을 따라가는 것을 확인했고 반대 순서의 자동 전환 검사는 계속 진행 중이다. 서로 다른 구간을10연속 시험으로 합산하지 않는다.
- 초기 실패:분할 경계 손잡이,지역화된 자기 플로팅 제목,API36 상단 창 제어 손잡이를 가림 창으로 오인했다. 메타데이터 관측으로 원인을 나누어 수정했으며,관측 허용과 실제 입력 경로 검사를 분리했다. 현재 창 경계가 요청 직전 경계와 다르면 입력을 거부한다. 재표시된 플로팅의 빈 상태 문구도 캐시 복원으로 수정했다.
- 듀얼OFF의실폰전환,상하분할,드래그/비율변경,개별X,두앱동시요청충돌의실제발생,사진두모드의듀얼실기기표본은 아직미검증이다. 관련합성/native검사는실폰PASS를대신하지않는다. GitHub커밋/푸시/태그/Release/메일없음.

EN: Public latest remains0.2.9. Local0.3.0 candidate51782705…B72199 is742174bytes and matches the installed phone APK. Build,557 unit tests,localization and static guards pass;lint has0 errors/13 warnings. API34 native27762 passed before an additional detached-window fixture encountered a hidden-setter restriction;that fixture and final26/33 runs are still being checked. The first untouched split run confirmed5/5 YouTube transitions(4 ordinary,1 long) and5/5 Instagram transitions(3 ordinary,2 long),with no manual swipes,blocks or recovery. Position-swap testing continues. These observations are not a10-consecutive or all-content guarantee. No publication or email.

## 현재0.2.9/code31 / Current verification

**공개 완료:** 제품 태그/커밋`v0.2.9`/`eb4bd0c`, [CI33163186891](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33163186891) SUCCESS. 최종 APK721898bytes·SHA256`5EEB5B8B090B090EB0313CE10CC71102ADF3EDA5728EDE4F01FA0937D831E68D`. 공개3파일 익명 다운로드 크기·SHA256·GitHub digest 일치/HTTP200/업데이트조회API PASS. 최종 APK의522JUnit·세OS native25335/25349/25132 PASS. 실폰 시험후보1EF와 실행 코드·Manifest·리소스·자산은 같고 내장 소스 revision만 다르다. 실폰에는1EF 후보를 유지했고 전체파일해시일치를 주장하지 않는다. [최종 릴리스·시각·상세근거](releases/v0.2.9.md). 아래 보류 문구는 배포 전 체크포인트다.

EN: Published0.2.9/source eb4bd0c;product CI and anonymous3-asset parity/HTTP/update-feed checks pass. Final APK SHA2565EEB5B8B…31E68D,721898bytes;522JUnit and final three-OS native checks pass. The phone retains the physically tested1EF candidate with identical runtime payload and different embedded revision/whole-file hash. See the release record;hold statements below are historical checkpoints.

### 배포 전 사진 릴스 검증 · 2026-08-28 / Pre-publication photo validation

두 모드의 기본값은 모두3초, 범위0–10초다. 사진옵션 기본OFF, 장 번호 불가 대체 기본OFF이며 일반 반복0회와 독립이다. [사진 계약](PHOTO_REELS.md), [재발방지D-042](DEBUG_LOG.md#d-042--사진-장-번호-조회정상-화면-오인--photo-index-tree-and-false-interaction-guard).

- 최신 PC 검증 후보: non-debuggable0.2.9/code31, **721898 bytes**, SHA256 **`1EF2434F633D119F0DC0CA9356BB7A823120CC8960FB61C15D1D3E1DDE505961`**. 로컬 미커밋 소스의 검증본이며 배포 고정본은 아니다.
- BUILD PASS, 제품 **522 JUnit PASS**, KO/EN334개 리소스쌍·103개 상태코드 및 기존/신규 정적 가드 PASS. lint0오류·기존2경고(OldTargetApi,ObsoleteSdkInt).
- 같은 최신 APK의 API26/33/34 native 검사는 각각 **25335 / 25349 / 25132 PASS**. 두 언어·320dp·글꼴1/1.5/2·입력/복원·사진 두 모드/경계/미확인 정지·source-node A→B→A·광고 메타데이터 보존을 포함한다. 합성 노드 시험을 실제 Instagram 시험으로 세지 않는다.
- 실폰 **앞선 후보** SHA256`C50B4DCCCBD4218DC1A4F8D1EAAD14C830F6C52F06E8DCB9D77F36A1A2C02AB9`(721982bytes): 사진1/2→2/2→다음 일반 릴스 **PASS**. 한 장 모드/각3초/반복1/대체OFF, 관측11.36초, 가로요청1/확인1·세로요청1/확인1·차단0. 실행 구간에는 수동 스와이프 없이 앱이 요청했으며 화면과 카운터를 대조했다. 시험 전후 수동 복귀는 성공 횟수에 넣지 않았다.
- 초기 후보의 장 번호 누락과 확장 트리의 빈 외곽 오인은 D-042로 수정했다. 이후 노드 키 원본 복귀·광고 메타데이터 보강을 포함한 **최신721898-byte 후보의 USB 설치 및 설치 APK SHA256 일치 PASS**. 기존 접근성 연결과 설정을 보존했고 권한을 ADB로 대신 부여하지 않았다.
- 2026-08-28 재연결 실폰 시험: 같은 두 장 사진 게시물에서 아래 여섯 조건의 세로 요청6/확인6, 가로 요청2/확인2, 차단0을 확인했다. 댓글창8초 대기는 새 요청0이었다. 시험별 수동 복귀와 설정 변경은 자동 성공 횟수에서 제외한다. 서로 다른 사진 게시물6개나 연속6개 시험을 뜻하지 않는다.
- 장 번호 누락 대체ON/OFF의 실제 대상, 사진→광고, 혼합 게시물 및 새 YouTube·Instagram10/20연속 시험은 **실폰 NOT RUN**. 아래 추가 신속 검사에서 한 장10초와 대체ON 상태의 댓글 보호를 확인했지만, 실제 번호 누락 분기의 PASS는 아니다. 페이지 노드 재사용이나 노드 정보 없는 광고 끝 화면에서는 보수적으로 정지할 수 있다.
- 변경 범위 독립 소스 재리뷰 완료, 확인된 P1/P2 미해결0. 제안→반례 검토→보강→재시험을 기록했으며 전체 기기/호스트 동작 보증은 아니다. GitHub 커밋·푸시·태그·Release·새 CI·공개 다운로드 검증은 보류, 메일 미발송.
- 산출물의 기존 단일 서명 SHA256`3604d3a1cc1f4e8f772d718cf8b9cba5adfd3650708cf169660b653e28b69632`와 package/code31/min26/target35/non-debuggable을 확인했다. 공개 후보240개 파일의 민감 패턴0건·문서 링크406개·diff 공백검사PASS. 이는 공개 전 소스 커밋·내장revision·실제 내려받은 파일 검증을 대신하지 않는다.

EN: Latest local candidate:721898bytes,SHA2561EF2434F…505961;build,522 JUnit,334 resource pairs/103 statuses and static guards pass,with0 lint errors/2 existing warnings. Exact-APK API26/33/34 native checks pass25335/25349/25132. The latest APK is installed with matching SHA256. Six initial physical scenarios on one two-photo post passed6/6 vertical and2/2 horizontal,with0 blocks;comments stayed protected for8s. Additional checks below cover each10s and fallbackON comment protection,not an actual unreadable-index branch. Unreadable-index,photo-to-ad,mixed media and fresh host endurance remain physically unverified. Earlier C50 results remain separate. Independent source re-review found no unresolved P1/P2. Publication and email remain unperformed.

#### 최신 APK 실폰 조건별 결과 / Latest-APK physical cases

| 조건 / Scenario | 실제 반복값 / Target | 결과 / Result | 관측 시간 / Observation |
|---|---:|---|---:|
| 통째3초 / Whole3s | 1 | 세로1/1, 가로0 / Vertical1/1,horizontal0 | 6.15s |
| 한 장3초,1/2→2/2→다음 / Each3s,last-slide exit | 1 | 가로1/1·세로1/1 / Horizontal1/1,vertical1/1 | 11.59s |
| 통째0초 / Whole0s | 0 | 세로1/1, 일반0회와 독립 / Vertical1/1,independent of repeat0 | 3.33s |
| 통째10초 / Whole10s | 1 | 세로1/1,10초 대기 후 요청 / Vertical1/1,request after10s wait | 13.88s |
| 댓글 닫기 후 통째3초 / Close comments,whole3s | 1 | 닫은 뒤 새 대기→세로1/1 / Fresh wait then vertical1/1 | 5.55s |
| 한 장0초,1/2→2/2→다음 / Each0s,last-slide exit | 0 | 가로1/1·세로1/1 / Horizontal1/1,vertical1/1 | 5.23s |

관측 시간은 앱 복귀/안전한 화면 안정화/전환 확인/PC 표본 수집을 포함하므로 설정한 보기 시간과 다르다. 0초에도 안전한 화면 확인과 이동 완료 확인은 유지한다. 통째10초 시험의 실제 반복값은1이며, 시험 폴더명에 포함된target0과 달라 런타임 값을 기준으로 기록했다. 댓글창에서는8초 동안 `screen.interaction`·요청4/확인4를 유지했다. 재실행 후 열린 댓글창의 대기를 다시 확인하고, 실행을 유지한 채 댓글만 닫아3초 새 대기와 다음 이동을 확인했다. 통째 입력의−는0에서, +는10에서 더 진행되지 않으며 다른 시간3초를 보존했다. 이 검사는 숫자 직접입력·미저장 복원 실폰 시험을 대신하지 않는다.

종료 설정: 사진ON/통째 모드·두 시간3초·대체OFF·반복1, 기존 시간제ON/5초·긴 영상ON/60초·광고ON·라이브ON/0초·화면분석OFF. 조건별 시험은 매번X로 중지했다. 이후 기본 설정으로 실행을 다시 켜 정상 후속 이동을 확인했으며 마지막 확인 상태는 실행ON·blocked=false다. 후속 누적 이동은 위6개 조건의 결과에 합산하지 않는다. 기기 화면과 숫자 런타임을 함께 대조했으며 사진·계정·댓글 원문은 비공개로 보존한다. 신규 코드 변경이 없어 동일 APK의 완료된 PC 전체 시험을 중복 실행하지 않았다.

EN: Elapsed observations include app return,settlement,transition confirmation and PC sampling,not only the configured delay. Zero retains safety/confirmation checks. Whole10s actually ran with target1 despite its private folder name. Comments stayed protected for8 seconds;closing them while execution remainedON started a fresh3-second wait and confirmed the next Reel. Whole-delay buttons bounded values at0 and10 without changing the other3-second delay;this does not claim physical typed-input coverage. Restored whole3s/each3s,photoON,fallbackOFF,target1 and other existing preferences. Individual cases were stopped withX;execution was later turnedON for a successful normal follow-up and remainedON/unblocked at the last check. Follow-up movements are excluded from the six scenarios. No source change or repeated full PC suite;raw handset content remains private.

#### 추가 신속 검사 · 2026-08-28 / Additional rapid checks

같은 최신 APK에서 사진 두 시간을0초로 낮추고 한 장 모드·대체ON으로 검사했다. 안전한 화면 안정화와 전환 확인 시간은 줄이지 않았다.

| 조건 / Scenario | 실제 결과 / Observed result |
|---|---|
| 댓글창 열림·한 장0초·대체ON / Comments,each0s,fallbackON | 약6초 동안 screen.interaction, 새 가로/세로 요청0 / No new request |
| 8장 사진1/8→8/8→다음 릴스 / Eight-photo post,each0s | 가로7/7·세로1/1,18.84초 관측,차단0 / Horizontal7/7,vertical1/1,no block |
| 한 장10초·7/8→8/8→다음 / Each10s,penultimate/last | 가로1/1·세로1/1,26.35초 관측,차단0 / Horizontal1/1,vertical1/1,no block |

한 장10초에서는1.24초에 사진 대기,11.72초에 가로 요청,13.61초에 다음 장 확인/새 대기,23.87초에 세로 요청,26.35초에 다음 릴스 확인을 관측했다. 전체 관측 시간에는 앱 복귀·안정화·확인·PC 표본 수집이 포함된다. 두 사진 시험 구간에는 수동 스와이프가 없고, 시험 전 수동 위치 복귀는 제외한다. 최초10초 준비 시 목표 사진으로 복귀하지 못한 실행은 해당 조건 PASS에서 제외하고 올바른7/8 시작 화면으로 다시 시험했다.

후속 신속 탐색은20개 화면 표본이다. 일반 재생/긴 영상/시간제/광고 인식이 확인되면 다음 대상으로 이동했으며 수동 탐색을 포함한다. 이것은20개 자동 연속 이동이나20개 예외 사례 PASS가 아니다. 실제 번호 없는 사진·사진 바로 다음 광고·사진/영상 혼합을 확보하지 못했다. 일반 광고 넘김이 관측된 것을 사진→광고 전환 검증으로 대체하지 않는다. 두 사진 시험 종료 직후 시작된 긴 영상 요청2건은 검사 도구의 명시적OFF로 중단됐으므로 누적 requests-confirmed 차이를 제품 전환 실패2건으로 계산하지 않는다.

최종 복원 확인: 실행ON,사진ON/통째/각3초/대체OFF,반복1,기존 시간제ON/5초·긴영상ON/60초·광고ON·라이브ON/0초·화면분석OFF,blocked=false. 사진 모드 변경으로 비활성화되는 대체 스위치는 한 장 모드에서OFF한 후 통째 모드로 복원했다. 원본 화면·런타임은 비공개 보존하고 문서에는 기술 결과만 남긴다. 제품 코드·APK를 바꾸지 않았으므로 완료된 동일 산출물 PC 전체 시험은 반복하지 않았다. 새 GitHub 게시나 메일 발송은 없다.

EN: Rapid checks retained safety timing while setting photo delays to0. Comments with fallbackON issued no request for about6s. An8-photo post completed7/7 horizontal and1/1 vertical moves in18.84s. Each10s from slide7/8 completed1/1 horizontal and1/1 vertical in26.35s,with fresh10-second waits on both slides. No manual swipe occurred inside these two cases. An initial wrong-page setup was excluded and rerun on the verified photo. The20-sample search included manual advances after recognition and is not20 consecutive automatic successes. Actual unreadable-index,photo-to-ad and mixed cases were not found. Two subsequent long requests were intentionally interrupted by the test stop and are not product failures. Restored executionON,photo whole3s/each3s/fallbackOFF,target1 and all prior unrelated options;unblocked. No product changes,rebuild,publication or email.

### 이전 언어 전용 후보 / Earlier localization-only candidate

언어 리소스·오류코드·동작 불변·숫자입력·좁은 창/큰 글꼴·언어전환·0.2.8덮어 설치 설정 보존을 검증한다. 최종 수치·해시·CI·공개 다운로드는 [0.2.9 릴리스 기록](releases/v0.2.9.md),계약은 [언어 안내](LOCALIZATION.md)를 따른다. 휴대폰/실제호스트 연속시험은NOT RUN이다. 아래수치는각과거버전근거로보존하며새시험으로합산하지않는다.

EN: Current scope covers localization,neutral errors,preserved decisions,inputs,narrow/large-text layouts,locale changes,and0.2.8 upgrade retention. Final counts/artifact/CI/public downloads are in the release record. Phone/host endurance NOT RUN;historical numbers below are not new-version evidence.

## 이전0.2.8/code30 / Historical verification

제품CI33144962247 SUCCESS:debug/release각32suites·468tests·실패/오류/건너뜀0,lint각0오류/2경고. Public v0.2.8 게시 후 익명3파일 크기·SHA256·GitHub digest·조회API·HTTP200 확인 PASS. 최종 설치 APK와 공개 APK의 SHA256은 `FAA554B16AD5A374A07057FDF2F2195931F77AE77ACC67E2E154A366108F012C`로 동일하다.<br>
Product CI passed both variants;anonymous public-file and installed-artifact parity passed. Exact source/tag identities and timing are in the release record.

배포 release 빌드·468JUnit·정적 가드 PASS, lint0오류/기존3경고. API26/33/34 같은 배포 APK의 실제 계측5572/5572/5571항목 PASS. 세 OS에서 이전0.2.7/code29→0.2.8/code30 덮어 설치,19개 설정의 타입·값 전체·UID·서명 보존, 실행OFF·디버깅OFF를 확인했다. 첫 upgrade fixture는 별도 test 패키지의 저장소에 쓰려다 UID 차이로 baseline저장이 실패했다. 제품 문제가 아니며 테스트코드만 대상 앱의 fixture전용 저장소를 사용하도록 고친 후 세 OS 모두 재시험했다.

휴대폰은 release 설치·두 버전 라벨/펼친 사용법/실험 경고 육안 확인·접근성 바인딩·런타임13개 비교·플로팅 위치/탭1→0→1/X종료·설치 해시 일치 PASS. private설정 전체를 읽지 않았고 `run-as`는 의도대로 거절된다. 현재 실행OFF. 독립 변경 리뷰 P1/P2 발견0건이며 전체 서명집합·실제 unsigned출력 선택도 보강했다. 감지/카운트/넘김 코드 변경은 없고 새 호스트10/20연속·설치화면E2E는 미실행이다.

EN: Release build,468 product tests,static guards and native5572/5572/5571 checks passed. All three emulator OS versions verified all19 typed settings plus UID/signer and OFF states across code29→30. An initial test-storage UID mistake was corrected in the test harness only. Phone evidence covers actual labels/help/disclosures,accessibility,13 exposed settings,overlay interactions and hash parity—not a full private-preference comparison. No new social-app endurance or installer-UI E2E run is claimed.

최종 파일·소스커밋·CI·공개검증은 [0.2.8 릴리스 기록](releases/v0.2.8.md), 반복명령은 [배포 빌드 절차](RELEASE_BUILD.md), 원인은 [감사](RELEASE_PRESENTATION_AUDIT.md)와 [D-037/D-038](DEBUG_LOG.md)를 따른다. 아래 기록은 과거 버전 결과다.

## 이전0.2.7/code29 · 플로팅 표시 수정 / Historical floating presentation fix

BUILD PASS,468JUnit PASS,lint0오류/기존3경고,API26/33/34 실제 계측5568/5568/5567검사항목 PASS. 기존 잘림 재현 및 실제 glyph 경계 검사를 추가했다. 동일 APK 설치본·서명·설치 전후 설정·접근성 연결 확인과 실폰 ‘긴영상’/숫자·탭·드래그·X 검증 완료. 독립 소스 리뷰 P1/P2 0건. 새 연속10/20시험은 미실행. APK726467bytes,SHA256 `809CD1EF1287209E23A31896B00FEFF9585511319939FC8113CBC2B1876DAF1A`. [상세 실행기록·제약](FLOATING_LAYOUT_FIX.md),[정식 게시·CI·공개 파일 검증](releases/v0.2.7.md).

EN:0.2.7/code29 passes build,468JUnit,static guards and5568/5568/5567 native assertions. Lint0 errors/3 existing warnings. Actual glyph bounds,legacy clipping reproduction and physical display/tap/drag/X checks passed with matching installed APK. Independent source review found no P1/P2. No new10/20-transition endurance test;see the reports for limitations and stable-publication checks.

## 이전0.2.6/code28 · Public 시험판 검증 완료 / Historical published version

**0.2.6/code28 공개 시험판(pre-release)을 게시하고 공개 파일 검증까지 완료했다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,로컬lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 is published as a public pre-release,and public artifact verification is complete.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;local lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

**공개 검증:** 제품 커밋·태그 `8dbcce3a5cd0cfa2931461773e58e12330de14b4` / `v0.2.6`. [GitHub CI33141470669](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33141470669)는SUCCESS이며,내려받은 보고서32suites·468tests·실패0·오류0·건너뜀0을 확인했다. CI lint는0오류/2경고로 로컬0오류/3경고와 구분한다. Actions의 Node20/setup-java4 사용중단 예고 경고는 비차단 유지보수 항목이다.

**Public verification:** product commit/tag `8dbcce3a5cd0cfa2931461773e58e12330de14b4` / `v0.2.6`. [GitHub CI33141470669](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33141470669) succeeded;downloaded reports show32 suites,468 tests,0 failures/errors/skips. CI lint has0 errors/2 warnings,distinct from local0 errors/3 warnings. Node20/setup-java4 deprecation warnings are nonblocking maintenance items.

2026-08-28 13:21:42KST에 게시했으며,13:22:04.764KST 익명 검증 완료 시 [공개 릴리스](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.6)의 Public·draft=false·pre-release=true와 페이지HTTP200을 확인했다. APK746246bytes·SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`,SHA256텍스트96bytes,업데이트JSON287bytes 모두 고정 산출물과 크기·SHA256이 일치했고 GitHub assets의 세 digest도 일치했다. 설치 APK도 같은 해시이며 제품 바이너리는 변경하지 않았다. 후속 문서는 별도 커밋이며 제품 태그는 변경하지 않는다.

Published at13:21:42KST on2026-08-28;anonymous verification completed at13:22:04.764KST and confirmed the [public release](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.6),Public visibility,draft=false,pre-release=true and HTTP200. The746246-byte APK with SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`,96-byte SHA256 text and287-byte update JSON all matched their frozen artifacts in size and SHA256;all three GitHub asset digests matched too. The installed APK also matches. Subsequent documentation has its own commit and does not change product binaries or the product tag.

독립 검토는 이번 변경과 검증 근거 범위에서PASS·확인된P1/P2 0건이다. 내비게이션 오버레이 실기기 시나리오는NOT RUN이며,동일 길이 영상 쌍의 최종 실기기 미관측·드문timeout 복구·플로팅 글자 일부 잘림 등 명시된 한계를 지우거나 모든 화면의 시각감사PASS로 확대하지 않는다.

Independent review passed for the changed scope and verification evidence,with0 identified P1/P2 issues. The navigation-overlay device scenario is NOT RUN. This does not remove the documented equal-duration-pair/rare-timeout limits or minor floating-label clipping,or imply that every visual scenario passed.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증 후 기존 Public 저장소에v0.2.6/code28 pre-release를 게시했으며 CI·공개 다운로드 동일성도 확인했다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. After the YouTube retest and impact-scope checks passed,v0.2.6/code28 was published as a pre-release in the existing Public repository. CI and public-download parity were verified.

code28 고정 APK는 **746246bytes**,SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`다. 위 PC·계측·설치·YouTube20회 결과는 이 동일 산출물에 해당한다. 시험 후 플로팅X로 실행을 중지하고 blocked=false를 확인했다.13:12 인앱 숫자 입력으로 긴 영상30→60초를 복원하고 런타임을 확인했다. 최종 상태는 **전체OFF,반복1,긴 영상ON/60초,광고ON,라이브ON/0초,시간제ON/10초,화면 분석OFF**다.

The frozen code28 APK is **746246bytes**,SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`. PC,emulator,installation and YouTube20 evidence refer to this same artifact. Execution was stopped with floatingX and blocked=false verified. At13:12 the long-video threshold was restored in-app30→60 seconds and checked in runtime. Final state:overallOFF,target1,long-videoON/60,adsON,liveON/0,timedON/10,visual assistanceOFF.

**YouTube20 실기기 기록:**13:08:46~13:11:14.291,148.6초,기준0→20·요청20/확인20. 일반4·긴 영상15·라이브1,광고/시간제0,수동0·실패0·복구0이다. 전후 화면0~20을 육안 대조했다.10번은 들어온 라이브가 다음 동작으로 이동 중인 화면이고11번에서 다른 일반 영상이 확인됐다. 현재 행의 정확한+1을 관측했다.13번 관측 중 알림 배너가 나타났으나 이후 전환은 계속됐으며 **단일 사례이지 모든 알림에 대한 보증이나 과거 실패 원인 규명은 아니다**. 목표 이후 추가3회는20회 결과에 합산하지 않는다.

**Physical YouTube20:**13:08:46–13:11:14.291,148.6 seconds,baseline0→20 and20 requests/20 confirmations. The mix was4 ordinary,15 long-video,1 live,0 ads/timed,with0 manual swipes,failures or recoveries. Screenshots0–20 were visually reviewed;10 shows the incoming live preview already moving out,and11 confirms a distinct ordinary video. Exact current-row+1 was observed. One heads-up notification at observation13 did not prevent later transitions;this single case neither guarantees all notification behavior nor identifies the original failure's cause. Three post-goal transitions are excluded.

**남은 한계:** 같은 길이의 연속 영상 쌍은 최종 실기기20회에서 나오지 않았다. 동일 길이 분기의 JUnit 검증과 실기기 일반 행+1 관측을 그 사례의 실기기 PASS로 합치지 않는다. 드문 일반 timeout의 실제 새 시작점 복구는 이번 연속 시험에서 발생하지 않았다. 좁은 플로팅의 ‘긴영상’ 첫 글자 일부 잘림은 동작에 영향 없는 경미한 알려진 문제로 남겼으며,1/1·10초 숫자는 정상이다. 전체 시각감사 PASS나 모든 기기·호스트 버전의 보증을 하지 않는다.

**Remaining limits:** no equal-duration pair occurred in the final physical20,so JUnit coverage plus ordinary row+1 observations do not constitute a physical PASS for that exact pair. Rare-timeout fresh-start recovery was not exercised by this run. Minor clipping of the first character in the floating long-video label remains a nonblocking known issue;1/1 and10-second numerals are normal. This is not an all-visual-audit PASS or a guarantee across all devices/host versions.

[현재 동작 계약](PRODUCT_SPEC.md) · [D-035 원인·예방](DEBUG_LOG.md) · [최신 검증](VERIFICATION.md)

| code28 검증 항목 | 현재 상태 |
|---|---|
| PC 빌드·제품시험·lint·정적 가드 | PASS:468JUnit,정적 가드 전체,lint0오류/기존3경고 |
| Android API26/33/34 동일APK 계측 | PASS:233/233/232개 |
| 설치·설정·접근성·런타임·APK 해시 | PASS:전체 기존설정 비교보존·연결·런타임·동일APK 해시 확인 |
| YouTube 실제 자동 전환20회 재시험 | PASS:13:08:46~13:11:14.291/148.6초,요청20/확인20.일반4·긴15·라이브1,수동/실패/복구0,전후0~20육안확인 |
| 시험값 복원·실행 중지 | PASS:13:12 인앱30→60복원·런타임확인,전체OFF·blocked=false |
| 정확히 같은 길이 영상 쌍의 실기기 확인 | 이번20회에서 미관측.동일길이 JUnit과실제행+1 관측은별도증거 |
| 알림 배너 중 전환 | 단일사례 관측,후속전환지속.모든알림안정성보증아님 |
| 최종 시각·사용성 | 비차단경미이슈:플로팅‘긴영상’첫글자일부잘림,숫자정상.전체시각PASS아님 |
| Instagram 전체10회 재실행 | 이번 범위에서 생략,code26 PASS 보존·새 실행 주장 금지 |
| 독립 검토 | PASS:이번변경·검증근거범위,확인된P1/P2 0 |
| 내비게이션 오버레이 실기기 | NOT RUN,전체시각PASS로확대하지않음 |
| GitHub CI33141470669 | SUCCESS:32suites/468tests,실패·오류·건너뜀0. CI lint0/2,로컬lint0/3과구분 |
| Public pre-release·익명 다운로드 일치 | PASS:13:21:42KST게시·13:22:04.764익명검증,HTTP200,draftfalse/prereleasetrue,APK·SHA텍스트·JSON크기/해시일치 |

## 과거 code26 · 고정 APK 검증 / Historical frozen candidate

**과거0.2.6/code26은 실폰 후속 실패로 게시 보류된 미배포 후보였다.** 빌드·454 JUnit(실패0)·정적 가드 PASS, lint0오류/기존3경고. 12:33 동일 APK의 Android API26/33/34 계측209/209/208개 PASS,12:36 휴대폰 설치·전체 기존 설정 직접 비교 보존·접근성 연결·설치 APK 해시 일치 PASS. 12:38:20 YouTube 공식 시험은 요청10/확인10(긴 영상9+라이브1)과 전후 화면의 서로 다른 영상 확인으로 PASS했다. 12:39:22 별도 일반1/1 전환1회도 화면 쌍으로 확인했다. 그러나 후속 연속 실행 중 요청20/확인19에서 같은59초 길이·pager index 부재로 안전정지했다. 해당 실패 요청에는 전후 화면 쌍이 없어 실제 다음 영상 이동 여부는 미확정이다. Instagram은12:43:56~12:45:31.831(96.0초) 별도 시험에서 요청10/확인10(일반3·긴 영상4·시간제10초2·광고1),수동0·실패/복구0으로 PASS했다. **두 앱의 지정10회 PASS가 유튜브 후속 실패를 덮지 않으며 제품 완료·배포 준비 완료가 아니다.**

| 항목 / Check | code26 결과 / Result | 증거·한계 / Evidence and limits |
|---|---|---|
| 빌드·제품 JUnit | PASS | 454개,실패0 |
| 정적 가드·lint | PASS | 정적 가드 전체 PASS,lint0오류/기존3경고 |
| API26/33/34 동일 APK 계측 | PASS | 12:33 각각209/209/208개. 네이티브 서비스 안전·UI 포함 |
| 휴대폰 설치·설정·접근성·해시 | PASS | 12:36 code26 설치,전체 기존 설정 직접 비교 보존,서비스 연결,설치 APK 일치 |
| YouTube10개 실제 자동 전환 | PASS·종류 한정 | 12:38:20 요청10/확인10.긴 영상9+라이브1,전후 화면0~10에서 서로 다른 페이지 확인.일반10회 시험 아님 |
| 별도 일반1/1 전환 | PASS·1회 | 12:39:22 요청/확인 기준13→14,전후 화면 쌍 확인.공식10회와 합산하지 않음 |
| 이후 YouTube 연속 실행 | FAIL·게시 보류 | 요청20/확인19,요청/현재 길이59초·다른metadata key·pager index−1로안전정지.이실패요청의전후화면쌍이없어실제이동미확정 |
| Instagram10개 실제 자동 전환 | PASS | 12:43:56~12:45:31.831/96.0초,기준19→29.일반3·긴4·시간제2·광고1.수동/실패/복구0.전후 화면 대조 |
| 드문 일반 timeout의 실제 새 시작 복구 | 미실행 / NOT RUN | 합성 서비스 검사를 실제 장애 복구로 대체하지 않음 |
| 시험값 복원·실행 중지 | PASS | 플로팅X 종료·blocked=false,12:46 인앱30→60초 입력 후 UI/런타임 확인,전체OFF |
| 최종 전체 화면 시각/사용성 감사 | 미완료·경미한 잘림 | 좁은 플로팅에서‘긴영상’첫글자일부잘림관측.1/1·10초표시정상,동작영향없음.전체시각PASS아님 |
| 공개 CI·Release·익명 다운로드 비교 | 미실행 / NOT RUN | 아직 미게시. 공개판0.2.5 결과를 재사용하지 않음 |

고정 APK: **757038bytes**,SHA256 `82CE7C221C1BF3E6DA8F86F9D487F9685D89DFB22A38D24F60B77F447519E926`. 위 빌드·계측·설치 결과는 이 산출물에 해당하며 진행 중 관측을 최종 연속시험 PASS로 올리지 않는다.

연속 시험은 반복1·긴 영상ON/기준30초·광고/라이브ON·Instagram 시간제10초로 수행했다. 종료 후 플로팅X로 실행을 중지했고 blocked=false를 확인했다.12:46 인앱 숫자 입력으로 긴 영상 기준30→60초를 복원하고 UI·런타임에서 확인했다. 최종 상태는 **전체 실행OFF,반복1,긴 영상ON/60초,광고ON,라이브ON/0초,시간제ON/10초,화면 분석OFF**다. 제품의 신규 기본값OFF/60초를 바꾼 것이 아니라 기존 옵션은 보존했다. 드문 일반 timeout의 실제 발생·새 시작점 복구,최종 전체 화면 시각/사용성 감사,공개 CI·Release·익명 다운로드 동일성은 완료로 표시하지 않는다. 유튜브 후속 확인 실패가 남아 게시 보류를 유지한다.

Instagram 공식 시험은12:43:56~12:45:31.831,총96.0초이며 기준 요청/확인19→29에서 **요청10/확인10 PASS**다. 구성은 일반3·긴 영상4·진행정보 없는10초 시간제2·광고1,수동 이동0·실패0·복구0이다. 전후0~10 화면을 육안 대조했으며8번 캡처는 광고→일반 전환 중이고9번은 안정된 페이지였다. 목표 뒤 추가6회는 이10회 결과에 합산하지 않는다.

The formal Instagram run lasted96.0 seconds at12:43:56–12:45:31.831,advancing the request/confirmation baseline19→29:10/10 PASS. It comprised3 ordinary,4 long-video,2 ten-second clockless and1 ad transition,with0 manual swipes,failures or recoveries. Screenshots0–10 were visually reviewed;capture8 shows the outgoing-ad/incoming-ordinary gesture and9 a settled page. Six later transitions are excluded from this ten-transition result.

다음 안전한 조사 방향은 `CollectionItemInfo` 또는 pager 스크롤 위치가 독립적인 페이지 이동 근거를 제공하는지 **읽기 전용으로 관측**하는 것이다. 이번 상태 정리에서는 추가 구현·공개를 하지 않는다. 기존 확인 조건을 제거하거나 서로 겹치지 않는 제목 전용→음원 전용 메타데이터를 곧바로 다른 영상으로 인정하지 않는다.

The next safe investigation is read-only observation of CollectionItemInfo or pager scroll position for independent transition evidence. No further implementation or publication occurs in this status update. Do not drop confirmation guards or treat disjoint title-only→audio-only metadata as proof of another video.

YouTube의 별도 RAM 메타데이터 키 경로는 **다른 키 AND (요청 후 같은 창·pager의 최신 실제 index 변화 OR 다른 유효 총길이) AND 300ms 이상 안정 AND 최신 실제 전진 재생**을 모두 요구한다. 요청 시 키 출처를 고정해 메타데이터의 등장·소실을 다른 출처의 키와 비교하지 않는다. 부분 메타데이터 소실로 키만 달라져도 이동으로 인정하지 않는다. 일반 반복 identity는 바꾸지 않는다. 메타데이터 키를 쓰지 않는 기존 확인 경로는 안정된 다른 identity 또는 최신 동일 pager 이동+다른 안정된 총길이+전진 근거를 유지한다. 길이 단독은 확인 근거가 아니며, 메타데이터 경로에서 같은 길이이고 pager index도 없으면 실제 이동했더라도 안전정지할 수 있다. 긴 영상 확인4.5초 실패는 일반 복구나 추가 스와이프로 우회하지 않는다.

**Historical0.2.6/code26 remained unpublished after a subsequent device failure blocked its release.** Build,454 JUnit tests with zero failures,static guards and209/209/208 exact-APK API26/33/34 checks passed; lint has0 errors and3 existing warnings. Installation preserved all compared preferences and accessibility binding,and matched the APK hash. The12:38:20 YouTube run passed10 requests/10 confirmed distinct transitions:9 long-video and1 live. A separate12:39:22 ordinary1/1 transition also passed screenshot-pair review. Further continuation then safety-stopped at20 requests/19 confirmations when both durations were59 seconds and pager indices were unavailable. No pre/post screenshot pair exists for that failed request,so actual movement is unproven. A separate96.0-second Instagram run at12:43:56–12:45:31.831 passed10 requests/10 confirmations:3 ordinary,4 long-video,2 ten-second clockless and1 ad,with no manual swipes,failures or recoveries. The two designated ten-transition PASS results do not override the later YouTube failure or establish release readiness.

The supplemental YouTube RAM-metadata path requires a different key AND either request-fresh same-window/pager index movement or a different valid duration,then at least300ms of stability and current forward playback. The identity source is fixed at request time; appearing,missing or partially missing metadata alone cannot confirm movement. Ordinary repeat identity is unchanged. Non-metadata confirmation retains stable changed identity or corroborated fresh pager movement with changed stable duration and forward progress. Duration alone is insufficient. Same-duration metadata pages without pager indices may still safety-stop after real movement. Long-video4.5-second timeouts never use ordinary recovery or retry swipes.

## 과거 code23/24/25 실폰 실패 · 미배포 / Historical device failures,unpublished

**이전 후보는 별도 기록이다.** code23은12:12 실제62→93초 영상 이동 후 요청1/확인0으로 실패했고, code24는12:17 같은 창·영역·인식·안전 조건에서도 공통 텍스트 identity가 같음을 재현했다. code25도12:21~12:22 실제93→57초 이동 후 요청/현재 index가 모두−1이고 공통 identity가 같아 실패했다. code23/24/25는 실폰FAIL·미배포이며 PC·계측PASS가 이를 덮지 않는다. code22의 YouTube2회는 기능 추가로 중단한 과거 관측이며 수동180초 영상 이동1회는 제외했다. 어느 후보의 관측도 code26의10회에 합산하지 않는다.

code25의 과거 PC 증거는 **442JUnit·정적 가드 PASS**,API26/33/34 **166/166/165 PASS**다. 고정 APK **722207bytes**,SHA256 `0EE88E622E9EA7F85DC0FFFDBFFE0D2104EDB616E82E2D1AAF896B5EE569ECE6`. 같은 후보의 실폰FAIL과 함께 보존하며 이후 code28 또는10+10 PASS로 재사용하지 않는다.

Earlier code23/24/25 candidates failed physical confirmation and were not published,despite PC/emulator passes. Code23 actually moved62→93 seconds but confirmed0 of1 requests; code24 reproduced identical shared-text identities; code25 moved93→57 seconds but both pager indices were−1. Code22 stopped after two automatic transitions for feature integration,excluding one manual180-second skip. No historical transitions count toward code26.

## 과거 0.2.6/code23 검증 · 실폰FAIL/미배포 / Historical candidate,device FAIL

code23은 일반 카운트 복구와 긴 영상 건너뛰기(기본OFF·총길이≥60초·1~3600초)를 통합한 미게시 후보다. 긴 영상은 반복0과 독립이며 길이 불명·정지·불안전한 화면을 우회하지 않고, 긴 영상4.5초 전환 확인 실패는 기존 안전정지다. 아래는code23 자체의 결과이며 과거code22의383/109/109/108과 구분한다.

| 검사 / Check | 결과 / Result | 범위 / Scope |
|---|---|---|
| 최종 BUILD / Final build | PASS | code23 제품·시험 APK 컴파일 |
| Unit/Regression | PASS | 418제품시험·실패0 /418 product tests,zero failures |
| 정적가드 / Static guards | PASS | 복구·긴 영상·권한·조회 생명주기 등 전체 연결가드 |
| Lint | 보고서 재확인 중 / Report recheck pending | 실행 결과0오류·기존3경고,최종 보고서 대조 예정 |
| Android26/33/34 | PASS |12:10 동일 고정 APK163/163/162검사,네이티브 안전서비스·UI 포함 |
| 실폰 설치·설정·접근성 / Phone installation | PASS |12:10 code23설치·기존prefs보존·접근성bound·설치해시 일치 |
| YouTube10 | FAIL·미완료 |12:12 첫 긴영상 요청에서 실제이동했으나전환확인실패·안전정지,10연속PASS아님 |
| Instagram10 | NOT RUN | 새code23에서 별도 실제 자동 전환 관측 예정 |
| 실제timeout 복구 / Physical timeout recovery | NOT RUN | 합성 네이티브서비스 시험과 구분 |
| 긴 영상 실폰 조건별 시험 / Physical long-video cases | FAIL |62초→93초 실제이동과확인실패를관측. 전체조건시험완료아님 |
| 최종 기능·시각·사용성 리뷰 / Final device review | 검증 중 / In progress | 후보 실화면·소셜앱 관측 결과 후 확정 |
| Public·CI·익명 다운로드 / Publication | NOT RUN | 미게시 / Unpublished |

고정code23 APK는 **757601bytes**,SHA-256 `FC866F0459CD3536114758DB277F0FCD0EF84CFA443E9C8817B448D6ED704B7F`다. version0.2.6/code23/minSdk26. PC·에뮬레이터·설치본의 버전과 해시를 구분하며, 공개 다운로드와의 비교는 아직 없다. 실제두앱10+10은 동일새APK에서 각각 관측하고 수동이동·이전후보관측·합성복구를 합산하지 않는다.

EN: Historical code23 passed build,418 tests,static guards,163/163/162 emulator checks and installation parity,then **failed physical long-video confirmation at12:12**. The frozen757601-byte artifact and its hash remain evidence for that failed unpublished candidate,not a release-ready result. Instagram10 and publication were not completed. These PC/emulator passes do not override the device failure or verify code25.

## 과거 0.2.6/code22 검증 / Historical candidate verification

| 검사 / Check | 결과 / Result | 범위 / Scope |
|---|---|---|
| BUILD | PASS | 제품·계측 APK 컴파일 / Product and instrumentation APKs |
| Unit/Regression | PASS | 383제품시험, 이전356+새27 /383 product tests |
| Lint | PASS | 0오류·기존3경고 /0 errors,3 existing warnings |
| 정적가드 / Static guards | PASS | 복구의 읽기전용·특수경로차단·기존권한/모듈가드 / Read-only recovery and preserved guards |
| 독립소스리뷰 / Independent source review | PASS(범위 내) | 확인된P1/P2 없음 / No confirmed P1/P2 |
| Android26/33/34 | PASS | 같은 APK109/109/108검사,각35합성복구서비스검사 포함 / Exact APK,35 synthetic recovery checks per OS |
| 설치·설정·접근성 / Installation, preferences, accessibility | PASS | code22 설치·전체OFF·다른설정 보존·기존접근성bound·설치파일해시 / Installed parity and preserved settings |
| YouTube10 / Ten YouTube transitions | 중단·10PASS 아님 / Interrupted,not PASS | 자동2회 확인,180초 영상1개 수동제외 후 기능 추가로 중단 |
| Instagram10 / Ten Instagram transitions | NOT RUN | YouTube 이후 실행 / Scheduled after YouTube |
| 실제timeout복구 / Physical timeout recovery | NOT RUN | 합성시험과 별개,자연발생 아직미관측 / Separate from synthetic tests |
| Public release/CI/download | NOT RUN | 게시 전 / Before publication |

과거code22 고정 APK711847bytes,SHA256 `A0916CD7935336D0527E0CB19EDEB3574A0F1406C508D75395CF063A7B7F3FCE`. version0.2.6/code22/minSdk26,기존서명유지. YouTube자동2회와180초 영상1개 수동제외는 기능 추가 전 중단 세션이며 최종10PASS가 아니다. 합성서비스시험은 실제tick·물리알림·gesture콜백전체E2E가 아니며code23검증으로 재사용하지 않는다. 두앱각10개를한앱20연속으로합산하지않는다. 아래0.2.5검증도과거이력이다.

## 2026-08-28 · v0.2.5 공개 검증 완료 / Public release verification

검증 기준은 공개 코드 커밋 `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5`(main·태그v0.2.5)과 동일한 code21 APK다. 이후 문서 정리는 제품 소스·APK를 바꾸지 않는다.

| 검사 / Check | 결과 / Result | 확인 범위 / Evidence |
|---|---|---|
| 저장소·릴리스 / Repository and release | PASS | Public, [v0.2.5](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.5),2026-08-28 10:57 KST,draft=false/prerelease=true |
| 코드 CI / Code CI | PASS | [33134278633](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33134278633) success; 내려받은 보고서356시험·실패0·오류0·건너뜀0 /356 tests,0 failures/errors/skips |
| CI Lint | PASS | 0오류·2경고. 아래 로컬0오류·기존3경고와 구분 /0 errors,2 warnings, separate from local3 warnings |
| 익명 릴리스 접근 / Anonymous release access | PASS | HTTP200 |
| 배포파일 / Uploaded assets | PASS | APK709703bytes + SHA96bytes + 업데이트JSON287bytes,총3개. 자동생성 소스 압축본 제외 / Three uploaded files, excluding generated source archives |
| 익명 재다운로드 / Anonymous redownload | PASS | 3개 파일 각각 원본 크기·SHA-256 일치 / Every downloaded asset matches its original size and SHA-256 |
| APK·설치본 동일성 / APK and installed parity | PASS | 공개 APK709703bytes,아래 SHA-256;10:51 설치본과 일치 / Published and installed APK match |
| APK 서명 / APK signature | PASS | v2서명 검증·기존 인증서 유지 / v2 verification and existing certificate retained |
| 업데이트 메타데이터 / Update metadata | PASS | versionCode21/versionName0.2.5/minSdk26 |
| 공개 대상 독립감사 / Independent publication audit | PASS | 169개 후보의 개인정보·키·원시자료·절대경로 제외 확인 /169 candidate files checked for excluded sensitive content |
| 문서 링크 / Documentation links | PASS | 로컬 링크224개,누락0 /224 local links,0 missing |
| 공개 후 제품 업데이트 조회 / Post-release in-app update check | PASS | 10:58~10:59 실제 수동 조회·새 업데이트 없음 안내·조회 시각 갱신. 자동조회ON/전체실행OFF 유지 |
| 메일 / Email | 미발송 / Not sent | 이번 배포에 포함하지 않음 / Not part of this delivery |

공개 APK SHA-256: `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`.
서명 인증서 SHA-256: `3604d3a1cc1f4e8f772d718cf8b9cba5adfd3650708cf169660b653e28b69632`.

공개 후10:58~10:59, 설치된code21 앱의 업데이트 확인을 실제 실행해 새 업데이트 없음 안내·조회 시각 갱신을 확인했다. 실제 Public HTTPS 조회와 현재 버전 안내는PASS이며 새 버전 다운로드/설치 재시험은 아니다. 업데이트 자동조회ON·전체실행OFF를 유지했다.

EN: Public prerelease0.2.5/code21 was verified against code commit `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5` and the unchanged APK. CI passed356 tests with no failures/errors/skips; CI lint0/2 is separate from local lint0/3. Anonymous release access and all three original/downloaded asset size/hash comparisons passed, with the existing signing certificate retained. Subsequent documentation changes do not alter product source or the APK. At10:58–10:59, the installed code21 app successfully checked the public release over HTTPS, displayed no applicable newer update and updated its attempt timestamp. This verifies current-version checking, not a new-version download or installation. Automatic checking remained ON and overall execution OFF.

code21 일반 YouTube10연속PASS와 공개 완료를 code21 라이브0초/5초/OFF 개별 재시험(NOT RUN),20연속 미완료 또는 D-021 미수정의 해결로 확대하지 않는다. 개별 라이브PASS는 아래code20 이력이다.

EN: Publication and ten normal YouTube transitions do not establish individual code21 live retests, twenty consecutive transitions or a fix for D-021. Individual live passes below belong to code20.

## 2026-08-28 · 공개 code21 로컬·실기기 검증 / Released code21 local and device verification

code21은 차단 상태의 확장 조회 재활성화 금지와 반복0/별도 동작 없는 idle 플래그 원복을 보강한 **공개0.2.5 산출물**이다. 고정 APK **709703bytes**, SHA-256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`. 최종 소스·같은 APK로 아래 결과를 확인했으며 code20 수치를 재사용하지 않는다.

| 검사 / Check | 결과 / Result | 범위 / Scope |
|---|---|---|
| 최종 BUILD / Compile | PASS | code21 제품·시험 빌드 / Final product/test build |
| Unit / Regression | PASS | 직접JUnit356개 /356 product tests |
| Lint | PASS | 0오류·기존3경고 /0 errors,3 existing warnings |
| 정적 보호 / Static guards | PASS | LIVE_TREE_LIFECYCLE 포함 조회 생명주기 원복 검사 |
| 동일 APK 계측 / Exact-APK emulator checks | PASS | 10:39 API26/33/34 각각74/74/73 |
| 휴대폰 설치·연결 / Phone installation and binding | PASS | 10:40 code21 설치·접근성 bound 확인 |
| 설정·산출물 동일성 / Preferences and APK parity | PASS | 재생 설정 보존·설치 SHA256 및709703bytes 일치 |
| 최종 독립리뷰 / Final independent review | PASS(검토 범위) | 조회 원복 추가P1/P2 없음 / No additionalP1/P2 findings |
| code21 YouTube10연속 / Ten consecutive transitions | PASS | 10:40:41→10:47:45.702,424.5초,확인0→10,세대6·수동입력/앱전환 없음·라이브0 |
| 실제 전환 화면 / Visual transition review | PASS(관측 범위) | 전환1~10 모두 서로 다른 정상 전체화면 영상·플로팅·올바른 이동 방향 육안 확인 |
| 시험 후 전체 실행 / Post-test execution | PASS | 약10:50 플로팅X로 전체OFF 확인 |
| 업데이트 자동조회 선택 복원 / Restore update preference | PASS |10:51 실제 UI에서ON 복원, UI·저장값 확인 / Restored ON via UI and checked in storage |
| 20연속 / Twenty consecutive transitions | 미완료 / Incomplete | 이번 공식 목표10회와 구분 |
| code21 라이브0초/5초/OFF 개별 / Individual live retests | NOT RUN | 아래 개별 라이브PASS는code20의 근거 |
| Public·CI·공개 재다운로드 / Publication | PASS | 위 공개 검증 표 참조 / See publication evidence above |

code21 공식10연속은 결과 집계의PASS와 전환1~10 화면의 실제 육안 대조를 모두 확인했다. 수동 입력·앱 전환 없이 확인기준0→10,같은 세대6으로 진행했으며 모두 일반 영상이고 라이브 이동은0이다. code20의 외부 중단2회나 개별 라이브 시험,목표 이후 추가 관측을 합산하지 않는다. 새 라이브0초/5초/OFF 개별 시험은code21에서 미실행이므로 code20의 근거와 구분한다. D-021은 미수정이며20연속 완료로 확대하지 않는다.

The final **code21** artifact passed build,356 product tests,lint,static guards and74/74/73 emulator checks;phone version/binding/preferences/hash and final review also passed. The separate code21 run **passed10 consecutive normal-video transitions** in424.5 seconds(10:40:41→10:47:45.702),confirmation0→10,generation6,with no manual input or app change and no live skips. All10 transition screens were visually reviewed. Post-target observations are excluded. FloatingX stopped overall execution around10:50. The update preference was restored ON at10:51. Code21 individual live retests remain unperformed;D-021 and20-transition completion are unresolved. Publication checks passed as recorded above.

## 2026-08-28 · 과거 code20 미게시 후보·실기기 검증 / Historical unpublished code20 verification

**0.2.5/code20, 미게시.** cleanup을 포함한 확정 APK는 **725487bytes**, SHA-256 `EF59D4E40E192A89D5B207741B03CCE08FA11AC1079DC61C7776C19A1D3D60EB`이다. 같은 후보를 에뮬레이터와 휴대폰에서 확인했다. 초기 cleanup 전356시험과 후속 cleanup 포함 최종356시험을 구분하며 아래는 후자의 결과다.

**Unpublished0.2.5/code20:** the final cleanup-inclusive **725487-byte** APK has the SHA-256 above. The following results concern this exact candidate, including the final356-test rerun, not just the earlier pre-cleanup checkpoint.

| 검사 / Check | 결과 / Result | 실제 범위 / Scope |
|---|---|---|
| BUILD / Compile | PASS | cleanup 포함 최종 제품·시험 APK / Final cleanup-inclusive build |
| 제품 Unit / Regression | PASS | 직접 JUnit356개 /356 product tests |
| Lint | PASS | 0오류·기존3경고 /0 errors,3 existing warnings |
| 동일 후보 에뮬레이터 / Exact-candidate emulators | PASS | 10:30 API26/33/34 각각74/74/73개 / Not host-app scrolling E2E |
| 휴대폰 설치·준비 상태 / Phone installation/readiness | PASS | code20 설치 및 준비 확인 / Installed candidate ready |
| 재생 설정 보존·설치 APK / Preferences and installed parity | PASS | 설정 유지,725487bytes·위 해시 일치 / Preferences, size and hash match |
| 실제 UI 준비 상태 / Main UI readiness | PASS(관측 범위) | 권한 경고 없음 육안 확인 / No permission warning visible |
| 라이브5초 지연 /5-second live delay | PASS | 10:30:29 대기 시작 →10:30:34 요청 →10:30:35 확인누계1·25초 일반 영상 진입 |
| 라이브OFF 대기 / Live-OFF waiting | PASS | 10:33:33~39 같은 라이브 인식,blocked=false·요청2/확인2 유지 |
| 0초·반복0회 독립 / Immediate at zero normal plays | PASS | 10:34:31 조회 준비 →10:34:33 요청 →10:34:34 확인누계2→3 |
| 새10연속 시험 / New10-transition run | 외부 중단·2/10 / Externally interrupted | 10:35:28 확인기준3·세대30 →10:35:52 1회 →10:36:47 2회 →10:37:17 외부 앱 전환.10PASS 아님 |
| D-021 일반 반복 경계 / Normal loop-boundary issue | 미해결 / Unresolved | code16의19초17→0 및 과거18초16→0 문제를 라이브 수정으로 해결 처리하지 않음 |
| Public Release·CI·익명 재다운로드 / Publication checks | NOT RUN | 새 후보 미게시 / Candidate unpublished |

5초 시험은 요청뿐 아니라 다음 일반 영상 진입까지 확인했다.0초는 별도 시청 지연이 없다는 의미이며 조회 준비·안정화·안전 확인 시간을0ms로 보장하지 않는다.0회 독립 시험의 증가분은1회(확인2→3)이고 누계3을 새로운3연속 성공으로 세지 않는다. 라이브OFF 시험도 이동 성공이 아니라 요청이 늘지 않는 보호 동작의 PASS다.

The5-second test confirmed arrival at a normal video, not only gesture dispatch. Immediate mode still permits retrieval/settlement/safety checks. The zero-play test confirmed one transition(2→3), not a new three-transition streak; live-OFF passed by keeping requests unchanged. The separate10-transition run was interrupted externally after two confirmations, not completed.

연속 시험 상세:10:35:28 기준확인3/세대30에서 시작해10:35:52 첫 확인 후54초 영상,10:36:47 두 번째 확인 후48초 영상으로 이동했다.10:37:17 전면이 런처/카메라로 바뀌어 세션을 중단했다. 종료 시 blocked=false·pending=false·요청5/확인5, 마지막 진행30/48초다. 제품 감지 실패로 분류하지 않지만10연속 성공도 아니며, 이전 개별 라이브 시험을 합산하지 않는다.

The run reached a54-second video after its first confirmation and a48-second video after its second. Foreground launcher/camera transition ended the session at10:37:17 with blocked=false, pending=false, requests=5, confirmations=5 and last progress30/48 seconds. This was external interruption, not a detected product failure or a10-transition PASS.

code18의 인식FAIL은 프로브/제품 플래그 불일치였다. 동일 화면3초씩 비교에서 기본49노드에는 전용 라이브 노드가 없고 확장101노드에는 있었다. 중간code19는 라이브ON만 확장했으나 code20은 전체실행ON·YouTube선택·전면YouTube이면 라이브 옵션과 무관하게 확장한다. 라이브OFF의 일반→라이브 확인과 pending 중 동일한 조회형태를 유지하고, 모드 변경 후 이전 root 폐기 및 failClosed/onDestroy 원복을 적용했다. D-033의 해당 원인은 실제 기기 재시험PASS로 확인하되 모든 호스트 UI·연속 라이브·장시간을 보장하지 않는다. [D-033](DEBUG_LOG.md).

Code18's flag mismatch is corrected and device-retested for the observed cases. Code20 keeps retrieval consistent even with live skipping OFF, discards old roots on mode changes and restores default flags on stop/destruction. This is not a guarantee for every host UI, consecutive live pair or endurance run.

## 2026-08-28 · 과거 code18 고정 후보 / Retained code18 candidate

**0.2.5/code18, 미게시·실기기 라이브 인식 FAIL.** 라이브 옵션은 기본OFF·대기0~60초/기본0초(안전 확인 후 바로), 일반 반복0회와 독립이며 전체 실행OFF는 광고·라이브까지 중지한다. [라이브 계약](LIVE_SKIP.md). 아래 수치는 동일 고정 후보에 대한 결과이며 code16/17 및 후속 code20와 합산하지 않는다.

**0.2.5/code18 was not published and failed actual live recognition.** Live previews are opt-in, initially OFF, with a0–60-second delay(default0, immediate after safety checks). The option is independent of zero normal plays; main OFF stops everything. Code16/17 and code20 results remain separate.

고정 APK / Frozen APK: **738945 bytes**. SHA-256: `941532517058CB8553EFE5DB34ED1762426C468B2D66F88A567CE788E306C54D`.

| 검사 / Check | 결과 / Result | 실제 범위 / Scope |
|---|---|---|
| BUILD / Compile | PASS | code18 제품·시험 APK 빌드 / Product and test APKs |
| 제품 Unit / Regression | PASS | 직접 JUnit352개 /352 product tests |
| Lint | PASS | 0오류·기존3경고 /0 errors,3 existing warnings |
| 동일 후보 에뮬레이터 계측 / Exact-candidate emulator checks | PASS | API26/33/34 각각74/74/73개. 실제 소셜앱 E2E 아님 / Not social-app scrolling E2E |
| 실기기 설치 / Phone installation | PASS | ADB 업데이트 설치, versionName0.2.5/versionCode18 확인 / ADB replacement installation |
| 접근성 재연결 / Accessibility reconnection | PASS | 기존 허용 서비스의 설치 후 연결 회복. 새 권한 자동 부여·실행 자동 시작 아님 / Existing service rebound; no new grant or auto-start |
| 재생 설정 보존 / Playback preferences | PASS | 설치 전후 설정 유지 / Preserved across installation |
| 설치 APK 동일성 / Installed APK parity | PASS | 738945bytes 및 위 SHA-256 일치 / Matching size and hash |
| 독립 리뷰 지적 보완 / Review fixes | 수정 완료 / Implemented | 최종 root 재검증, 오래된 인덱스 배제, 일반 수집 회귀 방지, 동일 uptime 안정 상태 유지 |
| 최초 실기기 라이브 관측 / Initial live observation | FAIL | 조회 플래그 차이로 전용 노드가 없어 인식 대기 지속. D-033 / Dedicated node absent under product flags |
| 라이브0초·5초 정식 실기기 시험 / Formal0s and5s phone tests | NOT RUN | 인식 문제 진단 후 별도 시험 / Pending recognition diagnosis |
| code18 YouTube10연속 / Ten consecutive transitions | NOT RUN | code16의2/10 실패와 합산하지 않음 / Separate from the failed code16 run |
| code18 D-021 재시험 / Loop-boundary retest | NOT RUN | 일반 영상17→0은 code16 당시 관측이며 code18 새 증거 아님 |
| 새 Public Release·CI·익명 재다운로드 / Publication checks | NOT RUN | 실패 후보 미게시 / Failed candidate unpublished |

최종 root 재조회에서 창·페이지·라이브 상태를 다시 확인하고, 라이브→라이브는 요청 이후 같은 창/pager의 인덱스 변화와 다른 안정 페이지를 함께 요구한다. 이전 이벤트를 다음 요청에 재사용하지 않는다. 일반 쇼츠는 기존의 null-child 허용·600노드 상한 수집을 유지하고 라이브에만 완전성 감사를 적용한다. 동일 millisecond 재검증은 안정 시간을 늘리지 않고 이미 완료한 상태만 유지하며 역행은 초기화한다. 이 자동시험 결과가 실제 라이브 넘김·장시간 안정성을 대신하지 않는다.

The final root is revalidated immediately before action. Live-to-live confirmation requires fresh same-window/pager index evidence and a distinct stable page. Normal collection retains its existing bounded behavior; complete-tree auditing is required for live actions. Same-timestamp checks preserve settled evidence without adding time. These checks do not establish real live scrolling or endurance reliability.

D-019/D-021 및20연속 검증 미완료를 유지한다. 미연결 VisualSequence20시험의18PASS/2FAIL와 별도 오디오 진단은 제품352시험과 분리한다. 새 기능의 실제 결과는 후속 증거가 생긴 뒤 추가하며 미실행을 PASS로 바꾸지 않는다.

## 2026-08-28 · 과거 code16 YouTube10연속 시험FAIL 및 라이브 조기 식별 조사

- 설치본0.2.5/code16, 목표1회. 읽기 전용 관측101.4초에서2개 전환 성공 후3번째 라이브 미리보기 진입에 전환 확인 실패/안전정지.10연속 성공 아님. 이전 누계/수동 전환을 이 시험에 합산하지 않는다.
- 코드와 실제 화면 대조: 제스처는 새 라이브 페이지로 이동했지만 유효 SeekBar0개여서 당시 code16 감지기가 새 페이지를 인정하지 못했다. 접근성은 연결 상태였다.
- 후속 수동 일반→동일 라이브 재진입2회에서 전용 `immersive_live_preview_player`가 안내 문구 전에도 접근성 트리에 존재함을 확인. 일부만 보이는 전환 도중에도 잡히므로 실제 넘김에는 활성 페이지/안정화 보호가 필요하다.
- 이 code16 조사 단계에서는 제품 수정/라이브 설정/라이브 자동 넘김 재시험이 미구현·미실행이었다. 조사는 기능 구현 PASS가 아니며 이후 code18 구현·검증은 위 항목으로 분리한다. [D-032 상세](DEBUG_LOG.md).

## 2026-08-28 · 과거 UI 안내 보완 후보 code17 체크포인트

당시 후보는0.2.5/code17이었다. 접근성 재연결이 필요한 상태를 구분하는 MainActivity와 안내 문자열만 보완했으며 핵심 감지·업데이터 코어는 아래 code16 검증본과 같았다. 아래 표는 그 시점의 빌드 진행 중·기기 설치 전 체크포인트를 보존하며 현재 code20 상태가 아니다. code16의 실제 OS 설치 성공을 code17 설치 완료로 바꾸어 기록하지 않는다.

| 검사 | 결과 | 범위 |
|---|---|---|
| code17 빌드·안내 변경 시험 | 진행 중 | MainActivity/문자열의 재연결 안내 보완 |
| code17 실기기 설치·해시·안내 화면 | 미실행 | 최종 산출물 확정 뒤 비파괴 확인 예정 |
| code17 Public·CI·공개 자산 동일성 | 미수행 | 게시 전 체크포인트 |
| 기존 감지 안정성·20연속 | 미완료 | 아래 과거 시험의 한계 유지, 이번 UI 보완으로 해결했다고 표시하지 않음 |

## 2026-08-28 · 중간 code16 핵심 업데이터·호환성 검증

아래 결과는0.2.5/code16,703134bytes의 중간 통합 APK에 해당한다. 후속 UI 후보code17 및 후속 라이브 후보code18·20과 구분한다.

| 검사 | 결과 | 실제 범위 |
|---|---|---|
| BUILD / Compile | PASS | 제품 APK·시험 APK 빌드 |
| 제품 Unit / Regression | PASS | 직접 JUnit274개, 기존 OS정책 포함241 + 업데이트정책33 |
| Lint | PASS | 0오류3경고 |
| 호환성·업데이트 계측 | PASS | API26/33/34에서 각각47/47/46개 |
| 설치 사전검사 | PASS | API26/33/34 각36개 + 실제 Android17 기기36개 |
| 손상 사본·provider 제한 | PASS | 정상 사본,0바이트 사본·잔류.part 복구,해시·버전 메타데이터 불일치 거부,읽기·쓰기·경로 제한 |
| 실제 Android 설치 | PASS | 시험 시작 code15→중간 code16,Android 설치창을 통한 버전 상승 |
| 설정 보존·APK 동일성 | PASS | 설치 전후 설정값 동일,설치본과 원본703134bytes/SHA256 일치 |
| 실제 GitHub 네트워크 | PASS | API26/API34/실기기에서 기존 공개 자산의 HTTPS 다운로드·리다이렉트 경로 확인; 새0.2.5 공개 파일 시험 아님 |
| 서명 일치 검증 | PASS(소스·정상 경로) | API28 전후 분기,전체 서명 집합 비교를 소스 리뷰하고 동일 서명 APK의 실제 설치 확인 |
| 다른 키 실물 APK 거부 | 미실행 | 소스의 서명 불일치 차단과 별개이며 동적 시험 PASS에 포함하지 않음 |
| 제품 시험 진입점 | PASS(정적 구성) | 시험 자산·외부 QA 진입점·검증 우회가 제품에 없고 전송 대체는 계측에서만 주입 |
| 새 Public 게시 | 미수행 | 중간 code16 검증본의 당시 상태. 현재 code20과 구분 |

code16 원본/설치본 SHA-256: `6FA61EA51C04AF5A8246E21183C7F4D9FDF0564FEEF5794553BEBEF7C1F4EFE1`. 설치 사전검사의36개는 패키지·해시·메타데이터·파일복구/provider 경로 시험이며 다른 서명 키 실물 시험을 포함하지 않는다. 구형 AOSP 이미지에 YouTube/Instagram은 없으므로47/47/46개 계측을 소셜앱 자동 넘김 E2E로 해석하지 않는다.

### D-031 · 계측 후 접근성 중단과09:31 복구

실제 OS 설치 뒤 네트워크 계측이 대상 앱을 종료시켜 접근성 서비스가 재연결되지 않았다. enabled 설정·오버레이 허용은 남았지만 `Bound{}`/자체서비스의 Crashed 항목과 상단 준비 안내가 확인됐다. exit-info09:26:47 `PACKAGE UPDATED`,09:27:22/23 계측 시작·종료 관련 `FORCE STOP`을 확인했으며 Java fatal crash는 관측되지 않았다. 제품 코드 크래시로 단정하지 않는다.

09:31 접근성 수동 OFF→ON 후 `Bound{자체서비스}`, `Crashed{}`, `runtime connected=true`, `enabled=false`, `blocked=false` 및 실제 화면의 준비 바로가기 소멸 확인. 제품 코드·APK·저장 설정 변경 없이 계측 후 연결 상태를 복구했다. 이 구간에서 소셜앱 자동 넘김 E2E를 새로 수행한 것은 아니다. [D-031](DEBUG_LOG.md) 참조.

재발방지 시험 순서: 파괴적 계측 후에는 enabled/bound/runtime과 사용 흐름을 확인한다. 실제폰 최종 설치 이후 계측을 재실행하지 않고 네트워크 시험은 설치 전 또는 에뮬레이터에서 수행한다. 최종 설치 뒤에는 APK 해시·버전·설정·연결 상태와 UI의 비파괴 확인만 수행한다.

## 2026-08-28 · 로컬 0.2.5-compat-test 호환성 후보

아래는 기존 공개판 및 이후 통합판과 구분하는 code14의 최초 호환성 후보 기록이다. [호환성 계약](COMPATIBILITY.md)을 검증하며 이 단계에서는 새 권한/감지 알고리즘 변경이 없다. 최초660364bytes 후보는 아이콘 패키징 누락이 뒤늦게 확인된 비최종 산출물이며, 후속660843bytes 수정 후보와 구분한다.

| 검사 | 결과 | 실제 범위 |
|---|---|---|
| BUILD / Compile | PASS | 제품 assembleDebug, compileDebugUnitTestJavaWithJavac, assembleDebugAndroidTest |
| 제품 Unit / Regression | PASS | 직접 JUnit 241개. 기존227 + OS정책14 |
| Lint | PASS | 0오류3경고; 기존 OldTargetApi/ObsoleteSdkInt/SecondsEditor SetTextI18n |
| 정적 API/권한 검사 | PASS | minSdk26, base/v34 XML, 캡처타입격리, API34팩토리, 기존시간제/광고/안전가드 |
| 독립 리뷰 | PASS | 지적한 계측시험 분기·구형라벨·권한설명 보완 후 재리뷰. 신규 C/H/M0 |
| 에뮬레이터 실행 | PASS(아래 범위) | Android8/API26 17검사, Android13/API33 17검사, Android14/API34 16검사 |
| UI·사용성 | PASS(관측 범위) | API26/34 세로1080×1920에서 지원 이유·스크롤·하단실행·수동 안내/시스템 추가요청창 육안 확인 |
| 타일 | PASS(제한 범위) | API26 추가 후 첫 탭으로 설정화면 복귀, 다시 펼치면 ‘권한 설정 필요’ 및 앱이름 포함 접근성 설명 확인. 실행ON/광고전용 실상태는 미실행 |
| APK 무결성 | PASS | v2서명, 산출물과3개 에뮬레이터에서pull한 설치본 SHA256/660364bytes 일치 |
| 실기기·소셜앱 E2E | 미실행 | 연결된 휴대폰 없음; 실제 타일·권한·자동넘김·장시간은 별도 |
| 새 Public / 메일 | 미수행 | 공개판0.2.4 변경 없음 |

후보 APK660364bytes, SHA256 `05E61E8FE4C8B657C3FE990487B73536B7C13A850B918147128A4FA8FC94FE01`. 구형 OS 설명/사용불가·설정보존은 제품 시험 범위이며, 기존 미연결 VisualSequence 실험2실패 및20연속 미완료를 해소하지 않는다.

계측 증거: 비공개 증거 자료, 20260828-085012-emulator-5556.txt, 20260828-085004-emulator-5558.txt. 각각 result=PASS를 직접 확인했다. 실제 Activity 초기화·저장된 ON이 미지원 화면에 ON으로 보이지 않음·설정 보존·기본기능 정책·API34 factory 로딩·구형 no-op·버전별 XML capability를 확인했다. 세 AOSP 이미지에는 YouTube/Instagram이 없으므로 실제 설치 호스트의 감지나 스와이프 성공 시험은 아니다.

화면 증거: 비공개 호환성 화면 자료의 top/bottom/support/dialog PNG, compat-5554-tile-after.png/XML. API26 타일은 에뮬레이터 cmd statusbar로 등록했고 실제 탭으로 설정화면 복귀를 확인했다. 최초 등록 직후에는 기본 이름, 서비스가 연결된 첫 탭 이후 상태 라벨이었다. 제조사별 전체 수동편집 조작·광고전용 타일의 최대글자 실화면·큰글꼴/가로/폴더블은 미실행이다. API34 요청창은 취소해 자동 추가되지 않음을 확인했다. API34 UIAutomator 초기 idle실패1회는 다음 대화창에서 정상 덤프했으며 화면 캡처 및 계측 성공과 구분한다.

최초 로컬 APK: `artifacts/shorts-loop-v0.2.5-compat-test.apk`(660364bytes,비최종·배포 제외). 계측 스크립트 `scripts/verify-compat-emulator.ps1`은 에뮬레이터만 허용한다. APK·테스트 APK 설치 외 접근성/플로팅/캡처 권한을 부여하지 않았고 자동실행을 켜지 않았다. 테스트용 가상기기는 종료하고 자료는 보존한다.

후속 아이콘 수정 후보: `artifacts/shorts-loop-v0.2.5-compat-test-verified.apk`,660843bytes,SHA256 `C52883CD9391786AF7F00137E065812813C23E51FD64D7BCCCDEE1C6262A7B45`. clean 빌드로 기본 anydpi와 v33 아이콘 자원을 함께 포함하고 완성 APK 패키징 검사 및 API26 실제 아이콘 표시를 확인했다. API26/33/34 계측18/18/17개 PASS. 이는 호환성 단계의 최종 수정 후보이며 이후 code16·17 통합판의 최종 배포본을 뜻하지 않는다. [D-029](DEBUG_LOG.md) 참조.

## 최종 0.2.4 · 광고 독립·기능별 UI

20연속미완료를명시하는시험판으로공개했다. 미완료를성공으로바꾸지않으며아래후보A/B의과거결과와합산하지않는다. 최종Public게시·CI·재다운로드결과는 [릴리스 기록](releases/v0.2.4.md)에기록했다. 소스2e89114의CI33074271656 제품227/실패0·빌드PASS·lint0오류3경고,익명APK681624bytes/원본·설치본SHA동일/v2검증PASS. 공개SHA256텍스트도원본일치. 최종155개로컬문서링크누락0·독립문서재리뷰PASS. 앱소스변경없이게시문서만후속커밋한다.

| 검사 | 결과 | 실제 범위 |
|---|---|---|
| BUILD / Compile | PASS | code13, version0.2.4, :app assembleDebug/compileDebugUnitTestJavaWithJavac |
| 제품 Unit / Regression | PASS | 직접JUnit227개; 타이머25·저장40, 광고정책8조건조합 포함 |
| Lint / 정적연결 | PASS | 0오류4경고; 실제0회분기 뒤 일반/시간제 차단, 그 앞 광고독립, 권한/창/전환안전가드 |
| 독립 소스 리뷰 | PASS | 신규 미해결C/H/M0; 오래된간헐정지·인식제한을해결완료로표현하지않음 |
| 실험분리 | PASS(구성) | 미연결VisualSequence 구현+시험을제품소스집합에서명시제외,참조/컴파일class/DEX부재검사. 별도실험20중18PASS/2FAIL 그대로보존 |
| APK / 서명 / 설치 | PASS | 681624bytes,v2서명검증,USB업데이트와설치본pull SHA256 일치 |
| 실제 UI | PASS(관측화면) | 기능별카드/초단위/0회광고예외/광고전용상태/전체토글 육안·탭제어; 커버1248×1972. 펼친화면·큰글꼴 미실행 |
| 실제0회광고 | PASS(1회) | 21:50:40.446요청1→21:50:41.529확인1,다른일반릴스확인;target0·adsON·전체ON |
| 0회일반/시간제 차단 | PASS(관측범위) | 초기15표본요청0,광고후21:51:14까지요청/확인1고정,timedON10이나IDLE/요청0,blocked=false |
| 광고OFF / 메인OFF | PASS(상태) | 옵션OFF는target0·enabledtrue보존/요청불변;메인OFF는adsON보존하면서enabledfalse·타이머중지·플로팅닫힘. 광고OFF에서같은광고재등장은미실행 |
| 연속20 / 광범위회귀 | 미완료 | 20회완료전시험판공개;최종YouTube E2E·모든정지/댓글/회전/큰글꼴/장시간은미실행 |

최종원본/설치본SHA256: `D2846EB1F935F5886DEE37CC5D2EA877C7E58DB0019EC0286E8AFEFA7DB92944`.

기기 숫자 기록과 자기 앱의 실제 화면 증거는 비공개로 보관한다. 광고진입을위한수동스와이프1회는자동성공에포함하지않는다. 광고요청·다른페이지확인과화면전후대조를함께확인했다. 광고 CTA클릭은하지않았다.

설정복구/정상회귀:기준·현재1,두앱/광고/플로팅/시간제ON10,화면보조OFF,전체ON으로복원했다. 21:55~56 restored기록에서33.932초정상영상이10초에잘리지않고N1끝에서요청/확인2→다음37.900초영상으로전환했다. 일반정상N1 실제회귀1회PASS이며20연속시험은아니다. 음량·음소거설정은변경하지않았다.

현재 scripts/verify.ps1은제품구현과같은시험범위다. scripts/verify-sequence-experiment.ps1이보존한실험20개를별도실행하며실제2FAIL을반환한다. 아래B의전체245시험/제외옵션설명은소스집합분리전의과거실행기록이다.

### 후보B 추가 관측·전환 집계 범위

21:32:31.823~21:37:39.561 같은generation13 비조작구간에서 총확인16→21,광고4→5,시간제1→2:5전환=정상3+시간제1+광고1. 앞선generation8구간과합산하지않는다. 긴142.569/167.666초일반영상에서도N회경로를유지했다. 정상·시간제·광고전환은종류별기록과함께총전환합계에포함하되20회미완료를명시한다. 원시파일timed-v024-b-twenty-run1.jsonl.

## 시간제 보조 B · 기본10초/최대60초

동작 계약과 후보별 증거는 [시간제 안내](TIMED_FALLBACK.md)를 따른다. 기존0.2.3/오디오 시험은 아래 이력으로 보존한다.

| 검사 | 결과 | 실제 범위 |
|---|---|---|
| BUILD/Compile | PASS | B code12, :app assembleDebug/compileDebugUnitTestJavaWithJavac |
| 연결된 기능 회귀 | PASS | 직접JUnit225개. 타이머25/설정38 포함 |
| 전체 시험 | FAIL(기존 실험) | B 전체245중243PASS/2FAIL. 미연결VisualSequence의2실패를 삭제/수정하지 않음 |
| Lint | PASS | 0오류/4경고 |
| 정적 안전 연결 | PASS | IG한정·real clock 우선·N0차단·strict다른identity확인·입력원문·권한/모듈 검사 |
| 설치/동일성 | PASS | B 659665bytes, 설치본과 원본 SHA256 일치 |
| 실제 입력/사용성 | PASS(검사 범위) | 60저장·상한+차단·61거부·10복구·키보드완료·단위/오류문구·설정보존 |
| 독립 코드 리뷰 | PASS | D-027 표시문제 수정, 새 미해결C/H/M0. 후속10/60 재리뷰도 완료 |
| 연속20개 실제 전환 | 미완료 | B 체크포인트 일반6(정상5/시간제1), 광고1별도. A일반12와합산금지 |
| 새 공개/메일 | 미수행 | 기존 공개0.2.2 유지 |

B 원본/설치본 SHA256: `29CEEE11A5F1101692BA3ED7122446C02349F5A94B7D0D75A6481CD4627D07D6`.

실제 B UI 화면은 최신 설치본에서 캡처하고 육안 확인했다. 61 입력이 원문으로 남고 저장값은60을 유지하는 오류 처리,60에서+무효,10 복구를 dumpsys와 함께 확인했다. 기본OFF는 신규/이전prefs 단위시험과 A 최초설치에서 확인했고 B에서는 기존에 켜진 시간제 선택을 유지한다. 정상43.604초 영상은10초 경과 후에도 기존 재생 횟수를 따르는 것을 관측했다.

시험 명령은 `scripts/verify.ps1`(전체), `-ExcludeUnwiredSequenceExperiment`(기존 미연결 실험20개만 명시적으로 제외, 전체PASS 아님)이다. 두 결과를 혼동하지 않는다. 재생 내용/계정이 포함된 화면은 private 밖에 게시하지 않는다.

### B 실제 동작 체크포인트 · 21:22:43

설치 뒤 실행ON부터 수동 이동/중간 실행 재시작 없이 요청7/확인7: 정상 횟수5, 시간제1, 광고1. 일반 영상6개 연속이며20개 달성은 아니다. 43.604/34.039/48.366/26.791/125.416초 정상 영상은10초로 잘리지 않고 기존 횟수 경로로 이동했다. 다음54.519초 영상에서 current1, blocked=false, timerinactive를 확인했다. 기존 A의 일반12개와 합산하지 않는다.

시간제 중 일시정지·댓글·잠금·회전·플로팅·0회 및 YouTube 실제 회귀는 이번 B 기기에서 미실행이다. 단위/연결/독립 코드 리뷰를 실제 E2E로 바꾸어 표현하지 않는다. 새 기능의60초 전체 대기 시험도 단위시험이며 기기에서60초를 끝까지 기다린 시험은 아니다.

문서 로컬 링크105개, 비공개 자료·산출물 Git 제외, 최신 APK/설치본 동일성 확인. 남은 범용 안정성 검증과 기존 실험2실패가 있어 새 Public 배포는 하지 않았다. 메일 요청/발송 없음.

## 별도 음향진단0.2.1 후보A · 미검출 경로 확인

한정모듈빌드/시험컴파일/lint PASS(0오류6경고),직접JUnit48PASS,범위·연결검사/독립리뷰PASS. code3/88989bytes/SHA256093881C3C8539762930A5389A764A1776A502E6A324D6D7D96864D8F912AC24F,USB설치·설치본pull일치. 실제시작/결과/스크롤UI육안확인. 기존38+신규10은합성시험으로서실제릴스인식률PASS아님.

OS동의후60.167초,958272표본/2724신호/598프레임중572유효/리셋0,후보0.119검색/776개별평가거부,WHOLE_QUALITY·FLAT_MINIMUM.수신및정상종료/Projection해제확인,반복주기검출은실패. [전체집계·원시증거·주의](AUDIO_PATTERN_TRIAL.md). 진단앱은영상신원을읽지않으므로동일영상의모든시점을원자적으로증명하는시험은아님. main2FAIL/20연속미달/새공개·메일없음유지.

## 별도 음향 반복 후보 시험 ·0.2-audio-pattern 후보A

- 두번째세션(진단자탭없음)은 최종60.162초/958272표본/2356신호블록/최대-10.75dBFS/특징578/초기화1/후보0. 관측은48.366초와59.987초부터종료까지이며초기전구간검증아님. 종료·Projection해제·IG재생확인. 별도기존화면프로브240프레임의10.400초는후보일뿐정확길이아님.10초미만이라는초기길이추정도미검증이므로긴영상원인단정금지. [수치·한계](AUDIO_PATTERN_TRIAL.md#두-번째-세션화면-대조--second-session-and-visual-control).

- [시험계약·사용법·상세결과](AUDIO_PATTERN_TRIAL.md). 기존제품 미교체, 새권한추가없음. Instagram UID+MEDIA/RAM전용/60초/사용자직접OS승인 유지, 자동넘김 미연결.
- `verify-audio-probe.ps1`:한정모듈 빌드/시험컴파일/lint PASS(0오류5경고), 직접JUnit38 PASS(새코어25+기존13), 범위·연결 정적검사 PASS. 독립 코드리뷰 미해결P1/P2지적0,PowerShell2스크립트구문오류0. main 미연결VisualSequenceTracker2FAIL은별개로유지.
- code2/name0.2-audio-pattern,APK65389bytes,SHA256 `67350933EAA04EA4A178CA3B5494086A93089CDCF0FB1236ED7A2A5B09D1C6C1`. USB설치Success·설치본pull해시일치·시작UI육안확인.
- 20:30~31 첫실제세션은 중간일시정지로 주기정확도 판정제외. 최종60.045초/957600표본/521신호구간/최대-11.49dBFS,후보0/초기화3/분석CPU221ms/최장7ms. paused화면/플레이어확인. 자동종료·서비스없음·Projection=null확인.
- 실제음악 주기대조/잠금·수동중지·OS취소/YouTube회귀·20연속은 이 버전에서 미검증 또는 미달. 새공개·메일없음.

## 별도 내부 오디오 수신 시험 ·0.1-audio-probe 후보A

후속결과독립리뷰: 실제도구관측수치와4개문서의수치/한계/복원을대조해불일치없음확인. 검토자가기기시험을재실행한것은아니다. 과거권한대기체크포인트는당시상태로구분한다.

**20:15 후속:** OS동의후내부PCM양성수신확인. 세션1:60.080초/955248표본/신호0(음소거조건). 세션2:45.635초까지0→IG음소거해제·재생후56.711초492신호구간/최대-11.07dBFS/peak17222→최종60.172초/957600표본/648신호구간/비영0표본22.99%. 양쪽자동종료후서비스없음/Projection=null확인. OS동의·실제수신·60초종료PASS,반복판정/정확N/20연속/잠금·수동취소는미검증. 아래19:04~05미실행은과거체크포인트다. [조건·한계·복원](AUDIO_PROBE_TRIAL.md#기기-수신-시험--device-capture-test). 코드변경없어빌드/단위시험재실행없음,기존13PASS와구분.

- 새 오디오 수집 권한이 필요한 시험을 별도 패키지로 분리. 기존 제품 설치·설정·권한불변. [계약·사용법](AUDIO_PROBE_TRIAL.md).
- `scripts/verify-audio-probe.ps1`: 한정모듈 assembleDebug/compileDebugUnitTestJavaWithJavac/lintDebug PASS, 직접JUnit13PASS,정적수집범위/권한검사PASS. lint0오류5경고(target35/backup규칙/진단아이콘/한국어문구2). 전체main회귀PASS로확대하지않는다. 기존 미연결 VisualSequenceTracker2FAIL은그대로다.
- 독립 코드리뷰: API29 Insets조건분기 지적반영 후 미해결P1/P2지적0. 수동중지/잠금/OS취소/직후재시작/회전동의복귀는 기기검증필요. 알림권한추가하지않아 OS가알림을숨길수있으며 최근앱→인앱중지안내포함.
- USB설치Success,version0.1-audio-probe/code1. APK56943bytes·SHA256 `0B3760F91D1B74AA60A2793159D281C103D710F0F6F754288E7FE5E7E4001174`,설치본pull대조일치. 실제시작화면/버튼표시/시작버튼터치→OS오디오권한창표시확인. 권한은대신부여하지않음.
- 19:04~05 `RECORD_AUDIO=false`, `running=false`, `samples=0` 확인. **실제오디오수신·60초자동종료·동의거부·OS중지·잠금·재시작 E2E는미실행**. 당시OS권한미허용상태.
- 실제 시작/권한 화면과 재수신 설치본은 비공개 증거로 보관. 개인정보화면은공개대상에서제외. 앱은원PCM/특징값을파일로저장하지않는다.
- 음악반복/정확N/자동넘김은미구현,20연속기준미달유지. Public업데이트/메일없음.

## 화면 분석 보조 · A/B/C 실제 설치 시험 (2026-08-27)

- 최종 B 빌드: assembleDebug/compileDebugUnitTestJavaWithJavac/lintDebug PASS, 직접 JUnit **186 PASS**, 정적 안전 검사6종 PASS. lint 0오류/3경고(기존 target, 하위API의 screenshot속성, 기존v26폴더). 기기 E2E와 별개다.
- 후보A/B 모두 업데이트 설치. B APK와 기기에서 다시 받은 base.apk 모두661363bytes, SHA256 `7B2710DB69CE439A5E1350D0113ACF1CFE0D3EDEC52989AC65411FF3E394B535`. 후보A는 별도 보존했다.
- 새 설정 기본OFF, 동의창 표시 중OFF·취소OFF·명시 확인ON을 기기에서 확인. B 업데이트 후에도 선택ON/기존목표1/두앱/광고/플로팅 유지, 실행은 안전OFF로 시작했다. 이후 실행ON은 화면의 스위치로 직접 조작했다.
- 후보A의 분석 미시작: 라우터에서 visualCandidate 누락. B의 `withIdentity` 보존과 새3개 JVM시험/정적 연결 검사/독립 리뷰로 수정. 실제 B에서는 frames가 증가하며 캡처 errors=0, 기존 문제 영상 분석이 시작됐다.
- 실제 동작 관측은 진행 중이며20개 연속/해결/공개 PASS가 아니다. A/B의 원시 숫자 로그·화면은 private에만 보관한다.

### C까지 관측한 결과

후속 미연결코어주의: 전체구간비교 VisualSequenceTracker는별도Java17컴파일PASS지만20시험중18PASS/2FAIL(39.15→37.35오추정,13.7초지터+노이즈학습실패). C설치본에는포함안됨.아래193시험PASS를현재미연결실험포함전체PASS로해석하지말것.

- C는 원인 진단 숫자만 추가한 동일 앵커 방식.193개 직접JUnit·Build/lint PASS.662205bytes, SHA256 `59C7CD4BCFDC35698348447C635FBD42AA7FF0BE27E124072DA0CCFAE1A97BC2`, 설치본 재수신 해시 일치. 독립 정적 검토는 동작불변/숫자전용 출력 확인, 별도빌드 미실행.
- B 첫 문제영상:18:36:43부터130초 학습해도 기준 장면 없음.18:38:53부터LEARNING_TIMEOUT,frames281이 이후13초간 고정,요청0. `…/1`→`?/1` 실제 화면 확인. 자동 넘김 실패를 안전정지 성공으로 바꾸어 보고하지 않는다.
- B 다음 무시간영상들:24.514초 후보 후초기화;8.367/24.696초 후보 후초기화;39.145초 후보 후LOW_CONFIDENCE와학습제한. 일부는 진단 목적 수동으로 종료했고 완주시험이 아니다.
- C 같은네번째영상 재관측:7.935초 후보의PERIOD_CHANGED 및15.552초 후보의INTERVAL_MISMATCH. 해당 시점restartIdleMs0,움직임비율0.664/0.867,errors0이므로 그 구간은 정지 감지나캡처실패가 아니라 반복 복귀/간격 검증 실패다. `v023c-same04-resume.jsonl`.
- 일시정지: B visual=false/frames866 고정·요청0, C 재설치 후 일시정지 그대로 frames0/요청0. 재생버튼 직접 탭 후 새학습시작 확인. 캡처·이동이 일시정지 동안 발생하지 않았다.
- 정상 시간 우선: 비교06에서0.512→5.855/16.715초,current1,visual=false/frames218고정. 다음으로 수동이동했으므로 해당영상 완주PASS아님.
- 별도 비조작 정상연속:비교08(25.475초)→17.865초 영상→다음 무시간원 영상,18:48:10.859/18:48:29.469 두 번 실제 전환확인.요청/확인2/2,광고0/화면추정0. 그다음그림형영상은STATIC학습초기화로정체. **20연속 기준 미달**, 전후수동비교구간은연속성공에포함안함. `v023c-normal-advance.jsonl`, `v023c-normal-chain2.jsonl`.
- C까지 화면분석 요청/확인0/0. 기존시간기반2회성공을화면분석해결로주장하지않는다. 단일앵커대신전체시간구간상관비교를별도코어로시험준비중이며아직기기에적용하지않았다.
- 후속 비교11의65.803초→69.633초→34.084초가진행해18:52:55누계요청/확인4/4.앞선2회와사이수동이동있으므로4연속아님.18:53:27~30 화면보조OFF·기존실행ON복원,frames517고정과시간진행확인. 원시 `v023c-long-normal`, `v023c-audio-policy-check`, `v023c-checkpoint-before-ui`, `v023c-visual-off-restored`.
- 대체시간원오디오조사:공식문서/IG패키지flag/최근재생기usage만확인,실제오디오캡처/새권한/녹음미실행.실제수집에는OS권한동의필요. YouTube새기기회귀/20연속/전체보조모드E2E는미검증또는미달.

아래0.2.2/C차의 제품 미수정·설치 없음은 과거 시점 기록이다.

## D-022 C차 · 화면 주기 대조·20개 연속 기준 (2026-08-27)

제품 소스/권한/APK는 그대로이며, [새 개발용 도구와 원시 근거](INSTAGRAM_TIMING_RESEARCH_2026-08-27.md)를 별도로 검증했다.

| 검사 | 결과 | 범위/제한 |
|---|---|---|
| Event/Visual 진단 Java·dex | PASS | Java8/구형API 경고 남음. 기기 사용한 이벤트v1·화면v2 JAR 로컬/기기 해시 일치 |
| 합성 수학 시험 | 3 PASS |8초 주기/전체 정지/빈 특징 거부. 제품 카운터 시험 아님 |
| 재빌드 안내·정적 위생 | PASS |README 통합 컴파일/dex/3시험 직접 실행, PowerShell 구문·문서 로컬 링크59개·diff 공백·제품 소스 무변경/private 제외 확인. 통합JAR 기기 미실행 |
| 숫자 이벤트 | 대체 시간 미확보 |18초 재생0건. 수동 일시정지 대조9건 수집, 시간 숫자 없음 |
| 무재생바 영상 화면 후보 | 가능성 확인 |A36초144표본·32초128표본 모두10.4초;B32초128표본14.75초. 실제 전체 길이/종료점 미확정 |
| 정상 실제 길이 대조 | 근접 후보 확인 |D Range16.205초,38초152표본 후보16.2초.250ms표본/50ms검색격자이므로5ms 정확도 입증 아님 |
| 정지 화면 오인식 대조 | Java 후보 없음 |8초32표본 motion0. PC 초기 도구는 정지화면에도 후보를 내므로 별도 한계로 유지 |
| 기존 앱 비조작 연속 시험 | **20개 기준 실패** |정상1회 자동 이동 후 다음 영상0/1·정보 없음. 요청/확인1/1, 광고0. 실패 영상을 수동으로 제외하지 않음 |
| 기능·시각·UX/독립 리뷰 | 한계 확인 |실제0/1·재생바 화면 대조. 약2.1주기 학습/실제 시작점 불명/내부 반복 위험. UI·제품 수정 없음 |
| 최종 독립 문서·증거 리뷰 | PASS |45+8표본/1회 이동 후 정체/정지 요약의 한계/세 JAR 해시/59링크/app 무변경 확인. 검토자는 빌드·기기 조작하지 않음 |
| 제품 Build/Unit/Lint/수정 후 회귀 | 미실행 |새 제품 수정 없음, 과거148시험을 이번 PASS로 사용하지 않음 |
| 새 capability/설치/게시/메일 | 미수행 |캡처 capability 추가·시험 설치 전 단계. 조건 미달로 새 공개 없음 |

최종18:01:25 확인: 실행ON·목표/기준1·두앱선택·광고/플로팅ON. 막힌 영상에 그대로 있음. 캡처는 private에만 보관. 제품 D-019/D-021/D-022 미해결.

## D-022 대체 시간원 조사 · 2026-08-27 (제품 미수정)

상세 출처/원시 로그/한계는 [조사 문서](INSTAGRAM_TIMING_RESEARCH_2026-08-27.md).

| 검사 | 결과 | 범위 |
|---|---|---|
| 조사 도구 javac/d8·기기 해시 | PASS | 제품 밖 TimingProbe. Java8/구형API 안내 경고 남음, 최종 d8 추가 경고 없음 |
| 보강된 문제/정상 대조 | 실행 완료 | 문제114노드4표본 ranges/time/state0, 정상116노드4표본 RangeInfo1·10.707→13.132/26.1초. 양쪽 stale/truncated0 |
| 일시정지 UI·MediaSession | 대체 시간원 미확보 | 초기 일시정지3표본 시간원0, 당시IG세션 없음. 모든 내부 시간원 부재는 미검증 |
| 이벤트·절단·전환 중 표본 | 판정 불가/제외 | 이벤트 빈 결과, 초기 출력 절단 및 수동 이동과 겹친 조회는 성공 증거로 사용하지 않음 |
| 복원·시각 확인 | 확인 | 17:41:41~44 실행ON,목표/기준1·두앱·광고/플로팅ON.10.014→12.848/26.1초. 서비스 재생성으로 전후 누계 분리 |
| 독립 조사 도구 리뷰 | 보강·재시험 | 초기 전체문자열 매치와 자식별 갱신 한계 보완. 범위 한정된 진단이며 제품 해결 판정 아님 |
| 제품 Build/Unit/Lint/전체 E2E | N/A/미실행 | 제품 소스/설치 변경 없음. 개발 도구 컴파일·실기기 출력만 새 실행 |
| 새 권한·제품 보조 모드·게시·메일 | 미수행 | 정확한N회 대체 시간원 미확보. 별도 초 간격 보조 모드 미구현 |

## D-022 USB 진단 · 2026-08-27 (제품 미수정)

아래 게시 당시의 설치/디버깅 보류 기록 이후 USB 진단을 재개했다. 실제0.2.2/code9를 확인했으며 이번 진단에서 설치한 것은 아니다. 상세 표본·제약·비공개 증거 경로는 [D-022](DEBUG_LOG.md)에 기록한다.

| 검사 | 결과 | 범위/제한 |
|---|---|---|
| USB 서비스 상태·실제 버전 | 확인 | 0.2.2, 접근성 연결/실행true. 실패 구간 blocked=false |
| 12개 짧은 비교 구간 | 증상 재현 | 02/05는0/1·재생 정보 없음, 나머지10구간은 현재1·시간 진행. 구간 사이 수동/자동 이동 혼재; 모든 항목 완주 시험 아님 |
| 실패 후 다음 일반 릴스 | 복구 확인 | 별도 토글/재시작 없이 다음 영상 시간 읽기·현재1 복구 |
| 별도 정확 scrubber 조회 | 미검출 확인 | 제품OFF인 두 장면 각각14표본. 한 장면은 직전 제품ON의0/정보 없음 확인. 모든 대체 시간원을 조회한 것은 아님 |
| 같은 프로브 정상 대조 | 확인 | fresh/visible=true, min0/max26100/current10203/type0, 탐색 지연으로1표본 |
| 실행·설정 복원 | 확인 | 실행ON, 두 앱 선택, 목표/기준1, 광고/플로팅ON. 8표본11.207→15.640/26.1초 정상 진행. 중간 재생 시작 대기0은 미검출0과 구분 |
| 기능·시각·UX 리뷰 | 진단 한계 확인 | 재생 막대 유무와 상태 화면 육안 대조. 플로팅0만으로 대기 원인을 구분하지 못함; UI 변경 없음 |
| 독립 증거·원인 리뷰 | 완료 | 노드 미검출/RangeInfo 거부 구분, 관측 공백·시간원 범위·서비스 재생성/누계 초기화 제한 대조 |
| 새 BUILD / Unit / Lint | N/A (미실행) | 제품 소스 변경 없음. 기존148시험 결과를 이번 새 실행으로 보고하지 않음 |
| 수정 후 E2E·전체 회귀·원본 링크 재현 | 미실행 | 진단 전용, 새 수정 없음. 기존 D-019/D-021 및 이번 D-022 미해결 |
| 새 공개 게시·메일 | 미수행 | 문서 로컬 갱신만, 실제 화면/로그는 공개 제외 |

## 공개 배포 확인 · 2026-08-27

| 검사 | 결과 | 증거 |
|---|---|---|
| 저장소 가시성 | PUBLIC | [fullmetalsonic/shorts-loop](https://github.com/fullmetalsonic/shorts-loop), API visibility=PUBLIC |
| 소스·태그 | PASS | v0.2.2 →95a4239844ba35a929b2719e8e935b7eb2d3ea14, 원격 ref 대조 |
| 릴리스 | PASS | [v0.2.2](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.2), draft=false, prerelease=true, APK+SHA256 2개 첨부 |
| 익명 공개 접근 | PASS | 인증 헤더 없이 저장소/Release HTTP200 및 APK·checksum 다운로드 |
| APK 무결성 | PASS | 아래 로컬 빌드·보존본·공개 다운로드 모두627713bytes/동일SHA256, checksum파일도 동일 |
| 패키지·서명 | PASS | aapt0.2.2/code9/min26/target35/overlay권한만, apksigner v2검증. aapt는 로컬 한글경로 오류를 동일해시 ASCII임시복사로 우회 |
| Linux CI 빌드·시험·lint | PASS | [run33049522094](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33049522094), head95a4239, conclusion=success |
| Linux Gradle JUnit | PASS | 보고서 다운로드 후 XML합산148tests/0failures/0errors/0skipped |
| Linux lint | PASS | 오류0/경고1(OldTargetApi). 비어 있는 v26폴더는Git에없어 로컬보다경고1개적음 |
| CI 도구 경고 | 남음 | Node20 기반Actions의Node24강제실행/SetupJava4폐기안내. 작업실행은 성공; 보안문제가 없다는 포괄보증은 아님 |
| 공개 안전·문서 | PASS(검사 범위) | 82공개파일의경로/텍스트검사·독립리뷰·링크검사, 비공개 자료·산출물/SDK/참고원본/서명키제외. 파일2개PNG는생성아이콘 |
| 소스 일치 | PASS | 공개 전 worktree와HEAD를git hash-object --filters로대조, remote main/ref확인. 이후 게시증거 문서만 갱신 |

배포검증과 실기기 안정성은 구분한다. 새0.2.2 설치·배터리 메뉴 실제 터치/시각/복귀·자동 넘김 재시험은 보류, D-019/D-021미해결. 공개APK는CI재서명본이아니라아래해시의로컬개발서명본이며CI에는검사보고서만첨부한다. 메일 발송 없음.

## 0.2.2 최신 검증 · 배터리 안내 및 공개 준비

| 검사 | 결과 | 범위/제한 |
|---|---|---|
| assembleDebug / 시험 컴파일 | PASS | verify.ps1 BUILD SUCCESSFUL |
| lintDebug | PASS | 로컬 오류0/경고2: OldTargetApi, 비어 있는 mipmap-anydpi-v26의 ObsoleteSdkInt |
| 직접 JUnit | PASS | 148개, 실패0 |
| 정적 안전 검사 | PASS | 권한·core 모듈 / 숫자 입력 / 전환 중단 / 배터리 설정 연결. 권한·모듈은 하나의 출력 항목 |
| 독립 변경 리뷰 | PASS | BatterySetupPanel·연결·문구·예외처리. 새 P1/P2 없음; 제품 전체 미해결은 별도 |
| 새 메뉴 기능·시각·사용성 | 정적 리뷰 PASS / 현장 검증 필요 | 자체 앱만 열기, 48dp 이상 버튼, 명시적 수동 안내. 실제 터치·잘림·복귀 표시는 미실행 |
| 0.2.2 휴대폰 설치·자동 넘김 회귀 | 미실행 | 이 단계에서 추가 기기 디버깅 보류. 마지막 설치본0.2.1 |
| Windows Gradle testDebugUnitTest | 기존 환경 FAIL, 미재실행 | 한글 경로 로딩 오류의 직접JUnit 우회 사용. Linux CI는 별도 기록 |
| 미해결 제품 문제 | 있음 | D-019 전환 실패, D-021 반복 경계 누락. 안정성 완료 아님 |
| 웹 화면·회사메일 | N/A / 미수행 | 네이티브 Android / 메일 요청 없음 |

산출물:0.2.2/code9,627,713bytes, SHA256 `38A283A6780295BB30E1482ED2535C5FE4204B4B5F0180D2C3326D60E3675B58`. 개발 서명 APK. 공개 다운로드/서명/CI 결과는 위 공개 배포 절에 기록했다.

### D-020 후속 실기기 시험 · 0.2.1 설정 변경, 코드 미변경

Android 앱 정보 → 배터리에서 자체 앱만 '최적화'에서 '제한 없음'으로 변경. 설정 화면 육안, deviceidle whitelist 수동 예외, kernel cgroup.events frozen=0 확인. 동일 프로세스/실행 상태 유지이며 적용 시 실행 OFF→ON하지 않음.

-16:09:20.257~16:10:15.977, 약56초, 비공개 증거 자료50표본. PiP+홈 대기에서 서비스 응답 모두 수신, blocked=false, 누계 요청16/확인15 유지, kernel frozen=0. 폴링 세대1351→1534.
- 전체 화면 복귀 후 시간 감지 재개. 이것은 PiP 안에서 자동 넘김 성공을 뜻하지 않는다.
-16:10:32.840~16:11:26.508, v021f-fullscreen-resume.jsonl65표본에서18초 영상16→0 반복3회와 카운트 누락 확인(D-021). 이후16:12:15.687~16:12:52.662 v021f-user-restart-observation.jsonl45표본은요청18/확인16/blocked=true, kernel frozen=0. 배터리 동결과 전환 확인 실패는 별개.
- 파일명의 user-restart는 관측 라벨일 뿐 실제 수동 재시작의 증거가 아니다. 일시적인 복구나 짧은 정상 관측만으로 전체 안정성 PASS로 기록하지 않음.
- 장시간·재부팅·다른 기기 효과는 현장 검증 필요. 개인 화면/로그는 공개하지 않는다.

아래0.2.1 이하 기록은 당시 상태이며, 가시성 대기·배터리 미조치 등 과거 표현은 위 최신 기록으로 대체한다.

## 추가 진단 · YouTube 간헐 정지 (제품 미수정)

후속16:01 전체 화면 복귀 캡처/topResumedActivity 확인,진단 timeout 지속.16:04 자체 앱 커널 cgroup.freeze=1/events frozen=1 및Samsung Freecess 동결 목록 확인. MARs기록15:56:30 Bg동결→16:01:08 접근성Binder해제→16:01:14 재동결. 진단SIGQUIT에의한16:03:59 일시해제 후16:04:05 재동결도 확인. D-020은 현재 무응답의 동결 원인 확인, 절전 예외 조치/재발방지 수정은 미실행. 독립 증거해석 리뷰 완료. 아래 최초 ActivityManager isFrozen=false만으로 실제 동결이 없다고 해석하면 안 됨. 최초15:46 요청실패와 동일 원인인지 미확정.

15:56 이후 PiP 시험: 홈+YouTube pinned/PiP 창 확인, 첫 상태 blocked=false·launcher 대기·누계16/15. 이후 Samsung Internet 전면에서 서비스 진단 응답 timeout이 반복되어 연속 관측 실패. ADB/PID/접근성 등록은 유지. PiP 원인이나 제품 크래시로 확정하지 않았으며 전체 화면 복귀 비교 대기. D-019 추가 항목 및 비공개 증거 자료 참조.

2026-08-27 15:47~15:51,0.2.1의 접근성 연결/실행true이나 blocked=true,요청11/확인10,넘김 확인 실패 상태 유지. PiP가 있었던 정황은 있으나 최초 요청 실패의 직접 트리거는 미확정. 세부 내용은 [D-019](DEBUG_LOG.md) 참조.

15:51 진단자가 자체 빠른 설정 타일을OFF→ON하고 패널을 닫은 후 비조작 재생 관측. 15:53:02.493 요청12 →15:53:03.298 확인11/58초, 이후58→15→11초 전환으로15:54:24.564 누계14/13,blocked=false.59초 시작부터 추가요청3/확인3 성공이며 남은 누계 차이1은 이전 실패. 실제 전환 장면과 다음 영상 화면도 육안 확인. v021d-fullscreen-observation/fullscreen-transition/second-transition JSONL 보존. 최초 간헐 실패의 재현 및 근본 해결은 미완료, 아래 과거 PASS를 전체 안정성으로 해석하지 않음. 이번 코드/APK/권한 변경·빌드·새 단위시험 없음.

과거 PiP종료 기록 정정: v021c-settings-clean에는 PiP가 남아 있었으며 최종 Instagram 캡처에 없다는 사실만 확인됨. 앱 완전 종료를 확인한 것은 아님. 이 단계에서는 GitHub 공개 범위 미정으로 게시하지 않음.

## 최신 0.2.1 · 광고 토글 구현·설치 및 실제 시험

2026-08-27, Fold8 커버1248×1972. 과거 잠금 상태/0.2.0 candidate-c 기록은 아래에 보존하며 최신 상태가 아니다.

| 검사 | 결과 | 근거/제한 |
|---|---|---|
| 빌드·Java 시험 컴파일 | PASS | 최종 candidate-c verify.ps1 BUILD SUCCESSFUL |
| lintDebug | PASS | 오류0, 기존 경고2(OldTargetApi/빈 v26 폴더) |
| 직접 JUnit | PASS | 148개, 개발 검증 및 독립 재실행. Gradle 기본 test task는 과거 환경 FAIL이며 별도 |
| 권한·모듈·숫자입력·전환보호 연결 | PASS | 인터넷 권한 없음, core Android 비의존, 입력 원문 및 비종료 중단 경로 정적 검사 |
| 독립 코드 리뷰 | PASS | 광고 전환중 회전/드래그 보호취소 P2 수정 후 재리뷰. 정적 범위 미해결CRITICAL/HIGH0 |
| 0.2.1 설치·서명·재추출 SHA-256 | PASS | candidate-c install-r Success, apksigner v2, 설치본과 동일 |
| 영상형 광고 인식 | PASS (관측 형태) | clips_ufi_component의 우하단 광고 배지. 재생바가 있어도 광고 판정 |
| 광고 OFF/ON 실제 비교 | PASS (candidate-b) | 같은 영상광고 OFF 6표본 요청0 → ON 광고 요청1/확인1 → 다른 일반26.799초 영상. CTA 클릭 없음 |
| 최종 설치본 광고 재확인 | PASS (candidate-c) | Instagram 복귀15:39:49.476 광고요청1/pending →15:39:51.097 광고확인1·일반20.454초 재생. 총요청/확인5/5. v021c-final-state 숫자 로그 |
| 광고 후 일반 N1 재개 | PASS (candidate-b) | 캡션 #광고만 있는26.799초 영상 즉시 넘기지 않음. 완주 뒤37.313초 영상, 총요청2/확인2·광고요청1/확인1 |
| 0회에서 광고 포함 중지 | PASS (candidate-b) | 실제 같은 광고 화면에서6표본 target0, 총요청2/확인2 및 광고요청1/확인1 유지. app/position은 마지막 값이라 화면 증거와 함께 판정 |
| Instagram 미선택 | PASS (candidate-c) | target1/adsON/실행ON,6표본 선택앱대기·요청0. 이 시험 화면은 일반 릴스이며 광고 위 미선택 실제 시험으로 확대하지 않음 |
| YouTube N1 회귀 | PASS (candidate-c 관측 구간) | 43초→5초→다음43초, 요청/확인1→2→3, 광고요청0. 전체 신규 앱 호환성 보장 아님 |
| 빠른 설정창·타일 | PASS (candidate-c) | 창 열림 동안 대기, 총요청4/확인4 유지. 실제 타일OFF→비활성 배경/enabled=false, ON→활성 배경/enabled=true·목표1/플로팅false 보존 |
| 입력·0/99·100거부·화살표·IME | PASS (0.2.0 c~e) | 커버 실제 입력/완료/키보드/값복원 확인. Clipboard 붙여넣기 자체는 미실행 |
| 플로팅 두 모드·드래그·X·표시선택 | PASS (0.2.0 c~e) | N3 순환·간편, 드래그 값불변/위치저장, X OFF, 재표시 위치복원, 표시OFF에서도 실행ON |
| 라디오 오른쪽 여백/컨테이너 강조 | PASS (0.2.0 d/e) | 실제 빈행 터치 선택 및 완료 후 회색 전체강조 제거 육안 확인 |
| 상단 종료형 광고 | 부분 확인 | candidate-a에서 광고OFF 인식 확인. 해당 형태의 최신 자동 넘김 E2E는 미실행 |
| 일반 N2→무시간 광고 | 부분 확인 | 0.2.0에서21.708초1/2→2/2→광고 실제 이동, 구버전 확인 실패. 새 recognized 경로는 단위시험, 같은 흐름의 최신 실기기 반복은 필요 |
| 전환 대기 중 회전·드래그 | 현장 검증 필요 | 3개 정책시험/정적 연결/독립 리뷰 PASS이나 실제 동시 타이밍 시험은 미실행 |
| 무시간 일반릴스·연속광고·별도팝업 | 지원 한계 | 시간 없는 일반릴스는 수동. 식별불가 연속광고는 안전정지 가능. 별도 창 자동닫기 미구현 |
| 실제 권한 제거·폴드·큰글꼴·장시간 | 현장 검증 필요 | 기존 권한 임의제거 없음, 기기 상호작용/장시간 측정 미실행 |
| 웹/1920×1080·GitHub·메일 | N/A·미수행·미수행 | Android native, 외부 게시/발송 요청 없음 |

### 최신 산출물

- 0.2.1/versionCode8, 개발 서명 APK,644,351bytes.
- 빌드본·candidate-v021-c.apk·installed-v021-c.apk SHA-256: `8932952A74FBCB6B131A6D020DA8385CEF86701B231EB4AB996F092E9B7B1799`.
- 보존 산출물 artifacts/shorts-loop-v0.2.1-debug.apk도 동일 크기/해시. 정식 외부 공개 릴리스 아님.
- 실제 증거는 비공개 기기시험 자료의 v021b-video-ad-off/on, v021b-ordinary-after-ad, v021b-zero-ad, v021c-ig-not-selected, v021c-youtube-regression 숫자 JSONL과 비공개 화면 자료의 동일 접두 화면. 개인 영상/계정 원문은 문서에 옮기지 않음.
- 광고 토글은 기본OFF, 시험 후 설정은 별도 최종 상태로 기록. OFF면 광고 수동 넘김이라는 안내를 실제 UI에 추가.
- 최종 폰 상태: 실행ON, 현재/기준1, 두 앱선택ON, 광고ON, 플로팅OFF(기존 조정 값 보존). 최종 설치본에서 광고1회 이동 후 일반릴스 재개. 시험용 YouTube PiP는 닫고 Instagram 복귀. v021c-settings-clean 실제 안내문 화면 확인.
- 하단 고정 토글의 높이 축소/명칭 개선은 검토안이며 미적용. 영상 내 CTA 카드가 있는 광고는 지원 시험을 통과했지만 별도 창 팝업 닫기로 확대하지 않음.
- 문서 링크 검사 및 독립 문서·산출물 대조 PASS. 검토자는 네 APK 크기/해시·버전·b/c 시험 구분·YouTube/타일/미선택 원시 로그를 직접 대조했다. 최종 상태를 추가 기록했고, 마지막 Instagram 화면에서 일반 릴스와 PiP가 없는 상태를 육안 확인했다.

## 이전 0.2.0 · 잠금 해제 전 구현/설치 이력

2026-08-27 최종 candidate-c. 아래 이전0.1.5 표는 당시 이력이며 최신 제품 통과로 승격하지 않는다.

| 검사 | 결과 | 근거/제한 |
|---|---|---|
| assembleDebug / 시험 Java 컴파일 | PASS | 최종 verify.ps1 BUILD SUCCESSFUL |
| lintDebug | PASS | 오류0,경고2. 기존 target35 OldTargetApi 및 비어 있는 mipmap-anydpi-v26 폴더의 ObsoleteSdkInt |
| 직접 JUnit | PASS | 120개,실패0. 독립 검토자 -SkipBuild도120개 PASS |
| 권한·모듈 / 숫자 입력 구성 | PASS | SYSTEM_ALERT_WINDOW만 유지,core Android 의존성 없음. 원문을 변형하는 숫자 입력 필터 재도입 정적 감시 |
| 설정 계약·회귀 | PASS (JVM 범위) | N/M·0~99·순환/간편·마이그레이션·메모리 SharedPreferences21개·Instagram 시간정책·null root 전환보호 포함 |
| 독립 코드 리뷰 | PASS (정적 범위) | 조회공백/복원/입력필터 지적 수정 후 재리뷰,미해결CRITICAL/HIGH0. 실제 OS상호작용 대체 아님 |
| APK 업데이트 설치 | PASS | adb install -r Success,설치본 base.apk 재추출·해시 일치 |
| 설치 서비스/기존 설정 | PASS (조회 범위) | 0.2.0 연결true,실행false,기준/현재2,모드0,플로팅true. 위치·기존 설정 보존 확인 |
| APK 서명 | PASS | apksigner verify, v2=true,signer1 |
| 최신 인앱·키보드·붙여넣기·아이콘 육안 | 검증 불가 | 휴대폰 보안잠금 showing=true,inputRestricted=true |
| 최신 플로팅·두 탭 모드·타일·저장 복원 E2E | 현장 검증 필요 | JVM 계약과 설치 조회만으로 실제 터치/디스크 복원 통과 선언하지 않음 |
| 최신 YouTube 자동 넘김 회귀 | 현장 검증 필요 | 이전0.1.4/0.1.5 실제 성공은 아래 별도 이력 |
| Instagram 서비스 자동 넘김 E2E | 현장 검증 필요 | 이전19.014/22.638초 진행·반복 관측은 탐지 가능성 조사이며 제품 스와이프 성공 아님 |
| 실제 플로팅 권한 없는 실행 | 현장 검증 필요 | 조건부 권한 코드/정책 검토. 기존 권한은 제거하지 않음 |
| 폴드·회전·큰 글꼴·장시간 | 현장 검증 필요 | 실제 장치 조작/측정 미실행 |
| Gradle testDebugUnitTest | 과거 환경 FAIL,미재실행 | 한글경로 ClassNotFoundException 우회 직접 JUnit 사용. Gradle시험 PASS 아님 |
| 웹 UI / GitHub / 메일 | N/A / 미수행 / 미수행 | Android 네이티브;외부 게시·메일 요청 없음 |

### 최종 산출물 일치

- 버전0.2.0,versionCode7,개발 서명. 정식 릴리스로 배포하지 않음.
- app/build/outputs/apk/debug/app-debug.apk:634,888bytes.
- 비공개 증거 자료 및 installed-v020-c.apk 모두 같은 크기/해시.
- SHA-256: `A2F01BA87B7203B2B80047BE0861F0AE5F4B433B078DBFA000C69A1E9EDE44BE`.
- 설치 후 진단: connected=true enabled=false target=2 current=0 blocked=false,pending=false,requests=0 confirmed=0,ceiling=2 tapMode=0 floating=true.
- 새 캡처를 실제 인앱 화면으로 제시하지 않음. 잠금화면만 확인했으며 앱 UI 시각/인간공학 PASS 판정 보류.
- 현재 문서/소스0.2.0 일치,이전 버전 APK·설명/증거는 이력으로 보존. 도구 정책으로 차단된 빈 리소스 폴더 제거는 재시도하지 않음.
- 최종 문서 링크 검사 PASS. 독립 검토자가 README/HANDOVER/검증/누적이력의 미검증 구분,versionCode7,세 APK의634,888bytes·동일SHA-256,실제lint0/2를 재대조하여 PASS. 폰/서명은 기존 실행증거 대조이며 검토자가 재실행했다고 표시하지 않음.

## 이전0.1.5 검증 이력

## 범위와 판정

**기능 시험판. 실제 자동 넘김 확인, 전체 실기기/장시간 검증 완료 판정은 보류.**

| 검사 | 결과 | 근거/제한 |
|---|---|---|
| assembleDebug | PASS | AGP 8.7.3 / Gradle 8.9 / SDK 35, 개발 서명 APK 생성 |
| Java 소스/시험 컴파일 | PASS | compileDebugJavaWithJavac / compileDebugUnitTestJavaWithJavac |
| lintDebug | PASS | 오류 0, OldTargetApi 경고 1: target SDK 35. Android 17 기본 동작 확인, 전체 호환 범위 추가 시험 필요 |
| 직접 JUnit 4.13.2 | PASS | 61개, 실패 0. scripts/verify.ps1 및 독립 -SkipBuild 실행 |
| Gradle testDebugUnitTest | FAIL (환경) | 한글 경로 테스트 클래스 ClassNotFoundException. 실제 컴파일 파일 존재. 직접 JUnit은 당시54개, 최신61개 정상 실행 |
| 단위·회귀 시험 | PASS | 파서12, 모드5, 반복23, 시간갱신6, 전환보호11, 위치4 |
| 권한·모듈 구조 검사 | PASS | SYSTEM_ALERT_WINDOW만 요청, core Android 의존성 없음 |
| 독립 코드 리뷰 | PASS | 초기 P2 2건 수정. 0.1.2/0.1.4/0.1.5 재리뷰 PASS, 검토자 JUnit61 및2배속 양자화 독립 재현 PASS. 검토 범위 미해결 CRITICAL/HIGH 0 |
| 자동 스와이프 E2E · 목표2 | PASS (0.1.4 및0.1.5) | 0.1.4의52초, 최신0.1.5의42초 영상에서 각각1/2→2/2→다음1/2 확인. 같은빌드3개 연속 기준은 미충족 |
| 자동 스와이프 E2E · 목표1 | PASS (0.1.4, 3개 연속) | 21초→45초→30초 쇼츠를 각1회 후 다음42초 영상까지 이동. 추가 요청3/확인3 |
| 플로팅 시각·드래그·X | PASS (커버 화면) | 숫자0/1/2 순환, 드래그 후 목표2 유지, 상대 위치 저장·재시작 복원, X 종료 확인 |
| 빠른 설정 타일 색상·ON/OFF | PASS (0.1.4) | 이미 등록된 타일 확인, ON은 흰 활성 바탕/목표2 복원/플로팅 표시, OFF는 비활성/숨김, 길게 설정 진입 |
| 목표0 자동 넘김 중지 | PASS (0.1.4) | 약87초, 53표본에서0/0 및 요청4/확인4 유지. 같은42초 영상의 진행화면 변경과 유지 육안 확인 |
| 빠른 설정 가림·PiP/다른 앱 대기 | PASS (0.1.5) | 빠른 설정6표본 대기/요청0 유지, 다른 앱+PiP 대기, 전체 YouTube 복귀 후 감지 재개 |
| 일시정지·댓글·앱 전환 | PASS (기본 동작) | 24/42초 일시정지8표본 동안 요청 증가 없음. 댓글창에서0/1 및 대기, 앱 전환 중 조회불가 대기 |
| 수동 탐색·모든 특수 메뉴·잠금 | 현장 검증 필요 | 기본 댓글/일시정지 시험을 전체 특수상태 검증으로 확대하지 않음 |
| 폴드 접기·펼치기·회전 | 현장 검증 필요 | 위치 계산 단위시험만 실행 |
| 배터리·장시간 사용 | 현장 검증 필요 | 실제 기기 장시간 세션 없음 |
| 웹/1920×1080 브라우저 | N/A | Android 전용 앱 |
| GitHub/메일 | 미수행 | 당시 개발·로컬 검증 단계 |

## 구현 전 관측 근거

재생 중인 일반 YouTube 쇼츠를 ADB 읽기 전용으로 관찰했다. SeekBar content-desc는 `0분 35초 중 0분 8초` 형식이었다. 31→33→0, 31→33→35→0으로 두 번의 반복을 관찰했다. 이는 정보 조회 가능성의 증거이며, 제작 APK 서비스/스와이프 성공의 증거는 아니다. 개인 영상 제목/내용/계정은 문서에 기록하지 않았다.

## 실제 기기 시험 체크리스트

정식 완료 전 다음 항목을 확대 검증한다. 아래 목록 자체는 PASS 표시가 아니며, 위 표와 실제 실행 이력에 명시된 항목만 통과다.

1. 접근성/오버레이 권한 없이 앱 실행 → 안내, 충돌 없음.
2. 사용자 권한 부여 → 서비스 연결. 다른 서비스 설정 보존.
3. 목표 1, 서로 다른 길이 영상 3개 → 각각 한 번 후 다음. 중간 시작 부분 재생 제외.
4. 목표 2, 서로 다른 길이 영상 3개 → 1/2→2/2→다음. 중복 넘김 없음.
5. 0 모드로 두 번 이상 반복 대기 → 자동 넘김 없음, 0/0 유지.
6. 숫자 탭 순환, 끌기 후 값 불변, X 즉시 숨김.
7. 재실행 후 설정·위치 복원, 자동 실행 OFF.
8. 타일 추가/짧게 ON/OFF/길게 설정, 실제 활성 색과 문구.
9. 댓글/키보드/공유/옵션/일시정지/수동 넘김/수동 탐색/다른 앱/잠금 → 오작동 없음.
10. 커버/내부 화면, 가로/세로, 화면 모서리에서 위치 보정과 터치영역.
11. 화면을 수동으로 만지는 동안 자동 제스처 충돌 여부. 감지 불가능한 경우 제한 명시.
12. 연결 종료 후 폰 단독 실행 및 장시간 CPU/배터리 측정.

## 재현 명령

```powershell
.\scripts\verify.ps1
```

`testDebugUnitTest`를 PASS로 치환하지 않는다. 위 스크립트는 시험 컴파일 이후 실제 JUnit runner로 같은 테스트 클래스들을 실행한다.

## 실제 실행 이력 · 2026-08-27

- 기기: Galaxy Z Fold8 SM-F971N, Android17/SDK37, 일반 YouTube21.33.322, Premium. 커버 화면1248×1972.
- 설치/실행 시험에서 권한은 사용자가 직접 부여. 다른 앱의 권한/설정 변경 없음.
- 0.1.0: 0/0→0/1→0/2 순환, 숫자 드래그 후 목표값2 유지, 저장 좌표와 위치 복원, X 종료 확인. 캡처는 비공개 화면 자료에 보관.
- 0.1.1: 서비스 연결을 방해하지 않는 진단으로 41/52 고정 확인(D-006).
- 0.1.2: 시간 갱신 해결. 1/2 진입 후 정상 갱신 오판으로 초기화되는 추가 문제 재현.
- 0.1.3: 14→16초,309ms 진단으로 점프 오판 확정(D-008).
- 0.1.4 목표2: 13:25:33 첫 온전한 재생1/2, 13:26:25경 두 번째2/2, 13:27:18 다음 영상54초의1/2. requests=confirmed=1, blocked=false. 자동 넘김 후 전체 쇼츠 화면 유지.
- 위 0.1.4 연속 시험 중 수동 스와이프/탭 없음. 증거: 비공개 증거 자료, 실제2/2와 전환 후 화면 육안 확인.
- 목표1 시험 시작 전 숫자로2→0→1 변경 후 한 번 수동 아래→위 스와이프하여21초 영상0초에서 시작. 이후 손대지 않고21/45/30초 세 영상에서 자동 전환 확인, 총 requests=confirmed=4(앞선 목표2의1회 포함). 증거: v014-one-play.jsonl.
- 일시정지: 다음42초 영상24초에서 멈춤,8개 표본 동안24초/요청4/확인4 유지. 댓글창을 열자0/1과 댓글·메뉴 대기. 계정/댓글 입력·추천·저장·구독 조작 없음.
- 목표0: 재생 재개 후1→2→0. 13:31:06~13:32:33,53표본에서요청4/확인4 유지. 0에서는시간조회 자체를 중단하므로 진단 position24는 마지막 조회값이지 재생 정지 증거가 아님. 전·중간 실제 화면은 같은 영상의 서로 다른 장면 및0/0.
- 타일: 등록 버튼에서 이미 추가됨 확인. 총34개 중 마지막에 있는 본 앱 타일만 조작. ON 흰 배경/플로팅0/2, OFF 비활성 및플로팅 숨김, 길게 앱 설정 진입. 다른 타일 설정 변경 없음.
- 0.1.5: 빠른 설정창 가림 시 뒤쪽영상 추적을 중단하도록 창 검사를 추가(D-009). 새 빌드/lint/직접 JUnit61 PASS, 독립 코드 리뷰 PASS. 빠른 설정6표본에서대기, 복귀 감지 및 일반 자동 넘김 영향 재시험.
- 0.1.5 최종 회귀: 42초 영상의 첫 온전한 재생1/2→13:39:54 두 번째2/2→13:40:37 다음36초 영상1/2. 요청1/확인1, 차단false. 최신 캡처에서 영상 전환과 전체화면 유지 확인. 증거: v015-final-auto-regression.jsonl, v015-auto-confirmed.png.
- 시험 종료: X로 자동 실행OFF, 목표2 및 저장 위치/기존 타일 유지. YouTube 전체화면은 그대로 두었으며 권한·다른 타일 설정 변경 없음.
- 최종 독립 요구사항·증거 대조 PASS: 검토자가 빠른 설정6표본, 최신1/2→2/2→전환 로그, 빌드/배포/설치APK의3개 해시와 문서 제한사항을 직접 확인. 목표1 시험 버전과 과거/최신 JUnit 개수 문구를 명확히 반영.
- 한계: 입력포커스 없는 다른 팝업/타 앱 플로팅 전체 가림은 미검증. 잠금 해제·폴드 실제 접기/펴기·장시간 배터리 시험은 미실행.
- 독립 검토자 추가 JShell: 2배속+2초 표시+300ms 관측에서 두 번 완주 후 정확히1회 요청, 대기 초기화0. 긴 일시정지 후3초 급진행 및3초영상600ms 조기 완료 차단.
- 앱/기기 외부로 영상 제목·계정·진단자료 업로드 없음. 숫자 진단만 사용, 개인 화면 캡처는 비공개.

## 배포용 시험 산출물

- 파일: `artifacts/shorts-loop-v0.1.5-debug.apk`
- 크기: 50,055 bytes
- SHA-256: `2518766C5E8730A7AEE6CF59F3065354484D78868E999F15CE0D6CB3C499BA2C`
- apksigner verify: PASS, v2 개발 서명, signer 1.
- APK 직접 실행: 설치·서비스·실제 플로팅·자동 넘김 확인. 설치된 base.apk를 다시 가져와 위 해시와 일치 확인.
- 기존0.1.0/0.1.2/0.1.4 산출물은 이력용 보존, 최신 배포용으로 사용하지 않음.
- Git 저장소 초기화/커밋/외부 게시 없음.

## 2026-08-27 · Instagram 사전 관측 (제품 E2E 아님)

- Instagram444.0.0.46.85/Fold8 커버 화면. 시험 전 ShortsLoop enabled=false 확인. 설치APK/권한 미변경.
- 재생 중 기본 UI dump는 idle 실패, 일시정지 시 성공. clips_viewer_view_pager/SeekBar scrubber 확인.
- scripts/probes/InstagramProgressProbe.java를 SDK로 컴파일하여 dexjar 읽기전용 실행. 프로브 자체에는 탭/스와이프 없음. 별도 ADB로 재생/일시정지/다음넘김1회 조작.
- sample-1-progress.txt: 일시정지18.721초 고정 후 재개 진행. 매번 노드 탐색은약10초 지연이 있어 연속 경계 정밀검증에 사용하지 않음.
- sample-2-cached-progress.txt:136개 모두 fresh=true/visible=true. max19014, current18708→95,18705→83,18737→124의 경계3회. 약400ms간격. 독립 검토자가 원시 숫자 재분석하여 일치 확인.
- sample-3-next-video.txt: ADB상향스와이프 후 max22638의 다른 영상 및22377→147 반복 경계 확인. 실제 화면도 확인.
- 증거: 비공개 프로브 자료, 비공개 화면 자료. assertion 없는 프로브 OK(1 test)는 실행 종료이지 자동 넘김 기능 PASS가 아님.
- 결론: 진행값 기반 지원의 기술적 근거 확보. 서비스 통합, 정확N회 후 다음, 광고/댓글/탐색/잠금/앱선택 보호는 미실행.
- 새 업데이트 제품 BUILD/TEST/REGRESSION은 N/A(미구현). 진단Java컴파일/실행/숫자관측은 수행. 아이콘 시안 육안 확인, 실제 런처 적용은 미실행.
