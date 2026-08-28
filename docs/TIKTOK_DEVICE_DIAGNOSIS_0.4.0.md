# TikTok 0.4.0 실기기 진단 / Device diagnosis

2026-08-29. **진단 완료, 제품 수정·자동 넘김 성공 아님.** 설치 ShortsLoop0.4.0/code33의 APK SHA256은 공개 산출물 `D8E10BC33664E83ED602F82168F2049D576FF1404E5415E83225D2C673987BBA`와 일치했다. TikTok46.7.3/code460703, `com.ss.android.ugc.trill`, Android API37, 단독 추천 피드, 반복1/실행ON/플로팅ON 조건이다. 기존 설정과 권한은 변경하지 않았다.

EN: Diagnosis only, not a fix or successful automatic-advance test. The installed0.4.0/code33 APK matches the published hash. TikTok46.7.3 on API37 was inspected in the single recommendation feed with target1, execution and floating display enabled. No installation or permission/settings change was performed.

## 1. 실제 관측 / Observations

처음 화면과 이후 수동 이동으로 **서로 다른26개 피드**를 화면 및 구조 기록과 대조했다. 왕복 재방문·같은 페이지에 머문 입력·사진 내부 이동은 새 피드 수에 포함하지 않는다. 각 피드의4회 짧은 조회는 영상 끝까지 본 시험이 아니다. 이전6페이지 사전조사와 합산하지 않는다.

| 유형 / Type | 개수 / Count | 결과 / Result |
| --- | ---: | --- |
| 일반 영상, 보이는 진행값 증가 / Ordinary, moving visible range | 9 | 진행정보가 있지만 제품 카운트0 / Progress exists but product count remains0 |
| 일반 영상, 유효 진행값 없음 / Ordinary, no usable visible range | 9 | 숨긴0값 또는 진행노드 없음 / Hidden zero or missing progress |
| 영상 광고 / Video ads | 5 | 이 중2개에도 진행값 증가 / Two also expose moving progress |
| 점 표시만 있는 광고 캐러셀 / Dot-only ad carousel | 1 | 장 번호 미확보, 가로 입력의 외부 페이지 이동 사례 / No numeric index; horizontal navigation opened an external page |
| 단일 이미지 노드로 관측된 사진 광고 / Photo ad observed with one image node | 1 | 총장수 미확정. 첫 세로 이동은 같은 페이지, 다음 중앙 이동은 전환 / Total unknown; first vertical attempt stayed, next central swipe moved |
| 사진 라벨·장 번호가 있는 광고 / Indexed photo ad | 1 | 분리된 장 번호 및 실제4→3→4→5장 이동 확인 / Split numeric index and manual4→3→4→5 confirmed |

진행값은0–10000의 비율이며 초단위 길이가 아니다. 일반 영상의 증가 예는28→232→436→572,17→195→374→552,23→239→456→673이다. 광고에서도0→1003→1603→2504 등이 관측되어 **진행값이 있다는 이유만으로 일반 영상으로 분류하면 안 된다.** 캡션의 광고 관련 단어와 앱의 전용 광고 라벨도 구분해야 한다.

설치 제품의 접근성 연결/실행은 유지됐지만, 확인한 TikTok 상태는 `tiktok.unsupported` 또는 `screen.interaction`, `normalized=-1`, 카운트0, 자동 요청/확정0이었다. 수동 이동은 제품 성공에 합산하지 않는다. 자연 반복 카운트·10연속 자동 넘김·다중 창은 미검증이다. 광고가 아닌 일반 사진 게시물과 실제LIVE는 이번 표본에서 확보하지 못했다.

EN: Twenty-six distinct feed pages were inspected, excluding revisits, failed moves and internal slides. Nine ordinary pages had advancing visible normalized progress and nine did not. Eight ads comprised five videos, one dot-only carousel and two photo-labelled types. Ads can also expose moving progress. Four brief reads are not full playback tests. Product requests/confirmations stayed0. Ordinary non-ad photos, actualLIVE, natural loops, ten consecutive automatic transitions and multi-window behavior remain unverified.

## 2. 일반 영상 카운트0의 직접 원인 / Ordinary detection failure

현재 표본의 보이는 영상 렌더 노드는 `SurfaceView`다. 제품 `TikTokStructurePolicy.inspect`는 현재 페이지의 `player_view` 아래 **TextureView만** 허용한다. 따라서 진행값이 정상 증가하는 영상도 미디어를 찾지 못해 `tiktok.unsupported`로 빠진다. 접근성 연결 실패가 아니며, 이 표본에서 권한을 다시 승인해야 하는 근거는 없다.

