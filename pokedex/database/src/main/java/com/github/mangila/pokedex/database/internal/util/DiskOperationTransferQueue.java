package com.github.mangila.pokedex.database.internal.util;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class DiskOperationTransferQueue<T> {

    private final TransferQueue<T> queue;

    public DiskOperationTransferQueue() {
        this.queue = new LinkedTransferQueue<>();
    }
}
