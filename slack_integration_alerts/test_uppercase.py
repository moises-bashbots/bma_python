#!/usr/bin/env python3
"""Quick test to verify uppercase with accents."""

# Test data
tipo_recompra = "Operação"

# Convert to uppercase
tipo_upper = tipo_recompra.upper()

print(f"Original: {tipo_recompra}")
print(f"Uppercase: {tipo_upper}")
print()

# Example message
user_id = "U12345ABC"
cedente = "METALURGICA REUTER"
numero = 2

message = f"<@{user_id}> {cedente}, borderô {numero}, {tipo_upper}"
print(f"Message: {message}")
print()
print("Expected: <@U12345ABC> METALURGICA REUTER, borderô 2, OPERAÇÃO")

