package com.github.mangila.pokedex.backstage.integration.bouncer.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public Empty saveOne(PokemonSpeciesDocument document) {
        return mongoDbOperationBlockingStub.saveOne(document.toProto());
    }
}
