param([Parameter(Mandatory=$true)][string]$Apk)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskSdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
$taskSource = (Resolve-Path -LiteralPath $Apk).Path
$taskBadging = (& (Join-Path $taskSdk 'build-tools/35.0.0/aapt2.exe') dump badging $taskSource | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'APK metadata inspection failed.' }
$taskMatch = [regex]::Match($taskBadging, "package: name='com.fullmetalsonic.shortsloop' versionCode='(\d+)' versionName='(\d+\.\d+\.\d+)'")
if (-not $taskMatch.Success) { throw 'Only the product package and a plain release version are accepted; bootstrap APKs are not releasable.' }
$taskCode = [int]$taskMatch.Groups[1].Value
$taskVersion = $taskMatch.Groups[2].Value
$taskMinMatch = [regex]::Match($taskBadging, "minSdkVersion:'(\d+)'")
if (-not $taskMinMatch.Success) { throw 'APK minSdk is missing.' }
$taskMin = [int]$taskMinMatch.Groups[1].Value
$taskSigner = (& (Join-Path $taskSdk 'build-tools/35.0.0/apksigner.bat') verify --print-certs $taskSource | Out-String)
if ($LASTEXITCODE -ne 0 -or $taskSigner -notmatch '3604d3a1cc1f4e8f772d718cf8b9cba5adfd3650708cf169660b653e28b69632') {
    throw 'APK signature does not match the existing public release key. Do not publish a CI-rebuilt APK as an update.'
}
$taskOutput = Join-Path $taskRoot "artifacts/release-v$taskVersion-code$taskCode"
if (Test-Path -LiteralPath $taskOutput) { throw 'Release output already exists; preserve it and verify instead of overwriting.' }
New-Item -ItemType Directory -Path $taskOutput | Out-Null
$taskName = "shorts-loop-v$taskVersion-debug.apk"
$taskCopy = Join-Path $taskOutput $taskName
Copy-Item -LiteralPath $taskSource -Destination $taskCopy
$taskHash = (Get-FileHash -LiteralPath $taskCopy -Algorithm SHA256).Hash.ToLowerInvariant()
$taskSize = (Get-Item -LiteralPath $taskCopy).Length
if ($taskSize -le 0 -or $taskSize -gt 41943040) { throw 'APK exceeds the updater size contract.' }
$taskManifest = [ordered]@{
    schema=1; packageName='com.fullmetalsonic.shortsloop'; versionCode=$taskCode; versionName=$taskVersion
    minSdk=$taskMin; apkName=$taskName; apkSize=$taskSize; sha256=$taskHash
}
$taskManifest | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $taskOutput 'shorts-loop-update.json') -Encoding utf8NoBOM
"$taskHash  $taskName" | Set-Content -LiteralPath (Join-Path $taskOutput "$taskName.sha256") -Encoding utf8NoBOM
Get-ChildItem -LiteralPath $taskOutput | Select-Object Name, Length
"SHA256=$taskHash"
"OUTPUT=$taskOutput"
