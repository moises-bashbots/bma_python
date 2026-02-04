#!/usr/bin/env python3
"""
APR Proposal Monitoring Dashboard
Simple Flask web dashboard for real-time monitoring
"""

from flask import Flask, render_template, jsonify, request, redirect, url_for, session
from flask.json.provider import DefaultJSONProvider
from werkzeug.middleware.proxy_fix import ProxyFix
import pymysql
import json
from datetime import datetime, date, timedelta
from decimal import Decimal
import bcrypt
from functools import wraps
import secrets

# Custom JSON encoder for Flask
class CustomJSONProvider(DefaultJSONProvider):
    def default(self, obj):
        if isinstance(obj, Decimal):
            return float(obj)
        if isinstance(obj, datetime):
            # Return datetime as string in format: "YYYY-MM-DD HH:MM:SS"
            # Database stores in Brazil local time, so we keep it as-is
            return obj.strftime('%Y-%m-%d %H:%M:%S')
        if isinstance(obj, date):
            return obj.isoformat()
        return super().default(obj)

app = Flask(__name__)
app.json = CustomJSONProvider(app)
app.secret_key = secrets.token_hex(32)  # Generate a secure secret key

# Configure ProxyFix for nginx reverse proxy
# This ensures Flask correctly handles X-Forwarded-* headers
app.wsgi_app = ProxyFix(app.wsgi_app, x_for=1, x_proto=1, x_host=1, x_port=1, x_prefix=1)

# Session configuration
app.config['PERMANENT_SESSION_LIFETIME'] = timedelta(hours=8)  # 8 hours session timeout
app.config['SESSION_COOKIE_SECURE'] = True  # Require HTTPS for session cookies
app.config['SESSION_COOKIE_HTTPONLY'] = True  # Prevent JavaScript access to session cookie
app.config['SESSION_COOKIE_SAMESITE'] = 'Lax'  # CSRF protection

# Proxy configuration (for nginx reverse proxy)
app.config['PREFERRED_URL_SCHEME'] = 'https'

# Load database configuration
def load_db_config():
    """Load MariaDB configuration from databases_config.json"""
    config_path = '/home/robot/Dev/bma_python/slack_integration_alerts/databases_config.json'
    with open(config_path, 'r') as f:
        config = json.load(f)
    return config['databases']['mariadb']

def get_db_connection():
    """Create MariaDB connection"""
    config = load_db_config()
    return pymysql.connect(
        host=config['server'],
        port=config.get('port', 3306),
        user=config['user'],
        password=config['password'],
        database=config['scheme'],
        cursorclass=pymysql.cursors.DictCursor
    )

def decimal_default(obj):
    """JSON serializer for Decimal objects"""
    if isinstance(obj, Decimal):
        return float(obj)
    if isinstance(obj, datetime):
        # Return datetime as string in format: "YYYY-MM-DD HH:MM:SS"
        # Database stores in Brazil local time, so we keep it as-is
        return obj.strftime('%Y-%m-%d %H:%M:%S')
    if isinstance(obj, date):
        return obj.isoformat()
    raise TypeError

# Authentication decorator
def login_required(f):
    """Decorator to require login for routes"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'user_id' not in session:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated_function

def verify_password(password: str, password_hash: str) -> bool:
    """Verify a password against its hash"""
    return bcrypt.checkpw(password.encode('utf-8'), password_hash.encode('utf-8'))

@app.route('/login', methods=['GET', 'POST'])
def login():
    """Login page"""
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')

        conn = get_db_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute("""
                    SELECT id, username, password_hash, full_name, is_active
                    FROM dashboard_users
                    WHERE username = %s AND is_active = TRUE
                """, (username,))
                user = cursor.fetchone()

                if user and verify_password(password, user['password_hash']):
                    # Login successful
                    session.permanent = True  # Enable permanent session (uses PERMANENT_SESSION_LIFETIME)
                    session['user_id'] = user['id']
                    session['username'] = user['username']
                    session['full_name'] = user['full_name']

                    # Update last login
                    cursor.execute("""
                        UPDATE dashboard_users
                        SET last_login = NOW()
                        WHERE id = %s
                    """, (user['id'],))
                    conn.commit()

                    return redirect(url_for('index'))
                else:
                    return render_template('login.html', error='Usuário ou senha inválidos')
        finally:
            conn.close()

    # If already logged in, redirect to dashboard
    if 'user_id' in session:
        return redirect(url_for('index'))

    return render_template('login.html')

@app.route('/logout')
def logout():
    """Logout and clear session"""
    session.clear()
    return redirect(url_for('login'))

@app.route('/')
@login_required
def index():
    """Main dashboard page"""
    return render_template('dashboard.html', user=session.get('full_name', session.get('username')))

@app.route('/api/summary')
@login_required
def api_summary():
    """Get today's summary metrics"""
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            # Get today's summary from apr_daily_summary
            cursor.execute("""
                SELECT
                    COALESCE(total_proposals, 0) as total_proposals,
                    COALESCE(valid_proposals, 0) as valid_proposals,
                    COALESCE(invalid_proposals, 0) as invalid_proposals,
                    COALESCE(total_vlr_aprovados, 0) as total_value
                FROM apr_daily_summary
                WHERE DATA = CURDATE()
            """)
            summary = cursor.fetchone()

            if not summary:
                summary = {
                    'total_proposals': 0,
                    'valid_proposals': 0,
                    'invalid_proposals': 0,
                    'total_value': 0
                }

            # Get invalid counts by type from apr_invalid_records
            cursor.execute("""
                SELECT
                    VALIDATION_TYPE,
                    COUNT(*) as count
                FROM apr_invalid_records
                WHERE DATA = CURDATE() AND is_resolved = 0
                GROUP BY VALIDATION_TYPE
            """)
            invalid_by_type = cursor.fetchall()

            # Initialize counts
            summary['invalid_nfechave'] = 0
            summary['invalid_duplicata'] = 0
            summary['invalid_seuno'] = 0
            summary['invalid_cheque'] = 0

            # Fill in actual counts
            for row in invalid_by_type:
                vtype = row['VALIDATION_TYPE'].lower()
                if vtype == 'nfechave':
                    summary['invalid_nfechave'] = row['count']
                elif vtype == 'duplicata':
                    summary['invalid_duplicata'] = row['count']
                elif vtype == 'seuno':
                    summary['invalid_seuno'] = row['count']
                elif vtype == 'cheque':
                    summary['invalid_cheque'] = row['count']

            return jsonify(summary)
    finally:
        conn.close()