기존 사전조사에서는 같은 TikTok 버전에 TextureView가 관측됐다. 렌더 방식이 달라진 이유는 미확정이다. 이 차이가 모든 틱톡 실패의 유일한 원인이라는 뜻도 아니다. 진행값 없는 영상은 렌더 허용만 바꿔도 반복 계산이 되지 않는다.

보강 후보: TextureView 또는 SurfaceView **합산 정확히1개**, 기존 현재 페이지/조상 관계/창/경계/사진·광고·정지 제외 조건을 유지한다. 렌더 교체 시 카운트를 다른 미디어에 이어 붙이지 않는다. 시작·반복 경계와 다음 페이지 확인은 수정 후 실기기 검증이 필요하다. 아직 제품에 반영하지 않았다.

EN: Current ordinary samples use SurfaceView while the product accepts only TextureView in the player hierarchy. This explains rejection despite valid progress. The prior survey used TextureView on the same TikTok version; renderer-selection causation is unknown. A narrow two-renderer path must retain uniqueness, hierarchy, window/page and special-content guards. It will not solve truly clockless videos. No product patch was applied.

## 3. 가로 콘텐츠의 두 구조 / Two horizontal structures

### 점만 표시하는 광고 / Dot-only ad

`player_view_pager`와 SurfaceView는 관측됐지만 화면의5개 점에 대응하는 현재/전체 번호나 선택 메타데이터는 확보하지 못했다. 하단 앱 메뉴의1행5열 CollectionInfo는 사진 장수가 아니다. 이 광고의 가로 스와이프 후 외부 광고 웹페이지가 열렸다. 직후 화면 재검사 없이 다음 입력을 실행한 것은 잘못된 시험 방법이며, 이후에는 **입력1회→현재 앱/화면 확인→다음 입력**으로 변경했다. BACK으로 외부 화면과 프로필을 벗어나 추천 피드 복귀를 확인했다. 구매·가입·팔로우·좋아요는 수행하지 않았다.

이 구조에 무조건 가로 넘김을 적용하지 않는다. 광고 정책을 구현한다면 현재 페이지의 정확한 광고 라벨과 안정된 출처를 확인하고, 링크/종료카드 클릭 없이 피드 전체 세로 이동을 우선 검토한다. 의미 기반 세로 스크롤 실행은 이번 조사에서 시험하지 않았다.

### 분리된 숫자가 있는 사진형 광고 / Split-index photo ad

다른 사진형에서는 `tv_label`의 사진 라벨, `player_view`→`abx`→`qzz`→`r0h`→현재 `r04`→`qzz`→`wel` 이미지 구조가 관측됐다. 같은 `qzz` 아래 `r06`의 직속 TextView3개 중 첫 숫자는 현재 장, 마지막 숫자는 전체 장으로 화면과 일치했다. 가운데 구분자는 화면에서 확인했지만 프로브에 원문을 출력하지 않았다.

- 실제 수동4→3→4→5 이동에서 전체 장수5와 같은 게시물·player·내부 컨테이너 식별이 유지됐다.
- 현재 이미지 식별은4장A→3장B→4장A→5장C였다. 단순 새 순번을 이미지키로 발급하면 왕복 이동을 잘못 판단한다.
- 초기4/5는4개 표본 모두 읽혔지만, 수동 이동 후에는 첫 표본에서만 번호를 읽었다. 이후 번호가 숨겨지거나 트리에서 사라진 경로를 더 구분해야 한다.
- 이동 직후에는 이전 사진 가장자리가 남아2개 이미지가 보였다. 숫자 변화만으로 즉시 완료 처리하지 말고 안정된 화면 배치를 확인해야 한다.
- 마지막5/5 뒤 추가 가로 이동은 하지 않았고, 중앙 세로 스와이프로 다른 일반 피드에 이동했다.

보강 후보는 **정확한 현재/전체 번호 + 같은 게시물 + 독립 현재 이미지 식별 + 안정된 화면 배치**를 결합한다. 마지막으로 검증한 번호를 잠깐 보존하려면 현재 이미지/게시물/창이 그대로라는 새 근거와 만료 조건이 필요하다. 누락·노드 재사용·뒤로 이동·총장수 변경·메뉴·창 변경·늦은 입력 결과는 차단한다. 새로운 관측 시작은 대기 요청이 없는 경우만 허용하며, 미확정 사진 이동 요청은 자동 재시도/일반 복구로 우회하지 않는다. 번호를 못 읽는 알려진 사진형은 선택형 전체 게시물 타이머 후보이며 모든 미인식 화면에 타이머를 적용하지 않는다. 사진 광고1표본을 일반 사진 전체 지원으로 표현하지 않는다.

