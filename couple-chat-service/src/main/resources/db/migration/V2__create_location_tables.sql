-- Flyway Migration: V2__create_location_tables.sql
-- Location Sharing Service 테이블 생성

-- 1. 사용자 위치 테이블 (현재 위치 저장)
CREATE TABLE IF NOT EXISTS user_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    couple_id UUID NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    accuracy DECIMAL(8, 2),
    address VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. 위치 히스토리 테이블 (과거 위치 기록)
CREATE TABLE IF NOT EXISTS location_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    couple_id UUID NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    accuracy DECIMAL(8, 2),
    address VARCHAR(500),
    shared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. 인덱스 생성 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_user_locations_user_id ON user_locations(user_id);
CREATE INDEX IF NOT EXISTS idx_user_locations_couple_id ON user_locations(couple_id);
CREATE INDEX IF NOT EXISTS idx_user_locations_is_active ON user_locations(is_active);
CREATE INDEX IF NOT EXISTS idx_user_locations_created_at ON user_locations(created_at);

CREATE INDEX IF NOT EXISTS idx_location_history_user_id ON location_history(user_id);
CREATE INDEX IF NOT EXISTS idx_location_history_couple_id ON location_history(couple_id);
CREATE INDEX IF NOT EXISTS idx_location_history_shared_at ON location_history(shared_at);

-- 4. 유니크 제약조건 (한 사용자당 하나의 활성 위치만)
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_locations_user_id_active 
    ON user_locations(user_id) WHERE is_active = true; 