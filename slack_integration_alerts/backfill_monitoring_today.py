#!/usr/bin/env python3
"""
Backfill monitoring tables with today's data from apr_valid_records.

This script:
1. Reads existing apr_valid_records for today
2. Creates initial entries in apr_status_history
3. Updates apr_daily_summary with today's aggregated data
"""

import sys
from pathlib import Path
from datetime import date, datetime
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

from models_mariadb import (
    APRValidRecord,
    APRStatusHistory,
    APRDailySummary,
    APRInvalidRecord
)
from monitoring_helpers import update_daily_summary


def load_config():
    """Load database configuration."""
    import json
    config_file = Path(__file__).parent / 'databases_config.json'
    with open(config_file, 'r') as f:
        return json.load(f)


def create_mariadb_engine(config):
    """Create SQLAlchemy engine for MariaDB."""
    db_config = config['databases']['mariadb']
    
    connection_string = (
        f"mysql+pymysql://{db_config['user']}:{db_config['password']}"
        f"@{db_config['server']}:{db_config['port']}/{db_config['scheme']}"
        f"?charset=utf8mb4"
    )
    
    return create_engine(connection_string, echo=False)


def backfill_status_history(session, target_date):
    """
    Create initial status history entries for today's valid records.
    
    Args:
        session: SQLAlchemy session
        target_date: Date to backfill
    
    Returns:
        Number of history entries created
    """
    print(f"\n{'='*80}")
    print(f"Backfilling Status History for {target_date}")
    print(f"{'='*80}")
    
    # Get all valid records for today
    valid_records = session.query(APRValidRecord).filter(
        APRValidRecord.DATA == target_date
    ).all()
    
    print(f"Found {len(valid_records)} valid records for {target_date}")
    
    created_count = 0
    
    for record in valid_records:
        # Check if history already exists for this proposal
        existing = session.query(APRStatusHistory).filter(
            APRStatusHistory.DATA == record.DATA,
            APRStatusHistory.PROPOSTA == record.PROPOSTA,
            APRStatusHistory.CEDENTE == record.CEDENTE
        ).first()
        
        if not existing:
            # Create initial history entry (no OLD_STATUS since it's the first entry)
            history = APRStatusHistory(
                DATA=record.DATA,
                PROPOSTA=record.PROPOSTA,
                CEDENTE=record.CEDENTE,
                RAMO=record.RAMO,
                OLD_STATUS=None,  # First entry
                NEW_STATUS=record.STATUS,
                OLD_VLR_APROVADOS=None,
                NEW_VLR_APROVADOS=record.VLR_APROVADOS,
                OLD_QTD_TITULOS=None,
                NEW_QTD_TITULOS=record.QTD_TITULOS,
                CHANGE_SOURCE='BACKFILL'
            )
            session.add(history)
            created_count += 1
    
    if created_count > 0:
        session.commit()
        print(f"✓ Created {created_count} status history entries")
    else:
        print("✓ No new history entries needed (already exists)")
    
    return created_count


def backfill_daily_summary(session, target_date):
    """
    Update daily summary for today.
    
    Args:
        session: SQLAlchemy session
        target_date: Date to backfill
    """
    print(f"\n{'='*80}")
    print(f"Updating Daily Summary for {target_date}")
    print(f"{'='*80}")
    
    update_daily_summary(session, target_date)
    
    # Show the summary
    summary = session.query(APRDailySummary).filter(
        APRDailySummary.DATA == target_date
    ).first()
    
    if summary:
        print(f"\n✓ Daily Summary Updated:")
        print(f"  Total Proposals: {summary.total_proposals}")
        print(f"  Valid Proposals: {summary.valid_proposals}")
        print(f"  Invalid Proposals: {summary.invalid_proposals}")
        print(f"  Total VLR Aprovados: R$ {summary.total_vlr_aprovados:,.2f}")
        print(f"  Aguardando: {summary.proposals_aguardando}")
        print(f"  Enviado: {summary.proposals_enviado}")
        print(f"  Assinado: {summary.proposals_assinado}")
        print(f"  Liberado: {summary.proposals_liberado}")
        print(f"  Finalizado: {summary.proposals_finalizado}")
    else:
        print("⚠ No summary created (no data for this date)")


def main():
    """Main backfill function."""
    print("=" * 80)
    print("APR Monitoring Tables - Backfill Today's Data")
    print("=" * 80)
    
    # Use today's date
    target_date = date.today()
    print(f"\nTarget Date: {target_date}")
    
    # Load config and create engine
    config = load_config()
    engine = create_mariadb_engine(config)
    Session = sessionmaker(bind=engine)
    session = Session()
    
    try:
        # Backfill status history
        backfill_status_history(session, target_date)
        
        # Update daily summary
        backfill_daily_summary(session, target_date)
        
        print(f"\n{'='*80}")
        print("✓ Backfill completed successfully!")
        print(f"{'='*80}")
        
    except Exception as e:
        print(f"\n✗ Error during backfill: {e}")
        import traceback
        traceback.print_exc()
        session.rollback()
        return 1
    
    finally:
        session.close()
    
    return 0


if __name__ == '__main__':
    sys.exit(main())

