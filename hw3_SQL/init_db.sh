#!/bin/bash

set -e

RESULTS_FILE="results_$(date +%Y%m%d_%H%M%S).txt"

if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
else
    echo "Error: .env file not found!"
    exit 1
fi

echo "Connecting as: $DB_USER@$DB_HOST:$DB_PORT"
echo "Results will be saved to: $RESULTS_FILE"

echo "Dropping old database and user (if any)"
sudo -u postgres psql -c "DROP DATABASE IF EXISTS \"$DB_NAME\";" 2>/dev/null || true
sudo -u postgres psql -c "DROP USER IF EXISTS \"$DB_USER\";" 2>/dev/null || true

echo "Creating user and database"
sudo -u postgres psql -c "CREATE USER \"$DB_USER\" WITH PASSWORD '$DB_PASSWORD';"
sudo -u postgres psql -c "CREATE DATABASE \"$DB_NAME\" OWNER \"$DB_USER\";"

export PGPASSWORD="$DB_PASSWORD"

psql -U "$DB_USER" -h "$DB_HOST" -p "$DB_PORT" -d "$DB_NAME" -f schema.sql
psql -U "$DB_USER" -h "$DB_HOST" -p "$DB_PORT" -d "$DB_NAME" -f seed_data.sql

> "$RESULTS_FILE"

{
    echo "=============================="
    echo "SQL Query Results"
    echo "Date: $(date)"
    echo "=============================="
    echo ""
} > "$RESULTS_FILE"


for query_file in queries/*.sql; do
    query_name=$(basename "$query_file")
    echo "Running: $query_name"

    {
        echo "### $query_name"
        echo ""
        echo "-- Query source:"
        cat "$query_file"
        echo ""
        echo "-- Query result:"
        psql -U "$DB_USER" -h "$DB_HOST" -p "$DB_PORT" -d "$DB_NAME" -f "$query_file" --no-align --field-separator='|'
        echo ""
        echo ""
    } >> "$RESULTS_FILE"

    psql -U "$DB_USER" -h "$DB_HOST" -p "$DB_PORT" -d "$DB_NAME" -f "$query_file"
    echo ""
done
