# APR Invalid Records Checker - Standalone Executable

## Quick Start

### Production Mode (Default - Sends Slack Alerts)
```bash
./query_apr_invalidos_status
```

### Dry-run Mode (Preview Only - No Alerts Sent)
```bash
./query_apr_invalidos_status --dry-run
```

---

## What This Program Does

Validates APR (Aprovação) records for invalid DUPLICATA and SEUNO fields:

1. **Queries Database** - Fetches APR_CAPA, APR_TITULOS, and CEDENTE data
2. **Applies Status Filter** - Only processes records with status >= "Aguardando Analista"
3. **Validates DUPLICATA** - Checks format: `NFE + separator (- or /) + sequential number`
4. **Validates SEUNO** - Verifies pre-printed number with verification digit algorithm
5. **Exports to Excel** - Creates separate files for invalid DUPLICATA and SEUNO
6. **Sends Slack Alerts** - Uploads Excel files to Slack (production mode only)
7. **Stores Valid Records** - Saves to MariaDB for tracking (production mode only)

---

## Output Files

### Invalid DUPLICATA
- **Location**: `duplicatas_invalidas/duplicatas_invalidas_YYYY-MM-DD_HHMMSS.xlsx`
- **Slack Alert**: 🚨 with red emoji
- **Columns**: GERENTE, PROPOSTA, CEDENTE, RAMO, EMPRESA, STATUS, DUPLICATA, NFE, VALOR, ERROR_REASON

### Invalid SEUNO
- **Location**: `seuno_invalidos/seuno_invalidos_YYYY-MM-DD_HHMMSS.xlsx`
- **Slack Alert**: ⚠️ with yellow emoji
- **Columns**: GERENTE, PROPOSTA, CEDENTE, RAMO, EMPRESA, STATUS, SEUNO, RANGE, VALOR, ERROR_REASON

---

## Status Filter

Only processes records with these statuses (or further in workflow):
1. **Aguardando Analista** ← Minimum required
2. Aguardando análise
3. Em análise
4. Aguardando Aprovação
5. Aprovado
6. Enviado para Assinar
7. Assinado
8. Liberado
9. Finalizado

Records with earlier statuses (e.g., "Rascunho", "Pendente") are excluded.

---

## MariaDB Storage

Valid records are stored in `apr_valid_records` table with:

**Fixed Fields** (never updated):
- DATA, PROPOSTA, CEDENTE, RAMO (composite key)
- GERENTE, EMPRESA

**Dynamic Fields** (updated on each run):
- STATUS, QTD_APROVADOS, VLR_APROVADOS
- VALOR_TITULOS, QTD_TITULOS, update_count

---

## Configuration Files

### databases_config.json
Database connection settings (MSSQL + MariaDB)

### slack_config.json
Slack bot token and channel configuration

**⚠️ Important**: Both files must be in the same directory as the executable!

---

## Dry-run vs Production Mode

| Feature | Production (Default) | Dry-run (--dry-run) |
|---------|----------------------|---------------------|
| Query database | ✅ | ✅ |
| Validate records | ✅ | ✅ |
| Create Excel files | ✅ | ✅ |
| Send Slack alerts | ✅ | ❌ |
| Update MariaDB | ✅ | ❌ |
| Console output | ✅ | ✅ |

---

## Console Output

The program displays:
- Database connection info
- Query progress
- Validation summary (valid/invalid counts)
- Invalid records table (first 50)
- Valid records table (first 100)
- Status breakdown with values
- MariaDB operation results
- File locations

---

## Scheduling with Cron

To run automatically every weekday at 9:00 AM:

```bash
# Edit crontab
crontab -e

# Add this line (production mode is default, no --send flag needed)
0 9 * * 1-5 cd /home/robot/Deploy/slack_integration_alerts && ./query_apr_invalidos_status >> /var/log/apr_invalidos.log 2>&1
```

---

## Troubleshooting

### "Permission denied"
```bash
chmod 755 ./query_apr_invalidos_status
```

### "Configuration file not found"
Ensure `databases_config.json` and `slack_config.json` are in the same directory.

### "Database connection failed"
Check database credentials in `databases_config.json`.

### "Slack notification failed"
1. Verify bot token in `slack_config.json`
2. Check bot has file upload permissions
3. Ensure you're NOT using `--dry-run` flag (production mode is default)

---

## File Cleanup

Old Excel files are automatically removed (keeps only the 5 most recent files in each directory).

---

## Support

For issues or questions, contact the development team.

**Version**: 2026-01-20  
**Binary Size**: 37 MB  
**Python Version**: 3.12.12

