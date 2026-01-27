# mensagens_whatsapp Table - Quick Reference

## ✅ Table Created Successfully!

**Database**: BMA  
**Table**: mensagens_whatsapp  
**Status**: Active and ready to use  
**Created**: 2026-01-21

---

## 📊 Quick Access Commands

### Connect to Database
```bash
mysql -h localhost -P 3306 -u robot -pr0b0t BMA
```

### View Table Structure
```sql
DESCRIBE mensagens_whatsapp;
```

### Count Messages
```sql
SELECT COUNT(*) FROM mensagens_whatsapp;
```

### View Latest Messages
```sql
SELECT * FROM mensagens_whatsapp ORDER BY data_envio DESC LIMIT 10;
```

---

## 🔍 Common Queries

### Today's Messages
```sql
SELECT 
    data_envio,
    cedente,
    telefone_destino,
    tipo_mensagem,
    status_envio,
    LEFT(mensagem, 50) as preview
FROM mensagens_whatsapp 
WHERE DATE(data_envio) = CURDATE()
ORDER BY data_envio DESC;
```

### Failed Messages
```sql
SELECT 
    data_envio,
    cedente,
    telefone_destino,
    erro_mensagem
FROM mensagens_whatsapp 
WHERE status_envio = 'failed'
ORDER BY data_envio DESC;
```

### Messages by Cedente
```sql
SELECT 
    cedente,
    COUNT(*) as total,
    SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) as successful
FROM mensagens_whatsapp 
GROUP BY cedente
ORDER BY total DESC;
```

### Success Rate
```sql
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) as successful,
    SUM(CASE WHEN status_envio = 'failed' THEN 1 ELSE 0 END) as failed,
    ROUND(SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate
FROM mensagens_whatsapp;
```

---

## 📋 Table Columns

| Column | Type | Description |
|--------|------|-------------|
| id_mensagem | BIGINT | Primary key (auto-increment) |
| message_id | VARCHAR(255) | Unique message ID |
| message_hash | VARCHAR(32) | MD5 hash for duplicate detection |
| data_envio | TIMESTAMP | When message was sent |
| data_atualizacao | TIMESTAMP | Last update timestamp |
| telefone_destino | VARCHAR(50) | Recipient phone number |
| nome_contato | VARCHAR(255) | Contact name |
| is_group | BOOLEAN | Whether sent to group |
| mensagem | TEXT | Full message text |
| tipo_mensagem | ENUM | status_update, alert, notification, manual |
| cedente | VARCHAR(100) | Cedente name |
| grupo | VARCHAR(100) | Grupo name |
| data_proposta | DATE | Proposal date |
| numero_proposta | INT | Proposal number |
| bordero | VARCHAR(100) | Borderô number |
| status_fluxo | VARCHAR(50) | Workflow status |
| qtd_recebiveis | INT | Quantity of receivables |
| valor_total | DECIMAL(15,2) | Total value |
| status_envio | ENUM | success, failed, pending |
| status_code | INT | HTTP status code |
| api_response | JSON | Full API response |
| erro_mensagem | TEXT | Error message if failed |
| config_file | VARCHAR(255) | Config file used |
| api_provider | VARCHAR(50) | API provider (default: Z-API) |
| usuario | VARCHAR(100) | User who triggered |
| origem | VARCHAR(100) | Origin (default: automated) |
| tentativas_envio | INT | Number of attempts |

---

## 🎯 Indexes

The table has optimized indexes on:
- telefone_destino
- cedente
- data_envio
- status_envio
- message_hash
- (data_proposta, numero_proposta) - composite
- status_fluxo
- tipo_mensagem

---

## 🚀 How Messages Are Logged

Messages are automatically logged when:
1. Running `/home/robot/Deploy/bma_send_whatsapp/query_apr_capa_status`
2. Using `send_message()` function with `log_to_db=True` (default)
3. Any script that imports and uses the WhatsApp sender

---

## 📁 Related Files

- **Migration Script**: `/home/robot/Dev/bma_python/database/migrations/create_mensagens_whatsapp_table.sql`
- **Useful Queries**: `/home/robot/Dev/bma_python/database/migrations/useful_queries_mensagens_whatsapp.sql`
- **ORM Model**: `/home/robot/Dev/bma_python/database/bma_models.py` (MensagemWhatsapp class)
- **Logger Module**: `/home/robot/Dev/bma_python/bma_send_whatsapp/message_logger.py`
- **Documentation**: `/home/robot/Dev/bma_python/bma_send_whatsapp/MESSAGE_LOGGING_README.md`

---

## ✅ Verification

To verify the table is working:

```bash
# Check table exists
mysql -h localhost -P 3306 -u robot -pr0b0t BMA -e "SHOW TABLES LIKE 'mensagens_whatsapp';"

# Check structure
mysql -h localhost -P 3306 -u robot -pr0b0t BMA -e "DESCRIBE mensagens_whatsapp;"

# Check if any messages logged
mysql -h localhost -P 3306 -u robot -pr0b0t BMA -e "SELECT COUNT(*) FROM mensagens_whatsapp;"
```

---

## 🎉 Ready to Use!

The table is now active and will automatically start logging messages when you run the WhatsApp sender binary!

