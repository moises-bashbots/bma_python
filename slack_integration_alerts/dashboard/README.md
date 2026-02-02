# APR Proposal Monitoring Dashboard

**Simple Flask web dashboard for real-time monitoring of APR proposals**

---

## 🎯 Features

### **Real-Time Metrics**
- ✅ Total proposals today
- ✅ Valid vs Invalid counts
- ✅ Total approved value
- ✅ Auto-refresh every 30 seconds

### **Interactive Charts**
- 📊 Invalid records by type (Bar chart)
- 🥧 Status changes by source (Pie chart)
- 📈 30-day validation trend (Line chart)

### **Live Tables**
- 📋 Recent status changes (last 2 hours)
- ⚠️ Current invalid records

### **Status Change Tracking**
- 🔵 SYSTEM - Changes detected by validation program
- 🟢 BOT - Changes made by bot after clicking "Processar"
- 🟡 AUTO_RESOLVE - Invalid → Valid transitions

---

## 🚀 Quick Start

### **1. Install Flask (if not already installed)**

```bash
cd /home/robot/Dev/bma_python
source .venv/bin/activate
pip install flask pymysql
```

### **2. Start the Dashboard**

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard
python3 app.py
```

Or use the startup script:

```bash
./start_dashboard.sh
```

### **3. Access the Dashboard**

Open your browser and go to:
```
http://localhost:5000
```

Or from another machine on the network:
```
http://[server-ip]:5000
```

---

## 📊 Dashboard Sections

### **Top Metrics Row**
- **Total Proposals** - Blue card showing total proposals today
- **Valid** - Green card showing valid proposals
- **Invalid** - Red card showing invalid proposals (turns red if > 0)
- **Total Value** - Orange card showing total approved value in BRL

### **Charts Section**
- **Invalid Records by Type** - Bar chart showing breakdown by validation type
- **Status Changes by Source** - Pie chart showing SYSTEM vs BOT changes

### **Trend Chart**
- **30-Day Validation Trend** - Line chart showing valid/invalid trends over time

### **Tables Section**
- **Recent Status Changes** - Live table of last 2 hours of status changes
- **Current Invalid Records** - Table of unresolved invalid records

---

## ⚙️ Configuration

### **Auto-Refresh Interval**

Edit `templates/dashboard.html` line 215:
```javascript
const REFRESH_INTERVAL = 30000; // 30 seconds (in milliseconds)
```

### **Database Connection**

The dashboard automatically reads from:
```
/home/robot/Dev/bma_python/slack_integration_alerts/databases_config.json
```

### **Port Configuration**

Edit `app.py` line 175:
```python
app.run(host='0.0.0.0', port=5000, debug=True)
```

---

## 🔧 API Endpoints

The dashboard provides REST API endpoints:

- **GET /api/summary** - Today's summary metrics
- **GET /api/status_changes** - Recent status changes (last 2 hours)
- **GET /api/status_by_source** - Status changes grouped by source
- **GET /api/trend** - 30-day trend data
- **GET /api/invalid_records** - Current invalid records

You can use these endpoints for custom integrations!

---

## 🎨 Customization

### **Change Colors**

Edit `templates/dashboard.html` CSS section (lines 10-155):
```css
.metric-card.total .metric-value { color: #667eea; }
.metric-card.valid .metric-value { color: #10b981; }
.metric-card.invalid .metric-value { color: #ef4444; }
```

### **Add More Metrics**

1. Add API endpoint in `app.py`
2. Add HTML element in `templates/dashboard.html`
3. Add JavaScript fetch function

---

## 🚨 Troubleshooting

### **Problem: "Connection refused"**

**Solution:**
```bash
# Check if Flask is running
ps aux | grep app.py

# Check if port 5000 is in use
netstat -tuln | grep 5000
```

### **Problem: "No data" in dashboard**

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

# Test database connection
mysql -u [user] -p BMA
```

---

## 📱 Mobile Access

The dashboard is responsive and works on mobile devices!

Access from your phone:
```
http://[server-ip]:5000
```

---

## 🔒 Security Notes

**For production use:**

1. **Disable debug mode** in `app.py`:
   ```python
   app.run(host='0.0.0.0', port=5000, debug=False)
   ```

2. **Add authentication** (optional):
   ```bash
   pip install flask-httpauth
   ```

3. **Use HTTPS** with reverse proxy (nginx/apache)

4. **Restrict access** by IP in firewall

---

## 🎉 Advantages Over Grafana

- ✅ **No installation** - Just Python + Flask
- ✅ **No configuration** - Works out of the box
- ✅ **Lightweight** - Minimal resource usage
- ✅ **Customizable** - Full control over design
- ✅ **Fast** - No complex setup
- ✅ **Portable** - Runs anywhere Python runs

---

**Enjoy your new monitoring dashboard!** 📊🚀

