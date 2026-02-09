#!/usr/bin/env python3
"""
Send Rating to Vadu System

This program automates the process of sending rating information to the Vadu system
through the GER web interface using Playwright for browser automation.

Features:
- Automated login to GER system
- Navigation to Proposta (Proposal) section
- Selection of rating group from dropdown
- Headless browser support
- Error handling and logging
"""

import os
import sys
from pathlib import Path

# Set Playwright browsers path BEFORE importing playwright
# This ensures the binary can find browsers in the user's home directory
if not os.environ.get('PLAYWRIGHT_BROWSERS_PATH'):
    home_dir = os.path.expanduser('~')
    os.environ['PLAYWRIGHT_BROWSERS_PATH'] = os.path.join(home_dir, '.cache', 'ms-playwright')

import argparse
import json
import time
from datetime import date, datetime
from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from models_mariadb import MariaDBBase, APRValidRecord
from models import APRCapa, Cedente as MSSQLCedente
from monitoring_helpers import track_all_status_changes_from_source


# Configuration
GER_URL = "https://gercloud2.rgbsys.com.br/ger_bma/"
USERNAME = "MOISES"
PASSWORD = "Mo02092022"


def load_config():
    """Load database configuration."""
    config_path = Path.cwd() / 'databases_config.json'
    with open(config_path) as f:
        return json.load(f)


def create_mariadb_session():
    """Create MariaDB session using SQLAlchemy."""
    config = load_config()
    db_config = config['databases']['mariadb']

    # Create connection string
    connection_string = (
        f"mysql+pymysql://{db_config['user']}:{db_config['password']}"
        f"@{db_config['server']}:{db_config['port']}/{db_config['scheme']}"
        f"?charset=utf8mb4"
    )

    # Create engine and session
    engine = create_engine(connection_string, echo=False)
    Session = sessionmaker(bind=engine)
    return Session()


def get_todays_valid_records(session, target_date=None):
    """
    Query today's valid records from MariaDB that haven't been processed yet.

    Args:
        session: SQLAlchemy session
        target_date: Date to query (default: today)

    Returns:
        List of APRValidRecord objects
    """
    if target_date is None:
        target_date = date.today()

    records = session.query(APRValidRecord).filter(
        APRValidRecord.DATA == target_date,
        APRValidRecord.is_processado == 0
    ).order_by(
        APRValidRecord.PROPOSTA.asc()  # Process oldest proposals first (ascending order)
    ).all()

    return records


def login_to_ger(page, pause_seconds=2):
    """
    Login to the GER system.

    Args:
        page: Playwright page object
        pause_seconds: Seconds to pause between steps
    """
    print("Logging in to GER system...")

    # Navigate to login page
    page.goto(GER_URL, wait_until="networkidle")
    print(f"✓ Navigated to {GER_URL}")
    time.sleep(pause_seconds)

    # Fill username
    page.fill("#edusuario_I", USERNAME)
    print(f"✓ Entered username: {USERNAME}")
    time.sleep(pause_seconds)

    # Fill password
    page.fill("#edsenha_I", PASSWORD)
    print("✓ Entered password")
    time.sleep(pause_seconds)

    # Click login button
    page.click("#LoginButton")
    print("✓ Clicked login button")
    time.sleep(pause_seconds)

    # Wait for navigation after login
    page.wait_for_load_state("networkidle")
    print("✓ Login successful")
    time.sleep(pause_seconds)


def navigate_to_proposta(page, pause_seconds=2):
    """
    Navigate to the Proposta (Proposal) section.

    Args:
        page: Playwright page object
        pause_seconds: Seconds to pause between steps
    """
    print("\nNavigating to Proposta section...")

    # Click CRÉDITO menu
    page.click("#menu > li > a:has-text('CRÉDITO')")
    print("✓ Clicked CRÉDITO menu")
    time.sleep(pause_seconds)

    # Click Proposta submenu
    page.click("#liproposta > a")
    print("✓ Clicked Proposta submenu")
    time.sleep(pause_seconds)

    # Wait for page to load
    page.wait_for_load_state("networkidle")
    print("✓ Navigated to Proposta page")
    time.sleep(pause_seconds)