@app.route('/api/status_changes')
@login_required
def api_status_changes():
    """Get recent status changes"""
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute("""
                SELECT 
                    changed_at,
                    PROPOSTA,
                    CEDENTE,
                    OLD_STATUS,
                    NEW_STATUS,
                    CHANGE_SOURCE,
                    OLD_VLR_APROVADOS,
                    NEW_VLR_APROVADOS
                FROM apr_status_history
                WHERE changed_at >= DATE_SUB(NOW(), INTERVAL 2 HOUR)
                ORDER BY changed_at DESC
                LIMIT 50
            """)
            changes = cursor.fetchall()
            return jsonify(changes)
    finally:
        conn.close()

@app.route('/api/status_by_source')
@login_required
def api_status_by_source():
    """Get status changes grouped by source"""
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute("""
                SELECT 
                    CHANGE_SOURCE,
                    COUNT(*) as count
                FROM apr_status_history
                WHERE DATE(changed_at) = CURDATE()
                GROUP BY CHANGE_SOURCE
            """)
            data = cursor.fetchall()
            return jsonify(data)
    finally:
        conn.close()

@app.route('/api/trend')
@login_required
def api_trend():
    """Get 30-day trend data"""
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            # Get daily summary data
            cursor.execute("""
                SELECT
                    DATA,
                    valid_proposals,
                    invalid_proposals,
                    total_vlr_aprovados
                FROM apr_daily_summary
                WHERE DATA >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                ORDER BY DATA
            """)
            daily_data = cursor.fetchall()

            # Get invalid counts by type for each day
            cursor.execute("""
                SELECT
                    DATA,
                    VALIDATION_TYPE,
                    COUNT(*) as count
                FROM apr_invalid_records
                WHERE DATA >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                GROUP BY DATA, VALIDATION_TYPE
                ORDER BY DATA
            """)
            invalid_data = cursor.fetchall()

            # Combine the data
            trend = []
            for day in daily_data:
                day_data = {
                    'DATA': day['DATA'],
                    'valid_records': day['valid_proposals'],
                    'invalid_nfechave': 0,
                    'invalid_duplicata': 0,
                    'invalid_seuno': 0,
                    'invalid_cheque': 0,
                    'total_vlr_aprovados': day['total_vlr_aprovados']
                }

                # Add invalid counts for this day
                for inv in invalid_data:
                    if inv['DATA'] == day['DATA']:
                        vtype = inv['VALIDATION_TYPE'].lower()
                        if vtype == 'nfechave':
                            day_data['invalid_nfechave'] = inv['count']
                        elif vtype == 'duplicata':
                            day_data['invalid_duplicata'] = inv['count']
                        elif vtype == 'seuno':
                            day_data['invalid_seuno'] = inv['count']
                        elif vtype == 'cheque':
                            day_data['invalid_cheque'] = inv['count']

                trend.append(day_data)

            return jsonify(trend)
    finally:
        conn.close()

@app.route('/api/invalid_records')
@login_required
def api_invalid_records():
    """Get current invalid records"""
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute("""
                SELECT
                    DATA,
                    PROPOSTA,
                    CEDENTE,
                    VALIDATION_TYPE,
                    MOTIVO,
                    detected_at,
                    is_resolved
                FROM apr_invalid_records
                WHERE is_resolved = 0
                ORDER BY detected_at DESC
                LIMIT 100
            """)
            records = cursor.fetchall()
            return jsonify(records)
    finally:
        conn.close()

