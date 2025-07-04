# UserVector API 가이드

## 개요
UserVector 서비스는 사용자별로 50개의 벡터 값을 MongoDB에 저장하고 관리하는 기능을 제공합니다.

## MongoDB 스키마

### UserVector 컬렉션
```json
{
  "_id": "ObjectId",
  "userId": "UUID",
  "vectors": {
    "vec1": 0.3,
    "vec2": -0.1,
    "vec3": 1.0,
    ...
    "vec50": -0.8
  },
  "updatedAt": "2025-07-03T13:00:00Z"
}
```

## API 엔드포인트

### 1. 사용자 벡터 생성
**POST** `http://localhost:8080/api/user-vectors`

**Headers:**
```
X-User-ID: 550e8400-e29b-41d4-a716-446655440000
X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
```

**응답:**
```json
{
  "success": true,
  "message": "사용자 벡터가 생성되었습니다.",
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "vectors": {
      "vec1": 0.0,
      "vec2": 0.0,
      "vec3": 0.0,
      ...
      "vec50": 0.0
    },
    "updatedAt": "2025-07-04T15:30:00"
  }
}
```

### 2. 내 벡터 조회
**GET** `http://localhost:8080/api/user-vectors/my-vector`

**Headers:**
```
X-User-ID: 550e8400-e29b-41d4-a716-446655440000
X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
```

**응답:**
```json
{
  "success": true,
  "message": "벡터 조회 성공",
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "vectors": {
      "vec1": 0.3,
      "vec2": -0.1,
      "vec3": 1.0,
      ...
      "vec50": -0.8
    },
    "updatedAt": "2025-07-04T15:30:00"
  }
}
```

### 3. 벡터 전체 업데이트
**PUT** `http://localhost:8080/api/user-vectors/my-vector`

**Headers:**
```
X-User-ID: 550e8400-e29b-41d4-a716-446655440000
X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
Content-Type: application/json
```

**Body:**
```json
{
  "vectors": {
    "vec1": 0.5,
    "vec2": -0.3,
    "vec3": 0.8,
    "vec4": 0.1,
    "vec5": -0.9
  }
}
```

### 4. 특정 벡터 업데이트
**PUT** `http://localhost:8080/api/user-vectors/my-vector/vec1?value=0.7`

**Headers:**
```
X-User-ID: 550e8400-e29b-41d4-a716-446655440000
X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
```

### 5. 벡터 삭제
**DELETE** `http://localhost:8080/api/user-vectors/my-vector`

**Headers:**
```
X-User-ID: 550e8400-e29b-41d4-a716-446655440000
X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
```

### 6. 내부 서버 간 벡터 조회 (인증 없음)
**GET** `http://localhost:8086/api/user-vectors/internal/{userId}`

**예시:**
```
GET http://localhost:8086/api/user-vectors/internal/550e8400-e29b-41d4-a716-446655440000
```

**응답:**
```json
{
  "success": true,
  "message": "벡터 조회 성공",
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "vectors": {
      "vec1": 0.3,
      "vec2": -0.1,
      "vec3": 1.0,
      ...
      "vec50": -0.8
    },
    "updatedAt": "2025-07-04T15:30:00"
  }
}
```

## Postman 테스트 예시

### 1. 벡터 생성
```
Method: POST
URL: http://localhost:8080/api/user-vectors
Headers:
  X-User-ID: 550e8400-e29b-41d4-a716-446655440000
  X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
```

### 2. 벡터 조회
```
Method: GET
URL: http://localhost:8080/api/user-vectors/my-vector
Headers:
  X-User-ID: 550e8400-e29b-41d4-a716-446655440000
  X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
```

### 3. 벡터 업데이트
```
Method: PUT
URL: http://localhost:8080/api/user-vectors/my-vector
Headers:
  X-User-ID: 550e8400-e29b-41d4-a716-446655440000
  X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
  Content-Type: application/json
Body (raw JSON):
{
  "vectors": {
    "vec1": 0.5,
    "vec2": -0.3,
    "vec3": 0.8,
    "vec4": 0.1,
    "vec5": -0.9
  }
}
```

### 4. 특정 벡터 업데이트
```
Method: PUT
URL: http://localhost:8080/api/user-vectors/my-vector/vec1?value=0.7
Headers:
  X-User-ID: 550e8400-e29b-41d4-a716-446655440000
  X-Couple-ID: 550e8400-e29b-41d4-a716-446655440001
```

### 5. 내부 서버 간 벡터 조회
```
Method: GET
URL: http://localhost:8086/api/user-vectors/internal/550e8400-e29b-41d4-a716-446655440000
Headers: 없음 (인증 불필요)
```

## 유효성 검사

### 벡터 키 검증
- `vec1` ~ `vec50`만 허용
- 다른 키 입력 시 400 Bad Request

### 벡터 값 검증
- -1.0 ~ 1.0 범위만 허용
- 범위를 벗어나는 값 입력 시 400 Bad Request

## 에러 코드

| 상태 코드 | 설명 |
|-----------|------|
| 400 | 잘못된 요청 (유효성 검사 실패) |
| 401 | 인증 실패 |
| 404 | 벡터를 찾을 수 없음 |
| 409 | 이미 존재하는 벡터 (생성 시) |
| 500 | 서버 내부 오류 |

## 내부 서버 간 통신

### 특징
- **인증 불필요**: JWT 토큰이나 헤더 인증이 필요하지 않음
- **직접 접근**: Gateway를 거치지 않고 직접 8086 포트로 접근
- **간단한 조회**: userId만으로 벡터 데이터 조회
- **에러 처리**: 벡터가 없을 때 404 응답

### 사용 예시
```bash
# 다른 서비스에서 호출할 때
curl http://localhost:8086/api/user-vectors/internal/550e8400-e29b-41d4-a716-446655440000
```

### 응답 예시
```json
{
  "success": true,
  "message": "벡터 조회 성공",
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "vectors": {
      "vec1": 0.3,
      "vec2": -0.1,
      "vec3": 1.0,
      ...
      "vec50": -0.8
    },
    "updatedAt": "2025-07-04T15:30:00"
  }
}
```

## MongoDB 설정

### 1. MongoDB 설치 및 실행
```bash
# MongoDB Community Edition 설치 후
mongod --dbpath /data/db
```

### 2. 데이터베이스 생성
```javascript
use question_answer_db
```

### 3. 컬렉션 확인
```javascript
show collections
db.user_vectors.find()
```

## 개발 참고사항

### 1. 자동 초기화
- 벡터가 존재하지 않을 때 자동으로 초기화 (모든 값이 0.0)
- 업데이트 시에도 자동으로 생성

### 2. 데이터 타입
- 벡터 값: Double (-1.0 ~ 1.0)
- 벡터 키: String (vec1 ~ vec50)
- userId: UUID

### 3. 성능 최적화
- MongoDB 인덱스 자동 생성 (userId)
- 벡터 값은 Map 구조로 효율적 저장 