-- Migration: Add UNIQUE constraint to contato_whatsapp.name column
-- Description: Clean up duplicate names and add unique constraint to prevent future duplicates
-- Date: 2026-01-21
-- Author: BMA System

-- ============================================
-- STEP 1: Analyze current duplicates
-- ============================================

SELECT 'Current duplicate names:' AS status;

SELECT name, COUNT(*) as count 
FROM contato_whatsapp 
GROUP BY name 
HAVING count > 1 
ORDER BY count DESC;

-- ============================================
-- STEP 2: Clean up duplicates
-- ============================================

-- Strategy: Keep the record with the lowest id_contato_whatsapp (oldest record)
-- and delete the newer duplicates

SELECT 'Cleaning up duplicate names...' AS status;

-- Create temporary table with IDs to keep (oldest record for each name)
CREATE TEMPORARY TABLE IF NOT EXISTS contacts_to_keep AS
SELECT MIN(id_contato_whatsapp) as id_to_keep, name
FROM contato_whatsapp
WHERE name IS NOT NULL AND name != ''
GROUP BY name;

-- Show records that will be deleted
SELECT 'Records to be deleted (duplicates):' AS status;

SELECT c.id_contato_whatsapp, c.phone, c.name, c.isGroup, c.cedente_grupo
FROM contato_whatsapp c
LEFT JOIN contacts_to_keep k ON c.name = k.name AND c.id_contato_whatsapp = k.id_to_keep
WHERE c.name IS NOT NULL 
  AND c.name != ''
  AND k.id_to_keep IS NULL
ORDER BY c.name, c.id_contato_whatsapp;

-- Delete duplicate records (keep only the oldest one for each name)
DELETE c FROM contato_whatsapp c
LEFT JOIN contacts_to_keep k ON c.name = k.name AND c.id_contato_whatsapp = k.id_to_keep
WHERE c.name IS NOT NULL 
  AND c.name != ''
  AND k.id_to_keep IS NULL;

SELECT CONCAT('Deleted ', ROW_COUNT(), ' duplicate records') AS status;

-- Clean up temporary table
DROP TEMPORARY TABLE IF EXISTS contacts_to_keep;

-- ============================================
-- STEP 3: Handle empty/NULL names
-- ============================================

-- For records with empty or NULL names, we'll append the phone number to make them unique
-- This ensures we don't lose any data

SELECT 'Handling empty/NULL names...' AS status;

-- Update empty string names to NULL first
UPDATE contato_whatsapp 
SET name = NULL 
WHERE name = '';

-- Count records with NULL names
SELECT CONCAT('Records with NULL names: ', COUNT(*)) AS status
FROM contato_whatsapp 
WHERE name IS NULL;

-- For NULL names, set name to 'Unknown - {phone}'
UPDATE contato_whatsapp 
SET name = CONCAT('Unknown - ', phone)
WHERE name IS NULL;

SELECT CONCAT('Updated ', ROW_COUNT(), ' records with NULL names') AS status;

-- ============================================
-- STEP 4: Verify no duplicates remain
-- ============================================

SELECT 'Verifying no duplicates remain...' AS status;

SELECT name, COUNT(*) as count 
FROM contato_whatsapp 
GROUP BY name 
HAVING count > 1;

-- If the above query returns no rows, we're good to proceed

-- ============================================
-- STEP 5: Add UNIQUE constraint
-- ============================================

SELECT 'Adding UNIQUE constraint to name column...' AS status;

-- Add unique constraint
ALTER TABLE contato_whatsapp 
ADD UNIQUE KEY idx_unique_name (name);

SELECT 'UNIQUE constraint added successfully!' AS status;

-- ============================================
-- STEP 6: Verify table structure
-- ============================================

SELECT 'Updated table structure:' AS status;

DESCRIBE contato_whatsapp;

-- Show indexes
SELECT 'Table indexes:' AS status;

SHOW INDEX FROM contato_whatsapp;

-- ============================================
-- STEP 7: Final statistics
-- ============================================

SELECT 'Final statistics:' AS status;

SELECT 
    COUNT(*) as total_contacts,
    SUM(CASE WHEN isGroup = 1 THEN 1 ELSE 0 END) as groups,
    SUM(CASE WHEN isGroup = 0 THEN 1 ELSE 0 END) as individual_contacts,
    SUM(CASE WHEN cedente_grupo IS NOT NULL THEN 1 ELSE 0 END) as groups_with_cedente
FROM contato_whatsapp;

SELECT '✓ Migration completed successfully!' AS status;

