#!/usr/bin/env python3
"""
Sync Enriched Sacado Records to MSSQL

This program syncs enriched sacado records from MariaDB back to MSSQL.
It only updates NULL/empty fields in MSSQL, preserving existing data.

Records are selected where completo=1 AND registrado=0 (enriched but not synced).
After successful sync, registrado=1 is set in MariaDB.
"""

import argparse
import json
import sys
from pathlib import Path
from typing import Optional

from sqlalchemy import create_engine, select, update, text
from sqlalchemy.orm import Session

from models import Sacado
from models_mssql import SacadoMSSQL


# LOG2 insert SQL template for audit logging
# NSEQ is identity/auto-increment, so we explicitly list all other columns
LOG2_INSERT_SQL = text("""
INSERT INTO LOG2 (
    LOGIN, DATA, TABELA, OPERACAO, OBS,
    EMPRESA, CEDENTE, TIPO_OPERACAO, BORDERO, DUPLICATA,
    BANCO, AGENCIA, CONTA, CHEQUE, NSEQ2,
    VALOR, NCLASSIF, DATA2, CONTA2, APLICACAO,
    CNPJ, CREDOR, TIPOAPLI, VALOR2, NCONTABIL, IP
) VALUES (
    :usuario, GETDATE(), 'SACADO', 'altera_sacado', :descricao,
    '', '', 0, 0, NULL,
    NULL, NULL, NULL, NULL, 0,
    0, 0, NULL, NULL, NULL,
    NULL, NULL, NULL, 0, 0, ''
)
""")


# Phone field priority order with max lengths
PHONE_FIELDS = [
    ('contato', 20),
    ('fonecob', 30),
    ('fone', 30),
    ('celular', 15),
    ('fax', 15),
]

# Email field priority order with max lengths
EMAIL_FIELDS = [
    ('email', 50),
    ('email1', 50),
    ('email2', 50),
    ('email3', 50),
    ('email4', 50),
    ('email5', 50),
]

# Address field mappings with max lengths
ADDRESS_FIELDS = {
    'endereco': 70,
    'bairro': 20,
    'cidade': 20,
    'uf': 2,
    'cep': 8,
}


def get_config_path() -> Path:
    """Get the path to the configuration file."""
    if getattr(sys, 'frozen', False):
        base_path = Path(sys.executable).parent
    else:
        base_path = Path(__file__).parent
    return base_path / "databases_config.json"


def load_config() -> dict:
    """Load database configuration."""
    config_path = get_config_path()
    if not config_path.exists():
        raise FileNotFoundError(f"Configuration file not found: {config_path}")

    with open(config_path, 'r') as f:
        config = json.load(f)

    return config['databases']


def get_mssql_engine():
    """Create SQLAlchemy engine for MSSQL (read-only)."""
    config = load_config()['mssql']
    connection_string = (
        f"mssql+pymssql://{config['user']}:{config['password']}"
        f"@{config['server']}:{config['port']}/{config['scheme']}"
    )
    return create_engine(connection_string, echo=False)


def get_mssql_write_engine():
    """Create SQLAlchemy engine for MSSQL with write credentials."""
    config = load_config()['mssql_write']
    connection_string = (
        f"mssql+pymssql://{config['user']}:{config['password']}"
        f"@{config['server']}:{config['port']}/{config['scheme']}"
    )
    return create_engine(connection_string, echo=False)


def get_mariadb_engine():
    """Create SQLAlchemy engine for MariaDB."""
    config = load_config()['mariadb']
    connection_string = (
        f"mysql+mysqlconnector://{config['user']}:{config['password']}"
        f"@{config['server']}:{config['port']}/{config['scheme']}"
    )
    return create_engine(connection_string, echo=False)


def sql_escape(value) -> str:
    """Escape a value for SQL string representation."""
    if value is None:
        return "NULL"
    if isinstance(value, str):
        # Escape single quotes by doubling them
        escaped = value.replace("'", "''")
        return f"'{escaped}'"
    return str(value)


def generate_update_sql(cnpj: str, update_values: dict) -> str:
    """Generate a raw SQL UPDATE statement for printing."""
    set_clauses = ", ".join(
        f"{field} = {sql_escape(value)}" for field, value in update_values.items()
    )
    return f"UPDATE dbo.sacado SET {set_clauses} WHERE cnpj = '{cnpj}'"


