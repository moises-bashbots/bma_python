#!/usr/bin/env python3
"""Test file upload to Slack."""

import json
import requests
from pathlib import Path

def load_slack_config():
    config_path = Path(__file__).parent / "slack_config.json"
    with open(config_path, 'r') as f:
        return json.load(f)

def test_upload():
    """Test uploading a file."""
    config = load_slack_config()
    bot_token = config['slack']['bot_token']
    channel_id = config['slack']['channel']
    
    # Find an Excel file
    excel_files = list(Path(__file__).parent.glob('duplicatas_invalidas/*.xlsx'))
    if not excel_files:
        print("No Excel files found")
        return
    
    excel_file = excel_files[0]
    print(f"Testing upload of: {excel_file}")
    print(f"File size: {excel_file.stat().st_size} bytes")
    print(f"Channel ID: {channel_id}")
    
    # Try using form data instead of JSON for getUploadURLExternal
    print("\n=== Using files.getUploadURLExternal (with form data) ===")

    filename = excel_file.name
    file_size = excel_file.stat().st_size

    # Step 1: Get upload URL using form data
    response1 = requests.post(
        'https://slack.com/api/files.getUploadURLExternal',
        headers={
            'Authorization': f'Bearer {bot_token}'
        },
        data={
            'filename': filename,
            'length': file_size
        },
        timeout=30
    )

    result1 = response1.json()
    print("Step 1 - Get Upload URL:")
    print(json.dumps(result1, indent=2))

    if not result1.get('ok'):
        print(f"\n❌ Failed at step 1: {result1.get('error')}")
        return

    upload_url = result1['upload_url']
    file_id = result1['file_id']
    print(f"\n✓ Got upload URL and file_id: {file_id}")

    # Step 2: Upload file
    print("\nStep 2 - Upload File:")
    with open(excel_file, 'rb') as f:
        response2 = requests.post(
            upload_url,
            data=f.read(),
            timeout=60
        )

    print(f"Status: {response2.status_code}")
    if response2.status_code != 200:
        print(f"❌ Failed at step 2: HTTP {response2.status_code}")
        print(f"Response: {response2.text}")
        return

    print("✓ File uploaded")

    # Step 3: Complete upload
    print("\nStep 3 - Complete Upload:")
    response3 = requests.post(
        'https://slack.com/api/files.completeUploadExternal',
        headers={
            'Authorization': f'Bearer {bot_token}'
        },
        data={
            'files': json.dumps([{'id': file_id, 'title': filename}]),
            'channel_id': channel_id,
            'initial_comment': '*Test Upload*\nTesting file upload functionality'
        },
        timeout=30
    )

    result3 = response3.json()
    print(json.dumps(result3, indent=2))

    if result3.get('ok'):
        print("\n✅ Upload successful!")
        return
    else:
        print(f"\n❌ Failed at step 3: {result3.get('error')}")
        return

    # OLD METHOD BELOW (keeping for reference)
    # Step 1: Get upload URL
    print("\n=== Step 1: Get Upload URL ===")
    response1 = requests.post(
        'https://slack.com/api/files.getUploadURLExternal',
        headers={
            'Authorization': f'Bearer {bot_token}',
            'Content-Type': 'application/json'
        },
        json={
            'filename': excel_file.name,
            'length': excel_file.stat().st_size
        },
        timeout=30
    )

    result1 = response1.json()
    print(json.dumps(result1, indent=2))
    
    if not result1.get('ok'):
        print(f"\n❌ Failed at step 1: {result1.get('error')}")
        return
    
    upload_url = result1['upload_url']
    file_id = result1['file_id']
    
    # Step 2: Upload file
    print("\n=== Step 2: Upload File ===")
    with open(excel_file, 'rb') as f:
        file_data = f.read()
    
    response2 = requests.post(
        upload_url,
        data=file_data,
        headers={'Content-Type': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'},
        timeout=60
    )
    
    print(f"Status: {response2.status_code}")
    print(f"Response: {response2.text[:200]}")
    
    if response2.status_code != 200:
        print(f"\n❌ Failed at step 2: HTTP {response2.status_code}")
        return
    
    # Step 3: Complete upload
    print("\n=== Step 3: Complete Upload ===")
    response3 = requests.post(
        'https://slack.com/api/files.completeUploadExternal',
        headers={
            'Authorization': f'Bearer {bot_token}',
            'Content-Type': 'application/json'
        },
        json={
            'files': [
                {
                    'id': file_id,
                    'title': excel_file.name
                }
            ],
            'channel_id': channel_id,
            'initial_comment': '*Test Upload*\nTesting file upload functionality'
        },
        timeout=30
    )
    
    result3 = response3.json()
    print(json.dumps(result3, indent=2))
    
    if result3.get('ok'):
        print("\n✅ Upload successful!")
    else:
        print(f"\n❌ Failed at step 3: {result3.get('error')}")

if __name__ == "__main__":
    test_upload()

