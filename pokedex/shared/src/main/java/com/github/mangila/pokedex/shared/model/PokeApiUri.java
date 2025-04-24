package com.github.mangila.pokedex.shared.model;


import java.net.URI;
import java.util.Objects;

/**
 * A value object representing a validated URI from PokeApi.
 * This class ensures that the URI is from a trusted source (pokeapi.co or raw.githubusercontent.com)
 * and uses the HTTPS protocol.
 */
public record PokeApiUri(URI uri) {

    public PokeApiUri {
        Objects.requireNonNull(uri, "URI cannot be null");
        ensureUriFromPokeApi(uri);
    }

    /**
     * Creates a validated PokeApi URI from a string representation.
     *
     * @param uri the string representation of the URI
     * @return a validated PokeApiUri instance
     * @throws IllegalArgumentException if the URI is not valid or doesn't meet the PokeApi requirements
     */
    public static PokeApiUri create(String uri) {
        Objects.requireNonNull(uri, "URI string cannot be null");
        URI parsedUri = URI.create(uri);
        return new PokeApiUri(parsedUri);
    }

    private static void ensureUriFromPokeApi(URI uri) {
        var host = uri.getHost();
        if (Objects.equals(host, "pokeapi.co") || Objects.equals(host, "raw.githubusercontent.com")) {
            if (!Objects.equals(uri.getScheme(), "https")) {
                throw new IllegalArgumentException("should be 'https' - " + uri);
            }
        } else {
            throw new IllegalArgumentException("should start with 'raw.githubusercontent.com' or 'pokeapi.co' - " + uri);
        }
    }

    public String toUriString() {
        return uri.toString();
    }
}
