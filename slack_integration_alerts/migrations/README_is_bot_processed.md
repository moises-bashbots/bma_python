# Migration: Add is_bot_processed Column

## Overview
This migration adds a new column `is_bot_processed` to the `apr_valid_records` table to track when the bot successfully clicks the "Processar" button in the GER system.

## Date
2026-01-23

## Changes Made

### 1. Database Schema
- **Table**: `apr_valid_records`
- **New Column**: `is_bot_processed`
  - Type: `TINYINT`
  - Default: `0`
  - Nullable: `NO`
  - Comment: "Bot processing status: 0=not processed by bot, 1=bot clicked Processar"
- **New Index**: `idx_is_bot_processed` on `is_bot_processed` column

### 2. Model Update
- Updated `models_mariadb.py` to include the new `is_bot_processed` field
- Added index definition in `__table_args__`

### 3. Application Logic
- Updated `send_rating_vadu.py` to set `is_bot_processed = 1` when:
  - The bot successfully clicks the "Processar" button
  - The bot completes the Altera/Grava workflow
- `is_bot_processed` remains `0` when:
  - The button value is "Reprocessar" (already processed)
  - The bot skips clicking for any reason

## Usage

### Running the Migration
```bash
cd slack_integration_alerts
../.venv/bin/python migrations/run_migration_add_is_bot_processed.py
```

### Querying Records
```sql
-- Get all records processed by the bot
SELECT * FROM apr_valid_records WHERE is_bot_processed = 1;

-- Get records that were marked as processed but not by the bot
SELECT * FROM apr_valid_records WHERE is_processado = 1 AND is_bot_processed = 0;

-- Get unprocessed records
SELECT * FROM apr_valid_records WHERE is_processado = 0;
```

## Difference Between is_processado and is_bot_processed

- **is_processado**: Indicates the record has been reviewed/handled (either manually or by bot)
  - Set to `1` when the proposal is opened and checked
  - Used to avoid processing the same record multiple times

- **is_bot_processed**: Indicates the bot actually clicked "Processar" button
  - Set to `1` ONLY when the bot clicks "Processar" and completes Altera/Grava
  - Remains `0` if the button shows "Reprocessar" (already processed before)
  - Useful for tracking bot's actual actions vs. just checking records

## Files Modified
1. `models_mariadb.py` - Added column definition
2. `send_rating_vadu.py` - Added logic to set is_bot_processed = 1
3. `migrations/add_is_bot_processed_column.sql` - SQL migration script
4. `migrations/run_migration_add_is_bot_processed.py` - Python migration runner

