#!/bin/bash
# Build script for query_apr_invalidos_status.py executable
# This script builds a standalone binary using PyInstaller and deploys it

set -e  # Exit on error

echo "=========================================="
echo "Query APR Invalidos Status - Build Script"
echo "=========================================="
echo ""

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DEPLOY_DIR="/home/robot/Deploy/slack_integration_alerts"
SPEC_FILE="$SCRIPT_DIR/query_apr_invalidos_status.spec"
BINARY_NAME="query_apr_invalidos_status"

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
REQUIRED_PACKAGES=("pymssql" "sqlalchemy" "pymysql" "openpyxl" "requests")
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
if [ ! -f "$PYTHON_CMD" ]; then
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
CONFIG_FILES=("databases_config.json" "slack_config.json")
for config in "${CONFIG_FILES[@]}"; do
    if [ -f "$SCRIPT_DIR/$config" ]; then
        cp "$SCRIPT_DIR/$config" "$DEPLOY_DIR/"
        echo "  ✓ Copied: $config"
    else
        echo -e "${YELLOW}  ⚠ Warning: $config not found${NC}"
    fi
done
echo ""

# Step 6: Verify deployment
echo -e "${YELLOW}[6/6] Verifying deployment...${NC}"
if [ -f "$DEPLOY_DIR/$BINARY_NAME" ]; then
    BINARY_SIZE=$(du -h "$DEPLOY_DIR/$BINARY_NAME" | cut -f1)
    echo "  ✓ Binary deployed successfully"
    echo "  ✓ Size: $BINARY_SIZE"
    echo "  ✓ Location: $DEPLOY_DIR/$BINARY_NAME"
else
    echo -e "${RED}  ✗ Deployment verification failed${NC}"
    exit 1
fi
echo ""

# Create subdirectories for output files and tracking (if they don't exist)
mkdir -p "$DEPLOY_DIR/duplicatas_invalidas" 2>/dev/null || true
mkdir -p "$DEPLOY_DIR/seuno_invalidos" 2>/dev/null || true
mkdir -p "$DEPLOY_DIR/duplicatas_invalidas_tracking" 2>/dev/null || true
mkdir -p "$DEPLOY_DIR/seuno_invalidos_tracking" 2>/dev/null || true
echo "  ✓ Output directories ready"
echo ""

echo "=========================================="
echo -e "${GREEN}✓ Build and deployment completed!${NC}"
echo "=========================================="
echo ""
echo "To run the program:"
echo "  cd $DEPLOY_DIR"
echo "  ./query_apr_invalidos_status           # Production mode (default - sends alerts)"
echo "  ./query_apr_invalidos_status --dry-run # Dry-run mode (preview only)"
echo ""

