# Couple Chat Service React Native 연동 가이드

## 1. 아키텍처 개요
- **채팅방 생성/조회/메시지 목록**: REST API
- **실시간 채팅 송수신**: WebSocket(STOMP over SockJS)
- **인증**: JWT 토큰 (로그인 시 발급)

---

## 2. 주요 엔드포인트

### 2.1 채팅방 생성 (REST)
```
POST /api/v1/couple-chat/rooms?coupleId={coupleId}&user1Id={user1Id}&user2Id={user2Id}
Authorization: Bearer <JWT>
```
- 응답 예시:
  ```json
  {
    "id": "<roomId>",
    "coupleId": "...",
    "user1Id": "...",
    "user2Id": "..."
  }
  ```

### 2.2 메시지 목록 조회 (REST)
```
GET /api/v1/couple-chat/rooms/{roomId}/messages?page=0&size=20
Authorization: Bearer <JWT>
X-User-ID: <userId>
```
- 응답: 메시지 배열

### 2.3 WebSocket 연결 (실시간 채팅)
- **URL**: `ws(s)://<gateway-host>/ws/couple-chat`
- **프로토콜**: STOMP over SockJS
- **연결 시 헤더**:  
  - `Authorization: Bearer <JWT>` (필요시)
- **구독**:  
  - `/topic/chat/{roomId}` (방 전체)
  - `/user/queue/messages` (개인)

### 2.4 메시지 전송 (WebSocket)
- **SEND**: `/app/send-message`
- **Payload 예시**:
  ```json
  {
    "roomId": "<roomId>",
    "message": "안녕하세요!",
    "messageType": "TEXT"
  }
  ```

---

## 3. React Native 연동 예시

### 3.1 REST API 호출 (axios 예시)
```js
import axios from 'axios';

const api = axios.create({
  baseURL: 'https://<gateway-host>',
  headers: {
    Authorization: `Bearer ${jwt}`,
    'X-User-ID': userId,
  },
});

const createRoom = async (coupleId, user1Id, user2Id) => {
  const res = await api.post(`/api/v1/couple-chat/rooms?coupleId=${coupleId}&user1Id=${user1Id}&user2Id=${user2Id}`);
  return res.data;
};

const getMessages = async (roomId) => {
  const res = await api.get(`/api/v1/couple-chat/rooms/${roomId}/messages?page=0&size=20`);
  return res.data;
};
```

### 3.2 WebSocket 연결 (stompjs + sockjs-client)
```js
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS('https://<gateway-host>/ws/couple-chat');
const stompClient = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    Authorization: `Bearer ${jwt}`,
  },
  debug: (str) => console.log(str),
});

stompClient.onConnect = (frame) => {
  // 방 전체 구독
  stompClient.subscribe(`/topic/chat/${roomId}`, (message) => {
    const chatMessage = JSON.parse(message.body);
    // 메시지 처리
  });
  // 개인 메시지 구독
  stompClient.subscribe(`/user/queue/messages`, (message) => {
    const chatMessage = JSON.parse(message.body);
    // 메시지 처리
  });
};

stompClient.activate();
```

### 3.3 메시지 전송
```js
stompClient.publish({
  destination: '/app/send-message',
  body: JSON.stringify({
    roomId,
    message: '안녕하세요!',
    messageType: 'TEXT',
  }),
});
```

---

## 4. 메시지 중복 방지
- 메시지의 `id`(UUID)를 Set 등으로 관리하여, 이미 표시한 메시지는 무시
- 예시:
  ```js
  const shownMessages = new Set();
  function handleMessage(msg) {
    if (shownMessages.has(msg.id)) return;
    shownMessages.add(msg.id);
    // 메시지 렌더링
  }
  ```

---

## 5. JWT 인증
- 로그인 후 받은 JWT를 모든 REST API, WebSocket 연결 시 헤더에 포함
- 토큰 만료 시 재로그인/재발급 필요

---

## 6. 기타 주의사항
- **UUID는 36자리(8-4-4-4-12) 형식**이어야 함
- **타임스탬프**는 UTC 기준 사용 권장
- **네트워크 오류/재연결** 처리 필요
- **WebSocket 연결 유지**: 앱이 백그라운드로 가면 연결이 끊길 수 있으니, 포그라운드 복귀 시 재연결 로직 필요

---

## 7. 테스트 시나리오
1. 두 기기(혹은 앱+웹)에서 각각 userId, roomId, JWT를 다르게 입력
2. WebSocket 연결 후 메시지 송수신 테스트
3. 내 메시지는 내 말풍선, 상대 메시지는 상대방 말풍선으로 표시되는지 확인
4. 메시지 중복 표시가 없는지 확인

---

## 8. 추천 라이브러리
- [@stomp/stompjs](https://www.npmjs.com/package/@stomp/stompjs)
- [sockjs-client](https://www.npmjs.com/package/sockjs-client)
- [axios](https://www.npmjs.com/package/axios)
- [react-native-gifted-chat](https://github.com/FaridSafi/react-native-gifted-chat) (UI용)

---

## 9. 참고/확장
- WebSocket 연결 시 네트워크 오류, 토큰 만료, 서버 재시작 등 예외 상황에 대한 핸들링을 추가하세요.
- 메시지 타입(TEXT, IMAGE, FILE 등)에 따라 UI를 다르게 처리할 수 있습니다.
- 채팅방/메시지 목록 페이징, 읽음 처리, 푸시 알림 등은 추가 구현이 필요할 수 있습니다. 

## 10. REST vs WebSocket 참고사항

- 실시간 채팅 메시지는 **WebSocket(STOMP)** 프로토콜로 송수신하는 것이 표준입니다.
- **WebSocket**은 실시간성(즉시 화면 반영, 상대방에게 바로 전달)이 필요한 경우에 반드시 사용해야 합니다.
- **REST API**는 주로 메시지 목록 조회(과거 대화 불러오기, 페이징 등)와 메시지 저장/백업 용도로 사용합니다.
- REST로 메시지를 전송할 수도 있지만, 일반적으로는 DB에만 저장되고 실시간 브로드캐스트는 WebSocket이 담당합니다.
- 서버 구현에 따라 REST로 메시지를 보내도 내부적으로 WebSocket 브로커로 publish하여 실시간 전파가 가능하도록 할 수 있습니다(이 경우 서버 코드 확인 필요).
- 실시간 채팅 경험을 원한다면 반드시 WebSocket을 메인 경로로 사용하세요.

--- 