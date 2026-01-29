#!/bin/bash

SERVER_IP="192.168.55.50"

echo "=========================================="
echo "ТЕСТИРОВАНИЕ С ПК РАЗРАБОТЧИКА"
echo "IP: $(hostname -I | awk '{print $2}')"
echo "Сервер: $SERVER_IP"
echo "=========================================="
echo ""

echo "1. Проверка Grafana (должен быть ОТКРЫТ):"
echo -n "   HTTP запрос: "
if curl -s -o /dev/null -w "%{http_code}" http://$SERVER_IP:3000; then
    echo -e " \e[32mУСПЕШНО\e[0m"
else
    echo -e " \e[31mОШИБКА\e[0m"
fi
echo ""

echo "2. Проверка SSH (должен быть ЗАКРЫТ):"
echo -n "   Попытка подключения: "
if timeout 2 nc -z $SERVER_IP 22 2>/dev/null; then
    echo -e "\e[31mДОСТУПЕН (ОШИБКА!)\e[0m"
else
    echo -e "\e[32mЗАБЛОКИРОВАН (правильно)\e[0m"
fi
echo ""

echo "3. Проверка Prometheus (должен быть ЗАКРЫТ):"
echo -n "   Попытка доступа: "
if timeout 2 curl -s http://$SERVER_IP:9090 > /dev/null; then
    echo -e "\e[31mДОСТУПЕН (ОШИБКА!)\e[0m"
else
    echo -e "\e[32mЗАБЛОКИРОВАН (правильно)\e[0m"
fi
echo ""

echo "4. Проверка Node Exporter (должен быть ЗАКРЫТ):"
echo -n "   Попытка доступа: "
if timeout 2 curl -s http://$SERVER_IP:9100 > /dev/null; then
    echo -e "\e[31mДОСТУПЕН (ОШИБКА!)\e[0m"
else
    echo -e "\e[32mЗАБЛОКИРОВАН (правильно)\e[0m"
fi

echo ""
echo "=========================================="
echo "ТЕСТИРОВАНИЕ ЗАВЕРШЕНО"
echo "=========================================="