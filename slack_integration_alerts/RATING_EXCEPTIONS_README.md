# Rating Exceptions Configuration

This document explains how to configure rating exceptions for the `send_rating_vadu` program.

## Overview

The `rating_exceptions.json` file allows you to specify cedentes that should always use a specific rating, regardless of their RAMO or QTD_TITULOS values.

## Configuration File

**Location:** `rating_exceptions.json` (same directory as the binary)

**Format:**
```json
{
  "sintetico_cedentes": {
    "description": "Cedentes that should ALWAYS use SINTÉTICO rating regardless of RAMO or QTD_TITULOS",
    "cedentes": [
      "ALIANCA",
      "ANOTHER_CEDENTE"
    ]
  }
}
```

## How It Works

### Priority Order

The rating selection follows this priority:

1. **🥇 SINTÉTICO Exceptions (from config)** → Always SINTÉTICO
2. **🥈 QTD_TITULOS >= 700** → SINTÉTICO
3. **🥉 RAMO-based** → Use RAMO (with cleanup)

### Example

If `rating_exceptions.json` contains:
```json
{
  "sintetico_cedentes": {
    "cedentes": ["ALIANCA", "EXAMPLE"]
  }
}
```

Then:
- **ALIANCA** proposals → Always use **SINTÉTICO** rating
- **EXAMPLE** proposals → Always use **SINTÉTICO** rating
- **Other cedentes** → Use normal logic (QTD >= 700 or RAMO-based)

## Adding New Cedentes

To add a new cedente to the SINTÉTICO exceptions list:

1. **Edit the configuration file:**
   ```bash
   cd /home/robot/Deploy/slack_integration_alerts
   nano rating_exceptions.json
   ```

2. **Add the cedente name to the list:**
   ```json
   {
     "sintetico_cedentes": {
       "cedentes": [
         "ALIANCA",
         "NEW_CEDENTE_NAME"
       ]
     }
   }
   ```

3. **Save the file** (Ctrl+O, Enter, Ctrl+X in nano)

4. **No rebuild needed!** The program loads this file at runtime.

## Important Notes

- ✅ **Case-insensitive:** ALIANCA, alianca, Alianca all work
- ✅ **Runtime loading:** Changes take effect immediately (no rebuild needed)
- ✅ **Error handling:** If file is missing or invalid, program continues with empty list
- ✅ **Logging:** Program logs which cedentes are loaded from config

## Testing

To test the configuration:

```bash
cd /home/robot/Dev/bma_python/slack_integration_alerts
python3 test_alianca_rating.py
```

This will verify that the rating logic works correctly with the current configuration.

## Deployment

The configuration file must be deployed alongside the binary:

```bash
# Development
/home/robot/Dev/bma_python/slack_integration_alerts/rating_exceptions.json

# Production
/home/robot/Deploy/slack_integration_alerts/rating_exceptions.json
```

## Log Output

When the program runs, you'll see:

```
✓ Loaded 2 cedente(s) with SINTÉTICO exception: ALIANCA, EXAMPLE
```

When processing a proposal with an exception cedente:

```
⭐ ALIANCA is in SINTÉTICO exceptions - using SINTÉTICO rating (from config)
```

## Troubleshooting

**Problem:** Cedente not using SINTÉTICO rating

**Solutions:**
1. Check cedente name spelling in `rating_exceptions.json`
2. Verify the file is in the same directory as the binary
3. Check the program logs for "Loaded X cedente(s)" message
4. Ensure JSON syntax is valid (use a JSON validator)

**Problem:** File not found warning

**Solution:**
```bash
# Copy from development to production
cp /home/robot/Dev/bma_python/slack_integration_alerts/rating_exceptions.json \
   /home/robot/Deploy/slack_integration_alerts/
```

