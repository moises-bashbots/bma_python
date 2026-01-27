#!/usr/bin/env python3
"""
Test the webfact parsing functionality
"""

import sys
from pathlib import Path

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent))

from query_apr_invalidos_status import parse_pre_impresso_ranges

# Test cases
test_cases = [
    ("Boleto Pré Impresso - Via Webfact", [("BMA", "200")]),
    ("Boleto pré-impresso - BMA FIDC faixa n/n 5233", [("BMA FIDC", "5233")]),
    ("Pré-impresso BMA INTER - Faixa 5101", [("BMA INTER", "5101")]),
    ("Boleto Pré impresso BMA FIDC - Faixa N/N - 02014", [("BMA FIDC", "02014")]),
    ("Boleto Pré Impresso - Via Webfact - BMA FIDC", [("BMA", "200")]),
    ("Boleto pre impresso - Site", []),  # Should not match - no "webfact"
    ("Pré-impresso Bma Fidc - Faixa 5065", [("BMA FIDC", "5065")]),
]

print("=" * 100)
print("TESTING WEBFACT PARSING FUNCTIONALITY")
print("=" * 100)
print()

passed = 0
failed = 0

for test_input, expected_output in test_cases:
    result = parse_pre_impresso_ranges(test_input)
    
    if result == expected_output:
        status = "✓ PASS"
        passed += 1
    else:
        status = "✗ FAIL"
        failed += 1
    
    print(f"{status}")
    print(f"  Input:    {test_input}")
    print(f"  Expected: {expected_output}")
    print(f"  Got:      {result}")
    print()

print("=" * 100)
print(f"Results: {passed} passed, {failed} failed out of {len(test_cases)} tests")
print("=" * 100)

