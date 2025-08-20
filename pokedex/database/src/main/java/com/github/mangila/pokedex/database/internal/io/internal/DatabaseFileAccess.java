package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFileHeader;
import com.github.mangila.pokedex.database.internal.io.internal.model.Entry;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.model.OffsetBoundary;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;

public record DatabaseFileAccess(DatabaseFileChannelHandler channelHandler) {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFileAccess.class);

    public Entry read(
            ByteBuffer readBuffer,
            long position,
            boolean flip
    ) throws IOException {
        channelHandler.read(readBuffer, position);
        if (flip) {
            // Flip the readBuffer, set the position to zero
            readBuffer.flip();
        }
        return new Entry(readBuffer);
    }

    public OffsetBoundary append(Entry entry) throws IOException {
        try (FileLock lock = acquireHeaderLock()) {
            DatabaseFileHeader currentHeader = readHeader();
            long currentOffset = currentHeader.offset().value();
            channelHandler.write(entry.value(), currentOffset);
            Offset newOffset = new Offset(currentHeader.offset().value() + entry.value().capacity());
            DatabaseFileHeader newHeader = DatabaseFileHeader.from(currentHeader, newOffset);
            writeHeader(newHeader);
            return new OffsetBoundary(currentHeader.offset(), newOffset);
        }
    }

    private FileLock acquireHeaderLock() throws IOException {
        boolean shared = false;
        return channelHandler.acquireLock(0, DatabaseFileHeader.HEADER_SIZE.value(), shared);
    }

    public DatabaseFileHeader readHeader() throws IOException {
        ByteBuffer buffer = BufferUtils.newByteBuffer((int) DatabaseFileHeader.HEADER_SIZE.value());
        channelHandler.read(buffer, 0);
        buffer.flip();
        DatabaseFileHeader header = DatabaseFileHeader.from(buffer);
        LOGGER.debug("Read header {} - {}", header, channelHandler.fileName());
        return header;
    }

    public void writeHeader(DatabaseFileHeader header) throws IOException {
        LOGGER.debug("Writing header {} - {}", header, channelHandler.fileName());
        channelHandler.write(header.toByteBuffer(Boolean.TRUE), 0);
    }

    public boolean isEmpty() throws IOException {
        return channelHandler.size() == 0;
    }
}
