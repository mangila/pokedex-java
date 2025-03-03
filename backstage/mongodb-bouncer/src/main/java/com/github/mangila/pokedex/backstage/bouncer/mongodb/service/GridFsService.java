package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.GridFsOperationGrpc;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class GridFsService extends GridFsOperationGrpc.GridFsOperationImplBase {

    private final GridFsTemplate gridFsTemplate;

    public GridFsService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }
}
