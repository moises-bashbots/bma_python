#!/usr/bin/env python3
"""
Create dias_uteis (business days) table in MariaDB.

This script:
1. Reads feriados_nacionais.xls file
2. Creates a dias_uteis table in MariaDB
3. Populates the table with dates from 2001 to 2050
4. Marks holidays and weekends as non-business days
"""

import xlrd
import pymysql
import json
from datetime import datetime, date, timedelta
from pathlib import Path


def load_database_config():
    """Load MariaDB configuration from databases_config.json."""
    config_path = Path(__file__).parent / 'databases_config.json'
    
    with open(config_path, 'r') as f:
        config = json.load(f)
    
    return config['databases']['mariadb']


def read_holidays_from_excel(file_path: str) -> dict:
    """
    Read holidays from Excel file.
    
    Args:
        file_path: Path to feriados_nacionais.xls
    
    Returns:
        Dictionary with date as key and holiday info as value
        Format: {date(2001, 1, 1): {'dia_semana': 'segunda-feira', 'feriado': 'Confraternização Universal'}}
    """
    wb = xlrd.open_workbook(file_path)
    sheet = wb.sheet_by_index(0)
    
    holidays = {}
    
    # Skip header row (row 0)
    for row_idx in range(1, sheet.nrows):
        # Column 0: Date
        date_cell = sheet.cell(row_idx, 0)
        
        if date_cell.ctype == xlrd.XL_CELL_DATE:
            date_tuple = xlrd.xldate_as_tuple(date_cell.value, wb.datemode)
            holiday_date = date(*date_tuple[:3])
            
            # Column 1: Day of week
            dia_semana = sheet.cell(row_idx, 1).value
            
            # Column 2: Holiday name
            feriado = sheet.cell(row_idx, 2).value
            
            holidays[holiday_date] = {
                'dia_semana': dia_semana,
                'feriado': feriado
            }
    
    return holidays


def create_dias_uteis_table(connection):
    """
    Create dias_uteis table in MariaDB.
    
    Table structure:
    - data: DATE (primary key)
    - dia_semana: VARCHAR(20) - Day of week in Portuguese
    - eh_feriado: BOOLEAN - Is it a holiday?
    - nome_feriado: VARCHAR(100) - Holiday name (if applicable)
    - eh_fim_de_semana: BOOLEAN - Is it a weekend?
    - eh_dia_util: BOOLEAN - Is it a business day? (not holiday and not weekend)
    """
    cursor = connection.cursor()
    
    # Drop table if exists
    cursor.execute("DROP TABLE IF EXISTS dias_uteis")
    
    # Create table
    create_table_sql = """
    CREATE TABLE dias_uteis (
        data DATE PRIMARY KEY,
        dia_semana VARCHAR(20) NOT NULL,
        eh_feriado BOOLEAN DEFAULT FALSE,
        nome_feriado VARCHAR(100),
        eh_fim_de_semana BOOLEAN DEFAULT FALSE,
        eh_dia_util BOOLEAN DEFAULT TRUE,
        INDEX idx_eh_dia_util (eh_dia_util),
        INDEX idx_data (data)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    """
    
    cursor.execute(create_table_sql)
    connection.commit()
    
    print("✓ Table 'dias_uteis' created successfully")


def get_dia_semana_pt(date_obj: date) -> str:
    """
    Get day of week in Portuguese.
    
    Args:
        date_obj: Date object
    
    Returns:
        Day of week in Portuguese (e.g., 'segunda-feira')
    """
    dias = {
        0: 'segunda-feira',
        1: 'terça-feira',
        2: 'quarta-feira',
        3: 'quinta-feira',
        4: 'sexta-feira',
        5: 'sábado',
        6: 'domingo'
    }
    return dias[date_obj.weekday()]


def populate_dias_uteis_table(connection, holidays: dict, start_year: int = 2001, end_year: int = 2050):
    """
    Populate dias_uteis table with dates from start_year to end_year.
    
    Args:
        connection: Database connection
        holidays: Dictionary of holidays from Excel
        start_year: Start year (default: 2001)
        end_year: End year (default: 2050)
    """
    cursor = connection.cursor()
    
    start_date = date(start_year, 1, 1)
    end_date = date(end_year, 12, 31)
    
    current_date = start_date
    records_inserted = 0
    
    print(f"Populating table from {start_date} to {end_date}...")
    
    while current_date <= end_date:
        dia_semana = get_dia_semana_pt(current_date)
        eh_fim_de_semana = current_date.weekday() >= 5  # Saturday (5) or Sunday (6)
        
        # Check if it's a holiday
        if current_date in holidays:
            eh_feriado = True
            nome_feriado = holidays[current_date]['feriado']
        else:
            eh_feriado = False
            nome_feriado = None
        
        # Business day: not weekend and not holiday
        eh_dia_util = not (eh_fim_de_semana or eh_feriado)
        
        # Insert record
        insert_sql = """
        INSERT INTO dias_uteis (data, dia_semana, eh_feriado, nome_feriado, eh_fim_de_semana, eh_dia_util)
        VALUES (%s, %s, %s, %s, %s, %s)
        """

        cursor.execute(insert_sql, (
            current_date,
            dia_semana,
            eh_feriado,
            nome_feriado,
            eh_fim_de_semana,
            eh_dia_util
        ))

        records_inserted += 1

        # Commit every 1000 records
        if records_inserted % 1000 == 0:
            connection.commit()
            print(f"  Inserted {records_inserted} records...")

        # Move to next day
        current_date += timedelta(days=1)

    # Final commit
    connection.commit()

    print(f"✓ Inserted {records_inserted} records")


def print_statistics(connection):
    """Print statistics about the dias_uteis table."""
    cursor = connection.cursor(pymysql.cursors.DictCursor)

    print()
    print("=" * 100)
    print("DIAS_UTEIS TABLE STATISTICS")
    print("=" * 100)

    # Total records
    cursor.execute("SELECT COUNT(*) as total FROM dias_uteis")
    total = cursor.fetchone()['total']
    print(f"Total records: {total:,}")

    # Business days
    cursor.execute("SELECT COUNT(*) as total FROM dias_uteis WHERE eh_dia_util = TRUE")
    business_days = cursor.fetchone()['total']
    print(f"Business days: {business_days:,} ({business_days/total*100:.1f}%)")

    # Holidays
    cursor.execute("SELECT COUNT(*) as total FROM dias_uteis WHERE eh_feriado = TRUE")
    holidays = cursor.fetchone()['total']
    print(f"Holidays: {holidays:,} ({holidays/total*100:.1f}%)")

    # Weekends
    cursor.execute("SELECT COUNT(*) as total FROM dias_uteis WHERE eh_fim_de_semana = TRUE")
    weekends = cursor.fetchone()['total']
    print(f"Weekends: {weekends:,} ({weekends/total*100:.1f}%)")

    print()

    # Sample holidays
    print("Sample holidays (first 10):")
    print("-" * 100)
    cursor.execute("""
        SELECT data, dia_semana, nome_feriado
        FROM dias_uteis
        WHERE eh_feriado = TRUE
        ORDER BY data
        LIMIT 10
    """)

    for row in cursor.fetchall():
        print(f"  {row['data']} ({row['dia_semana']}) - {row['nome_feriado']}")

    print()

    # Sample recent dates
    print("Sample recent dates (last 10 days):")
    print("-" * 100)
    cursor.execute("""
        SELECT data, dia_semana, eh_feriado, nome_feriado, eh_fim_de_semana, eh_dia_util
        FROM dias_uteis
        ORDER BY data DESC
        LIMIT 10
    """)

    for row in cursor.fetchall():
        status = []
        if row['eh_dia_util']:
            status.append('DIA ÚTIL')
        if row['eh_feriado']:
            status.append(f"FERIADO: {row['nome_feriado']}")
        if row['eh_fim_de_semana']:
            status.append('FIM DE SEMANA')

        status_str = ' | '.join(status) if status else 'N/A'
        print(f"  {row['data']} ({row['dia_semana']}) - {status_str}")

    print("=" * 100)


def main():
    """Main function."""
    print("=" * 100)
    print("CREATE DIAS_UTEIS TABLE - Business Days Calendar")
    print("=" * 100)
    print()

    # Read holidays from Excel
    excel_file = Path(__file__).parent / 'feriados_nacionais.xls'

    print(f"Reading holidays from: {excel_file}")
    holidays = read_holidays_from_excel(str(excel_file))
    print(f"✓ Loaded {len(holidays)} holidays from Excel")
    print()

    # Connect to MariaDB
    print("Connecting to MariaDB...")
    db_config = load_database_config()

    connection = pymysql.connect(
        host=db_config['server'],
        port=db_config['port'],
        user=db_config['user'],
        password=db_config['password'],
        database=db_config['scheme'],
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )

    print(f"✓ Connected to MariaDB: {db_config['server']}:{db_config['port']}/{db_config['scheme']}")
    print()

    try:
        # Create table
        create_dias_uteis_table(connection)
        print()

        # Populate table
        populate_dias_uteis_table(connection, holidays)
        print()

        # Print statistics
        print_statistics(connection)

    finally:
        connection.close()
        print()
        print("✓ Database connection closed")


if __name__ == '__main__':
    main()

