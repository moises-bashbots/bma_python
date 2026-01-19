#!/usr/bin/env python3
"""
Query APR_CAPA with CADASTRO_STATUS_FLUXO using ORM models.

This script performs the following query:
SELECT csf.NOME_STATUS, ac.*
FROM APR_CAPA ac WITH(NOLOCK), CADASTRO_STATUS_FLUXO csf
WHERE data = '20251013'
AND ac.status_atual = csf.NOME_STATUS

Additionally, it looks up WhatsApp contacts and sends status messages.
"""

import sys
from pathlib import Path
from datetime import datetime, time
from typing import List, Dict, Any, Optional, Set
import json
import mysql.connector
import hashlib

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Import existing models
from slack_integration_alerts.models import APRCapa, CadastroStatusFluxo, Cedente
from database.bma_models import ContatoWhatsapp

# Import WhatsApp sender
from bma_send_whatsapp.send_whatsapp import send_message


def load_database_config() -> dict:
    """Load database configuration from JSON file."""
    config_paths = [
        Path(__file__).parent / "databases_config.json",
        Path(__file__).parent.parent / "slack_integration_alerts" / "databases_config.json",
        Path(__file__).parent.parent / "database" / "databases_config.json",
    ]

    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                config = json.load(f)
                # Try different config structures
                if 'databases' in config:
                    return config['databases']['mssql']
                elif 'BMA' in config:
                    return config['BMA']
                else:
                    return config

    raise FileNotFoundError("databases_config.json not found")


def load_mariadb_config() -> dict:
    """Load MariaDB configuration from JSON file."""
    config_paths = [
        Path(__file__).parent.parent / "database" / "databases_config.json",
    ]

    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                config = json.load(f)
                return config['databases']['mariadb']

    raise FileNotFoundError("databases_config.json not found for MariaDB")


def create_connection_string(config: dict) -> str:
    """Create SQLAlchemy connection string for MSSQL."""
    server = config.get('server', 'localhost')
    port = config.get('port', 1433)
    database = config.get('database') or config.get('scheme', 'BMA')
    username = config.get('username') or config.get('user', '')
    password = config.get('password', '')

    # Using pymssql driver
    return f"mssql+pymssql://{username}:{password}@{server}:{port}/{database}"


def lookup_whatsapp_contact(cedente: str, grupo: Optional[str]) -> Optional[Dict[str, Any]]:
    """
    Lookup WhatsApp contact in MariaDB database.

    Args:
        cedente: Cedente name from APR_CAPA
        grupo: Grupo name from cedente table

    Returns:
        Dictionary with contact info or None if not found
    """
    try:
        # Load MariaDB config
        mariadb_config = load_mariadb_config()

        # Connect to MariaDB
        conn = mysql.connector.connect(
            host=mariadb_config['server'],
            port=mariadb_config['port'],
            user=mariadb_config['user'],
            password=mariadb_config['password'],
            database=mariadb_config['scheme']
        )

        cursor = conn.cursor(dictionary=True)

        # First try to match by CEDENTE
        query = """
            SELECT phone, name, cedente_grupo
            FROM contato_whatsapp
            WHERE UPPER(cedente_grupo) = UPPER(%s)
            AND isGroup = 1
        """

        cursor.execute(query, (cedente,))
        results = cursor.fetchall()

        # If no match and grupo exists, try matching by GRUPO
        if not results and grupo:
            cursor.execute(query, (grupo,))
            results = cursor.fetchall()

        cursor.close()
        conn.close()

        # Return results
        if len(results) == 0:
            return None
        elif len(results) == 1:
            return results[0]
        else:
            # Multiple matches found
            return {'multiple': True, 'count': len(results)}

    except Exception as e:
        print(f"❌ Error looking up WhatsApp contact: {e}")
        return None


def is_within_allowed_hours(status: str) -> bool:
    """
    Check if current time is within allowed hours for sending messages.

    Args:
        status: Status name ("Aguardando Analista" or "Enviado para Assinar")

    Returns:
        True if within allowed hours, False otherwise
    """
    now = datetime.now().time()

    if status == 'Aguardando Analista':
        # Allowed from 6h to 16h
        start_time = time(6, 0)
        end_time = time(16, 0)
    elif status == 'Enviado para Assinar':
        # Allowed from 6h to 17h
        start_time = time(6, 0)
        end_time = time(17, 0)
    else:
        # Unknown status, don't send
        return False

    return start_time <= now <= end_time


def get_tracking_file_path() -> Path:
    """
    Get the path to the message tracking file for today.

    Returns:
        Path to the tracking file
    """
    today = datetime.now().strftime("%Y%m%d")
    tracking_dir = Path(__file__).parent / "message_tracking"
    tracking_dir.mkdir(exist_ok=True)
    return tracking_dir / f"sent_messages_{today}.json"


def load_sent_messages() -> Set[str]:
    """
    Load the set of message IDs that have been sent today.

    Returns:
        Set of message IDs
    """
    tracking_file = get_tracking_file_path()

    if tracking_file.exists():
        try:
            with open(tracking_file, 'r') as f:
                data = json.load(f)
                return set(data.get('sent_messages', []))
        except Exception as e:
            print(f"⚠️ Error loading tracking file: {e}")
            return set()

    return set()


