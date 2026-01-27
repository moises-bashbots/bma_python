# MariaDB Integration for Valid APR Records

## Overview

The `query_apr_invalidos_status.py` program now stores valid APR records in a MariaDB table for tracking and historical analysis.

## Database Table: `apr_valid_records`

### Table Structure

```sql
CREATE TABLE apr_valid_records (
    -- Composite unique key fields
    DATA DATE NOT NULL,
    PROPOSTA INT NOT NULL,
    CEDENTE VARCHAR(100) NOT NULL,
    RAMO VARCHAR(100) NOT NULL,

    -- Additional fields
    GERENTE VARCHAR(100),
    EMPRESA VARCHAR(100),
    STATUS VARCHAR(50),
    QTD_APROVADOS INT DEFAULT 0,
    VLR_APROVADOS DECIMAL(15,2) DEFAULT 0.00,
    VALOR_TITULOS DECIMAL(15,2) DEFAULT 0.00,
    QTD_TITULOS INT DEFAULT 0,

    -- Processing status field
    is_processado TINYINT DEFAULT 0 COMMENT 'Processing status: 0=not processed, 1=processed',

    -- Tracking fields
    first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_count INT DEFAULT 0,

    -- Composite primary key
    PRIMARY KEY (DATA, PROPOSTA, CEDENTE, RAMO),

    -- Indexes
    INDEX idx_is_processado (is_processado)
);
```

### Composite Key

The table uses a **composite primary key** consisting of:
- **DATA** - Proposal date
- **PROPOSTA** - Proposal number
- **CEDENTE** - Client name
- **RAMO** - Business sector/rating

This ensures uniqueness per proposal-client-sector combination.

### Key Features

1. **Automatic Timestamps**
   - `first_seen`: Set when record is first inserted
   - `last_updated`: Automatically updated on every change

2. **Update Tracking**
   - `update_count`: Increments each time the record is updated
   - Helps identify frequently changing proposals

3. **Aggregated Data**
   - `VALOR_TITULOS`: Sum of all title values for this proposal
   - `QTD_TITULOS`: Count of titles in this proposal

4. **Status Tracking**
   - Stores current workflow status
   - Can track status changes over time via `last_updated`

## How It Works

### Data Flow

```
MSSQL (APR_CAPA + APR_TITULOS)
    ↓
Query valid records (excluding invalid DUPLICATA/SEUNO)
    ↓
Aggregate by (DATA, PROPOSTA, CEDENTE, RAMO)
    ↓
INSERT or UPDATE in MariaDB
    ↓
Track changes via update_count and timestamps
```

### Insert vs Update Logic

The program uses **UPSERT** (INSERT ... ON DUPLICATE KEY UPDATE):

```sql
INSERT INTO apr_valid_records (...) VALUES (...)
ON DUPLICATE KEY UPDATE
    GERENTE = VALUES(GERENTE),
    STATUS = VALUES(STATUS),
    QTD_APROVADOS = VALUES(QTD_APROVADOS),
    VLR_APROVADOS = VALUES(VLR_APROVADOS),
    VALOR_TITULOS = VALUES(VALOR_TITULOS),
    QTD_TITULOS = VALUES(QTD_TITULOS),
    update_count = update_count + 1
```

- **First run**: Inserts new records (update_count = 0)
- **Subsequent runs**: Updates existing records (update_count increments)

## Usage

### Dry Run (Default)

```bash
python3 query_apr_invalidos_status.py
```

Shows what would be inserted/updated without making changes:

```
[DRY RUN] Would insert/update 20 records in MariaDB
  Sample records (first 5):
    1. DATA=2026-01-20, PROP=3, CEDENTE=HEANLU, STATUS=Aguardando Analista, QTD_TIT=54, VLR=29744.80
    ...
```

### Actual Execution

```bash
python3 query_apr_invalidos_status.py --send
```

Performs actual INSERT/UPDATE operations:

```
✓ MariaDB storage completed:
  - Inserted: 20 new records
  - Updated: 0 existing records
  - Total processed: 20 records
```

## Querying the Data

### View All Records

```sql
SELECT * FROM apr_valid_records
ORDER BY DATA DESC, PROPOSTA;
```

### Find Recently Updated Records

```sql
SELECT DATA, PROPOSTA, CEDENTE, STATUS, update_count, last_updated
FROM apr_valid_records
WHERE last_updated >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY last_updated DESC;
```

### Track Status Changes

```sql
SELECT DATA, PROPOSTA, CEDENTE, STATUS, update_count
FROM apr_valid_records
WHERE update_count > 0
ORDER BY update_count DESC;
```

### Summary by Status

```sql
SELECT 
    STATUS,
    COUNT(*) as count,
    SUM(QTD_APROVADOS) as total_qty_aprovados,
    SUM(VLR_APROVADOS) as total_vlr_aprovados,
    SUM(VALOR_TITULOS) as total_valor_titulos
FROM apr_valid_records
GROUP BY STATUS
ORDER BY total_vlr_aprovados DESC;
```

## Benefits

1. **Historical Tracking**: Track how proposals progress through workflow stages
2. **Change Detection**: Identify proposals with frequent updates
3. **Performance**: Fast queries on indexed fields (STATUS, GERENTE, DATA)
4. **Data Integrity**: Composite key prevents duplicates
5. **Audit Trail**: Timestamps provide complete audit history

## Maintenance

### Table Creation

Run the table creation script:

```bash
python3 create_valid_records_table.py
```

### Cleanup Old Records

```sql
DELETE FROM apr_valid_records
WHERE DATA < DATE_SUB(CURDATE(), INTERVAL 90 DAY);
```

## Error Handling

The program gracefully handles MariaDB errors:

```
⚠ Warning: Could not store to MariaDB: [error message]
  Continuing without MariaDB storage...
```

This ensures the main validation workflow continues even if MariaDB is unavailable.

