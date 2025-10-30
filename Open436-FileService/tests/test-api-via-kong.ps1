# ============================================
# Open436 File Storage Service API Test
# Test all M7 APIs via Kong Gateway
# ============================================

$ErrorActionPreference = "Continue"

# Configuration
$KONG_URL = "http://localhost:8000"
$ADMIN_USERNAME = "admin"
$ADMIN_PASSWORD = "admin123"

# Test statistics
$script:TestsPassed = 0
$script:TestsFailed = 0
$script:UploadedFiles = @()

# Helper Functions
function Write-TestHeader {
    param([string]$Title)
    Write-Host ""
    Write-Host "===========================================" -ForegroundColor Cyan
    Write-Host $Title -ForegroundColor Cyan
    Write-Host "===========================================" -ForegroundColor Cyan
}

function Write-TestResult {
    param(
        [string]$TestName,
        [bool]$Passed,
        [string]$Message = ""
    )
    
    if ($Passed) {
        $script:TestsPassed++
        Write-Host "[PASS] $TestName" -ForegroundColor Green
        if ($Message) {
            Write-Host "       $Message" -ForegroundColor Gray
        }
    } else {
        $script:TestsFailed++
        Write-Host "[FAIL] $TestName" -ForegroundColor Red
        if ($Message) {
            Write-Host "       $Message" -ForegroundColor Yellow
        }
    }
}

function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )
    
    try {
        $params = @{
            Method = $Method
            Uri = $Url
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        return @{
            Success = $true
            Data = $response
        }
    } catch {
        return @{
            Success = $false
            Error = $_.Exception.Message
            StatusCode = $_.Exception.Response.StatusCode.value__
        }
    }
}

# Test 1: Health Check (Direct to service, bypass Kong)
function Test-HealthCheck {
    Write-TestHeader "TEST 1: Health Check"
    
    $result = Invoke-ApiRequest -Method "GET" -Url "http://localhost:8007/health"
    
    if ($result.Success -and $result.Data.status -eq "healthy") {
        Write-TestResult -TestName "Health Check" -Passed $true -Message "Service is healthy"
    } else {
        Write-TestResult -TestName "Health Check" -Passed $false -Message "Service unavailable"
    }
}

# Test 2: Login
function Get-AuthToken {
    Write-TestHeader "TEST 2: Admin Login"
    
    $loginBody = @{
        username = $ADMIN_USERNAME
        password = $ADMIN_PASSWORD
    }
    
    $result = Invoke-ApiRequest -Method "POST" -Url "$KONG_URL/api/auth/login" -Body $loginBody
    
    if ($result.Success -and $result.Data.data.token) {
        $token = $result.Data.data.token
        Write-TestResult -TestName "Admin Login" -Passed $true -Message "Token retrieved"
        return $token
    } else {
        Write-TestResult -TestName "Admin Login" -Passed $false -Message "Login failed"
        throw "Cannot get auth token"
    }
}

# Test 3: File Upload
function Test-FileUpload {
    param([string]$Token)
    
    Write-TestHeader "TEST 3: File Upload"
    
    $testFiles = @(
        @{ Path = "avatar.jpg"; Type = "avatar"; Name = "Avatar" },
        @{ Path = "post-image.png"; Type = "post"; Name = "Post Image" },
        @{ Path = "icon.png"; Type = "icon"; Name = "Icon" }
    )
    
    foreach ($file in $testFiles) {
        $filePath = Join-Path (Split-Path $PSCommandPath -Parent) "test-files\$($file.Path)"
        
        if (-not (Test-Path $filePath)) {
            Write-TestResult -TestName "Upload $($file.Name)" -Passed $false -Message "File not found"
            continue
        }
        
        try {
            $boundary = [System.Guid]::NewGuid().ToString()
            $LF = "`r`n"
            
            $fileBytes = [System.IO.File]::ReadAllBytes($filePath)
            $fileName = Split-Path $filePath -Leaf
            
            $bodyLines = @(
                "--$boundary",
                "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`"",
                "Content-Type: application/octet-stream$LF",
                [System.Text.Encoding]::GetEncoding("iso-8859-1").GetString($fileBytes),
                "--$boundary",
                "Content-Disposition: form-data; name=`"file_type`"$LF",
                $file.Type,
                "--$boundary--$LF"
            ) -join $LF
            
            $headers = @{
                "Authorization" = "Bearer $Token"
            }
            
            $response = Invoke-RestMethod -Method Post `
                -Uri "$KONG_URL/api/files/upload" `
                -Headers $headers `
                -ContentType "multipart/form-data; boundary=$boundary" `
                -Body $bodyLines
            
            if ($response.code -eq 201 -and $response.data.file_id) {
                $script:UploadedFiles += $response.data.file_id
                Write-TestResult -TestName "Upload $($file.Name)" -Passed $true `
                    -Message "ID: $($response.data.file_id)"
            } else {
                Write-TestResult -TestName "Upload $($file.Name)" -Passed $false
            }
        } catch {
            Write-TestResult -TestName "Upload $($file.Name)" -Passed $false -Message $_.Exception.Message
        }
    }
}

