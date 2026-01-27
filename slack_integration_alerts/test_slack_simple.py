#!/usr/bin/env python3
"""Simple test of Slack API."""

import json
import requests
from pathlib import Path


def load_slack_config():
    config_path = Path(__file__).parent / "slack_config.json"
    with open(config_path, 'r') as f:
        return json.load(f)


def test_auth():
    """Test authentication."""
    config = load_slack_config()
    bot_token = config['slack']['bot_token']
    
    print("Testing Slack authentication...")
    response = requests.get(
        'https://slack.com/api/auth.test',
        headers={'Authorization': f'Bearer {bot_token}'},
        timeout=5
    )
    
    result = response.json()
    print(json.dumps(result, indent=2))
    return result.get('ok')


def find_channel():
    """Find operacionalxcobranca channel."""
    config = load_slack_config()
    bot_token = config['slack']['bot_token']
    
    print("\nLooking for channel: operacionalxcobranca")
    response = requests.get(
        'https://slack.com/api/conversations.list',
        headers={'Authorization': f'Bearer {bot_token}'},
        params={'types': 'public_channel,private_channel', 'limit': 100},
        timeout=10
    )
    
    result = response.json()
    
    if result.get('ok'):
        for channel in result.get('channels', []):
            if 'operacional' in channel.get('name', '').lower():
                print(f"Found: {channel.get('name')} - ID: {channel.get('id')}")
    else:
        print(f"Error: {result.get('error')}")


if __name__ == "__main__":
    if test_auth():
        print("\n✓ Authentication successful")
        find_channel()
    else:
        print("\n✗ Authentication failed")

