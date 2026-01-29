#!/bin/bash

# ==================== КОНФИГУРАЦИЯ ====================
SERVER_IP="192.168.55.50"
ADMIN_IP="192.168.55.90"
ANALYTICS_NETWORK="192.168.55.10/28"      # 192.168.55.10-192.168.55.30
DEVELOPERS_NETWORK="192.168.55.91/27"     # 192.168.55.91-192.168.55.128

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Настройка сервера компании ===${NC}"
echo "IP адрес: $SERVER_IP"

# ==================== 1. ОБНОВЛЕНИЕ СИСТЕМЫ ====================
echo -e "\n${YELLOW}[1/6] Обновление системы...${NC}"
apt-get update
apt-get upgrade -y
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    software-properties-common \
    gnupg \
    lsb-release

# ==================== 2. УСТАНОВКА DOCKER ====================
echo -e "\n${YELLOW}[2/6] Установка Docker...${NC}"
# Удаляем старые версии
apt-get remove -y docker docker-engine docker.io containerd runc

# Устанавливаем Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Проверяем установку
docker --version
docker-compose --version

# ==================== 3. НАСТРОЙКА DOCKER ДЛЯ UFW ====================
echo -e "\n${YELLOW}[3/6] Настройка Docker для работы с UFW...${NC}"
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << EOF
{
  "iptables": false,
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

systemctl restart docker
systemctl enable docker

# ==================== 4. НАСТРОЙКА UFW ====================
echo -e "\n${YELLOW}[4/6] Настройка фаервола UFW...${NC}"
apt-get install -y ufw

# Сбрасываем UFW
ufw --force reset

# Базовые политики
ufw default deny incoming
ufw default allow outgoing

# Правила согласно задаче:
echo -e "${GREEN}Добавление правил UFW:${NC}"

# SSH только с админа
ufw allow from $ADMIN_IP to any port 22 proto tcp
echo "✓ SSH (22) разрешен только с $ADMIN_IP"

# Prometheus только с админа
ufw allow from $ADMIN_IP to any port 9090 proto tcp
ufw allow from 127.0.0.1 to any port 9090 proto tcp
echo "✓ Prometheus (9090) разрешен только с $ADMIN_IP и localhost"

# Node Exporter только с админа
ufw allow from $ADMIN_IP to any port 9100 proto tcp
ufw allow from 127.0.0.1 to any port 9100 proto tcp
echo "✓ Node Exporter (9100) разрешен только с $ADMIN_IP и localhost"

# Grafana для аналитиков и разработчиков
ufw allow from $ANALYTICS_NETWORK to any port 3000 proto tcp
ufw allow from $DEVELOPERS_NETWORK to any port 3000 proto tcp
echo "✓ Grafana (3000) разрешен для сетей:"
echo "  - $ANALYTICS_NETWORK (аналитики)"
echo "  - $DEVELOPERS_NETWORK (разработчики)"

# Включаем UFW
echo "y" | ufw enable

# Статус UFW
echo -e "\n${GREEN}Статус UFW:${NC}"
ufw status numbered

# ==================== 5. НАСТРОЙКА СЕРВИСОВ ====================
echo -e "\n${YELLOW}[5/6] Настройка сервисов мониторинга...${NC}"

# Создаем директории
mkdir -p /opt/monitoring/{prometheus,grafana/provisioning/{datasources,dashboards}}

# Копируем конфиги
cp /tmp/prometheus/prometheus.yml /opt/monitoring/prometheus/
cp -r /tmp/grafana/provisioning/* /opt/monitoring/grafana/provisioning/
cp /tmp/docker-compose.yml /opt/monitoring/

# Запускаем Docker Compose
cd /opt/monitoring
docker-compose up -d

# Ждем запуска сервисов
echo -e "\n${YELLOW}Ожидание запуска сервисов...${NC}"
sleep 10

# Проверяем запущенные контейнеры
echo -e "\n${GREEN}Запущенные контейнеры:${NC}"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# ==================== 6. ПРОВЕРКА И ИНФОРМАЦИЯ ====================
echo -e "\n${YELLOW}[6/6] Финальная проверка...${NC}"

# Функция проверки порта
check_port() {
    local port=$1
    local service=$2
    if nc -z localhost $port 2>/dev/null; then
        echo -e "✓ $service (порт $port): ${GREEN}работает${NC}"
        return 0
    else
        echo -e "✗ $service (порт $port): ${RED}не работает${NC}"
        return 1
    fi
}

# Проверяем порты
check_port 22 "SSH"
check_port 3000 "Grafana"
check_port 9090 "Prometheus"
check_port 9100 "Node Exporter"

# Информация для доступа
echo -e "\n${GREEN}=== ИНФОРМАЦИЯ ДЛЯ ДОСТУПА ===${NC}"
echo "IP сервера: $SERVER_IP"
echo ""
echo "1. SSH:"
echo "   Порт: 22"
echo "   Доступен только с: $ADMIN_IP"
echo ""
echo "2. Grafana:"
echo "   URL: http://$SERVER_IP:3000"
echo "   Логин: admin"
echo "   Пароль: admin123"
echo "   Доступен с сетей:"
echo "     - $ANALYTICS_NETWORK (аналитики)"
echo "     - $DEVELOPERS_NETWORK (разработчики)"
echo ""
echo "3. Prometheus:"
echo "   URL: http://$SERVER_IP:9090"
echo "   Доступен только с: $ADMIN_IP и localhost"
echo ""
echo "4. Node Exporter:"
echo "   URL: http://$SERVER_IP:9100/metrics"
echo "   Доступен только с: $ADMIN_IP и localhost"
echo ""
echo "5. UFW статус:"
ufw status | head -20

echo -e "\n${GREEN}=== НАСТРОЙКА ЗАВЕРШЕНА ===${NC}"