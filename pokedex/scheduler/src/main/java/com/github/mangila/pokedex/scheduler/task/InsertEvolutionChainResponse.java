package com.github.mangila.pokedex.scheduler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record InsertEvolutionChainResponse() implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertEvolutionChainResponse.class);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {

    }

    @Override
    public void shutdown() {
        LOGGER.info("Shutting down {}", name());
    }

    @Override
    public void run() {

    }
}
