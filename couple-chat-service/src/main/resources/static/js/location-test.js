// 전역 변수
let stompClient = null;
let currentUser = null;
let currentRoom = null;
let locationCount = 0;
let autoLocationInterval = null;
let isAutoLocationEnabled = false;

// UUID 생성 함수
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// DOM 요소들
const locationContainer = document.getElementById('locationContainer');
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
}

// 위치 공유 메시지 추가
function addLocationMessage(locationMessage, isSent = false) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `location-message ${isSent ? 'sent' : ''}`;
    
    const locationInfo = document.createElement('div');
    locationInfo.className = 'location-info';
    locationInfo.textContent = `${isSent ? currentUser.name : '상대방'} - ${new Date().toLocaleTimeString()}`;
    
    const locationContent = document.createElement('div');
    locationContent.innerHTML = `
        <div class="location-content">
            <i class="fas fa-map-marker-alt text-danger"></i>
            <strong>위치 공유</strong><br>
            <small>위도: ${locationMessage.latitude}, 경도: ${locationMessage.longitude}</small><br>
            ${locationMessage.accuracy ? `<small>정확도: ${locationMessage.accuracy}m</small><br>` : ''}
            ${locationMessage.address ? `<small>주소: ${locationMessage.address}</small><br>` : ''}
            <button class="btn btn-sm btn-outline-primary mt-1" onclick="openLocationInMap(${locationMessage.latitude}, ${locationMessage.longitude})">
                <i class="fas fa-map"></i> 지도에서 보기
            </button>
        </div>
    `;
    
    messageDiv.appendChild(locationInfo);
    messageDiv.appendChild(locationContent);
    
    locationContainer.appendChild(messageDiv);
    locationContainer.scrollTop = locationContainer.scrollHeight;
    
    locationCount++;
    document.getElementById('locationCount').textContent = locationCount;
}

// 시스템 메시지 추가
function addSystemMessage(message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'text-center text-muted mb-2';
    messageDiv.textContent = message;
    locationContainer.appendChild(messageDiv);
    locationContainer.scrollTop = locationContainer.scrollHeight;
}

// 지도에서 위치 열기
function openLocationInMap(latitude, longitude) {
    const url = `https://www.google.com/maps?q=${latitude},${longitude}`;
    window.open(url, '_blank');
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
        addSystemMessage('WebSocket 연결되었습니다! 📍');
        
        // 위치 공유 토픽 구독
        stompClient.subscribe(`/topic/location/${currentRoom}`, function (message) {
            const locationMessage = JSON.parse(message.body);
            const isSent = locationMessage.senderId === currentUser.id;
            addLocationMessage(locationMessage, isSent);
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
        
        // 자동 위치 공유도 중지
        if (isAutoLocationEnabled) {
            stopAutoLocation();
        }
    }
}

// 현재 위치 가져오기
function getCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            function(position) {
                document.getElementById('latitude').value = position.coords.latitude;
                document.getElementById('longitude').value = position.coords.longitude;
                document.getElementById('accuracy').value = position.coords.accuracy || 10;
                addSystemMessage('현재 위치를 가져왔습니다!');
            },
            function(error) {
                console.error('Geolocation error:', error);
                addSystemMessage('위치를 가져올 수 없습니다. 수동으로 입력해주세요.');
            }
        );
    } else {
        addSystemMessage('이 브라우저는 위치 서비스를 지원하지 않습니다.');
    }
}

// 위치 공유
function shareLocation() {
    const latitude = parseFloat(document.getElementById('latitude').value);
    const longitude = parseFloat(document.getElementById('longitude').value);
    const accuracy = parseFloat(document.getElementById('accuracy').value);
    const address = document.getElementById('address').value;
    
    if (isNaN(latitude) || isNaN(longitude)) {
        alert('유효한 위도와 경도를 입력해주세요.');
        return;
    }
    
    const locationRequest = {
        roomId: currentRoom,
        latitude: latitude,
        longitude: longitude,
        accuracy: accuracy,
        address: address,
        messageType: 'LOCATION'
    };
    
    // WebSocket으로 전송
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/share-location", {}, JSON.stringify(locationRequest));
    }
    
    // REST API로도 전송
    shareLocationViaRest(locationRequest);
    
    addSystemMessage('위치를 공유했습니다!');
}

// 랜덤 위치 공유
function shareRandomLocation() {
    // 서울 지역 내 랜덤 위치 생성
    const seoulLatRange = [37.4133, 37.7151]; // 서울 위도 범위
    const seoulLngRange = [126.7341, 127.2693]; // 서울 경도 범위
    
    const randomLat = seoulLatRange[0] + Math.random() * (seoulLatRange[1] - seoulLatRange[0]);
    const randomLng = seoulLngRange[0] + Math.random() * (seoulLngRange[1] - seoulLngRange[0]);
    
    document.getElementById('latitude').value = randomLat.toFixed(6);
    document.getElementById('longitude').value = randomLng.toFixed(6);
    document.getElementById('accuracy').value = Math.floor(Math.random() * 50) + 5;
    document.getElementById('address').value = '서울특별시 (랜덤 위치)';
    
    shareLocation();
}

