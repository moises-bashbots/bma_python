# WhatsApp API Integration with Z-API

A Python project for sending WhatsApp messages using the Z-API service.

## 🚀 Features

- ✅ Send WhatsApp text messages via Z-API
- ✅ Configuration-based authentication (JSON config file)
- ✅ Reusable `send_message()` function for integration
- ✅ Comprehensive error handling
- ✅ Clean, modular design
- ✅ Example scripts and usage patterns
- ✅ Support for bulk messaging

## 📁 Project Structure

```
bma_send_whatsapp/
├── venv/                    # Virtual environment (Python 3.8.20)
├── send_whatsapp.py        # Main WhatsApp messaging module with send_message() function
├── whatsapp_config.json    # Z-API configuration file
├── example_usage.py        # Examples of using send_message() from other programs
├── whatsapp_sender.py      # Enhanced WhatsApp sender class (legacy)
├── config.py               # Configuration management (legacy)
├── examples.py             # Usage examples (legacy)
├── requirements.txt        # Project dependencies
├── activate.sh             # Virtual environment activation script
└── README.md               # This file
```

## 🛠️ Setup

### Prerequisites
- Python 3.8+
- `uv` package manager

### Installation

1. **Clone and navigate to the project:**
   ```bash
   cd /path/to/bma_send_whatsapp
   ```

2. **Create virtual environment with Python 3.8:**
   ```bash
   uv venv --python 3.8 venv
   ```

3. **Activate the virtual environment:**
   ```bash
   source venv/bin/activate
   # or use the convenience script:
   ./activate.sh
   ```

4. **Install dependencies:**
   ```bash
   uv pip install -r requirements.txt
   ```

## 📱 Usage

### Quick Start

```python
from send_whatsapp import send_message

# Send a simple message
result = send_message("5511997435829", "Hello from Z-API!")
print(f"Message sent! ID: {result['messageId']}")
```

### Using from Another Program

```python
# Import the function
from send_whatsapp import send_message

# Send welcome message
def welcome_new_user(phone, name):
    message = f"Welcome {name}! Thanks for joining us! 🎉"
    try:
        result = send_message(phone, message)
        print(f"Welcome message sent! ID: {result['messageId']}")
        return True
    except Exception as e:
        print(f"Failed to send message: {e}")
        return False

# Usage
welcome_new_user("5511997435829", "João Silva")
```

### Configuration

You can configure the API settings in several ways:

1. **Default configuration** (in `config.py`):
   ```python
   from config import DEFAULT_CONFIG
   ```

2. **Environment variables**:
   ```bash
   export WHATSAPP_BASE_URL="https://your-api-endpoint.com"
   export WHATSAPP_API_KEY="App your-api-key"
   export WHATSAPP_FROM_NUMBER="your-phone-number"
   ```
   ```python
   from config import ENV_CONFIG
   ```

3. **Custom configuration**:
   ```python
   from whatsapp_sender import WhatsAppConfig
   
   config = WhatsAppConfig(
       base_url="https://custom-endpoint.com",
       api_key="App your-custom-key",
       from_number="your-number"
   )
   ```

### Examples

Run the example script to see different usage patterns:

```bash
python examples.py
```

## 🔧 API Reference

### WhatsAppSender

Main class for sending WhatsApp messages.

#### Methods

- `send_template_message(message: TemplateMessage) -> Dict[str, Any]`
  - Send a single template message
  
- `send_bulk_messages(messages: List[TemplateMessage]) -> Dict[str, Any]`
  - Send multiple template messages in one request

### TemplateMessage

Data class representing a WhatsApp template message.

#### Parameters

- `to_number: str` - Recipient phone number
- `template_name: str` - Name of the WhatsApp template
- `language: str` - Language code (default: "en")
- `placeholders: Optional[List[str]]` - Template placeholder values
- `message_id: Optional[str]` - Custom message ID (auto-generated if not provided)

## 📊 Response Format

Successful API responses include:

```json
{
  "messages": [
    {
      "to": "5511997435829",
      "messageCount": 1,
      "messageId": "unique-message-id",
      "status": {
        "groupId": 1,
        "groupName": "PENDING",
        "id": 7,
        "name": "PENDING_ENROUTE",
        "description": "Message sent to next instance"
      }
    }
  ]
}
```

## 🔍 Testing

Test the basic functionality:

```bash
# Test original implementation
python send_whatsapp.py

# Test enhanced implementation
python whatsapp_sender.py

# Run examples
python examples.py
```

## 📦 Dependencies

- `infobip-api-python-client==5.1.1` - Official Infobip API client
- `requests==2.32.4` - HTTP library for API calls

## 🤝 Contributing

1. Make sure to activate the virtual environment before making changes
2. Follow the existing code style and patterns
3. Add type hints and documentation for new functions
4. Test your changes before committing

## 📄 License

This project is for internal use with the Infobip WhatsApp API integration.

---

**Note**: Make sure you have proper API credentials and WhatsApp Business Account setup with Infobip before using this integration.
