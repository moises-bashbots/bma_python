#!/usr/bin/env python3
import os
import sys

dist_path = "/home/robot/Dev/bma_python/slack_integration_alerts/dist/send_rating_vadu"
deploy_path = "/home/robot/Deploy/slack_integration_alerts/send_rating_vadu"

print("=" * 80)
print("BINARY BUILD AND DEPLOYMENT CHECK")
print("=" * 80)

print(f"\n1. Checking dist directory: {dist_path}")
if os.path.exists(dist_path):
    size = os.path.getsize(dist_path)
    print(f"   ✓ Binary exists in dist!")
    print(f"   Size: {size:,} bytes ({size / 1024 / 1024:.2f} MB)")
    print(f"   Executable: {os.access(dist_path, os.X_OK)}")
else:
    print(f"   ✗ Binary NOT found in dist")
    sys.exit(1)

print(f"\n2. Checking deploy directory: {deploy_path}")
if os.path.exists(deploy_path):
    size = os.path.getsize(deploy_path)
    print(f"   ✓ Binary exists in deploy directory!")
    print(f"   Size: {size:,} bytes ({size / 1024 / 1024:.2f} MB)")
    print(f"   Executable: {os.access(deploy_path, os.X_OK)}")
else:
    print(f"   ✗ Binary NOT found in deploy directory")
    sys.exit(1)

env_path = "/home/robot/Deploy/slack_integration_alerts/.env"
print(f"\n3. Checking .env file: {env_path}")
if os.path.exists(env_path):
    print(f"   ✓ .env file exists in deploy directory!")
else:
    print(f"   ⚠ .env file NOT found (may need to be copied manually)")

print("\n" + "=" * 80)
print("✓ BUILD AND DEPLOYMENT SUCCESSFUL!")
print("=" * 80)
print(f"\nTo run the binary:")
print(f"  cd /home/robot/Deploy/slack_integration_alerts")
print(f"  DISPLAY=:10.0 ./send_rating_vadu --pause 2")
print()

