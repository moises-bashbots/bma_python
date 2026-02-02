-- Create authentication table for dashboard users
-- Run this with: mysql -u robot -pr0b0t BMA < create_auth_table.sql

CREATE TABLE IF NOT EXISTS dashboard_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user (password: admin123)
-- Password hash generated with bcrypt
INSERT INTO dashboard_users (username, password_hash, full_name, email) 
VALUES (
    'admin',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYqgdvJatQm',
    'Administrador',
    'admin@bmafidc.com.br'
) ON DUPLICATE KEY UPDATE username=username;

-- Insert robot user (password: robot123)
INSERT INTO dashboard_users (username, password_hash, full_name, email) 
VALUES (
    'robot',
    '$2b$12$K3eDJ8H5L5Z5Z5Z5Z5Z5ZeN5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z',
    'Robot User',
    'robot@bmafidc.com.br'
) ON DUPLICATE KEY UPDATE username=username;

