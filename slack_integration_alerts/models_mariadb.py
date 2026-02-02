#!/usr/bin/env python3
"""
SQLAlchemy ORM models for MariaDB tables:
- apr_valid_records
- apr_invalid_records
- apr_status_history
- apr_processing_log
- apr_daily_summary
"""

from datetime import datetime, date
from decimal import Decimal
from typing import Optional
from sqlalchemy import (
    Column, Integer, String, Date, DateTime, Numeric, Boolean,
    SmallInteger, TIMESTAMP, Index, Text, Enum
)
from sqlalchemy.ext.declarative import declarative_base

MariaDBBase = declarative_base()


class APRValidRecord(MariaDBBase):
    """
    ORM model for apr_valid_records table.
    
    Tracks valid APR records (proposals with valid DUPLICATA and SEUNO).
    Uses composite primary key: (DATA, PROPOSTA, CEDENTE, RAMO)
    """
    __tablename__ = 'apr_valid_records'
    
    # Composite Primary Key fields
    DATA = Column(
        Date,
        primary_key=True,
        nullable=False,
        comment='Proposal date from APR_CAPA'
    )
    PROPOSTA = Column(
        Integer,
        primary_key=True,
        nullable=False,
        comment='Proposal number from APR_CAPA'
    )
    CEDENTE = Column(
        String(100),
        primary_key=True,
        nullable=False,
        comment='Client name from APR_CAPA'
    )
    RAMO = Column(
        String(100),
        primary_key=True,
        nullable=False,
        comment='Business sector/rating from cedente table'
    )
    
    # Additional fields
    GERENTE = Column(
        String(100),
        comment='Manager name'
    )
    EMPRESA = Column(
        String(100),
        comment='Company name'
    )
    STATUS = Column(
        String(50),
        comment='Current workflow status'
    )
    QTD_APROVADOS = Column(
        Integer,
        default=0,
        comment='Quantity of approved titles'
    )
    VLR_APROVADOS = Column(
        Numeric(15, 2),
        default=0.00,
        comment='Total approved value'
    )
    VALOR_TITULOS = Column(
        Numeric(15, 2),
        default=0.00,
        comment='Sum of current title values'
    )
    QTD_TITULOS = Column(
        Integer,
        default=0,
        comment='Count of titles in this proposal'
    )
    
    # Processing status fields
    is_processado = Column(
        SmallInteger,  # TINYINT maps to SmallInteger in SQLAlchemy
        default=0,
        nullable=False,
        comment='Processing status: 0=not processed, 1=processed'
    )
    is_bot_processed = Column(
        SmallInteger,  # TINYINT maps to SmallInteger in SQLAlchemy
        default=0,
        nullable=False,
        comment='Bot processing status: 0=not processed by bot, 1=bot clicked Processar'
    )

    # Tracking fields
    first_seen = Column(
        TIMESTAMP,
        server_default='CURRENT_TIMESTAMP',
        comment='When record was first inserted'
    )
    last_updated = Column(
        TIMESTAMP,
        server_default='CURRENT_TIMESTAMP',
        server_onupdate='CURRENT_TIMESTAMP',
        comment='Last update timestamp'
    )
    update_count = Column(
        Integer,
        default=0,
        comment='Number of times record was updated'
    )
    
    # Indexes are defined in the table creation script
    __table_args__ = (
        Index('idx_status', 'STATUS'),
        Index('idx_gerente', 'GERENTE'),
        Index('idx_data', 'DATA'),
        Index('idx_last_updated', 'last_updated'),
        Index('idx_is_processado', 'is_processado'),
        Index('idx_is_bot_processed', 'is_bot_processed'),
    )
    
    def __repr__(self):
        return (
            f"<APRValidRecord("
            f"DATA={self.DATA}, "
            f"PROPOSTA={self.PROPOSTA}, "
            f"CEDENTE={self.CEDENTE}, "
            f"RAMO={self.RAMO}, "
            f"STATUS={self.STATUS}, "
            f"is_processado={self.is_processado}"
            f")>"
        )
    
    @property
    def is_processed(self) -> bool:
        """
        Boolean property for is_processado field.
        Returns True if processed (1), False if not processed (0).
        """
        return bool(self.is_processado)
    
    @is_processed.setter
    def is_processed(self, value: bool):
        """
        Set is_processado field using boolean value.
        True sets to 1, False sets to 0.
        """
        self.is_processado = 1 if value else 0
    
    def mark_as_processed(self):
        """Mark this record as processed."""
        self.is_processado = 1
    
    def mark_as_unprocessed(self):
        """Mark this record as not processed."""
        self.is_processado = 0


