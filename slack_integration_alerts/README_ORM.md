# SQLAlchemy ORM Models for BMA Database

This document describes the SQLAlchemy ORM models for the BMA FIDC SQL Server database.

## Overview

The `models.py` file contains three ORM models that map to SQL Server tables:

1. **APRTitulos** - Maps to `APR_TITULOS` table
2. **APRCapa** - Maps to `APR_capa` table  
3. **Cedente** - Maps to `cedente` table

## Installation

Make sure SQLAlchemy is installed:

```bash
.venv/bin/python3 -m pip install sqlalchemy pymssql
```

Or install from requirements.txt:

```bash
.venv/bin/python3 -m pip install -r requirements.txt
```

## Usage

### Basic Setup

```python
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from models import Base, APRTitulos, APRCapa, Cedente
import json

# Load database configuration
with open('databases_config.json', 'r') as f:
    config = json.load(f)
    cfg = config['databases']['mssql']

# Create engine
connection_string = (
    f"mssql+pymssql://{cfg['user']}:{cfg['password']}"
    f"@{cfg['server']}:{cfg['port']}/{cfg['scheme']}"
)
engine = create_engine(connection_string)

# Create session
Session = sessionmaker(bind=engine)
session = Session()
```

### Query Examples

#### 1. Query APR_TITULOS

```python
from datetime import date

# Get all titles from today
today = date.today()
titulos = session.query(APRTitulos).filter(
    APRTitulos.DATA >= today
).all()

# Get specific title by NUMERO and TITULO
titulo = session.query(APRTitulos).filter(
    APRTitulos.NUMERO == 1,
    APRTitulos.TITULO == '021467-1'
).first()

print(f"Title: {titulo.TITULO}")
print(f"Value: R$ {titulo.VALOR:,.2f}")
print(f"NFE Key: {titulo.NFEChave}")
```

#### 2. Query APR_capa

```python
# Get all proposals from today
capas = session.query(APRCapa).filter(
    APRCapa.DATA >= today
).all()

# Get by cedente
capa = session.query(APRCapa).filter(
    APRCapa.CEDENTE == 'ILHA SOLTEIRA'
).first()

print(f"Proposal: {capa.NUMERO}")
print(f"Manager: {capa.GERENTE}")
print(f"Company: {capa.empresa}")
```

#### 3. Query Cedente

```python
# Get all active cedentes
cedentes = session.query(Cedente).filter(
    Cedente.ativo == True
).all()

# Get by apelido (primary key)
cedente = session.query(Cedente).filter(
    Cedente.apelido == 'EUROPAN'
).first()

print(f"Name: {cedente.nome}")
print(f"CNPJ: {cedente.cnpj}")
print(f"Manager: {cedente.gerente}")
```

#### 4. Joined Queries

```python
# Join APR_TITULOS and APR_capa
results = session.query(
    APRCapa.GERENTE,
    APRTitulos.NUMERO,
    APRCapa.empresa,
    APRTitulos.TITULO,
    APRTitulos.VALOR,
    APRTitulos.id_produto,
    APRTitulos.NFEChave
).join(
    APRCapa,
    (APRTitulos.DATA == APRCapa.DATA) & 
    (APRTitulos.NUMERO == APRCapa.NUMERO)
).filter(
    APRTitulos.DATA >= today,
    APRCapa.DATA >= today,
    APRTitulos.NFEChave != None,
    APRTitulos.NFEChave != ''
).all()

for row in results:
    gerente, numero, empresa, titulo, valor, id_produto, nfechave = row
    print(f"Manager: {gerente}, Proposal: {numero}")
    print(f"  Title: {titulo}, Value: R$ {valor:,.2f}")
```

#### 5. Join with Cedente

```python
# Join all three tables
results = session.query(
    APRCapa.GERENTE,
    APRTitulos.NUMERO,
    Cedente.nome.label('cedente_nome'),
    Cedente.cnpj,
    APRTitulos.TITULO,
    APRTitulos.VALOR
).join(
    APRCapa,
    (APRTitulos.DATA == APRCapa.DATA) & 
    (APRTitulos.NUMERO == APRCapa.NUMERO)
).join(
    Cedente,
    APRCapa.CEDENTE == Cedente.apelido
).filter(
    APRTitulos.DATA >= today
).all()
```

### Closing the Session

Always close the session when done:

```python
session.close()
```

## Table Schemas

### APR_TITULOS

**Primary Key (Composite):**
- DATA (datetime)
- NUMERO (int)
- TIPO (tinyint)
- TITULO (varchar(14))
- BANCO (varchar(3))
- AGENCIA (varchar(5))
- CONTA (varchar(10))
- CHEQUE (varchar(6))

**Important Columns:**
- VALOR (decimal(18,2)) - Title value
- NFEChave (varchar(44)) - NFE key (44 characters)
- VENCIMENTO (datetime) - Due date
- CNPJ (varchar(14)) - Debtor CNPJ
- NOME (varchar(50)) - Debtor name
- id_produto (int) - Product ID

