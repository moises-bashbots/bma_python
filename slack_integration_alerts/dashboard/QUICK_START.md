# 🚀 Quick Start - APR Monitoring Dashboard

**Your simple web dashboard is ready!**

---

## ✅ What's Been Created

### **Files Created:**
```
slack_integration_alerts/dashboard/
├── app.py                    # Flask application (API + routes)
├── templates/
│   └── dashboard.html        # Beautiful web interface
├── start_dashboard.sh        # Startup script
├── README.md                 # Full documentation
└── QUICK_START.md           # This file
```

### **Dependencies Installed:**
- ✅ Flask 3.1.2 (web framework)
- ✅ PyMySQL 1.1.2 (database connector)
- ✅ All installed via UV in local venv

---

## 🎯 Start the Dashboard (3 Ways)

### **Option 1: Using the Startup Script (Recommended)**

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard
./start_dashboard.sh
```

### **Option 2: Using UV directly**

```bash
cd /home/robot/Dev/bma_python
uv run python slack_integration_alerts/dashboard/app.py
```

### **Option 3: Manual Python**

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard
python3 app.py
```

---

## 🌐 Access the Dashboard

Once started, open your browser:

```
http://localhost:5000
```

Or from another machine:
```
http://[your-server-ip]:5000
```

---

## 📊 What You'll See

### **Top Row - Key Metrics**
- 🔵 **Total Proposals** - Total proposals today
- 🟢 **Valid** - Valid proposals count
- 🔴 **Invalid** - Invalid proposals count (red if > 0)
- 🟠 **Total Value** - Total approved value in R$

### **Charts Section**
- 📊 **Invalid Records by Type** - Bar chart (NFEChave, DUPLICATA, SEUNO, CHEQUE)
- 🥧 **Status Changes by Source** - Pie chart (SYSTEM vs BOT)

### **Trend Chart**
- 📈 **30-Day Validation Trend** - Line chart showing trends over time

### **Live Tables**
- 📋 **Recent Status Changes** - Last 2 hours of activity
- ⚠️ **Current Invalid Records** - Unresolved issues

### **Auto-Refresh**
- ⏱️ Updates every 30 seconds automatically
- 🕐 Shows last update time in header

---

## 🎨 Features

### **Real-Time Monitoring**
- ✅ Auto-refresh every 30 seconds
- ✅ Live data from MariaDB
- ✅ No manual refresh needed

### **Interactive Charts**
- ✅ Hover for details
- ✅ Click legend to toggle
- ✅ Responsive design

### **Status Tracking**
- 🔵 **SYSTEM** badge - Changes from validation program
- 🟢 **BOT** badge - Changes from bot after "Processar"
- 🟡 **AUTO** badge - Auto-resolved issues

### **Mobile Friendly**
- ✅ Works on phones/tablets
- ✅ Responsive layout
- ✅ Touch-friendly

---

## 🔧 Configuration

### **Change Auto-Refresh Interval**

Edit `templates/dashboard.html` line 215:
```javascript
const REFRESH_INTERVAL = 30000; // milliseconds (30 seconds)
```

Examples:
- 10 seconds: `10000`
- 1 minute: `60000`
- 5 minutes: `300000`

### **Change Port**

Edit `app.py` line 175:
```python
app.run(host='0.0.0.0', port=5000, debug=True)
```

### **Database Connection**

Automatically reads from:
```
/home/robot/Dev/bma_python/slack_integration_alerts/databases_config.json
```

No configuration needed!

---

## 🧪 Test the API

The dashboard provides REST API endpoints you can test:

```bash
# Get today's summary
curl http://localhost:5000/api/summary | python3 -m json.tool

# Get recent status changes
curl http://localhost:5000/api/status_changes | python3 -m json.tool

# Get status by source
curl http://localhost:5000/api/status_by_source | python3 -m json.tool

# Get 30-day trend
curl http://localhost:5000/api/trend | python3 -m json.tool

# Get invalid records
curl http://localhost:5000/api/invalid_records | python3 -m json.tool
```

---

## 🚨 Troubleshooting

### **Problem: Port 5000 already in use**

**Solution:**
```bash
# Find what's using port 5000
sudo lsof -i :5000

# Kill the process
kill -9 [PID]

# Or change the port in app.py
```

### **Problem: No data showing**

**Solution:**
```bash
# Run validation program to populate data
cd /home/robot/Deploy/slack_integration_alerts
./query_apr_invalidos_status
```

### **Problem: Database connection error**

**Solution:**
```bash
# Check MariaDB is running
sudo systemctl status mariadb

# Test connection
mysql -u [user] -p BMA
```

---

## 🎉 Advantages Over Grafana

| Feature | This Dashboard | Grafana |
|---------|---------------|---------|
| Setup Time | 2 minutes | 30+ minutes |
| Configuration | None needed | Complex |
| Dependencies | Flask only | Grafana server |
| Customization | Full control | Limited |
| Resource Usage | Minimal | Heavy |
| Learning Curve | Easy | Steep |

---

## 📚 Next Steps

1. ✅ **Start the dashboard** using one of the methods above
2. ✅ **Access** at `http://localhost:5000`
3. ✅ **Customize** colors/layout if needed
4. ✅ **Add more panels** using the API endpoints
5. ✅ **Share** with your team!

---

**Your dashboard is ready to use!** 🚀📊

For full documentation, see `README.md`

