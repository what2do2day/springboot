// 전역 변수
let stompClient = null;
let currentUser = null;
let currentRoom = null;
let messageCount = 0;
let shownMessages = new Set();

// UUID 생성 함수
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// DOM 요소들
const chatContainer = document.getElementById('chatContainer');
const messageInput = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');
const statusIndicator = document.getElementById('statusIndicator');
const statusText = document.getElementById('statusText');

// 사용자 정보 업데이트
function updateUserInfo() {
    const userId = document.getElementById('userId').value;
    const userName = document.getElementById('userName').value;
    const roomId = document.getElementById('roomId').value;
    
    currentUser = { id: userId, name: userName };
    currentRoom = roomId;
    
    document.getElementById('currentUser').textContent = `${userName} (${userId})`;
    document.getElementById('currentRoom').textContent = roomId;
}

// 상태 업데이트
function updateStatus(connected) {
    statusIndicator.className = `status-indicator ${connected ? 'status-connected' : 'status-disconnected'}`;
    statusText.textContent = connected ? '연결됨' : '연결되지 않음';
    document.getElementById('connectionStatus').textContent = connected ? '연결됨' : '연결되지 않음';
    
    messageInput.disabled = !connected;
    sendButton.disabled = !connected;
}

// 메시지 추가
function addMessage(message, isSent = false, messageId = null) {
    if (messageId && shownMessages.has(messageId)) return;
    if (messageId) shownMessages.add(messageId);
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${isSent ? 'sent' : 'received'}`;
    
    const messageInfo = document.createElement('div');
    messageInfo.className = 'message-info';
    messageInfo.textContent = `${isSent ? currentUser.name : '상대방'} - ${new Date().toLocaleTimeString()}`;
    
    const messageContent = document.createElement('div');
    messageContent.textContent = message;
    
    messageDiv.appendChild(messageInfo);
    messageDiv.appendChild(messageContent);
    
    chatContainer.appendChild(messageDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;
    
    messageCount++;
    document.getElementById('messageCount').textContent = messageCount;
}

// 시스템 메시지 추가
function addSystemMessage(message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'text-center text-muted mb-2';
    messageDiv.textContent = message;
    chatContainer.appendChild(messageDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

// WebSocket 연결
function connectWebSocket() {
    updateUserInfo();
    
    const serverUrl = document.getElementById('serverUrl').value;
    const socket = new SockJS(`${serverUrl}/ws/couple-chat`);
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        updateStatus(true);
        addSystemMessage('WebSocket 연결되었습니다! 💕');
        
        // 채팅방 구독
        stompClient.subscribe(`/topic/chat/${currentRoom}`, function (message) {
            const chatMessage = JSON.parse(message.body);
            const isSent = chatMessage.senderId === currentUser.id;
            addMessage(chatMessage.message, isSent, chatMessage.id);
        });
        
        // 개인 메시지 구독
        stompClient.subscribe(`/user/queue/messages`, function (message) {
            const chatMessage = JSON.parse(message.body);
            addMessage(chatMessage.message, false, chatMessage.id);
        });
        
    }, function (error) {
        console.log('STOMP error: ' + error);
        updateStatus(false);
        addSystemMessage('연결에 실패했습니다. 다시 시도해주세요.');
    });
}

// WebSocket 연결 해제
function disconnectWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
        updateStatus(false);
        addSystemMessage('연결이 해제되었습니다.');
    }
}

// 메시지 전송
function sendMessage() {
    const message = messageInput.value.trim();
    if (message && stompClient && stompClient.connected) {
        const chatMessage = {
            roomId: currentRoom,
            message: message,
            messageType: 'TEXT'
        };
        
        // WebSocket으로 전송
        stompClient.send("/app/send-message", {}, JSON.stringify(chatMessage));
        
        // REST API로도 전송 (백업)
        sendMessageViaRest(chatMessage);
        
        messageInput.value = '';
    }
}

// REST API로 메시지 전송
async function sendMessageViaRest(chatMessage) {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const response = await fetch(`${serverUrl}/api/v1/couple-chat/messages`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-ID': currentUser.id
            },
            body: JSON.stringify(chatMessage)
        });
        
        if (!response.ok) {
            console.error('Failed to send message via REST API');
        }
    } catch (error) {
        console.error('Error sending message via REST API:', error);
    }
}

// 채팅방 생성
async function createRoom() {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const coupleId = prompt('커플 ID를 입력하세요:', generateUUID());
        const user1Id = prompt('사용자1 ID를 입력하세요:', generateUUID());
        const user2Id = prompt('사용자2 ID를 입력하세요:', generateUUID());
        
        if (!coupleId || !user1Id || !user2Id) {
            alert('모든 정보를 입력해주세요.');
            return;
        }
        
        const response = await fetch(`${serverUrl}/api/v1/couple-chat/rooms?coupleId=${coupleId}&user1Id=${user1Id}&user2Id=${user2Id}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            const room = await response.json();
            document.getElementById('roomId').value = room.id;
            currentRoom = room.id;
            document.getElementById('currentRoom').textContent = room.id;
            addSystemMessage(`채팅방이 생성되었습니다: ${room.id}`);
        } else {
            alert('채팅방 생성에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error creating room:', error);
        alert('채팅방 생성 중 오류가 발생했습니다.');
    }
}

// 메시지 조회
async function getMessages() {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const response = await fetch(`${serverUrl}/api/v1/couple-chat/rooms/${currentRoom}/messages?page=0&size=20`, {
            headers: {
                'X-User-ID': currentUser.id
            }
        });
        
        if (response.ok) {
            const messages = await response.json();
            addSystemMessage(`메시지 ${messages.length}개를 조회했습니다.`);
            console.log('Messages:', messages);
        } else {
            alert('메시지 조회에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error getting messages:', error);
        alert('메시지 조회 중 오류가 발생했습니다.');
    }
}

// 읽음 처리
async function markAsRead() {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const response = await fetch(`${serverUrl}/api/v1/couple-chat/rooms/${currentRoom}/read`, {
            method: 'PUT',
            headers: {
                'X-User-ID': currentUser.id
            }
        });
        
        if (response.ok) {
            addSystemMessage('메시지를 읽음 처리했습니다.');
        } else {
            alert('읽음 처리에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error marking as read:', error);
        alert('읽음 처리 중 오류가 발생했습니다.');
    }
}

// 읽지 않은 메시지 수 조회
async function getUnreadCount() {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const response = await fetch(`${serverUrl}/api/v1/couple-chat/rooms/${currentRoom}/unread-count`, {
            headers: {
                'X-User-ID': currentUser.id
            }
        });
        
        if (response.ok) {
            const count = await response.json();
            addSystemMessage(`읽지 않은 메시지: ${count}개`);
        } else {
            alert('읽지 않은 메시지 수 조회에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error getting unread count:', error);
        alert('읽지 않은 메시지 수 조회 중 오류가 발생했습니다.');
    }
}

// 채팅 초기화
function clearChat() {
    chatContainer.innerHTML = '<div class="text-center text-muted">채팅방에 오신 것을 환영합니다! 💕</div>';
    messageCount = 0;
    document.getElementById('messageCount').textContent = messageCount;
}

// Enter 키로 메시지 전송
messageInput.addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
});

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    updateUserInfo();
    updateStatus(false);
}); 