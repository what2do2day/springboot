#!/usr/bin/env python3
"""
실시간 채팅 API 테스트 스크립트
사용법: python test_chat_api.py
"""

import asyncio
import websockets
import json
import requests
import time
from datetime import datetime

class ChatAPITester:
    def __init__(self, base_url="http://127.0.0.1:8000"):
        self.base_url = base_url
        self.access_token = None
        self.user_email = None
        
    def register_user(self, email, password, nickname):
        """사용자 회원가입"""
        url = f"{self.base_url}/user/v1/register/"
        data = {
            "email": email,
            "password1": password,
            "password2": password,
            "nickname": nickname
        }
        
        try:
            response = requests.post(url, json=data)
            if response.status_code == 201:
                print(f"✅ 회원가입 성공: {email}")
                return True
            else:
                print(f"❌ 회원가입 실패: {response.status_code} - {response.text}")
                return False
        except Exception as e:
            print(f"❌ 회원가입 오류: {e}")
            return False
    
    def login_user(self, email, password):
        """사용자 로그인"""
        url = f"{self.base_url}/user/v1/login/"
        data = {
            "email": email,
            "password": password
        }
        
        try:
            response = requests.post(url, json=data)
            if response.status_code == 200:
                data = response.json()
                self.access_token = data.get('access')
                self.user_email = email
                print(f"✅ 로그인 성공: {email}")
                return True
            else:
                print(f"❌ 로그인 실패: {response.status_code} - {response.text}")
                return False
        except Exception as e:
            print(f"❌ 로그인 오류: {e}")
            return False
    
    def create_room(self, name, post_id=1):
        """채팅방 생성"""
        if not self.access_token:
            print("❌ 로그인이 필요합니다.")
            return None
            
        url = f"{self.base_url}/chat/v1/room/"
        headers = {
            "Authorization": f"Bearer {self.access_token}",
            "Content-Type": "application/json"
        }
        data = {
            "name": name,
            "post": post_id
        }
        
        try:
            response = requests.post(url, json=data, headers=headers)
            if response.status_code == 201:
                room_data = response.json()
                print(f"✅ 채팅방 생성 성공: {name} (ID: {room_data['id']})")
                return room_data
            else:
                print(f"❌ 채팅방 생성 실패: {response.status_code} - {response.text}")
                return None
        except Exception as e:
            print(f"❌ 채팅방 생성 오류: {e}")
            return None
    
    def get_rooms(self):
        """채팅방 목록 조회"""
        if not self.access_token:
            print("❌ 로그인이 필요합니다.")
            return []
            
        url = f"{self.base_url}/chat/v1/room/"
        headers = {
            "Authorization": f"Bearer {self.access_token}"
        }
        
        try:
            response = requests.get(url, headers=headers)
            if response.status_code == 200:
                rooms = response.json()
                print(f"✅ 채팅방 목록 조회 성공: {len(rooms)}개")
                return rooms
            else:
                print(f"❌ 채팅방 목록 조회 실패: {response.status_code}")
                return []
        except Exception as e:
            print(f"❌ 채팅방 목록 조회 오류: {e}")
            return []
    
    async def connect_to_chat(self, room_id, user_name):
        """WebSocket을 통한 채팅 연결"""
        ws_url = f"ws://127.0.0.1:8000/ws/chat/{room_id}/chat/"
        
        try:
            async with websockets.connect(ws_url) as websocket:
                print(f"✅ WebSocket 연결 성공: {user_name}")
                
                # 메시지 수신 대기
                async def receive_messages():
                    try:
                        while True:
                            message = await websocket.recv()
                            data = json.loads(message)
                            timestamp = datetime.now().strftime("%H:%M:%S")
                            
                            if data['type'] == 'chat.message':
                                print(f"[{timestamp}] {data['sender']}: {data['message']}")
                            elif data['type'] == 'chat.user.join':
                                print(f"[{timestamp}] 👋 {data['username']}님이 입장했습니다.")
                            elif data['type'] == 'chat.user.leave':
                                print(f"[{timestamp}] 👋 {data['username']}님이 퇴장했습니다.")
                            else:
                                print(f"[{timestamp}] 시스템: {data}")
                    except websockets.exceptions.ConnectionClosed:
                        print(f"❌ WebSocket 연결이 종료되었습니다: {user_name}")
                
                # 메시지 수신 태스크 시작
                receive_task = asyncio.create_task(receive_messages())
                
                # 사용자 입력 처리
                try:
                    while True:
                        message = input(f"[{user_name}] 메시지 입력 (종료: 'quit'): ")
                        if message.lower() == 'quit':
                            break
                        
                        if message.strip():
                            chat_message = {
                                "type": "chat.message",
                                "message": message
                            }
                            await websocket.send(json.dumps(chat_message))
                            print(f"📤 메시지 전송: {message}")
                
                except KeyboardInterrupt:
                    print(f"\n🛑 {user_name} 채팅 종료")
                
                finally:
                    receive_task.cancel()
                    
        except Exception as e:
            print(f"❌ WebSocket 연결 오류: {e}")

