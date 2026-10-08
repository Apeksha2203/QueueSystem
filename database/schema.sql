-- VIVA GUIDE: Base MySQL tables and relationships. Apply migrations afterward for service-day reservations and measured processing fields.
CREATE DATABASE IF NOT EXISTS campus_queue;

USE campus_queue;

-- =========================
-- STUDENTS
-- =========================

-- Define table and constraints for students.
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    password VARCHAR(255) NOT NULL
);

-- =========================
-- SERVICES
-- =========================

-- Define table and constraints for services.
CREATE TABLE IF NOT EXISTS services (
    service_id INT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    average_service_time INT NOT NULL
);

-- =========================
-- COUNTERS
-- Each counter belongs to one service
-- =========================

-- Define table and constraints for counters.
CREATE TABLE IF NOT EXISTS counters (
    counter_id INT PRIMARY KEY AUTO_INCREMENT,
    counter_name VARCHAR(100) NOT NULL,
    service_id INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    -- Foreign key links this identifier to its parent table and prevents dangling references.
    FOREIGN KEY (service_id) REFERENCES services(service_id)
);

-- =========================
-- STAFF
-- Each staff member is assigned to one counter
-- Counter determines the service
-- =========================

-- Define table and constraints for staff.
CREATE TABLE IF NOT EXISTS staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    service_id INT,
    counter_id INT,
    -- Foreign key links this identifier to its parent table and prevents dangling references.
    FOREIGN KEY (service_id) REFERENCES services(service_id),
    -- Foreign key links this identifier to its parent table and prevents dangling references.
    FOREIGN KEY (counter_id) REFERENCES counters(counter_id)
);

-- =========================
-- QUEUE
-- Queue is SERVICE-LEVEL.
-- No counter_id is stored here.
-- Multiple counters can serve the same queue.
-- =========================

-- Define table and constraints for queue.
CREATE TABLE IF NOT EXISTS queue (
    queue_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    service_id INT NOT NULL,
    token_number INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    called_at TIMESTAMP NULL,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    -- Foreign key links this identifier to its parent table and prevents dangling references.
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    -- Foreign key links this identifier to its parent table and prevents dangling references.
    FOREIGN KEY (service_id) REFERENCES services(service_id)
);

-- =========================
-- BOOKINGS
-- =========================

-- Define table and constraints for bookings.
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    service_id INT NOT NULL,
    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Foreign key links this identifier to its parent table and prevents dangling references.
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    -- Foreign key links this identifier to its parent table and prevents dangling references.
    FOREIGN KEY (service_id) REFERENCES services(service_id)
);
