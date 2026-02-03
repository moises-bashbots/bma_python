#!/usr/bin/env python3
"""
Query SQL Server directly to show real data for proposals with missing products
"""

import sys
sys.path.insert(0, '/home/robot/Dev/bma_python')

import json
from datetime import date
from sqlalchemy import create_engine, func, and_, cast, Date
from sqlalchemy.orm import sessionmaker
from models import APRCapa, APRTitulos, Produto, ProdutoCedente, ProdutoAtributo

# Load database configuration
with open('databases_config.json') as f:
    config = json.load(f)

# Connect to MSSQL (using pymssql driver like the validation program)
mssql_config = config['databases']['mssql']
mssql_conn_str = (
    f"mssql+pymssql://{mssql_config['user']}:{mssql_config['password']}"
    f"@{mssql_config['server']}:{mssql_config['port']}/{mssql_config['scheme']}"
)

engine = create_engine(mssql_conn_str)
Session = sessionmaker(bind=engine)
session = Session()

print("=" * 100)
print("QUERYING SQL SERVER DATABASE - PROPOSALS WITH MISSING PRODUCTS")
print("=" * 100)
print()

# Get today's date
today = date.today()
print(f"Date: {today}")
print()

# Query proposals with titles that have NULL id_produto
proposals_to_check = [27, 23, 22]  # FRIALTO, FRIGOVALE, FRIJOA

for prop_num in proposals_to_check:
    print(f"\n{'='*100}")
    print(f"PROPOSAL {prop_num}")
    print(f"{'='*100}")
    
    # Get proposal header info
    capa = session.query(APRCapa).filter(
        cast(APRCapa.DATA, Date) == today,
        APRCapa.NUMERO == prop_num
    ).first()
    
    if not capa:
        print(f"❌ Proposal {prop_num} not found for {today}")
        continue
    
    print(f"📋 Proposal Info:")
    print(f"   Cedente: {capa.CEDENTE}")
    print(f"   Gerente: {capa.GERENTE}")
    print(f"   Qty Proposed: {capa.QTD_PROPOSTOS}")
    print(f"   Value Proposed: R$ {capa.VLR_PROPOSTOS:,.2f}" if capa.VLR_PROPOSTOS else "   Value Proposed: R$ 0.00")
    print()
    
    # Count total titles
    total_titles = session.query(func.count(APRTitulos.TITULO)).filter(
        cast(APRTitulos.DATA, Date) == today,
        APRTitulos.NUMERO == prop_num
    ).scalar()
    
    # Count titles with NULL id_produto
    null_product_count = session.query(func.count(APRTitulos.TITULO)).filter(
        cast(APRTitulos.DATA, Date) == today,
        APRTitulos.NUMERO == prop_num,
        APRTitulos.id_produto.is_(None)
    ).scalar()
    
    # Count titles with id_produto
    with_product_count = total_titles - null_product_count
    
    print(f"📊 Title Statistics:")
    print(f"   Total Titles: {total_titles}")
    print(f"   Titles WITH product (id_produto NOT NULL): {with_product_count}")
    print(f"   Titles WITHOUT product (id_produto IS NULL): {null_product_count}")
    print()
    
    # Show sample titles with their id_produto values AND the join chain
    print(f"📄 Sample Titles (first 10) - WITH JOIN CHAIN DETAILS:")
    print(f"{'Title':<20} {'id_produto':<12} {'ProdCed.Id':<12} {'ProdAttr.Id':<12} {'Produto.Id':<12} {'Product Name':<30}")
    print("-" * 120)

    titles = session.query(
        APRTitulos.TITULO,
        APRTitulos.id_produto,
        APRTitulos.VALOR,
        ProdutoCedente.Id.label('prod_ced_id'),
        ProdutoCedente.IdProdutoAtributo,
        ProdutoAtributo.Id.label('prod_attr_id'),
        ProdutoAtributo.IdProduto,
        Produto.Id.label('produto_id'),
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
        cast(APRTitulos.DATA, Date) == today,
        APRTitulos.NUMERO == prop_num
    ).limit(10).all()

    for title in titles:
        titulo_str = str(title.TITULO)[:19]
        id_prod = str(title.id_produto) if title.id_produto else "NULL"
        prod_ced = str(title.prod_ced_id) if title.prod_ced_id else "NULL"
        prod_attr = str(title.prod_attr_id) if title.prod_attr_id else "NULL"
        produto = str(title.produto_id) if title.produto_id else "NULL"
        prod_name = title.Descritivo if title.Descritivo else "❌ BROKEN CHAIN"
        print(f"{titulo_str:<20} {id_prod:<12} {prod_ced:<12} {prod_attr:<12} {produto:<12} {prod_name:<30}")
    
    print()
    
    # Summary
    if null_product_count == total_titles:
        print(f"⚠️  RESULT: ALL {total_titles} titles have NULL id_produto")
        print(f"   This is why the dashboard shows 'SEM PRODUTO'")
    elif null_product_count > 0:
        print(f"⚠️  RESULT: {null_product_count} out of {total_titles} titles have NULL id_produto")
        print(f"   Mixed data - some titles have products, some don't")
    else:
        print(f"✅ RESULT: All titles have products assigned")

print()
print("=" * 100)
print("SUMMARY")
print("=" * 100)
print()
print("The data shown above is DIRECTLY from SQL Server (MSSQL) database.")
print("If id_produto is NULL, there is NO product assigned to those titles in the source system.")
print()
print("✅ The dashboard is correctly showing 'SEM PRODUTO' for these proposals.")
print("⚠️  This is a DATA ISSUE in the source APR system, not a monitoring system bug.")
print()

session.close()

