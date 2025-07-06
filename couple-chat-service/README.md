# 💕 Couple Chat Service

커플 간의 일대일 실시간 채팅 서비스입니다.

## 🚀 주요 기능

- **실시간 채팅**: WebSocket(STOMP) 기반 실시간 메시지 전송
- **일대일 채팅**: 커플 전용 채팅방
- **메시지 저장**: PostgreSQL에 메시지 영구 저장
- **읽음 처리**: 메시지 읽음 상태 관리
- **페이지네이션**: 메시지 히스토리 조회
- **웹 테스트**: HTML 기반 시각적 테스트 페이지

## 🛠 기술 스택

- **Backend**: Spring Boot 3.2.0, Java 17
- **Database**: PostgreSQL
- **Cache**: Redis
- **WebSocket**: STOMP over SockJS
- **Frontend**: HTML, JavaScript, Bootstrap 5

## 📋 API 엔드포인트

### REST API
- `POST /api/v1/couple-chat/messages` - 메시지 전송
- `GET /api/v1/couple-chat/rooms/{roomId}/messages` - 메시지 조회
- `GET /api/v1/couple-chat/rooms/my` - 내 채팅방 조회
- `PUT /api/v1/couple-chat/rooms/{roomId}/read` - 읽음 처리
- `GET /api/v1/couple-chat/rooms/{roomId}/unread-count` - 읽지 않은 메시지 수
- `POST /api/v1/couple-chat/rooms` - 채팅방 생성

### WebSocket
- 연결: `ws://localhost:8084/ws/couple-chat`
- 메시지 전송: `/app/send-message`
- 실시간 수신: `/topic/chat/{roomId}`

## 🏃‍♂️ 실행 방법

### 1. 로컬 실행
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 2. Docker 실행
```bash
# Docker 이미지 빌드
docker build -t couple-chat-service .

# Docker 컨테이너 실행
docker run -p 8084:8084 couple-chat-service
```

### 3. Kubernetes 배포
```bash
# Kubernetes 배포
kubectl apply -f k8s/couple-chat-service.yaml
```

## 🧪 테스트

### 웹 테스트 페이지
브라우저에서 `http://localhost:8084` 접속

### 기능 테스트
1. **WebSocket 연결**: "WebSocket 연결" 버튼 클릭
2. **채팅방 생성**: "채팅방 생성" 버튼으로 새 채팅방 생성
3. **메시지 전송**: 텍스트 입력 후 "전송" 버튼 또는 Enter 키
4. **API 테스트**: 우측 패널의 API 테스트 버튼들 사용

## 📊 데이터베이스 스키마

### couple_chat_rooms
```sql
CREATE TABLE couple_chat_rooms (
    id UUID PRIMARY KEY,
    couple_id UUID UNIQUE NOT NULL,
    user1_id UUID NOT NULL,
    user2_id UUID NOT NULL,
    room_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### couple_chat_messages
```sql
CREATE TABLE couple_chat_messages (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    message VARCHAR(1000) NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## 🔧 설정

### application.yml
```yaml
server:
  port: 8084

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/couple_chat_db
    username: postgres
    password: password
  
  data:
    redis:
      host: redis
      port: 6379
```

## 🌐 배포 정보

- **포트**: 8084
- **Docker 이미지**: `couple-chat-service:latest`
- **Kubernetes 서비스**: `couple-chat-service`
- **웹 테스트 페이지**: `http://localhost:8084`

## 📝 사용 예시

### 1. 채팅방 생성
```bash
curl -X POST "http://localhost:8084/api/v1/couple-chat/rooms?coupleId=couple1&user1Id=user1&user2Id=user2"
```

### 2. 메시지 전송
```bash
curl -X POST "http://localhost:8084/api/v1/couple-chat/messages" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user1" \
  -d '{"roomId":"room-uuid","message":"안녕하세요!","messageType":"TEXT"}'
```

### 3. 메시지 조회
```bash
curl -X GET "http://localhost:8084/api/v1/couple-chat/rooms/room-uuid/messages?page=0&size=20" \
  -H "X-User-ID: user1"
```

## 🔒 보안

- CORS 설정으로 모든 도메인 허용 (개발용)
- CSRF 비활성화 (WebSocket 사용)
- 정적 파일 접근 허용
- WebSocket 엔드포인트 접근 허용

## 🐛 문제 해결

### WebSocket 연결 실패
- 서버가 실행 중인지 확인
- 포트 8084가 열려있는지 확인
- 브라우저 콘솔에서 오류 메시지 확인

### 데이터베이스 연결 실패
- PostgreSQL 서비스 실행 확인
- 데이터베이스 URL 및 인증 정보 확인
- 데이터베이스 생성 확인

### Redis 연결 실패
- Redis 서비스 실행 확인
- Redis 호스트 및 포트 설정 확인 