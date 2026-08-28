# 누적이력

## 0.3.0/code32 · 듀얼모드 공개 완료 / Dual-mode release published

2026-08-28 21:25:22KST 기존Public에v0.3.0/6bfe330 게시,제품CI33170840370SUCCESS(debug/release각564시험),21:25:52KST 공개3파일익명크기/SHA256/digest·HTTP200·업데이트조회PASS. 최종9AA1E884…AF394AA APK742854bytes,실폰덮어설치해시일치·설정보존·실행OFF. 최종3OSnativePASS,개인정보감사/독립리뷰차단사항0. 메일없음. / Published with product CI,final three-OS checks,phone-installed identity and anonymous release/update-feed parity verified;no email.

- 듀얼 토글(기본OFF)을 켜두면 보이는 선택 앱에 따라 전체1개/분할2개/일반 앱 옆1개를 자동 처리한다. 앱별 카운트·긴영상·플로팅 탭/위치/일시정지를 분리하고 X는 해당 앱만 멈춘다. 기존 설정은 보존하며 최초 한 번 앱별로 복사한다.
- 분할/회전/전체화면 손잡이와 시스템 바의 가림 오인을 고쳤다. 관측 예외와 실제 입력 충돌 검사를 분리하고 오래되거나 창·페이지가 바뀐 대기 요청은 폐기한다.
- 최신53FD 실폰 후보564JUnit·3OS native·설치해시·독립리뷰PASS. 실행/듀얼ON 유지 전체→분할→전체에서 YT4/4·IG13/13 확인,숨긴 앱 요청0·차단/복구0. 이전 후보의 상하교환·계산기 병행 시험과 분리해 기록한다.
- 한영 사용법·설정/호환성/업데이트/설계/디버그/검증 문서를 최신화했다. 개인정보·원시로그·기기화면·키 제외 및 배포 검증을 마쳤다. [최종 릴리스 원장](releases/v0.3.0.md),[상세 시험·한계](VERIFICATION.md).

EN: Default-OFF dual mode automatically adapts to one or two visible selected hosts,with separate settings,counters and floats. Corrected bounded SystemUI observation while preserving input safety. Candidate53FD passes564 unit/three-OS native tests and uninterrupted mode-transition checks with4/4 YouTube and13/13 Instagram confirmations. Prior candidate evidence remains separate. Documentation and privacy/release checks are updated;see the release record for publication status.

### 이전0.3.0 후보 체크포인트 / Earlier candidate checkpoints

2026-08-28회전후속:상하창의정상상태표시줄고정높이오인을수정하고561JUnit/가드·3OSnativePASS. 상하두순서와계산기조작중YouTube전환을확인했다. 마지막F582후보742366bytes는한호스트+일반앱도듀얼ON이라는한영안내보완이며실폰설치해시일치;동일APK API26/33/34 native28043/28053/27794PASS. 계산기+Instagram일반반복확인중,원래회전복귀등은미검증. [후보별최신검증](VERIFICATION.md). EN: Fixed rotated status-bar misclassification,verified both top/bottom orders and calculator/YouTube coexistence,and clarified dual-mode help for one visible host. Final installed F582 passes561 unit tests and exact-APK three-OS native checks;remaining physical coverage is stated in verification. No publication.

아래는이번회전수정전의후보별이력이다. / The following preserves earlier candidate checkpoints.

후속검증:분할/상단손잡이와자기플로팅오인을수정하고관측허용과실제입력경로보호를분리했다. 최신818AC00D후보559JUnit·3OSnativePASS,설치해시일치,좁은창YT1/IG8자동전환확인. 이전후보좌우교체후누적YT10/IG12및개별X분리는별도근거로보존한다. 전체실행을끄고사용자의90도회전/상하분할준비를대기중이다. [최신검증](VERIFICATION.md). EN: Latest candidate passes559 unit tests/three-OS native checks and narrow-pane retest. Rotation/top-bottom physical testing is pending;earlier two-order and isolated-X evidence remains candidate-specific.

두 앱의 상태·설정·플로팅을 분리하고 기본OFF 듀얼 토글을 추가했다. 기존 키는 보존하고 앱별 초기값을 한 번 복사한다. 입력은직렬화하며 콘텐츠/노드/창/경계가달라지거나3초를넘긴대기는폐기한다. 초점을강제로옮기거나일시정지앱을재생하지않는다. 1차546JUnit,API26/33/34nativePASS;실폰비초점YouTube시계증가확인. 양쪽자동넘김·순서교환은검사중이며아직PASS아니다. [설계/근거](SPLIT_SCREEN_PLAN.md). 공개최신0.2.9유지,새게시없음.

EN: Independent sessions/settings/overlays and default-OFF dual mode. Preserved legacy settings,serialized input and bounded page-validated waiting. Initial unit/native checks pass;non-focused YouTube progress is observable. Physical dual-host auto-advance and swaps are still under validation. Public latest stays0.2.9.

## 0.2.9 공개 완료 / Published0.2.9

2026-08-28 19:26KST 기존Public에v0.2.9/eb4bd0c 게시. 제품CI33163186891 SUCCESS,debug/release각522tests·실패/오류/건너뜀0,최종3OS계측PASS,익명3파일 크기·SHA256·GitHub digest·HTTP200·업데이트조회API PASS. 최종5EEB5B8B…31E68D는 실폰시험후보1EF와 실행 내용이 같고 내장revision만 다르다. 실폰재설치/메일발송없음. [릴리스 원장](releases/v0.2.9.md). 아래 보류·local only 표기는 배포 전 이력이다.

EN: Published Public0.2.9 at19:26KST;source eb4bd0c,product CI and final native/public-asset verification pass. Final and phone-tested candidate runtime payloads are identical;embedded revision differs. No phone reinstall/email. See the release record;hold/local-only entries below are historical.

## 후속 계획 / Follow-up plan

분할 화면의 YouTube·Instagram 동시 처리, 배치 순서 독립, 앱별 플로팅2개 또는 분할형1개를 [별도 계획](SPLIT_SCREEN_PLAN.md)으로 기록했다. 읽기 전용 구조 확인만 수행했으며0.2.9에는 미포함이다.

EN: Dual-host split screen,order-independent operation and per-host/two-section overlays are planned separately. Read-only inspection only;not part of0.2.9.

## 0.2.9/code31 후속 · 사진 릴스 / Photo Reel modes, local only

후속 검증(2026-08-28): 두 기본값3초와 기존 노드·광고 보강을 유지했다. 최신721898-byte 후보를 실폰에 설치하고 SHA256 일치를 확인했다. 통째0·3·10초, 한 장0·3·10초, 마지막 장 이후 다음 릴스 이동과 댓글창 보호를 확인했다. 추가 신속 검사에서8장 릴스의 가로7/7·세로1/1, 한 장10초의 가로1/1·세로1/1을 확인했다. 번호 없는 사진·사진 직후 광고·혼합 게시물은 실폰 사례 미확보이며,20개 표본 탐색은 자동20연속 PASS가 아니다. GitHub 게시 보류. 신속 검사 후 실행ON·통째3초/한 장3초·대체OFF·반복1과 기존 다른 옵션을 복원했다. 제품 코드 변경/재빌드 없이 같은 APK를 시험했다. [검증 기록](VERIFICATION.md),D-042.

EN: Both defaults remain3s. The latest721898-byte APK is installed with matching SHA256. Whole0/3/10s,each0/3/10s,last-slide exit and comments protection have physical evidence. Additional checks passed7/7 horizontal and1/1 vertical moves on an8-photo post,and1/1 horizontal plus1/1 vertical with each10s. Unreadable-index photos,photo-to-ad and mixed posts were not found for physical testing;20 rapid samples are not20 consecutive automatic passes. Publication remains on hold. Restored executionON,whole3s/each3s,fallbackOFF,target1 and prior unrelated options. No product-code change/rebuild in this test pass.

Instagram 사진carousel 전용 감지와 통째/한장씩2모드,각0–10초(초기3)별도저장,장번호실패시선택형통째넘김을구현했다. 사진옵션은기본OFF이며반복0회와독립이다. 정확한다음사진/안정된다른릴스를확인하고미확인요청은재시도하지않는다. KO/EN설정·플로팅·0회및광고전용안내를정리했다. [설명·안전경계](PHOTO_REELS.md),[빌드별검증](VERIFICATION.md). GitHub미게시. 과거아래언어전용후보와사진확장후보의근거를분리한다.

EN: Local photo-carousel detection,two modes,separate0–10-second waits and optional unreadable-index fallback. DefaultOFF,independent of repeat0. Exact slide/stable post confirmation and hard-stop protections prevent blind retries. Bilingual controls/status/help are updated. Not published;verification distinguishes the earlier localization-only candidate from the photo extension.

## 0.2.9/code31 · 한국어/영어 자동 선택 / Automatic localization

시스템 첫 언어 기준KO/EN표시,309개 리소스쌍·93개 중립 상태코드·업데이트 오류 분리. 큰 글꼴과 작은 창 레이아웃 보완. GitHub 검색 설명/토픽/한영 안내 정리. 기존 감지·넘김·권한·설정·서명 계약 유지. [상세 언어 안내](LOCALIZATION.md),[최종 시험·게시 근거](releases/v0.2.9.md). 휴대폰 시험NOT RUN;검증/게시 진행상태는 릴리스 기록을 따른다.

