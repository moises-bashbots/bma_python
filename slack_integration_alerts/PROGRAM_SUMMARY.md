# query_apr_invalidos_status.py - Complete Program Summary

## Overview

A unified validation and alerting system for APR (Aprovação) records that checks for invalid DUPLICATA and SEUNO fields, applies status filtering, and sends alerts via Slack while storing valid records in MariaDB.

---

## Core Functionality

### 1. DUPLICATA Validation
**Format Rule**: `NFE + separator (- or /) + sequential number`

**Examples**:
- ✅ `021467-1`, `21467-2` (for NFE=21467)
- ✅ `140917/01`, `140917/02` (for NFE=140917)
- ❌ `ABC-123` (invalid format)

**Process**:
- Extracts NFE from NFEChave (positions 26-34, 9 digits)
- Validates DUPLICATA matches NFE pattern
- Provides Portuguese error messages

### 2. SEUNO Validation
**Verification Digit Algorithm**:
1. Form vector: `[1, 9] + first 11 digits of SEUNO`
2. Factor vector: `[2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2]`
3. Calculate dot product and modulo 11
4. Determine verification digit (0, P, or 1-9)

**Rules**:
- Must start with RANGE value from cedente's pre-impresso configuration
- Position 12 must contain valid verification digit
- Calculated digit must match actual digit

**Pre-impresso Parsing**:
- Reads cedente's `obs1` field
- Extracts company/range pairs (e.g., "SANTANDER: 5183, 5184")
- Matches range to empresa field

### 3. Status Filter ⭐ NEW
**Progression Order**:
1. Aguardando Analista ← **Minimum required**
2. Aguardando análise
3. Em análise
4. Aguardando Aprovação
5. Aprovado
6. Enviado para Assinar
7. Assinado
8. Liberado
9. Finalizado

**Behavior**:
- ✅ Only processes records with status >= "Aguardando Analista"
- ✅ Case-insensitive matching
- ✅ Handles whitespace variations
- ❌ Excludes records with earlier statuses (e.g., "Rascunho", "Pendente")

---

## Data Flow

### Input Sources
1. **APR_CAPA** (MSSQL) - Proposal header data
   - Fields: GERENTE, NUMERO, DATA, CEDENTE, empresa, status_atual, QTD_APROVADOS, VLR_APROVADOS
2. **APR_TITULOS** (MSSQL) - Title/invoice data
   - Fields: SEUNO, TITULO (DUPLICATA), NFEChave, VALOR
3. **CEDENTE** (MSSQL) - Client data
   - Fields: apelido, ramo, obs1 (pre-impresso ranges)

### Processing Steps
1. **Query** - Join tables, filter by date and NFEChave
2. **Status Filter** - Apply "Aguardando Analista" minimum requirement
3. **Validate** - Check DUPLICATA and SEUNO formats
4. **Separate** - Split into invalid DUPLICATA, invalid SEUNO, and valid records
5. **Export** - Create Excel files for invalid records
6. **Alert** - Send Slack notifications with attachments
7. **Store** - Save valid records to MariaDB

### Output Destinations
1. **Excel Files**:
   - `duplicatas_invalidas/duplicatas_invalidas_YYYY-MM-DD_HHMMSS.xlsx`
   - `seuno_invalidos/seuno_invalidos_YYYY-MM-DD_HHMMSS.xlsx`

2. **Slack Notifications**:
   - Channel: Configured in `slack_config.json`
   - Attachments: Excel files with invalid records
   - Emojis: 🚨 (DUPLICATA), ⚠️ (SEUNO)

3. **MariaDB Storage**:
   - Table: `apr_valid_records`
   - Composite Key: (DATA, PROPOSTA, CEDENTE, RAMO)
   - Aggregates: VALOR_TITULOS, QTD_TITULOS per proposal
   - Upsert: Inserts new or updates existing records
   - **Fixed Fields** (never updated): DATA, PROPOSTA, CEDENTE, RAMO, GERENTE, EMPRESA
   - **Dynamic Fields** (updated): STATUS, QTD_APROVADOS, VLR_APROVADOS, VALOR_TITULOS, QTD_TITULOS, update_count

4. **Console Output**:
   - Validation summary with counts
   - Invalid records table (first 50)
   - Valid records table (first 100) with totals
   - Status breakdown with value aggregations

---

## Key Features

✅ **Dual Validation** - Both DUPLICATA and SEUNO in one program
✅ **Status Filtering** - Only processes records >= "Aguardando Analista"
✅ **STATUS Tracking** - Includes APR_CAPA status column
✅ **RAMO Field** - Added from CEDENTE table
✅ **Production Mode** - Default mode sends alerts and updates database, use `--dry-run` for preview
✅ **Comprehensive Reporting** - Console, Excel, and Slack outputs
✅ **Data Persistence** - Valid records stored in MariaDB with update tracking
✅ **Error Handling** - Portuguese error messages, graceful degradation
✅ **Weekend-aware** - Auto-adjusts to business days
✅ **File Cleanup** - Removes old Excel files automatically

---

## Usage

### Production Mode (Default)
```bash
python3 query_apr_invalidos_status.py
```
- ✅ Creates Excel files
- ✅ Sends Slack notifications
- ✅ Updates MariaDB

### Dry-run Mode (Preview Only)
```bash
python3 query_apr_invalidos_status.py --dry-run
```
- ✅ Creates Excel files
- ❌ Does NOT send Slack notifications
- ❌ Does NOT update MariaDB

---

## Configuration Files

1. **databases_config.json** - Database connections (MSSQL + MariaDB)
2. **slack_config.json** - Slack webhook URL and channel settings

---

## Statistics Tracked

### Invalid Records
- Total invalid count
- DUPLICATA invalid count
- SEUNO invalid count
- Both invalid count
- Breakdown by error reason

### Valid Records
- Total valid count
- Records excluded by status filter
- Breakdown by STATUS
- Total values (QTD_APROVADOS, VLR_APROVADOS, VALOR_TITULOS)

### MariaDB Operations
- Records inserted (new)
- Records updated (existing)
- Total processed

---

## Testing

Run the status filter test:
```bash
python3 test_status_filter.py
```

Expected: All 12 test cases pass ✅

---

## Recent Updates

**2026-01-20**:
1. Added status filter to only process records with status >= "Aguardando Analista"
   - See `STATUS_FILTER_UPDATE.md` for detailed changes
2. Updated MariaDB upsert policy to protect fixed fields (GERENTE, EMPRESA)
   - See `MARIADB_UPDATE_POLICY.md` for detailed policy
3. Changed default behavior to production mode (sends alerts and updates database)
   - Use `--dry-run` flag to preview without sending alerts

