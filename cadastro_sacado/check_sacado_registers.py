#!/usr/bin/env python3
"""
Check Sacado Registers

This program queries the MSSQL database to find sacado records
that have missing contact or address information.
"""

import json
import sys
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Optional

from sqlalchemy import create_engine, select, or_, text
from sqlalchemy.orm import Session

from models import Sacado
from models_mssql import SacadoMSSQL, TituloMSSQL


@dataclass
class SacadoRegister:
    """Dataclass representing a sacado register with incomplete data."""
    nome: str
    apelido: str
    cnpj: str
    email: Optional[str]
    email1: Optional[str]
    email2: Optional[str]
    email3: Optional[str]
    email4: Optional[str]
    email5: Optional[str]
    contato: Optional[str]
    fone: Optional[str]
    celular: Optional[str]
    fax: Optional[str]
    saccob: Optional[str]
    endereco: Optional[str]
    bairro: Optional[str]
    cidade: Optional[str]
    uf: Optional[str]
    cep: Optional[str]
    dentrada: Optional[datetime]

    def __str__(self) -> str:
        return f"{self.nome} ({self.cnpj}) - Contato: {self.contato or 'N/A'}, Endereço: {self.endereco or 'N/A'}"

    @property
    def has_missing_contato(self) -> bool:
        return self.contato is None or self.contato.strip() == ""

    @property
    def has_missing_endereco(self) -> bool:
        return self.endereco is None or self.endereco.strip() == ""

    @property
    def all_emails(self) -> list[str]:
        """Return list of all non-empty emails."""
        emails = [self.email, self.email1, self.email2, self.email3, self.email4, self.email5]
        return [e for e in emails if is_valid_email(e)]

    @property
    def all_phones(self) -> list[str]:
        """Return list of all non-empty phone numbers."""
        phones = [self.contato, self.fone, self.celular, self.fax]
        return [p for p in phones if is_valid_phone(p)]

    @property
    def compiled_email(self) -> Optional[str]:
        """Return comma-separated list of all emails for MariaDB."""
        emails = self.all_emails
        return ", ".join(emails) if emails else None

    @property
    def compiled_telefone(self) -> Optional[str]:
        """Return comma-separated list of all phones for MariaDB."""
        phones = self.all_phones
        return ", ".join(phones) if phones else None

    @property
    def valid_endereco(self) -> Optional[str]:
        """Return endereco only if valid."""
        return self.endereco if is_valid_address(self.endereco) else None

    @property
    def valid_bairro(self) -> Optional[str]:
        """Return bairro only if valid."""
        return self.bairro if is_valid_address(self.bairro) else None

    @property
    def valid_cidade(self) -> Optional[str]:
        """Return cidade only if valid."""
        return self.cidade if is_valid_address(self.cidade) else None

    @property
    def valid_uf(self) -> Optional[str]:
        """Return uf only if valid (2 letters)."""
        if self.uf and len(self.uf.strip()) == 2 and self.uf.strip().isalpha():
            return self.uf.strip().upper()
        return None

    @property
    def valid_cep(self) -> Optional[str]:
        """Return cep only if valid (8 digits)."""
        if self.cep:
            digits = ''.join(c for c in self.cep if c.isdigit())
            if len(digits) == 8:
                return digits
        return None

    @property
    def calculated_is_pf(self) -> int:
        """Calculate is_pf based on CNPJ length. 0=CNPJ (14 chars), 1=CPF (other)."""
        if self.cnpj and len(self.cnpj.strip()) == 14:
            return 0  # CNPJ - legal entity
        return 1  # CPF - individual


def is_valid_phone(phone: Optional[str]) -> bool:
    """
    Validate phone number.
    Must have at least 8 digits and not be placeholder text.
    """
    if not phone:
        return False

    cleaned = phone.strip()
    if not cleaned:
        return False

    # Extract only digits
    digits = ''.join(c for c in cleaned if c.isdigit())

    # Must have at least 8 digits (minimum for a valid phone)
    if len(digits) < 8:
        return False

    # Check for placeholder patterns (all same digit, sequential, etc.)
    if len(set(digits)) == 1:  # All same digit like 00000000
        return False

    # Check for common invalid patterns
    invalid_patterns = ['00000000', '11111111', '12345678', '99999999']
    if digits in invalid_patterns or digits[:8] in invalid_patterns:
        return False

    return True


def is_valid_email(email: Optional[str]) -> bool:
    """
    Validate email address.
    Must contain @ and a dot after @, and not be placeholder text.
    """
    if not email:
        return False

    cleaned = email.strip()
    if not cleaned:
        return False

    # Basic email validation
    if '@' not in cleaned:
        return False

    parts = cleaned.split('@')
    if len(parts) != 2:
        return False

    local, domain = parts
    if not local or not domain:
        return False

    # Domain must have at least one dot
    if '.' not in domain:
        return False

    # Check for placeholder patterns
    invalid_patterns = ['teste@', 'test@', 'xxx@', 'nao@', 'sem@', 'null@', 'none@']
    if any(cleaned.lower().startswith(p) for p in invalid_patterns):
        return False

    return True


