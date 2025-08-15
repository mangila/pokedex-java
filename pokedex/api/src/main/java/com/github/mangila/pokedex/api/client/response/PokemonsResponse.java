package com.github.mangila.pokedex.api.client.response;

import com.github.mangila.pokedex.api.client.PokeApiUri;
import com.github.mangila.pokedex.shared.json.model.JsonTree;
import com.github.mangila.pokedex.shared.json.model.JsonValue;

import java.util.List;

public record PokemonsResponse(List<PokeApiUri> uris) {
    public static PokemonsResponse from(JsonTree jsonTree) {
        List<PokeApiUri> uris = jsonTree.getValue("results")
                .getArray()
                .values()
                .stream()
                .map(JsonValue::getObject)
                .map(jsonObject -> jsonObject.getString("url"))
                .map(PokeApiUri::fromString)
                .toList();
        return new PokemonsResponse(uris);
    }
}
