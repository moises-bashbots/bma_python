# Repurchase Alert Rules - Implementation Documentation

## Overview

This document describes the new alert rules implemented for the repurchase report system.

---

## 🎯 **New Alert Rules**

### **1. Filter by tipo_operacao**
- ✅ **Only send alerts for records with `tipo_operacao = 1`**
- Records with `tipo_operacao = 0` or `2` are excluded from alerts
- This filters to only regular factoring operations

### **2. Time-Based Alert Windows**
Alerts are sent **twice per day** in two time windows:

| Window | Time Range | Alert Frequency |
|--------|------------|-----------------|
| **Morning** | 08:00 - 11:59 | Once per record |
| **Afternoon** | 12:00 - 16:15 | Once per record |

- Each record can trigger **one alert in the morning** and **one alert in the afternoon**
- Duplicity control is per time window (not per day)

### **3. After-Hours Records**
- Records created **after 16:15** will be sent the **next BUSINESS day**
- The system queries records from **16:15 of the previous BUSINESS day** onwards
- This ensures no records are missed
- **Business days** exclude weekends and Brazilian national holidays

### **4. Business Days Only**
- ✅ **Alerts are ONLY sent on BUSINESS DAYS**
- Weekends (Saturday/Sunday) are skipped
- Brazilian national holidays are skipped
- Uses the `dias_uteis` table in MariaDB for business day calculation
- If today is not a business day, the script exits with a warning

### **5. Alert Sending Hours**
- ✅ **Alerts are ONLY sent between 08:00 and 16:15**
- If the script runs outside these hours, alerts are skipped
- A warning message is displayed

### **6. Simplified Message Format**
- **Old format:** `@Bárbara Melo ANIMALRACOES, borderô 39, OPERAÇÃO`
- **New format (BMA FIDC):** `@Bárbara Melo ANIMALRACOES`
- **New format (BMA):** `@Larissa Queiroz CLAUDIA BEATRIZ PINT BMA Sec`
- Removed: borderô number, operation type, and product information
- Added: "BMA Sec" suffix for BMA company records

**Company-Specific Formatting:**
- **BMA FIDC records:** Standard format (no suffix)
  - Format: `@Operator CEDENTE`
  - Example: `@Thais COMERCIAL SAN MARTIM`
- **BMA records:** Append "BMA Sec" at the end
  - Format: `@Operator CEDENTE BMA Sec`
  - Example: `@Larissa Queiroz CLAUDIA BEATRIZ PINT BMA Sec`

**Note on Product Information:**
- Product information is retrieved from the database for filtering purposes (see Rule 7)
- However, products are **NOT displayed** in the alert messages
- Products are only used internally to determine if an alert should be excluded

### **7. Product Exclusion Filter**
- ✅ **Certain products are EXCLUDED from alerts**
- If a borderô contains ANY of the following products, NO alert is sent:
  - **Capital de Giro**
  - **Capital de Giro NP**
  - **Cobrança Simples**
  - **Cobrança Simples Garantia**
  - **Nota Comercial**
  - **CCB**
  - **Renegociação**

**Exclusion Logic:**
- Product names are matched **case-insensitive** and **accent-insensitive**
- If a borderô has multiple products and ANY one is excluded, the entire borderô is excluded
- Example: `CONVENCIONAL PRÉ-IMPR. BMA FIDC | Capital de Giro` → **EXCLUDED** (has Capital de Giro)
- Example: `CONVENCIONAL PRÉ-IMPR. BMA FIDC | CHEQUE` → **ALLOWED** (no excluded products)

---

## 🔧 **Technical Implementation**

### **Query Changes**

**Old query:**
```python
filter(cast(APRCapa.DATA, Date) == target_date)
```

**New query (with business days):**
```python
# Check if today is a business day
if not is_business_day(target_date):
    return []  # Skip query on weekends/holidays

# Get previous business day
previous_business_day = get_previous_business_day(target_date)

# Calculate cutoff: previous BUSINESS day at 16:15:00
cutoff_datetime = datetime.combine(previous_business_day, time(16, 15, 0))

filter(
    APRCapa.dataentrada >= cutoff_datetime,
    APRCapa.tipo_operacao == 1
)
```

