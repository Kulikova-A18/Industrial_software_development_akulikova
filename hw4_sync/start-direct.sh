# Останавливаем упавший процесс
kill $(cat pids/file-analysis-service.pid) 2>/dev/null


# Пересобираем сервис
echo "Пересборка file-analysis-service..."
cd file-analysis-service
mvn clean package -DskipTests -q 2>&1 | tail -3
cd ..

# Запускаем заново
echo ""
echo "Запуск file-analysis-service..."
java -jar file-analysis-service/target/file-analysis-service-1.0.0.jar > logs/file-analysis-service.log 2>&1 &
echo $! > pids/file-analysis-service.pid
echo "PID: $(cat pids/file-analysis-service.pid)"

# Ждем
echo "Ожидание запуска..."
sleep 10

# Проверка
echo ""
echo "=== Проверка ==="
echo -n "File Analysis: "
if curl -s http://localhost:8082/api/internal/health > /dev/null 2>&1; then
    echo "✓ РАБОТАЕТ"
    curl -s http://localhost:8082/api/internal/health
else
    echo "✗ НЕДОСТУПЕН"
    echo ""
    echo "Ошибка:"
    grep "ERROR\|Caused by" logs/file-analysis-service.log | tail -5
fi

# Тест всей системы
echo ""
echo "=== Тест загрузки файла ==="
echo "test content" > /tmp/test.txt
echo "Отправка файла..."
curl -s -X POST http://localhost:8080/api/works \
    -F "studentName=Иванов Иван" \
    -F "file=@/tmp/test.txt"
rm /tmp/test.txt

echo ""
echo ""
echo "=== Статус всех сервисов ==="
for f in pids/*.pid; do
    name=$(basename "$f" .pid)
    pid=$(cat "$f")
    if kill -0 $pid 2>/dev/null; then
        echo "  ✓ $name (PID: $pid)"
    else
        echo "  ✗ $name (PID: $pid) - УПАЛ"
    fi
done

echo ""
echo "API Gateway: http://localhost:8080/health-check"