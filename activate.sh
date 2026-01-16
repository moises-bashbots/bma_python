#!/bin/bash
# Activation script for BMA virtual environment

# Activate the virtual environment
source .venv/bin/activate

echo "=========================================="
echo "BMA Virtual Environment Activated"
echo "=========================================="
echo "Python version: $(python --version)"
echo "Location: $VIRTUAL_ENV"
echo ""
echo "Available projects:"
echo "  - slack_integration_alerts/"
echo "  - bma_send_whatsapp/"
echo "  - bma_consulta_sacado/"
echo "  - cadastro_sacado/"
echo ""
echo "To deactivate, run: deactivate"
echo "=========================================="

