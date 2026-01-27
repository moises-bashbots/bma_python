#!/usr/bin/env python3
"""
Demo script showing how repurchase alerts would work (without actual Slack API calls).
This demonstrates the dry-run functionality.
"""


def demo_alerts():
    """Demonstrate the alert system."""
    
    print("=" * 120)
    print("REPURCHASE ALERT SYSTEM - DRY RUN DEMONSTRATION")
    print("=" * 120)
    print()
    
    # Sample matched records (from actual cross-reference)
    matched_records = [
        {
            'apr_cedente': 'METALURGICA REUTER',
            'apr_numero': 2,
            'apr_gerente': 'SUIENE',
            'apr_vlr_aprovados': 98032.47,
            'excel_operador': 'Leticia',
            'excel_tipo_recompra': 'Operação',
            'excel_prazo_recompra': '10 dias',
        },
        {
            'apr_cedente': 'BRAUNAS',
            'apr_numero': 3,
            'apr_gerente': 'CARTEIRA D',
            'apr_vlr_aprovados': 23711.12,
            'excel_operador': 'Bárbara',
            'excel_tipo_recompra': 'Operação',
            'excel_prazo_recompra': '45 dias',
        },
        {
            'apr_cedente': 'MARBETH',
            'apr_numero': 4,
            'apr_gerente': 'CARTEIRA D',
            'apr_vlr_aprovados': 107316.83,
            'excel_operador': 'Bárbara',
            'excel_tipo_recompra': 'Operação',
            'excel_prazo_recompra': '45 dias',
        },
        {
            'apr_cedente': 'FRIMAZZ CARNES',
            'apr_numero': 5,
            'apr_gerente': 'GIZELLE BR',
            'apr_vlr_aprovados': 1012422.80,
            'excel_operador': 'Franciane',
            'excel_tipo_recompra': 'Operação',
            'excel_prazo_recompra': '10 dias',
        },
        {
            'apr_cedente': 'NONAPACK INDUSTRIA',
            'apr_numero': 8,
            'apr_gerente': 'AMANDA',
            'apr_vlr_aprovados': 0.00,
            'excel_operador': 'Leticia',
            'excel_tipo_recompra': 'Operação',
            'excel_prazo_recompra': '10 dias',
        },
    ]
    
    # Simulated Slack user mapping
    operator_to_slack_user = {
        'Leticia': {'id': 'U12345ABC', 'name': 'leticia.silva', 'real_name': 'Letícia Silva'},
        'Bárbara': {'id': 'U67890DEF', 'name': 'barbara.costa', 'real_name': 'Bárbara Costa'},
        'Franciane': {'id': 'UABCDE123', 'name': 'franciane.souza', 'real_name': 'Franciane Souza'},
        'Ranielly': {'id': 'UFGHIJ456', 'name': 'ranielly.lima', 'real_name': 'Ranielly Lima'},
        'Thais': {'id': 'UKLMNO789', 'name': 'thais.oliveira', 'real_name': 'Thaís Oliveira'},
    }
    
    print("CONFIGURATION")
    print("-" * 120)
    print(f"Target Channel: #operacionalxcobranca")
    print(f"Total Records: {len(matched_records)}")
    print(f"Mode: DRY RUN (no messages will be sent)")
    print()
    
    print("=" * 120)
    print("PROCESSING ALERTS")
    print("=" * 120)
    print()
    
    stats = {
        'total': len(matched_records),
        'matched': 0,
        'not_matched': 0,
    }
    
    for i, record in enumerate(matched_records, 1):
        operator_name = record['excel_operador']
        cedente_apelido = record['apr_cedente']
        numero = record['apr_numero']
        tipo_recompra = record['excel_tipo_recompra']

        # Convert tipo_recompra to uppercase with accents preserved
        tipo_recompra_upper = tipo_recompra.upper()

        print(f"{i}. Cedente: {cedente_apelido} | Borderô: {numero} | Operador: {operator_name} | Tipo: {tipo_recompra}")

        # Check if operator matches a Slack user
        slack_user = operator_to_slack_user.get(operator_name)

        if slack_user:
            stats['matched'] += 1
            user_id = slack_user['id']
            real_name = slack_user['real_name']

            # Format message (Slack format: <@USER_ID>)
            # Message format: @{operator} {cedente}, borderô {numero}, {TIPO_RECOMPRA}
            message = f"<@{user_id}> {cedente_apelido}, borderô {numero}, {tipo_recompra_upper}"

            print(f"   ✓ Matched operator to Slack user: {real_name} ({user_id})")
            print(f"   📱 Message: {message}")
            print(f"   [DRY RUN] Would send to channel #operacionalxcobranca")
        else:
            stats['not_matched'] += 1
            print(f"   ⚠ Could not match operator '{operator_name}' to Slack user")
            print(f"   [DRY RUN] Would skip this alert")

        print()
    
    print("=" * 120)
    print("ALERT SUMMARY")
    print("=" * 120)
    print(f"Total records: {stats['total']}")
    print(f"Operators matched: {stats['matched']}")
    print(f"Operators not matched: {stats['not_matched']}")
    print()
    
    print("=" * 120)
    print("HOW IT WORKS")
    print("=" * 120)
    print()
    print("1. The system reads the Excel file with repurchase data")
    print("2. Queries APR_CAPA database for current date proposals")
    print("3. Cross-references Excel 'Cedente' with APR_CAPA 'CEDENTE' (cedente.apelido)")
    print("4. For each match:")
    print("   - Looks up the operator name in Slack channel members")
    print("   - Creates message: @{operator} {cedente}, borderô {numero}, {TIPO_RECOMPRA}")
    print("   - Sends to #operacionalxcobranca channel")
    print()
    print("5. In Slack, the operator will see:")
    print("   '@Letícia Silva METALURGICA REUTER, borderô 2, OPERAÇÃO'")
    print("   (with a notification because they were mentioned)")
    print()
    print("=" * 120)
    print()
    print("To run with ACTUAL Slack sending:")
    print("  python3 repurchase_report.py --send")
    print()
    print("To run in DRY RUN mode (default):")
    print("  python3 repurchase_report.py")
    print()


if __name__ == "__main__":
    demo_alerts()

