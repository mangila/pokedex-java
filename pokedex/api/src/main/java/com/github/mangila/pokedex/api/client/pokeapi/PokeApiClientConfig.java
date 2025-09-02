package com.github.mangila.pokedex.api.client.pokeapi;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.https.http.json.JsonClientConfig;

public record PokeApiClientConfig(JsonClientConfig jsonClientConfig,
                                  TtlCacheConfig cacheConfig) {
}
