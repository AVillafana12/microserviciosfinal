-- Initialize user-service database schema
-- Public schema para usuarios
SET search_path TO public;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    role VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on correo for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_correo ON users(correo);

-- Create index on role for filtering
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Create index on keycloak_id
CREATE INDEX IF NOT EXISTS idx_users_keycloak_id ON users(keycloak_id);

-- ============================================
-- INSERT SAMPLE DATA (Optional - Remove for production)
-- ============================================
INSERT INTO users (keycloak_id, nombre, apellido, correo, telefono, role) VALUES
    ('admin-keycloak-id', 'Admin', 'Sistema', 'admin@clinic.com', '+34912345678', 'ADMIN'),
    ('doctor-keycloak-id', 'Dr. Juan', 'Pérez García', 'juan.perez@clinic.com', '+34912345679', 'DOCTOR'),
    ('nurse-keycloak-id', 'Enfermera', 'María López', 'maria.lopez@clinic.com', '+34912345680', 'NURSE'),
    ('user-keycloak-id', 'Paciente', 'Carlos Rodríguez', 'carlos.rodriguez@clinic.com', '+34912345681', 'USER')
ON CONFLICT (correo) DO NOTHING;
