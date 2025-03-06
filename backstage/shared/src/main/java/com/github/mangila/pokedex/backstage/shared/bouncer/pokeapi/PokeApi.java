package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GenerationResponsePrototype;
import com.google.protobuf.StringValue;

public interface PokeApi {
    GenerationResponsePrototype getGeneration(StringValue request);
}
