#!/usr/bin/env python3
"""
OAuth2 Client for Assertiva Soluções API
Handles token requests and management for API authentication.
"""

import requests
import base64
import json
import time
from datetime import datetime, timedelta
from typing import Optional, Dict, Any
import logging
from config import Config

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class OAuth2Client:
    """OAuth2 client for Assertiva Soluções API authentication."""
    
    def __init__(self, client_id: Optional[str] = None, client_secret: Optional[str] = None):
        """
        Initialize OAuth2 client.

        Args:
            client_id: OAuth2 client ID (defaults to config)
            client_secret: OAuth2 client secret (defaults to config)
        """
        Config.validate()
        self.client_id = client_id or Config.CLIENT_ID
        self.client_secret = client_secret or Config.CLIENT_SECRET
        self.token_url = Config.TOKEN_URL
        self.access_token = None
        self.token_expires_at = None
        
    def _create_auth_header(self) -> str:
        """Create Basic Authentication header for OAuth2 request."""
        credentials = f"{self.client_id}:{self.client_secret}"
        encoded_credentials = base64.b64encode(credentials.encode()).decode()
        return f"Basic {encoded_credentials}"
    
    def request_token(self) -> Dict[str, Any]:
        """
        Request access token from OAuth2 endpoint.
        
        Returns:
            Dict containing token response data
            
        Raises:
            requests.RequestException: If the request fails
            ValueError: If the response is invalid
        """
        headers = {
            "Authorization": self._create_auth_header(),
            "Content-Type": "application/x-www-form-urlencoded"
        }
        
        data = {
            "grant_type": "client_credentials"
        }
        
        logger.info("Requesting OAuth2 token...")
        
        try:
            response = requests.post(
                self.token_url,
                headers=headers,
                data=data,
                timeout=Config.REQUEST_TIMEOUT
            )
            response.raise_for_status()
            
            token_data = response.json()
            
            # Store token and calculate expiration time
            self.access_token = token_data.get("access_token")
            expires_in = token_data.get("expires_in", 3600)  # Default to 1 hour
            self.token_expires_at = datetime.now() + timedelta(seconds=expires_in)
            
            logger.info("Token obtained successfully")
            logger.info(f"Token expires at: {self.token_expires_at}")
            
            return token_data
            
        except requests.RequestException as e:
            logger.error(f"Failed to request token: {e}")
            raise
        except (ValueError, KeyError) as e:
            logger.error(f"Invalid token response: {e}")
            raise
    
    def get_valid_token(self) -> str:
        """
        Get a valid access token, requesting a new one if necessary.
        
        Returns:
            Valid access token string
        """
        if self._is_token_expired():
            logger.info("Token expired or not available, requesting new token...")
            self.request_token()
        
        return self.access_token
    
    def _is_token_expired(self) -> bool:
        """Check if the current token is expired or not available."""
        if not self.access_token or not self.token_expires_at:
            return True
        
        # Add buffer before expiration
        buffer_time = timedelta(minutes=Config.TOKEN_BUFFER_MINUTES)
        return datetime.now() >= (self.token_expires_at - buffer_time)
    
    def get_auth_headers(self) -> Dict[str, str]:
        """
        Get headers with valid authorization token for API requests.
        
        Returns:
            Dictionary with Authorization header
        """
        token = self.get_valid_token()
        return {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }


def main():
    """Example usage of the OAuth2Client."""
    # Create OAuth2 client (uses credentials from config)
    oauth_client = OAuth2Client()
    
    try:
        # Request token
        token_response = oauth_client.request_token()
        
        print("Token Response:")
        print(json.dumps(token_response, indent=2))
        
        # Example of getting auth headers for subsequent requests
        auth_headers = oauth_client.get_auth_headers()
        print("\nAuth Headers for API requests:")
        print(json.dumps(auth_headers, indent=2))
        
    except Exception as e:
        logger.error(f"Error: {e}")
        return 1
    
    return 0


if __name__ == "__main__":
    exit(main())
