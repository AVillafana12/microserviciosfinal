-- Initialize user-service database schema
CREATE SCHEMA IF NOT EXISTS clinic;

-- Set search path to clinic schema
SET search_path TO clinic;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
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

-- ============================================
-- INSERT SAMPLE DATA (Optional - Remove for production)
-- ============================================
INSERT INTO clinic.users (id, nombre, apellido, correo, telefono, role) VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'Admin', 'Sistema', 'admin@clinic.com', '+34912345678', 'ADMIN'),
    ('550e8400-e29b-41d4-a716-446655440001', 'Dr. Juan', 'Pérez García', 'juan.perez@clinic.com', '+34912345679', 'DOCTOR'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Enfermera', 'María López', 'maria.lopez@clinic.com', '+34912345680', 'NURSE'),
    ('550e8400-e29b-41d4-a716-446655440003', 'Paciente', 'Carlos Rodríguez', 'carlos.rodriguez@clinic.com', '+34912345681', 'USER')
ON CONFLICT (correo) DO NOTHING;