### **Duplicity Control Changes**

**Old alert ID format:**
```
METALURGICA_REUTER_2
```

**New alert ID format (based on CEDENTE + TIME_WINDOW only):**
```
METALURGICA_REUTER_morning
METALURGICA_REUTER_afternoon
```

**IMPORTANT:** The alert ID is based on **CEDENTE + TIME_WINDOW only** (borderô number is NOT included).

This ensures:
- **Maximum 2 alerts per CEDENTE per day** (one morning, one afternoon)
- If a cedente has multiple borderôs in the same time window, only the **first one** triggers an alert
- Subsequent borderôs for the same cedente in the same time window are skipped

**Example:**
```
09:00 - METALURGICA_REUTER borderô 5  → ✓ Alert sent (first morning alert)
09:30 - METALURGICA_REUTER borderô 8  → ✗ Skipped (already alerted in morning)
13:00 - METALURGICA_REUTER borderô 12 → ✓ Alert sent (first afternoon alert)
14:00 - METALURGICA_REUTER borderô 15 → ✗ Skipped (already alerted in afternoon)
```

### **New Helper Functions**

1. **`get_alert_time_window()`**
   - Returns: `"morning"`, `"afternoon"`, or `"after_hours"`
   - Based on current time

2. **`is_within_alert_hours()`**
   - Returns: `True` if between 08:00 and 16:15
   - Returns: `False` otherwise

3. **`create_alert_id(cedente, numero, time_window)`**
   - Now includes time window in the ID
   - Allows separate tracking for morning/afternoon

---

## 📊 **Example Scenarios**

### **Scenario 1: Record created at 09:00**
- ✅ Alert sent in **morning window** (08:00-11:59)
- ✅ Alert sent again in **afternoon window** (12:00-16:15)
- Total: **2 alerts**

### **Scenario 2: Record created at 13:00**
- ❌ No alert in morning window (record didn't exist yet)
- ✅ Alert sent in **afternoon window** (12:00-16:15)
- Total: **1 alert**

### **Scenario 3: Record created at 17:00 (after hours)**
- ❌ No alert today (outside business hours)
- ✅ Alert sent **next weekday morning** (08:00-11:59)
- ✅ Alert sent **next weekday afternoon** (12:00-16:15)
- Total: **2 alerts** (next day)

### **Scenario 4: Script runs at 18:00**
- ⚠️ Warning: Outside alert hours
- ❌ No alerts sent
- Message: "Run this script between 08:00 and 16:15"

---

## 🚀 **Usage**

### **Dry-Run Mode (Default)**
```bash
./repurchase_report --dry-run
```
- Shows what would be sent
- Does NOT actually send alerts
- Does NOT mark alerts as sent

### **Production Mode**
```bash
./repurchase_report
```
- Sends actual Slack alerts
- Marks alerts as sent in duplicity control
- Only runs between 08:00 and 16:15

---

## 📝 **Alert Tracking File**

**File:** `sent_alerts.json`

**Format:**
```json
{
  "2026-01-20": [
    "CLAUDIA_BEATRIZ_PINT_14_morning",
    "PERFLEX_29_morning",
    "CLAUDIA_BEATRIZ_PINT_14_afternoon",
    "PERFLEX_29_afternoon"
  ]
}
```

- Cleaned up automatically (keeps last 7 days)
- Separate entries for morning and afternoon windows

---

## ✅ **Benefits**

1. ✅ **No spam** - Maximum 2 alerts per record per day
2. ✅ **Timely reminders** - Morning and afternoon notifications
3. ✅ **No missed records** - After-hours records sent next day
4. ✅ **Cleaner messages** - Simplified format
5. ✅ **Focused alerts** - Only tipo_operacao = 1 (regular operations)
6. ✅ **Business hours only** - Respects working hours (08:00-16:15)

