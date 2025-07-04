# UserAnswer API 가이드

## 개요
UserAnswer 서비스는 커플이 2지선다 질문에 답변을 제출하고 조회하는 기능을 제공합니다.

## 데이터베이스 스키마

### UserAnswer 테이블
```sql
CREATE TABLE user_answers (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL,
    couple_id UUID NOT NULL,
    user_id UUID NOT NULL,
    selected_option CHAR(1) NOT NULL, -- '1' 또는 '2'
    created_at TIMESTAMP NOT NULL
);
```

## API 엔드포인트

### 1. 답변 제출
**POST** `/api/user-answers`

#### 요청 헤더
- `X-User-ID`: 사용자 ID (필수)
- `X-Couple-ID`: 커플 ID (필수)
- `Content-Type`: application/json

#### 요청 본문
```json
{
    "questionId": "uuid-string",
    "selectedOption": "1"  // "1" 또는 "2"
}
```

#### 응답
```json
{
    "success": true,
    "message": "답변이 제출되었습니다.",
    "data": {
        "id": "uuid-string",
        "userId": "uuid-string",
        "questionId": "uuid-string",
        "coupleId": "uuid-string",
        "selectedOption": "1",
        "createdAt": "2024-01-01T12:00:00"
    }
}
```

### 2. 내 답변 조회
**GET** `/api/user-answers/my-answers`

#### 요청 헤더
- `X-User-ID`: 사용자 ID (필수)
- `X-Couple-ID`: 커플 ID (선택)

#### 응답
```json
{
    "success": true,
    "message": "답변 목록 조회 성공",
    "data": [
        {
            "id": "uuid-string",
            "userId": "uuid-string",
            "questionId": "uuid-string",
            "coupleId": "uuid-string",
            "selectedOption": "1",
            "createdAt": "2024-01-01T12:00:00"
        }
    ]
}
```

## 주요 변경사항

### 1. 테이블 구조 변경
- `choice` → `selected_option` 컬럼명 변경
- `answered_at` → `created_at` 컬럼명 변경
- `couple_id` 컬럼 추가

### 2. API 변경사항
- 답변 제출 시 `coupleId`가 자동으로 저장됨
- `selectedOption`은 "1" 또는 "2"만 허용
- 헤더에서 `X-Couple-ID`를 받아 처리

### 3. 데이터베이스 마이그레이션
Flyway를 사용하여 자동으로 테이블 구조가 변경됩니다:
```sql
-- V2__update_user_answers_table.sql
ALTER TABLE user_answers ADD COLUMN couple_id UUID;
ALTER TABLE user_answers RENAME COLUMN choice TO selected_option;
ALTER TABLE user_answers RENAME COLUMN answered_at TO created_at;
```

## 테스트 방법

### 1. 서비스 실행
```bash
cd springboot/question-answer-service
./gradlew bootRun
```

### 2. API 테스트
```bash
# Python 스크립트 실행
python test_user_answer_api.py
```

### 3. 수동 테스트
```bash
# 답변 제출
curl -X POST http://localhost:8086/api/user-answers \
  -H "X-User-ID: your-user-id" \
  -H "X-Couple-ID: your-couple-id" \
  -H "Content-Type: application/json" \
  -d '{"questionId": "question-uuid", "selectedOption": "1"}'

# 답변 조회
curl -X GET http://localhost:8086/api/user-answers/my-answers \
  -H "X-User-ID: your-user-id" \
  -H "X-Couple-ID: your-couple-id"
```

## 유효성 검사

### selectedOption 검증
- "1" 또는 "2"만 허용
- 다른 값 입력 시 400 Bad Request 응답

### 필수 필드 검증
- `questionId`: UUID 형식 필수
- `selectedOption`: 빈 값 불허용
- `X-User-ID`: 헤더 필수

## 에러 코드

| 상태 코드 | 설명 |
|-----------|------|
| 400 | 잘못된 요청 (유효성 검사 실패) |
| 401 | 인증 실패 |
| 500 | 서버 내부 오류 |

## 개발 참고사항

### 1. JWT 인증
Gateway에서 JWT 토큰을 검증하고 `X-User-ID`와 `X-Couple-ID` 헤더를 추가합니다.

### 2. 태그 점수 업데이트
답변 제출 시 자동으로 사용자의 태그 점수가 업데이트됩니다:
- 옵션 1 선택: 양수 점수
- 옵션 2 선택: 음수 점수

### 3. 데이터베이스 인덱스
성능 향상을 위해 다음 인덱스가 자동 생성됩니다:
- `idx_user_answers_user_id`
- `idx_user_answers_couple_id`
- `idx_user_answers_question_id` 