#!/bin/bash

set -xe

PROJECT_DIR=$(pwd)
REPORT_DIR="$PROJECT_DIR/security-reports"
mkdir -p "$REPORT_DIR" || echo error

export PATH="$HOME/.local/bin:$PATH"

if ! command -v gitleaks &> /dev/null; then echo error; else gitleaks detect --source "$PROJECT_DIR" --report-format json --report-path "$REPORT_DIR/gitleaks-report.json" 2>&1 | tail -1 || echo error; fi

if ! command -v semgrep &> /dev/null; then echo error; else semgrep --config=p/java --config=p/owasp-top-ten --config=p/secrets --json --output "$REPORT_DIR/semgrep-report.json" "$PROJECT_DIR" > /dev/null 2>&1 || echo error; fi

if ! command -v syft &> /dev/null; then echo error; else syft dir:"$PROJECT_DIR" -o cyclonedx-json="$REPORT_DIR/sbom.json" > /dev/null 2>&1 || echo error; fi

if ! command -v grype &> /dev/null || [ ! -f "$REPORT_DIR/sbom.json" ]; then echo error; else grype sbom:"$REPORT_DIR/sbom.json" -o json > "$REPORT_DIR/grype-report.json" 2>/dev/null || echo error; fi

if ! docker ps &> /dev/null; then echo error; else docker run --rm --network=host -v "$REPORT_DIR:/zap/wrk/:rw" ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://localhost:8080 -r /zap/wrk/zap-report.html -J /zap/wrk/zap-report.json 2>&1 | tail -5 || echo error; fi