class APRInvalidRecord(MariaDBBase):
    """
    ORM model for apr_invalid_records table.

    Tracks invalid APR records (NFEChave, DUPLICATA, SEUNO failures).
    """
    __tablename__ = 'apr_invalid_records'

    # Primary key
    id = Column(Integer, primary_key=True, autoincrement=True)

    # Proposal identification
    DATA = Column(Date, nullable=False, comment='Proposal date')
    PROPOSTA = Column(Integer, nullable=False, comment='Proposal number')
    CEDENTE = Column(String(100), nullable=False, comment='Client name')
    RAMO = Column(String(100), comment='Rating/sector')
    GERENTE = Column(String(100), comment='Manager name')
    EMPRESA = Column(String(100), comment='Company name')
    STATUS = Column(String(50), comment='Status when detected')
    PRODUTO = Column(String(100), comment='Product type')

    # Validation details
    VALIDATION_TYPE = Column(
        Enum('NFECHAVE', 'DUPLICATA', 'SEUNO'),
        nullable=False,
        comment='Type of validation failure'
    )
    MOTIVO = Column(String(255), comment='Error reason/message')
    NFE_CHAVE = Column(String(50), comment='NFE key value')
    DUPLICATA = Column(String(50), comment='DUPLICATA value')
    SEUNO = Column(String(20), comment='SEUNO value')

    # Tracking timestamps
    detected_at = Column(
        TIMESTAMP,
        server_default='CURRENT_TIMESTAMP',
        comment='When first detected'
    )
    alerted_at = Column(TIMESTAMP, nullable=True, comment='When Slack alert was sent')
    is_resolved = Column(SmallInteger, default=0, comment='0=unresolved, 1=resolved')
    resolved_at = Column(TIMESTAMP, nullable=True, comment='When resolved')

    # Indexes
    __table_args__ = (
        Index('idx_proposal', 'DATA', 'PROPOSTA'),
        Index('idx_cedente', 'CEDENTE'),
        Index('idx_validation_type', 'VALIDATION_TYPE'),
        Index('idx_is_resolved', 'is_resolved'),
        Index('idx_detected_at', 'detected_at'),
        Index('idx_resolved_at', 'resolved_at'),
    )

    def __repr__(self):
        return (
            f"<APRInvalidRecord("
            f"id={self.id}, "
            f"DATA={self.DATA}, "
            f"PROPOSTA={self.PROPOSTA}, "
            f"VALIDATION_TYPE={self.VALIDATION_TYPE}, "
            f"is_resolved={self.is_resolved}"
            f")>"
        )


class APRStatusHistory(MariaDBBase):
    """
    ORM model for apr_status_history table.

    Tracks status changes for proposals over time.
    """
    __tablename__ = 'apr_status_history'

    # Primary key
    id = Column(Integer, primary_key=True, autoincrement=True)

    # Proposal identification
    DATA = Column(Date, nullable=False, comment='Proposal date')
    PROPOSTA = Column(Integer, nullable=False, comment='Proposal number')
    CEDENTE = Column(String(100), nullable=False, comment='Client name')
    RAMO = Column(String(100), comment='Rating/sector')

    # Status change details
    OLD_STATUS = Column(String(50), comment='Previous status')
    NEW_STATUS = Column(String(50), nullable=False, comment='New status')

    # Value changes
    OLD_VLR_APROVADOS = Column(Numeric(15, 2), comment='Previous approved value')
    NEW_VLR_APROVADOS = Column(Numeric(15, 2), comment='New approved value')
    OLD_QTD_TITULOS = Column(Integer, comment='Previous title count')
    NEW_QTD_TITULOS = Column(Integer, comment='New title count')

    # Tracking
    changed_at = Column(
        TIMESTAMP,
        server_default='CURRENT_TIMESTAMP',
        comment='When change was detected'
    )
    CHANGE_SOURCE = Column(
        String(50),
        default='SYSTEM',
        comment='SYSTEM or BOT'
    )

    # Indexes
    __table_args__ = (
        Index('idx_proposal', 'DATA', 'PROPOSTA'),
        Index('idx_cedente', 'CEDENTE'),
        Index('idx_new_status', 'NEW_STATUS'),
        Index('idx_changed_at', 'changed_at'),
    )

    def __repr__(self):
        return (
            f"<APRStatusHistory("
            f"id={self.id}, "
            f"PROPOSTA={self.PROPOSTA}, "
            f"OLD_STATUS={self.OLD_STATUS}, "
            f"NEW_STATUS={self.NEW_STATUS}"
            f")>"
        )


