FROM openjdk:21-jdk-slim

WORKDIR /app

COPY /target/file-server.jar /app/file-server.jar

ENTRYPOINT ["java", "-jar", "file-server.jar"]