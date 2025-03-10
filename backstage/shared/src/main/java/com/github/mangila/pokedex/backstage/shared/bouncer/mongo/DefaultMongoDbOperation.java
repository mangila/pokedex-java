package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpecies;
import com.github.mangila.pokedex.backstage.model.grpc.service.MongoDbGrpc;
import com.google.protobuf.Empty;

class DefaultMongoDbOperation implements MongoDb {

    private final MongoDbGrpc.MongoDbBlockingStub stub;

    DefaultMongoDbOperation(MongoDbGrpc.MongoDbBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public Empty saveOne(PokemonSpecies pokemonSpecies) {
        return stub.saveOne(pokemonSpecies);
    }
}
