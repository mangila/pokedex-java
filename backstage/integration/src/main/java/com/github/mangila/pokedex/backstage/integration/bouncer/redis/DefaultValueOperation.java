package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationGrpc;
import com.google.protobuf.Empty;

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
    public Optional<String> get(EntryRequest request) {
        var response = valueOperationBlockingStub.get(request);
        if (response.getValue().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(response.getValue());
    }
}
