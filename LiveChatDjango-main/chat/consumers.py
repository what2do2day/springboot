from asgiref.sync import async_to_sync
from channels.generic.websocket import JsonWebsocketConsumer
from .models import Room
import json

class ChatConsumer(JsonWebsocketConsumer):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.group_name = ""
        self.room = None
        self.user_id = None
        self.nickname = None
        self.email = None
        self.profile_url = None
    
    def connect(self):
        # 게이트웨이에서 전달한 헤더 정보를 받음
        headers = dict(self.scope.get('headers', []))
        
        # 헤더에서 유저 정보 추출
        self.user_id = headers.get(b'x-user-id', b'').decode('utf-8', errors='ignore')
        self.nickname = headers.get(b'x-nickname', b'').decode('utf-8', errors='ignore')
        self.email = headers.get(b'x-email', b'').decode('utf-8', errors='ignore')
        self.profile_url = headers.get(b'x-profile-url', b'').decode('utf-8', errors='ignore')
        
        # 쿼리 파라미터에서도 확인 (백업)
        if not self.user_id or not self.nickname:
            query_string = self.scope.get('query_string', b'').decode()
            query_params = dict(param.split('=') for param in query_string.split('&') if '=' in param)
            
            self.user_id = self.user_id or query_params.get('user_id', '')
            self.nickname = self.nickname or query_params.get('nickname', '')
            self.email = self.email or query_params.get('email', '')
            self.profile_url = self.profile_url or query_params.get('profile_url', '')
        
        print(f"헤더 정보: {headers}")
        print(f"유저 정보: user_id={self.user_id}, nickname={self.nickname}")
        
        # 필수 유저 정보 검증
        if not self.user_id or not self.nickname:
            print(f"유저 정보 누락: user_id={self.user_id}, nickname={self.nickname}")
            self.close(code=4001)  # 유저 정보 없음
            return
        
        # room_pk가 있는 경우에만 채팅방 검증
        room_pk = self.scope["url_route"]["kwargs"].get("room_pk")
        if room_pk:
            try:
                self.room = Room.objects.get(pk=room_pk)
                self.group_name = self.room.chat_group_name
            except Room.DoesNotExist:
                print(f"채팅방 없음: room_id={room_pk}")
                self.close(code=4003)  # 채팅방 없음
                return
        else:
            # 기본 채팅방 또는 전체 채팅방
            self.group_name = "general_chat"
        
        # 채팅방 그룹에 추가
        async_to_sync(self.channel_layer.group_add)(
            self.group_name,
            self.channel_name,
        )
        
        # 입장 메시지 전송
        async_to_sync(self.channel_layer.group_send)(
            self.group_name,
            {
                "type": "chat.user.join",
                "username": self.nickname,
            }
        )
        
        self.accept()
        print(f"채팅방 입장: user_id={self.user_id}, nickname={self.nickname}, room_id={room_pk}")

    def disconnect(self, code):
        if self.group_name:
            async_to_sync(self.channel_layer.group_discard)(
                self.group_name,
                self.channel_name,
            )

        if self.room is not None and self.user_id is not None:
            # 퇴장 메시지 전송
            async_to_sync(self.channel_layer.group_send)(
                self.group_name,
                {
                    "type": "chat.user.leave",
                    "username": self.nickname,
                }
            )
            print(f"채팅방 퇴장: user_id={self.user_id}, nickname={self.nickname}")
    
    def receive_json(self, content, **kwargs):
        if not self.user_id:
            return

        _type = content.get("type", "chat.message")

        if _type == "chat.message":
            message = content.get("message", content)
            
            async_to_sync(self.channel_layer.group_send)(
                self.group_name,
                {
                    "type": "chat.message",
                    "message": message,
                    "sender": self.email,
                    "nickname": self.nickname,
                    "profile_picture_url": self.profile_url
                }
            )
        else:
            print(f"Invalid message type : {_type}")

    def receive(self, text_data=None, bytes_data=None, **kwargs):
        """일반 텍스트 메시지도 처리"""
        if not self.user_id:
            return

        if text_data:
            try:
                # JSON 형식인지 확인
                import json
                content = json.loads(text_data)
                self.receive_json(content, **kwargs)
            except json.JSONDecodeError:
                # 일반 텍스트인 경우
                async_to_sync(self.channel_layer.group_send)(
                    self.group_name,
                    {
                        "type": "chat.message",
                        "message": text_data,
                        "sender": self.email,
                        "nickname": self.nickname,
                        "profile_picture_url": self.profile_url
                    }
                )
        
    def chat_user_join(self, message_dict):
        self.send_json({
            "type": "chat.user.join",
            "username": message_dict["username"]
        })

    def chat_user_leave(self, message_dict):
        self.send_json({
            "type": "chat.user.leave",
            "username": message_dict["username"]
        })

    def chat_message(self, message_dict):
        self.send_json({
            "type": "chat.message",
            "message": message_dict["message"],
            "sender": message_dict["sender"],
            "nickname": message_dict["nickname"],
            "profile_picture_url": message_dict["profile_picture_url"],
        })