def is_valid_address(address: Optional[str]) -> bool:
    """
    Validate address field.
    Must have meaningful content, not just punctuation or placeholder text.
    """
    if not address:
        return False

    cleaned = address.strip()
    if not cleaned:
        return False

    # Must have at least 3 characters
    if len(cleaned) < 3:
        return False

    # Must contain at least some letters
    if not any(c.isalpha() for c in cleaned):
        return False

    # Check for placeholder patterns
    invalid_patterns = [
        'xxx', 'nao informado', 'não informado', 'n/a', 'n/d',
        'nao tem', 'não tem', 'sem endereco', 'sem endereço',
        'null', 'none', 'teste', 'test', '...', '---', '***'
    ]
    if cleaned.lower() in invalid_patterns:
        return False

    return True


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


def load_mssql_config() -> dict:
    """Load MSSQL database configuration."""
    return load_config()['mssql']


def load_mariadb_config() -> dict:
    """Load MariaDB database configuration."""
    return load_config()['mariadb']


def get_mssql_engine():
    """Create SQLAlchemy engine for MSSQL."""
    config = load_mssql_config()

    # SQLAlchemy connection string for pymssql
    connection_string = (
        f"mssql+pymssql://{config['user']}:{config['password']}"
        f"@{config['server']}:{config['port']}/{config['scheme']}"
    )

    return create_engine(connection_string, echo=False)


def get_mariadb_engine():
    """Create SQLAlchemy engine for MariaDB."""
    config = load_mariadb_config()

    # SQLAlchemy connection string for mysql-connector
    connection_string = (
        f"mysql+mysqlconnector://{config['user']}:{config['password']}"
        f"@{config['server']}:{config['port']}/{config['scheme']}"
    )

    return create_engine(connection_string, echo=False)


def fetch_incomplete_sacados() -> list[SacadoRegister]:
    """
    Fetch sacado records that have missing contato or endereco.

    Returns:
        List of SacadoRegister objects with incomplete data.
    """
    engine = get_mssql_engine()

    print(f"Connecting to MSSQL server using SQLAlchemy...")

    with Session(engine) as session:
        # Build query using SQLAlchemy ORM
        # Filter: contato IS NULL OR endereco IS NULL
        #         AND titulo from last 90 days (dentrada > dateadd(day,-90,getdate()))
        #         AND titulo not paid (quitacao IS NULL)
        # Use text() for dateadd to avoid parameterization of 'day' keyword
        date_90_days_ago = text("dateadd(day, -90, getdate())")

        stmt = (
            select(
                SacadoMSSQL.nome,
                SacadoMSSQL.apelido,
                SacadoMSSQL.cnpj,
                SacadoMSSQL.email,
                SacadoMSSQL.email1,
                SacadoMSSQL.email2,
                SacadoMSSQL.email3,
                SacadoMSSQL.email4,
                SacadoMSSQL.email5,
                SacadoMSSQL.contato,
                SacadoMSSQL.fone,
                SacadoMSSQL.celular,
                SacadoMSSQL.fax,
                SacadoMSSQL.saccob,
                SacadoMSSQL.endereco,
                SacadoMSSQL.bairro,
                SacadoMSSQL.cidade,
                SacadoMSSQL.uf,
                SacadoMSSQL.cep,
                TituloMSSQL.dentrada
            )
            .select_from(TituloMSSQL)
            .with_hint(TituloMSSQL, 'WITH (NOLOCK)', 'mssql')
            .join(SacadoMSSQL, SacadoMSSQL.cnpj == TituloMSSQL.cnpj)
            .where(
                or_(
                    SacadoMSSQL.contato.is_(None),
                    SacadoMSSQL.endereco.is_(None)
                )
            )
            .where(TituloMSSQL.dentrada > date_90_days_ago)
            .where(TituloMSSQL.quitacao.is_(None))
            .distinct()
            .order_by(TituloMSSQL.dentrada.desc())
        )

        print("Executing query...")
        results = session.execute(stmt).all()

        sacados = []
        for row in results:
            sacado = SacadoRegister(
                nome=row.nome,
                apelido=row.apelido,
                cnpj=row.cnpj,
                email=row.email,
                email1=row.email1,
                email2=row.email2,
                email3=row.email3,
                email4=row.email4,
                email5=row.email5,
                contato=row.contato,
                fone=row.fone,
                celular=row.celular,
                fax=row.fax,
                saccob=row.saccob,
                endereco=row.endereco,
                bairro=row.bairro,
                cidade=row.cidade,
                uf=row.uf,
                cep=row.cep,
                dentrada=row.dentrada
            )
            sacados.append(sacado)

    return sacados


