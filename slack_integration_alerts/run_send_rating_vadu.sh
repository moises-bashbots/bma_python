#!/bin/bash

# Wrapper script for send_rating_vadu binary
# This script sets up the environment for Playwright browsers

# Set Playwright browsers path to use system-installed browsers
export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Run the binary with all arguments passed to this script
exec "$SCRIPT_DIR/send_rating_vadu" "$@"

