param()
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskCache = if ($env:GRADLE_USER_HOME) { Join-Path $env:GRADLE_USER_HOME 'caches/modules-2/files-2.1' } else { Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1' }
$taskJunit = Get-ChildItem -LiteralPath (Join-Path $taskCache 'junit/junit/4.13.2') -Recurse -Filter 'junit-4.13.2.jar' | Select-Object -First 1 -ExpandProperty FullName
$taskHamcrest = Get-ChildItem -LiteralPath (Join-Path $taskCache 'org.hamcrest/hamcrest-core/1.3') -Recurse -Filter 'hamcrest-core-1.3.jar' | Select-Object -First 1 -ExpandProperty FullName
$taskDirectory = Join-Path $taskRoot ('private/sequence-experiment-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $taskDirectory | Out-Null
$taskClassPath = @($taskJunit, $taskHamcrest) -join [IO.Path]::PathSeparator
Write-Warning 'UNCONNECTED EXPERIMENT, NOT THE PRODUCT: baseline 20 tests, 18 pass and 2 fail. Failures intentionally remain failures.'
& javac -encoding UTF-8 --release 17 -cp $taskClassPath -d $taskDirectory `
    (Join-Path $taskRoot 'app/src/main/java/com/fullmetalsonic/shortsloop/core/VisualSequenceTracker.java') `
    (Join-Path $taskRoot 'app/src/test/java/com/fullmetalsonic/shortsloop/core/VisualSequenceTrackerTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Experiment compilation failed.' }
& java '-Dfile.encoding=UTF-8' -cp ($taskDirectory + [IO.Path]::PathSeparator + $taskClassPath) org.junit.runner.JUnitCore com.fullmetalsonic.shortsloop.core.VisualSequenceTrackerTest
if ($LASTEXITCODE -ne 0) { throw 'Unconnected sequence experiment tests failed (known baseline: 2 failures).' }
