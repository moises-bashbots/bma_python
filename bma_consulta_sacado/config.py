"""
Configuration file for OAuth2 credentials and API settings.
"""

import os
from typing import Optional

class Config:
    """Configuration class for API credentials and settings."""
    
    # OAuth2 Credentials - can be overridden by environment variables
    CLIENT_ID = os.getenv(
        "ASSERTIVA_CLIENT_ID",
        "ub81SRCGjeA827dGyul78g/jb8mHFxQRqwVLYBL0w5BTR71z46Iqw9kyPWh0Ab84xuSblNoRibe5grN2gXjtPw=="
    )
    
    CLIENT_SECRET = os.getenv(
        "ASSERTIVA_CLIENT_SECRET", 
        "3Je/F2PZhmSY6+X7sYlXwI9uvaPesA1BInQvQ6BW1IPgjfUiJJvhXhLN0VyRJkBa/P5Jb7oqSoNC9CucU/CKLg=="
    )
    
    # API Configuration
    BASE_URL = "https://api.assertivasolucoes.com.br"
    TOKEN_URL = f"{BASE_URL}/oauth2/v3/token"
    
    # Request settings
    REQUEST_TIMEOUT = 30
    TOKEN_BUFFER_MINUTES = 5  # Refresh token 5 minutes before expiration
    
    @classmethod
    def validate(cls) -> bool:
        """Validate that required configuration is present."""
        if not cls.CLIENT_ID or not cls.CLIENT_SECRET:
            raise ValueError("CLIENT_ID and CLIENT_SECRET must be provided")
        return True
