#!/usr/bin/env python3
"""
SQLAlchemy ORM models for MariaDB tables:
- apr_valid_records
"""

from datetime import datetime, date
from decimal import Decimal
from typing import Optional
from sqlalchemy import (
    Column, Integer, String, Date, DateTime, Numeric, Boolean,
    SmallInteger, TIMESTAMP, Index
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

