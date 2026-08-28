param([Parameter(Mandatory=$true)][string]$Apk, [ValidatePattern('^[a-z0-9-]+$')][string]$OutputSuffix)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskSdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
$taskSource = (Resolve-Path -LiteralPath $Apk).Path
$taskAapt = if ($IsWindows) { 'aapt2.exe' } else { 'aapt2' }
$taskBadging = (& (Join-Path $taskSdk "build-tools/35.0.0/$taskAapt") dump badging $taskSource | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'APK metadata inspection failed.' }
if ($taskBadging -match '(?m)^application-debuggable(?:\s|$)') { throw 'Debuggable APKs cannot be published. Build the signed release variant.' }
$taskMatch = [regex]::Match($taskBadging, "package: name='com.fullmetalsonic.shortsloop' versionCode='(\d+)' versionName='(\d+\.\d+\.\d+)'")
if (-not $taskMatch.Success) { throw 'Only the product package and a plain release version are accepted; bootstrap APKs are not releasable.' }
$taskCode = [int]$taskMatch.Groups[1].Value
$taskVersion = $taskMatch.Groups[2].Value
$taskMinMatch = [regex]::Match($taskBadging, "minSdkVersion:'(\d+)'")
if (-not $taskMinMatch.Success) { throw 'APK minSdk is missing.' }
$taskMin = [int]$taskMinMatch.Groups[1].Value
$taskSignerTool = if ($IsWindows) { 'apksigner.bat' } else { 'apksigner' }
$taskSigner = (& (Join-Path $taskSdk "build-tools/35.0.0/$taskSignerTool") verify --print-certs $taskSource | Out-String)
$taskSignerDigests = [regex]::Matches($taskSigner, '(?im)^Signer #\d+ certificate SHA-256 digest: ([0-9a-f]{64})\s*$')
if ($LASTEXITCODE -ne 0 -or $taskSignerDigests.Count -ne 1 -or
    $taskSignerDigests[0].Groups[1].Value -ne '3604d3a1cc1f4e8f772d718cf8b9cba5adfd3650708cf169660b653e28b69632') {
    throw 'APK signature does not match the existing public release key. Do not publish a CI-rebuilt APK as an update.'
}
$taskRevision = (& git -C $taskRoot rev-parse HEAD | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $taskRevision -notmatch '^[a-f0-9]{40}$') { throw 'Cannot identify the committed build source.' }
$taskDirty = (& git -C $taskRoot status --porcelain -- app/src/main app/build.gradle build.gradle settings.gradle gradle.properties gradle gradlew gradlew.bat | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $taskDirty) { throw 'Commit product sources before freezing the release APK.' }
$taskArchive = [IO.Compression.ZipFile]::OpenRead($taskSource)
try {
    $taskEntry = $taskArchive.GetEntry('META-INF/version-control-info.textproto')
    if ($null -eq $taskEntry) { throw 'APK source revision is missing.' }
    $taskReader = [IO.StreamReader]::new($taskEntry.Open())
    try { $taskVcs = $taskReader.ReadToEnd() } finally { $taskReader.Dispose() }
    if ($taskVcs -notmatch ('revision: "' + $taskRevision + '"')) { throw 'Rebuild after the product commit; embedded APK revision differs from HEAD.' }
} finally { $taskArchive.Dispose() }
$taskSuffix = if ($OutputSuffix) { '-' + $OutputSuffix } else { '' }
$taskOutput = Join-Path $taskRoot "artifacts/release-v$taskVersion-code$taskCode$taskSuffix"
if (Test-Path -LiteralPath $taskOutput) { throw 'Release output already exists; preserve it and verify instead of overwriting.' }
New-Item -ItemType Directory -Path $taskOutput | Out-Null
$taskName = "shorts-loop-v$taskVersion.apk"
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
