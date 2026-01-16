#!/usr/bin/env python3
"""
Query APR_TITULOS and APR_capa tables from MSSQL database.
Fetches today's data from both tables.
"""

import json
import sys
from pathlib import Path
from typing import Optional
import pymssql
from datetime import datetime


def load_config() -> dict:
    """Load database configuration from databases_config.json."""
    # Try current directory first, then parent directory
    config_paths = [
        Path(__file__).parent / "databases_config.json",
        Path(__file__).parent.parent / "databases_config.json",
    ]
    
    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)
    
    raise FileNotFoundError(
        f"Configuration file not found in: {[str(p) for p in config_paths]}"
    )


def get_mssql_connection():
    """Create connection to MSSQL database."""
    config = load_config()
    cfg = config['databases']['mssql']
    
    print(f"Connecting to MSSQL server: {cfg['server']}:{cfg['port']}")
    print(f"Database: {cfg['scheme']}")
    print(f"User: {cfg['user']}")
    
    conn = pymssql.connect(
        server=cfg['server'],
        port=cfg['port'],
        user=cfg['user'],
        password=cfg['password'],
        database=cfg['scheme'],
        login_timeout=15
    )
    
    return conn


def query_apr_titulos(conn) -> list[dict]:
    """
    Query APR_TITULOS table for today's data.
    
    Returns:
        List of dictionaries with columns: NFECHAVE, DATA, proposta, duplicata, VALOR, id_produto
    """
    query = """
    SELECT NFECHAVE,
           DATA,
           NUMERO AS proposta,
           TITULO AS duplicata,
           VALOR,
           id_produto        
    FROM APR_TITULOS WITH (NOLOCK)
    WHERE DATA >= CAST(GETDATE() AS DATE) 
      AND DATA < DATEADD(DAY, 1, CAST(GETDATE() AS DATE))
    """
    
    cursor = conn.cursor(as_dict=True)
    cursor.execute(query)
    results = cursor.fetchall()
    cursor.close()
    
    return results


def query_apr_capa(conn) -> list[dict]:
    """
    Query APR_capa table for today's data.
    
    Returns:
        List of dictionaries with columns: cedente, proposta, Gerente, Data, empresa
    """
    query = """
    SELECT cedente, 
           numero AS proposta,
           Gerente,
           Data,
           empresa 
    FROM APR_capa WITH (NOLOCK) 
    WHERE DATA >= CAST(GETDATE() AS DATE) 
      AND DATA < DATEADD(DAY, 1, CAST(GETDATE() AS DATE))
    """
    
    cursor = conn.cursor(as_dict=True)
    cursor.execute(query)
    results = cursor.fetchall()
    cursor.close()
    
    return results


def format_results(results: list[dict], title: str) -> None:
    """Print formatted results."""
    print("\n" + "=" * 80)
    print(f"{title}")
    print("=" * 80)
    
    if not results:
        print("No records found for today.")
        return
    
    print(f"Total records: {len(results)}\n")
    
    # Print first 10 records
    for i, row in enumerate(results[:10], 1):
        print(f"Record {i}:")
        for key, value in row.items():
            # Format datetime objects
            if isinstance(value, datetime):
                value = value.strftime('%Y-%m-%d %H:%M:%S')
            print(f"  {key}: {value}")
        print()
    
    if len(results) > 10:
        print(f"... and {len(results) - 10} more records")


def main():
    """Main entry point."""
    try:
        print("=" * 80)
        print("APR Data Query Tool")
        print("=" * 80)
        
        # Connect to database
        conn = get_mssql_connection()
        print("✓ Connected successfully!\n")
        
        # Query APR_TITULOS
        print("Querying APR_TITULOS...")
        titulos = query_apr_titulos(conn)
        format_results(titulos, "APR_TITULOS - Today's Records")
        
        # Query APR_capa
        print("\nQuerying APR_capa...")
        capa = query_apr_capa(conn)
        format_results(capa, "APR_capa - Today's Records")
        
        # Close connection
        conn.close()
        print("\n" + "=" * 80)
        print("Query completed successfully!")
        print("=" * 80)
        
        return 0
        
    except Exception as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())

