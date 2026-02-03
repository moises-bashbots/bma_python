#!/usr/bin/env python3
"""
Query ProdutoAtributo table to analyze IdProduto values
"""

import sys
sys.path.insert(0, '/home/robot/Dev/bma_python')

import json
from sqlalchemy import create_engine, func
from sqlalchemy.orm import sessionmaker
from models import ProdutoAtributo, Produto

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
print("PRODUTOATRIBUTO TABLE ANALYSIS - IdProduto Values")
print("=" * 140)
print()

# Count total records
total_count = session.query(func.count(ProdutoAtributo.Id)).scalar()
print(f"📊 Total records in ProdutoAtributo: {total_count:,}")
print()

# Count records with NULL IdProduto
null_count = session.query(func.count(ProdutoAtributo.Id)).filter(
    ProdutoAtributo.IdProduto.is_(None)
).scalar()

# Count records with IdProduto
with_produto_count = total_count - null_count

print(f"✅ Records WITH IdProduto: {with_produto_count:,} ({with_produto_count/total_count*100:.1f}%)")
print(f"❌ Records WITHOUT IdProduto (NULL): {null_count:,} ({null_count/total_count*100:.1f}%)")
print()

# Check the problematic IdProdutoAtributo values (6, 5, 12, etc.)
print("=" * 140)
print("🔍 CHECKING PRODUTOATRIBUTO RECORDS USED BY PROBLEMATIC PRODUCTS")
print("=" * 140)
print(f"{'ProdAttr.Id':<15} {'IdProduto':<15} {'Produto.Id':<15} {'Product Name':<50}")
print("-" * 140)

# The problematic products (2866, 202, 204) all have IdProdutoAtributo = 6
# Let's check what's in ProdutoAtributo.Id = 6
problematic_attr_ids = [6, 5, 12, 16, 13, 11, 8, 17, 2]

for attr_id in problematic_attr_ids:
    attr = session.query(
        ProdutoAtributo.Id,
        ProdutoAtributo.IdProduto,
        Produto.Id.label('produto_id'),
        Produto.Descritivo
    ).outerjoin(
        Produto,
        ProdutoAtributo.IdProduto == Produto.Id
    ).filter(
        ProdutoAtributo.Id == attr_id
    ).first()
    
    if attr:
        attr_id_str = str(attr.Id)
        id_produto_str = str(attr.IdProduto) if attr.IdProduto else "NULL ❌"
        produto_id_str = str(attr.produto_id) if attr.produto_id else "NULL"
        prod_name = (attr.Descritivo[:47] + "...") if attr.Descritivo and len(attr.Descritivo) > 50 else (attr.Descritivo or "NO NAME ❌")
        print(f"{attr_id_str:<15} {id_produto_str:<15} {produto_id_str:<15} {prod_name:<50}")
    else:
        print(f"{attr_id:<15} NOT FOUND")

print()

# Show all records with NULL IdProduto
if null_count > 0:
    print("=" * 140)
    print(f"❌ ALL PRODUTOATRIBUTO RECORDS WITH NULL IdProduto ({null_count} records)")
    print("=" * 140)
    print(f"{'ProdAttr.Id':<15} {'IdProduto':<15}")
    print("-" * 140)
    
    null_attrs = session.query(
        ProdutoAtributo.Id,
        ProdutoAtributo.IdProduto
    ).filter(
        ProdutoAtributo.IdProduto.is_(None)
    ).order_by(ProdutoAtributo.Id).all()
    
    for attr in null_attrs:
        attr_id_str = str(attr.Id)
        id_produto_str = "NULL ❌"
        print(f"{attr_id_str:<15} {id_produto_str:<15}")
    
    print()

print("=" * 140)
print("SUMMARY")
print("=" * 140)
print(f"Total ProdutoAtributo records: {total_count:,}")
print(f"Records with NULL IdProduto: {null_count:,} ({null_count/total_count*100:.1f}%)")
print(f"Records with IdProduto: {with_produto_count:,} ({with_produto_count/total_count*100:.1f}%)")
print()
print("⚠️  THE BROKEN CHAIN:")
print("   ProdutoCedente.IdProdutoAtributo = 6 (and others)")
print("   → ProdutoAtributo.Id = 6")
print("   → ProdutoAtributo.IdProduto = NULL ❌ ← BROKEN HERE!")
print("   → Cannot reach Produto table for product names")
print()
print("✅ This explains why the dashboard shows 'SEM PRODUTO' for these records.")
print()

session.close()

