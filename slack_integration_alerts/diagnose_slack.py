#!/usr/bin/env python3
"""
Comprehensive diagnostic script for Slack alert sending.
Tests every component step-by-step.
"""

import sys
import json
import requests
from pathlib import Path
from typing import Optional, Dict, List, Any

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent))

from repurchase_report import (
    load_slack_config,
    get_channel_id,
    get_channel_members,
    get_user_info,
    match_operator_to_slack_user,
    remove_accents,
    SLACK_CHANNEL_NAME
)


def print_section(title: str):
    """Print a section header."""
    print()
    print("=" * 120)
    print(f"STEP: {title}")
    print("=" * 120)
    print()


def test_1_slack_config():
    """Test 1: Verify Slack configuration file exists and is valid."""
    print_section("1. Slack Configuration File")

    config_path = Path(__file__).parent / "slack_config.json"

    print(f"Config file path: {config_path}")
    print(f"File exists: {config_path.exists()}")

    if not config_path.exists():
        print("❌ FAILED: slack_config.json not found")
        return None

    try:
        with open(config_path, 'r') as f:
            config = json.load(f)

        print("✓ File loaded successfully")
        print(f"Config structure: {list(config.keys())}")

        if 'slack' in config:
            slack_config = config['slack']
            print(f"Slack config keys: {list(slack_config.keys())}")

            if 'bot_token' in slack_config:
                token = slack_config['bot_token']
                print(f"✓ Bot token found (length: {len(token)})")
                print(f"  Token prefix: {token[:10]}...")
                return token
            else:
                print("❌ FAILED: 'bot_token' not found in slack config")
                return None
        else:
            print("❌ FAILED: 'slack' key not found in config")
            return None

    except Exception as e:
        print(f"❌ FAILED: Error loading config: {e}")
        return None


def test_2_slack_auth(bot_token: str):
    """Test 2: Verify Slack authentication."""
    print_section("2. Slack Authentication")

    print("Testing auth.test endpoint...")

    try:
        response = requests.post(
            'https://slack.com/api/auth.test',
            headers={'Authorization': f'Bearer {bot_token}'},
            timeout=10
        )

        result = response.json()

        print(f"Response status: {response.status_code}")
        print(f"Response: {json.dumps(result, indent=2)}")

        if result.get('ok'):
            print("✓ Authentication successful")
            print(f"  Team: {result.get('team')}")
            print(f"  User: {result.get('user')}")
            print(f"  User ID: {result.get('user_id')}")
            print(f"  Bot ID: {result.get('bot_id')}")
            return True
        else:
            print(f"❌ FAILED: {result.get('error')}")
            return False

    except Exception as e:
        print(f"❌ FAILED: Exception: {e}")
        import traceback
        traceback.print_exc()
        return False


def test_3_list_channels(bot_token: str):
    """Test 3: List all available channels."""
    print_section("3. List All Channels")

    print("Fetching channel list...")

    try:
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

        if not result.get('ok'):
            print(f"❌ FAILED: {result.get('error')}")
            return []

        channels = result.get('channels', [])
        print(f"✓ Found {len(channels)} channels")
        print()

        # Show all channels
        print("All channels:")
        for channel in sorted(channels, key=lambda x: x.get('name', '')):
            name = channel.get('name', 'N/A')
            channel_id = channel.get('id', 'N/A')
            is_member = channel.get('is_member', False)
            is_private = channel.get('is_private', False)
            num_members = channel.get('num_members', 0)

            member_status = "✓ Bot is member" if is_member else "✗ Bot NOT member"
            privacy = "Private" if is_private else "Public"

            print(f"  #{name:<40} | {privacy:<8} | Members: {num_members:<4} | {member_status}")

        return channels

    except Exception as e:
        print(f"❌ FAILED: Exception: {e}")
        import traceback
        traceback.print_exc()
        return []


