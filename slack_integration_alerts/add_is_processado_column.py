#!/usr/bin/env python3
"""
Add is_processado column to apr_valid_records table.

This script adds a new TINYINT column to track whether a record has been processed.
The column will be used as a boolean (0 = not processed, 1 = processed).
"""

import json
import sys
from pathlib import Path
import pymysql


def load_config() -> dict:
    """Load database configuration."""
    config_path = Path(__file__).parent / "databases_config.json"
    
    if not config_path.exists():
        raise FileNotFoundError(f"Configuration file not found: {config_path}")
    
    with open(config_path, 'r') as f:
        return json.load(f)


def create_mariadb_connection(config):
    """Create MariaDB connection."""
    db_config = config['databases']['mariadb']
    
    connection = pymysql.connect(
        host=db_config['server'],
        port=db_config['port'],
        user=db_config['user'],
        password=db_config['password'],
        database=db_config['scheme'],
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )
    
    return connection


def add_is_processado_column(connection):
    """
    Add is_processado column to apr_valid_records table.
    
    Column: is_processado TINYINT DEFAULT 0
    - 0 = not processed (default)
    - 1 = processed
    """
    
    # Check if column already exists
    check_column_sql = """
    SELECT COUNT(*) as count
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'apr_valid_records'
      AND COLUMN_NAME = 'is_processado'
    """
    
    with connection.cursor() as cursor:
        cursor.execute(check_column_sql)
        result = cursor.fetchone()
        
        if result['count'] > 0:
            print("⚠️  Column 'is_processado' already exists in table 'apr_valid_records'")
            return False
    
    # Add the column
    alter_table_sql = """
    ALTER TABLE apr_valid_records
    ADD COLUMN is_processado TINYINT DEFAULT 0 COMMENT 'Processing status: 0=not processed, 1=processed'
    """
    
    with connection.cursor() as cursor:
        print("Adding column 'is_processado' to table 'apr_valid_records'...")
        cursor.execute(alter_table_sql)
        connection.commit()
        print("✓ Column added successfully!")
        
    # Add index for better query performance
    add_index_sql = """
    CREATE INDEX idx_is_processado ON apr_valid_records (is_processado)
    """
    
    try:
        with connection.cursor() as cursor:
            print("Adding index on 'is_processado' column...")
            cursor.execute(add_index_sql)
            connection.commit()
            print("✓ Index created successfully!")
    except pymysql.err.OperationalError as e:
        if "Duplicate key name" in str(e):
            print("⚠️  Index 'idx_is_processado' already exists")
        else:
            raise
    
    return True


def verify_column(connection):
    """Verify that the column was added correctly."""
    verify_sql = """
    SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'apr_valid_records'
      AND COLUMN_NAME = 'is_processado'
    """
    
    with connection.cursor() as cursor:
        cursor.execute(verify_sql)
        result = cursor.fetchone()
        
        if result:
            print("\n" + "=" * 80)
            print("COLUMN VERIFICATION")
            print("=" * 80)
            print(f"Column Name:    {result['COLUMN_NAME']}")
            print(f"Column Type:    {result['COLUMN_TYPE']}")
            print(f"Default Value:  {result['COLUMN_DEFAULT']}")
            print(f"Comment:        {result['COLUMN_COMMENT']}")
            print("=" * 80)
            return True
        else:
            print("❌ Column verification failed!")
            return False


def main():
    """Main entry point."""
    print("=" * 80)
    print("ADD is_processado COLUMN TO apr_valid_records TABLE")
    print("=" * 80)
    print()
    
    try:
        # Load configuration
        config = load_config()
        
        # Connect to MariaDB
        connection = create_mariadb_connection(config)
        print(f"✓ Connected to MariaDB: {config['databases']['mariadb']['server']}")
        print()
        
        # Add column
        added = add_is_processado_column(connection)
        print()
        
        # Verify column
        if added or True:  # Always verify
            verify_column(connection)
        
        # Close connection
        connection.close()
        print("\n✓ Migration completed successfully!")
        
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()

