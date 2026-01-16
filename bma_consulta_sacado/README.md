# Assertiva Soluções OAuth2 Client

This program implements an OAuth2 client for the Assertiva Soluções API, handling token requests and management for API authentication.

## Features

- 🔐 OAuth2 client credentials flow implementation
- 🔄 Automatic token refresh with expiration handling
- ⚙️ Configurable credentials via environment variables
- 📝 Comprehensive logging
- 🛡️ Error handling and validation
- 🚀 Ready-to-use authentication headers for API requests

## Installation

1. Install Python dependencies:
```bash
pip install -r requirements.txt
```

2. (Optional) Set environment variables for credentials:
```bash
export ASSERTIVA_CLIENT_ID="your_client_id"
export ASSERTIVA_CLIENT_SECRET="your_client_secret"
```

## Usage

### Basic Usage

```python
from oauth_client import OAuth2Client

# Create OAuth2 client (uses credentials from config.py or environment)
oauth_client = OAuth2Client()

# Request access token
token_response = oauth_client.request_token()
print(f"Access token: {token_response['access_token']}")

# Get auth headers for API requests
headers = oauth_client.get_auth_headers()
# headers = {"Authorization": "Bearer <token>", "Content-Type": "application/json"}
```

### Making API Requests

```python
import requests
from oauth_client import OAuth2Client

oauth_client = OAuth2Client()

# The client automatically handles token refresh
headers = oauth_client.get_auth_headers()

# Make authenticated API request
response = requests.get(
    "https://api.assertivasolucoes.com.br/some/endpoint",
    headers=headers
)
```

### Running the Example

```bash
# Test the OAuth2 token request
python example_usage.py

# Or run the main OAuth client directly
python oauth_client.py
```

## Configuration

### Default Credentials

The program includes the credentials from your curl command by default in `config.py`:

- **Client ID**: `ub81SRCGjeA827dGyul78g/jb8mHFxQRqwVLYBL0w5BTR71z46Iqw9kyPWh0Ab84xuSblNoRibe5grN2gXjtPw==`
- **Client Secret**: `3Je/F2PZhmSY6+X7sYlXwI9uvaPesA1BInQvQ6BW1IPgjfUiJJvhXhLN0VyRJkBa/P5Jb7oqSoNC9CucU/CKLg==`

### Environment Variables

You can override the default credentials using environment variables:

```bash
export ASSERTIVA_CLIENT_ID="your_client_id"
export ASSERTIVA_CLIENT_SECRET="your_client_secret"
```

### Configuration Options

Edit `config.py` to modify:

- `REQUEST_TIMEOUT`: HTTP request timeout (default: 30 seconds)
- `TOKEN_BUFFER_MINUTES`: Token refresh buffer time (default: 5 minutes)
- `BASE_URL`: API base URL

## Files

- **`oauth_client.py`**: Main OAuth2 client implementation
- **`config.py`**: Configuration and credentials management
- **`example_usage.py`**: Example usage and testing script
- **`requirements.txt`**: Python dependencies
- **`README.md`**: This documentation

## API Response

The token request returns a response similar to:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

## Error Handling

The client includes comprehensive error handling for:

- Network connectivity issues
- Invalid credentials
- Token expiration
- API response errors

## Security Notes

- 🔒 Credentials are stored in configuration files - consider using environment variables for production
- 🔄 Tokens are automatically refreshed before expiration
- ⏰ Token expiration includes a 5-minute safety buffer
- 🛡️ All HTTP requests include proper timeout handling

## Equivalent curl Command

This program implements the following curl command:

```bash
curl -X POST "https://api.assertivasolucoes.com.br/oauth2/v3/token" \
  -u "ub81SRCGjeA827dGyul78g/jb8mHFxQRqwVLYBL0w5BTR71z46Iqw9kyPWh0Ab84xuSblNoRibe5grN2gXjtPw==:3Je/F2PZhmSY6+X7sYlXwI9uvaPesA1BInQvQ6BW1IPgjfUiJJvhXhLN0VyRJkBa/P5Jb7oqSoNC9CucU/CKLg==" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials"
```

## Next Steps

1. Test the token request with `python example_usage.py`
2. Implement specific API endpoints for your use case
3. Add business logic for handling API responses
4. Consider implementing token persistence for production use
