# HTTPS Deployment Guide - APR Dashboard

## Overview

This guide explains how to deploy the APR Dashboard with HTTPS access using nginx as a reverse proxy.

## Network Configuration

- **Internal VM IP:** 192.168.122.53
- **External IP (NAT):** 177.136.226.231
- **Flask Port:** 5000 (localhost only)
- **nginx Ports:** 80 (HTTP redirect), 443 (HTTPS)

## Deployment Steps

### Step 1: Run nginx SSL Setup

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard
chmod +x setup_nginx_ssl.sh
sudo ./setup_nginx_ssl.sh
```

**This script will:**
- Install nginx and certbot
- Create self-signed SSL certificate (valid for 365 days)
- Configure nginx as reverse proxy
- Set up HTTP to HTTPS redirect
- Enable security headers (HSTS, X-Frame-Options, etc.)

### Step 2: Configure Firewall

```bash
chmod +x configure_firewall.sh
sudo ./configure_firewall.sh
```

**This script will:**
- Open port 80 (HTTP)
- Open port 443 (HTTPS)
- Block external access to port 5000 (Flask)
- Save firewall rules permanently

### Step 3: Restart Flask Application

```bash
# Stop current Flask instance
pkill -f "python.*app.py"

# Start Flask with updated configuration
cd /home/robot/Dev/bma_python
nohup uv run python slack_integration_alerts/dashboard/app.py > /tmp/flask_dashboard.log 2>&1 &
```

### Step 4: Configure External NAT (Provider Firewall)

Contact your network provider to configure NAT forwarding:

**Port Forwarding Rules:**
```
External IP: 177.136.226.231:80  -> Internal IP: 192.168.122.53:80
External IP: 177.136.226.231:443 -> Internal IP: 192.168.122.53:443
```

### Step 5: Test Access

**Internal Access (from VM or local network):**
```bash
curl -k https://192.168.122.53
```

**External Access (after NAT configuration):**
```
https://177.136.226.231
```

## SSL Certificate

### Self-Signed Certificate

The setup script creates a self-signed SSL certificate:

- **Location:** `/etc/nginx/ssl/apr-dashboard.crt`
- **Key:** `/etc/nginx/ssl/apr-dashboard.key`
- **Valid for:** 365 days
- **Common Name:** 177.136.226.231

**⚠️ Browser Warning:** Users will see a security warning because the certificate is self-signed. They need to accept the warning to proceed.

### Upgrading to Trusted Certificate (Optional)

If you have a domain name, you can use Let's Encrypt for a free trusted certificate:

```bash
# Stop nginx
sudo systemctl stop nginx

# Get certificate (replace with your domain)
sudo certbot certonly --standalone -d your-domain.com

# Update nginx configuration
sudo nano /etc/nginx/sites-available/apr-dashboard
# Change ssl_certificate paths to:
# ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
# ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

# Restart nginx
sudo systemctl start nginx
```

## Security Features

### Flask Configuration

The Flask app has been updated with:

```python
app.config['SESSION_COOKIE_SECURE'] = True  # HTTPS only
app.config['SESSION_COOKIE_HTTPONLY'] = True  # XSS protection
app.config['SESSION_COOKIE_SAMESITE'] = 'Lax'  # CSRF protection
app.config['PREFERRED_URL_SCHEME'] = 'https'
```

### nginx Security Headers

- **HSTS:** Forces HTTPS for 1 year
- **X-Frame-Options:** Prevents clickjacking
- **X-Content-Type-Options:** Prevents MIME sniffing
- **X-XSS-Protection:** XSS filter enabled

### Firewall Rules

- Port 5000 blocked from external access
- Only localhost can access Flask directly
- All external traffic goes through nginx

## Troubleshooting

### Check nginx Status

```bash
sudo systemctl status nginx
sudo nginx -t  # Test configuration
```

### Check nginx Logs

```bash
sudo tail -f /var/log/nginx/apr-dashboard-access.log
sudo tail -f /var/log/nginx/apr-dashboard-error.log
```

### Check Flask Logs

```bash
tail -f /tmp/flask_dashboard.log
```

### Check Firewall Rules

```bash
sudo iptables -L -n -v | grep -E "dpt:(80|443|5000)"
```

### Test Internal Connectivity

```bash
# Test Flask directly (from VM)
curl http://localhost:5000

# Test nginx HTTPS (from VM)
curl -k https://192.168.122.53

# Test HTTP redirect
curl -I http://192.168.122.53
```

## Maintenance

### Renew Self-Signed Certificate

The certificate expires after 365 days. To renew:

```bash
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/apr-dashboard.key \
    -out /etc/nginx/ssl/apr-dashboard.crt \
    -subj "/C=BR/ST=SP/L=SaoPaulo/O=BMA FIDC/CN=177.136.226.231"

sudo systemctl restart nginx
```

### Update nginx Configuration

```bash
sudo nano /etc/nginx/sites-available/apr-dashboard
sudo nginx -t
sudo systemctl restart nginx
```

## Access URLs

- **Internal (VM/Local Network):** https://192.168.122.53
- **External (After NAT):** https://177.136.226.231
- **HTTP (Auto-redirects to HTTPS):** http://177.136.226.231

## Support

For issues or questions, check:
1. nginx error logs: `/var/log/nginx/apr-dashboard-error.log`
2. Flask logs: `/tmp/flask_dashboard.log`
3. System logs: `journalctl -u nginx -f`

