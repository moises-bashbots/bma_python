#!/usr/bin/env python3
"""
Test script for status filter functionality.
"""

import sys
from pathlib import Path


# Define the status progression order (copied from query_apr_invalidos_status.py)
STATUS_PROGRESSION = [
    "Aguardando Analista",
    "Aguardando análise",
    "Em análise",
    "Aguardando Aprovação",
    "Aprovado",
    "Enviado para Assinar",
    "Assinado",
    "Liberado",
    "Finalizado"
]


def is_status_equal_or_further(status: str, reference_status: str = "Aguardando Analista") -> bool:
    """
    Check if a status is equal to or further than a reference status in the workflow.
    (Copied from query_apr_invalidos_status.py for testing)
    """
    if not status:
        return False

    # Normalize status strings (case-insensitive, strip whitespace)
    status_normalized = status.strip()
    reference_normalized = reference_status.strip()

    # Try to find the status in the progression list (case-insensitive)
    status_index = None
    reference_index = None

    for i, prog_status in enumerate(STATUS_PROGRESSION):
        if prog_status.lower() == status_normalized.lower():
            status_index = i
        if prog_status.lower() == reference_normalized.lower():
            reference_index = i

    # If we found both statuses in the progression, compare their positions
    if status_index is not None and reference_index is not None:
        return status_index >= reference_index

    # If status not found in progression but reference is "Aguardando Analista",
    # check for partial match (for variations like "Aguardando análise")
    if reference_normalized.lower() == "aguardando analista":
        # Accept any status that contains "aguardando" or is further in the workflow
        if "aguardando" in status_normalized.lower():
            return True
        # If status is not in progression and doesn't contain "aguardando", assume it's earlier
        if status_index is None:
            return False

    # Default: if we can't determine, return False (conservative approach)
    return False


def test_status_filter():
    """Test the status filter function."""
    print("=" * 80)
    print("TESTING STATUS FILTER FUNCTION")
    print("=" * 80)
    print()
    
    print("Status Progression Order:")
    for i, status in enumerate(STATUS_PROGRESSION, 1):
        print(f"  {i}. {status}")
    print()
    
    # Test cases
    test_cases = [
        # (status, reference, expected_result, description)
        ("Aguardando Analista", "Aguardando Analista", True, "Exact match"),
        ("Aguardando análise", "Aguardando Analista", True, "Variation of Aguardando"),
        ("Em análise", "Aguardando Analista", True, "Further in progression"),
        ("Aprovado", "Aguardando Analista", True, "Much further in progression"),
        ("Enviado para Assinar", "Aguardando Analista", True, "Near end of progression"),
        ("Finalizado", "Aguardando Analista", True, "End of progression"),
        ("", "Aguardando Analista", False, "Empty status"),
        (None, "Aguardando Analista", False, "None status"),
        ("Rascunho", "Aguardando Analista", False, "Status before Aguardando Analista"),
        ("Pendente", "Aguardando Analista", False, "Status not in progression"),
        ("AGUARDANDO ANALISTA", "Aguardando Analista", True, "Case insensitive"),
        ("  Aguardando Analista  ", "Aguardando Analista", True, "With whitespace"),
    ]
    
    print("Test Cases:")
    print("-" * 80)
    passed = 0
    failed = 0
    
    for status, reference, expected, description in test_cases:
        result = is_status_equal_or_further(status, reference)
        status_display = f"'{status}'" if status else "None/Empty"
        
        if result == expected:
            status_icon = "✓"
            passed += 1
        else:
            status_icon = "✗"
            failed += 1
        
        print(f"{status_icon} {description}")
        print(f"  Status: {status_display}")
        print(f"  Expected: {expected}, Got: {result}")
        print()
    
    print("-" * 80)
    print(f"Results: {passed} passed, {failed} failed")
    print("=" * 80)
    
    return failed == 0


if __name__ == "__main__":
    success = test_status_filter()
    sys.exit(0 if success else 1)

