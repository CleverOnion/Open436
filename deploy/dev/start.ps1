# 开发环境启动脚本 (PowerShell，deploy/dev)
# 仅启动 Docker 基础设施，不启动后端服务

$ErrorActionPreference = "Stop"

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $SCRIPT_DIR

# 端口占用检查
$tcpPorts = @(8500, 5432, 5433, 6379, 9000, 9001, 8000, 8443, 8001)
$udpPorts = @(8600)

function Test-TcpPortInUse {
    param([int]$Port)
    if (Get-Command Get-NetTCPConnection -ErrorAction SilentlyContinue) {
        $conns = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
        return $null -ne $conns
    } else {
        $out = netstat -ano | Select-String -SimpleMatch ":$Port" | Where-Object { $_ -match "LISTEN" }
        return $null -ne $out
    }
}

function Test-UdpPortInUse {
    param([int]$Port)
    if (Get-Command Get-NetUDPEndpoint -ErrorAction SilentlyContinue) {
        $eps = Get-NetUDPEndpoint -LocalPort $Port -ErrorAction SilentlyContinue
        return $null -ne $eps
    } else {
        $out = netstat -ano -p udp | Select-String -SimpleMatch ":$Port"
        return $null -ne $out
    }
}

$conflicts = @()
foreach ($p in $tcpPorts) { if (Test-TcpPortInUse -Port $p) { $conflicts += "TCP:$p" } }
foreach ($p in $udpPorts) { if (Test-UdpPortInUse -Port $p) { $conflicts += "UDP:$p" } }

if ($conflicts.Count -gt 0) {
    Write-Host "检测到端口被占用，请释放以下端口后重试:" -ForegroundColor Red
    foreach ($c in $conflicts) { Write-Host "  $c" -ForegroundColor Red }
    Write-Host "脚本已终止以避免冲突。" -ForegroundColor Yellow
    exit 1
}

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Starting Open436 Development Environment" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This will start:" -ForegroundColor Yellow
Write-Host "  - Consul (Service Registry)"
Write-Host "  - Kong Gateway (API Gateway)"
Write-Host "  - PostgreSQL (Database)"
Write-Host "  - Redis (Cache)"
Write-Host "  - Minio (Object Storage)"
Write-Host ""
Write-Host "Backend services (M1, M7) should be run in your IDE" -ForegroundColor Yellow
Write-Host ""

# 1. 启动基础设施
Write-Host "Step 1: Starting infrastructure services..." -ForegroundColor Cyan
docker-compose -f ./docker-compose.yml up -d consul postgres redis minio kong-database

# 等待数据库就绪
Write-Host "Waiting for databases to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 2. 启动 Kong
Write-Host "Step 2: Starting Kong Gateway..." -ForegroundColor Cyan
docker-compose -f ./docker-compose.yml up -d kong

# 等待 Kong 就绪
Write-Host "Waiting for Kong to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

# 3. 配置 Kong
Write-Host "Step 3: Configuring Kong routes and plugins..." -ForegroundColor Cyan
& .\kong-config.ps1

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Development Environment Ready!" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Services Status:" -ForegroundColor Yellow
docker-compose -f ./docker-compose.yml ps
Write-Host ""
Write-Host "Access Points:" -ForegroundColor Yellow
Write-Host "  - Consul UI:       http://localhost:8500"
Write-Host "  - Kong Admin API:  http://localhost:8001"
Write-Host "  - Kong Proxy:      http://localhost:8000"
Write-Host "  - Minio Console:   http://localhost:9001 (minioadmin/minioadmin)"
Write-Host "  - PostgreSQL:      localhost:5432 (open436/open436)"
Write-Host "  - Redis:           localhost:6379"
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Start M1 Auth Service in IntelliJ IDEA (port 8081)"
Write-Host "  2. Start M7 File Service in VS Code/Terminal (port 8007)"
Write-Host "  3. Check Consul UI to verify services are registered"
Write-Host "  4. Test login through Kong"
Write-Host ""
Write-Host "To stop: docker-compose -f ./docker-compose.yml down" -ForegroundColor Gray
Write-Host ""


