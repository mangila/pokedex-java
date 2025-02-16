package com.github.mangila.scheduler.config;

import com.github.mangila.integration.pokeapi.response.species.EvolutionChain;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class EvolutionChainCacheKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        var evolutionChain = (EvolutionChain) params[0];
        var url = evolutionChain.url();
        String path = url.getPath();
        String[] pathSegments = path.split("/");
        return pathSegments[pathSegments.length - 1];
    }
}
