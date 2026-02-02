# Grafana Quick Setup Guide

## Prerequisites
- MariaDB is already running with monitoring tables created ✅
- Monitoring data is being populated ✅

## Step 1: Install Grafana

```bash
# Run the installation script
cd /home/robot/Dev/bma_python/slack_integration_alerts
chmod +x install_grafana.sh
sudo ./install_grafana.sh
```

**What this does:**
- Installs Grafana OSS (Open Source)
- Starts Grafana service
- Enables auto-start on boot

## Step 2: Verify Grafana is Running

```bash
# Check Grafana status
sudo systemctl status grafana-server

# If not running, start it
sudo systemctl start grafana-server
```

## Step 3: Access Grafana Web Interface

Open your browser and navigate to:
```
http://localhost:3000
```

**Default Login:**
- Username: `admin`
- Password: `admin`

You'll be prompted to change the password on first login.

## Step 4: Configure MariaDB Data Source

1. In Grafana, click **⚙️ Configuration** (gear icon) → **Data Sources**
2. Click **Add data source**
3. Search for and select **MySQL**
4. Configure the connection:

```
Name: BMA MariaDB
Host: localhost:3306
Database: bma_slack_alerts
User: <your_mariadb_user>
Password: <your_mariadb_password>
```

5. Click **Save & Test** - you should see "Database Connection OK"

## Step 5: Import the Dashboard

1. Click **➕** (plus icon) → **Import**
2. Click **Upload JSON file**
3. Browse to: `/home/robot/Dev/bma_python/slack_integration_alerts/grafana_dashboard_apr_monitoring.json`
4. In the **MySQL** dropdown, select the data source you just created
5. Click **Import**

## Step 6: View Your Dashboard

You should now see the **APR Monitoring Dashboard** with 12 panels:

### 📊 Dashboard Panels:
1. **Current Status Overview** - Pie chart of proposal statuses
2. **Invalid Records by Type** - Bar chart (NFEChave, DUPLICATA, SEUNO)
3. **Invalid Records by Product** - Product breakdown
4. **Processing Trend** - Time series of proposals over time
5. **Alert Volume** - Alerts sent over time
6. **Resolution Time** - How long invalid records take to resolve
7. **Active Invalid Records** - Current unresolved issues
8. **Today's Stats** - Key metrics for today
9. **Execution Time** - Program performance
10. **Recent Status Changes** - Latest status transitions
11. **Top Cedentes** - Most active assignors
12. **Product Distribution** - Product type analysis

## Troubleshooting

### Grafana won't start
```bash
# Check logs
sudo journalctl -u grafana-server -f

# Restart service
sudo systemctl restart grafana-server
```

### Can't connect to MariaDB
```bash
# Test MariaDB connection
mysql -u <user> -p -h localhost bma_slack_alerts

# Check if MariaDB is running
sudo systemctl status mariadb
```

### Dashboard shows "No Data"
```bash
# Verify monitoring data exists
cd /home/robot/Dev/bma_python/slack_integration_alerts
/home/robot/Dev/bma_python/.venv/bin/python3 view_monitoring_data.py

# Run the main program to populate data
/home/robot/Dev/bma_python/.venv/bin/python3 query_apr_invalidos_status.py
```

## Next Steps

Once the dashboard is working:
- ⏰ Set up scheduled runs (cron job) to populate data regularly
- 🔔 Configure Grafana alerts for critical thresholds
- 📧 Set up email notifications
- 👥 Add team members with appropriate permissions

## Useful Commands

```bash
# Start Grafana
sudo systemctl start grafana-server

# Stop Grafana
sudo systemctl stop grafana-server

# Restart Grafana
sudo systemctl restart grafana-server

# Check Grafana status
sudo systemctl status grafana-server

# View Grafana logs
sudo journalctl -u grafana-server -f
```

## Default Ports

- **Grafana**: http://localhost:3000
- **MariaDB**: localhost:3306

---

**Need Help?** Check the detailed guide: `GRAFANA_SETUP_GUIDE.md`

