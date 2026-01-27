#!/bin/bash

# Build script for send_rating_vadu binary
# This script builds the send_rating_vadu program using PyInstaller

set -e  # Exit on error

echo "=================================="
echo "Building send_rating_vadu binary"
echo "=================================="

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Clean previous builds
echo "Cleaning previous builds..."
rm -rf build/send_rating_vadu dist/send_rating_vadu
rm -rf __pycache__

# Build with PyInstaller using the spec file
echo "Building with PyInstaller..."
../.venv/bin/python -m PyInstaller --clean send_rating_vadu.spec

# Check if binary was created
if [ -f "dist/send_rating_vadu" ]; then
    echo "✓ Binary created successfully: dist/send_rating_vadu"
    ls -lh dist/send_rating_vadu
    
    # Deploy to production directory
    DEPLOY_DIR="/home/robot/Deploy/slack_integration_alerts"
    echo ""
    echo "Deploying to $DEPLOY_DIR..."
    
    # Create deployment directory if it doesn't exist
    mkdir -p "$DEPLOY_DIR"
    
    # Copy binary
    cp -f dist/send_rating_vadu "$DEPLOY_DIR/"
    chmod +x "$DEPLOY_DIR/send_rating_vadu"
    
    # Copy configuration files
    cp -f databases_config.json "$DEPLOY_DIR/" 2>/dev/null || echo "⚠ databases_config.json not found"
    
    # Copy wrapper script
    cp -f run_send_rating_vadu.sh "$DEPLOY_DIR/"
    chmod +x "$DEPLOY_DIR/run_send_rating_vadu.sh"
    
    echo "✓ Deployment complete!"
    echo ""
    echo "Deployed files:"
    ls -lh "$DEPLOY_DIR/send_rating_vadu"
    ls -lh "$DEPLOY_DIR/run_send_rating_vadu.sh"
    
    echo ""
    echo "To run the program:"
    echo "  cd $DEPLOY_DIR"
    echo "  DISPLAY=:10.0 ./send_rating_vadu"
    echo ""
    echo "Or use the wrapper script:"
    echo "  cd $DEPLOY_DIR"
    echo "  DISPLAY=:10.0 ./run_send_rating_vadu.sh"
else
    echo "✗ Binary creation failed!"
    exit 1
fi

echo ""
echo "Build and deployment completed successfully!"

