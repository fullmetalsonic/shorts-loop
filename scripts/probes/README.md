# Instagram 읽기 전용 조사 프로브

제품에 포함하지 않는 개발용 도구다. 앱/권한을 설치하거나 변경하지 않는다. UIAutomator는 기존 접근성 서비스 연결에 간섭하므로 **ShortsLoop 실행을 OFF한 뒤에만** 사용한다. 제품 E2E 중에는 사용하지 않는다.

## 확인 순서

1. 앱의 저장된 enabled=false를 확인한다. 사용자가 승인한 Instagram 릴스를 연다.
2. SDK35의 android.jar, uiautomator.jar, optional/android.test.base.jar로 InstagramProgressProbe.java를 Java8 대상으로 컴파일한다.
3. 클래스 JAR을 SDK d8로 dex 변환한 뒤 classes.dex를 담은 JAR을 만든다. 생성물은 private/instagram-probe에 보관한다.
4. JAR을 /data/local/tmp/shortsloop-instagram-progress-cached.jar로 adb push한다.
5. 단말기의 기존 framework JAR을 포함해 다음 셸 명령을 adb shell로 실행한다. 예시65초는 총 실행 목표이며 첫 노드 탐색에 약10초가 걸릴 수 있다.

```sh
CLASSPATH=/system/framework/android.test.runner.jar:/system/framework/android.test.base.jar:/system/framework/uiautomator.jar:/data/local/tmp/shortsloop-instagram-progress-cached.jar app_process /system/bin com.android.commands.uiautomator.Launcher runtest -c probes.InstagramProgressProbe -e jars /data/local/tmp/shortsloop-instagram-progress-cached.jar -e seconds 65
```

6. 출력 중 IG_PROGRESS 행의 fresh=true, visible=true, range 존재 조건을 만족하는 표본만 분석한다. min/max/current/type와 경과시간만 출력하며 제목·계정·영상 식별값·설명은 출력하지 않는다.
7. 종료 후 Instagram 재생 상태와 ShortsLoop OFF를 확인한다. 앱 E2E는 별도로 진행한다.

## 해석과 한계

- 예: max19014/current18708 → max19014/current95, 관측402ms: 끝에서 처음으로 돌아가는 진행값의 근거다. 단순 값 감소만으로 자동 넘김을 실행하면 수동 탐색과 혼동할 수 있다.
- 프로브에는 릴스 컨테이너·입력초점·영상 식별·광고/댓글 차단·자동 제스처가 없다. 기기 조사 용도이며 제품 감지기로 그대로 사용하지 않는다.
- fresh=false 행에는 캐시 값이 남을 수 있으므로 제외한다. 갱신 실패 시 다음 표본에서 노드를 재조회한다.
- assertion 없는 OK(1 test)는 실행 종료일 뿐 기능 정확성 PASS가 아니다. 초기 runner 오류 때에도 OK 문구가 나왔으므로 예외와 정상 표본을 함께 확인해야 한다.
- 조사 결과와 독립 검토: [업데이트 설계](../../docs/UPDATE_PLAN_2026-08-27.md), [검증](../../docs/VERIFICATION.md), [D-010](../../docs/DEBUG_LOG.md).

## 재생바 외 시간원 조사 / Alternative timing survey

[InstagramTimingProbe.java](InstagramTimingProbe.java)는 scrubber ID만 조회하는 위 도구와 다르다. Instagram 트리의 RangeInfo와 선택된 시간 형식 후보를 확인한다. 제품 실행OFF를 먼저 확인하고, 동일 SDK의 android.jar/uiautomator.jar/optional android.test.base.jar를 javac classpath 및 d8 lib로 지정한다. Java8 클래스 JAR→dex→classes.dex 포함 JAR 순서로 빌드한다. 최종 조사 JAR은 private에 보관한다.

