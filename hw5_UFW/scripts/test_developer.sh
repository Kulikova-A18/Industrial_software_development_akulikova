#!/bin/bash

SERVER_IP="192.168.55.50"

echo "Проверка Grafana"
if curl -s -o /dev/null -w "%{http_code}" http://$SERVER_IP:3000; then
    echo "Grafana: ДОСТУПЕН"
else
    echo "Grafana: ОШИБКА"
fi

echo "Проверка SSH"
if timeout 2 nc -z $SERVER_IP 22 2>/dev/null; then
    echo "SSH: ДОСТУПЕН"
else
    echo "SSH: ЗАБЛОКИРОВАН"
fi

echo "Проверка Prometheus"
if timeout 2 curl -s http://$SERVER_IP:9090 > /dev/null; then
    echo "Prometheus: ДОСТУПЕН"
else
    echo "Prometheus: ЗАБЛОКИРОВАН"
fi

echo "Проверка Node Exporter"
if timeout 2 curl -s http://$SERVER_IP:9100 > /dev/null; then
    echo "Node Exporter: ДОСТУПЕН"
else
    echo "Node Exporter: ЗАБЛОКИРОВАН"
fi