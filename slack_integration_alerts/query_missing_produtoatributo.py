#!/usr/bin/env python3
"""
Find all missing ProdutoAtributo records that ProdutoCedente references
"""

import sys
sys.path.insert(0, '/home/robot/Dev/bma_python')
sys.path.insert(0, '/home/robot/Dev/bma_python/slack_integration_alerts')

import json
from sqlalchemy import create_engine, func
from sqlalchemy.orm import sessionmaker
from models import ProdutoCedente, ProdutoAtributo

# Load database configuration
with open('databases_config.json') as f:
    config = json.load(f)

# Connect to MSSQL (using pymssql driver)
mssql_config = config['databases']['mssql']
mssql_conn_str = (
    f"mssql+pymssql://{mssql_config['user']}:{mssql_config['password']}"
    f"@{mssql_config['server']}:{mssql_config['port']}/{mssql_config['scheme']}"
)

engine = create_engine(mssql_conn_str)
Session = sessionmaker(bind=engine)
session = Session()

print("=" * 140)
print("MISSING PRODUTOATRIBUTO RECORDS ANALYSIS")
print("=" * 140)
print()

# Get all unique IdProdutoAtributo values from ProdutoCedente
print("📊 Analyzing ProdutoCedente references...")
all_refs = session.query(
    ProdutoCedente.IdProdutoAtributo,
    func.count(ProdutoCedente.Id).label('usage_count')
).group_by(
    ProdutoCedente.IdProdutoAtributo
).order_by(
    ProdutoCedente.IdProdutoAtributo
).all()

print(f"Found {len(all_refs)} unique IdProdutoAtributo values referenced by ProdutoCedente")
print()

# Get all existing ProdutoAtributo Ids
existing_ids = session.query(ProdutoAtributo.Id).all()
existing_ids_set = {row.Id for row in existing_ids}

print(f"✅ Existing ProdutoAtributo records: {existing_ids_set}")
print()

# Find missing references
missing_refs = []
existing_refs = []

for ref in all_refs:
    if ref.IdProdutoAtributo in existing_ids_set:
        existing_refs.append(ref)
    else:
        missing_refs.append(ref)

print("=" * 140)
print(f"❌ MISSING PRODUTOATRIBUTO RECORDS ({len(missing_refs)} missing)")
print("=" * 140)
print(f"{'IdProdutoAtributo':<20} {'ProdutoCedente Records Using This':<35} {'Status':<30}")
print("-" * 140)

total_affected = 0
for ref in missing_refs:
    id_str = str(ref.IdProdutoAtributo)
    count_str = f"{ref.usage_count:,} records"
    status = "❌ NOT FOUND in ProdutoAtributo"
    print(f"{id_str:<20} {count_str:<35} {status:<30}")
    total_affected += ref.usage_count

print("-" * 140)
print(f"{'TOTAL':<20} {f'{total_affected:,} records':<35} {'ALL HAVE BROKEN REFERENCES':<30}")
print()

if existing_refs:
    print("=" * 140)
    print(f"✅ EXISTING PRODUTOATRIBUTO RECORDS ({len(existing_refs)} found)")
    print("=" * 140)
    print(f"{'IdProdutoAtributo':<20} {'ProdutoCedente Records Using This':<35} {'Status':<30}")
    print("-" * 140)
    
    for ref in existing_refs:
        id_str = str(ref.IdProdutoAtributo)
        count_str = f"{ref.usage_count:,} records"
        status = "✅ EXISTS in ProdutoAtributo"
        print(f"{id_str:<20} {count_str:<35} {status:<30}")
    print()

print("=" * 140)
print("SUMMARY")
print("=" * 140)
print(f"Total unique IdProdutoAtributo values referenced: {len(all_refs)}")
print(f"Existing in ProdutoAtributo: {len(existing_refs)} ({len(existing_refs)/len(all_refs)*100:.1f}%)")
print(f"Missing from ProdutoAtributo: {len(missing_refs)} ({len(missing_refs)/len(all_refs)*100:.1f}%)")
print()
print(f"Total ProdutoCedente records: {sum(ref.usage_count for ref in all_refs):,}")
print(f"Records with valid references: {sum(ref.usage_count for ref in existing_refs):,}")
print(f"Records with broken references: {total_affected:,} ❌")
print()
print("⚠️  CRITICAL DATABASE INTEGRITY ISSUE:")
print(f"   {len(missing_refs)} ProdutoAtributo records are missing!")
print(f"   {total_affected:,} ProdutoCedente records have invalid foreign key references!")
print()
print("💡 RECOMMENDED ACTION:")
print("   Rebuild ProdutoAtributo table with the missing records listed above.")
print()

# Generate SQL to create missing records
print("=" * 140)
print("SQL TO CREATE MISSING PRODUTOATRIBUTO RECORDS")
print("=" * 140)
print()
print("-- You need to determine the correct IdProduto for each missing Id")
print("-- Replace '?' with the appropriate Produto.Id value")
print()
print("INSERT INTO ProdutoAtributo (Id, IdProduto) VALUES")
for i, ref in enumerate(missing_refs):
    comma = "," if i < len(missing_refs) - 1 else ";"
    print(f"({ref.IdProdutoAtributo}, ?){comma}  -- Used by {ref.usage_count:,} ProdutoCedente records")
print()

session.close()

