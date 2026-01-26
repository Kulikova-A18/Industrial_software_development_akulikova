-- 01_insert_data.sql
-- Insertion of system and test data


-- 0. INSTALL EXTENSIONS


-- Install pgcrypto extension for password hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- 1. POPULATE REFERENCE TABLES


-- Chat types
INSERT INTO chat_types (type_name, description) VALUES
    ('private', 'Private chat between two users'),
    ('group', 'Group chat with multiple participants'),
    ('channel', 'Channel with one sender and many subscribers'),
    ('broadcast', 'Broadcast messages from one user to many')
ON CONFLICT (type_name) DO UPDATE SET 
    description = EXCLUDED.description;

-- User statuses
INSERT INTO user_statuses (status_name, is_online, description) VALUES
    ('online', TRUE, 'User is online and active'),
    ('offline', FALSE, 'User is offline'),
    ('away', FALSE, 'User is away from device'),
    ('busy', FALSE, 'User is busy (do not disturb)'),
    ('invisible', FALSE, 'Invisible mode (online but status hidden)')
ON CONFLICT (status_name) DO UPDATE SET
    is_online = EXCLUDED.is_online,
    description = EXCLUDED.description;


-- 2. CREATE SYSTEM USERS (SIMPLIFIED VERSION)


-- System user (ID 0) - simplified password
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
    -- Use simple hash without gen_salt
    encode(digest('system_password_' || gen_random_uuid(), 'sha256'), 'hex'),
    (SELECT status_id FROM user_statuses WHERE status_name = 'online'),
    TRUE
) ON CONFLICT (user_id) DO UPDATE SET
    username = EXCLUDED.username,
    email = EXCLUDED.email,
    is_active = EXCLUDED.is_active;

-- Administrator
INSERT INTO users (
    username, 
    email, 
    first_name, 
    last_name, 
    password_hash,
    status_id,
    is_active
) VALUES (
    'admin',
    'admin@messenger.example.com',
    'System',
    'Administrator',
    -- Use digest instead of crypt
    encode(digest('Admin123!@#' || 'salt', 'sha256'), 'hex'),
    (SELECT status_id FROM user_statuses WHERE status_name = 'online'),
    TRUE
) ON CONFLICT (username) DO UPDATE SET
    email = EXCLUDED.email,
    is_active = EXCLUDED.is_active;

-- Technical support
INSERT INTO users (
    username, 
    email, 
    first_name, 
    last_name, 
    password_hash,
    status_id,
    is_active
) VALUES (
    'support',
    'support@messenger.example.com',
    'Customer',
    'Support',
    encode(digest('Support123!' || 'salt', 'sha256'), 'hex'),
    (SELECT status_id FROM user_statuses WHERE status_name = 'online'),
    TRUE
) ON CONFLICT (username) DO NOTHING;


-- 3. CREATE TEST USERS (SIMPLIFIED VERSION)


-- Generate test users with simple passwords
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
    -- Use simple password hash
    encode(digest('Password123' || tu.username, 'sha256'), 'hex'),
    (SELECT status_id FROM user_statuses ORDER BY status_id LIMIT 1 OFFSET tu.status_num - 1),
    TRUE
FROM test_users tu
ON CONFLICT (username) DO NOTHING;
