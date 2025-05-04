package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.func.VoidFunction;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Response;

import java.util.function.Function;

public interface HttpsClient {

    Function<GetRequest, Response> get();

    VoidFunction disconnect();

    VoidFunction connect();
}
