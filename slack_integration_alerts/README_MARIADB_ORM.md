# MariaDB ORM Models - APRValidRecord

## Overview

This document describes the SQLAlchemy ORM model for the MariaDB `apr_valid_records` table.

## Model: APRValidRecord

The `APRValidRecord` model maps to the `apr_valid_records` table in MariaDB and provides an object-oriented interface for querying and updating valid APR records.

### File Location

- **Model Definition**: `slack_integration_alerts/models_mariadb.py`
- **Test Script**: `slack_integration_alerts/test_mariadb_orm.py`

## Table Structure

### Composite Primary Key

- `DATA` (Date) - Proposal date
- `PROPOSTA` (Integer) - Proposal number
- `CEDENTE` (String) - Client name
- `RAMO` (String) - Business sector/rating

### Data Fields

- `GERENTE` (String) - Manager name
- `EMPRESA` (String) - Company name
- `STATUS` (String) - Current workflow status
- `QTD_APROVADOS` (Integer) - Quantity of approved titles
- `VLR_APROVADOS` (Decimal) - Total approved value
- `VALOR_TITULOS` (Decimal) - Sum of current title values
- `QTD_TITULOS` (Integer) - Count of titles

### Processing Status Field (NEW)

- **`is_processado`** (SmallInteger/TINYINT) - Processing status flag
  - `0` = Not processed (default)
  - `1` = Processed

### Tracking Fields

- `first_seen` (Timestamp) - When record was first inserted
- `last_updated` (Timestamp) - Last update timestamp
- `update_count` (Integer) - Number of times record was updated

## Usage Examples

### Basic Setup

```python
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from models_mariadb import MariaDBBase, APRValidRecord
import json

# Load configuration
with open('databases_config.json', 'r') as f:
    config = json.load(f)

# Create engine
db_config = config['databases']['mariadb']
connection_string = (
    f"mysql+pymysql://{db_config['user']}:{db_config['password']}"
    f"@{db_config['server']}:{db_config['port']}/{db_config['scheme']}"
    f"?charset=utf8mb4"
)
engine = create_engine(connection_string)

# Create session
Session = sessionmaker(bind=engine)
session = Session()
```

### Query Unprocessed Records

```python
# Get all unprocessed records
unprocessed = session.query(APRValidRecord).filter(
    APRValidRecord.is_processado == 0
).all()

for record in unprocessed:
    print(f"Proposta {record.PROPOSTA} - {record.CEDENTE}")
    print(f"  Status: {record.STATUS}")
    print(f"  Processed: {record.is_processed}")  # Boolean property
```

### Query Processed Records

```python
# Get all processed records
processed = session.query(APRValidRecord).filter(
    APRValidRecord.is_processado == 1
).all()
```

### Mark Record as Processed

```python
# Find a record
record = session.query(APRValidRecord).filter(
    APRValidRecord.DATA == date(2026, 1, 20),
    APRValidRecord.PROPOSTA == 3
).first()

# Mark as processed (Method 1: using helper method)
record.mark_as_processed()
session.commit()

# Mark as processed (Method 2: using property)
record.is_processed = True
session.commit()

# Mark as processed (Method 3: direct field access)
record.is_processado = 1
session.commit()
```

### Mark Record as Unprocessed

```python
# Mark as unprocessed (Method 1: using helper method)
record.mark_as_unprocessed()
session.commit()

# Mark as unprocessed (Method 2: using property)
record.is_processed = False
session.commit()
```

### Count Records by Status

```python
from sqlalchemy import func

# Count unprocessed records
unprocessed_count = session.query(func.count(APRValidRecord.PROPOSTA)).filter(
    APRValidRecord.is_processado == 0
).scalar()

# Count processed records
processed_count = session.query(func.count(APRValidRecord.PROPOSTA)).filter(
    APRValidRecord.is_processado == 1
).scalar()

print(f"Unprocessed: {unprocessed_count}")
print(f"Processed: {processed_count}")
```

### Query by Multiple Criteria

```python
# Get unprocessed records for a specific manager
records = session.query(APRValidRecord).filter(
    APRValidRecord.is_processado == 0,
    APRValidRecord.GERENTE == 'AMANDA',
    APRValidRecord.STATUS == 'Enviado para Assinar'
).all()
```

## Helper Methods and Properties

### Boolean Property: `is_processed`

The model provides a convenient boolean property that wraps the `is_processado` field:

```python
# Read
if record.is_processed:
    print("Record has been processed")

# Write
record.is_processed = True  # Sets is_processado to 1
record.is_processed = False  # Sets is_processado to 0
```

### Helper Methods

```python
# Mark as processed
record.mark_as_processed()  # Sets is_processado to 1

# Mark as unprocessed
record.mark_as_unprocessed()  # Sets is_processado to 0
```

## Update Policy

The `is_processado` field follows a special update policy:

- ✅ **Set on INSERT**: New records default to `is_processado = 0`
- ❌ **NOT updated on DUPLICATE KEY UPDATE**: When existing records are updated by `query_apr_invalidos_status.py`, the `is_processado` field is **NOT** reset
- ✅ **Managed separately**: External processes can update this field independently

This ensures that once a record is marked as processed, it remains processed even when other fields (STATUS, QTD_APROVADOS, etc.) are updated.

## Testing

Run the test script to verify the ORM model:

```bash
cd slack_integration_alerts
../.venv/bin/python3 test_mariadb_orm.py
```

The test script demonstrates:
- Counting records by processing status
- Querying unprocessed records
- Querying processed records
- Marking records as processed (dry-run mode)

## Migration

To add the `is_processado` column to an existing table:

```bash
cd slack_integration_alerts
../.venv/bin/python3 add_is_processado_column.py
```

This script:
1. Checks if the column already exists
2. Adds the column with default value 0
3. Creates an index on the column
4. Verifies the column was added correctly

## See Also

- `MARIADB_INTEGRATION.md` - Overview of MariaDB integration
- `MARIADB_UPDATE_POLICY.md` - Field update policy documentation
- `models_mariadb.py` - ORM model definition
- `test_mariadb_orm.py` - Test script with usage examples

