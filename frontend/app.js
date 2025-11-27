// app.js - Funciones comunes para autenticación y gestión de tokens

const KEYCLOAK_URL = 'http://localhost:8082';
const KEYCLOAK_REALM = 'clinic';
const KEYCLOAK_CLIENT_ID = 'clinic-frontend';
const KEYCLOAK_CLIENT_SECRET = 'gS3x7bDRltiBipTa28467qm3cESJQn9R';
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

// Login con Keycloak usando Password Grant (para desarrollo/testing)
async function loginWithPassword(username, password) {
    try {
        const formData = new URLSearchParams();
        formData.append('client_id', KEYCLOAK_CLIENT_ID);
        formData.append('client_secret', KEYCLOAK_CLIENT_SECRET);
        formData.append('username', username);
        formData.append('password', password);
        formData.append('grant_type', 'password');
        
        const response = await fetch(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error_description || 'Login failed');
        }
        
        const data = await response.json();
        saveToken(data.access_token);
        if (data.refresh_token) {
            localStorage.setItem('refresh_token', data.refresh_token);
        }
        if (data.expires_in) {
            const expiry = Date.now() + (data.expires_in * 1000);
            localStorage.setItem('token_expiry', expiry.toString());
        }
        
        return { success: true, data };
    } catch (error) {
        console.error('Login error:', error);
        return { success: false, error: error.message };
    }
}

// Función helper para hacer peticiones con mejor manejo de errores
async function fetchWithAuth(url, options = {}) {
    const token = getToken();
    if (!token) {
        throw new Error('No token available. Please login first.');
    }

    const headers = {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json',
        ...options.headers
    };

    // Solo agregar Content-Type si no es FormData
    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    const config = {
        ...options,
        headers,
        mode: 'cors',
        credentials: 'omit',
        cache: 'no-cache'
    };

    console.log('🌐 Fetching:', url);
    console.log('📤 Config:', config);

    try {
        const response = await fetch(url, config);
        
        console.log('📥 Response status:', response.status);
        console.log('📥 Response headers:', Object.fromEntries(response.headers.entries()));

        if (!response.ok) {
            let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
            try {
                const errorData = await response.text();
                if (errorData) {
                    errorMessage += `\n${errorData}`;
                }
            } catch (e) {
                // Ignorar si no se puede leer el body
            }
            throw new Error(errorMessage);
        }

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        } else if (contentType && contentType.includes('image/')) {
            return response.blob();
        } else {
            return await response.text();
        }
    } catch (error) {
        console.error('❌ Fetch error:', error);
        throw error;
    }
}

// Login con Keycloak (flujo Authorization Code - producción)
function loginWithKeycloak() {
    const redirectUri = encodeURIComponent(window.location.origin + '/login.html');
    const authUrl = `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/auth?client_id=${KEYCLOAK_CLIENT_ID}&redirect_uri=${redirectUri}&response_type=code&scope=openid`;
    window.location.href = authUrl;
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
            mode: 'cors',
            headers: {
                'Accept': 'application/json'
            },
            credentials: 'omit',
            cache: 'no-cache'
        });
        
        if (res.ok) {
            const data = await res.json();
            statusDiv.innerHTML = `<p class="success">✅ API Gateway: ${data.status || 'UP'}</p>`;
        } else {
            throw new Error(`Gateway responded with ${res.status}`);
        }
    } catch(e) {
        console.error('Gateway check error:', e);
        statusDiv.innerHTML = `<p class="error">❌ API Gateway: ${e.message}</p>`;
    }
}

// Función helper para testing: guardar un token manualmente
function setTestToken(token) {
    saveToken(token);
    alert('Token guardado. Recarga la página.');
}

// Exponer funciones en consola para testing
if (typeof window !== 'undefined') {
    window.setTestToken = setTestToken;
    window.loginWithPassword = loginWithPassword;
    window.fetchWithAuth = fetchWithAuth;
}
