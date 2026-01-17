#!/usr/bin/env python3
"""Get table schemas from SQL Server to create ORM models."""

import json
import sys
from pathlib import Path
import pymssql


def load_config() -> dict:
    """Load database configuration."""
    config_paths = [
        Path(__file__).parent / "databases_config.json",
        Path(__file__).parent.parent / "databases_config.json",
    ]
    
    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)
    
    raise FileNotFoundError("Configuration file not found")


def get_mssql_connection():
    """Create connection to MSSQL database."""
    config = load_config()
    cfg = config['databases']['mssql']
    
    return pymssql.connect(
        server=cfg['server'],
        port=cfg['port'],
        user=cfg['user'],
        password=cfg['password'],
        database=cfg['scheme'],
        login_timeout=15
    )


def get_table_schema(conn, table_name: str):
    """Get schema information for a table."""
    query = """
    SELECT 
        c.COLUMN_NAME,
        c.DATA_TYPE,
        c.CHARACTER_MAXIMUM_LENGTH,
        c.NUMERIC_PRECISION,
        c.NUMERIC_SCALE,
        c.IS_NULLABLE,
        c.COLUMN_DEFAULT
    FROM INFORMATION_SCHEMA.COLUMNS c
    WHERE c.TABLE_NAME = %s
    ORDER BY c.ORDINAL_POSITION
    """
    
    cursor = conn.cursor(as_dict=True)
    cursor.execute(query, (table_name,))
    results = cursor.fetchall()
    cursor.close()
    
    return results


def get_primary_keys(conn, table_name: str):
    """Get primary key columns for a table."""
    query = """
    SELECT COLUMN_NAME
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE OBJECTPROPERTY(OBJECT_ID(CONSTRAINT_SCHEMA + '.' + CONSTRAINT_NAME), 'IsPrimaryKey') = 1
    AND TABLE_NAME = %s
    ORDER BY ORDINAL_POSITION
    """
    
    cursor = conn.cursor(as_dict=True)
    cursor.execute(query, (table_name,))
    results = cursor.fetchall()
    cursor.close()
    
    return [row['COLUMN_NAME'] for row in results]


def main():
    """Main entry point."""
    tables = ['APR_TITULOS', 'APR_capa', 'cedente']
    
    try:
        conn = get_mssql_connection()
        print("✓ Connected to database\n")
        
        for table_name in tables:
            print("=" * 80)
            print(f"Table: {table_name}")
            print("=" * 80)
            
            # Get schema
            schema = get_table_schema(conn, table_name)
            
            if not schema:
                print(f"⚠ Table '{table_name}' not found or has no columns\n")
                continue
            
            # Get primary keys
            pk_columns = get_primary_keys(conn, table_name)
            
            print(f"\nPrimary Keys: {', '.join(pk_columns) if pk_columns else 'None'}\n")
            print(f"{'Column':<30} {'Type':<20} {'Nullable':<10} {'Default':<20}")
            print("-" * 80)
            
            for col in schema:
                col_name = col['COLUMN_NAME']
                data_type = col['DATA_TYPE']
                
                # Add length/precision info
                if col['CHARACTER_MAXIMUM_LENGTH']:
                    data_type += f"({col['CHARACTER_MAXIMUM_LENGTH']})"
                elif col['NUMERIC_PRECISION']:
                    if col['NUMERIC_SCALE']:
                        data_type += f"({col['NUMERIC_PRECISION']},{col['NUMERIC_SCALE']})"
                    else:
                        data_type += f"({col['NUMERIC_PRECISION']})"
                
                nullable = col['IS_NULLABLE']
                default = col['COLUMN_DEFAULT'] or ''
                
                # Mark primary keys
                if col_name in pk_columns:
                    col_name += " [PK]"
                
                print(f"{col_name:<30} {data_type:<20} {nullable:<10} {default:<20}")
            
            print()
        
        conn.close()
        
    except Exception as e:
        print(f"✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1
    
    return 0


if __name__ == "__main__":
    sys.exit(main())

