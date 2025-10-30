# ============================================
# Open436 - M7 File Service Complete Test Script
# ============================================
# Test all 9 file service API endpoints through Kong Gateway
# Account: admin / test123
# ============================================

$ErrorActionPreference = "Continue"

# Configuration
$KONG_URL = "http://localhost:8000"
$USERNAME = "admin"
$PASSWORD = "test123"

# Test counters
$script:TestTotal = 0
$script:TestPassed = 0
$script:TestFailed = 0
$script:Token = ""
$script:UploadedFileIds = @()

# ============================================
# Helper Functions
# ============================================

function Write-TestHeader {
    param([string]$Title)
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host $Title -ForegroundColor Cyan
    Write-Host "=========================================" -ForegroundColor Cyan
}

function Test-Start {
    param([string]$TestName)
    $script:TestTotal++
    Write-Host ""
    Write-Host "[$script:TestTotal] $TestName" -ForegroundColor Magenta
}

function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
    $script:TestPassed++
}

function Write-Failure {
    param([string]$Message)
    Write-Host "[FAIL] $Message" -ForegroundColor Red
    $script:TestFailed++
}

function New-TestImage {
    param([string]$Path)
    
    # Create a 1x1 pixel PNG image
    $pngBytes = @(
        0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x02, 0x00, 0x00, 0x00, 0x90, 0x77, 0x53,
        0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
        0x54, 0x08, 0xD7, 0x63, 0xF8, 0xCF, 0xC0, 0x00,
        0x00, 0x03, 0x01, 0x01, 0x00, 0x18, 0xDD, 0x8D,
        0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E,
        0x44, 0xAE, 0x42, 0x60, 0x82
    )
    
    [System.IO.File]::WriteAllBytes($Path, [byte[]]$pngBytes)
    Write-Host "  Created test image: $Path ($($pngBytes.Length) bytes)" -ForegroundColor Gray
}

# ============================================
# Test Cases
# ============================================

