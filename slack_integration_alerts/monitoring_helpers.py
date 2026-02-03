#!/usr/bin/env python3
"""
Helper functions for APR monitoring tables.

Functions for:
- Logging invalid records
- Tracking status changes
- Logging program execution
- Updating daily summary
- Auto-resolving invalid records
- Cleaning up old data (30-day retention)
"""

from datetime import datetime, date, timedelta
from decimal import Decimal
from typing import List, Dict, Optional
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_

from models_mariadb import (
    APRInvalidRecord,
    APRStatusHistory,
    APRProcessingLog,
    APRDailySummary,
    APRValidRecord,
    APRProposalProduct
)


# ============================================================================
# INVALID RECORDS TRACKING
# ============================================================================

def log_invalid_records(
    mariadb_session: Session,
    invalid_records: List[Dict],
    validation_type: str,
    alerted_count: int = 0
) -> int:
    """
    Log invalid records to apr_invalid_records table.
    
    Args:
        mariadb_session: MariaDB session
        invalid_records: List of invalid record dictionaries
        validation_type: 'NFECHAVE', 'DUPLICATA', or 'SEUNO'
        alerted_count: Number of alerts actually sent
    
    Returns:
        Number of records logged
    """
    logged_count = 0
    alerted_at = datetime.now() if alerted_count > 0 else None
    
    for record in invalid_records:
        # Check if this exact record already exists (not resolved)
        existing = mariadb_session.query(APRInvalidRecord).filter(
            and_(
                APRInvalidRecord.DATA == record.get('DATA'),
                APRInvalidRecord.PROPOSTA == record.get('PROPOSTA'),
                APRInvalidRecord.CEDENTE == record.get('CEDENTE'),
                APRInvalidRecord.VALIDATION_TYPE == validation_type,
                APRInvalidRecord.is_resolved == 0
            )
        ).first()
        
        if not existing:
            # Create new invalid record
            invalid_rec = APRInvalidRecord(
                DATA=record.get('DATA'),
                PROPOSTA=record.get('PROPOSTA'),
                CEDENTE=record.get('CEDENTE'),
                RAMO=record.get('RAMO'),
                GERENTE=record.get('GERENTE'),
                EMPRESA=record.get('EMPRESA'),
                STATUS=record.get('STATUS'),
                PRODUTO=record.get('PRODUTO'),
                VALIDATION_TYPE=validation_type,
                MOTIVO=record.get('MOTIVO_INVALIDO'),
                NFE_CHAVE=record.get('NFEChave'),
                DUPLICATA=record.get('DUPLICATA'),
                SEUNO=record.get('SEUNO'),
                alerted_at=alerted_at
            )
            mariadb_session.add(invalid_rec)
            logged_count += 1
    
    if logged_count > 0:
        mariadb_session.commit()
    
    return logged_count


def auto_resolve_invalid_records(mariadb_session: Session, target_date: date) -> int:
    """
    Automatically mark invalid records as resolved if they now appear in apr_valid_records.
    
    Args:
        mariadb_session: MariaDB session
        target_date: Date to check
    
    Returns:
        Number of records auto-resolved
    """
    resolved_count = 0
    
    # Get all unresolved invalid records for this date
    unresolved = mariadb_session.query(APRInvalidRecord).filter(
        and_(
            APRInvalidRecord.DATA == target_date,
            APRInvalidRecord.is_resolved == 0
        )
    ).all()
    
    for invalid_rec in unresolved:
        # Check if this proposal now exists in apr_valid_records
        valid_exists = mariadb_session.query(APRValidRecord).filter(
            and_(
                APRValidRecord.DATA == invalid_rec.DATA,
                APRValidRecord.PROPOSTA == invalid_rec.PROPOSTA,
                APRValidRecord.CEDENTE == invalid_rec.CEDENTE
            )
        ).first()
        
        if valid_exists:
            # Mark as resolved
            invalid_rec.is_resolved = 1
            invalid_rec.resolved_at = datetime.now()
            resolved_count += 1
    
    if resolved_count > 0:
        mariadb_session.commit()
    
    return resolved_count


# ============================================================================
# STATUS CHANGE TRACKING
# ============================================================================