EN: First-system-language KO/EN UI,309 paired resources,93 neutral statuses,localized update failures,and large-text/narrow-viewport layout fixes. Existing detection,permissions,settings and signer are retained. Discoverability docs/metadata are updated. Physical-phone tests NOT RUN;see the linked release record for current verification/publication evidence.

## 0.2.8/code30 · 버전 표시·배포 빌드 정리 / Version presentation and distribution

2026-08-28 Public 게시·제품CI33144962247·공개3파일 크기/해시 확인 완료. 제품af1ab39,태그v0.2.8→4b5647c(문서만 추가). [배포 기록](releases/v0.2.8.md).<br>
Public publication, product CI and all three public-asset checks completed.

두 버전 표시를 앱명·번호만 남기는 방식으로 수정했다. 실제 화면 분석 실험 안내는 유지한다. 같은 단일 서명을 사용하는 non-debuggable release 빌드, 디버깅 APK 출고 거절, 실제 라벨 계측검사, debug/release CI, 기존 설정 덮어 설치 검사와 소스 커밋 일치 검사를 추가했다. 현재 문서와 과거 기록도 분리했다. 감지·반복·자동 넘김·업데이트 선택 정책은 변경하지 않았다.

468JUnit·정적가드·로컬lint(0오류/기존3경고), API26/33/34 계측5572/5572/5571, 세OS19설정·UID·서명 보존, 휴대폰 두라벨·사용법·접근성·13런타임설정·플로팅 탭/X·설치해시 검사를 수행했다. 새로운 호스트 연속시험은 미실행. 최종 source/artifact/CI/공개 상태는 [0.2.8 기록](releases/v0.2.8.md), 절차는 [배포 빌드](RELEASE_BUILD.md), D-037/038 수정 근거는 [감사](RELEASE_PRESENTATION_AUDIT.md)를 따른다.

EN: Neutral version labels and a non-debuggable release variant preserve the existing signer and real experimental warnings. Added exact UI/publication/source-identity guards and upgrade regression;separated current docs from history.468 unit tests,three-OS native/19-preference upgrades and scoped phone checks passed. No new playback-policy change or host endurance claim. Publication details are recorded separately.

## 이전0.2.7 후속 배포·표시 검토 / Historical follow-up audit

앱/공개APK/문서/검증절차전수검토에서D-037(시험판라벨·현재문서불일치),D-038(디버깅허용배포APK·차단검사부재)을미해결로기록했다. 앱전체의시험/정식표시대신앱명·버전만표시하고실제실험기능안내를유지하는방향을권고한다. 이번에는검토문서만로컬기록했으며제품코드·공개파일·권한변경없음. [검토결과](RELEASE_PRESENTATION_AUDIT.md).

EN: Recorded open release-label/documentation drift and debuggable-artifact findings. Neutral app/version labels are recommended while preserving genuine experimental warnings. Only local review records were added;product/public artifacts and permissions remain unchanged.

## 0.2.7/code29 · 플로팅 잘림 수정 / Floating-label correction

2026-08-28 **정식 릴리스 공개 완료**(Public,prerelease=false,latest=v0.2.7). 제품5125b33·CI33143291193 SUCCESS와 공개3파일 크기/해시 일치 확인. [최종 배포 기록](releases/v0.2.7.md).<br>
Stable release published;product5125b33,CI33143291193 and anonymous verification of all3 assets passed.

플로팅72×56dp 유지,글자 전체폭·X 아래 영역 사용,가로 스크롤 해제와 실제 폭 기준 autosize로 ‘긴영상’ 잘림 수정. 실제 글자 경계·큰 글꼴·RTL·굵은 글꼴·상태 변경 회귀검사 추가.468JUnit 및 API26/33/34 계측5568/5568/5567항목 PASS,실폰 표시/터치·독립 리뷰 완료. 정식 릴리스로 배포하며 게시·CI·다운로드 검증은 [릴리스 기록](releases/v0.2.7.md)을 따른다. 기존0.2.6 APK는 변경하지 않는다. [원인·검증·한계](FLOATING_LAYOUT_FIX.md).

EN: Fixed compact floating-label clipping without enlarging the window. Added actual ink-boundary,large-font,RTL,bold and state-transition checks.468JUnit and5568/5568/5567 native assertions passed,with physical display/touch checks and independent review. Stable publication/CI/download checks are tracked in the release record;the existing0.2.6 APK is unchanged.

## 이전0.2.6/code28 · Public 시험판 검증 완료 / Historical published version

**0.2.6/code28 공개 시험판(pre-release)을 게시하고 공개 파일 검증까지 완료했다.** YouTube의 같은 창·pager·전체 페이지에서 현재 행이 요청 행보다 정확히1 증가하는 근거를 보강했다. 최종 빌드·468JUnit·정적 가드 PASS,로컬lint0오류/기존3경고,동일APK API26/33/34 계측233/233/232 PASS와 설치·설정 보존·접근성·런타임·해시 일치를 확인했다. YouTube20회는148.6초 동안 요청20/확인20(일반4·긴 영상15·라이브1),수동0·실패0·복구0으로 PASS했다. 같은 길이 영상 쌍은 이 실기기20회에 없었으므로 해당 조건의 실기기 재현 성공을 주장하지 않는다.

**0.2.6/code28 is published as a public pre-release,and public artifact verification is complete.** It adds exact current-row=request-row+1 evidence within the same YouTube window,pager and full-page bounds. Build,468 JUnit tests,static guards,233/233/232 exact-APK API26/33/34 checks and installation/settings/accessibility/runtime/hash parity passed;local lint has0 errors/3 existing warnings. YouTube20 passed in148.6 seconds with20 requests/20 confirmations:4 ordinary,15 long-video,1 live,and0 manual swipes,failures or recoveries. No equal-duration pair occurred in this run,so that precise physical case is not claimed as reproduced.

**공개 검증:** 제품 커밋·태그 `8dbcce3a5cd0cfa2931461773e58e12330de14b4` / `v0.2.6`. [GitHub CI33141470669](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33141470669)는SUCCESS이며,내려받은 보고서32suites·468tests·실패0·오류0·건너뜀0을 확인했다. CI lint는0오류/2경고로 로컬0오류/3경고와 구분한다. Actions의 Node20/setup-java4 사용중단 예고 경고는 비차단 유지보수 항목이다.

2026-08-28 13:21:42KST에 게시했으며,13:22:04.764KST 익명 검증 완료 시 [공개 릴리스](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.6)의 Public·draft=false·pre-release=true와 페이지HTTP200을 확인했다. APK746246bytes·SHA256 `AA217C63D4C5F97C9DB71740D45925260F779716B6944F7D3A860AF12B8012D6`,SHA256텍스트96bytes,업데이트JSON287bytes 모두 고정 산출물과 크기·SHA256이 일치했고 GitHub assets의 세 digest도 일치했다. 설치 APK도 같은 해시이며 제품 바이너리는 변경하지 않았다. 후속 문서는 별도 커밋이며 제품 태그는 변경하지 않는다.

독립 검토는 이번 변경과 검증 근거 범위에서PASS·확인된P1/P2 0건이다. 내비게이션 오버레이 실기기 시나리오는NOT RUN이며,동일 길이 영상 쌍의 최종 실기기 미관측·드문timeout 복구·플로팅 글자 일부 잘림 등 명시된 한계를 지우거나 모든 화면의 시각감사PASS로 확대하지 않는다.

**이번 code26→code28 YouTube 보완에서** Instagram의 일반 확인 경로와 `AdvanceGate`는 변경하지 않았다.0.2.5→0.2.6 전체에서 아무 변화가 없었다는 뜻은 아니다. code26의 Instagram10회 PASS(96.0초,일반3·긴 영상4·시간제2·광고1,수동0)는 해당 버전의 실기기 근거로 보존하고 이번에는 전체10회를 반복하지 않는다. 이 과거 결과를 새 code28 APK에서 Instagram을 재실행한 것처럼 표시하지 않는다. YouTube 재시험과 영향 범위 검증 후 기존 Public 저장소에v0.2.6/code28 pre-release를 게시했으며 CI·공개 다운로드 동일성도 확인했다.

**For this code26→code28 YouTube correction**,the generic Instagram path and AdvanceGate are unchanged from code26;this does not mean they were unchanged throughout0.2.5→0.2.6. Code26's Instagram10 PASS(96.0 seconds:3 ordinary,4 long-video,2 timed,1 ad;0 manual swipes) is retained as version-specific evidence without repeating the full run. It is not described as a new Instagram test on code28. After the YouTube retest and impact-scope checks passed,v0.2.6/code28 was published as a pre-release in the existing Public repository. CI and public-download parity were verified.

**YouTube20 실기기 기록:**13:08:46~13:11:14.291,148.6초,기준0→20·요청20/확인20. 일반4·긴 영상15·라이브1,광고/시간제0,수동0·실패0·복구0이다. 전후 화면0~20을 육안 대조했다.10번은 들어온 라이브가 다음 동작으로 이동 중인 화면이고11번에서 다른 일반 영상이 확인됐다. 현재 행의 정확한+1을 관측했다.13번 관측 중 알림 배너가 나타났으나 이후 전환은 계속됐으며 **단일 사례이지 모든 알림에 대한 보증이나 과거 실패 원인 규명은 아니다**. 목표 이후 추가3회는20회 결과에 합산하지 않는다.

