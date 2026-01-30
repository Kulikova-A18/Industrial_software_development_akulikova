#!/bin/bash

SERVER_IP="192.168.55.50"
ADMIN_IP="192.168.55.90"
ANALYTICS_NETWORK="192.168.55.10/28"
DEVELOPERS_NETWORK="192.168.55.91/27"

apt-get update
apt-get upgrade -y
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    software-properties-common \
    gnupg \
    lsb-release

apt-get remove -y docker docker-engine docker.io containerd runc

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

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

apt-get install -y ufw

ufw --force reset

ufw default deny incoming
ufw default allow outgoing

ufw allow from $ADMIN_IP to any port 22 proto tcp
ufw allow from $ADMIN_IP to any port 9090 proto tcp
ufw allow from 127.0.0.1 to any port 9090 proto tcp
ufw allow from $ADMIN_IP to any port 9100 proto tcp
ufw allow from 127.0.0.1 to any port 9100 proto tcp
ufw allow from $ANALYTICS_NETWORK to any port 3000 proto tcp
ufw allow from $DEVELOPERS_NETWORK to any port 3000 proto tcp

echo "y" | ufw enable

mkdir -p /opt/monitoring/{prometheus,grafana/provisioning/{datasources,dashboards}}

cp /tmp/prometheus/prometheus.yml /opt/monitoring/prometheus/
cp -r /tmp/grafana/provisioning/* /opt/monitoring/grafana/provisioning/
cp /tmp/docker-compose.yml /opt/monitoring/

cd /opt/monitoring
docker-compose up -d

sleep 30

check_port() {
    local port=$1
    local service=$2
    if nc -z localhost $port 2>/dev/null; then
        echo "$service (порт $port): работает"
        return 0
    else
        echo "$service (порт $port): не работает"
        return 1
    fi
}

check_port 22 "SSH"
check_port 3000 "Grafana"
check_port 9090 "Prometheus"
check_port 9100 "Node Exporter"