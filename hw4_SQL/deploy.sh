#!/bin/bash

LOG_FILE="deploy_$(date +%Y%m%d_%H%M%S).log"
exec > >(tee -a "$LOG_FILE") 2>&1

check_dependencies() {
    local missing_deps=()
    
    if ! command -v psql &> /dev/null; then
        missing_deps+=("PostgreSQL")
    fi
    
    if ! command -v envsubst &> /dev/null; then
        missing_deps+=("gettext (envsubst)")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        echo "Missing dependencies: ${missing_deps[*]}"
        echo "  Ubuntu/Debian: sudo apt-get install postgresql postgresql-contrib gettext"
        echo "  RHEL/CentOS: sudo yum install postgresql-server gettext"
        exit 1
    fi
}

load_env() {
    local env_file=".env"
    
    if [ ! -f "$env_file" ]; then
        echo ".env file not found. Using .env.example"
        if [ -f ".env.example" ]; then
            cp ".env.example" "$env_file"
            read -p "Edit .env now? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                nano "$env_file"
            fi
        else
            echo ".env.example also not found"
            exit 1
        fi
    fi
    
    set -a
    source "$env_file"
    set +a
    
    local required_vars=("DB_NAME" "DB_USER" "DB_PASSWORD")
    local missing_vars=()
    
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            missing_vars+=("$var")
        fi
    done
    
    if [ ${#missing_vars[@]} -ne 0 ]; then
        echo "Missing required variables in .env: ${missing_vars[*]}"
        exit 1
    fi
    
    DB_HOST="${DB_HOST:-localhost}"
    DB_PORT="${DB_PORT:-5432}"
    DB_SSL_MODE="${DB_SSL_MODE:-disable}"
    DB_BACKUP_ENABLED="${DB_BACKUP_ENABLED:-true}"
}

check_postgresql() {
    if systemctl is-active --quiet postgresql 2>/dev/null || service postgresql status >/dev/null 2>&1; then
        return 0
    else
        if sudo systemctl start postgresql 2>/dev/null || sudo service postgresql start 2>/dev/null; then
            return 0
        else
            echo "Failed to start PostgreSQL. Start manually:"
            echo "  sudo systemctl start postgresql"
            exit 1
        fi
    fi
}

fix_collation_issue() {
    sudo -u postgres bash << 'EOF'
        psql -c "ALTER DATABASE template1 REFRESH COLLATION VERSION;" 2>/dev/null || true
        psql -c "ALTER DATABASE postgres REFRESH COLLATION VERSION;" 2>/dev/null || true
EOF
    
    if [ $? -ne 0 ]; then
        echo "Could not fix collation, continuing..."
    fi
}

setup_postgres() {
    fix_collation_issue
    
    local user_exists=$(sudo -u postgres psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER';" 2>/dev/null || echo "0")
    
    if [ "$user_exists" != "1" ]; then
        sudo -u postgres psql -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';" 2>/dev/null
        
        if [ $? -ne 0 ]; then
            echo "Failed to create user"
            exit 1
        fi
    else
        sudo -u postgres psql -c "ALTER USER $DB_USER WITH PASSWORD '$DB_PASSWORD';" 2>/dev/null
    fi
    
    local db_exists=$(sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME';" 2>/dev/null || echo "0")
    
    if [ "$db_exists" != "1" ]; then
        sudo -u postgres psql -c "DROP DATABASE IF EXISTS $DB_NAME;" 2>/dev/null
        
        sudo -u postgres psql <<EOF 2>/dev/null
            CREATE DATABASE $DB_NAME 
            WITH 
            OWNER = $DB_USER
            ENCODING = 'UTF8'
            LC_COLLATE = 'C'
            LC_CTYPE = 'C'
            TEMPLATE = template0;
EOF
        
        if [ $? -ne 0 ]; then
            echo "Failed to create database with template0"
            
            sudo -u postgres psql -c "CREATE DATABASE $DB_NAME OWNER $DB_USER TEMPLATE template0;" 2>/dev/null
            
            if [ $? -ne 0 ]; then
                echo "Critical error creating database"
                echo "  sudo -u postgres psql"
                echo "  # Inside psql execute:"
                echo "  CREATE DATABASE $DB_NAME OWNER $DB_USER TEMPLATE template0;"
                exit 1
            fi
        fi
    else
        read -p "Recreate database? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            sudo -u postgres psql -c "DROP DATABASE $DB_NAME;" 2>/dev/null
            
            sudo -u postgres psql -c "CREATE DATABASE $DB_NAME OWNER $DB_USER TEMPLATE template0;" 2>/dev/null
            
            if [ $? -ne 0 ]; then
                echo "Failed to recreate database"
                exit 1
            fi
        fi
    fi
    
    sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;" 2>/dev/null
    
    sudo -u postgres psql -d "$DB_NAME" -c "GRANT CREATE ON SCHEMA public TO $DB_USER;" 2>/dev/null
    
    local connection_test=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 'PostgreSQL connected successfully';" 2>&1)
    
    if ! echo "$connection_test" | grep -q "connected successfully"; then
        echo "Connection problem: $connection_test"
        exit 1
    fi
}

check_db_connection() {
    local connection_test=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" 2>&1)
    
    if echo "$connection_test" | grep -q "FATAL\|ERROR\|psql:"; then
        echo "Failed to connect to database"
        echo "Error: $connection_test"
        return 1
    fi
    
    return 0
}

setup_database() {
    execute_sql_scripts
}

execute_sql_scripts() {
    local sql_dir="sql"
    if [ ! -d "$sql_dir" ]; then
        echo "Directory '$sql_dir' not found"
        exit 1
    fi
    
    local sql_files=(
        "00_create_tables.sql"
        "01_insert_data.sql" 
        "02_triggers_functions.sql"
        "03_test_data.sql"
    )
    
    for sql_file in "${sql_files[@]}"; do
        local file_path="$sql_dir/$sql_file"
        if [ -f "$file_path" ]; then
            local temp_output=$(mktemp)
            
            PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
                -f "$file_path" \
                --single-transaction \
                --set ON_ERROR_STOP=1 2>"$temp_output"
            
            local result=$?
            
            if [ $result -ne 0 ]; then
                echo "Error executing $sql_file"
                cat "$temp_output"
                rm -f "$temp_output"
                
                if [ "$sql_file" = "00_create_tables.sql" ]; then
                    if grep -q "already exists\|does not exist" "$temp_output"; then
                        echo "Skipping table creation errors, continuing..."
                    else
                        exit 1
                    fi
                else
                    exit 1
                fi
            else
                rm -f "$temp_output"
            fi
        else
            echo "File $sql_file not found, skipping"
        fi
    done
}

create_backup() {
    if [ "$DB_BACKUP_ENABLED" = "true" ]; then
        local backup_dir="backups"
        local backup_file="${backup_dir}/backup_${DB_NAME}_$(date +%Y%m%d_%H%M%S).sql"
        
        mkdir -p "$backup_dir"
        
        PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" \
            -d "$DB_NAME" \
            --clean \
            --if-exists \
            --no-owner \
            --no-privileges \
            --file="$backup_file"
        
        if [ $? -ne 0 ]; then
            echo "Error creating backup"
        else
            gzip -f "$backup_file"
            find "$backup_dir" -name "*.gz" -type f | sort -r | tail -n +6 | xargs rm -f 2>/dev/null
        fi
    fi
}

generate_report() {
    local report_file="deploy_report_$(date +%Y%m%d_%H%M%S).txt"
    
    cat > "$report_file" <<EOF
Database: $DB_NAME
User: $DB_USER
Host: $DB_HOST
Port: $DB_PORT
Template used: template0
EOF
}

check_status() {
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<'EOF'
SELECT 
    'Connection status' as check_item,
    'Success' as status
WHERE EXISTS (SELECT 1 FROM pg_database WHERE datname = current_database())
UNION ALL
SELECT 
    'Table count',
    COUNT(*)::text || ' tables'
FROM pg_tables 
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
UNION ALL
SELECT 
    'Database size',
    pg_size_pretty(pg_database_size(current_database()))
UNION ALL
SELECT 
    'Active users',
    COUNT(*)::text
FROM users 
WHERE is_active = true;
EOF
}

create_simple_env() {
    cat > .env <<EOF
DB_NAME=messenger_db
DB_USER=messenger_user
DB_PASSWORD=secure_password_123

DB_HOST=localhost
DB_PORT=5432

DB_BACKUP_ENABLED=true
EOF
}

fix_collation_manual() {
    sudo -u postgres bash << 'EOF'
        psql -c "ALTER DATABASE template1 REFRESH COLLATION VERSION;"
        psql -c "ALTER DATABASE postgres REFRESH COLLATION VERSION;"
        
        psql -c "DROP DATABASE IF EXISTS messenger_db;"
        
        psql -c "CREATE DATABASE messenger_db 
                 WITH 
                 OWNER = messenger_user
                 ENCODING = 'UTF8'
                 LC_COLLATE = 'C'
                 LC_CTYPE = 'C'
                 TEMPLATE = template0;"
        
        psql -c "GRANT ALL PRIVILEGES ON DATABASE messenger_db TO messenger_user;"
EOF
}

main() {
    local ACTION="deploy"
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --deploy|--install)
                ACTION="deploy"
                shift
                ;;
            --backup)
                ACTION="backup"
                shift
                ;;
            --status)
                ACTION="status"
                shift
                ;;
            --simple-env)
                create_simple_env
                exit 0
                ;;
            --fix-collation)
                fix_collation_manual
                exit 0
                ;;
            --help|-h)
                show_help
                exit 0
                ;;
            *)
                echo "Unknown argument: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    case $ACTION in
        deploy)
            check_dependencies
            load_env
            check_postgresql
            setup_postgres
            check_db_connection
            setup_database
            create_backup
            generate_report
            ;;
        backup)
            load_env
            create_backup
            ;;
        status)
            load_env
            check_status
            ;;
    esac
}

show_help() {
    cat <<EOF
Usage: $0 [COMMAND]

Commands:
  --deploy, --install   Full database deployment (default)
  --backup              Create database backup
  --status              Show database status
  --simple-env          Create simple .env file
  --fix-collation       Manual collation fix
  -h, --help            Show this help

Examples:
  $0                    # Full deployment
  $0 --deploy           # Same as above
  $0 --backup           # Backup only
  $0 --status           # Check status
  $0 --simple-env       # Create .env file
  $0 --fix-collation    # Manual collation fix

First time setup:
  1. sudo $0 --simple-env
  2. nano .env           # Edit password if needed
  3. sudo $0 --deploy    # Run deployment

If collation issues:
  1. sudo $0 --fix-collation  # Fix issue
  2. sudo $0 --deploy         # Run deployment
EOF
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi