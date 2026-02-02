# Grafana Setup Summary - What You Need

## 🎯 Current Status

✅ **MariaDB** - Running with monitoring tables created  
✅ **Monitoring Data** - Being populated by the Python program  
✅ **Dashboard JSON** - Ready to import (`grafana_dashboard_apr_monitoring.json`)  
❌ **Grafana** - Not installed yet  

---

## 📋 What You Need to Do

### **Option 1: Automated Setup (Recommended)**

Run the complete setup script:

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts
chmod +x setup_grafana_complete.sh
sudo ./setup_grafana_complete.sh
```

This will:
1. ✅ Install Grafana
2. ✅ Start Grafana service
3. ✅ Display connection information

Then manually:
1. Open browser → http://localhost:3000
2. Login (admin/admin)
3. Add MariaDB data source
4. Import dashboard JSON

---

### **Option 2: Manual Setup**

#### **Step 1: Install Grafana**

```bash
# Add Grafana repository
wget -q -O - https://packages.grafana.com/gpg.key | sudo apt-key add -
echo "deb https://packages.grafana.com/oss/deb stable main" | sudo tee /etc/apt/sources.list.d/grafana.list

# Install
sudo apt-get update
sudo apt-get install -y grafana

# Start service
sudo systemctl start grafana-server
sudo systemctl enable grafana-server
```

#### **Step 2: Access Grafana**

Open browser: **http://localhost:3000**

Login:
- Username: `admin`
- Password: `admin`

#### **Step 3: Add MariaDB Data Source**

1. Click **⚙️ Configuration** → **Data Sources**
2. Click **Add data source**
3. Select **MySQL**
4. Configure:

```
Name:     BMA MariaDB
Host:     localhost:3306
Database: BMA
User:     robot
Password: r0b0t
```

5. Click **Save & Test**

#### **Step 4: Import Dashboard**

1. Click **➕ Create** → **Import**
2. Click **Upload JSON file**
3. Select: `/home/robot/Dev/bma_python/slack_integration_alerts/grafana_dashboard_apr_monitoring.json`
4. Select data source: **BMA MariaDB**
5. Click **Import**

---

## 📊 What You'll See in Grafana

Once set up, you'll have a dashboard with **12 panels**:

### **Overview Panels**
- 📊 Current Status Overview (Pie chart)
- 📈 Processing Trend (Time series)
- 📋 Today's Stats (Key metrics)

### **Validation Panels**
- ⚠️ Invalid Records by Type (NFEChave, DUPLICATA, SEUNO)
- 📦 Invalid Records by Product
- 🔴 Active Invalid Records
- ⏱️ Resolution Time

### **Analysis Panels**
- 📢 Alert Volume
- 👥 Top Cedentes
- 🏷️ Product Distribution

### **Performance Panels**
- ⚡ Execution Time
- 📝 Recent Status Changes

---

## 🔧 Configuration Details

### **MariaDB Connection**
```
Server:   localhost
Port:     3306
Database: BMA
User:     robot
Password: r0b0t
```

### **Monitoring Tables**
- `apr_invalid_records` - Validation failures
- `apr_status_history` - Status changes
- `apr_proposal_products` - Product tracking
- `apr_processing_log` - Execution logs
- `apr_daily_summary` - Daily metrics

---

## 🚀 Quick Start Commands

```bash
# Install Grafana (automated)
cd /home/robot/Dev/bma_python/slack_integration_alerts
sudo ./setup_grafana_complete.sh

# Check if Grafana is running
sudo systemctl status grafana-server

# View monitoring data (console)
/home/robot/Dev/bma_python/.venv/bin/python3 view_monitoring_data.py

# Populate monitoring data
/home/robot/Dev/bma_python/.venv/bin/python3 query_apr_invalidos_status.py
```

---

## 📚 Additional Resources

- **Quick Setup Guide**: `GRAFANA_QUICK_SETUP.md`
- **Detailed Setup Guide**: `GRAFANA_SETUP_GUIDE.md`
- **Dashboard JSON**: `grafana_dashboard_apr_monitoring.json`
- **Console Viewer**: `view_monitoring_data.py`

---

## ❓ FAQ

**Q: Do I need to install anything else?**  
A: No, just Grafana. MariaDB and Python are already set up.

**Q: Can I use the console viewer instead?**  
A: Yes! Run `view_monitoring_data.py` to see data in the terminal.

**Q: How often is data updated?**  
A: Every time you run `query_apr_invalidos_status.py`

**Q: Can I customize the dashboard?**  
A: Yes! Once imported, you can edit panels, add new ones, etc.

---

## 🆘 Troubleshooting

**Grafana won't start:**
```bash
sudo journalctl -u grafana-server -f
sudo systemctl restart grafana-server
```

**Can't connect to MariaDB:**
```bash
mysql -u robot -p -h localhost BMA
# Password: r0b0t
```

**Dashboard shows "No Data":**
```bash
# Run the main program to populate data
/home/robot/Dev/bma_python/.venv/bin/python3 query_apr_invalidos_status.py
```

