#!/usr/bin/env python3
"""
Test parse_pre_impresso_ranges() function without database access
"""

import re

def parse_pre_impresso_ranges(obs1_text: str) -> list:
    """
    Parse pre-impresso ranges from obs1 field.
    Returns list of (company, range) tuples.
    """
    if not obs1_text:
        return []

    ranges = []

    # Handle multi-line format: concatenate lines with spaces
    obs1_normalized = ' '.join(obs1_text.split('\n'))
    obs1_upper = obs1_normalized.upper()

    # Check if text contains "pré-impresso" or "pre-impresso"
    has_pre_impresso = ('PRE' in obs1_upper or 'PRÉ' in obs1_upper) and 'IMPRESSO' in obs1_upper

    if not has_pre_impresso:
        return []

    # SPECIAL CASE 1: IGNORE if "parametrização" appears near "pré-impresso"
    # Only ignore if parametrização is in the same context as pré-impresso
    # Pattern: "pré-impresso" followed by "parametrização" within ~50 characters
    if re.search(r'PR[EÉ][\s\-]*[IÌ]MPRESSO.{0,50}PARAMETRI[ZS]A[ÇC][ÃA]O', obs1_upper):
        return []  # Ignore cases being configured
    # Also check reverse: "parametrização" followed by "pré-impresso" within ~50 characters
    if re.search(r'PARAMETRI[ZS]A[ÇC][ÃA]O.{0,50}PR[EÉ][\s\-]*[IÌ]MPRESSO', obs1_upper):
        return []  # Ignore cases being configured

    # SPECIAL CASE 2: Check for webfact with pre-impresso
    # These cases get assigned range 200
    has_webfact = 'WEBFACT' in obs1_upper

    if has_webfact:
        ranges.append(("BMA", "200"))
        return ranges  # Return immediately for webfact cases

    # First, try to find the company name mentioned in the text (for Matriz/Filial inheritance)
    # This is used even if the company doesn't have a direct range number
    first_company = None
    company_mention_pattern = r'(BMA\s+(?:FIDC|INTER|SEC)|FIDC|BMA)'
    company_mention_match = re.search(company_mention_pattern, obs1_upper)

    if company_mention_match:
        company = company_mention_match.group(1).strip()
        # Normalize company name
        if 'SEC' in company:
            first_company = 'BMA'  # BMA SEC => BMA
        elif 'FIDC' in company:
            first_company = 'BMA FIDC'
        elif 'INTER' in company:
            first_company = 'BMA INTER'
        else:
            first_company = 'BMA'

    # Main pattern: Find company + range combinations
    # Supports: BMA FIDC, BMA INTER, BMA SEC, BMA, FIDC (standalone)
    company_range_pattern = r'(BMA\s+(?:FIDC|INTER|SEC)|FIDC|BMA)[\s\-:\.]*(?:FAIXA)?[\s\-:]*(?:[N/]+)?[\s\-:]*(\d{1,5})'

    for match in re.finditer(company_range_pattern, obs1_upper):
        company = match.group(1).strip()
        range_val = match.group(2).strip()

        if range_val and company:  # Both company and range must be present
            # Normalize company name
            if 'SEC' in company:
                company_normalized = 'BMA'  # BMA SEC => BMA
            elif 'FIDC' in company:
                company_normalized = 'BMA FIDC'
            elif 'INTER' in company:
                company_normalized = 'BMA INTER'
            else:
                company_normalized = 'BMA'

            # Avoid duplicates
            if (company_normalized, range_val) not in ranges:
                ranges.append((company_normalized, range_val))

    # SPECIAL CASE 3: Handle "Matriz faixa X" and "Filial Y" patterns
    # These inherit the first company found, or default to BMA
    # Example: "Pré-impresso BMA FIDC - Matriz faixa 3 - Filial 5061"
    #          => BMA FIDC:3, BMA FIDC:5061
    matriz_filial_pattern = r'(?:MATRIZ|FILIAL)[\s\-]*(?:FAIXA)?[\s\-]*(\d{1,5})'

    for match in re.finditer(matriz_filial_pattern, obs1_upper):
        range_val = match.group(1).strip()

        if range_val:
            # Use first company found, or default to BMA
            company_for_matriz_filial = first_company if first_company else 'BMA'

            # Avoid duplicates
            if (company_for_matriz_filial, range_val) not in ranges:
                ranges.append((company_for_matriz_filial, range_val))

    return ranges


