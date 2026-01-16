#!/usr/bin/env python3
"""
Test cedente_grupo extraction logic against existing database records.
"""

import mysql.connector
import json
from pathlib import Path
from typing import Optional


def load_db_config() -> dict:
    """Load database configuration."""
    config_paths = [
        Path(__file__).parent / "databases_config.json",
        Path(__file__).parent.parent / "database" / "databases_config.json",
    ]
    
    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)
    
    raise FileNotFoundError("Database configuration not found")


def extract_cedente_grupo(name: str, is_group: bool) -> Optional[str]:
    """
    Extract cedente_grupo from WhatsApp group name.

    Args:
        name: Group or contact name
        is_group: Whether this is a group chat

    Returns:
        Cedente name in uppercase, or None
    """
    if not is_group:
        return None

    name_upper = name.upper()

    # Pattern 1: "Company X BMA" (most common - 90%)
    if " X BMA" in name_upper:
        pos = name_upper.find(" X BMA")
        cedente = name[:pos].strip().upper()
        return cedente if cedente else None

    # Pattern 2: "Company + BMA" (check before " - BMA" to handle "Finly + BMA - FIDC")
    if " + BMA" in name_upper:
        pos = name_upper.find(" + BMA")
        cedente = name[:pos].strip().upper()
        return cedente if cedente else None

    # Pattern 3: "BMA - Company" (reverse pattern)
    if "BMA -" in name_upper:
        pos = name_upper.find("BMA -")
        cedente = name[pos + 5:].strip().upper()  # Skip "BMA - "
        return cedente if cedente else None

    # Pattern 4: "Company - BMA"
    if " - BMA" in name_upper:
        pos = name_upper.find(" - BMA")
        cedente = name[:pos].strip().upper()
        return cedente if cedente else None

    # Pattern 5: "BMA + Company" or "BMA+Company"
    if "BMA+" in name_upper or "BMA +" in name_upper:
        # Extract after "BMA+"
        if "BMA+" in name_upper:
            pos = name_upper.find("BMA+")
            cedente = name[pos + 4:].strip().upper()
        else:
            pos = name_upper.find("BMA +")
            cedente = name[pos + 5:].strip().upper()
        return cedente if cedente else None

    # Pattern 6: "BMA & Company"
    if "BMA &" in name_upper:
        pos = name_upper.find("BMA &")
        cedente = name[pos + 5:].strip().upper()
        # Remove any prefix like "QI |" or similar
        if " | " in cedente:
            cedente = cedente.split(" | ", 1)[0].strip()
        return cedente if cedente else None

    # Pattern 7: "BMA/Company" or "BMA / Company"
    if "BMA/" in name_upper or "BMA /" in name_upper:
        if "BMA/" in name_upper:
            pos = name_upper.find("BMA/")
            cedente = name[pos + 4:].strip().upper()
        else:
            pos = name_upper.find("BMA /")
            cedente = name[pos + 5:].strip().upper()
        return cedente if cedente else None

    # No pattern matched
    return None


def main():
    """Test extraction logic."""
    config = load_db_config()
    cfg = config['databases']['mariadb']
    
    conn = mysql.connector.connect(
        host=cfg['server'],
        port=cfg['port'],
        user=cfg['user'],
        password=cfg['password'],
        database=cfg['scheme']
    )
    
    cursor = conn.cursor()
    
    print('=' * 80)
    print('Testing cedente_grupo Extraction Logic')
    print('=' * 80)
    
    # Get all records with cedente_grupo
    cursor.execute("""
        SELECT name, cedente_grupo, isGroup 
        FROM contato_whatsapp 
        WHERE cedente_grupo IS NOT NULL AND cedente_grupo != ''
        ORDER BY id_contato_whatsapp
        LIMIT 50
    """)
    
    rows = cursor.fetchall()
    
    matches = 0
    mismatches = 0
    
    print(f"\nTesting {len(rows)} records...\n")
    
    for name, expected_cedente, is_group in rows:
        extracted = extract_cedente_grupo(name, bool(is_group))
        
        if extracted == expected_cedente:
            matches += 1
            status = "✓"
        else:
            mismatches += 1
            status = "✗"
            print(f"{status} MISMATCH:")
            print(f"  Name:     {name}")
            print(f"  Expected: {expected_cedente}")
            print(f"  Extracted: {extracted}")
            print()
    
    # Summary
    print('=' * 80)
    print('Test Results')
    print('=' * 80)
    print(f"Total records:  {len(rows)}")
    print(f"Matches:        {matches} ({matches/len(rows)*100:.1f}%)")
    print(f"Mismatches:     {mismatches} ({mismatches/len(rows)*100:.1f}%)")
    print('=' * 80)
    
    # Test some edge cases
    print('\nEdge Case Tests:')
    print('=' * 80)
    
    test_cases = [
        # Pattern 1: "Company X BMA"
        ("Company X BMA", True, "COMPANY"),
        ("Company x BMA", True, "COMPANY"),
        ("Company X BMA FIDC", True, "COMPANY"),
        ("Company x BMA Grupo", True, "COMPANY"),
        ("UPPERCASE X BMA", True, "UPPERCASE"),
        ("Multi Word Company X BMA", True, "MULTI WORD COMPANY"),

        # Pattern 2: "BMA - Company"
        ("BMA - Gold RGBtec", True, "GOLD RGBTEC"),
        ("BMA - Company Name", True, "COMPANY NAME"),

        # Pattern 3: "Company - BMA"
        ("AI4Finance - BMA", True, "AI4FINANCE"),
        ("Robô Checagem - BMA", True, "ROBÔ CHECAGEM"),
        ("Finly + BMA - FIDC", True, "FINLY"),  # Will match " + BMA" first

        # Pattern 4: "Company + BMA" or "BMA + Company"
        ("Finly + BMA", True, "FINLY"),
        ("Bma+flix", True, "FLIX"),

        # Pattern 5: "BMA & Company"
        ("BMA & QI (CertifiQI)", True, "QI (CERTIFIQI)"),
        ("BMA & QI | White Label", True, "QI"),  # Extracts first part before |

        # Pattern 6: "BMA/Company"
        ("BMA/ECOLE", True, "ECOLE"),
        ("BMA / Company", True, "COMPANY"),

        # No pattern
        ("No Pattern Here", True, None),
        ("Individual Contact", False, None),
        ("", True, None),
    ]
    
    for name, is_group, expected in test_cases:
        result = extract_cedente_grupo(name, is_group)
        status = "✓" if result == expected else "✗"
        print(f"{status} '{name}' (group={is_group}) → {result} (expected: {expected})")
    
    cursor.close()
    conn.close()


if __name__ == "__main__":
    main()

