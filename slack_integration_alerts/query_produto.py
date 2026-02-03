#!/usr/bin/env python3
"""
Query Produto table to show all products
"""

import sys
sys.path.insert(0, '/home/robot/Dev/bma_python')
sys.path.insert(0, '/home/robot/Dev/bma_python/slack_integration_alerts')

import json
from sqlalchemy import create_engine, func
from sqlalchemy.orm import sessionmaker
from models import Produto

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
print("PRODUTO TABLE - ALL RECORDS")
print("=" * 140)
print()

# Count total records
total_count = session.query(func.count(Produto.Id)).scalar()
print(f"📊 Total records in Produto: {total_count:,}")
print()

# Get all products
print("=" * 140)
print(f"{'Id':<10} {'Descritivo':<80} {'Other Columns':<30}")
print("-" * 140)

produtos = session.query(Produto).order_by(Produto.Id).all()

for prod in produtos:
    id_str = str(prod.Id)
    descritivo = (prod.Descritivo[:77] + "...") if prod.Descritivo and len(prod.Descritivo) > 80 else (prod.Descritivo or "")
    
    # Try to show other attributes if they exist
    other_attrs = []
    for attr in dir(prod):
        if not attr.startswith('_') and attr not in ['Id', 'Descritivo', 'metadata', 'registry']:
            try:
                val = getattr(prod, attr)
                if val is not None and not callable(val):
                    other_attrs.append(f"{attr}={val}")
            except:
                pass
    
    other_str = ", ".join(other_attrs[:2]) if other_attrs else ""
    print(f"{id_str:<10} {descritivo:<80} {other_str:<30}")

print()
print("=" * 140)
print("SUMMARY")
print("=" * 140)
print(f"Total Produto records: {total_count:,}")
print()
print("These are the ONLY products available in the Produto table.")
print("ProdutoAtributo.IdProduto should reference these Produto.Id values.")
print()

session.close()

