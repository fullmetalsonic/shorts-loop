# TikTok 화면 구분 사전 관측 / TikTok screen-identification observation

2026-08-29 · **현재 화면의 구조 근거 확보, 제품 미구현·추가 조사 대기**.

## 후속 · 6개 페이지 신속 비교 / Six-page follow-up

같은 설치본에서 다음 페이지로 이동하며 서로 다른6개 화면을 비교했다. 수동 스와이프 명령은7회였고첫1회는 같은 게시물이 유지되어 전환 성공에 포함하지 않았다. 이후6개는 실제 화면 변경을 확인했다. 제품 자동 넘김이나6회 반복 완료 시험이 아니다. 각 페이지에서4회캐시비움 구조표본을 얻었고 영상 끝까지 기다리지 않았다. 최초 한 화면만 본 아래 기록과 구분한다.

| 도착 순서 | 화면 관측 | 접근성 진행값(범위0–10000) | 분류 근거 |
| --- | --- | --- | --- |
| 1 | 일반 영상 | 숨겨진SeekBar,0유지 | 영상 표시 구조는 있으나 유효 진행값 없음 |
| 2 | 틱톡 리워드 초대 홍보 화면 | RangeInfo 없음 | 시각적으로 홍보/참여 화면,현재 페이지에별도`zb1`→`zax`구조 |
| 3 | 일반 영상·진행바 표시 | 5754→6037→6327→6616 | visible SeekBar가 실제 증가 |
| 4 | 일반 영상·진행바 표시 | 2316→2510→2704→2895 | visible SeekBar가 실제 증가 |
| 5 | 일반 영상·진행바 표시 | 1205→1302→1399→1494 | visible SeekBar가 실제 증가 |
| 6 | 일반 영상·진행바 표시 | 4478→4674→4968→5261 | visible SeekBar가 실제 증가 |

세 관측 유형은 **진행값 있는 영상4·진행값 없는 영상1·리워드 홍보1**이다. 진행값은 정규화된 범위이며 초/전체길이로 해석하지 않는다. 실제 반복 끝→처음,수동 탐색/정지와 반복의 구분,광고 자동 인식/입력은 미검증이다. 사진/LIVE 표본은 이번6개에 없었으며 상업광고 전체와 틱톡 자체 리워드 홍보를 동일한 판별로 처리한다고 주장하지 않는다.

중요한 반례: **홍보 화면에도visible `player_view`와`TextureView`가 남았다.** 따라서 선택된 추천 탭+영상 컨테이너만으로 일반 영상이라고 판단하면 홍보를 잘못 포함한다. 현재 페이지의visible `zb1`→`zax`는 홍보 표본에만 있었고나머지5개에서는 관측되지 않았으나,난독화ID이고표본1개이므로완성된광고감지규칙이아니다. 다음 구현 때별도홍보/광고차단과추가정상·음성대조가필요하다. 최초한화면에서시계미관측이었던결론은이후4개영상에서진행값읽기성공으로범위가확장됐으며,재생길이/반복카운트가확인됐다는뜻은아니다.

이 구간에서는 제품/설정/권한 변경,APK 설치,게시를 하지 않았다. 실제 계정/영상 캡처와 구조 출력은 비공개로보존했다. 조사종료후기존서비스프로세스/연결/실행상태유지,두기존앱비표시를확인했다.6개비교후추가이동없이대기한다.

EN: After one unsuccessful movement attempt, six distinct destination pages were visually confirmed with four cache-cleared reads each:one ordinary video with a hidden zero-valued range,one TikTok referral/reward promotion without a range,and four ordinary videos with visible increasing0–10000 progress values shown above. These are three observation categories,not three universally verified content detectors. Ranges are not seconds/duration;natural loops,pause/seek discrimination,product counting and automatic input remain untested. The promotion retained a visible player/TextureView, disproving sufficiency of the initial player-only candidate. Visible `zb1`→`zax` appeared only in this promotion among the six pages, but one obfuscated-ID example cannot establish general ad detection. No photo/LIVE example was encountered. Product/settings/permissions/APK and publication are unchanged;private evidence is preserved and further movement is stopped.

## 범위 / Scope

설치 TikTok46.7.3/code460703, 패키지 `com.ss.android.ugc.trill`, 단말 Android API37에서 현재 추천 영상 한 화면만 읽었다. 설치 APK가 보고한 minSdk23/target36은 이 설치본의 정보이며 향후 전체 TikTok 지원 OS 정책을 확정한 것이 아니다. 기존 기획의 `com.zhiliaoapp.musically`와 구분해 이 패키지도 실제 지원 후보로 기록한다. 두 패키지의 내부 구조가 같다는 가정은 하지 않는다.

영상 넘김·일시정지·다른 탭 이동·광고/LIVE/사진 탐색은 하지 않았다. APK 설치·권한 변경·TalkBack 활성화·제품 TikTok 등록도 하지 않았다. 이 기록은 범용 감지기나 자동 넘김 성공을 뜻하지 않는다.

EN: Observed one currently open recommendation video in installed TikTok46.7.3/code460703, package `com.ss.android.ugc.trill`, on device API37. This differs from the original package candidate. No navigation, pause, special-content survey, APK installation, permission change or product integration was performed.