13:12 긴 영상 기준30→60초를 인앱에서 복원하고 전체 실행OFF·blocked=false를 확인했다. 같은 길이 영상 쌍은 이번 실기기 시험에 없었으며,플로팅 글자 일부 잘림은 비차단 한계로 남긴다. 공개 전 검증과 실제 게시·공개 파일 동일성 검증을 분리한다.

## 과거 code26 · 지정시험 성공 후 후속실패 / Historical candidate

**과거0.2.6/code26은 실폰 후속 실패로 게시 보류된 미배포 후보였다.** 빌드·454 JUnit(실패0)·정적 가드 PASS, lint0오류/기존3경고. 12:33 동일 APK의 Android API26/33/34 계측209/209/208개 PASS,12:36 휴대폰 설치·전체 기존 설정 직접 비교 보존·접근성 연결·설치 APK 해시 일치 PASS. 12:38:20 YouTube 공식 시험은 요청10/확인10(긴 영상9+라이브1)과 전후 화면의 서로 다른 영상 확인으로 PASS했다. 12:39:22 별도 일반1/1 전환1회도 화면 쌍으로 확인했다. 그러나 후속 연속 실행 중 요청20/확인19에서 같은59초 길이·pager index 부재로 안전정지했다. 해당 실패 요청에는 전후 화면 쌍이 없어 실제 다음 영상 이동 여부는 미확정이다. Instagram은12:43:56~12:45:31.831(96.0초) 별도 시험에서 요청10/확인10(일반3·긴 영상4·시간제10초2·광고1),수동0·실패/복구0으로 PASS했다. **두 앱의 지정10회 PASS가 유튜브 후속 실패를 덮지 않으며 제품 완료·배포 준비 완료가 아니다.**

**Historical0.2.6/code26 remained unpublished after a subsequent device failure blocked its release.** Build,454 JUnit tests with zero failures,static guards and209/209/208 exact-APK API26/33/34 checks passed; lint has0 errors and3 existing warnings. Installation preserved all compared preferences and accessibility binding,and matched the APK hash. The12:38:20 YouTube run passed10 requests/10 confirmed distinct transitions:9 long-video and1 live. A separate12:39:22 ordinary1/1 transition also passed screenshot-pair review. Further continuation then safety-stopped at20 requests/19 confirmations when both durations were59 seconds and pager indices were unavailable. No pre/post screenshot pair exists for that failed request,so actual movement is unproven. A separate96.0-second Instagram run at12:43:56–12:45:31.831 passed10 requests/10 confirmations:3 ordinary,4 long-video,2 ten-second clockless and1 ad,with no manual swipes,failures or recoveries. The two designated ten-transition PASS results do not override the later YouTube failure or establish release readiness.

YouTube의 별도 RAM 메타데이터 키 경로는 **다른 키 AND (요청 후 같은 창·pager의 최신 실제 index 변화 OR 다른 유효 총길이) AND 300ms 이상 안정 AND 최신 실제 전진 재생**을 모두 요구한다. 요청 시 키 출처를 고정해 메타데이터의 등장·소실을 다른 출처의 키와 비교하지 않는다. 부분 메타데이터 소실로 키만 달라져도 이동으로 인정하지 않는다. 일반 반복 identity는 바꾸지 않는다. 메타데이터 키를 쓰지 않는 기존 확인 경로는 안정된 다른 identity 또는 최신 동일 pager 이동+다른 안정된 총길이+전진 근거를 유지한다. 길이 단독은 확인 근거가 아니며, 메타데이터 경로에서 같은 길이이고 pager index도 없으면 실제 이동했더라도 안전정지할 수 있다. 긴 영상 확인4.5초 실패는 일반 복구나 추가 스와이프로 우회하지 않는다.

The supplemental YouTube RAM-metadata path requires a different key AND either request-fresh same-window/pager index movement or a different valid duration,then at least300ms of stability and current forward playback. The identity source is fixed at request time; appearing,missing or partially missing metadata alone cannot confirm movement. Ordinary repeat identity is unchanged. Non-metadata confirmation retains stable changed identity or corroborated fresh pager movement with changed stable duration and forward progress. Duration alone is insufficient. Same-duration metadata pages without pager indices may still safety-stop after real movement. Long-video4.5-second timeouts never use ordinary recovery or retry swipes.

고정 APK: **757038bytes**, SHA256 `82CE7C221C1BF3E6DA8F86F9D487F9685D89DFB22A38D24F60B77F447519E926`. [검증 원장](VERIFICATION.md), [원인·재발방지](DEBUG_LOG.md).

연속 시험은 반복1·긴 영상ON/기준30초·광고/라이브ON·Instagram 시간제10초로 수행했다. 종료 후 플로팅X로 실행을 중지했고 blocked=false를 확인했다.12:46 인앱 숫자 입력으로 긴 영상 기준30→60초를 복원하고 UI·런타임에서 확인했다. 최종 상태는 **전체 실행OFF,반복1,긴 영상ON/60초,광고ON,라이브ON/0초,시간제ON/10초,화면 분석OFF**다. 제품의 신규 기본값OFF/60초를 바꾼 것이 아니라 기존 옵션은 보존했다. 드문 일반 timeout의 실제 발생·새 시작점 복구,최종 전체 화면 시각/사용성 감사,공개 CI·Release·익명 다운로드 동일성은 완료로 표시하지 않는다. 유튜브 후속 확인 실패가 남아 게시 보류를 유지한다.

Instagram 공식 시험은12:43:56~12:45:31.831,총96.0초이며 기준 요청/확인19→29에서 **요청10/확인10 PASS**다. 구성은 일반3·긴 영상4·진행정보 없는10초 시간제2·광고1,수동 이동0·실패0·복구0이다. 전후0~10 화면을 육안 대조했으며8번 캡처는 광고→일반 전환 중이고9번은 안정된 페이지였다. 목표 뒤 추가6회는 이10회 결과에 합산하지 않는다.

The formal Instagram run lasted96.0 seconds at12:43:56–12:45:31.831,advancing the request/confirmation baseline19→29:10/10 PASS. It comprised3 ordinary,4 long-video,2 ten-second clockless and1 ad transition,with0 manual swipes,failures or recoveries. Screenshots0–10 were visually reviewed;capture8 shows the outgoing-ad/incoming-ordinary gesture and9 a settled page. Six later transitions are excluded from this ten-transition result.

**이전 후보는 별도 기록이다.** code23은12:12 실제62→93초 영상 이동 후 요청1/확인0으로 실패했고, code24는12:17 같은 창·영역·인식·안전 조건에서도 공통 텍스트 identity가 같음을 재현했다. code25도12:21~12:22 실제93→57초 이동 후 요청/현재 index가 모두−1이고 공통 identity가 같아 실패했다. code23/24/25는 실폰FAIL·미배포이며 PC·계측PASS가 이를 덮지 않는다. code22의 YouTube2회는 기능 추가로 중단한 과거 관측이며 수동180초 영상 이동1회는 제외했다. 어느 후보의 관측도 code26의10회에 합산하지 않는다.

Earlier code23/24/25 candidates failed physical confirmation and were not published,despite PC/emulator passes. Code23 actually moved62→93 seconds but confirmed0 of1 requests; code24 reproduced identical shared-text identities; code25 moved93→57 seconds but both pager indices were−1. Code22 stopped after two automatic transitions for feature integration,excluding one manual180-second skip. No historical transitions count toward code26.

## 과거 code23 · PC/설치PASS 이후 실폰FAIL·미배포 / Historical candidate,device FAIL

아래는12:12실폰FAIL 이전 체크포인트다. UI·설치검증은전체자동전환PASS가아니며최신code28검증으로재사용하지않는다.

기존 일반 전환 확인 시간초과의 새 시작점 복구에 긴 영상 필터를 추가했다. 기본OFF·총길이 기준60초·1~3600초/±1초이며 선택한 YouTube·Instagram의 일반 영상 중 확인된길이≥기준이면 안전한 실제 진행 확인 후 넘긴다. 반복0과 독립,전체OFF는 중지,길이 불명 추정 금지다. 긴 영상 요청의4.5초 확인 실패는 안전정지이며 일반 복구로 전환하지 않는다.

일반 횟수 다음에 긴 영상 카드를 배치하고 초안·입력 오류·상하한·미설치/미선택·설정보존·0회와 광고 전용 설명을 갱신했다. 복구대기/안전정지가 긴 영상이나0회 라벨에 가려지는 것을 예방한다. 최종빌드·418제품시험/실패0·정적가드PASS,12:10 동일APK API26/33/34 계측163/163/162 및 폰설치·기존설정/접근성bound/설치해시PASS. 고정757601bytes,SHA256 `FC866F0459CD3536114758DB277F0FCD0EF84CFA443E9C8817B448D6ED704B7F`. lint0오류/기존3경고는최종보고서재대조중이다. code23실폰 YouTube10/Instagram10·조건별실제시험·공개CI/릴리스는미실행이다. [검증](VERIFICATION.md), [제품 계약](PRODUCT_SPEC.md), [예방 대장](DEBUG_LOG.md), [릴리스](releases/v0.2.6.md).

