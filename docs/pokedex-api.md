# Pokedex API

The API module contains application-facing services that talk to external providers and expose typed operations to the rest of the system.

What it does:
- PokeApiClient: non-blocking HTTP/1.1 client for https://pokeapi.co powered by the shared JsonClient and TLS connection pool.
- PokemonDatabase facade: typed access to the database module specialized for the "pokedex" dataset.

Key classes:
- com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient
- com.github.mangila.pokedex.api.client.pokeapi.PokeApiClientConfig
- com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri
- com.github.mangila.pokedex.api.db.PokemonDatabase

Configuration:
- PokeApiClient.configureDefaultSettings() builds a JsonClientConfig using shared TLS connection pool and JSON parser.
- PokemonDatabase.configureDefaultSettings() wires a DefaultDatabase with database name "pokedex".

Lifecycle:
- Both PokeApiClient and PokemonDatabase are singletons with explicit configure(...), getInstance(), and shutdown/close semantics.

Usage example:
- PokeApiClient.getInstance().fetchAsync(PokeApiUri.species(id)) -> CompletableFuture<JsonRoot>
- PokemonDatabase.getInstance().instance().open()/close()