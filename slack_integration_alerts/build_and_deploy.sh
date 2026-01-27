#!/bin/bash

set -e

echo "========================================="
echo "Building send_rating_vadu binary"
echo "========================================="

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DEPLOY_DIR="/home/robot/Deploy/slack_integration_alerts"

echo "Script directory: $SCRIPT_DIR"
echo "Project root: $PROJECT_ROOT"
echo "Deploy directory: $DEPLOY_DIR"

# Activate virtual environment
echo ""
echo "Activating virtual environment..."
source "$PROJECT_ROOT/.venv/bin/activate"

# Install PyInstaller if not already installed
echo ""
echo "Installing PyInstaller..."
pip install pyinstaller

# Clean previous builds
echo ""
echo "Cleaning previous builds..."
cd "$SCRIPT_DIR"
rm -rf build dist __pycache__ *.spec~

# Build the binary
echo ""
echo "Building binary with PyInstaller..."
pyinstaller send_rating_vadu.spec

# Check if build was successful
if [ ! -f "dist/send_rating_vadu" ]; then
    echo "ERROR: Binary not created!"
    exit 1
fi

echo ""
echo "✓ Binary created successfully: dist/send_rating_vadu"

# Create deploy directory if it doesn't exist
echo ""
echo "Creating deploy directory..."
mkdir -p "$DEPLOY_DIR"

# Copy the binary to deploy directory
echo ""
echo "Deploying binary to $DEPLOY_DIR..."
cp dist/send_rating_vadu "$DEPLOY_DIR/"
chmod +x "$DEPLOY_DIR/send_rating_vadu"

# Copy wrapper script
echo "Copying wrapper script..."
cp run_send_rating_vadu.sh "$DEPLOY_DIR/"
chmod +x "$DEPLOY_DIR/run_send_rating_vadu.sh"

# Copy .env file if it exists
if [ -f "$PROJECT_ROOT/.env" ]; then
    echo "Copying .env file..."
    cp "$PROJECT_ROOT/.env" "$DEPLOY_DIR/"
fi

# Ensure Playwright browsers are installed
echo ""
echo "Checking Playwright browsers..."
if [ ! -d "$HOME/.cache/ms-playwright/chromium-1200" ]; then
    echo "Installing Playwright browsers..."
    PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright" "$PROJECT_ROOT/.venv/bin/playwright" install chromium
else
    echo "✓ Playwright browsers already installed"
fi

echo ""
echo "========================================="
echo "✓ Deployment complete!"
echo "========================================="
echo ""
echo "Binary location: $DEPLOY_DIR/send_rating_vadu"
echo "Wrapper script: $DEPLOY_DIR/run_send_rating_vadu.sh"
echo ""
echo "To run the binary (recommended - uses wrapper script):"
echo "  cd $DEPLOY_DIR"
echo "  xvfb-run-safe ./run_send_rating_vadu.sh --pause 2"
echo ""
echo "Or run directly with environment variable:"
echo "  cd $DEPLOY_DIR"
echo "  PLAYWRIGHT_BROWSERS_PATH=\"\$HOME/.cache/ms-playwright\" xvfb-run-safe ./send_rating_vadu --pause 2"
echo ""

