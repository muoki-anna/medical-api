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

-- ==========================================
-- SEEDING DATA
-- ==========================================

-- Admin
INSERT IGNORE INTO users (name, username, password, role, status) 
VALUES ('Main Admin', 'admin', 'admin.africa', 'ADMIN', 'active');

-- Wards
INSERT IGNORE INTO wards (name, capacity, occupied, available) VALUES 
('Ward A', 24, 14, 10), 
('Ward B', 20, 12, 8), 
('Ward C', 18, 8, 10), 
('ICU', 8, 5, 3);

-- Doctors
INSERT IGNORE INTO users (name, username, password, role, status, email) VALUES 
('Dr. Amina Odhiambo', 'doctor', 'password123', 'DOCTOR', 'active', 'amina@medicore.ke'),
('Dr. James Kariuki', 'jkariuki', 'password123', 'DOCTOR', 'active', 'james@medicore.ke'),
('Dr. Sarah Njeri', 'snjeri', 'password123', 'DOCTOR', 'active', 'sarah@medicore.ke'),
('Dr. Peter Mutua', 'pmutua', 'password123', 'DOCTOR', 'active', 'peter@medicore.ke'),
('Dr. Lucy Akinyi', 'lakinyi', 'password123', 'DOCTOR', 'inactive', 'lucy@medicore.ke');

INSERT IGNORE INTO doctors (user_id, name, specialization, email, phone, ward_id, status)
SELECT u.id, u.name, 'Internal Medicine', u.email, '0712 345 678', w.id, 'active' FROM users u, wards w WHERE u.username = 'doctor' AND w.name = 'Ward A';
INSERT IGNORE INTO doctors (user_id, name, specialization, email, phone, ward_id, status)
SELECT u.id, u.name, 'Pediatrics', u.email, '0713 456 789', w.id, 'active' FROM users u, wards w WHERE u.username = 'jkariuki' AND w.name = 'Ward B';
INSERT IGNORE INTO doctors (user_id, name, specialization, email, phone, ward_id, status)
SELECT u.id, u.name, 'Surgery', u.email, '0714 567 890', w.id, 'active' FROM users u, wards w WHERE u.username = 'snjeri' AND w.name = 'Ward C';
INSERT IGNORE INTO doctors (user_id, name, specialization, email, phone, ward_id, status)
SELECT u.id, u.name, 'Cardiology', u.email, '0715 678 901', w.id, 'active' FROM users u, wards w WHERE u.username = 'pmutua' AND w.name = 'ICU';
INSERT IGNORE INTO doctors (user_id, name, specialization, email, phone, ward_id, status)
SELECT u.id, u.name, 'Neurology', u.email, '0716 789 012', w.id, 'inactive' FROM users u, wards w WHERE u.username = 'lakinyi' AND w.name = 'Ward A';

-- Nurses
INSERT IGNORE INTO users (name, username, password, role, status, email) VALUES 
('Patricia Wanjiku', 'pwanjiku', 'password123', 'NURSE', 'active', 'patricia@medicore.ke'),
('Kevin Otieno', 'kotieno', 'password123', 'NURSE', 'active', 'kevin@medicore.ke'),
('Mary Chebet', 'mchebet', 'password123', 'NURSE', 'active', 'mary@medicore.ke'),
('John Mwenda', 'jmwenda', 'password123', 'NURSE', 'active', 'john@medicore.ke');

INSERT IGNORE INTO nurses (user_id, name, email, phone, ward_id, shift, status)
SELECT u.id, u.name, u.email, '0718 111 222', w.id, 'Morning', 'active' FROM users u, wards w WHERE u.username = 'pwanjiku' AND w.name = 'Ward A';
INSERT IGNORE INTO nurses (user_id, name, email, phone, ward_id, shift, status)
SELECT u.id, u.name, u.email, '0718 222 333', w.id, 'Evening', 'active' FROM users u, wards w WHERE u.username = 'kotieno' AND w.name = 'Ward B';
INSERT IGNORE INTO nurses (user_id, name, email, phone, ward_id, shift, status)
SELECT u.id, u.name, u.email, '0718 333 444', w.id, 'Night', 'active' FROM users u, wards w WHERE u.username = 'mchebet' AND w.name = 'ICU';
INSERT IGNORE INTO nurses (user_id, name, email, phone, ward_id, shift, status)
SELECT u.id, u.name, u.email, '0718 444 555', w.id, 'Morning', 'active' FROM users u, wards w WHERE u.username = 'jmwenda' AND w.name = 'Ward C';

