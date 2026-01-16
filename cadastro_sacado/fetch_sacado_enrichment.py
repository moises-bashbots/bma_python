#!/usr/bin/env python3
"""
Fetch Sacado Enrichment

This program fetches enrichment data for incomplete sacado records from the Vadu API
and updates the MariaDB database with the enriched data.
"""

import json
import os
import re
import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import certifi
import requests
from sqlalchemy import create_engine, select, update
from sqlalchemy.orm import Session

from models import Sacado

# Fix for PyInstaller bundled certifi - set SSL cert path
os.environ['SSL_CERT_FILE'] = certifi.where()
os.environ['REQUESTS_CA_BUNDLE'] = certifi.where()


# Global config holders (loaded at runtime)
VADU_CONFIG: dict = {}
DB_CONFIG: dict = {}


@dataclass
class VaduResponse:
    """Dataclass representing the Vadu API response."""
    success: bool
    cnpj: str
    data: Optional[dict] = None
    error: Optional[str] = None

    def __str__(self) -> str:
        if self.success:
            return f"VaduResponse(cnpj={self.cnpj}, success=True)"
        return f"VaduResponse(cnpj={self.cnpj}, success=False, error={self.error})"


# =============================================================================
# Validation Functions (same as check_sacado_registers.py)
# =============================================================================

def is_valid_single_phone(phone: str) -> bool:
    """Validate a single phone number. Must have at least 8 digits."""
    cleaned = phone.strip()
    if not cleaned:
        return False
    digits = ''.join(c for c in cleaned if c.isdigit())
    if len(digits) < 8:
        return False
    if len(set(digits)) == 1:
        return False
    invalid_patterns = ['00000000', '11111111', '12345678', '99999999']
    if digits in invalid_patterns or digits[:8] in invalid_patterns:
        return False
    return True


def is_valid_phone(phone: Optional[str]) -> bool:
    """Validate phone field (can be comma-separated list of phones)."""
    if not phone:
        return False
    # Split by comma and check if at least one valid phone exists
    phones = [p.strip() for p in phone.split(',')]
    return any(is_valid_single_phone(p) for p in phones)


def is_valid_single_email(email: str) -> bool:
    """Validate a single email address."""
    cleaned = email.strip()
    if not cleaned or '@' not in cleaned:
        return False
    parts = cleaned.split('@')
    if len(parts) != 2:
        return False
    local, domain = parts
    if not local or not domain or '.' not in domain:
        return False
    invalid_patterns = ['teste@', 'test@', 'xxx@', 'nao@', 'sem@', 'null@', 'none@']
    if any(cleaned.lower().startswith(p) for p in invalid_patterns):
        return False
    return True


def is_valid_email(email: Optional[str]) -> bool:
    """Validate email field (can be comma or semicolon-separated list of emails)."""
    if not email:
        return False
    # Split by comma or semicolon and check if at least one valid email exists
    emails = [e.strip() for e in re.split(r'[,;]', email)]
    return any(is_valid_single_email(e) for e in emails)


def normalize_email_separators(email: Optional[str]) -> Optional[str]:
    """Normalize email separators to use comma instead of semicolon."""
    if not email:
        return email
    # Replace semicolons with commas
    if ';' in email:
        return email.replace(';', ',')
    return email


def is_valid_address(address: Optional[str]) -> bool:
    """Validate address field."""
    if not address:
        return False
    cleaned = address.strip()
    if not cleaned or len(cleaned) < 3:
        return False
    if not any(c.isalpha() for c in cleaned):
        return False
    invalid_patterns = [
        'xxx', 'nao informado', 'não informado', 'n/a', 'n/d',
        'nao tem', 'não tem', 'sem endereco', 'sem endereço',
        'null', 'none', 'teste', 'test', '...', '---', '***'
    ]
    if cleaned.lower() in invalid_patterns:
        return False
    return True


def is_valid_uf(uf: Optional[str]) -> bool:
    """Validate UF (state code)."""
    if not uf:
        return False
    cleaned = uf.strip()
    return len(cleaned) == 2 and cleaned.isalpha()


def is_valid_cep(cep: Optional[str]) -> bool:
    """Validate CEP (postal code). Must have exactly 8 digits."""
    if not cep:
        return False
    digits = ''.join(c for c in cep if c.isdigit())
    return len(digits) == 8


# =============================================================================
# Configuration Loading
# =============================================================================

