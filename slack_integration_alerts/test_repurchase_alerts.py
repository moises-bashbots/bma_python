#!/usr/bin/env python3
"""
Test script for repurchase alerts - dry run only.
"""

import sys
sys.path.insert(0, '/home/robot/Dev/bma_python/slack_integration_alerts')

from repurchase_report import (
    load_slack_config,
    get_channel_id,
    get_channel_members,
    get_user_info,
    match_operator_to_slack_user,
    send_repurchase_alerts
)


def test_slack_connection():
    """Test Slack connection and channel lookup."""
    print("=" * 100)
    print("TEST: Slack Connection")
    print("=" * 100)
    print()
    
    # Load config
    slack_config = load_slack_config()
    bot_token = slack_config.get('bot_token')
    
    print(f"Bot token configured: {'Yes' if bot_token else 'No'}")
    print()
    
    # Get channel ID
    channel_name = "operacionalxcobranca"
    print(f"Looking up channel: #{channel_name}")
    channel_id = get_channel_id(bot_token, channel_name)
    
    if channel_id:
        print(f"✓ Channel found: {channel_id}")
    else:
        print(f"✗ Channel not found")
        return
    
    print()
    
    # Get channel members
    print("Fetching channel members...")
    member_ids = get_channel_members(bot_token, channel_id)
    print(f"✓ Found {len(member_ids)} members")
    print()
    
    # Get user info for first 10 members
    print("Fetching user info (first 10 members)...")
    for i, user_id in enumerate(member_ids[:10], 1):
        user_info = get_user_info(bot_token, user_id)
        if user_info and not user_info.get('is_bot'):
            profile = user_info.get('profile', {})
            print(f"{i}. {profile.get('real_name', 'N/A')} (@{user_info.get('name', 'N/A')}) - ID: {user_id}")
    
    print()


def test_operator_matching():
    """Test operator name matching."""
    print("=" * 100)
    print("TEST: Operator Matching")
    print("=" * 100)
    print()
    
    # Load config
    slack_config = load_slack_config()
    bot_token = slack_config.get('bot_token')
    
    # Get channel
    channel_name = "operacionalxcobranca"
    channel_id = get_channel_id(bot_token, channel_name)
    
    if not channel_id:
        print("✗ Channel not found")
        return
    
    # Get members
    member_ids = get_channel_members(bot_token, channel_id)
    
    # Get user info
    slack_users = []
    for user_id in member_ids:
        user_info = get_user_info(bot_token, user_id)
        if user_info and not user_info.get('is_bot'):
            slack_users.append(user_info)
    
    print(f"Testing with {len(slack_users)} Slack users")
    print()
    
    # Test operator names from the example
    test_operators = ["Leticia", "Bárbara", "Franciane", "Ranielly", "Thais", "Larissa Queiroz"]
    
    for operator in test_operators:
        user_id = match_operator_to_slack_user(operator, slack_users)
        if user_id:
            # Find the user info
            user_info = next((u for u in slack_users if u.get('id') == user_id), None)
            if user_info:
                profile = user_info.get('profile', {})
                print(f"✓ '{operator}' → {profile.get('real_name')} (@{user_info.get('name')})")
        else:
            print(f"✗ '{operator}' → No match found")
    
    print()


def test_dry_run_alerts():
    """Test sending alerts in dry-run mode."""
    print("=" * 100)
    print("TEST: Dry Run Alerts")
    print("=" * 100)
    print()
    
    # Create sample matched records
    sample_records = [
        {
            'apr_cedente': 'METALURGICA REUTER',
            'apr_numero': 2,
            'excel_operador': 'Leticia',
        },
        {
            'apr_cedente': 'BRAUNAS',
            'apr_numero': 3,
            'excel_operador': 'Bárbara',
        },
        {
            'apr_cedente': 'FRIMAZZ CARNES',
            'apr_numero': 5,
            'excel_operador': 'Franciane',
        },
    ]
    
    # Send alerts in dry-run mode
    stats = send_repurchase_alerts(sample_records, dry_run=True)
    
    print()
    print("Test completed!")
    print()


if __name__ == "__main__":
    test_slack_connection()
    print()
    test_operator_matching()
    print()
    test_dry_run_alerts()

