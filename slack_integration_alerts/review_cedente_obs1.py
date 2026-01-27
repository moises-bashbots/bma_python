#!/usr/bin/env python3
"""
Review obs1 field from CEDENTE table for records containing "pre" and "impresso"
"""

import sys
from pathlib import Path
from sqlalchemy import create_engine, or_
from sqlalchemy.orm import sessionmaker

# Add parent directory to path to import database models
sys.path.insert(0, str(Path(__file__).parent))

from query_apr_invalidos_status import Cedente, load_config, parse_pre_impresso_ranges, create_connection_string


def review_cedente_obs1():
    """
    Query and display all CEDENTE records with obs1 containing "pre" and "impresso"
    """
    # Load database configuration
    config = load_config()

    # Build connection string for MSSQL
    connection_string = create_connection_string(config)

    print("=" * 140)
    print("CEDENTE OBS1 REVIEW - Records with 'pré-impresso' or 'pre-impresso'")
    print("=" * 140)
    print(f"\nConnecting to database...")
    print(f"Server: {config['databases']['mssql']['server']}")
    print(f"Database: {config['databases']['mssql']['scheme']}\n")
    
    engine = create_engine(connection_string)
    Session = sessionmaker(bind=engine)
    session = Session()
    
    # Query cedentes with pre-impresso in obs1 (using NOLOCK to avoid blocking)
    cedentes = session.query(Cedente).with_hint(Cedente, 'WITH (NOLOCK)').filter(
        Cedente.obs1 != None,
        or_(
            Cedente.obs1.like('%pre%impresso%'),
            Cedente.obs1.like('%pré%impresso%'),
            Cedente.obs1.like('%pre%ìmpresso%'),
            Cedente.obs1.like('%pré%ìmpresso%'),
        )
    ).order_by(Cedente.apelido).all()
    
    print(f"Found {len(cedentes)} cedentes with 'pré-impresso' or 'pre-impresso' in obs1 field\n")
    print("=" * 140)
    
    for i, cedente in enumerate(cedentes, 1):
        print(f"\n[{i}] CEDENTE: {cedente.apelido}")
        print(f"    RAMO: {cedente.ramo or 'N/A'}")
        print(f"    OBS1:")
        print(f"    {'-' * 130}")
        
        # Print obs1 with proper formatting (handle multi-line text)
        if cedente.obs1:
            obs1_lines = cedente.obs1.split('\n')
            for line in obs1_lines:
                print(f"    {line}")
        else:
            print("    (empty)")
        
        print(f"    {'-' * 130}")
        
        # Try to parse ranges
        parsed_ranges = parse_pre_impresso_ranges(cedente.obs1)
        if parsed_ranges:
            ranges_str = ", ".join([f"{company}:{range_val}" for company, range_val in parsed_ranges])
            print(f"    PARSED RANGES: {ranges_str}")
        else:
            print(f"    PARSED RANGES: (none found)")
        
        print("=" * 140)
    
    session.close()
    
    print(f"\n✓ Review completed - {len(cedentes)} cedentes found")


if __name__ == "__main__":
    review_cedente_obs1()