def get_config_path() -> Path:
    """Get the path to the configuration files."""
    if getattr(sys, 'frozen', False):
        return Path(sys.executable).parent
    return Path(__file__).parent


def load_vadu_config() -> dict:
    """Load Vadu API configuration."""
    config_path = get_config_path() / "api_vadu_config.json"
    if not config_path.exists():
        raise FileNotFoundError(f"Vadu config not found: {config_path}")
    with open(config_path, 'r') as f:
        return json.load(f)['vadu_api']


def load_db_config() -> dict:
    """Load database configuration."""
    config_path = get_config_path() / "databases_config.json"
    if not config_path.exists():
        raise FileNotFoundError(f"Database config not found: {config_path}")
    with open(config_path, 'r') as f:
        return json.load(f)['databases']


def get_mariadb_engine():
    """Create SQLAlchemy engine for MariaDB."""
    config = DB_CONFIG['mariadb']
    connection_string = (
        f"mysql+mysqlconnector://{config['user']}:{config['password']}"
        f"@{config['server']}:{config['port']}/{config['scheme']}"
    )
    return create_engine(connection_string, echo=False)


# =============================================================================
# Vadu API Functions
# =============================================================================

def fetch_cnpj_data(cnpj: str, params: Optional[dict] = None) -> VaduResponse:
    """
    Fetch enrichment data for a CNPJ from the Vadu API.

    Args:
        cnpj: The CNPJ to query (14 digits, no formatting)
        params: Optional query parameters to override defaults

    Returns:
        VaduResponse object with the API response data
    """
    # Clean CNPJ - remove any formatting
    clean_cnpj = ''.join(c for c in cnpj if c.isdigit())

    if len(clean_cnpj) != 14:
        return VaduResponse(
            success=False,
            cnpj=cnpj,
            error=f"Invalid CNPJ length: {len(clean_cnpj)} (expected 14)"
        )

    # Build URL with CNPJ using config
    url = f"{VADU_CONFIG['url']}/{clean_cnpj}"

    # Merge default params with any overrides
    query_params = VADU_CONFIG['default_params'].copy()
    if params:
        query_params.update(params)

    # Set headers
    headers = {
        "Authorization": f"Bearer {VADU_CONFIG['token']}",
        "Content-Type": "application/json"
    }

    try:
        response = requests.post(
            url,
            params=query_params,
            headers=headers,
            timeout=30
        )

        # Handle 429 Too Many Requests - skip and continue
        if response.status_code == 429:
            return VaduResponse(
                success=False, cnpj=clean_cnpj,
                error="Rate limited (429)"
            )

        response.raise_for_status()
        data = response.json()

        return VaduResponse(success=True, cnpj=clean_cnpj, data=data)

    except requests.exceptions.Timeout:
        return VaduResponse(success=False, cnpj=clean_cnpj, error="Request timed out")
    except requests.exceptions.HTTPError as e:
        return VaduResponse(
            success=False, cnpj=clean_cnpj,
            error=f"HTTP error: {e.response.status_code} - {e.response.text}"
        )
    except requests.exceptions.RequestException as e:
        return VaduResponse(success=False, cnpj=clean_cnpj, error=f"Request failed: {str(e)}")
    except json.JSONDecodeError as e:
        return VaduResponse(success=False, cnpj=clean_cnpj, error=f"JSON parse error: {str(e)}")


# =============================================================================
# Field Mapping and Enrichment
# =============================================================================

def build_endereco_from_vadu(data: dict) -> Optional[str]:
    """Build complete address from Vadu API fields."""
    parts = []
    if data.get('Logradouro'):
        parts.append(data['Logradouro'])
    if data.get('NumeroLogradouro'):
        parts.append(data['NumeroLogradouro'])
    if data.get('ComplementoEndereco'):
        parts.append(data['ComplementoEndereco'])

    endereco = ', '.join(parts) if parts else None
    return endereco if is_valid_address(endereco) else None


def extract_vadu_fields(data: dict) -> dict:
    """Extract and validate relevant fields from Vadu API response."""
    endereco = build_endereco_from_vadu(data)
    bairro = data.get('BairroEndereco')
    cidade = data.get('MunicipioEndereco')
    uf = data.get('UfEndereco')
    telefone = data.get('TelefonePrincipal')
    email = data.get('EmailPrincipal')
    nome = data.get('Nome')

    # Extract CEP and keep only digits
    cep_raw = data.get('CepEnderecoFormatado')
    cep = ''.join(c for c in (cep_raw or '') if c.isdigit()) if cep_raw else None

    return {
        'nome': nome if nome else None,
        'endereco': endereco if is_valid_address(endereco) else None,
        'bairro': bairro if is_valid_address(bairro) else None,
        'cidade': cidade if is_valid_address(cidade) else None,
        'uf': uf.strip().upper() if is_valid_uf(uf) else None,
        'cep': cep if is_valid_cep(cep) else None,
        'telefone': telefone if is_valid_phone(telefone) else None,
        'email': email if is_valid_email(email) else None,
    }


