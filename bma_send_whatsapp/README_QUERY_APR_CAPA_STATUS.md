# Query APR CAPA Status - Standalone Binary

**Standalone executable for querying APR_CAPA status and sending WhatsApp notifications**

Built with: `uv` + `PyInstaller`  
Date: 2026-01-19

---

## 📋 Overview

This standalone binary queries the APR_CAPA table for records with specific statuses and automatically sends WhatsApp notifications to the corresponding cedente groups.

### Features

- ✅ Queries APR_CAPA for current date
- ✅ Filters for "Aguardando Analista" and "Enviado para Assinar" statuses
- ✅ Looks up WhatsApp contacts from MariaDB
- ✅ Sends automated status messages via Z-API
- ✅ Prevents duplicate messages with tracking system
- ✅ Time-based message sending restrictions
- ✅ All dependencies embedded (no installation required)

---

## 🚀 Quick Start

### Running the Binary

```bash
cd /home/robot/Deploy/bma_send_whatsapp
./query_apr_capa_status
```

The program will:
1. Query APR_CAPA for today's records
2. Filter for target statuses
3. Look up WhatsApp contacts
4. Send appropriate messages (if within allowed hours)
5. Track sent messages to prevent duplicates

---

## ⚙️ Configuration

### Required Configuration Files

The binary expects these files in the same directory:

#### 1. `databases_config.json`

Contains database connection settings for both MSSQL and MariaDB:

```json
{
  "databases": {
    "mssql": {
      "server": "your-server",
      "port": 1433,
      "database": "BMA",
      "username": "your-username",
      "password": "your-password"
    },
    "mariadb": {
      "server": "your-server",
      "port": 3306,
      "scheme": "BMA",
      "user": "your-username",
      "password": "your-password"
    }
  }
}
```

#### 2. `whatsapp_config.json`

Contains Z-API credentials:

```json
{
  "api_host": "api.z-api.io",
  "instance_id": "your-instance-id",
  "token": "your-token",
  "client_token": "your-client-token",
  "endpoint_path": "/instances/{instance_id}/token/{token}/send-text"
}
```

---

## 📊 How It Works

### 1. Database Query

Queries APR_CAPA joined with CADASTRO_STATUS_FLUXO and Cedente tables:

```sql
SELECT csf.NOME_STATUS, ac.*, c.grupo
FROM APR_CAPA ac
JOIN CADASTRO_STATUS_FLUXO csf ON ac.status_atual = csf.NOME_STATUS
LEFT JOIN cedente c ON ac.CEDENTE = c.apelido
WHERE ac.DATA = CURRENT_DATE
  AND ac.status_atual IN ('Aguardando Analista', 'Enviado para Assinar')
```

### 2. WhatsApp Contact Lookup

For each record, looks up the WhatsApp group in MariaDB:
- First tries to match by CEDENTE name
- Falls back to GRUPO name if no match
- Sends alert if no contact or multiple contacts found

### 3. Message Sending

Sends status-specific messages:

**"Aguardando Analista"** (6h-16h):
```
Olá,
Seu borderô com X recebíveis, no valor total proposto de R$ Y.YY
foi recebido com sucesso e encontra-se em análise 🔎
Em breve daremos um retorno.
Obrigada (o). ✨
```

**"Enviado para Assinar"** (6h-17h):
```
Olá,
O borderô XXXXX, com X recebíveis, no valor total de R$ Y.YY
está disponível para assinatura.
Obrigada (o). 😊
🖋️Assine aqui:
https://portal.qcertifica.com.br/Authentication/Login.aspx
```

### 4. Duplicate Prevention

- Generates unique message ID based on: cedente + status + date + numero
- Stores sent message IDs in `message_tracking/sent_messages_YYYYMMDD.json`
- Skips messages already sent today

---

## 🕐 Time Restrictions

Messages are only sent during specific hours:

| Status | Allowed Hours |
|--------|---------------|
| Aguardando Analista | 6:00 - 16:00 |
| Enviado para Assinar | 6:00 - 17:00 |

Outside these hours, the program will skip sending messages but still display the records.

---

## 📁 Directory Structure

```
/home/robot/Deploy/bma_send_whatsapp/
├── query_apr_capa_status          # Standalone binary (29MB)
├── databases_config.json           # Database credentials
├── whatsapp_config.json           # Z-API credentials
└── message_tracking/              # Tracking directory
    └── sent_messages_YYYYMMDD.json
```

---

## 🔧 Building from Source

If you need to rebuild the binary:

```bash
cd /home/robot/Dev/bma_python
./bma_send_whatsapp/build_query_apr_capa_status.sh
```

### Build Requirements

- Python 3.12+
- `uv` package manager
- PyInstaller 6.18.0+
- Dependencies: pymssql, sqlalchemy, mysql-connector-python, requests

---

## 🐛 Troubleshooting

### Configuration file not found

**Error**: `databases_config.json not found`

**Solution**: Ensure config files are in the same directory as the binary.

### Database connection errors

**Error**: Connection timeout or authentication failed

**Solution**: 
- Verify database credentials in `databases_config.json`
- Check network connectivity to database servers
- Ensure firewall allows connections

### WhatsApp API errors

**Error**: Failed to send message

**Solution**:
- Verify Z-API credentials in `whatsapp_config.json`
- Check that the Z-API instance is active
- Verify phone numbers are in correct format

### No WhatsApp contact found

**Warning**: `No WhatsApp contact found for CEDENTE_NAME`

**Solution**: Add the cedente to the `contato_whatsapp` table in MariaDB.

---

## 📦 Embedded Dependencies

All dependencies are embedded in the binary:

- Python 3.12.12
- SQLAlchemy 2.0+ (ORM)
- pymssql 2.3+ (SQL Server driver)
- mysql-connector-python 9.5+ (MariaDB driver)
- All project modules (models, send_whatsapp)

**No installation required!** Just copy the binary and config files.

---

## 📝 Version Information

- **Build Tool**: PyInstaller 6.18.0
- **Package Manager**: uv
- **Python Version**: 3.12.12
- **Binary Size**: ~29MB
- **Platform**: Linux x86_64

---

## 🔄 Automation

To run automatically via cron:

```bash
# Edit crontab
crontab -e

# Add entry to run every hour during business hours
0 6-17 * * * cd /home/robot/Deploy/bma_send_whatsapp && ./query_apr_capa_status >> query_apr_capa_status.log 2>&1
```

---

## 📞 Support

For issues or questions, contact the development team.

