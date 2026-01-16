#!/usr/bin/env python3
"""
Examples of using the WhatsApp API client
"""

from whatsapp_sender import WhatsAppSender, TemplateMessage
from config import DEFAULT_CONFIG
import json


def example_single_message():
    """Example: Send a single template message"""
    print("=" * 50)
    print("📱 Example 1: Single Template Message")
    print("=" * 50)
    
    # Create sender
    sender = WhatsAppSender(DEFAULT_CONFIG)
    
    # Create message
    message = TemplateMessage(
        to_number="5511997435829",
        template_name="test_whatsapp_template_en",
        language="en",
        placeholders=["Moisés Román"]
    )
    
    try:
        result = sender.send_template_message(message)
        print(f"✅ Success! Message ID: {result['messages'][0]['messageId']}")
    except Exception as e:
        print(f"❌ Failed: {e}")


def example_bulk_messages():
    """Example: Send multiple template messages"""
    print("\n" + "=" * 50)
    print("📱 Example 2: Bulk Template Messages")
    print("=" * 50)
    
    # Create sender
    sender = WhatsAppSender(DEFAULT_CONFIG)
    
    # Create multiple messages
    messages = [
        TemplateMessage(
            to_number="5511997435829",
            template_name="test_whatsapp_template_en",
            language="en",
            placeholders=["Moisés Román"]
        ),
        TemplateMessage(
            to_number="5511997435829",  # Same number for demo
            template_name="test_whatsapp_template_en",
            language="en",
            placeholders=["Test User 2"]
        )
    ]
    
    try:
        result = sender.send_bulk_messages(messages)
        print(f"✅ Success! Sent {len(result['messages'])} messages")
        for i, msg in enumerate(result['messages']):
            print(f"  📧 Message {i+1} ID: {msg['messageId']}")
    except Exception as e:
        print(f"❌ Failed: {e}")


def example_custom_template():
    """Example: Send message with different template"""
    print("\n" + "=" * 50)
    print("📱 Example 3: Custom Template Message")
    print("=" * 50)
    
    # Create sender
    sender = WhatsAppSender(DEFAULT_CONFIG)
    
    # Create message with different template (this might fail if template doesn't exist)
    message = TemplateMessage(
        to_number="5511997435829",
        template_name="welcome_template",  # Different template
        language="pt",  # Portuguese
        placeholders=["João Silva", "Bem-vindo"]
    )
    
    try:
        result = sender.send_template_message(message)
        print(f"✅ Success! Message ID: {result['messages'][0]['messageId']}")
    except Exception as e:
        print(f"❌ Expected failure (template might not exist): {e}")


def main():
    """Run all examples"""
    print("🚀 WhatsApp API Examples")
    print("This script demonstrates different ways to use the WhatsApp sender")
    
    # Run examples
    example_single_message()
    
    # Uncomment to run bulk example (sends multiple messages)
    # example_bulk_messages()
    
    # Uncomment to test with different template (will likely fail)
    # example_custom_template()
    
    print("\n" + "=" * 50)
    print("✨ Examples completed!")
    print("=" * 50)


if __name__ == "__main__":
    main()
