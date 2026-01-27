#!/usr/bin/env python3
"""
Full integration test for duplicity control.
Simulates sending alerts twice to verify duplicates are prevented.
"""

import sys
from pathlib import Path

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent))

from repurchase_report import send_repurchase_alerts, SENT_ALERTS_FILE


def cleanup_test_file():
    """Remove test file."""
    test_file = Path(__file__).parent / SENT_ALERTS_FILE
    if test_file.exists():
        test_file.unlink()
        print(f"✓ Cleaned up test file: {SENT_ALERTS_FILE}\n")


def test_full_duplicity_prevention():
    """Test full alert sending with duplicity prevention."""
    
    print("=" * 120)
    print("FULL DUPLICITY PREVENTION TEST")
    print("=" * 120)
    print()
    
    # Clean up any existing test file
    cleanup_test_file()
    
    # Create sample matched records (simulating cross-referenced data)
    sample_records = [
        {
            'apr_cedente': 'METALURGICA REUTER',
            'apr_numero': 2,
            'apr_gerente': 'SUIENE',
            'excel_operador': 'Leticia',
            'excel_tipo_recompra': 'Operação',
        },
        {
            'apr_cedente': 'BRAUNAS',
            'apr_numero': 3,
            'apr_gerente': 'CARTEIRA D',
            'excel_operador': 'Bárbara',
            'excel_tipo_recompra': 'Operação',
        },
        {
            'apr_cedente': 'FRIMAZZ CARNES',
            'apr_numero': 5,
            'apr_gerente': 'GIZELLE BR',
            'excel_operador': 'Franciane',
            'excel_tipo_recompra': 'Operação',
        },
    ]
    
    print("FIRST RUN - All alerts should be sent")
    print("=" * 120)
    print()
    
    # First run - all alerts should be sent
    stats1 = send_repurchase_alerts(sample_records, dry_run=True)
    
    print()
    print("FIRST RUN RESULTS:")
    print(f"  Total records: {stats1['total_records']}")
    print(f"  Alerts sent: {stats1['alerts_sent']}")
    print(f"  Alerts skipped (duplicate): {stats1['alerts_skipped_duplicate']}")
    print()
    
    # Verify first run
    if stats1['alerts_sent'] == 3 and stats1['alerts_skipped_duplicate'] == 0:
        print("✓ FIRST RUN: All 3 alerts sent, 0 duplicates - CORRECT")
    else:
        print("✗ FIRST RUN: Unexpected results - FAILED")
    
    print()
    print("=" * 120)
    print()
    
    print("SECOND RUN - All alerts should be skipped as duplicates")
    print("=" * 120)
    print()
    
    # Second run - all alerts should be skipped
    stats2 = send_repurchase_alerts(sample_records, dry_run=True)
    
    print()
    print("SECOND RUN RESULTS:")
    print(f"  Total records: {stats2['total_records']}")
    print(f"  Alerts sent: {stats2['alerts_sent']}")
    print(f"  Alerts skipped (duplicate): {stats2['alerts_skipped_duplicate']}")
    print()
    
    # Verify second run
    if stats2['alerts_sent'] == 0 and stats2['alerts_skipped_duplicate'] == 3:
        print("✓ SECOND RUN: 0 alerts sent, 3 duplicates skipped - CORRECT")
    else:
        print("✗ SECOND RUN: Unexpected results - FAILED")
    
    print()
    print("=" * 120)
    print()
    
    # Add a new record
    new_record = {
        'apr_cedente': 'NONAPACK INDUSTRIA',
        'apr_numero': 8,
        'apr_gerente': 'AMANDA',
        'excel_operador': 'Leticia',
        'excel_tipo_recompra': 'Operação',
    }
    
    sample_records.append(new_record)
    
    print("THIRD RUN - 1 new alert, 3 duplicates")
    print("=" * 120)
    print()
    
    # Third run - 1 new, 3 duplicates
    stats3 = send_repurchase_alerts(sample_records, dry_run=True)
    
    print()
    print("THIRD RUN RESULTS:")
    print(f"  Total records: {stats3['total_records']}")
    print(f"  Alerts sent: {stats3['alerts_sent']}")
    print(f"  Alerts skipped (duplicate): {stats3['alerts_skipped_duplicate']}")
    print()
    
    # Verify third run
    if stats3['alerts_sent'] == 1 and stats3['alerts_skipped_duplicate'] == 3:
        print("✓ THIRD RUN: 1 new alert sent, 3 duplicates skipped - CORRECT")
    else:
        print("✗ THIRD RUN: Unexpected results - FAILED")
    
    print()
    print("=" * 120)
    print("TEST SUMMARY")
    print("=" * 120)
    print()
    
    all_passed = (
        stats1['alerts_sent'] == 3 and stats1['alerts_skipped_duplicate'] == 0 and
        stats2['alerts_sent'] == 0 and stats2['alerts_skipped_duplicate'] == 3 and
        stats3['alerts_sent'] == 1 and stats3['alerts_skipped_duplicate'] == 3
    )
    
    if all_passed:
        print("✓✓✓ ALL TESTS PASSED ✓✓✓")
        print()
        print("Duplicity control is working correctly!")
        print("- First run: All alerts sent")
        print("- Second run: All duplicates prevented")
        print("- Third run: Only new alert sent, duplicates prevented")
    else:
        print("✗✗✗ SOME TESTS FAILED ✗✗✗")
    
    print()
    
    # Cleanup
    cleanup_test_file()


if __name__ == "__main__":
    test_full_duplicity_prevention()