def test_4_find_target_channel(bot_token: str, channels: List[Dict]):
    """Test 4: Find the target channel."""
    print_section("4. Find Target Channel")

    print(f"Looking for channel: #{SLACK_CHANNEL_NAME}")
    print()

    # Try direct lookup
    channel_id = get_channel_id(bot_token, SLACK_CHANNEL_NAME)

    if channel_id:
        print(f"✓ Channel found: {channel_id}")

        # Get channel details
        for channel in channels:
            if channel.get('id') == channel_id:
                print(f"  Name: #{channel.get('name')}")
                print(f"  ID: {channel.get('id')}")
                print(f"  Is member: {channel.get('is_member')}")
                print(f"  Is private: {channel.get('is_private')}")
                print(f"  Members: {channel.get('num_members')}")

                if not channel.get('is_member'):
                    print()
                    print("⚠ WARNING: Bot is NOT a member of this channel!")
                    print("  The bot needs to be added to the channel to send messages.")
                    print(f"  Please add the bot to #{SLACK_CHANNEL_NAME}")

                return channel_id

        return channel_id
    else:
        print(f"❌ FAILED: Channel '#{SLACK_CHANNEL_NAME}' not found")
        print()
        print("Searching for similar channel names...")

        for channel in channels:
            name = channel.get('name', '').lower()
            if 'operacional' in name or 'cobranca' in name:
                print(f"  Found similar: #{channel.get('name')} (ID: {channel.get('id')})")

        return None


def test_5_get_channel_members(bot_token: str, channel_id: str):
    """Test 5: Get channel members."""
    print_section("5. Get Channel Members")

    print(f"Fetching members for channel: {channel_id}")

    try:
        members = get_channel_members(bot_token, channel_id)

        if members:
            print(f"✓ Found {len(members)} members")
            print(f"  Member IDs: {members[:10]}{'...' if len(members) > 10 else ''}")
            return members
        else:
            print("❌ FAILED: No members found or error occurred")
            return []

    except Exception as e:
        print(f"❌ FAILED: Exception: {e}")
        import traceback
        traceback.print_exc()
        return []


def test_6_get_user_info(bot_token: str, members: List[str]):
    """Test 6: Get user information for channel members."""
    print_section("6. Get User Information")

    if not members:
        print("❌ FAILED: No members to test")
        return []

    print(f"Testing user info retrieval for {min(5, len(members))} members...")
    print()

    users = []

    for i, user_id in enumerate(members[:5], 1):
        print(f"{i}. User ID: {user_id}")

        try:
            user_info = get_user_info(bot_token, user_id)

            if user_info:
                real_name = user_info.get('real_name', 'N/A')
                display_name = user_info.get('display_name', 'N/A')
                is_bot = user_info.get('is_bot', False)

                print(f"   ✓ Real name: {real_name}")
                print(f"   ✓ Display name: {display_name}")
                print(f"   ✓ Is bot: {is_bot}")

                users.append(user_info)
            else:
                print(f"   ✗ Failed to get user info")

        except Exception as e:
            print(f"   ✗ Exception: {e}")

        print()

    if users:
        print(f"✓ Successfully retrieved {len(users)} user profiles")
        return users
    else:
        print("❌ FAILED: Could not retrieve any user info")
        return []


def test_7_operator_matching(bot_token: str, channel_id: str):
    """Test 7: Test operator name matching."""
    print_section("7. Operator Name Matching")

    # Test operators from the example
    test_operators = [
        'Leticia',
        'Bárbara',
        'Franciane',
        'Barbara',  # Without accent
        'LETICIA',  # Uppercase
    ]

    print("Testing operator matching...")
    print()

    matches = 0

    for operator in test_operators:
        print(f"Operator: '{operator}'")

        try:
            user_id = match_operator_to_slack_user(operator, bot_token, channel_id)

            if user_id:
                # Get user info to show who was matched
                user_info = get_user_info(bot_token, user_id)
                if user_info:
                    print(f"  ✓ Matched to: {user_info.get('real_name')} (ID: {user_id})")
                    matches += 1
                else:
                    print(f"  ✓ Matched to user ID: {user_id}")
                    matches += 1
            else:
                print(f"  ✗ No match found")

        except Exception as e:
            print(f"  ✗ Exception: {e}")

        print()

    print(f"Matching results: {matches}/{len(test_operators)} operators matched")

    if matches > 0:
        print("✓ Operator matching is working")
        return True
    else:
        print("❌ FAILED: No operators matched")
        return False


