#!/usr/bin/env python3
"""
Get chats from Z-API WhatsApp instance and sync to database.
Makes a GET request to the Z-API chats endpoint and updates contato_whatsapp table.
"""

import json
import sys
from pathlib import Path
from typing import Optional
import requests
import mysql.connector
from datetime import datetime


def load_zapi_config() -> dict:
    """Load Z-API configuration from zapi_config.json."""
    config_paths = [
        Path.cwd() / "zapi_config.json",  # Current working directory (for deployed binary)
        Path(__file__).parent / "zapi_config.json",
        Path(__file__).parent.parent / "zapi_config.json",
    ]

    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)

    raise FileNotFoundError(
        f"Configuration file not found in: {[str(p) for p in config_paths]}"
    )


def load_db_config() -> dict:
    """Load database configuration from databases_config.json."""
    config_paths = [
        Path.cwd() / "databases_config.json",  # Current working directory (for deployed binary)
        Path(__file__).parent / "databases_config.json",
        Path(__file__).parent.parent / "database" / "databases_config.json",
    ]

    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)

    raise FileNotFoundError(
        f"Database configuration file not found in: {[str(p) for p in config_paths]}"
    )


def extract_cedente_grupo(name: str, is_group: bool) -> Optional[str]:
    """
    Extract cedente_grupo from WhatsApp group name.

    Patterns analyzed from existing data (431 records):

    Primary Pattern (90% of cases):
    1. "Company Name X BMA" → "COMPANY NAME"
    2. "Company Name x BMA FIDC" → "COMPANY NAME"
    3. "Company Name X BMA FIDC" → "COMPANY NAME"

    Alternative Patterns (10% of cases):
    4. "BMA - Company Name" → "COMPANY NAME"
    5. "Company + BMA" → "COMPANY"
    6. "BMA & Company" → "COMPANY"
    7. "Company - BMA" → "COMPANY"
    8. "BMA/Company" → "COMPANY"

    Special cases:
    - Only extracts for groups (isGroup = True)
    - Case insensitive matching
    - Returns uppercase cedente name
    - If no pattern matches, returns None

    Args:
        name: Group or contact name
        is_group: Whether this is a group chat

    Returns:
        Cedente name in uppercase, or None if not a group or pattern doesn't match
    """
    if not is_group:
        return None

    name_upper = name.upper()

    # Pattern 1: "Company X BMA" (most common - 90%)
    if " X BMA" in name_upper:
        pos = name_upper.find(" X BMA")
        cedente = name[:pos].strip().upper()
        return cedente if cedente else None

    # Pattern 2: "Company + BMA" (check before " - BMA" to handle "Finly + BMA - FIDC")
    if " + BMA" in name_upper:
        pos = name_upper.find(" + BMA")
        cedente = name[:pos].strip().upper()
        return cedente if cedente else None

    # Pattern 3: "BMA - Company" (reverse pattern)
    if "BMA -" in name_upper:
        pos = name_upper.find("BMA -")
        cedente = name[pos + 5:].strip().upper()  # Skip "BMA - "
        return cedente if cedente else None

    # Pattern 4: "Company - BMA"
    if " - BMA" in name_upper:
        pos = name_upper.find(" - BMA")
        cedente = name[:pos].strip().upper()
        return cedente if cedente else None

    # Pattern 5: "BMA + Company" or "BMA+Company"
    if "BMA+" in name_upper or "BMA +" in name_upper:
        # Extract after "BMA+"
        if "BMA+" in name_upper:
            pos = name_upper.find("BMA+")
            cedente = name[pos + 4:].strip().upper()
        else:
            pos = name_upper.find("BMA +")
            cedente = name[pos + 5:].strip().upper()
        return cedente if cedente else None

    # Pattern 6: "BMA & Company"
    if "BMA &" in name_upper:
        pos = name_upper.find("BMA &")
        cedente = name[pos + 5:].strip().upper()
        # Remove any prefix like "QI |" or similar
        if " | " in cedente:
            cedente = cedente.split(" | ", 1)[0].strip()
        return cedente if cedente else None

    # Pattern 7: "BMA/Company" or "BMA / Company"
    if "BMA/" in name_upper or "BMA /" in name_upper:
        if "BMA/" in name_upper:
            pos = name_upper.find("BMA/")
            cedente = name[pos + 4:].strip().upper()
        else:
            pos = name_upper.find("BMA /")
            cedente = name[pos + 5:].strip().upper()
        return cedente if cedente else None

    # No pattern matched
    return None


