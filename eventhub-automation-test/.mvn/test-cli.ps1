param(
    [Parameter(Mandatory = $true)]
    [string]$MvnCmd,

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$RawArgs
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$TestsRoot = Join-Path $ProjectRoot "src\test\java\com\eventhub\automation\tests"

$UcCatalog = [ordered]@{
    "01" = "Dang ky tai khoan (Register)"
    "02" = "Dang nhap he thong (Login)"
    "03" = "Dang xuat (Logout)"
    "04" = "Quan ly thong tin ca nhan (Profile)"
    "05" = "Tim kiem su kien (Search)"
    "06" = "Loc su kien da tieu chi (Filter)"
    "07" = "Dat ve & Thanh toan (Booking)"
    "08" = "Lich su & Chi tiet Booking"
    "09" = "Ve dien tu & QR Code (My Tickets)"
    "10" = "Thong bao nguoi dung (Notifications)"
    "11" = "Chat voi AI (AI Assistant Chatbot)"
    "12" = "Ban to chuc tao su kien (Create Event)"
    "13" = "Phan cong nhan vien (Staff Dispatch)"
    "14" = "Huy su kien (Cancel Event)"
    "15" = "Yeu cau rut tien (Organizer Wallet)"
    "16" = "Soat ve & Check-in QR (Staff)"
    "17" = "Admin quan ly nguoi dung (Users)"
    "18" = "Quan ly danh muc su kien (Categories)"
    "19" = "Phe duyet su kien (Event Approval)"
    "20" = "Hoan tien su kien huy (Refunds)"
    "21" = "Phe duyet rut tien (Admin Payout)"
    "22" = "Bao cao ban to chuc (Organizer Reports)"
    "23" = "Chinh sua su kien (Edit Event)"
    "24" = "Dashboard & Thong ke Admin"
    "25" = "Doi mat khau (Change Password)"
}

function Show-Usage {
    Write-Host ""
    Write-Host "=== EventHub Automation Test CLI ===" -ForegroundColor Cyan
    Write-Host "Cu phap: .\mvnw test <muc-tieu> [tuy-chon]" -ForegroundColor White
    Write-Host ""
    Write-Host "Vi du nhanh:" -ForegroundColor Yellow
    Write-Host "  .\mvnw test UC01                   Chay toan bo test cua UC01"
    Write-Host "  .\mvnw test UC01 UC02              Chay nhieu UC (UC01 va UC02)"
    Write-Host "  .\mvnw test UC01-UC05              Chay theo dai UC (UC01 den UC05)"
    Write-Host "  .\mvnw test UC01_01                Chay 1 test case cu the (TC_UC01_01*)"
    Write-Host "  .\mvnw test TC_UC07_07             Chay theo ma test case (TC_UC07_07*)"
    Write-Host "  .\mvnw test customer               Chay toan bo UC cua Customer (UC01-UC10)"
    Write-Host "  .\mvnw test organizer              Chay toan bo UC cua Organizer (UC12-UC15,UC22,UC23)"
    Write-Host "  .\mvnw test staff                  Chay toan bo UC cua Staff (UC16)"
    Write-Host "  .\mvnw test admin                  Chay toan bo UC cua Admin (UC17-UC21,UC24)"
    Write-Host "  .\mvnw test smoke                  Chay CheckEnvironment + test _01 cua moi UC"
    Write-Host "  .\mvnw test env                    Chay CheckEnvironmentTest (kiem tra backend/FE/DB)"
    Write-Host "  .\mvnw test all                    Chay full suite (testng.xml)"
    Write-Host ""
    Write-Host "Tuy chon bo sung (khong can -D):" -ForegroundColor Yellow
    Write-Host "  headless       Chay ngam Chrome (khong mo cua so)"
    Write-Host "  fast           Tat delay giua cac buoc (step.delay=0)"
    Write-Host "  slow           Cham lai 2s moi buoc de quan sat (step.delay=2000)"
    Write-Host "  edge / chrome  Chon trinh duyet chay test"
    Write-Host "  no-rollback    Tat tu dong rollback database sau test"
    Write-Host "  ignore-fail    Khong bao BUILD FAILURE khi co test fail"
    Write-Host "  Vi du ket hop: .\mvnw test UC01 headless fast" -ForegroundColor Green
    Write-Host ""
    Write-Host "Danh sach Use Case hien co:" -ForegroundColor Yellow

    $totalTests = 0
    foreach ($code in $UcCatalog.Keys) {
        $dirMatch = Get-ChildItem -Path $TestsRoot -Directory -Filter "uc${code}_*" -ErrorAction SilentlyContinue | Select-Object -First 1
        $count = 0
        if ($dirMatch) {
            $count = @(Get-ChildItem -Path $dirMatch.FullName -Filter "TC_*.java" -ErrorAction SilentlyContinue).Count
        }
        $totalTests += $count
        $label = $UcCatalog[$code]
        Write-Host ("  UC{0}  ({1,2} tests)  - {2}" -f $code, $count, $label)
    }
    Write-Host ""
    Write-Host ("Tong cong: {0} Test Cases trong {1} Use Cases (+ CheckEnvironmentTest)." -f $totalTests, $UcCatalog.Count) -ForegroundColor Cyan
    Write-Host ""
}

function Add-UcPattern {
    param(
        [string]$NumStr,
        [System.Collections.Generic.List[string]]$Patterns
    )
    $padded = $NumStr.PadLeft(2, '0')
    if (-not $UcCatalog.Contains($padded)) {
        Write-Host "[EventHub Test] Khong tim thay Use Case UC$padded. Go '.\mvnw test list' de xem danh sach." -ForegroundColor Red
        return "NOT_FOUND"
    }
    $pattern = "TC_UC${padded}_*"
    if (-not $Patterns.Contains($pattern)) {
        $Patterns.Add($pattern)
    }
    return "OK"
}

$MavenGoals = [System.Collections.Generic.List[string]]::new()
$MavenFlags = [System.Collections.Generic.List[string]]::new()
$TestPatterns = [System.Collections.Generic.List[string]]::new()
$HasExplicitDTest = $false
$ForceAll = $false

foreach ($raw in $RawArgs) {
    if ([string]::IsNullOrWhiteSpace($raw)) { continue }
    $arg = $raw.Trim()

    # Check standard Maven lifecycle phases
    if ($arg -ieq "clean" -or $arg -ieq "test") {
        if (-not $MavenGoals.Contains($arg.ToLower())) {
            $MavenGoals.Add($arg.ToLower())
        }
        continue
    }

    # Check standard Maven flags (-D..., -X, -q, etc.) and friendly --flags
    if ($arg.StartsWith("-")) {
        switch ($arg.ToLower()) {
            "--help"        { Show-Usage; exit 0 }
            "-h"            { Show-Usage; exit 0 }
            "--list"        { Show-Usage; exit 0 }
            "--headless"    { $MavenFlags.Add("-Dheadless=true"); $arg = $null; break }
            "-headless"     { $MavenFlags.Add("-Dheadless=true"); $arg = $null; break }
            "--ui"          { $MavenFlags.Add("-Dheadless=false"); $arg = $null; break }
            "--headed"      { $MavenFlags.Add("-Dheadless=false"); $arg = $null; break }
            "--fast"        { $MavenFlags.Add("-Dstep.delay=0"); $arg = $null; break }
            "--normal"      { $MavenFlags.Add("-Dstep.delay=500"); $arg = $null; break }
            "--slow"        { $MavenFlags.Add("-Dstep.delay=2000"); $arg = $null; break }
            "--no-rollback" { $MavenFlags.Add("-Ddb.rollback.enabled=false"); $arg = $null; break }
            "--ignore-fail" { $MavenFlags.Add("-Dtest.failure.ignore=true"); $arg = $null; break }
        }
        if ($null -eq $arg) { continue }
        if ($arg -like "-Dtest=*") {
            $HasExplicitDTest = $true
        }
        $MavenFlags.Add($arg)
        continue
    }

    # Help / List
    if ($arg -ieq "list" -or $arg -ieq "help" -or $arg -ieq "/?") {
        Show-Usage
        exit 0
    }

    # Option modifiers and preset groups
    $handled = $true
    switch ($arg.ToLower()) {
        "headless"    { $MavenFlags.Add("-Dheadless=true"); break }
        "ui"          { $MavenFlags.Add("-Dheadless=false"); break }
        "headed"      { $MavenFlags.Add("-Dheadless=false"); break }
        "fast"        { $MavenFlags.Add("-Dstep.delay=0"); break }
        "normal"      { $MavenFlags.Add("-Dstep.delay=500"); break }
        "slow"        { $MavenFlags.Add("-Dstep.delay=2000"); break }
        "chrome"      { $MavenFlags.Add("-Dbrowser=chrome"); break }
        "edge"        { $MavenFlags.Add("-Dbrowser=edge"); break }
        "no-rollback" { $MavenFlags.Add("-Ddb.rollback.enabled=false"); break }
        "ignore-fail" { $MavenFlags.Add("-Dtest.failure.ignore=true"); break }
        "all"         { $ForceAll = $true; break }
        "env"         { if (-not $TestPatterns.Contains("CheckEnvironmentTest")) { $TestPatterns.Add("CheckEnvironmentTest") }; break }
        "check"       { if (-not $TestPatterns.Contains("CheckEnvironmentTest")) { $TestPatterns.Add("CheckEnvironmentTest") }; break }
        "smoke"       {
            if (-not $TestPatterns.Contains("CheckEnvironmentTest")) { $TestPatterns.Add("CheckEnvironmentTest") }
            if (-not $TestPatterns.Contains("TC_UC*_01_*")) { $TestPatterns.Add("TC_UC*_01_*") }
            break
        }
        "customer"    {
            @(1..11) + @(25) | ForEach-Object { [void](Add-UcPattern "$_" $TestPatterns) }
            break
        }
        "ai"          {
            [void](Add-UcPattern "11" $TestPatterns)
            break
        }
        "organizer"   {
            @(12, 13, 14, 15, 22, 23) | ForEach-Object { [void](Add-UcPattern "$_" $TestPatterns) }
            break
        }
        "staff"       {
            [void](Add-UcPattern "16" $TestPatterns)
            break
        }
        "admin"       {
            @(17, 18, 19, 20, 21, 24) | ForEach-Object { [void](Add-UcPattern "$_" $TestPatterns) }
            break
        }
        default       { $handled = $false }
    }
    if ($handled) { continue }

    # Handle comma-separated target tokens like UC01,UC02
    $subTokens = $arg -split ','
    foreach ($sub in $subTokens) {
        $token = $sub.Trim()
        if ([string]::IsNullOrWhiteSpace($token)) { continue }

        if ($token -match '^(?i)uc(\d{1,2})-(?:uc)?(\d{1,2})$') {
            $start = [int]$matches[1]
            $end = [int]$matches[2]
            if ($start -gt $end) {
                $tmp = $start; $start = $end; $end = $tmp
            }
            for ($i = $start; $i -le $end; $i++) {
                $res = Add-UcPattern "$i" $TestPatterns
                if ($res -eq "NOT_FOUND") { exit 1 }
            }
        }
        elseif ($token -match '^(?i)uc(\d{1,2})$') {
            $res = Add-UcPattern $matches[1] $TestPatterns
            if ($res -eq "NOT_FOUND") {
                exit 1
            }
        }
        elseif ($token -match '^(?i)(?:tc_)?uc(\d{1,2})_(\d{1,2})$') {
            $ucPadded = $matches[1].PadLeft(2, '0')
            $tcPadded = $matches[2].PadLeft(2, '0')
            $pat = "TC_UC${ucPadded}_${tcPadded}*"
            if (-not $TestPatterns.Contains($pat)) {
                $TestPatterns.Add($pat)
            }
        }
        elseif ($token -match '^(?i)TC_UC\d{2}.*' -or $token -like '*Test' -or $token -like '*`**') {
            if (-not $TestPatterns.Contains($token)) {
                $TestPatterns.Add($token)
            }
        }
        else {
            Write-Host "[EventHub Test] Khong hieu tham so: '$token'" -ForegroundColor Red
            Write-Host "Go '.\mvnw test list' de xem danh sach lenh va Use Case hop le." -ForegroundColor Yellow
            exit 1
        }
    }
}

if ($MavenGoals.Count -eq 0) {
    $MavenGoals.Add("test")
}

$FinalArgs = [System.Collections.Generic.List[string]]::new()
foreach ($g in $MavenGoals) { $FinalArgs.Add($g) }

if (-not $HasExplicitDTest -and -not $ForceAll -and $TestPatterns.Count -gt 0) {
    $joined = $TestPatterns -join ","
    $FinalArgs.Add("-Dtest=$joined")
    Write-Host "[EventHub Test] Target: $joined" -ForegroundColor Cyan
} elseif (-not $HasExplicitDTest -and $TestPatterns.Count -eq 0) {
    Write-Host "[EventHub Test] Target: Full Test Suite (testng.xml)" -ForegroundColor Cyan
}

foreach ($f in $MavenFlags) {
    $FinalArgs.Add($f)
}

if ($MavenFlags.Count -gt 0) {
    Write-Host "[EventHub Test] Options: $($MavenFlags -join ' ')" -ForegroundColor DarkCyan
}

# Build properly escaped command string for cmd.exe / mvn.cmd
# Quoting arguments containing ',' or '*' prevents cmd.exe batch FOR loop from splitting on commas
$CmdLineParts = [System.Collections.Generic.List[string]]::new()
$CmdLineParts.Add("`"$MvnCmd`"")
foreach ($a in $FinalArgs) {
    if ($a -match '[,\s\*;]') {
        $CmdLineParts.Add("`"$a`"")
    } else {
        $CmdLineParts.Add($a)
    }
}

$FullCmdLine = $CmdLineParts -join " "
cmd.exe /c $FullCmdLine
exit $LASTEXITCODE
