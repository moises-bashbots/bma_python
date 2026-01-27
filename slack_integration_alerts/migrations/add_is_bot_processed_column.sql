-- Migration: Add is_bot_processed column to apr_valid_records table
-- Date: 2026-01-23
-- Description: Adds a new column to track when the bot clicks "Processar" button

-- Add the is_bot_processed column
ALTER TABLE apr_valid_records
ADD COLUMN is_bot_processed TINYINT NOT NULL DEFAULT 0
COMMENT 'Bot processing status: 0=not processed by bot, 1=bot clicked Processar'
AFTER is_processado;

-- Add index for the new column
CREATE INDEX idx_is_bot_processed ON apr_valid_records(is_bot_processed);

-- Verify the column was added
DESCRIBE apr_valid_records;

