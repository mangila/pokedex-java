package com.github.mangila.pokedex.backstage.shared.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.model.EntryRequest;
import com.github.mangila.pokedex.backstage.model.grpc.service.ValueOperationGrpc;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.util.Optional;

class DefaultValueOperation implements ValueOperation {

    private final ValueOperationGrpc.ValueOperationBlockingStub valueOperationBlockingStub;

    public DefaultValueOperation(ValueOperationGrpc.ValueOperationBlockingStub valueOperationBlockingStub) {
        this.valueOperationBlockingStub = valueOperationBlockingStub;
    }

    @Override
    public Empty set(EntryRequest request) {
        return valueOperationBlockingStub.set(request);
    }

    @Override
    public <T extends Message> Optional<T> get(EntryRequest request, Class<T> clazz) {
        try {
            var response = valueOperationBlockingStub.get(request);
            return Optional.ofNullable(response.unpack(clazz));
        } catch (InvalidProtocolBufferException e) {
            return Optional.empty();
        }
    }

}
