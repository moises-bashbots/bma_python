#!/bin/bash
# Start APR Monitoring Dashboard

echo "=========================================="
echo "APR Proposal Monitoring Dashboard"
echo "=========================================="
echo ""

# Navigate to project root
cd /home/robot/Dev/bma_python

# Check if Flask is installed
if ! uv run python -c "import flask" 2>/dev/null; then
    echo "Flask not found. Installing..."
    uv pip install flask pymysql
fi

# Navigate to dashboard directory
cd /home/robot/Dev/bma_python/slack_integration_alerts/dashboard

echo ""
echo "Starting dashboard server..."
echo ""
echo "Access the dashboard at:"
echo "  http://localhost:5000"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Start Flask app with UV
cd /home/robot/Dev/bma_python
uv run python slack_integration_alerts/dashboard/app.py

