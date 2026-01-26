-- 02_triggers_functions.sql
-- Triggers and functions for Messenger database


-- 1. FUNCTIONS


-- Function for automatic update of updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to check private chat (no more than 2 participants)
CREATE OR REPLACE FUNCTION check_private_chat_members()
RETURNS TRIGGER AS $$
DECLARE
    chat_type_name VARCHAR;
    member_count INTEGER;
BEGIN
    -- Get chat type
    SELECT ct.type_name INTO chat_type_name
    FROM chats c
    JOIN chat_types ct ON c.type_id = ct.type_id
    WHERE c.chat_id = NEW.chat_id;
    
    -- If this is a private chat
    IF chat_type_name = 'private' THEN
        -- Count active members
        SELECT COUNT(*) INTO member_count
        FROM chat_members
        WHERE chat_id = NEW.chat_id 
          AND left_at IS NULL;
        
        -- If already 2 members and trying to add a third
        IF member_count >= 2 THEN
            RAISE EXCEPTION 'Private chat cannot have more than 2 active members';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to count chat members
CREATE OR REPLACE FUNCTION get_chat_member_count(chat_id_param INTEGER)
RETURNS INTEGER AS $$
DECLARE
    member_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO member_count
    FROM chat_members
    WHERE chat_id = chat_id_param AND left_at IS NULL;
    
    RETURN member_count;
END;
$$ LANGUAGE plpgsql;

-- Function for logging message deletion
CREATE OR REPLACE FUNCTION log_message_deletion()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.is_deleted = FALSE AND NEW.is_deleted = TRUE THEN
        INSERT INTO chat_audit_log (
            chat_id,
            user_id,
            action_type,
            target_user_id,
            old_value,
            new_value,
            performed_at
        ) VALUES (
            OLD.chat_id,
            OLD.sender_id,
            'message_deleted',
            NULL,
            OLD.content,
            NULL,
            CURRENT_TIMESTAMP
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function for automatic update of last_seen
CREATE OR REPLACE FUNCTION update_user_last_seen()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE users 
    SET last_seen = CURRENT_TIMESTAMP 
    WHERE user_id = NEW.sender_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- 2. TRIGGERS


-- Trigger to update updated_at in users table
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update updated_at in chats table
DROP TRIGGER IF EXISTS update_chats_updated_at ON chats;
CREATE TRIGGER update_chats_updated_at
    BEFORE UPDATE ON chats
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to check private chats
DROP TRIGGER IF EXISTS enforce_private_chat_limit ON chat_members;
CREATE TRIGGER enforce_private_chat_limit
    BEFORE INSERT OR UPDATE ON chat_members
    FOR EACH ROW
    EXECUTE FUNCTION check_private_chat_members();

-- Trigger to log message deletion
DROP TRIGGER IF EXISTS log_message_deletion_trigger ON messages;
CREATE TRIGGER log_message_deletion_trigger
    BEFORE UPDATE ON messages
    FOR EACH ROW
    EXECUTE FUNCTION log_message_deletion();

-- Trigger to update last_seen when sending a message
DROP TRIGGER IF EXISTS update_last_seen_on_message ON messages;
CREATE TRIGGER update_last_seen_on_message
    AFTER INSERT ON messages
    FOR EACH ROW
    EXECUTE FUNCTION update_user_last_seen();


-- 3. PROCEDURES


-- Procedure to create private chat
CREATE OR REPLACE PROCEDURE create_private_chat(
    user1_id INTEGER,
    user2_id INTEGER,
    OUT new_chat_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Create chat
    INSERT INTO chats (type_id, created_by, is_private)
    VALUES (
        (SELECT type_id FROM chat_types WHERE type_name = 'private'),
        user1_id,
        TRUE
    )
    RETURNING chat_id INTO new_chat_id;
    
    -- Add members
    INSERT INTO chat_members (chat_id, user_id, role)
    VALUES 
        (new_chat_id, user1_id, 'owner'),
        (new_chat_id, user2_id, 'member');
END;
$$;

-- Procedure to delete user (soft delete)
CREATE OR REPLACE PROCEDURE soft_delete_user(
    user_id_param INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Deactivate user
    UPDATE users 
    SET 
        is_active = FALSE,
        username = 'deleted_' || user_id_param || '_' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP),
        email = 'deleted_' || user_id_param || '_' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) || '@deleted.local',
        phone_number = NULL,
        password_hash = '',
        avatar_url = NULL,
        status_id = (SELECT status_id FROM user_statuses WHERE status_name = 'offline'),
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = user_id_param;
    
    -- Remove user from chats
    UPDATE chat_members 
    SET left_at = CURRENT_TIMESTAMP
    WHERE user_id = user_id_param AND left_at IS NULL;
    
    -- Mark messages as deleted
    UPDATE messages 
    SET 
        is_deleted = TRUE,
        content = '[message deleted]',
        deleted_at = CURRENT_TIMESTAMP
    WHERE sender_id = user_id_param AND is_deleted = FALSE;
END;
$$;


-- 4. VIEWS


-- View for user's active chats
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

-- View for unread messages
CREATE OR REPLACE VIEW user_unread_messages AS
SELECT 
    u.user_id,
    u.username,
    m.chat_id,
    c.chat_name,
    COUNT(m.message_id) as unread_count,
    MAX(m.sent_at) as last_unread_time
FROM users u
JOIN chat_members cm ON u.user_id = cm.user_id AND cm.left_at IS NULL
JOIN chats c ON cm.chat_id = c.chat_id AND c.deleted_at IS NULL
JOIN messages m ON c.chat_id = m.chat_id 
    AND m.is_deleted = FALSE 
    AND m.sent_at > COALESCE(cm.joined_at, '1970-01-01')
LEFT JOIN message_reads mr ON m.message_id = mr.message_id AND mr.user_id = u.user_id
WHERE mr.read_id IS NULL
    AND m.sender_id != u.user_id
    AND m.sent_at > COALESCE(cm.joined_at, '1970-01-01')
GROUP BY u.user_id, u.username, m.chat_id, c.chat_name;

-- View for user statistics
CREATE OR REPLACE VIEW user_statistics AS
SELECT 
    u.user_id,
    u.username,
    u.first_name,
    u.last_name,
    us.status_name,
    u.last_seen,
    COUNT(DISTINCT cm.chat_id) as active_chats_count,
    COUNT(DISTINCT m.message_id) as messages_sent,
    COUNT(DISTINCT mr.message_id) as messages_read
FROM users u
LEFT JOIN user_statuses us ON u.status_id = us.status_id
LEFT JOIN chat_members cm ON u.user_id = cm.user_id AND cm.left_at IS NULL
LEFT JOIN messages m ON u.user_id = m.sender_id AND m.is_deleted = FALSE
LEFT JOIN message_reads mr ON u.user_id = mr.user_id
WHERE u.is_active = TRUE
GROUP BY u.user_id, u.username, u.first_name, u.last_name, us.status_name, u.last_seen;
