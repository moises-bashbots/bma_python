# BMA - Brazilian Financial Automation System

Automated financial operations system for BMA FIDC with database integration, WhatsApp messaging, and web automation.

## Virtual Environment Setup

This project uses a **single centralized virtual environment** with Python 3.12 managed by `uv`.

All subprojects share the same virtual environment located at `/home/robot/Dev/bma/.venv`.

### Quick Start

```bash
# From the root directory (/home/robot/Dev/bma)
source activate.sh

# Or manually
source .venv/bin/activate

# From any subdirectory
source ../.venv/bin/activate
```

### Dependencies

All dependencies for all projects are installed in the shared virtual environment:

- **Database Connectors**: `pymssql`, `mysql-connector-python`, `sqlalchemy`
- **HTTP Clients**: `requests`
- **WhatsApp API**: `infobip-api-python-client`
- **Build Tools**: `pyinstaller`

### Installing New Dependencies

```bash
# Add to requirements.txt, then:
uv pip install -r requirements.txt
```

## Projects

### 1. slack_integration_alerts/

Database query tools for APR data.

**Run:**
```bash
cd slack_integration_alerts
python query_apr_data.py
```

**Features:**
- Queries APR_TITULOS and APR_capa tables
- Fetches today's data from MSSQL database
- Configuration in `databases_config.json`

### 2. bma_send_whatsapp/

WhatsApp messaging integration via Z-API.

**Run:**
```bash
cd bma_send_whatsapp
python get_chats.py
```

**Features:**
- Get WhatsApp chats via Z-API
- Configuration in `zapi_config.json`

### 3. bma_consulta_sacado/

OAuth2 client for Assertiva Soluções API.

**Features:**
- Debtor information lookup
- OAuth2 authentication

### 4. cadastro_sacado/

Database connector for MariaDB and MSSQL.

**Features:**
- Sync enriched data between databases
- Sacado (debtor) registration management

## Configuration Files

- `databases_config.json` - Database connection parameters (MSSQL, MariaDB)
- `zapi_config.json` - Z-API WhatsApp credentials
- `requirements.txt` - Python dependencies

## Database Connections

### MSSQL
- **Server**: 200.187.70.21:1433
- **Database**: BMA
- **User**: BMA_AUTOMACAO (read-only)
- **User**: usr_ai4finance_prod (write)

### MariaDB
- **Server**: localhost:3306
- **Database**: BMA
- **User**: robot

## Development

### Python Version
Python 3.12.12 (managed by uv)

### Virtual Environment Location
`.venv/` in the project root

### Running Tests
```bash
# Activate environment first
source .venv/bin/activate

# Run specific project scripts
cd <project_directory>
python <script_name>.py
```

## Notes

- **Single Virtual Environment**: All projects share `.venv` in the root directory (144MB total)
- **No Duplicate Environments**: All subfolder venvs have been removed to save disk space
- **Use `uv`**: Fast dependency management with `uv pip install`
- **Configuration Files**: Contain sensitive credentials (not for public repos)
- **Python 3.12**: Managed by `uv` for consistency across all projects

