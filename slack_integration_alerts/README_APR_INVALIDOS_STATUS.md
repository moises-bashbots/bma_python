# APR Invalid Records Checker - Unified Program

## Overview

This program unifies the functionality of two separate programs:
1. **duplicatas_invalidas** (from `test_query.py`)
2. **seuno_invalidos** (from `query_pre_impresso_with_propostas.py`)

Additionally, it includes the **STATUS** column from the APR_CAPA table.

## Features

✅ **Unified Validation**
- Validates both DUPLICATA and SEUNO in a single run
- Shows which validation failed for each record
- Combines error messages for records with multiple issues

✅ **STATUS Column**
- Includes `status_atual` from APR_CAPA table
- Shows workflow status for each record
- Helps prioritize which invalid records need attention

✅ **Comprehensive Reporting**
- Excel export with all validation details
- Slack notifications with file attachment
- Detailed summary statistics
- Breakdown by error type and status

## Usage

### Run the Program

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts
../.venv/bin/python3 query_apr_invalidos_status.py
```

### Output

The program will:
1. Query APR_CAPA + APR_TITULOS + CEDENTE for today's records
2. Validate DUPLICATA format (NFE-based validation)
3. Validate SEUNO format (pre-impresso range validation)
4. Display summary statistics
5. Export invalid records to Excel
6. Send Slack notification (if configured)
7. Clean up old Excel files

## Validation Rules

### DUPLICATA Validation

Format: `NFE + separator (- or /) + sequential number`

**Valid examples:**
- `021467-1` (for NFE=21467)
- `21467-2` (for NFE=21467)
- `140917/01` (for NFE=140917)

**Invalid examples:**
- `21467` (missing separator and sequential)
- `ABC-1` (doesn't contain NFE)
- `1-21467` (NFE not at beginning)

### SEUNO Validation

**Rules:**
1. SEUNO must start with the pre-impresso RANGE value
2. Position 12 must contain a valid verification digit
3. Verification digit must match calculated value

**Verification digit calculation:**
- Uses algorithm: `[1, 9] + first 11 digits of SEUNO`
- Factor vector: `[2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2]`
- Result can be: `0`, `P`, or `2-9`

## Excel Export

### File Location
`slack_integration_alerts/apr_invalidos/apr_invalidos_YYYY-MM-DD_HHMMSS.xlsx`

### Columns
1. **GERENTE** - Manager name
2. **PROPOSTA** - Proposal number
3. **DATA** - Date
4. **CEDENTE** - Client name
5. **EMPRESA** - Company name
6. **STATUS** - Workflow status (from status_atual)
7. **SEUNO** - SEUNO value
8. **DUPLICATA** - DUPLICATA value
9. **NFE** - NFE number (extracted from NFEChave)
10. **SEUNO_COMPANY** - Pre-impresso company
11. **SEUNO_RANGE** - Pre-impresso range
12. **VERIFICATION_DIGIT** - Calculated verification digit
13. **DUPLICATA_VALID** - SIM/NÃO
14. **SEUNO_VALID** - SIM/NÃO
15. **MOTIVO_INVALIDO** - Error reason(s)

## Slack Notification

### Configuration

Requires `slack_config.json` with:
```json
{
  "slack": {
    "bot_token": "xoxb-...",
    "channel": "#alerts"
  }
}
```

### Message Format

```
🚨 *Alerta de Registros Inválidos*

Encontrados *N* registros com DUPLICATA ou SEUNO inválidos
Data: DD/MM/YYYY
Arquivo anexo com detalhes.
```

## Database Schema

### Tables Queried

1. **APR_CAPA** - Proposal header
   - GERENTE, NUMERO, DATA, CEDENTE, empresa, status_atual

2. **APR_TITULOS** - Proposal line items
   - SEUNO, TITULO (DUPLICATA), NFEChave

3. **cedente** - Client information
   - apelido, obs1 (contains pre-impresso ranges)

### Join Logic

```sql
SELECT 
    APR_CAPA.GERENTE,
    APR_CAPA.NUMERO,
    APR_CAPA.DATA,
    APR_CAPA.CEDENTE,
    APR_CAPA.empresa,
    APR_CAPA.status_atual AS STATUS,
    APR_TITULOS.SEUNO,
    APR_TITULOS.TITULO AS DUPLICATA,
    APR_TITULOS.NFEChave,
    cedente.apelido
