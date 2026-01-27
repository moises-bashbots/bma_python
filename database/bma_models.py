"""
SQLAlchemy ORM Models for BMA MariaDB Schema
Database: BMA (MariaDB)
Generated: 2026-01-16
"""

from datetime import datetime, date
from decimal import Decimal
from typing import Optional, List
from sqlalchemy import (
    Column, Integer, String, Date, DateTime,
    Numeric, Boolean, Text, BigInteger, ForeignKey, Index, TIMESTAMP, Enum, JSON
)
from sqlalchemy.orm import declarative_base, relationship
from sqlalchemy.sql import func

Base = declarative_base()


class Banco(Base):
    """Bank information table"""
    __tablename__ = 'banco'
    
    id_banco = Column(Integer, primary_key=True, autoincrement=True)
    nome_banco = Column(String(400), nullable=False)
    codigo_compe = Column(String(3), nullable=False)
    codigo_ispb = Column(String(100), nullable=False)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())
    
    # Relationships
    dados_bancarios = relationship("DadosBancariosEmpresa", back_populates="banco")


class Empresa(Base):
    """Company/Enterprise table"""
    __tablename__ = 'empresa'
    
    id_empresa = Column(Integer, primary_key=True, autoincrement=True)
    apelido = Column(String(250), nullable=False)
    cnpj = Column(String(14), nullable=False)
    razao_social = Column(String(250), nullable=False)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())
    
    # Relationships
    cedentes = relationship("Cedente", back_populates="empresa")
    cobrancas_pix = relationship("CobrancaPix", back_populates="empresa")
    criticas = relationship("Critica", back_populates="empresa")
    dados_bancarios = relationship("DadosBancariosEmpresa", back_populates="empresa")


class Participante(Base):
    """Participant table (CNPJ registry)"""
    __tablename__ = 'participante'
    
    id_participante = Column(Integer, primary_key=True, autoincrement=True)
    razao_social = Column(String(350), nullable=False)
    cadastro = Column(String(14), unique=True, nullable=False)
    raiz_cadastro = Column(String(9), nullable=False)
    complemento_cadastro = Column(String(2), nullable=False)
    r1 = Column(Integer, nullable=False, index=True)
    r2 = Column(Integer, nullable=False, index=True)
    r3 = Column(Integer, nullable=False, index=True)
    r4 = Column(Integer, nullable=False, index=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())
    
    # Relationships
    cedentes = relationship("Cedente", back_populates="participante")


class Cedente(Base):
    """Assignor/Creditor table"""
    __tablename__ = 'cedente'
    
    id_cedente = Column(Integer, primary_key=True, autoincrement=True)
    empresa_id_empresa = Column(Integer, ForeignKey('empresa.id_empresa'), nullable=False, index=True)
    participante_id_participante = Column(Integer, ForeignKey('participante.id_participante'), nullable=False, index=True)
    apelido = Column(String(150), nullable=False, index=True)
    email = Column(String(250), nullable=False)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())
    
    # Relationships
    empresa = relationship("Empresa", back_populates="cedentes")
    participante = relationship("Participante", back_populates="cedentes")
    cobrancas_pix = relationship("CobrancaPix", back_populates="cedente")
    criticas = relationship("Critica", back_populates="cedente")


class Sacado(Base):
    """Debtor/Payer table"""
    __tablename__ = 'sacado'
    
    id_sacado = Column(Integer, primary_key=True, autoincrement=True)
    nome = Column(String(250), nullable=False)
    apelido = Column(String(100), nullable=False)
    cnpj = Column(String(14), unique=True, nullable=False)
    telefone = Column(String(100), nullable=True)
    email = Column(String(500), nullable=True)
    is_enrich_vadu = Column(Boolean, nullable=False, default=0)
    completo = Column(Boolean, nullable=False, default=0)
    registrado = Column(Boolean, nullable=False, default=0)
    endereco = Column(String(300), nullable=True)
    bairro = Column(String(300), nullable=True)
    cidade = Column(String(300), nullable=True)
    uf = Column(String(2), nullable=True)
    cep = Column(String(9), nullable=True)
    is_pf = Column(Boolean, nullable=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())


