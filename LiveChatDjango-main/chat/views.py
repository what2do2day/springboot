from rest_framework import viewsets, generics, status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from .models import Room, Chat
from .serializers import RoomSerializer, ChatSerializer

class RoomViewSet(viewsets.ModelViewSet):
    permission_classes = [AllowAny]  # 게이트웨이에서 이미 인증 처리
    queryset = Room.objects.all()
    serializer_class = RoomSerializer


class ChatCreateView(generics.CreateAPIView):
    permission_classes = [AllowAny]  # 게이트웨이에서 이미 인증 처리
    queryset = Chat.objects.all()
    serializer_class = ChatSerializer

    def create(self, request, *args, **kwargs):
        response = super().create(request, *args, **kwargs)
        
        chat = Chat.objects.get(pk=response.data['id'])

        post = chat.post

        room, created = Room.objects.get_or_create(post=post)

        room_data = RoomSerializer(room, context={'request': request}).data
        response.data['room_data'] = room_data

        return response


@api_view(['POST'])
@permission_classes([AllowAny])
def create_room_simple(request):
    """
    간단한 채팅방 생성 API
    게이트웨이에서 이미 인증된 요청만 받음
    """
    name = request.data.get('name', '새로운 채팅방')
    post_id = request.data.get('post_id', 1)
    
    try:
        from post.models import Post
        post = Post.objects.get(id=post_id)
    except Post.DoesNotExist:
        # Post가 없으면 기본값으로 생성
        post = Post.objects.create(title=f"채팅방 {name}", content="")
    
    # 게이트웨이에서 전달받은 유저 정보 사용
    # 실제로는 게이트웨이에서 owner 정보를 헤더로 전달받아야 함
    # 여기서는 임시로 첫 번째 유저를 사용
    from django.contrib.auth import get_user_model
    User = get_user_model()
    owner = User.objects.first()
    
    if not owner:
        return Response(
            {'error': '시스템에 유저가 없습니다.'}, 
            status=status.HTTP_400_BAD_REQUEST
        )
    
    room, created = Room.objects.get_or_create(
        post=post,
        defaults={
            'name': name,
            'owner': owner
        }
    )
    
    serializer = RoomSerializer(room, context={'request': request})
    return Response(serializer.data, status=status.HTTP_201_CREATED if created else status.HTTP_200_OK)


@api_view(['GET'])
@permission_classes([AllowAny])
def get_rooms(request):
    """
    채팅방 목록 조회 API
    """
    rooms = Room.objects.all()
    serializer = RoomSerializer(rooms, many=True, context={'request': request})
    return Response(serializer.data)


@api_view(['GET'])
@permission_classes([AllowAny])
def get_room_detail(request, room_id):
    """
    채팅방 상세 조회 API
    """
    try:
        room = Room.objects.get(id=room_id)
        serializer = RoomSerializer(room, context={'request': request})
        return Response(serializer.data)
    except Room.DoesNotExist:
        return Response(
            {'error': '채팅방을 찾을 수 없습니다.'}, 
            status=status.HTTP_404_NOT_FOUND
        )
