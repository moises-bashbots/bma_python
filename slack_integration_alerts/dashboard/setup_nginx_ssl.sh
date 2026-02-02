#!/bin/bash
# Setup nginx reverse proxy with SSL for APR Dashboard
# VM IP: 192.168.122.53
# External IP: 177.136.226.231

set -e

echo "=========================================="
echo "APR Dashboard - HTTPS Setup with nginx"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "Please run as root (use sudo)"
    exit 1
fi

# Install nginx
echo "📦 Installing nginx..."
apt-get update
apt-get install -y nginx

# Install certbot for Let's Encrypt (optional, for domain-based SSL)
echo "📦 Installing certbot (for Let's Encrypt SSL)..."
apt-get install -y certbot python3-certbot-nginx

# Create self-signed certificate for IP-based access
echo "🔐 Creating self-signed SSL certificate..."
mkdir -p /etc/nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/apr-dashboard.key \
    -out /etc/nginx/ssl/apr-dashboard.crt \
    -subj "/C=BR/ST=SP/L=SaoPaulo/O=BMA FIDC/CN=177.136.226.231"

echo "✅ SSL certificate created at /etc/nginx/ssl/"

# Backup default nginx config
if [ -f /etc/nginx/sites-enabled/default ]; then
    echo "📋 Backing up default nginx config..."
    mv /etc/nginx/sites-enabled/default /etc/nginx/sites-enabled/default.backup
fi

# Create nginx configuration
echo "⚙️  Creating nginx configuration..."
cat > /etc/nginx/sites-available/apr-dashboard << 'EOF'
# APR Dashboard - nginx reverse proxy configuration
# Redirect HTTP to HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name 177.136.226.231 192.168.122.53;
    
    # Redirect all HTTP traffic to HTTPS
    return 301 https://$host$request_uri;
}

# HTTPS server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name 177.136.226.231 192.168.122.53;
    
    # SSL certificate (self-signed)
    ssl_certificate /etc/nginx/ssl/apr-dashboard.crt;
    ssl_certificate_key /etc/nginx/ssl/apr-dashboard.key;
    
    # SSL configuration (modern, secure)
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Logging
    access_log /var/log/nginx/apr-dashboard-access.log;
    error_log /var/log/nginx/apr-dashboard-error.log;
    
    # Proxy to Flask application
    location / {
        proxy_pass http://127.0.0.1:5000;
        proxy_http_version 1.1;
        
        # Proxy headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # WebSocket support (if needed in future)
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Buffering
        proxy_buffering off;
    }
    
    # Static files (if any)
    location /static/ {
        alias /home/robot/Dev/bma_python/slack_integration_alerts/dashboard/static/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
EOF

echo "✅ nginx configuration created"

# Enable the site
echo "🔗 Enabling site..."
ln -sf /etc/nginx/sites-available/apr-dashboard /etc/nginx/sites-enabled/apr-dashboard

# Test nginx configuration
echo "🧪 Testing nginx configuration..."
nginx -t

# Restart nginx
echo "🔄 Restarting nginx..."
systemctl restart nginx
systemctl enable nginx

echo ""
echo "=========================================="
echo "✅ HTTPS Setup Complete!"
echo "=========================================="
echo ""
echo "📊 Dashboard URLs:"
echo "  Internal: https://192.168.122.53"
echo "  External: https://177.136.226.231"
echo ""
echo "⚠️  IMPORTANT NOTES:"
echo "  1. Using self-signed certificate (browser will show warning)"
echo "  2. Users need to accept the certificate warning"
echo "  3. Flask app must be running on localhost:5000"
echo "  4. Configure NAT on firewall: 177.136.226.231:443 -> 192.168.122.53:443"
echo ""
echo "🔒 SSL Certificate:"
echo "  Location: /etc/nginx/ssl/apr-dashboard.crt"
echo "  Valid for: 365 days"
echo "  Type: Self-signed"
echo ""
echo "📝 Next steps:"
echo "  1. Open ports 80 and 443 on VM firewall"
echo "  2. Configure NAT on external firewall (provider)"
echo "  3. Update Flask app.py to use SESSION_COOKIE_SECURE=True"
echo ""

