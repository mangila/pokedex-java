package com.github.mangila.pokedex.graphql.service;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@lombok.AllArgsConstructor
public class GridFsService {

    private final GridFsTemplate gridFsTemplate;

    public Optional<GridFsResource> findByFileName(String fileName) {
        var q = new Query(Criteria.where("filename").is(fileName));
        var fileInfo = gridFsTemplate.findOne(q);
        if (Objects.nonNull(fileInfo)) {
            return Optional.of(gridFsTemplate.getResource(fileInfo));
        }
        return Optional.empty();
    }

}
