# Отчет по рефакторингу проекта спутниковой группировки

## Архитектура системы

```mermaid
graph TB
    subgraph "Mission Scheduler (port=8081)"
        A[Main.java] --> B[AppConfig.java]
        B --> C[ThreadPoolTaskScheduler]
        B --> D[RestClient]
        
        E[application.yml] --> F[MissionProperties]
        F --> G[ConfiguredMissionScheduler]
        
        G --> C
        G --> H[MissionExecutionService]
        H --> I[SpaceOperationClient]
        I --> D
    end
    
    subgraph "Space Operation Center (port=8080)"
        J[REST API /api/missions]
        K[SpaceOperationCenterService]
        L[SatelliteConstellation]
    end
    
    D -->|POST /missions| J
    J --> K
    K --> L
```

## Что было сделано

Разработан микросервис-планировщик на Spring Framework, который автоматически выполняет запланированные миссии для спутниковых группировок по cron-расписанию 

Сервис читает конфигурацию миссий из YAML-файла, использует TaskScheduler для планирования задач и отправляет HTTP-запросы к основному сервису управления спутниками через RestClient

![run_programm](./screens/run_programm.png)

## Запуск проекта

```bash
chmod +x run.sh
./run.sh
```
