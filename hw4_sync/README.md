# Запускаем PostgreSQL контейнеры
cd ~/Industrial_software_development_akulikova/hw4_sync

# Запускаем только базы данных
sudo docker compose up -d db-storing db-analysis

# Ждем готовности БД
echo "Ожидание готовности PostgreSQL..."
sleep 10

# Проверяем статус
sudo docker compose ps

# Теперь запускаем сервисы
pkill -f "file-storing-service" 2>/dev/null
pkill -f "file-analysis-service" 2>/dev/null
pkill -f "api-gateway" 2>/dev/null

# Запускаем File Storing Service (без H2, с PostgreSQL)
echo "Запуск File Storing Service..."
java -jar file-storing-service/target/file-storing-service-1.0.0.jar > logs/file-storing-service.log 2>&1 &
echo $! > pids/file-storing-service.pid
sleep 10

# Запускаем File Analysis Service
echo "Запуск File Analysis Service..."
java -jar file-analysis-service/target/file-analysis-service-1.0.0.jar > logs/file-analysis-service.log 2>&1 &
echo $! > pids/file-analysis-service.pid
sleep 10

# Запускаем API Gateway
echo "Запуск API Gateway..."
java -jar api-gateway/target/api-gateway-1.0.0.jar > logs/api-gateway.log 2>&1 &
echo $! > pids/api-gateway.pid
sleep 10

# Проверяем статус
echo ""
echo "=== Проверка статуса ==="
curl -s http://localhost:8081/api/works/health && echo " ✓ File Storing OK" || echo " ✗ File Storing FAILED"
curl -s http://localhost:8082/api/internal/health && echo " ✓ File Analysis OK" || echo " ✗ File Analysis FAILED"
curl -s http://localhost:8080/health-check && echo " ✓ API Gateway OK" || echo " ✗ API Gateway FAILED"


cd ~/Industrial_software_development_akulikova/hw4_sync/gui

# Сборка
mvn clean package -DskipTests

# Запуск
java -jar target/gui-1.0.0.jar