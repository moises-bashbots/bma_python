# Business Days Integration - Repurchase Report

## Overview

The repurchase report system now uses **business days** instead of calendar days for alert scheduling. This ensures alerts are only sent on actual working days, excluding weekends and Brazilian national holidays.

---

## 🎯 **Key Changes**

### **1. Business Day Validation**
- ✅ Alerts are **ONLY sent on business days**
- ✅ Weekends (Saturday/Sunday) are automatically skipped
- ✅ Brazilian national holidays are automatically skipped
- ✅ Uses the `dias_uteis` table in MariaDB for accurate business day calculation

### **2. Query Logic Update**
- **Old logic:** Query from previous calendar day at 16:15
- **New logic:** Query from previous **BUSINESS day** at 16:15

### **3. Example Scenarios**

#### **Scenario 1: Regular Weekday (Tuesday)**
```
Today: Tuesday, Jan 20, 2026
Previous business day: Monday, Jan 19, 2026
Query cutoff: Monday 16:15:00
Result: ✓ Alerts sent for records created after Monday 16:15
```

#### **Scenario 2: Monday After Weekend**
```
Today: Monday, Jan 26, 2026
Previous business day: Friday, Jan 23, 2026
Query cutoff: Friday 16:15:00
Result: ✓ Alerts sent for records created after Friday 16:15
        (includes records from Friday evening, Saturday, Sunday, Monday morning)
```

#### **Scenario 3: Saturday (Weekend)**
```
Today: Saturday, Jan 24, 2026
Is business day: NO
Result: ✗ Query skipped - no alerts sent
```

#### **Scenario 4: Wednesday After Carnaval**
```
Today: Wednesday, Feb 18, 2026
Previous business day: Friday, Feb 13, 2026
  (skips: Sat Feb 14, Sun Feb 15, Mon Feb 16 [Carnaval], Tue Feb 17 [Carnaval])
Query cutoff: Friday Feb 13, 16:15:00
Result: ✓ Alerts sent for records created after Friday 16:15
        (includes 5 days of records)
```

---

## 🔧 **Technical Implementation**

### **New Functions Added**

#### **1. `create_mariadb_connection()`**
```python
def create_mariadb_connection():
    """Create MariaDB connection for dias_uteis queries."""
    # Returns pymysql connection to MariaDB
```

#### **2. `is_business_day(target_date)`**
```python
def is_business_day(target_date: date) -> bool:
    """
    Check if a date is a business day using dias_uteis table.
    
    Returns:
        True if business day, False otherwise
    """
```

#### **3. `get_previous_business_day(from_date)`**
```python
def get_previous_business_day(from_date: date = None) -> Optional[date]:
    """
    Get the previous business day from dias_uteis table.
    
    Returns:
        Previous business day, or None if not found
    """
```

### **Updated Functions**

#### **1. `query_apr_capa_for_date()`**
```python
# Check if today is a business day
if not is_business_day(target_date):
    print(f"⚠ {target_date} is NOT a business day")
    return []  # Skip query

# Get previous business day
previous_business_day = get_previous_business_day(target_date)

# Calculate cutoff: previous BUSINESS day at 16:15:00
cutoff_datetime = datetime.combine(previous_business_day, time(16, 15, 0))
```

#### **2. `send_repurchase_alerts()`**
```python
# Check if today is a business day
if not is_business_day(today):
    print(f"⚠ Today is NOT a business day")
    return {
        'alerts_skipped_not_business_day': len(matched_records),
        ...
    }
```

---

## 📊 **Database Integration**

### **dias_uteis Table**
- **Location:** MariaDB database (BMA schema)
- **Records:** 18,262 dates (2001-2050)
- **Columns:**
  - `data` - Date (Primary Key)
  - `eh_dia_util` - Is business day? (Boolean)
  - `eh_feriado` - Is holiday? (Boolean)
  - `nome_feriado` - Holiday name
  - `eh_fim_de_semana` - Is weekend? (Boolean)

### **Query Example**
```sql
SELECT eh_dia_util FROM dias_uteis WHERE data = '2026-01-20';
-- Returns: TRUE (Tuesday is a business day)

SELECT eh_dia_util FROM dias_uteis WHERE data = '2026-02-16';
-- Returns: FALSE (Carnaval holiday)
```

---

## ✅ **Benefits**

1. ✅ **Accurate scheduling** - No alerts on holidays
2. ✅ **Automatic handling** - No manual intervention needed
3. ✅ **50 years of data** - Covers 2001-2050
4. ✅ **Brazilian holidays** - All national holidays included
5. ✅ **Weekend handling** - Automatically skips Saturdays and Sundays
6. ✅ **Long weekend support** - Correctly handles extended holiday periods

---

## 🚀 **Usage**

### **Running the Script**

**On a business day:**
```bash
cd slack_integration_alerts
../.venv/bin/python3 repurchase_report.py --dry-run
```

Output:
```
Today: 2026-01-20 (BUSINESS DAY)
Previous business day: 2026-01-19
Querying APR_CAPA for records created after: 2026-01-19 16:15:00
✓ Found 27 records
```

**On a weekend:**
```bash
cd slack_integration_alerts
../.venv/bin/python3 repurchase_report.py --dry-run
```

Output:
```
⚠ 2026-01-25 is NOT a business day (weekend or holiday)
⚠ Skipping query - no alerts will be sent
```

---

## 📝 **Testing**

Use the test script to verify business day logic:

```bash
cd slack_integration_alerts
../.venv/bin/python3 test_business_days.py
```

This will test:
- Regular weekdays
- Weekends
- Holidays (Carnaval, etc.)
- Monday after weekend
- Wednesday after long holiday weekend

---

## 🔄 **Cron Schedule Recommendation**

Since the script now handles business days automatically, you can schedule it to run every day:

```cron
# Run every day at 09:00 and 13:00
0 9 * * * cd /path/to/slack_integration_alerts && ../.venv/bin/python3 repurchase_report.py
0 13 * * * cd /path/to/slack_integration_alerts && ../.venv/bin/python3 repurchase_report.py
```

The script will automatically:
- Skip execution on weekends
- Skip execution on holidays
- Query the correct previous business day
- Send alerts only during business hours (08:00-16:15)

