# WhatsApp Message Logging System

## Overview

This system automatically logs all WhatsApp messages sent by the application to a MariaDB database table called `mensagens_whatsapp`. This provides a complete audit trail, duplicate prevention, and business intelligence capabilities.

## Database Table: `mensagens_whatsapp`

### Table Structure

The table tracks comprehensive information about each message:

- **Message Identification**: Unique IDs and hashes for duplicate detection
- **Receiver Information**: Phone number, contact name, group status
- **Message Content**: Full message text and type classification
- **Business Context**: Links to APR_CAPA records (cedente, proposta, borderô, status)
- **Financial Details**: Quantity and value of receivables
- **API Response**: Status codes, full responses, error messages
- **Audit Trail**: Timestamps, user tracking, retry attempts

### Creating the Table

Run the migration script to create the table:

```bash
mysql -h <host> -u <user> -p <database> < database/migrations/create_mensagens_whatsapp_table.sql
```

Or execute the SQL directly in your MariaDB client.

## Components

### 1. ORM Model (`database/bma_models.py`)

The `MensagemWhatsapp` class provides SQLAlchemy ORM access to the table:

```python
from database.bma_models import MensagemWhatsapp
```

### 2. Message Logger (`bma_send_whatsapp/message_logger.py`)

Core logging functionality:

```python
from bma_send_whatsapp.message_logger import log_whatsapp_message

# Log a message
log_whatsapp_message(
    telefone_destino="5511997435829",
    mensagem="Hello, World!",
    tipo_mensagem="notification",
    status_envio="success",
    cedente="ACME Corp",
    status_code=200
)
```

### 3. Enhanced Send Function (`bma_send_whatsapp/send_whatsapp.py`)

The `send_message()` function now automatically logs messages:

```python
from bma_send_whatsapp.send_whatsapp import send_message

# Send and log automatically
result = send_message(
    phone="5511997435829",
    message="Your message here",
    log_to_db=True,  # Default: True
    message_metadata={
        'tipo_mensagem': 'status_update',
        'cedente': 'ACME Corp',
        'status_fluxo': 'Aguardando Analista',
        'qtd_recebiveis': 10,
        'valor_total': 50000.00
    }
)
```

## Message Types

The system supports four message types:

- **`status_update`**: APR_CAPA workflow status notifications
- **`alert`**: System alerts (e.g., missing contacts, errors)
- **`notification`**: General notifications
- **`manual`**: Manually triggered messages

## Features

### ✅ Automatic Logging

All messages sent through `send_message()` are automatically logged to the database with full context.

### ✅ Duplicate Detection

Uses MD5 hashes to prevent duplicate messages from being sent on the same day.

### ✅ Error Tracking

Failed messages are logged with error details for troubleshooting.

### ✅ Business Intelligence

Query the table to analyze:
- Message volume by cedente
- Success/failure rates
- Response times
- Message patterns by status

### ✅ Audit Trail

Complete history of all communications with timestamps and user tracking.

## Example Queries

### Messages sent today
```sql
SELECT * FROM mensagens_whatsapp 
WHERE DATE(data_envio) = CURDATE()
ORDER BY data_envio DESC;
```

### Failed messages
```sql
SELECT cedente, telefone_destino, erro_mensagem, data_envio
FROM mensagens_whatsapp 
WHERE status_envio = 'failed'
ORDER BY data_envio DESC;
```

### Messages by cedente
```sql
SELECT cedente, COUNT(*) as total, 
       SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) as successful
FROM mensagens_whatsapp 
GROUP BY cedente
ORDER BY total DESC;
```

### Messages by status type
```sql
SELECT status_fluxo, COUNT(*) as total, AVG(valor_total) as avg_value
FROM mensagens_whatsapp 
WHERE status_fluxo IS NOT NULL
GROUP BY status_fluxo;
```

## Integration

The logging system is integrated into:

1. **`query_apr_capa_status.py`**: Logs all APR_CAPA status update messages
2. **`send_whatsapp.py`**: Core send function with automatic logging
3. **Future integrations**: Any script using `send_message()` will automatically log

## Configuration

No additional configuration required! The system uses the existing `databases_config.json` for MariaDB connection.

## Troubleshooting

If logging fails, the message will still be sent. Check console output for warnings:

```
⚠️ Failed to log message to database: <error details>
```

To disable logging for a specific message:

```python
send_message(phone, message, log_to_db=False)
```

## Performance

- Logging is non-blocking and won't affect message delivery
- Indexes on key fields ensure fast queries
- JSON field stores full API responses for debugging

