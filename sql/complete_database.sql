-- ============================================
-- SYOS POS SYSTEM - COMPLETE DATABASE SCHEMA
-- Compatible with CLI and Web Application
-- Supports: ADMIN, MANAGER, CASHIER, CUSTOMER
-- FIXED: Stored procedure syntax errors
-- ============================================

DROP DATABASE IF EXISTS syos_db;
CREATE DATABASE syos_db;
USE syos_db;

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       user_id VARCHAR(50) UNIQUE NOT NULL,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100),
                       password_hash VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN', 'MANAGER', 'CASHIER', 'CUSTOMER') NOT NULL,
                       full_name VARCHAR(100),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       version INT DEFAULT 0,
                       modified_by BIGINT NULL,
                       modified_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_user_id (user_id),
                       INDEX idx_username (username),
                       INDEX idx_email (email),
                       INDEX idx_role (role),
                       INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- ITEMS TABLE
-- ============================================
CREATE TABLE items (
                       code VARCHAR(20) PRIMARY KEY,
                       item_code VARCHAR(20) AS (code) STORED,
                       name VARCHAR(100) NOT NULL,
                       category VARCHAR(50) DEFAULT 'General',
                       price DECIMAL(10, 2) NOT NULL,
                       quantity_in_store INT NOT NULL DEFAULT 0,
                       quantity_on_shelf INT NOT NULL DEFAULT 0,
                       quantity INT AS (quantity_in_store + quantity_on_shelf) STORED,
                       reorder_level INT DEFAULT 50,
                       state ENUM('IN_STORE', 'ON_SHELF', 'EXPIRED', 'SOLD_OUT') NOT NULL DEFAULT 'IN_STORE',
                       purchase_date DATE NOT NULL DEFAULT (CURDATE()),
                       expiry_date DATE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       version INT DEFAULT 0,
                       last_modified_by VARCHAR(50) NULL,
                       locked_by BIGINT NULL,
                       lock_timestamp TIMESTAMP NULL,
                       INDEX idx_state (state),
                       INDEX idx_expiry (expiry_date),
                       INDEX idx_category (category),
                       INDEX idx_quantity_on_shelf (quantity_on_shelf),
                       INDEX idx_version (version),
                       INDEX idx_locked_by (locked_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BILLS TABLE
-- ============================================
CREATE TABLE bills (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       bill_number VARCHAR(50) UNIQUE NOT NULL,
                       bill_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       total_amount DECIMAL(10, 2) NOT NULL,
                       discount DECIMAL(10, 2) DEFAULT 0.00,
                       tax_amount DECIMAL(10, 2) DEFAULT 0.00,
                       cash_tendered DECIMAL(10, 2) NOT NULL,
                       cash_received DECIMAL(10, 2) AS (cash_tendered) STORED,
                       change_amount DECIMAL(10, 2) NOT NULL,
                       transaction_type ENUM('IN_STORE', 'ONLINE', 'OFFLINE') NOT NULL DEFAULT 'IN_STORE',
                       payment_method ENUM('CASH', 'CARD', 'MOBILE', 'OTHER') DEFAULT 'CASH',
                       status ENUM('PENDING', 'COMPLETED', 'CANCELLED', 'REFUNDED') DEFAULT 'COMPLETED',
                       user_id_ref BIGINT,
                       user_id VARCHAR(50),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       completed_at TIMESTAMP NULL,
                       version INT DEFAULT 0,
                       FOREIGN KEY (user_id_ref) REFERENCES users(id) ON DELETE SET NULL,
                       INDEX idx_bill_number (bill_number),
                       INDEX idx_bill_date (bill_date),
                       INDEX idx_transaction_type (transaction_type),
                       INDEX idx_status (status),
                       INDEX idx_user_id (user_id),
                       INDEX idx_user_id_ref (user_id_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BILL ITEMS TABLE
-- ============================================
CREATE TABLE bill_items (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            bill_id BIGINT,
                            bill_number VARCHAR(50),
                            item_code VARCHAR(20) NOT NULL,
                            item_name VARCHAR(100),
                            quantity INT NOT NULL,
                            unit_price DECIMAL(10, 2) NOT NULL,
                            total_price DECIMAL(10, 2) AS (quantity * unit_price) STORED,
                            subtotal DECIMAL(10, 2) AS (quantity * unit_price) STORED,
                            discount DECIMAL(10, 2) DEFAULT 0.00,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
                            FOREIGN KEY (item_code) REFERENCES items(code) ON DELETE RESTRICT,
                            INDEX idx_bill_id (bill_id),
                            INDEX idx_bill_number (bill_number),
                            INDEX idx_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- STOCK MOVEMENTS TABLE
-- ============================================
CREATE TABLE stock_movements (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 item_code VARCHAR(20) NOT NULL,
                                 movement_type ENUM('PURCHASE', 'SHELF', 'SALE', 'EXPIRE', 'ADJUSTMENT', 'RETURN', 'DAMAGE') NOT NULL,
                                 quantity INT NOT NULL,
                                 from_state ENUM('IN_STORE', 'ON_SHELF', 'EXPIRED', 'SOLD_OUT'),
                                 to_state ENUM('IN_STORE', 'ON_SHELF', 'EXPIRED', 'SOLD_OUT'),
                                 movement_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 user_id BIGINT,
                                 reference_id VARCHAR(100),
                                 reference_type VARCHAR(50),
                                 notes TEXT,
                                 FOREIGN KEY (item_code) REFERENCES items(code) ON DELETE CASCADE,
                                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                 INDEX idx_movement_date (movement_date),
                                 INDEX idx_item_movement (item_code, movement_date),
                                 INDEX idx_movement_type (movement_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- AUDIT LOG TABLE
-- ============================================
CREATE TABLE audit_log (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           table_name VARCHAR(50) NOT NULL,
                           record_id VARCHAR(50) NOT NULL,
                           action ENUM('INSERT', 'UPDATE', 'DELETE', 'SELECT') NOT NULL,
                           user_id BIGINT,
                           action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           old_values JSON,
                           new_values JSON,
                           ip_address VARCHAR(50),
                           user_agent TEXT,
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                           INDEX idx_audit_timestamp (action_timestamp),
                           INDEX idx_table_record (table_name, record_id),
                           INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- USER SESSIONS TABLE
-- ============================================
CREATE TABLE user_sessions (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               session_id VARCHAR(255) UNIQUE NOT NULL,
                               login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               logout_time TIMESTAMP NULL,
                               ip_address VARCHAR(50),
                               user_agent TEXT,
                               is_active BOOLEAN DEFAULT TRUE,
                               session_type ENUM('CLI', 'WEB') DEFAULT 'WEB',
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               INDEX idx_session_id (session_id),
                               INDEX idx_user_id (user_id),
                               INDEX idx_is_active (is_active),
                               INDEX idx_session_type (session_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- OPERATION LOGS TABLE
-- ============================================
CREATE TABLE operation_logs (
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
-- SALES TABLE (Additional tracking)
-- ============================================
CREATE TABLE sales (
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
-- SALE ITEMS TABLE
-- ============================================
CREATE TABLE sale_items (
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
-- STORED PROCEDURES
-- ============================================

DELIMITER //

-- Procedure to acquire lock on item
CREATE PROCEDURE sp_acquire_item_lock(
    IN p_item_code VARCHAR(20),
    IN p_user_id BIGINT,
    OUT p_lock_acquired BOOLEAN
)
BEGIN
    DECLARE lock_owner BIGINT;
    DECLARE lock_time TIMESTAMP;

    SELECT locked_by, lock_timestamp
    INTO lock_owner, lock_time
    FROM items
    WHERE code = p_item_code
        FOR UPDATE;

    IF lock_owner IS NOT NULL AND TIMESTAMPDIFF(MINUTE, lock_time, NOW()) > 5 THEN
        SET lock_owner = NULL;
    END IF;

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

    START TRANSACTION;

    SELECT version, quantity_on_shelf
    INTO current_version, current_qty
    FROM items
    WHERE code = p_item_code
        FOR UPDATE;

    IF current_version = p_expected_version THEN
        IF (current_qty + p_quantity_change) >= 0 THEN
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
        SET p_success = FALSE;
        SET p_new_version = current_version;
        ROLLBACK;
    END IF;
END//

-- Procedure for creating sale with concurrent stock reduction (FIXED)
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
    DECLARE item_code VARCHAR(20);
    DECLARE item_qty INT;
    DECLARE item_price DECIMAL(10, 2);
    DECLARE current_stock INT;
    DECLARE i INT DEFAULT 0;
    DECLARE item_count INT;

    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            SET p_success = FALSE;
            SET p_error_message = 'Database error occurred during sale creation';
            ROLLBACK;
        END;

    SET p_success = TRUE;
    START TRANSACTION;

    INSERT INTO sales (
        sale_number, cashier_id, total_amount, discount,
        cash_tendered, change_amount, status, completed_at
    ) VALUES (
                 p_sale_number, p_cashier_id, p_total_amount, p_discount,
                 p_cash_tendered, p_change_amount, 'COMPLETED', NOW()
             );

    SET p_sale_id = LAST_INSERT_ID();
    SET item_count = JSON_LENGTH(p_items);

    process_items: WHILE i < item_count DO
            SET item_code = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', i, '].code')));
            SET item_qty = JSON_EXTRACT(p_items, CONCAT('$[', i, '].quantity'));
            SET item_price = JSON_EXTRACT(p_items, CONCAT('$[', i, '].price'));

            SELECT quantity_on_shelf INTO current_stock
            FROM items
            WHERE code = item_code
                FOR UPDATE;

            IF current_stock < item_qty THEN
                SET p_success = FALSE;
                SET p_error_message = CONCAT('Insufficient stock for item: ', item_code);
                ROLLBACK;
                LEAVE process_items;
            END IF;

            UPDATE items
            SET quantity_on_shelf = quantity_on_shelf - item_qty,
                version = version + 1,
                updated_at = CURRENT_TIMESTAMP
            WHERE code = item_code;

            INSERT INTO sale_items (sale_id, item_code, item_name, quantity, unit_price, subtotal)
            SELECT p_sale_id, code, name, item_qty, item_price, (item_qty * item_price)
            FROM items
            WHERE code = item_code;

            INSERT INTO stock_movements (item_code, movement_type, quantity, from_state, to_state, user_id, reference_id, reference_type, notes)
            VALUES (item_code, 'SALE', -item_qty, 'ON_SHELF', 'SOLD_OUT', p_cashier_id, p_sale_number, 'SALE', CONCAT('Sale: ', p_sale_number));

            SET i = i + 1;
        END WHILE process_items;

    IF p_success = TRUE THEN
        SET p_error_message = NULL;
        COMMIT;
    END IF;
END//

-- Procedure to expire items
CREATE PROCEDURE sp_expire_items()
BEGIN
    UPDATE items
    SET state = 'EXPIRED',
        quantity_on_shelf = 0,
        quantity_in_store = 0
    WHERE expiry_date < CURDATE()
      AND state != 'EXPIRED';

    INSERT INTO stock_movements (item_code, movement_type, quantity, from_state, to_state, notes)
    SELECT
        code,
        'EXPIRE',
        (quantity_in_store + quantity_on_shelf),
        state,
        'EXPIRED',
        'Auto-expired by system'
    FROM items
    WHERE expiry_date < CURDATE()
      AND state = 'EXPIRED'
      AND NOT EXISTS (
        SELECT 1 FROM stock_movements sm
        WHERE sm.item_code = items.code
          AND sm.movement_type = 'EXPIRE'
          AND DATE(sm.movement_date) = CURDATE()
    );
END//

-- Function to generate bill number
CREATE FUNCTION fn_generate_bill_number()
    RETURNS VARCHAR(50)
    DETERMINISTIC
BEGIN
    DECLARE bill_count INT;
    DECLARE new_bill_number VARCHAR(50);

    SELECT COUNT(*) INTO bill_count FROM bills;
    SET new_bill_number = CONCAT('BILL', LPAD(bill_count + 1, 6, '0'));

    RETURN new_bill_number;
END//

DELIMITER ;

-- ============================================
-- TRIGGERS
-- ============================================

DELIMITER //

-- Audit trigger for items update
CREATE TRIGGER tr_items_audit_update
    AFTER UPDATE ON items
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (table_name, record_id, action, old_values, new_values)
    VALUES (
               'items',
               OLD.code,
               'UPDATE',
               JSON_OBJECT(
                       'name', OLD.name,
                       'price', OLD.price,
                       'quantity_in_store', OLD.quantity_in_store,
                       'quantity_on_shelf', OLD.quantity_on_shelf,
                       'state', OLD.state
               ),
               JSON_OBJECT(
                       'name', NEW.name,
                       'price', NEW.price,
                       'quantity_in_store', NEW.quantity_in_store,
                       'quantity_on_shelf', NEW.quantity_on_shelf,
                       'state', NEW.state
               )
           );
END//

-- Audit trigger for bills insert
CREATE TRIGGER tr_bills_audit_insert
    AFTER INSERT ON bills
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (table_name, record_id, action, user_id, new_values)
    VALUES (
               'bills',
               NEW.bill_number,
               'INSERT',
               NEW.user_id_ref,
               JSON_OBJECT(
                       'bill_number', NEW.bill_number,
                       'total_amount', NEW.total_amount,
                       'transaction_type', NEW.transaction_type
               )
           );
END//

DELIMITER ;

-- ============================================
-- VIEWS
-- ============================================

-- Concurrent sales statistics
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

-- Stock conflict report
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

-- User role distribution
CREATE OR REPLACE VIEW v_user_role_distribution AS
SELECT
    role,
    COUNT(*) as user_count,
    COUNT(CASE WHEN is_active = TRUE THEN 1 END) as active_users,
    COUNT(CASE WHEN is_active = FALSE THEN 1 END) as inactive_users
FROM users
GROUP BY role
ORDER BY FIELD(role, 'ADMIN', 'MANAGER', 'CASHIER', 'CUSTOMER');

-- Cashier performance
CREATE OR REPLACE VIEW v_cashier_performance AS
SELECT
    u.user_id,
    u.username,
    u.full_name,
    COUNT(DISTINCT s.id) as total_sales,
    SUM(s.total_amount) as total_revenue,
    AVG(s.total_amount) as avg_sale_amount,
    MIN(s.created_at) as first_sale,
    MAX(s.created_at) as last_sale
FROM users u
         LEFT JOIN sales s ON u.id = s.cashier_id
WHERE u.role IN ('CASHIER', 'MANAGER', 'ADMIN')
  AND s.status = 'COMPLETED'
GROUP BY u.user_id, u.username, u.full_name
ORDER BY total_revenue DESC;

-- Low stock items
CREATE OR REPLACE VIEW v_low_stock_items AS
SELECT
    code,
    name,
    category,
    (quantity_in_store + quantity_on_shelf) as total_quantity,
    reorder_level,
    (reorder_level - (quantity_in_store + quantity_on_shelf)) as units_needed,
    price
FROM items
WHERE (quantity_in_store + quantity_on_shelf) < reorder_level
  AND state != 'EXPIRED'
ORDER BY units_needed DESC;

-- Items expiring soon
CREATE OR REPLACE VIEW v_items_expiring_soon AS
SELECT
    code,
    name,
    category,
    expiry_date,
    DATEDIFF(expiry_date, CURDATE()) as days_remaining,
    (quantity_in_store + quantity_on_shelf) as total_quantity,
    price
FROM items
WHERE expiry_date IS NOT NULL
  AND DATEDIFF(expiry_date, CURDATE()) BETWEEN 0 AND 7
  AND state != 'EXPIRED'
ORDER BY expiry_date;

-- Daily sales summary
CREATE OR REPLACE VIEW v_daily_sales_summary AS
SELECT
    DATE(bill_date) as sale_date,
    COUNT(*) as total_bills,
    SUM(total_amount) as gross_revenue,
    SUM(discount) as total_discounts,
    SUM(total_amount - discount) as net_revenue,
    AVG(total_amount) as avg_bill_amount,
    COUNT(DISTINCT user_id_ref) as active_cashiers
FROM bills
WHERE status = 'COMPLETED'
GROUP BY DATE(bill_date)
ORDER BY sale_date DESC;

-- ============================================
-- INSERT DEFAULT USERS
-- ============================================

INSERT INTO users (user_id, username, email, password_hash, role, full_name) VALUES
                                                                                 ('U001', 'admin', 'admin@syos.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'System Administrator'),
                                                                                 ('U002', 'manager1', 'manager1@syos.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'MANAGER', 'Store Manager'),
                                                                                 ('U003', 'cashier1', 'cashier1@syos.com', '8d23cf6c86e834a7aa6eded54c26ce2bb2e74903538c61bdd5d2197997ab2f72', 'CASHIER', 'Cashier One'),
                                                                                 ('U004', 'cashier', 'cashier@syos.com', '8d23cf6c86e834a7aa6eded54c26ce2bb2e74903538c61bdd5d2197997ab2f72', 'CASHIER', 'Cashier User'),
                                                                                 ('U005', 'test', 'test@syos.com', 'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae', 'ADMIN', 'Test User'),
                                                                                 ('U006', 'manager2', 'manager2@syos.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'MANAGER', 'Assistant Manager'),
                                                                                 ('U007', 'cashier2', 'cashier2@syos.com', '8d23cf6c86e834a7aa6eded54c26ce2bb2e74903538c61bdd5d2197997ab2f72', 'CASHIER', 'Cashier Two'),
                                                                                 ('U008', 'customer1', 'customer1@syos.com', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'CUSTOMER', 'John Customer'),
                                                                                 ('U009', 'customer2', 'customer2@syos.com', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'CUSTOMER', 'Jane Customer');

-- ============================================
-- INSERT SAMPLE ITEMS
-- ============================================

INSERT INTO items (code, name, category, price, quantity_in_store, quantity_on_shelf, reorder_level, state, purchase_date, expiry_date) VALUES
                                                                                                                                            ('MILK001', 'Fresh Milk 1L', 'Dairy', 3.50, 50, 50, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
                                                                                                                                            ('YOGURT001', 'Plain Yogurt 500g', 'Dairy', 3.25, 30, 20, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 21 DAY)),
                                                                                                                                            ('CHEESE001', 'Cheddar Cheese 200g', 'Dairy', 6.50, 20, 20, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 45 DAY)),
                                                                                                                                            ('BUTTER001', 'Butter 250g', 'Dairy', 4.00, 30, 30, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
                                                                                                                                            ('BREAD001', 'White Bread', 'Bakery', 2.50, 60, 90, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
                                                                                                                                            ('BREAD002', 'Whole Wheat Bread', 'Bakery', 3.00, 50, 50, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
                                                                                                                                            ('RICE001', 'Basmati Rice 5kg', 'Grains', 15.00, 100, 100, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                                                                            ('RICE002', 'Jasmine Rice 5kg', 'Grains', 14.50, 75, 75, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                                                                            ('FLOUR001', 'All Purpose Flour 1kg', 'Grains', 2.50, 150, 150, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
                                                                                                                                            ('SUGAR001', 'White Sugar 1kg', 'Staples', 2.00, 200, 200, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                                                                            ('SALT001', 'Table Salt 500g', 'Staples', 1.00, 300, 150, 50, 'ON_SHELF', CURDATE(), NULL),
                                                                                                                                            ('OIL001', 'Cooking Oil 1L', 'Cooking', 5.50, 80, 80, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                                                                            ('EGGS001', 'Eggs Dozen', 'Poultry', 4.50, 100, 100, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 21 DAY)),
                                                                                                                                            ('CHICKEN001', 'Chicken Breast 1kg', 'Poultry', 12.00, 30, 30, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
                                                                                                                                            ('BEEF001', 'Ground Beef 500g', 'Meat', 8.50, 25, 25, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
                                                                                                                                            ('TOMATO001', 'Fresh Tomatoes 1kg', 'Vegetables', 3.00, 60, 60, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
                                                                                                                                            ('ONION001', 'Red Onions 1kg', 'Vegetables', 2.50, 80, 80, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY)),
                                                                                                                                            ('POTATO001', 'Potatoes 2kg', 'Vegetables', 4.00, 100, 100, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
                                                                                                                                            ('APPLE001', 'Red Apples 1kg', 'Fruits', 5.00, 50, 50, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 DAY)),
                                                                                                                                            ('BANANA001', 'Bananas 1kg', 'Fruits', 3.50, 70, 70, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
                                                                                                                                            ('ORANGE001', 'Oranges 1kg', 'Fruits', 4.50, 60, 60, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY)),
                                                                                                                                            ('PASTA001', 'Spaghetti 500g', 'Pasta', 2.50, 120, 120, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                                                                            ('SAUCE001', 'Tomato Sauce 500ml', 'Condiments', 3.00, 80, 80, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
                                                                                                                                            ('JUICE001', 'Orange Juice 1L', 'Beverages', 4.50, 50, 50, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
                                                                                                                                            ('SODA001', 'Cola 2L', 'Beverages', 3.00, 100, 100, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY)),
                                                                                                                                            ('WATER001', 'Mineral Water 1.5L', 'Beverages', 1.50, 200, 200, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                                                                            ('COFFEE001', 'Ground Coffee 250g', 'Beverages', 8.00, 40, 40, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
                                                                                                                                            ('TEA001', 'Black Tea 100 Bags', 'Beverages', 5.50, 60, 60, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                                                                            ('SOAP001', 'Hand Soap 500ml', 'Personal Care', 3.50, 80, 80, 50, 'ON_SHELF', CURDATE(), NULL),
                                                                                                                                            ('SHAMPOO001', 'Shampoo 400ml', 'Personal Care', 6.00, 50, 50, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 730 DAY)),
                                                                                                                                            ('TISSUE001', 'Tissue Box', 'Household', 2.50, 100, 100, 50, 'ON_SHELF', CURDATE(), NULL);

-- ============================================
-- INSERT SAMPLE BILLS
-- ============================================

INSERT INTO bills (bill_number, bill_date, total_amount, discount, cash_tendered, change_amount, transaction_type, user_id_ref, user_id) VALUES
                                                                                                                                             ('BILL000001', DATE_SUB(NOW(), INTERVAL 2 DAY), 25.50, 0.00, 30.00, 4.50, 'IN_STORE', 3, 'U003'),
                                                                                                                                             ('BILL000002', DATE_SUB(NOW(), INTERVAL 1 DAY), 48.75, 2.00, 50.00, 3.25, 'IN_STORE', 3, 'U003'),
                                                                                                                                             ('BILL000003', NOW(), 15.25, 0.00, 20.00, 4.75, 'IN_STORE', 4, 'U004');

INSERT INTO bill_items (bill_id, bill_number, item_code, item_name, quantity, unit_price) VALUES
                                                                                              (1, 'BILL000001', 'MILK001', 'Fresh Milk 1L', 2, 3.50),
                                                                                              (1, 'BILL000001', 'BREAD001', 'White Bread', 3, 2.50),
                                                                                              (1, 'BILL000001', 'EGGS001', 'Eggs Dozen', 2, 4.50),
                                                                                              (2, 'BILL000002', 'RICE001', 'Basmati Rice 5kg', 2, 15.00),
                                                                                              (2, 'BILL000002', 'OIL001', 'Cooking Oil 1L', 2, 5.50),
                                                                                              (2, 'BILL000002', 'SUGAR001', 'White Sugar 1kg', 4, 2.00),
                                                                                              (3, 'BILL000003', 'BREAD002', 'Whole Wheat Bread', 3, 3.00),
                                                                                              (3, 'BILL000003', 'BUTTER001', 'Butter 250g', 1, 4.00),
                                                                                              (3, 'BILL000003', 'CHEESE001', 'Cheddar Cheese 200g', 1, 6.50);

-- ============================================
-- COMPLETION MESSAGE
-- ============================================

SELECT '========================================' as '';
SELECT 'SYOS DATABASE SETUP COMPLETED!' as '';
SELECT '========================================' as '';
SELECT '' as '';
SELECT 'ROLES: ADMIN, MANAGER, CASHIER, CUSTOMER' as '';
SELECT 'USERS: 9 users created' as '';
SELECT 'ITEMS: 30 sample items loaded' as '';
SELECT 'CONCURRENCY: Optimistic + Pessimistic locking enabled' as '';
SELECT 'CLI COMPATIBLE: Yes' as '';
SELECT 'WEB COMPATIBLE: Yes' as '';
SELECT '' as '';
SELECT '========================================' as '';