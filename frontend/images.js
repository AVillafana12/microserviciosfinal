// images.js - Lógica para gestión de imágenes

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
        
        if (!images || images.length === 0) {
            document.getElementById('imageList').innerHTML = '<p>No hay imágenes aún. Sube tu primera imagen usando el formulario arriba.</p>';
            return;
        }
        
        document.getElementById('imageList').innerHTML = `
            <div class="image-gallery">
                ${images.map(img => `
                    <div class="image-item">
                        <img src="${API_GATEWAY}/api/images/${img.id}" 
                             alt="${img.name || img.id}"
                             onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22><rect fill=%22%23ddd%22 width=%22200%22 height=%22200%22/><text x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%23999%22>Error</text></svg>'">
                        <p><strong>${img.name || 'Sin nombre'}</strong></p>
                        <small>${(img.size / 1024).toFixed(2)} KB</small><br>
                        <small>${new Date(img.uploadedAt).toLocaleString()}</small>
                    </div>
                `).join('')}
            </div>
        `;
    } catch(e) {
        console.error('Error al cargar imágenes:', e);
        document.getElementById('imageList').innerHTML = 
            `<p class="error">❌ Error al cargar imágenes: ${e.message}</p>
             <p class="warning">Nota: Si el servicio de imágenes está teniendo problemas de autenticación, 
             es posible que necesites verificar la configuración de seguridad.</p>`;
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