def generate_log2_sql(cnpj: str, updated_fields: list[str]) -> str:
    """Generate a raw SQL INSERT statement for LOG2."""
    fields_str = ", ".join(updated_fields)
    descricao = f"CNPJ: {cnpj} | campos: {fields_str}"
    if len(descricao) > 255:
        descricao = descricao[:252] + "..."

    return f"""INSERT INTO LOG2 (
    LOGIN, DATA, TABELA, OPERACAO, OBS,
    EMPRESA, CEDENTE, TIPO_OPERACAO, BORDERO, DUPLICATA,
    BANCO, AGENCIA, CONTA, CHEQUE, NSEQ2,
    VALOR, NCLASSIF, DATA2, CONTA2, APLICACAO,
    CNPJ, CREDOR, TIPOAPLI, VALOR2, NCONTABIL, IP
) VALUES (
    ' usr_ai4finance', GETDATE(), 'SACADO', 'altera_sacado', '{descricao}',
    '', '', 0, 0, NULL,
    NULL, NULL, NULL, NULL, 0,
    0, 0, NULL, NULL, NULL,
    NULL, NULL, NULL, 0, 0, ''
)"""


def insert_log2(session: Session, cnpj: str, updated_fields: list[str]) -> None:
    """
    Insert audit log record into LOG2 table.

    Args:
        session: MSSQL session
        cnpj: CNPJ of the sacado that was updated
        updated_fields: List of field names that were updated
    """
    # Build description: "CNPJ: 12345678901234 | campos: field1, field2, field3"
    fields_str = ", ".join(updated_fields)
    descricao = f"CNPJ: {cnpj} | campos: {fields_str}"

    # Truncate to 255 characters max
    if len(descricao) > 255:
        descricao = descricao[:252] + "..."

    session.execute(
        LOG2_INSERT_SQL,
        {"usuario": " usr_ai4finance", "descricao": descricao}
    )


def is_empty(value: Optional[str]) -> bool:
    """Check if a string value is None or empty/whitespace."""
    return value is None or str(value).strip() == ''


def truncate(value: Optional[str], max_length: int) -> Optional[str]:
    """Truncate a string to max_length, or return None if empty."""
    if is_empty(value):
        return None
    return str(value).strip()[:max_length]


def split_values(compound_value: Optional[str]) -> list[str]:
    """Split a comma/semicolon-separated string into a list of values."""
    if is_empty(compound_value):
        return []
    
    # Split by comma or semicolon
    values = []
    for sep in [',', ';']:
        if sep in compound_value:
            values = [v.strip() for v in compound_value.split(sep)]
            break
    
    if not values:
        values = [compound_value.strip()]
    
    # Filter out empty values
    return [v for v in values if v]


def fetch_enriched_sacados(limit: int) -> list[Sacado]:
    """
    Fetch enriched sacado records from MariaDB that haven't been synced to MSSQL.
    
    Criteria: completo=1 AND registrado=0
    """
    engine = get_mariadb_engine()
    
    with Session(engine) as session:
        stmt = (
            select(Sacado)
            .where(Sacado.completo == 1)
            .where(Sacado.registrado == 0)
            .limit(limit)
        )
        results = session.execute(stmt).scalars().all()
        
        # Detach from session
        sacados = list(results)
        for s in sacados:
            session.expunge(s)
        return sacados


def sync_sacado_to_mssql(
    sacado: Sacado,
    mssql_session: Session
) -> tuple[bool, list[str], bool]:
    """
    Sync a single enriched sacado record to MSSQL.

    Only updates NULL/empty fields in MSSQL.

    Returns:
        Tuple of (was_updated, list_of_updated_field_names, found_in_mssql, update_values_dict)
    """
    # Find existing record in MSSQL
    existing = mssql_session.execute(
        select(SacadoMSSQL).where(SacadoMSSQL.cnpj == sacado.cnpj)
    ).scalar_one_or_none()

    if not existing:
        return False, [], False, {}
    
    update_values = {}
    
    # --- Phone fields ---
    # Add " R" suffix to indicate it was inserted by the robot
    ROBOT_SUFFIX = " R"
    phones = split_values(sacado.telefone)
    phone_idx = 0

    for field_name, max_len in PHONE_FIELDS:
        current_value = getattr(existing, field_name, None)
        if is_empty(current_value) and phone_idx < len(phones):
            # Reserve space for suffix, then add it
            truncated = truncate(phones[phone_idx], max_len - len(ROBOT_SUFFIX))
            if truncated:
                update_values[field_name] = truncated + ROBOT_SUFFIX
                phone_idx += 1
    
    # --- Email fields ---
    emails = split_values(sacado.email)
    email_idx = 0
    
    for field_name, max_len in EMAIL_FIELDS:
        current_value = getattr(existing, field_name, None)
        if is_empty(current_value) and email_idx < len(emails):
            truncated = truncate(emails[email_idx], max_len)
            if truncated:
                update_values[field_name] = truncated
                email_idx += 1
    
    # --- Address fields ---
    for field_name, max_len in ADDRESS_FIELDS.items():
        current_value = getattr(existing, field_name, None)
        new_value = getattr(sacado, field_name, None)
        
        if is_empty(current_value) and not is_empty(new_value):
            truncated = truncate(new_value, max_len)
            if truncated:
                update_values[field_name] = truncated
    
    # Apply updates if any
    if update_values:
        stmt = (
            update(SacadoMSSQL)
            .where(SacadoMSSQL.cnpj == sacado.cnpj)
            .values(**update_values)
        )
        mssql_session.execute(stmt)
        return True, list(update_values.keys()), True, update_values

    return False, [], True, {}


