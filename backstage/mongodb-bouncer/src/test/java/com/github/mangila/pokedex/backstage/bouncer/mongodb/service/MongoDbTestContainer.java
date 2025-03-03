package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

abstract class MongoDbTestContainer {
    private static final DockerImageName MONGODB_IMAGE_NAME = DockerImageName.parse("mongo");
    @ServiceConnection
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(MONGODB_IMAGE_NAME);
}
