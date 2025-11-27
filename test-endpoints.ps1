# Script para probar todos los endpoints del sistema
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "   🧪 PRUEBA COMPLETA DE ENDPOINTS" -ForegroundColor Magenta  
Write-Host "========================================`n" -ForegroundColor Magenta

# 1. LOGIN
Write-Host "=== 1️⃣  LOGIN con Keycloak (test/test) ===" -ForegroundColor Cyan
$loginData = @{
    client_id = 'clinic-frontend'
    client_secret = 'gS3x7bDRltiBipTa28467qm3cESJQn9R'
    username = 'test'
    password = 'test'
    grant_type = 'password'
}

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/realms/clinic/protocol/openid-connect/token" -Method Post -Body $loginData -ContentType "application/x-www-form-urlencoded"
    $token = $response.access_token
    Write-Host "✅ Token obtenido exitosamente" -ForegroundColor Green
    Write-Host "Token (primeros 80 chars): $($token.Substring(0, 80))...`n" -ForegroundColor DarkGray
} catch {
    Write-Host "❌ Error en login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    Authorization = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. USER SERVICE - GET /me
Write-Host "=== 2️⃣  USER SERVICE - GET /api/users/me ===" -ForegroundColor Cyan
try {
    $meResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/users/me" -Method Get -Headers $headers
    Write-Host "✅ Usuario verificado/creado:" -ForegroundColor Green
    $meResponse | ConvertTo-Json | Write-Host -ForegroundColor Yellow
    $userId = $meResponse.id
    Write-Host "`nUser ID guardado: $userId`n" -ForegroundColor DarkGray
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Detalles: $($_.ErrorDetails.Message)`n" -ForegroundColor Red
}

# 3. USER SERVICE - GET all users
Write-Host "=== 3️⃣  USER SERVICE - GET /api/users (todos los usuarios) ===" -ForegroundColor Cyan
try {
    $usersResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Get -Headers $headers
    Write-Host "✅ Usuarios obtenidos: $($usersResponse.Count) usuario(s)" -ForegroundColor Green
    $usersResponse | ForEach-Object { 
        Write-Host "  - ID: $($_.id), Nombre: $($_.nombre) $($_.apellido), Email: $($_.correo), Role: $($_.role)" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)`n" -ForegroundColor Red
}

# 4. USER SERVICE - CREATE new user
Write-Host "=== 4️⃣  USER SERVICE - POST /api/users (crear usuario) ===" -ForegroundColor Cyan
$newUser = @{
    nombre = "Juan"
    apellido = "Pérez"
    correo = "juan.perez@clinic.com"
    telefono = "+52 123 456 7890"
    role = "DOCTOR"
} | ConvertTo-Json

try {
    $createUserResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Post -Headers $headers -Body $newUser
    Write-Host "✅ Usuario creado:" -ForegroundColor Green
    $createUserResponse | ConvertTo-Json | Write-Host -ForegroundColor Yellow
    $doctorId = $createUserResponse.id
    Write-Host "`nDoctor ID guardado: $doctorId`n" -ForegroundColor DarkGray
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)`n" -ForegroundColor Red
    $doctorId = "1" # Usar un ID por defecto si falla
}

# 5. APPOINTMENT SERVICE - GET all appointments
Write-Host "=== 5️⃣  APPOINTMENT SERVICE - GET /api/appointments ===" -ForegroundColor Cyan
try {
    $appointmentsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/appointments" -Method Get -Headers $headers
    Write-Host "✅ Citas obtenidas: $($appointmentsResponse.Count) cita(s)" -ForegroundColor Green
    if ($appointmentsResponse.Count -gt 0) {
        $appointmentsResponse | ForEach-Object {
            Write-Host "  - ID: $($_.id), Patient: $($_.patientName), Doctor: $($_.doctorName), Date: $($_.appointmentDate)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  (No hay citas aún)" -ForegroundColor DarkGray
    }
    Write-Host ""
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)`n" -ForegroundColor Red
}

# 6. APPOINTMENT SERVICE - CREATE appointment
Write-Host "=== 6️⃣  APPOINTMENT SERVICE - POST /api/appointments ===" -ForegroundColor Cyan
$newAppointment = @{
    patientId = if ($userId) { $userId } else { "test-user" }
    patientName = "Test User"
    doctorId = if ($doctorId) { $doctorId } else { "doctor-1" }
    doctorName = "Dr. Juan Pérez"
    specialty = "Cardiología"
    appointmentDate = (Get-Date).AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss")
    description = "Consulta de control anual"
} | ConvertTo-Json

try {
    $createAppointmentResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/appointments" -Method Post -Headers $headers -Body $newAppointment
    Write-Host "✅ Cita creada:" -ForegroundColor Green
    $createAppointmentResponse | ConvertTo-Json | Write-Host -ForegroundColor Yellow
    Write-Host ""
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)`n" -ForegroundColor Red
}

# 7. IMAGE SERVICE - GET all images
Write-Host "=== 7️⃣  IMAGE SERVICE - GET /api/images ===" -ForegroundColor Cyan
try {
    $imagesResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/images" -Method Get -Headers $headers
    Write-Host "✅ Imágenes obtenidas: $($imagesResponse.Count) imagen(es)" -ForegroundColor Green
    if ($imagesResponse.Count -gt 0) {
        $imagesResponse | ForEach-Object {
            Write-Host "  - ID: $($_.id), Name: $($_.name), Type: $($_.contentType)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  (No hay imágenes aún)" -ForegroundColor DarkGray
    }
    Write-Host ""
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)`n" -ForegroundColor Red
}

# 8. GATEWAY HEALTH CHECK
Write-Host "=== 8️⃣  API GATEWAY - Health Check ===" -ForegroundColor Cyan
try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
    Write-Host "✅ Gateway Status: $($healthResponse.status)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)`n" -ForegroundColor Red
}

Write-Host "========================================" -ForegroundColor Magenta
Write-Host "   ✅ PRUEBAS COMPLETADAS" -ForegroundColor Magenta  
Write-Host "========================================`n" -ForegroundColor Magenta
