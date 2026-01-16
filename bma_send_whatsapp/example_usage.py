#!/usr/bin/env python3
"""
Example of how to use the send_whatsapp module from another program
"""

from send_whatsapp import send_message
import json


def send_welcome_message(phone_number, customer_name):
    """
    Send a personalized welcome message
    
    Args:
        phone_number (str): Customer's phone number
        customer_name (str): Customer's name
    """
    message = f"Hello {customer_name}! Welcome to our service. We're excited to have you on board! 🎉"
    
    try:
        result = send_message(phone_number, message)
        print(f"✅ Welcome message sent to {customer_name} ({phone_number})")
        print(f"📧 Message ID: {result.get('messageId', 'N/A')}")
        return True
    except Exception as e:
        print(f"❌ Failed to send welcome message to {customer_name}: {e}")
        return False


def send_notification(phone_number, notification_text):
    """
    Send a notification message
    
    Args:
        phone_number (str): Recipient's phone number
        notification_text (str): Notification content
    """
    try:
        result = send_message(phone_number, notification_text)
        print(f"✅ Notification sent successfully")
        print(f"📧 Message ID: {result.get('messageId', 'N/A')}")
        return True
    except Exception as e:
        print(f"❌ Failed to send notification: {e}")
        return False


def send_bulk_messages(recipients):
    """
    Send messages to multiple recipients
    
    Args:
        recipients (list): List of dictionaries with 'phone' and 'message' keys
    """
    results = []
    
    for recipient in recipients:
        phone = recipient.get('phone')
        message = recipient.get('message')
        
        if not phone or not message:
            print(f"⚠️ Skipping invalid recipient: {recipient}")
            continue
        
        try:
            result = send_message(phone, message)
            results.append({
                'phone': phone,
                'success': True,
                'message_id': result.get('messageId'),
                'response': result
            })
            print(f"✅ Message sent to {phone}")
        except Exception as e:
            results.append({
                'phone': phone,
                'success': False,
                'error': str(e)
            })
            print(f"❌ Failed to send message to {phone}: {e}")
    
    return results


def main():
    """
    Example usage of the WhatsApp messaging functions
    """
    print("🚀 WhatsApp Messaging Examples")
    print("=" * 50)
    
    # Example 1: Send welcome message
    print("\n📱 Example 1: Welcome Message")
    send_welcome_message("5511997435829", "Moisés Román")
    
    # Example 2: Send notification
    print("\n📱 Example 2: Notification Message")
    notification = "Your order #12345 has been shipped! 📦 Track it here: https://example.com/track"
    send_notification("5511997435829", notification)
    
    # Example 3: Send bulk messages (commented out to avoid spam)
    print("\n📱 Example 3: Bulk Messages (Demo - not executed)")
    recipients = [
        {
            "phone": "5511997435829",
            "message": "Hello! This is a test message 1."
        },
        {
            "phone": "5511997435829",
            "message": "Hello! This is a test message 2."
        }
    ]
    
    print(f"Would send {len(recipients)} messages:")
    for i, recipient in enumerate(recipients, 1):
        print(f"  {i}. To {recipient['phone']}: {recipient['message'][:50]}...")
    
    # Uncomment the line below to actually send bulk messages
    # bulk_results = send_bulk_messages(recipients)
    # print(f"Bulk send results: {json.dumps(bulk_results, indent=2)}")
    
    print("\n✨ Examples completed!")


if __name__ == "__main__":
    main()
