#!/usr/bin/env python3
"""
Build standalone executables using PyInstaller.
Creates single-file executables with all dependencies embedded.
"""

import os
import sys
import shutil
import subprocess
from pathlib import Path

# Configuration
DEPLOY_DIR = "/home/robot/Deploy/slack_integration_alerts"

PROGRAMS = [
    {
        'name': 'duplicatas_invalidas',
        'script': 'test_query.py',
        'description': 'Duplicatas Inválidas Report Generator'
    },
    {
        'name': 'seuno_invalidos',
        'script': 'query_pre_impresso_with_propostas.py',
        'description': 'SEUNO Inválidos Report Generator'
    }
]

CONFIG_FILES = [
    'slack_integration_alerts/databases_config.json',
    'slack_integration_alerts/slack_config.json'
]


def build_executable(program, deploy_path):
    """Build a single executable using PyInstaller."""
    script_path = Path('slack_integration_alerts') / program['script']
    
    if not script_path.exists():
        print(f"✗ Script not found: {script_path}")
        return False
    
    print(f"\nBuilding {program['name']}...")
    print(f"  Script: {script_path}")
    
    # PyInstaller command
    cmd = [
        sys.executable, '-m', 'PyInstaller',
        '--onefile',  # Single file executable
        '--name', program['name'],  # Output name
        '--distpath', str(deploy_path),  # Output directory
        '--workpath', 'build',  # Build directory
        '--specpath', 'build',  # Spec file directory
        '--clean',  # Clean cache
        '--noconfirm',  # Overwrite without asking
        '--hidden-import', 'pymssql',  # Ensure pymssql is included
        '--hidden-import', 'sqlalchemy',
        '--hidden-import', 'openpyxl',
        '--hidden-import', 'requests',
        '--collect-all', 'pymssql',  # Collect all pymssql files
        '--collect-all', 'sqlalchemy',
        '--collect-all', 'openpyxl',
        str(script_path)
    ]
    
    print(f"  Running PyInstaller...")
    result = subprocess.run(cmd, capture_output=True, text=True)
    
    if result.returncode != 0:
        print(f"✗ PyInstaller failed!")
        print(f"STDERR: {result.stderr}")
        return False
    
    # Check if executable was created
    exe_path = deploy_path / program['name']
    if exe_path.exists():
        size_mb = exe_path.stat().st_size / (1024 * 1024)
        print(f"✓ Created: {program['name']} ({size_mb:.1f} MB)")
        return True
    else:
        print(f"✗ Executable not found: {exe_path}")
        return False


def main():
    """Main build function."""
    print("="*80)
    print("BMA Slack Integration - PyInstaller Build")
    print("="*80)
    
    deploy_path = Path(DEPLOY_DIR)
    
    # Clean and create deployment directory
    if deploy_path.exists():
        print(f"\nCleaning deployment directory: {deploy_path}")
        shutil.rmtree(deploy_path)
    
    deploy_path.mkdir(parents=True, exist_ok=True)
    print(f"Created deployment directory: {deploy_path}")
    
    # Build executables
    print(f"\n{'='*80}")
    print("Building executables...")
    print(f"{'='*80}")
    
    success_count = 0
    for program in PROGRAMS:
        if build_executable(program, deploy_path):
            success_count += 1
    
    # Copy configuration files
    print(f"\n{'='*80}")
    print("Copying configuration files...")
    print(f"{'='*80}\n")
    
    for config_file in CONFIG_FILES:
        src = Path(config_file)
        if src.exists():
            dest = deploy_path / src.name
            shutil.copy2(src, dest)
            print(f"✓ Copied: {src.name}")
        else:
            print(f"⚠ Warning: {config_file} not found")
    
    # Create README
    readme_content = """# BMA Slack Integration Alerts - Standalone Executables

## Contents

Standalone executables built with PyInstaller:

1. **duplicatas_invalidas** - Duplicatas Inválidas Report Generator
2. **seuno_invalidos** - SEUNO Inválidos Report Generator

## Requirements

- Configuration files (must be in the same directory as executables):
  - databases_config.json
  - slack_config.json

## Usage

```bash
./duplicatas_invalidas
./seuno_invalidos
```

## Notes

- All dependencies are embedded in the executables
- No Python installation or virtual environment required
- Only configuration JSON files are external dependencies
"""
    
    readme_file = deploy_path / 'README.md'
    readme_file.write_text(readme_content)
    print(f"✓ Created README.md")
    
    # Summary
    print(f"\n{'='*80}")
    if success_count == len(PROGRAMS):
        print("✓ Build completed successfully!")
    else:
        print(f"⚠ Build completed with {len(PROGRAMS) - success_count} failures")
    print(f"{'='*80}\n")
    
    print(f"Deployment location: {deploy_path}")
    print(f"Executables built: {success_count}/{len(PROGRAMS)}")
    
    return success_count == len(PROGRAMS)


if __name__ == '__main__':
    sys.exit(0 if main() else 1)

