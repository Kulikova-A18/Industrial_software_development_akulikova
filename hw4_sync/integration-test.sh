#!/bin/bash
set -xe

WORK_ID=$(curl -s -X POST http://localhost:8080/api/works \
    -F "studentName=Integration Test" \
    -F "file=@/tmp/test.pdf" | grep -o '"workId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$WORK_ID" ]; then
    echo "Integration test FAILED - cannot upload"
    exit 1
fi

sleep 3

REPORT=$(curl -s "http://localhost:8080/api/reports/${WORK_ID}")

if echo "$REPORT" | grep -q "workId"; then
    echo "Integration test PASSED"
else
    echo "Integration test FAILED"
    exit 1
fi