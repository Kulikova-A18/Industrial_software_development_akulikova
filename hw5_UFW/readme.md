# Домашнее задание. Настроить сетевой фильтр с помощью ufw

## Важно

**Сам отчет** находится в [Отчет по домашней работе](report.md)

## Вводная часть 

> Логин на VM: vagrant, Пароль: vagrant

Как запускать:

```
# 0. Удалить старые VM
vagrant destroy -f

# 1. Запустить сервер (самое главное)
vagrant up server

# 2. После успешного запуска сервера, запустить остальные:
vagrant up admin
vagrant up developer
vagrant up analyst

# Или все сразу (но дольше):
vagrant up
```

## Описание задачи

Настройка UFW для виртуальной машины-сервера с мониторингом согласно требованиям.

## Требования к доступу

- SSH (22): только с ПК администратора (192.168.55.90)
- Prometheus (9090): только с ПК администратора и localhost
- Node Exporter (9100): только с ПК администратора и localhost
- Grafana (3000): отделы аналитиков (192.168.55.10-30) и разработчиков (192.168.55.91-128)
- Все остальные входящие: заблокированы
- Исходящие соединения: разрешены все

## Запуск

### Предварительные требования

- Установленный Vagrant
- Установленный VirtualBox

### Подключение и проверка

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

1. Grafana: http://192.168.55.50:3000
   - Логин: `admin`
   - Пароль: `admin123`
   - Доступен только с разрешенных IP

2. Prometheus: http://192.168.55.50:9090
   - Доступен только с 192.168.55.90

3. Node Exporter: http://192.168.55.50:9100/metrics
   - Доступен только с 192.168.55.90

## Тестирование правил firewall

### Сценарии тестирования:

1. С админского ПК (192.168.55.90):
   - [yes] SSH доступен
   - [yes] Prometheus доступен
   - [yes] Node Exporter доступен
   - [no] Grafana НЕ доступен

2. С ПК разработчика (192.168.55.91):
   - [no] SSH НЕ доступен
   - [no] Prometheus НЕ доступен
   - [no] Node Exporter НЕ доступен
   - [yes] Grafana доступен

3. С ПК аналитика (192.168.55.15):
   - [no] SSH НЕ доступен
   - [no] Prometheus НЕ доступен
   - [no] Node Exporter НЕ доступен
   - [yes] Grafana доступен

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

1. UFW не блокирует Docker порты:

```bash
# На сервере!
sudo ufw status
sudo iptables -L
cat /etc/docker/daemon.json
```

2. Сервисы не запускаются:

```bash
docker logs prometheus
docker logs grafana
docker logs node-exporter
```

3. Проблемы с сетью:

```bash
ip addr show
ping 192.168.55.50
```