class TipoInstrucao(Base):
    """Instruction type table"""
    __tablename__ = 'tipo_instrucao'
    
    id_tipo_instrucao = Column(Integer, primary_key=True, autoincrement=True)
    tipo_instrucao = Column(String(100), nullable=False)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())
    
    # Relationships
    cobrancas_pix = relationship("CobrancaPix", back_populates="tipo_instrucao")


class StatusCobrancaPix(Base):
    """PIX collection status table"""
    __tablename__ = 'status_cobranca_pix'
    
    id_status_cobranca_pix = Column(Integer, primary_key=True, autoincrement=True)
    status = Column(String(200), nullable=False)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())
    
    # Relationships
    cobrancas_pix = relationship("CobrancaPix", back_populates="status_cobranca_pix")


class CobrancaPix(Base):
    """PIX collection/charge table"""
    __tablename__ = 'cobranca_pix'
    
    id_cobranca_pix = Column(Integer, primary_key=True, autoincrement=True)
    empresa_id_empresa = Column(Integer, ForeignKey('empresa.id_empresa'), nullable=False, index=True)
    cedente_id_cedente = Column(Integer, ForeignKey('cedente.id_cedente'), nullable=False, index=True)
    tipo_instrucao_id_tipo_instrucao = Column(Integer, ForeignKey('tipo_instrucao.id_tipo_instrucao'), nullable=False, index=True)
    codigo_unico = Column(String(100), unique=True, nullable=False)
    data_pix = Column(Date, nullable=False)
    txid = Column(String(35), nullable=False)
    status_cobranca_pix_id_status_cobranca_pix = Column(Integer, ForeignKey('status_cobranca_pix.id_status_cobranca_pix'), nullable=False, index=True)
    location = Column(String(200), nullable=False)
    revisao = Column(Integer, nullable=False)
    criacao = Column(TIMESTAMP, nullable=False)
    valor = Column(Numeric(12, 2), nullable=False)
    chave = Column(String(200), nullable=False)
    pix_copia_e_cola = Column(String(600), nullable=False)
    pago = Column(Boolean, nullable=False, default=0)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())

    # Relationships
    empresa = relationship("Empresa", back_populates="cobrancas_pix")
    cedente = relationship("Cedente", back_populates="cobrancas_pix")
    tipo_instrucao = relationship("TipoInstrucao", back_populates="cobrancas_pix")
    status_cobranca_pix = relationship("StatusCobrancaPix", back_populates="cobrancas_pix")
    emails = relationship("EmailCobrancaInstrucao", back_populates="cobranca_pix")
    pagamentos_pix = relationship("Pix", back_populates="cobranca_pix")


class Pix(Base):
    """PIX payment table"""
    __tablename__ = 'pix'

    id_pix = Column(Integer, primary_key=True, autoincrement=True)
    cobranca_pix_id_cobranca_pix = Column(Integer, ForeignKey('cobranca_pix.id_cobranca_pix'), nullable=False, index=True)
    horario = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())
    valor = Column(Numeric(12, 2), nullable=False)
    txid = Column(String(100), nullable=True, index=True)
    end_to_end_id = Column(String(100), nullable=False, index=True)
    registrado = Column(Boolean, nullable=False, default=0)
    info_pagador = Column(String(300), nullable=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())

    # Relationships
    cobranca_pix = relationship("CobrancaPix", back_populates="pagamentos_pix")


class EmailCobrancaInstrucao(Base):
    """Email collection instruction table"""
    __tablename__ = 'email_cobranca_instrucao'

    id_email_cobranca_instrucao = Column(Integer, primary_key=True, autoincrement=True)
    cobranca_pix_id_cobranca_pix = Column(Integer, ForeignKey('cobranca_pix.id_cobranca_pix'), nullable=False, index=True)
    enviado = Column(Boolean, nullable=False, default=0)
    hora_envio = Column(DateTime, nullable=True)
    de = Column(String(300), nullable=False)
    para = Column(String(300), nullable=False)
    assunto = Column(String(500), nullable=False)
    corpo = Column(Text, nullable=False)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())

    # Relationships
    cobranca_pix = relationship("CobrancaPix", back_populates="emails")


