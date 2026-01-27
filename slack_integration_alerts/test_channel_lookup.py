#!/usr/bin/env python3
"""
Quick test to verify channel lookup is working.
"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

from repurchase_report import (
    load_slack_config,
    get_channel_id,
    get_channel_members,
    SLACK_CHANNEL_NAME
)


def test_channel_lookup():
    """Test finding the operacionalxcobranca channel."""
    print("=" * 100)
    print("CHANNEL LOOKUP TEST")
    print("=" * 100)
    print()
    
    # Load config
    config = load_slack_config()
    bot_token = config['bot_token']
    
    print(f"Looking for channel: #{SLACK_CHANNEL_NAME}")
    print()
    
    # Test get_channel_id
    channel_id = get_channel_id(bot_token, SLACK_CHANNEL_NAME)
    
    if channel_id:
        print(f"✓ Channel found!")
        print(f"  Channel ID: {channel_id}")
        print()
        
        # Test getting members
        print("Getting channel members...")
        members = get_channel_members(bot_token, channel_id)
        
        if members:
            print(f"✓ Found {len(members)} members")
            print(f"  First 10 member IDs: {members[:10]}")
            print()
            print("✓✓✓ CHANNEL LOOKUP WORKING ✓✓✓")
        else:
            print("✗ Failed to get channel members")
    else:
        print(f"✗ Channel '#{SLACK_CHANNEL_NAME}' not found")
        print()
        print("This could mean:")
        print("1. The channel doesn't exist")
        print("2. The bot is not a member of the channel")
        print("3. The bot doesn't have the right permissions")


if __name__ == "__main__":
    test_channel_lookup()

