package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.RequestHeader;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.JsonParserConfig;
import com.github.mangila.pokedex.shared.testutil.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class PokeApiClientTest {

    private static PokeApiClient pokeApiClient;

    @BeforeAll
    static void beforeAll() {
        JsonParser.configure(new JsonParserConfig(64));
        pokeApiClient = TestUtil.createNewTestingPokeApiClient();
    }

    @Test
    void getJson_shouldReturnValidResponse() throws InterruptedException {
        // Given
        JsonRequest request = new JsonRequest("GET", "/api/v2/pokemon/ditto", Collections.emptyList());

        // When
        var response = pokeApiClient.getJson(request);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().httpStatus().code()).isEqualTo("200");
        assertThat(response.get().body()).isNotNull();
    }

    @Test
    void getJsonAsync_shouldReturnValidResponse() throws ExecutionException, InterruptedException {
        // Given
        JsonRequest request = new JsonRequest("GET", "/api/v2/pokemon/pikachu", Collections.emptyList());

        // When
        var futureResponse = pokeApiClient.getJsonAsync(request);
        var response = futureResponse.get();

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().httpStatus().code()).isEqualTo("200");
        assertThat(response.get().body()).isNotNull();
    }

    @Test
    void getJson_shouldCacheResponses() throws InterruptedException {
        // Given
        JsonRequest request = new JsonRequest("GET", "/api/v2/pokemon/bulbasaur", Collections.emptyList());

        // When
        var firstResponse = pokeApiClient.getJson(request);
        var secondResponse = pokeApiClient.getJson(request);

        // Then
        assertThat(firstResponse).isPresent();
        assertThat(secondResponse).isPresent();
        // Both responses should be the same object (from cache)
        assertThat(secondResponse.get()).isSameAs(firstResponse.get());
    }

    @Test
    void getJson_withCustomHeaders_shouldReturnValidResponse() throws InterruptedException {
        // Given
        List<RequestHeader> headers = List.of(
                new RequestHeader("User-Agent", "PokeApiClientTest")
        );
        JsonRequest request = new JsonRequest("GET", "/api/v2/pokemon/charmander", headers);

        // When
        var response = pokeApiClient.getJson(request);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().httpStatus().code()).isEqualTo("200");
        assertThat(response.get().body()).isNotNull();
    }

    @Test
    void getJson_withInvalidPath_shouldReturnEmptyOptional() throws InterruptedException {
        // Given
        JsonRequest request = new JsonRequest("GET", "/invalid/path", Collections.emptyList());

        // When
        var response = pokeApiClient.getJson(request);

        // Then
        assertThat(response).isEmpty();
    }
}
