"""
SQLAlchemy ORM models for BMA MSSQL database (dbo schema)
"""

from datetime import datetime
from decimal import Decimal
from typing import Optional

from sqlalchemy import String, Integer, Boolean, DateTime, Numeric, Text, SmallInteger, CHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class MSSQLBase(DeclarativeBase):
    """Base class for MSSQL ORM models."""
    pass


class SacadoMSSQL(MSSQLBase):
    """
    ORM model for the BMA.dbo.sacado table (MSSQL).
    
    Represents a 'sacado' (debtor/payer) entity in the MSSQL system.
    """
    __tablename__ = "sacado"
    __table_args__ = {"schema": "dbo"}

    # Primary key (CNPJ is the natural key)
    cnpj: Mapped[str] = mapped_column(String(14), primary_key=True)
    
    # Required fields
    apelido: Mapped[str] = mapped_column(String(20), nullable=False)
    nome: Mapped[str] = mapped_column(String(50), nullable=False)
    endcob: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    
    # Address fields
    endereco: Mapped[Optional[str]] = mapped_column(String(70), nullable=True)
    bairro: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    cidade: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    uf: Mapped[Optional[str]] = mapped_column(CHAR(2), nullable=True)
    cep: Mapped[Optional[str]] = mapped_column(CHAR(8), nullable=True)
    
    # Contact fields
    fone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    fax: Mapped[Optional[str]] = mapped_column(String(15), nullable=True)
    celular: Mapped[Optional[str]] = mapped_column(String(15), nullable=True)
    email: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    email1: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    email2: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    email3: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    email4: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    email5: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    fonecob: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    url: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    contato: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    
    # Business fields
    incest: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    ingresso: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    grupo: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    class_plc: Mapped[Optional[str]] = mapped_column(CHAR(2), nullable=True)
    class_ris: Mapped[Optional[str]] = mapped_column(CHAR(2), nullable=True)
    ramo: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    id_cnae: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    
    # Limit fields
    l_valor: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    l_acum: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    l_titulos: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    l_nconf: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    dlimite: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    
    # Risk/CAF fields
    riscomax_caf_valor: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    riscomax_caf_qtd: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    prazo_minimo: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    prazo_maximo: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    valor_minimo: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    fator: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    dmais: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    custo_dup: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    custo_ted: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    adicional: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    modelo: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    
    # Observation fields
    obs1: Mapped[Optional[str]] = mapped_column(String(80), nullable=True)
    obs2: Mapped[Optional[str]] = mapped_column(String(80), nullable=True)
    complemento: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    
    # Flags
    saccob: Mapped[Optional[str]] = mapped_column(String(14), nullable=True)
    conferido: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    altera_imp: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    impcartasac: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    nasc: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    cknaoconfirma: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    expsci: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    expser: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    BLOQUEADO: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    ckemanalise: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    PAGA_TERCEIROS: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    PGTO_PRACA_IRREGULAR: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    
    # Portal access fields
    login: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    senha: Mapped[Optional[str]] = mapped_column(String(200), nullable=True)
    AcessaMRSacado: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    ConsultaTitulosVencidos: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    ConsultaTitulosAVencer: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    ConsultaTitulosPagos: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    ImpressaoBoletos: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    ImpressaoBoletoVencido: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    CorrecaoBoletoVencido: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)
    CodigoRecuperacao: Mapped[Optional[str]] = mapped_column(String(10), nullable=True)
    ExibirFiltroEmpresa: Mapped[Optional[bool]] = mapped_column(Boolean, nullable=True)

    def __repr__(self) -> str:
        return f"<SacadoMSSQL(cnpj='{self.cnpj}', nome='{self.nome}')>"


