#!/usr/bin/env python3
"""
Test script to verify spectacles clicking logic
"""

# Test that we can access the first proposal from database
from models_mariadb import APRValidRecord, create_mariadb_session
from datetime import date

session = create_mariadb_session()

# Query today's valid records
today = date.today()
db_records = session.query(APRValidRecord).filter(
    APRValidRecord.DATA == today,
    APRValidRecord.is_processado == 0
).order_by(
    APRValidRecord.CEDENTE,
    APRValidRecord.PROPOSTA
).all()

print(f"Found {len(db_records)} unprocessed records")

if len(db_records) > 0:
    print(f"\nFirst record:")
    print(f"  Proposta: {db_records[0].PROPOSTA}")
    print(f"  Cedente: {db_records[0].CEDENTE}")
    print(f"  Data: {db_records[0].DATA}")
    print(f"  Status: {db_records[0].STATUS}")
    
    print(f"\n✓ Would click spectacles for Proposta {db_records[0].PROPOSTA}")
else:
    print("No records found")

session.close()