EN: The dot-only ad lacked indices and a horizontal swipe opened an external page; recovery to the feed was verified. A different photo ad exposes split current/total numbers under r06. Manual4→3→4→5 preserved total5/post/player and image keys followedA→B→A→C. The initial index persisted across four reads, but post-swipe indices were only seen in the first read, sometimes with two images during animation. Future confirmation needs exact index progression, same-post and independent-image proof, stable geometry and safe expiry. Unconfirmed photo requests remain blocked, never automatically retried or bypassed through ordinary recovery. The final slide was left vertically. These are manual observations, not product automation or universal photo support.

## 4. 다음 구현·시험 / Proposed implementation and tests

1. 일반 영상 렌더 인식을 보강하고 실제 시작→종료→다음 페이지를 시험한다. / First fix renderer recognition and verify complete playback/destination.
2. 진행정보 없는 일반 영상은 특수 콘텐츠·정지·메뉴를 제외한 별도 선택형 시간제로 검토한다. 시간은 미정이며 영상 길이를 추정하지 않는다. / A clockless timeout is a separate opt-in proposal, not fabricated duration.
3. 광고는 전용 라벨/현재 페이지 근거, 지연·취소, 세로 전환 확인을 따로 검증한다. / Ads need separate identity-bound timing, cancellation and vertical confirmation.
4. 사진은 처음·중간·마지막·왕복·번호 사라짐·노드 재사용·광고 우선순위를 시험하고 일반 비광고 사진 표본을 추가한다. / Test slide edges, reversals, disappearing indices, reuse and ad priority; obtain ordinary photo examples.

이 문서는 계획이지 신규 기능 승인·게시 기록이 아니다. 제품 빌드/설치/기능 회귀시험은 이번 진단에서는N/A, 수정 후 실기기 성공은NOT RUN이다. YouTube/Instagram에는 이번 조사에서 입력하지 않았다.

## 5. 진단 도구·검토 / Diagnostic tooling and review

개발 전용 `TikTokReadOnlyProbe`만 보강했다. 비억제 연결·4표본·700노드/48깊이·25초 상한을 유지하며, 정확한 역할 라벨/숫자 패턴/최대3자리 숫자/상태 메타데이터/노드 구조만 출력한다. 자유로운 캡션·계정·상태 원문을 출력하지 않는다. 입력 기능은 없으며 수동 탐색과 분리했다. API33+ 조사용이고 제품 APK에는 들어가지 않는다.

최종 조사 JAR7146bytes/SHA256 `08659DD879BFAE072DAD1EA08833183F1262B857947781DC98C7F509FE8BA788`: Java컴파일·DEX·실기기 읽기 실행 완료. 구형 source/target 옵션 및 deprecated API 알림은 남아 있다. 숫자 조회 결과 자체는 범용 사진 분류기가 아니다. 독립 읽기 검토에서 분리 숫자·이미지 왕복·애니메이션·번호 소실의 제약을 확인했다. 개인 화면·기기 식별자·원시 로그는 비공개 자료로만 유지한다. GitHub 커밋/푸시/릴리스·메일 발송 없음.

추가 PC 프로브 패턴검사22개 PASS: 유효 번호,범위 역전/0·과대숫자 거부,캡션/URL·비숫자 제외,정확한 역할 라벨을 검사했다. 제품 반복 카운터 시험과 합산하지 않는다. 독립 문서 검토의 지적2개(총장수 미확정/미확정 사진요청 복구 금지)를 반영했다. 새 진단 변경 범위의 미해결P1/P2는 없지만, 제품 렌더 인식 실패는D-047 미수정으로 남는다.

EN: Only the shell-only probe was extended, compiled/dexed/executed;22 bounded-pattern checks pass separately from product tests. Non-suppression, bounded traversal, whitelisted output and no input capability are preserved. Independent evidence review confirmed split indices, image reversals and transition/metadata limitations. Its wording corrections were incorporated. Private captures remain excluded. No product change, publication or email occurred;D-047 remains unfixed.
