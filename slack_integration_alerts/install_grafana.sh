#!/bin/bash
# Grafana Installation Script for Ubuntu/Debian
# This script installs Grafana OSS (Open Source)

set -e

echo "================================================================================"
echo "Grafana Installation Script"
echo "================================================================================"
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    echo "⚠️  Please do not run this script as root. Run as regular user with sudo access."
    exit 1
fi

# Check OS
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
    VERSION=$VERSION_ID
else
    echo "❌ Cannot detect OS. This script is for Ubuntu/Debian systems."
    exit 1
fi

echo "Detected OS: $OS $VERSION"
echo ""

# Install dependencies
echo "📦 Installing dependencies..."
sudo apt-get update
sudo apt-get install -y apt-transport-https wget gnupg2

# Add Grafana GPG key
echo "🔑 Adding Grafana GPG key..."
sudo mkdir -p /etc/apt/keyrings/
wget -q -O - https://apt.grafana.com/gpg.key | gpg --dearmor | sudo tee /etc/apt/keyrings/grafana.gpg > /dev/null

# Add Grafana repository
echo "📚 Adding Grafana repository..."
echo "deb [signed-by=/etc/apt/keyrings/grafana.gpg] https://apt.grafana.com stable main" | sudo tee -a /etc/apt/sources.list.d/grafana.list

# Update package list
echo "🔄 Updating package list..."
sudo apt-get update

# Install Grafana
echo "📥 Installing Grafana..."
sudo apt-get install -y grafana

# Enable and start Grafana service
echo "🚀 Enabling and starting Grafana service..."
sudo systemctl daemon-reload
sudo systemctl enable grafana-server
sudo systemctl start grafana-server

# Check status
echo ""
echo "================================================================================"
echo "✅ Grafana Installation Complete!"
echo "================================================================================"
echo ""
echo "Grafana is now running on: http://localhost:3000"
echo ""
echo "Default credentials:"
echo "  Username: admin"
echo "  Password: admin"
echo "  (You will be prompted to change the password on first login)"
echo ""
echo "To check status:"
echo "  sudo systemctl status grafana-server"
echo ""
echo "To view logs:"
echo "  sudo journalctl -u grafana-server -f"
echo ""
echo "Next steps:"
echo "  1. Open http://localhost:3000 in your browser"
echo "  2. Login with admin/admin"
echo "  3. Follow the setup guide in GRAFANA_SETUP_GUIDE.md"
echo ""
echo "================================================================================"

