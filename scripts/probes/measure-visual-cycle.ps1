param(
    [Parameter(Mandatory=$true)][string]$Device,
    [Parameter(Mandatory=$true)][ValidatePattern('^\d+$')][string]$DisplayId,
    [ValidateRange(12,120)][int]$Samples = 60,
    [ValidateRange(300,1500)][int]$IntervalMilliseconds = 500,
    [int]$Left = 270, [int]$Top = 400, [int]$Width = 580, [int]$Height = 900
)
# Research only: no gestures, settings, recording, file output, or automatic loop decisions.
# Screenshot bytes and coarse pixel features live in memory only. Output is aggregate numeric data.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$taskAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$taskFrames = [Collections.Generic.List[double[]]]::new()
$taskTimes = [Collections.Generic.List[double]]::new()
$taskClock = [Diagnostics.Stopwatch]::StartNew()
function Get-Distance([double[]]$First, [double[]]$Second) {
    $taskTotal = 0.0
    for ($taskPixel = 0; $taskPixel -lt $First.Length; $taskPixel++) {
        $taskTotal += [Math]::Abs($First[$taskPixel] - $Second[$taskPixel])
    }
    return $taskTotal / $First.Length
}
try {
    for ($taskIndex = 0; $taskIndex -lt $Samples; $taskIndex++) {
        $taskStartAt = $taskClock.Elapsed.TotalMilliseconds
        $taskStart = [Diagnostics.ProcessStartInfo]::new()
        $taskStart.FileName = $taskAdb
        $taskStart.UseShellExecute = $false
        $taskStart.CreateNoWindow = $true
        $taskStart.RedirectStandardOutput = $true
        $taskStart.RedirectStandardError = $true
        foreach ($taskArg in @('-s', $Device, 'exec-out', 'screencap', '-p', '-d', $DisplayId)) {
            $taskStart.ArgumentList.Add($taskArg)
        }
        $taskProcess = [Diagnostics.Process]::Start($taskStart)
        $taskBuffer = [IO.MemoryStream]::new()
        $taskBitmap = $null
        try {
            $taskErrorRead = $taskProcess.StandardError.ReadToEndAsync()
            $taskCopy = $taskProcess.StandardOutput.BaseStream.CopyToAsync($taskBuffer)
            if (-not $taskProcess.WaitForExit(5000)) {
                $taskProcess.Kill()
                throw 'Screenshot timed out; research stopped.'
            }
            $null = $taskCopy.GetAwaiter().GetResult()
            if ($taskProcess.ExitCode -ne 0) { throw 'Screenshot command failed.' }
            $taskBuffer.Position = 0
            $taskBitmap = [Drawing.Bitmap]::new($taskBuffer)
            if ($Left -lt 0 -or $Top -lt 0 -or $Width -lt 64 -or $Height -lt 64 -or
                $Left + $Width -gt $taskBitmap.Width -or $Top + $Height -gt $taskBitmap.Height) {
                throw 'Region is outside the observed display.'
            }
            $taskFeatures = [double[]]::new(16 * 24 * 3)
            $taskFeature = 0
            for ($taskY = 0; $taskY -lt 24; $taskY++) {
                for ($taskX = 0; $taskX -lt 16; $taskX++) {
                    $taskPx = $Left + [int](($taskX + 0.5) * $Width / 16)
                    $taskPy = $Top + [int](($taskY + 0.5) * $Height / 24)
                    $taskColor = $taskBitmap.GetPixel($taskPx, $taskPy)
                    $taskFeatures[$taskFeature++] = $taskColor.R
                    $taskFeatures[$taskFeature++] = $taskColor.G
                    $taskFeatures[$taskFeature++] = $taskColor.B
                }
            }
            $taskFrames.Add($taskFeatures)
            $taskTimes.Add(($taskStartAt + $taskClock.Elapsed.TotalMilliseconds) / 2000)
            $taskMotion = if ($taskIndex -gt 0) { Get-Distance $taskFeatures $taskFrames[$taskIndex - 1] } else { 0 }
            [pscustomobject]@{ Kind='Frame'; N=$taskIndex+1; Time=[Math]::Round($taskTimes[$taskIndex],3)
                CaptureMs=[Math]::Round($taskClock.Elapsed.TotalMilliseconds-$taskStartAt)
                Motion=[Math]::Round($taskMotion,3) } | ConvertTo-Json -Compress
        } finally {
            if ($taskBitmap) { $taskBitmap.Dispose() }
            $taskBuffer.Dispose()
            $taskProcess.Dispose()
        }
        $taskRemaining = $IntervalMilliseconds - ($taskClock.Elapsed.TotalMilliseconds - $taskStartAt)
        if ($taskRemaining -gt 0 -and $taskIndex + 1 -lt $Samples) {
            Start-Sleep -Milliseconds ([int]$taskRemaining)
        }
    }
    $taskCandidates = @()
    for ($taskLag = 3; $taskLag -le [int]($Samples / 2); $taskLag++) {
        $taskErrors = [Collections.Generic.List[double]]::new()
        $taskPeriods = [Collections.Generic.List[double]]::new()
        for ($taskIndex = $taskLag; $taskIndex -lt $Samples; $taskIndex++) {
            $taskErrors.Add((Get-Distance $taskFrames[$taskIndex] $taskFrames[$taskIndex-$taskLag]))
            $taskPeriods.Add($taskTimes[$taskIndex] - $taskTimes[$taskIndex-$taskLag])
        }
        $taskCandidates += [pscustomobject]@{ Kind='CandidateOnly'; Lag=$taskLag
            Period=[Math]::Round(($taskPeriods | Measure-Object -Average).Average,3)
            Error=[Math]::Round(($taskErrors | Measure-Object -Average).Average,3)
            Comparisons=$taskErrors.Count }
    }
    $taskCandidates | Sort-Object Error | Select-Object -First 8 | ForEach-Object { $_ | ConvertTo-Json -Compress }
    'VISUAL_RESEARCH_ONLY: candidates are not verified playback boundaries; static scenes and repeated motion can match.'
} finally {
    $taskFrames.Clear()
    $taskTimes.Clear()
}
