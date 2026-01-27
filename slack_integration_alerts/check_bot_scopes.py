#!/usr/bin/env python3
"""
Check bot token scopes and permissions.
"""

import json
import requests
from pathlib import Path


def load_slack_config():
    """Load Slack configuration."""
    config_path = Path(__file__).parent / "slack_config.json"
    with open(config_path, 'r') as f:
        config = json.load(f)
    return config['slack']


def check_bot_scopes():
    """Check what scopes the bot token has."""
    config = load_slack_config()
    bot_token = config['bot_token']
    
    print("=" * 100)
    print("BOT TOKEN SCOPES CHECK")
    print("=" * 100)
    print()
    
    # Test auth
    print("1. Testing authentication...")
    response = requests.post(
        'https://slack.com/api/auth.test',
        headers={'Authorization': f'Bearer {bot_token}'},
        timeout=10
    )
    
    result = response.json()
    print(f"   Team: {result.get('team')}")
    print(f"   User: {result.get('user')}")
    print(f"   Bot ID: {result.get('bot_id')}")
    print()
    
    # Try to get bot info to see scopes
    print("2. Checking bot info...")
    response = requests.get(
        'https://slack.com/api/bots.info',
        headers={'Authorization': f'Bearer {bot_token}'},
        params={'bot': result.get('bot_id')},
        timeout=10
    )
    
    bot_info = response.json()
    print(f"   Response: {json.dumps(bot_info, indent=2)}")
    print()
    
    # Try different channel list methods
    print("3. Testing different channel list methods...")
    print()
    
    # Method 1: Public channels only
    print("   a) Public channels only:")
    response = requests.get(
        'https://slack.com/api/conversations.list',
        headers={'Authorization': f'Bearer {bot_token}'},
        params={
            'types': 'public_channel',
            'limit': 100,
            'exclude_archived': True
        },
        timeout=15
    )
    
    result = response.json()
    if result.get('ok'):
        channels = result.get('channels', [])
        print(f"      Found {len(channels)} public channels")
        for ch in channels:
            print(f"        - #{ch.get('name')} (member: {ch.get('is_member')})")
    else:
        print(f"      Error: {result.get('error')}")
    print()
    
    # Method 2: Private channels
    print("   b) Private channels:")
    response = requests.get(
        'https://slack.com/api/conversations.list',
        headers={'Authorization': f'Bearer {bot_token}'},
        params={
            'types': 'private_channel',
            'limit': 100,
            'exclude_archived': True
        },
        timeout=15
    )
    
    result = response.json()
    if result.get('ok'):
        channels = result.get('channels', [])
        print(f"      Found {len(channels)} private channels")
        for ch in channels:
            print(f"        - #{ch.get('name')} (member: {ch.get('is_member')})")
    else:
        print(f"      Error: {result.get('error')}")
    print()
    
    # Method 3: All conversations
    print("   c) All conversations (public + private):")
    response = requests.get(
        'https://slack.com/api/conversations.list',
        headers={'Authorization': f'Bearer {bot_token}'},
        params={
            'types': 'public_channel,private_channel',
            'limit': 200,
            'exclude_archived': True
        },
        timeout=15
    )
    
    result = response.json()
    if result.get('ok'):
        channels = result.get('channels', [])
        print(f"      Found {len(channels)} total channels")
        for ch in channels:
            privacy = "private" if ch.get('is_private') else "public"
            print(f"        - #{ch.get('name'):<40} ({privacy}, member: {ch.get('is_member')})")
    else:
        print(f"      Error: {result.get('error')}")
    print()
    
    # Method 4: Search for specific channel by name
    print("   d) Search for 'operacionalxcobranca' by name:")
    response = requests.get(
        'https://slack.com/api/conversations.list',
        headers={'Authorization': f'Bearer {bot_token}'},
        params={
            'types': 'public_channel,private_channel',
            'limit': 1000,
            'exclude_archived': False  # Include archived too
        },
        timeout=20
    )
    
    result = response.json()
    if result.get('ok'):
        channels = result.get('channels', [])
        found = False
        for ch in channels:
            if 'operacional' in ch.get('name', '').lower() or 'cobranca' in ch.get('name', '').lower():
                found = True
                print(f"      ✓ Found: #{ch.get('name')}")
                print(f"        ID: {ch.get('id')}")
                print(f"        Private: {ch.get('is_private')}")
                print(f"        Member: {ch.get('is_member')}")
                print(f"        Archived: {ch.get('is_archived')}")
        
        if not found:
            print(f"      ✗ Channel not found in {len(channels)} channels")
            print()
            print("      This means either:")
            print("      1. The channel doesn't exist")
            print("      2. The bot doesn't have permission to see it")
            print("      3. The channel name is different")
    else:
        print(f"      Error: {result.get('error')}")
    print()
    
    print("=" * 100)
    print("RECOMMENDATIONS")
    print("=" * 100)
    print()
    print("If the channel 'operacionalxcobranca' exists but is not visible:")
    print()
    print("1. Make sure the bot is INVITED to the channel:")
    print("   - Go to the #operacionalxcobranca channel in Slack")
    print("   - Type: /invite @automatedreportandreq")
    print("   - Or click 'Add people' and search for 'automatedreportandreq'")
    print()
    print("2. Check bot token scopes in Slack App settings:")
    print("   Required scopes:")
    print("   - channels:read (view public channels)")
    print("   - groups:read (view private channels)")
    print("   - chat:write (send messages)")
    print("   - users:read (view user information)")
    print()
    print("3. If you added new scopes, you need to REINSTALL the app")
    print()


if __name__ == "__main__":
    check_bot_scopes()