# Test 4: Get File Info
function Test-GetFileInfo {
    Write-TestHeader "TEST 4: Get File Info"
    
    if ($script:UploadedFiles.Count -eq 0) {
        Write-TestResult -TestName "Get File Info" -Passed $false -Message "No uploaded files"
        return
    }
    
    $fileId = $script:UploadedFiles[0]
    $result = Invoke-ApiRequest -Method "GET" -Url "$KONG_URL/api/files/$fileId"
    
    if ($result.Success -and $result.Data.data.file_id -eq $fileId) {
        Write-TestResult -TestName "Get File Info" -Passed $true `
            -Message "File: $($result.Data.data.filename), Status: $($result.Data.data.status)"
    } else {
        Write-TestResult -TestName "Get File Info" -Passed $false
    }
}

# Test 5: Get File URL
function Test-GetFileUrl {
    Write-TestHeader "TEST 5: Get File URL"
    
    if ($script:UploadedFiles.Count -eq 0) {
        Write-TestResult -TestName "Get File URL" -Passed $false -Message "No uploaded files"
        return
    }
    
    $fileId = $script:UploadedFiles[0]
    $result = Invoke-ApiRequest -Method "GET" -Url "$KONG_URL/api/files/$fileId/url"
    
    if ($result.Success -and $result.Data.data.url) {
        Write-TestResult -TestName "Get File URL" -Passed $true -Message $result.Data.data.url
    } else {
        Write-TestResult -TestName "Get File URL" -Passed $false
    }
}

# Test 6: Mark File Used
function Test-MarkFileUsed {
    param([string]$Token)
    
    Write-TestHeader "TEST 6: Mark File as Used"
    
    if ($script:UploadedFiles.Count -eq 0) {
        Write-TestResult -TestName "Mark File Used" -Passed $false -Message "No uploaded files"
        return
    }
    
    $fileId = $script:UploadedFiles[0]
    $body = @{
        usage_type = "post"
        usage_id = 1
    }
    
    $headers = @{ "Authorization" = "Bearer $Token" }
    $result = Invoke-ApiRequest -Method "POST" -Url "$KONG_URL/api/files/$fileId/mark-used" `
        -Headers $headers -Body $body
    
    if ($result.Success -and $result.Data.data.status -eq "used") {
        Write-TestResult -TestName "Mark File Used" -Passed $true -Message "Status: used"
    } else {
        Write-TestResult -TestName "Mark File Used" -Passed $false
    }
}

# Test 7: Mark File Unused
function Test-MarkFileUnused {
    param([string]$Token)
    
    Write-TestHeader "TEST 7: Mark File as Unused"
    
    if ($script:UploadedFiles.Count -eq 0) {
        Write-TestResult -TestName "Mark File Unused" -Passed $false -Message "No uploaded files"
        return
    }
    
    $fileId = $script:UploadedFiles[0]
    $body = @{
        usage_type = "post"
        usage_id = 1
    }
    
    $headers = @{ "Authorization" = "Bearer $Token" }
    $result = Invoke-ApiRequest -Method "POST" -Url "$KONG_URL/api/files/$fileId/mark-unused" `
        -Headers $headers -Body $body
    
    if ($result.Success -and $result.Data.data.status -eq "unused") {
        Write-TestResult -TestName "Mark File Unused" -Passed $true -Message "Status: unused"
    } else {
        Write-TestResult -TestName "Mark File Unused" -Passed $false
    }
}

# Test 8: Batch Get File Info
function Test-BatchGetFileInfo {
    Write-TestHeader "TEST 8: Batch Get File Info"
    
    if ($script:UploadedFiles.Count -lt 2) {
        Write-TestResult -TestName "Batch Get Files" -Passed $false -Message "Not enough files"
        return
    }
    
    $body = @{
        file_ids = $script:UploadedFiles[0..([Math]::Min(2, $script:UploadedFiles.Count - 1))]
    }
    
    $result = Invoke-ApiRequest -Method "POST" -Url "$KONG_URL/api/files/batch-info" -Body $body
    
    if ($result.Success -and $result.Data.data.files.Count -gt 0) {
        Write-TestResult -TestName "Batch Get Files" -Passed $true `
            -Message "Got $($result.Data.data.files.Count) files"
    } else {
        Write-TestResult -TestName "Batch Get Files" -Passed $false
    }
}

