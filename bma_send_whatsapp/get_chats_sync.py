#!/usr/bin/env python3
"""
Get chats from Z-API WhatsApp instance and sync to database.
Makes a GET request to the Z-API chats endpoint and updates contato_whatsapp table.
"""

import os
import json
import sys
from pathlib import Path
from typing import Optional
import requests

# Force pure Python implementation of MySQL connector (required for PyInstaller)
os.environ['MYSQL_CONNECTOR_PYTHON_USE_PURE'] = '1'

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
    name = zapi_chat.get('name', '').strip()

    # Ensure name is never empty or NULL (required for unique constraint)
    phone = zapi_chat.get('phone', '')
    if not name:
        name = f"Unknown - {phone}"

    cedente_grupo = extract_cedente_grupo(name, bool(is_group))

    # Map to database fields
    db_record = {
        'phone': phone,
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
        database=cfg['scheme'],
        use_pure=True  # Force pure Python implementation
    )


def sync_chat_to_db(conn, chat_data: dict) -> tuple[str, str]:
    """
    Sync a single chat to the database.
    Uses INSERT ... ON DUPLICATE KEY UPDATE pattern.

    Handles duplicate name constraint by:
    1. First trying to update by phone (primary unique key)
    2. If name conflict occurs, appends phone to name to make it unique

    Args:
        conn: Database connection
        chat_data: Mapped chat data dictionary

    Returns:
        Tuple of (action, phone) where action is 'inserted' or 'updated'
    """
    cursor = conn.cursor()

    # SQL with ON DUPLICATE KEY UPDATE
    # Note: phone has UNIQUE constraint, name now also has UNIQUE constraint
    sql = """
        INSERT INTO contato_whatsapp (
            phone, name, pinned, messagesUnread, unread, lastMessageTime,
            isGroupAnnouncement, archived, isGroup, isMuted, isMarkedSpam, cedente_grupo,
            last_sync_at
        ) VALUES (
            %(phone)s, %(name)s, %(pinned)s, %(messagesUnread)s, %(unread)s, %(lastMessageTime)s,
            %(isGroupAnnouncement)s, %(archived)s, %(isGroup)s, %(isMuted)s, %(isMarkedSpam)s, %(cedente_grupo)s,
            NOW()
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
            cedente_grupo = VALUES(cedente_grupo),
            last_sync_at = NOW()
    """

    try:
        cursor.execute(sql, chat_data)
        # Determine if it was an insert or update
        action = 'inserted' if cursor.rowcount == 1 else 'updated'

    except mysql.connector.IntegrityError as e:
        # Handle duplicate name constraint violation (error code 1062)
        if e.errno == 1062 and 'idx_unique_name' in str(e):
            # Name already exists for a different phone number
            # Append phone to name to make it unique
            original_name = chat_data['name']
            chat_data['name'] = f"{original_name} ({chat_data['phone']})"

            # Retry with modified name
            cursor.execute(sql, chat_data)
            action = 'inserted' if cursor.rowcount == 1 else 'updated'

            print(f"  ⚠ Duplicate name '{original_name}' - renamed to '{chat_data['name']}'")
        else:
            # Re-raise if it's a different integrity error
            raise

    cursor.close()
    return action, chat_data['phone']


def delete_inactive_contacts(conn, days: int = 30) -> int:
    """
    Delete contacts that have not been seen in the last N sync runs.

    A contact is considered inactive when its last_sync_at is older than
    `days` days, meaning it was not returned by Z-API in any recent sync.
    Contacts whose last_sync_at is NULL (column just added, not yet synced)
    are left untouched.

    Args:
        conn: Database connection
        days: Number of days of inactivity before deletion (default: 30)

    Returns:
        Number of rows deleted
    """
    cursor = conn.cursor()
    cursor.execute(
        """
        DELETE FROM contato_whatsapp
        WHERE last_sync_at IS NOT NULL
          AND last_sync_at < NOW() - INTERVAL %s DAY
        """,
        (days,)
    )
    deleted = cursor.rowcount
    cursor.close()
    return deleted


