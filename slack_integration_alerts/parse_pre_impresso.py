#!/usr/bin/env python3
"""
Parse obs1 to extract pre-impresso boleto information.
Identifies sequences: "pre" + "impresso" + company (BMA FIDC/BMA INTER/BMA) + "faixa" + number
"""

import json
import sys
import re
from pathlib import Path
from sqlalchemy import create_engine, or_
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


def extract_company_range_pairs(text: str) -> list[dict]:
    """
    Extract ALL company/range pairs from pre-impresso text.

    Returns list of dictionaries with: company, range
    """
    if not text:
        return []

    results = []

    # Strategy: Find all company mentions, then look for numbers after them
    # Company patterns (case insensitive)
    company_patterns = [
        (r'BMA\s*FIDC', 'BMA FIDC'),
        (r'BMA\s*INTER', 'BMA INTER'),
        (r'BMA\s*SEC', 'BMA SEC'),
        (r'BMA\s*CAPITAL', 'BMA CAPITAL'),
        (r'Fidc', 'BMA FIDC'),  # Sometimes just "Fidc" is mentioned
        (r'Inter(?!\s*(?:company|nacional))', 'BMA INTER'),  # "Inter" but not "Intercompany"
    ]

    # Also look for standalone "BMA" not followed by FIDC/INTER/SEC/CAPITAL
    company_patterns.append((r'BMA(?!\s*(?:FIDC|INTER|SEC|CAPITAL|Fidc|Inter))', 'BMA'))

    for pattern, normalized_name in company_patterns:
        # Find all matches of this company pattern
        for match in re.finditer(pattern, text, re.IGNORECASE):
            company_start = match.start()
            company_end = match.end()

            # Look ahead up to 120 characters for a number
            lookahead_text = text[company_end:company_end + 120]

            # Try multiple patterns to find the range number
            # Pattern 1: After "faixa" keyword (with optional N/N, numero, etc.)
            faixa_pattern = r'(?:faixa|n/n|numero|nº|n°)[\s:\-]*(\d{1,5}|esquerda)'
            faixa_match = re.search(faixa_pattern, lookahead_text, re.IGNORECASE)

            if faixa_match:
                range_value = faixa_match.group(1)
                results.append({
                    'company': normalized_name,
                    'range': range_value,
                    'position': company_start
                })
                continue

            # Pattern 2: Direct number after separators (-, :, whitespace)
            # Look for 2-5 digit numbers
            direct_pattern = r'[\s\-:]+(\d{2,5})(?:\s|$|\.|\||/)'
            direct_match = re.search(direct_pattern, lookahead_text)

            if direct_match:
                range_value = direct_match.group(1)
                results.append({
                    'company': normalized_name,
                    'range': range_value,
                    'position': company_start
                })
                continue

            # Pattern 3: Number after "N/N" even if separated
            nn_pattern = r'N/N[\s:\-]*(\d{1,5})'
            nn_match = re.search(nn_pattern, lookahead_text, re.IGNORECASE)

            if nn_match:
                range_value = nn_match.group(1)
                results.append({
                    'company': normalized_name,
                    'range': range_value,
                    'position': company_start
                })
                continue

    # Sort by position in text to maintain order
    results.sort(key=lambda x: x['position'])

    # Remove duplicates while preserving order
    seen = set()
    unique_results = []
    for item in results:
        key = (item['company'], item['range'])
        if key not in seen:
            seen.add(key)
            unique_results.append({
                'company': item['company'],
                'range': item['range']
            })

    return unique_results


def query_and_extract_company_ranges(session):
    """Query cedentes and extract company/range pairs in table format."""
    print("=" * 100)
    print("EXTRACTING COMPANY/RANGE PAIRS FROM PRE-IMPRESSO BOLETOS")
    print("=" * 100)

    # Query cedentes with "pre" and "impresso"
    cedentes = session.query(Cedente).filter(
        Cedente.obs1 != None,
        or_(
            Cedente.obs1.like('%pre%impresso%'),
            Cedente.obs1.like('%pré%impresso%'),
            Cedente.obs1.like('%pre%ìmpresso%'),
            Cedente.obs1.like('%pré%ìmpresso%'),
        )
    ).all()

    print(f"\nFound {len(cedentes)} cedentes with pre-impresso references\n")

    # Collect all company/range pairs
    all_pairs = []
    cedentes_with_pairs = 0
    cedentes_without_pairs = []

    for cedente in cedentes:
        obs1_text = str(cedente.obs1) if cedente.obs1 else ""

        # Extract company/range pairs
        pairs = extract_company_range_pairs(obs1_text)

        if pairs:
            cedentes_with_pairs += 1
            for pair in pairs:
                all_pairs.append({
                    'cedente': cedente.apelido,
                    'nome': cedente.nome,
                    'company': pair['company'],
                    'range': pair['range'],
                    'gerente': cedente.gerente,
                    'ativo': cedente.ativo
                })
        else:
            cedentes_without_pairs.append({
                'cedente': cedente,
                'obs1': obs1_text
            })

    # Display table header
    print("\n" + "=" * 100)
    print(f"{'CEDENTE':<20} | {'COMPANY':<15} | {'RANGE':<15} | {'GERENTE':<12} | {'ATIVO':<6}")
    print("=" * 100)

    # Display all pairs
    for pair in all_pairs:
        ativo_str = 'SIM' if pair['ativo'] else 'NÃO'
        print(f"{pair['cedente']:<20} | {pair['company']:<15} | {pair['range']:<15} | {pair['gerente']:<12} | {ativo_str:<6}")

    print("=" * 100)

    # Statistics
    print(f"\n{'=' * 100}")
    print("STATISTICS")
    print(f"{'=' * 100}")
    print(f"Total cedentes queried: {len(cedentes)}")
    print(f"Cedentes with extracted pairs: {cedentes_with_pairs}")
    print(f"Cedentes without pairs: {len(cedentes_without_pairs)}")
    print(f"Total company/range pairs: {len(all_pairs)}")

    # Company breakdown
    company_counts = {}
    for pair in all_pairs:
        company = pair['company']
        company_counts[company] = company_counts.get(company, 0) + 1

    print(f"\nBy Company:")
    for company, count in sorted(company_counts.items(), key=lambda x: x[1], reverse=True):
        print(f"  {company}: {count} occurrences")

    # Show cedentes without pairs
    if cedentes_without_pairs:
        print(f"\n{'=' * 100}")
        print(f"CEDENTES WITHOUT EXTRACTED PAIRS ({len(cedentes_without_pairs)})")
        print(f"{'=' * 100}\n")

        for i, item in enumerate(cedentes_without_pairs[:20], 1):
            cedente = item['cedente']
            obs1_preview = item['obs1'][:150].replace('\n', ' ')
            print(f"{i}. {cedente.apelido} ({cedente.gerente})")
            print(f"   obs1: {obs1_preview}...")
            print()

        if len(cedentes_without_pairs) > 20:
            print(f"... and {len(cedentes_without_pairs) - 20} more")

    print("=" * 100)

    return all_pairs


def main():
    """Main entry point."""
    try:
        print("\nConnecting to database...")
        engine = create_db_engine()
        Session = sessionmaker(bind=engine)
        session = Session()
        print("✓ Connected successfully!\n")

        # Query and extract company/range pairs
        results = query_and_extract_company_ranges(session)

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

