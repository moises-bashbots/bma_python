#!/usr/bin/env python3
"""
Simple duplicity control test without Slack API calls.
Tests the core duplicity logic directly.
"""

import sys
from pathlib import Path
from datetime import date

sys.path.insert(0, str(Path(__file__).parent))

from repurchase_report import (
    load_sent_alerts,
    save_sent_alerts,
    create_alert_id,
    is_alert_already_sent,
    mark_alert_as_sent,
    SENT_ALERTS_FILE
)


def cleanup_test_file():
    """Remove test file."""
    test_file = Path(__file__).parent / SENT_ALERTS_FILE
    if test_file.exists():
        test_file.unlink()


def simulate_alert_sending():
    """Simulate the alert sending process with duplicity control."""
    
    print("=" * 120)
    print("DUPLICITY CONTROL SIMULATION")
    print("=" * 120)
    print()
    
    # Clean up
    cleanup_test_file()
    
    # Sample records
    records = [
        {'cedente': 'METALURGICA REUTER', 'numero': 2, 'operador': 'Leticia'},
        {'cedente': 'BRAUNAS', 'numero': 3, 'operador': 'Bárbara'},
        {'cedente': 'FRIMAZZ CARNES', 'numero': 5, 'operador': 'Franciane'},
    ]
    
    today = date.today()
    
    # === FIRST RUN ===
    print("FIRST RUN - Sending alerts")
    print("=" * 120)
    print()
    
    sent_alerts = load_sent_alerts()
    sent_count = 0
    skipped_count = 0
    
    for i, record in enumerate(records, 1):
        cedente = record['cedente']
        numero = record['numero']
        operador = record['operador']
        
        alert_id = create_alert_id(cedente, numero)
        
        print(f"{i}. {cedente} | Borderô: {numero} | Operador: {operador}")
        
        if is_alert_already_sent(alert_id, today, sent_alerts):
            print(f"   ⏭ Alert already sent today - SKIPPED")
            skipped_count += 1
        else:
            print(f"   ✓ Sending alert...")
            print(f"   Message: @{operador} {cedente}, borderô {numero}, OPERAÇÃO")
            sent_alerts = mark_alert_as_sent(alert_id, today, sent_alerts)
            sent_count += 1
        
        print()
    
    save_sent_alerts(sent_alerts)
    
    print(f"FIRST RUN SUMMARY: Sent: {sent_count}, Skipped: {skipped_count}")
    print()
    
    # Verify first run
    if sent_count == 3 and skipped_count == 0:
        print("✓ FIRST RUN PASSED: All 3 alerts sent, 0 skipped")
    else:
        print(f"✗ FIRST RUN FAILED: Expected sent=3, skipped=0, got sent={sent_count}, skipped={skipped_count}")
    
    print()
    print("=" * 120)
    print()
    
    # === SECOND RUN ===
    print("SECOND RUN - Same alerts (should be skipped)")
    print("=" * 120)
    print()
    
    sent_alerts = load_sent_alerts()
    sent_count = 0
    skipped_count = 0
    
    for i, record in enumerate(records, 1):
        cedente = record['cedente']
        numero = record['numero']
        operador = record['operador']
        
        alert_id = create_alert_id(cedente, numero)
        
        print(f"{i}. {cedente} | Borderô: {numero} | Operador: {operador}")
        
        if is_alert_already_sent(alert_id, today, sent_alerts):
            print(f"   ⏭ Alert already sent today - SKIPPED")
            skipped_count += 1
        else:
            print(f"   ✓ Sending alert...")
            print(f"   Message: @{operador} {cedente}, borderô {numero}, OPERAÇÃO")
            sent_alerts = mark_alert_as_sent(alert_id, today, sent_alerts)
            sent_count += 1
        
        print()
    
    save_sent_alerts(sent_alerts)
    
    print(f"SECOND RUN SUMMARY: Sent: {sent_count}, Skipped: {skipped_count}")
    print()
    
    # Verify second run
    if sent_count == 0 and skipped_count == 3:
        print("✓ SECOND RUN PASSED: 0 alerts sent, 3 skipped (duplicates)")
    else:
        print(f"✗ SECOND RUN FAILED: Expected sent=0, skipped=3, got sent={sent_count}, skipped={skipped_count}")
    
    print()
    print("=" * 120)
    print()
    
    # === THIRD RUN - Add new record ===
    records.append({'cedente': 'NONAPACK INDUSTRIA', 'numero': 8, 'operador': 'Leticia'})
    
    print("THIRD RUN - 3 duplicates + 1 new alert")
    print("=" * 120)
    print()
    
    sent_alerts = load_sent_alerts()
    sent_count = 0
    skipped_count = 0
    
    for i, record in enumerate(records, 1):
        cedente = record['cedente']
        numero = record['numero']
        operador = record['operador']
        
        alert_id = create_alert_id(cedente, numero)
        
        print(f"{i}. {cedente} | Borderô: {numero} | Operador: {operador}")
        
        if is_alert_already_sent(alert_id, today, sent_alerts):
            print(f"   ⏭ Alert already sent today - SKIPPED")
            skipped_count += 1
        else:
            print(f"   ✓ Sending alert...")
            print(f"   Message: @{operador} {cedente}, borderô {numero}, OPERAÇÃO")
            sent_alerts = mark_alert_as_sent(alert_id, today, sent_alerts)
            sent_count += 1
        
        print()
    
    save_sent_alerts(sent_alerts)
    
    print(f"THIRD RUN SUMMARY: Sent: {sent_count}, Skipped: {skipped_count}")
    print()
    
    # Verify third run
    if sent_count == 1 and skipped_count == 3:
        print("✓ THIRD RUN PASSED: 1 new alert sent, 3 skipped (duplicates)")
    else:
        print(f"✗ THIRD RUN FAILED: Expected sent=1, skipped=3, got sent={sent_count}, skipped={skipped_count}")
    
    print()
    print("=" * 120)
    print("FINAL SUMMARY")
    print("=" * 120)
    print()
    
    # Check sent_alerts file
    final_alerts = load_sent_alerts()
    date_str = today.strftime('%Y-%m-%d')
    alerts_today = final_alerts.get(date_str, [])
    
    print(f"Alerts tracked for {date_str}: {len(alerts_today)}")
    for alert_id in alerts_today:
        print(f"  - {alert_id}")
    print()
    
    if len(alerts_today) == 4:
        print("✓✓✓ ALL TESTS PASSED ✓✓✓")
        print()
        print("Duplicity control is working perfectly!")
        print("- First run: 3 alerts sent")
        print("- Second run: 3 duplicates prevented")
        print("- Third run: 1 new alert sent, 3 duplicates prevented")
        print("- Total unique alerts tracked: 4")
    else:
        print("✗ FAILED: Expected 4 unique alerts tracked")
    
    print()
    
    # Cleanup
    cleanup_test_file()


if __name__ == "__main__":
    simulate_alert_sending()

