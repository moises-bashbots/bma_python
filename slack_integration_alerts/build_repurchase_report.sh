#!/bin/bash

# Build script for repurchase_report standalone binary
# This script compiles repurchase_report.py into a standalone executable using PyInstaller

set -e  # Exit on error

echo "========================================================================"
echo "Building repurchase_report Standalone Binary"
echo "========================================================================"
echo ""

# Change to slack_integration_alerts directory
cd "$(dirname "$0")"

echo "📁 Working directory: $(pwd)"
echo ""

# Step 1: Clean previous builds
echo "🧹 Step 1: Cleaning previous builds..."
rm -rf build/repurchase_report dist/repurchase_report
echo "   ✓ Cleaned build and dist directories"
echo ""

# Step 2: Verify dependencies
echo "🔍 Step 2: Verifying dependencies..."
if [ ! -f "repurchase_report.spec" ]; then
    echo "   ❌ ERROR: repurchase_report.spec not found!"
    exit 1
fi
echo "   ✓ repurchase_report.spec found"

if [ ! -f "repurchase_report.py" ]; then
    echo "   ❌ ERROR: repurchase_report.py not found!"
    exit 1
fi
echo "   ✓ repurchase_report.py found"
echo ""

# Step 3: Build with PyInstaller
echo "🔨 Step 3: Building with PyInstaller..."
echo "   This may take a few minutes..."
echo ""

../.venv/bin/pyinstaller --clean repurchase_report.spec

if [ ! -f "dist/repurchase_report" ]; then
    echo "   ❌ ERROR: Build failed - binary not created!"
    exit 1
fi
echo ""
echo "   ✓ Binary created successfully"
echo ""

# Step 4: Display binary info
echo "📊 Step 4: Binary Information"
ls -lh dist/repurchase_report
echo ""

# Step 5: Deploy to /home/robot/Deploy/slack_integration_alerts
echo "🚀 Step 5: Deploying to /home/robot/Deploy/slack_integration_alerts..."

DEPLOY_DIR="/home/robot/Deploy/slack_integration_alerts"

if [ ! -d "$DEPLOY_DIR" ]; then
    echo "   ⚠️  Deploy directory does not exist: $DEPLOY_DIR"
    echo "   Creating directory..."
    mkdir -p "$DEPLOY_DIR"
fi

# Copy binary
cp -v dist/repurchase_report "$DEPLOY_DIR/repurchase_report"
chmod +x "$DEPLOY_DIR/repurchase_report"
echo "   ✓ Binary deployed and made executable"
echo ""

# Copy configuration files if they don't exist
if [ ! -f "$DEPLOY_DIR/databases_config.json" ]; then
    echo "   📋 Copying databases_config.json..."
    cp -v databases_config.json "$DEPLOY_DIR/"
fi

if [ ! -f "$DEPLOY_DIR/slack_config.json" ]; then
    echo "   📋 Copying slack_config.json..."
    cp -v slack_config.json "$DEPLOY_DIR/"
fi

echo ""
echo "========================================================================"
echo "✅ BUILD AND DEPLOYMENT COMPLETE!"
echo "========================================================================"
echo ""
echo "📦 Binary Location: $DEPLOY_DIR/repurchase_report"
echo "📏 Binary Size: $(ls -lh $DEPLOY_DIR/repurchase_report | awk '{print $5}')"
echo ""
echo "🚀 Usage:"
echo "   Production Mode (sends alerts):"
echo "     cd $DEPLOY_DIR"
echo "     ./repurchase_report"
echo ""
echo "   Dry Run Mode (test only):"
echo "     cd $DEPLOY_DIR"
echo "     ./repurchase_report --dry-run"
echo ""
echo "========================================================================"

