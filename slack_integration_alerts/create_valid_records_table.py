#!/usr/bin/env python3
"""
Script to create MariaDB table for storing valid APR records.
This table tracks valid records identified by the query_apr_invalidos_status.py program.
"""

import json
import pymysql
from pathlib import Path
from datetime import datetime


def load_config():
    """Load database configuration."""
    config_path = Path.cwd() / 'databases_config.json'
    with open(config_path) as f:
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


def create_valid_records_table(connection):
    """
    Create table to store valid APR records.
    
    Composite unique key: (DATA, PROPOSTA, CEDENTE, RAMO)
    """
    
    create_table_sql = """
    CREATE TABLE IF NOT EXISTS apr_valid_records (
        -- Composite unique key fields
        DATA DATE NOT NULL COMMENT 'Proposal date from APR_CAPA',
        PROPOSTA INT NOT NULL COMMENT 'Proposal number from APR_CAPA',
        CEDENTE VARCHAR(100) NOT NULL COMMENT 'Client name from APR_CAPA',
        RAMO VARCHAR(100) NOT NULL COMMENT 'Business sector/rating from cedente table',
        
        -- Additional fields
        GERENTE VARCHAR(100) COMMENT 'Manager name',
        EMPRESA VARCHAR(100) COMMENT 'Company name',
        STATUS VARCHAR(50) COMMENT 'Current workflow status',
        QTD_APROVADOS INT DEFAULT 0 COMMENT 'Quantity of approved titles',
        VLR_APROVADOS DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Total approved value',
        VALOR_TITULOS DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Sum of current title values',
        QTD_TITULOS INT DEFAULT 0 COMMENT 'Count of titles in this proposal',
        
        -- Tracking fields
        first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When record was first inserted',
        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
        update_count INT DEFAULT 0 COMMENT 'Number of times record was updated',
        
        -- Composite primary key
        PRIMARY KEY (DATA, PROPOSTA, CEDENTE, RAMO),
        
        -- Indexes for common queries
        INDEX idx_status (STATUS),
        INDEX idx_gerente (GERENTE),
        INDEX idx_data (DATA),
        INDEX idx_last_updated (last_updated)
        
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='Valid APR records tracking table - stores proposals with valid DUPLICATA and SEUNO';
    """
    
    with connection.cursor() as cursor:
        print("Creating table 'apr_valid_records'...")
        cursor.execute(create_table_sql)
        connection.commit()
        print("✓ Table created successfully!")


def show_table_structure(connection):
    """Display the table structure."""
    with connection.cursor() as cursor:
        cursor.execute("DESCRIBE apr_valid_records")
        results = cursor.fetchall()
        
        print("\n" + "=" * 120)
        print("TABLE STRUCTURE: apr_valid_records")
        print("=" * 120)
        print(f"{'Field':<20} {'Type':<30} {'Null':<6} {'Key':<6} {'Default':<15} {'Extra':<30}")
        print("-" * 120)
        
        for row in results:
            field = row['Field']
            type_val = row['Type']
            null_val = row['Null']
            key = row['Key']
            default = str(row['Default']) if row['Default'] is not None else 'NULL'
            extra = row['Extra']
            
            print(f"{field:<20} {type_val:<30} {null_val:<6} {key:<6} {default:<15} {extra:<30}")
        
        print("=" * 120)


def show_indexes(connection):
    """Display table indexes."""
    with connection.cursor() as cursor:
        cursor.execute("SHOW INDEXES FROM apr_valid_records")
        results = cursor.fetchall()
        
        print("\n" + "=" * 120)
        print("TABLE INDEXES")
        print("=" * 120)
        print(f"{'Key_name':<30} {'Column_name':<20} {'Seq_in_index':<15} {'Non_unique':<12}")
        print("-" * 120)
        
        for row in results:
            key_name = row['Key_name']
            column_name = row['Column_name']
            seq = row['Seq_in_index']
            non_unique = row['Non_unique']
            
            print(f"{key_name:<30} {column_name:<20} {seq:<15} {non_unique:<12}")
        
        print("=" * 120)


def main():
    """Main function."""
    try:
        print("=" * 120)
        print("CREATE MARIADB TABLE FOR VALID APR RECORDS")
        print("=" * 120)
        print()
        
        # Load configuration
        config = load_config()
        
        # Connect to MariaDB
        print(f"Connecting to MariaDB...")
        print(f"Server: {config['databases']['mariadb']['server']}")
        print(f"Database: {config['databases']['mariadb']['scheme']}")
        print()
        
        connection = create_mariadb_connection(config)
        
        # Create table
        create_valid_records_table(connection)
        
        # Show table structure
        show_table_structure(connection)
        
        # Show indexes
        show_indexes(connection)
        
        # Close connection
        connection.close()
        
        print("\n" + "=" * 120)
        print("✓ Table creation completed successfully!")
        print("=" * 120)
        
        return 0
        
    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
        return 1


if __name__ == '__main__':
    exit(main())