def track_all_status_changes_from_source(
    mssql_session: Session,
    mariadb_session: Session,
    target_date: date,
    change_source: str = 'SYSTEM'
) -> int:
    """
    Track ALL status changes by querying MSSQL source and comparing with MariaDB.

    This function:
    1. Queries ALL proposals from MSSQL APR_CAPA for target date
    2. Compares with apr_valid_records AND apr_invalid_records in MariaDB
    3. Detects status changes from ANY source (manual, bot, system)
    4. Logs changes to apr_status_history

    Args:
        mssql_session: MSSQL session (source database)
        mariadb_session: MariaDB session (monitoring database)
        target_date: Date to check
        change_source: 'SYSTEM', 'BOT', or 'AUTO_RESOLVE'

    Returns:
        Number of status changes tracked
    """
    from models import APRCapa, Cedente as MSSQLCedente

    changes_tracked = 0

    # Query ALL proposals from MSSQL source for this date
    mssql_proposals = mssql_session.query(
        APRCapa.DATA,
        APRCapa.NUMERO,
        APRCapa.CEDENTE,
        APRCapa.status_atual,
        APRCapa.QTD_APROVADOS,
        APRCapa.VLR_APROVADOS,
        MSSQLCedente.RAMO
    ).outerjoin(
        MSSQLCedente,
        APRCapa.CEDENTE == MSSQLCedente.APELIDO
    ).filter(
        APRCapa.DATA == target_date
    ).all()

    print(f"  Queried {len(mssql_proposals)} proposals from MSSQL source")

    # Process each proposal from MSSQL
    for mssql_prop in mssql_proposals:
        data = mssql_prop.DATA.date() if hasattr(mssql_prop.DATA, 'date') else mssql_prop.DATA
        proposta = mssql_prop.NUMERO
        cedente = mssql_prop.CEDENTE
        current_status = mssql_prop.STATUS_ATUAL
        current_vlr = mssql_prop.VLR_APROVADOS or 0
        current_qty = mssql_prop.QTD_APROVADOS or 0
        ramo = mssql_prop.RAMO

        # Get the most recent history entry for this proposal
        last_history = mariadb_session.query(APRStatusHistory).filter(
            and_(
                APRStatusHistory.DATA == data,
                APRStatusHistory.PROPOSTA == proposta,
                APRStatusHistory.CEDENTE == cedente
            )
        ).order_by(APRStatusHistory.changed_at.desc()).first()

        # Determine if status changed
        old_status = last_history.NEW_STATUS if last_history else None
        old_vlr = last_history.NEW_VLR_APROVADOS if last_history else None
        old_qty = last_history.NEW_QTD_TITULOS if last_history else None

        # Check if anything changed
        status_changed = (old_status != current_status)
        vlr_changed = (old_vlr != current_vlr) if old_vlr is not None else False
        qty_changed = (old_qty != current_qty) if old_qty is not None else False

        # Log change if status changed OR if this is first time seeing this proposal
        if status_changed or not last_history:
            history = APRStatusHistory(
                DATA=data,
                PROPOSTA=proposta,
                CEDENTE=cedente,
                RAMO=ramo,
                OLD_STATUS=old_status,
                NEW_STATUS=current_status,
                OLD_VLR_APROVADOS=old_vlr,
                NEW_VLR_APROVADOS=current_vlr,
                OLD_QTD_TITULOS=old_qty,
                NEW_QTD_TITULOS=current_qty,
                CHANGE_SOURCE=change_source
            )
            mariadb_session.add(history)
            changes_tracked += 1
        # Also log if values changed significantly (even if status didn't)
        elif vlr_changed or qty_changed:
            history = APRStatusHistory(
                DATA=data,
                PROPOSTA=proposta,
                CEDENTE=cedente,
                RAMO=ramo,
                OLD_STATUS=old_status,
                NEW_STATUS=current_status,  # Same status
                OLD_VLR_APROVADOS=old_vlr,
                NEW_VLR_APROVADOS=current_vlr,
                OLD_QTD_TITULOS=old_qty,
                NEW_QTD_TITULOS=current_qty,
                CHANGE_SOURCE=change_source
            )
            mariadb_session.add(history)
            changes_tracked += 1

    if changes_tracked > 0:
        mariadb_session.commit()

    return changes_tracked


