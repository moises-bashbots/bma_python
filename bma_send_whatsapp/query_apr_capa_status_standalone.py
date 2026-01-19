#!/usr/bin/env python3
"""
Standalone version of APR Status Monitor for PyInstaller.
All dependencies are included inline to avoid import issues.
"""

import sys
import http.client
import json
import hashlib
import mysql.connector
from pathlib import Path
from datetime import datetime, time
from typing import List, Dict, Any, Optional, Set
from decimal import Decimal

from sqlalchemy import (
    Column, Integer, String, DateTime, Numeric, Boolean, Text,
    SmallInteger, CHAR, ForeignKey, create_engine
)
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship

# ============================================================================
# ORM Models (inline to avoid import issues)
# ============================================================================

Base = declarative_base()


class APRCapa(Base):
    """ORM model for APR_CAPA table."""
    __tablename__ = 'APR_CAPA'
    
    # Primary Key (composite)
    DATA = Column(DateTime, primary_key=True, nullable=False)
    NUMERO = Column(Integer, primary_key=True, nullable=False)
    
    # Foreign Keys and regular columns
    CEDENTE = Column(String(20))
    GERENTE = Column(String(10))
    STATUS_ATUAL = Column(String(20))
    QTD_PROPOSTOS = Column(Integer)
    VLR_PROPOSTOS = Column(Numeric(15, 2))
    QTD_APROVADOS = Column(Integer)
    VLR_APROVADOS = Column(Numeric(15, 2))
    BORDERO = Column(Integer)


class CadastroStatusFluxo(Base):
    """ORM model for CADASTRO_STATUS_FLUXO table."""
    __tablename__ = 'CADASTRO_STATUS_FLUXO'
    
    NOME_STATUS = Column(String(20), primary_key=True, nullable=False)
    ID = Column(Integer, nullable=False)
    TROCA_AUTOMATICA = Column(Boolean, nullable=False, default=False)
    SEMPRE_APARECE = Column(Boolean, nullable=False, default=False)
    COR = Column(String(30), nullable=False, default='')
    ENVIA_WHATSAPP = Column(Boolean, nullable=True, default=False)
    MENSAGEM_WHATSAPP = Column(String(2000), nullable=True)
    IDMENSAGEM_WHATSAPP = Column(String(100), nullable=True)


class Cedente(Base):
    """ORM model for cedente table."""
    __tablename__ = 'cedente'
    
    APELIDO = Column(String(20), primary_key=True, nullable=False)
    GRUPO = Column(String(20))
    CNPJ = Column(String(14))
    RAZAO_SOCIAL = Column(String(50))


class ContatoWhatsapp(Base):
    """ORM model for contato_whatsapp table (MariaDB)."""
    __tablename__ = 'contato_whatsapp'
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    phone = Column(String(100), nullable=False)
    pushname = Column(String(255))
    isGroup = Column(Boolean, default=False)
    cedente_grupo = Column(String(255))


# ============================================================================
# WhatsApp Sender Functions (inline)
# ============================================================================

def load_whatsapp_config(config_file="zapi_config.json"):
    """Load WhatsApp API configuration from JSON file."""
    # Try multiple locations
    config_paths = [
        Path(config_file),
        Path(__file__).parent / config_file,
        Path(__file__).parent / "whatsapp_config.json",
    ]
    
    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)
    
    raise FileNotFoundError(f"Configuration file '{config_file}' not found")


def send_message(phone, message, config_file="zapi_config.json"):
    """Send a WhatsApp message using Z-API."""
    config = load_whatsapp_config(config_file)
    
    endpoint_path = config["endpoint_path"].format(
        instance_id=config["instance_id"],
        token=config["token"]
    )
    
    payload = json.dumps({"phone": phone, "message": message})
    headers = {
        'Content-Type': 'application/json',
        'Client-Token': config["client_token"]
    }
    
    try:
        conn = http.client.HTTPSConnection(config["api_host"])
        conn.request("POST", endpoint_path, payload, headers)
        response = conn.getresponse()
        data = response.read()
        conn.close()
        
        result = json.loads(data.decode("utf-8"))
        
        if response.status == 200:
            return result
        else:
            return {
                "error": result.get("error", "Unknown error"),
                "status_code": response.status
            }
    except Exception as e:
        return {"error": str(e), "status_code": 0}


