package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.pokemonspecies.PokemonSpecies;
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
