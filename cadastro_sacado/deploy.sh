#!/usr/bin/env bash
# Deploy script for db_connector

set -e

# Load configuration
CONFIG_FILE="$(dirname "$0")/deploy_config.json"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: deploy_config.json not found"
    exit 1
fi

SERVER=$(python3 -c "import json; print(json.load(open('$CONFIG_FILE'))['remote_server']['server'])")
USER=$(python3 -c "import json; print(json.load(open('$CONFIG_FILE'))['remote_server']['user'])")
PASSWORD=$(python3 -c "import json; print(json.load(open('$CONFIG_FILE'))['remote_server']['password'])")
DEPLOY_FOLDER=$(python3 -c "import json; print(json.load(open('$CONFIG_FILE'))['remote_server']['deploy_folder'])")

SCRIPT_DIR="$(dirname "$0")"
DIST_DIR="$SCRIPT_DIR/dist"

echo "=========================================="
echo "Deploying to $USER@$SERVER:$DEPLOY_FOLDER"
echo "=========================================="

# Create deploy folder and copy files using expect
expect << EOF
set timeout 30

# Create directory
spawn ssh -o StrictHostKeyChecking=no $USER@$SERVER "mkdir -p $DEPLOY_FOLDER"
expect "password:"
send "$PASSWORD\r"
expect eof

# Copy binary
spawn scp -o StrictHostKeyChecking=no $DIST_DIR/db_connector $USER@$SERVER:$DEPLOY_FOLDER/
expect "password:"
send "$PASSWORD\r"
expect eof

# Copy config
spawn scp -o StrictHostKeyChecking=no $DIST_DIR/databases_config.json $USER@$SERVER:$DEPLOY_FOLDER/
expect "password:"
send "$PASSWORD\r"
expect eof

# Make executable and run test
spawn ssh -o StrictHostKeyChecking=no $USER@$SERVER "chmod +x $DEPLOY_FOLDER/db_connector && $DEPLOY_FOLDER/db_connector"
expect "password:"
send "$PASSWORD\r"
expect eof
EOF

echo ""
echo "=========================================="
echo "Deployment complete!"
echo "=========================================="

