#!/usr/bin/env python3
"""
Database Connector - Connects to MariaDB and MSSQL databases
Reads configuration from databases_config.json
"""

import json
import os
import sys
from pathlib import Path


def get_config_path():
    """Get the path to the configuration file."""
    # Check if running as a binary (PyInstaller)
    if getattr(sys, 'frozen', False):
        base_path = Path(sys.executable).parent
    else:
        base_path = Path(__file__).parent
    
    config_path = base_path / "databases_config.json"
    
    if not config_path.exists():
        print(f"Error: Configuration file not found at {config_path}")
        sys.exit(1)
    
    return config_path


def load_config():
    """Load database configuration from JSON file."""
    config_path = get_config_path()
    print(f"Loading configuration from: {config_path}")
    
    with open(config_path, 'r') as f:
        return json.load(f)


def test_mariadb(config: dict) -> bool:
    """Test connection to MariaDB database."""
    print("\n" + "=" * 50)
    print("Testing MariaDB connection...")
    print("=" * 50)
    
    try:
        import mysql.connector
        
        cfg = config['databases']['mariadb']
        print(f"  Server: {cfg['server']}:{cfg['port']}")
        print(f"  Database: {cfg['scheme']}")
        print(f"  User: {cfg['user']}")
        
        conn = mysql.connector.connect(
            host=cfg['server'],
            port=cfg['port'],
            user=cfg['user'],
            password=cfg['password'],
            database=cfg['scheme'],
            connection_timeout=15
        )
        
        cursor = conn.cursor()
        cursor.execute('SELECT VERSION()')
        version = cursor.fetchone()
        print(f"\n✓ MariaDB connection successful!")
        print(f"  Server version: {version[0]}")
        
        cursor.close()
        conn.close()
        return True
        
    except Exception as e:
        print(f"\n✗ MariaDB connection failed: {e}")
        return False


def test_mssql(config: dict) -> bool:
    """Test connection to MSSQL database."""
    print("\n" + "=" * 50)
    print("Testing MSSQL connection...")
    print("=" * 50)
    
    try:
        import pymssql
        
        cfg = config['databases']['mssql']
        print(f"  Server: {cfg['server']}:{cfg['port']}")
        print(f"  Database: {cfg['scheme']}")
        print(f"  User: {cfg['user']}")
        
        conn = pymssql.connect(
            server=cfg['server'],
            port=cfg['port'],
            user=cfg['user'],
            password=cfg['password'],
            database=cfg['scheme'],
            login_timeout=15
        )
        
        cursor = conn.cursor()
        cursor.execute('SELECT @@VERSION')
        version = cursor.fetchone()
        print(f"\n✓ MSSQL connection successful!")
        print(f"  Server version: {version[0][:100]}...")
        
        cursor.close()
        conn.close()
        return True
        
    except Exception as e:
        print(f"\n✗ MSSQL connection failed: {e}")
        return False


def main():
    """Main entry point."""
    print("=" * 50)
    print("Database Connection Tester")
    print("=" * 50)
    
    config = load_config()
    
    mariadb_ok = test_mariadb(config)
    mssql_ok = test_mssql(config)
    
    print("\n" + "=" * 50)
    print("Summary")
    print("=" * 50)
    print(f"  MariaDB: {'✓ Connected' if mariadb_ok else '✗ Failed'}")
    print(f"  MSSQL:   {'✓ Connected' if mssql_ok else '✗ Failed'}")
    print("=" * 50)
    
    # Return exit code based on results
    if mariadb_ok and mssql_ok:
        print("\nAll connections successful!")
        return 0
    else:
        print("\nSome connections failed!")
        return 1


if __name__ == "__main__":
    sys.exit(main())

