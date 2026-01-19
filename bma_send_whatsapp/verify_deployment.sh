#!/bin/bash
# Verification script for APR Status Monitor deployment

echo "=========================================="
echo "APR Status Monitor - Deployment Verification"
echo "=========================================="
echo ""

DEPLOY_DIR="/home/robot/Deploy/bma_send_whatsapp"
BINARY="$DEPLOY_DIR/apr_status_monitor"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check deployment directory
echo "1. Checking deployment directory..."
if [ -d "$DEPLOY_DIR" ]; then
    echo -e "   ${GREEN}✓${NC} Directory exists: $DEPLOY_DIR"
else
    echo -e "   ${RED}✗${NC} Directory not found: $DEPLOY_DIR"
    exit 1
fi
echo ""

# Check binary
echo "2. Checking executable binary..."
if [ -f "$BINARY" ]; then
    echo -e "   ${GREEN}✓${NC} Binary exists: $BINARY"
    BINARY_SIZE=$(du -h "$BINARY" | cut -f1)
    echo "   Size: $BINARY_SIZE"
else
    echo -e "   ${RED}✗${NC} Binary not found: $BINARY"
    exit 1
fi

if [ -x "$BINARY" ]; then
    echo -e "   ${GREEN}✓${NC} Binary is executable"
else
    echo -e "   ${RED}✗${NC} Binary is not executable"
    exit 1
fi
echo ""

# Check configuration files
echo "3. Checking configuration files..."
if [ -f "$DEPLOY_DIR/databases_config.json" ]; then
    echo -e "   ${GREEN}✓${NC} databases_config.json exists"
else
    echo -e "   ${YELLOW}⚠${NC} databases_config.json not found"
fi

if [ -f "$DEPLOY_DIR/zapi_config.json" ]; then
    echo -e "   ${GREEN}✓${NC} zapi_config.json exists"
else
    echo -e "   ${YELLOW}⚠${NC} zapi_config.json not found"
fi
echo ""

# Check message tracking directory
echo "4. Checking message tracking directory..."
if [ -d "$DEPLOY_DIR/message_tracking" ]; then
    echo -e "   ${GREEN}✓${NC} message_tracking directory exists"
    TRACKING_FILES=$(ls -1 "$DEPLOY_DIR/message_tracking" 2>/dev/null | wc -l)
    echo "   Tracking files: $TRACKING_FILES"
else
    echo -e "   ${YELLOW}⚠${NC} message_tracking directory not found"
fi
echo ""

# Check documentation
echo "5. Checking documentation..."
if [ -f "$DEPLOY_DIR/README_APR_MONITOR.md" ]; then
    echo -e "   ${GREEN}✓${NC} README_APR_MONITOR.md exists"
else
    echo -e "   ${YELLOW}⚠${NC} README_APR_MONITOR.md not found"
fi
echo ""

# Test binary execution
echo "6. Testing binary execution..."
cd "$DEPLOY_DIR"
OUTPUT=$("$BINARY" 2>&1)
if echo "$OUTPUT" | grep -q "APR_CAPA WITH STATUS QUERY"; then
    echo -e "   ${GREEN}✓${NC} Binary executes successfully"
    echo "   Output: Query executed correctly"
else
    echo -e "   ${RED}✗${NC} Binary execution failed"
    echo "   Output: $OUTPUT"
    exit 1
fi
echo ""

# Summary
echo "=========================================="
echo -e "${GREEN}Deployment Verification Complete!${NC}"
echo "=========================================="
echo ""
echo "Deployment Summary:"
echo "  Location: $DEPLOY_DIR"
echo "  Binary: apr_status_monitor ($BINARY_SIZE)"
echo "  Status: Ready for use"
echo ""
echo "To run the monitor:"
echo "  cd $DEPLOY_DIR"
echo "  ./apr_status_monitor"
echo ""
echo "The monitor will automatically query records for the current date."
echo ""

