#!/usr/bin/env python3
"""
Test script for ORM models.
Demonstrates how to use the SQLAlchemy ORM models to query the database.
"""

import json
import sys
from pathlib import Path
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from models import Base, APRTitulos, APRCapa, Cedente


def load_config() -> dict:
    """Load database configuration."""
    config_paths = [
        Path(__file__).parent / "databases_config.json",
        Path(__file__).parent.parent / "databases_config.json",
    ]
    
    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)
    
    raise FileNotFoundError("Configuration file not found")


def create_db_engine():
    """Create SQLAlchemy engine for MSSQL database."""
    config = load_config()
    cfg = config['databases']['mssql']
    
    # Create connection string for pymssql
    connection_string = (
        f"mssql+pymssql://{cfg['user']}:{cfg['password']}"
        f"@{cfg['server']}:{cfg['port']}/{cfg['scheme']}"
    )
    
    engine = create_engine(connection_string, echo=False)
    return engine


def test_apr_titulos(session):
    """Test querying APR_TITULOS using ORM."""
    print("\n" + "=" * 80)
    print("Testing APR_TITULOS ORM Model")
    print("=" * 80)
    
    # Query first 5 records from today
    from datetime import date
    today = date.today()
    
    titulos = session.query(APRTitulos).filter(
        APRTitulos.DATA >= today
    ).limit(5).all()
    
    print(f"\nFound {len(titulos)} records from today")
    
    for i, titulo in enumerate(titulos, 1):
        print(f"\n{i}. {titulo}")
        print(f"   NUMERO: {titulo.NUMERO}")
        print(f"   TITULO: {titulo.TITULO}")
        print(f"   VALOR: R$ {titulo.VALOR:,.2f}" if titulo.VALOR else "   VALOR: N/A")
        print(f"   NFEChave: {titulo.NFEChave}")
        print(f"   id_produto: {titulo.id_produto}")


def test_apr_capa(session):
    """Test querying APR_capa using ORM."""
    print("\n" + "=" * 80)
    print("Testing APR_capa ORM Model")
    print("=" * 80)
    
    # Query first 5 records from today
    from datetime import date
    today = date.today()
    
    capas = session.query(APRCapa).filter(
        APRCapa.DATA >= today
    ).limit(5).all()
    
    print(f"\nFound {len(capas)} records from today")
    
    for i, capa in enumerate(capas, 1):
        print(f"\n{i}. {capa}")
        print(f"   NUMERO: {capa.NUMERO}")
        print(f"   CEDENTE: {capa.CEDENTE}")
        print(f"   GERENTE: {capa.GERENTE}")
        print(f"   empresa: {capa.empresa}")


def test_cedente(session):
    """Test querying cedente using ORM."""
    print("\n" + "=" * 80)
    print("Testing Cedente ORM Model")
    print("=" * 80)
    
    # Query first 5 active cedentes
    cedentes = session.query(Cedente).filter(
        Cedente.ativo == True
    ).limit(5).all()
    
    print(f"\nFound {len(cedentes)} active cedentes")
    
    for i, cedente in enumerate(cedentes, 1):
        print(f"\n{i}. {cedente}")
        print(f"   apelido: {cedente.apelido}")
        print(f"   nome: {cedente.nome}")
        print(f"   cnpj: {cedente.cnpj}")
        print(f"   gerente: {cedente.gerente}")
        print(f"   ativo: {cedente.ativo}")


def test_joined_query(session):
    """Test joining APR_TITULOS and APR_capa using ORM."""
    print("\n" + "=" * 80)
    print("Testing Joined Query (APR_TITULOS + APR_capa)")
    print("=" * 80)
    
    from datetime import date
    today = date.today()
    
    # Join query
    results = session.query(
        APRCapa.GERENTE,
        APRTitulos.NUMERO,
        APRCapa.empresa,
        APRTitulos.TITULO,
        APRTitulos.VALOR,
        APRTitulos.id_produto,
        APRTitulos.NFEChave
    ).join(
        APRCapa,
        (APRTitulos.DATA == APRCapa.DATA) & (APRTitulos.NUMERO == APRCapa.NUMERO)
    ).filter(
        APRTitulos.DATA >= today,
        APRCapa.DATA >= today,
        APRTitulos.NFEChave != None,
        APRTitulos.NFEChave != ''
    ).limit(5).all()
    
    print(f"\nFound {len(results)} joined records from today")
    
    for i, row in enumerate(results, 1):
        gerente, numero, empresa, titulo, valor, id_produto, nfechave = row
        print(f"\n{i}. GERENTE: {gerente}, PROPOSTA: {numero}")
        print(f"   EMPRESA: {empresa}")
        print(f"   DUPLICATA: {titulo}")
        print(f"   VALOR: R$ {valor:,.2f}" if valor else "   VALOR: N/A")
        print(f"   ID_PRODUTO: {id_produto}")
        print(f"   NFEChave: {nfechave[:20]}..." if nfechave and len(nfechave) > 20 else f"   NFEChave: {nfechave}")


def main():
    """Main entry point."""
    try:
        print("=" * 80)
        print("ORM Models Test")
        print("=" * 80)
        
        # Create engine and session
        engine = create_db_engine()
        Session = sessionmaker(bind=engine)
        session = Session()
        
        print("✓ Database connection established")
        
        # Test each model
        test_apr_titulos(session)
        test_apr_capa(session)
        test_cedente(session)
        test_joined_query(session)
        
        # Close session
        session.close()
        
        print("\n" + "=" * 80)
        print("✓ All ORM tests completed successfully!")
        print("=" * 80)
        
        return 0
        
    except Exception as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())

