#!/usr/bin/env python3
"""
Test the verification digit calculation.
"""

def calculate_verification_digit(seuno):
    """
    Calculate the verification digit for a SEUNO.

    Algorithm:
    1. Form seuno_vector: [1, 9] + first 11 digits of SEUNO
    2. factor_vector: [2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2]
    3. dot_product = sum(seuno_vector[i] * factor_vector[i])
    4. quotient = dot_product // 11
    5. pre_last_remainder = dot_product - (quotient * 11)  [modulo/remainder]
    6. Calculate verification_digit:
       - if (11 - pre_last_remainder) == 11: verification_digit = 0
       - if (11 - pre_last_remainder) == 10: verification_digit = 'P'
       - else: verification_digit = 11 - pre_last_remainder
    """
    if not seuno or len(seuno) < 11:
        return ''
    
    # Extract first 11 digits from SEUNO (ignore non-digit characters)
    seuno_digits = [int(c) for c in seuno if c.isdigit()][:11]
    
    if len(seuno_digits) < 11:
        return ''
    
    # Form seuno_vector: [1, 9] + first 11 digits
    seuno_vector = [1, 9] + seuno_digits
    
    # Fixed factor_vector
    factor_vector = [2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2]
    
    print(f"\nSEUNO: {seuno}")
    print(f"First 11 digits: {seuno_digits}")
    print(f"seuno_vector: {seuno_vector}")
    print(f"factor_vector: {factor_vector}")
    
    # Calculate dot product
    dot_product = sum(s * f for s, f in zip(seuno_vector, factor_vector))
    print(f"dot_product: {dot_product}")
    
    # Calculate quotient (integer division)
    quotient = dot_product // 11
    print(f"quotient (dot_product // 11): {quotient}")
    
    # Calculate pre_last_remainder (modulo)
    pre_last_remainder = dot_product - (quotient * 11)
    print(f"pre_last_remainder (dot_product - quotient * 11): {pre_last_remainder}")

    # Calculate result
    result = 11 - pre_last_remainder
    print(f"result (11 - pre_last_remainder): {result}")

    # Calculate verification_digit based on rules
    if result == 11:
        verification_digit = '0'
        print(f"Rule: 11 - pre_last_remainder = {result} == 11 -> verification_digit = 0")
    elif result == 10:
        verification_digit = 'P'
        print(f"Rule: 11 - pre_last_remainder = {result} == 10 -> verification_digit = P")
    else:
        verification_digit = str(result)
        print(f"Rule: 11 - pre_last_remainder = {result} -> verification_digit = {verification_digit}")

    print(f"VERIFICATION_DIGIT: {verification_digit}")
    
    return verification_digit


if __name__ == "__main__":
    # Test with the example from the user
    test_seuno = "518300121527"
    print("=" * 80)
    print("Testing verification digit calculation")
    print("=" * 80)
    
    result = calculate_verification_digit(test_seuno)
    
    print("\n" + "=" * 80)
    print(f"Final result for SEUNO {test_seuno}: {result}")
    print("=" * 80)

