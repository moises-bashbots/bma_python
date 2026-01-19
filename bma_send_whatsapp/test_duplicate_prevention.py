#!/usr/bin/env python3
"""
Test script to demonstrate duplicate message prevention.
"""

import sys
from pathlib import Path

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from bma_send_whatsapp.query_apr_capa_status import (
    generate_message_id,
    load_sent_messages,
    save_sent_message,
    get_tracking_file_path
)

def test_duplicate_prevention():
    """Test the duplicate prevention system."""
    
    print("=" * 80)
    print("DUPLICATE MESSAGE PREVENTION TEST")
    print("=" * 80)
    
    # Test data
    cedente = "CENTRAL CARNES"
    status = "Enviado para Assinar"
    date = "20251013"
    numero = 31
    
    # Generate message ID
    message_id = generate_message_id(cedente, status, date, numero)
    print(f"\n1. Generated message ID: {message_id}")
    print(f"   For: {cedente} | {status} | {date} | #{numero}")
    
    # Check tracking file
    tracking_file = get_tracking_file_path()
    print(f"\n2. Tracking file: {tracking_file}")
    print(f"   Exists: {tracking_file.exists()}")
    
    # Load sent messages
    sent_messages = load_sent_messages()
    print(f"\n3. Currently tracked messages: {len(sent_messages)}")
    
    # Check if this message was sent
    if message_id in sent_messages:
        print(f"   ✓ Message ID {message_id} is already tracked (duplicate)")
    else:
        print(f"   ✗ Message ID {message_id} is NOT tracked (new message)")
    
    # Simulate sending a message
    print(f"\n4. Simulating message send...")
    save_sent_message(message_id)
    print(f"   ✓ Message ID saved to tracking file")
    
    # Verify it was saved
    sent_messages = load_sent_messages()
    if message_id in sent_messages:
        print(f"   ✓ Verification: Message ID is now tracked")
    else:
        print(f"   ✗ Verification: Failed to save message ID")
    
    # Test duplicate detection
    print(f"\n5. Testing duplicate detection...")
    if message_id in sent_messages:
        print(f"   ✓ Duplicate detected! This message would be skipped.")
    else:
        print(f"   ✗ Duplicate NOT detected (unexpected)")
    
    # Show all tracked messages
    print(f"\n6. All tracked messages today:")
    for idx, msg_id in enumerate(sent_messages, 1):
        print(f"   {idx}. {msg_id}")
    
    print("\n" + "=" * 80)
    print("TEST COMPLETE")
    print("=" * 80)


if __name__ == "__main__":
    test_duplicate_prevention()