```sh
adb push instagram-timing-v2.jar /data/local/tmp/shortsloop-instagram-timing-v2.jar
adb shell sha256sum /data/local/tmp/shortsloop-instagram-timing-v2.jar
adb shell 'CLASSPATH=/system/framework/android.test.runner.jar:/system/framework/android.test.base.jar:/system/framework/uiautomator.jar:/data/local/tmp/shortsloop-instagram-timing-v2.jar app_process /system/bin com.android.commands.uiautomator.Launcher runtest -c probes.InstagramTimingProbe -e jars /data/local/tmp/shortsloop-instagram-timing-v2.jar -e samples 4'
```

- 기기가 여러 대면 adb에 선택한 `-s`를 명시한다. 기기/로컬 해시를 비교한다. 새 APK나 권한을 설치하는 절차가 아니다.
- 첫 표본만 전체 노드 구조를 출력하고, 이후에는 range/시간 후보/state/분류되지 않은 추가 데이터 노드와 집계를 출력한다. 텍스트·설명·계정·extras 값은 출력하지 않는다.
- 노드별 refresh를 확인한다. stale/truncated가 있으면 완전한 최신 트리라고 해석하지 않는다. refresh 사이의 화면은 변할 수 있으므로 원자적 스냅샷은 아니다.
- timeFields는 시:분:초/분:초/한국어·영어 분·초 패턴의 문자열 내부 후보다. 다른 언어는 놓칠 수 있고 캡션의 시각도 포함할 수 있으므로 실제 재생 시간으로 확정하지 않는다.
- otherExtraData는 알려진 렌더링/문자위치 외 요청 가능 키의 개수이며 값은 조회하지 않는다.0이 아니어도 재생 정보라고 단정하지 않는다.
- 초기 탐색은 약10초 지연될 수 있다. **명령이 종료될 때까지 탭/스와이프하지 않는다.** 출력 절단·실행 중 콘텐츠 전환 자료는 정상/실패 대조에서 제외한다.
- 조사 후 사용자 기존 실행 상태와 설정을 복원한다. 프로브가 제품 접근성 연결을 재생성할 수 있으므로 전후 누계를 합산하지 않는다.
- 실측·자료·선택지: [D-022 후속 조사](../../docs/INSTAGRAM_TIMING_RESEARCH_2026-08-27.md). This is a read-only research tool, not a product detector or an automatic-scrolling test.

## 숫자 이벤트·화면 주기 실험 / Event and visual research

- [InstagramEventProbe.java](InstagramEventProbe.java): IG 전체 이벤트 종류의 안전한ID/숫자 필드만 확인. 기본15초,2~45초,출력1500건 한도. 이벤트가0이면 바로 부재로 확정하지 말고 별도 수동 일시정지 양성 대조로 수집 경로를 확인한다. 텍스트/설명/계정은 출력하지 않는다.
- [InstagramVisualProbe.java](InstagramVisualProbe.java): 활성IG의 단일 영상 컨테이너 사각형을 확인하고, 전체 screenshot을 일시RAM에 받은 뒤 중앙 특징만 RAM에서 비교한다. 기본35초,8~90초,360표본 한도. 창 밖/기하 변화/캡처 실패 시 중단. 화면·특징 파일/네트워크/입력 제스처 없음.
- [VisualCycleMath.java](VisualCycleMath.java) / [VisualCycleMathTest.java](VisualCycleMathTest.java): 약250ms 표본,50ms 주기검색, 정지 평균움직임0.8미만 거부. 합성8초/전체정지/빈벡터3검사. 후보만 반환하며 정확도 확정/자동제스처/영상 시작점/동일콘텐츠·부분정지 보호는 없다. 약2.1주기 이상 관측을 요구하므로 진입 즉시 정확N1/N2를 보장하지 않는다.
- [measure-visual-cycle.ps1](measure-visual-cycle.ps1): PC에서 선택한 표시장치의 지정ROI를 RAM 분석하는 초기 방식. `-Device`/`-DisplayId` 필수. 캡처 지연과 정지 화면의 가짜 후보 한계가 있으므로 제품 detector로 사용하지 않는다. 패키지/창 보호도 기기 Java보다 약하며 화면 변경 중 사용하지 않는다.