def track_status_changes(
    mariadb_session: Session,
    target_date: date,
    change_source: str = 'SYSTEM'
) -> int:
    """
    DEPRECATED: Use track_all_status_changes_from_source() instead.

    Track status changes by comparing current apr_valid_records with previous state.
    This function is kept for backward compatibility but should not be used.

    Args:
        mariadb_session: MariaDB session
        target_date: Date to check
        change_source: 'SYSTEM' or 'BOT'

    Returns:
        Number of status changes tracked
    """
    changes_tracked = 0

    # Get all records for this date
    current_records = mariadb_session.query(APRValidRecord).filter(
        APRValidRecord.DATA == target_date
    ).all()

    for current in current_records:
        # Check if update_count > 0 (meaning it was updated, not inserted)
        if current.update_count > 0:
            # Get the most recent history entry for this proposal
            last_history = mariadb_session.query(APRStatusHistory).filter(
                and_(
                    APRStatusHistory.DATA == current.DATA,
                    APRStatusHistory.PROPOSTA == current.PROPOSTA,
                    APRStatusHistory.CEDENTE == current.CEDENTE
                )
            ).order_by(APRStatusHistory.changed_at.desc()).first()

            # Determine if status actually changed
            old_status = last_history.NEW_STATUS if last_history else None
            status_changed = (old_status != current.STATUS) if old_status else True

            if status_changed or not last_history:
                # Log the status change
                history = APRStatusHistory(
                    DATA=current.DATA,
                    PROPOSTA=current.PROPOSTA,
                    CEDENTE=current.CEDENTE,
                    RAMO=current.RAMO,
                    OLD_STATUS=old_status,
                    NEW_STATUS=current.STATUS,
                    OLD_VLR_APROVADOS=last_history.NEW_VLR_APROVADOS if last_history else None,
                    NEW_VLR_APROVADOS=current.VLR_APROVADOS,
                    OLD_QTD_TITULOS=last_history.NEW_QTD_TITULOS if last_history else None,
                    NEW_QTD_TITULOS=current.QTD_TITULOS,
                    CHANGE_SOURCE=change_source
                )
                mariadb_session.add(history)
                changes_tracked += 1

    if changes_tracked > 0:
        mariadb_session.commit()

    return changes_tracked


# ============================================================================
# PROCESSING LOG
# ============================================================================

def log_program_execution(
    mariadb_session: Session,
    target_date: date,
    stats: Dict,
    execution_time: int,
    run_mode: str = 'PRODUCTION',
    error_message: Optional[str] = None
) -> int:
    """
    Log program execution to apr_processing_log table.

    Args:
        mariadb_session: MariaDB session
        target_date: Date being processed
        stats: Dictionary with processing statistics
        execution_time: Execution time in seconds
        run_mode: 'PRODUCTION' or 'DRY_RUN'
        error_message: Error message if failed

    Returns:
        Log entry ID
    """
    log_entry = APRProcessingLog(
        target_date=target_date,
        total_records_queried=stats.get('total_records_queried', 0),
        valid_records=stats.get('valid_records', 0),
        invalid_nfechave=stats.get('invalid_nfechave', 0),
        invalid_duplicata=stats.get('invalid_duplicata', 0),
        invalid_seuno=stats.get('invalid_seuno', 0),
        invalid_cheque=stats.get('invalid_cheque', 0),
        records_stored=stats.get('records_stored', 0),
        alerts_sent_nfechave=stats.get('alerts_sent_nfechave', 0),
        alerts_sent_duplicata=stats.get('alerts_sent_duplicata', 0),
        alerts_sent_seuno=stats.get('alerts_sent_seuno', 0),
        alerts_sent_cheque=stats.get('alerts_sent_cheque', 0),
        execution_time_seconds=execution_time,
        run_mode=run_mode,
        error_message=error_message
    )

    mariadb_session.add(log_entry)
    mariadb_session.commit()

    return log_entry.id


# ============================================================================
# DAILY SUMMARY
# ============================================================================

