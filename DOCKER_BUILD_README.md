# Docker 빌드 및 배포 가이드

## 개요
이 프로젝트는 마이크로서비스 아키텍처로 구성되어 있으며, 각 서비스는 Docker 컨테이너로 빌드되어 Naver Cloud Container Registry에 배포됩니다.

## 서비스 목록
- **Gateway Service**: 포트 8080
- **User-Couple Service**: 포트 8081
- **Schedule-Meeting Service**: 포트 8082
- **Question-Answer Service**: 포트 8086
- **LiveChatDjango Service**: 포트 8000

## 사전 준비사항
1. Docker가 설치되어 있어야 합니다.
2. Naver Cloud Container Registry에 로그인되어 있어야 합니다.
   - 로그인 명령어: `docker login hyundae.kr.ncr.ntruss.com`
3. 각 서비스의 JAR 파일이 `build/libs/` 디렉토리에 생성되어 있어야 합니다.

## 사용 방법

### 1. 개별 서비스 빌드 및 배포
각 서비스 디렉토리에서 다음 명령어를 실행합니다:

```bash
# 실행 권한 부여
chmod +x build-and-push.sh

# 빌드 및 배포 실행
./build-and-push.sh
```

### 2. 모든 서비스 한 번에 빌드 및 배포
프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다:

```bash
# 실행 권한 부여
chmod +x build-all-services.sh

# 모든 서비스 빌드 및 배포
./build-all-services.sh
```

### 3. 실행 권한 일괄 부여
```bash
chmod +x make-executable.sh
./make-executable.sh
```

## Docker 이미지 태그 형식
모든 이미지는 다음 형식으로 태그됩니다:
```
hyundae.kr.ncr.ntruss.com/[서비스명]:latest
```

- 이미지 push: `docker push hyundae.kr.ncr.ntruss.com/<TARGET_IMAGE[:TAG]>`
- 이미지 pull: `docker pull hyundae.kr.ncr.ntruss.com/<TARGET_IMAGE[:TAG]>`

## 주의사항
- `[서비스명]` 부분을 실제 서비스 이름(gateway, user-couple 등)으로 변경해야 합니다.
- Spring Boot 서비스들은 Gradle을 사용하여 빌드되므로, 빌드 전에 `./gradlew build` 명령어가 실행됩니다.
- LiveChatDjango 서비스는 Python 기반이므로 별도의 빌드 과정 없이 Docker 이미지만 생성됩니다.

## 문제 해결
1. **권한 오류**: 스크립트에 실행 권한이 없는 경우 `chmod +x` 명령어로 권한을 부여하세요.
2. **빌드 실패**: 각 서비스의 `build/libs/` 디렉토리에 JAR 파일이 있는지 확인하세요.
3. **Docker 로그인 오류**: `docker login hyundae.kr.ncr.ntruss.com` 명령어로 로그인하세요. 