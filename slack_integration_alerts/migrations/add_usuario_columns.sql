-- Migration: Add usuario_processar and usuario_confirmar columns to apr_valid_records
-- Date: 2026-02-12
-- Description: Track which user is selected in GER dropdown during processing and confirmation

USE monitoring_db;

-- Add usuario_processar column (tracks user when clicking "Processar" button)
ALTER TABLE apr_valid_records
ADD COLUMN usuario_processar VARCHAR(100) DEFAULT NULL
COMMENT 'Username selected in GER dropdown when clicking Processar button';

-- Add usuario_confirmar column (tracks user during confirmation check)
ALTER TABLE apr_valid_records
ADD COLUMN usuario_confirmar VARCHAR(100) DEFAULT NULL
COMMENT 'Username selected in GER dropdown during confirmation check';

-- Verify the columns were added
DESCRIBE apr_valid_records;

-- Show sample of the table structure
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

