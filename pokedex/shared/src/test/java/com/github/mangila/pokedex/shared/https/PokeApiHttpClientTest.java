package com.github.mangila.pokedex.shared.https;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PokeApiHttpClientTest {

    @Test
    void abc() {
       var http =  new PokeApiHttpClient();
        http.connect(new Request(URI.create("https://pokeapi.co/api/v2/pokemon-species/bulbasaur"), "GET", new Header[0]));
       var k =  http.execute(new Request(URI.create("https://pokeapi.co/api/v2/pokemon-species/bulbasaur"),
               "GET",
               new Header[]{
                       new Header("Accept", "application/json"),
               }));
        System.out.println(Arrays.toString(k));
        http.disconnect();
    }

}