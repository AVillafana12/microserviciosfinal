// users.js - Lógica para gestión de usuarios

const API_GATEWAY = 'http://localhost:8080';

function showCreateForm() {
    document.getElementById('createForm').style.display = 'block';
}

function hideCreateForm() {
    document.getElementById('createForm').style.display = 'none';
}

async function ensureUserExists() {
    const token = getToken();
    if (!token) return false;

    try {
        // Llamar a /me para crear/obtener el usuario desde Keycloak
        await fetchWithAuth(`${API_GATEWAY}/api/users/me`);
        return true;
    } catch(e) {
        console.error('Error al verificar usuario:', e);
        return false;
    }
}

async function fetchUsers() {
    const token = getToken();
    if (!token) {
        showError('No estás autenticado. Ve a Login primero.');
        return;
    }

    try {
        showLoading();
        
        // Asegurar que el usuario existe en la BD
        await ensureUserExists();
        
        const data = await fetchWithAuth(`${API_GATEWAY}/api/users`);
        document.getElementById('result').textContent = JSON.stringify(data, null, 2);
    } catch(e) {
        showError('Error al obtener usuarios: ' + e.message);
    }
}

async function createUser(event) {
    event.preventDefault();
    const token = getToken();
    if (!token) {
        showError('No estás autenticado. Ve a Login primero.');
        return;
    }

    const userData = {
        name: document.getElementById('userName').value,
        email: document.getElementById('userEmail').value,
        role: document.getElementById('userRole').value
    };

    try {
        showLoading();
        const data = await fetchWithAuth(`${API_GATEWAY}/api/users`, {
            method: 'POST',
            body: JSON.stringify(userData)
        });
        
        document.getElementById('result').textContent = JSON.stringify(data, null, 2);
        hideCreateForm();
        document.getElementById('userName').value = '';
        document.getElementById('userEmail').value = '';
    } catch(e) {
        showError('Error al crear usuario: ' + e.message);
    }
}

function showLoading() {
    document.getElementById('result').textContent = 'Cargando...';
}

function showError(message) {
    document.getElementById('result').textContent = `❌ ${message}`;
}

// Verificar autenticación al cargar
window.addEventListener('DOMContentLoaded', async () => {
    updateAuthStatus();
    // Crear usuario automáticamente al cargar la página si está autenticado
    if (getToken()) {
        await ensureUserExists();
    }
});
