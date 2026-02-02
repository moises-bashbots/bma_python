# NFEChave Validation Implementation

## Overview

Implemented NFEChave (NF-e access key) validation in `query_apr_invalidos_status.py` to detect and alert on missing NFEChave fields for applicable products.

**Implementation Date**: January 29, 2026  
**Binary Version**: Deployed to `/home/robot/Deploy/slack_integration_alerts/query_apr_invalidos_status`

---

## Features Implemented

### 1. Product Filter Scope

NFEChave validation applies to ALL products EXCEPT the following 16 product types (case-insensitive):

```
CTE BMA FIDC
CTE PRE-IMPRESSO BMA FIDC
INTERCIA NFSE
NF SERV. BMA SEC.
CONTRATO
COB SIMPLES NÃO PG 3ºS
SEM NOTA
CAPITAL DE GIRO NP
RENEGOCIAÇÃO
NOTA COMERCIAL
MEIA NOTA
NF SERV. PRE-IMPR. BMA FIDC
NF SERV. BMA FIDC
CCB
CONVENCIONAL BMA FIDC
CHEQUE
```

### 2. Validation Logic

- **Validation Order**: NFEChave → DUPLICATA → SEUNO (NFEChave is validated FIRST)
- **Validation Check**: Detects if NFEChave field is empty, null, or whitespace-only
- **Cascading Exclusion**: Records with invalid NFEChave are:
  - ❌ Excluded from DUPLICATA validation
  - ❌ Excluded from SEUNO validation
  - ❌ NOT stored in `apr_valid_records` MariaDB table
  - ✅ Exported to separate Excel file
  - ✅ Sent via Slack notification

### 3. Proposal-Level Exclusion

**Critical Feature**: If ANY record in a proposal has invalid NFEChave, DUPLICATA, or SEUNO, the ENTIRE proposal is excluded from the `apr_valid_records` table.

This ensures data integrity by preventing partial proposal storage.

### 4. Alert Message Format

```
{CEDENTE}, {PROPOSTA}, {PRODUTO} sem chave de NF
```

**Example**:
```
COMBRASIL, 53, DUPLICATA sem chave de NF
```

### 5. Duplicate Alert Prevention

- **Tracking Method**: Individual combination tracking
- **Tracking Key**: `(CEDENTE, PROPOSTA, PRODUTO)`
- **Tracking File**: `.tracking_nfechave_YYYY-MM-DD.json` in `nfechave_ausente_tracking/` directory
- **Behavior**: Only sends alerts for NEW combinations that haven't been alerted today

### 6. Excel Export

- **Directory**: `nfechave_ausente/`
- **Filename Pattern**: `nfechave_ausente_YYYY-MM-DD_HHMMSS.xlsx`
- **Columns**: GERENTE, PROPOSTA, DATA, CEDENTE, EMPRESA, STATUS, RAMO, PRODUTO, MOTIVO_INVALIDO
- **MOTIVO_INVALIDO Format**: `"{PRODUTO} sem chave de NF"`

### 7. Slack Notification

- **Alert Type**: "NFECHAVE"
- **Emoji**: ⚠️ (warning)
- **Message Title**: "⚠️ NFEChave Ausente - Chaves de NF Faltando"
- **File Attachment**: Excel file with invalid records
- **Duplicate Prevention**: Applied (only new combinations)

### 8. Processing Order

```
STEP 0: Validate NFEChave (for applicable products)
  ↓
STEP 1: Validate DUPLICATA (exclude NFEChave failures)
  ↓
STEP 2: Validate SEUNO (exclude NFEChave failures)
  ↓
STEP 3: Process invalid NFEChave records (export + alert)
  ↓
STEP 4: Process invalid DUPLICATA records (export + alert)
  ↓
STEP 5: Process invalid SEUNO records (export + alert)
  ↓
STEP 6: Store valid records to MariaDB (exclude all invalid records)
```

---

## Code Changes

### Files Modified

1. **`query_apr_invalidos_status.py`** (lines 245-2126)
   - Added NFEChave validation functions (lines 245-283)
   - Added product exclusion list (lines 794-811)
   - Modified validation logic to check NFEChave first (lines 923-1029)
   - Added Excel export function (lines 1200-1273)
   - Updated tracking file support (lines 1384-1410)
   - Updated Slack notification (lines 1511-1609)
   - Updated cleanup functions (lines 1687-1758)
   - Updated main function with STEP 0 (lines 1956-2016)
   - Added proposal-level exclusion logic (lines 2059-2096)

### Key Functions Added

```python
validate_nfechave(nfechave: str) -> bool
get_invalid_nfechave_reason(produto: str) -> str
export_invalid_nfechave_to_excel(invalid_records, target_date)
```

---

## Testing

### Dry-Run Test Results

```bash
cd /home/robot/Deploy/slack_integration_alerts
./query_apr_invalidos_status --dry-run
```

**Output**:
```
APR INVALID RECORDS CHECKER - NFECHAVE, DUPLICATA & SEUNO with STATUS
✓ No invalid NFEChave records found!
✓ No invalid DUPLICATA records found!
✓ No invalid SEUNO records found!
⚠️  Excluding 0 proposals with invalid records from apr_valid_records table
```

---

## Deployment

**Binary Location**: `/home/robot/Deploy/slack_integration_alerts/query_apr_invalidos_status`  
**Binary Size**: 37M  
**Build Date**: January 29, 2026

### Running the Program

```bash
# Production mode (sends alerts and updates database)
./query_apr_invalidos_status

# Dry-run mode (preview only, no alerts or database updates)
./query_apr_invalidos_status --dry-run
```

---

## Next Steps

1. ✅ Implementation complete
2. ✅ Binary built and deployed
3. ✅ Dry-run testing successful
4. ⏳ Monitor for NFEChave validation alerts in production
5. ⏳ Verify Excel exports and Slack notifications when invalid records are detected

---

## Notes

- The validation is **case-insensitive** for product name matching
- Tracking files are automatically cleaned up (7 days retention)
- Excel files from previous dates are automatically removed
- The program respects the existing status filter (only processes records with status >= "Aguardando Analista")

