#!/usr/bin/env python3
"""
Test operator name matching to Slack users.
"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

from repurchase_report import (
    load_slack_config,
    get_channel_id,
    get_channel_members,
    get_user_info,
    match_operator_to_slack_user,
    SLACK_CHANNEL_NAME
)


def test_operator_matching():
    """Test matching operator names to Slack users."""
    print("=" * 120)
    print("OPERATOR MATCHING TEST")
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
    
    # Get members
    print(f"2. Getting channel members...")
    members = get_channel_members(bot_token, channel_id)
    print(f"   ✓ Found {len(members)} members")
    print()
    
    # Get all user names
    print(f"3. Fetching user information for all {len(members)} members...")
    print()

    slack_users = []
    for i, user_id in enumerate(members, 1):
        user_info = get_user_info(bot_token, user_id)
        if user_info and not user_info.get('is_bot'):
            # user_info is the full Slack user object with 'profile' key
            profile = user_info.get('profile', {})
            real_name = profile.get('real_name', 'N/A')
            display_name = profile.get('display_name', 'N/A')
            slack_users.append(user_info)  # Append the full user_info object
            print(f"   {i:2d}. {real_name:<40} (display: {display_name})")

    print()
    print(f"   ✓ Found {len(slack_users)} human users (excluding bots)")
    print()
    
    # Test operator matching
    print("=" * 120)
    print("4. Testing Operator Matching")
    print("=" * 120)
    print()

    # Test operators from the example data
    test_operators = [
        'Leticia',
        'Bárbara',
        'Franciane',
        'Barbara',  # Without accent
        'LETICIA',  # Uppercase
        'leticia',  # Lowercase
    ]

    matches = 0

    for operator in test_operators:
        print(f"Operator: '{operator}'")

        # match_operator_to_slack_user takes operator_name and slack_users list
        user_id = match_operator_to_slack_user(operator, slack_users)

        if user_id:
            # Find the matched user in our list
            matched_user = next((u for u in slack_users if u.get('id') == user_id), None)
            if matched_user:
                profile = matched_user.get('profile', {})
                real_name = profile.get('real_name', 'N/A')
                print(f"  ✓ Matched to: {real_name} (ID: {user_id})")
                matches += 1
            else:
                print(f"  ✓ Matched to user ID: {user_id}")
                matches += 1
        else:
            print(f"  ✗ No match found")

        print()
    
    # Summary
    print("=" * 120)
    print("SUMMARY")
    print("=" * 120)
    print()
    print(f"Channel members: {len(members)}")
    print(f"Human users: {len(slack_users)}")
    print(f"Test operators: {len(test_operators)}")
    print(f"Successful matches: {matches}/{len(test_operators)}")
    print()

    if matches > 0:
        print("✓ Operator matching is working!")
        print()
        print("The system can match operator names from Excel to Slack users.")
    else:
        print("⚠ No operators matched")
        print()
        print("Possible reasons:")
        print("1. The test operator names don't match any users in the channel")
        print("2. The matching algorithm needs adjustment")
        print()
        print("Available users in channel:")
        for user in slack_users[:10]:
            profile = user.get('profile', {})
            print(f"  - {profile.get('real_name', 'N/A')}")


if __name__ == "__main__":
    test_operator_matching()

