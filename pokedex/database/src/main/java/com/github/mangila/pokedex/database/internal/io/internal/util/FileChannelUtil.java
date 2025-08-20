package com.github.mangila.pokedex.database.internal.io.internal.util;

import com.github.mangila.pokedex.database.internal.io.internal.DatabaseFileHeader;
import com.github.mangila.pokedex.database.internal.io.internal.model.Entry;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.internal.model.OffsetBoundary;
import com.github.mangila.pokedex.shared.util.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public final class FileChannelUtil {

    private FileChannelUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Entry read(
            ByteBuffer buffer,
            long position,
            boolean flip,
            FileChannel fileChannel
    ) throws IOException {
        fileChannel.read(buffer, position);
        if (flip) {
            // Flip the buffer, set the position to zero
            buffer.flip();
        }
        return new Entry(buffer);
    }

    public static FileLock acquireHeaderLock(FileChannel channel) throws IOException {
        // Exclusive lock for writing, basically a Write lock
        boolean shared = false;
        return channel.lock(0, DatabaseFileHeader.HEADER_SIZE.value(), shared);
    }

    public static OffsetBoundary append(Entry entry, FileChannel channel) throws IOException {
        try (FileLock lock = FileChannelUtil.acquireHeaderLock(channel)) {
            DatabaseFileHeader currentHeader = FileChannelUtil.readHeader(lock.channel());
            long currentOffset = currentHeader.offset().value();
            lock.channel().write(entry.value(), currentOffset);
            Offset newOffset = new Offset(currentHeader.offset().value() + entry.value().capacity());
            DatabaseFileHeader newHeader = DatabaseFileHeader.from(currentHeader, newOffset);
            FileChannelUtil.writeHeader(lock.channel(), newHeader);
            return new OffsetBoundary(currentHeader.offset(), newOffset);
        }
    }

    public static DatabaseFileHeader readHeader(FileChannel channel) throws IOException {
        ByteBuffer buffer = BufferUtils.newByteBuffer((int) DatabaseFileHeader.HEADER_SIZE.value());
        channel.read(buffer, 0);
        buffer.flip();
        return DatabaseFileHeader.from(buffer);
    }

    public static void writeHeader(FileChannel channel, DatabaseFileHeader header) throws IOException {
        channel.write(header.toByteBuffer(Boolean.TRUE), 0);
    }
}
