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


def query_apr_joined(conn) -> list[dict]:
    """
    Query APR_TITULOS and APR_capa tables joined on proposta number.
    Filters both tables for current date only.
    Excludes records with empty or null NFECHAVE.

    Returns:
        List of dictionaries with columns:
        GERENTE, PROPOSTA, EMPRESA, DUPLICATA, VALOR, ID_PRODUTO, NFE (as integer)
    """
    query = """
    SELECT
        c.Gerente AS GERENTE,
        t.NUMERO AS PROPOSTA,
        c.empresa AS EMPRESA,
        t.TITULO AS DUPLICATA,
        t.VALOR,
        t.id_produto AS ID_PRODUTO,
        CAST(SUBSTRING(t.NFECHAVE, 26, 9) AS INT) AS NFE
    FROM APR_TITULOS t WITH (NOLOCK)
    INNER JOIN APR_capa c WITH (NOLOCK)
        ON t.NUMERO = c.numero
    WHERE t.DATA >= CAST(GETDATE() AS DATE)
      AND t.DATA < DATEADD(DAY, 1, CAST(GETDATE() AS DATE))
      AND c.Data >= CAST(GETDATE() AS DATE)
      AND c.Data < DATEADD(DAY, 1, CAST(GETDATE() AS DATE))
      AND t.NFECHAVE IS NOT NULL
      AND t.NFECHAVE <> ''
    ORDER BY c.Gerente, t.NUMERO, t.TITULO
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


def format_joined_results_table(results: list[dict]) -> None:
    """Print joined results in a human-friendly table format."""
    print("\n" + "=" * 130)
    print("APR JOINED DATA - TODAY'S RECORDS")
    print("=" * 130)

    if not results:
        print("No records found for today.")
        return

    print(f"\nTotal records: {len(results)}\n")

    # Print table header
    print(f"{'#':<5} {'GERENTE':<20} {'PROPOSTA':<10} {'EMPRESA':<12} {'DUPLICATA':<15} {'VALOR':>15} {'ID_PRODUTO':<12} {'NFE':<10}")
    print("-" * 130)

    # Print all records in table format
    for i, row in enumerate(results, 1):
        gerente = row.get('GERENTE', '')[:19]  # Truncate if too long
        proposta = row.get('PROPOSTA', '')
        empresa = row.get('EMPRESA', '')[:11]  # Truncate if too long
        duplicata = row.get('DUPLICATA', '')[:14]  # Truncate if too long
        valor = row.get('VALOR', 0)
        id_produto = row.get('ID_PRODUTO', '')
        nfe = row.get('NFE', '')

        # Format valor as currency
        valor_str = f"R$ {valor:,.2f}" if valor else "R$ 0.00"

        print(f"{i:<5} {gerente:<20} {proposta:<10} {empresa:<12} {duplicata:<15} {valor_str:>15} {id_produto:<12} {nfe:<10}")

    print("-" * 130)

    # Calculate total
    total_valor = sum(row.get('VALOR', 0) for row in results)
    print(f"\n{'TOTAL:':>67} R$ {total_valor:,.2f}")
    print("=" * 130)


def main():
    """Main entry point."""
    try:
        print("=" * 80)
        print("APR Data Query Tool")
        print("=" * 80)

        # Connect to database
        conn = get_mssql_connection()
        print("✓ Connected successfully!\n")

        # Query joined data
        print("Querying joined APR_TITULOS + APR_capa (today's data only)...")
        joined = query_apr_joined(conn)
        format_joined_results_table(joined)

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

