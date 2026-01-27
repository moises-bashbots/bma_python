# Build Instructions for query_apr_capa_status Binary

**Date**: 2026-01-19  
**Tools**: `uv` + `PyInstaller`  
**Target**: Standalone executable for `query_apr_capa_status.py`

---

## ✅ Build Summary

Successfully created a standalone binary for `query_apr_capa_status.py` using:
- **Package Manager**: `uv` for dependency management
- **Build Tool**: PyInstaller 6.18.0 for creating standalone executable
- **Python Version**: 3.12.12
- **Binary Size**: ~29MB
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
uv pip show pyinstaller pymssql sqlalchemy mysql-connector-python requests
```

### 3. Build the Binary

Simply run the build script:

```bash
cd /home/robot/Dev/bma_python
./bma_send_whatsapp/build_query_apr_capa_status.sh
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
- `bma_send_whatsapp/build/` - Temporary build files
- `bma_send_whatsapp/dist/query_apr_capa_status` - Final binary

**Deployment location**:
- `/home/robot/Deploy/bma_send_whatsapp/query_apr_capa_status` - Deployed binary
- `/home/robot/Deploy/bma_send_whatsapp/databases_config.json` - Database config
- `/home/robot/Deploy/bma_send_whatsapp/whatsapp_config.json` - WhatsApp config
- `/home/robot/Deploy/bma_send_whatsapp/message_tracking/` - Message tracking directory

---

## 📝 Key Implementation Details

### MySQL Pure Python Implementation

To avoid native plugin loading issues in PyInstaller binaries, the code forces mysql-connector-python to use its pure Python implementation:

```python
# Force mysql-connector-python to use pure Python implementation
# This avoids issues with native plugin loading in PyInstaller binaries
import mysql.connector
mysql.connector.HAVE_CEXT = False
```

This prevents the error:
```
Authentication plugin 'mysql_native_password' cannot be loaded:
/usr/lib/mysql/plugin/mysql_native_password.so: cannot open shared object file
```

### Config File Resolution

The binary looks for config files in **its own directory**, not the current working directory:

```python
def get_executable_dir() -> Path:
    """Get the directory where the executable/script is located."""
    if getattr(sys, 'frozen', False):
        # Running as compiled binary
        return Path(sys.executable).parent
    else:
        # Running as script
        return Path(__file__).parent
```

This means you can run the binary from anywhere:

```bash
# Works from any directory!
cd /tmp
/home/robot/Deploy/bma_send_whatsapp/query_apr_capa_status
```

### PyInstaller Spec File

The `query_apr_capa_status.spec` file includes:

**Hidden imports**:
- `pymssql` and submodules
- `mysql.connector` and locales
- `sqlalchemy` dialects (mssql, mysql)
- Project modules (models, send_whatsapp)

**Path configuration**:
- Adds project root, slack_integration_alerts, and database directories to path
- Ensures all modules can be imported correctly

**Excluded packages** (to reduce size):
- matplotlib, numpy, pandas, PIL, tkinter, pytest, setuptools

---

## 🔄 Rebuilding After Changes

If you modify the source code:

```bash
cd /home/robot/Dev/bma_python

# Edit the source file
vim bma_send_whatsapp/query_apr_capa_status.py

# Rebuild
./bma_send_whatsapp/build_query_apr_capa_status.sh
```

The build script automatically:
- Cleans previous builds
- Rebuilds from source
- Redeploys to `/home/robot/Deploy/bma_send_whatsapp/`

---

## 📦 Embedded Dependencies

All dependencies are embedded in the binary:

**Database drivers**:
- pymssql 2.3+ (SQL Server)
- mysql-connector-python 9.5+ (MariaDB)

**ORM & SQL**:
- SQLAlchemy 2.0+

**HTTP & Utilities**:
- requests 2.32+
- Standard library modules

**Project modules**:
- `slack_integration_alerts.models` (APRCapa, CadastroStatusFluxo, Cedente)
- `database.bma_models` (ContatoWhatsapp)
- `bma_send_whatsapp.send_whatsapp` (WhatsApp messaging)

---

## ✅ Verification

Test the binary works correctly:

```bash
# Run from different directory to verify config resolution
cd /tmp
/home/robot/Deploy/bma_send_whatsapp/query_apr_capa_status
```

Expected behavior:
- Finds config files in `/home/robot/Deploy/bma_send_whatsapp/`
- Connects to databases
- Queries APR_CAPA for current date
- Processes WhatsApp notifications

---

## 📚 Related Files

- `query_apr_capa_status.py` - Source script
- `query_apr_capa_status.spec` - PyInstaller spec file
- `build_query_apr_capa_status.sh` - Build script
- `README_QUERY_APR_CAPA_STATUS.md` - User documentation

---

## 🎯 Success Criteria

✅ Binary builds without errors  
✅ Binary size ~29MB  
✅ All dependencies embedded  
✅ Config files loaded from binary's directory  
✅ Can run from any working directory  
✅ Database connections work  
✅ WhatsApp messaging works  
✅ Message tracking works  

---

**Build completed successfully on 2026-01-19**

