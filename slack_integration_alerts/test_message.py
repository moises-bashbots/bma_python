#!/usr/bin/env python3
"""Test sending a simple message to Slack."""

import json
import requests
from pathlib import Path

def load_slack_config():
    config_path = Path(__file__).parent / "slack_config.json"
    with open(config_path, 'r') as f:
        return json.load(f)

def test_message():
    """Test sending a simple message."""
    config = load_slack_config()
    bot_token = config['slack']['bot_token']
    channel_id = config['slack']['channel']
    
    print(f"Sending message to channel: {channel_id}")
    
    response = requests.post(
        'https://slack.com/api/chat.postMessage',
        headers={
            'Authorization': f'Bearer {bot_token}',
            'Content-Type': 'application/json'
        },
        json={
            'channel': channel_id,
            'text': '*Test Message*\nThis is a test message from the BMA Alert Bot!'
        },
        timeout=10
    )
    
    result = response.json()
    print(json.dumps(result, indent=2))
    
    if result.get('ok'):
        print("\n✅ Message sent successfully!")
    else:
        print(f"\n❌ Failed: {result.get('error')}")
        if 'needed' in result:
            print(f"  Missing scopes: {result.get('needed')}")
        if 'provided' in result:
            print(f"  Provided scopes: {result.get('provided')}")

if __name__ == "__main__":
    test_message()

