# Comprehensive Status Tracking - Implementation Plan

**Date:** 2026-02-01  
**Objective:** Track ALL status changes from ALL sources in the monitoring database

---

## 🎯 Current Situation

### Status Change Sources:

1. **MSSQL Source Database (APR_CAPA.STATUS_ATUAL)**
   - Manual changes by users in GER system
   - Automatic status changes by GER workflows
   - Changes from other integrated systems

2. **Validation Program (query_apr_invalidos_status.py)**
   - Detects status changes when querying MSSQL
   - Currently tracks changes with `CHANGE_SOURCE='SYSTEM'`
   - Only tracks changes for records that pass validation

3. **Bot Program (send_rating_vadu.py)**
   - Clicks "Processar" button in GER
   - Sets `is_bot_processed=1` in MariaDB
   - Should track status change with `CHANGE_SOURCE='BOT'`

4. **Invalid → Valid Transitions**
   - When invalid proposals get corrected
   - Currently auto-resolved but status change not tracked

---

## ❌ Current Problems

1. **Incomplete Tracking:**
   - Only tracks status changes for records processed during validation run
   - Doesn't track changes that happen between runs
   - Doesn't track status changes for invalid proposals

2. **Missing Bot Status Tracking:**
   - Bot sets `is_bot_processed=1` but doesn't log status change
   - Can't see in history when bot processed a proposal

3. **No Invalid → Valid Tracking:**
   - When invalid proposal becomes valid, we mark it resolved
   - But we don't track the status change in history

4. **Limited Change Detection:**
   - Only compares with last history entry
   - Doesn't query MSSQL to detect all changes

---

## ✅ Proposed Solution

### 1. **Enhanced Status Change Tracking Function**

Create `track_all_status_changes()` that:
- Queries **ALL** proposals from MSSQL for target date
- Compares with **ALL** records in MariaDB (both valid and invalid)
- Detects status changes from **ANY** source
- Logs changes with appropriate `CHANGE_SOURCE`

### 2. **Bot Integration**

Update `send_rating_vadu.py` to:
- Call MariaDB monitoring function after clicking "Processar"
- Log status change with `CHANGE_SOURCE='BOT'`
- Include timestamp of bot action

### 3. **Invalid → Valid Transition Tracking**

Update `auto_resolve_invalid_records()` to:
- Query current status from MSSQL when resolving
- Log status change in history
- Mark with `CHANGE_SOURCE='AUTO_RESOLVE'`

### 4. **Scheduled Monitoring**

Create new program `monitor_status_changes.py` that:
- Runs every hour (or configurable interval)
- Queries MSSQL for all proposals
- Compares with MariaDB
- Logs ALL detected changes
- Can run independently of validation program

---

## 📋 Implementation Steps

### Step 1: Create Enhanced Tracking Function ✅
```python
def track_all_status_changes_from_source(
    mssql_session: Session,
    mariadb_session: Session,
    target_date: date,
    change_source: str = 'SYSTEM'
) -> int:
    """
    Track ALL status changes by querying MSSQL source and comparing with MariaDB.
    
    This function:
    1. Queries ALL proposals from MSSQL APR_CAPA for target date
    2. Compares with apr_valid_records AND apr_invalid_records in MariaDB
    3. Detects status changes from ANY source
    4. Logs changes to apr_status_history
    
    Returns:
        Number of status changes tracked
    """
```

### Step 2: Update Validation Program ✅
- Replace `track_status_changes()` with `track_all_status_changes_from_source()`
- Pass both MSSQL and MariaDB sessions
- Use `CHANGE_SOURCE='SYSTEM'`

### Step 3: Update Bot Program ✅
- Import monitoring helpers
- After clicking "Processar", call tracking function
- Use `CHANGE_SOURCE='BOT'`
- Log the exact status after bot processing

### Step 4: Update Auto-Resolution ✅
- Enhance `auto_resolve_invalid_records()` to track status
- Query MSSQL for current status
- Log status change with `CHANGE_SOURCE='AUTO_RESOLVE'`

### Step 5: Create Standalone Monitor (Optional) ⏳
- New program that runs independently
- Scheduled via cron (hourly)
- Catches changes between validation runs
- Useful for real-time monitoring

---

## 🔧 Database Schema Changes

### apr_status_history - Add New CHANGE_SOURCE Values

Current: `ENUM('SYSTEM', 'BOT')`  
Proposed: `ENUM('SYSTEM', 'BOT', 'AUTO_RESOLVE', 'MONITOR')`

- **SYSTEM**: Changes detected by validation program
- **BOT**: Changes made by bot clicking "Processar"
- **AUTO_RESOLVE**: Invalid → Valid transitions
- **MONITOR**: Changes detected by standalone monitor

---

## 📊 Expected Benefits

1. **Complete Audit Trail:**
   - Every status change tracked with timestamp
   - Know exactly when and how status changed
   - Can trace proposal lifecycle

2. **Bot Activity Visibility:**
   - See when bot processed each proposal
   - Distinguish bot actions from manual changes
   - Track bot performance

3. **Invalid → Valid Tracking:**
   - Know when proposals were corrected
   - Track resolution time
   - Measure correction efficiency

4. **Real-Time Monitoring:**
   - Detect changes between validation runs
   - Alert on unexpected status changes
   - Better Grafana dashboards

---

## 🎯 Next Steps

1. ✅ Review and approve this plan
2. ⏳ Implement enhanced tracking function
3. ⏳ Update validation program
4. ⏳ Update bot program
5. ⏳ Test complete flow
6. ⏳ Deploy to production
7. ⏳ (Optional) Create standalone monitor

---

**Status:** Awaiting approval to proceed with implementation

