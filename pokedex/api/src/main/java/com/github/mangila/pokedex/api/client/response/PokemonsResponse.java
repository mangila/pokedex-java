package com.github.mangila.pokedex.api.client.response;

import com.github.mangila.pokedex.api.client.PokeApiUri;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.json.model.JsonValue;

import java.util.List;

public record PokemonsResponse(List<PokeApiUri> uris) {
    public static PokemonsResponse from(JsonRoot jsonRoot) {
        return new PokemonsResponseMapper().map(jsonRoot);
    }

    private static class PokemonsResponseMapper implements JsonMapper<PokemonsResponse> {
        @Override
        public PokemonsResponse map(JsonRoot jsonRoot) {
            List<PokeApiUri> uris = getResultsArray(jsonRoot);
            return new PokemonsResponse(uris);
        }

        public List<PokeApiUri> getResultsArray(JsonRoot jsonRoot) {
            return jsonRoot.getValue("results")
                    .unwrapArray()
                    .values()
                    .stream()
                    .map(JsonValue::unwrapObject)
                    .map(jsonObject -> jsonObject.getString("url"))
                    .map(PokeApiUri::from)
                    .toList();
        }
    }
}
