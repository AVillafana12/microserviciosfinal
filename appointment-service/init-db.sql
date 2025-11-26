-- Create appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    specialty VARCHAR(100),
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create doctors table (referencia de doctores)
CREATE TABLE IF NOT EXISTS doctors (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indices for better query performance
CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);
CREATE INDEX IF NOT EXISTS idx_appointments_appointment_date ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_status ON appointments(patient_id, status);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_status ON appointments(doctor_id, status);

-- Insert sample doctors
INSERT INTO doctors (id, name, specialty, email, phone) VALUES
    ('doc-001', 'Dr. Juan García', 'Cardiology', 'juan.garcia@clinic.com', '555-0001'),
    ('doc-002', 'Dra. María López', 'Pediatrics', 'maria.lopez@clinic.com', '555-0002'),
    ('doc-003', 'Dr. Carlos Rodríguez', 'Orthopedics', 'carlos.rodriguez@clinic.com', '555-0003'),
    ('doc-004', 'Dra. Ana Martínez', 'Dermatology', 'ana.martinez@clinic.com', '555-0004')
ON CONFLICT DO NOTHING;

-- Insert sample appointments
INSERT INTO appointments (id, patient_id, patient_name, doctor_id, doctor_name, specialty, appointment_date, status, description) VALUES
    ('apt-001', 'pat-001', 'Juan Pérez', 'doc-001', 'Dr. Juan García', 'Cardiology', CURRENT_TIMESTAMP + INTERVAL '2 days', 'SCHEDULED', 'Routine checkup'),
    ('apt-002', 'pat-002', 'María González', 'doc-002', 'Dra. María López', 'Pediatrics', CURRENT_TIMESTAMP + INTERVAL '3 days', 'CONFIRMED', 'Annual vaccination'),
    ('apt-003', 'pat-001', 'Juan Pérez', 'doc-003', 'Dr. Carlos Rodríguez', 'Orthopedics', CURRENT_TIMESTAMP + INTERVAL '5 days', 'SCHEDULED', 'Knee pain consultation'),
    ('apt-004', 'pat-003', 'Carlos López', 'doc-001', 'Dr. Juan García', 'Cardiology', CURRENT_TIMESTAMP + INTERVAL '1 day', 'CONFIRMED', 'Blood pressure check'),
    ('apt-005', 'pat-002', 'María González', 'doc-004', 'Dra. Ana Martínez', 'Dermatology', CURRENT_TIMESTAMP - INTERVAL '1 day', 'COMPLETED', 'Skin exam completed')
ON CONFLICT DO NOTHING;
