#!/usr/bin/env python3
"""
Create authentication table and default users for the dashboard.
"""

import sys
import os
import bcrypt
import pymysql

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app import get_db_connection

def create_auth_table():
    """Create the dashboard_users table."""
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            # Create table
            cursor.execute("""
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """)
            conn.commit()
            print("✓ Table 'dashboard_users' created successfully")
    finally:
        conn.close()

def hash_password(password: str) -> str:
    """Hash a password using bcrypt."""
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def create_default_users():
    """Create default users."""
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            # Create admin user (password: admin123)
            admin_hash = hash_password('admin123')
            cursor.execute("""
                INSERT INTO dashboard_users (username, password_hash, full_name, email)
                VALUES (%s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash)
            """, ('admin', admin_hash, 'Administrador', 'admin@bmafidc.com.br'))
            
            # Create robot user (password: robot123)
            robot_hash = hash_password('robot123')
            cursor.execute("""
                INSERT INTO dashboard_users (username, password_hash, full_name, email)
                VALUES (%s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash)
            """, ('robot', robot_hash, 'Robot User', 'robot@bmafidc.com.br'))
            
            conn.commit()
            print("✓ Default users created successfully")
            print("  - Username: admin, Password: admin123")
            print("  - Username: robot, Password: robot123")
    finally:
        conn.close()

if __name__ == '__main__':
    print("Creating authentication infrastructure...")
    print()
    
    create_auth_table()
    create_default_users()
    
    print()
    print("✓ Authentication setup complete!")
    print()
    print("You can now login to the dashboard with:")
    print("  Username: admin")
    print("  Password: admin123")

