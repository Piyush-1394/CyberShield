#Requires -Version 5.1
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$backend = Join-Path $root "backend"

if (-not $env:JAVA_HOME) {
  # Auto-detect: look for any JDK 21+ installation
  $searchDirs = @("C:\Program Files\Java", "C:\Program Files\Eclipse Adoptium")
  foreach ($dir in $searchDirs) {
    $jdk = Get-ChildItem $dir -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "jdk-(\d+)" -and [int]$Matches[1] -ge 21 } | Sort-Object { [int]($_.Name -replace '.*jdk-(\d+).*','$1') } -Descending | Select-Object -First 1
    if ($jdk) { $env:JAVA_HOME = $jdk.FullName; break }
  }
}

Write-Host "JAVA_HOME=$($env:JAVA_HOME)"
Set-Location $backend
& .\mvnw.cmd spring-boot:run
