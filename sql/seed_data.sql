-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, role) VALUES
                                                             ('admin', 'admin@syos.com', 'sha256_hash_of_admin123', 'ADMIN'),
                                                             ('cashier1', 'cashier1@syos.com', 'sha256_hash_of_cashier123', 'CASHIER'),
                                                             ('manager1', 'manager1@syos.com', 'manager1', 'MANAGER');

-- Insert sample items
INSERT INTO items (code, name, price, quantity, state, purchase_date, expiry_date) VALUES
                                                                                       ('MILK001', 'Fresh Milk 1L', 3.50, 100, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
                                                                                       ('BREAD001', 'White Bread', 2.50, 150, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
                                                                                       ('RICE001', 'Basmati Rice 5kg', 15.00, 200, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                       ('EGGS001', 'Eggs (Dozen)', 4.50, 80, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY)),
                                                                                       ('SUGAR001', 'White Sugar 1kg', 2.00, 300, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 730 DAY)),
                                                                                       ('OIL001', 'Cooking Oil 1L', 5.50, 120, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
                                                                                       ('FLOUR001', 'All Purpose Flour 1kg', 3.00, 250, 'IN_STORE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
                                                                                       ('BUTTER001', 'Butter 250g', 4.00, 60, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
                                                                                       ('CHEESE001', 'Cheddar Cheese 200g', 6.50, 40, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 45 DAY)),
                                                                                       ('YOGURT001', 'Plain Yogurt 500g', 3.25, 50, 'ON_SHELF', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 21 DAY));

-- Create views for reporting
CREATE OR REPLACE VIEW daily_sales_summary AS
SELECT
    DATE(b.bill_date) as sale_date,
    COUNT(DISTINCT b.bill_number) as total_transactions,
    SUM(b.total_amount) as gross_revenue,
    SUM(b.discount) as total_discount,
    SUM(b.total_amount - b.discount) as net_revenue,
    COUNT(DISTINCT bi.item_code) as unique_items_sold
FROM bills b
    JOIN bill_items bi ON b.bill_number = bi.bill_number
GROUP BY DATE(b.bill_date);

CREATE OR REPLACE VIEW low_stock_items AS
SELECT
    code,
    name,
    quantity,
    state
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
    DATEDIFF(expiry_date, CURDATE()) as days_until_expiry
FROM items
WHERE expiry_date IS NOT NULL
  AND DATEDIFF(expiry_date, CURDATE()) BETWEEN 0 AND 7
  AND state NOT IN ('EXPIRED', 'SOLD_OUT')
ORDER BY expiry_date ASC;




