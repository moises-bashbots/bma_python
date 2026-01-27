-- Migration: Create mensagens_whatsapp table
-- Description: Historic table for tracking all WhatsApp messages sent by the system
-- Date: 2026-01-21
-- Author: BMA System

-- Create the table
CREATE TABLE IF NOT EXISTS mensagens_whatsapp (
    -- Primary Key
    id_mensagem BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Message Identification
    message_id VARCHAR(255) NULL COMMENT 'Unique message ID from hash or API response',
    message_hash VARCHAR(32) NULL COMMENT 'MD5 hash for duplicate detection',
    
    -- Timestamp
    data_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Message send timestamp',
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Receiver Information
    telefone_destino VARCHAR(50) NOT NULL COMMENT 'Recipient phone number (e.g., 5511997435829)',
    nome_contato VARCHAR(255) NULL COMMENT 'Contact name from contato_whatsapp table',
    is_group BOOLEAN DEFAULT FALSE COMMENT 'Whether message was sent to a group',
    
    -- Message Content
    mensagem TEXT NOT NULL COMMENT 'Full message text sent',
    tipo_mensagem ENUM('status_update', 'alert', 'notification', 'manual') NOT NULL COMMENT 'Message type/category',
    
    -- Business Context (APR_CAPA related)
    cedente VARCHAR(100) NULL COMMENT 'Cedente name from APR_CAPA',
    grupo VARCHAR(100) NULL COMMENT 'Grupo from cedente table',
    data_proposta DATE NULL COMMENT 'Proposal date from APR_CAPA.DATA',
    numero_proposta INT NULL COMMENT 'Proposal number from APR_CAPA.NUMERO',
    bordero VARCHAR(100) NULL COMMENT 'Borderô number if applicable',
    status_fluxo VARCHAR(50) NULL COMMENT 'Workflow status (e.g., Aguardando Analista, Enviado para Assinar)',
    
    -- Financial Details
    qtd_recebiveis INT NULL COMMENT 'Quantity of receivables (QTD_PROPOSTOS or QTD_APROVADOS)',
    valor_total DECIMAL(15,2) NULL COMMENT 'Total value (VLR_PROPOSTOS or VLR_APROVADOS)',
    
    -- API Response
    status_envio ENUM('success', 'failed', 'pending') NOT NULL DEFAULT 'pending' COMMENT 'Send status',
    status_code INT NULL COMMENT 'HTTP status code from API response',
    api_response JSON NULL COMMENT 'Full API response for debugging',
    erro_mensagem TEXT NULL COMMENT 'Error message if send failed',
    
    -- Configuration
    config_file VARCHAR(255) NULL COMMENT 'WhatsApp config file used',
    api_provider VARCHAR(50) DEFAULT 'Z-API' COMMENT 'API provider (Z-API, Infobip, etc.)',
    
    -- Tracking & Audit
    usuario VARCHAR(100) NULL COMMENT 'User who triggered the message (if applicable)',
    origem VARCHAR(100) DEFAULT 'automated' COMMENT 'Message origin (automated, manual, scheduled)',
    tentativas_envio INT DEFAULT 1 COMMENT 'Number of send attempts',
    
    -- Indexes
    INDEX idx_telefone_destino (telefone_destino),
    INDEX idx_cedente (cedente),
    INDEX idx_data_envio (data_envio),
    INDEX idx_status_envio (status_envio),
    INDEX idx_message_hash (message_hash),
    INDEX idx_proposta (data_proposta, numero_proposta),
    INDEX idx_status_fluxo (status_fluxo),
    INDEX idx_tipo_mensagem (tipo_mensagem)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historic table for tracking all WhatsApp messages sent by the system';

-- Verify table creation
SELECT 'Table mensagens_whatsapp created successfully!' AS status;

-- Show table structure
DESCRIBE mensagens_whatsapp;