class DadosBancariosEmpresa(Base):
    """Company banking data table"""
    __tablename__ = 'dados_bancarios_empresa'

    id_dados_bancarios_empresa = Column(Integer, primary_key=True, autoincrement=True)
    empresa_id_empresa = Column(Integer, ForeignKey('empresa.id_empresa'), nullable=False, index=True)
    banco_id_banco = Column(Integer, ForeignKey('banco.id_banco'), nullable=False, index=True)
    apelido = Column(String(250), nullable=False)
    agencia = Column(String(5), nullable=False)
    digito_agencia = Column(String(1), nullable=False)
    conta = Column(String(12), nullable=False)
    digito_conta = Column(String(1), nullable=False)
    codigo_pre_impresso = Column(Integer, nullable=False)
    codigo_convencional = Column(Integer, nullable=False)
    chave_pix = Column(String(300), nullable=True)
    client_id = Column(String(200), nullable=False)
    client_secret = Column(String(200), nullable=False)
    path_certificate = Column(String(500), nullable=False)
    password_certificate = Column(String(200), nullable=False)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())

    # Relationships
    empresa = relationship("Empresa", back_populates="dados_bancarios")
    banco = relationship("Banco", back_populates="dados_bancarios")


class TipoCritica(Base):
    """Criticism/Issue type table"""
    __tablename__ = 'tipo_critica'

    id_tipo_critica = Column(Integer, primary_key=True, autoincrement=True)
    apelido = Column(String(200), nullable=False, index=True)
    nome_critica = Column(String(250), nullable=False, index=True)
    descricao_critica = Column(String(450), nullable=False, index=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())

    # Relationships
    criticas = relationship("Critica", back_populates="tipo_critica")


class Critica(Base):
    """Criticism/Issue table"""
    __tablename__ = 'critica'

    id_critica = Column(Integer, primary_key=True, autoincrement=True)
    data_critica = Column(Date, nullable=False, index=True)
    empresa_id_empresa = Column(Integer, ForeignKey('empresa.id_empresa'), nullable=False, index=True)
    cedente_id_cedente = Column(Integer, ForeignKey('cedente.id_cedente'), nullable=False, index=True)
    tipo_critica_id_tipo_critica = Column(Integer, ForeignKey('tipo_critica.id_tipo_critica'), nullable=False, index=True)
    identificacao_duplicata = Column(String(150), nullable=False)
    registrado = Column(Boolean, nullable=False, default=0)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())

    # Relationships
    empresa = relationship("Empresa", back_populates="criticas")
    cedente = relationship("Cedente", back_populates="criticas")
    tipo_critica = relationship("TipoCritica", back_populates="criticas")


class ContatoWhatsapp(Base):
    """WhatsApp contact table"""
    __tablename__ = 'contato_whatsapp'

    id_contato_whatsapp = Column(Integer, primary_key=True, autoincrement=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())
    phone = Column(String(50), unique=True, nullable=False)
    name = Column(String(255), unique=True, nullable=False)  # Added unique=True and nullable=False
    pinned = Column(String(10), nullable=True)
    messagesUnread = Column(Integer, nullable=True)
    unread = Column(Integer, nullable=True)
    lastMessageTime = Column(BigInteger, nullable=True)
    isGroupAnnouncement = Column(Boolean, nullable=True)
    archived = Column(String(10), nullable=True)
    isGroup = Column(Boolean, nullable=True)
    isMuted = Column(String(10), nullable=True)
    isMarkedSpam = Column(String(10), nullable=True)
    cedente_grupo = Column(String(255), nullable=True)


