package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonSpeciesPrototype;
import com.google.protobuf.Empty;

class DefaultMongoDbOperation implements MongoDb {

    private final MongoDbGrpc.MongoDbBlockingStub stub;

    DefaultMongoDbOperation(MongoDbGrpc.MongoDbBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public Empty saveOne(PokemonSpeciesPrototype prototype) {
        return stub.saveOne(prototype);
    }
}