EN: Unpublished code23 adds optional long-video filtering(defaultOFF,known duration≥60 seconds,range1–3600),independent of zero plays and guarded by real progress. Build,418 tests,static guards,163/163/162 exact-APK emulator checks and installation parity passed. Lint0/3 awaits final report comparison; actual10+10,physical feature cases and public delivery remain pending.

## 과거 0.2.6/code22 · 시작점 복구 후보 / Historical fresh-start candidate

일반 영상 넘김 확인 시간초과 후 읽기전용 시작점 재인식과 전체횟수 재계산, 플로팅 대기/정지 표시, 인앱 도움말, 개인정보 없는 복구누계 진단을 추가했다. 기존 특수영상/권한/제스처 안전정지를 유지한다. README에 한영 원출처 표시 요청과 원본/변경 구분 안내를 추가하며 별도 라이선스를 임의로 지정하지 않았다. [복구설계](PLAYBACK_RECOVERY.md), [D-034](DEBUG_LOG.md).

과거code22의 빌드·383단위시험·정적가드·Android26/33/34 계측109/109/108 PASS를 보존한다. 실기기 YouTube2회 자동 전환 후180초 영상1개 수동 이동은 제외했고, 기능 추가로 시험을 중단했다.10연속PASS가 아니며 Instagram10·게시 검증은 미실행이다. code23 시험과 합산하지 않는다.

EN: Historical code22 passed build,383 tests and109/109/108 emulator checks for fresh-start recovery. Its phone run confirmed two automatic YouTube transitions,excluded one manual180-second skip,and stopped for feature integration. It is not a ten-transition PASS; Instagram10 and publication were not run. Bilingual attribution requests remain without introducing a new license.

## 2026-08-28 · D-034 읽기 전용 진단 / Read-only diagnosis

후속: 동일 영상에서 플로팅1→0→1 재개 후3회 실제 자동 전환·화면 확인. 최초 실패 미재현/근본 원인 미확정, 제품 수정·새 게시 없음. 시험 후 실행ON·목표1·간편모드1 유지. / Follow-up: rearming the same video restored three actual auto-transitions; the original failure did not reproduce. Root cause remains unknown, with no product change or new release.

code21의 일반 YouTube0/1 정지는 접근성 미연결이 아니라 넘김 확인 실패 후 차단 유지 상태(요청21/확인20)로 확인됐다. 차단 시 조회 중단과 플로팅 오류 라벨 누락을 소스와 실제 상태로 대조했다. 최초 전환 실패의 상세 원인은 미확정. 재시작·수동 넘김·구현·빌드·새 게시 없음. [디버그 대장](DEBUG_LOG.md).

EN: A subsequent code21 normal-YouTube0/1 stall was a latched transition-confirmation failure, not a disconnected accessibility service. Polling stops and the floating label hides the error. Original failure details remain unknown; no restart, manual scroll, code/build change or new publication.

## 2026-08-28 · v0.2.5 Public 공개·무결성 확인 / Publication and integrity verification

2026-08-28 10:57 KST에 **0.2.5/code21을 기존 Public 저장소의 시험판으로 공개**했다. [v0.2.5 Release](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.5), main·태그 기준 코드 커밋 `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5`, draft=false/prerelease=true. [CI33134278633](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33134278633) 성공: 내려받은 보고서356시험·실패0·오류0·건너뜀0, CI lint0오류/2경고(로컬 기존3경고와 구분). 익명 릴리스HTTP200 및 배포파일3개(APK709703bytes/SHA96bytes/JSON287bytes)의 원본 대비 크기·SHA-256 일치를 확인했다. 이후 문서 정리는 제품 소스·APK를 바꾸지 않는다.

**Version0.2.5/code21 was published as a Public prerelease at10:57 KST on2026-08-28**, from code commit `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5` and tagv0.2.5. CI33134278633 succeeded with356 tests and zero failures/errors/skips; CI lint reported0 errors/2 warnings, separately from3 local warnings. Anonymous release access returnedHTTP200 and all three uploaded assets matched their originals byte-for-byte and by SHA-256. Subsequent documentation changes do not alter product source or the APK.

공개 APK SHA-256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`, v2서명 검증PASS, 기존 인증서 유지, 메타데이터21/0.2.5/min26 및10:51 설치본 동일성PASS. 공개 후보169개 민감정보 제외 독립감사PASS,224개 로컬 링크 누락0. code21 일반 YouTube10연속PASS는 유지하되 라이브 개별0/5초/OFF 재시험NOT RUN·20연속미완료·D-021미수정은 그대로다. 메일 발송 없음.

EN: APK signature/certificate continuity,21/0.2.5/min26 metadata and installed parity passed. The169-file privacy audit and224-link check passed. Code21's normal ten-transition PASS remains distinct from unperformed individual live retests, incomplete twenty-transition testing and unresolved D-021. No email was sent.

공개 후10:58~10:59, 설치된code21 앱의 업데이트 확인을 실제 실행해 새 업데이트 없음 안내·조회 시각 갱신을 확인했다. 실제 Public HTTPS 조회와 현재 버전 안내는PASS이며 새 버전 다운로드/설치 재시험은 아니다. 업데이트 자동조회ON·전체실행OFF를 유지했다.

At10:58–10:59, the installed code21 app successfully checked the public release over HTTPS, displayed no applicable newer update and updated its attempt timestamp. This verifies current-version checking, not a new-version download or installation. Automatic checking remained ON and overall execution OFF.

## 2026-08-28 · code21 최종 PC·설치·YouTube10연속 PASS / Build, installation and ten-transition verification

code21의 차단 상태·반복0 idle 조회 원복을 반영한 최종356JUnit·빌드·lint0오류/기존3경고·정적가드(LIVE_TREE_LIFECYCLE 포함) PASS.10:39 동일 고정 APK의 API26/33/34 계측74/74/73,10:40 휴대폰code21 설치·접근성bound·재생설정보존·설치해시 PASS. 최종 조회 원복 독립리뷰에서 추가P1/P2 없음.

고정 APK709703bytes, SHA256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`. code21 YouTube10연속은10:40:41→10:47:45.702,424.5초,확인0→10/세대6/수동입력·앱전환없음/라이브0으로PASS. 전환1~10의 서로 다른 정상 전체화면 영상·플로팅과 올바른 이동을 모두 육안 확인했다. 목표 이후 관측분은 공식10회에 포함하지 않는다. 약10:50 플로팅X 전체OFF 확인,10:51 업데이트 자동조회ON 복원·UI/저장값 확인.

code20 외부 중단/개별 라이브 시험을 합산하지 않으며 code21 라이브0/5초/OFF 재시험은 NOT RUN이다. D-021미수정·20연속미완료 유지. 이후 Public·CI·익명 배포파일 검증 결과는 위 공개 항목에 기록했다.

EN: The final code21 build,356 tests,lint/static guards,74/74/73 emulator checks and phone installation/binding/preferences/hash passed for the frozen709703-byte APK. Its separate **10-consecutive-transition YouTube run passed** in424.5 seconds,confirmation0→10,generation6,without manual input/app changes and with zero live skips. All10 transition screens were visually checked;later observations are excluded. FloatingX stopped execution around10:50. The update preference was restored ON at10:51. Code21 individual live retests and20-transition completion remain pending; subsequent publication checks are recorded above. D-021 is unchanged.

## 2026-08-28 · code20 연속 시험 외부 중단·code21 원복 보강 / Interrupted run and cleanup follow-up

code20의10연속은10:35:28 확인기준3/세대30에서 시작해10:35:52 첫 전환(다음54초),10:36:47 두 번째 전환(다음48초)을 확인했다.10:37:17 런처/카메라 전환으로 외부 중단됐다. 종료 blocked=false·pending=false·요청5/확인5·마지막30/48초. 제품 감지FAIL이 아니지만10PASS도 아니다. 개별 라이브 시험과 합산하지 않는다.

당시 독립 리뷰의 차단 상태 확장 플래그 재활성화 금지·반복0 idle 플래그 원복은code21로 보강 중이었다. 이 과거 체크포인트는 로컬 빌드 중·기기 미설치 상태이며 code20의 실제 시험을21의 결과로 재사용하지 않는다. GitHub 게시 조건 미달로 보류, D-021 미해결 유지.

EN: Code20's separate10-transition run confirmed two transitions before a foreground launcher/camera change ended it; no blocked/pending failure was recorded. It is not a10-transition PASS. At this earlier checkpoint, code21 was a local, uninstalled cleanup follow-up for blocked and zero-idle states. Publication was on hold at that checkpoint, and D-021 is unresolved.

## 2026-08-28 · code20 조회 플래그 수정·실기기 확인 / Retrieval fix and device verification

