# 🔧 Corrección del Frontend - Funciones JavaScript

## ✅ Problema Resuelto

Se corrigieron los errores de funciones JavaScript no definidas en el frontend:
- `fetchUsers is not defined`
- `showCreateForm is not defined`
- `fetchAppointments is not defined`
- `fetchImages is not defined`

## 🔄 Cambios Realizados

### 1. **Separación de Código JavaScript**

Se crearon archivos JavaScript dedicados para cada módulo:

#### `frontend/users.js`
```javascript
// Funciones específicas para gestión de usuarios
- fetchUsers()
- createUser()
- showCreateForm()
- hideCreateForm()
- ensureUserExists()
```

#### `frontend/appointments.js`
```javascript
// Funciones específicas para gestión de citas
- fetchAppointments()
- createAppointment()
- showCreateForm()
- hideCreateForm()
- ensureUserExists()
```

#### `frontend/images.js`
```javascript
// Funciones específicas para gestión de imágenes
- fetchImages()
- uploadImage()
- initImagePreview()
- ensureUserExists()
```

### 2. **Actualización de Archivos HTML**

Cada archivo HTML ahora carga su script específico:

```html
<!-- Antes -->
<script src="app.js"></script>
<script>
  // Código inline...
</script>

<!-- Ahora -->
<script src="app.js"></script>
<script src="users.js"></script>  <!-- o appointments.js, images.js -->
```

### 3. **Mejoras en Manejo de Errores**

Todos los servicios ahora capturan y muestran errores detallados:

```javascript
if (!res.ok) {
    const errorText = await res.text();
    throw new Error(`HTTP ${res.status}: ${errorText}`);
}
```

## 🚀 Cómo Usar

### 1. **Login**
```
Usuario: test
Password: test
```

### 2. **Servicios Disponibles**

#### **Users Service** (`http://localhost:8090/users.html`)
- ✅ Ver todos los usuarios
- ✅ Crear nuevos usuarios
- ✅ Auto-sincronización con Keycloak

#### **Appointments Service** (`http://localhost:8090/appointments.html`)
- ✅ Ver todas las citas
- ✅ Crear nuevas citas
- ✅ Incluye información de paciente y doctor

#### **Images Service** (`http://localhost:8090/images.html`)
- ✅ Subir imágenes
- ✅ Ver galería de imágenes
- ✅ Preview antes de subir

## 📦 Estructura de Archivos

```
frontend/
├── index.html              # Página principal
├── login.html              # Página de login
├── users.html              # Gestión de usuarios
├── appointments.html       # Gestión de citas
├── images.html             # Gestión de imágenes
├── app.js                  # Funciones comunes (auth, tokens)
├── users.js                # ⭐ NUEVO - Lógica de usuarios
├── appointments.js         # ⭐ NUEVO - Lógica de citas
├── images.js               # ⭐ NUEVO - Lógica de imágenes
└── styles.css              # Estilos globales
```

## 🔑 Flujo de Autenticación

1. Usuario hace login con `test/test`
2. Se obtiene token JWT de Keycloak
3. Token se guarda en `localStorage`
4. Cada petición incluye: `Authorization: Bearer <token>`
5. Backend valida el token con Keycloak
6. Si el usuario no existe en BD, se crea automáticamente

## 🌐 URLs de Acceso

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Frontend | http://localhost:8090 | Aplicación web |
| Login | http://localhost:8090/login.html | Autenticación |
| Users | http://localhost:8090/users.html | CRUD usuarios |
| Appointments | http://localhost:8090/appointments.html | CRUD citas |
| Images | http://localhost:8090/images.html | Upload/gallery |
| API Gateway | http://localhost:8080 | Gateway principal |
| Keycloak | http://localhost:8082 | Admin: admin/admin |
| Eureka | http://localhost:8761 | Registro servicios |

## 🧪 Testing Rápido

### Opción 1: Desde la Interfaz Web
1. Abrir http://localhost:8090/login.html
2. Login: `test` / `test`
3. Navegar a cada servicio
4. Probar las funciones

### Opción 2: Desde la Consola del Navegador
```javascript
// Login programático
await loginWithPassword('test', 'test')

// Ver token actual
console.log(getToken())

// Probar endpoint
fetch('http://localhost:8080/api/users', {
    headers: { 'Authorization': 'Bearer ' + getToken() }
}).then(r => r.json()).then(console.log)
```

## ✨ Características Implementadas

### ✅ Auto-sincronización de Usuarios
Cada página verifica automáticamente si el usuario existe en la base de datos y lo crea si es necesario.

### ✅ Validación de Token
Cada función verifica que existe un token válido antes de hacer peticiones.

### ✅ Manejo de Errores
Mensajes de error claros y detallados para debugging.

### ✅ UI Responsive
Interfaz adaptada a diferentes tamaños de pantalla.

### ✅ Preview de Imágenes
Vista previa antes de subir imágenes.

## 🐛 Solución de Problemas

### Error: "No estás autenticado"
**Solución**: Ve a `/login.html` y haz login con `test/test`

### Error: "Function is not defined"
**Solución**: Verifica que el navegador cargó todos los scripts. Recarga la página (Ctrl+F5)

### Error: "CORS"
**Solución**: Verifica que el Gateway esté corriendo en http://localhost:8080

### Error: "401 Unauthorized"
**Solución**: El token expiró. Haz login nuevamente.

## 🔄 Estado de los Servicios

```bash
# Ver estado
docker-compose ps

# Ver logs de un servicio
docker-compose logs -f gateway

# Reiniciar un servicio
docker-compose restart gateway
```

## 📝 Notas Importantes

1. **Token Storage**: Los tokens se guardan en `localStorage` del navegador
2. **Auto-create User**: Al cargar cualquier página, se intenta crear el usuario automáticamente
3. **Error Handling**: Todos los errores se muestran en la UI con formato JSON
4. **CORS**: Configurado en el Gateway para permitir peticiones desde localhost:8090

---

**✅ Todo listo para usar con login test/test**
