package com.github.mangila.pokedex.shared.pokeapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PokeApiHttpsClient extends HttpsClient {

    public PokeApiHttpsClient(String host) {
        super(host);
    }

    public Response execute(Request request) {
        try {
            var input = getSocket().getInputStream();
            var output = getSocket().getOutputStream();
            var reader = new BufferedReader(new InputStreamReader(input));
            var r = request.toRawHttpRequest(getHost());
            output.write(r.getBytes());
            output.flush();
            var statusCode = reader.readLine();
            System.out.println(statusCode);
        } catch (Exception e) {

        }
        return new Response();
    }

}
