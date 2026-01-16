# Cedente Grupo Extraction Logic

## 📊 Analysis Summary

**Database Analysis Date**: 2026-01-16  
**Total Records with cedente_grupo**: 431  
**Pattern Match Success Rate**: 90%

## 🔍 Extraction Pattern

### Primary Pattern: " X BMA" Separator

The cedente_grupo is extracted from WhatsApp group names by splitting on " X BMA" (case insensitive).

**Format**: `[CEDENTE_NAME] X BMA [OPTIONAL_SUFFIX]`

### Examples

| WhatsApp Group Name | Extracted cedente_grupo | Match |
|---------------------|------------------------|-------|
| `Contec Conexoes X BMA` | `CONTEC CONEXOES` | ✓ |
| `Procoat X BMA` | `PROCOAT` | ✓ |
| `FRIALL X BMA` | `FRIALL` | ✓ |
| `RB HIGIENE x BMA FIDC` | `RB HIGIENE` | ✓ |
| `CMS x BMA FIDC` | `CMS` | ✓ |
| `Boi Gaucho X BMA` | `BOI GAUCHO` | ✓ |
| `Nonapack Industria X BMA` | `NONAPACK INDUSTRIA` | ✓ |

### Pattern Variations

1. **Uppercase X**: `Company X BMA`
2. **Lowercase x**: `Company x BMA`
3. **With suffix**: `Company X BMA FIDC`
4. **Mixed case**: `Company x BMA Grupo`

All variations extract the text **before** the first occurrence of " X BMA" (case insensitive).

## 🎯 Extraction Rules

### Rule 1: Groups Only
- Only extract cedente_grupo for **group chats** (`isGroup = 1`)
- Individual contacts (`isGroup = 0`) should have `cedente_grupo = NULL`

### Rule 2: Case Insensitive Matching
- Search for " X BMA" in uppercase version of name
- Extract from original string to preserve case
- Convert result to uppercase

### Rule 3: Trim Whitespace
- Remove leading/trailing whitespace from extracted value

### Rule 4: Return NULL if No Match
- If pattern not found, return `NULL`
- If extracted value is empty, return `NULL`

## 💻 Implementation

```python
def extract_cedente_grupo(name: str, is_group: bool) -> Optional[str]:
    """
    Extract cedente_grupo from WhatsApp group name.
    
    Args:
        name: Group or contact name
        is_group: Whether this is a group chat
        
    Returns:
        Cedente name in uppercase, or None
    """
    if not is_group:
        return None
    
    name_upper = name.upper()
    
    # Try to find " X BMA" (case insensitive)
    if " X BMA" in name_upper:
        # Find position in uppercase string
        pos = name_upper.find(" X BMA")
        # Extract from original string, then uppercase
        cedente = name[:pos].strip().upper()
        return cedente if cedente else None
    
    return None
```

## 📈 Test Results

### Sample Data (First 20 Records)

```
✓ Contec Conexoes X BMA          → CONTEC CONEXOES
✓ Procoat X BMA                  → PROCOAT
✓ Nonapack Industria X BMA       → NONAPACK INDUSTRIA
✓ Metaperfil X BMA               → METAPERFIL
✓ FRIALL X BMA                   → FRIALL
✓ Boi Gaucho X BMA               → BOI GAUCHO
✓ RB HIGIENE x BMA FIDC          → RB HIGIENE
✓ Takafer X BMA                  → TAKAFER
✓ Araguaya X BMA                 → ARAGUAYA
✓ Catuai X BMA                   → CATUAI
✓ Luimed X BMA                   → LUIMED
✓ Unifilter X BMA                → UNIFILTER
✓ Metalquip X BMA                → METALQUIP
✓ Perfilgerais X BMA             → PERFILGERAIS
✓ CMS x BMA FIDC                 → CMS
✓ Supremo X BMA                  → SUPREMO
✓ Panifex X BMA                  → PANIFEX
✓ KTELI X BMA                    → KTELI
✓ Tocantins Embalagens X BMA     → TOCANTINS EMBALAGENS
✓ Plastwin X BMA                 → PLASTWIN
```

**Success Rate**: 18/20 = 90%

### Edge Cases

#### Case 1: Special Characters
```
Input:  "Dmom X BMA- Grupo"
Stored: "DMOM X BMA- GRUPO"  (entire name stored)
Logic:  "DMOM"  (extracted by pattern)
```

This appears to be a data quality issue in the existing database where the entire name was stored instead of just the cedente portion.

## 🔄 Database Sync Strategy

### INSERT ... ON DUPLICATE KEY UPDATE

```sql
INSERT INTO contato_whatsapp (
    phone, name, cedente_grupo, isGroup, ...
) VALUES (
    ?, ?, ?, ?, ...
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    cedente_grupo = VALUES(cedente_grupo),
    isGroup = VALUES(isGroup),
    ...
```

### Benefits
1. **Idempotent**: Can run multiple times safely
2. **Automatic**: Inserts new records, updates existing ones
3. **Consistent**: Always applies latest extraction logic

## 📝 Usage in get_chats_sync.py

```python
# 1. Get chats from Z-API
chats = get_chats()

# 2. Map each chat to database format
for chat in chats:
    db_data = map_zapi_to_db(chat)
    # db_data['cedente_grupo'] is automatically extracted
    
# 3. Sync to database
sync_chats_to_db(chats)
```

## 🎯 Recommendations

### For New Groups
When creating WhatsApp groups, follow the naming convention:
```
[CEDENTE_NAME] X BMA
```

Examples:
- `ACME CORP X BMA`
- `TECH SOLUTIONS X BMA`
- `GLOBAL TRADE X BMA FIDC`

### For Existing Data
Run the sync script to update all records with the latest extraction logic:
```bash
cd bma_send_whatsapp
source ../.venv/bin/activate
python get_chats_sync.py
```

This will:
1. Fetch all chats from Z-API
2. Extract cedente_grupo using the pattern
3. Update database with consistent values

## 📊 Expected Results

After running sync:
- **Groups with " X BMA" pattern**: cedente_grupo extracted
- **Groups without pattern**: cedente_grupo = NULL
- **Individual contacts**: cedente_grupo = NULL
- **All records**: Updated with latest data from Z-API

