from setuptools import setup, find_packages

setup(
    name='bma-whatsapp-sync',
    version='1.0.0',
    description='BMA WhatsApp Chats Sync Tool',
    py_modules=['get_chats_sync'],
    install_requires=[
        'requests==2.32.5',
        'mysql-connector-python==9.5.0',
    ],
    entry_points={
        'console_scripts': [
            'get_chats_sync=get_chats_sync:main',
        ],
    },
    python_requires='>=3.8',
)

