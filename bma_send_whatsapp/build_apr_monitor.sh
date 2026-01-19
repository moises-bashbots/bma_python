#!/bin/bash
# Build script for APR Status Monitor executable
# This script builds a standalone binary using PyInstaller and deploys it

set -e  # Exit on error

echo "=========================================="
echo "APR Status Monitor - Build Script"
echo "=========================================="
echo ""

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DEPLOY_DIR="/home/robot/Deploy/bma_send_whatsapp"
SPEC_FILE="$SCRIPT_DIR/apr_status_monitor_standalone.spec"
BINARY_NAME="apr_status_monitor"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Project Root: $PROJECT_ROOT"
echo "Script Directory: $SCRIPT_DIR"
echo "Deploy Directory: $DEPLOY_DIR"
echo ""

# Step 1: Clean previous builds
echo -e "${YELLOW}[1/6] Cleaning previous builds...${NC}"
if [ -d "$SCRIPT_DIR/build" ]; then
    rm -rf "$SCRIPT_DIR/build"
    echo "  ✓ Removed build directory"
fi
if [ -d "$SCRIPT_DIR/dist" ]; then
    rm -rf "$SCRIPT_DIR/dist"
    echo "  ✓ Removed dist directory"
fi
echo ""

# Step 2: Verify dependencies
echo -e "${YELLOW}[2/6] Verifying dependencies...${NC}"
cd "$PROJECT_ROOT"

# Check if PyInstaller is installed
if ! uv pip show pyinstaller > /dev/null 2>&1; then
    echo -e "${RED}  ✗ PyInstaller not found. Installing...${NC}"
    uv pip install pyinstaller
else
    echo "  ✓ PyInstaller is installed"
fi

# Check critical dependencies
REQUIRED_PACKAGES=("pymssql" "sqlalchemy" "mysql-connector-python" "requests")
for package in "${REQUIRED_PACKAGES[@]}"; do
    if uv pip show "$package" > /dev/null 2>&1; then
        echo "  ✓ $package is installed"
    else
        echo -e "${RED}  ✗ $package not found${NC}"
        exit 1
    fi
done
echo ""

# Step 3: Build the executable
echo -e "${YELLOW}[3/6] Building executable with PyInstaller...${NC}"
cd "$SCRIPT_DIR"

# Use Python from virtual environment to run PyInstaller as a module
PYTHON_CMD="$PROJECT_ROOT/.venv/bin/python3"
if [ ! -L "$PYTHON_CMD" ]; then
    echo -e "${RED}  ✗ Python not found at $PYTHON_CMD${NC}"
    exit 1
fi

"$PYTHON_CMD" -m PyInstaller "$SPEC_FILE" --clean --noconfirm

if [ ! -f "$SCRIPT_DIR/dist/$BINARY_NAME" ]; then
    echo -e "${RED}  ✗ Build failed - binary not found${NC}"
    exit 1
fi
echo -e "${GREEN}  ✓ Build successful${NC}"
echo ""

# Step 4: Create deployment directory
echo -e "${YELLOW}[4/6] Creating deployment directory...${NC}"
mkdir -p "$DEPLOY_DIR"
echo "  ✓ Created $DEPLOY_DIR"
echo ""

# Step 5: Deploy binary and configuration files
echo -e "${YELLOW}[5/6] Deploying binary and configuration files...${NC}"

# Copy binary
cp "$SCRIPT_DIR/dist/$BINARY_NAME" "$DEPLOY_DIR/"
chmod 755 "$DEPLOY_DIR/$BINARY_NAME"
echo "  ✓ Deployed binary: $DEPLOY_DIR/$BINARY_NAME"

# Copy configuration files
if [ -f "$SCRIPT_DIR/databases_config.json" ]; then
    cp "$SCRIPT_DIR/databases_config.json" "$DEPLOY_DIR/"
    echo "  ✓ Deployed databases_config.json"
else
    echo -e "${YELLOW}  ⚠ databases_config.json not found - you'll need to create it${NC}"
fi

if [ -f "$SCRIPT_DIR/zapi_config.json" ]; then
    cp "$SCRIPT_DIR/zapi_config.json" "$DEPLOY_DIR/"
    echo "  ✓ Deployed zapi_config.json"
elif [ -f "$SCRIPT_DIR/whatsapp_config.json" ]; then
    cp "$SCRIPT_DIR/whatsapp_config.json" "$DEPLOY_DIR/zapi_config.json"
    echo "  ✓ Deployed whatsapp_config.json as zapi_config.json"
else
    echo -e "${YELLOW}  ⚠ WhatsApp config not found - you'll need to create zapi_config.json${NC}"
fi

# Create message_tracking directory
mkdir -p "$DEPLOY_DIR/message_tracking"
echo "  ✓ Created message_tracking directory"
echo ""

# Step 6: Verify deployment
echo -e "${YELLOW}[6/6] Verifying deployment...${NC}"
if [ -x "$DEPLOY_DIR/$BINARY_NAME" ]; then
    BINARY_SIZE=$(du -h "$DEPLOY_DIR/$BINARY_NAME" | cut -f1)
    echo -e "${GREEN}  ✓ Binary is executable${NC}"
    echo "  ✓ Binary size: $BINARY_SIZE"
else
    echo -e "${RED}  ✗ Binary is not executable${NC}"
    exit 1
fi
echo ""

# Summary
echo "=========================================="
echo -e "${GREEN}Build and Deployment Complete!${NC}"
echo "=========================================="
echo ""
echo "Deployment location: $DEPLOY_DIR"
echo "Binary: $DEPLOY_DIR/$BINARY_NAME"
echo ""
echo "To run the monitor:"
echo "  cd $DEPLOY_DIR"
echo "  ./$BINARY_NAME"
echo ""
echo "The monitor will automatically query records for the current date."
echo ""

