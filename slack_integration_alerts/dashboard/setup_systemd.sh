#!/bin/bash
#
# Setup APR Dashboard as a systemd service
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_FILE="$SCRIPT_DIR/apr_dashboard.service"
SYSTEMD_DIR="/etc/systemd/system"

echo "=========================================="
echo "APR Dashboard - Systemd Service Setup"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "❌ Error: This script must be run as root (use sudo)"
    exit 1
fi

# Create log directory
echo "Creating log directory..."
mkdir -p /var/log/apr_dashboard
chown robot:robot /var/log/apr_dashboard
echo "✓ Log directory created: /var/log/apr_dashboard"

# Copy service file
echo ""
echo "Installing systemd service..."
cp "$SERVICE_FILE" "$SYSTEMD_DIR/apr_dashboard.service"
chmod 644 "$SYSTEMD_DIR/apr_dashboard.service"
echo "✓ Service file installed: $SYSTEMD_DIR/apr_dashboard.service"

# Reload systemd
echo ""
echo "Reloading systemd daemon..."
systemctl daemon-reload
echo "✓ Systemd daemon reloaded"

# Enable service
echo ""
echo "Enabling service (auto-start on boot)..."
systemctl enable apr_dashboard.service
echo "✓ Service enabled"

echo ""
echo "=========================================="
echo "✓ Setup Complete!"
echo "=========================================="
echo ""
echo "Service management commands:"
echo "  Start:   sudo systemctl start apr_dashboard"
echo "  Stop:    sudo systemctl stop apr_dashboard"
echo "  Restart: sudo systemctl restart apr_dashboard"
echo "  Status:  sudo systemctl status apr_dashboard"
echo "  Logs:    sudo journalctl -u apr_dashboard -f"
echo ""
echo "Log files:"
echo "  Access:  /var/log/apr_dashboard/access.log"
echo "  Error:   /var/log/apr_dashboard/error.log"
echo ""
echo "To start the service now, run:"
echo "  sudo systemctl start apr_dashboard"
echo ""