def map_zapi_to_db(zapi_chat: dict) -> dict:
    """
    Map Z-API chat response to database fields.
    
    Args:
        zapi_chat: Single chat object from Z-API response
        
    Returns:
        Dictionary with database field names and converted values
    """
    # Convert boolean to tinyint (0 or 1)
    is_group_announcement = 1 if zapi_chat.get('isGroupAnnouncement', False) else 0
    is_group = 1 if zapi_chat.get('isGroup', False) else 0
    
    # Convert string numbers to integers
    messages_unread = int(zapi_chat.get('messagesUnread', 0))
    unread = int(zapi_chat.get('unread', 0))
    
    # Convert string timestamp to bigint
    last_message_time = int(zapi_chat.get('lastMessageTime', 0))
    
    # Extract cedente_grupo using business logic
    name = zapi_chat.get('name', '')
    cedente_grupo = extract_cedente_grupo(name, bool(is_group))
    
    # Map to database fields
    db_record = {
        'phone': zapi_chat.get('phone', ''),
        'name': name,
        'pinned': zapi_chat.get('pinned', 'false'),
        'messagesUnread': messages_unread,
        'unread': unread,
        'lastMessageTime': last_message_time,
        'isGroupAnnouncement': is_group_announcement,
        'archived': zapi_chat.get('archived', 'false'),
        'isGroup': is_group,
        'isMuted': zapi_chat.get('isMuted', '0'),
        'isMarkedSpam': zapi_chat.get('isMarkedSpam', 'false'),
        'cedente_grupo': cedente_grupo
    }
    
    return db_record


def get_chats() -> list:
    """
    Get chats from Z-API.
    
    Returns:
        List of chat dictionaries
    """
    config = load_zapi_config()
    zapi = config['zapi']
    
    # Build URL
    url = f"{zapi['base_url']}/instances/{zapi['instance_id']}/token/{zapi['token']}/chats"

    # Build headers
    headers = {
        'Client-Token': zapi['client_token']
    }
    
    print("=" * 80)
    print("Z-API Get Chats")
    print("=" * 80)
    print(f"URL: {url}")
    print()
    
    # Make GET request
    print("Making GET request...")
    response = requests.get(url, headers=headers)
    
    # Check response status
    print(f"Status Code: {response.status_code}")
    response.raise_for_status()
    
    # Parse JSON response
    data = response.json()

    return data if isinstance(data, list) else []


def get_db_connection():
    """Get MariaDB database connection."""
    config = load_db_config()
    cfg = config['databases']['mariadb']

    return mysql.connector.connect(
        host=cfg['server'],
        port=cfg['port'],
        user=cfg['user'],
        password=cfg['password'],
        database=cfg['scheme']
    )


