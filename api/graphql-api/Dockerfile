FROM openjdk:21-jdk-slim

WORKDIR /app

COPY /target/graphql-api.jar /app/graphql-api.jar

ENTRYPOINT ["java", "-jar", "graphql-api.jar"]