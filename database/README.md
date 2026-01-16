# BMA Database Module

Centralized database configuration, models, and utilities for the BMA project.

## 📁 Directory Structure

```
database/
├── README.md                          # This file
├── __init__.py                        # Python package initialization
├── databases_config.json              # Database connection configuration
│
├── bma_models.py                      # SQLAlchemy ORM models for MariaDB (18 tables)
├── models_mariadb_sacado.py          # MariaDB Sacado models (from cadastro_sacado)
├── models_mssql_sacado.py            # SQL Server Sacado models (from cadastro_sacado)
├── db_connector.py                    # Database connection utilities
│
├── BMA_MODELS_GUIDE.md               # Guide for using bma_models.py
├── MSSQL_CEDENTE_COLUMNS.md          # SQL Server cedente table documentation
├── WHATSAPP_CONTACTS_MAPPING.md      # WhatsApp contacts mapping guide
└── whatsapp_mapping_example.py       # Example mapping code
```

## 🗄️ Database Configuration

### Configuration File: `databases_config.json`

```json
{
  "databases": {
    "mariadb": {
      "server": "localhost",
      "port": 3306,
      "user": "robot",
      "password": "r0b0t",
      "scheme": "BMA"
    },
    "mssql": {
      "server": "200.187.70.21",
      "port": 1433,
      "user": "BMA_AUTOMACAO",
      "password": "Jfx^kO7F41Vi",
      "scheme": "BMA"
    },
    "mssql_write": {
      "server": "200.187.70.21",
      "port": 1433,
      "user": "usr_ai4finance_prod",
      "password": "usr2020",
      "scheme": "BMA"
    }
  }
}
```

## 📊 Available Models

### 1. MariaDB Models (`bma_models.py`)

Complete ORM models for **18 tables**:

#### Core Tables
- `Banco` - Bank information
- `Empresa` - Companies/Enterprises
- `Participante` - Participants (CNPJ registry)
- `Cedente` - Assignors/Creditors
- `Sacado` - Debtors/Payers

#### PIX & Collections
- `CobrancaPix` - PIX collections
- `Pix` - PIX payments
- `StatusCobrancaPix` - Status
- `TipoInstrucao` - Instruction types
- `EmailCobrancaInstrucao` - Email instructions

#### Banking & Operations
- `DadosBancariosEmpresa` - Banking data
- `OperacaoRecompra` - Repurchase operations
- `TituloRecompra` - Repurchase titles

#### Other
- `Critica` - Issues/Criticisms
- `TipoCritica` - Issue types
- `Solicitacao` - Requests
- `ContatoWhatsapp` - WhatsApp contacts
- `EventoTransporte` - Transport events

### 2. SQL Server Models

#### Cedente Table (`MSSQL_CEDENTE_COLUMNS.md`)
- **359 columns** - Comprehensive assignor/creditor information
- Key fields: `apelido`, `nome`, `cnpj`, `grupo`, `ativo`

## 🚀 Usage Examples

### MariaDB Connection

```python
from database.bma_models import Sacado, create_session

# Query debtors
with create_session() as session:
    sacados = session.query(Sacado).filter(
        Sacado.completo == False
    ).limit(10).all()
    
    for sacado in sacados:
        print(f"{sacado.cnpj} - {sacado.nome}")
```

### SQL Server Connection

```python
import pymssql
import json
from pathlib import Path

# Load config
config_path = Path(__file__).parent / "databases_config.json"
with open(config_path, 'r') as f:
    config = json.load(f)

cfg = config['databases']['mssql']

# Connect
conn = pymssql.connect(
    server=cfg['server'],
    port=cfg['port'],
    user=cfg['user'],
    password=cfg['password'],
    database=cfg['scheme']
)

cursor = conn.cursor()
cursor.execute("SELECT TOP 10 apelido, nome FROM cedente WHERE ativo = 1")
for row in cursor.fetchall():
    print(f"{row[0]} - {row[1]}")

cursor.close()
conn.close()
```

### Using db_connector.py

```python
from database.db_connector import get_mariadb_connection, get_mssql_connection

# MariaDB
mariadb_conn = get_mariadb_connection()
cursor = mariadb_conn.cursor()
cursor.execute("SELECT * FROM sacado LIMIT 5")
# ... use cursor ...
mariadb_conn.close()

# SQL Server
mssql_conn = get_mssql_connection()
cursor = mssql_conn.cursor()
cursor.execute("SELECT TOP 5 * FROM cedente")
# ... use cursor ...
mssql_conn.close()
```

## 📚 Documentation

### Guides
- **[BMA_MODELS_GUIDE.md](BMA_MODELS_GUIDE.md)** - Complete guide for MariaDB ORM models
- **[MSSQL_CEDENTE_COLUMNS.md](MSSQL_CEDENTE_COLUMNS.md)** - SQL Server cedente table reference
- **[WHATSAPP_CONTACTS_MAPPING.md](WHATSAPP_CONTACTS_MAPPING.md)** - WhatsApp contacts mapping

### Examples
- **[whatsapp_mapping_example.py](whatsapp_mapping_example.py)** - Z-API to database mapping

## 🔧 Dependencies

Required Python packages (already in centralized `.venv`):
- `sqlalchemy` - ORM for MariaDB
- `mysql-connector-python` - MariaDB driver
- `pymssql` - SQL Server driver

## 🎯 Best Practices

1. **Always use the centralized config**: Load from `database/databases_config.json`
2. **Use context managers**: Ensure connections are properly closed
3. **Use ORM when possible**: Prefer SQLAlchemy models over raw SQL
4. **Handle errors gracefully**: Wrap database calls in try/except blocks
5. **Use connection pooling**: For production applications

## 📝 Notes

- All database files are now centralized in this folder
- Configuration files are copied (not moved) to preserve originals in subprojects
- Model files from `cadastro_sacado` are included for reference
- Use the centralized virtual environment at `/home/robot/Dev/bma/.venv`