def test_8_send_test_message(bot_token: str, channel_id: str):
    """Test 8: Send a test message."""
    print_section("8. Send Test Message")

    print("⚠ This will send an actual test message to the channel!")
    print(f"Channel: #{SLACK_CHANNEL_NAME} ({channel_id})")
    print()

    # Ask for confirmation
    response = input("Do you want to send a test message? (yes/no): ").strip().lower()

    if response != 'yes':
        print("Skipped - user declined")
        return False

    print()
    print("Sending test message...")

    test_message = "🧪 Test message from repurchase alert system - please ignore"

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

        print(f"Response: {json.dumps(result, indent=2)}")

        if result.get('ok'):
            print("✓ Test message sent successfully!")
            print(f"  Message timestamp: {result.get('ts')}")
            return True
        else:
            print(f"❌ FAILED: {result.get('error')}")

            # Provide helpful error messages
            error = result.get('error')
            if error == 'not_in_channel':
                print()
                print("⚠ The bot is not a member of the channel!")
                print(f"  Please add the bot to #{SLACK_CHANNEL_NAME}")
            elif error == 'channel_not_found':
                print()
                print("⚠ Channel not found or bot doesn't have access")
            elif error == 'missing_scope':
                print()
                print("⚠ Bot token is missing required permissions")
                print("  Required scopes: chat:write, channels:read, users:read")

            return False

    except Exception as e:
        print(f"❌ FAILED: Exception: {e}")
        import traceback
        traceback.print_exc()
        return False


def main():
    """Run all diagnostic tests."""
    print()
    print("╔" + "═" * 118 + "╗")
    print("║" + " " * 40 + "SLACK ALERT DIAGNOSTIC TOOL" + " " * 50 + "║")
    print("╚" + "═" * 118 + "╝")

    results = {}

    # Test 1: Config file
    bot_token = test_1_slack_config()
    results['config'] = bot_token is not None

    if not bot_token:
        print()
        print("❌ Cannot continue without valid bot token")
        return

    # Test 2: Authentication
    results['auth'] = test_2_slack_auth(bot_token)

    if not results['auth']:
        print()
        print("❌ Cannot continue without valid authentication")
        return

    # Test 3: List channels
    channels = test_3_list_channels(bot_token)
    results['list_channels'] = len(channels) > 0

    # Test 4: Find target channel
    channel_id = test_4_find_target_channel(bot_token, channels)
    results['find_channel'] = channel_id is not None

    if not channel_id:
        print()
        print("❌ Cannot continue without target channel")
        return

    # Test 5: Get members
    members = test_5_get_channel_members(bot_token, channel_id)
    results['get_members'] = len(members) > 0

    # Test 6: Get user info
    users = test_6_get_user_info(bot_token, members)
    results['get_user_info'] = len(users) > 0

    # Test 7: Operator matching
    results['operator_matching'] = test_7_operator_matching(bot_token, channel_id)

    # Test 8: Send test message
    results['send_message'] = test_8_send_test_message(bot_token, channel_id)

    # Final summary
    print_section("DIAGNOSTIC SUMMARY")

    print("Test Results:")
    print()

    tests = [
        ('1. Slack Configuration', results.get('config', False)),
        ('2. Slack Authentication', results.get('auth', False)),
        ('3. List Channels', results.get('list_channels', False)),
        ('4. Find Target Channel', results.get('find_channel', False)),
        ('5. Get Channel Members', results.get('get_members', False)),
        ('6. Get User Information', results.get('get_user_info', False)),
        ('7. Operator Matching', results.get('operator_matching', False)),
        ('8. Send Test Message', results.get('send_message', False)),
    ]

    passed = sum(1 for _, result in tests if result)
    total = len(tests)

    for test_name, result in tests:
        status = "✓ PASS" if result else "✗ FAIL"
        print(f"  {status}  {test_name}")

    print()
    print(f"Overall: {passed}/{total} tests passed")
    print()

    if passed == total:
        print("✓✓✓ ALL TESTS PASSED ✓✓✓")
        print()
        print("The Slack alert system is fully functional!")
    else:
        print("⚠ SOME TESTS FAILED")
        print()
        print("Please review the failed tests above and fix the issues.")


if __name__ == "__main__":
    main()

