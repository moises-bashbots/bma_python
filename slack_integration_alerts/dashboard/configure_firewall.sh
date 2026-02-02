#!/bin/bash
# Configure firewall for APR Dashboard HTTPS access
# VM IP: 192.168.122.53
# External IP: 177.136.226.231

set -e

echo "=========================================="
echo "APR Dashboard - Firewall Configuration"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "Please run as root (use sudo)"
    exit 1
fi

echo "🔥 Configuring firewall rules..."

# Install iptables-persistent to save rules
echo "📦 Installing iptables-persistent..."
apt-get update
apt-get install -y iptables-persistent

# Allow HTTP (port 80) - for redirect to HTTPS
echo "✅ Opening port 80 (HTTP)..."
iptables -C INPUT -p tcp --dport 80 -j ACCEPT 2>/dev/null || \
    iptables -A INPUT -p tcp --dport 80 -j ACCEPT

# Allow HTTPS (port 443)
echo "✅ Opening port 443 (HTTPS)..."
iptables -C INPUT -p tcp --dport 443 -j ACCEPT 2>/dev/null || \
    iptables -A INPUT -p tcp --dport 443 -j ACCEPT

# Block direct access to Flask port 5000 from external networks
# Only allow localhost to access port 5000
echo "🔒 Blocking external access to port 5000 (Flask)..."
iptables -C INPUT -p tcp --dport 5000 -s 127.0.0.1 -j ACCEPT 2>/dev/null || \
    iptables -I INPUT -p tcp --dport 5000 -s 127.0.0.1 -j ACCEPT

iptables -C INPUT -p tcp --dport 5000 -j DROP 2>/dev/null || \
    iptables -A INPUT -p tcp --dport 5000 -j DROP

# Save iptables rules
echo "💾 Saving firewall rules..."
netfilter-persistent save

# Display current rules
echo ""
echo "=========================================="
echo "✅ Firewall Configuration Complete!"
echo "=========================================="
echo ""
echo "📋 Current firewall rules for HTTP/HTTPS:"
iptables -L INPUT -n -v | grep -E "dpt:(80|443|5000)" || echo "No rules found"
echo ""
echo "🔓 Open ports:"
echo "  ✅ Port 80 (HTTP) - Redirects to HTTPS"
echo "  ✅ Port 443 (HTTPS) - Main dashboard access"
echo "  🔒 Port 5000 (Flask) - Blocked from external, localhost only"
echo ""
echo "🌐 Access URLs:"
echo "  Internal: https://192.168.122.53"
echo "  External: https://177.136.226.231 (after NAT configuration)"
echo ""
echo "⚠️  NEXT STEPS:"
echo "  1. Configure NAT on external firewall (provider):"
echo "     - Forward 177.136.226.231:80 -> 192.168.122.53:80"
echo "     - Forward 177.136.226.231:443 -> 192.168.122.53:443"
echo "  2. Restart Flask application"
echo "  3. Test access from external network"
echo ""

