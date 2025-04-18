FROM openjdk:21-jdk-slim

WORKDIR /app

COPY /target/api.jar /app/api.jar

ENTRYPOINT ["java", "-jar", "api.jar"]