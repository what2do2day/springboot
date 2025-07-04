## API
### 1. Chat
#### 1.1 메시지 관리
##### 1.1.1 메세지 생성
---
* URL: `/api/chat/message/`
* Method: POST
* Description: 새로운 채팅 메세지를 생성합니다.

**요청 예시**:
```json
{
  "type": "chat.message",
  "message": "Hello, World!"
}
```
#### 1.2 방 관리
##### 1.2.1 방 생성
---
* URL: `/api/chat/room/`
* Method: POST
* Description: 새로운 대화방을 생성합니다.

**요청 예시**:
```json
{
  "name": "Chat Room"
}
```
##### 1.2.2 방 조회
---
* URL: `/api/chat/room/{room_id}/`
* Method: POST
* Description: 특정 대화방을 조회합니다.
* 요청 파라미터: `{room_id}` - 조회할 방의 고유 식별자

**요청 예시**:
```json
{
  "id": 42,
  "name": "Chat Room",
  "chat_url": "/ws/chat/42/chat/"
}
```
#### 1.3 Websocket 
##### 1.3.1 Websocket 연결
---
* URL: `wss://example/ws/chat/{room_id}/chat/`
* Description: 클라이언트는 이 엔드포인트를 통해 웹소켓 서버에 연결할 수 있습니다.
##### 1.3.2 메시지 교환
##### 메시지 유형:
WebSocket 연결을 통해 주고받는 메시지는 다음과 같은 유형을 가질 수 있습니다:
* `chat.message`: 일반 채팅 메시지
* `chat.user.join`: 사용자가 채팅방에 입장한 이벤트
* `chat.user.leave`: 사용자가 채팅방에서 퇴장한 이벤트
##### 메시지 전송:
클라이언트는 JSON 형식의 메시지를 WebSocket 서버로 전송할 수 있습니다. 메시지의 형식은 다음과 같습니다:
```json
{
  "type": "chat.message",
  "message": "Hello, World!"
}
```
##### 메시지 수신:
WebSocket 서버는 다른 클라이언트로부터 수신한 메시지를 현재 연결된 클라이언트에게 전달합니다.
```json
{
  "type": "chat.message",
  "message": "Hello, World!",
  "sender": "user@example.com",
  "nickname": "John",
  "profile_picture_url": "https://example.com/profile.jpg"
}
```
##### 이벤트 처리:
클라이언트는 chat.user.join 및 chat.user.leave 이벤트를 수신하여 사용자의 입장 및 퇴장을 처리할 수 있습니다.
```json
{
  "type": "chat.user.join",
  "username": "John"
}
```
##### 오류 처리
WebSocket API에서 오류가 발생할 경우, 클라이언트는 적절한 오류 메시지를 수신합니다. 오류 메시지의 형식은 다음과 같습니다:
```json
{
  "error_type": "invalid_request",
  "message": "Invalid message format."
}
```
## 2. user
### 2-1 회원가입
---
* URL: /user/registration/
* Method: POST
* Description: 사용자의 정보를 사용하여 회원가입을 진행합니다.

**요청:**
```json
{
    "email": "이메일 주소",
    "password": "비밀번호",
    ...
}
```
**응답:**
```json
{
    "email": "이메일 주소",
    "message": "회원가입이 완료되었습니다.",
    "profile": {
        ...  # 프로필 정보
    }
}
```

* **회원가입된 이메일 입력시**
```json
응답 :
{
    "email": [
        "A user is already registered with this e-mail address."
    ]
}
```

### 2-2 로그인
---
* URL: /user/login/
* Method: POST
* Description: 사용자의 이메일과 비밀번호를 사용하여 로그인을 진행합니다.

**요청:**
```json

{
    "email": "이메일 주소",
    "password": "비밀번호"
}
```

**응답:**
```json
{
    "message": "환영합니다.",
    "profile": {
        ...  # 프로필 정보
    },
    "refresh": "JWT Refresh 토큰",
    "access": "JWT Access 토큰"
}
```

### 2-3 로그아웃
---
* URL: /user/logout/
* Method: POST
* Description: 로그아웃을 진행합니다.

**응답:**
```json
{
    "message": "로그아웃이 완료되었습니다."
}
```

### 2-4 친구추가(jwt)
---
* URL: /user/add-friend/
* Method: POST
* Description: 인증된 사용자 접근시 친구를 추가합니다.

**요청:**
```json
{
    "email": "친구의 이메일 주소"
}
```

**응답:**
```json
{
    "message": "친구 추가 완료 메시지"
}
```

### 2-5 친구 목록 조회(jwt)
---
* URL: /user/friends/
* Method: GET
* Description: 인증된 사용자 접근시 친구 목록을 조회합니다.

**응답:**
```json
{
    ...  # 친구 목록 정보
}
```

### 2-6 친구(사용자) 프로필 조회
---
* URL: /user/friend-profile/<int:friend_id>/
* Method: GET
* Description: 인증된 사용자 접근시 특정 사용자의 프로필 정보를 조회합니다.

**응답:**
```json
{
    ...  # 친구의 프로필 정보
}
```

### 2-7 친구 검색
---
* URL: /user/search-friends/?keyword=[이메일 정보]
* Method: GET
* Description: 인증된 사용자 접근시 키워드를 기반으로 친구를 검색합니다. 키워드는 사용자의 이메일에 포함된 키워드여야 합니다.

