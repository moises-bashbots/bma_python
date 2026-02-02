# HTTPS Setup - Quick Reference Card

## 🚀 Quick Deployment (3 Steps)

### 1️⃣ Setup nginx with SSL
```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard
sudo ./setup_nginx_ssl.sh
```

### 2️⃣ Configure Firewall
```bash
sudo ./configure_firewall.sh
```

### 3️⃣ Restart Flask
```bash
pkill -f "python.*app.py"
cd /home/robot/Dev/bma_python
nohup uv run python slack_integration_alerts/dashboard/app.py > /tmp/flask_dashboard.log 2>&1 &
```

## 🌐 Network Configuration

| Type | IP Address | Ports |
|------|------------|-------|
| **Internal VM** | 192.168.122.53 | 80, 443 |
| **External (NAT)** | 177.136.226.231 | 80, 443 |
| **Flask (localhost)** | 127.0.0.1 | 5000 |

## 🔒 Firewall Rules

| Port | Protocol | Access | Purpose |
|------|----------|--------|---------|
| 80 | TCP | Open | HTTP → HTTPS redirect |
| 443 | TCP | Open | HTTPS dashboard access |
| 5000 | TCP | Localhost only | Flask application |

## 📋 NAT Configuration (Provider Firewall)

Configure these port forwarding rules on your external firewall:

```
177.136.226.231:80  → 192.168.122.53:80
177.136.226.231:443 → 192.168.122.53:443
```

## 🔗 Access URLs

- **Internal:** https://192.168.122.53
- **External:** https://177.136.226.231

## ⚠️ Important Notes

1. **Self-Signed Certificate:** Browser will show security warning
2. **Accept Warning:** Users must accept certificate to proceed
3. **Valid for:** 365 days from installation
4. **Flask must run on:** localhost:5000

## 🔍 Verification Commands

```bash
# Check nginx status
sudo systemctl status nginx

# Check Flask is running
ps aux | grep "python.*app.py"

# Check firewall rules
sudo iptables -L -n -v | grep -E "dpt:(80|443|5000)"

# Test internal HTTPS
curl -k https://192.168.122.53

# View nginx logs
sudo tail -f /var/log/nginx/apr-dashboard-access.log
```

## 🛠️ Troubleshooting

### nginx not starting
```bash
sudo nginx -t  # Test configuration
sudo systemctl restart nginx
```

### Can't access externally
1. Check NAT configuration on provider firewall
2. Verify ports 80 and 443 are open
3. Check nginx logs: `sudo tail -f /var/log/nginx/apr-dashboard-error.log`

### Flask not responding
```bash
tail -f /tmp/flask_dashboard.log
curl http://localhost:5000  # Test Flask directly
```

## 📞 Support

- **nginx logs:** `/var/log/nginx/apr-dashboard-*.log`
- **Flask logs:** `/tmp/flask_dashboard.log`
- **Configuration:** `/etc/nginx/sites-available/apr-dashboard`
- **SSL Certificate:** `/etc/nginx/ssl/apr-dashboard.crt`

