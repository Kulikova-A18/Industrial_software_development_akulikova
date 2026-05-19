# КосмоСкан (CosmoScan)

Информационная система для приёма и автоматической проверки студенческих работ.

## Архитектура

### Микросервисы
- **API Gateway** (порт 8080) - единая точка входа, маршрутизация запросов
- **File Storing Service** (порт 8081) - хранение файлов и метаданных
- **File Analysis Service** (порт 8082) - автоматическая проверка файлов
- **CosmoScan UI** - клиентское Swing-приложение

### Базы данных
- `file_storing_db` (PostgreSQL, порт 5432)
- `file_analysis_db` (PostgreSQL, порт 5433)

## Требования
- Java 17+
- Docker и Docker Compose
- Maven 3.8+
- Linux (протестировано на Ubuntu 22.04)

## Быстрый запуск

### 1. Сборка проекта
```bash
chmod +x create-cosmoscan-project.sh
./create-cosmoscan-project.sh
cd cosmoscan
```

### 2. Сборка каждого сервиса
```bash
cd api-gateway && mvn clean package -DskipTests && cd ..
cd file-storing-service && mvn clean package -DskipTests && cd ..
cd file-analysis-service && mvn clean package -DskipTests && cd ..
cd cosmoscan-ui && mvn clean package -DskipTests && cd ..
```

### 3. Запуск через Docker Compose
```bash
docker-compose up --build
```

### 4. Запуск UI (отдельно)
```bash
java -jar cosmoscan-ui/target/cosmoscan-ui-1.0.0.jar
```

## API Endpoints

| Метод | URL | Описание |
|-------|-----|----------|
| POST | /api/works | Загрузка работы (multipart) |
| GET | /api/works/{id}/file | Скачивание файла |
| GET | /api/reports/{workId} | Получение отчётов |
| GET | /health-check | Проверка здоровья |

## Тестирование

### Загрузка валидного PDF
```bash
curl -X POST http://localhost:8080/api/works \
  -F "studentName=Иванов Иван" \
  -F "file=@test.pdf"
```

### Получение отчёта
```bash
curl http://localhost:8080/api/reports/{workId}
```

### Тест отказоустойчивости
```bash
docker-compose stop file-analysis-service
# Отправить файл - должно вернуться предупреждение
```

## Структура проекта
```
cosmoscan/
├── api-gateway/
├── file-storing-service/
├── file-analysis-service/
├── cosmoscan-ui/
├── docker-compose.yml
└── README.md
```
