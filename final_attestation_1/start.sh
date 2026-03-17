#!/bin/bash

print_message() {
    echo "[INFO] $1"
}

print_error() {
    echo "[ERROR] $1"
}

print_warning() {
    echo "[WARNING] $1"
}

check_structure() {
    print_message "Проверка структуры проекта..."
    
    if [ -f "src/main/resources/application.properties" ]; then
        print_message "application.properties найден в правильном месте"
        cat src/main/resources/application.properties
    elif [ -f "src/resources/application.properties" ]; then
        print_warning "application.properties в неправильном месте. Исправляю..."
        mkdir -p src/main/resources
        mv src/resources/application.properties src/main/resources/
        rmdir src/resources 2>/dev/null
        print_message "Файл перемещен"
    else
        print_error "application.properties не найден!"
        print_message "Создаю файл конфигурации..."
        mkdir -p src/main/resources
        cat > src/main/resources/application.properties << 'EOF'
db.url=jdbc:postgresql://localhost:5432/library
db.user=library_user
db.password=library_password
EOF
        print_message "Файл создан"
    fi
}

cleanup_docker() {
    print_message "Очистка Docker контейнеров..."
    sudo docker-compose down -v 2>/dev/null
    
    if sudo netstat -tlnp | grep -q ":5432"; then
        print_warning "Порт 5432 занят. Проверяю PostgreSQL..."
        if sudo systemctl is-active --quiet postgresql; then
            sudo systemctl stop postgresql
            sudo systemctl disable postgresql
        fi
    fi
}

start_database() {
    print_message "Запуск PostgreSQL в Docker..."
    sudo docker-compose up -d
    sleep 5
    
    if sudo docker ps | grep -q "library-db"; then
        print_message "PostgreSQL запущен"
        
        TABLES=$(sudo docker exec -i library-db psql -U library_user -d library -t -c "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';" 2>/dev/null | tr -d ' ')
        if [ "$TABLES" -gt "0" ]; then
            print_message "Таблицы созданы (найдено $TABLES)"
        else
            print_warning "Таблицы не найдены"
        fi
    else
        print_error "Ошибка запуска PostgreSQL"
        sudo docker-compose logs postgres
        exit 1
    fi
}

build_application() {
    print_message "Сборка приложения..."
    
    check_structure
    
    rm -rf target/
    
    mvn clean compile dependency:copy-dependencies
    
    if [ $? -eq 0 ]; then
        print_message "Сборка успешна"
        
        if [ -f "target/classes/application.properties" ]; then
            print_message "Ресурсы скопированы"
        else
            print_warning "Ресурсы не скопированы. Копирую вручную..."
            cp src/main/resources/application.properties target/classes/
        fi
    else
        print_error "Ошибка сборки"
        exit 1
    fi
}

test_connection() {
    print_message "Тест подключения к БД..."
    
    if sudo docker exec -i library-db psql -U library_user -d library -c "SELECT 1" &>/dev/null; then
        print_message "Подключение к БД работает"
        
        echo ""
        echo "Статистика базы данных:"
        sudo docker exec -i library-db psql -U library_user -d library -c "SELECT 'Книги: ' || count(*) FROM books;" 2>/dev/null
        sudo docker exec -i library-db psql -U library_user -d library -c "SELECT 'Читатели: ' || count(*) FROM readers;" 2>/dev/null
        sudo docker exec -i library-db psql -U library_user -d library -c "SELECT 'Активные выдачи: ' || count(*) FROM loans WHERE return_date IS NULL;" 2>/dev/null
        return 0
    else
        print_error "Не удалось подключиться к БД"
        return 1
    fi
}

run_application() {
    print_message "Запуск библиотечной системы..."
    
    echo ""
    echo "    БИБЛИОТЕЧНАЯ СИСТЕМА"
    echo ""
    echo "Конфигурация:"
    cat src/main/resources/application.properties
    echo ""
    
    java -cp "target/classes:target/dependency/*" com.library.Main
}

quick_start() {
    cleanup_docker
    start_database
    build_application
    test_connection
    run_application
}

show_menu() {
    echo ""
    echo "    УПРАВЛЕНИЕ БИБЛИОТЕЧНОЙ СИСТЕМОЙ"
    echo ""
    echo "1. Быстрый запуск (всё сразу)"
    echo "2. Только сборка приложения"
    echo "3. Только запуск БД"
    echo "4. Сброс БД"
    echo "5. Проверка статуса"
    echo "6. Тест подключения"
    echo "7. Просмотр логов БД"
    echo "0. Выход"
    echo ""
    echo -n "Выберите действие: "
}

main() {
    if [ "$1" = "quick" ]; then
        quick_start
    else
        while true; do
            show_menu
            read choice
            
            case $choice in
                1) quick_start ;;
                2) build_application ;;
                3) cleanup_docker; start_database ;;
                4) cleanup_docker; start_database ;;
                5) ./status.sh 2>/dev/null || test_connection ;;
                6) test_connection ;;
                7) sudo docker-compose logs --tail=50 postgres ;;
                0) print_message "Выход"; exit 0 ;;
                *) print_error "Неверный выбор" ;;
            esac
            
            echo ""
            echo "Нажмите Enter для продолжения..."
            read
        done
    fi
}

main $1