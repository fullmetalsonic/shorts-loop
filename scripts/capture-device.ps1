param(
    [Parameter(Mandatory=$true)][string]$Device,
    [Parameter(Mandatory=$true)][ValidatePattern('^\d+$')][string]$DisplayId,
    [string]$Name = 'device-screen.png'
)
$ErrorActionPreference = 'Stop'
if ($Name -notmatch '^[a-zA-Z0-9_-]+\.png$') { throw 'Use a simple PNG filename.' }
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskOutputDirectory = Join-Path $taskRoot 'private/device-captures'
if (-not (Test-Path -LiteralPath $taskOutputDirectory)) { New-Item -ItemType Directory -Path $taskOutputDirectory | Out-Null }
$taskOutput = Join-Path $taskOutputDirectory $Name
$taskStart = [Diagnostics.ProcessStartInfo]::new()
$taskStart.FileName = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$taskStart.UseShellExecute = $false
$taskStart.CreateNoWindow = $true
$taskStart.RedirectStandardOutput = $true
$taskStart.RedirectStandardError = $true
$taskArguments = @('-s', $Device, 'exec-out', 'screencap', '-p')
if ($DisplayId) { $taskArguments += @('-d', $DisplayId) }
foreach ($taskArgument in $taskArguments) { $taskStart.ArgumentList.Add($taskArgument) }
$taskProcess = [Diagnostics.Process]::Start($taskStart)
$taskFile = [IO.File]::Open($taskOutput, [IO.FileMode]::CreateNew)
try { $taskProcess.StandardOutput.BaseStream.CopyTo($taskFile) } finally { $taskFile.Dispose() }
$taskProcess.WaitForExit()
if ($taskProcess.ExitCode -ne 0) { throw $taskProcess.StandardError.ReadToEnd() }
$taskProcess.Dispose()
$taskPngBytes = [IO.File]::ReadAllBytes($taskOutput)
if ($taskPngBytes.Length -lt 8 -or [BitConverter]::ToString($taskPngBytes, 0, 8) -ne '89-50-4E-47-0D-0A-1A-0A') {
    throw 'Capture is not a clean PNG. Preserve it privately for diagnosis; check the active display.'
}
$taskOutput