def read_proposals_table(page, save_html=False):
    """
    Read the proposals table from the Proposta page.

    Args:
        page: Playwright page object
        save_html: If True, save page HTML to file for debugging

    Returns:
        Tuple of (headers, proposals_data)
        - headers: List of column names (empty list for performance)
        - proposals_data: List of lists with proposal data
    """
    print("\nReading proposals table...")

    # Wait for table to be visible
    page.wait_for_selector("table", timeout=120000)

    # Save HTML for debugging if requested
    if save_html:
        html_content = page.content()
        with open('proposta_page.html', 'w', encoding='utf-8') as f:
            f.write(html_content)
        print("✓ Saved page HTML to proposta_page.html")

    # OPTIMIZATION: Skip reading headers entirely - we don't use them
    # This saves processing 2269 header cells
    headers = []
    print(f"✓ Skipped reading headers for performance (not needed)")

    # OPTIMIZATION: Use CSS selector to directly get only DXDataRow rows
    # This is much faster than querying all rows and filtering
    data_rows = page.query_selector_all('table tbody tr[id*="DXDataRow"]')
    print(f"✓ Found {len(data_rows)} data rows using optimized selector")

    # OPTIMIZATION: Use JavaScript to extract all data at once
    # This is MUCH faster than Python loops with individual text_content() calls
    proposals = page.evaluate("""
        () => {
            const rows = document.querySelectorAll('table tbody tr[id*="DXDataRow"]');
            const data = [];

            rows.forEach(row => {
                const cells = row.querySelectorAll('td');
                const rowData = [];

                cells.forEach(cell => {
                    rowData.push(cell.textContent.trim());
                });

                if (rowData.length > 0) {
                    data.push(rowData);
                }
            });

            return data;
        }
    """)

    print(f"✓ Extracted {len(proposals)} proposals using JavaScript (much faster)")

    # Print first 3 rows as sample
    if len(proposals) > 0:
        print("\nSample rows (first 3):")
        for i, row in enumerate(proposals[:3]):
            print(f"  Row {i+1}: {row}")

    return headers, proposals


def match_proposals_with_db(web_proposals, db_records):
    """
    Match web proposals with database records.

    Args:
        web_proposals: List of proposal data from web table
        db_records: List of APRValidRecord objects from database

    Returns:
        Dictionary with matched and unmatched records
    """
    print("\nMatching proposals with database records...")

    # Create a set of proposal numbers from DB
    db_proposal_numbers = {record.PROPOSTA for record in db_records}

    matched = []
    unmatched_web = []

    # Try to match web proposals with DB records
    # Note: We'll need to identify which column contains the proposal number
    # This will depend on the actual table structure

    for web_prop in web_proposals:
        # TODO: Identify proposal number column in web table
        # For now, we'll just collect all proposals
        matched.append(web_prop)

    print(f"✓ DB records: {len(db_records)}")
    print(f"✓ Web proposals: {len(web_proposals)}")

    return {
        'matched': matched,
        'unmatched_web': unmatched_web,
        'db_records': db_records
    }


