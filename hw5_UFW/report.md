# Отчет по домашней работе. Настроить сетевой фильтр с помощью ufw

## Архитектура системы

```mermaid
graph TB
    subgraph "Хостовая машина"
        HOST[Хост Windows]
        VAGRANT[Vagrant]
        VBOX[VirtualBox]
    end

    subgraph "Виртуальная сеть 192.168.55.0/24"
        subgraph "Сервер компании 192.168.55.50"
            SERVER[Сервер Ubuntu 22.04]

            subgraph "Docker контейнеры"
                GRAFANA[Grafana:3000]
                PROM[Prometheus:9090]
                NODE[Node Exporter:9100]
            end

            UFW[UFW Firewall]

            SERVER --> GRAFANA
            SERVER --> PROM
            SERVER --> NODE
            SERVER --> UFW
        end

        subgraph "ПК Администратора 192.168.55.90"
            ADMIN[Админский ПК]
            ADMIN_SCRIPT[test_admin.sh]
            ADMIN --> ADMIN_SCRIPT
        end

        subgraph "ПК Разработчика 192.168.55.91"
            DEV[ПК разработчика]
            DEV_SCRIPT[test_developer.sh]
            DEV --> DEV_SCRIPT
        end

        subgraph "ПК Аналитика 192.168.55.15"
            ANALYST[ПК аналитика]
        end

    end

    %% Соединения управления
    HOST --> VAGRANT
    VAGRANT --> VBOX
    VBOX --> SERVER
    VBOX --> ADMIN
    VBOX --> DEV
    VBOX --> ANALYST


    %% Правила доступа UFW
    UFW_RULES["
    ПРАВИЛА UFW:
    1. SSH (22) ← 192.168.55.90
    2. Prometheus (9090) ← 192.168.55.90
    3. Node Exporter (9100) ← 192.168.55.90
    4. Grafana (3000) ← 192.168.55.10-30
    5. Grafana (3000) ← 192.168.55.91-128
    6. Default: DENY INCOMING
    7. Default: ALLOW OUTGOING
    "]

    UFW --> UFW_RULES

    %% Соединения доступа
    ADMIN -- "SSH:22 OK<br/>Prometheus:9090 OK<br/>Node Exporter:9100 OK" --> SERVER
    DEV -- "Grafana:3000 OK" --> SERVER
    ANALYST -- "Grafana:3000 OK" --> SERVER


```

## Процесс выполнения работы

```mermaid
flowchart TD
    START[Запуск лабораторной работы] --> VAGRANT_CMD[Выполнение vagrant up]

    VAGRANT_CMD --> INIT_VM[Инициализация виртуальных машин]

    INIT_VM --> SERVER_SETUP[Настройка сервера компании<br/>IP: 192.168.55.50]
    INIT_VM --> ADMIN_SETUP[Настройка ПК администратора<br/>IP: 192.168.55.90]
    INIT_VM --> DEV_SETUP[Настройка ПК разработчика<br/>IP: 192.168.55.91]
    INIT_VM --> ANALYST_SETUP[Настройка ПК аналитика<br/>IP: 192.168.55.15]
    INIT_VM --> BLOCKED_SETUP[Настройка заблокированного ПК<br/>IP: 192.168.55.200]

    %% Настройка сервера
    subgraph SERVER_CONFIG[Процесс настройки сервера]
        direction LR
        S1[Этап 1: Ожидание сети] -->
        S2[Этап 2: Установка пакетов] -->
        S3[Этап 3: Настройка UFW] -->
        S4[Этап 4: Установка Docker] -->
        S5[Этап 5: Запуск сервисов] -->
        S6[Этап 6: Проверка]
    end

    SERVER_SETUP --> SERVER_CONFIG
    SERVER_CONFIG --> SERVER_READY[Сервер готов]

    SERVER_READY --> UFW_ACTIVE[UFW активен с правилами]
    SERVER_READY --> SERVICES_ACTIVE[Сервисы запущены]

    %% Настройка клиентских машин
    ADMIN_SETUP --> ADMIN_READY[Админский ПК готов]
    DEV_SETUP --> DEV_READY[ПК разработчика готов]
    ANALYST_SETUP --> ANALYST_READY[ПК аналитика готов]
    BLOCKED_SETUP --> BLOCKED_READY[Заблокированный ПК готов]

    %% Тестирование
    ADMIN_READY --> TEST_ADMIN[Тестирование с админского ПК]
    DEV_READY --> TEST_DEV[Тестирование с ПК разработчика]
    ANALYST_READY --> TEST_ANALYST[Тестирование с ПК аналитика]
    BLOCKED_READY --> TEST_BLOCKED[Тестирование с заблокированного ПК]

    %% Итог
    TEST_ADMIN --> RESULT[Работа завершена успешно]
    TEST_DEV --> RESULT
    TEST_ANALYST --> RESULT
    TEST_BLOCKED --> RESULT
```

