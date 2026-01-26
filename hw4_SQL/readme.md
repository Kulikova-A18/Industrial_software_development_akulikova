# Домашнее задание 2. Мессенджер, MMORPG, Компания, занимающаяся грузоперевозками

## Содержание

- [Структура БД](#структура-бд)
- [Задание](#задание)
- [Отчет о выполнении задания](#отчет-о-выполнении-задания)
- [Схема базы данных](#схема-базы-данных)
- [Типы данных](#типы-данных)
- [Развертывание](#развертывание)

## Задание

Представьте что вы создаете базу данных для одной из предметных областей (Мессенджер, MMORPG, Компания, занимающаяся грузоперевозками). Попробуйте спроектировать базу данных под выбранную предметную область соблюдая принципы нормальных форм включая  НФБК.

## Отчет о выполнении задания

Сам отчет о выполнении задания реализован по следующему пути: [Отчет о выполнении задания](./report.md)

## Схема базы данных

```mermaid
erDiagram
    chat_types ||--o{ chats : type
    user_statuses ||--o{ users : status
    users ||--o{ chat_members : member
    users ||--o{ messages : sender
    users ||--o{ media : uploader
    users ||--o{ pinned_messages : pinned_by
    users ||--o{ message_reads : reader
    users ||--o{ message_reactions : reactor
    users ||--o{ chat_audit_log : performer
    chats ||--o{ chat_members : contains
    chats ||--o{ messages : contains
    chats ||--o{ pinned_messages : contains
    chats ||--o{ chat_audit_log : tracked
    messages ||--o{ message_reads : read_status
    messages ||--o{ pinned_messages : pinned
    messages ||--o{ message_reactions : reactions
    messages }o--|| messages : replies_to
    messages }o--o| media : contains
    
    chat_types {
        serial type_id PK
        varchar type_name UK
        text description
        timestamp created_at
    }
    
    user_statuses {
        serial status_id PK
        varchar status_name UK
        boolean is_online
        text description
    }
    
    users {
        serial user_id PK
        varchar username UK
        varchar email UK
        varchar phone_number UK
        varchar first_name
        varchar last_name
        varchar password_hash
        varchar avatar_url
        integer status_id FK
        timestamp last_seen
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    
    chats {
        serial chat_id PK
        varchar chat_name
        integer type_id FK
        integer created_by FK
        varchar avatar_url
        text description
        boolean is_private
        integer max_members
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at
    }
    
    chat_members {
        serial member_id PK
        integer chat_id FK
        integer user_id FK
        varchar role
        timestamp joined_at
        timestamp left_at
        varchar nickname_in_chat
        boolean is_muted
        boolean is_banned
        boolean notifications_enabled
    }
    
    media {
        serial media_id PK
        integer user_id FK
        varchar file_url UK
        varchar file_name
        varchar file_type
        bigint file_size
        varchar mime_type
        integer width
        integer height
        integer duration
        varchar thumbnail_url
        timestamp uploaded_at
        boolean is_encrypted
    }
    
    messages {
        serial message_id PK
        integer chat_id FK
        integer sender_id FK
        integer reply_to_id FK
        text content
        integer media_id FK
        boolean is_edited
        boolean is_deleted
        boolean is_pinned
        varchar encryption_key
        timestamp sent_at
        timestamp delivered_at
        timestamp read_at
        timestamp edited_at
        timestamp deleted_at
    }
    
    message_reads {
        serial read_id PK
        integer message_id FK
        integer user_id FK
        timestamp read_at
    }
    
    pinned_messages {
        serial pin_id PK
        integer message_id FK
        integer pinned_by FK
        timestamp pinned_at
        timestamp unpinned_at
        text reason
    }
    
    chat_audit_log {
        serial log_id PK
        integer chat_id FK
        integer user_id FK
        varchar action_type
        integer target_user_id FK
        text old_value
        text new_value
        inet ip_address
        text user_agent
        timestamp performed_at
    }
    
    message_reactions {
        serial reaction_id PK
        integer message_id FK
        integer user_id FK
        varchar emoji
        timestamp reacted_at
    }
```

## Типы данных

### Типы чатов
| Тип | Описание |
|------|-------------|
| `private` | Индивидуальный чат между двумя пользователями |
| `group` | Многопользовательский чат с разрешениями |
| `channel" | Широковещательный канал с одним отправителем |
| "broadcast" | Рассылка сообщений "Один ко многим" |

### Статусы пользователей
| Статус | Онлайн | Описание |
|--------|--------|-------------|
| `online` | Да | Пользователь активен |
| "offline" | Нет | Пользователь отключен |
| `away" | Нет | Пользователь в отъезде |
| `busy" | Нет | Пользователь занят |
| "invisible" | Нет | Онлайн, но скрыт |

### Роли участников чата
| Роль | Разрешения /
|------|-------------|
| "owner" | Полный контроль, может удалять чат |
| "admin" | Управляет участниками, удаляет сообщения |
| "moderator" | Удаляет сообщения, отключает звук |
| `member` | Базовый доступ для чтения/записи |

## Примеры данных

### Системные пользователи
| Имя пользователя | Пароль | Роль | Статус |
|----------|----------|------|--------|
| `system` | автоматически сгенерированный | Системный бот | онлайн |
| `admin" | Admin123!@# | Администратор | онлайн |
| `support" | Support123! | Сотрудники службы поддержки | онлайн |

### Тестовые пользователи
| Имя пользователя | Пароль | Фамилия |
|----------|----------|------------|-----------|
| `alice` | Пароль 123 | Алиса | Чудо |
| `bob` | Пароль 123 | Боб | Строитель |
| `charlie` | Пароль 123 | Чарли | Чаплин |
| `diana` | Пароль 123 | Диана | Принц |
| `eva` | Пароль 123 | Ева | Грин |
| `frank` | Пароль 123 | Фрэнк | Оушен |

### Примеры чатов

1.  Общий чат в мессенджере  - Общедоступный групповой чат для всех пользователей
2.  Техническая поддержка  - Канал поддержки, управляемый сотрудниками службы поддержки

## Развертывание

### .env

```bash
DB_NAME=messenger_db
DB_USER=messenger_user
DB_PASSWORD=secure_password_123
DB_HOST=localhost
DB_PORT=5432
DB_BACKUP_ENABLED=true
```

### быстрый старт

```bash
sudo ./deploy.sh --simple-env
nano .env
sudo ./deploy.sh --deploy
```

### Команды скрипта

```bash
./deploy.sh --deploy      # Full deployment
./deploy.sh --backup      # Create backup
./deploy.sh --status      # Check status
./deploy.sh --simple-env  # Create .env file
./deploy.sh --fix-collation  # Fix collation issues
```

### Подключение к базе данных

```bash
psql -h localhost -p 5432 -U messenger_user -d messenger_db
```
