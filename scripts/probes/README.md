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
