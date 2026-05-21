#!/bin/bash

# ============================================================
# Скрипт запуска проекта КосмоСкан
# Версия: 2.0 (поддержка Docker и локального запуска)
# ============================================================

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# Директории проекта
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# PID файлы
mkdir -p pids logs

# Логотип
echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                                                          ║"
echo "║   КосмоСкан - Система приёма и проверки работ            ║"
echo "║   CosmoScan v2.0                                         ║"
echo "║                                                          ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Функции
progress() {
    echo -e "${BLUE}[$(date +%H:%M:%S)]${NC} $1"
}

success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[⚠]${NC} $1"
}

error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Проверка Java
check_java() {
    if ! command -v java &> /dev/null; then
        error "Java не найдена"
        return 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        error "Требуется Java 17 или выше. Текущая версия: $JAVA_VERSION"
        return 1
    fi
    
    success "Java найдена: $(java -version 2>&1 | head -n 1)"
    return 0
}

# Проверка Maven
check_maven() {
    if ! command -v mvn &> /dev/null; then
        error "Maven не найден"
        return 1
    fi
    
    success "Maven найден: $(mvn --version 2>&1 | head -n 1)"
    return 0
}

# Проверка Docker
check_docker() {
    if ! command -v docker &> /dev/null; then
        warning "Docker не найден"
        return 1
    fi
    
    if ! docker info &> /dev/null; then
        warning "Docker не запущен"
        return 1
    fi
    
    success "Docker запущен: $(docker --version)"
    return 0
}

# Сборка всех сервисов
build_all() {
    progress "Сборка всех сервисов..."
    echo ""
    
    local services=("api-gateway" "file-storing-service" "file-analysis-service" "cosmoscan-ui")
    local failed=false
    
    for service in "${services[@]}"; do
        if [ -d "$service" ]; then
            progress "Сборка $service..."
            cd "$service"
            if mvn clean package -DskipTests -q 2>&1 | tail -5; then
                success "$service собран"
            else
                error "Ошибка сборки $service"
                failed=true
            fi
            cd ..
        else
            error "Директория $service не найдена"
            failed=true
        fi
    done
    
    if [ "$failed" = true ]; then
        return 1
    fi
    
    success "Все сервисы собраны"
    return 0
}

# Запуск через Docker Compose
start_docker() {
    progress "Запуск через Docker Compose..."
    echo ""
    
    # Остановка существующих
    if [ -f "docker-compose.yml" ]; then
        docker compose down 2>/dev/null || true
    fi
    
    # Запуск
    if docker compose up -d --build; then
        success "Docker контейнеры запущены"
        
        # Ожидание готовности
        progress "Ожидание готовности сервисов (30 сек)..."
        sleep 30
        
        # Проверка
        check_services_docker
        return $?
    else
        error "Ошибка запуска Docker контейнеров"
        return 1
    fi
}

# Запуск локально (без Docker)
start_local() {
    progress "Локальный запуск сервисов..."
    echo ""
    
    # Остановка существующих процессов
    stop_local
    
    # Создание директорий
    mkdir -p uploads logs
    
    # Запуск PostgreSQL для локальной разработки
    start_postgres_local
    
    # Запуск File Storing Service
    progress "Запуск File Storing Service (порт 8081)..."
    if [ -f "file-storing-service/target/file-storing-service-1.0.0.jar" ]; then
        nohup java -jar file-storing-service/target/file-storing-service-1.0.0.jar \
            > logs/file-storing-service.log 2>&1 &
        echo $! > pids/file-storing-service.pid
        success "File Storing Service запущен (PID: $(cat pids/file-storing-service.pid))"
    else
        error "JAR файл File Storing Service не найден"
        return 1
    fi
    
    # Запуск File Analysis Service
    progress "Запуск File Analysis Service (порт 8082)..."
    if [ -f "file-analysis-service/target/file-analysis-service-1.0.0.jar" ]; then
        nohup java -jar file-analysis-service/target/file-analysis-service-1.0.0.jar \
            > logs/file-analysis-service.log 2>&1 &
        echo $! > pids/file-analysis-service.pid
        success "File Analysis Service запущен (PID: $(cat pids/file-analysis-service.pid))"
    else
        error "JAR файл File Analysis Service не найден"
        return 1
    fi
    
    # Запуск API Gateway
    progress "Запуск API Gateway (порт 8080)..."
    if [ -f "api-gateway/target/api-gateway-1.0.0.jar" ]; then
        nohup java -jar api-gateway/target/api-gateway-1.0.0.jar \
            > logs/api-gateway.log 2>&1 &
        echo $! > pids/api-gateway.pid
        success "API Gateway запущен (PID: $(cat pids/api-gateway.pid))"
    else
        error "JAR файл API Gateway не найден"
        return 1
    fi
    
    # Ожидание готовности
    progress "Ожидание готовности сервисов..."
    sleep 15
    
    # Проверка
    check_services_local
    return $?
}

