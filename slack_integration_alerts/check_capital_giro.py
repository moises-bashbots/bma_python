#!/usr/bin/env python3
"""
Check if CAPITAL DE GIRO product exists and analyze SEUNO usage.
"""

import sys
from pathlib import Path
from datetime import datetime, date, timedelta
from sqlalchemy import create_engine, func
from sqlalchemy.orm import sessionmaker
import json

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

from models import APRCapa, APRTitulos, Produto, ProdutoCedente

# Load database configuration
DATABASE_CONFIG_FILE = 'databases_config.json'

def load_database_config():
    """Load database configuration from JSON file."""
    config_path = Path(__file__).parent / DATABASE_CONFIG_FILE
    with open(config_path, 'r') as f:
        return json.load(f)

def create_mssql_session():
    """Create SQLAlchemy session for MSSQL database."""
    config = load_database_config()
    mssql_config = config['databases']['mssql']

    conn_str = (
        f"mssql+pymssql://{mssql_config['user']}:{mssql_config['password']}"
        f"@{mssql_config['server']}/{mssql_config['scheme']}"
    )

    engine = create_engine(conn_str, echo=False)
    Session = sessionmaker(bind=engine)
    return Session()

def main():
    print("=" * 80)
    print("CHECKING FOR 'CAPITAL DE GIRO' PRODUCT")
    print("=" * 80)
    print()
    
    session = create_mssql_session()
    
    try:
        # Query 1: Find all products matching CAPITAL DE GIRO
        print("1. Products matching 'CAPITAL DE GIRO' in APR_TITULOS:")
        print("-" * 80)

        products = session.query(
            Produto.Descritivo,
            func.count(APRTitulos.TITULO).label('count')
        ).join(
            ProdutoCedente,
            APRTitulos.id_produto == ProdutoCedente.Id
        ).join(
            Produto,
            ProdutoCedente.IdProdutoAtributo == Produto.Id
        ).filter(
            Produto.Descritivo.like('%CAPITAL%GIRO%')
        ).group_by(
            Produto.Descritivo
        ).order_by(
            func.count(APRTitulos.TITULO).desc()
        ).all()
        
        if products:
            for produto, count in products:
                print(f"  - {produto}: {count} records")
        else:
            print("  (none found)")
        print()
        
        # Query 2: Check recent usage (last 30 days)
        print("2. Recent usage (last 30 days) - Sample records:")
        print("-" * 80)

        cutoff_date = date.today() - timedelta(days=30)

        recent = session.query(
            APRCapa.DATA,
            APRCapa.NUMERO,
            APRCapa.CEDENTE,
            Produto.Descritivo.label('PRODUTO'),
            APRTitulos.SEUNO,
            APRTitulos.TITULO.label('DUPLICATA')
        ).join(
            APRTitulos,
            (APRCapa.DATA == APRTitulos.DATA) & (APRCapa.NUMERO == APRTitulos.NUMERO)
        ).join(
            ProdutoCedente,
            APRTitulos.id_produto == ProdutoCedente.Id
        ).join(
            Produto,
            ProdutoCedente.IdProdutoAtributo == Produto.Id
        ).filter(
            Produto.Descritivo.like('%CAPITAL%GIRO%'),
            APRCapa.DATA >= cutoff_date
        ).order_by(
            APRCapa.DATA.desc()
        ).limit(10).all()
        
        if recent:
            print(f"{'DATA':<12} {'PROPOSTA':<10} {'CEDENTE':<20} {'PRODUTO':<25} {'SEUNO':<15} {'DUPLICATA':<15}")
            print("-" * 110)
            for data, numero, cedente, produto, seuno, duplicata in recent:
                data_str = data.strftime('%Y-%m-%d') if data else ''
                numero_str = str(numero) if numero else ''
                cedente_str = (cedente or '')[:19]
                produto_str = (produto or '')[:24]
                seuno_str = (seuno or '')[:14]
                duplicata_str = (duplicata or '')[:14]
                print(f"{data_str:<12} {numero_str:<10} {cedente_str:<20} {produto_str:<25} {seuno_str:<15} {duplicata_str:<15}")
        else:
            print("  (none found in last 30 days)")
        print()
        
        # Query 3: SEUNO usage pattern
        print("3. SEUNO usage pattern for CAPITAL DE GIRO products:")
        print("-" * 80)

        # Count empty vs filled SEUNO
        empty_count = session.query(func.count(APRTitulos.TITULO)).join(
            ProdutoCedente,
            APRTitulos.id_produto == ProdutoCedente.Id
        ).join(
            Produto,
            ProdutoCedente.IdProdutoAtributo == Produto.Id
        ).filter(
            Produto.Descritivo.like('%CAPITAL%GIRO%'),
            (APRTitulos.SEUNO == None) | (APRTitulos.SEUNO == '')
        ).scalar()

        filled_count = session.query(func.count(APRTitulos.TITULO)).join(
            ProdutoCedente,
            APRTitulos.id_produto == ProdutoCedente.Id
        ).join(
            Produto,
            ProdutoCedente.IdProdutoAtributo == Produto.Id
        ).filter(
            Produto.Descritivo.like('%CAPITAL%GIRO%'),
            (APRTitulos.SEUNO != None) & (APRTitulos.SEUNO != '')
        ).scalar()
        
        total = empty_count + filled_count
        
        if total > 0:
            empty_pct = (empty_count / total * 100) if total > 0 else 0
            filled_pct = (filled_count / total * 100) if total > 0 else 0
            
            print(f"  Empty/NULL: {empty_count} records ({empty_pct:.1f}%)")
            print(f"  Filled:     {filled_count} records ({filled_pct:.1f}%)")
            print(f"  Total:      {total} records")
        else:
            print("  (no data)")
        print()
        
        # Recommendation
        print("=" * 80)
        print("RECOMMENDATION:")
        print("=" * 80)
        
        if total > 0:
            if empty_pct > 90:
                print("✅ CAPITAL DE GIRO products have mostly EMPTY SEUNO fields")
                print("✅ It is SAFE to add 'CAPITAL DE GIRO' to products_no_seuno_validation")
                print()
                print("This means:")
                print("  - CAPITAL DE GIRO products SHOULD have empty SEUNO")
                print("  - If SEUNO is filled, it will be marked as INVALID")
            elif filled_pct > 90:
                print("⚠️  CAPITAL DE GIRO products have mostly FILLED SEUNO fields")
                print("⚠️  DO NOT add to products_no_seuno_validation")
                print("⚠️  These products require SEUNO validation")
            else:
                print("⚠️  CAPITAL DE GIRO products have MIXED SEUNO usage")
                print(f"⚠️  Empty: {empty_pct:.1f}%, Filled: {filled_pct:.1f}%")
                print("⚠️  Requires manual review before adding to exception list")
        else:
            print("⚠️  No CAPITAL DE GIRO products found in database")
            print("⚠️  Cannot make recommendation")
        
        print("=" * 80)
        
    finally:
        session.close()

if __name__ == '__main__':
    main()