def update_daily_summary(mariadb_session: Session, target_date: date) -> None:
    """
    Update or create daily summary for the target date.

    Args:
        mariadb_session: MariaDB session
        target_date: Date to summarize
    """
    # Get all valid records for this date
    valid_records = mariadb_session.query(APRValidRecord).filter(
        APRValidRecord.DATA == target_date
    ).all()

    # Get all invalid records for this date
    invalid_count = mariadb_session.query(APRInvalidRecord).filter(
        and_(
            APRInvalidRecord.DATA == target_date,
            APRInvalidRecord.is_resolved == 0
        )
    ).count()

    # Calculate aggregates
    total_proposals = len(set((r.PROPOSTA, r.CEDENTE) for r in valid_records))
    total_vlr_aprovados = sum(r.VLR_APROVADOS or 0 for r in valid_records)
    total_valor_titulos = sum(r.VALOR_TITULOS or 0 for r in valid_records)

    # Count by status
    status_counts = {}
    for record in valid_records:
        status = record.STATUS or 'Unknown'
        status_counts[status] = status_counts.get(status, 0) + 1

    # Map to specific status fields
    proposals_aguardando = sum(
        count for status, count in status_counts.items()
        if 'aguardando' in status.lower()
    )
    proposals_enviado = status_counts.get('Enviado para Assinar', 0)
    proposals_assinado = status_counts.get('Assinado', 0)
    proposals_liberado = status_counts.get('Liberado', 0)
    proposals_finalizado = status_counts.get('Finalizado', 0)

    # Check if summary already exists
    summary = mariadb_session.query(APRDailySummary).filter(
        APRDailySummary.DATA == target_date
    ).first()

    if summary:
        # Update existing
        summary.total_proposals = total_proposals
        summary.valid_proposals = total_proposals
        summary.invalid_proposals = invalid_count
        summary.total_vlr_aprovados = total_vlr_aprovados
        summary.total_valor_titulos = total_valor_titulos
        summary.proposals_aguardando = proposals_aguardando
        summary.proposals_enviado = proposals_enviado
        summary.proposals_assinado = proposals_assinado
        summary.proposals_liberado = proposals_liberado
        summary.proposals_finalizado = proposals_finalizado
    else:
        # Create new
        summary = APRDailySummary(
            DATA=target_date,
            total_proposals=total_proposals,
            valid_proposals=total_proposals,
            invalid_proposals=invalid_count,
            total_vlr_aprovados=total_vlr_aprovados,
            total_valor_titulos=total_valor_titulos,
            proposals_aguardando=proposals_aguardando,
            proposals_enviado=proposals_enviado,
            proposals_assinado=proposals_assinado,
            proposals_liberado=proposals_liberado,
            proposals_finalizado=proposals_finalizado
        )
        mariadb_session.add(summary)

    mariadb_session.commit()


# ============================================================================
# DATA CLEANUP (30-day retention)
# ============================================================================

def cleanup_old_monitoring_data(mariadb_session: Session, retention_days: int = 30) -> Dict[str, int]:
    """
    Clean up monitoring data older than retention_days.

    Args:
        mariadb_session: MariaDB session
        retention_days: Number of days to retain (default: 30)

    Returns:
        Dictionary with counts of deleted records per table
    """
    cutoff_date = date.today() - timedelta(days=retention_days)
    deleted_counts = {}

    # Clean up apr_invalid_records
    deleted = mariadb_session.query(APRInvalidRecord).filter(
        APRInvalidRecord.DATA < cutoff_date
    ).delete()
    deleted_counts['apr_invalid_records'] = deleted

    # Clean up apr_status_history
    deleted = mariadb_session.query(APRStatusHistory).filter(
        APRStatusHistory.DATA < cutoff_date
    ).delete()
    deleted_counts['apr_status_history'] = deleted

    # Clean up apr_processing_log
    deleted = mariadb_session.query(APRProcessingLog).filter(
        APRProcessingLog.target_date < cutoff_date
    ).delete()
    deleted_counts['apr_processing_log'] = deleted

    # Clean up apr_daily_summary
    deleted = mariadb_session.query(APRDailySummary).filter(
        APRDailySummary.DATA < cutoff_date
    ).delete()
    deleted_counts['apr_daily_summary'] = deleted

    mariadb_session.commit()

    # Clean up apr_proposal_products
    deleted = mariadb_session.query(APRProposalProduct).filter(
        APRProposalProduct.DATA < cutoff_date
    ).delete()
    deleted_counts['apr_proposal_products'] = deleted

    mariadb_session.commit()

    return deleted_counts


