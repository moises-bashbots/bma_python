#!/usr/bin/env python3
"""
Verify that all monitoring tables were created successfully.
"""

import json
import pymysql
from pathlib import Path


def load_config():
    """Load database configuration."""
    config_file = Path(__file__).parent / 'databases_config.json'
    with open(config_file, 'r') as f:
        return json.load(f)


def verify_tables():
    """Verify all monitoring tables exist."""
    config = load_config()
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
    
    expected_tables = [
        'apr_invalid_records',
        'apr_status_history',
        'apr_processing_log',
        'apr_proposal_products',
        'apr_daily_summary'
    ]
    
    print("=" * 80)
    print("Verifying Monitoring Tables")
    print("=" * 80)
    print()
    
    with connection.cursor() as cursor:
        # Get all tables
        cursor.execute("""
            SELECT TABLE_NAME, TABLE_ROWS, TABLE_COMMENT
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = %(schema)s
            AND TABLE_NAME LIKE 'apr_%%'
            ORDER BY TABLE_NAME
        """, {'schema': db_config['scheme']})
        
        tables = cursor.fetchall()
        
        if not tables:
            print("❌ No APR monitoring tables found!")
            return False
        
        print(f"Found {len(tables)} APR tables:\n")
        
        found_tables = []
        for table in tables:
            found_tables.append(table['TABLE_NAME'])
            status = "✅" if table['TABLE_NAME'] in expected_tables else "⚠️"
            print(f"{status} {table['TABLE_NAME']}")
            print(f"   Rows: {table['TABLE_ROWS']}")
            print(f"   Comment: {table['TABLE_COMMENT']}")
            print()
        
        # Check for missing tables
        missing = set(expected_tables) - set(found_tables)
        if missing:
            print("\n❌ Missing tables:")
            for table in missing:
                print(f"   - {table}")
            return False
        
        print("\n" + "=" * 80)
        print("✅ All 5 monitoring tables verified successfully!")
        print("=" * 80)
        return True
    
    connection.close()


if __name__ == '__main__':
    verify_tables()

