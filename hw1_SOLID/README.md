# Домашнее задание №1. Закрепление принципов SOLID

## Предметная область

Программа моделирует основной модуль ERP-системы для Московского зоопарка, направленный на автоматизацию ключевых рутинных процессов по уходу за животными. 

Система имитирует кормление животных, проведение обязательных ветеринарных осмотров при поступлении в зоопарк и уборку вольеров с учетом видоспецифических требований. 

`Цель` — обеспечить своевременный и регламентированный уход, способствующий здоровью и благополучию животных.

## Реализация принципов SOLID

```memraid
classDiagram
    class AnimalInformationProvider {
        <<interface>>
        +getAnimalName() String
        +getAnimalSpecies() String
    }

    class FeedableAnimal {
        <<interface>>
        +feedAnimal(foodType String, foodAmountInGramsOrKilograms int) void
    }

    class HealableAnimal {
        <<interface>>
        +performVeterinaryMedicalCheckup() void
    }

    class CleanableEnclosure {
        <<interface>>
        +cleanAnimalEnclosure() void
    }

    class ZooAnimal {
        <<abstract>>
        -animalName String
        -animalSpecies String
        +ZooAnimal(animalName String, animalSpecies String)
        +getAnimalName() String
        +getAnimalSpecies() String
    }

    class Parrot {
        +Parrot(animalName String)
        +feedAnimal(foodType String, foodAmountInGrams int) void
        +performVeterinaryMedicalCheckup() void
    }

    class Crocodile {
        +Crocodile(animalName String)
        +feedAnimal(foodType String, foodAmountInKilograms int) void
        +performVeterinaryMedicalCheckup() void
        +cleanAnimalEnclosure() void
    }

    class ZookeeperEmployee {
        +feedSpecifiedAnimal(animal FeedableAnimal, foodType String, foodAmount int) void
    }

    class VeterinarianEmployee {
        +conductMedicalExaminationOnAnimal(animal HealableAnimal) void
    }

    class EnclosureCleanerEmployee {
        +cleanSpecifiedAnimalEnclosure(enclosure CleanableEnclosure) void
    }

    class AnimalFeedingOrchestrationService {
        -assignedZookeeper ZookeeperEmployee
        +AnimalFeedingOrchestrationService(zookeeper ZookeeperEmployee)
        +scheduleAndExecuteFeeding(targetAnimal FeedableAnimal, foodType String, foodAmount int) void
    }

    class VeterinaryMedicalCheckupService {
        -assignedVeterinarian VeterinarianEmployee
        +VeterinaryMedicalCheckupService(veterinarian VeterinarianEmployee)
        +initiateAndCompleteMedicalCheckup(targetAnimal HealableAnimal) void
    }

    class ZooManagementSystemDemo {
        +main(args String[]) void
    }

    ZooAnimal ..|> AnimalInformationProvider : implements
    Parrot --|> ZooAnimal : extends
    Parrot ..|> FeedableAnimal : implements
    Parrot ..|> HealableAnimal : implements

    Crocodile --|> ZooAnimal : extends
    Crocodile ..|> FeedableAnimal : implements
    Crocodile ..|> HealableAnimal : implements
    Crocodile ..|> CleanableEnclosure : implements

    ZookeeperEmployee --> FeedableAnimal : uses
    VeterinarianEmployee --> HealableAnimal : uses
    EnclosureCleanerEmployee --> CleanableEnclosure : uses

    AnimalFeedingOrchestrationService --> ZookeeperEmployee : depends on
    AnimalFeedingOrchestrationService --> FeedableAnimal : uses

    VeterinaryMedicalCheckupService --> VeterinarianEmployee : depends on
    VeterinaryMedicalCheckupService --> HealableAnimal : uses

    ZooManagementSystemDemo --> Parrot : creates
    ZooManagementSystemDemo --> Crocodile : creates
    ZooManagementSystemDemo --> AnimalFeedingOrchestrationService : instantiates
    ZooManagementSystemDemo --> VeterinaryMedicalCheckupService : instantiates
    ZooManagementSystemDemo --> EnclosureCleanerEmployee : uses
    ZooManagementSystemDemo --> ZookeeperEmployee : creates
    ZooManagementSystemDemo --> VeterinarianEmployee : creates
```

### Принцип Single Responsibility Principle (сокр. SRP)

Каждый класс отвечает ровно за одну задачу. Абстрактный класс `ZooAnimal` отвечает за хранение и предоставление идентификационных данных. 

Конкретные классы животных `Parrot`,`Crocodile` инкапсулируют только поведение, специфичное для вида. 

Классы сотрудников `ZookeeperEmployee`,`VeterinarianEmployee`,`EnclosureCleanerEmployee` реализуют только свои операционные функции. 

Сервисы оркестрации `AnimalFeedingOrchestrationService`,`VeterinaryMedicalCheckupService` координируют бизнес-процессы и делегируют выполнение сотрудникам, не содержа при этом логики непосредственного ухода.

### Принцип Open/Closed Principle (сокр. OCP)

Система открыта для расширения, но закрыта для модификации. 

Новые виды животных можно добавлять путем создания новых подклассов `ZooAnimal` и реализации необходимых интерфейсов возможностей без изменения существующей логики сервисов или сотрудников. 

Аналогично, новые роли сотрудников или сервисы могут быть внедрены без вмешательства в ядро архитектуры.

### Принцип Liskov Substitution Principle (сокр. LSP)

