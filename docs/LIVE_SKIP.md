# YouTube 라이브 미리보기 넘김 / Live preview skipping

## 설정 / Settings

0.2.5의 YouTube 라이브 옵션은 쇼츠 피드에 섞여 나오는 라이브 미리보기를 대상으로 한다. 라이브 본 방송을 여는 버튼을 누르거나 방송 내부를 조작하지 않는다.

| 항목 / Field | 의미 / Meaning |
|---|---|
| 라이브 자동 넘김 / Skip live previews | 기본OFF. 켜면 선택한 YouTube에서 라이브 미리보기를 넘긴다. / Off by default; applies to the selected YouTube app. |
| 지연 시간 / Delay | 기본0초,0~60초. 숫자 입력·−/+로 설정. / Defaults to0 seconds, range0–60, typed input and step buttons. |
| 0초 / 0 seconds | 바로 넘기기. 화면 전환이 끝나고 안전한 대상이 확인되면 별도 시청 지연 없이 요청한다. / Immediate after the page settles and safety checks pass; not a disabled state. |
| 1초 이상 / 1+ seconds | 확인된 라이브 화면에서 지정 시간 후 넘긴다. / Wait for the chosen delay on a recognized live preview. |
| 일반 반복0회 / Ordinary count0 | 일반 반복·Instagram 시간제는 중지. 별도로 켠 광고·라이브 옵션은 동작한다. / Stops ordinary repeats and Instagram timeout, not independently enabled ads/live. |
| 전체 실행OFF / Execution off | 광고·라이브를 포함한 모든 자동 넘김 중지. / Stops all automatic scrolling. |

라이브 토글을 끄면 지연 값을 유지하고 라이브를 자동으로 넘기지 않는다. YouTube가 설치되지 않았거나 사용할 앱에서 선택되지 않으면 이 설정은 비활성 표시되지만 이전 선택은 지우지 않는다. 지원하지 않는 숫자·소수·음수·빈 입력은 저장하지 않는다. 예:5를 입력하고적용하면5초,0을적용하면바로넘김이다.

Turning off the option preserves its delay. Missing or deselected YouTube disables the controls without erasing saved choices. Invalid, decimal, negative or empty drafts do not overwrite the saved value.

## 인식과 보호 / Recognition and safety

- 현재 전면 YouTube 쇼츠 페이지 아래의 전용 `immersive_live_preview_player`를 확인한다. 제목의LIVE문자, 빨간 색상, 진행바 부재만으로 판정하지 않는다.
- 해당 전용 요소는 안내 문구보다 먼저 관측됐다. 다만 스와이프 중 화면 일부에 나타날 때는 넘기지 않는다. 최신 노드, 단일 페이지, 창·페이지 범위, 안정된 기하를 확인한다.
- 0초도 제스처가 끝날 때까지 필요한 안전 확인은 한다.0밀리초 반응이나 모든 기기에서 동일한 지연을 보장하는 의미는 아니다.
- 이미 요청한 스와이프의 실제 페이지 전환을 확인하기 전에는 다음 스와이프를 요청하지 않는다. 전환 확인 실패·잠금·창 변경·조작 충돌은 안전정지한다.
- 일반→라이브 진입 확인과 라이브 자동 넘김 선택을 분리한다. 라이브 옵션OFF여도 라이브 진입 자체를 전환 실패로 오해하지 않도록 한다.
- 실행 중 전면YouTube에서는 접근성의 레이아웃 요소 포함 조회를 사용한다. 라이브OFF에서도 같은 조회 형태를 유지해 영상 식별이 조회 설정만으로 달라지지 않게 한다. Instagram·다른 앱은 기존 조회, 안전정지·할 일 없는0회 대기·종료는 확장 조회를 원복한다. 이는 기존 접근성 권한의 조회 설정이며 새 권한은 아니다.
- 라이브→라이브는 요청 이후 같은pager/창의 인덱스 변화와 다른 안정된 페이지 노드가 함께 있어야 확인한다. 앱이 같은 노드를 다른 방송에 재사용하는 경우 구분하지 못하고 안전정지할 수 있다.
- 페이지 노드는 RAM에만 유지한다. 제목·시청자 수·CTA 변화는 새 페이지 근거로 사용하지 않는다. 화면 분석·마이크·추가 권한·영상 외부 전송을 사용하지 않는다.

The detector uses a dedicated preview element inside the current foreground Shorts page, not titles or pixels. Partial/preloaded pages, menus and ambiguous windows are excluded. Immediate mode still waits for safe page settlement. Each requested swipe needs confirmation before another is allowed. Consecutive live previews need both fresh pager-index evidence and a stable different page node; reused nodes can cause a conservative stop. No OCR, microphone, new permission or viewing-data upload is required.

Foreground YouTube uses layout-inclusive accessibility queries while execution is active, including when live skipping is OFF, to keep page identity consistent. Other apps keep their existing query mode. Blocked, idle-zero and destroyed sessions restore the default mode.

## 검증 상태 / Validation status

code21은 빌드·356제품시험·Android8/13/14 계측74/74/73개와 실제 Android17의 일반 YouTube10연속 전환(424.5초)을 통과했다. 이10개에는 라이브가 없었다. 라이브5초 지연·0회에서0초 넘김·라이브OFF 대기는 code20에서 실제 확인했으며 code21의 개별 재시험으로 표시하지 않는다. code21은 조회 플래그의 안전정지·종료·0회대기 원복을 보강했다. 기존 code16의10연속2개후FAIL과 합산하지 않는다. 최종 결과·제한은 [검증 원장](VERIFICATION.md), 원인과 재발방지는 [디버그 대장](DEBUG_LOG.md), 누적 변경은 [이력](CHANGELOG.md)을 따른다.

Code21 passed the build,356 product tests,74/74/73 Android8/13/14 emulator checks and ten consecutive ordinary YouTube transitions on Android17 in424.5 seconds. No live preview occurred in that run. Actual5-second delay, immediate skipping at zero ordinary plays and live-OFF waiting were verified on code20, not individually repeated on code21. Code21 hardens query-flag cleanup. The earlier failed code16 run is separate;20-transition endurance remains incomplete.
