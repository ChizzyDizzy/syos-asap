-- ============================================
-- SYOS WEB POS SYSTEM - UPDATED DATABASE SCHEMA
-- Supporting Admin, Manager, and Customer roles
-- With concurrency control features
-- ============================================

-- Drop existing tables if needed for fresh setup
-- DROP DATABASE IF EXISTS syos_db;
-- CREATE DATABASE syos_db;
USE syos_db;

-- ============================================
-- 1. UPDATE USERS TABLE FOR THREE ROLES
-- ============================================
ALTER TABLE users
    MODIFY COLUMN role ENUM('ADMIN', 'MANAGER', 'CUSTOMER') NOT NULL;

-- Add new columns for better concurrency control
ALTER TABLE users
    ADD COLUMN version INT DEFAULT 0 COMMENT 'Optimistic locking version',
    ADD COLUMN is_active BOOLEAN DEFAULT TRUE COMMENT 'Account status',
    ADD COLUMN modified_by BIGINT NULL COMMENT 'Last modified by user_id',
    ADD COLUMN modified_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP;

-- ============================================
-- 2. ADD OPTIMISTIC LOCKING TO ITEMS
-- ============================================
ALTER TABLE items
    ADD COLUMN version INT DEFAULT 0 COMMENT 'Optimistic locking for concurrent updates',
    ADD COLUMN last_modified_by VARCHAR(50) NULL COMMENT 'User who last modified',
    ADD COLUMN locked_by BIGINT NULL COMMENT 'User currently editing (pessimistic lock)',
    ADD COLUMN lock_timestamp TIMESTAMP NULL COMMENT 'When lock was acquired';

-- Index for better performance on concurrent queries
CREATE INDEX idx_items_version ON items(version);
CREATE INDEX idx_items_locked_by ON items(locked_by);

