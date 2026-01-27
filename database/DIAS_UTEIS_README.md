# dias_uteis Table - Business Days Calendar

## Overview

The `dias_uteis` table provides a comprehensive business days calendar for Brazil, including:
- All dates from 2001 to 2050
- Brazilian national holidays
- Weekend identification
- Business day calculation

---

## 📊 Table Structure

### **Table: `dias_uteis`**

| Column | Type | Description |
|--------|------|-------------|
| **data** | DATE | Date (Primary Key) |
| **dia_semana** | VARCHAR(20) | Day of week in Portuguese |
| **eh_feriado** | BOOLEAN | Is it a national holiday? |
| **nome_feriado** | VARCHAR(100) | Holiday name (if applicable) |
| **eh_fim_de_semana** | BOOLEAN | Is it a weekend (Saturday/Sunday)? |
| **eh_dia_util** | BOOLEAN | Is it a business day? (not holiday and not weekend) |

### **Indexes:**
- Primary Key: `data`
- Index: `idx_eh_dia_util` (for fast business day queries)
- Index: `idx_data` (for date range queries)

---

## 📈 Statistics

- **Total records:** 18,262 (50 years of data)
- **Business days:** 12,541 (68.7%)
- **Holidays:** 627 (3.4%)
- **Weekends:** 5,217 (28.6%)

---

## 🎯 Data Source

**File:** `feriados_nacionais.xls`
- Contains 1,263 Brazilian national holidays
- Covers years 2001-2050
- Includes:
  - Confraternização Universal (New Year)
  - Carnaval (Monday and Tuesday)
  - Paixão de Cristo (Good Friday)
  - Tiradentes (April 21)
  - Dia do Trabalho (May 1)
  - Corpus Christi
  - Independência do Brasil (September 7)
  - Nossa Sr.a Aparecida (October 12)
  - Finados (November 2)
  - Proclamação da República (November 15)
  - Natal (December 25)

---

## 🔧 Setup

### **1. Create the Table**

```bash
cd database
../.venv/bin/python3 create_dias_uteis_table.py
```

This will:
1. Read holidays from `feriados_nacionais.xls`
2. Create the `dias_uteis` table in MariaDB
3. Populate with 18,262 records (2001-2050)
4. Display statistics

---

## 💻 Usage

### **Python Helper Module**

Use the `business_days_helper.py` module for easy access:

```python
from business_days_helper import *
from datetime import date

# Check if today is a business day
is_biz = is_business_day(date.today())
print(f"Is today a business day? {is_biz}")

# Get next business day
next_biz = get_next_business_day()
print(f"Next business day: {next_biz}")

# Get previous business day
prev_biz = get_previous_business_day()
print(f"Previous business day: {prev_biz}")

# Check if date is a holiday
is_hol, hol_name = is_holiday(date(2026, 2, 17))
print(f"Is holiday? {is_hol} - {hol_name}")

# Get upcoming holidays
holidays = get_upcoming_holidays(limit=5)
for h in holidays:
    print(f"{h['data']} - {h['nome_feriado']}")
```

### **Direct SQL Queries**

#### **Check if a date is a business day:**
```sql
SELECT eh_dia_util FROM dias_uteis WHERE data = '2026-01-20';
```

#### **Get next business day:**
```sql
SELECT data FROM dias_uteis 
WHERE data > '2026-01-20' AND eh_dia_util = TRUE 
ORDER BY data 
LIMIT 1;
```

#### **Get previous business day:**
```sql
SELECT data FROM dias_uteis 
WHERE data < '2026-01-20' AND eh_dia_util = TRUE 
ORDER BY data DESC
LIMIT 1;
```

#### **Count business days in a month:**
```sql
SELECT COUNT(*) FROM dias_uteis 
WHERE YEAR(data) = 2026 AND MONTH(data) = 1 AND eh_dia_util = TRUE;
```

#### **Get upcoming holidays:**
```sql
SELECT data, dia_semana, nome_feriado 
FROM dias_uteis 
WHERE data >= CURDATE() AND eh_feriado = TRUE 
ORDER BY data 
LIMIT 10;
```

#### **Get all holidays in a year:**
```sql
SELECT data, dia_semana, nome_feriado 
FROM dias_uteis 
WHERE YEAR(data) = 2026 AND eh_feriado = TRUE 
ORDER BY data;
```

---

## 📝 Example Queries

### **Example 1: Find next weekday after 16:15 cutoff**

```sql
-- If current time is after 16:15, get next business day
-- Otherwise, use current date if it's a business day

SELECT 
    CASE 
        WHEN CURTIME() >= '16:15:00' THEN 
            (SELECT data FROM dias_uteis 
             WHERE data > CURDATE() AND eh_dia_util = TRUE 
             ORDER BY data LIMIT 1)
        ELSE 
            CURDATE()
    END as target_date;
```

### **Example 2: Check if we should send alerts today**

```sql
-- Check if today is a business day and current time is between 08:00 and 16:15
SELECT 
    eh_dia_util,
    CASE 
        WHEN CURTIME() BETWEEN '08:00:00' AND '16:15:00' THEN TRUE
        ELSE FALSE
    END as within_alert_hours
FROM dias_uteis 
WHERE data = CURDATE();
```

---

## 🚀 Integration with Repurchase Report

The `dias_uteis` table can be used to enhance the repurchase report system:

1. **Skip alerts on holidays** - Don't send alerts on national holidays
2. **Calculate next business day** - For records created after 16:15
3. **Business hours validation** - Ensure alerts are sent only on business days
4. **Holiday notifications** - Warn users about upcoming holidays

---

## ✅ Benefits

1. ✅ **Accurate business day calculation** - Includes Brazilian holidays
2. ✅ **50 years of data** - Covers 2001-2050
3. ✅ **Fast queries** - Indexed for performance
4. ✅ **Easy to use** - Helper functions available
5. ✅ **Comprehensive** - Includes all national holidays
6. ✅ **Reliable** - Based on official holiday calendar

---

## 📅 Sample Data

```
2026-01-20 (terça-feira)    - BUSINESS DAY
2026-01-25 (domingo)         - WEEKEND
2026-02-16 (segunda-feira)   - HOLIDAY: Carnaval
2026-02-17 (terça-feira)     - HOLIDAY: Carnaval
2026-04-03 (sexta-feira)     - HOLIDAY: Paixão de Cristo
2026-04-21 (terça-feira)     - HOLIDAY: Tiradentes
```