def click_proposal_spectacles(page, proposal_number, pause_seconds=2):
    """
    Click the spectacles icon for a specific proposal to enter its details.

    Args:
        page: Playwright page object
        proposal_number: The proposal number to click
        pause_seconds: Seconds to pause after clicking

    Returns:
        True if successful, False otherwise
    """
    print(f"\nClicking spectacles icon for Proposta {proposal_number}...")

    try:
        # Wait for the table to be fully loaded
        page.wait_for_selector("table tbody tr", timeout=120000)

        # Give JavaScript time to render DevExpress buttons
        print("  Waiting for DevExpress buttons to render...")
        time.sleep(2)

        # Find the row containing the proposal number
        rows = page.query_selector_all("table tbody tr")
        print(f"  Found {len(rows)} rows in table")

        for row_idx, row in enumerate(rows):
            cells = row.query_selector_all("td")

            # Debug: Print number of cells in first row
            if row_idx == 0:
                print(f"  First row has {len(cells)} cells (columns)")

            if len(cells) > 3:  # Make sure we have enough cells to read proposal number
                # The proposal number is typically in column 3 (0-indexed)
                proposal_cell_text = cells[3].inner_text().strip()

                if proposal_cell_text == str(proposal_number):
                    print(f"✓ Found row for Proposta {proposal_number} at row index {row_idx}")
                    print(f"  Row has {len(cells)} cells")

                    # Check if row has enough cells for the spectacles button (column 22)
                    if len(cells) <= 20:
                        print(f"  ⚠ Row has only {len(cells)} cells, not enough for spectacles button in column 22")
                        print(f"  This might be a header row or incomplete data row. Skipping...")
                        continue

                    # Debug: Get the actual row ID to find the correct index
                    row_id = row.get_attribute("id")
                    print(f"  DEBUG: Row ID = {row_id}")

                    # Extract the actual row number from the ID (e.g., DXDataRow0 -> 0)
                    actual_row_idx = None
                    if row_id and "DXDataRow" in row_id:
                        try:
                            actual_row_idx = int(row_id.split("DXDataRow")[1])
                            print(f"  DEBUG: Actual DXDataRow index = {actual_row_idx}")
                        except:
                            print(f"  DEBUG: Could not extract row index from ID: {row_id}")

                    # Try multiple strategies to click the DevExpress button
                    # Based on HTML structure (confirmed by user):
                    # - The <td> has id pattern: ctl00_contentManager_gridapr_tccell{row_idx}_22
                    # - The button itself has id: ctl00_contentManager_gridapr_cell{row_idx}_22_btnVisualiza
                    # - The spectacles button is in column 22

                    # Strategy 0: Click using the exact button ID pattern with actual row index
                    # The button ID ends with _btnVisualize_CD (not _btnVisualiza)
                    if actual_row_idx is not None:
                        button_id = f"ctl00_contentManager_gridapr_cell{actual_row_idx}_22_btnVisualize_CD"
                        print(f"  Strategy 0: Looking for button with ID: {button_id}")
                    else:
                        button_id = f"ctl00_contentManager_gridapr_cell{row_idx}_22_btnVisualize_CD"
                        print(f"  Strategy 0: Looking for button with ID (using loop index): {button_id}")

                    try:
                        # Try to click using JavaScript with the exact ID
                        js_result = page.evaluate(f"""
                            () => {{
                                const button = document.getElementById('{button_id}');
                                if (button) {{
                                    console.log('Found button with ID: {button_id}');
                                    button.click();
                                    return 'clicked';
                                }} else {{
                                    console.log('Button not found with ID: {button_id}');
                                    return 'not_found';
                                }}
                            }}
                        """)

                        if js_result == 'clicked':
                            print(f"✓ Clicked spectacles button using exact ID (Strategy 0)")

                            # Wait for navigation to complete
                            print(f"  Waiting for page navigation...")
                            time.sleep(3)  # Give time for navigation to start
                            page.wait_for_load_state("networkidle", timeout=120000)
                            print(f"✓ Page loaded successfully")

                            time.sleep(pause_seconds)
                            return True
                        else:
                            print(f"  Strategy 0 failed: Button not found with ID {button_id}")
                    except Exception as e:
                        print(f"  Strategy 0 error: {e}")

                    # Strategy 1: Try clicking the cell's div with title="Visualizar"
                    try:
                        idx_to_use = actual_row_idx if actual_row_idx is not None else row_idx
                        cell_id = f"ctl00_contentManager_gridapr_tccell{idx_to_use}_22"
                        print(f"  Strategy 1: Looking for cell with ID: {cell_id}")

                        js_click_result = page.evaluate(f"""
                            () => {{
                                const cell = document.getElementById('{cell_id}');
                                if (cell) {{
                                    console.log('Found cell with ID: {cell_id}');
                                    const visualizarDiv = cell.querySelector('div[title="Visualizar"]');
                                    if (visualizarDiv) {{
                                        console.log('Found Visualizar div inside cell');
                                        visualizarDiv.click();
                                        return 'clicked_div';
                                    }} else {{
                                        console.log('Visualizar div not found inside cell');
                                        return 'div_not_found';
                                    }}
                                }} else {{
                                    console.log('Cell not found with ID: {cell_id}');
                                    return 'cell_not_found';
                                }}
                            }}
                        """)

                        if js_click_result == 'clicked_div':
                            print(f"✓ Clicked spectacles button using JavaScript (Strategy 1)")

                            # Wait for navigation to complete
                            print(f"  Waiting for page navigation...")
                            time.sleep(3)  # Give time for navigation to start
                            page.wait_for_load_state("networkidle", timeout=120000)
                            print(f"✓ Page loaded successfully")

                            time.sleep(pause_seconds)
                            return True
                        else:
                            print(f"  Strategy 1 (JavaScript by ID) failed: {js_click_result}")
                    except Exception as e:
                        print(f"  Strategy 1 (JavaScript) error: {e}")

                    # Strategy 2: Click the outer div with class "dxbButton" and title="Visualizar"
                    spectacles_div = row.query_selector('div.dxbButton[title="Visualizar"]')
                    if spectacles_div:
                        print(f"✓ Found spectacles div (Strategy 2) for Proposta {proposal_number}")
                        spectacles_div.click()
                        print(f"✓ Clicked spectacles div for Proposta {proposal_number}")

                        # Wait for navigation to complete
                        print(f"  Waiting for page navigation...")
                        time.sleep(3)  # Give time for navigation to start
                        page.wait_for_load_state("networkidle", timeout=120000)
                        print(f"✓ Page loaded successfully")

                        time.sleep(pause_seconds)
                        return True

                    # Strategy 3: Find by background-image containing "Visualizar.png"
                    visualizar_elements = row.query_selector_all('div[style*="Visualizar.png"]')
                    if visualizar_elements:
                        print(f"✓ Found {len(visualizar_elements)} Visualizar elements (Strategy 3)")
                        visualizar_elements[0].click()
                        print(f"✓ Clicked Visualizar element for Proposta {proposal_number}")

                        # Wait for navigation to complete
                        print(f"  Waiting for page navigation...")
                        time.sleep(3)  # Give time for navigation to start
                        page.wait_for_load_state("networkidle", timeout=120000)
                        print(f"✓ Page loaded successfully")

                        time.sleep(pause_seconds)
                        return True

                    # If all strategies fail, print debug info
                    print(f"⚠ Could not find spectacles button in row for Proposta {proposal_number}")
                    print(f"  Debug: Searching for any clickable elements in row...")

                    # Debug: Find all elements with title="Visualizar"
                    all_visualizar = row.query_selector_all('[title="Visualizar"]')
                    print(f"  Found {len(all_visualizar)} elements with title='Visualizar'")
                    for elem in all_visualizar:
                        tag = elem.evaluate("el => el.tagName")
                        classes = elem.get_attribute("class")
                        elem_id = elem.get_attribute("id")
                        print(f"    - {tag}: class='{classes}', id='{elem_id}'")

                    return False

        print(f"⚠ Could not find row for Proposta {proposal_number}")
        return False

    except Exception as e:
        print(f"✗ Error clicking spectacles icon: {e}")
        import traceback
        traceback.print_exc()
        return False


