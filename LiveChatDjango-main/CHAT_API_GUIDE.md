# 실시간 채팅 API 가이드

## 🏗️ 아키텍처

### 주요 컴포넌트

1. **Models** (`chat/models.py`)
   - `Room`: 채팅방 정보
   - `Chat`: 채팅 메시지
   - `RoomMember`: 채팅방 멤버 관리

2. **Consumer** (`chat/consumers.py`)
   - `ChatConsumer`: WebSocket 연결 처리
   - 실시간 메시지 전송/수신

3. **Views** (`chat/views.py`)
   - REST API 엔드포인트
   - 채팅방 및 메시지 관리

## 🔌 API 엔드포인트

### 1. 사용자 인증

#### 회원가입
```
POST /user/v1/register/
Content-Type: application/json

{
    "email": "user@example.com",
    "password1": "password123",
    "password2": "password123",
    "nickname": "사용자명"
}
```

#### 로그인
```
POST /user/v1/login/
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123"
}

Response:
{
    "access": "jwt_token_here",
    "refresh": "refresh_token_here"
}
```

### 2. 채팅방 API

#### 채팅방 목록 조회
```
GET /chat/v1/room/
Authorization: Bearer <access_token>
```

#### 채팅방 생성
```
POST /chat/v1/room/
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "name": "채팅방 이름",
    "post": 1
}
```

#### 채팅방 상세 조회
```
GET /chat/v1/room/{room_id}/
Authorization: Bearer <access_token>
```

### 3. 메시지 API

#### 메시지 생성
```
POST /chat/v1/message/
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "message": "안녕하세요!",
    "room": 1
}
```

## 🌐 WebSocket API

### 연결
```
WebSocket URL: ws://127.0.0.1:8000/ws/chat/{room_id}/chat/
```

### 메시지 형식

#### 클라이언트 → 서버 (메시지 전송)
```json
{
    "type": "chat.message",
    "message": "안녕하세요!"
}
```

#### 서버 → 클라이언트 (메시지 수신)
```json
{
    "type": "chat.message",
    "message": "안녕하세요!",
    "sender": "user@example.com",
    "nickname": "사용자명",
    "profile_picture_url": "/media/profile.jpg"
}
```

#### 사용자 입장 알림
```json
{
    "type": "chat.user.join",
    "username": "사용자명"
}
```

#### 사용자 퇴장 알림
```json
{
    "type": "chat.user.leave",
    "username": "사용자명"
}
```

## 🧪 테스트 방법

### 1. 서버 실행

```bash
# 프로젝트 디렉토리로 이동
cd LiveChatDjango-main

# 가상환경 활성화 (선택사항)
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 데이터베이스 마이그레이션
python manage.py migrate

# 서버 실행
python manage.py runserver
```

### 2. HTML 테스트 페이지 사용

1. `chat_test.html` 파일을 브라우저에서 열기
2. 사용자 정보 입력 및 로그인
3. 채팅방 ID 입력 후 연결
4. 실시간 채팅 테스트

### 3. Python 스크립트 테스트

#### 단일 사용자 테스트
```bash
# 메인 테스트 실행 (사용자 등록, 채팅방 생성)
python test_chat_api.py

# 특정 사용자로 채팅 참여
python test_chat_api.py --user 1  # 사용자1
python test_chat_api.py --user 2  # 사용자2
```

#### 다중 사용자 테스트
1. 첫 번째 터미널에서:
   ```bash
   python test_chat_api.py
   ```

2. 두 번째 터미널에서:
   ```bash
   python test_chat_api.py --user 2
   ```

3. 각 터미널에서 메시지를 입력하여 실시간 채팅 테스트

### 4. cURL을 이용한 API 테스트

#### 사용자 등록
```bash
curl -X POST http://127.0.0.1:8000/user/v1/register/ \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password1": "testpass123",
    "password2": "testpass123",
    "nickname": "테스트사용자"
  }'
```

#### 로그인
```bash
curl -X POST http://127.0.0.1:8000/user/v1/login/ \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "testpass123"
  }'
```

#### 채팅방 생성
```bash
curl -X POST http://127.0.0.1:8000/chat/v1/room/ \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트 채팅방",
    "post": 1
  }'
```

## 🔧 설정

### 필수 패키지
- Django
- Django Channels
- Django REST Framework
- daphne (ASGI 서버)
- channels-redis (Redis 백엔드, 선택사항)

### 환경 변수
```bash
# .env 파일 생성
SECRET_KEY=your_secret_key
DEBUG=True
ALLOWED_HOSTS=localhost,127.0.0.1

# Redis 설정 (선택사항)
CHANNEL_LAYER_REDIS_URL=redis://localhost:6379/0
```

## 🚀 배포

### 개발 환경
```bash
python manage.py runserver
```

### 프로덕션 환경
```bash
# Daphne 사용
daphne -b 0.0.0.0 -p 8000 mysite.asgi:application

# 또는 Gunicorn + Uvicorn
gunicorn mysite.asgi:application -w 4 -k uvicorn.workers.UvicornWorker
```

## 📝 주의사항

1. **인증**: WebSocket 연결 시 사용자 인증이 필요합니다.
2. **채팅방 존재**: 존재하지 않는 채팅방에 연결하면 연결이 거부됩니다.
3. **메시지 형식**: WebSocket 메시지는 JSON 형식이어야 합니다.
4. **Redis**: 대규모 사용자를 위해서는 Redis 백엔드 사용을 권장합니다.

## 🐛 문제 해결

### WebSocket 연결 실패
- 서버가 실행 중인지 확인
- 올바른 채팅방 ID 사용
- 사용자 인증 상태 확인

### 메시지 전송 실패
- WebSocket 연결 상태 확인
- 올바른 JSON 형식 사용
- 메시지 타입 확인

### 인증 오류
- JWT 토큰 유효성 확인
- 토큰 만료 시 재로그인
- 올바른 Authorization 헤더 사용 