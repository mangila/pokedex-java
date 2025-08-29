package com.github.mangila.pokedex.app;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.scheduler.Scheduler;
import com.github.mangila.pokedex.shared.Config;

public class Application {


    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.configurePokemonDatabase();
        bootstrap.configurePokeApiClient();
        bootstrap.initQueues();
        Scheduler scheduler = bootstrap.initScheduler();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //TODO: try catch or run in separate thread
            PokeApiClient.getInstance().shutdown();
            scheduler.shutdown();
            PokemonDatabase.getInstance().instance().close();
        }));
        while (true) {
            Boolean shutdown = Config.SHUTDOWN_QUEUE.take();
            if (shutdown) {
                break;
            }
        }
    }
}