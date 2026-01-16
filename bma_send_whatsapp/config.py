#!/usr/bin/env python3
"""
Configuration file for WhatsApp API integration
"""

import os
from dataclasses import dataclass
from typing import Optional


@dataclass
class WhatsAppConfig:
    """Configuration for WhatsApp API"""
    base_url: str = "https://wgne28.api.infobip.com"
    api_key: str = "App 7ab8933757a70c2a0d9e9e4e44785e69-d745c038-d468-451a-800c-9b96df63d4bf"
    from_number: str = "447860088970"
    
    @classmethod
    def from_env(cls) -> 'WhatsAppConfig':
        """Create configuration from environment variables"""
        return cls(
            base_url=os.getenv('WHATSAPP_BASE_URL', cls.base_url),
            api_key=os.getenv('WHATSAPP_API_KEY', cls.api_key),
            from_number=os.getenv('WHATSAPP_FROM_NUMBER', cls.from_number)
        )


# Default configuration instance
DEFAULT_CONFIG = WhatsAppConfig()

# Environment-based configuration
ENV_CONFIG = WhatsAppConfig.from_env()