@app.route('/api/all_proposals')
@login_required
def api_all_proposals():
    """Get all proposals for today (valid and invalid)"""
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            # Get all valid proposals with product types
            # Use the most recent status from apr_status_history if available, otherwise use v.STATUS
            cursor.execute("""
                SELECT
                    v.DATA,
                    v.PROPOSTA,
                    v.CEDENTE,
                    v.RAMO,
                    v.GERENTE,
                    v.EMPRESA,
                    COALESCE(h.NEW_STATUS, v.STATUS) as STATUS,
                    v.VLR_APROVADOS,
                    v.QTD_APROVADOS,
                    v.VALOR_TITULOS,
                    v.QTD_TITULOS,
                    v.first_seen,
                    v.last_updated,
                    v.is_processado,
                    v.is_bot_processed,
                    'VALID' as VALIDATION_STATUS,
                    NULL as VALIDATION_TYPE,
                    NULL as MOTIVO,
                    GROUP_CONCAT(DISTINCT p.PRODUTO ORDER BY p.PRODUTO SEPARATOR ', ') as PRODUTOS
                FROM apr_valid_records v
                LEFT JOIN apr_proposal_products p
                    ON v.DATA = p.DATA
                    AND v.PROPOSTA = p.PROPOSTA
                    AND v.CEDENTE = p.CEDENTE
                LEFT JOIN (
                    SELECT
                        DATA,
                        PROPOSTA,
                        CEDENTE,
                        NEW_STATUS,
                        ROW_NUMBER() OVER (PARTITION BY DATA, PROPOSTA, CEDENTE ORDER BY changed_at DESC) as rn
                    FROM apr_status_history
                ) h ON v.DATA = h.DATA
                   AND v.PROPOSTA = h.PROPOSTA
                   AND v.CEDENTE = h.CEDENTE
                   AND h.rn = 1
                WHERE v.DATA = CURDATE()
                GROUP BY v.DATA, v.PROPOSTA, v.CEDENTE, v.RAMO, v.GERENTE, v.EMPRESA,
                         v.STATUS, h.NEW_STATUS,
                         v.VLR_APROVADOS, v.QTD_APROVADOS, v.VALOR_TITULOS, v.QTD_TITULOS,
                         v.first_seen, v.last_updated, v.is_processado, v.is_bot_processed
            """)
            valid_proposals = list(cursor.fetchall())

            # Get all invalid proposals with error types grouped
            # Use the most recent status from apr_status_history if available
            cursor.execute("""
                SELECT
                    i.DATA,
                    i.PROPOSTA,
                    i.CEDENTE,
                    i.RAMO,
                    i.GERENTE,
                    i.EMPRESA,
                    COALESCE(h.NEW_STATUS, i.STATUS) as STATUS,
                    0 as VLR_APROVADOS,
                    0 as QTD_APROVADOS,
                    0 as VALOR_TITULOS,
                    0 as QTD_TITULOS,
                    MIN(i.detected_at) as first_seen,
                    MAX(i.detected_at) as last_updated,
                    0 as is_processado,
                    0 as is_bot_processed,
                    'INVALID' as VALIDATION_STATUS,
                    GROUP_CONCAT(DISTINCT i.VALIDATION_TYPE ORDER BY i.VALIDATION_TYPE SEPARATOR ', ') as VALIDATION_TYPE,
                    GROUP_CONCAT(DISTINCT i.MOTIVO ORDER BY i.MOTIVO SEPARATOR ' | ') as MOTIVO,
                    GROUP_CONCAT(DISTINCT i.PRODUTO ORDER BY i.PRODUTO SEPARATOR ', ') as PRODUTOS
                FROM apr_invalid_records i
                LEFT JOIN (
                    SELECT
                        DATA,
                        PROPOSTA,
                        CEDENTE,
                        NEW_STATUS,
                        ROW_NUMBER() OVER (PARTITION BY DATA, PROPOSTA, CEDENTE ORDER BY changed_at DESC) as rn
                    FROM apr_status_history
                ) h ON i.DATA = h.DATA
                   AND i.PROPOSTA = h.PROPOSTA
                   AND i.CEDENTE = h.CEDENTE
                   AND h.rn = 1
                WHERE i.DATA = CURDATE() AND i.is_resolved = 0
                GROUP BY i.DATA, i.PROPOSTA, i.CEDENTE, i.RAMO, i.GERENTE, i.EMPRESA, i.STATUS, h.NEW_STATUS
            """)
            invalid_proposals = list(cursor.fetchall())

            # Combine both lists
            all_proposals = valid_proposals + invalid_proposals

            # Sort by proposal number
            all_proposals.sort(key=lambda x: x['PROPOSTA'])

            return jsonify(all_proposals)
    finally:
        conn.close()

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)

