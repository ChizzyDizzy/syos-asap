-- Recreate users table with all required columns
USE syos_db;

-- Backup existing data (if any)
CREATE TABLE IF NOT EXISTS users_backup AS SELECT * FROM users;

-- Drop and recreate the table
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN', 'CASHIER', 'MANAGER') NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP NULL,
                       INDEX idx_username (username)
);

-- Insert test users with proper password hashes
INSERT INTO users (username, email, password_hash, role) VALUES
-- Password: admin123
('admin', 'admin@syos.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
-- Password: cashier123
('cashier1', 'cashier1@syos.com', '56f9a591bd64e713d87b1bb4e87062ad9b19080ad003b2cd8cdfcb9c3ab9da7b', 'CASHIER'),
-- Password: manager123
('manager1', 'manager1@syos.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MANAGER');

-- Verify the data
SELECT username, email, role, created_at FROM users;