class MensagemWhatsapp(Base):
    """WhatsApp messages history table"""
    __tablename__ = 'mensagens_whatsapp'

    # Primary Key
    id_mensagem = Column(BigInteger, primary_key=True, autoincrement=True)

    # Message Identification
    message_id = Column(String(255), nullable=True, comment='Unique message ID from hash or API response')
    message_hash = Column(String(32), nullable=True, index=True, comment='MD5 hash for duplicate detection')

    # Timestamp
    data_envio = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), comment='Message send timestamp')
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())

    # Receiver Information
    telefone_destino = Column(String(50), nullable=False, index=True, comment='Recipient phone number')
    nome_contato = Column(String(255), nullable=True, comment='Contact name from contato_whatsapp table')
    is_group = Column(Boolean, default=False, comment='Whether message was sent to a group')

    # Message Content
    mensagem = Column(Text, nullable=False, comment='Full message text sent')
    tipo_mensagem = Column(
        Enum('status_update', 'alert', 'notification', 'manual', name='tipo_mensagem_enum'),
        nullable=False,
        comment='Message type/category'
    )

    # Business Context (APR_CAPA related)
    cedente = Column(String(100), nullable=True, index=True, comment='Cedente name from APR_CAPA')
    grupo = Column(String(100), nullable=True, comment='Grupo from cedente table')
    data_proposta = Column(Date, nullable=True, comment='Proposal date from APR_CAPA.DATA')
    numero_proposta = Column(Integer, nullable=True, comment='Proposal number from APR_CAPA.NUMERO')
    bordero = Column(String(100), nullable=True, comment='Borderô number if applicable')
    status_fluxo = Column(String(50), nullable=True, index=True, comment='Workflow status')

    # Financial Details
    qtd_recebiveis = Column(Integer, nullable=True, comment='Quantity of receivables')
    valor_total = Column(Numeric(15, 2), nullable=True, comment='Total value')

    # API Response
    status_envio = Column(
        Enum('success', 'failed', 'pending', name='status_envio_enum'),
        nullable=False,
        default='pending',
        index=True,
        comment='Send status'
    )
    status_code = Column(Integer, nullable=True, comment='HTTP status code from API response')
    api_response = Column(JSON, nullable=True, comment='Full API response for debugging')
    erro_mensagem = Column(Text, nullable=True, comment='Error message if send failed')

    # Configuration
    config_file = Column(String(255), nullable=True, comment='WhatsApp config file used')
    api_provider = Column(String(50), default='Z-API', comment='API provider')

    # Tracking & Audit
    usuario = Column(String(100), nullable=True, comment='User who triggered the message')
    origem = Column(String(100), default='automated', comment='Message origin')
    tentativas_envio = Column(Integer, default=1, comment='Number of send attempts')

    # Indexes
    __table_args__ = (
        Index('idx_proposta', 'data_proposta', 'numero_proposta'),
    )


class EventoTransporte(Base):
    """Transport event table"""
    __tablename__ = 'evento_transporte'

    id_evento_transporte = Column(Integer, primary_key=True, autoincrement=True)
    chave_nota = Column(String(100), nullable=False, index=True)
    data_hora = Column(DateTime, nullable=True)
    unidade = Column(String(250), nullable=True)
    titulo_evento = Column(String(100), nullable=True)
    descricao_evento = Column(String(450), nullable=True)
    evento_cadastrado = Column(Integer, nullable=False, default=0)
    ultima_tentativa = Column(Date, nullable=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp(), onupdate=func.current_timestamp())


class OperacaoRecompra(Base):
    """Repurchase operation table"""
    __tablename__ = 'operacao_recompra'

    id_operacao_recompra = Column(Integer, primary_key=True, autoincrement=True)
    data_recompra = Column(Date, nullable=False)
    empresa_id_empresa = Column(Integer, nullable=False)
    cedente_id_cedente = Column(Integer, nullable=False)
    valor = Column(Numeric(12, 2), nullable=False)
    path_file_demostrativo = Column(String(400), nullable=True)
    pago = Column(Boolean, nullable=False, default=0)
    baixado = Column(Boolean, nullable=False, default=0)
    horario_baixa = Column(DateTime, nullable=True)
    baixado_bradesco = Column(Boolean, nullable=False, default=0)
    horario_baixa_bradesco = Column(DateTime, nullable=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())

    # Relationships
    titulos = relationship("TituloRecompra", back_populates="operacao_recompra")


class TituloRecompra(Base):
    """Repurchase title/invoice table"""
    __tablename__ = 'titulo_recompra'

    id_titulo_recompra = Column(Integer, primary_key=True, autoincrement=True)
    operacao_recompra_id_operacao_recompra = Column(Integer, ForeignKey('operacao_recompra.id_operacao_recompra'), nullable=False)
    identificacao_titulo = Column(String(150), nullable=False, index=True)
    nosso_numero = Column(String(14), nullable=True)
    tipo_cobranca = Column(String(4), nullable=True)
    data_solicitacao = Column(Date, nullable=True)
    vencimento = Column(Date, nullable=False)
    valor = Column(Numeric(11, 2), nullable=False)
    valor_total = Column(Numeric(10, 2), nullable=False)
    abatimento = Column(Numeric(10, 2), nullable=False)
    valor_corrigido = Column(Numeric(10, 2), nullable=False)
    deducao = Column(Numeric(11, 2), nullable=True)
    mora = Column(Numeric(11, 2), nullable=True)
    diferenca_liquidacao = Column(Numeric(11, 2), nullable=True)
    valor_recompra = Column(Numeric(12, 2), nullable=False)
    efetuado = Column(Boolean, nullable=False, default=0)
    baixado = Column(Boolean, nullable=False, default=0)
    baixado_bradesco = Column(Integer, nullable=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())

    # Relationships
    operacao_recompra = relationship("OperacaoRecompra", back_populates="titulos")


