#!/bin/bash
# setup-db.sh

echo "Настройка баз данных PostgreSQL..."

# Проверка существования БД file_storing_db
if psql -lqt | cut -d \| -f 1 | grep -qw file_storing_db; then
    echo "БД file_storing_db уже существует"
else
    echo "Создание БД file_storing_db..."
    createdb file_storing_db
fi

# Проверка существования БД file_analysis_db
if psql -lqt | cut -d \| -f 1 | grep -qw file_analysis_db; then
    echo "БД file_analysis_db уже существует"
else
    echo "Создание БД file_analysis_db..."
    createdb file_analysis_db
fi

# Создание пользователей
psql -c "CREATE USER IF NOT EXISTS storing_user WITH PASSWORD 'storing_pass';" 2>/dev/null || true
psql -c "CREATE USER IF NOT EXISTS analysis_user WITH PASSWORD 'analysis_pass';" 2>/dev/null || true

# Выдача прав
psql -c "GRANT ALL PRIVILEGES ON DATABASE file_storing_db TO storing_user;"
psql -c "GRANT ALL PRIVILEGES ON DATABASE file_analysis_db TO analysis_user;"

echo "Готово!"