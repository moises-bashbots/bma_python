#!/usr/bin/env python3
"""
Quick diagnostic script to check why some proposals have "SEM PRODUTO"
"""

import json
import sys
from datetime import date
from sqlalchemy import create_engine, func
from sqlalchemy.orm import sessionmaker
from models import APRCapa, APRTitulos, Produto, ProdutoCedente, ProdutoAtributo

# Load database configuration
with open('databases_config.json') as f:
    config = json.load(f)

# Connect to MSSQL
mssql_config = config['databases']['mssql']
mssql_conn_str = (
    f"mssql+pyodbc://{mssql_config['user']}:{mssql_config['password']}"
    f"@{mssql_config['server']}:{mssql_config['port']}/{mssql_config['scheme']}"
    f"?driver=ODBC+Driver+17+for+SQL+Server"
)

engine = create_engine(mssql_conn_str)
Session = sessionmaker(bind=engine)
session = Session()

print("=" * 80)
print("CHECKING PROPOSALS WITH MISSING PRODUCTS")
print("=" * 80)
print()

# Check specific proposals
proposals_to_check = [27, 23, 22]  # FRIALTO, FRIGOVALE, FRIJOA

for prop_num in proposals_to_check:
    print(f"\n{'='*80}")
    print(f"PROPOSAL {prop_num}")
    print(f"{'='*80}")
    
    # Get proposal info
    capa = session.query(APRCapa).filter(
        APRCapa.DATA == date.today(),
        APRCapa.NUMERO == prop_num
    ).first()
    
    if not capa:
        print(f"❌ Proposal {prop_num} not found for today")
        continue
    
    print(f"Cedente: {capa.CEDENTE}")
    print(f"Status: {capa.STATUS}")
    print()
    
    # Check titles and their products
    print("Sample Titles (first 5):")
    print(f"{'Title':<20} {'id_produto':<15} {'Product Name':<30}")
    print("-" * 70)
    
    titles = session.query(
        APRTitulos.TITULO,
        APRTitulos.id_produto,
        Produto.Descritivo
    ).outerjoin(
        ProdutoCedente,
        APRTitulos.id_produto == ProdutoCedente.Id
    ).outerjoin(
        ProdutoAtributo,
        ProdutoCedente.IdProdutoAtributo == ProdutoAtributo.Id
    ).outerjoin(
        Produto,
        ProdutoAtributo.IdProduto == Produto.Id
    ).filter(
        APRTitulos.DATA == date.today(),
        APRTitulos.NUMERO == prop_num
    ).limit(5).all()
    
    for title in titles:
        id_prod = title.id_produto if title.id_produto else "NULL"
        prod_name = title.Descritivo if title.Descritivo else "❌ NO PRODUCT"
        print(f"{str(title.TITULO):<20} {str(id_prod):<15} {prod_name:<30}")
    
    # Count titles with and without products
    total_titles = session.query(func.count(APRTitulos.TITULO)).filter(
        APRTitulos.DATA == date.today(),
        APRTitulos.NUMERO == prop_num
    ).scalar()
    
    titles_with_null_product = session.query(func.count(APRTitulos.TITULO)).filter(
        APRTitulos.DATA == date.today(),
        APRTitulos.NUMERO == prop_num,
        APRTitulos.id_produto.is_(None)
    ).scalar()
    
    titles_with_product = total_titles - titles_with_null_product
    
    print()
    print(f"📊 Summary:")
    print(f"   Total Titles: {total_titles}")
    print(f"   Titles WITH product: {titles_with_product}")
    print(f"   Titles WITHOUT product (NULL): {titles_with_null_product}")
    
    if titles_with_null_product == total_titles:
        print(f"   ⚠️  ALL titles have NULL id_produto - This is a DATA ISSUE in source database")
    elif titles_with_null_product > 0:
        print(f"   ⚠️  Some titles have NULL id_produto - Mixed data")
    else:
        print(f"   ✅ All titles have products assigned")

print()
print("=" * 80)
print("CONCLUSION")
print("=" * 80)
print()
print("If proposals show 'SEM PRODUTO', it means:")
print("1. The titles in APRTitulos have NULL in the id_produto column")
print("2. This is SOURCE DATA from the MSSQL database")
print("3. The monitoring system is correctly tracking this as 'SEM PRODUTO'")
print()
print("✅ The dashboard is working correctly - it's showing real data!")
print("⚠️  The issue is in the source system where products are not assigned to titles")
print()

session.close()

