// appointments.js - Lógica para gestión de citas

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
        await fetchWithAuth(`${API_GATEWAY}/api/users/me`);
        return true;
    } catch(e) {
        console.error('Error al verificar usuario:', e);
        return false;
    }
}

async function fetchAppointments() {
    const token = getToken();
    if (!token) {
        showError('No estás autenticado. Ve a Login primero.');
        return;
    }

    try {
        showLoading();
        await ensureUserExists();
        
        const data = await fetchWithAuth(`${API_GATEWAY}/api/appointments`);
        document.getElementById('result').textContent = JSON.stringify(data, null, 2);
    } catch(e) {
        showError('Error al obtener citas: ' + e.message);
    }
}

async function createAppointment(event) {
    event.preventDefault();
    const token = getToken();
    if (!token) {
        showError('No estás autenticado. Ve a Login primero.');
        return;
    }

    const appointmentData = {
        patientId: document.getElementById('patientId').value,
        patientName: document.getElementById('patientName').value,
        doctorId: document.getElementById('doctorId').value,
        doctorName: document.getElementById('doctorName').value,
        specialty: document.getElementById('specialty').value,
        appointmentDate: document.getElementById('appointmentDate').value,
        description: document.getElementById('description').value
    };

    try {
        showLoading();
        const data = await fetchWithAuth(`${API_GATEWAY}/api/appointments`, {
            method: 'POST',
            body: JSON.stringify(appointmentData)
        });
        
        document.getElementById('result').textContent = JSON.stringify(data, null, 2);
        hideCreateForm();
        event.target.reset();
    } catch(e) {
        showError('Error al crear cita: ' + e.message);
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
    if (getToken()) {
        await ensureUserExists();
    }
});
