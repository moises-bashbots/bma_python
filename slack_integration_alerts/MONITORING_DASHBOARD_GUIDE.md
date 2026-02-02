# APR Monitoring Dashboard Guide

## Overview

This guide explains the monitoring database schema designed for tracking APR proposal processing through Grafana dashboards.

**Created**: 2026-01-30  
**Retention Policy**: 30 days  
**Auto-Resolution**: Enabled (invalid records automatically marked as resolved when they appear in valid records)

---

## Database Tables

### 1. `apr_invalid_records` - Invalid Records Tracking

Stores all detected validation failures (NFEChave, DUPLICATA, SEUNO).

**Key Features**:
- Tracks when invalid records are first detected
- Records when Slack alerts are sent
- Auto-resolves when records become valid
- Supports filtering by validation type

**Primary Use Cases**:
- Show unresolved validation issues
- Track resolution time
- Identify recurring problems by cedente/product

**Sample Queries**:

```sql
-- Unresolved invalid records
SELECT VALIDATION_TYPE, COUNT(*) as count
FROM apr_invalid_records
WHERE is_resolved = 0
GROUP BY VALIDATION_TYPE;

-- Average resolution time
SELECT 
    VALIDATION_TYPE,
    AVG(TIMESTAMPDIFF(HOUR, detected_at, resolved_at)) as avg_hours_to_resolve
FROM apr_invalid_records
WHERE is_resolved = 1
GROUP BY VALIDATION_TYPE;

-- Top cedentes with validation issues
SELECT CEDENTE, COUNT(*) as issue_count
FROM apr_invalid_records
WHERE is_resolved = 0
GROUP BY CEDENTE
ORDER BY issue_count DESC
LIMIT 10;
```

---

### 2. `apr_status_history` - Status Change Tracking

Captures every status transition for proposals.

**Key Features**:
- Tracks old and new status values
- Records value changes (VLR_APROVADOS, QTD_TITULOS)
- Identifies change source (SYSTEM or BOT)
- Enables timeline visualization

**Primary Use Cases**:
- Show proposal journey through workflow
- Calculate average time per status
- Identify bottlenecks
- Track value changes over time

**Sample Queries**:

```sql
-- Average time in each status
SELECT 
    NEW_STATUS,
    AVG(TIMESTAMPDIFF(HOUR, 
        LAG(changed_at) OVER (PARTITION BY PROPOSTA ORDER BY changed_at),
        changed_at
    )) as avg_hours_in_status
FROM apr_status_history
GROUP BY NEW_STATUS;

-- Recent status changes
SELECT 
    PROPOSTA, CEDENTE, OLD_STATUS, NEW_STATUS, changed_at
FROM apr_status_history
WHERE changed_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY changed_at DESC;

-- Proposals stuck in status for > 48 hours
SELECT 
    h.PROPOSTA, h.CEDENTE, h.NEW_STATUS,
    TIMESTAMPDIFF(HOUR, h.changed_at, NOW()) as hours_in_status
FROM apr_status_history h
WHERE h.id IN (
    SELECT MAX(id) FROM apr_status_history GROUP BY PROPOSTA
)
AND TIMESTAMPDIFF(HOUR, h.changed_at, NOW()) > 48;
```

---

### 3. `apr_processing_log` - Program Execution Log

Logs each run of the validation program.

**Key Features**:
- Tracks processing statistics
- Records alert counts
- Monitors execution time
- Captures errors

**Primary Use Cases**:
- Monitor program health
- Detect anomalies in processing
- Track alert volumes
- Performance monitoring

**Sample Queries**:

```sql
-- Daily processing summary
SELECT 
    target_date,
    total_records_queried,
    valid_records,
    invalid_nfechave + invalid_duplicata + invalid_seuno as total_invalid,
    execution_time_seconds
FROM apr_processing_log
WHERE target_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
ORDER BY target_date DESC;

-- Alert volume trends
SELECT 
    DATE(run_timestamp) as date,
    SUM(alerts_sent_nfechave) as nfechave_alerts,
    SUM(alerts_sent_duplicata) as duplicata_alerts,
    SUM(alerts_sent_seuno) as seuno_alerts
FROM apr_processing_log
WHERE run_timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(run_timestamp)
ORDER BY date DESC;

-- Failed runs
SELECT run_timestamp, target_date, error_message
FROM apr_processing_log
WHERE error_message IS NOT NULL
ORDER BY run_timestamp DESC;
```

---

### 4. `apr_proposal_products` - Product Type Tracking

Tracks all product types for each proposal (many-to-many relationship).

**Key Features**:
- One row per proposal-product combination
- Supports proposals with multiple products
- Tracks quantity and value per product
- Enables product-based filtering and analysis

**Primary Use Cases**:
- Show which products are in each proposal
- Filter dashboards by product type
- Analyze validation issues by product
- Track product mix trends

**Sample Queries**:

```sql
-- Products for a specific proposal
SELECT PRODUTO, QTD_TITULOS, VALOR_TITULOS
FROM apr_proposal_products
WHERE DATA = '2026-01-30' AND PROPOSTA = 123
ORDER BY PRODUTO;

-- Most common products
SELECT PRODUTO, COUNT(DISTINCT PROPOSTA) as proposal_count
FROM apr_proposal_products
WHERE DATA >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY PRODUTO
ORDER BY proposal_count DESC;

-- Proposals with multiple products
SELECT DATA, PROPOSTA, CEDENTE, COUNT(*) as product_count
FROM apr_proposal_products
GROUP BY DATA, PROPOSTA, CEDENTE
HAVING COUNT(*) > 1
ORDER BY product_count DESC;

-- Invalid records by product type
SELECT
    p.PRODUTO,
    COUNT(DISTINCT CONCAT(i.DATA, '-', i.PROPOSTA)) as invalid_proposals
FROM apr_invalid_records i
JOIN apr_proposal_products p
    ON i.DATA = p.DATA
    AND i.PROPOSTA = p.PROPOSTA
    AND i.CEDENTE = p.CEDENTE
WHERE i.is_resolved = 0
GROUP BY p.PRODUTO
ORDER BY invalid_proposals DESC;
```

---

### 5. `apr_daily_summary` - Daily Aggregated Metrics

Pre-computed daily statistics for fast dashboard queries.

**Key Features**:
- One row per date
- Pre-aggregated counts and values
- Status breakdown
- Fast queries for dashboards

**Primary Use Cases**:
- Daily KPI dashboard
- Trend analysis
- Executive summary
- Quick overview

**Sample Queries**:

```sql
-- Last 30 days trend
SELECT 
    DATA,
    total_proposals,
    valid_proposals,
    invalid_proposals,
    total_vlr_aprovados
FROM apr_daily_summary
WHERE DATA >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
ORDER BY DATA DESC;

-- Status distribution today
SELECT 
    proposals_aguardando,
    proposals_enviado,
    proposals_assinado,
    proposals_liberado,
    proposals_finalizado
FROM apr_daily_summary
WHERE DATA = CURDATE();
```

---

## Grafana Dashboard Panels

### Recommended Panels

1. **Current Status Overview** (Pie Chart)
   - Query: `apr_daily_summary` for today
   - Shows: Distribution across statuses

2. **Invalid Records by Type** (Bar Chart)
   - Query: `apr_invalid_records` WHERE `is_resolved = 0`
   - Shows: Count by VALIDATION_TYPE

3. **Processing Trend** (Line Chart)
   - Query: `apr_processing_log` last 30 days
   - Shows: valid_records, total_invalid over time

4. **Alert Volume** (Stacked Area Chart)
   - Query: `apr_processing_log` alerts by type
   - Shows: Daily alert counts

5. **Average Resolution Time** (Stat Panel)
   - Query: `apr_invalid_records` with TIMESTAMPDIFF
   - Shows: Hours to resolve by type

6. **Recent Status Changes** (Table)
   - Query: `apr_status_history` last 24 hours
   - Shows: Proposal, Cedente, Old/New Status

7. **Top Issues by Cedente** (Bar Chart)
   - Query: `apr_invalid_records` GROUP BY CEDENTE
   - Shows: Cedentes with most unresolved issues

8. **Execution Performance** (Line Chart)
   - Query: `apr_processing_log` execution_time_seconds
   - Shows: Program performance over time

9. **Product Distribution** (Pie Chart)
   - Query: `apr_proposal_products` GROUP BY PRODUTO
   - Shows: Most common product types

10. **Invalid Records by Product** (Bar Chart)
    - Query: JOIN `apr_invalid_records` with `apr_proposal_products`
    - Shows: Which products have most validation issues

---

## Data Flow

```
query_apr_invalidos_status.py runs
    ↓
Validates records (NFEChave, DUPLICATA, SEUNO)
    ↓
Logs invalid records → apr_invalid_records
    ↓
Stores valid records → apr_valid_records
    ↓
Tracks product types → apr_proposal_products
    ↓
Tracks status changes → apr_status_history
    ↓
Logs execution stats → apr_processing_log
    ↓
Updates daily summary → apr_daily_summary
    ↓
Auto-resolves invalid records (if now valid)
    ↓
Cleans up data > 30 days old
```

---

## Next Steps

1. ✅ Tables created
2. ⏳ Integrate monitoring functions into `query_apr_invalidos_status.py`
3. ⏳ Test with today's data
4. ⏳ Create Grafana dashboard
5. ⏳ Set up alerts in Grafana

---

## Files Created

- `create_monitoring_tables.sql` - Table definitions
- `create_monitoring_tables.py` - Table creation script
- `models_mariadb.py` - Updated with 4 new ORM models
- `monitoring_helpers.py` - Helper functions for logging/tracking
- `MONITORING_DASHBOARD_GUIDE.md` - This file