class APRProcessingLog(MariaDBBase):
    """
    ORM model for apr_processing_log table.

    Logs each execution of the validation program.
    """
    __tablename__ = 'apr_processing_log'

    # Primary key
    id = Column(Integer, primary_key=True, autoincrement=True)

    # Run identification
    run_timestamp = Column(
        TIMESTAMP,
        server_default='CURRENT_TIMESTAMP',
        comment='When program ran'
    )
    target_date = Column(Date, nullable=False, comment='Date being processed')

    # Processing statistics
    total_records_queried = Column(Integer, default=0, comment='Total records from MSSQL')
    valid_records = Column(Integer, default=0, comment='Count of valid records')
    invalid_nfechave = Column(Integer, default=0, comment='Count of NFEChave failures')
    invalid_duplicata = Column(Integer, default=0, comment='Count of DUPLICATA failures')
    invalid_seuno = Column(Integer, default=0, comment='Count of SEUNO failures')
    invalid_cheque = Column(Integer, default=0, comment='Count of CHEQUE failures')
    records_stored = Column(Integer, default=0, comment='Records inserted/updated in MariaDB')

    # Alert statistics
    alerts_sent_nfechave = Column(Integer, default=0, comment='NFEChave alerts sent')
    alerts_sent_duplicata = Column(Integer, default=0, comment='DUPLICATA alerts sent')
    alerts_sent_seuno = Column(Integer, default=0, comment='SEUNO alerts sent')
    alerts_sent_cheque = Column(Integer, default=0, comment='CHEQUE alerts sent')

    # Performance
    execution_time_seconds = Column(Integer, comment='How long the run took')
    run_mode = Column(
        Enum('PRODUCTION', 'DRY_RUN'),
        default='PRODUCTION',
        comment='Mode of execution'
    )
    error_message = Column(Text, comment='Error details if failed')

    # Indexes
    __table_args__ = (
        Index('idx_run_timestamp', 'run_timestamp'),
        Index('idx_target_date', 'target_date'),
        Index('idx_run_mode', 'run_mode'),
    )

    def __repr__(self):
        return (
            f"<APRProcessingLog("
            f"id={self.id}, "
            f"target_date={self.target_date}, "
            f"run_mode={self.run_mode}"
            f")>"
        )


class APRProposalProduct(MariaDBBase):
    """
    ORM model for apr_proposal_products table.

    Tracks all product types for each proposal (many-to-many relationship).
    Allows proposals to have multiple products.
    """
    __tablename__ = 'apr_proposal_products'

    # Primary key
    id = Column(Integer, primary_key=True, autoincrement=True)

    # Proposal identification
    DATA = Column(Date, nullable=False, comment='Proposal date')
    PROPOSTA = Column(Integer, nullable=False, comment='Proposal number')
    CEDENTE = Column(String(100), nullable=False, comment='Client name')

    # Product details
    PRODUTO = Column(String(100), nullable=False, comment='Product type')
    QTD_TITULOS = Column(Integer, comment='Number of titles for this product')
    VALOR_TITULOS = Column(Numeric(15, 2), comment='Total value for this product')

    # Tracking
    created_at = Column(
        TIMESTAMP,
        server_default='CURRENT_TIMESTAMP',
        comment='When first recorded'
    )
    updated_at = Column(
        TIMESTAMP,
        server_default='CURRENT_TIMESTAMP',
        server_onupdate='CURRENT_TIMESTAMP',
        comment='Last update'
    )

    # Indexes
    __table_args__ = (
        Index('idx_proposal', 'DATA', 'PROPOSTA'),
        Index('idx_produto', 'PRODUTO'),
        Index('idx_cedente', 'CEDENTE'),
        Index('unique_proposal_product', 'DATA', 'PROPOSTA', 'CEDENTE', 'PRODUTO', unique=True),
    )

    def __repr__(self):
        return (
            f"<APRProposalProduct("
            f"PROPOSTA={self.PROPOSTA}, "
            f"CEDENTE={self.CEDENTE}, "
            f"PRODUTO={self.PRODUTO}"
            f")>"
        )


class APRDailySummary(MariaDBBase):
    """
    ORM model for apr_daily_summary table.

    Daily aggregated metrics for dashboard queries.
    """
    __tablename__ = 'apr_daily_summary'

    # Primary key
    DATA = Column(Date, primary_key=True, comment='The date')

    # Proposal counts
    total_proposals = Column(Integer, default=0, comment='Total proposals for the day')
    valid_proposals = Column(Integer, default=0, comment='Valid proposals')
    invalid_proposals = Column(Integer, default=0, comment='Invalid proposals')

    # Financial totals
    total_vlr_aprovados = Column(
        Numeric(15, 2),
        default=0.00,
        comment='Total approved value'
    )
    total_valor_titulos = Column(
        Numeric(15, 2),
        default=0.00,
        comment='Total title value'
    )

    # Status breakdown
    proposals_aguardando = Column(Integer, default=0, comment='Count in Aguardando status')
    proposals_enviado = Column(Integer, default=0, comment='Count in Enviado para Assinar')
    proposals_assinado = Column(Integer, default=0, comment='Count in Assinado')
    proposals_liberado = Column(Integer, default=0, comment='Count in Liberado')
    proposals_finalizado = Column(Integer, default=0, comment='Count in Finalizado')

    # Tracking
    last_updated = Column(
        TIMESTAMP,
        server_default='CURRENT_TIMESTAMP',
        server_onupdate='CURRENT_TIMESTAMP',
        comment='Last update time'
    )

    # Indexes
    __table_args__ = (
        Index('idx_last_updated', 'last_updated'),
    )

    def __repr__(self):
        return (
            f"<APRDailySummary("
            f"DATA={self.DATA}, "
            f"total_proposals={self.total_proposals}, "
            f"valid_proposals={self.valid_proposals}"
            f")>"
        )