def is_record_complete(sacado: Sacado) -> bool:
    """
    Check if a sacado record has all required fields populated with VALID data.
    Requires: valid phone AND valid email AND valid address fields.
    """
    has_phone = is_valid_phone(sacado.telefone)
    has_email = is_valid_email(sacado.email)
    has_address = (
        is_valid_address(sacado.endereco) and
        is_valid_address(sacado.bairro) and
        is_valid_address(sacado.cidade) and
        is_valid_uf(sacado.uf)
    )
    return has_phone and has_email and has_address


def fetch_incomplete_sacados(limit: int = 20) -> list[Sacado]:
    """Fetch incomplete sacado records from MariaDB that haven't been enriched by Vadu."""
    engine = get_mariadb_engine()

    with Session(engine) as session:
        stmt = (
            select(Sacado)
            .where(Sacado.completo == 0)
            .where(Sacado.is_pf == 0)
            .where((Sacado.is_enrich_vadu == 0) | (Sacado.is_enrich_vadu.is_(None)))
            .limit(limit)
        )
        results = session.execute(stmt).scalars().all()
        # Detach from session
        sacados = [s for s in results]
        for s in sacados:
            session.expunge(s)
        return sacados


def enrich_sacado(sacado: Sacado, vadu_data: dict, session: Session) -> tuple[bool, bool]:
    """
    Enrich a sacado record with Vadu API data.
    Updates NULL/empty fields OR invalid fields with valid API data.
    Uses explicit UPDATE statements to ensure changes are persisted.
    Returns (was_updated, is_complete) tuple.
    """
    fields = extract_vadu_fields(vadu_data)

    # Build update values dict
    update_values = {}

    # Update NULL/empty fields OR invalid fields with valid API data
    if fields['nome'] and not sacado.nome:
        update_values['nome'] = fields['nome']

    # Address fields - update if empty OR invalid
    if fields['endereco'] and (not sacado.endereco or not is_valid_address(sacado.endereco)):
        update_values['endereco'] = fields['endereco']
    if fields['bairro'] and (not sacado.bairro or not is_valid_address(sacado.bairro)):
        update_values['bairro'] = fields['bairro']
    if fields['cidade'] and (not sacado.cidade or not is_valid_address(sacado.cidade)):
        update_values['cidade'] = fields['cidade']
    if fields['uf'] and (not sacado.uf or not is_valid_uf(sacado.uf)):
        update_values['uf'] = fields['uf']
    if fields['cep'] and (not sacado.cep or not is_valid_cep(sacado.cep)):
        update_values['cep'] = fields['cep']

    # Contact fields - update if empty OR invalid
    if fields['telefone'] and (not sacado.telefone or not is_valid_phone(sacado.telefone)):
        update_values['telefone'] = fields['telefone']
    if fields['email'] and (not sacado.email or not is_valid_email(sacado.email)):
        update_values['email'] = fields['email']

    # Normalize email separators (replace ; with ,)
    current_email = update_values.get('email', sacado.email)
    normalized_email = normalize_email_separators(current_email)
    if normalized_email != current_email:
        update_values['email'] = normalized_email

    # Always mark as enriched by Vadu API (we have valid API data)
    update_values['is_enrich_vadu'] = 1

    # Check if record will be complete after updates
    # Create a temporary view of the record with updates applied
    final_telefone = update_values.get('telefone', sacado.telefone)
    final_email = update_values.get('email', sacado.email)
    final_endereco = update_values.get('endereco', sacado.endereco)
    final_bairro = update_values.get('bairro', sacado.bairro)
    final_cidade = update_values.get('cidade', sacado.cidade)
    final_uf = update_values.get('uf', sacado.uf)
    final_cep = update_values.get('cep', sacado.cep)

    is_complete = (
        is_valid_phone(final_telefone) and
        is_valid_email(final_email) and
        is_valid_address(final_endereco) and
        is_valid_address(final_bairro) and
        is_valid_address(final_cidade) and
        is_valid_uf(final_uf) and
        is_valid_cep(final_cep)
    )

    if is_complete:
        update_values['completo'] = 1

    # Execute the UPDATE statement
    stmt = update(Sacado).where(Sacado.id_sacado == sacado.id_sacado).values(**update_values)
    session.execute(stmt)

    # Return (was_updated, is_complete)
    field_updates = {k: v for k, v in update_values.items() if k not in ('is_enrich_vadu', 'completo')}
    return len(field_updates) > 0, is_complete