# ============================================================================
# PRODUCT TRACKING
# ============================================================================

def track_proposal_products(
    mariadb_session: Session,
    mssql_session: Session,
    target_date
) -> int:
    """
    Track product types for each proposal by querying MSSQL database.

    This function stores all products associated with each proposal,
    allowing Grafana dashboards to filter and analyze by product type.

    Args:
        mariadb_session: MariaDB session
        mssql_session: MSSQL session to query product data
        target_date: Date to query proposals for

    Returns:
        Number of product records created/updated
    """
    from models import APRCapa, APRTitulos, Produto, ProdutoCedente
    from sqlalchemy import cast, Date, func

    try:
        # Query products for all proposals on target_date
        # Group by proposal and product to get aggregated counts and values
        # Join chain: APRTitulos.id_produto -> ProdutoCedente.Id -> ProdutoCedente.IdProdutoAtributo -> Produto.Id
        # FIXED: Bypass broken ProdutoAtributo table - join ProdutoCedente.IdProdutoAtributo directly to Produto.Id
        # Use COALESCE to handle NULL products (titles without product assigned)
        from sqlalchemy import func as sqlfunc, case

        query = mssql_session.query(
            APRCapa.DATA,
            APRCapa.NUMERO.label('PROPOSTA'),
            APRCapa.CEDENTE,
            sqlfunc.coalesce(Produto.Descritivo, 'SEM PRODUTO').label('PRODUTO'),
            func.count(APRTitulos.TITULO).label('QTD_TITULOS'),
            func.sum(APRTitulos.VALOR).label('VALOR_TITULOS')
        ).join(
            APRTitulos,
            and_(
                APRCapa.DATA == APRTitulos.DATA,
                APRCapa.NUMERO == APRTitulos.NUMERO
            )
        ).outerjoin(
            ProdutoCedente,
            APRTitulos.id_produto == ProdutoCedente.Id
        ).outerjoin(
            Produto,
            ProdutoCedente.IdProdutoAtributo == Produto.Id
        ).filter(
            cast(APRCapa.DATA, Date) == target_date
        ).group_by(
            APRCapa.DATA,
            APRCapa.NUMERO,
            APRCapa.CEDENTE,
            sqlfunc.coalesce(Produto.Descritivo, 'SEM PRODUTO')
        )

        results = query.all()

        if not results:
            return 0

        tracked_count = 0

        # Store each product for each proposal
        for row in results:
            # Check if this product already exists for this proposal
            existing = mariadb_session.query(APRProposalProduct).filter(
                and_(
                    APRProposalProduct.DATA == row.DATA,
                    APRProposalProduct.PROPOSTA == row.PROPOSTA,
                    APRProposalProduct.CEDENTE == row.CEDENTE,
                    APRProposalProduct.PRODUTO == row.PRODUTO
                )
            ).first()

            if existing:
                # Update existing record
                existing.QTD_TITULOS = row.QTD_TITULOS
                existing.VALOR_TITULOS = row.VALOR_TITULOS
                tracked_count += 1
            else:
                # Create new record
                product_rec = APRProposalProduct(
                    DATA=row.DATA,
                    PROPOSTA=row.PROPOSTA,
                    CEDENTE=row.CEDENTE,
                    PRODUTO=row.PRODUTO,
                    QTD_TITULOS=row.QTD_TITULOS,
                    VALOR_TITULOS=row.VALOR_TITULOS
                )
                mariadb_session.add(product_rec)
                tracked_count += 1

        if tracked_count > 0:
            mariadb_session.commit()

        return tracked_count

    except Exception as e:
        mariadb_session.rollback()
        print(f"Warning: Could not track products: {e}")
        return 0


