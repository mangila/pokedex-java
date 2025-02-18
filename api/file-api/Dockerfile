FROM openjdk:21-jdk-slim

WORKDIR /app

COPY /target/file-api.jar /app/file-api.jar

ENTRYPOINT ["java", "-jar", "file-api.jar"]