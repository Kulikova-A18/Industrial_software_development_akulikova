#!/bin/bash

# ============================================================
# Скрипт запуска проекта КосмоСкан
# Версия: 1.1 (с автоустановкой зависимостей)
# ============================================================

set -e  # Остановка при ошибке

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Определение ОС
detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        OS_VERSION=$VERSION_ID
    elif [ -f /etc/lsb-release ]; then
        . /etc/lsb-release
        OS=$DISTRIB_ID
        OS_VERSION=$DISTRIB_RELEASE
    elif [ -f /etc/debian_version ]; then
        OS="debian"
        OS_VERSION=$(cat /etc/debian_version)
    else
        OS=$(uname -s)
        OS_VERSION=$(uname -r)
    fi
    
    OS=$(echo "$OS" | tr '[:upper:]' '[:lower:]')
    
    echo -e "${BLUE}Определена ОС: ${GREEN}$OS $OS_VERSION${NC}"
}

# Проверка прав sudo
check_sudo() {
    if [ "$EUID" -ne 0 ]; then
        if ! sudo -v &> /dev/null; then
            echo -e "${YELLOW}⚠ Нет прав sudo. Некоторые установки могут не работать.${NC}"
            SUDO_AVAILABLE=false
        else
            SUDO_AVAILABLE=true
        fi
    else
        SUDO_AVAILABLE=true
    fi
}

# Установка Java 17
install_java() {
    echo ""
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo -e "${BLUE}  Установка Java 17${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    
    # Проверяем, установлена ли уже Java 17
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
        if [ "$JAVA_VERSION" = "17.0" ] || [ "$JAVA_VERSION" = "17" ]; then
            echo -e "${GREEN}✓ Java 17 уже установлена${NC}"
            return 0
        else
            echo -e "${YELLOW}⚠ Найдена Java $JAVA_VERSION, требуется Java 17${NC}"
        fi
    fi
    
    if [ "$SUDO_AVAILABLE" != true ]; then
        echo -e "${RED}✗ Нужны права sudo для установки Java${NC}"
        return 1
    fi
    
    case $OS in
        ubuntu|debian|linuxmint)
            echo -e "${BLUE}Установка Java 17 на Ubuntu/Debian...${NC}"
            sudo apt update -y
            sudo apt install -y openjdk-17-jdk openjdk-17-jre
            ;;
        
        fedora|rhel|centos)
            echo -e "${BLUE}Установка Java 17 на Fedora/RHEL/CentOS...${NC}"
            sudo dnf install -y java-17-openjdk java-17-openjdk-devel
            ;;
        
        arch|manjaro)
            echo -e "${BLUE}Установка Java 17 на Arch Linux...${NC}"
            sudo pacman -S --noconfirm jdk17-openjdk
            ;;
        
        opensuse*|sles)
            echo -e "${BLUE}Установка Java 17 на openSUSE...${NC}"
            sudo zypper install -y java-17-openjdk java-17-openjdk-devel
            ;;
        
        alpine)
            echo -e "${BLUE}Установка Java 17 на Alpine...${NC}"
            apk add --no-cache openjdk17
            ;;
        
        *)
            echo -e "${RED}✗ Неизвестная ОС: $OS${NC}"
            echo -e "${YELLOW}  Попробуйте установить Java 17 вручную:${NC}"
            echo "  https://adoptium.net/download/"
            return 1
            ;;
    esac
    
    # Проверка установки
    if command -v java &> /dev/null; then
        echo -e "${GREEN}✓ Java 17 успешно установлена${NC}"
        java -version 2>&1 | head -n 1
    else
        echo -e "${RED}✗ Ошибка установки Java${NC}"
        return 1
    fi
}

