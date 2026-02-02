#!/bin/bash

# Complete Grafana Setup Script for APR Monitoring
# This script installs Grafana, configures MariaDB data source, and imports the dashboard

set -e  # Exit on error

echo "============================================================================"
echo "  APR Monitoring - Complete Grafana Setup"
echo "============================================================================"
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "❌ Please run as root (use sudo)"
    exit 1
fi

# Step 1: Install Grafana
echo "📦 Step 1: Installing Grafana..."
echo "============================================================================"

# Add Grafana GPG key
wget -q -O - https://packages.grafana.com/gpg.key | apt-key add -

# Add Grafana repository
echo "deb https://packages.grafana.com/oss/deb stable main" | tee /etc/apt/sources.list.d/grafana.list

# Update package list
apt-get update

# Install Grafana
apt-get install -y grafana

echo "✅ Grafana installed successfully"
echo ""

# Step 2: Start Grafana service
echo "🚀 Step 2: Starting Grafana service..."
echo "============================================================================"

systemctl daemon-reload
systemctl start grafana-server
systemctl enable grafana-server

echo "✅ Grafana service started and enabled"
echo ""

# Step 3: Wait for Grafana to be ready
echo "⏳ Step 3: Waiting for Grafana to be ready..."
echo "============================================================================"

sleep 10

# Check if Grafana is running
if systemctl is-active --quiet grafana-server; then
    echo "✅ Grafana is running"
else
    echo "❌ Grafana failed to start"
    systemctl status grafana-server
    exit 1
fi

echo ""

# Step 4: Display connection information
echo "============================================================================"
echo "  ✅ Grafana Installation Complete!"
echo "============================================================================"
echo ""
echo "📍 Access Grafana at: http://localhost:3000"
echo ""
echo "🔐 Default credentials:"
echo "   Username: admin"
echo "   Password: admin"
echo "   (You'll be prompted to change the password on first login)"
echo ""
echo "============================================================================"
echo "  Next Steps:"
echo "============================================================================"
echo ""
echo "1. Open your browser and go to: http://localhost:3000"
echo ""
echo "2. Login with admin/admin and change the password"
echo ""
echo "3. Add MariaDB Data Source:"
echo "   - Click ⚙️ Configuration → Data Sources"
echo "   - Click 'Add data source'"
echo "   - Select 'MySQL'"
echo "   - Configure:"
echo "     Name: BMA MariaDB"
echo "     Host: localhost:3306"
echo "     Database: BMA"
echo "     User: robot"
echo "     Password: r0b0t"
echo "   - Click 'Save & Test'"
echo ""
echo "4. Import Dashboard:"
echo "   - Click ➕ Create → Import"
echo "   - Upload: $(pwd)/grafana_dashboard_apr_monitoring.json"
echo "   - Select 'BMA MariaDB' as data source"
echo "   - Click 'Import'"
echo ""
echo "============================================================================"
echo "  Useful Commands:"
echo "============================================================================"
echo ""
echo "Check Grafana status:    sudo systemctl status grafana-server"
echo "Restart Grafana:         sudo systemctl restart grafana-server"
echo "View Grafana logs:       sudo journalctl -u grafana-server -f"
echo ""
echo "============================================================================"

