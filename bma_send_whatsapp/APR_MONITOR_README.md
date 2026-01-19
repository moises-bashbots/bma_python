# APR Status Monitor - Standalone Executable

## Overview

The APR Status Monitor is a standalone executable that queries the APR_CAPA database for records with specific statuses and sends WhatsApp notifications to relevant contacts.

## Features

- ✅ **Standalone Binary**: Single executable file with all dependencies embedded
- ✅ **Status Filtering**: Monitors "Aguardando Analista" and "Enviado para Assinar" statuses
- ✅ **WhatsApp Integration**: Sends automated notifications via Z-API
- ✅ **Time-Based Control**: Only sends messages during business hours
- ✅ **Duplicate Prevention**: Tracks sent messages to avoid duplicates
- ✅ **Contact Lookup**: Automatically finds WhatsApp groups for cedentes

## Deployment Location

```
/home/robot/Deploy/bma_send_whatsapp/
├── apr_status_monitor          # Standalone executable (31MB)
├── databases_config.json       # Database connection settings
├── zapi_config.json           # WhatsApp API credentials
└── message_tracking/          # Daily tracking files
```

## Configuration Files

### 1. databases_config.json

Contains connection settings for both MSSQL and MariaDB databases:

```json
{
  "databases": {
    "mssql": {
      "server": "200.187.70.21",
      "port": 1433,
      "database": "BMA",
      "username": "your_username",
      "password": "your_password"
    },
    "mariadb": {
      "host": "localhost",
      "port": 3306,
      "user": "your_user",
      "password": "your_password",
      "database": "BMA"
    }
  }
}
```

### 2. zapi_config.json

Contains Z-API credentials for WhatsApp messaging:

```json
{
  "instance_id": "your_instance_id",
  "token": "your_token",
  "client_token": "your_client_token",
  "api_host": "api.z-api.io",
  "endpoint_path": "/instances/{instance_id}/token/{token}/send-text"
}
```

## Usage

### Basic Usage

The monitor automatically queries records for the **current date** (today):

```bash
cd /home/robot/Deploy/bma_send_whatsapp
./apr_status_monitor
```

The executable will:
1. Get the current system date
2. Query APR_CAPA records for that date
3. Filter by status ("Aguardando Analista" and "Enviado para Assinar")
4. Send WhatsApp notifications if within allowed hours
5. Track sent messages to prevent duplicates

## Business Rules

### Status Filtering

The monitor only processes records with these statuses:
- **Aguardando Analista**: Operation being analyzed
- **Enviado para Assinar**: Operation sent for signature

### Time Windows

Messages are only sent during specific hours:
- **Aguardando Analista**: 06:00 - 16:00 (6 AM - 4 PM)
- **Enviado para Assinar**: 06:00 - 17:00 (6 AM - 5 PM)

Outside these hours, the monitor will skip sending messages but still display the query results.

### Duplicate Prevention

The monitor tracks sent messages daily to prevent duplicates:
- Each message gets a unique ID based on: cedente + status + date + numero
- Tracking files are stored in `message_tracking/sent_messages_YYYYMMDD.json`
- Messages are only sent once per day per unique record

### Contact Lookup Logic

1. First tries to match `CEDENTE` against `cedente_grupo` in contato_whatsapp table
2. If no match, tries to match `GRUPO` against `cedente_grupo`
3. Only sends to group chats (`isGroup = 1`)
4. If no contact found or multiple contacts found, sends alert to +55 31 3448 3014

## Message Templates

### Aguardando Analista

```
🔔 *Operação em Análise*

Cedente: [CEDENTE_NAME]
Borderô: [BORDERO_NUMBER]
Títulos Aprovados: [QTD_APROVADOS]
Valor Aprovado: R$ [VLR_APROVADOS]

Status: Aguardando análise do analista
Portal: https://portal.qcertifica.com.br/Authentication/Login.aspx
```

### Enviado para Assinar

```
✅ *Operação Enviada para Assinatura*

Cedente: [CEDENTE_NAME]
Borderô: [BORDERO_NUMBER]
Títulos Aprovados: [QTD_APROVADOS]
Valor Aprovado: R$ [VLR_APROVADOS]

Status: Enviado para assinatura
Portal: https://portal.qcertifica.com.br/Authentication/Login.aspx
```

## Building from Source

To rebuild the executable:

```bash
cd /home/robot/Dev/bma_python
./bma_send_whatsapp/build_apr_monitor.sh
```

The build script will:
1. Clean previous builds
2. Verify dependencies
3. Build the executable with PyInstaller
4. Deploy to `/home/robot/Deploy/bma_send_whatsapp/`
5. Copy configuration files
6. Set proper permissions

## Troubleshooting

### Binary won't execute

```bash
chmod 755 /home/robot/Deploy/bma_send_whatsapp/apr_status_monitor
```

### Configuration file not found

Ensure `databases_config.json` and `zapi_config.json` are in the same directory as the executable.

### Database connection errors

Check the database credentials in `databases_config.json` and verify network connectivity.

### WhatsApp API errors

Verify the Z-API credentials in `zapi_config.json` and ensure the instance is active.

## Dependencies (Embedded)

All dependencies are embedded in the executable:
- Python 3.12.12
- SQLAlchemy (ORM)
- pymssql (SQL Server driver)
- mysql-connector-python (MariaDB driver)
- All project modules and models

## Version Information

- **Build Tool**: PyInstaller 6.18.0
- **Python Version**: 3.12.12
- **Binary Size**: ~31MB
- **Platform**: Linux x86_64

## Support

For issues or questions, contact the development team.

