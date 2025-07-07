FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]