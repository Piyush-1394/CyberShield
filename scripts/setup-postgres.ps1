#Requires -Version 5.1
$ErrorActionPreference = "Stop"
$pgUser = "cybershield"
$pgPass = "cybershield"
$pgDb = "cybershield"

function Find-Psql {
  $candidates = @(
    "$env:ProgramFiles\PostgreSQL\16\bin\psql.exe",
    "$env:ProgramFiles\PostgreSQL\17\bin\psql.exe",
    "$env:ProgramFiles\PostgreSQL\15\bin\psql.exe"
  )
  foreach ($c in $candidates) { if (Test-Path $c) { return $c } }
  $cmd = Get-Command psql -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }
  return $null
}

$psql = Find-Psql
if (-not $psql) {
  Write-Host "PostgreSQL client not found. Install PostgreSQL 16+ then re-run this script."
  Write-Host "winget install --id PostgreSQL.PostgreSQL.16 -e --accept-package-agreements --accept-source-agreements"
  exit 1
}

$env:PGPASSWORD = "postgres"
$createdb = Join-Path (Split-Path $psql) "createdb.exe"

Write-Host "Using $psql"

$sql = @"
DO `$`$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$pgUser') THEN
    CREATE ROLE $pgUser LOGIN PASSWORD '$pgPass';
  END IF;
END
`$`$;
SELECT 'ok';
"@

$superUsers = @("postgres")
$ok = $false
foreach ($su in $superUsers) {
  try {
    & $psql -U $su -d postgres -v ON_ERROR_STOP=1 -c $sql | Out-Null
    $exists = & $psql -U $su -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$pgDb'"
    if ("$exists".Trim() -ne "1") {
      & $psql -U $su -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $pgDb OWNER $pgUser;"
    }
    $ok = $true
    break
  } catch {
    Write-Host "Could not connect as $su (set PGPASSWORD if your postgres user uses another password)."
  }
}

if (-not $ok) {
  Write-Host "Create the DB manually:"
  Write-Host "  CREATE USER cybershield WITH PASSWORD 'cybershield';"
  Write-Host "  CREATE DATABASE cybershield OWNER cybershield;"
  exit 1
}

Write-Host "PostgreSQL ready: database '$pgDb' user '$pgUser'"
