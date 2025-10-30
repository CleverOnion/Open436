# Kong 开发环境配置脚本 (PowerShell，deploy/dev)
# 适用于 Windows PowerShell

$ErrorActionPreference = "Stop"

$KONG_ADMIN = "http://localhost:8001"
$HOST_ADDR = "host.docker.internal"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Configuring Kong for Development Mode" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend services running on host: $HOST_ADDR" -ForegroundColor Yellow
Write-Host ""

# 等待 Kong 就绪
Write-Host "Waiting for Kong to be ready..." -ForegroundColor Yellow
$retries = 0
$maxRetries = 30
while ($retries -lt $maxRetries) {
    try {
        $response = Invoke-RestMethod -Uri "$KONG_ADMIN/status" -Method Get -ErrorAction Stop
        Write-Host "Kong is ready!" -ForegroundColor Green
        break
    } catch {
        Write-Host "Kong is not ready yet, waiting... ($retries/$maxRetries)" -ForegroundColor Gray
        Start-Sleep -Seconds 2
        $retries++
    }
}

if ($retries -eq $maxRetries) {
    Write-Host "ERROR: Kong did not become ready in time!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 删除已存在的配置（如果有）
Write-Host "Cleaning up existing configurations..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$KONG_ADMIN/services/auth-service" -Method Delete -ErrorAction SilentlyContinue
    Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service" -Method Delete -ErrorAction SilentlyContinue
} catch {
    # 忽略错误
}
Start-Sleep -Seconds 1

# 1. 创建 M1 认证服务
Write-Host "Step 1: Creating auth-service..." -ForegroundColor Cyan
try {
    $authService = Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Post -Body @{
        name = "auth-service"
        url = "http://$HOST_ADDR:8081"
    }
    Write-Host "✓ auth-service created" -ForegroundColor Green

    $authRoute = Invoke-RestMethod -Uri "$KONG_ADMIN/services/auth-service/routes" -Method Post -Body @{
        "paths[]" = "/api/auth"
        strip_path = $false
    }
    Write-Host "✓ auth-service route created" -ForegroundColor Green
} catch {
    Write-Host "ERROR creating auth-service: $_" -ForegroundColor Red
}

Write-Host ""

# 2. 创建 M7 文件服务
Write-Host "Step 2: Creating file-service..." -ForegroundColor Cyan
try {
    $fileService = Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Post -Body @{
        name = "file-service"
        url = "http://$HOST_ADDR:8007"
    }
    Write-Host "✓ file-service created" -ForegroundColor Green

    $fileRoute = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/routes" -Method Post -Body @{
        "paths[]" = "/api/files"
        strip_path = $false
    }
    Write-Host "✓ file-service route created" -ForegroundColor Green
} catch {
    Write-Host "ERROR creating file-service: $_" -ForegroundColor Red
}

Write-Host ""

# 3. 启用 Sa-Token 认证插件
Write-Host "Step 3: Enabling satoken-auth plugin..." -ForegroundColor Cyan
try {
    $plugin = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/plugins" -Method Post -Body @{
        name = "satoken-auth"
        "config.auth_service_url" = "http://$HOST_ADDR:8081"
    }
    Write-Host "✓ satoken-auth plugin enabled" -ForegroundColor Green
} catch {
    Write-Host "ERROR enabling plugin: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Kong Configuration Complete!" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Services registered:" -ForegroundColor Yellow
Write-Host "  - auth-service:  http://$HOST_ADDR:8081 → http://localhost:8000/api/auth/*"
Write-Host "  - file-service:  http://$HOST_ADDR:8007 → http://localhost:8000/api/files/*"
Write-Host ""
Write-Host "Verification:" -ForegroundColor Yellow
Write-Host "  - Kong services: curl http://localhost:8001/services"
Write-Host "  - Kong routes:   curl http://localhost:8001/routes"
Write-Host ""
Write-Host "Test login through Kong:" -ForegroundColor Yellow
Write-Host '  Invoke-RestMethod -Uri "http://localhost:8000/api/auth/login" `' -ForegroundColor Gray
Write-Host '    -Method Post `' -ForegroundColor Gray
Write-Host '    -ContentType "application/json" `' -ForegroundColor Gray
Write-Host "    -Body '{\"username\":\"alice\",\"password\":\"password123\"}'" -ForegroundColor Gray
Write-Host ""