- code18 프로브/제품 조회 플래그 차이를 수정했다. 기본49노드/확장101노드 대조로 원인을 확인했다. 중간code19의 라이브ON 조건은 제외하고 code20은 전체실행ON·YouTube선택·전면YouTube에서 라이브 옵션과 무관하게 확장 조회한다. 라이브OFF도 진입을 인식하되 넘김만 차단한다. 조회 모드 변경 시 이전 root 폐기, failClosed/onDestroy 기본 플래그 원복, 새 권한 없음.
- cleanup 포함 최종 빌드·직접JUnit356·lint0오류/기존3경고 PASS.10:30 동일 후보 API26/33/34 에뮬레이터74/74/73 PASS. 휴대폰 설치 준비·설정 보존·설치본 해시 PASS.
- 고정 APK **725487bytes**, SHA256 `EF59D4E40E192A89D5B207741B03CCE08FA11AC1079DC61C7776C19A1D3D60EB`. 초기 cleanup 전 시험과 별도로 최종 결과를 확인했다.
- 실제 라이브5초는10:30:29 대기→34 요청→35 확인1·25초 일반 영상 진입 PASS. 라이브OFF는10:33:33~39 같은 라이브 인식·blocked=false·요청2/확인2 유지 PASS.0초/반복0회는10:34:31 조회 준비→33 요청→34 확인2→3 PASS. 메인 UI의 권한 경고 없음 육안 확인.
- D-033 원인 수정은 위 실기기 범위에서 확인했다.10:35:28 확인기준3/세대30으로 시작한 새10연속은2회 확인 후10:37:17 외부 앱 전환으로 중단됐으며 이전 개별 시험을 합산하지 않는다. D-021 일반 반복 경계, code16의2/10 실패, code18 인식FAIL,20연속 미완료를 유지한다. 새 Public 게시 없음. [검증](VERIFICATION.md), [D-033](DEBUG_LOG.md).

EN: Code20 corrected the retrieval-flag mismatch while preserving live recognition with skipping OFF. Its final cleanup-inclusive build,356 tests,74/74/73 emulator checks and phone installation/preferences/hash passed for the725487-byte APK. Actual5-second delay, immediate live skipping at zero normal plays, and live-OFF waiting passed separately; no permission warning was visible. The new10-transition run ended externally after two confirmations, not a10-transition PASS. Earlier failures and D-021 remain recorded; code20 remained unpublished.

## 2026-08-28 · code18 라이브 구현·고정 후보 검증 / Live implementation and retained candidate

- YouTube 라이브 미리보기 전용 옵션을 추가했다. 기본OFF·대기0~60초/기본0초(안전 확인 후 바로), 일반 반복0회와 독립, 전체OFF는 모두 중지한다. 라이브 카드를 광고 다음에 배치하고 설정·하단·타일·플로팅의 의미를 맞췄다.
- 전용 노드·단일 안정 페이지·전면창을 검사하고 RAM 노드 동일성으로 페이지를 구분한다. 제목/CTA/시청자 수를 식별에 사용하지 않는다. 일반→라이브 진입 확인과 라이브 넘김 선택을 분리하고, 라이브→라이브는 요청 이후 같은 창/pager 인덱스 변화와 다른 안정 페이지를 요구한다.
- 리뷰 지적 보완: 마지막 root 재검증, 오래된/이전 요청 인덱스 배제, 일반 쇼츠 null-child/600노드 수집 회귀 방지, 동일 uptime 재검증의 안정 상태 보존. 새 권한·OCR·화면/오디오 분석은 추가하지 않았다.
- code18 빌드·직접JUnit352·lint0오류/기존3경고 PASS. 동일 고정 APK로 API26/33/34 에뮬레이터74/74/73검사 PASS. 휴대폰 ADB 업데이트 설치·설치 후 접근성 재연결·재생 설정 보존·설치본 해시 일치 PASS.
- 보존 APK738945bytes, SHA256 `941532517058CB8553EFE5DB34ED1762426C468B2D66F88A567CE788E306C54D`. 실제 라이브 인식은FAIL(D-033)이므로 배포 성공으로 기록하지 않는다. 정식0초/5초·10연속은 NOT RUN. code16의2/10 실패·D-021 반복 경계·20연속 미완료는 유지한다.

EN: Code18 implemented independent live settings, safe structural recognition and stricter page confirmation. Build,352 product tests,74/74/73 emulator checks and phone installation/preferences/hash checks passed for the retained738945-byte APK. Actual live recognition failed(D-033); formal0/5-second and10-transition runs were not performed. This candidate was not published, and earlier failures remain recorded.

## 2026-08-28 · 과거 code16 YouTube 연속 시험 실패 재현·라이브 식별 조사

설치된0.2.5/code16의10연속 자동 전환 시험은2개 확인 후 라이브 미리보기 진입에서FAIL. 제스처 후 새 화면은 나타났으나 재생 시계가 없어 전환 확인이 실패했다. 후속 수동 재진입2회에서 안내 문구보다 먼저 접근성의 `immersive_live_preview_player`로 식별 가능함을 확인했다. 화면 일부가 보이는 전환 중에도 잡히므로 즉시 감지와 안전한 다음 제스처 시점은 분리해야 한다. 이 조사 시점에는 제품 감지/설정 변경과 라이브 수정·재시험 전이었다. 이후 code18/19 구현 상태와 구분하며 새 Public 게시 없음. [검증](VERIFICATION.md), [D-032](DEBUG_LOG.md).

## 2026-08-28 · 과거 code16 호환성·인앱 업데이트·메뉴 순서 통합 검증

- Android 8/API26 이상에서 OS별 지원 기능과 제한 이유를 안내한다. API34 화면 분석 격리, 구형 타일 표시·수동 추가 안내를 유지하고, 자주 쓰는 반복·시간제·광고·플로팅 설정을 앞쪽에 배치했다.
- GitHub 새 버전 안내와 수동 다운로드·설치를 추가했다. 저장소·버전·OS·파일 크기·SHA-256·패키지·전체 서명을 검사하고, 해시별 읽기 전용 설치 사본으로 파일 교체를 차단한다. 설치는 Android 확인창에서 직접 진행하며 설정값은 보존한다.
- 제품 단위·회귀시험 274개, 빌드, lint 0오류/3경고 PASS. API26/33/34 호환성 계측검사 각각 47/47/46개 PASS, 설치 사전검사 각각 36개 PASS. 실제 Android 17 기기의 설치 사전검사도 36개 PASS.
- 실제 Android 설치창을 통한 code15→16 업데이트 PASS. 설치 전후 설정값 동일, 최종 APK 703134bytes 및 설치본 SHA-256 `6FA61EA51C04AF5A8246E21183C7F4D9FDF0564FEEF5794553BEBEF7C1F4EFE1` 일치. API26/API34/실기기에서 기존 GitHub 공개 자산 다운로드 PASS.
- 후속 실기기 계측의 시작·종료로 접근성 서비스가 끊겨 사용 준비 안내가 지속되는 D-031을 분리 기록했다. enabled 설정·오버레이 허용과 실제 Bound 상태가 다른 것을 확인했으며 Java fatal crash는 관측되지 않았다.09:31 접근성 수동 OFF→ON 후 Bound 연결·Crashed 목록 해소·runtime connected=true/실행OFF/blocked=false 및 상단 준비 안내 소멸을 확인했다. 제품 코드·APK·저장 설정은 변경하지 않았다.
- 이 항목은 게시 전 통합 검증 기록이다. 새 Release·CI·공개 파일의 최종 확인 상태는 [검증 기록](VERIFICATION.md)을 따른다. 기존 감지 한계·20연속 미완료·미연결 VisualSequence 실험 2실패는 별개로 유지한다.

## 2026-08-28 · OS별 기능 안내·호환성 로컬 후보

OS별 지원 기능을 구분하는0.2.5-compat-test/code14 구현. minSdk26 유지, Android7백포트 없음. API34 캡처 전용 구현/XML 격리, 구형OS no-op, 미지원 이유 및 설정 보존, 구형타일 수동추가 안내/상태표시. 설치된호스트·선택·OS가용성을 분리하며 Instagram 최소OS를 추정하지 않음. 기존 반복/시간제/광고/플로팅 로직 유지. [한영 호환성 안내](COMPATIBILITY.md), [검증](VERIFICATION.md), [D-028](DEBUG_LOG.md).

빌드·제품241시험·lint0오류3경고·정적가드·테스트APK컴파일·독립재리뷰 PASS. API26/33/34 에뮬레이터17/17/16계측검사PASS,지원안내/타일요청창/구형타일설정복귀관측.660364bytes·v2서명·3개설치본SHA동일. 실제 기기 미연결로소셜앱E2E미실행. Public0.2.4 불변, 새 게시/실기기 설치/메일 없음.

## 2026-08-27 · 0.2.4 Public 게시·무결성 확인 완료

소스main2e89114/tagv0.2.4를Public시험판으로공개. CI33074271656 제품227시험/빌드PASS,lint0오류3경고. 릴리스페이지익명HTTP200,공개APK681624bytes와설치본/원본SHA256일치,v2서명검증PASS. 제품APK+SHA256만첨부;별도오디오APK/개인정보/원시로그/서명키없음. 실제정상N1복구/재전환확인,문서152개로컬링크검사누락0. [배포기록](releases/v0.2.4.md).20연속미완료및기존감지한계를보존,메일발송없음.

## 2026-08-27 · 0.2.4 광고 독립·기능별 UI·시험판 공개 준비

