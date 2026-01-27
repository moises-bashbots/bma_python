# Changelog - APR Invalid Records Checker

## Version 2.0 - Enhanced (2026-01-20)

### 🎯 Major Enhancements

#### 1. Added RAMO Column
- ✅ Added `ramo` column from `cedente` table to all queries
- ✅ Included in Excel exports
- ✅ Displayed in valid records summary

#### 2. Separate Alert Processing
- ✅ **DUPLICATA alerts** - Separate Excel file and Slack notification
  - Output: `duplicatas_invalidas/duplicatas_invalidas_YYYY-MM-DD_HHMMSS.xlsx`
  - Columns: GERENTE, PROPOSTA, DATA, CEDENTE, EMPRESA, STATUS, RAMO, DUPLICATA, NFE, MOTIVO_INVALIDO
  
- ✅ **SEUNO alerts** - Separate Excel file and Slack notification
  - Output: `seuno_invalidos/seuno_invalidos_YYYY-MM-DD_HHMMSS.xlsx`
  - Columns: GERENTE, PROPOSTA, DATA, CEDENTE, EMPRESA, STATUS, RAMO, SEUNO, DUPLICATA, SEUNO_COMPANY, SEUNO_RANGE, VERIFICATION_DIGIT, MOTIVO_INVALIDO

#### 3. Valid Records Query with Status Filter
- ✅ After processing invalid records, queries valid records
- ✅ Excludes all invalid records from the list
- ✅ Filters by status: "Aguardando Analista" or further (configurable)
- ✅ Displays in human-readable format with:
  - Record count and total value
  - Breakdown by STATUS
  - Formatted currency values

#### 4. Dry Run Mode
- ✅ Default behavior: DRY RUN (no Slack notifications)
- ✅ Use `--send` flag to actually send notifications
- ✅ Clear visual indicators when in dry-run mode

#### 5. Improved File Management
- ✅ Separate cleanup for `duplicatas_invalidas/` and `seuno_invalidos/` directories
- ✅ Keeps only files from current date
- ✅ Removes old files automatically

### 📊 Program Flow

```
1. Query APR_CAPA + APR_TITULOS + CEDENTE (with RAMO column)
   ↓
2. Validate DUPLICATA and SEUNO for all records
   ↓
3. STEP 1: Process Invalid DUPLICATA
   - Print table
   - Export to Excel (duplicatas_invalidas/)
   - Send Slack alert (if --send)
   - Cleanup old files
   ↓
4. STEP 2: Process Invalid SEUNO
   - Print table
   - Export to Excel (seuno_invalidos/)
   - Send Slack alert (if --send)
   - Cleanup old files
   ↓
5. STEP 3: Query Valid Records
   - Exclude all invalid records
   - Filter by status (optional)
   - Display summary with totals
   - Group by STATUS
```

### 🚀 Usage

**Dry Run (Default - No Slack notifications):**
```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts
../.venv/bin/python3 query_apr_invalidos_status.py
```

**Send Alerts to Slack:**
```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts
../.venv/bin/python3 query_apr_invalidos_status.py --send
```

### 📁 Output Files

**Invalid DUPLICATA:**
- Directory: `slack_integration_alerts/duplicatas_invalidas/`
- Pattern: `duplicatas_invalidas_YYYY-MM-DD_HHMMSS.xlsx`

**Invalid SEUNO:**
- Directory: `slack_integration_alerts/seuno_invalidos/`
- Pattern: `seuno_invalidos_YYYY-MM-DD_HHMMSS.xlsx`

### 📱 Slack Notifications

**DUPLICATA Alert:**
```
🚨 *Alerta de DUPLICATA Inválida*

Encontrados *N* registros com DUPLICATA inválido(s)
Data: DD/MM/YYYY
Arquivo anexo com detalhes.
```

**SEUNO Alert:**
```
⚠️ *Alerta de SEUNO Inválido*

Encontrados *N* registros com SEUNO inválido(s)
Data: DD/MM/YYYY
Arquivo anexo com detalhes.
```

### 🎨 Valid Records Display

```
VALID RECORDS - READY FOR PROCESSING (showing first 54 of 54)
============================================================================
#     GERENTE         PROPOSTA   CEDENTE                   RAMO                 STATUS                         VALOR
----------------------------------------------------------------------------
1     AMANDA          3          HEANLU                    RATING C-            Aguardando Analista        R$ 149.90
...
                                                                                         TOTAL: R$ 29,744.80
============================================================================

VALID RECORDS BY STATUS:
----------------------------------------------------------------------------
  Aguardando Analista               54 records    R$       29,744.80
----------------------------------------------------------------------------
```

### ✅ Testing Results

**Test Date:** 2026-01-20

**Results:**
- ✅ 54 records queried
- ✅ 0 invalid DUPLICATA
- ✅ 0 invalid SEUNO
- ✅ 54 valid records displayed
- ✅ Total value: R$ 29,744.80
- ✅ All records in "Aguardando Analista" status
- ✅ RAMO column included: "RATING C-"
- ✅ Dry-run mode working correctly

### 🔧 Technical Changes

**Modified Functions:**
1. `query_apr_invalidos_with_status()` - Added RAMO column
2. `export_invalid_records_to_excel()` - Split into two functions
3. `send_slack_notification()` - Added dry_run parameter and alert_type
4. `cleanup_old_files()` - Added alert_type parameter
5. `main()` - Complete rewrite with 3-step workflow

**New Functions:**
1. `export_invalid_duplicata_to_excel()` - DUPLICATA-specific export
2. `export_invalid_seuno_to_excel()` - SEUNO-specific export
3. `query_valid_records_with_status_filter()` - Query valid records
4. `print_valid_records_table()` - Display valid records

---

## Version 1.0 - Initial (2026-01-20)

### Features
- ✅ Unified DUPLICATA and SEUNO validation
- ✅ STATUS column from APR_CAPA
- ✅ Single Excel export
- ✅ Slack notifications
- ✅ File cleanup

---

**Current Version:** 2.0  
**Last Updated:** 2026-01-20

