#!/usr/bin/env python3
"""
Test script to verify duplicate notification prevention works correctly.
"""

import sys
from pathlib import Path
from datetime import date

# Add parent directory to path to import the module
sys.path.insert(0, str(Path(__file__).parent))

from query_apr_invalidos_status import (
    get_records_hash,
    get_tracking_file,
    is_already_sent,
    mark_as_sent
)


def test_duplicate_prevention():
    """Test the duplicate notification prevention mechanism."""
    
    print("=" * 80)
    print("TESTING DUPLICATE NOTIFICATION PREVENTION")
    print("=" * 80)
    
    # Create sample invalid records
    sample_records_duplicata = [
        {
            'DATA': date(2026, 1, 23),
            'PROPOSTA': 12345,
            'CEDENTE': 'TEST CEDENTE 1',
            'DUPLICATA': '021467-1',
            'NFE': 21467,
            'MOTIVO_INVALIDO': 'Test error'
        },
        {
            'DATA': date(2026, 1, 23),
            'PROPOSTA': 12346,
            'CEDENTE': 'TEST CEDENTE 2',
            'DUPLICATA': '021468-1',
            'NFE': 21468,
            'MOTIVO_INVALIDO': 'Test error 2'
        }
    ]
    
    sample_records_seuno = [
        {
            'DATA': date(2026, 1, 23),
            'PROPOSTA': 12347,
            'CEDENTE': 'TEST CEDENTE 3',
            'SEUNO': '518300121527',
            'SEUNO_RANGE': '5183',
            'MOTIVO_INVALIDO': 'Test SEUNO error'
        }
    ]
    
    # Test 1: Check hash generation
    print("\n1. Testing hash generation...")
    hash1 = get_records_hash(sample_records_duplicata, "DUPLICATA")
    hash2 = get_records_hash(sample_records_duplicata, "DUPLICATA")
    print(f"   Hash 1: {hash1[:16]}...")
    print(f"   Hash 2: {hash2[:16]}...")
    assert hash1 == hash2, "Hashes should be identical for same records"
    print("   ✓ Hash generation is deterministic")
    
    # Test 2: Check tracking file path
    print("\n2. Testing tracking file path...")
    tracking_file_dup = get_tracking_file("DUPLICATA")
    tracking_file_seuno = get_tracking_file("SEUNO")
    print(f"   DUPLICATA tracking file: {tracking_file_dup}")
    print(f"   SEUNO tracking file: {tracking_file_seuno}")
    assert tracking_file_dup != tracking_file_seuno, "Different alert types should have different tracking files"
    print("   ✓ Tracking files are separate for different alert types")
    
    # Test 3: Check is_already_sent (should be False initially)
    print("\n3. Testing is_already_sent (before marking)...")
    already_sent = is_already_sent(sample_records_duplicata, "DUPLICATA")
    print(f"   Already sent: {already_sent}")
    assert not already_sent, "Should not be marked as sent initially"
    print("   ✓ Records not marked as sent initially")
    
    # Test 4: Mark as sent
    print("\n4. Testing mark_as_sent...")
    mark_as_sent(sample_records_duplicata, "DUPLICATA")
    print("   ✓ Records marked as sent")
    
    # Test 5: Check is_already_sent (should be True now)
    print("\n5. Testing is_already_sent (after marking)...")
    already_sent = is_already_sent(sample_records_duplicata, "DUPLICATA")
    print(f"   Already sent: {already_sent}")
    assert already_sent, "Should be marked as sent after marking"
    print("   ✓ Records correctly marked as sent")
    
    # Test 6: Different records should not be marked as sent
    print("\n6. Testing with different records...")
    different_records = [
        {
            'DATA': date(2026, 1, 23),
            'PROPOSTA': 99999,
            'CEDENTE': 'DIFFERENT CEDENTE',
            'DUPLICATA': '999999-1',
            'NFE': 999999,
            'MOTIVO_INVALIDO': 'Different error'
        }
    ]
    already_sent_different = is_already_sent(different_records, "DUPLICATA")
    print(f"   Already sent (different records): {already_sent_different}")
    assert not already_sent_different, "Different records should not be marked as sent"
    print("   ✓ Different records correctly identified as not sent")
    
    # Test 7: SEUNO tracking is separate
    print("\n7. Testing SEUNO tracking is separate...")
    already_sent_seuno = is_already_sent(sample_records_seuno, "SEUNO")
    print(f"   SEUNO already sent: {already_sent_seuno}")
    assert not already_sent_seuno, "SEUNO should have separate tracking"
    print("   ✓ SEUNO tracking is separate from DUPLICATA")
    
    # Cleanup test tracking files
    print("\n8. Cleaning up test tracking files...")
    if tracking_file_dup.exists():
        tracking_file_dup.unlink()
        print(f"   Removed: {tracking_file_dup.name}")
    if tracking_file_seuno.exists():
        tracking_file_seuno.unlink()
        print(f"   Removed: {tracking_file_seuno.name}")
    
    print("\n" + "=" * 80)
    print("✓ ALL TESTS PASSED!")
    print("=" * 80)


if __name__ == "__main__":
    test_duplicate_prevention()

