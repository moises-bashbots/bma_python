# SQL Server - Cedente Table Structure

## Database Information
- **Server**: 200.187.70.21:1433
- **Database**: BMA
- **Table**: cedente
- **Total Columns**: 359

## Column Categories

### 1. Basic Information (Core Fields)
| Column | Type | Length | Nullable | Description |
|--------|------|--------|----------|-------------|
| `apelido` | varchar | 20 | NO | **Nickname/Alias** (Primary identifier) |
| `nome` | varchar | 52 | NO | **Full company name** |
| `nome_fantasia` | varchar | 52 | YES | Trade name |
| `cnpj` | varchar | 14 | NO | **Tax ID** |
| `ID` | int | - | NO | **Auto-increment ID** |

### 2. Address Information
| Column | Type | Length | Nullable |
|--------|------|--------|----------|
| `endereco` | varchar | 70 | YES |
| `complemento` | text | 2147483647 | YES |
| `bairro` | varchar | 20 | YES |
| `cidade` | varchar | 20 | YES |
| `uf` | char | 2 | YES |
| `cep` | char | 8 | YES |
| `codmunicipio` | varchar | 10 | YES |

### 3. Contact Information
| Column | Type | Length | Nullable |
|--------|------|--------|----------|
| `fone` | varchar | 30 | YES |
| `fax` | varchar | 15 | YES |
| `celular` | varchar | 15 | YES |
| `EMAILCED` | varchar | 50 | YES |
| `email1` | varchar | 50 | YES |
| `email2` | varchar | 50 | YES |
| `email3` | varchar | 50 | YES |
| `email4` | varchar | 50 | YES |
| `email5` | varchar | 50 | YES |
| `emailNFSE` | varchar | 50 | YES |
| `rsemail` | varchar | 200 | YES |

### 4. Registration & Legal
| Column | Type | Length | Nullable |
|--------|------|--------|----------|
| `incest` | varchar | 20 | YES |
| `inscricao_m` | varchar | 20 | YES |
| `regjuntacom` | varchar | 15 | YES |
| `nire` | varchar | 15 | YES |
| `dtconst` | datetime | - | YES |
| `id_cnae` | int | - | YES |

### 5. Contract Information
| Column | Type | Length | Nullable |
|--------|------|--------|----------|
| `ncontrato` | char | 10 | YES |
| `dcontrato` | datetime | - | YES |
| `ncontratomp` | varchar | 10 | YES |
| `dcontratomp` | datetime | - | YES |
| `venc_contratomp` | datetime | - | YES |
| `ncontratofidc` | varchar | 10 | YES |
| `dcontratofidc` | datetime | - | YES |
| `dataVencContratoFidc` | datetime | - | YES |
| `ncontratotrustee` | varchar | 10 | YES |
| `dcontratotrustee` | datetime | - | YES |
| `dataVencContratoTrustee` | datetime | - | YES |
| `valor_contrato` | decimal | - | YES |

### 6. Limits & Credit
| Column | Type | Length | Nullable |
|--------|------|--------|----------|
| `l_valor` | decimal | - | YES |
| `l_acum` | decimal | - | YES |
| `l_titulos` | int | - | YES |
| `l_sac` | decimal | - | YES |
| `l_nconf` | decimal | - | YES |
| `l_acumch` | decimal | - | YES |
| `l_acumdp` | decimal | - | YES |
| `l_sacch` | decimal | - | YES |
| `l_sacdp` | decimal | - | YES |
| `l_raiz_sac` | decimal | - | YES |
| `limite_mp` | decimal | - | YES |
| `limitech` | decimal | - | YES |
| `limminconf` | decimal | - | YES |
| `lcnpjNaoConf` | decimal | - | YES |
| `VENCLIMITE` | datetime | - | YES |

### 7. Rates & Fees (Taxes)
| Column | Type | Length | Nullable | Description |
|--------|------|--------|----------|-------------|
| `tdesc` | decimal | - | YES | Discount rate |
| `tdesc_fidc` | decimal | - | YES | FIDC discount rate |
| `tdescfidc` | decimal | - | YES | FIDC discount |
| `tdesccs` | decimal | - | YES | CS discount |
| `TxAcerto` | decimal | - | YES | Settlement rate |
| `txacertopos` | decimal | - | YES | Post settlement rate |
| `txserv` | decimal | - | YES | Service rate |
| `txserv_fidc` | decimal | - | YES | FIDC service rate |
| `txservCS` | decimal | - | YES | CS service rate |
| `txservCap` | decimal | - | YES | Capital service rate |
| `txmora` | decimal | - | YES | Late payment rate |
| `txmorarec` | decimal | - | YES | Receivable late rate |
| `txprorrogacao` | decimal | - | YES | Extension rate |
| `multa` | decimal | - | YES | Fine/penalty |
| `multa_redeposito` | decimal | - | YES | Redeposit fine |
| `comissao` | decimal | - | YES | Commission |
| `tac` | decimal | - | YES | TAC (Opening fee) |
| `taxamin` | decimal | - | YES | Minimum rate |

