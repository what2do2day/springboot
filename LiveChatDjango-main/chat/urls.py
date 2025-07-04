from django.urls import re_path, include, path
from rest_framework import routers
from chat.views import RoomViewSet, ChatCreateView, create_room_simple, get_rooms, get_room_detail

router = routers.DefaultRouter()
router.register('room', RoomViewSet)

urlpatterns = [
    re_path(r'^', include(router.urls)),
    path('message/', ChatCreateView.as_view(), name='chat-create'),
    path('create-room/', create_room_simple, name='create-room-simple'),
    path('rooms/', get_rooms, name='get-rooms'),
    path('room/<int:room_id>/', get_room_detail, name='get-room-detail'),
]
