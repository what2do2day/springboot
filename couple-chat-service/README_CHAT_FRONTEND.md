# Couple Chat Service 프론트엔드 연동 가이드

## 1. 채팅방 연동 구조
- 채팅방은 `roomId`(UUID)로 식별합니다.
- 각 사용자는 `userId`(UUID)와 JWT 토큰을 발급받아야 합니다.
- 채팅방 생성은 REST API로, 메시지 송수신은 WebSocket(STOMP)으로 처리합니다.

## 2. 주요 엔드포인트

### 2.1 채팅방 생성 (REST API)
```
POST /api/v1/couple-chat/rooms?coupleId={coupleId}&user1Id={user1Id}&user2Id={user2Id}
```
- 응답: `{ id: <roomId>, ... }`

### 2.2 메시지 전송 (WebSocket)
- WebSocket 연결: `/ws/couple-chat`
- 메시지 전송: `/app/send-message`
- 메시지 구독: `/topic/chat/{roomId}` (방 전체), `/user/queue/messages` (개인)

#### 메시지 예시
```json
{
  "roomId": "<roomId>",
  "message": "내용",
  "messageType": "TEXT"
}
```

#### 서버에서 내려주는 메시지 예시
```json
{
  "id": "<메시지 UUID>",
  "roomId": "<roomId>",
  "senderId": "<userId>",
  "message": "내용",
  "messageType": "TEXT",
  ...
}
```

## 3. JWT 인증
- WebSocket 연결 시, 필요하다면 헤더에 JWT 토큰을 추가해야 합니다.
- REST API 호출 시 Authorization 헤더에 Bearer 토큰 사용 권장

## 4. 중복 메시지 방지
- 메시지의 `id`(UUID)를 클라이언트에서 Set 등으로 관리하여, 이미 표시한 메시지는 무시
- 예시:
```js
let shownMessages = new Set();
function addMessage(message, isSent, messageId) {
  if (messageId && shownMessages.has(messageId)) return;
  if (messageId) shownMessages.add(messageId);
  // ... 메시지 렌더링 ...
}
```

## 5. 테스트 시나리오
1. 두 개의 브라우저(또는 시크릿 창)에서 각각 userId, roomId, JWT를 다르게 입력
2. WebSocket 연결 후 메시지 송수신 테스트
3. 내 메시지는 내 말풍선, 상대 메시지는 상대방 말풍선으로 표시되는지 확인

## 6. 기타
- 채팅방/유저/메시지 UUID는 반드시 36자리(8-4-4-4-12) 형식이어야 함
- 서버/프론트 모두 UTC 기준 타임스탬프 사용 권장
- REST API, WebSocket 모두 401/403 에러 발생 시 토큰 재발급/로그인 처리 필요 