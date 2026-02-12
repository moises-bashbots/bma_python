# Migration: Add usuario_processar and usuario_confirmar Columns

**Date:** 2026-02-12  
**Status:** Ready to apply  
**Impact:** Low (adds nullable columns, no data loss risk)

## Overview

This migration adds two new columns to the `apr_valid_records` table to track which user is selected in the GER system dropdown during proposal processing and confirmation phases.

## New Columns

### 1. `usuario_processar`
- **Type:** VARCHAR(100)
- **Nullable:** YES
- **Default:** NULL
- **Purpose:** Stores the username selected in GER dropdown when clicking the "Processar" button
- **Populated by:** `send_rating_vadu.py` during normal processing phase

### 2. `usuario_confirmar`
- **Type:** VARCHAR(100)
- **Nullable:** YES
- **Default:** NULL
- **Purpose:** Stores the username selected in GER dropdown during confirmation check
- **Populated by:** `send_rating_vadu.py` during confirmation phase

## Business Value

- **Audit Trail:** Track which user account was used to process each proposal
- **Troubleshooting:** Identify if wrong user was selected during automation
- **Compliance:** Maintain record of user assignments for regulatory purposes
- **Analytics:** Analyze which users are processing which proposals

## How to Run

### Option 1: Using Python Script (Recommended)
```bash
cd /home/robot/Dev/bma_python
uv run python slack_integration_alerts/migrations/run_migration_add_usuario_columns.py
```

### Option 2: Using SQL File Directly
```bash
mysql -u robot -p'Credi2024!' -D monitoring_db < slack_integration_alerts/migrations/add_usuario_columns.sql
```

## Verification

After running the migration, verify the columns exist:

```sql
USE monitoring_db;

SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'monitoring_db'
  AND TABLE_NAME = 'apr_valid_records'
  AND COLUMN_NAME IN ('usuario_processar', 'usuario_confirmar');
```

Expected output:
```
+--------------------+---------------+-------------+----------------+----------------------------------------------------------+
| COLUMN_NAME        | COLUMN_TYPE   | IS_NULLABLE | COLUMN_DEFAULT | COLUMN_COMMENT                                           |
+--------------------+---------------+-------------+----------------+----------------------------------------------------------+
| usuario_confirmar  | varchar(100)  | YES         | NULL           | Username selected in GER dropdown during confirmation... |
| usuario_processar  | varchar(100)  | YES         | NULL           | Username selected in GER dropdown when clicking Proc...  |
+--------------------+---------------+-------------+----------------+----------------------------------------------------------+
```

## Rollback

If needed, the migration can be rolled back:

```sql
USE monitoring_db;

ALTER TABLE apr_valid_records DROP COLUMN usuario_processar;
ALTER TABLE apr_valid_records DROP COLUMN usuario_confirmar;
```

## Related Files

- **Migration SQL:** `add_usuario_columns.sql`
- **Migration Script:** `run_migration_add_usuario_columns.py`
- **ORM Model:** `../models_mariadb.py` (needs update)
- **Bot Script:** `../send_rating_vadu.py` (needs update)

## Next Steps

1. ✅ Run this migration
2. ⏳ Update ORM model (`models_mariadb.py`)
3. ⏳ Update bot script (`send_rating_vadu.py`)
4. ⏳ Rebuild and deploy binary
5. ⏳ Update dashboard to display new columns (optional)

