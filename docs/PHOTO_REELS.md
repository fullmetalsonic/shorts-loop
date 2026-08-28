# Instagram 사진 릴스 / Photo Reels

상태: 0.2.9 로컬 구현·검증 중. GitHub 미게시. 실기기 자동 넘김 시험 결과는 아래 검증 구분을 따른다.

Status: local0.2.9 implementation under validation,not published. Physical automatic transitions are not implied by PC/emulator results.

## 설정 / Settings

- Instagram을 적용 앱으로 선택하고 ‘사진 릴스 자동 넘김’을 켠다. 최초 기본은OFF이며 전체 자동 넘김 실행도ON이어야 한다. 일반 반복0회와 무관하게 동작한다.
- ‘릴스 통째로 넘기기’: 사진 게시물 전체를 설정 시간 뒤 다음 릴스로 넘긴다.
- ‘한 장씩 보고 다음 릴스로’: 각 사진을 설정 시간만큼 보고 가로로 다음 사진으로 이동한다. 마지막 사진도 같은 시간만큼 본 뒤 세로 방향의 다음 릴스로 이동한다.
- 두 시간은 각각0–10초,초기3초로 따로 저장한다. 숫자 직접 입력→적용/키보드완료,또는±버튼으로 변경한다. 범위 밖·소수·부호 입력은 저장하지 않는다.
- 0초는OFF가 아니라 화면 안정 확인 후 즉시 동작이다. 전체 실행을끄면모든자동동작이멈추고,사진옵션을끄면사진규칙만멈춘다.
- 한 장 모드의 ‘장 번호 확인 불가 시 통째로 넘기기’: OFF는대기,ON은확인된사진릴스에서통째넘김시간을재사용한다. 통째 모드에서도 이 선택값은 보존하되 비활성 표시한다.
- 플로팅에는 사진 대기의남은초,이동확인중에는다음표시를사용한다. 사진장번호와일반영상반복횟수를혼동하여가산하지않는다.

EN: Select Instagram,enable photo automation and turn main execution on. Whole-Reel mode skips the post after its delay. Each-photo mode waits on every photo,including the last,then opens the next Reel. Both delays are saved separately:0–10 seconds,initial3. Type a whole number and apply/Done,or use±. Zero means immediate after safety checks,not off. Photo rules remain independent of ordinary repeat0. The optional unreadable-index fallback reuses the whole-Reel delay;off waits. Main execution off stops everything. The compact overlay shows remaining seconds/movement status,not an invented play count.

## 동작 예 / Examples

1. 통째0초: 사진으로 확인되고 화면이 안정되면 다음릴스.
2. 한장3초·사진3장: 각장확인후3초씩보기→다음장,마지막장3초후다음릴스. 화면안정/전환확인시간이별도로있으므로벽시계정확히9초를보장하지않는다.
3. 한장3초·통째5초·번호실패대체ON: 장번호를읽을수없는안정된사진화면에서재확인후5초를새로계산한다. 전환중번호잠깐사라짐은즉시넘김근거가아니다.
4. 대체OFF: 장번호가없으면대기. 임의로마지막장이라고가정하지않는다.

EN: Whole0 skips after safe settlement. Each3 on three photos waits3 seconds per photo,including the last;movement/settlement adds time. Each3/whole5/fallbackON starts a fresh5-second whole-post wait after qualifying an unreadable index. FallbackOFF waits. It never guesses that an unreadable index is the last photo.

## 감지·안전 경계 / Detection and safety

