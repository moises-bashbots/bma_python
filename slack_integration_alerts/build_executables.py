#!/usr/bin/env python3
"""
Build standalone executables for Slack integration programs.
Creates single-file executables with all dependencies embedded.
"""

import os
import sys
import shutil
import subprocess
import base64
import tarfile
import io
from pathlib import Path

# Programs to build
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

# Files to copy
SOURCE_FILES = [
    'test_query.py',
    'query_pre_impresso_with_propostas.py',
    'models.py'
]

# Configuration files to copy
CONFIG_FILES = [
    'databases_config.json',
    'slack_config.json'
]


def create_embedded_tarball(temp_dir):
    """Create a tarball of all dependencies and source files."""
    tarball_io = io.BytesIO()

    with tarfile.open(fileobj=tarball_io, mode='w:gz') as tar:
        # Add lib directory
        lib_dir = temp_dir / 'lib'
        if lib_dir.exists():
            tar.add(lib_dir, arcname='lib')

        # Add source files
        for source_file in SOURCE_FILES:
            src = temp_dir / source_file
            if src.exists():
                tar.add(src, arcname=source_file)

    tarball_io.seek(0)
    return base64.b64encode(tarball_io.read()).decode('ascii')


def create_self_extracting_executable(program, embedded_data, deploy_path, python_executable):
    """Create a self-extracting executable with embedded dependencies."""
    python_version = f"{sys.version_info.major}.{sys.version_info.minor}"
    python_path = os.path.realpath(python_executable)

    # Create the self-extracting script
    launcher_content = f'''#!{python_path}
# Self-extracting executable for {program['name']}
# All dependencies are embedded in this file
# Built with Python {python_version}

import sys
import os
import base64
import tarfile
import io
import tempfile
import atexit
import shutil
from pathlib import Path

# Embedded data (base64-encoded tarball)
EMBEDDED_DATA = """
{embedded_data}
"""

def extract_and_run():
    """Extract embedded dependencies and run the program."""
    # Create temporary directory for extraction
    temp_dir = tempfile.mkdtemp(prefix='{program['name']}_')

    # Register cleanup
    def cleanup():
        try:
            shutil.rmtree(temp_dir, ignore_errors=True)
        except:
            pass
    atexit.register(cleanup)

    # Decode and extract embedded data
    try:
        tarball_data = base64.b64decode(EMBEDDED_DATA)
        tarball_io = io.BytesIO(tarball_data)

        with tarfile.open(fileobj=tarball_io, mode='r:gz') as tar:
            tar.extractall(temp_dir)

        # Add lib to path
        lib_dir = os.path.join(temp_dir, 'lib')
        if os.path.exists(lib_dir):
            sys.path.insert(0, lib_dir)

        # Add temp_dir to path for source files
        sys.path.insert(0, temp_dir)

        # Import and run main
        from {program['script'].replace('.py', '')} import main
        return main()

    except Exception as e:
        print(f"✗ Error extracting/running: {{e}}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1

if __name__ == '__main__':
    sys.exit(extract_and_run())
'''

    launcher_file = deploy_path / program['name']
    launcher_file.write_text(launcher_content)
    os.chmod(launcher_file, 0o755)

    # Get file size
    file_size_mb = launcher_file.stat().st_size / (1024 * 1024)
    print(f"✓ Created executable: {program['name']} ({file_size_mb:.1f} MB)")
    return launcher_file


def prepare_bundle_directory(temp_dir):
    """Prepare a temporary directory with all files to be bundled."""
    print(f"\n{'='*80}")
    print(f"Preparing bundle directory...")
    print(f"{'='*80}\n")

    # Copy source files
    print("Copying source files...")
    for source_file in SOURCE_FILES:
        src = Path(source_file)
        if src.exists():
            dest = temp_dir / source_file
            shutil.copy2(src, dest)
            print(f"✓ Copied: {source_file}")

    # Install dependencies to lib/
    lib_dir = temp_dir / 'lib'
    lib_dir.mkdir(exist_ok=True)

    print("\nInstalling dependencies...")
    cmd = [
        sys.executable, '-m', 'pip', 'install',
        '-r', 'requirements.txt',
        '--target', str(lib_dir),
        '--upgrade',
        '--quiet'
    ]

    result = subprocess.run(cmd, capture_output=True, text=True)

    if result.returncode != 0:
        print(f"✗ Failed to install dependencies")
        print(f"STDERR: {result.stderr}")
        return False

    print(f"✓ Dependencies installed")
    return True


