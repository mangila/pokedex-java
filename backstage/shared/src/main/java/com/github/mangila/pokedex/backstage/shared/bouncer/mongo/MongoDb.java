package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpecies;
import com.google.protobuf.Empty;

public interface MongoDb {

    Empty saveOne(PokemonSpecies pokemonSpecies);

}