-- ============================================
-- 3. ADD SALE TRACKING TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS sales (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     sale_number VARCHAR(50) UNIQUE NOT NULL,
                                     cashier_id BIGINT NOT NULL,
                                     total_amount DECIMAL(10, 2) NOT NULL,
                                     discount DECIMAL(10, 2) DEFAULT 0.00,
                                     tax_amount DECIMAL(10, 2) DEFAULT 0.00,
                                     payment_method ENUM('CASH', 'CARD', 'ONLINE') DEFAULT 'CASH',
                                     cash_tendered DECIMAL(10, 2),
                                     change_amount DECIMAL(10, 2),
                                     status ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'COMPLETED',
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     completed_at TIMESTAMP NULL,
                                     version INT DEFAULT 0,
                                     FOREIGN KEY (cashier_id) REFERENCES users(id) ON DELETE RESTRICT,
                                     INDEX idx_sale_number (sale_number),
                                     INDEX idx_cashier_id (cashier_id),
                                     INDEX idx_created_at (created_at),
                                     INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 4. SALE ITEMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS sale_items (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          sale_id BIGINT NOT NULL,
                                          item_code VARCHAR(20) NOT NULL,
                                          item_name VARCHAR(255) NOT NULL,
                                          quantity INT NOT NULL,
                                          unit_price DECIMAL(10, 2) NOT NULL,
                                          subtotal DECIMAL(10, 2) NOT NULL,
                                          FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
                                          FOREIGN KEY (item_code) REFERENCES items(code) ON DELETE RESTRICT,
                                          INDEX idx_sale_id (sale_id),
                                          INDEX idx_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 5. SESSION TRACKING FOR CONCURRENCY
-- ============================================
CREATE TABLE IF NOT EXISTS user_sessions (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             user_id BIGINT NOT NULL,
                                             session_id VARCHAR(255) UNIQUE NOT NULL,
                                             login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             ip_address VARCHAR(50),
                                             user_agent TEXT,
                                             is_active BOOLEAN DEFAULT TRUE,
                                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                             INDEX idx_session_id (session_id),
                                             INDEX idx_user_id (user_id),
                                             INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 6. CONCURRENT OPERATION LOGS
-- ============================================
CREATE TABLE IF NOT EXISTS operation_logs (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              user_id BIGINT,
                                              operation_type VARCHAR(50) NOT NULL,
                                              table_name VARCHAR(50) NOT NULL,
                                              record_id VARCHAR(100),
                                              operation_details JSON,
                                              status ENUM('SUCCESS', 'FAILED', 'CONFLICT') DEFAULT 'SUCCESS',
                                              error_message TEXT,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              execution_time_ms INT,
                                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                              INDEX idx_user_id (user_id),
                                              INDEX idx_operation_type (operation_type),
                                              INDEX idx_created_at (created_at),
                                              INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 7. STORED PROCEDURES FOR CONCURRENT OPERATIONS
-- ============================================

DELIMITER //

-- Procedure to acquire lock on item (pessimistic locking)
CREATE PROCEDURE sp_acquire_item_lock(
    IN p_item_code VARCHAR(20),
    IN p_user_id BIGINT,
    OUT p_lock_acquired BOOLEAN
)
BEGIN
    DECLARE lock_owner BIGINT;
    DECLARE lock_time TIMESTAMP;

    -- Check if item is already locked
    SELECT locked_by, lock_timestamp
    INTO lock_owner, lock_time
    FROM items
    WHERE code = p_item_code
        FOR UPDATE;

    -- If locked and lock is older than 5 minutes, release it
    IF lock_owner IS NOT NULL AND TIMESTAMPDIFF(MINUTE, lock_time, NOW()) > 5 THEN
        SET lock_owner = NULL;
    END IF;

    -- Acquire lock if available
    IF lock_owner IS NULL OR lock_owner = p_user_id THEN
        UPDATE items
        SET locked_by = p_user_id,
            lock_timestamp = NOW()
        WHERE code = p_item_code;
        SET p_lock_acquired = TRUE;
    ELSE
        SET p_lock_acquired = FALSE;
    END IF;
END//

-- Procedure to release lock on item
CREATE PROCEDURE sp_release_item_lock(
    IN p_item_code VARCHAR(20),
    IN p_user_id BIGINT
)
BEGIN
    UPDATE items
    SET locked_by = NULL,
        lock_timestamp = NULL
    WHERE code = p_item_code
      AND locked_by = p_user_id;
END//

-- Procedure for concurrent stock update with optimistic locking
CREATE PROCEDURE sp_update_stock_optimistic(
    IN p_item_code VARCHAR(20),
    IN p_quantity_change INT,
    IN p_expected_version INT,
    IN p_user_id VARCHAR(50),
    OUT p_success BOOLEAN,
    OUT p_new_version INT
)
BEGIN
    DECLARE current_version INT;
    DECLARE current_qty INT;

    -- Start transaction
    START TRANSACTION;

    -- Get current version and quantity with row lock
    SELECT version, (quantity_in_store + quantity_on_shelf)
    INTO current_version, current_qty
    FROM items
    WHERE code = p_item_code
        FOR UPDATE;

    -- Check version for optimistic locking
    IF current_version = p_expected_version THEN
        -- Check if we have enough stock
        IF (current_qty + p_quantity_change) >= 0 THEN
            -- Update the item
            UPDATE items
            SET quantity_on_shelf = quantity_on_shelf + p_quantity_change,
                version = version + 1,
                last_modified_by = p_user_id,
                updated_at = CURRENT_TIMESTAMP
            WHERE code = p_item_code;

            SET p_success = TRUE;
            SET p_new_version = current_version + 1;

            COMMIT;
        ELSE
            SET p_success = FALSE;
            SET p_new_version = current_version;
            ROLLBACK;
        END IF;
    ELSE
        -- Version mismatch - concurrent modification detected
        SET p_success = FALSE;
        SET p_new_version = current_version;
        ROLLBACK;
    END IF;
END//

-- Procedure for creating sale with concurrent stock reduction
CREATE PROCEDURE sp_create_sale_concurrent(
    IN p_sale_number VARCHAR(50),
    IN p_cashier_id BIGINT,
    IN p_total_amount DECIMAL(10, 2),
    IN p_discount DECIMAL(10, 2),
    IN p_cash_tendered DECIMAL(10, 2),
    IN p_change_amount DECIMAL(10, 2),
    IN p_items JSON,
    OUT p_sale_id BIGINT,
    OUT p_success BOOLEAN,
    OUT p_error_message VARCHAR(500)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE item_code VARCHAR(20);
    DECLARE item_qty INT;
    DECLARE item_price DECIMAL(10, 2);
    DECLARE current_stock INT;
    DECLARE i INT DEFAULT 0;
    DECLARE item_count INT;

    -- Error handler
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            SET p_success = FALSE;
            SET p_error_message = 'Database error occurred during sale creation';
            ROLLBACK;
        END;

    START TRANSACTION;

    -- Create sale record
    INSERT INTO sales (
        sale_number, cashier_id, total_amount, discount,
        cash_tendered, change_amount, status, completed_at
    ) VALUES (
                 p_sale_number, p_cashier_id, p_total_amount, p_discount,
                 p_cash_tendered, p_change_amount, 'COMPLETED', NOW()
             );

    SET p_sale_id = LAST_INSERT_ID();

    -- Get item count from JSON
    SET item_count = JSON_LENGTH(p_items);

    -- Process each item
    WHILE i < item_count DO
            SET item_code = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', i, '].code')));
            SET item_qty = JSON_EXTRACT(p_items, CONCAT('$[', i, '].quantity'));
            SET item_price = JSON_EXTRACT(p_items, CONCAT('$[', i, '].price'));

            -- Check and reduce stock with row lock
            SELECT quantity_on_shelf INTO current_stock
            FROM items
            WHERE code = item_code
                FOR UPDATE;

            IF current_stock < item_qty THEN
                SET p_success = FALSE;
                SET p_error_message = CONCAT('Insufficient stock for item: ', item_code);
                ROLLBACK;
                LEAVE;
            END IF;

            -- Reduce stock
            UPDATE items
            SET quantity_on_shelf = quantity_on_shelf - item_qty,
                version = version + 1,
                updated_at = CURRENT_TIMESTAMP
            WHERE code = item_code;

            -- Insert sale item
            INSERT INTO sale_items (sale_id, item_code, item_name, quantity, unit_price, subtotal)
            SELECT p_sale_id, code, name, item_qty, item_price, (item_qty * item_price)
            FROM items
            WHERE code = item_code;

            -- Record stock movement
            INSERT INTO stock_movements (item_code, movement_type, quantity, from_state, to_state, user_id, notes)
            VALUES (item_code, 'SALE', item_qty, 'ON_SHELF', 'SOLD_OUT', p_cashier_id, CONCAT('Sale: ', p_sale_number));

            SET i = i + 1;
        END WHILE;

    IF p_success IS NULL THEN
        SET p_success = TRUE;
        SET p_error_message = NULL;
        COMMIT;
    END IF;
END//

DELIMITER ;

-- ============================================
-- 8. INSERT SAMPLE USERS FOR ALL ROLES
-- ============================================

-- Clear existing users and insert new ones
DELETE FROM users;

INSERT INTO users (user_id, username, password_hash, role, full_name, email, is_active) VALUES
-- Admin users
('U001', 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMIN', 'System Administrator', 'admin@syos.com', TRUE),
('U002', 'admin2', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMIN', 'Admin Two', 'admin2@syos.com', TRUE),

-- Manager users
('U003', 'manager1', '6ee4a469cd4e91053847f5d3fcb61dbcc91e8f0ef10be7748da4c4a1ba382d17', 'MANAGER', 'Store Manager', 'manager1@syos.com', TRUE),
('U004', 'manager2', '6ee4a469cd4e91053847f5d3fcb61dbcc91e8f0ef10be7748da4c4a1ba382d17', 'MANAGER', 'Assistant Manager', 'manager2@syos.com', TRUE),

-- Customer users
('U005', 'customer1', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'CUSTOMER', 'John Customer', 'customer1@syos.com', TRUE),
('U006', 'customer2', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'CUSTOMER', 'Jane Customer', 'customer2@syos.com', TRUE);

-- Password reference:
-- admin/admin2: admin123 (hash: 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918)
-- manager1/manager2: manager123 (hash: 6ee4a469cd4e91053847f5d3fcb61dbcc91e8f0ef10be7748da4c4a1ba382d17)
-- customer1/customer2: customer123 (hash: e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446)

-- ============================================
-- 9. CREATE VIEWS FOR REPORTING
-- ============================================

-- View for concurrent sale statistics
CREATE OR REPLACE VIEW v_concurrent_sales_stats AS
SELECT
    DATE(s.created_at) as sale_date,
    HOUR(s.created_at) as sale_hour,
    COUNT(DISTINCT s.id) as total_sales,
    COUNT(DISTINCT s.cashier_id) as concurrent_cashiers,
    SUM(s.total_amount) as total_revenue,
    AVG(s.total_amount) as avg_sale_amount,
    MAX(s.created_at) as last_sale_time
FROM sales s
WHERE s.status = 'COMPLETED'
GROUP BY DATE(s.created_at), HOUR(s.created_at);

-- View for stock conflicts (items frequently locked)
CREATE OR REPLACE VIEW v_stock_conflict_report AS
SELECT
    i.code,
    i.name,
    COUNT(ol.id) as conflict_count,
    MAX(ol.created_at) as last_conflict_time
FROM items i
         LEFT JOIN operation_logs ol ON ol.table_name = 'items'
    AND ol.record_id = i.code
    AND ol.status = 'CONFLICT'
WHERE ol.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY i.code, i.name
HAVING conflict_count > 0
ORDER BY conflict_count DESC;

-- ============================================
-- 10. SETUP COMPLETION MESSAGE
-- ============================================
SELECT '========================================' as '';
SELECT 'DATABASE SCHEMA UPDATE COMPLETED!' as 'STATUS';
SELECT '========================================' as '';
SELECT 'Features Added:' as '';
SELECT '✓ Three-tier role system (ADMIN, MANAGER, CUSTOMER)' as '';
SELECT '✓ Optimistic locking for concurrent updates' as '';
SELECT '✓ Pessimistic locking for critical operations' as '';
SELECT '✓ Sale tracking with concurrent stock management' as '';
SELECT '✓ Session tracking for multi-user support' as '';
SELECT '✓ Operation logging for debugging concurrency' as '';
SELECT '========================================' as '';
