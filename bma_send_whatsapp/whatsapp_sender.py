#!/usr/bin/env python3
"""
Enhanced WhatsApp API client using requests library
Provides a clean interface for sending WhatsApp template messages via Infobip API
"""

import requests
import json
import uuid
from typing import Dict, List, Optional, Any
from dataclasses import dataclass


@dataclass
class WhatsAppConfig:
    """Configuration for WhatsApp API"""
    base_url: str = "https://wgne28.api.infobip.com"
    api_key: str = "App 7ab8933757a70c2a0d9e9e4e44785e69-d745c038-d468-451a-800c-9b96df63d4bf"
    from_number: str = "447860088970"


@dataclass
class TemplateMessage:
    """WhatsApp template message configuration"""
    to_number: str
    template_name: str
    language: str = "en"
    placeholders: Optional[List[str]] = None
    message_id: Optional[str] = None


class WhatsAppSender:
    """WhatsApp API client for sending template messages"""
    
    def __init__(self, config: WhatsAppConfig):
        self.config = config
        self.session = requests.Session()
        self.session.headers.update({
            'Authorization': config.api_key,
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        })
    
    def send_template_message(self, message: TemplateMessage) -> Dict[str, Any]:
        """
        Send a WhatsApp template message
        
        Args:
            message: TemplateMessage object with message details
            
        Returns:
            Dict containing the API response
            
        Raises:
            requests.RequestException: If the API request fails
        """
        
        # Generate message ID if not provided
        if not message.message_id:
            message.message_id = str(uuid.uuid4())
        
        # Build the payload
        payload = {
            "messages": [
                {
                    "from": self.config.from_number,
                    "to": message.to_number,
                    "messageId": message.message_id,
                    "content": {
                        "templateName": message.template_name,
                        "templateData": {
                            "body": {
                                "placeholders": message.placeholders or []
                            }
                        },
                        "language": message.language
                    }
                }
            ]
        }
        
        # Make the API request
        url = f"{self.config.base_url}/whatsapp/1/message/template"
        
        try:
            response = self.session.post(url, json=payload)
            response.raise_for_status()
            
            result = response.json()
            
            # Log success
            print(f"✅ Message sent successfully!")
            print(f"📱 To: {message.to_number}")
            print(f"📧 Message ID: {message.message_id}")
            
            # Extract status information
            if 'messages' in result and result['messages']:
                for msg in result['messages']:
                    if 'status' in msg:
                        status = msg['status']
                        print(f"📊 Status: {status.get('name', 'Unknown')} - {status.get('description', 'No description')}")
            
            return result
            
        except requests.RequestException as e:
            print(f"❌ Error sending message: {e}")
            if hasattr(e, 'response') and e.response is not None:
                try:
                    error_details = e.response.json()
                    print(f"📋 Error details: {json.dumps(error_details, indent=2)}")
                except:
                    print(f"📋 Response text: {e.response.text}")
            raise
    
    def send_bulk_messages(self, messages: List[TemplateMessage]) -> Dict[str, Any]:
        """
        Send multiple WhatsApp template messages in a single request
        
        Args:
            messages: List of TemplateMessage objects
            
        Returns:
            Dict containing the API response
        """
        
        message_list = []
        for msg in messages:
            if not msg.message_id:
                msg.message_id = str(uuid.uuid4())
            
            message_list.append({
                "from": self.config.from_number,
                "to": msg.to_number,
                "messageId": msg.message_id,
                "content": {
                    "templateName": msg.template_name,
                    "templateData": {
                        "body": {
                            "placeholders": msg.placeholders or []
                        }
                    },
                    "language": msg.language
                }
            })
        
        payload = {"messages": message_list}
        url = f"{self.config.base_url}/whatsapp/1/message/template"
        
        try:
            response = self.session.post(url, json=payload)
            response.raise_for_status()
            
            result = response.json()
            print(f"✅ Bulk messages sent successfully! ({len(messages)} messages)")
            
            return result
            
        except requests.RequestException as e:
            print(f"❌ Error sending bulk messages: {e}")
            raise


def main():
    """Example usage of the WhatsApp sender"""
    
    # Initialize configuration
    config = WhatsAppConfig()
    
    # Create sender instance
    sender = WhatsAppSender(config)
    
    # Create a template message
    message = TemplateMessage(
        to_number="5511997435829",
        template_name="test_whatsapp_template_en",
        language="en",
        placeholders=["Moisés Román"]
    )
    
    try:
        # Send the message
        print("🚀 Sending WhatsApp template message...")
        result = sender.send_template_message(message)
        
        print("✨ Operation completed successfully!")
        print(f"📋 Full response: {json.dumps(result, indent=2)}")
        
    except Exception as e:
        print(f"💥 Operation failed: {e}")


if __name__ == "__main__":
    main()