20연속시험미완료상태를명시하는Public시험판으로준비했다. 광고독립설정과기능별UI분류를적용: 앱/일반횟수/무진행시간제/광고/플로팅/준비/실험.0회는일반·시간제만중지하고광고는IG선택+광고ON+메인ON으로동작한다. 전체토글·타일·플로팅문구·접근성설명·한영문서를같이수정했다.

code13/version0.2.4,제품227시험·빌드·lint0오류4경고·정적가드·독립리뷰PASS. 미연결VisualSequence원본은보존하고제품구현/시험소스집합에서제외,별도시험20중2FAIL공개·DEX부재확인. 실제0회광고1회전환확인,일반영상/시간제중지확인. 최종APK681624bytes,설치본SHA256일치. [릴리스·최종게시검증](releases/v0.2.4.md),[검증원장](VERIFICATION.md).20연속성공으로표현하지않으며메일없음.

## 2026-08-27 · 시간제 기본10초·최대60초 변경

후보A의기본15초를후보B에서기본10초/최대60초로변경. 후보B(code12)는5~60초/±1초/기본OFF유지,UI·입력검사·기본값·경계시험·설명서동기화. 기기저장값도10초로수동설정후재시험한다. 최초A증거는보존하며B시험과혼합하지않는다.

후속 검증: B의 연결 범위 225시험·빌드·lint(0오류/4경고)·정적 검사 PASS. 659665bytes APK와 설치본 SHA256 일치. 실제 60 저장/61 거부/상한 + 차단 후 기기 저장값을 10초로 설정했다. A는 일반12(시간제8/정상4)+광고1 별도에서 B 설치로 시험 중단. 새로운 Public/메일 없음.

21:22:43 B 체크포인트: 일반6개 연속(정상5/시간제1), 광고1 별도, 요청7/확인7·blocked=false. 정상125.416초 영상에도10초 타이머를 적용하지 않고 기존N완주를 기다렸다. 20개 기준과 추가 중단/복귀 기기시험은 미완료. 전체245시험은 기존 미연결 실험2실패, 연결225PASS를 구분했다. 독립 재리뷰·문서105개 로컬 링크·산출물 동일성 확인, 기기10초/실행ON 유지.

## 2026-08-27 · 인스타그램 무진행 시간제 보조 설계·구현 중

기본15초·숫자입력·±1초·5~120초·별도기본OFF토글. 정상진행과YouTube는기존N회,시간은독립총대기시간. 정지/댓글/앱전환초기화,플로팅남은초,엄격한전환확인. [계약](TIMED_FALLBACK.md). 오디오반복실험을제품에연결하지않는다. 아직빌드/기기검증/게시완료아님.

## 2026-08-27 · 음향 후보 거부 진단0.2.1·실제 미검출 경로 확인

판정/임계값/권한불변의 누적프레임품질·후보거부집계 추가.48시험/빌드/lint(0오류6경고)/독립리뷰후시험앱만업데이트,88989bytes·설치본SHA일치. 실제60.167초에서572/598유효특징·리셋0인데후보0,개별평가776회거부를확인했다. 소리수신불가가아닌후보선택/검증실패이며정확N/20연속해결아님. [실측·한계](AUDIO_PATTERN_TRIAL.md). 임계값추측수정·제품통합·새공개·메일없음.

## 2026-08-27 · 음향 반복 후보 분석 구현·시험판 설치

별도시험앱0.2-audio-pattern에서 주파수 특징 순서와3~25초 주기후보를 RAM 비교하도록 추가. 음소거 사전확인과 후보/영상끝 미확정 문구, 분석비용 진단, 개인정보를 제외한 관측 스크립트 추가. [시험 계약](AUDIO_PATTERN_TRIAL.md). 빌드/lint/38합성·기존시험/정적검사/독립리뷰 PASS,65389bytes 후보A USB설치·설치본SHA256일치. 첫60초기기세션은 중간일시정지로 주기정확도 판정제외,수신·자동종료 확인. 주기대조/20연속미완료,기존제품/공개/메일변경없음.

## 2026-08-27 20:15 · 내부 오디오 실제 수신 성공·반복판정은 별개