// REST API로 위치 공유
async function shareLocationViaRest(locationRequest) {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const response = await fetch(`${serverUrl}/api/v1/location/share`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-ID': currentUser.id
            },
            body: JSON.stringify(locationRequest)
        });
        
        if (!response.ok) {
            console.error('Failed to share location via REST API');
        }
    } catch (error) {
        console.error('Error sharing location via REST API:', error);
    }
}

// 커플 최신 위치 조회
async function getCoupleLatestLocations() {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const response = await fetch(`${serverUrl}/api/v1/location/couple/${currentRoom}/latest`, {
            headers: {
                'X-User-ID': currentUser.id
            }
        });
        
        if (response.ok) {
            const locations = await response.json();
            addSystemMessage(`커플 최신 위치 조회: ${locations.length}개 위치`);
            locations.forEach(location => {
                addLocationMessage(location, location.senderId === currentUser.id);
            });
        } else {
            addSystemMessage('커플 최신 위치 조회에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error getting couple latest locations:', error);
        addSystemMessage('커플 최신 위치 조회 중 오류가 발생했습니다.');
    }
}

// 내 위치 조회
async function getUserLocation() {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const response = await fetch(`${serverUrl}/api/v1/location/user/${currentUser.id}`, {
            headers: {
                'X-User-ID': currentUser.id
            }
        });
        
        if (response.ok) {
            const location = await response.json();
            addSystemMessage('내 위치 조회 성공');
            addLocationMessage(location, true);
        } else {
            addSystemMessage('내 위치 조회에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error getting user location:', error);
        addSystemMessage('내 위치 조회 중 오류가 발생했습니다.');
    }
}

// 위치 히스토리 조회
async function getLocationHistory() {
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const response = await fetch(`${serverUrl}/api/v1/location/couple/${currentRoom}/history?page=0&size=10`, {
            headers: {
                'X-User-ID': currentUser.id
            }
        });
        
        if (response.ok) {
            const history = await response.json();
            addSystemMessage(`위치 히스토리 조회: ${history.length}개 기록`);
            history.forEach(location => {
                addLocationMessage(location, location.senderId === currentUser.id);
            });
        } else {
            addSystemMessage('위치 히스토리 조회에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error getting location history:', error);
        addSystemMessage('위치 히스토리 조회 중 오류가 발생했습니다.');
    }
}

// 위치 기록 초기화
function clearLocations() {
    locationContainer.innerHTML = '<div class="text-center text-muted">위치 공유 기록이 여기에 표시됩니다 📍</div>';
    locationCount = 0;
    document.getElementById('locationCount').textContent = locationCount;
    addSystemMessage('위치 기록을 초기화했습니다.');
}

// 자동 위치 공유 토글
function toggleAutoLocation() {
    if (isAutoLocationEnabled) {
        stopAutoLocation();
    } else {
        startAutoLocation();
    }
}

// 자동 위치 공유 시작
function startAutoLocation() {
    if (!stompClient || !stompClient.connected) {
        alert('먼저 WebSocket에 연결해주세요.');
        return;
    }
    
    const interval = parseInt(document.getElementById('autoInterval').value) * 1000; // 초를 밀리초로 변환
    
    if (interval < 10000) { // 최소 10초
        alert('자동 공유 간격은 최소 10초 이상이어야 합니다.');
        return;
    }
    
    isAutoLocationEnabled = true;
    const autoLocationBtn = document.getElementById('autoLocationBtn');
    autoLocationBtn.innerHTML = '<i class="fas fa-stop"></i> 자동 위치 공유 중지';
    autoLocationBtn.className = 'btn btn-danger';
    
    addSystemMessage(`자동 위치 공유가 시작되었습니다. (${interval/1000}초 간격)`);
    
    // 즉시 첫 번째 위치 공유
    shareCurrentLocationAuto();
    
    // 주기적으로 위치 공유
    autoLocationInterval = setInterval(shareCurrentLocationAuto, interval);
}

// 자동 위치 공유 중지
function stopAutoLocation() {
    isAutoLocationEnabled = false;
    const autoLocationBtn = document.getElementById('autoLocationBtn');
    autoLocationBtn.innerHTML = '<i class="fas fa-play"></i> 자동 위치 공유 시작';
    autoLocationBtn.className = 'btn btn-warning';
    
    if (autoLocationInterval) {
        clearInterval(autoLocationInterval);
        autoLocationInterval = null;
    }
    
    addSystemMessage('자동 위치 공유가 중지되었습니다.');
}

// 자동 위치 공유용 함수 (에러 처리 없음)
function shareCurrentLocationAuto() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            function(position) {
                const locationRequest = {
                    roomId: currentRoom,
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude,
                    accuracy: position.coords.accuracy || 10,
                    address: '자동 위치 공유',
                    messageType: 'LOCATION'
                };
                
                // WebSocket으로 전송
                if (stompClient && stompClient.connected) {
                    stompClient.send("/app/share-location", {}, JSON.stringify(locationRequest));
                }
                
                // REST API로도 전송
                shareLocationViaRest(locationRequest);
                
                console.log('자동 위치 공유 완료:', new Date().toLocaleTimeString());
            },
            function(error) {
                console.error('자동 위치 공유 실패:', error);
                addSystemMessage('자동 위치 공유 중 오류가 발생했습니다.');
                stopAutoLocation(); // 오류 발생 시 자동 중지
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 30000 // 30초 이내의 캐시된 위치 사용
            }
        );
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    updateUserInfo();
}); 