# Установка Maven
install_maven() {
    echo ""
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo -e "${BLUE}  Установка Maven${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn --version 2>&1 | head -n 1)
        echo -e "${GREEN}✓ Maven уже установлен: $MVN_VERSION${NC}"
        return 0
    fi
    
    if [ "$SUDO_AVAILABLE" != true ]; then
        echo -e "${RED}✗ Нужны права sudo для установки Maven${NC}"
        return 1
    fi
    
    case $OS in
        ubuntu|debian|linuxmint)
            sudo apt update -y
            sudo apt install -y maven
            ;;
        
        fedora|rhel|centos)
            sudo dnf install -y maven
            ;;
        
        arch|manjaro)
            sudo pacman -S --noconfirm maven
            ;;
        
        opensuse*|sles)
            sudo zypper install -y maven
            ;;
        
        alpine)
            apk add --no-cache maven
            ;;
        
        *)
            echo -e "${YELLOW}  Установка Maven через SDKMAN...${NC}"
            install_sdkman
            source "$HOME/.sdkman/bin/sdkman-init.sh"
            sdk install maven
            ;;
    esac
    
    if command -v mvn &> /dev/null; then
        echo -e "${GREEN}✓ Maven успешно установлен${NC}"
    else
        echo -e "${RED}✗ Ошибка установки Maven${NC}"
        return 1
    fi
}

# Установка Docker
install_docker() {
    echo ""
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo -e "${BLUE}  Установка Docker${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    
    if command -v docker &> /dev/null; then
        echo -e "${GREEN}✓ Docker уже установлен${NC}"
        docker --version
        return 0
    fi
    
    if [ "$SUDO_AVAILABLE" != true ]; then
        echo -e "${RED}✗ Нужны права sudo для установки Docker${NC}"
        return 1
    fi
    
    case $OS in
        ubuntu|debian|linuxmint)
            echo -e "${BLUE}Установка Docker на Ubuntu/Debian...${NC}"
            sudo apt update -y
            sudo apt install -y ca-certificates curl gnupg lsb-release
            sudo mkdir -p /etc/apt/keyrings
            curl -fsSL https://download.docker.com/linux/$OS/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
            echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/$OS $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
            sudo apt update -y
            sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            ;;
        
        fedora)
            sudo dnf -y install dnf-plugins-core
            sudo dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo
            sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            ;;
        
        centos|rhel)
            sudo dnf -y install dnf-plugins-core
            sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
            sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            ;;
        
        arch|manjaro)
            sudo pacman -S --noconfirm docker docker-compose
            ;;
        
        *)
            echo -e "${RED}✗ Автоматическая установка Docker не поддерживается для $OS${NC}"
            echo -e "${YELLOW}  Установите Docker вручную: https://docs.docker.com/engine/install/${NC}"
            return 1
            ;;
    esac
    
    # Запуск Docker
    if command -v systemctl &> /dev/null; then
        sudo systemctl start docker
        sudo systemctl enable docker
    fi
    
    # Добавление пользователя в группу docker
    sudo usermod -aG docker $USER 2>/dev/null || true
    
    if command -v docker &> /dev/null; then
        echo -e "${GREEN}✓ Docker успешно установлен${NC}"
    else
        echo -e "${RED}✗ Ошибка установки Docker${NC}"
        return 1
    fi
}

# Установка SDKMAN (для случаев, когда нет пакетного менеджера)
install_sdkman() {
    if [ ! -d "$HOME/.sdkman" ]; then
        echo -e "${BLUE}Установка SDKMAN...${NC}"
        curl -s "https://get.sdkman.io" | bash
    fi
}

# Установка всех зависимостей
install_all_dependencies() {
    echo ""
    echo -e "${MAGENTA}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${MAGENTA}║   Установка всех зависимостей                           ║${NC}"
    echo -e "${MAGENTA}╚══════════════════════════════════════════════════════════╝${NC}"
    
    detect_os
    check_sudo
    
    install_java || {
        echo -e "${RED}Не удалось установить Java. Установите вручную.${NC}"
        exit 1
    }
    
    install_maven || {
        echo -e "${RED}Не удалось установить Maven. Установите вручную.${NC}"
        exit 1
    }
    
    install_docker || {
        echo -e "${YELLOW}⚠ Docker не установлен. Сервисы можно запустить без Docker.${NC}"
    }
    
    echo ""
    echo -e "${GREEN}✓ Все зависимости установлены${NC}"
}