def save_sent_message(message_id: str) -> None:
    """
    Save a message ID to the tracking file.

    Args:
        message_id: Unique identifier for the message
    """
    tracking_file = get_tracking_file_path()
    sent_messages = load_sent_messages()
    sent_messages.add(message_id)

    try:
        with open(tracking_file, 'w') as f:
            json.dump({
                'sent_messages': list(sent_messages),
                'last_updated': datetime.now().isoformat()
            }, f, indent=2)
    except Exception as e:
        print(f"⚠️ Error saving tracking file: {e}")


def generate_message_id(cedente: str, status: str, date: str, numero: int) -> str:
    """
    Generate a unique message ID based on the record details.

    Args:
        cedente: Cedente name
        status: Status name
        date: Date string
        numero: Record number

    Returns:
        Unique message ID (hash)
    """
    # Create a unique string from the record details
    unique_str = f"{cedente}|{status}|{date}|{numero}"
    # Generate a hash for compact storage
    return hashlib.md5(unique_str.encode()).hexdigest()


def send_alert_message(phone: str, message: str) -> bool:
    """
    Send alert message to specified phone number.

    Args:
        phone: Phone number to send to
        message: Message text

    Returns:
        True if successful, False otherwise
    """
    try:
        config_path = Path(__file__).parent / "whatsapp_config.json"
        result = send_message(phone, message, str(config_path))

        if result.get('status_code') == 200:
            print(f"✅ Alert sent to {phone}")
            return True
        else:
            print(f"⚠️ Alert failed: {result}")
            return False

    except Exception as e:
        print(f"❌ Error sending alert: {e}")
        return False


def send_status_message(contact: Dict[str, Any], record: Dict[str, Any]) -> bool:
    """
    Send status message to WhatsApp contact based on APR_CAPA status.

    Args:
        contact: WhatsApp contact info
        record: APR_CAPA record data

    Returns:
        True if successful, False otherwise
    """
    try:
        phone = contact['phone']
        status = record['NOME_STATUS']

        # Build message based on status
        if status == 'Aguardando Analista':
            message = (
                f"Olá,\n"
                f"Seu borderô com {record['QTD_PROPOSTOS']} recebíveis, "
                f"no valor total proposto de R$ {record['VLR_PROPOSTOS']:,.2f} "
                f"foi recebido com sucesso e encontra-se em análise 🔎\n"
                f"Em breve daremos um retorno.\n"
                f"Obrigada (o). ✨"
            )
        elif status == 'Enviado para Assinar':
            message = (
                f"Olá,\n"
                f"O borderô {record['BORDERO']}, com {record['QTD_APROVADOS']} recebíveis, "
                f"no valor total de R$ {record['VLR_APROVADOS']:,.2f} "
                f"está disponível para assinatura.\n"
                f"Obrigada (o). 😊\n"
                f"🖋️Assine aqui:\n"
                f"https://portal.qcertifica.com.br/Authentication/Login.aspx"
            )
        else:
            print(f"⚠️ Unknown status: {status}")
            return False

        # Send message
        config_path = Path(__file__).parent / "whatsapp_config.json"
        result = send_message(phone, message, str(config_path))

        if result.get('status_code') == 200:
            print(f"✅ Message sent to {contact['name']} ({phone})")
            print(f"   Status: {status}")
            return True
        else:
            print(f"⚠️ Message failed: {result}")
            return False

    except Exception as e:
        print(f"❌ Error sending status message: {e}")
        return False


