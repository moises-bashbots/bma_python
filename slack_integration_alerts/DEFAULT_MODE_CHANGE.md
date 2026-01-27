# Default Mode Change - query_apr_invalidos_status.py

## Summary

Changed the default behavior from **dry-run mode** to **production mode**.

---

## What Changed

### Before (Dry-run Default)
```bash
# Default behavior - no alerts sent
./query_apr_invalidos_status

# Explicit production mode
./query_apr_invalidos_status --send
```

### After (Production Default)
```bash
# Default behavior - sends alerts and updates database
./query_apr_invalidos_status

# Explicit dry-run mode
./query_apr_invalidos_status --dry-run
```

---

## Rationale

The program is now mature and stable enough to run in production mode by default:
- ✅ Status filter prevents processing of incomplete records
- ✅ MariaDB update policy protects fixed fields
- ✅ Comprehensive validation logic tested
- ✅ Error handling in place
- ✅ File cleanup automated

**Dry-run mode is still available** for testing or preview purposes using the `--dry-run` flag.

---

## Code Changes

### File: `query_apr_invalidos_status.py`

**Line 1208** (Main function):
```python
# Before
dry_run = '--dry-run' in sys.argv or len(sys.argv) == 1  # Default to dry-run

# After
dry_run = '--dry-run' in sys.argv  # Only dry-run if explicitly requested
```

**Lines 1210-1217** (Mode indicator):
```python
# Added production mode indicator
if dry_run:
    print("🔍 DRY RUN MODE - No Slack notifications will be sent, no database updates")
else:
    print("🚀 PRODUCTION MODE - Slack notifications will be sent, database will be updated")
```

**Line 19** (Docstring):
```python
# Before
- Dry-run mode by default (use --send to actually send alerts)

# After
- Production mode by default (use --dry-run to preview without sending alerts)
```

---

## Behavior Comparison

| Feature | Production Mode (Default) | Dry-run Mode (--dry-run) |
|---------|---------------------------|--------------------------|
| Query database | ✅ | ✅ |
| Validate DUPLICATA | ✅ | ✅ |
| Validate SEUNO | ✅ | ✅ |
| Apply status filter | ✅ | ✅ |
| Create Excel files | ✅ | ✅ |
| **Send Slack alerts** | **✅** | **❌** |
| **Update MariaDB** | **✅** | **❌** |
| Console output | ✅ | ✅ |

---

## Impact

### For Automated Execution (Cron)

**Before**:
```bash
0 9 * * 1-5 cd /home/robot/Deploy/slack_integration_alerts && ./query_apr_invalidos_status --send
```

**After** (simpler):
```bash
0 9 * * 1-5 cd /home/robot/Deploy/slack_integration_alerts && ./query_apr_invalidos_status
```

### For Manual Testing

**Before**: Run without arguments to preview (safe default)

**After**: Use `--dry-run` flag to preview
```bash
./query_apr_invalidos_status --dry-run
```

---

## Documentation Updated

The following files were updated to reflect the new default behavior:

1. ✅ `query_apr_invalidos_status.py` - Main program
2. ✅ `PROGRAM_SUMMARY.md` - Program overview
3. ✅ `DEPLOYMENT_GUIDE.md` - Deployment instructions
4. ✅ `README_APR_INVALIDOS_DEPLOYMENT.md` - User guide
5. ✅ `build_query_apr_invalidos_status.sh` - Build script

---

## Migration Guide

### For Existing Cron Jobs

If you have existing cron jobs using `--send`:
```bash
# Old (still works)
./query_apr_invalidos_status --send

# New (recommended - simpler)
./query_apr_invalidos_status
```

Both commands now do the same thing (production mode).

### For Testing/Preview

If you want to test without sending alerts:
```bash
# Use the new --dry-run flag
./query_apr_invalidos_status --dry-run
```

---

## Backward Compatibility

⚠️ **Breaking Change**: Running the program without arguments now sends alerts and updates the database.

**Migration Required**: If you have scripts or cron jobs that rely on the old dry-run default behavior, you must add the `--dry-run` flag.

---

## Safety Considerations

The program includes multiple safety features:
- Status filter (only processes records >= "Aguardando Analista")
- MariaDB upsert (won't corrupt existing data)
- Fixed field protection (GERENTE, EMPRESA never updated)
- File cleanup (automatic removal of old files)
- Error handling (graceful degradation)

These features make production mode safe to use as the default.

---

## Date

**2026-01-20**: Changed default mode from dry-run to production

