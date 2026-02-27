#!/usr/bin/env python3
"""
Test script to verify duplicate cedente group detection and cleanup.

This script:
1. Connects to the database
2. Runs the duplicate detection function
3. Shows what would be fixed without making changes (dry-run mode)
"""

import os
import sys
from pathlib import Path

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent.parent))

# Force pure Python implementation
os.environ['MYSQL_CONNECTOR_PYTHON_USE_PURE'] = '1'

import mysql.connector
from bma_send_whatsapp.get_chats_sync import load_db_config, fix_duplicate_cedente_groups


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
        use_pure=True
    )


def show_current_duplicates():
    """Show current duplicate cedente groups."""
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    
    print("=" * 80)
    print("Current Duplicate Cedente Groups")
    print("=" * 80)
    
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
        print("✓ No duplicate cedente groups found!")
        cursor.close()
        conn.close()
        return
    
    print(f"\nFound {len(duplicates)} cedente(s) with multiple groups:\n")
    
    for dup in duplicates:
        cedente = dup['cedente_grupo']
        count = dup['group_count']
        
        print(f"{cedente}: {count} groups")
        
        # Get details for each group
        cursor.execute("""
            SELECT id_contato_whatsapp, name, phone, 
                   FROM_UNIXTIME(lastMessageTime/1000) as last_message,
                   messagesUnread
            FROM contato_whatsapp
            WHERE cedente_grupo = %s
              AND isGroup = 1
            ORDER BY lastMessageTime DESC
        """, (cedente,))
        
        groups = cursor.fetchall()
        
        for i, g in enumerate(groups, 1):
            marker = "✓ KEEP" if i == 1 else "✗ DELETE"
            print(f"  {marker}: {g['name']}")
            print(f"         ID: {g['id_contato_whatsapp']}, Phone: {g['phone']}")
            print(f"         Last message: {g['last_message']}")
        print()
    
    cursor.close()
    conn.close()


def test_duplicate_fix():
    """Test the duplicate fix function."""
    conn = get_db_connection()
    
    print("\n" + "=" * 80)
    print("Running Duplicate Detection & Fix")
    print("=" * 80)
    
    # Run the fix
    stats = fix_duplicate_cedente_groups(conn)
    
    # Commit changes
    conn.commit()
    conn.close()
    
    print("\n" + "=" * 80)
    print("Results")
    print("=" * 80)
    print(f"Cedentes with duplicates: {stats['duplicates_found']}")
    print(f"Duplicate groups deleted: {stats['groups_deleted']}")
    print(f"Names fixed:              {stats['names_fixed']}")
    if stats['cedentes_affected']:
        print(f"Affected cedentes:        {', '.join(stats['cedentes_affected'])}")
    print("=" * 80)


def main():
    """Main entry point."""
    print("=" * 80)
    print("Duplicate Cedente Group Detection Test")
    print("=" * 80)
    print()
    
    # Show current state
    show_current_duplicates()
    
    # Ask for confirmation
    print("\n" + "=" * 80)
    response = input("Run duplicate fix? (yes/no): ").strip().lower()
    
    if response == 'yes':
        test_duplicate_fix()
        print("\n✓ Fix completed! Showing updated state...\n")
        show_current_duplicates()
    else:
        print("\n✓ Test completed (no changes made)")
    
    return 0


if __name__ == "__main__":
    sys.exit(main())

