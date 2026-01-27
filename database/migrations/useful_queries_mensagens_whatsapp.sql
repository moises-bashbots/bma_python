-- Useful Queries for mensagens_whatsapp Table
-- Collection of common queries for analyzing WhatsApp message history

-- ============================================
-- BASIC QUERIES
-- ============================================

-- 1. View all messages (latest first)
SELECT 
    id_mensagem,
    data_envio,
    telefone_destino,
    nome_contato,
    cedente,
    tipo_mensagem,
    status_envio,
    LEFT(mensagem, 50) as mensagem_preview
FROM mensagens_whatsapp
ORDER BY data_envio DESC
LIMIT 20;

-- 2. Count total messages
SELECT COUNT(*) as total_messages FROM mensagens_whatsapp;

-- 3. Messages sent today
SELECT * FROM mensagens_whatsapp 
WHERE DATE(data_envio) = CURDATE()
ORDER BY data_envio DESC;

-- 4. Messages sent in the last 7 days
SELECT 
    DATE(data_envio) as data,
    COUNT(*) as total,
    SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) as successful,
    SUM(CASE WHEN status_envio = 'failed' THEN 1 ELSE 0 END) as failed
FROM mensagens_whatsapp
WHERE data_envio >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(data_envio)
ORDER BY data DESC;

-- ============================================
-- STATUS ANALYSIS
-- ============================================

-- 5. Messages by status
SELECT 
    status_envio,
    COUNT(*) as total,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM mensagens_whatsapp), 2) as percentage
FROM mensagens_whatsapp
GROUP BY status_envio;

-- 6. Failed messages with error details
SELECT 
    data_envio,
    cedente,
    telefone_destino,
    tipo_mensagem,
    status_code,
    erro_mensagem,
    LEFT(mensagem, 100) as mensagem_preview
FROM mensagens_whatsapp 
WHERE status_envio = 'failed'
ORDER BY data_envio DESC;

-- ============================================
-- CEDENTE ANALYSIS
-- ============================================

-- 7. Messages by cedente
SELECT 
    cedente,
    COUNT(*) as total_messages,
    SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) as successful,
    SUM(CASE WHEN status_envio = 'failed' THEN 1 ELSE 0 END) as failed,
    MAX(data_envio) as last_message
FROM mensagens_whatsapp 
WHERE cedente IS NOT NULL
GROUP BY cedente
ORDER BY total_messages DESC;

-- 8. Top 10 cedentes by message volume
SELECT 
    cedente,
    COUNT(*) as total,
    SUM(valor_total) as valor_total_acumulado
FROM mensagens_whatsapp
WHERE cedente IS NOT NULL
GROUP BY cedente
ORDER BY total DESC
LIMIT 10;

-- ============================================
-- MESSAGE TYPE ANALYSIS
-- ============================================

-- 9. Messages by type
SELECT 
    tipo_mensagem,
    COUNT(*) as total,
    SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) as successful
FROM mensagens_whatsapp
GROUP BY tipo_mensagem
ORDER BY total DESC;

-- 10. Status updates by workflow status
SELECT 
    status_fluxo,
    COUNT(*) as total,
    AVG(valor_total) as avg_value,
    SUM(qtd_recebiveis) as total_recebiveis
FROM mensagens_whatsapp
WHERE tipo_mensagem = 'status_update' AND status_fluxo IS NOT NULL
GROUP BY status_fluxo
ORDER BY total DESC;

-- ============================================
-- FINANCIAL ANALYSIS
-- ============================================

-- 11. Total value by status
SELECT 
    status_fluxo,
    COUNT(*) as total_messages,
    SUM(valor_total) as valor_total,
    SUM(qtd_recebiveis) as qtd_total_recebiveis,
    AVG(valor_total) as valor_medio
FROM mensagens_whatsapp
WHERE valor_total IS NOT NULL
GROUP BY status_fluxo;

-- 12. Daily financial summary
SELECT 
    DATE(data_envio) as data,
    COUNT(*) as total_messages,
    SUM(valor_total) as valor_total_dia,
    SUM(qtd_recebiveis) as qtd_recebiveis_dia
FROM mensagens_whatsapp
WHERE valor_total IS NOT NULL
GROUP BY DATE(data_envio)
ORDER BY data DESC;

-- ============================================
-- DUPLICATE DETECTION
-- ============================================

-- 13. Find duplicate message hashes
SELECT 
    message_hash,
    COUNT(*) as occurrences,
    GROUP_CONCAT(id_mensagem) as message_ids
FROM mensagens_whatsapp
WHERE message_hash IS NOT NULL
GROUP BY message_hash
HAVING COUNT(*) > 1;

-- 14. Messages sent to same phone multiple times today
SELECT 
    telefone_destino,
    nome_contato,
    COUNT(*) as times_sent,
    GROUP_CONCAT(DISTINCT tipo_mensagem) as message_types
FROM mensagens_whatsapp
WHERE DATE(data_envio) = CURDATE()
GROUP BY telefone_destino, nome_contato
HAVING COUNT(*) > 1;

-- ============================================
-- PERFORMANCE MONITORING
-- ============================================

-- 15. Success rate by hour
SELECT 
    HOUR(data_envio) as hour,
    COUNT(*) as total,
    SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) as successful,
    ROUND(SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate
FROM mensagens_whatsapp
WHERE DATE(data_envio) = CURDATE()
GROUP BY HOUR(data_envio)
ORDER BY hour;

-- 16. API provider performance
SELECT 
    api_provider,
    COUNT(*) as total,
    SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) as successful,
    ROUND(SUM(CASE WHEN status_envio = 'success' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate
FROM mensagens_whatsapp
GROUP BY api_provider;

