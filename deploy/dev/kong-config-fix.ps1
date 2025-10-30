# ============================================
# Kong Configuration Fix Script
# ============================================
# Purpose: Fix file-service routes to separate public and authenticated endpoints
# ============================================

$ErrorActionPreference = "Stop"

$KONG_ADMIN = "http://localhost:8001"
$HOST_ADDR = "host.docker.internal"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Fixing Kong Routes for File Service" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Wait for Kong to be ready
Write-Host "Checking Kong status..." -ForegroundColor Yellow
$retries = 0
$maxRetries = 10
while ($retries -lt $maxRetries) {
    try {
        $response = Invoke-RestMethod -Uri "$KONG_ADMIN/status" -Method Get -ErrorAction Stop
        Write-Host "Kong is ready!" -ForegroundColor Green
        break
    } catch {
        Write-Host "Kong not ready yet, waiting... ($retries/$maxRetries)" -ForegroundColor Gray
        Start-Sleep -Seconds 2
        $retries++
    }
}

if ($retries -eq $maxRetries) {
    Write-Host "ERROR: Kong did not become ready in time!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Get file-service ID
Write-Host "Getting file-service ID..." -ForegroundColor Yellow
$services = Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Get
$fileService = $services.data | Where-Object { $_.name -eq "file-service" }

if (-not $fileService) {
    Write-Host "ERROR: file-service not found!" -ForegroundColor Red
    exit 1
}

$serviceId = $fileService.id
Write-Host "Found file-service: $serviceId" -ForegroundColor Green
Write-Host ""

# Step 1: Delete existing routes
Write-Host "Step 1: Deleting existing file-service routes..." -ForegroundColor Cyan
$routes = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/routes" -Method Get
foreach ($route in $routes.data) {
    try {
        Invoke-RestMethod -Uri "$KONG_ADMIN/routes/$($route.id)" -Method Delete -ErrorAction SilentlyContinue
        Write-Host "  Deleted route: $($route.id)" -ForegroundColor Gray
    } catch {
        Write-Host "  Could not delete route: $($route.id)" -ForegroundColor Yellow
    }
}
Write-Host "Existing routes deleted" -ForegroundColor Green
Write-Host ""

# Step 2: Create public routes (no authentication)
Write-Host "Step 2: Creating public routes (no auth)..." -ForegroundColor Cyan

# Public route 1: GET file info using regex (higher priority)
$publicRoute1Body = @{
    name = "file-service-public-get"
    protocols = @("http", "https")
    methods = @("GET")
    paths = @("~/api/files/[a-f0-9-]+(/url)?$")
    strip_path = $false
    regex_priority = 100
} | ConvertTo-Json

try {
    $publicRoute1 = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/routes" -Method Post -ContentType "application/json" -Body $publicRoute1Body
    Write-Host "  Created public GET route" -ForegroundColor Green
} catch {
    Write-Host "  ERROR creating public GET route: $_" -ForegroundColor Red
}

# Public route 2: POST batch-info
$publicRoute2Body = @{
    name = "file-service-public-batch"
    protocols = @("http", "https")
    methods = @("POST")
    paths = @("/api/files/batch-info")
    strip_path = $false
    regex_priority = 100
} | ConvertTo-Json

try {
    $publicRoute2 = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/routes" -Method Post -ContentType "application/json" -Body $publicRoute2Body
    Write-Host "  Created public batch-info route" -ForegroundColor Green
} catch {
    Write-Host "  ERROR creating batch-info route: $_" -ForegroundColor Red
}

Write-Host ""

# Step 3: Create admin routes (highest priority)
Write-Host "Step 3: Creating admin routes (high priority)..." -ForegroundColor Cyan

# Admin route 1: Statistics
$adminRoute1Body = @{
    name = "file-service-admin-statistics"
    protocols = @("http", "https")
    methods = @("GET")
    paths = @("/api/files/statistics")
    strip_path = $false
    regex_priority = 200
} | ConvertTo-Json

try {
    $adminRoute1 = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/routes" -Method Post -ContentType "application/json" -Body $adminRoute1Body
    Write-Host "  Created admin statistics route" -ForegroundColor Green
    
    $pluginBody = @{
        name = "satoken-auth"
        config = @{
            auth_service_url = "http://$HOST_ADDR:8081"
        }
    } | ConvertTo-Json -Depth 5
    
    $plugin = Invoke-RestMethod -Uri "$KONG_ADMIN/routes/$($adminRoute1.id)/plugins" -Method Post -ContentType "application/json" -Body $pluginBody
    Write-Host "  Enabled satoken-auth on statistics route" -ForegroundColor Green
} catch {
    Write-Host "  ERROR creating statistics route: $_" -ForegroundColor Red
}

# Admin route 2: Cleanup
$adminRoute2Body = @{
    name = "file-service-admin-cleanup"
    protocols = @("http", "https")
    methods = @("POST")
    paths = @("/api/files/cleanup")
    strip_path = $false
    regex_priority = 200
} | ConvertTo-Json

try {
    $adminRoute2 = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/routes" -Method Post -ContentType "application/json" -Body $adminRoute2Body
    Write-Host "  Created admin cleanup route" -ForegroundColor Green
    
    $pluginBody = @{
        name = "satoken-auth"
        config = @{
            auth_service_url = "http://$HOST_ADDR:8081"
        }
    } | ConvertTo-Json -Depth 5
    
    $plugin = Invoke-RestMethod -Uri "$KONG_ADMIN/routes/$($adminRoute2.id)/plugins" -Method Post -ContentType "application/json" -Body $pluginBody
    Write-Host "  Enabled satoken-auth on cleanup route" -ForegroundColor Green
} catch {
    Write-Host "  ERROR creating cleanup route: $_" -ForegroundColor Red
}

Write-Host ""

# Step 4: Create authenticated routes
Write-Host "Step 4: Creating authenticated routes..." -ForegroundColor Cyan

# Authenticated route: All other file operations
$authRouteBody = @{
    name = "file-service-auth"
    protocols = @("http", "https")
    paths = @("/api/files")
    strip_path = $false
    regex_priority = 0
} | ConvertTo-Json

try {
    $authRoute = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/routes" -Method Post -ContentType "application/json" -Body $authRouteBody
    Write-Host "  Created authenticated route" -ForegroundColor Green
    
    # Enable satoken-auth plugin on this route
    $authRouteId = $authRoute.id
    $pluginBody = @{
        name = "satoken-auth"
        config = @{
            auth_service_url = "http://$HOST_ADDR:8081"
        }
    } | ConvertTo-Json -Depth 5
    
    try {
        $plugin = Invoke-RestMethod -Uri "$KONG_ADMIN/routes/$authRouteId/plugins" -Method Post -ContentType "application/json" -Body $pluginBody
        Write-Host "  Enabled satoken-auth on authenticated route" -ForegroundColor Green
    } catch {
        Write-Host "  ERROR enabling plugin: $_" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ERROR creating authenticated route: $_" -ForegroundColor Red
}

Write-Host ""

# Step 5: Remove service-level plugin (if exists)
Write-Host "Step 5: Removing service-level plugin..." -ForegroundColor Cyan
$plugins = Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/plugins" -Method Get
foreach ($plugin in $plugins.data) {
    if ($plugin.name -eq "satoken-auth") {
        try {
            Invoke-RestMethod -Uri "$KONG_ADMIN/plugins/$($plugin.id)" -Method Delete
            Write-Host "  Removed service-level satoken-auth plugin" -ForegroundColor Green
        } catch {
            Write-Host "  Could not remove plugin: $_" -ForegroundColor Yellow
        }
    }
}

Write-Host ""

# Verify configuration
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Configuration Complete!" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Public endpoints (no auth):" -ForegroundColor Green
Write-Host "  GET  /api/files/:id" -ForegroundColor Gray
Write-Host "  GET  /api/files/:id/url" -ForegroundColor Gray
Write-Host "  POST /api/files/batch-info" -ForegroundColor Gray
Write-Host ""
Write-Host "Authenticated endpoints:" -ForegroundColor Yellow
Write-Host "  POST   /api/files/upload" -ForegroundColor Gray
Write-Host "  POST   /api/files/:id/mark-used" -ForegroundColor Gray
Write-Host "  POST   /api/files/:id/mark-unused" -ForegroundColor Gray
Write-Host "  DELETE /api/files/:id" -ForegroundColor Gray
Write-Host "  GET    /api/files/statistics" -ForegroundColor Gray
Write-Host "  POST   /api/files/cleanup" -ForegroundColor Gray
Write-Host ""
Write-Host "Verification commands:" -ForegroundColor Cyan
Write-Host "  curl http://localhost:8001/services/file-service/routes" -ForegroundColor Gray
Write-Host "  curl http://localhost:8000/api/files/batch-info -X POST -d '{...}'" -ForegroundColor Gray
Write-Host ""

