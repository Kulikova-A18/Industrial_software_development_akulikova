#!/bin/bash

cd ~/Industrial_software_development_akulikova/hw4_sync

for svc in api-gateway file-analysis-service file-storing-service; do
    if [ -f pids/${svc}.pid ]; then
        kill $(cat pids/${svc}.pid) 2>/dev/null || true
        rm pids/${svc}.pid
    fi
done

pkill -f "file-storing-service" 2>/dev/null || true
pkill -f "file-analysis-service" 2>/dev/null || true
pkill -f "api-gateway" 2>/dev/null || true

sudo docker compose down -v
