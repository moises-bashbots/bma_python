"""
SQLAlchemy ORM models for BMA database
"""

from datetime import datetime
from typing import Optional

from sqlalchemy import String, Integer, Boolean, DateTime, func
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    """Base class for all ORM models."""
    pass


class Sacado(Base):
    """
    ORM model for the BMA.sacado table.
    
    Represents a 'sacado' (debtor/payer) entity in the system.
    """
    __tablename__ = "sacado"
    __table_args__ = {"schema": "BMA"}

    # Primary key
    id_sacado: Mapped[int] = mapped_column(
        Integer, 
        primary_key=True, 
        autoincrement=True,
        comment="Unique identifier for the sacado"
    )

    # Required fields
    nome: Mapped[str] = mapped_column(
        String(250), 
        nullable=False,
        comment="Full name of the sacado"
    )
    
    apelido: Mapped[str] = mapped_column(
        String(100), 
        nullable=False,
        comment="Nickname or short name"
    )
    
    cnpj: Mapped[str] = mapped_column(
        String(14), 
        nullable=False, 
        unique=True,
        comment="CNPJ (14 digits) or CPF (11 digits)"
    )

    # Optional fields
    telefone: Mapped[Optional[str]] = mapped_column(
        String(100), 
        nullable=True,
        comment="Phone number"
    )
    
    email: Mapped[Optional[str]] = mapped_column(
        String(500), 
        nullable=True,
        comment="Email address(es)"
    )
    
    endereco: Mapped[Optional[str]] = mapped_column(
        String(300), 
        nullable=True,
        comment="Street address"
    )
    
    bairro: Mapped[Optional[str]] = mapped_column(
        String(300), 
        nullable=True,
        comment="Neighborhood/District"
    )
    
    cidade: Mapped[Optional[str]] = mapped_column(
        String(300), 
        nullable=True,
        comment="City"
    )
    
    uf: Mapped[Optional[str]] = mapped_column(
        String(2),
        nullable=True,
        comment="State code (2 letters)"
    )

    cep: Mapped[Optional[str]] = mapped_column(
        String(8),
        nullable=True,
        comment="CEP (postal code, 8 digits)"
    )

    # Boolean/Flag fields
    completo: Mapped[int] = mapped_column(
        Integer,
        nullable=False,
        default=0,
        comment="Flag indicating if record is complete (0=incomplete, 1=complete)"
    )

    registrado: Mapped[int] = mapped_column(
        Integer,
        nullable=False,
        default=0,
        comment="Registration status flag"
    )

    is_pf: Mapped[Optional[int]] = mapped_column(
        Integer,
        nullable=True,
        comment="Is Pessoa Física (individual) - 1=PF, 0=PJ, NULL=unknown"
    )

    is_enrich_vadu: Mapped[Optional[int]] = mapped_column(
        Integer,
        nullable=True,
        default=0,
        comment="Flag indicating if record was enriched with Vadu API data (0=no, 1=yes)"
    )

    # Timestamp
    data_atualizacao: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False,
        server_default=func.current_timestamp(),
        onupdate=func.current_timestamp(),
        comment="Last update timestamp"
    )

    def __repr__(self) -> str:
        return f"<Sacado(id={self.id_sacado}, nome='{self.nome}', cnpj='{self.cnpj}')>"

    def __str__(self) -> str:
        return f"{self.nome} ({self.cnpj})"

