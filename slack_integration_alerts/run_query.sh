#!/bin/bash
cd /home/robot/Dev/bma_python
.venv/bin/python3 slack_integration_alerts/test_query.py > /tmp/query_output.txt 2>&1
echo "Output saved to /tmp/query_output.txt"
cat /tmp/query_output.txt

