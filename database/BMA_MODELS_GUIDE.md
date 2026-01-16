# BMA ORM Models Guide

SQLAlchemy ORM models for the BMA MariaDB database schema.

## Overview

The `bma_models.py` file contains complete ORM models for all 18 tables in the BMA database:

### Core Tables

1. **Banco** - Bank information (COMPE codes, ISPB)
2. **Empresa** - Companies/Enterprises (BMA FIDC entities)
3. **Participante** - Participants (CNPJ registry)
4. **Cedente** - Assignors/Creditors
5. **Sacado** - Debtors/Payers

### PIX & Collections

6. **CobrancaPix** - PIX collections/charges
7. **Pix** - PIX payments
8. **StatusCobrancaPix** - PIX collection status
9. **TipoInstrucao** - Instruction types
10. **EmailCobrancaInstrucao** - Email collection instructions

### Banking & Operations

11. **DadosBancariosEmpresa** - Company banking data
12. **OperacaoRecompra** - Repurchase operations
13. **TituloRecompra** - Repurchase titles/invoices

### Issues & Requests

14. **Critica** - Issues/Criticisms
15. **TipoCritica** - Issue types
16. **Solicitacao** - Requests/Solicitations

### Other

17. **ContatoWhatsapp** - WhatsApp contacts
18. **EventoTransporte** - Transport events

## Installation

The models are already available in the centralized virtual environment:

```bash
cd /home/robot/Dev/bma
source .venv/bin/activate
```

## Usage Examples

### Basic Query

```python
from bma_models import Sacado, create_session

with create_session() as session:
    # Get all debtors
    sacados = session.query(Sacado).limit(10).all()
    for sacado in sacados:
        print(f"{sacado.cnpj} - {sacado.nome}")
```

### Filtering

```python
from bma_models import Sacado, create_session

with create_session() as session:
    # Get incomplete sacados
    incomplete = session.query(Sacado).filter(
        Sacado.completo == False
    ).all()
    
    # Get sacados enriched by Vadu
    enriched = session.query(Sacado).filter(
        Sacado.is_enrich_vadu == True
    ).all()
```

### Relationships

```python
from bma_models import CobrancaPix, Empresa, Cedente, create_session

with create_session() as session:
    # Get PIX collection with related data
    cobranca = session.query(CobrancaPix).first()
    
    # Access related empresa
    print(f"Company: {cobranca.empresa.razao_social}")
    
    # Access related cedente
    print(f"Assignor: {cobranca.cedente.apelido}")
    
    # Access status
    print(f"Status: {cobranca.status_cobranca_pix.status}")
```

### Aggregations

```python
from bma_models import CobrancaPix, create_session
from sqlalchemy import func

with create_session() as session:
    # Count paid vs unpaid
    total = session.query(CobrancaPix).count()
    paid = session.query(CobrancaPix).filter(CobrancaPix.pago == True).count()
    
    print(f"Total: {total}, Paid: {paid}, Pending: {total - paid}")
    
    # Sum of values
    total_value = session.query(func.sum(CobrancaPix.valor)).scalar()
    print(f"Total value: R$ {total_value:,.2f}")
```

### Joins

```python
from bma_models import Cedente, Empresa, Participante, create_session

with create_session() as session:
    # Join cedente with empresa and participante
    results = session.query(
        Cedente, Empresa, Participante
    ).join(
        Empresa, Cedente.empresa_id_empresa == Empresa.id_empresa
    ).join(
        Participante, Cedente.participante_id_participante == Participante.id_participante
    ).limit(10).all()
    
    for cedente, empresa, participante in results:
        print(f"{cedente.apelido} - {empresa.razao_social} - {participante.cadastro}")
```

### Insert New Record

```python
from bma_models import Sacado, create_session
from datetime import datetime

with create_session() as session:
    # Create new sacado
    novo_sacado = Sacado(
        nome="EMPRESA EXEMPLO LTDA",
        apelido="EXEMPLO",
        cnpj="12345678000199",
        telefone="11999999999",
        email="contato@exemplo.com",
        is_enrich_vadu=False,
        completo=False,
        registrado=False
    )
    
    session.add(novo_sacado)
    session.commit()
    
    print(f"Created sacado with ID: {novo_sacado.id_sacado}")
```

### Update Record

```python
from bma_models import Sacado, create_session

with create_session() as session:
    # Find and update
    sacado = session.query(Sacado).filter(
        Sacado.cnpj == "12345678000199"
    ).first()
    
    if sacado:
        sacado.telefone = "11988888888"
        sacado.completo = True
        session.commit()
```

## Model Relationships

### Key Relationships

- **Empresa** → Cedente (one-to-many)
- **Empresa** → CobrancaPix (one-to-many)
- **Cedente** → CobrancaPix (one-to-many)
- **CobrancaPix** → Pix (one-to-many)
- **CobrancaPix** → EmailCobrancaInstrucao (one-to-many)
- **OperacaoRecompra** → TituloRecompra (one-to-many)

## Helper Functions

### get_mariadb_engine()
Creates SQLAlchemy engine using configuration from `databases_config.json`

### create_session()
Creates a new database session for queries

## Running the Example

```bash
cd /home/robot/Dev/bma
source .venv/bin/activate
python3 bma_models.py
```

This will display:
- List of banks
- Sample debtors
- PIX collection statistics

## Notes

- All models use the centralized `.venv` virtual environment
- Timestamps are automatically updated on record changes
- Foreign key relationships are properly defined
- Indexes are created for frequently queried fields

