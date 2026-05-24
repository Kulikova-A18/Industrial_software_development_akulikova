#!/bin/bash
set -xe

BASE_URL="http://localhost:8080"
WORK_ID=""

# Создание временного файла
echo "Test content" > /tmp/test.pdf

# 1. Тест загрузки файла
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/works" \
    -F "studentName=Test Student" \
    -F "file=@/tmp/test.pdf")
echo "$RESPONSE"

WORK_ID=$(echo "$RESPONSE" | grep -o '"workId":"[^"]*"' | cut -d'"' -f4)
echo "Work ID: $WORK_ID"

if [ -n "$WORK_ID" ]; then
    echo "Upload test PASSED"
else
    echo "Upload test FAILED"
    exit 1
fi

# 2. Тест получения отчёта
sleep 2
REPORT=$(curl -s "${BASE_URL}/api/reports/${WORK_ID}")
echo "$REPORT"

if echo "$REPORT" | grep -q "workId"; then
    echo "Report test PASSED"
else
    echo "Report test FAILED"
fi

# 3. Тест health check
echo -e "\n=== Test 3: Health check ==="
HEALTH=$(curl -s "${BASE_URL}/health-check")
echo "$HEALTH"

if echo "$HEALTH" | grep -q "UP"; then
    echo "Health check PASSED"
else
    echo "Health check FAILED"
fi

# 4. Тест загрузки невалидного файла (архив)
echo "test" > /tmp/test.zip
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/works" \
    -F "studentName=Test Student" \
    -F "file=@/tmp/test.zip")
echo "$RESPONSE"

if echo "$RESPONSE" | grep -qi "error\|архив"; then
    echo "Invalid file test PASSED"
else
    echo "Invalid file test FAILED"
fi

# 5. Тест загрузки без имени
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/works" \
    -F "file=@/tmp/test.pdf")
echo "$RESPONSE"

if echo "$RESPONSE" | grep -qi "error\|обязательно"; then
    echo "Validation test PASSED"
else
    echo "Validation test FAILED"
fi

rm -f /tmp/test.pdf /tmp/test.zip