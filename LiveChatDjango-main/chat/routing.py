from django.urls import path
from chat import consumers
import logging

logger = logging.getLogger(__name__)

websocket_urlpatterns = [
    path("", consumers.ChatConsumer.as_asgi()),  # 루트 경로
    path("ws", consumers.ChatConsumer.as_asgi()),  # Gateway에서 연결하는 경로
    path("ws/", consumers.ChatConsumer.as_asgi()),
    path("ws/chat/<str:room_pk>/chat/", consumers.ChatConsumer.as_asgi()),
]

# 디버깅을 위한 로그
logger.debug(f"WebSocket URL patterns loaded: {websocket_urlpatterns}")