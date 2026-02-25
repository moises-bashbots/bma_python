#!/usr/bin/env python3
"""
Test script to verify ALIANCA cedente rating logic.
"""

# Mock record class for testing
class MockRecord:
    def __init__(self, cedente, ramo, qtd_titulos):
        self.CEDENTE = cedente
        self.RAMO = ramo
        self.QTD_TITULOS = qtd_titulos

def determine_rating(record):
    """
    Replicate the rating determination logic from send_rating_vadu.py
    """
    if not record.RAMO:
        return None
    
    # Special case: ALIANCA cedente always uses SINTÉTICO
    if record.CEDENTE and record.CEDENTE.upper() == "ALIANCA":
        return "SINTÉTICO", "ALIANCA cedente detected - using SINTÉTICO rating (special rule)"
    
    # Check if QTD_TITULOS >= 700, use "SINTÉTICO" instead
    elif record.QTD_TITULOS and record.QTD_TITULOS >= 700:
        return "SINTÉTICO", "QTD_TITULOS >= 700, using SINTÉTICO rating"
    
    else:
        # Remove the last character only if it's a space, plus, or minus
        if len(record.RAMO) > 0 and record.RAMO[-1] in [' ', '+', '-']:
            rating_value = record.RAMO[:-1]
            reason = f"Removed last character '{record.RAMO[-1]}' from RAMO"
        else:
            rating_value = record.RAMO
            reason = "Using RAMO as-is"
        
        return rating_value, reason

# Test cases
test_cases = [
    # (cedente, ramo, qtd_titulos, expected_rating, description)
    ("ALIANCA", "RATING A", 50, "SINTÉTICO", "ALIANCA with RATING A and low qty"),
    ("ALIANCA", "RATING B-", 100, "SINTÉTICO", "ALIANCA with RATING B- and medium qty"),
    ("ALIANCA", "RATING C+", 800, "SINTÉTICO", "ALIANCA with RATING C+ and high qty (>700)"),
    ("alianca", "RATING A", 50, "SINTÉTICO", "ALIANCA (lowercase) with RATING A"),
    ("OTHER", "RATING A", 50, "RATING A", "Other cedente with RATING A and low qty"),
    ("OTHER", "RATING B-", 100, "RATING B", "Other cedente with RATING B- (remove -)"),
    ("OTHER", "RATING C+", 100, "RATING C", "Other cedente with RATING C+ (remove +)"),
    ("OTHER", "RATING A ", 100, "RATING A", "Other cedente with RATING A (remove space)"),
    ("OTHER", "RATING A", 800, "SINTÉTICO", "Other cedente with high qty (>=700)"),
    ("ZABUMBA", "RATING B-", 61, "RATING B", "ZABUMBA with RATING B- and normal qty"),
]

print("=" * 100)
print("TESTING ALIANCA RATING LOGIC")
print("=" * 100)
print()

all_passed = True

for cedente, ramo, qtd_titulos, expected_rating, description in test_cases:
    record = MockRecord(cedente, ramo, qtd_titulos)
    rating, reason = determine_rating(record)
    
    passed = rating == expected_rating
    status = "✅ PASS" if passed else "❌ FAIL"
    
    print(f"{status} | {description}")
    print(f"       Cedente: {cedente}, RAMO: {ramo}, QTD: {qtd_titulos}")
    print(f"       Expected: {expected_rating}, Got: {rating}")
    print(f"       Reason: {reason}")
    print()
    
    if not passed:
        all_passed = False

print("=" * 100)
if all_passed:
    print("✅ ALL TESTS PASSED!")
else:
    print("❌ SOME TESTS FAILED!")
print("=" * 100)