**쿼리 파라미터**
```json
{
    "keyword": "검색할 키워드(이메일에 포함된 키워드)"
}
```

**응답:**
```json
{
    ...  # 검색된 사용자들의 정보
}
```

### 2-8 친구 삭제
---
* URL: /user/delete-friend/
* Method: POST
* Description: 인증된 사용자 접근시 친구목록에 존재하면 친구를 삭제합니다.

**요청:**
```json
{
    "email": "삭제할 친구의 이메일 주소"
}
```

**응답:**
```json
{
    "message": "친구 삭제 완료 메시지"
}
```

* 친구목록에 없을 경우
**응답**
```json
{
    "detail": "user5@email.com님은 친구 목록에 없습니다."
}
```
### 2-9 프로필 조회
---
URL: /mysite/user/v1/profile/
Method: GET
Description: 

**응답**
```json
{
    "id": "id번호"
    "profile pictute": "프로필사진"
    "contact number": "연락처"
    "status": "상태"
    "user": "user번호"
    "is_private": "계정활성화/비활성화상태"
}
```
### 2-9 프로필 수정
---
URL: /mysite/user/v1/profile/edit/
Method: PUT
Description: 인증된 사용자가 프로필 수정 요청을 보내고 수정을 할 수 있습니다.
**요청**
```json
{
    "profile pictute": "변경할 프로필사진"
    "contact number": "변경할 연락처"
    "status": "변경할 상태"
}
**응답**
```json
{
    "profile pictute": "변경된 프로필 사진"
    "contact number": "변경된 연락처"
    "status": "변경된 상태"
    "is_private": "계정활성화/비활성화상태"
}
```

## 3. post
### 3-1 게시글 목록
---
* URL: /api/post/
* Method: GET
* Description: 게시된 모든 게시물의 목록을 볼 수 있습니다.

**응답**
```json

[
{
"id": 1,
"tags": [
{
"name": "{'name': '태그1 수정'}"
}
],
"title": "제목 수정1",
"content": "내용 수정",
"created_at": "2023-08-22T08:11:42.241124Z",
"updated_at": "2023-08-25T04:37:38.446555Z",
"hit": 1
}
]
```
### 3-2 게시글 상세 조회
---
* URL: /api/post/detail/1/
* Method: GET
* Description: 특정 게시물에 대한 내용, 작성자 및 기타 세부 정보를 확인할 수 있습니다.

**응답**
```json
{
"post_id": 1,
"title": "제목 1",
"content": "내용",
"tags": [
{
"name": "{'name': '태그1'}"
}
],
"hit": 1
}
```
### 3-3 게시글 작성
---
* URL: /api/post/write
* Method: POST
* Description: 제목, 내용 및 관련 정보를 제출하여 새 게시물을 만들 수 있습니다.

**요청**
```json
{
"title": "제목",
"content": "내용",
"tags": [
{"name": "태그"},
]
}
```
**응답**
```json
{
"id": 1,
"tags": [
{
"name": "{'name': '태그1'}"
}
],
"title": "제목 1",
"content": "내용 1",
"created_at": "2023-08-22T08:11:42.241124Z",
"updated_at": "2023-08-25T07:26:47.254487Z",
"hit": 0
}
```
### 3-4 게시글 수정
---
* URL: /api/post/detail/1/edit/
* Method: GET, POST
* Description: 특정 게시물의 내용, 해시태그 및 기타 세부 정보를 수정할 수 있습니다.

**요청**
```json
{
"title": "제목",
"content": "내용",
"tags": [
{"name": "태그"},
]
}
```
**응답**
```json
{
"id": 1,
"tags": [
{
"name": "{'name': '태그1 수정'}"
},
{
"name": "{'name': '태그2 추가'}"
}
],
"title": "제목 수정1",
"content": "내용 수정1",
"created_at": "2023-08-22T08:11:42.241124Z",
"updated_at": "2023-08-25T07:26:47.254487Z",
"hit": 1
}
```
### 3-5 게시글 삭제
---
* URL: /api/post/detail/1/delete/
* Method: POST
* Description: 특정 게시물을 웹 사이트에서 제거할 수 있습니다.

**응답**
```json
{
"msg": "Post deleted",
}
```
### 3-6 카테고리
---
* URL: api/post/categories
* Method: POST
* Description: 존재하는 모든 카테고리의 목록을 볼 수 있습니다.

**응답**
```json
{
    "id": 1,
    "name": "Programming"
},
{
    "id": 2,
    "name": "Ormi"
},
{
    "id": 3,
    "name": "ESTsoft"
}
```

### 3-7 게시물검색 
---
* URL: api/post/?search=title
* Method: POST
* Description: 작성한 키워드에 해당하는 게시물의 목록을 볼 수 있습니다.

**응답**
```json
{
    "id": 2,
    "tags": [],
    "title": "Title2",
    "content": "content2",
    "created_at": "2023-08-30T16:46:04.899333+09:00",
    "updated_at": "2023-08-30T16:58:01.008073+09:00",
    "hit": 0,
    "category": 1
},
{
    "id": 3,
    "tags": [],
    "title": "New Post Title",
    "content": "This is the content of the new post.",
    "created_at": "2023-08-30T17:03:28.688333+09:00",
    "updated_at": "2023-08-30T17:03:28.688333+09:00",
    "hit": 0,
    "category": 1
}
```
