package com.github.mangila.pokedex.graphql.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class GridFsService {

    private static final Logger logger = LoggerFactory.getLogger(GridFsService.class);
    private final GridFsTemplate gridFsTemplate;

    public GridFsService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
        logger.debug("GridFsService initialized with template: {}", gridFsTemplate);
    }

    public Optional<GridFsResource> findByFileName(String fileName) {
        logger.debug("Finding GridFS file by filename: {}", fileName);
        var q = new Query(Criteria.where("filename").is(fileName));

        logger.trace("Executing GridFS query: {}", q);
        var fileInfo = gridFsTemplate.findOne(q);

        if (Objects.nonNull(fileInfo)) {
            logger.info("Found GridFS file: id={}, filename={}, length={}", 
                    fileInfo.getObjectId(), fileInfo.getFilename(), fileInfo.getLength());
            return Optional.of(gridFsTemplate.getResource(fileInfo));
        }

        logger.warn("GridFS file not found: {}", fileName);
        return Optional.empty();
    }

}
