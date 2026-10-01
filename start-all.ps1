# ==============================================================================
# TRAVEL MANAGEMENT PLATFORM - ONE-CLICK ALL-IN-ONE LAUNCHER
# ==============================================================================
# Leng duy nhat de khoi chay TOAN BO He Thong Backend Microservices & Frontend
# ==============================================================================

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " KHOI CHAY TRON GOI: BACKEND MICROSERVICES + DOCKER INFRA + FRONTEND " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$ROOT_DIR = $PSScriptRoot

# Load environment variables from .env
$envFile = Join-Path $ROOT_DIR ".env"
if (Test-Path $envFile) {
    Write-Host " Dang nap bien moi truong tu file .env..." -ForegroundColor Gray
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $key = $parts[0].Trim()
            $value = $parts[1].Trim()
            [System.Environment]::SetEnvironmentVariable($key, $value, [System.EnvironmentVariableTarget]::Process)
        }
    }
}

# 1. Start Infrastructure Docker Containers
Write-Host "`n[1/4] Dang khoi chay Ha Tang Docker (PostgreSQL pgvector, Redis, Kafka, Kafka UI)..." -ForegroundColor Yellow
Set-Location -Path $ROOT_DIR
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host " Docker-compose gap loi. Vui long kiem tra Docker Desktop!" -ForegroundColor Red
    exit 1
}

# 2. Build Java Microservices
Write-Host "`n[2/4] Dang bien dich va dong goi tat ca Java Microservices (Maven)..." -ForegroundColor Yellow
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host " Bien dich Maven gap loi. Vui long kiem tra code Java!" -ForegroundColor Red
    exit 1
}

# 3. Launch 6 Java Microservices in background processes
Write-Host "`n[3/4] Dang khoi chay 6 Backend Microservices..." -ForegroundColor Yellow

$services = @(
    @{ Name = "API Gateway (Port 8080)";     Jar = "api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar" },
    @{ Name = "Auth Service (Port 8081)";    Jar = "auth-service\target\auth-service-1.0.0-SNAPSHOT.jar" },
    @{ Name = "Tour Service (Port 8082)";    Jar = "tour-service\target\tour-service-1.0.0-SNAPSHOT.jar" },
    @{ Name = "Booking Service (Port 8083)"; Jar = "booking-service\target\booking-service-1.0.0-SNAPSHOT.jar" },
    @{ Name = "Payment Service (Port 8084)"; Jar = "payment-service\target\payment-service-1.0.0-SNAPSHOT.jar" },
    @{ Name = "AI Service (Port 8085)";      Jar = "ai-service\target\ai-service-1.0.0-SNAPSHOT.jar" }
)

foreach ($s in $services) {
    $jarPath = Join-Path $ROOT_DIR $s.Jar
    if (Test-Path $jarPath) {
        Write-Host " -> Dang khoi chay $($s.Name)..." -ForegroundColor Green
        Start-Process -FilePath "java" -ArgumentList "-jar", "`"$jarPath`"" -WindowStyle Minimized
    } else {
        Write-Host " Khong tim thay file Jar: $($s.Jar)" -ForegroundColor Red
    }
}

# 4. Start Next.js Frontend
Write-Host "`n[4/4] Dang khoi chay Next.js Frontend Website (Port 3000)..." -ForegroundColor Yellow
$frontendDir = Join-Path $ROOT_DIR "travel-frontend"
Start-Process -FilePath "powershell" -ArgumentList "-NoExit", "-Command", "Set-Location '$frontendDir'; npm run dev"

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " THANH CONG! TOAN BO HE THONG DA DUOC KHOI CHAY!" -ForegroundColor Cyan
Write-Host " Website Frontend:   http://localhost:3000" -ForegroundColor White
Write-Host " API Gateway:        http://localhost:8080" -ForegroundColor White
Write-Host " Kafka UI:           http://localhost:8088" -ForegroundColor White
Write-Host "======================================================================" -ForegroundColor Cyan
