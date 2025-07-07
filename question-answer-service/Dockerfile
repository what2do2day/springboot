FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8086

ENTRYPOINT [ "java", "--add-opens=java.base/java.time=ALL-UNNAMED", "-Djavax.net.ssl.trustStore=/usr/local/openjdk-17/lib/security/cacerts", "-Djavax.net.ssl.trustStorePassword=changeit", "-Dspring.profiles.active=prod", "-jar", "app.jar" ]