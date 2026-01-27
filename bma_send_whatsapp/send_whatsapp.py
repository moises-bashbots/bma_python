import http.client
import json
from typing import Optional
from pathlib import Path
import sys

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent))

try:
    from bma_send_whatsapp.message_logger import log_whatsapp_message
    LOGGING_ENABLED = True
except ImportError:
    LOGGING_ENABLED = False
    print("⚠️ Message logging disabled - could not import message_logger")


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


def send_message(
    phone: str,
    message: str,
    config_file: str = "whatsapp_config.json",
    log_to_db: bool = True,
    message_metadata: Optional[dict] = None
):
    """
    Send a WhatsApp message using Z-API and log to database

    Args:
        phone (str): Phone number to send message to (e.g., "5511997435829")
        message (str): Message text to send
        config_file (str): Path to configuration file (optional)
        log_to_db (bool): Whether to log message to database (default: True)
        message_metadata (dict): Additional metadata for logging (optional)
            - message_id: Unique message ID
            - message_hash: MD5 hash for duplicate detection
            - nome_contato: Contact name
            - is_group: Whether message is to a group
            - cedente: Cedente name
            - grupo: Grupo name
            - data_proposta: Proposal date
            - numero_proposta: Proposal number
            - bordero: Borderô number
            - status_fluxo: Workflow status
            - qtd_recebiveis: Quantity of receivables
            - valor_total: Total value
            - tipo_mensagem: Message type (status_update, alert, notification, manual)
            - usuario: User who triggered the message
            - origem: Message origin

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

    response_data = None
    error_message = None

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

        # Log to database if enabled
        if log_to_db and LOGGING_ENABLED:
            try:
                metadata = message_metadata or {}
                log_whatsapp_message(
                    telefone_destino=phone,
                    mensagem=message,
                    tipo_mensagem=metadata.get('tipo_mensagem', 'notification'),
                    status_envio='success' if res.status == 200 else 'failed',
                    message_id=metadata.get('message_id'),
                    message_hash=metadata.get('message_hash'),
                    nome_contato=metadata.get('nome_contato'),
                    is_group=metadata.get('is_group', False),
                    cedente=metadata.get('cedente'),
                    grupo=metadata.get('grupo'),
                    data_proposta=metadata.get('data_proposta'),
                    numero_proposta=metadata.get('numero_proposta'),
                    bordero=metadata.get('bordero'),
                    status_fluxo=metadata.get('status_fluxo'),
                    qtd_recebiveis=metadata.get('qtd_recebiveis'),
                    valor_total=metadata.get('valor_total'),
                    status_code=res.status,
                    api_response=response_data,
                    erro_mensagem=None,
                    config_file=config_file,
                    api_provider='Z-API',
                    usuario=metadata.get('usuario'),
                    origem=metadata.get('origem', 'automated'),
                    tentativas_envio=metadata.get('tentativas_envio', 1)
                )
            except Exception as log_error:
                print(f"⚠️ Failed to log message to database: {log_error}")

        return response_data

    except Exception as e:
        error_message = str(e)

        # Log failed message to database if enabled
        if log_to_db and LOGGING_ENABLED:
            try:
                metadata = message_metadata or {}
                log_whatsapp_message(
                    telefone_destino=phone,
                    mensagem=message,
                    tipo_mensagem=metadata.get('tipo_mensagem', 'notification'),
                    status_envio='failed',
                    message_id=metadata.get('message_id'),
                    message_hash=metadata.get('message_hash'),
                    nome_contato=metadata.get('nome_contato'),
                    is_group=metadata.get('is_group', False),
                    cedente=metadata.get('cedente'),
                    grupo=metadata.get('grupo'),
                    data_proposta=metadata.get('data_proposta'),
                    numero_proposta=metadata.get('numero_proposta'),
                    bordero=metadata.get('bordero'),
                    status_fluxo=metadata.get('status_fluxo'),
                    qtd_recebiveis=metadata.get('qtd_recebiveis'),
                    valor_total=metadata.get('valor_total'),
                    status_code=None,
                    api_response=None,
                    erro_mensagem=error_message,
                    config_file=config_file,
                    api_provider='Z-API',
                    usuario=metadata.get('usuario'),
                    origem=metadata.get('origem', 'automated'),
                    tentativas_envio=metadata.get('tentativas_envio', 1)
                )
            except Exception as log_error:
                print(f"⚠️ Failed to log error to database: {log_error}")

        raise Exception(f"Failed to send message: {error_message}")
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
