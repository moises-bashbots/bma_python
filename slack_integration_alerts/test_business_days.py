#!/usr/bin/env python3
"""
Test business days logic for repurchase_report.py
"""

import sys
from pathlib import Path
from datetime import date, timedelta
import pymysql
import json

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent))

from repurchase_report import (
    is_business_day,
    get_previous_business_day,
    query_apr_capa_for_date
)


def test_business_day_scenarios():
    """Test various business day scenarios."""
    
    print("=" * 100)
    print("BUSINESS DAY LOGIC TESTS")
    print("=" * 100)
    print()
    
    # Test scenarios
    test_dates = [
        (date(2026, 1, 20), "Tuesday - Regular business day"),
        (date(2026, 1, 24), "Friday - Last business day of week"),
        (date(2026, 1, 25), "Saturday - Weekend"),
        (date(2026, 1, 26), "Sunday - Weekend"),
        (date(2026, 1, 27), "Monday - After weekend"),
        (date(2026, 2, 16), "Monday - Carnaval holiday"),
        (date(2026, 2, 17), "Tuesday - Carnaval holiday"),
        (date(2026, 2, 18), "Wednesday - After Carnaval"),
    ]
    
    for test_date, description in test_dates:
        print(f"Test: {test_date} - {description}")
        print("-" * 100)
        
        # Check if business day
        is_biz = is_business_day(test_date)
        print(f"  Is business day: {is_biz}")
        
        if is_biz:
            # Get previous business day
            prev_biz = get_previous_business_day(test_date)
            print(f"  Previous business day: {prev_biz}")
            
            # Calculate days between
            if prev_biz:
                days_diff = (test_date - prev_biz).days
                print(f"  Days since previous business day: {days_diff}")
        else:
            print(f"  ⚠ NOT a business day - query would be skipped")
        
        print()
    
    print("=" * 100)


def test_query_on_different_days():
    """Test query behavior on different days."""
    
    print()
    print("=" * 100)
    print("QUERY BEHAVIOR TESTS")
    print("=" * 100)
    print()
    
    # Test on business day
    print("1. Query on Tuesday (business day):")
    print("-" * 100)
    tuesday = date(2026, 1, 20)
    results = query_apr_capa_for_date(tuesday)
    print(f"  Results: {len(results)} records")
    print()
    
    # Test on Saturday (weekend)
    print("2. Query on Saturday (weekend):")
    print("-" * 100)
    saturday = date(2026, 1, 25)
    results = query_apr_capa_for_date(saturday)
    print(f"  Results: {len(results)} records")
    print()
    
    # Test on Monday after weekend
    print("3. Query on Monday (after weekend):")
    print("-" * 100)
    monday = date(2026, 1, 27)
    results = query_apr_capa_for_date(monday)
    print(f"  Results: {len(results)} records")
    print()
    
    print("=" * 100)


def test_carnaval_scenario():
    """Test Carnaval holiday scenario."""
    
    print()
    print("=" * 100)
    print("CARNAVAL HOLIDAY SCENARIO")
    print("=" * 100)
    print()
    
    # Carnaval 2026: Feb 16-17 (Monday-Tuesday)
    # Friday Feb 13 is last business day before Carnaval
    # Wednesday Feb 18 is first business day after Carnaval
    
    print("Carnaval 2026: Monday Feb 16 - Tuesday Feb 17")
    print()
    
    # Friday before Carnaval
    print("1. Friday Feb 13 (last business day before Carnaval):")
    print("-" * 100)
    friday = date(2026, 2, 13)
    is_biz = is_business_day(friday)
    prev_biz = get_previous_business_day(friday)
    print(f"  Is business day: {is_biz}")
    print(f"  Previous business day: {prev_biz}")
    print()
    
    # Monday Carnaval
    print("2. Monday Feb 16 (Carnaval - holiday):")
    print("-" * 100)
    monday = date(2026, 2, 16)
    is_biz = is_business_day(monday)
    print(f"  Is business day: {is_biz}")
    if not is_biz:
        print(f"  ⚠ Query would be SKIPPED")
    print()
    
    # Tuesday Carnaval
    print("3. Tuesday Feb 17 (Carnaval - holiday):")
    print("-" * 100)
    tuesday = date(2026, 2, 17)
    is_biz = is_business_day(tuesday)
    print(f"  Is business day: {is_biz}")
    if not is_biz:
        print(f"  ⚠ Query would be SKIPPED")
    print()
    
    # Wednesday after Carnaval
    print("4. Wednesday Feb 18 (first business day after Carnaval):")
    print("-" * 100)
    wednesday = date(2026, 2, 18)
    is_biz = is_business_day(wednesday)
    prev_biz = get_previous_business_day(wednesday)
    print(f"  Is business day: {is_biz}")
    print(f"  Previous business day: {prev_biz}")
    if prev_biz:
        days_diff = (wednesday - prev_biz).days
        print(f"  Days since previous business day: {days_diff}")
        print(f"  ✓ Query would fetch records from {prev_biz} 16:15 onwards")
    print()
    
    print("=" * 100)


if __name__ == '__main__':
    test_business_day_scenarios()
    test_carnaval_scenario()
    
    # Only run query tests if explicitly requested
    if '--test-queries' in sys.argv:
        test_query_on_different_days()

