-- ============================================================================
-- APR Monitoring Tables for Grafana Dashboard
-- Created: 2026-01-30
-- Purpose: Track proposal processing, status changes, and validation issues
-- ============================================================================

-- Table 1: apr_invalid_records
-- Tracks all detected invalid records (NFEChave, DUPLICATA, SEUNO)
-- ============================================================================

CREATE TABLE IF NOT EXISTS apr_invalid_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Proposal identification
    DATA DATE NOT NULL COMMENT 'Proposal date',
    PROPOSTA INT NOT NULL COMMENT 'Proposal number',
    CEDENTE VARCHAR(100) NOT NULL COMMENT 'Client name',
    RAMO VARCHAR(100) COMMENT 'Rating/sector',
    GERENTE VARCHAR(100) COMMENT 'Manager name',
    EMPRESA VARCHAR(100) COMMENT 'Company name',
    STATUS VARCHAR(50) COMMENT 'Status when detected',
    PRODUTO VARCHAR(100) COMMENT 'Product type',
    
    -- Validation details
    VALIDATION_TYPE ENUM('NFECHAVE', 'DUPLICATA', 'SEUNO', 'CHEQUE') NOT NULL COMMENT 'Type of validation failure',
    MOTIVO VARCHAR(255) COMMENT 'Error reason/message',
    NFE_CHAVE VARCHAR(50) COMMENT 'NFE key value',
    DUPLICATA VARCHAR(50) COMMENT 'DUPLICATA value',
    SEUNO VARCHAR(20) COMMENT 'SEUNO value',
    
    -- Tracking timestamps
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When first detected',
    alerted_at TIMESTAMP NULL COMMENT 'When Slack alert was sent',
    is_resolved TINYINT DEFAULT 0 COMMENT '0=unresolved, 1=resolved',
    resolved_at TIMESTAMP NULL COMMENT 'When resolved (NULL if not)',
    
    -- Indexes for common queries
    INDEX idx_proposal (DATA, PROPOSTA),
    INDEX idx_cedente (CEDENTE),
    INDEX idx_validation_type (VALIDATION_TYPE),
    INDEX idx_is_resolved (is_resolved),
    INDEX idx_detected_at (detected_at),
    INDEX idx_resolved_at (resolved_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks invalid APR records for monitoring and resolution';


-- Table 2: apr_status_history
-- Captures every status change for proposals
-- ============================================================================

CREATE TABLE IF NOT EXISTS apr_status_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Proposal identification
    DATA DATE NOT NULL COMMENT 'Proposal date',
    PROPOSTA INT NOT NULL COMMENT 'Proposal number',
    CEDENTE VARCHAR(100) NOT NULL COMMENT 'Client name',
    RAMO VARCHAR(100) COMMENT 'Rating/sector',
    
    -- Status change details
    OLD_STATUS VARCHAR(50) COMMENT 'Previous status',
    NEW_STATUS VARCHAR(50) NOT NULL COMMENT 'New status',
    
    -- Value changes
    OLD_VLR_APROVADOS DECIMAL(15,2) COMMENT 'Previous approved value',
    NEW_VLR_APROVADOS DECIMAL(15,2) COMMENT 'New approved value',
    OLD_QTD_TITULOS INT COMMENT 'Previous title count',
    NEW_QTD_TITULOS INT COMMENT 'New title count',
    
    -- Tracking
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When change was detected',
    CHANGE_SOURCE VARCHAR(50) DEFAULT 'SYSTEM' COMMENT 'Source of change: SYSTEM, BOT, AUTO_RESOLVE, MONITOR',
    
    -- Indexes for common queries
    INDEX idx_proposal (DATA, PROPOSTA),
    INDEX idx_cedente (CEDENTE),
    INDEX idx_new_status (NEW_STATUS),
    INDEX idx_changed_at (changed_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks status changes for proposals over time';


-- Table 3: apr_processing_log
-- Logs each run of the validation program
-- ============================================================================

CREATE TABLE IF NOT EXISTS apr_processing_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Run identification
    run_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When program ran',
    target_date DATE NOT NULL COMMENT 'Date being processed',
    
    -- Processing statistics
    total_records_queried INT DEFAULT 0 COMMENT 'Total records from MSSQL',
    valid_records INT DEFAULT 0 COMMENT 'Count of valid records',
    invalid_nfechave INT DEFAULT 0 COMMENT 'Count of NFEChave failures',
    invalid_duplicata INT DEFAULT 0 COMMENT 'Count of DUPLICATA failures',
    invalid_seuno INT DEFAULT 0 COMMENT 'Count of SEUNO failures',
    invalid_cheque INT DEFAULT 0 COMMENT 'Count of CHEQUE failures',
    records_stored INT DEFAULT 0 COMMENT 'Records inserted/updated in MariaDB',

    -- Alert statistics
    alerts_sent_nfechave INT DEFAULT 0 COMMENT 'NFEChave alerts sent',
    alerts_sent_duplicata INT DEFAULT 0 COMMENT 'DUPLICATA alerts sent',
    alerts_sent_seuno INT DEFAULT 0 COMMENT 'SEUNO alerts sent',
    alerts_sent_cheque INT DEFAULT 0 COMMENT 'CHEQUE alerts sent',
    
    -- Performance
    execution_time_seconds INT COMMENT 'How long the run took',
    run_mode ENUM('PRODUCTION', 'DRY_RUN') DEFAULT 'PRODUCTION' COMMENT 'Mode of execution',
    error_message TEXT COMMENT 'Error details if failed',
    
    -- Indexes
    INDEX idx_run_timestamp (run_timestamp),
    INDEX idx_target_date (target_date),
    INDEX idx_run_mode (run_mode)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Logs each execution of the validation program';


-- Table 4: apr_proposal_products
-- Tracks all product types for each proposal (many-to-many relationship)
-- ============================================================================

CREATE TABLE IF NOT EXISTS apr_proposal_products (
    id INT AUTO_INCREMENT PRIMARY KEY,

    -- Proposal identification
    DATA DATE NOT NULL COMMENT 'Proposal date',
    PROPOSTA INT NOT NULL COMMENT 'Proposal number',
    CEDENTE VARCHAR(100) NOT NULL COMMENT 'Client name',

    -- Product details
    PRODUTO VARCHAR(100) NOT NULL COMMENT 'Product type',
    QTD_TITULOS INT COMMENT 'Number of titles for this product',
    VALOR_TITULOS DECIMAL(15,2) COMMENT 'Total value for this product',

    -- Tracking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When first recorded',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update',

    -- Indexes
    INDEX idx_proposal (DATA, PROPOSTA),
    INDEX idx_produto (PRODUTO),
    INDEX idx_cedente (CEDENTE),
    UNIQUE KEY unique_proposal_product (DATA, PROPOSTA, CEDENTE, PRODUTO)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks product types for each proposal (supports multiple products per proposal)';


-- Table 5: apr_daily_summary
-- Pre-computed daily statistics for fast dashboard queries
-- ============================================================================

CREATE TABLE IF NOT EXISTS apr_daily_summary (
    DATA DATE PRIMARY KEY COMMENT 'The date',

    -- Proposal counts
    total_proposals INT DEFAULT 0 COMMENT 'Total proposals for the day',
    valid_proposals INT DEFAULT 0 COMMENT 'Valid proposals',
    invalid_proposals INT DEFAULT 0 COMMENT 'Invalid proposals',

    -- Financial totals
    total_vlr_aprovados DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Total approved value',
    total_valor_titulos DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Total title value',

    -- Status breakdown
    proposals_aguardando INT DEFAULT 0 COMMENT 'Count in Aguardando status',
    proposals_enviado INT DEFAULT 0 COMMENT 'Count in Enviado para Assinar',
    proposals_assinado INT DEFAULT 0 COMMENT 'Count in Assinado',
    proposals_liberado INT DEFAULT 0 COMMENT 'Count in Liberado',
    proposals_finalizado INT DEFAULT 0 COMMENT 'Count in Finalizado',

    -- Tracking
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',

    -- Indexes
    INDEX idx_last_updated (last_updated)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Daily aggregated metrics for dashboard';

