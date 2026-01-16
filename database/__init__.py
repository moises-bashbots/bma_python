"""
BMA Database Module

Centralized database configuration, models, and utilities.

This module provides:
- SQLAlchemy ORM models for MariaDB (18 tables)
- Database connection utilities
- Configuration management
- Helper functions for common database operations

Usage:
    from database import bma_models
    from database.bma_models import Sacado, Cedente, create_session
    
    with create_session() as session:
        sacados = session.query(Sacado).all()
"""

__version__ = "1.0.0"
__author__ = "BMA Development Team"

# Import commonly used models and functions
try:
    from .bma_models import (
        # Core tables
        Banco,
        Empresa,
        Participante,
        Cedente,
        Sacado,
        
        # PIX & Collections
        CobrancaPix,
        Pix,
        StatusCobrancaPix,
        TipoInstrucao,
        EmailCobrancaInstrucao,
        
        # Banking & Operations
        DadosBancariosEmpresa,
        OperacaoRecompra,
        TituloRecompra,
        
        # Other
        Critica,
        TipoCritica,
        Solicitacao,
        ContatoWhatsapp,
        EventoTransporte,
        
        # Helper functions
        get_mariadb_engine,
        create_session,
    )
except ImportError as e:
    # Models not available (missing dependencies)
    pass

# Import database connector utilities
try:
    from .db_connector import (
        get_mariadb_connection,
        get_mssql_connection,
    )
except ImportError:
    pass

__all__ = [
    # Core tables
    'Banco',
    'Empresa',
    'Participante',
    'Cedente',
    'Sacado',
    
    # PIX & Collections
    'CobrancaPix',
    'Pix',
    'StatusCobrancaPix',
    'TipoInstrucao',
    'EmailCobrancaInstrucao',
    
    # Banking & Operations
    'DadosBancariosEmpresa',
    'OperacaoRecompra',
    'TituloRecompra',
    
    # Other
    'Critica',
    'TipoCritica',
    'Solicitacao',
    'ContatoWhatsapp',
    'EventoTransporte',
    
    # Helper functions
    'get_mariadb_engine',
    'create_session',
    'get_mariadb_connection',
    'get_mssql_connection',
]

