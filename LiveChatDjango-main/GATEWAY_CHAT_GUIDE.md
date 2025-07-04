# 게이트웨이 기반 실시간 채팅 시스템 가이드

## 📋 개요

이 프로젝트는 **API Gateway**에서 인증을 처리하고, **Django Channels**를 사용한 실시간 채팅 시스템입니다. 게이트웨이에서 유저 정보를 전달받아 채팅 서버는 오직 실시간 채팅만 담당합니다.

## 🏗️ 아키텍처

### 전체 구조
```
클라이언트 → API Gateway → Django 채팅 서버
                ↓
            JWT 인증 + 유저 정보 전달
```

### 주요 컴포넌트

1. **API Gateway** (별도 구현 필요)
   - JWT 토큰 검증
   - 유저 정보 추출
   - 채팅 서버로 유저 정보 전달

2. **Django 채팅 서버**
   - 게이트웨이에서 전달받은 유저 정보 사용
   - JWT 검증 없음 (게이트웨이에서 이미 처리)
   - 실시간 WebSocket 채팅만 담당

## 🔌 API 엔드포인트

### 1. 채팅방 API

#### 채팅방 목록 조회
```
GET /chat/v1/rooms/
```

#### 채팅방 생성
```
POST /chat/v1/create-room/
Content-Type: application/json

{
    "name": "채팅방 이름",
    "post_id": 1
}
```

#### 채팅방 상세 조회
```
GET /chat/v1/room/{room_id}/
```

### 2. 메시지 API

#### 메시지 생성
```
POST /chat/v1/message/
Content-Type: application/json

{
    "message": "안녕하세요!",
    "room": 1
}
```

## 🌐 WebSocket API

### 연결
```
WebSocket URL: ws://127.0.0.1:8000/ws/chat/{room_id}/chat/?user_id={user_id}&nickname={nickname}&email={email}&profile_url={profile_url}
```

### 쿼리 파라미터 (게이트웨이에서 전달)
- `user_id`: 사용자 ID (필수)
- `nickname`: 닉네임 (필수)
- `email`: 이메일 (필수)
- `profile_url`: 프로필 이미지 URL (선택)

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

# 의존성 설치
pip install -r requirements.txt

# 데이터베이스 마이그레이션
python manage.py migrate

# 서버 실행
python manage.py runserver
```

### 2. HTML 테스트 페이지 사용

1. `chat_gateway_test.html` 파일을 브라우저에서 열기
2. 유저 정보 입력 (ID, 닉네임, 이메일, 프로필 URL)
3. 채팅방 ID 입력 후 연결
4. 실시간 채팅 테스트

### 3. cURL을 이용한 API 테스트

#### 채팅방 생성
```bash
curl -X POST http://127.0.0.1:8000/chat/v1/create-room/ \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트 채팅방",
    "post_id": 1
  }'
```

#### 채팅방 목록 조회
```bash
curl -X GET http://127.0.0.1:8000/chat/v1/rooms/
```

#### 채팅방 상세 조회
```bash
curl -X GET http://127.0.0.1:8000/chat/v1/room/1/
```

## 🔧 게이트웨이 구현 가이드

### 게이트웨이에서 해야 할 일

1. **JWT 토큰 검증**
2. **유저 정보 추출**
3. **채팅 서버로 유저 정보 전달**

### 예시 구현 (Spring Cloud Gateway)

```java
@Component
public class ChatWebSocketFilter implements WebSocketFilter {
    
    @Override
    public Mono<Void> filter(WebSocketSession session, WebSocketHandler next) {
        // JWT 토큰 검증
        String token = extractToken(session);
        UserInfo userInfo = validateToken(token);
        
        // 유저 정보를 쿼리 파라미터로 추가
        String chatUrl = buildChatUrl(session.getUri(), userInfo);
        
        // 채팅 서버로 연결
        return next.handle(session);
    }
    
    private String buildChatUrl(URI originalUri, UserInfo userInfo) {
        return String.format("%s?user_id=%s&nickname=%s&email=%s&profile_url=%s",
            originalUri.toString(),
            userInfo.getUserId(),
            userInfo.getNickname(),
            userInfo.getEmail(),
            userInfo.getProfileUrl()
        );
    }
}
```

### 예시 구현 (Nginx)

```nginx
location /ws/chat/ {
    # JWT 토큰 검증 (Lua 스크립트 사용)
    access_by_lua_file /etc/nginx/lua/verify_jwt.lua;
    
    # 유저 정보를 쿼리로 추가
    set $user_id $http_x_user_id;
    set $nickname $http_x_nickname;
    set $email $http_x_email;
    set $profile_url $http_x_profile_url;
    
    # 채팅 서버로 프록시
    proxy_pass http://chat-server:8000/ws/chat/$1/chat/?user_id=$user_id&nickname=$nickname&email=$email&profile_url=$profile_url;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

## 📝 주요 변경사항

### 제거된 기능
- JWT 토큰 검증 (게이트웨이에서 처리)
- 회원가입/로그인 API
- 사용자 인증 관련 코드

### 추가된 기능
- 게이트웨이에서 전달받은 유저 정보 처리
- 쿼리 파라미터 기반 유저 식별
- 간소화된 채팅방 관리

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

1. **게이트웨이 필수**: 반드시 API Gateway를 통해 접근해야 함
2. **유저 정보 전달**: 게이트웨이에서 유저 정보를 쿼리 파라미터로 전달
3. **보안**: 게이트웨이에서 인증을 완전히 처리
4. **채팅방 존재**: 존재하지 않는 채팅방에 연결하면 연결이 거부됨

## 🐛 문제 해결

### WebSocket 연결 실패
- 게이트웨이가 올바르게 유저 정보를 전달하는지 확인
- 채팅방 ID가 올바른지 확인
- 필수 유저 정보(user_id, nickname, email)가 모두 전달되는지 확인

### 메시지 전송 실패
- WebSocket 연결 상태 확인
- 올바른 JSON 형식 사용
- 메시지 타입 확인

### 게이트웨이 연동 문제
- 게이트웨이에서 JWT 검증이 올바르게 되는지 확인
- 유저 정보 전달 형식 확인
- 네트워크 연결 상태 확인

## 🔗 연동 예시

### 프론트엔드에서 게이트웨이를 통한 연결

```javascript
// 게이트웨이를 통한 WebSocket 연결
const wsUrl = `ws://gateway:8080/ws/chat/${roomId}/chat/`;
const ws = new WebSocket(wsUrl);

// 게이트웨이가 자동으로 JWT 토큰을 검증하고 유저 정보를 추가
ws.onopen = function() {
    console.log('채팅 연결 성공');
};
```

이 구조를 통해 **게이트웨이에서 인증을 처리**하고, **채팅 서버는 오직 실시간 채팅만 담당**하는 깔끔한 분리가 가능합니다. 