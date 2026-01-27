#!/usr/bin/env python3
"""
List all Slack channels to find the correct channel name.
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


def list_all_channels():
    """List all Slack channels."""
    config = load_slack_config()
    bot_token = config['bot_token']
    
    print("=" * 100)
    print("SLACK CHANNELS LIST")
    print("=" * 100)
    print()
    
    try:
        # Get all channels (public and private)
        response = requests.get(
            'https://slack.com/api/conversations.list',
            headers={'Authorization': f'Bearer {bot_token}'},
            params={
                'types': 'public_channel,private_channel',
                'limit': 200,
                'exclude_archived': True
            },
            timeout=10
        )
        
        result = response.json()
        
        if not result.get('ok'):
            print(f"❌ Error: {result.get('error')}")
            return
        
        channels = result.get('channels', [])
        
        print(f"Found {len(channels)} channels:\n")
        
        # Group channels
        public_channels = []
        private_channels = []
        
        for channel in channels:
            if channel.get('is_private'):
                private_channels.append(channel)
            else:
                public_channels.append(channel)
        
        # Display public channels
        if public_channels:
            print("PUBLIC CHANNELS:")
            print("-" * 100)
            for channel in sorted(public_channels, key=lambda x: x.get('name', '')):
                name = channel.get('name', 'N/A')
                channel_id = channel.get('id', 'N/A')
                num_members = channel.get('num_members', 0)
                print(f"  #{name:<40} ID: {channel_id:<15} Members: {num_members}")
            print()
        
        # Display private channels
        if private_channels:
            print("PRIVATE CHANNELS:")
            print("-" * 100)
            for channel in sorted(private_channels, key=lambda x: x.get('name', '')):
                name = channel.get('name', 'N/A')
                channel_id = channel.get('id', 'N/A')
                num_members = channel.get('num_members', 0)
                print(f"  #{name:<40} ID: {channel_id:<15} Members: {num_members}")
            print()
        
        # Search for channels containing "operacional" or "cobranca"
        print("=" * 100)
        print("CHANNELS MATCHING 'operacional' OR 'cobranca':")
        print("=" * 100)
        print()
        
        matching = []
        for channel in channels:
            name = channel.get('name', '').lower()
            if 'operacional' in name or 'cobranca' in name or 'cobrança' in name:
                matching.append(channel)
        
        if matching:
            for channel in matching:
                name = channel.get('name', 'N/A')
                channel_id = channel.get('id', 'N/A')
                is_private = "Private" if channel.get('is_private') else "Public"
                num_members = channel.get('num_members', 0)
                print(f"  #{name}")
                print(f"    ID: {channel_id}")
                print(f"    Type: {is_private}")
                print(f"    Members: {num_members}")
                print()
        else:
            print("  No matching channels found.")
            print()
            print("  Suggestion: Create a channel named 'operacionalxcobranca' or update")
            print("  the SLACK_CHANNEL_NAME constant in repurchase_report.py")
            print()
        
    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    list_all_channels()

