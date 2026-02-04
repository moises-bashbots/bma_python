#!/usr/bin/env python3
"""
Gunicorn configuration for APR Dashboard (Production)
"""

import multiprocessing
import os

# Server socket
bind = "0.0.0.0:5000"
backlog = 2048

# Worker processes
workers = multiprocessing.cpu_count() * 2 + 1  # Recommended formula
worker_class = "sync"
worker_connections = 1000
timeout = 120  # 2 minutes for slow queries
keepalive = 5

# Restart workers after this many requests (prevents memory leaks)
max_requests = 1000
max_requests_jitter = 50

# Logging
accesslog = "/var/log/apr_dashboard/access.log"
errorlog = "/var/log/apr_dashboard/error.log"
loglevel = "info"
access_log_format = '%(h)s %(l)s %(u)s %(t)s "%(r)s" %(s)s %(b)s "%(f)s" "%(a)s" %(D)s'

# Process naming
proc_name = "apr_dashboard"

# Server mechanics
daemon = False  # systemd will manage daemonization
pidfile = "/var/run/apr_dashboard.pid"
umask = 0o007
user = None  # Run as current user (robot)
group = None
tmp_upload_dir = None

# SSL (handled by nginx, so we don't need it here)
# keyfile = None
# certfile = None

# Server hooks
def on_starting(server):
    """Called just before the master process is initialized."""
    print("Starting APR Dashboard with Gunicorn...")

def on_reload(server):
    """Called to recycle workers during a reload via SIGHUP."""
    print("Reloading APR Dashboard...")

def when_ready(server):
    """Called just after the server is started."""
    print(f"APR Dashboard ready! Listening on {bind}")
    print(f"Workers: {workers}")

def on_exit(server):
    """Called just before exiting Gunicorn."""
    print("Shutting down APR Dashboard...")

