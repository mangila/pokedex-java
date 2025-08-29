package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.scheduler.KeyUriPair;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static com.github.mangila.pokedex.shared.Config.POKEMON_EVOLUTION_CHAIN_URL_QUEUE;
import static com.github.mangila.pokedex.shared.Config.POKEMON_VARIETY_URL_QUEUE;

public class InsertSpeciesResponseTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSpeciesResponseTask.class);
    private final PokeApiClient pokeApiClient;
    private final BlockingQueue queue;
    private final PokemonDatabase database;

    public InsertSpeciesResponseTask(PokeApiClient pokeApiClient,
                                     BlockingQueue queue,
                                     PokemonDatabase database) {
        this.pokeApiClient = pokeApiClient;
        this.queue = queue;
        this.database = database;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.submit(this);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueEntry queueEntry;
            try {
                queueEntry = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("{} interrupted", name());
                Thread.currentThread().interrupt();
                break;
            }
            PokeApiUri uri = queueEntry.unwrapAs(PokeApiUri.class);
            pokeApiClient.fetchAsync(uri)
                    .thenAcceptAsync(jsonRoot -> {
                        BigInteger id = (BigInteger) jsonRoot.getNumber("id");
                        String key = jsonRoot.getString("name");
                        LOGGER.info("#{} {}", id, key);
                        QueueService.getInstance()
                                .add(POKEMON_EVOLUTION_CHAIN_URL_QUEUE, new QueueEntry(getEvolutionChainUrl(key, jsonRoot)));
                        getVarietiesUrls(key, jsonRoot)
                                .forEach(url -> QueueService.getInstance()
                                        .add(POKEMON_VARIETY_URL_QUEUE, new QueueEntry(url)));
                        insertFieldsToDatabase(key, jsonRoot, database);
                    }, VirtualThreadFactory.newThreadPerTaskExecutor())
                    .exceptionallyAsync(throwable -> {
                        LOGGER.error("ERR", throwable);
                        if (queueEntry.equalsMaxRetries(3)) {
                            queue.addDlq(queueEntry);
                            return null;
                        }
                        queueEntry.incrementFailCounter();
                        queue.add(queueEntry);
                        return null;
                    }, VirtualThreadFactory.newThreadPerTaskExecutor());
        }
    }

    private static KeyUriPair getEvolutionChainUrl(String key, JsonRoot jsonRoot) {
        String url = jsonRoot.getObject("evolution_chain")
                .getString("url");
        return new KeyUriPair(key, PokeApiUri.from(url));
    }

    private static List<KeyUriPair> getVarietiesUrls(String key, JsonRoot jsonRoot) {
        return jsonRoot.getArray("varieties")
                .values()
                .stream()
                .map(variety -> PokeApiUri.from(variety.unwrapObject()
                        .getObject("pokemon")
                        .getString("url")))
                .map(uri -> new KeyUriPair(key, uri))
                .toList();
    }

    private static void insertFieldsToDatabase(String key, JsonRoot jsonRoot, PokemonDatabase database) {
        insertId(jsonRoot, key, database);
        insertName(jsonRoot, key, database);
        insertColor(jsonRoot, key, database);
        insertDescription(jsonRoot, key, database);
        insertGenus(jsonRoot, key, database);
        insertBabyFlag(jsonRoot, key, database);
        insertLegendaryFlag(jsonRoot, key, database);
        insertMythicalFlag(jsonRoot, key, database);
    }

    private static void insertId(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        BigInteger id = (BigInteger) jsonRoot.getNumber("id");
        database.instance().putAsync(key, "id", id);
    }

    private static void insertName(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        String name = jsonRoot.getString("name");
        database.instance().putAsync(key, "name", name);
    }

    private static void insertColor(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        String color = jsonRoot.getObject("color")
                .getString("name");
        database.instance().putAsync(key, "color", color);
    }

    private static void insertDescription(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        String englishDescription = jsonRoot.getArray("flavor_text_entries")
                .values()
                .stream()
                .filter(jsonValue -> jsonValue.unwrapObject().getObject("language").getString("name").equals("en"))
                .findFirst()
                .orElseThrow()
                .unwrapObject()
                .getString("flavor_text");
        database.instance().putAsync(key, "description", englishDescription);
    }

    private static void insertGenus(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        String genus = jsonRoot.getArray("genera")
                .values()
                .stream()
                .filter(entry -> entry.unwrapObject().getObject("language").getString("name").equals("en"))
                .findFirst()
                .orElseThrow()
                .unwrapObject()
                .getString("genus");
        database.instance().putAsync(key, "genus", genus);
    }


    private static void insertBabyFlag(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        boolean baby = jsonRoot.getBoolean("is_baby");
        database.instance().putAsync(key, "baby", baby);
    }

    private static void insertLegendaryFlag(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        boolean legendary = jsonRoot.getBoolean("is_legendary");
        database.instance().putAsync(key, "legendary", legendary);
    }

    private static void insertMythicalFlag(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        boolean mythical = jsonRoot.getBoolean("is_mythical");
        database.instance().putAsync(key, "mythical", mythical);
    }
}
