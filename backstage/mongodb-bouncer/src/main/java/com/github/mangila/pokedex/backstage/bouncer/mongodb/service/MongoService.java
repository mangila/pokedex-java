package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.github.mangila.pokedex.backstage.bouncer.mongodb.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.backstage.bouncer.mongodb.mapper.DocumentMapper;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonMediaValue;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpecies;
import com.github.mangila.pokedex.backstage.model.grpc.service.MongoDbGrpc;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class MongoService extends MongoDbGrpc.MongoDbImplBase {

    private final MongoTemplate mongoTemplate;
    private final DocumentMapper documentMapper;

    public MongoService(MongoTemplate mongoTemplate,
                        DocumentMapper documentMapper) {
        this.mongoTemplate = mongoTemplate;
        this.documentMapper = documentMapper;
    }

    @Override
    public void saveOne(PokemonSpecies request, StreamObserver<Empty> responseObserver) {
        mongoTemplate.save(documentMapper.toDocument(request));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void pushMedia(PokemonMediaValue request, StreamObserver<Empty> responseObserver) {
        var speciesId = request.getSpeciesId();
        var pokemonId = request.getPokemonId();
        var query = Query.query(Criteria.where("_id")
                .is(speciesId)
                .and("varieties.pokemon_id")
                .is(pokemonId));
        Update update = new Update();
        update.push("varieties.$.media", documentMapper.toDocument(request));
        mongoTemplate.updateFirst(query, update, PokemonSpeciesDocument.class);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