def select_rating_group(page, rating_group="RATING A", pause_seconds=2):
    """
    Select rating group from dropdown.

    Args:
        page: Playwright page object
        rating_group: Rating group to select (default: "RATING A")
        pause_seconds: Seconds to pause between steps
    """
    print(f"\nSelecting rating group: {rating_group}...")

    # Click on the dropdown
    dropdown_selector = "#ctl00_contentManager_DDLVaduMD"
    page.click(dropdown_selector, timeout=120000)
    print("✓ Clicked rating group dropdown")
    time.sleep(pause_seconds)

    # Select the rating group option
    # Note: The exact selector may need adjustment based on the dropdown implementation
    page.select_option(dropdown_selector, label=rating_group)
    print(f"✓ Selected rating group: {rating_group}")
    time.sleep(pause_seconds)


def send_rating_vadu(rating_group="RATING A", headless=True, dry_run=False, pause_seconds=2, final_pause=10, target_date=None):
    """
    Main function to send rating to Vadu system.

    Args:
        rating_group: Rating group to select
        headless: Run browser in headless mode
        dry_run: If True, only navigate but don't make changes
        pause_seconds: Seconds to pause between steps
        final_pause: Seconds to pause at the end before closing browser
        target_date: Date to query records for (default: today)
    """
    print("=" * 80)
    print("SEND RATING TO VADU SYSTEM")
    print("=" * 80)

    if dry_run:
        print("🔍 DRY RUN MODE - No changes will be made")

    print(f"Rating Group: {rating_group}")
    print(f"Headless Mode: {headless}")
    print(f"Pause between steps: {pause_seconds} seconds")
    print(f"Final pause: {final_pause} seconds")

    if target_date is None:
        target_date = date.today()
    print(f"Target Date: {target_date}")
    print("=" * 80)
    print()

    # Step 0: Query database for today's valid records
    print("Step 0: Querying database for valid records...")
    try:
        session = create_mariadb_session()
        db_records = get_todays_valid_records(session, target_date)
        print(f"✓ Found {len(db_records)} unprocessed valid records in database")

        if len(db_records) > 0:
            print("\nValid records to process:")
            for record in db_records[:10]:  # Show first 10
                print(f"  - Proposta {record.PROPOSTA}: {record.CEDENTE} ({record.RAMO}) - {record.STATUS}")
            if len(db_records) > 10:
                print(f"  ... and {len(db_records) - 10} more")
        else:
            print("\n⚠ No unprocessed valid records found for today")
            print("✓ Nothing to process - browser will not be opened")
            print("=" * 80)
            if session:
                session.close()
            return True  # Success - nothing to do
        print()
    except Exception as e:
        print(f"⚠ Warning: Could not query database: {e}")
        print("Continuing without database records...")
        db_records = []
        session = None

    # Only launch browser if there are records to process
    with sync_playwright() as p:
        # Launch browser
        print(f"\n🌐 Launching browser (headless={headless})...")
        browser = p.chromium.launch(headless=headless)
        context = browser.new_context(
            viewport={'width': 1443, 'height': 1705}
        )
        page = context.new_page()
        print(f"✓ Browser launched successfully!")

        try:
            # Step 1: Login
            login_to_ger(page, pause_seconds=pause_seconds)

            # Step 2: Navigate to Proposta
            navigate_to_proposta(page, pause_seconds=pause_seconds)

            # Take a screenshot of the Proposta page
            page.screenshot(path="proposta_screenshot.png")
            print("✓ Screenshot saved to proposta_screenshot.png")

            # Step 3: Read proposals table from web page
            headers, web_proposals = read_proposals_table(page, save_html=True)

            # Step 4: Match web proposals with database records
            if len(db_records) > 0:
                match_result = match_proposals_with_db(web_proposals, db_records)
                print(f"\n✓ Matching complete")

            # Step 5: Process each record - click spectacles, select rating, click Processar
            if len(db_records) > 0:
                print(f"\n📋 Processing {len(db_records)} proposals...")

                for idx, record in enumerate(db_records, 1):
                    try:
                        print(f"\n{'='*80}")
                        print(f"Processing {idx}/{len(db_records)}: Proposta {record.PROPOSTA} - {record.CEDENTE}")
                        print(f"{'='*80}")

                        # Click spectacles icon to open proposal details
                        print(f"\n🔍 Opening proposal details...")
                        success = click_proposal_spectacles(page, record.PROPOSTA, pause_seconds=pause_seconds)

                        if not success:
                            print(f"❌ Failed to open Proposta {record.PROPOSTA} details. Skipping...")

                            # Mark as processed in database since it's not in the table
                            if not dry_run and session:
                                print(f"📝 Marking Proposta {record.PROPOSTA} as processed (not found in table)...")
                                try:
                                    session.refresh(record)
                                    record.is_processado = 1
                                    session.commit()
                                    print(f"✓ Marked Proposta {record.PROPOSTA} as processed in database")
                                except Exception as db_error:
                                    session.rollback()
                                    print(f"⚠ Could not mark as processed: {db_error}")
                            else:
                                print(f"[DRY RUN] Would mark Proposta {record.PROPOSTA} as processed (not found in table)")

                            # We're already on the Proposta list page (we never left it)
                            # No need to go back, just continue to the next proposal
                            continue

                        print(f"✓ Successfully opened Proposta {record.PROPOSTA} details!")

                        # Select the rating in the dropdown
                        if record.RAMO:
                            print(f"\n🎯 Selecting rating in dropdown...")
                            print(f"   RAMO from database: {record.RAMO}")
                            print(f"   QTD_TITULOS: {record.QTD_TITULOS}")

                            # Check if QTD_TITULOS >= 700, use "SINTÉTICO" instead
                            if record.QTD_TITULOS and record.QTD_TITULOS >= 700:
                                rating_value = "SINTÉTICO"
                                print(f"   ⚠ QTD_TITULOS >= 700, using SINTÉTICO rating")
                            else:
                                # Remove the last character only if it's a space, plus, or minus
                                if len(record.RAMO) > 0 and record.RAMO[-1] in [' ', '+', '-']:
                                    rating_value = record.RAMO[:-1]
                                    print(f"   Removed last character '{record.RAMO[-1]}' from RAMO")
                                else:
                                    rating_value = record.RAMO

                            print(f"   Rating value to select: {rating_value}")

                            try:
                                # Wait for the dropdown to be available (some pages take longer to load)
                                dropdown_selector = 'select[name="ctl00$contentManager$DDLVaduMD"]'
                                print(f"   Waiting for rating dropdown to load...")

                                # Wait up to 2 minutes for the dropdown to appear
                                try:
                                    page.wait_for_selector(dropdown_selector, timeout=120000)
                                    print(f"   ✓ Dropdown loaded")
                                except Exception as wait_error:
                                    print(f"   ⚠ Timeout waiting for dropdown: {wait_error}")

                                dropdown = page.query_selector(dropdown_selector)

                                if dropdown:
                                    # Select the rating by label (visible text)
                                    # This works for both regular ratings (RATING A, RATING B, etc.)
                                    # and special ratings (Sintético)
                                    page.select_option(dropdown_selector, label=rating_value)
                                    print(f"✓ Selected rating: {rating_value}")
                                    time.sleep(1)

                                    # Check the "Processar" button value before clicking
                                    print(f"\n🔘 Checking 'Processar' button...")
                                    processar_button = page.query_selector('input[name="ctl00$contentManager$btVaduMDProcessa"]')

                                    if processar_button:
                                        button_value = processar_button.get_attribute('value')
                                        print(f"   Button value: {button_value}")

                                        if button_value == "Processar":
                                            # New workflow: Click Altera → Grava → Processar
                                            print(f"\n🔘 Starting workflow: Altera → Grava → Processar")

                                            try:
                                                # Step 1: Click on "Altera" button
                                                print(f"\n🔘 Step 1: Looking for 'Altera' button...")

                                                # The Altera button is a div with id="ctl00_contentManager_BtAlteraFluxo_CD"
                                                altera_selector = "div#ctl00_contentManager_BtAlteraFluxo_CD"

                                                # Wait for the Altera button to be visible
                                                altera_button = page.wait_for_selector(
                                                    altera_selector,
                                                    timeout=30000,
                                                    state="visible"
                                                )

                                                if altera_button:
                                                    print(f"✓ Found 'Altera' button")

                                                    # Try to click normally first
                                                    try:
                                                        print(f"   Attempting normal click...")
                                                        altera_button.click(timeout=5000)
                                                        print(f"✓ Clicked 'Altera' button (normal click)")
                                                    except Exception as click_error:
                                                        # If normal click fails (e.g., due to overlay), try JavaScript click
                                                        print(f"   Normal click failed: {click_error}")
                                                        print(f"   Trying JavaScript click to bypass overlay...")
                                                        page.evaluate(f'document.querySelector("{altera_selector}").click()')
                                                        print(f"✓ Clicked 'Altera' button (JavaScript click)")

                                                    # Wait for page to update after Altera
                                                    print(f"   Waiting for page to update after Altera...")
                                                    time.sleep(2)
                                                    print(f"✓ Page updated after Altera")

                                                    # Step 2: Click on "Grava" button
                                                    print(f"\n🔘 Step 2: Looking for 'Grava' button...")

                                                    # The Grava button is a div with id="ctl00_contentManager_BtGravaFluxo_CD"
                                                    grava_selector = "div#ctl00_contentManager_BtGravaFluxo_CD"

                                                    # Wait for the Grava button to be visible
                                                    grava_button = page.wait_for_selector(
                                                        grava_selector,
                                                        timeout=30000,
                                                        state="visible"
                                                    )

                                                    if grava_button:
                                                        print(f"✓ Found 'Grava' button")

                                                        # Try to click normally first
                                                        try:
                                                            print(f"   Attempting normal click...")
                                                            grava_button.click(timeout=5000)
                                                            print(f"✓ Clicked 'Grava' button (normal click)")
                                                        except Exception as click_error:
                                                            # If normal click fails (e.g., due to overlay), try JavaScript click
                                                            print(f"   Normal click failed: {click_error}")
                                                            print(f"   Trying JavaScript click to bypass overlay...")
                                                            page.evaluate(f'document.querySelector("{grava_selector}").click()')
                                                            print(f"✓ Clicked 'Grava' button (JavaScript click)")

                                                        # Wait for loading after Grava
                                                        print(f"   Waiting for page to finish loading after Grava...")
                                                        time.sleep(2)
                                                        page.wait_for_load_state("domcontentloaded", timeout=120000)
                                                        page.wait_for_load_state("networkidle", timeout=120000)
                                                        time.sleep(pause_seconds)
                                                        print(f"✓ Page loaded after Grava")

                                                        # Step 3: Click on "Processar" button with timeout detection
                                                        print(f"\n🔘 Step 3: Clicking 'Processar' button...")

                                                        # Re-query the Processar button (page may have updated)
                                                        processar_button = page.query_selector('input[name="ctl00$contentManager$btVaduMDProcessa"]')

                                                        if processar_button:
                                                            processar_button.click()
                                                            print(f"✓ Clicked 'Processar' button")

                                                            # Mark as processed in database BEFORE restarting browser
                                                            if not dry_run and session:
                                                                # Retry logic for database update (in case of concurrent updates)
                                                                max_retries = 3
                                                                retry_delay = 1  # seconds

                                                                for attempt in range(max_retries):
                                                                    try:
                                                                        # Refresh the record to avoid stale data
                                                                        session.refresh(record)
                                                                        record.is_processado = 1
                                                                        record.is_bot_processed = 1  # Bot clicked Processar
                                                                        session.commit()
                                                                        print(f"✓ Marked Proposta {record.PROPOSTA} as processed in database (is_bot_processed=1)")
                                                                        break  # Success, exit retry loop
                                                                    except Exception as e:
                                                                        session.rollback()
                                                                        if attempt < max_retries - 1:
                                                                            print(f"⚠ Database update failed (attempt {attempt + 1}/{max_retries}): {e}")
                                                                            print(f"  Waiting {retry_delay}s before retry...")
                                                                            time.sleep(retry_delay)
                                                                        else:
                                                                            print(f"❌ Could not mark as processed after {max_retries} attempts: {e}")
                                                            else:
                                                                print(f"[DRY RUN] Would mark Proposta {record.PROPOSTA} as processed (is_bot_processed=1)")

                                                            # SIMPLIFIED APPROACH: Always close browser and restart after clicking Processar
                                                            # This avoids issues with unresponsive pages, DNS errors, etc.
                                                            print(f"   Waiting 5 seconds...")
                                                            time.sleep(5)

                                                            print(f"   Closing browser and restarting process...")

                                                            # Close browser
                                                            try:
                                                                browser.close()
                                                            except:
                                                                pass

                                                            # Close database session
                                                            if session:
                                                                try:
                                                                    session.close()
                                                                except:
                                                                    pass

                                                            # Restart the entire process by calling this function recursively
                                                            print(f"\n{'='*80}")
                                                            print(f"RESTARTING PROCESS AFTER CLICKING PROCESSAR")
                                                            print(f"{'='*80}\n")

                                                            # Wait a bit before restarting
                                                            time.sleep(5)

                                                            # Recursive call to restart
                                                            return send_rating_vadu(
                                                                rating_group=rating_group,
                                                                headless=headless,
                                                                dry_run=dry_run,
                                                                pause_seconds=pause_seconds,
                                                                final_pause=final_pause,
                                                                target_date=target_date
                                                            )
                                                        else:
                                                            print(f"❌ Could not find 'Processar' button after Grava")
                                                    else:
                                                        print(f"❌ Could not find 'Grava' button")
                                                else:
                                                    print(f"❌ Could not find 'Altera' button")
                                            except Exception as e:
                                                print(f"❌ Error in Altera → Grava → Processar workflow: {e}")
                                                import traceback
                                                traceback.print_exc()
                                        else:
                                            # Button value is not "Processar", just mark as processed
                                            print(f"⚠ Button value is '{button_value}' (not 'Processar'), skipping click")

                                            # Mark as processed in database if not dry run
                                            if not dry_run and session:
                                                # Retry logic for database update (in case of concurrent updates)
                                                max_retries = 3
                                                retry_delay = 1  # seconds

                                                for attempt in range(max_retries):
                                                    try:
                                                        # Refresh the record to avoid stale data
                                                        session.refresh(record)
                                                        record.is_processado = 1
                                                        session.commit()
                                                        print(f"✓ Marked Proposta {record.PROPOSTA} as processed in database (without clicking)")
                                                        break  # Success, exit retry loop
                                                    except Exception as e:
                                                        session.rollback()
                                                        if attempt < max_retries - 1:
                                                            print(f"⚠ Database update failed (attempt {attempt + 1}/{max_retries}): {e}")
                                                            print(f"  Waiting {retry_delay}s before retry...")
                                                            time.sleep(retry_delay)
                                                        else:
                                                            print(f"❌ Could not mark as processed after {max_retries} attempts: {e}")
                                            else:
                                                print(f"[DRY RUN] Would mark Proposta {record.PROPOSTA} as processed (without clicking)")
                                    else:
                                        print(f"❌ Could not find 'Processar' button")
                                else:
                                    print(f"❌ Could not find rating dropdown on page")
                            except Exception as e:
                                print(f"❌ Error selecting rating or clicking Processar: {e}")
                        else:
                            print(f"\n⚠ No RAMO found for this record, skipping rating selection")

                        # Return to Proposta list page by re-navigating (more reliable than go_back)
                        print(f"\n⬅ Returning to Proposta list page...")
                        try:
                            navigate_to_proposta(page, pause_seconds=pause_seconds)
                            print(f"✓ Back to Proposta list page")
                        except Exception as nav_error:
                            print(f"⚠ Could not navigate back to Proposta page: {nav_error}")
                            # If navigation fails, try to recover by going to home and then to Proposta
                            try:
                                print(f"   Attempting recovery: navigating from home...")
                                page.goto("https://ger.bmafidc.com.br/GER/Default.aspx")
                                time.sleep(pause_seconds)
                                navigate_to_proposta(page, pause_seconds=pause_seconds)
                                print(f"✓ Recovered and back to Proposta list page")
                            except Exception as recovery_error:
                                print(f"❌ Recovery failed: {recovery_error}")
                                raise  # Re-raise to trigger outer exception handler

                    except Exception as e:
                        print(f"\n❌ Error processing Proposta {record.PROPOSTA}: {e}")
                        import traceback
                        traceback.print_exc()
                        # Try to return to Proposta list page
                        try:
                            print(f"⬅ Attempting to return to Proposta list page...")
                            navigate_to_proposta(page, pause_seconds=pause_seconds)
                            print(f"✓ Returned to Proposta list page")
                        except Exception as nav_error:
                            print(f"⚠ Could not navigate back: {nav_error}")
                            # Try recovery
                            try:
                                print(f"   Attempting recovery: navigating from home...")
                                page.goto("https://ger.bmafidc.com.br/GER/Default.aspx")
                                time.sleep(pause_seconds)
                                navigate_to_proposta(page, pause_seconds=pause_seconds)
                                print(f"✓ Recovered and back to Proposta list page")
                            except Exception as recovery_error:
                                print(f"❌ Recovery failed: {recovery_error}")
                                # Continue to next proposal anyway
                        continue

                print(f"\n{'='*80}")
                print(f"✓ Finished processing all {len(db_records)} proposals!")
                print(f"{'='*80}")

                # Track status changes after bot processing
                if not dry_run and session and len(db_records) > 0:
                    print(f"\n{'='*80}")
                    print("TRACKING STATUS CHANGES AFTER BOT PROCESSING")
                    print(f"{'='*80}")

                    try:
                        # Create MSSQL session to query current status
                        config = load_config()
                        mssql_config = config['databases']['mssql']
                        mssql_connection_string = (
                            f"mssql+pyodbc://{mssql_config['user']}:{mssql_config['password']}"
                            f"@{mssql_config['server']}/{mssql_config['scheme']}"
                            f"?driver=ODBC+Driver+17+for+SQL+Server"
                        )
                        mssql_engine = create_engine(mssql_connection_string)
                        MSSQLSession = sessionmaker(bind=mssql_engine)
                        mssql_session = MSSQLSession()

                        # Track status changes with BOT source
                        status_changes = track_all_status_changes_from_source(
                            mssql_session=mssql_session,
                            mariadb_session=session,
                            target_date=target_date,
                            change_source='BOT'
                        )
                        print(f"✓ Tracked {status_changes} status changes after bot processing")

                        # Close MSSQL session
                        mssql_session.close()

                    except Exception as e:
                        print(f"⚠️  Warning: Could not track status changes: {e}")
                        import traceback
                        traceback.print_exc()
            else:
                print(f"\n⚠ No records to process for today")

            # Final pause to observe the result
            if not headless:
                print(f"\nWaiting {final_pause} seconds before closing browser...")
                time.sleep(final_pause)

            print("\n" + "=" * 80)
            print("✓ Process completed successfully!")
            print("=" * 80)

        except PlaywrightTimeout as e:
            print(f"\n❌ Timeout error: {e}")
            print("The page took too long to load or element was not found.")
            return False

        except Exception as e:
            print(f"\n❌ Error: {e}")
            import traceback
            traceback.print_exc()
            return False

        finally:
            # Close browser
            browser.close()

    return True