# ============================================================================
# Configuration and Database Functions
# ============================================================================

def load_database_config() -> dict:
    """Load database configuration from JSON file."""
    config_paths = [
        Path(__file__).parent / "databases_config.json",
        Path("databases_config.json"),
    ]

    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                config = json.load(f)
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
        Path(__file__).parent / "databases_config.json",
        Path("databases_config.json"),
    ]

    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                config = json.load(f)
                if 'databases' in config and 'mariadb' in config['databases']:
                    return config['databases']['mariadb']

    raise FileNotFoundError("databases_config.json not found for MariaDB")


def create_connection_string(config: dict) -> str:
    """Create SQLAlchemy connection string for MSSQL."""
    server = config.get('server', 'localhost')
    port = config.get('port', 1433)
    database = config.get('database') or config.get('scheme', 'BMA')
    username = config.get('username') or config.get('user', '')
    password = config.get('password', '')

    return f"mssql+pymssql://{username}:{password}@{server}:{port}/{database}"


# ============================================================================
# Tracking Functions
# ============================================================================

def get_tracking_file_path() -> Path:
    """Get the path to the tracking file for today."""
    today = datetime.now().strftime("%Y%m%d")
    tracking_dir = Path(__file__).parent / "message_tracking"
    if not tracking_dir.exists():
        tracking_dir = Path("message_tracking")
    tracking_dir.mkdir(exist_ok=True)
    return tracking_dir / f"sent_messages_{today}.json"


def load_sent_messages() -> Set[str]:
    """Load the set of message IDs that have been sent today."""
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
    """Save a message ID to the tracking file."""
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
    """Generate a unique message ID based on the record details."""
    unique_str = f"{cedente}|{status}|{date}|{numero}"
    return hashlib.md5(unique_str.encode()).hexdigest()


def is_within_allowed_hours(status: str) -> bool:
    """Check if current time is within allowed hours for sending messages."""
    now = datetime.now().time()

    if status == 'Aguardando Analista':
        start_time = time(6, 0)
        end_time = time(16, 0)
    elif status == 'Enviado para Assinar':
        start_time = time(6, 0)
        end_time = time(17, 0)
    else:
        return False

    return start_time <= now <= end_time


# ============================================================================
# WhatsApp Contact Lookup and Messaging
# ============================================================================

def lookup_whatsapp_contact(cedente: str, grupo: Optional[str]) -> Optional[Dict[str, Any]]:
    """Lookup WhatsApp contact in MariaDB database."""
    try:
        mariadb_config = load_mariadb_config()

        conn = mysql.connector.connect(
            host=mariadb_config.get('host', 'localhost'),
            port=mariadb_config.get('port', 3306),
            user=mariadb_config.get('user', 'root'),
            password=mariadb_config.get('password', ''),
            database=mariadb_config.get('database', 'BMA')
        )

        cursor = conn.cursor(dictionary=True)

        # Try matching by cedente first
        query = """
            SELECT id, phone, pushname, isGroup, cedente_grupo
            FROM contato_whatsapp
            WHERE cedente_grupo = %s AND isGroup = 1
        """
        cursor.execute(query, (cedente,))
        results = cursor.fetchall()

        # If no match and grupo exists, try matching by grupo
        if not results and grupo:
            cursor.execute(query, (grupo,))
            results = cursor.fetchall()

        cursor.close()
        conn.close()

        if len(results) == 0:
            return None
        elif len(results) == 1:
            return {
                'phone': results[0]['phone'],
                'name': results[0]['pushname'] or results[0]['cedente_grupo'],
                'cedente_grupo': results[0]['cedente_grupo']
            }
        else:
            return {
                'multiple': True,
                'count': len(results)
            }

    except Exception as e:
        print(f"Error looking up WhatsApp contact: {e}")
        return None


