# Grafana Dashboard Import - Fixed! ✅

## Problem Solved

The original `grafana_dashboard_apr_monitoring.json` had a JSON syntax error at line 215.

**Fixed Version:** `grafana_dashboard_simple.json` ✅

---

## 🚀 How to Import the Dashboard

### **Step 1: Access Grafana**

Open your browser and go to:
```
http://localhost:3000
```

Login with your Grafana credentials.

---

### **Step 2: Import Dashboard**

1. Click **➕** (plus icon) in the left sidebar
2. Click **Import**
3. Click **Upload JSON file**
4. Select: `/home/robot/Dev/bma_python/slack_integration_alerts/grafana_dashboard_simple.json`
5. Click **Load**

---

### **Step 3: Configure Data Source**

1. In the **Import** screen, you'll see:
   - **Name:** APR Proposal Monitoring
   - **Folder:** General (or select a folder)
   - **Data Source:** Select **BMA_MariaDB** (or your MariaDB data source)

2. Click **Import**

---

## ✅ What You'll See

The dashboard includes **5 panels**:

### **Row 1: Key Metrics (3 stat panels)**
1. **Total Proposals Today** - Blue stat
2. **Valid Proposals Today** - Green stat
3. **Invalid Proposals Today** - Red stat (if > 0)

### **Row 2: Live Activity**
4. **Recent Status Changes** - Table showing last hour of changes
   - Time, Proposal, Client, Old Status, New Status, Source

### **Row 3: Analysis**
5. **Status Changes by Source** - Pie chart
   - SYSTEM vs BOT changes

---

## 🔧 After Import

### **Configure Auto-Refresh**

1. Click **🕐** (time range) at top right
2. Set refresh interval: **1m** (1 minute)
3. Click **Apply**

### **Set Time Range**

- Default: Last 24 hours
- You can change to: Last 7 days, Last 30 days, etc.

---

## 📊 Add More Panels (Optional)

Use queries from `GRAFANA_DASHBOARD_QUERIES.md` to add more panels:

### **Example: Add "Validation Trend" Graph**

1. Click **Add panel** → **Add new panel**
2. Select **BMA_MariaDB** as data source
3. Paste this query:
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
   ORDER BY DATA
   ```
4. Set visualization type: **Time series**
5. Set title: **Validation Results Trend (Last 30 Days)**
6. Click **Apply**

---

## 🎨 Customize Dashboard

### **Edit Panel**
- Click panel title → **Edit**

### **Move Panel**
- Drag panel by title bar

### **Resize Panel**
- Drag corners/edges

### **Delete Panel**
- Click panel title → **Remove**

---

## 🚨 Troubleshooting

### **Problem: "No data" in panels**

**Check if tables have data:**
```bash
mysql -u [user] -p BMA
SELECT COUNT(*) FROM apr_daily_summary;
SELECT COUNT(*) FROM apr_status_history;
```

**Run validation program to populate data:**
```bash
cd /home/robot/Deploy/slack_integration_alerts
./query_apr_invalidos_status
```

### **Problem: "Database connection failed"**

**Check data source configuration:**
1. Go to **⚙️ Configuration** → **Data Sources**
2. Click **BMA_MariaDB**
3. Click **Save & Test**
4. Should see: ✅ "Database Connection OK"

---

## 📝 Next Steps

1. ✅ Import `grafana_dashboard_simple.json`
2. ✅ Configure auto-refresh (1 minute)
3. ✅ Add more panels from `GRAFANA_DASHBOARD_QUERIES.md`
4. ✅ Set up alerts for invalid records
5. ✅ Share dashboard with team

---

## 🎉 You're Done!

Your Grafana dashboard is ready to monitor APR proposals in real-time!

**Dashboard Features:**
- ✅ Real-time metrics
- ✅ Status change tracking
- ✅ SYSTEM vs BOT activity
- ✅ Auto-refresh every minute
- ✅ Clean, professional layout

---

**File Location:** `/home/robot/Dev/bma_python/slack_integration_alerts/grafana_dashboard_simple.json`

**Enjoy your new monitoring dashboard!** 📊🚀

