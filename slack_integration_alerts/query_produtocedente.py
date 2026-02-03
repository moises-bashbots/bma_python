#!/usr/bin/env python3
"""
Query ProdutoCedente table to analyze IdProdutoAtributo values
"""

import sys
sys.path.insert(0, '/home/robot/Dev/bma_python')

import json
from sqlalchemy import create_engine, func
from sqlalchemy.orm import sessionmaker
from models import ProdutoCedente, ProdutoAtributo, Produto

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
print("PRODUTOCEDENTE TABLE ANALYSIS - IdProdutoAtributo Values")
print("=" * 140)
print()

# Count total records
total_count = session.query(func.count(ProdutoCedente.Id)).scalar()
print(f"📊 Total records in ProdutoCedente: {total_count:,}")
print()

# Count records with NULL IdProdutoAtributo
null_count = session.query(func.count(ProdutoCedente.Id)).filter(
    ProdutoCedente.IdProdutoAtributo.is_(None)
).scalar()

# Count records with IdProdutoAtributo
with_attr_count = total_count - null_count

print(f"✅ Records WITH IdProdutoAtributo: {with_attr_count:,} ({with_attr_count/total_count*100:.1f}%)")
print(f"❌ Records WITHOUT IdProdutoAtributo (NULL): {null_count:,} ({null_count/total_count*100:.1f}%)")
print()

# Show records WITH NULL IdProdutoAtributo
if null_count > 0:
    print("=" * 140)
    print(f"❌ PRODUCTS WITH NULL IdProdutoAtributo ({null_count} records)")
    print("=" * 140)
    print(f"{'Id':<10} {'IdProdutoAtributo':<20}")
    print("-" * 140)

    null_products = session.query(
        ProdutoCedente.Id,
        ProdutoCedente.IdProdutoAtributo
    ).filter(
        ProdutoCedente.IdProdutoAtributo.is_(None)
    ).order_by(ProdutoCedente.Id).limit(50).all()

    for prod in null_products:
        id_str = str(prod.Id)
        attr_str = "NULL ❌"
        print(f"{id_str:<10} {attr_str:<20}")

    if null_count > 50:
        print(f"\n... and {null_count - 50} more records with NULL IdProdutoAtributo")
    print()

# Show sample records WITH IdProdutoAtributo (with full join chain)
print("=" * 140)
print(f"✅ SAMPLE PRODUCTS WITH IdProdutoAtributo (first 20)")
print("=" * 140)
print(f"{'Id':<10} {'IdProdAttr':<12} {'ProdAttr.Id':<12} {'Produto.Id':<12} {'Product Name':<50}")
print("-" * 140)

with_attr_products = session.query(
    ProdutoCedente.Id,
    ProdutoCedente.IdProdutoAtributo,
    ProdutoAtributo.Id.label('prod_attr_id'),
    ProdutoAtributo.IdProduto,
    Produto.Id.label('produto_id'),
    Produto.Descritivo
).outerjoin(
    ProdutoAtributo,
    ProdutoCedente.IdProdutoAtributo == ProdutoAtributo.Id
).outerjoin(
    Produto,
    ProdutoAtributo.IdProduto == Produto.Id
).filter(
    ProdutoCedente.IdProdutoAtributo.isnot(None)
).order_by(ProdutoCedente.Id).limit(20).all()

for prod in with_attr_products:
    id_str = str(prod.Id)
    attr_str = str(prod.IdProdutoAtributo) if prod.IdProdutoAtributo else "NULL"
    attr_id_str = str(prod.prod_attr_id) if prod.prod_attr_id else "NULL"
    produto_str = str(prod.produto_id) if prod.produto_id else "NULL"
    prod_name = (prod.Descritivo[:47] + "...") if prod.Descritivo and len(prod.Descritivo) > 50 else (prod.Descritivo or "NO NAME")
    print(f"{id_str:<10} {attr_str:<12} {attr_id_str:<12} {produto_str:<12} {prod_name:<50}")

print()

# Check if the problematic products (2866, 202, 204) exist
print("=" * 140)
print("🔍 CHECKING PROBLEMATIC PRODUCTS (2866, 202, 204)")
print("=" * 140)
print(f"{'Id':<10} {'IdProdAttr':<12} {'ProdAttr.Id':<12} {'Produto.Id':<12} {'Product Name':<50}")
print("-" * 140)

problematic_ids = [2866, 202, 204]
for prod_id in problematic_ids:
    prod = session.query(
        ProdutoCedente.Id,
        ProdutoCedente.IdProdutoAtributo,
        ProdutoAtributo.Id.label('prod_attr_id'),
        Produto.Id.label('produto_id'),
        Produto.Descritivo
    ).outerjoin(
        ProdutoAtributo,
        ProdutoCedente.IdProdutoAtributo == ProdutoAtributo.Id
    ).outerjoin(
        Produto,
        ProdutoAtributo.IdProduto == Produto.Id
    ).filter(
        ProdutoCedente.Id == prod_id
    ).first()

    if prod:
        id_str = str(prod.Id)
        attr_str = str(prod.IdProdutoAtributo) if prod.IdProdutoAtributo else "NULL ❌"
        attr_id_str = str(prod.prod_attr_id) if prod.prod_attr_id else "NULL"
        produto_str = str(prod.produto_id) if prod.produto_id else "NULL"
        prod_name = (prod.Descritivo[:47] + "...") if prod.Descritivo and len(prod.Descritivo) > 50 else (prod.Descritivo or "NO NAME")
        print(f"{id_str:<10} {attr_str:<12} {attr_id_str:<12} {produto_str:<12} {prod_name:<50}")
    else:
        print(f"{prod_id:<10} NOT FOUND")

print()
print("=" * 140)
print("SUMMARY")
print("=" * 140)
print(f"Total ProdutoCedente records: {total_count:,}")
print(f"Records with NULL IdProdutoAtributo: {null_count:,} ({null_count/total_count*100:.1f}%)")
print(f"Records with IdProdutoAtributo: {with_attr_count:,} ({with_attr_count/total_count*100:.1f}%)")
print()
print("⚠️  Products with NULL IdProdutoAtributo cannot be linked to Produto table for product names.")
print("✅ This explains why the dashboard shows 'SEM PRODUTO' for these records.")
print()

session.close()

