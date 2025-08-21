package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.Buffer;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFileHeader;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.model.OffsetBoundary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileLock;

public record DatabaseFileAccess(DatabaseFileChannelHandler channelHandler) {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFileAccess.class);

    public Buffer read(Buffer buffer, Offset offset, boolean flip) throws IOException {
        channelHandler.read(buffer, offset);
        if (flip) {
            // Flip the readBuffer, set the position to zero
            buffer.flip();
        }
        return buffer;
    }

    public OffsetBoundary append(Buffer buffer) throws IOException {
        try (FileLock lock = acquireHeaderLock()) {
            DatabaseFileHeader currentHeader = readHeader();
            Offset nextOffset = currentHeader.nextOffset();
            channelHandler.write(buffer, nextOffset);
            Offset newOffset = new Offset(currentHeader.nextOffset().value() + buffer.value().capacity());
            DatabaseFileHeader newHeader = new DatabaseFileHeader(
                    currentHeader.magicNumber(),
                    currentHeader.version(),
                    DatabaseFileHeader.RecordCount.increment(currentHeader.recordCount()),
                    newOffset
            );
            writeHeader(newHeader);
            return new OffsetBoundary(currentHeader.nextOffset(), newOffset);
        }
    }

    private FileLock acquireHeaderLock() throws IOException {
        boolean shared = false;
        return channelHandler.acquireLock(DatabaseFileHeader.HEADER_OFFSET_BOUNDARY, shared);
    }

    public DatabaseFileHeader readHeader() throws IOException {
        Buffer buffer = read(Buffer.from((int) DatabaseFileHeader.HEADER_OFFSET_BOUNDARY.end().value()),
                DatabaseFileHeader.HEADER_OFFSET_BOUNDARY.start(),
                true);
        DatabaseFileHeader header = DatabaseFileHeader.from(buffer);
        LOGGER.debug("Read header {} - {}", header, channelHandler.fileName());
        return header;
    }

    public void writeHeader(DatabaseFileHeader header) throws IOException {
        LOGGER.debug("Writing header {} - {}", header, channelHandler.fileName());
        Buffer buffer = header.toBuffer(true);
        channelHandler.write(buffer, Offset.ZERO);
    }

    public boolean isEmpty() throws IOException {
        return channelHandler.size() == 0;
    }
}