# Test cases from the actual cedente obs1 fields
test_cases = [
    # Webfact cases - should return range 200
    ("Boleto Pré Impresso BMA FIDC - Via Webfact", "BMA:200"),
    ("Boleto Pré Impresso - Via Webfact", "BMA:200"),

    # Normal cases
    ("Boleto pré-impresso - BMA FIDC faixa n/n 5233", "BMA FIDC:5233"),
    ("Pré-impresso BMA INTER - Faixa 5101", "BMA INTER:5101"),
    ("Boleto Pré impresso BMA FIDC - Faixa N/N - 02014", "BMA FIDC:02014"),
    ("Pré-impresso Bma Fidc - Faixa 5065", "BMA FIDC:5065"),
    ("Boleto Pré Impresso BMA FIDC- Faixa - 0125", "BMA FIDC:0125"),
    ("Pré-impresso BMA FIDC Faixa 5335", "BMA FIDC:5335"),

    # Colon after company name
    ("Boleto Pré-Impresso BMA Inter: faixa 6003.", "BMA INTER:6003"),
    ("Boleto pré-impresso BMA Inter: faixa 4162.", "BMA INTER:4162"),

    # Multi-line format
    ("Boleto Pré Impresso:\nBMA FIDC - Faixa N/N - 0052", "BMA FIDC:0052"),
    ("Boleto Pré Impresso:\nBMA FIDC - Faixa N/N - 0077\nBMA INTER - Faixa N/N - 02013", "BMA FIDC:0077, BMA INTER:02013"),

    # NEW: BMA SEC => normalized to BMA
    ("Pré Impresso BMA SEC. - Faixa N/N - 5125.", "BMA:5125"),
    ("10/02/25 - Cobrança simples - Pré Impresso BMA SEC. - Faixa N/N - 5125.", "BMA:5125"),

    # NEW: Standalone "Fidc" => normalized to BMA FIDC
    ("Pré-impresso Fidc Faixa 5325", "BMA FIDC:5325"),

    # NEW: Matriz and Filial patterns
    # Note: Matriz/Filial inherit the first company found (BMA FIDC in first case)
    ("Pré-impresso BMA FIDC - Matriz faixa 3 à esquerda - Filial 5061", "BMA FIDC:3, BMA FIDC:5061"),
    ("Pré-impresso - Matriz faixa 100 - Filial 200", "BMA:100, BMA:200"),

    # NEW: Parametrização cases - MUST BE IGNORED (only if near pré-impresso)
    ("Pré-impresso BMA INTER em PARAMETRIZAÇÃO 26/04.", "(none)"),
    ("Pré-impresso INTER em parametrização. Faixa N/N 0165", "(none)"),
    ("Boleto pré-impresso INTER em PARAMETRIZAÇÃO - faixa N/N 0135.", "(none)"),

    # NEW: Parametrização NOT near pré-impresso - should be parsed
    ("Boleto pré-impresso BMA FIDC - faixa 5267.\n\nCadastro feito na Matriz, mas parametrização e conta são da filial.", "BMA FIDC:5267"),

    # Cases that should NOT match (missing company name)
    ("Boleto pre impresso - Site", "(none)"),
    ("Boleto pré impresso", "(none)"),
    ("Boleto Pré Impresso - Faixa N/N - 02014.", "(none)"),  # Missing company name
]

print("=" * 100)
print("TESTING parse_pre_impresso_ranges() FUNCTION")
print("=" * 100)
print()

passed = 0
failed = 0

for obs1_text, expected_str in test_cases:
    result = parse_pre_impresso_ranges(obs1_text)
    
    if result:
        result_str = ", ".join([f"{company}:{range_val}" for company, range_val in result])
    else:
        result_str = "(none)"
    
    if result_str == expected_str:
        status = "✓ PASS"
        passed += 1
    else:
        status = "✗ FAIL"
        failed += 1
    
    print(f"{status}")
    print(f"  Input:    {obs1_text[:80]}")
    print(f"  Expected: {expected_str}")
    print(f"  Got:      {result_str}")
    print()

print("=" * 100)
print(f"Results: {passed} passed, {failed} failed out of {len(test_cases)} tests")
print("=" * 100)