def fix_duplicate_cedente_groups(conn) -> dict:
    """
    Detect and fix duplicate groups for the same cedente_grupo.

    When multiple groups exist for the same cedente_grupo, this function:
    1. Identifies duplicates
    2. Keeps the most recently active group (highest lastMessageTime)
    3. Deletes older/inactive duplicates
    4. Fixes malformed names (removes phone numbers in parentheses)

    Pattern enforcement: {CEDENTE} X {COMPANY}
    Example: "ALPA X BMA" (not "Alpa X BMA (120363406844884243-group)")

    Args:
        conn: Database connection

    Returns:
        Dictionary with statistics: {
            'duplicates_found': int,
            'groups_deleted': int,
            'names_fixed': int,
            'cedentes_affected': list
        }
    """
    cursor = conn.cursor(dictionary=True)

    stats = {
        'duplicates_found': 0,
        'groups_deleted': 0,
        'names_fixed': 0,
        'cedentes_affected': []
    }

    # Find cedentes with multiple groups
    cursor.execute("""
        SELECT cedente_grupo, COUNT(*) as group_count
        FROM contato_whatsapp
        WHERE isGroup = 1
          AND cedente_grupo IS NOT NULL
          AND cedente_grupo != ''
        GROUP BY cedente_grupo
        HAVING COUNT(*) > 1
        ORDER BY cedente_grupo
    """)

    duplicates = cursor.fetchall()

    if not duplicates:
        cursor.close()
        return stats

    stats['duplicates_found'] = len(duplicates)

    print(f"\n  Found {len(duplicates)} cedente(s) with multiple groups:")

    for dup in duplicates:
        cedente = dup['cedente_grupo']
        count = dup['group_count']

        # Get all groups for this cedente
        cursor.execute("""
            SELECT id_contato_whatsapp, name, phone, lastMessageTime,
                   messagesUnread, isGroup
            FROM contato_whatsapp
            WHERE cedente_grupo = %s
              AND isGroup = 1
            ORDER BY lastMessageTime DESC
        """, (cedente,))

        groups = cursor.fetchall()

        # Check if these are intentionally different groups (different purposes)
        # Groups with different purposes have distinct names beyond the pattern
        unique_names = set()
        for g in groups:
            # Extract the part after "X BMA" or similar patterns
            name_upper = g['name'].upper()
            if ' X BMA' in name_upper:
                suffix = name_upper.split(' X BMA', 1)[1].strip()
                # Remove phone numbers in parentheses
                if '(' in suffix and '-GROUP)' in suffix:
                    suffix = ''
                unique_names.add(suffix)
            else:
                unique_names.add(g['name'])

        # If all groups have the same suffix (or no suffix), they are duplicates
        # If they have different suffixes (e.g., "Operações", "Cedentes"), keep all
        if len(unique_names) <= 1:
            # These are true duplicates - keep only the most active one
            keep_group = groups[0]  # Most recent lastMessageTime
            delete_groups = groups[1:]  # All others

            print(f"\n    {cedente}: {count} duplicate groups found")
            print(f"      ✓ Keeping: {keep_group['name']} (ID: {keep_group['id_contato_whatsapp']})")

            # Fix the name if it has phone number appended
            if '(' in keep_group['name'] and '-group)' in keep_group['name']:
                # Extract clean name (remove phone number in parentheses)
                clean_name = keep_group['name'].split('(')[0].strip()

                # Ensure it follows the pattern: {CEDENTE} X {COMPANY}
                # Normalize the 'X' to uppercase
                if ' x ' in clean_name.lower():
                    parts = clean_name.split(' x ', 1)
                    if len(parts) == 2:
                        clean_name = f"{parts[0].strip()} X {parts[1].strip()}"

                cursor.execute("""
                    UPDATE contato_whatsapp
                    SET name = %s
                    WHERE id_contato_whatsapp = %s
                """, (clean_name, keep_group['id_contato_whatsapp']))

                print(f"      ✓ Fixed name: '{keep_group['name']}' → '{clean_name}'")
                stats['names_fixed'] += 1

            # Delete duplicates
            for del_group in delete_groups:
                cursor.execute("""
                    DELETE FROM contato_whatsapp
                    WHERE id_contato_whatsapp = %s
                """, (del_group['id_contato_whatsapp'],))

                print(f"      ✗ Deleted: {del_group['name']} (ID: {del_group['id_contato_whatsapp']})")
                stats['groups_deleted'] += 1

            stats['cedentes_affected'].append(cedente)
        else:
            # These are intentionally different groups (e.g., different departments)
            print(f"\n    {cedente}: {count} groups (different purposes - keeping all)")
            for g in groups:
                print(f"      - {g['name']}")

    cursor.close()
    return stats


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
        'deleted': 0,
        'errors': 0,
        'groups': 0,
        'groups_with_cedente': 0,
        'duplicates_found': 0,
        'duplicate_groups_deleted': 0,
        'names_fixed': 0,
        'cedentes_affected': []
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

    # Delete contacts not seen in the last 30 days
    print("\n  Cleaning up inactive contacts (not seen in 30+ days)...")
    stats['deleted'] = delete_inactive_contacts(conn, days=30)

    # Fix duplicate cedente groups
    print("\n  Verifying and fixing duplicate cedente groups...")
    dup_stats = fix_duplicate_cedente_groups(conn)
    stats['duplicates_found'] = dup_stats['duplicates_found']
    stats['duplicate_groups_deleted'] = dup_stats['groups_deleted']
    stats['names_fixed'] = dup_stats['names_fixed']
    stats['cedentes_affected'] = dup_stats['cedentes_affected']

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
    print(f"  Deleted (inactive):     {stats['deleted']}")
    print(f"  Errors:                 {stats['errors']}")
    print()
    print(f"Groups:                   {stats['groups']}")
    print(f"  With cedente_grupo:     {stats['groups_with_cedente']}")
    print(f"  Without cedente_grupo:  {stats['groups'] - stats['groups_with_cedente']}")
    print()
    print(f"Duplicate Detection:")
    print(f"  Cedentes with duplicates: {stats['duplicates_found']}")
    print(f"  Duplicate groups deleted: {stats['duplicate_groups_deleted']}")
    print(f"  Names fixed:              {stats['names_fixed']}")
    if stats['cedentes_affected']:
        print(f"  Affected cedentes:        {', '.join(stats['cedentes_affected'])}")
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