FROM APR_CAPA
JOIN APR_TITULOS ON (APR_CAPA.DATA = APR_TITULOS.DATA 
                 AND APR_CAPA.NUMERO = APR_TITULOS.NUMERO)
JOIN cedente ON APR_CAPA.CEDENTE = cedente.apelido
WHERE CAST(APR_CAPA.DATA AS DATE) = TODAY
  AND APR_TITULOS.NFEChave IS NOT NULL
  AND APR_TITULOS.NFEChave <> ''
```

## Comparison with Original Programs

### duplicatas_invalidas (test_query.py)
- ✅ Validates DUPLICATA format
- ❌ Does not validate SEUNO
- ❌ Does not include STATUS column
- Uses raw SQL with pymssql

### seuno_invalidos (query_pre_impresso_with_propostas.py)
- ❌ Does not validate DUPLICATA
- ✅ Validates SEUNO format
- ❌ Does not include STATUS column
- Uses SQLAlchemy ORM

### query_apr_invalidos_status.py (NEW)
- ✅ Validates DUPLICATA format
- ✅ Validates SEUNO format
- ✅ Includes STATUS column
- ✅ Uses SQLAlchemy ORM
- ✅ Unified error reporting
- ✅ Single Excel export with all data

## Error Message Examples

### DUPLICATA Errors
- `DUPLICATA: DUPLICATA vazio ou nulo`
- `DUPLICATA: NFE vazio ou nulo`
- `DUPLICATA: DUPLICATA não contém o número da NFE (21467)`
- `DUPLICATA: Falta separador (- ou /) entre NFE e número sequencial`
- `DUPLICATA: NFE (21467) não está no início da DUPLICATA`

### SEUNO Errors
- `SEUNO: SEUNO vazio ou nulo`
- `SEUNO: SEUNO com menos de 12 caracteres`
- `SEUNO: SEUNO não inicia com a faixa 5183`
- `SEUNO: Dígito verificador inválido na posição 12: 'X'`
- `SEUNO: Dígito verificador incorreto: esperado '7', encontrado '5'`
- `SEUNO: Empresa SANTANDER não possui faixa pré-impressa cadastrada`

### Combined Errors
- `DUPLICATA: Falta separador (- ou /) entre NFE e número sequencial; SEUNO: Dígito verificador incorreto: esperado '7', encontrado '5'`

## File Cleanup

The program automatically removes old Excel files, keeping only files from the current report date.

**Cleanup logic:**
- Scans `apr_invalidos/` directory
- Removes files with different date in filename
- Keeps all files from current date (multiple runs)

## Dependencies

- Python 3.12+
- SQLAlchemy
- pymssql
- openpyxl
- requests

## Configuration Files

### databases_config.json
```json
{
  "databases": {
    "mssql": {
      "server": "200.187.70.21",
      "port": 1433,
      "scheme": "BMA",
      "user": "username",
      "password": "password"
    }
  }
}
```

### slack_config.json (optional)
```json
{
  "slack": {
    "bot_token": "xoxb-your-token",
    "channel": "#alerts"
  }
}
```

## Future Enhancements

Potential improvements:
- [ ] Add command-line arguments for date selection
- [ ] Support for date ranges
- [ ] Historical trend analysis
- [ ] Auto-correction suggestions
- [ ] Integration with ticketing system
- [ ] Email notifications
- [ ] Dashboard/web interface

---

**Version:** 1.0  
**Created:** 2026-01-20  
**Author:** BMA Development Team
