package com.github.mangila.pokedex.backstage.integration.bouncer.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.InsertRequest;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbOperationGrpc;
import com.github.mangila.pokedex.backstage.shared.model.document.PokemonSpeciesDocument;
import com.google.protobuf.Empty;
import org.springframework.stereotype.Service;

@Service
public class MongoBouncerClient {

    private final MongoDbOperationGrpc.MongoDbOperationBlockingStub mongoDbOperationBlockingStub;
    private final ObjectMapper objectMapper;

    public MongoBouncerClient(MongoDbOperationGrpc.MongoDbOperationBlockingStub mongoDbOperationBlockingStub,
                              ObjectMapper objectMapper) {
        this.mongoDbOperationBlockingStub = mongoDbOperationBlockingStub;
        this.objectMapper = objectMapper;
    }

    public Empty insertOne(PokemonSpeciesDocument document) {
        return mongoDbOperationBlockingStub.insertOne(
                InsertRequest.newBuilder()
                        .setType(PokemonSpeciesDocument.class.getName())
                        .setCollection("pokemon-species")
                        .setData(document.toJson(objectMapper))
                        .build()
        );
    }
}
