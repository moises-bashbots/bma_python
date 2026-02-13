#!/bin/bash

################################################################################
# Simple wrapper script for send_rating_vadu binary
#
# The binary runs in loop mode and headless mode by default.
# This wrapper just ensures we're in the right directory and runs the binary.
#
# Default behavior:
# - Loop mode: ON (runs continuously)
# - Headless mode: ON (browser runs in background)
# - Loop interval: 5 seconds (between iterations)
#
# To override defaults:
# - Use --no-loop for single run
# - Use --no-headless to see the browser
# - Use --loop-interval N to change interval
################################################################################

# Ensure we're in the correct directory
cd /home/robot/Deploy/slack_integration_alerts || exit 1

# Run the binary directly - no overhead!
# The binary already runs in headless mode, so no need for xvfb-run-safe
exec ./send_rating_vadu "$@"