# Запуск PostgreSQL локально (если не запущен)
start_postgres_local() {
    if pg_isready &> /dev/null; then
        success "PostgreSQL уже запущен"
        return 0
    fi
    
    if command -v pg_ctl &> /dev/null; then
        progress "Запуск PostgreSQL..."
        pg_ctl start -l logs/postgres.log 2>/dev/null || true
        sleep 5
    else
        warning "PostgreSQL не установлен. Убедитесь, что БД доступны на localhost:5434 и localhost:5435"
        warning "Для локальной разработки рекомендуется установить PostgreSQL:"
        echo "  Ubuntu/Debian: sudo apt install postgresql"
        echo "  Fedora: sudo dnf install postgresql-server"
        echo "  Arch: sudo pacman -S postgresql"
    fi
}

# Остановка локальных сервисов
stop_local() {
    progress "Остановка локальных сервисов..."
    
    for service in api-gateway file-storing-service file-analysis-service; do
        if [ -f "pids/$service.pid" ]; then
            PID=$(cat "pids/$service.pid")
            if kill -0 $PID 2>/dev/null; then
                kill $PID
                success "$service остановлен (PID: $PID)"
            fi
            rm -f "pids/$service.pid"
        fi
    done
    
    # Остановка UI
    if [ -f "pids/ui.pid" ]; then
        PID=$(cat pids/ui.pid)
        if kill -0 $PID 2>/dev/null; then
            kill $PID
            success "UI остановлен"
        fi
        rm -f pids/ui.pid
    fi
}

# Запуск UI отдельно
start_ui() {
    progress "Запуск CosmoScan UI..."
    
    if [ -f "cosmoscan-ui/target/cosmoscan-ui-1.0.0.jar" ]; then
        java -jar cosmoscan-ui/target/cosmoscan-ui-1.0.0.jar &
        echo $! > pids/ui.pid
        success "UI запущен (PID: $(cat pids/ui.pid))"
        return 0
    else
        error "JAR файл UI не найден. Сначала соберите проект."
        return 1
    fi
}

# Проверка Docker сервисов
check_services_docker() {
    progress "Проверка сервисов Docker..."
    echo ""
    
    local max_attempts=20
    local attempt=1
    local all_ready=true
    
    while [ $attempt -le $max_attempts ]; do
        # API Gateway
        if curl -s --connect-timeout 2 http://localhost:8080/health-check > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} API Gateway (8080) готов"
        else
            all_ready=false
            echo -e "  ${YELLOW}○${NC} API Gateway (8080) ожидание..."
        fi
        
        # File Storing
        if curl -s --connect-timeout 2 http://localhost:8081/api/works/health > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} File Storing (8081) готов"
        else
            all_ready=false
            echo -e "  ${YELLOW}○${NC} File Storing (8081) ожидание..."
        fi
        
        # File Analysis
        if curl -s --connect-timeout 2 http://localhost:8082/api/internal/health > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} File Analysis (8082) готов"
        else
            all_ready=false
            echo -e "  ${YELLOW}○${NC} File Analysis (8082) ожидание..."
        fi
        
        if $all_ready; then
            echo ""
            success "Все сервисы готовы!"
            return 0
        fi
        
        echo ""
        progress "Попытка $attempt/$max_attempts..."
        sleep 3
        ((attempt++))
        all_ready=true
    done
    
    echo ""
    warning "Не все сервисы готовы"
    return 1
}

# Проверка локальных сервисов
check_services_local() {
    check_services_docker
}

# Проверка статуса
check_status() {
    echo ""
    echo -e "${CYAN}=== Статус сервисов ===${NC}"
    echo ""
    
    # Проверка через Docker
    if docker info &> /dev/null && [ -f "docker-compose.yml" ]; then
        if docker compose ps 2>/dev/null | grep -q "Up"; then
            echo -e "${GREEN}Режим: Docker${NC}"
            docker compose ps
            echo ""
        fi
    fi
    
    # Проверка локальных процессов
    for service in api-gateway file-storing-service file-analysis-service; do
        if [ -f "pids/$service.pid" ]; then
            PID=$(cat "pids/$service.pid")
            if kill -0 $PID 2>/dev/null; then
                echo -e "  ${GREEN}✓${NC} $service (PID: $PID)"
            else
                echo -e "  ${RED}✗${NC} $service (PID: $PID - упал)"
            fi
        fi
    done
    
    # Проверка UI
    if [ -f "pids/ui.pid" ]; then
        PID=$(cat pids/ui.pid)
        if kill -0 $PID 2>/dev/null; then
            echo -e "  ${GREEN}✓${NC} CosmoScan UI (PID: $PID)"
        fi
    fi
}

