# 🏥 Frontend - Sistema de Microservicios Clínica

## 📋 Resumen

Frontend web interactivo para interactuar con todos los microservicios del sistema de gestión de clínicas.

## 🚀 Acceso

El frontend está disponible en: **http://localhost:3000**

## 🔐 Autenticación

### Usuario de Prueba
- **Usuario:** `test`
- **Password:** `test`

### Flujo de Autenticación
1. Ve a la página de **Login** (http://localhost:3000/login.html)
2. Ingresa las credenciales
3. Haz clic en "Login con Password Grant"
4. El token se guardará automáticamente en localStorage

## 📱 Páginas Disponibles

### 1. 🏠 Index (index.html)
- Página principal con navegación
- Estado del API Gateway
- Enlaces a todos los servicios
- Información del sistema

### 2. 🔐 Login (login.html)
- Autenticación con Keycloak
- Visualización del token JWT
- Información del usuario autenticado

### 3. 👤 Users Service (users.html)
**Funcionalidades:**
- ✅ Ver todos los usuarios (GET /api/users)
- ✅ Ver perfil actual (GET /api/users/me)
- ✅ Crear nuevo usuario (POST /api/users)

**Campos para crear usuario:**
- **Nombre Completo:** Se divide automáticamente en nombre y apellido
- **Email:** Campo obligatorio
- **Teléfono:** Opcional
- **Role:** USER, DOCTOR, NURSE, ADMIN

**Ejemplo:**
```
Nombre Completo: María López González
Email: maria.lopez@clinic.com
Teléfono: +52 123 456 7890
Role: DOCTOR
```

### 4. 📅 Appointments (appointments.html)
**Funcionalidades:**
- ✅ Ver todas las citas (GET /api/appointments)
- ✅ Crear nueva cita (POST /api/appointments)

**Campos para crear cita:**
- Patient ID y Patient Name
- Doctor ID y Doctor Name
- Especialidad
- Fecha y hora
- Descripción

**Formato de fecha:**
```
2025-12-03T14:30:00
```

### 5. 🖼️ Images Service (images.html)
**Funcionalidades:**
- Ver galería de imágenes
- Subir nuevas imágenes
- Preview antes de subir

**Formatos soportados:**
- JPG, PNG, GIF
- Tamaño máximo: 10MB

**⚠️ Nota:** El servicio de imágenes actualmente tiene un problema de autenticación (401) que está siendo resuelto. La funcionalidad de upload está implementada pero requiere corrección en la configuración JWT del backend.

## 🎨 Características del Frontend

### Diseño Moderno
- ✅ Interfaz responsiva (móvil y desktop)
- ✅ Colores modernos y profesionales
- ✅ Animaciones suaves
- ✅ Iconos emoji para mejor UX

### Manejo de Errores
- ✅ Mensajes claros de error
- ✅ Validación de autenticación
- ✅ Feedback visual (loading, success, error)

### Integración Completa
- ✅ Autenticación con Keycloak
- ✅ Token JWT en localStorage
- ✅ Headers de autorización automáticos
- ✅ CORS configurado

## 🔧 Arquitectura Backend

### Servicios Integrados
```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTP
       ↓
┌─────────────────┐
│  API Gateway    │ :8080
│ (Spring Cloud)  │
└────────┬────────┘
         │
    ┌────┴────┬──────────┬──────────┐
    ↓         ↓          ↓          ↓
┌─────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│  User   │ │Appoint │ │ Image  │ │Keycloak│
│ Service │ │Service │ │Service │ │  :8082 │
│  :9090  │ │  :8081 │ │ :5000  │ └────────┘
└────┬────┘ └────────┘ └───┬────┘
     │                      │
     │      gRPC :9091      │
     └──────────────────────┘
```

### Comunicación gRPC
- **User Service ↔ Image Service:** gRPC para obtener información de usuario
- **Puerto gRPC:** 9091
- **Protocolo:** plaintext (desarrollo)

## ✅ Estado de Funcionalidades

| Servicio | Endpoint | Estado | Notas |
|----------|----------|--------|-------|
| Login | POST /token | ✅ | Funcionando perfectamente |
| Users | GET /api/users | ✅ | Lista todos los usuarios |
| Users | GET /api/users/me | ✅ | Perfil del usuario actual |
| Users | POST /api/users | ⚠️ | Error 500 (constraint email) |
| Appointments | GET /api/appointments | ✅ | Lista todas las citas |
| Appointments | POST /api/appointments | ✅ | Crea citas correctamente |
| Images | GET /api/images | ⚠️ | Error 401 (config JWT) |
| Images | POST /api/images/upload | ⚠️ | Error 401 (config JWT) |
| Gateway | GET /actuator/health | ✅ | Sistema operativo |

## 🐛 Problemas Conocidos

### 1. POST /api/users (500)
**Causa:** El script de prueba intenta crear un usuario con un email que ya existe o viola constraints de BD.

**Solución desde el frontend:** Usar emails únicos al crear usuarios.

### 2. Image Service (401)
**Causa:** Configuración del issuer-uri de Keycloak. El servicio está rechazando los tokens JWT.

**En progreso:** Se está corrigiendo la configuración de seguridad OAuth2 del image-service.

## 📚 Uso desde el Frontend

### Flujo Típico de Usuario

1. **Autenticarse**
   ```
   Login → Ingresar test/test → Token guardado
   ```

2. **Ver Usuarios**
   ```
   Users → Get All Users → Ver lista formateada
   ```

3. **Crear Cita**
   ```
   Appointments → Create Appointment → Llenar formulario → Submit
   ```

4. **Subir Imagen** (cuando se corrija el 401)
   ```
   Images → Seleccionar archivo → Preview → Upload
   ```

## 🔍 Debugging

### Ver Token JWT
1. Ve a Login
2. Después de autenticarte, verás el payload del token
3. También disponible en: `localStorage.getItem('access_token')`

### Consola del Navegador
El frontend tiene logging detallado en la consola:
```javascript
// Ver todas las peticiones
🌐 Fetching: http://localhost:8080/api/users
📤 Config: {method: 'GET', headers: {...}}
📥 Response status: 200
```

### Funciones Helpers
Disponibles en la consola del navegador:
```javascript
// Guardar token manualmente
setTestToken('tu-token-aqui')

// Login programático
await loginWithPassword('test', 'test')

// Fetch con auth
await fetchWithAuth('http://localhost:8080/api/users')
```

## 🎯 Próximos Pasos

1. ✅ **Completado:** Frontend funcional con todas las páginas
2. ✅ **Completado:** Integración con User Service
3. ✅ **Completado:** Integración con Appointment Service
4. ⏳ **En progreso:** Corregir autenticación de Image Service
5. ⏳ **Pendiente:** Agregar edición y eliminación de registros
6. ⏳ **Pendiente:** Mejorar visualización con tablas HTML

## 📖 Documentación Adicional

- **Backend:** Ver `README_DOCKER.md`
- **Docker:** Ver `DOCKER_SETUP.md`
- **Testing:** Ver `test-endpoints.ps1`

## 🎨 Personalización

### Colores (styles.css)
```css
--primary-color: #2563eb;   /* Azul principal */
--success-color: #10b981;   /* Verde éxito */
--error-color: #ef4444;     /* Rojo error */
```

### API Gateway
Cambiar en `app.js`:
```javascript
const API_GATEWAY = 'http://localhost:8080';
```

---

**Desarrollado con ❤️ para el Sistema de Gestión de Clínicas**