Все подклассы и реализации интерфейсов полностью соблюдают контракт, заданный их базовыми типами. Любой метод, принимающий `FeedableAnimal`, `HealableAnimal` или `CleanableEnclosure`, может безопасно работать с любой конкретной реализацией, не зная их точного типа и не рискуя вызвать неожиданное поведение.

### Принцип Interface Segregation Principle (сокр. ISP)

Дизайн избегает сложных интерфейсов за счет определения минимальных и целенаправленных контрактов. 

Вместо одного интерфейса `Animal` со всеми возможными методами система использует четыре узкоспециализированных интерфейса. 

Каждое животное реализует только те интерфейсы, которые соответствуют его биологическим и операционным потребностям (например,`Parrot` не реализует `CleanableEnclosure`). 

Сотрудники и сервисы зависят только от тех возможностей, которые им действительно нужны.

### Принцип Dependency Inversion Principle (сокр. DIP)

Высокоуровневые модули не зависят от низкоуровневых модулей. Вместо этого оба уровня зависят от абстракций. Например,`AnimalFeedingOrchestrationService` зависит от класса `ZookeeperEmployee` и интерфейса `FeedableAnimal`.

## Ограничения и сложности при расширении

Текущая архитектура может столкнуться с трудностями в следующих сценариях:

1.  Если потребуется сложная логика кормления, текущий простой метод `scheduleAndExecuteFeeding` потребует достаточно большой переработки.
2.  Модель предполагает, что животные не имеют внутреннего состояния (например, голод, здоровье). Введение такого состояния потребует перепроектирования, чтобы не нарушить `SRP` или `LSP`.
3.  Добавление новой функции, затрагивающей многие роли (например, "транспортируемый"), может привести к загрязнению интерфейсов, если не управлять этим процессом более внимательно.

## Польза от применения SOLID принципов и абстракций высокого уровня качества дизайна

* Введенные абстракции значительно повышают качество проектирования за счет улучшения модульности, поддерживаемости и тестируемости. 
* Четкое разделение ответственностей и использование сегрегированных интерфейсов делают систему проще для понимания, модификации и также длаьнейшего расширения. 

## Структура модулей системы

| Модуль (Класс / Интерфейс)        | Тип               | Основная ответственность                                                         | Зависимости                                                |
| --------------------------------- | ----------------- | -------------------------------------------------------------------------------- | ---------------------------------------------------------- |
| AnimalInformationProvider         | Интерфейс         | Предоставление метаданных об идентификации животного (имя, вид)                  | —                                                          |
| FeedableAnimal                    | Интерфейс         | Определение контракта для кормления животного                                    | —                                                          |
| HealableAnimal                    | Интерфейс         | Определение контракта для проведения ветеринарного осмотра                       | —                                                          |
| CleanableEnclosure                | Интерфейс         | Определение контракта для уборки вольера                                         | —                                                          |
| ZooAnimal                         | Абстрактный класс | Хранение и предоставление неизменяемых идентификационных данных животного        | AnimalInformationProvider                                  |
| Parrot                            | Конкретный класс  | Реализация поведения попугая: кормление и осмотр                                 | ZooAnimal,FeedableAnimal,HealableAnimal                    |
| Crocodile                         | Конкретный класс  | Реализация поведения крокодила: кормление, осмотр и уборка вольера               | ZooAnimal,FeedableAnimal,HealableAnimal,CleanableEnclosure |
| ZookeeperEmployee                 | Конкретный класс  | Выполнение операции кормления животного                                          | FeedableAnimal                                             |
| VeterinarianEmployee              | Конкретный класс  | Выполнение ветеринарного осмотра                                                 | HealableAnimal                                             |
| EnclosureCleanerEmployee          | Конкретный класс  | Выполнение уборки вольера                                                        | CleanableEnclosure                                         |
| AnimalFeedingOrchestrationService | Конкретный класс  | Оркестрация процесса кормления: планирование и делегирование сотруднику          | ZookeeperEmployee,FeedableAnimal                           |
| VeterinaryMedicalCheckupService   | Конкретный класс  | Оркестрация процесса ветеринарного осмотра: инициация и делегирование сотруднику | VeterinarianEmployee,HealableAnimal                        |
| ZooManagementSystemDemo           | Главный класс     | Демонстрация работы системы: инициализация объектов и запуск бизнес-процессов    | Все вышеперечисленные классы и интерфейсы                  |

## Инструкция по запуску

Для удобства были реализованы 2 способа запуска данной программы:

1. Запуск вручную
2. Запуск скрипта

### Запуск вручную

1.  Убедитесь, что на вашем компьютере установлен компилятор Java (JDK 8 или выше).
2.  Сохраните исходный код в файл с именемZooManagementSystemDemo.java`.
3.  Откройте терминал или командную строку и перейдите в директорию, где находится файл.
4.  Выполните компиляцию:

```bash
 javac ZooManagementSystemDemo.java
```

5.  Запустите программу:

```bash
 java ZooManagementSystemDemo
```

### Запуск скрипта

Запустить полный процесс:

```bash
./run.sh
```

Запустить отдельные шаги (опционально):

```bash
source cleanup.sh               # Очистить предыдущую сборку
source check_dependencies.sh    # Проверить установку Java
source compile.sh               # Скомпилировать Java код
source run_program.sh           # Запустить программу
```

Что скрипт делает:

* Очищает старые .class файлы
* Проверяет установлен ли Java
* Компилирует все Java исходные файлы
* Запускает программу ZooManagementSystemDemo
* Показывает сводку выполнения
