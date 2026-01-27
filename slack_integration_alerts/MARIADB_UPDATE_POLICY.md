# MariaDB Update Policy - apr_valid_records Table

## Overview

The `apr_valid_records` table uses an **UPSERT** operation (INSERT ... ON DUPLICATE KEY UPDATE) to store valid APR records. This document defines which fields can and cannot be updated.

---

## Composite Primary Key

The table uses a composite key consisting of:
- `DATA` (Proposal date)
- `PROPOSTA` (Proposal number)
- `CEDENTE` (Client name)
- `RAMO` (Business sector)

---

## Field Update Policy

### ❌ **NEVER UPDATED** (Fixed on Insert Only)

These fields are set once when the record is first inserted and **NEVER** updated:

1. **`DATA`** - Part of composite key (cannot be updated)
2. **`PROPOSTA`** - Part of composite key (cannot be updated)
3. **`CEDENTE`** - Part of composite key (cannot be updated)
4. **`RAMO`** - Part of composite key (cannot be updated)
5. **`GERENTE`** - Manager assigned to proposal (fixed field)
6. **`EMPRESA`** - Company/bank code (fixed field)
7. **`is_processado`** - Processing status flag (managed separately, not reset on update)

**Rationale**: These fields represent the core identity of the proposal and should not change over time. The manager and company are assigned at proposal creation and remain constant. The `is_processado` flag is managed by external processes and should not be reset when other fields are updated.

---

### ✅ **CAN BE UPDATED** (Dynamic Fields)

These fields are updated every time the program runs and finds the same proposal:

1. **`STATUS`** - Current status of the proposal (can change as workflow progresses)
2. **`QTD_APROVADOS`** - Quantity of approved items (can change)
3. **`VLR_APROVADOS`** - Value of approved items (can change)
4. **`VALOR_TITULOS`** - Aggregated value of all valid titles (recalculated)
5. **`QTD_TITULOS`** - Count of valid titles (recalculated)
6. **`update_count`** - Incremented each time the record is updated

**Rationale**: These fields reflect the current state of the proposal and its titles, which can change as the proposal moves through the workflow or as titles are validated.

---

## SQL Implementation

```sql
INSERT INTO apr_valid_records
    (DATA, PROPOSTA, CEDENTE, RAMO, GERENTE, EMPRESA, STATUS,
     QTD_APROVADOS, VLR_APROVADOS, VALOR_TITULOS, QTD_TITULOS, update_count)
VALUES
    (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 0)
ON DUPLICATE KEY UPDATE
    STATUS = VALUES(STATUS),
    QTD_APROVADOS = VALUES(QTD_APROVADOS),
    VLR_APROVADOS = VALUES(VLR_APROVADOS),
    VALOR_TITULOS = VALUES(VALOR_TITULOS),
    QTD_TITULOS = VALUES(QTD_TITULOS),
    update_count = update_count + 1
```

**Note**: The `ON DUPLICATE KEY UPDATE` clause **does NOT include**:
- DATA, PROPOSTA, CEDENTE, RAMO (composite key)
- GERENTE, EMPRESA (fixed fields)

---

## Behavior Examples

### Example 1: New Record (Insert)

**Input**:
- DATA: 2026-01-20
- PROPOSTA: 12345
- CEDENTE: "ACME Corp"
- RAMO: "Varejo"
- GERENTE: "JOE"
- EMPRESA: "001"
- STATUS: "Aguardando Analista"
- QTD_APROVADOS: 10
- VLR_APROVADOS: 50000.00
- VALOR_TITULOS: 45000.00
- QTD_TITULOS: 8

**Result**: Record is inserted with all fields, `update_count = 0`

---

### Example 2: Existing Record (Update)

**Existing Record**:
- DATA: 2026-01-20
- PROPOSTA: 12345
- CEDENTE: "ACME Corp"
- RAMO: "Varejo"
- GERENTE: "JOE"
- EMPRESA: "001"
- STATUS: "Aguardando Analista"
- QTD_APROVADOS: 10
- VLR_APROVADOS: 50000.00
- VALOR_TITULOS: 45000.00
- QTD_TITULOS: 8
- update_count: 0

**New Data** (same proposal, status changed):
- DATA: 2026-01-20
- PROPOSTA: 12345
- CEDENTE: "ACME Corp"
- RAMO: "Varejo"
- GERENTE: "MARY" ← Different manager (will NOT be updated)
- EMPRESA: "002" ← Different company (will NOT be updated)
- STATUS: "Aprovado" ← Changed
- QTD_APROVADOS: 12 ← Changed
- VLR_APROVADOS: 55000.00 ← Changed
- VALOR_TITULOS: 50000.00 ← Changed
- QTD_TITULOS: 10 ← Changed

**Result**: Record is updated with:
- GERENTE: "JOE" (unchanged - still original value)
- EMPRESA: "001" (unchanged - still original value)
- STATUS: "Aprovado" (updated)
- QTD_APROVADOS: 12 (updated)
- VLR_APROVADOS: 55000.00 (updated)
- VALOR_TITULOS: 50000.00 (updated)
- QTD_TITULOS: 10 (updated)
- update_count: 1 (incremented)

---

## Implementation Location

**File**: `slack_integration_alerts/query_apr_invalidos_status.py`  
**Function**: `store_valid_records_to_mariadb()` (Lines 133-240)

---

## Change History

**2026-01-20**: Updated to exclude GERENTE and EMPRESA from ON DUPLICATE KEY UPDATE clause
- Previously: GERENTE and EMPRESA were updated on duplicate key
- Now: GERENTE and EMPRESA are fixed on insert and never updated