- 관측된 `clips_carousel_viewpager` 아래의 `clips_carousel_image_media_content`를 사용한다. 단일의충분히넓고페이지안에있는ImageView가필요하며전체가시트리의댓글/메뉴등기존보호를먼저검사한다. 별도영상노드가있다는것만으로현재사진을영상으로판정하지않는다.
- 장번호는전용 `carousel_index_indicator_text_view`에서현재/전체를읽는다. 사진번호1/2는재생횟수1/2와다르다. 모순되는숫자·여러페이지·부분스와이프·갱신실패는사진시간제로우회하지않는다.
- 사진가로이동은사진안의안전한경로에서1회요청한다. 우측버튼열·상단시스템영역·자체플로팅을피한다. 세로이동은확인한릴스pager의scroll-forward를1회요청한다.
- 가로 이동 후 같은 게시물·같은 창·같은 전체 장수에서 정확히 다음 장 번호가 안정되어야 성공이다. 세로 이동은 같은 창에서 다른 게시물 식별정보 **그리고 다른 media source-node**를 확인해야 성공이다. 캡션 변화만으로 성공 처리하지 않으며 제스처 완료 콜백도 성공 증거가 아니다.
- source-node는 창번호와 Android 노드 해시로 구별한다. A→B→A 복귀 시 A의 키는 같으며, 해시 충돌·노드 재사용은 확인 거절로 처리한다. 다음 광고가 단일 media 정보를 제공하면 전환 확인에 사용하지만, media 정보 없는 광고 끝 화면은 실제 이동했어도 안전정지할 수 있다.
- 실패/취소/확인시간초과(4.5초)는안전정지하며설정시간변경이나장번호실패대체로재시도하지않는다. 전체실행을명시적으로OFF→ON해야재개한다.
- 사진을영상파일로편집한릴스는영상규칙대상이다. 모든호스트버전과사진/영상혼합전체를지원한다고보장하지않는다. 한장모드가혼합게시물의영상항목으로이동하면사진이동확인을못해안전정지할수있다. 이후사용자가처리하거나지원정책확장이필요하다.
- 댓글/잠금/앱전환/감지된사용자조작은기존보호를유지한다. 모든비초점플로팅과손가락조작을완벽히감지하는것은아니다.

EN: A narrowly recognized image carousel and dedicated slide index are required. Incomplete/ambiguous trees,contradictory indices and partial movement do not become fallback candidates. Horizontal movement requires the exact next index in the same post/window and unchanged total. Vertical movement requires both a stable different post and a different media source-node;caption changes alone cannot confirm it. Stateless window/node-hash keys preserve A→B→A rollback. Hash collisions,reused nodes or ad end cards without media metadata may conservatively stop. Unconfirmed/cancelled movement hard-stops;changing settings/fallback cannot retry it. Explicit mainOFF/ON is required. Encoded slideshow videos retain video rules. Mixed media,other host versions and overlays remain limited;landing on a video item during each-photo mode can stop confirmation.

## 검증 구분 / Validation boundaries

- 설치0.2.8/관측호스트에서사진1/2→이전일반영상→복귀1/2→사진2/2의구조비교와ADB수동가로이동확인. 이것은신규제품자동이동PASS가아니다.
- 최신 로컬 후보는522 JUnit,API26/33/34 native25335/25349/25132 검사를 통과했다. 기본3초,0–10초 경계,독립 저장,번호 누락 대체,실패 정지,좁은 화면/큰 글꼴,KO/EN,노드 복귀와 광고 메타데이터를 검사했다. 정확한 파일/해시는 [검증 기록](VERIFICATION.md)을 따른다.
- 실폰 시험후보1EF2434F의 설치 SHA256 일치를 확인했다. 최종 배포본은 실행 내용이 같고 내장revision만 다르다. 통째0·3·10초, 한 장0·3·10초, 마지막 장 이후 다음 릴스 이동과 댓글창 보호를 확인했다. 추가 신속 검사에서8장 릴스의 가로7/7·세로1/1, 한 장10초의 가로1/1·세로1/1을 확인했다. 번호 없는 사진·사진 직후 광고·혼합 게시물은 실폰 사례 미확보이며,20개 표본 탐색은 자동20연속 PASS가 아니다. 공개 완료. 최종 파일과 CI는 릴리스 기록을 따른다. 자동 시험 구간과 수동 복귀/신속 탐색을 분리한다. [조건별 검증](VERIFICATION.md).
- [D-041 원인·예방](DEBUG_LOG.md#d-041--instagram-사진-릴스-제외--photo-reel-diagnosis-not-implemented),[인수인계](../HANDOVER.md),[제품계약](PRODUCT_SPEC.md).

EN: Original manual-gesture evidence is not automation success. The phone-tested1EF candidate has identical runtime payload to the final APK,differing only in embedded revision metadata. Whole0/3/10s,each0/3/10s,last-slide exit and comments protection have physical evidence. Additional checks passed7/7 horizontal and1/1 vertical moves on an8-photo post,and1/1 horizontal plus1/1 vertical with each10s. Unreadable-index photos,photo-to-ad and mixed posts were not found for physical testing;20 rapid samples are not20 consecutive automatic passes. Publication is complete;see the release record for final artifacts and CI. Build/hash-specific evidence is in the verification record. Raw screenshots,trees and device identifiers remain private.
