#!/usr/bin/env python3
"""Test Slack API connection and permissions."""

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
    
    response = requests.get(
        'https://slack.com/api/auth.test',
        headers={'Authorization': f'Bearer {bot_token}'},
        timeout=10
    )
    
    result = response.json()
    print("=== Auth Test ===")
    print(json.dumps(result, indent=2))
    return result

def list_channels():
    """List all channels."""
    config = load_slack_config()
    bot_token = config['slack']['bot_token']

    all_channels = []
    cursor = None

    while True:
        params = {'types': 'public_channel,private_channel', 'limit': 200}
        if cursor:
            params['cursor'] = cursor

        response = requests.get(
            'https://slack.com/api/conversations.list',
            headers={'Authorization': f'Bearer {bot_token}'},
            params=params,
            timeout=10
        )

        result = response.json()

        if not result.get('ok'):
            print(f"Error: {result.get('error')}")
            break

        all_channels.extend(result.get('channels', []))

        cursor = result.get('response_metadata', {}).get('next_cursor')
        if not cursor:
            break

    print("\n=== Channels List ===")
    print(f"Total channels: {len(all_channels)}")
    for channel in sorted(all_channels, key=lambda x: x['name']):
        print(f"  - {channel['name']} (ID: {channel['id']}) - Private: {channel.get('is_private', False)}")

    # Search for alertascredito1
    print("\n=== Searching for 'alertascredito1' ===")
    found = [ch for ch in all_channels if 'alerta' in ch['name'].lower() or 'credito' in ch['name'].lower()]
    if found:
        for ch in found:
            print(f"  Found: {ch['name']} (ID: {ch['id']})")
    else:
        print("  No matching channels found")

    return all_channels

if __name__ == "__main__":
    test_auth()
    list_channels()

