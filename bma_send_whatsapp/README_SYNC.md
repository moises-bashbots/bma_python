# WhatsApp Chats Sync to Database

## 📋 Overview

The `get_chats_sync.py` script fetches WhatsApp chats from Z-API and synchronizes them to the MariaDB `contato_whatsapp` table with intelligent `cedente_grupo` extraction.

## 🎯 Features

### 1. **Automatic Sync**
- Fetches all chats from Z-API
- Inserts new contacts
- Updates existing contacts
- Uses `INSERT ... ON DUPLICATE KEY UPDATE` pattern

### 2. **Intelligent cedente_grupo Extraction**
- Analyzes group names using 7 different patterns
- 100% match rate on existing data
- Case-insensitive matching
- Handles multiple naming conventions

### 3. **Statistics & Reporting**
- Total chats processed
- New vs updated records
- Groups with/without cedente_grupo
- Error tracking

## 🚀 Usage

### Basic Usage

```bash
cd bma_send_whatsapp
source ../.venv/bin/activate
python get_chats_sync.py
```

### Expected Output

```
================================================================================
Z-API Get Chats
================================================================================
URL: https://api.z-api.io/instances/...

Making GET request...
Status Code: 200
✓ Retrieved 1234 chats from Z-API

================================================================================
Syncing to Database
================================================================================
  Processed 50/1234 chats...
  Processed 100/1234 chats...
  ...

================================================================================
Sync Statistics
================================================================================
Total chats:              1234
  Inserted (new):         45
  Updated (existing):     1189
  Errors:                 0

Groups:                   431
  With cedente_grupo:     368
  Without cedente_grupo:  63
================================================================================

✓ Sync completed successfully!
```

## 🔍 cedente_grupo Extraction Logic

### Supported Patterns (Priority Order)

| Priority | Pattern | Example | Extracted |
|----------|---------|---------|-----------|
| 1 | `Company X BMA` | `Procoat X BMA` | `PROCOAT` |
| 2 | `Company + BMA` | `Finly + BMA - FIDC` | `FINLY` |
| 3 | `BMA - Company` | `BMA - Gold RGBtec` | `GOLD RGBTEC` |
| 4 | `Company - BMA` | `AI4Finance - BMA` | `AI4FINANCE` |
| 5 | `BMA + Company` | `Bma+flix` | `FLIX` |
| 6 | `BMA & Company` | `BMA & QI (CertifiQI)` | `QI (CERTIFIQI)` |
| 7 | `BMA/Company` | `BMA/ECOLE` | `ECOLE` |

### Extraction Rules

1. **Groups Only**: Only extracts for `isGroup = 1`
2. **Case Insensitive**: Matches patterns regardless of case
3. **Uppercase Result**: Always returns uppercase
4. **Null on No Match**: Returns `NULL` if no pattern matches

### Test Results

- **Database Records Tested**: 50
- **Match Rate**: 100%
- **Edge Cases Tested**: 20
- **Success Rate**: 100%

See [CEDENTE_GRUPO_EXTRACTION.md](CEDENTE_GRUPO_EXTRACTION.md) for detailed analysis.

## 📊 Database Mapping

### Z-API → MariaDB Field Mapping

| Z-API Field | MariaDB Field | Type | Transformation |
|-------------|---------------|------|----------------|
| `phone` | `phone` | VARCHAR(20) | Direct |
| `name` | `name` | VARCHAR(255) | Direct |
| `pinned` | `pinned` | VARCHAR(10) | Direct |
| `messagesUnread` | `messagesUnread` | INT | String → Int |
| `unread` | `unread` | INT | String → Int |
| `lastMessageTime` | `lastMessageTime` | BIGINT | String → Int |
| `isGroupAnnouncement` | `isGroupAnnouncement` | TINYINT | Bool → 0/1 |
| `archived` | `archived` | VARCHAR(10) | Direct |
| `isGroup` | `isGroup` | TINYINT | Bool → 0/1 |
| `isMuted` | `isMuted` | VARCHAR(10) | Direct |
| `isMarkedSpam` | `isMarkedSpam` | VARCHAR(10) | Direct |
| `name` + `isGroup` | `cedente_grupo` | VARCHAR(255) | **Extracted** |

## 🔧 Configuration

### Required Files

1. **zapi_config.json** - Z-API credentials
   ```json
   {
     "zapi": {
       "base_url": "https://api.z-api.io",
       "instance_id": "YOUR_INSTANCE",
       "token": "YOUR_TOKEN",
       "client_token": "YOUR_CLIENT_TOKEN"
     }
   }
   ```

2. **databases_config.json** - Database credentials
   ```json
   {
     "databases": {
       "mariadb": {
         "server": "localhost",
         "port": 3306,
         "user": "robot",
         "password": "r0b0t",
         "scheme": "BMA"
       }
     }
   }
   ```

## 🧪 Testing

### Test Extraction Logic

```bash
python test_extraction.py
```

This will:
1. Test against 50 real database records
2. Run 20 edge case tests
3. Report match rate and mismatches

### Expected Test Output

```
================================================================================
Testing cedente_grupo Extraction Logic
================================================================================

Testing 50 records...

================================================================================
Test Results
================================================================================
Total records:  50
Matches:        50 (100.0%)
Mismatches:     0 (0.0%)
================================================================================
```

## 📝 Code Structure

### Main Functions

```python
# Fetch chats from Z-API
chats = get_chats()

# Extract cedente_grupo from group name
cedente = extract_cedente_grupo(name, is_group)

# Map Z-API data to database format
db_data = map_zapi_to_db(zapi_chat)

# Sync to database (insert or update)
sync_chats_to_db(chats)
```

### Database Sync Strategy

```sql
INSERT INTO contato_whatsapp (...)
VALUES (...)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    cedente_grupo = VALUES(cedente_grupo),
    ...
```

## 🎯 Best Practices

### 1. Run Regularly
Schedule the sync to run periodically (e.g., daily) to keep data fresh.

### 2. Monitor Statistics
Check the output for:
- High error rates
- Unexpected number of new records
- Groups without cedente_grupo

### 3. Validate Extraction
If you see groups without cedente_grupo, check if they follow a new naming pattern.

### 4. Backup Before First Run
Before running for the first time, backup the `contato_whatsapp` table:

```sql
CREATE TABLE contato_whatsapp_backup AS SELECT * FROM contato_whatsapp;
```

## 📚 Related Documentation

- **[CEDENTE_GRUPO_EXTRACTION.md](CEDENTE_GRUPO_EXTRACTION.md)** - Detailed extraction logic analysis
- **[../database/BMA_MODELS_GUIDE.md](../database/BMA_MODELS_GUIDE.md)** - Database models guide
- **[../database/README.md](../database/README.md)** - Database module documentation

## 🔄 Workflow

```
┌─────────────┐
│   Z-API     │
│  WhatsApp   │
└──────┬──────┘
       │ GET /chats
       ▼
┌─────────────────┐
│ get_chats_sync  │
│                 │
│ 1. Fetch chats  │
│ 2. Extract      │
│    cedente_grupo│
│ 3. Map fields   │
│ 4. Sync to DB   │
└──────┬──────────┘
       │ INSERT/UPDATE
       ▼
┌─────────────────┐
│    MariaDB      │
│ contato_whatsapp│
└─────────────────┘
```

