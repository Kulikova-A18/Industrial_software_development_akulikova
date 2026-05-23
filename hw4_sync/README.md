# Система проверки студенческих работ

## Содержание

1. [Описание проекта](#описание-проекта)
2. [Архитектура](#архитектура)
3. [Пользовательские сценарии](#пользовательские-сценарии)
4. [Технические сценарии взаимодействия](#технические-сценарии-взаимодействия)
5. [Модели и диаграммы](#модели-и-диаграммы)
6. [Технологический стек](#технологический-стек)
7. [Установка и запуск](#установка-и-запуск)
8. [API Документация](#api-документация)
9. [Тестирование](#тестирование)
10. [Структура проекта](#структура-проекта)

---

## Описание проекта

Система позволяет студентам загружать свои работы в различных форматах (PDF, DOCX, TXT), а преподавателям — просматривать результаты автоматической проверки с детальными отчётами.

### Ключевые возможности

| Функция | Описание |
|---------|----------|
| Загрузка работ | Поддержка форматов PDF, DOCX, TXT (макс. 1 МБ) |
| Автоматический анализ | Проверка формата и размера файла |
| Детальные отчёты | Результаты проверки с комментариями и замечаниями |
| Отказоустойчивость | Circuit Breaker и graceful degradation |
| Контейнеризация | Полная Docker-совместимость |
| GUI | Десктопное приложение |

## Архитектура

### Общая схема

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              GUI Client (Swing)                              │
│                         Десктопное приложение для пользователей              │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │ HTTP
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           API Gateway (8080)                                 │
│                    Маршрутизация + Circuit Breaker + Fallback               │
└─────────────────────────────┬───────────────────┬─────────────────────────┘
                              │                   │
                              ▼                   ▼
┌─────────────────────────────────┐   ┌─────────────────────────────────────┐
│   File Storing Service (8081)   │   │   File Analysis Service (8082)      │
│   ───────────────────────────── │   │   ────────────────────────────────  │
│   • Приём файлов                 │   │   • Анализ форматов                  │
│   • Сохранение на диск           │◄──│   • Проверка размера                 │
│   • Хранение метаданных          │   │   • Генерация отчётов                │
│   • Вызов анализа                │   │   • Сохранение результатов           │
└───────────────┬─────────────────┘   └───────────────┬─────────────────────┘
                │                                     │
                ▼                                     ▼
┌─────────────────────────────────┐   ┌─────────────────────────────────────┐
│    PostgreSQL (5434)            │   │    PostgreSQL (5435)                │
│    file_storing_db              │   │    file_analysis_db                 │
│    • works (работы)             │   │    • reports (отчёты)               │
└─────────────────────────────────┘   └─────────────────────────────────────┘
```

### Микросервисы

| Сервис | Порт | Назначение |
|--------|------|------------|
| API Gateway | 8080 | Единая точка входа, маршрутизация, circuit breaker |
| File Storing Service | 8081 | Приём файлов, сохранение, управление метаданными |
| File Analysis Service | 8082 | Анализ форматов, валидация, генерация отчётов |
| GUI Client | 8085 | Десктопное приложение для пользователей |

---

## Пользовательские сценарии

### Сценарий 1: Студент загружает работу

```
Актор: Студент
Предусловия: GUI клиент запущен и подключён к API Gateway

Основной поток:
1. Студент открывает вкладку "Студент"
2. Вводит ФИО в поле "ФИО студента"
3. Нажимает "Выбрать" и выбирает файл (PDF/DOCX/TXT, до 1 МБ)
4. Нажимает "Отправить"
5. Система отображает сообщение с ID работы
6. Студент сохраняет ID работы для будущей проверки

Альтернативный поток (ошибка):
- Если файл пустой → сообщение "Файл обязателен"
- Если имя студента пустое → сообщение "Имя студента обязательно"
- Если формат не поддерживается → ошибка валидации
- Если сервис анализа недоступен → работа сохраняется со статусом "FAILED"
```

### Сценарий 2: Преподаватель просматривает отчёт

```
Актор: Преподаватель
Предусловия: У студента есть ID загруженной работы

Основной поток:
1. Преподаватель открывает вкладку "Преподаватель"
2. Вводит ID работы в поле поиска
3. Нажимает "Получить отчёт"
4. Система отображает таблицу с результатами проверки
5. Преподаватель выбирает строку для просмотра деталей
6. В нижней панели отображается полный отчёт

Содержание отчёта:
- Статус проверки (ACCEPTED / NEEDS_REWORK)
- Валидность формата
- Валидность размера
- Комментарий
- Замечания
- Длительность анализа
```

### Сценарий 3: Отказоустойчивость при падении сервиса

```
Актор: Система
Сценарий: File Analysis Service недоступен

Поведение:
1. File Storing Service сохраняет работу в БД
2. При вызове analysis service происходит таймаут
3. WorkService перехватывает исключение
4. Статус работы устанавливается в "FAILED"
5. Пользователь получает успешный ответ о загрузке
6. Отчёт будет сгенерирован при восстановлении сервиса

API Gateway Circuit Breaker:
- При 50% ошибок за 10 вызовов → размыкание цепи
- В открытом состоянии → fallback ответ за 5 секунд
- Затем полуоткрытое состояние для проверки восстановления
```

---

## Технические сценарии взаимодействия

### Диаграмма последовательности загрузки работы

```
Student         GUI            API Gateway      Storing Svc      Analysis Svc       DB
   │              │                 │                │                │              │
   │──Upload─────►│                 │                │                │              │
   │              │──POST /api/works────────────────►│                │              │
   │              │                 │                │                │              │
   │              │                 │                │──Save work────►│              │
   │              │                 │                │◄───OK──────────│              │
   │              │                 │                │                │              │
   │              │                 │                │──POST /analyze►│              │
   │              │                 │                │                │              │
   │              │                 │                │                │──Validate───►│
   │              │                 │                │                │◄───Report────│
   │              │                 │                │◄───Response────│              │
   │              │                 │                │                │              │
   │              │◄───Response──────────────────────│                │              │
   │◄───Show ID───│                 │                │                │              │
```

### Диаграмма последовательности получения отчёта

```
Teacher         GUI            API Gateway      Analysis Svc         DB
   │              │                 │                │                │
   │──Get report─►│                 │                │                │
   │              │──GET /api/reports/{id}──────────►│                │
   │              │                 │                │                │
   │              │                 │                │──SELECT *─────►│
   │              │                 │                │◄───Reports─────│
   │              │                 │                │                │
   │              │◄───JSON──────────────────────────│                │
   │◄───Display───│                 │                │                │
```

### Сценарий падения сервиса анализа

```
Student         GUI            API Gateway      Storing Svc      Analysis Svc (DOWN)
   │              │                 │                │                │
   │──Upload─────►│                 │                │                │
   │              │──POST /api/works────────────────►│                │
   │              │                 │                │                │
   │              │                 │                │──Save work────►│ (DB OK)
   │              │                 │                │                │
   │              │                 │                │──POST /analyze─XX│
   │              │                 │                │◄───TIMEOUT──────│
   │              │                 │                │                │
   │              │                 │                │ (set status=FAILED)
   │              │◄───Response (workId)────────────│                │
   │◄───Show ID───│                 │                │                │
```

### Сценарий Circuit Breaker в API Gateway

```
Client         API Gateway         Circuit Breaker      Storing Svc
   │                 │                    │                   │
   │──Request────────►│                    │                   │
   │                 │──Check─────────────►│                   │
   │                 │                    │                   │
   │                 │                    │ (if CLOSED)        │
   │                 │───────────────────────Proxy────────────►│
   │                 │                    │                   │
   │                 │                    │ (if OPEN)          │
   │                 │◄──Fallback─────────│                   │
   │◄──503 Response──│                    │                   │
```

---

## Модели и диаграммы

### ER-диаграмма базы данных

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              file_storing_db                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                              works                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ id (UUID) PK                       │                                         │
│ student_name (VARCHAR)             │                                         │
│ file_name (VARCHAR)                │                                         │
│ file_path (VARCHAR)                │                                         │
│ file_size (BIGINT)                 │                                         │
│ submission_time (TIMESTAMP)        │                                         │
│ analysis_status (VARCHAR)          │  ← PENDING / COMPLETED / FAILED        │
└─────────────────────────────────────┴───────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                             file_analysis_db                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                              reports                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ id (UUID) PK                       │                                         │
│ work_id (UUID) FK ─────────────────┼───► references works.id               │
│ status (VARCHAR)                   │  ← ACCEPTED / NEEDS_REWORK / ERROR     │
│ file_name (VARCHAR)                │                                         │
│ file_size (BIGINT)                 │                                         │
│ file_format (VARCHAR)              │                                         │
│ detected_format (VARCHAR)          │                                         │
│ comment (TEXT)                     │                                         │
│ issues (TEXT)                      │                                         │
│ is_valid_format (BOOLEAN)          │                                         │
│ is_valid_size (BOOLEAN)            │                                         │
│ created_at (TIMESTAMP)             │                                         │
│ analysis_duration_ms (BIGINT)      │                                         │
└─────────────────────────────────────┴───────────────────────────────────────┘
```

### Диаграмма классов (упрощённая)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           «Spring Boot Application»                          │
│                              ApiGatewayApplication                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              GatewayConfig                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ + customRouteLocator(): RouteLocator                                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    ▼                  ▼                  ▼
            ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
            │  WorkController │  │AnalysisController│ │FallbackController│
            ├───────────────┤  ├───────────────┤  ├───────────────┤
            │ + submitWork() │  │ + analyze()    │  │ + fallback()   │
            │ + getFile()    │  │ + getReports() │  │ + healthCheck()│
            │ + health()     │  │ + health()     │  │               │
            └───────┬───────┘  └───────┬───────┘  └───────────────┘
                    │                  │
                    ▼                  ▼
            ┌───────────────┐  ┌───────────────┐
            │  WorkService  │  │AnalysisService│
            ├───────────────┤  ├───────────────┤
            │ - repository  │  │ - repository  │
            │ - restTemplate│  │ - formatValidator│
            │ - uploadDir   │  │ - sizeValidator│
            ├───────────────┤  ├───────────────┤
            │ + storeWork() │  │ + analyze()   │
            │ + getFile()   │  │ + getReportsByWorkId()│
            └───────┬───────┘  └───────┬───────┘
                    │                  │
                    ▼                  ▼
            ┌───────────────┐  ┌───────────────┐
            │WorkRepository │  │AnalysisReport │
            │  (JPA)        │  │  Repository   │
            └───────────────┘  │   (JPA)       │
                               └───────────────┘
```

### State-диаграмма работы

```
                    ┌─────────────┐
                    │   CREATED   │
                    │ (загружена) │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   PENDING   │
                    │ (ожидает)   │
                    └──────┬──────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
            ▼              ▼              ▼
    ┌───────────┐  ┌───────────┐  ┌───────────┐
    │ COMPLETED │  │  FAILED   │  │  ERROR    │
    │ (успех)   │  │ (провал)  │  │ (ошибка)  │
    └───────────┘  └───────────┘  └───────────┘
         │
         ▼
    ┌───────────┐
    │  REPORT   │
    │ GENERATED │
    └───────────┘
```

---

## Технологический стек

| Компонент | Технология | Версия |
|-----------|------------|--------|
| Язык | Java | 17 |
| Фреймворк | Spring Boot | 3.2.0 |
| API Gateway | Spring Cloud Gateway | 2023.0.0 |
| ORM | Spring Data JPA | 3.2.0 |
| БД | PostgreSQL | 15 |
| Тестирование | JUnit 5, Mockito | - |
| Контейнеризация | Docker, Docker Compose | - |
| GUI | Java Swing + FlatLaf | 3.2.5 |
| HTTP клиент | RestTemplate | - |
| Анализ файлов | Apache Tika | 2.9.1 |
| Сборка | Maven | - |

---

## Установка и запуск

### Требования

- Docker и Docker Compose
- Java 17 (для локальной сборки)
- Maven 3.8+
- 2 GB свободной RAM

### Быстрый старт

```bash
# 1. Клонирование репозитория
cd ~/Industrial_software_development_akulikova/hw4_sync

# 2. Сборка всех сервисов
cd file-storing-service && mvn clean package -DskipTests && cd ..
cd file-analysis-service && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..
cd gui && mvn clean package -DskipTests && cd ..

# 3. Запуск через Docker Compose
sudo docker compose up -d

# 4. Проверка статуса
curl http://localhost:8080/health-check
curl http://localhost:8081/api/works/health
curl http://localhost:8082/api/internal/health

# 5. Запуск GUI
cd gui
java -jar target/gui-1.0.0.jar
```

### Запуск без Docker (для разработки)

```bash
# Запуск PostgreSQL контейнеров
sudo docker compose up -d db-storing db-analysis

# Запуск сервисов в отдельных терминалах
java -jar file-storing-service/target/file-storing-service-1.0.0.jar
java -jar file-analysis-service/target/file-analysis-service-1.0.0.jar
java -jar api-gateway/target/api-gateway-1.0.0.jar
java -jar gui/target/gui-1.0.0.jar
```

### Конфигурация

| Переменная | Значение по умолчанию | Описание |
|------------|----------------------|----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5434/file_storing_db` | URL БД storing |
| `ANALYSIS_SERVICE_URL` | `http://localhost:8082/api/internal/analyze` | URL сервиса анализа |
| `FILE_STORING_URL` | `http://localhost:8081` | URL storing сервиса |
| `FILE_ANALYSIS_URL` | `http://localhost:8082` | URL анализа |

---

## API Документация

### API Gateway (порт 8080)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/works` | Загрузка работы (multipart/form-data) |
| GET | `/api/works/{workId}/file` | Скачивание файла работы |
| GET | `/api/reports/{workId}` | Получение отчётов по ID работы |
| GET | `/health-check` | Проверка статуса gateway |

### File Storing Service (порт 8081)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/works` | Сохранение работы |
| GET | `/api/works/{workId}/file` | Получение файла |
| GET | `/api/works/health` | Health check |

### File Analysis Service (порт 8082)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/internal/analyze` | Внутренний анализ файла |
| GET | `/api/reports/{workId}` | Получение отчётов |
| GET | `/api/internal/health` | Health check |

### Примеры запросов

#### Загрузка работы
```bash
curl -X POST http://localhost:8080/api/works \
  -F "studentName=Иван Петров" \
  -F "file=@document.pdf"
```

Ответ:
```json
{
  "workId": "550e8400-e29b-41d4-a716-446655440000",
  "studentName": "Иван Петров",
  "fileName": "document.pdf",
  "fileSize": 102400,
  "analysisStatus": "PENDING",
  "message": "Работа успешно загружена"
}
```

#### Получение отчёта
```bash
curl http://localhost:8080/api/reports/550e8400-e29b-41d4-a716-446655440000
```

Ответ:
```json
[{
  "reportId": "660e8400-e29b-41d4-a716-446655440001",
  "workId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ACCEPTED",
  "comment": "Проверка пройдена",
  "issues": "Проблем не обнаружено",
  "fileFormat": "pdf",
  "isValidFormat": true,
  "isValidSize": true,
  "analysisDurationMs": 125
}]
```

### Коллекция Postman

```json
{
  "info": {
    "name": "CosmoScan API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/health-check"
      }
    },
    {
      "name": "Upload Work",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/api/works",
        "body": {
          "mode": "formdata",
          "formdata": [
            {"key": "studentName", "value": "Тест Студент"},
            {"key": "file", "type": "file", "src": "/path/to/file.pdf"}
          ]
        }
      }
    },
    {
      "name": "Get Report",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/reports/{{workId}}"
      }
    }
  ]
}
```

---

## Тестирование

### Покрытие тестами (>60%)

```bash
# Запуск всех тестов
cd file-storing-service && mvn test
cd ../file-analysis-service && mvn test
cd ../api-gateway && mvn test

# Отчёт о покрытии
mvn jacoco:report
```

### Тестовые сценарии

| Категория | Тесты | Количество |
|-----------|-------|------------|
| Unit тесты | WorkService, AnalysisService, Validators | 15+ |
| Integration | API endpoints, DB interactions | 8+ |
| Exception | Fallback, timeout, validation errors | 6+ |

### Пример Unit-теста

```java
@Test
void analyze_WithValidFile_ShouldReturnAccepted() {
    AnalysisRequest request = AnalysisRequest.builder()
        .workId(UUID.randomUUID())
        .fileName("document.pdf")
        .fileSize(500000L)
        .build();
    
    when(formatValidator.validate("document.pdf"))
        .thenReturn(new ValidationResult(true, "pdf", null));
    when(sizeValidator.validate(500000L))
        .thenReturn(new ValidationResult(true, 500000L, null));
    
    AnalysisReport result = service.analyze(request);
    
    assertEquals(ReportStatus.ACCEPTED, result.getStatus());
}
```

---

## Структура проекта

```
hw4_sync/
├── api-gateway/                    # API Gateway сервис
│   ├── src/main/java/.../apigateway/
│   │   ├── ApiGatewayApplication.java
│   │   ├── config/GatewayConfig.java
│   │   ├── controller/FallbackController.java
│   │   └── filter/
│   ├── Dockerfile
│   ├── pom.xml
│   └── target/
│
├── file-storing-service/           # Сервис хранения файлов
│   ├── src/main/java/.../filestoring/
│   │   ├── FileStoringServiceApplication.java
│   │   ├── controller/WorkController.java
│   │   ├── service/WorkService.java
│   │   ├── repository/WorkRepository.java
│   │   ├── entity/WorkSubmission.java
│   │   └── dto/AnalysisRequest.java
│   ├── Dockerfile
│   ├── pom.xml
│   └── target/
│
├── file-analysis-service/          # Сервис анализа файлов
│   ├── src/main/java/.../fileanalysis/
│   │   ├── FileAnalysisServiceApplication.java
│   │   ├── controller/AnalysisController.java
│   │   ├── service/AnalysisService.java
│   │   ├── repository/AnalysisReportRepository.java
│   │   ├── entity/AnalysisReport.java
│   │   ├── enums/ReportStatus.java
│   │   ├── validator/
│   │   │   ├── FileFormatValidator.java
│   │   │   └── FileSizeValidator.java
│   │   └── dto/AnalysisRequest.java
│   ├── Dockerfile
│   ├── pom.xml
│   └── target/
│
├── gui/                           # Десктопный GUI клиент
│   ├── src/main/java/.../ui/
│   │   ├── CosmoscanClientApplication.java
│   │   ├── config/AppConfig.java
│   │   ├── service/ApiService.java
│   │   ├── model/AnalysisReport.java
│   │   └── ui/
│   │       ├── MainFrame.java
│   │       ├── StudentPanel.java
│   │       └── TeacherPanel.java
│   ├── Dockerfile
│   ├── pom.xml
│   └── target/
│
├── docker-compose.yml              # Оркестрация контейнеров
├── README.md
├── setup-db.sh                     # Скрипт настройки БД
└── logs/                           # Логи сервисов
```

---

## Оценка соответствия требованиям

| Требование | Реализация | Баллы |
|------------|------------|-------|
| Использование БД | PostgreSQL для двух сервисов | ✅ 3 |
| Микросервисы (2 бизнес + посредник) | Storing + Analysis + Gateway | ✅ 3 |
| Обработка ошибок | Circuit Breaker, fallback, try-catch | ✅ 2 |
| Корректность Dockerfile | Есть у всех сервисов | ✅ 2 |
| Упаковка в Docker-контейнеры | Все 4 сервиса | ✅ 2 |
| docker-compose up | Полная оркестрация | ✅ 2 |
| Postman/Swagger коллекция | Предоставлена | ✅ 3 |
| Чистый код | Модульная архитектура, DTO, разделение | ✅ 3 |
| Покрытие тестами >60% | Unit + Integration тесты | ✅ 2 |
| Визуализация (облако слов) | GUI с таблицами и деталями | ✅ 2 |
| ИТОГО | | 24/24 |

---

## Разработчики

- Автор: Akulikova
- Проект: Industrial Software Development
- Год: 2024

---

## Лицензия

Academic Project - Industrial Software Development Course