def sync_sacados_to_mariadb(sacados: list[SacadoRegister]) -> dict:
    """
    Synchronize sacado records from MSSQL to MariaDB.

    Args:
        sacados: List of SacadoRegister objects to sync.

    Returns:
        Dictionary with sync statistics (created, updated, skipped).
    """
    engine = get_mariadb_engine()
    stats = {"created": 0, "updated": 0, "skipped": 0, "errors": 0}

    print("\n" + "=" * 70)
    print("Synchronizing sacados to MariaDB...")
    print("=" * 70)

    with Session(engine) as session:
        for sacado_reg in sacados:
            try:
                # Check if record exists by CNPJ
                existing = session.execute(
                    select(Sacado).where(Sacado.cnpj == sacado_reg.cnpj)
                ).scalar_one_or_none()

                if existing:
                    # Update existing record - only update NULL/empty fields
                    updated = False

                    # Direct field mappings - only update if target is NULL/empty
                    if not existing.nome and sacado_reg.nome:
                        existing.nome = sacado_reg.nome
                        updated = True
                    if not existing.apelido and sacado_reg.apelido:
                        existing.apelido = sacado_reg.apelido
                        updated = True

                    # Address fields - use validated properties
                    if not existing.endereco and sacado_reg.valid_endereco:
                        existing.endereco = sacado_reg.valid_endereco
                        updated = True
                    if not existing.bairro and sacado_reg.valid_bairro:
                        existing.bairro = sacado_reg.valid_bairro
                        updated = True
                    if not existing.cidade and sacado_reg.valid_cidade:
                        existing.cidade = sacado_reg.valid_cidade
                        updated = True
                    if not existing.uf and sacado_reg.valid_uf:
                        existing.uf = sacado_reg.valid_uf
                        updated = True
                    if not existing.cep and sacado_reg.valid_cep:
                        existing.cep = sacado_reg.valid_cep
                        updated = True

                    # Aggregated fields (already validated) - only update if target is NULL/empty
                    if not existing.email and sacado_reg.compiled_email:
                        existing.email = sacado_reg.compiled_email
                        updated = True
                    if not existing.telefone and sacado_reg.compiled_telefone:
                        existing.telefone = sacado_reg.compiled_telefone
                        updated = True

                    # Calculated fields - only update if target is NULL
                    if existing.is_pf is None:
                        existing.is_pf = sacado_reg.calculated_is_pf
                        updated = True

                    # Conditional field: registrado based on completo
                    if existing.completo == 0:
                        existing.registrado = 0
                        updated = True

                    if updated:
                        stats["updated"] += 1
                    else:
                        stats["skipped"] += 1
                else:
                    # Create new record - use validated properties
                    new_sacado = Sacado(
                        nome=sacado_reg.nome,
                        apelido=sacado_reg.apelido,
                        cnpj=sacado_reg.cnpj,
                        endereco=sacado_reg.valid_endereco,
                        bairro=sacado_reg.valid_bairro,
                        cidade=sacado_reg.valid_cidade,
                        uf=sacado_reg.valid_uf,
                        cep=sacado_reg.valid_cep,
                        email=sacado_reg.compiled_email,
                        telefone=sacado_reg.compiled_telefone,
                        is_pf=sacado_reg.calculated_is_pf,
                        completo=0,
                        registrado=0
                    )
                    session.add(new_sacado)
                    stats["created"] += 1

            except Exception as e:
                print(f"  ✗ Error processing CNPJ {sacado_reg.cnpj}: {e}")
                stats["errors"] += 1
                continue

        # Commit all changes
        session.commit()

    print(f"\n  Created: {stats['created']}")
    print(f"  Updated: {stats['updated']}")
    print(f"  Skipped: {stats['skipped']}")
    print(f"  Errors:  {stats['errors']}")

    return stats


def main():
    """Main entry point."""
    print("=" * 70)
    print("Check Sacado Registers - Finding Incomplete Records")
    print("=" * 70)
    
    try:
        sacados = fetch_incomplete_sacados()
        
        print(f"\nFound {len(sacados)} sacado records with missing data\n")
        print("=" * 70)
        
        # Count missing fields
        missing_contato = sum(1 for s in sacados if s.has_missing_contato)
        missing_endereco = sum(1 for s in sacados if s.has_missing_endereco)
        
        print(f"Missing 'contato': {missing_contato}")
        print(f"Missing 'endereco': {missing_endereco}")
        print("=" * 70)
        
        # Display first 10 records as sample
        print("\nSample records (first 10):")
        print("-" * 70)
        for i, sacado in enumerate(sacados[:10], 1):
            missing = []
            if sacado.has_missing_contato:
                missing.append("contato")
            if sacado.has_missing_endereco:
                missing.append("endereco")
            
            print(f"{i}. {sacado.nome[:40]:<40} | CNPJ: {sacado.cnpj}")
            print(f"   Missing: {', '.join(missing)}")
            if sacado.dentrada:
                print(f"   Last entry: {sacado.dentrada.strftime('%Y-%m-%d')}")
            print()
        
        if len(sacados) > 10:
            print(f"... and {len(sacados) - 10} more records")

        # Synchronize to MariaDB
        if sacados:
            sync_stats = sync_sacados_to_mariadb(sacados)

            print("\n" + "=" * 70)
            print("Synchronization Complete!")
            print("=" * 70)

        return sacados

    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    result = main()

