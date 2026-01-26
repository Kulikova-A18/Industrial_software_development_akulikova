-- 00_create_tables.sql
-- Table creation for Messenger database


-- 1. CHAT TYPES

CREATE TABLE IF NOT EXISTS chat_types (
    type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 2. USER STATUSES

CREATE TABLE IF NOT EXISTS user_statuses (
    status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(20) NOT NULL UNIQUE,
    is_online BOOLEAN DEFAULT FALSE,
    description TEXT
);


-- 3. USERS

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


-- 4. CHATS

CREATE TABLE IF NOT EXISTS chats (
    chat_id SERIAL PRIMARY KEY,
    chat_name VARCHAR(200),
    type_id INTEGER NOT NULL REFERENCES chat_types(type_id),
    created_by INTEGER NOT NULL REFERENCES users(user_id),
    avatar_url VARCHAR(500),
    description TEXT,
    is_private BOOLEAN DEFAULT FALSE,
    max_members INTEGER DEFAULT 1000 CHECK (max_members > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);


-- 5. CHAT MEMBERS

CREATE TABLE IF NOT EXISTS chat_members (
    member_id SERIAL PRIMARY KEY,
    chat_id INTEGER NOT NULL REFERENCES chats(chat_id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role VARCHAR(20) DEFAULT 'member' CHECK (role IN ('owner', 'admin', 'moderator', 'member')),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    nickname_in_chat VARCHAR(100),
    is_muted BOOLEAN DEFAULT FALSE,
    is_banned BOOLEAN DEFAULT FALSE,
    notifications_enabled BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_joined_left CHECK (joined_at <= COALESCE(left_at, CURRENT_TIMESTAMP))
);


-- 6. MEDIA

CREATE TABLE IF NOT EXISTS media (
    media_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    file_url VARCHAR(500) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    mime_type VARCHAR(100),
    width INTEGER,
    height INTEGER,
    duration INTEGER,
    thumbnail_url VARCHAR(500),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_encrypted BOOLEAN DEFAULT FALSE,
    CONSTRAINT chk_file_size_limit CHECK (file_size <= 10737418240)
);


-- 7. MESSAGES

CREATE TABLE IF NOT EXISTS messages (
    message_id SERIAL PRIMARY KEY,
    chat_id INTEGER NOT NULL REFERENCES chats(chat_id) ON DELETE CASCADE,
    sender_id INTEGER NOT NULL REFERENCES users(user_id),
    reply_to_id INTEGER REFERENCES messages(message_id) ON DELETE SET NULL,
    content TEXT,
    media_id INTEGER REFERENCES media(media_id) ON DELETE SET NULL,
    is_edited BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    encryption_key VARCHAR(500),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    edited_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_content_or_media CHECK (content IS NOT NULL OR media_id IS NOT NULL),
    CONSTRAINT chk_message_timestamps CHECK (
        sent_at <= COALESCE(delivered_at, CURRENT_TIMESTAMP) AND
        COALESCE(delivered_at, sent_at) <= COALESCE(read_at, CURRENT_TIMESTAMP) AND
        sent_at <= COALESCE(edited_at, CURRENT_TIMESTAMP) AND
        sent_at <= COALESCE(deleted_at, CURRENT_TIMESTAMP)
    )
);


-- 8. MESSAGE READS

CREATE TABLE IF NOT EXISTS message_reads (
    read_id SERIAL PRIMARY KEY,
    message_id INTEGER NOT NULL REFERENCES messages(message_id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_message_read UNIQUE (message_id, user_id)
);


-- 9. PINNED MESSAGES

CREATE TABLE IF NOT EXISTS pinned_messages (
    pin_id SERIAL PRIMARY KEY,
    message_id INTEGER NOT NULL REFERENCES messages(message_id) ON DELETE CASCADE,
    pinned_by INTEGER NOT NULL REFERENCES users(user_id),
    pinned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unpinned_at TIMESTAMP,
    reason TEXT
);


-- 10. AUDIT LOG

CREATE TABLE IF NOT EXISTS chat_audit_log (
    log_id SERIAL PRIMARY KEY,
    chat_id INTEGER NOT NULL REFERENCES chats(chat_id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    action_type VARCHAR(50) NOT NULL,
    target_user_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    old_value TEXT,
    new_value TEXT,
    ip_address INET,
    user_agent TEXT,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_action_type CHECK (
        action_type IN (
            'user_joined', 'user_left', 'user_kicked', 'user_banned',
            'role_changed', 'chat_created', 'chat_updated', 'message_deleted',
            'permissions_changed', 'invitation_sent'
        )
    )
);


-- 11. MESSAGE REACTIONS

CREATE TABLE IF NOT EXISTS message_reactions (
    reaction_id SERIAL PRIMARY KEY,
    message_id INTEGER NOT NULL REFERENCES messages(message_id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    emoji VARCHAR(10) NOT NULL,
    reacted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_message_user_reaction UNIQUE (message_id, user_id, emoji)
);


-- 12. PARTIAL INDEXES


-- Unique chat name by type for non-deleted chats
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_chat_name_type 
ON chats(chat_name, type_id) 
WHERE deleted_at IS NULL AND chat_name IS NOT NULL;

-- Unique chat member for active members (who haven't left)
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_chat_member 
ON chat_members(chat_id, user_id) 
WHERE left_at IS NULL;

-- Unique active pinned message
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_pinned_message 
ON pinned_messages(message_id) 
WHERE unpinned_at IS NULL;


-- 13. PERFORMANCE INDEXES


-- Indexes for users table
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number) WHERE phone_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status_id) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active) WHERE is_active = TRUE;

-- Indexes for chat_members table
CREATE INDEX IF NOT EXISTS idx_chat_members_user ON chat_members(user_id) WHERE left_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_chat_members_chat ON chat_members(chat_id) WHERE left_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_chat_members_role ON chat_members(role, chat_id);

-- Indexes for messages table
CREATE INDEX IF NOT EXISTS idx_messages_chat ON messages(chat_id, sent_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_reply ON messages(reply_to_id) WHERE reply_to_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_messages_media ON messages(media_id) WHERE media_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_messages_pinned ON messages(is_pinned) WHERE is_pinned = TRUE;
CREATE INDEX IF NOT EXISTS idx_messages_deleted ON messages(is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_messages_sent_date ON messages(date(sent_at));

-- Indexes for media table
CREATE INDEX IF NOT EXISTS idx_media_user ON media(user_id);
CREATE INDEX IF NOT EXISTS idx_media_type ON media(file_type);
CREATE INDEX IF NOT EXISTS idx_media_upload_date ON media(date(uploaded_at));

-- Indexes for other tables
CREATE INDEX IF NOT EXISTS idx_message_reads_user ON message_reads(user_id);
CREATE INDEX IF NOT EXISTS idx_message_reads_message ON message_reads(message_id);
CREATE INDEX IF NOT EXISTS idx_pinned_messages_message ON pinned_messages(message_id) WHERE unpinned_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_chat_audit_log_chat ON chat_audit_log(chat_id, performed_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_audit_log_user ON chat_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_message_reactions_message ON message_reactions(message_id);
CREATE INDEX IF NOT EXISTS idx_message_reactions_user ON message_reactions(user_id);


-- 14. TABLE COMMENTS

COMMENT ON TABLE chat_types IS 'Chat types (private, group, channel, broadcast)';
COMMENT ON TABLE user_statuses IS 'User statuses (online, offline, away, busy, invisible)';
COMMENT ON TABLE users IS 'Messenger users';
COMMENT ON TABLE chats IS 'Chats and channels';
COMMENT ON TABLE chat_members IS 'Chat participants';
COMMENT ON TABLE media IS 'Media files uploaded by users';
COMMENT ON TABLE messages IS 'Messages in chats';
COMMENT ON TABLE message_reads IS 'Message read receipts';
COMMENT ON TABLE pinned_messages IS 'Pinned messages';
COMMENT ON TABLE chat_audit_log IS 'Chat action audit log';
COMMENT ON TABLE message_reactions IS 'Message reactions';
