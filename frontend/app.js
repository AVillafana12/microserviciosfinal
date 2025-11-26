// app.js - Funciones comunes para autenticación y gestión de tokens

const KEYCLOAK_URL = 'http://localhost:8082';
const KEYCLOAK_REALM = 'clinic'; // Cambia esto según tu realm
const KEYCLOAK_CLIENT_ID = 'clinic-frontend'; // Cambia esto según tu cliente
const API_GATEWAY = 'http://localhost:8080';

// Obtener token del localStorage
function getToken() {
    return localStorage.getItem('access_token');
}

// Guardar token en localStorage
function saveToken(token) {
    localStorage.setItem('access_token', token);
}

// Limpiar token
function clearToken() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('token_expiry');
}

// Login con Keycloak (flujo simplificado - en producción usar PKCE)
function loginWithKeycloak() {
    // Nota: Este es un flujo simplificado. En producción deberías usar:
    // - Authorization Code Flow con PKCE
    // - Una librería como keycloak-js
    
    alert('⚠️ Configuración necesaria:\n\n' +
          '1. Crea un realm "clinic" en Keycloak\n' +
          '2. Crea un cliente público "clinic-frontend"\n' +
          '3. Configura Valid Redirect URIs: http://localhost:8090/*\n' +
          '4. Para pruebas rápidas, puedes obtener un token manualmente:\n' +
          '   - Ve a Keycloak Admin Console\n' +
          '   - Crea un usuario de prueba\n' +
          '   - Usa Postman o curl para obtener un token\n\n' +
          'Comando curl de ejemplo:\n' +
          'curl -X POST "http://localhost:8082/realms/clinic/protocol/openid-connect/token" \\\n' +
          '  -d "client_id=clinic-frontend" \\\n' +
          '  -d "username=tu_usuario" \\\n' +
          '  -d "password=tu_password" \\\n' +
          '  -d "grant_type=password"');
    
    // Redirigir a Keycloak (comentado porque necesita configuración)
    // const redirectUri = encodeURIComponent(window.location.origin + '/login.html');
    // const authUrl = `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/auth?client_id=${KEYCLOAK_CLIENT_ID}&redirect_uri=${redirectUri}&response_type=code&scope=openid`;
    // window.location.href = authUrl;
}

// Logout
function logout() {
    clearToken();
    alert('Sesión cerrada correctamente');
    window.location.href = 'index.html';
}

// Verificar estado de autenticación
function checkAuthStatus() {
    const token = getToken();
    const loginForm = document.getElementById('loginForm');
    const userInfo = document.getElementById('userInfo');
    const loginStatus = document.getElementById('loginStatus');
    
    if (token) {
        if (loginForm) loginForm.style.display = 'none';
        if (userInfo) {
            userInfo.style.display = 'block';
            try {
                const payload = parseJwt(token);
                document.getElementById('tokenInfo').textContent = JSON.stringify(payload, null, 2);
            } catch(e) {
                document.getElementById('tokenInfo').textContent = 'Token presente (no JWT válido)';
            }
        }
        if (loginStatus) {
            loginStatus.innerHTML = '<p class="success">✅ Sesión activa</p>';
        }
    } else {
        if (loginForm) loginForm.style.display = 'block';
        if (userInfo) userInfo.style.display = 'none';
        if (loginStatus) {
            loginStatus.innerHTML = '<p class="warning">⚠️ No estás autenticado. Configura Keycloak o ingresa un token manualmente.</p>';
        }
    }
}

// Actualizar status de auth en páginas de servicio
function updateAuthStatus() {
    const token = getToken();
    const authStatus = document.getElementById('authStatus');
    
    if (!authStatus) return;
    
    if (token) {
        authStatus.innerHTML = '<p class="success">✅ Autenticado</p>';
        authStatus.className = 'auth-status success';
    } else {
        authStatus.innerHTML = '<p class="warning">⚠️ No autenticado - <a href="login.html">Ir a Login</a></p>';
        authStatus.className = 'auth-status warning';
    }
}

// Parse JWT (para debug)
function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
}

// Verificar estado del gateway
async function checkGatewayStatus() {
    const statusDiv = document.getElementById('status');
    if (!statusDiv) return;
    
    try {
        const res = await fetch(`${API_GATEWAY}/actuator/health`, { 
            method: 'GET',
            mode: 'cors'
        });
        
        if (res.ok) {
            const data = await res.json();
            statusDiv.innerHTML = `<p class="success">✅ API Gateway: ${data.status || 'UP'}</p>`;
        } else {
            throw new Error('Gateway no disponible');
        }
    } catch(e) {
        statusDiv.innerHTML = `<p class="error">❌ API Gateway no disponible en ${API_GATEWAY}</p>`;
    }
}

// Función helper para testing: guardar un token manualmente
function setTestToken(token) {
    saveToken(token);
    alert('Token guardado. Recarga la página.');
}

// Exponer función en consola para testing
if (typeof window !== 'undefined') {
    window.setTestToken = setTestToken;
}
