#!/usr/bin/env python3
"""
Simple test script to verify we can identify "Altera" and "Grava" buttons.
Uses the exact same code as send_rating_vadu.py but in test mode.
"""

import time
from playwright.sync_api import sync_playwright

# Import the working functions from send_rating_vadu.py
from send_rating_vadu import (
    login_to_ger,
    navigate_to_proposta,
    click_proposal_spectacles
)

GER_URL = "https://gercloud2.rgbsys.com.br/ger_bma/"
USERNAME = "MOISES"
PASSWORD = "Mo02092022"

def main():
    print("=" * 80)
    print("TEST: Identify Altera and Grava buttons")
    print("=" * 80)
    
    with sync_playwright() as p:
        # Launch browser
        print("\n🌐 Launching browser...")
        browser = p.chromium.launch(headless=False)
        context = browser.new_context(viewport={'width': 1443, 'height': 1705})
        page = context.new_page()
        print("✓ Browser launched")
        
        try:
            # Login
            print("\nLogging in...")
            page.goto(GER_URL, wait_until="networkidle")
            time.sleep(2)
            page.fill("#edusuario_I", USERNAME)
            time.sleep(1)
            page.fill("#edsenha_I", PASSWORD)
            time.sleep(1)
            page.click("#LoginButton")
            time.sleep(2)
            page.wait_for_load_state("networkidle")
            print("✓ Logged in")
            time.sleep(2)
            
            # Navigate to Proposta
            print("\nNavigating to Proposta...")
            page.click("#menu > li > a:has-text('CRÉDITO')")
            time.sleep(2)
            page.click("#liproposta > a")
            time.sleep(2)
            page.wait_for_load_state("networkidle")
            print("✓ On Proposta page")
            time.sleep(2)
            
            # Find proposal rows
            print("\nLooking for proposals...")
            rows = page.query_selector_all("tr[id*='DXDataRow']")
            print(f"✓ Found {len(rows)} proposals")

            if len(rows) == 0:
                print("❌ No proposals found!")
                return

            # Get the first proposal number from the first row
            print("\nGetting first proposal number...")
            first_row = rows[0]
            cells = first_row.query_selector_all("td")

            # The proposal number is typically in the 3rd column (index 2)
            # Format: DATA, CEDENTE, PROPOSTA, ...
            proposal_number = None
            for i, cell in enumerate(cells):
                text = cell.text_content().strip()
                if text.isdigit():
                    proposal_number = text
                    print(f"✓ Found proposal number: {proposal_number} in column {i}")
                    break

            if not proposal_number:
                print("❌ Could not find proposal number!")
                return

            # Use the working function from send_rating_vadu.py to click spectacles button
            print(f"\nOpening proposal details using click_proposal_spectacles()...")
            success = click_proposal_spectacles(page, proposal_number, pause_seconds=2)

            if not success:
                print("❌ Failed to open proposal details!")
                return

            print("✓ Opened proposal details")
            time.sleep(2)
            
            # Take screenshot
            page.screenshot(path="test_proposal_details.png")
            print("✓ Screenshot saved: test_proposal_details.png")

            # Save HTML for debugging
            html_content = page.content()
            with open("test_proposal_details.html", "w", encoding="utf-8") as f:
                f.write(html_content)
            print("✓ HTML saved: test_proposal_details.html")

            # Look for "Altera" button (DO NOT click Processar first)
            # The Altera button is a div with id="ctl00_contentManager_BtAlteraFluxo_CD"
            print("\nLooking for Altera button...")
            altera_selector = "div#ctl00_contentManager_BtAlteraFluxo_CD"

            altera_found = False
            try:
                if page.is_visible(altera_selector, timeout=2000):
                    print(f"  ✓ Found Altera button with selector: {altera_selector}")
                    altera_found = True
                else:
                    print(f"  ⚠ Altera button exists but not visible")
            except Exception as e:
                print(f"  ❌ Altera button NOT found!")
                print(f"  Error: {e}")

            if altera_found:
                # Click Altera button
                print(f"\nClicking Altera button...")
                page.click(altera_selector)
                print("✓ Clicked Altera")

                # Wait for the page to update after clicking Altera
                print("  Waiting 2 seconds for page to update...")
                time.sleep(2)

                # Take screenshot after clicking Altera
                page.screenshot(path="test_after_altera.png")
                print("✓ Screenshot after Altera: test_after_altera.png")

            # Look for "Grava" button (without clicking)
            # The Grava button is a div with id="ctl00_contentManager_BtGravaFluxo_CD"
            # It should appear after clicking Altera and waiting 2 seconds
            print("\nLooking for Grava button (without clicking)...")
            grava_selector = "div#ctl00_contentManager_BtGravaFluxo_CD"

            grava_found = False
            try:
                if page.is_visible(grava_selector, timeout=2000):
                    print(f"  ✓ Found Grava button with selector: {grava_selector}")
                    grava_found = True
                    print("  ✓ Grava button is visible (NOT clicking it)")
                else:
                    print(f"  ⚠ Grava button exists but not visible")
            except Exception as e:
                print(f"  ❌ Grava button NOT found!")
                print(f"  Error: {e}")
                if altera_found:
                    print("  Note: Grava should appear after clicking Altera")

            # Summary
            print("\n" + "=" * 80)
            print("SUMMARY:")
            print(f"  Altera button: {'✓ FOUND and CLICKED' if altera_found else '❌ NOT FOUND'}")
            print(f"  Grava button: {'✓ FOUND (not clicked)' if grava_found else '❌ NOT FOUND'}")
            if altera_found:
                print(f"  Altera selector: {altera_selector}")
            if grava_found:
                print(f"  Grava selector: {grava_selector}")
            print("=" * 80)

            # Wait for manual inspection
            print("\n" + "=" * 80)
            print("BROWSER IS OPEN FOR MANUAL INSPECTION")
            print("Please inspect the page and identify the Altera and Grava buttons")
            print("Press Ctrl+C in the terminal when you're done to close the browser")
            print("=" * 80)

            # Wait indefinitely until user presses Ctrl+C
            try:
                while True:
                    time.sleep(1)
            except KeyboardInterrupt:
                print("\n\n✓ User interrupted - closing browser...")
            
        except Exception as e:
            print(f"\n❌ Error: {e}")
            import traceback
            traceback.print_exc()
            time.sleep(10)
        finally:
            browser.close()
            print("\n✓ Browser closed")

if __name__ == "__main__":
    main()