후보A기기2세션관측. 초기무음은미디어음량0/IG자체음소거상태였고,2번째IG음소거해제·재생후최대-11.07dBFS/peak17222/최종648신호구간확보.60초자동종료2회및서비스/Projection해제확인. [시험조건·한계](AUDIO_PROBE_TRIAL.md#기기-수신-시험--device-capture-test). 폰음량0·IG음소거·재생복원,기존앱불변. 반복판정/20연속미검증,코드·APK교체·새공개·메일없음.

## 2026-08-27 · 내부 오디오 수신 시험 앱 분리

기존 앱을 바꾸지 않는 `audio-probe` 진단 앱 추가. Instagram UID+MEDIA의 오디오만 최대60초/RAM집계, 사용자 직접 OS 권한·캡처동의, 마이크입력·저장·전송·제스처없음. [상세 계약·사용법](AUDIO_PROBE_TRIAL.md). 후보A 빌드/13시험/정적검사/독립리뷰 후 USB설치·설치본해시일치. 19:04~05 오디오권한창대기, 실제수신/종료E2E미실행/반복판정미구현. 새 공개·메일 없음.

## 2026-08-27 ·0.2.3-visual-test 캡처 보조 구현·A/B/C 설치시험

- 후속:전체구간비교미연결코어20시험중2실패하여통합보류.대체시간원으로재생음비교를검토하고설치IG의재생음캡처허용flag/최근USAGE_MEDIA확인. 실제오디오캡처는미실행이며별도OS권한동의가필요하다. C화면분석OFF·기존실행ON복원.누계일반4회전환은수동이동으로나뉜구간이며최대확인연속2회,20기준미달.

- 기본OFF/별도동의인화면분석보조추가.IG단일무시간영상만창캡처,RAM전용,기존시간우선,학습추가재생·추정표시·실패수동안내. [시험계약](VISUAL_ASSIST_TRIAL.md).
- A 라우터메타데이터누락을기기frames0으로발견,D-023에기록. B withIdentity보존·3시험·정적연결검사·독립리뷰로수정,실제캡처진입확인. C는초기화원인/숫자진단추가,판정동작불변.
- C빌드/lint·193시험PASS,설치본해시대조.동의취소/확인/업데이트후설정유지/일시정지캡처차단확인.정지에가까운문제영상130초후캡처중단,움직이는영상의반복후보검증실패도확인.화면추정실제이동0.
- 정상시간기반2개연속자동이동후다음그림형무시간영상정체.20연속기준미달로새게시/메일없음.전체구간비교방식추가시험준비중. 상세수치와한계는[검증](VERIFICATION.md),개인화면/원시로그는private에보관.

## 2026-08-27 · D-022 숫자 이벤트·화면 반복 실험·연속 시험 실패

- 당시 검증 목표: 수동 개입 없는 일반 영상20개 이상 자동 진행 확인 후 새 빌드 Public 게시. [후속 조사](INSTAGRAM_TIMING_RESEARCH_2026-08-27.md)에 공식 API/원시 숫자 결과/안전 한계 누적.
- 제품 밖 EventProbe·PC 화면 특징 실험·기기 VisualProbe/VisualCycleMath 추가. 재생 중 숫자 이벤트 없음, 수동 일시정지9건으로 수집 경로 확인. PC 정지 화면이 가짜 후보를 반환하는 한계를 확인하고 Java 경로에 정지 화면 거부 적용, 합성3개 및 기기32정지표본 확인.
- 무재생바A10.4초 후보 두 번 재현, B14.75초 후보, 정상16.205초→약16.2초 대조. 이는 후보 탐지이며 정확한N회/자동 넘김 성공이 아니다. 약2.1주기 학습·영상 내부 반복·시작점 불명 문제는 독립 검토로 명시.
- 제품ON 복원 후 수동 개입 없이16.205초 정상 영상1회 자동 이동, 다음 영상에서0/정보 없음 재현.20개 기준 미달. 화면 캡처 capability 추가·시험 APK 설치는 이 단계에서 미실행. 제품 코드/권한/설치/새 빌드/게시/메일 변경 없음.

## 2026-08-27 · D-022 대체 시간원 인터넷 조사·실기기 시도

- 공식 Android API와 공개 Android/브라우저 구현 조사. [후속 조사 문서](INSTAGRAM_TIMING_RESEARCH_2026-08-27.md)에 출처·시도·한계·미구현 대안 기록, 인수인계/대장/검증/프로브 안내에서 연결.
- 읽기 전용 InstagramTimingProbe 추가, 컴파일/dex·기기 JAR 해시 일치. 초기 리뷰 지적을 반영해 자식별 refresh·문장 내부 시간패턴 후보 탐지·출력량 보강. 문제114노드4표본 시간원 미검출, 정상116노드4표본10.707→13.132/26.1초 진행 확인.
- 일시정지 UI 조회·MediaSession 후보도 시간원 확보 실패. 이벤트 조사는 판정 불가. 출력 절단/실행 중 수동이동 겹친 시험은 안정된 콘텐츠 대조에서 제외.
- 실행ON·기존 설정 복원,10.014→12.848/26.1초 감지 확인. 제품 코드/설치/권한/게시/메일 변경 없음. 시간 제한 보조 모드는 정확한N회와 다른 요구이므로 미구현.

## 2026-08-27 · D-022 USB 비교 진단·재생 막대 미검출 확인 (미수정)

- 실제 설치본0.2.2/code9 확인. 짧은12개 라벨 비교 구간 중02/05에서0/1·재생 정보 없음 관측, 나머지10구간에서 시간 진행·현재1 확인. 수동 진단 이동과 제품 자동 이동은 구분하며 모든 영상 완주 시험은 아님.
- 추가 문제 장면에서 실행OFF 후 기존 읽기 전용 프로브 분리 조회. 두 장면 각각14표본 scrubber 미검출, 정상 대조에서26.1초 RangeInfo 확인. 현재 reader의 시간원 미검출→카운트 초기화/대기 경로 확인; 원본 링크·모든 대체 시간 정보 부재까지 확정하지 않음.
- 프로브 후 서비스 재생성으로 누계 초기화. 실행ON·기존 선택/횟수/광고/플로팅 설정 복원, 시간11.207→15.640/26.1초 정상 읽기 확인. 복원 직후0은 중간 재생 시작 대기였음.
- 독립 증거 리뷰 완료. [디버그 대장](DEBUG_LOG.md)·[검증 기록](VERIFICATION.md)·[인수인계](../HANDOVER.md) 갱신. 실제 계정/콘텐츠 화면·로그는 private에만 보관.
- 제품 코드·설치·권한 변경 없음. 새 빌드/수정 후 회귀/게시/메일 없음. D-022는 원인 범위를 좁혔지만 미수정·미해결.

## 2026-08-27 · 일부 Instagram 카운트 정지 초기 조사 (D-022)

- 일부 영상의 카운트 정지와 관련된 감지/카운터/대기 조건을 읽기 전용 확인. 기기 미연결·링크 조회 제한으로 직접 원인 미확정.
- 카운트0/N 대기와1/N 이상에서 반복 경계 누락, 전환 실패 안전정지를 구분하여 후속 재현할 계획만 기록.
- 초기 증상 정보: 해당 영상 진입 시 현재 회차0으로 바뀌고 지속. 이 단계에서는 기기 직접 관측 전이며, 특정 영상 감지 대기 외에도0을 표시하는 경로가 있어 직접 원인 미확정 유지.
- 제품 코드·설정·설치·공개 배포 및 시험 결과 변경 없음. DEBUG_LOG/HANDOVER 로컬 갱신, 새 빌드/기기 E2E 미실행.

## 2026-08-27 · 0.2.2 Public 게시·다운로드·CI 검증 완료

- [Public 저장소](https://github.com/fullmetalsonic/shorts-loop) 생성, main 소스95a4239844ba35a929b2719e8e935b7eb2d3ea14 및 v0.2.2 태그 게시. 개발 서명 APK와SHA256 파일을 prerelease로 공개.
- 비로그인 HTTP200 및 공개 첨부 재다운로드 크기627713/SHA25638A283A6780295BB30E1482ED2535C5FE4204B4B5F0180D2C3326D60E3675B58 일치. checksum 첨부도 원본과 일치, APK v2서명 PASS.
- [GitHub CI33049522094](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33049522094) SUCCESS. Linux Gradle testDebugUnitTest148개(실패/오류/skip0), assembleDebug,lint0오류1경고. 보고서 다운로드·XML 합산 확인. 기존 Windows 직접JUnit148과 구분.
- 독립 공개 준비 리뷰 PASS:82파일/로컬 문서 링크/개인자료·키 제외/버전·미해결 상태 확인. 별도 위치의WindowsSDK는 ANDROID_HOME 설정 필요 문구 보완.
- 새 메뉴 실제 기기검사·0.2.2 설치 및 추가 재생 디버깅은 이 단계에서 미실행. 메일 미발송.

## 2026-08-27 · 0.2.2 배터리 안내 메뉴·공개 시험판 준비

- 자체 앱만 배터리 제한 없음 설정. 예외 목록/커널 frozen=0 확인, 약56초 PiP대기50표본 응답 유지와 전체 화면 시간 추적 재개 확인. D-020 조치 후 제한적 개선이며 전체 안정성 PASS 아님.
- 사용 준비에 배터리 예외 상태·안내 대화상자·자기 앱 설정 열기 추가, Activity 복귀 시 재조회. 새 권한·자동 설정 변경·실행 재시작 없이 독립 패널로 분리.
- versionName0.2.2/code9. 로컬 빌드/시험 컴파일/lint0오류2경고·직접JUnit148·정적 안전 연결 검사5종 PASS. 배터리 변경 독립 정적 리뷰 PASS.
- D-019 전환 실패 재발과 D-021 18초영상16→0 경계 누락은 미해결로 기록. 추가 휴대폰 조작/0.2.2 설치/새 메뉴 실기기 검증은 이 단계에서 중단.
- Public 시험판용 한영 README 설명서/릴리스 노트/CI 구성/공개 제외 규칙 추가, 과거 APK·증거 보존. 게시·다운로드·CI 결과는 후속 절에 누적한다. 메일 미발송.

## 2026-08-27 · 0.2.1 전체 화면 복귀 무응답 원인 확인 (D-020)

- 전체 화면 복귀 후에도 Samsung Freecess가 자체 앱을 동결. 커널 frozen=1,동결목록,15:56~16:04 동결/일시해제/재동결 시각으로 확인. 단순 횟수 누적이라는 증거는 없음.
- 독립 진단 리뷰 완료. 진단 SIGQUIT으로 잠깐 깨어난 것을 자연복구로 보고하지 않음. 임시 디버그포트 제거,root요구도구 중단. 설정/권한/코드/APK 변경 없음.
- 자체 앱 백그라운드 절전 예외 비교 시험은 이 단계에서 미실행. 최초15:46 넘김 확인 실패 원인은 별도 미확정 유지. 빌드·새Unit·안정성PASS·GitHub게시·메일 없음.

## 2026-08-27 · 0.2.1 YouTube 간헐 정지 진단 (제품 수정 없음)

- 후속 작은 창 시험에서 홈+YouTube PiP,초기 launcher대기/blocked=false 확인. 이후 브라우저 전면 상태에서 서비스 진단 응답 timeout 관측. ADB/프로세스/접근성 등록 유지,원인 미확정. 토글·창 조작 없이 읽기 전용 조사했으며 전체 화면 복귀 비교 필요.

- 요청11/확인10 뒤 넘김 확인 실패 안전정지가 지속됨을 실기기 확인. 권한 누락이나 두 앱 감지 혼합으로 단정하지 않음. 최초 실패 조건은 미확정(D-019).
- 자체 타일OFF→ON 및 패널 닫기 후 비조작 관측59→58→15→11초, 추가 자동 요청/확인3/3 성공. 코드·APK·권한 변경 및 재빌드 없음. 전체 안정성이나 근본 수정 PASS 아님.
- 과거 '시험용 YouTube PiP닫음' 기록 정정: 설정 화면에는 남아 있었고 최종 Instagram 화면에는 보이지 않았음. YouTube 프로세스 완전 종료를 확인한 것은 아님.
- GitHub 배포 준비 전 단계로 저장소 공개 범위 미정, 한영 README 확대 미완료. 외부 게시·메일 없음.

## 2026-08-27 · 0.2.1 광고 토글·우하단 배지 수정·실제 광고 이동

- skip_ads 기본OFF, 실행ON/현재1이상/Instagram선택 조건. OFF면 광고는 수동,0이면 전체자동넘김중지. 앱 내 안내 추가.
- 상단 종료형 광고와 우하단 동작열 광고 배지를 구조적으로 인식. 재생바 부재만으로 판정하지 않고 캡션/계정의 광고 문구 제외.
- CTA를 클릭하지 않고 릴스 pager ACTION_SCROLL_FORWARD1회, 실제 다른 페이지 확인 후 카운트 재개. 같은/연속 광고 불확실 시 안전정지.
- 후보b에서 실제 같은 광고 OFF 요청0→ON 광고 요청1/확인1→26.799초 일반 N1→37.313초 영상. 0회 실제 광고 요청불변 확인. 최신c Instagram미선택 대기 및 YouTube43초→5초→43초 N1 전환 확인.
- 독립 리뷰의 전환중 회전/드래그 보호취소 P2 수정. 최종 빌드/lint0오류2경고/직접JUnit148/정적 연결 검사/독립 재리뷰PASS. 해당 순간 실제 회전/드래그는 미실행으로 유지.
- 최종c 설치·서명·재추출 해시 일치:644,351bytes,8932952A74FBCB6B131A6D020DA8385CEF86701B231EB4AB996F092E9B7B1799. 이전 APK/증거 보존.
- 최종c 타일OFF/ON색상·서비스 상태 및 Instagram 복귀 후 광고1회 이동→20.454초 일반릴스 재개 확인. 최종1회·광고ON·플로팅OFF·두앱선택 상태. 새 광고 안내문 실제 화면 확인, 시험용YouTube PiP닫음.
- DEBUG_LOG D-015~018, 광고설계/제품/사용/UI/검증/인수인계/README 갱신. 하단 높이·명칭 변경은 제안만, 별도 창 팝업 자동닫기는 미구현. GitHub/메일 미수행.

## 2026-08-27 · 실제 UI 시험 및 광고 즉시 넘김 설계

- 잠금 해제 후0.2.0 실제입력·0/99·100거부·IME완료·두모드·플로팅드래그/위치/X·선택표시·실행토글 확인. 라디오행빈영역/하드웨어Enter뒤컨테이너강조 수정,각빌드120시험PASS.
- Instagram첫일반영상의무진행바/시간정보없음 재현. 다음21.708초영상2회완주후광고로실제이동했으나광고의무시간정보때문에구버전전환확인이실패. 정식전체E2E PASS아님.
- 광고를완주없이즉시넘기는별도토글설계. 광고명시label 실측,재생바부재만으로광고판정금지. [광고설계](ADS_PLAN_2026-08-27.md)와0.2.1코드/시험작업진행.

## 2026-08-27 · 0.2.0 설정·플로팅 개선 구현·설치, 실기기 잠금 상태

- 0~99 숫자/화살표, N/M 분리, 두 터치 모드, 앱 선택, 선택적 플로팅, 단일 실행/타일 토글 구현.
- 플로팅124×56→72×56dp, 배경 실효 불투명도 약40%. 숫자48dp 유지, 모서리X24dp는 전체 크기를 줄이기 위한 작은 터치영역 절충.
- 인앱: 횟수→앱→하단 고정 실행 순서, 큰 입력/행 터치, 필요 시에만 플로팅 상세·권한 안내. 네이티브 UI와 아이콘의 남색·청록·보라색 계열 연결. Figma 미사용.
- Instagram 전용 reader·선택 앱만 읽는 router·앱 변경 세션 분리 구현. 사전 관측과 제품 자동 넘김 검증은 분리.
- image_gen으로 선택 시안의 투명 전경 PNG 생성, adaptive/단색 아이콘 적용. 원본 보존, [자산·프롬프트](../assets/icon-concepts/ADAPTIVE_ASSET.md) 기록.
- 코드·독립 리뷰에서 null root의 전환보호 취소, 위젯복원에 의한 재시작, 숫자 붙여넣기 변형 위험 수정(D-011~013).
- 최종빌드/lint PASS(오류0/경고2), 직접JUnit120·권한/모듈·입력구성 검사 PASS. 독립 검토자도120개 재실행, 정적 리뷰 범위 미해결CRITICAL/HIGH0. 실제 UI/키보드/E2E는 미실행.
- 0.2.0 candidate-c 설치. 휴대폰 보안 잠금으로 새 화면 시험 불가. 설치 해시와 후속 상태는 [검증](VERIFICATION.md)에 기록.
- [사용 설명서](USER_GUIDE.md), [제품 기준](PRODUCT_SPEC.md), 신규[UI 기준](UI_DESIGN.md), README·인수인계·검증·디버그·색인 갱신. 과거 버전 산출물/이력 보존. GitHub/메일 없음.

## 2026-08-27 · 다음 업데이트 사전 조사·아이콘 시안 (앱 미구현)

- 7개기능 개선과 아이콘 스타일/Instagram 지원 조사 범위를 [설계안](UPDATE_PLAN_2026-08-27.md)에 요구·상태·이전 규칙·검증 계획으로 기록. 이 단계는 구현 전 설계 상태.
- 독립 검토: 기준N/현재M 분리, 선택적 오버레이 권한, 0/실행/표시/X 의미, 폭 절반과 독립48dp터치영역 충돌 확인.
- Instagram19.014초/22.638초 릴스의 숫자 시간과 끝→처음 반복 관측. 영상 내부 상향 ADB스와이프1회로 다음 영상 확인. 앱 N회 자동 넘김은 미실행.
- SDK 읽기전용 진단 프로브 추가, 자체 앱OFF에서만 실행. 숫자/상태만 출력. runner 누락/idle 지연을 제품 문제와 분리하여 검증·디버그 기록에 남김.
- image_gen으로 남색·네온·입체 재생/반복 홈아이콘 시안 생성. assets/icon-concepts에 이미지·프롬프트·해시·적용 주의점 보관. 앱 미적용.
- 앱 소스/매니페스트/권한/설치APK 변경 없음. GitHub 게시/메일 없음.

## 2026-08-27 · 0.1.0 개발 시작

- 초기 기능 범위: 반복 횟수 0/1/2, 로터리 탭, 드래그 위치 저장, 반투명 플로팅, 현재/목표 숫자, X, 빠른 설정 타일.
- 0(플로팅 유지)과 X/OFF(플로팅 숨김)를 분리하고 마지막 비영 설정을 저장한다.
- 앱 단독 동작 구조. 무선 ADB는 개발/시험에만 사용한다.
- 개발환경: Java 17 소스, JDK 21 실행, AGP 8.7.3, Gradle 8.9, compile/target SDK 35. 기존 SDK 설치는 변경하지 않는다.

## 2026-08-27 · 0.1.0 구현/자동검증

- 모듈화한 파서/반복기/전환보호/설정/시간조회/플로팅/서비스/타일/설정 화면 구현.
- 화면 조작과 자동 넘김 경쟁을 줄이는 재조회, 플로팅 드래그 중 중지, OFF/X 우선 취소, 전환 확인 타임아웃 추가.
- 위치 상대 저장 및 회전/화면크기 변경 보정. 72% 불투명도, 숫자·X 각각 최소 48dp 터치영역.
- 단위·회귀시험 54개 작성 및 직접 JUnit 실행 PASS. 빌드와 lint PASS(OldTargetApi 1경고).
- 독립 리뷰 2건 수정, 독립 재리뷰 및 검토자 시험 재실행 PASS.
- Gradle test runner의 한글경로 로딩 실패와 aapt 경로 인코딩 제한을 디버그 대장에 별도 기록.
- README/상세 사용 설명서/제품 기준/검증/디버그/인수인계 작성·연결.
- 이 단계에서는 실기기 설치 전이며 접근성/오버레이 권한은 변경하지 않음.
- GitHub 게시 및 메일 발송 없음. 아이콘은 기능 확인용 임시 벡터만 사용.

## 2026-08-27 · 설치 및 실기기 시험 / 0.1.1~0.1.2

- 설치/동작 시험 시작. 접근성과 다른 앱 위 표시 권한은 사용자가 직접 설정.
- 0.1.0 설치 후 플로팅 숫자 순환/드래그 후 값 유지/위치 저장/복원/X 종료 확인.
- 0.1.1: 서비스 상태를 숫자와 고정 문구만으로 조회하는 dump와 비공개 관측 스크립트 추가. UIAutomator 시험 간섭을 피함.
- 조사 증상: 창 축소, 스와이프 방향, 카운트 고정. 창 축소는 설정 앱 전환 중 YouTube PiP 관측이며 당시 자동 제스처 요청0. 자동 넘김은 아래→위로 구현되어 있음.
- 0.1.2: 진행시간 41/52 고정 재현 후 SeekBar 최신 상태 요청 추가. 실패/소멸 시 보수적 대기. 독립 변경 리뷰 PASS.
- 0.1.2 빌드/lint/직접 JUnit54 PASS. 설치된 APK를 다시 가져와 로컬 산출물과 SHA-256 일치 확인.
- 실기기 세부 결과와 미실행 항목은 [검증 기록](VERIFICATION.md)에 누적.

## 2026-08-27 · 0.1.3 진단 / 0.1.4 갱신 지연 보정

- 0.1.3: 반복기 초기화 사유와 숫자 차이/경과시간 진단 추가. 영상 식별값·제목·계정은 출력하지 않음.
- 0.1.4: 정상 14→16초 갱신을 수동 탐색으로 오인하는 문제 수정. 최근3초 표본의 누적 속도 검사로 시간표시 갱신 오차 허용과 빠른 반복 탐색 차단을 함께 적용.
- 회귀시험 7개 추가, 전체61 PASS. 빌드/lint PASS. 실기기에 업데이트 설치.

## 2026-08-27 · 실기기 검증 및 0.1.5 창 보호

- 0.1.4 실제 목표2: 52초 영상 두 번 완주 후 다음. 목표1: 21/45/30초 3개 연속 후 다음. 요청과 확인 총4회 일치, 중복 없음.
- 0: 약87초 동안 요청 증가 없음/0/0 유지. 일시정지/댓글 대기, 플로팅X, 타일 ON/OFF 실제 활성색과 길게 설정 열기 확인.
- 추가 안전시험에서 빠른 설정 뒤의 영상 시간을 계속 읽는 문제 발견. 실제 자동 제스처 전에 X로 중단.
- 0.1.5: 창 메타데이터의 입력초점/ID/앱 유형/PiP를 확인하여 조회와 제스처 직전에 동일 검사. 다른 앱 내용 탐색 및 새 uses-permission 없음.
- 빌드/lint/JUnit61 및 독립 변경 리뷰 PASS. 설치 APK를 재추출해 로컬 50,055bytes와 SHA-256 일치. 빠른 설정 대기/복귀 감지 실기기 확인.
- 전체 팝업/폴드/잠금/장시간 배터리 한계는 검증 기록과 설명서에 명시. 디자인/GitHub/메일 범위 확장 없음.
- 0.1.5 최종 실기기 회귀 PASS: 빠른 설정 복귀 후42초 영상 두 번 완주→다음36초 영상, 요청1/확인1. 시험 종료로 X OFF, 목표2·위치·타일 유지. 최신 소스와 설치APK의 버전0.1.5/해시 일치 확인.
