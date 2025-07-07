FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY certs/global-bundle.pem /app/ssl/global-bundle.pem

RUN keytool -importcert \
    -alias docdb \
    -keystore /usr/local/openjdk-17/lib/security/cacerts \
    -storepass changeit \
    -file /app/ssl/global-bundle.pem \
    -noprompt

EXPOSE 8086

ENTRYPOINT [ "java", "--add-opens=java.base/java.time=ALL-UNNAMED", "-Djavax.net.ssl.trustStore=/usr/local/openjdk-17/lib/security/cacerts", "-Djavax.net.ssl.trustStorePassword=changeit", "-Dspring.profiles.active=prod", "-jar", "app.jar" ]