def mark_as_registered(cnpjs: list[str]) -> None:
    """Mark sacado records as registered (synced to MSSQL) in MariaDB."""
    if not cnpjs:
        return

    engine = get_mariadb_engine()
    with Session(engine) as session:
        stmt = (
            update(Sacado)
            .where(Sacado.cnpj.in_(cnpjs))
            .values(registrado=1)
        )
        session.execute(stmt)
        session.commit()


def sync_enriched_to_mssql(sacados: list[Sacado]) -> dict:
    """
    Sync enriched sacado records to MSSQL.

    Returns:
        Dictionary with sync statistics.
    """
    mssql_engine = get_mssql_write_engine()

    stats = {
        "total": len(sacados),
        "updated": 0,
        "skipped": 0,
        "not_found": 0,
        "errors": 0,
        "fields_updated": 0,
    }

    synced_cnpjs = []

    print("\n" + "=" * 70)
    print("Syncing enriched records to MSSQL...")
    print("=" * 70)

    with Session(mssql_engine) as session:
        for i, sacado in enumerate(sacados, 1):
            try:
                was_updated, updated_fields, found, update_values = sync_sacado_to_mssql(sacado, session)

                if not found:
                    print(f"  [{i}/{len(sacados)}] CNPJ {sacado.cnpj}: Not found in MSSQL")
                    stats["not_found"] += 1
                elif was_updated:
                    print(f"  [{i}/{len(sacados)}] CNPJ {sacado.cnpj}: Updated {len(updated_fields)} fields ({', '.join(updated_fields)})")

                    # Print the SQL statements for manual execution
                    print(f"\n-- SQL for CNPJ {sacado.cnpj}:")
                    print(generate_update_sql(sacado.cnpj, update_values) + ";")
                    print(generate_log2_sql(sacado.cnpj, updated_fields) + ";")
                    print("")

                    stats["updated"] += 1
                    stats["fields_updated"] += len(updated_fields)
                    synced_cnpjs.append(sacado.cnpj)

                    # Insert audit log into LOG2
                    insert_log2(session, sacado.cnpj, updated_fields)
                else:
                    # Record exists but no fields needed updating
                    print(f"  [{i}/{len(sacados)}] CNPJ {sacado.cnpj}: No updates needed")
                    stats["skipped"] += 1
                    synced_cnpjs.append(sacado.cnpj)  # Still mark as registered

            except Exception as e:
                print(f"  [{i}/{len(sacados)}] CNPJ {sacado.cnpj}: Error - {e}")
                stats["errors"] += 1

        # Commit all MSSQL changes
        session.commit()

    # Mark successfully synced records as registered in MariaDB
    if synced_cnpjs:
        print(f"\nMarking {len(synced_cnpjs)} records as registered in MariaDB...")
        mark_as_registered(synced_cnpjs)

    return stats


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="Sync enriched sacado records from MariaDB to MSSQL"
    )
    parser.add_argument(
        "-l", "--limit",
        type=int,
        default=1000,
        help="Maximum number of records to process (default: 1000)"
    )
    args = parser.parse_args()

    print("=" * 70)
    print("Sync Enriched Sacados to MSSQL")
    print("=" * 70)
    print(f"Limit: {args.limit} records")

    # Fetch enriched records from MariaDB
    print("\nFetching enriched records from MariaDB...")
    print("  Criteria: completo=1 AND registrado=0")

    sacados = fetch_enriched_sacados(args.limit)
    print(f"  Found {len(sacados)} records to sync")

    if not sacados:
        print("\n✓ No records to sync.")
        return

    # Sync to MSSQL
    stats = sync_enriched_to_mssql(sacados)

    # Print summary
    print("\n" + "=" * 70)
    print("Sync Summary")
    print("=" * 70)
    print(f"  Total records:     {stats['total']}")
    print(f"  Updated in MSSQL:  {stats['updated']}")
    print(f"  Fields updated:    {stats['fields_updated']}")
    print(f"  Skipped (no diff): {stats['skipped']}")
    print(f"  Not found:         {stats['not_found']}")
    print(f"  Errors:            {stats['errors']}")
    print("=" * 70)
    print("\n✓ Done.")


if __name__ == "__main__":
    main()

