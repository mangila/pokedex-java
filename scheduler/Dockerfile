FROM openjdk:21-jdk-slim

WORKDIR /app

COPY /target/scheduler.jar /app/scheduler.jar

ENTRYPOINT ["java", "-jar", "scheduler.jar"]