async def main():
    """메인 테스트 함수"""
    print("🚀 실시간 채팅 API 테스트 시작")
    print("=" * 50)
    
    tester = ChatAPITester()
    
    # 테스트 사용자 정보
    users = [
        {"email": "user1@test.com", "password": "testpass123", "nickname": "사용자1"},
        {"email": "user2@test.com", "password": "testpass123", "nickname": "사용자2"}
    ]
    
    # 1. 사용자 회원가입 및 로그인
    print("\n📝 1단계: 사용자 등록 및 로그인")
    print("-" * 30)
    
    for user in users:
        # 회원가입 시도
        tester.register_user(user["email"], user["password"], user["nickname"])
        time.sleep(1)
        
        # 로그인 시도
        tester.login_user(user["email"], user["password"])
        time.sleep(1)
    
    # 2. 채팅방 생성
    print("\n🏠 2단계: 채팅방 생성")
    print("-" * 30)
    
    # 첫 번째 사용자로 로그인
    tester.login_user(users[0]["email"], users[0]["password"])
    
    # 채팅방 생성
    room = tester.create_room("테스트 채팅방")
    if not room:
        print("❌ 채팅방 생성에 실패했습니다. 기존 채팅방을 사용합니다.")
        rooms = tester.get_rooms()
        if rooms:
            room = rooms[0]
            print(f"✅ 기존 채팅방 사용: {room['name']} (ID: {room['id']})")
        else:
            print("❌ 사용 가능한 채팅방이 없습니다.")
            return
    
    room_id = room['id']
    
    # 3. 다중 사용자 채팅 테스트
    print(f"\n💬 3단계: 다중 사용자 채팅 테스트 (채팅방 ID: {room_id})")
    print("-" * 30)
    print("각 사용자가 별도의 터미널에서 채팅에 참여할 수 있습니다.")
    print("사용법: python test_chat_api.py --user <사용자번호>")
    print("=" * 50)
    
    # 현재 사용자로 채팅 참여
    await tester.connect_to_chat(room_id, users[0]["nickname"])

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 2 and sys.argv[1] == "--user":
        # 특정 사용자로 채팅 참여
        user_num = int(sys.argv[2])
        if user_num == 1:
            user_info = {"email": "user1@test.com", "password": "testpass123", "nickname": "사용자1"}
        elif user_num == 2:
            user_info = {"email": "user2@test.com", "password": "testpass123", "nickname": "사용자2"}
        else:
            print("❌ 잘못된 사용자 번호입니다. 1 또는 2를 사용하세요.")
            sys.exit(1)
        
        async def single_user_chat():
            tester = ChatAPITester()
            tester.login_user(user_info["email"], user_info["password"])
            rooms = tester.get_rooms()
            if rooms:
                await tester.connect_to_chat(rooms[0]['id'], user_info["nickname"])
            else:
                print("❌ 사용 가능한 채팅방이 없습니다.")
        
        asyncio.run(single_user_chat())
    else:
        # 메인 테스트 실행
        asyncio.run(main()) 