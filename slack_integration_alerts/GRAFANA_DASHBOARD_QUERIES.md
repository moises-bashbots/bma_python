# Grafana Dashboard Queries - APR Proposal Monitoring

**Database:** BMA (MariaDB)  
**Purpose:** Monitor APR proposal flow, status changes, and validation results

---

## 📊 Dashboard Panels

### **Panel 1: Daily Summary - Key Metrics**
**Type:** Stat Panel (4 stats in a row)  
**Time Range:** Last 30 days

```sql
-- Total Proposals Processed Today
SELECT 
    COALESCE(SUM(total_records_queried), 0) as value
FROM apr_daily_summary
WHERE DATA = CURDATE();

-- Valid Proposals Today
SELECT 
    COALESCE(SUM(valid_records), 0) as value
FROM apr_daily_summary
WHERE DATA = CURDATE();

-- Invalid Proposals Today
SELECT 
    COALESCE(SUM(invalid_nfechave + invalid_duplicata + invalid_seuno + invalid_cheque), 0) as value
FROM apr_daily_summary
WHERE DATA = CURDATE();

-- Total Approved Value Today
SELECT 
    COALESCE(SUM(total_vlr_aprovados), 0) as value
FROM apr_daily_summary
WHERE DATA = CURDATE();
```

---

### **Panel 2: Validation Results Trend (Last 30 Days)**
**Type:** Time Series Graph  
**Time Range:** Last 30 days

```sql
SELECT 
    DATA as time,
    valid_records as "Valid",
    invalid_nfechave as "Invalid NFEChave",
    invalid_duplicata as "Invalid DUPLICATA",
    invalid_seuno as "Invalid SEUNO",
    invalid_cheque as "Invalid CHEQUE"
FROM apr_daily_summary
WHERE DATA >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
ORDER BY DATA;
```

---

### **Panel 3: Status Changes by Source (Today)**
**Type:** Pie Chart  
**Time Range:** Today

```sql
SELECT 
    CHANGE_SOURCE as metric,
    COUNT(*) as value
FROM apr_status_history
WHERE DATE(changed_at) = CURDATE()
GROUP BY CHANGE_SOURCE;
```

---

### **Panel 4: Status Changes Timeline (Last 7 Days)**
**Type:** Time Series Graph  
**Time Range:** Last 7 days

```sql
SELECT 
    changed_at as time,
    CHANGE_SOURCE as metric,
    1 as value
FROM apr_status_history
WHERE changed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY changed_at;
```

---

### **Panel 5: Current Invalid Records by Type**
**Type:** Bar Gauge  
**Time Range:** Current

```sql
SELECT 
    VALIDATION_TYPE as metric,
    COUNT(*) as value
FROM apr_invalid_records
WHERE DATA = CURDATE()
AND is_resolved = 0
GROUP BY VALIDATION_TYPE;
```

---

### **Panel 6: Top Cedentes with Invalid Records (Today)**
**Type:** Table  
**Time Range:** Today

```sql
SELECT 
    CEDENTE,
    VALIDATION_TYPE,
    COUNT(*) as "Invalid Count",
    SUM(VLR_APROVADOS) as "Total Value"
FROM apr_invalid_records
WHERE DATA = CURDATE()
AND is_resolved = 0
GROUP BY CEDENTE, VALIDATION_TYPE
ORDER BY COUNT(*) DESC
LIMIT 10;
```

---

### **Panel 7: Status Distribution (Current)**
**Type:** Pie Chart  
**Time Range:** Current

```sql
SELECT 
    NEW_STATUS as metric,
    COUNT(DISTINCT CONCAT(DATA, '-', PROPOSTA, '-', CEDENTE)) as value
FROM apr_status_history
WHERE DATA = CURDATE()
AND changed_at = (
    SELECT MAX(changed_at) 
    FROM apr_status_history h2 
    WHERE h2.DATA = apr_status_history.DATA 
    AND h2.PROPOSTA = apr_status_history.PROPOSTA 
    AND h2.CEDENTE = apr_status_history.CEDENTE
)
GROUP BY NEW_STATUS;
```

---

### **Panel 8: Bot Processing Activity (Last 24 Hours)**
**Type:** Time Series Graph  
**Time Range:** Last 24 hours

