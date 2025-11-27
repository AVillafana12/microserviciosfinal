// images.js - Lógica para gestión de imágenes

const API_GATEWAY = 'http://localhost:8080';

// Preview de imagen antes de upload
function initImagePreview() {
    const fileInput = document.getElementById('imageFile');
    if (fileInput) {
        fileInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    document.getElementById('preview').innerHTML = 
                        `<img src="${e.target.result}" style="max-width: 300px; max-height: 300px;">`;
                };
                reader.readAsDataURL(file);
            }
        });
    }
}

async function uploadImage(event) {
    event.preventDefault();
    const token = getToken();
    if (!token) {
        showError('No estás autenticado. Ve a Login primero.');
        return;
    }

    const fileInput = document.getElementById('imageFile');
    if (!fileInput.files[0]) {
        showError('Selecciona una imagen primero');
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try {
        document.getElementById('uploadResult').textContent = 'Uploading...';
        
        const data = await fetchWithAuth(`${API_GATEWAY}/api/images/upload`, {
            method: 'POST',
            body: formData
        });
        
        document.getElementById('uploadResult').textContent = JSON.stringify(data, null, 2);
        
        // Limpiar formulario y preview
        fileInput.value = '';
        document.getElementById('preview').innerHTML = '';
        
        // Recargar lista
        fetchImages();
    } catch(e) {
        showError('Error al subir imagen: ' + e.message);
    }
}

async function ensureUserExists() {
    const token = getToken();
    if (!token) return false;

    try {
        await fetchWithAuth(`${API_GATEWAY}/api/users/me`);
        return true;
    } catch(e) {
        console.error('Error al verificar usuario:', e);
        return false;
    }
}

async function fetchImages() {
    const token = getToken();
    if (!token) {
        document.getElementById('imageList').innerHTML = 
            '<p class="error">❌ No estás autenticado. Ve a Login primero.</p>';
        return;
    }

    try {
        document.getElementById('imageList').innerHTML = '<p>Cargando imágenes...</p>';
        await ensureUserExists();
        
        const images = await fetchWithAuth(`${API_GATEWAY}/api/images`);
        
        if (images.length === 0) {
            document.getElementById('imageList').innerHTML = '<p>No hay imágenes aún.</p>';
            return;
        }
        
        document.getElementById('imageList').innerHTML = images.map(img => `
            <div class="image-item">
                <img src="${API_GATEWAY}/api/images/${img.id}" alt="${img.name || img.id}">
                <p>${img.name || img.id}</p>
            </div>
        `).join('');
    } catch(e) {
        document.getElementById('imageList').innerHTML = 
            `<p class="error">❌ Error al cargar imágenes: ${e.message}</p>`;
    }
}

function showError(message) {
    document.getElementById('uploadResult').textContent = `❌ ${message}`;
}

// Verificar autenticación al cargar
window.addEventListener('DOMContentLoaded', async () => {
    updateAuthStatus();
    initImagePreview();
    if (getToken()) {
        await ensureUserExists();
    }
});
