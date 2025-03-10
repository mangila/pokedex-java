package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.github.mangila.pokedex.backstage.model.grpc.service.GridFsGrpc;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class GridFsService extends GridFsGrpc.GridFsImplBase {

    private final GridFsTemplate gridFsTemplate;

    public GridFsService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }
}