# Логотип
echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                                                          ║"
echo "║   КосмоСкан - Система приёма и проверки работ            ║"
echo "║   CosmoScan v1.0                                         ║"
echo "║                                                          ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Функция для вывода прогресса
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

# Проверка наличия необходимых инструментов
check_prerequisites() {
    progress "Проверка необходимых инструментов..."
    
    local java_ok=false
    local maven_ok=false
    local docker_ok=false
    
    # Проверка Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        success "Java найдена: $JAVA_VERSION"
        java_ok=true
    else
        warning "Java не найдена"
    fi
    
    # Проверка Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn --version 2>&1 | head -n 1)
        success "Maven найден: $MVN_VERSION"
        maven_ok=true
    else
        warning "Maven не найден"
    fi
    
    # Проверка Docker
    if command -v docker &> /dev/null; then
        DOCKER_VERSION=$(docker --version)
        success "Docker найден: $DOCKER_VERSION"
        docker_ok=true
    else
        warning "Docker не найден"
    fi
    
    echo ""
    
    # Если чего-то не хватает, предлагаем установить
    if ! $java_ok || ! $maven_ok || ! $docker_ok; then
        echo -e "${YELLOW}Не все зависимости установлены.${NC}"
        echo -n "Установить недостающие зависимости автоматически? (Y/n): "
        read answer
        
        if [ -z "$answer" ] || [ "$answer" = "y" ] || [ "$answer" = "Y" ] || [ "$answer" = "yes" ] || [ "$answer" = "Yes" ]; then
            detect_os
            check_sudo
            
            if ! $java_ok; then
                install_java
            fi
            
            if ! $maven_ok; then
                install_maven
            fi
            
            if ! $docker_ok; then
                install_docker
            fi
            
            # Повторная проверка
            check_prerequisites
            return $?
        else
            echo -e "${YELLOW}Установка пропущена. Некоторые функции могут не работать.${NC}"
            return 1
        fi
    fi
    
    return 0
}

