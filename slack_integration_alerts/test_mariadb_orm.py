#!/usr/bin/env python3
"""
Test script for MariaDB ORM models.
Demonstrates how to use the APRValidRecord model to query and update records.
"""

import json
import sys
from pathlib import Path
from datetime import date, datetime
from sqlalchemy import create_engine, func
from sqlalchemy.orm import sessionmaker
from models_mariadb import MariaDBBase, APRValidRecord


def load_config() -> dict:
    """Load database configuration."""
    config_path = Path(__file__).parent / "databases_config.json"
    
    if not config_path.exists():
        raise FileNotFoundError(f"Configuration file not found: {config_path}")
    
    with open(config_path, 'r') as f:
        return json.load(f)


def create_mariadb_engine(config):
    """Create SQLAlchemy engine for MariaDB database."""
    db_config = config['databases']['mariadb']
    
    connection_string = (
        f"mysql+pymysql://{db_config['user']}:{db_config['password']}"
        f"@{db_config['server']}:{db_config['port']}/{db_config['scheme']}"
        f"?charset=utf8mb4"
    )
    
    engine = create_engine(connection_string, echo=False)
    return engine


def test_query_unprocessed_records(session):
    """Query records that have not been processed yet."""
    print("=" * 100)
    print("UNPROCESSED RECORDS (is_processado = 0)")
    print("=" * 100)
    
    records = session.query(APRValidRecord).filter(
        APRValidRecord.is_processado == 0
    ).limit(10).all()
    
    if not records:
        print("No unprocessed records found.")
        return
    
    for record in records:
        print(f"\nDATA: {record.DATA}, PROPOSTA: {record.PROPOSTA}")
        print(f"  CEDENTE: {record.CEDENTE}")
        print(f"  RAMO: {record.RAMO}")
        print(f"  STATUS: {record.STATUS}")
        print(f"  GERENTE: {record.GERENTE}")
        print(f"  QTD_APROVADOS: {record.QTD_APROVADOS}, VLR_APROVADOS: {record.VLR_APROVADOS}")
        print(f"  is_processado: {record.is_processado} (is_processed property: {record.is_processed})")
        print(f"  first_seen: {record.first_seen}, last_updated: {record.last_updated}")


def test_query_processed_records(session):
    """Query records that have been processed."""
    print("\n" + "=" * 100)
    print("PROCESSED RECORDS (is_processado = 1)")
    print("=" * 100)
    
    records = session.query(APRValidRecord).filter(
        APRValidRecord.is_processado == 1
    ).limit(10).all()
    
    if not records:
        print("No processed records found.")
        return
    
    for record in records:
        print(f"\nDATA: {record.DATA}, PROPOSTA: {record.PROPOSTA}")
        print(f"  CEDENTE: {record.CEDENTE}")
        print(f"  is_processado: {record.is_processado} (is_processed property: {record.is_processed})")


def test_count_by_processing_status(session):
    """Count records by processing status."""
    print("\n" + "=" * 100)
    print("RECORD COUNT BY PROCESSING STATUS")
    print("=" * 100)
    
    unprocessed_count = session.query(func.count(APRValidRecord.PROPOSTA)).filter(
        APRValidRecord.is_processado == 0
    ).scalar()
    
    processed_count = session.query(func.count(APRValidRecord.PROPOSTA)).filter(
        APRValidRecord.is_processado == 1
    ).scalar()
    
    total_count = session.query(func.count(APRValidRecord.PROPOSTA)).scalar()
    
    print(f"\nUnprocessed (is_processado = 0): {unprocessed_count}")
    print(f"Processed (is_processado = 1):   {processed_count}")
    print(f"Total records:                   {total_count}")
    
    if total_count > 0:
        print(f"\nProcessed percentage: {(processed_count / total_count * 100):.2f}%")


def test_mark_as_processed(session, dry_run=True):
    """Test marking a record as processed."""
    print("\n" + "=" * 100)
    print("TEST: MARK RECORD AS PROCESSED")
    print("=" * 100)
    
    # Find first unprocessed record
    record = session.query(APRValidRecord).filter(
        APRValidRecord.is_processado == 0
    ).first()
    
    if not record:
        print("No unprocessed records found to test with.")
        return
    
    print(f"\nFound unprocessed record:")
    print(f"  DATA: {record.DATA}, PROPOSTA: {record.PROPOSTA}")
    print(f"  CEDENTE: {record.CEDENTE}")
    print(f"  is_processado BEFORE: {record.is_processado}")
    
    if dry_run:
        print("\n[DRY RUN] Would mark record as processed (is_processado = 1)")
    else:
        # Mark as processed using the helper method
        record.mark_as_processed()
        session.commit()
        print(f"\n✓ Record marked as processed")
        print(f"  is_processado AFTER: {record.is_processado}")


def main():
    """Main entry point."""
    print("=" * 100)
    print("MARIADB ORM MODEL TEST - APRValidRecord")
    print("=" * 100)
    print()
    
    try:
        # Load configuration
        config = load_config()
        
        # Create engine and session
        engine = create_mariadb_engine(config)
        Session = sessionmaker(bind=engine)
        session = Session()
        
        print(f"✓ Connected to MariaDB: {config['databases']['mariadb']['server']}")
        print()
        
        # Run tests
        test_count_by_processing_status(session)
        test_query_unprocessed_records(session)
        test_query_processed_records(session)
        test_mark_as_processed(session, dry_run=True)
        
        # Close session
        session.close()
        print("\n" + "=" * 100)
        print("✓ Tests completed successfully!")
        print("=" * 100)
        
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()

