FROM openjdk:17-jdk-slim

WORKDIR /app

# 멀티모듈 프로젝트에서 couple-chat-service의 JAR 파일을 복사
COPY build/libs/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"] 