# Heavyweight, human-triggered backend bootstrap. Do not invoke from hooks.
$ErrorActionPreference = 'Stop'
$AppPort = 8080
$LogDir = 'logs'
$StdoutLog = Join-Path $LogDir 'backend.out.log'
$StderrLog = Join-Path $LogDir 'backend.err.log'

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
  throw 'Java 17+ is required.'
}
if (-not (Test-Path -LiteralPath $LogDir)) {
  New-Item -ItemType Directory -Path $LogDir | Out-Null
}

$MavenWrapper = Join-Path $PSScriptRoot 'back\mvnw.cmd'
$Process = Start-Process -FilePath $MavenWrapper -ArgumentList 'spring-boot:run' -WorkingDirectory (Join-Path $PSScriptRoot 'back') -RedirectStandardOutput (Join-Path $PSScriptRoot $StdoutLog) -RedirectStandardError (Join-Path $PSScriptRoot $StderrLog) -PassThru
Write-Host "[init] Backend started (PID $($Process.Id), logs -> $StdoutLog)"

for ($attempt = 0; $attempt -lt 60; $attempt++) {
  try {
    $client = [System.Net.Sockets.TcpClient]::new()
    $connect = $client.BeginConnect('localhost', $AppPort, $null, $null)
    if ($connect.AsyncWaitHandle.WaitOne(1000, $false)) {
      $client.EndConnect($connect)
      $client.Close()
      Write-Host "[init] Backend reachable on port $AppPort"
      exit 0
    }
    $client.Close()
  } catch {}
  Start-Sleep -Seconds 1
}

Write-Host "[ERROR] Backend was not reachable after 60 seconds; check $StdoutLog and $StderrLog"
exit 1
