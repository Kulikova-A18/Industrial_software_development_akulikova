# Отчет по рефакторингу проекта спутниковой группировки

## Архитектура системы

```mermaid
classDiagram
    class SatelliteType {
        <<enumeration>>
        IMAGE
        COMMUNICATION
    }

    class SatelliteParam {
        <<abstract>>
        -SatelliteType type
        -String name
        -double batteryLevel
        +getType()
        +getName()
        +getBatteryLevel()
    }

    class ImagingSatelliteParam {
        -double resolution
        +getResolution()
    }

    class CommunicationSatelliteParam {
        -double bandwidth
        +getBandwidth()
    }

    class SatelliteFactory {
        <<interface>>
        +createSatelliteWithParameter(SatelliteParam)
        +isSatelliteTypeSupported(SatelliteType)
    }

    class ImagingSatelliteFactory {
        +createSatelliteWithParameter(SatelliteParam)
        +isSatelliteTypeSupported(SatelliteType)
        +createHighResolutionSatellite()
        +createLowResolutionSatellite()
    }

    class CommunicationSatelliteFactory {
        +createSatelliteWithParameter(SatelliteParam)
        +isSatelliteTypeSupported(SatelliteType)
    }

    class SatelliteService {
        <<interface>>
        +createSatellite(SatelliteParam)
    }

    class SatelliteServiceImpl {
        -List~SatelliteFactory~ factories
        +createSatellite(SatelliteParam)
        -findSuitableFactory(SatelliteType)
    }

    class SpaceOperationException {
        +SpaceOperationException(String)
        +SpaceOperationException(String, Throwable)
    }

    class Satellite {
        <<abstract>>
    }

    class ImagingSatellite {
        +ImagingSatellite(String, double)
    }

    class CommunicationSatellite {
        +CommunicationSatellite(String, double)
    }

    SatelliteParam <|-- ImagingSatelliteParam
    SatelliteParam <|-- CommunicationSatelliteParam
    
    SatelliteFactory <|.. ImagingSatelliteFactory
    SatelliteFactory <|.. CommunicationSatelliteFactory
    
    SatelliteService <|.. SatelliteServiceImpl
    
    SatelliteServiceImpl --> SatelliteFactory : использует
    SatelliteFactory --> SatelliteParam : принимает
    SatelliteFactory --> SpaceOperationException : выбрасывает
    
    ImagingSatelliteFactory --> ImagingSatelliteParam : создает
    ImagingSatelliteFactory --> ImagingSatellite : создает
    
    CommunicationSatelliteFactory --> CommunicationSatelliteParam : создает
    CommunicationSatelliteFactory --> CommunicationSatellite : создает
    
    Satellite <|-- ImagingSatellite
    Satellite <|-- CommunicationSatellite
```


## Что было изменено

Добавлены новые компоненты

| Компонент | Назначение |
|-----------|------------|
|SatelliteType(enum) | Перечисление типов спутников:IMAGE`,COMMUNICATION|
|SatelliteParam(abstract) | Базовый класс для параметров спутника |
|ImagingSatelliteParam| Параметры для спутника ДЗЗ (добавлено полеresolution`) |
|CommunicationSatelliteParam| Параметры для коммуникационного спутника (добавлено полеbandwidth`) |
|SpaceOperationException| Специализированное исключение для ошибок в операциях |
|SatelliteService(interface) | Интерфейс сервиса создания спутников |
|SatelliteServiceImpl| Реализация сервиса с выбором подходящей фабрики |


Реализованные паттерны GoF

Strategy Pattern (поведенческий)
- Контекст:SatelliteServiceImpl`
- Стратегии: различные реализацииSatelliteFactory`
- Результат: алгоритм создания спутника выбирается динамически на основе типа

Factory Method (порождающий)
- Создатели:SatelliteFactoryи его реализации
- Продукты:Satelliteи его наследники
- Результат: единый интерфейс создания с типизированными параметрами

Builder Pattern (уже был)
- Используется вEnergySystem.EnergySystemBuilderиConstellationBuilder`

Template Method (уже был)
- ВSatellite.executeMission()с вызовом абстрактногоperformSpecificMission()`

