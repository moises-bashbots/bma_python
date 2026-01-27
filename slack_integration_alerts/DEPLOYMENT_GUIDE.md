# Deployment Guide - query_apr_invalidos_status

## Overview

This guide explains how to build and deploy the standalone binary for `query_apr_invalidos_status.py`.

---

## Quick Build & Deploy

```bash
cd /home/robot/Dev/bma_python
./slack_integration_alerts/build_query_apr_invalidos_status.sh
```

The script will:
1. ✅ Clean previous builds
2. ✅ Verify all dependencies are installed
3. ✅ Build new executable with PyInstaller
4. ✅ Deploy to `/home/robot/Deploy/slack_integration_alerts/`
5. ✅ Copy configuration files
6. ✅ Set proper permissions
7. ✅ Create output directories

---

## Build Details

### Prerequisites

- Python 3.12+ with virtual environment at `/home/robot/Dev/bma_python/.venv`
- `uv` package manager
- PyInstaller 6.18.0+
- Required packages: pymssql, sqlalchemy, pymysql, openpyxl, requests

### Build Tool

**PyInstaller** is used to create a standalone executable that includes:
- Python interpreter
- All dependencies (SQLAlchemy, pymssql, pymysql, openpyxl, requests)
- Application code (query_apr_invalidos_status.py, models.py)
- Runtime hooks for proper initialization

### Build Configuration

**Spec File**: `slack_integration_alerts/query_apr_invalidos_status.spec`

**Hidden Imports**:
- openpyxl (Excel file generation)
- sqlalchemy (ORM and database dialects)
- pymssql (MSSQL database driver)
- pymysql (MySQL/MariaDB database driver)
- requests (Slack API)

**Output**:
- Single-file executable (~37 MB)
- No external dependencies except configuration files

---

## Deployment Structure

```
/home/robot/Deploy/slack_integration_alerts/
├── query_apr_invalidos_status          # Standalone executable (37 MB)
├── databases_config.json               # Database configuration
├── slack_config.json                   # Slack configuration
├── duplicatas_invalidas/               # Output directory for DUPLICATA reports
│   └── duplicatas_invalidas_*.xlsx
└── seuno_invalidos/                    # Output directory for SEUNO reports
    └── seuno_invalidos_*.xlsx
```

---

## Usage

### Production Mode (Default - Sends Alerts)

```bash
cd /home/robot/Deploy/slack_integration_alerts
./query_apr_invalidos_status
```

**Behavior**:
- ✅ Queries database
- ✅ Validates DUPLICATA and SEUNO
- ✅ Creates Excel files
- ✅ Sends Slack notifications
- ✅ Updates MariaDB

### Dry-run Mode (Preview Only)

```bash
cd /home/robot/Deploy/slack_integration_alerts
./query_apr_invalidos_status --dry-run
```

**Behavior**:
- ✅ Queries database
- ✅ Validates DUPLICATA and SEUNO
- ✅ Creates Excel files
- ❌ Does NOT send Slack notifications
- ❌ Does NOT update MariaDB

---

## Configuration Files

### databases_config.json

Must be in the same directory as the executable. Contains:
- MSSQL connection (APR_CAPA, APR_TITULOS, CEDENTE tables)
- MariaDB connection (apr_valid_records table)

### slack_config.json

Must be in the same directory as the executable. Contains:
- Slack bot token
- Channel IDs for notifications

---

## Rebuilding After Changes

If you modify the source code:

```bash
cd /home/robot/Dev/bma_python
./slack_integration_alerts/build_query_apr_invalidos_status.sh
```

The script will automatically:
- Clean previous builds
- Rebuild the executable
- Redeploy to `/home/robot/Deploy/slack_integration_alerts/`

---

## Binary Details

**Name**: `query_apr_invalidos_status`  
**Size**: ~37 MB  
**Platform**: Linux x86_64  
**Python Version**: 3.12.12  
**PyInstaller Version**: 6.18.0  

**Included Dependencies**:
- SQLAlchemy 2.0.36
- pymssql 2.3.2
- pymysql 1.1.1
- openpyxl 3.1.5
- requests 2.32.3

---

## Troubleshooting

### Binary won't run

Check permissions:
```bash
chmod 755 /home/robot/Deploy/slack_integration_alerts/query_apr_invalidos_status
```

### Configuration files not found

Ensure config files are in the same directory:
```bash
ls -l /home/robot/Deploy/slack_integration_alerts/*.json
```

### Database connection errors

Verify `databases_config.json` has correct credentials and server addresses.

### Slack notifications not sending

1. Check `slack_config.json` has valid bot token
2. Ensure you're using `--send` flag (not dry-run mode)
3. Verify bot has permissions to upload files to the channel

---

## Automation

To run automatically via cron:

```bash
# Add to crontab
0 9 * * 1-5 cd /home/robot/Deploy/slack_integration_alerts && ./query_apr_invalidos_status --send >> /var/log/apr_invalidos.log 2>&1
```

This runs every weekday at 9:00 AM in production mode.

---

## Version History

**2026-01-20**: Initial deployment
- Binary size: 37 MB
- Includes status filter (>= "Aguardando Analista")
- MariaDB update policy (fixed fields: GERENTE, EMPRESA)

