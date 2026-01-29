# Задача: Настройка сетевого фильтра для сервера компании

Как запускать:

```
# 0. Удалите старые VM если были проблемы
vagrant destroy -f

# 1. Запустить сервер (самое главное)
vagrant up server

# 2. После успешного запуска сервера, запустить остальные:
vagrant up admin
vagrant up developer
vagrant up analyst
vagrant up blocked

# Или все сразу (но дольше):
vagrant up
```

## Описание задачи

Настройка UFW для виртуальной машины-сервера с мониторингом согласно требованиям.

## Требования к доступу

- **SSH (22)**: только с ПК администратора (192.168.55.90)
- **Prometheus (9090)**: только с ПК администратора и localhost
- **Node Exporter (9100)**: только с ПК администратора и localhost
- **Grafana (3000)**: отделы аналитиков (192.168.55.10-30) и разработчиков (192.168.55.91-128)
- **Все остальные входящие**: заблокированы
- **Исходящие соединения**: разрешены все

## Структура проекта

```
monitoring-firewall-task/
├── Vagrantfile
├── docker-compose.yml
├── prometheus/prometheus.yml
├── grafana/provisioning/
├── scripts/
└── README.md
```

## Запуск

### 1. Предварительные требования

- Установленный Vagrant
- Установленный VirtualBox

### 2. Клонирование и запуск

```bash
# Создаем директорию проекта
mkdir monitoring-task && cd monitoring-task

# Копируем все файлы из списка выше

# Запускаем все виртуальные машины
vagrant up

# Или только определенные:
vagrant up server      # Сервер компании
vagrant up admin       # ПК администратора
vagrant up developer   # ПК разработчика
vagrant up analyst     # ПК аналитика
vagrant up blocked     # Заблокированный ПК
```

### 3. Подключение и проверка

```bash
# Подключиться к серверу
vagrant ssh server

# Проверить UFW правила
sudo ufw status numbered

# Проверить запущенные сервисы
docker ps

# Подключиться к админскому ПК и протестировать
vagrant ssh admin
./test_admin.sh

# Подключиться к ПК разработчика и протестировать
vagrant ssh developer
./test_developer.sh
```

## Проверка работы

### Сервисы доступны по адресам:

1. **Grafana**: http://192.168.55.50:3000
   - Логин: `admin`
   - Пароль: `admin123`
   - Доступен только с разрешенных IP

2. **Prometheus**: http://192.168.55.50:9090
   - Доступен только с 192.168.55.90

3. **Node Exporter**: http://192.168.55.50:9100/metrics
   - Доступен только с 192.168.55.90

## Тестирование правил firewall

### Сценарии тестирования:

1. **С админского ПК (192.168.55.90)**:
   - ✓ SSH доступен
   - ✓ Prometheus доступен
   - ✓ Node Exporter доступен
   - ✗ Grafana НЕ доступен

2. **С ПК разработчика (192.168.55.91)**:
   - ✗ SSH НЕ доступен
   - ✗ Prometheus НЕ доступен
   - ✗ Node Exporter НЕ доступен
   - ✓ Grafana доступен

3. **С ПК аналитика (192.168.55.15)**:
   - ✗ SSH НЕ доступен
   - ✗ Prometheus НЕ доступен
   - ✗ Node Exporter НЕ доступен
   - ✓ Grafana доступен

4. **С заблокированного ПК (192.168.55.200)**:
   - ✗ Все порты заблокированы

## Остановка и очистка

```bash
# Остановить все машины
vagrant halt

# Приостановить все машины
vagrant suspend

# Удалить все машины
vagrant destroy -f

# Полная очистка
vagrant destroy -f && rm -rf .vagrant
```

## Устранение неполадок

1. **UFW не блокирует Docker порты**:

   ```bash
   # На сервере проверьте:
   sudo ufw status
   sudo iptables -L
   cat /etc/docker/daemon.json
   ```

2. **Сервисы не запускаются**:

   ```bash
   # Проверьте логи Docker
   docker logs prometheus
   docker logs grafana
   docker logs node-exporter
   ```

3. **Проблемы с сетью**:
   ```bash
   # Проверьте IP адреса
   ip addr show
   # Проверьте доступность
   ping 192.168.55.50
   ```

````

## **Инструкция по запуску**

### **Быстрый старт:**

```bash
# 1. Создайте папку проекта
mkdir monitoring-task
cd monitoring-task

# 2. Создайте все файлы из списка выше

# 3. Запустите виртуальные машины
vagrant up

# 4. После запуска проверьте:
# - Подключитесь к серверу: vagrant ssh server
# - Проверьте правила: sudo ufw status numbered
# - Проверьте сервисы: docker ps

# 5. Протестируйте с разных машин:
vagrant ssh admin
./test_admin.sh

vagrant ssh developer
./test_developer.sh
````

### **Проверка через браузер (с хостовой машины):**

Если хотите проверить через браузер на основной системе, добавьте в Vagrantfile проброс портов:

```ruby
server.vm.network "forwarded_port", guest: 3000, host: 8300  # Grafana
server.vm.network "forwarded_port", guest: 9090, host: 8900  # Prometheus
```

Тогда будет доступно:

- Grafana: http://localhost:8300
- Prometheus: http://localhost:8900

## **Что будет настроено автоматически:**

1. **Сервер компании (192.168.55.50)**:
   - Docker + Docker Compose
   - UFW с правилами согласно задаче
   - Prometheus на порту 9090
   - Grafana на порту 3000 (admin/admin123)
   - Node Exporter на порту 9100

2. **Тестовые машины**:
   - Администратор (192.168.55.90)
   - Разработчик (192.168.55.91)
   - Аналитик (192.168.55.15)
   - Заблокированный ПК (192.168.55.200)

3. **Автоматические тесты**:
   - Скрипты для проверки правил firewall
   - Проверка доступности сервисов
