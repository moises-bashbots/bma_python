# WhatsApp Contacts Mapping

## Field Mapping: Z-API Response → MariaDB Table

### Database Table: `contato_whatsapp`

| Database Column | Type | Nullable | Z-API Field | Z-API Type | Notes |
|----------------|------|----------|-------------|------------|-------|
| `id_contato_whatsapp` | int(11) | NO | - | - | Auto-increment primary key |
| `data_atualizacao` | timestamp | NO | - | - | Auto-updated timestamp |
| `phone` | varchar(50) | NO | `phone` | string | **UNIQUE KEY** - Primary identifier |
| `name` | varchar(255) | YES | `name` | string | Contact/Group name |
| `pinned` | varchar(10) | YES | `pinned` | string | "true" or "false" |
| `messagesUnread` | int(11) | YES | `messagesUnread` | string | Convert to int |
| `unread` | int(11) | YES | `unread` | string | Convert to int |
| `lastMessageTime` | bigint(20) | YES | `lastMessageTime` | string | Unix timestamp in milliseconds |
| `isGroupAnnouncement` | tinyint(1) | YES | `isGroupAnnouncement` | boolean | Convert to 0/1 |
| `archived` | varchar(10) | YES | `archived` | string | "true" or "false" |
| `isGroup` | tinyint(1) | YES | `isGroup` | boolean | Convert to 0/1 |
| `isMuted` | varchar(10) | YES | `isMuted` | string | "0" or "1" |
| `isMarkedSpam` | varchar(10) | YES | `isMarkedSpam` | string | "true" or "false" |
| `cedente_grupo` | varchar(255) | YES | - | - | **NOT in Z-API** - Business logic field |

### Fields in Z-API Response NOT in Database

| Z-API Field | Type | Description |
|-------------|------|-------------|
| `lid` | string | Local ID (e.g., "111012053315828@lid") - Not stored |

### Data Type Conversions Required

1. **String to Integer**:
   - `messagesUnread`: "0" → 0
   - `unread`: "0" → 0
   - `lastMessageTime`: "1768497607000" → 1768497607000

2. **Boolean to TinyInt**:
   - `isGroupAnnouncement`: false → 0, true → 1
   - `isGroup`: false → 0, true → 1

3. **String Values** (stored as-is):
   - `pinned`: "true" or "false"
   - `archived`: "true" or "false"
   - `isMuted`: "0" or "1"
   - `isMarkedSpam`: "true" or "false"

## Sample Data Comparison

### Z-API Response (Individual Contact)
```json
{
  "pinned": "true",
  "messagesUnread": "0",
  "unread": "0",
  "lid": "111012053315828@lid",
  "lastMessageTime": "1768497607000",
  "isGroupAnnouncement": false,
  "archived": "false",
  "phone": "553134483014",
  "name": "BMA Adm",
  "isGroup": false,
  "isMuted": "0",
  "isMarkedSpam": "false"
}
```

### Database Record (Group Contact)
```
id_contato_whatsapp:     1
data_atualizacao:        2026-01-16 03:30:02
phone:                   120363045800800245-group
name:                    Contec Conexoes X BMA
pinned:                  false
messagesUnread:          0
unread:                  0
lastMessageTime:         1768501204000
isGroupAnnouncement:     0
archived:                false
isGroup:                 1
isMuted:                 0
isMarkedSpam:            false
cedente_grupo:           CONTEC CONEXOES
```

## Update Strategy

### UPSERT Logic (INSERT or UPDATE)

Use `phone` as the unique identifier:

1. **If phone exists**: UPDATE all fields
2. **If phone doesn't exist**: INSERT new record

### Fields to Update

- ✅ `name` - May change
- ✅ `pinned` - User can pin/unpin
- ✅ `messagesUnread` - Changes frequently
- ✅ `unread` - Changes frequently
- ✅ `lastMessageTime` - Updates with new messages
- ✅ `isGroupAnnouncement` - May change
- ✅ `archived` - User can archive/unarchive
- ✅ `isGroup` - Should not change (but update for safety)
- ✅ `isMuted` - User can mute/unmute
- ✅ `isMarkedSpam` - User can mark/unmark spam
- ⚠️ `cedente_grupo` - **Business logic field** - requires separate logic

### Fields NOT to Update

- ❌ `id_contato_whatsapp` - Primary key
- ❌ `data_atualizacao` - Auto-updated by database

## Business Logic: `cedente_grupo`

This field is NOT provided by Z-API. It requires business logic:

### Current Pattern (from existing data)
- Group name: "Contec Conexoes X BMA" → cedente_grupo: "CONTEC CONEXOES"
- Group name: "Procoat X BMA" → cedente_grupo: "PROCOAT"
- Group name: "Nonapack Industria X BMA" → cedente_grupo: "NONAPACK INDUSTRIA"

### Extraction Logic
```python
# If group name contains " X BMA"
if " X BMA" in name and isGroup:
    cedente_grupo = name.split(" X BMA")[0].strip().upper()
else:
    cedente_grupo = None
```

## SQL Operations

### INSERT (New Contact)
```sql
INSERT INTO contato_whatsapp (
    phone, name, pinned, messagesUnread, unread, 
    lastMessageTime, isGroupAnnouncement, archived, 
    isGroup, isMuted, isMarkedSpam, cedente_grupo
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
```

### UPDATE (Existing Contact)
```sql
UPDATE contato_whatsapp SET
    name = ?,
    pinned = ?,
    messagesUnread = ?,
    unread = ?,
    lastMessageTime = ?,
    isGroupAnnouncement = ?,
    archived = ?,
    isGroup = ?,
    isMuted = ?,
    isMarkedSpam = ?,
    cedente_grupo = ?
WHERE phone = ?
```

### UPSERT (MySQL/MariaDB)
```sql
INSERT INTO contato_whatsapp (
    phone, name, pinned, messagesUnread, unread, 
    lastMessageTime, isGroupAnnouncement, archived, 
    isGroup, isMuted, isMarkedSpam, cedente_grupo
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
```

## Implementation Checklist

- [ ] Parse Z-API response JSON
- [ ] Convert data types (string → int, boolean → tinyint)
- [ ] Extract `cedente_grupo` from group names
- [ ] Use UPSERT to insert or update records
- [ ] Handle errors gracefully
- [ ] Log sync statistics (new, updated, errors)

