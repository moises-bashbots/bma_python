# Grafana Dashboard Fixes - Column Name Corrections

**Date**: 2026-01-30  
**Issue**: Dashboard JSON had incorrect column names that didn't match the actual database schema

---

## 🔍 **Problems Found**

### **Problem 1: Non-existent Table Reference**
- **Original Query**: Referenced `apr_valid_records` table for status overview
- **Issue**: This table exists but is separate from the monitoring tables and doesn't have the same structure
- **Fix**: Changed to use `apr_daily_summary` table which has pre-computed status breakdowns

### **Problem 2: Column Name Mismatch**
- **User Report**: "column summary_date is not in the table"
- **Root Cause**: Dashboard JSON used `summary_date` but actual column is `DATA`
- **Fix**: All queries now use correct column names from the actual schema

---

## ✅ **Corrections Made**

### **1. Status Distribution Panel (Panel #1)**
**Before**:
```sql
SELECT STATUS as metric, COUNT(*) as value 
FROM apr_valid_records 
WHERE DATA >= CURDATE() - INTERVAL 7 DAY 
GROUP BY STATUS
```

**After**:
```sql
SELECT 'Aguardando' as metric, proposals_aguardando as value 
FROM apr_daily_summary WHERE DATA = CURDATE() 
UNION ALL SELECT 'Enviado', proposals_enviado 
FROM apr_daily_summary WHERE DATA = CURDATE() 
...
```

**Reason**: Uses the correct `apr_daily_summary` table with actual column names

---

## 📋 **Verified Column Names**

### **Table: `apr_daily_summary`**
✅ `DATA` (not `summary_date`)  
✅ `total_proposals`  
✅ `valid_proposals`  
✅ `invalid_proposals`  
✅ `total_vlr_aprovados`  
✅ `total_valor_titulos`  
✅ `proposals_aguardando`  
✅ `proposals_enviado`  
✅ `proposals_assinado`  
✅ `proposals_liberado`  
✅ `proposals_finalizado`  
✅ `last_updated`  

### **Table: `apr_invalid_records`**
✅ `DATA`  
✅ `PROPOSTA`  
✅ `CEDENTE`  
✅ `STATUS`  
✅ `PRODUTO`  
✅ `VALIDATION_TYPE`  
✅ `MOTIVO`  
✅ `detected_at`  
✅ `alerted_at`  
✅ `is_resolved`  
✅ `resolved_at`  

### **Table: `apr_status_history`**
✅ `DATA`  
✅ `PROPOSTA`  
✅ `CEDENTE`  
✅ `OLD_STATUS`  
✅ `NEW_STATUS`  
✅ `OLD_VLR_APROVADOS`  
✅ `NEW_VLR_APROVADOS`  
✅ `changed_at`  

### **Table: `apr_processing_log`**
✅ `run_timestamp`  
✅ `target_date`  
✅ `total_records_queried`  
✅ `valid_records`  
✅ `invalid_nfechave`  
✅ `invalid_duplicata`  
✅ `invalid_seuno`  
✅ `execution_time_seconds`  

### **Table: `apr_proposal_products`**
✅ `DATA`  
✅ `PROPOSTA`  
✅ `CEDENTE`  
✅ `PRODUTO`  
✅ `QTD_TITULOS`  
✅ `VALOR_TITULOS`  
✅ `created_at`  
✅ `updated_at`  

---

## 🎯 **Dashboard Panels (12 Total)**

All panels now use correct column names:

1. **Status Distribution (Today)** - Uses `apr_daily_summary.DATA`
2. **Invalid Records by Type** - Uses `apr_invalid_records.VALIDATION_TYPE`
3. **Invalid Records by Product** - Joins `apr_invalid_records` + `apr_proposal_products`
4. **Processing Trend** - Uses `apr_daily_summary.DATA`
5. **Alert Volume by Type** - Uses `apr_invalid_records.detected_at`
6. **Average Resolution Time** - Uses `apr_invalid_records.resolved_at`
7. **Total Active Invalid Records** - Uses `apr_invalid_records.is_resolved`
8. **Today's Processing Stats** - Uses `apr_daily_summary.DATA`
9. **Avg Execution Time** - Uses `apr_processing_log.execution_time_seconds`
10. **Recent Status Changes** - Uses `apr_status_history.changed_at`
11. **Top 10 Cedentes** - Uses `apr_invalid_records.CEDENTE`
12. **Product Distribution** - Uses `apr_proposal_products.PRODUTO`

---

## 🚀 **Next Steps**

1. **Delete the old dashboard** in Grafana (BMA Propostas)
2. **Re-import** the corrected JSON file:
   - Go to Dashboards → Import
   - Upload: `grafana_dashboard_apr_monitoring.json`
   - Select data source: BMA MariaDB
3. **Test each panel** to ensure data displays correctly

---

## ✅ **Verification**

All SQL queries have been verified against the actual table schemas in:
- `create_monitoring_tables.sql` (lines 1-187)
- All column names match exactly
- All JOINs use correct composite keys (DATA, PROPOSTA, CEDENTE)
- All date filters use correct column names

---

**Status**: ✅ **FIXED** - Dashboard JSON now matches actual database schema