def send_alert_message(phone: str, message: str) -> bool:
    """Send an alert message to a specific phone number."""
    try:
        result = send_message(phone, message)
        if 'error' in result:
            print(f"⚠️ Alert failed: {result['error']}")
            return False
        return True
    except Exception as e:
        print(f"⚠️ Alert exception: {e}")
        return False


def send_status_message(contact: Dict[str, Any], record: Dict[str, Any]) -> bool:
    """Send status message to WhatsApp contact."""
    try:
        status = record['status']
        cedente = record['cedente']
        bordero = record['bordero']
        qtd_aprovados = record['qtd_aprovados']
        vlr_aprovados = record['vlr_aprovados']

        # Format currency
        vlr_formatted = f"R$ {vlr_aprovados:,.2f}".replace(',', 'X').replace('.', ',').replace('X', '.')

        # Build message based on status
        if status == 'Aguardando Analista':
            message = (
                f"🔔 *Operação em Análise*\n\n"
                f"Cedente: {cedente}\n"
                f"Borderô: {bordero}\n"
                f"Títulos Aprovados: {qtd_aprovados}\n"
                f"Valor Aprovado: {vlr_formatted}\n\n"
                f"Status: Aguardando análise do analista\n"
                f"Portal: https://portal.qcertifica.com.br/Authentication/Login.aspx"
            )
        elif status == 'Enviado para Assinar':
            message = (
                f"✅ *Operação Enviada para Assinatura*\n\n"
                f"Cedente: {cedente}\n"
                f"Borderô: {bordero}\n"
                f"Títulos Aprovados: {qtd_aprovados}\n"
                f"Valor Aprovado: {vlr_formatted}\n\n"
                f"Status: Enviado para assinatura\n"
                f"Portal: https://portal.qcertifica.com.br/Authentication/Login.aspx"
            )
        else:
            return False

        result = send_message(contact['phone'], message)

        if 'error' in result:
            print(f"⚠️ Message failed: {result}")
            return False
        else:
            print(f"✅ Message sent to {contact['name']} ({contact['phone']})")
            print(f"   Status: {status}")
            return True

    except Exception as e:
        print(f"⚠️ Message exception: {e}")
        return False


# ============================================================================
# Main Query Function
# ============================================================================

