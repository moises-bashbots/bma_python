#!/usr/bin/env python3
"""
Main entry point for APR Status Monitor executable.
This wrapper ensures all modules are properly bundled and accessible.
"""

import sys
import os
from pathlib import Path

# Get the directory where the executable is running from
if getattr(sys, 'frozen', False):
    # Running as compiled executable
    application_path = sys._MEIPASS
else:
    # Running as script
    application_path = Path(__file__).parent.parent

# Add application path to sys.path to find our modules
sys.path.insert(0, str(application_path))

# Now import and run the actual query script
from bma_send_whatsapp import query_apr_capa_status

if __name__ == "__main__":
    # Run the main function from query_apr_capa_status
    query_apr_capa_status.main()

