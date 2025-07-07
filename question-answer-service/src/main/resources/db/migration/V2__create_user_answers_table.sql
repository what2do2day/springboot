-- UserAnswer 테이블 생성 (PostgreSQL)
-- MongoDB로 이동된 Question과 달리, 답변 기록은 PostgreSQL에 유지

CREATE TABLE user_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    couple_id UUID NOT NULL,
    question_id VARCHAR(24) NOT NULL, -- MongoDB ObjectId 저장용
    selected_choice VARCHAR(1) NOT NULL CHECK (selected_choice IN ('A', 'B')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_user_answers_user_id ON user_answers(user_id);
CREATE INDEX idx_user_answers_couple_id ON user_answers(couple_id);
CREATE INDEX idx_user_answers_question_id ON user_answers(question_id);
CREATE INDEX idx_user_answers_created_at ON user_answers(created_at);

-- updated_at 자동 업데이트를 위한 트리거 함수
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 트리거 생성
CREATE TRIGGER update_user_answers_updated_at 
    BEFORE UPDATE ON user_answers 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column(); 