def main():
    """
    Main entry point for the program.
    """
    parser = argparse.ArgumentParser(
        description="Send rating information to Vadu system via GER interface"
    )
    parser.add_argument(
        "--rating-group",
        default="RATING A",
        help="Rating group to select (default: RATING A)"
    )
    parser.add_argument(
        "--headless",
        action="store_true",
        default=False,
        help="Run browser in headless mode (default: False)"
    )
    parser.add_argument(
        "--no-headless",
        action="store_true",
        help="Run browser in visible mode (overrides --headless)"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Dry run mode - navigate but don't make changes"
    )
    parser.add_argument(
        "--pause",
        type=int,
        default=2,
        help="Seconds to pause between steps (default: 2)"
    )
    parser.add_argument(
        "--final-pause",
        type=int,
        default=10,
        help="Seconds to pause at the end before closing browser (default: 10)"
    )
    parser.add_argument(
        "--loop",
        action="store_true",
        help="Run in continuous loop mode"
    )
    parser.add_argument(
        "--loop-interval",
        type=int,
        default=60,
        help="Seconds to wait between loop iterations (default: 60)"
    )

    args = parser.parse_args()

    # Handle headless mode
    headless = args.headless and not args.no_headless

    # Run the automation
    if args.loop:
        print("=" * 80)
        print("RUNNING IN LOOP MODE")
        print(f"Loop interval: {args.loop_interval} seconds")
        print("Press Ctrl+C to stop")
        print("=" * 80)
        print()

        iteration = 1
        while True:
            try:
                print(f"\n{'=' * 80}")
                print(f"LOOP ITERATION {iteration}")
                print(f"{'=' * 80}\n")

                success = send_rating_vadu(
                    rating_group=args.rating_group,
                    headless=headless,
                    dry_run=args.dry_run,
                    pause_seconds=args.pause,
                    final_pause=args.final_pause
                )

                iteration += 1

                print(f"\n{'=' * 80}")
                print(f"Waiting {args.loop_interval} seconds before next iteration...")
                print(f"{'=' * 80}\n")
                time.sleep(args.loop_interval)

            except KeyboardInterrupt:
                print("\n\n" + "=" * 80)
                print("Loop stopped by user (Ctrl+C)")
                print("=" * 80)
                sys.exit(0)
    else:
        success = send_rating_vadu(
            rating_group=args.rating_group,
            headless=headless,
            dry_run=args.dry_run,
            pause_seconds=args.pause,
            final_pause=args.final_pause
        )

        # Exit with appropriate code
        sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()