# Сборка всех микросервисов
build_services() {
    progress "Сборка микросервисов..."
    echo ""
    
    local services=("api-gateway" "file-storing-service" "file-analysis-service" "cosmoscan-ui")
    local total=${#services[@]}
    local current=0
    
    for service in "${services[@]}"; do
        ((current++))
        progress "[$current/$total] Сборка $service..."
        
        if [ -d "$service" ]; then
            cd "$service"
            if mvn clean package -DskipTests 2>&1 | grep -E "(BUILD SUCCESS|BUILD FAILURE)"; then
                success "[$current/$total] $service собран успешно"
            else
                error "[$current/$total] Ошибка сборки $service"
                cd ..
                return 1
            fi
            cd ..
        else
            warning "[$current/$total] Директория $service не найдена, пропускаем"
        fi
        echo ""
    done
    
    success "Все сервисы собраны"
    echo ""
    return 0
}

# Запуск Docker контейнеров
start_docker() {
    progress "Запуск Docker контейнеров..."
    echo ""
    
    # Проверка, запущен ли Docker
    if ! docker info &> /dev/null; then
        error "Docker не запущен"
        
        if command -v systemctl &> /dev/null; then
            echo -n "Запустить Docker? (Y/n): "
            read answer
            if [ -z "$answer" ] || [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
                sudo systemctl start docker
                success "Docker запущен"
            else
                warning "Docker не будет запущен"
                return 1
            fi
        fi
    fi
    
    # Остановка существующих контейнеров
    if docker compose ps 2>/dev/null | grep -q "Up"; then
        warning "Обнаружены запущенные контейнеры. Останавливаем..."
        docker compose down
    fi
    
    # Запуск контейнеров
    if docker compose up -d --build; then
        success "Docker контейнеры запущены"
        echo ""
        progress "Ожидание готовности сервисов..."
    else
        error "Ошибка запуска Docker контейнеров"
        return 1
    fi
    
    return 0
}

# Проверка готовности сервисов
check_services() {
    local max_attempts=30
    local attempt=1
    
    progress "Проверка готовности сервисов..."
    echo ""
    
    while [ $attempt -le $max_attempts ]; do
        local all_ready=true
        
        # Проверка API Gateway
        if curl -s --connect-timeout 2 http://localhost:8080/health-check > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} API Gateway (8080) готов"
        else
            all_ready=false
            echo -e "  ${YELLOW}○${NC} API Gateway (8080) ожидание..."
        fi
        
        # Проверка File Storing Service
        if curl -s --connect-timeout 2 http://localhost:8081/api/works > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} File Storing Service (8081) готов"
        else
            all_ready=false
            echo -e "  ${YELLOW}○${NC} File Storing Service (8081) ожидание..."
        fi
        
        # Проверка File Analysis Service
        if curl -s --connect-timeout 2 http://localhost:8082/api/internal/health > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} File Analysis Service (8082) готов"
        else
            all_ready=false
            echo -e "  ${YELLOW}○${NC} File Analysis Service (8082) ожидание..."
        fi
        
        if $all_ready; then
            echo ""
            success "Все сервисы готовы к работе!"
            return 0
        fi
        
        echo ""
        progress "Попытка $attempt/$max_attempts. Ожидание 5 сек..."
        sleep 5
        ((attempt++))
    done
    
    echo ""
    warning "Не все сервисы готовы. Проверьте логи Docker."
    return 1
}

# Запуск UI
start_ui() {
    progress "Запуск CosmoScan UI..."
    
    if [ -f "cosmoscan-ui/target/cosmoscan-ui-1.0.0.jar" ]; then
        java -jar cosmoscan-ui/target/cosmoscan-ui-1.0.0.jar &
        UI_PID=$!
        success "UI запущен (PID: $UI_PID)"
        echo $UI_PID > .ui_pid
    else
        error "JAR файл UI не найден. Соберите проект сначала."
        return 1
    fi
    
    return 0
}

# Просмотр логов
show_logs() {
    echo ""
    progress "Последние логи сервисов:"
    echo ""
    if command -v docker &> /dev/null && docker info &> /dev/null 2>&1; then
        docker compose logs --tail=30
    else
        error "Docker не запущен, логи недоступны"
    fi
}

# Очистка
cleanup() {
    echo ""
    progress "Очистка..."
    
    # Остановка UI если запущен
    if [ -f ".ui_pid" ]; then
        kill $(cat .ui_pid) 2>/dev/null || true
        rm .ui_pid
    fi
}

# Главное меню
show_menu() {
    echo ""
    echo -e "${CYAN}╔════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║        Выберите действие:              ║${NC}"
    echo -e "${CYAN}╠════════════════════════════════════════╣${NC}"
    echo -e "${CYAN}║                                        ║${NC}"
    echo -e "${CYAN}║  1)${NC} Собрать и запустить всё           ${CYAN}║${NC}"
    echo -e "${CYAN}║  2)${NC} Только собрать проект            ${CYAN}║${NC}"
    echo -e "${CYAN}║  3)${NC} Только запустить Docker          ${CYAN}║${NC}"
    echo -e "${CYAN}║  4)${NC} Только запустить UI             ${CYAN}║${NC}"
    echo -e "${CYAN}║  5)${NC} Проверить статус сервисов       ${CYAN}║${NC}"
    echo -e "${CYAN}║  6)${NC} Показать логи Docker            ${CYAN}║${NC}"
    echo -e "${CYAN}║  7)${NC} Остановить все сервисы          ${CYAN}║${NC}"
    echo -e "${CYAN}║  8)${NC} Перезапустить все сервисы       ${CYAN}║${NC}"
    echo -e "${CYAN}║  9)${NC} Очистка Docker ресурсов         ${CYAN}║${NC}"
    echo -e "${CYAN}║ 10)${NC} Установить зависимости          ${CYAN}║${NC}"
    echo -e "${CYAN}║  0)${NC} Выход                           ${CYAN}║${NC}"
    echo -e "${CYAN}║                                        ║${NC}"
    echo -e "${CYAN}╚════════════════════════════════════════╝${NC}"
    echo ""
    echo -n "Введите номер: "
}

# Остановка всех сервисов
stop_all() {
    progress "Остановка всех сервисов..."
    
    # Остановка UI
    if [ -f ".ui_pid" ]; then
        kill $(cat .ui_pid) 2>/dev/null || true
        rm .ui_pid
        success "UI остановлен"
    fi
    
    # Остановка Docker контейнеров
    if command -v docker &> /dev/null && docker info &> /dev/null 2>&1; then
        docker compose down 2>/dev/null || true
        success "Docker контейнеры остановлены"
    fi
    
    echo -e "${GREEN}Все сервисы остановлены${NC}"
}

# Перезапуск
restart_all() {
    stop_all
    sleep 2
    start_docker
    check_services
    start_ui
}

# Очистка Docker
clean_docker() {
    warning "Это удалит все неиспользуемые Docker ресурсы"
    echo -n "Продолжить? (y/N): "
    read answer
    
    if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
        docker system prune -f
        success "Docker ресурсы очищены"
    else
        progress "Очистка отменена"
    fi
}

# Интерактивный режим
interactive_mode() {
    while true; do
        show_menu
        read choice
        
        case $choice in
            1)
                check_prerequisites
                build_services
                if command -v docker &> /dev/null && docker info &> /dev/null 2>&1; then
                    start_docker
                    check_services
                else
                    warning "Docker недоступен, сервисы не запущены в контейнерах"
                fi
                start_ui
                show_logs
                ;;
            2)
                check_prerequisites
                build_services
                ;;
            3)
                if command -v docker &> /dev/null; then
                    start_docker
                    check_services
                else
                    error "Docker не установлен. Сначала установите Docker."
                fi
                ;;
            4)
                start_ui
                ;;
            5)
                if command -v docker &> /dev/null; then
                    check_services
                else
                    warning "Docker не установлен"
                fi
                
                # Проверка UI
                echo ""
                if [ -f ".ui_pid" ]; then
                    UI_PID=$(cat .ui_pid)
                    if kill -0 $UI_PID 2>/dev/null; then
                        success "UI запущен (PID: $UI_PID)"
                    else
                        error "UI не запущен"
                    fi
                fi
                ;;
            6)
                show_logs
                ;;
            7)
                stop_all
                ;;
            8)
                restart_all
                ;;
            9)
                clean_docker
                ;;
            10)
                install_all_dependencies
                ;;
            0)
                echo -e "${CYAN}До свидания!${NC}"
                exit 0
                ;;
            *)
                error "Неверный выбор. Попробуйте снова."
                ;;
        esac
        
        # Пауза перед возвратом в меню
        if [ "$choice" != "0" ]; then
            echo ""
            echo -n "Нажмите Enter для продолжения..."
            read
        fi
    done
}

