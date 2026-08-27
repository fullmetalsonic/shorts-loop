# 0.2.0 앱 적용 아이콘

- 내장 image_gen의 이미지 편집 기능 사용. 시안의 재생 삼각형/반복 화살표를 투명 배경으로 분리했다. 별도 API/CLI 호출 없음.
- 실제 앱 자산: [launcher_mark.png](../../app/src/main/res/drawable-nodpi/launcher_mark.png).
- 1254×1254 PNG, 좌상단 alpha=0 확인. 생성 후 육안으로 색상/형상/투명 배경 확인.
- Android drawable의9% inset과 남색 gradient 배경을 조합하여 adaptive icon으로 적용. API33에는 단색 vector 레이어 별도 제공.
- 시안 원본은 보존. 픽셀 파일을 스크립트로 재편집하지 않고 Android 리소스 배치로 여백을 조정했다.
- 런처 실기기 육안 확인은 설치 후 별도로 기록한다.

## 최종 편집 프롬프트

Use case: background-extraction. Input image 1 is the edit target: the approved ShortsLoop icon. Create the Android adaptive-icon FOREGROUND asset by isolating only the central glossy cyan/blue play triangle and surrounding blue-to-lavender upward looping arrow. Preserve exactly their original shape, relative proportions, lighting, color and smooth premium 3D glass material. Remove the entire dark rounded-square tile, border bevel, outer white corners and all cast shadows on the tile. Replace all background and holes between the symbol parts with genuine transparent alpha, NOT a checkered or white rendered background. Center the complete mark (loop and triangle together) within the central 60 percent of the square canvas width and height; leave generous transparent padding on all four sides for Android adaptive icon masking. No text, no extra marks, no new outline, no other subjects. One square transparent PNG foreground asset.
