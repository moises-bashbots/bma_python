#!/usr/bin/env python3
"""
WhatsApp Message Logger
Logs all WhatsApp messages sent to the mensagens_whatsapp table in MariaDB
"""

import json
from datetime import datetime, date
from decimal import Decimal
from typing import Optional, Dict, Any
from pathlib import Path
import sys

# Force mysql-connector-python to use pure Python implementation
import os
os.environ['MYSQL_CONNECTOR_PYTHON_USE_PURE'] = '1'
import mysql.connector

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session

# Import the model
sys.path.insert(0, str(Path(__file__).parent.parent))
from database.bma_models import MensagemWhatsapp


def get_executable_dir() -> Path:
    """Get the directory where the executable/script is located."""
    # When running as PyInstaller binary, use sys.executable
    # When running as script, use __file__
    if getattr(sys, 'frozen', False):
        # Running as compiled binary
        return Path(sys.executable).parent
    else:
        # Running as script
        return Path(__file__).parent


def load_mariadb_config(config_file: Optional[str] = None) -> dict:
    """
    Load MariaDB configuration from JSON file.

    Args:
        config_file: Path to config file (optional)

    Returns:
        Dictionary with MariaDB configuration
    """
    if config_file and Path(config_file).exists():
        config_path = Path(config_file)
    else:
        # Try default locations
        exe_dir = get_executable_dir()
        config_paths = [
            exe_dir / "databases_config.json",  # Same directory as executable
            Path(__file__).parent.parent / "database" / "databases_config.json",
            Path(__file__).parent / "databases_config.json",
        ]

        config_path = None
        for path in config_paths:
            if path.exists():
                config_path = path
                break

        if not config_path:
            raise FileNotFoundError("databases_config.json not found for MariaDB")

    with open(config_path, 'r') as f:
        config = json.load(f)
        return config['databases']['mariadb']


def create_mariadb_session(config: Optional[dict] = None) -> Session:
    """
    Create a SQLAlchemy session for MariaDB.

    Args:
        config: MariaDB configuration dictionary (optional)

    Returns:
        SQLAlchemy Session object
    """
    if config is None:
        config = load_mariadb_config()

    # Create connection string with use_pure parameter to force pure Python implementation
    # This avoids issues with native plugin loading in PyInstaller binaries
    connection_string = (
        f"mysql+mysqlconnector://{config['user']}:{config['password']}"
        f"@{config['server']}:{config['port']}/{config['scheme']}"
        f"?use_pure=True"
    )

    # Create engine and session
    engine = create_engine(connection_string, echo=False)
    SessionLocal = sessionmaker(bind=engine)
    return SessionLocal()


def log_whatsapp_message(
    telefone_destino: str,
    mensagem: str,
    tipo_mensagem: str = 'notification',
    status_envio: str = 'pending',
    message_id: Optional[str] = None,
    message_hash: Optional[str] = None,
    nome_contato: Optional[str] = None,
    is_group: bool = False,
    cedente: Optional[str] = None,
    grupo: Optional[str] = None,
    data_proposta: Optional[date] = None,
    numero_proposta: Optional[int] = None,
    bordero: Optional[str] = None,
    status_fluxo: Optional[str] = None,
    qtd_recebiveis: Optional[int] = None,
    valor_total: Optional[Decimal] = None,
    status_code: Optional[int] = None,
    api_response: Optional[Dict[str, Any]] = None,
    erro_mensagem: Optional[str] = None,
    config_file: Optional[str] = None,
    api_provider: str = 'Z-API',
    usuario: Optional[str] = None,
    origem: str = 'automated',
    tentativas_envio: int = 1,
    session: Optional[Session] = None
) -> Optional[int]:
    """
    Log a WhatsApp message to the database.
    
    Args:
        telefone_destino: Recipient phone number
        mensagem: Message text
        tipo_mensagem: Message type (status_update, alert, notification, manual)
        status_envio: Send status (success, failed, pending)
        message_id: Unique message ID
        message_hash: MD5 hash for duplicate detection
        nome_contato: Contact name
        is_group: Whether message was sent to a group
        cedente: Cedente name
        grupo: Grupo name
        data_proposta: Proposal date
        numero_proposta: Proposal number
        bordero: Borderô number
        status_fluxo: Workflow status
        qtd_recebiveis: Quantity of receivables
        valor_total: Total value
        status_code: HTTP status code
        api_response: Full API response
        erro_mensagem: Error message
        config_file: Config file used
        api_provider: API provider name
        usuario: User who triggered the message
        origem: Message origin
        tentativas_envio: Number of send attempts
        session: SQLAlchemy session (optional, will create if not provided)
        
    Returns:
        ID of the created message record, or None if failed
    """
    close_session = False
    if session is None:
        session = create_mariadb_session()
        close_session = True
    
    try:
        # Create message record
        message_record = MensagemWhatsapp(
            message_id=message_id,
            message_hash=message_hash,
            telefone_destino=telefone_destino,
            nome_contato=nome_contato,
            is_group=is_group,
            mensagem=mensagem,
            tipo_mensagem=tipo_mensagem,
            cedente=cedente,
            grupo=grupo,
            data_proposta=data_proposta,
            numero_proposta=numero_proposta,
            bordero=bordero,
            status_fluxo=status_fluxo,
            qtd_recebiveis=qtd_recebiveis,
            valor_total=valor_total,
            status_envio=status_envio,
            status_code=status_code,
            api_response=api_response,
            erro_mensagem=erro_mensagem,
            config_file=config_file,
            api_provider=api_provider,
            usuario=usuario,
            origem=origem,
            tentativas_envio=tentativas_envio
        )
        
        session.add(message_record)
        session.commit()
        
        return message_record.id_mensagem
        
    except Exception as e:
        session.rollback()
        print(f"❌ Error logging WhatsApp message to database: {e}")
        return None
        
    finally:
        if close_session:
            session.close()

