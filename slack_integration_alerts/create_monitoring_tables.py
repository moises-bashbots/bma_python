#!/usr/bin/env python3
"""
Create monitoring tables for APR proposal tracking dashboard.

Tables created:
1. apr_invalid_records - Track invalid records and resolution
2. apr_status_history - Track status changes over time
3. apr_processing_log - Log each program execution
4. apr_daily_summary - Daily aggregated metrics

Usage:
    python3 create_monitoring_tables.py
"""

import json
import pymysql
from pathlib import Path


def load_config():
    """Load database configuration from databases_config.json."""
    config_file = Path(__file__).parent / 'databases_config.json'

    with open(config_file, 'r') as f:
        config = json.load(f)

    return config


def create_tables():
    """Create all monitoring tables."""
    config = load_config()
    db_config = config['databases']['mariadb']

    print("=" * 80)
    print("APR Monitoring Tables Creation")
    print("=" * 80)
    print(f"\nConnecting to MariaDB...")
    print(f"Host: {db_config['server']}")
    print(f"Database: {db_config['scheme']}")

    connection = pymysql.connect(
        host=db_config['server'],
        port=db_config['port'],
        user=db_config['user'],
        password=db_config['password'],
        database=db_config['scheme'],
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )
    
    try:
        # Read SQL file
        sql_file = Path(__file__).parent / 'create_monitoring_tables.sql'
        with open(sql_file, 'r') as f:
            sql_content = f.read()

        # Remove comment lines
        lines = []
        for line in sql_content.split('\n'):
            stripped = line.strip()
            if not stripped.startswith('--') and stripped:
                lines.append(line)

        clean_sql = '\n'.join(lines)

        # Split by semicolons and execute each statement
        statements = [s.strip() for s in clean_sql.split(';') if s.strip()]

        print(f"Found {len(statements)} SQL statements to execute\n")

        with connection.cursor() as cursor:
            created_count = 0
            for i, statement in enumerate(statements, 1):
                # Skip empty statements
                if not statement:
                    continue

                # Extract table name from CREATE TABLE statement
                if 'CREATE TABLE' in statement:
                    try:
                        table_name = statement.split('CREATE TABLE IF NOT EXISTS')[1].split('(')[0].strip()
                        print(f"[{i}/{len(statements)}] Creating table: {table_name}")
                        cursor.execute(statement)
                        print(f"  ✓ Table '{table_name}' created successfully\n")
                        created_count += 1
                    except Exception as e:
                        print(f"  ✗ Error creating table: {e}\n")
                        print(f"Statement: {statement[:200]}...")
                        raise

            connection.commit()
            print(f"✓ Successfully created {created_count} tables\n")
        
        print("\n" + "=" * 80)
        print("✓ All monitoring tables created successfully!")
        print("=" * 80)
        
        # Show table information
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT TABLE_NAME, TABLE_ROWS, TABLE_COMMENT
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = %(schema)s
                AND TABLE_NAME IN ('apr_invalid_records', 'apr_status_history',
                                   'apr_processing_log', 'apr_proposal_products', 'apr_daily_summary')
                ORDER BY TABLE_NAME
            """, {'schema': db_config['scheme']})
            
            tables = cursor.fetchall()
            
            print("\nCreated Tables:")
            print("-" * 80)
            for table in tables:
                print(f"  • {table['TABLE_NAME']}")
                print(f"    Rows: {table['TABLE_ROWS']}")
                print(f"    Description: {table['TABLE_COMMENT']}")
                print()
    
    except Exception as e:
        print(f"\n❌ Error creating tables: {e}")
        raise
    
    finally:
        connection.close()
        print("Connection closed.")


if __name__ == '__main__':
    create_tables()

