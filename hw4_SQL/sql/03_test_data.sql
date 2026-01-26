-- 03_test_data_simple.sql
-- Simplified test data


-- 1. CREATING TEST CHATS


-- Remove old test chats if they exist
DELETE FROM chats WHERE chat_name IN ('Technical Support', 'General Messenger Chat');

-- General support channel
INSERT INTO chats (chat_name, type_id, created_by, description, is_private)
SELECT 
    'Technical Support',
    type_id,
    (SELECT user_id FROM users WHERE username = 'support'),
    'Official technical support channel.',
    FALSE
FROM chat_types
WHERE type_name = 'channel';

-- General chat for all users (group)
INSERT INTO chats (chat_name, type_id, created_by, description, is_private)
SELECT 
    'General Messenger Chat',
    type_id,
    (SELECT user_id FROM users WHERE username = 'admin'),
    'Main chat for all users to communicate.',
    FALSE
FROM chat_types
WHERE type_name = 'group';


-- 2. ADDING MEMBERS TO CHATS


-- Remove old members
DELETE FROM chat_members WHERE chat_id IN (
    SELECT chat_id FROM chats WHERE chat_name IN ('General Messenger Chat', 'Technical Support')
);

-- Add all active users to general chat
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
WHERE u.is_active = TRUE
    AND u.username != 'system';

-- Add all active users to support channel
INSERT INTO chat_members (chat_id, user_id, role)
SELECT 
    c.chat_id,
    u.user_id,
    'member'
FROM users u
CROSS JOIN (SELECT chat_id FROM chats WHERE chat_name = 'Technical Support') c
WHERE u.is_active = TRUE
    AND u.username != 'system';


-- 3. CREATING TEST MESSAGES


-- Remove old messages
DELETE FROM messages WHERE chat_id IN (
    SELECT chat_id FROM chats WHERE chat_name IN ('General Messenger Chat', 'Technical Support')
);

-- Messages in general chat
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

-- Messages in support channel
INSERT INTO messages (chat_id, sender_id, content)
SELECT 
    (SELECT chat_id FROM chats WHERE chat_name = 'Technical Support'),
    (SELECT user_id FROM users WHERE username = 'support'),
    unnest(ARRAY[
        'Important update: technical maintenance tomorrow.',
        'New feature: voice messages.',
        'How to change avatar?',
        'We recommend enabling two-factor authentication.'
    ]);


-- 4. SIMPLE VERIFICATION


SELECT 'DATABASE VERIFICATION' as info;

SELECT 'Users:' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Chats:', COUNT(*) FROM chats
UNION ALL
SELECT 'Messages:', COUNT(*) FROM messages
UNION ALL
SELECT 'Chat members:', COUNT(*) FROM chat_members;

SELECT 'FIRST 5 USERS' as info;
SELECT user_id, username, email, is_active FROM users ORDER BY user_id LIMIT 5;

SELECT 'TEST CHATS' as info;
SELECT chat_id, chat_name, 
       (SELECT type_name FROM chat_types WHERE type_id = chats.type_id) as type,
       (SELECT username FROM users WHERE user_id = chats.created_by) as created_by
FROM chats ORDER BY chat_id;