def build_standalone_executables(deploy_dir):
    """Build standalone executables with all dependencies embedded."""
    deploy_path = Path(deploy_dir)
    deploy_path.mkdir(parents=True, exist_ok=True)

    print(f"\n{'='*80}")
    print(f"Building standalone executables at: {deploy_path}")
    print(f"{'='*80}\n")

    # Create temporary directory for bundling
    import tempfile
    with tempfile.TemporaryDirectory() as temp_dir_str:
        temp_dir = Path(temp_dir_str)

        # Prepare bundle directory
        if not prepare_bundle_directory(temp_dir):
            return False

        # Create embedded tarball
        print(f"\n{'='*80}")
        print(f"Creating embedded bundle...")
        print(f"{'='*80}\n")

        print("Compressing dependencies and source files...")
        embedded_data = create_embedded_tarball(temp_dir)
        embedded_size_mb = len(embedded_data) / (1024 * 1024)
        print(f"✓ Bundle created ({embedded_size_mb:.1f} MB compressed)")

        # Create self-extracting executables
        print(f"\n{'='*80}")
        print(f"Creating self-extracting executables...")
        print(f"{'='*80}\n")

        for program in PROGRAMS:
            create_self_extracting_executable(program, embedded_data, deploy_path, sys.executable)

    # Copy configuration files
    print(f"\n{'='*80}")
    print(f"Copying configuration files...")
    print(f"{'='*80}\n")

    for config_file in CONFIG_FILES:
        src = Path(config_file)
        if src.exists():
            dest = deploy_path / config_file
            shutil.copy2(src, dest)
            print(f"✓ Copied: {config_file}")
        else:
            print(f"⚠ Warning: {config_file} not found")

    # Create README
    readme_content = """# BMA Slack Integration Alerts - Standalone Executables

## Contents

This package contains standalone, self-extracting executables for BMA Slack integration alerts:

1. **duplicatas_invalidas** - Duplicatas Inválidas Report Generator
2. **seuno_invalidos** - SEUNO Inválidos Report Generator

## Structure

```
slack_integration_alerts/
├── duplicatas_invalidas          # Self-extracting executable (~20MB)
├── seuno_invalidos               # Self-extracting executable (~20MB)
├── databases_config.json         # Database configuration
└── slack_config.json             # Slack configuration
```

## Requirements

- Python 3.12 (automatically used by executables)
- Configuration files (must be in the same directory as executables):
  - databases_config.json
  - slack_config.json

## Usage

Simply run the executables from this directory:

```bash
./duplicatas_invalidas
./seuno_invalidos
```

The executables are self-contained and will automatically:
1. Extract embedded dependencies to a temporary directory
2. Run the program with all dependencies available
3. Clean up temporary files on exit

## Output

- Duplicatas Inválidas reports: `duplicatas_invalidas/` directory (created automatically)
- SEUNO Inválidos reports: `seuno_invalidos/` directory (created automatically)

## Configuration

Edit the JSON configuration files as needed:
- `databases_config.json` - Database connection settings
- `slack_config.json` - Slack bot token and channel settings

## Notes

- **All Python dependencies are embedded inside the executables**
- No virtual environment, pip installation, or lib/ directory required
- **Only configuration JSON files are external dependencies**
- Each executable is self-extracting and self-contained
- Temporary files are automatically cleaned up after execution
- Built with Python 3.12 - executables use the bundled Python interpreter path
"""

    readme_file = deploy_path / 'README.md'
    readme_file.write_text(readme_content)
    print(f"✓ Created README.md")

    return True


def main():
    """Main build and deployment process."""
    # Change to slack_integration_alerts directory
    script_dir = Path(__file__).parent
    os.chdir(script_dir)

    print("="*80)
    print("BMA Slack Integration - Standalone Deployment Builder")
    print("="*80)

    # Deploy to target directory
    deploy_dir = '/home/robot/Deploy/slack_integration_alerts'

    if build_deployment_package(deploy_dir):
        print(f"\n{'='*80}")
        print(f"✓ Deployment package created successfully!")
        print(f"{'='*80}")
        print(f"\nLocation: {deploy_dir}")
        print(f"\nExecutables:")
        for program in PROGRAMS:
            print(f"  - {program['name']}")
        print(f"\nTo run the programs:")
        print(f"  cd {deploy_dir}")
        print(f"  ./duplicatas_invalidas")
        print(f"  ./seuno_invalidos")
        print(f"\nNote: All dependencies are bundled in the lib/ directory.")
        print(f"      Only configuration files (*.json) are external dependencies.")
        return 0
    else:
        print(f"\n{'='*80}")
        print(f"✗ Build failed")
        print(f"{'='*80}")
        return 1


if __name__ == '__main__':
    sys.exit(main())

