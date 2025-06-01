-- Complete Database Setup for SYOS POS System
-- Run this file with: mysql -u root -p < complete_database_setup.sql

-- Create database
CREATE DATABASE IF NOT EXISTS syos_db;
USE syos_db;

-- Drop existing tables (in correct order due to foreign keys)
DROP TABLE IF EXISTS bill_items;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS stock_movements;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

-- Create Users table
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN', 'CASHIER', 'MANAGER') NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP NULL,
                       INDEX idx_username (username),
                       INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create Items table
CREATE TABLE items (
                       code VARCHAR(20) PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       price DECIMAL(10, 2) NOT NULL,
                       quantity INT NOT NULL DEFAULT 0,
                       state ENUM('IN_STORE', 'ON_SHELF', 'EXPIRED', 'SOLD_OUT') NOT NULL DEFAULT 'IN_STORE',
                       purchase_date DATE NOT NULL,
                       expiry_date DATE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_state (state),
                       INDEX idx_expiry (expiry_date),
                       INDEX idx_quantity (quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create Bills table
CREATE TABLE bills (
                       bill_number BIGINT PRIMARY KEY AUTO_INCREMENT,
                       bill_date DATETIME NOT NULL,
                       total_amount DECIMAL(10, 2) NOT NULL,
                       discount DECIMAL(10, 2) DEFAULT 0.00,
                       cash_tendered DECIMAL(10, 2) NOT NULL,
                       change_amount DECIMAL(10, 2) NOT NULL,
                       transaction_type ENUM('IN_STORE', 'ONLINE') NOT NULL DEFAULT 'IN_STORE',
                       user_id BIGINT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                       INDEX idx_bill_date (bill_date),
                       INDEX idx_transaction_type (transaction_type),
                       INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create Bill Items table (for bill details)
CREATE TABLE bill_items (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            bill_number BIGINT NOT NULL,
                            item_code VARCHAR(20) NOT NULL,
                            quantity INT NOT NULL,
                            unit_price DECIMAL(10, 2) NOT NULL,
                            total_price DECIMAL(10, 2) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (bill_number) REFERENCES bills(bill_number) ON DELETE CASCADE,
                            FOREIGN KEY (item_code) REFERENCES items(code) ON DELETE RESTRICT,
                            INDEX idx_bill_number (bill_number),
                            INDEX idx_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create Stock movements table (for tracking item movements)
CREATE TABLE stock_movements (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 item_code VARCHAR(20) NOT NULL,
                                 movement_type ENUM('PURCHASE', 'SHELF', 'SALE', 'EXPIRE', 'ADJUSTMENT') NOT NULL,
                                 quantity INT NOT NULL,
                                 from_state ENUM('IN_STORE', 'ON_SHELF', 'EXPIRED', 'SOLD_OUT'),
                                 to_state ENUM('IN_STORE', 'ON_SHELF', 'EXPIRED', 'SOLD_OUT'),
                                 movement_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 user_id BIGINT,
                                 notes TEXT,
                                 FOREIGN KEY (item_code) REFERENCES items(code) ON DELETE CASCADE,
                                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                 INDEX idx_movement_date (movement_date),
                                 INDEX idx_item_movement (item_code, movement_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create Audit log table
CREATE TABLE audit_log (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           table_name VARCHAR(50) NOT NULL,
                           record_id VARCHAR(50) NOT NULL,
                           action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
                           user_id BIGINT,
                           action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           old_values JSON,
                           new_values JSON,
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                           INDEX idx_audit_timestamp (action_timestamp),
                           INDEX idx_table_record (table_name, record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default users with proper password hashes
INSERT INTO users (username, email, password_hash, role) VALUES
-- Password: admin123 (SHA-256 hash)
('admin', 'admin@syos.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
-- Password: cashier123
('cashier1', 'cashier1@syos.com', '56f9a591bd64e713d87b1bb4e87062ad9b19080ad003b2cd8cdfcb9c3ab9da7b', 'CASHIER'),
-- Password: manager123
('manager1', 'manager1@syos.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MANAGER'),
-- Password: test123 (for testing)
('test', 'test@syos.com', 'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae', 'ADMIN');

-- Insert sample items
INSERT INTO items (code, name, price, quantity, state, purchase_date, expiry_date) VALUES
-- Dairy Products
('MILK001', 'Fresh Milk 1L', 3.50, 100, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
('YOGURT001', 'Plain Yogurt 500g', 3.25, 50, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 21 DAY)),
('CHEESE001', 'Cheddar Cheese 200g', 6.50, 40, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 45 DAY)),
('BUTTER001', 'Butter 250g', 4.00, 60, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY)),

-- Bakery
('BREAD001', 'White Bread', 2.50, 150, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
('BREAD002', 'Whole Wheat Bread', 3.00, 100, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),

-- Staples
('RICE001', 'Basmati Rice 5kg', 15.00, 200, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
('RICE002', 'Jasmine Rice 5kg', 14.50, 150, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
('FLOUR001', 'All Purpose Flour 1kg', 3.00, 250, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
('SUGAR001', 'White Sugar 1kg', 2.00, 300, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 730 DAY)),

-- Eggs and Oil
('EGGS001', 'Eggs (Dozen)', 4.50, 80, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY)),
('OIL001', 'Cooking Oil 1L', 5.50, 120, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY)),

-- Low stock items (for testing reorder report)
('SALT001', 'Table Salt 1kg', 1.50, 30, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 730 DAY)),
('PEPPER001', 'Black Pepper 100g', 3.50, 25, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),

-- Items expiring soon (for testing)
('BANANA001', 'Bananas 1kg', 2.50, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
('TOMATO001', 'Fresh Tomatoes 1kg', 3.00, 40, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY));

-- Create views for reporting
CREATE OR REPLACE VIEW daily_sales_summary AS
SELECT
    DATE(b.bill_date) as sale_date,
    COUNT(DISTINCT b.bill_number) as total_transactions,
    SUM(b.total_amount) as gross_revenue,
    SUM(b.discount) as total_discount,
    SUM(b.total_amount - b.discount) as net_revenue,
    COUNT(DISTINCT bi.item_code) as unique_items_sold,
    SUM(bi.quantity) as total_items_sold
FROM bills b
         LEFT JOIN bill_items bi ON b.bill_number = bi.bill_number
GROUP BY DATE(b.bill_date);

CREATE OR REPLACE VIEW low_stock_items AS
SELECT
    code,
    name,
    quantity,
    state,
    price
FROM items
WHERE quantity < 50
  AND state NOT IN ('EXPIRED', 'SOLD_OUT')
ORDER BY quantity ASC;

CREATE OR REPLACE VIEW expiring_soon AS
SELECT
    code,
    name,
    quantity,
    expiry_date,
    state,
    DATEDIFF(expiry_date, CURDATE()) as days_until_expiry
FROM items
WHERE expiry_date IS NOT NULL
  AND DATEDIFF(expiry_date, CURDATE()) BETWEEN 0 AND 7
  AND state NOT IN ('EXPIRED', 'SOLD_OUT')
ORDER BY expiry_date ASC;

CREATE OR REPLACE VIEW inventory_value AS
SELECT
    state,
    COUNT(*) as item_count,
    SUM(quantity) as total_quantity,
    SUM(price * quantity) as total_value
FROM items
WHERE state NOT IN ('EXPIRED', 'SOLD_OUT')
GROUP BY state
WITH ROLLUP;

-- Create stored procedures for common operations
DELIMITER //

CREATE PROCEDURE sp_record_sale(
    IN p_bill_number BIGINT,
    IN p_item_code VARCHAR(20),
    IN p_quantity INT
)
BEGIN
    -- Update item quantity
    UPDATE items
    SET quantity = quantity - p_quantity
    WHERE code = p_item_code;

    -- Record stock movement
    INSERT INTO stock_movements (item_code, movement_type, quantity, from_state, to_state, notes)
    VALUES (p_item_code, 'SALE', p_quantity, 'ON_SHELF', 'ON_SHELF', CONCAT('Bill #', p_bill_number));
END//

CREATE PROCEDURE sp_expire_items()
BEGIN
    -- Mark expired items
    UPDATE items
    SET state = 'EXPIRED'
    WHERE expiry_date < CURDATE()
      AND state != 'EXPIRED';

    -- Record the expiry movements
    INSERT INTO stock_movements (item_code, movement_type, quantity, from_state, to_state, notes)
    SELECT code, 'EXPIRE', quantity, state, 'EXPIRED', 'Auto-expired'
    FROM items
    WHERE expiry_date < CURDATE()
      AND state != 'EXPIRED';
END//

DELIMITER ;

-- Grant privileges (optional - if not using root)
-- CREATE USER IF NOT EXISTS 'syos_app'@'localhost' IDENTIFIED BY 'syos_password';
-- GRANT ALL PRIVILEGES ON syos_db.* TO 'syos_app'@'localhost';
-- FLUSH PRIVILEGES;

-- Display summary
SELECT 'Database setup completed successfully!' as Status;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as item_count FROM items;
SELECT DATABASE() as current_database;

-- Show user credentials for reference
SELECT
    username,
    email,
    role,
    CASE username
        WHEN 'admin' THEN 'admin123'
        WHEN 'cashier1' THEN 'cashier123'
        WHEN 'manager1' THEN 'manager123'
        WHEN 'test' THEN 'test123'
        END as password
FROM users
ORDER BY role, username;