def query_apr_capa_with_status(target_date: str) -> List[Dict[str, Any]]:
    """
    Query APR_CAPA joined with CADASTRO_STATUS_FLUXO.
    
    Args:
        target_date: Date string in format 'YYYYMMDD' (e.g., '20251013')
    
    Returns:
        List of dictionaries containing query results
    """
    # Load configuration
    config = load_database_config()
    connection_string = create_connection_string(config)
    
    # Create engine and session
    engine = create_engine(connection_string, echo=False)
    Session = sessionmaker(bind=engine)
    session = Session()
    
    try:
        # Parse date
        query_date = datetime.strptime(target_date, '%Y%m%d')
        
        print(f"Connecting to database...")
        print(f"Server: {config.get('server')}")
        print(f"Database: {config.get('database')}")
        print(f"Query date: {query_date.strftime('%Y-%m-%d')}")
        print()
        
        # Perform the join query with cedente table
        # Filter only for specific statuses
        target_statuses = ['Aguardando Analista', 'Enviado para Assinar']

        results = session.query(
            CadastroStatusFluxo.NOME_STATUS,
            APRCapa,
            Cedente.grupo
        ).join(
            APRCapa,
            APRCapa.status_atual == CadastroStatusFluxo.NOME_STATUS
        ).outerjoin(
            Cedente,
            APRCapa.CEDENTE == Cedente.apelido
        ).filter(
            APRCapa.DATA == query_date,
            APRCapa.status_atual.in_(target_statuses)
        ).all()
        
        print(f"Found {len(results)} records")
        print("="*120)

        # Convert results to list of dictionaries and process WhatsApp messages
        result_list = []
        alert_phone = "553134483014"  # Alert number

        for nome_status, apr_capa, grupo in results:
            record = {
                'NOME_STATUS': nome_status,
                'DATA': apr_capa.DATA,
                'NUMERO': apr_capa.NUMERO,
                'CEDENTE': apr_capa.CEDENTE,
                'GRUPO': grupo,
                'GERENTE': apr_capa.GERENTE,
                'BORDERO': apr_capa.BORDERO,
                'status_atual': apr_capa.status_atual,
                'empresa': apr_capa.empresa,
                'QTD_PROPOSTOS': apr_capa.QTD_PROPOSTOS,
                'VLR_PROPOSTOS': apr_capa.VLR_PROPOSTOS,
                'QTD_APROVADOS': apr_capa.QTD_APROVADOS,
                'VLR_APROVADOS': apr_capa.VLR_APROVADOS,
                'SEQUENCIAL': apr_capa.SEQUENCIAL,
                'observacao': apr_capa.observacao,
                'usuario': apr_capa.usuario,
            }
            result_list.append(record)

            # WhatsApp contact lookup and messaging
            print(f"\n📱 Processing WhatsApp for: {apr_capa.CEDENTE}")
            print("-" * 80)

            # Generate unique message ID for this record
            message_id = generate_message_id(
                apr_capa.CEDENTE,
                nome_status,
                apr_capa.DATA.strftime("%Y%m%d"),
                apr_capa.NUMERO
            )

            # Check if message was already sent today
            sent_messages = load_sent_messages()
            if message_id in sent_messages:
                print(f"✓ Message already sent today for {apr_capa.CEDENTE} (Status: {nome_status})")
                print(f"   Skipping duplicate message")
                continue

            # Check if we're within allowed hours for this status
            if not is_within_allowed_hours(nome_status):
                current_time = datetime.now().strftime("%H:%M:%S")
                print(f"⏰ Outside allowed hours for status '{nome_status}' (current time: {current_time})")
                print(f"   Skipping WhatsApp message for {apr_capa.CEDENTE}")
                continue

            contact = lookup_whatsapp_contact(apr_capa.CEDENTE, grupo)

            if contact is None:
                # No contact found - send alert
                print(f"⚠️ No WhatsApp contact found for {apr_capa.CEDENTE}")
                alert_msg = f"O cedente {apr_capa.CEDENTE} ainda não tem um grupo de Whatsapp criado para a BMA."
                send_alert_message(alert_phone, alert_msg)

            elif contact.get('multiple'):
                # Multiple contacts found - send alert
                print(f"⚠️ Multiple WhatsApp contacts found for {apr_capa.CEDENTE} ({contact['count']} matches)")
                alert_msg = f"Múltiplos grupos de WhatsApp encontrados para o cedente {apr_capa.CEDENTE}. Verificar cadastro."
                send_alert_message(alert_phone, alert_msg)

            else:
                # Single contact found - send status message
                print(f"✓ Contact found: {contact['name']} ({contact['phone']})")
                success = send_status_message(contact, record)

                # If message was sent successfully, save the message ID
                if success:
                    save_sent_message(message_id)
                    print(f"✓ Message tracked to prevent duplicates")

        return result_list
        
    finally:
        session.close()


def main():
    """Main function."""
    # Use current date
    target_date = datetime.now().strftime("%Y%m%d")

    print("="*120)
    print(f"APR_CAPA WITH STATUS QUERY - Date: {target_date} (Current Date)")
    print("="*120)
    print()
    
    try:
        results = query_apr_capa_with_status(target_date)
        
        # Display results
        if results:
            print(f"\n{'NOME_STATUS':<20} {'DATA':<12} {'NUM':<6} {'CEDENTE':<20} {'GRUPO':<15} "
                  f"{'QTD_PROP':<10} {'VLR_PROP':<15} {'QTD_APR':<10} {'VLR_APR':<15} {'GERENTE':<12}")
            print("="*160)

            for record in results:
                print(f"{record['NOME_STATUS']:<20} "
                      f"{record['DATA'].strftime('%Y-%m-%d'):<12} "
                      f"{record['NUMERO']:<6} "
                      f"{(record['CEDENTE'] or ''):<20} "
                      f"{(record['GRUPO'] or ''):<15} "
                      f"{(record['QTD_PROPOSTOS'] or 0):<10} "
                      f"{(record['VLR_PROPOSTOS'] or 0):>15,.2f} "
                      f"{(record['QTD_APROVADOS'] or 0):<10} "
                      f"{(record['VLR_APROVADOS'] or 0):>15,.2f} "
                      f"{(record['GERENTE'] or ''):<12}")
        else:
            print("No records found for the specified date.")
        
        return 0
        
    except Exception as e:
        print(f"✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == '__main__':
    sys.exit(main())

