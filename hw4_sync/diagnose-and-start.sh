#!/bin/bash

# ============================================================
# Скрипт диагностики и запуска КосмоСкан
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║   Диагностика и запуск КосмоСкан                        ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

# 1. Проверка директории проекта
echo -e "${BLUE}[1/7] Проверка структуры проекта...${NC}"

REQUIRED_DIRS=("api-gateway" "file-storing-service" "file-analysis-service" "cosmoscan-ui")
MISSING_DIRS=()

for dir in "${REQUIRED_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo -e "  ${GREEN}✓${NC} $dir"
    else
        echo -e "  ${RED}✗${NC} $dir - отсутствует"
        MISSING_DIRS+=("$dir")
    fi
done

if [ ${#MISSING_DIRS[@]} -gt 0 ]; then
    echo ""
    echo -e "${RED}✗ Отсутствуют директории: ${MISSING_DIRS[*]}${NC}"
    echo -e "${YELLOW}Сначала создайте структуру проекта скриптом create-cosmoscan-project.sh${NC}"
    exit 1
fi

echo ""

# 2. Проверка наличия pom.xml в каждом сервисе
echo -e "${BLUE}[2/7] Проверка файлов проектов...${NC}"

for dir in "${REQUIRED_DIRS[@]}"; do
    if [ -f "$dir/pom.xml" ]; then
        echo -e "  ${GREEN}✓${NC} $dir/pom.xml"
    else
        echo -e "  ${RED}✗${NC} $dir/pom.xml - отсутствует"
        MISSING_DIRS+=("$dir/pom.xml")
    fi
done

echo ""

# 3. Проверка Docker
echo -e "${BLUE}[3/7] Проверка Docker...${NC}"

DOCKER_RUNNING=false
if docker info &> /dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} Docker запущен"
    DOCKER_RUNNING=true
else
    echo -e "  ${YELLOW}⚠${NC} Docker не запущен"
    
    # Попытка запустить Docker
    if command -v systemctl &> /dev/null; then
        echo -e "  ${BLUE}  Попытка запуска Docker...${NC}"
        if sudo systemctl start docker 2>/dev/null; then
            sleep 2
            if docker info &> /dev/null 2>&1; then
                echo -e "  ${GREEN}✓${NC} Docker успешно запущен"
                DOCKER_RUNNING=true
            fi
        else
            echo -e "  ${RED}✗${NC} Не удалось запустить Docker"
            echo -e "  ${YELLOW}  Запустите Docker вручную: sudo systemctl start docker${NC}"
        fi
    fi
fi

echo ""

# 4. Проверка собранных JAR файлов
echo -e "${BLUE}[4/7] Проверка собранных JAR файлов...${NC}"

NEED_BUILD=false
SERVICES=(
    "api-gateway:target/api-gateway-1.0.0.jar"
    "file-storing-service:target/file-storing-service-1.0.0.jar"
    "file-analysis-service:target/file-analysis-service-1.0.0.jar"
    "cosmoscan-ui:target/cosmoscan-ui-1.0.0.jar"
)

for service_info in "${SERVICES[@]}"; do
    IFS=':' read -r service jar <<< "$service_info"
    if [ -f "$service/$jar" ]; then
        echo -e "  ${GREEN}✓${NC} $service собран"
    else
        echo -e "  ${YELLOW}○${NC} $service не собран"
        NEED_BUILD=true
    fi
done

echo ""

# 5. Сборка если нужно
if $NEED_BUILD; then
    echo -e "${BLUE}[5/7] Сборка проектов...${NC}"
    echo ""
    
    for dir in "${REQUIRED_DIRS[@]}"; do
        echo -e "  ${BLUE}Сборка $dir...${NC}"
        if [ -d "$dir" ]; then
            cd "$dir"
            
            # Очистка и сборка
            if mvn clean package -DskipTests 2>&1 | tee /tmp/mvn_build.log | grep -E "(BUILD SUCCESS|BUILD FAILURE|ERROR)"; then
                if grep -q "BUILD SUCCESS" /tmp/mvn_build.log; then
                    echo -e "  ${GREEN}✓${NC} $dir собран успешно"
                else
                    echo -e "  ${RED}✗${NC} Ошибка сборки $dir"
                    echo ""
                    echo -e "${YELLOW}Последние строки лога:${NC}"
                    tail -20 /tmp/mvn_build.log
                    cd ..
                    exit 1
                fi
            else
                echo -e "  ${RED}✗${NC} Ошибка сборки $dir"
                cd ..
                exit 1
            fi
            
            cd ..
            echo ""
        fi
    done
else
    echo -e "${BLUE}[5/7] Сборка не требуется${NC}"
    echo ""
fi

# 6. Запуск Docker контейнеров
if $DOCKER_RUNNING; then
    echo -e "${BLUE}[6/7] Запуск Docker контейнеров...${NC}"
    echo ""
    
    # Останавливаем существующие контейнеры если есть
    if docker compose ps 2>/dev/null | grep -q "Up"; then
        echo -e "  ${YELLOW}Останавливаем существующие контейнеры...${NC}"
        docker compose down
        sleep 2
    fi
    
    # Исправляем docker-compose.yml (убираем version)
    if [ -f "docker-compose.yml" ]; then
        if grep -q "^version:" docker-compose.yml; then
            echo -e "  ${BLUE}Исправляем docker-compose.yml...${NC}"
            sed -i '/^version:/d' docker-compose.yml
            echo -e "  ${GREEN}✓${NC} Строка 'version' удалена"
        fi
    fi
    
    # Запускаем контейнеры
    echo -e "  ${BLUE}Запуск контейнеров...${NC}"
    if docker compose up -d --build 2>&1 | tee /tmp/docker_up.log; then
        echo ""
        echo -e "  ${GREEN}✓${NC} Контейнеры запущены"
    else
        echo -e "  ${RED}✗${NC} Ошибка запуска контейнеров"
        echo ""
        echo -e "${YELLOW}Лог ошибок:${NC}"
        tail -30 /tmp/docker_up.log
        exit 1
    fi
    
    # Показываем статус контейнеров
    echo ""
    echo -e "${BLUE}Статус контейнеров:${NC}"
    docker compose ps
    
else
    echo -e "${BLUE}[6/7] Docker недоступен, пропускаем${NC}"
    echo -e "${YELLOW}  Сервисы можно запустить вручную без Docker${NC}"
    echo ""
fi

# 7. Проверка готовности
if $DOCKER_RUNNING; then
    echo ""
    echo -e "${BLUE}[7/7] Проверка готовности сервисов...${NC}"
    echo ""
    
    MAX_ATTEMPTS=30
    ATTEMPT=1
    ALL_READY=false
    
    while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
        GATEWAY_OK=false
        STORING_OK=false
        ANALYSIS_OK=false
        
        # Проверка API Gateway
        if curl -s --connect-timeout 2 http://localhost:8080/health-check > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} API Gateway (8080) готов"
            GATEWAY_OK=true
        else
            echo -e "  ${YELLOW}○${NC} API Gateway (8080) ожидание..."
        fi
        
        # Проверка File Storing Service
        if curl -s --connect-timeout 2 http://localhost:8081/actuator/health > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} File Storing Service (8081) готов"
            STORING_OK=true
        fi
        
        # Проверка File Analysis Service
        if curl -s --connect-timeout 2 http://localhost:8082/api/internal/health > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} File Analysis Service (8082) готов"
            ANALYSIS_OK=true
        fi
        
        if $GATEWAY_OK && $STORING_OK && $ANALYSIS_OK; then
            ALL_READY=true
            break
        fi
        
        echo ""
        echo -e "${BLUE}  Попытка $ATTEMPT/$MAX_ATTEMPTS. Ожидание 5 секунд...${NC}"
        sleep 5
        ((ATTEMPT++))
    done
    
    echo ""
    if $ALL_READY; then
        echo -e "${GREEN}╔══════════════════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║   ✓ Все сервисы запущены и готовы к работе!              ║${NC}"
        echo -e "${GREEN}╠══════════════════════════════════════════════════════════╣${NC}"
        echo -e "${GREEN}║   API Gateway:        http://localhost:8080              ║${NC}"
        echo -e "${GREEN}║   File Storing:       http://localhost:8081              ║${NC}"
        echo -e "${GREEN}║   File Analysis:      http://localhost:8082              ║${NC}"
        echo -e "${GREEN}║   Health Check:       http://localhost:8080/health-check ║${NC}"
        echo -e "${GREEN}╚══════════════════════════════════════════════════════════╝${NC}"
        
        # Тестовый запрос
        echo ""
        echo -e "${BLUE}Тестовый запрос к API Gateway:${NC}"
        curl -s http://localhost:8080/health-check | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8080/health-check
        
        # Запуск UI
        echo ""
        echo ""
        echo -n "Запустить CosmoScan UI? (Y/n): "
        read answer
        if [ -z "$answer" ] || [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
            if [ -f "cosmoscan-ui/target/cosmoscan-ui-1.0.0.jar" ]; then
                echo -e "${BLUE}Запуск UI...${NC}"
                java -jar cosmoscan-ui/target/cosmoscan-ui-1.0.0.jar &
                UI_PID=$!
                echo $UI_PID > .ui_pid
                echo -e "${GREEN}✓ UI запущен (PID: $UI_PID)${NC}"
                echo ""
                echo -e "${YELLOW}Для остановки UI выполните: kill $UI_PID${NC}"
            else
                echo -e "${RED}✗ JAR файл UI не найден${NC}"
            fi
        fi
        
    else
        echo -e "${YELLOW}╔══════════════════════════════════════════════════════════╗${NC}"
        echo -e "${YELLOW}║   ⚠ Не все сервисы готовы                               ║${NC}"
        echo -e "${YELLOW}╠══════════════════════════════════════════════════════════╣${NC}"
        echo -e "${YELLOW}║   Проверьте логи: docker compose logs                   ║${NC}"
        echo -e "${YELLOW}╚══════════════════════════════════════════════════════════╝${NC}"
        
        echo ""
        echo -e "${BLUE}Последние логи контейнеров:${NC}"
        docker compose logs --tail=20
    fi
fi

echo ""
echo -e "${CYAN}Готово!${NC}"