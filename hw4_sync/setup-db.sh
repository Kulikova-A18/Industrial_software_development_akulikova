#!/bin/bash

set -xe

if psql -lqt | cut -d \| -f 1 | grep -qw file_storing_db; then
    echo "БД существует"
else
    createdb file_storing_db
fi

if psql -lqt | cut -d \| -f 1 | grep -qw file_analysis_db; then
    echo "БД существует"
else
    createdb file_analysis_db
fi

psql -c "CREATE USER IF NOT EXISTS storing_user WITH PASSWORD 'storing_pass';" 2>/dev/null || true
psql -c "CREATE USER IF NOT EXISTS analysis_user WITH PASSWORD 'analysis_pass';" 2>/dev/null || true

psql -c "GRANT ALL PRIVILEGES ON DATABASE file_storing_db TO storing_user;"
psql -c "GRANT ALL PRIVILEGES ON DATABASE file_analysis_db TO analysis_user;"
