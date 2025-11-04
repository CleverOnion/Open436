# Post Creation Test Script
# Based on PRD/M3-Content Management Module.md lines 624-634

$baseUrl = "http://localhost:8003"
$apiUrl = "$baseUrl/api/content/posts"
$testResults = @()

# Color output function
function Write-TestResult {
    param(
        [string]$TestName,
        [string]$Status,
        [string]$Message
    )
    
    $color = if ($Status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "[$Status] $TestName" -ForegroundColor $color
    if ($Message) {
        Write-Host "  $Message" -ForegroundColor Gray
    }
}

# Test function
function Test-PostCreation {
    param(
        [string]$TestName,
        [string]$TestCode,
        [object]$RequestBody,
        [int]$ExpectedStatus,
        [string]$ExpectedMessage = ""
    )
    
    Write-Host "`nTesting: $TestName" -ForegroundColor Cyan
    
    try {
        $jsonBody = if ($RequestBody -is [string]) { $RequestBody } else { $RequestBody | ConvertTo-Json -Depth 10 }
        $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Body $jsonBody -ContentType "application/json" -Headers @{"X-User-Id" = "1"} -ErrorAction Stop
        
        if ($ExpectedStatus -eq 200) {
            Write-TestResult $TestCode "PASS" "Post created successfully, ID: $($response.data)"
            $script:testResults += @{Test = $TestCode; Status = "PASS"; Message = "Success"}
        } else {
            Write-TestResult $TestCode "FAIL" "Expected status $ExpectedStatus, but got 200"
            $script:testResults += @{Test = $TestCode; Status = "FAIL"; Message = "Status code mismatch"}
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message | ConvertFrom-Json
        
        if ($statusCode -eq $ExpectedStatus) {
            $message = $errorBody.message
            if ($ExpectedMessage -and $message -like "*$ExpectedMessage*") {
                Write-TestResult $TestCode "PASS" "Error message: $message"
                $script:testResults += @{Test = $TestCode; Status = "PASS"; Message = $message}
            } elseif (-not $ExpectedMessage) {
                Write-TestResult $TestCode "PASS" "Error message: $message"
                $script:testResults += @{Test = $TestCode; Status = "PASS"; Message = $message}
            } else {
                Write-TestResult $TestCode "FAIL" "Expected message contains '$ExpectedMessage', but got: $message"
                $script:testResults += @{Test = $TestCode; Status = "FAIL"; Message = $message}
            }
        } else {
            Write-TestResult $TestCode "FAIL" "Expected status $ExpectedStatus, but got $statusCode"
            $script:testResults += @{Test = $TestCode; Status = "FAIL"; Message = "Status code: $statusCode"}
        }
    }
}

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Post Creation Test Scenarios (TC-001 ~ TC-009)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Test API: $apiUrl`n" -ForegroundColor Gray

# TC-001: Normal post creation
Test-PostCreation -TestName "TC-001: Normal Creation" -TestCode "TC-001" `
    -RequestBody @{
        title = "This is a test post title"
        content = "This is the post content, must be at least 10 characters long"
        boardId = 1
    } `
    -ExpectedStatus 200

# TC-002: Empty title
Test-PostCreation -TestName "TC-002: Empty Title" -TestCode "TC-002" `
    -RequestBody @{
        title = ""
        content = "This is the post content, must be at least 10 characters long"
        boardId = 1
    } `
    -ExpectedStatus 400 `
    -ExpectedMessage "标题不能为空"

# TC-003: Title too short (4 characters)
Test-PostCreation -TestName "TC-003: Title Too Short" -TestCode "TC-003" `
    -RequestBody @{
        title = "Test"
        content = "This is the post content, must be at least 10 characters long"
        boardId = 1
    } `
    -ExpectedStatus 400 `
    -ExpectedMessage "标题长度必须在5-100个字符之间"

# TC-004: Title too long (101 characters)
$longTitle = "a" * 101
Test-PostCreation -TestName "TC-004: Title Too Long" -TestCode "TC-004" `
    -RequestBody @{
        title = $longTitle
        content = "This is the post content, must be at least 10 characters long"
        boardId = 1
    } `
    -ExpectedStatus 400 `
    -ExpectedMessage "标题长度必须在5-100个字符之间"

# TC-005: Empty content
Test-PostCreation -TestName "TC-005: Empty Content" -TestCode "TC-005" `
    -RequestBody @{
        title = "This is a test post title"
        content = ""
        boardId = 1
    } `
    -ExpectedStatus 400 `
    -ExpectedMessage "内容不能为空"

# TC-006: Content too short (9 characters)
Test-PostCreation -TestName "TC-006: Content Too Short" -TestCode "TC-006" `
    -RequestBody @{
        title = "This is a test post title"
        content = "Short"
        boardId = 1
    } `
    -ExpectedStatus 400 `
    -ExpectedMessage "内容长度必须在10-50000个字符之间"

# TC-007: Missing boardId
$bodyWithoutBoardJson = @{
    title = "This is a test post title"
    content = "This is the post content, must be at least 10 characters long"
} | ConvertTo-Json
Test-PostCreation -TestName "TC-007: Missing BoardId" -TestCode "TC-007" `
    -RequestBody $bodyWithoutBoardJson `
    -ExpectedStatus 400 `
    -ExpectedMessage "板块ID不能为空"

# TC-008: Upload image (skip - needs file service API)
Write-Host "`nTest: TC-008: Upload Image" -ForegroundColor Cyan
Write-TestResult "TC-008" "SKIP" "Image upload requires separate file service API (/api/files/upload) test"
$script:testResults += @{Test = "TC-008"; Status = "SKIP"; Message = "Requires file service"}

# TC-009: Image too large (skip - needs file service API)
Write-Host "`nTest: TC-009: Image Too Large" -ForegroundColor Cyan
Write-TestResult "TC-009" "SKIP" "Image upload requires separate file service API (/api/files/upload) test"
$script:testResults += @{Test = "TC-009"; Status = "SKIP"; Message = "Requires file service"}

# Output test summary
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "Test Summary" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

$passed = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failed = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$skipped = ($testResults | Where-Object { $_.Status -eq "SKIP" }).Count

Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "Skipped: $skipped" -ForegroundColor Yellow
Write-Host "Total: $($passed + $failed + $skipped)`n" -ForegroundColor Cyan

if ($failed -eq 0) {
    Write-Host "All tests passed! ✓" -ForegroundColor Green
    exit 0
} else {
    Write-Host "Some tests failed, please check the error messages above." -ForegroundColor Red
    exit 1
}
