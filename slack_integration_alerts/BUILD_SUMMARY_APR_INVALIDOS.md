# Build Summary - query_apr_invalidos_status Binary

**Date**: 2026-01-20  
**Tools**: `uv` + `PyInstaller`  
**Target**: Standalone executable for `query_apr_invalidos_status.py`

---

## ✅ Build Summary

Successfully created a standalone binary for `query_apr_invalidos_status.py` using:
- **Package Manager**: `uv` for dependency management
- **Build Tool**: PyInstaller 6.18.0 for creating standalone executable
- **Python Version**: 3.12.12
- **Binary Size**: ~37 MB
- **Platform**: Linux x86_64

---

## 🔧 Build Process

### 1. Prerequisites

Ensure you have:
- Python 3.12+ installed
- `uv` package manager installed
- Virtual environment activated at `/home/robot/Dev/bma_python/.venv`

### 2. Install Dependencies

All required dependencies should already be installed via `uv`:

```bash
cd /home/robot/Dev/bma_python
source .venv/bin/activate

# Verify critical packages
uv pip show pyinstaller pymssql sqlalchemy pymysql openpyxl requests
```

### 3. Build the Binary

Simply run the build script:

```bash
cd /home/robot/Dev/bma_python
./slack_integration_alerts/build_query_apr_invalidos_status.sh
```

The script will:
1. Clean previous builds
2. Verify dependencies
3. Build executable with PyInstaller
4. Create deployment directory
5. Deploy binary and config files
6. Verify deployment

### 4. Build Output

**Build artifacts**:
- `slack_integration_alerts/build/` - Temporary build files
- `slack_integration_alerts/dist/query_apr_invalidos_status` - Final binary

**Deployment location**:
- `/home/robot/Deploy/slack_integration_alerts/query_apr_invalidos_status`

---

## 📦 Deployment Structure

```
/home/robot/Deploy/slack_integration_alerts/
├── query_apr_invalidos_status          # Standalone executable (37 MB)
├── databases_config.json               # Database configuration
├── slack_config.json                   # Slack configuration
├── duplicatas_invalidas/               # Output directory
├── seuno_invalidos/                    # Output directory
├── README_APR_INVALIDOS.md             # User guide
└── DEPLOYMENT_INFO_APR_INVALIDOS.txt   # Quick reference
```

---

## 🚀 Usage

### Dry-run Mode (Default)
```bash
cd /home/robot/Deploy/slack_integration_alerts
./query_apr_invalidos_status
```

### Production Mode
```bash
cd /home/robot/Deploy/slack_integration_alerts
./query_apr_invalidos_status --send
```

---

## 🔑 Key Features

The binary includes:

1. **DUPLICATA Validation** - Format: `NFE + separator (- or /) + sequential`
2. **SEUNO Validation** - Verification digit algorithm
3. **Status Filter** - Only processes records >= "Aguardando Analista"
4. **Excel Export** - Separate files for invalid DUPLICATA and SEUNO
5. **Slack Integration** - Uploads Excel files with notifications
6. **MariaDB Storage** - Stores valid records with update policy
7. **Dry-run Mode** - Safe preview before sending alerts
8. **Auto Cleanup** - Removes old Excel files

---

## 📋 Configuration Files

The binary requires two configuration files in the same directory:

1. **databases_config.json** - MSSQL and MariaDB connection settings
2. **slack_config.json** - Slack bot token and channel configuration

These files are automatically copied during the build process.

---

## 🔄 Rebuilding After Changes

If you modify the source code:

```bash
cd /home/robot/Dev/bma_python
./slack_integration_alerts/build_query_apr_invalidos_status.sh
```

The script will automatically rebuild and redeploy the binary.

---

## 📊 Binary Details

**Name**: `query_apr_invalidos_status`  
**Size**: ~37 MB  
**Platform**: Linux x86_64  
**Python**: 3.12.12  
**PyInstaller**: 6.18.0  

**Included Dependencies**:
- SQLAlchemy 2.0.36
- pymssql 2.3.2
- pymysql 1.1.1
- openpyxl 3.1.5
- requests 2.32.3

---

## 🎯 PyInstaller Spec File

The `query_apr_invalidos_status.spec` file includes:

**Hidden imports**:
- openpyxl (Excel generation)
- sqlalchemy (ORM and dialects)
- pymssql (MSSQL driver)
- pymysql (MySQL/MariaDB driver)
- requests (Slack API)

**Data files**:
- models.py (ORM models)

**Optimizations**:
- Single-file executable
- UPX compression enabled
- Console mode (shows output)

---

## 📝 Files Created

### Source Directory (`slack_integration_alerts/`)
- `query_apr_invalidos_status.spec` - PyInstaller spec file
- `build_query_apr_invalidos_status.sh` - Build script
- `DEPLOYMENT_GUIDE.md` - Deployment documentation
- `BUILD_SUMMARY_APR_INVALIDOS.md` - This file
- `README_APR_INVALIDOS_DEPLOYMENT.md` - User guide template

### Deployment Directory (`/home/robot/Deploy/slack_integration_alerts/`)
- `query_apr_invalidos_status` - Standalone executable
- `databases_config.json` - Database configuration
- `slack_config.json` - Slack configuration
- `README_APR_INVALIDOS.md` - User guide
- `DEPLOYMENT_INFO_APR_INVALIDOS.txt` - Quick reference

---

## ✅ Verification

The binary was tested and verified to:
- ✅ Start successfully
- ✅ Connect to database
- ✅ Query APR data
- ✅ Apply status filter
- ✅ Validate DUPLICATA and SEUNO
- ✅ Run in dry-run mode

---

## 🎉 Deployment Complete!

The standalone binary is ready for production use at:
**`/home/robot/Deploy/slack_integration_alerts/query_apr_invalidos_status`**