### 로컬 컴파일과 합성 시험

SDK35/Build Tools35.0.0·JDK 및 PowerShell 필요. 저장소 루트에서 실행한다. 아래 경로는 새 출력 폴더를 만들며 이미 있으면 `probe-rebuild-01` 번호를 바꾼다. 기존 증거를 삭제/덮어쓰지 않는다. 각 native 명령 후 종료코드가0인지 확인하고 실패 시 다음 단계로 진행하지 않는다.

```powershell
$taskSdk = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
$taskWork = Join-Path (Get-Location) 'private/instagram-probe/probe-rebuild-01'
New-Item -ItemType Directory -Path $taskWork -ErrorAction Stop
New-Item -ItemType Directory -Path "$taskWork/classes", "$taskWork/dex"
$taskApi = "$taskSdk/platforms/android-35"
$taskLibs = @("$taskApi/android.jar", "$taskApi/uiautomator.jar", "$taskApi/optional/android.test.base.jar")
$taskSources = @('InstagramEventProbe.java', 'InstagramTimingProbe.java', 'InstagramVisualProbe.java', 'VisualCycleMath.java', 'VisualCycleMathTest.java') | ForEach-Object { "scripts/probes/$_" }
javac -source 8 -target 8 -encoding UTF-8 -cp ($taskLibs -join ';') -d "$taskWork/classes" @taskSources
if ($LASTEXITCODE -ne 0) { throw 'Probe compilation failed' }
java -cp "$taskWork/classes" probes.VisualCycleMathTest
if ($LASTEXITCODE -ne 0) { throw 'Probe math tests failed' }
jar --create --file "$taskWork/classes.jar" -C "$taskWork/classes" .
if ($LASTEXITCODE -ne 0) { throw 'Class packaging failed' }
& "$taskSdk/build-tools/35.0.0/d8.bat" --min-api 26 --lib $taskLibs[0] --lib $taskLibs[1] --lib $taskLibs[2] --output "$taskWork/dex" "$taskWork/classes.jar"
if ($LASTEXITCODE -ne 0) { throw 'Probe dex conversion failed' }
jar --create --file "$taskWork/research-probes.jar" -C "$taskWork/dex" classes.dex
if ($LASTEXITCODE -ne 0) { throw 'Dex packaging failed' }
Get-FileHash -LiteralPath "$taskWork/research-probes.jar" -Algorithm SHA256
```

기대 출력은 `VISUAL_MATH_TESTS: 3 PASS`다. Android 제품 Unit/E2E와는 별개다. 조사 기기의 일련번호를 명시하여 새 JAR을 `/data/local/tmp/shortsloop-research-probes.jar`에 push하고 기기 `sha256sum`과 비교한다. **제품 실행OFF 확인 후**, 위와 같은 framework classpath에 이 JAR을 마지막으로 넣어 다음 main 중 하나만 실행한다.

```sh
app_process /system/bin probes.InstagramEventProbe 18
app_process /system/bin probes.InstagramVisualProbe 38
```

두 프로브를 동시에 실행하지 않는다. 화면 프로브 동안 탭/스와이프 금지. 이벤트 양성 대조의 수동 일시정지는 별도 구간으로 명시한다. 프로세스 종료 후 일시정지를 해제하고 원래 앱 실행/설정을 복원한다. 제품 연속 자동이동 시험은 UIAutomator와 분리해 `observe-device.ps1`만 사용하며 실제 이동·카운트·요청/확인을 기록한다.

현재 C차 기기 사용본은 각각 이벤트v1/화면v2 JAR이며, 위 통합 재빌드 JAR의 해시와 혼동하지 않는다. 실기기 결과·원본 파일·20개 기준 실패·새 capability/설치 승인 대기는 [조사 기록](../../docs/INSTAGRAM_TIMING_RESEARCH_2026-08-27.md)을 따른다. These are research probes, not an APK, a permission grant, or a verified loop counter.
