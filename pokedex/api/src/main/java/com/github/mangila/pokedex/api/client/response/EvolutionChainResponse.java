package com.github.mangila.pokedex.api.client.response;

import com.github.mangila.pokedex.shared.json.model.JsonArray;
import com.github.mangila.pokedex.shared.json.model.JsonObject;
import com.github.mangila.pokedex.shared.json.model.JsonTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record EvolutionChainResponse(List<Evolution> evolutions) {

    private static final EvolutionChainResponse EMPTY = new EvolutionChainResponse(Collections.emptyList());

    public record Evolution(int order, String name) {
    }

    public static EvolutionChainResponse from(JsonTree jsonTree) {
        List<Evolution> evolutions = new ArrayList<>();
        JsonObject firstChain = jsonTree.getObject("chain");
        if (firstChain.getArray("evolves_to").isEmpty()) {
            return EMPTY;
        }
        int order = 1;
        evolutions.add(new Evolution(order, firstChain
                .getObject("species")
                .getString("name")));
        order = order + 1;
        JsonArray nChain = firstChain.getArray("evolves_to");
        while (true) {
            if (nChain.isEmpty()) {
                return new EvolutionChainResponse(evolutions);
            }
            String nChainName = nChain.values()
                    .getFirst()
                    .getObject()
                    .getObject("species")
                    .getString("name");
            evolutions.add(new Evolution(order, nChainName));
            nChain = nChain.values().getFirst()
                    .getObject()
                    .getArray("evolves_to");
            order = order + 1;
        }
    }
}
