package com.github.mangila.pokedex.backstage.bouncer.pokeapi.mapper;

import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.evolutionchain.EvolutionChain;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.generation.Species;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.Cries;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.Stats;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.Type;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.Types;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites.*;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species.*;
import com.github.mangila.pokedex.backstage.model.grpc.model.*;
import com.github.mangila.pokedex.backstage.model.grpc.model.Pokemon;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonId;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.github.mangila.pokedex.backstage.shared.util.JavaStreamUtil;
import com.github.mangila.pokedex.backstage.shared.util.UriUtil;
import com.google.protobuf.Any;
import com.google.protobuf.ListValue;
import com.google.protobuf.Value;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Map pokeapi http responses to proto models
 */
@Component
public class PokeApiProtoMapper {

    private static final Logger log = LoggerFactory.getLogger(PokeApiProtoMapper.class);
    private static final Matcher REPLACE_LINE_BREAKS = Pattern.compile("[\r\n\t\f]+").matcher(Strings.EMPTY);

    private final PokeApiTemplate pokeApiTemplate;
    private final RedisBouncerClient redisBouncerClient;

    public PokeApiProtoMapper(PokeApiTemplate pokeApiTemplate,
                              RedisBouncerClient redisBouncerClient) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.redisBouncerClient = redisBouncerClient;
    }

    public GenerationResponse map(com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.generation.GenerationResponse generationResponse) {
        return GenerationResponse.newBuilder()
                .addAllPokemonName(generationResponse.pokemonSpecies()
                        .stream()
                        .map(Species::name)
                        .toList())
                .build();
    }

    public PokemonSpecies map(PokemonSpeciesResponse response) {
        return PokemonSpecies.newBuilder()
                .setSpeciesId(response.id())
                .setName(response.name())
                .setGeneration(response.generation().name())
                .addAllNames(toNames(response.names()))
                .addAllEvolutions(toEvolutions(URI.create(response.evolutionChain().url())))
                .addAllGenera(toGenera(response.genera()))
                .addAllDescriptions(toDescriptions(response.flavorTextEntries()))
                .addAllVarieties(toVarieties(response.id(), response.varieties()))
                .setSpecial(toSpecial(response.baby(), response.legendary(), response.mythical()))
                .build();
    }

    private Iterable<PokemonName> toNames(List<Names> names) {
        return names.stream()
                .map(name -> PokemonName.newBuilder()
                        .setName(name.name())
                        .setLanguage(name.language().name())
                        .build())
                .toList();
    }

    private Iterable<PokemonEvolution> toEvolutions(URI url) {
        var evolutionChainId = UriUtil.getLastPathSegment(url);
        var entryRequest = EntryRequest.newBuilder()
                .setKey(RedisKeyPrefix.EVOLUTION_CHAIN_KEY_PREFIX.getPrefix().concat(evolutionChainId))
                .build();
        var cacheValue = redisBouncerClient.valueOps()
                .get(entryRequest, ListValue.class);
        if (cacheValue.isEmpty()) {
            log.debug("Cache miss for {}", url);
            var response = pokeApiTemplate.fetchEvolutionChain(PokemonId.create(evolutionChainId));
            if (CollectionUtils.isEmpty(response.chain().firstChain())) {
                redisBouncerClient.valueOps().set(entryRequest
                        .toBuilder()
                        .setValue(Any.pack(ListValue.getDefaultInstance()))
                        .build());
                return Collections.emptyList();
            }
            var chain = response.chain();
            var evolutions = getEvolutions(chain.firstChain(), new ArrayList<>(
                    List.of(PokemonEvolution.newBuilder()
                            .setOrder(0)
                            .setName(chain.species().name())
                            .build())
            ));
            var values = evolutions.stream()
                    .map(evolution -> Value.newBuilder()
                            .setStringValue(evolution.getName())
                            .setNumberValue(evolution.getOrder())
                            .build())
                    .toList();
            var protoList = ListValue.newBuilder()
                    .addAllValues(values)
                    .build();
            redisBouncerClient.valueOps().set(entryRequest.toBuilder()
                    .setValue(Any.pack(protoList))
                    .build());
            return evolutions;
        }
        return cacheValue.get()
                .getValuesList()
                .stream()
                .map(value -> PokemonEvolution.newBuilder()
                        .setOrder(value.getNullValueValue())
                        .setName(value.getStringValue())
                        .build())
                .toList();
    }

    private List<PokemonEvolution> getEvolutions(List<EvolutionChain> next,
                                                 ArrayList<PokemonEvolution> evolutions) {
        while (true) {
            if (CollectionUtils.isEmpty(next)) {
                return evolutions;
            }
            var chain = next.getFirst();
            evolutions.add(PokemonEvolution.newBuilder()
                    .setOrder(evolutions.size())
                    .setName(chain.species().name())
                    .build());
            next = chain.nextChain();
        }
    }

    private Iterable<PokemonGenera> toGenera(List<Genera> genera) {
        return genera.stream()
                .map(g -> PokemonGenera.newBuilder()
                        .setGenera(g.genus())
                        .setLanguage(g.language().name())
                        .build())
                .toList();
    }

    private Iterable<PokemonDescription> toDescriptions(List<FlavorTextEntries> flavorTextEntries) {
        return flavorTextEntries.stream()
                .filter(JavaStreamUtil.distinctByKey(FlavorTextEntries::language))
                .map(text -> PokemonDescription.newBuilder()
                        .setDescription(REPLACE_LINE_BREAKS.reset(text.flavorText()).replaceAll(" "))
                        .setLanguage(text.language().name())
                        .build())
                .toList();
    }

    private Iterable<Pokemon> toVarieties(int speciesId, List<Varieties> varieties) {
        return varieties.stream()
                .peek(variety -> log.debug("variety : {}", variety.pokemon().name()))
                .map(variety -> variety.pokemon().name())
                .map(com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName::create)
                .map(pokeApiTemplate::fetchPokemon)
                .map(response -> Pokemon.newBuilder()
                        .setPokemonId(response.id())
                        .setName(response.name())
                        .setHeight(toMeter(response.height()))
                        .setWeight(toKilogram(response.weight()))
                        .setIsDefault(response.isDefault())
                        .addAllTypes(response.types().stream().map(Types::type).map(Type::name).toList())
                        .addAllStats(toStats(response.stats()))
                        .addAllMedia(toMedia(response.name(), speciesId, response.id(), response.sprites(), response.cries()))
                        .build())
                .toList();
    }

    private Iterable<PokemonStat> toStats(List<Stats> stats) {
        AtomicInteger total = new AtomicInteger();
        var list = new ArrayList<>(stats.stream()
                .peek(stat -> total.addAndGet(stat.baseStat()))
                .map(stat -> PokemonStat.newBuilder()
                        .setName(stat.stat().name())
                        .setValue(stat.baseStat())
                        .build())
                .toList());
        list.add(PokemonStat.newBuilder()
                .setName("total")
                .setValue(total.get())
                .build());
        return list;
    }

    /**
     * <summary>
     * PokeApi returns height in Decimeters
     * </summary>
     */
    private String toMeter(int height) {
        int meter = (height / 10);
        return String.valueOf(meter);
    }

    /**
     * <summary>
     * PokeApi returns weight in Hectograms
     * </summary>
     */
    private String toKilogram(int weight) {
        int kilogram = (weight / 10);
        return String.valueOf(kilogram);
    }

    private PokemonSpecial toSpecial(boolean baby,
                                     boolean legendary,
                                     boolean mythical) {
        var isSpecial = baby && legendary && mythical;
        return PokemonSpecial.newBuilder()
                .setIsSpecial(isSpecial)
                .setBaby(baby)
                .setLegendary(legendary)
                .setMythical(mythical)
                .build();
    }

    private Iterable<PokemonMedia> toMedia(String name,
                                           int speciesId,
                                           int pokemonId,
                                           Sprites sprites,
                                           Cries cries) {
        return Stream.of(
                        // Audio
                        createMediaIfNotNull(name, speciesId, pokemonId, "latest",
                                Optional.ofNullable(cries).map(Cries::latest).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "legacy",
                                Optional.ofNullable(cries).map(Cries::legacy).orElse(null)),
                        // Sprites
                        createMediaIfNotNull(name, speciesId, pokemonId, "front-default",
                                Optional.ofNullable(sprites).map(Sprites::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "back-default",
                                Optional.ofNullable(sprites).map(Sprites::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "back-female",
                                Optional.ofNullable(sprites).map(Sprites::backFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "front-female",
                                Optional.ofNullable(sprites).map(Sprites::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "front-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::frontShinyFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "back-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::backShinyFemale).orElse(null)),
                        // DreamWorld
                        createMediaIfNotNull(name, speciesId, pokemonId, "dreamworld-front-default",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::dreamWorld).map(DreamWorld::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "dreamworld-front-female",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::dreamWorld).map(DreamWorld::frontFemale).orElse(null)),
                        // Home
                        createMediaIfNotNull(name, speciesId, pokemonId, "home-front-default",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::home).map(Home::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "home-front-female",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::home).map(Home::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "home-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::home).map(Home::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "home-front-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::home).map(Home::frontShinyFemale).orElse(null)),
                        // Official Artwork
                        createMediaIfNotNull(name, speciesId, pokemonId, "official-artwork-front-default",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::officialArtwork).map(OfficialArtwork::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "official-artwork-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::officialArtwork).map(OfficialArtwork::frontShiny).orElse(null)),
                        // Showdown
                        createMediaIfNotNull(name, speciesId, pokemonId, "showdown-back-default",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::showdown).map(Showdown::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "showdown-back-female",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::showdown).map(Showdown::backFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "showdown-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::showdown).map(Showdown::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "showdown-back-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::showdown).map(Showdown::backShinyFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "showdown-front-default",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::showdown).map(Showdown::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "showdown-front-female",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::showdown).map(Showdown::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "showdown-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::showdown).map(Showdown::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "showdown-front-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::other).map(Other::showdown).map(Showdown::frontShinyFemale).orElse(null)),
                        // Generation I - RedBlue
                        createMediaIfNotNull(name, speciesId, pokemonId, "redblue-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::redBlue).map(RedBlue::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "redblue-back-gray",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::redBlue).map(RedBlue::backGray).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "redblue-back-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::redBlue).map(RedBlue::backTransparent).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "redblue-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::redBlue).map(RedBlue::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "redblue-front-gray",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::redBlue).map(RedBlue::frontGray).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "redblue-front-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::redBlue).map(RedBlue::frontTransparent).orElse(null)),
                        // Generation I - Yellow
                        createMediaIfNotNull(name, speciesId, pokemonId, "yellow-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::yellow).map(Yellow::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "yellow-back-gray",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::yellow).map(Yellow::backGray).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "yellow-back-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::yellow).map(Yellow::backTransparent).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "yellow-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationI).map(GenerationI::yellow).map(Yellow::frontDefault).orElse(null)),
                        // Generation II - Crystal
                        createMediaIfNotNull(name, speciesId, pokemonId, "crystal-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::crystal).map(Crystal::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "crystal-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::crystal).map(Crystal::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "crystal-back-shiny-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::crystal).map(Crystal::backShinyTransparent).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "crystal-back-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::crystal).map(Crystal::backTransparent).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "crystal-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::crystal).map(Crystal::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "crystal-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::crystal).map(Crystal::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "crystal-front-shiny-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::crystal).map(Crystal::frontShinyTransparent).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "crystal-front-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::crystal).map(Crystal::frontTransparent).orElse(null)),
                        // Generation II - Gold
                        createMediaIfNotNull(name, speciesId, pokemonId, "gold-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::gold).map(Gold::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "gold-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::gold).map(Gold::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "gold-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::gold).map(Gold::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "gold-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::gold).map(Gold::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "gold-front-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::gold).map(Gold::frontTransparent).orElse(null)),
                        // Generation II - Silver
                        createMediaIfNotNull(name, speciesId, pokemonId, "silver-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::silver).map(Silver::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "silver-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::silver).map(Silver::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "silver-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::silver).map(Silver::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "silver-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::silver).map(Silver::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "silver-front-transparent",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationII).map(GenerationII::silver).map(Silver::frontTransparent).orElse(null)),
                        // Generation III - Emerald
                        createMediaIfNotNull(name, speciesId, pokemonId, "emerald-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::emerald).map(Emerald::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "emerald-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::emerald).map(Emerald::frontShiny).orElse(null)),
                        // Generation III - FireredLeafgreen
                        createMediaIfNotNull(name, speciesId, pokemonId, "firered-leafgreen-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::fireredLeafgreen).map(FireredLeafgreen::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "firered-leafgreen-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::fireredLeafgreen).map(FireredLeafgreen::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "firered-leafgreen-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::fireredLeafgreen).map(FireredLeafgreen::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "firered-leafgreen-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::fireredLeafgreen).map(FireredLeafgreen::frontShiny).orElse(null)),
                        // Generation III - RubySapphire
                        createMediaIfNotNull(name, speciesId, pokemonId, "ruby-sapphire-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::rubySapphire).map(RubySapphire::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "ruby-sapphire-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::rubySapphire).map(RubySapphire::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "ruby-sapphire-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::rubySapphire).map(RubySapphire::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "ruby-sapphire-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIII).map(GenerationIII::rubySapphire).map(RubySapphire::frontShiny).orElse(null)),
                        // Generation IV - DiamondPearl
                        createMediaIfNotNull(name, speciesId, pokemonId, "diamond-pearl-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::diamondPearl).map(DiamondPearl::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "diamond-pearl-back-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::diamondPearl).map(DiamondPearl::backFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "diamond-pearl-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::diamondPearl).map(DiamondPearl::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "diamond-pearl-back-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::diamondPearl).map(DiamondPearl::backShinyFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "diamond-pearl-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::diamondPearl).map(DiamondPearl::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "diamond-pearl-front-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::diamondPearl).map(DiamondPearl::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "diamond-pearl-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::diamondPearl).map(DiamondPearl::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "diamond-pearl-front-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::diamondPearl).map(DiamondPearl::frontShinyFemale).orElse(null)),
                        // Generation IV - HeartgoldSoulsilver
                        createMediaIfNotNull(name, speciesId, pokemonId, "heartgold-soulsilver-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::heartgoldSoulsilver).map(HeartgoldSoulsilver::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "heartgold-soulsilver-back-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::heartgoldSoulsilver).map(HeartgoldSoulsilver::backFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "heartgold-soulsilver-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::heartgoldSoulsilver).map(HeartgoldSoulsilver::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "heartgold-soulsilver-back-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::heartgoldSoulsilver).map(HeartgoldSoulsilver::backShinyFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "heartgold-soulsilver-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::heartgoldSoulsilver).map(HeartgoldSoulsilver::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "heartgold-soulsilver-front-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::heartgoldSoulsilver).map(HeartgoldSoulsilver::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "heartgold-soulsilver-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::heartgoldSoulsilver).map(HeartgoldSoulsilver::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "heartgold-soulsilver-front-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::heartgoldSoulsilver).map(HeartgoldSoulsilver::frontShinyFemale).orElse(null)),
                        // Generation IV - Platinum
                        createMediaIfNotNull(name, speciesId, pokemonId, "platinum-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::platinum).map(Platinum::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "platinum-back-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::platinum).map(Platinum::backFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "platinum-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::platinum).map(Platinum::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "platinum-back-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::platinum).map(Platinum::backShinyFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "platinum-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::platinum).map(Platinum::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "platinum-front-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::platinum).map(Platinum::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "platinum-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::platinum).map(Platinum::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "platinum-front-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationIV).map(GenerationIV::platinum).map(Platinum::frontShinyFemale).orElse(null)),
                        // Generation V - BlackWhite Animated
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-animated-back-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::animated).map(Animated::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-animated-back-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::animated).map(Animated::backFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-animated-back-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::animated).map(Animated::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-animated-back-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::animated).map(Animated::backShinyFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-animated-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::animated).map(Animated::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-animated-front-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::animated).map(Animated::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-animated-front-shiny",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::animated).map(Animated::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-animated-front-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::animated).map(Animated::frontShinyFemale).orElse(null)),
                        // Generation V - BlackWhite Static
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-back-default", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::backDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-back-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::backFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-back-shiny", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::backShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-back-shiny-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::backShinyFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-front-default", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-front-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-front-shiny", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "blackwhite-front-shiny-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationV).map(GenerationV::blackWhite).map(BlackWhite::frontShinyFemale).orElse(null)),
                        // Generation VI - OmegaRubyAlphaSapphire
                        createMediaIfNotNull(name, speciesId, pokemonId, "omegaruby-alphasapphire-front-default", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVI).map(GenerationVI::omegarubyAlphasapphire).map(OmegarubyAlphasapphire::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "omegaruby-alphasapphire-front-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVI).map(GenerationVI::omegarubyAlphasapphire).map(OmegarubyAlphasapphire::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "omegaruby-alphasapphire-front-shiny", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVI).map(GenerationVI::omegarubyAlphasapphire).map(OmegarubyAlphasapphire::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "omegaruby-alphasapphire-front-shiny-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVI).map(GenerationVI::omegarubyAlphasapphire).map(OmegarubyAlphasapphire::frontShinyFemale).orElse(null)),
                        // Generation VI - XY
                        createMediaIfNotNull(name, speciesId, pokemonId, "xy-front-default", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVI).map(GenerationVI::xy).map(XY::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "xy-front-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVI).map(GenerationVI::xy).map(XY::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "xy-front-shiny", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVI).map(GenerationVI::xy).map(XY::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "xy-front-shiny-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVI).map(GenerationVI::xy).map(XY::frontShinyFemale).orElse(null)),
                        // Generation VII - Icons
                        createMediaIfNotNull(name, speciesId, pokemonId, "icons-front-default", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVII).map(GenerationVII::icons).map(Icons::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "icons-front-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVII).map(GenerationVII::icons).map(Icons::frontFemale).orElse(null)),
                        // Generation VII - UltraSunUltraMoon
                        createMediaIfNotNull(name, speciesId, pokemonId, "ultrasun-ultramoon-front-default", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVII).map(GenerationVII::ultraSunUltraMoon).map(UltraSunUltraMoon::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "ultrasun-ultramoon-front-female", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVII).map(GenerationVII::ultraSunUltraMoon).map(UltraSunUltraMoon::frontFemale).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "ultrasun-ultramoon-front-shiny", Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVII).map(GenerationVII::ultraSunUltraMoon).map(UltraSunUltraMoon::frontShiny).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "ultrasun-ultramoon-front-shiny-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVII).map(GenerationVII::ultraSunUltraMoon).map(UltraSunUltraMoon::frontShinyFemale).orElse(null)),
                        // Generation VIII - Icons
                        createMediaIfNotNull(name, speciesId, pokemonId, "generationviii-icons-front-default",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVIII).map(GenerationVIII::icons).map(Icons::frontDefault).orElse(null)),
                        createMediaIfNotNull(name, speciesId, pokemonId, "generationviii-icons-front-female",
                                Optional.ofNullable(sprites).map(Sprites::versions).map(Versions::generationVIII).map(GenerationVIII::icons).map(Icons::frontFemale).orElse(null))
                )
                .filter(Objects::nonNull)
                .toList();
    }


    private PokemonMedia createMediaIfNotNull(String name,
                                              int speciesId,
                                              int pokemonId,
                                              String description,
                                              String url) {
        if (StringUtils.hasText(url)) {
            return PokemonMedia.newBuilder()
                    .setPokemonName(name)
                    .setSpeciesId(speciesId)
                    .setPokemonId(pokemonId)
                    .setDescription(description)
                    .setUrl(url)
                    .build();
        }
        return null;
    }
}