```sql
SELECT 
    changed_at as time,
    COUNT(*) as "Proposals Processed"
FROM apr_status_history
WHERE CHANGE_SOURCE = 'BOT'
AND changed_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY DATE_FORMAT(changed_at, '%Y-%m-%d %H:00:00')
ORDER BY time;
```

---

### **Panel 9: Recent Status Changes (Live Table)**
**Type:** Table  
**Time Range:** Last 1 hour  
**Auto-refresh:** 30 seconds

```sql
SELECT 
    changed_at as "Time",
    PROPOSTA as "Proposal",
    CEDENTE as "Client",
    RAMO as "Rating",
    OLD_STATUS as "Old Status",
    NEW_STATUS as "New Status",
    CHANGE_SOURCE as "Source"
FROM apr_status_history
WHERE changed_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY changed_at DESC
LIMIT 50;
```

---

### **Panel 10: Product Type Distribution (Today)**
**Type:** Bar Chart  
**Time Range:** Today

```sql
SELECT 
    PRODUCT_NAME as metric,
    SUM(PRODUCT_COUNT) as value
FROM apr_proposal_products
WHERE DATA = CURDATE()
GROUP BY PRODUCT_NAME
ORDER BY value DESC;
```

---

### **Panel 11: Auto-Resolution Rate (Last 30 Days)**
**Type:** Time Series Graph  
**Time Range:** Last 30 days

```sql
SELECT 
    DATE(resolved_at) as time,
    COUNT(*) as "Auto-Resolved Records"
FROM apr_invalid_records
WHERE is_resolved = 1
AND resolved_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(resolved_at)
ORDER BY time;
```

---

### **Panel 12: Program Execution Log (Last 10 Runs)**
**Type:** Table  
**Time Range:** Last 7 days

```sql
SELECT 
    execution_time as "Time",
    total_records_queried as "Total",
    valid_records as "Valid",
    (invalid_nfechave + invalid_duplicata + invalid_seuno + invalid_cheque) as "Invalid",
    runtime_seconds as "Runtime (s)",
    CASE 
        WHEN error_message IS NULL THEN '✓ Success'
        ELSE CONCAT('✗ ', error_message)
    END as "Status"
FROM apr_processing_log
WHERE DATA = CURDATE()
ORDER BY execution_time DESC
LIMIT 10;
```

---

## 🎨 Dashboard Layout Suggestion

```
Row 1: Key Metrics (4 stat panels)
├─ Total Proposals | Valid | Invalid | Total Value

Row 2: Validation Trends
├─ Validation Results Trend (Last 30 Days) - Full Width

Row 3: Status Tracking
├─ Status Changes by Source (Pie) | Status Distribution (Pie)

Row 4: Timeline
├─ Status Changes Timeline (Last 7 Days) - Full Width

Row 5: Invalid Records
├─ Current Invalid by Type (Bar) | Top Cedentes (Table)

Row 6: Bot Activity
├─ Bot Processing Activity (Last 24h) - Full Width

Row 7: Live Monitoring
├─ Recent Status Changes (Table) - Full Width

Row 8: Additional Metrics
├─ Product Distribution | Auto-Resolution Rate

Row 9: Execution Log
├─ Program Execution Log - Full Width
```

---

## 🔧 Variables (Optional)

Add these dashboard variables for filtering:

```sql
-- Variable: date_filter
SELECT DISTINCT DATA as __text, DATA as __value
FROM apr_daily_summary
ORDER BY DATA DESC;

-- Variable: cedente_filter
SELECT DISTINCT CEDENTE as __text, CEDENTE as __value
FROM apr_status_history
WHERE DATA = CURDATE()
ORDER BY CEDENTE;

-- Variable: status_filter
SELECT DISTINCT NEW_STATUS as __text, NEW_STATUS as __value
FROM apr_status_history
WHERE DATA = CURDATE()
ORDER BY NEW_STATUS;
```

---

## 📝 Next Steps

1. Import these queries into Grafana
2. Configure MariaDB data source (if not done)
3. Create dashboard panels using queries above
4. Set auto-refresh intervals (30s - 5min)
5. Configure alerts for critical metrics

---

**Ready to visualize your APR proposal flow!** 📊

