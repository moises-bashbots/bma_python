#!/usr/bin/env python3
"""
Query obs1 column from cedente table.
Shows examples of the obs1 field content.
"""

import json
import sys
from pathlib import Path
from sqlalchemy import create_engine
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


def query_obs1_examples(session, limit=20):
    """Query cedente table and show obs1 column examples."""
    print("=" * 100)
    print("CEDENTE TABLE - obs1 COLUMN EXAMPLES")
    print("=" * 100)
    
    # Query cedentes with non-null obs1
    cedentes = session.query(Cedente).filter(
        Cedente.obs1 != None,
        Cedente.obs1 != ''
    ).limit(limit).all()
    
    print(f"\nFound {len(cedentes)} cedentes with obs1 content (showing first {limit})\n")
    
    for i, cedente in enumerate(cedentes, 1):
        print(f"\n{'=' * 100}")
        print(f"Record {i}:")
        print(f"{'=' * 100}")
        print(f"Apelido: {cedente.apelido}")
        print(f"Nome: {cedente.nome}")
        print(f"CNPJ: {cedente.cnpj}")
        print(f"Gerente: {cedente.gerente}")
        print(f"Ativo: {cedente.ativo}")
        print(f"\nobs1 Content:")
        print("-" * 100)
        
        # Display obs1 content
        if cedente.obs1:
            # obs1 is a Text field, might be long
            obs1_text = str(cedente.obs1)
            if len(obs1_text) > 500:
                print(obs1_text[:500])
                print(f"\n... [truncated, total length: {len(obs1_text)} characters]")
            else:
                print(obs1_text)
        else:
            print("(empty)")
    
    print("\n" + "=" * 100)
    
    # Also show statistics
    total_with_obs1 = session.query(Cedente).filter(
        Cedente.obs1 != None,
        Cedente.obs1 != ''
    ).count()
    
    total_cedentes = session.query(Cedente).count()
    
    print(f"\nStatistics:")
    print(f"  Total cedentes: {total_cedentes}")
    print(f"  Cedentes with obs1: {total_with_obs1}")
    print(f"  Percentage: {(total_with_obs1/total_cedentes*100):.2f}%")
    print("=" * 100)


def main():
    """Main entry point."""
    try:
        print("\nConnecting to database...")
        engine = create_db_engine()
        Session = sessionmaker(bind=engine)
        session = Session()
        print("✓ Connected successfully!\n")
        
        # Query obs1 examples
        query_obs1_examples(session, limit=20)
        
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

