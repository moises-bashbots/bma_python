#!/bin/bash
# Build script for get_chats_sync
# Creates a standalone executable using PyInstaller

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Directories
PROJECT_ROOT="/home/robot/Dev/bma_python"
SCRIPT_DIR="$PROJECT_ROOT/bma_send_whatsapp"
DEPLOY_DIR="/home/robot/Deploy/bma_send_whatsapp"

echo "=========================================="
echo "Get Chats Sync - Build Script"
echo "=========================================="
echo ""
echo "Project Root: $PROJECT_ROOT"
echo "Script Directory: $SCRIPT_DIR"
echo "Deploy Directory: $DEPLOY_DIR"
echo ""

# Step 1: Clean previous builds
echo -e "${BLUE}[1/6] Cleaning previous builds...${NC}"
cd "$SCRIPT_DIR"
if [ -d "build/get_chats_sync" ]; then
    rm -rf build/get_chats_sync
    echo -e "${GREEN}  ✓ Removed build directory${NC}"
fi
if [ -d "dist/get_chats_sync" ]; then
    rm -rf dist/get_chats_sync
    echo -e "${GREEN}  ✓ Removed dist directory${NC}"
fi
if [ -f "dist/get_chats_sync" ]; then
    rm -f dist/get_chats_sync
    echo -e "${GREEN}  ✓ Removed old binary${NC}"
fi
echo ""

# Step 2: Verify dependencies
echo -e "${BLUE}[2/6] Verifying dependencies...${NC}"
cd "$PROJECT_ROOT"

# Check for PyInstaller
if [ -f "$PROJECT_ROOT/.venv/bin/pyinstaller" ]; then
    echo -e "${GREEN}  ✓ PyInstaller is installed${NC}"
    PYINSTALLER="$PROJECT_ROOT/.venv/bin/pyinstaller"
else
    echo -e "${RED}  ✗ PyInstaller not found${NC}"
    exit 1
fi

# Check for required packages using Python
$PROJECT_ROOT/.venv/bin/python3 -c "import mysql.connector" 2>/dev/null && echo -e "${GREEN}  ✓ mysql-connector-python is installed${NC}" || { echo -e "${RED}  ✗ mysql-connector-python not found${NC}"; exit 1; }
$PROJECT_ROOT/.venv/bin/python3 -c "import requests" 2>/dev/null && echo -e "${GREEN}  ✓ requests is installed${NC}" || { echo -e "${RED}  ✗ requests not found${NC}"; exit 1; }
echo ""

# Step 3: Build executable
echo -e "${BLUE}[3/6] Building executable with PyInstaller...${NC}"
cd "$SCRIPT_DIR"

# Set environment variable for pure Python MySQL connector
export MYSQL_CONNECTOR_PYTHON_USE_PURE=1

# Run PyInstaller
$PYINSTALLER --clean get_chats_sync.spec

if [ -f "dist/get_chats_sync" ]; then
    echo -e "${GREEN}  ✓ Build successful${NC}"
else
    echo -e "${RED}  ✗ Build failed - binary not found${NC}"
    exit 1
fi
echo ""

# Step 4: Create deployment directory
echo -e "${BLUE}[4/6] Creating deployment directory...${NC}"
mkdir -p "$DEPLOY_DIR"
echo -e "${GREEN}  ✓ Created $DEPLOY_DIR${NC}"
echo ""

# Step 5: Deploy binary and configuration files
echo -e "${BLUE}[5/6] Deploying binary and configuration files...${NC}"

# Copy binary
cp dist/get_chats_sync "$DEPLOY_DIR/"
chmod +x "$DEPLOY_DIR/get_chats_sync"
echo -e "${GREEN}  ✓ Deployed binary: $DEPLOY_DIR/get_chats_sync${NC}"

# Copy configuration files if they exist
if [ -f "$SCRIPT_DIR/databases_config.json" ]; then
    cp "$SCRIPT_DIR/databases_config.json" "$DEPLOY_DIR/"
    echo -e "${GREEN}  ✓ Deployed databases_config.json${NC}"
fi

if [ -f "$SCRIPT_DIR/zapi_config.json" ]; then
    cp "$SCRIPT_DIR/zapi_config.json" "$DEPLOY_DIR/"
    echo -e "${GREEN}  ✓ Deployed zapi_config.json${NC}"
fi

echo ""

# Step 6: Verify deployment
echo -e "${BLUE}[6/6] Verifying deployment...${NC}"
if [ -x "$DEPLOY_DIR/get_chats_sync" ]; then
    echo -e "${GREEN}  ✓ Binary is executable${NC}"
    BINARY_SIZE=$(du -h "$DEPLOY_DIR/get_chats_sync" | cut -f1)
    echo -e "${GREEN}  ✓ Binary size: $BINARY_SIZE${NC}"
else
    echo -e "${RED}  ✗ Binary is not executable${NC}"
    exit 1
fi
echo ""

echo "=========================================="
echo -e "${GREEN}Build and Deployment Complete!${NC}"
echo "=========================================="
echo ""
echo "Deployment location: $DEPLOY_DIR"
echo "Binary: $DEPLOY_DIR/get_chats_sync"
echo ""
echo "To run the program:"
echo "  cd $DEPLOY_DIR"
echo "  ./get_chats_sync"
echo ""
echo "The program will sync WhatsApp contacts from Z-API to the database."
echo ""

