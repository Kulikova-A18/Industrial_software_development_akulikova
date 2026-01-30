#!/bin/bash

SERVER_IP="192.168.55.50"

test_port() {
    local port=$1
    local service=$2
    local expected=$3
    
    if timeout 2 nc -z $SERVER_IP $port 2>/dev/null; then
        if [ "$expected" = "open" ]; then
            echo "$service (порт $port): ОТКРЫТ"
            return 0
        else
            echo "$service (порт $port): ОТКРЫТ (НО НЕ ДОЛЖЕН БЫТЬ!)"
            return 1
        fi
    else
        if [ "$expected" = "closed" ]; then
            echo "$service (порт $port): ЗАКРЫТ"
            return 0
        else
            echo "$service (порт $port): ЗАКРЫТ (НО ДОЛЖЕН БЫТЬ ОТКРЫТ!)"
            return 1
        fi
    fi
}

test_port 22 "SSH" "open"

if curl -s http://$SERVER_IP:9090 > /dev/null; then
    echo "Prometheus UI: ДОСТУПЕН"
else
    echo "Prometheus UI: НЕ ДОСТУПЕН"
fi

if curl -s http://$SERVER_IP:9100/metrics | head -1 > /dev/null; then
    echo "Node Exporter: ДОСТУПЕН"
else
    echo "Node Exporter: НЕ ДОСТУПЕН"
fi

test_port 3000 "Grafana" "closed"

if ping -c 2 8.8.8.8 > /dev/null; then
    echo "Исходящие соединения: РАБОТАЮТ"
else
    echo "Исходящие соединения: НЕ РАБОТАЮТ"
fi