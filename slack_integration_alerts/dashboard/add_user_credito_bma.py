#!/usr/bin/env python3
"""
Add credito_bma user to dashboard_users table
"""

import pymysql
import bcrypt
import json

def load_db_config():
    """Load MariaDB configuration from databases_config.json"""
    config_path = '/home/robot/Dev/bma_python/slack_integration_alerts/databases_config.json'
    with open(config_path, 'r') as f:
        config = json.load(f)
    return config['databases']['mariadb']

def hash_password(password: str) -> str:
    """Hash a password using bcrypt"""
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def add_user():
    """Add credito_bma user to database"""
    config = load_db_config()
    
    conn = pymysql.connect(
        host=config['server'],
        port=config['port'],
        user=config['user'],
        password=config['password'],
        database=config['scheme']
    )
    
    try:
        with conn.cursor() as cursor:
            # Hash the password
            password_hash = hash_password('oXU3Fco2p09QfviqIARa')
            
            # Insert the new user
            cursor.execute("""
                INSERT INTO dashboard_users (username, password_hash, full_name, email, is_active)
                VALUES (%s, %s, %s, %s, %s)
            """, (
                'credito_bma',
                password_hash,
                'Crédito BMA',
                'credito@bmafidc.com.br',
                True
            ))
            
            conn.commit()
            print("✓ User 'credito_bma' created successfully!")
            print("  Username: credito_bma")
            print("  Password: oXU3Fco2p09QfviqIARa")
            print("  Full Name: Crédito BMA")
            print("  Email: credito@bmafidc.com.br")
            
    except pymysql.err.IntegrityError as e:
        if 'Duplicate entry' in str(e):
            print("⚠ User 'credito_bma' already exists!")
        else:
            print(f"✗ Error: {e}")
    except Exception as e:
        print(f"✗ Error creating user: {e}")
    finally:
        conn.close()

if __name__ == '__main__':
    print("Adding user 'credito_bma' to dashboard_users table...\n")
    add_user()