# Test 9: Get Statistics
function Test-GetStatistics {
    param([string]$Token)
    
    Write-TestHeader "TEST 9: Get Statistics"
    
    $headers = @{ "Authorization" = "Bearer $Token" }
    $result = Invoke-ApiRequest -Method "GET" -Url "$KONG_URL/api/files/statistics" -Headers $headers
    
    if ($result.Success -and $result.Data.data.total_files -ge 0) {
        Write-TestResult -TestName "Get Statistics" -Passed $true `
            -Message "Files: $($result.Data.data.total_files), Size: $($result.Data.data.total_size_pretty)"
    } else {
        Write-TestResult -TestName "Get Statistics" -Passed $false
    }
}

# Test 10: Manual Cleanup
function Test-ManualCleanup {
    param([string]$Token)
    
    Write-TestHeader "TEST 10: Manual Cleanup (Dry Run)"
    
    $body = @{
        dry_run = $true
        batch_size = 10
    }
    
    $headers = @{ "Authorization" = "Bearer $Token" }
    $result = Invoke-ApiRequest -Method "POST" -Url "$KONG_URL/api/files/cleanup" `
        -Headers $headers -Body $body
    
    if ($result.Success) {
        Write-TestResult -TestName "Manual Cleanup" -Passed $true `
            -Message "Deleted: $($result.Data.data.files_deleted), Freed: $($result.Data.data.space_freed_pretty)"
    } else {
        Write-TestResult -TestName "Manual Cleanup" -Passed $false
    }
}

# Test 11: Delete File
function Test-DeleteFile {
    param([string]$Token)
    
    Write-TestHeader "TEST 11: Delete File"
    
    if ($script:UploadedFiles.Count -lt 2) {
        Write-TestResult -TestName "Delete File" -Passed $false -Message "Not enough files"
        return
    }
    
    $fileId = $script:UploadedFiles[-1]
    $headers = @{ "Authorization" = "Bearer $Token" }
    $result = Invoke-ApiRequest -Method "DELETE" -Url "$KONG_URL/api/files/$fileId" -Headers $headers
    
    if ($result.Success -and $result.Data.data.status -eq "deleted") {
        Write-TestResult -TestName "Delete File" -Passed $true -Message "File deleted"
    } else {
        Write-TestResult -TestName "Delete File" -Passed $false
    }
}

# Test 12: Error Handling
function Test-ErrorHandling {
    Write-TestHeader "TEST 12: Error Handling"
    
    $fakeId = "00000000-0000-0000-0000-000000000000"
    $result = Invoke-ApiRequest -Method "GET" -Url "$KONG_URL/api/files/$fakeId"
    
    if (-not $result.Success -and $result.StatusCode -eq 404) {
        Write-TestResult -TestName "404 Not Found" -Passed $true -Message "Correctly returned 404"
    } else {
        Write-TestResult -TestName "404 Not Found" -Passed $false
    }
}

# Main Test Runner
function Run-AllTests {
    $startTime = Get-Date
    
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Magenta
    Write-Host "  Open436 File Service API Test" -ForegroundColor Magenta
    Write-Host "=========================================" -ForegroundColor Magenta
    Write-Host "Target: $KONG_URL" -ForegroundColor Yellow
    Write-Host "Start Time: $startTime" -ForegroundColor Yellow
    Write-Host ""
    
    try {
        Test-HealthCheck
        $token = Get-AuthToken
        Test-FileUpload -Token $token
        Test-GetFileInfo
        Test-GetFileUrl
        Test-MarkFileUsed -Token $token
        Test-MarkFileUnused -Token $token
        Test-BatchGetFileInfo
        Test-GetStatistics -Token $token
        Test-ManualCleanup -Token $token
        Test-DeleteFile -Token $token
        Test-ErrorHandling
    } catch {
        Write-Host ""
        Write-Host "Test execution failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test Report
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Magenta
    Write-Host "  Test Report" -ForegroundColor Magenta
    Write-Host "=========================================" -ForegroundColor Magenta
    Write-Host "Passed: $script:TestsPassed" -ForegroundColor Green
    Write-Host "Failed: $script:TestsFailed" -ForegroundColor $(if ($script:TestsFailed -eq 0) { "Green" } else { "Red" })
    Write-Host "Total: $($script:TestsPassed + $script:TestsFailed)" -ForegroundColor Cyan
    Write-Host "Duration: $([math]::Round($duration.TotalSeconds, 2)) seconds" -ForegroundColor Cyan
    Write-Host ""
    
    if ($script:UploadedFiles.Count -gt 0) {
        Write-Host "Uploaded File IDs:" -ForegroundColor Yellow
        $script:UploadedFiles | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }
        Write-Host ""
    }
    
    if ($script:TestsFailed -eq 0) {
        Write-Host "All tests passed!" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "Some tests failed" -ForegroundColor Yellow
        exit 1
    }
}

# Run all tests
Run-AllTests
