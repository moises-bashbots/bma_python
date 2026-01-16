import http.client
import json


def load_config(config_file="whatsapp_config.json"):
    """
    Load WhatsApp API configuration from JSON file

    Args:
        config_file (str): Path to the configuration file

    Returns:
        dict: Configuration dictionary

    Raises:
        FileNotFoundError: If config file doesn't exist
        json.JSONDecodeError: If config file is invalid JSON
    """
    try:
        with open(config_file, 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        raise FileNotFoundError(f"Configuration file '{config_file}' not found")
    except json.JSONDecodeError as e:
        raise json.JSONDecodeError(f"Invalid JSON in configuration file: {e}")


def send_message(phone, message, config_file="whatsapp_config.json"):
    """
    Send a WhatsApp message using Z-API

    Args:
        phone (str): Phone number to send message to (e.g., "5511997435829")
        message (str): Message text to send
        config_file (str): Path to configuration file (optional)

    Returns:
        dict: API response as dictionary

    Raises:
        Exception: If API request fails
    """
    # Load configuration
    config = load_config(config_file)

    # Build endpoint path
    endpoint_path = config["endpoint_path"].format(
        instance_id=config["instance_id"],
        token=config["token"]
    )

    # Create connection
    conn = http.client.HTTPSConnection(config["api_host"])

    # Prepare payload
    payload = json.dumps({
        "phone": phone,
        "message": message
    })

    # Prepare headers
    headers = {
        'Client-Token': config["client_token"],
        'Content-Type': 'application/json'
    }

    try:
        # Make request
        conn.request("POST", endpoint_path, payload, headers)
        res = conn.getresponse()
        data = res.read()

        # Parse response
        response_text = data.decode("utf-8")

        try:
            response_data = json.loads(response_text)
        except json.JSONDecodeError:
            response_data = {"raw_response": response_text}

        # Add status code to response
        response_data["status_code"] = res.status

        return response_data

    except Exception as e:
        raise Exception(f"Failed to send message: {str(e)}")
    finally:
        conn.close()


def main():
    """
    Main function for testing the send_message function
    """
    try:
        # Test the function
        phone = "5511997435829"
        message = "Welcome to *Z-API*"

        print(f"📱 Sending message to {phone}...")
        print(f"💬 Message: {message}")

        result = send_message(phone, message)

        print("✅ Message sent successfully!")
        print(f"📋 Response: {json.dumps(result, indent=2)}")

    except Exception as e:
        print(f"❌ Error: {e}")


if __name__ == "__main__":
    main()
