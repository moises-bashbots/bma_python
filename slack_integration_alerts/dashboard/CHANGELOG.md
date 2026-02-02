# Dashboard Changelog

## 2026-02-02 - Major Update: Removed Charts, Added Full Proposals List

### Changes Made

#### 1. **Removed All Charts**
- ❌ Removed Chart.js library
- ❌ Removed "Invalid Records by Type" bar chart
- ❌ Removed "Status Changes by Source" pie chart
- ❌ Removed "30-Day Validation Trend" line chart
- ❌ Removed all chart-related CSS
- ❌ Removed all chart-related JavaScript functions

#### 2. **Added Full Proposals List**
- ✅ Created new API endpoint: `/api/all_proposals`
- ✅ Combines data from `apr_valid_records` and `apr_invalid_records` tables
- ✅ Shows ALL proposals for the current day (valid and invalid)
- ✅ Displays comprehensive information:
  - Proposal number
  - Client name (CEDENTE)
  - Branch (RAMO)
  - Manager (GERENTE)
  - Status
  - Approved value (VLR_APROVADOS)
  - Validation status (VALID/INVALID)
  - Validation details (type and reason for invalid proposals)

#### 3. **Visual Improvements**
- ✅ Invalid proposals highlighted with red background (#fee2e2)
- ✅ Valid proposals shown with green badge
- ✅ Invalid proposals shown with red badge and validation type
- ✅ Currency formatting for approved values
- ✅ Sticky table headers for better scrolling
- ✅ Improved table readability with smaller font size

#### 4. **Removed Tables**
- ❌ Removed "Current Invalid Records" table (now part of full list)

#### 5. **Kept Tables**
- ✅ "All Proposals for Today" - NEW main table
- ✅ "Recent Status Changes (Last 2 Hours)" - Kept for tracking

---

### New Dashboard Layout

```
┌─────────────────────────────────────────────────────────┐
│ 📊 APR Proposal Monitoring                              │
│ Last update: HH:MM:SS                                   │
└─────────────────────────────────────────────────────────┘

┌──────────┬──────────┬──────────┬──────────┐
│  Total   │  Valid   │ Invalid  │  Value   │
│   77     │   77     │   14     │ R$ 33M   │
└──────────┴──────────┴──────────┴──────────┘

┌─────────────────────────────────────────────────────────┐
│ All Proposals for Today                                 │
├─────────────────────────────────────────────────────────┤
│ Proposal │ Client │ Branch │ Manager │ Status │ Value  │
│    1     │  ABC   │  C-    │  John   │ Signed │ R$ 200K│
│    2     │  XYZ   │  C+    │  Mary   │ Signed │ R$ 64K │
│   ...    │  ...   │  ...   │  ...    │  ...   │  ...   │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ Recent Status Changes (Last 2 Hours)                    │
├─────────────────────────────────────────────────────────┤
│ Time │ Proposal │ Client │ Old → New │ Source          │
│ ...  │   ...    │  ...   │   ...     │  ...            │
└─────────────────────────────────────────────────────────┘
```

---

### API Changes

#### New Endpoint: `/api/all_proposals`

**Returns:** Array of all proposals for today (valid + invalid)

**Response Structure:**
```json
[
  {
    "DATA": "2026-02-02",
    "PROPOSTA": 1,
    "CEDENTE": "CLIENT NAME",
    "RAMO": "RATING C-",
    "GERENTE": "MANAGER NAME",
    "EMPRESA": "BMA FIDC",
    "STATUS": "Assinado",
    "VLR_APROVADOS": "204753.15",
    "QTD_APROVADOS": 44,
    "VALOR_TITULOS": "204753.15",
    "QTD_TITULOS": 44,
    "VALIDATION_STATUS": "VALID",
    "VALIDATION_TYPE": null,
    "MOTIVO": null
  },
  {
    "DATA": "2026-02-02",
    "PROPOSTA": 50,
    "CEDENTE": "INVALID CLIENT",
    "RAMO": "RATING B",
    "GERENTE": "MANAGER",
    "EMPRESA": "BMA FIDC",
    "STATUS": "Aguardando",
    "VLR_APROVADOS": "0.00",
    "QTD_APROVADOS": 0,
    "VALOR_TITULOS": "0.00",
    "QTD_TITULOS": 0,
    "VALIDATION_STATUS": "INVALID",
    "VALIDATION_TYPE": "DUPLICATA",
    "MOTIVO": "Missing DUPLICATA records"
  }
]
```

---

### Files Modified

1. **`app.py`**
   - Added `/api/all_proposals` endpoint (lines 241-302)
   - Queries both `apr_valid_records` and `apr_invalid_records`
   - Combines and sorts results by proposal number

2. **`templates/dashboard.html`**
   - Removed Chart.js script tag
   - Removed all chart-related CSS (~20 lines)
   - Removed all chart HTML elements
   - Added "All Proposals for Today" table
   - Removed "Current Invalid Records" table
   - Removed all chart JavaScript functions (~140 lines)
   - Added `loadAllProposals()` function
   - Updated `loadAllData()` to call new function
   - Added sticky header CSS for better table scrolling

---

### Benefits

1. **Simpler Interface** - No complex charts, just data
2. **Complete View** - See ALL proposals in one place
3. **Easy Filtering** - Visual distinction between valid/invalid
4. **Better Performance** - No chart rendering overhead
5. **More Information** - Shows manager, branch, company, etc.
6. **Easier to Export** - Table format is easier to copy/paste

---

### Usage

**Start Dashboard:**
```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard
./start_dashboard.sh
```

**Access:**
```
http://localhost:5000
```

**Features:**
- ✅ Auto-refresh every 30 seconds
- ✅ Red highlighting for invalid proposals
- ✅ Sortable by proposal number
- ✅ Shows validation details for invalid records
- ✅ Currency formatting for values

---

**Dashboard is now cleaner and more focused on the data!** 📊✨

