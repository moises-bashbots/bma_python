#!/usr/bin/env python3
"""
Query cedentes where obs1 contains "pre" and "impresso" (in that order).
Handles accents: pré/pre and impresso/ìmpresso.
"""

import json
import sys
from pathlib import Path
from sqlalchemy import create_engine, or_, and_, func
from sqlalchemy.orm import sessionmaker
from models import Cedente


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
    
    connection_string = (
        f"mssql+pymssql://{cfg['user']}:{cfg['password']}"
        f"@{cfg['server']}:{cfg['port']}/{cfg['scheme']}"
    )
    
    engine = create_engine(connection_string, echo=False)
    return engine


def query_pre_impresso(session):
    """
    Query cedentes where obs1 contains "pre" and "impresso" in that order.
    Handles variations with/without accents.
    """
    print("=" * 100)
    print("CEDENTES WITH 'PRE' AND 'IMPRESSO' IN obs1")
    print("=" * 100)
    
    # Query using LIKE pattern to find "pre" followed by "impresso"
    # Using % wildcards to allow any characters between them
    # SQL Server LIKE is case-insensitive by default
    cedentes = session.query(Cedente).filter(
        Cedente.obs1 != None,
        or_(
            # Match "pre" (with or without accent) followed by "impresso"
            Cedente.obs1.like('%pre%impresso%'),
            Cedente.obs1.like('%pré%impresso%'),
            Cedente.obs1.like('%pre%ìmpresso%'),
            Cedente.obs1.like('%pré%ìmpresso%'),
            # Also match "Pre" capitalized
            Cedente.obs1.like('%Pre%impresso%'),
            Cedente.obs1.like('%Pré%impresso%'),
            Cedente.obs1.like('%Pre%ìmpresso%'),
            Cedente.obs1.like('%Pré%ìmpresso%'),
        )
    ).all()
    
    print(f"\nFound {len(cedentes)} cedentes matching the criteria\n")
    
    if not cedentes:
        print("No cedentes found with 'pre' and 'impresso' in obs1.")
        return
    
    for i, cedente in enumerate(cedentes, 1):
        print(f"\n{'=' * 100}")
        print(f"Record {i}:")
        print(f"{'=' * 100}")
        print(f"Apelido: {cedente.apelido}")
        print(f"Nome: {cedente.nome}")
        print(f"CNPJ: {cedente.cnpj}")
        print(f"Gerente: {cedente.gerente}")
        print(f"Ativo: {'SIM' if cedente.ativo else 'NÃO'}")
        print(f"Email: {cedente.email1 or 'N/A'}")
        print(f"Telefone: {cedente.fone or 'N/A'}")
        print(f"\nobs1 Content:")
        print("-" * 100)
        
        if cedente.obs1:
            obs1_text = str(cedente.obs1)
            # Highlight the matching parts
            print(obs1_text)
            
            # Show character count
            print(f"\n[Length: {len(obs1_text)} characters]")
        else:
            print("(empty)")
        
        # Also show obs2 if available
        if cedente.obs2:
            print(f"\nobs2 Content:")
            print("-" * 100)
            obs2_text = str(cedente.obs2)
            if len(obs2_text) > 300:
                print(obs2_text[:300] + "...")
            else:
                print(obs2_text)
    
    print("\n" + "=" * 100)
    print(f"\nSummary:")
    print(f"  Total matches: {len(cedentes)}")
    print(f"  Active: {sum(1 for c in cedentes if c.ativo)}")
    print(f"  Inactive: {sum(1 for c in cedentes if not c.ativo)}")
    print("=" * 100)


def main():
    """Main entry point."""
    try:
        print("\nConnecting to database...")
        engine = create_db_engine()
        Session = sessionmaker(bind=engine)
        session = Session()
        print("✓ Connected successfully!\n")
        
        # Query cedentes with "pre" and "impresso"
        query_pre_impresso(session)
        
        # Close session
        session.close()
        
        return 0
        
    except Exception as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())