## Последовательность взаимодействия

```mermaid
sequenceDiagram
    participant H as Хост Windows
    participant V as Vagrant
    participant VB as VirtualBox
    participant S as Сервер 192.168.55.50
    participant A as Админ 192.168.55.90
    participant D as Разработчик 192.168.55.91

    H->>V: vagrant up server
    V->>VB: Создание VM
    VB->>S: Загрузка Ubuntu
    S-->>VB: Система загружена
    VB-->>V: VM готова
    V-->>H: Сервер запущен

    Note over S: Настройка UFW
    S->>S: ufw --force reset
    S->>S: ufw default deny incoming
    S->>S: ufw default allow outgoing
    S->>S: Добавление правил доступа

    Note over S: Запуск сервисов
    S->>S: docker run grafana:3000
    S->>S: docker run prometheus:9090
    S->>S: docker run node-exporter:9100

    H->>V: vagrant up admin
    V->>VB: Создание админской VM
    VB->>A: Загрузка и настройка
    A-->>H: Админ готов

    H->>V: vagrant up developer
    V->>VB: Создание VM разработчика
    VB->>D: Загрузка и настройка
    D-->>H: Разработчик готов

    Note over A,S: Тестирование
    A->>S: Запрос SSH:22
    S->>S: UFW: Проверка правила
    S-->>A: Разрешено

    A->>S: Запрос Grafana:3000
    S->>S: UFW: Проверка правила
    S-->>A: Заблокировано

    D->>S: Запрос SSH:22
    S->>S: UFW: Проверка правила
    S-->>D: Заблокировано

    D->>S: Запрос Grafana:3000
    S->>S: UFW: Проверка правила
    S-->>D: Разрешено
```

## Технические детали реализации

### 1. Конфигурация виртуальных машин

Сервер компании (192.168.55.50):

- Операционная система: Ubuntu 22.04
- Ресурсы: 2 CPU, 2GB RAM
- Установленные сервисы:
  - UFW (Uncomplicated Firewall)
  - Docker CE
  - Grafana (порт 3000)
  - Prometheus (порт 9090)
  - Node Exporter (порт 9100)

Клиентские машины:

- Все машины: Ubuntu 22.04, 1 CPU, 512MB RAM
- ПК администратора: 192.168.55.90
- ПК разработчика: 192.168.55.91
- ПК аналитика: 192.168.55.15
- Заблокированный ПК: 192.168.55.200

### 2. Правила UFW

```bash
ufw default deny incoming
ufw default allow outgoing

ufw allow from 192.168.55.90 to any port 22 proto tcp 
ufw allow from 192.168.55.90 to any port 9090 proto tcp
ufw allow from 192.168.55.90 to any port 9100 proto tcp
ufw allow from 127.0.0.1 to any port 9090 proto tcp
ufw allow from 127.0.0.1 to any port 9100 proto tcp
ufw allow from 192.168.55.10/28 to any port 3000 proto tcp
ufw allow from 192.168.55.91/27 to any port 3000 proto tcp
```

### 3. Этапы настройки сервера

1. Инициализация сети - ожидание полной загрузки сетевых интерфейсов
2. Установка пакетов - обновление системы и установка необходимого ПО
3. Настройка UFW - применение правил фаервола согласно заданию
4. Установка Docker - установка Docker для запуска сервисов мониторинга
5. Запуск сервисов - запуск Grafana, Prometheus и Node Exporter в контейнерах
6. Финальная проверка - проверка доступности всех сервисов и правил UFW

### 4. Результаты тестирования

С админского ПК (192.168.55.90):

- SSH (22) - доступен
- Prometheus (9090) - доступен
- Node Exporter (9100) - доступен
- Grafana (3000) - заблокирован (соответствует требованиям)

С ПК разработчика (192.168.55.91):

- SSH (22) - заблокирован
- Prometheus (9090) - заблокирован
- Node Exporter (9100) - заблокирован
- Grafana (3000) - доступен (соответствует требованиям)

С ПК аналитика (192.168.55.15):

- SSH (22) - заблокирован
- Prometheus (9090) - заблокирован
- Node Exporter (9100) - заблокирован
- Grafana (3000) - доступен (соответствует требованиям)

С заблокированного ПК (192.168.55.200):

- Все порты заблокированы (соответствует требованиям)

### 5. Верификация результатов

1. Проверка правил UFW:

```bash
sudo ufw status verbose
```

2. Проверка запущенных сервисов:

```bash
sudo docker ps
```

3. Проверка доступности сервисов:

- Grafana: http://192.168.55.50:3000 (admin/admin123)
- Prometheus: http://192.168.55.50:9090
- Node Exporter: http://192.168.55.50:9100/metrics
