// users.js - Lógica para gestión de usuarios

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
        
        // Formatear para mejor visualización
        const formatted = data.map(user => ({
            id: user.id,
            nombre: user.nombre,
            apellido: user.apellido,
            correo: user.correo,
            telefono: user.telefono || 'N/A',
            role: user.role,
            createdAt: user.createdAt ? new Date(user.createdAt).toLocaleString() : 'N/A'
        }));
        
        document.getElementById('result').textContent = JSON.stringify(formatted, null, 2);
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
        nombre: document.getElementById('userName').value.split(' ')[0] || '',
        apellido: document.getElementById('userName').value.split(' ').slice(1).join(' ') || '',
        correo: document.getElementById('userEmail').value,
        telefono: document.getElementById('userPhone')?.value || null,
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
