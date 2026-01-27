# Deployment Summary - query_apr_capa_status with Message Logging

**Date**: 2026-01-21  
**Status**: ✅ Successfully Built and Deployed with Message Logging Feature

---

## 🎯 What's New

### Message Logging to Database

The binary now includes **automatic WhatsApp message logging** to MariaDB database:

- ✅ All messages are logged to `mensagens_whatsapp` table
- ✅ Complete audit trail with business context
- ✅ Success/failure tracking with API responses
- ✅ Duplicate detection using message hashes
- ✅ Financial details (quantity, value) tracked
- ✅ Error messages captured for troubleshooting

---

## 📦 Deployment Details

### Binary Information
- **File**: `/home/robot/Deploy/bma_send_whatsapp/query_apr_capa_status`
- **Size**: 30 MB
- **Type**: ELF 64-bit LSB executable
- **Platform**: Linux x86_64
- **Build Date**: 2026-01-21 11:45
- **PyInstaller Version**: 6.18.0
- **Python Version**: 3.12.12

### Included Features
1. **APR_CAPA Status Monitoring** - Queries and processes workflow statuses
2. **WhatsApp Messaging** - Sends status updates via Z-API
3. **Database Logging** - Logs all messages to MariaDB (NEW!)
4. **Duplicate Prevention** - File-based and database-based tracking
5. **Time-based Sending** - Respects business hours
6. **Contact Lookup** - Matches cedentes to WhatsApp groups

---

## 🗄️ Database Setup Required

Before running the binary, create the `mensagens_whatsapp` table:

```bash
# Run the migration script
mysql -h <host> -u <user> -p BMA < /home/robot/Dev/bma_python/database/migrations/create_mensagens_whatsapp_table.sql
```

Or manually execute the SQL from the migration file.

---

## 📁 Deployment Structure

```
/home/robot/Deploy/bma_send_whatsapp/
├── query_apr_capa_status          # 30MB executable (NEW BUILD)
├── apr_status_monitor             # 31MB executable (older version)
├── databases_config.json          # Database credentials (MSSQL + MariaDB)
├── whatsapp_config.json          # WhatsApp API config
├── message_tracking/             # Daily tracking files
│   └── sent_messages_YYYYMMDD.json
└── README_QUERY_APR_CAPA_STATUS.md
```

---

## 🚀 Usage

### Run the Binary

```bash
cd /home/robot/Deploy/bma_send_whatsapp
./query_apr_capa_status
```

The program will:
1. Query APR_CAPA for current date
2. Filter for "Aguardando Analista" and "Enviado para Assinar" statuses
3. Lookup WhatsApp contacts for each cedente
4. Send appropriate status messages
5. **Log all messages to database** (NEW!)
6. Track sent messages to prevent duplicates

---

## 📊 What Gets Logged

Each message logged includes:

### Message Details
- Phone number and contact name
- Full message text
- Message type (status_update, alert, notification)
- Send timestamp

### Business Context
- Cedente and grupo
- Proposal date and number
- Borderô number
- Workflow status
- Quantity of receivables
- Total value

### API Response
- HTTP status code
- Full API response (JSON)
- Error messages (if failed)

### Audit Information
- Config file used
- API provider (Z-API)
- Origin (automated)
- Number of send attempts

---

## 🔍 Querying Logged Messages

### View today's messages
```sql
SELECT * FROM mensagens_whatsapp 
WHERE DATE(data_envio) = CURDATE()
ORDER BY data_envio DESC;
```

### Check failed messages
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

---

## 🔄 Rebuild Instructions

To rebuild after code changes:

```bash
cd /home/robot/Dev/bma_python
./bma_send_whatsapp/build_query_apr_capa_status.sh
```

The script automatically:
1. Cleans previous builds
2. Verifies dependencies
3. Builds with PyInstaller
4. Deploys to target directory
5. Copies config files
6. Sets permissions

---

## ✅ Verification

Check deployment:
```bash
ls -lh /home/robot/Deploy/bma_send_whatsapp/query_apr_capa_status
file /home/robot/Deploy/bma_send_whatsapp/query_apr_capa_status
```

Test execution (dry run):
```bash
cd /home/robot/Deploy/bma_send_whatsapp
./query_apr_capa_status
```

---

## 📚 Related Documentation

- **Message Logging**: `/home/robot/Dev/bma_python/bma_send_whatsapp/MESSAGE_LOGGING_README.md`
- **Build Instructions**: `/home/robot/Dev/bma_python/bma_send_whatsapp/BUILD_INSTRUCTIONS.md`
- **User Guide**: `/home/robot/Deploy/bma_send_whatsapp/README_QUERY_APR_CAPA_STATUS.md`
- **Migration Script**: `/home/robot/Dev/bma_python/database/migrations/create_mensagens_whatsapp_table.sql`

---

## 🎉 Success!

The binary has been successfully built and deployed with the new message logging feature!