def query_apr_capa_with_status(target_date: datetime):
    """Query APR_CAPA with status filtering and WhatsApp messaging."""
    config = load_database_config()
    connection_string = create_connection_string(config)

    print("Connecting to database...")
    print(f"Server: {config.get('server')}")
    print(f"Database: {config.get('database')}")
    print(f"Query date: {target_date.strftime('%Y-%m-%d')}")
    print()

    engine = create_engine(connection_string)
    Session = sessionmaker(bind=engine)
    session = Session()

    try:
        # Query with joins
        results = session.query(
            CadastroStatusFluxo.NOME_STATUS,
            APRCapa,
            Cedente.GRUPO
        ).outerjoin(
            CadastroStatusFluxo,
            APRCapa.STATUS_ATUAL == CadastroStatusFluxo.NOME_STATUS
        ).outerjoin(
            Cedente,
            APRCapa.CEDENTE == Cedente.APELIDO
        ).filter(
            APRCapa.DATA == target_date
        ).filter(
            CadastroStatusFluxo.NOME_STATUS.in_(['Aguardando Analista', 'Enviado para Assinar'])
        ).all()

        print(f"Found {len(results)} records")
        print("=" * 120)
        print()

        # Alert phone number
        alert_phone = "+55 31 3448 3014"

        result_list = []

        for nome_status, apr_capa, grupo in results:
            record = {
                'status': nome_status,
                'data': apr_capa.DATA,
                'numero': apr_capa.NUMERO,
                'cedente': apr_capa.CEDENTE,
                'grupo': grupo or '',
                'gerente': apr_capa.GERENTE or '',
                'qtd_propostos': apr_capa.QTD_PROPOSTOS or 0,
                'vlr_propostos': float(apr_capa.VLR_PROPOSTOS or 0),
                'qtd_aprovados': apr_capa.QTD_APROVADOS or 0,
                'vlr_aprovados': float(apr_capa.VLR_APROVADOS or 0),
                'bordero': apr_capa.BORDERO or 0,
            }
            result_list.append(record)

            # WhatsApp processing
            print(f"📱 Processing WhatsApp for: {apr_capa.CEDENTE}")
            print("-" * 80)

            # Generate unique message ID
            message_id = generate_message_id(
                apr_capa.CEDENTE,
                nome_status,
                apr_capa.DATA.strftime("%Y%m%d"),
                apr_capa.NUMERO
            )

            # Check if already sent
            sent_messages = load_sent_messages()
            if message_id in sent_messages:
                print(f"✓ Message already sent today for {apr_capa.CEDENTE} (Status: {nome_status})")
                print(f"   Skipping duplicate message")
                continue

            # Check time window
            if not is_within_allowed_hours(nome_status):
                current_time = datetime.now().strftime("%H:%M:%S")
                print(f"⏰ Outside allowed hours for status '{nome_status}' (current time: {current_time})")
                print(f"   Skipping WhatsApp message for {apr_capa.CEDENTE}")
                continue

            contact = lookup_whatsapp_contact(apr_capa.CEDENTE, grupo)

            if contact is None:
                print(f"⚠️ No WhatsApp contact found for {apr_capa.CEDENTE}")
                alert_msg = f"O cedente {apr_capa.CEDENTE} ainda não tem um grupo de Whatsapp criado para a BMA."
                send_alert_message(alert_phone, alert_msg)

            elif contact.get('multiple'):
                print(f"⚠️ Multiple WhatsApp contacts found for {apr_capa.CEDENTE} ({contact['count']} matches)")
                alert_msg = f"Múltiplos grupos de WhatsApp encontrados para o cedente {apr_capa.CEDENTE}. Verificar cadastro."
                send_alert_message(alert_phone, alert_msg)

            else:
                print(f"✓ Contact found: {contact['name']} ({contact['phone']})")
                success = send_status_message(contact, record)

                if success:
                    save_sent_message(message_id)
                    print(f"✓ Message tracked to prevent duplicates")

        # Print summary table
        print()
        print(f"{'NOME_STATUS':<20} {'DATA':<12} {'NUM':<6} {'CEDENTE':<20} {'GRUPO':<15} {'QTD_PROP':<10} "
              f"{'VLR_PROP':<15} {'QTD_APR':<10} {'VLR_APR':<15} {'GERENTE':<12}")
        print("=" * 150)

        for r in result_list:
            print(f"{r['status']:<20} {r['data'].strftime('%Y-%m-%d'):<12} {r['numero']:<6} "
                  f"{r['cedente']:<20} {r['grupo']:<15} {r['qtd_propostos']:<10} "
                  f"{r['vlr_propostos']:>15,.2f} {r['qtd_aprovados']:<10} "
                  f"{r['vlr_aprovados']:>15,.2f} {r['gerente']:<12}")

        return result_list

    finally:
        session.close()


def main():
    """Main entry point."""
    # Use current date
    target_date = datetime.now()
    date_str = target_date.strftime("%Y%m%d")

    print("=" * 120)
    print(f"APR_CAPA WITH STATUS QUERY - Date: {date_str} (Current Date)")
    print("=" * 120)
    print()

    try:
        query_apr_capa_with_status(target_date)
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()

