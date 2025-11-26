# Microservices Docker Management Script for Windows
# Usage: .\docker-manage.ps1 [command]

param(
    [Parameter(Mandatory=$false)]
    [string]$Command,
    [Parameter(Mandatory=$false)]
    [string]$Service
)

$ComposeFile = "docker-compose.yml"
$ProjectName = "microservices-clinic"

# Functions
function Print-Header {
    param([string]$Text)
    Write-Host "=================================================="  -ForegroundColor Blue
    Write-Host $Text -ForegroundColor Blue
    Write-Host "=================================================="  -ForegroundColor Blue
}

function Print-Success {
    param([string]$Text)
    Write-Host "✓ $Text" -ForegroundColor Green
}

function Print-Error {
    param([string]$Text)
    Write-Host "✗ $Text" -ForegroundColor Red
}

function Print-Warning {
    param([string]$Text)
    Write-Host "⚠ $Text" -ForegroundColor Yellow
}

function Print-Info {
    param([string]$Text)
    Write-Host "ℹ $Text" -ForegroundColor Cyan
}

function Build-Images {
    Print-Header "Building Docker Images"
    docker-compose -p $ProjectName build
    if ($LASTEXITCODE -eq 0) {
        Print-Success "Images built successfully"
    } else {
        Print-Error "Build failed"
    }
}

function Start-Services {
    Print-Header "Starting Services"
    docker-compose -p $ProjectName up -d
    
    Print-Info "Waiting for services to be healthy..."
    Start-Sleep -Seconds 5
    
    Check-Health
}

function Stop-Services {
    Print-Header "Stopping Services"
    docker-compose -p $ProjectName down
    Print-Success "Services stopped"
}

function Restart-Services {
    Print-Header "Restarting Services"
    docker-compose -p $ProjectName restart
    Print-Success "Services restarted"
}

function Show-Status {
    Print-Header "Service Status"
    docker-compose -p $ProjectName ps
}

function Show-Logs {
    param([string]$ServiceName = "")
    
    if ([string]::IsNullOrEmpty($ServiceName)) {
        Print-Header "Tailing All Logs"
        docker-compose -p $ProjectName logs -f
    } else {
        Print-Header "Logs for $ServiceName"
        docker-compose -p $ProjectName logs -f $ServiceName
    }
}

function Check-Health {
    Print-Header "Checking Service Health"
    
    $services = @("eureka-server", "api-gateway", "user-service", "keycloak")
    
    foreach ($service in $services) {
        $status = docker-compose -p $ProjectName ps $service 2>&1
        if ($status -like "*running*") {
            Print-Success "$service is running"
        } else {
            Print-Error "$service is not running"
        }
    }
    
    # Check HTTP endpoints
    Print-Info "Checking HTTP endpoints..."
    
    $endpoints = @(
        @{url="http://localhost:8761/eureka/apps"; name="Eureka"},
        @{url="http://localhost:8080/actuator/health"; name="Gateway"},
        @{url="http://localhost:8081/actuator/health"; name="User Service"},
        @{url="http://localhost:8082/health/ready"; name="Keycloak"}
    )
    
    foreach ($endpoint in $endpoints) {
        try {
            $response = Invoke-WebRequest -Uri $endpoint.url -TimeoutSec 3 -ErrorAction Stop
            Print-Success "$($endpoint.name) is responding"
        } catch {
            Print-Warning "$($endpoint.name) is not responding yet"
        }
    }
}

function Clean-Up {
    Print-Header "Cleaning Up"
    $response = Read-Host "Are you sure you want to remove all containers and volumes? (y/n)"
    
    if ($response -eq 'y' -or $response -eq 'Y') {
        docker-compose -p $ProjectName down -v
        Print-Success "Cleanup complete"
    } else {
        Print-Info "Cleanup cancelled"
    }
}

function Build-And-Start {
    Print-Header "Full Build and Start"
    Build-Images
    Start-Services
    Check-Health
}

function Test-Endpoints {
    Print-Header "Testing Endpoints"
    
    Print-Info "Testing Eureka..."
    try {
        $eureka = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps" -ErrorAction Stop
        $eureka.Content | ConvertFrom-Xml | Out-Host
    } catch {
        Print-Error "Eureka failed: $_"
    }
    
    Print-Info "Testing User Service..."
    try {
        $userService = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -ErrorAction Stop
        $userService.Content | ConvertFrom-Json | Out-Host
    } catch {
        Print-Error "User Service failed: $_"
    }
    
    Print-Info "Testing Gateway..."
    try {
        $gateway = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -ErrorAction Stop
        $gateway.Content | ConvertFrom-Json | Out-Host
    } catch {
        Print-Error "Gateway failed: $_"
    }
    
    Print-Info "Testing Keycloak..."
    try {
        $keycloak = Invoke-WebRequest -Uri "http://localhost:8082/health/ready" -ErrorAction Stop
        $keycloak.Content | ConvertFrom-Json | Out-Host
    } catch {
        Print-Error "Keycloak failed: $_"
    }
}

function Open-Database {
    Print-Header "Opening User Service Database Shell"
    docker exec -it user-service-db psql -U clinic_user -d clinic
}

function Print-Usage {
    Write-Host "Microservices Docker Management Script for Windows" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\docker-manage.ps1 [command] [options]" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  build              Build Docker images"
    Write-Host "  start              Start all services"
    Write-Host "  stop               Stop all services"
    Write-Host "  restart            Restart all services"
    Write-Host "  status             Show service status"
    Write-Host "  logs               Show logs from all services"
    Write-Host "  logs <service>     Show logs from specific service"
    Write-Host "  health             Check service health"
    Write-Host "  clean              Remove all containers and volumes"
    Write-Host "  build-start        Build and start all services"
    Write-Host "  test               Test all endpoints"
    Write-Host "  db                 Open database shell"
    Write-Host "  help               Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\docker-manage.ps1 build-start"
    Write-Host "  .\docker-manage.ps1 logs user-service"
    Write-Host "  .\docker-manage.ps1 health"
    Write-Host ""
}

# Main
switch ($Command.ToLower()) {
    "build" { Build-Images }
    "start" { Start-Services }
    "stop" { Stop-Services }
    "restart" { Restart-Services }
    "status" { Show-Status }
    "logs" { Show-Logs $Service }
    "health" { Check-Health }
    "clean" { Clean-Up }
    "build-start" { Build-And-Start }
    "test" { Test-Endpoints }
    "db" { Open-Database }
    "help" { Print-Usage }
    default {
        if ([string]::IsNullOrEmpty($Command)) {
            Print-Usage
        } else {
            Print-Error "Unknown command: $Command"
            Write-Host ""
            Print-Usage
        }
    }
}
