#!/bin/bash
#
# Start APR Dashboard in Production Mode with Gunicorn
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "=========================================="
echo "APR Dashboard - Production Startup"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    echo "⚠️  Warning: Running as root. Consider running as 'robot' user."
fi

# Change to project directory
cd "$PROJECT_DIR"

# Check if log directory exists
if [ ! -d "/var/log/apr_dashboard" ]; then
    echo "Creating log directory..."
    sudo mkdir -p /var/log/apr_dashboard
    sudo chown robot:robot /var/log/apr_dashboard
fi

# Check if virtual environment exists
if [ ! -d ".venv" ]; then
    echo "❌ Error: Virtual environment not found!"
    echo "   Please run: uv sync"
    exit 1
fi

# Kill any existing Flask development server
echo "Stopping any existing development servers..."
pkill -f "python.*dashboard/app.py" 2>/dev/null || true
sleep 2

# Start Gunicorn
echo ""
echo "Starting Gunicorn with production configuration..."
echo "Workers: $(python3 -c 'import multiprocessing; print(multiprocessing.cpu_count() * 2 + 1)')"
echo "Bind: 0.0.0.0:5000"
echo "Logs: /var/log/apr_dashboard/"
echo ""

exec uv run gunicorn \
    --config "$SCRIPT_DIR/gunicorn_config.py" \
    --chdir "$PROJECT_DIR" \
    slack_integration_alerts.dashboard.app:app

