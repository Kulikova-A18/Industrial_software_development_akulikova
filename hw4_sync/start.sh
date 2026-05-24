#!/bin/bash

set -e

cd /home/vboxuser/Industrial_software_development_akulikova/hw4_sync

mkdir -p logs pids

# Остановка старых процессов
pkill -f "file-storing-service" 2>/dev/null || true
pkill -f "file-analysis-service" 2>/dev/null || true
pkill -f "api-gateway" 2>/dev/null || true

# Запуск баз данных
sudo docker compose up -d db-storing db-analysis
sleep 10

# Проверка, что JAR файлы существуют
if [ ! -f "file-storing-service/target/file-storing-service-1.0.0.jar" ]; then
    echo "Building file-storing-service..."
    cd file-storing-service && mvn clean package -DskipTests && cd ..
fi

if [ ! -f "file-analysis-service/target/file-analysis-service-1.0.0.jar" ]; then
    echo "Building file-analysis-service..."
    cd file-analysis-service && mvn clean package -DskipTests && cd ..
fi

if [ ! -f "api-gateway/target/api-gateway-1.0.0.jar" ]; then
    echo "Building api-gateway..."
    cd api-gateway && mvn clean package -DskipTests && cd ..
fi

# Запуск сервисов с правильными profile
echo "Starting File Storing Service..."
java -jar file-storing-service/target/file-storing-service-1.0.0.jar --spring.profiles.active=default > logs/file-storing-service.log 2>&1 &
echo $! > pids/file-storing-service.pid

sleep 5

echo "Starting File Analysis Service..."
java -jar file-analysis-service/target/file-analysis-service-1.0.0.jar --spring.profiles.active=default > logs/file-analysis-service.log 2>&1 &
echo $! > pids/file-analysis-service.pid

sleep 5

echo "Starting API Gateway..."
java -jar api-gateway/target/api-gateway-1.0.0.jar > logs/api-gateway.log 2>&1 &
echo $! > pids/api-gateway.pid

sleep 15

# Проверка статуса
echo ""
echo "=== Проверка статуса ==="

if curl -s http://localhost:8081/api/works/health > /dev/null 2>&1; then
    echo "File Storing OK"
else
    echo "File Storing FAILED - check logs/file-storing-service.log"
fi

if curl -s http://localhost:8082/api/internal/health > /dev/null 2>&1; then
    echo "File Analysis OK"
else
    echo "File Analysis FAILED - check logs/file-analysis-service.log"
fi

if curl -s http://localhost:8080/health-check > /dev/null 2>&1; then
    echo "API Gateway OK"
else
    echo "API Gateway FAILED - check logs/api-gateway.log"
fi

echo ""
echo "GUI will start in 5 seconds..."
sleep 5

cd gui
mvn clean package -DskipTests -q
java -jar target/gui-1.0.0.jar