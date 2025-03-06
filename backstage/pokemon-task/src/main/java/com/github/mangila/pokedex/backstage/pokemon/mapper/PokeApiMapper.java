package com.github.mangila.pokedex.backstage.pokemon.mapper;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.*;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.*;
import com.github.mangila.pokedex.backstage.pokemon.handler.PokemonMediaHandler;
import com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi.PokeApiBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonId;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName;
import com.google.protobuf.ProtocolStringList;
import com.google.protobuf.StringValue;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class PokeApiMapper {

    private static final Logger log = LoggerFactory.getLogger(PokeApiMapper.class);
    private static final Matcher REPLACE_LINE_BREAKS = Pattern.compile("[\r\n\t\f]+").matcher(Strings.EMPTY);

    private final PokeApiBouncerClient pokeApiBouncerClient;
    private final PokemonMediaHandler pokemonMediaHandler;

    public PokeApiMapper(PokeApiBouncerClient pokeApiBouncerClient,
                         PokemonMediaHandler pokemonMediaHandler) {
        this.pokeApiBouncerClient = pokeApiBouncerClient;
        this.pokemonMediaHandler = pokemonMediaHandler;
    }

    public PokemonSpeciesPrototype toProto(PokemonSpeciesResponsePrototype response) {
        var id = PokemonId.create(response.getId());
        var name = PokemonName.create(response.getName());
        return PokemonSpeciesPrototype.newBuilder()
                .setId(id.getValueAsInteger())
                .setName(name.getValue())
                .setGeneration(response.getGeneration())
                .addAllNames(toPokemonNames(response.getNamesList()))
                .addAllDescriptions(toDescriptions(response.getFlavorTextEntriesList()))
                .addAllGenera(toGenera(response.getGeneraList()))
                .addAllEvolutions(toEvolutions(URI.create(response.getEvolutionChainUrl())))
                .addAllVarieties(toVarieties(id, response.getVarietiesList()))
                .setSpecial(toSpecial(response.getIsBaby(), response.getIsLegendary(), response.getIsMythical()))
                .build();
    }

    private Iterable<PokemonPrototype> toVarieties(PokemonId speciesId, ProtocolStringList varietiesList) {
        return varietiesList.stream()
                .peek(variety -> log.debug("Fetching variety: {}", variety))
                .map(PokemonName::create)
                .map(PokemonName::getValue)
                .map(StringValue::of)
                .map(pokeApiBouncerClient::fetchPokemon)
                .peek(response -> {
                    log.debug("adding pokemon media for pokemon: {}", response.getName());
                    var pokemonVarietyId = PokemonId.create(response.getId());
                    var pokemonVarietyName = PokemonName.create(response.getName());
                    pokemonMediaHandler.handle(speciesId, pokemonVarietyId, pokemonVarietyName, response.getCries());
                    pokemonMediaHandler.handle(speciesId, pokemonVarietyId, pokemonVarietyName, response.getSprites());
                })
                .map(response -> PokemonPrototype.newBuilder()
                        .setId(response.getId())
                        .setName(response.getName())
                        .setWeight(toKilogram(response.getWeight()))
                        .setHeight(toMeter(response.getHeight()))
                        .setIsDefault(response.getIsDefault())
                        .addAllTypes(response.getTypesList())
                        .addAllStats(response.getStatsList()
                                .stream()
                                .map(statsPrototype -> PokemonStatPrototype.newBuilder()
                                        .setValue(statsPrototype.getValue())
                                        .setName(statsPrototype.getName())
                                        .build()).toList())
                        .addAllMedia(Collections.emptyList())
                        .build())
                .toList();
    }

    private Iterable<PokemonEvolutionPrototype> toEvolutions(URI uri) {
        var response = Stream.ofNullable(uri)
                .map(URI::toString)
                .peek(uriString -> log.debug("fetch evolution chain: {}", uriString))
                .map(StringValue::of)
                .map(pokeApiBouncerClient::fetchEvolutionChain)
                .findFirst()
                .orElseThrow();
        if (CollectionUtils.isEmpty(response.getChain().getFirstChainList())) {
            return Collections.emptyList();
        }
        var chain = response.getChain();
        var evolutions = new ArrayList<PokemonEvolutionPrototype>();
        evolutions.add(PokemonEvolutionPrototype.newBuilder()
                .setOrder(0)
                .setName(chain.getSpeciesName())
                .build());
        return getEvolutions(chain.getFirstChainList(), evolutions);
    }

    private Iterable<PokemonGeneraPrototype> toGenera(List<GeneraPrototype> generaList) {
        return generaList.stream()
                .map(generaPrototype -> PokemonGeneraPrototype.newBuilder()
                        .setGenera(generaPrototype.getGenus())
                        .setLanguage(generaPrototype.getLanguage())
                        .build())
                .toList();
    }

    private Iterable<PokemonNamePrototype> toPokemonNames(List<NamePrototype> namesList) {
        return namesList.stream()
                .map(namePrototype -> PokemonNamePrototype.newBuilder()
                        .setName(namePrototype.getName())
                        .setLanguage(namePrototype.getLanguage())
                        .build())
                .toList();
    }

    private Iterable<PokemonDescriptionPrototype> toDescriptions(List<FlavorTextEntriesPrototype> flavorTextEntriesList) {
        return flavorTextEntriesList.stream()
                .map(flavorText -> {
                    var matcher = REPLACE_LINE_BREAKS.reset(flavorText.getFlavorText());
                    var description = matcher.replaceAll(" ");
                    return PokemonDescriptionPrototype.newBuilder()
                            .setDescription(description)
                            .setLanguage(flavorText.getLanguage())
                            .build();
                })
                .toList();
    }

    private List<PokemonEvolutionPrototype> getEvolutions(List<EvolutionChainPrototype> next,
                                                          ArrayList<PokemonEvolutionPrototype> evolutions) {
        while (true) {
            if (CollectionUtils.isEmpty(next)) {
                return evolutions;
            }
            var chain = next.getFirst();
            evolutions.add(PokemonEvolutionPrototype.newBuilder()
                    .setOrder(evolutions.size())
                    .setName(chain.getSpeciesName())
                    .build());
            next = chain.getNextChainList();
        }
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

    private PokemonSpecialPrototype toSpecial(boolean isBaby, boolean isLegendary, boolean isMythical) {
        var isSpecial = isBaby && isLegendary && isMythical;
        return PokemonSpecialPrototype.newBuilder()
                .setIsSpecial(isSpecial)
                .setBaby(isBaby)
                .setLegendary(isLegendary)
                .setMythical(isMythical)
                .build();
    }
}