class TituloMSSQL(MSSQLBase):
    """
    ORM model for the BMA.dbo.titulo table (MSSQL).

    Represents a 'titulo' (financial title/document) entity in the MSSQL system.
    """
    __tablename__ = "titulo"
    __table_args__ = {"schema": "dbo"}

    # Composite primary key fields
    empresa: Mapped[str] = mapped_column(String(10), primary_key=True)
    cedente: Mapped[str] = mapped_column(String(20), primary_key=True)
    tipo_operacao: Mapped[int] = mapped_column(SmallInteger, primary_key=True)
    bordero: Mapped[int] = mapped_column(Integer, primary_key=True)
    duplicata: Mapped[str] = mapped_column(String(14), primary_key=True)

    # Identity column
    sequencial: Mapped[int] = mapped_column(Integer, autoincrement=True, nullable=False)

    # Bank fields
    banco: Mapped[str] = mapped_column(CHAR(3), nullable=False)
    agencia: Mapped[str] = mapped_column(CHAR(5), nullable=False)
    conta: Mapped[str] = mapped_column(CHAR(10), nullable=False)
    cheque: Mapped[str] = mapped_column(CHAR(6), nullable=False)
    bbanco: Mapped[str] = mapped_column(String(15), nullable=False)
    bancpago: Mapped[Optional[str]] = mapped_column(String(15), nullable=True)
    pbcob: Mapped[Optional[str]] = mapped_column(String(3), nullable=True)
    pacob: Mapped[Optional[str]] = mapped_column(String(5), nullable=True)

    # Operation fields
    operacao_atual: Mapped[Optional[int]] = mapped_column(SmallInteger, nullable=True)
    tipo: Mapped[Optional[str]] = mapped_column(CHAR(1), nullable=True)
    tcobran: Mapped[int] = mapped_column(Integer, nullable=False)
    tliquid: Mapped[Optional[int]] = mapped_column(SmallInteger, nullable=True)
    status: Mapped[Optional[int]] = mapped_column(SmallInteger, nullable=True)
    confirma: Mapped[Optional[int]] = mapped_column(SmallInteger, nullable=True)

    # Value fields
    valor: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    desconto: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    abatimento: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    pgconta: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    antparcial: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    tarifaliq: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    difliq: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    mora: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    mora2: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    total: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    desagio: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    desagiado: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    vfatura: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    RecompraParcial: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)
    Desconto_liquidacao: Mapped[Optional[Decimal]] = mapped_column(Numeric, nullable=True)

    # Date fields
    dcadastro: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    emissao: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    dentrada: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    quitacao: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    data_rec: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    exec_quitacao: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    data_recompra: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    ddesc: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    # Original dates
    venc0: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    vencutil0: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    dcobrada0: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    dliberada0: Mapped[datetime] = mapped_column(DateTime, nullable=False)

    # Current dates
    venc: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    vencutil: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    dcobrada: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    dliberada: Mapped[datetime] = mapped_column(DateTime, nullable=False)

    # Days fields
    dmais: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    dmaislib: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    dd: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    dd0: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    comp: Mapped[Optional[int]] = mapped_column(SmallInteger, nullable=True)

    # Reference fields
    cnpj: Mapped[Optional[str]] = mapped_column(String(14), nullable=True)
    chave: Mapped[Optional[str]] = mapped_column(String(25), nullable=True)
    chave_origem: Mapped[Optional[str]] = mapped_column(String(25), nullable=True)
    seuno: Mapped[Optional[str]] = mapped_column(String(12), nullable=True)
    SEUNOSOFISA: Mapped[Optional[str]] = mapped_column(String(12), nullable=True)
    critica: Mapped[Optional[str]] = mapped_column(String(40), nullable=True)
    praca: Mapped[Optional[str]] = mapped_column(CHAR(2), nullable=True)
    class_ris: Mapped[Optional[str]] = mapped_column(CHAR(2), nullable=True)
    origem: Mapped[Optional[str]] = mapped_column(CHAR(1), nullable=True)
    recbord: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    nseq_recompra: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    cedente_orig: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)

    # Flags
    x: Mapped[Optional[str]] = mapped_column(CHAR(1), nullable=True)
    x_transf: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    invalida_cnab: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    gravado: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    LiqRetorno: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    tipifica: Mapped[Optional[str]] = mapped_column(CHAR(1), nullable=True)
    dv1: Mapped[Optional[str]] = mapped_column(CHAR(1), nullable=True)
    dv2: Mapped[Optional[str]] = mapped_column(CHAR(1), nullable=True)
    dv3: Mapped[Optional[str]] = mapped_column(CHAR(1), nullable=True)

    # Avalista/Guarantor
    CNPJ_AVAL: Mapped[Optional[str]] = mapped_column(String(14), nullable=True)

    # Invoice/NF-e fields
    FATURA: Mapped[Optional[str]] = mapped_column(String(14), nullable=True)
    NFEChave: Mapped[Optional[str]] = mapped_column(String(44), nullable=True)
    STATUS_NF: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    NFESERIE: Mapped[Optional[str]] = mapped_column(String(3), nullable=True)
    RaizCnpj: Mapped[Optional[str]] = mapped_column(String(14), nullable=True)
    NS_CodigoVerificacao: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)

    # Product/Approval
    id_produto: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    id_apr_titulos: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)

    def __repr__(self) -> str:
        return f"<TituloMSSQL(empresa='{self.empresa}', cedente='{self.cedente}', duplicata='{self.duplicata}')>"

