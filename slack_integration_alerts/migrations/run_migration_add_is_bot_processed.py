#!/usr/bin/env python3
"""
Migration script to add is_bot_processed column to apr_valid_records table.

This script:
1. Connects to MariaDB database
2. Adds the is_bot_processed column
3. Creates an index on the new column
4. Verifies the migration was successful
"""

import json
import sys
from pathlib import Path
from sqlalchemy import create_engine, text

# Add parent directory to path to import config
sys.path.insert(0, str(Path(__file__).parent.parent))


def load_config():
    """Load database configuration."""
    config_path = Path(__file__).parent.parent / 'databases_config.json'
    with open(config_path) as f:
        return json.load(f)


def run_migration():
    """Run the migration to add is_bot_processed column."""
    print("=" * 80)
    print("MIGRATION: Add is_bot_processed column to apr_valid_records")
    print("=" * 80)
    print()
    
    # Load configuration
    config = load_config()
    db_config = config['databases']['mariadb']
    
    # Create connection string
    connection_string = (
        f"mysql+pymysql://{db_config['user']}:{db_config['password']}"
        f"@{db_config['server']}:{db_config['port']}/{db_config['scheme']}"
        f"?charset=utf8mb4"
    )
    
    # Create engine
    engine = create_engine(connection_string, echo=True)
    
    try:
        with engine.connect() as conn:
            print("\n1. Adding is_bot_processed column...")
            
            # Add the column
            add_column_sql = """
                ALTER TABLE apr_valid_records
                ADD COLUMN is_bot_processed TINYINT NOT NULL DEFAULT 0
                COMMENT 'Bot processing status: 0=not processed by bot, 1=bot clicked Processar'
                AFTER is_processado
            """
            
            try:
                conn.execute(text(add_column_sql))
                conn.commit()
                print("✓ Column added successfully")
            except Exception as e:
                if "Duplicate column name" in str(e):
                    print("⚠ Column already exists, skipping...")
                else:
                    raise
            
            print("\n2. Creating index on is_bot_processed...")
            
            # Create index
            create_index_sql = """
                CREATE INDEX idx_is_bot_processed ON apr_valid_records(is_bot_processed)
            """
            
            try:
                conn.execute(text(create_index_sql))
                conn.commit()
                print("✓ Index created successfully")
            except Exception as e:
                if "Duplicate key name" in str(e):
                    print("⚠ Index already exists, skipping...")
                else:
                    raise
            
            print("\n3. Verifying table structure...")
            
            # Verify the column exists
            verify_sql = "DESCRIBE apr_valid_records"
            result = conn.execute(text(verify_sql))
            
            print("\nTable structure:")
            print("-" * 80)
            for row in result:
                print(f"  {row[0]:<25} {row[1]:<20} {row[2]:<10} {row[3]:<10}")
            print("-" * 80)
            
            print("\n✓ Migration completed successfully!")
            
    except Exception as e:
        print(f"\n✗ Migration failed: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    run_migration()

