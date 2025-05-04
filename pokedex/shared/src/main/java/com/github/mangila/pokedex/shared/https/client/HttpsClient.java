package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.func.VoidFunction;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Response;

import java.util.function.Function;
import java.util.function.Supplier;

public interface HttpsClient {

    Function<GetRequest, Response> get();

    Supplier<Boolean> isConnected();

    VoidFunction disconnect();

    VoidFunction connect();
}
