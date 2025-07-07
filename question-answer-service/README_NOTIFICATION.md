# Question Answer Service - 알림 기능 구현

## 개요

question-answer-service에 매 1분마다 실행되는 스케줄러와 FCM 푸시 알림 기능을 구현했습니다.

## 구현된 기능

### 1. 스케줄러 (ScheduleNotificationScheduler)
- 매 1분마다 실행 (`@Scheduled(fixedRate = 60000)`)
- 현재 시각과 일치하는 `send_time`을 가진 유저들을 조회
- 유효한 FCM 토큰을 가진 유저들에게 데이트 일정 알림 전송

### 2. User 엔티티
- `users` 테이블과 매핑
- `send_time`: "HH:mm" 형식으로 저장되는 알림 전송 시각
- `fcm_token`: Firebase Cloud Messaging 토큰

### 3. FCM 서비스 (FcmService)
- Firebase Cloud Messaging을 통한 푸시 알림 전송
- 단일/일괄 알림 전송 지원
- 에러 처리 및 로깅

### 4. 테스트 API (NotificationController)
- 현재 시각에 알림을 받을 유저 조회
- 특정 시각에 알림을 받을 유저 조회
- 수동 알림 전송 테스트
- 모든 유저 조회

## 데이터베이스 스키마

### users 테이블
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    send_time VARCHAR(5), -- "HH:mm" 형식
    fcm_token TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## API 엔드포인트

### 알림 관련 API
- `GET /api/notifications/current-time-users` - 현재 시각에 알림을 받을 유저 조회
- `GET /api/notifications/users-by-time?time=HH:mm` - 특정 시각에 알림을 받을 유저 조회
- `POST /api/notifications/send-test?time=HH:mm` - 수동 알림 전송 테스트
- `GET /api/notifications/all-users` - 모든 유저 조회

## 설정

### 1. 스케줄링 활성화
메인 애플리케이션 클래스에 `@EnableScheduling` 어노테이션이 추가되어 있습니다.

### 2. Firebase 설정 (TODO)
현재는 Firebase 설정이 주석 처리되어 있습니다. 실제 FCM 기능을 사용하려면:

1. Firebase Admin SDK JSON 파일을 `src/main/resources/firebase-adminsdk.json`에 배치
2. `FirebaseConfig.java`의 주석을 해제
3. `FcmService.java`의 실제 FCM 전송 로직 주석을 해제

## 테스트 방법

### 1. 샘플 데이터 확인
```bash
curl http://localhost:8080/api/notifications/all-users
```

### 2. 특정 시각의 유저 조회
```bash
curl "http://localhost:8080/api/notifications/users-by-time?time=09:00"
```

### 3. 수동 알림 전송 테스트
```bash
curl -X POST "http://localhost:8080/api/notifications/send-test?time=09:00"
```

### 4. 현재 시각 알림 테스트
```bash
curl -X POST http://localhost:8080/api/notifications/send-test
```

## 로그 확인

스케줄러 실행 로그:
```
INFO  - Starting schedule notification scheduler...
INFO  - Found X users to send notifications at time: HH:mm
INFO  - Schedule notification completed. Successfully sent to X/Y users
```

FCM 전송 로그:
```
INFO  - FCM notification would be sent to user X: title='데이트 일정 알림', body='오늘의 일정을 확인해보세요!', token='...'
```

## 주의사항

1. **Firebase 설정**: 실제 FCM 기능을 사용하려면 Firebase 프로젝트 설정과 Admin SDK JSON 파일이 필요합니다.
2. **FCM 토큰 유효성**: null이거나 빈 문자열인 FCM 토큰은 자동으로 필터링됩니다.
3. **에러 처리**: FCM 전송 실패 시에도 스케줄러는 중단되지 않습니다.
4. **성능**: 매 1분마다 실행되므로 데이터베이스 인덱스가 중요합니다.

## 향후 개선 사항

1. Firebase Admin SDK 설정 완료
2. 알림 전송 실패 시 재시도 로직
3. 알림 전송 이력 관리
4. 알림 템플릿 관리
5. 알림 설정 개인화 