-- Lab Technicians
INSERT IGNORE INTO users (name, username, password, role, status, email) VALUES 
('Grace Muthoni', 'gmuthoni', 'password123', 'LABTECH', 'active', 'grace@medicore.ke'),
('Samuel Kiprop', 'skiprop', 'password123', 'LABTECH', 'active', 'samuel@medicore.ke'),
('Irene Waweru', 'iwaweru', 'password123', 'LABTECH', 'inactive', 'irene@medicore.ke');

INSERT IGNORE INTO lab_technicians (user_id, name, email, phone, status)
SELECT u.id, u.name, u.email, '0717 111 222', 'active' FROM users u WHERE u.username = 'gmuthoni';
INSERT IGNORE INTO lab_technicians (user_id, name, email, phone, status)
SELECT u.id, u.name, u.email, '0717 222 333', 'active' FROM users u WHERE u.username = 'skiprop';
INSERT IGNORE INTO lab_technicians (user_id, name, email, phone, status)
SELECT u.id, u.name, u.email, '0717 333 444', 'inactive' FROM users u WHERE u.username = 'iwaweru';

-- Patients
INSERT IGNORE INTO patients (patient_code, name, age, gender, blood_type, diagnosis, ward_id, assigned_doctor_id, admission_date, status, contact, email, emergency_contact, emergency_phone, insurance_provider, policy_number, insurance_expiry)
SELECT 'P001', 'Brian Mwangi', 34, 'Male', 'O+', 'Malaria', w.id, d.id, '2025-04-01', 'admitted', '0722 111 222', 'brian@email.com', 'Jane Mwangi', '0733 111 222', 'NHIF', 'NHF-2025-001', '2026-12-31' FROM wards w, doctors d WHERE w.name = 'Ward A' AND d.username = 'doctor';

INSERT IGNORE INTO patients (patient_code, name, age, gender, blood_type, diagnosis, ward_id, assigned_doctor_id, admission_date, status, contact, email, emergency_contact, emergency_phone, insurance_provider, policy_number, insurance_expiry)
SELECT 'P002', 'Cynthia Achieng', 28, 'Female', 'A+', 'Typhoid', w.id, d.id, '2025-04-03', 'admitted', '0722 222 333', 'cynthia@email.com', 'Paul Achieng', '0733 222 333', 'Jubilee', 'JUB-2025-045', '2025-11-30' FROM wards w, doctors d WHERE w.name = 'Ward B' AND d.username = 'jkariuki';

INSERT IGNORE INTO patients (patient_code, name, age, gender, blood_type, diagnosis, ward_id, assigned_doctor_id, admission_date, status, contact, email, emergency_contact, emergency_phone, insurance_provider, policy_number, insurance_expiry)
SELECT 'P003', 'David Kamau', 52, 'Male', 'B+', 'Hypertension', w.id, d.id, '2025-03-28', 'admitted', '0722 333 444', 'david@email.com', 'Ann Kamau', '0733 333 444', 'AAR', 'AAR-2025-112', '2026-06-15' FROM wards w, doctors d WHERE w.name = 'Ward A' AND d.username = 'doctor';

-- Inventory
INSERT IGNORE INTO inventory (item_code, name, category, quantity, unit, reorder_level, status) VALUES 
('INV001', 'Malaria RDT Kits', 'Diagnostics', 245, 'Tests', 100, 'In Stock'),
('INV002', 'CBC Reagent', 'Hematology', 34, 'Bottles', 50, 'Low'),
('INV003', 'Glucose Strips', 'Chemistry', 500, 'Strips', 200, 'In Stock'),
('INV004', 'Urinalysis Strips', 'Chemistry', 0, 'Strips', 150, 'Out of Stock'),
('INV005', 'X-Ray Film', 'Radiology', 89, 'Sheets', 50, 'In Stock');

-- Billboard (Activities)
INSERT IGNORE INTO activities (icon, description, patient_name, action_date) VALUES 
('general', 'Dr. Kariuki admitted Brian Mwangi', 'Brian Mwangi', '2025-04-18'),
('meds', 'Prescription issued for David Kamau', 'David Kamau', '2025-04-18'),
('vitals', 'Nurse Patricia recorded vitals for Ward A', 'Ward A', '2025-04-18');
