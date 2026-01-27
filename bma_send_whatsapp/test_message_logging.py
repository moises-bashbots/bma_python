#!/usr/bin/env python3
"""
Test script for WhatsApp message logging functionality
"""

import sys
from pathlib import Path
from datetime import date
from decimal import Decimal

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from bma_send_whatsapp.message_logger import log_whatsapp_message, create_mariadb_session
from database.bma_models import MensagemWhatsapp


def test_database_connection():
    """Test database connection"""
    print("=" * 60)
    print("Test 1: Database Connection")
    print("=" * 60)
    
    try:
        session = create_mariadb_session()
        print("✅ Successfully connected to MariaDB")
        session.close()
        return True
    except Exception as e:
        print(f"❌ Failed to connect to MariaDB: {e}")
        return False


def test_log_simple_message():
    """Test logging a simple message"""
    print("\n" + "=" * 60)
    print("Test 2: Log Simple Message")
    print("=" * 60)
    
    try:
        message_id = log_whatsapp_message(
            telefone_destino="5511997435829",
            mensagem="Test message from logging system",
            tipo_mensagem="notification",
            status_envio="success",
            status_code=200
        )
        
        if message_id:
            print(f"✅ Message logged successfully with ID: {message_id}")
            return True
        else:
            print("❌ Failed to log message (no ID returned)")
            return False
            
    except Exception as e:
        print(f"❌ Error logging message: {e}")
        return False


def test_log_complete_message():
    """Test logging a message with all metadata"""
    print("\n" + "=" * 60)
    print("Test 3: Log Complete Message with Metadata")
    print("=" * 60)
    
    try:
        message_id = log_whatsapp_message(
            telefone_destino="5511997435829",
            mensagem="Seu borderô com 10 recebíveis foi aprovado!",
            tipo_mensagem="status_update",
            status_envio="success",
            message_id="test-msg-001",
            message_hash="abc123def456",
            nome_contato="Test Contact",
            is_group=True,
            cedente="ACME Corporation",
            grupo="Grupo A",
            data_proposta=date(2026, 1, 21),
            numero_proposta=12345,
            bordero="BOR-2026-001",
            status_fluxo="Enviado para Assinar",
            qtd_recebiveis=10,
            valor_total=Decimal("50000.00"),
            status_code=200,
            api_response={"status": "ok", "messageId": "xyz789"},
            config_file="whatsapp_config.json",
            api_provider="Z-API",
            usuario="system",
            origem="automated",
            tentativas_envio=1
        )
        
        if message_id:
            print(f"✅ Complete message logged successfully with ID: {message_id}")
            return True
        else:
            print("❌ Failed to log complete message")
            return False
            
    except Exception as e:
        print(f"❌ Error logging complete message: {e}")
        import traceback
        traceback.print_exc()
        return False


def test_query_messages():
    """Test querying logged messages"""
    print("\n" + "=" * 60)
    print("Test 4: Query Logged Messages")
    print("=" * 60)
    
    try:
        session = create_mariadb_session()
        
        # Query last 5 messages
        messages = session.query(MensagemWhatsapp).order_by(
            MensagemWhatsapp.data_envio.desc()
        ).limit(5).all()
        
        print(f"✅ Found {len(messages)} recent messages:")
        print()
        
        for msg in messages:
            print(f"  ID: {msg.id_mensagem}")
            print(f"  To: {msg.telefone_destino}")
            print(f"  Type: {msg.tipo_mensagem}")
            print(f"  Status: {msg.status_envio}")
            print(f"  Cedente: {msg.cedente or 'N/A'}")
            print(f"  Date: {msg.data_envio}")
            print(f"  Message: {msg.mensagem[:50]}...")
            print("-" * 60)
        
        session.close()
        return True
        
    except Exception as e:
        print(f"❌ Error querying messages: {e}")
        import traceback
        traceback.print_exc()
        return False


def main():
    """Run all tests"""
    print("\n" + "=" * 60)
    print("WhatsApp Message Logging - Test Suite")
    print("=" * 60)
    
    results = []
    
    # Run tests
    results.append(("Database Connection", test_database_connection()))
    results.append(("Log Simple Message", test_log_simple_message()))
    results.append(("Log Complete Message", test_log_complete_message()))
    results.append(("Query Messages", test_query_messages()))
    
    # Summary
    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)
    
    for test_name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{status} - {test_name}")
    
    total = len(results)
    passed = sum(1 for _, result in results if result)
    
    print()
    print(f"Total: {passed}/{total} tests passed")
    print("=" * 60)
    
    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())