def is_already_complete(sacado: Sacado) -> bool:
    """
    Check if a sacado record already has valid phone, email, and address data.
    If so, it should be marked as complete without calling Vadu API.
    Requires: valid phone AND valid email AND valid address fields.
    """
    has_phone = is_valid_phone(sacado.telefone)
    has_email = is_valid_email(sacado.email)
    has_address = (
        is_valid_address(sacado.endereco) and
        is_valid_address(sacado.bairro) and
        is_valid_address(sacado.cidade) and
        is_valid_uf(sacado.uf)
    )
    return has_phone and has_email and has_address


def print_sacado_status(sacado: Sacado) -> None:
    """Print current field status for a sacado record."""
    phone_ok = "✓" if is_valid_phone(sacado.telefone) else "✗"
    email_ok = "✓" if is_valid_email(sacado.email) else "✗"
    endereco_ok = "✓" if is_valid_address(sacado.endereco) else "✗"
    bairro_ok = "✓" if is_valid_address(sacado.bairro) else "✗"
    cidade_ok = "✓" if is_valid_address(sacado.cidade) else "✗"
    uf_ok = "✓" if is_valid_uf(sacado.uf) else "✗"

    print(f"      telefone: {phone_ok} '{sacado.telefone or ''}'")
    print(f"      email:    {email_ok} '{sacado.email or ''}'")
    print(f"      endereco: {endereco_ok} '{sacado.endereco or ''}'")
    print(f"      bairro:   {bairro_ok} '{sacado.bairro or ''}'")
    print(f"      cidade:   {cidade_ok} '{sacado.cidade or ''}'")
    print(f"      uf:       {uf_ok} '{sacado.uf or ''}'")


def fetch_api_for_sacado(sacado: Sacado) -> tuple[Sacado, VaduResponse]:
    """Fetch API data for a single sacado. Used for concurrent execution."""
    response = fetch_cnpj_data(sacado.cnpj)
    return (sacado, response)


