# Status Filter Update - query_apr_invalidos_status.py

## Summary

Added status filtering to only process records with status equal to or further than **"Aguardando Analista"** in the workflow progression.

## Changes Made

### 1. Status Progression Definition (Lines 428-491)

Added a new section with:
- **STATUS_PROGRESSION**: Ordered list of status values from earliest to latest in the workflow
- **is_status_equal_or_further()**: Function to check if a status meets the minimum requirement

```python
STATUS_PROGRESSION = [
    "Aguardando Analista",
    "Aguardando análise",
    "Em análise",
    "Aguardando Aprovação",
    "Aprovado",
    "Enviado para Assinar",
    "Assinado",
    "Liberado",
    "Finalizado"
]
```

### 2. Main Query Function Update (Lines 498-524)

Modified `query_apr_invalidos_with_status()`:
- Added `apply_status_filter` parameter (default: True)
- Added status filter check in the record processing loop (Line 599-601)
- Records with status < "Aguardando Analista" are skipped

### 3. Valid Records Query Update (Lines 727-762)

Modified `query_valid_records_with_status_filter()`:
- Replaced the commented-out status filter with active status progression check
- Added counter for skipped records due to status filter
- Prints number of records skipped by status filter

### 4. Summary Display Update (Lines 1026-1037)

Modified `print_summary()`:
- Updated title to indicate status filter is active
- Added note about excluded records

### 5. Main Function Update (Lines 1196-1219)

Modified `main()`:
- Added status filter information to header
- Passes `apply_status_filter=True` to query function

## Behavior

### Records Included
✅ Status = "Aguardando Analista" or any status further in the progression
✅ Case-insensitive matching
✅ Handles whitespace variations

### Records Excluded
❌ Empty or null status
❌ Status before "Aguardando Analista" (e.g., "Rascunho", "Pendente")
❌ Unknown statuses not in the progression list

## Testing

Created `test_status_filter.py` to verify the status filter logic:
- ✅ All 12 test cases passed
- Tests exact matches, variations, case sensitivity, and edge cases

## Impact

### Invalid Records Processing
- Only invalid records with status >= "Aguardando Analista" will be:
  - Exported to Excel
  - Sent as Slack alerts
  - Displayed in console output

### Valid Records Processing
- Only valid records with status >= "Aguardando Analista" will be:
  - Stored in MariaDB
  - Displayed in console output
  - Included in statistics

### Console Output
- Summary shows total records processed (after status filter)
- Displays count of records skipped by status filter
- Clear indication that filter is active

## Consistency with Other Programs

This implementation follows the same pattern as:
- `bma_send_whatsapp/query_apr_capa_status.py` - Filters for "Aguardando Analista" and "Enviado para Assinar"
- Similar status-based filtering used across the BMA system

## Files Modified

1. `slack_integration_alerts/query_apr_invalidos_status.py` - Main program
2. `slack_integration_alerts/test_status_filter.py` - Test script (new)
3. `slack_integration_alerts/STATUS_FILTER_UPDATE.md` - This documentation (new)