### APR_capa

**Primary Key (Composite):**
- DATA (datetime)
- NUMERO (int)

**Important Columns:**
- CEDENTE (varchar(20)) - Cedente nickname (FK to cedente.apelido)
- GERENTE (varchar(10)) - Manager name
- empresa (varchar(10)) - Company name
- BORDERO (int) - Bordero number
- TAXA (decimal(18,2)) - Rate
- QTD_PROPOSTOS (int) - Quantity proposed
- VLR_PROPOSTOS (decimal(18,2)) - Value proposed
- QTD_APROVADOS (int) - Quantity approved
- VLR_APROVADOS (decimal(18,2)) - Value approved

### cedente

**Primary Key:**
- apelido (varchar(20)) - Cedente nickname/alias

**Required Columns:**
- nome (varchar(52)) - Full company name
- cnpj (varchar(14)) - Company CNPJ
- simples (bit) - Simples Nacional flag
- ativo (bit) - Active flag
- internet (bit) - Internet access flag
- reapresenta (bit) - Re-presentation flag
- cedentefidc (int) - FIDC cedente flag
- tipocedente (int) - Cedente type
- ID (int) - Unique ID

**Important Optional Columns:**
- endereco (varchar(70)) - Address
- cidade (varchar(20)) - City
- uf (char(2)) - State
- cep (char(8)) - ZIP code
- gerente (varchar(10)) - Manager name
- fone (varchar(30)) - Phone
- email1-5 (varchar(50)) - Email addresses
- l_valor (decimal(18,2)) - Limit value
- tdesc (decimal(18,2)) - Discount rate
- txserv (decimal(18,2)) - Service rate

## Testing

Run the test script to verify the ORM models:

```bash
.venv/bin/python3 slack_integration_alerts/test_orm_models.py
```

This will:
1. Connect to the database
2. Query APR_TITULOS for today's records
3. Query APR_capa for today's records
4. Query active cedentes
5. Perform a joined query between APR_TITULOS and APR_capa

## Schema Inspection

To inspect the database schema and get detailed column information:

```bash
.venv/bin/python3 slack_integration_alerts/get_table_schemas.py
```

This will display:
- All columns for each table
- Data types with precision/length
- Nullable flags
- Default values
- Primary key columns

## Integration with Existing Code

The ORM models can be used alongside the existing raw SQL queries. For example, you can replace the raw SQL in `test_query.py` with ORM queries:

**Before (Raw SQL):**
```python
query = """
SELECT
    c.Gerente AS GERENTE,
    t.NUMERO AS PROPOSTA,
    c.empresa AS EMPRESA,
    t.TITULO AS DUPLICATA,
    t.VALOR,
    t.id_produto AS ID_PRODUTO,
    CAST(SUBSTRING(t.NFECHAVE, 26, 9) AS INT) AS NFE
FROM APR_TITULOS t WITH (NOLOCK)
INNER JOIN APR_capa c WITH (NOLOCK)
    ON t.NUMERO = c.numero
WHERE t.DATA >= CAST(GETDATE() AS DATE)
"""
cursor.execute(query)
results = cursor.fetchall()
```

**After (ORM):**
```python
from datetime import date
from sqlalchemy import cast, Integer, func
from models import APRTitulos, APRCapa

today = date.today()

results = session.query(
    APRCapa.GERENTE,
    APRTitulos.NUMERO.label('PROPOSTA'),
    APRCapa.empresa.label('EMPRESA'),
    APRTitulos.TITULO.label('DUPLICATA'),
    APRTitulos.VALOR,
    APRTitulos.id_produto.label('ID_PRODUTO'),
    cast(func.substring(APRTitulos.NFEChave, 26, 9), Integer).label('NFE')
).join(
    APRCapa,
    (APRTitulos.DATA == APRCapa.DATA) &
    (APRTitulos.NUMERO == APRCapa.NUMERO)
).filter(
    APRTitulos.DATA >= today,
    APRCapa.DATA >= today,
    APRTitulos.NFEChave != None,
    APRTitulos.NFEChave != ''
).all()
```

## Benefits of Using ORM

1. **Type Safety** - Column types are defined in Python
2. **Auto-completion** - IDEs can provide better code completion
3. **Relationships** - Easy to define and navigate table relationships
4. **Query Building** - Programmatic query construction
5. **Portability** - Easier to switch databases if needed
6. **Validation** - Can add custom validation logic to models

## Notes

- All models use the existing database schema without modifications
- Composite primary keys are properly defined
- Nullable columns are correctly mapped
- Default values from the database are preserved
- The models are read-only by default (no INSERT/UPDATE/DELETE operations defined)

## Files

- `models.py` - ORM model definitions
- `test_orm_models.py` - Test script demonstrating ORM usage
- `get_table_schemas.py` - Schema inspection utility
- `README_ORM.md` - This documentation file

