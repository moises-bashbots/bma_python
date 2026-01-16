# Binary Build and Deployment Guide

**Tool**: shiv (Python zipapp builder)  
**Target**: Self-contained executable for `get_chats_sync.py`  
**Date**: 2026-01-16

---

## 🔧 Build Process

### Prerequisites

1. **Install shiv** (already done):
   ```bash
   cd /home/robot/Dev/bma
   source .venv/bin/activate
   uv pip install shiv
   ```

2. **Ensure setup.py exists** in `bma_send_whatsapp/`:
   - Defines package metadata
   - Lists dependencies
   - Creates entry point

### Build Command

```bash
cd /home/robot/Dev/bma/bma_send_whatsapp
source ../.venv/bin/activate

# Clean previous build
rm -f get_chats_sync.pyz

# Build new binary
shiv -c get_chats_sync -o get_chats_sync.pyz -e get_chats_sync:main .
```

**Parameters**:
- `-c get_chats_sync`: Console script name
- `-o get_chats_sync.pyz`: Output file name
- `-e get_chats_sync:main`: Entry point (module:function)
- `.`: Current directory (package root)

### Build Output

```
Processing /home/robot/Dev/bma/bma_send_whatsapp
Installing build dependencies...
Building wheel for bma-whatsapp-sync...
Installing collected packages:
  - requests==2.32.5
  - mysql-connector-python==9.5.0
  - charset-normalizer==3.4.4
  - idna==3.11
  - urllib3==2.6.3
  - certifi==2026.1.4
  - bma-whatsapp-sync==1.0.0

Successfully installed!
```

**Result**: `get_chats_sync.pyz` (34 MB)

---

## 📦 Deployment

### Deploy to Production

```bash
# Create deployment directory
mkdir -p /home/robot/Deploy/bma_send_whatsapp

# Copy binary
cp get_chats_sync.pyz /home/robot/Deploy/bma_send_whatsapp/

# Copy configuration files
cp zapi_config.json /home/robot/Deploy/bma_send_whatsapp/
cp ../database/databases_config.json /home/robot/Deploy/bma_send_whatsapp/

# Verify deployment
ls -lh /home/robot/Deploy/bma_send_whatsapp/
```

### Set Permissions

```bash
cd /home/robot/Deploy/bma_send_whatsapp

# Binary executable
chmod 755 get_chats_sync.pyz

# Config files restricted
chmod 600 zapi_config.json
chmod 600 databases_config.json
```

---

## ✅ Testing

### Test Deployed Binary

```bash
cd /home/robot/Deploy/bma_send_whatsapp
./get_chats_sync.pyz
```

**Expected Output**:
```
================================================================================
Z-API Get Chats
================================================================================
URL: https://api.z-api.io/instances/.../chats

Making GET request...
Status Code: 200
✓ Retrieved 10 chats from Z-API

================================================================================
Syncing to Database
================================================================================

================================================================================
Sync Statistics
================================================================================
Total chats:              10
  Inserted (new):         0
  Updated (existing):     10
  Errors:                 0

Groups:                   8
  With cedente_grupo:     8
  Without cedente_grupo:  0
================================================================================

✓ Sync completed successfully!
```

---

## 🔄 Update Process

### When to Rebuild

Rebuild the binary when:
- Code changes in `get_chats_sync.py`
- Dependency version updates
- Bug fixes or new features

### Quick Update

```bash
cd /home/robot/Dev/bma/bma_send_whatsapp
source ../.venv/bin/activate

# Edit code
vim get_chats_sync.py

# Rebuild
rm -f get_chats_sync.pyz
shiv -c get_chats_sync -o get_chats_sync.pyz -e get_chats_sync:main .

# Redeploy
cp get_chats_sync.pyz /home/robot/Deploy/bma_send_whatsapp/

# Test
cd /home/robot/Deploy/bma_send_whatsapp
./get_chats_sync.pyz
```

**Note**: Configuration files don't need to be redeployed unless their structure changes.

---

## 📋 Files

### Development (`/home/robot/Dev/bma/bma_send_whatsapp/`)

```
get_chats_sync.py           # Source code
setup.py                    # Package definition
requirements_binary.txt     # Dependencies list
get_chats_sync.spec         # PyInstaller spec (not used)
get_chats_sync.pyz          # Built binary (local copy)
```

### Deployment (`/home/robot/Deploy/bma_send_whatsapp/`)

```
get_chats_sync.pyz          # Standalone executable
zapi_config.json            # Z-API credentials
databases_config.json       # Database credentials
README.md                   # Usage documentation
sync.log                    # Execution log (created by cron)
```

---

## 🎯 Key Features

### Self-Contained
- All Python dependencies bundled (34 MB)
- No pip install required
- No virtual environment needed
- Only requires Python 3.8+ on system

### Configuration
- Looks for config files in current directory first
- Falls back to development paths
- Clear error messages if config missing

### Portability
- Single executable file
- Can be copied to any Linux x86_64 system
- Works from any directory
- No installation required

---

## 🐛 Troubleshooting

### "Configuration file not found"

**Cause**: Config files not in expected location

**Solution**:
```bash
cd /home/robot/Deploy/bma_send_whatsapp
ls -l *.json  # Verify files exist
```

### "Python not found"

**Cause**: Python not installed or not in PATH

**Solution**:
```bash
which python3
python3 --version  # Should be 3.8+
```

### Binary doesn't execute

**Cause**: Missing execute permission

**Solution**:
```bash
chmod +x get_chats_sync.pyz
```

---

## 📊 Technical Details

### How Shiv Works

1. **Packages Python code** and dependencies into a zip file
2. **Adds shebang** (`#!/usr/bin/env python3`) to make it executable
3. **Extracts to cache** on first run (`~/.shiv/`)
4. **Reuses cache** on subsequent runs
5. **Executes entry point** (`get_chats_sync:main`)

### Cache Location

```
~/.shiv/get_chats_sync.pyz_<hash>/
├── site-packages/
│   ├── get_chats_sync.py
│   ├── requests/
│   ├── mysql/
│   └── ...
└── ...
```

### Dependencies Bundled

- requests==2.32.5
- mysql-connector-python==9.5.0
- charset-normalizer==3.4.4
- idna==3.11
- urllib3==2.6.3
- certifi==2026.1.4

---

## ✅ Verification Checklist

- [x] shiv installed via uv
- [x] setup.py created
- [x] Binary built successfully (34 MB)
- [x] Deployed to /home/robot/Deploy/bma_send_whatsapp/
- [x] Configuration files copied
- [x] Permissions set correctly
- [x] Binary tested and working
- [x] Documentation created

