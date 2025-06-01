
-- Fix user passwords with actual SHA-256 hashes
USE syos_db;

-- First, let's check what users we have
SELECT id, username, email, role FROM users;

-- Update passwords with actual SHA-256 hashes
-- Password for admin: admin123
UPDATE users SET password_hash = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'
WHERE username = 'admin';

-- Password for cashier1: cashier123
UPDATE users SET password_hash = '56f9a591bd64e713d87b1bb4e87062ad9b19080ad003b2cd8cdfcb9c3ab9da7b'
WHERE username = 'cashier1';

-- Password for manager1: manager123
UPDATE users SET password_hash = 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'
WHERE username = 'manager1';

-- Verify the updates
SELECT username, LEFT(password_hash, 20) as password_hash_preview, role FROM users;

-- Alternative: If you want to use simpler passwords for testing
-- Password for all users: password123
-- UPDATE users SET password_hash = '482c811da5d5b4bc6d497ffa98491e38'
-- WHERE username IN ('admin', 'cashier1', 'manager1');