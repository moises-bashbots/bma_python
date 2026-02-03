#!/usr/bin/env python3
"""
Fix product data for today's proposals in MariaDB.

This script updates the apr_proposal_products table with correct product names
by using the fixed lookup chain that bypasses the broken ProdutoAtributo table.
"""

import sys
import json
from datetime import date
from sqlalchemy import create_engine, cast, Date, func, and_
from sqlalchemy.orm import sessionmaker

# Add paths for imports
sys.path.insert(0, '/home/robot/Dev/bma_python')
sys.path.insert(0, '/home/robot/Dev/bma_python/slack_integration_alerts')

from models import APRCapa, APRTitulos, Produto, ProdutoCedente
from models_mariadb import APRProposalProduct


def load_config():
    """Load database configuration."""
    with open('/home/robot/Dev/bma_python/databases_config.json') as f:
        return json.load(f)


def create_mssql_session(config):
    """Create MSSQL database session."""
    mssql_config = config['databases']['mssql']
    mssql_conn_str = (
        f"mssql+pymssql://{mssql_config['user']}:{mssql_config['password']}"
        f"@{mssql_config['server']}:{mssql_config['port']}/{mssql_config['scheme']}"
    )
    engine = create_engine(mssql_conn_str)
    Session = sessionmaker(bind=engine)
    return Session()


def create_mariadb_session(config):
    """Create MariaDB database session."""
    mariadb_config = config['databases']['mariadb']
    mariadb_conn_str = (
        f"mysql+pymysql://{mariadb_config['user']}:{mariadb_config['password']}"
        f"@{mariadb_config['server']}:{mariadb_config['port']}/{mariadb_config['scheme']}"
    )
    engine = create_engine(mariadb_conn_str)
    Session = sessionmaker(bind=engine)
    return Session()


def fix_product_data():
    """Fix product data for today's proposals."""
    config = load_config()
    mssql_session = create_mssql_session(config)
    mariadb_session = create_mariadb_session(config)
    
    today = date.today()
    
    print(f"Fixing product data for {today}...")
    print()
    
    try:
        # Query products using FIXED join chain (bypassing ProdutoAtributo)
        # APRTitulos.id_produto -> ProdutoCedente.Id -> ProdutoCedente.IdProdutoAtributo -> Produto.Id
        from sqlalchemy import func as sqlfunc
        
        query = mssql_session.query(
            APRCapa.DATA,
            APRCapa.NUMERO.label('PROPOSTA'),
            APRCapa.CEDENTE,
            sqlfunc.coalesce(Produto.Descritivo, 'SEM PRODUTO').label('PRODUTO'),
            func.count(APRTitulos.TITULO).label('QTD_TITULOS'),
            func.sum(APRTitulos.VALOR).label('VALOR_TITULOS')
        ).join(
            APRTitulos,
            and_(
                APRCapa.DATA == APRTitulos.DATA,
                APRCapa.NUMERO == APRTitulos.NUMERO
            )
        ).outerjoin(
            ProdutoCedente,
            APRTitulos.id_produto == ProdutoCedente.Id
        ).outerjoin(
            Produto,
            ProdutoCedente.IdProdutoAtributo == Produto.Id
        ).filter(
            cast(APRCapa.DATA, Date) == today
        ).group_by(
            APRCapa.DATA,
            APRCapa.NUMERO,
            APRCapa.CEDENTE,
            sqlfunc.coalesce(Produto.Descritivo, 'SEM PRODUTO')
        )
        
        results = query.all()
        
        if not results:
            print("No proposals found for today.")
            return
        
        print(f"Found {len(results)} product records for today")
        print()
        
        # Delete existing records for today
        deleted = mariadb_session.query(APRProposalProduct).filter(
            cast(APRProposalProduct.DATA, Date) == today
        ).delete(synchronize_session=False)
        
        print(f"Deleted {deleted} old product records")
        print()
        
        # Insert new records with correct product names
        inserted = 0
        for row in results:
            product_rec = APRProposalProduct(
                DATA=row.DATA,
                PROPOSTA=row.PROPOSTA,
                CEDENTE=row.CEDENTE,
                PRODUTO=row.PRODUTO,
                QTD_TITULOS=row.QTD_TITULOS,
                VALOR_TITULOS=row.VALOR_TITULOS
            )
            mariadb_session.add(product_rec)
            inserted += 1
            
            # Show progress
            if row.PRODUTO != 'SEM PRODUTO':
                print(f"✅ Proposal {row.PROPOSTA} ({row.CEDENTE}): {row.PRODUTO} - {row.QTD_TITULOS} titles")
            else:
                print(f"⚠️  Proposal {row.PROPOSTA} ({row.CEDENTE}): SEM PRODUTO - {row.QTD_TITULOS} titles")
        
        mariadb_session.commit()
        
        print()
        print(f"✅ Successfully inserted {inserted} product records")
        print()
        
        # Show summary
        sem_produto = sum(1 for r in results if r.PRODUTO == 'SEM PRODUTO')
        com_produto = len(results) - sem_produto
        
        print("Summary:")
        print(f"  With product: {com_produto}")
        print(f"  Without product (SEM PRODUTO): {sem_produto}")
        
    except Exception as e:
        mariadb_session.rollback()
        print(f"❌ Error: {e}")
        raise
    finally:
        mssql_session.close()
        mariadb_session.close()


if __name__ == '__main__':
    fix_product_data()

