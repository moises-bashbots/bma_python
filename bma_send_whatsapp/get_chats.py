#!/usr/bin/env python3
"""
Get chats from Z-API WhatsApp instance.
Makes a GET request to the Z-API chats endpoint.
"""

import json
import sys
from pathlib import Path
import requests


def load_config() -> dict:
    """Load Z-API configuration from zapi_config.json."""
    # Try current directory first, then parent directory
    config_paths = [
        Path(__file__).parent / "zapi_config.json",
        Path(__file__).parent.parent / "zapi_config.json",
    ]
    
    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)
    
    raise FileNotFoundError(
        f"Configuration file not found in: {[str(p) for p in config_paths]}"
    )


def get_chats() -> dict:
    """
    Get chats from Z-API.
    
    Returns:
        Response JSON as dictionary
    """
    config = load_config()
    zapi = config['zapi']
    
    # Build URL
    url = f"{zapi['base_url']}/instances/{zapi['instance_id']}/token/{zapi['token']}/chats"

    # Build headers
    headers = {
        'Client-Token': zapi['client_token']
    }
    
    print("=" * 80)
    print("Z-API Get Chats")
    print("=" * 80)
    print(f"URL: {url}")
    print(f"Headers: {headers}")
    print()
    
    # Make GET request
    print("Making GET request...")
    response = requests.get(url, headers=headers)
    
    # Check response status
    print(f"Status Code: {response.status_code}")
    print()
    
    # Raise exception for bad status codes
    response.raise_for_status()
    
    # Parse JSON response
    data = response.json()
    
    return data


def format_response(data: dict) -> None:
    """Print formatted response."""
    print("=" * 80)
    print("Response")
    print("=" * 80)
    
    # Pretty print JSON
    print(json.dumps(data, indent=2, ensure_ascii=False))
    print()
    
    # If response contains a list of chats, show summary
    if isinstance(data, list):
        print("=" * 80)
        print(f"Total chats: {len(data)}")
        print("=" * 80)
    elif isinstance(data, dict) and 'chats' in data:
        chats = data.get('chats', [])
        print("=" * 80)
        print(f"Total chats: {len(chats)}")
        print("=" * 80)


def main():
    """Main entry point."""
    try:
        # Get chats
        data = get_chats()
        
        # Format and print response
        format_response(data)
        
        print("✓ Request completed successfully!")
        return 0
        
    except requests.exceptions.HTTPError as e:
        print(f"\n✗ HTTP Error: {e}", file=sys.stderr)
        print(f"Response: {e.response.text}", file=sys.stderr)
        return 1
    except requests.exceptions.RequestException as e:
        print(f"\n✗ Request Error: {e}", file=sys.stderr)
        return 1
    except Exception as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())

