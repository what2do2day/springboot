-- Flyway Migration: V1__create_couple_chat_tables.sql
-- Couple Chat Service 초기 테이블 생성

-- 1. 커플 채팅방 테이블
CREATE TABLE IF NOT EXISTS couple_chat_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    couple_id UUID NOT NULL UNIQUE,
    user1_id UUID NOT NULL,
    user2_id UUID NOT NULL,
    room_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. 커플 채팅 메시지 테이블
CREATE TABLE IF NOT EXISTS couple_chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    message VARCHAR(1000) NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 외래키 제약조건
    CONSTRAINT fk_couple_chat_messages_room_id 
        FOREIGN KEY (room_id) REFERENCES couple_chat_rooms(id) ON DELETE CASCADE
);

-- 3. 인덱스 생성 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_couple_chat_rooms_couple_id ON couple_chat_rooms(couple_id);
CREATE INDEX IF NOT EXISTS idx_couple_chat_rooms_user1_id ON couple_chat_rooms(user1_id);
CREATE INDEX IF NOT EXISTS idx_couple_chat_rooms_user2_id ON couple_chat_rooms(user2_id);
CREATE INDEX IF NOT EXISTS idx_couple_chat_messages_room_id ON couple_chat_messages(room_id);
CREATE INDEX IF NOT EXISTS idx_couple_chat_messages_sender_id ON couple_chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_couple_chat_messages_created_at ON couple_chat_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_couple_chat_messages_is_read ON couple_chat_messages(is_read); 