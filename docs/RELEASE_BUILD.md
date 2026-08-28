# 배포 APK 빌드·검사 / Distribution build and checks

0.2.8/code30부터 사용자 배포는 `release` 변형을 사용한다. 앱 하단은 `ShortsLoop <버전>`, 업데이트 메뉴는 `설치 버전 <버전>`만 표시한다. 화면 분석 보조의 실제 실험 안내는 유지한다. [발견 원인](RELEASE_PRESENTATION_AUDIT.md), [검증 결과](VERIFICATION.md).

From0.2.8/code30, distribution uses the release variant. App/version labels have no release-stage suffix. Genuine experimental feature warnings remain.

## 서명과 빌드 / Signing and build

기존 설치본 업데이트에는 같은 패키지와 기존 서명이 필요하다. 이 변경은 기존 인증서 신원을 유지하며 개인키 교체·앱 삭제·새 권한을 요구하지 않는다. 과거 개발용 인증서를 유지하는 것과 APK의 디버깅 허용 여부는 별개다. `release`는 `debuggable false`, `minifyEnabled false`이며 이번에 축소/난독화 동작을 추가하지 않는다.

Updates preserve the existing package and signing identity. Retaining the historical development certificate does not require a debuggable APK. This change disables debugging without adding shrinking/obfuscation, key rotation, uninstall or permissions.

배포 담당자는 개인키와 비밀번호를 저장소 밖에 보관하고 다음 환경변수를 로컬 프로세스에 제공한다. 실제 값·키 파일을 커밋하거나 CI 로그에 출력하지 않는다.

The distributor supplies these environment variables locally; never commit keys/passwords or print their values in CI:

- `SHORTSLOOP_KEYSTORE`: 기존 개인 keystore의 절대 경로 / existing private keystore path
- `SHORTSLOOP_STORE_PASSWORD`: 저장소 비밀번호 / store password
- `SHORTSLOOP_KEY_ALIAS`: 기존 서명 별칭 / existing signing alias
- `SHORTSLOOP_KEY_PASSWORD`: 키 비밀번호 / key password

```powershell
./gradlew.bat --no-daemon :app:assembleRelease :app:compileReleaseUnitTestJavaWithJavac :app:lintRelease :app:assembleDebug :app:assembleDebugAndroidTest
./scripts/verify.ps1 -SkipBuild -BuildType release
./scripts/verify-release-safety.ps1 -Apk app/build/outputs/apk/release/app-release.apk -DebugApk app/build/outputs/apk/debug/app-debug.apk
./scripts/prepare-release.ps1 -Apk app/build/outputs/apk/release/app-release.apk -OutputSuffix final
```

예상 결과: 제품 JUnit·정적 검사 PASS, `DEBUG_APK_PUBLICATION_REJECTION=PASS`, `RELEASE_LABEL_AND_DEBUGGABLE_AUDIT=PASS`. 제품 소스를 먼저 커밋한 뒤 배포 빌드를 다시 생성한다. 준비 스크립트는 기존 단일 서명·패키지·버전·크기, 제품 소스 미커밋 변경 부재, APK 내 소스 revision과 HEAD 일치를 검사하고 새 폴더에 APK/SHA256/업데이트JSON을 생성한다. 디버깅 허용 APK는 폴더를 만들기 전에 거절한다. 기존 폴더를 보존하면서 새 검증본을 만들 때는 `-OutputSuffix final`을 사용할 수 있다.

Expected: product tests/static guards and both safety guards pass. Commit product sources and rebuild before freezing. Preparation checks the exact single signer, package/version/size, clean product sources and embedded revision matching HEAD. Debuggable APKs are rejected before output creation. Use `-OutputSuffix final` for a separate final directory without overwriting candidates.

키 환경변수가 없는 CI는 서명되지 않은 `app-release-unsigned.apk`를 빌드한다. CI는 debug/release 단위시험·lint와 배포 안전검사를 실행하지만 공개 APK를 재서명/대체하지 않는다. 개인 빌드를 재사용하려면 자체 서명이 필요하며 원본 앱 위로 설치되지 않을 수 있다.

CI without signing environment variables builds an unsigned release and tests both variants. It does not replace the locally signed public artifact. Forks need their own signing identity and cannot assume compatibility with the original installed app.

## 덮어 설치·표시 검사 / Upgrade and UI checks

아래 명령의 `emulator-NNNN`은 일회용 에뮬레이터 ID로 바꾼다. 실제 휴대폰에는 계측시험을 실행하지 않는다. 이전0.2.7 APK는 공개 릴리스에서 내려받아 SHA256을 확인한다.

Replace `emulator-NNNN` with a disposable emulator ID. Do not run instrumentation on a personal phone. Obtain the previous0.2.7 APK from its release and verify its checksum.

```powershell
./scripts/verify-release-upgrade.ps1 -Device emulator-NNNN -PreviousApk <previous-0.2.7.apk> -ReleaseApk artifacts/release-v0.2.8-code30-final/shorts-loop-v0.2.8.apk
./scripts/verify-compat-emulator.ps1 -Device emulator-NNNN -Apk artifacts/release-v0.2.8-code30-final/shorts-loop-v0.2.8.apk -ExpectRelease
```

업데이트 시험은 이전 APK에 다양한 설정을 저장하고, 제거 없이 새 APK를 설치한 뒤 설정 전체의 타입·값, UID·서명·버전, 실행OFF와 디버깅OFF를 확인한다. 표시 시험은 실제 설치 버전과 두 라벨의 정확한 일치 및 실험 경고 유지를 확인한다. 자동 넘김 실기기 연속시험과는 구분한다.

The upgrade regression checks all typed playback preferences, package UID/signature/version, executionOFF and debuggingOFF across an in-place update. UI checks compare both labels to the installed package version and preserve experimental disclosure. These are not social-app endurance tests.

휴대폰에서는 전체 실행을 중지하고 `adb install -r`로 업데이트한 후 설치 APK 해시·접근성 바인딩·런타임 설정·실제 화면을 확인한다. release 앱은 `run-as`로 비공개 설정을 읽을 수 없으므로 이를 허용하려고 디버깅이나 QA 우회 기능을 켜지 않는다. `verify-device-ready.ps1 -ExpectedRuntimeSettings <baseline.json>`은 명시한 런타임 항목만 비교하며 전체 설정 파일 비교로 보고하지 않는다. 플로팅 위치·앱 선택 등은 화면 검사를 병행한다.

On phones, stop execution and update with `adb install -r`, then verify installed hash, accessibility binding, exposed runtime settings and actual UI. Release apps intentionally deny `run-as` private-data access. Runtime baseline checks are explicitly partial, not full private-preference comparison; inspect app selection and overlay position visually too.

## 게시 전후 / Before and after publication

소스 커밋·버전·태그·CI 결과·공개 상태·Release 첨부3개와 내려받은 크기/SHA256을 대조한다. UI·README·업데이트JSON·릴리스 기록은 같은 현재 버전을 설명해야 한다. 지난 버전의 시험·시험판 이력은 과거라는 표기와 함께 보존한다. [릴리스 기록](releases/v0.2.8.md).

Compare source/version/tag, CI status, visibility, all three release assets and anonymously downloaded size/SHA256. Current UI/docs/update metadata must align; preserve historical evidence under explicit historical headings.
