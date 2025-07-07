-- couple_id 컬럼을 nullable로 변경
ALTER TABLE user_answers ALTER COLUMN couple_id DROP NOT NULL; 