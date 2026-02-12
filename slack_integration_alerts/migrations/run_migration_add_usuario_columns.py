#!/usr/bin/env python3
"""
Migration Script: Add usuario_processar and usuario_confirmar columns
Date: 2026-02-12
Description: Adds two new columns to apr_valid_records table to track which user
             is selected in GER dropdown during processing and confirmation phases.
"""

import pymysql
import sys
from pathlib import Path

def run_migration():
    """Run the migration to add usuario columns to apr_valid_records table."""
    
    print("=" * 80)
    print("MIGRATION: Add usuario_processar and usuario_confirmar columns")
    print("=" * 80)
    print()
    
    # Database connection parameters
    db_config = {
        'host': 'localhost',
        'user': 'robot',
        'password': 'r0b0t',
        'database': 'BMA',
        'charset': 'utf8mb4'
    }
    
    try:
        # Connect to database
        print("Connecting to MariaDB...")
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        print("✓ Connected successfully\n")
        
        # Check if columns already exist
        print("Checking if columns already exist...")
        cursor.execute("""
            SELECT COUNT(*)
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'BMA'
              AND TABLE_NAME = 'apr_valid_records'
              AND COLUMN_NAME IN ('usuario_processar', 'usuario_confirmar')
        """)
        existing_count = cursor.fetchone()[0]
        
        if existing_count == 2:
            print("⚠ Columns already exist. Migration already applied.")
            cursor.close()
            conn.close()
            return True
        elif existing_count == 1:
            print("⚠ Warning: Only one column exists. Proceeding with migration...")
        else:
            print("✓ Columns do not exist. Proceeding with migration...\n")
        
        # Add usuario_processar column
        print("Adding usuario_processar column...")
        cursor.execute("""
            ALTER TABLE apr_valid_records
            ADD COLUMN IF NOT EXISTS usuario_processar VARCHAR(100) DEFAULT NULL
            COMMENT 'Username selected in GER dropdown when clicking Processar button'
        """)
        print("✓ usuario_processar column added\n")
        
        # Add usuario_confirmar column
        print("Adding usuario_confirmar column...")
        cursor.execute("""
            ALTER TABLE apr_valid_records
            ADD COLUMN IF NOT EXISTS usuario_confirmar VARCHAR(100) DEFAULT NULL
            COMMENT 'Username selected in GER dropdown during confirmation check'
        """)
        print("✓ usuario_confirmar column added\n")
        
        # Commit changes
        conn.commit()
        print("✓ Changes committed\n")
        
        # Verify columns were added
        print("Verifying columns...")
        cursor.execute("""
            SELECT 
                COLUMN_NAME,
                COLUMN_TYPE,
                IS_NULLABLE,
                COLUMN_DEFAULT,
                COLUMN_COMMENT
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'monitoring_db'
              AND TABLE_NAME = 'apr_valid_records'
              AND COLUMN_NAME IN ('usuario_processar', 'usuario_confirmar')
            ORDER BY COLUMN_NAME
        """)
        
        results = cursor.fetchall()
        if len(results) == 2:
            print("✓ Verification successful!\n")
            print("Column Details:")
            print("-" * 80)
            for row in results:
                print(f"  Column: {row[0]}")
                print(f"  Type: {row[1]}")
                print(f"  Nullable: {row[2]}")
                print(f"  Default: {row[3]}")
                print(f"  Comment: {row[4]}")
                print()
        else:
            print("✗ Verification failed! Expected 2 columns, found", len(results))
            cursor.close()
            conn.close()
            return False
        
        # Close connection
        cursor.close()
        conn.close()
        
        print("=" * 80)
        print("✓ MIGRATION COMPLETED SUCCESSFULLY")
        print("=" * 80)
        return True
        
    except pymysql.Error as e:
        print(f"\n✗ Database error: {e}")
        print("=" * 80)
        print("✗ MIGRATION FAILED")
        print("=" * 80)
        return False
    except Exception as e:
        print(f"\n✗ Unexpected error: {e}")
        print("=" * 80)
        print("✗ MIGRATION FAILED")
        print("=" * 80)
        return False

if __name__ == "__main__":
    success = run_migration()
    sys.exit(0 if success else 1)

