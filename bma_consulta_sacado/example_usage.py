#!/usr/bin/env python3
"""
Example usage of the OAuth2Client for Assertiva Soluções API.
"""

import json
import requests
from oauth_client import OAuth2Client
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def test_token_request():
    """Test the OAuth2 token request functionality."""
    print("=" * 50)
    print("Testing OAuth2 Token Request")
    print("=" * 50)
    
    try:
        # Create OAuth2 client
        oauth_client = OAuth2Client()
        
        # Request token
        print("Requesting access token...")
        token_response = oauth_client.request_token()
        
        print("\n✅ Token obtained successfully!")
        print(f"Access Token: {token_response.get('access_token', 'N/A')[:20]}...")
        print(f"Token Type: {token_response.get('token_type', 'N/A')}")
        print(f"Expires In: {token_response.get('expires_in', 'N/A')} seconds")
        
        # Test getting auth headers
        print("\n📋 Auth headers for API requests:")
        auth_headers = oauth_client.get_auth_headers()
        print(json.dumps(auth_headers, indent=2))
        
        return True
        
    except Exception as e:
        print(f"\n❌ Error occurred: {e}")
        logger.error(f"Token request failed: {e}")
        return False


def example_api_request(oauth_client: OAuth2Client, endpoint: str = "/some/api/endpoint"):
    """
    Example of how to make an authenticated API request.
    
    Args:
        oauth_client: Configured OAuth2Client instance
        endpoint: API endpoint to call (example only)
    """
    print(f"\n📡 Example API request to {endpoint}")
    
    try:
        # Get auth headers with valid token
        headers = oauth_client.get_auth_headers()
        
        # Example API call (this endpoint may not exist)
        # Replace with actual API endpoints from Assertiva Soluções
        url = f"https://api.assertivasolucoes.com.br{endpoint}"
        
        print(f"Making request to: {url}")
        print(f"Headers: {json.dumps(headers, indent=2)}")
        
        # Note: This is just an example - replace with actual API endpoints
        # response = requests.get(url, headers=headers, timeout=30)
        # response.raise_for_status()
        # return response.json()
        
        print("ℹ️  This is just an example - replace with actual API endpoints")
        
    except Exception as e:
        logger.error(f"API request failed: {e}")
        raise


def main():
    """Main function demonstrating OAuth2Client usage."""
    print("🚀 Assertiva Soluções OAuth2 Client Demo")
    
    # Test token request
    success = test_token_request()
    
    if success:
        print("\n" + "=" * 50)
        print("OAuth2 Client is working correctly!")
        print("=" * 50)
        
        # Create client for further examples
        oauth_client = OAuth2Client()
        
        # Example of how to use the client for API requests
        example_api_request(oauth_client, "/example/endpoint")
        
        print("\n💡 Next steps:")
        print("1. Replace example endpoints with actual API endpoints")
        print("2. Implement specific API methods for your use case")
        print("3. Add error handling for specific API responses")
        print("4. Consider storing tokens securely for production use")
        
    else:
        print("\n❌ OAuth2 setup needs attention")
        print("Please check your credentials and network connection")
    
    return 0 if success else 1


if __name__ == "__main__":
    exit(main())
