package com.github.mangila.pokedex.backstage.pokemon.mapper;

import com.github.mangila.pokedex.backstage.bouncer.pokeapi.response.evolutionchain.EvolutionChain;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.response.pokemon.Stats;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.response.pokemon.Types;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonSpeciesPrototype;
import com.github.mangila.pokedex.backstage.pokemon.handler.PokemonHandler;
import com.github.mangila.pokedex.backstage.pokemon.handler.PokemonMediaHandler;
import com.github.mangila.pokedex.backstage.shared.model.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonId;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonName;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonStat;
import com.github.mangila.pokedex.backstage.shared.model.domain.PokemonType;
import com.github.mangila.pokedex.backstage.shared.util.JavaStreamUtil;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class PokeApiMapper {

    private static final Logger log = LoggerFactory.getLogger(PokeApiMapper.class);
    private static final Matcher REPLACE_LINE_BREAKS = Pattern.compile("[\r\n\t\f]+").matcher(Strings.EMPTY);

    private final PokemonHandler pokemonHandler;
    private final PokemonMediaHandler pokemonMediaHandler;

    public PokeApiMapper(PokemonHandler pokemonHandler,
                         PokemonMediaHandler pokemonMediaHandler) {
        this.pokemonHandler = pokemonHandler;
        this.pokemonMediaHandler = pokemonMediaHandler;
    }

    public PokemonSpeciesPrototype toDocument(PokemonSpeciesResponse response) {
        var id = PokemonId.create(response.id());
        var name = PokemonName.create(response.name());
        var evolutionChainUrl = URI.create(response.evolutionChain().url());
        return new PokemonSpeciesDocument(
                id.getValueAsInteger(),
                name.getValue(),
                response.generation().name(),
                toPokemonNameDocuments(response.names()),
                toPokemonDescriptionDocuments(response.flavorTextEntries()),
                toPokemonGeneraDocuments(response.genera()),
                toPokemonEvolutionDocuments(evolutionChainUrl),
                toPokemonDocuments(id, response.varieties()),
                toPokemonSpecialDocument(response.legendary(), response.mythical(), response.baby())
        );
    }

    private static List<PokemonNameDocument> toPokemonNameDocuments(List<Names> names) {
        return names.stream()
                .map(name -> new PokemonNameDocument(name.name(), name.language().name()))
                .toList();
    }

    private static List<PokemonDescriptionDocument> toPokemonDescriptionDocuments(List<FlavorTextEntries> favorTextEntries) {
        return favorTextEntries.stream()
                .filter(JavaStreamUtil.distinctByKey(text -> text.language().name()))
                .map(textEntries -> {
                    var matcher = REPLACE_LINE_BREAKS.reset(textEntries.flavorText());
                    var description = matcher.replaceAll(" ");
                    return new PokemonDescriptionDocument(
                            description,
                            textEntries.language().name()
                    );
                })
                .toList();
    }

    private List<PokemonGeneraDocument> toPokemonGeneraDocuments(List<Genera> genera) {
        return genera.stream()
                .map(g -> new PokemonGeneraDocument(g.genus(), g.language().name()))
                .toList();
    }

    private List<PokemonEvolutionDocument> toPokemonEvolutionDocuments(URI evolutionChainUrl) {
        var response = Stream.ofNullable(evolutionChainUrl)
                .map(pokemonHandler::fetchEvolutionChain)
                .findFirst()
                .orElseThrow();
        if (CollectionUtils.isEmpty(response.chain().firstChain())) {
            return Collections.emptyList();
        }
        var chain = response.chain();
        var evolutions = new ArrayList<PokemonEvolutionDocument>();
        evolutions.add(new PokemonEvolutionDocument(0, chain.species().name()));
        return getEvolutions(chain.firstChain(), evolutions);
    }

    private List<PokemonEvolutionDocument> getEvolutions(List<EvolutionChain> next,
                                                         ArrayList<PokemonEvolutionDocument> evolutions) {
        while (true) {
            if (CollectionUtils.isEmpty(next)) {
                return evolutions;
            }

            var chain = next.getFirst();
            evolutions.add(new PokemonEvolutionDocument(evolutions.size(), chain.species().name()));
            next = chain.nextChain();
        }
    }

    private List<PokemonDocument> toPokemonDocuments(PokemonId speciesId, List<Varieties> varieties) {
        return varieties.stream()
                .peek(variety -> log.debug("Fetching varieties for pokemon: {}", variety.pokemon().name()))
                .map(variety -> variety.pokemon().name())
                .map(PokemonName::create)
                .map(PokemonName::getValue)
                .map(pokemonHandler::fetchPokemon)
                .peek(pokemonResponse -> {
                    log.debug("adding pokemon media for pokemon: {}", pokemonResponse.name());
                    var pokemonVarietyId = PokemonId.create(pokemonResponse.id());
                    var pokemonVarietyName = PokemonName.create(pokemonResponse.name());
                    var idPair = Pair.of(speciesId, pokemonVarietyId);
                    pokemonMediaHandler.handle(idPair, pokemonVarietyName, pokemonResponse.sprites());
                    pokemonMediaHandler.handle(idPair, pokemonVarietyName, pokemonResponse.cries());
                })
                .map(this::toDocument)
                .toList();
    }

    private PokemonDocument toDocument(PokemonResponse pokemonResponse) {
        return new PokemonDocument(
                pokemonResponse.id(),
                pokemonResponse.name(),
                pokemonResponse.isDefault(),
                toMeter(pokemonResponse.height()),
                toKilogram(pokemonResponse.weight()),
                toTypes(pokemonResponse.types()),
                toStats(pokemonResponse.stats()),
                Collections.emptyList()
        );
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

    private List<PokemonTypeDocument> toTypes(List<Types> types) {
        return types.stream()
                .map(type -> type.type().name())
                .map(PokemonType::from)
                .map(PokemonType::getType)
                .map(PokemonTypeDocument::new)
                .toList();
    }

    private List<PokemonStatDocument> toStats(List<Stats> stats) {
        var total = new AtomicInteger();
        var documents = new ArrayList<>(stats.stream()
                .map(s -> {
                    var stat = PokemonStat.from(s.stat().name());
                    var baseStat = s.baseStat();
                    total.addAndGet(baseStat);
                    stat.setValue(baseStat);
                    return stat;
                })
                .map(pokemonStat -> new PokemonStatDocument(
                        pokemonStat.getStat(),
                        pokemonStat.getValue()))
                .toList());
        documents.add(new PokemonStatDocument("total", total.get()));
        return documents;
    }

    private PokemonSpecialDocument toPokemonSpecialDocument(boolean legendary,
                                                            boolean mythical,
                                                            boolean baby) {
        boolean isSpecial = legendary && mythical && baby;
        return new PokemonSpecialDocument(isSpecial, legendary, mythical, baby);
    }
}
