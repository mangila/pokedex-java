package com.github.mangila.pokedex.app;

import com.github.mangila.pokedex.shared.Config;

public class Application {


    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.configurePokemonDatabase();
        bootstrap.configurePokeApiClient();
        bootstrap.initQueues();
        bootstrap.initScheduler();
        while (true) {
            Boolean shutdown = Config.SHUTDOWN_QUEUE.take();
            if (shutdown) {
                break;
            }
        }
    }
}