def sync_chat_to_db(conn, chat_data: dict) -> tuple[str, str]:
    """
    Sync a single chat to the database.
    Uses INSERT ... ON DUPLICATE KEY UPDATE pattern.

    Args:
        conn: Database connection
        chat_data: Mapped chat data dictionary

    Returns:
        Tuple of (action, phone) where action is 'inserted' or 'updated'
    """
    cursor = conn.cursor()

    # SQL with ON DUPLICATE KEY UPDATE
    sql = """
        INSERT INTO contato_whatsapp (
            phone, name, pinned, messagesUnread, unread, lastMessageTime,
            isGroupAnnouncement, archived, isGroup, isMuted, isMarkedSpam, cedente_grupo
        ) VALUES (
            %(phone)s, %(name)s, %(pinned)s, %(messagesUnread)s, %(unread)s, %(lastMessageTime)s,
            %(isGroupAnnouncement)s, %(archived)s, %(isGroup)s, %(isMuted)s, %(isMarkedSpam)s, %(cedente_grupo)s
        )
        ON DUPLICATE KEY UPDATE
            name = VALUES(name),
            pinned = VALUES(pinned),
            messagesUnread = VALUES(messagesUnread),
            unread = VALUES(unread),
            lastMessageTime = VALUES(lastMessageTime),
            isGroupAnnouncement = VALUES(isGroupAnnouncement),
            archived = VALUES(archived),
            isGroup = VALUES(isGroup),
            isMuted = VALUES(isMuted),
            isMarkedSpam = VALUES(isMarkedSpam),
            cedente_grupo = VALUES(cedente_grupo)
    """

    cursor.execute(sql, chat_data)

    # Determine if it was an insert or update
    action = 'inserted' if cursor.rowcount == 1 else 'updated'

    cursor.close()
    return action, chat_data['phone']


def sync_chats_to_db(chats: list) -> dict:
    """
    Sync all chats to database.

    Args:
        chats: List of chat dictionaries from Z-API

    Returns:
        Dictionary with sync statistics
    """
    conn = get_db_connection()

    stats = {
        'total': len(chats),
        'inserted': 0,
        'updated': 0,
        'errors': 0,
        'groups': 0,
        'groups_with_cedente': 0
    }

    print("\n" + "=" * 80)
    print("Syncing to Database")
    print("=" * 80)

    for i, chat in enumerate(chats, 1):
        try:
            # Map Z-API data to database format
            db_data = map_zapi_to_db(chat)

            # Track groups
            if db_data['isGroup']:
                stats['groups'] += 1
                if db_data['cedente_grupo']:
                    stats['groups_with_cedente'] += 1

            # Sync to database
            action, phone = sync_chat_to_db(conn, db_data)

            if action == 'inserted':
                stats['inserted'] += 1
            else:
                stats['updated'] += 1

            # Progress indicator
            if i % 50 == 0:
                print(f"  Processed {i}/{stats['total']} chats...")

        except Exception as e:
            stats['errors'] += 1
            print(f"  ✗ Error syncing {chat.get('phone', 'unknown')}: {e}")

    # Commit all changes
    conn.commit()
    conn.close()

    return stats


def print_stats(stats: dict) -> None:
    """Print sync statistics."""
    print("\n" + "=" * 80)
    print("Sync Statistics")
    print("=" * 80)
    print(f"Total chats:              {stats['total']}")
    print(f"  Inserted (new):         {stats['inserted']}")
    print(f"  Updated (existing):     {stats['updated']}")
    print(f"  Errors:                 {stats['errors']}")
    print()
    print(f"Groups:                   {stats['groups']}")
    print(f"  With cedente_grupo:     {stats['groups_with_cedente']}")
    print(f"  Without cedente_grupo:  {stats['groups'] - stats['groups_with_cedente']}")
    print("=" * 80)


def main():
    """Main entry point."""
    try:
        # Get chats from Z-API
        chats = get_chats()
        print(f"✓ Retrieved {len(chats)} chats from Z-API\n")

        if not chats:
            print("No chats to sync.")
            return 0

        # Sync to database
        stats = sync_chats_to_db(chats)

        # Print statistics
        print_stats(stats)

        if stats['errors'] > 0:
            print(f"\n⚠ Completed with {stats['errors']} errors")
            return 1
        else:
            print("\n✓ Sync completed successfully!")
            return 0

    except requests.exceptions.HTTPError as e:
        print(f"\n✗ HTTP Error: {e}", file=sys.stderr)
        print(f"Response: {e.response.text}", file=sys.stderr)
        return 1
    except requests.exceptions.RequestException as e:
        print(f"\n✗ Request Error: {e}", file=sys.stderr)
        return 1
    except Exception as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())

