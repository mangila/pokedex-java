package com.github.mangila.pokedex.api.client.pokeapi.response;

import com.github.mangila.pokedex.shared.json.model.JsonRoot;

public interface JsonMapper<SELF> {
    SELF map(JsonRoot jsonRoot);
}
