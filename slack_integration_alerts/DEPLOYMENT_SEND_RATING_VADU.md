# Send Rating Vadu - Deployment Guide

## Overview

The `send_rating_vadu` binary automates the process of sending rating information to the Vadu system via the GER web interface. It uses browser automation (Playwright) to log in, navigate to proposals, select ratings, and click through the workflow.

## Build Process

### Building the Binary

To build the binary from source:

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts
bash build_send_rating_vadu.sh
```

This script will:
1. Clean previous builds
2. Build the binary using PyInstaller with the `send_rating_vadu.spec` configuration
3. Deploy the binary to `/home/robot/Deploy/slack_integration_alerts/`
4. Copy configuration files (`databases_config.json`)
5. Copy the wrapper script (`run_send_rating_vadu.sh`)

### Build Output

- **Binary**: `/home/robot/Deploy/slack_integration_alerts/send_rating_vadu` (~72MB)
- **Wrapper Script**: `/home/robot/Deploy/slack_integration_alerts/run_send_rating_vadu.sh`
- **Config**: `/home/robot/Deploy/slack_integration_alerts/databases_config.json`

## Deployment Location

```
/home/robot/Deploy/slack_integration_alerts/
├── send_rating_vadu              # Main binary
├── run_send_rating_vadu.sh       # Wrapper script
└── databases_config.json         # Database configuration
```

## Running the Program

### Basic Usage

```bash
cd /home/robot/Deploy/slack_integration_alerts
DISPLAY=:10.0 ./send_rating_vadu
```

### Using the Wrapper Script

```bash
cd /home/robot/Deploy/slack_integration_alerts
DISPLAY=:10.0 ./run_send_rating_vadu.sh
```

### Command-Line Options

```bash
# Show help
./send_rating_vadu --help

# Run with specific rating group
DISPLAY=:10.0 ./send_rating_vadu --rating-group "RATING B"

# Run in headless mode (no visible browser)
DISPLAY=:10.0 ./send_rating_vadu --headless

# Run in dry-run mode (navigate but don't make changes)
DISPLAY=:10.0 ./send_rating_vadu --dry-run

# Run in continuous loop mode
DISPLAY=:10.0 ./send_rating_vadu --loop --loop-interval 300

# Adjust pause times
DISPLAY=:10.0 ./send_rating_vadu --pause 3 --final-pause 15
```

## Environment Requirements

### Display Server

The program requires an X11 display server. Use `DISPLAY=:10.0` or the appropriate display number for your environment.

### Playwright Browsers

The program uses Playwright for browser automation. Ensure Playwright browsers are installed:

```bash
playwright install chromium
```

The wrapper script sets `PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"` to use system-installed browsers.

## Database Configuration

The program reads database credentials from `databases_config.json`:

```json
{
  "mariadb": {
    "host": "your-host",
    "port": 3306,
    "user": "your-user",
    "password": "your-password",
    "database": "your-database"
  }
}
```

## What the Program Does

1. **Queries Database**: Fetches unprocessed records from `apr_valid_records` table where `is_processado = 0`
2. **Logs into GER**: Authenticates with the GER system
3. **Navigates to Proposals**: Goes to CRÉDITO → Proposta section
4. **Processes Each Proposal**:
   - Opens proposal details (clicks spectacles icon)
   - Selects the appropriate rating from dropdown
   - Checks the button value:
     - If "Processar": Clicks button and executes full workflow (Altera → Grava)
     - If "Reprocessar": Skips clicking (already processed)
   - Marks record as processed in database (`is_processado = 1`)
   - If "Processar" was clicked, also sets `is_bot_processed = 1`

## Database Columns

- **`is_processado`**: Filter column (0 = needs processing, 1 = processed)
- **`is_bot_processed`**: Bot action tracking (0 = bot hasn't clicked Processar, 1 = bot has clicked)

## Logging

The program outputs detailed logs to stdout:
- Database query results
- Browser navigation steps
- Proposal processing status
- Button values and actions taken
- Database update confirmations
- Error messages and stack traces

## Troubleshooting

### Binary Not Found

Ensure the binary is executable:
```bash
chmod +x /home/robot/Deploy/slack_integration_alerts/send_rating_vadu
```

### Display Issues

If you get display errors, verify the DISPLAY variable:
```bash
echo $DISPLAY
# Should output something like :10.0
```

### Database Connection Issues

Verify `databases_config.json` exists and has correct credentials:
```bash
cat /home/robot/Deploy/slack_integration_alerts/databases_config.json
```

### Playwright Browser Issues

Reinstall Playwright browsers:
```bash
playwright install chromium --force
```

## Maintenance

### Rebuilding After Code Changes

After modifying `send_rating_vadu.py` or related files:

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts
bash build_send_rating_vadu.sh
```

The script will automatically rebuild and redeploy the binary.

## Version Information

- **PyInstaller**: 6.18.0
- **Python**: 3.12.12
- **Binary Size**: ~72MB
- **Last Built**: 2026-01-23

