# Dashboard Authentication System

## Overview

The APR Monitoring Dashboard now includes a secure authentication system to protect access to sensitive proposal data.

## Features

✅ **Session-based authentication** using Flask sessions
✅ **Password hashing** with bcrypt (industry-standard)
✅ **Login page** with BMA branding
✅ **Protected API routes** - all endpoints require authentication
✅ **Logout functionality** - secure session termination
✅ **Countdown timer** - shows seconds until next auto-refresh (30s)
✅ **Portuguese interface** - all text in Brazilian Portuguese

## Database Table

### `dashboard_users`

```sql
CREATE TABLE dashboard_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_active (is_active)
);
```

## Default Users

| Username | Password   | Full Name      | Email                  |
|----------|------------|----------------|------------------------|
| admin    | admin123   | Administrador  | admin@bmafidc.com.br   |
| robot    | robot123   | Robot User     | robot@bmafidc.com.br   |

**⚠️ IMPORTANT:** Change these default passwords in production!

## Setup Instructions

### 1. Install Dependencies

```bash
cd /home/robot/Dev/bma_python
uv pip install bcrypt
```

### 2. Create Database Table and Users

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard
uv run python create_auth_users.py
```

This will:
- Create the `dashboard_users` table
- Insert default admin and robot users
- Hash passwords securely with bcrypt

### 3. Start the Dashboard

```bash
cd /home/robot/Dev/bma_python
uv run python slack_integration_alerts/dashboard/app.py
```

### 4. Access the Dashboard

Open your browser and navigate to:
```
http://localhost:5000
```

You'll be redirected to the login page.

## Usage

### Login

1. Navigate to `http://localhost:5000`
2. Enter username and password
3. Click "Entrar" (Login)
4. You'll be redirected to the dashboard

### Logout

Click the "🚪 Sair" button in the top-right corner of the dashboard header.

### Auto-Refresh Countdown

The dashboard shows a countdown timer indicating seconds until the next automatic refresh (every 30 seconds).

## Adding New Users

### Using Python Script

```python
import bcrypt
import pymysql

def hash_password(password: str) -> str:
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

# Connect to database
conn = pymysql.connect(
    host='localhost',
    user='robot',
    password='r0b0t',
    database='BMA'
)

try:
    with conn.cursor() as cursor:
        password_hash = hash_password('newpassword123')
        cursor.execute("""
            INSERT INTO dashboard_users (username, password_hash, full_name, email)
            VALUES (%s, %s, %s, %s)
        """, ('newuser', password_hash, 'New User', 'newuser@bmafidc.com.br'))
        conn.commit()
        print("User created successfully!")
finally:
    conn.close()
```

### Using SQL (Not Recommended)

You cannot insert plain text passwords. You must hash them first using bcrypt.

## Security Features

1. **Password Hashing**: All passwords are hashed using bcrypt with salt
2. **Session Management**: Flask sessions with secure secret key
3. **Session Timeout**: 8 hours of inactivity before automatic logout
4. **Protected Routes**: All dashboard and API routes require authentication
5. **Active User Check**: Only active users (`is_active = TRUE`) can login
6. **Last Login Tracking**: System tracks when users last logged in
7. **HTTP-Only Cookies**: Session cookies are not accessible via JavaScript
8. **CSRF Protection**: SameSite cookie attribute set to 'Lax'

## Files Modified

- `app.py` - Added authentication routes and decorators
- `templates/login.html` - New login page with BMA branding
- `templates/dashboard.html` - Added logout button and countdown timer
- `create_auth_users.py` - Script to create table and default users

## Session Configuration

### Session Timeout

The dashboard is configured with an **8-hour session timeout**. This means:

- Users remain logged in for **8 hours** of inactivity
- After 8 hours without activity, users must login again
- The session timer resets with each page interaction

### Changing Session Timeout

To change the session timeout, edit `app.py`:

```python
# Session configuration
app.config['PERMANENT_SESSION_LIFETIME'] = timedelta(hours=8)  # Change hours here
```

**Examples:**
- 4 hours: `timedelta(hours=4)`
- 12 hours: `timedelta(hours=12)`
- 1 day: `timedelta(days=1)`
- 30 minutes: `timedelta(minutes=30)`

After changing, restart the Flask application.

### Session Security Settings

```python
app.config['SESSION_COOKIE_SECURE'] = False      # Set to True if using HTTPS
app.config['SESSION_COOKIE_HTTPONLY'] = True     # Prevent JavaScript access
app.config['SESSION_COOKIE_SAMESITE'] = 'Lax'    # CSRF protection
```

## Troubleshooting

### "ModuleNotFoundError: No module named 'bcrypt'"

Install bcrypt:
```bash
cd /home/robot/Dev/bma_python
uv pip install bcrypt
```

### "Access denied for user"

Check database credentials in `databases_config.json`

### "Table 'dashboard_users' doesn't exist"

Run the setup script:
```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard
uv run python create_auth_users.py
```

### Session expires immediately

Check that `app.secret_key` is set in `app.py`

## Production Recommendations

1. **Change default passwords** immediately
2. **Use environment variables** for secret key instead of hardcoded value
3. **Enable HTTPS** for encrypted communication
4. **Implement password complexity requirements**
5. **Add password reset functionality**
6. **Implement account lockout** after failed login attempts
7. **Add audit logging** for login attempts
8. **Regular password rotation policy**

