# Repurchase Report - Excel File Reader

## 📋 Overview

The **Repurchase Report** program reads and processes client repurchase data from the Excel file maintained by the collections team.

**File**: `repurchase_report.py`  
**Data Source**: `/home/robot/PBI/Cobranca/Planilha Cobranca.xlsx`  
**Sheet**: `Sheet1`

## 🎯 Purpose

This program provides:
- **Excel file reading** - Loads repurchase data from the collections spreadsheet
- **Data parsing** - Extracts all relevant client repurchase information
- **Summary statistics** - Shows breakdown by manager, operator, and repurchase type
- **Search functionality** - Find specific clients by name
- **Data structure** - Provides a clean Python interface to the Excel data

## 📊 Data Structure

The program reads the following columns from the Excel file:

| Column | Description |
|--------|-------------|
| **Gerente** | Manager responsible for the client |
| **Operador** | Operator handling the account |
| **Grupo** | Client group |
| **Cedente** | Client name |
| **Particularidade** | Special notes/particularities |
| **Tipo de Recompra** | Repurchase type (Operação/Depósito) |
| **Prazo de recompra** | Repurchase deadline |
| **Prazo de recompra definido pelo Rating** | Rating-defined deadline |
| **Aumentar prazo de recompra para quantos dias?** | Deadline extension (days) |
| **Contato do Cedente** | Client contact information |
| **Restrição em mudar de operador?** | Operator change restriction |
| **OBSERVAÇÃO DO CLIENTE** | Client observations |
| **Parecer resumido do cliente** | Client summary opinion |
| **Rating 03/09/2024** | Client rating |
| **FÉRIAS COLETIVAS** | Collective vacation information |

## 🚀 Usage

### Basic Usage (Summary Report)

```bash
cd slack_integration_alerts
python3 repurchase_report.py
```

This will:
1. Read the Excel file
2. Display total records loaded
3. Show summary statistics by manager, operator, and repurchase type
4. Display sample records (first 5)

### Search for Specific Client

```bash
python3 repurchase_report.py "CLIENT_NAME"
```

Example:
```bash
python3 repurchase_report.py "ACACIA"
```

This will:
1. Load all data
2. Display summary
3. Search for clients matching "ACACIA" (case-insensitive, partial match)
4. Display detailed information for matching records

## 📈 Sample Output

```
====================================================================================================
REPURCHASE REPORT - Excel File Reader
====================================================================================================

Reading Excel file: /home/robot/PBI/Cobranca/Planilha Cobranca.xlsx
Sheet: Sheet1

✓ Workbook loaded successfully
  Total rows: 960
  Total columns: 16

✓ Data loaded successfully
  Total records: 517

====================================================================================================
REPURCHASE DATA SUMMARY
====================================================================================================

Records by Manager:
  DANIELA: 53
  NILSON: 46
  SUIENE: 46
  AMANDA: 45
  ...

Records by Repurchase Type:
  Operação: 308
  Depósito : 209

Records by Operator:
  Thais: 103
  Larissa Queiroz: 85
  Ranielly: 79
  ...
```

## 🔧 Technical Details

### Dependencies

- **openpyxl** - Excel file reading
- **Python 3.12+** - Runtime environment

### Class: `RepurchaseData`

Represents a single client's repurchase information.

**Methods:**
- `__init__(row_data)` - Initialize from Excel row
- `to_dict()` - Convert to dictionary
- `__repr__()` - String representation

### Functions

- `read_excel_file(file_path, sheet_name)` - Read Excel and return list of RepurchaseData objects
- `display_summary(data_list)` - Display summary statistics
- `display_sample_records(data_list, num_records)` - Show sample records
- `search_by_cedente(data_list, cedente_name)` - Search by client name

## 📁 File Location

```
slack_integration_alerts/
├── repurchase_report.py          # Main program
└── README_REPURCHASE.md          # This file
```

## 🔍 Use Cases

1. **Quick Data Overview** - See how many clients each manager/operator handles
2. **Client Lookup** - Find specific client repurchase rules quickly
3. **Data Export** - Use as a library to export data to other formats
4. **Integration** - Import and use in other Python scripts

## 💡 Example: Using as a Library

```python
from repurchase_report import read_excel_file, search_by_cedente

# Load all data
data = read_excel_file()

# Search for a client
matches = search_by_cedente(data, "ACACIA")

# Access client information
for client in matches:
    print(f"Client: {client.cedente}")
    print(f"Manager: {client.gerente}")
    print(f"Repurchase Type: {client.tipo_recompra}")
    print(f"Deadline: {client.prazo_recompra}")
```

## ✅ Features Implemented

- ✅ Excel file reading with openpyxl
- ✅ Data parsing and validation
- ✅ Summary statistics generation
- ✅ Client search functionality
- ✅ Clean data structure (RepurchaseData class)
- ✅ Command-line interface
- ✅ Error handling

## 🎯 Next Steps

This is the foundation for the repurchase report system. Future enhancements could include:
- Database integration
- Automated report generation
- Slack notifications
- Data validation and alerts
- Export to different formats

---

**Created**: 2026-01-19  
**Status**: ✅ Fully functional

