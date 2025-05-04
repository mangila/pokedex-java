package com.github.mangila.pokedex.shared.https.internal;

import com.github.mangila.pokedex.shared.https.model.Response;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

class ResponseTtlCacheTest {

    @Test
    void abc() throws InterruptedException {
        var cache = new ResponseTtlCache(Duration.ofSeconds(5));
        cache.put("key", new Response("statusLine", null, null));
        TimeUnit.SECONDS.sleep(20);
    }

}