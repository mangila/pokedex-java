package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.scheduler.model.EvolutionChainUrl;
import com.github.mangila.pokedex.scheduler.model.VarietyUrl;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.mangila.pokedex.shared.Config.POKEMON_EVOLUTION_CHAIN_URL_QUEUE;
import static com.github.mangila.pokedex.shared.Config.POKEMON_VARIETY_URL_QUEUE;

public record InsertSpeciesResponseTaskTask(PokeApiClient pokeApiClient,
                                            Queue queue,
                                            PokemonDatabase database) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSpeciesResponseTaskTask.class);

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_POOL = VirtualThreadFactory.newScheduledThreadPool(10);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        LOGGER.info("Scheduling {}", name());
        SCHEDULED_EXECUTOR_POOL.scheduleAtFixedRate(this,
                100,
                100,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean shutdown() {
        LOGGER.info("Shutting down {}", name());
        var duration = Duration.ofSeconds(30);
        return VirtualThreadFactory.terminateGracefully(SCHEDULED_EXECUTOR_POOL, duration);
    }

    @Override
    public void run() {
        QueueEntry queueEntry = queue.poll();
        if (queueEntry == null) {
            LOGGER.debug("Queue is empty");
            return;
        }
        try {
            PokeApiUri uri = queueEntry.unwrapAs(PokeApiUri.class);
            JsonRoot jsonRoot = pokeApiClient.fetch(uri)
                    .join();
            String key = "pokemon::".concat(getId(jsonRoot).toString());
            LOGGER.info("{}", key);
            QueueService.getInstance()
                    .add(POKEMON_EVOLUTION_CHAIN_URL_QUEUE, new QueueEntry(getEvolutionChainUrl(key, jsonRoot)));
            getVarietiesUrls(key, jsonRoot)
                    .forEach(url -> QueueService.getInstance()
                            .add(POKEMON_VARIETY_URL_QUEUE, new QueueEntry(url)));
            insertFields(key, jsonRoot, database);
        } catch (Exception e) {
            LOGGER.error("ERR", e);
            if (queueEntry.equalsMaxRetries(3)) {
                queue.addDlq(queueEntry);
                return;
            }
            queueEntry.incrementFailCounter();
            queue.add(queueEntry);
        }
    }

    private static EvolutionChainUrl getEvolutionChainUrl(String key, JsonRoot jsonRoot) {
        String url = jsonRoot.getObject("evolution_chain")
                .getString("url");
        return new EvolutionChainUrl(PokeApiUri.from(url), key);
    }

    private static List<VarietyUrl> getVarietiesUrls(String key, JsonRoot jsonRoot) {
        return jsonRoot.getArray("varieties")
                .values()
                .stream()
                .map(variety -> PokeApiUri.from(variety.unwrapObject()
                        .getObject("pokemon")
                        .getString("url")))
                .map(uri -> new VarietyUrl(uri, key))
                .toList();
    }

    private static void insertFields(String key, JsonRoot jsonRoot, PokemonDatabase database) {
        insertName(jsonRoot, key, database);
        insertColor(jsonRoot, key, database);
        insertDescription(jsonRoot, key, database);
        insertGenus(jsonRoot, key, database);
        insertBabyFlag(jsonRoot, key, database);
        insertLegendaryFlag(jsonRoot, key, database);
        insertMythicalFlag(jsonRoot, key, database);
    }

    private static BigInteger getId(JsonRoot jsonRoot) {
        return (BigInteger) jsonRoot.getNumber("id");
    }

    private static void insertName(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        byte[] name = jsonRoot.getString("name")
                .getBytes(Charset.defaultCharset());
        database.instance().engine()
                .putAsync(key, "name", name);
    }

    private static void insertColor(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        byte[] color = jsonRoot.getObject("color")
                .getString("name")
                .getBytes(Charset.defaultCharset());
        database.instance()
                .engine()
                .putAsync(key, "color", color);
    }

    private static void insertDescription(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        byte[] englishDescription = jsonRoot.getArray("flavor_text_entries")
                .values()
                .stream()
                .filter(jsonValue -> jsonValue.unwrapObject().getObject("language").getString("name").equals("en"))
                .findFirst()
                .orElseThrow()
                .unwrapObject()
                .getString("flavor_text")
                .getBytes(Charset.defaultCharset());
        database.instance().engine()
                .putAsync(key,
                        "description",
                        englishDescription);
    }

    private static void insertGenus(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        byte[] genus = jsonRoot.getArray("genera")
                .values()
                .stream()
                .filter(entry -> entry.unwrapObject().getObject("language").getString("name").equals("en"))
                .findFirst()
                .orElseThrow()
                .unwrapObject()
                .getString("genus")
                .getBytes(Charset.defaultCharset());
        database.instance().engine()
                .putAsync(key, "genus", genus);
    }

    private static void insertBabyFlag(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        boolean baby = jsonRoot.getBoolean("is_baby");
        database.instance().engine()
                .putAsync(key,
                        "baby",
                        new byte[]{baby ? (byte) 1 : (byte) 0});
    }

    private static void insertLegendaryFlag(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        boolean legendary = jsonRoot.getBoolean("is_legendary");
        database.instance().engine()
                .putAsync(key,
                        "legendary",
                        new byte[]{legendary ? (byte) 1 : (byte) 0});
    }

    private static void insertMythicalFlag(JsonRoot jsonRoot, String key, PokemonDatabase database) {
        boolean mythical = jsonRoot.getBoolean("is_mythical");
        database.instance().engine()
                .putAsync(key,
                        "mythical",
                        new byte[]{mythical ? (byte) 1 : (byte) 0});
    }
}
