# 0.4.0 실사용 점검표 / Field checks

대상은 **ShortsLoop 0.4.0/code33**입니다. 아래는 아직 실시하지 않은 실폰 점검 순서이며 성공 기록이 아닙니다. USB·무선 디버깅 없이 앱 업데이트 후 확인할 수 있습니다. 차량을 운전하거나 보행 중 화면 조작이 위험한 상황에서는 시험하지 마세요.

For **ShortsLoop 0.4.0/code33**. This is an unrun field checklist, not a pass report. No debugging connection is required. Test while stationary and safe, never while driving or in unsafe walking conditions.

## 1. 준비 / Prepare

1. 기존 앱을 삭제하지 않고 업데이트한 뒤 인앱 하단에서0.4.0을 확인합니다. 전체 실행은OFF로 둡니다. 기존 YouTube·Instagram 설정이 유지되는지 봅니다. / Update in place,confirm0.4.0,keep executionOFF and check preserved settings.
2. 사용할 앱을 선택합니다. TikTok은 처음에는 선택OFF입니다. 플로팅은 시험 중 상태 확인용으로 켜면 편하지만 필수는 아닙니다. / Select hosts;TikTok starts deselected. Floating controls are optional but useful for observing status.
3. 접근성 연결 상태를 확인합니다. 연결되지 않았으면 Android 설정에서 이 앱의 접근성을OFF→ON하고 돌아옵니다. / Reconnect accessibility manually if disconnected.

## 2. 일반 영상과0회 / Ordinary videos and zero

- 한 앱씩 선택하고 독립 광고·사진·라이브·긴 영상 옵션은 잠시OFF로 둡니다. / Select one host and temporarily disable independent filters.
- **1회:** 짧고 진행정보가 있는 일반 영상에서1/1 이후 자동으로 다음 영상에 가는지 확인합니다. 중간에서 시작하면 다음 처음을 기다릴 수 있습니다. / At1 play,check a full observed cycle and automatic transition.
- **2회:** 1/2→2/2→다음의 순서를 확인합니다. / At2 plays,check the two-cycle sequence.
- **0회:** 일반 영상이 자동으로 넘어가지 않는지 확인합니다. / At0,ordinary videos must not auto-advance.
- YouTube·Instagram을 각각 확인합니다. TikTok은 추천 피드의 읽을 수 있는 일반 진행정보만 대상입니다. 진행정보 없음·광고·LIVE·사진에서 기다리는 것은 미지원 상태이며 성공으로 세지 않습니다. / Check each host separately;unsupported TikTok pages waiting are not successful transitions.

## 3. Instagram 시간제와광고 / Instagram timing and ads

1. 시간제ON·반복1·대기3초로 진행정보 없는 일반 릴스를 확인합니다. 총3초에 최초 판별2초가 포함됩니다.2초·60초 입력도 저장되며0초·1초는 거부해야 합니다. 반복0이면 시간제도 중지합니다. / Test3s total including qualification,valid2/60 bounds,rejected0/1,and count0 disabling timing.
2. 광고ON·반복0·다른 독립 옵션OFF로 광고만 넘기는지 확인합니다. 광고 대기를 **0.0초→0.3초→1.3초**로 각각 적용하여 서로 다른 시험 구간으로 기록합니다. / With adsON,count0 and other filtersOFF,test separate0.0/0.3/1.3s intervals.
3. 각 구간에서 광고가 넘어간 것과 **다음 일반 영상의 첫 카운트가 시작된 것**을 따로 확인합니다. 일반 반복을 볼 때는 반복1로 바꿉니다. / Distinguish skipping the ad from counting the next video;set count1 for the latter.
4. 양수 광고 대기는 같은 광고의 식별 근거를 읽지 못하면 대기할 수 있습니다.0.0초로 비교할 수 있으나 광고를 반복해서 강제로 넘기는 시험은 필요하지 않습니다. / Positive delay may wait for identity;compare0.0 if needed without repeated forced swipes.
5. 광고 대기 중 댓글·다른 화면·전체OFF로 바꾸면 이전 광고의 지연 요청이 나중에 실행되지 않아야 합니다. / Old delayed requests must not fire after comments,screen changes or executionOFF.

## 4. 여러 창 / Multiple windows

- 여러 앱 동시 적용ON·전체ON을 유지하고 **한 앱 전체화면→두 앱 분할→가능하면세 앱→다시한 앱**으로 바꿉니다. 기기가 지원하는 배치만 시험합니다. / Follow1→2→3 if supported→1 visible hosts.
- 앱별 횟수를 다르게 설정합니다. 예:YouTube1,Instagram2,TikTok1. 각 플로팅의YT/IG/TT와 숫자가 다른 앱에 섞이지 않아야 합니다. / Different targets must remain isolated by host label.
- 한쪽 X를 누르면 그 앱만 멈추고,전체OFF는 전부 멈춰야 합니다. 숨긴 앱에 자동 입력이 없어야 합니다. / X pauses one;masterOFF stops all;hidden hosts receive no input.
- 앱 순서·회전을 바꿔 보되 중간 수동 조작은 연속 자동 성공에 합산하지 않습니다. 한 앱이 자체 일시정지하면 강제로 재생하지 않는 것이 정상입니다. / Order/rotation/manual edits break a continuous test segment;host pauses are not forcibly resumed.

## 5. 멈췄을 때 / If it waits or stops

1. 설정 횟수0인지,현재 카운트0인지 구분합니다. 설정이1/2인데 현재0이면 시작 또는 진행정보를 기다릴 수 있습니다. / Distinguish configured zero from current zero.
2. 댓글·메뉴·키보드·가림·PiP·호스트 자체 일시정지를 확인합니다. 안전한 일반 영상으로 돌아옵니다. / Clear unsafe UI and return to a supported ordinary video.
3. **전체OFF→ON**으로 새로 시작합니다. TikTok 목적지 확인 실패 후 수동으로 지원 영상을 선택해야 할 수 있습니다. / Restart masterOFF→ON;TikTok may require manually choosing a supported destination.
4. 계속 재현되면 앱 이름,설정 횟수,영상/광고/사진/LIVE,전체/분할,표시 상태,재개 방법만 기록합니다. 공개 이슈에 계정명·개인 메시지·전체 화면·기기 식별자를 올리지 마세요. / Report only host,settings,content type,layout,status and recovery;omit private content and identifiers.

## 기록 기준 / Recording results

자동 넘김은 실제로 다음 게시물이 확인된 경우만1건입니다. 수동 넘김·요청 표시·플로팅 숫자 변화만으로 성공을 세지 않습니다. 일반·시간제·광고·사진·라이브·긴 영상은 각각 나눕니다. 안전정지와 미지원 대기는 실패 원인/제한으로 기록합니다.

Count only confirmed automatic next-post transitions,not manual swipes,requests or number changes. Record transition types separately and retain waits/stops as limits or failures.

목표는 단독 TikTok 일반10연속과 별개로,세 앱이 실제로 재생 가능한 기기에서 **앱별10회·총30회 이상**입니다. 아직 달성한 결과가 아니며 테스트 시간이 부족하면 관측한 수만 기록합니다. 새 기능을 확인한 뒤 원래 선호 설정으로 되돌리세요.

The unrun target is ten consecutive ordinary TikTok transitions plus,where actual triple playback is possible,ten per host/thirty total. Record only what was observed and restore preferred settings afterward.

[사용법](USER_GUIDE.md) · [검증 원장](VERIFICATION.md) · [0.4.0 릴리스](releases/v0.4.0.md).
