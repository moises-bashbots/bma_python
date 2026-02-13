#!/bin/bash

################################################################################
# Optimized wrapper script for send_rating_vadu binary
#
# OPTIMIZATION: Uses Python's built-in --loop mode instead of bash loop
# This allows the Python process to reuse the Playwright browser instance
# across iterations, significantly reducing startup overhead.
#
# Benefits:
# - Faster iterations (no Python process restart)
# - Lower CPU usage (no repeated binary loading)
# - Persistent Playwright browser (reused across iterations)
# - Cleaner process management
#
# The Python binary will:
# - Create Playwright browser once
# - Run multiple iterations with the same browser
# - Handle its own error recovery and restarts
################################################################################

# Configuration
LOGFILE="/home/robot/Deploy/slack_integration_alerts/send_rating.log"
TARGET="./send_rating_vadu"
LOOP_INTERVAL=5  # Seconds between iterations
RATING_GROUP="RATING A"

# Force unbuffered output for Python
export PYTHONUNBUFFERED=1

# Ensure we're in the correct directory
cd /home/robot/Deploy/slack_integration_alerts || exit 1

echo "=" | tee -a "$LOGFILE"
echo "Starting send_rating_vadu in optimized loop mode" | tee -a "$LOGFILE"
echo "Loop interval: ${LOOP_INTERVAL} seconds" | tee -a "$LOGFILE"
echo "Rating group: ${RATING_GROUP}" | tee -a "$LOGFILE"
echo "Log file: ${LOGFILE}" | tee -a "$LOGFILE"
echo "=" | tee -a "$LOGFILE"
echo "" | tee -a "$LOGFILE"

# Run with xvfb-run-safe and use Python's built-in loop mode
# The --loop flag tells the Python binary to run continuously
# The --headless flag runs the browser in headless mode
# stdbuf forces line-buffered output for real-time logging
xvfb-run-safe stdbuf -oL -eL "$TARGET" \
    --loop \
    --headless \
    --loop-interval "$LOOP_INTERVAL" \
    --rating-group "$RATING_GROUP" \
    2>&1 | tee -a "$LOGFILE"

# If we get here, the process exited (shouldn't happen in loop mode)
echo "" | tee -a "$LOGFILE"
echo "=" | tee -a "$LOGFILE"
echo "send_rating_vadu process exited unexpectedly" | tee -a "$LOGFILE"
echo "Exit code: $?" | tee -a "$LOGFILE"
echo "=" | tee -a "$LOGFILE"

