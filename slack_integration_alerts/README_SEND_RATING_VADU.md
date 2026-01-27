# Send Rating to Vadu System

## Overview

`send_rating_vadu.py` is a browser automation script that automates the process of sending rating information to the Vadu system through the GER web interface.

## Features

- ✅ Automated login to GER system
- ✅ Navigation to Proposta (Proposal) section  
- ✅ Selection of rating group from dropdown
- ✅ Headless browser support (required for server environments)
- ✅ Dry-run mode for testing
- ✅ Error handling and logging

## Requirements

- Python 3.12+
- Playwright library
- Chromium browser (installed via Playwright)

## Installation

### 1. Install Playwright

```bash
cd /home/robot/Dev/bma_python
source .venv/bin/activate
uv pip install playwright
```

### 2. Install Chromium Browser

```bash
.venv/bin/python -m playwright install chromium
```

## Usage

### Basic Usage (Production Mode)

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts
../.venv/bin/python3 send_rating_vadu.py
```

### Dry-Run Mode (Testing)

```bash
../.venv/bin/python3 send_rating_vadu.py --dry-run
```

### Specify Rating Group

```bash
../.venv/bin/python3 send_rating_vadu.py --rating-group "RATING A+"
```

## Command-Line Options

| Option | Description | Default |
|--------|-------------|---------|
| `--rating-group` | Rating group to select | `RATING A` |
| `--headless` | Run browser in headless mode | `True` |
| `--no-headless` | Run browser in visible mode (requires X server) | - |
| `--dry-run` | Navigate but don't make changes | `False` |

## Examples

### Test with dry-run

```bash
../.venv/bin/python3 send_rating_vadu.py --dry-run
```

### Send RATING A+ to Vadu

```bash
../.venv/bin/python3 send_rating_vadu.py --rating-group "RATING A+"
```

### Send RATING B to Vadu

```bash
../.venv/bin/python3 send_rating_vadu.py --rating-group "RATING B"
```

## Process Flow

1. **Login**: Navigates to GER system and logs in with credentials
2. **Navigate**: Clicks CRÉDITO menu → Proposta submenu
3. **Select**: Selects the specified rating group from dropdown
4. **Complete**: Closes browser and reports success

## Configuration

The script uses hardcoded credentials:
- **URL**: `https://gercloud2.rgbsys.com.br/ger_bma/`
- **Username**: `MOISES`
- **Password**: `Mo02092022`

To change credentials, edit the constants at the top of `send_rating_vadu.py`:

```python
GER_URL = "https://gercloud2.rgbsys.com.br/ger_bma/"
USERNAME = "MOISES"
PASSWORD = "Mo02092022"
```

## Output

### Successful Execution

```
================================================================================
SEND RATING TO VADU SYSTEM
================================================================================
Rating Group: RATING A
Headless Mode: True
================================================================================

Logging in to GER system...
✓ Navigated to https://gercloud2.rgbsys.com.br/ger_bma/
✓ Entered username: MOISES
✓ Entered password
✓ Clicked login button
✓ Login successful

Navigating to Proposta section...
✓ Clicked CRÉDITO menu
✓ Clicked Proposta submenu
✓ Navigated to Proposta page

Selecting rating group: RATING A...
✓ Clicked rating group dropdown
✓ Selected rating group: RATING A

================================================================================
✓ Process completed successfully!
================================================================================
```

### Dry-Run Mode

```
================================================================================
SEND RATING TO VADU SYSTEM
================================================================================
🔍 DRY RUN MODE - No changes will be made
Rating Group: RATING A
Headless Mode: True
================================================================================

Logging in to GER system...
✓ Navigated to https://gercloud2.rgbsys.com.br/ger_bma/
✓ Entered username: MOISES
✓ Entered password
✓ Clicked login button
✓ Login successful

Navigating to Proposta section...
✓ Clicked CRÉDITO menu
✓ Clicked Proposta submenu
✓ Navigated to Proposta page

[DRY RUN] Would select rating group: RATING A

================================================================================
✓ Process completed successfully!
================================================================================
```

## Troubleshooting

### Error: Missing X server or $DISPLAY

This error occurs when trying to run in non-headless mode on a server without a display.

**Solution**: Always use headless mode (default) or use `xvfb-run`:

```bash
xvfb-run ../.venv/bin/python3 send_rating_vadu.py --no-headless
```

### Error: Timeout

If the page takes too long to load, the script will timeout.

**Solution**: Check network connectivity and GER system availability.

### Error: Element not found

If the page structure has changed, selectors may need to be updated.

**Solution**: Update the selectors in `send_rating_vadu.py`.

## Notes

- The script runs in **headless mode by default** (no visible browser window)
- Headless mode is **required** for server environments without X server
- Use `--dry-run` to test without making actual changes
- The script waits for network idle after each navigation to ensure pages are fully loaded

## Future Enhancements

- [ ] Support for multiple rating groups in one run
- [ ] Configuration file for credentials
- [ ] Screenshot capture on errors
- [ ] Integration with database to fetch rating groups dynamically
- [ ] Slack notifications on completion/errors

