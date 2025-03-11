package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.github.mangila.pokedex.backstage.model.grpc.model.MediaValue;
import com.github.mangila.pokedex.backstage.model.grpc.service.GridFsGrpc;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.grpc.server.service.GrpcService;

import java.io.ByteArrayInputStream;

@GrpcService
public class GridFsService extends GridFsGrpc.GridFsImplBase {

    private final GridFsTemplate gridFsTemplate;

    public GridFsService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public void storeOne(MediaValue request, StreamObserver<StringValue> responseObserver) {
        var id = gridFsTemplate.store(
                new ByteArrayInputStream(request.getContent().toByteArray()),
                request.getFileName(),
                request.getContentType());
        responseObserver.onNext(StringValue.of(id.toString()));
        responseObserver.onCompleted();
    }
}