def process_enrichment(limit: int = 20, delay: float = 0.2, max_workers: int = 5) -> dict:
    """
    Main enrichment process with optimizations:
    - Batch commits for already-complete records
    - Concurrent API requests using ThreadPoolExecutor
    - Reduced delay between API calls
    """
    stats = {"total": 0, "enriched": 0, "completed": 0, "errors": 0, "skipped": 0, "already_complete": 0}

    print(f"\nFetching incomplete sacado records (limit={limit})...")
    sacados = fetch_incomplete_sacados(limit)
    stats["total"] = len(sacados)
    print(f"Found {len(sacados)} incomplete records\n")

    if not sacados:
        return stats

    engine = get_mariadb_engine()

    # Phase 1: Process already-complete records (batch commit, no API calls)
    print("=" * 60)
    print("Phase 1: Checking for already-complete records...")
    print("=" * 60)

    already_complete_records = []
    needs_api_records = []

    for sacado in sacados:
        if is_already_complete(sacado):
            already_complete_records.append(sacado)
        else:
            needs_api_records.append(sacado)

    # Batch update already-complete records
    if already_complete_records:
        print(f"\nFound {len(already_complete_records)} already-complete records. Batch updating...")
        with Session(engine) as session:
            for sacado in already_complete_records:
                normalized_email = normalize_email_separators(sacado.email)
                update_values = {"completo": 1}
                if normalized_email != sacado.email:
                    update_values["email"] = normalized_email
                    print(f"  [{sacado.id_sacado}] Normalizing email: '{sacado.email}' → '{normalized_email}'")

                stmt = update(Sacado).where(Sacado.id_sacado == sacado.id_sacado).values(**update_values)
                session.execute(stmt)
                stats["already_complete"] += 1
                stats["completed"] += 1

            session.commit()
            print(f"✓ Batch updated {len(already_complete_records)} records as complete")

    if not needs_api_records:
        print("\nNo records need API enrichment.")
        return stats

    # Phase 2: Fetch API data concurrently
    print("\n" + "=" * 60)
    print(f"Phase 2: Fetching API data for {len(needs_api_records)} records...")
    print(f"Using {max_workers} concurrent workers, {delay}s delay between batches")
    print("=" * 60)

    api_results = []

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        # Submit all API requests
        future_to_sacado = {
            executor.submit(fetch_api_for_sacado, sacado): sacado
            for sacado in needs_api_records
        }

        # Collect results as they complete
        for i, future in enumerate(as_completed(future_to_sacado), 1):
            try:
                sacado, response = future.result()
                api_results.append((sacado, response))
                print(f"  [{i}/{len(needs_api_records)}] CNPJ {sacado.cnpj}: {'OK' if response.success else 'Error'}")
            except Exception as e:
                sacado = future_to_sacado[future]
                print(f"  [{i}/{len(needs_api_records)}] CNPJ {sacado.cnpj}: Exception - {e}")
                api_results.append((sacado, VaduResponse(success=False, cnpj=sacado.cnpj, error=str(e), data=None)))

            # Small delay to avoid overwhelming the API
            if i < len(needs_api_records) and i % max_workers == 0:
                time.sleep(delay)

    # Phase 3: Update database with API results
    print("\n" + "=" * 60)
    print("Phase 3: Updating database with API results...")
    print("=" * 60)

    with Session(engine) as session:
        for sacado, response in api_results:
            print(f"\n[{sacado.id_sacado}] CNPJ: {sacado.cnpj}")
            print(f"    Nome: {sacado.nome or '(empty)'}")

            # Do NOT mark is_enrich_vadu=1 for failed API calls
            if not response.success:
                print(f"    → API Error: {response.error}")
                stats["errors"] += 1
                continue

            # Do NOT mark is_enrich_vadu=1 when API returns no data
            if not response.data:
                print(f"    → No data returned from API")
                stats["skipped"] += 1
                continue

            # At this point we have valid API data - enrich the record
            # enrich_sacado() uses explicit UPDATE and always sets is_enrich_vadu=1
            try:
                was_updated, is_complete = enrich_sacado(sacado, response.data, session)

                if was_updated:
                    print(f"    → Updated fields from API, is_enrich_vadu=1")
                    stats["enriched"] += 1
                    if is_complete:
                        print(f"    → Record now complete")
                        stats["completed"] += 1
                else:
                    print(f"    → API data compared, no updates needed, is_enrich_vadu=1")
                    stats["skipped"] += 1

            except Exception as e:
                # Do NOT mark is_enrich_vadu=1 on exception
                print(f"    → Error: {e}")
                stats["errors"] += 1
                continue

        # Commit all changes in one batch
        print(f"\nCommitting all changes to database...")
        session.commit()
        print("✓ Done.")

    return stats


# =============================================================================
# Main Entry Point
# =============================================================================

def main():
    """Main entry point."""
    global VADU_CONFIG, DB_CONFIG

    print("=" * 70)
    print("Fetch Sacado Enrichment - Vadu API")
    print("=" * 70)

    try:
        # Load configurations
        print("Loading configurations...")
        VADU_CONFIG = load_vadu_config()
        DB_CONFIG = load_db_config()
        print("✓ Configurations loaded\n")

        # Parse command line args
        limit = 20
        max_workers = 5
        delay = 0.2

        if len(sys.argv) > 1:
            try:
                limit = int(sys.argv[1])
            except ValueError:
                print(f"Warning: Invalid limit '{sys.argv[1]}', using default {limit}")

        if len(sys.argv) > 2:
            try:
                max_workers = int(sys.argv[2])
            except ValueError:
                print(f"Warning: Invalid workers '{sys.argv[2]}', using default {max_workers}")

        print(f"Settings: limit={limit}, workers={max_workers}, delay={delay}s")

        # Run enrichment process
        stats = process_enrichment(limit=limit, delay=delay, max_workers=max_workers)

        # Print summary
        print("\n" + "=" * 70)
        print("Enrichment Summary")
        print("=" * 70)
        print(f"  Total records processed: {stats['total']}")
        print(f"  Already complete:        {stats['already_complete']}")
        print(f"  Enriched from API:       {stats['enriched']}")
        print(f"  Completed (all fields):  {stats['completed']}")
        print(f"  Skipped (no new data):   {stats['skipped']}")
        print(f"  Errors:                  {stats['errors']}")
        print("=" * 70)

        return stats

    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()

