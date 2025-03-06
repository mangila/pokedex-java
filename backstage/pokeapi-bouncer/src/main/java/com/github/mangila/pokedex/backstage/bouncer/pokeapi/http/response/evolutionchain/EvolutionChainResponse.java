package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.evolutionchain;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.EvolutionChainResponsePrototype;

public record EvolutionChainResponse(Chain chain) {

    public EvolutionChainResponsePrototype toProto() {
        return EvolutionChainResponsePrototype.newBuilder()
                .setChain(chain.toProto())
                .build();
    }

}