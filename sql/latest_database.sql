-- unified_complete_schema.sql
-- Combined schema for CLI and Web Application using same database

DROP DATABASE IF EXISTS syos_db;
CREATE DATABASE syos_db;
USE syos_db;

-- ============================================
-- USERS TABLE (Unified for CLI and Web)
-- ============================================
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       user_id VARCHAR(50) UNIQUE NOT NULL,  -- For web app compatibility
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100),
                       password_hash VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN', 'CASHIER', 'MANAGER') NOT NULL,
                       full_name VARCHAR(100),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP NULL,
                       INDEX idx_user_id (user_id),
                       INDEX idx_username (username),
                       INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- ITEMS TABLE (Unified for CLI and Web)
-- ============================================
CREATE TABLE items (
                       code VARCHAR(20) PRIMARY KEY,           -- CLI uses this
                       item_code VARCHAR(20) AS (code) STORED, -- Virtual column for web compatibility
                       name VARCHAR(100) NOT NULL,
                       category VARCHAR(50) DEFAULT 'General',
                       price DECIMAL(10, 2) NOT NULL,
                       quantity_in_store INT NOT NULL DEFAULT 0,
                       quantity_on_shelf INT NOT NULL DEFAULT 0,
                       quantity INT AS (quantity_in_store + quantity_on_shelf) STORED, -- Total for CLI
                       reorder_level INT DEFAULT 50,
                       state ENUM('IN_STORE', 'ON_SHELF', 'EXPIRED', 'SOLD_OUT') NOT NULL DEFAULT 'IN_STORE',
                       purchase_date DATE NOT NULL DEFAULT (CURDATE()),
                       expiry_date DATE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_state (state),
                       INDEX idx_expiry (expiry_date),
                       INDEX idx_category (category),
                       INDEX idx_quantity_on_shelf (quantity_on_shelf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BILLS TABLE (Unified for CLI and Web)
-- ============================================
CREATE TABLE bills (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       bill_number VARCHAR(50) UNIQUE NOT NULL, -- For web app
                       bill_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       total_amount DECIMAL(10, 2) NOT NULL,
                       discount DECIMAL(10, 2) DEFAULT 0.00,
                       cash_tendered DECIMAL(10, 2) NOT NULL,    -- CLI uses this
                       cash_received DECIMAL(10, 2) AS (cash_tendered) STORED, -- Web uses this
                       change_amount DECIMAL(10, 2) NOT NULL,
                       transaction_type ENUM('IN_STORE', 'ONLINE', 'OFFLINE') NOT NULL DEFAULT 'IN_STORE',
                       user_id_ref BIGINT,                       -- CLI foreign key
                       user_id VARCHAR(50),                      -- Web uses this
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (user_id_ref) REFERENCES users(id) ON DELETE SET NULL,
                       INDEX idx_bill_number (bill_number),
                       INDEX idx_bill_date (bill_date),
                       INDEX idx_transaction_type (transaction_type),
                       INDEX idx_user_id (user_id),
                       INDEX idx_user_id_ref (user_id_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BILL ITEMS TABLE (Unified)
-- ============================================
CREATE TABLE bill_items (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            bill_id BIGINT,                           -- CLI uses this
                            bill_number VARCHAR(50),                  -- Web uses this
                            item_code VARCHAR(20) NOT NULL,
                            item_name VARCHAR(100),                   -- Web includes item name
                            quantity INT NOT NULL,
                            unit_price DECIMAL(10, 2) NOT NULL,
                            total_price DECIMAL(10, 2) AS (quantity * unit_price) STORED, -- CLI
                            subtotal DECIMAL(10, 2) AS (quantity * unit_price) STORED,    -- Web
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
                            FOREIGN KEY (item_code) REFERENCES items(code) ON DELETE RESTRICT,
                            INDEX idx_bill_id (bill_id),
                            INDEX idx_bill_number (bill_number),
                            INDEX idx_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- STOCK MOVEMENTS TABLE (CLI feature)
-- ============================================
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

-- ============================================
-- AUDIT LOG TABLE (CLI feature)
-- ============================================
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

-- ============================================
-- INSERT DEFAULT USERS
-- ============================================
INSERT INTO users (user_id, username, email, password_hash, role, full_name) VALUES
-- Password: admin123 (SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
('U001', 'admin', 'admin@syos.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'System Administrator'),

-- Password: manager123 (SHA-256: 0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c)
('U002', 'manager1', 'manager1@syos.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'MANAGER', 'Store Manager'),

-- Password: cashier123 (SHA-256: 8d23cf6c86e834a7aa6eded54c26ce2bb2e74903538c61bdd5d2197997ab2f72)
('U003', 'cashier1', 'cashier1@syos.com', '8d23cf6c86e834a7aa6eded54c26ce2bb2e74903538c61bdd5d2197997ab2f72', 'CASHIER', 'Cashier One'),

-- Password: cashier123 (SHA-256: 8d23cf6c86e834a7aa6eded54c26ce2bb2e74903538c61bdd5d2197997ab2f72)
('U004', 'cashier', 'cashier@syos.com', '8d23cf6c86e834a7aa6eded54c26ce2bb2e74903538c61bdd5d2197997ab2f72', 'CASHIER', 'Cashier User'),

-- Password: test123 (SHA-256: ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae)
('U005', 'test', 'test@syos.com', 'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae', 'ADMIN', 'Test User');

-- ============================================
-- INSERT SAMPLE ITEMS
-- ============================================
INSERT INTO items (code, name, category, price, quantity_in_store, quantity_on_shelf, reorder_level, state, purchase_date, expiry_date) VALUES
-- Dairy Products
('MILK001', 'Fresh Milk 1L', 'Dairy', 3.50, 50, 50, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
('YOGURT001', 'Plain Yogurt 500g', 'Dairy', 3.25, 30, 20, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 21 DAY)),
('CHEESE001', 'Cheddar Cheese 200g', 'Dairy', 6.50, 20, 20, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 45 DAY)),
('BUTTER001', 'Butter 250g', 'Dairy', 4.00, 30, 30, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY)),

-- Bakery
('BREAD001', 'White Bread', 'Bakery', 2.50, 60, 90, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
('BREAD002', 'Whole Wheat Bread', 'Bakery', 3.00, 50, 50, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),

-- Staples & Grains
('RICE001', 'Basmati Rice 5kg', 'Grains', 15.00, 100, 100, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
('RICE002', 'Jasmine Rice 5kg', 'Grains', 14.50, 75, 75, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
('FLOUR001', 'All Purpose Flour 1kg', 'Grains', 3.00, 125, 125, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
('SUGAR001', 'White Sugar 1kg', 'Grains', 2.00, 150, 150, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 730 DAY)),

-- Eggs and Cooking Oil
('EGGS001', 'Eggs Dozen', 'Dairy', 4.50, 40, 40, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY)),
('OIL001', 'Cooking Oil 1L', 'Cooking', 5.50, 60, 60, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY)),

-- Spices (Low stock for testing)
('SALT001', 'Table Salt 1kg', 'Spices', 1.50, 15, 15, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 730 DAY)),
('PEPPER001', 'Black Pepper 100g', 'Spices', 3.50, 10, 15, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),

-- Fresh Produce (Expiring soon for testing)
('BANANA001', 'Bananas 1kg', 'Produce', 2.50, 25, 25, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
('TOMATO001', 'Fresh Tomatoes 1kg', 'Produce', 3.00, 20, 20, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY)),

-- Additional items
('PASTA001', 'Spaghetti 500g', 'Grains', 2.75, 80, 70, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
('SOAP001', 'Hand Soap 500ml', 'Household', 3.50, 50, 50, 50, 'ON_SHELF', CURDATE(), NULL),
('DETERGENT001', 'Laundry Detergent 1L', 'Household', 8.50, 40, 40, 50, 'ON_SHELF', CURDATE(), NULL),
('TISSUE001', 'Tissue Box', 'Household', 2.25, 100, 100, 50, 'ON_SHELF', CURDATE(), NULL);

-- ============================================
-- VIEWS FOR REPORTING
-- ============================================

-- Daily sales summary (CLI compatible)
CREATE OR REPLACE VIEW daily_sales_summary AS
SELECT
    DATE(b.bill_date) as sale_date,
    COUNT(DISTINCT b.id) as total_transactions,
    SUM(b.total_amount) as gross_revenue,
    SUM(b.discount) as total_discount,
    SUM(b.total_amount - b.discount) as net_revenue,
    COUNT(DISTINCT bi.item_code) as unique_items_sold,
    SUM(bi.quantity) as total_items_sold
FROM bills b
         LEFT JOIN bill_items bi ON b.id = bi.bill_id
GROUP BY DATE(b.bill_date);

-- Low stock items
CREATE OR REPLACE VIEW low_stock_items AS
SELECT
    code,
    name,
    category,
    quantity_on_shelf,
    quantity_in_store,
    (quantity_in_store + quantity_on_shelf) as total_quantity,
    reorder_level,
    state,
    price
FROM items
WHERE (quantity_in_store + quantity_on_shelf) < reorder_level
  AND state NOT IN ('EXPIRED', 'SOLD_OUT')
ORDER BY (quantity_in_store + quantity_on_shelf) ASC;

-- Items expiring soon
CREATE OR REPLACE VIEW expiring_soon AS
SELECT
    code,
    name,
    category,
    quantity_on_shelf,
    expiry_date,
    state,
    DATEDIFF(expiry_date, CURDATE()) as days_until_expiry
FROM items
WHERE expiry_date IS NOT NULL
  AND DATEDIFF(expiry_date, CURDATE()) BETWEEN 0 AND 7
  AND state NOT IN ('EXPIRED', 'SOLD_OUT')
ORDER BY expiry_date ASC;

-- Inventory value by state
CREATE OR REPLACE VIEW inventory_value AS
SELECT
    state,
    COUNT(*) as item_count,
    SUM(quantity_in_store + quantity_on_shelf) as total_quantity,
    SUM(price * (quantity_in_store + quantity_on_shelf)) as total_value
FROM items
WHERE state NOT IN ('EXPIRED', 'SOLD_OUT')
GROUP BY state
WITH ROLLUP;

-- Items on shelf (for web app)
CREATE OR REPLACE VIEW items_on_shelf AS
SELECT
    code as item_code,
    name,
    category,
    price,
    quantity_on_shelf,
    state
FROM items
WHERE quantity_on_shelf > 0 AND state = 'ON_SHELF'
ORDER BY name;

-- ============================================
-- STORED PROCEDURES
-- ============================================

DELIMITER //

-- Record a sale and update inventory
CREATE PROCEDURE sp_record_sale(
    IN p_bill_id BIGINT,
    IN p_item_code VARCHAR(20),
    IN p_quantity INT
)
BEGIN
    DECLARE current_shelf_qty INT;

    -- Check available quantity
    SELECT quantity_on_shelf INTO current_shelf_qty
    FROM items
    WHERE code = p_item_code;

    IF current_shelf_qty >= p_quantity THEN
        -- Update item quantity
        UPDATE items
        SET quantity_on_shelf = quantity_on_shelf - p_quantity,
            updated_at = CURRENT_TIMESTAMP
        WHERE code = p_item_code;

        -- Record stock movement
        INSERT INTO stock_movements (item_code, movement_type, quantity, from_state, to_state, notes)
        VALUES (p_item_code, 'SALE', p_quantity, 'ON_SHELF', 'ON_SHELF', CONCAT('Bill ID: ', p_bill_id));

        -- Check if sold out
        UPDATE items
        SET state = 'SOLD_OUT'
        WHERE code = p_item_code
          AND (quantity_on_shelf + quantity_in_store) = 0;
    ELSE
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient stock on shelf';
    END IF;
END//

-- Move items from store to shelf
CREATE PROCEDURE sp_move_to_shelf(
    IN p_item_code VARCHAR(20),
    IN p_quantity INT,
    IN p_user_id BIGINT
)
BEGIN
    DECLARE current_store_qty INT;

    -- Check available quantity in store
    SELECT quantity_in_store INTO current_store_qty
    FROM items
    WHERE code = p_item_code;

    IF current_store_qty >= p_quantity THEN
        -- Move from store to shelf
        UPDATE items
        SET quantity_in_store = quantity_in_store - p_quantity,
            quantity_on_shelf = quantity_on_shelf + p_quantity,
            state = 'ON_SHELF',
            updated_at = CURRENT_TIMESTAMP
        WHERE code = p_item_code;

        -- Record stock movement
        INSERT INTO stock_movements (item_code, movement_type, quantity, from_state, to_state, user_id, notes)
        VALUES (p_item_code, 'SHELF', p_quantity, 'IN_STORE', 'ON_SHELF', p_user_id, 'Moved to shelf');
    ELSE
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient stock in store';
    END IF;
END//

-- Expire old items
CREATE PROCEDURE sp_expire_items()
BEGIN
    -- Update expired items
    UPDATE items
    SET state = 'EXPIRED',
        updated_at = CURRENT_TIMESTAMP
    WHERE expiry_date < CURDATE()
      AND state NOT IN ('EXPIRED', 'SOLD_OUT');

    -- Record expiry movements
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

-- Generate bill number for web app
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
-- TRIGGERS FOR AUDIT LOG
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
-- SAMPLE BILLS (for testing)
-- ============================================
INSERT INTO bills (bill_number, bill_date, total_amount, discount, cash_tendered, change_amount, transaction_type, user_id_ref, user_id)
VALUES
    ('BILL000001', DATE_SUB(NOW(), INTERVAL 2 DAY), 25.50, 0.00, 30.00, 4.50, 'IN_STORE', 3, 'U003'),
    ('BILL000002', DATE_SUB(NOW(), INTERVAL 1 DAY), 48.75, 2.00, 50.00, 3.25, 'IN_STORE', 3, 'U003'),
    ('BILL000003', NOW(), 15.25, 0.00, 20.00, 4.75, 'IN_STORE', 4, 'U004');

INSERT INTO bill_items (bill_id, bill_number, item_code, item_name, quantity, unit_price)
VALUES
-- Bill 1
(1, 'BILL000001', 'MILK001', 'Fresh Milk 1L', 2, 3.50),
(1, 'BILL000001', 'BREAD001', 'White Bread', 3, 2.50),
(1, 'BILL000001', 'EGGS001', 'Eggs Dozen', 2, 4.50),

-- Bill 2
(2, 'BILL000002', 'RICE001', 'Basmati Rice 5kg', 2, 15.00),
(2, 'BILL000002', 'OIL001', 'Cooking Oil 1L', 2, 5.50),
(2, 'BILL000002', 'SUGAR001', 'White Sugar 1kg', 4, 2.00),

-- Bill 3
(3, 'BILL000003', 'BREAD002', 'Whole Wheat Bread', 3, 3.00),
(3, 'BILL000003', 'BUTTER001', 'Butter 250g', 1, 4.00),
(3, 'BILL000003', 'CHEESE001', 'Cheddar Cheese 200g', 1, 6.50);

-- ============================================
-- DISPLAY SETUP SUMMARY
-- ============================================
SELECT '========================================' as '';
SELECT 'DATABASE SETUP COMPLETED SUCCESSFULLY!' as 'STATUS';
SELECT '========================================' as '';

SELECT 'User Accounts Created:' as '';
SELECT
    user_id,
    username,
    role,
    full_name,
    CASE username
        WHEN 'admin' THEN 'admin123'
        WHEN 'manager1' THEN 'manager123'
        WHEN 'cashier1' THEN 'cashier123'
        WHEN 'cashier' THEN 'cashier123'
        WHEN 'test' THEN 'test123'
        END as password
FROM users
ORDER BY role, username;

SELECT '' as '';
SELECT 'Database Statistics:' as '';
SELECT
    (SELECT COUNT(*) FROM users) as 'Total Users',
    (SELECT COUNT(*) FROM items) as 'Total Items',
    (SELECT COUNT(*) FROM bills) as 'Total Bills',
    (SELECT SUM(quantity_in_store + quantity_on_shelf) FROM items) as 'Total Stock Items';

SELECT '' as '';
SELECT 'Low Stock Items (< reorder level):' as '';
SELECT code, name, (quantity_in_store + quantity_on_shelf) as total_qty, reorder_level
FROM items
WHERE (quantity_in_store + quantity_on_shelf) < reorder_level
LIMIT 5;

SELECT '' as '';
SELECT 'Items Expiring Soon:' as '';
SELECT code, name, expiry_date, DATEDIFF(expiry_date, CURDATE()) as days_remaining
FROM items
WHERE expiry_date IS NOT NULL
  AND DATEDIFF(expiry_date, CURDATE()) BETWEEN 0 AND 7
ORDER BY expiry_date
LIMIT 5;

SELECT '' as '';
SELECT 'Database: syos_db' as 'CURRENT DATABASE';
SELECT 'Ready for CLI and Web Application!' as 'STATUS';