# Остановка Docker
stop_docker() {
    progress "Остановка Docker контейнеров..."
    if [ -f "docker-compose.yml" ]; then
        docker compose down
        success "Docker контейнеры остановлены"
    fi
}

# Очистка
clean() {
    progress "Очистка..."
    
    # Остановка сервисов
    stop_local 2>/dev/null
    stop_docker 2>/dev/null
    
    # Очистка Maven
    for service in api-gateway file-storing-service file-analysis-service cosmoscan-ui; do
        if [ -d "$service" ]; then
            cd "$service"
            mvn clean 2>/dev/null || true
            cd ..
        fi
    done
    
    # Очистка логов и PID
    rm -rf logs/* pids/* 2>/dev/null
    
    success "Очистка завершена"
}

# Показать логи
show_logs() {
    local service=$1
    
    if [ -z "$service" ]; then
        echo -e "${CYAN}Доступные логи:${NC}"
        ls -la logs/ 2>/dev/null || echo "  (нет логов)"
        return
    fi
    
    if [ -f "logs/$service.log" ]; then
        tail -50 "logs/$service.log"
    else
        error "Лог $service не найден"
    fi
}

# Главное меню
show_menu() {
    echo ""
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                    Выберите действие                      ║${NC}"
    echo -e "${CYAN}╠══════════════════════════════════════════════════════════╣${NC}"
    echo -e "${CYAN}║                                                          ║${NC}"
    echo -e "${CYAN}║  1)${NC} Запуск через Docker Compose (рекомендуется)       ${CYAN}║${NC}"
    echo -e "${CYAN}║  2)${NC} Запуск локально (без Docker)                      ${CYAN}║${NC}"
    echo -e "${CYAN}║  3)${NC} Только запустить UI                               ${CYAN}║${NC}"
    echo -e "${CYAN}║  4)${NC} Собрать проект                                    ${CYAN}║${NC}"
    echo -e "${CYAN}║  5)${NC} Проверить статус                                  ${CYAN}║${NC}"
    echo -e "${CYAN}║  6)${NC} Остановить все сервисы                           ${CYAN}║${NC}"
    echo -e "${CYAN}║  7)${NC} Показать логи                                     ${CYAN}║${NC}"
    echo -e "${CYAN}║  8)${NC} Очистить проект                                   ${CYAN}║${NC}"
    echo -e "${CYAN}║  0)${NC} Выход                                             ${CYAN}║${NC}"
    echo -e "${CYAN}║                                                          ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -n "Введите номер: "
}

# Обработка аргументов командной строки
case "${1:-}" in
    --docker|-d)
        check_docker && build_all && start_docker && start_ui
        ;;
    --local|-l)
        check_java && check_maven && build_all && start_local && start_ui
        ;;
    --ui|-u)
        start_ui
        ;;
    --build|-b)
        build_all
        ;;
    --stop|-s)
        stop_local && stop_docker
        ;;
    --status)
        check_status
        ;;
    --logs)
        show_logs "$2"
        ;;
    --clean)
        clean
        ;;
    --help|-h)
        echo "Использование: $0 [опции]"
        echo ""
        echo "Опции:"
        echo "  --docker, -d    Запуск через Docker Compose"
        echo "  --local, -l     Запуск локально (без Docker)"
        echo "  --ui, -u        Только запуск UI"
        echo "  --build, -b     Только сборка"
        echo "  --stop, -s      Остановить все"
        echo "  --status        Проверить статус"
        echo "  --logs [name]   Показать логи (api-gateway, file-storing-service, file-analysis-service)"
        echo "  --clean         Очистить проект"
        echo "  --help, -h      Показать справку"
        ;;
    *)
        # Интерактивный режим
        while true; do
            show_menu
            read choice
            
            case $choice in
                1)
                    check_docker && build_all && start_docker && start_ui
                    ;;
                2)
                    check_java && check_maven && build_all && start_local && start_ui
                    ;;
                3)
                    start_ui
                    ;;
                4)
                    build_all
                    ;;
                5)
                    check_status
                    ;;
                6)
                    stop_local && stop_docker
                    ;;
                7)
                    echo ""
                    echo "Выберите сервис:"
                    echo "  1) api-gateway"
                    echo "  2) file-storing-service"
                    echo "  3) file-analysis-service"
                    echo -n "Выбор: "
                    read log_choice
                    case $log_choice in
                        1) show_logs "api-gateway" ;;
                        2) show_logs "file-storing-service" ;;
                        3) show_logs "file-analysis-service" ;;
                        *) echo "Неверный выбор" ;;
                    esac
                    ;;
                8)
                    clean
                    ;;
                0)
                    echo -e "${CYAN}До свидания!${NC}"
                    exit 0
                    ;;
                *)
                    error "Неверный выбор"
                    ;;
            esac
            
            echo ""
            echo -n "Нажмите Enter для продолжения..."
            read
        done
        ;;
esac