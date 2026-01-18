# Slack Integration for DUPLICATAS Inválidas

## 📋 Overview

This script automatically detects invalid DUPLICATA records and sends notifications to Slack with an Excel report attached.

## 🔧 Configuration

### 1. Set up Slack Webhook

1. Go to your Slack workspace
2. Navigate to **Apps** → **Incoming Webhooks**
3. Click **Add to Slack**
4. Choose the channel where you want to receive notifications
5. Copy the **Webhook URL**

### 2. Update Configuration File

Edit `slack_config.json`:

```json
{
  "slack": {
    "webhook_url": "https://hooks.slack.com/services/YOUR/WEBHOOK/URL",
    "channel": "#alerts",
    "username": "BMA Alert Bot"
  }
}
```

Replace `YOUR/WEBHOOK/URL` with your actual webhook URL.

## 🚀 Features

### 1. **Duplicate Detection**
- Tracks records sent today using a hash-based system
- Prevents sending the same records multiple times
- Tracking file: `.tracking_YYYY-MM-DD.json`

### 2. **Automatic Cleanup**
- Removes Excel files from previous days
- Removes old tracking files
- Keeps only today's files

### 3. **Slack Notification**
The notification includes:
- **Quantidade**: Number of invalid records
- **Data**: Current date and time (YYYY-MM-DD HH:MM:SS)
- **Relatório**: Path to the Excel file

Example message:
```
DUPLICATAS com formato inválido
Quantidade: 541
Data: 2026-01-16 17:42:48
Relatório: Arquivo Excel gerado em `/path/to/file.xlsx`
```

### 4. **Excel Report**
Generated file includes:
- GERENTE
- PROPOSTA
- DATA_OPERACAO
- EMPRESA
- DUPLICATA
- DUPLICATA_CORRETA (suggested correction)
- VALOR
- ID_PRODUTO
- NFE
- MOTIVO_INVALIDO (error reason in Portuguese)

## 📁 File Structure

```
slack_integration_alerts/
├── duplicatas_invalidas/
│   ├── .tracking_2026-01-16.json          # Tracking file (hidden)
│   └── duplicatas_invalidas_20260116_174248.xlsx  # Excel report
├── slack_config.json                       # Slack configuration
├── test_query.py                          # Main script
└── README_SLACK.md                        # This file
```

## 🔄 Workflow

1. **Query Database** → Fetch today's APR data
2. **Validate DUPLICATA** → Check format against NFE
3. **Cleanup** → Remove old files
4. **Check Duplicates** → Skip if already sent today
5. **Export Excel** → Create report with corrections
6. **Send Slack** → Notify team
7. **Mark as Sent** → Update tracking file

## ⚙️ Running the Script

```bash
# Activate virtual environment
source .venv/bin/activate

# Run the script
python slack_integration_alerts/test_query.py
```

## 📊 Output Example

```
==================================================================
Cleaning up old files...
  Removed old file: duplicatas_invalidas_20260115_120000.xlsx
  Removed old tracking: .tracking_2026-01-15.json
✓ Cleaned up 2 old file(s)

Exporting invalid records to Excel...
✓ Excel file created: /path/to/duplicatas_invalidas_20260116_174248.xlsx
  Total invalid records exported: 541

Sending Slack notification...
✓ Slack notification sent successfully!

✓ Records marked as sent for today
==================================================================
```

## 🔍 Troubleshooting

### Slack notification not working?
- Check if `webhook_url` is correctly configured in `slack_config.json`
- Verify the webhook URL is valid and active
- Check network connectivity

### Records not being tracked?
- Check if `.tracking_YYYY-MM-DD.json` file exists
- Verify file permissions in `duplicatas_invalidas/` folder

### Old files not being cleaned?
- Files are only removed if they're from a different date
- Check file naming format: `duplicatas_invalidas_YYYYMMDD_HHMMSS.xlsx`

## 📝 Notes

- Tracking is date-based (resets daily)
- Excel files are automatically cleaned up each day
- Slack notification is optional (script works without it)
- If Slack webhook is not configured, the script will skip notification but still create the Excel file

## 🔐 Security

- Keep `slack_config.json` secure (contains webhook URL)
- Do not commit webhook URLs to version control
- Consider using environment variables for sensitive data

