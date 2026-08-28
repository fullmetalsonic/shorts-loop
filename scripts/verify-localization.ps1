$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
function Read-Strings([string]$Folder) {
    $taskMap = @{}
    Get-ChildItem -LiteralPath (Join-Path $taskRoot "app/src/main/res/$Folder") -Filter 'strings*.xml' | ForEach-Object {
        [xml]$taskXml = Get-Content -LiteralPath $_.FullName -Raw -Encoding utf8
        foreach ($taskItem in $taskXml.resources.string) {
            $taskKey = $taskItem.GetAttribute('name')
            if ($taskMap.ContainsKey($taskKey)) { throw "Duplicate localization key: $taskKey" }
            $taskMap[$taskKey] = $taskItem.InnerText
        }
    }
    return $taskMap
}
$taskEnglish = Read-Strings 'values'
$taskKorean = Read-Strings 'values-ko'
if (Compare-Object @($taskEnglish.Keys | Sort-Object) @($taskKorean.Keys | Sort-Object)) { throw 'Language key mismatch.' }
foreach ($taskKey in $taskEnglish.Keys) {
    if ($taskEnglish[$taskKey] -match '[가-힣]') { throw "Korean leaked into default English: $taskKey" }
    $taskEnFormat = @([regex]::Matches($taskEnglish[$taskKey], '%\d+\$[dsf]') | ForEach-Object Value | Sort-Object) -join ','
    $taskKoFormat = @([regex]::Matches($taskKorean[$taskKey], '%\d+\$[dsf]') | ForEach-Object Value | Sort-Object) -join ','
    if ($taskEnFormat -ne $taskKoFormat) { throw "Format arguments differ: $taskKey" }
}
$taskSources = Get-ChildItem -LiteralPath (Join-Path $taskRoot 'app/src/main/java') -Recurse -Filter '*.java'
foreach ($taskFile in $taskSources) {
    $taskText = Get-Content -LiteralPath $taskFile.FullName -Raw -Encoding utf8
    # Host recognition lexicons must remain bilingual; they are not display copy.
    if ($taskFile.Name -notin @('ProgressParser.java','InstagramPolicy.java','YouTubeContentKey.java') -and $taskText -match '[가-힣]') {
        throw "Hardcoded Korean outside the host-recognition lexicon: $($taskFile.Name)"
    }
}
$taskRenderer = Get-Content -LiteralPath (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/i18n/StatusText.java') -Raw
$taskCodes = [regex]::Matches($taskRenderer, 'case "([a-z_.]+)": return R.string.(state_[a-z_]+);')
$taskSeen = @{}
foreach ($taskCode in $taskCodes) {
    $taskToken = $taskCode.Groups[1].Value; $taskKey = $taskCode.Groups[2].Value
    if ($taskSeen.ContainsKey($taskToken) -or -not $taskEnglish.ContainsKey($taskKey)) { throw "Invalid status mapping: $taskToken" }
    $taskSeen[$taskToken] = $true
}
"LOCALIZATION_RESOURCE_AUDIT=PASS keys=$($taskEnglish.Count) statuses=$($taskCodes.Count)"
