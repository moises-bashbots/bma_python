# Send Rating Vadu - Deployment Guide

## Overview

This document describes how to deploy and run the `send_rating_vadu` standalone binary.

## Files

- `send_rating_vadu` - Standalone executable binary (72 MB)
- `run_send_rating_vadu.sh` - Wrapper script that sets up the environment
- `.env` - Environment variables (database credentials, etc.)

## Deployment Location

```
/home/robot/Deploy/slack_integration_alerts/
├── send_rating_vadu
├── run_send_rating_vadu.sh
└── .env
```

## Prerequisites

### 1. Playwright Browsers

The binary requires Playwright browsers to be installed in the user's home directory:

```bash
# Check if browsers are installed
ls -la ~/.cache/ms-playwright/

# If not installed, run:
cd /home/robot/Dev/bma_python
PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright" .venv/bin/playwright install chromium
```

### 2. Environment Variables

The `.env` file must contain database credentials:

```
MARIADB_HOST=your_host
MARIADB_PORT=3306
MARIADB_USER=your_user
MARIADB_PASSWORD=your_password
MARIADB_DATABASE=your_database

SQLSERVER_HOST=your_host
SQLSERVER_PORT=1433
SQLSERVER_USER=your_user
SQLSERVER_PASSWORD=your_password
SQLSERVER_DATABASE=your_database
```

## Running the Binary

### Option 1: Using the Wrapper Script (Recommended)

The wrapper script automatically sets the `PLAYWRIGHT_BROWSERS_PATH` environment variable:

```bash
cd /home/robot/Deploy/slack_integration_alerts
xvfb-run-safe ./run_send_rating_vadu.sh --pause 2
```

### Option 2: Running Directly

Set the environment variable manually:

```bash
cd /home/robot/Deploy/slack_integration_alerts
PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright" xvfb-run-safe ./send_rating_vadu --pause 2
```

## Command-Line Options

```
--rating-group RATING    Rating group to select (default: RATING A)
--headless               Run browser in headless mode
--no-headless            Run browser in visible mode (default)
--dry-run                Dry run mode - navigate but don't make changes
--pause SECONDS          Pause between steps (default: 2)
--final-pause SECONDS    Final pause before closing (default: 10)
--target-date DATE       Target date (YYYY-MM-DD, default: today)
```

## Examples

### Process today's records with visible browser
```bash
cd /home/robot/Deploy/slack_integration_alerts
xvfb-run-safe ./run_send_rating_vadu.sh --pause 2
```

### Dry run mode (no changes)
```bash
xvfb-run-safe ./run_send_rating_vadu.sh --dry-run --pause 3
```

### Process specific date
```bash
xvfb-run-safe ./run_send_rating_vadu.sh --target-date 2026-01-22 --pause 2
```

### Run in headless mode
```bash
xvfb-run-safe ./run_send_rating_vadu.sh --headless --pause 2
```

## Troubleshooting

### Error: "Executable doesn't exist at /tmp/_MEI.../playwright/driver/..."

**Solution:** Make sure the `PLAYWRIGHT_BROWSERS_PATH` environment variable is set:

```bash
export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
```

Or use the wrapper script which sets this automatically.

### Error: "BrowserType.launch: Executable doesn't exist"

**Solution:** Install Playwright browsers:

```bash
cd /home/robot/Dev/bma_python
PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright" .venv/bin/playwright install chromium
```

### Database connection errors

**Solution:** Check that the `.env` file exists and contains valid credentials:

```bash
cat /home/robot/Deploy/slack_integration_alerts/.env
```

## Rebuilding the Binary

To rebuild the binary from source:

```bash
cd /home/robot/Dev/bma_python
bash slack_integration_alerts/build_and_deploy.sh
```

This will:
1. Install PyInstaller
2. Build the binary
3. Deploy to `/home/robot/Deploy/slack_integration_alerts/`
4. Copy the wrapper script
5. Ensure Playwright browsers are installed

## Notes

- Default headless mode is `False` (browser runs in visible mode)
- The binary is 72 MB in size
- Playwright browsers are stored in `~/.cache/ms-playwright/`
- The binary extracts to a temporary directory on each run