# Обработка аргументов командной строки
parse_args() {
    case "${1:-}" in
        --install-deps|-i)
            install_all_dependencies
            ;;
        --build|-b)
            check_prerequisites
            build_services
            ;;
        --docker|-d)
            start_docker
            check_services
            ;;
        --ui|-u)
            start_ui
            ;;
        --stop|-s)
            stop_all
            ;;
        --restart|-r)
            restart_all
            ;;
        --logs|-l)
            show_logs
            ;;
        --status)
            check_services
            ;;
        --help|-h)
            echo "Использование: $0 [опции]"
            echo ""
            echo "Опции:"
            echo "  --install-deps, -i  Установить все зависимости"
            echo "  --build, -b         Только сборка проекта"
            echo "  --docker, -d        Только запуск Docker"
            echo "  --ui, -u            Только запуск UI"
            echo "  --stop, -s          Остановить все"
            echo "  --restart, -r       Перезапустить все"
            echo "  --logs, -l          Показать логи"
            echo "  --status            Проверить статус"
            echo "  --help, -h          Показать справку"
            echo ""
            echo "Без аргументов запускается интерактивное меню"
            exit 0
            ;;
        *)
            interactive_mode
            ;;
    esac
}

# Обработка сигналов завершения
trap cleanup EXIT INT TERM

# Запуск
parse_args "$@"