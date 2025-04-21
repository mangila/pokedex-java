package com.github.mangila.pokedex.shared.pokeapi;

import org.springframework.http.MediaType;

public record MediaResponse(
        byte[] imageData,
        MediaType mediaType,
        long contentLength,
        long lastModified
) {
}
