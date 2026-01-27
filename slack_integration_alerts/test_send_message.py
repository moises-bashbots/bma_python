#!/usr/bin/env python3
"""
Test sending a message to the Slack channel.
"""

import sys
import requests
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

from repurchase_report import (
    load_slack_config,
    get_channel_id,
    SLACK_CHANNEL_NAME
)


def test_send_message():
    """Test sending a message to the channel."""
    print("=" * 120)
    print("MESSAGE SENDING TEST")
    print("=" * 120)
    print()
    
    # Load config
    config = load_slack_config()
    bot_token = config['bot_token']
    
    # Get channel
    print(f"1. Finding channel: #{SLACK_CHANNEL_NAME}")
    channel_id = get_channel_id(bot_token, SLACK_CHANNEL_NAME)
    
    if not channel_id:
        print(f"   ✗ Channel not found")
        return
    
    print(f"   ✓ Channel ID: {channel_id}")
    print()
    
    # Ask for confirmation
    print("⚠ This will send a TEST message to the #operacionalxcobranca channel!")
    print()
    response = input("Do you want to send a test message? (yes/no): ").strip().lower()
    
    if response != 'yes':
        print()
        print("Test cancelled by user.")
        return
    
    print()
    print("2. Sending test message...")
    print()
    
    # Send a test message
    test_message = "🧪 **Test Alert from Repurchase System**\n\nThis is a test message. Please ignore."
    
    try:
        response = requests.post(
            'https://slack.com/api/chat.postMessage',
            headers={
                'Authorization': f'Bearer {bot_token}',
                'Content-Type': 'application/json'
            },
            json={
                'channel': channel_id,
                'text': test_message
            },
            timeout=10
        )
        
        result = response.json()
        
        print(f"   Response: {result}")
        print()
        
        if result.get('ok'):
            print("   ✓ Test message sent successfully!")
            print(f"   Message timestamp: {result.get('ts')}")
            print()
            print("=" * 120)
            print("✓✓✓ MESSAGE SENDING WORKS ✓✓✓")
            print("=" * 120)
            print()
            print("The bot can successfully send messages to the channel!")
            return True
        else:
            error = result.get('error')
            print(f"   ✗ Failed to send message: {error}")
            print()
            
            # Provide helpful error messages
            if error == 'not_in_channel':
                print("   ⚠ The bot is not a member of the channel!")
                print(f"   Please add the bot to #{SLACK_CHANNEL_NAME}")
            elif error == 'channel_not_found':
                print("   ⚠ Channel not found or bot doesn't have access")
            elif error == 'missing_scope':
                print("   ⚠ Bot token is missing required permissions")
                print("   Required scopes: chat:write")
            
            return False
            
    except Exception as e:
        print(f"   ✗ Exception: {e}")
        import traceback
        traceback.print_exc()
        return False


if __name__ == "__main__":
    test_send_message()

