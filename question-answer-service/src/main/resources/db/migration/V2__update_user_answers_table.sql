-- UserAnswer 테이블 구조 변경
-- 1. couple_id 컬럼 추가
ALTER TABLE user_answers ADD COLUMN couple_id UUID;

-- 2. choice 컬럼을 selected_option으로 변경
ALTER TABLE user_answers RENAME COLUMN choice TO selected_option;

-- 3. answered_at 컬럼을 created_at으로 변경
ALTER TABLE user_answers RENAME COLUMN answered_at TO created_at;

-- 4. couple_id에 NOT NULL 제약조건 추가 (기존 데이터가 있다면 먼저 처리 필요)
-- ALTER TABLE user_answers ALTER COLUMN couple_id SET NOT NULL;

-- 5. 인덱스 추가 (성능 향상을 위해)
CREATE INDEX idx_user_answers_user_id ON user_answers(user_id);
CREATE INDEX idx_user_answers_couple_id ON user_answers(couple_id);
CREATE INDEX idx_user_answers_question_id ON user_answers(question_id); 