function Test-Login {
    Test-Start "Login and get Token"
    
    try {
        $loginBody = @{
            username = $USERNAME
            password = $PASSWORD
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
        
        if ($response.code -eq 200) {
            $script:Token = $response.data.token
            Write-Success "Login successful, Token obtained"
            Write-Host "  User: $($response.data.user.username)" -ForegroundColor Gray
            Write-Host "  Roles: $($response.data.user.roles -join ', ')" -ForegroundColor Gray
        }
        else {
            Write-Failure "Login failed: code=$($response.code)"
            throw "Cannot continue testing"
        }
    }
    catch {
        Write-Failure "Login request failed: $($_.Exception.Message)"
        throw
    }
}

function Test-UploadFile {
    param([string]$FileType = "avatar")
    
    Test-Start "Upload file (file_type=$FileType)"
    
    $tempImage = Join-Path $PSScriptRoot "temp-test-image.png"
    New-TestImage -Path $tempImage
    
    try {
        $boundary = [System.Guid]::NewGuid().ToString()
        $LF = "`r`n"
        
        $fileBytes = [System.IO.File]::ReadAllBytes($tempImage)
        $fileName = [System.IO.Path]::GetFileName($tempImage)
        
        $bodyLines = @(
            "--$boundary",
            "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`"",
            "Content-Type: image/png",
            "",
            ""
        )
        
        $encoding = [System.Text.Encoding]::UTF8
        $header = $encoding.GetBytes(($bodyLines -join $LF))
        
        $footer = $encoding.GetBytes("$LF--$boundary$LF" + "Content-Disposition: form-data; name=`"file_type`"$LF$LF" + "$FileType$LF" + "--$boundary--$LF")
        
        $bodyBytes = New-Object byte[] ($header.Length + $fileBytes.Length + $footer.Length)
        [Array]::Copy($header, 0, $bodyBytes, 0, $header.Length)
        [Array]::Copy($fileBytes, 0, $bodyBytes, $header.Length, $fileBytes.Length)
        [Array]::Copy($footer, 0, $bodyBytes, ($header.Length + $fileBytes.Length), $footer.Length)
        
        $headers = @{
            "Authorization" = "Bearer $script:Token"
        }
        
        $webResponse = Invoke-WebRequest -Uri "$KONG_URL/api/files/upload" -Method Post -Headers $headers -ContentType "multipart/form-data; boundary=$boundary" -Body $bodyBytes
        
        $data = $webResponse.Content | ConvertFrom-Json
        
        if ($data.code -eq 201) {
            $fileId = $data.data.file_id
            $script:UploadedFileIds += $fileId
            Write-Success "File uploaded successfully"
            Write-Host "  File ID: $fileId" -ForegroundColor Gray
            Write-Host "  Filename: $($data.data.filename)" -ForegroundColor Gray
            Write-Host "  Size: $($data.data.file_size) bytes" -ForegroundColor Gray
            Write-Host "  Type: $($data.data.file_type)" -ForegroundColor Gray
            Write-Host "  Status: $($data.data.status)" -ForegroundColor Gray
            return $fileId
        }
        else {
            Write-Failure "Upload failed: code=$($data.code)"
            return $null
        }
    }
    catch {
        Write-Failure "Upload request failed: $($_.Exception.Message)"
        return $null
    }
    finally {
        if (Test-Path $tempImage) {
            Remove-Item $tempImage -Force
        }
    }
}

function Test-GetFileInfo {
    param([string]$FileId)
    
    Test-Start "Get file info"
    
    try {
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/$FileId" -Method Get
        
        if ($response.code -eq 200) {
            Write-Success "Got file info successfully"
            Write-Host "  File ID: $($response.data.file_id)" -ForegroundColor Gray
            Write-Host "  Filename: $($response.data.filename)" -ForegroundColor Gray
            Write-Host "  Status: $($response.data.status)" -ForegroundColor Gray
        }
        else {
            Write-Failure "Get info failed: code=$($response.code)"
        }
    }
    catch {
        Write-Failure "Request failed: $($_.Exception.Message)"
    }
}

function Test-GetFileUrl {
    param([string]$FileId)
    
    Test-Start "Get file URL"
    
    try {
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/$FileId/url" -Method Get
        
        if ($response.code -eq 200) {
            Write-Success "Got file URL successfully"
            Write-Host "  URL: $($response.data.url)" -ForegroundColor Gray
        }
        else {
            Write-Failure "Get URL failed: code=$($response.code)"
        }
    }
    catch {
        Write-Failure "Request failed: $($_.Exception.Message)"
    }
}

function Test-MarkFileUsed {
    param([string]$FileId)
    
    Test-Start "Mark file as used"
    
    try {
        $headers = @{
            "Authorization" = "Bearer $script:Token"
        }
        
        $body = @{
            usage_type = "avatar"
            usage_id = 1
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/$FileId/mark-used" -Method Post -Headers $headers -ContentType "application/json" -Body $body
        
        if ($response.code -eq 200) {
            Write-Success "Marked as used successfully"
            Write-Host "  Status: $($response.data.status)" -ForegroundColor Gray
        }
        else {
            Write-Failure "Mark failed: code=$($response.code)"
        }
    }
    catch {
        Write-Failure "Request failed: $($_.Exception.Message)"
    }
}

function Test-MarkFileUnused {
    param([string]$FileId)
    
    Test-Start "Mark file as unused"
    
    try {
        $headers = @{
            "Authorization" = "Bearer $script:Token"
        }
        
        $body = @{
            usage_type = "avatar"
            usage_id = 1
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/$FileId/mark-unused" -Method Post -Headers $headers -ContentType "application/json" -Body $body
        
        if ($response.code -eq 200) {
            Write-Success "Marked as unused successfully"
            Write-Host "  Status: $($response.data.status)" -ForegroundColor Gray
        }
        else {
            Write-Failure "Mark failed: code=$($response.code)"
        }
    }
    catch {
        Write-Failure "Request failed: $($_.Exception.Message)"
    }
}

function Test-BatchGetFileInfo {
    param([array]$FileIds)
    
    Test-Start "Batch get file info"
    
    try {
        $body = @{
            file_ids = $FileIds
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/batch-info" -Method Post -ContentType "application/json" -Body $body
        
        if ($response.code -eq 200) {
            $count = $response.data.files.Count
            Write-Success "Batch get successful, returned $count files"
            foreach ($file in $response.data.files) {
                Write-Host "  - $($file.file_id): $($file.filename)" -ForegroundColor Gray
            }
        }
        else {
            Write-Failure "Batch get failed: code=$($response.code)"
        }
    }
    catch {
        Write-Failure "Request failed: $($_.Exception.Message)"
    }
}

function Test-GetStatistics {
    Test-Start "Get storage statistics (admin)"
    
    try {
        $headers = @{
            "Authorization" = "Bearer $script:Token"
        }
        
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/statistics" -Method Get -Headers $headers
        
        if ($response.code -eq 200) {
            Write-Success "Got statistics successfully"
            Write-Host "  Total files: $($response.data.total_files)" -ForegroundColor Gray
            Write-Host "  Total size: $($response.data.total_size_pretty)" -ForegroundColor Gray
        }
        else {
            Write-Failure "Get statistics failed: code=$($response.code)"
        }
    }
    catch {
        Write-Failure "Request failed: $($_.Exception.Message)"
    }
}

function Test-CleanupFiles {
    Test-Start "Manual cleanup trigger (admin)"
    
    try {
        $headers = @{
            "Authorization" = "Bearer $script:Token"
        }
        
        $body = @{
            dry_run = $true
            days_threshold = 30
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/cleanup" -Method Post -Headers $headers -ContentType "application/json" -Body $body
        
        if ($response.code -eq 200) {
            Write-Success "Cleanup task executed successfully (Dry Run)"
            Write-Host "  Files deleted: $($response.data.files_deleted)" -ForegroundColor Gray
            Write-Host "  Space freed: $($response.data.space_freed_pretty)" -ForegroundColor Gray
        }
        else {
            Write-Failure "Cleanup failed: code=$($response.code)"
        }
    }
    catch {
        Write-Failure "Request failed: $($_.Exception.Message)"
    }
}

function Test-DeleteFile {
    param([string]$FileId)
    
    Test-Start "Delete file (admin)"
    
    try {
        $headers = @{
            "Authorization" = "Bearer $script:Token"
        }
        
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/$FileId" -Method Delete -Headers $headers
        
        if ($response.code -eq 200) {
            Write-Success "File deleted successfully"
            Write-Host "  Message: $($response.message)" -ForegroundColor Gray
        }
        else {
            Write-Failure "Delete failed: code=$($response.code)"
        }
    }
    catch {
        Write-Failure "Request failed: $($_.Exception.Message)"
    }
}

function Test-ErrorScenario_NotFound {
    Test-Start "Get non-existent file (expect 404)"
    
    try {
        $fakeId = "00000000-0000-0000-0000-000000000000"
        $response = Invoke-RestMethod -Uri "$KONG_URL/api/files/$fakeId" -Method Get
        Write-Failure "Should return 404 but request succeeded"
    }
    catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 404) {
            Write-Success "Correctly returned 404"
        }
        else {
            Write-Failure "Returned wrong status code"
        }
    }
}

function Test-ErrorScenario_Unauthorized {
    Test-Start "Upload without auth (expect 401)"
    
    $tempImage = Join-Path $PSScriptRoot "temp-test-image.png"
    New-TestImage -Path $tempImage
    
    try {
        $boundary = [System.Guid]::NewGuid().ToString()
        $fileBytes = [System.IO.File]::ReadAllBytes($tempImage)
        
        $response = Invoke-WebRequest -Uri "$KONG_URL/api/files/upload" -Method Post -ContentType "multipart/form-data; boundary=$boundary" -Body $fileBytes
        
        Write-Failure "Should return 401 but request succeeded"
    }
    catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 401) {
            Write-Success "Correctly returned 401 unauthorized"
        }
        else {
            Write-Failure "Returned wrong status code: $($_.Exception.Response.StatusCode.value__)"
        }
    }
    finally {
        if (Test-Path $tempImage) {
            Remove-Item $tempImage -Force
        }
    }
}

# ============================================
# Main Test Flow
# ============================================

function Run-AllTests {
    Write-TestHeader "Open436 - M7 File Service Complete Test"
    Write-Host "Kong Gateway: $KONG_URL" -ForegroundColor Gray
    Write-Host "Test Account: $USERNAME" -ForegroundColor Gray
    Write-Host ""
    
    try {
        # 1. Login
        Test-Login
        
        Write-TestHeader "File Upload Tests"
        
        # 2. Upload test files
        $fileId1 = Test-UploadFile -FileType "avatar"
        $fileId2 = Test-UploadFile -FileType "post"
        $fileId3 = Test-UploadFile -FileType "section_icon"
        
        if (-not $fileId1) {
            throw "File upload failed, cannot continue testing"
        }
        
        Write-TestHeader "File Query Tests"
        
        # 3. Get file info
        Test-GetFileInfo -FileId $fileId1
        
        # 4. Get file URL
        Test-GetFileUrl -FileId $fileId1
        
        Write-TestHeader "File Status Management"
        
        # 5. Mark as used
        Test-MarkFileUsed -FileId $fileId1
        
        # 6. Mark as unused
        Test-MarkFileUnused -FileId $fileId1
        
        Write-TestHeader "Batch Operations"
        
        # 7. Batch get file info
        $fileIds = @($fileId1, $fileId2, $fileId3) | Where-Object { $_ }
        if ($fileIds.Count -gt 0) {
            Test-BatchGetFileInfo -FileIds $fileIds
        }
        
        Write-TestHeader "Admin Interface Tests"
        
        # 8. Get statistics
        Test-GetStatistics
        
        # 9. Manual cleanup
        Test-CleanupFiles
        
        # 10. Delete file
        if ($fileId1) {
            Test-DeleteFile -FileId $fileId1
        }
        
        Write-TestHeader "Error Scenario Tests"
        
        # 11. Error scenarios
        Test-ErrorScenario_NotFound
        Test-ErrorScenario_Unauthorized
        
    }
    catch {
        Write-Host ""
        Write-Host "[ERROR] Test interrupted: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test Report
    Write-TestHeader "Test Report"
    Write-Host "Total Tests: $script:TestTotal" -ForegroundColor White
    Write-Host "Passed: $script:TestPassed" -ForegroundColor Green
    Write-Host "Failed: $script:TestFailed" -ForegroundColor Red
    
    $successRate = if ($script:TestTotal -gt 0) { 
        [math]::Round(($script:TestPassed / $script:TestTotal) * 100, 2) 
    } else { 
        0 
    }
    Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } else { "Yellow" })
    
    Write-Host ""
    if ($script:TestFailed -eq 0) {
        Write-Host "All tests passed!" -ForegroundColor Green
    } else {
        Write-Host "Some tests failed, please check error messages" -ForegroundColor Yellow
    }
}

# Execute tests
Run-AllTests

