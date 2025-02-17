FROM openjdk:21-jdk-slim

WORKDIR /app

COPY /target/graphql-server.jar /app/graphql-server.jar

ENTRYPOINT ["java", "-jar", "graphql-server.jar"]