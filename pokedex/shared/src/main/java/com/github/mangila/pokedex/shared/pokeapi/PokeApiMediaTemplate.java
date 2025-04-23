package com.github.mangila.pokedex.shared.pokeapi;

import com.github.mangila.pokedex.shared.model.PokeApiUri;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@lombok.extern.slf4j.Slf4j
public class PokeApiMediaTemplate {

    private final RestClient http;

    public PokeApiMediaTemplate() {
        this.http = RestClient.builder()
                .requestFactory(new ReactorClientHttpRequestFactory())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            throw new ResponseStatusException(response.getStatusCode(), "Image not found: " + request.getURI());
                        })
                .build();
    }

    public MediaResponse fetchMedia(PokeApiUri pokeApiUri) {
        log.debug("Fetching media: {}", pokeApiUri);
        return http.get()
                .uri(pokeApiUri.uri())
                .exchange((request, response) -> {
                    var contentType = response.getHeaders().getContentType();
                    long contentLength = response.getHeaders().getContentLength();
                    byte[] imageData = response.getBody().readAllBytes();
                    return new MediaResponse(
                            imageData,
                            contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM,
                            contentLength,
                            response.getHeaders().getLastModified()
                    );
                });
    }
}
