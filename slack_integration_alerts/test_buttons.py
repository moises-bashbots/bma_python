#!/usr/bin/env python3
"""
Test script to verify we can identify "Altera" and "Grava" buttons
in the proposal details page.
"""

import time
from playwright.sync_api import sync_playwright

# Import the working functions from send_rating_vadu.py
import sys
sys.path.insert(0, '/home/robot/Dev/bma_python/slack_integration_alerts')
from send_rating_vadu import login_to_ger, navigate_to_proposta

def test_buttons():
    """Test identifying Altera and Grava buttons in proposal details."""

    print("=" * 80)
    print("TEST: Identify Altera and Grava buttons")
    print("=" * 80)

    with sync_playwright() as p:
        # Launch browser (same settings as send_rating_vadu.py)
        print("\n🌐 Launching browser...")
        browser = p.chromium.launch(headless=False)
        context = browser.new_context(
            viewport={'width': 1443, 'height': 1705}
        )
        page = context.new_page()
        print("✓ Browser launched successfully!")

        try:
            # Use the working login function
            login_to_ger(page, pause_seconds=2)

            # Use the working navigation function
            navigate_to_proposta(page, pause_seconds=2)

            # Take screenshot of proposals page
            page.screenshot(path="test_proposals_page.png")
            print("✓ Screenshot of proposals page saved")

            # Find the first proposal row and click it
            print("\nLooking for proposal rows...")
            rows = page.query_selector_all("tr[id*='DXDataRow']")
            print(f"✓ Found {len(rows)} proposal rows")

            if len(rows) == 0:
                print("❌ No proposal rows found!")
                return

            # Click the first row to open details
            print("\nClicking first proposal row...")
            first_row = rows[0]
            first_row.click()
            print("✓ Clicked CRÉDITO menu")
            time.sleep(2)

            page.click("#liproposta > a")
            print("✓ Clicked Proposta submenu")
            time.sleep(2)

            page.wait_for_load_state("networkidle")
            print("✓ Navigated to Proposta page")
            time.sleep(2)
            
            # Find and click on the first proposal (Proposta 2 - MADEGEM)
            print("\nLooking for proposals in the table...")
            
            # Get the first data row
            first_row = page.query_selector("table tbody tr[id*='DXDataRow']")
            
            if not first_row:
                print("❌ No proposal rows found!")
                return
            
            # Get proposal number from the row
            cells = first_row.query_selector_all("td")
            if len(cells) > 3:
                proposta_num = cells[3].text_content().strip()
                cedente = cells[1].text_content().strip()
                print(f"✓ Found Proposta {proposta_num} - {cedente}")
            else:
                print("❌ Could not extract proposal info")
                return
            
            # Click on the row to open details
            print(f"\nClicking on Proposta {proposta_num} to open details...")
            first_row.click()
            time.sleep(3)  # Wait for details page to load
            
            print("✓ Proposal details page opened")
            
            # Take a screenshot
            page.screenshot(path="test_buttons_screenshot.png")
            print("✓ Screenshot saved to test_buttons_screenshot.png")
            
            # Try to find "Altera" button
            print("\n" + "=" * 80)
            print("TESTING: Looking for 'Altera' button...")
            print("=" * 80)
            
            altera_selectors = [
                "input[value='Altera']",
                "button:has-text('Altera')",
                "input[type='submit'][value='Altera']",
                "input[type='button'][value='Altera']",
                "[id*='Altera']",
                "[name*='Altera']",
            ]
            
            altera_found = False
            for selector in altera_selectors:
                try:
                    element = page.query_selector(selector)
                    if element:
                        print(f"✓ Found 'Altera' button with selector: {selector}")
                        print(f"  - Tag: {element.evaluate('el => el.tagName')}")
                        print(f"  - Type: {element.get_attribute('type')}")
                        print(f"  - Value: {element.get_attribute('value')}")
                        print(f"  - ID: {element.get_attribute('id')}")
                        print(f"  - Name: {element.get_attribute('name')}")
                        print(f"  - Visible: {element.is_visible()}")
                        altera_found = True
                        
                        # Try to click it
                        print(f"\n  Attempting to click 'Altera' button...")
                        try:
                            element.click(timeout=5000)
                            print(f"  ✓ Successfully clicked 'Altera' button!")
                            time.sleep(2)  # Wait for any changes
                        except Exception as e:
                            print(f"  ⚠ Click failed: {e}")
                            print(f"  Trying JavaScript click...")
                            try:
                                page.evaluate(f"document.querySelector('{selector}').click()")
                                print(f"  ✓ JavaScript click successful!")
                                time.sleep(2)
                            except Exception as e2:
                                print(f"  ❌ JavaScript click also failed: {e2}")
                        
                        break
                except Exception as e:
                    continue
            
            if not altera_found:
                print("❌ 'Altera' button not found with any selector")
                print("\nLet me search for all input elements...")
                all_inputs = page.query_selector_all("input")
                print(f"Found {len(all_inputs)} input elements:")
                for i, inp in enumerate(all_inputs[:20]):  # Show first 20
                    inp_type = inp.get_attribute('type')
                    inp_value = inp.get_attribute('value')
                    inp_id = inp.get_attribute('id')
                    if inp_value or inp_id:
                        print(f"  {i+1}. type={inp_type}, value={inp_value}, id={inp_id}")
            
            # Try to find "Grava" button
            print("\n" + "=" * 80)
            print("TESTING: Looking for 'Grava' button...")
            print("=" * 80)
            
            grava_selectors = [
                "input[value='Grava']",
                "button:has-text('Grava')",
                "input[type='submit'][value='Grava']",
                "input[type='button'][value='Grava']",
                "[id*='Grava']",
                "[name*='Grava']",
            ]
            
            grava_found = False
            for selector in grava_selectors:
                try:
                    element = page.query_selector(selector)
                    if element:
                        print(f"✓ Found 'Grava' button with selector: {selector}")
                        print(f"  - Tag: {element.evaluate('el => el.tagName')}")
                        print(f"  - Type: {element.get_attribute('type')}")
                        print(f"  - Value: {element.get_attribute('value')}")
                        print(f"  - ID: {element.get_attribute('id')}")
                        print(f"  - Name: {element.get_attribute('name')}")
                        print(f"  - Visible: {element.is_visible()}")
                        grava_found = True
                        print(f"\n  ℹ️  NOT clicking 'Grava' button (test only)")
                        break
                except Exception as e:
                    continue
            
            if not grava_found:
                print("❌ 'Grava' button not found with any selector")
            
            # Wait before closing
            print("\n" + "=" * 80)
            print("Test complete! Waiting 10 seconds before closing...")
            print("=" * 80)
            time.sleep(10)
            
        except Exception as e:
            print(f"\n❌ Error during test: {e}")
            import traceback
            traceback.print_exc()
        
        finally:
            browser.close()
            print("\n✓ Browser closed")

if __name__ == "__main__":
    test_buttons()

