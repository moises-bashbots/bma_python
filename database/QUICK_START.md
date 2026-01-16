# Database Module - Quick Start Guide

## 🚀 Quick Import

```python
# Import models and session
from database import Sacado, Cedente, CobrancaPix, create_session

# Or import from specific module
from database.bma_models import Sacado, create_session
```

## 📊 Common Queries

### Query Sacados (Debtors)

```python
from database import Sacado, create_session

with create_session() as session:
    # Get all incomplete sacados
    incomplete = session.query(Sacado).filter(
        Sacado.completo == False
    ).all()
    
    # Get by CNPJ
    sacado = session.query(Sacado).filter(
        Sacado.cnpj == "12345678000199"
    ).first()
    
    # Count total
    total = session.query(Sacado).count()
```

### Query Cedentes (Assignors)

```python
from database import Cedente, create_session

with create_session() as session:
    # Get active cedentes
    active = session.query(Cedente).filter(
        Cedente.ativo == True
    ).all()
    
    # Get by apelido (nickname)
    cedente = session.query(Cedente).filter(
        Cedente.apelido == "EMPRESA ABC"
    ).first()
```

### Query PIX Collections

```python
from database import CobrancaPix, create_session

with create_session() as session:
    # Get unpaid collections
    unpaid = session.query(CobrancaPix).filter(
        CobrancaPix.pago == False
    ).all()
    
    # Get by txid
    cobranca = session.query(CobrancaPix).filter(
        CobrancaPix.txid == "ABC123XYZ"
    ).first()
```

### Query WhatsApp Contacts

```python
from database import ContatoWhatsapp, create_session

with create_session() as session:
    # Get all groups
    groups = session.query(ContatoWhatsapp).filter(
        ContatoWhatsapp.isGroup == 1
    ).all()
    
    # Get by phone
    contact = session.query(ContatoWhatsapp).filter(
        ContatoWhatsapp.phone == "5511999999999"
    ).first()
```

## 🔄 Insert/Update/Delete

### Insert New Record

```python
from database import Sacado, create_session
from datetime import datetime

with create_session() as session:
    novo_sacado = Sacado(
        nome="EMPRESA EXEMPLO LTDA",
        apelido="EXEMPLO",
        cnpj="12345678000199",
        telefone="11999999999",
        email="contato@exemplo.com",
        completo=False,
        registrado=False
    )
    
    session.add(novo_sacado)
    session.commit()
    
    print(f"Created: {novo_sacado.id_sacado}")
```

### Update Record

```python
from database import Sacado, create_session

with create_session() as session:
    sacado = session.query(Sacado).filter(
        Sacado.cnpj == "12345678000199"
    ).first()
    
    if sacado:
        sacado.telefone = "11988888888"
        sacado.completo = True
        session.commit()
```

### Delete Record

```python
from database import Sacado, create_session

with create_session() as session:
    sacado = session.query(Sacado).filter(
        Sacado.cnpj == "12345678000199"
    ).first()
    
    if sacado:
        session.delete(sacado)
        session.commit()
```

## 🔗 Joins & Relationships

```python
from database import CobrancaPix, Empresa, Cedente, create_session

with create_session() as session:
    # Query with relationships
    cobranca = session.query(CobrancaPix).first()
    
    # Access related objects
    print(f"Company: {cobranca.empresa.razao_social}")
    print(f"Assignor: {cobranca.cedente.apelido}")
    print(f"Status: {cobranca.status_cobranca_pix.status}")
```

## 📈 Aggregations

```python
from database import CobrancaPix, create_session
from sqlalchemy import func

with create_session() as session:
    # Count
    total = session.query(CobrancaPix).count()
    
    # Sum
    total_value = session.query(
        func.sum(CobrancaPix.valor)
    ).scalar()
    
    # Group by
    by_status = session.query(
        CobrancaPix.status_cobranca_pix_id_status_cobranca_pix,
        func.count(CobrancaPix.id_cobranca_pix)
    ).group_by(
        CobrancaPix.status_cobranca_pix_id_status_cobranca_pix
    ).all()
```

## 🗄️ Direct SQL (when needed)

```python
from database import get_mariadb_engine

engine = get_mariadb_engine()

with engine.connect() as conn:
    result = conn.execute("SELECT COUNT(*) FROM sacado")
    count = result.scalar()
    print(f"Total sacados: {count}")
```

## 🔧 Configuration

Database credentials are in `database/databases_config.json`:

```json
{
  "databases": {
    "mariadb": {
      "server": "localhost",
      "port": 3306,
      "user": "robot",
      "password": "r0b0t",
      "scheme": "BMA"
    }
  }
}
```

## 📚 More Information

- **[README.md](README.md)** - Full documentation
- **[BMA_MODELS_GUIDE.md](BMA_MODELS_GUIDE.md)** - Complete model reference
- **[MSSQL_CEDENTE_COLUMNS.md](MSSQL_CEDENTE_COLUMNS.md)** - SQL Server schema

