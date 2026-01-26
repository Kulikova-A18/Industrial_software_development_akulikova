# Отчет о выполнении задания

Для выполнения задания были реализованы:

- deploy.sh
- sql/*sql 

Команды скрипта deploy.sh:

```bash
./deploy.sh --deploy      # Full deployment
./deploy.sh --backup      # Create backup
./deploy.sh --status      # Check status
./deploy.sh --simple-env  # Create .env file
./deploy.sh --fix-collation  # Fix collation issues
```

## Содержание

- [Ход выполнения задания](#ход-выполнения-задания)
- [Этап 1: Подготовка окружения](#этап-1-подготовка-окружения)
- [Этап 2: Создание базы данных и таблиц](#этап-2-создание-структуры-базы-данных)
- [Этап 3: Заполнение таблиц тестовыми данными](#этап-3-установка-расширений-и-начальных-данных)
- [Этап 4: Проверка корректности](#этап-4-проверка-корректности)
- [Итоговый результат выполнения](#итоговый-результат-выполнения)

## Ход выполнения задания

`Важно`. Сами комментарии были реализованы на англ. языке, но для удобства чтения были переведены на рус. язык

### Этап 1: Подготовка окружения

Выполненные команды:

```sql
-- Исправление проблемы с коллацией
ALTER DATABASE template1 REFRESH COLLATION VERSION;
ALTER DATABASE postgres REFRESH COLLATION VERSION;

-- Создание/обновление пользователя (если существовал)
ALTER USER messenger_user WITH PASSWORD 'secure_password_123';

-- Запрос пользователю
Recreate database? (y/N): N

-- Удаление существующей БД (если требовалось)
DROP DATABASE IF EXISTS messenger_db;

-- Создание новой БД с template0
CREATE DATABASE messenger_db 
WITH 
OWNER = messenger_user
ENCODING = 'UTF8'
LC_COLLATE = 'C'
LC_CTYPE = 'C'
TEMPLATE = template0;

-- Предоставление прав
GRANT ALL PRIVILEGES ON DATABASE messenger_db TO messenger_user;
GRANT CREATE ON SCHEMA public TO messenger_user;
```

### Этап 2: Создание структуры базы данных
Основные команды создания таблиц:
```sql
-- 1. Справочные таблицы
CREATE TABLE IF NOT EXISTS chat_types (...);
CREATE TABLE IF NOT EXISTS user_statuses (...);

-- 2. Основные таблицы
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    status_id INTEGER REFERENCES user_statuses(status_id) ON DELETE SET NULL,
    last_seen TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_phone_format CHECK (phone_number ~* '^\+?[1-9]\d{1,14}$' OR phone_number IS NULL)
);

CREATE TABLE IF NOT EXISTS chats (...);
CREATE TABLE IF NOT EXISTS chat_members (...);
CREATE TABLE IF NOT EXISTS media (...);
CREATE TABLE IF NOT EXISTS messages (...);

-- 3. Вспомогательные таблицы
CREATE TABLE IF NOT EXISTS message_reads (...);
CREATE TABLE IF NOT EXISTS pinned_messages (...);
CREATE TABLE IF NOT EXISTS chat_audit_log (...);
CREATE TABLE IF NOT EXISTS message_reactions (...);

-- Создание индексов
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_chat_name_type 
ON chats(chat_name, type_id) 
WHERE deleted_at IS NULL AND chat_name IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_chat_member 
ON chat_members(chat_id, user_id) 
WHERE left_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
-- ... остальные 20 индексов
```

### Этап 3: Установка расширений и начальных данных
```sql
-- Установка расширения
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Заполнение справочников
INSERT INTO chat_types (type_name, description) VALUES
    ('private', 'Private chat between two users'),
    ('group', 'Group chat with multiple participants'),
    ('channel', 'Channel with one sender and many subscribers'),
    ('broadcast', 'Broadcast messages from one user to many')
ON CONFLICT (type_name) DO UPDATE SET 
    description = EXCLUDED.description;

INSERT INTO user_statuses (status_name, is_online, description) VALUES
    ('online', TRUE, 'User is online and active'),
    ('offline', FALSE, 'User is offline'),
    ('away', FALSE, 'User is away from device'),
    ('busy', FALSE, 'User is busy (do not disturb)'),
    ('invisible', FALSE, 'Invisible mode (online but status hidden)')
ON CONFLICT (status_name) DO UPDATE SET
    is_online = EXCLUDED.is_online,
    description = EXCLUDED.description;

-- Создание системных пользователей
INSERT INTO users (
    user_id,
    username, 
    email, 
    first_name, 
    last_name, 
    password_hash,
    status_id,
    is_active
) VALUES (
    0,
    'system',
    'system@messenger.internal',
    'System',
    'Bot',
    encode(digest('system_password_' || gen_random_uuid(), 'sha256'), 'hex'),
    (SELECT status_id FROM user_statuses WHERE status_name = 'online'),
    TRUE
) ON CONFLICT (user_id) DO UPDATE SET
    username = EXCLUDED.username,
    email = EXCLUDED.email,
    is_active = EXCLUDED.is_active;

-- Создание тестовых пользователей
WITH test_users AS (
    SELECT 
        unnest(ARRAY['alice', 'bob', 'charlie', 'diana', 'eva', 'frank']) as username,
        unnest(ARRAY['Alice', 'Bob', 'Charlie', 'Diana', 'Eva', 'Frank']) as first_name,
        unnest(ARRAY['Wonder', 'Builder', 'Chaplin', 'Prince', 'Green', 'Ocean']) as last_name,
        unnest(ARRAY['alice@example.com', 'bob@example.com', 'charlie@example.com', 
                'diana@example.com', 'eva@example.com', 'frank@example.com']) as email,
        unnest(ARRAY[1, 1, 2, 1, 3, 1]) as status_num
)
INSERT INTO users (
    username, 
    email, 
    first_name, 
    last_name, 
    password_hash,
    status_id,
    is_active
)
SELECT 
    tu.username,
    tu.email,
    tu.first_name,
    tu.last_name,
    encode(digest('Password123' || tu.username, 'sha256'), 'hex'),
    (SELECT status_id FROM user_statuses ORDER BY status_id LIMIT 1 OFFSET tu.status_num - 1),
    TRUE
FROM test_users tu
ON CONFLICT (username) DO NOTHING;
```

### Этап 4: Создание триггеров, функций и представлений
```sql
-- Создание функций
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_private_chat_members()
RETURNS TRIGGER AS $$
DECLARE
    chat_type_name VARCHAR;
    member_count INTEGER;
BEGIN
    SELECT ct.type_name INTO chat_type_name
    FROM chats c
    JOIN chat_types ct ON c.type_id = ct.type_id
    WHERE c.chat_id = NEW.chat_id;
    
    IF chat_type_name = 'private' THEN
        SELECT COUNT(*) INTO member_count
        FROM chat_members
        WHERE chat_id = NEW.chat_id AND left_at IS NULL;
        
        IF member_count >= 2 THEN
            RAISE EXCEPTION 'Private chat cannot have more than 2 active members';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Создание триггеров
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS enforce_private_chat_limit ON chat_members;
CREATE TRIGGER enforce_private_chat_limit
    BEFORE INSERT OR UPDATE ON chat_members
    FOR EACH ROW
    EXECUTE FUNCTION check_private_chat_members();

-- Создание процедур
CREATE OR REPLACE PROCEDURE create_private_chat(
    user1_id INTEGER,
    user2_id INTEGER,
    OUT new_chat_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO chats (type_id, created_by, is_private)
    VALUES (
        (SELECT type_id FROM chat_types WHERE type_name = 'private'),
        user1_id,
        TRUE
    )
    RETURNING chat_id INTO new_chat_id;
    
    INSERT INTO chat_members (chat_id, user_id, role)
    VALUES 
        (new_chat_id, user1_id, 'owner'),
        (new_chat_id, user2_id, 'member');
END;
$$;

-- Создание представлений
CREATE OR REPLACE VIEW user_active_chats AS
SELECT 
    u.user_id,
    u.username,
    c.chat_id,
    c.chat_name,
    ct.type_name as chat_type,
    COUNT(cm.user_id) as member_count,
    MAX(m.sent_at) as last_message_time
FROM users u
JOIN chat_members cm ON u.user_id = cm.user_id AND cm.left_at IS NULL
JOIN chats c ON cm.chat_id = c.chat_id AND c.deleted_at IS NULL
JOIN chat_types ct ON c.type_id = ct.type_id
LEFT JOIN messages m ON c.chat_id = m.chat_id AND m.is_deleted = FALSE
WHERE u.is_active = TRUE
GROUP BY u.user_id, u.username, c.chat_id, c.chat_name, ct.type_name;
```

### Этап 5: Тестовые данные

```sql
-- Создание тестовых чатов
DELETE FROM chats WHERE chat_name IN ('Technical Support', 'General Messenger Chat');

INSERT INTO chats (chat_name, type_id, created_by, description, is_private)
SELECT 
    'Technical Support',
    type_id,
    (SELECT user_id FROM users WHERE username = 'support'),
    'Official technical support channel.',
    FALSE
FROM chat_types
WHERE type_name = 'channel';

INSERT INTO chats (chat_name, type_id, created_by, description, is_private)
SELECT 
    'General Messenger Chat',
    type_id,
    (SELECT user_id FROM users WHERE username = 'admin'),
    'Main chat for all users to communicate.',
    FALSE
FROM chat_types
WHERE type_name = 'group';

-- Добавление участников
DELETE FROM chat_members WHERE chat_id IN (
    SELECT chat_id FROM chats WHERE chat_name IN ('General Messenger Chat', 'Technical Support')
);

INSERT INTO chat_members (chat_id, user_id, role)
SELECT 
    c.chat_id,
    u.user_id,
    CASE 
        WHEN u.username = 'admin' THEN 'owner'
        WHEN u.username = 'support' THEN 'admin'
        ELSE 'member'
    END
FROM users u
CROSS JOIN (SELECT chat_id FROM chats WHERE chat_name = 'General Messenger Chat') c
WHERE u.is_active = TRUE AND u.username != 'system';

-- Тестовые сообщения
DELETE FROM messages WHERE chat_id IN (
    SELECT chat_id FROM chats WHERE chat_name IN ('General Messenger Chat', 'Technical Support')
);

INSERT INTO messages (chat_id, sender_id, content, is_pinned)
SELECT 
    (SELECT chat_id FROM chats WHERE chat_name = 'General Messenger Chat'),
    u.user_id,
    CASE 
        WHEN u.username = 'admin' THEN 'Welcome to Messenger!'
        WHEN u.username = 'support' THEN 'For any questions, please contact support.'
        WHEN u.username = 'alice' THEN 'Hello everyone!'
        WHEN u.username = 'bob' THEN 'Greetings!'
        WHEN u.username = 'charlie' THEN 'Who wants to discuss technology?'
        WHEN u.username = 'diana' THEN 'Great platform!'
        ELSE 'Hi!'
    END,
    CASE WHEN u.username = 'admin' THEN TRUE ELSE FALSE END
FROM users u
WHERE u.username IN ('admin', 'support', 'alice', 'bob', 'charlie', 'diana')
    AND u.is_active = TRUE
ORDER BY u.user_id;

-- Проверка данных
SELECT 'DATABASE VERIFICATION' as info;

SELECT 'Users:' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Chats:', COUNT(*) FROM chats
UNION ALL
SELECT 'Messages:', COUNT(*) FROM messages
UNION ALL
SELECT 'Chat members:', COUNT(*) FROM chat_members;
```

## Итоговый результат выполнения

Статистика созданной базы данных:

```
ALTER DATABASE
ALTER DATABASE
ALTER ROLE
Recreate database? (y/N): 
DROP DATABASE
CREATE DATABASE
GRANT
GRANT
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
CREATE INDEX
COMMENT
COMMENT
COMMENT
COMMENT
COMMENT
COMMENT
COMMENT
COMMENT
COMMENT
COMMENT
COMMENT
CREATE EXTENSION
INSERT 0 4
INSERT 0 5
INSERT 0 1
INSERT 0 1
INSERT 0 1
INSERT 0 6
CREATE FUNCTION
CREATE FUNCTION
CREATE FUNCTION
CREATE FUNCTION
CREATE FUNCTION
DROP TRIGGER
CREATE TRIGGER
DROP TRIGGER
CREATE TRIGGER
DROP TRIGGER
CREATE TRIGGER
DROP TRIGGER
CREATE TRIGGER
DROP TRIGGER
CREATE TRIGGER
CREATE PROCEDURE
CREATE PROCEDURE
CREATE VIEW
CREATE VIEW
CREATE VIEW
DELETE 0
INSERT 0 1
INSERT 0 1
DELETE 0
INSERT 0 8
INSERT 0 8
DELETE 0
INSERT 0 6
INSERT 0 4
         info          
-----------------------
 DATABASE VERIFICATION
(1 row)

  table_name   | count 
---------------+-------
 Users:        |     9
 Chats:        |     2
 Messages:     |    10
 Chat members: |    16
(4 rows)

     info      
---------------
 FIRST 5 USERS
(1 row)

 user_id | username |             email             | is_active 
---------+----------+-------------------------------+-----------
       0 | system   | system@messenger.internal     | t
       1 | admin    | admin@messenger.example.com   | t
       2 | support  | support@messenger.example.com | t
       3 | alice    | alice@example.com             | t
       4 | bob      | bob@example.com               | t
(5 rows)

    info    
------------
 TEST CHATS
(1 row)

 chat_id |       chat_name        |  type   | created_by 
---------+------------------------+---------+------------
       1 | Technical Support      | channel | support
       2 | General Messenger Chat | group   | admin
(2 rows)


```
