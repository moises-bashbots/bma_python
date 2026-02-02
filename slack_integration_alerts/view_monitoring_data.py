#!/usr/bin/env python3
"""
Quick Monitoring Data Viewer
Displays monitoring data in the console without needing Grafana.
"""

import sys
import json
from pathlib import Path
from datetime import datetime, timedelta
import pymysql
from tabulate import tabulate


def load_config():
    """Load database configuration from databases_config.json."""
    config_paths = [
        Path.cwd() / "databases_config.json",
        Path(__file__).parent / "databases_config.json",
        Path(__file__).parent.parent / "databases_config.json",
    ]

    for config_path in config_paths:
        if config_path.exists():
            with open(config_path, 'r') as f:
                return json.load(f)

    raise FileNotFoundError(
        f"Configuration file not found in: {[str(p) for p in config_paths]}"
    )


def create_connection(config):
    """Create MariaDB connection."""
    db_config = config['databases']['mariadb']
    
    return pymysql.connect(
        host=db_config['server'],
        port=db_config['port'],
        user=db_config['user'],
        password=db_config['password'],
        database=db_config['scheme'],
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )


def print_section(title):
    """Print section header."""
    print("\n" + "=" * 100)
    print(f"  {title}")
    print("=" * 100)


def view_daily_summary(conn):
    """View daily summary data."""
    print_section("📊 DAILY SUMMARY (Last 7 Days)")
    
    with conn.cursor() as cursor:
        cursor.execute("""
            SELECT 
                DATA as 'Date',
                total_proposals as 'Total',
                valid_proposals as 'Valid',
                invalid_proposals as 'Invalid',
                CONCAT('R$ ', FORMAT(total_vlr_aprovados, 2)) as 'Total Value',
                proposals_aguardando as 'Aguardando',
                proposals_enviado as 'Enviado',
                proposals_assinado as 'Assinado',
                proposals_liberado as 'Liberado',
                proposals_finalizado as 'Finalizado'
            FROM apr_daily_summary
            WHERE DATA >= CURDATE() - INTERVAL 7 DAY
            ORDER BY DATA DESC
        """)
        
        results = cursor.fetchall()
        
        if results:
            print(tabulate(results, headers="keys", tablefmt="grid"))
        else:
            print("  No data available")


def view_invalid_records(conn):
    """View invalid records summary."""
    print_section("⚠️  INVALID RECORDS BY TYPE")
    
    with conn.cursor() as cursor:
        cursor.execute("""
            SELECT 
                VALIDATION_TYPE as 'Type',
                COUNT(*) as 'Total',
                SUM(CASE WHEN is_resolved = 0 THEN 1 ELSE 0 END) as 'Active',
                SUM(CASE WHEN is_resolved = 1 THEN 1 ELSE 0 END) as 'Resolved',
                CONCAT(ROUND(SUM(CASE WHEN is_resolved = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1), '%') as 'Resolution Rate'
            FROM apr_invalid_records
            WHERE detected_at >= NOW() - INTERVAL 30 DAY
            GROUP BY VALIDATION_TYPE
            ORDER BY Total DESC
        """)
        
        results = cursor.fetchall()
        
        if results:
            print(tabulate(results, headers="keys", tablefmt="grid"))
        else:
            print("  No invalid records in last 30 days")


def view_product_distribution(conn):
    """View product distribution."""
    print_section("📦 PRODUCT DISTRIBUTION (Last 7 Days)")
    
    with conn.cursor() as cursor:
        cursor.execute("""
            SELECT 
                PRODUTO as 'Product',
                COUNT(DISTINCT CONCAT(DATA, '-', PROPOSTA)) as 'Proposals',
                SUM(QTD_TITULOS) as 'Titles',
                CONCAT('R$ ', FORMAT(SUM(VALOR_TITULOS), 2)) as 'Total Value'
            FROM apr_proposal_products
            WHERE DATA >= CURDATE() - INTERVAL 7 DAY
            GROUP BY PRODUTO
            ORDER BY Proposals DESC
            LIMIT 15
        """)
        
        results = cursor.fetchall()
        
        if results:
            print(tabulate(results, headers="keys", tablefmt="grid"))
        else:
            print("  No product data available")


def view_status_changes(conn):
    """View recent status changes."""
    print_section("📈 RECENT STATUS CHANGES (Last 20)")
    
    with conn.cursor() as cursor:
        cursor.execute("""
            SELECT 
                DATE_FORMAT(changed_at, '%Y-%m-%d %H:%i') as 'Time',
                DATA as 'Date',
                PROPOSTA as 'Proposal',
                LEFT(CEDENTE, 30) as 'Cedente',
                OLD_STATUS as 'From',
                NEW_STATUS as 'To',
                CONCAT('R$ ', FORMAT(NEW_VLR_APROVADOS, 2)) as 'Value'
            FROM apr_status_history
            ORDER BY changed_at DESC
            LIMIT 20
        """)
        
        results = cursor.fetchall()
        
        if results:
            print(tabulate(results, headers="keys", tablefmt="grid"))
        else:
            print("  No status changes recorded")


def view_processing_log(conn):
    """View processing log."""
    print_section("🔄 PROCESSING LOG (Last 10 Runs)")

    with conn.cursor() as cursor:
        cursor.execute("""
            SELECT
                DATE_FORMAT(run_timestamp, '%Y-%m-%d %H:%i') as 'Executed',
                target_date as 'Target Date',
                total_records_queried as 'Queried',
                valid_records as 'Valid',
                invalid_nfechave as 'NFE',
                invalid_duplicata as 'DUP',
                invalid_seuno as 'SEU',
                execution_time_seconds as 'Time(s)',
                run_mode as 'Mode'
            FROM apr_processing_log
            ORDER BY run_timestamp DESC
            LIMIT 10
        """)
        
        results = cursor.fetchall()
        
        if results:
            print(tabulate(results, headers="keys", tablefmt="grid"))
        else:
            print("  No processing logs available")


def main():
    """Main function."""
    try:
        print("\n" + "=" * 100)
        print("  APR MONITORING DATA VIEWER")
        print("=" * 100)
        
        # Load config and connect
        config = load_config()
        conn = create_connection(config)
        
        # Display all sections
        view_daily_summary(conn)
        view_invalid_records(conn)
        view_product_distribution(conn)
        view_status_changes(conn)
        view_processing_log(conn)
        
        # Close connection
        conn.close()
        
        print("\n" + "=" * 100)
        print("  ✅ Data viewer complete")
        print("=" * 100)
        print()
        
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
        return 1
    
    return 0


if __name__ == "__main__":
    sys.exit(main())

