#!/usr/bin/env python3
"""
Business Days Helper - Utility functions for working with dias_uteis table.

This module provides helper functions to:
- Check if a date is a business day
- Get next/previous business day
- Calculate business days between dates
- Get upcoming holidays
"""

import pymysql
import json
from datetime import date, timedelta
from pathlib import Path
from typing import Optional, List, Dict, Any


def load_database_config():
    """Load MariaDB configuration from databases_config.json."""
    config_path = Path(__file__).parent / 'databases_config.json'
    
    with open(config_path, 'r') as f:
        config = json.load(f)
    
    return config['databases']['mariadb']


def get_connection():
    """Get MariaDB connection."""
    db_config = load_database_config()
    
    return pymysql.connect(
        host=db_config['server'],
        port=db_config['port'],
        user=db_config['user'],
        password=db_config['password'],
        database=db_config['scheme'],
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )


def is_business_day(target_date: date) -> bool:
    """
    Check if a date is a business day.
    
    Args:
        target_date: Date to check
    
    Returns:
        True if business day, False otherwise
    """
    conn = get_connection()
    cursor = conn.cursor()
    
    try:
        cursor.execute(
            'SELECT eh_dia_util FROM dias_uteis WHERE data = %s',
            (target_date,)
        )
        result = cursor.fetchone()
        
        if result:
            return bool(result['eh_dia_util'])
        else:
            # If date not in table, assume it's a business day (fallback)
            return target_date.weekday() < 5
    finally:
        conn.close()


def get_next_business_day(from_date: date = None) -> Optional[date]:
    """
    Get the next business day from a given date.
    
    Args:
        from_date: Starting date (default: today)
    
    Returns:
        Next business day, or None if not found
    """
    if from_date is None:
        from_date = date.today()
    
    conn = get_connection()
    cursor = conn.cursor()
    
    try:
        cursor.execute('''
            SELECT data FROM dias_uteis 
            WHERE data > %s AND eh_dia_util = TRUE 
            ORDER BY data 
            LIMIT 1
        ''', (from_date,))
        
        result = cursor.fetchone()
        return result['data'] if result else None
    finally:
        conn.close()


def get_previous_business_day(from_date: date = None) -> Optional[date]:
    """
    Get the previous business day from a given date.
    
    Args:
        from_date: Starting date (default: today)
    
    Returns:
        Previous business day, or None if not found
    """
    if from_date is None:
        from_date = date.today()
    
    conn = get_connection()
    cursor = conn.cursor()
    
    try:
        cursor.execute('''
            SELECT data FROM dias_uteis 
            WHERE data < %s AND eh_dia_util = TRUE 
            ORDER BY data DESC
            LIMIT 1
        ''', (from_date,))
        
        result = cursor.fetchone()
        return result['data'] if result else None
    finally:
        conn.close()


def is_holiday(target_date: date) -> tuple[bool, Optional[str]]:
    """
    Check if a date is a holiday.
    
    Args:
        target_date: Date to check
    
    Returns:
        Tuple of (is_holiday, holiday_name)
    """
    conn = get_connection()
    cursor = conn.cursor()
    
    try:
        cursor.execute(
            'SELECT eh_feriado, nome_feriado FROM dias_uteis WHERE data = %s',
            (target_date,)
        )
        result = cursor.fetchone()
        
        if result:
            return (bool(result['eh_feriado']), result['nome_feriado'])
        else:
            return (False, None)
    finally:
        conn.close()


def get_upcoming_holidays(from_date: date = None, limit: int = 10) -> List[Dict[str, Any]]:
    """
    Get upcoming holidays from a given date.
    
    Args:
        from_date: Starting date (default: today)
        limit: Maximum number of holidays to return
    
    Returns:
        List of dictionaries with holiday information
    """
    if from_date is None:
        from_date = date.today()
    
    conn = get_connection()
    cursor = conn.cursor()
    
    try:
        cursor.execute('''
            SELECT data, dia_semana, nome_feriado 
            FROM dias_uteis 
            WHERE data >= %s AND eh_feriado = TRUE 
            ORDER BY data 
            LIMIT %s
        ''', (from_date, limit))
        
        return cursor.fetchall()
    finally:
        conn.close()

