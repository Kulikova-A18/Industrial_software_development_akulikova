#!/bin/bash

SERVER_IP="192.168.55.50"

echo "=========================================="
echo "ТЕСТИРОВАНИЕ С АДМИНИСТРАТОРСКОГО ПК"
echo "IP: $(hostname -I | awk '{print $2}')"
echo "Сервер: $SERVER_IP"
echo "=========================================="
echo ""

# Функция проверки порта
test_port() {
    local port=$1
    local service=$2
    local expected=$3
    
    echo -n "Порт $port ($service): "
    if timeout 2 nc -z $SERVER_IP $port 2>/dev/null; then
        if [ "$expected" = "open" ]; then
            echo -e "\e[32mОТКРЫТ (как и должно быть)\e[0m"
            return 0
        else
            echo -e "\e[31mОТКРЫТ (НО НЕ ДОЛЖЕН БЫТЬ!)\e[0m"
            return 1
        fi
    else
        if [ "$expected" = "closed" ]; then
            echo -e "\e[32mЗАКРЫТ (как и должно быть)\e[0m"
            return 0
        else
            echo -e "\e[31mЗАКРЫТ (НО ДОЛЖЕН БЫТЬ ОТКРЫТ!)\e[0m"
            return 1
        fi
    fi
}

# Проверяем порты согласно задаче:
echo "1. Проверка SSH (должен быть ОТКРЫТ):"
test_port 22 "SSH" "open"
echo ""

echo "2. Проверка Prometheus (должен быть ОТКРЫТ):"
if curl -s http://$SERVER_IP:9090 > /dev/null; then
    echo -e "   Prometheus UI: \e[32mДОСТУПЕН\e[0m"
else
    echo -e "   Prometheus UI: \e[31mНЕ ДОСТУПЕН\e[0m"
fi
echo ""

echo "3. Проверка Node Exporter (должен быть ОТКРЫТ):"
if curl -s http://$SERVER_IP:9100/metrics | head -1 > /dev/null; then
    echo -e "   Node Exporter: \e[32mДОСТУПЕН\e[0m"
else
    echo -e "   Node Exporter: \e[31mНЕ ДОСТУПЕН\e[0m"
fi
echo ""

echo "4. Проверка Grafana (должен быть ЗАКРЫТ для админа):"
test_port 3000 "Grafana" "closed"
echo ""

echo "5. Проверка исходящих соединений:"
if ping -c 2 8.8.8.8 > /dev/null; then
    echo -e "   Исходящие соединения: \e[32mРАБОТАЮТ\e[0m"
else
    echo -e "   Исходящие соединения: \e[31mНЕ РАБОТАЮТ\e[0m"
fi

echo ""
echo "=========================================="
echo "ТЕСТИРОВАНИЕ ЗАВЕРШЕНО"
echo "=========================================="