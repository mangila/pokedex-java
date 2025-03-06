package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonSpeciesPrototype;
import com.google.protobuf.Empty;

class DefaultMongoDbOperation implements MongoDb {

    private final MongoDbOperationGrpc.MongoDbOperationBlockingStub mongoDbOperationBlockingStub;

    public DefaultMongoDbOperation(MongoDbOperationGrpc.MongoDbOperationBlockingStub mongoDbOperationBlockingStub) {
        this.mongoDbOperationBlockingStub = mongoDbOperationBlockingStub;
    }


    @Override
    public Empty saveOne(PokemonSpeciesPrototype prototype) {
        return mongoDbOperationBlockingStub.saveOne(prototype);
    }
}
