#!/usr/bin/env python3
"""
Example mapping logic for Z-API response to contato_whatsapp table
This shows the exact field mapping and data transformations needed
"""

def extract_cedente_grupo(name: str, is_group: bool) -> str | None:
    """
    Extract cedente_grupo from group name.
    
    Pattern: "Company Name X BMA" → "COMPANY NAME"
    
    Args:
        name: Group or contact name
        is_group: Whether this is a group chat
        
    Returns:
        Cedente name in uppercase, or None if not a group or pattern doesn't match
    """
    if not is_group:
        return None
    
    if " X BMA" in name:
        cedente = name.split(" X BMA")[0].strip().upper()
        return cedente
    
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


# ============================================================================
# Example Usage
# ============================================================================

if __name__ == "__main__":
    # Sample Z-API responses
    
    # Example 1: Individual contact
    individual_chat = {
        "pinned": "true",
        "messagesUnread": "0",
        "unread": "0",
        "lid": "111012053315828@lid",
        "lastMessageTime": "1768497607000",
        "isGroupAnnouncement": False,
        "archived": "false",
        "phone": "553134483014",
        "name": "BMA Adm",
        "isGroup": False,
        "isMuted": "0",
        "isMarkedSpam": "false"
    }
    
    # Example 2: Group chat
    group_chat = {
        "pinned": "false",
        "messagesUnread": "0",
        "unread": "0",
        "lastMessageTime": "1768584538000",
        "isGroupAnnouncement": False,
        "archived": "false",
        "phone": "120363381681585792-group",
        "name": "GRUPO SYL x BMA",
        "isGroup": True,
        "isMuted": "0",
        "isMarkedSpam": "false"
    }
    
    # Example 3: Group with different pattern
    group_chat_2 = {
        "pinned": "false",
        "messagesUnread": "5",
        "unread": "5",
        "lastMessageTime": "1768501204000",
        "isGroupAnnouncement": False,
        "archived": "false",
        "phone": "120363045800800245-group",
        "name": "Contec Conexoes X BMA",
        "isGroup": True,
        "isMuted": "0",
        "isMarkedSpam": "false"
    }
    
    print("=" * 80)
    print("Z-API to Database Mapping Examples")
    print("=" * 80)
    
    # Map individual contact
    print("\n1. Individual Contact:")
    print("-" * 80)
    print(f"Z-API Input: {individual_chat['name']} ({individual_chat['phone']})")
    db_record = map_zapi_to_db(individual_chat)
    print("\nDatabase Record:")
    for key, value in db_record.items():
        print(f"  {key:25} = {value}")
    
    # Map group chat
    print("\n2. Group Chat:")
    print("-" * 80)
    print(f"Z-API Input: {group_chat['name']} ({group_chat['phone']})")
    db_record = map_zapi_to_db(group_chat)
    print("\nDatabase Record:")
    for key, value in db_record.items():
        print(f"  {key:25} = {value}")
    
    # Map group chat with cedente extraction
    print("\n3. Group Chat with Cedente Extraction:")
    print("-" * 80)
    print(f"Z-API Input: {group_chat_2['name']} ({group_chat_2['phone']})")
    db_record = map_zapi_to_db(group_chat_2)
    print("\nDatabase Record:")
    for key, value in db_record.items():
        print(f"  {key:25} = {value}")
    
    print("\n" + "=" * 80)
    print("Mapping completed successfully!")
    print("=" * 80)

