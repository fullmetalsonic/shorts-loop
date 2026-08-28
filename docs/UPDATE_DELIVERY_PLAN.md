# 통합 업데이트 계획 · 2026-08-28

## 공개 완료 / Published delivery

2026-08-28 10:57 KST에 **0.2.5/code21을 기존 Public 저장소의 시험판으로 공개**했다. [v0.2.5 Release](https://github.com/fullmetalsonic/shorts-loop/releases/tag/v0.2.5), main·태그 기준 코드 커밋 `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5`, draft=false/prerelease=true. [CI33134278633](https://github.com/fullmetalsonic/shorts-loop/actions/runs/33134278633) 성공: 내려받은 보고서356시험·실패0·오류0·건너뜀0, CI lint0오류/2경고(로컬 기존3경고와 구분). 익명 릴리스HTTP200 및 배포파일3개(APK709703bytes/SHA96bytes/JSON287bytes)의 원본 대비 크기·SHA-256 일치를 확인했다. 이후 문서 정리는 제품 소스·APK를 바꾸지 않는다.

**Version0.2.5/code21 was published as a Public prerelease at10:57 KST on2026-08-28**, from code commit `689ea8f704792e5bc2dcf3a9089a5f5a3481fdf5` and tagv0.2.5. CI33134278633 succeeded with356 tests and zero failures/errors/skips; CI lint reported0 errors/2 warnings, separately from3 local warnings. Anonymous release access returnedHTTP200 and all three uploaded assets matched their originals byte-for-byte and by SHA-256. Subsequent documentation changes do not alter product source or the APK.

공개 후10:58~10:59, 설치된code21 앱의 업데이트 확인을 실제 실행해 새 업데이트 없음 안내·조회 시각 갱신을 확인했다. 실제 Public HTTPS 조회와 현재 버전 안내는PASS이며 새 버전 다운로드/설치 재시험은 아니다. 업데이트 자동조회ON·전체실행OFF를 유지했다.

At10:58–10:59, the installed code21 app successfully checked the public release over HTTPS, displayed no applicable newer update and updated its attempt timestamp. This verifies current-version checking, not a new-version download or installation. Automatic checking remained ON and overall execution OFF.

v2서명·기존 인증서·메타데이터21/0.2.5/min26·10:51 설치본 동일성PASS. 공개 후보169개 독립 민감정보 감사·224개 로컬 링크(누락0)도PASS. 서명 인증서와 파일별 검증 범위는 [검증 원장](VERIFICATION.md)을 따른다. 메일 발송 없음.

EN: APK signature/certificate continuity, metadata and installed parity passed, as did the169-file publication audit and224 local links. See the verification ledger for exact values and scope. No email was sent.

## 현재 체크포인트 / Current checkpoint

**최신0.2.5/code21은 PC 검증·휴대폰 설치·Public 공개 완료**다.709703bytes, SHA256 `6095BC8C22BD49AACA348E7D1C048301A9E229C92288D1620439F047179E16B2`로 고정했다.356시험·빌드·lint0오류/기존3경고·LIVE_TREE_LIFECYCLE 포함 정적가드 PASS.10:39 동일 APK API26/33/34 계측74/74/73,10:40 code21 설치·접근성bound·prefs·설치해시 PASS. 최종 조회 원복 독립리뷰 추가P1/P2 없음.

**Current0.2.5/code21 is PC-verified, phone-installed and publicly released.** Its709703-byte frozen APK matches the SHA-256 above. Final build/356 tests/lint/static guards,74/74/73 exact-APK emulator checks and phone binding/preferences/hash passed. Final review reported no additionalP1/P2 findings.

**code21 공식 YouTube10연속 PASS**:10:40:41→10:47:45.702,424.5초,확인0→10/세대6,수동 입력·앱 전환 없음,라이브0. 전환1~10 화면을 모두 육안 확인해 서로 다른 정상 전체화면 영상·플로팅과 올바른 이동을 확인했다. code20 외부 중단/개별 라이브/목표 이후 추가 관측을 합산하지 않는다. 약10:50 플로팅X 전체OFF 확인,10:51 업데이트 자동조회 선택ON 복원·UI/저장값 확인. code21 라이브 개별 재시험·20연속은 미완료이고 D-021은 미수정이다. 공개·CI·익명 배포파일·해시 일치 확인은 완료했으며 상세 결과는 위 공개 기록을 따른다.

**Code21 passed the official10-transition YouTube run** in424.5 seconds(10:40:41→10:47:45.702),confirmation0→10,generation6,no manual input/app changes,zero live skips. All10 transition screens were visually reviewed. Code20 trials and post-target observations are not added. FloatingX stopped execution around10:50;the automatic-update preference was restored to ON and checked in UI/storage at10:51. Individual code21 live retests,20-transition completion and D-021 remain separate limitations. Publication, CI and anonymous asset/hash checks passed as recorded above.

## 과거 code20·18 후보 / Historical candidates

**0.2.5/code20은 당시 확정된 미게시 후보**다. 고정 APK725487bytes, SHA256 `EF59D4E40E192A89D5B207741B03CCE08FA11AC1079DC61C7776C19A1D3D60EB`. cleanup 포함 최종356제품시험·빌드·lint0오류/기존3경고,10:30 동일 후보 API26/33/34 계측74/74/73 PASS. 휴대폰 설치 준비·설정 보존·설치본 해시도 PASS했다. 이전 cleanup 전 시험을 최종 결과로 대신한 것이 아니다.

**Unpublished0.2.5/code20** was frozen at725487bytes with the SHA-256 above. Its final cleanup-inclusive356-test run, build, lint(0 errors/3 existing warnings),74/74/73 exact-candidate emulator checks and phone installation/preferences/hash passed.

실기기5초 지연은10:30:29 대기→34 요청→35 확인1·일반25초 영상 진입,0초/반복0회는10:34:31 준비→33 요청→34 확인2→3,라이브OFF는10:33:33~39 요청2/확인2 유지로 각각PASS했다. 메인 화면에 권한 경고가 없음을 육안 확인했다. 새10연속은10:35:28 확인기준3/세대30에서 시작해2회 확인 후10:37:17 외부 앱 전환으로 중단됐으며, code20의 Public·CI·익명 재다운로드는 당시 미수행으로 보존한다.

Device5-second delay, immediate skipping at zero normal plays, and live-OFF waiting each passed; the main UI showed no permission warning. The10-transition run began separately at10:35:28 from confirmation baseline3/generation30 and ended externally at10:37:17 after two confirmations. Code20 was not published; those publication checks were unperformed at that checkpoint.

code19의 라이브ON 조건부 조회는 중간안이다. code20은 라이브OFF에서도 일반→라이브 전환을 확인하고 pending 중 조회형태를 유지하도록 옵션과 조회 조건을 분리했다. 전체실행ON·YouTube선택·전면YouTube에서 확장하고 다른 앱/전체OFF는 기존 조회,모드 변경 후 이전 root 폐기,failClosed/onDestroy 기본 조회 원복이다. 새 권한은 없다.

Code19 remains an intermediate live-ON-only proposal. Code20 separates retrieval from skipping, preserving recognition and tree shape with live OFF, discarding old roots on mode changes and restoring defaults on stop/destruction. No new permission.

code18은738945bytes/SHA256 `941532517058CB8553EFE5DB34ED1762426C468B2D66F88A567CE788E306C54D`의 보존 실패 후보다.352시험·설치는 통과했지만 조회 플래그 차이로 라이브 인식FAIL했다. code16/17의 업데이터·UI 결과와 code18 실패를 code20에 혼합하지 않는다. D-033의 확인된 원인은 실기기 재시험PASS지만 D-021 일반 반복 경계와 연속 안정성은 별도다. [검증 기록](VERIFICATION.md).

Code18's retained artifact passed automated/install checks but failed actual recognition. It is not the code20 release candidate. Code16/17/18 histories are preserved; the corrected D-033 cause does not resolve D-021 or prove endurance.

## 제품 범위

OS별 기능 호환성, GitHub 새 버전 인앱 안내·설치 버튼, 중요도·빈도에 따른 메뉴 순서를 함께 개선한다. 기기 시험과 문서·보안 검사를 거쳐 기존 Public 저장소에 동일 산출물을 게시한다. INTERNET는 업데이트 조회·다운로드에만, REQUEST_INSTALL_PACKAGES는 Android 설치 확인창을 여는 데 사용한다. OS 설치 허용과 최종 설치 확인은 사용자가 직접 조작하며 ADB로 대신 허용하지 않는다. 메일 발송은 포함하지 않는다.

YouTube 쇼츠의 라이브 미리보기는 별도 기본OFF 옵션이며 대기0~60초/기본0초(바로)를 제공한다. 일반 반복0회와 독립, 전체 실행OFF는 광고·라이브까지 모두 정지한다. 전용 구조·전면창·안정 페이지와 요청 후 전환 확인을 요구하며 방송 참여 버튼·제목/CTA/시청자 수·OCR을 사용하지 않는다. [라이브 계약](LIVE_SKIP.md).

Live previews inside YouTube Shorts are separately opt-in(defaultOFF), with a0–60-second delay(default0, immediate after safety checks). The option is independent of zero normal plays; main OFF stops ads and live too. Dedicated structure, foreground-window/page checks and confirmed transitions are required; no join-button click, text-based identity or OCR.

## 화면

- 일상 순서: 반복 횟수 → 시간제 → 광고 → YouTube 라이브 → 플로팅 → 적용 앱 → 사용 준비 → 업데이트·앱 정보 → 실험 기능 → 도움말.
- 전체 실행은 기존 하단 고정으로 언제든 끄고 켤 수 있다.
- 필수권한이 부족할 때만 상단 준비 바로가기. 새 버전이 있을 때만 상단 작은 배지/버튼. 업데이트가 없으면 주요 설정을 밀어내지 않는다.
- 앱 진입 시 최대 하루 한 번 조용히 조회, 수동 확인·다운로드·취소·진행률·실패·재시도 제공. 백그라운드 서비스나 알림 권한은 추가하지 않는다.

## 네트워크·업데이트 계약

고정 Public 저장소 fullmetalsonic/shorts-loop의 릴리스 목록만 무인증 조회한다. 현재 공개판이 시험판이므로 draft는 제외하되 prerelease를 포함한다. 릴리스의 shorts-loop-update.json을 사용하며 schema/packageName/versionCode/versionName/minSdk/apkName/apkSize/sha256를 실제 APK와 비교한다. 버전코드가 높고 기기 OS에 맞는 것만 제공한다. Github 태그만 있는 커밋이나 임의 APK를 업데이트로 취급하지 않는다.

HTTPS·정확한호스트/저장소 경로 허용목록·매리다이렉트 검사·응답/파일 크기제한·연결/읽기/전체시간제한을 적용한다. 토큰·계정·시청내용 업로드 없음. GitHub는 접속 IP 등 일반 네트워크 요청 정보를 볼 수 있다. APK는 내부 임시폴더만 사용하고 취소/실패 시 부분파일을 정리한다.

다운로드 후 크기/SHA256/패키지/버전/최소OS/현재설치앱과동일서명집합 확인. OS 설치기로 보내기 직전 재확인한다. 해시별 읽기 전용 설치 사본을 만들어 후속 다운로드가 설치창의 파일을 바꾸지 못하게 한다. 손상 사본은 검증된 임시 사본으로 교체한다. SHA/인증서대조를 APK 전체 암호학적서명검증으로 과장하지 않으며 Android 설치검증을 우회하지 않는다. 설치직전 전체실행/플로팅은멈추고 다른설정은보존, 취소/복귀에도자동재시작하지 않는다.

## 시험·게시 순서

1. 제품 정책/파서/네트워크경계/손상·다운그레이드 차단 시험, 빌드/lint/구형OS 재시험. 서명 불일치 거부는 소스 리뷰와 실제 다른 키 APK 시험을 구분해 기록한다.
2. 기존 폰의 버전·서명·설정을 기록하고 낮은 code의 업데이트 시험 시작 APK를 설치한다. 계측시험의 전송 대체는 시험 APK에만 존재하며 제품에는 외부 시험 진입점이나 검증 우회를 두지 않는다.
3. 실제 GitHub HTTPS/리다이렉트는 기존 공개 자산으로 확인한다. 대상 앱을 중단시키는 계측은 최종 설치 전에 끝내거나 에뮬레이터에서 수행한다. 아직 없는 새 공개 릴리스를 게시 전에 다운로드했다고 기록하지 않는다.
4. Android 설치 허용·확인과 실제 버전 상승·설정 보존을 확인한다. 핵심 업데이터의 code15→16 OS설치, 후속code17 UI 체크포인트, code18 ADB설치/실제 라이브FAIL, 새code20 검증은 구분한다. ADB설치 PASS를 새 인앱 설치 경로 PASS로 바꾸지 않는다. 권한 복귀·취소·재시도 중 실행하지 못한 항목은 미실행으로 남긴다.
5. 최종 설치 후에는 비파괴 확인만 수행한다. 설치 APK 해시·버전·설정 및 접근성 enabled/bound/runtime 상태를 대조하고 실제 화면을 확인한다. 이 단계에서 대상 앱을 종료시키는 계측을 다시 실행하지 않는다.
6. 실제 제품 조회 조건으로 라이브 인식·0초/5초 이동·연속 전환을 시험한다. 일반→라이브/라이브→일반/연속 라이브, OFF·앱 전환·잠금·중복 제스처 차단을 구분한다. code16의2/10 실패를 새 성공에 합산하지 않으며 D-021 일반 반복 경계는 별도로 기록한다.
7. 독립리뷰·한영문서·민감정보검사 후 동일 최종 APK와 메타데이터/SHA를 Public 게시한다. CI·익명 재다운로드·폰 설치본 해시·버전 일치를 확인한다. 실제 미완료 항목을 성공으로 바꾸지 않는다.

계측 후 접근성 enabled가 남아 있어도 서비스가 실제로 bound 상태인 것은 아니다. D-031처럼 계측에 의한 강제 종료가 확인되면 원인을 구분한 뒤 수동 OFF→ON 재연결과 상태 복구를 확인한다. 이유 없이 권한을 반복 설정하도록 안내하지 않으며, 계측 후유증을 제품 코드 크래시로 단정하지 않는다.

검증 한계: 동일 서명 APK의 정상 설치와 서명 비교 소스 리뷰는 수행했다. **다른 키로 서명한 실물 APK의 설치 전 거부 시험은 미실행**이며 자동시험 PASS로 포함하지 않는다. code20의 개별 라이브 시험과 공개code21의10연속·배포 검증은 각각 통과했으며 남은 개별 라이브 재시험·20연속 상태는 [검증 기록](VERIFICATION.md)을 따른다. code18 합성 계측은 실제 YouTube 노드 노출을 검증하지 못했으므로 기기에서 같은 조회 조건을 확인하는 단계를 생략하지 않는다.

## 근거

- [GitHub Releases API](https://docs.github.com/en/rest/releases/releases): 공개 무인증조회·릴리스목록/시험판·자산 URL.
- [Android 설치 요청 허용](https://developer.android.com/reference/android/content/pm/PackageManager#canRequestPackageInstalls()): API26부터 앱별 설치허용을 확인.
- [Android SigningInfo](https://developer.android.com/reference/android/content/pm/SigningInfo): API28 이상서명조회,구형은GET_SIGNATURES호환분기.

진행·증거는 [검증 기록](VERIFICATION.md), 오류는 [디버그 대장](DEBUG_LOG.md), 결정은 [인수인계](../HANDOVER.md)에 누적한다. 이 문서는 완료 선언이 아니다.