class Solicitacao(Base):
    """Request/Solicitation table"""
    __tablename__ = 'solicitacao'

    id_solicitacao = Column(Integer, primary_key=True, autoincrement=True)
    selecionado = Column(Boolean, nullable=False, default=0)
    situacao = Column(String(100), nullable=True)
    solicitacao = Column(String(100), nullable=True)
    empresa = Column(String(100), nullable=True, index=True)
    cedente = Column(String(100), nullable=True)
    bordero = Column(String(100), nullable=True)
    duplicata = Column(String(100), nullable=True)
    cheque = Column(String(100), nullable=True)
    valor = Column(Numeric(20, 2), nullable=True)
    aprovado = Column(String(1), nullable=True)
    vencimento = Column(Date, nullable=True)
    data_hora = Column(DateTime, nullable=True)
    sacado = Column(String(100), nullable=True)
    numero = Column(Integer, nullable=True)
    novo_vencimento = Column(Date, nullable=True)
    abatimento = Column(Numeric(20, 2), nullable=True)
    banco_cobrador = Column(String(100), nullable=True)
    observacoes = Column(String(250), nullable=True)
    aprova_robo = Column(Boolean, nullable=True)
    rejeita_robo = Column(Boolean, nullable=True)
    efetua_robo = Column(Boolean, nullable=True)
    motivo_rejeicao = Column(String(500), nullable=True)
    data_atualizacao = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())


# ============================================================================
# Helper Functions
# ============================================================================

def get_mariadb_engine():
    """
    Create SQLAlchemy engine for MariaDB.
    Loads configuration from databases_config.json
    """
    import json
    from pathlib import Path
    from sqlalchemy import create_engine

    # Config file is in the same directory as this module
    config_path = Path(__file__).parent / "databases_config.json"

    if not config_path.exists():
        raise FileNotFoundError(f"databases_config.json not found at {config_path}")

    with open(config_path, 'r') as f:
        config = json.load(f)

    cfg = config['databases']['mariadb']
    connection_string = (
        f"mysql+mysqlconnector://{cfg['user']}:{cfg['password']}"
        f"@{cfg['server']}:{cfg['port']}/{cfg['scheme']}"
    )

    return create_engine(connection_string, echo=False)


def create_session():
    """Create a new database session"""
    from sqlalchemy.orm import Session
    engine = get_mariadb_engine()
    return Session(engine)


# ============================================================================
# Example Usage
# ============================================================================

if __name__ == "__main__":
    """Example usage of the ORM models"""
    from sqlalchemy.orm import Session

    # Create engine and session
    engine = get_mariadb_engine()

    with Session(engine) as session:
        # Example: Query all banks
        print("=" * 60)
        print("Banks in database:")
        print("=" * 60)
        bancos = session.query(Banco).limit(5).all()
        for banco in bancos:
            print(f"  {banco.codigo_compe} - {banco.nome_banco}")

        # Example: Query sacados (debtors)
        print("\n" + "=" * 60)
        print("Sacados (Debtors) - First 5:")
        print("=" * 60)
        sacados = session.query(Sacado).limit(5).all()
        for sacado in sacados:
            print(f"  {sacado.cnpj} - {sacado.nome}")
            print(f"    Complete: {sacado.completo}, Enriched: {sacado.is_enrich_vadu}")

        # Example: Count PIX collections
        print("\n" + "=" * 60)
        print("Statistics:")
        print("=" * 60)
        total_cobrancas = session.query(CobrancaPix).count()
        cobrancas_pagas = session.query(CobrancaPix).filter(CobrancaPix.pago == True).count()
        print(f"  Total PIX collections: {total_cobrancas}")
        print(f"  Paid PIX collections: {cobrancas_pagas}")
        print(f"  Pending: {total_cobrancas - cobrancas_pagas}")

        print("\n" + "=" * 60)
        print("ORM Models loaded successfully!")
        print("=" * 60)