### 8. Terms & Periods
| Column | Type | Length | Nullable |
|--------|------|--------|----------|
| `prazo` | smallint | - | YES |
| `prazomin` | int | - | YES |
| `prazomax` | int | - | YES |
| `prazomin_ch` | int | - | YES |
| `prazomax_ch` | int | - | YES |
| `dmais` | smallint | - | YES |
| `DmaisRedeposito` | int | - | YES |

### 9. Status Flags
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `ativo` | bit | NO | Active status |
| `internet` | bit | NO | Internet access |
| `simples` | bit | NO | Simples Nacional |
| `baixa` | bit | YES | Closed/Inactive |
| `reapresenta` | bit | NO | Re-present |
| `expsci` | bit | YES | Export SCI |
| `expser` | bit | YES | Export SER |

### 10. Email Flags (em0-em12)
| Column | Type | Nullable |
|--------|------|----------|
| `em0` through `em12` | bit | YES |

### 11. Web Flags (web1-web43)
| Column | Type | Nullable |
|--------|------|----------|
| `web1` through `web43` | bit | YES |

### 12. Risk & Rating
| Column | Type | Nullable |
|--------|------|----------|
| `rating_inicial_equifax` | int | YES |
| `rating_inicial_carteira` | int | YES |
| `rating_inicial_total` | int | YES |
| `rating_atual_equifax` | int | YES |
| `rating_atual_carteira` | int | YES |
| `rating_atual_total` | int | YES |
| `class_plc` | varchar(5) | YES |
| `class_ris` | varchar(5) | YES |
| `riscomax_caf_valor` | decimal | YES |
| `riscomax_caf_qtd` | int | YES |

### 13. Accounting Codes (conta1-conta7, QDR, etc.)
| Column | Type | Length | Nullable |
|--------|------|--------|----------|
| `conta1` through `conta7` | varchar | 18 | YES |
| `conta_cc` | varchar | 18 | YES |
| `numero_cc` | varchar | 10 | YES |
| `CONTA_REEMB` | varchar | 18 | YES |
| `obrig_terc` | varchar | 18 | YES |
| `QDR1_2P` through `QDR1_7P` | varchar | 18 | YES |

### 14. Tariffs (TAR1-TAR14, db1-db4)
| Column | Type | Nullable |
|--------|------|----------|
| `TAR1` through `TAR14` | decimal | YES |
| `db1` through `db4` | decimal | YES |
| `ADICIONAL2`, `adicional3` | decimal | YES |

### 15. Service Costs (SER_CUS_*)
| Column | Type | Nullable |
|--------|------|----------|
| `SER_CUS_ACHEIRECH` | decimal | YES |
| `SER_CUS_CONCENTRE` | decimal | YES |
| `SER_CUS_CONCENTRE1`, `SER_CUS_CONCENTRE2` | decimal | YES |
| `SER_CUS_CREDIT` | decimal | YES |
| `SER_CUS_RELATO` through `SER_CUS_RELATO4` | decimal | YES |
| `SER_CUS_RELATOMAIS` | decimal | YES |

## Key Fields for ORM Model

### Primary Keys
- `ID` (int, auto-increment)
- `apelido` (varchar(20), unique business key)

### Essential Fields
- `nome` - Company name
- `cnpj` - Tax ID
- `ativo` - Active status
- `ingresso` - Entry date
- `ncontrato` - Contract number
- `dcontrato` - Contract date

### Most Used Fields (Based on Sample Data)
- Contact: `EMAILCED`, `fone`, `celular`
- Limits: `l_sac`, `l_nconf`, `l_acumdp`
- Rates: `tdesc`, `txserv`, `txmora`
- Status: `ativo`, `internet`, `cedentefidc`

## Notes
- Total of **359 columns** - very wide table
- Many legacy/deprecated fields (web1-web43, em0-em12)
- Multiple contract types (FIDC, Trustee, MP)
- Extensive tariff and fee structure
- Complex accounting integration (conta1-7, QDR fields)

