-- Create database if not exists
CREATE DATABASE IF NOT EXISTS medicore;
USE medicore;

-- 1. Users table (consolidated for all roles)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role ENUM('ADMIN', 'DOCTOR', 'NURSE', 'LABTECH', 'PATIENT') NOT NULL,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin user
INSERT IGNORE INTO users (name, username, password, email, role, status) 
VALUES ('Administrator', 'admin', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy2k1pS', 'admin@medicore.com', 'ADMIN', 'active');

-- 2. Wards table
CREATE TABLE IF NOT EXISTS wards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT DEFAULT 20,
    occupied INT DEFAULT 0,
    available INT DEFAULT 20,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Doctors table
CREATE TABLE IF NOT EXISTS doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    name VARCHAR(255),
    specialization VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    ward_id INT,
    status ENUM('active', 'inactive') DEFAULT 'active',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ward_id) REFERENCES wards(id) ON DELETE SET NULL
);

-- 4. Nurses table
CREATE TABLE IF NOT EXISTS nurses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    name VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    ward_id INT,
    shift ENUM('Morning', 'Evening', 'Night'),
    status ENUM('active', 'inactive') DEFAULT 'active',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ward_id) REFERENCES wards(id) ON DELETE SET NULL
);

-- 5. Lab Technicians table
CREATE TABLE IF NOT EXISTS lab_technicians (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    name VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    status ENUM('active', 'inactive') DEFAULT 'active',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Patients table
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(20) UNIQUE, -- e.g. P001
    name VARCHAR(255) NOT NULL,
    age INT,
    gender ENUM('Male', 'Female', 'Other'),
    dob DATE,
    blood_type VARCHAR(10),
    diagnosis TEXT,
    ward_id INT,
    assigned_doctor_id INT,
    admission_date DATE,
    status ENUM('admitted', 'discharged', 'outpatient') DEFAULT 'outpatient',
    contact VARCHAR(50),
    email VARCHAR(255),
    emergency_contact VARCHAR(255),
    emergency_phone VARCHAR(50),
    insurance_provider VARCHAR(255),
    policy_number VARCHAR(100),
    insurance_expiry DATE,
    user_id INT UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (ward_id) REFERENCES wards(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(id) ON DELETE SET NULL
);

-- 7. Appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_code VARCHAR(20) UNIQUE,
    patient_id INT,
    doctor_id INT,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    reason TEXT,
    department VARCHAR(255),
    status ENUM('pending', 'confirmed', 'completed', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- 8. Lab Tests table
CREATE TABLE IF NOT EXISTS lab_tests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    test_code VARCHAR(20) UNIQUE,
    patient_id INT,
    doctor_id INT,
    test_type VARCHAR(255),
    urgency ENUM('Routine', 'STAT'),
    date_requested DATE,
    date_completed DATE,
    status ENUM('pending', 'in-progress', 'completed') DEFAULT 'pending',
    result TEXT,
    flag ENUM('normal', 'abnormal'),
    technician_id INT,
    reference_range TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    FOREIGN KEY (technician_id) REFERENCES lab_technicians(id) ON DELETE SET NULL
);

-- 9. Prescriptions table
CREATE TABLE IF NOT EXISTS prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_code VARCHAR(20) UNIQUE,
    patient_id INT,
    doctor_id INT,
    medication VARCHAR(255),
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    start_date DATE,
    end_date DATE,
    instructions TEXT,
    status ENUM('active', 'completed', 'discontinued') DEFAULT 'active',
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- 10. Vitals table
CREATE TABLE IF NOT EXISTS vitals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT,
    bp VARCHAR(20),
    hr INT,
    temp DECIMAL(4,1),
    spo2 INT,
    rr INT,
    weight DECIMAL(5,2),
    measured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- 11. Inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_code VARCHAR(20) UNIQUE,
    name VARCHAR(255),
    category VARCHAR(100),
    quantity INT,
    unit VARCHAR(50),
    reorder_level INT,
    status VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 12. Billing table
CREATE TABLE IF NOT EXISTS billing (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_code VARCHAR(20) UNIQUE,
    patient_id INT,
    service VARCHAR(255),
    billing_date DATE,
    amount DECIMAL(15,2),
    status ENUM('Paid', 'Pending', 'Partially Paid') DEFAULT 'Pending',
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- 13. Nurse Tasks table
CREATE TABLE IF NOT EXISTS nurse_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_code VARCHAR(20) UNIQUE,
    patient_id INT,
    description TEXT,
    due_time TIME,
    priority ENUM('Low', 'Medium', 'High'),
    status ENUM('todo', 'inprogress', 'done') DEFAULT 'todo',
    assigned_nurse_id INT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_nurse_id) REFERENCES nurses(id) ON DELETE SET NULL
);

-- 14. Medication Administration table
CREATE TABLE IF NOT EXISTS medication_administration (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT,
    medication VARCHAR(255),
    dose VARCHAR(100),
    time_due TIME,
    administered BOOLEAN DEFAULT FALSE,
    administered_at DATETIME,
    administered_by_id INT,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (administered_by_id) REFERENCES nurses(id) ON DELETE SET NULL
);

-- 15. Activities table (Audit Log)
CREATE TABLE IF NOT EXISTS activities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    icon VARCHAR(50),
    description TEXT,
    patient_name VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action_date DATE
);

-- 16. Doctor Notes table
CREATE TABLE IF NOT EXISTS doctor_notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT,
    doctor_id INT,
    note_date DATE,
    content TEXT,
    is_private BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- 17. Explicit Indexes (Optimization)
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_patient_status ON patients(status);
CREATE INDEX idx_appointment_date ON appointments(appointment_date);
CREATE INDEX idx_lab_test_status ON lab_tests(status);
CREATE INDEX idx_activity_date ON activities(action_date);
CREATE INDEX idx_billing_status ON billing(status);