## 확인 결과 / Evidence

| 항목 | 실제 관측 | 해석 |
| --- | --- | --- |
| 추천 탭 | 추천 역할 노드가 visible/selected 모두 true | 추천 피드 선택 상태를 읽을 수 있음 |
| 영상 페이지 | 내부 `androidx.viewpager.widget.ViewPager` 아래 `view_rootview` 중 현재 페이지만 visible | 미리 로드된 앞뒤 비표시 페이지와 현재 페이지를 나눌 구조 후보 |
| 재생 영역 | 현재 페이지 아래 `video_container_area`→`player_view`→`TextureView`가 보이는 경계로 제공됨 | 영상 화면 구조 근거. 재생 중/일시정지 구분 자체를 증명하지는 않음 |
| 이동 기능 후보 | 해당 내부 pager에 앞/뒤 scroll action이 노출됨 | 동작 실행/실제 다음 영상 확인은 미실행. 바깥에도 같은 ID의 pager가 있어 ID 하나만 선택하면 안 됨 |
| 진행 정보 | 첫3표본의 SeekBar는 invisible,범위0–10000/값0;4번째에는 range 없음 | 실제 재생 시계로 채택할 수 없음.10000을 영상 길이로 해석하지 않음 |
| 시간 형식 | 조사한 text/description에서 엄격한 시각 형식 후보0 | 이번 화면·탐색 방식에서 미관측. 모든 속성/영상에서 불가능하다는 결론은 아님 |

캐시를 표본마다 비운4회 조회에서 노드 수335/335/335/312,프로브 자체 순회 한도에 따른 truncated0을 기록했다. 첫 표본과 마지막 표본의 시차는 약2.69초다. 이 숫자는 호스트가 모든 노드/속성을 제공했다거나 전체 트리가 원자적으로 같은 시각에 읽혔다는 보장이 아니다. 짧은 앞선 캐시 포함 탐색도 있었으나 재생 변화 판단에는 사용하지 않는다.

결론: **현재 추천 영상 화면을 감지할 구조적 단서는 확인됐다.** 일반 영상/사진/LIVE/광고/댓글 등 다른 화면 사이의 오인 방지,반복 횟수,실제 자동 넘김,여러 창에서는 아직 검증하지 않았다. 진행값이 없었다고 시간제 자동 넘김부터 구현하지 않는다. 다음 재개 시에만 해당 화면들을 대조하고 안정적인 페이지/정지/재생 근거를 확인한다.

EN: The selected recommendation tab and visible inner pager/page/player hierarchy provide positive identification evidence. Offscreen pages also exist, so visibility, ancestry and geometry matter. Pager actions were exposed but never executed. A hidden SeekBar stayed at0 with a0–10000 range in three snapshots and disappeared in the fourth;no usable playback clock was established. Four cache-cleared reads do not prove complete or atomic host metadata. Cross-screen discrimination, pause, loops, automatic advances and multi-window behavior remain unverified.

## 조사 도구·개인정보·종료 상태 / Probe, privacy and stop state

[개발 전용 프로브](../scripts/probes/TikTokReadOnlyProbe.java)는 제품 APK에 포함하지 않는다. 기존 서비스 억제를 피하는 [Android 비억제 플래그](https://developer.android.com/reference/android/app/UiAutomation#FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)를 지정하며,기본 억제 연결로 대체하지 않는다. 프레임워크 reflection은 shell 진단용으로만 사용했고 제품 구현 방법으로 제안하지 않는다. API33+의 캐시 비우기를 사용한 이번 실행은 API37에서만 확인했다.

최종 조사 JAR은4962bytes,SHA256 `A1E9FE19114E7D1250903BFDFB1F0404E62CE3B47328ADA078118159E65219F7`;로컬과 기기 전송본이 일치했다. 이후 소스 주석의 OS 설명만 API33+/실측37로 정정했고 이 주석 변경 후 JAR을 재빌드하지 않았다.4표본·700노드/48깊이·25초 상한,자유 텍스트/계정/캡션 출력 없음,제스처/네트워크/파일 저장 동작 없음. 실제 화면과 숫자/구조 출력은 로컬 비공개 자료로만 보관했다.

조사 전후 ShortsLoop0.3.0 서비스 프로세스가 같고 connected/enabled=true를 유지했다. 두 기존 대상 앱은 비표시 상태였다. 조사 프로세스는 종료됐으며 현재 틱톡을 조작하지 않고 대기한다. 새 APK·GitHub 커밋/푸시/Release·메일 발송은 없다. 독립 읽기 전용 검토에서 새 P1/P2 차단사항은 없었으며,위 미검증 범위를 유지했다.

EN: This shell-only, non-suppressing probe is not product code. Its bounded run did not inject input, grant permissions or upload content. The4962-byte JAR hash matched after transfer;only an OS comment was corrected afterward without rebuilding it. Existing ShortsLoop service identity/connectivity and settings remained unchanged. Private captures are not published. The probe exited;further work is on hold. Independent review found no new P1/P2 blocker within this narrow scope;no release or email occurred.
