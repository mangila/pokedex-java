package com.github.mangila.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class GridFsRepository {

    private final GridFsTemplate gridFsTemplate;

    public ObjectId store(InputStream content,
                          String fileName,
                          String contentType) {
        return gridFsTemplate.store(content, fileName, contentType);
    }

    public Optional<GridFsResource> find(ObjectId mediaId) {
        var q = new Query(Criteria.where("_id").is(mediaId));
        var fileInfo = gridFsTemplate.findOne(q);
        if (Objects.nonNull(fileInfo)) {
            return Optional.of(gridFsTemplate.getResource(fileInfo));
        }
        return Optional.empty();
    }

}
